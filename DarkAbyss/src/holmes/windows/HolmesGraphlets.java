package holmes.windows;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.comparison.GraphletComparator;
import holmes.analyse.comparison.experiment.NetGenerator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.*;
import holmes.utilities.ColorPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.DoubleStream;

public class HolmesGraphlets extends JFrame {

    private GUIManager overlord;
    private JPanel mainPanel;
    JComboBox<String> orbit;
    private JTextArea comparisonResultsArea;
    private JTextArea graphletResultsArea;
    private JTextArea orbitResultsArea;

    private JComboBox graphletResult;

    private boolean firstNetLoaded = false;
    private boolean secondNetLoaded = false;

    JComboBox<String> graphletListSorted;
    JComboBox<String> graphletListUniq;
    JComboBox getSize;
    JComboBox getNode;
    JComboBox getSizeForComparison;

    String[] orbitData = {" ----- "};
    String[] graphDataUniq = {" ----- "};

    private String pathToFirstFile = "";
    private String pathToSecondFile = "";

    public HolmesGraphlets() {
        overlord = GUIManager.getDefaultGUIManager();
        JTabbedPane tabbedPane = new JTabbedPane();

        JComponent singeNetGraphlets = makeSingleNetPane();
        JComponent comparisonNetGraphlets = makeComparisonNetPane();
        tabbedPane.addTab("Single net ", null, singeNetGraphlets, "Graphlets in single net");
        tabbedPane.addTab("Net comparison ", null, comparisonNetGraphlets, "Graphlets in single net");
        this.add(tabbedPane, BorderLayout.CENTER);
        this.pack();
        this.setVisible(true);
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    }

