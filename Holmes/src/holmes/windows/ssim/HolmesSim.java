package holmes.windows.ssim;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.StateSimulator;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;
import holmes.windows.managers.HolmesStatesManager;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa okna modułu symulatora stanów sieci.
 * 
 * @author MR
 */
public class HolmesSim extends JFrame {
	private static final long serialVersionUID = 5287992734385359453L;
	private JFrame ego;
	private GUIManager overlord;
	private HolmesSimActions action = new HolmesSimActions();
	private StateSimulator ssim;
	public boolean doNotUpdate = false;
	
	private JProgressBar progressBar;
	private int transInterval = 100;
	private int placesInterval = 100;
	private boolean sortedP = false;
	private boolean sortedT = false;
	
	private ArrayList<ArrayList<Integer>> placesRawData; //dane o historii miejsc z symulatora
	private ArrayList<ArrayList<Integer>> transitionsRawData; //j.w. : dla tranzycji
	private ArrayList<Integer> transitionsCompactData; // suma odpaleń tranzycji
	private ArrayList<Double> transAvgData;
	private ArrayList<Double> placesAvgData;
	private ArrayList<Long> placesTotalData;
	private ArrayList<Integer> placesInChart;
	private ArrayList<String> placesInChartStr;
	
	private XYSeriesCollection placesSeriesDataSet = null;
	private XYSeriesCollection transitionsSeriesDataSet = null;
	private JFreeChart placesChart;
	private JFreeChart transitionsChart;
	private int transChartType = 0; //suma odpaleń, 1=konkretne tranzycje
	private int placesChartType = 0; //j.w. dla miejsc
	
	private JPanel placesJPanel;
	private JPanel transitionsJPanel;
	private JSpinner transIntervalSpinner;
	private JSpinner placesIntervalSpinner;
	private JComboBox<String> placesCombo = null;
	private JComboBox<String> transitionsCombo = null;
	private ChartProperties chartDetails;
	
	private JLabel selStateLabel;
	private JButton stateManagerButton;
	private JButton acqDataButton;
	//reset:
	private JPanel knockoutTab;		//ZAKLADKA KNOCKOUT SIM //TODO
	private JButton cancelButton;
	private JButton simSettingsButton;
	private boolean workInProgress;
	
	public JTabbedPane mainTabPanel;
	
	/**
	 * Konstruktor domyślny obiektu klasy StateSimulator (podokna Holmes)
	 */
	public HolmesSim(GUIManager overlord) {
		this.overlord = overlord;
		ego = this;
		ssim = new StateSimulator();
		chartDetails = new ChartProperties();
		placesRawData = new ArrayList<ArrayList<Integer>>();
		transitionsRawData = new ArrayList<ArrayList<Integer>>();
		transitionsCompactData = new ArrayList<Integer>();
		placesInChart = new ArrayList<Integer>();
		placesInChartStr = new ArrayList<String>();
		transAvgData = new ArrayList<Double>();
		placesAvgData = new ArrayList<Double>();
		placesTotalData = new ArrayList<Long>();
		
		initializeComponents();
		initiateListeners();
	}
	
	public HolmesSim returnFrame() {
		return this;
	}

	/**
	 * Metoda pomocnica konstuktora, odpowiada za utworzenie elementów graficznych okna.
	 */
	private void initializeComponents() {
		setVisible(false);
		setTitle("State Simulator");
		setLocation(30,30);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {}
		setSize(new Dimension(1000, 750));
		
		JPanel main = new JPanel(new BorderLayout()); //główny panel okna
		add(main);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Places dynamics", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), createPlacesTabPanel(), "Places dynamics");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		tabbedPane.addTab("Transitions dynamics", Tools.getResIcon16("/icons/stateSim/transDyn.png"), createTransitionsTabPanel(), "Transistions dynamics");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

		mainTabPanel = new JTabbedPane();
		JPanel firstTab = new JPanel(new BorderLayout());
		firstTab.add(craeteDataAcquisitionPanel(), BorderLayout.NORTH);
		firstTab.add(tabbedPane, BorderLayout.CENTER);

		mainTabPanel.addTab("Simple mode", Tools.getResIcon16("/icons/stateSim/simpleSimTab.png"), firstTab, "Simple mode");
		
		knockoutTab = new HolmesSimKnock(this);
		mainTabPanel.addTab("KnockoutSim", Tools.getResIcon16("/icons/stateSim/knockSimTab.png"), knockoutTab, "KnockoutSim");
		
		main.add(mainTabPanel, BorderLayout.CENTER);
		
		repaint();
	}
	
