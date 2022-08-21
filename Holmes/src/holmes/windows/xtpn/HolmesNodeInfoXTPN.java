package holmes.windows.xtpn;

import holmes.darkgui.GUIManager;
import holmes.darkgui.dockwindows.SharedActionsXTPN;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.petrinet.elements.*;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.simulators.xtpn.StateSimulatorXTPN;
import holmes.utilities.Tools;
import holmes.workspace.WorkspaceSheet;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class HolmesNodeInfoXTPN extends JFrame {
    private GUIManager overlord;
    private PlaceXTPN thePlace;
    private ElementLocation eLocation;
    private HolmesNodeInfoXTPNactions action = new HolmesNodeInfoXTPNactions(this);
    private TransitionXTPN theTransition;
    private boolean doNotUpdate = false;

    private JPanel mainInfoPanel;
    private JFrame parentFrame;
    public boolean mainSimulatorActive = false;
    private XYSeriesCollection dynamicsSeriesDataSet = null;
    private JFreeChart dynamicsChart;

    //simulation variables:
    private int simSteps = 30000; //ile kroków symulacji
    private int repeated = 10; //ile powtórzeń (dla kroków)
    private int rep_succeed = 1;
    private boolean simulateTime = false; //czy wykres czasowy na osi X dla miejsc
    private double simTimeLength = 5000.0;
    private int placeChartType = 0; //0 - kroki, 1 - czas (oś X)
    private int transitionChartType = 0; //0 - kroki, 1 - czas (oś X)
    ArrayList<Double> stepsVectorPlaces = new ArrayList<>();
    ArrayList<ArrayList<Double>> timeVectorPlaces = new ArrayList<>();
    ArrayList<ArrayList<Double>> statusVectorTransition = new ArrayList<>();



    //MIEJSCA:
    private HolmesRoundedButton buttonGammaMode;
    private HolmesRoundedButton gammaVisibilityButton;
    private HolmesRoundedButton tokensWindowButton; //przycisk podokna tokenó XTPN
    private JFormattedTextField tokensTextBox; //liczba tokenów
    private JFormattedTextField gammaMinTextField;
    private JFormattedTextField gammaMaxTextField;

    private JCheckBox simPlaceStepsCheckbox;
    private JCheckBox simPlaceTimeCheckbox;
    private JCheckBox simPlaceRepsCheckbox;
    private boolean simPlaceReps = false;

    //TRANZYCJE:
    private HolmesRoundedButton buttonAlphaMode;
    private HolmesRoundedButton buttonBetaMode;
    private HolmesRoundedButton buttonClassXTPNmode;
    private HolmesRoundedButton alphaVisibilityButton;
    private HolmesRoundedButton betaVisibilityButton;
    private HolmesRoundedButton tauVisibilityButton;
    private JFormattedTextField alphaMinTextField;
    private JFormattedTextField alphaMaxTextField;
    private JFormattedTextField betaMinTextField;
    private JFormattedTextField betaMaxTextField;

    //STATYSTYKI SYMULACJI DLA TRANZYCJI:
    private JLabel stepTransLabel;
    private JLabel timeTransLabel;
    private JFormattedTextField inactiveStepsTextBox;
    private JFormattedTextField activeStepsTextBox;
    private JFormattedTextField producingStepsTextBox;
    private JFormattedTextField firedStepsTextBox;
    private JFormattedTextField inactiveTimeTextBox;
    private JFormattedTextField activeTimeTextBox;
    private JFormattedTextField producingTimeTextBox;

    private JCheckBox simStatsStepsCheckbox;
    private JCheckBox simStatsTimeCheckbox;
    private boolean simStatsTransBySteps = true;
    private int simStatsTransRepetitions = 10;
    private int simStatsTransSteps = 10000;
    private double simStatsTransTime = 5000.0;
    private boolean repetitions = false;

    SimulatorGlobals ownSettings = new SimulatorGlobals();

    /**
     * Konstruktor do tworzenia okna właściwości miejsca.
     * @param place PlaceXTPN - obiekt miejsca
     * @param parent JFrame - okno wywołujące
     */
    public HolmesNodeInfoXTPN(PlaceXTPN place, ElementLocation eloc, JFrame parent) {
        overlord = GUIManager.getDefaultGUIManager();
        parentFrame = parent;
        this.thePlace = place;
        this.eLocation = eloc;
        setTitle("Node: "+place.getName());
        setBackground(Color.WHITE);
        initiateSimGlobals();
        initializeCommon(place);

        JPanel main = new JPanel(new BorderLayout()); //główny panel okna
        add(main);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("XTPN Place Info", Tools.getResIcon16("/icons/nodeViewer/tab1.png"), initializePlaceInfo(), "General information about XTPN place");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab("Additional Data", Tools.getResIcon16("/icons/nodeViewer/tab3.png"), initializePlaceInvPanel(), "Additional data");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.setBackgroundAt(0, Color.WHITE);
        tabbedPane.setBackgroundAt(1, Color.WHITE);

        setFieldStatus(true);
        main.add(tabbedPane);
    }

    /**
     * Konstruktor do tworzenia okna właściwości tranzycji.
     * @param transition TransitionXTPN - obiekt tranzycji
     * @param parent JFrame - okno wywołujące
     */
    public HolmesNodeInfoXTPN(TransitionXTPN transition, ElementLocation eloc, JFrame parent) {
        overlord = GUIManager.getDefaultGUIManager();
        parentFrame = parent;
        this.theTransition = transition;
        this.eLocation = eloc;
        setTitle("Node: "+transition.getName());
        setBackground(Color.WHITE);
        initiateSimGlobals();
        initializeCommon(transition);

        JPanel main = new JPanel(new BorderLayout()); //główny panel okna
        add(main);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("XTPN Transition Info", Tools.getResIcon16("/icons/nodeViewer/tab1.png"), initializeTransitionInfo(), "General information about XTPN transition");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab("Additional data", Tools.getResIcon16("/icons/nodeViewer/tab3.png"), initializeTransInvPanel(), "Additional data");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.setBackgroundAt(0, Color.WHITE);
        tabbedPane.setBackgroundAt(1, Color.WHITE);

        setFieldStatus(false);
        main.add(tabbedPane);
    }

    private void initiateSimGlobals() {
        simSteps = 10000; //ile kroków symulacji
        repeated = 1; //ile powtórzeń (dla kroków)
        rep_succeed = 1;
        simulateTime = false; //czy wykres czasowy na osi X dla miejsc
        simTimeLength = 5000.0;

        ownSettings.setSimSteps_XTPN(simSteps);
        ownSettings.setSimTime_XTPN(simTimeLength);
        ownSettings.setSimRepetitions_XTPN(repeated);
        ownSettings.setTimeSimulationStatus_XTPN(simulateTime);
    }

    /**
     * Metoda agregująca główne, wspólne elementy interfejsu miejsc/tranzycji.
     */
    private void initializeCommon(Node node) {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (181278513) | Exception:  "+ex.getMessage(), "error", true);
        }

        if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != GraphicalSimulator.SimulatorMode.STOPPED)
            mainSimulatorActive = true;
        if(overlord.getWorkspace().getProject().isSimulationActive()) {
            mainSimulatorActive = true;
        }

        parentFrame.setEnabled(false);
        setResizable(false);
        setLocation(20, 20);
        if(node instanceof PlaceXTPN) {
            setSize(new Dimension(800, 600));
        } else { //tranzycja
            setSize(new Dimension(800, 750));
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {parentFrame.setEnabled(true);
            }
        });

        this.setVisible(true);
    }

    /**
     * Metoda odpowiedzialna za elementy interfejsu właściwości dla miejsca sieci.
     */
    private JPanel initializePlaceInfo() {
        mainInfoPanel = new JPanel(null);
        mainInfoPanel.setBounds(0, 0, 800, 610);
        mainInfoPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(mPanelX, mPanelY, mainInfoPanel.getWidth()-18, 160);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Structural data:"));

        int infPanelX = 10;
        int infPanelY = 20;

        //************************* NEWLINE *************************

        JLabel labelID = new JLabel("ID:");
        labelID.setBounds(infPanelX, infPanelY, 20, 20);
        infoPanel.add(labelID);

        int id = overlord.getWorkspace().getProject().getPlaces().indexOf(thePlace);
        JFormattedTextField idTextBox = new JFormattedTextField(id);
        idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
        idTextBox.setEditable(false);
        infoPanel.add(idTextBox);

        JLabel labelName = new JLabel("Name:");
        labelName.setBounds(infPanelX+60, infPanelY, 40, 20);
        infoPanel.add(labelName);

        DefaultFormatter format = new DefaultFormatter();
        format.setOverwriteMode(false);
        JFormattedTextField nameField = new JFormattedTextField(format);
        nameField.setLocation(infPanelX+100, infPanelY);
        nameField.setSize(350, 20);
        nameField.setValue(thePlace.getName());
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                GUIManager.getDefaultGUIManager().log("Error (611156405) | Exception:  "+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            thePlace.setName(newName);
            action.repaintGraphPanel(thePlace);
        });
        infoPanel.add(nameField);

        JLabel commmentLabel = new JLabel("Comments:", JLabel.LEFT);
        commmentLabel.setBounds(infPanelX+460, infPanelY-22, 100, 20);
        infoPanel.add(commmentLabel);

        JTextArea commentField = new JTextArea(thePlace.getComment());
        commentField.setLineWrap(true);
        commentField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newComment = "";
                if(field != null)
                    newComment = field.getText();

                thePlace.setComment(newComment);
            }
        });
        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+460, infPanelY, 300, 70);
        infoPanel.add(creationPanel);
        mainInfoPanel.add(infoPanel);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel portalLabel = new JLabel("Portal:");
        portalLabel.setBounds(infPanelX, infPanelY, 40, 20);
        infoPanel.add(portalLabel);

        String port = "no";
        if(thePlace.isPortal())
            port = "yes";

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+40, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        int inTrans = 0;
        int outTrans = 0;
        for (ElementLocation el : thePlace.getElementLocations()) {
            inTrans += el.getInArcs().size(); //tyle tranzycji kieruje tutaj łuk
            outTrans += el.getOutArcs().size();
        }

        JLabel inTransLabel = new JLabel("Input transitions:");
        inTransLabel.setBounds(infPanelX+60, infPanelY, 120, 20);
        infoPanel.add(inTransLabel);

        JFormattedTextField inTransTextBox = new JFormattedTextField(inTrans);
        inTransTextBox.setBounds(infPanelX+160, infPanelY, 25, 20);
        inTransTextBox.setEditable(false);
        infoPanel.add(inTransTextBox);

        JLabel outTransLabel = new JLabel("Output transitions:");
        outTransLabel.setBounds(infPanelX+190, infPanelY, 120, 20);
        infoPanel.add(outTransLabel);

        JFormattedTextField outTransTextBox = new JFormattedTextField(outTrans);
        outTransTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        outTransTextBox.setEditable(false);
        infoPanel.add(outTransTextBox);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel gammaModeInfoLabel = new JLabel("Time mode:");
        gammaModeInfoLabel.setBounds(infPanelX, infPanelY, 80, 20);
        infoPanel.add(gammaModeInfoLabel);

        buttonGammaMode = new HolmesRoundedButton("<html>Gamma: ON</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonGammaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonGammaMode.setName("gammaButton1");
        buttonGammaMode.setBounds(infPanelX+80, infPanelY, 100, 25);
        buttonGammaMode.setFocusPainted(false);
        if(thePlace.isGammaModeActive()) {
            buttonGammaMode.setNewText("<html>Gamma: ON</html>");
            buttonGammaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonGammaMode.setNewText("<html>Gamma: OFF</html>");
            buttonGammaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonGammaMode.addActionListener(e -> {
            action.buttonGammaModeSwitch(e, thePlace, tokensWindowButton, gammaVisibilityButton);
            action.reselectElement(eLocation);
        });
        infoPanel.add(buttonGammaMode);

        JLabel gammaVisInfoLabel = new JLabel("Visibility:");
        gammaVisInfoLabel.setBounds(infPanelX+190, infPanelY, 70, 20);
        infoPanel.add(gammaVisInfoLabel);

        gammaVisibilityButton = new HolmesRoundedButton("<html>\u03B3:visible</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        gammaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        gammaVisibilityButton.setName("gammaVisButton1");
        gammaVisibilityButton.setBounds(infPanelX+250, infPanelY, 100, 25);
        gammaVisibilityButton.setFocusPainted(false);
        if(thePlace.isGammaModeActive()) {
            if (thePlace.isGammaRangeVisible()) {
                gammaVisibilityButton.setNewText("<html>\u03B3:visible<html>");
                gammaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                gammaVisibilityButton.setNewText("<html>\u03B3:hidden<html>");
                gammaVisibilityButton.repaintBackground("amber_bH3_press_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            //gammaVisibilityButton.setNewText("<html>\u03B3: Hidden<html>");
            //gammaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            gammaVisibilityButton.setEnabled(false);
        }
        gammaVisibilityButton.addActionListener(e -> {
            action.gammaVisButtonSwitch(e, thePlace);
            action.reselectElement(eLocation);
        });
        infoPanel.add(gammaVisibilityButton);


        //************************* NEWLINE *************************
        infPanelY += 30;
        //************************* NEWLINE *************************

        // XTPN-place  Zakresy gamma:
        JLabel minMaxLabel = new JLabel("\u03B3 (min/max): ", JLabel.LEFT);
        minMaxLabel.setBounds(infPanelX, infPanelY, 80, 20);
        infoPanel.add(minMaxLabel);

        // format danych gamma do 6 miejsc po przecinku
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(thePlace.getFractionForPlaceXTPN());
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Double example = 3.14;

        gammaMinTextField = new JFormattedTextField(formatter);
        gammaMinTextField.setValue(example);
        gammaMinTextField.setValue(thePlace.getGammaMinValue());
        gammaMinTextField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            if (doNotUpdate)
                return;

            double min = Double.parseDouble(""+field.getValue());

            if( !(SharedActionsXTPN.access().setGammaMinTime(min, thePlace, eLocation) ) ) {
                doNotUpdate = true;
                field.setValue(thePlace.getGammaMinValue());
                doNotUpdate = false;
                overlord.markNetChange();
            }
            doNotUpdate = true;
            gammaMaxTextField.setValue(thePlace.getGammaMaxValue());
            doNotUpdate = false;
            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        gammaMaxTextField = new JFormattedTextField(formatter);
        gammaMaxTextField.setValue(example);
        gammaMaxTextField.setValue(thePlace.getGammaMaxValue());
        gammaMaxTextField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            if (doNotUpdate)
                return;

            double max = Double.parseDouble(""+field.getValue());
            if( !(SharedActionsXTPN.access().setGammaMaxTime(max, thePlace, eLocation) ) ) {
                doNotUpdate = true;
                field.setValue(thePlace.getGammaMaxValue());
                doNotUpdate = false;
                overlord.markNetChange();
            }
            doNotUpdate = true;
            gammaMinTextField.setValue(thePlace.getGammaMinValue());
            doNotUpdate = false;
            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        if(!thePlace.isGammaModeActive()) {
            gammaMinTextField.setEnabled(false);
            gammaMaxTextField.setEnabled(false);
        }

        gammaMinTextField.setBounds(infPanelX+80, infPanelY, 90, 20);
        infoPanel.add(gammaMinTextField);
        JLabel slash1 = new JLabel(" / ", JLabel.LEFT);
        slash1.setBounds(infPanelX+170, infPanelY, 15, 20);
        infoPanel.add(slash1);
        gammaMaxTextField.setBounds(infPanelX+190, infPanelY, 90, 20);
        infoPanel.add(gammaMaxTextField);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel tokenInfoLabel = new JLabel("Tokens:");
        tokenInfoLabel.setBounds(infPanelX, infPanelY, 90, 20);
        infoPanel.add(tokenInfoLabel);

        tokensWindowButton = new HolmesRoundedButton("<html>Token window</html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        tokensWindowButton.setMargin(new Insets(0, 0, 0, 0));
        tokensWindowButton.setBounds(infPanelX+80, infPanelY, 100, 25);
        if(!thePlace.isGammaModeActive()) {
            tokensWindowButton.setEnabled(false);
        }
        tokensWindowButton.addActionListener(actionEvent -> new HolmesXTPNtokens(thePlace, this, thePlace.accessMultiset(), thePlace.isGammaModeActive()));
        infoPanel.add(tokensWindowButton);


        JLabel tokenNumberInfoLabel = new JLabel("Current number:");
        tokenNumberInfoLabel.setBounds(infPanelX+190, infPanelY, 120, 20);
        infoPanel.add(tokenNumberInfoLabel);

        tokensTextBox = new JFormattedTextField("0");
        tokensTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        tokensTextBox.setEditable(false);
        infoPanel.add(tokensTextBox);
        printTokenNumber();

        JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
        chartMainPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-18, 295);
        chartMainPanel.add(createChartPanel(thePlace), BorderLayout.CENTER);
        chartMainPanel.setBackground(Color.WHITE);
        mainInfoPanel.add(chartMainPanel);

        JPanel chartButtonPanel = panelButtonsPlace(infoPanel.getHeight() + chartMainPanel.getHeight()); //dolny panel przycisków
        mainInfoPanel.add(chartButtonPanel);

        try {
            if(overlord.getWorkspace().getProject().getTransitions().size() > 0
                    && overlord.getWorkspace().getProject().getPlaces().size() > 0) {

                fillPlaceDynamicData(chartMainPanel);
            }
        } catch (Exception ex) {
            overlord.log("Error while trying to simulate and compute place data.", "error", true);
        }
        return mainInfoPanel;
    }

    private JPanel initializePlaceInvPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, 600, 470);
        panel.setBackground(Color.WHITE);

        //int posX = 20;
        //int posY = 20;
        //panel.add(progressBar);
        return panel;
    }

    /**
     * Metoda tworzy dolny panel / pasek przycisków okna miejsc.
     * @param y (<b>int</b>) współrzędna pionowa panelu.
     * @return (<b>JPanel</b>) - panel dolnych przycisków.
     */
    private JPanel panelButtonsPlace(int y) {
        JPanel chartButtonPanel = new JPanel(null);
        chartButtonPanel.setBounds(0, y, mainInfoPanel.getWidth()-18, 80);
        chartButtonPanel.setBorder(BorderFactory.createTitledBorder("Simulation options:"));
        chartButtonPanel.setBackground(Color.WHITE);

        int chartX = 5;
        //int chartY_1st = 10;
        int chartY_2nd = 25;

        HolmesRoundedButton acqDataButton = new HolmesRoundedButton("<html>Simulate</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        acqDataButton.setBounds(chartX, chartY_2nd, 110, 25);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
        acqDataButton.addActionListener(actionEvent -> {
            if(overlord.getWorkspace().getProject().isSimulationActive()) {
                JOptionPane.showMessageDialog(null, "Holmes simulator is running. Please wait or stop it manually first.", "Simulator active",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                acquireNewPlaceData();
            }
        });
        chartButtonPanel.add(acqDataButton);

        //chartY_2nd += 20;

        simPlaceStepsCheckbox = new JCheckBox("Steps:");
        simPlaceStepsCheckbox.setBounds(chartX+120, chartY_2nd, 70, 20);
        simPlaceStepsCheckbox.setSelected(!simulateTime);
        simPlaceStepsCheckbox.setBackground(Color.WHITE);
        simPlaceStepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            simulateTime = !box.isSelected();

            doNotUpdate = true;
            simPlaceTimeCheckbox.setSelected(simulateTime);
            doNotUpdate = false;
        });
        chartButtonPanel.add(simPlaceStepsCheckbox);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 100000000, 10000);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(chartX +190, chartY_2nd, 80, 25);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simSteps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsSpinner);

        simPlaceTimeCheckbox = new JCheckBox("Time:");
        simPlaceTimeCheckbox.setBounds(chartX +120, chartY_2nd+25, 70, 25);
        simPlaceTimeCheckbox.setSelected(simulateTime);
        simPlaceTimeCheckbox.setBackground(Color.WHITE);
        simPlaceTimeCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            simulateTime = box.isSelected();
            doNotUpdate = true;
            simPlaceStepsCheckbox.setSelected(!simulateTime);
            doNotUpdate = false;
        });
        chartButtonPanel.add(simPlaceTimeCheckbox);

        SpinnerModel simTimeLengthSpinnerModel = new SpinnerNumberModel(simTimeLength, 0, 1000000, 5000);
        JSpinner simTimeLengthSpinner = new JSpinner(simTimeLengthSpinnerModel);
        simTimeLengthSpinner.setBounds(chartX +190, chartY_2nd+25, 80, 25);
        simTimeLengthSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            double tmp = (double)spinner.getValue();
            simTimeLength = (int) tmp;
        });
        chartButtonPanel.add(simTimeLengthSpinner);

        simPlaceRepsCheckbox = new JCheckBox("Repetitions:");
        simPlaceRepsCheckbox.setBounds(chartX +280, chartY_2nd, 100, 25);
        simPlaceRepsCheckbox.setSelected(simulateTime);
        simPlaceRepsCheckbox.setBackground(Color.WHITE);
        simPlaceRepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            simPlaceReps = box.isSelected();
        });
        chartButtonPanel.add(simPlaceRepsCheckbox);

        SpinnerModel simStepsRepeatedSpinnerModel = new SpinnerNumberModel(repeated, 1, 100, 10);
        JSpinner simStepsRepeatedSpinner = new JSpinner(simStepsRepeatedSpinnerModel);
        simStepsRepeatedSpinner.setBounds(chartX +380, chartY_2nd, 60, 25);
        simStepsRepeatedSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            repeated = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsRepeatedSpinner);


        JLabel label1 = new JLabel("Show:");
        label1.setBounds(chartX+450, chartY_2nd+5, 50, 15);
        chartButtonPanel.add(label1);

        final JComboBox<String> simMode = new JComboBox<String>(new String[] {"Steps", "Time"});
        simMode.setBounds(chartX+490, chartY_2nd, 100, 25);
        simMode.setSelectedIndex(0);
        simMode.setMaximumRowCount(6);
        simMode.addActionListener(actionEvent -> {
            placeChartType = simMode.getSelectedIndex();
            showPlaceChart();
        });
        chartButtonPanel.add(simMode);

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
            title.setBackgroundPaint(Color.white);
            title.setFont(new Font("Dialog", Font.PLAIN, 20));
            title.setExpandToFitSpace(true);
            title.setPaint(Color.red);
            title.setText("Chart unavailable, main simulator is active.");
        }
    }

    /**
     * Metoda aktywuje symulator dla jednej tranzycji w ustalonym wcześniej trybie i dla wcześniej
     * ustalonej liczby kroków. Testy są powtarzane ustaloną liczbę razy. Wyniki zapisuje na wykresie.
     */
    private void acquireNewPlaceData() {
        StateSimulatorXTPN ss = new StateSimulatorXTPN();

        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
        ownSettings.setSimSteps_XTPN( simSteps );
        ownSettings.setSimTime_XTPN( simTimeLength );
        ownSettings.setTimeSimulationStatus_XTPN(simulateTime);

        ss.initiateSim(ownSettings);

        ArrayList<ArrayList<Double>> firstDataVectors = ss.simulateNetSinglePlace(ownSettings, thePlace);
        ArrayList<ArrayList<Double>> tmpDataMatrix = new ArrayList<>();
        tmpDataMatrix.add(firstDataVectors.get(0));

        timeVectorPlaces = new ArrayList<>(firstDataVectors);
        stepsVectorPlaces = new ArrayList<>(firstDataVectors.get(0));

        //timeVectorPlaces
        int problemCounter = 0;
        for(int i=1; i<repeated; i++) {
            //ss.clearData();
            ArrayList<ArrayList<Double>> newData = ss.simulateNetSinglePlace(ownSettings, thePlace);
            if(newData.get(0).size() < tmpDataMatrix.get(0).size()) { //powtórz test, zły rozmiar danych
                problemCounter++;
                i--;
                if(problemCounter == 10) {
                    overlord.log("Unable to gather "+repeated+" data vectors (places) of same size. "
                            + "State simulator cannot proceed "+simSteps+ " steps. First pass had: "
                            + tmpDataMatrix.get(0).size() +" steps.", "error", true);
                    break;
                }
            } else { //ok, taki sam lub dłuższy
                rep_succeed++;
                tmpDataMatrix.add(newData.get(0));
            }
        }

        //dodaj do siebie
        if(repeated > 1) {
            stepsVectorPlaces.clear();
            //ArrayList<Double> dataDVector = new ArrayList<Double>();
            for(int i=0; i<repeated; i++) {
                if(i==0) {
                    stepsVectorPlaces.addAll(tmpDataMatrix.get(0));
                } else {
                    for(int j=0; j<tmpDataMatrix.get(i).size(); j++) {
                        double oldval = stepsVectorPlaces.get(j);
                        oldval += tmpDataMatrix.get(i).get(j);
                        stepsVectorPlaces.set(j, oldval);
                    }
                }
            }
        }

        showPlaceChart();
    }

    /**
     * Metoda odpowiedzialna za pokazanie odpowiednich danych na wykresie miejsc. Zakładamy, że na początku
     * zostaną wygenerowane wektory stepsVectorPlaces oraz timeVectorPlaces, więc zależnie od ustawień,
     * wyświetli liczbę tokenów w każdym kroku / po czasie tau symulacji.
     */
    private void showPlaceChart() {

        //TODO: wykres miejsc

        dynamicsSeriesDataSet.removeAllSeries();
        XYSeries series = new XYSeries("Number of tokens");

        if(placeChartType == 0) {
            if(repeated != 1) {
                for(int step=0; step<stepsVectorPlaces.size(); step++) {
                    double value = stepsVectorPlaces.get(step);
                    value /= rep_succeed;
                    series.add(step, value);
                }
            } else {
                if(stepsVectorPlaces != null) {
                    double sumTokens = 0.0;
                    int interval = 0;
                    for(int step=0; step<stepsVectorPlaces.size(); step++) {
                        double value = stepsVectorPlaces.get(step);
                        sumTokens += value;
                        if(interval++ == 50) {
                            sumTokens /= 50;
                            series.add(step, (int)sumTokens);
                            sumTokens = 0;
                            interval = 0;
                        }
                        //series.add(step, (int)value);
                    }
                }
            }
            dynamicsSeriesDataSet.addSeries(series);
        } else { //wykres czasowy
            if(timeVectorPlaces != null) {
                for(int step=0; step<timeVectorPlaces.get(0).size(); step++) {
                    double value = timeVectorPlaces.get(0).get(step);
                    double time = timeVectorPlaces.get(1).get(step);
                    series.add(time, value);
                }
            }
            dynamicsSeriesDataSet.addSeries(series);
        }
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

        boolean showLegend = true;
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

    public void printTokenNumber() {
        int tokens;
        if(thePlace.isGammaModeActive()) {
            tokens = thePlace.accessMultiset().size();
        } else {
            tokens = thePlace.getTokensNumber();
        }

        tokensTextBox.setText(""+tokens);
    }




    //********************************************************************************************
    //********************************************************************************************
    //************************************               *****************************************
    //************************************   TRANZYCJE   *****************************************
    //************************************               *****************************************
    //********************************************************************************************
    //********************************************************************************************

    private JPanel initializeTransInvPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, 600, 480);
        panel.setBackground(Color.WHITE);
        return panel;
    }

    /**
     * Metoda odpowiedzialna za elementy interfejsu właściwości dla tranzycji sieci.
     */
    private JPanel initializeTransitionInfo() {
        mainInfoPanel = new JPanel(null);
        mainInfoPanel.setBounds(0, 0, 800, 680);
        mainInfoPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(mPanelX, mPanelY, mainInfoPanel.getWidth()-18, 185);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Structural data:"));
        infoPanel.setBackground(Color.WHITE);

        int infPanelX = 10;
        int infPanelY = 20;

        //************************* NEWLINE *************************

        JLabel labelID = new JLabel("ID:");
        labelID.setBounds(infPanelX, infPanelY, 20, 20);
        infoPanel.add(labelID);

        int id = overlord.getWorkspace().getProject().getTransitions().indexOf(theTransition);
        JFormattedTextField idTextBox = new JFormattedTextField(id);
        idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
        idTextBox.setEditable(false);
        infoPanel.add(idTextBox);

        JLabel labelName = new JLabel("Name:");
        labelName.setBounds(infPanelX+60, infPanelY, 40, 20);
        infoPanel.add(labelName);

        DefaultFormatter format = new DefaultFormatter();
        format.setOverwriteMode(false);
        JFormattedTextField nameField = new JFormattedTextField(format);
        nameField.setLocation(infPanelX+100, infPanelY);
        nameField.setSize(350, 20);
        nameField.setValue(theTransition.getName());
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                GUIManager.getDefaultGUIManager().log("Error (212156495) | Exception:  "+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            theTransition.setName(newName);
            action.repaintGraphPanel(theTransition);

            //action.parentTableUpdate(parentFrame, newName);
        });
        infoPanel.add(nameField);

        JLabel commmentLabel = new JLabel("Comments:", JLabel.LEFT);
        commmentLabel.setBounds(infPanelX+460, infPanelY-22, 100, 20);
        infoPanel.add(commmentLabel);

        JTextArea commentField = new JTextArea(theTransition.getComment());
        commentField.setLineWrap(true);
        commentField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newComment = "";
                if(field != null)
                    newComment = field.getText();

                theTransition.setComment(newComment);
            }
        });

        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+460, infPanelY, 300, 70);
        infoPanel.add(creationPanel);
        mainInfoPanel.add(infoPanel);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel portalLabel = new JLabel("Portal:");
        portalLabel.setBounds(infPanelX, infPanelY, 40, 20);
        infoPanel.add(portalLabel);

        String port = "no";
        if(theTransition.isPortal())
            port = "yes";

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+40, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        int preP = 0;
        int postP = 0;
        for (ElementLocation el : theTransition.getElementLocations()) {
            preP += el.getInArcs().size(); //tyle miejsc kieruje tutaj łuk
            postP += el.getOutArcs().size();
        }

        JLabel prePlaceLabel = new JLabel("Input places:");
        prePlaceLabel.setBounds(infPanelX+60, infPanelY, 120, 20);
        infoPanel.add(prePlaceLabel);

        JFormattedTextField prePlaceTextBox = new JFormattedTextField(preP);
        prePlaceTextBox.setBounds(infPanelX+160, infPanelY, 25, 20);
        prePlaceTextBox.setEditable(false);
        infoPanel.add(prePlaceTextBox);

        JLabel postPlaceLabel = new JLabel("Output places:");
        postPlaceLabel.setBounds(infPanelX+190, infPanelY, 120, 20);
        infoPanel.add(postPlaceLabel);

        JFormattedTextField postPlaceTextBox = new JFormattedTextField(postP);
        postPlaceTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        postPlaceTextBox.setEditable(false);
        infoPanel.add(postPlaceTextBox);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel timeModesInfoLabel = new JLabel("Time modes:");
        timeModesInfoLabel.setBounds(infPanelX, infPanelY, 80, 20);
        infoPanel.add(timeModesInfoLabel);

        buttonAlphaMode = new HolmesRoundedButton("<html>Alpha: ON</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonAlphaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonAlphaMode.setName("alphaButton1");
        buttonAlphaMode.setBounds(infPanelX+80, infPanelY, 100, 25);
        buttonAlphaMode.setFocusPainted(false);
        if(theTransition.isAlphaModeActive()) {
            buttonAlphaMode.setNewText("<html>Alpha: ON</html>");
            buttonAlphaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonAlphaMode.setNewText("<html>Alpha: OFF</html>");
            buttonAlphaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonAlphaMode.addActionListener(e -> {

            doNotUpdate = true;
            SharedActionsXTPN.access().buttonAlphaSwitchMode(e, theTransition, this, tauVisibilityButton, buttonClassXTPNmode, alphaMaxTextField, eLocation);
            doNotUpdate = false;

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(buttonAlphaMode);

        JLabel alphaVisInfoLabel = new JLabel("Visibility:");
        alphaVisInfoLabel.setBounds(infPanelX+190, infPanelY, 70, 20);
        infoPanel.add(alphaVisInfoLabel);

        alphaVisibilityButton = new HolmesRoundedButton("<html>\u03B1: Visible</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        alphaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        alphaVisibilityButton.setName("gammaVisButton1");
        alphaVisibilityButton.setBounds(infPanelX+250, infPanelY, 100, 25);
        alphaVisibilityButton.setFocusPainted(false);
        if(theTransition.isAlphaModeActive()) {
            if (theTransition.isAlphaRangeVisible()) {
                alphaVisibilityButton.setNewText("<html>\u03B1: Visible<html>");
                alphaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                alphaVisibilityButton.setNewText("<html>\u03B1: Hidden<html>");
                alphaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            alphaVisibilityButton.setNewText("<html>\u03B1: Hidden<html>");
            alphaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            alphaVisibilityButton.setEnabled(false);
        }
        alphaVisibilityButton.addActionListener(e -> {
            HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
            if (theTransition.isAlphaRangeVisible()) { //wyłączamy
                theTransition.setAlphaRangeVisibility(false);
                button.setNewText("<html>\u03B1: Hidden<html>");
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { // włączamy
                theTransition.setAlphaRangeVisibility(true);
                button.setNewText("<html>\u03B1: Visible<html>");
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
            GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(alphaVisibilityButton);

        //************************* NEWLINE *************************
        infPanelY += 30;
        //************************* NEWLINE *************************

        buttonBetaMode = new HolmesRoundedButton("<html>Beta: ON</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonBetaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonBetaMode.setName("alphaButton1");
        buttonBetaMode.setBounds(infPanelX+80, infPanelY, 100, 25);
        buttonBetaMode.setFocusPainted(false);
        if(theTransition.isAlphaModeActive()) {
            buttonBetaMode.setNewText("<html>Beta: ON</html>");
            buttonBetaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonBetaMode.setNewText("<html>Beta: OFF</html>");
            buttonBetaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonBetaMode.addActionListener(e -> {
            doNotUpdate = true;
            SharedActionsXTPN.access().buttonBetaSwitchMode(e, theTransition, this, tauVisibilityButton, buttonClassXTPNmode, betaMaxTextField, eLocation);
            doNotUpdate = false;

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(buttonBetaMode);

        JLabel betaVisInfoLabel = new JLabel("Visibility:");
        betaVisInfoLabel.setBounds(infPanelX+190, infPanelY, 70, 20);
        infoPanel.add(betaVisInfoLabel);

        betaVisibilityButton = new HolmesRoundedButton("<html>\u03B2: Visible</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        betaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        betaVisibilityButton.setName("gammaVisButton1");
        betaVisibilityButton.setBounds(infPanelX+250, infPanelY, 100, 25);
        betaVisibilityButton.setFocusPainted(false);
        if(theTransition.isBetaModeActive()) {
            if (theTransition.isBetaRangeVisible()) {
                betaVisibilityButton.setNewText("<html>\u03B2: Visible<html>");
                betaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                betaVisibilityButton.setNewText("<html>\u03B2: Hidden<html>");
                betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            betaVisibilityButton.setNewText("<html>\u03B2: Hidden<html>");
            betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            betaVisibilityButton.setEnabled(false);
        }
        betaVisibilityButton.addActionListener(e -> {
            HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
            if (theTransition.isBetaRangeVisible()) { //wyłączamy
                theTransition.setBetaRangeVisibility(false);
                button.setNewText("<html>\u03B2: Hidden<html>");
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { // włączamy
                theTransition.setBetaRangeVisibility(true);
                button.setNewText("<html>\u03B2: Visible<html>");
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
            GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(betaVisibilityButton);


        JLabel classXTPNInfoLabel = new JLabel("Classical/XTPN:");
        classXTPNInfoLabel.setBounds(infPanelX+360, infPanelY, 120, 20);
        infoPanel.add(classXTPNInfoLabel);

        buttonClassXTPNmode = new HolmesRoundedButton("<html>XTPN</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonClassXTPNmode.setMargin(new Insets(0, 0, 0, 0));
        buttonClassXTPNmode.setName("alphaButton1");
        buttonClassXTPNmode.setBounds(infPanelX+460, infPanelY, 100, 25);
        buttonClassXTPNmode.setFocusPainted(false);
        if(!theTransition.isAlphaModeActive() && !theTransition.isBetaModeActive()) {
            buttonClassXTPNmode.setNewText("<html>Classical<html>");
            buttonClassXTPNmode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        } else { //gdy jeden z trybów włączony
            buttonClassXTPNmode.setNewText("<html>XTPN<html>");
            buttonClassXTPNmode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        }
        buttonClassXTPNmode.addActionListener(e -> {
            if (doNotUpdate)
                return;

            doNotUpdate = true;
            SharedActionsXTPN.access().buttonTransitionToXTPN_classicSwitchMode(e, theTransition, this, alphaMaxTextField, betaMaxTextField, eLocation);
            doNotUpdate = false;

            setFieldStatus(false);
        });
        infoPanel.add(buttonClassXTPNmode);

        JLabel tauVisInfoLabel = new JLabel("Tau:");
        tauVisInfoLabel.setBounds(infPanelX+570, infPanelY, 50, 20);
        infoPanel.add(tauVisInfoLabel);

        tauVisibilityButton = new HolmesRoundedButton("<html>\u03C4: Visible</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        tauVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        tauVisibilityButton.setName("gammaVisButton1");
        tauVisibilityButton.setBounds(infPanelX+600, infPanelY, 100, 25);
        tauVisibilityButton.setFocusPainted(false);
        if(theTransition.isAlphaModeActive() || theTransition.isBetaModeActive()) {
            if (theTransition.isTauTimerVisible()) {
                tauVisibilityButton.setNewText("<html>\u03C4: Visible<html>");
                tauVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                tauVisibilityButton.setNewText("<html>\u03C4: Hidden<html>");
                tauVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            tauVisibilityButton.setEnabled(false);
        }
        tauVisibilityButton.addActionListener(e -> {
            if (doNotUpdate)
                return;
            HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
            if (theTransition.isTauTimerVisible()) { //wyłączamy
                theTransition.setTauTimersVisibility(false);
                button.setNewText("<html>\u03C4: Hidden<html>");
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { //włączamy
                theTransition.setTauTimersVisibility(true);
                button.setNewText("<html>\u03C4: Visible<html>");
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
            }
            GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(tauVisibilityButton);

        //************************* NEWLINE *************************
        infPanelY += 30;
        //************************* NEWLINE *************************


        // XTPN-transition Zakresy alfa:
        JLabel minMaxLabel = new JLabel("\u03B1 (min/max): ", JLabel.LEFT);
        minMaxLabel.setBounds(infPanelX, infPanelY, 80, 20);
        infoPanel.add(minMaxLabel);

        // format danych alfa i beta: do 6 miejsc po przecinku
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(6);
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Double example = 3.14;

        // XTPN-transition alfaMin value
        alphaMinTextField = new JFormattedTextField(formatter);
        alphaMinTextField.setValue(example);
        alphaMinTextField.setValue(theTransition.getAlphaMinValue());
        alphaMinTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }

            double min = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setAlfaMinTime(min, theTransition, eLocation);

            doNotUpdate = true;
            alphaMaxTextField.setValue(theTransition.getAlphaMaxValue());
            field.setValue(theTransition.getAlphaMinValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);

        });

        // alfaMax value
        alphaMaxTextField = new JFormattedTextField(formatter);
        alphaMaxTextField.setValue(example);
        alphaMaxTextField.setValue(theTransition.getAlphaMaxValue());
        alphaMaxTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }

            double max = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setAlfaMaxTime(max, theTransition, eLocation);

            doNotUpdate = true;
            alphaMinTextField.setValue(theTransition.getAlphaMinValue());
            field.setValue(theTransition.getAlphaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        if(!theTransition.isAlphaModeActive()) {
            alphaMinTextField.setEnabled(false);
            alphaMaxTextField.setEnabled(false);
        }

        alphaMinTextField.setBounds(infPanelX+80, infPanelY, 90, 20);
        infoPanel.add(alphaMinTextField);
        JLabel slash1 = new JLabel(" / ", JLabel.LEFT);
        slash1.setBounds(infPanelX+170, infPanelY, 15, 20);
        infoPanel.add(slash1);
        alphaMaxTextField.setBounds(infPanelX+190, infPanelY, 90, 20);
        infoPanel.add(alphaMaxTextField);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        // XTPN-transition zakresy beta:
        JLabel betaLabel = new JLabel("\u03B2 (min/max): ", JLabel.LEFT);
        betaLabel.setBounds(infPanelX, infPanelY, 80, 20);
        infoPanel.add(betaLabel);

        // XTPN-transition betaMin value
        betaMinTextField = new JFormattedTextField(formatter);
        betaMinTextField.setValue(example);
        betaMinTextField.setValue(theTransition.getBetaMinValue());
        betaMinTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }

            double min = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setBetaMinTime(min, theTransition, eLocation);

            doNotUpdate = true;
            field.setValue(theTransition.getBetaMinValue());
            betaMaxTextField.setValue(theTransition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        // XTPN-transition betaMax value
        betaMaxTextField = new JFormattedTextField(formatter);
        betaMaxTextField.setValue(example);
        betaMaxTextField.setValue(theTransition.getBetaMaxValue());
        betaMaxTextField.addPropertyChangeListener("value", e -> {
            if (doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
            double max = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setBetaMaxTime(max, theTransition, eLocation);

            doNotUpdate = true;
            betaMinTextField.setValue(theTransition.getBetaMinValue());
            field.setValue(theTransition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        if(!theTransition.isBetaModeActive()) {
            betaMinTextField.setEnabled(false);
            betaMaxTextField.setEnabled(false);
        }

        betaMinTextField.setBounds(infPanelX+80, infPanelY, 90, 20);
        infoPanel.add(betaMinTextField);
        JLabel slash3 = new JLabel(" / ", JLabel.LEFT);
        slash3.setBounds(infPanelX+170, infPanelY, 15, 20);
        infoPanel.add(slash3);
        betaMaxTextField.setBounds(infPanelX+190, infPanelY, 90, 20);
        infoPanel.add(betaMaxTextField);

        JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
        chartMainPanel.setBorder(BorderFactory.createTitledBorder("Transition chart"));
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-18, 280);
        chartMainPanel.add(createChartPanel(theTransition), BorderLayout.CENTER);
        chartMainPanel.setBackground(Color.WHITE);
        mainInfoPanel.add(chartMainPanel);

        JPanel chartButtonPanel = panelButtonsTransition(infoPanel.getHeight()+chartMainPanel.getHeight());
        mainInfoPanel.add(chartButtonPanel);

        JPanel simStatsPanel = panelSimStatsTransition(infoPanel.getHeight()+chartMainPanel.getHeight()+chartButtonPanel.getHeight());
        mainInfoPanel.add(simStatsPanel);

        try {
            if(overlord.getWorkspace().getProject().getTransitions().size() > 0
                    && overlord.getWorkspace().getProject().getPlaces().size() > 0) {
                fillTransitionDynamicData(firedStepsTextBox, chartMainPanel, chartButtonPanel);
            }
        } catch (Exception ex) {
            overlord.log("Error while trying to simulate and compute transition data.", "error", true);
        }
        return mainInfoPanel;
    }

    /**
     * Metoda tworzy dolny panel / pasek przycisków okna tranzycji.
     * @param y (<b>int</b>) współrzędna pionowa panelu.
     * @return (<b>JPanel</b>) - panel dolnych przycisków okna tranzycji.
     */
    private JPanel panelButtonsTransition(int y) {
        JPanel chartButtonPanel = new JPanel(null);
        chartButtonPanel.setBounds(0, y, mainInfoPanel.getWidth()-18, 60);
        chartButtonPanel.setBorder(BorderFactory.createTitledBorder("Simulation options:"));
        chartButtonPanel.setBackground(Color.WHITE);

        int chartX = 5;
        int chartY_1st = 10;
        int chartY_2nd = 25;

        HolmesRoundedButton acqDataButton = new HolmesRoundedButton("<html>Simulate</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        acqDataButton.setBounds(chartX, chartY_2nd, 110, 25);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
        acqDataButton.addActionListener(actionEvent -> {
            if(overlord.getWorkspace().getProject().isSimulationActive()) {
                JOptionPane.showMessageDialog(null, "Holmes simulator is running. Please wait or stop it manually first.", "Simulator active",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                getSingleTransitionData();
            }
        });
        chartButtonPanel.add(acqDataButton);

        JLabel labelSteps = new JLabel("Sim. Steps:");
        labelSteps.setBounds(chartX+120, chartY_1st, 70, 15);
        chartButtonPanel.add(labelSteps);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 100000000, 10000);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(chartX+120, chartY_2nd, 80, 25);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simSteps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsSpinner);

        JLabel labelMode = new JLabel("Simulation mode:");
        labelMode.setBounds(chartX+210, chartY_1st, 110, 15);
        chartButtonPanel.add(labelMode);

        final JComboBox<String> simMode = new JComboBox<String>(new String[] {"Steps", "Time"});
        simMode.setBounds(chartX+210, chartY_2nd, 120, 25);
        simMode.setSelectedIndex(0);
        simMode.setMaximumRowCount(6);
        simMode.addActionListener(actionEvent -> {
            transitionChartType = simMode.getSelectedIndex();
            if(statusVectorTransition.size() == 2) {
                showTransitionsChart();
            }
        });
        chartButtonPanel.add(simMode);

        //Second row:
        //chartY_2nd += 25;

        JCheckBox timeSeriesCheckbox = new JCheckBox("Simulate time");
        timeSeriesCheckbox.setBounds(chartX+330, chartY_2nd, 120, 25);
        timeSeriesCheckbox.setSelected(simulateTime);
        timeSeriesCheckbox.setBackground(Color.WHITE);
        timeSeriesCheckbox.addItemListener(e -> {
            JCheckBox box = (JCheckBox) e.getSource();
            simulateTime = box.isSelected();
        });
        chartButtonPanel.add(timeSeriesCheckbox);

        JLabel timeMaxLabel = new JLabel("Sim. max time:");
        timeMaxLabel.setBounds(chartX+450, chartY_1st, 120, 15);
        chartButtonPanel.add(timeMaxLabel);

        SpinnerModel simTimeLengthSpinnerModel = new SpinnerNumberModel(simTimeLength, 0, 1000000, 5000);
        JSpinner simTimeLengthSpinner = new JSpinner(simTimeLengthSpinnerModel);
        simTimeLengthSpinner.setBounds(chartX+450, chartY_2nd, 80, 25);
        simTimeLengthSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            double tmp = (double)spinner.getValue();
            simTimeLength = (int) tmp;
        });
        chartButtonPanel.add(simTimeLengthSpinner);

        return chartButtonPanel;
    }

    /**
     * Tworzy panel informacji o zachowaniu się tranzycji XTPN podczas symulacji.
     * @param y (<b>int</b>) współrzędna pionowa panelu.
     * @return (<b>JPanel</b>) gotowy panel.
     */
    private JPanel panelSimStatsTransition(int y) {
        JPanel resultPanel = new JPanel(null);
        resultPanel.setBounds(0, y, mainInfoPanel.getWidth()-18, 130);
        resultPanel.setBorder(BorderFactory.createTitledBorder("Simulation data:"));
        resultPanel.setBackground(Color.WHITE);

        int positionX = 10;
        int positionY = 30;

        stepTransLabel = new JLabel("Steps:", JLabel.LEFT);
        stepTransLabel.setBounds(positionX+80, positionY-20, 140, 20);
        resultPanel.add(stepTransLabel);

        timeTransLabel = new JLabel("Time:", JLabel.LEFT);
        timeTransLabel.setBounds(positionX+185, positionY-20, 140, 20);
        resultPanel.add(timeTransLabel);

        JLabel inactiveStepsLabel = new JLabel("Inactive:", JLabel.LEFT);
        inactiveStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(inactiveStepsLabel);

        inactiveStepsTextBox = new JFormattedTextField("n/a");
        inactiveStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        inactiveStepsTextBox.setEditable(false);
        resultPanel.add(inactiveStepsTextBox);

        inactiveTimeTextBox = new JFormattedTextField("n/a");
        inactiveTimeTextBox.setBounds(positionX+185, positionY, 110, 20);
        inactiveTimeTextBox.setEditable(false);
        resultPanel.add(inactiveTimeTextBox);

        HolmesRoundedButton acqDataButton = new HolmesRoundedButton("<html>Simulate</html>"
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        acqDataButton.setBounds(positionX+300, positionY, 110, 20);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
        acqDataButton.addActionListener(actionEvent -> {
            if(overlord.getWorkspace().getProject().isSimulationActive()) {
                JOptionPane.showMessageDialog(null, "Holmes simulator is running. Please wait or stop it manually first.", "Simulator active",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                getMultipleTransitionData();
            }
        });
        resultPanel.add(acqDataButton);

        positionY+=23;

        JLabel activeStepsLabel = new JLabel("Active:", JLabel.LEFT);
        activeStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(activeStepsLabel);

        activeStepsTextBox = new JFormattedTextField("n/a");
        activeStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        activeStepsTextBox.setEditable(false);
        resultPanel.add(activeStepsTextBox);

        activeTimeTextBox = new JFormattedTextField("n/a");
        activeTimeTextBox.setBounds(positionX+185, positionY, 110, 20);
        activeTimeTextBox.setEditable(false);
        resultPanel.add(activeTimeTextBox);

        simStatsStepsCheckbox = new JCheckBox("Steps");
        simStatsStepsCheckbox.setBounds(positionX+300, positionY, 70, 20);
        simStatsStepsCheckbox.setSelected(simulateTime);
        simStatsStepsCheckbox.setBackground(Color.WHITE);
        simStatsStepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            simStatsTransBySteps = box.isSelected();
            doNotUpdate = true;
            simStatsTimeCheckbox.setSelected(!simStatsTransBySteps);
            doNotUpdate = false;
        });
        resultPanel.add(simStatsStepsCheckbox);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simStatsTransSteps, 0, 1000000, 5000);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(positionX+400, positionY, 70, 20);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simStatsTransSteps = (int) spinner.getValue();
        });
        resultPanel.add(simStepsSpinner);

        positionY+=23;

        JLabel producingStepsLabel = new JLabel("Producing:", JLabel.LEFT);
        producingStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(producingStepsLabel);

        producingStepsTextBox = new JFormattedTextField("n/a");
        producingStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        producingStepsTextBox.setEditable(false);
        resultPanel.add(producingStepsTextBox);

        producingTimeTextBox = new JFormattedTextField("n/a");
        producingTimeTextBox.setBounds(positionX+185, positionY, 110, 20);
        producingTimeTextBox.setEditable(false);
        resultPanel.add(producingTimeTextBox);

        simStatsTimeCheckbox = new JCheckBox("Time");
        simStatsTimeCheckbox.setBounds(positionX+300, positionY, 70, 20);
        simStatsTimeCheckbox.setSelected(simulateTime);
        simStatsTimeCheckbox.setBackground(Color.WHITE);
        simStatsTimeCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            simStatsTransBySteps = !(box.isSelected());
            doNotUpdate = true;
            simStatsStepsCheckbox.setSelected(simStatsTransBySteps);
            doNotUpdate = false;
        });
        resultPanel.add(simStatsTimeCheckbox);

        SpinnerModel simTimeLengthSpinnerModel = new SpinnerNumberModel(simStatsTransTime, 0, 50000, 1000);
        JSpinner simTimeLengthSpinner = new JSpinner(simTimeLengthSpinnerModel);
        simTimeLengthSpinner.setBounds(positionX+400, positionY, 70, 20);
        simTimeLengthSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simStatsTransTime = (double)spinner.getValue();
        });
        resultPanel.add(simTimeLengthSpinner);

        positionY+=23;

        JLabel producedStepsLabel = new JLabel("Fired:", JLabel.LEFT);
        producedStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(producedStepsLabel);

        firedStepsTextBox = new JFormattedTextField("n/a");
        firedStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        firedStepsTextBox.setEditable(false);
        resultPanel.add(firedStepsTextBox);

        JCheckBox transRepsCheckbox = new JCheckBox("Repetitions:");
        transRepsCheckbox.setBounds(positionX+300, positionY, 100, 20);
        transRepsCheckbox.setSelected(repetitions);
        transRepsCheckbox.setBackground(Color.WHITE);
        transRepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;

            JCheckBox box = (JCheckBox) e.getSource();
            repetitions = box.isSelected();
        });
        resultPanel.add(transRepsCheckbox);

        //JLabel repetitionsStepsLabel = new JLabel("Repetitions:", JLabel.LEFT);
        //repetitionsStepsLabel.setBounds(positionX+300, positionY, 70, 20);
        //resultPanel.add(repetitionsStepsLabel);

        SpinnerModel simRepetitionsSpinnerModel = new SpinnerNumberModel(10, 10, 100, 1);
        JSpinner simRepetitionsSpinner = new JSpinner(simRepetitionsSpinnerModel);
        simRepetitionsSpinner.setBounds(positionX+400, positionY, 70, 20);
        simRepetitionsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simStatsTransRepetitions = (int) spinner.getValue();
        });
        resultPanel.add(simRepetitionsSpinner);

        doNotUpdate = true;
        simStatsStepsCheckbox.setSelected(true);
        doNotUpdate = false;

        return resultPanel;
    }

    /**
     * Metoda wypełnia pola danych dynamicznych dla tranzycji, tj. symuluje 1000 kroków sieci na bazie
     * czego ustala prawdopodobieństwo uruchomienia tranzycji oraz przedstawia wykres dla symulacji.
     * @param avgFiredTextBox (<b>JFormattedTextField</b>) pole z wartością procentową.
     * @param chartMainPanel (<b>JPanel</b>) panel wykresu.
     */
    private void fillTransitionDynamicData(JFormattedTextField avgFiredTextBox, JPanel chartMainPanel,
                                           JPanel chartButtonPanel) {
        if(!mainSimulatorActive) {
            getSingleTransitionData();
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
            title.setText("Chart unavailable, main simulator is active.");
        }
    }

    /**
     * Metoda aktywuje symulator dla jednej tranzycji w ustalonym wcześniej trybie i dla wcześniej
     * ustalonej liczby kroków. Wyniki zapisuje na wykresie, zwraca też wektor danych.
     */
    private void getSingleTransitionData() {
        StateSimulatorXTPN ss = new StateSimulatorXTPN();

        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
        ownSettings.setSimSteps_XTPN( simSteps );
        ownSettings.setSimTime_XTPN( simTimeLength );
        ownSettings.setTimeSimulationStatus_XTPN(simulateTime);
        ss.initiateSim(ownSettings);

        statusVectorTransition = new ArrayList<>( ss.simulateNetSingleTransition(ownSettings, theTransition) );
        if(statusVectorTransition.size() == 2) {
            showTransitionsChart();
        }
    }

    /**
     * Metoda odpowiedzialna za pobranie dokładniejszych danych statystycznych o zachowaniu tranzycji
     * w symulacji - symulacja powtarzana jest pewną liczbę razy a wyniki uśredniane.
     */
    private void getMultipleTransitionData() {
        StateSimulatorXTPN ss = new StateSimulatorXTPN();
        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
        ownSettings.setSimSteps_XTPN( simStatsTransSteps );
        ownSettings.setSimTime_XTPN( simStatsTransTime );
        ownSettings.setTimeSimulationStatus_XTPN( !simStatsTransBySteps );
        ss.initiateSim(ownSettings);

        ArrayList<Double> statsVector = null;
        for(int i = 0; i< simStatsTransRepetitions; i++) {
            ss.restartEngine();

            ArrayList<Double> dataVector = ss.simulateNetSingleTransitionStatistics(ownSettings, theTransition);

            if(i == 0) {
                statsVector = new ArrayList<>(dataVector);
            } else {
                for(int j=0; j<dataVector.size(); j++) { //dodaj kolejne iteracje danych
                    statsVector.set(j, statsVector.get(j) + dataVector.get(j));
                }
            }
        }

        for(int j=0; j<statsVector.size(); j++) { //uśrednij dane z iteracji
            statsVector.set(j, statsVector.get(j) / simStatsTransRepetitions);
        }

        fillStatsFields(statsVector.get(0), statsVector.get(1), statsVector.get(2), statsVector.get(3)
                , statsVector.get(4), statsVector.get(5), statsVector.get(6), statsVector.get(7), statsVector.get(8));
    }

    /**
     * Metoda odpowiedzialna za pokazanie odpowiednich danych na wykresie tranzycji. Zakładamy, że na początku
     * zostaną wygenerowane wektory zawarte w statusVectorTransition.
     */
    private void showTransitionsChart() {
        dynamicsSeriesDataSet.removeAllSeries();
        XYSeries series0 = new XYSeries("Avg. inactive");
        XYSeries series1 = new XYSeries("Avg. active");
        XYSeries series2 = new XYSeries("Avg. producing");
        XYSeries series3 = new XYSeries("Avg. firing");

        if(transitionChartType == 0) { //wykres po krokach
            if(statusVectorTransition != null) {
                double inactive = 0.0;
                double active = 0.0;
                double producing = 0.0;
                double firing = 0.0;
                int interval = 0;
                for(int step=0; step<statusVectorTransition.get(0).size(); step++) {
                    double value = statusVectorTransition.get(0).get(step);
                    if(value == 0.0) {
                        inactive++;
                    } else if(value == 1.0) {
                        active++;
                    } else if(value == 2.0) {
                        producing++;
                    } else {
                        firing++;
                    }

                    if(interval++ == 100) {
                        inactive /= 100;
                        series0.add(step, inactive);
                        active /= 100;
                        series1.add(step, active);
                        producing /= 100;
                        series2.add(step, producing);
                        firing /= 100;
                        series3.add(step, firing);

                        inactive = active = producing = firing = 0.0;
                        interval = 0;
                    }
                    //series.add(step, value);
                }
            }
        } else { //wykres czasowy
            if(statusVectorTransition != null) {
                double simTime = 0.0;
                for(int step=0; step<statusVectorTransition.get(0).size(); step++) {
                    double value = statusVectorTransition.get(0).get(step);
                    simTime += statusVectorTransition.get(1).get(step);
                    series1.add(simTime, value);
                }
            }
        }
        dynamicsSeriesDataSet.addSeries(series0);
        dynamicsSeriesDataSet.addSeries(series1);
        dynamicsSeriesDataSet.addSeries(series2);
        dynamicsSeriesDataSet.addSeries(series3);

        ArrayList<Double> resultVector = statusVectorTransition.get(2);
        fillStatsFields(resultVector.get(0), resultVector.get(1), resultVector.get(2), resultVector.get(3)
                , resultVector.get(4), resultVector.get(5), resultVector.get(6), resultVector.get(7), resultVector.get(8));
    }

    //********************************************************************************************
    //********************************************************************************************
    //********************************************************************************************
    //********************************************************************************************
    //********************************************************************************************

    /**
     * Metoda odpowiedzialna za odpowieni status przycisków rządzących trybami Alfa/Beta tranzycji
     * oraz Gamma dla miejsca.
     * @param isPlace (<b>boolean</b>) jeśli true odświeża dane miejsca, false - tranzycji.
     */
    private void setFieldStatus(boolean isPlace) {
        if(isPlace) {
            if(thePlace.isGammaModeActive()) {
                buttonGammaMode.setNewText("<html>Gamma: ON</html>");
                buttonGammaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                gammaVisibilityButton.setEnabled(true);
                gammaMinTextField.setEnabled(true);
                gammaMaxTextField.setEnabled(true);

                if(thePlace.isGammaRangeVisible()) {
                    gammaVisibilityButton.setNewText("<html>\u03B3:visible<html>");
                    gammaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    gammaVisibilityButton.setNewText("<html>\u03B3:hidden<html>");
                    gammaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
            } else { //GAMMA OFFLINE
                buttonGammaMode.setNewText("<html>Gamma: OFF</html>");
                buttonGammaMode.repaintBackground("paerl_bH1_neutr.png", "paerl_bH2_hover.png", "paerl_bH3_press.png");

                gammaVisibilityButton.setEnabled(false);

                gammaMinTextField.setEnabled(false);
                gammaMaxTextField.setEnabled(false);
            }

            doNotUpdate = true;
            gammaMinTextField.setValue(thePlace.getGammaMinValue());
            gammaMaxTextField.setValue(thePlace.getGammaMaxValue());
            doNotUpdate = false;

        } else { //dla tranzycji
            if(theTransition.isAlphaModeActive()) {
                buttonAlphaMode.setNewText("<html>Alpha: ON</html>");
                buttonAlphaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                buttonClassXTPNmode.setNewText("<html>XTPN<html>");
                buttonClassXTPNmode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                alphaMinTextField.setEnabled(true);
                alphaMaxTextField.setEnabled(true);

                alphaVisibilityButton.setEnabled(true);
                tauVisibilityButton.setEnabled(true);

                if(theTransition.isAlphaRangeVisible()) {
                    alphaVisibilityButton.setNewText("<html>\u03B1: Visible<html>");
                    alphaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    alphaVisibilityButton.setNewText("<html>\u03B1: Hidden<html>");
                    alphaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }

                if(theTransition.isTauTimerVisible()) {
                    tauVisibilityButton.setNewText("<html>\u03C4: Visible<html>");
                    tauVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    tauVisibilityButton.setNewText("<html>\u03C4: Hidden<html>");
                    tauVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
            } else { //ALFA OFFLINE
                buttonAlphaMode.setNewText("<html>Alpha: OFF</html>");
                buttonAlphaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

                alphaMinTextField.setEnabled(false);
                alphaMaxTextField.setEnabled(false);

                alphaVisibilityButton.setEnabled(false);
            }

            if(theTransition.isBetaModeActive()) {
                buttonBetaMode.setNewText("<html>Beta: ON</html>");
                buttonBetaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                buttonClassXTPNmode.setNewText("<html>XTPN<html>");
                buttonClassXTPNmode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                betaMinTextField.setEnabled(true);
                betaMaxTextField.setEnabled(true);

                betaVisibilityButton.setEnabled(true);
                tauVisibilityButton.setEnabled(true);

                if(theTransition.isBetaRangeVisible()) {
                    betaVisibilityButton.setNewText("<html>\u03B2: Visible<html>");
                    betaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    betaVisibilityButton.setNewText("<html>\u03B2: Hidden<html>");
                    betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
                if(theTransition.isTauTimerVisible()) {
                    tauVisibilityButton.setNewText("<html>\u03C4: Visible<html>");
                    tauVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    tauVisibilityButton.setNewText("<html>\u03C4: Hidden<html>");
                    tauVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
            } else { //BETA OFFLINE
                buttonBetaMode.setNewText("<html>Beta: OFF</html>");
                buttonBetaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

                betaMinTextField.setEnabled(false);
                betaMaxTextField.setEnabled(false);

                betaVisibilityButton.setEnabled(false);
            }

            if(!theTransition.isAlphaModeActive() && !theTransition.isBetaModeActive()) { //both offline
                buttonClassXTPNmode.setNewText("<html>Classical<html>");
                buttonClassXTPNmode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

                tauVisibilityButton.setEnabled(false);
            }

            doNotUpdate = true;
            alphaMinTextField.setValue(theTransition.getAlphaMinValue());
            alphaMaxTextField.setValue(theTransition.getAlphaMaxValue());
            betaMinTextField.setValue(theTransition.getBetaMinValue());
            betaMaxTextField.setValue(theTransition.getBetaMaxValue());
            doNotUpdate = false;
        }
    }

    private void fillStatsFields(double realSimulationSteps, double realSimulationTime, double inactiveSteps
            , double activeSteps, double producingSteps, double fireSteps, double inactiveTime
            , double activeTime, double producingTime) {

        stepTransLabel.setText("Steps: "+Tools.cutValue(realSimulationSteps) );
        timeTransLabel.setText("Time: "+Tools.cutValue(realSimulationTime) );

        double tmp = (inactiveSteps / realSimulationSteps) * 100;
        inactiveStepsTextBox.setText((int)inactiveSteps + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (activeSteps / realSimulationSteps) * 100;
        activeStepsTextBox.setText((int)activeSteps + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (producingSteps / realSimulationSteps) * 100;
        producingStepsTextBox.setText((int)producingSteps + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (fireSteps / realSimulationSteps) * 100;
        firedStepsTextBox.setText((int)fireSteps+" "+ " ("+Tools.cutValue(tmp)+"%)");

        tmp = (inactiveTime / realSimulationTime) * 100;
        inactiveTimeTextBox.setText(Tools.cutValue(inactiveTime) + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (activeTime / realSimulationTime) * 100;
        activeTimeTextBox.setText(Tools.cutValue(activeTime) + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (producingTime / realSimulationTime) * 100;
        producingTimeTextBox.setText(Tools.cutValue(producingTime) + " ("+Tools.cutValue(tmp)+"%)");
    }
}
