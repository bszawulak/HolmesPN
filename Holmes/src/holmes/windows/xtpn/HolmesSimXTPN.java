package holmes.windows.xtpn;

import holmes.darkgui.GUIManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.IRandomGenerator;
import holmes.petrinet.simulators.xtpn.GraphicalSimulatorXTPN;
import holmes.petrinet.simulators.xtpn.StateSimDataContainer;
import holmes.petrinet.simulators.xtpn.StateSimulatorXTPN;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;
import holmes.windows.managers.HolmesStatesManager;
import holmes.workspace.ExtensionFileFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static holmes.windows.xtpn.HolmesSimXTPNActions.crunchifySortMapXTPN;

/**
 * Klasa okna modułu symulatora stanów XTPN sieci.
 */
public class HolmesSimXTPN extends JFrame {
    @Serial
    private static final long serialVersionUID = 5381991734385357453L;
    private JFrame ego;
    private GUIManager overlord;
    private HolmesSimXTPNActions action = new HolmesSimXTPNActions();
    private StateSimulatorXTPN ssim;
    public boolean doNotUpdate = false;

    private JProgressBar progressBar;
    private int transInterval = 100;
    private int placesStepsInterval = 100;

    private int placesTimeInterval = 10;
    private boolean sortedP = false;
    private boolean sortedT = false;

    private ArrayList<Integer> placesInChart;
    private ArrayList<String> placesInChartStr;
    private ArrayList<Integer> transInChart;
    private ArrayList<String> transInChartStr;

    private XYSeriesCollection placesSeriesDataSet = null;
    private XYSeriesCollection transitionsSeriesDataSet = null;
    private JFreeChart placesChart;
    private JFreeChart transitionsChart;
    private int transChartType = 0; //suma odpaleń, 1=konkretne tranzycje
    private int placesChartType = 0; //j.w. dla miejsc

    private JPanel placesJPanel;
    private JPanel transitionsJPanel;
    private JSpinner transIntervalSpinner;
    private JSpinner placesStepsIntervalSpinner;
    private JSpinner placesTimeIntervalSpinner;

    private SpinnerModel placeRepsSpinnerModel = null;  //reps
    private JCheckBox qSimXTPNRecordStepsCheckbox = null;  //simSettings.setRecordSomeSteps
    private SpinnerModel selStepsSpinnerModel = null; //simSettings.setRecordedSteps
    private JCheckBox qSimXTPNCompStatsCheckbox = null; //simSettings.setRecordStatictis
    private SpinnerModel placeStepsSpinnerModel = null; //steps
    private SpinnerModel intervalPlaceStepsSpinnerModel = null; //placesStepsInterval
    private JCheckBox qSimXTPNStatsTimeCheckbox = null; //simSettings.setSimulateTime
    private SpinnerModel placeTimeSpinnerModel = null; //simSettings.setSimMaxTime_XTPN


    private JComboBox<String> placesCombo = null;
    private JComboBox<String> transitionsCombo = null;
    private ChartPropertiesXTPN chartDetails;

    private JLabel selStateLabel;
    private HolmesRoundedButton stateManagerButton;
    private HolmesRoundedButton acqDataButton;
    //reset:
    private JPanel knockoutTab;		//ZAKLADKA KNOCKOUT SIM //TODO
    private boolean workInProgress;

    public JTabbedPane mainTabPanel;

    //XTPN:
    StateSimDataContainer simDataBox = new StateSimDataContainer();
    private boolean isTimeSimulationXTPN = false;
    private JLabel placeSimSteps = null;
    private JLabel placeSimTime = null;
    private JLabel placeSimCompTime = null;

    /**
     * Konstruktor domyślny obiektu klasy StateSimulator (podokna Holmes)
     */
    public HolmesSimXTPN(GUIManager overlord) {
        this.overlord = overlord;
        ego = this;
        ssim = new StateSimulatorXTPN();
        chartDetails = new ChartPropertiesXTPN();
        placesInChart = new ArrayList<Integer>();
        placesInChartStr = new ArrayList<String>();

        transInChart = new ArrayList<Integer>();
        transInChartStr = new ArrayList<String>();

        initializeComponents();
        initiateListeners();
    }

    public HolmesSimXTPN returnFrame() {
        return this;
    }

    /**
     * Metoda pomocnica konstuktora, odpowiada za utworzenie elementów graficznych okna.
     */
    private void initializeComponents() {
        setVisible(false);
        setTitle("State Simulator");
        setLocation(30,30);
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (418594315) | Exception:  "+ex.getMessage(), "error", true);
        }
        setSize(new Dimension(1120, 750));

        JPanel main = new JPanel(new BorderLayout()); //główny panel okna
        add(main);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Places dynamics", Tools.getResIcon16("/icons/stateSim/placesDyn.png"), createPlacesTabPanel(), "Places dynamics");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        tabbedPane.addTab("Transitions dynamics", Tools.getResIcon16("/icons/stateSim/transDyn.png"), createTransitionsTabPanel(), "Transistions dynamics");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        mainTabPanel = new JTabbedPane();
        JPanel firstTab = new JPanel(new BorderLayout());
        firstTab.add(craeteDataAcquisitionPanel(), BorderLayout.NORTH);
        firstTab.add(tabbedPane, BorderLayout.CENTER);

        mainTabPanel.addTab("Standard mode", Tools.getResIcon16("/icons/stateSim/simpleSimTab.png"), firstTab, "Simple mode");

        knockoutTab = new JPanel();
        mainTabPanel.addTab("KnockoutSim", Tools.getResIcon16("/icons/stateSim/knockSimTab.png"), knockoutTab, "KnockoutSim");

        main.add(mainTabPanel, BorderLayout.CENTER);

