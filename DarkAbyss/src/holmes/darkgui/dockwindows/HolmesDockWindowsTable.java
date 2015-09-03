package holmes.darkgui.dockwindows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import holmes.analyse.InvariantsTools;
import holmes.analyse.MCTCalculator;
import holmes.analyse.ProblemDetector;
import holmes.clusters.ClusterDataPackage;
import holmes.clusters.ClusterTransition;
import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.PetriNetElement;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.simulators.NetSimulator;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;
import holmes.windows.HolmesStatesManager;
import holmes.workspace.WorkspaceSheet;

/**
 * Klasa zawierająca szczegóły interfejsu podokien dokowalnych programu.
 * @author students
 * @author MR<br>
 * <br>
 * <b>Absolute positioning. Of almost absolute everything here.</b><br>
 * Nie obchodzi mnie, co o tym myślisz (╯゜Д゜）╯︵ ┻━┻) . Idź w layout i nie wracaj. ┌∩┐(◣_◢)┌∩┐ 
 *   
 */
public class HolmesDockWindowsTable extends JPanel {
	private static final long serialVersionUID = 4510802239873443705L;
	private GUIManager overlord;
	private ArrayList<JComponent> components;
	private int mode;
	private ArrayList<Transition> transitions; // j.w.
	private ArrayList<ArrayList<Transition>> mctGroups; //używane tylko w przypadku, gdy obiekt jest typu DockWindowType.MctANALYZER
	private ArrayList<ArrayList<Integer>> knockoutData;
	// Containers & general use
	private JPanel panel; // główny panel okna
	public boolean stopAction = false;
	public boolean doNotUpdate = false;
	//simulator:
	public ButtonGroup group = new ButtonGroup();
	public JSpinner spiner = new JSpinner();
	public JComboBox<String> simMode;
	public JCheckBox maximumModeCheckBox;
	public JCheckBox singleModeCheckBox;
	public JLabel timeStepLabelValue;
	private NetSimulator simulator;  // obiekt symulatora
	// P/T/M/A
	public ButtonGroup groupRadioMetaType = new ButtonGroup();  //metanode
	private boolean nameLocChangeMode = false;
	private PetriNetElement element;
	private ElementLocation elementLocation;
	public SpinnerModel nameLocationXSpinnerModel = null;
	public SpinnerModel nameLocationYSpinnerModel = null;
	//MCT:
	private int selectedMCTindex = -1;
	private boolean colorMCT = false;
	private boolean allMCTselected = false;
	//knockout:
	private JTextArea knockoutTextArea;
	//invariants:
	private ArrayList<ArrayList<Integer>> invariantsMatrix; //używane w podoknie inwariantów
	private int selectedInvIndex = -1;
	private boolean markMCT = false;
	private boolean glowInv = true;
	private JFormattedTextField invNameField;
	//clusters:
	private JComboBox<String> chooseCluster;
	private JComboBox<String> chooseClusterInv;
	private ClusterDataPackage clusterColorsData;
	private JFormattedTextField MCTnameField;
	private int selectedClusterIndex = -1;
	private int selectedClusterInvIndex = -1;
	private boolean clustersMCT = false;
	private JProgressBar progressBar = null;
	private JLabel mssValueLabel;
	//MCS
	public JComboBox<String> mcsObjRCombo;
	public JComboBox<String> mcsMCSforObjRCombo;
	//sheets:
	private WorkspaceSheet currentSheet;
	//fixer:
	public JLabel fixInvariants;
	public JLabel fixInvariants2;
	public JLabel fixIOPlaces;
	public JLabel fixIOTransitions;
	public JLabel fixlinearTrans;
	private ProblemDetector detector;

	// modes
	private static final int PLACE = 0;
	private static final int TRANSITION = 1;
	private static final int ARC = 2;
	private static final int SHEET = 3;
	private static final int SIMULATOR = 4;
	private static final int INVARIANTS = 5;
	private static final int MCT = 6;
	private static final int TIMETRANSITION = 7;
	@SuppressWarnings("unused")
	private static final int INVARIANTSSIMULATOR = 8;
	private static final int CLUSTERS = 9;
	private static final int KNOCKOUT = 10;
	private static final int META = 11;

	public enum SubWindow { SIMULATOR, PLACE, TRANSITION, TIMETRANSITION, META, ARC, SHEET, INVARIANTS, MCT, CLUSTERS, KNOCKOUT, MCS, FIXER }
	
