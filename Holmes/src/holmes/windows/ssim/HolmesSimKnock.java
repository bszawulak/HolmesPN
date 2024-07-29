package holmes.windows.ssim;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.StateSimulator;
import holmes.utilities.Tools;
import holmes.windows.managers.HolmesStatesManager;

/**
 * Klasa odpowiedzialna za tworzenie podstrony knockoutSim.
 */
public class HolmesSimKnock extends JPanel {
	@Serial
	private static final long serialVersionUID = 4257940971120618716L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	public HolmesSimKnockActions action;
	public StateSimulator ssimKnock;
	public HolmesSim mainSimWindow;
	
	//reference simulation panel:
	public JProgressBar refProgressBarKnockout;
	public boolean refShowNotepad = false;
	
	//reference details panel:
	private JComboBox<String> referencesCombo = null;
	private JLabel refLabelDate;
	private JLabel refLabelSimNetMode;
	private JLabel refLabelMaxMode;
	private JLabel refLabelSteps;
	private JLabel refLabelReps;
	
	//knockout details panel:
	private JComboBox<String> dataCombo = null;
	private JLabel dataLabelDate;
	private JLabel dataLabelSimNetMode;
	private JLabel dataLabelMaxMode;
	private JLabel dataLabelSteps;
	private JLabel dataLabelReps;
	private JLabel dataLabelDisabled;
	
	//data sets acquisition panel:
	private JComboBox<String> dataTransitionsCombo = null;
	private JComboBox<String> dataMctCombo = null;
	public JProgressBar dataProgressBarKnockout;
	public JTextArea dataSelectedTransTextArea;
	public boolean dataSimUseEditorOffline = false;
	public boolean dataSimComputeAll = false;
	public boolean dataShowNotepad = false;
	
	public JCheckBox dataSimUseEditorOfflineCheckBox;
	public JCheckBox dataSimComputeForAllTransitions;
	
	//blokowane elementy:
	private JButton acqRefDataButton;
	private JButton acqDataSimButton;
	private JButton simSettingsButton; 
	private JButton loadAllButton;
	private JButton saveAllButton;


	public boolean refSimInProgress = false;
	public boolean dataSimInProgress = false;
	
	private JLabel selStateLabel;
	private JLabel selStateDescrLabel;
	private JButton stateManagerButton;

	
	/**
	 * Konstruktor obiektu klasy HolmesStateSimulatorKnockout.
	 * @param holmesStateSimulator (<b>HolmesSim</b>) obiekt symulatora.
	 */
	public HolmesSimKnock(HolmesSim holmesStateSimulator) {
		this.mainSimWindow = holmesStateSimulator;
		ssimKnock = new StateSimulator();
		action = new HolmesSimKnockActions(this);
		
		setLayout(new BorderLayout());
		
		add(getMainOptionsPanel(), BorderLayout.NORTH); //górny panel przycisków
		JPanel allTheRest = new JPanel(new BorderLayout()); //panel pod przyciskami
		add(allTheRest);

		allTheRest.add(getRefAcqPanel(), BorderLayout.NORTH); //dodaj panel uzyskania zbioru ref.
		JPanel refAndDataPanel = new JPanel(new BorderLayout()); //panel grupujący dane ref. i datasets
		refAndDataPanel.add(getRefDetailsPanel(), BorderLayout.NORTH);
		

		JPanel dataAndChartsPanel = new JPanel(new BorderLayout());
		dataAndChartsPanel.add(getSimDataAcqPanel(), BorderLayout.NORTH);
		dataAndChartsPanel.add(getSimDataDetailsPanel(), BorderLayout.CENTER);
		
		refAndDataPanel.add(dataAndChartsPanel, BorderLayout.CENTER);

		allTheRest.add(refAndDataPanel, BorderLayout.CENTER);
	}

	/**
	 * Metoda tworząca panel tworzenia zbioru referencyjnego.
	 * @return JPanel - obiekt panelu
	 */
	public JPanel getRefAcqPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Reference data acquisition panel")); //Reference data acquisition panel
		result.setPreferredSize(new Dimension(670, 100));
	
		int posXda = 10;
		int posYda = 10;
		
		acqRefDataButton = new JButton(lang.getText("HSKwin_entry001")); //SimStart
		acqRefDataButton.setBounds(posXda, posYda+10, 110, 40);
		acqRefDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqRefDataButton.setFocusPainted(false);
		acqRefDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqRefDataButton.setToolTipText(lang.getText("HSKwin_entry001t"));
		acqRefDataButton.addActionListener(actionEvent -> action.acquireDataForRefSet());
		result.add(acqRefDataButton);
		
