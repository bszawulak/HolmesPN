package holmes.windows;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.SubnetCalculator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class HolmesBranchVerticesPrototype extends JFrame {
    private JPanel mainPanel;
    private JPanel logPanel;
    private JPanel inputPanel;
    private GUIManager overlord;

    JTextArea minallTA, maxallTA, minallTAIN, maxallTAIN, minallTAOUT, maxallTAOUT;
    public ArrayList<BranchStructure> bsl = new ArrayList<>();

    public HolmesBranchVerticesPrototype() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception e) {

        }
        overlord = GUIManager.getDefaultGUIManager();
        setVisible(false);
        this.setTitle("Decomposition");

        setLayout(new BorderLayout());
        setSize(new Dimension(900, 600));
        setLocation(15, 15);

        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        GraphletsCalculator.GraphletsCalculator();
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        logPanel = createLogPanel(0, 0, 180, 130);
        inputPanel = createInputPanel(0, 130, 500, 300);
        panel.add(logPanel, BorderLayout.WEST);
        panel.add(inputPanel, BorderLayout.EAST);
        panel.repaint();
        return panel;
    }

    private JPanel createLogPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Info"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));

        int posX = 10;
        int posY = 10;

        GridBagConstraints c = new GridBagConstraints();


        ///////first subnet//////////
        JLabel text1 = new JLabel("NUMBER OF DEGREES");
        text1.setLayout(new GridLayout());
        text1.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(text1, c);

        //for each

        int row = 1;

        for (ArrayList<Integer> group : countVerticesDegrees()) {
            JLabel groupDegree = new JLabel("Degree : " + group.get(0));
            groupDegree.setLayout(new GridLayout());
            groupDegree.setEnabled(true);
            //c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = row;
            panel.add(groupDegree, c);

            JLabel groupValue = new JLabel(String.valueOf(group.size()));
            groupValue.setLayout(new GridLayout());
            groupValue.setEnabled(true);
            //c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = row;
            panel.add(groupValue, c);

            row++;
        }

        return panel;
    }

    private JPanel createInputPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Info"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));

        int posX = 10;
        int posY = 10;

        GridBagConstraints c = new GridBagConstraints();

        JLabel minall = new JLabel("min");
        minall.setLayout(new GridLayout());
        minall.setEnabled(true);
        c.gridx = 0;
        c.gridy = 0;
        panel.add(minall, c);

        minallTA = new JTextArea(1, 5);
        minallTA.setLayout(new GridLayout());
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

        JButton calc = new JButton("Find Branch Vertices");
        calc.setLayout(new GridLayout());
        calc.setEnabled(true);
        c.gridx = 1;
        c.gridy = 7;

        calc.setBounds(posX, posY, 120, 36);
        calc.setMargin(new Insets(0, 0, 0, 0));
        calc.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        calc.addActionListener(actionEvent -> caclBranches());
        calc.setFocusPainted(false);

        panel.add(calc, c);

        return panel;
    }

    private void caclBranches() {
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
            if (
                    (n.getOutNodes().size() > 1 || n.getInNodes().size() > 1)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() >= minimalValue)
                            &&
                            (n.getOutNodes().size() + n.getInNodes().size() <= maximalValue)
                            &&
                            (n.getInNodes().size() >= minimaInlValue || n.getInNodes().size() <= maximaInlValue)
                            &&
                            (n.getOutNodes().size() >= minimalOutValue || n.getOutNodes().size() <= maximalOutValue)
            ) {


                BranchStructure bs = new BranchStructure(n, cp.getColor());
                bsl.add(bs);

            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    private ArrayList<ArrayList<Integer>> countVerticesDegrees() {

        ArrayList<ArrayList<Integer>> counters = new ArrayList<>();

        for (Node n : overlord.getWorkspace().getProject().getNodes()) {
            Integer degree = n.getInNodes().size() + n.getOutNodes().size();

            boolean exist = false;
            int existlocation = -1;
            for (int i = 0; i < counters.size(); i++) {
                if (!counters.get(i).isEmpty() & counters.get(i).get(0).equals(degree)) {
                    exist = true;
                    existlocation = i;
                }
            }

            if (exist) {
                counters.get(existlocation).add(degree);
            } else {
                ArrayList<Integer> newGroup = new ArrayList<>();
                newGroup.add(degree);
                if (counters.isEmpty()) {
                    counters.add(newGroup);
                } else {
                    boolean added = false;
                    for (int j = 0; j < counters.size(); j++) {
                        if (counters.get(j).get(0) > degree) {
                            counters.add(j, newGroup);
                            added = true;
                            break;
                        }
                    }

                    if (!added)
                        counters.add(newGroup);
                }
            }
        }

        return counters;
    }

    private void clearColors() {
        for (Arc a : overlord.getWorkspace().getProject().getArcs()) {
            a.layers.clear();
        }

        for (Node n : overlord.getWorkspace().getProject().getNodes()) {
            n.branchBorderColors = new ArrayList<>();
            n.branchColor = null;
        }


    }

    public static class BranchStructure {
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
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
                } else {
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                }
            }


            for (Node m : root.getInNodes()) {
                ArrayList<Node> startPath = new ArrayList<>();
                startPath.add(root);
                ArrayList<Node> nodes = calculatePathRevers(m, startPath);
                if (nodes.get(nodes.size() - 1).getInNodes().contains(nodes.get(0))) {
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
                } else {
                    paths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                }
            }


        }

        private void setElemetsColor() {
            for (SubnetCalculator.Path p : paths
            ) {

                for (int i = 0; i < p.path.size() - 1; i++
                ) {
                    //if(p.path.get(i).getOutArcs().contains(p.path.get(i+1)))
                    for (Arc a : p.path.get(i).getOutArcs()
                    ) {
                        if (a.getEndNode().getID() == p.path.get(i + 1).getID()) {
                            a.layers.add(branchColor);
                        }
                    }

                    //p.path.get(i).getInArcs().contains(p.path.get(i+1))
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
                for (Node n : this.borderNodes
                ) {
                    n.branchBorderColors.add(this.branchColor);
                }
        }

        private ArrayList<Node> calculatePath(Node m, ArrayList<Node> path) {
            if (path.contains(m)) {
                return path;
            }
            //usedNodes.add(m);
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
                Collections.reverse(path);
                return path;
            }
            //usedNodes.add(m);
            path.add(m);
            if (m.getOutNodes().size() < 2 || m.getInNodes().size() < 2) {
                if ((m.getInNodes().size() == 1)&&(m.getOutNodes().size() == 1)) {
                    calculatePath(m.getInNodes().get(0), path);
                }
            }

            Collections.reverse(path);
            return path;
        }

    }
}
