package holmes.windows.xtpn;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.simulators.StateSimulator;
import holmes.utilities.Tools;
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
import java.text.ParseException;
import java.util.ArrayList;

public class HolmesNodeInfoXTPN extends JFrame {
    private GUIManager overlord;
    private Place place;
    private Transition transition;
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

    /**
     * Konstruktor do tworzenia okna właściwości miejsca.
     * @param place PlaceXTPN - obiekt miejsca
     * @param papa JFrame - okno wywołujące
     */
    public HolmesNodeInfoXTPN(PlaceXTPN place, JFrame papa) {
        overlord = GUIManager.getDefaultGUIManager();
        parentFrame = papa;
        this.place = place;
        setTitle("Node: "+place.getName());

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


        main.add(tabbedPane);

    }

    /**
     * Konstruktor do tworzenia okna właściwości tranzycji.
     * @param transition TransitionXTPN - obiekt tranzycji
     * @param papa JFrame - okno wywołujące
     */
    public HolmesNodeInfoXTPN(TransitionXTPN transition, JFrame papa) {
        overlord = GUIManager.getDefaultGUIManager();
        parentFrame = papa;
        this.transition = transition;
        setTitle("Node: "+transition.getName());

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

        main.add(tabbedPane);
    }

    /**
     * Metoda agregująca główne, wspólne elementy interfejsu miejsc/tranzycji.
     */
    private void initializeCommon() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (181278513) | Exception:  "+ex.getMessage(), "error", false);
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
        setSize(new Dimension(600, 480));

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
        mainInfoPanel.setBounds(0, 0, 600, 480);
        mainInfoPanel.setBackground(Color.WHITE);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(mPanelX, mPanelY, mainInfoPanel.getWidth()-10, 130);
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Place general information:"));

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

        JLabel portalLabel = new JLabel("Portal:");
        portalLabel.setBounds(infPanelX+60, infPanelY, 40, 20);
        infoPanel.add(portalLabel);

        String port = "no";
        if(place.isPortal())
            port = "yes";

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+100, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        JLabel tokenLabel = new JLabel("Tokens:", JLabel.LEFT);
        tokenLabel.setBounds(infPanelX+130, infPanelY, 50, 20);
        infoPanel.add(tokenLabel);

        int tok = place.getTokensNumber();
        boolean problem = false;
        if(tok < 0) {
            overlord.log("Negative number of tokens in "+place.getName(), "error", true);
            tok = 0;
            problem = true;
        }
        SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(tok, 0, Integer.MAX_VALUE, 1);
        JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
        tokenSpinner.setLocation(infPanelX+180, infPanelY);
        tokenSpinner.setSize(60, 20);
        tokenSpinner.setMaximumSize(new Dimension(60,20));
        tokenSpinner.setMinimumSize(new Dimension(60,20));
        if(mainSimulatorActive || problem) {
            tokenSpinner.setEnabled(false);
        }
        tokenSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int tokens = (int) spinner.getValue();
            //action.setTokens(place, tokens);