    protected JComponent makeSingleNetPane() {
        JPanel panel = new JPanel(false);
        panel.setLayout(new GridLayout(3, 1));

        JPanel graphletPanel = new JPanel(false);
        graphletPanel.setLayout(new GridLayout(1, 3));

        JPanel graphletButtonPanel = new JPanel(false);
        graphletButtonPanel.setLayout(new GridLayout(2, 1));

        JButton checknetForGraphlets = new JButton("Check net for graphlets");
        checknetForGraphlets.addActionListener(actionEvent -> generateGraphletOrbits());

        //checknetForGraphlets.setHorizontalAlignment(JButton.CENTER);

        String[] sizeString = {"3-node graphlets", "4-node graphlets", "5-node graphlets"};
        getSize = new JComboBox(sizeString);
        getSize.setSelectedIndex(2);

        graphletButtonPanel.add(checknetForGraphlets);
        graphletButtonPanel.add(getSize);

        graphletResultsArea = new JTextArea(15, 20);
        JScrollPane gscroll = new JScrollPane(graphletResultsArea);
        gscroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        String[] resultString = {"None"};
        graphletResult = new JComboBox(sizeString);
        graphletResult.setSelectedIndex(0);
        graphletResult.setEnabled(false);
        graphletResult.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
                int selectedGraphlet = comboBox.getSelectedIndex();

                GraphletsCalculator.Struct graphletStructure = GraphletsCalculator.uniqGraphlets.get(selectedGraphlet);

                paintGraphlet(graphletStructure);
            }
        });


        graphletPanel.add(graphletButtonPanel);
        graphletPanel.add(gscroll);
        graphletPanel.add(graphletResult);
        panel.add(graphletPanel);
        //-----------------------------------


        JPanel orbitPanel = new JPanel(false);
        orbitPanel.setLayout(new GridLayout(1, 2));

        String[] nodeString = {"Choose node"};
        getNode = new JComboBox(nodeString);
        getNode.setSelectedIndex(0);
        getNode.addActionListener(actionEvent -> {
            @SuppressWarnings("unchecked")
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selectedGraphlet = comboBox.getSelectedIndex();

            if (!comboBox.getModel().getSelectedItem().equals("Choose node")) {
                Node n = overlord.getWorkspace().getProject().getNodes().get(comboBox.getSelectedIndex());
                int[] orbits = GraphletsCalculator.vectorOrbit(n, false);

                orbitResultsArea.setText("");
                for (int i = 0; i < orbits.length; i++) {
                    if (orbits[i] > 0) {
                        orbitResultsArea.append("Orbit-" + i + " : " + orbits[i] + "\n");
                    }
                }
            }
        });
        orbitPanel.add(getNode);

        orbitResultsArea = new JTextArea(15, 20);

        JScrollPane oscroll = new JScrollPane(orbitResultsArea);
        oscroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        orbitPanel.add(oscroll);
        panel.add(orbitPanel);

        JPanel saveButtons = new JPanel();

        JButton saveGDDA = new JButton("Save orbits");
        saveGDDA.addActionListener(actionEvent -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(HolmesGraphlets.this);
            saveGDDA(fc.getSelectedFile().getAbsolutePath());
        });

        saveButtons.setLayout(new GridLayout(1, 2));
        saveButtons.add(saveGDDA);
        panel.add(saveButtons);

        return panel;
    }

    private void saveGDDA(String path) {
        ArrayList<int[]> DGDV = new ArrayList<>();

        for (Node startNode : overlord.getWorkspace().getProject().getNodes()) {
            int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
            DGDV.add(vectorOrbit);
        }

        //DGDV
        //writeDGDV(tmpdir + "-DGDV.txt", DGDV);

        //DGDDA
        NetGenerator.writeDGDDA(path, DGDV);
    }

    protected JComponent makeComparisonNetPane() {
        JPanel panel = new JPanel(false);
        panel.setLayout(new GridLayout(2, 1));

        JPanel buttonPanel = new JPanel(false);
        JButton getFirstNet = new JButton("Get First Net Orbits");
        getFirstNet.setHorizontalAlignment(JButton.CENTER);
        getFirstNet.addActionListener(actionEvent -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(HolmesGraphlets.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                pathToFirstFile = file.getAbsolutePath();
                firstNetLoaded = true;
                comparisonResultsArea.append("First net : " + file.getAbsolutePath() + "\n");
            }
        });

        JButton getSecondNet = new JButton("Get Second Net orbits");
        getSecondNet.setHorizontalAlignment(JButton.CENTER);
        getSecondNet.addActionListener(actionEvent -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(HolmesGraphlets.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                pathToSecondFile = file.getAbsolutePath();
                secondNetLoaded = true;
                comparisonResultsArea.append("Second net  : " + file.getAbsolutePath() + "\n");
            }
        });

        String[] sizeString = {"3-node graphlets", "4-node graphlets", "5-node graphlets"};
        getSizeForComparison = new JComboBox(sizeString);
        getSizeForComparison.setSelectedIndex(2);

        JButton compare = new JButton("Compare");
        compare.setHorizontalAlignment(JButton.CENTER);
        compare.addActionListener(actionEvent -> {
            if (firstNetLoaded && secondNetLoaded)
                graphletCompare();
        });


        buttonPanel.setLayout(new GridLayout(1, 4));
        buttonPanel.add(getFirstNet);
        buttonPanel.add(getSecondNet);
        buttonPanel.add(getSizeForComparison);
        buttonPanel.add(compare);


        panel.add(buttonPanel, BorderLayout.NORTH);

        comparisonResultsArea = new JTextArea(15, 20);

        JScrollPane cscroll = new JScrollPane(comparisonResultsArea);
        cscroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(cscroll, BorderLayout.SOUTH);

        return panel;
    }

    private void generateGraphletOrbits() {

        switch (getSize.getSelectedIndex()) {
            case 0:
                GraphletsCalculator.generateGraphletsNode3();
                break;
            case 1:
                GraphletsCalculator.generateGraphletsNode4();
                break;
            case 2:
                GraphletsCalculator.generateGraphletsNode5();
                break;
        }

        //check all net for graphlets and orbits
        GraphletsCalculator.getFoundGraphlets();

        graphletResultsArea.setText("");
        for (int i = 0; i < GraphletsCalculator.graphetsList.size(); i++) {
            int finalI = i;
            long val = GraphletsCalculator.uniqGraphlets.stream().filter(x -> x.getGraphletID() == finalI).count();
            //if(val!=0) {
            graphletResultsArea.append("Graphlet-" + i + " : " + val + "\n");
            //}
        }

        String[] resultString = new String[GraphletsCalculator.uniqGraphlets.size()];
        for (int i = 0; i < GraphletsCalculator.uniqGraphlets.size(); i++) {
            GraphletsCalculator.Struct st = GraphletsCalculator.uniqGraphlets.get(i);
            resultString[i] = i + " : Graphlet ID " + st.getGraphletID();
        }

        //graphletResult = new JComboBox(resultString);
        graphletResult.setModel(new DefaultComboBoxModel(resultString));
        graphletResult.setEnabled(true);
        graphletResult.updateUI();

        //ORBITY
        String[] NoderesultString = new String[overlord.getWorkspace().getProject().getNodes().size()];
        for (int i = 0; i < overlord.getWorkspace().getProject().getNodes().size(); i++) {
            Node n = overlord.getWorkspace().getProject().getNodes().get(i);
            NoderesultString[i] = i + " : " + n.getType() + " - " + n.getName();
        }

        getNode.setModel(new DefaultComboBoxModel(NoderesultString));

        /*
        for(int j = 0 ; j < GraphletsCalculator.graphlets.get(0).size();j++) {
            for (int i = 0; i < GraphletsCalculator.graphlets.size(); i++) {
                System.out.print(GraphletsCalculator.graphlets.get(i).get(j).size() + ",");
            }
            System.out.println();
        }
        */
    }

    private void graphletCompare() {
        int orbNumber = 17;

        switch (getSizeForComparison.getSelectedIndex()) {
            case 0:
                orbNumber = 18;
                break;
            case 1:
                orbNumber = 90;
                break;
            case 2:
                orbNumber = 592;
                break;
            default:
                orbNumber = 592;
                break;
        }

        GraphletComparator gc = new GraphletComparator(orbNumber);

        double[] result = gc.calcDGDDApartitioned(pathToFirstFile, pathToSecondFile);

        comparisonResultsArea.append("Results:\n");
        comparisonResultsArea.append("GDDA : " + DoubleStream.of(result).sum() / orbNumber + "\n");
        comparisonResultsArea.append("\nPartial :\n");
        for (int i = 0; i < result.length; i++) {
            comparisonResultsArea.append("Orbit " + i + " : " + result[i] + "\n");

        }

    }

    private void paintGraphlet(GraphletsCalculator.Struct graphletStructure) {
        ColorPalette cp = new ColorPalette();
        overlord.getWorkspace().getProject().resetNetColors();

        for (Node element : graphletStructure.getNodeMap().values()) {

            if (element.getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                ((Place) element).setColorWithNumber(true, Color.red, false, 0, false, "");
            } else {
                ((Transition) element).setColorWithNumber(true, Color.blue, false, 0, true, "");
            }
        }


        overlord.getWorkspace().getProject().repaintAllGraphPanels();

        for (Arc element : graphletStructure.getArcMap().values()) {
            element.setColor(true, Color.green);
        }
    }
}