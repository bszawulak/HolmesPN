package holmes.windows;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.InvariantsCalculator;
import holmes.analyse.SubnetCalculator;
import holmes.analyse.comparison.*;
import holmes.analyse.comparison.structures.BranchVertex;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.files.io.Snoopy.SnoopyReader;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.server.BranchesServerCalc;
import holmes.workspace.ExtensionFileFilter;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class HolmesComparisonModule extends JFrame {


    enum ComparisonTypes {Invariants, Graphlets, Orbits, Netdiv, Branches, Gred}
    PetriNet secondNet = null;
    ArrayList<SubnetCalculator.SubNet> seconNetList;
    JTextArea infoPaneInv = new JTextArea(30, 30);
    JTextArea infoPaneDec = new JTextArea(30, 30);
    JTextArea infoPaneDRGF = new JTextArea(30, 30);
    JTextArea infoPaneGDDA = new JTextArea(30, 30);
    JTextArea infoPaneNetdiv = new JTextArea(30, 30);
    JTextArea infoPaneBranch = new JTextArea(30, 60);


    JButton generateInvl;
    JButton generateDec;
    JButton generateDrgf;
    JButton generateGDDA;
    JButton saveDGDV1;
    JButton saveDGDV2;
    JButton saveDGDD;
    JButton generateNetdiv;
    JButton generateBranch;
    JTable dgddTable;
    JComboBox graphletSize;
    JComboBox orbitSize;
    JComboBox egoSize;
    JComboBox graphletNDSize;
    JComboBox branchingVariant;
    JPanel decoResult;
    boolean invariantMatchingTypr = false;
    boolean transitionMatchingTypr = false;
    Boolean firstQuestionDec = false;
    Boolean secondQuestionDec = false;
    Boolean thirdQuestionDec = false;
    ArrayList<JTable> listOfTablesDec = new ArrayList<>();
    private boolean indexQuestionB = false;
    SubnetComparator sc;
    JRadioButton hungarianButton;
    int coloringMode = 0;
    ArrayList<ArrayList<ArrayList<GreatCommonSubnet>>> listOfTableContent = new ArrayList<>();
    String resultFlag = "Hungarian";

    InvariantComparator invComp;
    JTable matchingTable;
    JTable branchTable;
    JPanel invPanel;
    JPanel verticesPanel;

    Object[][] dataDRGF;
    Object[][] dataDGDD;
    Object[][] dataBranch;
    JTable drgfTable;

    JButton matchVertices;
    int[][] DGDVFirst;
    int[][] DGDVSecond;

    private XYSeriesCollection grdfSeriesDataSet = null;
    private JFreeChart grdfChart;
    private JPanel grdfChartPanel = new JPanel();

    private final JPanel branchChartPanel = new JPanel();
    private JPanel branchTabs;
    JPanel listBranchView;
    JList<String> leftBranchList = new JList();
    JList<String> centerBranchList = new JList();
    JList<String> rightBranchList = new JList();
    ArrayList<branchingPairs> currentBranchingRelations = new ArrayList<>();

    boolean reGenerateBoolean = false;

    public HolmesComparisonModule() {
        setTitle("Comparison module");
        setSize(1000, 800);

        JTabbedPane tabbedPane = new JTabbedPane();
        JComponent panel1 = makeInCompPanel();
        tabbedPane.addTab("Invariant based comparison", null, panel1,"");

        JComponent panel2 = makeGraphletPanel();
        tabbedPane.addTab("Graphlets (GRDF) comparison", null, panel2,"");

        JComponent panel3 = makeGDDAPanel();
        tabbedPane.addTab("Graphlets (GDDA) comparison", null, panel3,"");

        JComponent panel4 = makeNetdivPanel();
        //tabbedPane.addTab("Graphlets (Netdiv) comparison", null, panel4,"");

        JComponent panel5 = makeDecoPanel();
        tabbedPane.addTab("Decomposition based comparison", null, panel5,"");

        JComponent panel6 = makeBranchPanel();
        tabbedPane.addTab("Branching based comparison", null, panel6,"");
        this.add(tabbedPane);
    }

    private JPanel makeInCompPanel() {
        invPanel = new JPanel();

        JPanel optionPanel = createInOptionPanel();
        invPanel.add(optionPanel, BorderLayout.NORTH);

        verticesPanel = createVerticesPanel();
        invPanel.add(verticesPanel, BorderLayout.WEST);

        JPanel textPanel = createInvTextArea();
        invPanel.add(textPanel, BorderLayout.CENTER);

        return invPanel;
    }

    /*
            INVARIANT COMPARISON
     */

    private JPanel createInvTextArea() {
        JPanel textPanel = new JPanel();
        //infoPaneInv.setLayout(new BorderLayout());

        JScrollPane jsp = new JScrollPane(infoPaneInv);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        textPanel.add(jsp);
        return textPanel;
    }

    private JPanel createVerticesPanel() {
        JPanel panel = new JPanel();
        matchingTable = new JTable();
        JScrollPane jpane = new JScrollPane(matchingTable);
        JPanel scroll = new JPanel();
        panel.add(jpane);
        scroll.add(new JScrollPane(panel));
        scroll.setEnabled(false);

        return scroll;
    }

    private JPanel createInOptionPanel() {
        JPanel panel = new JPanel();

        //Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1));
        JPanel mopanel = new JPanel(new GridLayout(1, 2));
        JButton chooser = new JButton("Choose second net");
        chooser.setVisible(true);
        chooser.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();

            javax.swing.filechooser.FileFilter[] filters = new FileFilter[2];
            filters[0] = new ExtensionFileFilter("Snoopy Petri Net file (.spped),(.pn)", new String[] { "SPPED","PN" });
            filters[1] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });

            jfc.addChoosableFileFilter(filters[0]);
            jfc.addChoosableFileFilter(filters[1]);
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);

            infoPaneInv.append("Choosen file: " + jfc.getSelectedFile().getName() + "\n");
            chooseSecondNet(jfc.getSelectedFile().getAbsolutePath());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                invComp = new InvariantComparator(GUIManager.getDefaultGUIManager().getWorkspace().getProject(), secondNet);
                generateInvl.setEnabled(true);
                matchVertices.setEnabled(true);
            }


        });
        mopanel.add(chooser);

        JButton chooserPnt = new JButton("Load invariants for second net(.inv)");
        chooserPnt.setVisible(true);
        chooserPnt.addActionListener(e -> {
            //FileFilter[] filters = new FileFilter[3];

            //filters[0] = new ExtensionFileFilter("All supported Snoopy files", new String[] { "SPPED", "SPTPT" });
            //filters[1] = new ExtensionFileFilter("Snoopy Petri Net (.spped)", new String[] { "SPPED" });
            //filters[2] = new ExtensionFileFilter("INA PNT format (.pnt)", new String[] { "PNT" });

            JFileChooser jfc = new JFileChooser();
            jfc.setFileFilter(new ExtensionFileFilter("INA INV format (.inv)", new String[]{"INV"}));
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                infoPaneInv.append("Choosen pnt file: " + jfc.getSelectedFile().getName() + "\n");
                IOprotocols io = new IOprotocols();
                secondNet.setT_InvMatrix(io.readT_invariantsOut(jfc.getSelectedFile().getAbsolutePath()), false);
            }
        });
        mopanel.add(chooserPnt);
        buttonPanel.add(mopanel);

        JPanel tmpanel = new JPanel(new GridLayout(1, 3));
        //tmpanel.setBorder(titleF);
        matchVertices = new JButton("Match transitions");
        matchVertices.setEnabled(false);
        matchVertices.addActionListener(e -> {
            HashMap<Node, Node> matching;
            if (transitionMatchingTypr) {
                infoPaneInv.append("Choosen: SED transition label matching\n\r");
                matching = invComp.matchVertices(1);// idealInvariantMatching();
            } else {
                infoPaneInv.append("Choosen: Precise transition label matching\n\r");
                matching = invComp.matchVertices(0);
            }
            infoPaneInv.append("First net transitions number: " + invComp.pn1.getTransitions().size() +
                    "\nSecond net transition number: " + invComp.pn2.getTransitions().size() +
                    "\nMatched transition number: " + matching.size());
            calcMatchingTable(matching);
        });
        tmpanel.add(matchVertices);

        JButton loadMatchVertices = new JButton("Load match");
        loadMatchVertices.setEnabled(true);
        loadMatchVertices.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                infoPaneInv.append("Choosen match file: " + jfc.getSelectedFile().getName() + "\n");
                IOprotocols io = new IOprotocols();
                HashMap<Node, Node> matching;
                matching = loadMatchVerticesFromFile(jfc.getSelectedFile().getAbsolutePath());
                calcMatchingTable(matching);
            }
        });
        tmpanel.add(loadMatchVertices);


        JButton saveMatchedVertices = new JButton("Save match");
        saveMatchedVertices.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            int returnVal = jfc.showSaveDialog(HolmesComparisonModule.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                IOprotocols io = new IOprotocols();


                boolean result = saveMatchVerticesFromFile(jfc.getSelectedFile().getAbsolutePath());
                //TODO WRITE CSV
                //HashMap<Node, Node> matching = new HashMap<>();
                //matching = loadMatchVerticesFromFile(jfc.getSelectedFile().getAbsolutePath());
                //calcMatchingTable(matching);
                if (result)
                    infoPaneInv.append("Match saved to file: " + jfc.getSelectedFile().getName() + "\n");
                else
                    infoPaneInv.append("Save operation failed");

            }
        });
        tmpanel.add(saveMatchedVertices);
        buttonPanel.add(tmpanel);

        JPanel impanel = new JPanel(new GridLayout(1, 2));

        generateInvl = new JButton("Compare nets");
        generateInvl.addActionListener(e -> compareInv());
        generateInvl.setEnabled(false);
        impanel.add(generateInvl);
        buttonPanel.add(impanel);
        panel.add(buttonPanel);

        JPanel questions = new JPanel(new GridLayout(1, 2));
        JPanel firstQuestion = new JPanel(new GridLayout(0, 1));

        TitledBorder titleQ1;
        titleQ1 = BorderFactory.createTitledBorder("Invariant");
        JRadioButton preButton = new JRadioButton("Precise matching");
        preButton.setActionCommand("");
        preButton.setSelected(true);
        preButton.addActionListener(e -> {
            if (preButton.isSelected()) {
                invariantMatchingTypr = true;
            }
        });
        firstQuestion.add(preButton);

        JRadioButton maxButton = new JRadioButton("Best matching");
        maxButton.setActionCommand("");
        maxButton.addActionListener(e -> {
            if (maxButton.isSelected()) {
                invariantMatchingTypr = false;
            }
        });
        firstQuestion.add(maxButton);
        firstQuestion.setBorder(titleQ1);
        questions.add(firstQuestion);

        ButtonGroup groupQ1 = new ButtonGroup();
        groupQ1.add(preButton);
        groupQ1.add(maxButton);

        JPanel secondQuestion = new JPanel(new GridLayout(0, 1));

        TitledBorder titleQ2;
        titleQ2 = BorderFactory.createTitledBorder("Transition");
        secondQuestion.setBorder(titleQ2);
        JRadioButton preTButton = new JRadioButton("Precise matching");
        preTButton.setActionCommand("");
        preTButton.setSelected(true);
        preTButton.addActionListener(e -> {
            if (preButton.isSelected()) {
                transitionMatchingTypr = false;
            }
        });
        secondQuestion.add(preTButton);

        JRadioButton maxTButton = new JRadioButton("Best matching");
        maxTButton.setActionCommand("");
        maxTButton.addActionListener(e -> {
            if (maxButton.isSelected()) {
                transitionMatchingTypr = true;
            }
        });
        secondQuestion.add(maxTButton);
        questions.add(secondQuestion);
        panel.add(questions);

        ButtonGroup groupQ2 = new ButtonGroup();
        groupQ2.add(preTButton);
        groupQ2.add(maxTButton);

        return panel;
    }

    private HashMap<Node, Node> loadMatchVerticesFromFile(String absolutePath) {
        HashMap<Node, Node> result = new HashMap<>();
        try {
            DataInputStream in = new DataInputStream(new FileInputStream(absolutePath));
            BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
            String readLine = buffer.readLine();

            while (readLine != null && readLine.length() > 0) {
                String[] line = readLine.split(",");

                if (!line[1].equals("-1")) {
                    Node n1 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(Integer.parseInt(line[0]));
                    Node n2 = secondNet.getTransitions().get(Integer.parseInt(line[1]));
                    result.put(n1, n2);
                }
                readLine = buffer.readLine();
            }
            buffer.close();
        } catch (Exception e) {
            System.out.println("Problem with loading matching : "+ e.getMessage());
        }
        return result;
    }

    private boolean saveMatchVerticesFromFile(String absolutePath) {
        try {
            HashMap<Node, Node> toSave = new HashMap<>();

            for (int i = 0; i < matchingTable.getRowCount(); i++) {
                final int finalI = i;
                String cell = (String)matchingTable.getModel().getValueAt(finalI, 1);
                Node firstNetNode = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().stream().filter(x -> x.getName().equals(matchingTable.getModel().getValueAt(finalI, 0))).findFirst().orElse(new Transition("Error"));
                Node secondNetNode = secondNet.getTransitions().stream().filter(x -> x.getName().equals(cell)).findFirst().orElse(new Transition("Error"));

                toSave.put(firstNetNode, secondNetNode);
            }

            File csvOutputFile = new File(absolutePath);
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                for (Map.Entry<Node, Node> entry : toSave.entrySet()) {
                    Node key = entry.getKey();
                    Node value = entry.getValue();

                    pw.write(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().indexOf(key) + "," + secondNet.getTransitions().indexOf(value) + "\n");
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void calcMatchingTable(HashMap<Node, Node> matching) {
        String[] colNames = new String[]{"First Net", "Second Net"};
        String[][] rowData = new String[GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size()][colNames.length];

        for (int i = 0; i < rowData.length; i++) {
            rowData[i][0] = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(i).getName();

            if (matching.get(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(i)) != null) {
                Node nodeFormSecond = matching.get(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(i));
                rowData[i][1] = nodeFormSecond.getName();
            } else {
                rowData[i][1] = "--";
            }
        }

        DefaultTableModel model = new DefaultTableModel(rowData, colNames);
        matchingTable.setModel(model);
        //verticesPanel = createVerticesPanel();
        invPanel.add(verticesPanel, BorderLayout.WEST);
        invPanel.updateUI();
        verticesPanel.updateUI();
        this.revalidate();
    }

    private void compareInv() {

        if (invComp.pn1.getT_InvMatrix() != null && !invComp.pn1.getT_InvMatrix().isEmpty() && invComp.pn2.getT_InvMatrix() != null && !invComp.pn2.getT_InvMatrix().isEmpty()) {
            infoPaneInv.append("Comparison result: \n\r");
            HashMap<Integer, Integer> maping;
            double matchingScore = 0;
            if (invariantMatchingTypr) {
                infoPaneInv.append("Choosen: Ideal invariant matching\n\r");
                infoPaneInv.append("Matched invariants\n\r");
                infoPaneInv.append("First net ID - Second net ID\n\r");
                maping = invComp.idealInvariantMatching();
                for (Map.Entry<Integer, Integer> ma : maping.entrySet()) {
                    infoPaneInv.append(ma.getKey() + " - " + ma.getValue() + "\n\r");
                }
                calcIdealScore(maping);
            } else {
                maping = invComp.bestInvariantMatching();
                infoPaneInv.append("Choosen: Best invariant matching\n\r");
                infoPaneInv.append("Matched invariants\n\r");
                infoPaneInv.append("First net ID - Second net ID\n\r");
                for (Map.Entry<Integer, Integer> ma : maping.entrySet()) {
                    infoPaneInv.append(ma.getKey() + " - " + ma.getValue() + "\n\r");
                }
                calcBestScore(maping);
            }
        } else {
            if (invComp.pn1.getT_InvMatrix() == null || invComp.pn1.getT_InvMatrix().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please generate Invariants for first net");
                infoPaneInv.append("First net lack generated t-invariants\n");
                GUIManager.getDefaultGUIManager().showInvariantsWindow();
            }
            if (invComp.pn2.getT_InvMatrix() == null || invComp.pn2.getT_InvMatrix().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please load Invariants for second net");
                infoPaneInv.append("Second net lack loaded t-invariants\n");

            }
        }

    }

    private void calcIdealScore(HashMap<Integer, Integer> maping) {
        double score = 0;

        //TODO check if nessesery

        score = (double) (2 * maping.values().size()) / (double) (GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().size() + secondNet.getT_InvMatrix().size());
        infoPaneInv.append("Ideal count score: " + score + "\n\r");
        score = 0;

        for (Map.Entry<Integer, Integer> entry : maping.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();

            ArrayList<Integer> firstInvariant = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().get(key);
            ArrayList<Integer> secondInvariant = secondNet.getT_InvMatrix().get(value);

            int commonPart = 0;
            int size1 = 0;
            for (int i = 0; i < firstInvariant.size(); i++) {
                Node matched = invComp.matchedVertices.get(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(i));
                int index = secondNet.getTransitions().indexOf(matched);
                if (firstInvariant.get(i) != 0) {
                    size1++;
                    if (Objects.equals(firstInvariant.get(index), secondInvariant.get(i))) {
                        commonPart++;
                    }
                }
            }

            int size2 = 0;
            for (Integer integer : secondInvariant) {
                if (integer != 0) {
                    size2++;
                }
            }

            score += (double) (2 * commonPart) / (double) (size1 + size2);

        }
        infoPaneInv.append("Ideal common score: " + score + "\n\r");
    }

    private void calcBestScore(HashMap<Integer, Integer> maping) {
        double score;

        //TODO Check if nessesery
        // miltipli by invariant size?

        score = (double) (2 * maping.values().size()) / (double) (GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().size() + secondNet.getT_InvMatrix().size());

        infoPaneInv.append("Best count score: " + score + "\n\r");

        score = 0;

        for (Map.Entry<Integer, Integer> entry : maping.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();

            ArrayList<Integer> firstInvariant = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().get(key);
            ArrayList<Integer> secondInvariant = secondNet.getT_InvMatrix().get(value);

            int commonPart = 0;
            int size1 = 0;
            for (int i = 0; i < firstInvariant.size(); i++) {
                Node matched = invComp.matchedVertices.get(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(i));
                int index = secondNet.getTransitions().indexOf(matched);
                if (firstInvariant.get(i) != 0) {
                    size1++;
                    if (i < secondInvariant.size()) {
                        if (index != -1 && index < firstInvariant.size() && Objects.equals(firstInvariant.get(index), secondInvariant.get(i))) {
                            commonPart++;
                        }
                    }
                }
            }

            int size2 = 0;
            for (Integer integer : secondInvariant) {
                if (integer != 0) {
                    size2++;
                }
            }

            score += (double) (2 * commonPart) / (double) (size1 + size2);
        }
        infoPaneInv.append("Best common score: " + score + "\n\r");
    }

    private void chooseSecondNet(String absolutePath) {
        if(absolutePath.endsWith(".pnt")) {
            IOprotocols io = new IOprotocols();
            secondNet = io.serverReadPNT(absolutePath, 99);
        }
        if(absolutePath.endsWith(".spped") || absolutePath.endsWith(".pn"))
        {
            SnoopyReader reader = new SnoopyReader(0, absolutePath);
            secondNet = new PetriNet(reader.getNodesList(), reader.getArcList());
        }
    }

    /*
        GRAPHLETS COMPARISON - GRDF
     */

    private JPanel makeGraphletPanel() {
        JPanel panel = new JPanel();

        JPanel optionPanel = createGRDFOptionPanel();
        panel.add(optionPanel, BorderLayout.NORTH);

        JPanel south = new JPanel();
        JPanel verticesPanel = createGRDFResultPanel();
        south.add(verticesPanel, BorderLayout.WEST);

        JPanel textPanel = createGRDFTextArea();
        south.add(textPanel, BorderLayout.EAST);
        panel.add(south);

        grdfChartPanel = createChartPanel();
        grdfChartPanel.setVisible(false);
        panel.add(grdfChartPanel, BorderLayout.SOUTH);
        return panel;
    }

    JPanel createChartPanel() {
        String chartTitle = "Graflet Relative Distribution Frequency (GRDF)";
        String xAxisLabel = "Grphlets";
        String yAxisLabel = "Count";

        boolean showLegend = true;
        boolean createTooltip = true;
        boolean createURL = false;

        grdfSeriesDataSet = new XYSeriesCollection();
        grdfChart = ChartFactory.createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, grdfSeriesDataSet,
                PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

        grdfChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 14));

        ChartPanel placesChartPanel = new ChartPanel(grdfChart);
        return placesChartPanel;
    }

    private JPanel createGRDFResultPanel() {
        JPanel panel = new JPanel();

        Object[] column = {"Graphlets", "First net", "Second net", "Difference"};
        dataDRGF = new Object[][]{{"Graphlet O", "-", "-", "-"}};

        drgfTable = new JTable(dataDRGF, column);
        JScrollPane jpane = new JScrollPane(drgfTable);
        JPanel scroll = new JPanel();
        panel.add(jpane);
        scroll.add(new JScrollPane(panel));
        scroll.setEnabled(false);

        return scroll;
    }

    private JPanel createGRDFTextArea() {
        JPanel textPanel = new JPanel();

        JScrollPane jsp = new JScrollPane(infoPaneDRGF);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        textPanel.add(jsp);
        return textPanel;
    }

    private JPanel createGRDFOptionPanel() {
        JPanel panel = new JPanel();
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2));

        JButton chooser = new JButton("Choose second net");
        chooser.setVisible(true);
        chooser.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();

            javax.swing.filechooser.FileFilter[] filters = new FileFilter[2];
            filters[0] = new ExtensionFileFilter("Snoopy Petri Net file (.spped), (.pn)", new String[] { "SPPED","PN" });
            filters[1] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });

            jfc.addChoosableFileFilter(filters[0]);
            jfc.addChoosableFileFilter(filters[1]);
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);
            infoPaneDRGF.append("Choosen file: " + jfc.getSelectedFile().getName() + "\n");
            chooseSecondNet(jfc.getSelectedFile().getAbsolutePath());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                generateDrgf.setEnabled(true);
            }

        });
        buttonPanel.add(chooser);

        graphletSize = new JComboBox();
        graphletSize.setModel(new DefaultComboBoxModel(new String[]{"Graphlets size", "2-node size", "3-node size",
                "4-node size", "5-node size"}));
        buttonPanel.add(graphletSize);

        generateDrgf = new JButton("Compare nets");
        generateDrgf.addActionListener(e -> compareGraphlets());
        generateDrgf.setEnabled(false);
        buttonPanel.add(generateDrgf);

        JButton analysis = new JButton("Single net graphlet analysis");
        analysis.addActionListener(e -> {

            GUIManager.getDefaultGUIManager().createGraphletsWindow();
            GUIManager.getDefaultGUIManager().showGraphletsWindow();

        });
        buttonPanel.add(analysis);

        JPanel radioButtonsPanel = new JPanel(new GridLayout(2, 1));
        JRadioButton maxButton = new JRadioButton("Single arcs interpetation");
        maxButton.setActionCommand("");
        maxButton.setSelected(true);
        maxButton.addActionListener(e -> {
            if (maxButton.isSelected()) {
                GraphletsCalculator.multipleArcCheck = false;
            }
        });
        radioButtonsPanel.add(maxButton);

        JRadioButton minButton = new JRadioButton("Multiple arc interpertation");
        minButton.setActionCommand("");
        minButton.addActionListener(e -> {
            if (minButton.isSelected()) {
                GraphletsCalculator.multipleArcCheck = true;
            }
        });
        radioButtonsPanel.add(minButton);


        ButtonGroup groupQ1 = new ButtonGroup();
        groupQ1.add(minButton);
        groupQ1.add(maxButton);

        panel.add(buttonPanel);
        panel.add(radioButtonsPanel);

        return panel;
    }

    private void compareGraphlets() {

        int chiisenGraohletSize = getChoosenGraohletSize(graphletSize.getSelectedIndex());

        long[] firstSingleDRGF = calcDRGF(GUIManager.getDefaultGUIManager().getWorkspace().getProject());
        long[] secondSingleDRGF = calcDRGF(secondNet);

        long firstSum = Arrays.stream(firstSingleDRGF).sum();
        long secondSum = Arrays.stream(secondSingleDRGF).sum();

        long[] distanceDRGF = new long[chiisenGraohletSize];
        double result = 0;


        for (int i = 0; i < chiisenGraohletSize; i++) {
            distanceDRGF[i] = Math.abs(firstSingleDRGF[i] - secondSingleDRGF[i]);
            //if(firstSingleDRGF[i]!=0 && secondSingleDRGF[i]!=0) {
            result += Math.abs(((double) firstSingleDRGF[i] / (double) firstSum) - ((double) secondSingleDRGF[i] / (double) secondSum));


        }
        infoPaneDRGF.append("DRGF : " + result + "\n");
        /*
        infoPaneDRGF.append("Distance getween graphlets\n");
        for(int i = 0 ; i < distanceDRGF.length ; i++)
        {
            infoPaneDRGF.append("Graphlet " + i +" - "+ distanceDRGF[i] + " \n");
        }
        */

        XYSeries series1 = new XYSeries("Number of graphlets of net 1");
        XYSeries series2 = new XYSeries("Number of graphlets of net 2");
        dataDRGF = new Object[chiisenGraohletSize + 1][4];
        String[] colNames = new String[4];
        colNames[0] = "Graphlets";
        colNames[1] = "First net";
        colNames[2] = "Second net";
        colNames[3] = "Distance";
        for (int i = 0; i < chiisenGraohletSize; i++) {
            dataDRGF[i][0] = "G " + i;
            dataDRGF[i][1] = firstSingleDRGF[i];
            dataDRGF[i][2] = secondSingleDRGF[i];
            dataDRGF[i][3] = distanceDRGF[i];
            series1.add(i, firstSingleDRGF[i]);
            series2.add(i, secondSingleDRGF[i]);
        }

        DefaultTableModel model = new DefaultTableModel(dataDRGF, colNames);

        drgfTable.setModel(model);
        grdfSeriesDataSet.removeAllSeries();
        grdfSeriesDataSet.addSeries(series1);
        grdfSeriesDataSet.addSeries(series2);
        grdfChartPanel.setVisible(true);

        CategoryPlot chartPlot = grdfChart.getCategoryPlot();
        ValueAxis yAxis = chartPlot.getRangeAxis();
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        CategoryAxis xAxis = chartPlot.getDomainAxis();
        this.setSize(950, 1500);
        this.pack();
    }

    private int getChoosenGraohletSize(int index) {

        return switch (graphletSize.getSelectedIndex()) {
            case 1 -> 2;
            case 2 -> 8;
            case 3 -> 31;
            case 4 -> 151;
            default -> 151;
        };
    }

    private long[] calcDRGF(PetriNet project) {
        GraphletsCalculator.cleanAll();
        GraphletsCalculator.generateGraphlets();
        GraphletsCalculator.getFoundServerGraphlets(project);
        long[] singleDRGF = new long[GraphletsCalculator.graphetsList.size()];

        for (int k = 0; k < GraphletsCalculator.graphetsList.size(); k++) {
            int finalI = k;
            long val = GraphletsCalculator.uniqGraphlets.stream().filter(x -> x.getGraphletID() == finalI).count();
            singleDRGF[k] = val;
        }
        return singleDRGF;
    }

    private JPanel makeGDDAPanel() {
        JPanel panel = new JPanel();

        JPanel optionPanel = createGDDAOptionPanel();
        panel.add(optionPanel, BorderLayout.NORTH);

        JPanel south = new JPanel();
        JPanel verticesPanel = createGDDAResultPanel();
        south.add(verticesPanel, BorderLayout.SOUTH);

        JPanel textPanel = createGDDATextArea();
        south.add(textPanel, BorderLayout.EAST);
        panel.add(south);

        return panel;
    }

    private JPanel createGDDATextArea() {
        JPanel panel = new JPanel();

        JScrollPane jsp = new JScrollPane(infoPaneGDDA);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        panel.add(jsp);
        return panel;
    }

    private JPanel createGDDAResultPanel() {
        JPanel panel = new JPanel();

        Object[] column = {"DGDD", "0", "1", "2", "...", "k"};
        Object[][] data = {{"Orbit O", 0, 0, 0, 0, 0}, {"Orbit 1", 0, 0, 0, 0, 0}, {"Orbit 2", 0, 0, 0, 0, 0}};

        dgddTable = new JTable(data, column);
        JScrollPane jpane = new JScrollPane(dgddTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        dgddTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JPanel scroll = new JPanel();
        panel.add(jpane);
        scroll.add(new JScrollPane(panel));
        scroll.setEnabled(false);

        return scroll;

    }

    private JPanel createGDDAOptionPanel() {
        JPanel panel = new JPanel();

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3));

        JButton chooser = new JButton("Choose second net");
        chooser.setVisible(true);
        chooser.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            javax.swing.filechooser.FileFilter[] filters = new FileFilter[2];
            filters[0] = new ExtensionFileFilter("Snoopy Petri Net file (.spped), (.pn)", new String[] { "SPPED","PN" });
            filters[1] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });

            jfc.addChoosableFileFilter(filters[0]);
            jfc.addChoosableFileFilter(filters[1]);
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);
            infoPaneGDDA.append("Choosen file: " + jfc.getSelectedFile().getName() + "\n");
            chooseSecondNet(jfc.getSelectedFile().getAbsolutePath());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                generateGDDA.setEnabled(true);
            }

        });
        buttonPanel.add(chooser);


        generateGDDA = new JButton("Compare nets");
        generateGDDA.addActionListener(e -> {
            compareGDDA();
            saveDGDV1.setEnabled(true);
            saveDGDV2.setEnabled(true);
            saveDGDD.setEnabled(true);
        });
        generateGDDA.setEnabled(false);
        buttonPanel.add(generateGDDA);


        orbitSize = new JComboBox();
        orbitSize.setModel(new DefaultComboBoxModel(new String[]{"Orbits", "18",
                "90", "592"}));
        buttonPanel.add(orbitSize);

        saveDGDV1 = new JButton("Save DGDV for first net");
        saveDGDV1.addActionListener(e -> saveDGDV(DGDVFirst));
        saveDGDV1.setEnabled(false);
        buttonPanel.add(saveDGDV1);

        saveDGDV2 = new JButton("Save DGDV for first net");
        saveDGDV2.addActionListener(e -> saveDGDV(DGDVSecond));
        saveDGDV2.setEnabled(false);
        buttonPanel.add(saveDGDV2);

        saveDGDD = new JButton("Save DGDV for first net");
        saveDGDD.addActionListener(e -> saveDGDV(toInt(dataDGDD)));
        saveDGDD.setEnabled(false);
        buttonPanel.add(saveDGDD);

        panel.add(buttonPanel);

        return panel;
    }

    private int[][] toInt(Object[][] dataDGDD) {
        int[][] result = new int[dataDGDD.length][dataDGDD[0].length];

        for (int i = 0; i < dataDGDD.length; i++)
            for (int j = 0; j < dataDGDD[i].length; j++)
                result[i][j] = (int) dataDGDD[i][j];

        return result;
    }

    private void saveDGDV(int[][] data) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //chooser.showSaveDialog(null);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            exportToCSV(data, chooser.getSelectedFile().getAbsolutePath());
        } else {
            infoPaneGDDA.append("No directiory choosen for DGDV\n");
        }

    }

    private void compareGDDA() {
        GraphletsCalculator.GraphletsCalculator();

        int[][] firstSingleDGDV = calcDGDD(GUIManager.getDefaultGUIManager().getWorkspace().getProject(), true);
        int[][] secondSingleDGDV = calcDGDD(secondNet, false);

        //long firstSum =  Arrays.stream(firstSingleDGDDA).sum();
        //long secondSum =  Arrays.stream(secondSingleDGDDA).sum();

        long[] distanceDGDD = new long[GraphletsCalculator.globalOrbitMap.size()];
        double result = 0;

        double DGDDA = calcDGDDA(firstSingleDGDV, secondSingleDGDV, getOrbitsNumber());

        /*
        for(int i = 0 ; i < GraphletsCalculator.globalOrbitMap.size() ; i++)
        {
            distanceDRGF[i] = Math.abs(firstSingleDRGF[i] - secondSingleDRGF[i]);
            //if(firstSingleDRGF[i]!=0 && secondSingleDRGF[i]!=0) {
            result += Math.abs(((double) firstSingleDRGF[i] / (double) firstSum) - ((double) secondSingleDRGF[i] / (double) secondSum));


        }
        */
        infoPaneGDDA.append("DGDDA : " + DGDDA + "\n");

        /*
        Object[][] dataDGDD = new Object[GraphletsCalculator.globalOrbitMap.size()+1][4];
        String[] colNames = new String[4];
        int = getK()
        colNames[0] = "Graphlets";
        colNames[1] = "First net";
        colNames[2] = "Second net";
        colNames[3] = "Distance";
        for(int i = 0 ; i < GraphletsCalculator.graphetsList.size(); i++) {
            dataDGDD[i][0] = "G "+i;
            dataDGDD[i][1] = firstSingleDRGF[i];
            dataDGDD[i][2] = secondSingleDRGF[i];
            dataDGDD[i][3] = distanceDRGF[i];
        }
        */

        if (firstSingleDGDV.length != 0) {
            String[] colNames = new String[firstSingleDGDV[0].length + 1];
            for (int i = 1; i < firstSingleDGDV[0].length + 1; i++) {
                colNames[i] = String.valueOf(i);
            }

            DefaultTableModel model = new DefaultTableModel(dataDGDD, colNames);
            dgddTable.setAutoResizeMode(5);
            dgddTable.setModel(model);
            dgddTable.setAutoResizeMode(5);
        }
    }

    private int getOrbitsNumber() {
        return switch (orbitSize.getSelectedIndex()) {
            case 1 -> 18;
            case 2 -> 90;
            case 3 -> 592;
            default -> 592;
        };
    }

    private int[][] calcDGDD(PetriNet pn, boolean firstNet) {
        int[][] result = new int[0][0];
        ArrayList<int[]> DGDV = new ArrayList<>();

        //TODO stetowanie orbitami
        if (firstNet)
            DGDVFirst = new int[pn.getNodes().size()][GraphletsCalculator.globalOrbitMap.size()];
        else
            DGDVSecond = new int[pn.getNodes().size()][GraphletsCalculator.globalOrbitMap.size()];

        for (Node startNode : pn.getNodes()) {
            int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
            DGDV.add(vectorOrbit);
            if (firstNet) {
                DGDVFirst[pn.getNodes().indexOf(startNode)] = vectorOrbit;
            } else {
                DGDVSecond[pn.getNodes().indexOf(startNode)] = vectorOrbit;
            }
        }


        int max = 0;
        for (int[] ints : DGDV) {
            int localMax = Arrays.stream(ints).max().getAsInt();
            if (localMax > max)
                max = localMax;
        }

        int orbitNumber = DGDV.get(0).length;

        int[][] d = new int[orbitNumber][max + 1];

        for (int[] ints : DGDV) {
            for (int m = 0; m < ints.length; m++) {
                if (ints[m] > 0) {
                    d[m][ints[m]]++;
                }
            }
        }


        return d;
    }

    public double calcDGDDA(int[][] nG, int[][] nH, int orbNumber) {

        /*
        int k2 = 0;
        for(int[] vector : path2)
        {
            if(vector.length>k2)
                k2=vector.length;
        }

        int[][] nH = new int[path2.size()][k2];
        for(int i = 0 ; i< path2.size() ; i++)
        {
            nH[i]=path2.get(i);
        }

        int k1 = 0;
        for(int[] vector : path1)
        {
            if(vector.length>k1)
                k1=vector.length;
        }

        int[][] nG = new int[path1.size()][k1];
        for(int i = 0 ; i< path1.size() ; i++)
        {
            nG[i]=path1.get(i);
        }
        */

        //if (nH == null || nH.length == 0) {
        //    return -1;
        // }

        int maxk = Math.max(nG[0].length, nH[0].length);

        double[] d = new double[orbNumber];

        dataDGDD = new Object[orbNumber][maxk];

        for (int orb = 0; orb < orbNumber; orb++) {
            double di = 0;

            for (int k = 0; k < maxk; k++) {
                if (k >= nG[orb].length) {
                    dataDGDD[orb][k] = Math.pow(0 - nH[orb][k], 2);
                    di += (double) dataDGDD[orb][k];
                } else if (k >= nH[orb].length) {
                    dataDGDD[orb][k] = Math.pow(nG[orb][k] - 0, 2);
                    di += (double) dataDGDD[orb][k];
                } else {
                    dataDGDD[orb][k] = Math.pow(nG[orb][k] - nH[orb][k], 2);
                    di += (double) dataDGDD[orb][k];
                }
            }

            d[orb] = 1 - (1 / Math.sqrt(2)) * Math.sqrt(di);
        }

        return DoubleStream.of(d).sum() / orbNumber;
    }

    private JPanel makeNetdivPanel() {
        JPanel panel = new JPanel();

        JPanel optionPanel = createNetdivOptionPanel();
        panel.add(optionPanel, BorderLayout.NORTH);

        JPanel south = new JPanel();
        JPanel verticesPanel = createNetdivResultPanel();
        south.add(verticesPanel, BorderLayout.WEST);

        JPanel textPanel = createNetdivTextArea();
        south.add(textPanel, BorderLayout.EAST);
        panel.add(south);

        return panel;
    }

    private JPanel createNetdivOptionPanel() {
        JPanel panel = new JPanel();


        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));

        JButton chooser = new JButton("Choose second net");
        chooser.setVisible(true);
        chooser.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);
            jfc.setFileFilter(new ExtensionFileFilter("INA PNT format (.pnt)", new String[]{"PNT"}));
            infoPaneNetdiv.append("Choosen file: " + jfc.getSelectedFile().getName() + "\n");
            chooseSecondNet(jfc.getSelectedFile().getAbsolutePath());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                generateNetdiv.setEnabled(true);
            }

        });
        buttonPanel.add(chooser);

        generateNetdiv = new JButton("Generate");
        generateNetdiv.setVisible(true);
        generateNetdiv.addActionListener(e -> {
            GraphletComparator gc = new GraphletComparator(592);
            infoPaneNetdiv.append(gc.compareNetdiv(getNDKsize(), getRadius(), GUIManager.getDefaultGUIManager().getWorkspace().getProject(), secondNet));
        });

        buttonPanel.add(generateNetdiv);

        graphletNDSize = new JComboBox();
        graphletNDSize.setModel(new DefaultComboBoxModel(new String[]{"Graphlets size", "3-node size",
                "4-node size", "5-node size"}));
        buttonPanel.add(graphletNDSize);

        egoSize = new JComboBox();
        egoSize.setModel(new DefaultComboBoxModel(new String[]{"Ego net size", "3-node radius",
                "4-node radius", "5-node radius", "6-node radius", "7-node radius", "8-node radius"}));
        buttonPanel.add(egoSize);

        panel.add(buttonPanel);
        return panel;
    }

    private int getNDKsize() {
        return switch (graphletNDSize.getSelectedIndex()) {
            case 1 -> 3;
            case 2 -> 4;
            case 3 -> 5;
            default -> 3;
        };
    }

    private int getRadius() {
        return switch (egoSize.getSelectedIndex()) {
            case 1 -> 3;
            case 2 -> 4;
            case 3 -> 5;
            case 4 -> 6;
            case 6 -> 7;
            case 7 -> 8;
            default -> 3;
        };
    }

    private JPanel createNetdivResultPanel() {

        return new JPanel();
    }

    private JPanel createNetdivTextArea() {
        JPanel panel = new JPanel();

        JScrollPane jsp = new JScrollPane(infoPaneNetdiv);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        panel.add(jsp);
        return panel;
    }

    private JPanel makeDecoPanel() {
        JPanel panel = new JPanel();

        JPanel csop = createStartOptionsPanel();
        panel.add(csop, BorderLayout.NORTH);

        decoResult = new JPanel();

        JScrollPane jsp = new JScrollPane(infoPaneDec);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        decoResult.add(jsp, BorderLayout.CENTER);

        panel.add(decoResult);

        return panel;
    }

    private JPanel createStartOptionsPanel() {
        JPanel jp = new JPanel();

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));

        JButton chooser = new JButton("Choose second net");
        chooser.setVisible(true);
        chooser.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();

            javax.swing.filechooser.FileFilter[] filters = new FileFilter[2];
            filters[0] = new ExtensionFileFilter("Snoopy Petri Net file (.spped), (.pn)", new String[] { "SPPED", "PN" });
            filters[1] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });

            jfc.addChoosableFileFilter(filters[0]);
            jfc.addChoosableFileFilter(filters[1]);
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);

            infoPaneDec.append("Choosen file: " + jfc.getSelectedFile().getName() + "\n");
            chooseSecondNet(jfc.getSelectedFile().getAbsolutePath());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                generateDec.setEnabled(true);
            }

        });
        buttonPanel.add(chooser);

        generateDec = new JButton("Compare nets");
        generateDec.addActionListener(e -> compare());
        generateDec.setEnabled(false);
        buttonPanel.add(generateDec);
        jp.add(buttonPanel);

        JPanel firstQuestion = new JPanel(new GridLayout(0, 1));


        JRadioButton maxButton = new JRadioButton("Max common path");
        maxButton.setActionCommand("");
        maxButton.setSelected(true);
        maxButton.addActionListener(e -> {
            if (maxButton.isSelected()) {
                firstQuestionDec = false;
            }
        });
        firstQuestion.add(maxButton);

        JRadioButton minButton = new JRadioButton("Min common path");
        minButton.setActionCommand("");
        minButton.addActionListener(e -> {
            if (minButton.isSelected()) {
                firstQuestionDec = true;
            }
        });
        firstQuestion.add(minButton);


        ButtonGroup groupQ1 = new ButtonGroup();
        groupQ1.add(minButton);
        groupQ1.add(maxButton);

        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Path size");
        firstQuestion.setBorder(titleF);

        jp.add(firstQuestion);

        if (GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix() == null || GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().isEmpty()) {
            infoPaneDec.append("Generate inwariants for net in workspace.\n");
        }
        if (SubnetCalculator.adtSubNets == null || SubnetCalculator.adtSubNets.isEmpty()) {
            infoPaneDec.append("Generate ADT subnets for net in workspace.\n");
        }


        JPanel secondQuestion = new JPanel(new GridLayout(0, 1));

        JRadioButton ttButton = new JRadioButton("Same type branches comparison");
        ttButton.setActionCommand("");
        ttButton.setSelected(true);
        ttButton.addActionListener(e -> {
            if (ttButton.isSelected()) {
                secondQuestionDec = false;
            }
        });
        secondQuestion.add(ttButton);

        JRadioButton tpButton = new JRadioButton("Mix type branches comparison");
        tpButton.setActionCommand("");
        tpButton.addActionListener(e -> {
            if (tpButton.isSelected()) {
                secondQuestionDec = true;
            }
        });
        secondQuestion.add(tpButton);

        ButtonGroup groupQ2 = new ButtonGroup();
        groupQ2.add(ttButton);
        groupQ2.add(tpButton);

        TitledBorder titleS;
        titleS = BorderFactory.createTitledBorder("Branch restriction");
        secondQuestion.setBorder(titleS);
        jp.add(secondQuestion);


        JPanel thirdQuestion = new JPanel(new GridLayout(0, 1));
        JRadioButton loButton = new JRadioButton("With loops");
        loButton.setActionCommand("");
        loButton.addActionListener(e -> {
            if (loButton.isSelected()) {
                thirdQuestionDec = false;
            }
        });
        loButton.setSelected(true);

        thirdQuestion.add(loButton);

        JRadioButton nloButton = new JRadioButton("Without loops");
        nloButton.setActionCommand("");
        nloButton.addActionListener(e -> {
            if (nloButton.isSelected()) {
                thirdQuestionDec = true;
            }
        });
        thirdQuestion.add(nloButton);

        ButtonGroup groupQ3 = new ButtonGroup();
        groupQ3.add(loButton);
        groupQ3.add(nloButton);

        TitledBorder titleT;
        titleT = BorderFactory.createTitledBorder("Loop restriction");
        thirdQuestion.setBorder(titleT);
        jp.add(thirdQuestion);

        //----
        JPanel indexQuestion = new JPanel(new GridLayout(0, 1));
        JRadioButton jacButton = new JRadioButton("Jackard index");
        jacButton.setActionCommand("");
        jacButton.addActionListener(e -> {
            if (jacButton.isSelected()) {
                indexQuestionB = false;
            }
        });
        jacButton.setSelected(true);

        indexQuestion.add(jacButton);

        JRadioButton sorButton = new JRadioButton("Sørensen index");
        sorButton.setActionCommand("");
        sorButton.addActionListener(e -> {
            if (sorButton.isSelected()) {
                indexQuestionB = true;
            }
        });
        indexQuestion.add(sorButton);

        ButtonGroup groupQ4 = new ButtonGroup();
        groupQ4.add(jacButton);
        groupQ4.add(sorButton);

        TitledBorder titleIn;
        titleIn = BorderFactory.createTitledBorder("Index");
        indexQuestion.setBorder(titleIn);
        jp.add(indexQuestion);

        return jp;
    }

    private void compare() {

        if (SubnetCalculator.adtSubNets == null || SubnetCalculator.adtSubNets.isEmpty()) {
            JOptionPane jpo = new JOptionPane("No ADT sets!");
            GUIManager.getDefaultGUIManager().showDecoWindow();
        } else {
            listOfTablesDec.clear();
            InvariantsCalculator ic = new InvariantsCalculator(secondNet);
            ic.generateInvariantsForTest(secondNet);

            infoPaneDec.append("Second net: Invariants generated.\n");

            secondNet.setT_InvMatrix(ic.getInvariants(true), false);
            seconNetList = SubnetCalculator.generateADTFromSecondNet(secondNet);

            infoPaneDec.append("Second net: ADT generated.\n");

            sc = new SubnetComparator(SubnetCalculator.adtSubNets, seconNetList);
            //rrayList<ArrayList<GreatCommonSubnet>> listofComparedSubnets = sc.compare();

            infoPaneDec.append("Start comparison...\n");
            JComponent result = createResultsPanel();//stofComparedSubnets);//createPartResultTable(listofComparedSubnets);// createResultsPanel(listofComparedSubnets);
            decoResult.add(result, BorderLayout.WEST);
            this.revalidate();
        }
    }


    private JPanel createResultsPanel() {//ArrayList<ArrayList<GreatCommonSubnet>> gscl) {
        JPanel jp = new JPanel();
        //

        JPanel leftPanel = new JPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setSize(200, 500);

        this.setSize(950, 800);

        sc.firstQuestion = firstQuestionDec;
        sc.secondQuestion = secondQuestionDec;
        sc.thirdQuestion = thirdQuestionDec;

        infoPaneDec.append("Compare first net to second net.\n");
        JComponent panelFF = createPartResultTable(sc.compareFirstSecond(), false);//,gscl.size(),gscl.get(0).size());
        tabbedPane.addTab("First net to second net", null, panelFF, "Does nothing");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        infoPaneDec.append("Compare second net to first net.\n");
        JComponent panelSS = createPartResultTable(sc.compareSecondFirst(), false);//,gscl.get(0).size(),gscl.size());
        tabbedPane.addTab("Second net to first net", null, panelSS, "Does twice as much nothing");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        infoPaneDec.append("Compare first net internally.\n");
        JComponent panelFS = createPartResultTable(sc.compareInternalFirst(), true);//,gscl.size(),gscl.size());
        tabbedPane.addTab("Internal similarity of First net", null, panelFS, "Still does nothing");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        infoPaneDec.append("Compare second net internally.\n");
        JComponent panelSF = createPartResultTable(sc.compareInternalSecond(), true);//,gscl.get(0).size(),gscl.get(0).size());
        tabbedPane.addTab("Internal similarity of Second net", null, panelSF, "Does nothing at all");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);


        infoPaneDec.append("Comparison finished.\n");

        leftPanel.add(tabbedPane);

        jp.add(leftPanel, BorderLayout.PAGE_END);
        // jp.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);

        //

        JPanel rightPanel = new JPanel(new GridLayout(0, 1));

        hungarianButton = new JRadioButton("Hungarian method");
        hungarianButton.setMnemonic(KeyEvent.VK_B);
        hungarianButton.addActionListener(e -> {
            if (hungarianButton.isSelected()) {
                coloringMode = 2;
                if (tabbedPane.getSelectedIndex() < 2) {
                    int[] hungarianCels = calcHungarianCels(listOfTableContent.get(tabbedPane.getSelectedIndex()));
                    colorAllHungarianCel(hungarianCels, listOfTableContent.get(tabbedPane.getSelectedIndex()));
                }
            }
        });
        hungarianButton.setActionCommand(resultFlag);
        //hungarianButton.setActionCommand(birdString);

        //JRadioButton maxButton = new JRadioButton("Max method");
        //maxButton.setMnemonic(KeyEvent.VK_C);
        //maxButton.setActionCommand(resultFlag);
        //maxButton.setActionCommand(catString);

        JRadioButton singleHandButton = new JRadioButton("Color single matching");
        singleHandButton.setMnemonic(KeyEvent.VK_D);
        singleHandButton.setSelected(true);
        singleHandButton.addActionListener(e -> {
            if (singleHandButton.isSelected()) {
                coloringMode = 0;
            }
        });
        //dogButton.setActionCommand(dogString);

        JRadioButton handButton = new JRadioButton("Color choosen matching");
        handButton.setMnemonic(KeyEvent.VK_D);
        handButton.addActionListener(e -> {
            if (handButton.isSelected()) {
                coloringMode = 1;
            }
        });
        //dogButton.setActionCommand(dogString);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(singleHandButton);
        group.add(handButton);
        group.add(hungarianButton);

        rightPanel.add(singleHandButton);
        rightPanel.add(handButton);
        rightPanel.add(hungarianButton);

        TitledBorder title = BorderFactory.createTitledBorder("");
        //rightPanel.setBorder(title);
        //jp.add(rightPanel);

        JButton saveButton = new JButton("Save to .csv");
        saveButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(HolmesComparisonModule.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                exportToCSV(listOfTablesDec.get(0), fc.getSelectedFile().getPath() + "1-2.csv");
                exportToCSV(listOfTablesDec.get(1), fc.getSelectedFile().getPath() + "2-1.csv");
                exportToCSV(listOfTablesDec.get(2), fc.getSelectedFile().getPath() + "1-1.csv");
                exportToCSV(listOfTablesDec.get(3), fc.getSelectedFile().getPath() + "2-2.csv");
                infoPaneDec.append("csv files saved!\n");
            }
        });

        rightPanel.add(saveButton);

        //jp.add(rightPanel, BorderLayout.EAST);

        return jp;
    }

    public JComponent createPartResultTable(ArrayList<ArrayList<GreatCommonSubnet>> gcls, boolean isInternal) {//, int x_size, int y_size) {
        listOfTableContent.add(gcls);
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(800, 300));

        //ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList = gcls;
        String[] netLabelsFirst = new String[gcls.size()];
        String[] netLabelsSecond = new String[gcls.get(0).size()];

        //do poszerzenia dla 2 sieci
        for (int i = 0; i < gcls.size(); i++) {
            netLabelsFirst[i] = "SubF :" + i;
            System.out.println(netLabelsFirst[i]);
        }
        for (int i = 0; i < gcls.get(0).size(); i++) {
            netLabelsSecond[i] = "SubS :" + i;
            System.out.println(netLabelsSecond[i]);
        }

        ListModel lm = new AbstractListModel() {
            final String[] headers = netLabelsFirst;

            public int getSize() {
                return headers.length;
            }

            public Object getElementAt(int index) {
                return headers[index];
            }
        };

        String[][] results = new String[gcls.size()][gcls.get(0).size()];

        for (int i = 0; i < gcls.size(); i++) {
            for (int j = 0; j < gcls.get(i).size(); j++) {
                results[i][j] = gcls.get(i).get(j).gcsValue + "/" + gcls.get(i).get(j).firstNetNodeSize;
            }
        }

        int[] hungarianCels = calcHungarianCels(gcls);

        JTable comparisonTable = new JTable(results, netLabelsSecond) {//;
            //JTable table = new JTable(data, columnNames) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                Object value = getModel().getValueAt(row, col);
                //if (getSelectedRow() == row) {
                //colorIsomorphicCels(row, col, comp, subNetArrayList);
                //colorHungarianCels(row, col, comp, hungarianCels, gcls);
                if (isInternal) {
                    colorIsomorphicCels(row, col, comp, gcls);
                } else {
                    if (coloringMode == 0) {
                        //clean and single
                    }
                    if (coloringMode == 1) {
                        //multiple
                    }
                    if (coloringMode == 2) {
                        colorHungarianCels(row, col, comp, hungarianCels, gcls);
                    }
                }


                //pod przycisk
                //colorAllHungarianCel(row,col,comp,hungarianCels,gcls);

                return comp;
            }
        };
        comparisonTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JList rowHeader = new JList(lm);
        rowHeader.setFixedCellWidth(50);

        rowHeader.setFixedCellHeight(comparisonTable.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(comparisonTable));

        comparisonTable.getSelectionModel().addListSelectionListener(event -> {

        });

        comparisonTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                GUIManager.getDefaultGUIManager().reset.clearGraphColors();
                int row = comparisonTable.rowAtPoint(evt.getPoint());
                int col = comparisonTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0 && coloringMode == 0) {
                    colorHungarianCel(row, col, gcls);
                    TableCellEditor tce = comparisonTable.getCellEditor(row, col);
                    //tce.getTableCellEditorComponent(comparisonTable,)
                    //comparisonTable.getRowgetCellEditor(row,col);;
                }
            }
        });

        JScrollPane scroll = new JScrollPane(comparisonTable);
        scroll.setRowHeaderView(rowHeader);

        listOfTablesDec.add(comparisonTable);

        return scroll;
    }


    private void colorHungarianCels(int row, int col, Component comp, int[] hungarianCels, ArrayList<
            ArrayList<GreatCommonSubnet>> gcls) {
        if (row < hungarianCels.length)
            if (hungarianCels[row] == col) {
                comp.setBackground(Color.green);
                colorSubnet(gcls.get(row).get(col), SubnetCalculator.adtSubNets.get(row));
            } else {
                comp.setBackground(Color.white);
            }

    }

    private void colorHungarianCel(int row, int col, ArrayList<ArrayList<GreatCommonSubnet>> gcls) {
        colorSubnet(gcls.get(row).get(col), SubnetCalculator.adtSubNets.get(row));
    }

    private void colorAllHungarianCel(int[] hungarianCels, ArrayList<ArrayList<GreatCommonSubnet>> gcls) {

        for (int i = 0; i < gcls.size(); i++) {
            for (int j = 0; j < gcls.get(i).size(); j++) {
                if (i < hungarianCels.length)
                    if (hungarianCels[i] == j) {
                        //comp.setBackground(Color.green);
                        colorSubnetDensity(gcls.get(i).get(j), SubnetCalculator.adtSubNets.get(i), i, gcls.size());
                    } else {
                        //comp.setBackground(Color.white);
                    }
            }
        }
    }

    private void colorSubnetDensity(GreatCommonSubnet gcs, SubnetCalculator.SubNet sn, int fr, int max) {

        Color randomColor = new Color(0, (int) (255 * ((double) fr / (double) max)), 0);

        for (Node transition : sn.getSubNode()) {
            if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                ((Transition) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
            if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                ((Place) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
        }
        ArrayList<Arc> arcs = sn.getSubArcs();
        for (Arc arc : arcs) {
            arc.setColor(true, Color.red);
        }

        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Node transition : pse.partialNodes) {
                if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                    ((Transition) transition).setColorWithNumber(true, randomColor, false, 0, true, "");
                if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                    ((Place) transition).setColorWithNumber(true, randomColor, false, 0, true, "");
            }
            for (Arc arc : pse.partialArcs) {
                arc.setColor(true, randomColor);
            }
        }


        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
    }

    private int[] calcHungarianCels(ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList) {

        double[][] matrix = new double[subNetArrayList.size()][subNetArrayList.get(0).size()];

        for (int i = 0; i < subNetArrayList.size(); i++) {
            for (int j = 0; j < subNetArrayList.get(i).size(); j++) {
                matrix[i][j] = 1 - subNetArrayList.get(i).get(j).gcsValue;
            }
        }

        Hungarian h = new Hungarian(matrix);
        int[] result = h.execute();

        //TODO wybierz wspólne grafy o maxymalnych wartościach i zwróć je

        for (int i = 0; i < result.length; i++) {
            //ArrayList<Integer> row = subNetArrayList.get(i).get(result[i]).gcsValue;
            //create and add gcs


        }
        return result;
    }

    private void colorIsomorphicCels(int row, int col, Component
            comp, ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList) {
        if (subNetArrayList.get(row).get(col).gcsValue == subNetArrayList.get(row).get(col).firstNetNodeSize) {
            if (subNetArrayList.get(row).get(col).secondNetNodeSize == subNetArrayList.get(row).get(col).firstNetNodeSize)
                comp.setBackground(new Color(0, 153, 0));
            else
                comp.setBackground(Color.green);
        } else if (subNetArrayList.get(row).get(col).gcsValue == 0) {
            comp.setBackground(Color.red);
        } else if (subNetArrayList.get(row).get(col).gcsValue > subNetArrayList.get(row).get(col).firstNetNodeSize ||
                subNetArrayList.get(row).get(col).gcsValue > subNetArrayList.get(row).get(col).secondNetNodeSize) {
            comp.setBackground(Color.blue);
        } else {
            comp.setBackground(Color.white);
        }
    }


    static class RowHeaderRenderer extends JLabel implements ListCellRenderer {

        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            setText((value == null) ? "" : value.toString());
            return this;
        }

    }

    private void colorSubnet(GreatCommonSubnet gcs, SubnetCalculator.SubNet sn) {

        for (Node transition : sn.getSubNode()) {
            if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                ((Transition) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
            if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                ((Place) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
        }
        ArrayList<Arc> arcs = sn.getSubArcs();
        for (Arc arc : arcs) {
            arc.setColor(true, Color.red);
        }

        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Node transition : pse.partialNodes) {
                if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                    ((Transition) transition).setColorWithNumber(true, Color.GREEN, false, 0, true, "");
                if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                    ((Place) transition).setColorWithNumber(true, Color.GREEN, false, 0, true, "");
            }
            for (Arc arc : pse.partialArcs) {
                arc.setColor(true, Color.GREEN);
            }
        }


        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
    }


    public static boolean exportToCSV(JTable tableToExport,
                                      String pathToExportTo) {
        try {

            TableModel model = tableToExport.getModel();
            FileWriter csv = new FileWriter(pathToExportTo);

            for (int i = 0; i < model.getColumnCount(); i++) {
                csv.write(model.getColumnName(i) + ",");
            }

            csv.write("\n");

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    csv.write(model.getValueAt(i, j).toString() + ",");
                }
                csv.write("\n");
            }

            csv.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean exportToCSV(int[][] data,
                                      String pathToExportTo) {
        try {

            //TableModel model = tableToExport.getModel();
            FileWriter csv = new FileWriter(pathToExportTo);

            //for (int i = 0; i < model.getColumnCount(); i++) {
            //    csv.write(model.getColumnName(i) + ",");
            //}

            //csv.write("\n");

            for (int[] datum : data) {
                for (int i : datum) {
                    csv.write(i + ",");
                }
                csv.write("\n");
            }

            csv.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private JPanel makeBranchPanel() {
        JPanel panel = new JPanel();

        JPanel optionPanel = createBramchOptionPanel();
        panel.add(optionPanel, BorderLayout.NORTH);


        JPanel south = new JPanel();
        JPanel textPanel = createBranchTextArea();
        south.add(textPanel, BorderLayout.EAST);

        //panel.add(south);

        //branchChartPanel = createBranchChartPanel();
        //branchChartPanel.setVisible(false);
        //panel.add(branchChartPanel, BorderLayout.SOUTH);
        JPanel resultPanel = new JPanel();
        branchTabs = new JPanel();
        branchTabs.setVisible(true);
        //panel.add(branchTabs, BorderLayout.SOUTH);


        //resultPanel.add(branchTabs, BorderLayout.WEST);
        south.add(branchTabs, BorderLayout.WEST);
        panel.add(south);

        listBranchView = new JPanel();
        listBranchView.setVisible(true);
        //resultPanel.add(listBranchView, BorderLayout.EAST);
        panel.add(listBranchView, BorderLayout.SOUTH);
        //panel.add(south);

        return panel;
    }

    private JPanel createBramchOptionPanel() {
        JPanel panel = new JPanel();


        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));

        JButton chooser = new JButton("Choose second net");
        chooser.setVisible(true);
        chooser.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            javax.swing.filechooser.FileFilter[] filters = new FileFilter[2];
            filters[0] = new ExtensionFileFilter("Snoopy Petri Net file (.spped), (.pn)", new String[] { "SPPED", "PN" });
            filters[1] = new ExtensionFileFilter(".pnt - INA PNT file (.pnt)", new String[] { "PNT" });

            jfc.addChoosableFileFilter(filters[0]);
            jfc.addChoosableFileFilter(filters[1]);
            int returnVal = jfc.showOpenDialog(HolmesComparisonModule.this);

            infoPaneBranch.append("Choosen file: " + jfc.getSelectedFile().getName() + "\n");
            chooseSecondNet(jfc.getSelectedFile().getAbsolutePath());
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                generateBranch.setEnabled(true);
            }

        });
        buttonPanel.add(chooser);

        generateBranch = new JButton("Generate");
        generateBranch.setVisible(true);
        generateBranch.addActionListener(e -> {
            BranchesServerCalc bsc = new BranchesServerCalc();
            BranchesServerCalc.ParsedBranchData result = bsc.compare(GUIManager.getDefaultGUIManager().getWorkspace().getProject(), secondNet, 1);
            parsBranchingData(result);
            if(reGenerateBoolean) {
                infoPaneBranch.selectAll();
                infoPaneBranch.replaceSelection("");
            }
            reGenerateBoolean = true;
        });

        buttonPanel.add(generateBranch);

        branchingVariant = new JComboBox();
        branchingVariant.setModel(new DefaultComboBoxModel(new String[]{"Matching variant", "Type I","Type II", "Type III", "Type IV", "Type V"}));
        //buttonPanel.add(branchingVariant);

        JButton singleAnalysis = new JButton("Single net branching analysis");
        singleAnalysis.setVisible(true);
        singleAnalysis.addActionListener(e -> {
            GUIManager.getDefaultGUIManager().createBranchWindow();
            GUIManager.getDefaultGUIManager().showBranchWindow();
        });
        buttonPanel.add(singleAnalysis);
        panel.add(buttonPanel);

        return panel;
    }