	/**
	 * Konstruktor główny, wybierający odpowiednią metodę do tworzenia podokna wybranego typu
	 * @param subType SubWindow - typ podokna do utworzenia
	 * @param blackBox Object[...] - bliżej nieokreślona lista nieokreślonych parametrów :)
	 */
	@SuppressWarnings("unchecked")
	public HolmesDockWindowsTable(SubWindow subType, Object... blackBox) {
		overlord = GUIManager.getDefaultGUIManager();
		
		if(subType == SubWindow.SIMULATOR) {
			createSimulatorSubWindow((NetSimulator) blackBox[0]);
		} else if (subType == SubWindow.PLACE) {
			createPlaceSubWindow((Place) blackBox[0], (ElementLocation) blackBox[1]);
		} else if (subType == SubWindow.TRANSITION) {
			createTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
		} else if (subType == SubWindow.TIMETRANSITION) {
			createTimeTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
		} else if (subType == SubWindow.META) {
			createMetaNodeSubWindow((MetaNode) blackBox[0], (ElementLocation) blackBox[1]);
		} else if (subType == SubWindow.ARC) {
			createArcSubWindow((Arc) blackBox[0]);
		} else if (subType == SubWindow.SHEET) {
			createSheetSubWindow((WorkspaceSheet) blackBox[0]);
		} else if (subType == SubWindow.INVARIANTS) {
			createInvariantsSubWindow((ArrayList<ArrayList<Integer>>) blackBox[0]);
		} else if (subType == SubWindow.MCT) {
			createMCTSubWindow((ArrayList<ArrayList<Transition>>) blackBox[0]);
		} else if (subType == SubWindow.CLUSTERS) {
			createClustersSubWindow((ClusterDataPackage) blackBox[0]);
		} else if (subType == SubWindow.MCS) {
			createMCSSubWindow((MCSDataMatrix) blackBox[0]);
		} else if (subType == SubWindow.FIXER) {
			createFixerSubWindow();
		} 
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************    SYMULATOR     ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	/**
	 * Metoda pomocnicza konstruktora odpowiedzialna za tworzenie podokna dla symulatora sieci.
	 * @param sim NetSimulator - obiekt symulatora sieci
	 */
	private void createSimulatorSubWindow(NetSimulator sim) {
		int columnA_posX = 10;
		int columnB_posX = 80;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 70;
		
		initiateContainers();
		
		String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
		mode = SIMULATOR;
		setSimulator(sim);
		
		// SIMULATION MODE
		JLabel netTypeLabel = new JLabel("Mode:");
		netTypeLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(netTypeLabel);
		
		simMode = new JComboBox<String>(simModeName);
		simMode.setLocation(columnB_posX-30, columnB_Y += 10);
		simMode.setSize(colBCompLength+30, 20);
		simMode.setSelectedIndex(0);
		simMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				int selectedModeIndex = simMode.getSelectedIndex();
				int change = simulator.setSimNetType(selectedModeIndex);
				doNotUpdate = true;
				if(change == 0) {
					simMode.setSelectedIndex(0);
				} else if(change == 1) {
					simMode.setSelectedIndex(1);
				} else if(change == 2) {
					simMode.setSelectedIndex(2);
				} else {
					overlord.log("Error while changing graphical simulator mode.", "error", true);
				}
				doNotUpdate = false;
			}
		});
		components.add(simMode);
		
		JLabel timeStepLabel = new JLabel("Time/step:");
		timeStepLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(timeStepLabel);
		
		timeStepLabelValue = new JLabel("0");
		timeStepLabelValue.setBounds(columnA_posX+70, columnB_Y += 20, colACompLength, 20);
		components.add(timeStepLabelValue);
		
		// SIMULATOR CONTROLS
		// metoda startSimulation obiektu simulator troszczy się o wygaszanie
		// i aktywowanie odpowiednich przycisków
		JLabel controlsLabel = new JLabel("Simulation options:");
		controlsLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength*2, 20);
		components.add(controlsLabel);
		columnB_Y += 20;
		
		JButton oneActionBack = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_back.png"));
		oneActionBack.setName("simB1");
		oneActionBack.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 30);
		oneActionBack.setToolTipText("One action back");
		oneActionBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				simulator.startSimulation(SimulatorMode.ACTION_BACK);
				mode = SIMULATOR;
			}
		});
		components.add(oneActionBack);
		
		JButton oneTransitionForward = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_fwd.png"));
		oneTransitionForward.setName("simB2");
		oneTransitionForward.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 30);
		oneTransitionForward.setToolTipText("One transition forward");
		oneTransitionForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION);
				mode = SIMULATOR;
			}
		});
		components.add(oneTransitionForward);
		
		JButton loopBack = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_backLoop.png"));
		loopBack.setName("simB3");
		loopBack.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
		loopBack.setToolTipText("Loop back to oldest saved action");
		loopBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				simulator.startSimulation(SimulatorMode.LOOP_BACK);
				mode = SIMULATOR;
			}
		});
		components.add(loopBack);
		JButton oneStepForward = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_fwdLoop.png"));
		oneStepForward.setName("simB4");
		oneStepForward.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
		oneStepForward.setToolTipText("One step forward");
		oneStepForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				simulator.startSimulation(SimulatorMode.STEP);
				mode = SIMULATOR;
			}
		});
		components.add(oneStepForward);
		
		JButton loopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_loop.png"));
		loopSimulation.setName("simB5");
		loopSimulation.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
		loopSimulation.setToolTipText("Loop simulation");
		loopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				simulator.startSimulation(SimulatorMode.LOOP);
				mode = SIMULATOR;
			}
		});
		components.add(loopSimulation);
		
		JButton singleTransitionLoopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_1transLoop.png"));
		singleTransitionLoopSimulation.setName("simB6");
		singleTransitionLoopSimulation.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
		singleTransitionLoopSimulation.setToolTipText("Loop single transition simulation");
		singleTransitionLoopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
				mode = SIMULATOR;
			}
		});
		components.add(singleTransitionLoopSimulation);
		
		JButton pauseSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_pause.png"));
		pauseSimulation.setName("stop");
		pauseSimulation.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
		pauseSimulation.setToolTipText("Pause simulation");
		pauseSimulation.setEnabled(false);
		pauseSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
				simulator.pause();
				mode = SIMULATOR;
			}
		});
		components.add(pauseSimulation);
		
		JButton stopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_stop.png"));
		stopSimulation.setName("stop");
		stopSimulation.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
		stopSimulation.setToolTipText("Schedule a stop for the simulation");
		stopSimulation.setEnabled(false);
		stopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.stop();
				mode = SIMULATOR;
			}
		});
		components.add(stopSimulation);
		
		JButton resetButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_reset.png"));
		resetButton.setName("reset");
		resetButton.setBounds(columnA_posX, columnB_Y += 30, colACompLength, 30);
		resetButton.setToolTipText("Reset all tokens in places.");
		resetButton.setEnabled(false);
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.getWorkspace().getProject().restoreMarkingZero();
			}
		});
		components.add(resetButton);
		
		JButton saveButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_save_m0.png"));
		saveButton.setName("Save m0");
		saveButton.setBounds(columnB_posX, columnA_Y += 30, colBCompLength, 30);
		saveButton.setToolTipText("Save m0 state.");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(overlord.reset.isSimulatorActiveWarning(
						"Operation impossible while simulator is working.", "Warning") == true)
					return;
				
				Object[] options = {"Save new m0 state", "Cancel",};
				int n = JOptionPane.showOptionDialog(null,
								"Add new net state to states table?",
								"Saving m0 state", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0) {
					overlord.getWorkspace().getProject().accessStatesManager().addCurrentState();
				}
			}
		});
		components.add(saveButton);
		
		JButton statesButton = new JButton("State manager");
		statesButton.setName("State manager");
		statesButton.setBounds(columnA_posX, columnB_Y += 35, colACompLength*2, 30);
		statesButton.setToolTipText("Open states manager window.");
		statesButton.setEnabled(true);
		statesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
					JOptionPane.showMessageDialog(null, "Net simulator must be stopped in order to access state manager.", 
							"Simulator working", JOptionPane.WARNING_MESSAGE);
				} else {
					new HolmesStatesManager();
				}
			}
		});
		components.add(statesButton);
		
		columnB_Y += 35;
		columnA_Y += 35;
		//doNotUpdate = false;
		maximumModeCheckBox = new JCheckBox("Maximum mode");
		maximumModeCheckBox.setBounds(columnA_posX, columnA_Y += 30, 200, 20);
		maximumModeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				if(singleModeCheckBox.isSelected()) {
					JOptionPane.showMessageDialog(null, "Mode overrided by an active single mode.",
						    "Cannot change now", JOptionPane.WARNING_MESSAGE);
					doNotUpdate = true;
					if(overlord.getSettingsManager().getValue("simSingleMode").equals("1")) {
						maximumModeCheckBox.setSelected(true);
					} else {
						maximumModeCheckBox.setSelected(false);
					}
					doNotUpdate = false;
				}

				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					simulator.setMaxMode(true);
				} else {
					simulator.setMaxMode(false);
				}
			}
		});
		components.add(maximumModeCheckBox);
		
		columnB_Y += 20;
		singleModeCheckBox = new JCheckBox("Single mode");
		singleModeCheckBox.setBounds(columnA_posX, columnA_Y += 20, 200, 20);
		singleModeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					simulator.setSingleMode(true);
					doNotUpdate = true;
					if(overlord.getSettingsManager().getValue("simSingleMode").equals("1")) {
						maximumModeCheckBox.setSelected(true);
					} else {
						maximumModeCheckBox.setSelected(false);
					}
					doNotUpdate = false;
				} else {
					simulator.setSingleMode(false);
					
					doNotUpdate = true;
					maximumModeCheckBox.setSelected(false);
					simulator.setMaxMode(false);
					doNotUpdate = false;				
				}
			}
		});
		components.add(singleModeCheckBox);
		
		//PANEL SYMULATORA INWARIANTÓW
		JPanel invariantsSimulatorPanel = new JPanel();
		
		invariantsSimulatorPanel.setLayout(null);
		invariantsSimulatorPanel.setBorder(BorderFactory.createTitledBorder("Invariants simulator"));
		invariantsSimulatorPanel.setBounds(columnA_posX-5, columnA_Y += 20, 160, 160);
		
		int internalXA = 10;
		int internalXB = 60;
		int internalY = 20;
		
		JLabel simTypeLabel = new JLabel("Mode:");
		simTypeLabel.setBounds(internalXA, internalY, 50, 20);
		invariantsSimulatorPanel.add(simTypeLabel);
		
		JRadioButton TimeMode = new JRadioButton("Time Mode");
		TimeMode.setBounds(internalXB, internalY, 90, 20);
		//TimeMode.setLocation(internalXB, internalY);
		internalY+=20;
		TimeMode.setSize(90, 20);
		TimeMode.setActionCommand("0");
		invariantsSimulatorPanel.add(TimeMode);
		group.add(TimeMode);
		
		columnA_Y += 20;
		JRadioButton StepMode = new JRadioButton("Step Mode");
		StepMode.setBounds(internalXB, internalY, 90, 20);
		//StepMode.setLocation(internalXB, internalY);
		internalY+=20;
		StepMode.setSize(90, 20);
		StepMode.setActionCommand("1");
		invariantsSimulatorPanel.add(StepMode);
		group.add(StepMode);
		
		columnA_Y += 20;
		JRadioButton CycleMode = new JRadioButton("Cycle Mode");
		CycleMode.setBounds(internalXB, internalY, 90, 20);
		//CycleMode.setLocation(internalXB, internalY);
		internalY+=20;
		CycleMode.setSize(90, 20);
		CycleMode.setActionCommand("2");
		invariantsSimulatorPanel.add(CycleMode);
		group.add(CycleMode);
		group.setSelected(TimeMode.getModel(), true);
		

		JLabel timeLabel = new JLabel("Time (min):");
		timeLabel.setBounds(internalXA, internalY, 70, 20);
		invariantsSimulatorPanel.add(timeLabel);
		
		SpinnerModel timeCycle = new SpinnerNumberModel(1,1,9999,1);
		spiner = new JSpinner(timeCycle);
		spiner.setLocation(internalXB+20, internalY+3);
		spiner.setSize(70, 20);
		internalY+=25;
		invariantsSimulatorPanel.add(spiner);
		
		// INVARIANTS SIMULATION START BUTTON
		//JButton startButton = new JButton("Start");
		JButton startButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_fwd.png"));
		startButton.setBounds(internalXA, internalY, 80, 30);
		//startButton.setLocation(internalXA, internalY);
		//startButton.setSize(80, 40);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				/*
				if(overlord.getWorkspace().getProject().get2ndFormInvariantsList().size()>0)
				{
					
					mode = INVARIANTSSIMULATOR;
					setEnabledSimulationInitiateButtons(false);
					setEnabledSimulationDisruptButtons(false);
					overlord.getShortcutsBar().setEnabledSimulationInitiateButtons(false);
					overlord.getShortcutsBar().setEnabledSimulationDisruptButtons(false);
					
					try {
						//overlord.startInvariantsSimulation(Integer.valueOf(group.getSelection().getActionCommand()), 
						//		(Integer) spiner.getValue()); //jaki tryb
					} catch (Exception e) {
						e.printStackTrace();
					}
					//STOP:
					setEnabledSimulationInitiateButtons(true);
					setEnabledSimulationDisruptButtons(false);
					overlord.getShortcutsBar().setEnabledSimulationInitiateButtons(true);
					overlord.getShortcutsBar().setEnabledSimulationDisruptButtons(false);
					
				}
				else {
					JOptionPane.showMessageDialog(null, "There are no invariants to simulate.",
						    "Invariant simulator", JOptionPane.INFORMATION_MESSAGE);
				}
				*/
			}
		});
		
		TimeMode.setEnabled(false);
		StepMode.setEnabled(false);
		CycleMode.setEnabled(false);
		spiner.setEnabled(false);
		startButton.setEnabled(false);
		invariantsSimulatorPanel.add(startButton);
		components.add(invariantsSimulatorPanel);
		
		panel.setLayout(null); 
		for (int i = 0; i < components.size(); i++) {
			 panel.add(components.get(i));
		}
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}

	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************     MIEJSCE      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	
	/**
	 * Metoda pomocnicza konstruktora podokna wyświetlającego właściwości klikniętego miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param location ElementLocation - lokalizacja miejsca
	 */
	public void createPlaceSubWindow(Place place, ElementLocation location) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		elementLocation = location;
		initiateContainers();
		mode = PLACE;
		element = place;
		Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
		
		// ID
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		//components.add(idLabel);
		components.add(idLabel);
		
		//int gID = overlord.getWorkspace().getProject().getPlaces().lastIndexOf(place);
		int gID = overlord.getWorkspace().getProject().getPlaces().indexOf(place);
		
		//JLabel idLabel2 = new JLabel(Integer.toString(place.getID()));
		JLabel idLabel2 = new JLabel(Integer.toString(gID));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, 50, 20);
		idLabel2.setFont(normalFont);
		components.add(idLabel2);
		
		JLabel idLabel3 = new JLabel("gID:");
		idLabel3.setBounds(columnB_posX+35, columnA_Y, 50, 20);
		components.add(idLabel3);
		JLabel idLabel4 = new JLabel(place.getID()+"");
		idLabel4.setBounds(columnB_posX+60, columnB_Y, 50, 20);
		idLabel4.setFont(normalFont);
		components.add(idLabel4);

		// NAME
		JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
		nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(nameLabel);

		JFormattedTextField nameField = new JFormattedTextField();
		nameField.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		nameField.setText(place.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeName(newName);
			}
		});
		components.add(nameField);
		
		// KOMENTARZE WIERZCHOŁKA
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);	
		JTextArea commentField = new JTextArea(place.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	String newComment = "";
            	if(field != null)
            		newComment = field.getText();
				changeComment(newComment);
            }
        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
        
		// PLACE TOKEN
        JLabel tokenLabel = new JLabel("Tokens:", JLabel.LEFT);
        tokenLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(tokenLabel);
        int tok = place.getTokensNumber();
        boolean problem = false;
        if(tok < 0) {
        	overlord.log("Negative number of tokens in "+place.getName(), "error", true);
        	tok = 0;
        	problem = true;
        }
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(tok, 0, Integer.MAX_VALUE, 1);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.setBounds(columnB_posX, columnB_Y += 20, 95, 20);
		tokenSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int tokenz = (int) spinner.getValue();
				setTokens(tokenz);
			}
		});
		if(problem)
			tokenSpinner.setEnabled(false);
		components.add(tokenSpinner);

		//SHEET ID
		int sheetIndex = overlord.IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = overlord
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		int xPos = location.getPosition().x;
		int width =  graphPanel.getSize().width;
		int zoom = graphPanel.getZoom();
		int yPos = location.getPosition().y;
		int height =  graphPanel.getSize().height;
		width = (int) (((double)100/(double)zoom) * width);
		height = (int) (((double)100/(double)zoom) * height);
		
		JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
		sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(sheetLabel);
		JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
		sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		sheetIdLabel.setFont(normalFont);
		components.add(sheetIdLabel);
		
		//ZOOM:
		JLabel zoomLabel = new JLabel("Zoom:");
		zoomLabel.setBounds(columnB_posX+30, columnB_Y, 50, 20);
		components.add(zoomLabel);
		JLabel zoomLabel2 = new JLabel(""+zoom);
		zoomLabel2.setBounds(columnB_posX+70, columnB_Y, colBCompLength, 20);
		zoomLabel2.setFont(normalFont);
		if(zoom != 100)
			zoomLabel2.setForeground(Color.red);
		components.add(zoomLabel2);
		
		//LOKALIZACJA:
		JLabel locLabel = new JLabel("Location:", JLabel.LEFT);
		locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(locLabel);
		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);

		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		if(zoom != 100) {
			locationXSpinner.setEnabled(false);
			locationYSpinner.setEnabled(false);
		}
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		
		locationSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, colBCompLength, 20);
		components.add(locationSpinnerPanel);
		
		// PORTAL
		JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
		portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(portalLabel);
		JCheckBox portalBox = new JCheckBox("", place.isPortal());
		portalBox.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		if(((Place)element).isPortal()) 
			portalBox.setSelected(true);
		else
			portalBox.setSelected(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected()) {
					makePortal();
				} else {
					if(((Place)element).getElementLocations().size() > 1)
						JOptionPane.showMessageDialog(null, "Place contains more than one location!", "Cannot proceed", 
								JOptionPane.INFORMATION_MESSAGE);
					else
						unPortal();
				}
			}
		});
		components.add(portalBox);

		// WSPÓŁRZĘDNE NAPISU:
		columnA_Y += 20;
		columnB_Y += 20;
		
		JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
		locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength+10, 20);
		components.add(locNameLabel);

		int locationIndex = place.getElementLocations().indexOf(location);
		int xNameOffset = place.getNamesLocations().get(locationIndex).getPosition().x;
		int yNameOffset = place.getNamesLocations().get(locationIndex).getPosition().y;
		
		nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
		nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);
		
		JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
		locNameLabelX.setBounds(columnA_posX+90, columnA_Y, 40, 20);
		components.add(locNameLabelX);
		
		JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
		nameLocationXSpinner.setBounds(columnA_posX+125, columnA_Y, 60, 20);
		nameLocationXSpinner.addChangeListener(new ChangeListener() {
			private Place place_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				Point res = setNameOffsetX(x, place_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationXSpinnerModel.setValue(res.x);
				doNotUpdate = false;
			}
			private ChangeListener yesWeCan(Place inPlace, ElementLocation inLoc){
				place_tmp = inPlace;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(place, location) ); 
		
		components.add(nameLocationXSpinner);
		
		JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
		locNameLabelY.setBounds(columnA_posX+195, columnB_Y, 40, 20);
		components.add(locNameLabelY);
		
		JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
		nameLocationYSpinner.setBounds(columnA_posX+230, columnA_Y, 60, 20);
		nameLocationYSpinner.addChangeListener(new ChangeListener() {
			private Place place_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				Point res = setNameOffsetY(y, place_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationYSpinnerModel.setValue(res.y);
				doNotUpdate = false;	
			}
			private ChangeListener yesWeCan(Place inPlace, ElementLocation inLoc){
				place_tmp = inPlace;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(place, location) ); 
		components.add(nameLocationYSpinner);
		
		JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
		nameLocChangeButton.setName("LocNameChanger");
		nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
		nameLocChangeButton.setBounds(columnA_posX+90, columnA_Y += 25, 150, 40);
		nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
		nameLocChangeButton.setFocusPainted(false);
		nameLocChangeButton.addActionListener(new ActionListener() {
			// anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
			private Place place_tmp;
			private ElementLocation el_tmp;
			public void actionPerformed(ActionEvent actionEvent) {
				JButton button_tmp = (JButton) actionEvent.getSource();
				
				if(nameLocChangeMode == false) {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
					nameLocChangeMode = true;
					overlord.setNameLocationChangeMode(place_tmp, el_tmp, true);
				} else {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
					nameLocChangeMode = false;
					overlord.setNameLocationChangeMode(null, null, false);
				}
			} 
			private ActionListener yesWeCan(Place inPlace, ElementLocation inLoc){
				place_tmp = inPlace;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(place, location) ); 
		components.add(nameLocChangeButton);
		
		panel.setLayout(null);
		for (JComponent component : components) {
			panel.add(component);
		}
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************    TRANZYCJA     ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda odpowiedzialna za tworzenie podokna właściwości klikniętej tranzycji.
	 * @param transition Transition - obiekt tranzycji sieci
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public void createTransitionSubWindow(Transition transition, ElementLocation location) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;

		mode = TRANSITION;
		elementLocation = location;
		initiateContainers();
		element = transition;
		Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
		
		// ID:
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(idLabel);
		
		int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transition);
		JLabel idLabel2 = new JLabel(Integer.toString(gID));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
		idLabel2.setFont(normalFont);
		components.add(idLabel2);
		
		JLabel idLabel3 = new JLabel("gID:");
		idLabel3.setBounds(columnB_posX+35, columnA_Y, 50, 20);
		components.add(idLabel3);
		JLabel idLabel4 = new JLabel(transition.getID()+"");
		idLabel4.setBounds(columnB_posX+60, columnB_Y, 50, 20);
		idLabel4.setFont(normalFont);
		components.add(idLabel4);

		// TRANSITION NAME:
		JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
		nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(nameLabel);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeName(newName);
			}
		});
		components.add(nameField);
		
		//KOMENTARZE WIERZCHOŁKA:
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);	
		
		JTextArea commentField = new JTextArea(transition.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent e) {
	            	JTextArea field = (JTextArea) e.getSource();
	            	String newComment = "";
	            	if(field != null)
	            		newComment = field.getText();
					changeComment(newComment);
	            }
	        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
		
		//SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		int xPos = location.getPosition().x;
		int width =  graphPanel.getSize().width;
		int zoom = graphPanel.getZoom();
		int yPos = location.getPosition().y;
		int height =  graphPanel.getSize().height;
		width = (int) (((double)100/(double)zoom) * width);
		height = (int) (((double)100/(double)zoom) * height);
		
		JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
		sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(sheetLabel);
		JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
		sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		sheetIdLabel.setFont(normalFont);
		components.add(sheetIdLabel);
		
		//ZOOM:
		JLabel zoomLabel = new JLabel("Zoom:");
		zoomLabel.setBounds(columnB_posX+30, columnB_Y, 50, 20);
		components.add(zoomLabel);
		JLabel zoomLabel2 = new JLabel(""+zoom);
		zoomLabel2.setBounds(columnB_posX+70, columnB_Y, colBCompLength, 20);
		zoomLabel2.setFont(normalFont);
		if(zoom != 100)
			zoomLabel2.setForeground(Color.red);
		components.add(zoomLabel2);
		
		//LOKALIZACJA:
		JLabel locLabel = new JLabel("Location:", JLabel.LEFT);
		locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(locLabel);
		
		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
		
		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		if(zoom != 100) {
			locationXSpinner.setEnabled(false);
			locationYSpinner.setEnabled(false);
		}
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		
		locationSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, colBCompLength, 20);
		components.add(locationSpinnerPanel);
		
		// PORTAL
		JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
		portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(portalLabel);
		JCheckBox portalBox = new JCheckBox("", transition.isPortal());
		portalBox.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		if(((Transition)element).isPortal()) 
			portalBox.setSelected(true);
		else
			portalBox.setSelected(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected()) {
					makePortal();
				} else {
					if(((Transition)element).getElementLocations().size() > 1)
						JOptionPane.showMessageDialog(null, "Transition contains more than one location!", "Cannot proceed", 
								JOptionPane.INFORMATION_MESSAGE);
					else
						unPortal();
				}
			}
		});
		components.add(portalBox);
		
		//FUNKCYJNOŚĆ
		JLabel functionLabel = new JLabel("Functional:", JLabel.LEFT);
		functionLabel.setBounds(columnA_posX, columnA_Y+=20, 80, 20);
		components.add(functionLabel);
		
		JCheckBox functionalCheckBox = new JCheckBox("", transition.isPortal());
		functionalCheckBox.setBounds(columnB_posX, columnB_Y+=20, 150, 20);
		if(((Transition)element).isFunctional())
			functionalCheckBox.setSelected(true);
		else
			functionalCheckBox.setSelected(false);
		
		functionalCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					((Transition)element).setFunctional(true);
				else
					((Transition)element).setFunctional(false);
			}
		});
		components.add(functionalCheckBox);

		// WSPÓŁRZĘDNE NAPISU:
		columnA_Y += 20;
		columnB_Y += 20;
		
		JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
		locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength+10, 20);
		components.add(locNameLabel);

		int locationIndex = transition.getElementLocations().indexOf(location);
		int xNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().x;
		int yNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().y;
		
		nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
		nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);
		
		JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
		locNameLabelX.setBounds(columnA_posX+90, columnA_Y, 40, 20);
		components.add(locNameLabelX);
		
		JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
		nameLocationXSpinner.setBounds(columnA_posX+125, columnA_Y, 60, 20);
		nameLocationXSpinner.addChangeListener(new ChangeListener() {
			private Transition trans_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				Point res = setNameOffsetX(x, trans_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationXSpinnerModel.setValue(res.x);
				doNotUpdate = false;
			}
			private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc){
				trans_tmp = transition;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(transition, location) ); 
		
		components.add(nameLocationXSpinner);
		
		JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
		locNameLabelY.setBounds(columnA_posX+195, columnB_Y, 40, 20);
		components.add(locNameLabelY);
		
		JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
		nameLocationYSpinner.setBounds(columnA_posX+230, columnA_Y, 60, 20);
		nameLocationYSpinner.addChangeListener(new ChangeListener() {
			private Transition trans_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				Point res = setNameOffsetY(y, trans_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationYSpinnerModel.setValue(res.y);
				doNotUpdate = false;	
			}
			private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc){
				trans_tmp = transition;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(transition, location) ); 
		components.add(nameLocationYSpinner);
		
		JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
		nameLocChangeButton.setName("LocNameChanger");
		nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
		nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
		nameLocChangeButton.setBounds(columnA_posX+90, columnA_Y += 25, 150, 40);
		nameLocChangeButton.addActionListener(new ActionListener() {
			// anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
			private Transition trans_tmp;
			private ElementLocation el_tmp;
			public void actionPerformed(ActionEvent actionEvent) {
				JButton button_tmp = (JButton) actionEvent.getSource();
				
				if(nameLocChangeMode == false) {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
					nameLocChangeMode = true;
					overlord.setNameLocationChangeMode(trans_tmp, el_tmp, true);
				} else {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
					nameLocChangeMode = false;
					overlord.setNameLocationChangeMode(null, null, false);
				}
			} 
			private ActionListener yesWeCan(Transition transition, ElementLocation inLoc){
				trans_tmp = transition;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(transition, location) ); 
		components.add(nameLocChangeButton);
		 
		panel.setLayout(null);
		for (JComponent component : components) {
			panel.add(component);
		}

		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}
	
	//**************************************************************************************
	//*********************************    TRANZYCJA     ***********************************
	//*********************************     CZASOWA      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda odpowiedzialna za utworzenie podokna właściwości tranzycji czasowej.
	 * @param transition TimeTransition - obiekt tranzycji czasowej
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public void createTimeTransitionSubWindow(final Transition transition, ElementLocation location) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		
		mode = TIMETRANSITION;
		elementLocation = location;
		initiateContainers(); //!!!
		element = transition;
		Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
		
		// ID
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(idLabel);
		
		int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transition);
		JLabel idLabel2 = new JLabel(Integer.toString(gID));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
		idLabel2.setFont(normalFont);
		components.add(idLabel2);
		
		JLabel idLabel3 = new JLabel("gID:");
		idLabel3.setBounds(columnB_posX+35, columnA_Y, 50, 20);
		components.add(idLabel3);
		JLabel idLabel4 = new JLabel(transition.getID()+"");
		idLabel4.setBounds(columnB_posX+60, columnB_Y, 50, 20);
		idLabel4.setFont(normalFont);
		components.add(idLabel4);

		// T-TRANSITION NAME
		JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
		nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(nameLabel);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeName(newName);
			}
		});
		components.add(nameField);
		
		// T-TRANSITION COMMENT
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);
		
		JTextArea commentField = new JTextArea(transition.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent e) {
	            	JTextArea field = (JTextArea) e.getSource();
	            	String newComment = "";
	            	if(field != null)
	            		newComment = field.getText();
					changeComment(newComment);
	            }
	        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
        
		// EFT / LFT TIMES:
		JLabel minMaxLabel = new JLabel("EFT / LFT:", JLabel.LEFT);
		minMaxLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(minMaxLabel);
		JFormattedTextField minTimeField = new JFormattedTextField();
		minTimeField.setValue(transition.getEFT());
		minTimeField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}						
				double min = (double) field.getValue();
				setMinFireTime(min);
			}
		});

		JFormattedTextField maxTimeField = new JFormattedTextField();
		maxTimeField.setValue(transition.getLFT());
		maxTimeField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}						
				double max = (double) field.getValue();
				setMaxFireTime(max);
			}
		});

		JPanel minTimeSpinnerPanel = new JPanel();
		minTimeSpinnerPanel.setLayout(new BoxLayout(minTimeSpinnerPanel, BoxLayout.X_AXIS));
		minTimeSpinnerPanel.add(minTimeField);
		minTimeSpinnerPanel.add(new JLabel(" / "));
		minTimeSpinnerPanel.add(maxTimeField);
		minTimeSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, 200, 20);
		components.add(minTimeSpinnerPanel);
		
		//DURATION:
		JLabel durationLabel = new JLabel("Duration:", JLabel.LEFT);
		durationLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(durationLabel);
		JFormattedTextField durationField = new JFormattedTextField();
		durationField.setValue(transition.getDPNduration());
		durationField.setBounds(columnA_posX+90, columnB_Y += 20, 90, 20);
		durationField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
					double time = (double) field.getValue();
					setDurationTime(time);
				} catch (Exception ex) {
				}
				
				
			}
		});
		components.add(durationField);
		
		//columnA_Y+=40;
		JCheckBox tpnBox = new JCheckBox("TPN active", transition.getTPNstatus());
		tpnBox.setBounds(columnB_posX-5, columnB_Y += 20, 100, 20);
		tpnBox.setEnabled(true);
		tpnBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					setTPNstatus(true);
				else
					setTPNstatus(false);
			}
		});
		components.add(tpnBox);
		
		columnA_Y+=20;
		JCheckBox dpnBox = new JCheckBox("DPN active", transition.getDPNstatus());
		dpnBox.setBounds(columnB_posX+100, columnB_Y, 100, 20);
		dpnBox.setEnabled(true);
		dpnBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					setDPNstatus(true);
				else
					setDPNstatus(false);
			}
		});
		components.add(dpnBox);

		// T-TRANSITION SHEET ID
		int sheetIndex = overlord.IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = overlord
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		int xPos = location.getPosition().x;
		int width =  graphPanel.getSize().width;
		int zoom = graphPanel.getZoom();
		int yPos = location.getPosition().y;
		int height =  graphPanel.getSize().height;
		width = (int) (((double)100/(double)zoom) * width);
		height = (int) (((double)100/(double)zoom) * height);

		JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
		sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(sheetLabel);
		JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
		sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, 100, 20);
		sheetIdLabel.setFont(normalFont);
		components.add(sheetIdLabel);
		
		JLabel zoomLabel = new JLabel("Zoom:");
		zoomLabel.setBounds(columnB_posX+30, columnB_Y, 50, 20);
		components.add(zoomLabel);
		JLabel zoomLabel2 = new JLabel(""+zoom);
		zoomLabel2.setBounds(columnB_posX+70, columnB_Y, colBCompLength, 20);
		zoomLabel2.setFont(normalFont);
		if(zoom != 100)
			zoomLabel2.setForeground(Color.red);
		components.add(zoomLabel2);
		
		// T-TRANSITION LOCATION:
		JLabel comLabel2 = new JLabel("Location:", JLabel.LEFT);
		comLabel2.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(comLabel2);

		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		if(zoom != 100) {
			locationXSpinner.setEnabled(false);
			locationYSpinner.setEnabled(false);
		}
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		locationSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, 200, 20);
		components.add(locationSpinnerPanel);
		
		// T-TRANSITION PORTAL STATUS
		JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
		portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(portalLabel);

		JCheckBox portalBox = new JCheckBox("", transition.isPortal());
		portalBox.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		if(((Transition)element).isPortal()) 
			portalBox.setSelected(true);
		else
			portalBox.setSelected(false);
		portalBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected()) {
					makePortal();
				} else {
					if(((Transition)element).getElementLocations().size() > 1)
						JOptionPane.showMessageDialog(null, "Transition contains more than one location!", "Cannot proceed", 
								JOptionPane.INFORMATION_MESSAGE);
					else
						unPortal();
				}
			}
		});
		components.add(portalBox);
		
		//FUNKCYJNOŚĆ
		JLabel functionLabel = new JLabel("Functional:", JLabel.LEFT);
		functionLabel.setBounds(columnA_posX, columnA_Y+=20, 80, 20);
		components.add(functionLabel);
		
		JCheckBox functionalCheckBox = new JCheckBox("", transition.isPortal());
		functionalCheckBox.setBounds(columnB_posX, columnB_Y+=20, 150, 20);
		if(((Transition)element).isFunctional())
			functionalCheckBox.setSelected(true);
		else
			functionalCheckBox.setSelected(false);
		
		functionalCheckBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					((Transition)element).setFunctional(true);
				else
					((Transition)element).setFunctional(false);
			}
		});
		components.add(functionalCheckBox);
		
		// WSPÓŁRZĘDNE NAPISU:
		columnA_Y += 20;
		columnB_Y += 20;
		
		JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
		locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength+10, 20);
		components.add(locNameLabel);

		int locationIndex = transition.getElementLocations().indexOf(location);
		int xNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().x;
		int yNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().y;
		
		nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
		nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);
		
		JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
		locNameLabelX.setBounds(columnA_posX+90, columnA_Y, 40, 20);
		components.add(locNameLabelX);
		
		JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
		nameLocationXSpinner.setBounds(columnA_posX+125, columnA_Y, 60, 20);
		nameLocationXSpinner.addChangeListener(new ChangeListener() {
			private Transition trans_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				Point res = setNameOffsetX(x, trans_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationXSpinnerModel.setValue(res.x);
				doNotUpdate = false;
			}
			private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc){
				trans_tmp = transition;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(transition, location) ); 
		
		components.add(nameLocationXSpinner);
		
		JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
		locNameLabelY.setBounds(columnA_posX+195, columnB_Y, 40, 20);
		components.add(locNameLabelY);
		
		JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
		nameLocationYSpinner.setBounds(columnA_posX+230, columnA_Y, 60, 20);
		nameLocationYSpinner.addChangeListener(new ChangeListener() {
			private Transition trans_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				Point res = setNameOffsetY(y, trans_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationYSpinnerModel.setValue(res.y);
				doNotUpdate = false;	
			}
			private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc){
				trans_tmp = transition;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(transition, location) ); 
		components.add(nameLocationYSpinner);
		
		JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
		nameLocChangeButton.setName("LocNameChanger");
		nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
		nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
		nameLocChangeButton.setBounds(columnA_posX+90, columnA_Y += 25, 150, 40);
		nameLocChangeButton.addActionListener(new ActionListener() {
			// anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
			private Transition trans_tmp;
			private ElementLocation el_tmp;
			public void actionPerformed(ActionEvent actionEvent) {
				JButton button_tmp = (JButton) actionEvent.getSource();
				
				if(nameLocChangeMode == false) {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
					nameLocChangeMode = true;
					overlord.setNameLocationChangeMode(trans_tmp, el_tmp, true);
				} else {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
					nameLocChangeMode = false;
					overlord.setNameLocationChangeMode(null, null, false);
				}
			} 
			private ActionListener yesWeCan(Transition transition, ElementLocation inLoc){
				trans_tmp = transition;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(transition, location) ); 
		components.add(nameLocChangeButton);

		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}
	
	//**************************************************************************************
	//*********************************        META      ***********************************
	//*********************************                  ***********************************
	//*********************************       WĘZEŁ      ***********************************
	//**************************************************************************************

	/**
	 * Metoda odpowiedzialna za utworzenie podokna właściwości tranzycji czasowej.
	 * @param transition TimeTransition - obiekt tranzycji czasowej
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public void createMetaNodeSubWindow(final MetaNode metaNode, ElementLocation location) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;

		mode = META;
		elementLocation = location;
		initiateContainers(); //!!!
		element = metaNode;
		Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
		
		// ID
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(idLabel);
		
		int gID = overlord.getWorkspace().getProject().getMetaNodes().lastIndexOf(metaNode);
		JLabel idLabel2 = new JLabel(Integer.toString(gID));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
		idLabel2.setFont(normalFont);
		components.add(idLabel2);
		
		JLabel idLabel3 = new JLabel("gID:");
		idLabel3.setBounds(columnB_posX+35, columnA_Y, 50, 20);
		components.add(idLabel3);
		JLabel idLabel4 = new JLabel(metaNode.getID()+"");
		idLabel4.setBounds(columnB_posX+60, columnB_Y, 50, 20);
		idLabel4.setFont(normalFont);
		components.add(idLabel4);
		
		JLabel sheetRepresentedLabel = new JLabel("Subnet(sheet):");
		sheetRepresentedLabel.setBounds(columnA_posX, columnA_Y+= 20, 95, 20);
		components.add(sheetRepresentedLabel);
		int shID = metaNode.getRepresentedSheetID();
		String text = ""+shID+"";
		text += " ("+overlord.getWorkspace().getIndexOfId(metaNode.getRepresentedSheetID())+")";
		JLabel sheetRepresentedLabelValue = new JLabel(text);
		sheetRepresentedLabelValue.setBounds(columnB_posX, columnB_Y+= 20, 50, 20);
		sheetRepresentedLabelValue.setFont(normalFont);
		components.add(sheetRepresentedLabelValue);

		// META-NODE NAME
		JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
		nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(nameLabel);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		nameField.setValue(metaNode.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeName(newName);
			}
		});
		components.add(nameField);
		
		// T-TRANSITION COMMENT
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);
		
		JTextArea commentField = new JTextArea(metaNode.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent e) {
	            	JTextArea field = (JTextArea) e.getSource();
	            	String newComment = "";
	            	if(field != null)
	            		newComment = field.getText();
					changeComment(newComment);
	            }
	        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
        
        // ZMIANA TYPU META-WĘZŁA
        // ВНИМАНИЕ!!! Hic sunt leones...
		JRadioButton subnetTButton = new JRadioButton("Subnet T-type");
		subnetTButton.setBounds(columnA_posX-5, columnA_Y += 20, 105, 20);
		subnetTButton.setActionCommand("0");
		subnetTButton.addActionListener(new ActionListener() {
			private MetaNode myMeta = null;
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) return;
				boolean status = false;
				if(myMeta.getMetaType() != MetaType.SUBNETTRANS) {
					status = overlord.subnetsHQ.changeSubnetType(myMeta, MetaType.SUBNETTRANS);
				}
				if(status == false) {
					doNotUpdate = true;
					Enumeration<AbstractButton> wtf = groupRadioMetaType.getElements();
					JRadioButton radioB = (JRadioButton) wtf.nextElement(); //first: t-type
					if(myMeta.getMetaType() == MetaType.SUBNETPLACE)
						radioB = (JRadioButton) wtf.nextElement(); //second p-type
					if(myMeta.getMetaType() == MetaType.SUBNET) {
						radioB = (JRadioButton) wtf.nextElement(); //second
						radioB = (JRadioButton) wtf.nextElement(); //third pt-type
					}
					groupRadioMetaType.setSelected(radioB.getModel(), true);
					doNotUpdate = false;
				}
			}
			private ActionListener yesWeCan(MetaNode metaN){
				myMeta = metaN;
		        return this;
		    }
		}.yesWeCan(metaNode) ); 

		groupRadioMetaType.add(subnetTButton);
		components.add(subnetTButton);
		
		
		JRadioButton subnetPButton = new JRadioButton("Subnet P-type");
		subnetPButton.setBounds(columnA_posX+100, columnA_Y, 120, 20);
		subnetPButton.setActionCommand("1");
		subnetPButton.addActionListener(new ActionListener() {
			private MetaNode myMeta = null;
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) return;
				boolean status = false;
				if(myMeta.getMetaType() != MetaType.SUBNETPLACE) {
					status = overlord.subnetsHQ.changeSubnetType(myMeta, MetaType.SUBNETPLACE);
				}
				if(status == false) {
					doNotUpdate = true;
					Enumeration<AbstractButton> wtf = groupRadioMetaType.getElements();
					JRadioButton radioB = (JRadioButton) wtf.nextElement(); //first: t-type
					if(myMeta.getMetaType() == MetaType.SUBNETPLACE)
						radioB = (JRadioButton) wtf.nextElement(); //second p-type
					if(myMeta.getMetaType() == MetaType.SUBNET) {
						radioB = (JRadioButton) wtf.nextElement(); //second
						radioB = (JRadioButton) wtf.nextElement(); //third pt-type
					}
					groupRadioMetaType.setSelected(radioB.getModel(), true);
					doNotUpdate = false;
				}
			}
			private ActionListener yesWeCan(MetaNode metaN){
				myMeta = metaN;
		        return this;
		    }
		}.yesWeCan(metaNode) ); 
		groupRadioMetaType.add(subnetPButton);
		components.add(subnetPButton);
		
		JRadioButton subnetPTButton = new JRadioButton("P & T");
		subnetPTButton.setBounds(columnA_posX+230, columnA_Y, 80, 20);
		subnetPTButton.setActionCommand("2");
		subnetPTButton.addActionListener(new ActionListener() {
			private MetaNode myMeta = null;
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate) return;
				boolean status = false;
				if(myMeta.getMetaType() != MetaType.SUBNET) {
					overlord.subnetsHQ.changeSubnetType(myMeta, MetaType.SUBNET);
				}
				if(status == false) {
					doNotUpdate = true;
					Enumeration<AbstractButton> wtf = groupRadioMetaType.getElements();
					JRadioButton radioB = (JRadioButton) wtf.nextElement(); //first: t-type
					if(myMeta.getMetaType() == MetaType.SUBNETPLACE)
						radioB = (JRadioButton) wtf.nextElement(); //second p-type
					if(myMeta.getMetaType() == MetaType.SUBNET) {
						radioB = (JRadioButton) wtf.nextElement(); //second
						radioB = (JRadioButton) wtf.nextElement(); //third pt-type
					}
					groupRadioMetaType.setSelected(radioB.getModel(), true);
					doNotUpdate = false;
				}
			}
			private ActionListener yesWeCan(MetaNode metaN){
				myMeta = metaN;
		        return this;
		    }
		}.yesWeCan(metaNode) ); 
		groupRadioMetaType.add(subnetPTButton);
		components.add(subnetPTButton);
		
		doNotUpdate = true;
		if(metaNode.getMetaType() == MetaType.SUBNETTRANS)
			groupRadioMetaType.setSelected(subnetTButton.getModel(), true);
		else if(metaNode.getMetaType() == MetaType.SUBNETPLACE)
			groupRadioMetaType.setSelected(subnetPButton.getModel(), true);
		else if(metaNode.getMetaType() == MetaType.SUBNET)
			groupRadioMetaType.setSelected(subnetPTButton.getModel(), true);
        
		doNotUpdate = false;
        columnB_Y += 20;

		// T-TRANSITION SHEET ID
		int sheetIndex = overlord.IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		int xPos = location.getPosition().x;
		int width =  graphPanel.getSize().width;
		int zoom = graphPanel.getZoom();
		int yPos = location.getPosition().y;
		int height =  graphPanel.getSize().height;
		width = (int) (((double)100/(double)zoom) * width);
		height = (int) (((double)100/(double)zoom) * height);

		JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
		sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(sheetLabel);
		JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
		sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, 100, 20);
		sheetIdLabel.setFont(normalFont);
		components.add(sheetIdLabel);
		
		JLabel zoomLabel = new JLabel("Zoom:");
		zoomLabel.setBounds(columnB_posX+30, columnB_Y, 50, 20);
		components.add(zoomLabel);
		JLabel zoomLabel2 = new JLabel(""+zoom);
		zoomLabel2.setBounds(columnB_posX+70, columnB_Y, colBCompLength, 20);
		zoomLabel2.setFont(normalFont);
		if(zoom != 100)
			zoomLabel2.setForeground(Color.red);
		components.add(zoomLabel2);
		
		// T-TRANSITION LOCATION:
		JLabel comLabel2 = new JLabel("Location:", JLabel.LEFT);
		comLabel2.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(comLabel2);

		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		if(zoom != 100) {
			locationXSpinner.setEnabled(false);
			locationYSpinner.setEnabled(false);
		}
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		locationSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, 200, 20);
		components.add(locationSpinnerPanel);
		
		// WSPÓŁRZĘDNE NAPISU:
		columnA_Y += 20;
		columnB_Y += 20;
		
		JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
		locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength+10, 20);
		components.add(locNameLabel);

		int locationIndex = metaNode.getElementLocations().indexOf(location);
		int xNameOffset = metaNode.getNamesLocations().get(locationIndex).getPosition().x;
		int yNameOffset = metaNode.getNamesLocations().get(locationIndex).getPosition().y;
		
		nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
		nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);
		
		JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
		locNameLabelX.setBounds(columnA_posX+90, columnA_Y, 40, 20);
		components.add(locNameLabelX);
		
		JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
		nameLocationXSpinner.setBounds(columnA_posX+125, columnA_Y, 60, 20);
		nameLocationXSpinner.addChangeListener(new ChangeListener() {
			private MetaNode meta_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				Point res = setNameOffsetX(x, meta_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationXSpinnerModel.setValue(res.x);
				doNotUpdate = false;
			}
			private ChangeListener yesWeCan(MetaNode metaN, ElementLocation inLoc){
				meta_tmp = metaN;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(metaNode, location) ); 
		
		components.add(nameLocationXSpinner);
		
		JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
		locNameLabelY.setBounds(columnA_posX+195, columnB_Y, 40, 20);
		components.add(locNameLabelY);
		
		JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
		nameLocationYSpinner.setBounds(columnA_posX+230, columnA_Y, 60, 20);
		nameLocationYSpinner.addChangeListener(new ChangeListener() {
			private MetaNode meta_tmp;
			private ElementLocation el_tmp;
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate==true)
					return;
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				Point res = setNameOffsetY(y, meta_tmp, el_tmp);
				doNotUpdate = true;
				nameLocationYSpinnerModel.setValue(res.y);
				doNotUpdate = false;	
			}
			private ChangeListener yesWeCan(MetaNode metaN, ElementLocation inLoc){
				meta_tmp = metaN;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(metaNode, location) ); 
		components.add(nameLocationYSpinner);
		
		JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
		nameLocChangeButton.setName("LocNameChanger");
		nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
		nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
		nameLocChangeButton.setBounds(columnA_posX+90, columnA_Y += 25, 150, 40);
		nameLocChangeButton.addActionListener(new ActionListener() {
			// anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
			private MetaNode meta_tmp;
			private ElementLocation el_tmp;
			public void actionPerformed(ActionEvent actionEvent) {
				JButton button_tmp = (JButton) actionEvent.getSource();
				
				if(nameLocChangeMode == false) {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
					nameLocChangeMode = true;
					overlord.setNameLocationChangeMode(meta_tmp, el_tmp, true);
				} else {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
					nameLocChangeMode = false;
					overlord.setNameLocationChangeMode(null, null, false);
				}
			} 
			private ActionListener yesWeCan(MetaNode metaN, ElementLocation inLoc){
				meta_tmp = metaN;
				el_tmp = inLoc;
		        return this;
		    }
		}.yesWeCan(metaNode, location) ); 
		components.add(nameLocChangeButton);

		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************       ŁUK        ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	/**
	 * Metoda pomocnicza konstruktora odpowiedzialna za utworzenie podokna właściwości łuku sieci.
	 * @param arc Arc - obiekt łuku
	 */
	public void createArcSubWindow(Arc arc) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		initiateContainers();
		// set mode
		mode = ARC;
		element = arc;
		elementLocation = arc.getStartLocation();
		
		Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
		
		// ARC ID
		JLabel idLabel = new JLabel("gID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(arc.getID()));
		idLabel2.setFont(normalFont);
		idLabel2.setBounds(columnB_posX-10, columnB_Y += 10, colACompLength, 20);
		components.add(idLabel2);
		
		// ARC COMMENT
		JLabel commLabel = new JLabel("Comment:", JLabel.LEFT);
		commLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(commLabel);
		
		JTextArea commentField = new JTextArea(arc.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	String newComment = "";
            	if(field != null)
            		newComment = field.getText();
				changeComment(newComment);
            }
        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX-10, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
		
		// ARC WEIGHT
        JLabel weightLabel = new JLabel("Weight:", JLabel.LEFT);
        weightLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(weightLabel);
		
		SpinnerModel weightSpinnerModel = new SpinnerNumberModel(arc.getWeight(), 0, Integer.MAX_VALUE, 1);
		JSpinner weightSpinner = new JSpinner(weightSpinnerModel);
		weightSpinner.setBounds(columnB_posX-10, columnB_Y += 20, colBCompLength/3, 20);
		weightSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int tokenz = (int) spinner.getValue();
				setWeight(tokenz);
			}
		});
		components.add(weightSpinner);

		// startNode
		columnB_posX+= 30;
		colACompLength += 40;

		JLabel typeArcLabel = new JLabel("Type:", JLabel.LEFT);
		//typeArcLabel.setFont(boldFont);
		typeArcLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(typeArcLabel);
		
		JLabel typeArcLabel2 = new JLabel(arc.getArcType().toString());
		typeArcLabel2.setFont(normalFont);
		typeArcLabel2.setBounds(columnB_posX-40, columnB_Y += 20, colACompLength+40, 20);
		components.add(typeArcLabel2);
		
		JLabel readArcLabel = new JLabel("Read arc:", JLabel.LEFT);
		readArcLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(readArcLabel);
		Arc readArc = arc.getPairedArc();
		
		String txt = "no";
		if(readArc != null) {
			txt = "yes [paired arc ID: "+readArc.getID()+"]";
		} else {
			if(InvariantsTools.isDoubleArc(arc) == true) {
				txt = "double arc (hidden readarc)";
			}
		}
		
		JLabel readArcLabel2 = new JLabel(txt);
		readArcLabel2.setFont(normalFont);
		readArcLabel2.setBounds(columnB_posX-40, columnB_Y += 20, colACompLength+60, 20);
		components.add(readArcLabel2);
		
		JLabel startNodeLabel = new JLabel("Start Node:", JLabel.LEFT);
		startNodeLabel.setBounds(columnA_posX+90, columnA_Y += 20, colACompLength, 20);
		components.add(startNodeLabel);
		columnB_Y += 20;
		
		JLabel label2A = new JLabel("Name:", JLabel.LEFT);
		label2A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label2A);
		JLabel label2B = new JLabel(arc.getStartNode().getName());
		label2B.setFont(normalFont);
		label2B.setBounds(columnA_posX+40, columnB_Y += 20, colBCompLength+40, 20);
		components.add(label2B);
		
		JLabel label1A = new JLabel("gID:", JLabel.LEFT);
		label1A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label1A);
		JLabel label1B = new JLabel(Integer.toString(arc.getStartNode().getID()));
		label1B.setFont(normalFont);
		label1B.setBounds(columnA_posX+40, columnB_Y += 20, 50, 20);
		components.add(label1B);
		
		JLabel label3A = new JLabel("Sheet:", JLabel.LEFT);
		label3A.setBounds(columnA_posX+80, columnA_Y, colACompLength, 20);
		components.add(label3A);
		JLabel label3B = new JLabel(Integer.toString(arc.getStartLocation().getSheetID()));
		label3B.setFont(normalFont);
		label3B.setBounds(columnA_posX+120, columnB_Y, 40, 20);
		components.add(label3B);
		
		JLabel label4A = new JLabel("Location:", JLabel.LEFT);
		label4A.setBounds(columnA_posX+150, columnA_Y, colACompLength, 20);
		components.add(label4A);
		JLabel label4B = new JLabel(Integer.toString(arc.getStartLocation().getPosition().x)+ ", "
				+ Integer.toString(arc.getStartLocation().getPosition().y));
		label4B.setBounds(columnA_posX+210, columnB_Y, colBCompLength, 20);
		label4B.setFont(normalFont);
		components.add(label4B);
		
		// endNode
		JLabel endNodeLabel = new JLabel("End Node:", JLabel.LEFT);
		endNodeLabel.setBounds(columnA_posX+90, columnA_Y += 20, colACompLength, 20);
		components.add(endNodeLabel);
		columnB_Y += 20;

		JLabel label6A = new JLabel("Name:", JLabel.LEFT);
		label6A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label6A);
		JLabel label6B = new JLabel(arc.getEndNode().getName());
		label6B.setFont(normalFont);
		label6B.setBounds(columnA_posX+40, columnB_Y += 20, colBCompLength+40, 20);
		components.add(label6B);
		
		JLabel label5A = new JLabel("gID:", JLabel.LEFT);
		label5A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label5A);
		JLabel label5B = new JLabel(Integer.toString(arc.getEndNode().getID()));
		label5B.setFont(normalFont);
		label5B.setBounds(columnA_posX+40, columnB_Y += 20, colBCompLength, 20);
		components.add(label5B);
		
		JLabel label7A = new JLabel("Sheet:", JLabel.LEFT);
		label7A.setBounds(columnA_posX+80, columnA_Y, colACompLength, 20);
		components.add(label7A);
		JLabel label7B = new JLabel(Integer.toString(arc.getEndLocation().getSheetID()));
		label7B.setFont(normalFont);
		label7B.setBounds(columnA_posX+120, columnB_Y, 40, 20);
		components.add(label7B);
		
		JLabel label8A = new JLabel("Location:", JLabel.LEFT);
		label8A.setBounds(columnA_posX+150, columnA_Y, colACompLength, 20);
		components.add(label8A);
		JLabel label8B = new JLabel(Integer.toString(arc.getEndLocation().getPosition().x)+ ", "
				+ Integer.toString(arc.getEndLocation().getPosition().y));
		label8B.setFont(normalFont);
		label8B.setBounds(columnA_posX+210, columnB_Y, colBCompLength, 20);
		components.add(label8B);
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	//**************************************************************************************
	//*********************************      ARKUSZ      ***********************************
	//*********************************                  ***********************************
	//*********************************       SHEET      ***********************************
	//**************************************************************************************
	
	/**
	 * Metoda pomocnicza konstruktora odpowiedzialna za utworzenia podokna właściwości arkusza sieci. 
	 * @param sheet WorkspaceSheet - obiekt arkusza
	 */
	public void createSheetSubWindow(WorkspaceSheet sheet) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		
		initiateContainers();
		mode = SHEET;
		currentSheet = sheet;

		// SHEET ID
		JLabel netNameLabel = new JLabel("PN Name:", JLabel.LEFT);
		netNameLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(netNameLabel);

		JFormattedTextField netNameField = new JFormattedTextField();
		netNameField.setBounds(columnB_posX, columnB_Y += 10, colBCompLength, 20);
		netNameField.setText(overlord.getWorkspace().getProject().getName());
		netNameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				
				overlord.getWorkspace().getProject().setName(newName);
			}
		});
		components.add(netNameField);
		
		
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(idLabel);
		String text = ""+Integer.toString(sheet.getId());
		int shPos = overlord.getWorkspace().getIndexOfId(sheet.getId());
		text += " (sheet position: "+shPos+")";
		JLabel idLabel2 = new JLabel(text);
		idLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength+150, 20);
		components.add(idLabel2);
		
		// SHEET ZOOM
		int zoom = sheet.getGraphPanel().getZoom();
		//
		Dimension x = sheet.getGraphPanel().getOriginSize();
		int widthOrg = 0;
		int heightOrg = 0;
		if(x != null) {
			widthOrg = (int) x.getWidth();
			heightOrg =  (int) x.getHeight();
		} else {
			widthOrg = sheet.getGraphPanel().getSize().width;
			heightOrg =  sheet.getGraphPanel().getSize().height;
		}
		
		//widthOrg = (int) (((double)100/(double)zoom) * widthOrg);
		//heightOrg = (int) (((double)100/(double)zoom) * heightOrg);
		
		JLabel zoomLabel1 = new JLabel("Zoom:", JLabel.LEFT);
		zoomLabel1.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(zoomLabel1);
		JLabel zoomLabel2 = new JLabel(Integer.toString(zoom)+"%");
		zoomLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		if(zoom != 100)
			zoomLabel2.setForeground(Color.red);
		components.add(zoomLabel2);
		
		// SHEET SIZE
		JLabel widthLabel = new JLabel("Width:", JLabel.LEFT);
		widthLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(widthLabel);
		
		SpinnerModel widthSpinnerModel = new SpinnerNumberModel(sheet.getGraphPanel().getSize().width, 0, Integer.MAX_VALUE, 1);
		JSpinner widthSpinner = new JSpinner(widthSpinnerModel);
		widthSpinner.setBounds(columnB_posX, columnB_Y += 20, colBCompLength/2, 20);
		widthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int width = (int) spinner.getValue();
				setSheetWidth(width);
			}
		});
		components.add(widthSpinner);
		
		JLabel widthLabel2 = new JLabel(Integer.toString(widthOrg), JLabel.LEFT);
		widthLabel2.setBounds(columnB_posX+110, columnA_Y, colACompLength, 20);
		components.add(widthLabel2);
		JLabel widthLabel3 = new JLabel("(orig.)", JLabel.LEFT);
		widthLabel3.setBounds(columnB_posX+150, columnA_Y, colACompLength, 20);
		components.add(widthLabel3);
		
		// SHEET HEIGHT
		JLabel heightLabel = new JLabel("Height:", JLabel.LEFT);
		heightLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(heightLabel);
		
		SpinnerModel heightSpinnerModel = new SpinnerNumberModel(sheet.getGraphPanel().getSize().height, 0, Integer.MAX_VALUE, 1);
		JSpinner heightSpinner = new JSpinner(heightSpinnerModel);
		heightSpinner.setBounds(columnB_posX, columnB_Y += 20, colBCompLength/2, 20);
		heightSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int height = (int) spinner.getValue();
				setSheetHeight(height);
			}
		});
		components.add(heightSpinner);
		if(zoom != 100) {
			widthSpinner.setEnabled(false);
			heightSpinner.setEnabled(false);
		}
		
		JLabel heightLabel2 = new JLabel(Integer.toString(heightOrg), JLabel.LEFT);
		heightLabel2.setBounds(columnB_posX+110, columnB_Y, colACompLength, 20);
		components.add(heightLabel2);
		JLabel heightLabel3 = new JLabel("(orig.)", JLabel.LEFT);
		heightLabel3.setBounds(columnB_posX+150, columnB_Y, colACompLength, 20);
		components.add(heightLabel3);
		
		// is auto scroll when dragging automatic
		JLabel autoSrclLabel = new JLabel("Autoscroll:", JLabel.LEFT);
		autoSrclLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(autoSrclLabel);
		
		JCheckBox autoscrollBox = new JCheckBox("", sheet.getGraphPanel().isAutoDragScroll());
		autoscrollBox.setBounds(columnB_posX-4, columnB_Y, colACompLength, 20);
		autoscrollBox.setLocation(columnB_posX-4, columnB_Y += 20);
		autoscrollBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					setAutoscroll(true);
				else
					setAutoscroll(false);
			}
		});
		components.add(autoscrollBox);
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************    INWARIANTY    ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	
	/**
	 * Metoda pomocnicza konstruktora odpowiedzialna za wypełnienie podokna informacji o inwariantach sieci.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 */
	public void createInvariantsSubWindow(ArrayList<ArrayList<Integer>> invariantsData) {
		doNotUpdate = true;
		if(invariantsData == null || invariantsData.size() == 0) {
			return;
		} else {
			mode = INVARIANTS;
			invariantsMatrix = invariantsData;
			transitions = overlord.getWorkspace().getProject().getTransitions();
			overlord.reset.setInvariantsStatus(true);
		}
		
		int colA_posX = 10;
		int colB_posX = 100;
		int positionY = 10;
		initiateContainers();

		JLabel chooseInvLabel = new JLabel("Invariant: ");
		chooseInvLabel.setBounds(colA_posX, positionY, 80, 20);
		components.add(chooseInvLabel);
		
		String[] invariantHeaders = new String[invariantsMatrix.size() + 3];
		invariantHeaders[0] = "---";
		for (int i = 0; i < invariantsMatrix.size(); i++) {
			int invSize = InvariantsTools.getSupport(invariantsMatrix.get(i)).size();
			invariantHeaders[i + 1] = "Inv. #" + (i+1) +" (size: "+invSize+")";
		}
		invariantHeaders[invariantHeaders.length-2] = "null transitions";
		invariantHeaders[invariantHeaders.length-1] = "inv/trans frequency";
		
		JComboBox<String> chooseInvBox = new JComboBox<String>(invariantHeaders);
		chooseInvBox.setBounds(colB_posX, positionY, 150, 20);
		chooseInvBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				int items = comboBox.getItemCount();
				if (comboBox.getSelectedIndex() == 0) {
					selectedInvIndex = -1;
					showInvariant(); //clean
				} else if(comboBox.getSelectedIndex() == items-2) { 
					selectedInvIndex = -1;
					showDeadInv(); //show transition without invariants
				} else if(comboBox.getSelectedIndex() == items-1) { 
					selectedInvIndex = -1;
					showInvTransFrequency(); //show transition frequency (in invariants)
				} else {
					selectedInvIndex = comboBox.getSelectedIndex() - 1;
					showInvariant();
				}
			}
		});
		components.add(chooseInvBox);
		
		JButton showDetailsButton = new JButton();
		showDetailsButton.setText("Show details");
		showDetailsButton.setBounds(colA_posX, positionY+=30, 120, 30);
		showDetailsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showInvariantNotepad();
			}
		});
		components.add(showDetailsButton);

		JCheckBox markMCTcheckBox = new JCheckBox("Color MCT");
		markMCTcheckBox.setBounds(colA_posX+130, positionY-5, 120, 20);
		markMCTcheckBox.setSelected(false);;
		markMCTcheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					markMCT = true;
				} else {
					markMCT = false;
				}
				showInvariant();
			}
		});
		components.add(markMCTcheckBox);
		
		JCheckBox glowINVcheckBox = new JCheckBox("Transitions glow");
		glowINVcheckBox.setBounds(colA_posX+130, positionY+15, 120, 20);
		glowINVcheckBox.setSelected(true);;
		glowINVcheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					glowInv = true;
				} else {
					glowInv = false;
				}
				showInvariant();
			}
		});
		components.add(glowINVcheckBox);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		invNameField = new JFormattedTextField(format);
		invNameField.setBounds(colA_posX, positionY += 40, 250, 20);
		invNameField.setValue("");
		invNameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeInvName(newName);
			}
		});
		components.add(invNameField);
		
		doNotUpdate = false;
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}
	
	/**
	 * Zmiana nazwy inwariantu.
	 * @param newName String - nowa nazwa
	 */
	protected void changeInvName(String newName) {
		if(selectedInvIndex == -1)
			return;

		overlord.getWorkspace().getProject().accessINVnames().set(selectedInvIndex, newName);
	}

	/**
	 * Metoda wywołuje okno notatnika z danymi o inwariancie.
	 * @param selectedInvIndex2 int - indeks wybranego z listy
	 */
	protected void showInvariantNotepad() {
		if(selectedInvIndex == -1)
			return;
		
		HolmesNotepad note = new HolmesNotepad(640, 480);
		ArrayList<Integer> invariant = invariantsMatrix.get(selectedInvIndex);
		
		//MCT:
		ArrayList<Integer> mcts = new ArrayList<Integer>();
		ArrayList<String> singleT = new ArrayList<String>();
		ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();
		int transNumber = 0;
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			if(fireValue == 0)
				continue;
			
			transNumber++;
			int mctNo = transMCTvector.get(t);
			if(mctNo == -1) { 
				singleT.add("T"+t+"_"+transitions.get(t).getName());
			} else {
				if(!mcts.contains(mctNo)) {
					mcts.add(mctNo);
				}
			}
		}
		Collections.sort(mcts);
		String name = overlord.getWorkspace().getProject().accessINVnames().get(selectedInvIndex);
		note.addTextLineNL("Invariant "+(selectedInvIndex+1)+": "+name, "text");
		note.addTextLineNL("Total number of transitions: "+transNumber, "text");
		note.addTextLineNL("Support structure:", "text");
		for(int mct : mcts) {
			String MCTname = overlord.getWorkspace().getProject().getMCTname(mct);
			note.addTextLineNL("  [MCT: "+(mct+1)+"]: "+MCTname, "text");
		}
		for(String transName : singleT)
			note.addTextLineNL(transName, "text");
		//END OF STRUCTURE BLOCK
		
		note.addTextLineNL("", "text");
		note.addTextLineNL("All transitions of INV #" + (selectedInvIndex+1)+":", "text");
		
		if(transitions.size() != invariant.size()) {
			transitions = overlord.getWorkspace().getProject().getTransitions();
			if(transitions == null || transitions.size() != invariant.size()) {
				overlord.log("Critical error in invariants subwindow. "
						+ "Invariants support size refers to non-existing transitions.", "error", true);
				return;
			}
		}
		
		String vector = "";
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			vector += fireValue+";";
			if(fireValue == 0)
				continue;
			
			Transition realT = transitions.get(t);
			String t1 = Tools.setToSize("t"+t, 5, false);
			String t2 = Tools.setToSize("Fired: "+fireValue, 12, false);
			note.addTextLineNL(t1 + t2 + " ; "+realT.getName(), "text");
		}
		vector = vector.substring(0, vector.length()-1);
		note.addTextLineNL("", "text");
		note.addTextLineNL("Invariant vector:", "text");
		note.addTextLineNL(vector, "text");

		note.setCaretFirstLine();
		note.setVisible(true);
	}

	/**
	 * Metoda odpowiedzialna za podświetlanie inwariantów na rysunku sieci.
	 */
	private void showInvariant() {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();
		
		if(selectedInvIndex != -1)
		{
			ArrayList<Integer> invariant = invariantsMatrix.get(selectedInvIndex);
			if(transitions.size() != invariant.size()) {
				transitions = overlord.getWorkspace().getProject().getTransitions();
				if(transitions == null || transitions.size() != invariant.size()) {
					overlord.log("Critical error in invariants subwindow. "
							+ "Invariants size differ from transition set cardinality!", "error", true);
					return;
				}
			}
			
			ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();
			ColorPalette cp = new ColorPalette();
			for(int t=0; t<invariant.size(); t++) {
				int fireValue = invariant.get(t);
				if(fireValue == 0)
					continue;

				if(markMCT) {
					int mctNo = transMCTvector.get(t);
					if(mctNo == -1) {
						transitions.get(t).setGlowedINV(glowInv, fireValue);
					} else {
						transitions.get(t).setColorWithNumber(true, cp.getColor(mctNo), false, fireValue, true, "[MCT"+(mctNo+1)+"]");
						transitions.get(t).setGlowedINV(false, fireValue);
					}	
				} else {
					transitions.get(t).setGlowedINV(glowInv, fireValue);
				}
			}
			//name field:
			String name = overlord.getWorkspace().getProject().accessINVnames().get(selectedInvIndex);
			invNameField.setValue(name);
			
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda pokazująca w ilu inwariantach występuje każda tranzycja
	 */
	private void showInvTransFrequency() {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();
		
		ArrayList<Integer> freqVector = InvariantsTools.getFrequency(invariantsMatrix);
		ArrayList<Transition> transitions_tmp = overlord.getWorkspace().getProject().getTransitions();
		
		if(freqVector == null) {
			JOptionPane.showMessageDialog(null, "No invariants data available.", "No invariants", JOptionPane.INFORMATION_MESSAGE);
		} else {
			for(int i=0; i<freqVector.size(); i++) {
				Transition realT = transitions_tmp.get(i);
				
				if(freqVector.get(i)!=0) {
					realT.setGlowedINV(glowInv, freqVector.get(i));
				} else
					realT.setColorWithNumber(true, Color.red, true, 0, false, "");
			}
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda pomocnicza do zaznaczania tranzycji nie pokrytych inwariantami.
	 */
	private void showDeadInv() {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();
		
		HolmesNotepad note = new HolmesNotepad(640, 480);
		note.addTextLineNL("Transitions not covered by invariants:", "text");

		ArrayList<Integer> deadTrans = InvariantsTools.detectUncovered(invariantsMatrix);
		ArrayList<Transition> transitions_tmp = overlord.getWorkspace().getProject().getTransitions();
		int counter = 0;
		if(deadTrans == null) {
			JOptionPane.showMessageDialog(null, "No invariants data available.", "No invariants", JOptionPane.INFORMATION_MESSAGE);
		} else {
			for(int i=0; i<deadTrans.size(); i++) {
				int deadOne = deadTrans.get(i);
				Transition realT = transitions_tmp.get(deadOne);
				String t1 = Tools.setToSize("t"+deadOne, 5, false);
				note.addTextLineNL(t1 + " | "+realT.getName(), "text");
				realT.setGlowedINV(true, 0);
				counter++;
			}
		}
		if(counter > 0) {
			note.setCaretFirstLine();
			note.setVisible(true);
		} else {
			note.dispose();
			note = null;
		}
		
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}

	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************       MCT        ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda pomocnicza konstruktora odpowiedzialna za utworzenie podokna wyboru zbiorów MCT.
	 * @param mct ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
	 * @param type Properties.PropertiesType - nic nie znaczący tutaj element...
	 */
	@SuppressWarnings("unchecked")
	public void createMCTSubWindow(ArrayList<ArrayList<Transition>> mct) {
		if(mct == null || mct.size() == 0) {
			return;
			//błędne wywołanie
		} else {
			mode = MCT;
			overlord.reset.setMCTStatus(true);
		}
		doNotUpdate = true;
		
		int colA_posX = 10;
		int colB_posX = 100;
		int positionY = 10;
		
		initiateContainers();
		this.mctGroups = mct;
		
		String[] mctHeaders = new String[mctGroups.size() + 2];
		mctHeaders[0] = "---";
		for (int i = 0; i < mctGroups.size(); i++) {
			if(i < mctGroups.size()-1)
				mctHeaders[i + 1] = "MCT #" + Integer.toString(i+1) +" (size: "+mctGroups.get(i).size()+")";
			else {
				mctHeaders[i + 1] = "No-MCT transitions";
				mctHeaders[i + 2] = "Show all";
			}
		}
				
		// getting the data
		JLabel chooseMctLabel = new JLabel("Choose MCT: ");
		chooseMctLabel.setBounds(colA_posX, positionY, 80, 20);
		components.add(chooseMctLabel);

		JComboBox<String> chooseMctBox = new JComboBox<String>(mctHeaders);
		chooseMctBox.setBounds(colB_posX, positionY, 150, 20);
		chooseMctBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				int selected = comboBox.getSelectedIndex();
				if (selected == 0) {
					selectedMCTindex = -1;
					allMCTselected = false;
					showMct();
				} else if(selected == comboBox.getItemCount()-1) {
					allMCTselected = true;
					showAllColors();
				} else {
					selectedMCTindex = selected - 1;
					allMCTselected = false;
					showMct();
				}
			}
		});
		components.add(chooseMctBox);

		JButton showDetailsButton = new JButton();
		showDetailsButton.setText("Show details");
		showDetailsButton.setBounds(colA_posX, positionY+=30, 120, 30);
		showDetailsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showMCTNotepad();
			}
		});
		components.add(showDetailsButton);

		JCheckBox glowCheckBox = new JCheckBox("Different colors");
		glowCheckBox.setBounds(colA_posX+130, positionY-5, 120, 20);
		glowCheckBox.setSelected(false);
		glowCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					colorMCT = true;
				} else {
					colorMCT = false;
				}
				if(allMCTselected)
					showAllColors();
				else
					showMct();
			}
		});
		components.add(glowCheckBox);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
	    MCTnameField = new JFormattedTextField(format);
	    MCTnameField.setBounds(colA_posX, positionY += 40, 250, 20);
	    MCTnameField.setValue("");
	    MCTnameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if(doNotUpdate)
					return;
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeMCTname(newName);
			}
		});
		components.add(MCTnameField);

		doNotUpdate = false;
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}
	
	/**
	 * Metoda zmiany nazwy zbioru MCT.
	 * @param newName String - nowa nazwa
	 */
	protected void changeMCTname(String newName) {
		if(selectedMCTindex == -1)
			return;

		overlord.getWorkspace().getProject().accessMCTnames().set(selectedMCTindex, newName);
	}

	/**
	 * Metoda pokazująca za pomocą notatnika dane o zbiorze MCT.
	 */
	protected void showMCTNotepad() {
		if(selectedMCTindex == -1)
			return;
		
		HolmesNotepad note = new HolmesNotepad(640, 480);

		ArrayList<Transition> mct = mctGroups.get(selectedMCTindex);
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		int size = mct.size();
		if(selectedMCTindex == mctGroups.size()-1) {
			note.addTextLineNL("Trivial MCT-transitions ("+size+"):", "text");
		} else {
			note.addTextLineNL("Transitions ("+size+") of MCT #"+(selectedMCTindex+1), "text");
		}
		
		for(Transition transition : mct) {
			int globalIndex = transitions.lastIndexOf(transition);
			String t1 = Tools.setToSize("t"+globalIndex, 5, false);
			note.addTextLineNL("T"+t1 + "_"+transition.getName(), "text");
		}
		
		note.setCaretFirstLine();
		note.setVisible(true);
	}

	/**
	 * Metoda odpowiedzialna za pokazanie szczegółów wybranego zbioru MCT.
	 * @param mctIndex Integer - numer wybranego zbioru
	 * @param isThereMCT boolean - true, jeśli wybrano zbiór mct, false jeśli "---"
	 */
	private void showMct() {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();
		
		if(selectedMCTindex == -1)
			return;
		
		ArrayList<Transition> mct = mctGroups.get(selectedMCTindex);
		int size = mctGroups.size();
		ColorPalette cp = new ColorPalette();
		for (Transition transition : mct) {
			if(!colorMCT) {
				transition.setGlowed_MTC(true);
			} else {
				if(selectedMCTindex == size - 1) 
					transition.setColorWithNumber(true, cp.getColor(selectedMCTindex), false, 0, true, "[trivial]");
				else
					transition.setColorWithNumber(true, cp.getColor(selectedMCTindex), false, 0, true, "[MCT"+(selectedMCTindex+1)+"]");
			}
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();

		//name field:
		String name = overlord.getWorkspace().getProject().accessMCTnames().get(selectedMCTindex);
		MCTnameField.setValue(name);
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie wszystkich nietrywalniach zbiorów MCT w kolorach.
	 */
	private void showAllColors() {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();

		ColorPalette cp = new ColorPalette();
		for(int m=0; m<mctGroups.size()-1; m++) {
			Color currentColor = cp.getColor();
			ArrayList<Transition> mct = mctGroups.get(m);
			for (Transition transition : mct) {
				if(overlord.getSettingsManager().getValue("mctNameShow").equals("1"))
					transition.setColorWithNumber(true, currentColor, false, m, true, "MCT #"+(m+1)+" ("+mct.size()+")");
				else
					transition.setColorWithNumber(true, currentColor, false, m, true, "");
			}
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************     KLASTRY      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Metoda pomocnicza konstruktora tworząca podokno danych o klastrach.
	 * @param windowType int - w zależności od tego, tworzy dane okno
	 */
	public void createClustersSubWindow(ClusterDataPackage clusteringData) {
		initiateContainers();
		doNotUpdate = true;
		if(clusteringData == null || clusteringData.dataMatrix.size() == 0) {
			return;
		} else {
			mode = CLUSTERS;
			clusterColorsData = clusteringData;
			overlord.reset.setClustersStatus(true);
		}
		
		int colA_posX = 10;
		int colB_posX = 100;
		int positionY = 10;
		initiateContainers();
		
		JLabel label1 = new JLabel("Algorithm: ");
		label1.setBounds(colA_posX, positionY, 80, 20);
		components.add(label1);
		JLabel label2 = new JLabel(clusterColorsData.algorithm);
		label2.setBounds(colB_posX, positionY, 80, 20);
		components.add(label2);
		positionY += 20;
		JLabel label3 = new JLabel("Metric: ");
		label3.setBounds(colA_posX, positionY, 80, 20);
		components.add(label3);
		JLabel label4 = new JLabel(clusterColorsData.metric);
		label4.setBounds(colB_posX, positionY, 80, 20);
		components.add(label4);
		positionY += 20;
		JLabel label5 = new JLabel("Clusters: ");
		label5.setBounds(colA_posX, positionY, 80, 20);
		components.add(label5);
		JLabel label6 = new JLabel(clusterColorsData.clNumber+"");
		label6.setBounds(colB_posX, positionY, 80, 20);
		components.add(label6);

		JLabel chooseInvLabel = new JLabel("Selected: ");
		chooseInvLabel.setBounds(colA_posX, positionY += 20, 80, 20);
		components.add(chooseInvLabel);
		
		// PRZEWIJALNA LISTA KLASTRÓW:
		String[] clustersHeaders = new String[clusterColorsData.dataMatrix.size() + 1];
		clustersHeaders[0] = "---";
		for (int i = 0; i < clusterColorsData.dataMatrix.size(); i++) {
			clustersHeaders[i + 1] = "Cluster " + Integer.toString(i+1) 
					+ " (size: "+clusterColorsData.clSize.get(i)+" inv.)";
		}
		
		chooseCluster = new JComboBox<String>(clustersHeaders);
		chooseCluster.setBounds(colB_posX, positionY, 180, 20);
		chooseCluster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				if (comboBox.getSelectedIndex() == 0) {
					selectedClusterIndex = -1;
					showClusters();
					fillClustInvCombo();
				} else {
					selectedClusterIndex = comboBox.getSelectedIndex() - 1;
					showClusters();
					fillClustInvCombo();
				}
			}
		});
		components.add(chooseCluster);
		
		JLabel mssLabel1 = new JLabel("MSS value:");
		mssLabel1.setBounds(colA_posX, positionY += 20, 80, 20);
		components.add(mssLabel1);

		mssValueLabel = new JLabel("n/a");
		mssValueLabel.setBounds(colB_posX, positionY, 80, 20);
		components.add(mssValueLabel);
		
		//SPOSÓB WYŚWIETLANIA - TRANZYCJE CZY ODPALENIA
		JCheckBox transFiringMode = new JCheckBox("Show transition average firing");
		transFiringMode.setBounds(colA_posX-3, positionY+=20, 220, 20);
		transFiringMode.setSelected(false);;
		transFiringMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					clusterColorsData.showFirings = true;
				} else {
					clusterColorsData.showFirings = false;
				}
				int selected = chooseCluster.getSelectedIndex();
				chooseCluster.setSelectedIndex(selected);
				//chooseCluster.setSelectedItem(selected);
			}
		});
		components.add(transFiringMode);

		JCheckBox scaleMode = new JCheckBox("Show scaled colors");
		scaleMode.setBounds(colA_posX-3, positionY+=20, 170, 20);
		scaleMode.setSelected(false);;
		scaleMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					clusterColorsData.showScale = true;
				} else {
					clusterColorsData.showScale = false;
				}
				int selected = chooseCluster.getSelectedIndex();
				chooseCluster.setSelectedIndex(selected);
				//chooseCluster.setSelectedItem(selected);
			}
		});
		components.add(scaleMode);
		
		JCheckBox mctMode = new JCheckBox("Show MCT sets");
		mctMode.setBounds(colA_posX-3, positionY+=20, 120, 20);
		mctMode.setSelected(false);
		mctMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					clustersMCT = true;
				} else {
					clustersMCT = false;
				}
				int selected = chooseCluster.getSelectedIndex();
				chooseCluster.setSelectedIndex(selected);
			}
		});
		components.add(mctMode);

		JButton showDetailsButton = new JButton();
		showDetailsButton.setText("Show details");
		showDetailsButton.setBounds(colA_posX, positionY+=30, 130, 30);
		showDetailsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showClustersNotepad();
			}
		});
		components.add(showDetailsButton);
		
		JButton screenshotsButton = new JButton();
		screenshotsButton.setText("Export pictures");
		screenshotsButton.setBounds(colA_posX+135, positionY, 130, 30);
		screenshotsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				dropClustersToFiles();
			}
		});
		components.add(screenshotsButton);
		
		progressBar = new JProgressBar();
		progressBar.setBounds(colA_posX+135, positionY-5, 130, 35);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Completed");
	    progressBar.setBorder(border);
	    progressBar.setVisible(false);
	    progressBar.setForeground(Color.RED);
	    components.add(progressBar);
	    
	    //inwarianty w ramach klastra:
	    JLabel chooseClustInvLabel = new JLabel("Cluster inv.:");
	    chooseClustInvLabel.setBounds(colA_posX, positionY += 40, 80, 20);
		components.add(chooseClustInvLabel);
		
		String[] clustersInvHeaders = new String[1];
		clustersInvHeaders[0] = "---";
	    chooseClusterInv = new JComboBox<String>(clustersInvHeaders);
	    chooseClusterInv.setBounds(colB_posX, positionY, 180, 20);
	    chooseClusterInv.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				if (comboBox.getSelectedIndex() == 0) {
					selectedClusterInvIndex = -1;
					showClusters();
				} else {
					selectedClusterInvIndex = comboBox.getSelectedIndex() - 1;
					showClusterInv();
				}
			}
		});
		components.add(chooseClusterInv);
		
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		doNotUpdate = true;
		add(panel);
	}
	
	/**
	 * Metoda wypełniająca drugi combox - lista inwariantów wybranego klastra.
	 */
	protected void fillClustInvCombo() {
		try {
			if(selectedClusterIndex == -1) {
				chooseClusterInv.removeAllItems();
				chooseClusterInv.addItem("---");
				return;
			}
			doNotUpdate = true;
			
			ArrayList<Integer> clInvariants = clusterColorsData.clustersInvariants.get(selectedClusterIndex);
			chooseClusterInv.removeAllItems();
			
			String[] clustersInvHeaders = new String[clInvariants.size()+1];
			clustersInvHeaders[0] = "---";
			for (int i = 0; i < clInvariants.size(); i++) {
				int invIndex = clInvariants.get(i);
				clustersInvHeaders[i+1] = "Cluster: "+(selectedClusterIndex+1)+"  |#"+(i+1)+"  Inv: "+(invIndex+1);
			}
			chooseClusterInv.setModel(new DefaultComboBoxModel<String>(clustersInvHeaders));
		} catch (Exception e) {
			
		}
		doNotUpdate = false;
	}
	
	/**
	 * Metoda podświetlająca wybrany inwariant w ramach klastra.
	 */
	protected void showClusterInv() {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();
		
		if(selectedClusterIndex == -1)
			return;
		
		ArrayList<ClusterTransition> transColors = clusterColorsData.dataMatrix.get(selectedClusterIndex);
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		ArrayList<Integer> clInvariants = clusterColorsData.clustersInvariants.get(selectedClusterIndex);
		int invIndex = clInvariants.get(selectedClusterInvIndex);
		ArrayList<Integer> invariant = overlord.getWorkspace().getProject().getINVmatrix().get(invIndex);
		
		for(int i=0; i<transColors.size(); i++) {
			if(transColors.get(i).transInCluster != 0) {   //equals(Color.white)) {
				transitions.get(i).setColorWithNumber(true, Color.DARK_GRAY, false, -1, false, "");
			}
		}
		
		for(int i=0; i<invariant.size(); i++) {
			if(invariant.get(i) != 0) {
				
				transitions.get(i).setColorWithNumber(true, Color.GREEN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
			}
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}

	/**
	 * Metoda odpowiedzialna za zapisanie na dysky obrazów klastrów.
	 */
	protected void dropClustersToFiles() {
		//chose folder
		String lastPath = overlord.getLastPath();
		String dirPath = Tools.selectDirectoryDialog(lastPath, "Select dir",
				"Select directory for clusters screenshots.");
		if(dirPath.equals("")) { // czy wskazano cokolwiek
			return;
		}
		
		int clusters = clusterColorsData.dataMatrix.size();
		int oldSelected = selectedClusterIndex;
		
		
		GraphPanel main = overlord.getWorkspace().getSheets().get(0).getGraphPanel();
		main.setZoom(120, main.getZoom());
		try {
			JOptionPane.showMessageDialog(null, 
					  "Please click OK button and wait until another information window shows up.\n"
					+ "Depending on the number of clusters this should take from 10 sec. to 1 min.\n"
					+ "Please do not use the program until operation is finished.", "Please wait", JOptionPane.INFORMATION_MESSAGE);
			
			progressBar.setMaximum(clusters-1);
			progressBar.setValue(0);
			progressBar.setVisible(true);
			
			for(int c=0; c<clusters; c++) {
				progressBar.setValue(c);
				progressBar.update(progressBar.getGraphics());
				String clusterName = "cluster"+c+"("+clusterColorsData.clSize.get(c)+")";
				selectedClusterIndex = c;
				showClusters();
				String fileName = ""+dirPath + "//"+clusterName+".png";
				BufferedImage image = main.createImageFromSheet();
				ImageIO.write(image, "png", new File(fileName));
			}
		} catch (Exception e) {
			overlord.log("Saving clusters screenshots failed.", "error", true);
			progressBar.setVisible(false);
		} finally {
			selectedClusterIndex = oldSelected;
			main.setZoom(100, main.getZoom());
		}
		progressBar.setVisible(false);
		JOptionPane.showMessageDialog(null, "Saving "+clusters+" clusters to graphic files completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
		
	}

	/**
	 * Metoda pokazująca dane o klastrach w notatniku.
	 */
	protected void showClustersNotepad() {
		if(selectedClusterIndex == -1)
			return;
		
		HolmesNotepad note = new HolmesNotepad(640, 480);
		
		note.addTextLineNL("", "text");
		note.addTextLineNL("Cluster: "+(selectedClusterIndex+1)+" ("+clusterColorsData.clSize.get(selectedClusterIndex)+" inv.) alg.: "+clusterColorsData.algorithm
				+" metric: "+clusterColorsData.metric, "text");
		note.addTextLineNL("", "text");
		
		ArrayList<ClusterTransition> transColors = clusterColorsData.dataMatrix.get(selectedClusterIndex);
		
		ArrayList<Transition> holyVector = overlord.getWorkspace().getProject().getTransitions();
		for(int i=0; i<transColors.size(); i++) { //ustaw kolory dla tranzycji
			int trInCluster = transColors.get(i).transInCluster;
			double firedInCluster = transColors.get(i).firedInCluster; // ????
			if(trInCluster>0) {
				String t1 = Tools.setToSize("t"+(i), 5, false);
				String t2 = Tools.setToSize("Freq.: "+trInCluster, 12, false);
				String t3 = Tools.setToSize("Fired: "+formatD(firedInCluster), 15, false);
				String txt = t1 + t2 + t3 + " ; "+holyVector.get(i).getName();
				note.addTextLineNL(txt, "text");
			}
		}
		note.setCaretFirstLine();
		note.setVisible(true);
	}

	/**
	 * Metoda pokazująca dane o klastrze na ekranie sieci oraz w podoknie programu.
	 */
	protected void showClusters() {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();
		
		if(selectedClusterIndex == -1) {
			mssValueLabel.setText("n/a");
			return;
		}
		
		ArrayList<ClusterTransition> transColors = clusterColorsData.dataMatrix.get(selectedClusterIndex);
		ArrayList<Transition> holyVector = overlord.getWorkspace().getProject().getTransitions();
		ColorPalette cp = new ColorPalette();
		ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();

		float mss = clusterColorsData.clMSS.get(selectedClusterIndex);
		mssValueLabel.setText(""+mss);
		
		for(int i=0; i<transColors.size(); i++) { //ustaw kolory dla tranzycji
			if(transColors.get(i).transInCluster == 0) {   //equals(Color.white)) {
				holyVector.get(i).setColorWithNumber(false, Color.white, false, -1, false, "");
			} else {
				if(clustersMCT) {
					int mctNo = transMCTvector.get(i);
					if(mctNo == -1) {
						if(clusterColorsData.showFirings == true) { //pokazuj średnią liczbę odpaleń
							if(clusterColorsData.showScale == true) { //pokazuj kolory skalowalne
								holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).firedInCluster, false, "", 0, 20, 5, -3);
							} else { //pokazuj kolory z krokiem 10%
								holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).firedInCluster, false, "", 0, 20, 5, -3);
							}
						} else { //pokazuj tylko liczbę wystąpień jako część inwariantów
							if(clusterColorsData.showScale == true) { //pokazuj kolory skalowalne
								holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
							} else { //pokazuj kolory z krokiem 10%
								holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).transInCluster , false, "", 0, 20, 5, -3);
							}
						}
					} else {
						double value = 0;
						if(clusterColorsData.showFirings == true) {
							value = transColors.get(i).firedInCluster;
						} else {
							value = transColors.get(i).transInCluster;
						}
						holyVector.get(i).setColorWithNumber(true, cp.getColor(mctNo), true, value, true, "[MCT"+(mctNo+1)+"]", -10, 15, 5, -3);
					}	
				} else {
					if(clusterColorsData.showFirings == true) { //pokazuj średnią liczbę odpaleń
						if(clusterColorsData.showScale == true) { //pokazuj kolory skalowalne
							double tranNumber = transColors.get(i).firedInCluster;
							Color tranColor = transColors.get(i).colorFiredScale;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
						} else { //pokazuj kolory z krokiem 10%
							double tranNumber = transColors.get(i).firedInCluster;
							Color tranColor = transColors.get(i).colorFiredGrade;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
						}
					} else { //pokazuj tylko liczbę wystąpień jako część inwariantów
						if(clusterColorsData.showScale == true) { //pokazuj kolory skalowalne
							int tranNumber = transColors.get(i).transInCluster;
							Color tranColor = transColors.get(i).colorTransScale;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
						} else { //pokazuj kolory z krokiem 10%
							int tranNumber = transColors.get(i).transInCluster;
							Color tranColor = transColors.get(i).colorTransGrade;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
						}
					}
				}
			}
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda zmienia liczbę double na formatowany ciąg znaków.
	 * @param value double - liczba
	 * @return String - ciąg znaków
	 */
	private static String formatD(double value) {
        DecimalFormat df = new DecimalFormat("#.####");
        String txt = df.format(value);
        txt = txt.replace(",", ".");
		return txt;
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************        MCS       ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	/**
	 * Metoda pomocnicza konstruktora podokna dla zbiorów MCS.
	 * @param mcsData MCSDataMatrix - obiekt danych zbiorów MCS
	 */
	public void createMCSSubWindow(MCSDataMatrix mcsData)
	{
		transitions = overlord.getWorkspace().getProject().getTransitions();
		if(mcsData == null || transitions.size() == 0) {
			//return;
		} 
		
		initiateContainers();
		
		int posX = 10;
		int posY = 10;

		JLabel objRLabel = new JLabel("Reaction: ");
		objRLabel.setBounds(posX, posY, 80, 20);
		components.add(objRLabel);

		String[] objRset = new String[transitions.size() + 1];
		objRset[0] = "---";
		for (int i = 0; i < transitions.size(); i++) {
			objRset[i + 1] = "t"+i+transitions.get(i).getName();
		}
		

		//WYBÓR REAKCJI ZE ZBIORAMI MCS
		mcsObjRCombo = new JComboBox<String>(objRset);
		mcsObjRCombo.setBounds(posX+60, posY, 230, 20);
		mcsObjRCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(stopAction == true)
					return;
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				int selected = comboBox.getSelectedIndex();
				if (selected > 0) {
					selected--;
					MCSDataMatrix mcsDataCore = overlord.getWorkspace().getProject().getMCSdataCore();
					ArrayList<ArrayList<Integer>> sets = mcsDataCore.getMCSlist(selected--);
					
					if(sets == null)
						return;
					
					stopAction = true;
					mcsMCSforObjRCombo.removeAllItems();
					mcsMCSforObjRCombo.addItem("---");
					
					String newRow = "";
					for(ArrayList<Integer> set : sets) {
						newRow = "[";
						for(int el : set) {
							newRow += el+", ";
						}
						newRow += "]";
						newRow = newRow.replace(", ]", "]");
						mcsMCSforObjRCombo.addItem(newRow);
					}
					stopAction = false;
				}
			}
		});
		components.add(mcsObjRCombo);
		posY += 25;
		
		JLabel mcsLabel = new JLabel("MCS: ");
		mcsLabel.setBounds(posX, posY, 80, 20);
		components.add(mcsLabel);
		
		String[] init = new String[1];
		init[0] = "---";

		//WYBÓR ZBIORU MCS:
		mcsMCSforObjRCombo = new JComboBox<String>(init);
		mcsMCSforObjRCombo.setBounds(posX+60, posY, 160, 20);
		mcsMCSforObjRCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(stopAction == true)
					return;
				
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				int selected = comboBox.getSelectedIndex();
				if (selected > 0) {
					selected--;
					//MCSDataMatrix mcsDataCore = overlord.getWorkspace().getProject().getMCSdataCore();
					int selTrans = mcsObjRCombo.getSelectedIndex();
					selTrans--;
					showMCSDataInNet(comboBox.getSelectedItem().toString(), selTrans);
				}
			}
		});
		components.add(mcsMCSforObjRCombo);
		
		JButton refreshButton = new JButton();
		refreshButton.setText("Refresh");
		refreshButton.setBounds(posX+225, posY, 70, 20);
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				transitions = overlord.getWorkspace().getProject().getTransitions();
				if(transitions.size() == 0)
					return;
					
				String[] objRset = new String[transitions.size() + 1];
				objRset[0] = "---";
				for (int i = 0; i < transitions.size(); i++) {
					objRset[i + 1] = "t"+i+"_"+transitions.get(i).getName();
				}
				stopAction = true;

				mcsObjRCombo.removeAllItems();
				for(String str : objRset) {
					mcsObjRCombo.addItem(str);
				}
				stopAction = false;
			}
		});
		refreshButton.setFocusPainted(false);
		panel.add(refreshButton);
		posY += 20;

		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}
	
	/**
	 * Metoda pokazuje w kolorach tranzycje wchodzące w skład MCS oraz tramzycję bazową zbioru MCS.
	 * @param sets String - zbiór w formie łańcucha znaków [x, y, z, ...]
	 * @param objReactionID int - nr tranzycji bazowe
	 */
	protected void showMCSDataInNet(String sets, int objReactionID) {
		try {
			PetriNet pn = overlord.getWorkspace().getProject();
			pn.resetNetColors();

			sets = sets.replace("[", "");
			sets = sets.replace("]", "");
			sets = sets.replace(" ", "");
			
			String[] elements = sets.split(",");
			ArrayList<Integer> invIDs = new ArrayList<Integer>();
			
			for(String el : elements) {
				invIDs.add(Integer.parseInt(el));
			}
			
			Transition trans_TMP = overlord.getWorkspace().getProject().getTransitions().get(objReactionID);
			trans_TMP.setColorWithNumber(true, Color.red, false, -1, false, "");
			
			for(int id : invIDs) {
				trans_TMP= overlord.getWorkspace().getProject().getTransitions().get(id);
				trans_TMP.setColorWithNumber(true, Color.black, false, -1, false, "");
				//double tranNumber = transColors.get(i).firedInCluster;
				//Color tranColor = transColors.get(i).colorFiredScale;
				//holyVector.get(i).setColorWithNumber(true, tranColor, tranNumber);
			}
			
			overlord.getWorkspace().getProject().repaintAllGraphPanels();
		} catch (Exception e) {
			
		}
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************     KNOCKOUT     ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * 
	 * @param knockoutData ArrayList[ArrayList[Integer]] - macierz danych o knockout
	 * @param type1 - nie ważne co, ważne że wyróżnia konstruktor
	 * @param type2 - nie ważne co, ważne że wyróżnia konstruktor
	 * @param type3 - nie ważne co, ważne że wyróżnia konstruktor
	 */
	public HolmesDockWindowsTable(ArrayList<ArrayList<Integer>> knockoutData, boolean type1, int type2, boolean type3)
	{
		if(knockoutData == null || knockoutData.size() == 0) {
			knockoutData = null;
			return;
		} else {
			mode = KNOCKOUT;
			this.knockoutData = knockoutData;
			//overlord.reset.setMCTStatus(true);
		}
		
		int colA_posX = 10;
		int colB_posX = 100;
		int positionY = 10;
		
		initiateContainers();
		
		//MCT - obliczenia:
		MCTCalculator analyzer = overlord.getWorkspace().getProject().getMCTanalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		mct = MCTCalculator.getSortedMCT(mct, false);
		
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		int transSize = transitions.size();
		
		ArrayList<String> mctOrNot = new ArrayList<String>();
		ArrayList<Integer> mctSize = new ArrayList<Integer>();
		for(int i=0; i<transSize; i++) {
			mctOrNot.add("");
			mctSize.add(0);
		}
		int mctNo= 0;
		for(ArrayList<Transition> arr : mct) {
			mctNo++;
			for(Transition t : arr) {
				int id = transitions.indexOf(t);
				mctOrNot.set(id, "MCT_"+mctNo);
				mctSize.set(id, arr.size());
			}
		}
		
		// nazwy tranzycji
		String[] headers = new String[transSize + 1];
		headers[0] = "---";
		for (int i = 0; i < transSize; i++) {
			String newLine = "t" + i + "   " + mctOrNot.get(i);
			headers[i + 1] = newLine;
		}
				
		// getting the data
		JLabel chooseMctLabel = new JLabel("Knockout: ");
		chooseMctLabel.setBounds(colA_posX, positionY, 60, 20);
		components.add(chooseMctLabel);

		JComboBox<String> chooseMctBox = new JComboBox<String>(headers);
		chooseMctBox.setBounds(colB_posX, positionY, 150, 20);
		chooseMctBox.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent actionEvent) {
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				int selected = comboBox.getSelectedIndex();
				if (selected == 0) {
					showKnockout(-1, false);
				} else  {
					selected--;
					showKnockout(selected, true);
				} 
			}
		});
		components.add(chooseMctBox);
		positionY += 30;
		
		knockoutTextArea = new JTextArea();
		knockoutTextArea.setEditable(false);
		knockoutTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		//mctTextArea.setLineWrap(true);
		//mctTextArea.setWrapStyleWord(true);
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());
		textAreaPanel.add(new JScrollPane(
				knockoutTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
        		BorderLayout.CENTER);
		
		int w = overlord.getMctBox().getWidth();
		int h = overlord.getMctBox().getHeight();
		textAreaPanel.setBounds(colA_posX, positionY, w-30, h-60);
		components.add(textAreaPanel);
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}	

	/**
	 * Metoda odpowiedzialna za pokazanie szczegółów wybranego zbioru MCT.
	 * @param knockIndex Integer - numer wybranego zbioru
	 * @param showOrClear boolean - true, jeśli wybrano zbiór mct, false jeśli "---"
	 */
	private void showKnockout(Integer knockIndex, boolean showOrClear) {
		PetriNet pn = overlord.getWorkspace().getProject();
		pn.resetNetColors();
		
		if(showOrClear == true)
		{
			ArrayList<Integer> idToShow = knockoutData.get(knockIndex);
			Transition trans_TMP;
			ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
			
			for(int id : idToShow) { //wyłączane przez objR
				trans_TMP= transitions.get(id);
				trans_TMP.setColorWithNumber(true, Color.black, false, -1, false, "");
			}
			
			trans_TMP= transitions.get(knockIndex);
			trans_TMP.setColorWithNumber(true, Color.red, false, -1, false, "");
			
			knockoutTextArea.setText("");
			knockoutTextArea.append("Knocked out:" + knockIndex + ":\n");
			knockoutTextArea.append("\n");
			knockoutTextArea.append(" * * * Also knocked out: \n");
			
			for (int t_id : idToShow) {
				String t1 = Tools.setToSize("t"+t_id, 5, false);
				knockoutTextArea.append(t1 + " ; "+transitions.get(t_id).getName()+"\n");
			}
			knockoutTextArea.setCaretPosition(0);
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************   FIX & DETECT   ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Tworzy okno wykrywania i wskazywania problemów sieci.
	 */
	public void createFixerSubWindow() {
		int posX = 10;
		int posY = 10;
		
		initiateContainers();
		detector = new ProblemDetector(this);
		//TODO
		
		JLabel label0 = new JLabel("Invariants:");
		label0.setBounds(posX, posY, 100, 20);
		components.add(label0);
		
		fixInvariants = new JLabel("Normal: 0 / Non-inv.: 0");
		fixInvariants.setBounds(posX, posY+=20, 190, 20);
		components.add(fixInvariants);
		
		fixInvariants2 = new JLabel("Sub-inv.: 0 / Sur-inv: 0");
		fixInvariants2.setBounds(posX, posY+=20, 190, 20);
		components.add(fixInvariants2);
		
		JButton markInvButton = new JButton();
		markInvButton.setText("<html>Show<br>inv.</html>");
		markInvButton.setBounds(posX+195, posY-18, 90, 32);
		markInvButton.setMargin(new Insets(0, 0, 0, 0));
		markInvButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/aa.png"));
		markInvButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				detector.markSubSurNonInvariantsPlaces();
			}
		});
		markInvButton.setFocusPainted(false);
		components.add(markInvButton);
		
		JLabel label1 = new JLabel("Input and output places:");
		label1.setBounds(posX, posY+=25, 200, 20);
		components.add(label1);
		
		fixIOPlaces = new JLabel("Input: 0 / Output: 0");
		fixIOPlaces.setBounds(posX, posY+=20, 190, 20);
		components.add(fixIOPlaces);
		
		JButton markIOPlacesButton = new JButton();
		markIOPlacesButton.setText("<html>Show<br>places</html>");
		markIOPlacesButton.setBounds(posX+195, posY-16, 90, 32);
		markIOPlacesButton.setMargin(new Insets(0, 0, 0, 0));
		markIOPlacesButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/aa.png"));
		markIOPlacesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				detector.markIOPlaces();
			}
		});
		markIOPlacesButton.setFocusPainted(false);
		components.add(markIOPlacesButton);
		
		JLabel label2 = new JLabel("Input and output transitions:");
		label2.setBounds(posX, posY+=25, 200, 20);
		components.add(label2);
		
		fixIOTransitions = new JLabel("Input: 0 / Output: 0");
		fixIOTransitions.setBounds(posX, posY+=20, 190, 20);
		components.add(fixIOTransitions);
		
		JButton markIOTransButton = new JButton();
		markIOTransButton.setText("<html>Show<br>trans.</html>");
		markIOTransButton.setBounds(posX+195, posY-14, 90, 32);
		markIOTransButton.setMargin(new Insets(0, 0, 0, 0));
		markIOTransButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/aa.png"));
		markIOTransButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				detector.markIOTransitions();
			}
		});
		markIOTransButton.setFocusPainted(false);
		components.add(markIOTransButton);
		
		JLabel label3 = new JLabel("Linear transitions and places");
		label3.setBounds(posX, posY+=25, 200, 20);
		components.add(label3);
		
		fixlinearTrans = new JLabel("Transitions: 0 / Places: 0");
		fixlinearTrans.setBounds(posX, posY+=20, 190, 20);
		components.add(fixlinearTrans);
		
		JButton markLinearTPButton = new JButton();
		markLinearTPButton.setText("<html>Show<br>T & P</html>");
		markLinearTPButton.setBounds(posX+195, posY-12, 90, 32);
		markLinearTPButton.setMargin(new Insets(0, 0, 0, 0));
		markLinearTPButton.setIcon(Tools.getResIcon22("/icons/mcsWindow/aa.png"));
		markLinearTPButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				detector.markLinearRegions();
			}
		});
		markLinearTPButton.setFocusPainted(false);
		components.add(markLinearTPButton);
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}

	
	//**************************************************************************************
	//**************************************************************************************
	//**************************************************************************************
	//**************************************************************************************
	//**************************************************************************************
	
	/**
	 * Metoda pomocnicza tworząca szkielet podokna właściwości.
	 */
	private void initiateContainers() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		components = new ArrayList<JComponent>();
		panel = new JPanel();
	}
	
	/**
	 * Metoda zwraca okno tekstowe na bazie podanego comboBox.
	 * @param spinner JSpinner - ComboBox po ludzku
	 * @return JFormattedTextField - chyba TextBox?
	 */
	public JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor) editor).getTextField();
		} else {
			System.err.println("Unexpected editor type: " + spinner.getEditor().getClass()
					+ " isn't a descendant of DefaultEditor");
			return null;
		}
	}

	/**
	 * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
	 */
	private void repaintGraphPanel() {
		int sheetIndex = overlord.IDtoIndex(elementLocation.getSheetID());
		GraphPanel graphPanel = overlord
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		graphPanel.repaint();
	}

	/**
	 * Metoda zmienia szerokość arkusza dla sieci.
	 * @param width int - nowa szerokość
	 */
	private void setSheetWidth(int width) {
		if (mode == SHEET) {
			setContainerWidth(width, currentSheet.getGraphPanel());
			setContainerWidth(width, currentSheet.getContainerPanel());
			currentSheet.getGraphPanel().setOriginSize(currentSheet.getGraphPanel().getSize());
		}
	}

	/**
	 * Metoda zmienia wysokość arkusza dla sieci.
	 * @param width int - nowa wysokość
	 */
	private void setSheetHeight(int height) {
		if (mode == SHEET) {
			setContainerHeight(height, currentSheet.getGraphPanel());
			setContainerHeight(height, currentSheet.getContainerPanel());
			currentSheet.getGraphPanel().setOriginSize(currentSheet.getGraphPanel().getSize());
		}
	}

	/**
	 * Metoda zmienia szerokość wymiaru dla arkusza dla sieci.
	 * @param width int - nowa szerokość
	 * @param container JComponent - obiekt dla którego zmieniany jest wymiar
	 */
	private void setContainerWidth(int width, JComponent container) {
		if (mode == SHEET) {
			Dimension dim = container.getSize();
			dim.setSize(width, dim.height);
			container.setSize(dim);
		}
	}

	/**
	 * Metoda zmienia wysokość dla wymiaru dla arkusza dla sieci.
	 * @param width int - nowa wysokość
	 * @param container JComponent - obiekt dla którego zmieniany jest wymiar
	 */
	private void setContainerHeight(int height, JComponent container) {
		if (mode == SHEET) {
			Dimension dim = container.getSize();
			dim.setSize(dim.width, height);
			container.setSize(dim);
		}
	}

	/**
	 * Metoda ustawia opcję autoscroll dla panelu graficznego w arkuszu sieci.
	 * @param value boolean - true, jeśli autoscroll włączony
	 */
	private void setAutoscroll(boolean value) {
		if (mode == SHEET) {
			currentSheet.getGraphPanel().setAutoDragScroll(value);
		}
	}
	
	/**
	 * Metoda ustawia nową wartość czasu EFT dla tranzycji czasowej.
	 * @param x double - nowe EFT
	 */
	private void setMinFireTime(double x) {
		if (mode == TIMETRANSITION) {
			Transition transition = (Transition) element;
			transition.setEFT(x);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda ustawia nową wartość czasu LFT dla tranzycji czasowej.
	 * @param x double - nowe LFT
	 */
	private void setMaxFireTime(double x) {
		if (mode == TIMETRANSITION) {
			Transition transition = (Transition) element;
			transition.setLFT(x);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda ustawia nową wartość opóźnienia dla produkcji tokenów.
	 * @param x double - nowa wartość duration
	 */
	private void setDurationTime(double x) {
		if (mode == TIMETRANSITION) {
			Transition transition = (Transition) element;
			transition.setDPNduration(x);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda ustawia status trybu TPN dla tranzycji.
	 * @param status boolean - nowy status
	 */
	private void setTPNstatus(boolean status) {
		if (mode == TIMETRANSITION) {
			Transition transition = (Transition) element;
			transition.setTPNstatus(status);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda ustawia status trybu DPN dla tranzycji.
	 * @param status boolean - nowy status
	 */
	private void setDPNstatus(boolean status) {
		if (mode == TIMETRANSITION) {
			Transition transition = (Transition) element;
			transition.setDPNstatus(status);
			repaintGraphPanel();
		}
	}

	/**
	 * Metoda zmienia współrzędną X dla wierzchołka sieci.
	 * @param x int - nowa wartość
	 */
	private void setX(int x) {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			elementLocation.setPosition(new Point(x, elementLocation.getPosition().y));
			repaintGraphPanel();
		}
	}

	/**
	 * Metoda zmienia współrzędną Y dla wierzchołka sieci.
	 * @param y int - nowa wartość
	 */
	private void setY(int y) {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			elementLocation.setPosition(new Point(elementLocation.getPosition().x, y));
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda sprawdza, czy dla danego węzła sieci lokalizacja jego nazwy nie wykracza poza ramy
	 * obrazu sieci - dla współrzędnej Y
	 * @param y int - współrzędna Y
	 * @param n Node - wierzchołek sieci
	 * @param el ElementLocation - obiekt lokalizacji wierzchołka
	 * @return Point - prawidłowe współrzędne
	 */
	protected Point setNameOffsetY(int y, Node n, ElementLocation el) {
		int nameLocIndex = n.getElementLocations().indexOf(el);
		int oldX = n.getNamesLocations().get(nameLocIndex).getPosition().x;
		int oldY = y;
		int newx = oldX+el.getPosition().x;
		int newy = oldY+el.getPosition().y;
		
		int sheetIndex = overlord.IDtoIndex(el.getSheetID());
		GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		
		if(graphPanel.isLegalLocation(new Point(newx, newy)) == true) {
			n.getNamesLocations().get(nameLocIndex).getPosition().setLocation(oldX, oldY);
			graphPanel.repaint();
		}
		return n.getNamesLocations().get(nameLocIndex).getPosition();
	}
	
	/**
	 * Metoda sprawdza, czy dla danego węzła sieci lokalizacja jego nazwy nie wykracza poza ramy
	 * obrazu sieci - dla współrzędnej X
	 * @param x int - współrzędna X
	 * @param n Node - wierzchołek sieci
	 * @param el ElementLocation - obiekt lokalizacji wierzchołka
	 * @return Point - prawidłowe współrzędne
	 */
	protected Point setNameOffsetX(int x, Node n, ElementLocation el) {
		int nameLocIndex = n.getElementLocations().indexOf(el);
		int oldX = x;
		int oldY = n.getNamesLocations().get(nameLocIndex).getPosition().y;
		int newx = oldX+el.getPosition().x;
		int newy = oldY+el.getPosition().y;
		
		int sheetIndex = overlord.IDtoIndex(el.getSheetID());
		GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		
		if(graphPanel.isLegalLocation(new Point(newx, newy)) == true) {
			n.getNamesLocations().get(nameLocIndex).getPosition().setLocation(oldX, oldY);
			graphPanel.repaint();
		}
		
		return n.getNamesLocations().get(nameLocIndex).getPosition();
	}
	
	/**
	 * Zmiana nazwy elementu sieci, dokonywana poza listenerem, który
	 * jest klasa anonimową (i nie widzi pola element).
	 * @param newName String - nowa nazwa
	 */
	private void changeName(String newName) {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION || mode == META) {
			Node node = (Node) element;
			node.setName(newName);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda zmienia komentarz dla elementu sieci, poza listenerem, który
	 * jest klasą anonimową (i nie widzi pola element).
	 * @param newComment String - nowy komentarz
	 */
	private void changeComment(String newComment) {
		element.setComment(newComment);	
	}

	private void makePortal() {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			Node node = (Node) element;
			node.setPortal(true);
		}
	}

	private void unPortal() {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			Node node = (Node) element;
			if(node.getElementLocations().size() == 1)
				node.setPortal(false);
		}
	}

	/**
	 * Metoda zmienia liczbę tokenów dla miejsca sieci, poza listenerem, który
	 * jest klasą anonimową (i nie widzi pola element).
	 * @param tokenz int - nowa liczba tokenów
	 */
	private void setTokens(int tokenz) {
		Place place = (Place) element;
		if (mode == PLACE) {
			place.setTokensNumber(tokenz);
			repaintGraphPanel();
		}
	}

	/**
	 * Metoda zmienia wagę dla łuku sieci, poza listenerem, który
	 * jest klasą anonimową (i nie widzi pola element).
	 * @param weight int - nowa waga
	 */
	private void setWeight(int weight) {
		Arc arc = (Arc) element;
		if (mode == ARC) {
			arc.setWeight(weight);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda ustawia nowy obiekt symulatora sieci.
	 * @param netSim NetSimulator - nowy obiekt
	 */
	public void setSimulator(NetSimulator netSim) {
		simulator = netSim;
	}
	
	/**
	 * Metoda zwraca obiekt aktywnego symulatora z podokna symulacji.
	 * @return NetSimulator - obiekt symulatora
	 */
	public NetSimulator getSimulator() {
		return simulator;
	}
	
	/**
	 * Metoda ustawia status wszystkich przycisków rozpoczęcia symulacji za wyjątkiem
	 * Pauzy, Stopu - w przypadku startu / stopu symulacji
	 * @param enabled boolean - true, jeśli mają być aktywne
	 */
	public void setEnabledSimulationInitiateButtons(boolean enabled) {
		for(JComponent comp: components) {
			if(comp instanceof JButton && comp != null && comp.getName() != null) {
				if(comp.getName().equals("simB1") || comp.getName().equals("simB2")
						|| comp.getName().equals("simB3") || comp.getName().equals("simB4")
						|| comp.getName().equals("simB5") || comp.getName().equals("simB6")
						|| comp.getName().equals("reset")) {
					comp.setEnabled(enabled);
				}
			}
		}
	}

	/**
	 * Metoda ustawia status przycisków Stop, Pauza.
	 * @param enabled boolean - true, jeśli mają być aktywne
	 */
	public void setEnabledSimulationDisruptButtons(boolean enabled) {
		for(JComponent comp: components) {
			if(comp instanceof JButton && comp != null && comp.getName() != null) {
				if(comp.getName().equals("stop") || comp.getName().equals("pause")) {
					comp.setEnabled(enabled);
				}
			}
		}
	}

	/**
	 * Metoda uaktywnia tylko przycisku startu dla symulatora, bloku stop i pauzę.
	 */
	public void allowOnlySimulationInitiateButtons() {
		setEnabledSimulationInitiateButtons(true);
		setEnabledSimulationDisruptButtons(false);
	}

	/**
	 * Metoda uaktywnia tylko przyciski stop i pauza dla symulatora. Cała reszta - nieaktywna.
	 */
	public void allowOnlySimulationDisruptButtons() {
		setEnabledSimulationInitiateButtons(false);
		setEnabledSimulationDisruptButtons(true);
	}

	/**
	 * Metoda zostawia aktywny tylko przycisku od-pauzowania.
	 */
	public void allowOnlyUnpauseButton() {
		allowOnlySimulationDisruptButtons();
		//values.get(4).setEnabled(false);
		for(JComponent comp: components) {
			if(comp instanceof JButton && comp != null && comp.getName() != null) {
				if(comp.getName().equals("pause") ) {
					comp.setEnabled(false);
					break;
				}
			}
		}
	}
	
	/**
	 * Metoda czyści dane o inwariantach.
	 */
	public void resetInvariants() {
		invariantsMatrix = null;
	}
	
	/**
	 * Metoda czyści dane o zbiorach MCT.
	 */
	public void resetMCT() {
		mctGroups = null;	
	}
	
	/**
	 * Metoda czyści dane o klastrach.
	 */
	public void resetClusters() {
		clusterColorsData = null;	
	}
}
