package holmes.windows.xtpn;

import holmes.darkgui.GUIManager;
import holmes.darkgui.dockwindows.SharedActionsXTPN;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.petrinet.elements.*;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.simulators.StateSimulator;
import holmes.utilities.Tools;
import holmes.windows.xtpn.managers.HolmesNodeInfoXTPNactions;
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
    private PlaceXTPN place;
    private ElementLocation eLocation;
    private HolmesNodeInfoXTPNactions action = new HolmesNodeInfoXTPNactions(this);
    private TransitionXTPN transition;
    private boolean doNotUpdate = false;


    private JPanel mainInfoPanel;
    private JFrame parentFrame;
    public boolean mainSimulatorActive = false;
    private XYSeriesCollection dynamicsSeriesDataSet = null;
    private JFreeChart dynamicsChart;
    private int simSteps = 1000;
    private int repeated = 1;
    private JSpinner transIntervalSpinner;
    private boolean maximumMode = false;
    private boolean singleMode = false;
    private int transInterval = 10;
    private JFormattedTextField avgFiredTextBox;

    //MIEJSCA:
    private HolmesRoundedButton buttonGammaMode;
    private HolmesRoundedButton gammaVisibilityButton;
    private HolmesRoundedButton tokensWindowButton; //przycisk podokna tokenó XTPN
    private JFormattedTextField tokensTextBox; //liczba tokenów
    private JFormattedTextField gammaMinTextField;
    private JFormattedTextField gammaMaxTextField;

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




    /**
     * Konstruktor do tworzenia okna właściwości miejsca.
     * @param place PlaceXTPN - obiekt miejsca
     * @param parent JFrame - okno wywołujące
     */
    public HolmesNodeInfoXTPN(PlaceXTPN place, ElementLocation eloc, JFrame parent) {
        overlord = GUIManager.getDefaultGUIManager();
        parentFrame = parent;
        this.place = place;
        this.eLocation = eloc;
        setTitle("Node: "+place.getName());
        setBackground(Color.WHITE);
        initializeCommon();

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
        this.transition = transition;
        this.eLocation = eloc;
        setTitle("Node: "+transition.getName());
        setBackground(Color.WHITE);
        initializeCommon();

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

    /**
     * Metoda agregująca główne, wspólne elementy interfejsu miejsc/tranzycji.
     */
    private void initializeCommon() {
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

        mainSimulatorActive = true; //TODO

        parentFrame.setEnabled(false);
        setResizable(false);
        setLocation(20, 20);
        setSize(new Dimension(800, 600));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parentFrame.setEnabled(true);
            }
        });

        this.setVisible(true);
    }

    /**
     * Metoda odpowiedzialna za elementy interfejsu właściwości dla miejsca sieci.
     */
    private JPanel initializePlaceInfo() {
        mainInfoPanel = new JPanel(null);
        mainInfoPanel.setBounds(0, 0, 800, 600);
        mainInfoPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(mPanelX, mPanelY, mainInfoPanel.getWidth()-22, 160);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Structural data:"));

        int infPanelX = 10;
        int infPanelY = 20;

        //************************* NEWLINE *************************

        JLabel labelID = new JLabel("ID:");
        labelID.setBounds(infPanelX, infPanelY, 20, 20);
        infoPanel.add(labelID);

        int id = overlord.getWorkspace().getProject().getPlaces().indexOf(place);
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
        nameField.setValue(place.getName());
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                GUIManager.getDefaultGUIManager().log("Error (611156405) | Exception:  "+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            place.setName(newName);
            action.repaintGraphPanel(place);
        });
        infoPanel.add(nameField);

        JLabel commmentLabel = new JLabel("Comments:", JLabel.LEFT);
        commmentLabel.setBounds(infPanelX+460, infPanelY-22, 100, 20);
        infoPanel.add(commmentLabel);

        JTextArea commentField = new JTextArea(place.getComment());
        commentField.setLineWrap(true);
        commentField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newComment = "";
                if(field != null)
                    newComment = field.getText();

                place.setComment(newComment);
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
        if(place.isPortal())
            port = "yes";

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+40, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        int inTrans = 0;
        int outTrans = 0;
        for (ElementLocation el : place.getElementLocations()) {
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
        if(place.isGammaModeActive()) {
            buttonGammaMode.setNewText("<html>Gamma: ON</html>");
            buttonGammaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonGammaMode.setNewText("<html>Gamma: OFF</html>");
            buttonGammaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonGammaMode.addActionListener(e -> {
            action.buttonGammaModeSwitch(e, place, tokensWindowButton, gammaVisibilityButton);
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
        if(place.isGammaModeActive()) {
            if (place.isGammaRangeVisible()) {
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
            action.gammaVisButtonSwitch(e, place);
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
        formatter.setMaximumFractionDigits(place.getFractionForPlaceXTPN());
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        Double example = 3.14;

        gammaMinTextField = new JFormattedTextField(formatter);
        gammaMinTextField.setValue(example);
        gammaMinTextField.setValue(place.getGammaMinValue());
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

            if( !(SharedActionsXTPN.access().setGammaMinTime(min, place, eLocation) ) ) {
                doNotUpdate = true;
                field.setValue(place.getGammaMinValue());
                doNotUpdate = false;
                overlord.markNetChange();
            }
            doNotUpdate = true;
            gammaMaxTextField.setValue(place.getGammaMaxValue());
            doNotUpdate = false;
            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        gammaMaxTextField = new JFormattedTextField(formatter);
        gammaMaxTextField.setValue(example);
        gammaMaxTextField.setValue(place.getGammaMaxValue());
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
            if( !(SharedActionsXTPN.access().setGammaMaxTime(max, place, eLocation) ) ) {
                doNotUpdate = true;
                field.setValue(place.getGammaMaxValue());
                doNotUpdate = false;
                overlord.markNetChange();
            }
            doNotUpdate = true;
            gammaMinTextField.setValue(place.getGammaMinValue());
            doNotUpdate = false;
            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        if(!place.isGammaModeActive()) {
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
        if(!place.isGammaModeActive()) {
            tokensWindowButton.setEnabled(false);
        }
        tokensWindowButton.addActionListener(actionEvent -> new HolmesXTPNtokens(place, this, place.accessMultiset(), place.isGammaModeActive()));
        infoPanel.add(tokensWindowButton);


        JLabel tokenNumberInfoLabel = new JLabel("Current number:");
        tokenNumberInfoLabel.setBounds(infPanelX+190, infPanelY, 120, 20);
        infoPanel.add(tokenNumberInfoLabel);

        tokensTextBox = new JFormattedTextField("0");
        tokensTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        tokensTextBox.setEditable(false);
        infoPanel.add(tokensTextBox);
        printTokenNumber();


        //************************* NEWLINE *************************
        infPanelY += 20;
        //************************* NEWLINE *************************



        JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
        chartMainPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-10, 245);
        chartMainPanel.add(createChartPanel(place), BorderLayout.CENTER);
        chartMainPanel.setBackground(Color.WHITE);
        mainInfoPanel.add(chartMainPanel);


        JPanel chartButtonPanel = panelButtonsPlace(infoPanel, chartMainPanel); //dolny panel przycisków
        mainInfoPanel.add(chartButtonPanel);

        fillPlaceDynamicData(chartMainPanel);

        return mainInfoPanel;
    }




    private JPanel initializePlaceInvPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, 600, 480);
        panel.setBackground(Color.WHITE);

        //int posX = 20;
        //int posY = 20;
        //panel.add(progressBar);
        return panel;
    }

    /**
     * Metoda tworzy dolny panel / pasek przycisków okna miejsc.
     * @param infoPanel JPanel - panel informacji o tranzycji
     * @param chartMainPanel JPanel - panel wykresu
     * @return JPanel - panel dolnych przycisków
     */
    private JPanel panelButtonsPlace(JPanel infoPanel, JPanel chartMainPanel) {
        JPanel chartButtonPanel = new JPanel(null);
        chartButtonPanel.setBounds(0, infoPanel.getHeight()+chartMainPanel.getHeight(), mainInfoPanel.getWidth()-10, 50);
        chartButtonPanel.setBackground(Color.WHITE);

        int chartX = 5;
        int chartY_1st = 0;
        int chartY_2nd = 15;

        JButton acqDataButton = new JButton("SimStart");
        acqDataButton.setBounds(chartX, chartY_2nd, 110, 25);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
        acqDataButton.addActionListener(actionEvent -> acquireNewPlaceData());
        chartButtonPanel.add(acqDataButton);

        JLabel labelSteps = new JLabel("Sim. Steps:");
        labelSteps.setBounds(chartX+120, chartY_1st, 70, 15);
        chartButtonPanel.add(labelSteps);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 50000, 100);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(chartX +120, chartY_2nd, 80, 25);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simSteps = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsSpinner);

        JLabel labelRep = new JLabel("Repeated:");
        labelRep.setBounds(chartX+210, chartY_1st, 70, 15);
        chartButtonPanel.add(labelRep);

        SpinnerModel simStepsRepeatedSpinnerModel = new SpinnerNumberModel(repeated, 1, 50, 1);
        JSpinner simStepsRepeatedSpinner = new JSpinner(simStepsRepeatedSpinnerModel);
        simStepsRepeatedSpinner.setBounds(chartX +210, chartY_2nd, 60, 25);
        simStepsRepeatedSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            repeated = (int) spinner.getValue();
        });
        chartButtonPanel.add(simStepsRepeatedSpinner);


        JLabel label1 = new JLabel("Mode:");
        label1.setBounds(chartX+280, chartY_1st, 50, 15);
        chartButtonPanel.add(label1);

        final JComboBox<String> simMode = new JComboBox<String>(new String[] {"50/50 mode", "Maximum mode", "Single mode"});
        simMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
        simMode.setBounds(chartX+280, chartY_2nd, 120, 25);
        simMode.setSelectedIndex(0);
        simMode.setMaximumRowCount(6);
        simMode.addActionListener(actionEvent -> {
            int selected = simMode.getSelectedIndex();
            if(selected == 0) {
                maximumMode = false;
                singleMode = false;
            } else if(selected == 1) {
                maximumMode = true;
                singleMode = false;
            } else {
                singleMode = true;
            }
        });
        chartButtonPanel.add(simMode);

        String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
        final JComboBox<String> simNetMode = new JComboBox<String>(simModeName);
        simNetMode.setBounds(chartX+400, chartY_2nd, 120, 25);
        simNetMode.setSelectedIndex(0);
        simNetMode.addActionListener(actionEvent -> {
            int selectedModeIndex = simNetMode.getSelectedIndex();
            switch (selectedModeIndex) {
                //case 0 -> choosenNetType = SimulatorGlobals.SimNetType.BASIC;
                //case 1 -> choosenNetType = SimulatorGlobals.SimNetType.TIME;
                //case 2 -> choosenNetType = SimulatorGlobals.SimNetType.HYBRID;
            }
        });
        chartButtonPanel.add(simNetMode);

        return chartButtonPanel;
    }

    /**
     * Metoda odpowiedzialna za elementy interfejsu właściwości dla tranzycji sieci.
     */
    private JPanel initializeTransitionInfo() {
        mainInfoPanel = new JPanel(null);
        mainInfoPanel.setBounds(0, 0, 800, 600);
        mainInfoPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(mPanelX, mPanelY, mainInfoPanel.getWidth()-22, 200);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Structural data:"));
        infoPanel.setBackground(Color.WHITE);


        int infPanelX = 10;
        int infPanelY = 20;

        //************************* NEWLINE *************************

        JLabel labelID = new JLabel("ID:");
        labelID.setBounds(infPanelX, infPanelY, 20, 20);
        infoPanel.add(labelID);

        int id = overlord.getWorkspace().getProject().getTransitions().indexOf(transition);
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
        nameField.setValue(transition.getName());
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                GUIManager.getDefaultGUIManager().log("Error (212156495) | Exception:  "+ex.getMessage(), "error", true);
            }
            String newName = field.getText();
            transition.setName(newName);
            action.repaintGraphPanel(transition);

            //action.parentTableUpdate(parentFrame, newName);
        });
        infoPanel.add(nameField);

        JLabel commmentLabel = new JLabel("Comments:", JLabel.LEFT);
        commmentLabel.setBounds(infPanelX+460, infPanelY-22, 100, 20);
        infoPanel.add(commmentLabel);

        JTextArea commentField = new JTextArea(transition.getComment());
        commentField.setLineWrap(true);
        commentField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newComment = "";
                if(field != null)
                    newComment = field.getText();

                transition.setComment(newComment);
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
        if(transition.isPortal())
            port = "yes";

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+40, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        int preP = 0;
        int postP = 0;
        for (ElementLocation el : transition.getElementLocations()) {
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
        if(transition.isAlphaModeActive()) {
            buttonAlphaMode.setNewText("<html>Alpha: ON</html>");
            buttonAlphaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonAlphaMode.setNewText("<html>Alpha: OFF</html>");
            buttonAlphaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonAlphaMode.addActionListener(e -> {

            doNotUpdate = true;
            SharedActionsXTPN.access().buttonAlphaSwitchMode(e, transition, this, tauVisibilityButton, buttonClassXTPNmode, alphaMaxTextField, eLocation);
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
        if(transition.isAlphaModeActive()) {
            if (transition.isAlphaRangeVisible()) {
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
            if (transition.isAlphaRangeVisible()) { //wyłączamy
                transition.setAlphaRangeVisibility(false);
                button.setNewText("<html>\u03B1: Hidden<html>");
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { // włączamy
                transition.setAlphaRangeVisibility(true);
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
        if(transition.isAlphaModeActive()) {
            buttonBetaMode.setNewText("<html>Beta: ON</html>");
            buttonBetaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
        } else {
            buttonBetaMode.setNewText("<html>Beta: OFF</html>");
            buttonBetaMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
        }
        buttonBetaMode.addActionListener(e -> {
            doNotUpdate = true;
            SharedActionsXTPN.access().buttonBetaSwitchMode(e, transition, this, tauVisibilityButton, buttonClassXTPNmode, betaMaxTextField, eLocation);
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
        if(transition.isBetaModeActive()) {
            if (transition.isBetaRangeVisible()) {
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
            if (transition.isBetaRangeVisible()) { //wyłączamy
                transition.setBetaRangeVisibility(false);
                button.setNewText("<html>\u03B2: Hidden<html>");
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { // włączamy
                transition.setBetaRangeVisibility(true);
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
        if(!transition.isAlphaModeActive() && !transition.isBetaModeActive()) {
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
            SharedActionsXTPN.access().buttonTransitionToXTPN_classicSwitchMode(e, transition, this, alphaMaxTextField, betaMaxTextField, eLocation);
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
        if(transition.isAlphaModeActive() || transition.isBetaModeActive()) {
            if (transition.isTauTimerVisible()) {
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
            if (transition.isTauTimerVisible()) { //wyłączamy
                transition.setTauTimersVisibility(false);
                button.setNewText("<html>\u03C4: Hidden<html>");
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            } else { //włączamy
                transition.setTauTimersVisibility(true);
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
        alphaMinTextField.setValue(transition.getAlphaMinValue());
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
            SharedActionsXTPN.access().setAlfaMinTime(min, transition, eLocation);

            doNotUpdate = true;
            alphaMaxTextField.setValue(transition.getAlphaMaxValue());
            field.setValue(transition.getAlphaMinValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);

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
                System.out.println(ex.getMessage());
            }

            double max = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setAlfaMaxTime(max, transition, eLocation);

            doNotUpdate = true;
            alphaMinTextField.setValue(transition.getAlphaMinValue());
            field.setValue(transition.getAlphaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        if(!transition.isAlphaModeActive()) {
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
        betaMinTextField.setValue(transition.getBetaMinValue());
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
            SharedActionsXTPN.access().setBetaMinTime(min, transition, eLocation);

            doNotUpdate = true;
            field.setValue(transition.getBetaMinValue());
            betaMaxTextField.setValue(transition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
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
                System.out.println(ex.getMessage());
            }
            double max = Double.parseDouble(""+field.getValue());
            SharedActionsXTPN.access().setBetaMaxTime(max, transition, eLocation);

            doNotUpdate = true;
            betaMinTextField.setValue(transition.getBetaMinValue());
            field.setValue(transition.getBetaMaxValue());
            doNotUpdate = false;
            overlord.markNetChange();
            setFieldStatus(false);

            WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
            ws.getGraphPanel().getSelectionManager().selectOneElementLocation(eLocation);
        });

        if(!transition.isBetaModeActive()) {
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




        //************************* NEWLINE *************************
        infPanelY += 25;
        //************************* NEWLINE *************************

        JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
        chartMainPanel.setBorder(BorderFactory.createTitledBorder("Transition chart"));
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-10, 245);
        chartMainPanel.add(createChartPanel(transition), BorderLayout.CENTER);
        mainInfoPanel.add(chartMainPanel);

        JPanel chartButtonPanel = panelButtonsTransition(infoPanel, chartMainPanel);
        mainInfoPanel.add(chartButtonPanel);

        try {
            fillTransitionDynamicData(avgFiredTextBox, chartMainPanel, chartButtonPanel);
        } catch (Exception ex) {
            overlord.log("Error (576101739) | Exception: "+ex.getMessage(), "error", false);
        }
        return mainInfoPanel;
    }

    private void setFieldStatus(boolean isPlace) {
        if(isPlace) {
            if(place.isGammaModeActive()) {
                buttonGammaMode.setNewText("<html>Gamma: ON</html>");
                buttonGammaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                gammaVisibilityButton.setEnabled(true);
                gammaMinTextField.setEnabled(true);
                gammaMaxTextField.setEnabled(true);

                if(place.isGammaRangeVisible()) {
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
            gammaMinTextField.setValue(place.getGammaMinValue());
            gammaMaxTextField.setValue(place.getGammaMaxValue());
            doNotUpdate = false;

        } else { //dla tranzycji
            if(transition.isAlphaModeActive()) {
                buttonAlphaMode.setNewText("<html>Alpha: ON</html>");
                buttonAlphaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                buttonClassXTPNmode.setNewText("<html>XTPN<html>");
                buttonClassXTPNmode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                alphaMinTextField.setEnabled(true);
                alphaMaxTextField.setEnabled(true);

                alphaVisibilityButton.setEnabled(true);
                tauVisibilityButton.setEnabled(true);

                if(transition.isAlphaRangeVisible()) {
                    alphaVisibilityButton.setNewText("<html>\u03B1: Visible<html>");
                    alphaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    alphaVisibilityButton.setNewText("<html>\u03B1: Hidden<html>");
                    alphaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }

                if(transition.isTauTimerVisible()) {
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

            if(transition.isBetaModeActive()) {
                buttonBetaMode.setNewText("<html>Beta: ON</html>");
                buttonBetaMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                buttonClassXTPNmode.setNewText("<html>XTPN<html>");
                buttonClassXTPNmode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                betaMinTextField.setEnabled(true);
                betaMaxTextField.setEnabled(true);

                betaVisibilityButton.setEnabled(true);
                tauVisibilityButton.setEnabled(true);

                if(transition.isBetaRangeVisible()) {
                    betaVisibilityButton.setNewText("<html>\u03B2: Visible<html>");
                    betaVisibilityButton.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                } else {
                    betaVisibilityButton.setNewText("<html>\u03B2: Hidden<html>");
                    betaVisibilityButton.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                }
                if(transition.isTauTimerVisible()) {
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


            if(!transition.isAlphaModeActive() && !transition.isBetaModeActive()) { //both offline
                buttonClassXTPNmode.setNewText("<html>Classical<html>");
                buttonClassXTPNmode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

                tauVisibilityButton.setEnabled(false);
            }

            doNotUpdate = true;
            alphaMinTextField.setValue(transition.getAlphaMinValue());
            alphaMaxTextField.setValue(transition.getAlphaMaxValue());
            betaMinTextField.setValue(transition.getBetaMinValue());
            betaMaxTextField.setValue(transition.getBetaMaxValue());
            doNotUpdate = false;
        }
    }

    private JPanel initializeTransInvPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, 600, 480);
        panel.setBackground(Color.WHITE);

        //int posX = 20;
        //int posY = 20;
        //panel.add(progressBar);

        return panel;
    }

    /**
     * Metoda tworzy dolny panel / pasek przycisków okna tranzycji.
     * @param infoPanel JPanel - panel informacji o tranzycji
     * @param chartMainPanel JPanel - panel wykresu
     * @return JPanel - panel dolnych przycisków okna tramzycji
     */
    private JPanel panelButtonsTransition(JPanel infoPanel, JPanel chartMainPanel) {
        JPanel chartButtonPanel = new JPanel(null);
        chartButtonPanel.setBounds(0, infoPanel.getHeight()+chartMainPanel.getHeight(), mainInfoPanel.getWidth()-10, 50);
        chartButtonPanel.setBackground(Color.WHITE);

        int chartX = 5;
        int chartY_1st = 0;
        int chartY_2nd = 15;

        JButton acqDataButton = new JButton("SimStart");
        acqDataButton.setBounds(chartX, chartY_2nd, 110, 25);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
        acqDataButton.addActionListener(actionEvent -> acquireNewTransitionData());
        chartButtonPanel.add(acqDataButton);

        JLabel labelSteps = new JLabel("Sim. Steps:");
        labelSteps.setBounds(chartX+120, chartY_1st, 70, 15);
        chartButtonPanel.add(labelSteps);

        SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simSteps, 0, 50000, 100);
        JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
        simStepsSpinner.setBounds(chartX+120, chartY_2nd, 80, 25);
        simStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            simSteps = (int) spinner.getValue();

            //update spinner przerw/srednich dla tranzycji:
            int cVal = simSteps / 100;
            int mVal = simSteps / 5;
            if(cVal < 1)
                cVal = 1;
            if(cVal > mVal) {
                cVal = 1;
                mVal = 20;
            }
            try {
                SpinnerNumberModel spinnerClustersModel = new SpinnerNumberModel(cVal, 1, mVal, 1);
                transIntervalSpinner.setModel(spinnerClustersModel);
                transInterval = cVal;
            } catch (Exception ex) {
                overlord.log("Cannot update transition interval for simulator (Transition Info Windows).", "warning", true);
            }
        });
        chartButtonPanel.add(simStepsSpinner);

        JLabel labelInterval = new JLabel("Interval:");
        labelInterval.setBounds(chartX+210, chartY_1st, 80, 15);
        chartButtonPanel.add(labelInterval);

        int maxVal = simSteps / 10;
        SpinnerModel intervSpinnerModel = new SpinnerNumberModel(transInterval, 1, maxVal, 1);
        transIntervalSpinner = new JSpinner(intervSpinnerModel);
        transIntervalSpinner.setBounds(chartX+210, chartY_2nd, 60, 25);
        transIntervalSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            transInterval = (int) spinner.getValue();
            //clearTransitionsChart();
        });
        chartButtonPanel.add(transIntervalSpinner);

        JLabel labelMode = new JLabel("Simulation mode:");
        labelMode.setBounds(chartX+280, chartY_1st, 110, 15);
        chartButtonPanel.add(labelMode);

        final JComboBox<String> simMode = new JComboBox<String>(new String[] {"50/50 mode", "Maximum mode", "Single mode"});
        simMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
        simMode.setBounds(chartX+280, chartY_2nd, 120, 25);
        simMode.setSelectedIndex(0);
        simMode.setMaximumRowCount(6);
        simMode.addActionListener(actionEvent -> {
            int selected = simMode.getSelectedIndex();
            if(selected == 0) {
                maximumMode = false;
                singleMode = false;
            } else if(selected == 1) {
                maximumMode = true;
                singleMode = false;
            } else {
                singleMode = true;
            }
        });
        chartButtonPanel.add(simMode);

        String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
        final JComboBox<String> simNetMode = new JComboBox<String>(simModeName);
        simNetMode.setBounds(chartX+400, chartY_2nd, 120, 25);

        //if(choosenNetType == SimulatorGlobals.SimNetType.TIME)
        //    simNetMode.setSelectedIndex(1);
        //else
        //    simNetMode.setSelectedIndex(0);

        simNetMode.addActionListener(actionEvent -> {
            int selectedModeIndex = simNetMode.getSelectedIndex();
            switch (selectedModeIndex) {
                //case 0 -> choosenNetType = SimulatorGlobals.SimNetType.BASIC;
                //case 1 -> choosenNetType = SimulatorGlobals.SimNetType.TIME;
                //case 2 -> choosenNetType = SimulatorGlobals.SimNetType.HYBRID;
            }
        });
        chartButtonPanel.add(simNetMode);

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
            //title.setBackgroundPaint(Color.white);
            title.setFont(new Font("Dialog", Font.PLAIN, 20));
            title.setExpandToFitSpace(true);
            title.setPaint(Color.red);
            title.setText("Chart unavailable, main simulator is active.");
        }
    }

    /**
     * Metoda wypełnia pola danych dynamicznych dla tranzycji, tj. symuluje 1000 kroków sieci na bazie
     * czego ustala prawdopodobieństwo uruchomienia tranzycji oraz przedstawia wykres dla symulacji.
     * @param avgFiredTextBox JFormattedTextField - pole z wartością procentową
     * @param chartMainPanel JPanel - panel wykresu
     */
    private void fillTransitionDynamicData(JFormattedTextField avgFiredTextBox, JPanel chartMainPanel,
                                           JPanel chartButtonPanel) {
        if(!mainSimulatorActive) {
            ArrayList<Integer> dataVector = acquireNewTransitionData();
            if(dataVector != null) {
                int sum = dataVector.get(dataVector.size()-2);
                int steps = dataVector.get(dataVector.size()-1);
                double avgFired = sum;
                avgFired /= steps;
                avgFired *= 100; // * 100%
                avgFiredTextBox.setText(Tools.cutValue(avgFired)+"%");
            }
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
     * ustalonej liczby kroków. Testy są powtarzane ustaloną liczbę razy. Wyniki zapisuje na wykresie.
     */
    private void acquireNewPlaceData() {
        StateSimulator ss = new StateSimulator();

        SimulatorGlobals ownSettings = new SimulatorGlobals();
        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN);

        //ss.initiateSim(false, ownSettings);

        ArrayList<Integer> dataVector = ss.simulateNetSinglePlace(simSteps, place, false);
        ArrayList<ArrayList<Integer>> dataMatrix = new ArrayList<ArrayList<Integer>>();
        dataMatrix.add(dataVector);

        int problemCounter = 0;
        int rep_succeed = 1;
        for(int i=1; i<repeated; i++) {
            ss.clearData();
            ArrayList<Integer> newData = ss.simulateNetSinglePlace(simSteps, place, false);
            if(newData.size() < dataVector.size()) { //powtórz test, zły rozmiar danych
                problemCounter++;
                i--;
                if(problemCounter == 10) {
                    overlord.log("Unable to gather "+repeated+" data vectors (places) of same size. "
                            + "State simulator cannot proceed "+simSteps+ " steps. First pass had: "+ dataVector.size() +" steps.", "error", true);
                    break;
                } else {
                    continue;
                }
            } else { //ok, taki sam lub dłuższy
                rep_succeed++;
                dataMatrix.add(newData);

            }
        }

        dynamicsSeriesDataSet.removeAllSeries();
        XYSeries series = new XYSeries("Number of tokens");

        if(repeated != 1) {
            ArrayList<Double> dataDVector = new ArrayList<Double>();
            for(int i=0; i<repeated; i++) {
                if(i==0) {
                    for(int j=0; j<dataMatrix.get(0).size(); j++) {
                        dataDVector.add((double)dataMatrix.get(0).get(j));
                    }
                } else {
                    for(int j=0; j<dataMatrix.get(i).size(); j++) {
                        double oldval = dataDVector.get(j);
                        oldval += dataMatrix.get(i).get(j);
                        dataDVector.set(j, oldval);
                    }
                }
            }

            for(int step=0; step<dataDVector.size(); step++) {
                double value = dataDVector.get(step);
                value /= rep_succeed;
                series.add(step, value);
            }
        } else {
            if(dataVector != null) {
                for(int step=0; step<dataVector.size(); step++) {
                    int value = dataVector.get(step);
                    series.add(step, value);
                }
            }
        }
        dynamicsSeriesDataSet.addSeries(series);
        dataMatrix.clear();
    }

    /**
     * Metoda aktywuje symulator dla jednej tranzycji w ustalonym wcześniej trybie i dla wcześniej
     * ustalonej liczby kroków. Wyniki zapisuje na wykresie, zwraca też wektor danych.
     * @return ArrayList[Integer] - wektor danych z symulatora
     */
    private ArrayList<Integer> acquireNewTransitionData() {
        StateSimulator ss = new StateSimulator();

        SimulatorGlobals ownSettings = new SimulatorGlobals();
        ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN);

        //ss.initiateSim(false, ownSettings);

        ArrayList<Integer> dataVector = ss.simulateNetSingleTransition(simSteps, transition, false);

        dynamicsSeriesDataSet.removeAllSeries();
        XYSeries series = new XYSeries("Average firing");
        if(dataVector != null) {
            for(int step=0; step<dataVector.size()-2; step++) {
                double value = 0; //suma odpaleń w przedziale czasu
                int interval = transInterval;
                if(step+interval >= dataVector.size()-2)
                    interval = dataVector.size() - 2 - step;

                for(int i=0; i<interval; i++) {
                    try {
                        value += dataVector.get(step+i);
                    } catch (Exception e) {

                    }
                }
                value /= interval;
                value *= 100;
                series.add(step, value);
                step += (interval-1);
            }
        }
        dynamicsSeriesDataSet.addSeries(series);

        int sum = dataVector.get(dataVector.size()-2);
        int steps = dataVector.get(dataVector.size()-1);
        double avgFired = sum;
        avgFired /= steps;
        avgFired *= 100; // * 100%
        avgFiredTextBox.setText(Tools.cutValue(avgFired)+"%");

        return dataVector;
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

        boolean showLegend = false;
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
        if(place.isGammaModeActive()) {
            tokens = place.accessMultiset().size();
        } else {
            tokens = place.getTokensNumber();
        }

        tokensTextBox.setText(""+tokens);
    }


}