/*
    private void parsBranchingData(BranchesServerCalc.ParsedBranchData result) {

        dataBranch = new Object[result.matched.size() + result.onlyFirstNet.size() + result.onlySecondNet.size() + 1][3];
        String[] colNames = new String[3];
        colNames[0] = "Branching vertex";
        colNames[1] = "First net";
        colNames[2] = "Second net";

        //matched
        int counter = 0;
        for (Map.Entry<BranchVertex, BranchVertex> entry : result.matched.entrySet()) {
            BranchVertex key = entry.getKey();
            BranchVertex value = entry.getValue();
            dataBranch[counter][0] = "BV" + counter;
            dataBranch[counter][1] = key.getBVName();
            dataBranch[counter][2] = value.getBVName();
            counter++;
        }

        //first net
        for (BranchVertex entry : result.onlyFirstNet) {
            dataBranch[counter][0] = "BV" + counter;
            dataBranch[counter][1] = entry.getBVName();
            dataBranch[counter][2] = "--";
            counter++;
        }

        //second net
        for (BranchVertex entry : result.onlySecondNet) {
            dataBranch[counter][0] = "BV" + counter;
            dataBranch[counter][1] = "--";
            dataBranch[counter][2] = entry.getBVName();
            counter++;
        }

        DefaultTableModel model = new DefaultTableModel(dataBranch, colNames);
        branchTable.setAutoResizeMode(5);
        branchTable.setModel(model);
        branchTable.setAutoResizeMode(5);

        // .//
        //rysowanie Z INNYCH DANYCGH

        JComponent tabRes = createBranchDiagramsPanel(result);

        calcDataForRelationTable(result);
        // branchTabs =
        //branchTabs.setVisible(true);
        branchTabs.add(tabRes, BorderLayout.WEST);

        //asdgfhjkl lista


        this.setSize(950, 1200);
    }
    */

    public void parsBranchingData(BranchesServerCalc.ParsedBranchData result) {
        JComponent tabRes = createBranchDiagramsPanel(result);
        infoPaneBranch.setColumns(25);
        infoPaneBranch.revalidate();
        infoPaneBranch.append("Calculation compleated :\n");
        infoPaneBranch.append("BrRDF value for all vertices : " + result.brrdf.vBrRDFvalue + "\n");
        infoPaneBranch.append("BrRDF value for transitions : " + result.brrdf.tBrRDFvalue + "\n");
        infoPaneBranch.append("BrRDF value for places : " + result.brrdf.pBrRDFvalue + "\n");
        infoPaneBranch.append("\n");
        infoPaneBranch.append("Partial BrRDF for each vertex: \n");
        for (int x = 0; x < result.brrdf.branchingVertices.size(); x++) {
            String name;
            if (result.brrdf.branchingVertices.get(x).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.TRANSITION))
                name = "T";
            else
                name = "P";

            String line = name + "<" + result.brrdf.branchingVertices.get(x).getNumberOfInTransitions() + ","
                    + result.brrdf.branchingVertices.get(x).getNumberOfOutTransitions() + ","
                    + result.brrdf.branchingVertices.get(x).getNumberOfInPlace() + ","
                    + result.brrdf.branchingVertices.get(x).getNumberOfOutPlace() + ">";
            infoPaneBranch.append(line + " : " + result.brrdf.vBrRDFtable[x] + "\n");
        }

        calcDataForRelationTable(result);
        branchTabs.removeAll();
        branchTabs.add(tabRes, BorderLayout.WEST);
        branchTabs.revalidate();

        //this.setSize(750, 900);
    }


    private void calcDataForRelationTable(BranchesServerCalc.ParsedBranchData result) {
        //First step
        ArrayList<branchingPairs> bp = new ArrayList<>();
        ArrayList<branchingRelation> br2 = new ArrayList<>();
        for (BranchVertex vertex : result.lbv2) {
            boolean isIso = false;
            for (branchingRelation br : br2) {
                isIso = br.compare(vertex);
            }
            if (!isIso) {
                br2.add(new branchingRelation(vertex));
                bp.add(new branchingPairs(null, br2.get(br2.size() - 1)));
            }
        }

        ArrayList<branchingRelation> br1 = new ArrayList<>();
        for (BranchVertex vertex : result.lbv1) {
            boolean isIso = false;
            for (branchingRelation br : br1) {
                isIso = br.compare(vertex);
            }
            if (!isIso) {
                br1.add(new branchingRelation(vertex));
                boolean noIso = true;
                for (branchingPairs element : bp) {
                    if (element.net2 != null && element.net2.isIso(vertex)) {
                        element.net1 = br1.get(br1.size() - 1);
                        noIso = false;
                    }
                }
                if (noIso) {
                    bp.add(new branchingPairs(br1.get(br1.size() - 1), null));
                }

            }
        }

        //Second Step
        for (branchingPairs basePair : bp) {
            for (branchingPairs internalPair : bp) {
                if (basePair.type.isIncludED(internalPair.type.root)) {
                    if (!basePair.smaller.contains(internalPair))
                        basePair.smaller.add(internalPair);
                    if (!internalPair.larger.contains(basePair))
                        internalPair.larger.add(basePair);
                }

                if (basePair.type.isIncludING(internalPair.type.root)) {
                    if (!basePair.larger.contains(internalPair))
                        basePair.larger.add(internalPair);
                    if (!internalPair.smaller.contains(basePair))
                        internalPair.smaller.add(basePair);
                }
            }
        }

        //listBranchView = createBranchingList(bp);

    }

    private JPanel createBranchingList(ArrayList<branchingPairs> bp) {
        currentBranchingRelations.clear();
        currentBranchingRelations = bp;
        JPanel jp = new JPanel();

        JPanel northPanel = new JPanel(new FlowLayout());
        JLabel name = new JLabel("Choose branch vertex");
        northPanel.add(name);
        String[] branchingString = calcBranchingString(bp);
        JComboBox brList = new JComboBox(branchingString);
        brList.setSelectedIndex(0);
        brList.addActionListener(e -> {
            //BranchesServerCalc bsc = new BranchesServerCalc();
            //BranchesServerCalc.ParsedBranchData result = bsc.compare(GUIManager.getDefaultGUIManager().getWorkspace().getProject(), secondNet, branchingVariant.getSelectedIndex());
            //parsBranchingData(result);
            //infoPaneBranch.append("");
            branchingPairs pair = currentBranchingRelations.get(brList.getSelectedIndex());
            ArrayList<branchingPairs> larger = pair.larger;
            ArrayList<branchingPairs> smaller = pair.smaller;


            DefaultListModel<String> centerNodel = createListInterior(brList, pair);
            centerBranchList.setModel(centerNodel);
            centerBranchList.setVisible(true);
            centerBranchList.updateUI();
            centerBranchList.setBounds(200, 200, 75, 75);

            //Larger
            DefaultListModel<String> leftModel = new DefaultListModel<>();
            for (branchingPairs element : pair.larger) {
                DefaultListModel<String> tmpModel = createListInterior(brList, element);
                addTo(tmpModel, leftModel);
            }
            leftBranchList.setModel(leftModel);
            leftBranchList.setVisible(true);
            leftBranchList.updateUI();
            leftBranchList.setBounds(200, 200, 75, 75);

            //Smaller
            DefaultListModel<String> rightModel = new DefaultListModel<>();
            for (branchingPairs element : pair.smaller) {
                DefaultListModel<String> tmpModel = createListInterior(brList, element);
                addTo(tmpModel, rightModel);
            }
            rightBranchList.setModel(rightModel);
            rightBranchList.setVisible(true);
            rightBranchList.updateUI();
            rightBranchList.setBounds(200, 200, 75, 75);
        });
        jp.add(brList);

        //DefaultListModel<String> l1 = new DefaultListModel<>();
        //l1.addElement("Item1");
        //l1.addElement("Item2");
        //l1.addElement("Item3");
        //l1.addElement("Item4");
        //JList<String> list = new JList<>(l1);
        //list.setBounds(100,100, 75,75);
        //jp.add(list);

        DefaultListModel centerNodel = new DefaultListModel();
        DefaultListModel leftModel = new DefaultListModel();
        DefaultListModel rightModel = new DefaultListModel();
        leftModel.addElement("TEST");
        centerNodel.addElement("TEST");
        rightModel.addElement("TEST");
        leftBranchList = new JList(leftModel);
        centerBranchList = new JList<>(centerNodel);
        rightBranchList = new JList(rightModel);
        jp.add(leftBranchList);
        jp.add(centerBranchList);
        jp.add(rightBranchList);

        listBranchView.add(jp, BorderLayout.SOUTH);

        return jp;
    }

    private DefaultListModel<String> createListInterior(JComboBox brList, branchingPairs pair) {
        DefaultListModel<String> centerNodel = new DefaultListModel<>();
        centerNodel.addElement(brList.getSelectedItem().toString());
        centerNodel.addElement("FIRST NET");
        for (int i = 0; i < pair.net1.list.size(); i++) {
            centerNodel.addElement("- " + pair.net1.list.get(i).getBVName());
        }
        centerNodel.addElement("SECOND NET");
        for (int i = 0; i < pair.net2.list.size(); i++) {
            centerNodel.addElement("- " + pair.net2.list.get(i).getBVName());
        }
        return centerNodel;
    }

    private String getBRanchName(BranchVertex root) {
        String result;
        String name;
        if (root.equals(PetriNetElement.PetriNetElementType.TRANSITION))
            name = "T";
        else
            name = "P";

        result = name + "<" + root.getNumberOfInTransitions() + "," + root.getNumberOfOutTransitions() + "," + root.getNumberOfInPlace() + "," + root.getNumberOfOutPlace() + ">";

        return result;
    }

    private String[] calcBranchingString(ArrayList<branchingPairs> bp) {
        String[] result = new String[bp.size()];
        for (int i = 0; i < bp.size(); i++) {
            String name;
            if (bp.get(i).type.root.getTypeOfBV().equals(PetriNetElement.PetriNetElementType.TRANSITION))
                name = "T";
            else
                name = "P";

            result[i] = name + "<" + bp.get(i).type.root.getNumberOfInTransitions() + "," + bp.get(i).type.root.getNumberOfOutTransitions() + "," + bp.get(i).type.root.getNumberOfInPlace() + "," + bp.get(i).type.root.getNumberOfOutPlace() + ">";
        }
        return result;
    }

    private Map<? extends BranchVertex, ? extends Integer> parsed(ArrayList<BranchVertex> onlyFIrst) {
        HashMap<BranchVertex, Integer> result = new HashMap<>();
        while (onlyFIrst.size() > 0) {
            BranchVertex bv1 = onlyFIrst.get(0);
            onlyFIrst.remove(bv1);
            int counter = 1;
            ArrayList<BranchVertex> toRemove = new ArrayList<>();
            for (BranchVertex bv2 : onlyFIrst) {
                if (sameType(bv1, bv2)) {
                    counter++;
                    toRemove.add(bv2);
                }
            }
            onlyFIrst.removeAll(toRemove);
            result.put(bv1, counter);
        }
        return result;
    }

    private int sameTypeInList(BranchVertex bv1, ArrayList<BranchVertex> list) {
        int position = -1;
        for (BranchVertex bv2 : list) {
            if (sameType(bv1, bv2)) {//&&!bv1.getBVName().equals(bv2.getBVName())){
                position = list.indexOf(bv2);
            }
        }

        //TODO
        // ENDPOINT TO NIE IN ARC
        return position;
    }

    private boolean sameType(BranchVertex bv1, BranchVertex bv2) {
        if (bv2.getBVName().equals("promoting_thinning_of_the_fibrous_cap") || bv1.getBVName().equals("promoting_thinning_of_the_fibrous_cap")) {
            System.out.println();
        }

        return bv1.getTypeOfBV().equals(bv2.getTypeOfBV()) &&
                //bv1.inEndpoints.size() == bv2.inEndpoints.size() &&
                //bv1.outEndpoints.size() == bv2.outEndpoints.size() &&
                bv1.getOutDegreeOfBV() == bv2.getOutDegreeOfBV() &&
                bv1.getInDegreeOfBV() == bv2.getInDegreeOfBV() &&
                bv1.getNumberOfInPlace() == bv2.getNumberOfInPlace() &&
                bv1.getNumberOfOutPlace() == bv2.getNumberOfOutPlace() &&
                bv1.getNumberOfInTransitions() == bv2.getNumberOfInTransitions() &&
                bv1.getNumberOfOutTransitions() == bv2.getNumberOfOutTransitions();
    }

    private JPanel createBranchResultPanel() {
        JPanel panel = new JPanel();

        Object[] column = {"Branches", "First net", "Second net"};
        dataBranch = new Object[][]{{"Branch O", "-", "-"}};

        branchTable = new JTable(dataBranch, column);
        JScrollPane jpane = new JScrollPane(branchTable);
        JPanel scroll = new JPanel();
        panel.add(jpane);
        scroll.add(new JScrollPane(panel));
        scroll.setEnabled(false);

        return scroll;
    }

    private JPanel createBranchTextArea() {
        JPanel panel = new JPanel();

        JScrollPane jsp = new JScrollPane(infoPaneBranch);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        panel.add(jsp);
        return panel;
    }