        repaint();
    }

    /**
     * Metoda tworzy panel opcji okna symulatora stanów sieci.
     * @return JPanel - panel opcji pobrania danych
     */
    private JPanel craeteDataAcquisitionPanel() {
        JPanel dataAcquisitionPanel = new JPanel(null);
        dataAcquisitionPanel.setBackground(Color.WHITE);
        dataAcquisitionPanel.setBorder(BorderFactory.createTitledBorder("Data acquisition"));
        dataAcquisitionPanel.setPreferredSize(new Dimension(670, 110));

        int posXda = 10;
        int posYda = 20;

        acqDataButton = new HolmesRoundedButton("<html><center>Simulation<br>start</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        acqDataButton.setBounds(posXda, posYda, 110, 40);
        acqDataButton.setMargin(new Insets(0, 0, 0, 0));
        acqDataButton.setFocusPainted(false);
        acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
        acqDataButton.addActionListener(actionEvent -> acquireDataFromSimulation());
        dataAcquisitionPanel.add(acqDataButton);

        HolmesRoundedButton cancelButton = new HolmesRoundedButton("<html><center>Cancel</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        cancelButton.setText("<html>&nbsp;&nbsp;&nbsp;STOP&nbsp;&nbsp;&nbsp;</html>");
        cancelButton.setIcon(Tools.getResIcon32("/icons/simulationKnockout/stopIcon.png"));
        cancelButton.setBounds(posXda, posYda+45, 110, 30);
        cancelButton.setMargin(new Insets(0, 0, 0, 0));
        cancelButton.addActionListener(actionEvent -> ssim.setCancelStatus(true));

        cancelButton.setFocusPainted(false);
        dataAcquisitionPanel.add(cancelButton);

        JButton simSettingsButton = new HolmesRoundedButton("<html><center>Simulation<br>Settings</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        simSettingsButton.setBounds(posXda+120, posYda, 130, 40);
        simSettingsButton.setMargin(new Insets(0, 0, 0, 0));
        simSettingsButton.setFocusPainted(false);
        simSettingsButton.setIcon(Tools.getResIcon32("/icons/simSettings/setupIcon.png"));
        simSettingsButton.setToolTipText("Set simulator options.");
        simSettingsButton.setEnabled(false);
        //simSettingsButton.addActionListener(actionEvent -> new HolmesSimSetup(ego));

        simSettingsButton.setEnabled(false);
        dataAcquisitionPanel.add(simSettingsButton);

        stateManagerButton = new HolmesRoundedButton("<html><center>States<br>Manager</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        stateManagerButton.setIcon(Tools.getResIcon32("/icons/stateManager/stManIcon.png"));
        stateManagerButton.setBounds(posXda+260, posYda, 130, 40);
        stateManagerButton.setMargin(new Insets(0, 0, 0, 0));
        stateManagerButton.setFocusPainted(false);
        stateManagerButton.addActionListener(actionEvent -> new HolmesStatesManager());
        dataAcquisitionPanel.add(stateManagerButton);

        JLabel stateLabel0 = new JLabel("Selected m0 state ID: ");
        stateLabel0.setBounds(posXda+400, posYda, 130, 20);
        dataAcquisitionPanel.add(stateLabel0);

        selStateLabel = new JLabel(""+overlord.getWorkspace().getProject().accessStatesManager().selectedStateXTPN);
        selStateLabel.setBounds(posXda+530, posYda, 40, 20);
        dataAcquisitionPanel.add(selStateLabel);

        JButton clearDataButton = new HolmesRoundedButton("<html><center>Clear all<br>data</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        clearDataButton.setBounds(posXda+550, posYda, 110, 40);
        clearDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/clearData.png"));
        clearDataButton.setToolTipText("Clear all charts and data vectors. Reset simulator.");
        clearDataButton.setFocusPainted(false);
        clearDataButton.addActionListener(actionEvent -> {
            clearPlacesChart();
            clearTransitionsChart();
            clearAllData();
        });
        dataAcquisitionPanel.add(clearDataButton);


        progressBar = new JProgressBar();
        progressBar.setBounds(posXda+120, posYda+40, 540, 40);
        progressBar.setMaximum(100);
        progressBar.setMinimum(0);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        Border border = BorderFactory.createTitledBorder("Progress");
        progressBar.setBorder(border);
        progressBar.setBackground(Color.WHITE);
        dataAcquisitionPanel.add(progressBar);


        JPanel XTPNoptionsPanel = new JPanel(null);
        XTPNoptionsPanel.setBackground(Color.WHITE);
        XTPNoptionsPanel.setBorder(BorderFactory.createTitledBorder("XTPN sim options"));
        XTPNoptionsPanel.setBounds(posXda+670, posYda-20, 380, 110);
        dataAcquisitionPanel.add(XTPNoptionsPanel);

        int internalX = 10;
        int internalY = 20;

        JLabel labelReps = new JLabel("Repetitions:");
        labelReps.setBounds(internalX, internalY, 70, 20);
        XTPNoptionsPanel.add(labelReps);

        //int repValue = overlord.simSettings.simRepetitions_XTPN;
        placeRepsSpinnerModel = new SpinnerNumberModel(1, 1, 100, 10);
        JSpinner placesRepsSpinner = new JSpinner(placeRepsSpinnerModel);
        placesRepsSpinner.setBounds(internalX+70, internalY, 50, 20);
        placesRepsSpinner.addChangeListener(e -> {
            if(doNotUpdate)
                return;

            JSpinner spinner = (JSpinner) e.getSource();
            int tmp = (int) spinner.getValue();
            overlord.simSettings.setSimRepetitions_XTPN(tmp);
        });
        XTPNoptionsPanel.add(placesRepsSpinner);

        qSimXTPNRecordStepsCheckbox = new JCheckBox("Sel. steps");
        qSimXTPNRecordStepsCheckbox.setBackground(Color.WHITE);
        qSimXTPNRecordStepsCheckbox.setBounds(internalX+130, internalY, 90, 20);
        qSimXTPNRecordStepsCheckbox.setSelected(overlord.simSettings.isTimeSimulation_XTPN());
        qSimXTPNRecordStepsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;

            JCheckBox box = (JCheckBox) e.getSource();
            overlord.simSettings.setPartialRecordingStetsStatus(box.isSelected() );
        });
        XTPNoptionsPanel.add(qSimXTPNRecordStepsCheckbox);

        selStepsSpinnerModel = new SpinnerNumberModel(10, 2, 100, 10);
        JSpinner selStepsSpinner = new JSpinner(selStepsSpinnerModel);
        selStepsSpinner.setBounds(internalX+220, internalY, 50, 20);
        selStepsSpinner.addChangeListener(e -> {
            if(doNotUpdate)
                return;

            JSpinner spinner = (JSpinner) e.getSource();
            int tmp = (int) spinner.getValue();
            overlord.simSettings.setRecordedSteps( tmp );
        });
        XTPNoptionsPanel.add(selStepsSpinner);

        qSimXTPNCompStatsCheckbox = new JCheckBox("Comp. stats");
        qSimXTPNCompStatsCheckbox.setBackground(Color.WHITE);
        qSimXTPNCompStatsCheckbox.setBounds(internalX+130, internalY, 90, 20);
        qSimXTPNCompStatsCheckbox.setSelected(overlord.simSettings.isTimeSimulation_XTPN());
        qSimXTPNCompStatsCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;

            JCheckBox box = (JCheckBox) e.getSource();
            overlord.simSettings.setStatsRecordingStatus(box.isSelected() );
        });
        XTPNoptionsPanel.add(qSimXTPNCompStatsCheckbox);

        internalY += 20;

        JLabel labelSteps = new JLabel("Steps:");
        labelSteps.setBounds(internalX, internalY, 40, 20);
        XTPNoptionsPanel.add(labelSteps);

        long stepValue = overlord.simSettings.getSimSteps_XTPN();
        placeStepsSpinnerModel = new SpinnerNumberModel((int)stepValue, 0, 100000000, 10000);
        JSpinner placesStepsSpinner = new JSpinner(placeStepsSpinnerModel);
        placesStepsSpinner.setBounds(internalX+45, internalY, 90, 20);
        placesStepsSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int tmp = (int) spinner.getValue();
            overlord.simSettings.setSimSteps_XTPN(tmp);
        });
        XTPNoptionsPanel.add(placesStepsSpinner);

        JLabel labelPInterval = new JLabel("Interval:");
        labelPInterval.setBounds(internalX+140, internalY, 50, 20);
        XTPNoptionsPanel.add(labelPInterval);

        int placeStepsInterval = (int)overlord.simSettings.getSimSteps_XTPN ()/10;
        intervalPlaceStepsSpinnerModel = new SpinnerNumberModel(placesStepsInterval, 0, placeStepsInterval, 100);
        placesStepsIntervalSpinner = new JSpinner(intervalPlaceStepsSpinnerModel);
        placesStepsIntervalSpinner.setBounds(internalX+190, internalY, 60, 20);
        placesStepsIntervalSpinner.addChangeListener(e -> {
            if(doNotUpdate)
                return;

            JSpinner spinner = (JSpinner) e.getSource();
            int tmp = (int) spinner.getValue();
            placesStepsInterval = tmp;
            clearPlacesChart();
        });
        XTPNoptionsPanel.add(placesStepsIntervalSpinner);

        internalY += 20;

        qSimXTPNStatsTimeCheckbox = new JCheckBox("Time simulation");
        qSimXTPNStatsTimeCheckbox.setBackground(Color.WHITE);
        qSimXTPNStatsTimeCheckbox.setBounds(internalX, internalY, 120, 20);
        qSimXTPNStatsTimeCheckbox.setSelected(overlord.simSettings.isTimeSimulation_XTPN());
        qSimXTPNStatsTimeCheckbox.addItemListener(e -> {
            if(doNotUpdate)
                return;

            JCheckBox box = (JCheckBox) e.getSource();
            overlord.simSettings.setTimeSimulationStatus_XTPN(box.isSelected() );
        });
        XTPNoptionsPanel.add(qSimXTPNStatsTimeCheckbox);

        internalY += 20;

        JLabel labelTime = new JLabel("Time:");
        labelTime.setBounds(internalX, internalY, 40, 20);
        XTPNoptionsPanel.add(labelTime);

        double timeValue = overlord.simSettings.getSimTime_XTPN();
        placeTimeSpinnerModel = new SpinnerNumberModel((int)timeValue, 0, 1000000, 5000);
        JSpinner placesTimeSpinner = new JSpinner(placeTimeSpinnerModel);
        placesTimeSpinner.setBounds(internalX+45, internalY, 90, 20);
        placesTimeSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int tmp = (int) spinner.getValue();
            overlord.simSettings.setSimTime_XTPN( tmp );
        });
        XTPNoptionsPanel.add(placesTimeSpinner);

        JLabel labelPTimeInterval = new JLabel("Interval:");
        labelPTimeInterval.setBounds(internalX+140, internalY, 50, 20);
        XTPNoptionsPanel.add(labelPTimeInterval);

        int placeTimeMaxInterval = (int)overlord.simSettings.getSimTime_XTPN() / 100;
        SpinnerModel intervalPlaceTimeSpinnerModel = new SpinnerNumberModel(placesTimeInterval, 0, placeTimeMaxInterval, 10);
        placesTimeIntervalSpinner = new JSpinner(intervalPlaceTimeSpinnerModel);
        placesTimeIntervalSpinner.setBounds(internalX+190, internalY, 60, 20);
        placesTimeIntervalSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            int tmp = (int) spinner.getValue();
            placesTimeInterval = tmp;
            clearPlacesChart();
        });
        XTPNoptionsPanel.add(placesTimeIntervalSpinner);



        return dataAcquisitionPanel;
    }

    /**
     * Metoda ta tworzy panel dla zakładki miejsc.
     * @return JPanel - panel
     */
    private JPanel createPlacesTabPanel() {
        JPanel result = new JPanel(new BorderLayout());
        result.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        result.add(topPanel, BorderLayout.PAGE_START);

        JPanel placesChartOptionsPanel = new JPanel(null);
        placesChartOptionsPanel.setBackground(Color.WHITE);
        placesChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Places chart options"));
        placesChartOptionsPanel.setPreferredSize(new Dimension(500, 120));

        int posXchart = 10;
        int posYchart = 20;

        JButton showAllButton = new HolmesRoundedButton("<html><center>Show all</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        showAllButton.setBounds(posXchart, posYchart, 120, 24);
        showAllButton.setMargin(new Insets(0, 0, 0, 0));
        showAllButton.setFocusPainted(false);
        showAllButton.setIcon(Tools.getResIcon16("/icons/stateSim/showAll.png"));
        showAllButton.setToolTipText("Show average numbers of token in places through simulation steps.");
        showAllButton.addActionListener(actionEvent -> showAllPlacesData());
        placesChartOptionsPanel.add(showAllButton);

        JButton showNotepadButton = new HolmesRoundedButton("<html><center>Show notepad</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        showNotepadButton.setBounds(posXchart+130, posYchart, 120, 24);
        showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
        showNotepadButton.setFocusPainted(false);
        showNotepadButton.setIcon(Tools.getResIcon16("/icons/stateSim/showNotepad.png"));
        showNotepadButton.setToolTipText("Show average numbers of token in places through simulation steps.");
        showNotepadButton.addActionListener(actionEvent -> showPlacesAllInNotepad());
        placesChartOptionsPanel.add(showNotepadButton);
        placesChartOptionsPanel.setEnabled(false);

        JCheckBox sortedCheckBox = new JCheckBox("Sorted by tokens");
        sortedCheckBox.setBounds(posXchart+460, posYchart+10, 130, 20);
        sortedCheckBox.setBackground(Color.WHITE);
        sortedCheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            sortedP = abstractButton.getModel().isSelected();
            fillPlacesAndTransitionsData();
        });
        placesChartOptionsPanel.add(sortedCheckBox);
        posYchart += 30;

        JLabel label1 = new JLabel("Places:");
        label1.setBounds(posXchart, posYchart, 70, 20);
        placesChartOptionsPanel.add(label1);

        String[] dataP = { "---" };
        placesCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
        placesCombo.setLocation(posXchart + 75, posYchart+2);
        placesCombo.setSize(500, 20);
        placesCombo.setSelectedIndex(0);
        placesCombo.setMaximumRowCount(12);
        placesCombo.addActionListener(actionEvent -> {

        });
        placesChartOptionsPanel.add(placesCombo);

        posYchart += 30;

        JButton addPlaceButton = new HolmesRoundedButton("<html><center>Add to chart</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        addPlaceButton.setBounds(posXchart, posYchart+2, 110, 24);
        addPlaceButton.setMargin(new Insets(0, 0, 0, 0));
        addPlaceButton.setFocusPainted(false);
        addPlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/addChart.png"));
        addPlaceButton.setToolTipText("Add data about place tokens to the chart.");
        addPlaceButton.addActionListener(actionEvent -> {
            if(simDataBox == null)
                return;

            int selected = placesCombo.getSelectedIndex();
            if(selected>0) {
                String name = placesCombo.getSelectedItem().toString();
                int sel = action.getRealNodeID(name);
                if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID

                name = trimNodeName(name);
                placesInChart.set(sel, 1);
                placesInChartStr.set(sel, name);

                addNewPlaceSeries(sel, name);
                updatePlacesGraphicChart("places");
            }
        });
        placesChartOptionsPanel.add(addPlaceButton);

        JButton removePlaceButton = new HolmesRoundedButton("<html><center>Remove</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        removePlaceButton.setBounds(posXchart+120, posYchart+2, 110, 24);
        removePlaceButton.setMargin(new Insets(0, 0, 0, 0));
        removePlaceButton.setFocusPainted(false);
        removePlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/removeChart.png"));
        removePlaceButton.setToolTipText("Remove data about place tokens from the chart.");
        removePlaceButton.addActionListener(actionEvent -> {
            if(simDataBox == null)
                return;

            int selected = placesCombo.getSelectedIndex();
            if(selected>0) {
                String name = placesCombo.getSelectedItem().toString();
                int sel = action.getRealNodeID(name);
                if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID

                name = trimNodeName(name);
                placesInChart.set(sel, -1);
                placesInChartStr.set(sel, "");
                removePlaceSeries(name);
            }
        });
        placesChartOptionsPanel.add(removePlaceButton);

        JButton clearPlacesChartButton = new HolmesRoundedButton("<html><center>Clear chart</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        clearPlacesChartButton.setBounds(posXchart+240, posYchart+2, 110, 24);
        clearPlacesChartButton.setMargin(new Insets(0, 0, 0, 0));
        clearPlacesChartButton.setFocusPainted(false);
        clearPlacesChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/clearChart.png"));
        clearPlacesChartButton.setToolTipText("Clears the chart.");
        clearPlacesChartButton.addActionListener(actionEvent -> clearPlacesChart());
        placesChartOptionsPanel.add(clearPlacesChartButton);

        JButton savePlacesChartButton = new HolmesRoundedButton("<html><center>Save image</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        savePlacesChartButton.setBounds(posXchart+360, posYchart+2, 110, 24);
        savePlacesChartButton.setMargin(new Insets(0, 0, 0, 0));
        savePlacesChartButton.setFocusPainted(false);
        savePlacesChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/saveImage.png"));
        savePlacesChartButton.setToolTipText("Saves the chart as image file.");
        savePlacesChartButton.addActionListener(actionEvent -> saveChartImage("places", 1200, 1024));
        placesChartOptionsPanel.add(savePlacesChartButton);

        JButton showPlaceButton = new HolmesRoundedButton("<html><center>Find place</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        showPlaceButton.setBounds(posXchart+480, posYchart+2, 110, 24);
        showPlaceButton.setMargin(new Insets(0, 0, 0, 0));
        showPlaceButton.setFocusPainted(false);
        showPlaceButton.setIcon(Tools.getResIcon16("/icons/stateSim/findNode.png"));
        showPlaceButton.setToolTipText("Find selected place within the net.");
        showPlaceButton.addActionListener(actionEvent -> {
            int selected = placesCombo.getSelectedIndex();
            if(selected>0) {
                String name = placesCombo.getSelectedItem().toString();
                int sel = action.getRealNodeID(name);
                if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID

                GUIManager.getDefaultGUIManager().getSearchWindow().fillComboBoxesData();
                GUIManager.getDefaultGUIManager().getSearchWindow().selectedManually(true, sel);
            }
        });
        placesChartOptionsPanel.add(showPlaceButton);
        topPanel.add(placesChartOptionsPanel, BorderLayout.CENTER);


        placeSimSteps = new JLabel("Place simulation steps: 0");
        placeSimSteps.setBounds(posXchart+620, posYchart-60, 200, 20);
        placesChartOptionsPanel.add(placeSimSteps);

        placeSimTime = new JLabel("Place simulation time: 0");
        placeSimTime.setBounds(posXchart+620, posYchart-40, 200, 20);
        placesChartOptionsPanel.add(placeSimTime);

        placeSimCompTime = new JLabel("Simulation computing time: 0");
        placeSimCompTime.setBounds(posXchart+620, posYchart-20, 230, 20);
        placesChartOptionsPanel.add(placeSimCompTime);

        //************************************************************************************************************
        //************************************************************************************************************

        JPanel placesChartGraphicPanel = new JPanel(null);
        placesChartGraphicPanel.setBackground(Color.WHITE);
        placesChartGraphicPanel.setBorder(BorderFactory.createTitledBorder("Chart graphic"));
        placesChartGraphicPanel.setPreferredSize(new Dimension(200, 100));

        int posXGchart = 10;
        int posYGchart = 20;

        ButtonGroup groupWidth = new ButtonGroup();
        JRadioButton width1 = new JRadioButton("Thin line");
        width1.setBackground(Color.WHITE);
        width1.setBounds(posXGchart, posYGchart, 90, 20);
        width1.setActionCommand("0");
        width1.addActionListener(actionEvent -> {
            if(((AbstractButton) actionEvent.getSource()).isSelected()) {
                chartDetails.p_StrokeWidth = 1.0f;
                updatePlacesGraphicChart("places");
            }
        });
        placesChartGraphicPanel.add(width1);
        groupWidth.add(width1);
        groupWidth.setSelected(width1.getModel(), true);

        JRadioButton width2 = new JRadioButton("Normal line");
        width2.setBackground(Color.WHITE);
        width2.setBounds(posXGchart, posYGchart+20, 90, 20);
        width2.setActionCommand("1");
        width2.addActionListener(actionEvent -> {
            if(((AbstractButton) actionEvent.getSource()).isSelected()) {
                chartDetails.p_StrokeWidth = 2.0f;
                updatePlacesGraphicChart("places");
            }
        });
        placesChartGraphicPanel.add(width2);
        groupWidth.add(width2);

        JRadioButton width3 = new JRadioButton("Thick line");
        width3.setBackground(Color.WHITE);
        width3.setBounds(posXGchart, posYGchart+40, 90, 20);
        width3.setActionCommand("2");
        width3.addActionListener(actionEvent -> {
            if(((AbstractButton) actionEvent.getSource()).isSelected()) {
                chartDetails.p_StrokeWidth = 3.0f;
                updatePlacesGraphicChart("places");
            }
        });
        placesChartGraphicPanel.add(width3);
        groupWidth.add(width3);

        topPanel.add(placesChartGraphicPanel, BorderLayout.EAST);
        //************************************************************************************************************
        //************************************************************************************************************

        placesJPanel = new JPanel(new BorderLayout());
        placesJPanel.setBorder(BorderFactory.createTitledBorder("Places chart"));
        placesJPanel.add(createPlacesChartPanel(), BorderLayout.CENTER);
        result.add(placesJPanel, BorderLayout.CENTER);

        return result;
    }

    /**
     * Metoda ta tworzy panel dla zakładki tranzycji.
     * @return JPanel - panel
     */
    private JPanel createTransitionsTabPanel() {
        JPanel result = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        result.add(topPanel, BorderLayout.PAGE_START);

        JPanel transChartOptionsPanel = new JPanel(null);
        transChartOptionsPanel.setBackground(Color.WHITE);
        transChartOptionsPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart options"));
        transChartOptionsPanel.setPreferredSize(new Dimension(500, 120));

        int posXchart = 10;
        int posYchart = 20;


        JButton showAllButton = new HolmesRoundedButton("<html><center>Show all</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        showAllButton.setBounds(posXchart, posYchart, 120, 24);
        showAllButton.setMargin(new Insets(0, 0, 0, 0));
        showAllButton.setFocusPainted(false);
        showAllButton.setIcon(Tools.getResIcon16("/icons/stateSim/showAll.png"));
        showAllButton.setToolTipText("Show average numbers of firings of transitions through simulation steps.");
        showAllButton.addActionListener(actionEvent -> showAllTransData());

        showAllButton.setEnabled(false);
        transChartOptionsPanel.add(showAllButton);

        JButton showNotepadButton = new HolmesRoundedButton("<html><center>Show notepad</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        showNotepadButton.setBounds(posXchart+130, posYchart, 120, 24);
        showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
        showNotepadButton.setFocusPainted(false);
        showNotepadButton.setIcon(Tools.getResIcon16("/icons/stateSim/showNotepad.png"));
        showNotepadButton.setToolTipText("Show average numbers of firings of transitions through simulation steps.");
        showNotepadButton.addActionListener(actionEvent -> showTransAllInNotepad());

        showNotepadButton.setEnabled(false);
        transChartOptionsPanel.add(showNotepadButton);

        JLabel label1 = new JLabel("Interval:");
        label1.setBounds(posXchart+280, posYchart+2, 70, 20);
        transChartOptionsPanel.add(label1);

        int mValue = (int)overlord.simSettings.getSimSteps_XTPN () / 10;
        SpinnerModel intervSpinnerModel = new SpinnerNumberModel(transInterval, 1, mValue, 10);
        transIntervalSpinner = new JSpinner(intervSpinnerModel);
        transIntervalSpinner.setBounds(posXchart+330, posYchart+3, 60, 20);
        transIntervalSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            transInterval = (int) spinner.getValue();
            clearTransitionsChart();
        });
        transChartOptionsPanel.add(transIntervalSpinner);

        JCheckBox sortedCheckBox = new JCheckBox("Sorted by firing");
        sortedCheckBox.setBounds(posXchart+460, posYchart+10, 130, 20);
        sortedCheckBox.setBackground(Color.WHITE);
        sortedCheckBox.addActionListener(actionEvent -> {
            AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
            sortedT = abstractButton.getModel().isSelected();
            fillPlacesAndTransitionsData();
        });
        transChartOptionsPanel.add(sortedCheckBox);
        posYchart += 30;

        JLabel label2 = new JLabel("Transition:");
        label2.setBounds(posXchart, posYchart, 70, 20);
        transChartOptionsPanel.add(label2);

        String[] dataP = { "---" };
        transitionsCombo = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
        transitionsCombo.setLocation(posXchart + 75, posYchart+2);
        transitionsCombo.setSize(500, 20);
        transitionsCombo.setSelectedIndex(0);
        transitionsCombo.setMaximumRowCount(12);
        transChartOptionsPanel.add(transitionsCombo);

        posYchart += 30;

        JButton addTransitionButton = new HolmesRoundedButton("<html><center>Add to chart</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        addTransitionButton.setBounds(posXchart, posYchart+2, 110, 24);
        addTransitionButton.setMargin(new Insets(0, 0, 0, 0));
        addTransitionButton.setFocusPainted(false);
        addTransitionButton.setIcon(Tools.getResIcon16("/icons/stateSim/addChart.png"));
        addTransitionButton.setToolTipText("Add data about transition firing to the chart.");
        addTransitionButton.addActionListener(actionEvent -> {
            if(simDataBox == null)
                return;

            int selected = transitionsCombo.getSelectedIndex();
            if(selected>0) {
                String name = transitionsCombo.getSelectedItem().toString();
                int sel = action.getRealNodeID(name);
                if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID
                name = trimNodeName(name);

                transInChart.set(sel, 1);
                transInChartStr.set(sel, name);

                addNewTransitionSeries(sel, name);
                updatePlacesGraphicChart("transitions");
            }
        });
        transChartOptionsPanel.add(addTransitionButton);

        JButton removeTransitionButton = new HolmesRoundedButton("<html><center>Remove</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        removeTransitionButton.setBounds(posXchart+120, posYchart+2, 110, 24);
        removeTransitionButton.setMargin(new Insets(0, 0, 0, 0));
        removeTransitionButton.setFocusPainted(false);
        removeTransitionButton.setIcon(Tools.getResIcon16("/icons/stateSim/removeChart.png"));
        removeTransitionButton.setToolTipText("Remove data about transition firing from the chart.");
        removeTransitionButton.addActionListener(actionEvent -> {
            if(simDataBox == null)
                return;

            int selected = transitionsCombo.getSelectedIndex();
            if(selected>0) {
                String name = transitionsCombo.getSelectedItem().toString();
                int sel = action.getRealNodeID(name);
                if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID
                name = trimNodeName(name);

                transInChart.set(sel, -1);
                transInChartStr.set(sel, "");

                removeTransitionSeries(name);
            }
        });
        transChartOptionsPanel.add(removeTransitionButton);

        JButton clearTransChartButton = new HolmesRoundedButton("<html><center>Clear chart</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        clearTransChartButton.setBounds(posXchart+240, posYchart+2, 110, 24);
        clearTransChartButton.setMargin(new Insets(0, 0, 0, 0));
        clearTransChartButton.setFocusPainted(false);
        clearTransChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/clearChart.png"));
        clearTransChartButton.setToolTipText("Clears the transitions chart.");
        clearTransChartButton.addActionListener(actionEvent -> clearTransitionsChart());
        transChartOptionsPanel.add(clearTransChartButton);

        JButton saveTransitionsChartButton = new HolmesRoundedButton("<html><center>Save image</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        saveTransitionsChartButton.setBounds(posXchart+360, posYchart+2, 110, 24);
        saveTransitionsChartButton.setMargin(new Insets(0, 0, 0, 0));
        saveTransitionsChartButton.setFocusPainted(false);
        saveTransitionsChartButton.setIcon(Tools.getResIcon16("/icons/stateSim/saveImage.png"));
        saveTransitionsChartButton.setToolTipText("Saves the chart as image file.");
        saveTransitionsChartButton.addActionListener(actionEvent -> saveChartImage("transitions", 1200, 1024));
        transChartOptionsPanel.add(saveTransitionsChartButton);

        JButton showTransButton = new HolmesRoundedButton("<html><center>Find place</center></html>"
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        showTransButton.setBounds(posXchart+480, posYchart+2, 110, 24);
        showTransButton.setMargin(new Insets(0, 0, 0, 0));
        showTransButton.setFocusPainted(false);
        showTransButton.setIcon(Tools.getResIcon16("/icons/stateSim/findNode.png"));
        showTransButton.setToolTipText("Find selected transition within the net.");
        showTransButton.addActionListener(actionEvent -> {
            int selected = transitionsCombo.getSelectedIndex();
            if(selected>0) {
                //ustalanie prawdziwego ID:
                int sel = action.getRealNodeID(transitionsCombo.getSelectedItem().toString());
                if(sel == -1) return; //komunikat błędu podany już z metody getRealTransID

                GUIManager.getDefaultGUIManager().getSearchWindow().fillComboBoxesData();
                GUIManager.getDefaultGUIManager().getSearchWindow().selectedManually(false, sel);
            }
        });
        transChartOptionsPanel.add(showTransButton);
        topPanel.add(transChartOptionsPanel, BorderLayout.CENTER);

        //************************************************************************************************************
        //************************************************************************************************************

        JPanel transChartGraphicPanel = new JPanel(null);
        transChartGraphicPanel.setBackground(Color.WHITE);
        transChartGraphicPanel.setBorder(BorderFactory.createTitledBorder("Chart graphic"));
        transChartGraphicPanel.setPreferredSize(new Dimension(200, 100));

        int posXGchart = 10;
        int posYGchart = 20;

        ButtonGroup groupWidth = new ButtonGroup();
        JRadioButton width1 = new JRadioButton("Thin");
        width1.setBackground(Color.WHITE);
        width1.setBounds(posXGchart, posYGchart, 70, 20);
        width1.setActionCommand("0");
        width1.addActionListener(actionEvent -> {
            if(((AbstractButton) actionEvent.getSource()).isSelected()) {
                chartDetails.t_StrokeWidth = 1.0f;
                updatePlacesGraphicChart("transitions");
            }
        });
        transChartGraphicPanel.add(width1);
        groupWidth.add(width1);
        groupWidth.setSelected(width1.getModel(), true);

        JRadioButton width2 = new JRadioButton("Normal");
        width2.setBackground(Color.WHITE);
        width2.setBounds(posXGchart, posYGchart+20, 70, 20);
        width2.setActionCommand("1");
        width2.addActionListener(actionEvent -> {
            if(((AbstractButton) actionEvent.getSource()).isSelected()) {
                chartDetails.t_StrokeWidth = 2.0f;
                updatePlacesGraphicChart("transitions");
            }
        });
        transChartGraphicPanel.add(width2);
        groupWidth.add(width2);

        JRadioButton width3 = new JRadioButton("Thick");
        width3.setBackground(Color.WHITE);
        width3.setBounds(posXGchart, posYGchart+40, 70, 20);
        width3.setActionCommand("2");
        width3.addActionListener(actionEvent -> {
            if(((AbstractButton) actionEvent.getSource()).isSelected()) {
                chartDetails.t_StrokeWidth = 3.0f;
                updatePlacesGraphicChart("transitions");
            }
        });
        transChartGraphicPanel.add(width3);
        groupWidth.add(width3);

        topPanel.add(transChartGraphicPanel, BorderLayout.EAST);

        //************************************************************************************************************
        //************************************************************************************************************

        transitionsJPanel = new JPanel(new BorderLayout());
        transitionsJPanel.setBorder(BorderFactory.createTitledBorder("Transitions chart"));
        transitionsJPanel.add(createTransChartPanel(), BorderLayout.CENTER);
        result.add(transitionsJPanel, BorderLayout.CENTER);

        return result;
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************     MIEJSCA      ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda ta tworzy wykres liniowy dla miejsc.
     * @return JPanel - panel z wykresem
     */
    private JPanel createPlacesChartPanel() {
        String chartTitle = "Places dynamics";
        String xAxisLabel = "Step";
        String yAxisLabel = "Tokens";
        boolean showLegend = true;
        boolean createTooltip = true;
        boolean createURL = false;

        placesSeriesDataSet = new XYSeriesCollection();
        placesChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, placesSeriesDataSet,
                PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

        return new ChartPanel(placesChart);
    }

    /**
     * Metoda dodaje nową linię do wykresu.
     * @param selPlaceID int - indeks miejsca
     * @param name String - nazwa miejsca
     */
    private void addNewPlaceSeries(int selPlaceID, String name) {
        if(placesChartType == 0) { //replace chart
            placesJPanel.removeAll();
            placesJPanel.add(createPlacesChartPanel(), BorderLayout.CENTER);
            placesJPanel.revalidate();
            placesJPanel.repaint();
            placesChartType = 1;
            placesSeriesDataSet.removeAllSeries(); // ??
        }

        @SuppressWarnings("unchecked")
        java.util.List<XYSeries> x = placesSeriesDataSet.getSeries();
        for(XYSeries xys : x) {
            if(xys.getKey().equals(name))
                return;
        }

        XYSeries series = new XYSeries(name);
        //ArrayList<Integer> test = new ArrayList<Integer>();

        if(isTimeSimulationXTPN) {
            int step = 0;
            int intervalIteration = 1;

            while(step < simDataBox.fullTokensHistory.size()) {

                if(simDataBox.avgTimeForStep.get(step) < intervalIteration * placesTimeInterval) {
                    double value = 0.0;
                    int getIndex = 0;
                    while(simDataBox.avgTimeForStep.get(step) < intervalIteration * placesTimeInterval) {
                        value += simDataBox.fullTokensHistory.get(step).get(selPlaceID);
                        step++;
                        getIndex = step;

                        if(step >= simDataBox.avgTimeForStep.size()) {
                            getIndex = simDataBox.avgTimeForStep.size()-1;
                            break;
                        }
                    }
                    series.add((Number)simDataBox.avgTimeForStep.get(getIndex), value);
                } else {
                    intervalIteration++;
                }

            }
        } else {
            int interval = placesStepsInterval;
            int maxStep =  simDataBox.fullTokensHistory.size()-interval-1;

            if(simDataBox.fullTokensHistory.size() > 10*interval) {
                for(int step=0; step<maxStep; step+=interval) {

                    double value = 0;
                    for(int j=0; j<interval; j++) {
                        value += simDataBox.fullTokensHistory.get(step+j).get(selPlaceID);
                    }
                    value = value / interval;

                    //int value = placesRawData.get(step).get(selPlaceID);
                    series.add(step, value);
                }
            } else {
                for(int step = 0; step<simDataBox.fullTokensHistory.size(); step++) {
                    double value = simDataBox.fullTokensHistory.get(step).get(selPlaceID);
                    series.add(step, value);
                }
            }
        }
        placesSeriesDataSet.addSeries(series);
    }

    /**
     * Metoda ta usuwa z wykresu dane o wskazanym miejscu.
     * @param name String - nazwa miejsca (unikalna)
     */
    private void removePlaceSeries(String name) {
        @SuppressWarnings("unchecked")
        java.util.List<XYSeries> x = placesSeriesDataSet.getSeries();
        for(XYSeries xys : x) {
            if(xys.getKey().equals(name)) {
                placesSeriesDataSet.removeSeries(xys);
                return;
            }
        }
    }

    /**
     * Metoda czyszcząca dane okna.
     */
    private void clearPlacesChart() {
        placesSeriesDataSet.removeAllSeries();
        placesInChart = new ArrayList<Integer>();
        placesInChartStr  = new ArrayList<String>();
        for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
            placesInChart.add(-1);
            placesInChartStr.add("");
        }
    }

    /**
     * Metoda ta pokazuje na wykresie średnią liczbę tokenów w miejscach
     */
    private void showAllPlacesData() {
        /*
        if(placesAvgData.size() == 0)
            return;

        placesChartType = 0;
        //int steps = placesRawData.size();
        double max = 0.0;
        for (Double placesAvgDatum : placesAvgData) {
            if (placesAvgDatum > max)
                max = placesAvgDatum;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int p=0; p<placesAvgData.size(); p++) {
            String tName = "p"+p;//+"_"+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().get(p).getName();
            double value = placesAvgData.get(p);

            //dataset.addValue(value, "Firing", tName);
            dataset.addValue(value, "Tokens", tName);

            if(value > 0) {
                dataset.addValue(max-value, "toMax", tName);
                dataset.addValue(0, "ZeroTokens", tName);
            } else {
                dataset.addValue(0, "toMax", tName);
                dataset.addValue(max, "ZeroTokens", tName);
            }
        }

        CategoryAxis xAxisPlaces = new CategoryAxis("Place ID");
        xAxisPlaces.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisPlaces.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisPlaces.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Average tokens");
        yAxisPlaces.setLabelFont(new Font("Helvetica", Font.BOLD, 24));
        CategoryItemRenderer rendererPlaces = new StackedBarRenderer();
        CategoryPlot plotPlaces = new CategoryPlot(dataset, xAxisPlaces, yAxisPlaces, rendererPlaces);

        //TODO
        rendererPlaces.setToolTipGenerator(new holmes.windows.ssim.HolmesSim.CustomToolTipPlacesGenerator(
                overlord.getWorkspace().getProject().getPlaces(), placesAvgData, max));

        placesChart = new JFreeChart("Places average tokens data", new Font("Helvetica", Font.BOLD, 24), plotPlaces, true);
        CategoryPlot plot = (CategoryPlot) placesChart.getPlot();

        StackedBarRenderer renderer = (StackedBarRenderer) plot.getRenderer();
        renderer.setBase(1);
        //CategoryItemRenderer renderer = plot.getRenderer();
        Paint p1 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, Color.red);
        renderer.setSeriesPaint(0, p1);
        Paint p2 = new GradientPaint(0.0f, 0.0f, Color.lightGray, 0.0f, 0.0f, Color.lightGray);
        renderer.setSeriesPaint(1, p2);
        Paint p3 = new GradientPaint(0.0f, 0.0f, Color.gray, 0.0f, 0.0f, Color.gray);
        renderer.setSeriesPaint(2, p3);
        plot.setRenderer(renderer);

        Font font3 = new Font("Dialog", Font.PLAIN, 10);
        plot.getDomainAxis().setLabelFont(font3);
        plot.getRangeAxis().setLabelFont(font3);

        LegendTitle legend = placesChart.getLegend();
        Font labelFont = new Font("Arial", Font.BOLD, 12);
        legend.setItemFont(labelFont);

        BarRenderer br = (BarRenderer) plot.getRenderer();
        br.setMaximumBarWidth(20);

        placesJPanel.removeAll();
        ChartPanel placesChartPanel = new ChartPanel(placesChart);
        int places = placesAvgData.size();

        if(placesAvgData.size() > 99) {
            placesChartPanel.setPreferredSize(new Dimension(places*50, 270));
            placesChartPanel.setMaximumDrawWidth(places*30);
        } else {
            placesChartPanel.setPreferredSize(new Dimension(places*40, 270));
            placesChartPanel.setMaximumDrawWidth(places*30);
        }

        JScrollPane sPane = new JScrollPane(placesChartPanel);
        placesJPanel.add(sPane, BorderLayout.CENTER);
        placesJPanel.revalidate();
        placesJPanel.repaint();
         */
    }









    public static double calculateSD(Double numArray[]) {
        double sum = 0.0;
        double standardDeviation = 0.0;
        int length = numArray.length;

        for (double num : numArray) {
            sum += num;
        }
        double mean = sum / length;
        for (double num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }


    /**
     * Pokazuje wyniki dla wszystkich miejsc w notatniku.
     */
    private void showPlacesAllInNotepad() {
/*
        //mass action kinetics tests

        IRandomGenerator generator = ssim.getRandomGenerator();
        HolmesNotepad notePad = new HolmesNotepad(900,600);
        notePad.setVisible(true);

        int reps = 1000;
        notePad.addTextLineNL("Denominator = 1.0", "text");
        double probTime = 0.0;
        ArrayList<Double> values = new ArrayList<>();
        for(int i=0; i<reps; i++) {
            double val = -(Math.log(1 - generator.nextDouble())  );
            values.add(val);
            probTime += val;
        }
        probTime /= reps;
        Double[] array = new Double[values.size()];
        values.toArray(array); // fill the array
        double sd = calculateSD(array);
        notePad.addTextLineNL("Mean: "+Tools.cutValueExt(probTime,6), "text");
        notePad.addTextLineNL("Std. dev.:"+Tools.cutValueExt(sd,6), "text");

        notePad.addTextLineNL("********************************************", "text");
        for(int i=0; i<30; i++) {
            double val = -(Math.log(1 - generator.nextDouble())  );
            notePad.addTextLineNL("P: "+Tools.cutValueExt(val,6), "text");
        }

        notePad.addTextLineNL("********************************************", "text");
*/
        
        HolmesNotepad notePad = new HolmesNotepad(900,600);
        notePad.setVisible(true);

        ArrayList<Place> places_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();


    }


    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************    TRANZYCJE     ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda ta tworzy wykres liniowy dla tranzycji.
     * @return JPanel - panel z wykresem
     */
    private JPanel createTransChartPanel() {
        String chartTitle = "Transitions dynamics";
        String xAxisLabel = "Transition";
        String yAxisLabel = "Firing";
        boolean showLegend = true;
        boolean createTooltip = true;
        boolean createURL = false;

        transitionsSeriesDataSet = new XYSeriesCollection();
        transitionsChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, transitionsSeriesDataSet,
                PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

        return new ChartPanel(transitionsChart);
    }

    /**
     * Metoda dodaje nowy wykres dla tranzycji.
     * @param selTransID int - indeks tranzycji
     * @param name String - nazwa tranzycji
     */
    private void addNewTransitionSeries(int selTransID, String name) {
        if(transChartType == 0) { //replace chart
            transitionsJPanel.removeAll();
            transitionsJPanel.add(createTransChartPanel(), BorderLayout.CENTER);
            transitionsJPanel.revalidate();
            transitionsJPanel.repaint();
            transChartType = 1;
            transitionsSeriesDataSet.removeAllSeries(); // ??
        }

        java.util.List<XYSeries> x = transitionsSeriesDataSet.getSeries();
        for(XYSeries xys : x) {
            if(xys.getKey().equals(name))
                return;
        }

        XYSeries series = new XYSeries(name);


        for(int step=0; step<simDataBox.transitionsSimHistory.get(selTransID).size(); step++) {
            if(transInterval > (simDataBox.transitionsSimHistory.get(selTransID).size()/10)) {
                transInterval = simDataBox.transitionsSimHistory.get(selTransID).size()/10;

            }
            double value = 0; //suma odpaleń w przedziale czasu
            for(int i=0; i<transInterval; i++) {
                value += simDataBox.transitionsSimHistory.get(selTransID).get(step+i).fired;
            }
            series.add(step, value);
            step += transInterval;

            if(transInterval>1)
                step--;
        }
        transitionsSeriesDataSet.addSeries(series);
    }

    /**
     * Metoda ta usuwa z wykresu dane o wskazanym miejscu.
     * @param name String - nazwa miejsca (unikalna)
     */
    private void removeTransitionSeries(String name) {
        @SuppressWarnings("unchecked")
        List<XYSeries> x = transitionsSeriesDataSet.getSeries();
        for(XYSeries xys : x) {
            if(xys.getKey().equals(name)) {
                transitionsSeriesDataSet.removeSeries(xys);
                return;
            }
        }
    }

    /**
     * Metoda czyszcząca dane okna.
     */
    private void clearTransitionsChart() {
        transitionsSeriesDataSet.removeAllSeries();

        transInChart = new ArrayList<Integer>();
        transInChartStr  = new ArrayList<String>();
        for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size(); i++) {
            transInChart.add(-1);
            transInChartStr.add("");
        }

        //if(transitionsSeriesDataSet.getSeriesCount() > 0)
        //	transitionsSeriesDataSet.removeAllSeries();
    }

    /**
     * Metoda ta pokazuje na wykresie tranzycji wszystkie odpalenia tranzycji dla obliczonej
     * liczby kroków symulacji.
     */
    private void showAllTransData() {
        /*
        if(transitionsCompactData.size() == 0)
            return;

        transChartType = 0;

        double max = 0.0;
        for (Integer transitionsCompactDatum : transitionsCompactData) {
            if (transitionsCompactDatum > max)
                max = transitionsCompactDatum;
        }

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for(int t=0; t<transitionsCompactData.size(); t++) {
            String tName = "t"+t; //+"_"+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(t).getName();
            int value = transitionsCompactData.get(t);
            dataset.addValue(value, "Firing", tName);
            //dataset.addValue(max-value, "NotFiring", tName);

            if(value > 0) {
                dataset.addValue(max-value, "NotFiring", tName);
                dataset.addValue(0, "zero", tName);
            } else {
                dataset.addValue(0, "NotFiring", tName);
                dataset.addValue(max, "zero", tName);
            }
        }

        CategoryAxis xAxisTrans = new CategoryAxis("Transition ID");
        xAxisTrans.setLowerMargin(0.01d); // percentage of space before first bar
        xAxisTrans.setUpperMargin(0.01d); // percentage of space after last bar
        xAxisTrans.setCategoryMargin(0.05d); // percentage of space between categories
        ValueAxis yAxisPlaces = new NumberAxis("Total firing value");
        CategoryItemRenderer rendererTrans = new StackedBarRenderer();
        CategoryPlot plotTrans = new CategoryPlot(dataset, xAxisTrans, yAxisPlaces, rendererTrans);

        //TODO
        rendererTrans.setToolTipGenerator(new holmes.windows.ssim.HolmesSim.CustomToolTipTransGenerator(
                overlord.getWorkspace().getProject().getTransitions(), transitionsCompactData, max));

        transitionsChart = new JFreeChart("Transitions statistical data", new Font("Helvetica", Font.BOLD, 14), plotTrans, true);

        //transitionsChart = ChartFactory.createStackedBarChart(chartTitle, xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

        CategoryPlot plot = (CategoryPlot) transitionsChart.getPlot();
        CategoryItemRenderer renderer = plot.getRenderer();
        Paint p1 = new GradientPaint(0.0f, 0.0f, Color.red, 0.0f, 0.0f, Color.red);
        renderer.setSeriesPaint(0, p1);
        Paint p2 = new GradientPaint(0.0f, 0.0f, Color.lightGray, 0.0f, 0.0f, Color.lightGray);
        renderer.setSeriesPaint(1, p2);
        Paint p3 = new GradientPaint(0.0f, 0.0f, Color.gray, 0.0f, 0.0f, Color.gray);
        renderer.setSeriesPaint(2, p3);
        plot.setRenderer(renderer);

        Font font3 = new Font("Dialog", Font.PLAIN, 12);
        plot.getDomainAxis().setLabelFont(font3);
        plot.getRangeAxis().setLabelFont(font3);

        LegendTitle legend = transitionsChart.getLegend();
        Font labelFont = new Font("Arial", Font.BOLD, 12);
        legend.setItemFont(labelFont);

        transitionsJPanel.removeAll();
        ChartPanel transChartPanel = new ChartPanel(transitionsChart);
        int transitions = transitionsCompactData.size();

        if(transitionsCompactData.size() > 99) {
            transChartPanel.setPreferredSize(new Dimension(transitions*50, 270));
            transChartPanel.setMaximumDrawWidth(transitions*30);
        } else {
            transChartPanel.setPreferredSize(new Dimension(transitions*40, 270));
            transChartPanel.setMaximumDrawWidth(transitions*30);
        }

        JScrollPane sPane = new JScrollPane(transChartPanel);
        transitionsJPanel.add(sPane, BorderLayout.CENTER);
        transitionsJPanel.revalidate();
        transitionsJPanel.repaint();

         */
    }

    /**
     * Metoda pokazuje w notatniku dane odpaleń tranzycji w symulacji.
     */

    private void showTransAllInNotepad() {
        /*
        if(transitionsCompactData.size() == 0)
            return;

        double max = 0.0;
        long total = 0;
        for (Integer transitionsCompactDatum : transitionsCompactData) {
            total += transitionsCompactDatum;
            if (transitionsCompactDatum > max)
                max = transitionsCompactDatum;
        }


        HolmesNotepad notePad = new HolmesNotepad(900,600);
        notePad.setVisible(true);
        ArrayList<Transition> trans_tmp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        notePad.addTextLineNL("", "text");
        notePad.addTextLineNL("Transitions: (sum of all firing: "+total+")", "text");
        notePad.addTextLineNL("", "text");
        for(int t=0; t<transitionsCompactData.size(); t++) {
            String t_name = trans_tmp.get(t).getName();
            double val = transitionsCompactData.get(t);
            if(val > 0)
                notePad.addTextLineNL("  t_"+t+" "+t_name+" : Fired: "+val+" (max: "+max+")", "text");
            else {
                if(trans_tmp.get(t).isKnockedOut()) {
                    notePad.addTextLineNL("MANUALLY DISABLED: t_"+t+" "+t_name+" : "+val, "text");
                } else {
                    notePad.addTextLineNL("OFFLINE: t_"+t+" "+t_name+" : "+val, "text");
                }
            }
        }

         */
    }

    //**************************************************************************************
    //*********************************                  ***********************************
    //*********************************      OGÓLNE      ***********************************
    //*********************************                  ***********************************
    //**************************************************************************************

    /**
     * Metoda zapisująca aktualnie wyświetlany wykres do pliku.
     */
    @SuppressWarnings("SameParameterValue")
    private void saveChartImage(String chartType, int w, int h) {
        String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
        FileFilter[] filters = new FileFilter[1];
        filters[0] = new ExtensionFileFilter("Portable Network Graphics (.png)", new String[] { "PNG" });
        String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "", "");
        if(selectedFile.equals(""))
            return;

        if(!selectedFile.contains(".png"))
            selectedFile += ".png";

        File imageFile = new File(selectedFile);
        try {
            if(chartType.equals("places")) {
                ChartUtilities.saveChartAsPNG(imageFile, placesChart, w, h);
            } else if(chartType.equals("transitions")) {
                ChartUtilities.saveChartAsPNG(imageFile, transitionsChart, w, h);
            }
        } catch (IOException ex) {
            overlord.log("Error:"+ex, "error", true);
        }
    }

    /**
     * Metoda wypełnia komponenty rozwijalne danymi o miejscach i tranzycjach.
     */
    private void fillPlacesAndTransitionsData() {
        doNotUpdate = true;

        placeRepsSpinnerModel.setValue(overlord.simSettings.getRepetitions());  //reps
        qSimXTPNRecordStepsCheckbox.setSelected(overlord.simSettings.isPartialRecordingSteps());  //simSettings.setRecordSomeSteps
        selStepsSpinnerModel.setValue(overlord.simSettings.getRecordedSteps()); //simSettings.setRecordedSteps
        qSimXTPNCompStatsCheckbox.setSelected(overlord.simSettings.isStatsRecorded()); //simSettings.setRecordStatictis

        placeStepsSpinnerModel.setValue((int)overlord.simSettings.getSimSteps_XTPN()); //steps
        qSimXTPNStatsTimeCheckbox.setSelected(overlord.simSettings.isTimeSimulation_XTPN() );
        placeTimeSpinnerModel.setValue((int)overlord.simSettings.getSimTime_XTPN()); //simSettings.setSimMaxTime_XTPN

        doNotUpdate = false;

        if(simDataBox == null)
            simDataBox = new StateSimDataContainer();

        selStateLabel.setText(""+overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN);

        ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        if(places== null || places.isEmpty()) {
            placesCombo.removeAllItems();
            placesCombo.addItem("---");
            transitionsCombo.removeAllItems();
            transitionsCombo.addItem("---");

            return;
        }

        placesCombo.removeAllItems();
        placesCombo.addItem("---");

        if(simDataBox.avgTokens.size() == places.size()) {
            if(!sortedP) {
                for(int p=0; p < places.size(); p++) {
                    placesCombo.addItem("p"+(p)+"."+places.get(p).getName() + " "+formatD(simDataBox.avgTokens.get(p)));
                }
            } else {
                //sortowanie po tokenach:
                Map<Integer, Double> map = new HashMap<Integer, Double>();
                for(int j=0; j<simDataBox.avgTokens.size(); j++) {
                    map.put(j, simDataBox.avgTokens.get(j));
                }
                //sortuj po value (frequency)
                Map<Integer, Double> sortedByValues;
                sortedByValues = crunchifySortMapXTPN(map); // dark magic happens here
                for (Map.Entry<Integer, Double> entry : sortedByValues.entrySet()) {
                    placesCombo.addItem("p"+(entry.getKey())+"."+places.get(entry.getKey()).getName() + " "+formatD(entry.getValue()));
                }
            }
        } else {
            for(int p=0; p < places.size(); p++) {
                placesCombo.addItem("p"+(p)+"."+places.get(p).getName());
            }
        }

        ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        if(transitions== null || transitions.isEmpty())
            return;

        transitionsCombo.removeAllItems();
        transitionsCombo.addItem("---");

        if(simDataBox.avgFired.size() == transitions.size()) {
            if(!sortedT) {
                for(int t=0; t < transitions.size(); t++) {
                    transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName() + " "+formatD(simDataBox.avgFired.get(t)));
                }
            } else {
                //sortowanie po odpaleniach:
                Map<Integer, Double> map = new HashMap<Integer, Double>();
                for(int j = 0; j<simDataBox.avgFired.size(); j++) {
                    map.put(j, simDataBox.avgFired.get(j));
                }
                //sortuj po value (frequency)
                Map<Integer, Double> sortedByValues;
                sortedByValues = crunchifySortMapXTPN(map); // dark magic happens here
                for (Map.Entry<Integer, Double> entry : sortedByValues.entrySet()) {
                    transitionsCombo.addItem("t"+(entry.getKey())+"."+transitions.get(entry.getKey()).getName() + " "+formatD(entry.getValue()));
                }
            }
        } else {
            for(int t=0; t < transitions.size(); t++) {
                transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
            }
        }
    }

    /**
     * Metoda wywoływana w wątku, symuluje daną liczbę kroków sieci, a następnie
     * pobiera tablice i wektory danych z obiektu symulatora do struktur wewnętrznych
     * obiektu klasy HolmesStateSimulator - czyli podokna programu głównego.
     */
    private void acquireDataFromSimulation() {
        if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != GraphicalSimulator.SimulatorMode.STOPPED) {
            JOptionPane.showMessageDialog(ego,
                    "Main simulator active. Please turn if off before starting state simulator process",
                    "Main simulator active", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulatorXTPN().getsimulatorStatusXTPN() != GraphicalSimulatorXTPN.SimulatorModeXTPN.STOPPED) {
            JOptionPane.showMessageDialog(ego,
                    "Main XTPN simulator active. Please turn if off before starting state simulator process",
                    "XTPN simulator active", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        if(places == null || places.isEmpty())
            return;

        clearTransitionsChart();
        clearPlacesChart();
        clearAllData();

        progressBar.setMaximum(overlord.simSettings.getSimSteps());

        boolean success = ssim.initiateSim(overlord.simSettings);
        if(!success)
            return;

        //zablokuj kilka elementów okna symulatora
        setSimWindowComponentsStatus(false);
        setWorkInProgress(true);

        ssim.setThreadDetails(4, this, overlord.simSettings, progressBar);
        Thread myThread = new Thread(ssim);
        myThread.start();
    }

    /**
     * Blokuje komponenty na czas symulacji.
     * @param value (<b>boolean</b>) true, jeżeli mają być aktywne.
     */
    private void setSimWindowComponentsStatus(boolean value) {
        acqDataButton.setEnabled(value);
        stateManagerButton.setEnabled(value);
        overlord.getFrame().setEnabled(value);
    }

    /**
     * Metoda wywoływana zdalnie, kiedy wątek symulacji głównej (Simple) zakończy obliczenia
     * @param result (<b>StateSimDataContainer</b>) obiekt danych symulacji.
     */
    public void completeSimulationProcedures_Mk1(StateSimDataContainer result) {
        simDataBox = result;

        isTimeSimulationXTPN = overlord.simSettings.isTimeSimulation_XTPN(); //czy symulacja była po krokach czy po czasie

        placesInChart = new ArrayList<Integer>();
        placesInChartStr  = new ArrayList<String>();
        for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size(); i++) {
            placesInChart.add(-1);
            placesInChartStr.add("");
        }

        transInChart = new ArrayList<Integer>();
        transInChartStr  = new ArrayList<String>();
        for(int i=0; i<GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size(); i++) {
            transInChart.add(-1);
            transInChartStr.add("");
        }

        placeSimSteps.setText("Place simulation steps: " + simDataBox.avgTimeForStep.size());
        placeSimTime.setText("Place simulation time:  " + Tools.cutValue( simDataBox.avgTimeForStep.get( simDataBox.avgTimeForStep.size() -  1)));
        placeSimCompTime.setText("Simulation computing time: " + Tools.getTime( simDataBox.compTime ) );

        fillPlacesAndTransitionsData();
        setSimWindowComponentsStatus(true);
        setWorkInProgress(false);
    }

    /**
     * Metoda czyści wszystkie sturktury danych.
     */
    private void clearAllData() {
        ssim = new StateSimulatorXTPN();
        simDataBox = null;

        placesInChart.clear();
        placesInChartStr.clear();

        transInChart.clear();
        transInChartStr.clear();
    }

    /**
     * Konwersja liczby double do określonej długości łańcucha znaków.
     * @param value double - liczba do konwersji
     * @return String - liczba sformatowana
     */
    private String formatD(double value) {
        DecimalFormat df = new DecimalFormat("#.#######");
        String txt = "(avg: ";
        txt += df.format(value);
        txt += ")";
        txt = txt.replace(",", ".");
        return txt;
    }

    /**
     * Metoda przycina nazwę węzła, obcinając informacje o średniej liczbie wystąpień
     * w symulacji stanów.
     * @param name String - nazwa z comboBox
     * @return String - przycięta nazwa
     */
    private String trimNodeName(String name) {
        int i = name.indexOf("(avg");
        if(i<0) {
            return name;
        } else {
            name = name.substring(0, i-1);
            return name;
        }
    }

    /**
     * Metoda uaktualniania stylu wyświetlania
     */
    private void updatePlacesGraphicChart(String chartType) {
        if(chartType.equals("places")) {
            if(placesChartType == 0)
                return;

            XYPlot plot = placesChart.getXYPlot();
            int count =  placesChart.getXYPlot().getSeriesCount();
            for(int i=0; i<count; i++) {
                plot.getRenderer().setSeriesStroke(i, new BasicStroke(chartDetails.p_StrokeWidth));
            }
        } else if(chartType.equals("transitions")){
            if(transChartType == 0)
                return;

            XYPlot plot = transitionsChart.getXYPlot();
            int count =  transitionsChart.getXYPlot().getSeriesCount();
            for(int i=0; i<count; i++) {
                plot.getRenderer().setSeriesStroke(i, new BasicStroke(chartDetails.t_StrokeWidth));
            }
        }

        //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        //renderer.setSeriesLinesVisible(0, true);
        //renderer.setSeriesShapesVisible(0, false);
        //renderer.setSeriesLinesVisible(1, false);
        //renderer.setSeriesShapesVisible(1, true);
        //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        //plot.setRenderer(renderer);
        //plot.setBackgroundPaint(Color.DARK_GRAY);
        //plot.setRenderer(renderer);
    }

    /**
     * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
     */
    private void initiateListeners() { //HAIL SITHIS
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                fillPlacesAndTransitionsData();
                //((HolmesSimKnock) knockoutTab).updateFreshKnockoutTab();
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if(isWorkInProgress()) {
                    JOptionPane.showMessageDialog(ego, "Simulator working. Window closing operation cancelled.",
                            "Simulator working",JOptionPane.INFORMATION_MESSAGE);
                    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
                } else {
                    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                    overlord.getFrame().setEnabled(true);
                }

            }
        });

    }

    /**
     * Ustawia status pracy w toku - kiedy symulator pracuje.
     * @param value boolean - true, jeśli trwa symulacja
     */
    public void setWorkInProgress(boolean value) {
        this.workInProgress = value;
    }

    /**
     * Zwraca status pracy w toku, jeśli true - działa symulator.
     * @return boolean -  true/false j.w.
     */
    public boolean isWorkInProgress() {
        return this.workInProgress;
    }

    /**
     * Umożliwia dostęp do obiektu odpowiedzialnego za zakładkę Knockout symulatora.
     * @return HolmesStateSimulatorKnockout - obiekt
     */
    //public HolmesSimKnock accessKnockoutTab() {
      //  return (HolmesSimKnock)knockoutTab;
    //}

    /**
     * Reset okna symulatora.
     */
    public void resetSimWindow() {
        doNotUpdate = true;
        clearTransitionsChart();
        clearPlacesChart();
        clearAllData();
        fillPlacesAndTransitionsData();

        transInterval = 100;
        placesStepsInterval = 100;
        transChartType = 0;
        placesChartType = 0;

        int mValue = overlord.simSettings.getSimSteps()/10;
        SpinnerModel intervSpinnerModel = new SpinnerNumberModel(100, 0, mValue, 10);
        transIntervalSpinner.setModel(intervSpinnerModel);

        //SpinnerModel intervSpinnerModel2 = new SpinnerNumberModel(100, 0, mValue, 10);
        //placesStepsIntervalSpinner.setModel(intervSpinnerModel2);

        placesStepsIntervalSpinner.setValue(placesStepsInterval);
        placesTimeIntervalSpinner.setValue(placesTimeInterval);

        doNotUpdate = false;
    }

    public void updateIntervalSpinner() {
        int mValue = overlord.simSettings.getSimSteps()/10;
        SpinnerModel intervSpinnerModel = new SpinnerNumberModel(100, 0, mValue, 10);
        transIntervalSpinner.setModel(intervSpinnerModel);

        SpinnerModel intervSpinnerModel2 = new SpinnerNumberModel(100, 0, mValue, 10);
        placesStepsIntervalSpinner.setModel(intervSpinnerModel2);
    }

    public JFrame getFrame() {
        return ego;
    }

    //*************************************************************************************************************
    //*************************************************************************************************************
    //*************************************************************************************************************
    //*************************************************************************************************************
    //*************************************************************************************************************

    private static class ChartPropertiesXTPN {
        public float p_StrokeWidth = 1.0f;
        public float t_StrokeWidth = 1.0f;

        public ChartPropertiesXTPN() {}
    }

    //*************************************************************************************************************
    //*************************************************************************************************************
    //*************************************************************************************************************
    //*************************************************************************************************************
    //*************************************************************************************************************

    public static class CustomToolTipTransGeneratorXTPN implements CategoryToolTipGenerator {
        ArrayList<Transition> transitions;
        ArrayList<Integer> dataVector;
        double max;
        DecimalFormat formatter;

        public CustomToolTipTransGeneratorXTPN(ArrayList<Transition> transitions, ArrayList<Integer> dataVector, double max) {
            this.transitions = transitions;
            this.dataVector = dataVector;
            this.max = max;
            formatter = new DecimalFormat("#.###");
        }

        @Override
        public String generateToolTip(CategoryDataset dataset, int bar, int nodeIndex)   {
            String text = "<html><font size=\"5\">";
            text += "t"+nodeIndex+"_"+transitions.get(nodeIndex).getName()+"<br>";
            text += "Fired: "+dataVector.get(nodeIndex)+"   (maximum in this simulation: "+max+")";
            text += "</font></html>";
            return text;
        }
    }

    public static class CustomToolTipPlacesGeneratorXTPN implements CategoryToolTipGenerator  {
        ArrayList<Place> places;
        ArrayList<Double> dataVector;
        double max;
        DecimalFormat formatter;

        public CustomToolTipPlacesGeneratorXTPN(ArrayList<Place> places, ArrayList<Double> dataVector, double max) {
            this.places = places;
            this.dataVector = dataVector;
            this.max = max;
            formatter = new DecimalFormat("#.###");
        }

        @Override
        public String generateToolTip(CategoryDataset dataset, int bar, int nodeIndex)   {
            String text = "<html><font size=\"5\">";
            text += "p"+nodeIndex+"_"+places.get(nodeIndex).getName()+"<br>";
            text += "Tokens: "+dataVector.get(nodeIndex)+"   (maximum in this simulation: "+max+")";
            text += "</font></html>";
            return text;
        }
    }
}
