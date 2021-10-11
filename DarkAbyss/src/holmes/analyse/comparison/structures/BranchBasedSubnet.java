package holmes.analyse.comparison.structures;

import holmes.analyse.SubnetCalculator;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class BranchBasedSubnet {

    public ArrayList<BranchVertex> branchVertices = new ArrayList<>();
    public ArrayList<Node> nodes = new ArrayList<>();
    public ArrayList<Transition> transitions = new ArrayList<>();
    public ArrayList<Place> places = new ArrayList<>();
    public ArrayList<Branch> pairs;
    public int[][] branchDirMatrix;

    public ArrayList<SubnetCalculator.Path> paths;
    ArrayList<Node> usedNodes = new ArrayList<>();

    public BranchBasedSubnet(SubnetCalculator.SubNet sn) {
        this.nodes = sn.getSubNode();
        this.places = sn.getSubPlaces();
        this.transitions = sn.getSubTransitions();

        for (Transition t : sn.getSubTransitions()) {
            if ((t.getInNodes().size() > 1 || t.getOutNodes().size() > 1)) {//|| (t.getOutInNodes().size() == 1)) {
                branchVertices.add(new BranchVertex(t, this));
            }
        }

        //if(branchVertices.size()==0){
        paths = calculatePaths(nodes);
        //}

        this.pairs = generatePaits();
        this.branchDirMatrix = generateMatrix();
    }

    public BranchBasedSubnet(BranchVertex bv) {
        branchVertices.add(bv);
        for (Branch b : bv.tbranch) {
            for (Node n : b.branchElements) {
                if (!nodes.contains(n)) {
                    this.nodes.add(n);
                }

                if (n.getType().equals(PetriNetElement.PetriNetElementType.PLACE) && !places.contains(n)) {
                    this.places.add((Place) n);
                }

                if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && !transitions.contains(n)) {
                    this.transitions.add((Transition) n);
                }

            }
        }

        for (Branch b : bv.pbranches) {
            for (Node n : b.branchElements) {
                if (!nodes.contains(n)) {
                    this.nodes.add(n);
                }

                if (n.getType().equals(PetriNetElement.PetriNetElementType.PLACE) && !places.contains(n)) {
                    this.places.add((Place) n);
                }

                if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && !transitions.contains(n)) {
                    this.transitions.add((Transition) n);
                }

            }
        }

        paths = calculatePaths(nodes);
        //}

        this.pairs = generatePaits();
        this.branchDirMatrix = generateMatrix();
    }

    private ArrayList<SubnetCalculator.Path> calculatePaths(ArrayList<Node> allNodes) {

        ArrayList<SubnetCalculator.Path> listOfPaths = new ArrayList<>();
        for (Node n : allNodes) {
            if (n.getOutNodes().size() > 1 || n.getInNodes().size() == 0 || (n.getInNodes().size() > 1 && n.getOutNodes().size() != 0)) {

                if (n.getOutNodes().size() > 1) {
                    usedNodes.add(n);
                    for (Node m : n.getOutNodes()) {
                        if (allNodes.contains(m)) {
                            ArrayList<Node> startPath = new ArrayList<>();
                            startPath.add(n);
                            ArrayList<Node> nodes = calculatePath(m, startPath, allNodes);
                            if (nodes.get(nodes.size() - 1).getOutNodes().contains(nodes.get(0))) {
                                listOfPaths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
                            } else {
                                listOfPaths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                            }
                        }
                    }
                } else {
                    ArrayList<Node> nodes = calculatePath(n, new ArrayList<>(), allNodes);
                    listOfPaths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                }
            }
        }

        for (Node n : allNodes) {
            if (!usedNodes.contains(n)) {
                ArrayList<Node> nodes = calculatePath(n, new ArrayList<>(), allNodes);
                listOfPaths.add(new SubnetCalculator.Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
            }
        }

        ArrayList<SubnetCalculator.Path> listToRemove = new ArrayList<>();
        ArrayList<SubnetCalculator.Path> listToAdd = new ArrayList<>();
        //merge addjastend
        for (int i = 0; i < listOfPaths.size(); i++) {
            for (int j = i + 1; j < listOfPaths.size(); j++) {
                if (listOfPaths.get(i).startNode.getID() == listOfPaths.get(j).endNode.getID()) {
                    ArrayList<Node> newList = new ArrayList<>();
                    newList.addAll(listOfPaths.get(j).path);
                    ArrayList<Node> toRemoveFirstElement = new ArrayList<>(listOfPaths.get(i).path);
                    toRemoveFirstElement.remove(0);
                    newList.addAll(toRemoveFirstElement);
                    listToAdd.add(new SubnetCalculator.Path(newList.get(0), newList.get(newList.size() - 1), newList));
                    listToRemove.add(listOfPaths.get(i));
                    listToRemove.add(listOfPaths.get(j));
                } else if (listOfPaths.get(i).endNode.getID() == listOfPaths.get(j).startNode.getID()) {
                    ArrayList<Node> newList = new ArrayList<>();
                    newList.addAll(listOfPaths.get(i).path);
                    ArrayList<Node> toRemoveFirstElement = new ArrayList<>(listOfPaths.get(j).path);
                    toRemoveFirstElement.remove(0);
                    newList.addAll(toRemoveFirstElement);
                    listToAdd.add(new SubnetCalculator.Path(newList.get(0), newList.get(newList.size() - 1), newList));

                    listToRemove.add(listOfPaths.get(i));
                    listToRemove.add(listOfPaths.get(j));
                }
            }
        }
        listOfPaths.removeAll(listToRemove);
        listOfPaths.addAll(listToAdd);

        listToRemove = new ArrayList<>();
        for (SubnetCalculator.Path p : listOfPaths
        ) {
            if (p.path.size() == 1 && listOfPaths.size() > 1) {
                listToRemove.add(p);
            }
        }


        listOfPaths.removeAll(listToRemove);
        return listOfPaths;
    }

    private ArrayList<Node> calculatePath(Node m, ArrayList<Node> path, ArrayList<Node> possibleNodes) {
        if (path.contains(m)) {
            return path;
        }
        usedNodes.add(m);
        path.add(m);
        if (m.getOutNodes().size() > 0) {
            if (m.getOutNodes().size() == 1) {
                if (possibleNodes.contains(m.getOutNodes().get(0)))
                    calculatePath(m.getOutNodes().get(0), path, possibleNodes);
            }
        }
        return path;
    }

    private ArrayList<Branch> generatePaits() {
        ArrayList<Branch> bp = new ArrayList<>();
        for (BranchVertex bv : branchVertices) {
            for (Branch bt : bv.tbranch) {

                //if(!bp.containsAll(bt))
                if (bp.stream().noneMatch(x -> x.branchElements.containsAll(bt.branchElements)))
                    bp.add(bt);
            }
        }
        return bp;
    }

    private int[][] generateMatrix() {
        int[][] bdm = new int[pairs.size()][transitions.size()];
        for (int b = 0; b < pairs.size(); b++) {
            for (int t = 0; t < transitions.size(); t++) {
                if (transitions.get(t).getID() == pairs.get(b).startNode.getID()) {
                    bdm[b][t] = -1;
                } else if (transitions.get(t).getID() == pairs.get(b).endNode.getID()) {
                    bdm[b][t] = 1;
                } else {
                    bdm[b][t] = 0;
                }
            }
        }
        return bdm;
    }

    public class BranchVertex {
        public Node root;

        public ArrayList<Branch> pbranches = new ArrayList<>();
        public ArrayList<Branch> tbranch = new ArrayList<>();

        public BranchVertex(Node r, BranchBasedSubnet bbs) {
            root = r;

            for (Node n : r.getOutNodes()) {
                Branch br = new Branch(r, n, bbs);
                if (br.startNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE) || br.endNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE))
                    pbranches.add(br);
                else
                    tbranch.add(br);
            }

            for (Node n : r.getInNodes()) {
                Branch br = new Branch(r, n, bbs);
                //reverse branch
                if ((br.startNode.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && br.endNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE)))//|| (br.startNode.equals(br.endNode)))
                    br.reverseBranch();

                if (br.startNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE) || br.endNode.getType().equals(PetriNetElement.PetriNetElementType.PLACE))
                    pbranches.add(br);
                else
                    tbranch.add(br);
            }
        }

        public ArrayList<Branch> getSek() {
            //TODO czy startowy też?
            return tbranch.stream().filter(x -> x.endNode.getOutInArcs().size() == 1 || x.startNode.getOutInArcs().size() == 1).collect(Collectors.toCollection(ArrayList::new));
        }

        public ArrayList<Branch> getLoop() {
            //TODO czy startowy też?
            return tbranch.stream().filter(x -> x.startNode.equals(x.endNode)).collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public static class Branch {
        public Node startNode;
        public Node endNode;
        public ArrayList<Node> internalBranchElements = new ArrayList<>();
        public ArrayList<Node> branchElements = new ArrayList<>();
        public ArrayList<Arc> branchArcs = new ArrayList<>();
        public BranchBasedSubnet parent;

        public HashMap<Node, Node> nodeMap = new HashMap<>();
        public HashMap<Arc, Arc> arcMap = new HashMap<>();


        public Branch(ArrayList<Node> be, ArrayList<Arc> ba, BranchBasedSubnet parentNode) {

            this.parent = parentNode;
            branchElements.addAll(be);
            branchArcs.addAll(ba);
            startNode = be.get(0);
            endNode = be.get(be.size() - 1);

            sortBranch();

            ArrayList<Node> intEr = new ArrayList<>();
            if (be.size() > 2) {
                intEr = new ArrayList<>(be);
                intEr.remove(intEr.size() - 1);
                intEr.remove(0);
            }
            internalBranchElements = intEr;

            if (branchElements.size() == 1) {
                System.out.println("Zła branch");
            }
        }

        public Branch(ArrayList<Node> be, ArrayList<Arc> ba, BranchBasedSubnet parentNode, HashMap<Node, Node> nm, HashMap<Arc, Arc> am) {

            this.nodeMap = nm;
            this.arcMap = am;
            this.parent = parentNode;
            branchElements.addAll(be);
            branchArcs.addAll(ba);
            startNode = be.get(0);
            endNode = be.get(be.size() - 1);

            sortBranch();

            ArrayList<Node> intEr = new ArrayList<>();
            if (be.size() > 2) {
                intEr = new ArrayList<>(be);
                intEr.remove(intEr.size() - 1);
                intEr.remove(0);
            }
            internalBranchElements = intEr;

            if (branchElements.size() == 1) {
                System.out.println("Zła branch");
            }
        }

        private void sortBranch() {
            ArrayList<Node> newOrdering = new ArrayList<>();

            ArrayList<Node> elements = branchElements.stream().filter(x -> x.getInArcs().size() > 1 || x.getOutArcs().size() > 1 || x.getOutArcs().size() == 0 || x.getInArcs().size() == 0).collect(Collectors.toCollection(ArrayList::new));
            Node start = null;
            Node end = null;
            //get start element
            for (Node nod : elements) {
                ArrayList<Node> tmp = nod.getInNodes();
                tmp.retainAll(branchElements);
                if (tmp.size() > 0)
                    end = nod;
            }

            for (Node nod : elements) {
                ArrayList<Node> tmp = nod.getOutNodes();
                tmp.retainAll(branchElements);
                if (tmp.size() > 0)
                    start = nod;
            }

            if (start == null) {
                newOrdering.add(end);
                while (newOrdering.size() != branchElements.size()) {
                    ArrayList<Node> tmp = new ArrayList<>(branchElements);
                    tmp.retainAll(
                            newOrdering.get(0).getInNodes());
                    if (tmp.size() == 0) {
                        break;
                    } else if (tmp.size() == 1) {
                        newOrdering.add(0, tmp.get(0));
                    } else {
                        newOrdering.add(0, tmp.get(0));
                        System.out.println("Koniec sortowania cyklu w branchu");
                    }
                }
            } else {
                newOrdering.add(start);
                while (newOrdering.size() != branchElements.size()) {
                    ArrayList<Node> tmp = new ArrayList<>(branchElements);
                    tmp.retainAll(
                            newOrdering.get(newOrdering.size() - 1).getOutNodes());
                    if (tmp.size() == 0) {
                        break;
                    } else if (tmp.size() == 1) {
                        newOrdering.add(tmp.get(0));
                    } else {
                        newOrdering.add(tmp.get(0));
                        System.out.println("Koniec sortowania cyklu w branchu");
                    }

                    if (newOrdering.size() > 1 && newOrdering.get(newOrdering.size() - 1).equals(end)) {
                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA1");
                        break;
                    }
                }
            }

            if (newOrdering.size() == branchElements.size())
                branchElements = newOrdering;
            //else
            //JOptionPane.showMessageDialog(null, "Coś zjebałeś nr 23 - sortowanie czegoś brakuje", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }

        private void OldsortBranch() {
            ArrayList<Node> newOrdering = new ArrayList<>();
            Node n = branchElements.get(0);
            newOrdering.add(n);
            //albo z true;
            while (newOrdering.size() != branchElements.size()) {
                boolean noPrev = false;
                boolean noNext = false;
                ArrayList<Node> tmp = new ArrayList<>(branchElements);
                tmp.retainAll(newOrdering.get(0).getInNodes());
                if (tmp.size() == 0) {
                    noPrev = true;
                } else if (tmp.size() == 1) {
                    if (tmp.get(0).getOutArcs().size() > 1 || tmp.get(0).getInArcs().size() > 1) {
                        System.out.println("-> " + tmp.get(0).getName());
                    }
                    if (newOrdering.contains(tmp.get(0)))
                        newOrdering.add(0, tmp.get(0));
                } else if (tmp.size() > 1) {
                    JOptionPane.showMessageDialog(null, "Coś zjebałeś nr 11", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                }

                tmp = new ArrayList<>(branchElements);
                tmp.retainAll(newOrdering.get(newOrdering.size() - 1).getOutNodes());
                if (tmp.size() == 0) {
                    noNext = true;
                } else if (tmp.size() == 1) {
                    if (tmp.get(0).getOutArcs().size() > 1 || tmp.get(0).getInArcs().size() > 1) {
                        System.out.println("<- " + tmp.get(0).getName());
                    }
                    newOrdering.add(tmp.get(0));
                } else if (tmp.size() > 1) {
                    JOptionPane.showMessageDialog(null, "Coś zjebałeś nr 12", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                }

                if (noNext && noPrev)
                    break;
            }

            branchElements.addAll(newOrdering);
        }

        Branch(Node s, Node nextNode, BranchBasedSubnet parentNode) {
            this.parent = parentNode;
            startNode = s;
            branchElements.add(s);
            for (Arc a : s.getOutInArcs()) {
                if (a.getStartNode().equals(nextNode) || a.getEndNode().equals(nextNode))
                    branchArcs.add(a);
            }
            generateList(nextNode);

            // TO CHECK
            if (internalBranchElements.size() > 0 && startNode.getID() != endNode.getID() && startNode.getInNodes().contains(internalBranchElements.get(0))) {
                Collections.reverse(branchElements);
                Collections.reverse(internalBranchElements);
                Node tmp = endNode;
                endNode = startNode;
                startNode = tmp;
            }

            if (branchElements.size() == 1) {
                System.out.println("Zła branch");
            }
        }

        public ArrayList<Node> returnBorderNodes() {
            ArrayList<Node> list = new ArrayList<>();
            list.add(startNode);
            list.add(endNode);
            return list;
        }

        public int branchSize() {
            return branchElements.size();
        }

        public void reverseBranch() {
            Collections.reverse(branchElements);
            Collections.reverse(branchArcs);
            Collections.reverse(internalBranchElements);
            startNode = branchElements.get(0);
            endNode = branchElements.get(branchElements.size() - 1);
        }

        void generateList(Node n) {
            if ((n.getOutNodes().size() > 1 || n.getInNodes().size() > 1) || (n.getOutNodes().size() == 0 || n.getInNodes().size() == 0)) {
                ArrayList<Node> list = new ArrayList<>(parent.nodes);
                list.retainAll(n.getOutInNodes());

                ArrayList<Node> listIn = new ArrayList<>(parent.nodes);
                listIn.retainAll(n.getInNodes());

                ArrayList<Node> listOut = new ArrayList<>(parent.nodes);
                listOut.retainAll(n.getOutNodes());
                if (list.size() > 1 && n.getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                    //probably add more
                    if ((listIn.size() > 1 && listOut.size() == 0) || (listOut.size() > 1 && listIn.size() == 0)) {
                        branchElements.add(n);
                        endNode = n;
                    } else {
                        internalBranchElements.add(n);
                        branchElements.add(n);
                        if (branchElements.size() > 1) {
                            int counter = 0;
                            for (Node m : n.getOutInNodes()) {
                                //if((!branchElements.get(branchElements.size()-1).equals(m)&&m.equals(startNode))||(!branchElements.contains(m)&&!m.equals(startNode)&& parent.nodes.contains(m))) {
                                if (!branchElements.contains(m) && parent.nodes.contains(m)) {
                                    ArrayList<Arc> arc1 = new ArrayList<>(n.getOutInArcs());
                                    ArrayList<Arc> arc2 = new ArrayList<>(m.getOutInArcs());
                                    arc1.retainAll(arc2);
                                    branchArcs.addAll(arc1);
                                    generateList(m);
                                    if (counter == 1) {
                                        System.out.println("tt1");
                                    }
                                    counter++;
                                }
                                if (counter > 1) {
                                    JOptionPane.showMessageDialog(null, "Coś zjebałeś nr 1", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        }
                    }

                } else {
                    branchElements.add(n);
                    endNode = n;
                }
            } else {
                internalBranchElements.add(n);
                branchElements.add(n);
                if (branchElements.size() > 1) {
                    int counter = 0;
                    for (Node m : n.getOutInNodes()) {
                        //nie zawiera idziesz dalej

                        //zawiera i

                        //if ((!branchElements.get(branchElements.size()-1).equals(m)&&m.equals(startNode))||
                        if ((!branchElements.contains(m) && !m.equals(startNode))) {
                            ArrayList<Arc> arc1 = new ArrayList<>(n.getOutInArcs());
                            ArrayList<Arc> arc2 = new ArrayList<>(m.getOutInArcs());
                            arc1.retainAll(arc2);
                            branchArcs.addAll(arc1);
                            generateList(m);
                            counter++;
                            if (counter == 2) {
                                System.out.println("tt");
                            }
                        }
                        if (counter > 1) {
                            JOptionPane.showMessageDialog(null, "Coś zjebałeś nr 2", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }

            if (endNode == null && branchElements.get(branchElements.size() - 1).getOutInNodes().contains(startNode)) {
                for (Arc a : branchElements.get(branchElements.size() - 1).getOutInArcs()) {
                    if (a.getStartNode().equals(startNode) || a.getEndNode().equals(startNode))
                        branchArcs.add(a);
                }
                branchElements.add(startNode);
                endNode = startNode;
            } else {
                if (endNode == null) {
                    //JOptionPane.showMessageDialog(null, "Coś zjebałeś nr 3", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
                    System.out.println("Tu powinno być okno cos zjenales nr 3");
                }
            }
        }

        public static class LenghtSort implements Comparator<Branch> {
            @Override
            public int compare(Branch o1, Branch o2) {
                return o1.branchElements.size() - o2.branchElements.size();
            }
        }
    }
}
