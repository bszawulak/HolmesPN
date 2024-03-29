package holmes.darkgui.dockwindows;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

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

import holmes.analyse.*;
import holmes.clusters.ClusterDataPackage;
import holmes.clusters.ClusterTransition;
import holmes.darkgui.GUIManager;
import holmes.graphpanel.EditorResources;
import holmes.graphpanel.GraphPanel;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SPNtransitionData;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.PetriNetElement;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.simulators.NetSimulator;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.petrinet.simulators.QuickSimTools;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;
import holmes.windows.HolmesFunctionsBuilder;
import holmes.windows.HolmesInvariantsViewer;
import holmes.windows.HolmesNotepad;
import holmes.windows.managers.HolmesStatesManager;
import holmes.windows.ssim.HolmesSimSetup;
import holmes.workspace.WorkspaceSheet;
import jxl.biff.drawing.ComboBox;

/**
 * Klasa zawierająca szczegóły interfejsu podokien dokowalnych programu.
 *
 * @author students<br>
 * @author MR<br>
 * <br>
 * <b>Absolute positioning. Of almost absolute everything here.</b><br>
 * Nie obchodzi mnie, co o tym myślisz (╯゜Д゜）╯︵ ┻━┻) . Idź w layout i nie wracaj. ┌∩┐(◣_◢)┌∩┐
 */