		JButton cancelButton = new JButton();
		cancelButton.setText(lang.getText("HSKwin_entry002")); //STOP
		cancelButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/stopIcon.png"));
		cancelButton.setToolTipText(lang.getText("HSKwin_entry002t"));
		cancelButton.setBounds(posXda, posYda+55, 110, 25);
		cancelButton.setMargin(new Insets(0, 0, 0, 0));
		cancelButton.setFocusPainted(false);
		cancelButton.addActionListener(actionEvent -> {
			if(refSimInProgress)
				ssimKnock.setCancelStatus(true);
		});
		result.add(cancelButton);

		refProgressBarKnockout = new JProgressBar();
		refProgressBarKnockout.setBounds(posXda+120, posYda+3, 830, 40);
		refProgressBarKnockout.setMaximum(100);
		refProgressBarKnockout.setMinimum(0);
	    refProgressBarKnockout.setValue(0);
	    refProgressBarKnockout.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder(lang.getText("HSKwin_entry003")); //Progress
	    refProgressBarKnockout.setBorder(border);
	    result.add(refProgressBarKnockout);
	    
	    JCheckBox showdataCheckBox = new JCheckBox(lang.getText("HSKwin_entry004")); //Show results in notepad
	    showdataCheckBox.setBounds(posXda+120, posYda+50, 300, 20);
	    showdataCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			refShowNotepad = abstractButton.getModel().isSelected();
		});
		result.add(showdataCheckBox);
		
		//posYda += 40;
		return result;
	}

	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************

	/**
	 * Metoda tworząca panel wyświetlania danych o zbiorach referencyjnych
	 * @return JPanel - obiekt panelu
	 */
	public JPanel getRefDetailsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKwin_entry005"))); //Reference data details panel
		result.setPreferredSize(new Dimension(500, 100));
		mainSimWindow.doNotUpdate = true;
		int posXda = 10;
		int posYda = 20;
		
		JLabel label1 = new JLabel(lang.getText("HSKwin_entry006")); //Ref. sets:
		label1.setBounds(posXda, posYda, 70, 20);
		result.add(label1);
		
		String[] data = { " ----- " };
		referencesCombo = new JComboBox<String>(data); //final, aby listener przycisku odczytał wartość
		referencesCombo.setBounds(posXda+80, posYda, 420, 20);
		referencesCombo.setMaximumRowCount(12);
		referencesCombo.addActionListener(actionEvent -> {
			if(mainSimWindow.doNotUpdate)
				return;

			int selected = referencesCombo.getSelectedIndex();
			if(selected > 0) {
				updateRefDetails(selected-1);
			}
		});
		result.add(referencesCombo);
		
		JButton removeRefButton = new JButton(lang.getText("HSKwin_entry007")); //Remove
		removeRefButton.setBounds(posXda+505, posYda, 100, 20);
		removeRefButton.setMargin(new Insets(0, 0, 0, 0));
		removeRefButton.setFocusPainted(false);
		removeRefButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/removeIcon.png"));
		removeRefButton.setToolTipText(lang.getText("HSKwin_entry007t"));
		removeRefButton.addActionListener(actionEvent -> {
			int selected = referencesCombo.getSelectedIndex();
			if(selected > 0) {
				if(action.removeRedDataSet(selected-1))
					updateFreshKnockoutTab();
			}
		});
		result.add(removeRefButton);

		JLabel dateTxtLabel = new JLabel(lang.getText("HSKwin_entry008")); //Date:
		dateTxtLabel.setBounds(posXda, posYda+20, 90, 20);
		result.add(dateTxtLabel);
		refLabelDate = new JLabel("---");
		refLabelDate.setBounds(posXda+100, posYda+20, 120, 20);
		result.add(refLabelDate);
		
		JLabel modeTxtLabel = new JLabel(lang.getText("HSKwin_entry009")); //Net mode:
		modeTxtLabel.setBounds(posXda+250, posYda+20, 110, 20);
		result.add(modeTxtLabel);
		refLabelSimNetMode = new JLabel("---");
		refLabelSimNetMode.setBounds(posXda+370, posYda+20, 70, 20);
		result.add(refLabelSimNetMode);
		
		JLabel maxModeTxtLabel = new JLabel(lang.getText("HSKwin_entry010")); //Max mode:
		maxModeTxtLabel.setBounds(posXda+450, posYda+20, 90, 20);
		result.add(maxModeTxtLabel);
		refLabelMaxMode = new JLabel("---");
		refLabelMaxMode.setBounds(posXda+550, posYda+20, 90, 20);
		result.add(refLabelMaxMode);
		
		JLabel stepsTxtLabel = new JLabel(lang.getText("HSKwin_entry011")); //Sim. steps:
		stepsTxtLabel.setBounds(posXda, posYda+40, 90, 20);
		result.add(stepsTxtLabel);
		refLabelSteps = new JLabel("---");
		refLabelSteps.setBounds(posXda+100, posYda+40, 80, 20);
		result.add(refLabelSteps);
		
		JLabel repsTxtLabel = new JLabel(lang.getText("HSKwin_entry012")); //Repetitions:
		repsTxtLabel.setBounds(posXda+250, posYda+40, 110, 20);
		result.add(repsTxtLabel);
		refLabelReps = new JLabel("---");
		refLabelReps.setBounds(posXda+370, posYda+40, 90, 20);
		result.add(refLabelReps);
		
		mainSimWindow.doNotUpdate = false;
	    return result;
	}
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	
	public JPanel getSimDataAcqPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKwin_entry013"))); //Knockout data acquisition setup
		result.setPreferredSize(new Dimension(670, 165));
		mainSimWindow.doNotUpdate = true;
		int posXda = 10;
		int posYda = 20;
		
		JLabel transitionsLabel = new JLabel(lang.getText("HSKwin_entry014")); //Transitions:
		transitionsLabel.setBounds(posXda, posYda, 70, 20);
		result.add(transitionsLabel);
		
		String[] data = { " ----- " };
		dataTransitionsCombo = new JComboBox<String>(data);
		dataTransitionsCombo.setBounds(posXda+80, posYda, 400, 20);
		dataTransitionsCombo.setMaximumRowCount(12);
		dataTransitionsCombo.addActionListener(actionEvent -> {
			//int selected = dataTransitionsCombo.getSelectedIndex();
		});
		result.add(dataTransitionsCombo);
		
		JButton addTransButton = new JButton(lang.getText("HSKwin_entry015")); //Add
		addTransButton.setBounds(posXda+485, posYda, 100, 20);
		addTransButton.setMargin(new Insets(0, 0, 0, 0));
		addTransButton.setFocusPainted(false);
		addTransButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/addIcon.png"));
		addTransButton.setToolTipText(lang.getText("HSKwin_entry015t"));
		addTransButton.addActionListener(actionEvent -> {
			int selected = dataTransitionsCombo.getSelectedIndex();
			if(selected > 0)
				action.addOfflineElement(1, selected-1, dataSelectedTransTextArea);
		});
		result.add(addTransButton);
		
		JButton removeTransButton = new JButton(lang.getText("HSKwin_entry016")); //Remove
		removeTransButton.setBounds(posXda+590, posYda, 100, 20);
		removeTransButton.setMargin(new Insets(0, 0, 0, 0));
		removeTransButton.setFocusPainted(false);
		removeTransButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/removeIcon.png"));
		removeTransButton.setToolTipText(lang.getText("HSKwin_entry016t"));
		removeTransButton.addActionListener(actionEvent -> {
			int selected = dataTransitionsCombo.getSelectedIndex();
			if(selected > 0)
				action.removeOfflineElement(1, selected-1, dataSelectedTransTextArea);
		});
		result.add(removeTransButton);
		
		JLabel MCTsLabel = new JLabel(lang.getText("HSKwin_entry017")); //MCT set:
		MCTsLabel.setBounds(posXda, posYda+25, 70, 20);
		result.add(MCTsLabel);
		
		String[] data2 = { " ----- " };
		dataMctCombo = new JComboBox<String>(data2);
		dataMctCombo.setBounds(posXda+80, posYda+25, 400, 20);
		dataMctCombo.setMaximumRowCount(12);
		dataMctCombo.addActionListener(actionEvent -> {
			//int selected = dataMctCombo.getSelectedIndex();
		});
		result.add(dataMctCombo);
		
		JButton addMCTButton = new JButton(lang.getText("HSKwin_entry018")); //Add
		addMCTButton.setBounds(posXda+485, posYda+25, 100, 20);
		addMCTButton.setMargin(new Insets(0, 0, 0, 0));
		addMCTButton.setFocusPainted(false);
		addMCTButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/addIcon.png"));
		addMCTButton.setToolTipText(lang.getText("HSKwin_entry018t"));
		addMCTButton.addActionListener(actionEvent -> {
			int selected = dataMctCombo.getSelectedIndex();
			if(selected > 0)
				action.addOfflineElement(2, selected, dataSelectedTransTextArea);
		});
		result.add(addMCTButton);
		
		JButton removeMCTButton = new JButton(lang.getText("HSKwin_entry019")); //Remove
		removeMCTButton.setBounds(posXda+590, posYda+25, 100, 20);
		removeMCTButton.setMargin(new Insets(0, 0, 0, 0));
		removeMCTButton.setFocusPainted(false);
		removeMCTButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/removeIcon.png"));
		removeMCTButton.setToolTipText(lang.getText("HSKwin_entry019t"));
		removeMCTButton.addActionListener(actionEvent -> {
			int selected = dataMctCombo.getSelectedIndex();
			if(selected > 0)
				action.removeOfflineElement(2, selected, dataSelectedTransTextArea);
		});
		result.add(removeMCTButton);
		
		dataSelectedTransTextArea = new JTextArea();
		dataSelectedTransTextArea.setLineWrap(true);
		dataSelectedTransTextArea.setEditable(true);
        JPanel dataFieldPanel = new JPanel();
        dataFieldPanel.setLayout(new BorderLayout());
        dataFieldPanel.add(new JScrollPane(dataSelectedTransTextArea), BorderLayout.CENTER);
        dataFieldPanel.setBounds(posXda+700, posYda, 165, 45);
        result.add(dataFieldPanel);
        
        JButton clearButton = new JButton(lang.getText("HSKwin_entry020")); //Clear
        clearButton.setBounds(posXda+870, posYda, 90, 45);
        clearButton.setMargin(new Insets(0, 0, 0, 0));
        clearButton.setFocusPainted(false);
        clearButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/clearIcon.png"));
        clearButton.setToolTipText(lang.getText("HSKwin_entry020t"));
        clearButton.addActionListener(actionEvent -> dataSelectedTransTextArea.setText(""));
		result.add(clearButton);
		
		//przyciski symulacji:
		
		posYda+=50;
		
		acqDataSimButton = new JButton(lang.getText("HSKwin_entry021")); //SimStart
		acqDataSimButton.setBounds(posXda, posYda+10, 110, 40);
		acqDataSimButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataSimButton.setFocusPainted(false);
		acqDataSimButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataSimButton.setToolTipText(lang.getText("HSKwin_entry021t"));
		acqDataSimButton.addActionListener(actionEvent -> {
			if(dataSimUseEditorOffline && dataSimComputeAll) {
				JOptionPane.showMessageDialog(mainSimWindow.getFrame(),
						lang.getText("HSKwin_entry022"),
						lang.getText("problem"),JOptionPane.WARNING_MESSAGE);
				return;
			}

			if(dataSimComputeAll) {
				action.acquireAll();
				return;
			}

			action.acquireDataForKnockoutSet(dataSelectedTransTextArea, dataSimUseEditorOffline);
		});
		result.add(acqDataSimButton);
		
		JButton cancelButton = new JButton();
		cancelButton.setText(lang.getText("HSKwin_entry023")); //STOP
		cancelButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/stopIcon.png"));
		cancelButton.setBounds(posXda, posYda+55, 110, 25);
		cancelButton.setMargin(new Insets(0, 0, 0, 0));
		cancelButton.setFocusPainted(false);
		cancelButton.addActionListener(actionEvent -> {
			if(dataSimComputeAll && dataSimInProgress) {
				Object[] options = {"Stop and remove", "Proceed with simulation",}; //Stop and remove, Proceed with simulation
				int n = JOptionPane.showOptionDialog(null,
						lang.getText("HSKwin_entry023msg"),
						lang.getText("HSKwin_entry023t"), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
				if (n == 0) {
					ssimKnock.setCancelStatus(true);
					JOptionPane.showMessageDialog(mainSimWindow.getFrame(), lang.getText("HSKwin_entry024"),
							lang.getText("HSKwin_entry024t"),JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		result.add(cancelButton);
		
		JPanel special = new JPanel(null);
		special.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKwin_entry025"))); //Special options
		special.setBounds(posXda+120, posYda, 770, 40);
		result.add(special);
		
		dataSimUseEditorOfflineCheckBox = new JCheckBox(lang.getText("HSKwin_entry026")); //Manually disabled transitions
		dataSimUseEditorOfflineCheckBox.setBounds(10, 15, 240, 20);
		//dataSimUseEditorOfflineCheckBox.setEnabled(false);
		dataSimUseEditorOfflineCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			dataSimUseEditorOffline = abstractButton.getModel().isSelected();
		});
		special.add(dataSimUseEditorOfflineCheckBox);
		
		dataSimComputeForAllTransitions = new JCheckBox(lang.getText("HSKwin_entry027")); //All transitions (one by one)
		dataSimComputeForAllTransitions.setBounds(260, 15, 200, 20);
		dataSimComputeForAllTransitions.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			dataSimComputeAll = abstractButton.getModel().isSelected();
		});
		special.add(dataSimComputeForAllTransitions);
		
		JCheckBox showdataCheckBox = new JCheckBox(lang.getText("HSKwin_entry028")); //Show results in notepad
	    showdataCheckBox.setBounds(480, 15, 260, 20);
	    showdataCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			dataShowNotepad = abstractButton.getModel().isSelected();
		});
	    special.add(showdataCheckBox);
		
		dataProgressBarKnockout = new JProgressBar();
		dataProgressBarKnockout.setBounds(posXda+120, posYda+45, 830, 40);
		dataProgressBarKnockout.setMaximum(100);
		dataProgressBarKnockout.setMinimum(0);
		dataProgressBarKnockout.setValue(0);
		dataProgressBarKnockout.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder(lang.getText("HSKwin_entry029"));
	    dataProgressBarKnockout.setBorder(border);
	    result.add(dataProgressBarKnockout);

	    mainSimWindow.doNotUpdate = false;
	    return result;
	}
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	
	/**
	 * Zwraca panel informacji o danych knockout.
	 * @return JPanel - panel
	 */
	public JPanel getSimDataDetailsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKwin_entry030"))); //Knockout data details panel
		result.setPreferredSize(new Dimension(670, 110));
		mainSimWindow.doNotUpdate = true;
		int posXda = 10;
		int posYda = 20;
		
		JLabel label1 = new JLabel(lang.getText("HSKwin_entry031")); //Data sets:
		label1.setBounds(posXda, posYda, 70, 20);
		result.add(label1);
		
		String[] data = { " ----- " };
		dataCombo = new JComboBox<String>(data); //final, aby listener przycisku odczytał wartość
		dataCombo.setBounds(posXda+80, posYda, 600, 20);
		dataCombo.setMaximumRowCount(12);
		dataCombo.addActionListener(actionEvent -> {
			if(mainSimWindow.doNotUpdate)
				return;

			int selected = dataCombo.getSelectedIndex();
			if(selected > 0) {
				updateDataDetails(selected-1);
			}
		});
		result.add(dataCombo);
		
		JButton removeDataButton = new JButton(lang.getText("HSKwin_entry032")); //Remove
		removeDataButton.setBounds(posXda+685, posYda, 85, 20);
		removeDataButton.setMargin(new Insets(0, 0, 0, 0));
		removeDataButton.setFocusPainted(false);
		removeDataButton.setIcon(Tools.getResIcon16("/icons/simulationKnockout/removeIcon.png"));
		removeDataButton.setToolTipText(lang.getText("HSKwin_entry032t"));
		removeDataButton.addActionListener(actionEvent -> {
			int selected = dataCombo.getSelectedIndex();
			if(selected > 0) {
				if(action.removeKnockDataSet(selected-1))
					updateFreshKnockoutTab();
			}
		});
		result.add(removeDataButton);

		JLabel dateTxtLabel = new JLabel(lang.getText("HSKwin_entry033")); //Date:
		dateTxtLabel.setBounds(posXda, posYda+20, 90, 20);
		result.add(dateTxtLabel);
		dataLabelDate = new JLabel("---");
		dataLabelDate.setBounds(posXda+100, posYda+20, 120, 20);
		result.add(dataLabelDate);
		
		JLabel modeTxtLabel = new JLabel(lang.getText("HSKwin_entry034")); //Net mode:
		modeTxtLabel.setBounds(posXda+250, posYda+20, 110, 20);
		result.add(modeTxtLabel);
		dataLabelSimNetMode = new JLabel("---");
		dataLabelSimNetMode.setBounds(posXda+370, posYda+20, 70, 20);
		result.add(dataLabelSimNetMode);
		
		JLabel maxModeTxtLabel = new JLabel(lang.getText("HSKwin_entry035")); //Max mode:
		maxModeTxtLabel.setBounds(posXda+450, posYda+20, 90, 20);
		result.add(maxModeTxtLabel);
		dataLabelMaxMode = new JLabel("---");
		dataLabelMaxMode.setBounds(posXda+550, posYda+20, 90, 20);
		result.add(dataLabelMaxMode);
		
		JLabel stepsTxtLabel = new JLabel(lang.getText("HSKwin_entry036")); //Sim. steps:
		stepsTxtLabel.setBounds(posXda, posYda+40, 90, 20);
		result.add(stepsTxtLabel);
		dataLabelSteps = new JLabel("---");
		dataLabelSteps.setBounds(posXda+100, posYda+40, 80, 20);
		result.add(dataLabelSteps);
		
		JLabel repsTxtLabel = new JLabel(lang.getText("HSKwin_entry037")); //Repetitions:
		repsTxtLabel.setBounds(posXda+250, posYda+40, 110, 20);
		result.add(repsTxtLabel);
		dataLabelReps = new JLabel("---");
		dataLabelReps.setBounds(posXda+370, posYda+40, 90, 20);
		result.add(dataLabelReps);
		
		JLabel disabledTxtLabel = new JLabel(lang.getText("HSKwin_entry038")); //Disabled:
		disabledTxtLabel.setBounds(posXda, posYda+60, 90, 20);
		result.add(disabledTxtLabel);
		dataLabelDisabled = new JLabel("----- ----- -----");
		dataLabelDisabled.setBounds(posXda+100, posYda+60, 350, 20);
		result.add(dataLabelDisabled);
		
		mainSimWindow.doNotUpdate = false;
	    return result;
	}
	
	/**
	 * Zwraca panel górnych przecisków symulatora.
	 * @return JPanel - panel
	 */
	private JPanel getMainOptionsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSKwin_entry039"))); //General options panel
		result.setPreferredSize(new Dimension(670, 120));
		mainSimWindow.doNotUpdate = true;
		int posXda = 10;
		int posYda = 20;
		
		loadAllButton = new JButton(lang.getText("HSKwin_entry040")); //Load all
		loadAllButton.setBounds(posXda, posYda, 130, 40);
		loadAllButton.setMargin(new Insets(0, 0, 0, 0));
		loadAllButton.setFocusPainted(false);
		loadAllButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/loadIcon.png"));
		loadAllButton.setToolTipText(lang.getText("HSKwin_entry040t"));
		loadAllButton.addActionListener(actionEvent -> action.loadDataSets());
		result.add(loadAllButton);
		
		saveAllButton = new JButton(lang.getText("HSKwin_entry041")); //Save all
		saveAllButton.setBounds(posXda+140, posYda, 130, 40);
		saveAllButton.setMargin(new Insets(0, 0, 0, 0));
		saveAllButton.setFocusPainted(false);
		saveAllButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/saveIcon.png"));
		saveAllButton.setToolTipText(lang.getText("HSKwin_entry041t"));
		saveAllButton.addActionListener(actionEvent -> action.saveDataSets());
		result.add(saveAllButton);

		JButton showVisualsButton = new JButton(lang.getText("HSKwin_entry042")); //Analyse
		showVisualsButton.setBounds(posXda+280, posYda, 130, 40);
		showVisualsButton.setMargin(new Insets(0, 0, 0, 0));
		showVisualsButton.setFocusPainted(false);
		showVisualsButton.setToolTipText(lang.getText("HSKwin_entry042t"));
		showVisualsButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/showIcon.png"));
		showVisualsButton.addActionListener(actionEvent -> new HolmesSimKnockVis(mainSimWindow.returnFrame()));
		result.add(showVisualsButton);
		
		simSettingsButton = new JButton(lang.getText("HSKwin_entry043")); //SimSettings
		simSettingsButton.setBounds(posXda, posYda+45, 130, 40);
		simSettingsButton.setMargin(new Insets(0, 0, 0, 0));
		simSettingsButton.setFocusPainted(false);
		simSettingsButton.setIcon(Tools.getResIcon32("/icons/simSettings/setupIcon.png"));
		simSettingsButton.setToolTipText(lang.getText("HSKwin_entry043t"));
		simSettingsButton.addActionListener(actionEvent -> new HolmesSimSetup(mainSimWindow.getFrame()));
		result.add(simSettingsButton);
		
		stateManagerButton = new JButton();
	    stateManagerButton.setText(lang.getText("HSKwin_entry044")); //States Manager
	    stateManagerButton.setIcon(Tools.getResIcon32("/icons/stateManager/stManIcon.png"));
	    stateManagerButton.setBounds(posXda+140, posYda+45, 130, 40);
	    stateManagerButton.setMargin(new Insets(0, 0, 0, 0));
	    stateManagerButton.setToolTipText(lang.getText("HSKwin_entry044t"));
	    stateManagerButton.setFocusPainted(false);
	    stateManagerButton.addActionListener(actionEvent -> new HolmesStatesManager());
	    stateManagerButton.setFocusPainted(false);
	    result.add(stateManagerButton);
		
		JLabel stateLabel0 = new JLabel(lang.getText("HSKwin_entry045")); //Selected m0 state ID:
		stateLabel0.setBounds(posXda+280, posYda+45, 220, 20);
		result.add(stateLabel0);
		    
		int selState = overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN;
		selStateLabel = new JLabel(""+selState);
	    selStateLabel.setBounds(posXda+510, posYda+45, 60, 20);
	    result.add(selStateLabel);

		//TODO: XTPN
	    selStateDescrLabel = new JLabel(overlord.getWorkspace().getProject().accessStatesManager().getStateDescriptionPN(selState));
	    selStateDescrLabel.setBounds(posXda+280, posYda+65, 650, 20);
	    result.add(selStateDescrLabel);
		
		return result;
	}
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	
	/**
	 * Ustawia status komponentów na czas symulacji.
	 * @param state boolean - false, jeśli mają być zablokowane, true jeśli mają być odblokowane
	 */
	//TODO: dodawać kolejne tutaj:
	public void setSimWindowComponentsStatus(boolean state) {
		acqRefDataButton.setEnabled(state);
		acqDataSimButton.setEnabled(state);
		simSettingsButton.setEnabled(state);
		stateManagerButton.setEnabled(state);
		loadAllButton.setEnabled(state);
		saveAllButton.setEnabled(state);
		mainSimWindow.mainTabPanel.setEnabledAt(0, state);
		overlord.getFrame().setEnabled(state);
	}
	
	/**
	 * Resetuje ustawienia do domyślnych.
	 */
	public void resetWindow() {
		mainSimWindow.doNotUpdate = true;
		refProgressBarKnockout.setValue(0);
		mainSimWindow.doNotUpdate = false;
	}
	
	/**
	 * Aktualizuje komponenty panelu symulacji knockout - combo box zbioru referencyjnego, combobox tranzycji i mct.
	 */
	public void updateFreshKnockoutTab() {
		PetriNet pn = overlord.getWorkspace().getProject();
		
		int sel = pn.accessStatesManager().selectedStatePN;
		selStateLabel.setText(""+sel);
		selStateDescrLabel.setText(pn.accessStatesManager().getStateDescriptionPN(sel));
		//TODO: XTPN

		//reference data:
		ArrayList<NetSimulationData> references = pn.accessSimKnockoutData().accessReferenceSets();
		int refSize = references.size();
		mainSimWindow.doNotUpdate = true;
		int oldSelected = referencesCombo.getSelectedIndex();
		referencesCombo.removeAllItems();
		referencesCombo.addItem(" ----- ");
		if(refSize > 0) {
			for(int r=0; r<refSize; r++) {
				String strB = "err.";
				try {
                    strB = String.format(lang.getText("HSKwin_entry046"), r, references.get(r).date, references.get(r).netSimType, references.get(r).maxMode);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKwin_entry046", "error", true);
				}
				referencesCombo.addItem(strB);
			}
			
			refLabelDate.setText("---");
			refLabelSimNetMode.setText("---");
			refLabelMaxMode.setText("---");
			refLabelSteps.setText("---");
			refLabelReps.setText("---");
			
			if(oldSelected < referencesCombo.getItemCount())
				referencesCombo.setSelectedIndex(oldSelected);
			else
				referencesCombo.setSelectedIndex(0);
		}
		
		
		//knockout data:
		ArrayList<NetSimulationData> knockout = pn.accessSimKnockoutData().accessKnockoutDataSets();
		int knockSize = knockout.size();
		mainSimWindow.doNotUpdate = true;
		int oldKnockSelected;
		oldKnockSelected = dataCombo.getSelectedIndex();
		dataCombo.removeAllItems();
		dataCombo.addItem(" ----- ");
		if(knockSize > 0) {
			for(int r=0; r<knockSize; r++) {
				
				StringBuilder disTxt = new StringBuilder(lang.getText("HSKwin_entry047"));
				for(int t : knockout.get(r).disabledTransitionsIDs) {
					disTxt.append("t").append(t).append(", ");
				}
				for(int t : knockout.get(r).disabledMCTids) {
					disTxt.append("MCT").append(t + 1).append(", ");
				}
				disTxt = new StringBuilder(disTxt.toString().replace(", ", " "));

				String strB = "err.";
				try {
					strB = String.format(lang.getText("HSKwin_entry048"), r, disTxt, knockout.get(r).netSimType, knockout.get(r).maxMode);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKwin_entry048", "error", true);
				}
				dataCombo.addItem(strB);
			}
			
			dataLabelDate.setText("---");
			dataLabelSimNetMode.setText("---");
			dataLabelMaxMode.setText("---");
			dataLabelSteps.setText("---");
			dataLabelReps.setText("---");
			
			if(oldKnockSelected < dataCombo.getItemCount())
				dataCombo.setSelectedIndex(oldKnockSelected);
			else
				dataCombo.setSelectedIndex(0);
		}
		
		//inne:
		ArrayList<Transition> transitions = pn.getTransitions();
		int oldTsel = dataTransitionsCombo.getSelectedIndex();
		int oldTsize = dataTransitionsCombo.getItemCount() - 1;
		dataTransitionsCombo.removeAllItems();
		dataTransitionsCombo.addItem("---");
		
		if(!transitions.isEmpty()) {
			for(int t=0; t < transitions.size(); t++) {
				dataTransitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
			}
			if(dataTransitionsCombo.getItemCount() > oldTsel && oldTsize == transitions.size())
				dataTransitionsCombo.setSelectedIndex(oldTsel);
			
			//MCT:
			int oldMsel = dataMctCombo.getSelectedIndex();
			dataMctCombo.removeAllItems();
			dataMctCombo.addItem("---");
			ArrayList<ArrayList<Transition>> mcts = pn.getMCTMatrix();
			ArrayList<String> mctNames = pn.accessMCTnames();
			if(mcts != null && !mcts.isEmpty()) {
				for(int m=0; m < mcts.size(); m++) {
					dataMctCombo.addItem("MCT"+(m+1)+": "+mctNames.get(m));
				}
			}

			if(dataMctCombo.getItemCount() > oldMsel)
				dataMctCombo.setSelectedIndex(oldMsel);
		}
		mainSimWindow.doNotUpdate = false;
	}

	/**
	 * Update danych o wybranym właśnie zbiorze referencyjnym.
	 * @param selected int - index zbioru ref w combobox (który wywołuję tę metodę)
	 */
	public void updateRefDetails(int selected) {
		PetriNet pn = overlord.getWorkspace().getProject();
		ArrayList<NetSimulationData> references = pn.accessSimKnockoutData().accessReferenceSets();
		NetSimulationData selectedRef = references.get(selected);
		
		refLabelDate.setText(selectedRef.date);
		refLabelSimNetMode.setText(selectedRef.netSimType.toString());
		if(selectedRef.maxMode)
			refLabelMaxMode.setText(lang.getText("HSKwin_entry049")); //TRUE
		else
			refLabelMaxMode.setText(lang.getText("HSKwin_entry050")); //FALSE
		refLabelSteps.setText(""+selectedRef.steps);
		refLabelReps.setText(""+selectedRef.reps);
	}
	
	/**
	 * Update danych o wybranym właśnie zbiorze danych knockout.
	 * @param selected int - index zbioru danych w combobox (który wywołuję tę metodę)
	 */
	public void updateDataDetails(int selected) {
		PetriNet pn = overlord.getWorkspace().getProject();
		ArrayList<NetSimulationData> data = pn.accessSimKnockoutData().accessKnockoutDataSets();
		NetSimulationData selectedData = data.get(selected);
		
		dataLabelDate.setText(selectedData.date);
		dataLabelSimNetMode.setText(selectedData.netSimType.toString());
		if(selectedData.maxMode)
			dataLabelMaxMode.setText(lang.getText("HSKwin_entry049")); //TRUE
		else
			dataLabelMaxMode.setText(lang.getText("HSKwin_entry050")); //FALSE
		dataLabelSteps.setText(""+selectedData.steps);
		dataLabelReps.setText(""+selectedData.reps);
		
		StringBuilder disTxt = new StringBuilder();
		for(int t : selectedData.disabledTransitionsIDs) {
			disTxt.append("t").append(t).append(", ");
		}
		for(int t : selectedData.disabledMCTids) {
			disTxt.append("MCT").append(t + 1).append(", ");
		}
		disTxt = new StringBuilder(disTxt.toString().replace(", ", " "));
		dataLabelDisabled.setText(disTxt.toString());
	}
}
