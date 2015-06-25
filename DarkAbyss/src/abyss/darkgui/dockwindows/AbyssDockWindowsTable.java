package abyss.darkgui.dockwindows;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import abyss.analyse.InvariantsSimulator;
import abyss.analyse.InvariantsTools;
import abyss.analyse.MCTCalculator;
import abyss.clusters.ClusterDataPackage;
import abyss.clusters.ClusterTransition;
import abyss.darkgui.GUIManager;
import abyss.darkgui.dockwindows.AbyssDockWindow.DockWindowType;
import abyss.graphpanel.GraphPanel;
import abyss.graphpanel.GraphPanel.DrawModes;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.MCSDataMatrix;
import abyss.math.Node;
import abyss.math.PetriNetElement;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.utilities.ColorPalette;
import abyss.utilities.Tools;
import abyss.workspace.WorkspaceSheet;

/**
 * Klasa zawierająca szczegóły interfejsu podokien dokowalnych programu.
 * @author students
 * @author MR<br>
 * <br>
 * <b>Absolute positioning. Of almost absolute everything here. <br>
 * Nie obchodzi mnie, co o tym myślisz. Idź w layout i nie wracaj. ┌∩┐(◣_◢)┌∩┐ 
 * </b><br>
 * Właściwie, to wyleje tu swoje żale na Jave, opensourcowe podejścia w tym języku i takie
 * tam. Nie ma to NIC wspólnego ze studentami, którzy się serio postarali i zrobili okna ok.
 * Przerobiłem metody na pozycjonowanie absolutne, wywaliłem w cholerę wszystkie layouty.
 * Bo tak. Bo ludzie padający przed ich ideą na kolana i bijący pokłony "Oh, layout, jak
 * cudownie, wszystko się nam teraz automatycznie rozmieści" nie zauważają, albo nie chcą
 * zauważać, że to 'automatycznie' jest tak do dupy, tak bardzo zj... że już bardziej się
 * chyba nie da. PO CO MI LATAJĄCE WE WSZYSTKIE STRONY ELEMENTY OKNA, SKORO CHCIAŁBYM
 * MIEĆ JE NA STAŁE W JEDNYM MIEJSCU?! Ok, ale o co tu chodzi? No więc albo się używa w
 * Javie layoutów, 2 polecenia na krzyż i wszystko się rozmieszcza gdzie chce i jak chce,
 * albo robi ręcznie i okazuje się, że Java w najmniejszym stopniu nie wspiera takiego podejścia.
 * Nagle miliard rzeczy należy ręcznie ustawiać, niepotrzebych na zdrowy rozsądek (PO CO MI 
 * BORDERSIZE JAK MOGŁ USTAWIĆ START LOCATION I SIZE? PO NIC. ALE BEZ NIEGO JPANEL SIE NIE
 * WYŚWIETLI. BO NIE!). Nagle okazuje się, że JPanel ręcznie należy zmusić do przerysowania się
 * (repaint) - bo tak. Z layoutami jakoś pamięta, żeby się narysować. Bez nich już nie.
 * 
 * Konkluzja. Ktoś mógłby powiedzieć, że przecież skoro chce się ręcznie wszystko rozmieścić,
 * to nie należy narzekać, że jest dużo roboty. ZOBACZCIE SOBIE DURNIE .NET MICROSOFTU!!!
 * Są panele, layouty i inne. Ale nie zmuszą się nikogo młotem do ich korzystania jak w Javie.
 * I okazuje się, że nagle jest mniej tam roboty z rozmieszczaniem, niż nawet z layoutami w Javie.
 * Ten język powinien pozostać na etapie konsoli. Jego próby udawania, że służy do
 * tworzenia także aplikacji w oknach kosztują więcej nerwów niż jest to tego warte.
 * 
 * Ostatnia rzecz, jeśli cię to nie przekonuje. Otwórz google. Wpisz dowolną frazę ze słowami
 * "java layout", "problem" względnie "does not". Pół internetu wyleci z pytaniami i (rzadziej) 
 * odpowiedziami. Takie to wspaniałe layouty. (╯゜Д゜）╯︵ ┻━┻)   
 */
