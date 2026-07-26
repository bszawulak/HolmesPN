package holmes.windows.xtpn;

import holmes.analyse.XTPN.*;
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.function.Function;

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
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return 32;
            }
            @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
            }
        });
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
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected int calculateTabHeight(int tabPlacement, int tabIndex, int fontHeight) {
                return 32;
            }
            @Override protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {
                super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
            }
        });
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
            setSize(new Dimension(800, 590));
        } else { //tranzycja
            setSize(new Dimension(800, 730));
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
        portalLabel.setBounds(infPanelX, infPanelY, 50, 20);
        infoPanel.add(portalLabel);

        String port = lang.getText("no");
        if(thePlace.isPortal())
            port = lang.getText("yes");

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+50, infPanelY, 35, 20);
        infoPanel.add(portalLabel2);

        int inTrans = 0;
        int outTrans = 0;
        for (ElementLocation el : thePlace.getElementLocations()) {
            inTrans += el.getInArcs().size(); //tyle tranzycji kieruje tutaj łuk
            outTrans += el.getOutArcs().size();
        }

        JLabel inTransLabel = new JLabel(lang.getText("HNXTPN_entry011")); //Input transitions:
        inTransLabel.setBounds(infPanelX+90, infPanelY, 120, 20);
        infoPanel.add(inTransLabel);

        JFormattedTextField inTransTextBox = new JFormattedTextField(inTrans);
        inTransTextBox.setBounds(infPanelX+215, infPanelY, 25, 20);
        inTransTextBox.setEditable(false);
        infoPanel.add(inTransTextBox);

        JLabel outTransLabel = new JLabel(lang.getText("HNXTPN_entry012")); //Output transitions:
        outTransLabel.setBounds(infPanelX+245, infPanelY, 130, 20);
        infoPanel.add(outTransLabel);

        JFormattedTextField outTransTextBox = new JFormattedTextField(outTrans);
        outTransTextBox.setBounds(infPanelX+380, infPanelY, 30, 20);
        outTransTextBox.setEditable(false);
        infoPanel.add(outTransTextBox);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel gammaModeInfoLabel = new JLabel(lang.getText("HNXTPN_entry013")); //Time mode:
        gammaModeInfoLabel.setBounds(infPanelX, infPanelY, 80, 30);
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
        analP_firstPanel.setBounds(mPanelX, mPanelY, secondTabPanel.getWidth()-24, 510);
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
        
        HolmesRoundedButton checkKboundButton = new HolmesRoundedButton("<html>Check OLD</html>" //Check boundedness //OBSOLETE
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        checkKboundButton.setMargin(new Insets(0, 0, 0, 0));
        checkKboundButton.setBounds(subPanelX+150, subPanelY, 130, 32);
        checkKboundButton.addActionListener(actionEvent -> {
            placeSecondPanelResults.setText("Simple Mode");
            long maxSteps = MaxTokensBoundCalculator.maxSteps(thePlace);
            long result = MaxTokensBoundCalculator.computeUpperBoundForPlace(thePlace, maxSteps);
            placeSecondPanelResults.append("Place: "+thePlace.getName()+"\n");
            placeSecondPanelResults.append("Max steps: "+maxSteps+"\n");
            placeSecondPanelResults.append("Tokens per place: "+result+"\n");

            placeSecondPanelResults.append("\n");
            placeSecondPanelResults.append("Ext Safe Mode\n");
            long stepsExt = MaxTokensBoundCalculator.maxStepsExtended(thePlace);
            boolean unsafePlaces = false; // EXT_SAFE
            int maxTokensExtSafe = MaxTokensBoundCalculator.computeUpperBoundForPlaceExtended(thePlace, stepsExt, unsafePlaces);
            placeSecondPanelResults.append("Max steps (EXT SAFE): "+stepsExt+"\n");
            placeSecondPanelResults.append("Tokens per place (EXT SAFE): "+maxTokensExtSafe+"\n");
        });
        //analP_firstPanel.add(checkKboundButton);

        HolmesRoundedButton checkKboundButtonV2 = new HolmesRoundedButton("<html>Check boundedness</html>" //Check boundedness
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        checkKboundButtonV2.setMargin(new Insets(0, 0, 0, 0));
        checkKboundButtonV2.setBounds(subPanelX, subPanelY, 130, 32);
        checkKboundButtonV2.addActionListener(actionEvent -> {
            try {
                MaxTokensBoundCalculatorV2.HorizonOptions horizonOptions = new MaxTokensBoundCalculatorV2.HorizonOptions(3, 100_000L);
                // ------------------------------------------------------------
                // 1. Direct formula and producer-only simulation
                // ------------------------------------------------------------
                MaxTokensBoundCalculatorV2.AnalysisModel producerModel = MaxTokensBoundCalculatorV2.prepare(thePlace, MaxTokensBoundCalculatorV2.Mode.PRODUCERS_ONLY);
                MaxTokensBoundCalculatorV2.FormulaResult formula = MaxTokensBoundCalculatorV2.computeProducerOnlyFormula(producerModel);
                MaxTokensBoundCalculatorV2.HorizonEstimate producerSteps = MaxTokensBoundCalculatorV2.estimateMaxSteps(producerModel, horizonOptions);
                MaxTokensBoundCalculatorV2.CalculationResult producerSimulation = MaxTokensBoundCalculatorV2.compute(producerModel, producerSteps.getMaxSteps());

                placeSecondPanelResults.append("Producer-only formula\n");
                placeSecondPanelResults.append("B_prod(p): " + formula.getProducerOnlyMaximum() + "\n");
                placeSecondPanelResults.append("B_safe(p): " + formula.getSafeBoundWithInitialTokens() + "\n");
                placeSecondPanelResults.append("Formula exact for current initial K: " + formula.isExactForCurrentInitialMultiset() + "\n\n");

                placeSecondPanelResults.append("Producer-only simulation\n");
                placeSecondPanelResults.append("Max steps: " + producerSteps.getMaxSteps() + (producerSteps.isCapped() ? " (capped)" : "") + "\n");
                placeSecondPanelResults.append("Maximum tokens: " + producerSimulation.getMaxTokens() + "\n");
                placeSecondPanelResults.append("First maximum at scaled step: " + producerSimulation.getFirstMaximumStep() + "\n");
                placeSecondPanelResults.append("First maximum at time: " + producerSimulation.getFirstMaximumTime() + "\n");
                placeSecondPanelResults.append("Time scale: " + producerModel.getTimeScale() + " step(s) per original time unit.\n");
                if (producerSteps.isCapped()) {placeSecondPanelResults.append("Estimated steps before cap: " + producerSteps.getUncappedSteps() + "\n");
                }

                if (formula.isExactForCurrentInitialMultiset() && !producerSteps.isCapped()
                        && formula.getProducerOnlyMaximum() != producerSimulation.getMaxTokens()) {
                    placeSecondPanelResults.append("WARNING: formula and producer-only simulation differ.\n");
                }

                // ------------------------------------------------------------
                // 2. Simulation with all NORMAL output transitions as consumers
                // ------------------------------------------------------------
                MaxTokensBoundCalculatorV2.AnalysisModel consumerModel = MaxTokensBoundCalculatorV2.prepare(thePlace, MaxTokensBoundCalculatorV2.Mode.WITH_CONSUMERS);
                MaxTokensBoundCalculatorV2.HorizonEstimate consumerSteps = MaxTokensBoundCalculatorV2.estimateMaxSteps(consumerModel, horizonOptions);
                MaxTokensBoundCalculatorV2.CalculationResult consumerSimulation = MaxTokensBoundCalculatorV2.compute(consumerModel, consumerSteps.getMaxSteps());

                placeSecondPanelResults.append("\nConsumer-aware simulation\n");
                placeSecondPanelResults.append("Max-steps method: " + consumerSteps.getMethod() + "\n");
                placeSecondPanelResults.append("Max steps: " + consumerSteps.getMaxSteps() + (consumerSteps.isCapped() ? " (capped)" : "") + "\n");
                placeSecondPanelResults.append("Maximum tokens: " + consumerSimulation.getMaxTokens() + "\n");
                placeSecondPanelResults.append("First maximum at scaled step: " + consumerSimulation.getFirstMaximumStep() + "\n");
                placeSecondPanelResults.append("First maximum at time: " + consumerSimulation.getFirstMaximumTime() + "\n");
                placeSecondPanelResults.append("Time scale: " + consumerModel.getTimeScale() + " step(s) per original time unit.\n");
                if (consumerSteps.isCapped()) {placeSecondPanelResults.append("Estimated steps before cap: " + consumerSteps.getUncappedSteps() + "\n");
                }

            } catch (IllegalArgumentException | IllegalStateException | ArithmeticException exception) {
                String message = "The token-count analysis could not be performed:\n" + exception.getMessage();
                placeSecondPanelResults.append(message + "\n");
                JOptionPane.showMessageDialog(null, message, "xTPN token-count analysis", JOptionPane.ERROR_MESSAGE);
            }
        });
        analP_firstPanel.add(checkKboundButtonV2);

        subPanelY+=40;

        placeSecondPanelResults = new JTextArea();
        placeSecondPanelResults.setLineWrap(true);
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(placeSecondPanelResults), BorderLayout.CENTER);
        CreationPanel.setBounds(subPanelX, subPanelY, 755, 440);
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

        //secondTabPanel.add(analP_secondPanel);

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
        simPlaceRepsCheckbox.setBounds(positionX+295, positionY-15, 100, 15);
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
        simStepsRepeatedSpinner.setBounds(positionX+300, positionY, 80, 25);
        simStepsRepeatedSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simPlaceNumberOfReps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsRepeatedSpinner);


        JLabel simPlaceIntervalLabel = new JLabel(lang.getText("HNXTPN_entry029")); //Interval:
        simPlaceIntervalLabel.setBounds(positionX+410, positionY-15, 90, 15);
        chartButtonPanel.add(simPlaceIntervalLabel);

        SpinnerModel simIntervalSpinnerModel = new SpinnerNumberModel(simPlaceInterval, 1, 1000, 10);
        JSpinner simIntervalSpinner = new JSpinner(simIntervalSpinnerModel);
        simIntervalSpinner.setBounds(positionX+410, positionY, 60, 25);
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
                interval++;
                if(interval == maxInterval) {
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
        analP_firstPanel.setBounds(mPanelX, mPanelY, secondTabPanel.getWidth()-24, 650);
        analP_firstPanel.setBorder(BorderFactory.createTitledBorder("XTPN analysis:"));

        int subPanelX = 10;
        int subPanelY = 20;

        //************************* NEWLINE *************************

        int id = overlord.getWorkspace().getProject().getTransitions().indexOf(theTransition);   //OBSOLETE
        HolmesRoundedButton checkActivationWindowsForTransButton = new HolmesRoundedButton("<html>Check LEGACY</html>" //Check lifeness
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        checkActivationWindowsForTransButton.setMargin(new Insets(0, 0, 0, 0));
        checkActivationWindowsForTransButton.setBounds(subPanelX+300, subPanelY, 130, 32);
        checkActivationWindowsForTransButton.addActionListener(actionEvent -> {
            try {
                transSecondPanelResults.setText("");
                transSecondPanelResults.append("Transition: " + theTransition.getName() + "\n");
                // --- parametry analizy (przykład) ---
                int param = 1;                    // 1 = best, 2 = worst
                boolean includeCompetitors = true; // czy uwzględniać competitors
                boolean simplifiedHorizon = true;  // uproszczony horyzont (LCM + max(gammaU))
                // --- 1) inicjalizacja + horyzont ---
                long maxSteps = ActivationAnalyzerXTPN.initialize(
                        theTransition,
                        param,
                        includeCompetitors,
                        simplifiedHorizon
                );
                if (maxSteps <= 0L) {
                    // initialize() już pokaże JOptionPane przy błędzie, ale tu dopiszmy log do JTextArea
                    transSecondPanelResults.append("Initialization failed (maxSteps = 0).\n");
                    return;
                }

                transSecondPanelResults.append("Max steps (horizon): " + maxSteps + "\n");
                // --- 2) właściwa analiza ---
                int result = ActivationAnalyzerXTPN.analyzeActivationChances(theTransition, maxSteps);
                if (result < 0) {
                    transSecondPanelResults.append("Analysis failed (cache mismatch or invalid args).\n");
                    return;
                }
                // --- 3) odczyt alfa^L / alfa^U badanej tranzycji (do komunikatu) ---
                // Uwaga: w analyzerze mogliśmy robić 0->1 dla L (po zgodzie użytkownika),
                // a tu bierzemy "surowe" wartości z obiektu. To jest tylko opis do UI.
                int alphaL = (int) Math.round(theTransition.getAlphaMinValue());
                int alphaU = (int) Math.round(theTransition.getAlphaMaxValue());
                if (alphaL < 0) alphaL = 0;
                if (alphaU < 0) alphaU = 0;
                // --- 4) interpretacja wyniku ---
                transSecondPanelResults.append("Result (max continuous activation time): " + result + "\n");
                transSecondPanelResults.append("Alpha window: [" + alphaL + ", " + alphaU + "]\n");

                if (result >= alphaU) {
                    transSecondPanelResults.append("Status: FULL activation window achievable (result == alpha^U).\n");
                } else if (result >= alphaL) {
                    transSecondPanelResults.append("Status: PARTIAL window achievable (alpha^L <= result < alpha^U).\n");
                } else {
                    transSecondPanelResults.append("Status: Activation does NOT reach alpha^L (result < alpha^L).\n");
                }
                transSecondPanelResults.append("\nParameters:\n");
                transSecondPanelResults.append(" - param = " + param + " (" + (param == 1 ? "best" : "worst") + ")\n");
                transSecondPanelResults.append(" - includeCompetitors = " + includeCompetitors + "\n");
                transSecondPanelResults.append(" - simplifiedHorizon = " + simplifiedHorizon + "\n");
            } catch (Exception ex) {
                transSecondPanelResults.append("Exception: " + ex.getMessage() + "\n");
                //ex.printStackTrace();
            }
        });
        //analP_firstPanel.add(checkActivationWindowsForTransButton);

        //subPanelX += 150;
        HolmesRoundedButton checkActivationWindowsForTransButtonV2 = new HolmesRoundedButton("<html>Check lifeness</html>" //Check lifeness
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        checkActivationWindowsForTransButtonV2.setMargin(new Insets(0, 0, 0, 0));
        checkActivationWindowsForTransButtonV2.setBounds(subPanelX, subPanelY, 130, 32);
        checkActivationWindowsForTransButtonV2.addActionListener(actionEvent -> {
            try {
                transSecondPanelResults.setText("");
                ArrayList<Transition> allTransitions = overlord.getWorkspace().getProject().getTransitions();
                int transitionID = allTransitions.indexOf(theTransition);
                String transitionSymbol = transitionID >= 0 ? "t_" + transitionID : "t_?";
                transSecondPanelResults.append("Transition: " + transitionSymbol + " (name: " + theTransition.getName() + ")\n");
                // Scenario used in the article:
                // fast producers and slow competitors.
                ActivationAnalyzerXTPNV2.TimingScenario timingScenario = ActivationAnalyzerXTPNV2.TimingScenario.FAVORABLE_TO_TARGET;
                boolean includeCompetitors = true;
                // PLACE_AWARE starts with the common-period estimate and can enlarge it
                // using the separate token-flow estimate for each target pre-place.
                // extraBlocks = 3 follows the current article; maxSteps is capped at 100000.
                ActivationAnalyzerXTPNV2.HorizonOptions horizonOptions =
                        new ActivationAnalyzerXTPNV2.HorizonOptions(ActivationAnalyzerXTPNV2.HorizonStrategy.PLACE_AWARE, 3, 100_000L);

                ActivationAnalyzerXTPNV2.AnalysisModel model = ActivationAnalyzerXTPNV2.prepare(theTransition, timingScenario, includeCompetitors);
                ActivationAnalyzerXTPNV2.HorizonEstimate horizon = ActivationAnalyzerXTPNV2.estimateMaxSteps(model, horizonOptions);

                // maxSteps is supplied to compute() explicitly. The analysis performs no
                // repeated-configuration stopping. It can stop early only after alpha^U
                // of the target has been reached.
                ActivationAnalyzerXTPNV2.CalculationResult result = ActivationAnalyzerXTPNV2.compute(model, horizon.getMaxSteps());

                transSecondPanelResults.append("Pre-places: " + model.getPrePlaceCount() + "\n");
                transSecondPanelResults.append("Producers: " + model.getProducerCount() + "\n");
                transSecondPanelResults.append("Competitors: " + model.getCompetitorCount() + "\n");
                transSecondPanelResults.append("Time scale: " + model.getTimeScale() + " iteration(s) per original time unit\n");

                transSecondPanelResults.append("maxSteps strategy: " + horizon.getStrategy() + "\n");
                transSecondPanelResults.append("Estimated maxSteps before cap: " + horizon.getUncappedSteps() + "\n");
                transSecondPanelResults.append("maxSteps used: " + horizon.getMaxSteps() + " (original time: " + horizon.getMaxTime().toPlainString() + ")\n");
                if (horizon.isCapped()) {
                    transSecondPanelResults.append("Warning: maxSteps was reduced by the user limit.\n");
                }

                transSecondPanelResults.append("Result (maximum continuous activation time): " + result.getMaximumActivationTime().toPlainString() + "\n");
                transSecondPanelResults.append("Alpha window: [" + result.getAlphaL().toPlainString() + ", " + result.getAlphaU().toPlainString() + "]\n");
                transSecondPanelResults.append("Iterations actually performed: " + result.getPerformedSteps() + "\n");

                switch (result.getStatus()) {
                    case FULL_WINDOW:
                        transSecondPanelResults.append("Status: FULL activation window reached in the selected local simulation.\n");
                        break;
                    case PARTIAL_WINDOW:
                        transSecondPanelResults.append("Status: alpha^L is reached, but alpha^U is not reached in the selected local simulation.\n");
                        break;
                    case BELOW_ALPHA_L:
                        transSecondPanelResults.append("Status: POTENTIAL activation problem: the target is active, but does not reach alpha^L.\n");
                        break;
                    case NEVER_ACTIVE:
                        transSecondPanelResults.append("Status: POTENTIAL activation problem: the target is never active in the tested iterations.\n");
                        break;
                    default:
                        throw new IllegalStateException("Unknown activation-analysis status");
                }

                if (result.isIterationLimitReached()) {
                    transSecondPanelResults.append("The result is limited to the tested maxSteps value.\n");
                } else if (result.isFullWindowReached()) {
                    transSecondPanelResults.append("The calculation stopped after the complete alpha window had been observed.\n");
                }

                transSecondPanelResults.append("\nParameters:\n");
                transSecondPanelResults.append(" - timingScenario = " + timingScenario + "\n");
                transSecondPanelResults.append(" - includeCompetitors = " + includeCompetitors + "\n");
                transSecondPanelResults.append(" - horizonStrategy = " + horizonOptions.getStrategy() + "\n");
                transSecondPanelResults.append(" - extraBlocks = " + horizonOptions.getExtraBlocks() + "\n");
                transSecondPanelResults.append(" - maxStepsCap = " + horizonOptions.getMaxStepsCap() + "\n");

            } catch (Exception ex) {
                transSecondPanelResults.append("Activation analysis error: " + ex.getMessage() + "\n");
                // ex.printStackTrace();
            }

        });
        analP_firstPanel.add(checkActivationWindowsForTransButtonV2);

        HolmesRoundedButton checkKboundButtonV3 = new HolmesRoundedButton("<html>Complete report</html>", "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        checkKboundButtonV3.setMargin(new Insets(0, 0, 0, 0));
        checkKboundButtonV3.setBounds(subPanelX + 150, subPanelY, 130, 32);

        checkKboundButtonV3.addActionListener(actionEvent -> {
            final TransitionXTPN target = theTransition;

            final ArrayList<Transition> allTransitions = overlord.getWorkspace().getProject().getTransitions();
            final int targetID = allTransitions.indexOf(target);
            final String targetSymbol = targetID >= 0 ? "t_" + targetID : "t_?";

            final int extraBlocks = 3;
            final long maxStepsCap = 100_000L;
            final boolean includeCompetitors = true;

            final ActivationAnalyzerXTPNV2.TimingScenario[] timingScenarios = {
                    ActivationAnalyzerXTPNV2.TimingScenario.FAVORABLE_TO_TARGET, ActivationAnalyzerXTPNV2.TimingScenario.OPPOSITE_ENDPOINTS
            };
            final ActivationAnalyzerXTPNV2.HorizonStrategy[] horizonStrategies = {
                    ActivationAnalyzerXTPNV2.HorizonStrategy.BASE, ActivationAnalyzerXTPNV2.HorizonStrategy.PLACE_AWARE
            };
            final String[] timingLabels = {"FAV", "OPP"};
            final String[] horizonLabels = {"BASE", "PLACE"};

            /*
             * prepare(...) only reads the current net and creates immutable models.
             * It is done on the Swing event thread before the longer simulations start,
             * so the background worker does not read a net that could be edited at the
             * same time.
             */
            final ActivationAnalyzerXTPNV2.AnalysisModel[] models = new ActivationAnalyzerXTPNV2.AnalysisModel[2];
            final String[][] errors = new String[2][2];

            for (int s = 0; s < timingScenarios.length; s++) {
                try {
                    models[s] = ActivationAnalyzerXTPNV2.prepare(target, timingScenarios[s], includeCompetitors);
                } catch (Exception ex) {
                    String message = ex.getMessage();
                    if (message == null || message.isBlank()) {
                        message = ex.getClass().getSimpleName();
                    }
                    message = message.replace('\n', ' ').replace('\r', ' ');
                    errors[s][0] = message;
                    errors[s][1] = message;
                }
            }

            transSecondPanelResults.setFont(new Font(Font.MONOSPACED, Font.PLAIN, transSecondPanelResults.getFont().getSize()));
            transSecondPanelResults.setText("Calculating four activation variants for " + targetSymbol + "...\n");
            checkKboundButtonV3.setEnabled(false);

            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() {
                    ActivationAnalyzerXTPNV2.HorizonEstimate[][] horizons = new ActivationAnalyzerXTPNV2.HorizonEstimate[2][2];
                    ActivationAnalyzerXTPNV2.CalculationResult[][] results = new ActivationAnalyzerXTPNV2.CalculationResult[2][2];

                    for (int s = 0; s < timingScenarios.length; s++) {
                        if (models[s] == null) {
                            continue;
                        }

                        for (int h = 0; h < horizonStrategies.length; h++) {
                            try {
                                ActivationAnalyzerXTPNV2.HorizonOptions options = new ActivationAnalyzerXTPNV2.HorizonOptions(horizonStrategies[h], extraBlocks, maxStepsCap);
                                horizons[s][h] = ActivationAnalyzerXTPNV2.estimateMaxSteps(models[s], options);
                                results[s][h] = ActivationAnalyzerXTPNV2.compute(models[s], horizons[s][h].getMaxSteps());
                            } catch (Exception ex) {
                                String message = ex.getMessage();
                                if (message == null || message.isBlank()) {
                                    message = ex.getClass().getSimpleName();
                                }
                                errors[s][h] = message.replace('\n', ' ').replace('\r', ' ');
                            }
                        }
                    }

                    Function<BigDecimal, String> number = value -> {
                        if (value == null) {
                            return "-";
                        }
                        BigDecimal normalized = value.stripTrailingZeros();
                        if (normalized.scale() < 0) {
                            normalized = normalized.setScale(0);
                        }
                        return normalized.toPlainString();
                    };

                    Function<BigDecimal, String> signedNumber = value -> {
                        String plain = number.apply(value);
                        return value.signum() > 0 ? "+" + plain : plain;
                    };

                    Function<BigInteger, String> signedInteger = value -> value.signum() > 0 ? "+" + value : value.toString();
                    Function<ActivationAnalyzerXTPNV2.ActivationStatus, String> shortStatus = status -> {
                        switch (status) {
                            case FULL_WINDOW:
                                return "FULL";
                            case PARTIAL_WINDOW:
                                return "PARTIAL";
                            case BELOW_ALPHA_L:
                                return "BELOW aL";
                            case NEVER_ACTIVE:
                                return "NEVER";
                            default:
                                return "?";
                        }
                    };

                    StringBuilder report = new StringBuilder(4096);
                    report.append("Transition: ")
                            .append(targetSymbol)
                            .append(" (name: ")
                            .append(target.getName()).append(")\n");

                    ActivationAnalyzerXTPNV2.AnalysisModel referenceModel = models[0] != null ? models[0] : models[1];

                    if (referenceModel != null) {
                        report.append("Alpha window: [")
                                .append(number.apply(referenceModel.getAlphaL()))
                                .append(", ")
                                .append(number.apply(referenceModel.getAlphaU()))
                                .append("]\n");
                        report.append("Pre-places: ")
                                .append(referenceModel.getPrePlaceCount())
                                .append(" | producers: ")
                                .append(referenceModel.getProducerCount())
                                .append(" | competitors: ")
                                .append(referenceModel.getCompetitorCount())
                                .append("\n");

                        StringJoiner competitorOrder = new StringJoiner(", ");
                        for (TransitionXTPN competitor :
                                referenceModel.getCompetitorOrder()) {
                            int id = allTransitions.indexOf(competitor);
                            competitorOrder.add(id >= 0 ? "t_" + id : "t_?");
                        }
                        report.append("Competitor order: ")
                                .append(competitorOrder.length() == 0
                                        ? "none"
                                        : competitorOrder.toString())
                                .append("\n");
                    }

                    report.append("Settings: competitors=yes, extraBlocks=")
                            .append(extraBlocks)
                            .append(", maxSteps cap=")
                            .append(maxStepsCap)
                            .append("\n");

                    report.append("Time scale: ");
                    if (models[0] != null) {
                        report.append("FAV=")
                                .append(models[0].getTimeScale());
                    } else {
                        report.append("FAV=error");
                    }
                    report.append(" | ");
                    if (models[1] != null) {
                        report.append("OPP=")
                                .append(models[1].getTimeScale());
                    } else {
                        report.append("OPP=error");
                    }
                    report.append(" iteration(s) per original time unit\n\n");

                    report.append("RESULTS\n");
                    report.append(String.format(
                            Locale.ROOT, "%-12s %10s %-10s %10s %14s %10s%n", "variant", "result[t]", "status", "first@[t]", "maxSteps[it]", "done[it]"));
                    report.append("-----------------------------------------------------------------------\n");

                    for (int s = 0; s < timingScenarios.length; s++) {
                        for (int h = 0; h < horizonStrategies.length; h++) {
                            String variant = timingLabels[s] + " + " + horizonLabels[h];

                            if (errors[s][h] != null) {
                                report.append(String.format(Locale.ROOT, "%-12s %10s %-10s %10s %14s %10s%n", variant, "-", "ERROR", "-", "-", "-"));
                                continue;
                            }

                            ActivationAnalyzerXTPNV2.HorizonEstimate horizon = horizons[s][h];
                            ActivationAnalyzerXTPNV2.CalculationResult result = results[s][h];

                            String maxStepsText = Long.toString(horizon.getMaxSteps()) + (horizon.isCapped() ? "*" : "");

                            report.append(String.format(
                                    Locale.ROOT, "%-12s %10s %-10s %10s %14s %10d%n", variant,
                                    number.apply(result.getMaximumActivationTime()), shortStatus.apply(result.getStatus()), number.apply(result.getFirstMaximumTime()), maxStepsText, result.getPerformedSteps()));
                        }
                    }

                    report.append("\n");
                    report.append("FAV = fast producers and slow competitors\n");
                    report.append("OPP = opposite interval endpoints (not a proven worst case)\n");
                    report.append("BASE / PLACE = method used to calculate maxSteps\n");
                    report.append("[t] = original time; [it] = scaled algorithm iterations\n");
                    report.append("FULL / PARTIAL / BELOW aL / NEVER describe the observed result\n");
                    report.append("If a non-FULL row has done=maxSteps, its result is limited by the selected iteration count.\n");

                    boolean anyCapped = false;
                    for (int s = 0; s < 2; s++) {
                        for (int h = 0; h < 2; h++) {
                            if (horizons[s][h] != null
                                    && horizons[s][h].isCapped()) {
                                if (!anyCapped) {
                                    report.append(
                                            "\nCAPPED maxSteps VALUES (*):\n");
                                    anyCapped = true;
                                }
                                report.append("- ")
                                        .append(timingLabels[s])
                                        .append(" + ")
                                        .append(horizonLabels[h])
                                        .append(": estimated ")
                                        .append(horizons[s][h]
                                                .getUncappedSteps())
                                        .append(", used ")
                                        .append(horizons[s][h]
                                                .getMaxSteps())
                                        .append("\n");
                            }
                        }
                    }

                    report.append("\nDIFFERENCES\n");

                    // Effect of changing timing endpoints, separately for each
                    // maxSteps strategy.
                    for (int h = 0; h < 2; h++) {
                        if (results[0][h] == null || results[1][h] == null) {
                            report.append("- ").append(horizonLabels[h]).append(": timing comparison unavailable\n");
                            continue;
                        }

                        BigDecimal favorable = results[0][h].getMaximumActivationTime();
                        BigDecimal opposite = results[1][h].getMaximumActivationTime();
                        BigDecimal difference = opposite.subtract(favorable);

                        report.append("- ")
                                .append(horizonLabels[h])
                                .append(", FAV -> OPP: result ")
                                .append(number.apply(favorable))
                                .append(" -> ")
                                .append(number.apply(opposite))
                                .append(" (change ")
                                .append(signedNumber.apply(difference))
                                .append("), status ")
                                .append(shortStatus.apply(results[0][h].getStatus()))
                                .append(" -> ")
                                .append(shortStatus.apply(results[1][h].getStatus()))
                                .append("\n");
                    }

                    // Effect of changing only the maxSteps strategy.
                    for (int s = 0; s < 2; s++) {
                        if (results[s][0] == null || results[s][1] == null) {
                            report.append("- ").append(timingLabels[s]).append(": horizon comparison unavailable\n");
                            continue;
                        }

                        BigDecimal baseResult = results[s][0].getMaximumActivationTime();
                        BigDecimal placeResult = results[s][1].getMaximumActivationTime();
                        BigDecimal resultDifference = placeResult.subtract(baseResult);

                        BigInteger stepDifference = BigInteger.valueOf(horizons[s][1].getMaxSteps()).subtract(BigInteger.valueOf(horizons[s][0].getMaxSteps()));

                        report.append("- ")
                                .append(timingLabels[s])
                                .append(", BASE -> PLACE: result ")
                                .append(number.apply(baseResult))
                                .append(" -> ")
                                .append(number.apply(placeResult))
                                .append(" (change ")
                                .append(signedNumber.apply(resultDifference))
                                .append("), maxSteps ")
                                .append(horizons[s][0].getMaxSteps())
                                .append(" -> ")
                                .append(horizons[s][1].getMaxSteps())
                                .append(" (change ")
                                .append(signedInteger.apply(stepDifference))
                                .append(")\n");
                    }

                    boolean anyError = false;
                    for (int s = 0; s < 2; s++) {
                        for (int h = 0; h < 2; h++) {
                            if (errors[s][h] != null) {
                                if (!anyError) {
                                    report.append("\nERRORS\n");
                                    anyError = true;
                                }
                                report.append("- ")
                                        .append(timingLabels[s])
                                        .append(" + ")
                                        .append(horizonLabels[h])
                                        .append(": ")
                                        .append(errors[s][h])
                                        .append("\n");
                            }
                        }
                    }

                    report.append("\nInterpretation: changing BASE to PLACE changes ")
                            .append("only the number of tested iterations. Changing ")
                            .append("FAV to OPP changes the selected transition times ")
                            .append("and can change the simulated behavior itself.\n");
                    return report.toString();
                }

                @Override
                protected void done() {
                    try {
                        transSecondPanelResults.setText(get());
                        transSecondPanelResults.setCaretPosition(0);
                    } catch (Exception ex) {
                        Throwable cause =
                                ex.getCause() != null ? ex.getCause() : ex;
                        String message = cause.getMessage();
                        if (message == null || message.isBlank()) {
                            message = cause.getClass().getSimpleName();
                        }
                        transSecondPanelResults.setText(
                                "Complete activation report failed:\n"
                                        + message);
                    } finally {
                        checkKboundButtonV3.setEnabled(true);
                    }
                }
            }.execute();
        });

        analP_firstPanel.add(checkKboundButtonV3);

        subPanelY+=40;

        transSecondPanelResults = new JTextArea();
        transSecondPanelResults.setLineWrap(true);
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(transSecondPanelResults), BorderLayout.CENTER);
        CreationPanel.setBounds(subPanelX, subPanelY, 755, 580);
        analP_firstPanel.add(CreationPanel);


        secondTabPanel.add(analP_firstPanel);


        JPanel analP_secondPanel = new JPanel(null);
        analP_secondPanel.setBackground(Color.WHITE);
        analP_secondPanel.setBounds(mPanelX, analP_firstPanel.getHeight(), secondTabPanel.getWidth()-24, 60);
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

        //secondTabPanel.add(analP_secondPanel);

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
        portalLabel.setBounds(infPanelX, infPanelY, 50, 20);
        infoPanel.add(portalLabel);

        String port = lang.getText("no"); //no
        if(theTransition.isPortal())
            port = lang.getText("yes"); //yes

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+50, infPanelY, 35, 20);
        infoPanel.add(portalLabel2);

        int preP = 0;
        int postP = 0;
        for (ElementLocation el : theTransition.getElementLocations()) {
            preP += el.getInArcs().size(); //tyle miejsc kieruje tutaj łuk
            postP += el.getOutArcs().size();
        }

        JLabel prePlaceLabel = new JLabel(lang.getText("HNXTPN_entry039")); //Input places:
        prePlaceLabel.setBounds(infPanelX+90, infPanelY, 120, 20);
        infoPanel.add(prePlaceLabel);

        JFormattedTextField prePlaceTextBox = new JFormattedTextField(preP);
        prePlaceTextBox.setBounds(infPanelX+210, infPanelY, 25, 20);
        prePlaceTextBox.setEditable(false);
        infoPanel.add(prePlaceTextBox);

        JLabel postPlaceLabel = new JLabel(lang.getText("HNXTPN_entry040")); //Output places:
        postPlaceLabel.setBounds(infPanelX+240, infPanelY, 120, 20);
        infoPanel.add(postPlaceLabel);

        JFormattedTextField postPlaceTextBox = new JFormattedTextField(postP);
        postPlaceTextBox.setBounds(infPanelX+365, infPanelY, 30, 20);
        postPlaceTextBox.setEditable(false);
        infoPanel.add(postPlaceTextBox);

        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JLabel timeModesInfoLabel = new JLabel(lang.getText("HNXTPN_entry041")); //Time modes:
        timeModesInfoLabel.setBounds(infPanelX, infPanelY, 80, 30);
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
        simTransRepsCheckbox.setBounds(positionX+295, positionY-15, 100, 15);
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
        simTransStepsRepeatedSpinner.setBounds(positionX+300, positionY, 80, 25);
        simTransStepsRepeatedSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simTransNumberOfReps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simTransStepsRepeatedSpinner);

        JLabel simTransIntervalLabel = new JLabel(lang.getText("HNXTPN_entry062")); //Interval:
        simTransIntervalLabel.setBounds(positionX+410, positionY-15, 90, 15);
        chartButtonPanel.add(simTransIntervalLabel);

        SpinnerModel simTransIntervalSpinnerModel = new SpinnerNumberModel(simPlaceInterval, 1, 1000, 10);
        JSpinner simTransIntervalSpinner = new JSpinner(simTransIntervalSpinnerModel);
        simTransIntervalSpinner.setBounds(positionX+410, positionY, 60, 25);
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
                interval++;
                if(interval == maxInterval) {
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