            if(overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN == 0) {
                ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
                overlord.getWorkspace().getProject().accessStatesManager().getStatePN(0).setTokens(places.indexOf(place), tokens);
            }
        });
        infoPanel.add(tokenSpinner);


        int inTrans = 0;
        int outTrans = 0;
        for (ElementLocation el : place.getElementLocations()) {
            inTrans += el.getInArcs().size(); //tyle tranzycji kieruje tutaj łuk
            outTrans += el.getOutArcs().size();
        }

        JLabel inTransLabel = new JLabel("IN-Trans:");
        inTransLabel.setBounds(infPanelX+250, infPanelY, 60, 20);
        infoPanel.add(inTransLabel);

        JFormattedTextField inTransTextBox = new JFormattedTextField(inTrans);
        inTransTextBox.setBounds(infPanelX+310, infPanelY, 30, 20);
        inTransTextBox.setEditable(false);
        infoPanel.add(inTransTextBox);

        JLabel outTransLabel = new JLabel("OUT-Trans:");
        outTransLabel.setBounds(infPanelX+345, infPanelY, 65, 20);
        infoPanel.add(outTransLabel);

        JFormattedTextField outTransTextBox = new JFormattedTextField(outTrans);
        outTransTextBox.setBounds(infPanelX+420, infPanelY, 30, 20);
        outTransTextBox.setEditable(false);
        infoPanel.add(outTransTextBox);

        infPanelY += 20;
        //************************* NEWLINE *************************

        JLabel labelName = new JLabel("Name:");
        labelName.setBounds(infPanelX, infPanelY, 40, 20);
        infoPanel.add(labelName);

        DefaultFormatter format = new DefaultFormatter();
        format.setOverwriteMode(false);
        JFormattedTextField nameField = new JFormattedTextField(format);
        nameField.setLocation(infPanelX+60, infPanelY);
        nameField.setSize(460, 20);
        nameField.setValue(place.getName());
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                GUIManager.getDefaultGUIManager().log("Error (611156405) | Exception:  "+ex.getMessage(), "error", false);
            }
            String newName = field.getText();

            changeName(place, newName);
            //action.parentTableUpdate(parentFrame, newName);
        });
        infoPanel.add(nameField);

        infPanelY += 20;
        //************************* NEWLINE *************************

        JLabel commmentLabel = new JLabel("Comm.:", JLabel.LEFT);
        commmentLabel.setBounds(infPanelX, infPanelY, 50, 20);
        infoPanel.add(commmentLabel);

        JTextArea commentField = new JTextArea(place.getComment());
        commentField.setLineWrap(true);
        commentField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newComment = "";
                if(field != null)
                    newComment = field.getText();

                //action.changeComment(place, newComment);
            }
        });

        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+60, infPanelY, 460, 60);

        infoPanel.add(creationPanel);
        //infPanelY += 60;

        mainInfoPanel.add(infoPanel);

        JPanel chartMainPanel = new JPanel(new BorderLayout()); //panel wykresów, globalny, bo musimy
        chartMainPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
        chartMainPanel.setBounds(0, infoPanel.getHeight(), mainInfoPanel.getWidth()-10, 245);
        chartMainPanel.add(createChartPanel(place), BorderLayout.CENTER);
        mainInfoPanel.add(chartMainPanel);


        JPanel chartButtonPanel = panelButtonsPlace(infoPanel, chartMainPanel); //dolny panel przycisków
        mainInfoPanel.add(chartButtonPanel);

        fillPlaceDynamicData(chartMainPanel);

        return mainInfoPanel;
    }

    private JPanel initializePlaceInvPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, 600, 480);

        int posX = 20;
        int posY = 20;

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
        mainInfoPanel.setBounds(0, 0, 600, 450);

        int mPanelX = 0;
        int mPanelY = 0;

        //panel informacji podstawowych
        JPanel infoPanel = new JPanel(null);
        infoPanel.setBounds(mPanelX, mPanelY, 580, 130);
        infoPanel.setBorder(BorderFactory.createTitledBorder("Transition general information:"));

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

        JLabel portalLabel = new JLabel("Portal:");
        portalLabel.setBounds(infPanelX+60, infPanelY, 40, 20);
        infoPanel.add(portalLabel);

        String port = "no";
        if(transition.isPortal())
            port = "yes";

        JLabel portalLabel2 = new JLabel(port);
        portalLabel2.setBounds(infPanelX+100, infPanelY, 30, 20);
        infoPanel.add(portalLabel2);

        JLabel avgFiredLabel = new JLabel("Avg.f.:", JLabel.LEFT);
        avgFiredLabel.setBounds(infPanelX+130, infPanelY, 50, 20);
        infoPanel.add(avgFiredLabel);

        avgFiredTextBox = new JFormattedTextField(id);
        //wypełnianie niżej
        avgFiredTextBox.setBounds(infPanelX+180, infPanelY, 50, 20);
        avgFiredTextBox.setEditable(false);
        infoPanel.add(avgFiredTextBox);

        int preP = 0;
        int postP = 0;
        for (ElementLocation el : transition.getElementLocations()) {
            preP += el.getInArcs().size(); //tyle miejsc kieruje tutaj łuk
            postP += el.getOutArcs().size();
        }

        JLabel prePlaceLabel = new JLabel("PRE-Place:");
        prePlaceLabel.setBounds(infPanelX+235, infPanelY, 65, 20);
        infoPanel.add(prePlaceLabel);

        JFormattedTextField prePlaceTextBox = new JFormattedTextField(preP);
        prePlaceTextBox.setBounds(infPanelX+300, infPanelY, 30, 20);
        prePlaceTextBox.setEditable(false);
        infoPanel.add(prePlaceTextBox);

        JLabel postPlaceLabel = new JLabel("POST-Place:");
        postPlaceLabel.setBounds(infPanelX+335, infPanelY, 75, 20);
        infoPanel.add(postPlaceLabel);

        JFormattedTextField postPlaceTextBox = new JFormattedTextField(postP);
        postPlaceTextBox.setBounds(infPanelX+420, infPanelY, 30, 20);
        postPlaceTextBox.setEditable(false);
        infoPanel.add(postPlaceTextBox);

        infPanelY += 20;
        //************************* NEWLINE *************************

        JLabel labelName = new JLabel("Name:");
        labelName.setBounds(infPanelX, infPanelY, 40, 20);
        infoPanel.add(labelName);

        DefaultFormatter format = new DefaultFormatter();
        format.setOverwriteMode(false);
        JFormattedTextField nameField = new JFormattedTextField(format);
        nameField.setLocation(infPanelX+60, infPanelY);
        nameField.setSize(460, 20);
        nameField.setValue(transition.getName());
        nameField.addPropertyChangeListener("value", e -> {
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
            }
            String newName = field.getText();

            //action.changeName(transition, newName);
            //action.parentTableUpdate(parentFrame, newName);
        });
        infoPanel.add(nameField);

        infPanelY += 20;
        //************************* NEWLINE *************************

        JLabel commmentLabel = new JLabel("Comm.:", JLabel.LEFT);
        commmentLabel.setBounds(infPanelX, infPanelY, 50, 20);
        infoPanel.add(commmentLabel);

        JTextArea commentField = new JTextArea(transition.getComment());
        commentField.setLineWrap(true);
        commentField.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                String newComment = "";
                if(field != null)
                    newComment = field.getText();

                //action.changeComment(transition, newComment);
            }
        });

        JPanel creationPanel = new JPanel();
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        creationPanel.setBounds(infPanelX+60, infPanelY, 460, 60);

        infoPanel.add(creationPanel);
        infPanelY += 60;
        mainInfoPanel.add(infoPanel);

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
        } catch (Exception e) {}
        return mainInfoPanel;
    }

    private JPanel initializeTransInvPanel() {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, 600, 480);

        int posX = 20;
        int posY = 20;


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






    /**
     * Metoda zmieniająca nazwę dla miejsca sieci.
     * @param place Place - obiekt miejsca
     * @param newName String - nowa nazwa
     */
    public void changeName(Node place, String newName) {
        place.setName(newName);
        repaintGraphPanel(place);
    }

    /**
     * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
     */
    private void repaintGraphPanel(Node node) {
        int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(node.getElementLocations().get(0).getSheetID());
        GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        graphPanel.repaint();
    }
}