public class HolmesDockWindowsTable extends JPanel {
    private static final long serialVersionUID = 4510802239873443705L;
    private GUIManager overlord;
    private ArrayList<JComponent> components;
    private int mode;
    private ArrayList<Transition> transitions; // j.w.
    private ArrayList<Place> places;
    private ArrayList<ArrayList<Transition>> mctGroups; //używane tylko w przypadku, gdy obiekt jest typu DockWindowType.MctANALYZER
    private ArrayList<ArrayList<Integer>> knockoutData;

    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }

    // Containers & general use
    private JPanel panel; // główny panel okna
    private boolean stopAction = false;
    public boolean doNotUpdate = false;
    //simulator:
    public ButtonGroup group = new ButtonGroup();
    private JSpinner spiner = new JSpinner();
    public JComboBox<String> simMode;
    private JCheckBox maximumModeCheckBox;
    private JCheckBox singleModeCheckBox;
    public JCheckBox functionalModeCheckBox;
    public JLabel timeStepLabelValue;
    private NetSimulator simulator;  // obiekt symulatora
    // P/T/M/A
    private ButtonGroup groupRadioMetaType = new ButtonGroup();  //metanode
    private boolean nameLocChangeMode = false;
    private PetriNetElement element;
    private ElementLocation elementLocation;
    public SpinnerModel nameLocationXSpinnerModel = null;
    public SpinnerModel nameLocationYSpinnerModel = null;
    private Arc pairedArc = null;
    private JCheckBox tpnCheckBox;
    private JCheckBox pnCheckBox;
    //MCT:
    private int selectedMCTindex = -1;
    private boolean colorMCT = false;
    private boolean allMCTselected = false;
    private JTextArea MCTnameField;
    //knockout:
    private JTextArea knockoutTextArea;
    //t-invariants:
    private JComboBox<String> chooseInvBox;
    private JComboBox<String> chooseSurInvBox;
    private JComboBox<String> chooseSubInvBox;
    private JComboBox<String> chooseNoneInvBox;
    private ArrayList<ArrayList<Integer>> t_invariantsMatrix; //używane w podoknie t-inwariantów
    private int selectedT_invIndex = -1;
    private boolean markMCT = false;
    private boolean glowT_inv = true;
    private JTextArea t_invNameField;
    //t-invariants:
    private ArrayList<ArrayList<Integer>> p_invariantsMatrix; //używane w podoknie p-inwariantów
    private int selectedP_invIndex = -1;
    private JTextArea p_invNameField;
    private boolean invStructure = true;
    private JLabel minTimeLabel;
    private JLabel avgTimeLabel;
    private JLabel maxTimeLabel;
    private JLabel structureLabel;
    //clusters:
    private JComboBox<String> chooseCluster;
    private JComboBox<String> chooseClusterInv;
    private ClusterDataPackage clusterColorsData;
    private int selectedClusterIndex = -1;
    private int selectedClusterInvIndex = -1;
    private boolean clustersMCT = false;
    private JProgressBar progressBar = null;
    private JLabel mssValueLabel;
    //MCS
    private JComboBox<String> mcsObjRCombo;
    private JComboBox<String> mcsMCSforObjRCombo;
    //sheets:
    private WorkspaceSheet currentSheet;
    //fixer:
    public JLabel fixInvariants;
    public JLabel fixInvariants2;
    public JLabel fixIOPlaces;
    public JLabel fixIOTransitions;
    public JLabel fixlinearTrans;
    private ProblemDetector detector;
    //quickSim:
    private QuickSimTools quickSim;
    private boolean scanTransitions = true;
    private boolean scanPlaces = true;
    private boolean markArcs = true;
    private boolean repetitions = true;
    private JProgressBar quickProgressBar;

    //deco
    private int choosenDeco = 0;
    private JTextArea elementsOfDecomposedStructure;
    private int selectedSubNetindex = -1;
    private boolean colorSubNet = false;
    private boolean allSubNetsselected = false;

    // modes
    private static final int PLACE = 0;
    private static final int TRANSITION = 1;
    private static final int ARC = 2;
    private static final int SHEET = 3;
    private static final int SIMULATOR = 4;
    private static final int tINVARIANTS = 5;
    private static final int MCT = 6;
    private static final int TIMETRANSITION = 7;
    private static final int pINVARIANTS = 8;
    private static final int CLUSTERS = 9;
    private static final int KNOCKOUT = 10;
    private static final int META = 11;
    private static final int CTRANSITION = 12;
    private static final int DECOMPOSITION = 13;

    public enum SubWindow {
        SIMULATOR, PLACE, TRANSITION, TIMETRANSITION, CTRANSITION, META, ARC, SHEET, T_INVARIANTS, P_INVARIANTS,
        MCT, CLUSTERS, KNOCKOUT, MCS, FIXER, QUICKSIM, DECOMPOSITION
    }

    /**
     * Konstruktor główny, wybierający odpowiednią metodę do tworzenia podokna wybranego typu
     *
     * @param subType  SubWindow - typ podokna do utworzenia
     * @param blackBox Object[...] - bliżej nieokreślona lista nieokreślonych parametrów :)
     */
    @SuppressWarnings("unchecked")
    public HolmesDockWindowsTable(SubWindow subType, Object... blackBox) {
        overlord = GUIManager.getDefaultGUIManager();

        switch (subType) {

            case SIMULATOR:
                createSimulatorSubWindow((NetSimulator) blackBox[0]);
                break;
            case PLACE:
                createPlaceSubWindow((Place) blackBox[0], (ElementLocation) blackBox[1]);
                break;
            case TRANSITION:
                createTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
                break;
            case TIMETRANSITION:
                createTimeTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
                break;
            case CTRANSITION:
                createColorTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
                break;
            case META:
                createMetaNodeSubWindow((MetaNode) blackBox[0], (ElementLocation) blackBox[1]);
                break;
            case ARC:
                createArcSubWindow((Arc) blackBox[0]);
                break;
            case SHEET:
                createSheetSubWindow((WorkspaceSheet) blackBox[0]);
                break;
            case T_INVARIANTS:
                createT_invSubWindow((ArrayList<ArrayList<Integer>>) blackBox[0]);
                break;
            case P_INVARIANTS:
                createP_invSubWindow((ArrayList<ArrayList<Integer>>) blackBox[0]);
                break;
            case MCT:
                createMCTSubWindow((ArrayList<ArrayList<Transition>>) blackBox[0]);
                break;
            case CLUSTERS:
                createClustersSubWindow((ClusterDataPackage) blackBox[0]);
                break;
            case MCS:
                createMCSSubWindow((MCSDataMatrix) blackBox[0]);
                break;
            case FIXER:
                createFixerSubWindow();
                break;
            case QUICKSIM:
                createQuickSimSubWindow();
                break;
            case KNOCKOUT:
                createKnockoutData((ArrayList<ArrayList<Integer>>) blackBox[0]);
                break;
            case DECOMPOSITION:
                createDecompositionData();
                break;
        }

    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************    SYMULATOR     ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda pomocnicza konstruktora odpowiedzialna za tworzenie podokna dla symulatora sieci.
     *
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

        String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode", "Color"};
        mode = SIMULATOR;
        setSimulator(sim);

        // SIMULATION MODE
        JLabel netTypeLabel = new JLabel("Mode:");
        netTypeLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
        components.add(netTypeLabel);

        simMode = new JComboBox<>(simModeName);
        simMode.setLocation(columnB_posX - 30, columnB_Y += 10);
        simMode.setSize(colBCompLength + 30, 20);
        simMode.setSelectedIndex(0);
        simMode.addActionListener(actionEvent -> {
            if (doNotUpdate)
                return;

            int selectedModeIndex = simMode.getSelectedIndex();
            int change = simulator.setSimNetType(selectedModeIndex);
            doNotUpdate = true;
            if (change == 0) {
                simMode.setSelectedIndex(0);
            } else if (change == 1) {
                simMode.setSelectedIndex(1);
            } else if (change == 2) {
                simMode.setSelectedIndex(2);
            } else if (change == 3) {
                simMode.setSelectedIndex(3);
            } else {
                overlord.log("Error while changing graphical simulator mode.", "error", true);
            }
            doNotUpdate = false;
        });
        components.add(simMode);

        JLabel timeStepLabel = new JLabel("Time/step:");
        timeStepLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(timeStepLabel);

        timeStepLabelValue = new JLabel("0");
        timeStepLabelValue.setBounds(columnA_posX + 70, columnB_Y += 20, colACompLength, 20);
        components.add(timeStepLabelValue);

        // SIMULATOR CONTROLS
        // metoda startSimulation obiektu simulator troszczy się o wygaszanie
        // i aktywowanie odpowiednich przycisków
        JLabel controlsLabel = new JLabel("Simulation options:");
        controlsLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength * 2, 20);
        components.add(controlsLabel);
        columnB_Y += 20;

        JButton oneActionBack = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_back.png"));
        oneActionBack.setName("simB1");
        oneActionBack.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 30);
        oneActionBack.setToolTipText("One action back");
        oneActionBack.addActionListener(actionEvent -> {
            overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
            simulator.startSimulation(SimulatorMode.ACTION_BACK);
            mode = SIMULATOR;
        });
        components.add(oneActionBack);

        JButton oneTransitionForward = new JButton(
                Tools.getResIcon22("/icons/simulation/control_sim_fwd.png"));
        oneTransitionForward.setName("simB2");
        oneTransitionForward.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 30);
        oneTransitionForward.setToolTipText("One transition forward");
        oneTransitionForward.addActionListener(actionEvent -> {
            overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
            simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION);
            mode = SIMULATOR;
        });
        components.add(oneTransitionForward);

        JButton loopBack = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_backLoop.png"));
        loopBack.setName("simB3");
        loopBack.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
        loopBack.setToolTipText("Loop back to oldest saved action");
        loopBack.addActionListener(actionEvent -> {
            overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
            simulator.startSimulation(SimulatorMode.LOOP_BACK);
            mode = SIMULATOR;
        });
        components.add(loopBack);
        JButton oneStepForward = new JButton(
                Tools.getResIcon22("/icons/simulation/control_sim_fwdLoop.png"));
        oneStepForward.setName("simB4");
        oneStepForward.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
        oneStepForward.setToolTipText("One step forward");
        oneStepForward.addActionListener(actionEvent -> {
            overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
            simulator.startSimulation(SimulatorMode.STEP);
            mode = SIMULATOR;
        });
        components.add(oneStepForward);

        JButton loopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_loop.png"));
        loopSimulation.setName("simB5");
        loopSimulation.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
        loopSimulation.setToolTipText("Loop simulation");
        loopSimulation.addActionListener(actionEvent -> {
            overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
            simulator.startSimulation(SimulatorMode.LOOP);
            mode = SIMULATOR;
        });
        components.add(loopSimulation);

        JButton singleTransitionLoopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_1transLoop.png"));
        singleTransitionLoopSimulation.setName("simB6");
        singleTransitionLoopSimulation.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
        singleTransitionLoopSimulation.setToolTipText("Loop single transition simulation");
        singleTransitionLoopSimulation.addActionListener(actionEvent -> {
            overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
            simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
            mode = SIMULATOR;
        });
        components.add(singleTransitionLoopSimulation);

        JButton pauseSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_pause.png"));
        pauseSimulation.setName("stop");
        pauseSimulation.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
        pauseSimulation.setToolTipText("Pause simulation");
        pauseSimulation.setEnabled(false);
        pauseSimulation.addActionListener(actionEvent -> {
            overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
            simulator.pause();
            mode = SIMULATOR;
        });
        components.add(pauseSimulation);

        JButton stopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_stop.png"));
        stopSimulation.setName("stop");
        stopSimulation.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
        stopSimulation.setToolTipText("Schedule a stop for the simulation");
        stopSimulation.setEnabled(false);
        stopSimulation.addActionListener(actionEvent -> {
            simulator.stop();
            mode = SIMULATOR;
        });
        components.add(stopSimulation);

        JButton resetButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_reset.png"));
        resetButton.setName("reset");
        resetButton.setBounds(columnA_posX, columnB_Y += 30, colACompLength, 30);
        resetButton.setToolTipText("Reset all tokens in places.");
        resetButton.setEnabled(false);
        resetButton.addActionListener(actionEvent -> overlord.getWorkspace().getProject().restoreMarkingZero());
        components.add(resetButton);

        JButton saveButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_save_m0.png"));
        saveButton.setName("Save m0");
        saveButton.setBounds(columnB_posX, columnA_Y += 30, colBCompLength, 30);
        saveButton.setToolTipText("Save m0 state.");
        saveButton.addActionListener(actionEvent -> {
            if (overlord.reset.isSimulatorActiveWarning(
                    "Operation impossible while simulator is working.", "Warning"))
                return;

            Object[] options = {"Save new m0 state", "Cancel",};
            int n = JOptionPane.showOptionDialog(null,
                    "Add new net state to states table?",
                    "Saving m0 state", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (n == 0) {
                overlord.getWorkspace().getProject().accessStatesManager().addCurrentState();
            }
        });
        components.add(saveButton);

        JButton c1Button = new JButton("Store");
        c1Button.setName("reset");
        c1Button.setBounds(columnA_posX, columnB_Y += 30, colACompLength, 30);
        c1Button.setToolTipText("Reset all tokens in places.");
        c1Button.setEnabled(true);
        c1Button.addActionListener(actionEvent -> overlord.getWorkspace().getProject().storeColors());
        components.add(c1Button);

        JButton c2Button = new JButton("Reset");
        c2Button.setName("reset");
        c2Button.setBounds(columnB_posX, columnA_Y += 30, colBCompLength, 30);
        c2Button.setToolTipText("Reset all tokens in places.");
        c2Button.setEnabled(true);
        c2Button.addActionListener(actionEvent -> overlord.getWorkspace().getProject().restoreColors());
        components.add(c2Button);

        JButton statesButton = new JButton("State manager");
        statesButton.setName("State manager");
        statesButton.setBounds(columnA_posX, columnB_Y += 35, colACompLength * 2, 30);
        statesButton.setToolTipText("Open states manager window.");
        statesButton.setEnabled(true);
        statesButton.addActionListener(actionEvent -> {
            if (overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
                JOptionPane.showMessageDialog(null, "Net simulator must be stopped in order to access state manager.",
                        "Simulator working", JOptionPane.WARNING_MESSAGE);
            } else {
                new HolmesStatesManager();
            }
        });
        components.add(statesButton);

        columnB_Y += 35;
        columnA_Y += 35;
        //doNotUpdate = false;
        maximumModeCheckBox = new JCheckBox("Maximum mode");
        maximumModeCheckBox.setBounds(columnA_posX, columnA_Y += 30, 200, 20);
        maximumModeCheckBox.addActionListener(actionEvent -> {
            if (doNotUpdate)
                return;

            if (singleModeCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(null, "Mode overrided by an active single mode.",
                        "Cannot change now", JOptionPane.WARNING_MESSAGE);
                doNotUpdate = true;
                maximumModeCheckBox.setSelected(overlord.getSettingsManager().getValue("simSingleMode").equals("1"));
                doNotUpdate = false;
            }

            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            simulator.setMaxMode(abstractButton.getModel().isSelected());
        });
        components.add(maximumModeCheckBox);

        singleModeCheckBox = new JCheckBox("Single mode");
        singleModeCheckBox.setBounds(columnA_posX, columnA_Y += 20, 200, 20);
        singleModeCheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            if (abstractButton.getModel().isSelected()) {
                simulator.setSingleMode(true);
                doNotUpdate = true;
                maximumModeCheckBox.setSelected(overlord.getSettingsManager().getValue("simSingleMode").equals("1"));
                doNotUpdate = false;
            } else {
                simulator.setSingleMode(false);

                doNotUpdate = true;
                maximumModeCheckBox.setSelected(false);
                simulator.setMaxMode(false);
                doNotUpdate = false;
            }
        });
        components.add(singleModeCheckBox);

        //PANEL SYMULATORA INWARIANTÓW
        JPanel invariantsSimulatorPanel = new JPanel();

        invariantsSimulatorPanel.setLayout(null);
        invariantsSimulatorPanel.setBorder(BorderFactory.createTitledBorder("Invariants simulator"));
        invariantsSimulatorPanel.setBounds(columnA_posX - 5, columnA_Y += 20, 160, 160);

        int internalXA = 10;
        int internalXB = 60;
        int internalY = 20;

        JLabel simTypeLabel = new JLabel("Mode:");
        simTypeLabel.setBounds(internalXA, internalY, 50, 20);
        invariantsSimulatorPanel.add(simTypeLabel);

        JRadioButton TimeMode = new JRadioButton("Time Mode");
        TimeMode.setBounds(internalXB, internalY, 90, 20);
        //TimeMode.setLocation(internalXB, internalY);
        internalY += 20;
        TimeMode.setSize(90, 20);
        TimeMode.setActionCommand("0");
        invariantsSimulatorPanel.add(TimeMode);
        group.add(TimeMode);

        columnA_Y += 20;
        JRadioButton StepMode = new JRadioButton("Step Mode");
        StepMode.setBounds(internalXB, internalY, 90, 20);
        //StepMode.setLocation(internalXB, internalY);
        internalY += 20;
        StepMode.setSize(90, 20);
        StepMode.setActionCommand("1");
        invariantsSimulatorPanel.add(StepMode);
        group.add(StepMode);

        JRadioButton CycleMode = new JRadioButton("Cycle Mode");
        CycleMode.setBounds(internalXB, internalY, 90, 20);
        //CycleMode.setLocation(internalXB, internalY);
        internalY += 20;
        CycleMode.setSize(90, 20);
        CycleMode.setActionCommand("2");
        invariantsSimulatorPanel.add(CycleMode);
        group.add(CycleMode);
        group.setSelected(TimeMode.getModel(), true);


        JLabel timeLabel = new JLabel("Time (min):");
        timeLabel.setBounds(internalXA, internalY, 70, 20);
        invariantsSimulatorPanel.add(timeLabel);

        SpinnerModel timeCycle = new SpinnerNumberModel(1, 1, 9999, 1);
        spiner = new JSpinner(timeCycle);
        spiner.setLocation(internalXB + 20, internalY + 3);
        spiner.setSize(70, 20);
        internalY += 25;
        invariantsSimulatorPanel.add(spiner);

        // INVARIANTS SIMULATION START BUTTON
        //JButton startButton = new JButton("Start");
        JButton startButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_fwd.png"));
        startButton.setBounds(internalXA, internalY, 80, 30);
        //startButton.setLocation(internalXA, internalY);
        //startButton.setSize(80, 40);
        startButton.addActionListener(actionEvent -> {
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
        });

        TimeMode.setEnabled(false);
        StepMode.setEnabled(false);
        CycleMode.setEnabled(false);
        spiner.setEnabled(false);
        startButton.setEnabled(false);
        invariantsSimulatorPanel.add(startButton);
        //components.add(invariantsSimulatorPanel);

        panel.setLayout(null);
        for (JComponent component : components) {
            panel.add(component);
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
     *
     * @param place    Place - obiekt miejsca
     * @param location ElementLocation - lokalizacja miejsca
     */
    private void createPlaceSubWindow(Place place, ElementLocation location) {
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
        idLabel3.setBounds(columnB_posX + 35, columnA_Y, 50, 20);
        components.add(idLabel3);
        JLabel idLabel4 = new JLabel(place.getID() + "");
        idLabel4.setBounds(columnB_posX + 60, columnB_Y, 50, 20);
        idLabel4.setFont(normalFont);
        components.add(idLabel4);

        // NAME
        JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
        nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(nameLabel);

        JFormattedTextField nameField = new JFormattedTextField();
        nameField.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        nameField.setText(place.getName());
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
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
                if (field != null)
                    newComment = field.getText();
                changeComment(newComment);
                overlord.markNetChange();
            }
        });

        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);

        // PLACE TOKEN
        if (!place.isColored) {
            JLabel tokenLabel = new JLabel("Tokens:", JLabel.LEFT);
            tokenLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
            components.add(tokenLabel);
            int tok = place.getTokensNumber();
            boolean problem = false;
            if (tok < 0) {
                overlord.log("Negative number of tokens in " + place.getName(), "error", true);
                tok = 0;
                problem = true;
            }
            SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(tok, 0, Integer.MAX_VALUE, 1);
            JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
            tokenSpinner.setBounds(columnB_posX, columnB_Y += 20, 95, 20);
            tokenSpinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setTokens(tokenz);
                overlord.markNetChange();
            });
            if (problem)
                tokenSpinner.setEnabled(false);
            components.add(tokenSpinner);
        }


        //SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        //ZOOM:
        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // PORTAL
        JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
        portalLabel.setBounds(columnB_posX + 110, columnB_Y, colACompLength, 20);
        components.add(portalLabel);
        JCheckBox portalBox = new JCheckBox("", place.isPortal());
        portalBox.setBounds(columnB_posX + 180, columnB_Y, colACompLength, 20);
        portalBox.setSelected(((Place) element).isPortal());
        portalBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                makePortal();
            } else {
                if (((Place) element).getElementLocations().size() > 1)
                    JOptionPane.showMessageDialog(null, "Place contains more than one location!", "Cannot proceed",
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
        });
        components.add(portalBox);

        //LOKALIZACJA:
        JLabel locLabel = new JLabel("Location:", JLabel.LEFT);
        locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(locLabel);
        SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
        SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);

        JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
        locationXSpinner.addChangeListener(e -> setX((int) ((JSpinner) e.getSource()).getValue()));
        JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
        locationYSpinner.addChangeListener(e -> setY((int) ((JSpinner) e.getSource()).getValue()));
        if (zoom != 100) {
            locationXSpinner.setEnabled(false);
            locationYSpinner.setEnabled(false);
        }
        JPanel locationSpinnerPanel = new JPanel();
        locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel, BoxLayout.X_AXIS));
        locationSpinnerPanel.add(locationXSpinner);
        locationSpinnerPanel.add(new JLabel(" , "));
        locationSpinnerPanel.add(locationYSpinner);

        locationSpinnerPanel.setBounds(columnA_posX + 90, columnB_Y += 20, colBCompLength, 20);
        components.add(locationSpinnerPanel);


        // WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = place.getElementLocations().indexOf(location);
        int xNameOffset = place.getNamesLocations().get(locationIndex).getPosition().x;
        int yNameOffset = place.getNamesLocations().get(locationIndex).getPosition().y;

        nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
        nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);

        JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
        locNameLabelX.setBounds(columnA_posX + 90, columnA_Y, 40, 20);
        components.add(locNameLabelX);

        JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
        nameLocationXSpinner.setBounds(columnA_posX + 125, columnA_Y, 60, 20);
        nameLocationXSpinner.addChangeListener(new ChangeListener() {
            private Place place_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetX((int) ((JSpinner) e.getSource()).getValue(), place_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationXSpinnerModel.setValue(res.x);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Place inPlace, ElementLocation inLoc) {
                place_tmp = inPlace;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(place, location));

        components.add(nameLocationXSpinner);

        JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
        locNameLabelY.setBounds(columnA_posX + 195, columnB_Y, 40, 20);
        components.add(locNameLabelY);

        JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
        nameLocationYSpinner.setBounds(columnA_posX + 230, columnA_Y, 60, 20);
        nameLocationYSpinner.addChangeListener(new ChangeListener() {
            private Place place_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetY((int) ((JSpinner) e.getSource()).getValue(), place_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationYSpinnerModel.setValue(res.y);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Place inPlace, ElementLocation inLoc) {
                place_tmp = inPlace;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(place, location));
        components.add(nameLocationYSpinner);

        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnA_posX + 90, columnA_Y += 25, 150, 40);
        nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
        nameLocChangeButton.setFocusPainted(false);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Place place_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                JButton button_tmp = (JButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(place_tmp, el_tmp, true);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, false);
                }
            }

            private ActionListener yesWeCan(Place inPlace, ElementLocation inLoc) {
                place_tmp = inPlace;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(place, location));
        components.add(nameLocChangeButton);

        //COLORS:
        //TODO
        if (place.isColored) {
            // PLACE TOKEN
            JLabel tokenLabel = new JLabel("T0 Red:", JLabel.LEFT);
            tokenLabel.setBounds(columnA_posX, columnA_Y += 50, colACompLength, 20);
            components.add(tokenLabel);
            int tok0 = place.getTokensNumber();
            boolean problem = false;
            if (tok0 < 0) {
                overlord.log("Negative number of tokens in " + place.getName(), "error", true);
                tok0 = 0;
                problem = true;
            }
            SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(tok0, 0, Integer.MAX_VALUE, 1);
            JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
            tokenSpinner.setBounds(columnB_posX - 30, columnB_Y += 75, 75, 20);
            tokenSpinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setTokens(tokenz);
                overlord.markNetChange();
            });
            if (problem)
                tokenSpinner.setEnabled(false);
            components.add(tokenSpinner);

            JLabel token3Label = new JLabel("T3 Yellow:", JLabel.LEFT);
            token3Label.setBounds(columnB_posX + 60, columnB_Y, colACompLength, 20);
            components.add(token3Label);
            int tok3 = place.getColorTokensNumber(3);

            SpinnerModel token3SpinnerModel = new SpinnerNumberModel(tok3, 0, Integer.MAX_VALUE, 1);
            JSpinner token3Spinner = new JSpinner(token3SpinnerModel);
            token3Spinner.setBounds(columnB_posX + 130, columnB_Y, 75, 20);
            token3Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 3);
                overlord.markNetChange();
            });
            components.add(token3Spinner);


            JLabel token1Label = new JLabel("T1 Green:", JLabel.LEFT);
            token1Label.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
            components.add(token1Label);
            int tok1 = place.getColorTokensNumber(1);

            SpinnerModel token1SpinnerModel = new SpinnerNumberModel(tok1, 0, Integer.MAX_VALUE, 1);
            JSpinner token1Spinner = new JSpinner(token1SpinnerModel);
            token1Spinner.setBounds(columnB_posX - 30, columnB_Y += 20, 75, 20);
            token1Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 1);
                overlord.markNetChange();
            });
            components.add(token1Spinner);

            JLabel token4Label = new JLabel("T4 Grey:", JLabel.LEFT);
            token4Label.setBounds(columnB_posX + 60, columnB_Y, colACompLength, 20);
            components.add(token4Label);
            int tok4 = place.getColorTokensNumber(4);

            SpinnerModel token4SpinnerModel = new SpinnerNumberModel(tok4, 0, Integer.MAX_VALUE, 1);
            JSpinner token4Spinner = new JSpinner(token4SpinnerModel);
            token4Spinner.setBounds(columnB_posX + 130, columnB_Y, 75, 20);
            token4Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 4);
                overlord.markNetChange();
            });
            components.add(token4Spinner);

            JLabel token2Label = new JLabel("T2 Blue:", JLabel.LEFT);
            token2Label.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
            components.add(token2Label);
            int tok2 = place.getColorTokensNumber(2);

            SpinnerModel token2SpinnerModel = new SpinnerNumberModel(tok2, 0, Integer.MAX_VALUE, 1);
            JSpinner token2Spinner = new JSpinner(token2SpinnerModel);
            token2Spinner.setBounds(columnB_posX - 30, columnB_Y += 20, 75, 20);
            token2Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 2);
                overlord.markNetChange();
            });
            components.add(token2Spinner);

            JLabel token5Label = new JLabel("T5 Black:", JLabel.LEFT);
            token5Label.setBounds(columnB_posX + 60, columnB_Y, colACompLength, 20);
            components.add(token5Label);
            int tok5 = place.getColorTokensNumber(5);

            SpinnerModel token5SpinnerModel = new SpinnerNumberModel(tok5, 0, Integer.MAX_VALUE, 1);
            JSpinner token5Spinner = new JSpinner(token5SpinnerModel);
            token5Spinner.setBounds(columnB_posX + 130, columnB_Y, 75, 20);
            token5Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 5);
                overlord.markNetChange();
            });
            components.add(token5Spinner);
        }

        JLabel debugModeLabel1 = new JLabel("Debug1:", JLabel.LEFT);
        debugModeLabel1.setBounds(columnA_posX, columnA_Y += 45, colACompLength, 20);
        components.add(debugModeLabel1);

        JTextArea debugChangeID = new JTextArea("");
        debugChangeID.setBounds(columnB_posX, columnB_Y += 70, 60, 20);
        debugChangeID.setLineWrap(true);
        debugChangeID.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newFR = "";
                if (field != null)
                    newFR = field.getText();

                try {
                    int id = Integer.parseInt(newFR);

                    ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
                    if (id >= 0 && id < places.size()) {

                        Place p1 = (Place) element;
                        Place p2 = places.get(id);
                        if (places.indexOf(p1) == id)
                            return;

                        int pos1 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(p1);
                        int pos2 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(p2);
                        Collections.swap(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes(), pos1, pos2);
                        GUIManager.getDefaultGUIManager().log("Swapping places " + p1.getName() + " and " + p2.getName() + " successfull.", "text", true);

                        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
                        //ElementLocation loc1st = ((Transition)element).getElementLocations().get(0);
                        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation); //zaznacz element
                    }
                } catch (Exception ee) {
                    System.out.println(ee.getMessage());
                }
            }
        });
        components.add(debugChangeID);
        JButton aaa = new JButton();
        aaa.setName("ChangeID");
        aaa.setText("Change ID");
        aaa.setMargin(new Insets(0, 0, 0, 0));
        aaa.setBounds(columnB_posX + 70, columnB_Y, 80, 20);
        aaa.addActionListener(actionEvent -> {
        });
        components.add(aaa);
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
     *
     * @param transition Transition - obiekt tranzycji sieci
     * @param location   ElementLocation - lokalizacja tranzycji
     */
    private void createTransitionSubWindow(Transition transition, ElementLocation location) {
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

        final int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transition);
        JLabel idLabel2 = new JLabel(Integer.toString(gID));
        idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
        idLabel2.setFont(normalFont);
        components.add(idLabel2);

        JLabel idLabel3 = new JLabel("gID:");
        idLabel3.setBounds(columnB_posX + 35, columnA_Y, 50, 20);
        components.add(idLabel3);
        JLabel idLabel4 = new JLabel(transition.getID() + "");
        idLabel4.setBounds(columnB_posX + 60, columnB_Y, 50, 20);
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
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
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
                if (field != null)
                    newComment = field.getText();
                changeComment(newComment);
                overlord.markNetChange();
            }
        });

        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);

        //CHANGE TYPE:
        JLabel changeTypeLabel = new JLabel("Trans. Type:", JLabel.LEFT);
        changeTypeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(changeTypeLabel);

        pnCheckBox = new JCheckBox("Standard", transition.isPortal());
        pnCheckBox.setBounds(columnB_posX, columnB_Y += 20, 80, 20);
        pnCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.PN);

        pnCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.PN);

                doNotUpdate = true;
                tpnCheckBox.setSelected(false);
                doNotUpdate = false;
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            }
        });
        components.add(pnCheckBox);

        tpnCheckBox = new JCheckBox("Time", transition.isPortal());
        tpnCheckBox.setBounds(columnB_posX + 100, columnB_Y, 80, 20);
        tpnCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.TPN);

        tpnCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.TPN);

                doNotUpdate = true;
                pnCheckBox.setSelected(false);
                doNotUpdate = false;
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            }
        });
        components.add(tpnCheckBox);

        JLabel frLabel = new JLabel("Firing rate:", JLabel.LEFT);
        frLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(frLabel);

        JTextArea frField = new JTextArea("" + ((Transition) element).getFiringRate());
        frField.setBounds(columnB_posX, columnB_Y += 20, 60, 20);
        frField.setLineWrap(true);
        frField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newFR = "";
                if (field != null)
                    newFR = field.getText();

                try {
                    double newVal = Double.parseDouble(newFR);

                    SPNtransitionData xxx = GUIManager.getDefaultGUIManager().getWorkspace().getProject().accessFiringRatesManager()
                            .getCurrentSPNdataVector().accessVector().get(gID);
                    ((Transition) element).setFiringRate(newVal);
                    xxx.ST_function = newFR;
                } catch (Exception ee) {
                    System.out.println(ee.getMessage());
                }
                //changeComment(newComment);
                //overlord.markNetChange();
            }
        });
        components.add(frField);

        //SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        //ZOOM:
        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        //LOKALIZACJA:
        JLabel locLabel = new JLabel("Location:", JLabel.LEFT);
        locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(locLabel);

        SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
        SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);

        JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
        locationXSpinner.addChangeListener(e -> setX((int) ((JSpinner) e.getSource()).getValue()));
        JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
        locationYSpinner.addChangeListener(e -> setY((int) ((JSpinner) e.getSource()).getValue()));
        if (zoom != 100) {
            locationXSpinner.setEnabled(false);
            locationYSpinner.setEnabled(false);
        }
        JPanel locationSpinnerPanel = new JPanel();
        locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel, BoxLayout.X_AXIS));
        locationSpinnerPanel.add(locationXSpinner);
        locationSpinnerPanel.add(new JLabel(" , "));
        locationSpinnerPanel.add(locationYSpinner);

        locationSpinnerPanel.setBounds(columnA_posX + 90, columnB_Y += 20, colBCompLength, 20);
        components.add(locationSpinnerPanel);

        // PORTAL
        JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
        portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(portalLabel);
        JCheckBox portalBox = new JCheckBox("", transition.isPortal());
        portalBox.setBounds(columnB_posX, columnB_Y += 20, 30, 20);
        portalBox.setSelected(((Transition) element).isPortal());

        portalBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                makePortal();
            } else {
                if (((Transition) element).getElementLocations().size() > 1)
                    JOptionPane.showMessageDialog(null, "Transition contains more than one location!", "Cannot proceed",
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
        });
        components.add(portalBox);

        //FUNKCYJNOŚĆ
        JLabel functionLabel = new JLabel("Functional:", JLabel.LEFT);
        functionLabel.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(functionLabel);

        JCheckBox functionalCheckBox = new JCheckBox("", transition.isPortal());
        functionalCheckBox.setBounds(columnB_posX, columnB_Y += 20, 30, 20);
        functionalCheckBox.setSelected(((Transition) element).isFunctional());

        functionalCheckBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            ((Transition) element).setFunctional(box.isSelected());
        });
        components.add(functionalCheckBox);

        JButton functionsEditorButton = new JButton(Tools.getResIcon32("/icons/functionsWindow/functionsIcon.png"));
        functionsEditorButton.setName("Functions editor");
        functionsEditorButton.setText("<html>Functions<br>&nbsp;&nbsp;&nbsp;editor&nbsp;</html>");
        functionsEditorButton.setMargin(new Insets(0, 0, 0, 0));
        functionsEditorButton.setBounds(columnA_posX + 125, columnA_Y - 16, 110, 32);
        functionsEditorButton.addActionListener(actionEvent -> new HolmesFunctionsBuilder((Transition) element));
        components.add(functionsEditorButton);

        // WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().x;
        int yNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().y;

        nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
        nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);

        JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
        locNameLabelX.setBounds(columnA_posX + 90, columnA_Y, 40, 20);
        components.add(locNameLabelX);

        JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
        nameLocationXSpinner.setBounds(columnA_posX + 125, columnA_Y, 60, 20);
        nameLocationXSpinner.addChangeListener(new ChangeListener() {
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;
                Point res = setNameOffsetX((int) ((JSpinner) e.getSource()).getValue(), trans_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationXSpinnerModel.setValue(res.x);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));

        components.add(nameLocationXSpinner);

        JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
        locNameLabelY.setBounds(columnA_posX + 195, columnB_Y, 40, 20);
        components.add(locNameLabelY);

        JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
        nameLocationYSpinner.setBounds(columnA_posX + 230, columnA_Y, 60, 20);
        nameLocationYSpinner.addChangeListener(new ChangeListener() {
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetY((int) ((JSpinner) e.getSource()).getValue(), trans_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationYSpinnerModel.setValue(res.y);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocationYSpinner);

        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnA_posX + 90, columnA_Y += 25, 150, 40);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                JButton button_tmp = (JButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, true);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, false);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocChangeButton);


        JLabel debugModeLabel1 = new JLabel("Debug1:", JLabel.LEFT);
        debugModeLabel1.setBounds(columnA_posX, columnA_Y += 45, colACompLength, 20);
        components.add(debugModeLabel1);

        JTextArea debugChangeID = new JTextArea("");
        debugChangeID.setBounds(columnB_posX, columnB_Y += 70, 60, 20);
        debugChangeID.setLineWrap(true);
        debugChangeID.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newFR = "";
                if (field != null)
                    newFR = field.getText();

                try {
                    int id = Integer.parseInt(newFR);

                    ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
                    if (id >= 0 && id < transitions.size()) {

                        Transition t1 = (Transition) element;
                        Transition t2 = transitions.get(id);
                        if (transitions.indexOf(t1) == id)
                            return;

                        int pos1 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(t1);
                        int pos2 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(t2);
                        Collections.swap(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes(), pos1, pos2);
                        GUIManager.getDefaultGUIManager().log("Swapping transitions " + t1.getName() + " and " + t2.getName() + " successfull.", "text", true);

                        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
                        //ElementLocation loc1st = ((Transition)element).getElementLocations().get(0);
                        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation); //zaznacz element
                    }
                } catch (Exception ee) {
                    System.out.println(ee.getMessage());
                }
            }
        });
        components.add(debugChangeID);

        JButton aaa = new JButton();
        aaa.setName("ChangeID");
        aaa.setText("Change ID");
        aaa.setMargin(new Insets(0, 0, 0, 0));
        aaa.setBounds(columnB_posX + 70, columnB_Y, 80, 20);
        aaa.addActionListener(actionEvent -> {
        });
        components.add(aaa);

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
     *
     * @param transition TimeTransition - obiekt tranzycji czasowej
     * @param location   ElementLocation - lokalizacja tranzycji
     */
    private void createTimeTransitionSubWindow(final Transition transition, ElementLocation location) {
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
        idLabel3.setBounds(columnB_posX + 35, columnA_Y, 50, 20);
        components.add(idLabel3);
        JLabel idLabel4 = new JLabel(transition.getID() + "");
        idLabel4.setBounds(columnB_posX + 60, columnB_Y, 50, 20);
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
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
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
                if (field != null)
                    newComment = field.getText();
                changeComment(newComment);
                overlord.markNetChange();
            }
        });

        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);

        //CHANGE TYPE:
        JLabel changeTypeLabel = new JLabel("Trans. Type:", JLabel.LEFT);
        changeTypeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(changeTypeLabel);

        pnCheckBox = new JCheckBox("Standard", transition.isPortal());
        pnCheckBox.setBounds(columnB_posX, columnB_Y += 20, 80, 20);
        pnCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.PN);

        pnCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.PN);

                doNotUpdate = true;
                tpnCheckBox.setSelected(false);
                doNotUpdate = false;
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            }
        });
        components.add(pnCheckBox);

        tpnCheckBox = new JCheckBox("Time", transition.isPortal());
        tpnCheckBox.setBounds(columnB_posX + 100, columnB_Y, 80, 20);
        tpnCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.TPN);

        tpnCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.TPN);

                doNotUpdate = true;
                pnCheckBox.setSelected(false);
                doNotUpdate = false;
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            }
        });
        components.add(tpnCheckBox);

        // EFT / LFT TIMES:
        JLabel minMaxLabel = new JLabel("EFT / LFT:", JLabel.LEFT);
        minMaxLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(minMaxLabel);
        JFormattedTextField minTimeField = new JFormattedTextField();
        minTimeField.setValue(transition.getEFT());
        minTimeField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            double min = (double) field.getValue();
            setMinFireTime(min);
            overlord.markNetChange();
        });

        JFormattedTextField maxTimeField = new JFormattedTextField();
        maxTimeField.setValue(transition.getLFT());
        maxTimeField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            double max = (double) field.getValue();
            setMaxFireTime(max);
            overlord.markNetChange();
        });

        JPanel minTimeSpinnerPanel = new JPanel();
        minTimeSpinnerPanel.setLayout(new BoxLayout(minTimeSpinnerPanel, BoxLayout.X_AXIS));
        minTimeSpinnerPanel.add(minTimeField);
        minTimeSpinnerPanel.add(new JLabel(" / "));
        minTimeSpinnerPanel.add(maxTimeField);
        minTimeSpinnerPanel.setBounds(columnA_posX + 90, columnB_Y += 20, 200, 20);
        components.add(minTimeSpinnerPanel);

        //DURATION:
        JLabel durationLabel = new JLabel("Duration:", JLabel.LEFT);
        durationLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(durationLabel);
        JFormattedTextField durationField = new JFormattedTextField();
        durationField.setValue(transition.getDPNduration());
        durationField.setBounds(columnA_posX + 90, columnB_Y += 20, 90, 20);
        durationField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
                double time = (double) field.getValue();
                setDurationTime(time);
                overlord.markNetChange();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        });
        components.add(durationField);

        //columnA_Y+=40;
        JCheckBox tpnBox = new JCheckBox("TPN active", transition.getTPNstatus());
        tpnBox.setBounds(columnB_posX - 5, columnB_Y += 20, 100, 20);
        tpnBox.setEnabled(true);
        tpnBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            setTPNstatus(box.isSelected());

            overlord.markNetChange();
        });
        components.add(tpnBox);

        columnA_Y += 20;
        JCheckBox dpnBox = new JCheckBox("DPN active", transition.getDPNstatus());
        dpnBox.setBounds(columnB_posX + 100, columnB_Y, 100, 20);
        dpnBox.setEnabled(true);
        dpnBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            setDPNstatus(box.isSelected());

            overlord.markNetChange();
        });
        components.add(dpnBox);

        // T-TRANSITION SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord
                .getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, 100, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // T-TRANSITION LOCATION:
        JLabel comLabel2 = new JLabel("Location:", JLabel.LEFT);
        comLabel2.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(comLabel2);

        SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
        SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
        JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
        locationXSpinner.addChangeListener(e -> setX((int) ((JSpinner) e.getSource()).getValue()));
        JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
        locationYSpinner.addChangeListener(e -> setY((int) ((JSpinner) e.getSource()).getValue()));
        if (zoom != 100) {
            locationXSpinner.setEnabled(false);
            locationYSpinner.setEnabled(false);
        }
        JPanel locationSpinnerPanel = new JPanel();
        locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel, BoxLayout.X_AXIS));
        locationSpinnerPanel.add(locationXSpinner);
        locationSpinnerPanel.add(new JLabel(" , "));
        locationSpinnerPanel.add(locationYSpinner);
        locationSpinnerPanel.setBounds(columnA_posX + 90, columnB_Y += 20, 200, 20);
        components.add(locationSpinnerPanel);

        // T-TRANSITION PORTAL STATUS
        JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
        portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(portalLabel);

        JCheckBox portalBox = new JCheckBox("", transition.isPortal());
        portalBox.setBounds(columnB_posX, columnB_Y += 20, 30, 20);
        portalBox.setSelected(((Transition) element).isPortal());
        portalBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                makePortal();
            } else {
                if (((Transition) element).getElementLocations().size() > 1)
                    JOptionPane.showMessageDialog(null, "Transition contains more than one location!", "Cannot proceed",
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
        });
        components.add(portalBox);

        //FUNKCYJNOŚĆ
        JLabel functionLabel = new JLabel("Functional:", JLabel.LEFT);
        functionLabel.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(functionLabel);

        JCheckBox functionalCheckBox = new JCheckBox("", transition.isPortal());
        functionalCheckBox.setBounds(columnB_posX, columnB_Y += 20, 30, 20);
        functionalCheckBox.setSelected(((Transition) element).isFunctional());

        functionalCheckBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            ((Transition) element).setFunctional(box.isSelected());

            overlord.markNetChange();
        });
        components.add(functionalCheckBox);

        JButton functionsEditorButton = new JButton(Tools.getResIcon32("/icons/functionsWindow/functionsIcon.png"));
        functionsEditorButton.setName("Functions editor");
        functionsEditorButton.setText("<html>Functions<br>&nbsp;&nbsp;&nbsp;editor&nbsp;</html>");
        functionsEditorButton.setMargin(new Insets(0, 0, 0, 0));
        functionsEditorButton.setBounds(columnA_posX + 125, columnA_Y - 16, 110, 32);
        functionsEditorButton.addActionListener(actionEvent -> new HolmesFunctionsBuilder((Transition) element));
        components.add(functionsEditorButton);

        // WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().x;
        int yNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().y;

        nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
        nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);

        JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
        locNameLabelX.setBounds(columnA_posX + 90, columnA_Y, 40, 20);
        components.add(locNameLabelX);

        JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
        nameLocationXSpinner.setBounds(columnA_posX + 125, columnA_Y, 60, 20);
        nameLocationXSpinner.addChangeListener(new ChangeListener() {
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetX((int) ((JSpinner) e.getSource()).getValue(), trans_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationXSpinnerModel.setValue(res.x);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));

        components.add(nameLocationXSpinner);

        JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
        locNameLabelY.setBounds(columnA_posX + 195, columnB_Y, 40, 20);
        components.add(locNameLabelY);

        JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
        nameLocationYSpinner.setBounds(columnA_posX + 230, columnA_Y, 60, 20);
        nameLocationYSpinner.addChangeListener(new ChangeListener() {
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetY((int) ((JSpinner) e.getSource()).getValue(), trans_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationYSpinnerModel.setValue(res.y);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocationYSpinner);

        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnA_posX + 90, columnA_Y += 25, 150, 40);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                JButton button_tmp = (JButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, true);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, false);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocChangeButton);

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);

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
     *
     * @param transition Transition - obiekt tranzycji sieci
     * @param location   ElementLocation - lokalizacja tranzycji
     */
    private void createColorTransitionSubWindow(Transition transition, ElementLocation location) {
        int columnA_posX = 10;
        int columnB_posX = 100;
        int columnA_Y = 0;
        int columnB_Y = 0;
        int colACompLength = 70;
        int colBCompLength = 200;

        //TODO:

        mode = CTRANSITION;
        elementLocation = location;
        initiateContainers();
        element = transition;
        Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);

        JLabel colorLabel = new JLabel("Colored transition data:", JLabel.LEFT);
        colorLabel.setBounds(columnA_posX, columnA_Y += 10, 220, 20);
        components.add(colorLabel);
        columnB_Y += 10;

        // ID:
        JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
        idLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(idLabel);

        final int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transition);
        JLabel idLabel2 = new JLabel(Integer.toString(gID));
        idLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
        idLabel2.setFont(normalFont);
        components.add(idLabel2);

        JLabel idLabel3 = new JLabel("gID:");
        idLabel3.setBounds(columnB_posX + 35, columnA_Y, 50, 20);
        components.add(idLabel3);
        JLabel idLabel4 = new JLabel(transition.getID() + "");
        idLabel4.setBounds(columnB_posX + 60, columnB_Y, 50, 20);
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
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
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
                if (field != null)
                    newComment = field.getText();
                changeComment(newComment);
                overlord.markNetChange();
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
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        //ZOOM:
        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        //PORTAL
        JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
        portalLabel.setBounds(columnB_posX + 120, columnB_Y, colACompLength, 20);
        components.add(portalLabel);

        JCheckBox portalBox = new JCheckBox("", transition.isPortal());
        portalBox.setBounds(columnB_posX + 180, columnB_Y, 30, 20);
        portalBox.setSelected(((Transition) element).isPortal());

        portalBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                makePortal();
            } else {
                if (((Transition) element).getElementLocations().size() > 1)
                    JOptionPane.showMessageDialog(null, "Transition contains more than one location!", "Cannot proceed",
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
        });
        components.add(portalBox);

        //LOKALIZACJA:
        JLabel locLabel = new JLabel("Location:", JLabel.LEFT);
        locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(locLabel);

        SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
        SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);

        JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
        locationXSpinner.addChangeListener(e -> setX((int) ((JSpinner) e.getSource()).getValue()));
        JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
        locationYSpinner.addChangeListener(e -> setY((int) ((JSpinner) e.getSource()).getValue()));
        if (zoom != 100) {
            locationXSpinner.setEnabled(false);
            locationYSpinner.setEnabled(false);
        }
        JPanel locationSpinnerPanel = new JPanel();
        locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel, BoxLayout.X_AXIS));
        locationSpinnerPanel.add(locationXSpinner);
        locationSpinnerPanel.add(new JLabel(" , "));
        locationSpinnerPanel.add(locationYSpinner);

        locationSpinnerPanel.setBounds(columnA_posX + 90, columnB_Y += 20, colBCompLength, 20);
        components.add(locationSpinnerPanel);


        // WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().x;
        int yNameOffset = transition.getNamesLocations().get(locationIndex).getPosition().y;

        nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
        nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);

        JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
        locNameLabelX.setBounds(columnA_posX + 90, columnA_Y, 40, 20);
        components.add(locNameLabelX);

        JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
        nameLocationXSpinner.setBounds(columnA_posX + 125, columnA_Y, 60, 20);
        nameLocationXSpinner.addChangeListener(new ChangeListener() {
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;
                Point res = setNameOffsetX((int) ((JSpinner) e.getSource()).getValue(), trans_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationXSpinnerModel.setValue(res.x);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));

        components.add(nameLocationXSpinner);

        JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
        locNameLabelY.setBounds(columnA_posX + 195, columnB_Y, 40, 20);
        components.add(locNameLabelY);

        JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
        nameLocationYSpinner.setBounds(columnA_posX + 230, columnA_Y, 60, 20);
        nameLocationYSpinner.addChangeListener(new ChangeListener() {
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetY((int) ((JSpinner) e.getSource()).getValue(), trans_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationYSpinnerModel.setValue(res.y);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocationYSpinner);

        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnA_posX + 90, columnA_Y += 25, 150, 40);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                JButton button_tmp = (JButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, true);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, false);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocChangeButton);

        JLabel reqLabel = new JLabel("Required tokens threshold:", JLabel.LEFT);
        reqLabel.setBounds(columnA_posX, columnA_Y += 50, 220, 20);
        columnB_Y += 76;
        components.add(reqLabel);

        JLabel reqT0Label = new JLabel("T0 red:", JLabel.LEFT);
        reqT0Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(reqT0Label);

        SpinnerModel weightT0SpinnerModel = new SpinnerNumberModel(transition.getRequiredColoredTokens(0), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT0Spinner = new JSpinner(weightT0SpinnerModel);
        weightT0Spinner.setBounds(columnB_posX - 35, columnB_Y += 20, 65, 20);
        weightT0Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 0);
        });
        components.add(weightT0Spinner);

        JLabel reqT3Label = new JLabel("T3 yellow:", JLabel.LEFT);
        reqT3Label.setBounds(columnB_posX + 40, columnB_Y, 80, 20);
        components.add(reqT3Label);

        SpinnerModel weightT3SpinnerModel = new SpinnerNumberModel(transition.getRequiredColoredTokens(3), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT3Spinner = new JSpinner(weightT3SpinnerModel);
        weightT3Spinner.setBounds(columnB_posX + 100, columnB_Y, 65, 20);
        weightT3Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 3);
        });
        components.add(weightT3Spinner);

        JLabel reqT1Label = new JLabel("T1 green:", JLabel.LEFT);
        reqT1Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(reqT1Label);

        SpinnerModel weightT1SpinnerModel = new SpinnerNumberModel(transition.getRequiredColoredTokens(1), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT1Spinner = new JSpinner(weightT1SpinnerModel);
        weightT1Spinner.setBounds(columnB_posX - 35, columnB_Y += 20, 65, 20);
        weightT1Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 1);
        });
        components.add(weightT1Spinner);

        JLabel reqT4Label = new JLabel("T4 gray:", JLabel.LEFT);
        reqT4Label.setBounds(columnB_posX + 40, columnB_Y, 80, 20);
        components.add(reqT4Label);

        SpinnerModel weightT4SpinnerModel = new SpinnerNumberModel(transition.getRequiredColoredTokens(4), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT4Spinner = new JSpinner(weightT4SpinnerModel);
        weightT4Spinner.setBounds(columnB_posX + 100, columnB_Y, 65, 20);
        weightT4Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 4);
        });
        components.add(weightT4Spinner);

        JLabel reqT2Label = new JLabel("T2 blue:", JLabel.LEFT);
        reqT2Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(reqT2Label);

        SpinnerModel weightT2SpinnerModel = new SpinnerNumberModel(transition.getRequiredColoredTokens(2), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT2Spinner = new JSpinner(weightT2SpinnerModel);
        weightT2Spinner.setBounds(columnB_posX - 35, columnB_Y += 20, 65, 20);
        weightT2Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 2);
        });
        components.add(weightT2Spinner);

        JLabel reqT5Label = new JLabel("T5 black:", JLabel.LEFT);
        reqT5Label.setBounds(columnB_posX + 40, columnB_Y, 80, 20);
        components.add(reqT5Label);

        SpinnerModel weightT5SpinnerModel = new SpinnerNumberModel(transition.getRequiredColoredTokens(5), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT5Spinner = new JSpinner(weightT5SpinnerModel);
        weightT5Spinner.setBounds(columnB_posX + 100, columnB_Y, 65, 20);
        weightT5Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 5);
        });
        components.add(weightT5Spinner);

        panel.setLayout(null);
        for (JComponent component : components) {
            panel.add(component);
        }

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
     *
     * @param metaNode TimeTransition - obiekt tranzycji czasowej
     * @param location ElementLocation - lokalizacja tranzycji
     */
    private void createMetaNodeSubWindow(final MetaNode metaNode, ElementLocation location) {
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
        idLabel3.setBounds(columnB_posX + 35, columnA_Y, 50, 20);
        components.add(idLabel3);
        JLabel idLabel4 = new JLabel(metaNode.getID() + "");
        idLabel4.setBounds(columnB_posX + 60, columnB_Y, 50, 20);
        idLabel4.setFont(normalFont);
        components.add(idLabel4);

        JLabel sheetRepresentedLabel = new JLabel("Subnet(sheet):");
        sheetRepresentedLabel.setBounds(columnA_posX, columnA_Y += 20, 95, 20);
        components.add(sheetRepresentedLabel);
        int shID = metaNode.getRepresentedSheetID();
        String text = "" + shID + "";
        text += " (" + overlord.getWorkspace().getIndexOfId(metaNode.getRepresentedSheetID()) + ")";
        JLabel sheetRepresentedLabelValue = new JLabel(text);
        sheetRepresentedLabelValue.setBounds(columnB_posX, columnB_Y += 20, 50, 20);
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
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
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
                if (field != null)
                    newComment = field.getText();
                changeComment(newComment);
                overlord.markNetChange();
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
        subnetTButton.setBounds(columnA_posX - 5, columnA_Y += 20, 105, 20);
        subnetTButton.setActionCommand("0");
        subnetTButton.addActionListener(new ActionListener() {
            private MetaNode myMeta = null;

            public void actionPerformed(ActionEvent actionEvent) {
                if (doNotUpdate) return;
                boolean status = false;
                if (myMeta.getMetaType() != MetaType.SUBNETTRANS) {
                    status = overlord.subnetsHQ.changeSubnetType(myMeta, MetaType.SUBNETTRANS);
                }
                if (!status) {
                    doNotUpdate = true;
                    Enumeration<AbstractButton> wtf = groupRadioMetaType.getElements();
                    JRadioButton radioB = (JRadioButton) wtf.nextElement(); //first: t-type
                    if (myMeta.getMetaType() == MetaType.SUBNETPLACE)
                        radioB = (JRadioButton) wtf.nextElement(); //second p-type
                    if (myMeta.getMetaType() == MetaType.SUBNET) {
                        radioB = (JRadioButton) wtf.nextElement(); //second
                        radioB = (JRadioButton) wtf.nextElement(); //third pt-type
                    }
                    groupRadioMetaType.setSelected(radioB.getModel(), true);
                    doNotUpdate = false;
                }
            }

            private ActionListener yesWeCan(MetaNode metaN) {
                myMeta = metaN;
                return this;
            }
        }.yesWeCan(metaNode));

        groupRadioMetaType.add(subnetTButton);
        components.add(subnetTButton);


        JRadioButton subnetPButton = new JRadioButton("Subnet P-type");
        subnetPButton.setBounds(columnA_posX + 100, columnA_Y, 120, 20);
        subnetPButton.setActionCommand("1");
        subnetPButton.addActionListener(new ActionListener() {
            private MetaNode myMeta = null;

            public void actionPerformed(ActionEvent actionEvent) {
                if (doNotUpdate) return;
                boolean status = false;
                if (myMeta.getMetaType() != MetaType.SUBNETPLACE) {
                    status = overlord.subnetsHQ.changeSubnetType(myMeta, MetaType.SUBNETPLACE);
                }
                if (!status) {
                    doNotUpdate = true;
                    Enumeration<AbstractButton> wtf = groupRadioMetaType.getElements();
                    JRadioButton radioB = (JRadioButton) wtf.nextElement(); //first: t-type
                    if (myMeta.getMetaType() == MetaType.SUBNETPLACE)
                        radioB = (JRadioButton) wtf.nextElement(); //second p-type
                    if (myMeta.getMetaType() == MetaType.SUBNET) {
                        radioB = (JRadioButton) wtf.nextElement(); //second
                        radioB = (JRadioButton) wtf.nextElement(); //third pt-type
                    }
                    groupRadioMetaType.setSelected(radioB.getModel(), true);
                    doNotUpdate = false;
                }
            }

            private ActionListener yesWeCan(MetaNode metaN) {
                myMeta = metaN;
                return this;
            }
        }.yesWeCan(metaNode));
        groupRadioMetaType.add(subnetPButton);
        components.add(subnetPButton);

        JRadioButton subnetPTButton = new JRadioButton("P & T");
        subnetPTButton.setBounds(columnA_posX + 230, columnA_Y, 80, 20);
        subnetPTButton.setActionCommand("2");
        subnetPTButton.addActionListener(new ActionListener() {
            private MetaNode myMeta = null;

            public void actionPerformed(ActionEvent actionEvent) {
                if (doNotUpdate) return;
                if (myMeta.getMetaType() != MetaType.SUBNET) {
                    overlord.subnetsHQ.changeSubnetType(myMeta, MetaType.SUBNET);
                }
                doNotUpdate = true;
                Enumeration<AbstractButton> wtf = groupRadioMetaType.getElements();
                JRadioButton radioB = (JRadioButton) wtf.nextElement(); //first: t-type
                if (myMeta.getMetaType() == MetaType.SUBNETPLACE)
                    radioB = (JRadioButton) wtf.nextElement(); //second p-type
                if (myMeta.getMetaType() == MetaType.SUBNET) {
                    radioB = (JRadioButton) wtf.nextElement(); //second
                    radioB = (JRadioButton) wtf.nextElement(); //third pt-type
                }
                groupRadioMetaType.setSelected(radioB.getModel(), true);
                doNotUpdate = false;
            }

            private ActionListener yesWeCan(MetaNode metaN) {
                myMeta = metaN;
                return this;
            }
        }.yesWeCan(metaNode));
        groupRadioMetaType.add(subnetPTButton);
        components.add(subnetPTButton);

        doNotUpdate = true;
        if (metaNode.getMetaType() == MetaType.SUBNETTRANS)
            groupRadioMetaType.setSelected(subnetTButton.getModel(), true);
        else if (metaNode.getMetaType() == MetaType.SUBNETPLACE)
            groupRadioMetaType.setSelected(subnetPButton.getModel(), true);
        else if (metaNode.getMetaType() == MetaType.SUBNET)
            groupRadioMetaType.setSelected(subnetPTButton.getModel(), true);

        doNotUpdate = false;
        columnB_Y += 20;

        // T-TRANSITION SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, 100, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        JLabel zoomLabel = new JLabel("Zoom:");
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // T-TRANSITION LOCATION:
        JLabel comLabel2 = new JLabel("Location:", JLabel.LEFT);
        comLabel2.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(comLabel2);

        SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
        SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
        JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
        locationXSpinner.addChangeListener(e -> setX((int) ((JSpinner) e.getSource()).getValue()));
        JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
        locationYSpinner.addChangeListener(e -> setY((int) ((JSpinner) e.getSource()).getValue()));
        if (zoom != 100) {
            locationXSpinner.setEnabled(false);
            locationYSpinner.setEnabled(false);
        }
        JPanel locationSpinnerPanel = new JPanel();
        locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel, BoxLayout.X_AXIS));
        locationSpinnerPanel.add(locationXSpinner);
        locationSpinnerPanel.add(new JLabel(" , "));
        locationSpinnerPanel.add(locationYSpinner);
        locationSpinnerPanel.setBounds(columnA_posX + 90, columnB_Y += 20, 200, 20);
        components.add(locationSpinnerPanel);

        // WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel("Name offset:", JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = metaNode.getElementLocations().indexOf(location);
        int xNameOffset = metaNode.getNamesLocations().get(locationIndex).getPosition().x;
        int yNameOffset = metaNode.getNamesLocations().get(locationIndex).getPosition().y;

        nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
        nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);

        JLabel locNameLabelX = new JLabel("xOff: ", JLabel.LEFT);
        locNameLabelX.setBounds(columnA_posX + 90, columnA_Y, 40, 20);
        components.add(locNameLabelX);

        JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
        nameLocationXSpinner.setBounds(columnA_posX + 125, columnA_Y, 60, 20);
        nameLocationXSpinner.addChangeListener(new ChangeListener() {
            private MetaNode meta_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetX((int) ((JSpinner) e.getSource()).getValue(), meta_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationXSpinnerModel.setValue(res.x);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(MetaNode metaN, ElementLocation inLoc) {
                meta_tmp = metaN;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(metaNode, location));

        components.add(nameLocationXSpinner);

        JLabel locNameLabelY = new JLabel("yOff: ", JLabel.LEFT);
        locNameLabelY.setBounds(columnA_posX + 195, columnB_Y, 40, 20);
        components.add(locNameLabelY);

        JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
        nameLocationYSpinner.setBounds(columnA_posX + 230, columnA_Y, 60, 20);
        nameLocationYSpinner.addChangeListener(new ChangeListener() {
            private MetaNode meta_tmp;
            private ElementLocation el_tmp;

            public void stateChanged(ChangeEvent e) {
                if (doNotUpdate)
                    return;

                Point res = setNameOffsetY((int) ((JSpinner) e.getSource()).getValue(), meta_tmp, el_tmp);
                doNotUpdate = true;
                nameLocationYSpinnerModel.setValue(res.y);
                doNotUpdate = false;
            }

            private ChangeListener yesWeCan(MetaNode metaN, ElementLocation inLoc) {
                meta_tmp = metaN;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(metaNode, location));
        components.add(nameLocationYSpinner);

        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText("MouseWheel - up/down ; SHIFT+MouseWheel - left/right");
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnA_posX + 90, columnA_Y += 25, 150, 40);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private MetaNode meta_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                JButton button_tmp = (JButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocationON.png"));
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(meta_tmp, el_tmp, true);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, false);
                }
            }

            private ActionListener yesWeCan(MetaNode metaN, ElementLocation inLoc) {
                meta_tmp = metaN;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(metaNode, location));
        components.add(nameLocChangeButton);

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);

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
     *
     * @param arc Arc - obiekt łuku
     */
    private void createArcSubWindow(Arc arc) {
        int columnA_posX = 10;
        int columnB_posX = 110;
        int columnA_Y = 0;
        int columnB_Y = 0;
        int colACompLength = 70;
        int colBCompLength = 200;
        initiateContainers();
        // set mode
        mode = ARC;
        element = arc;
        pairedArc = arc.getPairedArc();
        elementLocation = arc.getStartLocation();

        Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);
        // ARC ID
        JLabel idLabel = new JLabel("gID:", JLabel.LEFT);
        idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
        components.add(idLabel);
        JLabel idLabel2 = new JLabel(Integer.toString(arc.getID()));
        idLabel2.setFont(normalFont);
        idLabel2.setBounds(columnB_posX - 10, columnB_Y += 10, colACompLength, 20);
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
                if (field != null)
                    newComment = field.getText();
                changeComment(newComment);
            }
        });

        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX - 10, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);

        if (((Arc) element).getArcType() != TypeOfArc.RESET && ((Arc) element).getArcType() != TypeOfArc.COLOR) {
            if (((Arc) element).getArcType() == TypeOfArc.READARC && pairedArc != null) {
                String type1 = "T-->P";
                String type2 = "P-->T";
                if (((Arc) element).getStartNode() instanceof Place) {
                    type1 = "P-->T";
                    type2 = "T-->P";
                }
                JLabel weightLabel = new JLabel("Weight (" + type1 + ")", JLabel.LEFT);
                weightLabel.setBounds(columnA_posX, columnA_Y += 20, 100, 20);
                components.add(weightLabel);

                SpinnerModel weightSpinnerModel = new SpinnerNumberModel(arc.getWeight(), 0, Integer.MAX_VALUE, 1);
                JSpinner weightSpinner = new JSpinner(weightSpinnerModel);
                weightSpinner.setBounds(columnB_posX - 10, columnB_Y += 20, colBCompLength / 3, 20);
                weightSpinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setWeight(tokenz, (Arc) element);
                });
                components.add(weightSpinner);

                JLabel weightLabel2 = new JLabel("Weight (" + type2 + ")", JLabel.LEFT);
                weightLabel2.setBounds(columnA_posX, columnA_Y += 20, 100, 20);
                components.add(weightLabel2);

                SpinnerModel weightSpinnerModel2 = new SpinnerNumberModel(pairedArc.getWeight(), 0, Integer.MAX_VALUE, 1);
                JSpinner weightSpinner2 = new JSpinner(weightSpinnerModel2);
                weightSpinner2.setBounds(columnB_posX - 10, columnB_Y += 20, colBCompLength / 3, 20);
                weightSpinner2.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setWeight(tokenz, pairedArc);
                });
                components.add(weightSpinner2);
            } else {
                JLabel weightLabel = new JLabel("Weight:", JLabel.LEFT);
                weightLabel.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
                components.add(weightLabel);

                SpinnerModel weightSpinnerModel = new SpinnerNumberModel(arc.getWeight(), 0, Integer.MAX_VALUE, 1);
                JSpinner weightSpinner = new JSpinner(weightSpinnerModel);
                weightSpinner.setBounds(columnB_posX - 10, columnB_Y += 20, colBCompLength / 3, 20);
                weightSpinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setWeight(tokenz, (Arc) element);
                });
                components.add(weightSpinner);
            }
        }

        // startNode
        columnB_posX += 30;
        colACompLength += 40;

        JLabel typeArcLabel = new JLabel("Type:", JLabel.LEFT);
        //typeArcLabel.setFont(boldFont);
        typeArcLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(typeArcLabel);

        JLabel typeArcLabel2 = new JLabel(arc.getArcType().toString());
        typeArcLabel2.setFont(normalFont);
        typeArcLabel2.setBounds(columnB_posX - 40, columnB_Y += 20, colACompLength + 40, 20);
        components.add(typeArcLabel2);

        JLabel readArcLabel = new JLabel("Read arc:", JLabel.LEFT);
        readArcLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(readArcLabel);


        String txt = "no";
        if (pairedArc != null) {
            txt = "yes [paired arc ID: " + pairedArc.getID() + "]";
        } else {
            if (InvariantsTools.isDoubleArc(arc)) {
                txt = "double arc (hidden readarc)";
            }
        }

        JLabel readArcLabel2 = new JLabel(txt);
        readArcLabel2.setFont(normalFont);
        readArcLabel2.setBounds(columnB_posX - 40, columnB_Y += 20, colACompLength + 60, 20);
        components.add(readArcLabel2);

        JLabel startNodeLabel = new JLabel("Start Node name:", JLabel.LEFT);
        startNodeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(startNodeLabel);
        columnB_Y += 20;

        //JLabel label2A = new JLabel("Name:", JLabel.LEFT);
        //label2A.setBounds(columnA_posX+80, columnA_Y , colACompLength, 20);
        //components.add(label2A);
        JLabel label2B = new JLabel(arc.getStartNode().getName());
        label2B.setFont(normalFont);
        label2B.setBounds(columnA_posX + 110, columnB_Y, colBCompLength + 40, 20);
        components.add(label2B);

        JLabel label1A = new JLabel("gID:", JLabel.LEFT);
        label1A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(label1A);
        JLabel label1B = new JLabel(Integer.toString(arc.getStartNode().getID()));
        label1B.setFont(normalFont);
        label1B.setBounds(columnA_posX + 40, columnB_Y += 20, 50, 20);
        components.add(label1B);

        JLabel label3A = new JLabel("Sheet:", JLabel.LEFT);
        label3A.setBounds(columnA_posX + 80, columnA_Y, colACompLength, 20);
        components.add(label3A);
        JLabel label3B = new JLabel(Integer.toString(arc.getStartLocation().getSheetID()));
        label3B.setFont(normalFont);
        label3B.setBounds(columnA_posX + 120, columnB_Y, 40, 20);
        components.add(label3B);

        JLabel label4A = new JLabel("Location:", JLabel.LEFT);
        label4A.setBounds(columnA_posX + 150, columnA_Y, colACompLength, 20);
        components.add(label4A);
        JLabel label4B = new JLabel(arc.getStartLocation().getPosition().x + ", "
                + arc.getStartLocation().getPosition().y);
        label4B.setBounds(columnA_posX + 210, columnB_Y, colBCompLength, 20);
        label4B.setFont(normalFont);
        components.add(label4B);

        // endNode
        JLabel endNodeLabel = new JLabel("End Node name:", JLabel.LEFT);
        endNodeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(endNodeLabel);
        columnB_Y += 20;

        //JLabel label6A = new JLabel("Name:", JLabel.LEFT);
        //label6A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        //components.add(label6A);
        JLabel label6B = new JLabel(arc.getEndNode().getName());
        label6B.setFont(normalFont);
        label6B.setBounds(columnA_posX + 110, columnB_Y, colBCompLength + 40, 20);
        components.add(label6B);

        JLabel label5A = new JLabel("gID:", JLabel.LEFT);
        label5A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(label5A);
        JLabel label5B = new JLabel(Integer.toString(arc.getEndNode().getID()));
        label5B.setFont(normalFont);
        label5B.setBounds(columnA_posX + 40, columnB_Y += 20, colBCompLength, 20);
        components.add(label5B);

        JLabel label7A = new JLabel("Sheet:", JLabel.LEFT);
        label7A.setBounds(columnA_posX + 80, columnA_Y, colACompLength, 20);
        components.add(label7A);
        JLabel label7B = new JLabel(Integer.toString(arc.getEndLocation().getSheetID()));
        label7B.setFont(normalFont);
        label7B.setBounds(columnA_posX + 120, columnB_Y, 40, 20);
        components.add(label7B);

        JLabel label8A = new JLabel("Location:", JLabel.LEFT);
        label8A.setBounds(columnA_posX + 150, columnA_Y, colACompLength, 20);
        components.add(label8A);
        JLabel label8B = new JLabel(arc.getEndLocation().getPosition().x + ", "
                + arc.getEndLocation().getPosition().y);
        label8B.setFont(normalFont);
        label8B.setBounds(columnA_posX + 210, columnB_Y, colBCompLength, 20);
        components.add(label8B);

        //KOLORY
        //TODO:
        if (arc.getArcType() == TypeOfArc.COLOR) {
            String type1 = "T-->P (Transition to Place weights):";
            String type2 = "P-->T (Place to Transition weights):";
            pairedArc = InvariantsTools.getPairedArc(arc);
            if (pairedArc != null) {
                if (((Arc) element).getStartNode() instanceof Place) {
                    type1 = "P-->T (Place to Transition weights):";
                    type2 = "T-->P (Transition to Place weights):";
                }

                JLabel weightPair1Label = new JLabel(type1, JLabel.LEFT);
                weightPair1Label.setBounds(columnA_posX, columnA_Y += 20, 260, 20);
                columnB_Y += 20;
                components.add(weightPair1Label);
            }
            JLabel weight0Label = new JLabel("W0 (red):", JLabel.LEFT);
            weight0Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
            components.add(weight0Label);

            SpinnerModel weightT0SpinnerModel = new SpinnerNumberModel(arc.getColorWeight(0), 0, Integer.MAX_VALUE, 1);
            JSpinner weightT0Spinner = new JSpinner(weightT0SpinnerModel);
            weightT0Spinner.setBounds(columnB_posX - 60, columnB_Y += 20, 65, 20);
            weightT0Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorWeight(tokenz, (Arc) element, 0);
            });
            components.add(weightT0Spinner);

            JLabel weight3Label = new JLabel("W3 (yellow):", JLabel.LEFT);
            weight3Label.setBounds(columnB_posX + 10, columnB_Y, 80, 20);
            components.add(weight3Label);

            SpinnerModel weightT3SpinnerModel = new SpinnerNumberModel(arc.getColorWeight(3), 0, Integer.MAX_VALUE, 1);
            JSpinner weightT3Spinner = new JSpinner(weightT3SpinnerModel);
            weightT3Spinner.setBounds(columnB_posX + 90, columnB_Y, 65, 20);
            weightT3Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorWeight(tokenz, (Arc) element, 3);
            });
            components.add(weightT3Spinner);

            JLabel weight1Label = new JLabel("W1 (green):", JLabel.LEFT);
            weight1Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
            components.add(weight1Label);

            SpinnerModel weightT1SpinnerModel = new SpinnerNumberModel(arc.getColorWeight(1), 0, Integer.MAX_VALUE, 1);
            JSpinner weightT1Spinner = new JSpinner(weightT1SpinnerModel);
            weightT1Spinner.setBounds(columnB_posX - 60, columnB_Y += 20, 65, 20);
            weightT1Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorWeight(tokenz, (Arc) element, 1);
            });
            components.add(weightT1Spinner);

            JLabel weight4Label = new JLabel("W4 (gray):", JLabel.LEFT);
            weight4Label.setBounds(columnB_posX + 10, columnB_Y, 80, 20);
            components.add(weight4Label);

            SpinnerModel weightT4SpinnerModel = new SpinnerNumberModel(arc.getColorWeight(4), 0, Integer.MAX_VALUE, 1);
            JSpinner weightT4Spinner = new JSpinner(weightT4SpinnerModel);
            weightT4Spinner.setBounds(columnB_posX + 90, columnB_Y, 65, 20);
            weightT4Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorWeight(tokenz, (Arc) element, 4);
            });
            components.add(weightT4Spinner);

            JLabel weight2Label = new JLabel("W2 (blue):", JLabel.LEFT);
            weight2Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
            components.add(weight2Label);

            SpinnerModel weightT2SpinnerModel = new SpinnerNumberModel(arc.getColorWeight(2), 0, Integer.MAX_VALUE, 1);
            JSpinner weightT2Spinner = new JSpinner(weightT2SpinnerModel);
            weightT2Spinner.setBounds(columnB_posX - 60, columnB_Y += 20, 65, 20);
            weightT2Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorWeight(tokenz, (Arc) element, 2);
            });
            components.add(weightT2Spinner);

            JLabel weight5Label = new JLabel("W5 (black):", JLabel.LEFT);
            weight5Label.setBounds(columnB_posX + 10, columnB_Y, 80, 20);
            components.add(weight5Label);

            SpinnerModel weightT5SpinnerModel = new SpinnerNumberModel(arc.getColorWeight(5), 0, Integer.MAX_VALUE, 1);
            JSpinner weightT5Spinner = new JSpinner(weightT5SpinnerModel);
            weightT5Spinner.setBounds(columnB_posX + 90, columnB_Y, 65, 20);
            weightT5Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorWeight(tokenz, (Arc) element, 5);
            });
            components.add(weightT5Spinner);

            //dla łuku kolorowego sparowanego:
            if (pairedArc != null) {
                JLabel weightPair2Label = new JLabel(type2, JLabel.LEFT);
                weightPair2Label.setBounds(columnA_posX, columnA_Y += 20, 260, 20);
                columnB_Y += 20;
                components.add(weightPair2Label);

                JLabel weightp0Label = new JLabel("W0 (red):", JLabel.LEFT);
                weightp0Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
                components.add(weightp0Label);

                SpinnerModel weightTp0SpinnerModel = new SpinnerNumberModel(pairedArc.getColorWeight(0), 0, Integer.MAX_VALUE, 1);
                JSpinner weightTp0Spinner = new JSpinner(weightTp0SpinnerModel);
                weightTp0Spinner.setBounds(columnB_posX - 60, columnB_Y += 20, 65, 20);
                weightTp0Spinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setColorWeight(tokenz, pairedArc, 0);
                });
                components.add(weightTp0Spinner);

                JLabel weightp3Label = new JLabel("W3 (yellow):", JLabel.LEFT);
                weightp3Label.setBounds(columnB_posX + 10, columnB_Y, 80, 20);
                components.add(weightp3Label);

                SpinnerModel weightTp3SpinnerModel = new SpinnerNumberModel(pairedArc.getColorWeight(3), 0, Integer.MAX_VALUE, 1);
                JSpinner weightTp3Spinner = new JSpinner(weightTp3SpinnerModel);
                weightTp3Spinner.setBounds(columnB_posX + 90, columnB_Y, 65, 20);
                weightTp3Spinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setColorWeight(tokenz, pairedArc, 3);
                });
                components.add(weightTp3Spinner);

                JLabel weightp1Label = new JLabel("W1 (green):", JLabel.LEFT);
                weightp1Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
                components.add(weightp1Label);

                SpinnerModel weightTp1SpinnerModel = new SpinnerNumberModel(pairedArc.getColorWeight(1), 0, Integer.MAX_VALUE, 1);
                JSpinner weightTp1Spinner = new JSpinner(weightTp1SpinnerModel);
                weightTp1Spinner.setBounds(columnB_posX - 60, columnB_Y += 20, 65, 20);
                weightTp1Spinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setColorWeight(tokenz, pairedArc, 1);
                });
                components.add(weightTp1Spinner);

                JLabel weightp4Label = new JLabel("W4 (gray):", JLabel.LEFT);
                weightp4Label.setBounds(columnB_posX + 10, columnB_Y, 80, 20);
                components.add(weightp4Label);

                SpinnerModel weightTp4SpinnerModel = new SpinnerNumberModel(pairedArc.getColorWeight(4), 0, Integer.MAX_VALUE, 1);
                JSpinner weightTp4Spinner = new JSpinner(weightTp4SpinnerModel);
                weightTp4Spinner.setBounds(columnB_posX + 90, columnB_Y, 65, 20);
                weightTp4Spinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setColorWeight(tokenz, pairedArc, 4);
                });
                components.add(weightTp4Spinner);

                JLabel weightp2Label = new JLabel("W2 (blue):", JLabel.LEFT);
                weightp2Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
                components.add(weightp2Label);

                SpinnerModel weightTp2SpinnerModel = new SpinnerNumberModel(pairedArc.getColorWeight(2), 0, Integer.MAX_VALUE, 1);
                JSpinner weightTp2Spinner = new JSpinner(weightTp2SpinnerModel);
                weightTp2Spinner.setBounds(columnB_posX - 60, columnB_Y += 20, 65, 20);
                weightTp2Spinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setColorWeight(tokenz, pairedArc, 2);
                });
                components.add(weightTp2Spinner);

                JLabel weightp5Label = new JLabel("W5 (black):", JLabel.LEFT);
                weightp5Label.setBounds(columnB_posX + 10, columnB_Y, 80, 20);
                components.add(weightp5Label);

                SpinnerModel weightTp5SpinnerModel = new SpinnerNumberModel(pairedArc.getColorWeight(5), 0, Integer.MAX_VALUE, 1);
                JSpinner weightTp5Spinner = new JSpinner(weightTp5SpinnerModel);
                weightTp5Spinner.setBounds(columnB_posX + 90, columnB_Y, 65, 20);
                weightTp5Spinner.addChangeListener(e -> {
                    int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                    setColorWeight(tokenz, pairedArc, 5);
                });
                components.add(weightTp5Spinner);
            }
        }


        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
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
     *
     * @param sheet WorkspaceSheet - obiekt arkusza
     */
    private void createSheetSubWindow(WorkspaceSheet sheet) {
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
        netNameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            String newName = field.getText();

            overlord.getWorkspace().getProject().setName(newName);
        });
        components.add(netNameField);


        JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
        idLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(idLabel);
        String text = "" + sheet.getId();
        int shPos = overlord.getWorkspace().getIndexOfId(sheet.getId());
        text += " (sheet position: " + shPos + ")";
        JLabel idLabel2 = new JLabel(text);
        idLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength + 150, 20);
        components.add(idLabel2);

        // SHEET ZOOM
        int zoom = sheet.getGraphPanel().getZoom();
        //
        Dimension x = sheet.getGraphPanel().getOriginSize();
        int widthOrg;
        int heightOrg;
        if (x != null) {
            widthOrg = (int) x.getWidth();
            heightOrg = (int) x.getHeight();
        } else {
            widthOrg = sheet.getGraphPanel().getSize().width;
            heightOrg = sheet.getGraphPanel().getSize().height;
        }

        //widthOrg = (int) (((double)100/(double)zoom) * widthOrg);
        //heightOrg = (int) (((double)100/(double)zoom) * heightOrg);

        JLabel zoomLabel1 = new JLabel("Zoom:", JLabel.LEFT);
        zoomLabel1.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(zoomLabel1);
        JLabel zoomLabel2 = new JLabel(zoom + "%");
        zoomLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // SHEET SIZE
        JLabel widthLabel = new JLabel("Width:", JLabel.LEFT);
        widthLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(widthLabel);

        SpinnerModel widthSpinnerModel = new SpinnerNumberModel(sheet.getGraphPanel().getSize().width, 0, Integer.MAX_VALUE, 1);
        JSpinner widthSpinner = new JSpinner(widthSpinnerModel);
        widthSpinner.setBounds(columnB_posX, columnB_Y += 20, colBCompLength / 2, 20);
        widthSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int width = (int) spinner.getValue();
            setSheetWidth(width);
        });
        components.add(widthSpinner);

        JLabel widthLabel2 = new JLabel(Integer.toString(widthOrg), JLabel.LEFT);
        widthLabel2.setBounds(columnB_posX + 110, columnA_Y, colACompLength, 20);
        components.add(widthLabel2);
        JLabel widthLabel3 = new JLabel("(orig.)", JLabel.LEFT);
        widthLabel3.setBounds(columnB_posX + 150, columnA_Y, colACompLength, 20);
        components.add(widthLabel3);

        // SHEET HEIGHT
        JLabel heightLabel = new JLabel("Height:", JLabel.LEFT);
        heightLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(heightLabel);

        SpinnerModel heightSpinnerModel = new SpinnerNumberModel(sheet.getGraphPanel().getSize().height, 0, Integer.MAX_VALUE, 1);
        JSpinner heightSpinner = new JSpinner(heightSpinnerModel);
        heightSpinner.setBounds(columnB_posX, columnB_Y += 20, colBCompLength / 2, 20);
        heightSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int height = (int) spinner.getValue();
            setSheetHeight(height);
        });
        components.add(heightSpinner);
        if (zoom != 100) {
            widthSpinner.setEnabled(false);
            heightSpinner.setEnabled(false);
        }

        JLabel heightLabel2 = new JLabel(Integer.toString(heightOrg), JLabel.LEFT);
        heightLabel2.setBounds(columnB_posX + 110, columnB_Y, colACompLength, 20);
        components.add(heightLabel2);
        JLabel heightLabel3 = new JLabel("(orig.)", JLabel.LEFT);
        heightLabel3.setBounds(columnB_posX + 150, columnB_Y, colACompLength, 20);
        components.add(heightLabel3);

        // is auto scroll when dragging automatic
        JLabel autoSrclLabel = new JLabel("Autoscroll:", JLabel.LEFT);
        autoSrclLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(autoSrclLabel);

        JCheckBox autoscrollBox = new JCheckBox("", sheet.getGraphPanel().isAutoDragScroll());
        autoscrollBox.setBounds(columnB_posX - 4, columnB_Y, colACompLength, 20);
        autoscrollBox.setLocation(columnB_posX - 4, columnB_Y += 20);
        autoscrollBox.setSelected(false);
        autoscrollBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            setAutoscroll(box.isSelected());
        });
        components.add(autoscrollBox);

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        add(panel);
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************  t-INWARIANTY    ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda pomocnicza konstruktora odpowiedzialna za wypełnienie podokna informacji o inwariantach sieci.
     *
     * @param invariantsData ArrayList[ArrayList[Integer]] - macierz inwariantów
     */
    private void createT_invSubWindow(ArrayList<ArrayList<Integer>> invariantsData) {
        doNotUpdate = true;
        if (invariantsData == null || invariantsData.size() == 0) {
            return;
        } else {
            mode = tINVARIANTS;
            t_invariantsMatrix = invariantsData;
            transitions = overlord.getWorkspace().getProject().getTransitions();
            places = overlord.getWorkspace().getProject().getPlaces();
            overlord.reset.setT_invariantsStatus(true);
        }

        int colA_posX = 10;
        int colB_posX = 100;
        int positionY = 10;
        initiateContainers();

        JLabel chooseInvLabel = new JLabel("T-invariant: ");
        chooseInvLabel.setBounds(colA_posX, positionY, 80, 20);
        components.add(chooseInvLabel);

        String[] invariantHeaders = new String[t_invariantsMatrix.size() + 3];
        invariantHeaders[0] = "---";
        for (int i = 0; i < t_invariantsMatrix.size(); i++) {
            int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(i)).size();
            invariantHeaders[i + 1] = "Inv. #" + (i + 1) + " (size: " + invSize + ")";
        }
        invariantHeaders[invariantHeaders.length - 2] = "null transitions";
        invariantHeaders[invariantHeaders.length - 1] = "inv/trans frequency";

        chooseInvBox = new JComboBox<>(invariantHeaders);
        chooseInvBox.setBounds(colB_posX, positionY, 150, 20);
        chooseInvBox.addActionListener(actionEvent -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int items = comboBox.getItemCount();
            if (comboBox.getSelectedIndex() == 0) {
                selectedT_invIndex = -1;
                showT_invariant(); //clean
            } else if (comboBox.getSelectedIndex() == items - 2) {
                selectedT_invIndex = -1;
                showDeadT_inv(); //show transition without invariants
            } else if (comboBox.getSelectedIndex() == items - 1) {
                selectedT_invIndex = -1;
                showT_invTransFrequency(); //show transition frequency (in invariants)
            } else {
                selectedT_invIndex = comboBox.getSelectedIndex() - 1;
                showT_invariant();
            }
        });
        components.add(chooseInvBox);

        JButton prevButton = new JButton("Previous");
        prevButton.setBounds(colB_posX - 50, positionY += 25, 100, 20);
        prevButton.setMargin(new Insets(0, 0, 0, 0));
        prevButton.setIcon(Tools.getResIcon16("/icons/invViewer/prevIcon.png"));
        prevButton.setToolTipText("Show previous invariant data.");
        prevButton.addActionListener(actionEvent -> {
            int sel = chooseInvBox.getSelectedIndex();
            if (sel > 0) {
                chooseInvBox.setSelectedIndex(sel - 1);
            }
        });
        components.add(prevButton);

        JButton nextButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Next&nbsp;</html>");
        nextButton.setBounds(colB_posX + 55, positionY, 100, 20);
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setIcon(Tools.getResIcon16("/icons/invViewer/nextIcon.png"));
        nextButton.setToolTipText("Show next invariant data.");
        nextButton.addActionListener(actionEvent -> {
            int sel = chooseInvBox.getSelectedIndex();
            int max = chooseInvBox.getItemCount();
            if (sel < max - 1) {
                chooseInvBox.setSelectedIndex(sel + 1);
            }
        });
        components.add(nextButton);

        JButton recalculateTypesButton = new JButton("Refresh");
        //showDetailsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Show<br>&nbsp;&nbsp;&nbsp;&nbsp;details</html>");
        //recalculateTypesButton.setIcon(Tools.getResIcon16("/icons/menu/aaa.png"));
        recalculateTypesButton.setBounds(colA_posX, positionY += 25, 100, 20);
        recalculateTypesButton.setToolTipText("If t-inv types have been determined, this button will refresh the content of the three comboboxes below:");
        recalculateTypesButton.addActionListener(actionEvent -> refreshSubSurCombos());
        components.add(recalculateTypesButton);

        JButton recalculateInvTypesButton = new JButton("Recalculate");
        //showDetailsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Show<br>&nbsp;&nbsp;&nbsp;&nbsp;details</html>");
        recalculateInvTypesButton.setIcon(Tools.getResIcon16("/icons/portal.png"));
        recalculateInvTypesButton.setToolTipText("This will force the program to determine the type of t-invariants (it may take time) and refresh the comboboxes below:");
        recalculateInvTypesButton.setBounds(colA_posX + 105, positionY, 130, 20);
        recalculateInvTypesButton.addActionListener(actionEvent -> {
            try {
                InvariantsCalculator ic = new InvariantsCalculator(true);
                InvariantsTools.analyseInvariantTypes(ic.getCMatrix(), t_invariantsMatrix, true);

                refreshSubSurCombos();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        });
        components.add(recalculateInvTypesButton);

        //sur-sub-non-invariants:
        ArrayList<Integer> typesVector = overlord.getWorkspace().getProject().accessT_InvTypesVector();

        ArrayList<Integer> sursInv = new ArrayList<>();
        ArrayList<Integer> subsInv = new ArrayList<>();
        ArrayList<Integer> nonsInv = new ArrayList<>();

        for (int i = 0; i < typesVector.size(); i++) { //zliczanie tałatajstwa
            if (typesVector.get(i) == 1)
                sursInv.add(i);
            else if (typesVector.get(i) == -1)
                subsInv.add(i);
            if (typesVector.get(i) == 11)
                nonsInv.add(i);
        }

        String[] surHeaders = new String[sursInv.size() + 1];
        surHeaders[0] = "---";
        if (sursInv.size() > 0) {
            for (int i = 0; i < sursInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(sursInv.get(i))).size();
                surHeaders[i + 1] = "Inv. #" + (sursInv.get(i) + 1) + " (size: " + invSize + ")";
            }
        }
        String[] subHeaders = new String[subsInv.size() + 1];
        subHeaders[0] = "---";
        if (subsInv.size() > 0) {
            for (int i = 0; i < subsInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(subsInv.get(i))).size();
                subHeaders[i + 1] = "Inv. #" + (subsInv.get(i) + 1) + " (size: " + invSize + ")";
            }
        }
        String[] nonsHeaders = new String[nonsInv.size() + 1];
        nonsHeaders[0] = "---";
        if (nonsInv.size() > 0) {
            for (int i = 0; i < nonsInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(nonsInv.get(i))).size();
                nonsHeaders[i + 1] = "Inv. #" + (nonsInv.get(i) + 1) + " (size: " + invSize + ")";
            }
        }

        JLabel surLabel1 = new JLabel("Sur-inv: ");
        surLabel1.setBounds(colA_posX, positionY += 25, 80, 20);
        components.add(surLabel1);

        chooseSurInvBox = new JComboBox<>(surHeaders);
        chooseSurInvBox.setBounds(colB_posX, positionY, 150, 20);
        chooseSurInvBox.addActionListener(actionEvent -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            if (comboBox.getSelectedIndex() == 0) {
                selectedT_invIndex = -1;
                showT_invariant(); //clean
            } else {
                try {
                    String txt = (String) comboBox.getSelectedItem();
                    txt = Objects.requireNonNull(txt).substring(txt.indexOf("#") + 1);
                    txt = txt.substring(0, txt.indexOf(" "));
                    int index = Integer.parseInt(txt);

                    selectedT_invIndex = index - 1;
                    showT_invariant();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        components.add(chooseSurInvBox);

        JLabel subLabel1 = new JLabel("Sub-inv: ");
        subLabel1.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(subLabel1);

        chooseSubInvBox = new JComboBox<>(subHeaders);
        chooseSubInvBox.setBounds(colB_posX, positionY, 150, 20);
        chooseSubInvBox.addActionListener(actionEvent -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            if (comboBox.getSelectedIndex() == 0) {
                selectedT_invIndex = -1;
                showT_invariant(); //clean
            } else {
                try {
                    String txt = (String) comboBox.getSelectedItem();
                    txt = Objects.requireNonNull(txt).substring(txt.indexOf("#") + 1);
                    txt = txt.substring(0, txt.indexOf(" "));
                    int index = Integer.parseInt(txt);

                    selectedT_invIndex = index - 1;
                    showT_invariant();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        components.add(chooseSubInvBox);

        JLabel noneLabel1 = new JLabel("None-inv: ");
        noneLabel1.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(noneLabel1);

        chooseNoneInvBox = new JComboBox<>(subHeaders);
        chooseNoneInvBox.setBounds(colB_posX, positionY, 150, 20);
        chooseNoneInvBox.addActionListener(actionEvent -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            if (comboBox.getSelectedIndex() == 0) {
                selectedT_invIndex = -1;
                showT_invariant(); //clean
            } else {
                try {
                    String txt = (String) comboBox.getSelectedItem();
                    txt = Objects.requireNonNull(txt).substring(txt.indexOf("#") + 1);
                    txt = txt.substring(0, txt.indexOf(" "));
                    int index = Integer.parseInt(txt);

                    selectedT_invIndex = index - 1;
                    showT_invariant();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        });
        components.add(chooseNoneInvBox);

        JButton showDetailsButton = new JButton();
        showDetailsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Show<br>&nbsp;&nbsp;&nbsp;&nbsp;details</html>");
        showDetailsButton.setIcon(Tools.getResIcon32("/icons/menu/menu_invViewer.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 30, 120, 32);
        showDetailsButton.addActionListener(actionEvent -> {
            if (selectedT_invIndex == -1)
                return;
            new HolmesInvariantsViewer(selectedT_invIndex);
        });
        components.add(showDetailsButton);

        JCheckBox markMCTcheckBox = new JCheckBox("Color MCT");
        markMCTcheckBox.setBounds(colA_posX + 130, positionY - 5, 120, 20);
        markMCTcheckBox.setSelected(false);
        markMCTcheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            markMCT = abstractButton.getModel().isSelected();
            showT_invariant();
        });
        components.add(markMCTcheckBox);

        JCheckBox glowINVcheckBox = new JCheckBox("Transitions glow");
        glowINVcheckBox.setBounds(colA_posX + 130, positionY + 15, 120, 20);
        glowINVcheckBox.setSelected(true);
        glowINVcheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            glowT_inv = abstractButton.getModel().isSelected();
            showT_invariant();
        });
        components.add(glowINVcheckBox);

        t_invNameField = new JTextArea();
        t_invNameField.setLineWrap(true);
        t_invNameField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                if (field != null)
                    changeT_invName(field.getText());
            }
        });

        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BorderLayout());
        descPanel.add(new JScrollPane(t_invNameField), BorderLayout.CENTER);
        descPanel.setBounds(colA_posX, positionY += 40, 250, 80);
        components.add(descPanel);

        JCheckBox markAreaCheckBox = new JCheckBox("Invariant-net structure painted");
        markAreaCheckBox.setBounds(colA_posX, positionY += 85, 200, 20);
        markAreaCheckBox.setSelected(true);
        markAreaCheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            invStructure = abstractButton.getModel().isSelected();
            showT_invariant();
        });
        components.add(markAreaCheckBox);

        //TPN:
        JLabel labelTime1 = new JLabel("Min. time: ");
        labelTime1.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(labelTime1);
        minTimeLabel = new JLabel("---");
        minTimeLabel.setBounds(colB_posX, positionY, 80, 20);
        components.add(minTimeLabel);

        JLabel labelTime2 = new JLabel("Avg. time: ");
        labelTime2.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(labelTime2);
        avgTimeLabel = new JLabel("---");
        avgTimeLabel.setBounds(colB_posX, positionY, 80, 20);
        components.add(avgTimeLabel);

        JLabel labelTime3 = new JLabel("Max. time: ");
        labelTime3.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(labelTime3);
        maxTimeLabel = new JLabel("---");
        maxTimeLabel.setBounds(colB_posX, positionY, 80, 20);
        components.add(maxTimeLabel);

        JLabel labelTime4 = new JLabel("Structure:");
        labelTime4.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(labelTime4);
        structureLabel = new JLabel("---");
        structureLabel.setBounds(colB_posX, positionY, 200, 20);
        components.add(structureLabel);

        doNotUpdate = false;
        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    /**
     * Metoda odświeża zawartość comboBoxów dla niekanonicznych "inwariantów".
     */
    public void refreshSubSurCombos() {
        ArrayList<Integer> typesVector = overlord.getWorkspace().getProject().accessT_InvTypesVector();

        ArrayList<Integer> sursInv = new ArrayList<>();
        ArrayList<Integer> subsInv = new ArrayList<>();
        ArrayList<Integer> nonsInv = new ArrayList<>();

        for (int i = 0; i < typesVector.size(); i++) { //zliczanie tałatajstwa
            if (typesVector.get(i) == 1)
                sursInv.add(i);
            else if (typesVector.get(i) == -1)
                subsInv.add(i);
            if (typesVector.get(i) == 11)
                nonsInv.add(i);
        }

        String[] surHeaders = new String[sursInv.size() + 1];
        surHeaders[0] = "---";
        if (sursInv.size() > 0) {
            for (int i = 0; i < sursInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(sursInv.get(i))).size();
                surHeaders[i + 1] = "Inv. #" + (sursInv.get(i) + 1) + " (size: " + invSize + ")";
            }
        }
        String[] subHeaders = new String[subsInv.size() + 1];
        subHeaders[0] = "---";
        if (subsInv.size() > 0) {
            for (int i = 0; i < subsInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(subsInv.get(i))).size();
                subHeaders[i + 1] = "Inv. #" + (subsInv.get(i) + 1) + " (size: " + invSize + ")";
            }
        }
        String[] nonsHeaders = new String[nonsInv.size() + 1];
        nonsHeaders[0] = "---";
        if (nonsInv.size() > 0) {
            for (int i = 0; i < nonsInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(nonsInv.get(i))).size();
                nonsHeaders[i + 1] = "Inv. #" + (nonsInv.get(i) + 1) + " (size: " + invSize + ")";
            }
        }

        chooseSurInvBox.setModel(new DefaultComboBoxModel<>(surHeaders));
        chooseSubInvBox.setModel(new DefaultComboBoxModel<>(subHeaders));
        chooseNoneInvBox.setModel(new DefaultComboBoxModel<>(nonsHeaders));
    }

    /**
     * Zmiana nazwy inwariantu.
     *
     * @param newName String - nowa nazwa
     */
    private void changeT_invName(String newName) {
        if (selectedT_invIndex == -1)
            return;

        overlord.getWorkspace().getProject().accessT_InvDescriptions().set(selectedT_invIndex, newName);
    }

    /**
     * Metoda odpowiedzialna za podświetlanie inwariantów na rysunku sieci.
     */
    private void showT_invariant() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedT_invIndex != -1) {
            ArrayList<Integer> invariant = t_invariantsMatrix.get(selectedT_invIndex);
            if (transitions.size() != invariant.size()) {
                transitions = overlord.getWorkspace().getProject().getTransitions();
                if (transitions == null || transitions.size() != invariant.size()) {
                    overlord.log("Critical error in t-invariants subwindow. "
                            + "T-invariants size differ from transition set cardinality!", "error", true);
                    return;
                }
            }

            ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();
            ArrayList<Transition> invTransitions = new ArrayList<>();
            ColorPalette cp = new ColorPalette();
            for (int t = 0; t < invariant.size(); t++) {
                int fireValue = invariant.get(t);
                Transition trans = transitions.get(t);
                if (fireValue == 0) {
                    trans.qSimDrawed = false;
                    trans.qSimArcSign = false;
                    continue;
                }

                invTransitions.add(trans);
                if (markMCT) {
                    int mctNo = transMCTvector.get(t);
                    if (mctNo == -1) {
                        trans.setGlowedINV(glowT_inv, fireValue);
                    } else {
                        trans.setColorWithNumber(true, cp.getColor(mctNo), false, fireValue, true, "[MCT" + (mctNo + 1) + "]");
                        trans.setGlowedINV(false, fireValue);
                    }
                } else {
                    trans.setGlowedINV(glowT_inv, fireValue);
                }

                if (invStructure) {
                    trans.qSimDrawed = true;
                    trans.qSimArcSign = true;
                    trans.qSimFillColor = new Color(0, 102, 0);
                    for (ElementLocation el : trans.getElementLocations()) {
                        el.qSimArcSign = true;
                        //el.qSimDrawed = true;
                    }
                }
            }

            if (invStructure) {
                for (Place place : places) { //zaznacz wszystkie EL struktury t-inwariantu
                    for (ElementLocation el : place.getElementLocations()) { //dla każdej lokalizacji miejsca
                        boolean inFound = false;
                        for (Arc inArc : el.getInArcs()) { //sprawdź łuki wejściowe
                            if (((Transition) inArc.getStartNode()).qSimDrawed) {
                                inFound = true; //to znaczy, że prowadzi tu łuk z tranzycji inwariantu
                                break;
                            }
                        }
                        if (!inFound)
                            continue;

                        for (ElementLocation el2 : place.getElementLocations()) {
                            for (Arc outArc : el2.getOutArcs()) { //sprawdź łuki wyjściowe
                                if (((Transition) outArc.getEndNode()).qSimDrawed) {
                                    el2.qSimArcSign = true;    //to znaczy, że prowadzi stąd łuk do tranzycji inwariantu
                                    el2.qSimDrawed = true;
                                    el.qSimArcSign = true;
                                    el.qSimDrawed = true;
                                    place.qSimFillColor = new Color(0, 102, 0);
                                    place.qSimDrawed = true;

                                    place.qSimTokens = 0; //jakie to płytkie :)
                                    place.qSimOvalColor = new Color(0, 102, 0);
                                    place.qSimOvalSize = 0;
                                    break;
                                }
                            }
                        }
                    }


                }

                for (Arc arc : pn.getArcs()) {
                    if (arc.getStartLocation().qSimArcSign && arc.getEndLocation().qSimArcSign) {
                        arc.qSimForcedArc = true;
                        arc.qSimForcedColor = new Color(0, 102, 0);
                    }
                }
            }

            //subwindow fields:
            String name = overlord.getWorkspace().getProject().accessT_InvDescriptions().get(selectedT_invIndex);
            t_invNameField.setText(name);

            ArrayList<Double> timeVector = TimeComputations.getT_InvTimeValues(invariant, transitions);
            if (timeVector != null) {
                minTimeLabel.setText(String.format("%.2f", timeVector.get(0) + timeVector.get(3)));
                avgTimeLabel.setText(String.format("%.2f", timeVector.get(2) + timeVector.get(3)));
                maxTimeLabel.setText(String.format("%.2f", timeVector.get(1) + timeVector.get(3)));

                String structText = "";
                if (timeVector.get(5) > 0) {
                    structText += "" + timeVector.get(5).intValue() + "xTPN; ";
                }
                if (timeVector.get(6) > 0) {
                    structText += "" + timeVector.get(6).intValue() + "xDPN; ";
                }
                if (timeVector.get(7) > 0) {
                    structText += "" + timeVector.get(7).intValue() + "xTDPN; ";
                }
                if (timeVector.get(4) > 0) {
                    structText += "" + timeVector.get(4).intValue() + "xPN; ";
                }
                structureLabel.setText(structText);
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    /**
     * Metoda pokazująca w ilu inwariantach występuje każda tranzycja
     */
    private void showT_invTransFrequency() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        //ArrayList<Integer> freqVector = InvariantsTools.getFrequency(t_invariantsMatrix, false);
        ArrayList<Integer> freqVector = InvariantsTools.getFrequencyRealInvariants(t_invariantsMatrix, false);
        ArrayList<Transition> transitions_tmp = overlord.getWorkspace().getProject().getTransitions();


        int max_freq = freqVector.stream()
                .mapToInt(v -> v)
                .max().orElseThrow(NoSuchElementException::new);

        if (freqVector == null) {
            JOptionPane.showMessageDialog(null, "T-invariants data unavailable.", "No t-invariants", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (int i = 0; i < freqVector.size(); i++) {
                Transition realT = transitions_tmp.get(i);

                if (freqVector.get(i) != 0) {
                    //realT.setGlowedINV(glowT_inv, freqVector.get(i));

                    double fr = (double) freqVector.get(i) / (double) max_freq;
                    //double fr = freqVector.get(i) /max_freq;

                    int usuwane = (int) ((1 - fr) * 255);
                    //System.out.println(usuwane);


                    //int[] color = getRGB(usuwane);
                    //new Color(color[0],color[1],color[2]

                    realT.setColorWithNumber(true, getDiscColor(fr), true, freqVector.get(i), false, "");
                    System.out.println(realT.getName() + " trans \t" + realT.getID() + " \t " + fr + " \t " + freqVector.get(i));

                } else {
                    realT.setColorWithNumber(true, Color.gray, true, 0, false, "");
                    System.out.println(realT.getName() + " trans \t" + realT.getID() + " \t " + 0 + " \t " + 0);
                }
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    public static Color getDiscColor(double fr) {
        if (fr == 1) {//rgba(87,187,138,255)
            return new Color(87, 187, 138, 255);
        } else if (fr > 0.9) {
            return new Color(104, 194, 150, 255);
        } else if (fr > 0.8) {
            return new Color(121, 201, 162, 255);
        } else if (fr > 0.7) {
            return new Color(138, 208, 174, 255);
        } else if (fr > 0.6) {
            return new Color(55, 215, 185, 255);
        } else if (fr > 0.5) {
            return new Color(171, 221, 197, 255);
        } else if (fr > 0.4) {
            return new Color(171, 221, 197, 255);
        } else if (fr > 0.3) {
            return new Color(205, 235, 220, 255);
        } else if (fr > 0.2) {
            return new Color(222, 242, 232, 255);
        } else if (fr > 0.1) {
            return new Color(222, 242, 232, 255);
        }
        return Color.white;
    }

    public static int[] getRGB(int gray) {
        double[] WEIGHTS = {0.2989, 0.5870, 0.1140};
        int[] rgb = new int[3];

        for (int i = 0; i < 3; i++) {
            rgb[i] = (int) (gray / WEIGHTS[i]);
            if (rgb[i] < 256)
                return rgb; // Successfully "distributed" all of gray, return it

            // Not quite there, cut it...
            rgb[i] = 255;
            // And distribute the remaining on the rest of the RGB components:
            gray -= (int) (255 * WEIGHTS[i]);
        }

        return rgb;
    }

    /**
     * Metoda pomocnicza do zaznaczania tranzycji nie pokrytych inwariantami.
     */
    private void showDeadT_inv() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        HolmesNotepad note = new HolmesNotepad(640, 480);
        note.addTextLineNL("Transitions not covered by t-invariants:", "text");

        ArrayList<Integer> deadTrans = InvariantsTools.detectUncovered(t_invariantsMatrix, true);
        ArrayList<Transition> transitions_tmp = overlord.getWorkspace().getProject().getTransitions();
        int counter = 0;
        if (deadTrans == null) {
            JOptionPane.showMessageDialog(null, "T-invariants data unavailable.", "No t-invariants", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (int deadOne : deadTrans) {
                Transition realT = transitions_tmp.get(deadOne);
                String t1 = Tools.setToSize("t" + deadOne, 5, false);
                note.addTextLineNL(t1 + " | " + realT.getName(), "text");
                realT.setGlowedINV(true, 0);
                counter++;
            }
        }
        if (counter > 0) {
            note.setCaretFirstLine();
            note.setVisible(true);
        } else {
            note.dispose();
        }

        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************  p-INWARIANTY    ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda pomocnicza konstruktora odpowiedzialna za wypełnienie podokna informacji o p-inwariantach sieci.
     *
     * @param pInvData ArrayList[ArrayList[Integer]] - macierz inwariantów
     */
    private void createP_invSubWindow(ArrayList<ArrayList<Integer>> pInvData) {
        doNotUpdate = true;
        if (pInvData == null || pInvData.size() == 0) {
            return;
        } else {
            mode = pINVARIANTS;
            p_invariantsMatrix = pInvData;
            places = overlord.getWorkspace().getProject().getPlaces();
            overlord.reset.setP_invariantsStatus(true);
        }

        int colA_posX = 10;
        int colB_posX = 100;
        int positionY = 10;
        initiateContainers();

        JLabel chooseInvLabel = new JLabel("P-invariant: ");
        chooseInvLabel.setBounds(colA_posX, positionY, 80, 20);
        components.add(chooseInvLabel);

        String[] invariantHeaders = new String[p_invariantsMatrix.size() + 3];
        invariantHeaders[0] = "---";
        for (int i = 0; i < p_invariantsMatrix.size(); i++) {
            int invSize = InvariantsTools.getSupport(p_invariantsMatrix.get(i)).size();
            invariantHeaders[i + 1] = "Inv. #" + (i + 1) + " (size: " + invSize + ")";
        }
        invariantHeaders[invariantHeaders.length - 2] = "null places";
        invariantHeaders[invariantHeaders.length - 1] = "inv/places frequency";

        JComboBox<String> chooseInvBox = new JComboBox<>(invariantHeaders);
        chooseInvBox.setBounds(colB_posX, positionY, 150, 20);
        chooseInvBox.addActionListener(actionEvent -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int items = comboBox.getItemCount();
            if (comboBox.getSelectedIndex() == 0) {
                selectedP_invIndex = -1;
                showP_invariant(); //clean
            } else if (comboBox.getSelectedIndex() == items - 2) {
                selectedP_invIndex = -1;
                showDeadP_inv(); //show transition without invariants
            } else if (comboBox.getSelectedIndex() == items - 1) {
                selectedP_invIndex = -1;
                showP_invTransFrequency(); //show transition frequency (in invariants)
            } else {
                selectedP_invIndex = comboBox.getSelectedIndex() - 1;
                showP_invariant();
            }
        });
        components.add(chooseInvBox);

        JButton showDetailsButton = new JButton();
        showDetailsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Show<br>&nbsp;&nbsp;&nbsp;&nbsp;details</html>");
        showDetailsButton.setIcon(Tools.getResIcon32("/icons/menu/menu_invViewer.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 30, 120, 32);
        showDetailsButton.addActionListener(actionEvent -> {
            if (selectedP_invIndex == -1)
                return;
            //new HolmesInvariantsViewer(selectedT_invIndex);
        });
        components.add(showDetailsButton);

        p_invNameField = new JTextArea();
        p_invNameField.setLineWrap(true);
        p_invNameField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                if (field != null)
                    changeP_invName(field.getText());
            }
        });

        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BorderLayout());
        descPanel.add(new JScrollPane(p_invNameField), BorderLayout.CENTER);
        descPanel.setBounds(colA_posX, positionY += 40, 250, 80);
        components.add(descPanel);

        doNotUpdate = false;
        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    /**
     * Zmiana nazwy p-inwariantu.
     *
     * @param newName String - nowa nazwa
     */
    private void changeP_invName(String newName) {
        if (selectedT_invIndex == -1)
            return;

        overlord.getWorkspace().getProject().accessP_InvDescriptions().set(selectedP_invIndex, newName);
    }

    /**
     * Metoda odpowiedzialna za podświetlanie inwariantów na rysunku sieci.
     */
    private void showP_invariant() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedP_invIndex != -1) {
            ArrayList<Integer> invariant = p_invariantsMatrix.get(selectedP_invIndex);
            if (places.size() != invariant.size()) {
                places = overlord.getWorkspace().getProject().getPlaces();
                if (places == null || places.size() != invariant.size()) {
                    overlord.log("Critical error in p-invariants subwindow. "
                            + "P-invariants size differ from transition set cardinality!", "error", true);
                    return;
                }
            }

            for (int p = 0; p < invariant.size(); p++) {
                int value = invariant.get(p);
                if (value == 0)
                    continue;

                places.get(p).setColorWithNumber(true, EditorResources.glowPlaceColorLevelBlue, false, -1, false, "");
            }
            //name field:
            String name = overlord.getWorkspace().getProject().accessP_InvDescriptions().get(selectedP_invIndex);
            p_invNameField.setText(name);

        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    /**
     * Metoda pokazująca w ilu inwariantach występuje każda tranzycja
     */
    private void showP_invTransFrequency() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        ArrayList<Integer> freqVector = InvariantsTools.getFrequency(p_invariantsMatrix, false);
        ArrayList<Place> places_tmp = pn.getPlaces();

        if (freqVector == null) {
            JOptionPane.showMessageDialog(null, "P-invariants data unavailable.", "No p-invariants", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (int i = 0; i < freqVector.size(); i++) {
                Place realP = places_tmp.get(i);
                if (freqVector.get(i) != 0) {
                    realP.setColorWithNumber(true, EditorResources.glowPlaceColorLevelBlue, true, freqVector.get(i), false, "");
                } else
                    realP.setColorWithNumber(true, EditorResources.glowPlaceColorLevelRed, true, 0, false, "");
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    /**
     * Metoda pomocnicza do zaznaczania tranzycji nie pokrytych inwariantami.
     */
    private void showDeadP_inv() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        HolmesNotepad note = new HolmesNotepad(640, 480);
        note.addTextLineNL("Places not covered by p-invariants:", "text");

        ArrayList<Integer> deadPlaces = InvariantsTools.detectUncovered(p_invariantsMatrix, false);
        ArrayList<Place> places_tmp = overlord.getWorkspace().getProject().getPlaces();
        int counter = 0;
        if (deadPlaces == null) {
            JOptionPane.showMessageDialog(null, "P-invariants data unavailable.", "No p-invariants", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (int deadOne : deadPlaces) {
                Place realP = places_tmp.get(deadOne);
                String p1 = Tools.setToSize("p" + deadOne, 5, false);
                note.addTextLineNL(p1 + " | " + realP.getName(), "text");
                realP.setColorWithNumber(true, EditorResources.glowPlaceColorLevelRed, false, -1, false, "");
                //realT.setGlowedINV(true, 0);
                counter++;
            }
        }
        if (counter > 0) {
            note.setCaretFirstLine();
            note.setVisible(true);
        } else {
            note.dispose();
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
     *
     * @param mct ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
     */
    @SuppressWarnings("unchecked")
    private void createMCTSubWindow(ArrayList<ArrayList<Transition>> mct) {


        if (mct == null || mct.size() == 0) {
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
            if (i < mctGroups.size() - 1)
                mctHeaders[i + 1] = "MCT #" + (i + 1) + " (size: " + mctGroups.get(i).size() + ")";
            else {
                mctHeaders[i + 1] = "No-MCT transitions";
                mctHeaders[i + 2] = "Show all";
            }
        }

        // getting the data
        JLabel chooseMctLabel = new JLabel("Choose MCT: ");
        chooseMctLabel.setBounds(colA_posX, positionY, 80, 20);
        components.add(chooseMctLabel);

        JComboBox<String> chooseMctBox = new JComboBox<>(mctHeaders);
        chooseMctBox.setBounds(colB_posX, positionY, 150, 20);
        chooseMctBox.addActionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                selectedMCTindex = -1;
                allMCTselected = false;
                showMct();
                MCTnameField.setText("");
            } else if (selected == comboBox.getItemCount() - 1) {
                allMCTselected = true;
                showAllColors();
            } else {
                selectedMCTindex = selected - 1;
                allMCTselected = false;
                showMct();
            }
        });
        components.add(chooseMctBox);

        JButton showDetailsButton = new JButton();
        showDetailsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Show<br>&nbsp;&nbsp;&nbsp;&nbsp;details</html>");
        showDetailsButton.setIcon(Tools.getResIcon32("/icons/invViewer/showInNotepad.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 30, 120, 32);
        showDetailsButton.addActionListener(actionEvent -> showMCTNotepad());
        components.add(showDetailsButton);

        JCheckBox glowCheckBox = new JCheckBox("Different colors");
        glowCheckBox.setBounds(colA_posX + 130, positionY - 5, 120, 20);
        glowCheckBox.setSelected(false);
        glowCheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            colorMCT = abstractButton.getModel().isSelected();
            if (allMCTselected)
                showAllColors();
            else
                showMct();
        });
        components.add(glowCheckBox);

		/*
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
		*/

        MCTnameField = new JTextArea();
        MCTnameField.setLineWrap(true);
        MCTnameField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                if (field != null)
                    changeMCTname(field.getText());
            }
        });

        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BorderLayout());
        descPanel.add(new JScrollPane(MCTnameField), BorderLayout.CENTER);
        descPanel.setBounds(colA_posX, positionY += 40, 250, 80);
        components.add(descPanel);


        doNotUpdate = false;

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    /**
     * Metoda zmiany nazwy zbioru MCT.
     *
     * @param newName String - nowa nazwa
     */
    private void changeMCTname(String newName) {
        if (selectedMCTindex == -1)
            return;

        overlord.getWorkspace().getProject().accessMCTnames().set(selectedMCTindex, newName);
    }

    /**
     * Metoda pokazująca za pomocą notatnika dane o zbiorze MCT.
     */
    private void showMCTNotepad() {
        if (selectedMCTindex == -1)
            return;

        HolmesNotepad note = new HolmesNotepad(800, 600);

        ArrayList<Transition> mct = mctGroups.get(selectedMCTindex);
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        int size = mct.size();
        if (selectedMCTindex == mctGroups.size() - 1) {
            note.addTextLineNL("Trivial MCT-transitions (" + size + "):", "text");
        } else {
            note.addTextLineNL("Transitions (" + size + ") of MCT #" + (selectedMCTindex + 1), "text");
        }

        for (Transition transition : mct) {
            int globalIndex = transitions.lastIndexOf(transition);
            String t1 = Tools.setToSize("t" + globalIndex, 5, false);
            note.addTextLineNL(t1 + transition.getName(), "text");
        }

        note.setCaretFirstLine();
        note.setVisible(true);
    }

    /**
     * Metoda odpowiedzialna za pokazanie szczegółów wybranego zbioru MCT.
     */
    private void showMct() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedMCTindex == -1)
            return;

        ArrayList<Transition> mct = mctGroups.get(selectedMCTindex);
        int size = mctGroups.size();
        ColorPalette cp = new ColorPalette();
        for (Transition transition : mct) {
            if (!colorMCT) {
                transition.setGlowed_MTC(true);
            } else {
                if (selectedMCTindex == size - 1)
                    transition.setColorWithNumber(true, cp.getColor(selectedMCTindex), false, 0, true, "[trivial]");
                else
                    transition.setColorWithNumber(true, cp.getColor(selectedMCTindex), false, 0, true, "[MCT" + (selectedMCTindex + 1) + "]");
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();

        //name field:
        String name = overlord.getWorkspace().getProject().accessMCTnames().get(selectedMCTindex);
        MCTnameField.setText(name);
    }

    /**
     * Metoda odpowiedzialna za pokazanie wszystkich nietrywalniach zbiorów MCT w kolorach.
     */
    private void showAllColors() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        ColorPalette cp = new ColorPalette();
        for (int m = 0; m < mctGroups.size() - 1; m++) {
            Color currentColor = cp.getColor();
            ArrayList<Transition> mct = mctGroups.get(m);
            for (Transition transition : mct) {
                if (overlord.getSettingsManager().getValue("mctNameShow").equals("1"))
                    transition.setColorWithNumber(true, currentColor, false, m, true, "MCT #" + (m + 1) + " (" + mct.size() + ")");
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
     *
     * @param clusteringData int - w zależności od tego, tworzy dane okno
     */
    private void createClustersSubWindow(ClusterDataPackage clusteringData) {
        initiateContainers();
        doNotUpdate = true;
        if (clusteringData == null || clusteringData.dataMatrix.size() == 0) {
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
        JLabel label6 = new JLabel(clusterColorsData.clNumber + "");
        label6.setBounds(colB_posX, positionY, 80, 20);
        components.add(label6);

        JLabel chooseInvLabel = new JLabel("Selected: ");
        chooseInvLabel.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(chooseInvLabel);

        // PRZEWIJALNA LISTA KLASTRÓW:
        String[] clustersHeaders = new String[clusterColorsData.dataMatrix.size() + 1];
        clustersHeaders[0] = "---";
        for (int i = 0; i < clusterColorsData.dataMatrix.size(); i++) {
            clustersHeaders[i + 1] = "Cluster " + (i + 1)
                    + " (size: " + clusterColorsData.clSize.get(i) + " inv.)";
        }

        chooseCluster = new JComboBox<>(clustersHeaders);
        chooseCluster.setBounds(colB_posX, positionY, 180, 20);
        chooseCluster.addActionListener(actionEvent -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            if (comboBox.getSelectedIndex() == 0) {
                selectedClusterIndex = -1;
                showClusters();
                fillClustInvCombo();
            } else {
                selectedClusterIndex = comboBox.getSelectedIndex() - 1;
                showClusters();
                fillClustInvCombo();
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
        transFiringMode.setBounds(colA_posX - 3, positionY += 20, 220, 20);
        transFiringMode.setSelected(false);
        transFiringMode.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            clusterColorsData.showFirings = abstractButton.getModel().isSelected();
            int selected = chooseCluster.getSelectedIndex();
            chooseCluster.setSelectedIndex(selected);
            //chooseCluster.setSelectedItem(selected);
        });
        components.add(transFiringMode);

        JCheckBox scaleMode = new JCheckBox("Show scaled colors");
        scaleMode.setBounds(colA_posX - 3, positionY += 20, 170, 20);
        scaleMode.setSelected(false);
        scaleMode.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            clusterColorsData.showScale = abstractButton.getModel().isSelected();
            int selected = chooseCluster.getSelectedIndex();
            chooseCluster.setSelectedIndex(selected);
            //chooseCluster.setSelectedItem(selected);
        });
        components.add(scaleMode);

        JCheckBox mctMode = new JCheckBox("Show MCT sets");
        mctMode.setBounds(colA_posX - 3, positionY += 20, 120, 20);
        mctMode.setSelected(false);
        mctMode.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            clustersMCT = abstractButton.getModel().isSelected();
            int selected = chooseCluster.getSelectedIndex();
            chooseCluster.setSelectedIndex(selected);
        });
        components.add(mctMode);

        JButton showDetailsButton = new JButton();
        showDetailsButton.setText("<html>&nbsp;Show&nbsp;<br>details</html>");
        showDetailsButton.setIcon(Tools.getResIcon22("/icons/clustWindow/showInfo.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 30, 130, 30);
        showDetailsButton.addActionListener(actionEvent -> showClustersNotepad());
        components.add(showDetailsButton);

        JButton screenshotsButton = new JButton();
        screenshotsButton.setText("<html>&nbsp;Export&nbsp;<br>pictures</html>");
        screenshotsButton.setIcon(Tools.getResIcon22("/icons/clustWindow/exportPictures.png"));
        screenshotsButton.setBounds(colA_posX + 135, positionY, 130, 30);
        screenshotsButton.addActionListener(actionEvent -> dropClustersToFiles());
        components.add(screenshotsButton);

        progressBar = new JProgressBar();
        progressBar.setBounds(colA_posX + 135, positionY - 5, 130, 35);
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
        chooseClusterInv = new JComboBox<>(clustersInvHeaders);
        chooseClusterInv.setBounds(colB_posX, positionY, 180, 20);
        chooseClusterInv.addActionListener(actionEvent -> {
            if (doNotUpdate)
                return;
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            if (comboBox.getSelectedIndex() == 0) {
                selectedClusterInvIndex = -1;
                showClusters();
            } else {
                selectedClusterInvIndex = comboBox.getSelectedIndex() - 1;
                showClusterInv();
            }
        });
        components.add(chooseClusterInv);

        JButton showTimeDetailsButton = new JButton();
        showTimeDetailsButton.setText("<html>&nbsp;Time&nbsp;<br>details</html>");
        showTimeDetailsButton.setIcon(Tools.getResIcon22("/icons/clustWindow/showInfo.png"));
        showTimeDetailsButton.setBounds(colA_posX, positionY += 30, 130, 30);
        showTimeDetailsButton.addActionListener(actionEvent -> showTimeDataNotepad());
        components.add(showTimeDetailsButton);

        JButton showClustersDataButton = new JButton();
        showClustersDataButton.setText("<html>&nbsp;Clusters&nbsp;<br>details</html>");
        showClustersDataButton.setIcon(Tools.getResIcon22("/icons/clustWindow/showInfo.png"));
        showClustersDataButton.setBounds(colA_posX, positionY += 35, 130, 30);
        showClustersDataButton.addActionListener(actionEvent -> showClustersData());
        components.add(showClustersDataButton);

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        doNotUpdate = true;
        add(panel);
    }

    private void showTimeDataNotepad() {
        if (selectedClusterIndex == -1)
            return;

        PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
        ArrayList<Transition> transitions = pn.getTransitions();
        ArrayList<ArrayList<Integer>> invMatrix = pn.getT_InvMatrix();
        if (invMatrix == null || invMatrix.size() == 0)
            return;

        ArrayList<Integer> clInvariants = clusterColorsData.clustersInvariants.get(selectedClusterIndex);
        ArrayList<ArrayList<Integer>> invSubMatrix = new ArrayList<>();
        for (int i : clInvariants) {
            invSubMatrix.add(invMatrix.get(i));
        }

        ArrayList<Double> avgStatsVector = new ArrayList<>();

        HolmesNotepad note = new HolmesNotepad(800, 600);

        note.addTextLineNL("", "text");
        note.addTextLineNL("Cluster: " + (selectedClusterIndex + 1) + " (" + clusterColorsData.clSize.get(selectedClusterIndex) + " inv.) alg.: " + clusterColorsData.algorithm
                + " metric: " + clusterColorsData.metric, "text");
        note.addTextLineNL("", "text");
        note.addTextLineNL("T-invariants and their time values:", "text");

        note.addTextLineNL(" No.           Min.         Avg.          Max.    PN   TPN  DPN TDPN", "text");
        for (int i = 0; i < invSubMatrix.size(); i++) {
            ArrayList<Integer> invariant = invSubMatrix.get(i);
            ArrayList<Double> timeVector = TimeComputations.getT_InvTimeValues(invariant, transitions);
            avgStatsVector.add(timeVector.get(2) + timeVector.get(3));

            String line = "";
            String eftStr = String.format("%.2f", timeVector.get(0) + timeVector.get(3));
            String lftStr = String.format("%.2f", timeVector.get(1) + timeVector.get(3));
            String avgStr = String.format("%.2f", timeVector.get(2) + timeVector.get(3));

            String normal = "" + timeVector.get(4).intValue();
            String tpn = "" + timeVector.get(5).intValue();
            String dpn = "" + timeVector.get(6).intValue();
            String tdpn = "" + timeVector.get(7).intValue();

            line += Tools.setToSize("i_" + clInvariants.get(i), 5, false);
            line += Tools.setToSize(eftStr, 14, true);
            line += Tools.setToSize(avgStr, 14, true);
            line += Tools.setToSize(lftStr, 14, true);
            line += "    ";
            line += Tools.setToSize(normal, 5, false);
            line += Tools.setToSize(tpn, 5, false);
            line += Tools.setToSize(dpn, 5, false);
            line += Tools.setToSize(tdpn, 5, false);
            note.addTextLineNL(line, "text");
        }

        //średnie, odchylenia:
        double avgMean = 0;
        for (double avg : avgStatsVector) {
            avgMean += avg;
        }
        avgMean /= (double) avgStatsVector.size();

        double variance = 0;
        for (double avg : avgStatsVector) {
            variance += (avgMean - avg) * (avgMean - avg);
        }
        variance /= (double) avgStatsVector.size();

        double stdDev = Math.sqrt(variance);

        note.addTextLineNL("", "text");
        note.addTextLineNL("Mean of average times: " + avgMean, "text");
        note.addTextLineNL("Variance: " + variance, "text");
        note.addTextLineNL("Standard deviation: " + stdDev, "text");

        note.setCaretFirstLine();
        note.setVisible(true);
    }

    //TODO
    private void showClustersData() {
        PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
        ArrayList<Transition> transitions = pn.getTransitions();
        ArrayList<ArrayList<Integer>> invMatrix = pn.getT_InvMatrix();
        if (invMatrix == null || invMatrix.size() == 0)
            return;

        HolmesNotepad note = new HolmesNotepad(800, 600);

        for (int i = 0; i < clusterColorsData.clustersInvariants.size(); i++) {
            note.addTextLineNL("Cluster#" + i, "text");

            ArrayList<Integer> invariantsIndices = clusterColorsData.clustersInvariants.get(i);
            ArrayList<ArrayList<Integer>> invSubMatrix = new ArrayList<>();
            for (int j : invariantsIndices) {
                invSubMatrix.add(invMatrix.get(j));
            }

            for (int inv = 0; inv < invSubMatrix.size(); inv++) {
                int invIndex = invariantsIndices.get(inv);
                note.addTextLine("x" + invIndex, "text");

                for (int trans = 0; trans < invSubMatrix.get(inv).size(); trans++) {
                    note.addTextLine(";" + invSubMatrix.get(inv).get(trans), "text");
                }
                note.addTextLineNL("", "text");
            }

        }
        note.addTextLineNL("", "text");
        note.addTextLineNL("", "text");
        note.addTextLineNL("", "text");
        note.addTextLineNL("", "text");
        note.addTextLineNL("", "text");


        for (int i = 0; i < clusterColorsData.clustersInvariants.size(); i++) {
            note.addTextLineNL("Cluster#" + i, "text");

            ArrayList<Integer> invariantsIndices = clusterColorsData.clustersInvariants.get(i);
            ArrayList<ArrayList<Integer>> invSubMatrix = new ArrayList<>();
            for (int j : invariantsIndices) {
                invSubMatrix.add(invMatrix.get(j));
            }

            for (int inv = 0; inv < invSubMatrix.size(); inv++) {
                int invIndex = invariantsIndices.get(inv);
                note.addTextLine("x" + invIndex, "text");

                for (int trans = 0; trans < invSubMatrix.get(inv).size(); trans++) {
                    if (invSubMatrix.get(inv).get(trans) > 0) {
                        note.addTextLine(";t" + trans, "text");
                    }


                }
                note.addTextLineNL("", "text");
            }

        }

        note.setCaretFirstLine();
        note.setVisible(true);

    }

    /**
     * Metoda wypełniająca drugi combox - lista inwariantów wybranego klastra.
     */
    private void fillClustInvCombo() {
        try {
            if (selectedClusterIndex == -1) {
                chooseClusterInv.removeAllItems();
                chooseClusterInv.addItem("---");
                return;
            }
            doNotUpdate = true;

            ArrayList<Integer> clInvariants = clusterColorsData.clustersInvariants.get(selectedClusterIndex);
            chooseClusterInv.removeAllItems();

            String[] clustersInvHeaders = new String[clInvariants.size() + 1];
            clustersInvHeaders[0] = "---";
            for (int i = 0; i < clInvariants.size(); i++) {
                int invIndex = clInvariants.get(i);
                clustersInvHeaders[i + 1] = "Cluster: " + (selectedClusterIndex + 1) + "  |#" + (i + 1) + "  Inv: " + (invIndex + 1);
            }
            chooseClusterInv.setModel(new DefaultComboBoxModel<>(clustersInvHeaders));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        doNotUpdate = false;
    }

    /**
     * Metoda podświetlająca wybrany inwariant w ramach klastra.
     */
    private void showClusterInv() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedClusterIndex == -1)
            return;

        ArrayList<ClusterTransition> transColors = clusterColorsData.dataMatrix.get(selectedClusterIndex);
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        ArrayList<Integer> clInvariants = clusterColorsData.clustersInvariants.get(selectedClusterIndex);
        int invIndex = clInvariants.get(selectedClusterInvIndex);
        ArrayList<Integer> invariant = overlord.getWorkspace().getProject().getT_InvMatrix().get(invIndex);

        for (int i = 0; i < transColors.size(); i++) {
            if (transColors.get(i).transInCluster != 0) {   //equals(Color.white)) {
                transitions.get(i).setColorWithNumber(true, Color.DARK_GRAY, false, -1, false, "");
            }
        }

        for (int i = 0; i < invariant.size(); i++) {
            if (invariant.get(i) != 0) {

                transitions.get(i).setColorWithNumber(true, Color.GREEN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    /**
     * Metoda odpowiedzialna za zapisanie na dysky obrazów klastrów.
     */
    private void dropClustersToFiles() {
        //chose folder
        String lastPath = overlord.getLastPath();
        String dirPath = Tools.selectDirectoryDialog(lastPath, "Select dir",
                "Select directory for clusters screenshots.");
        if (dirPath.equals("")) { // czy wskazano cokolwiek
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

            progressBar.setMaximum(clusters - 1);
            progressBar.setValue(0);
            progressBar.setVisible(true);

            for (int c = 0; c < clusters; c++) {
                progressBar.setValue(c);
                progressBar.update(progressBar.getGraphics());
                int number = c + 1;
                String clusterName = "cluster_" + number + "_(invariants_" + clusterColorsData.clSize.get(c) + ")";
                selectedClusterIndex = c;
                clustersMCT = false;
                showClusters();
                String fileName = "" + dirPath + "//" + clusterName + ".png";
                BufferedImage image = main.createImageFromSheet();
                ImageIO.write(image, "png", new File(fileName));

                clustersMCT = true;
                showClusters();
                fileName = "" + dirPath + "//" + clusterName + "_MCTview.png";
                BufferedImage image2 = main.createImageFromSheet();
                ImageIO.write(image2, "png", new File(fileName));
            }
        } catch (Exception e) {
            overlord.log("Saving clusters screenshots failed.", "error", true);
            progressBar.setVisible(false);
        } finally {
            selectedClusterIndex = oldSelected;
            main.setZoom(100, main.getZoom());
        }
        progressBar.setVisible(false);
        JOptionPane.showMessageDialog(null, "Saving " + clusters + " clusters to graphic files completed.", "Success", JOptionPane.INFORMATION_MESSAGE);

    }

    /**
     * Metoda pokazująca dane o klastrach w notatniku.
     */
    private void showClustersNotepad() {
        if (selectedClusterIndex == -1)
            return;

        HolmesNotepad note = new HolmesNotepad(640, 480);

        note.addTextLineNL("", "text");
        note.addTextLineNL("Cluster: " + (selectedClusterIndex + 1) + " (" + clusterColorsData.clSize.get(selectedClusterIndex) + " inv.) alg.: " + clusterColorsData.algorithm
                + " metric: " + clusterColorsData.metric, "text");
        note.addTextLineNL("", "text");

        ArrayList<ClusterTransition> transColors = clusterColorsData.dataMatrix.get(selectedClusterIndex);

        ArrayList<Transition> holyVector = overlord.getWorkspace().getProject().getTransitions();
        for (int i = 0; i < transColors.size(); i++) { //ustaw kolory dla tranzycji
            int trInCluster = transColors.get(i).transInCluster;
            double firedInCluster = transColors.get(i).firedInCluster; // ????
            if (trInCluster > 0) {
                String t1 = Tools.setToSize("t" + (i), 5, false);
                String t2 = Tools.setToSize("Freq.: " + trInCluster, 12, false);
                String t3 = Tools.setToSize("Fired: " + formatD(firedInCluster), 15, false);
                String txt = t1 + t2 + t3 + " ; " + holyVector.get(i).getName();
                note.addTextLineNL(txt, "text");
            }
        }
        note.setCaretFirstLine();
        note.setVisible(true);
    }

    /**
     * Metoda pokazująca dane o klastrze na ekranie sieci oraz w podoknie programu.
     */
    private void showClusters() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedClusterIndex == -1) {
            mssValueLabel.setText("n/a");
            return;
        }

        ArrayList<ClusterTransition> transColors = clusterColorsData.dataMatrix.get(selectedClusterIndex);
        ArrayList<Transition> holyVector = overlord.getWorkspace().getProject().getTransitions();
        ColorPalette cp = new ColorPalette();
        ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();

        float mss = clusterColorsData.clMSS.get(selectedClusterIndex);
        mssValueLabel.setText("" + mss);

        for (int i = 0; i < transColors.size(); i++) { //ustaw kolory dla tranzycji
            if (transColors.get(i).transInCluster == 0) {   //equals(Color.white)) {
                holyVector.get(i).setColorWithNumber(false, Color.white, false, -1, false, "");
            } else {
                if (clustersMCT) {
                    int mctNo = transMCTvector.get(i);
                    if (mctNo == -1) {
                        if (clusterColorsData.showFirings) { //pokazuj średnią liczbę odpaleń
                            if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
                                holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).firedInCluster, false, "", 0, 20, 5, -3);
                            } else { //pokazuj kolory z krokiem 10%
                                holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).firedInCluster, false, "", 0, 20, 5, -3);
                            }
                        } else { //pokazuj tylko liczbę wystąpień jako część inwariantów
                            if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
                                holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
                            } else { //pokazuj kolory z krokiem 10%
                                holyVector.get(i).setColorWithNumber(true, Color.CYAN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
                            }
                        }
                    } else {
                        double value;
                        if (clusterColorsData.showFirings) {
                            value = transColors.get(i).firedInCluster;
                        } else {
                            value = transColors.get(i).transInCluster;
                        }
                        holyVector.get(i).setColorWithNumber(true, cp.getColor(mctNo), true, value, true, "[MCT" + (mctNo + 1) + "]", -10, 15, 5, -3);
                    }
                } else {
                    if (clusterColorsData.showFirings) { //pokazuj średnią liczbę odpaleń
                        if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
                            double tranNumber = transColors.get(i).firedInCluster;
                            Color tranColor = transColors.get(i).colorFiredScale;
                            holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
                        } else { //pokazuj kolory z krokiem 10%
                            double tranNumber = transColors.get(i).firedInCluster;
                            Color tranColor = transColors.get(i).colorFiredGrade;
                            holyVector.get(i).setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
                        }
                    } else { //pokazuj tylko liczbę wystąpień jako część inwariantów
                        if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
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
     *
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
     *
     * @param mcsData MCSDataMatrix - obiekt danych zbiorów MCS
     */
    private void createMCSSubWindow(MCSDataMatrix mcsData) {
        transitions = overlord.getWorkspace().getProject().getTransitions();

        initiateContainers();

        int posX = 10;
        int posY = 10;

        JLabel objRLabel = new JLabel("Reaction: ");
        objRLabel.setBounds(posX, posY, 80, 20);
        components.add(objRLabel);

        String[] objRset = new String[transitions.size() + 1];
        objRset[0] = "---";
        for (int i = 0; i < transitions.size(); i++) {
            objRset[i + 1] = "t" + i + transitions.get(i).getName();
        }


        //WYBÓR REAKCJI ZE ZBIORAMI MCS
        mcsObjRCombo = new JComboBox<>(objRset);
        mcsObjRCombo.setBounds(posX + 60, posY, 230, 20);
        mcsObjRCombo.addActionListener(actionEvent -> {
            if (stopAction)
                return;
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected > 0) {
                selected--;
                MCSDataMatrix mcsDataCore = overlord.getWorkspace().getProject().getMCSdataCore();
                ArrayList<ArrayList<Integer>> sets = mcsDataCore.getMCSlist(selected--);

                if (sets == null)
                    return;

                stopAction = true;
                mcsMCSforObjRCombo.removeAllItems();
                mcsMCSforObjRCombo.addItem("---");

                String newRow;
                for (ArrayList<Integer> set : sets) {
                    StringBuilder newRowBuilder = new StringBuilder("[");
                    for (int el : set) {
                        newRowBuilder.append(el).append(", ");
                    }
                    newRow = newRowBuilder.toString();
                    newRow += "]";
                    newRow = newRow.replace(", ]", "]");
                    mcsMCSforObjRCombo.addItem(newRow);
                }
                stopAction = false;
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
        mcsMCSforObjRCombo = new JComboBox<>(init);
        mcsMCSforObjRCombo.setBounds(posX + 60, posY, 160, 20);
        mcsMCSforObjRCombo.addActionListener(actionEvent -> {
            if (stopAction)
                return;

            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected > 0) {
                selected--;
                //MCSDataMatrix mcsDataCore = overlord.getWorkspace().getProject().getMCSdataCore();
                int selTrans = mcsObjRCombo.getSelectedIndex();
                selTrans--;
                showMCSDataInNet(Objects.requireNonNull(comboBox.getSelectedItem()).toString(), selTrans);
            }
        });
        components.add(mcsMCSforObjRCombo);

        JButton refreshButton = new JButton();
        refreshButton.setText("Refresh");
        refreshButton.setBounds(posX + 225, posY, 70, 20);
        refreshButton.addActionListener(actionEvent -> {
            transitions = overlord.getWorkspace().getProject().getTransitions();
            if (transitions.size() == 0)
                return;

            String[] objRset1 = new String[transitions.size() + 1];
            objRset1[0] = "---";
            for (int i = 0; i < transitions.size(); i++) {
                objRset1[i + 1] = "t" + i + "_" + transitions.get(i).getName();
            }
            stopAction = true;

            mcsObjRCombo.removeAllItems();
            for (String str : objRset1) {
                mcsObjRCombo.addItem(str);
            }
            stopAction = false;
        });
        refreshButton.setFocusPainted(false);
        panel.add(refreshButton);
        posY += 20;

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    /**
     * Metoda pokazuje w kolorach tranzycje wchodzące w skład MCS oraz tramzycję bazową zbioru MCS.
     *
     * @param sets          String - zbiór w formie łańcucha znaków [x, y, z, ...]
     * @param objReactionID int - nr tranzycji bazowe
     */
    private void showMCSDataInNet(String sets, int objReactionID) {
        try {
            PetriNet pn = overlord.getWorkspace().getProject();
            pn.resetNetColors();

            sets = sets.replace("[", "");
            sets = sets.replace("]", "");
            sets = sets.replace(" ", "");

            String[] elements = sets.split(",");
            ArrayList<Integer> invIDs = new ArrayList<>();

            for (String el : elements) {
                invIDs.add(Integer.parseInt(el));
            }

            Transition trans_TMP = overlord.getWorkspace().getProject().getTransitions().get(objReactionID);
            trans_TMP.setColorWithNumber(true, Color.red, false, -1, false, "");

            for (int id : invIDs) {
                trans_TMP = overlord.getWorkspace().getProject().getTransitions().get(id);
                trans_TMP.setColorWithNumber(true, Color.black, false, -1, false, "");
                //double tranNumber = transColors.get(i).firedInCluster;
                //Color tranColor = transColors.get(i).colorFiredScale;
                //holyVector.get(i).setColorWithNumber(true, tranColor, tranNumber);
            }

            overlord.getWorkspace().getProject().repaintAllGraphPanels();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************     KNOCKOUT     ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * @param knockoutData ArrayList[ArrayList[Integer]] - macierz danych o knockout
     */
    private void createKnockoutData(ArrayList<ArrayList<Integer>> knockoutData) {
        if (knockoutData == null || knockoutData.isEmpty()) {
            return;
        } else {
            mode = KNOCKOUT;
            this.knockoutData = knockoutData;
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

        ArrayList<String> mctOrNot = new ArrayList<>();
        ArrayList<Integer> mctSize = new ArrayList<>();
        for (int i = 0; i < transSize; i++) {
            mctOrNot.add("");
            mctSize.add(0);
        }
        int mctNo = 0;
        for (ArrayList<Transition> arr : mct) {
            mctNo++;
            for (Transition t : arr) {
                int id = transitions.indexOf(t);
                mctOrNot.set(id, "MCT_" + mctNo);
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

        JComboBox<String> chooseMctBox = new JComboBox<>(headers);
        chooseMctBox.setBounds(colB_posX, positionY, 150, 20);
        chooseMctBox.addActionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                showKnockout(-1, false);
            } else {
                selected--;
                showKnockout(selected, true);
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
        textAreaPanel.setBounds(colA_posX, positionY, w - 30, h - 60);
        components.add(textAreaPanel);

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    /**
     * Metoda odpowiedzialna za pokazanie szczegółów wybranego zbioru MCT.
     *
     * @param knockIndex  Integer - numer wybranego zbioru
     * @param showOrClear boolean - true, jeśli wybrano zbiór mct, false jeśli "---"
     */
    private void showKnockout(Integer knockIndex, boolean showOrClear) {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (showOrClear) {
            ArrayList<Integer> idToShow = knockoutData.get(knockIndex);
            Transition trans_TMP;
            ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();

            for (int id : idToShow) { //wyłączane przez objR
                trans_TMP = transitions.get(id);
                trans_TMP.setColorWithNumber(true, Color.black, false, -1, false, "");
            }

            trans_TMP = transitions.get(knockIndex);
            trans_TMP.setColorWithNumber(true, Color.red, false, -1, false, "");

            knockoutTextArea.setText("");
            knockoutTextArea.append("Knocked out:" + knockIndex + ":\n");
            knockoutTextArea.append("\n");
            knockoutTextArea.append(" * * * Also knocked out: \n");

            for (int t_id : idToShow) {
                String t1 = Tools.setToSize("t" + t_id, 5, false);
                knockoutTextArea.append(t1 + " ; " + transitions.get(t_id).getName() + "\n");
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
    private void createFixerSubWindow() {
        int posX = 10;
        int posY = 10;

        initiateContainers();
        detector = new ProblemDetector(this);

        JLabel label0 = new JLabel("t-invariants:");
        label0.setBounds(posX, posY, 100, 20);
        components.add(label0);

        fixInvariants = new JLabel("Normal: 0 / Non-inv.: 0");
        fixInvariants.setBounds(posX, posY += 20, 190, 20);
        components.add(fixInvariants);

        fixInvariants2 = new JLabel("Sub-inv.: 0 / Sur-inv: 0");
        fixInvariants2.setBounds(posX, posY += 20, 190, 20);
        components.add(fixInvariants2);

        JButton markInvButton = new JButton();
        markInvButton.setText("<html>Show<br>&nbsp;&nbsp;&nbsp;inv.</html>");
        markInvButton.setBounds(posX + 185, posY - 18, 100, 32);
        markInvButton.setMargin(new Insets(0, 0, 0, 0));
        markInvButton.setIcon(Tools.getResIcon22("/icons/fixGlass.png"));
        markInvButton.addActionListener(actionEvent -> detector.markSubSurNonInvariantsPlaces());
        markInvButton.setFocusPainted(false);
        components.add(markInvButton);

        JLabel label1 = new JLabel("Input and output places:");
        label1.setBounds(posX, posY += 25, 200, 20);
        components.add(label1);

        fixIOPlaces = new JLabel("Input: 0 / Output: 0");
        fixIOPlaces.setBounds(posX, posY += 20, 190, 20);
        components.add(fixIOPlaces);

        JButton markIOPlacesButton = new JButton();
        markIOPlacesButton.setText("<html>Show<br>places</html>");
        markIOPlacesButton.setBounds(posX + 185, posY - 16, 100, 32);
        markIOPlacesButton.setMargin(new Insets(0, 0, 0, 0));
        markIOPlacesButton.setIcon(Tools.getResIcon22("/icons/fixGlass.png"));
        markIOPlacesButton.addActionListener(actionEvent -> detector.markIOPlaces());
        markIOPlacesButton.setFocusPainted(false);
        components.add(markIOPlacesButton);

        JLabel label2 = new JLabel("Input and output transitions:");
        label2.setBounds(posX, posY += 25, 200, 20);
        components.add(label2);

        fixIOTransitions = new JLabel("Input: 0 / Output: 0");
        fixIOTransitions.setBounds(posX, posY += 20, 190, 20);
        components.add(fixIOTransitions);

        JButton markIOTransButton = new JButton();
        markIOTransButton.setText("<html>Show<br>trans.</html>");
        markIOTransButton.setBounds(posX + 185, posY - 14, 100, 32);
        markIOTransButton.setMargin(new Insets(0, 0, 0, 0));
        markIOTransButton.setIcon(Tools.getResIcon22("/icons/fixGlass.png"));
        markIOTransButton.addActionListener(actionEvent -> detector.markIOTransitions());
        markIOTransButton.setFocusPainted(false);
        components.add(markIOTransButton);

        JLabel label3 = new JLabel("Linear transitions and places");
        label3.setBounds(posX, posY += 25, 200, 20);
        components.add(label3);

        fixlinearTrans = new JLabel("Transitions: 0 / Places: 0");
        fixlinearTrans.setBounds(posX, posY += 20, 190, 20);
        components.add(fixlinearTrans);

        JButton markLinearTPButton = new JButton();
        markLinearTPButton.setText("<html>Show<br>T & P</html>");
        markLinearTPButton.setBounds(posX + 185, posY - 12, 100, 32);
        markLinearTPButton.setMargin(new Insets(0, 0, 0, 0));
        markLinearTPButton.setIcon(Tools.getResIcon22("/icons/fixGlass.png"));
        markLinearTPButton.addActionListener(actionEvent -> detector.markLinearRegions());
        markLinearTPButton.setFocusPainted(false);
        components.add(markLinearTPButton);

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************     quickSim     ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Tworzy okno wykrywania i wskazywania problemów sieci.
     */
    private void createQuickSimSubWindow() {
        int posX = 10;
        int posY = 10;
        initiateContainers();

        quickSim = new QuickSimTools(this);

        JButton acqDataButton = new JButton("SimStart");
        acqDataButton.setBounds(posX, posY, 110, 40);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setFocusPainted(false);
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText("Compute steps from zero marking through the number of states");
        acqDataButton.addActionListener(actionEvent -> quickSim.acquireData(scanTransitions, scanPlaces, markArcs, repetitions, quickProgressBar));
        components.add(acqDataButton);

        JButton simSettingsButton = new JButton("SimSettings");
        simSettingsButton.setBounds(posX + 120, posY, 130, 40);
        simSettingsButton.setMargin(new Insets(0, 0, 0, 0));
        simSettingsButton.setFocusPainted(false);
        simSettingsButton.setIcon(Tools.getResIcon32("/icons/simSettings/setupIcon.png"));
        simSettingsButton.setToolTipText("Set simulator options.");
        simSettingsButton.addActionListener(actionEvent -> new HolmesSimSetup(GUIManager.getDefaultGUIManager().getFrame()));
        components.add(simSettingsButton);

        quickProgressBar = new JProgressBar();
        quickProgressBar.setBounds(posX, posY += 45, 280, 25);
        quickProgressBar.setMaximum(100);
        quickProgressBar.setMinimum(0);
        quickProgressBar.setValue(0);
        quickProgressBar.setStringPainted(true);
        //Border border = BorderFactory.createTitledBorder("Progress");
        //quickProgressBar.setBorder(border);
        components.add(quickProgressBar);

        JPanel borderPanel = new JPanel(null);
        //borderPanel.setLayout(new BorderLayout());
        borderPanel.setBounds(posX, posY += 30, 280, 80);
        borderPanel.setBorder(BorderFactory.createTitledBorder("Data type simulated"));
        components.add(borderPanel);

        JCheckBox transBox = new JCheckBox("Transitions firing data", scanTransitions);
        transBox.setBounds(5, 15, 160, 20);
        transBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            scanTransitions = box.isSelected();
        });
        borderPanel.add(transBox);


        JCheckBox placesBox = new JCheckBox("Places tokens data", scanPlaces);
        placesBox.setBounds(5, 35, 160, 20);
        placesBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            scanPlaces = box.isSelected();
        });
        borderPanel.add(placesBox);

        JCheckBox arcsBox = new JCheckBox("Color arcs", markArcs);
        arcsBox.setBounds(5, 55, 140, 20);
        arcsBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            markArcs = box.isSelected();
        });
        borderPanel.add(arcsBox);

        JCheckBox repsBox = new JCheckBox("Repetitions", repetitions);
        repsBox.setBounds(165, 15, 100, 20);
        repsBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            repetitions = box.isSelected();
        });
        borderPanel.add(repsBox);


        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************   Decomposition  ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Tworzy okno z opcjami do dekompozycji
     */

    private void createDecompositionData() {


        int posX = 10;
        int posY = 10;

        /*
        String[] newComoList = new String[SubnetCalculator.functionalSubNets.size()];
        for (int i = 0; i < SubnetCalculator.functionalSubNets.size(); i++) {
            newComoList[i] = "Functional subnet " + overlordi;
        }
        */

        if (!SubnetCalculator.functionalSubNets.isEmpty()) {
            mode = DECOMPOSITION;
            SubnetCalculator.cleanSubnets();
        }

        SubnetCalculator.cleanSubnets();

        initiateContainers();

        JLabel chooseDecoLabel = new JLabel("Choose Decomposition: ");
        chooseDecoLabel.setBounds(posX, posY, 150, 20);
        components.add(chooseDecoLabel);

        String[] decoList = {"Functional", "S-nets", "T-nets", "maxADT", "Teng-Zhang", "Hou", "Nishi", "Cycle", "Ootsuki"};

        JComboBox<String> chooseMctBox = new JComboBox<>(decoList);
        chooseMctBox.setBounds(posX, posY + 30, 150, 20);
        chooseMctBox.addActionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            choosenDeco = comboBox.getSelectedIndex();
        });
        components.add(chooseMctBox);

        JButton calculateDeco = new JButton("Calculate");
        calculateDeco.setBounds(posX + 200, posY + 30, 100, 50);
        calculateDeco.addActionListener(actionEvent -> calculateDeco(choosenDeco));
        components.add(calculateDeco);

        JComboBox<String> subnetList = new JComboBox<>();
        subnetList.setBounds(posX, posY + 60, 150, 20);
        subnetList.addActionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                selectedMCTindex = -1;
                allMCTselected = false;
                showSubNet(0);
                MCTnameField.setText("");
            } else if (selected == comboBox.getItemCount() - 1) {
                allMCTselected = true;
                showAllColors();
            } else {
                selectedMCTindex = selected - 1;
                allMCTselected = false;
                showSubNet(0);
            }
        });
        components.add(subnetList);

        //TODO uruchomić
        elementsOfDecomposedStructure = new JTextArea();
        elementsOfDecomposedStructure.setLineWrap(true);
        elementsOfDecomposedStructure.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                if (field != null)
                    changeSubNetname(field.getText());
            }
        });

        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BorderLayout());
        descPanel.add(new JScrollPane(MCTnameField), BorderLayout.CENTER);
        descPanel.setBounds(posX, posY + 100, 250, 80);
        components.add(descPanel);


        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
    }

    private void changeSubNetname(String newName) {
        if (selectedSubNetindex == -1)
            return;

        overlord.getWorkspace().getProject().accessMCTnames().set(selectedSubNetindex, newName);
    }

    private void calculateDeco(int index) {
        getSubnetOfType(index);
    }

    private void getSubnetOfType(int index) {
        generateProperSubNet(index);
        /*
        int size = getSubnetSize(index);

        String[] newComoList = new String[size + 3];
        for (int i = 0; i < size; i++) {
            newComoList[i + 1] = getProperSubNetName(index) + i;
        }
        newComoList[0] = "--";
        newComoList[size + 1] = "All non trivial subnets";
        newComoList[size + 2] = "All subnets";
*/
        int listIndex = components.stream().map(Component::getLocation).collect(Collectors.toList()).indexOf(new Point(10, 70));

        JComboBox<String> newCB = generateButton(index);
        newCB.setBounds(10, 70, 150, 20);
        newCB.addActionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                selectedSubNetindex = -1;
                allSubNetsselected = false;
                showSubNet(index);
                elementsOfDecomposedStructure.setText("");
            } else if (selected == comboBox.getItemCount() - 1) {
                allSubNetsselected = true;
                showAllSubColors(true, index);
            } else if (selected == comboBox.getItemCount() - 2) {
                allSubNetsselected = true;
                showAllSubColors(false, index);
            } else {
                selectedSubNetindex = selected - 1;
                allSubNetsselected = false;
                showSubNet(index);
            }
        });
        newCB.setVisible(true);
        this.components.set(listIndex, newCB);
        this.panel.removeAll();

        for (JComponent component : this.components) this.panel.add(component);

        this.panel.repaint();
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    private JComboBox<String> generateButton(int index) {
        if (index == 0 || index == 1 || index == 2 || index == 3)
            return generateProperDecoButton(index);
        if (index == 4 || index == 5 || index == 6 || index == 7)
            return generateInProperDecoButton(index);

        return generateProperDecoButton(index);
    }

    private JComboBox<String> generateProperDecoButton(int index) {
        int size = getSubnetSize(index);
        String[] newComoList = new String[size + 3];
        for (int i = 0; i < size; i++) {
            newComoList[i + 1] = getProperSubNetName(index) + i;
        }
        newComoList[0] = "--";
        newComoList[size + 1] = "All non trivial subnets";
        newComoList[size + 2] = "All subnets";
        JComboBox<String> newCB = new JComboBox<>(newComoList);
        newCB.setBounds(10, 70, 150, 20);
        newCB.addActionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                selectedSubNetindex = -1;
                allSubNetsselected = false;
                showSubNet(index);
                elementsOfDecomposedStructure.setText("");
            } else if (selected == comboBox.getItemCount() - 1) {
                allSubNetsselected = true;
                showAllSubColors(true, index);
            } else if (selected == comboBox.getItemCount() - 2) {
                allSubNetsselected = true;
                showAllSubColors(false, index);
            } else {
                selectedSubNetindex = selected - 1;
                allSubNetsselected = false;
                showSubNet(index);
            }
        });
        newCB.setVisible(true);
        return newCB;
    }

    private JComboBox<String> generateInProperDecoButton(int index) {
        int size = getSubnetSize(index);
        String[] newComoList = new String[size + 2];
        for (int i = 0; i < size; i++) {
            newComoList[i + 1] = getProperSubNetName(index) + i;
        }
        newComoList[0] = "--";
        newComoList[size + 1] = "Net coverage";
        JComboBox<String> newCB = new JComboBox<>(newComoList);
        newCB.setBounds(10, 70, 150, 20);
        newCB.addActionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                selectedSubNetindex = -1;
                allSubNetsselected = false;
                showSubNet(index);
                elementsOfDecomposedStructure.setText("");
            } else if (selected == comboBox.getItemCount() - 1) {
                allSubNetsselected = true;
                showCoverageColors(index);
            } else {
                selectedSubNetindex = selected - 1;
                allSubNetsselected = false;
                showSubNet(index);
            }
        });
        newCB.setVisible(true);
        return newCB;
    }

    private String getProperSubNetName(int index) {
        switch (index) {
            case 0:
                return "Functional ";
            case 1:
                return "S-net ";
            case 2:
                return "T-net ";
            case 3:
                return "maxADT ";
            case 4:
                return "Teng-Zeng ";
            case 5:
                return "Hou ";
            case 6:
                return "Nishi ";
            case 7:
                return "Cycle";
            case 8:
                return "Ootsuki";
        }
        return "";
    }

    private void generateProperSubNet(int index) {
        SubnetCalculator.compileElements();
        switch (index) {
            case 0:
                SubnetCalculator.generateFS();
                break;
            case 1:
                SubnetCalculator.generateSnets();
                break;
            case 2:
                SubnetCalculator.generateTnets();
                break;
            case 3:
                SubnetCalculator.generateADT();
                break;
            case 4:
                SubnetCalculator.generateTZ();
                break;
            case 5:
                SubnetCalculator.generateHou();
                break;
            case 6:
                SubnetCalculator.generateNishi();
                break;
            case 7:
                SubnetCalculator.generateCycle(false);
                break;
            case 8:
                SubnetCalculator.generateOotsuki();
                break;
        }
    }

    private int getSubnetSize(int index) {
        switch (index) {
            case 0:
                return SubnetCalculator.functionalSubNets.size();
            case 1:
                return SubnetCalculator.snetSubNets.size();
            case 2:
                return SubnetCalculator.tnetSubNets.size();
            case 3:
                return SubnetCalculator.adtSubNets.size();
            case 4:
                return SubnetCalculator.tzSubNets.size();
            case 5:
                return SubnetCalculator.houSubNets.size();
            case 6:
                return SubnetCalculator.nishiSubNets.size();
            case 7:
                return SubnetCalculator.cycleSubNets.size();
            case 8:
                return SubnetCalculator.ootsukiSubNets.size();
        }
        return 0;
    }

    private void showSubNet(int typeOfDecomposition) {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedSubNetindex == -1)
            return;
        SubnetCalculator.SubNet subnet = null;
        int size = 0;
        switch (typeOfDecomposition) {
            case 0:
                subnet = SubnetCalculator.functionalSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.functionalSubNets.size();
                break;
            case 1:
                subnet = SubnetCalculator.snetSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.snetSubNets.size();
                break;
            case 2:
                subnet = SubnetCalculator.tnetSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.tnetSubNets.size();
                break;
            case 3:
                subnet = SubnetCalculator.adtSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.adtSubNets.size();
                break;
            case 4:
                subnet = SubnetCalculator.tzSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.tzSubNets.size();
                break;
            case 5:
                subnet = SubnetCalculator.houSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.houSubNets.size();
                break;
            case 6:
                subnet = SubnetCalculator.nishiSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.nishiSubNets.size();
                break;
            case 7:
                subnet = SubnetCalculator.cycleSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.cycleSubNets.size();
                break;
            case 8:
                subnet = SubnetCalculator.ootsukiSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.ootsukiSubNets.size();
                break;
        }

        ColorPalette cp = new ColorPalette();

        //places
        for (Place place : subnet.getSubPlaces()) {
            if (!colorSubNet) {
                place.setGlowedSub(true);

            } else {
                if (selectedSubNetindex == size - 1)
                    place.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[trivial]");
                else
                    place.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
        }

        //arcs
        for (Arc arc : subnet.getSubArcs()) {
            if (!colorSubNet)
                arc.setGlowedSub(true);
        }

        //transitions
        for (Transition transition : subnet.getSubTransitions()) {
            if (!colorSubNet) {
                transition.setGlowed_MTC(true);

            } else {
                if (selectedSubNetindex == size - 1)
                    transition.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[trivial]");
                else
                    transition.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();

        //name field:
        if (overlord.getWorkspace().getProject().accessSubNetNames() != null) {
            String name = overlord.getWorkspace().getProject().accessSubNetNames().get(selectedSubNetindex);
            elementsOfDecomposedStructure.setText(name);
        }
    }

    ////

    private void showCoverageColors(int subnetType) {
        ArrayList<SubnetCalculator.SubNet> subnets = getCorrectSubnet(subnetType);
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();


        for (int m = 0; m < subnets.size(); m++) {

            SubnetCalculator.SubNet subNet = subnets.get(m);
            ArrayList<Node> transitions = subNet.getSubNode();

            for (Node transition : transitions) {
                if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                    ((Transition) transition).setColorWithNumber(true, Color.red, false, m, true, "");
                if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                    ((Place) transition).setColorWithNumber(true, Color.red, false, m, true, "");
            }
            ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
            for (Arc arc : arcs) {
                arc.setColor(true, Color.red);
            }
        }

        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    ////

    private void showAllSubColors(boolean trivial, int subnetType) {
        ArrayList<SubnetCalculator.SubNet> subnets = getCorrectSubnet(subnetType);
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        ColorPalette cp = new ColorPalette();

        if (subnetType == 0) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor = cp.getColor();
                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Transition> transitions = subNet.getSubTransitions();
                if (transitions.size() > 1 || trivial) {
                    for (Transition transition : transitions) {
                        transition.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.setColorWithNumber(true, calcMiddleColor(currentColor, place.getPlaceNewColor()), false, m, true, "");
                        else
                            place.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.setColor(true, currentColor);
                    }
                }
            }
        }
        if (subnetType == 1) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor = cp.getColor();
                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Place> places = subNet.getSubPlaces();
                if (places.size() > 1 || trivial) {
                    for (Place place : places) {
                        place.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + places.size() + ")");
                    }

                    ArrayList<Transition> transitions = subnets.get(m).getSubTransitions();
                    for (Transition transition : transitions) {
                        if (subNet.getSubBorderTransition().contains(transition))
                            transition.setColorWithNumber(true, calcMiddleColor(currentColor, transition.getTransitionNewColor()), false, m, true, "");
                        else
                            transition.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.setColor(true, currentColor);
                    }
                }
            }
        }
        if (subnetType == 2) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor = cp.getColor();
                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Transition> transitions = subNet.getSubTransitions();
                if (transitions.size() > 1 || trivial) {
                    for (Transition transition : transitions) {
                        transition.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.setColorWithNumber(true, calcMiddleColor(currentColor, place.getPlaceNewColor()), false, m, true, "");
                        else
                            place.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.setColor(true, currentColor);
                    }
                }
            }
        }

        if (subnetType == 3) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor;
                /*if(subnets.get(m).isProper()) {
                 */
                currentColor = cp.getColor();
                  /*  if(currentColor == Color.red)
                        currentColor = cp.getColor();
                }
                else{*/
                //  currentColor=Color.red;
                //}

                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Transition> transitions = subNet.getSubTransitions();
                if (transitions.size() > 1 || trivial) {
                    for (Transition transition : transitions) {
                        transition.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.setColorWithNumber(true, calcMiddleColor(currentColor, place.getPlaceNewColor()), false, m, true, "");
                        else
                            place.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.setColor(true, currentColor);
                    }
                }

            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    private ArrayList<SubnetCalculator.SubNet> getCorrectSubnet(int type) {
        switch (type) {
            case 0:
                return SubnetCalculator.functionalSubNets;
            case 1:
                return SubnetCalculator.snetSubNets;
            case 2:
                return SubnetCalculator.tnetSubNets;
            case 3:
                return SubnetCalculator.adtSubNets;
            case 4:
                return SubnetCalculator.tzSubNets;
            case 5:
                return SubnetCalculator.houSubNets;
            case 6:
                return SubnetCalculator.nishiSubNets;
            case 7:
                return SubnetCalculator.cycleSubNets;
            case 8:
                return SubnetCalculator.ootsukiSubNets;
        }
        return SubnetCalculator.functionalSubNets;
    }

    private Color calcMiddleColor(Color one, Color two) {
        int blue = 0;
        int red = 0;
        int green = 0;
        int absBlue = Math.abs(one.getBlue() - two.getBlue());
        int absRed = Math.abs(one.getRed() - two.getRed());
        int absGreen = Math.abs(one.getGreen() - two.getGreen());

        if (one.getBlue() > two.getBlue())
            blue = one.getBlue() - (absBlue / 2);
        else
            blue = two.getBlue() - (absBlue / 2);

        if (one.getRed() > two.getRed())
            red = one.getRed() - (absRed / 2);
        else
            red = two.getRed() - (absRed / 2);

        if (one.getGreen() > two.getGreen())
            green = one.getGreen() - (absGreen / 2);
        else
            green = two.getGreen() - (absGreen / 2);

        //return new Color(red, green, blue);
        return Color.LIGHT_GRAY;
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
        components = new ArrayList<>();
        panel = new JPanel();
    }

    /**
     * Metoda zwraca okno tekstowe na bazie podanego comboBox.
     *
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
     *
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
     *
     * @param height int - nowa wysokość
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
     *
     * @param width     int - nowa szerokość
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
     *
     * @param height    int - nowa wysokość
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
     *
     * @param value boolean - true, jeśli autoscroll włączony
     */
    private void setAutoscroll(boolean value) {
        if (mode == SHEET) {
            currentSheet.getGraphPanel().setAutoDragScroll(value);
        }
    }

    /**
     * Metoda ustawia nową wartość czasu EFT dla tranzycji czasowej.
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
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
     *
     * @param y  int - współrzędna Y
     * @param n  Node - wierzchołek sieci
     * @param el ElementLocation - obiekt lokalizacji wierzchołka
     * @return Point - prawidłowe współrzędne
     */
    private Point setNameOffsetY(int y, Node n, ElementLocation el) {
        int nameLocIndex = n.getElementLocations().indexOf(el);
        int oldX = n.getNamesLocations().get(nameLocIndex).getPosition().x;
        int oldY = y;
        int newx = oldX + el.getPosition().x;
        int newy = oldY + el.getPosition().y;

        int sheetIndex = overlord.IDtoIndex(el.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();

        if (graphPanel.isLegalLocation(new Point(newx, newy))) {
            n.getNamesLocations().get(nameLocIndex).getPosition().setLocation(oldX, oldY);
            graphPanel.repaint();
        }
        return n.getNamesLocations().get(nameLocIndex).getPosition();
    }

    /**
     * Metoda sprawdza, czy dla danego węzła sieci lokalizacja jego nazwy nie wykracza poza ramy
     * obrazu sieci - dla współrzędnej X
     *
     * @param x  int - współrzędna X
     * @param n  Node - wierzchołek sieci
     * @param el ElementLocation - obiekt lokalizacji wierzchołka
     * @return Point - prawidłowe współrzędne
     */
    private Point setNameOffsetX(int x, Node n, ElementLocation el) {
        int nameLocIndex = n.getElementLocations().indexOf(el);
        int oldX = x;
        int oldY = n.getNamesLocations().get(nameLocIndex).getPosition().y;
        int newx = oldX + el.getPosition().x;
        int newy = oldY + el.getPosition().y;

        int sheetIndex = overlord.IDtoIndex(el.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();

        if (graphPanel.isLegalLocation(new Point(newx, newy))) {
            n.getNamesLocations().get(nameLocIndex).getPosition().setLocation(oldX, oldY);
            graphPanel.repaint();
        }

        return n.getNamesLocations().get(nameLocIndex).getPosition();
    }

    /**
     * Zmiana nazwy elementu sieci, dokonywana poza listenerem, który
     * jest klasa anonimową (i nie widzi pola element).
     *
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
     *
     * @param newComment String - nowy komentarz
     */
    private void changeComment(String newComment) {
        element.setComment(newComment);
    }

    /**
     * Tworzenie portalu z aktualnie wybranego elementu sieci.
     */
    private void makePortal() {
        if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION || mode == CTRANSITION) {
            Node node = (Node) element;
            node.setPortal(true);
        }
    }

    /**
     * Wyłączenie statusu portalu na elemencie posiadającym tylko jedno EL.
     */
    private void unPortal() {
        if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION || mode == CTRANSITION) {
            Node node = (Node) element;
            if (node.getElementLocations().size() == 1)
                node.setPortal(false);
        }
    }

    /**
     * Metoda zmienia liczbę tokenów dla miejsca sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     *
     * @param tokens int - nowa liczba tokenów
     */
    private void setTokens(int tokens) {
        Place place = (Place) element;
        if (mode == PLACE) {
            place.setTokensNumber(tokens);

            if (overlord.getWorkspace().getProject().accessStatesManager().selectedState == 0) {
                ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
                overlord.getWorkspace().getProject().accessStatesManager().getState(0).setTokens(places.indexOf(place), tokens);
            }
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia liczbę tokenów dla miejsca kolorowego sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     *
     * @param tokens int - nowa liczba tokenów
     * @param i      int - nr porządkowy tokenu, default 0, od 0 do 5
     */
    private void setColorTokens(int tokens, int i) {
        Place place = (Place) element;
        if (mode == PLACE && place.isColored) {
            switch (i) {
                case 0:
                    place.setColorTokensNumber(tokens, 0);
                    break;
                case 1:
                    place.setColorTokensNumber(tokens, 1);
                    break;
                case 2:
                    place.setColorTokensNumber(tokens, 2);
                    break;
                case 3:
                    place.setColorTokensNumber(tokens, 3);
                    break;
                case 4:
                    place.setColorTokensNumber(tokens, 4);
                    break;
                case 5:
                    place.setColorTokensNumber(tokens, 5);
                    break;
                default:
                    place.setTokensNumber(tokens);
            }
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia wagę dla łuku sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     *
     * @param weight int - nowa waga
     * @param arc    Arc - łuk
     */
    private void setWeight(int weight, Arc arc) {
        if (mode == ARC) {
            arc.setWeight(weight);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia wagę dla łuku sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     *
     * @param weight int - nowa waga dla koloru
     * @param arc    Arc - obiekt łuku
     * @param i      int - nr porządkowy koloru, default 0, od 0 do 5
     */
    private void setColorWeight(int weight, Arc arc, int i) {
        if (mode == ARC) {
            switch (i) {
                case 0:
                    arc.setColorWeight(weight, i);
                    break;
                case 1:
                    arc.setColorWeight(weight, i);
                    break;
                case 2:
                    arc.setColorWeight(weight, i);
                    break;
                case 3:
                    arc.setColorWeight(weight, i);
                    break;
                case 4:
                    arc.setColorWeight(weight, i);
                    break;
                case 5:
                    arc.setColorWeight(weight, i);
                    break;
                default:
                    arc.setColorWeight(weight, i);
                    break;
            }

            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia liczbę tokenów aktywacji tranzycji, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     *
     * @param weight int - nowa waga aktywacji dla koloru
     * @param trans  Transition - obiekt łuku
     * @param i      int - nr porządkowy koloru, default 0, od 0 do 5
     */
    private void setActivationWeight(int weight, Transition trans, int i) {
        switch (i) {
            case 0:
                trans.setRequiredColoredTokens(weight, 0);
                break;
            case 1:
                trans.setRequiredColoredTokens(weight, 1);
                break;
            case 2:
                trans.setRequiredColoredTokens(weight, 2);
                break;
            case 3:
                trans.setRequiredColoredTokens(weight, 3);
                break;
            case 4:
                trans.setRequiredColoredTokens(weight, 4);
                break;
            case 5:
                trans.setRequiredColoredTokens(weight, 5);
                break;
            default:
                trans.setRequiredColoredTokens(weight, 0);
        }
    }

    /**
     * Metoda ustawia nowy obiekt symulatora sieci.
     *
     * @param netSim NetSimulator - nowy obiekt
     */
    public void setSimulator(NetSimulator netSim) {
        simulator = netSim;
    }

    /**
     * Metoda zwraca obiekt aktywnego symulatora z podokna symulacji.
     *
     * @return NetSimulator - obiekt symulatora
     */
    public NetSimulator getSimulator() {
        return simulator;
    }

    /**
     * Metoda ustawia status wszystkich przycisków rozpoczęcia symulacji za wyjątkiem
     * Pauzy, Stopu - w przypadku startu / stopu symulacji
     *
     * @param enabled boolean - true, jeśli mają być aktywne
     */
    private void setEnabledSimulationInitiateButtons(boolean enabled) {
        for (JComponent comp : components) {
            if (comp instanceof JButton && comp.getName() != null) {
                if (comp.getName().equals("simB1") || comp.getName().equals("simB2")
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
     *
     * @param enabled boolean - true, jeśli mają być aktywne
     */
    private void setEnabledSimulationDisruptButtons(boolean enabled) {
        for (JComponent comp : components) {
            if (comp instanceof JButton && comp.getName() != null) {
                if (comp.getName().equals("stop") || comp.getName().equals("pause")) {
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
        for (JComponent comp : components) {
            if (comp instanceof JButton && comp.getName() != null) {
                if (comp.getName().equals("pause")) {
                    comp.setEnabled(false);
                    break;
                }
            }
        }
    }

    /**
     * Metoda czyści dane o t-inwariantach.
     */
    public void resetT_invariants() {
        t_invariantsMatrix = null;
    }

    /**
     * Metoda czyści dane o p-inwariantach.
     */
    public void resetP_invariants() {
        p_invariantsMatrix = null;
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
