package holmes.windows.decompositions;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.SubnetCalculator;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.elements.*;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class HolmesBranchVerticesPrototype extends JFrame {
    private JPanel mainPanel;
    private JPanel logPanel;
    private JPanel inputPanel;
    private JPanel outputPanel;
    private GUIManager overlord;
    private JTextArea resultArea;

    JTextArea minallTA, maxallTA, minallTAIN, maxallTAIN, minallTAOUT, maxallTAOUT;
    public ArrayList<BranchStructure> bsl = new ArrayList<>();

    public HolmesBranchVerticesPrototype() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
        overlord = GUIManager.getDefaultGUIManager();
        setVisible(false);
        this.setTitle("Branching");

        setLayout(new BorderLayout());
        setSize(new Dimension(900, 600));
        setLocation(15, 15);

        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        GraphletsCalculator.GraphletsCalculator();
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        logPanel = createLogPanel(0, 0, 200, 150);
        inputPanel = createInputPanel(0, 130, 100, 150);
        outputPanel = createOutputPanel(0, 130, 700, 300);
        //panel.add(logPanel, BorderLayout.WEST);
        panel.add(inputPanel, BorderLayout.WEST);
        panel.add(outputPanel, BorderLayout.EAST);
        panel.repaint();
        return panel;
    }

    private JPanel createLogPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Numbers of degrees"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));
        GridBagConstraints c = new GridBagConstraints();

        JLabel nameDeg = new JLabel("Degree  :  ");
        nameDeg.setLayout(new GridLayout());
        nameDeg.setEnabled(true);
        c.gridx = 0;
        c.gridy = 0;
        panel.add(nameDeg, c);

        JLabel nameV = new JLabel(" Ver. ");
        nameV.setLayout(new GridLayout());
        nameV.setEnabled(true);
        c.gridx = 1;
        c.gridy = 0;
        panel.add(nameV, c);

        JLabel nameT = new JLabel(" Tran. ");
        nameT.setLayout(new GridLayout());
        nameT.setEnabled(true);
        c.gridx = 2;
        c.gridy = 0;
        panel.add(nameT, c);

        JLabel nameP = new JLabel(" Pla. ");
        nameP.setLayout(new GridLayout());
        nameP.setEnabled(true);
        c.gridx = 3;
        c.gridy = 0;
        panel.add(nameP, c);

        int row = 1;


        for (VertCounter group : countVerticesDegrees()) {
            JLabel groupDegree = new JLabel(String.valueOf(group.deg));
            groupDegree.setLayout(new GridLayout());
            groupDegree.setEnabled(true);
            c.gridx = 0;
            c.gridy = row;
            panel.add(groupDegree, c);

            JLabel groupValue = new JLabel(String.valueOf(group.t + group.p));
            groupValue.setLayout(new GridLayout());
            groupValue.setEnabled(true);
            c.gridx = 1;
            c.gridy = row;
            panel.add(groupValue, c);

            JLabel groupTValue = new JLabel(String.valueOf(group.t));
            groupTValue.setLayout(new GridLayout());
            groupTValue.setEnabled(true);
            c.gridx = 2;
            c.gridy = row;
            panel.add(groupTValue, c);

            JLabel groupPValue = new JLabel(String.valueOf(group.p));
            groupPValue.setLayout(new GridLayout());
            groupPValue.setEnabled(true);
            c.gridx = 3;
            c.gridy = row;
            panel.add(groupPValue, c);

            row++;
        }

        return panel;
    }


    private JPanel createOutputPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Data"));
        panel.setPreferredSize(new Dimension(width, height));

        resultArea = new JTextArea();
        resultArea.setText("");
        resultArea.setLayout(new BorderLayout());

        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createInputPanel(int x, int y, int width, int height) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayout(3, 1));
        mainPanel.setBorder(BorderFactory.createTitledBorder(""));
        //mainPanel.setPreferredSize(new Dimension(width, height));
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Restrictions"));

        int posX = 10;
        int posY = 10;

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1));

        JButton calc = new JButton("<html>Find Branching <br />Vertices</html>");
        calc.setLayout(new FlowLayout());
        calc.setEnabled(true);
        calc.setBounds(posX, posY, 70, 70);
        calc.setMargin(new Insets(0, 0, 0, 0));
        calc.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        calc.addActionListener(actionEvent -> caclBranches(0));
        calc.setFocusPainted(false);
        buttonPanel.add(calc);

        JButton calcT = new JButton("<html>Find Branching <br />Transitions</html>");
        calcT.setLayout(new FlowLayout());
        calcT.setEnabled(true);
        calcT.setBounds(posX, posY + 80, 70, 70);
        calcT.setMargin(new Insets(0, 0, 0, 0));
        calcT.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        calcT.addActionListener(actionEvent -> caclBranches(1));
        calcT.setFocusPainted(false);
        buttonPanel.add(calcT);

        JButton calcP = new JButton("<html>Find Branching <br />Places</html>");
        calcP.setLayout(new FlowLayout());
        calcP.setEnabled(true);
        calcP.setBounds(posX, posY + 160, 70, 70);
        calcP.setMargin(new Insets(0, 0, 0, 0));
        calcP.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        calcP.addActionListener(actionEvent -> caclBranches(2));
        calcP.setFocusPainted(false);
        buttonPanel.add(calcP);

        //----comparison

        JButton export = new JButton("<html>Export</html>");
        export.setLayout(new FlowLayout());
        export.setEnabled(true);
        export.setBounds(posX, posY + 80, 70, 70);
        export.setMargin(new Insets(0, 0, 0, 0));
        export.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        export.addActionListener(actionEvent -> exportClick());
        export.setFocusPainted(false);
        buttonPanel.add(export);

        JButton impor = new JButton("<html>Import</html>");
        impor.setLayout(new FlowLayout());
        impor.setEnabled(true);
        impor.setBounds(posX, posY + 80, 70, 70);
        impor.setMargin(new Insets(0, 0, 0, 0));
        impor.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        impor.addActionListener(actionEvent -> importClick());
        impor.setFocusPainted(false);
        //buttonPanel.add(impor);

        JButton comp = new JButton("<html>Compare</html>");
        comp.setLayout(new FlowLayout());
        comp.setEnabled(true);
        comp.setBounds(posX, posY + 80, 70, 70);
        comp.setMargin(new Insets(0, 0, 0, 0));
        comp.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        comp.addActionListener(actionEvent -> compareClick());
        comp.setFocusPainted(false);
        buttonPanel.add(comp);

        GridBagConstraints c = new GridBagConstraints();

        JLabel minall = new JLabel("min");
        minall.setLayout(new GridLayout());
        minall.setEnabled(true);
        c.gridx = 0;
        c.gridy = 0;
        panel.add(minall, c);

        minallTA = new JTextArea(1, 5);
        minallTA.setLayout(new GridLayout());
        //minallTA.setText("3");
        minallTA.setEnabled(true);
        c.gridx = 1;
        c.gridy = 0;
        panel.add(minallTA, c);

        JLabel maxall = new JLabel("max");
        maxall.setLayout(new GridLayout());
        maxall.setEnabled(true);
        c.gridx = 0;
        c.gridy = 1;
        panel.add(maxall, c);

        maxallTA = new JTextArea(1, 5);
        maxallTA.setLayout(new GridLayout());
        maxallTA.setEnabled(true);
        c.gridx = 1;
        c.gridy = 1;
        panel.add(maxallTA, c);

        //-------------------------------

        JLabel minallIN = new JLabel("min in");
        minallIN.setLayout(new GridLayout());
        minallIN.setEnabled(true);
        c.gridx = 0;
        c.gridy = 2;
        panel.add(minallIN, c);

        minallTAIN = new JTextArea(1, 5);
        minallTAIN.setLayout(new GridLayout());
        minallTAIN.setEnabled(true);
        c.gridx = 1;
        c.gridy = 2;
        panel.add(minallTAIN, c);

        JLabel maxallIN = new JLabel("max in");
        maxallIN.setLayout(new GridLayout());
        maxallIN.setEnabled(true);
        c.gridx = 0;
        c.gridy = 3;
        panel.add(maxallIN, c);

        maxallTAIN = new JTextArea(1, 5);
        maxallTAIN.setLayout(new GridLayout());
        maxallTAIN.setEnabled(true);
        c.gridx = 1;
        c.gridy = 3;
        panel.add(maxallTAIN, c);

        //------------------------------

        JLabel minallOUT = new JLabel("min out");
        minallOUT.setLayout(new GridLayout());
        minallOUT.setEnabled(true);
        c.gridx = 0;
        c.gridy = 4;
        panel.add(minallOUT, c);

        minallTAOUT = new JTextArea(1, 5);
        minallTAOUT.setLayout(new GridLayout());
        minallTAOUT.setEnabled(true);
        c.gridx = 1;
        c.gridy = 4;
        panel.add(minallTAOUT, c);

        JLabel maxallOUT = new JLabel("max out");
        maxallOUT.setLayout(new GridLayout());
        maxallOUT.setEnabled(true);
        c.gridx = 0;
        c.gridy = 5;
        panel.add(maxallOUT, c);

        maxallTAOUT = new JTextArea(1, 5);
        maxallTAOUT.setLayout(new GridLayout());
        maxallTAOUT.setEnabled(true);
        c.gridx = 1;
        c.gridy = 5;
        panel.add(maxallTAOUT, c);

        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        return mainPanel;
    }

    private void exportClick() {
        IOprotocols io = new IOprotocols();
        calcBase();
        io.exportBranchVertices(bsl);
    }

    private void importClick() {
        IOprotocols io = new IOprotocols();
        calcBase();

        try {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(HolmesBranchVerticesPrototype.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();

                //IOprotocols io = new IOprotocols();
                String path = file.getAbsolutePath();
                bsl = io.importBranchVertices(path);
            }
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }
    }

    private void compareClick() {
        GUIManager.getDefaultGUIManager().createComparisonnWindow();
        GUIManager.getDefaultGUIManager().showCompWindow();
    }

    private void calcBase() {
        for (Node n : overlord.getWorkspace().getProject().getNodes()) {
            if ((n.getOutNodes().size() > 1 || n.getInNodes().size() > 1)) {
                BranchStructure bs = new BranchStructure(n, new Color(0, 0, 0));
                bsl.add(bs);
            }
        }
    }

    private void calcTypeI() {
        calcBase();

        /**
         only branch vertex
         - type
         - degree in
         - degree out
         */
        for (BranchStructure bs : bsl) {
            bs.root.getType();
        }

    }

    private void calcTypeII() {
        /**
         root branch vertex
         - type
         - degree in
         - degree out

         branch end vertex
         - type
         */
    }

    private void calcTypeIII() {
        /**
         root branch vertex
         - type
         - degree in
         - degree out

         branch end vertex
         - type
         - degree in
         - degree out
         */
    }

    private void calcTypeIV() {
        /**
         root branch vertex
         - type
         - degree in
         - degree out

         branch end vertex
         - type
         - degree in
         - degree out
         - ISTOTNOŚĆ
         */
    }

    private void caclBranches(int type) {
        clearLayers();
        bsl.clear();
        resultArea.selectAll();
        resultArea.replaceSelection("");
        clearColors();
        ColorPalette cp = new ColorPalette();

        int minimalValue = -1;
        int maximalValue = Integer.MAX_VALUE;

        int minimaInlValue = -1;
        int maximaInlValue = Integer.MAX_VALUE;

        int minimalOutValue = -1;
        int maximalOutValue = Integer.MAX_VALUE;
        if (!minallTA.getText().isEmpty()) {
            int tmp = Integer.parseInt(minallTA.getText());
            if (tmp != 0)
                minimalValue = tmp;
        }
        if (!maxallTA.getText().isEmpty()) {
            int tmp = Integer.parseInt(maxallTA.getText());
            if (tmp != 0)
                maximalValue = tmp;
        }
        if (!minallTAIN.getText().isEmpty()) {
            int tmp = Integer.parseInt(minallTAIN.getText());
            if (tmp != 0)
                minimaInlValue = tmp;
        }
        if (!maxallTAIN.getText().isEmpty()) {
            int tmp = Integer.parseInt(maxallTAIN.getText());
            if (tmp != 0)
                maximaInlValue = tmp;
        }
        if (!minallTAOUT.getText().isEmpty()) {
            int tmp = Integer.parseInt(minallTAOUT.getText());
            if (tmp != 0)
                minimalOutValue = tmp;
        }
        if (!maxallTAOUT.getText().isEmpty()) {
            int tmp = Integer.parseInt(maxallTAOUT.getText());
            if (tmp != 0)
                maximalOutValue = tmp;
        }

        for (Node n : overlord.getWorkspace().getProject().getNodes()) {
            switch (type) {
                case 1:
                    if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)
                            &&
                            (n.getOutNodes().size() > 1 || n.getInNodes().size() > 1)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() >= minimalValue)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() <= maximalValue)
                            && ((
                            (n.getInNodes().size() >= minimaInlValue && n.getInNodes().size() <= maximaInlValue))
                            || (
                            (n.getOutNodes().size() >= minimalOutValue && n.getOutNodes().size() <= maximalOutValue)))
                    ) {
                        BranchStructure bs = new BranchStructure(n, cp.getColor());
                        bsl.add(bs);
                    }
                    break;
                case 2:
                    if (n.getType().equals(PetriNetElement.PetriNetElementType.PLACE)
                            &&
                            (n.getOutNodes().size() > 1 || n.getInNodes().size() > 1)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() >= minimalValue)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() <= maximalValue)
                            && ((
                            (n.getInNodes().size() >= minimaInlValue && n.getInNodes().size() <= maximaInlValue))
                            | (
                            (n.getOutNodes().size() >= minimalOutValue && n.getOutNodes().size() <= maximalOutValue)))
                    ) {
                        BranchStructure bs = new BranchStructure(n, cp.getColor());
                        bsl.add(bs);
                    }
                    break;
                default:
                    if ((n.getOutNodes().size() > 1 || n.getInNodes().size() > 1)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() >= minimalValue)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() <= maximalValue)
                            &&
                            (n.getInNodes().size() >= minimaInlValue && n.getInNodes().size() <= maximaInlValue)
                            &&
                            (n.getOutNodes().size() >= minimalOutValue && n.getOutNodes().size() <= maximalOutValue)
                    ) {
                        BranchStructure bs = new BranchStructure(n, cp.getColor());
                        bsl.add(bs);
                    }
            }
        }
        StringBuilder resultString = new StringBuilder();
        for (int i = 0; i < bsl.size(); i++) {
            Node root = bsl.get(i).root;
            resultString.append(i).append(") ").append(root.getType()).append(": ").append(root.getName()).append("\n\r");
            for (Node n : bsl.get(i).borderNodes) {
                String direction = "";

                if (bsl.get(i).paths.stream().anyMatch(path -> path.endNode.equals(n) && !path.isRevers)) {
                    direction = " --> ";
                }

                if (bsl.get(i).paths.stream().anyMatch(path -> path.endNode.equals(n) && path.isRevers)) {
                    direction = " <-- ";
                }

                if (bsl.get(i).paths.stream().anyMatch(path -> path.endNode.equals(n) && !path.isRevers) && bsl.get(i).paths.stream().anyMatch(path -> path.endNode.equals(n) && path.isRevers)) {
                    direction = " <-> ";
                }


                if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION))
                    resultString.append("\t ").append(direction).append(" T: <").append(n.getInNodes().size()).append(" | ").append(n.getOutNodes().size()).append("> ").append(n.getName()).append("\n\r");
                else
                    resultString.append("\t ").append(direction).append(" P: <").append(n.getInNodes().size()).append(" | ").append(n.getOutNodes().size()).append("> ").append(n.getName()).append("\n\r");
            }
        }

        int transitionNumberIn = 0;
        int transitionNumberOut = 0;
        int placeNumberIn = 0;
        int placeNumberOut = 0;

        int transitionNumberInWeight = 0;
        int transitionNumberOutWeight = 0;
        int placeNumberInWeight = 0;
        int placeNumberOutWeight = 0;

        for (Node n : overlord.getWorkspace().getProject().getNodes()) {
            if (n.getInArcs().size() > 1 || n.getOutArcs().size() > 1) {
                if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                    transitionNumberIn += n.getInArcs().size();
                    transitionNumberInWeight += n.getInArcs().stream().mapToInt(Arc::getWeight).sum();

                    transitionNumberOut += n.getInArcs().size();
                    transitionNumberOutWeight += n.getInArcs().stream().mapToInt(Arc::getWeight).sum();
                } else {
                    placeNumberIn += n.getInArcs().size();
                    placeNumberInWeight += n.getInArcs().stream().mapToInt(Arc::getWeight).sum();

                    placeNumberOut += n.getInArcs().size();
                    placeNumberOutWeight += n.getInArcs().stream().mapToInt(Arc::getWeight).sum();
                }
            }
        }
