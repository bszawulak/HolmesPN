package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import abyss.darkgui.GUIManager;
import abyss.petrinet.data.NetSimulationData;
import abyss.petrinet.data.PetriNet;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.simulators.StateSimulator;
import abyss.petrinet.simulators.NetSimulator.NetType;
import abyss.utilities.Tools;

/**
 * Klasa odpowiedzialna za tworzenie podstrony knockoutSim.
 * 
 * @author MR
 */
public class AbyssStateSimulatorKnockout extends JPanel {
	private static final long serialVersionUID = 4257940971120618716L;
	public AbyssStateSimulatorKnockoutActions action;
	private boolean doNotUpdate = false;
	public StateSimulator ssimKnock;
	public AbyssStateSimulator mainSimWindow;
	
	//reference simulation panel:
	public int refSimSteps = 1000;			//liczba kroków dla zbioru referencyjnego
	public int refRepetitions = 100;
	public NetType refNetType = NetType.BASIC;		//rodzaj sieci: BASIC, TIMED, HYBRID, itd.
	public boolean refMaximumMode = false;
	public JProgressBar refProgressBarKnockout;
	private JComboBox<String> referencesCombo = null;
	
	//reference details panel:
	private JLabel refLabelDate;
	private JLabel refLabelSimNetMode;
	private JLabel refLabelMaxMode;
	private JLabel refLabelSteps;
	private JLabel refLabelReps;
	
	//data sets acquisition panel:
	private JComboBox<String> dataTransitionsCombo = null;
	private JComboBox<String> dataMctCombo = null;
	public int dataSimSteps = 1000;			//liczba kroków dla zbioru referencyjnego
	public int dataRepetitions = 100;
	public NetType dataNetType = NetType.BASIC;		//rodzaj sieci: BASIC, TIMED, HYBRID, itd.
	public boolean dataMaximumMode = false;
	public JProgressBar dataProgressBarKnockout;
	public JTextArea dataSelectedTrans;
	public boolean dataSimUseEditorOffline = false;
	public boolean dataSimComputeAll = false;
	
	//blokowane elementy:
	private JButton acqRefDataButton;
	private JComboBox<String> refSimNetMode;
	private JComboBox<String> refSimMaxMode;
	private JSpinner refSimStepsSpinner;
	private JSpinner refSimRepsSpinner;
	
	private JButton acqDataSimButton;
	private JComboBox<String> dataSimNetMode;
	private JComboBox<String> dataSimMaxMode;
	private JSpinner dataSimStepsSpinner;
	private JSpinner dataSimRepsSpinner;
	private JCheckBox dataSimUseEditorOfflineCheckBox;
	private JCheckBox dataSimComputeForAllTransitions;
	
	/**
	 * Konstruktor obiektu klasy AbyssStateSimulatorKnockout.
	 * @param abyssStateSimulator 
	 */
	public AbyssStateSimulatorKnockout(AbyssStateSimulator abyssStateSimulator) {
		this.mainSimWindow = abyssStateSimulator;
		ssimKnock = new StateSimulator();
		action = new AbyssStateSimulatorKnockoutActions(this);
		
		setLayout(new BorderLayout());
		
		add(getTopPanel(), BorderLayout.NORTH);
		
		JPanel refAndDataPanel = new JPanel(new BorderLayout());
		
		JPanel refAndButtonsPanel = new JPanel(new BorderLayout());
		refAndButtonsPanel.setPreferredSize(new Dimension(600, 100));
		refAndButtonsPanel.add(getReferencesPanel(), BorderLayout.CENTER);
		refAndButtonsPanel.add(getButtonsPanel(), BorderLayout.EAST);
		
		refAndDataPanel.add(refAndButtonsPanel, BorderLayout.NORTH);
		
		JPanel dataAndChartsPanel = new JPanel(new BorderLayout());
		dataAndChartsPanel.add(getSimDataAcqPanel(), BorderLayout.NORTH);
		dataAndChartsPanel.add(getChartsPanel(), BorderLayout.CENTER);
		refAndDataPanel.add(dataAndChartsPanel, BorderLayout.CENTER);
		
		add(refAndDataPanel, BorderLayout.CENTER);
	}
	
