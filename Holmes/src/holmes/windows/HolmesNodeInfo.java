package holmes.windows;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultFormatter;

import holmes.analyse.XTPN.AlgorithmsXTPN;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import holmes.darkgui.GUIManager;
import holmes.petrinet.simulators.StateSimulator;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.utilities.Tools;

/**
 * Klasa odpowiedzialna za okno właściwości elementu sieci.
 */
public class HolmesNodeInfo extends JFrame {
	@Serial
	private static final long serialVersionUID = 1476738825515760744L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private Place place;
	private Transition transition;
	private JPanel mainInfoPanel;
	private JFrame parentFrame;
	private HolmesNodeInfoActions action = new HolmesNodeInfoActions(this);
	public boolean mainSimulatorActive = false;
	
	private XYSeriesCollection dynamicsSeriesDataSet = null;
	private JFreeChart dynamicsChart;
	private int simSteps = 1000;
	private int repeated = 1;
	private JSpinner transIntervalSpinner;
	private boolean maximumMode = false;
	private boolean singleMode = false;
	private int transInterval = 10;
	private JFormattedTextField avgFiredTextBox;
	
	private SimulatorGlobals.SimNetType choosenNetType = SimulatorGlobals.SimNetType.BASIC;

	//XTPN second panel analysis:
	JTextArea placeSecondPanelResults;
	