/*
        resultString.append("transitionNumberIn: " + transitionNumberIn + "\n");
        resultString.append("transitionNumberInWeight: " + transitionNumberInWeight + "\n");
        resultString.append("transitionNumberOut: " + transitionNumberOut + "\n");
        resultString.append("transitionNumberOutWeight: " + transitionNumberOutWeight + "\n");


        resultString.append("placeNumberIn: " + transitionNumberIn + "\n");
        resultString.append("placeNumberInWeight: " + transitionNumberInWeight + "\n");
        resultString.append("placeNumberOut: " + transitionNumberOut + "\n");
        resultString.append("placeNumberOutWeight: " + transitionNumberOutWeight + "\n");
*/
        resultArea.setText(resultString.toString());

        overlord.reset.clearGraphColors();
    }

    private void clearLayers() {
        for (BranchStructure bs : bsl) {
            bs.root.branchColor = Color.LIGHT_GRAY;//null;
            for (Node n : bs.borderNodes) {
                n.branchColor = null;
            }

            for (Arc a : calculateInternalArcs(bs.paths)) {
                a.arcDecoBox.layers.clear();
            }

        }
        overlord.getWorkspace().repaintAllGraphPanels();
    }

    private ArrayList<Arc> calculateInternalArcs(ArrayList<SubnetCalculator.Path> pathList) {
        ArrayList<Arc> listOfAllArcs = new ArrayList<>();
        for (SubnetCalculator.Path path : pathList) {
            for (int i = 0; i < path.path.size() - 1; i++) {
                Node startNode = path.path.get(i);
                Node endNode = path.path.get(i + 1);
                for (Arc arc : startNode.getOutArcs()) {
                    if (arc.getEndNode().getID() == endNode.getID())
                        listOfAllArcs.add(arc);
                }
            }
            for (Arc arc : path.endNode.getOutArcs()) {
                if (arc.getEndNode().getID() == path.startNode.getID())
                    listOfAllArcs.add(arc);
            }
        }
        return listOfAllArcs;
    }

    private ArrayList<VertCounter> countVerticesDegrees() {
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<VertCounter> counters = new ArrayList<>();
        for (Node n : overlord.getWorkspace().getProject().getNodes()) {
            Integer degree = n.getInNodes().size() + n.getOutNodes().size();

            if (counters.stream().anyMatch(x -> x.deg.equals(degree))) {
                for (VertCounter vc : counters) {
                    if (vc.deg.equals(degree)) {
                        if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION))
                            vc.t++;
                        else
                            vc.p++;
                    }
                }
            } else {
                VertCounter newDeg = new VertCounter(degree);
                if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION))
                    newDeg.t++;
                else
                    newDeg.p++;
                counters.add(newDeg);
            }
        }
        counters.sort(new DegSort());

        return counters;
    }

    private void clearColors() {
        for (Arc a : overlord.getWorkspace().getProject().getArcs()) {
            a.arcDecoBox.layers.clear();
        }

        for (Node n : overlord.getWorkspace().getProject().getNodes()) {
            n.branchBorderColors = new ArrayList<>();
            n.branchColor = null;
        }
    }

    public static class BranchStructureList implements Serializable {
        public ArrayList<BranchStructure> bsl;

        public BranchStructureList(ArrayList<BranchStructure> b) {
            bsl = b;
        }
    }

    public static class BranchStructure implements Serializable {
        Node root;
        ArrayList<Node> borderNodes;
        public ArrayList<SubnetCalculator.Path> paths;
        Color branchColor;

        public BranchStructure(Node r, ArrayList<Node> bn, ArrayList<SubnetCalculator.Path> p, Color c) {
            this.root = r;
            this.borderNodes = bn;
            this.branchColor = c;
            this.paths = p;
        }

        public BranchStructure(Node r, Color c) {
            this.root = r;
            this.branchColor = c;
            root.branchColor = c;
            this.paths = new ArrayList<>();
            this.borderNodes = new ArrayList<>();
            //clearColors();
            calcPaths();
            calcBorders(true);
            setElemetsColor();
        }

        /**
         * For decomposition
         *
         * @param r root vercicle
         */
        public BranchStructure(Node r) {
            this.root = r;
            this.paths = new ArrayList<>();
            this.borderNodes = new ArrayList<>();
            //clearColors();
            calcPaths();
            calcBorders(false);
        }


        private void calcPaths() {

            for (Node m : root.getOutNodes()) {
                ArrayList<Node> startPath = new ArrayList<>();
                startPath.add(root);
                ArrayList<Node> nodes = calculatePath(m, startPath);
                if (nodes.get(nodes.size() - 1).getOutNodes().contains(nodes.get(0))) {
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true, false));
                } else {
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), false, false));
                }
            }


            for (Node m : root.getInNodes()) {
                ArrayList<Node> startPath = new ArrayList<>();
                startPath.add(root);
                ArrayList<Node> nodes = calculatePathRevers(m, startPath);
                if (nodes.get(nodes.size() - 1).getInNodes().contains(nodes.get(0))) {
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true, true));
                } else {
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), false, true));
                }
            }


        }

        private void setElemetsColor() {
            for (SubnetCalculator.Path p : paths) {
                for (int i = 0; i < p.path.size() - 1; i++) {
                    for (Arc a : p.path.get(i).getOutArcs()
                    ) {
                        if (a.getEndNode().getID() == p.path.get(i + 1).getID()) {
                            a.arcDecoBox.layers.add(branchColor);
                        }
                    }
                    for (Arc a : p.path.get(i).getInArcs()) {
                        if (a.getStartNode().getID() == p.path.get(i + 1).getID()) {
                            a.arcDecoBox.layers.add(branchColor);
                        }
                    }
                }
            }
        }

        private void calcBorders(boolean isNotDeco) {
            for (SubnetCalculator.Path p : this.paths) {
                if (p.startNode.getID() == root.getID()) {
                    this.borderNodes.add(p.endNode);
                } else {
                    this.borderNodes.add(p.startNode);
                }
            }

            if (isNotDeco)
                for (Node n : this.borderNodes) {
                    n.branchBorderColors.add(this.branchColor);
                }
        }

        private ArrayList<Node> calculatePath(Node m, ArrayList<Node> path) {
            if (path.contains(m)) {
                return path;
            }
            path.add(m);
            if (m.getOutNodes().size() < 2 || m.getInNodes().size() < 2) {
                if (m.getOutNodes().size() == 1 && m.getInNodes().size() == 1) {
                    calculatePath(m.getOutNodes().get(0), path);
                }
            }
            return path;
        }

        private ArrayList<Node> calculatePathRevers(Node m, ArrayList<Node> path) {
            if (path.contains(m)) {
                return path;
            }
            path.add(m);
            if (m.getOutNodes().size() < 2 || m.getInNodes().size() < 2) {
                if ((m.getInNodes().size() == 1) && (m.getOutNodes().size() == 1)) {
                    calculatePathRevers(m.getInNodes().get(0), path);
                }
            }
            return path;
        }


    }

    private class VertCounter {
        public Integer deg;
        public int t;
        public int p;

        VertCounter(int degree) {
            this.deg = degree;
            this.t = 0;
            this.p = 0;
        }

        public Integer getDeg() {
            return deg;
        }
    }

    public class DegSort implements Comparator<VertCounter> {
        @Override
        public int compare(VertCounter o1, VertCounter o2) {
            return o1.getDeg().compareTo(o2.getDeg());
        }
    }
}