public class AbyssDockWindowsTable extends JPanel {
	private static final long serialVersionUID = 4510802239873443705L;
	private ArrayList<JComponent> components;
	private int mode;
	// Containers
	private JPanel panel; // główny panel okna
	public ButtonGroup group = new ButtonGroup();
	public JSpinner spiner = new JSpinner();
	private JTextArea mctTextArea; // tutaj są wyświetlane szczegóły podświetlonego MCT
	private JTextArea knockoutTextArea;
	private JTextArea invTextArea;
	private JTextArea clTextArea;
	private JComboBox<String> chooseCluster;
	public JComboBox<String> simMode;
	public JComboBox<String> mcsObjRCombo;
	public JComboBox<String> mcsMCSforObjRCombo;
	public boolean stopAction = false;
	public JLabel timeStepLabelValue;
	
	private boolean nameLocChangeMode = false;
	
	private WorkspaceSheet currentSheet;
	private PetriNetElement element;
	private ElementLocation elementLocation;
	private NetSimulator simulator;  // obiekt symulatora
	private InvariantsSimulator invSimulator;

	private ArrayList<ArrayList<Integer>> invariantsMatrix; //używane w podoknie inwariantów
	private ArrayList<Transition> transitions; // j.w.
	private ArrayList<ArrayList<Transition>> mctGroups; //używane tylko w przypadku, gdy obiekt jest typu DockWindowType.MctANALYZER
	private ArrayList<ArrayList<Integer>> knockoutData;
	private ClusterDataPackage clusterColorsData;
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
	
	public SpinnerModel nameLocationXSpinnerModel = null;
	public SpinnerModel nameLocationYSpinnerModel = null;
	public boolean doNotUpdate = false;
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************    SYMULATOR     ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************
	