	/**
	 * Metoda tworząca panel tworzenia zbioru referencyjnego.
	 * @return JPanel - obiekt panelu
	 */
	public JPanel getTopPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("New reference set simulation setup"));
		result.setPreferredSize(new Dimension(670, 100));
	
		int posXda = 10;
		int posYda = 10;
		
		acqRefDataButton = new JButton("SimStart");
		acqRefDataButton.setBounds(posXda, posYda+10, 110, 40);
		acqRefDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqRefDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqRefDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
		acqRefDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				action.acquireDataForRefSet();
			}
		});
		result.add(acqRefDataButton);
		
		JButton cancelButton = new JButton();
		cancelButton.setText("STOP");
		cancelButton.setBounds(posXda, posYda+55, 110, 25);
		cancelButton.setMargin(new Insets(0, 0, 0, 0));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ssimKnock.terminate = true;
			}
		});
		cancelButton.setFocusPainted(false);
		result.add(cancelButton);
		
		//Main mode:
		JLabel simMainModeLabel = new JLabel("Main mode:");
		simMainModeLabel.setBounds(posXda+120, posYda, 80, 20);
		result.add(simMainModeLabel);

		String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
		refSimNetMode = new JComboBox<String>(simModeName);
		refSimNetMode.setBounds(posXda+120, posYda+20, 120, 20);
		refSimNetMode.setSelectedIndex(0);
		refSimNetMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				int selectedModeIndex = refSimNetMode.getSelectedIndex();
				selectedModeIndex = GUIManager.getDefaultGUIManager().simSettings.checkSimulatorNetType(selectedModeIndex);
				doNotUpdate = true;
				switch(selectedModeIndex) {
					case 0:
						refNetType = NetType.BASIC;
						refSimNetMode.setSelectedIndex(0);
						break;
					case 1:
						refNetType = NetType.TIME;
						refSimNetMode.setSelectedIndex(1);
						break;
					case 2:
						refNetType = NetType.HYBRID;
						refSimNetMode.setSelectedIndex(2);
						break;
					case -1:
						refNetType = NetType.BASIC;
						refSimNetMode.setSelectedIndex(1);
						GUIManager.getDefaultGUIManager().log("Error while changing simulator mode for reference set. Set for BASIC.", "error", true);
						break;
				}
				doNotUpdate = false;
			}
		});
		result.add(refSimNetMode);
		
		//Sub-mode:
		JLabel label1 = new JLabel("Sub-mode:");
		label1.setBounds(posXda+120, posYda+40, 90, 20);
		result.add(label1);
		
		refSimMaxMode = new JComboBox<String>(new String[] {"50/50 mode", "Maximum mode",});
		refSimMaxMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
		refSimMaxMode.setBounds(posXda+120, posYda+60, 120, 20);
		refSimMaxMode.setSelectedIndex(0);
		refSimMaxMode.setMaximumRowCount(6);
		refSimMaxMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = refSimMaxMode.getSelectedIndex();
				if(selected == 0)
					refMaximumMode = false;
				else
					refMaximumMode = true;
			}
		});
		result.add(refSimMaxMode);

		JLabel simStepsLabel = new JLabel("Steps:");
		simStepsLabel.setBounds(posXda+250, posYda, 80, 20);
		result.add(simStepsLabel);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(refSimSteps, 100, 1000000, 100);
		refSimStepsSpinner = new JSpinner(simStepsSpinnerModel);
		refSimStepsSpinner.setBounds(posXda+250, posYda+20, 80, 20);
		refSimStepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				refSimSteps = val;
			}
		});
		result.add(refSimStepsSpinner);

		JLabel repetLabel = new JLabel("Repetitions:");
		repetLabel.setBounds(posXda+250, posYda+40, 80, 20);
		result.add(repetLabel);
		
		SpinnerModel simRepetsSpinnerModel = new SpinnerNumberModel(refRepetitions, 1, 100000, 10);
		refSimRepsSpinner = new JSpinner(simRepetsSpinnerModel);
		refSimRepsSpinner.setBounds(posXda+250, posYda+60, 80, 20);
		refSimRepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				refRepetitions = val;
			}
		});
		result.add(refSimRepsSpinner);
		
		refProgressBarKnockout = new JProgressBar();
		refProgressBarKnockout.setBounds(posXda+340, posYda+3, 550, 40);
		refProgressBarKnockout.setMaximum(100);
		refProgressBarKnockout.setMinimum(0);
	    refProgressBarKnockout.setValue(0);
	    refProgressBarKnockout.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    refProgressBarKnockout.setBorder(border);
	    result.add(refProgressBarKnockout);
		
		posYda += 40;
		return result;
	}

	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************

	/**
	 * Metoda tworząca panel wyświetlania danych o zbiorach referencyjnych
	 * @return JPanel - obiekt panelu
	 */
	public JPanel getReferencesPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Reference set selection and details"));
		result.setPreferredSize(new Dimension(500, 150));
		doNotUpdate = true;
		int posXda = 10;
		int posYda = 20;
		
		JLabel label1 = new JLabel("Ref. sets:");
		label1.setBounds(posXda, posYda, 70, 20);
		result.add(label1);
		
		String[] data = { " ----- " };
		referencesCombo = new JComboBox<String>(data); //final, aby listener przycisku odczytał wartość
		referencesCombo.setBounds(posXda+80, posYda, 400, 20);
		referencesCombo.setMaximumRowCount(12);
		referencesCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) 
					return;
				
				int selected = referencesCombo.getSelectedIndex();
				if(selected > 0) {
					updateRefDetails(selected-1);
				} 
			}
			
		});
		result.add(referencesCombo);

		JLabel dateTxtLabel = new JLabel("Date:");
		dateTxtLabel.setBounds(posXda, posYda+20, 70, 20);
		result.add(dateTxtLabel);
		refLabelDate = new JLabel("---");
		refLabelDate.setBounds(posXda+80, posYda+20, 120, 20);
		result.add(refLabelDate);
		
		JLabel modeTxtLabel = new JLabel("Net mode:");
		modeTxtLabel.setBounds(posXda+210, posYda+20, 70, 20);
		result.add(modeTxtLabel);
		refLabelSimNetMode = new JLabel("---");
		refLabelSimNetMode.setBounds(posXda+290, posYda+20, 70, 20);
		result.add(refLabelSimNetMode);
		
		JLabel maxModeTxtLabel = new JLabel("Max mode:");
		maxModeTxtLabel.setBounds(posXda+360, posYda+20, 70, 20);
		result.add(maxModeTxtLabel);
		refLabelMaxMode = new JLabel("---");
		refLabelMaxMode.setBounds(posXda+430, posYda+20, 90, 20);
		result.add(refLabelMaxMode);
		
		JLabel stepsTxtLabel = new JLabel("Sim. steps:");
		stepsTxtLabel.setBounds(posXda, posYda+40, 70, 20);
		result.add(stepsTxtLabel);
		refLabelSteps = new JLabel("---");
		refLabelSteps.setBounds(posXda+80, posYda+40, 80, 20);
		result.add(refLabelSteps);
		
		JLabel repsTxtLabel = new JLabel("Repetitions:");
		repsTxtLabel.setBounds(posXda+210, posYda+40, 70, 20);
		result.add(repsTxtLabel);
		refLabelReps = new JLabel("---");
		refLabelReps.setBounds(posXda+290, posYda+40, 90, 20);
		result.add(refLabelReps);
		
		doNotUpdate = false;
	    return result;
	}
	
	/**
	 * Metoda zwraca panel przycisków bocznych - zapis/odczyt.
	 * @return JPanel - panel. Okrętu się pan spodziewałeś?
	 */
	public JPanel getButtonsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Knockout simulation data setup"));
		result.setPreferredSize(new Dimension(300, 150));
		doNotUpdate = true;
		int posXda = 10;
		int posYda = 15;
		
		JButton saveAllButton = new JButton("Save all");
		saveAllButton.setBounds(posXda, 20, 110, 35);
		saveAllButton.setMargin(new Insets(0, 0, 0, 0));
		saveAllButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeDa.png"));
		saveAllButton.setToolTipText("Saves all simulation data to single file.");
		saveAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				action.saveDataSets();
			}
		});
		result.add(saveAllButton);
		
		JButton loadAllButton = new JButton("Load all");
		loadAllButton.setBounds(posXda, 60, 110, 35);
		loadAllButton.setMargin(new Insets(0, 0, 0, 0));
		loadAllButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeDa.png"));
		loadAllButton.setToolTipText("Saves all simulation data to single file.");
		loadAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				action.loadDataSets();
			}
		});
		result.add(loadAllButton);
		
		doNotUpdate = false;
	    return result;
	}
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	
	public JPanel getSimDataAcqPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Knockout data acquisition setup"));
		result.setPreferredSize(new Dimension(670, 165));
		doNotUpdate = true;
		int posXda = 10;
		int posYda = 20;
		
		JLabel transitionsLabel = new JLabel("Transitions:");
		transitionsLabel.setBounds(posXda, posYda, 70, 20);
		result.add(transitionsLabel);
		
		//TODO:
		String[] data = { " ----- " };
		dataTransitionsCombo = new JComboBox<String>(data);
		dataTransitionsCombo.setBounds(posXda+80, posYda, 400, 20);
		dataTransitionsCombo.setMaximumRowCount(12);
		dataTransitionsCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = dataTransitionsCombo.getSelectedIndex();
			}
			
		});
		result.add(dataTransitionsCombo);
		
		JButton addTransButton = new JButton("Add");
		addTransButton.setBounds(posXda+485, posYda, 70, 20);
		addTransButton.setMargin(new Insets(0, 0, 0, 0));
		addTransButton.setIcon(Tools.getResIcon16("/icons/stateSim/ss.png"));
		addTransButton.setToolTipText("Sets transition for offline in the incoming simulation sesssion.");
		addTransButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//action.saveDataSets();
			}
		});
		result.add(addTransButton);
		
		JButton removeTransButton = new JButton("Del");
		removeTransButton.setBounds(posXda+560, posYda, 70, 20);
		removeTransButton.setMargin(new Insets(0, 0, 0, 0));
		removeTransButton.setIcon(Tools.getResIcon16("/icons/stateSim/ss.png"));
		removeTransButton.setToolTipText("Remove transitions from offline set.");
		removeTransButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//action.saveDataSets();
			}
		});
		result.add(removeTransButton);
		
		JLabel MCTsLabel = new JLabel("MCT set:");
		MCTsLabel.setBounds(posXda, posYda+25, 70, 20);
		result.add(MCTsLabel);
		
		String[] data2 = { " ----- " };
		dataMctCombo = new JComboBox<String>(data2);
		dataMctCombo.setBounds(posXda+80, posYda+25, 400, 20);
		dataMctCombo.setMaximumRowCount(12);
		dataMctCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = dataMctCombo.getSelectedIndex();
			}
			
		});
		result.add(dataMctCombo);
		
		JButton addMCTButton = new JButton("Add");
		addMCTButton.setBounds(posXda+485, posYda+25, 70, 20);
		addMCTButton.setMargin(new Insets(0, 0, 0, 0));
		addMCTButton.setIcon(Tools.getResIcon16("/icons/stateSim/ss.png"));
		addMCTButton.setToolTipText("Sets whole MCT for offline in the incoming simulation sesssion.");
		addMCTButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//action.saveDataSets();
			}
		});
		result.add(addMCTButton);
		
		JButton removeMCTButton = new JButton("Del");
		removeMCTButton.setBounds(posXda+560, posYda+25, 70, 20);
		removeMCTButton.setMargin(new Insets(0, 0, 0, 0));
		removeMCTButton.setIcon(Tools.getResIcon16("/icons/stateSim/ss.png"));
		removeMCTButton.setToolTipText("Remove MCT set from offline set.");
		removeMCTButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//action.saveDataSets();
			}
		});
		result.add(removeMCTButton);
		
		dataSelectedTrans = new JTextArea();
		dataSelectedTrans.setLineWrap(true);
		dataSelectedTrans.setEditable(true);
        JPanel dataFieldPanel = new JPanel();
        dataFieldPanel.setLayout(new BorderLayout());
        dataFieldPanel.add(new JScrollPane(dataSelectedTrans),BorderLayout.CENTER);
        dataFieldPanel.setBounds(posXda+640, posYda, 230, 45);
        result.add(dataFieldPanel);
        
        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(posXda+870, posYda, 70, 45);
        clearButton.setMargin(new Insets(0, 0, 0, 0));
        clearButton.setIcon(Tools.getResIcon16("/icons/stateSim/ss.png"));
        clearButton.setToolTipText("Clear offline set.");
        clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				dataSelectedTrans.setText("");
			}
		});
		result.add(clearButton);
		
		//przyciski symulacji:
		
		posYda+=50;
		
		acqDataSimButton = new JButton("SimStart");
		acqDataSimButton.setBounds(posXda, posYda+10, 110, 40);
		acqDataSimButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataSimButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataSimButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
		acqDataSimButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//action.acquireDataForRefSet();
			}
		});
		result.add(acqDataSimButton);
		
		JButton cancelButton = new JButton();
		cancelButton.setText("STOP");
		cancelButton.setBounds(posXda, posYda+55, 110, 25);
		cancelButton.setMargin(new Insets(0, 0, 0, 0));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//ssimKnock.terminate = true;
			}
		});
		cancelButton.setFocusPainted(false);
		result.add(cancelButton);
		
		//Main mode:
		JLabel simMainModeLabel = new JLabel("Main mode:");
		simMainModeLabel.setBounds(posXda+120, posYda, 80, 20);
		result.add(simMainModeLabel);

		String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
		dataSimNetMode = new JComboBox<String>(simModeName);
		dataSimNetMode.setBounds(posXda+120, posYda+20, 120, 20);
		dataSimNetMode.setSelectedIndex(0);
		dataSimNetMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				int selectedModeIndex = dataSimNetMode.getSelectedIndex();
				selectedModeIndex = GUIManager.getDefaultGUIManager().simSettings.checkSimulatorNetType(selectedModeIndex);
				doNotUpdate = true;
				switch(selectedModeIndex) {
					case 0:
						dataNetType = NetType.BASIC;
						dataSimNetMode.setSelectedIndex(0);
						break;
					case 1:
						dataNetType = NetType.TIME;
						dataSimNetMode.setSelectedIndex(1);
						break;
					case 2:
						dataNetType = NetType.HYBRID;
						dataSimNetMode.setSelectedIndex(2);
						break;
					case -1:
						dataNetType = NetType.BASIC;
						dataSimNetMode.setSelectedIndex(1);
						GUIManager.getDefaultGUIManager().log("Error while changing simulator mode for knockout simulation set. "
								+ "Set for BASIC.", "error", true);
						break;
				}
				doNotUpdate = false;
			}
		});
		result.add(dataSimNetMode);
		
		//Sub-mode:
		JLabel label1 = new JLabel("Sub-mode:");
		label1.setBounds(posXda+120, posYda+40, 90, 20);
		result.add(label1);
		
		dataSimMaxMode = new JComboBox<String>(new String[] {"50/50 mode", "Maximum mode",});
		dataSimMaxMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
		dataSimMaxMode.setBounds(posXda+120, posYda+60, 120, 20);
		dataSimMaxMode.setSelectedIndex(0);
		dataSimMaxMode.setMaximumRowCount(6);
		dataSimMaxMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = refSimMaxMode.getSelectedIndex();
				if(selected == 0)
					dataMaximumMode = false;
				else
					dataMaximumMode = true;
			}
		});
		result.add(dataSimMaxMode);

		JLabel simStepsLabel = new JLabel("Steps:");
		simStepsLabel.setBounds(posXda+250, posYda, 80, 20);
		result.add(simStepsLabel);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(dataSimSteps, 100, 1000000, 100);
		refSimStepsSpinner = new JSpinner(simStepsSpinnerModel);
		refSimStepsSpinner.setBounds(posXda+250, posYda+20, 80, 20);
		refSimStepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				refSimSteps = val;
			}
		});
		result.add(refSimStepsSpinner);

		JLabel repetLabel = new JLabel("Repetitions:");
		repetLabel.setBounds(posXda+250, posYda+40, 80, 20);
		result.add(repetLabel);
		
		SpinnerModel simRepetsSpinnerModel = new SpinnerNumberModel(dataRepetitions, 1, 100000, 10);
		dataSimRepsSpinner = new JSpinner(simRepetsSpinnerModel);
		dataSimRepsSpinner.setBounds(posXda+250, posYda+60, 80, 20);
		dataSimRepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				dataRepetitions = val;
			}
		});
		result.add(dataSimRepsSpinner);
		
		JPanel special = new JPanel(null);
		special.setBorder(BorderFactory.createTitledBorder("Special options"));
		special.setBounds(posXda+340, posYda, 550, 40);
		result.add(special);
		
		dataSimUseEditorOfflineCheckBox = new JCheckBox("Use editor offline marks");
		dataSimUseEditorOfflineCheckBox.setBounds(10, 15, 170, 20);
		dataSimUseEditorOfflineCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					dataSimUseEditorOffline = true;
				} else {
					dataSimUseEditorOffline = false;
				}
			}
		});
		special.add(dataSimUseEditorOfflineCheckBox);
		
		dataSimComputeForAllTransitions = new JCheckBox("All transitions (one by one)");
		dataSimComputeForAllTransitions.setBounds(180, 15, 180, 20);
		dataSimComputeForAllTransitions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					dataSimComputeAll = true;
				} else {
					dataSimComputeAll = false;
				}
			}
		});
		special.add(dataSimComputeForAllTransitions);
		
		dataProgressBarKnockout = new JProgressBar();
		dataProgressBarKnockout.setBounds(posXda+340, posYda+45, 550, 40);
		dataProgressBarKnockout.setMaximum(100);
		dataProgressBarKnockout.setMinimum(0);
		dataProgressBarKnockout.setValue(0);
		dataProgressBarKnockout.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    dataProgressBarKnockout.setBorder(border);
	    result.add(dataProgressBarKnockout);
		
		doNotUpdate = false;
	    return result;
	}
	
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	
	public JPanel getChartsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Knockout data acquisition setup"));
		result.setPreferredSize(new Dimension(670, 50));
		doNotUpdate = true;
		int posXda = 10;
		int posYda = 30;
		
		
		doNotUpdate = false;
	    return result;
	}
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	
	/**
	 * Blokuje komponenty na czas symulacji.
	 */
	public void blockSimWindowComponents() {
		acqRefDataButton.setEnabled(false);
		refSimNetMode.setEnabled(false);
		refSimMaxMode.setEnabled(false);
		refSimStepsSpinner.setEnabled(false);
		refSimRepsSpinner.setEnabled(false);
		
		GUIManager.getDefaultGUIManager().getFrame().setEnabled(false);
		//TODO: dodawać kolejne tutaj:
		
		
	}
	
	/**
	 * Odblokowuje komponenty po zakończonej / przerwanej symulacji.
	 */
	public void unblockSimWindowComponents() {
		acqRefDataButton.setEnabled(true);
		refSimNetMode.setEnabled(true);
		refSimMaxMode.setEnabled(true);
		refSimStepsSpinner.setEnabled(true);
		refSimRepsSpinner.setEnabled(true);
		
		GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
		//TODO: dodawać kolejne tutaj:
	}
	
	/**
	 * Resetuje ustawienia do domyślnych.
	 */
	public void resetWindow() {
		doNotUpdate = true;
		
		//reference set simulation panel:
		refNetType = NetType.BASIC;
		refSimNetMode.setSelectedIndex(0);
		refMaximumMode = false;
		refSimMaxMode.setSelectedIndex(0);
		refSimSteps = 1000;
		SpinnerNumberModel spinnerClustersModel = new SpinnerNumberModel(refSimSteps, 100, 1000000, 100);
		refSimStepsSpinner.setModel(spinnerClustersModel);
		refProgressBarKnockout.setValue(0);
		
		//ref details panel:
		
		doNotUpdate = false;
	}
	
	/**
	 * Aktualizuje komponenty panelu.
	 */
	public void updateFreshKnockoutTab() {
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		ArrayList<NetSimulationData> references = pn.accessSimKnockoutData().getReferenceSets();
		int refSize = references.size();
		doNotUpdate = true;
		int oldSelected = 0;
		if(refSize > 0) {
			oldSelected = referencesCombo.getSelectedIndex();
			referencesCombo.removeAllItems();
			referencesCombo.addItem(" ----- ");
			for(int r=0; r<refSize; r++) {
				String name = "Ref:"+r+" Date: "+references.get(r).date+" NetMode:"+references.get(r).netSimType+
						" MaxMode:"+references.get(r).maxMode;
				referencesCombo.addItem(name);
			}
			
			if(oldSelected < referencesCombo.getItemCount())
				referencesCombo.setSelectedIndex(oldSelected);
			else
				referencesCombo.setSelectedIndex(0);

			refLabelDate.setText("---");
			refLabelSimNetMode.setText("---");
			refLabelMaxMode.setText("---");
			refLabelSteps.setText("---");
			refLabelReps.setText("---");
		}
		doNotUpdate = false;
	}

	public void updateRefDetails(int selected) {
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		ArrayList<NetSimulationData> references = pn.accessSimKnockoutData().getReferenceSets();
		NetSimulationData selectedRef = references.get(selected);
		
		refLabelDate.setText(selectedRef.date);
		refLabelSimNetMode.setText(selectedRef.netSimType.toString());
		if(selectedRef.maxMode)
			refLabelMaxMode.setText("TRUE");
		else
			refLabelMaxMode.setText("FALSE");
		refLabelSteps.setText(""+selectedRef.steps);
		refLabelReps.setText(""+selectedRef.reps);
	}
}
