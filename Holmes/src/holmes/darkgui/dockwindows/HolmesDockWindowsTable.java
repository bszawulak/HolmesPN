package holmes.darkgui.dockwindows;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serial;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import holmes.analyse.*;
import holmes.clusters.ClusterDataPackage;
import holmes.clusters.ClusterTransition;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.darkgui.GUIController;
import holmes.graphpanel.EditorResources;
import holmes.graphpanel.GraphPanel;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SPNtransitionData;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.petrinet.simulators.xtpn.GraphicalSimulatorXTPN;
import holmes.petrinet.simulators.QuickSimTools;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;
import holmes.windows.HolmesFunctionsBuilder;
import holmes.windows.HolmesInvariantsViewer;
import holmes.windows.HolmesNotepad;
import holmes.windows.xtpn.HolmesXTPNtokens;
import holmes.windows.managers.HolmesStatesManager;
import holmes.windows.ssim.HolmesSimSetup;
import holmes.workspace.WorkspaceSheet;

/**
 * [MR]Klasa zawierająca szczegóły interfejsu podokien programu. Jest to najlepiej napisana klasa w całym programie.
 * Nasi profesorowie inżynierii oprogramowania byliby dumni.
 * Aby wprawić się w odpowiedni nastrój do jej przeglądania, zacznijmy od dowcipu:
 * Jedziesz na koniu. Żyrafa popierdala przed tobą, z tyłu goni cię lew. Co robisz?
 * Obrzygujesz lwa, mówisz sobie, że więcej nie pijesz i schodzisz z karuzeli.
 * Absolute positioning. Of absolute everything here. It's like a 90s website.
 * Nie obchodzi mnie, co o tym myślicie (╯゜Д゜）╯︵ ┻━┻) . Idźcie w layout i nie wracajcie. ┌∩┐(◣_◢)┌∩┐
 */
@SuppressWarnings("FieldCanBeLocal")
public class HolmesDockWindowsTable extends JPanel {
    @Serial
    private static final long serialVersionUID = 4510802239873443705L;
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private ArrayList<JComponent> components;
    private int mode;

    public int simulatorType = 0; //normalne symulatory
    private ArrayList<Transition> transitions; // j.w.
    private ArrayList<Place> places;
    private ArrayList<ArrayList<Integer>> knockoutData;

    // Containers & general use
    private JPanel panel; // główny panel okna
    private boolean stopAction = false;
    public boolean doNotUpdate = false;

    //simulator:
    public ButtonGroup group = new ButtonGroup();
    public JComboBox<String> simMode;
    private JCheckBox maximumModeCheckBox;
    private JCheckBox singleModeCheckBox;
    private JCheckBox showColorsCheckBox;
    public JLabel timeStepLabelValue;
    public JLabel timeLabelXTPN;
    public JLabel stepLabelXTPN;
    private GraphicalSimulator simulator;  // obiekt symulatora
    private GraphicalSimulatorXTPN simulatorXTPN;

    // P/T/M/A
    private final ButtonGroup groupRadioMetaType = new ButtonGroup();  //metanode
    private boolean nameLocChangeMode = false;
    private PetriNetElement element;
    private ElementLocation elementLocation;
    public SpinnerModel nameLocationXSpinnerModel = null;
    public SpinnerModel nameLocationYSpinnerModel = null;
    private Arc pairedArc = null;
    private JCheckBox timeTransitionCheckBox;
    private JCheckBox classicalTransitionCheckBox;
    private JCheckBox stochasticTransitionCheckBox;

    //MCT:
    private int selectedMCTindex = -1;
    private boolean colorMCT = false;
    private boolean allMCTselected = false;

    JComboBox<String> chooseMctBox = null; //combobox z wyborem MCT
    private JTextArea MCTnameField;

    private ArrayList<ArrayList<Transition>> mctGroups;
    //ArrayList<ArrayList<Transition>> mctGroupsMatrix; //używane w podoknie MCT

    //knockout:
    private JTextArea knockoutTextArea;

    //t-invariants:
    private ArrayList<ArrayList<Integer>> t_invariantsMatrix; //używane w podoknie t-inwariantów
    private JComboBox<String> chooseInvBox = null;
    private JComboBox<String> chooseSurInvBox;
    private JComboBox<String> chooseSubInvBox;
    private JComboBox<String> chooseNoneInvBox;
    private int selectedT_invIndex = -1;
    private boolean markMCT = false;
    private boolean glowT_inv = true;
    private JTextArea t_invNameField;

    //p-invariants:
    private ArrayList<ArrayList<Integer>> p_invariantsMatrix; //używane w podoknie p-inwariantów
    private JComboBox<String> choosePInvBox = null;
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

    private boolean allSubNetsselected = false;

    //kolor:
    private JButton c1Button;
    private JButton c2Button;

    private JLabel projectTypeLabelText; //typ sieci

    //XTPN:
    private HolmesRoundedButton alfaVisibilityButton;//tranzycja
    private HolmesRoundedButton betaVisibilityButton;//tranzycja
    private HolmesRoundedButton tauVisibilityButton;//tranzycja
    private HolmesRoundedButton alphaLocChangeButton;//tranzycja
    private HolmesRoundedButton betaLocChangeButton;//tranzycja
    private HolmesRoundedButton tauLocChangeButton;//tranzycja
    private HolmesRoundedButton buttonClassicMode;//tranzycja

    private HolmesRoundedButton buttonGammaMode; //miejsce
    private HolmesRoundedButton gammaVisibilityButton;//miejsce
    private HolmesRoundedButton gammaLocChangeButton;//miejsce

    private boolean alphaLocChangeMode = false;
    private boolean betaLocChangeMode = false;
    private boolean gammaLocChangeMode = false;
    private boolean tauLocChangeMode = false;

    //aby była możliwa zmiana bez odświeżania:
    private JFormattedTextField alphaMinTextField;
    private JFormattedTextField alphaMaxTextField;
    private JFormattedTextField betaMinTextField;
    private JFormattedTextField betaMaxTextField;

    //przyciski sumulatora XTPN używają poniższych aby globalnie zmieniać widoczność wartości czasowych rysunku sieci:
    private boolean alphaValuesVisible = true;
    private boolean betaValuesVisible = true;
    private boolean gammaValuesVisible = true;
    private boolean tauValuesVisible = true;

    //XTPN qSim:
    JCheckBox qSimXTPNStatsStepsCheckbox;
    JCheckBox qSimXTPNStatsTimeCheckbox;

    boolean qSimXTPNSbySteps = true;
    boolean qSimXTPNrepeateSim = false;
    double qSimXTPNStatsTime = 500.0;
    int qSimXTPNsimStatsSteps = 10000;
    int qSimXTPNStatsRepetitions = 10;
    boolean qSimXTPNknockoutMode = false;

    private JProgressBar qSimXTPNProgressBar = null;
    private HolmesRoundedButton acqDataButtonXTPN = null; //przycisk qSim, wygaszony gdy trwa analiza


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
    private static final int XTPN_TRANS = 14;
    private static final int XTPN_PLACE = 15;
    private static final int XARC = 16;
    private static final int SPN = 99; //rzutem na taśmę

    public enum SubWindow {
        SIMULATOR, PLACE, TRANSITION, TIMETRANSITION, SPNTRANSITION, XTPNTRANSITION, XTPNPLACE, XARC, CTRANSITION, META, ARC, SHEET, T_INVARIANTS, P_INVARIANTS, MCT, CLUSTERS, KNOCKOUT, MCS, FIXER, QUICKSIM, DECOMPOSITION, EMPTY
    }
    
    public JPanel getPanel() {
        return panel;
    }

    public void setPanel(JPanel panel) {
        this.panel = panel;
    }
    
    /**
     * Konstruktor główny, wybierający odpowiednią metodę do tworzenia podokna wybranego typu
     * @param subType  SubWindow - typ podokna do utworzenia
     * @param blackBox Object[...] - bliżej nieokreślona lista nieokreślonych parametrów :)
     */
    @SuppressWarnings("unchecked")
    public HolmesDockWindowsTable(SubWindow subType, Object... blackBox) {
        //System.out.println("HolmesDockWindowsTable constructor called by: "+subType);  //What are you creating
        switch (subType) {
            case SIMULATOR -> createSimulatorSubWindow((GraphicalSimulator) blackBox[0], (GraphicalSimulatorXTPN) blackBox[1]);
            case PLACE -> createPlaceSubWindow((Place) blackBox[0], (ElementLocation) blackBox[1]);
            case TRANSITION -> createTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
            case TIMETRANSITION -> createTimeTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
            case SPNTRANSITION -> createSPNTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
            case XTPNTRANSITION -> createXTPNTransitionSubWindow((TransitionXTPN) blackBox[0], (ElementLocation) blackBox[1]);
            case XTPNPLACE -> createXTPNPlaceSubWindow((PlaceXTPN) blackBox[0], (ElementLocation) blackBox[1]);
            case CTRANSITION -> createColorTransitionSubWindow((Transition) blackBox[0], (ElementLocation) blackBox[1]);
            case META -> createMetaNodeSubWindow((MetaNode) blackBox[0], (ElementLocation) blackBox[1]);
            case ARC, XARC -> createArcSubWindow((Arc) blackBox[0]);
            case SHEET -> createSheetSubWindow((WorkspaceSheet) blackBox[0]);
            case T_INVARIANTS -> createT_invSubWindow((ArrayList<ArrayList<Integer>>) blackBox[0]);
            case P_INVARIANTS -> createP_invSubWindow((ArrayList<ArrayList<Integer>>) blackBox[0]);
            case MCT -> createMCTSubWindow((ArrayList<ArrayList<Transition>>) blackBox[0]);
            case CLUSTERS -> createClustersSubWindow((ClusterDataPackage) blackBox[0]);
            case MCS -> createMCSSubWindow();//(MCSDataMatrix) blackBox[0]);
            case FIXER -> createFixerSubWindow();
            case QUICKSIM -> createQuickSimSubWindow();
            case KNOCKOUT -> createKnockoutData((ArrayList<ArrayList<Integer>>) blackBox[0]);
            case DECOMPOSITION -> createDecompositionData();
            case EMPTY -> createEmpty();
        }
    }