	/**
	 * Metoda tworzy panel opcji okna symulatora stanów sieci.
	 * @return JPanel - panel opcji pobrania danych
	 */
	private JPanel craeteDataAcquisitionPanel() {
		JPanel dataAcquisitionPanel = new JPanel(null);
		dataAcquisitionPanel.setBorder(BorderFactory.createTitledBorder("Data acquisition"));
		dataAcquisitionPanel.setPreferredSize(new Dimension(670, 110));

		int posXda = 10;
		int posYda = 20;
		
		acqDataButton = new JButton("SimStart");
		acqDataButton.setBounds(posXda, posYda, 110, 40);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setFocusPainted(false);
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
		acqDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				acquireDataFromSimulation();
			}
		});
		dataAcquisitionPanel.add(acqDataButton);
		
		cancelButton = new JButton();
		cancelButton.setText("<html>&nbsp;&nbsp;&nbsp;STOP&nbsp;&nbsp;&nbsp;</html>");
		cancelButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/stopIcon.png"));
		cancelButton.setBounds(posXda, posYda+45, 110, 30);
		cancelButton.setMargin(new Insets(0, 0, 0, 0));
		cancelButton.setFocusPainted(false);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ssim.setCancelStatus(true);
			}
		});

		dataAcquisitionPanel.add(cancelButton);
		
		simSettingsButton = new JButton("SimSettings");
		simSettingsButton.setBounds(posXda+120, posYda, 130, 40);
		simSettingsButton.setMargin(new Insets(0, 0, 0, 0));
		simSettingsButton.setFocusPainted(false);
		simSettingsButton.setIcon(Tools.getResIcon32("/icons/simSettings/setupIcon.png"));
		simSettingsButton.setToolTipText("Set simulator options.");
		simSettingsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				new HolmesSimSetup(ego);
			}
		});
		dataAcquisitionPanel.add(simSettingsButton);
		
		stateManagerButton = new JButton();
	    stateManagerButton.setText("<html>States<br>Manager</html>");
	    stateManagerButton.setIcon(Tools.getResIcon32("/icons/stateManager/stManIcon.png"));
	    stateManagerButton.setBounds(posXda+260, posYda, 130, 40);
	    stateManagerButton.setMargin(new Insets(0, 0, 0, 0));
	    stateManagerButton.setFocusPainted(false);
	    stateManagerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				new HolmesStatesManager();
			}
		});
		dataAcquisitionPanel.add(stateManagerButton);
		
		JLabel stateLabel0 = new JLabel("Selected m0 state ID: ");
	    stateLabel0.setBounds(posXda+400, posYda, 130, 20);
	    dataAcquisitionPanel.add(stateLabel0);
	    
	    selStateLabel = new JLabel(""+overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN);
	    selStateLabel.setBounds(posXda+400, posYda+20, 60, 20);
	    dataAcquisitionPanel.add(selStateLabel);
		
		JButton clearDataButton = new JButton("Clear");
		clearDataButton.setBounds(posXda+840, posYda, 110, 40);
		clearDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/clearData.png"));
		clearDataButton.setToolTipText("Clear all charts and data vectors. Reset simulator.");
		clearDataButton.setFocusPainted(false);
		clearDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearPlacesChart();
				clearTransitionsChart();
				clearAllData();
			}
		});
		dataAcquisitionPanel.add(clearDataButton);
		

		progressBar = new JProgressBar();
		progressBar.setBounds(posXda+120, posYda+40, 840, 40);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBar.setBorder(border);
	    dataAcquisitionPanel.add(progressBar);

		return dataAcquisitionPanel;
	}

	/**
	 * Metoda ta tworzy panel dla zakładki miejsc.
	 * @return JPanel - panel
	 */
	private JPanel createPlacesTabPanel() {
		JPanel result = new JPanel(new BorderLayout());

		JPanel topPanel = new JPanel(new BorderLayout());
		result.add(topPanel, BorderLayout.PAGE_START);
		
		JPanel placesChartOptionsPanel = new JPanel(null);
		placesChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Places chart options"));
		placesChartOptionsPanel.setPreferredSize(new Dimension(500, 120));
		
		int posXchart = 10;
		int posYchart = 20;
		
		JButton showAllButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Show all&nbsp;&nbsp;&nbsp;&nbsp;</htm>");
		showAllButton.setBounds(posXchart, posYchart, 120, 24);
		showAllButton.setMargin(new Insets(0, 0, 0, 0));
		showAllButton.setFocusPainted(false);
		showAllButton.setIcon(Tools.getResIcon16("/icons/stateSim/showAll.png"));
		showAllButton.setToolTipText("Show average numbers of token in places through simulation steps.");
		showAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showAllPlacesData();
			}
		});
		placesChartOptionsPanel.add(showAllButton);
		
		JButton showNotepadButton = new JButton("Show notepad");
		showNotepadButton.setBounds(posXchart+130, posYchart, 120, 24);
		showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
		showNotepadButton.setFocusPainted(false);
		showNotepadButton.setIcon(Tools.getResIcon16("/icons/stateSim/showNotepad.png"));
		showNotepadButton.setToolTipText("Show average numbers of token in places through simulation steps.");
		showNotepadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showPlacesAllInNotepad();
			}
		});
		placesChartOptionsPanel.add(showNotepadButton);
		
		JLabel labelInt = new JLabel("Interval:");
		labelInt.setBounds(posXchart+280, posYchart+2, 70, 20);
		placesChartOptionsPanel.add(labelInt);
		
		int mValue = overlord.simSettings.getSimSteps()/10;
		SpinnerModel intervSpinnerModel = new SpinnerNumberModel(placesInterval, 0, mValue, 10);
		placesIntervalSpinner = new JSpinner(intervSpinnerModel);
		placesIntervalSpinner.setBounds(posXchart+330, posYchart+3, 60, 20);
		placesIntervalSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				placesInterval = (int) spinner.getValue();
				clearPlacesChart();
			}
		});
		placesChartOptionsPanel.add(placesIntervalSpinner);
		
		JCheckBox sortedCheckBox = new JCheckBox("Sorted by tokens");
		sortedCheckBox.setBounds(posXchart+460, posYchart+10, 130, 20);
		sortedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					sortedP = true;
				} else {
					sortedP = false;
				}
				fillPlacesAndTransitionsData();
			}
		});
		placesChartOptionsPanel.add(sortedCheckBox);
		posYchart += 30;
		
		JLabel label1 = new JLabel("Places:");
		label1.setBounds(posXchart, posYchart, 70, 20);
		placesChartOptionsPanel.add(label1);
		
		String[] dataP = { "---" };
		placesCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
		placesCombo.setLocation(posXchart + 75, posYchart+2);
		placesCombo.setSize(500, 20);
		placesCombo.setSelectedIndex(0);
		placesCombo.setMaximumRowCount(12);
		placesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
			}
		});
		placesChartOptionsPanel.add(placesCombo);
		
		posYchart += 30;
		
		JButton addPlaceButton = new JButton("Add to chart");
		addPlaceButton.setBounds(posXchart, posYchart+2, 110, 24);
		addPlaceButton.setMargin(new Insets(0, 0, 0, 0));
		addPlaceButton.setFocusPainted(false);
		addPlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/addChart.png"));
		addPlaceButton.setToolTipText("Add data about place tokens to the chart.");
		addPlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(placesRawData.size() == 0)
					return;
				
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					String name = placesCombo.getSelectedItem().toString();
					int sel = action.getRealNodeID(name);
					if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID
					
					name = trimNodeName(name);
					placesInChart.set(sel, 1);
					placesInChartStr.set(sel, name);
					
					addNewPlaceSeries(sel, name);
					updatePlacesGraphicChart("places");
				}
			}
		});
		placesChartOptionsPanel.add(addPlaceButton);
		
		JButton removePlaceButton = new JButton("Remove");
		removePlaceButton.setBounds(posXchart+120, posYchart+2, 110, 24);
		removePlaceButton.setMargin(new Insets(0, 0, 0, 0));
		removePlaceButton.setFocusPainted(false);
		removePlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/removeChart.png"));
		removePlaceButton.setToolTipText("Remove data about place tokens from the chart.");
		removePlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(placesRawData.size() == 0)
					return;
				
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					String name = placesCombo.getSelectedItem().toString();
					int sel = action.getRealNodeID(name);
					if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID
					
					name = trimNodeName(name);
					placesInChart.set(sel, -1);
					placesInChartStr.set(sel, "");
					removePlaceSeries(name);
				}
			}
		});
		placesChartOptionsPanel.add(removePlaceButton);
		
		JButton clearPlacesChartButton = new JButton("Clear chart");
		clearPlacesChartButton.setBounds(posXchart+240, posYchart+2, 110, 24);
		clearPlacesChartButton.setMargin(new Insets(0, 0, 0, 0));
		clearPlacesChartButton.setFocusPainted(false);
		clearPlacesChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/clearChart.png"));
		clearPlacesChartButton.setToolTipText("Clears the chart.");
		clearPlacesChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearPlacesChart();
			}
		});
		placesChartOptionsPanel.add(clearPlacesChartButton);
		
		JButton savePlacesChartButton = new JButton("Save Image");
		savePlacesChartButton.setBounds(posXchart+360, posYchart+2, 110, 24);
		savePlacesChartButton.setMargin(new Insets(0, 0, 0, 0));
		savePlacesChartButton.setFocusPainted(false);
		savePlacesChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/saveImage.png"));
		savePlacesChartButton.setToolTipText("Saves the chart as image file.");
		savePlacesChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveChartImage("places", 1200, 1024);
			}
		});
		placesChartOptionsPanel.add(savePlacesChartButton);
		
		JButton showPlaceButton = new JButton("Find place");
		showPlaceButton.setBounds(posXchart+480, posYchart+2, 110, 24);
		showPlaceButton.setMargin(new Insets(0, 0, 0, 0));
		showPlaceButton.setFocusPainted(false);
		showPlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/findNode.png"));
		showPlaceButton.setToolTipText("Find selected place within the net.");
		showPlaceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = placesCombo.getSelectedIndex();
				if(selected>0) {
					String name = placesCombo.getSelectedItem().toString();
					int sel = action.getRealNodeID(name);
					if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID
					
					GUIManager.getDefaultGUIManager().getSearchWindow().fillComboBoxesData();
					GUIManager.getDefaultGUIManager().getSearchWindow().selectedManually(true, sel);
				}
			}
		});
		placesChartOptionsPanel.add(showPlaceButton);
		topPanel.add(placesChartOptionsPanel, BorderLayout.CENTER);
		
		//************************************************************************************************************
		//************************************************************************************************************
		
		JPanel placesChartGraphicPanel = new JPanel(null);
		placesChartGraphicPanel.setBorder(BorderFactory.createTitledBorder("Chart graphic"));
		placesChartGraphicPanel.setPreferredSize(new Dimension(200, 100));
		
		int posXGchart = 10;
		int posYGchart = 20;
		
		ButtonGroup groupWidth = new ButtonGroup();
		JRadioButton width1 = new JRadioButton("Thin");
		width1.setBounds(posXGchart, posYGchart, 70, 20);
		width1.setActionCommand("0");
		width1.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.p_StrokeWidth = 1.0f;
				updatePlacesGraphicChart("places");
			}
		}});
		placesChartGraphicPanel.add(width1);
		groupWidth.add(width1);
		groupWidth.setSelected(width1.getModel(), true);
		
		JRadioButton width2 = new JRadioButton("Normal");
		width2.setBounds(posXGchart, posYGchart+20, 70, 20);
		width2.setActionCommand("1");
		width2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.p_StrokeWidth = 2.0f;
				updatePlacesGraphicChart("places");
			}
		}});
		placesChartGraphicPanel.add(width2);
		groupWidth.add(width2);
		
		JRadioButton width3 = new JRadioButton("Thick");
		width3.setBounds(posXGchart, posYGchart+40, 70, 20);
		width3.setActionCommand("2");
		width3.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.p_StrokeWidth = 3.0f;
				updatePlacesGraphicChart("places");
			}
		}});
		placesChartGraphicPanel.add(width3);
		groupWidth.add(width3);
				
		topPanel.add(placesChartGraphicPanel, BorderLayout.EAST);
		//************************************************************************************************************
		//************************************************************************************************************

		placesJPanel = new JPanel(new BorderLayout());
		placesJPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
		
		//JScrollPane sP = new JScrollPane(createPlacesChartPanel(), JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		//placesJPanel.add(sP, BorderLayout.CENTER);
		placesJPanel.add(createPlacesChartPanel(), BorderLayout.CENTER);
		result.add(placesJPanel, BorderLayout.CENTER);

		return result;
	}
	
	/**
	 * Metoda ta tworzy panel dla zakładki tranzycji.
	 * @return JPanel - panel
	 */
	private JPanel createTransitionsTabPanel() {
		JPanel result = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		result.add(topPanel, BorderLayout.PAGE_START);
		
		JPanel transChartOptionsPanel = new JPanel(null);
		transChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart options"));
		transChartOptionsPanel.setPreferredSize(new Dimension(500, 120));
		
		int posXchart = 10;
		int posYchart = 20;
		
		JButton showAllButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Show all&nbsp;&nbsp;&nbsp;&nbsp;</html>");
		showAllButton.setBounds(posXchart, posYchart, 120, 24);
		showAllButton.setMargin(new Insets(0, 0, 0, 0));
		showAllButton.setFocusPainted(false);
		showAllButton.setIcon(Tools.getResIcon16("/icons/stateSim/showAll.png"));
		showAllButton.setToolTipText("Show average numbers of firings of transitions through simulation steps.");
		showAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showAllTransData();
			}
		});
		transChartOptionsPanel.add(showAllButton);
		
		JButton showNotepadButton = new JButton("Show notepad");
		showNotepadButton.setBounds(posXchart+130, posYchart, 120, 24);
		showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
		showNotepadButton.setFocusPainted(false);
		showNotepadButton.setIcon(Tools.getResIcon16("/icons/stateSim/showNotepad.png"));
		showNotepadButton.setToolTipText("Show average numbers of firings of transitions through simulation steps.");
		showNotepadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showTransAllInNotepad();
			}
		});
		transChartOptionsPanel.add(showNotepadButton);
		
		JLabel label1 = new JLabel("Interval:");
		label1.setBounds(posXchart+280, posYchart+2, 70, 20);
		transChartOptionsPanel.add(label1);
		
		int mValue = overlord.simSettings.getSimSteps()/10;
		SpinnerModel intervSpinnerModel = new SpinnerNumberModel(transInterval, 0, mValue, 10);
		transIntervalSpinner = new JSpinner(intervSpinnerModel);
		transIntervalSpinner.setBounds(posXchart+330, posYchart+3, 60, 20);
		transIntervalSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				transInterval = (int) spinner.getValue();
				clearTransitionsChart();
			}
		});
		transChartOptionsPanel.add(transIntervalSpinner);

		JCheckBox sortedCheckBox = new JCheckBox("Sorted by firing");
		sortedCheckBox.setBounds(posXchart+460, posYchart+10, 130, 20);
		sortedCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					sortedT = true;
				} else {
					sortedT = false;
				}
				fillPlacesAndTransitionsData();
			}
		});
		transChartOptionsPanel.add(sortedCheckBox);
		posYchart += 30;
		
		JLabel label2 = new JLabel("Transition:");
		label2.setBounds(posXchart, posYchart, 70, 20);
		transChartOptionsPanel.add(label2);
		
		String[] dataP = { "---" };
		transitionsCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
		transitionsCombo.setLocation(posXchart + 75, posYchart+2);
		transitionsCombo.setSize(500, 20);
		transitionsCombo.setSelectedIndex(0);
		transitionsCombo.setMaximumRowCount(12);
		transChartOptionsPanel.add(transitionsCombo);
		
		posYchart += 30;
		
		JButton addTransitionButton = new JButton("Add to chart");
		addTransitionButton.setBounds(posXchart, posYchart+2, 110, 24);
		addTransitionButton.setMargin(new Insets(0, 0, 0, 0));
		addTransitionButton.setFocusPainted(false);
		addTransitionButton.setIcon(Tools.getResIcon16("/icons/stateSim/addChart.png"));
		addTransitionButton.setToolTipText("Add data about transition firing to the chart.");
		addTransitionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitionsRawData.size() == 0)
					return;
				
				int selected = transitionsCombo.getSelectedIndex();
				if(selected>0) {
					String name = transitionsCombo.getSelectedItem().toString();
					int sel = action.getRealNodeID(name);
					if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID
					
					//selected--;
					name = trimNodeName(name);
					addNewTransitionSeries(sel, name);
					//updateTransitionsGraphicChart();
				}
			}
		});
		transChartOptionsPanel.add(addTransitionButton);
		
		JButton removeTransitionButton = new JButton("Remove");
		removeTransitionButton.setBounds(posXchart+120, posYchart+2, 110, 24);
		removeTransitionButton.setMargin(new Insets(0, 0, 0, 0));
		removeTransitionButton.setFocusPainted(false);
		removeTransitionButton.setIcon(Tools.getResIcon16("/icons/stateSim/removeChart.png"));
		removeTransitionButton.setToolTipText("Remove data about transition firing from the chart.");
		removeTransitionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitionsRawData.size() == 0)
					return;
				
				int selected = transitionsCombo.getSelectedIndex();
				if(selected>0) {
					String name = transitionsCombo.getSelectedItem().toString();
					name = trimNodeName(name);
					removeTransitionSeries(name);
				}
			}
		});
		transChartOptionsPanel.add(removeTransitionButton);
		
		JButton clearTransChartButton = new JButton("Clear chart");
		clearTransChartButton.setBounds(posXchart+240, posYchart+2, 110, 24);
		clearTransChartButton.setMargin(new Insets(0, 0, 0, 0));
		clearTransChartButton.setFocusPainted(false);
		clearTransChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/clearChart.png"));
		clearTransChartButton.setToolTipText("Clears the transitions chart.");
		clearTransChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				clearTransitionsChart();
			}
		});
		transChartOptionsPanel.add(clearTransChartButton);
		
		JButton saveTransitionsChartButton = new JButton("Save Image");
		saveTransitionsChartButton.setBounds(posXchart+360, posYchart+2, 110, 24);
		saveTransitionsChartButton.setMargin(new Insets(0, 0, 0, 0));
		saveTransitionsChartButton.setFocusPainted(false);
		saveTransitionsChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/saveImage.png"));
		saveTransitionsChartButton.setToolTipText("Saves the chart as image file.");
		saveTransitionsChartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveChartImage("transitions", 1200, 1024);
			}
		});
		transChartOptionsPanel.add(saveTransitionsChartButton);
		
		JButton showTransButton = new JButton("Find trans.");
		showTransButton.setBounds(posXchart+480, posYchart+2, 110, 24);
		showTransButton.setMargin(new Insets(0, 0, 0, 0));
		showTransButton.setFocusPainted(false);
		showTransButton.setIcon(Tools.getResIcon16("/icons/stateSim/findNode.png"));
		showTransButton.setToolTipText("Find selected transition within the net.");
		showTransButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = transitionsCombo.getSelectedIndex();
				if(selected>0) {
					//ustalanie prawdziwego ID:
					int sel = action.getRealNodeID(transitionsCombo.getSelectedItem().toString());
					if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID
					
					GUIManager.getDefaultGUIManager().getSearchWindow().fillComboBoxesData();
					GUIManager.getDefaultGUIManager().getSearchWindow().selectedManually(false, sel);
				}
			}
		});
		transChartOptionsPanel.add(showTransButton);
		topPanel.add(transChartOptionsPanel, BorderLayout.CENTER);
		
		//************************************************************************************************************
		//************************************************************************************************************
				
		JPanel transChartGraphicPanel = new JPanel(null);
		transChartGraphicPanel.setBorder(BorderFactory.createTitledBorder("Chart graphic"));
		transChartGraphicPanel.setPreferredSize(new Dimension(200, 100));
		
		int posXGchart = 10;
		int posYGchart = 20;
		
		ButtonGroup groupWidth = new ButtonGroup();
		JRadioButton width1 = new JRadioButton("Thin");
		width1.setBounds(posXGchart, posYGchart, 70, 20);
		width1.setActionCommand("0");
		width1.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.t_StrokeWidth = 1.0f;
				updatePlacesGraphicChart("transitions");
			}
		}});
		transChartGraphicPanel.add(width1);
		groupWidth.add(width1);
		groupWidth.setSelected(width1.getModel(), true);
		
		JRadioButton width2 = new JRadioButton("Normal");
		width2.setBounds(posXGchart, posYGchart+20, 70, 20);
		width2.setActionCommand("1");
		width2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.t_StrokeWidth = 2.0f;
				updatePlacesGraphicChart("transitions");
			}
		}});
		transChartGraphicPanel.add(width2);
		groupWidth.add(width2);
		
		JRadioButton width3 = new JRadioButton("Thick");
		width3.setBounds(posXGchart, posYGchart+40, 70, 20);
		width3.setActionCommand("2");
		width3.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent actionEvent) {
			if(((AbstractButton) actionEvent.getSource()).isSelected() == true) {
				chartDetails.t_StrokeWidth = 3.0f;
				updatePlacesGraphicChart("transitions");
			}
		}});
		transChartGraphicPanel.add(width3);
		groupWidth.add(width3);
		
		topPanel.add(transChartGraphicPanel, BorderLayout.EAST);
						
		//************************************************************************************************************
		//************************************************************************************************************
		
		transitionsJPanel = new JPanel(new BorderLayout());
		transitionsJPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart"));
		transitionsJPanel.add(createTransChartPanel(), BorderLayout.CENTER);
		result.add(transitionsJPanel, BorderLayout.CENTER);

		return result;
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************     MIEJSCA      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda ta tworzy wykres liniowy dla miejsc.
	 * @return JPanel - panel z wykresem
	 */
	private JPanel createPlacesChartPanel() {
		String chartTitle = "Places dynamics";
	    String xAxisLabel = "Step";
	    String yAxisLabel = "Tokens";
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
	    
		placesSeriesDataSet = new XYSeriesCollection();
	    placesChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, placesSeriesDataSet, 
	    		PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
	
	    ChartPanel placesChartPanel = new ChartPanel(placesChart);
	    return placesChartPanel;
	}
	
	/**
	 * Metoda dodaje nową linię do wykresu.
	 * @param selPlaceID int - indeks miejsca
	 * @param name String - nazwa miejsca
	 */
	private void addNewPlaceSeries(int selPlaceID, String name) {
		if(placesChartType == 0) { //replace chart
			placesJPanel.removeAll();
			placesJPanel.add(createPlacesChartPanel(), BorderLayout.CENTER);
			placesJPanel.revalidate();
			placesJPanel.repaint();
			placesChartType = 1;
			
			placesSeriesDataSet.removeAllSeries(); // ??
		}
		
		@SuppressWarnings("unchecked")
		List<XYSeries> x = placesSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name))
				return;
		}
		
		XYSeries series = new XYSeries(name);
		//ArrayList<Integer> test = new ArrayList<Integer>();
		
		int interval = placesInterval;
		int maxStep = placesRawData.size()-interval-1;
		
		if(placesRawData.size() > 10*interval) {
			for(int step=0; step<maxStep; step+=interval) {
				
				double value = 0;
				for(int j=0; j<interval; j++) {
					value += placesRawData.get(step+j).get(selPlaceID);
				}
				value = value / interval;
				
				//int value = placesRawData.get(step).get(selPlaceID);
				series.add(step, value);
			}
		} else {
			for(int step=0; step<placesRawData.size(); step++) {
				int value = placesRawData.get(step).get(selPlaceID);
				series.add(step, value);
			}
		}
		placesSeriesDataSet.addSeries(series);
	}
	
	/**
	 * Metoda ta usuwa z wykresu dane o wskazanym miejscu.
	 * @param name String - nazwa miejsca (unikalna)
	 */
	private void removePlaceSeries(String name) {
		@SuppressWarnings("unchecked")
		List<XYSeries> x = placesSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name)) {
				placesSeriesDataSet.removeSeries(xys);
				return;
			}
		}
	}
	
	/**
	 * Metoda czyszcząca dane okna.
	 */
	private void clearPlacesChart() {
		placesSeriesDataSet.removeAllSeries();
		placesInChart = new ArrayList<Integer>();
		placesInChartStr  = new ArrayList<String>();
		for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
			placesInChart.add(-1);
			placesInChartStr.add("");
		}
	}
	
	/**
	 * Metoda ta pokazuje na wykresie średnią liczbę tokenów w miejscach
	 */
	@SuppressWarnings("deprecation")
	private void showAllPlacesData() {
		if(placesAvgData.size() == 0)
			return;

		placesChartType = 0;
		//int steps = placesRawData.size();
		double max = 0.0;
		for(int p=0; p<placesAvgData.size(); p++) {
			if(placesAvgData.get(p) > max)
				max = placesAvgData.get(p);
		}
		
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    for(int p=0; p<placesAvgData.size(); p++) {
			String tName = "p"+p;//+"_"+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().get(p).getName();
			double value = placesAvgData.get(p);
			
			//dataset.addValue(value, "Firing", tName);
			dataset.addValue(value, "Tokens", tName);
			
			if(value > 0) {
				dataset.addValue(max-value, "toMax", tName);
				dataset.addValue(0, "ZeroTokens", tName);
			} else {
				dataset.addValue(0, "toMax", tName);
				dataset.addValue(max, "ZeroTokens", tName);
			}
		}
	    
	    CategoryAxis xAxisPlaces = new CategoryAxis("Place ID");
        xAxisPlaces.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisPlaces.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisPlaces.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Average tokens");
        yAxisPlaces.setLabelFont(new Font("Helvetica", Font.BOLD, 24));
        CategoryItemRenderer rendererPlaces = new StackedBarRenderer();
        CategoryPlot plotPlaces = new CategoryPlot(dataset, xAxisPlaces, yAxisPlaces, rendererPlaces);

        //TODO
        rendererPlaces.setToolTipGenerator(new CustomToolTipPlacesGenerator( 
        		overlord.getWorkspace().getProject().getPlaces(), placesAvgData, max));
	    
	    placesChart = new JFreeChart("Places average tokens data", new Font("Helvetica", Font.BOLD, 24), plotPlaces, true);
		CategoryPlot plot = (CategoryPlot) placesChart.getPlot();

	    StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
	    renderer.setBase(1);
		//CategoryItemRenderer renderer = plot.getRenderer();
		Paint p1 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, Color.red);
        renderer.setSeriesPaint(0, p1);
        Paint p2 = new GradientPaint(0.0f, 0.0f, Color.lightGray, 0.0f, 0.0f, Color.lightGray);
        renderer.setSeriesPaint(1, p2);
        Paint p3 = new GradientPaint(0.0f, 0.0f, Color.gray, 0.0f, 0.0f, Color.gray);
        renderer.setSeriesPaint(2, p3);
        plot.setRenderer(renderer);
        
        Font font3 = new Font("Dialog", Font.PLAIN, 10); 
      	plot.getDomainAxis().setLabelFont(font3);
      	plot.getRangeAxis().setLabelFont(font3);
      	
      	LegendTitle legend = placesChart.getLegend();
      	Font labelFont = new Font("Arial", Font.BOLD, 12);
      	legend.setItemFont(labelFont);
      	
      	BarRenderer br = (BarRenderer) plot.getRenderer();
		br.setMaximumBarWidth(20);

		placesJPanel.removeAll();
		ChartPanel placesChartPanel = new ChartPanel(placesChart);
		int places = placesAvgData.size();
		
		if(placesAvgData.size() > 99) {
			placesChartPanel.setPreferredSize(new Dimension(places*50, 270)); 
			placesChartPanel.setMaximumDrawWidth(places*30);
		} else {
			placesChartPanel.setPreferredSize(new Dimension(places*40, 270)); 
			placesChartPanel.setMaximumDrawWidth(places*30);
		}
		
	    JScrollPane sPane = new JScrollPane(placesChartPanel); 
		placesJPanel.add(sPane, BorderLayout.CENTER);
		placesJPanel.revalidate();
		placesJPanel.repaint();
	}
	
	/**
	 * Pokazuje wyniki dla wszystkich miejsc w notatniku.
	 */
	private void showPlacesAllInNotepad() {
		if(placesAvgData.size() == 0)
			return;
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		ArrayList<Place> places_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Places: ", "text");
		notePad.addTextLineNL("", "text");
		for(int p=0; p<placesAvgData.size(); p++) {
			String p_name = places_tmp.get(p).getName();
			double val = placesAvgData.get(p);
			long total = placesTotalData.get(p);
			if(val > 0)
				notePad.addTextLineNL("  p_"+p+" "+p_name+" : Avg. tokens: "+val+" Total: "+total, "text");
			else
				notePad.addTextLineNL("NO_TOKENS: p_"+p+" "+p_name+" : "+val, "text");
		}
	}

	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************    TRANZYCJE     ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda ta tworzy wykres liniowy dla tranzycji.
	 * @return JPanel - panel z wykresem
	 */
	private JPanel createTransChartPanel() {
		String chartTitle = "Transitions dynamics";
	    String xAxisLabel = "Transition";
	    String yAxisLabel = "Firing";
	    boolean showLegend = true;
	    boolean createTooltip = true;
	    boolean createURL = false;
	    
		transitionsSeriesDataSet = new XYSeriesCollection();
		transitionsChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, transitionsSeriesDataSet,
			PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
	    ChartPanel res = new ChartPanel(transitionsChart);
	    return res;
	}
	
	/**
	 * Metoda dodaje nowy wykres dla tranzycji.
	 * @param selTransID int - indeks tranzycji
	 * @param name String - nazwa tranzycji
	 */
	private void addNewTransitionSeries(int selTransID, String name) {
		if(transChartType == 0) { //replace chart
			transitionsJPanel.removeAll();
			transitionsJPanel.add(createTransChartPanel(), BorderLayout.CENTER);
			transitionsJPanel.revalidate();
			transitionsJPanel.repaint();
			transChartType = 1;
			
			transitionsSeriesDataSet.removeAllSeries(); // ??
		}
		
		@SuppressWarnings("unchecked")
		List<XYSeries> x = transitionsSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name))
				return;
		}
		
		XYSeries series = new XYSeries(name);
		//ArrayList<Integer> test = new ArrayList<Integer>();
		//int counter = 0;
		for(int step=0; step<transitionsRawData.size(); step++) {
			if(transInterval > (transitionsRawData.size()/10)) {
				transInterval = transitionsRawData.size()/10;
				
			}
			int value = 0; //suma odpaleń w przedziale czasu
			for(int i=0; i<transInterval; i++) {
				try {
					value += transitionsRawData.get(step+i).get(selTransID);
				} catch (Exception e) {
					
				}
			}
			
			//test.add(value);
			series.add(step, value);
			step += transInterval;
			
			if(transInterval>1)
				step--;
			//counter++;
		}
		transitionsSeriesDataSet.addSeries(series);
	}
	
	/**
	 * Metoda ta usuwa z wykresu dane o wskazanym miejscu.
	 * @param name String - nazwa miejsca (unikalna)
	 */
	private void removeTransitionSeries(String name) {
		@SuppressWarnings("unchecked")
		List<XYSeries> x = transitionsSeriesDataSet.getSeries();
		for(XYSeries xys : x) {
			if(xys.getKey().equals(name)) {
				transitionsSeriesDataSet.removeSeries(xys);
				return;
			}
		}
	}
	
	/**
	 * Metoda czyszcząca dane okna.
	 */
	private void clearTransitionsChart() {
		transitionsSeriesDataSet.removeAllSeries();

		//if(transitionsSeriesDataSet.getSeriesCount() > 0)
		//	transitionsSeriesDataSet.removeAllSeries();	
	}
	
	/**
	 * Metoda ta pokazuje na wykresie tranzycji wszystkie odpalenia tranzycji dla obliczonej
	 * liczby kroków symulacji.
	 */
	@SuppressWarnings("deprecation")
	private void showAllTransData() {
		if(transitionsCompactData.size() == 0)
			return;

		transChartType = 0;
		
		double max = 0.0;
		for(int p=0; p<transitionsCompactData.size(); p++) {
			if(transitionsCompactData.get(p) > max)
				max = transitionsCompactData.get(p);
		}
		
	    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	    for(int t=0; t<transitionsCompactData.size(); t++) {
			String tName = "t"+t; //+"_"+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(t).getName();
			int value = transitionsCompactData.get(t);
			dataset.addValue(value, "Firing", tName);
			//dataset.addValue(max-value, "NotFiring", tName);
			
			if(value > 0) {
				dataset.addValue(max-value, "NotFiring", tName);
				dataset.addValue(0, "zero", tName);
			} else {
				dataset.addValue(0, "NotFiring", tName);
				dataset.addValue(max, "zero", tName);
			}
		}

	    CategoryAxis xAxisTrans = new CategoryAxis("Transition ID");
        xAxisTrans.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisTrans.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisTrans.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Total firing value");
        CategoryItemRenderer rendererTrans = new StackedBarRenderer();
        CategoryPlot plotTrans = new CategoryPlot(dataset, xAxisTrans, yAxisPlaces, rendererTrans);
        
        //TODO
        rendererTrans.setToolTipGenerator(new CustomToolTipTransGenerator( 
        		overlord.getWorkspace().getProject().getTransitions(), transitionsCompactData, max));
	    
	    transitionsChart = new JFreeChart("Transitions statistical data", new Font("Helvetica", Font.BOLD, 14), plotTrans, true);
	    
		//transitionsChart = ChartFactory.createStackedBarChart(chartTitle, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
		
		CategoryPlot plot = (CategoryPlot) transitionsChart.getPlot();
		CategoryItemRenderer renderer = plot.getRenderer();
		Paint p1 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, Color.red);
        renderer.setSeriesPaint(0, p1);
        Paint p2 = new GradientPaint(0.0f, 0.0f, Color.lightGray, 0.0f, 0.0f, Color.lightGray);
        renderer.setSeriesPaint(1, p2);
        Paint p3 = new GradientPaint(0.0f, 0.0f, Color.gray, 0.0f, 0.0f, Color.gray);
        renderer.setSeriesPaint(2, p3);
        plot.setRenderer(renderer);
        
        Font font3 = new Font("Dialog", Font.PLAIN, 12); 
      	plot.getDomainAxis().setLabelFont(font3);
      	plot.getRangeAxis().setLabelFont(font3);
      	
      	LegendTitle legend = transitionsChart.getLegend();
      	Font labelFont = new Font("Arial", Font.BOLD, 12);
      	legend.setItemFont(labelFont);

      	transitionsJPanel.removeAll();
		ChartPanel transChartPanel = new ChartPanel(transitionsChart);
		int transitions = transitionsCompactData.size();
		
		if(transitionsCompactData.size() > 99) {
			transChartPanel.setPreferredSize(new Dimension(transitions*50, 270)); 
			transChartPanel.setMaximumDrawWidth(transitions*30);
		} else {
			transChartPanel.setPreferredSize(new Dimension(transitions*40, 270)); 
			transChartPanel.setMaximumDrawWidth(transitions*30);
		}
		
	    JScrollPane sPane = new JScrollPane(transChartPanel); 
	    transitionsJPanel.add(sPane, BorderLayout.CENTER);
	    transitionsJPanel.revalidate();
	    transitionsJPanel.repaint();
	}
	
	/**
	 * Metoda pokazuje w notatniku dane odpaleń tranzycji w symulacji.
	 */
	private void showTransAllInNotepad() {
		if(transitionsCompactData.size() == 0)
			return;
		
		double max = 0.0;
		long total = 0;
		for(int p=0; p<transitionsCompactData.size(); p++) {
			total += transitionsCompactData.get(p);
			if(transitionsCompactData.get(p) > max)
				max = transitionsCompactData.get(p);
		}
		
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
 		notePad.setVisible(true);
 		ArrayList<Transition> trans_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
 		notePad.addTextLineNL("", "text");
 		notePad.addTextLineNL("Transitions: (sum of all firing: "+total+")", "text");
 		notePad.addTextLineNL("", "text");
 		for(int t=0; t<transitionsCompactData.size(); t++) {
 			String t_name = trans_tmp.get(t).getName();
 			double val = transitionsCompactData.get(t);
 			if(val > 0)
 				notePad.addTextLineNL("  t_"+t+" "+t_name+" : Fired: "+val+" (max: "+max+")", "text");
 			else {
 				if(trans_tmp.get(t).isOffline() == true) {
 					notePad.addTextLineNL("MANUALLY DISABLED: t_"+t+" "+t_name+" : "+val, "text");
 				} else {
 					notePad.addTextLineNL("OFFLINE: t_"+t+" "+t_name+" : "+val, "text");
 				}
 			}
 		}
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************      OGÓLNE      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	
	/**
	 * Metoda zapisująca aktualnie wyświetlany wykres do pliku.
	 */
	private void saveChartImage(String chartType, int w, int h) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Portable Network Graphics (.png)", new String[] { "PNG" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "", "");
		if(selectedFile.equals(""))
			return;
		
		if(!selectedFile.contains(".png"))
			selectedFile += ".png";
		
		File imageFile = new File(selectedFile);
		int width = w; //1024;
		int height = h; //768;
		 
		try {
			if(chartType.equals("places")) {
				ChartUtilities.saveChartAsPNG(imageFile, placesChart, width, height);
			} else if(chartType.equals("transitions")) {
				ChartUtilities.saveChartAsPNG(imageFile, transitionsChart, width, height);
			}
		} catch (IOException ex) {
		    System.err.println(ex);
		}
	}
	
	/**
	 * Metoda wypełnia komponenty rozwijalne danymi o miejscach i tranzycjach.
	 */
	private void fillPlacesAndTransitionsData() {
		selStateLabel.setText(""+overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN);
		
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places== null || places.size() == 0) {
			placesCombo.removeAllItems();
			placesCombo.addItem("---");
			transitionsCombo.removeAllItems();
			transitionsCombo.addItem("---");
			
			return;
		}
		
		placesCombo.removeAllItems();
		placesCombo.addItem("---");
		if(placesAvgData.size() == places.size()) {
			if(sortedP == false) {
				for(int p=0; p < places.size(); p++) {
					placesCombo.addItem("p"+(p)+"."+places.get(p).getName() + " "+formatD(placesAvgData.get(p)));
				}
			} else {
				//sortowanie po odpaleniach:
				Map<Integer, Double> map = new HashMap<Integer, Double>();
				for(int j=0; j<placesAvgData.size(); j++) {
					map.put(j, placesAvgData.get(j));
				}
				//sortuj po value (frequency)
				Map<Integer, Double> sortedByValues = new HashMap<Integer, Double>(); 
				sortedByValues = HolmesSimActions.crunchifySortMap(map); // dark magic happens here
				for (Map.Entry<Integer, Double> entry : sortedByValues.entrySet()) {
					placesCombo.addItem("p"+(entry.getKey())+"."+places.get(entry.getKey()).getName() + " "+formatD(entry.getValue()));
				}
			}
		} else {
			for(int p=0; p < places.size(); p++) {
				placesCombo.addItem("p"+(p)+"."+places.get(p).getName());
			}
		}
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		if(transitions== null || transitions.size() == 0)
			return;
		
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		
		if(transAvgData.size() == transitions.size()) {
			if(sortedT == false) {
				for(int t=0; t < transitions.size(); t++) {
					transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName() + " "+formatD(transAvgData.get(t)));
				}
			} else {
				//sortowanie po odpaleniach:
				Map<Integer, Double> map = new HashMap<Integer, Double>();
				for(int j=0; j<transAvgData.size(); j++) {
					map.put(j, transAvgData.get(j));
				}
				//sortuj po value (frequency)
				Map<Integer, Double> sortedByValues = new HashMap<Integer, Double>(); 
				sortedByValues = HolmesSimActions.crunchifySortMap(map); // dark magic happens here
				for (Map.Entry<Integer, Double> entry : sortedByValues.entrySet()) {
					transitionsCombo.addItem("t"+(entry.getKey())+"."+transitions.get(entry.getKey()).getName() + " "+formatD(entry.getValue()));
					
					
				}
			}
		} else {
			for(int t=0; t < transitions.size(); t++) {
				transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
			}
		}
	}

    /**
     * Metoda wywoływana w wątku, symuluje daną liczbę kroków sieci, a następnie
     * pobiera tablice i wektory danych z obiektu symulatora do struktur wewnętrznych
     * obiektu klasy HolmesStateSimulator - czyli podokna programu głównego.
     */
	private void acquireDataFromSimulation() {
		if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(ego,
					"Main simulator active. Please turn if off before starting state simulator process", 
					"Main simulator active", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places == null || places.size() == 0)
			return;
		
		clearTransitionsChart();
		clearPlacesChart();
		clearAllData();
		
		progressBar.setMaximum(overlord.simSettings.getSimSteps());

		boolean success = ssim.initiateSim(true, null);
		if(success == false)
			return;
		
		//zablokuj kilka elementów okna symulatora
		setSimWindowComponentsStatus(false);
		setWorkInProgress(true);
		
		ssim.setThreadDetails(1, this, progressBar);
		Thread myThread = new Thread(ssim);
		myThread.start();
	}
	
	/**
	 * Blokuje komponenty na czas symulacji.
	 */
	private void setSimWindowComponentsStatus(boolean value) {
		acqDataButton.setEnabled(value);
		stateManagerButton.setEnabled(value);
		overlord.getFrame().setEnabled(value);
	}
	
	/**
	 * Metoda wywoływana zdalnie, kiedy wątek symulacji głównej (Simple) zakończy obliczenia
	 */
	public void completeSimulationProcedures() {
		//pobieranie wektorów danych zebranych w symulacji:
		placesRawData = ssim.getPlacesData();
		placesAvgData = ssim.getPlacesAvgData();
		placesTotalData = ssim.getPlacesTotalData();
		transitionsRawData = ssim.getTransitionsData();
		transitionsCompactData = ssim.getTransitionsCompactData();
		transAvgData = ssim.getTransitionsAvgData();
		
		placesInChart = new ArrayList<Integer>();
		placesInChartStr  = new ArrayList<String>();
		for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
			placesInChart.add(-1);
			placesInChartStr.add("");
		}
		
		fillPlacesAndTransitionsData();
		setSimWindowComponentsStatus(true);
		setWorkInProgress(false);
	}
	
	/**
	 * Metoda czyści wszystkie sturktury danych.
	 */
	private void clearAllData() {
		ssim = new StateSimulator();
		placesRawData.clear();
		transitionsRawData.clear();
		transitionsCompactData.clear();
		placesAvgData.clear();
		placesTotalData.clear();
		transAvgData.clear();
		
		placesInChart.clear();
		placesInChartStr.clear();
		
	}
	
	/**
	 * Konwersja liczby double do określonej długości łańcucha znaków.
	 * @param value double - liczba do konwersji
	 * @return String - liczba sformatowana
	 */
	private String formatD(double value) {
        DecimalFormat df = new DecimalFormat("#.#######");
        String txt = "(avg: ";
        txt += df.format(value);
        txt += ")";
        txt = txt.replace(",", ".");
		return txt;
	}
	
	/**
	 * Metoda przycina nazwę węzła, obcinając informacje o średniej liczbie wystąpień
	 * w symulacji stanów.
	 * @param name String - nazwa z comboBox
	 * @return String - przycięta nazwa
	 */
	private String trimNodeName(String name) {
		int i = name.indexOf("(avg");
		if(i<0) {
			return name;
		} else {
			name = name.substring(0, i-1);
			return name;
		}
	}

	/**
	 * Metoda uaktualniania stylu wyświetlania
	 */
	private void updatePlacesGraphicChart(String chartType) {
		if(chartType.equals("places")) {
			if(placesChartType == 0)
				return;
			
			XYPlot plot = placesChart.getXYPlot();
			int count =  placesChart.getXYPlot().getSeriesCount();
			for(int i=0; i<count; i++) {
				plot.getRenderer().setSeriesStroke(i, new BasicStroke(chartDetails.p_StrokeWidth));
			}
		} else if(chartType.equals("transitions")){
			if(transChartType == 0)
				return;
			
			XYPlot plot = transitionsChart.getXYPlot();
			int count =  transitionsChart.getXYPlot().getSeriesCount();
			for(int i=0; i<count; i++) {
				plot.getRenderer().setSeriesStroke(i, new BasicStroke(chartDetails.t_StrokeWidth));
			}
		}
		
		//XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		//renderer.setSeriesLinesVisible(0, true);
        //renderer.setSeriesShapesVisible(0, false);
        //renderer.setSeriesLinesVisible(1, false);
        //renderer.setSeriesShapesVisible(1, true); 
		 //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	    //plot.setRenderer(renderer);
	    //plot.setBackgroundPaint(Color.DARK_GRAY);
		//plot.setRenderer(renderer);
	}
	
	/**
	 * Umożliwia dostęp do symulatora.
	 * @return StateSimulator - obiekt symulatora
	 */
	public StateSimulator accessSim() {
		return ssim;
	}

	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillPlacesAndTransitionsData();
  	  	    	((HolmesSimKnock) knockoutTab).updateFreshKnockoutTab();
  	  	    }  
    	});
    	
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	if(isWorkInProgress()) {
		    		JOptionPane.showMessageDialog(ego, "Simulator working. Window closing operation cancelled.", 
							"Simulator working",JOptionPane.INFORMATION_MESSAGE);
		    		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		    	} else {
		    		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		    		overlord.getFrame().setEnabled(true);
		    	}
		    	
		    }
		});

    }
    
    /**
     * Ustawia status pracy w toku - kiedy symulator pracuje.
     * @param value boolean - true, jeśli trwa symulacja
     */
    public void setWorkInProgress(boolean value) {
    	this.workInProgress = value;
    }
    
    /**
     * Zwraca status pracy w toku, jeśli true - działa symulator.
     * @return boolean -  true/false j.w.
     */
    public boolean isWorkInProgress() {
    	return this.workInProgress;
    }
    
    /**
     * Umożliwia dostęp do obiektu odpowiedzialnego za zakładkę Knockout symulatora.
     * @return HolmesStateSimulatorKnockout - obiekt
     */
    public HolmesSimKnock accessKnockoutTab() {
    	return (HolmesSimKnock)knockoutTab;
    }
	
	/**
	 * Reset okna symulatora.
	 */
	public void resetSimWindow() {
		doNotUpdate = true;
		clearTransitionsChart();
		clearPlacesChart();
		clearAllData();
		fillPlacesAndTransitionsData();
		
		transInterval = 100;
		placesInterval = 100;
		transChartType = 0;
		placesChartType = 0;
		
		int mValue = overlord.simSettings.getSimSteps()/10;
		SpinnerModel intervSpinnerModel = new SpinnerNumberModel(100, 0, mValue, 10);
		transIntervalSpinner.setModel(intervSpinnerModel);
		
		SpinnerModel intervSpinnerModel2 = new SpinnerNumberModel(100, 0, mValue, 10);
		placesIntervalSpinner.setModel(intervSpinnerModel2);
		
		
		((HolmesSimKnock)knockoutTab).resetWindow();
		doNotUpdate = false;
	}
	
	public void updateIntervalSpinner() {
		int mValue = overlord.simSettings.getSimSteps()/10;
		SpinnerModel intervSpinnerModel = new SpinnerNumberModel(100, 0, mValue, 10);
		transIntervalSpinner.setModel(intervSpinnerModel);
		
		SpinnerModel intervSpinnerModel2 = new SpinnerNumberModel(100, 0, mValue, 10);
		placesIntervalSpinner.setModel(intervSpinnerModel2);
	}
	
	public JFrame getFrame() {
		return ego;
	}
	
	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************
	
	private class ChartProperties {
		public float p_StrokeWidth = 1.0f;
		public float t_StrokeWidth = 1.0f;
		
		public ChartProperties() {}
	}
	
	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************	
	
	public class CustomToolTipTransGenerator implements CategoryToolTipGenerator  {
		ArrayList<Transition> transitions;
		ArrayList<Integer> dataVector;
		double max;
		DecimalFormat formatter;

		public CustomToolTipTransGenerator(ArrayList<Transition> transitions, ArrayList<Integer> dataVector, double max) {
			this.transitions = transitions;
			this.dataVector = dataVector;
			this.max = max;
			formatter = new DecimalFormat("#.###");
		}
		
		@Override
	    public String generateToolTip(CategoryDataset dataset, int bar, int nodeIndex)   {
	    	String text = "<html><font size=\"5\">";
	    	text += "t"+nodeIndex+"_"+transitions.get(nodeIndex).getName()+"<br>";
	    	text += "Fired: "+dataVector.get(nodeIndex)+"   (maximum in this simulation: "+max+")";
	    	text += "</font></html>";
	    	return text;
	    }
	}
	
	public class CustomToolTipPlacesGenerator implements CategoryToolTipGenerator  {
		ArrayList<Place> places;
		ArrayList<Double> dataVector;
		double max;
		DecimalFormat formatter;

		public CustomToolTipPlacesGenerator(ArrayList<Place> places, ArrayList<Double> dataVector, double max) {
			this.places = places;
			this.dataVector = dataVector;
			this.max = max;
			formatter = new DecimalFormat("#.###");
		}
		
		@Override
	    public String generateToolTip(CategoryDataset dataset, int bar, int nodeIndex)   {
	    	String text = "<html><font size=\"5\">";
	    	text += "p"+nodeIndex+"_"+places.get(nodeIndex).getName()+"<br>";
	    	text += "Tokens: "+dataVector.get(nodeIndex)+"   (maximum in this simulation: "+max+")";
	    	text += "</font></html>";
	    	return text;
	    }
	}
}