	/**
	 * Konstruktor do tworzenia okna właściwości miejsca.
	 * @param place Place - obiekt miejsca
	 * @param papa JFrame - okno wywołujące
	 */
	public HolmesNodeInfo(Place place, JFrame papa) {
		parentFrame = papa;
		this.place = place;
		setTitle(lang.getText("HNIwin_entry001title")+" "+place.getName());

		initializeCommon();
		
		JPanel main = new JPanel(new BorderLayout()); //główny panel okna
		add(main);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
			@Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 32;
			}
			@Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
				super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
			}
		});
		tabbedPane.addTab(lang.getText("HNIwin_entryP002"), Tools.getResIcon16("/icons/nodeViewer/tab1.png")
				, initializePlaceInfo(), lang.getText("HNIwin_entryP002t"));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab(lang.getText("HNIwin_entryP003"), Tools.getResIcon16("/icons/nodeViewer/tab1.png")
				, initializePlaceSecondPanel(), lang.getText("HNIwin_entryP003t"));
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		
		main.add(tabbedPane);
		
	}

	/**
	 * Konstruktor do tworzenia okna właściwości tranzycji.
	 * @param transition Transition - obiekt tranzycji
	 * @param papa JFrame - okno wywołujące
	 */
	public HolmesNodeInfo(Transition transition, JFrame papa) {
		parentFrame = papa;
		this.transition = transition;
		setTitle(lang.getText("HNIwin_entry001title")+" "+transition.getName());
		
		if(transition.timeExtension.isDPN() || transition.timeExtension.isTPN())
			choosenNetType = SimulatorGlobals.SimNetType.TIME;
		
		initializeCommon();
		
		JPanel main = new JPanel(new BorderLayout()); //główny panel okna
		add(main);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
			@Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
				return 32;
			}
			@Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
				super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
			}
		});
		tabbedPane.addTab(lang.getText("HNIwin_entryT004"), Tools.getResIcon16("/icons/nodeViewer/tab1.png")
				, initializeTransitionInfo(), lang.getText("HNIwin_entryT004t"));
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab(lang.getText("HNIwin_entryT005"), Tools.getResIcon16("/icons/nodeViewer/tab1.png")
				, initializeTransSecondPanel(), lang.getText("HNIwin_entryT005t"));
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		main.add(tabbedPane);
	}
	
	public HolmesNodeInfo(MetaNode metanode, JFrame papa) {
		parentFrame = papa;
		setTitle(lang.getText("HNIwin_entryM006title")+" "+metanode.getName()); //MetaNode

		GraphPanel graphPanel = overlord.getWorkspace().getProject().getGraphPanel(metanode.getRepresentedSheetID());
		initializeCommon();

		HolmesSubnetsInfo subnetsInfo = new HolmesSubnetsInfo(graphPanel);
		subnetsInfo.bind(this);
	}
	
	/**
	 * Metoda agregująca główne, wspólne elementy interfejsu miejsc/tranzycji.
	 */
	private void initializeCommon() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00478exception")+"\n"+ex.getMessage(), "error", true);
		}
		
		if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED)
			mainSimulatorActive = true;

		//TODO: sprawdzić czy górne w ogóle potrzebne

		if(overlord.getWorkspace().getProject().isSimulationActive()) {
			mainSimulatorActive = true;
		}

		parentFrame.setEnabled(false);
		setResizable(false);
		setLocation(20, 20);
		setSize(new Dimension(600, 500));
		
		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
		
		this.setVisible(true);
	}

	/**
	 * Metoda odpowiedzialna za elementy interfejsu właściwości dla miejsca sieci.
	 */
	private JPanel initializePlaceInfo() {
		mainInfoPanel = new JPanel(null);
		mainInfoPanel.setBounds(0, 0, 600, 480);
		
		int mPanelX = 0;
		int mPanelY = 0;
		
		//panel informacji podstawowych
		JPanel infoPanel = new JPanel(null);
		infoPanel.setBounds(mPanelX, mPanelY, mainInfoPanel.getWidth()-10, 130);
		infoPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNIwin_entryP007"))); //Place general information
		
		int infPanelX = 10;
		int infPanelY = 20;
		
		//************************* NEWLINE *************************
		
		JLabel labelID = new JLabel(lang.getText("HNIwin_entryP008")); //ID:
		labelID.setBounds(infPanelX, infPanelY, 20, 20);
		infoPanel.add(labelID);
		
		int id = overlord.getWorkspace().getProject().getPlaces().indexOf(place);
		JFormattedTextField idTextBox = new JFormattedTextField(id);
		idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
		idTextBox.setEditable(false);
		infoPanel.add(idTextBox);
		
		JLabel portalLabel = new JLabel(lang.getText("HNIwin_entryP009")); //Portal
		portalLabel.setBounds(infPanelX+60, infPanelY, 50, 20);
		infoPanel.add(portalLabel);
		
		String port = lang.getText("no");
		if(place.isPortal())
			port = lang.getText("yes");
		
		JLabel portalLabel2 = new JLabel(port);
		portalLabel2.setBounds(infPanelX+110, infPanelY, 40, 20);
		infoPanel.add(portalLabel2);
		
		JLabel tokenLabel = new JLabel(lang.getText("HNIwin_entryP010"), JLabel.LEFT); //Tokens
        tokenLabel.setBounds(infPanelX+160, infPanelY, 50, 20);
        infoPanel.add(tokenLabel);
        
        int tok = place.getTokensNumber();
        boolean problem = false;
        if(tok < 0) {
        	overlord.log(lang.getText("LOGentry00479")+" "+place.getName(), "error", true);
        	tok = 0;
        	problem = true;
        }
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(tok, 0, Integer.MAX_VALUE, 1);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.setLocation(infPanelX+215, infPanelY);
		tokenSpinner.setSize(45, 20);
		tokenSpinner.setMaximumSize(new Dimension(60,20));
		tokenSpinner.setMinimumSize(new Dimension(60,20));
		if(mainSimulatorActive || problem) {
			tokenSpinner.setEnabled(false);
		}
		tokenSpinner.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			int tokens = (int) spinner.getValue();
			action.setTokens(place, tokens);

			if(overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN == 0) {
				ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
				overlord.getWorkspace().getProject().accessStatesManager().getStatePN(0).setTokens(places.indexOf(place), tokens);
			}
		});
		infoPanel.add(tokenSpinner);
		
		
		int inTrans = 0;
		int outTrans = 0;
		for (ElementLocation el : place.getElementLocations()) {
			inTrans += el.getInArcs().size(); //tyle tranzycji kieruje tutaj łuk
			outTrans += el.getOutArcs().size();
		}
		
		JLabel inTransLabel = new JLabel(lang.getText("HNIwin_entryP011")); //IN-Trans:
		inTransLabel.setBounds(infPanelX+270, infPanelY, 75, 20);
		infoPanel.add(inTransLabel);
		
		JFormattedTextField inTransTextBox = new JFormattedTextField(inTrans);
		inTransTextBox.setBounds(infPanelX+345, infPanelY, 30, 20);
		inTransTextBox.setEditable(false);
		infoPanel.add(inTransTextBox);
		
		JLabel outTransLabel = new JLabel(lang.getText("HNIwin_entryP012")); //OUT-Trans:
		outTransLabel.setBounds(infPanelX+380, infPanelY, 75, 20);
		infoPanel.add(outTransLabel);
		
		JFormattedTextField outTransTextBox = new JFormattedTextField(outTrans);
		outTransTextBox.setBounds(infPanelX+465, infPanelY, 30, 20);
		outTransTextBox.setEditable(false);
		infoPanel.add(outTransTextBox);
		
		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel labelName = new JLabel(lang.getText("HNIwin_entryP013")); //Name:
		labelName.setBounds(infPanelX, infPanelY, 40, 20);
		infoPanel.add(labelName);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setLocation(infPanelX+60, infPanelY);
		nameField.setSize(460, 20);
		nameField.setValue(place.getName());
		nameField.addPropertyChangeListener("value", e -> {
			JFormattedTextField field = (JFormattedTextField) e.getSource();
			try {
				field.commitEdit();
			} catch (ParseException ex) {
				overlord.log(lang.getText("LOGentry00480exception")+"\n"+ex.getMessage(), "error", true);
			}
			String newName = field.getText();

			action.changeName(place, newName);
			action.parentTableUpdate(parentFrame, newName);
		});
		infoPanel.add(nameField);

		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel commmentLabel = new JLabel(lang.getText("HNIwin_entryP014"), JLabel.LEFT); //Comm.:
		commmentLabel.setBounds(infPanelX, infPanelY, 50, 20);
		infoPanel.add(commmentLabel);
		
		JTextArea commentField = new JTextArea(place.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	String newComment = "";
            	if(field != null)
            		newComment = field.getText();
            	
				action.changeComment(place, newComment);
            }
        });
		
        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+60, infPanelY, 460, 60);

        infoPanel.add(creationPanel);
        //infPanelY += 60;
        
		mainInfoPanel.add(infoPanel);
		
		JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
		chartMainPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNIwin_entryP015"))); //Places chart
		chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-10, 245);
		chartMainPanel.add(createChartPanel(place), BorderLayout.CENTER);
		mainInfoPanel.add(chartMainPanel);
		
		
		JPanel chartButtonPanel = panelButtonsPlace(infoPanel, chartMainPanel); //dolny panel przycisków
		mainInfoPanel.add(chartButtonPanel);
		
		fillPlaceDynamicData(chartMainPanel);
		
		return mainInfoPanel;
	}

	/**
	 * Metoda tworzy drugą zakładkę
	 * @return JPanel - panel z wykresem
	 */
	private JPanel initializePlaceSecondPanel() {
		JPanel secondaryTabMainPanel = new JPanel(null);
		secondaryTabMainPanel.setBounds(0, 0, 600, 480);
		
		int mPanelX = 0;
		int mPanelY = 0;

		//panel informacji podstawowych
		JPanel analP_firstPanel = new JPanel(null);
		analP_firstPanel.setBounds(mPanelX, mPanelY, secondaryTabMainPanel.getWidth()-20, 200);
		analP_firstPanel.setBorder(BorderFactory.createTitledBorder("Analysis:"));

		int subPanelX = 10;
		int subPanelY = 20;

		//************************* NEWLINE *************************

		JLabel labelID = new JLabel("ID:"); //ID:
		labelID.setBounds(subPanelX, subPanelY, 20, 20);
		analP_firstPanel.add(labelID);

		int id = overlord.getWorkspace().getProject().getPlaces().indexOf(place);
		JFormattedTextField idTextBox = new JFormattedTextField(id);
		idTextBox.setBounds(subPanelX+20, subPanelY, 30, 20);
		idTextBox.setEditable(false);
		analP_firstPanel.add(idTextBox);

		subPanelY+=30;

		JButton button1 = new JButton("Test"); //Test
		button1.setBounds(subPanelX, subPanelY, 100, 32);
		button1.setMargin(new Insets(0, 0, 0, 0));
		button1.setIcon(Tools.getResIcon32("/icons/nodeViewer/iconInv.png"));
		button1.setToolTipText("Show information about t-invariants going through place");
		button1.addActionListener(actionEvent -> {

		});
		analP_firstPanel.add(button1);

		subPanelY+=40;

		placeSecondPanelResults = new JTextArea();
		placeSecondPanelResults.setLineWrap(true);
		JPanel CreationPanel = new JPanel();
		CreationPanel.setLayout(new BorderLayout());
		CreationPanel.add(new JScrollPane(placeSecondPanelResults), BorderLayout.CENTER);
		CreationPanel.setBounds(subPanelX, subPanelY, 400, 40);
		analP_firstPanel.add(CreationPanel);


		secondaryTabMainPanel.add(analP_firstPanel);


		JPanel analP_secondPanel = new JPanel(null);
		analP_secondPanel.setBounds(mPanelX, analP_firstPanel.getHeight(), mainInfoPanel.getWidth()-20, 130);
		analP_secondPanel.setBorder(BorderFactory.createTitledBorder("Analysis")); //Analysis
		subPanelX = 10;
		subPanelY = 20;

		final JProgressBar progressBar = new JProgressBar();
		JButton tInvButton = new JButton("t-inv list"); //t-inv list
		tInvButton.setBounds(subPanelX, subPanelY, 120, 26);
		tInvButton.setMargin(new Insets(0, 0, 0, 0));
		tInvButton.setIcon(Tools.getResIcon32("/icons/nodeViewer/iconInv.png"));
		tInvButton.setToolTipText("Show information about t-invariants going through place");
		tInvButton.addActionListener(actionEvent -> action.showTinvForPlace(place, progressBar));
		analP_secondPanel.add(tInvButton);

		progressBar.setBounds(subPanelX+130, subPanelY, 400, 30);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBar.setBorder(border);
		analP_secondPanel.add(progressBar);

		secondaryTabMainPanel.add(analP_secondPanel);
		
		return secondaryTabMainPanel;
	}

	/**
	 * Metoda tworzy dolny panel / pasek przycisków okna miejsc.
	 * @param infoPanel JPanel - panel informacji o tranzycji
	 * @param chartMainPanel JPanel - panel wykresu
	 * @return JPanel - panel dolnych przycisków
	 */
	private JPanel panelButtonsPlace(JPanel infoPanel, JPanel chartMainPanel) {
		JPanel chartButtonPanel = new JPanel(null);
		chartButtonPanel.setBounds(0, infoPanel.getHeight()+chartMainPanel.getHeight(), mainInfoPanel.getWidth()-10, 50);
		
		int chartX = 5;
		int chartY_1st = 0;
		int chartY_2nd = 15;
		
		JButton acqDataButton = new JButton(lang.getText("HNIwin_entryP016")); //SimStart
		acqDataButton.setBounds(chartX, chartY_2nd, 110, 25);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText(lang.getText("HNIwin_entryP016t"));
		acqDataButton.addActionListener(actionEvent -> acquireNewPlaceData());
		chartButtonPanel.add(acqDataButton);
		
		JLabel labelSteps = new JLabel(lang.getText("HNIwin_entryP017")); //Sim. Steps:
		labelSteps.setBounds(chartX+120, chartY_1st, 70, 15);
		chartButtonPanel.add(labelSteps);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 50000, 100);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(chartX +120, chartY_2nd, 80, 25);
		simStepsSpinner.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			simSteps = (int) spinner.getValue();
		});
		chartButtonPanel.add(simStepsSpinner);
		
		JLabel labelRep = new JLabel(lang.getText("HNIwin_entryP018")); //Repeated:
		labelRep.setBounds(chartX+210, chartY_1st, 70, 15);
		chartButtonPanel.add(labelRep);
		
		SpinnerModel simStepsRepeatedSpinnerModel = new SpinnerNumberModel(repeated, 1, 50, 1);
		JSpinner simStepsRepeatedSpinner = new JSpinner(simStepsRepeatedSpinnerModel);
		simStepsRepeatedSpinner.setBounds(chartX +210, chartY_2nd, 60, 25);
		simStepsRepeatedSpinner.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			repeated = (int) spinner.getValue();
		});
		chartButtonPanel.add(simStepsRepeatedSpinner);
		
		
		JLabel label1 = new JLabel("Mode:"); //Mode:
		label1.setBounds(chartX+280, chartY_1st, 50, 15);
		chartButtonPanel.add(label1);
		
		final JComboBox<String> simMode = new JComboBox<String>(new String[] {lang.getText("HNIwin_entryP019op1")
				, lang.getText("HNIwin_entryP019op2"), lang.getText("HNIwin_entryP019op3")}); //50/50 mode, Maximum mode, Single mode
		simMode.setToolTipText(lang.getText("HNIwin_entryP019t"));
		simMode.setBounds(chartX+280, chartY_2nd, 120, 25);
		simMode.setSelectedIndex(0);
		simMode.setMaximumRowCount(6);
		simMode.addActionListener(actionEvent -> {
			int selected = simMode.getSelectedIndex();
			if(selected == 0) {
				maximumMode = false;
				singleMode = false;
			} else if(selected == 1) {
				maximumMode = true;
				singleMode = false;
			} else {
				singleMode = true;
			}
		});
		chartButtonPanel.add(simMode);
		
		String[] simModeName = {lang.getText("HNIwin_entryP020op1"), lang.getText("HNIwin_entryP020op2")
				, lang.getText("HNIwin_entryP020op3")}; //Petri Net, Timed Petri Net, Hybrid mode
		final JComboBox<String> simNetMode = new JComboBox<String>(simModeName);
		simNetMode.setBounds(chartX+400, chartY_2nd, 120, 25);
		simNetMode.setSelectedIndex(0);
		simNetMode.addActionListener(actionEvent -> {
			int selectedModeIndex = simNetMode.getSelectedIndex();
			switch (selectedModeIndex) {
				case 0 -> choosenNetType = SimulatorGlobals.SimNetType.BASIC;
				case 1 -> choosenNetType = SimulatorGlobals.SimNetType.TIME;
				case 2 -> choosenNetType = SimulatorGlobals.SimNetType.HYBRID;
			}
		});
		chartButtonPanel.add(simNetMode);
		
		return chartButtonPanel;
	}
	
	/**
	 * Metoda odpowiedzialna za elementy interfejsu właściwości dla tranzycji sieci.
	 */
	private JPanel initializeTransitionInfo() {
		mainInfoPanel = new JPanel(null);
		mainInfoPanel.setBounds(0, 0, 600, 450);
		
		int mPanelX = 0;
		int mPanelY = 0;
		
		//panel informacji podstawowych
		JPanel infoPanel = new JPanel(null);
		infoPanel.setBounds(mPanelX, mPanelY, 580, 130);
		infoPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNIwin_entryT036"))); //Transition general information
		
		int infPanelX = 10;
		int infPanelY = 20;
		
		//************************* NEWLINE *************************
		
		JLabel labelID = new JLabel(lang.getText("HNIwin_entryT021")); //ID:
		labelID.setBounds(infPanelX, infPanelY, 20, 20);
		infoPanel.add(labelID);
		
		int id = overlord.getWorkspace().getProject().getTransitions().indexOf(transition);
		JFormattedTextField idTextBox = new JFormattedTextField(id);
		idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
		idTextBox.setEditable(false);
		infoPanel.add(idTextBox);
		
		JLabel portalLabel = new JLabel(lang.getText("HNIwin_entryT022")); //Portal
		portalLabel.setBounds(infPanelX+60, infPanelY, 50, 20);
		infoPanel.add(portalLabel);
		
		String port = lang.getText("no");
		if(transition.isPortal())
			port = lang.getText("yes");
		
		JLabel portalLabel2 = new JLabel(port);
		portalLabel2.setBounds(infPanelX+110, infPanelY, 40, 20);
		infoPanel.add(portalLabel2);
		
		JLabel avgFiredLabel = new JLabel(lang.getText("HNIwin_entryT023"), JLabel.LEFT); //Avg.f.:
		avgFiredLabel.setBounds(infPanelX+160, infPanelY, 50, 20);
        infoPanel.add(avgFiredLabel);
        
        avgFiredTextBox = new JFormattedTextField(id);
        //wypełnianie niżej
        avgFiredTextBox.setBounds(infPanelX+215, infPanelY, 50, 20);
        avgFiredTextBox.setEditable(false);
		infoPanel.add(avgFiredTextBox);
		
		int preP = 0;
		int postP = 0;
		for (ElementLocation el : transition.getElementLocations()) {
			preP += el.getInArcs().size(); //tyle miejsc kieruje tutaj łuk
			postP += el.getOutArcs().size();
		}
		
		JLabel prePlaceLabel = new JLabel(lang.getText("HNIwin_entryT024")); //PRE-Place:
		prePlaceLabel.setBounds(infPanelX+270, infPanelY, 75, 20);
		infoPanel.add(prePlaceLabel);
		
		JFormattedTextField prePlaceTextBox = new JFormattedTextField(preP);
		prePlaceTextBox.setBounds(infPanelX+345, infPanelY, 30, 20);
		prePlaceTextBox.setEditable(false);
		infoPanel.add(prePlaceTextBox);
		
		JLabel postPlaceLabel = new JLabel(lang.getText("HNIwin_entryT025")); //POST-Place:
		postPlaceLabel.setBounds(infPanelX+380, infPanelY, 75, 20);
		infoPanel.add(postPlaceLabel);
		
		JFormattedTextField postPlaceTextBox = new JFormattedTextField(postP);
		postPlaceTextBox.setBounds(infPanelX+465, infPanelY, 30, 20);
		postPlaceTextBox.setEditable(false);
		infoPanel.add(postPlaceTextBox);
		
		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel labelName = new JLabel(lang.getText("HNIwin_entryT026")); //Name:
		labelName.setBounds(infPanelX, infPanelY, 40, 20);
		infoPanel.add(labelName);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setLocation(infPanelX+60, infPanelY);
		nameField.setSize(460, 20);
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value", e -> {
			JFormattedTextField field = (JFormattedTextField) e.getSource();
			try {
				field.commitEdit();
			} catch (ParseException ignored) {
			}
			String newName = field.getText();

			action.changeName(transition, newName);
			action.parentTableUpdate(parentFrame, newName);
		});
		infoPanel.add(nameField);
		
		infPanelY += 20;
		//************************* NEWLINE *************************
		
		JLabel commmentLabel = new JLabel(lang.getText("HNIwin_entryT027"), JLabel.LEFT); //Comm.:
		commmentLabel.setBounds(infPanelX, infPanelY, 50, 20);
		infoPanel.add(commmentLabel);
		
		JTextArea commentField = new JTextArea(transition.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	String newComment = "";
            	if(field != null)
            		newComment = field.getText();
            	
				action.changeComment(transition, newComment);
            }
        });
		
        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+60, infPanelY, 460, 60);

        infoPanel.add(creationPanel);
        infPanelY += 60;
        mainInfoPanel.add(infoPanel);
        
        //************************* NEWLINE *************************
        
        JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
        chartMainPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNIwin_entryT028"))); //Transition chart
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-10, 245);
        chartMainPanel.add(createChartPanel(transition), BorderLayout.CENTER);
		mainInfoPanel.add(chartMainPanel);
		
		JPanel chartButtonPanel = panelButtonsTransition(infoPanel, chartMainPanel);
		mainInfoPanel.add(chartButtonPanel);

		try {
			if(!transition.getOutputPlaces().isEmpty() || !transition.getInputPlaces().isEmpty()) {
				fillTransitionDynamicData(avgFiredTextBox, chartMainPanel, chartButtonPanel);
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00481exception")+"\n"+e.getMessage(), "error", true);
		}
		return mainInfoPanel;
	}
	
	private JPanel initializeTransSecondPanel() {
		JPanel secondaryTabMainPanel = new JPanel(null);
		secondaryTabMainPanel.setBounds(0, 0, 600, 480);

		int mPanelX = 0;
		int mPanelY = 0;

		//panel informacji podstawowych
		JPanel analT_firstPanel = new JPanel(null);
		analT_firstPanel.setBounds(mPanelX, mPanelY, secondaryTabMainPanel.getWidth()-20, 130);
		analT_firstPanel.setBorder(BorderFactory.createTitledBorder("Analysis:")); //XTPN analysis
		int subPanelX = 10;
		int subPanelY = 20;

		JLabel labelID = new JLabel("ID:");
		labelID.setBounds(subPanelX, subPanelY, 20, 20);
		analT_firstPanel.add(labelID);

		int id = overlord.getWorkspace().getProject().getPlaces().indexOf(transition);
		JFormattedTextField idTextBox = new JFormattedTextField(id);
		idTextBox.setBounds(subPanelX+20, subPanelY, 30, 20);
		idTextBox.setEditable(false);
		analT_firstPanel.add(idTextBox);

		secondaryTabMainPanel.add(analT_firstPanel);


		JPanel analT_secondPanel = new JPanel(null);
		analT_secondPanel.setBounds(mPanelX, analT_firstPanel.getHeight(), secondaryTabMainPanel.getWidth()-20, 130);
		analT_secondPanel.setBorder(BorderFactory.createTitledBorder("Analysis:")); //Analysis
		subPanelX = 10;
		subPanelY = 20;

		final JProgressBar progressBar = new JProgressBar();
		JButton tInvButton = new JButton("t-inv list");
		tInvButton.setBounds(subPanelX, subPanelY, 120, 26);
		tInvButton.setMargin(new Insets(0, 0, 0, 0));
		tInvButton.setIcon(Tools.getResIcon32("/icons/nodeViewer/iconInv.png"));
		tInvButton.setToolTipText("Show information about t-invariants going through transition");
		tInvButton.addActionListener(actionEvent -> action.showTinvForTransition(transition, progressBar));
		analT_secondPanel.add(tInvButton);

		progressBar.setBounds(subPanelX+130, subPanelY, 400, 30);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBar.setBorder(border);
		analT_secondPanel.add(progressBar);

		secondaryTabMainPanel.add(analT_secondPanel);

	    
		return secondaryTabMainPanel;
	}

	/**
	 * Metoda tworzy dolny panel / pasek przycisków okna tranzycji.
	 * @param infoPanel JPanel - panel informacji o tranzycji
	 * @param chartMainPanel JPanel - panel wykresu
	 * @return JPanel - panel dolnych przycisków okna tramzycji
	 */
	private JPanel panelButtonsTransition(JPanel infoPanel, JPanel chartMainPanel) {
		JPanel chartButtonPanel = new JPanel(null);
		chartButtonPanel.setBounds(0, infoPanel.getHeight()+chartMainPanel.getHeight(), mainInfoPanel.getWidth()-10, 50);
		
		int chartX = 5;
		int chartY_1st = 0;
		int chartY_2nd = 15;
		
		JButton acqDataButton = new JButton(lang.getText("HNIwin_entryT029")); //SimStart
		acqDataButton.setBounds(chartX, chartY_2nd, 110, 25);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText(lang.getText("HNIwin_entryT029t"));
		acqDataButton.addActionListener(actionEvent -> acquireNewTransitionData());
		chartButtonPanel.add(acqDataButton);
		
		JLabel labelSteps = new JLabel(lang.getText("HNIwin_entryT030")); //Sim. Steps:
		labelSteps.setBounds(chartX+120, chartY_1st, 70, 15);
		chartButtonPanel.add(labelSteps);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 50000, 100);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(chartX+120, chartY_2nd, 80, 25);
		simStepsSpinner.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			simSteps = (int) spinner.getValue();

			//update spinner przerw/srednich dla tranzycji:
			int cVal = simSteps / 100;
			int mVal = simSteps / 5;
			if(cVal < 1)
				cVal = 1;
			if(cVal > mVal) {
				cVal = 1;
				mVal = 20;
			}
			try {
				SpinnerNumberModel spinnerClustersModel = new SpinnerNumberModel(cVal, 1, mVal, 1);
				transIntervalSpinner.setModel(spinnerClustersModel);
				transInterval = cVal;
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00482exception")+ "\n"+ex.getMessage(), "warning", true);
			}
		});
		chartButtonPanel.add(simStepsSpinner);

		JLabel labelInterval = new JLabel(lang.getText("HNIwin_entryT031")); //Interval:
		labelInterval.setBounds(chartX+210, chartY_1st, 80, 15);
		chartButtonPanel.add(labelInterval);
		
		int maxVal = simSteps / 10;
		SpinnerModel intervSpinnerModel = new SpinnerNumberModel(transInterval, 1, maxVal, 1);
		transIntervalSpinner = new JSpinner(intervSpinnerModel);
		transIntervalSpinner.setBounds(chartX+210, chartY_2nd, 60, 25);
		transIntervalSpinner.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			transInterval = (int) spinner.getValue();
			//clearTransitionsChart();
		});
		chartButtonPanel.add(transIntervalSpinner);
		
		JLabel labelMode = new JLabel(lang.getText("HNIwin_entryT032")); //Simulation mode:
		labelMode.setBounds(chartX+280, chartY_1st, 110, 15);
		chartButtonPanel.add(labelMode);
		
		final JComboBox<String> simMode = new JComboBox<String>(new String[] {lang.getText("HNIwin_entryT033op1")
				, lang.getText("HNIwin_entryT033op2"), lang.getText("HNIwin_entryT033op3")}); //50/50 mode, Maximum mode, Single mode
		simMode.setToolTipText(lang.getText("HNIwin_entryT033t"));
		simMode.setBounds(chartX+280, chartY_2nd, 120, 25);
		simMode.setSelectedIndex(0);
		simMode.setMaximumRowCount(6);
		simMode.addActionListener(actionEvent -> {
			int selected = simMode.getSelectedIndex();
			if(selected == 0) {
				maximumMode = false;
				singleMode = false;
			} else if(selected == 1) {
				maximumMode = true;
				singleMode = false;
			} else {
				singleMode = true;
			}
		});
		chartButtonPanel.add(simMode);
		
		String[] simModeName = {lang.getText("HNIwin_entryT034op1")
				, lang.getText("HNIwin_entryT034op2"), lang.getText("HNIwin_entryT034op3")}; //Petri Net, Timed Petri Net, Hybrid mode
		final JComboBox<String> simNetMode = new JComboBox<String>(simModeName);
		simNetMode.setBounds(chartX+400, chartY_2nd, 120, 25);
		
		if(choosenNetType == SimulatorGlobals.SimNetType.TIME)
			simNetMode.setSelectedIndex(1);
		else
			simNetMode.setSelectedIndex(0);
		
		simNetMode.addActionListener(actionEvent -> {
			int selectedModeIndex = simNetMode.getSelectedIndex();
			switch (selectedModeIndex) {
				case 0 -> choosenNetType = SimulatorGlobals.SimNetType.BASIC;
				case 1 -> choosenNetType = SimulatorGlobals.SimNetType.TIME;
				case 2 -> choosenNetType = SimulatorGlobals.SimNetType.HYBRID;
			}
		});
		chartButtonPanel.add(simNetMode);
		
		return chartButtonPanel;
	}

	/**
	 * Metoda wypełnia pola danych dynamicznych dla miejsca, tj. symuluje 1000 kroków sieci na bazie
	 * czego ustala liczbę tekenów dla w ramach tej symulacji 
	 * //@param avgFiredTextBox JFormattedTextField - pole z wartością procentową
	 * @param chartMainPanel JPanel - panel wykresu
	 */
	private void fillPlaceDynamicData(JPanel chartMainPanel) {
		if(!mainSimulatorActive) {
			acquireNewPlaceData();			
		} else {
			chartMainPanel.setEnabled(false);
			TextTitle title = dynamicsChart.getTitle();   
			title.setBorder(2, 2, 2, 2);   
			//title.setBackgroundPaint(Color.white); 
			title.setFont(new Font("Dialog", Font.PLAIN, 20));
			title.setExpandToFitSpace(true);
			title.setPaint(Color.red);
			title.setText(lang.getText("HNIwin_entryT035"));
		}
	}

	/**
	 * Metoda wypełnia pola danych dynamicznych dla tranzycji, tj. symuluje 1000 kroków sieci na bazie
	 * czego ustala prawdopodobieństwo uruchomienia tranzycji oraz przedstawia wykres dla symulacji. 
	 * @param avgFiredTextBox JFormattedTextField - pole z wartością procentową
	 * @param chartMainPanel JPanel - panel wykresu
	 */
	private void fillTransitionDynamicData(JFormattedTextField avgFiredTextBox, JPanel chartMainPanel, JPanel chartButtonPanel) {
		if(!mainSimulatorActive) {
			ArrayList<Integer> dataVector = acquireNewTransitionData();
			if(!dataVector.isEmpty()) {
				int sum = dataVector.get(dataVector.size()-2);
	    		int steps = dataVector.get(dataVector.size()-1);
	    		double avgFired = sum;
	    		avgFired /= steps;
	    		avgFired *= 100; // * 100%
	    		avgFiredTextBox.setText(Tools.cutValue(avgFired)+"%");
			}
		} else {
			avgFiredTextBox.setEnabled(false);
        	avgFiredTextBox.setText("n/a");
        	//*********************************************
			chartMainPanel.setEnabled(false);
			chartButtonPanel.setEnabled(false);
			//*********************************************
			TextTitle title = dynamicsChart.getTitle();
			title.setBorder(2, 2, 2, 2);
			title.setFont(new Font("Dialog", Font.PLAIN, 20));
			title.setExpandToFitSpace(true);
			title.setPaint(Color.red);
			title.setText(lang.getText("HNIwin_entryT035"));
		}
	}
	
	/**
	 * Metoda aktywuje symulator dla jednej tranzycji w ustalonym wcześniej trybie i dla wcześniej
	 * ustalonej liczby kroków. Testy są powtarzane ustaloną liczbę razy. Wyniki zapisuje na wykresie.
	 */
	private void acquireNewPlaceData() {
		StateSimulator ss = new StateSimulator();

		SimulatorGlobals ownSettings = new SimulatorGlobals();
		ownSettings.setNetType(choosenNetType, false);
		ownSettings.setMaxMode(maximumMode);
		ownSettings.setSingleMode(singleMode);
		ss.initiateSim(false, ownSettings);
		
		ArrayList<Integer> dataVector = ss.simulateNetSinglePlace(simSteps, place, false);
		ArrayList<ArrayList<Integer>> dataMatrix = new ArrayList<ArrayList<Integer>>();
		dataMatrix.add(dataVector);
		
		int problemCounter = 0;
		int rep_succeed = 1;
		for(int i=1; i<repeated; i++) {
			ss.clearData();
			ArrayList<Integer> newData = ss.simulateNetSinglePlace(simSteps, place, false);
			if(newData.size() < dataVector.size()) { //powtórz test, zły rozmiar danych
				problemCounter++;
				i--;
				if(problemCounter == 10) {
					String strB = "err.";
					try {
						strB = String.format(lang.getText("LOGentry00483"), repeated, simSteps, dataVector.size());
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"LOGentry00483", "error", true);
					}
					overlord.log(strB, "error", true);
					break;
				}
			} else { //ok, taki sam lub dłuższy
				rep_succeed++;
				dataMatrix.add(newData);
				
			}
		}
		
		dynamicsSeriesDataSet.removeAllSeries();
		XYSeries series = new XYSeries("Number of tokens");
		
		if(repeated != 1) {
			ArrayList<Double> dataDVector = new ArrayList<Double>();
			for(int i=0; i<repeated; i++) {
				if(i==0) {
					for(int j=0; j<dataMatrix.get(0).size(); j++) {
						dataDVector.add((double)dataMatrix.get(0).get(j));
					}
				} else {
					for(int j=0; j<dataMatrix.get(i).size(); j++) {
						double oldval = dataDVector.get(j);
						oldval += dataMatrix.get(i).get(j);
						dataDVector.set(j, oldval);
					}
				}
			}

			for(int step=0; step<dataDVector.size(); step++) {
				double value = dataDVector.get(step);
				value /= rep_succeed;
				series.add(step, value);
			}
		} else {
			if(dataVector != null) {
				for(int step=0; step<dataVector.size(); step++) {
					int value = dataVector.get(step);
					series.add(step, value);
				}
			}
		}
		dynamicsSeriesDataSet.addSeries(series);
		dataMatrix.clear();
	}
	
	/**
	 * Metoda aktywuje symulator dla jednej tranzycji w ustalonym wcześniej trybie i dla wcześniej
	 * ustalonej liczby kroków. Wyniki zapisuje na wykresie, zwraca też wektor danych.
	 * @return ArrayList[Integer] - wektor danych z symulatora
	 */
	private ArrayList<Integer> acquireNewTransitionData() {
		StateSimulator ss = new StateSimulator();
		
		SimulatorGlobals ownSettings = new SimulatorGlobals();
		ownSettings.setNetType(choosenNetType, false);
		ownSettings.setMaxMode(maximumMode);
		ownSettings.setSingleMode(singleMode);
		ss.initiateSim(false, ownSettings);

		ArrayList<Integer> dataVector = ss.simulateNetSingleTransition(simSteps, transition, false);
		
		dynamicsSeriesDataSet.removeAllSeries();
		XYSeries series = new XYSeries("Average firing");
		if(dataVector != null) {
			for(int step=0; step<dataVector.size()-2; step++) {
				double value = 0; //suma odpaleń w przedziale czasu
				int interval = transInterval;
				if(step+interval >= dataVector.size()-2)
					interval = dataVector.size() - 2 - step;
				
				for(int i=0; i<interval; i++) {
					try {
						value += dataVector.get(step+i);
					} catch (Exception e) {
						overlord.log("Error Ex: "+e.getMessage(), "error", true);
					}
				}
				value /= interval;
				value *= 100;
				series.add(step, value);
				step += (interval-1);
			}
		}
		dynamicsSeriesDataSet.addSeries(series);

		assert dataVector != null;
		int sum = dataVector.get(dataVector.size()-2);
		int steps = dataVector.get(dataVector.size()-1);
		double avgFired = sum;
		avgFired /= steps;
		avgFired *= 100; // * 100%
		avgFiredTextBox.setText(Tools.cutValue(avgFired)+"%");
		
		return dataVector;
	}
	
	/**
	 * Metoda tworząca podstawowe elementy wykresu okna.
	 * @param node Node - klieknięty wierzchołek
	 * @return JPanel - panel komponentów
	 */
	JPanel createChartPanel(Node node) {
		String chartTitle = node.getName()+ " dynamics";
	    String xAxisLabel = "Simulation steps";
	    String yAxisLabel = "Tokens";
	    if(node instanceof Transition)
	    	yAxisLabel = "Firings chance %";
	    
	    boolean showLegend = false;
	    boolean createTooltip = true;
	    boolean createURL = false;
	    
		dynamicsSeriesDataSet = new XYSeriesCollection();
	    dynamicsChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, dynamicsSeriesDataSet, 
	    		PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
	    
	    dynamicsChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 14));
	    //NOT UNTIL PLOT IN PLACE:
	    //CategoryPlot plot = (CategoryPlot) placesChart.getPlot();
        //Font font = new Font("Dialog", Font.PLAIN, 12); 
      	//plot.getDomainAxis().setLabelFont(font);
      	//plot.getRangeAxis().setLabelFont(font);

		return new ChartPanel(dynamicsChart);
	}
}
