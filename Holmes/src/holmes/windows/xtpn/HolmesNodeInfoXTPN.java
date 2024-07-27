package holmes.windows.xtpn;

import holmes.analyse.XTPN.AlgorithmsXTPN;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
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
import javax.swing.border.Border;
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
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private PlaceXTPN thePlace;
    private ElementLocation eLocation;
    private HolmesNodeInfoXTPNactions action = new HolmesNodeInfoXTPNactions(this);
    private TransitionXTPN theTransition;
    private boolean doNotUpdate = false;

    private JPanel mainInfoPanel;
    private JPanel secondTabPanel;
    private JFrame parentFrame;
    public boolean mainSimulatorActive = false;
    private XYSeriesCollection dynamicsSeriesDataSet = null;
    private JFreeChart dynamicsChart;

    //simulation variables:
    private int simSteps = 30000; //ile kroków symulacji
    private int repeated = 10; //ile powtórzeń (dla kroków)
    private boolean simulateTime = false; //czy wykres czasowy na osi X dla miejsc
    private double simTimeLength = 5000.0;
    private int placeChartType = 0; //0 - kroki, 1 - czas (oś X)
    private int transitionChartType = 0; //0 - kroki, 1 - czas (oś X)
    ArrayList<Double> stepsVectorPlaces = new ArrayList<>();
    ArrayList<Double> timeVectorPlaces = new ArrayList<>();
    ArrayList<ArrayList<Double>> statusVectorTransition = new ArrayList<>();


    //MIEJSCA:
    private HolmesRoundedButton buttonGammaMode;
    private HolmesRoundedButton gammaVisibilityButton;
    private HolmesRoundedButton tokensWindowButton; //przycisk podokna tokenó XTPN
    private JFormattedTextField tokensTextBox; //liczba tokenów
    private JFormattedTextField gammaMinTextField;
    private JFormattedTextField gammaMaxTextField;

    //SYMULACJE MIEJSCA:
    private HolmesRoundedButton acqDataButton;
    private boolean simPlaceReps = false;
    private int simPlaceNumberOfReps = 5;
    private int simPlaceInterval = 100;

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

    //SYMULACJE TRANZYCJI:
    private JLabel transStatsStepLabel;
    private JLabel transStatsTimeLabel;
    private JFormattedTextField transStatsInactiveStepsTextBox;
    private JFormattedTextField transStatsActiveStepsTextBox;
    private JFormattedTextField transStatsProductionStepsTextBox;
    private JFormattedTextField transStatsFiringStepsTextBox;
    private JFormattedTextField transStatsInactiveTimeTextBox;
    private JFormattedTextField transStatsActiveTimeTextBox;
    private JFormattedTextField transStatsProductionTimeTextBox;

    //Simulation:
    private boolean simTransReps = false;
    private int simTransNumberOfReps = 5;
    private int simTransInterval = 100;

    //Trans statistics:
    private JCheckBox transStatsStepsCheckbox;
    private JCheckBox transStatsTimeCheckbox;
    private boolean transStatsSimulateWithSteps = true;
    private int transStatsRepetitions = 10;
    private int transStatsNumberOfSteps = 10000;
    private double transStatsMaxTime = 5000.0;
    private boolean transStatsReps = false;

    SimulatorGlobals ownSettings = new SimulatorGlobals();

    //XTPN second panel analysis:
    JTextArea placeSecondPanelResults;
    JTextArea transSecondPanelResults;

    /**
     * Konstruktor do tworzenia okna właściwości miejsca.
     * @param place PlaceXTPN - obiekt miejsca
     * @param parent JFrame - okno wywołujące
     */
    public HolmesNodeInfoXTPN(PlaceXTPN place, ElementLocation eloc, JFrame parent) {
        parentFrame = parent;
        this.thePlace = place;
        this.eLocation = eloc;
        setTitle(lang.getText("HNXTPN_entry001title")+" "+place.getName()); //Node: 
        setBackground(Color.WHITE);
        initiateSimGlobals();
        initializeCommon(place);

        JPanel main = new JPanel(new BorderLayout()); //główny panel okna
        add(main);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(lang.getText("HNXTPN_entry002"), Tools.getResIcon16("/icons/nodeViewer/tab1.png") //XTPN place data
                , initializePlaceInfo(), lang.getText("HNXTPN_entry002t"));
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab(lang.getText("HNXTPN_entry003"), Tools.getResIcon16("/icons/nodeViewer/tab3.png") //Analysis
                , initializePlaceSecondPanel(), lang.getText("HNXTPN_entry003t"));
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
        parentFrame = parent;
        this.theTransition = transition;
        this.eLocation = eloc;
        setTitle("Node:"+" "+transition.getName()); //Node:
        setBackground(Color.WHITE);
        initiateSimGlobals();
        initializeCommon(transition);

        JPanel main = new JPanel(new BorderLayout()); //główny panel okna
        add(main);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab(lang.getText("HNXTPN_entry004"), Tools.getResIcon16("/icons/nodeViewer/tab1.png") //XTPN transition data
                , initializeTransitionInfo(), lang.getText("HNXTPN_entry004t"));
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab(lang.getText("HNXTPN_entry005"), Tools.getResIcon16("/icons/nodeViewer/tab3.png") //Analysis
                , initializeTransSecondPanel(), lang.getText("HNXTPN_entry005t"));
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        tabbedPane.setBackgroundAt(0, Color.WHITE);
        tabbedPane.setBackgroundAt(1, Color.WHITE);

        setFieldStatus(false);
        main.add(tabbedPane);
    }

    private void initiateSimGlobals() {
        simSteps = 30000; //ile kroków symulacji
        repeated = 1; //ile powtórzeń (dla kroków)
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
            overlord.log(lang.getText("LOGentry00593exception")+"\n"+ex.getMessage(), "error", true);
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
            setSize(new Dimension(800, 580));
        } else { //tranzycja
            setSize(new Dimension(800, 720));
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
        mainInfoPanel.setBounds(0, 0, 800, 590);
        mainInfoPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(mPanelX, mPanelY, mainInfoPanel.getWidth()-18, 160);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry006"))); //Structural data:

        int infPanelX = 10;
        int infPanelY = 20;

        //************************* NEWLINE *************************

        JLabel labelID = new JLabel(lang.getText("HNXTPN_entry007")); //ID:
        labelID.setBounds(infPanelX, infPanelY, 20, 20);
        infoPanel.add(labelID);

        int id = overlord.getWorkspace().getProject().getPlaces().indexOf(thePlace);
        JFormattedTextField idTextBox = new JFormattedTextField(id);
        idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
        idTextBox.setEditable(false);
        infoPanel.add(idTextBox);

        JLabel labelName = new JLabel(lang.getText("HNXTPN_entry008")); //Name:
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
                overlord.log(lang.getText("LOGentry00594exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            thePlace.setName(newName);
            action.repaintGraphPanel(thePlace);
        });
        infoPanel.add(nameField);

        JLabel commmentLabel = new JLabel(lang.getText("HNXTPN_entry009"), JLabel.LEFT); //Comments:
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

        JLabel portalLabel = new JLabel(lang.getText("HNXTPN_entry010")); //Portal:
        portalLabel.setBounds(infPanelX, infPanelY, 40, 20);
        infoPanel.add(portalLabel);

        String port = lang.getText("no");
        if(thePlace.isPortal())
            port = lang.getText("yes");

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+40, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        int inTrans = 0;
        int outTrans = 0;
        for (ElementLocation el : thePlace.getElementLocations()) {
            inTrans += el.getInArcs().size(); //tyle tranzycji kieruje tutaj łuk
            outTrans += el.getOutArcs().size();
        }

        JLabel inTransLabel = new JLabel(lang.getText("HNXTPN_entry011")); //Input transitions:
        inTransLabel.setBounds(infPanelX+60, infPanelY, 120, 20);
        infoPanel.add(inTransLabel);

        JFormattedTextField inTransTextBox = new JFormattedTextField(inTrans);
        inTransTextBox.setBounds(infPanelX+160, infPanelY, 25, 20);
        inTransTextBox.setEditable(false);
        infoPanel.add(inTransTextBox);

        JLabel outTransLabel = new JLabel(lang.getText("HNXTPN_entry012")); //Output transitions:
        outTransLabel.setBounds(infPanelX+190, infPanelY, 120, 20);
        infoPanel.add(outTransLabel);

        JFormattedTextField outTransTextBox = new JFormattedTextField(outTrans);
        outTransTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        outTransTextBox.setEditable(false);
        infoPanel.add(outTransTextBox);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel gammaModeInfoLabel = new JLabel(lang.getText("HNXTPN_entry013")); //Time mode:
        gammaModeInfoLabel.setBounds(infPanelX, infPanelY, 80, 20);
        infoPanel.add(gammaModeInfoLabel);

        buttonGammaMode = new HolmesRoundedButton(lang.getText("HNXTPN_entry014on") //Gamma: ON
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonGammaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonGammaMode.setName("gammaButton1");
        buttonGammaMode.setBounds(infPanelX+80, infPanelY, 100, 25);
        buttonGammaMode.setFocusPainted(false);
        if(thePlace.isGammaModeActive()) {
            buttonGammaMode.setNewText(lang.getText("HNXTPN_entry014on")); //Gamma: ON
            buttonGammaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonGammaMode.setNewText(lang.getText("HNXTPN_entry014off")); //Gamma: OFF
            buttonGammaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonGammaMode.addActionListener(e -> {
            action.buttonGammaModeSwitch(e, thePlace, tokensWindowButton, gammaVisibilityButton);
            action.reselectElement(eLocation);
        });
        infoPanel.add(buttonGammaMode);

        JLabel gammaVisInfoLabel = new JLabel(lang.getText("HNXTPN_entry015")); //Visibility:
        gammaVisInfoLabel.setBounds(infPanelX+190, infPanelY, 70, 20);
        infoPanel.add(gammaVisInfoLabel);

        gammaVisibilityButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry016vis") //\u03B3:visible
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        gammaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        gammaVisibilityButton.setName("gammaVisButton1");
        gammaVisibilityButton.setBounds(infPanelX+250, infPanelY, 100, 25);
        gammaVisibilityButton.setFocusPainted(false);
        if(thePlace.isGammaModeActive()) {
            if (thePlace.isGammaRangeVisible()) {
                gammaVisibilityButton.setNewText(lang.getText("HNXTPN_entry016vis")); //\u03B3:visible
                gammaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                gammaVisibilityButton.setNewText(lang.getText("HNXTPN_entry016invis"));  //\u03B3:hidden
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
        JLabel minMaxLabel = new JLabel(lang.getText("HNXTPN_entry017"), JLabel.LEFT); //\u03B3 (min/max):
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
                overlord.log(lang.getText("LOGentry00598exception")+"\n"+ex.getMessage(), "error", true);
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
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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
                overlord.log(lang.getText("LOGentry00599exception")+"\n"+ex.getMessage(), "error", true);
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
            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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

        JLabel tokenInfoLabel = new JLabel(lang.getText("HNXTPN_entry018"));
        tokenInfoLabel.setBounds(infPanelX, infPanelY, 90, 20);
        infoPanel.add(tokenInfoLabel);

        tokensWindowButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry019") //Token window
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        tokensWindowButton.setMargin(new Insets(0, 0, 0, 0));
        tokensWindowButton.setBounds(infPanelX+80, infPanelY, 100, 25);
        if(!thePlace.isGammaModeActive()) {
            tokensWindowButton.setEnabled(false);
        }
        tokensWindowButton.addActionListener(actionEvent -> new HolmesXTPNtokens(thePlace, this, thePlace.accessMultiset(), thePlace.isGammaModeActive()));
        infoPanel.add(tokensWindowButton);


        JLabel tokenNumberInfoLabel = new JLabel(lang.getText("HNXTPN_entry020")); //Current number:
        tokenNumberInfoLabel.setBounds(infPanelX+190, infPanelY, 120, 20);
        infoPanel.add(tokenNumberInfoLabel);

        tokensTextBox = new JFormattedTextField("0");
        tokensTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        tokensTextBox.setEditable(false);
        infoPanel.add(tokensTextBox);
        printTokenNumber();

        JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
        chartMainPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry021"))); //Places chart
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-18, 295);
        chartMainPanel.add(createChartPanel(thePlace), BorderLayout.CENTER);
        chartMainPanel.setBackground(Color.WHITE);
        mainInfoPanel.add(chartMainPanel);

        JPanel chartButtonPanel = panelButtonsPlace(infoPanel.getHeight() + chartMainPanel.getHeight()); //dolny panel przycisków
        mainInfoPanel.add(chartButtonPanel);

        try {
            if(!overlord.getWorkspace().getProject().getTransitions().isEmpty()
                    && !overlord.getWorkspace().getProject().getPlaces().isEmpty()) {

                fillPlaceDynamicData(chartMainPanel);
            }
        } catch (Exception ex) {
            overlord.log(lang.getText("LOGentry00595exception")+"\n"+ex.getMessage(), "error", true);
        }
        return mainInfoPanel;
    }

    private JPanel initializePlaceSecondPanel() {
        secondTabPanel = new JPanel(null);
        secondTabPanel.setBounds(0, 0, 800, 590);
        secondTabPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel analP_firstPanel = new JPanel(null);
        analP_firstPanel.setBackground(Color.WHITE);
        analP_firstPanel.setBounds(mPanelX, mPanelY, secondTabPanel.getWidth()-24, 320);
        analP_firstPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry022"))); //XTPN analysis:

        int subPanelX = 10;
        int subPanelY = 20;

        //************************* NEWLINE *************************

        //JLabel labelID = new JLabel("ID:");
        //labelID.setBounds(subPanelX, subPanelY, 20, 20);
        //analP_firstPanel.add(labelID);
        //JFormattedTextField idTextBox = new JFormattedTextField(id);
        //idTextBox.setBounds(subPanelX+20, subPanelY, 30, 20);
        //idTextBox.setEditable(false);
        //analP_firstPanel.add(idTextBox);

        int id = overlord.getWorkspace().getProject().getPlaces().indexOf(thePlace);
        HolmesRoundedButton checkKboundButton = new HolmesRoundedButton("<html>Check boundedness</html>" //Check boundedness
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        checkKboundButton.setMargin(new Insets(0, 0, 0, 0));
        checkKboundButton.setBounds(subPanelX, subPanelY, 130, 32);
        checkKboundButton.addActionListener(actionEvent -> {
            int result = AlgorithmsXTPN.getTokensPerPlace(thePlace, 100, -1, true);
            placeSecondPanelResults.setText("");
            placeSecondPanelResults.append("Place: "+thePlace.getName()+"\n");
            placeSecondPanelResults.append("Tokens per place: "+result+"\n");
        });
        analP_firstPanel.add(checkKboundButton);

        subPanelY+=40;

        placeSecondPanelResults = new JTextArea();
        placeSecondPanelResults.setLineWrap(true);
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(placeSecondPanelResults), BorderLayout.CENTER);
        CreationPanel.setBounds(subPanelX, subPanelY, 755, 120);
        analP_firstPanel.add(CreationPanel);


        secondTabPanel.add(analP_firstPanel);


        JPanel analP_secondPanel = new JPanel(null);
        analP_secondPanel.setBackground(Color.WHITE);
        analP_secondPanel.setBounds(mPanelX, analP_firstPanel.getHeight(), secondTabPanel.getWidth()-24, 200);
        analP_secondPanel.setBorder(BorderFactory.createTitledBorder("Analysis")); //Analysis
        subPanelX = 10;
        subPanelY = 20;

        final JProgressBar progressBar = new JProgressBar();

        progressBar.setBounds(subPanelX, subPanelY, 750, 40);
        progressBar.setBackground(Color.WHITE);
        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Calculations progress"); //Calculations progress
        progressBar.setBorder(border);
        analP_secondPanel.add(progressBar);

        secondTabPanel.add(analP_secondPanel);

        return secondTabPanel;
    }

    /**
     * Metoda tworzy dolny panel / pasek przycisków okna miejsc.
     * @param y (<b>int</b>) współrzędna pionowa panelu.
     * @return (<b>JPanel</b>) - panel dolnych przycisków.
     */
    private JPanel panelButtonsPlace(int y) {
        JPanel chartButtonPanel = new JPanel(null);
        chartButtonPanel.setBounds(0, y, mainInfoPanel.getWidth()-18, 60);
        chartButtonPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry023"))); //Simulation options:
        chartButtonPanel.setBackground(Color.WHITE);

        int positionX = 5;
        int positionY = 30;

        acqDataButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry024") //Simulate
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        acqDataButton.setBounds(positionX, positionY-10, 110, 35);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setToolTipText(lang.getText("HNXTPN_entry024t"));
        acqDataButton.addActionListener(actionEvent -> {
            if(overlord.getWorkspace().getProject().isSimulationActive()) {
                JOptionPane.showMessageDialog(null, lang.getText("HNXTPN_entry025"), lang.getText("HNXTPN_entry025t"),
                        JOptionPane.WARNING_MESSAGE);
            } else {
                acqDataButton.setEnabled(false);
                getPlaceSimpleChartData(simPlaceNumberOfReps);
            }
        });
        chartButtonPanel.add(acqDataButton);

        //chartY_2nd += 20;

        JLabel simPlaceStepLabel = new JLabel(lang.getText("HNXTPN_entry026")); //Steps:
        simPlaceStepLabel.setBounds(positionX+120, positionY-15, 60, 15);
        chartButtonPanel.add(simPlaceStepLabel);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 100000000, 30000);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(positionX +120, positionY, 80, 25);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simSteps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsSpinner);

        JLabel label1 = new JLabel(lang.getText("HNXTPN_entry027")); //Show:
        label1.setBounds(positionX+210, positionY-15, 50, 15);
        chartButtonPanel.add(label1);

        final JComboBox<String> simMode = new JComboBox<String>(new String[] {lang.getText("HNXTPN_entry028op1")
                , lang.getText("HNXTPN_entry028op2")}); //Steps, Time
        simMode.setBounds(positionX+210, positionY, 80, 25);
        simMode.setSelectedIndex(0);
        simMode.setMaximumRowCount(6);
        simMode.addActionListener(actionEvent -> {
            placeChartType = simMode.getSelectedIndex();
            showPlaceChart();
        });
        chartButtonPanel.add(simMode);

        JCheckBox simPlaceRepsCheckbox = new JCheckBox(lang.getText("HNXTPN_entry028")); //Reps:
        simPlaceRepsCheckbox.setBounds(positionX+295, positionY-15, 70, 15);
        simPlaceRepsCheckbox.setSelected(simulateTime);
        simPlaceRepsCheckbox.setBackground(Color.WHITE);
        simPlaceRepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            simPlaceReps = box.isSelected();
        });
        chartButtonPanel.add(simPlaceRepsCheckbox);

        SpinnerModel simStepsRepeatedSpinnerModel = new SpinnerNumberModel(simPlaceNumberOfReps, 1, 100, 10);
        JSpinner simStepsRepeatedSpinner = new JSpinner(simStepsRepeatedSpinnerModel);
        simStepsRepeatedSpinner.setBounds(positionX+300, positionY, 70, 25);
        simStepsRepeatedSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simPlaceNumberOfReps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsRepeatedSpinner);


        JLabel simPlaceIntervalLabel = new JLabel(lang.getText("HNXTPN_entry029")); //Interval:
        simPlaceIntervalLabel.setBounds(positionX+380, positionY-15, 80, 15);
        chartButtonPanel.add(simPlaceIntervalLabel);

        SpinnerModel simIntervalSpinnerModel = new SpinnerNumberModel(simPlaceInterval, 1, 1000, 10);
        JSpinner simIntervalSpinner = new JSpinner(simIntervalSpinnerModel);
        simIntervalSpinner.setBounds(positionX+380, positionY, 60, 25);
        simIntervalSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simPlaceInterval = (int) spinner.getValue();
            showPlaceChart();
        });
        chartButtonPanel.add(simIntervalSpinner);

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
            getPlaceSimpleChartData(1);
        } else {
            chartMainPanel.setEnabled(false);
            TextTitle title = dynamicsChart.getTitle();
            title.setBorder(2, 2, 2, 2);
            title.setBackgroundPaint(Color.white);
            title.setFont(new Font("Dialog", Font.PLAIN, 20));
            title.setExpandToFitSpace(true);
            title.setPaint(Color.red);
            title.setText(lang.getText("HNXTPN_entry030")); //Chart unavailable, main simulator is active.
        }
    }

    /**
     * Metoda aktywuje symulator dla jednej tranzycji w ustalonym wcześniej trybie i dla wcześniej
     * ustalonej liczby kroków. Testy są powtarzane ustaloną liczbę razy. Wyniki zapisuje na wykresie.
     * @param reps (<b>int</b>) liczba powtórzeń.
     */
    private void getPlaceSimpleChartData(int reps) {
        StateSimulatorXTPN ss = new StateSimulatorXTPN();

        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
        ownSettings.setSimSteps_XTPN( simSteps );
        ownSettings.setTimeSimulationStatus_XTPN(false);
        ss.initiateSim(ownSettings);

        if(!simPlaceReps) {
            reps = 1; //override if simPlaceReps = false
        }

        ArrayList<ArrayList<Double>> firstDataVectors = ss.simulateNetSinglePlace(ownSettings, thePlace, reps);
        stepsVectorPlaces = new ArrayList<>(firstDataVectors.get(0));
        timeVectorPlaces = new ArrayList<>(firstDataVectors.get(1));

        acqDataButton.setEnabled(true);
        showPlaceChart();
    }

    /**
     * Metoda odpowiedzialna za pokazanie odpowiednich danych na wykresie miejsc. Zakładamy, że na początku
     * zostaną wygenerowane wektory stepsVectorPlaces oraz timeVectorPlaces, więc zależnie od ustawień,
     * wyświetli liczbę tokenów w każdym kroku / po czasie tau symulacji.
     */
    private void showPlaceChart() {
        dynamicsSeriesDataSet.removeAllSeries();
        XYSeries series = new XYSeries(lang.getText("HNXTPN_entry031")); //Liczba tokenów

        int maxInterval = simPlaceInterval;
        if(10*simPlaceInterval > stepsVectorPlaces.size()) {
            maxInterval = 1;
            overlord.log(lang.getText("HNXTPN_entry032"), "warning", true);
        }

        if(stepsVectorPlaces != null) {
            double sumTokens = 0.0;
            int interval = 0;
            for(int step=0; step<stepsVectorPlaces.size(); step++) {
                double value = stepsVectorPlaces.get(step);
                sumTokens += value;
                if(interval++ == maxInterval) {
                    sumTokens /= maxInterval;
                    if(placeChartType == 0) {
                        series.add(step, (int) sumTokens);
                    } else { //wykres czasowy
                        double time = timeVectorPlaces.get(step);
                        series.add(time, (int) sumTokens);
                    }
                    sumTokens = 0;
                    interval = 0;
                }
            }
        }
        dynamicsSeriesDataSet.addSeries(series);
    }

    /**
     * Metoda tworząca podstawowe elementy wykresu okna.
     * @param node Node - klieknięty wierzchołek
     * @return JPanel - panel komponentów
     */
    JPanel createChartPanel(Node node) {
        String chartTitle = node.getName()+ " "+lang.getText("HNXTPN_entry033");
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
    //************************************               *****************************************
    //************************************   TRANZYCJE   *****************************************
    //************************************               *****************************************
    //********************************************************************************************

    private JPanel initializeTransSecondPanel() {
        secondTabPanel = new JPanel(null);
        secondTabPanel.setBounds(0, 0, 800, 590);
        secondTabPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel analP_firstPanel = new JPanel(null);
        analP_firstPanel.setBackground(Color.WHITE);
        analP_firstPanel.setBounds(mPanelX, mPanelY, secondTabPanel.getWidth()-24, 320);
        analP_firstPanel.setBorder(BorderFactory.createTitledBorder("XTPN analysis:"));

        int subPanelX = 10;
        int subPanelY = 20;

        //************************* NEWLINE *************************

        //JLabel labelID = new JLabel("ID:");
        //labelID.setBounds(subPanelX, subPanelY, 20, 20);
        //analP_firstPanel.add(labelID);
        //JFormattedTextField idTextBox = new JFormattedTextField(id);
        //idTextBox.setBounds(subPanelX+20, subPanelY, 30, 20);
        //idTextBox.setEditable(false);
        //analP_firstPanel.add(idTextBox);

        int id = overlord.getWorkspace().getProject().getTransitions().indexOf(theTransition);
        HolmesRoundedButton checkKboundButton = new HolmesRoundedButton("<html>Check lifeness</html>" //Check lifeness
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        checkKboundButton.setMargin(new Insets(0, 0, 0, 0));
        checkKboundButton.setBounds(subPanelX, subPanelY, 130, 32);
        checkKboundButton.addActionListener(actionEvent -> {
            //ArrayList<Integer> result = AlgorithmsXTPN.getTokensPerPlace(thePlace, 100, -1, false);
            //placeSecondPanelResults.setText("");
            //placeSecondPanelResults.append("Place: "+thePlace.getName()+"\n");
            //placeSecondPanelResults.append("Tokens per place: "+result.get(0).toString()+"\n");
        });
        analP_firstPanel.add(checkKboundButton);

        subPanelY+=40;

        transSecondPanelResults = new JTextArea();
        transSecondPanelResults.setLineWrap(true);
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(transSecondPanelResults), BorderLayout.CENTER);
        CreationPanel.setBounds(subPanelX, subPanelY, 755, 120);
        analP_firstPanel.add(CreationPanel);


        secondTabPanel.add(analP_firstPanel);


        JPanel analP_secondPanel = new JPanel(null);
        analP_secondPanel.setBackground(Color.WHITE);
        analP_secondPanel.setBounds(mPanelX, analP_firstPanel.getHeight(), secondTabPanel.getWidth()-24, 200);
        analP_secondPanel.setBorder(BorderFactory.createTitledBorder("Analysis")); //Analysis
        subPanelX = 10;
        subPanelY = 20;

        final JProgressBar progressBar = new JProgressBar();

        progressBar.setBounds(subPanelX, subPanelY, 750, 40);
        progressBar.setBackground(Color.WHITE);
        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Calculations progress");
        progressBar.setBorder(border);
        analP_secondPanel.add(progressBar);

        secondTabPanel.add(analP_secondPanel);

        return secondTabPanel;
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
        infoPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry034"))); //Structural data:
        infoPanel.setBackground(Color.WHITE);

        int infPanelX = 10;
        int infPanelY = 20;

        //************************* NEWLINE *************************

        JLabel labelID = new JLabel(lang.getText("HNXTPN_entry035")); //ID:
        labelID.setBounds(infPanelX, infPanelY, 20, 20);
        infoPanel.add(labelID);

        int id = overlord.getWorkspace().getProject().getTransitions().indexOf(theTransition);
        JFormattedTextField idTextBox = new JFormattedTextField(id);
        idTextBox.setBounds(infPanelX+20, infPanelY, 30, 20);
        idTextBox.setEditable(false);
        infoPanel.add(idTextBox);
        
        JLabel labelName = new JLabel(lang.getText("HNXTPN_entry036")); //Name:
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
                overlord.log(lang.getText("LOGentry00596exception")+"\n"+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            theTransition.setName(newName);
            action.repaintGraphPanel(theTransition);

            //action.parentTableUpdate(parentFrame, newName);
        });
        infoPanel.add(nameField);

        JLabel commmentLabel = new JLabel(lang.getText("HNXTPN_entry037"), JLabel.LEFT); //Comments:
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

        JLabel portalLabel = new JLabel(lang.getText("HNXTPN_entry038")); //Portal:
        portalLabel.setBounds(infPanelX, infPanelY, 40, 20);
        infoPanel.add(portalLabel);

        String port = lang.getText("no"); //no
        if(theTransition.isPortal())
            port = lang.getText("yes"); //yes

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+40, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        int preP = 0;
        int postP = 0;
        for (ElementLocation el : theTransition.getElementLocations()) {
            preP += el.getInArcs().size(); //tyle miejsc kieruje tutaj łuk
            postP += el.getOutArcs().size();
        }

        JLabel prePlaceLabel = new JLabel(lang.getText("HNXTPN_entry039")); //Input places:
        prePlaceLabel.setBounds(infPanelX+60, infPanelY, 120, 20);
        infoPanel.add(prePlaceLabel);

        JFormattedTextField prePlaceTextBox = new JFormattedTextField(preP);
        prePlaceTextBox.setBounds(infPanelX+160, infPanelY, 25, 20);
        prePlaceTextBox.setEditable(false);
        infoPanel.add(prePlaceTextBox);

        JLabel postPlaceLabel = new JLabel(lang.getText("HNXTPN_entry040")); //Output places:
        postPlaceLabel.setBounds(infPanelX+190, infPanelY, 120, 20);
        infoPanel.add(postPlaceLabel);

        JFormattedTextField postPlaceTextBox = new JFormattedTextField(postP);
        postPlaceTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        postPlaceTextBox.setEditable(false);
        infoPanel.add(postPlaceTextBox);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel timeModesInfoLabel = new JLabel(lang.getText("HNXTPN_entry041")); //Time modes:
        timeModesInfoLabel.setBounds(infPanelX, infPanelY, 80, 20);
        infoPanel.add(timeModesInfoLabel);

        buttonAlphaMode = new HolmesRoundedButton(lang.getText("HNXTPN_entry042on") //Alpha: ON
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonAlphaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonAlphaMode.setName("alphaButton1");
        buttonAlphaMode.setBounds(infPanelX+80, infPanelY, 100, 25);
        buttonAlphaMode.setFocusPainted(false);
        if(theTransition.isAlphaModeActive()) {
            buttonAlphaMode.setNewText(lang.getText("HNXTPN_entry042on")); //Alpha: ON
            buttonAlphaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonAlphaMode.setNewText(lang.getText("HNXTPN_entry042off"));
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

        JLabel alphaVisInfoLabel = new JLabel(lang.getText("HNXTPN_entry043")); //Visibility:
        alphaVisInfoLabel.setBounds(infPanelX+190, infPanelY, 70, 20);
        infoPanel.add(alphaVisInfoLabel);

        alphaVisibilityButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry044vis") //Alpha: Visible
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        alphaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        alphaVisibilityButton.setName("gammaVisButton1");
        alphaVisibilityButton.setBounds(infPanelX+250, infPanelY, 100, 25);
        alphaVisibilityButton.setFocusPainted(false);
        if(theTransition.isAlphaModeActive()) {
            if (theTransition.isAlphaRangeVisible()) {
                alphaVisibilityButton.setNewText(lang.getText("HNXTPN_entry044vis")); //Alpha: Visible
                alphaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                alphaVisibilityButton.setNewText(lang.getText("HNXTPN_entry044invis")); //Alpha: Hidden
                alphaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            alphaVisibilityButton.setNewText(lang.getText("HNXTPN_entry044invis")); //Alpha: Hidden
            alphaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            alphaVisibilityButton.setEnabled(false);
        }
        alphaVisibilityButton.addActionListener(e -> {
            HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
            if (theTransition.isAlphaRangeVisible()) { //wyłączamy
                theTransition.setAlphaRangeVisibility(false);
                button.setNewText(lang.getText("HNXTPN_entry044invis")); //Alpha: Hidden
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            } else { // włączamy
                theTransition.setAlphaRangeVisibility(true);
                button.setNewText(lang.getText("HNXTPN_entry044vis")); //Alpha: Visible
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(alphaVisibilityButton);

        //************************* NEWLINE *************************
        infPanelY += 30;
        //************************* NEWLINE *************************

        buttonBetaMode = new HolmesRoundedButton(lang.getText("HNXTPN_entry045on") //Beta: ON
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonBetaMode.setMargin(new Insets(0, 0, 0, 0));
        buttonBetaMode.setName("alphaButton1");
        buttonBetaMode.setBounds(infPanelX+80, infPanelY, 100, 25);
        buttonBetaMode.setFocusPainted(false);
        if(theTransition.isAlphaModeActive()) {
            buttonBetaMode.setNewText(lang.getText("HNXTPN_entry045on")); //Beta: ON
            buttonBetaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonBetaMode.setNewText(lang.getText("HNXTPN_entry045off")); //Beta: OFF
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

        JLabel betaVisInfoLabel = new JLabel(lang.getText("HNXTPN_entry046")); //Visibility:
        betaVisInfoLabel.setBounds(infPanelX+190, infPanelY, 70, 20);
        infoPanel.add(betaVisInfoLabel);

        betaVisibilityButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry047vis")   //Beta: Visible
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        betaVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        betaVisibilityButton.setName("gammaVisButton1");
        betaVisibilityButton.setBounds(infPanelX+250, infPanelY, 100, 25);
        betaVisibilityButton.setFocusPainted(false);
        if(theTransition.isBetaModeActive()) {
            if (theTransition.isBetaRangeVisible()) {
                betaVisibilityButton.setNewText(lang.getText("HNXTPN_entry047vis")); //Beta: Visible
                betaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                betaVisibilityButton.setNewText(lang.getText("HNXTPN_entry047invis")); //Beta: Hidden
                betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            }
        } else {
            betaVisibilityButton.setNewText(lang.getText("HNXTPN_entry047invis")); //Beta: Hidden
            betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            betaVisibilityButton.setEnabled(false);
        }
        betaVisibilityButton.addActionListener(e -> {
            HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
            if (theTransition.isBetaRangeVisible()) { //wyłączamy
                theTransition.setBetaRangeVisibility(false);
                button.setNewText(lang.getText("HNXTPN_entry047invis")); //Beta: Hidden
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { // włączamy
                theTransition.setBetaRangeVisibility(true);
                button.setNewText(lang.getText("HNXTPN_entry047vis")); //Beta: Visible
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            }
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(betaVisibilityButton);


        JLabel classXTPNInfoLabel = new JLabel(lang.getText("HNXTPN_entry048")); //Classical/XTPN:
        classXTPNInfoLabel.setBounds(infPanelX+360, infPanelY, 120, 20);
        infoPanel.add(classXTPNInfoLabel);

        buttonClassXTPNmode = new HolmesRoundedButton(lang.getText("HNXTPN_entry049") //XTPN
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        buttonClassXTPNmode.setMargin(new Insets(0, 0, 0, 0));
        buttonClassXTPNmode.setName("alphaButton1");
        buttonClassXTPNmode.setBounds(infPanelX+460, infPanelY, 100, 25);
        buttonClassXTPNmode.setFocusPainted(false);
        if(!theTransition.isAlphaModeActive() && !theTransition.isBetaModeActive()) {
            buttonClassXTPNmode.setNewText(lang.getText("HNXTPN_entry050")); //Classical
            buttonClassXTPNmode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        } else { //gdy jeden z trybów włączony
            buttonClassXTPNmode.setNewText(lang.getText("HNXTPN_entry049")); //XTPN
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

        JLabel tauVisInfoLabel = new JLabel(lang.getText("HNXTPN_entry051"));
        tauVisInfoLabel.setBounds(infPanelX+570, infPanelY, 50, 20);
        infoPanel.add(tauVisInfoLabel);

        tauVisibilityButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry052vis") //Tau: Visible
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        tauVisibilityButton.setMargin(new Insets(0, 0, 0, 0));
        tauVisibilityButton.setName("gammaVisButton1");
        tauVisibilityButton.setBounds(infPanelX+600, infPanelY, 100, 25);
        tauVisibilityButton.setFocusPainted(false);
        if(theTransition.isAlphaModeActive() || theTransition.isBetaModeActive()) {
            if (theTransition.isTauTimerVisible()) {
                tauVisibilityButton.setNewText(lang.getText("HNXTPN_entry052vis")); //Tau: Visible
                tauVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            } else {
                tauVisibilityButton.setNewText(lang.getText("HNXTPN_entry052invis")); //Tau: Hidden
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
                button.setNewText(lang.getText("HNXTPN_entry052invis")); //Tau: Hidden
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { //włączamy
                theTransition.setTauTimersVisibility(true);
                button.setNewText(lang.getText("HNXTPN_entry052vis")); //Tau: Visible
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
            }
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);

            action.reselectElement(eLocation);
            setFieldStatus(false);
        });
        infoPanel.add(tauVisibilityButton);

        //************************* NEWLINE *************************
        infPanelY += 30;
        //************************* NEWLINE *************************


        // XTPN-transition Zakresy alfa:
        JLabel minMaxLabel = new JLabel(lang.getText("HNXTPN_entry053"), JLabel.LEFT); //Alpha (min/max):
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
                overlord.log(lang.getText("LOGentry00600exception")+"\n"+ex.getMessage(), "error", true);
            }

            double min = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setAlfaMinTime(min, theTransition, eLocation);

            doNotUpdate = true;
            alphaMaxTextField.setValue(theTransition.getAlphaMaxValue());
            field.setValue(theTransition.getAlphaMinValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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
                overlord.log(lang.getText("LOGentry00601exception")+"\n"+ex.getMessage(), "error", true);
            }

            double max = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setAlfaMaxTime(max, theTransition, eLocation);

            doNotUpdate = true;
            alphaMinTextField.setValue(theTransition.getAlphaMinValue());
            field.setValue(theTransition.getAlphaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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
        JLabel betaLabel = new JLabel(lang.getText("HNXTPN_entry054"), JLabel.LEFT);
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
                overlord.log(lang.getText("LOGentry00602exception")+"\n"+ex.getMessage(), "error", true);
            }

            double min = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setBetaMinTime(min, theTransition, eLocation);

            doNotUpdate = true;
            field.setValue(theTransition.getBetaMinValue());
            betaMaxTextField.setValue(theTransition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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
                overlord.log(lang.getText("LOGentry00603exception")+"\n"+ex.getMessage(), "error", true);
            }
            double max = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setBetaMaxTime(max, theTransition, eLocation);

            doNotUpdate = true;
            betaMinTextField.setValue(theTransition.getBetaMinValue());
            field.setValue(theTransition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
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
        chartMainPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry055"))); //Transition chart
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-18, 280);
        chartMainPanel.add(createChartPanel(theTransition), BorderLayout.CENTER);
        chartMainPanel.setBackground(Color.WHITE);
        mainInfoPanel.add(chartMainPanel);

        JPanel chartButtonPanel = panelButtonsTransition(infoPanel.getHeight()+chartMainPanel.getHeight());
        mainInfoPanel.add(chartButtonPanel);

        JPanel simStatsPanel = panelSimStatsTransition(infoPanel.getHeight()+chartMainPanel.getHeight()+chartButtonPanel.getHeight());
        mainInfoPanel.add(simStatsPanel);

        try {
            if(!overlord.getWorkspace().getProject().getTransitions().isEmpty()
                    && !overlord.getWorkspace().getProject().getPlaces().isEmpty()) {
                fillTransitionDynamicData(transStatsFiringStepsTextBox, chartMainPanel, chartButtonPanel);
            }
        } catch (Exception ex) {
            overlord.log(lang.getText("LOGentry00597exception")+"\n"+ex.getMessage(), "error", true);
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
        chartButtonPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry056"))); //Chart options
        chartButtonPanel.setBackground(Color.WHITE);

        int positionX = 5;
        int positionY = 30;

        acqDataButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry057") //Simulate
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        acqDataButton.setBounds(positionX, positionY-10, 110, 35);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText(lang.getText("HNXTPN_entry057t"));
        acqDataButton.addActionListener(actionEvent -> {
            if(overlord.getWorkspace().getProject().isSimulationActive()) {
                JOptionPane.showMessageDialog(null, lang.getText("HNXTPN_entry058")
                        , lang.getText("HNXTPN_entry058t"),
                        JOptionPane.WARNING_MESSAGE);
            } else {
                acqDataButton.setEnabled(false);
                getTransSimpleChartData(simTransNumberOfReps);
            }
        });
        chartButtonPanel.add(acqDataButton);

        JLabel labelSteps = new JLabel(lang.getText("HNXTPN_entry059")); //Steps:
        labelSteps.setBounds(positionX+120, positionY-15, 70, 15);
        chartButtonPanel.add(labelSteps);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 100000000, 30000);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(positionX+120, positionY, 80, 25);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simSteps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsSpinner);

        JLabel labelMode = new JLabel(lang.getText("HNXTPN_entry060")); //Show:
        labelMode.setBounds(positionX+210, positionY-15, 80, 15);
        chartButtonPanel.add(labelMode);

        final JComboBox<String> simMode = new JComboBox<String>(new String[] {lang.getText("HNXTPN_entry061op1"), lang.getText("HNXTPN_entry061op2")}); //Steps, Time
        simMode.setBounds(positionX+210, positionY, 80, 25);
        simMode.setSelectedIndex(0);
        simMode.setMaximumRowCount(6);
        simMode.addActionListener(actionEvent -> {
            transitionChartType = simMode.getSelectedIndex();
            if(statusVectorTransition.size() == 3) {
                showTransitionsChart();
            }
        });
        chartButtonPanel.add(simMode);

        JCheckBox simTransRepsCheckbox = new JCheckBox(lang.getText("HNXTPN_entry061")); //Reps:
        simTransRepsCheckbox.setBounds(positionX+295, positionY-15, 70, 15);
        simTransRepsCheckbox.setSelected(simulateTime);
        simTransRepsCheckbox.setBackground(Color.WHITE);
        simTransRepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            simTransReps = box.isSelected();
        });
        chartButtonPanel.add(simTransRepsCheckbox);

        SpinnerModel simTransStepsRepeatedSpinnerModel = new SpinnerNumberModel(simPlaceNumberOfReps, 1, 100, 10);
        JSpinner simTransStepsRepeatedSpinner = new JSpinner(simTransStepsRepeatedSpinnerModel);
        simTransStepsRepeatedSpinner.setBounds(positionX+300, positionY, 70, 25);
        simTransStepsRepeatedSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simTransNumberOfReps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simTransStepsRepeatedSpinner);

        JLabel simTransIntervalLabel = new JLabel(lang.getText("HNXTPN_entry062")); //Interval:
        simTransIntervalLabel.setBounds(positionX+380, positionY-15, 80, 15);
        chartButtonPanel.add(simTransIntervalLabel);

        SpinnerModel simTransIntervalSpinnerModel = new SpinnerNumberModel(simPlaceInterval, 1, 1000, 10);
        JSpinner simTransIntervalSpinner = new JSpinner(simTransIntervalSpinnerModel);
        simTransIntervalSpinner.setBounds(positionX+380, positionY, 60, 25);
        simTransIntervalSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simTransInterval = (int) spinner.getValue();
            showTransitionsChart();
        });
        chartButtonPanel.add(simTransIntervalSpinner);

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
        resultPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNXTPN_entry063")));
        resultPanel.setBackground(Color.WHITE);

        int positionX = 10;
        int positionY = 30;

        transStatsStepLabel = new JLabel(lang.getText("HNXTPN_entry064"), JLabel.LEFT); //Steps:
        transStatsStepLabel.setBounds(positionX+80, positionY-20, 140, 20);
        resultPanel.add(transStatsStepLabel);

        transStatsTimeLabel = new JLabel(lang.getText("HNXTPN_entry065"), JLabel.LEFT); //Time:
        transStatsTimeLabel.setBounds(positionX+185, positionY-20, 140, 20);
        resultPanel.add(transStatsTimeLabel);

        JLabel inactiveStepsLabel = new JLabel(lang.getText("HNXTPN_entry066"), JLabel.LEFT); //Inactive:
        inactiveStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(inactiveStepsLabel);

        transStatsInactiveStepsTextBox = new JFormattedTextField(lang.getText("na")); //n/a
        transStatsInactiveStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        transStatsInactiveStepsTextBox.setEditable(false);
        resultPanel.add(transStatsInactiveStepsTextBox);

        transStatsInactiveTimeTextBox = new JFormattedTextField(lang.getText("na")); //n/a
        transStatsInactiveTimeTextBox.setBounds(positionX+185, positionY, 110, 20);
        transStatsInactiveTimeTextBox.setEditable(false);
        resultPanel.add(transStatsInactiveTimeTextBox);

        HolmesRoundedButton acqTransSimDataButton = new HolmesRoundedButton(lang.getText("HNXTPN_entry067") //Simulate
                , "jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        acqTransSimDataButton.setBounds(positionX+300, positionY, 110, 20);
        acqTransSimDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqTransSimDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqTransSimDataButton.setToolTipText(lang.getText("HNXTPN_entry067t"));
        acqTransSimDataButton.addActionListener(actionEvent -> {
            if(overlord.getWorkspace().getProject().isSimulationActive()) {
                JOptionPane.showMessageDialog(null, lang.getText("HNXTPN_entry068")
                        , lang.getText("HNXTPN_entry068t"),
                        JOptionPane.WARNING_MESSAGE);
            } else {
                getMultipleTransitionData();
            }
        });
        resultPanel.add(acqTransSimDataButton);

        positionY+=23;

        JLabel activeStepsLabel = new JLabel(lang.getText("HNXTPN_entry069"), JLabel.LEFT); //Active:
        activeStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(activeStepsLabel);

        transStatsActiveStepsTextBox = new JFormattedTextField(lang.getText("na")); //n/a
        transStatsActiveStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        transStatsActiveStepsTextBox.setEditable(false);
        resultPanel.add(transStatsActiveStepsTextBox);

        transStatsActiveTimeTextBox = new JFormattedTextField(lang.getText("na")); //n/a
        transStatsActiveTimeTextBox.setBounds(positionX+185, positionY, 110, 20);
        transStatsActiveTimeTextBox.setEditable(false);
        resultPanel.add(transStatsActiveTimeTextBox);

        transStatsStepsCheckbox = new JCheckBox(lang.getText("HNXTPN_entry070")); //Steps
        transStatsStepsCheckbox.setBounds(positionX+300, positionY, 70, 20);
        transStatsStepsCheckbox.setSelected(simulateTime);
        transStatsStepsCheckbox.setBackground(Color.WHITE);
        transStatsStepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            transStatsSimulateWithSteps = box.isSelected();
            doNotUpdate = true;
            transStatsTimeCheckbox.setSelected(!transStatsSimulateWithSteps);
            doNotUpdate = false;
        });
        resultPanel.add(transStatsStepsCheckbox);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(transStatsNumberOfSteps, 0, 1000000, 5000);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(positionX+400, positionY, 70, 20);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            transStatsNumberOfSteps = (int) spinner.getValue();
        });
        resultPanel.add(simStepsSpinner);

        positionY+=23;

        JLabel producingStepsLabel = new JLabel(lang.getText("HNXTPN_entry071"), JLabel.LEFT); //Producing:
        producingStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(producingStepsLabel);

        transStatsProductionStepsTextBox = new JFormattedTextField(lang.getText("na")); //n/a
        transStatsProductionStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        transStatsProductionStepsTextBox.setEditable(false);
        resultPanel.add(transStatsProductionStepsTextBox);

        transStatsProductionTimeTextBox = new JFormattedTextField(lang.getText("na")); //n/a
        transStatsProductionTimeTextBox.setBounds(positionX+185, positionY, 110, 20);
        transStatsProductionTimeTextBox.setEditable(false);
        resultPanel.add(transStatsProductionTimeTextBox);

        transStatsTimeCheckbox = new JCheckBox(lang.getText("HNXTPN_entry072")); //Time
        transStatsTimeCheckbox.setBounds(positionX+300, positionY, 70, 20);
        transStatsTimeCheckbox.setSelected(simulateTime);
        transStatsTimeCheckbox.setBackground(Color.WHITE);
        transStatsTimeCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;
            JCheckBox box = (JCheckBox) e.getSource();
            transStatsSimulateWithSteps = !(box.isSelected());
            doNotUpdate = true;
            transStatsStepsCheckbox.setSelected(transStatsSimulateWithSteps);
            doNotUpdate = false;
        });
        resultPanel.add(transStatsTimeCheckbox);

        SpinnerModel simTimeLengthSpinnerModel = new SpinnerNumberModel(transStatsMaxTime, 0, 50000, 1000);
        JSpinner simTimeLengthSpinner = new JSpinner(simTimeLengthSpinnerModel);
        simTimeLengthSpinner.setBounds(positionX+400, positionY, 70, 20);
        simTimeLengthSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            transStatsMaxTime = (double)spinner.getValue();
        });
        resultPanel.add(simTimeLengthSpinner);

        positionY+=23;

        JLabel producedStepsLabel = new JLabel(lang.getText("HNXTPN_entry073"), JLabel.LEFT); //Fired:
        producedStepsLabel.setBounds(positionX, positionY, 70, 20);
        resultPanel.add(producedStepsLabel);

        transStatsFiringStepsTextBox = new JFormattedTextField("n/a");
        transStatsFiringStepsTextBox.setBounds(positionX+80, positionY, 100, 20);
        transStatsFiringStepsTextBox.setEditable(false);
        resultPanel.add(transStatsFiringStepsTextBox);

        JCheckBox transRepsCheckbox = new JCheckBox(lang.getText("HNXTPN_entry074")); //Repetitions:
        transRepsCheckbox.setBounds(positionX+300, positionY, 100, 20);
        transRepsCheckbox.setSelected(transStatsReps);
        transRepsCheckbox.setBackground(Color.WHITE);
        transRepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;

            JCheckBox box = (JCheckBox) e.getSource();
            transStatsReps = box.isSelected();
        });
        resultPanel.add(transRepsCheckbox);

        SpinnerModel simRepetitionsSpinnerModel = new SpinnerNumberModel(10, 10, 100, 1);
        JSpinner simRepetitionsSpinner = new JSpinner(simRepetitionsSpinnerModel);
        simRepetitionsSpinner.setBounds(positionX+400, positionY, 70, 20);
        simRepetitionsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            transStatsRepetitions = (int) spinner.getValue();
        });
        resultPanel.add(simRepetitionsSpinner);

        doNotUpdate = true;
        transStatsStepsCheckbox.setSelected(true);
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
            getTransSimpleChartData(1);
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
            title.setText(lang.getText("HNXTPN_entry075"));
        }
    }

    /**
     * Metoda aktywuje symulator dla jednej tranzycji w ustalonym wcześniej trybie i dla wcześniej
     * ustalonej liczby kroków. Wyniki zapisuje na wykresie, zwraca też wektor danych.
     */
    private void getTransSimpleChartData(int reps) {
        StateSimulatorXTPN ss = new StateSimulatorXTPN();

        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
        ownSettings.setSimSteps_XTPN( simSteps );
        ownSettings.setSimTime_XTPN( simTimeLength );
        ownSettings.setTimeSimulationStatus_XTPN(simulateTime);
        ss.initiateSim(ownSettings);

        if(!simTransReps) {
            reps = 1; //override if simPlaceReps = false
        }

        ArrayList<ArrayList<Double>> tmp = ss.simulateNetSingleTransition(ownSettings, theTransition, reps);
        if(tmp != null) {
            statusVectorTransition = new ArrayList<>(tmp);
            acqDataButton.setEnabled(true);
            if(statusVectorTransition.size() == 3) {
                showTransitionsChart();
            }
        }
    }

    /**
     * Metoda odpowiedzialna za pobranie dokładniejszych danych statystycznych o zachowaniu tranzycji
     * w symulacji - symulacja powtarzana jest pewną liczbę razy a wyniki uśredniane.
     */
    private void getMultipleTransitionData() {
        StateSimulatorXTPN ss = new StateSimulatorXTPN();
        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
        ownSettings.setSimSteps_XTPN(transStatsNumberOfSteps);
        ownSettings.setSimTime_XTPN(transStatsMaxTime);
        ownSettings.setTimeSimulationStatus_XTPN( !transStatsSimulateWithSteps);
        ss.initiateSim(ownSettings);

        ArrayList<Double> statsVector = null;
        for(int i = 0; i< transStatsRepetitions; i++) {
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
            statsVector.set(j, statsVector.get(j) / transStatsRepetitions);
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

        int maxInterval = simTransInterval;
        if(10*simTransInterval > statusVectorTransition.get(0).size()) {
            maxInterval = 1;
            overlord.log(lang.getText("HNXTPN_entry076"), "warning", true);
        }

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

                if(interval++ == maxInterval) {
                    inactive /= maxInterval;
                    active /= maxInterval;
                    producing /= maxInterval;
                    firing /= maxInterval;

                    if(transitionChartType == 0) {
                        series0.add(step, inactive);
                        series1.add(step, active);
                        series2.add(step, producing);
                        series3.add(step, firing);
                    } else {
                        double time = statusVectorTransition.get(1).get(step);

                        series0.add(time, inactive);
                        series1.add(time, active);
                        series2.add(time, producing);
                        series3.add(time, firing);
                    }

                    inactive = active = producing = firing = 0.0;
                    interval = 0;
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
                buttonGammaMode.setNewText(lang.getText("HNXTPN_entry077on")); //Gamma: ON
                buttonGammaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                gammaVisibilityButton.setEnabled(true);
                gammaMinTextField.setEnabled(true);
                gammaMaxTextField.setEnabled(true);

                if(thePlace.isGammaRangeVisible()) {
                    gammaVisibilityButton.setNewText(lang.getText("HNXTPN_entry078vis")); //Gamma: visible
                    gammaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    gammaVisibilityButton.setNewText(lang.getText("HNXTPN_entry078invis")); //Gamma: hidden
                    gammaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
            } else { //GAMMA OFFLINE
                buttonGammaMode.setNewText(lang.getText("HNXTPN_entry077off")); //Gamma: OFF
                buttonGammaMode.repaintBackground("pearl_bH1_neutr.png", "paerl_bH2_hover.png", "paerl_bH3_press.png");

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
                buttonAlphaMode.setNewText(lang.getText("HNXTPN_entry042on")); //Alpha: ON
                buttonAlphaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                buttonClassXTPNmode.setNewText(lang.getText("HNXTPN_entry049")); //XTPN
                buttonClassXTPNmode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                alphaMinTextField.setEnabled(true);
                alphaMaxTextField.setEnabled(true);

                alphaVisibilityButton.setEnabled(true);
                tauVisibilityButton.setEnabled(true);

                if(theTransition.isAlphaRangeVisible()) {
                    alphaVisibilityButton.setNewText(lang.getText("HNXTPN_entry044vis"));    //Alpha: Visible
                    alphaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    alphaVisibilityButton.setNewText(lang.getText("HNXTPN_entry044invis"));   //Alpha: Hidden
                    alphaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }

                if(theTransition.isTauTimerVisible()) {
                    tauVisibilityButton.setNewText(lang.getText("HNXTPN_entry079vis")); //Tau: Visible
                    tauVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    tauVisibilityButton.setNewText(lang.getText("HNXTPN_entry079invis")); //Tau: Hidden
                    tauVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
            } else { //ALFA OFFLINE
                buttonAlphaMode.setNewText(lang.getText("HNXTPN_entry042off")); //Alpha: OFF
                buttonAlphaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

                alphaMinTextField.setEnabled(false);
                alphaMaxTextField.setEnabled(false);

                alphaVisibilityButton.setEnabled(false);
            }

            if(theTransition.isBetaModeActive()) {
                buttonBetaMode.setNewText(lang.getText("HNXTPN_entry045on")); //Beta: ON
                buttonBetaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                buttonClassXTPNmode.setNewText(lang.getText("HNXTPN_entry049")); //XTPN
                buttonClassXTPNmode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                betaMinTextField.setEnabled(true);
                betaMaxTextField.setEnabled(true);

                betaVisibilityButton.setEnabled(true);
                tauVisibilityButton.setEnabled(true);

                if(theTransition.isBetaRangeVisible()) {
                    betaVisibilityButton.setNewText(lang.getText("HNXTPN_entry047vis")); //Beta: Visible
                    betaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    betaVisibilityButton.setNewText(lang.getText("HNXTPN_entry047invis")); //Beta: Hidden
                    betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
                if(theTransition.isTauTimerVisible()) {
                    tauVisibilityButton.setNewText(lang.getText("HNXTPN_entry079vis")); //Tau: Visible
                    tauVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    tauVisibilityButton.setNewText(lang.getText("HNXTPN_entry079invis")); //Tau: Hidden
                    tauVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
            } else { //BETA OFFLINE
                buttonBetaMode.setNewText(lang.getText("HNXTPN_entry045off")); //Beta: OFF
                buttonBetaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

                betaMinTextField.setEnabled(false);
                betaMaxTextField.setEnabled(false);

                betaVisibilityButton.setEnabled(false);
            }

            if(!theTransition.isAlphaModeActive() && !theTransition.isBetaModeActive()) { //both offline
                buttonClassXTPNmode.setNewText(lang.getText("HNXTPN_entry050")); //Classical
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

        transStatsStepLabel.setText(lang.getText("HNXTPN_entry064")+" "+Tools.cutValue(realSimulationSteps) ); //Steps:
        transStatsTimeLabel.setText(lang.getText("HNXTPN_entry065")+" "+Tools.cutValue(realSimulationTime) ); //Time:

        double tmp = (inactiveSteps / realSimulationSteps) * 100;
        transStatsInactiveStepsTextBox.setText((int)inactiveSteps + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (activeSteps / realSimulationSteps) * 100;
        transStatsActiveStepsTextBox.setText((int)activeSteps + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (producingSteps / realSimulationSteps) * 100;
        transStatsProductionStepsTextBox.setText((int)producingSteps + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (fireSteps / realSimulationSteps) * 100;
        transStatsFiringStepsTextBox.setText((int)fireSteps+" "+ " ("+Tools.cutValue(tmp)+"%)");

        tmp = (inactiveTime / realSimulationTime) * 100;
        transStatsInactiveTimeTextBox.setText(Tools.cutValue(inactiveTime) + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (activeTime / realSimulationTime) * 100;
        transStatsActiveTimeTextBox.setText(Tools.cutValue(activeTime) + " ("+Tools.cutValue(tmp)+"%)");
        tmp = (producingTime / realSimulationTime) * 100;
        transStatsProductionTimeTextBox.setText(Tools.cutValue(producingTime) + " ("+Tools.cutValue(tmp)+"%)");
    }
}
