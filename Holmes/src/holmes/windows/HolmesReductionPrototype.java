package holmes.windows;

import holmes.analyse.SubnetCalculator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HolmesReductionPrototype extends JFrame {

    JTextArea infoPane = new JTextArea(30,30);

    public HolmesReductionPrototype() {

        setTitle("Reduction prototype");
        setSize(450, 700);
        JPanel panel = new JPanel();

        JPanel buttonPanel = new JPanel(new GridLayout(5, 2));

        JButton reductionAButton = new JButton("Reduction A");
        reductionAButton.addActionListener(e -> {
            reductionA();
        });
        buttonPanel.add(reductionAButton);

        JButton reductionBButton = new JButton("Reduction B");
        reductionBButton.addActionListener(e -> {
            reductionB();
        });
        buttonPanel.add(reductionBButton);

        JButton reductionCButton = new JButton("Reduction C");
        reductionCButton.addActionListener(e -> {
            reductionC();
        });
        buttonPanel.add(reductionCButton);

        JButton reductionDButton = new JButton("Reduction D");
        reductionDButton.addActionListener(e -> {
            reductionD();
        });
        buttonPanel.add(reductionDButton);

        JButton reductionEButton = new JButton("Reduction E");
        reductionEButton.addActionListener(e -> {
            reductionE();
        });
        buttonPanel.add(reductionEButton);

        JButton reductionFButton = new JButton("Reduction F");
        reductionFButton.addActionListener(e -> {
            reductionF();
        });
        buttonPanel.add(reductionFButton);

        JButton reductionGButton = new JButton("Reduction G");
        reductionGButton.setEnabled(false);
        reductionGButton.addActionListener(e -> {
            reductionG();
        });
        buttonPanel.add(reductionGButton);

        JButton reductionHButton = new JButton("Reduction H");
        reductionHButton.setEnabled(false);
        reductionHButton.addActionListener(e -> {
            reductionH();
        });
        buttonPanel.add(reductionHButton);


        JButton preButton = new JButton("Pre Reduction");
        preButton.setVisible(true);
        preButton.addActionListener(e -> {
            preReduction();
        });
        buttonPanel.add(preButton);


        JButton propCascadeButton = new JButton("Propper Cascade");
        propCascadeButton.addActionListener(e -> {
            properCascade();
        });
        buttonPanel.add(propCascadeButton);

        panel.add(buttonPanel, BorderLayout.NORTH);

        JPanel textPanel = new JPanel();
        //infoPaneInv.setLayout(new BorderLayout());

        JScrollPane jsp = new JScrollPane(infoPane);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        textPanel.add(jsp);

        panel.add(textPanel, BorderLayout.SOUTH);
        this.add(panel);
    }

    private void preReduction() {
        ArrayList<Node> list = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();

        ArrayList<Node> toReduce = list.stream().filter(x -> x.getInArcs().size() == 1 && x.getOutArcs().size() == 1).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<SubnetCalculator.Path> paths = findPaths();

        ArrayList<SubnetCalculator.Path> ppaths = paths.stream().filter(x -> x.startNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE) && x.endNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<SubnetCalculator.Path> tpaths = paths.stream().filter(x -> x.startNode.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && x.endNode.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)).collect(Collectors.toCollection(ArrayList::new));

        for (Node t : toReduce) {
            if ((ppaths.stream().anyMatch(x -> x.innerpath.contains(t)) || tpaths.stream().anyMatch(x -> x.innerpath.contains(t))) && !t.getInNodes().isEmpty()&& !t.getOutNodes().isEmpty()){
                Node inPlace = t.getInNodes().get(0);
                Node outPlace = t.getOutNodes().get(0);

                if (inPlace.getOutArcs().size() == 1 && outPlace.getInArcs().size() == 1 && outPlace.getOutArcs().size() == 1 && inPlace.getInArcs().size() == 1) {
                    for (Arc a : outPlace.getInArcs()) {
                        if (!(a.getStartNode().getID() == t.getID())) {
                            Arc na = new Arc(IdGenerator.getNextId(), a.getStartLocation(), inPlace.getLastLocation(), Arc.TypeOfArc.NORMAL);
                            na.setWeight(a.getWeight());
                            GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                        }
                    }

                    for (Arc a : outPlace.getOutArcs()) {
                        if (!(a.getEndNode().getID() == t.getID())) {
                            Arc na = new Arc(IdGenerator.getNextId(), inPlace.getLastLocation(), a.getEndLocation(), Arc.TypeOfArc.NORMAL);
                            na.setWeight(a.getWeight());
                            GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                        }
                    }

                    infoPane.append("Pre-Reduction: Sequential place merge :" + inPlace.getName() + "<-" + t.getName() + ">" + outPlace.getName() + "\n");
                    inPlace.setName("[" + inPlace.getName() + "<-" + t.getName() + ">" + outPlace.getName() + "]");

                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(outPlace.getLastLocation());

                    for (Arc a : outPlace.getOutInArcs()) {
                        GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().getGraphPanelArcs().remove(a);
                    }

                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(t.getLastLocation());
                    for (Arc a : t.getOutInArcs()) {
                        GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().getGraphPanelArcs().remove(a);
                    }

                    GUIManager.getDefaultGUIManager().markNetChange();
                    GUIManager.getDefaultGUIManager().repaint();

                    for (Node n : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes()) {
                        System.out.println("n : " + n.getName() + " in Arc: " + n.getInArcs().size()
                                + " out Arc: " + n.getOutArcs().size() + " in Nodes: " + n.getInNodes().size() + " out Nodes: " + n.getOutNodes().size());
                    }

                    if (inPlace.getType().equals(PetriNetElement.PetriNetElementType.PLACE))
                        ((Place) inPlace).drawGraphBoxP.setColorWithNumber(true, Color.BLUE, false, 0, true, "");
                    else
                        ((Transition) inPlace).drawGraphBoxT.setColorWithNumber(true, Color.RED, false, 0, true, "");

                }
            }
        }
    }

    private void reductionA() {
        ArrayList<Transition> list = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

        ArrayList<Transition> toReduce =list.stream().filter(x->x.getInArcs().size()==1 && x.getOutArcs().size()==1).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<SubnetCalculator.Path>  paths = findPaths();

        ArrayList<SubnetCalculator.Path> ppaths = paths.stream().filter(x->x.startNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE) && x.endNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE)).collect(Collectors.toCollection(ArrayList::new));

        for(Transition t : toReduce)
        {
            if(ppaths.stream().anyMatch(x->x.innerpath.contains(t))) {
                Node inPlace = t.getInNodes().get(0);
                Node outPlace = t.getOutNodes().get(0);

                System.out.println(t.getName());

                if ((inPlace.getOutArcs().size() == 1 && inPlace.getInArcs().size() > 0) || (outPlace.getOutArcs().size() > 0 && outPlace.getInArcs().size() == 1)) {
                    for (Arc a : outPlace.getInArcs()) {
                        if (!(a.getStartNode().getID() == t.getID())) {
                            Arc na = new Arc(IdGenerator.getNextId(), a.getStartLocation(), inPlace.getLastLocation(), Arc.TypeOfArc.NORMAL);
                            na.setWeight(a.getWeight());
                            GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                        }
                    }

                    for (Arc a : outPlace.getOutArcs()) {
                        if (!(a.getEndNode().getID() == t.getID())) {
                            Arc na = new Arc(IdGenerator.getNextId(), inPlace.getLastLocation(), a.getEndLocation(), Arc.TypeOfArc.NORMAL);
                            na.setWeight(a.getWeight());
                            GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                        }
                    }

                    infoPane.append("Reduction A: Sequential place merge :" + inPlace.getName() + "<-" + t.getName() + ">" + outPlace.getName() + "\n");
                    inPlace.setName("[" + inPlace.getName() + "<-" + t.getName() + ">" + outPlace.getName() + "]");

                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(outPlace.getLastLocation());

                    for (Arc a : outPlace.getOutInArcs()) {
                        GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().getGraphPanelArcs().remove(a);
                    }

                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(t.getLastLocation());
                    for (Arc a : t.getOutInArcs()) {
                        GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().getGraphPanelArcs().remove(a);
                    }

                    GUIManager.getDefaultGUIManager().markNetChange();
                    GUIManager.getDefaultGUIManager().repaint();

                    for (Node n : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes()) {
                        System.out.println("n : " + n.getName() + " in Arc: " + n.getInArcs().size()
                                + " out Arc: " + n.getOutArcs().size() + " in Nodes: " + n.getInNodes().size() + " out Nodes: " + n.getOutNodes().size());
                    }

                    ((Place) inPlace).drawGraphBoxP.setColorWithNumber(true, Color.BLUE, false, 0, true, "");
                }
            }
        }
    }

    private ArrayList<SubnetCalculator.Path> findPaths() {

        ArrayList<Node> nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();

        ArrayList<SubnetCalculator.Path>  result = new ArrayList<>();

        for (Node n : nodes) {
            //DLA sink jeszcze
            if(n.getOutNodes().size()>1 || n.getInNodes().size()>1)
            {
                for (Node outN : n.getOutNodes()) {
                    ArrayList<Node> path = new ArrayList<>();
                    path.add(n);
                    path = walkPath(outN,path);
                    result.add(new SubnetCalculator.Path(path.get(0),path.get(path.size()-1),path));
                }
            }
        }

        return result;
    }

    private ArrayList<Node> walkPath(Node n, ArrayList<Node> walk){
        walk.add(n);
        if(n.getOutNodes().size()==1 && n.getInNodes().size()==1)
        {
            walk = walkPath(n.getOutNodes().get(0),walk);
        }

        return  walk;
    }

    /*
    private static ArrayList<SubnetCalculator.Path> calculatePaths() {
        ArrayList<Node> allNodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
        ArrayList<Node> usedNodes = new ArrayList<>();

        ArrayList<SubnetCalculator.Path> listOfPaths = new ArrayList<>();
        for (Node n : allNodes) {
            if(!usedNodes.contains(n)) {
                //TODO CHANGE START POINT
                //if (n.getOutNodes().size() > 1 || n.getInNodes().size() == 0 || (n.getInNodes().size() > 1 && n.getOutNodes().size() != 0)) {
                if (n.getOutNodes().size() > 1 || n.getInNodes().size() > 1) {

                    if (n.getOutNodes().size() > 1) {
                        usedNodes.add(n);
                        for (Node m : n.getOutNodes()) {
                            ArrayList<Node> startPath = new ArrayList<>();
                            startPath.add(n);
                            ArrayList<Node> nodes = calculatePath(m, startPath, usedNodes);
                            if (nodes.get(nodes.size() - 1).getOutNodes().contains(nodes.get(0))) {
                                listOfPaths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
                            } else {
                                listOfPaths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                            }
                        }
                    } else {
                        //ArrayList<Node> nodes = calculatePath(n, new ArrayList<>(), usedNodes);
                        //listOfPaths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                    }
                }
            }
        }
        return listOfPaths;
    }
    */

    private static ArrayList<Node> calculatePath(Node m, ArrayList<Node> path, ArrayList<Node> usedNodes) {
        if (path.contains(m)) {
            return path;
        }
        usedNodes.add(m);
        path.add(m);
        if (m.getOutNodes().size() > 0) {
            if (m.getOutNodes().size() == 1) {
                calculatePath(m.getOutNodes().get(0), path,usedNodes);
            }
        }
        return path;
    }


    private ArrayList<Node> findBramchNodes(ArrayList<Node> nodes) {
        ArrayList<Node> result = new ArrayList<>();

        for (Node n: nodes) {
            if(n.getInNodes().size()>1|| n.getOutNodes().size()>1)
            {
                result.add(n);
            }
        }

        return result;
    }

    private void reductionB() {
        ArrayList<Place> list = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();

        ArrayList<Place> toReduce =list.stream().filter(x->x.getInArcs().size()==1 && x.getOutArcs().size()==1).collect(Collectors.toCollection(ArrayList::new));

        ArrayList<SubnetCalculator.Path>  paths = findPaths();

        ArrayList<SubnetCalculator.Path> tpaths = paths.stream().filter(x->x.startNode.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && x.endNode.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)).collect(Collectors.toCollection(ArrayList::new));

        for(Place t : toReduce) {
            if (tpaths.stream().anyMatch(x -> x.innerpath.contains(t))) {
                Node inTransition = t.getInNodes().get(0);
                Node outTransition = t.getOutNodes().get(0);

                if ((inTransition.getOutArcs().size() == 1 && inTransition.getInArcs().size() > 0) || (outTransition.getOutArcs().size() > 0 && outTransition.getInArcs().size() == 1)) {
                //if (inTransition.getOutArcs().size() == 1 && outTransition.getInArcs().size() == 1) {
                    for (Arc a : outTransition.getInArcs()) {
                        if (!(a.getStartNode().getID() == t.getID())) {
                            Arc na = new Arc(IdGenerator.getNextId(), a.getStartLocation(), inTransition.getLastLocation(), Arc.TypeOfArc.NORMAL);
                            na.setWeight(a.getWeight());
                            GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                        }
                    }

                    for (Arc a : outTransition.getOutArcs()) {
                        if (!(a.getEndNode().getID() == t.getID())) {
                            Arc na = new Arc(IdGenerator.getNextId(), inTransition.getLastLocation(), a.getEndLocation(), Arc.TypeOfArc.NORMAL);
                            na.setWeight(a.getWeight());
                            GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                        }
                    }

                    infoPane.append("Reduction B: Sequential transition merge :" + inTransition.getName() + "-" + t.getName() + "-" + outTransition.getName() + "\n");
                    inTransition.setName("[" + inTransition.getName() + "<-" + t.getName() + "->" + outTransition.getName() + "]");

                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(outTransition.getLastLocation());
                    for (Arc a : outTransition.getOutInArcs()) {
                        GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().getGraphPanelArcs().remove(a);
                    }

                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(t.getLastLocation());
                    for (Arc a : t.getOutInArcs()) {
                        GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().getGraphPanelArcs().remove(a);
                    }
                    GUIManager.getDefaultGUIManager().markNetChange();
                    GUIManager.getDefaultGUIManager().repaint();

                    ((Transition) inTransition).drawGraphBoxT.setColorWithNumber(true, Color.RED, false, 0, true, "");
                }
            }
        }
    }

    private void reductionC() {
        ArrayList<Place> list = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();

        ArrayList<Place> toReduce =list.stream().filter(x->x.getInArcs().size()==1 && x.getOutArcs().size()==1
            && x.getInArcs().get(0).getWeight()==x.getOutArcs().get(0).getWeight()).collect(Collectors.toCollection(ArrayList::new));

        //to only where is one arc

        while(toReduce.size()>0)
        {
            Place p = toReduce.get(0);
            toReduce.remove(0);

            ArrayList<Node> parallel = toReduce.stream().filter(x->x.getInNodes().get(0).getID()== p.getInNodes().get(0).getID() && x.getOutNodes().get(0).getID()== p.getOutNodes().get(0).getID()).collect(Collectors.toCollection(ArrayList::new));

            String parallelElements = "";
            for (Node n : parallel) {
                p.setName("("+p.getName()+"="+n.getName()+")");
                parallelElements+="=="+n.getName();
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(n.getLastLocation());
            }
            if(parallel.size()>0) {
                infoPane.append("Reduction C: Parallel place merge :" + p.getName() +  "==" + parallelElements + "\n");
                p.drawGraphBoxP.setColorWithNumber(true, Color.GREEN, false, 0, true, "");
                toReduce.removeAll(parallel);
            }
        }
    }

    private void reductionD() {
        ArrayList<Transition> list = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

        ArrayList<Transition> toReduce =list.stream().filter(x->x.getInArcs().size()==1 && x.getOutArcs().size()==1
                && x.getInArcs().get(0).getWeight()==x.getOutArcs().get(0).getWeight()).collect(Collectors.toCollection(ArrayList::new));

        //to only where is one arc

        while(toReduce.size()>0)
        {
            Transition t = toReduce.get(0);
            toReduce.remove(0);

            System.out.println("Redukowana t: " + t.getName());
            ArrayList<Node> parallel = toReduce.stream().filter(x->x.getInNodes().get(0).getID()== t.getInNodes().get(0).getID() && x.getOutNodes().get(0).getID()== t.getOutNodes().get(0).getID()).collect(Collectors.toCollection(ArrayList::new));

            String parallelElements = "";
            for (Node n : parallel) {
                t.setName("("+t.getName()+"="+n.getName()+")");
                parallelElements+="=="+n.getName();
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(n.getLastLocation());
            }
            if(parallel.size()>0) {
                infoPane.append("Reduction D: Parallel transition merge :" + t.getName() +  "==" + parallelElements + "\n");
                toReduce.removeAll(parallel);
                t.drawGraphBoxT.setColorWithNumber(true, Color.YELLOW, false, 0, true, "");
            }
        }
    }

    private void reductionE() {
        ArrayList<Transition> list = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

        ArrayList<Transition> toReduce =list.stream().filter(x->x.getInArcs().size()==1 && x.getInArcs().get(0).getArcType().equals(Arc.TypeOfArc.READARC)).collect(Collectors.toCollection(ArrayList::new));

        for (Node n : toReduce) {
            GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(n.getLastLocation());
        }
    }

    private void reductionF() {
        ArrayList<Place> list = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();

        ArrayList<Place> toReduce =list.stream().filter(x->x.getInArcs().size()==1 && x.getInArcs().get(0).getArcType().equals(Arc.TypeOfArc.READARC)).collect(Collectors.toCollection(ArrayList::new));

        for (Node n : toReduce) {
            GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(n.getLastLocation());
        }
    }

    private void reductionG() {
        ArrayList<SubnetCalculator.Path>  paths = findPaths();

        ArrayList<SubnetCalculator.Path> tppaths = paths.stream().filter(x->x.startNode.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && x.endNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE)).collect(Collectors.toCollection(ArrayList::new));



        for (SubnetCalculator.Path path : tppaths) {
            boolean differentWeight = false;

            String nameMergedArc = "";
            //TODO for some unstable should be possible
            for (Node n : path.innerpath) {
                nameMergedArc+= ">"+n.getName() + ">";
                for(Arc a :n.getOutInArcs()){
                    if(a.getWeight()!=1)
                        differentWeight = true;
                }
            }

            if(!differentWeight){
                Arc na = new Arc(IdGenerator.getNextId(), path.startNode.getLastLocation(), path.endNode.getLastLocation(), Arc.TypeOfArc.NORMAL);
                na.setWeight(1);
                na.setName(nameMergedArc);
                na.setComment(nameMergedArc);
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                for (Node n : path.innerpath) {
                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(n.getLastLocation());
                }

                if (path.startNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE))
                    ((Place) path.startNode).drawGraphBoxP.setColorWithNumber(true, Color.BLACK, false, 0, true, "");
                else
                    ((Transition) path.startNode).drawGraphBoxT.setColorWithNumber(true, Color.BLACK, false, 0, true, "");

                if (path.endNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE))
                    ((Place) path.endNode).drawGraphBoxP.setColorWithNumber(true, Color.BLACK, false, 0, true, "");
                else
                    ((Transition) path.endNode).drawGraphBoxT.setColorWithNumber(true, Color.BLACK, false, 0, true, "");

            }
        }
    }

    private void reductionH() {
        ArrayList<SubnetCalculator.Path>  paths = findPaths();

        ArrayList<SubnetCalculator.Path> ptpaths = paths.stream().filter(x->x.startNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE) && x.endNode.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)).collect(Collectors.toCollection(ArrayList::new));

        for (SubnetCalculator.Path path : ptpaths) {
            boolean differentWeight = false;

            String nameMergedArc = "";
            //TODO for some unstable should be possible
            for (Node n : path.innerpath) {
                nameMergedArc+= ">"+n.getName() + ">";
                for(Arc a :n.getOutInArcs()){
                    if(a.getWeight()!=1)
                        differentWeight = true;
                }
            }

            if(!differentWeight){
                Arc na = new Arc(IdGenerator.getNextId(), path.startNode.getLastLocation(), path.endNode.getLastLocation(), Arc.TypeOfArc.NORMAL);
                na.setWeight(1);
                na.setName(nameMergedArc);
                na.setComment(nameMergedArc);
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().addArc(na);
                for (Node n : path.innerpath) {
                    GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanels().get(0).getSelectionManager().deleteElementLocation(n.getLastLocation());
                }
            }
        }
    }

    private void properCascade() {
        int numberOfArcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs().size();
        int numberOfNode = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().size();

        int oldNOA = Integer.MAX_VALUE;
        int oldNON = Integer.MAX_VALUE;

        while(numberOfArcs!=oldNOA && numberOfNode!=oldNON){
            preReduction();
            reductionA();
            reductionB();
            reductionC();
            reductionD();

            oldNOA = numberOfArcs;
            oldNON = numberOfNode;

            numberOfArcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs().size();
            numberOfNode = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().size();
        }
    }
}