	/**
	 * Konstruktor odpowiedzialny za tworzenie elementów podokna dla symulatora sieci.
	 * @param sim NetSimulator - obiekt symulatora sieci
	 */
	//@SuppressWarnings({ "unchecked", "rawtypes" })
	public AbyssDockWindowsTable(NetSimulator sim, InvariantsSimulator is) {
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
		invSimulator = is;
		
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
				int selectedModeIndex = simMode.getSelectedIndex();
				
				simulator.setSimulatorNetType(selectedModeIndex);
				
				if(invSimulator != null)
					invSimulator.setSimulatorNetType(selectedModeIndex);
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
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
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
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
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
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
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
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
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
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
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
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
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
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
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
				if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
					GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
					GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup = false;
				}
				//mode = SIMULATOR;
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
				if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning("Operation impossible when simulator is working."
						, "Warning") == true)
					return;
				
				Object[] options = {"Save new m0 state", "Cancel",};
				int n = JOptionPane.showOptionDialog(null,
								"Do you want to replace saved m0 state with the current one?",
								"Saving m0 state", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0)
					GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveMarkingZero();
			}
		});
		components.add(saveButton);
		
		columnB_Y += 30;
		JCheckBox maximumMode = new JCheckBox("Maximum mode");
		maximumMode.setBounds(columnA_posX, columnA_Y += 30, 200, 20);
		//maximumMode.setLocation(columnA_posX-4, columnA_Y);
		maximumMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					simulator.setMaximumMode(true);
				} else {
					simulator.setMaximumMode(false);
				}
			}
		});
		components.add(maximumMode);
		
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
				if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().get2ndFormInvariantsList().size()>0)
				{
					
					mode = INVARIANTSSIMULATOR;
					setEnabledSimulationInitiateButtons(false);
					setEnabledSimulationDisruptButtons(false);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationInitiateButtons(false);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationDisruptButtons(false);
					
					try {
						//GUIManager.getDefaultGUIManager().startInvariantsSimulation(Integer.valueOf(group.getSelection().getActionCommand()), 
						//		(Integer) spiner.getValue()); //jaki tryb
					} catch (Exception e) {
						e.printStackTrace();
					}
					//STOP:
					setEnabledSimulationInitiateButtons(true);
					setEnabledSimulationDisruptButtons(false);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationInitiateButtons(true);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationDisruptButtons(false);
					
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
	 * Konstruktor podokna wyświetlającego właściwości klikniętego miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param location ElementLocation - lokalizacja miejsca
	 */
	public AbyssDockWindowsTable(Place place, ElementLocation location) {
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
		
		//int gID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().lastIndexOf(place);
		int gID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().indexOf(place);
		
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
		nameField.setLocation(columnB_posX, columnB_Y += 20);
		nameField.setSize(colBCompLength, 20);
		nameField.setMaximumSize(new Dimension(colBCompLength,20));
		nameField.setMinimumSize(new Dimension(colBCompLength,20));
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
        CreationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
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
        	GUIManager.getDefaultGUIManager().log("Negative number of tokens in "+place.getName(), "error", true);
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
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
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
		portalBox.setEnabled(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					unPortal();
				else
					makePortal();
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
					GUIManager.getDefaultGUIManager().setNameLocationChangeMode(place_tmp, el_tmp, true);
				} else {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
					nameLocChangeMode = false;
					GUIManager.getDefaultGUIManager().setNameLocationChangeMode(null, null, false);
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
	 * Metoda odpowiedzialna za wyświetlenie właściwości klikniętej tranzycji.
	 * @param transition Transition - obiekt tranzycji sieci
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public AbyssDockWindowsTable(Transition transition, ElementLocation location) {
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
		
		int gID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().lastIndexOf(transition);
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
        CreationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
		
		//SHEET ID
        int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
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
		portalBox.setEnabled(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					unPortal();
				else
					makePortal();
			}
		});
		components.add(portalBox);

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
					GUIManager.getDefaultGUIManager().setNameLocationChangeMode(trans_tmp, el_tmp, true);
				} else {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
					nameLocChangeMode = false;
					GUIManager.getDefaultGUIManager().setNameLocationChangeMode(null, null, false);
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
	 * Metoda odpowiedzialna za wyświetlenie właściwości klikniętej tranzycji czasowej.
	 * @param transition TimeTransition - obiekt tranzycji czasowej
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public AbyssDockWindowsTable(final Transition transition, ElementLocation location, int x11, double y17) {
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
		
		int gID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().lastIndexOf(transition);
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
        CreationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
        
		// EFT / LFT TIMES:
		JLabel minMaxLabel = new JLabel("EFT / LFT:", JLabel.LEFT);
		minMaxLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(minMaxLabel);
		JFormattedTextField minTimeField = new JFormattedTextField();
		minTimeField.setValue(transition.getMinFireTime());
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
		maxTimeField.setValue(transition.getMaxFireTime());
		maxTimeField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e
						.getSource();
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

		// T-TRANSITION SHEET ID
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
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
		portalBox.setEnabled(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					unPortal();
				else
					makePortal();
			}
		});
		components.add(portalBox);
		
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
					GUIManager.getDefaultGUIManager().setNameLocationChangeMode(trans_tmp, el_tmp, true);
				} else {
					button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
					nameLocChangeMode = false;
					GUIManager.getDefaultGUIManager().setNameLocationChangeMode(null, null, false);
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
	//*********************************                  ***********************************
	//*********************************       ŁUK        ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Konstruktor odpowiedzialny za utworzenie elementów podokna właściwości klikniętego
	 * łuku sieci.
	 * @param arc Arc - obiekt łuku
	 */
	public AbyssDockWindowsTable(Arc arc) {
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
        CreationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
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
	 * Konstruktor odpowiedzialny za wypełnienie podokna właściwości dla wybranego arkusza sieci. 
	 * @param sheet WorkspaceSheet - obiekt arkusza
	 */
	public AbyssDockWindowsTable(WorkspaceSheet sheet) {
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
		netNameField.setText(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getName());
		netNameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().setName(newName);
			}
		});
		components.add(netNameField);
		
		
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(sheet.getId()));
		idLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
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
	 * Konstruktor odpowiedzialny za wypełnienie podokna umożliwiającego wybór poszczególnych
	 * inwariantów sieci.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 */
	public AbyssDockWindowsTable(ArrayList<ArrayList<Integer>> invariantsData) {
	//public AbyssDockWindowsTable(ArrayList<ArrayList<InvariantTransition>> invariants) {
		
		if(invariantsData == null || invariantsData.size() == 0) {
			return;
		} else {
			mode = INVARIANTS;
			invariantsMatrix = invariantsData;
			transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			GUIManager.getDefaultGUIManager().reset.setInvariantsStatus(true);
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
			invariantHeaders[i + 1] = "Inv. #" + Integer.toString(i) +" (size: "+invSize+")";
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
					showInvariant(0, false); //clean
				} else if(comboBox.getSelectedIndex() == items-2) { 
					showDeadInv(); //show transition without invariants
				} else if(comboBox.getSelectedIndex() == items-1) { 
					showInvTransFrequency(); //show transition frequency (in invariants)
				} else {
					showInvariant(comboBox.getSelectedIndex() - 1, true);
				}
			}
		});
		components.add(chooseInvBox);
		positionY += 30;
		
		invTextArea = new JTextArea();
		invTextArea.setEditable(false);
		invTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());
		textAreaPanel.add(new JScrollPane(
				invTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
        		BorderLayout.CENTER);
		
		int w = GUIManager.getDefaultGUIManager().getMctBox().getWidth();
		int h = GUIManager.getDefaultGUIManager().getMctBox().getHeight();
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
	 * Metoda odpowiedzialna za podświetlanie inwariantów na rysunku sieci.
	 * @param invariantIndex Integer - numer wybranego inwariantu
	 * @param inv isThereInv - false, jeśli wybrano opcję '---'
	 */
	private void showInvariant(Integer invariantIndex, boolean isThereInv) {
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();
		
		if(isThereInv)
		{
			invTextArea.setText("");
			invTextArea.append("Transitions of INV #" + invariantIndex + ":\n");
			ArrayList<Integer> invariant = invariantsMatrix.get(invariantIndex);
			if(transitions.size() != invariant.size()) {
				transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
				if(transitions == null || transitions.size() != invariant.size()) {
					GUIManager.getDefaultGUIManager().log("Critical error in invariants subwindow. "
							+ "Invariants size differ from transition set cardinality!", "error", true);
					return;
				}
			}
			
			//long mintime = 0;
			//long maxtime = 0;
			//for (InvariantTransition transition : invariant) {
				//mintime+=transition.getTransition().getMinFireTime();
				//maxtime+=transition.getTransition().getMaxFireTime();
			//}
			
			for(int t=0; t<invariant.size(); t++) {
				int fireValue = invariant.get(t);
				if(fireValue == 0)
					continue;
				
				Transition realT = transitions.get(t);
				String t1 = Tools.setToSize("t"+t, 5, false);
				String t2 = Tools.setToSize("Fired: "+fireValue, 12, false);
				invTextArea.append(t1 + t2 + " ; "+realT.getName()+"\n");
				
				realT.setGlowedINV(true, fireValue);
			}
			/*
			for (InvariantTransition invTrans : invariant) {
				Transition realT = invTrans.getTransition(); //prawdziwy obiekt tranzycji
				int globalIndex = GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getTransitions().lastIndexOf(realT);
				String t1 = Tools.setToSize("t"+globalIndex, 5, false);
				String t2 = Tools.setToSize("Fired: "+invTrans.getAmountOfFirings(), 12, false);
				invTextArea.append(t1 + t2 + " ; "+invTrans.getTransition().getName()+"\n");
				
				invTrans.getTransition().setGlowed_INV(true, invTrans.getAmountOfFirings());
			}
			*/
			
			invTextArea.setCaretPosition(0);
		}
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	
	private void showInvTransFrequency() {
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();
		
		invTextArea.setText("");
		ArrayList<Integer> freqVector = InvariantsTools.getFrequency(invariantsMatrix);
		ArrayList<Transition> transitions_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		if(freqVector == null) {
			JOptionPane.showMessageDialog(null, "No invariants data available.", "No invariants", JOptionPane.INFORMATION_MESSAGE);
		} else {
			for(int i=0; i<freqVector.size(); i++) {
				Transition realT = transitions_tmp.get(i);
				realT.setGlowedINV(true, freqVector.get(i));
			}
		}
		invTextArea.setCaretPosition(0);
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda pomicnicza do zaznaczania tranzycji nie pokrytych inwariantami.
	 */
	private void showDeadInv() {
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();
		
		invTextArea.setText("");
		invTextArea.append("Transitions not covered by invariants:\n");
		//ArrayList<Integer> deadTrans = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getUncoveredInvariants();
		ArrayList<Integer> deadTrans = InvariantsTools.detectUncovered(invariantsMatrix);
		ArrayList<Transition> transitions_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		if(deadTrans == null) {
			JOptionPane.showMessageDialog(null, "No invariants data available.", "No invariants", JOptionPane.INFORMATION_MESSAGE);
		} else {
			for(int i=0; i<deadTrans.size(); i++) {
				int active = deadTrans.get(i);
				if(active == 1)
					continue;
				
				Transition realT = transitions_tmp.get(i);
				int globalIndex = GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getTransitions().lastIndexOf(realT);
				
				String t1 = Tools.setToSize("t"+globalIndex, 5, false);
				invTextArea.append(t1 + " | "+realT.getName()+"\n");
				
				realT.setGlowedINV(true, 0);
			}
		}
		invTextArea.setCaretPosition(0);
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}

	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************       MCT        ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * Konstruktor odpowiedzialny za utworzenie podokna wyboru zbiorów MCT.
	 * @param mct ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
	 * @param type Properties.PropertiesType - nic nie znaczący tutaj element...
	 */
	@SuppressWarnings("unchecked")
	public AbyssDockWindowsTable(ArrayList<ArrayList<Transition>> mct, AbyssDockWindow.DockWindowType type) {
		if(!(type == DockWindowType.MctANALYZER) || mct == null || mct.size() == 0) {
			return;
			//błędna wywołanie
		} else {
			mode = MCT;
			GUIManager.getDefaultGUIManager().reset.setMCTStatus(true);
		}
		
		int colA_posX = 10;
		int colB_posX = 100;
		int positionY = 10;
		
		initiateContainers();
		this.mctGroups = mct;
		//ogranicz MCT do nietrywialnych
		ArrayList<Transition> unused = new ArrayList<Transition>();
		for(int i=0; i<mctGroups.size(); i++) {
			ArrayList<Transition> mctRow = mctGroups.get(i);
			if(mctRow.size()==1) {
				unused.add(mctRow.get(0));
				mctGroups.set(i, null);
			}
		}
		for(int i=0; i<mctGroups.size(); i++) {
			ArrayList<Transition> mctRow = mctGroups.get(i);
			if(mctRow == null) {
				mctGroups.remove(i);
				i--;
			}
		}
		Object [] temp = mctGroups.toArray();
		Arrays.sort(temp, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
		        ArrayList<Transition> temp1 = (ArrayList<Transition>)o1;
		        ArrayList<Transition> temp2 = (ArrayList<Transition>)o2;

		        if(temp1.size() > temp2.size())
		        	return -1;
		        else if(temp1.size() == temp2.size()) {
		        	return 0;
		        } else
		        	return 1;
		    }
		});
		
		mctGroups.clear();
		for(Object o: temp) {
			mctGroups.add((ArrayList<Transition>)o);
		}
		mctGroups.add(unused); //dodaj wszystkie pojedzyncze tranzycje w jeden 'mct'
		
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
		chooseMctLabel.setBounds(colA_posX, positionY, 60, 20);
		components.add(chooseMctLabel);

		JComboBox<String> chooseMctBox = new JComboBox<String>(mctHeaders);
		chooseMctBox.setBounds(colB_posX, positionY, 150, 20);
		chooseMctBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				int selected = comboBox.getSelectedIndex();
				if (selected == 0) {
					showMct(0, false);
				} else if(selected == comboBox.getItemCount()-1) {
					showAllColors();
				} else {
					showMct(selected - 1, true);
				}
			}
		});
		components.add(chooseMctBox);
		positionY += 30;
		
		mctTextArea = new JTextArea();
		mctTextArea.setEditable(false);
		mctTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		//mctTextArea.setLineWrap(true);
		//mctTextArea.setWrapStyleWord(true);
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());
		textAreaPanel.add(new JScrollPane(
				mctTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
        		BorderLayout.CENTER);
		
		int w = GUIManager.getDefaultGUIManager().getMctBox().getWidth();
		int h = GUIManager.getDefaultGUIManager().getMctBox().getHeight();
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
	 * @param mctIndex Integer - numer wybranego zbioru
	 * @param isThereMCT boolean - true, jeśli wybrano zbiór mct, false jeśli "---"
	 */
	private void showMct(Integer mctIndex, boolean isThereMCT) {
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();
		
		if(isThereMCT)
		{
			mctTextArea.setText("");
			mctTextArea.append("Transitions of MCT #" + mctIndex + ":\n");
			ArrayList<Transition> mct = mctGroups.get(mctIndex);
			for (Transition transition : mct) {
				int globalIndex = GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getTransitions().lastIndexOf(transition);
				String t1 = Tools.setToSize("t"+globalIndex, 5, false);
				mctTextArea.append(t1 + " ; "+transition.getName()+"\n");
				transition.setGlowed_MTC(true);
			}
			mctTextArea.setCaretPosition(0);
		}
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie wszystkich nietrywalniach zbiorów MCT w kolorach.
	 */
	private void showAllColors() {
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();

		ColorPalette cp = new ColorPalette();
		for(int m=0; m<mctGroups.size()-1; m++) {
			Color currentColor = cp.getColor();
			ArrayList<Transition> mct = mctGroups.get(m);
			for (Transition transition : mct) {
				if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("mctNameShow").equals("1"))
					transition.setColorWithNumber(true, currentColor, false, m, true, "MCT #"+m+" ("+mct.size()+")");
				else
					transition.setColorWithNumber(true, currentColor, false, m, true, "");
			}
		}
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************     KLASTRY      ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * 
	 * @param windowType int - w zależności od tego, tworzy dane okno
	 */
	public AbyssDockWindowsTable(ClusterDataPackage clusteringData, boolean ImNotHere) {
		initiateContainers();
			
		if(clusteringData == null || clusteringData.dataMatrix.size() == 0) {
			return;
		} else {
			mode = CLUSTERS;
			clusterColorsData = clusteringData;
			GUIManager.getDefaultGUIManager().reset.setClustersStatus(true);
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
		
		
		
		positionY += 20;
		JLabel chooseInvLabel = new JLabel("Selected: ");
		chooseInvLabel.setBounds(colA_posX, positionY, 80, 20);
		components.add(chooseInvLabel);
		
		// PRZEWIJALNA LISTA KLASTRÓW:
		String[] clustersHeaders = new String[clusterColorsData.dataMatrix.size() + 1];
		clustersHeaders[0] = "---";
		for (int i = 0; i < clusterColorsData.dataMatrix.size(); i++) {
			clustersHeaders[i + 1] = "Cluster " + Integer.toString(i+1) 
					+ " (size: "+clusterColorsData.clSize.get(i)+" inv.)";
		}
		
		chooseCluster = new JComboBox<String>(clustersHeaders);
		chooseCluster.setBounds(colB_posX, positionY, 150, 20);
		chooseCluster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
				if (comboBox.getSelectedIndex() == 0) {
					showClusters(0, false);
				} else {
					showClusters(comboBox.getSelectedIndex() - 1, true);
				}
			}
		});
		components.add(chooseCluster);
		positionY += 20;
		
		//SPOSÓB WYŚWIETLANIA - TRANZYCJE CZY ODPALENIA
		JCheckBox transFiringMode = new JCheckBox("Show transition average firing");
		transFiringMode.setBounds(colA_posX-3, positionY, 220, 20);
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
		
		positionY += 20;
		JCheckBox scaleMode = new JCheckBox("Show scaled colors");
		scaleMode.setBounds(colA_posX-3, positionY, 160, 20);
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
		positionY += 20;
		
		clTextArea = new JTextArea();
		clTextArea.setEditable(false);
		clTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		JPanel textAreaPanel = new JPanel();
		textAreaPanel.setLayout(new BorderLayout());
		textAreaPanel.add(new JScrollPane(
				clTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
        		BorderLayout.CENTER);
		
		int w = GUIManager.getDefaultGUIManager().getMctBox().getWidth();
		int h = GUIManager.getDefaultGUIManager().getMctBox().getHeight();
		textAreaPanel.setBounds(colA_posX, positionY, w-20, h-(positionY+20));
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
	 * Metoda pokazująca dane o klastrze na ekranie sieci oraz w podoknie programu.
	 * @param clusterNo int - nr klastra
	 * @param isThereCluser boolean - false, jeśli wskazano "---";
	 */
	protected void showClusters(int clusterNo, boolean isThereCluser) {
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();
		
		if(isThereCluser)
		{
			clTextArea.setText("");
			clTextArea.append("Cluster: "+clusterNo + " ("+clusterColorsData.clSize.get(clusterNo)+" inv.) alg.: "+clusterColorsData.algorithm
					+" metric: "+clusterColorsData.metric+"\n\n");
			ArrayList<ClusterTransition> transColors = clusterColorsData.dataMatrix.get(clusterNo);
			ArrayList<Transition> holyVector = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			for(int i=0; i<transColors.size(); i++) { //ustaw kolory dla tranzycji
				if(transColors.get(i).equals(Color.white)) {
					holyVector.get(i).setColorWithNumber(false, Color.white, false, -1, false, "");
				} else {
					if(clusterColorsData.showFirings == true) { //pokazuj średnią liczbę odpaleń
						if(clusterColorsData.showScale == true) { //pokazuj kolory skalowalne
							double tranNumber = transColors.get(i).firedInCluster;
							Color tranColor = transColors.get(i).colorFiredScale;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "");
						} else { //pokazuj kolory z krokiem 10%
							double tranNumber = transColors.get(i).firedInCluster;
							Color tranColor = transColors.get(i).colorFiredGrade;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "");
						}
					} else { //pokazuj tylko liczbę wystąpień jako część inwariantów
						if(clusterColorsData.showScale == true) { //pokazuj kolory skalowalne
							int tranNumber = transColors.get(i).transInCluster;
							Color tranColor = transColors.get(i).colorTransScale;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "");
						} else { //pokazuj kolory z krokiem 10%
							int tranNumber = transColors.get(i).transInCluster;
							Color tranColor = transColors.get(i).colorTransGrade;
							holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "");
						}
					}
				}
				int trInCluster = transColors.get(i).transInCluster;
				double firedInCluster = transColors.get(i).firedInCluster; // ????
				if(trInCluster>0) {
					String t1 = Tools.setToSize("t"+(i), 5, false);
					String t2 = Tools.setToSize("Freq.: "+trInCluster, 12, false);
					String t3 = Tools.setToSize("Fired: "+formatD(firedInCluster), 15, false);
					String txt = t1 + t2 + t3 + " ; "+holyVector.get(i).getName();
					clTextArea.append(txt+"\n");
				}
			}
			clTextArea.setCaretPosition(0);
		}
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
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
	//TODO:
	/**
	 * Konstruktor podokna zbiorów MCS.
	 * @param mcsData MCSDataMatrix - obiekt danych zbiorów MCS
	 */
	public AbyssDockWindowsTable(MCSDataMatrix mcsData)
	{
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
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
					MCSDataMatrix mcsDataCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
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
					//MCSDataMatrix mcsDataCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
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
		//generateButton.setMargin(new Insets(0, 0, 0, 0));
		//generateButton.setIcon(Tools.getResIcon32("/icons/mcsWindow/computeData.png"));
		refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
				if(transitions.size() == 0)
					return;
					
				String[] objRset = new String[transitions.size() + 1];
				objRset[0] = "---";
				for (int i = 0; i < transitions.size(); i++) {
					objRset[i + 1] = "t"+i+"_"+transitions.get(i).getName();
				}
				stopAction = true;
				
				//GUIManager.getDefaultGUIManager().getMCSBox().getCurrentDockWindow().mcsObjRCombo.removeAllItems();
				//GUIManager.getDefaultGUIManager().getMCSBox().getCurrentDockWindow().mcsObjRCombo = new JComboBox<String>(objRset);
				mcsObjRCombo.removeAllItems();
				for(String str : objRset) {
					mcsObjRCombo.addItem(str);
				}
				//mcsObjRCombo.removeAllItems();
				//mcsObjRCombo = new JComboBox<String>(objRset);
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
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();
			
			
			sets = sets.replace("[", "");
			sets = sets.replace("]", "");
			sets = sets.replace(" ", "");
			
			String[] elements = sets.split(",");
			ArrayList<Integer> invIDs = new ArrayList<Integer>();
			
			for(String el : elements) {
				invIDs.add(Integer.parseInt(el));
			}
			
			Transition trans_TMP = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(objReactionID);
			trans_TMP.setColorWithNumber(true, Color.red, false, -1, false, "");
			
			for(int id : invIDs) {
				trans_TMP= GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(id);
				trans_TMP.setColorWithNumber(true, Color.black, false, -1, false, "");
				//double tranNumber = transColors.get(i).firedInCluster;
				//Color tranColor = transColors.get(i).colorFiredScale;
				//holyVector.get(i).setColorWithNumber(true, tranColor, tranNumber);
			}
			
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
		} catch (Exception e) {
			
		}
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************                  ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	/**
	 * 
	 * @param knockoutData ArrayList[ArrayList[Integer]] - macierz danych o knockout
	 * @param type1 - nie ważne co, ważne że wyróżnia konstruktor
	 * @param type2 - nie ważne co, ważne że wyróżnia konstruktor
	 * @param type3 - nie ważne co, ważne że wyróżnia konstruktor
	 */
	public AbyssDockWindowsTable(ArrayList<ArrayList<Integer>> knockoutData, boolean type1, int type2, boolean type3)
	{
		//TODO:
		if(knockoutData == null || knockoutData.size() == 0) {
			knockoutData = null;
			return;
		} else {
			mode = KNOCKOUT;
			this.knockoutData = knockoutData;
			//GUIManager.getDefaultGUIManager().reset.setMCTStatus(true);
		}
		
		int colA_posX = 10;
		int colB_posX = 100;
		int positionY = 10;
		
		initiateContainers();
		
		//MCT - obliczenia:
		MCTCalculator analyzer = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		mct = MCTCalculator.getSortedMCT(mct);
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
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
		
		int w = GUIManager.getDefaultGUIManager().getMctBox().getWidth();
		int h = GUIManager.getDefaultGUIManager().getMctBox().getHeight();
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
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();
		
		if(showOrClear == true)
		{
			ArrayList<Integer> idToShow = knockoutData.get(knockIndex);
			Transition trans_TMP;
			ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			
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
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	//**************************************************************************************
	//*********************************                  ***********************************
	//*********************************                  ***********************************
	//*********************************                  ***********************************
	//**************************************************************************************

	

	/**
	 * Konstruktor 
	 * @param is
	 */
	public AbyssDockWindowsTable(InvariantsSimulator is)
	{

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
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(elementLocation.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
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
	 * @param x int - nowe EFT
	 */
	private void setMinFireTime(double x) {
		if (mode == TIMETRANSITION) {
			Transition transition = (Transition) element;
			transition.setMinFireTime(x);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda ustawia nową wartość czasu LFT dla tranzycji czasowej.
	 * @param x int - nowe LFT
	 */
	private void setMaxFireTime(double x) {
		if (mode == TIMETRANSITION) {
			Transition transition = (Transition) element;
			transition.setMaxFireTime(x);
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
		
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(el.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		
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
		
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(el.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		
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
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
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
			@SuppressWarnings("unused")
			Node node = (Node) element;
		}
	}

	private void unPortal() {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			@SuppressWarnings("unused")
			Node node = (Node) element;
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