    private void createEmpty() {
        // wiem że tworzy nowe okno w evencie, ale czy dodaje gdzie trzeba? Test z zwenetrznym oknem?
        initiateContainers();
        createSheetSubWindow(overlord.getWorkspace().getSelectedSheet());
        //mode = PLACE;
        //panel=new JPanel();
        add(panel);
        setPanel(panel);
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************    SYMULATOR     ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda pomocnicza konstruktora odpowiedzialna za tworzenie podokna dla symulatora sieci.
     * @param sim (<b>GraphicalSimulator</b>) obiekt symulatora sieci PN.
     * @param simXTPN (<b>GraphicalSimulatorXTPN</b>) obiekt symulatora sieci XTPN.
     */
    //@SuppressWarnings("UnusedAssignment")
    private void createSimulatorSubWindow(GraphicalSimulator sim, GraphicalSimulatorXTPN simXTPN) {
        initiateContainers();
        mode = SIMULATOR;
        setSimulator(sim, simXTPN);
        if (GUIManager.isXTPN_simMode())
            simulatorType = 1; //XTPN
        else
            simulatorType = 0;

        //System.out.println("Window activated: " + GUIManager.isXTPN_simMode());

        repopulateSimulatorPanel();
        panel.setLayout(null);
        
        panel.setOpaque(true);
        panel.repaint();
        panel.setVisible(true);
        add(panel);
        setPanel(panel);
    }

    /**
     * Metoda odpowiedzialna za stworzenie komponentów panelu symulacji standardowej lub XTPN (po prawej
     * stronie głównego okna). Jest wywoływana przez actionListener comboBoxa, gdy zmieniamy rodzaj symulatora.
     */
    private void repopulateSimulatorPanel() {
        int internalX = 5;
        int internalY = 10;

        if (GUIManager.isXTPN_simMode()) { // comboBox dla symulatora XPTN
            JLabel netTypeLabel = new JLabel(lang.getText("HDWT_entry001"));
            netTypeLabel.setBounds(internalX, internalY, 70, 20);
            components.add(netTypeLabel);

            String[] simModeName = {lang.getText("HDWT_entry001op1"), lang.getText("HDWT_entry001op2")};
            simMode = new JComboBox<>(simModeName);
            simMode.setName("XTPNcombo");
            simMode.setLocation(internalX+40, internalY);
            simMode.setSize(120, 20);
            simMode.setSelectedIndex(0);
            simMode.addActionListener(actionEvent -> {
                if (doNotUpdate)
                    return;

                if(simulatorXTPN.getsimulatorStatusXTPN() != GraphicalSimulatorXTPN.SimulatorModeXTPN.STOPPED) {
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_dialog001"),
                            lang.getText("HDWT_dialog001t"), JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int selectedModeIndex = simMode.getSelectedIndex();
                if (selectedModeIndex == 1) { //restore others
                    GUIManager.setXTPN_simMode(false);
                    System.out.println("isXTPNsim? : " + GUIManager.isXTPN_simMode());
                    overlord.getSimulatorBox().createSimulatorProperties(false);
                    components.clear();
                    panel.removeAll();
                    repopulateSimulatorPanel();
                    this.getPanel().validate();
                    this.getPanel().repaint();
                    return;
                } else {
                    GUIManager.setXTPN_simMode(true);
                    //System.out.println("XTPNmode2 " + GUIManager.isXTPN_simMode());
                }
                doNotUpdate = false;
            });
            components.add(simMode);
        } else { //XTPNmode == false
            // SIMULATION MODE
            JLabel netTypeLabel = new JLabel(lang.getText("HDWT_entry001"));
            netTypeLabel.setBounds(internalX, internalY, 70, 20);
            components.add(netTypeLabel);

            String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode", "Color", "XTPN"};
            simMode = new JComboBox<>(simModeName);
            simMode.setName("Classical");
            simMode.setLocation(internalX+50, internalY);
            simMode.setSize(100, 20);
            simMode.setSelectedIndex(0);
            simMode.addActionListener(actionEvent -> {
                if (doNotUpdate)
                    return;

                if (simulator.getSimulatorStatus() != SimulatorMode.STOPPED) {
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_dialog002"),
                            lang.getText("HDWT_dialog002t"), JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int selectedModeIndex = simMode.getSelectedIndex();
                if (selectedModeIndex == 4) { //XTPN
                    //overlord.getSimulatorBox().createSimulatorProperties(true);
                    //doNotUpdate = false;
                    GUIManager.setXTPN_simMode(true);
                    System.out.println("isXTPNsim? : " + GUIManager.isXTPN_simMode());
                    components.clear();
                    panel.removeAll();
                    repopulateSimulatorPanel();
                    return;
                } else {
                    int change = simulator.setGraphicalSimulatorNetType(selectedModeIndex);
                    doNotUpdate = true;
                    c1Button.setEnabled(false);
                    c2Button.setEnabled(false);
                    if (change == 0) {
                        simMode.setSelectedIndex(0);
                    } else if (change == 1) {
                        simMode.setSelectedIndex(1);
                    } else if (change == 2) {
                        simMode.setSelectedIndex(2);
                    } else if (change == 3) {
                        simMode.setSelectedIndex(3);
                        c1Button.setEnabled(true);
                        c2Button.setEnabled(true);
                    } else {
                        overlord.log(lang.getText("LOGentry00041"), "error", true);
                    }
                    GUIManager.setXTPN_simMode(false);
                    System.out.println("isXTPNsim? :  " + GUIManager.isXTPN_simMode());
                    components.clear();
                    panel.removeAll();
                    repopulateSimulatorPanel();
                }
                doNotUpdate = false;
            });
            components.add(simMode);
        }

        if (!GUIManager.isXTPN_simMode()) { //normalny symulator (klasyczny)
            //System.out.println("Standard simulator creation started");
            //i tyle, jakby kto pytał, to są obiekty, można im zmienić tekst, ale się nie wyświetla /NA RAZIE tak ma być:
            stepLabelXTPN = new JLabel("0");
            timeLabelXTPN = new JLabel("0.0");

            internalX = 5;
            internalY = 30;

            JLabel timeStepLabel = new JLabel(lang.getText("HDWT_entry002PNsim"));
            timeStepLabel.setBounds(internalX, internalY, 70, 20);
            components.add(timeStepLabel);

            timeStepLabelValue = new JLabel("0");
            timeStepLabelValue.setBounds(internalX + 70, internalY, 70, 20);
            components.add(timeStepLabelValue);

            internalY += 25;

            JSeparator separator1 = new JSeparator();
            separator1.setOrientation(SwingConstants.HORIZONTAL);
            separator1.setBounds(internalX, internalY, 160, 2);
            components.add(separator1);

            internalY += 5;
            
            // SIMULATOR CONTROLS
            // metoda startSimulation obiektu simulator troszczy się o wygaszanie
            // i aktywowanie odpowiednich przycisków
            JLabel controlsLabel = new JLabel(lang.getText("HDWT_entry003PNsim")); // Simulation controls
            controlsLabel.setBounds(internalX, internalY, 160, 20);
            components.add(controlsLabel);

            internalY += 20;

            JButton loopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/simPN_start2.png"));
            loopSimulation.setName("simPNstart");
            loopSimulation.setBounds(internalX, internalY, 80, 40);
            loopSimulation.setToolTipText(lang.getText("HDWT_entry004PNsim"));
            loopSimulation.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulator.startSimulation(SimulatorMode.LOOP);
                mode = SIMULATOR;
            });
            components.add(loopSimulation);

            JButton singleTransitionLoopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/simPN_startSingle2.png"));
            singleTransitionLoopSimulation.setName("simPNstartSingle");
            singleTransitionLoopSimulation.setBounds(internalX+80, internalY, 80, 40);
            singleTransitionLoopSimulation.setToolTipText(lang.getText("HDWT_entry005PNsim"));
            singleTransitionLoopSimulation.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
                mode = SIMULATOR;
            });
            components.add(singleTransitionLoopSimulation);

            internalY += 40;

            JButton pauseSimulation = new JButton(Tools.getResIcon22("/icons/simulation/simPN_pause2.png"));
            pauseSimulation.setName("simPNpause");
            pauseSimulation.setBounds(internalX, internalY, 80, 40);
            pauseSimulation.setToolTipText(lang.getText("HDWT_entry006PNsim"));
            pauseSimulation.setEnabled(false);
            pauseSimulation.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulator.pause();
                mode = SIMULATOR;
            });
            components.add(pauseSimulation);

            JButton stopSimulation = new JButton(Tools.getResIcon22("/icons/simulation/simPN_stop2.png"));
            stopSimulation.setName("simPNstop");
            stopSimulation.setBounds(internalX+80, internalY, 80, 40);
            stopSimulation.setToolTipText(lang.getText("HDWT_entry007PNsim"));
            stopSimulation.setEnabled(false);
            stopSimulation.addActionListener(actionEvent -> {
                simulator.stop();
                mode = SIMULATOR;
            });
            components.add(stopSimulation);

            internalY += 40;

            JButton resetButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_reset2.png"));
            resetButton.setName("simPNreset");
            resetButton.setBounds(internalX, internalY, 80, 40);
            resetButton.setToolTipText(lang.getText("HDWT_entry008PNsim"));
            resetButton.setEnabled(false);
            resetButton.addActionListener(actionEvent -> overlord.getWorkspace().getProject().restoreMarkingZero());
            components.add(resetButton);

            JButton saveButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_save_m02.png"));
            saveButton.setName("Save m0");
            saveButton.setBounds(internalX+80, internalY, 80, 40);
            saveButton.setToolTipText(lang.getText("HDWT_entry009PNsim"));
            saveButton.addActionListener(actionEvent -> {
                if (overlord.reset.isSimulatorActiveWarning(
                        lang.getText("LOGentry00042"), "Warning"))
                    return;
                if (overlord.reset.isXTPNSimulatorActiveWarning(
                        lang.getText("LOGentry00043"), "Warning"))
                    return;

                Object[] options = {lang.getText("HDWT_entry010op1"), lang.getText("cancel"),};
                int n = JOptionPane.showOptionDialog(null,
                        lang.getText("HDWT_entry010msg1"),
                        lang.getText("HDWT_entry010t"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (n == 0) {
                    overlord.getWorkspace().getProject().accessStatesManager().addCurrentStatePN();
                }
            });
            components.add(saveButton);

            internalY += 40;

            JLabel otherControlsLabel = new JLabel(lang.getText("HDWT_entry011PNsim")); //Other modes:
            otherControlsLabel.setBounds(internalX, internalY, 140, 20);
            components.add(otherControlsLabel);

            internalY += 20;

            JButton oneActionBack = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_back.png"));
            oneActionBack.setName("simPNoneBack");
            oneActionBack.setBounds(internalX, internalY, 80, 30);
            oneActionBack.setToolTipText(lang.getText("HDWT_entry012PNsim"));
            oneActionBack.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulator.startSimulation(SimulatorMode.ACTION_BACK);
                mode = SIMULATOR;
            });
            components.add(oneActionBack);

            JButton oneTransitionForward = new JButton(
                    Tools.getResIcon22("/icons/simulation/simPN_1transForw.png"));
            oneTransitionForward.setName("simPNoneForward");
            oneTransitionForward.setBounds(internalX+80, internalY, 80, 30);
            oneTransitionForward.setToolTipText(lang.getText("HDWT_entry013PNsim"));
            oneTransitionForward.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION);
                mode = SIMULATOR;
            });
            components.add(oneTransitionForward);

            internalY += 30;

            JButton loopBack = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_backLoop.png"));
            loopBack.setName("simB3");
            loopBack.setBounds(internalX, internalY, 80, 30);
            loopBack.setToolTipText(lang.getText("HDWT_entry014PNsim"));
            loopBack.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulator.startSimulation(SimulatorMode.LOOP_BACK);
                mode = SIMULATOR;
            });
            components.add(loopBack);

            JButton oneStepForward = new JButton(
                    Tools.getResIcon22("/icons/simulation/simPN_1stepForw.png"));
            oneStepForward.setName("simB4");
            oneStepForward.setBounds(internalX+80, internalY, 80, 30);
            oneStepForward.setToolTipText(lang.getText("HDWT_entry015PNsim"));
            oneStepForward.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulator.startSimulation(SimulatorMode.STEP);
                mode = SIMULATOR;
            });
            components.add(oneStepForward);

            internalY += 30;

            c1Button = new JButton(lang.getText("HDWT_entry306QuickSim"));
            c1Button.setName("resetColor");
            c1Button.setBounds(internalX, internalY, 80, 30);
            c1Button.setToolTipText(lang.getText("HDWT_entry016PNsim"));
            c1Button.setEnabled(false);
            c1Button.addActionListener(actionEvent -> {
                overlord.getWorkspace().getProject().storeColors();
            });
            components.add(c1Button);

            c2Button = new JButton(lang.getText("HDWT_entry307QuickSim"));
            c2Button.setName("SaveM0Color");
            c2Button.setBounds(internalX+80, internalY, 80, 30);
            c2Button.setToolTipText(lang.getText("HDWT_entry017PNsim"));
            c2Button.setEnabled(false);
            c2Button.addActionListener(actionEvent -> {
                JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry017PNsim_1"),
                        lang.getText("HDWT_entry017PNsim_2"), JOptionPane.INFORMATION_MESSAGE);
                overlord.getWorkspace().getProject().restoreColors();
            });
            components.add(c2Button);

            internalY += 40;

            JSeparator separator5 = new JSeparator();
            separator5.setOrientation(SwingConstants.HORIZONTAL);
            separator5.setBounds(internalX, internalY, 160, 2);
            components.add(separator5);

            internalY += 10;

            JButton statesButton = new JButton(lang.getText("HDWT_entry308QuickSim")); //States manager
            statesButton.setName("State manager");
            statesButton.setIcon(Tools.getResIcon32("/icons/stateManager/stManIcon.png"));
            statesButton.setBounds(internalX, internalY, 150, 40);
            statesButton.setToolTipText(lang.getText("HDWT_entry018PNsim"));
            statesButton.setEnabled(true);
            statesButton.addActionListener(actionEvent -> {
                if (overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry018PNsim_1"),
                            lang.getText("HDWT_entry018PNsim_1t"), JOptionPane.WARNING_MESSAGE);
                } else {
                    new HolmesStatesManager();
                }
            });
            components.add(statesButton);

            internalY += 45;

            //doNotUpdate = false;
            maximumModeCheckBox = new JCheckBox(lang.getText("HDWT_entry019PNsim"));
            maximumModeCheckBox.setBounds(internalX, internalY, 200, 20);
            maximumModeCheckBox.addActionListener(actionEvent -> {
                if (doNotUpdate)
                    return;

                if (singleModeCheckBox.isSelected()) {
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry019PNsim_1"),
                            lang.getText("HDWT_entry019PNsim_1t"), JOptionPane.WARNING_MESSAGE);
                    doNotUpdate = true;
                    maximumModeCheckBox.setSelected(overlord.getSettingsManager().getValue("simSingleMode").equals("1"));
                    doNotUpdate = false;
                }

                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                simulator.setMaxMode(abstractButton.getModel().isSelected());
            });
            components.add(maximumModeCheckBox);

            internalY += 20;

            singleModeCheckBox = new JCheckBox(lang.getText("HDWT_entry020PNsim"));
            singleModeCheckBox.setBounds(internalX, internalY, 200, 20);
            singleModeCheckBox.addActionListener(actionEvent -> {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                if (abstractButton.getModel().isSelected()) {
                    simulator.setSingleMode(true);
                    doNotUpdate = true;
                    singleModeCheckBox.setSelected(overlord.getSettingsManager().getValue("simSingleMode").equals("1"));
                    doNotUpdate = false;
                } else {
                    simulator.setSingleMode(false);

                    doNotUpdate = true;
                    singleModeCheckBox.setSelected(false);
                    simulator.setMaxMode(false);
                    doNotUpdate = false;
                }
            });
            components.add(singleModeCheckBox);
            internalY += 25;
            
            JSeparator separator9 = new JSeparator();
            separator9.setOrientation(SwingConstants.HORIZONTAL);
            separator9.setBounds(internalX, internalY, 160, 2);
            components.add(separator9);

            internalY += 5;
            
            JLabel graphInfoLabel = new JLabel(lang.getText("HDWT_entry314SimPanel")); 
            graphInfoLabel.setBounds(internalX, internalY, 150, 20);
            components.add(graphInfoLabel);

            internalY += 20;

            showColorsCheckBox = new JCheckBox(lang.getText("HDWT_entry315SimPanel"));
            showColorsCheckBox.setSelected(overlord.getSettingsManager().getValue("simPlacesColors").equals("1"));
            showColorsCheckBox.setBounds(internalX, internalY, 150, 20);
            showColorsCheckBox.addActionListener(actionEvent -> {
                AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
                if (abstractButton.getModel().isSelected()) {
                    overlord.getSettingsManager().setValue("simPlacesColors", "1", false);
                } else {
                    overlord.getSettingsManager().setValue("simPlacesColors", "0", false);
                }
            });
            components.add(showColorsCheckBox);
            
            internalY += 25;
            
            JLabel transDelayLabel = new JLabel(lang.getText("HPPwinSIM_entry042")); //Transition firing delay
            transDelayLabel.setBounds(internalX, internalY, 150, 20);
            components.add(transDelayLabel);

            JLabel arcDelayLabel = new JLabel(lang.getText("HPPwinSIM_entry043")); //Arc token delay
            arcDelayLabel.setBounds(internalX, internalY+70, 150, 20);
            components.add(arcDelayLabel);

            final JSlider arcDelaySlider = new JSlider(JSlider.HORIZONTAL, 5, 85, 25);
            arcDelaySlider.setBounds(internalX, internalY+90, 150, 50);
            arcDelaySlider.setMinorTickSpacing(10);
            arcDelaySlider.setMajorTickSpacing(20);
            arcDelaySlider.setPaintTicks(true);
            //arcDelaySlider.setPaintLabels(true);
            arcDelaySlider.setLabelTable(arcDelaySlider.createStandardLabels(10));
            arcDelaySlider.addChangeListener(e -> {
                JSlider s = (JSlider) e.getSource();
                int val = s.getValue();
                int reference = overlord.simSettings.getTransitionGraphicDelay();
                if(val <= reference) {
                    arcDelaySlider.setValue(val);
                    overlord.simSettings.setArcGraphicDelay(val);
                } else {
                    s.setValue(reference);
                }
            });
            components.add(arcDelaySlider);

            final JSlider transDelaySlider = new JSlider(JSlider.HORIZONTAL, 5, 85, 25);
            transDelaySlider.setBounds(internalX, internalY+20, 150, 50);
            transDelaySlider.setMinorTickSpacing(10);
            transDelaySlider.setMajorTickSpacing(20);
            transDelaySlider.setPaintTicks(true);
            //transDelaySlider.setPaintLabels(true);
            transDelaySlider.setLabelTable(transDelaySlider.createStandardLabels(10));
            transDelaySlider.addChangeListener(new ChangeListener() {
                private JSlider anotherSlider = null;
                public void stateChanged(ChangeEvent e) {
                    JSlider s = (JSlider) e.getSource();
                    int value = s.getValue();
                    transDelaySlider.setValue(value);
                    overlord.simSettings.setTransitionGraphicDelay(value);
                    if(value <  overlord.simSettings.getArcGraphicDelay()) {
                        anotherSlider.setValue(value);
                    }
                }
                private ChangeListener yesWeCan(JSlider slider){
                    anotherSlider = slider;
                    return this;
                }
            }.yesWeCan(arcDelaySlider) );
            components.add(transDelaySlider);
            
            
        } else { // nienormalny symulator: XTPN
            internalX = 5;
            internalY = 30;
            //i tyle, jakby kto pytał, to jest obiekt, można mu zmienić tekst, ale się nie wyświetla NA RAZIE tak ma być
            timeStepLabelValue = new JLabel("0");

            JLabel stepLabelText = new JLabel(lang.getText("HDWT_entry021XTPNsim"));
            stepLabelText.setBounds(internalX, internalY, 90, 20);
            components.add(stepLabelText);

            stepLabelXTPN = new JLabel("0");
            stepLabelXTPN.setBounds(internalX + 60, internalY, 90, 20);
            components.add(stepLabelXTPN);

            internalY += 20;

            JLabel timeLabelText = new JLabel(lang.getText("HDWT_entry022XTPNsim"));
            timeLabelText.setBounds(internalX, internalY, 90, 20);
            components.add(timeLabelText);

            timeLabelXTPN = new JLabel("0.0");
            timeLabelXTPN.setBounds(internalX + 60, internalY, 90, 20);
            components.add(timeLabelXTPN);

            internalY += 20;

            JLabel optionsLavel = new JLabel(lang.getText("HDWT_entry023XTPNsim"));
            optionsLavel.setBounds(internalX, internalY, 120, 20);
            components.add(optionsLavel);

            internalY += 20;

            HolmesRoundedButton loopSimulation = new HolmesRoundedButton(""
                    , "simulator/simStart1.png", "simulator/simStart2.png", "simulator/simStart3.png");
            loopSimulation.setName("XTPNstart");
            loopSimulation.setBounds(internalX, internalY, 40, 35);
            loopSimulation.setToolTipText(lang.getText("HDWT_entry024XTPNsim"));
            loopSimulation.addActionListener(actionEvent -> {
                if (overlord.getWorkspace().getProject().isSimulationActive()) {
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry024XTPNsim_1"), lang.getText("HDWT_entry024XTPNsim_1t"),
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                    simulatorXTPN.startSimulation(GraphicalSimulatorXTPN.SimulatorModeXTPN.XTPNLOOP);
                    mode = SIMULATOR;
                }

            });
            components.add(loopSimulation);

            HolmesRoundedButton pauseSimulation = new HolmesRoundedButton(""
                    , "simulator/simPause1.png", "simulator/simPause2.png", "simulator/simPause3.png");
            pauseSimulation.setName("XTPNpause");
            pauseSimulation.setBounds(internalX + 40, internalY, 40, 35);
            pauseSimulation.setToolTipText(lang.getText("HDWT_entry025XTPNsim"));
            pauseSimulation.setEnabled(false);
            pauseSimulation.addActionListener(actionEvent -> {
                overlord.getWorkspace().setGraphMode(DrawModes.POINTER);
                simulatorXTPN.pause();
                mode = SIMULATOR;
            });
            components.add(pauseSimulation);

            HolmesRoundedButton stopSimulation = new HolmesRoundedButton(""
                    , "simulator/simStop1.png", "simulator/simStop2.png", "simulator/simStop2.png");
            stopSimulation.setName("XTPNstop");
            stopSimulation.setBounds(internalX + 80, internalY, 40, 35);
            stopSimulation.setToolTipText(lang.getText("HDWT_entry026XTPNsim"));
            stopSimulation.setEnabled(false);
            stopSimulation.addActionListener(actionEvent -> {
                simulatorXTPN.stop();
                mode = SIMULATOR;
            });
            components.add(stopSimulation);

            internalY += 40;

            HolmesRoundedButton resetButton = new HolmesRoundedButton(lang.getText("HDWT_entry309QuickSim")
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            resetButton.setName("resetM0button");
            resetButton.setBounds(internalX, internalY, 75, 40);
            resetButton.setToolTipText(lang.getText("HDWT_entry027XTPNsim"));
            resetButton.setEnabled(true);
            resetButton.addActionListener(actionEvent -> overlord.getWorkspace().getProject().restoreMarkingZero());
            components.add(resetButton);

            HolmesRoundedButton storeButton = new HolmesRoundedButton(lang.getText("HDWT_entry310QuickSim")
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            storeButton.setName("storeM0button");
            storeButton.setBounds(internalX+75, internalY, 75, 40);
            storeButton.setToolTipText(lang.getText("HDWT_entry028XTPNsim"));
            storeButton.setEnabled(true);
            storeButton.addActionListener(actionEvent -> {
                int selected = 0;
                Object[] options = {lang.getText("HDWT_entry028XTPNsim_op1"), lang.getText("cancel"),};
                int n = JOptionPane.showOptionDialog(null,
                        lang.getText("HDWT_entry028XTPNsim_1"), lang.getText("HDWT_entry028XTPNsim_1t"), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                if (n == 1) {
                    return;
                }

                overlord.getWorkspace().getProject().accessStatesManager().replaceStoredMultiset_M_withCurrentNetState(selected);
                overlord.markNetChange();
            });
            components.add(storeButton);

            internalY += 50;

            JLabel transDelayLabel = new JLabel(lang.getText("HDWT_entry029XTPNsim"));
            transDelayLabel.setBounds(internalX, internalY, 150, 20);
            components.add(transDelayLabel);

            internalY += 20;

            final JSlider arcDelaySlider = new JSlider(JSlider.HORIZONTAL, 5, 85, 25);
            arcDelaySlider.setBounds(internalX, internalY, 150, 50);
            arcDelaySlider.setMinorTickSpacing(5);
            arcDelaySlider.setMajorTickSpacing(20);
            arcDelaySlider.setPaintTicks(true);
            arcDelaySlider.setPaintLabels(true);
            arcDelaySlider.setLabelTable(arcDelaySlider.createStandardLabels(20));
            arcDelaySlider.addChangeListener(e -> {
                JSlider s = (JSlider) e.getSource();
                int val = s.getValue();
                int reference = overlord.simSettings.getTransitionGraphicDelay();
                if (val <= reference) {
                    arcDelaySlider.setValue(val);
                    overlord.simSettings.setArcGraphicDelay(val);
                } else {
                    s.setValue(reference);
                }
            });
            components.add(arcDelaySlider);

            internalY += 50;

            JLabel arcDelayLabel = new JLabel(lang.getText("HDWT_entry311QuickSim"));
            arcDelayLabel.setBounds(internalX, internalY, 120, 20);
            components.add(arcDelayLabel);

            internalY += 20;

            final JSlider transDelaySlider = new JSlider(JSlider.HORIZONTAL, 5, 85, 25);
            transDelaySlider.setBounds(internalX, internalY, 150, 50);
            transDelaySlider.setMinorTickSpacing(5);
            transDelaySlider.setMajorTickSpacing(20);
            transDelaySlider.setPaintTicks(true);
            transDelaySlider.setPaintLabels(true);
            transDelaySlider.setLabelTable(transDelaySlider.createStandardLabels(20));
            transDelaySlider.addChangeListener(new ChangeListener() {
                private JSlider anotherSlider = null;

                public void stateChanged(ChangeEvent e) {
                    JSlider s = (JSlider) e.getSource();
                    int value = s.getValue();
                    transDelaySlider.setValue(value);
                    overlord.simSettings.setTransitionGraphicDelay(value);
                    if (value < overlord.simSettings.getArcGraphicDelay()) {
                        anotherSlider.setValue(value);
                    }
                }

                private ChangeListener yesWeCan(JSlider slider) {
                    anotherSlider = slider;
                    return this;
                }
            }.yesWeCan(arcDelaySlider));
            components.add(transDelaySlider);

            internalY += 50;

            JLabel timeValuesVisLabel = new JLabel(lang.getText("HDWT_entry030XTPNsim"));
            timeValuesVisLabel.setBounds(internalX, internalY, 140, 20);
            components.add(timeValuesVisLabel);

            internalY += 20;

            HolmesRoundedButton showAlfaSwitchButton = new HolmesRoundedButton("<html><center>\u03B1:ON</center></html>"
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            showAlfaSwitchButton.setName("switchAlphas");
            showAlfaSwitchButton.setBounds(internalX, internalY, 80, 30);
            showAlfaSwitchButton.setToolTipText(lang.getText("HDWT_entry031XTPNsim"));
            showAlfaSwitchButton.setEnabled(true);
            showAlfaSwitchButton.addActionListener(actionEvent -> {
                HolmesRoundedButton button = (HolmesRoundedButton) actionEvent.getSource();
                if (alphaValuesVisible) {
                    alphaValuesVisible = false;
                    button.setNewText("<html><center>\u03B1:OFF</center></html>");//u03B1
                } else {
                    alphaValuesVisible = true;
                    button.setNewText("<html><center>\u03B1:ON</center></html>");
                }
                for (Transition trans : overlord.getWorkspace().getProject().getTransitions()) {
                    ((TransitionXTPN) trans).setAlphaRangeVisibility(alphaValuesVisible);
                }
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
            });
            components.add(showAlfaSwitchButton);

            HolmesRoundedButton showBetaSwitchButton = new HolmesRoundedButton("<html><center>\u03B2:ON</center></html>"
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            showBetaSwitchButton.setName("switchBetas");
            showBetaSwitchButton.setBounds(internalX + 80, internalY, 80, 30);
            showBetaSwitchButton.setToolTipText(lang.getText("HDWT_entry032XTPNsim"));
            showBetaSwitchButton.setEnabled(true);
            showBetaSwitchButton.addActionListener(actionEvent -> {
                HolmesRoundedButton button = (HolmesRoundedButton) actionEvent.getSource();

                if (betaValuesVisible) {
                    betaValuesVisible = false;
                    button.setNewText("<html><center>\u03B2:OFF</center></html>"); //u03B2
                } else {
                    betaValuesVisible = true;
                    button.setNewText("<html><center>\u03B2:ON</center></html>");
                }
                for (Transition trans : overlord.getWorkspace().getProject().getTransitions()) {
                    ((TransitionXTPN) trans).setBetaRangeVisibility(betaValuesVisible);
                }
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
            });
            components.add(showBetaSwitchButton);

            internalY += 30;

            HolmesRoundedButton showGammaSwitchButton = new HolmesRoundedButton("<html><center>\u03B3:ON</center></html>"
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            showGammaSwitchButton.setName("switchGammas");
            showGammaSwitchButton.setBounds(internalX, internalY, 80, 30);
            showGammaSwitchButton.setToolTipText(lang.getText("HDWT_entry033XTPNsim"));
            showGammaSwitchButton.setEnabled(true);
            showGammaSwitchButton.addActionListener(actionEvent -> {
                HolmesRoundedButton button = (HolmesRoundedButton) actionEvent.getSource();

                if (gammaValuesVisible) {
                    gammaValuesVisible = false;
                    button.setNewText("<html><center>\u03B3:OFF</center></html>"); //u03C4
                } else {
                    gammaValuesVisible = true;
                    button.setNewText("<html><center>\u03B3:ON</center></html>");
                }
                for (Place place : overlord.getWorkspace().getProject().getPlaces()) {
                    ((PlaceXTPN) place).setGammaRangeVisibility(gammaValuesVisible);
                }
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
            });
            components.add(showGammaSwitchButton);

            HolmesRoundedButton showTauSwitchButton = new HolmesRoundedButton("<html><center>\u03C4:ON</center></html>"
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            showTauSwitchButton.setName("switchTaus");
            showTauSwitchButton.setBounds(internalX + 80, internalY, 80, 30);
            showTauSwitchButton.setToolTipText(lang.getText("HDWT_entry034XTPNsim"));
            showTauSwitchButton.setEnabled(true);
            showTauSwitchButton.addActionListener(actionEvent -> {
                HolmesRoundedButton button = (HolmesRoundedButton) actionEvent.getSource();

                if (tauValuesVisible) {
                    tauValuesVisible = false;
                    button.setNewText("<html><center>\u03C4:OFF</center></html>"); //u03C4
                } else {
                    tauValuesVisible = true;
                    button.setNewText("<html><center>\u03C4:ON</center></html>");
                }
                for (Transition trans : overlord.getWorkspace().getProject().getTransitions()) {
                    ((TransitionXTPN) trans).setTauTimersVisibility(tauValuesVisible);
                }
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
            });
            components.add(showTauSwitchButton);
            quickSim = new QuickSimTools(this);

            internalY += 40;

            JLabel qSimLabel = new JLabel(lang.getText("HDWT_entry035XTPNsim"));
            qSimLabel.setBounds(internalX, internalY, 140, 20);
            components.add(qSimLabel);

            internalY += 20;

            acqDataButtonXTPN = new HolmesRoundedButton("<html>Simulate</html>"
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            acqDataButtonXTPN.setBounds(internalX, internalY, 100, 30);
            acqDataButtonXTPN.setMargin(new Insets(0, 0, 0, 0));
            acqDataButtonXTPN.setFocusPainted(false);
            acqDataButtonXTPN.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
            acqDataButtonXTPN.setToolTipText(lang.getText("HDWT_entry036XTPNsim"));
            acqDataButtonXTPN.addActionListener(actionEvent -> {
                if (overlord.getWorkspace().getProject().isSimulationActive()) {
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry036XTPNsim_1"), lang.getText("HDWT_entry036XTPNsim_1t"),
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    quickSim.acquireDataXTPN(qSimXTPNSbySteps, qSimXTPNsimStatsSteps, qSimXTPNStatsTime
                            , qSimXTPNrepeateSim, qSimXTPNStatsRepetitions, qSimXTPNknockoutMode
                            , qSimXTPNProgressBar, acqDataButtonXTPN);
                }
            });
            components.add(acqDataButtonXTPN);

            internalY += 35;

            qSimXTPNProgressBar = new JProgressBar();
            qSimXTPNProgressBar.setBounds(internalX, internalY, 160, 20);
            qSimXTPNProgressBar.setMaximum(100);
            qSimXTPNProgressBar.setMinimum(0);
            qSimXTPNProgressBar.setValue(0);
            qSimXTPNProgressBar.setStringPainted(true);
            components.add(qSimXTPNProgressBar);

            internalY += 25;

            qSimXTPNStatsStepsCheckbox = new JCheckBox(lang.getText("HDWT_entry037XTPNsim"));
            qSimXTPNStatsStepsCheckbox.setBounds(internalX, internalY, 70, 20);
            qSimXTPNStatsStepsCheckbox.setSelected(qSimXTPNSbySteps);
            qSimXTPNStatsStepsCheckbox.addItemListener(e -> {
                if (doNotUpdate)
                    return;
                JCheckBox box = (JCheckBox) e.getSource();
                qSimXTPNSbySteps = box.isSelected();
                doNotUpdate = true;
                qSimXTPNStatsTimeCheckbox.setSelected(!qSimXTPNSbySteps);
                doNotUpdate = false;
            });
            components.add(qSimXTPNStatsStepsCheckbox);

            SpinnerModel qSimStepsSpinnerModel = new SpinnerNumberModel(qSimXTPNsimStatsSteps, 0, 100000000, 1000);
            JSpinner qsimStepsSpinner = new JSpinner(qSimStepsSpinnerModel);
            qsimStepsSpinner.setBounds(internalX + 70, internalY, 90, 20);
            qsimStepsSpinner.addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                int tmp = (int) spinner.getValue();
                qSimXTPNsimStatsSteps = tmp;
            });
            components.add(qsimStepsSpinner);

            internalY += 22;

            qSimXTPNStatsTimeCheckbox = new JCheckBox(lang.getText("HDWT_entry038XTPNsim"));
            qSimXTPNStatsTimeCheckbox.setBounds(internalX, internalY, 70, 20);
            qSimXTPNStatsTimeCheckbox.setSelected(!qSimXTPNSbySteps);
            qSimXTPNStatsTimeCheckbox.addItemListener(e -> {
                if (doNotUpdate)
                    return;
                JCheckBox box = (JCheckBox) e.getSource();
                qSimXTPNSbySteps = !(box.isSelected());
                doNotUpdate = true;
                qSimXTPNStatsStepsCheckbox.setSelected(qSimXTPNSbySteps);
                doNotUpdate = false;
            });
            components.add(qSimXTPNStatsTimeCheckbox);

            SpinnerModel qsimTimeLengthSpinnerModel = new SpinnerNumberModel(qSimXTPNStatsTime, 0, 1000000, 100);
            JSpinner qsimTimeSpinner = new JSpinner(qsimTimeLengthSpinnerModel);
            qsimTimeSpinner.setBounds(internalX + 70, internalY, 90, 20);
            qsimTimeSpinner.addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                qSimXTPNStatsTime = (double) spinner.getValue();
            });
            components.add(qsimTimeSpinner);

            internalY += 22;

            JCheckBox qSimXTPNrepetitionsCheckBox = new JCheckBox(lang.getText("HDWT_entry039XTPNsim"));
            qSimXTPNrepetitionsCheckBox.setBounds(internalX, internalY, 70, 20);
            qSimXTPNrepetitionsCheckBox.setSelected(!qSimXTPNSbySteps);
            qSimXTPNrepetitionsCheckBox.addItemListener(e -> {
                JCheckBox box = (JCheckBox) e.getSource();
                qSimXTPNrepeateSim = (box.isSelected());
            });
            components.add(qSimXTPNrepetitionsCheckBox);

            SpinnerModel qsimRepetitionsSpinnerModel = new SpinnerNumberModel(qSimXTPNStatsRepetitions, 10, 100, 10);
            JSpinner qsimRepetitionsSpinner = new JSpinner(qsimRepetitionsSpinnerModel);
            qsimRepetitionsSpinner.setBounds(internalX + 70, internalY, 90, 20);
            qsimRepetitionsSpinner.addChangeListener(e -> {
                JSpinner spinner = (JSpinner) e.getSource();
                qSimXTPNStatsRepetitions = (int) spinner.getValue();
            });
            components.add(qsimRepetitionsSpinner);

            internalY += 22;

            JCheckBox qSimXTPNknockoutCheckBox = new JCheckBox(lang.getText("HDWT_entry040XTPNsim"));
            qSimXTPNknockoutCheckBox.setBounds(internalX, internalY, 150, 20);
            qSimXTPNknockoutCheckBox.setSelected(qSimXTPNknockoutMode);
            qSimXTPNknockoutCheckBox.addItemListener(e -> {
                JCheckBox box = (JCheckBox) e.getSource();
                qSimXTPNknockoutMode = (box.isSelected());
            });
            components.add(qSimXTPNknockoutCheckBox);

            internalY += 30;

            HolmesRoundedButton sSimAlpha = new HolmesRoundedButton("<html><center>XTPN net<br>simulator</center></html>"
                    , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
            sSimAlpha.setBounds(internalX, internalY, 100, 35);
            sSimAlpha.setMargin(new Insets(0, 0, 0, 0));
            sSimAlpha.setFocusPainted(false);
            sSimAlpha.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
            sSimAlpha.setToolTipText(lang.getText("HDWT_entry041XTPNsim"));
            sSimAlpha.addActionListener(actionEvent -> {
                if(overlord.getWorkspace().getProject().isSimulationActive()) {
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry041XTPNsim_1"), lang.getText("HDWT_entry041XTPNsim_1t"),
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    overlord.showStateSimulatorWindowXTPN();
                }
            });
            components.add(sSimAlpha);

        }
        for (JComponent component : components) {
            panel.add(component);
        }
        panel.revalidate();
        panel.repaint();
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************     MIEJSCE      ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda pomocnicza konstruktora podokna wyświetlającego właściwości klikniętego miejsca sieci.
     * @param place    Place - obiekt miejsca
     * @param location ElementLocation - lokalizacja miejsca
     */
    @SuppressWarnings("UnusedAssignment")
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
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry042place"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00567exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // KOMENTARZE WIERZCHOŁKA
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry043place"), JLabel.LEFT);
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
            JLabel tokenLabel = new JLabel(lang.getText("HDWT_entry044place"), JLabel.LEFT);
            tokenLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
            components.add(tokenLabel);
            int tok = place.getTokensNumber();
            boolean problem = false;
            if (tok < 0) {
                overlord.log(lang.getText("LOGentry00044") + place.getName(), "error", true);
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

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry045place"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        //ZOOM:
        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry046place"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // PORTAL
        JLabel portalLabel = new JLabel(lang.getText("HDWT_entry047place"), JLabel.LEFT);
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
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry047place_1"), lang.getText("problem"),
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(portalBox);

        //LOKALIZACJA:
        JLabel locLabel = new JLabel(lang.getText("HDWT_entry048place"), JLabel.LEFT);
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

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry049place"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = place.getElementLocations().indexOf(location);
        int xNameOffset = place.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = place.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

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
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry050place"));
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
                    overlord.setNameLocationChangeMode(place_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
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
        if (place.isColored) {
            // PLACE TOKEN
            JLabel tokenLabel = new JLabel(lang.getText("HDWT_entry051place"), JLabel.LEFT);
            tokenLabel.setBounds(columnA_posX, columnA_Y += 50, colACompLength, 20);
            components.add(tokenLabel);
            int tok0 = place.getTokensNumber();
            boolean problem = false;
            if (tok0 < 0) {
                overlord.log(lang.getText("LOGentry00045") + place.getName(), "error", true);
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

            JLabel token3Label = new JLabel(lang.getText("HDWT_entry052place"), JLabel.LEFT);
            token3Label.setBounds(columnB_posX + 60, columnB_Y, colACompLength, 20);
            components.add(token3Label);
            int tok3 = ((PlaceColored) place).getColorTokensNumber(3);

            SpinnerModel token3SpinnerModel = new SpinnerNumberModel(tok3, 0, Integer.MAX_VALUE, 1);
            JSpinner token3Spinner = new JSpinner(token3SpinnerModel);
            token3Spinner.setBounds(columnB_posX + 130, columnB_Y, 75, 20);
            token3Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 3);
                overlord.markNetChange();
            });
            components.add(token3Spinner);


            JLabel token1Label = new JLabel(lang.getText("HDWT_entry053place"), JLabel.LEFT);
            token1Label.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
            components.add(token1Label);
            int tok1 = ((PlaceColored) place).getColorTokensNumber(1);

            SpinnerModel token1SpinnerModel = new SpinnerNumberModel(tok1, 0, Integer.MAX_VALUE, 1);
            JSpinner token1Spinner = new JSpinner(token1SpinnerModel);
            token1Spinner.setBounds(columnB_posX - 30, columnB_Y += 20, 75, 20);
            token1Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 1);
                overlord.markNetChange();
            });
            components.add(token1Spinner);

            JLabel token4Label = new JLabel(lang.getText("HDWT_entry054place"), JLabel.LEFT);
            token4Label.setBounds(columnB_posX + 60, columnB_Y, colACompLength, 20);
            components.add(token4Label);
            int tok4 = ((PlaceColored) place).getColorTokensNumber(4);

            SpinnerModel token4SpinnerModel = new SpinnerNumberModel(tok4, 0, Integer.MAX_VALUE, 1);
            JSpinner token4Spinner = new JSpinner(token4SpinnerModel);
            token4Spinner.setBounds(columnB_posX + 130, columnB_Y, 75, 20);
            token4Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 4);
                overlord.markNetChange();
            });
            components.add(token4Spinner);

            JLabel token2Label = new JLabel(lang.getText("HDWT_entry055place"), JLabel.LEFT);
            token2Label.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
            components.add(token2Label);
            int tok2 = ((PlaceColored) place).getColorTokensNumber(2);

            SpinnerModel token2SpinnerModel = new SpinnerNumberModel(tok2, 0, Integer.MAX_VALUE, 1);
            JSpinner token2Spinner = new JSpinner(token2SpinnerModel);
            token2Spinner.setBounds(columnB_posX - 30, columnB_Y += 20, 75, 20);
            token2Spinner.addChangeListener(e -> {
                int tokenz = (int) ((JSpinner) e.getSource()).getValue();
                setColorTokens(tokenz, 2);
                overlord.markNetChange();
            });
            components.add(token2Spinner);

            JLabel token5Label = new JLabel(lang.getText("HDWT_entry056place"), JLabel.LEFT);
            token5Label.setBounds(columnB_posX + 60, columnB_Y, colACompLength, 20);
            components.add(token5Label);
            int tok5 = ((PlaceColored) place).getColorTokensNumber(5);

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

        /*
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

                    ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
                    if (id >= 0 && id < places.size()) {

                        Place p1 = (Place) element;
                        Place p2 = places.get(id);
                        if (places.indexOf(p1) == id)
                            return;

                        int pos1 = overlord.getWorkspace().getProject().getNodes().indexOf(p1);
                        int pos2 = overlord.getWorkspace().getProject().getNodes().indexOf(p2);
                        Collections.swap(overlord.getWorkspace().getProject().getNodes(), pos1, pos2);
                        overlord.log("Swapping places " + p1.getName() + " and " + p2.getName() + " successfull.", "text", true);

                        WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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

         */

        panel.setLayout(null);
        for (JComponent component : components) {
            panel.add(component);
        }
        panel.setOpaque(true);
        panel.repaint();
        add(panel);
    }

    //**************************************************************************************
    //*********************************      MIEJSCE     ***********************************
    //*********************************       _XTPN       ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda pokazuje okno właściwości klikniętego miejsca XTPN.
     * @param place    Place - obiekt miejsca
     * @param location ElementLocation - lokalizacja miejsca
     */
    private void createXTPNPlaceSubWindow(PlaceXTPN place, ElementLocation location) {
        int columnA_posX = 10;
        int columnB_posX = 100;
        int columnA_Y = 0;
        int columnB_Y = 0;
        int colACompLength = 70;
        int colBCompLength = 200;
        elementLocation = location;
        initiateContainers();
        mode = XTPN_PLACE;
        element = place;
        Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);

        // ID
        JLabel idLabel = new JLabel("ID XTPN:", JLabel.LEFT);
        idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
        components.add(idLabel);
        
        int gID = overlord.getWorkspace().getProject().getPlaces().indexOf(place);
        
        JLabel idLabel2 = new JLabel(Integer.toString(gID));
        idLabel2.setBounds(columnB_posX, columnB_Y += 10, 50, 20);
        idLabel2.setFont(normalFont);
        components.add(idLabel2);

        JLabel idLabel3 = new JLabel("global ID:");
        idLabel3.setBounds(columnB_posX + 55, columnA_Y, 60, 20);
        components.add(idLabel3);
        JLabel idLabel4 = new JLabel(place.getID() + "");
        idLabel4.setBounds(columnB_posX + 120, columnB_Y, 50, 20);
        idLabel4.setFont(normalFont);
        components.add(idLabel4);

        // XTPN-place NAME
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry057XTPNplace"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00568exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // XTPN-place  KOMENTARZE
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry058XTPNplace"), JLabel.LEFT);
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

        // XTPN-place przycisk Gamma ON/OFF
        JLabel gammaLabel = new JLabel(lang.getText("HDWT_entry059XTPNplace"), JLabel.LEFT);
        gammaLabel.setBounds(columnA_posX, columnA_Y += 25, colACompLength + 15, 20);
        components.add(gammaLabel);

        buttonGammaMode = new HolmesRoundedButton(lang.getText("HDWT_entry060XTPNplaceG_ON")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonGammaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonGammaMode.setName("gammaModeButton");
        buttonGammaMode.setBounds(columnB_posX, columnB_Y += 25, 65, 35);
        buttonGammaMode.setFocusPainted(false);
        if (place.isGammaModeActive()) {
            buttonGammaMode.setNewText(lang.getText("HDWT_entry060XTPNplaceG_ON"));
            buttonGammaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonGammaMode.setNewText(lang.getText("HDWT_entry060XTPNplaceG_OFF"));
            buttonGammaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonGammaMode.addActionListener(e -> {
            if (doNotUpdate)
                return;

            SharedActionsXTPN.access().buttonGammaSwitchMode(e, place, null, gammaVisibilityButton, elementLocation);
        });
        components.add(buttonGammaMode);

        // XTPN-place gamma values visibility
        gammaVisibilityButton = new HolmesRoundedButton(lang.getText("HDWT_entry061XTPNplaceG_vis")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        gammaVisibilityButton.setName("gammaVisButton");
        gammaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        gammaVisibilityButton.setBounds(columnB_posX + 65, columnB_Y, 65, 35);
        gammaVisibilityButton.setFocusPainted(false);
        if (place.isGammaModeActive()) {
            if (place.isGammaRangeVisible()) {
                gammaVisibilityButton.setNewText(lang.getText("HDWT_entry061XTPNplaceG_vis"));
                gammaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                gammaVisibilityButton.setNewText(lang.getText("HDWT_entry061XTPNplaceG_invis"));
                gammaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            gammaVisibilityButton.setEnabled(false);
        }
        gammaVisibilityButton.addActionListener(e -> {
            if (doNotUpdate)
                return;
            HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
            if (place.isGammaRangeVisible()) {
                ((PlaceXTPN) element).setGammaRangeVisibility(false);

                button.setNewText(lang.getText("HDWT_entry061XTPNplaceG_invis"));
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            } else {
                ((PlaceXTPN) element).setGammaRangeVisibility(true);

                button.setNewText(lang.getText("HDWT_entry061XTPNplaceG_vis"));
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(gammaVisibilityButton);

        // XTPN-place gamma offset
        gammaLocChangeButton = new HolmesRoundedButton(lang.getText("HDWT_entry062XTPNplaceG_off")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        //gammaLocChangeButton = new JButton("<html>Gamma<br>offset<html>");
        gammaLocChangeButton.setName("gammaOffsetButton");
        gammaLocChangeButton.setToolTipText(lang.getText("HDWT_entry064XTPNplaceG"));
        gammaLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        gammaLocChangeButton.setBounds(columnB_posX + 130, columnB_Y, 65, 35);
        gammaLocChangeButton.setFocusPainted(false);
        if (place.isGammaModeActive() && place.isGammaRangeVisible()) {
            if (gammaLocChangeMode) {

                gammaLocChangeButton.setNewText(lang.getText("HDWT_entry063XTPNplaceG_changeLoc"));
                gammaLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            } else {
                gammaLocChangeButton.setNewText(lang.getText("HDWT_entry062XTPNplaceG_off"));
                gammaLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
        } else {
            gammaLocChangeButton.setNewText(lang.getText("HDWT_entry063XTPNplaceG_changeLoc"));
            gammaLocChangeMode = false;
            gammaLocChangeButton.setEnabled(false);
        }
        gammaLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Place place_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                if (!gammaLocChangeMode) { //włączamy tryb przesuwania napisu
                    gammaLocChangeButton.setNewText(lang.getText("HDWT_entry063XTPNplaceG_changeLoc"));
                    gammaLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                    gammaLocChangeMode = true;
                    overlord.setNameLocationChangeMode(place_tmp, el_tmp, GUIManager.locationMoveType.GAMMA);
                } else {
                    gammaLocChangeButton.setNewText(lang.getText("HDWT_entry062XTPNplaceG_off"));
                    gammaLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                    gammaLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
                }
            }

            private ActionListener yesWeCan(Place place, ElementLocation inLoc) {
                place_tmp = place;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(place, location));
        components.add(gammaLocChangeButton);

        // XTPN-place  Zakresy gamma:
        JLabel minMaxLabel = new JLabel(lang.getText("HDWT_entry065XTPNplaceG"), JLabel.LEFT);
        minMaxLabel.setBounds(columnA_posX, columnA_Y += 40, colACompLength + 20, 20);
        components.add(minMaxLabel);

        // format danych gamma do 6 miejsc po przecinku
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(place.getFractionForPlaceXTPN());
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Double example = 3.14;

        JFormattedTextField gammaMinTextField = new JFormattedTextField(formatter);
        gammaMinTextField.setValue(example);
        gammaMinTextField.setValue(place.getGammaMinValue());
        gammaMinTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00569exception")+"\n"+ex.getMessage(), "error", true);
            }
            double min = Double.parseDouble("" + field.getValue());

            if (!(SharedActionsXTPN.access().setGammaMinTime(min, (PlaceXTPN) element, elementLocation))) {
                doNotUpdate = true;
                field.setValue(place.getGammaMinValue());
                doNotUpdate = false;
                overlord.markNetChange();
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });

        JFormattedTextField gammaMaxTextField = new JFormattedTextField(formatter);
        gammaMaxTextField.setValue(example);
        gammaMaxTextField.setValue(place.getGammaMaxValue());
        gammaMaxTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00570exception")+"\n"+ex.getMessage(), "error", true);
            }

            double max = Double.parseDouble("" + field.getValue());

            if (!(SharedActionsXTPN.access().setGammaMaxTime(max, (PlaceXTPN) element, elementLocation))) {
                doNotUpdate = true;
                field.setValue(place.getGammaMaxValue());
                doNotUpdate = false;
                overlord.markNetChange();
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });

        if (!place.isGammaModeActive()) {
            gammaMinTextField.setEnabled(false);
            gammaMaxTextField.setEnabled(false);
        }

        gammaMinTextField.setBounds(columnB_posX, columnB_Y += 41, 90, 20);
        components.add(gammaMinTextField);
        JLabel slash1 = new JLabel(" / ", JLabel.LEFT);
        slash1.setBounds(columnB_posX + 95, columnA_Y, 15, 20);
        components.add(slash1);
        gammaMaxTextField.setBounds(columnB_posX + 110, columnB_Y, 90, 20);
        components.add(gammaMaxTextField);


        //JLabel tokensXTPNLabel = new JLabel("Tokens options:", JLabel.LEFT);
        //tokensXTPNLabel.setBounds(columnA_posX, columnA_Y += 20, 120, 20);
        //components.add(tokensXTPNLabel);

        JLabel tokensXTPNLabel2 = new JLabel(lang.getText("HDWT_entry066XTPNplaceG") + place.getTokensNumber(), JLabel.LEFT);
        tokensXTPNLabel2.setBounds(columnA_posX, columnA_Y += 20, 140, 20);
        components.add(tokensXTPNLabel2);

        JLabel fractionLabel = new JLabel(lang.getText("HDWT_entry067XTPNplaceG"), JLabel.LEFT);
        fractionLabel.setBounds(columnA_posX, columnA_Y += 22, 70, 20);
        components.add(fractionLabel);

        int fract = place.getFractionForPlaceXTPN();
        SpinnerModel fractionSpinnerModel = new SpinnerNumberModel(fract, 0, 6, 1);
        JSpinner fractionSpinner = new JSpinner(fractionSpinnerModel);
        fractionSpinner.setBounds(columnB_posX, columnB_Y += 42, 40, 20);
        fractionSpinner.addChangeListener(e -> {
            int fraction = (int) ((JSpinner) e.getSource()).getValue();
            place.setFractionForPlaceXTPN(fraction);
        });
        components.add(fractionSpinner);

        HolmesRoundedButton nameLocChangeButton = new HolmesRoundedButton(lang.getText("HDWT_entry068XTPNplaceName_OFF")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        //JButton nameLocChangeButton = new JButton("<html><center>Name<br>offset</center><html>");
        nameLocChangeButton.setName("placeLocOffsetButton");
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnB_posX+131, columnB_Y-12, 65, 35);
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry069XTPNplaceName"));
        nameLocChangeButton.setFocusPainted(false);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Place place_tmp;
            private ElementLocation el_tmp;
            public void actionPerformed(ActionEvent actionEvent) {
                HolmesRoundedButton button = (HolmesRoundedButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button.setNewText(lang.getText("HDWT_entry070XTPNplaceName_changeLoc"));
                    button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(place_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button.setNewText(lang.getText("HDWT_entry068XTPNplaceName_OFF"));
                    button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
                }
            }
            private ActionListener yesWeCan(Place inPlace, ElementLocation inLoc) {
                place_tmp = inPlace;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(place, location));
        components.add(nameLocChangeButton);

        // XTPN-place przycisk okna tokenów
        //JButton tokensWindowButton = new JButton("<html>Tokens<br>window</html>");

        HolmesRoundedButton tokensWindowButton = new HolmesRoundedButton(lang.getText("HDWT_entry071XTPNplaceTokens")
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        tokensWindowButton.setMargin(new Insets(0, 0, 0, 0));
        tokensWindowButton.setBounds(columnA_posX, columnB_Y += 40, 90, 40);
        if (!place.isGammaModeActive()) {
            tokensWindowButton.setEnabled(false);
        }
        tokensWindowButton.addActionListener(actionEvent -> new HolmesXTPNtokens((PlaceXTPN) element, null, place.accessMultiset(), place.isGammaModeActive()));
        components.add(tokensWindowButton);

        // XTPN-place przycisk dodania tokenu XTPN
        HolmesRoundedButton add0tokenButton = new HolmesRoundedButton(lang.getText("HDWT_entry072XTPNplaceTokens")
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        add0tokenButton.setMargin(new Insets(0, 0, 0, 0));
        add0tokenButton.setBounds(columnB_posX, columnB_Y, 90, 40);
        if (place.isGammaModeActive()) {
            add0tokenButton.setText(lang.getText("HDWT_entry073XTPNplaceTokens"));
            add0tokenButton.setBackground(Color.GREEN);
        } else {
            add0tokenButton.setText(lang.getText("HDWT_entry074XTPNplaceTokens"));
            add0tokenButton.setBackground(null);
        }
        add0tokenButton.addActionListener(e -> {
            if (doNotUpdate)
                return;
            JButton button = (JButton) e.getSource();

            place.addTokens_XTPN(1, 0.0);

            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(add0tokenButton);

        // XTPN-place przycisk usunięcia tokenu XTPN
        //JButton remove0tokenButton = new JButton("<html>Remove<br>0-token</html>");
        HolmesRoundedButton remove0tokenButton = new HolmesRoundedButton(lang.getText("HDWT_entry075XTPNplaceTokens")
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        remove0tokenButton.setMargin(new Insets(0, 0, 0, 0));
        remove0tokenButton.setBounds(columnB_posX + 90, columnB_Y, 90, 40);
        if (place.isGammaModeActive()) {
            remove0tokenButton.setText(lang.getText("HDWT_entry076XTPNplaceTokens"));
        } else {
            remove0tokenButton.setText(lang.getText("HDWT_entry077XTPNplaceTokens"));
        }
        remove0tokenButton.addActionListener(e -> {
            if (doNotUpdate)
                return;
            JButton button = (JButton) e.getSource();

            if (place.isGammaModeActive()) {
                int size = place.accessMultiset().size();
                if (size > 0) {
                    double lastToken = place.accessMultiset().get(size - 1);
                    if (lastToken == 0.0) {
                        place.accessMultiset().remove(size - 1);
                        place.addTokensNumber(-1);
                    }
                }
            } else {
                int tokens = place.getTokensNumber();
                if (tokens > 0)
                    place.addTokensNumber(-1);
            }

            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(remove0tokenButton);

        // XTPN-place SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry078XTPNplace"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 80, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 40, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        // XTPN-place ZOOM:
        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry079XTPNplace"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // XTPN-place  PORTAL
        JLabel portalLabel = new JLabel(lang.getText("HDWT_entry080XTPNplace"), JLabel.LEFT);
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
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry080XTPNplace_1"), lang.getText("HDWT_entry080XTPNplace_1t"),
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(portalBox);

        // XTPN-place LOKALIZACJA:
        JLabel locLabel = new JLabel(lang.getText("HDWT_entry081XTPNplace"), JLabel.LEFT);
        locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(locLabel);

        SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
        JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
        locationXSpinner.setBounds(columnB_posX, columnB_Y += 20, 60, 20);
        locationXSpinner.addChangeListener(e -> setX((int) ((JSpinner) e.getSource()).getValue()));

        JLabel labelCom = new JLabel(" , ");
        labelCom.setBounds(columnB_posX + 60, columnB_Y, 10, 20);

        SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
        JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
        locationYSpinner.setBounds(columnB_posX + 70, columnB_Y, 60, 20);
        locationYSpinner.addChangeListener(e -> setY((int) ((JSpinner) e.getSource()).getValue()));
        if (zoom != 100) {
            locationXSpinner.setEnabled(false);
            locationYSpinner.setEnabled(false);
        }
        components.add(locationXSpinner);
        components.add(labelCom);
        components.add(locationYSpinner);

        // XTPN-place  WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 25;

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry082XTPNplace"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = place.getElementLocations().indexOf(location);
        int xNameOffset = place.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = place.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

        nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
        nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);

        JLabel locNameLabelX = new JLabel("x: ", JLabel.LEFT);
        locNameLabelX.setBounds(columnA_posX + 90, columnA_Y, 20, 20);
        components.add(locNameLabelX);

        JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
        nameLocationXSpinner.setBounds(columnA_posX + 105, columnA_Y, 45, 20);
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

        JLabel locNameLabelY = new JLabel("y: ", JLabel.LEFT);
        locNameLabelY.setBounds(columnA_posX + 160, columnA_Y, 15, 20);
        components.add(locNameLabelY);

        JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
        nameLocationYSpinner.setBounds(columnA_posX + 175, columnA_Y, 45, 20);
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

        // XTPN-place przycisk zmiany lokalizacj napisu

        nameLocChangeButton = new HolmesRoundedButton(lang.getText("HDWT_entry083XTPNplace")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        //JButton nameLocChangeButton = new JButton("<html><center>Name<br>offset</center><html>");
        nameLocChangeButton.setName("placeLocOffsetButton");
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnB_posX + 131, columnA_Y - 15, 65, 35);
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry084XTPNplace"));
        nameLocChangeButton.setFocusPainted(false);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Place place_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                HolmesRoundedButton button = (HolmesRoundedButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button.setNewText(lang.getText("HDWT_entry085XTPNplace"));
                    button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(place_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button.setNewText(lang.getText("HDWT_entry083XTPNplace"));
                    button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
                }
            }

            private ActionListener yesWeCan(Place inPlace, ElementLocation inLoc) {
                place_tmp = inPlace;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(place, location));
        components.add(nameLocChangeButton);

        /*
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

                    ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
                    if (id >= 0 && id < places.size()) {

                        Place p1 = (Place) element;
                        Place p2 = places.get(id);
                        if (places.indexOf(p1) == id)
                            return;

                        int pos1 = overlord.getWorkspace().getProject().getNodes().indexOf(p1);
                        int pos2 = overlord.getWorkspace().getProject().getNodes().indexOf(p2);
                        Collections.swap(overlord.getWorkspace().getProject().getNodes(), pos1, pos2);
                        overlord.log("Swapping places " + p1.getName() + " and " + p2.getName() + " successfull.", "text", true);

                        WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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
         */


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
    @SuppressWarnings("UnusedAssignment")
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

        // CLASSICAL TRANSITION ID:
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

        // CLASSICAL TRANSITION NAME:
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry086trans"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00571exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // CLASSICAL TRANSITION KOMENTARZE WIERZCHOŁKA:
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry087trans"), JLabel.LEFT);
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

        // CLASSICAL TRANSITION CHANGE TYPE:
        JLabel changeTypeLabel = new JLabel(lang.getText("HDWT_entry088trans"), JLabel.LEFT);
        changeTypeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(changeTypeLabel);

        classicalTransitionCheckBox = new JCheckBox("PN");
        classicalTransitionCheckBox.setBounds(columnB_posX, columnB_Y += 20, 60, 20);
        classicalTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.PN);
        classicalTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.PN);
                doNotUpdate = true;
                timeTransitionCheckBox.setSelected(false);
                stochasticTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(classicalTransitionCheckBox);

        timeTransitionCheckBox = new JCheckBox("TPN");
        timeTransitionCheckBox.setBounds(columnB_posX + 70, columnB_Y, 60, 20);
        timeTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.TPN);
        timeTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.TPN);
                doNotUpdate = true;
                classicalTransitionCheckBox.setSelected(false);
                stochasticTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(timeTransitionCheckBox);

        stochasticTransitionCheckBox = new JCheckBox("SPN");
        stochasticTransitionCheckBox.setBounds(columnB_posX + 130, columnB_Y, 60, 20);
        stochasticTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.SPN);
        stochasticTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.SPN);
                doNotUpdate = true;
                classicalTransitionCheckBox.setSelected(false);
                timeTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(stochasticTransitionCheckBox);

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

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry089trans"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        //ZOOM:
        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry090trans"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // CLASSICAL TRANSITION LOKALIZACJA:
        JLabel locLabel = new JLabel(lang.getText("HDWT_entry091trans"), JLabel.LEFT);
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

        // CLASSICAL TRANSITION PORTAL
        JLabel portalLabel = new JLabel(lang.getText("HDWT_entry092trans"), JLabel.LEFT);
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
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry092trans_1"), lang.getText("HDWT_entry092trans_1t"),
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(portalBox);

        // CLASSICAL TRANSITION FUNKCYJNOŚĆ
        JLabel functionLabel = new JLabel(lang.getText("HDWT_entry093trans"), JLabel.LEFT);
        functionLabel.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(functionLabel);

        JCheckBox functionalCheckBox = new JCheckBox("", transition.fpnExtension.isFunctional());
        functionalCheckBox.setBounds(columnB_posX, columnB_Y += 20, 30, 20);
        functionalCheckBox.setSelected(((Transition) element).fpnExtension.isFunctional());

        functionalCheckBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            ((Transition) element).fpnExtension.setFunctional(box.isSelected());
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(functionalCheckBox);

        JButton functionsEditorButton = new JButton(Tools.getResIcon32("/icons/functionsWindow/functionsIcon.png"));
        functionsEditorButton.setName("Functions editor");
        functionsEditorButton.setText(lang.getText("HDWT_entry094trans"));
        functionsEditorButton.setMargin(new Insets(0, 0, 0, 0));
        functionsEditorButton.setBounds(columnA_posX + 125, columnA_Y - 16, 110, 32);
        functionsEditorButton.addActionListener(actionEvent -> new HolmesFunctionsBuilder((Transition) element));
        components.add(functionsEditorButton);

        // CLASSICAL TRANSITION WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry095trans"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

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

        // CLASSICAL TRANSITION TEXT LOCATION CHANGE BUTTON
        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry096trans"));
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
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
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
        //components.add(debugModeLabel1);

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

                    ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
                    if (id >= 0 && id < transitions.size()) {

                        Transition t1 = (Transition) element;
                        Transition t2 = transitions.get(id);
                        if (transitions.indexOf(t1) == id)
                            return;

                        int pos1 = overlord.getWorkspace().getProject().getNodes().indexOf(t1);
                        int pos2 = overlord.getWorkspace().getProject().getNodes().indexOf(t2);
                        Collections.swap(overlord.getWorkspace().getProject().getNodes(), pos1, pos2);
                        overlord.log("Swapping transitions " + t1.getName() + " and " + t2.getName() + " successfull.", "text", true);

                        WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                        //ElementLocation loc1st = ((Transition)element).getElementLocations().get(0);
                        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation); //zaznacz element
                    }
                } catch (Exception ee) {
                    overlord.log(lang.getText("LOGentry00572exception")+"\n"+ee.getMessage(), "error", true);
                }
            }
        });
        //components.add(debugChangeID);

        JButton aaa = new JButton();
        aaa.setName("ChangeID");
        aaa.setText("Change ID");
        aaa.setMargin(new Insets(0, 0, 0, 0));
        aaa.setBounds(columnB_posX + 70, columnB_Y, 80, 20);
        aaa.addActionListener(actionEvent -> {
        });
        //components.add(aaa);

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
    //*********************************     KOLOROWA     ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda odpowiedzialna za tworzenie podokna właściwości klikniętej tranzycji.
     * @param transition (<b>Transition</b>) obiekt tranzycji sieci.
     * @param location   (<b>ElementLocation</b>) - lokalizacja tranzycji.
     */
    @SuppressWarnings("UnusedAssignment")
    private void createColorTransitionSubWindow(Transition transition, ElementLocation location) {
        int columnA_posX = 10;
        int columnB_posX = 100;
        int columnA_Y = 0;
        int columnB_Y = 0;
        int colACompLength = 70;
        int colBCompLength = 200;

        mode = CTRANSITION;
        elementLocation = location;
        initiateContainers();
        element = transition;
        Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);

        JLabel colorLabel = new JLabel(lang.getText("HDWT_entry097Ctrans"), JLabel.LEFT);
        colorLabel.setBounds(columnA_posX, columnA_Y += 10, 220, 20);
        components.add(colorLabel);
        columnB_Y += 10;

        // COLOR-TRANSITION ID:
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

        // COLOR-TRANSITION NAME:
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry098Ctrans"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00573exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // COLOR-TRANSITION KOMENTARZE WIERZCHOŁKA:
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry099Ctrans"), JLabel.LEFT);
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

        // COLOR-TRANSITION SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry100Ctrans"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        // COLOR-TRANSITION ZOOM:
        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry101Ctrans"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // COLOR-TRANSITION PORTAL
        JLabel portalLabel = new JLabel(lang.getText("HDWT_entry102Ctrans"), JLabel.LEFT);
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
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry102Ctrans_1"), lang.getText("HDWT_entry102Ctrans_1t"),
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(portalBox);

        // COLOR-TRANSITION LOKALIZACJA:
        JLabel locLabel = new JLabel(lang.getText("HDWT_entry103Ctrans"), JLabel.LEFT);
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


        // COLOR-TRANSITION WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry104Ctrans"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

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

        // COLOR-TRANSITION TEXT LOCATION BUTTON
        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry105Ctrans"));
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
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocChangeButton);

        // COLOR-TRANSITION TOKENS
        JLabel reqLabel = new JLabel(lang.getText("HDWT_entry106Ctrans"), JLabel.LEFT);
        reqLabel.setBounds(columnA_posX, columnA_Y += 50, 220, 20);
        columnB_Y += 76;
        components.add(reqLabel);

        JLabel reqT0Label = new JLabel(lang.getText("HDWT_entry107Ctrans"), JLabel.LEFT);
        reqT0Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(reqT0Label);

        SpinnerModel weightT0SpinnerModel = new SpinnerNumberModel(((TransitionColored) transition).getRequiredColoredTokens(0), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT0Spinner = new JSpinner(weightT0SpinnerModel);
        weightT0Spinner.setBounds(columnB_posX - 35, columnB_Y += 20, 65, 20);
        weightT0Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 0);
        });
        components.add(weightT0Spinner);

        JLabel reqT3Label = new JLabel(lang.getText("HDWT_entry108Ctrans"), JLabel.LEFT);
        reqT3Label.setBounds(columnB_posX + 40, columnB_Y, 80, 20);
        components.add(reqT3Label);

        SpinnerModel weightT3SpinnerModel = new SpinnerNumberModel(((TransitionColored) transition).getRequiredColoredTokens(3), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT3Spinner = new JSpinner(weightT3SpinnerModel);
        weightT3Spinner.setBounds(columnB_posX + 100, columnB_Y, 65, 20);
        weightT3Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 3);
        });
        components.add(weightT3Spinner);

        JLabel reqT1Label = new JLabel(lang.getText("HDWT_entry109Ctrans"), JLabel.LEFT);
        reqT1Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(reqT1Label);

        SpinnerModel weightT1SpinnerModel = new SpinnerNumberModel(((TransitionColored) transition).getRequiredColoredTokens(1), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT1Spinner = new JSpinner(weightT1SpinnerModel);
        weightT1Spinner.setBounds(columnB_posX - 35, columnB_Y += 20, 65, 20);
        weightT1Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 1);
        });
        components.add(weightT1Spinner);

        JLabel reqT4Label = new JLabel(lang.getText("HDWT_entry110Ctrans"), JLabel.LEFT);
        reqT4Label.setBounds(columnB_posX + 40, columnB_Y, 80, 20);
        components.add(reqT4Label);

        SpinnerModel weightT4SpinnerModel = new SpinnerNumberModel(((TransitionColored) transition).getRequiredColoredTokens(4), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT4Spinner = new JSpinner(weightT4SpinnerModel);
        weightT4Spinner.setBounds(columnB_posX + 100, columnB_Y, 65, 20);
        weightT4Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 4);
        });
        components.add(weightT4Spinner);

        JLabel reqT2Label = new JLabel(lang.getText("HDWT_entry111Ctrans"), JLabel.LEFT);
        reqT2Label.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(reqT2Label);

        SpinnerModel weightT2SpinnerModel = new SpinnerNumberModel(((TransitionColored) transition).getRequiredColoredTokens(2), 0, Integer.MAX_VALUE, 1);
        JSpinner weightT2Spinner = new JSpinner(weightT2SpinnerModel);
        weightT2Spinner.setBounds(columnB_posX - 35, columnB_Y += 20, 65, 20);
        weightT2Spinner.addChangeListener(e -> {
            int tokenz = (int) ((JSpinner) e.getSource()).getValue();
            setActivationWeight(tokenz, (Transition) element, 2);
        });
        components.add(weightT2Spinner);

        JLabel reqT5Label = new JLabel(lang.getText("HDWT_entry112Ctrans"), JLabel.LEFT);
        reqT5Label.setBounds(columnB_posX + 40, columnB_Y, 80, 20);
        components.add(reqT5Label);

        SpinnerModel weightT5SpinnerModel = new SpinnerNumberModel(((TransitionColored) transition).getRequiredColoredTokens(5), 0, Integer.MAX_VALUE, 1);
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
    //*********************************     TRANZYCJA    ***********************************
    //*********************************        SPN       ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda odpowiedzialna za tworzenie podokna właściwości klikniętej tranzycji.
     * @param transition (<b>Transition</b>) obiekt tranzycji sieci.
     * @param location   (<b>ElementLocation</b>) - lokalizacja tranzycji.
     */
    @SuppressWarnings("UnusedAssignment")
    private void createSPNTransitionSubWindow(Transition transition, ElementLocation location) {
        //[2022-07-06] na razie czysto na bazie zwykłej tranzycji, copy pase

        int columnA_posX = 10;
        int columnB_posX = 100;
        int columnA_Y = 0;
        int columnB_Y = 0;
        int colACompLength = 70;
        int colBCompLength = 200;

        mode = SPN;
        elementLocation = location;
        initiateContainers();
        element = transition;
        Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);

        // SPN TRANSITION ID:
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

        // SPN TRANSITION NAME:
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry113SPNtrans"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00574exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // SPN TRANSITION KOMENTARZE WIERZCHOŁKA:
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry114SPNtrans"), JLabel.LEFT);
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

        // SPN TRANSITION CHANGE TYPE:
        JLabel changeTypeLabel = new JLabel(lang.getText("HDWT_entry115SPNtrans"), JLabel.LEFT);
        changeTypeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(changeTypeLabel);

        classicalTransitionCheckBox = new JCheckBox("PN");
        classicalTransitionCheckBox.setBounds(columnB_posX, columnB_Y += 20, 60, 20);
        classicalTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.PN);
        classicalTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.PN);
                doNotUpdate = true;
                timeTransitionCheckBox.setSelected(false);
                stochasticTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(classicalTransitionCheckBox);

        timeTransitionCheckBox = new JCheckBox("TPN");
        timeTransitionCheckBox.setBounds(columnB_posX + 70, columnB_Y, 60, 20);
        timeTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.TPN);
        timeTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.TPN);
                doNotUpdate = true;
                classicalTransitionCheckBox.setSelected(false);
                stochasticTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(timeTransitionCheckBox);

        stochasticTransitionCheckBox = new JCheckBox("SPN");
        stochasticTransitionCheckBox.setBounds(columnB_posX + 130, columnB_Y, 60, 20);
        stochasticTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.SPN);
        stochasticTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.SPN);
                doNotUpdate = true;
                classicalTransitionCheckBox.setSelected(false);
                timeTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(stochasticTransitionCheckBox);

        JLabel frLabel = new JLabel(lang.getText("HDWT_entry116SPNtrans"), JLabel.LEFT);
        frLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(frLabel);

        JTextArea frField = new JTextArea("" + ((Transition) element).spnExtension.getFiringRate());
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

                    SPNtransitionData xxx = overlord.getWorkspace().getProject().accessFiringRatesManager()
                            .getCurrentSPNdataVector().accessVector().get(gID);
                    ((Transition) element).spnExtension.setFiringRate(newVal);
                    xxx.ST_function = newFR;
                } catch (Exception ee) {
                    overlord.log(lang.getText("LOGentry00575exception")+"\n"+ee.getMessage(), "error", true);
                }
            }
        });
        components.add(frField);

        // SPN TRANSITION SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry117SPNtrans"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        //ZOOM:
        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry118SPNtrans"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        //LOKALIZACJA:
        JLabel locLabel = new JLabel(lang.getText("HDWT_entry119SPNtrans"), JLabel.LEFT);
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

        // SPN TRANSITION PORTAL
        JLabel portalLabel = new JLabel(lang.getText("HDWT_entry120SPNtrans"), JLabel.LEFT);
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
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry120SPNtrans_1"), lang.getText("HDWT_entry120SPNtrans_1t"),
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(portalBox);

        // SPN TRANSITION FUNKCYJNOŚĆ
        JLabel functionLabel = new JLabel(lang.getText("HDWT_entry121SPNtrans"), JLabel.LEFT);
        functionLabel.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(functionLabel);

        JCheckBox functionalCheckBox = new JCheckBox("", transition.fpnExtension.isFunctional());
        functionalCheckBox.setBounds(columnB_posX, columnB_Y += 20, 30, 20);
        functionalCheckBox.setSelected(((Transition) element).fpnExtension.isFunctional());

        functionalCheckBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            ((Transition) element).fpnExtension.setFunctional(box.isSelected());
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(functionalCheckBox);

        JButton functionsEditorButton = new JButton(Tools.getResIcon32("/icons/functionsWindow/functionsIcon.png"));
        functionsEditorButton.setName("Functions editor");
        functionsEditorButton.setText(lang.getText("HDWT_entry122SPNtrans"));
        functionsEditorButton.setMargin(new Insets(0, 0, 0, 0));
        functionsEditorButton.setBounds(columnA_posX + 125, columnA_Y - 16, 110, 32);
        functionsEditorButton.addActionListener(actionEvent -> new HolmesFunctionsBuilder((Transition) element));
        components.add(functionsEditorButton);

        // SPN TRANSITION WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry123SPNtrans"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

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

        // SPN TRANSITION TEXT LOCATION BUTTON
        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry124SPNtrans"));
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
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocChangeButton);

        /*
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

                    ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
                    if (id >= 0 && id < transitions.size()) {

                        Transition t1 = (Transition) element;
                        Transition t2 = transitions.get(id);
                        if (transitions.indexOf(t1) == id)
                            return;

                        int pos1 = overlord.getWorkspace().getProject().getNodes().indexOf(t1);
                        int pos2 = overlord.getWorkspace().getProject().getNodes().indexOf(t2);
                        Collections.swap(overlord.getWorkspace().getProject().getNodes(), pos1, pos2);
                        overlord.log("Swapping transitions " + t1.getName() + " and " + t2.getName() + " successfull.", "text", true);

                        WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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
        components.add(aaa); */

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
     * @param location   ElementLocation - lokalizacja tranzycji
     */
    @SuppressWarnings("UnusedAssignment")
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

        // TIMETRANSITION ID
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

        // TIME TRANSITION NAME
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry125TPNtrans"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00576exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // TIME TRANSITION COMMENT
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry126TPNtrans"), JLabel.LEFT);
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

        // TIME TRANSITION CHANGE TYPE:
        JLabel changeTypeLabel = new JLabel(lang.getText("HDWT_entry127TPNtrans"), JLabel.LEFT);
        changeTypeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(changeTypeLabel);

        classicalTransitionCheckBox = new JCheckBox("PN");
        classicalTransitionCheckBox.setBounds(columnB_posX, columnB_Y += 20, 60, 20);
        classicalTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.PN);
        classicalTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.PN);
                doNotUpdate = true;
                timeTransitionCheckBox.setSelected(false);
                stochasticTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(classicalTransitionCheckBox);

        timeTransitionCheckBox = new JCheckBox("TPN");
        timeTransitionCheckBox.setBounds(columnB_posX + 70, columnB_Y, 60, 20);
        timeTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.TPN);
        timeTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.TPN);
                doNotUpdate = true;
                classicalTransitionCheckBox.setSelected(false);
                stochasticTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(timeTransitionCheckBox);

        stochasticTransitionCheckBox = new JCheckBox("SPN");
        stochasticTransitionCheckBox.setBounds(columnB_posX + 130, columnB_Y, 60, 20);
        stochasticTransitionCheckBox.setSelected(((Transition) element).getTransType() == TransitionType.SPN);
        stochasticTransitionCheckBox.addItemListener(e -> {
            if (doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                ((Transition) element).setTransType(TransitionType.SPN);
                doNotUpdate = true;
                classicalTransitionCheckBox.setSelected(false);
                timeTransitionCheckBox.setSelected(false);
                doNotUpdate = false;
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
            }
        });
        components.add(stochasticTransitionCheckBox);

        // TIME TRANSITION EFT / LFT TIMES:
        JLabel minMaxLabel = new JLabel("EFT / LFT:", JLabel.LEFT);
        minMaxLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(minMaxLabel);
        JFormattedTextField minTimeField = new JFormattedTextField();
        minTimeField.setValue(transition.timeExtension.getEFT());
        minTimeField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00577exception")+"\n"+ex.getMessage(), "error", true);
            }
            double min = (double) field.getValue();
            setMinFireTime(min);
            overlord.markNetChange();
        });

        JFormattedTextField maxTimeField = new JFormattedTextField();
        maxTimeField.setValue(transition.timeExtension.getLFT());
        maxTimeField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00578exception")+"\n"+ex.getMessage(), "error", true);
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
        JLabel durationLabel = new JLabel(lang.getText("HDWT_entry128TPNtrans"), JLabel.LEFT);
        durationLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(durationLabel);
        JFormattedTextField durationField = new JFormattedTextField();
        durationField.setValue(transition.timeExtension.getDPNduration());
        durationField.setBounds(columnA_posX + 90, columnB_Y += 20, 90, 20);
        durationField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
                double time = (double) field.getValue();
                setDurationTime(time);
                overlord.markNetChange();
            } catch (Exception ex) {
                overlord.log(lang.getText("LOGentry00579exception")+"\n"+ex.getMessage(), "error", true);
            }
        });
        components.add(durationField);

        //columnA_Y+=40;
        JCheckBox tpnBox = new JCheckBox(lang.getText("HDWT_entry129TPNtrans"), transition.timeExtension.isTPN());
        tpnBox.setBounds(columnB_posX - 5, columnB_Y += 20, 100, 20);
        tpnBox.setEnabled(true);
        tpnBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            setTPNstatus(box.isSelected());

            overlord.markNetChange();
        });
        components.add(tpnBox);

        columnA_Y += 20;
        JCheckBox dpnBox = new JCheckBox(lang.getText("HDWT_entry130TPNtrans"), transition.timeExtension.isDPN());
        dpnBox.setBounds(columnB_posX + 100, columnB_Y, 100, 20);
        dpnBox.setEnabled(true);
        dpnBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            setDPNstatus(box.isSelected());

            overlord.markNetChange();
        });
        components.add(dpnBox);

        // TIME TRANSITION SHEET ID
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

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry131TPNtrans"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, 100, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry132TPNtrans"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // T-TRANSITION LOCATION:
        JLabel comLabel2 = new JLabel(lang.getText("HDWT_entry133TPNtrans"), JLabel.LEFT);
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
        JLabel portalLabel = new JLabel(lang.getText("HDWT_entry134TPNtrans"), JLabel.LEFT);
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
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry134TPNtrans_1"), lang.getText("HDWT_entry134TPNtrans_1t"),
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
        });
        components.add(portalBox);

        // TIME TRANSITION FUNKCYJNOŚĆ
        JLabel functionLabel = new JLabel(lang.getText("HDWT_entry135TPNtrans"), JLabel.LEFT);
        functionLabel.setBounds(columnA_posX, columnA_Y += 20, 80, 20);
        components.add(functionLabel);

        JCheckBox functionalCheckBox = new JCheckBox("", transition.fpnExtension.isFunctional());
        functionalCheckBox.setBounds(columnB_posX, columnB_Y += 20, 30, 20);
        functionalCheckBox.setSelected(((Transition) element).fpnExtension.isFunctional());

        functionalCheckBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            ((Transition) element).fpnExtension.setFunctional(box.isSelected());

            overlord.markNetChange();
        });
        components.add(functionalCheckBox);

        JButton functionsEditorButton = new JButton(Tools.getResIcon32("/icons/functionsWindow/functionsIcon.png"));
        functionsEditorButton.setName("Functions editor");
        functionsEditorButton.setText(lang.getText("HDWT_entry136TPNtrans"));
        functionsEditorButton.setMargin(new Insets(0, 0, 0, 0));
        functionsEditorButton.setBounds(columnA_posX + 125, columnA_Y - 16, 110, 32);
        functionsEditorButton.addActionListener(actionEvent -> new HolmesFunctionsBuilder((Transition) element));
        components.add(functionsEditorButton);

        // TIME TRANSITION WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry137TPNtrans"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

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

        // TIME TRANSITION LOCATION CHANGE
        JButton nameLocChangeButton = new JButton(Tools.getResIcon22("/icons/changeNameLocation.png"));
        nameLocChangeButton.setName("LocNameChanger");
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry138TPNtrans"));
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
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
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
    //*********************************    TRANZYCJA     ***********************************
    //*********************************      _XTPN       ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda odpowiedzialna za utworzenie podokna właściwości tranzycji XTPN.
     * @param transition (<b>Transition</b>) obiekt tranzycji czasowej.
     * @param location   (<b>ElementLocation</b>) lokalizacja tranzycji.
     */
    private void createXTPNTransitionSubWindow(final TransitionXTPN transition, ElementLocation location) {
        int columnA_posX = 10;
        int columnB_posX = 100;
        int columnA_Y = 0;
        int columnB_Y = 0;
        int colACompLength = 70;
        int colBCompLength = 200;

        mode = XTPN_TRANS;
        elementLocation = location;
        initiateContainers(); //!!!
        element = transition;
        Font normalFont = new Font(Font.DIALOG, Font.PLAIN, 12);

        // XTPN ID
        JLabel idLabel = new JLabel("ID XTPN:", JLabel.LEFT);
        idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
        components.add(idLabel);

        int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transition);
        JLabel idLabel2 = new JLabel(Integer.toString(gID));
        idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
        idLabel2.setFont(normalFont);
        components.add(idLabel2);

        JLabel idLabel3 = new JLabel("globalID:");
        idLabel3.setBounds(columnB_posX + 55, columnA_Y, 50, 20);
        components.add(idLabel3);
        JLabel idLabel4 = new JLabel(transition.getID() + "");
        idLabel4.setBounds(columnB_posX + 120, columnB_Y, 50, 20);
        idLabel4.setFont(normalFont);
        components.add(idLabel4);

        // XTPN transition name
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry139XTPNtrans"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00580exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // XTPN transition comment
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry140XTPNtrans"), JLabel.LEFT);
        comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
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


        // XTPN transition włączanie lub wyłączanie funkcji alfa i beta
        columnA_Y += 20;
        JLabel changeTypeLabel = new JLabel(lang.getText("HDWT_entry141XTPNtrans"), JLabel.LEFT);
        changeTypeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(changeTypeLabel);

        // XTPN transition przycisk Alfa mode ON/OFF
        HolmesRoundedButton buttonAlfaMode = new HolmesRoundedButton(lang.getText("HDWT_entry142XTPNtransA_ON")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        //JButton buttonAlfaMode = new JButton("Alfa: ON");
        buttonAlfaMode.setName("alphaModeButton");
        buttonAlfaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonAlfaMode.setBounds(columnB_posX, columnB_Y += 20, 65, 35);
        if (transition.isAlphaModeActive()) {
            buttonAlfaMode.setNewText(lang.getText("HDWT_entry142XTPNtransA_ON"));
            buttonAlfaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonAlfaMode.setNewText(lang.getText("HDWT_entry142XTPNtransA_OFF"));
            buttonAlfaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonAlfaMode.addActionListener(e -> {
            if (doNotUpdate)
                return;

            doNotUpdate = true;
            SharedActionsXTPN.access().buttonAlphaSwitchMode(e, transition, null, tauVisibilityButton, buttonClassicMode, alphaMaxTextField, elementLocation);
            doNotUpdate = false;
        });
        components.add(buttonAlfaMode);

        // XTPN transition przycisk Beta ON/OFF
        HolmesRoundedButton buttonBetaMode = new HolmesRoundedButton(lang.getText("HDWT_entry143XTPNtransB_ON")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        //JButton buttonBetaMode = new JButton("Beta: ON");
        buttonBetaMode.setName("betaModeButton");
        buttonBetaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonBetaMode.setBounds(columnB_posX + 65, columnB_Y, 65, 35);
        if (transition.isBetaModeActive()) {
            buttonBetaMode.setNewText(lang.getText("HDWT_entry143XTPNtransB_ON"));
            buttonBetaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonBetaMode.setNewText(lang.getText("HDWT_entry143XTPNtransB_OFF"));
            buttonBetaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonBetaMode.addActionListener(e -> {
            if (doNotUpdate)
                return;

            doNotUpdate = true;
            SharedActionsXTPN.access().buttonBetaSwitchMode(e, transition, null, tauVisibilityButton, buttonClassicMode, betaMaxTextField, elementLocation);
            doNotUpdate = false;
        });
        components.add(buttonBetaMode);

        // XTPN transition przycisk XTPN ON/OFF
        buttonClassicMode = new HolmesRoundedButton(lang.getText("HDWT_entry144XTPNtrans")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        //buttonClassicMode = new JButton("<html><center>PN<b>trans.</center></html>");
        buttonClassicMode.setName("classXTPNswitchButton");
        buttonClassicMode.setMargin(new Insets(0, 0, 0, 0));
        buttonClassicMode.setBounds(columnB_posX + 130, columnB_Y, 65, 35);
        if (!transition.isAlphaModeActive() && !transition.isBetaModeActive()) {
            buttonClassicMode.setNewText("<html>Classical<html>");
            buttonClassicMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        } else { //gdy jeden z trybów włączony
            buttonClassicMode.setNewText("<html>XTPN<html>");
            buttonClassicMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        }
        buttonClassicMode.addActionListener(e -> {
            if (doNotUpdate)
                return;

            doNotUpdate = true;
            SharedActionsXTPN.access().buttonTransitionToXTPN_classicSwitchMode(e, transition, null, alphaMaxTextField, betaMaxTextField, elementLocation);
            doNotUpdate = false;
        });
        components.add(buttonClassicMode);

        // XTPN transition alpha values visibility
        alfaVisibilityButton = new HolmesRoundedButton("<html>\u03B1:visible</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        alfaVisibilityButton.setName("alphaVisButton");
        alfaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        alfaVisibilityButton.setBounds(columnB_posX, columnB_Y += 37, 65, 35);
        alfaVisibilityButton.setFocusPainted(false);
        if (transition.isAlphaModeActive()) {
            if (transition.isAlphaRangeVisible()) {
                alfaVisibilityButton.setNewText(lang.getText("HDWT_entry145XTPNtransA_vis"));
                alfaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                alfaVisibilityButton.setNewText(lang.getText("HDWT_entry145XTPNtransA_invis"));
                alfaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            if (transition.isAlphaRangeVisible()) {
                alfaVisibilityButton.setNewText(lang.getText("HDWT_entry145XTPNtransA_vis"));
            } else {
                alfaVisibilityButton.setNewText(lang.getText("HDWT_entry145XTPNtransA_invis"));
            }
            alfaVisibilityButton.setEnabled(false);
        }

        alfaVisibilityButton.addActionListener(e -> {
            if (doNotUpdate)
                return;

            alphaLocChangeMode = SharedActionsXTPN.access().alphaVisButtonAction(e, transition, alphaLocChangeButton, alphaLocChangeMode, elementLocation);
        });
        components.add(alfaVisibilityButton);

        // XTPN transition beta values visibility
        betaVisibilityButton = new HolmesRoundedButton(lang.getText("HDWT_entry146XTPNtransB_vis")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        betaVisibilityButton.setName("betaVisButton1");
        betaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        betaVisibilityButton.setBounds(columnB_posX + 65, columnB_Y, 65, 35);
        betaVisibilityButton.setFocusPainted(false);
        if (transition.isBetaModeActive()) {
            if (transition.isBetaRangeVisible()) {
                betaVisibilityButton.setNewText(lang.getText("HDWT_entry146XTPNtransB_vis"));
                betaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                betaVisibilityButton.setNewText(lang.getText("HDWT_entry146XTPNtransB_invis"));
                betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            if (transition.isBetaRangeVisible()) {
                betaVisibilityButton.setNewText(lang.getText("HDWT_entry146XTPNtransB_vis"));
            } else {
                betaVisibilityButton.setNewText(lang.getText("HDWT_entry146XTPNtransB_invis"));
            }
            betaVisibilityButton.setEnabled(false);
        }
        betaVisibilityButton.addActionListener(e -> {
            if (doNotUpdate)
                return;

            betaLocChangeMode = SharedActionsXTPN.access().betaVisButtonAction(e, transition, betaLocChangeButton, betaLocChangeMode, elementLocation);
        });
        components.add(betaVisibilityButton);

        // XTPN-transition tau values visibility
        tauVisibilityButton = new HolmesRoundedButton(lang.getText("HDWT_entry147XTPNtransT_vis")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        tauVisibilityButton.setName("tauVisButton");
        tauVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        tauVisibilityButton.setBounds(columnB_posX + 130, columnB_Y, 65, 35);
        tauVisibilityButton.setFocusPainted(false);
        if (transition.isAlphaModeActive() || transition.isBetaModeActive()) {
            if (transition.isTauTimerVisible()) {
                tauVisibilityButton.setNewText(lang.getText("HDWT_entry147XTPNtransT_vis"));
                tauVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                tauVisibilityButton.setNewText(lang.getText("HDWT_entry147XTPNtransT_invis"));
                tauVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            if (transition.isTauTimerVisible()) {
                tauVisibilityButton.setNewText(lang.getText("HDWT_entry147XTPNtransT_vis"));
            } else {
                tauVisibilityButton.setNewText(lang.getText("HDWT_entry147XTPNtransT_invis"));
            }
            tauVisibilityButton.setEnabled(false);
        }
        tauVisibilityButton.addActionListener(e -> {
            if (doNotUpdate)
                return;

            tauLocChangeMode = SharedActionsXTPN.access().tauVisButtonAction(e, transition, tauLocChangeButton, tauLocChangeMode, elementLocation);
        });
        components.add(tauVisibilityButton);

        // XTPN-transition Alfa offset
        alphaLocChangeButton = new HolmesRoundedButton(lang.getText("HDWT_entry148XTPNtransA_off")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        alphaLocChangeButton.setName("alphaOffsetButton");
        alphaLocChangeButton.setToolTipText(lang.getText("HDWT_entry150XTPNtrans"));
        alphaLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        alphaLocChangeButton.setBounds(columnB_posX, columnB_Y += 37, 65, 35);
        alphaLocChangeButton.setFocusPainted(false);
        if (transition.isAlphaModeActive() && transition.isAlphaRangeVisible()) {
            if (alphaLocChangeMode) {
                alphaLocChangeButton.setNewText(lang.getText("HDWT_entry149XTPNtransB_changeLoc"));
                alphaLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            } else {
                alphaLocChangeButton.setNewText(lang.getText("HDWT_entry148XTPNtransA_off"));
                alphaLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
        } else {
            alphaLocChangeMode = false;
            alphaLocChangeButton.setEnabled(false);
        }
        alphaLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                if (!alphaLocChangeMode) {
                    alphaLocChangeButton.setNewText(lang.getText("HDWT_entry149XTPNtransB_changeLoc"));
                    alphaLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                    alphaLocChangeMode = true;
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.ALPHA);
                } else {
                    alphaLocChangeButton.setNewText(lang.getText("HDWT_entry148XTPNtransA_off"));
                    alphaLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                    alphaLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.ALPHA);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(alphaLocChangeButton);

        // XTPN-transition beta offset
        betaLocChangeButton = new HolmesRoundedButton(lang.getText("HDWT_entry152XTPNtransB_changeLoc")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        betaLocChangeButton.setName("betaOffsetButton");
        betaLocChangeButton.setToolTipText(lang.getText("HDWT_entry153XTPNtransB"));
        betaLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        betaLocChangeButton.setBounds(columnB_posX + 65, columnB_Y, 65, 35);
        betaLocChangeButton.setFocusPainted(false);
        if (transition.isBetaModeActive() && transition.isBetaRangeVisible()) {
            if (betaLocChangeMode) {
                betaLocChangeButton.setNewText(lang.getText("HDWT_entry152XTPNtransB_changeLoc"));
                betaLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            } else {
                betaLocChangeButton.setNewText(lang.getText("HDWT_entry154XTPNtransB_off"));
                betaLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
        } else {
            betaLocChangeMode = false;
            betaLocChangeButton.setEnabled(false);
        }
        betaLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                //JButton button_tmp = (JButton) actionEvent.getSource();
                if (!betaLocChangeMode) {
                    betaLocChangeButton.setNewText(lang.getText("HDWT_entry152XTPNtransB_changeLoc"));
                    betaLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                    betaLocChangeMode = true;
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.BETA);
                } else {
                    betaLocChangeButton.setNewText(lang.getText("HDWT_entry154XTPNtransB_off"));
                    betaLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                    betaLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.BETA);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(betaLocChangeButton);

        // XTPN-transition tau offset
        tauLocChangeButton = new HolmesRoundedButton(lang.getText("HDWT_entry155XTPNtransT_off")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        tauLocChangeButton.setName("tauOffsetButton");
        tauLocChangeButton.setToolTipText(lang.getText("HDWT_entry157XTPNtransT"));
        tauLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        tauLocChangeButton.setBounds(columnB_posX + 130, columnB_Y, 65, 35);
        tauLocChangeButton.setFocusPainted(false);
        if (transition.isAlphaModeActive() || transition.isBetaModeActive()) {
            if (tauLocChangeMode) {
                tauLocChangeButton.setNewText(lang.getText("HDWT_entry156XTPNtransT_changeLoc"));
                tauLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            } else {
                tauLocChangeButton.setNewText(lang.getText("HDWT_entry155XTPNtransT_off"));
                tauLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
        } else {
            tauLocChangeMode = false;
            tauLocChangeButton.setEnabled(false);
        }
        tauLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                //JButton button_tmp = (JButton) actionEvent.getSource();
                if (!tauLocChangeMode) {
                    tauLocChangeButton.setNewText(lang.getText("HDWT_entry156XTPNtransT_changeLoc"));
                    tauLocChangeButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                    tauLocChangeMode = true;
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.TAU);
                } else {
                    tauLocChangeButton.setNewText(lang.getText("HDWT_entry155XTPNtransT_off"));
                    tauLocChangeButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                    tauLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.TAU);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(tauLocChangeButton);

        // XTPN-transition Zakresy alfa:
        JLabel minMaxLabel = new JLabel(lang.getText("HDWT_entry158XTPNtrans"), JLabel.LEFT);
        minMaxLabel.setBounds(columnA_posX, columnA_Y += 105, colACompLength + 10, 20);
        components.add(minMaxLabel);

        // format danych alfa i beta: do 6 miejsc po przecinku
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(6);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Double example = 3.14;

        // XTPN-transition alfaMin value
        alphaMinTextField = new JFormattedTextField(formatter);
        alphaMinTextField.setValue(example);
        alphaMinTextField.setValue(transition.getAlphaMinValue());
        alphaMinTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00581exception")+"\n"+ex.getMessage(), "error", true);
            }

            double min = Double.parseDouble("" + field.getValue());
            SharedActionsXTPN.access().setAlfaMinTime(min, transition, elementLocation);

            doNotUpdate = true;
            alphaMaxTextField.setValue(transition.getAlphaMaxValue());
            field.setValue(transition.getAlphaMinValue());
            doNotUpdate = false;
            overlord.markNetChange();

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });

        // alfaMax value
        alphaMaxTextField = new JFormattedTextField(formatter);
        alphaMaxTextField.setValue(example);
        alphaMaxTextField.setValue(transition.getAlphaMaxValue());
        alphaMaxTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00582exception")+"\n"+ex.getMessage(), "error", true);
            }

            double max = Double.parseDouble("" + field.getValue());
            SharedActionsXTPN.access().setAlfaMaxTime(max, transition, elementLocation);

            doNotUpdate = true;
            alphaMinTextField.setValue(transition.getAlphaMinValue());
            field.setValue(transition.getAlphaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });

        if (!transition.isAlphaModeActive()) {
            alphaMinTextField.setEnabled(false);
            alphaMaxTextField.setEnabled(false);
        }

        alphaMinTextField.setBounds(columnB_posX, columnB_Y += 36, 80, 20);
        components.add(alphaMinTextField);
        JLabel slash1 = new JLabel(" / ", JLabel.LEFT);
        slash1.setBounds(columnB_posX + 85, columnB_Y, 15, 20);
        components.add(slash1);
        alphaMaxTextField.setBounds(columnB_posX + 100, columnB_Y, 80, 20);
        components.add(alphaMaxTextField);

        // XTPN-transition zakresy beta:
        JLabel betaLabel = new JLabel(lang.getText("HDWT_entry159XTPNtrans"), JLabel.LEFT);
        betaLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength + 10, 20);
        components.add(betaLabel);

        // XTPN-transition betaMin value
        betaMinTextField = new JFormattedTextField(formatter);
        betaMinTextField.setValue(example);
        betaMinTextField.setValue(transition.getBetaMinValue());
        betaMinTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00583exception")+"\n"+ex.getMessage(), "error", true);
            }

            double min = Double.parseDouble("" + field.getValue());
            SharedActionsXTPN.access().setBetaMinTime(min, transition, elementLocation);

            doNotUpdate = true;
            field.setValue(transition.getBetaMinValue());
            betaMaxTextField.setValue(transition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });

        // XTPN-transition betaMax value
        betaMaxTextField = new JFormattedTextField(formatter);
        betaMaxTextField.setValue(example);
        betaMaxTextField.setValue(transition.getBetaMaxValue());
        betaMaxTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00584exception")+"\n"+ex.getMessage(), "error", true);
            }
            double max = Double.parseDouble("" + field.getValue());
            SharedActionsXTPN.access().setBetaMaxTime(max, transition, elementLocation);

            doNotUpdate = true;
            betaMinTextField.setValue(transition.getBetaMinValue());
            field.setValue(transition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });

        if (!transition.isBetaModeActive()) {
            betaMinTextField.setEnabled(false);
            betaMaxTextField.setEnabled(false);
        }

        betaMinTextField.setBounds(columnB_posX, columnB_Y += 20, 80, 20);
        components.add(betaMinTextField);
        JLabel slash2 = new JLabel(" / ", JLabel.LEFT);
        slash2.setBounds(columnB_posX + 85, columnB_Y, 15, 20);
        components.add(slash2);
        betaMaxTextField.setBounds(columnB_posX + 100, columnB_Y, 80, 20);
        components.add(betaMaxTextField);

        //mass-action for XTPN transition
        JCheckBox makCheckBox = new JCheckBox(lang.getText("HDWT_entry160XTPNtrans"));
        makCheckBox.setBounds(columnA_posX - 5, columnA_Y += 25, 150, 20);
        makCheckBox.setSelected(transition.isMassActionKineticsActiveXTPN());
        makCheckBox.addActionListener(actionEvent -> {
            if (doNotUpdate)
                return;

            transition.setMassActionKineticsXTPNstatus(makCheckBox.isSelected());
        });
        components.add(makCheckBox);

        // XTPM immediate classical-XTPN
        JCheckBox immediateCheckBox = new JCheckBox(lang.getText("HDWT_entry161XTPNtrans"));
        immediateCheckBox.setBounds(columnA_posX + 150, columnA_Y, 100, 20);
        immediateCheckBox.setSelected(transition.isImmediateXTPN());
        immediateCheckBox.addActionListener(actionEvent -> {
            if (doNotUpdate)
                return;
            transition.setImmediateStatusXTPN(immediateCheckBox.isSelected());
        });
        components.add(immediateCheckBox);

        // XTPN-transition FUNKCYJNOŚĆ
        JCheckBox functionalCheckBox = new JCheckBox(lang.getText("HDWT_entry162XTPNtrans"), transition.fpnExtension.isFunctional());
        functionalCheckBox.setBounds(columnA_posX - 5, columnA_Y += 20, 140, 20);
        functionalCheckBox.setSelected(((Transition) element).fpnExtension.isFunctional());
        functionalCheckBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            ((Transition) element).fpnExtension.setFunctional(box.isSelected());
            overlord.markNetChange();
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        functionalCheckBox.setEnabled(false);
        components.add(functionalCheckBox);

        JButton functionsEditorButton = new JButton(Tools.getResIcon32("/icons/functionsWindow/functionsIcon.png"));
        functionsEditorButton.setName("Functions editor");
        functionsEditorButton.setText(lang.getText("HDWT_entry163XTPNtrans"));
        functionsEditorButton.setMargin(new Insets(0, 0, 0, 0));
        functionsEditorButton.setBounds(columnB_posX + 65, columnB_Y + 45, 110, 32);
        functionsEditorButton.addActionListener(actionEvent -> new HolmesFunctionsBuilder((Transition) element));
        functionsEditorButton.setEnabled(false);
        components.add(functionsEditorButton);

        // XTPN portal status
        JCheckBox portalBox = new JCheckBox(lang.getText("HDWT_entry164XTPNtrans"), transition.isPortal());
        portalBox.setBounds(columnA_posX - 5, columnA_Y += 20, 90, 20);
        portalBox.setSelected(((Transition) element).isPortal());
        portalBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            if (box.isSelected()) {
                makePortal();
            } else {
                if (((Transition) element).getElementLocations().size() > 1)
                    JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry164XTPNtrans_1"), lang.getText("HDWT_entry164XTPNtrans_1t"),
                            JOptionPane.INFORMATION_MESSAGE);
                else {
                    unPortal();
                    overlord.markNetChange();
                }
            }
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
        });
        components.add(portalBox);

        JLabel fractionLabel = new JLabel(lang.getText("HDWT_entry165XTPNtrans"), JLabel.LEFT);
        fractionLabel.setBounds(columnA_posX, columnA_Y += 20, 70, 20);
        components.add(fractionLabel);

        int fract = transition.getFraction_xTPN();
        SpinnerModel fractionSpinnerModel = new SpinnerNumberModel(fract, 0, 6, 1);
        JSpinner fractionSpinner = new JSpinner(fractionSpinnerModel);
        fractionSpinner.setBounds(columnB_posX, columnB_Y += 82, 40, 20);
        fractionSpinner.addChangeListener(e -> {
            int fraction = (int) ((JSpinner) e.getSource()).getValue();
            transition.setFraction_xTPN(fraction);
        });
        components.add(fractionSpinner);

        // XTPN-transition SHEET ID
        int sheetIndex = overlord.IDtoIndex(location.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        int xPos = location.getPosition().x;
        int width = graphPanel.getSize().width;
        int zoom = graphPanel.getZoom();
        int yPos = location.getPosition().y;
        int height = graphPanel.getSize().height;
        width = (int) (((double) 100 / (double) zoom) * width);
        height = (int) (((double) 100 / (double) zoom) * height);

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry166XTPNtrans"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 25, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 25, 100, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry167XTPNtrans"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // XTPN-transition lokalizacja:
        JLabel comLabel2 = new JLabel(lang.getText("HDWT_entry168XTPNtrans"), JLabel.LEFT);
        comLabel2.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(comLabel2);

        SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
        JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
        locationXSpinner.setBounds(columnB_posX, columnB_Y += 20, 60, 20);
        locationXSpinner.addChangeListener(e -> setX((int) ((JSpinner) e.getSource()).getValue()));

        JLabel labelCom = new JLabel(" , ");
        labelCom.setBounds(columnB_posX + 60, columnB_Y, 10, 20);

        SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
        JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
        locationYSpinner.setBounds(columnB_posX + 70, columnB_Y, 60, 20);
        locationYSpinner.addChangeListener(e -> setY((int) ((JSpinner) e.getSource()).getValue()));
        if (zoom != 100) {
            locationXSpinner.setEnabled(false);
            locationYSpinner.setEnabled(false);
        }

        components.add(locationXSpinner);
        components.add(labelCom);
        components.add(locationYSpinner);

        // XTPN-transition WSPÓŁRZĘDNE NAPISU:
        columnA_Y += 20;
        columnB_Y += 20;

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry169XTPNtrans"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = transition.getElementLocations().indexOf(location);
        int xNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

        nameLocationXSpinnerModel = new SpinnerNumberModel(xNameOffset, -99999, 99999, 1);
        nameLocationYSpinnerModel = new SpinnerNumberModel(yNameOffset, -99999, 99999, 1);

        JLabel locNameLabelX = new JLabel("x:", JLabel.LEFT);
        locNameLabelX.setBounds(columnA_posX + 90, columnB_Y, 20, 20);
        components.add(locNameLabelX);

        JSpinner nameLocationXSpinner = new JSpinner(nameLocationXSpinnerModel);
        nameLocationXSpinner.setBounds(columnA_posX + 105, columnB_Y, 45, 20);
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

        JLabel locNameLabelY = new JLabel("y:", JLabel.LEFT);
        locNameLabelY.setBounds(columnA_posX + 160, columnB_Y, 10, 20);
        components.add(locNameLabelY);

        JSpinner nameLocationYSpinner = new JSpinner(nameLocationYSpinnerModel);
        nameLocationYSpinner.setBounds(columnA_posX + 175, columnA_Y, 45, 20);
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

        // XTPN-transition zmiana lokalizacji napisu

        HolmesRoundedButton nameLocChangeButton = new HolmesRoundedButton(lang.getText("HDWT_entry170XTPNtransName_off")
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        nameLocChangeButton.setName("transNameOffsetButton");
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry171XTPNtransName"));
        nameLocChangeButton.setMargin(new Insets(0, 0, 0, 0));
        nameLocChangeButton.setBounds(columnB_posX + 131, columnA_Y - 15, 65, 35);
        nameLocChangeButton.setFocusPainted(false);
        nameLocChangeButton.addActionListener(new ActionListener() {
            // anonimowy action listener przyjmujący zmienne non-final (⌐■_■)
            private Transition trans_tmp;
            private ElementLocation el_tmp;

            public void actionPerformed(ActionEvent actionEvent) {
                HolmesRoundedButton button = (HolmesRoundedButton) actionEvent.getSource();

                if (!nameLocChangeMode) {
                    button.setNewText(lang.getText("HDWT_entry172XTPNtransName_changeLoc"));
                    button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                    nameLocChangeMode = true;
                    overlord.setNameLocationChangeMode(trans_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button.setNewText(lang.getText("HDWT_entry170XTPNtransName_off"));
                    button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
                }
            }

            private ActionListener yesWeCan(Transition transition, ElementLocation inLoc) {
                trans_tmp = transition;
                el_tmp = inLoc;
                return this;
            }
        }.yesWeCan(transition, location));
        components.add(nameLocChangeButton);

/*
        JButton S1L1Reset = new JButton("Reset");
        S1L1Reset.setFont(new Font("Work Sans", Font.PLAIN, 14));
        S1L1Reset.setForeground(new Color(90, 90, 90));
        S1L1Reset.setBackground(new Color(220, 208, 192));
        S1L1Reset.setOpaque(true);
        S1L1Reset.setBorderPainted(false);
        S1L1Reset.setBorder(new RoundedBorder(20));
        S1L1Reset.setBounds(43, 465, 100, 20);

        S1L1Reset.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                S1L1Reset.setForeground(new Color(0,0,0));

            }
            public void mouseExited(MouseEvent e) {
                S1L1Reset.setForeground(new Color(90,90,90));
            }
        });
        S1L1Reset.setMargin(new Insets(0, 0, 0, 0));
        S1L1Reset.setBounds(columnA_posX + 90, columnA_Y += 45, 150, 40);
        components.add(S1L1Reset);*/

        panel.setLayout(null);
        for (JComponent component : components) panel.add(component);

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
    @SuppressWarnings("UnusedAssignment")
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

        JLabel sheetRepresentedLabel = new JLabel(lang.getText("HDWT_entry173subnet"));
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
        JLabel nameLabel = new JLabel(lang.getText("HDWT_entry174subnet"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00584exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            changeName(newName);
            overlord.markNetChange();
        });
        components.add(nameField);

        // T-TRANSITION COMMENT
        JLabel comLabel = new JLabel(lang.getText("HDWT_entry175subnet"), JLabel.LEFT);
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
        // ВНИМАНИЕ!!!
        JRadioButton subnetTButton = new JRadioButton(lang.getText("HDWT_entry176subnet"));
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


        JRadioButton subnetPButton = new JRadioButton(lang.getText("HDWT_entry177subnet"));
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

        JLabel sheetLabel = new JLabel(lang.getText("HDWT_entry178subnet"), JLabel.LEFT);
        sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(sheetLabel);
        JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
        sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, 100, 20);
        sheetIdLabel.setFont(normalFont);
        components.add(sheetIdLabel);

        JLabel zoomLabel = new JLabel(lang.getText("HDWT_entry179subnet"));
        zoomLabel.setBounds(columnB_posX + 30, columnB_Y, 50, 20);
        components.add(zoomLabel);
        JLabel zoomLabel2 = new JLabel("" + zoom);
        zoomLabel2.setBounds(columnB_posX + 70, columnB_Y, colBCompLength, 20);
        zoomLabel2.setFont(normalFont);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // T-TRANSITION LOCATION:
        JLabel comLabel2 = new JLabel(lang.getText("HDWT_entry180subnet"), JLabel.LEFT);
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

        JLabel locNameLabel = new JLabel(lang.getText("HDWT_entry181subnet"), JLabel.LEFT);
        locNameLabel.setBounds(columnA_posX, columnA_Y, colACompLength + 10, 20);
        components.add(locNameLabel);

        int locationIndex = metaNode.getElementLocations().indexOf(location);
        int xNameOffset = metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().x;
        int yNameOffset = metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).get(locationIndex).getPosition().y;

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
        nameLocChangeButton.setToolTipText(lang.getText("HDWT_entry182subnet"));
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
                    overlord.setNameLocationChangeMode(meta_tmp, el_tmp, GUIManager.locationMoveType.NAME);
                } else {
                    button_tmp.setIcon(Tools.getResIcon22("/icons/changeNameLocation.png"));
                    nameLocChangeMode = false;
                    overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
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
    @SuppressWarnings("UnusedAssignment")
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
        if (arc.arcXTPNbox.isXTPN())
            mode = XARC;

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
        JLabel commLabel = new JLabel(lang.getText("HDWT_entry183arc"), JLabel.LEFT);
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
                JLabel weightLabel = new JLabel(lang.getText("HDWT_entry184arc") + type1 + ")", JLabel.LEFT);
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

                JLabel weightLabel2 = new JLabel(lang.getText("HDWT_entry184arc") + type2 + ")", JLabel.LEFT);
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
                JLabel weightLabel = new JLabel(lang.getText("HDWT_entry185arc"), JLabel.LEFT);
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

        JLabel typeArcLabel = new JLabel(lang.getText("HDWT_entry186arc"), JLabel.LEFT);
        //typeArcLabel.setFont(boldFont);
        typeArcLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(typeArcLabel);

        JLabel typeArcLabel2 = new JLabel(arc.getArcType().toString());
        typeArcLabel2.setFont(normalFont);
        typeArcLabel2.setBounds(columnB_posX - 40, columnB_Y += 20, colACompLength + 40, 20);
        components.add(typeArcLabel2);

        JLabel readArcLabel = new JLabel(lang.getText("HDWT_entry187arc"), JLabel.LEFT);
        readArcLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(readArcLabel);


        String txt = lang.getText("HDWT_entry188arc");
        if (pairedArc != null) {
            txt = lang.getText("HDWT_entry189arc") + pairedArc.getID() + "]";
        } else {
            if (InvariantsTools.isDoubleArc(arc)) {
                txt = lang.getText("HDWT_entry190arc");
            }
        }

        JLabel readArcLabel2 = new JLabel(txt);
        readArcLabel2.setFont(normalFont);
        readArcLabel2.setBounds(columnB_posX - 40, columnB_Y += 20, colACompLength + 60, 20);
        components.add(readArcLabel2);

        JLabel startNodeLabel = new JLabel(lang.getText("HDWT_entry191arc"), JLabel.LEFT);
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

        JLabel label3A = new JLabel(lang.getText("HDWT_entry192arc"), JLabel.LEFT);
        label3A.setBounds(columnA_posX + 80, columnA_Y, colACompLength, 20);
        components.add(label3A);
        JLabel label3B = new JLabel(Integer.toString(arc.getStartLocation().getSheetID()));
        label3B.setFont(normalFont);
        label3B.setBounds(columnA_posX + 120, columnB_Y, 40, 20);
        components.add(label3B);

        JLabel label4A = new JLabel(lang.getText("HDWT_entry193arc"), JLabel.LEFT);
        label4A.setBounds(columnA_posX + 150, columnA_Y, colACompLength, 20);
        components.add(label4A);
        JLabel label4B = new JLabel(arc.getStartLocation().getPosition().x + ", "
                + arc.getStartLocation().getPosition().y);
        label4B.setBounds(columnA_posX + 210, columnB_Y, colBCompLength, 20);
        label4B.setFont(normalFont);
        components.add(label4B);

        // endNode
        JLabel endNodeLabel = new JLabel(lang.getText("HDWT_entry194arc"), JLabel.LEFT);
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

        JLabel label7A = new JLabel(lang.getText("HDWT_entry195arc"), JLabel.LEFT);
        label7A.setBounds(columnA_posX + 80, columnA_Y, colACompLength, 20);
        components.add(label7A);
        JLabel label7B = new JLabel(Integer.toString(arc.getEndLocation().getSheetID()));
        label7B.setFont(normalFont);
        label7B.setBounds(columnA_posX + 120, columnB_Y, 40, 20);
        components.add(label7B);

        JLabel label8A = new JLabel(lang.getText("HDWT_entry196arc"), JLabel.LEFT);
        label8A.setBounds(columnA_posX + 150, columnA_Y, colACompLength, 20);
        components.add(label8A);
        JLabel label8B = new JLabel(arc.getEndLocation().getPosition().x + ", "
                + arc.getEndLocation().getPosition().y);
        label8B.setFont(normalFont);
        label8B.setBounds(columnA_posX + 210, columnB_Y, colBCompLength, 20);
        components.add(label8B);

        //KOLORY
        if (arc.getArcType() == TypeOfArc.COLOR) {
            String type1 = lang.getText("HDWT_entry197arc");
            String type2 = lang.getText("HDWT_entry198arc");
            pairedArc = InvariantsTools.getPairedArc(arc);
            if (pairedArc != null) {
                if (((Arc) element).getStartNode() instanceof Place) {
                    type1 = lang.getText("HDWT_entry198arc");
                    type2 = lang.getText("HDWT_entry197arc");
                }

                JLabel weightPair1Label = new JLabel(type1, JLabel.LEFT);
                weightPair1Label.setBounds(columnA_posX, columnA_Y += 20, 260, 20);
                columnB_Y += 20;
                components.add(weightPair1Label);
            }
            JLabel weight0Label = new JLabel(lang.getText("HDWT_entry199arc"), JLabel.LEFT);
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

            JLabel weight3Label = new JLabel(lang.getText("HDWT_entry200arc"), JLabel.LEFT);
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

            JLabel weight1Label = new JLabel(lang.getText("HDWT_entry201arc"), JLabel.LEFT);
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

            JLabel weight4Label = new JLabel(lang.getText("HDWT_entry202arc"), JLabel.LEFT);
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

            JLabel weight2Label = new JLabel(lang.getText("HDWT_entry203arc"), JLabel.LEFT);
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

            JLabel weight5Label = new JLabel(lang.getText("HDWT_entry204arc"), JLabel.LEFT);
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

                JLabel weightp0Label = new JLabel(lang.getText("HDWT_entry199arc"), JLabel.LEFT);
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

                JLabel weightp3Label = new JLabel(lang.getText("HDWT_entry200arc"), JLabel.LEFT);
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

                JLabel weightp1Label = new JLabel(lang.getText("HDWT_entry201arc"), JLabel.LEFT);
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

                JLabel weightp4Label = new JLabel(lang.getText("HDWT_entry202arc"), JLabel.LEFT);
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

                JLabel weightp2Label = new JLabel(lang.getText("HDWT_entry203arc"), JLabel.LEFT);
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

                JLabel weightp5Label = new JLabel(lang.getText("HDWT_entry204arc"), JLabel.LEFT);
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
     * Metoda pomocnicza konstruktora odpowiedzialna za utworzenia okna właściwości arkusza sieci.<br>
     * [2022-07-06] ...i czasopisma... tfu, znaczy: i jeszcze projektu aktualnie wgranego.
     * @param sheet (<b>WorkspaceSheet</b>) obiekt arkusza
     */
    @SuppressWarnings("UnusedAssignment")
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

        JLabel projectTypeLabel = new JLabel(lang.getText("HDWT_entry205sheet"), JLabel.LEFT);
        projectTypeLabel.setBounds(columnA_posX, columnA_Y += 10, 80, 20);
        components.add(projectTypeLabel);

        projectTypeLabelText = new JLabel(lang.getText("HDWT_entry206sheet"), JLabel.LEFT);
        projectTypeLabelText.setBounds(columnB_posX, columnB_Y += 10, 200, 20);
        projectTypeLabelText.setText(GUIController.access().getCurrentNetType() + "");
        components.add(projectTypeLabelText);

        //ArrayList<Integer> nodeTypes = Check.getSuggestedNetType();

        // SHEET ID
        JLabel netNameLabel = new JLabel(lang.getText("HDWT_entry207sheet"), JLabel.LEFT);
        netNameLabel.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 20);
        components.add(netNameLabel);

        JFormattedTextField netNameField = new JFormattedTextField();
        netNameField.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 20);
        netNameField.setText(overlord.getWorkspace().getProject().getName());
        netNameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00585exception")+"\n"+ex.getMessage(), "error", true);
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
        text += " " + lang.getText("HDWT_entry208sheet") + shPos + ")";
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

        JLabel zoomLabel1 = new JLabel(lang.getText("HDWT_entry209sheet"), JLabel.LEFT);
        zoomLabel1.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(zoomLabel1);
        JLabel zoomLabel2 = new JLabel(zoom + "%");
        zoomLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
        if (zoom != 100)
            zoomLabel2.setForeground(Color.red);
        components.add(zoomLabel2);

        // SHEET SIZE
        JLabel widthLabel = new JLabel(lang.getText("HDWT_entry210sheet"), JLabel.LEFT);
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
        JLabel heightLabel = new JLabel(lang.getText("HDWT_entry211sheet"), JLabel.LEFT);
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
        JLabel autoSrclLabel = new JLabel(lang.getText("HDWT_entry212sheet"), JLabel.LEFT);
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
     * @param invariantsData ArrayList[ArrayList[Integer]] - macierz inwariantów
     */
    private void createT_invSubWindow(ArrayList<ArrayList<Integer>> invariantsData) {
        doNotUpdate = true;
        initiateContainers();

        mode = tINVARIANTS;
        t_invariantsMatrix = new ArrayList<>();
        transitions = overlord.getWorkspace().getProject().getTransitions();
        places = overlord.getWorkspace().getProject().getPlaces();
        
        components.clear();
        panel.removeAll();
        int colA_posX = 10;
        int colB_posX = 100;
        int positionY = 10;

        JLabel chooseInvLabel = new JLabel(lang.getText("HDWT_entry213TinvPanel"));
        chooseInvLabel.setBounds(colA_posX, positionY, 80, 20);
        components.add(chooseInvLabel);

        //MRtinv
        String[] invariantHeaders = new String[3];
        invariantHeaders[0] = "---";
        invariantHeaders[1] = lang.getText("HDWT_entry214TinvPanel");   //null transitions
        invariantHeaders[2] = lang.getText("HDWT_entry215TinvPanel");   //inv/trans frequency

        chooseInvBox = new JComboBox<>(invariantHeaders);
        chooseInvBox.setBounds(colB_posX, positionY, 150, 20);
        chooseInvBox.addActionListener(actionEvent -> {
            if(doNotUpdate)
                return;

            refreshTINVwindowData();

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
                try{
                    String xStr = String.valueOf(comboBox.getSelectedItem());
                    int selInd = -1;
                    int ind1 = xStr.indexOf("#");
                    int ind2 = xStr.indexOf("(");
                    String invIndex = xStr.substring(ind1 + 1, ind2 - 1);
                    selInd = Integer.parseInt(invIndex);
                    selectedT_invIndex = selInd - 1;
                } catch (Exception ignored) {
                    overlord.log(lang.getText("LOGentry00046exception"), "error", true);
                    return;
                }
                showT_invariant();
            }
        });
        components.add(chooseInvBox);

        JButton prevButton = new JButton(lang.getText("HDWT_entry216TinvPanel"));   //Previous
        prevButton.setBounds(colB_posX - 50, positionY += 25, 100, 20);
        prevButton.setMargin(new Insets(0, 0, 0, 0));
        prevButton.setIcon(Tools.getResIcon16("/icons/invViewer/prevIcon.png"));
        prevButton.setToolTipText(lang.getText("HDWT_entry217TinvPanel"));
        prevButton.addActionListener(actionEvent -> {
            int sel = chooseInvBox.getSelectedIndex();
            if (sel > 0) {
                chooseInvBox.setSelectedIndex(sel - 1);
            }
        });
        components.add(prevButton);

        JButton nextButton = new JButton(lang.getText("HDWT_entry218TinvPanel")); //<html>&nbsp;&nbsp;&nbsp;Next&nbsp;</html>
        nextButton.setBounds(colB_posX + 55, positionY, 100, 20);
        nextButton.setMargin(new Insets(0, 0, 0, 0));
        nextButton.setIcon(Tools.getResIcon16("/icons/invViewer/nextIcon.png"));
        nextButton.setToolTipText(lang.getText("HDWT_entry219TinvPanel"));
        nextButton.addActionListener(actionEvent -> {
            int sel = chooseInvBox.getSelectedIndex();
            int max = chooseInvBox.getItemCount();
            if (sel < max - 1) {
                chooseInvBox.setSelectedIndex(sel + 1);
            }
        });
        components.add(nextButton);

        JButton recalculateTypesButton = new JButton(lang.getText("HDWT_entry220TinvPanel")); //Refresh
        recalculateTypesButton.setBounds(colA_posX, positionY += 25, 100, 20);
        recalculateTypesButton.setToolTipText(lang.getText("HDWT_entry220TinvPanel_1"));
        recalculateTypesButton.addActionListener(actionEvent -> {
            try {
                if(t_invariantsMatrix != null && !t_invariantsMatrix.isEmpty()) {
                    InvariantsCalculator ic = new InvariantsCalculator(true);
                    InvariantsTools.analyseInvariantTypes(ic.getCMatrix(), t_invariantsMatrix, true);
                }
                refreshTINVwindowData();
                refreshSubSurCombos();
            } catch (Exception e) {
                overlord.log(lang.getText("LOGentry00586exception"), "error", true);
            }
        });
        components.add(recalculateTypesButton);

        JButton recalculateInvTypesButton = new JButton(lang.getText("HDWT_entry221TinvPanel")); //Recalculate
        recalculateInvTypesButton.setIcon(Tools.getResIcon16("/icons/portal.png"));
        recalculateInvTypesButton.setToolTipText(lang.getText("HDWT_entry221TinvPanel_1"));
        recalculateInvTypesButton.setBounds(colA_posX + 105, positionY, 130, 20);
        recalculateInvTypesButton.addActionListener(actionEvent -> {
            try {
                refreshTINVwindowData();
                if(t_invariantsMatrix != null && !t_invariantsMatrix.isEmpty()) {
                    InvariantsCalculator ic = new InvariantsCalculator(true);
                    InvariantsTools.analyseInvariantTypes(ic.getCMatrix(), t_invariantsMatrix, true);
                }
                refreshSubSurCombos();
            } catch (Exception e) {
                overlord.log(lang.getText("LOGentry00587exception"), "error", true);
            }
        });
        components.add(recalculateInvTypesButton);

        String[] surHeaders = new String[1];
        surHeaders[0] = "---";
        String[] subHeaders = new String[1];
        subHeaders[0] = "---";

        @SuppressWarnings("MismatchedReadAndWriteOfArray")
        String[] nonsHeaders = new String[1];
        nonsHeaders[0] = "---";

        JLabel surLabel1 = new JLabel("Sur-inv: ");
        surLabel1.setBounds(colA_posX, positionY += 25, 80, 20);
        components.add(surLabel1);

        chooseSurInvBox = new JComboBox<>(surHeaders);
        chooseSurInvBox.setBounds(colB_posX, positionY, 150, 20);
        chooseSurInvBox.addActionListener(actionEvent -> {
            if(doNotUpdate)
                return;
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
                    overlord.log(lang.getText("LOGentry00588exception"), "error", true);
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
            if(doNotUpdate)
                return;
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
                    overlord.log(lang.getText("LOGentry00589exception"), "error", true);
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
            if(doNotUpdate)
                return;
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
                    overlord.log(lang.getText("LOGentry00590exception"), "error", true);
                }
            }
        });
        components.add(chooseNoneInvBox);

        JButton showDetailsButton = new JButton();
        showDetailsButton.setText(lang.getText("HDWT_entry222TinvPanel")); //Show details
        showDetailsButton.setIcon(Tools.getResIcon32("/icons/menu/menu_invViewer.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 25, 120, 40);
        showDetailsButton.addActionListener(actionEvent -> {
            if (selectedT_invIndex == -1)
                return;
            new HolmesInvariantsViewer(selectedT_invIndex);
        });
        components.add(showDetailsButton);

        JCheckBox markMCTcheckBox = new JCheckBox(lang.getText("HDWT_entry223TinvPanel")); //Color MCT
        markMCTcheckBox.setBounds(colA_posX + 120, positionY, 120, 20);
        markMCTcheckBox.setSelected(false);
        markMCTcheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            markMCT = abstractButton.getModel().isSelected();
            showT_invariant();
        });
        components.add(markMCTcheckBox);

        JCheckBox glowINVcheckBox = new JCheckBox(lang.getText("HDWT_entry224TinvPanel")); //Transitions glow
        glowINVcheckBox.setBounds(colA_posX + 120, positionY + 20, 160, 20);
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
        descPanel.setBounds(colA_posX, positionY += 45, 250, 80);
        components.add(descPanel);

        JCheckBox markAreaCheckBox = new JCheckBox(lang.getText("HDWT_entry225TinvPanel")); //Invariant-net structure painted
        markAreaCheckBox.setBounds(colA_posX, positionY += 85, 260, 20);
        markAreaCheckBox.setSelected(true);
        markAreaCheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            invStructure = abstractButton.getModel().isSelected();
            showT_invariant();
        });
        components.add(markAreaCheckBox);

        //TPN:
        JLabel labelTime1 = new JLabel(lang.getText("HDWT_entry226TinvPanel"));  //Min. time: 
        labelTime1.setBounds(colA_posX, positionY += 20, 100, 20);
        components.add(labelTime1);
        minTimeLabel = new JLabel("---");
        minTimeLabel.setBounds(colB_posX+20, positionY, 80, 20);
        components.add(minTimeLabel);

        JLabel labelTime2 = new JLabel(lang.getText("HDWT_entry228TinvPanel")); //Avg. time: 
        labelTime2.setBounds(colA_posX, positionY += 20, 100, 20);
        components.add(labelTime2);
        avgTimeLabel = new JLabel("---");
        avgTimeLabel.setBounds(colB_posX+20, positionY, 80, 20);
        components.add(avgTimeLabel);

        JLabel labelTime3 = new JLabel(lang.getText("HDWT_entry227TinvPanel")); //Max. time: 
        labelTime3.setBounds(colA_posX, positionY += 20, 100, 20);
        components.add(labelTime3);
        maxTimeLabel = new JLabel("---");
        maxTimeLabel.setBounds(colB_posX+20, positionY, 80, 20);
        components.add(maxTimeLabel);

        JLabel labelTime4 = new JLabel(lang.getText("HDWT_entry229TinvPanel")); //Structure:
        labelTime4.setBounds(colA_posX, positionY += 20, 100, 20);
        components.add(labelTime4);
        structureLabel = new JLabel("---");
        structureLabel.setBounds(colB_posX+20, positionY, 200, 20);
        components.add(structureLabel);

        doNotUpdate = false;

        panel.setLayout(null);
        for (JComponent component : components)
            panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        add(panel);
    }

    /**
     * Metoda odświeża zawartość comboBoxa dla inwariantów. Wyświetla tylko te, dla których cX=0.
     */
    public void refreshInvariantsComboBox() {
        if (t_invariantsMatrix == null)
            return;

        ArrayList<Integer> typesVector = overlord.getWorkspace().getProject().accessT_InvTypesVector();
        ArrayList<Integer> zeroInv = new ArrayList<>();
        for (int i = 0; i < typesVector.size(); i++) { //zliczanie tałatajstwa
            if(typesVector.get(i) == 0)
                zeroInv.add(i);
            //else if (typesVector.get(i) == 1)
            //    sursInv.add(i);
            //else if (typesVector.get(i) == -1)
            //    subsInv.add(i);
            //else if (typesVector.get(i) == 11)
            //    nonsInv.add(i);
        }

        //String[] invariantHeaders = new String[t_invariantsMatrix.size() + 3];
        String[] invariantHeaders = new String[zeroInv.size() + 3];
        invariantHeaders[0] = "---";

        if (!zeroInv.isEmpty()) {
            for (int i = 0; i < zeroInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(zeroInv.get(i))).size();
                invariantHeaders[i + 1] = "Inv. #" + (zeroInv.get(i) + 1) + " (size: " + invSize + ")";
            }
        }

        invariantHeaders[invariantHeaders.length - 2] = lang.getText("HDWT_entry214TinvPanel"); //null transitions
        invariantHeaders[invariantHeaders.length - 1] = lang.getText("HDWT_entry215TinvPanel"); //inv/trans frequency

        doNotUpdate = true;
        chooseInvBox.removeAllItems();
        for (String header : invariantHeaders) {
            chooseInvBox.addItem(header);
        }
        chooseInvBox.setSelectedIndex(0);
        doNotUpdate = false;
    }

    /**
     * Metoda odświeża zawartość comboBoxów dla niekanonicznych "inwariantów".
     */
    public void refreshSubSurCombos() {
        ArrayList<Integer> typesVector = overlord.getWorkspace().getProject().accessT_InvTypesVector();
        if(typesVector == null)
            typesVector = new ArrayList<>();

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
        if (!sursInv.isEmpty()) {
            for (int i = 0; i < sursInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(sursInv.get(i))).size();
                surHeaders[i + 1] = "Inv. #" + (sursInv.get(i) + 1) + " "+ lang.getText("HDWT_entry230TinvPanel") + invSize + ")";
            }
        }
        String[] subHeaders = new String[subsInv.size() + 1];
        subHeaders[0] = "---";
        if (!subsInv.isEmpty()) {
            for (int i = 0; i < subsInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(subsInv.get(i))).size();
                subHeaders[i + 1] = "Inv. #" + (subsInv.get(i) + 1) + " " + lang.getText("HDWT_entry230TinvPanel") + invSize + ")";
            }
        }
        String[] nonsHeaders = new String[nonsInv.size() + 1];
        nonsHeaders[0] = "---";
        if (!nonsInv.isEmpty()) {
            for (int i = 0; i < nonsInv.size(); i++) {
                int invSize = InvariantsTools.getSupport(t_invariantsMatrix.get(nonsInv.get(i))).size();
                nonsHeaders[i + 1] = "Inv. #" + (nonsInv.get(i) + 1) + " " + lang.getText("HDWT_entry230TinvPanel") + invSize + ")";
            }
        }

        chooseSurInvBox.setModel(new DefaultComboBoxModel<>(surHeaders));
        chooseSubInvBox.setModel(new DefaultComboBoxModel<>(subHeaders));
        chooseNoneInvBox.setModel(new DefaultComboBoxModel<>(nonsHeaders));
    }

    /**
     * Zmiana nazwy inwariantu.
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
        if(t_invariantsMatrix == null)
            return;
        if(t_invariantsMatrix.isEmpty()) //nie ma co odświeżać
            return;

        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedT_invIndex != -1) {
            ArrayList<Integer> invariant = t_invariantsMatrix.get(selectedT_invIndex);
            if (transitions.size() != invariant.size()) {
                transitions = overlord.getWorkspace().getProject().getTransitions();
                if (transitions == null || transitions.size() != invariant.size()) {
                    overlord.log(lang.getText("LOGentry00047"), "error", true);
                    return;
                }
            }

            ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();
            ColorPalette cp = new ColorPalette();
            for (int t = 0; t < invariant.size(); t++) {
                int fireValue = invariant.get(t);
                Transition trans = transitions.get(t);
                if (fireValue == 0) {
                    trans.qSimBoxT.qSimDrawed = false;
                    trans.qSimArcSign = false;
                    continue;
                }
                
                if (markMCT) {
                    int mctNo = transMCTvector.get(t);
                    if (mctNo == -1) {
                        trans.drawGraphBoxT.setGlowedINV(glowT_inv, fireValue);
                    } else {
                        trans.drawGraphBoxT.setColorWithNumber(true, cp.getColor(mctNo), false, fireValue, true, "[MCT" + (mctNo + 1) + "]");
                        trans.drawGraphBoxT.setGlowedINV(false, fireValue);
                    }
                } else {
                    trans.drawGraphBoxT.setGlowedINV(glowT_inv, fireValue);
                }

                if (invStructure) {
                    trans.qSimBoxT.qSimDrawed = true;
                    trans.qSimArcSign = true;
                    trans.qSimBoxT.qSimFillColor = new Color(0, 102, 0);
                    for (ElementLocation el : trans.getElementLocations()) {
                        el.qSimArcSign = true;
                    }
                }
            }

            if (invStructure) {
                for (Place place : places) { //zaznacz wszystkie EL struktury t-inwariantu
                    for (ElementLocation el : place.getElementLocations()) { //dla każdej lokalizacji miejsca
                        boolean inFound = false;
                        for (Arc inArc : el.getInArcs()) { //sprawdź łuki wejściowe
                            if (((Transition) inArc.getStartNode()).qSimBoxT.qSimDrawed) {
                                inFound = true; //to znaczy, że prowadzi tu łuk z tranzycji inwariantu
                                break;
                            }
                        }
                        if (!inFound)
                            continue;

                        for (ElementLocation el2 : place.getElementLocations()) {
                            for (Arc outArc : el2.getOutArcs()) { //sprawdź łuki wyjściowe
                                if (((Transition) outArc.getEndNode()).qSimBoxT.qSimDrawed) {
                                    el2.qSimArcSign = true;    //to znaczy, że prowadzi stąd łuk do tranzycji inwariantu
                                    el2.qSimDrawed = true;
                                    el.qSimArcSign = true;
                                    el.qSimDrawed = true;
                                    place.qSimBoxP.qSimFillColor = new Color(0, 102, 0);
                                    place.qSimBoxP.qSimDrawed = true;

                                    place.qSimBoxP.qSimTokens = 0; //jakie to płytkie :)
                                    place.qSimBoxP.qSimOvalColor = new Color(0, 102, 0);
                                    place.qSimBoxP.qSimOvalSize = 0;
                                    break;
                                }
                            }
                        }
                    }
                }

                for (Arc arc : pn.getArcs()) {
                    if (arc.getStartLocation().qSimArcSign && arc.getEndLocation().qSimArcSign) {
                        arc.arcQSimBox.qSimForcedArc = true;
                        arc.arcQSimBox.qSimForcedColor = new Color(0, 102, 0);
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
                    structText += timeVector.get(5).intValue() + "xTPN; ";
                }
                if (timeVector.get(6) > 0) {
                    structText += timeVector.get(6).intValue() + "xDPN; ";
                }
                if (timeVector.get(7) > 0) {
                    structText += timeVector.get(7).intValue() + "xTDPN; ";
                }
                if (timeVector.get(4) > 0) {
                    structText += timeVector.get(4).intValue() + "xPN; ";
                }
                structureLabel.setText(structText);
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    /**
     * Metoda pokazująca w ilu inwariantach występuje każda tranzycja.
     */
    private void showT_invTransFrequency() {
        if(t_invariantsMatrix == null)
            return;
        if(t_invariantsMatrix.isEmpty()) //nie ma co odświeżać
            return;

        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();
        
        ArrayList<Integer> freqVector = InvariantsTools.getFrequencyRealInvariants(t_invariantsMatrix, false);
        ArrayList<Transition> transitions_tmp = overlord.getWorkspace().getProject().getTransitions();

        int max_freq = freqVector.stream()
                .mapToInt(v -> v)
                .max().orElseThrow(NoSuchElementException::new);

        for (int i = 0; i < freqVector.size(); i++) {
            Transition realT = transitions_tmp.get(i);

            if (freqVector.get(i) != 0) {
                double fr = (double) freqVector.get(i) / (double) max_freq;
                realT.drawGraphBoxT.setColorWithNumber(true, getDiscColor(fr), true, freqVector.get(i), false, "");
                //System.out.println(realT.getName() + " trans \t" + realT.getID() + " \t " + fr + " \t " + freqVector.get(i));
            } else {
                realT.drawGraphBoxT.setColorWithNumber(true, Color.gray, true, 0, false, "");
                //System.out.println(realT.getName() + " trans \t" + realT.getID() + " \t " + 0 + " \t " + 0);
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
        if (t_invariantsMatrix == null)
            return;
        if (t_invariantsMatrix.isEmpty()) //nie ma co odświeżać
            return;

        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        HolmesNotepad note = new HolmesNotepad(700, 480);
        note.addTextLineNL(lang.getText("LOGentry00048"), "text");
        note.addTextLineNL(lang.getText("LOGentry00049"), "text");
        note.addTextLineNL("", "text");

        ArrayList<Integer> deadTrans = InvariantsTools.detectUncovered(t_invariantsMatrix, true);
        ArrayList<Transition> transitions_tmp = overlord.getWorkspace().getProject().getTransitions();
        int counter = 0;
        if (deadTrans == null) {
            JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry231TinvPanel"), lang.getText("HDWT_entry231TinvPanel_t"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (int deadOne : deadTrans) {
                Transition realT = transitions_tmp.get(deadOne);
                String t1 = Tools.setToSize("t" + deadOne, 5, false);
                note.addTextLineNL(t1 + " | " + realT.getName(), "text");
                realT.drawGraphBoxT.setGlowedINV(true, 0);
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

    /**
     * Metoda czyści podokno t-inwariantów, ponieważ chwilowo nie ma danych.
     */
    public void cleanTINVsubwindowFields() {
        t_invariantsMatrix = new ArrayList<>();

        String[] invariantHeaders = new String[3];
        invariantHeaders[0] = "---";
        invariantHeaders[1] = lang.getText("HDWT_entry214TinvPanel"); //null transitions
        invariantHeaders[2] = lang.getText("HDWT_entry215TinvPanel"); //inv/trans frequency

        doNotUpdate = true;
        chooseInvBox.removeAllItems();
        chooseInvBox.addItem(invariantHeaders[0]);
        chooseInvBox.addItem(invariantHeaders[1]);
        chooseInvBox.addItem(invariantHeaders[2]);
        chooseInvBox.setSelectedIndex(0);

        chooseSurInvBox.removeAllItems();
        chooseSubInvBox.removeAllItems();
        chooseNoneInvBox.removeAllItems();
        doNotUpdate = false;

        t_invNameField.setText("");
        minTimeLabel.setText("---");
        avgTimeLabel.setText("---");
        maxTimeLabel.setText("---");
        structureLabel.setText("---");
    }

    /**
     * Metoda pomocnicza konstruktora odpowiedzialna za wypełnienie podokna informacji o t-inwariantach sieci.
     * @param tInvariants ArrayList[ArrayList[Integer]] - macierz inwariantów
     */
    public void setT_invariants(ArrayList<ArrayList<Integer>> tInvariants) {
        t_invariantsMatrix = tInvariants;
    }

    /**
     * Metoda pobiera aktualne dane o p-invariantach. A także o miejsach i tranzycja sieci.
     */
    private void refreshTINVwindowData() {
        transitions = overlord.getWorkspace().getProject().getTransitions();
        places = overlord.getWorkspace().getProject().getPlaces();
        t_invariantsMatrix = overlord.getWorkspace().getProject().getT_InvMatrix();
        if(t_invariantsMatrix == null)
            t_invariantsMatrix = new ArrayList<>();
    }


    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************  p-INWARIANTY    ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda pomocnicza konstruktora odpowiedzialna za wypełnienie podokna informacji o p-inwariantach sieci.
     * @param pInvData ArrayList[ArrayList[Integer]] - macierz inwariantów
     */
    @SuppressWarnings("UnusedAssignment")
    private void createP_invSubWindow(ArrayList<ArrayList<Integer>> pInvData) {
        doNotUpdate = true;
        initiateContainers();

        mode = pINVARIANTS;
        p_invariantsMatrix = new ArrayList<>();
        places = overlord.getWorkspace().getProject().getPlaces();
        transitions = overlord.getWorkspace().getProject().getTransitions();

        int colA_posX = 10;
        int colB_posX = 100;
        int positionY = 10;

        JLabel chooseInvLabel = new JLabel(lang.getText("HDWT_entry232PinvPanel")); //p-invariant: 
        chooseInvLabel.setBounds(colA_posX, positionY, 80, 20);
        components.add(chooseInvLabel);

        String[] invariantHeaders = new String[3];
        invariantHeaders[0] = "---";
        invariantHeaders[1] = lang.getText("HDWT_entry233PinvPanel"); //null places
        invariantHeaders[2] = lang.getText("HDWT_entry234PinvPanel"); //inv/places frequency

        choosePInvBox = new JComboBox<>(invariantHeaders);
        choosePInvBox.setBounds(colB_posX, positionY, 150, 20);
        choosePInvBox.addActionListener(actionEvent -> {

            refreshPINVwindowData();
            if(p_invariantsMatrix.isEmpty()) { //nie ma co pokazywać
                return;
            }

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
        components.add(choosePInvBox);

        JButton showDetailsButton = new JButton();
        showDetailsButton.setText(lang.getText("HDWT_entry235PinvPanel")); //Show details
        showDetailsButton.setIcon(Tools.getResIcon32("/icons/menu/menu_invViewer.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 30, 120, 40);
        showDetailsButton.addActionListener(actionEvent -> {
            if(p_invariantsMatrix == null)
                return;
            if(p_invariantsMatrix.isEmpty()) //nie ma co pokazywać
                return;

            if (selectedP_invIndex == -1) {
                return;
            }
            try {
                new HolmesInvariantsViewer(selectedT_invIndex);
            } catch (Exception ex) {
                overlord.log(lang.getText("LOGentry00050exception")+"\n"+ex.getMessage(), "error", true);
            }
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
        descPanel.setBounds(colA_posX, positionY += 50, 250, 80);
        components.add(descPanel);

        doNotUpdate = false;

        panel.setLayout(null);
        for (JComponent component : components)
            panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        add(panel);
    }

    /**
     * Zmiana nazwy p-inwariantu.
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
                    overlord.log(lang.getText("LOGentry00051"), "error", true);
                    return;
                }
            }

            for (int p = 0; p < invariant.size(); p++) {
                int value = invariant.get(p);
                if (value == 0)
                    continue;

                places.get(p).drawGraphBoxP.setColorWithNumber(true, EditorResources.glowPlaceColorLevelBlue, false, -1, false, "");
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

        if (freqVector.isEmpty()) {
            JOptionPane.showMessageDialog(null, lang.getText("HWDT_entry236"), lang.getText("HWDT_entry236_t"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (int i = 0; i < freqVector.size(); i++) {
                Place realP = places_tmp.get(i);
                if (freqVector.get(i) != 0) {
                    realP.drawGraphBoxP.setColorWithNumber(true, EditorResources.glowPlaceColorLevelBlue, true, freqVector.get(i), false, "");
                } else
                    realP.drawGraphBoxP.setColorWithNumber(true, EditorResources.glowPlaceColorLevelRed, true, 0, false, "");
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    /**
     * Metoda pomocnicza do zaznaczania miejsc nie pokrytych inwariantami.
     */
    private void showDeadP_inv() {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        HolmesNotepad note = new HolmesNotepad(640, 480);
        note.addTextLineNL(lang.getText("HDWT_entry237PinvPanel"), "text");

        ArrayList<Integer> deadPlaces = InvariantsTools.detectUncovered(p_invariantsMatrix, false);
        ArrayList<Place> places_tmp = overlord.getWorkspace().getProject().getPlaces();
        int counter = 0;
        if (deadPlaces == null) {
            JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry237PinvPanel_1")
                    , lang.getText("HDWT_entry237PinvPanel_1t"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (int deadOne : deadPlaces) {
                Place realP = places_tmp.get(deadOne);
                String p1 = Tools.setToSize("p" + deadOne, 5, false);
                note.addTextLineNL(p1 + " | " + realP.getName(), "text");
                realP.drawGraphBoxP.setColorWithNumber(true, EditorResources.glowPlaceColorLevelRed, false, -1, false, "");
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

    /**
     * Metoda odświeża zawartość comboBoxów dla p-invariantów.
     */
    public void refreshP_invComboBox() {
        if (p_invariantsMatrix == null)
            return;

        String[] invariantHeaders = new String[p_invariantsMatrix.size() + 3];
        invariantHeaders[0] = "---";
        for (int i = 0; i < p_invariantsMatrix.size(); i++) {
            int invSize = InvariantsTools.getSupport(p_invariantsMatrix.get(i)).size();
            invariantHeaders[i + 1] = "Inv. #" + (i + 1) + " " + lang.getText("HDWT_entry238PinvPanel") + invSize + ")"; //size: 
        }
        invariantHeaders[invariantHeaders.length - 2] = lang.getText("HDWT_entry233PinvPanel");  //null places
        invariantHeaders[invariantHeaders.length - 1] = lang.getText("HDWT_entry234PinvPanel"); //inv/places frequency

        doNotUpdate = true;
        choosePInvBox.removeAllItems();
        for (String header : invariantHeaders) {
            choosePInvBox.addItem(header);
        }
        choosePInvBox.setSelectedIndex(0);
        doNotUpdate = false;
    }

    /**
     * Metoda pobiera aktualne dane o p-invariantach. A także o miejsach i tranzycja sieci.
     */
    private void refreshPINVwindowData() {
        transitions = overlord.getWorkspace().getProject().getTransitions();
        places = overlord.getWorkspace().getProject().getPlaces();
        p_invariantsMatrix = overlord.getWorkspace().getProject().getP_InvMatrix();
        if(p_invariantsMatrix == null)
            p_invariantsMatrix = new ArrayList<>();
    }

    /**
     * Metoda ustawia macierz p-inwariantów na podaną w parametrze.
     * @param pInvData ArrayList[ArrayList[Integer]] - macierz p-inwariantów
     */
    public void setP_invariants(ArrayList<ArrayList<Integer>> pInvData) {
        p_invariantsMatrix = pInvData;
    }

    /**
     * Metoda czyści podokno p-inwariantów, ponieważ chwilowo nie ma danych.
     */
    public void cleanPINVsubwindowFields() {
        p_invariantsMatrix = new ArrayList<>();

        String[] invariantHeaders = new String[3];
        invariantHeaders[0] = "---";
        invariantHeaders[1] = lang.getText("HDWT_entry233PinvPanel"); //null places
        invariantHeaders[2] = lang.getText("HDWT_entry234PinvPanel"); //inv/places frequency

        doNotUpdate = true;
        choosePInvBox.removeAllItems();
        choosePInvBox.addItem(invariantHeaders[0]);
        choosePInvBox.addItem(invariantHeaders[1]);
        choosePInvBox.addItem(invariantHeaders[2]);
        choosePInvBox.setSelectedIndex(0);

        doNotUpdate = false;
        p_invNameField.setText("");
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
    @SuppressWarnings("UnusedAssignment")
    private void createMCTSubWindow(ArrayList<ArrayList<Transition>> mct) {
        initiateContainers();
        mode = MCT;
        //overlord.reset.setMCTStatus(true);

        doNotUpdate = true;

        int colA_posX = 10;
        int colB_posX = 100;
        int positionY = 10;

        this.mctGroups = new ArrayList<>();

        String[] mctHeaders = new String[1];
        mctHeaders[0] = "---";

        // getting the data
        JLabel chooseMctLabel = new JLabel(lang.getText("HDWT_entry239MCTPanel")); //Choose MCT:
        chooseMctLabel.setBounds(colA_posX, positionY, 100, 20);
        components.add(chooseMctLabel);

        chooseMctBox = new JComboBox<>(mctHeaders);
        chooseMctBox.setBounds(colB_posX+15, positionY, 150, 20);
        chooseMctBox.addActionListener(actionEvent -> {
            if(mctGroups.isEmpty())
                return;

            JComboBox<?> comboBox = (JComboBox<?>) actionEvent.getSource();
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
        showDetailsButton.setText(lang.getText("HDWT_entry240MCTPanel")); //Show details
        showDetailsButton.setIcon(Tools.getResIcon32("/icons/invViewer/showInNotepad.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 30, 120, 32);
        showDetailsButton.addActionListener(actionEvent -> {
            showMCTNotepad();
        });
        components.add(showDetailsButton);

        JCheckBox glowCheckBox = new JCheckBox(lang.getText("HDWT_entry241MCTPanel")); //Different colors
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
        for (JComponent component : components)
            panel.add(component);
        panel.setOpaque(true);
        panel.repaint();
        add(panel);
    }

    /**
     * Metoda czyści podokno MCT, ponieważ chwilowo nie ma danych.
     */
    public void cleanMCtsubwindowFields() {
        mctGroups = new ArrayList<>();
        selectedMCTindex = -1;
        allMCTselected = false;
        MCTnameField.setText("");
        doNotUpdate = true;
        chooseMctBox.setSelectedIndex(0);
        doNotUpdate = false;
    }

    /**
     * Metoda pomocnicza konstruktora odpowiedzialna za wypełnienie podokna informacji o zbiorach MCT sieci.
     * @param mct ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
     */
    public void refreshMCTComboBox(ArrayList<ArrayList<Transition>> mct) {
        this.mctGroups = mct;
        if(mctGroups == null) //nie ma co pokazywać
            return;
        if(mctGroups.isEmpty())
            return;
        overlord.reset.setMCTStatus(true);

        String[] mctHeaders = new String[mctGroups.size() + 2];
        mctHeaders[0] = "---";
        for (int i = 0; i < mctGroups.size(); i++) {
            if (i < mctGroups.size() - 1)
                mctHeaders[i + 1] = "MCT #" + (i + 1) + " " + lang.getText("HDWT_entry242MCTPanel") + mctGroups.get(i).size() + ")"; // (size: 
            else {
                mctHeaders[i + 1] = lang.getText("HDWT_entry243MCTPanel"); //No-MCT transitions
                mctHeaders[i + 2] = lang.getText("HDWT_entry244MCTPanel"); //Show all
            }
        }
        doNotUpdate = true;
        chooseMctBox.removeAllItems();
        for (String header : mctHeaders) {
            chooseMctBox.addItem(header);
        }
        chooseMctBox.setSelectedIndex(0);
        doNotUpdate = false;
    }

    /**
     * Metoda zmiany nazwy zbioru MCT.
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
            note.addTextLineNL(lang.getText("HDWT_entry245MCTPanel") + size + "):", "text"); //Trivial MCT-transitions (
        } else {
            note.addTextLineNL(lang.getText("HDWT_entry246MCTPanel") + size + lang.getText("HDWT_entry247MCTPanel") + (selectedMCTindex + 1), "text"); //Transitions (  ) of MCT #
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
        if(mctGroups == null)
            return;

        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedMCTindex == -1)
            return;

        ArrayList<Transition> mct = mctGroups.get(selectedMCTindex);
        int size = mctGroups.size();
        ColorPalette cp = new ColorPalette();
        for (Transition transition : mct) {
            if (!colorMCT) {
                transition.drawGraphBoxT.setGlowed_MTC(true);
            } else {
                if (selectedMCTindex == size - 1)
                    transition.drawGraphBoxT.setColorWithNumber(true, cp.getColor(selectedMCTindex), false, 0, true, "[trivial]");
                else
                    transition.drawGraphBoxT.setColorWithNumber(true, cp.getColor(selectedMCTindex), false, 0, true, "[MCT" + (selectedMCTindex + 1) + "]");
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
                    transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "MCT #" + (m + 1) + " (" + mct.size() + ")");
                else
                    transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "");
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
    @SuppressWarnings("UnusedAssignment")
    private void createClustersSubWindow(ClusterDataPackage clusteringData) {
        initiateContainers();
        doNotUpdate = true;
        if (clusteringData == null || clusteringData.dataMatrix.isEmpty()) {
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

        JLabel label1 = new JLabel(lang.getText("HDWT_entry248ClusterPanel")); //Algorithm: 
        label1.setBounds(colA_posX, positionY, 80, 20);
        components.add(label1);
        JLabel label2 = new JLabel(clusterColorsData.algorithm);
        label2.setBounds(colB_posX, positionY, 80, 20);
        components.add(label2);
        positionY += 20;
        JLabel label3 = new JLabel(lang.getText("HDWT_entry249ClusterPanel")); //Metric:
        label3.setBounds(colA_posX, positionY, 80, 20);
        components.add(label3);
        JLabel label4 = new JLabel(clusterColorsData.metric);
        label4.setBounds(colB_posX, positionY, 80, 20);
        components.add(label4);
        positionY += 20;
        JLabel label5 = new JLabel(lang.getText("HDWT_entry250ClusterPanel"));   //Clusters: 
        label5.setBounds(colA_posX, positionY, 80, 20);
        components.add(label5);
        JLabel label6 = new JLabel(clusterColorsData.clNumber + "");
        label6.setBounds(colB_posX, positionY, 80, 20);
        components.add(label6);

        JLabel chooseInvLabel = new JLabel(lang.getText("HDWT_entry251ClusterPanel")); //Selected:
        chooseInvLabel.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(chooseInvLabel);

        // PRZEWIJALNA LISTA KLASTRÓW:
        String[] clustersHeaders = new String[clusterColorsData.dataMatrix.size() + 1];
        clustersHeaders[0] = "---";
        for (int i = 0; i < clusterColorsData.dataMatrix.size(); i++) {
            clustersHeaders[i + 1] = lang.getText("HDWT_entry252ClusterPanel") + " " + (i + 1) //Cluster 
                    + " " + lang.getText("HDWT_entry253ClusterPanel") + " " + clusterColorsData.clSize.get(i) + " inv.)"; //(size: 
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

        JLabel mssLabel1 = new JLabel(lang.getText("HDWT_entry254ClusterPanel")); //MSS value:
        mssLabel1.setBounds(colA_posX, positionY += 20, 80, 20);
        components.add(mssLabel1);

        mssValueLabel = new JLabel("n/a");
        mssValueLabel.setBounds(colB_posX, positionY, 80, 20);
        components.add(mssValueLabel);

        //SPOSÓB WYŚWIETLANIA - TRANZYCJE CZY ODPALENIA
        JCheckBox transFiringMode = new JCheckBox(lang.getText("HDWT_entry255ClusterPanel")); //Show transition average firing
        transFiringMode.setBounds(colA_posX - 3, positionY += 20, 220, 20);
        transFiringMode.setSelected(false);
        transFiringMode.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            clusterColorsData.showFirings = abstractButton.getModel().isSelected();
            int selected = chooseCluster.getSelectedIndex();
            chooseCluster.setSelectedIndex(selected);
        });
        components.add(transFiringMode);

        JCheckBox scaleMode = new JCheckBox(lang.getText("HDWT_entry256ClusterPanel")); //Show scaled colors
        scaleMode.setBounds(colA_posX - 3, positionY += 20, 170, 20);
        scaleMode.setSelected(false);
        scaleMode.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            clusterColorsData.showScale = abstractButton.getModel().isSelected();
            int selected = chooseCluster.getSelectedIndex();
            chooseCluster.setSelectedIndex(selected);
        });
        components.add(scaleMode);

        JCheckBox mctMode = new JCheckBox(lang.getText("HDWT_entry257ClusterPanel")); //Show MCT sets
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
        showDetailsButton.setText(lang.getText("HDWT_entry258ClusterPanel"));  //Show details
        showDetailsButton.setIcon(Tools.getResIcon22("/icons/clustWindow/showInfo.png"));
        showDetailsButton.setBounds(colA_posX, positionY += 30, 130, 30);
        showDetailsButton.addActionListener(actionEvent -> showClustersNotepad());
        components.add(showDetailsButton);

        JButton screenshotsButton = new JButton();
        screenshotsButton.setText(lang.getText("HDWT_entry259ClusterPanel")); //Export pictures
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
        Border border = BorderFactory.createTitledBorder(lang.getText("HDWT_entry260ClusterPanel")); //Completed
        progressBar.setBorder(border);
        progressBar.setVisible(false);
        progressBar.setForeground(Color.RED);
        components.add(progressBar);

        //inwarianty w ramach klastra:
        JLabel chooseClustInvLabel = new JLabel(lang.getText("HDWT_entry261ClusterPanel")); //Cluster inv.:
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
        showTimeDetailsButton.setText(lang.getText("HDWT_entry262ClusterPanel")); //Time details
        showTimeDetailsButton.setIcon(Tools.getResIcon22("/icons/clustWindow/showInfo.png"));
        showTimeDetailsButton.setBounds(colA_posX, positionY += 30, 130, 30);
        showTimeDetailsButton.addActionListener(actionEvent -> showTimeDataNotepad());
        components.add(showTimeDetailsButton);

        JButton showClustersDataButton = new JButton();
        showClustersDataButton.setText(lang.getText("HDWT_entry263ClusterPanel")); //Cluster details
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

        PetriNet pn = overlord.getWorkspace().getProject();
        ArrayList<Transition> transitions = pn.getTransitions();
        ArrayList<ArrayList<Integer>> invMatrix = pn.getT_InvMatrix();
        if (invMatrix == null || invMatrix.isEmpty())
            return;

        ArrayList<Integer> clInvariants = clusterColorsData.clustersInvariants.get(selectedClusterIndex);
        ArrayList<ArrayList<Integer>> invSubMatrix = new ArrayList<>();
        for (int i : clInvariants) {
            invSubMatrix.add(invMatrix.get(i));
        }

        ArrayList<Double> avgStatsVector = new ArrayList<>();

        HolmesNotepad note = new HolmesNotepad(800, 600);

        note.addTextLineNL("", "text");
        note.addTextLineNL(lang.getText("HDWT_entry264ClusterPanel") + " " + (selectedClusterIndex + 1) + " (" + clusterColorsData.clSize.get(selectedClusterIndex) 
                + " " + lang.getText("HDWT_entry265ClusterPanel") + " " + clusterColorsData.algorithm
                + " " + lang.getText("HDWT_entry266ClusterPanel") + clusterColorsData.metric, "text"); //Cluster: ; inv.) alg.:  ;  metric:
        note.addTextLineNL("", "text");
        note.addTextLineNL(lang.getText("HDWT_entry267ClusterPanel"), "text"); //T-invariants and their time values:

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
        avgMean /= avgStatsVector.size();

        double variance = 0;
        for (double avg : avgStatsVector) {
            variance += (avgMean - avg) * (avgMean - avg);
        }
        variance /= avgStatsVector.size();

        double stdDev = Math.sqrt(variance);

        note.addTextLineNL("", "text");
        note.addTextLineNL(lang.getText("HDWT_entry268ClusterPanel") + " " + avgMean, "text"); //Mean of average times:
        note.addTextLineNL(lang.getText("HDWT_entry269ClusterPanel") + " " + variance, "text"); //Variance:
        note.addTextLineNL(lang.getText("HDWT_entry270ClusterPanel") +" " + stdDev, "text"); //Standard deviation:

        note.setCaretFirstLine();
        note.setVisible(true);
    }

    //TODO
    private void showClustersData() {
        PetriNet pn = overlord.getWorkspace().getProject();
        ArrayList<ArrayList<Integer>> invMatrix = pn.getT_InvMatrix();
        if (invMatrix == null || invMatrix.isEmpty())
            return;

        HolmesNotepad note = new HolmesNotepad(800, 600);

        for (int i = 0; i < clusterColorsData.clustersInvariants.size(); i++) {
            note.addTextLineNL(lang.getText("HDWT_entry271ClusterPanel") + i, "text"); //Cluster#

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
            note.addTextLineNL(lang.getText("HDWT_entry271ClusterPanel") + i, "text"); //Cluster#

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
                clustersInvHeaders[i + 1] = lang.getText("HDWT_entry264ClusterPanel") + " " 
                        + (selectedClusterIndex + 1) + "  |#" + (i + 1) + "  Inv: " + (invIndex + 1); //Cluster: ;  |# ;  Inv:
            }
            chooseClusterInv.setModel(new DefaultComboBoxModel<>(clustersInvHeaders));
        } catch (Exception e) {
            overlord.log(lang.getText("LOGentry00591exception"), "error", true);
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
                transitions.get(i).drawGraphBoxT.setColorWithNumber(true, Color.DARK_GRAY, false, -1, false, "");
            }
        }

        for (int i = 0; i < invariant.size(); i++) {
            if (invariant.get(i) != 0) {

                transitions.get(i).drawGraphBoxT.setColorWithNumber(true, Color.GREEN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
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
        String dirPath = Tools.selectDirectoryDialog(lastPath, lang.getText("HDWT_entry272ClusterPanel"),
                lang.getText("HDWT_entry273ClusterPanel"));
        if (dirPath.isEmpty()) { // czy wskazano cokolwiek
            return;
        }

        int clusters = clusterColorsData.dataMatrix.size();
        int oldSelected = selectedClusterIndex;


        GraphPanel main = overlord.getWorkspace().getSheets().get(0).getGraphPanel();
        main.setZoom(120, main.getZoom());
        try {
            JOptionPane.showMessageDialog(null,
                    lang.getText("HDWT_entry274ClusterPanel"), 
                    lang.getText("HDWT_entry274ClusterPanel_t"), JOptionPane.INFORMATION_MESSAGE);

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
            overlord.log(lang.getText("HDWT_entry275ClusterPanel")+":\n"+e.getMessage(), "error", true);
            progressBar.setVisible(false);
        } finally {
            selectedClusterIndex = oldSelected;
            main.setZoom(100, main.getZoom());
        }
        progressBar.setVisible(false);
        JOptionPane.showMessageDialog(null, lang.getText("HDWT_entry276ClusterPanel") + " " 
                + clusters + " " + lang.getText("HDWT_entry276ClusterPanelb"), "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Metoda pokazująca dane o klastrach w notatniku.
     */
    private void showClustersNotepad() {
        if (selectedClusterIndex == -1)
            return;

        HolmesNotepad note = new HolmesNotepad(640, 480);

        note.addTextLineNL("", "text");
        note.addTextLineNL(lang.getText("HDWT_entry277ClusterPanel") + " " + (selectedClusterIndex + 1) + " (" + clusterColorsData.clSize.get(selectedClusterIndex) 
                + " " + lang.getText("HDWT_entry278ClusterPanel") + " " + clusterColorsData.algorithm
                + " " + lang.getText("HDWT_entry279ClusterPanel") + " " + clusterColorsData.metric, "text");
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
                holyVector.get(i).drawGraphBoxT.setColorWithNumber(false, Color.white, false, -1, false, "");
            } else {
                if (clustersMCT) {
                    int mctNo = transMCTvector.get(i);
                    if (mctNo == -1) {
                        if (clusterColorsData.showFirings) { //pokazuj średnią liczbę odpaleń
                            if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
                                holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, Color.CYAN, true, transColors.get(i).firedInCluster, false, "", 0, 20, 5, -3);
                            } else { //pokazuj kolory z krokiem 10%
                                holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, Color.CYAN, true, transColors.get(i).firedInCluster, false, "", 0, 20, 5, -3);
                            }
                        } else { //pokazuj tylko liczbę wystąpień jako część inwariantów
                            if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
                                holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, Color.CYAN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
                            } else { //pokazuj kolory z krokiem 10%
                                holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, Color.CYAN, true, transColors.get(i).transInCluster, false, "", 0, 20, 5, -3);
                            }
                        }
                    } else {
                        double value;
                        if (clusterColorsData.showFirings) {
                            value = transColors.get(i).firedInCluster;
                        } else {
                            value = transColors.get(i).transInCluster;
                        }
                        holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, cp.getColor(mctNo), true, value, true, "[MCT" + (mctNo + 1) + "]", -10, 15, 5, -3);
                    }
                } else {
                    if (clusterColorsData.showFirings) { //pokazuj średnią liczbę odpaleń
                        if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
                            double tranNumber = transColors.get(i).firedInCluster;
                            Color tranColor = transColors.get(i).colorFiredScale;
                            holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
                        } else { //pokazuj kolory z krokiem 10%
                            double tranNumber = transColors.get(i).firedInCluster;
                            Color tranColor = transColors.get(i).colorFiredGrade;
                            holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
                        }
                    } else { //pokazuj tylko liczbę wystąpień jako część inwariantów
                        if (clusterColorsData.showScale) { //pokazuj kolory skalowalne
                            int tranNumber = transColors.get(i).transInCluster;
                            Color tranColor = transColors.get(i).colorTransScale;
                            holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
                        } else { //pokazuj kolory z krokiem 10%
                            int tranNumber = transColors.get(i).transInCluster;
                            Color tranColor = transColors.get(i).colorTransGrade;
                            holyVector.get(i).drawGraphBoxT.setColorWithNumber(true, tranColor, true, tranNumber, false, "", 0, 0, 5, -2);
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
     */
    @SuppressWarnings("UnusedAssignment")
    private void createMCSSubWindow(){//MCSDataMatrix mcsData) {
        transitions = overlord.getWorkspace().getProject().getTransitions();
        initiateContainers();

        int posX = 10;
        int posY = 10;

        JLabel objRLabel = new JLabel(lang.getText("HDWT_entry280MCSPanel") + " ");
        objRLabel.setBounds(posX, posY, 80, 20);
        components.add(objRLabel);

        String[] objRset = new String[transitions.size() + 1];
        objRset[0] = "---";
        for (int i = 0; i < transitions.size(); i++) {
            objRset[i + 1] = "t" + i + transitions.get(i).getName();
        }

        //WYBÓR REAKCJI ZE ZBIORAMI MCS
        mcsObjRCombo = new JComboBox<>(objRset);
        mcsObjRCombo.setBounds(posX + 60, posY, 210, 20);
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

        JLabel mcsLabel = new JLabel(lang.getText("HDWT_entry281MCSPanel") + " "); //MCS: 
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
                int selTrans = mcsObjRCombo.getSelectedIndex();
                selTrans--;
                showMCSDataInNet(Objects.requireNonNull(comboBox.getSelectedItem()).toString(), selTrans);
            }
        });
        components.add(mcsMCSforObjRCombo);

        JButton refreshButton = new JButton();
        refreshButton.setText(lang.getText("HDWT_entry282MCSPanel")); //Refresh data
        refreshButton.setBounds(posX, posY + 30, 150, 30);
        refreshButton.addActionListener(actionEvent -> {
            transitions = overlord.getWorkspace().getProject().getTransitions();
            if (transitions.isEmpty())
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
     * Metoda inicjująca kontenery dla podokna z danymi o MCS.
     */
    public void cleanMCScomboBoxes() {
        transitions = overlord.getWorkspace().getProject().getTransitions();
        if (transitions.isEmpty())
            return;

        String[] objRset = new String[transitions.size() + 1];
        objRset[0] = "---";
        for (int i = 0; i < transitions.size(); i++) {
            objRset[i + 1] = "t" + i + transitions.get(i).getName();
        }

        stopAction = true;
        mcsObjRCombo.removeAllItems();
        for (String str : objRset) {
            mcsObjRCombo.addItem(str);
        }

        String[] init = new String[1];
        init[0] = "---";
        mcsMCSforObjRCombo.removeAllItems();
        for (String str : init) {
            mcsMCSforObjRCombo.addItem(str);
        }
        stopAction = false;
    }

    /**
     * Metoda pokazuje w kolorach tranzycje wchodzące w skład MCS oraz tramzycję bazową zbioru MCS.
     * @param sets String - zbiór w formie łańcucha znaków [x, y, z, ...]
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
            trans_TMP.drawGraphBoxT.setColorWithNumber(true, Color.red, false, -1, false, "");

            for (int id : invIDs) {
                trans_TMP = overlord.getWorkspace().getProject().getTransitions().get(id);
                trans_TMP.drawGraphBoxT.setColorWithNumber(true, Color.black, false, -1, false, "");
            }

            overlord.getWorkspace().getProject().repaintAllGraphPanels();
        } catch (Exception e) {
            overlord.log(lang.getText("LOGentry00592exception"), "error", true);
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
        initiateContainers();
        if (knockoutData == null || knockoutData.isEmpty()) {
            return;
        } else {
            mode = KNOCKOUT;
            this.knockoutData = knockoutData;
        }

        int colA_posX = 10;
        int colB_posX = 100;
        int positionY = 10;

        //MCT - obliczenia:
        MCTCalculator analyzer = overlord.getWorkspace().getProject().getMCTanalyzer();
        ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
        mct = MCTCalculator.getSortedMCT(mct, false);

        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        int transSize = transitions.size();

        ArrayList<String> mctOrNot = new ArrayList<>();
        for (int i = 0; i < transSize; i++) {
            mctOrNot.add("");
        }
        int mctNo = 0;
        for (ArrayList<Transition> arr : mct) {
            mctNo++;
            for (Transition t : arr) {
                int id = transitions.indexOf(t);
                mctOrNot.set(id, "MCT_" + mctNo);
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
        JLabel chooseMctLabel = new JLabel(lang.getText("HDWT_entry283KnockPanel") + " "); //Knockout:
        chooseMctLabel.setBounds(colA_posX, positionY, 60, 20);
        components.add(chooseMctLabel);

        JComboBox<String> chooseMctBox = new JComboBox<>(headers);
        chooseMctBox.setBounds(colB_posX, positionY, 150, 20);
        chooseMctBox.addActionListener(actionEvent -> {
            JComboBox<?> comboBox = (JComboBox<?>) actionEvent.getSource();
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

        JPanel textAreaPanel = new JPanel();
        textAreaPanel.setLayout(new BorderLayout());
        textAreaPanel.add(new JScrollPane(
                        knockoutTextArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
                BorderLayout.CENTER);

        int w = 0;//overlord.getMctBox().getWidth();
        int h = 0;//overlord.getMctBox().getHeight();
        textAreaPanel.setBounds(colA_posX, positionY, w - 30, h - 60);
        components.add(textAreaPanel);

        overlord.getKnockoutBox().getCurrentDockWindow().getPanel().setLayout(null);
        for (JComponent component : components) overlord.getKnockoutBox().getCurrentDockWindow().getPanel().add(component);
        overlord.getKnockoutBox().getCurrentDockWindow().getPanel().setOpaque(true);
        overlord.getKnockoutBox().getCurrentDockWindow().getPanel().revalidate();
        overlord.getKnockoutBox().getCurrentDockWindow().getPanel().repaint();
        panel.setVisible(true);
        add(panel);
    }

    /**
     * Metoda odpowiedzialna za pokazanie szczegółów wybranego zbioru MCT.
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
                trans_TMP.drawGraphBoxT.setColorWithNumber(true, Color.black, false, -1, false, "");
            }

            trans_TMP = transitions.get(knockIndex);
            trans_TMP.drawGraphBoxT.setColorWithNumber(true, Color.red, false, -1, false, "");

            knockoutTextArea.setText("");
            knockoutTextArea.append(lang.getText("HDWT_entry284KnockPanel") + knockIndex + ":\n"); //Knocked out:
            knockoutTextArea.append("\n");
            knockoutTextArea.append(" " + lang.getText("HDWT_entry285KnockPanel")); // * * * Also knocked out:\n

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
        int internalX = 10;
        int internalY = 10;

        initiateContainers();
        detector = new ProblemDetector(this);

        JLabel label0 = new JLabel(lang.getText("HDWT_entry286FixPanel")); //t-invariants:
        label0.setBounds(internalX, internalY, 100, 20);
        components.add(label0);

        internalY += 20;

        fixInvariants = new JLabel(lang.getText("HDWT_entry287FixPanel")); //Normal: 0 / Non-inv.: 0
        fixInvariants.setBounds(internalX, internalY, 190, 20);
        components.add(fixInvariants);

        internalY += 20;

        fixInvariants2 = new JLabel(lang.getText("HDWT_entry288FixPanel")); //Sub-inv.: 0 / Sur-inv: 0
        fixInvariants2.setBounds(internalX, internalY, 190, 20);
        components.add(fixInvariants2);

        JButton markInvButton = new JButton();
        markInvButton.setText(lang.getText("HDWT_entry289FixPanel")); //Show inv.
        markInvButton.setBounds(internalX + 185, internalY - 18, 110, 32);
        markInvButton.setMargin(new Insets(0, 0, 0, 0));
        markInvButton.setIcon(Tools.getResIcon22("/icons/fixGlass.png"));
        markInvButton.addActionListener(actionEvent -> detector.markSubSurNonInvariantsPlaces());
        markInvButton.setFocusPainted(false);
        components.add(markInvButton);

        internalY += 25;

        JLabel label1 = new JLabel(lang.getText("HDWT_entry290FixPanel")); //Input and output places:
        label1.setBounds(internalX, internalY, 200, 20);
        components.add(label1);

        internalY += 20;

        fixIOPlaces = new JLabel(lang.getText("HDWT_entry291FixPanel")); //Input: 0 / Output: 0
        fixIOPlaces.setBounds(internalX, internalY, 190, 20);
        components.add(fixIOPlaces);

        JButton markIOPlacesButton = new JButton();
        markIOPlacesButton.setText(lang.getText("HDWT_entry292FixPanel")); //Show places
        markIOPlacesButton.setBounds(internalX + 185, internalY - 16, 110, 32);
        markIOPlacesButton.setMargin(new Insets(0, 0, 0, 0));
        markIOPlacesButton.setIcon(Tools.getResIcon22("/icons/fixGlass.png"));
        markIOPlacesButton.addActionListener(actionEvent -> detector.markIOPlaces());
        markIOPlacesButton.setFocusPainted(false);
        components.add(markIOPlacesButton);

        internalY += 25;

        JLabel label2 = new JLabel(lang.getText("HDWT_entry293FixPanel")); //Input and output transitions:
        label2.setBounds(internalX, internalY, 200, 20);
        components.add(label2);

        internalY += 20;

        fixIOTransitions = new JLabel(lang.getText("HDWT_entry294FixPanel")); //Input: 0 / Output: 0
        fixIOTransitions.setBounds(internalX, internalY, 190, 20);
        components.add(fixIOTransitions);

        JButton markIOTransButton = new JButton();
        markIOTransButton.setText(lang.getText("HDWT_entry295FixPanel")); //Show trans.
        markIOTransButton.setBounds(internalX + 185, internalY - 14, 110, 32);
        markIOTransButton.setMargin(new Insets(0, 0, 0, 0));
        markIOTransButton.setIcon(Tools.getResIcon22("/icons/fixGlass.png"));
        markIOTransButton.addActionListener(actionEvent -> detector.markIOTransitions());
        markIOTransButton.setFocusPainted(false);
        components.add(markIOTransButton);

        JLabel label3 = new JLabel(lang.getText("HDWT_entry296FixPanel")); //Linear transitions and places
        label3.setBounds(internalX, internalY += 25, 200, 20);
        components.add(label3);

        internalY += 20;

        fixlinearTrans = new JLabel(lang.getText("HDWT_entry297FixPanel")); //Transitions: 0 / Places: 0
        fixlinearTrans.setBounds(internalX, internalY, 190, 20);
        components.add(fixlinearTrans);

        JButton markLinearTPButton = new JButton();
        markLinearTPButton.setText(lang.getText("HDWT_entry298FixPanel")); //Show T & P
        markLinearTPButton.setBounds(internalX + 185, internalY - 12, 110, 32);
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
    //*********************************       qSim       ***********************************
    //**************************************************************************************

    /**
     * Tworzy okno wykrywania i wskazywania problemów sieci.
     */
    @SuppressWarnings("UnusedAssignment")
    private void createQuickSimSubWindow() {
        int internalX = 10;
        int internalY = 10;
        initiateContainers();

        quickSim = new QuickSimTools(this);

        JButton acqDataButton = new JButton(lang.getText("HDWT_entry313QuickSim"));
        acqDataButton.setBounds(internalX, internalY, 130, 40);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setFocusPainted(false);
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText(lang.getText("HDWT_entry299QuickSim"));
        acqDataButton.addActionListener(actionEvent -> {
            quickSim.acquireData(scanTransitions, scanPlaces, markArcs, repetitions, quickProgressBar);
        });
        components.add(acqDataButton);

        JButton simSettingsButton = new JButton(lang.getText("HDWT_entry312QuickSim"));
        simSettingsButton.setBounds(internalX + 140, internalY, 130, 40);
        simSettingsButton.setMargin(new Insets(0, 0, 0, 0));
        simSettingsButton.setFocusPainted(false);
        simSettingsButton.setIcon(Tools.getResIcon32("/icons/simSettings/setupIcon.png"));
        simSettingsButton.setToolTipText(lang.getText("HDWT_entry300QuickSim")); //Set simulator options.
        simSettingsButton.addActionListener(actionEvent -> new HolmesSimSetup(overlord.getFrame()));
        components.add(simSettingsButton);

        internalY += 45;

        quickProgressBar = new JProgressBar();
        quickProgressBar.setBounds(internalX, internalY, 280, 25);
        quickProgressBar.setMaximum(100);
        quickProgressBar.setMinimum(0);
        quickProgressBar.setValue(0);
        quickProgressBar.setStringPainted(true);
        components.add(quickProgressBar);

        internalY += 30;

        JPanel borderPanel = new JPanel(null);
        //borderPanel.setLayout(new BorderLayout());
        borderPanel.setBounds(internalX, internalY, 280, 110);
        borderPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HDWT_entry301QuickSim"))); //Data type simulated
        components.add(borderPanel);

        JCheckBox transBox = new JCheckBox(lang.getText("HDWT_entry302QuickSim"), scanTransitions); //Transitions firing data
        transBox.setBounds(5, 20, 240, 20);
        transBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            scanTransitions = box.isSelected();
        });
        borderPanel.add(transBox);


        JCheckBox placesBox = new JCheckBox(lang.getText("HDWT_entry303QuickSim"), scanPlaces); //Places tokens data
        placesBox.setBounds(5, 40, 240, 20);
        placesBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            scanPlaces = box.isSelected();
        });
        borderPanel.add(placesBox);

        JCheckBox arcsBox = new JCheckBox(lang.getText("HDWT_entry304QuickSim"), markArcs); //Color arcs
        arcsBox.setBounds(5, 60, 240, 20);
        arcsBox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            markArcs = box.isSelected();
        });
        borderPanel.add(arcsBox);

        JCheckBox repsBox = new JCheckBox(lang.getText("HDWT_entry305QuickSim"), repetitions); //Repetitions
        repsBox.setBounds(5, 80, 240, 20);
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
    @SuppressWarnings("unchecked")
    private void createDecompositionData() {
        int posX = 10;
        int posY = 10;

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
            JComboBox<?> comboBox = (JComboBox<?>) actionEvent.getSource();
            //JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
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
            JComboBox<?> comboBox = (JComboBox<?>) actionEvent.getSource();
            //JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
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
            JComboBox<?> comboBox = (JComboBox<?>) actionEvent.getSource();
            //JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
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
        return switch (index) {
            case 0 -> "Functional ";
            case 1 -> "S-net ";
            case 2 -> "T-net ";
            case 3 -> "maxADT ";
            case 4 -> "Teng-Zeng ";
            case 5 -> "Hou ";
            case 6 -> "Nishi ";
            case 7 -> "Cycle";
            case 8 -> "Ootsuki";
            default -> "";
        };
    }

    private void generateProperSubNet(int index) {
        SubnetCalculator.compileElements();
        switch (index) {
            case 0 -> SubnetCalculator.generateFS();
            case 1 -> SubnetCalculator.generateSnets();
            case 2 -> SubnetCalculator.generateTnets();
            case 3 -> SubnetCalculator.generateADT();
            case 4 -> SubnetCalculator.generateTZ();
            case 5 -> SubnetCalculator.generateHou();
            case 6 -> SubnetCalculator.generateNishi();
            case 7 -> SubnetCalculator.generateCycle(false);
            case 8 -> SubnetCalculator.generateOotsuki();
        }
    }

    private int getSubnetSize(int index) {
        return switch (index) {
            case 0 -> SubnetCalculator.functionalSubNets.size();
            case 1 -> SubnetCalculator.snetSubNets.size();
            case 2 -> SubnetCalculator.tnetSubNets.size();
            case 3 -> SubnetCalculator.adtSubNets.size();
            case 4 -> SubnetCalculator.tzSubNets.size();
            case 5 -> SubnetCalculator.houSubNets.size();
            case 6 -> SubnetCalculator.nishiSubNets.size();
            case 7 -> SubnetCalculator.cycleSubNets.size();
            case 8 -> SubnetCalculator.ootsukiSubNets.size();
            default -> 0;
        };
    }

    private void showSubNet(int typeOfDecomposition) {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedSubNetindex == -1)
            return;
        SubnetCalculator.SubNet subnet = null;
        int size = 0;
        switch (typeOfDecomposition) {
            case 0 -> {
                subnet = SubnetCalculator.functionalSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.functionalSubNets.size();
            }
            case 1 -> {
                subnet = SubnetCalculator.snetSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.snetSubNets.size();
            }
            case 2 -> {
                subnet = SubnetCalculator.tnetSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.tnetSubNets.size();
            }
            case 3 -> {
                subnet = SubnetCalculator.adtSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.adtSubNets.size();
            }
            case 4 -> {
                subnet = SubnetCalculator.tzSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.tzSubNets.size();
            }
            case 5 -> {
                subnet = SubnetCalculator.houSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.houSubNets.size();
            }
            case 6 -> {
                subnet = SubnetCalculator.nishiSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.nishiSubNets.size();
            }
            case 7 -> {
                subnet = SubnetCalculator.cycleSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.cycleSubNets.size();
            }
            case 8 -> {
                subnet = SubnetCalculator.ootsukiSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.ootsukiSubNets.size();
            }
        }

        ColorPalette cp = new ColorPalette();

        //places
        boolean colorSubNet = false;

        assert subnet != null;
        for (Place place : subnet.getSubPlaces()) {
            if (!colorSubNet) {
                place.setGlowedSub(true);

            } else {
                if (selectedSubNetindex == size - 1)
                    place.drawGraphBoxP.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[trivial]");
                else
                    place.drawGraphBoxP.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
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
                transition.drawGraphBoxT.setGlowed_MTC(true);

            } else {
                if (selectedSubNetindex == size - 1)
                    transition.drawGraphBoxT.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[trivial]");
                else
                    transition.drawGraphBoxT.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
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
                    ((Transition) transition).drawGraphBoxT.setColorWithNumber(true, Color.red, false, m, true, "");
                if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                    ((Place) transition).drawGraphBoxP.setColorWithNumber(true, Color.red, false, m, true, "");
            }
            ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
            for (Arc arc : arcs) {
                arc.arcDecoBox.setColor(true, Color.red);
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
                        transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.drawGraphBoxP.setColorWithNumber(true, calcMiddleColor(currentColor, place.drawGraphBoxP.getPlaceNewColor()), false, m, true, "");
                        else
                            place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
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
                        place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + places.size() + ")");
                    }

                    ArrayList<Transition> transitions = subnets.get(m).getSubTransitions();
                    for (Transition transition : transitions) {
                        if (subNet.getSubBorderTransition().contains(transition))
                            transition.drawGraphBoxT.setColorWithNumber(true, calcMiddleColor(currentColor, transition.drawGraphBoxT.getTransitionNewColor()), false, m, true, "");
                        else
                            transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
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
                        transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.drawGraphBoxP.setColorWithNumber(true, calcMiddleColor(currentColor, place.drawGraphBoxP.getPlaceNewColor()), false, m, true, "");
                        else
                            place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
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
                        transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m + 1) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.drawGraphBoxP.setColorWithNumber(true, calcMiddleColor(currentColor, place.drawGraphBoxP.getPlaceNewColor()), false, m, true, "");
                        else
                            place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
                    }
                }
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    private ArrayList<SubnetCalculator.SubNet> getCorrectSubnet(int type) {
        return switch (type) {
            case 0 -> SubnetCalculator.functionalSubNets;
            case 1 -> SubnetCalculator.snetSubNets;
            case 2 -> SubnetCalculator.tnetSubNets;
            case 3 -> SubnetCalculator.adtSubNets;
            case 4 -> SubnetCalculator.tzSubNets;
            case 5 -> SubnetCalculator.houSubNets;
            case 6 -> SubnetCalculator.nishiSubNets;
            case 7 -> SubnetCalculator.cycleSubNets;
            case 8 -> SubnetCalculator.ootsukiSubNets;
            default -> SubnetCalculator.functionalSubNets;
        };
    }

    @SuppressWarnings("all")
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
     * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
     */
    private void repaintGraphPanel() {
        int sheetIndex = overlord.IDtoIndex(elementLocation.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
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
     * @param value boolean - true, jeśli autoscroll włączony
     */
    private void setAutoscroll(boolean value) {
        if (mode == SHEET) {
            currentSheet.getGraphPanel().setAutoDragScroll(value);
        }
    }

    /**
     * Metoda ustawia nową wartość czasu EFT dla tranzycji czasowej.
     * @param x (double) nowe EFT.
     */
    private void setMinFireTime(double x) {
        if (mode == TIMETRANSITION) {
            Transition transition = (Transition) element;
            transition.timeExtension.setEFT(x);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda ustawia nową wartość czasu LFT dla tranzycji czasowej.
     * @param x (double) nowe LFT.
     */
    private void setMaxFireTime(double x) {
        if (mode == TIMETRANSITION) {
            Transition transition = (Transition) element;
            transition.timeExtension.setLFT(x);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda ustawia nową wartość opóźnienia dla produkcji tokenów.
     * @param x (double) nowa wartość duration.
     */
    private void setDurationTime(double x) {
        if (mode == TIMETRANSITION) {
            Transition transition = (Transition) element;
            transition.timeExtension.setDPNduration(x);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda ustawia status trybu TPN dla tranzycji.
     * @param status (boolean) nowy status.
     */
    private void setTPNstatus(boolean status) {
        if (mode == TIMETRANSITION) {
            Transition transition = (Transition) element;
            transition.timeExtension.setTPNstatus(status);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda ustawia status trybu DPN dla tranzycji.
     * @param status (boolean) nowy status, true jeżeli ma być DPN
     */
    private void setDPNstatus(boolean status) {
        if (mode == TIMETRANSITION) {
            Transition transition = (Transition) element;
            transition.timeExtension.setDPNstatus(status);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia współrzędną X dla wierzchołka sieci.
     * @param x (int) nowa wartość
     */
    private void setX(int x) {
        if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION
                || mode == XTPN_PLACE || mode == XTPN_TRANS) {
            elementLocation.setPosition(new Point(x, elementLocation.getPosition().y));
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia współrzędną Y dla wierzchołka sieci.
     * @param y int - nowa wartość
     */
    private void setY(int y) {
        if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION
                || mode == XTPN_PLACE || mode == XTPN_TRANS) {
            elementLocation.setPosition(new Point(elementLocation.getPosition().x, y));
            repaintGraphPanel();
        }
    }

    /**
     * Metoda sprawdza, czy dla danego węzła sieci lokalizacja jego nazwy nie wykracza poza ramy
     * obrazu sieci - dla współrzędnej Y.
     * @param oldY (int) współrzędna Y.
     * @param n    (Node) wierzchołek sieci.
     * @param el   (ElementLocation) obiekt lokalizacji wierzchołka.
     * @return (Point) prawidłowe współrzędne.
     */
    private Point setNameOffsetY(int oldY, Node n, ElementLocation el) {
        int nameLocIndex = n.getElementLocations().indexOf(el);
        int oldX = n.getTextsLocations(GUIManager.locationMoveType.NAME).get(nameLocIndex).getPosition().x;

        int sheetIndex = overlord.IDtoIndex(el.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();

        if (graphPanel.isLegalLocation(new Point(oldX + el.getPosition().x, oldY + el.getPosition().y))) {
            n.getTextsLocations(GUIManager.locationMoveType.NAME).get(nameLocIndex).getPosition().setLocation(oldX, oldY);
            graphPanel.repaint();
        }
        return n.getTextsLocations(GUIManager.locationMoveType.NAME).get(nameLocIndex).getPosition();
    }

    /**
     * Metoda sprawdza, czy dla danego węzła sieci lokalizacja jego nazwy nie wykracza poza ramy
     * obrazu sieci - dla współrzędnej X.
     * @param oldX (int) współrzędna X.
     * @param n    (Node) wierzchołek sieci.
     * @param el   (ElementLocation) - obiekt lokalizacji wierzchołka.
     * @return (Point) prawidłowe współrzędne.
     */
    private Point setNameOffsetX(int oldX, Node n, ElementLocation el) {
        int nameLocIndex = n.getElementLocations().indexOf(el);
        int oldY = n.getTextsLocations(GUIManager.locationMoveType.NAME).get(nameLocIndex).getPosition().y;

        int sheetIndex = overlord.IDtoIndex(el.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();

        if (graphPanel.isLegalLocation(new Point(oldX + el.getPosition().x, oldY + el.getPosition().y))) {
            n.getTextsLocations(GUIManager.locationMoveType.NAME).get(nameLocIndex).getPosition().setLocation(oldX, oldY);
            graphPanel.repaint();
        }
        return n.getTextsLocations(GUIManager.locationMoveType.NAME).get(nameLocIndex).getPosition();
    }

    /**
     * Zmiana nazwy elementu sieci, dokonywana poza listenerem, który
     * jest klasa anonimową (i nie widzi pola element).
     * @param newName (String) nowa nazwa.
     */
    private void changeName(String newName) {
        if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION || mode == META
                || mode == XTPN_PLACE || mode == XTPN_TRANS) {
            Node node = (Node) element;
            node.setName(newName);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia komentarz dla elementu sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     * @param newComment (String) nowy komentarz.
     */
    private void changeComment(String newComment) {
        element.setComment(newComment);
    }

    /**
     * Tworzenie portalu z aktualnie wybranego elementu sieci.
     */
    private void makePortal() {
        if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION || mode == CTRANSITION
                || mode == XTPN_PLACE || mode == XTPN_TRANS) {
            Node node = (Node) element;
            node.setPortal(true);
        }
    }

    /**
     * Wyłączenie statusu portalu na elemencie posiadającym tylko jedno EL.
     */
    private void unPortal() {
        if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION || mode == CTRANSITION
                || mode == XTPN_PLACE || mode == XTPN_TRANS) {
            Node node = (Node) element;
            if (node.getElementLocations().size() == 1)
                node.setPortal(false);
        }
    }

    /**
     * Metoda zmienia liczbę tokenów dla miejsca sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     * @param tokens int - nowa liczba tokenów
     */
    private void setTokens(int tokens) {
        Place place = (Place) element;
        if (mode == PLACE) {
            place.setTokensNumber(tokens);

            if (overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN == 0) {
                ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
                overlord.getWorkspace().getProject().accessStatesManager().getStatePN(0).setTokens(places.indexOf(place), tokens);
            }
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia liczbę tokenów dla miejsca kolorowego sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     * @param tokens int - nowa liczba tokenów
     * @param i      int - nr porządkowy tokenu, default 0, od 0 do 5
     */
    private void setColorTokens(int tokens, int i) {
        if (mode == PLACE && element instanceof PlaceColored) {
            PlaceColored place = (PlaceColored) element;
            switch (i) {
                case 0 -> place.setColorTokensNumber(tokens, 0);
                case 1 -> place.setColorTokensNumber(tokens, 1);
                case 2 -> place.setColorTokensNumber(tokens, 2);
                case 3 -> place.setColorTokensNumber(tokens, 3);
                case 4 -> place.setColorTokensNumber(tokens, 4);
                case 5 -> place.setColorTokensNumber(tokens, 5);
                default -> place.setTokensNumber(tokens);
            }
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia wagę dla łuku sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     * @param weight int - nowa waga.
     * @param arc    Arc - łuk.
     */
    private void setWeight(int weight, Arc arc) {
        if (mode == ARC || mode == XARC) {
            arc.setWeight(weight);
            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia wagę dla łuku sieci, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     * @param weight int - nowa waga dla koloru
     * @param arc    Arc - obiekt łuku
     * @param i      int - nr porządkowy koloru, default 0, od 0 do 5
     */
    private void setColorWeight(int weight, Arc arc, int i) {
        if (mode == ARC || mode == XARC) {
            switch (i) {
                case 0 -> arc.setColorWeight(weight, i);
                case 1 -> arc.setColorWeight(weight, i);
                case 2 -> arc.setColorWeight(weight, i);
                case 3 -> arc.setColorWeight(weight, i);
                case 4 -> arc.setColorWeight(weight, i);
                case 5 -> arc.setColorWeight(weight, i);
                default -> arc.setColorWeight(weight, i);
            }

            repaintGraphPanel();
        }
    }

    /**
     * Metoda zmienia liczbę tokenów aktywacji tranzycji, poza listenerem, który
     * jest klasą anonimową (i nie widzi pola element).
     * @param weight int - nowa waga aktywacji dla koloru
     * @param trans  Transition - obiekt łuku
     * @param i      int - nr porządkowy koloru, default 0, od 0 do 5
     */
    private void setActivationWeight(int weight, Transition trans, int i) {
        if (trans instanceof TransitionColored) {
            TransitionColored transition = (TransitionColored) trans;
            switch (i) {
                case 0 -> transition.setRequiredColoredTokens(weight, 0);
                case 1 -> transition.setRequiredColoredTokens(weight, 1);
                case 2 -> transition.setRequiredColoredTokens(weight, 2);
                case 3 -> transition.setRequiredColoredTokens(weight, 3);
                case 4 -> transition.setRequiredColoredTokens(weight, 4);
                case 5 -> transition.setRequiredColoredTokens(weight, 5);
                default -> transition.setRequiredColoredTokens(weight, 0);
            }
        }

    }

    /**
     * Metoda ustawia nowy obiekt symulatora sieci.
     * @param netSim     (<b>NetSimulator</b>) simulator zwykły.
     * @param netSimXTPN (<b>NetSimulatorXTPN</b>) simulator XTPN.
     */
    public void setSimulator(GraphicalSimulator netSim, GraphicalSimulatorXTPN netSimXTPN) {
        simulator = netSim;
        simulatorXTPN = netSimXTPN;
    }

    /**
     * Metoda zwraca obiekt aktywnego zwykłego symulatora z podokna symulacji.
     * @return (< b > NetSimulator < / b >) obiekt symulatora sieci zwykłej.
     */
    public GraphicalSimulator getSimulator() {
        return simulator;
    }

    /**
     * Metoda zwraca obiekt aktywnego symulatora XTPN z podokna symulacji.
     * @return (< b > NetSimulatorXTPN < / b >) obiekt symulatora XTPN.
     */
    public GraphicalSimulatorXTPN getSimulatorXTPN() {
        return simulatorXTPN;
    }
    
    /**
     * Metoda uaktywnia tylko przyciski stop i pauza dla symulatora. Cała reszta - nieaktywna.
     */
    public void allowOnlySimulationDisruptButtons() {
        setStartSimulationButtonsStatus(false);
        setPauseAndStopSimulationButtonsStatus(true);

        overlord.getSimulatorBox().getCurrentDockWindow().getPanel().revalidate();
        overlord.getSimulatorBox().getCurrentDockWindow().getPanel().repaint();
    }

    /**
     * Metoda uaktywnia tylko przycisku startu dla symulatora, blokuje stop i pauzę.
     */
    public void allowOnlySimulationInitiateButtons() {
        setStartSimulationButtonsStatus(true);
        setPauseAndStopSimulationButtonsStatus(false);

        overlord.getSimulatorBox().getCurrentDockWindow().getPanel().revalidate();
        overlord.getSimulatorBox().getCurrentDockWindow().getPanel().repaint();
    }

    /**
     * Metoda ustawia status wszystkich przycisków rozpoczęcia symulacji za wyjątkiem
     * Pauzy, Stopu - w przypadku startu / stopu symulacji
     *
     * @param enabled boolean - true, jeśli mają być aktywne
     */
    private void setStartSimulationButtonsStatus(boolean enabled) {
        for (JComponent comp : components) {
            if (comp instanceof JButton && comp.getName() != null) {
                if (comp.getName().equals("simPNoneBack") || comp.getName().equals("simPNoneForward")
                        || comp.getName().equals("simB3") || comp.getName().equals("simB4")
                        || comp.getName().equals("simPNstart") || comp.getName().equals("simPNstartSingle")
                        || comp.getName().equals("simPNreset")) {
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
    private void setPauseAndStopSimulationButtonsStatus(boolean enabled) {
        for (JComponent comp : components) {
            if (comp instanceof JButton && comp.getName() != null) {
                if (comp.getName().equals("simPNpause") || comp.getName().equals("simPNstop")) {
                    comp.setEnabled(enabled);
                }
            }
        }
    }

    /**
     * Metoda zostawia aktywny tylko przycisku od-pauzowania.
     */
    public void allowOnlyUnpauseButton() {
        allowOnlySimulationDisruptButtons();

        //values.get(4).setEnabled(false);
        for (JComponent comp : components) {
            if (comp instanceof JButton && comp.getName() != null) {
                if (comp.getName().equals("simPNpause")) {
                    comp.setEnabled(true);
                    break;
                }
            }
        }
    }

    /**
     * Metoda uaktywnia tylko przyciski stop i pauza dla symulatora XTPN. Cała reszta - nieaktywna.
     */
    public void allowOnlySimulationDisruptButtonsXTPN() {
        setEnabledSimulationInitiateButtonsXTPN(false);
        setEnabledSimulationDisruptButtonsXTPN(true);
    }

    /**
     * Metoda uaktywnia tylko przycisku startu dla symulatora, blokuje stop i pauzę.
     */
    public void allowOnlySimulationInitiateButtonsXTPN() {
        setEnabledSimulationInitiateButtonsXTPN(true);
        setEnabledSimulationDisruptButtonsXTPN(false);
    }

    /**
     * Metoda ustawia status wszystkich przycisków rozpoczęcia symulacji XTPN za wyjątkiem
     * Pauzy, Stopu - w przypadku startu / stopu symulacji
     * @param enabled boolean - true, jeśli mają być aktywne
     */
    private void setEnabledSimulationInitiateButtonsXTPN(boolean enabled) {
        for (JComponent comp : components) {
            if (comp instanceof HolmesRoundedButton && comp.getName() != null) {
                if (comp.getName().equals("XTPNstart") || comp.getName().equals("resetM0button")) {
                    comp.setEnabled(enabled);
                }
            }
        }
    }
    
    /**
     * Metoda ustawia status przycisków Stop, Pauza.
     * @param enabled boolean - true, jeśli mają być aktywne
     */
    private void setEnabledSimulationDisruptButtonsXTPN(boolean enabled) {
        for (JComponent comp : components) {
            if (comp instanceof HolmesRoundedButton && comp.getName() != null) {
                if (comp.getName().equals("XTPNstop") || comp.getName().equals("XTPNpause")) {
                    comp.setEnabled(enabled);
                }
            }
        }
    }

    /**
     * Metoda zostawia aktywny tylko przycisku od-pauzowania.
     */
    public void allowOnlyUnpauseButtonXTPN() {
        allowOnlySimulationDisruptButtonsXTPN();
        for (JComponent comp : components) {
            if (comp instanceof HolmesRoundedButton && comp.getName() != null) {
                if (comp.getName().equals("XTPNpause")) {
                    comp.setEnabled(true);
                    break;
                }
            }
        }
    }

    /**
     * Metoda czyści dane o p-inwariantach.
     */
    public void cleanPInvSubwindowData() {
        p_invariantsMatrix = null;
    }

    /**
     * Metoda czyści dane o klastrach.
     */
    public void cleanClustersSubwindowData() {
        clusterColorsData = null;
    }
}