/*
    JPanel createBranchChartPanel() {
        String chartTitle = "Branching vertices measure";
        String xAxisLabel = "Branching vertices";
        String yAxisLabel = "Count";

        boolean showLegend = true;
        boolean createTooltip = true;
        boolean createURL = false;

        branchSeriesDataSet = new XYSeriesCollection();
        //branchChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet, PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
        branchChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet,
                PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
    //createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet,PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

        branchChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 14));

        ChartPanel placesChartPanel = new ChartPanel(branchChart);
        return placesChartPanel;
    }
    */

    private JPanel createBranchDiagramsPanel(BranchesServerCalc.ParsedBranchData result) {
        JPanel jp = new JPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setSize(200, 500);

        this.setSize(950, 800);

        ArrayList<BranchVertex> lbv;
        JComponent panelFF = createDiagram(result, 0);//,gscl.size(),gscl.get(0).size());
        tabbedPane.addTab("Branching vertices BrRDF", null, panelFF, "Diagram for branching vertices ");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JComponent panelFP = createDiagram(result, 1);
        tabbedPane.addTab("Branching transitions BrRDF", null, panelFP, "Diagram for branching transitions ");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        JComponent panelPP = createDiagram(result, 2);
        tabbedPane.addTab("Branching places BrRDF", null, panelPP, "Diagram for branching places ");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        jp.add(tabbedPane);

        return jp;
    }

    private JComponent createDiagram(BranchesServerCalc.ParsedBranchData result, int mod) {
        JPanel jp = new JPanel();

        ArrayList<BranchVertex> lista = new ArrayList<>();

        int position = 0;

        XYSeries series1 = new XYSeries("Branching vertices of first net");
        XYSeries series2 = new XYSeries("Branching vertices of second net");

        for (int fb1 = 0; fb1 < result.lbv1.size(); fb1++) {
            int pos = sameTypeInList(result.lbv1.get(fb1), lista);
            if (pos > -1) {
                if ((mod == 1 && result.lbv1.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.TRANSITION)) ||
                        mod == 2 && result.lbv1.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.PLACE) ||
                        mod == 0) {
                    series1.update((Number) (pos), series1.getY(pos).intValue() + 1);
                    System.out.println("Position: " + pos + " - " + result.lbv1.get(fb1).getBVName());
                }
            } else {
                if ((mod == 1 && result.lbv1.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.TRANSITION)) ||
                        mod == 2 && result.lbv1.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.PLACE) ||
                        mod == 0) {
                    series1.add(position, 1);
                    series2.add(position, 0);
                    lista.add(result.lbv1.get(fb1));
                    System.out.println("Position: " + position + " - " + result.lbv1.get(fb1).getBVName());
                    position++;
                }
            }
        }

        System.out.println("second");
        for (int fb1 = 0; fb1 < result.lbv2.size(); fb1++) {
            int pos = sameTypeInList(result.lbv2.get(fb1), lista);
            if (pos > -1) {
                if ((mod == 1 && result.lbv2.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.TRANSITION)) ||
                        mod == 2 && result.lbv2.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.PLACE) ||
                        mod == 0) {
                    series2.update((Number) (pos), series2.getY(pos).intValue() + 1);
                    //series2.getDataItem(pos).setY(series2.getDataItem(pos).getYValue()+1);
                    System.out.println("Position: " + pos + " - " + result.lbv2.get(fb1).getBVName());
                }
            } else {
                if ((mod == 1 && result.lbv2.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.TRANSITION)) ||
                        mod == 2 && result.lbv2.get(fb1).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.PLACE) ||
                        mod == 0) {
                    series1.add(position, 0);
                    series2.add(position, 1);
                    lista.add(result.lbv2.get(fb1));
                    System.out.println("Position: " + position + " - " + result.lbv2.get(fb1).getBVName());
                    position++;
                }
            }
        }

        //get name for axis X
        String[] axisX = new String[lista.size()];
        for (int x = 0; x < lista.size(); x++) {
            String name;
            if (lista.get(x).getTypeOfBV().equals(PetriNetElement.PetriNetElementType.TRANSITION))
                name = "T";
            else
                name = "P";

            axisX[x] = name + "<" + lista.get(x).getNumberOfInTransitions() + "," + lista.get(x).getNumberOfOutTransitions() + "," + lista.get(x).getNumberOfInPlace() + "," + lista.get(x).getNumberOfOutPlace() + ">";
        }

        if (mod == 0) {
            String chartTitle = "Branching vertices measure";
            String xAxisLabel = "Branching vertices";
            String yAxisLabel = "Count";

            boolean showLegend = true;
            boolean createTooltip = true;
            boolean createURL = false;

            XYSeriesCollection branchVertSeriesDataSet = new XYSeriesCollection();
            //branchChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet, PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
            JFreeChart branchVertChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchVertSeriesDataSet,
                    PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
            //createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet,PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

            branchVertChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 14));

            ChartPanel vertChartPanel = new ChartPanel(branchVertChart);

            branchVertSeriesDataSet.removeAllSeries();
            branchVertSeriesDataSet.addSeries(series1);
            branchVertSeriesDataSet.addSeries(series2);


            XYPlot xyplot = (XYPlot) branchVertChart.getPlot();
            xyplot.setForegroundAlpha(0.85F);
            XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
            xybarrenderer.setBarPainter(new StandardXYBarPainter());

            Paint[] paintArray = {              //code related to translucent colors begin here
                    new Color(0x80ff0000, true),
                    new Color(0x800000ff, true)
            };
            SymbolAxis rangeAxis = new SymbolAxis("Branching Vertices", axisX);
            rangeAxis.setVerticalTickLabels(true);
            xyplot.setDomainAxis(rangeAxis);

            NumberAxis yAxis = (NumberAxis) xyplot.getRangeAxis();
            DecimalFormat format = new DecimalFormat("0");
            yAxis.setNumberFormatOverride(format);
            yAxis.setTickUnit(new NumberTickUnit(1));

            xyplot.setDrawingSupplier(new DefaultDrawingSupplier(
                    paintArray,
                    DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
            //branchChartPanel = new ChartPanel(branchVertChart);
            branchChartPanel.setVisible(true);

            jp.add(vertChartPanel);
        }
        if (mod == 1) {

            String chartTitle = "Branching vertices measure";
            String xAxisLabel = "Branching vertices";
            String yAxisLabel = "Count";

            boolean showLegend = true;
            boolean createTooltip = true;
            boolean createURL = false;

            XYSeriesCollection branchSeriesTranDataSet = new XYSeriesCollection();
            //branchChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet, PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
            JFreeChart branchTranChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchSeriesTranDataSet,
                    PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
            //createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet,PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

            branchTranChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 14));

            ChartPanel vertChartPanel = new ChartPanel(branchTranChart);

            branchSeriesTranDataSet.removeAllSeries();
            branchSeriesTranDataSet.addSeries(series1);
            branchSeriesTranDataSet.addSeries(series2);


            XYPlot xyplot = (XYPlot) branchTranChart.getPlot();
            xyplot.setForegroundAlpha(0.85F);
            XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
            xybarrenderer.setBarPainter(new StandardXYBarPainter());

            Paint[] paintArray = {              //code related to translucent colors begin here
                    new Color(0x80ff0000, true),
                    new Color(0x800000ff, true)
            };
            SymbolAxis rangeAxis = new SymbolAxis("Branching Vertices", axisX);
            rangeAxis.setVerticalTickLabels(true);
            xyplot.setDomainAxis(rangeAxis);

            NumberAxis yAxis = (NumberAxis) xyplot.getRangeAxis();
            DecimalFormat format = new DecimalFormat("0");
            yAxis.setNumberFormatOverride(format);
            yAxis.setTickUnit(new NumberTickUnit(1));

            xyplot.setDrawingSupplier(new DefaultDrawingSupplier(
                    paintArray,
                    DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
            //branchChartPanel = new ChartPanel(branchVertChart);
            branchChartPanel.setVisible(true);

            jp.add(vertChartPanel);
        }
        if (mod == 2) {

            String chartTitle = "Branching vertices measure";
            String xAxisLabel = "Branching vertices";
            String yAxisLabel = "Count";

            boolean showLegend = true;
            boolean createTooltip = true;
            boolean createURL = false;

            XYSeriesCollection branchSeriesPlaDataSet = new XYSeriesCollection();
            //branchChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet, PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
            JFreeChart branchPlaChart = ChartFactory.createHistogram(chartTitle, xAxisLabel, yAxisLabel, branchSeriesPlaDataSet,
                    PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);
            //createXYLineChart(chartTitle, xAxisLabel, yAxisLabel, branchSeriesDataSet,PlotOrientation.VERTICAL, showLegend, createTooltip, createURL);

            branchPlaChart.getTitle().setFont(new Font("Dialog", Font.PLAIN, 14));

            ChartPanel vertChartPanel = new ChartPanel(branchPlaChart);


            branchSeriesPlaDataSet.removeAllSeries();
            branchSeriesPlaDataSet.addSeries(series1);
            branchSeriesPlaDataSet.addSeries(series2);


            XYPlot xyplot = (XYPlot) branchPlaChart.getPlot();
            xyplot.setForegroundAlpha(0.85F);
            XYBarRenderer xybarrenderer = (XYBarRenderer) xyplot.getRenderer();
            xybarrenderer.setBarPainter(new StandardXYBarPainter());

            Paint[] paintArray = {              //code related to translucent colors begin here
                    new Color(0x80ff0000, true),
                    new Color(0x800000ff, true)
            };
            SymbolAxis rangeAxis = new SymbolAxis("Branching Vertices", axisX);
            rangeAxis.setVerticalTickLabels(true);
            xyplot.setDomainAxis(rangeAxis);

            NumberAxis yAxis = (NumberAxis) xyplot.getRangeAxis();
            DecimalFormat format = new DecimalFormat("0");
            yAxis.setNumberFormatOverride(format);
            yAxis.setTickUnit(new NumberTickUnit(1));

            xyplot.setDrawingSupplier(new DefaultDrawingSupplier(
                    paintArray,
                    DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                    DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));
            //branchChartPanel = new ChartPanel(branchVertChart);
            branchChartPanel.setVisible(true);

            jp.add(vertChartPanel);

        }

        return jp;
    }

    private class branchingPairs {
        public branchingRelation net1;
        public branchingRelation net2;

        public branchingRelation type;
        public ArrayList<branchingPairs> larger = new ArrayList<>();
        public ArrayList<branchingPairs> smaller = new ArrayList<>();

        private branchingPairs(branchingRelation n1, branchingRelation n2) {
            if (n1 != null) {
                net1 = n1;
                type = net1;
            }
            if (n2 != null) {
                net2 = n2;
                type = net2;
            }
        }
    }

    private static class branchingRelation {
        BranchVertex root;
        ArrayList<BranchVertex> list = new ArrayList<>();

        private branchingRelation(BranchVertex newVertex) {
            root = newVertex;
            list.add(root);
        }

        private int getTi() {
            return (int) root.getNumberOfInTransitions();
        }

        private int getTo() {
            return (int) root.getNumberOfOutTransitions();
        }

        private int getPi() {
            return (int) root.getNumberOfInPlace();
        }

        private int getPo() {
            return (int) root.getNumberOfOutPlace();
        }

        public boolean compare(BranchVertex toCompare) {
            boolean result = false;
            if (isIso(toCompare)) {
                result = true;
                list.add(toCompare);
            }
            return result;
        }

        public boolean isIso(BranchVertex toCompare) {
            return getTi() == toCompare.getNumberOfInTransitions() && getTo() == toCompare.getNumberOfOutTransitions() &&
                    getPi() == toCompare.getNumberOfInPlace() && getPo() == toCompare.getNumberOfOutPlace();
        }

        public boolean isIncludED(BranchVertex toCompare) {
            return getTi() >= toCompare.getNumberOfInTransitions() && getTo() >= toCompare.getNumberOfOutTransitions() &&
                    getPi() >= toCompare.getNumberOfInPlace() && getPo() >= toCompare.getNumberOfOutPlace() && !(getTi() == toCompare.getNumberOfInTransitions() && getTo() == toCompare.getNumberOfOutTransitions() &&
                    getPi() == toCompare.getNumberOfInPlace() && getPo() == toCompare.getNumberOfOutPlace());
        }

        public boolean isIncludING(BranchVertex toCompare) {
            return getTi() <= toCompare.getNumberOfInTransitions() && getTo() <= toCompare.getNumberOfOutTransitions() &&
                    getPi() <= toCompare.getNumberOfInPlace() && getPo() <= toCompare.getNumberOfOutPlace() && !(getTi() == toCompare.getNumberOfInTransitions() && getTo() == toCompare.getNumberOfOutTransitions() &&
                    getPi() == toCompare.getNumberOfInPlace() && getPo() == toCompare.getNumberOfOutPlace());
        }
    }

    protected static <T> void addTo(ListModel<T> from, DefaultListModel<T> to) {
        for (int index = 0; index < from.getSize(); index++) {
            to.addElement(from.getElementAt(index));
        }
    }
}
