package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.ToDoubleBiFunction;

/**
 * Klasa odpowiedzialna za dekompozycję PN do wybranych typów podsieci
 */
public class SubnetCalculator {
    private enum SubNetType {ZAJCEV, SNET, TNET, ADT, ADP, OOSTUKI, TZ, HOU, NISHI, CYCLE, SMC}

    public static ArrayList<SubNet> functionalSubNets = new ArrayList<>();
    public static ArrayList<SubNet> snetSubNets = new ArrayList<>();
    public static ArrayList<SubNet> tnetSubNets = new ArrayList<>();
    public static ArrayList<SubNet> adtSubNets = new ArrayList<>();
    public static ArrayList<SubNet> adpSubNets = new ArrayList<>();
    public static ArrayList<SubNet> tzSubNets = new ArrayList<>();
    public static ArrayList<SubNet> houSubNets = new ArrayList<>();
    public static ArrayList<SubNet> nishiSubNets = new ArrayList<>();
    public static ArrayList<SubNet> cycleSubNets = new ArrayList<>();
    public static ArrayList<SubNet> ootsukiSubNets = new ArrayList<>();
    public static ArrayList<SubNet> smcSubNets = new ArrayList<>();
    public static ArrayList<Path> paths = new ArrayList<>();

    private static ArrayList<ArrayList<Path>> houResultList = new ArrayList<>();
    private static ArrayList<ArrayList<Path>> nishiResultList = new ArrayList<>();
    private static ArrayList<ArrayList<Path>> tzResultList = new ArrayList<>();
    private static ArrayList<ArrayList<Path>> ootsukiResultList = new ArrayList<>();

    private static ArrayList<Transition> allTransitions;
    private static ArrayList<Place> allPlaces;
    private static ArrayList<ArrayList<Integer>> invMatrixT;
    private static ArrayList<ArrayList<Integer>> invMatrixP;
    private static ArrayList<Node> allNodes;
    private static ArrayList<Node> usedNodes;

    /**
     * Metoda odpowiedzialna za dekompozycję do podsieci funkcyjnych Zajcewa - nie mylić z sieciami funkcyjnymi
     */

    public static void compileElements() {
        allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        allPlaces = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        invMatrixT = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
        invMatrixP = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix();
        allNodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
        usedNodes = new ArrayList<>();
    }

    public static void compileTestElements(ArrayList<Transition> at, ArrayList<Place> ap, ArrayList<Node> an, ArrayList<ArrayList<Integer>> ati) {
        allTransitions = at;
        allPlaces = ap;
        invMatrixT = ati;
        allNodes = an;
        usedNodes = new ArrayList<>();
    }

    public static void generateFS() {
        while (!allTransitions.isEmpty()) {
            Transition firstTransition = allTransitions.get(0);
            ArrayList<Transition> temporaryList = new ArrayList<>();
            temporaryList.add(firstTransition);
            allTransitions.remove(firstTransition);
            temporaryList = findFunctionalTransition(temporaryList);
            functionalSubNets.add(new SubNet(SubNetType.ZAJCEV, temporaryList, null, null, null, null));
        }

        if (!functionalSubNets.isEmpty()) {
            //GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    private static ArrayList<Transition> findFunctionalTransition(ArrayList<Transition>temporaryList)
    {
        boolean found = false;
        for (Transition transition : allTransitions) {
            ArrayList<Transition> listToAdd = new ArrayList<>();
            if (chceckParalellInputOutPut(transition, temporaryList)) {
                if (!listToAdd.contains(transition)) {
                    listToAdd.add(transition);
                    found=true;
                }
            }
            temporaryList.addAll(listToAdd);
        }

        allTransitions.removeAll(temporaryList);
        if(found)
        {
            temporaryList = findFunctionalTransition(temporaryList);
        }
        return temporaryList;
    }

    private static boolean chceckParalellInputOutPut(Node newTran, ArrayList<Transition> sub) {
        boolean paralel = false;
        boolean seq = false;
        for (Transition subTran : sub) {
            boolean in = chceckNodeOutIn(newTran.getInNodes(), subTran.getInNodes());
            boolean out = chceckNodeOutIn(newTran.getOutNodes(), subTran.getOutNodes());
            boolean inout = chceckNodeOutIn(newTran.getInNodes(), subTran.getOutNodes());
            boolean outin = chceckNodeOutIn(newTran.getOutNodes(), subTran.getInNodes());

            if (in || out)
                paralel = true;
            if (inout || outin)
                seq = true;
        }

        if (seq)
            return false;
        return paralel;
    }

    private static boolean chceckSeqInputOutPut(Node newPlac, ArrayList<Place> sub) {
        boolean paralel = false;
        boolean seq = false;
        for (Place subPlace : sub) {
            boolean in = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getInNodes());
            boolean out = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getOutNodes());
            boolean inout = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getOutNodes());
            boolean outin = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getInNodes());

            if (in || out)
                paralel = true;
            if (inout || outin)
                seq = true;
        }

        if (paralel)
            return false;
        return seq;
    }

    private static boolean chceckSeqTInputOutPut(Node newPlac, ArrayList<Transition> sub) {
        boolean paralel = false;
        boolean seq = false;
        for (Transition subPlace : sub) {
            boolean in = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getInNodes());
            boolean out = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getOutNodes());
            boolean inout = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getOutNodes());
            boolean outin = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getInNodes());

            if (in || out)
                paralel = true;
            if (inout || outin)
                seq = true;
        }

        if (paralel)
            return false;
        return seq;
    }

    private static boolean chceckNodeOutIn(ArrayList<Node> newNodes, ArrayList<Node> subNodes) {
        for (Node newNode : newNodes)
            for (Node subNode : subNodes)
                if (newNode.getID() == subNode.getID())
                    return true;

        return false;
    }

    public static void cleanSubnets() {
        functionalSubNets = new ArrayList<>();
        snetSubNets = new ArrayList<>();
        tnetSubNets = new ArrayList<>();
        adtSubNets = new ArrayList<>();
        adpSubNets = new ArrayList<>();
        tzSubNets = new ArrayList<>();
        houSubNets = new ArrayList<>();
        nishiSubNets = new ArrayList<>();
        cycleSubNets = new ArrayList<>();
        houResultList = new ArrayList<>();
        nishiResultList = new ArrayList<>();
        tzResultList = new ArrayList<>();
        ootsukiResultList = new ArrayList<>();
        ootsukiSubNets = new ArrayList<>();
        smcSubNets = new ArrayList<>();
    }

    public static void generateSnets() {
        if (invMatrixT != null) {
            if (!invMatrixT.isEmpty()) {
                ArrayList<ArrayList<Place>> placesOfSupport = new ArrayList<>();

                for (ArrayList<Integer> inv : invMatrixT) {
                    ArrayList<Transition> invSupport = new ArrayList<>();
                    for (int i = 0; i < inv.size(); i++) {//Integer t : inv ) {
                        if (inv.get(i) > 0)
                            invSupport.add(allTransitions.get(i));
                    }

                    ArrayList<Place> invPlace = new ArrayList<>();
                    for (int i = 0; i < invSupport.size(); i++) {
                        for (int j = i + 1; j < invSupport.size(); j++) {
                            for (Place ip : invSupport.get(i).getPostPlaces()) {
                                for (Place jp : invSupport.get(j).getPrePlaces()) {
                                    if (ip.getID() == jp.getID() && !inv.contains(ip))
                                        invPlace.add(ip);
                                }
                            }

                            for (Place ip : invSupport.get(i).getPrePlaces()) {
                                for (Place jp : invSupport.get(j).getPostPlaces()) {
                                    if (ip.getID() == jp.getID() && !inv.contains(ip))
                                        invPlace.add(ip);
                                }
                            }
                        }
                    }
                    placesOfSupport.add(invPlace);
                }

                //Sprawdzamy w jakich T-invariantach występują te miejsca...

                Integer[][] matrix = new Integer[allPlaces.size()][placesOfSupport.size()];
                ArrayList<ArrayList<Integer>> list = new ArrayList<>();
                for (Place p : allPlaces
                ) {
                    ArrayList<Integer> l = new ArrayList<>();
                    for (ArrayList<Place> sup : placesOfSupport
                    ) {
                        if (sup.contains(p)) {
                            matrix[allPlaces.indexOf(p)][placesOfSupport.indexOf(sup)] = 1;
                            l.add(placesOfSupport.indexOf(sup));
                        } else {
                            matrix[allPlaces.indexOf(p)][placesOfSupport.indexOf(sup)] = 0;
                        }
                    }
                    list.add(l);
                }

                //create s-net

                ArrayList<ArrayList<Place>> snets = new ArrayList<>();

                ArrayList<Integer> used = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    if (!used.contains(i)) {
                        ArrayList<Place> net = new ArrayList<>();
                        net.add(allPlaces.get(i));
                        used.add(i);
                        for (int j = i + 1; j < list.size(); j++) {
                            if (list.get(i).equals(list.get(j)) && !used.contains(j)) {
                                net.add(allPlaces.get(j));
                                used.add(j);
                            }
                        }

                        snets.add(net);
                        snetSubNets.add(new SubNet(SubNetType.SNET, null, net, null, null, null));
                    }
                }


                if (!snetSubNets.isEmpty()) {
                    //GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Decomposition can not be processed, because of the lack of invariants!", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Before determine ADT sets, you need to generate T-invariants.", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void generateTnets() {
        //TODO  unifikacja z ADT
    }

    public static void generateADT() {
        //cleanSubnets();

        if (invMatrixT != null) {
            if (!invMatrixT.isEmpty()) {
                //ArrayList<ArrayList<Integer>> invMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
                ArrayList<ArrayList<Integer>> nonAssignedRows = new ArrayList<>();

                for (int i = 0; i < invMatrixT.get(0).size(); i++) {
                    ArrayList<Integer> newRow = new ArrayList<>();
                    for (int j = 0; j < invMatrixT.size(); j++) {
                        newRow.add(invMatrixT.get(j).get(i));
                    }
                    nonAssignedRows.add(newRow);
                }

                ArrayList<Integer> listOfusedTransitions = new ArrayList<>();

                for (int i = 0; i < nonAssignedRows.size(); i++) {
                    ArrayList<Integer> newADTset = new ArrayList<Integer>();

                    if (!listOfusedTransitions.contains(i)) {
                        newADTset.add(i);
                        listOfusedTransitions.add(i);
                        for (int j = i; j < nonAssignedRows.size(); j++) {
                            if (checkADT(nonAssignedRows.get(i), nonAssignedRows.get(j))) {
                                if (!listOfusedTransitions.contains(j)) {
                                    listOfusedTransitions.add(j);
                                    newADTset.add(j);
                                }
                            }
                        }
                    }
                    if (!newADTset.isEmpty())
                        adtSubNets.add(new SubNet(SubNetType.ADT, null, null, null, newADTset, null));
                }

                if (!adtSubNets.isEmpty()) {
                    //GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Decomposition can not be processed, because of the lack of invariants!", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Before determine ADT sets, you need to generate T-invariants.", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void generateADP() {
        //cleanSubnets();

        if (GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix() != null) {
            if (!GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix().isEmpty()) {
                ArrayList<ArrayList<Integer>> invMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix();
                ArrayList<ArrayList<Integer>> nonAssignedRows = new ArrayList<>();

                for (int i = 0; i < invMatrix.get(0).size(); i++) {
                    ArrayList<Integer> newRow = new ArrayList<>();
                    for (int j = 0; j < invMatrix.size(); j++) {
                        newRow.add(invMatrix.get(j).get(i));
                    }
                    nonAssignedRows.add(newRow);
                }

                ArrayList<Integer> listOfusedTransitions = new ArrayList<>();

                for (int i = 0; i < nonAssignedRows.size(); i++) {
                    ArrayList<Integer> newADPset = new ArrayList<Integer>();

                    if (!listOfusedTransitions.contains(i)) {
                        newADPset.add(i);
                        listOfusedTransitions.add(i);
                        for (int j = i; j < nonAssignedRows.size(); j++) {
                            if (checkADT(nonAssignedRows.get(i), nonAssignedRows.get(j))) {
                                if (!listOfusedTransitions.contains(j)) {
                                    listOfusedTransitions.add(j);
                                    newADPset.add(j);
                                }
                            }
                        }
                    }
                    if (!newADPset.isEmpty())
                        adpSubNets.add(new SubNet(SubNetType.ADP, null, null, null, newADPset, null));
                }

                if (!adpSubNets.isEmpty()) {
                    //GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Decomposition can not be processed, because of the lack of invariants!", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Before determine ADT sets, you need to generate T-invariants.", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static boolean checkADT(ArrayList<Integer> t1, ArrayList<Integer> t2) {
        boolean result = true;
        for (int i = 0; i < t1.size(); i++) {
            if (!t1.get(i).equals(t2.get(i)))
                result = false;
        }

        return result;
    }

    public static void generateTZ() {
        //cleanSubnets();
        paths = calculatePaths();

        for (Path path : paths) {

            if (path.isCycle) {
                ArrayList<Path> list = new ArrayList<Path>();
                list.add(path);
                tzResultList.add(list);
            } else {

                ArrayList<Path> localListOfPaths = new ArrayList<>(paths);
                ArrayList<Path> listOfCycles = new ArrayList<>();

                localListOfPaths.remove(path);
                listOfCycles.add(path);
                for (Path p : localListOfPaths) {
                    if (path.endNode == p.startNode) {
                        dep(listOfCycles, localListOfPaths);
                    }
                }
            }
        }
        ArrayList<ArrayList<Path>> temp = new ArrayList<>();
        for (int i = 0; i < tzResultList.size(); i++) {
            boolean isDouble = false;
            for (int j = i + 1; j < tzResultList.size(); j++)
                if (tzResultList.get(i).containsAll(tzResultList.get(j))) {
                    isDouble = true;
                }

            addSinkToSubnets(tzResultList.get(i));

            if (!isDouble) {
                temp.add(tzResultList.get(i));
                tzSubNets.add(new SubNet(SubNetType.TZ, null, null, null, null, tzResultList.get(i)));
            }
        }

        tzResultList = temp;
        tzResultList.clear();

        if (!tzSubNets.isEmpty()) {
            //GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    public static void addSinkToSubnets(ArrayList<Path> paths) {
        ArrayList<Path> sinkSourcePaths = new ArrayList<>();
        for (Path path : paths) {

            for (Node n : path.path) {
                for (Node chceck : n.getOutNodes()) {
                    if (chceck.getOutNodes().isEmpty()) {
                        ArrayList<Node> list = new ArrayList<>();
                        list.add(n);
                        list.add(chceck);
                        Path sinkPath = new Path(n, chceck, list);
                        sinkSourcePaths.add(sinkPath);
                    }
                }

                for (Node chceck : n.getInNodes()) {
                    if (chceck.getInNodes().isEmpty()) {
                        ArrayList<Node> list = new ArrayList<>();
                        list.add(n);
                        list.add(chceck);
                        Path sinkPath = new Path(chceck, n, list);
                        sinkSourcePaths.add(sinkPath);
                    }
                }
            }
        }
        paths.addAll(sinkSourcePaths);
    }

    public static void generateCycle(boolean isOotsuki) {
        //cleanSubnets();
        paths = calculatePaths();

        for (Path path : paths) {
            if (path.isCycle) {
                ArrayList<Path> list = new ArrayList<Path>();
                list.add(path);
                tzResultList.add(list);
            } else {
                ArrayList<Path> localListOfPaths = new ArrayList<>(paths);
                ArrayList<Path> listOfCycles = new ArrayList<>();

                localListOfPaths.remove(path);
                listOfCycles.add(path);
                for (Path p : localListOfPaths) {
                    if (path.endNode == p.startNode) {
                        dep(listOfCycles, localListOfPaths);
                    }
                }
            }
        }

        ArrayList<ArrayList<Path>> temp = new ArrayList<>();
        for (int i = 0; i < tzResultList.size(); i++) {
            boolean isDouble = false;
            for (int j = i + 1; j < tzResultList.size(); j++)
                if (tzResultList.get(i).containsAll(tzResultList.get(j)))
                    isDouble = true;

            if (!isDouble) {
                temp.add(tzResultList.get(i));
                cycleSubNets.add(new SubNet(SubNetType.CYCLE, null, null, null, null, tzResultList.get(i)));
            }
        }

        tzResultList = temp;
        if(!isOotsuki)
            tzResultList.clear();

        if (!cycleSubNets.isEmpty()) {
//            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    public static void generateHou() {
        //cleanSubnets();
        paths = calculatePathsHou();

        ArrayList<Node> inNode = new ArrayList<>();
        ArrayList<Node> outNode = new ArrayList<>();

        for (Path p : paths) {
            if (p.startNode.getInNodes().size() == 0 && p.startNode.getOutNodes().size() != 0) {
                inNode.add(p.startNode);
            }

            if (p.endNode.getInNodes().size() != 0 && p.endNode.getOutNodes().size() == 0) {
                outNode.add(p.endNode);
            }
        }

        for (Path path : paths) {
            if (inNode.contains(path.startNode) && outNode.contains((path.endNode))) {
                ArrayList<Path> pl = new ArrayList<Path>();
                pl.add(path);
                houResultList.add(pl);
            } else {
                if (inNode.contains(path.startNode)) {
                    ArrayList<Path> localListOfPaths = new ArrayList<>(paths);
                    ArrayList<Path> listOfCycles = new ArrayList<>();

                    localListOfPaths.remove(path);
                    listOfCycles.add(path);
                    for (Path p : localListOfPaths) {
                        if (path.endNode == p.startNode) {
                            depHou(listOfCycles, localListOfPaths, outNode);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < houResultList.size(); i++) {
            boolean isDouble = false;
            for (int j = i + 1; j < houResultList.size(); j++) {
                if (houResultList.get(i).containsAll(houResultList.get(j))) {
                    isDouble = true;
                }
            }

            if (!isDouble) {
                houSubNets.add(new SubNet(SubNetType.HOU, null, null, null, null, houResultList.get(i)));
            }
        }

        //houResultList.clear();

        if (!houSubNets.isEmpty()) {
            //      GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    public static void generateNishi() {
        //cleanSubnets();
        paths = calculatePathsHou();

        ArrayList<Node> inNode = new ArrayList<>();
        ArrayList<Node> outNode = new ArrayList<>();

        for (Path p : paths) {
            if (p.startNode.getInNodes().size() == 0 && p.startNode.getOutNodes().size() != 0) {
                inNode.add(p.startNode);
            }

            if (p.endNode.getInNodes().size() != 0 && p.endNode.getOutNodes().size() == 0) {
                outNode.add(p.endNode);
            }
        }

        for (Path path : paths) {
            if (inNode.contains(path.startNode) && outNode.contains((path.endNode))) {
                ArrayList<Path> pl = new ArrayList<Path>();
                pl.add(path);
                nishiResultList.add(pl);
            } else {
                if (inNode.contains(path.startNode)) {
                    ArrayList<Path> localListOfPaths = new ArrayList<>(paths);
                    ArrayList<Path> listOfCycles = new ArrayList<>();

                    localListOfPaths.remove(path);
                    listOfCycles.add(path);
                    for (Path paralelPath : paths) {
                        if (!(paralelPath.startNode == path.startNode && paralelPath.endNode == path.endNode)) {
                            if (paralelPath.startNode.getInNodes().size() == 0 && paralelPath.endNode == path.endNode) {
                                localListOfPaths.remove(paralelPath);
                                listOfCycles.add(paralelPath);
                            }
                        }
                    }
                    for (Path p : localListOfPaths) {
                        if (path.endNode == p.startNode) {
                            depNishi(listOfCycles, localListOfPaths, outNode);
                        }
                    }
                }
            }
        }

        for (int i = 0; i < nishiResultList.size(); i++) {
            boolean isDouble = false;
            for (int j = i + 1; j < nishiResultList.size(); j++) {
                if (nishiResultList.get(i).containsAll(nishiResultList.get(j))) {
                    isDouble = true;
                }
            }

            if (!isDouble) {
                nishiSubNets.add(new SubNet(SubNetType.NISHI, null, null, null, null, nishiResultList.get(i)));
            }
        }

        nishiResultList.clear();

        if (!nishiSubNets.isEmpty()) {
            ///        GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    public static void generateOotsuki() {
        if (cycleSubNets == null || cycleSubNets.isEmpty()) {
            generateCycle(true);
        }
        if (houSubNets == null || houSubNets.isEmpty()) {
            generateHou();
        }

        //ootsukiSubNets.clear();

        ootsukiResultList.addAll(houResultList);
        ootsukiResultList.addAll(tzResultList);

        ArrayList<ArrayList<Path>> allFiringSeq = new ArrayList<>(ootsukiResultList);
        ArrayList<Path> fs = new ArrayList<>();

        while(!allFiringSeq.isEmpty())
        {
            //weź pierwszy element
            ArrayList<Path> root = allFiringSeq.get(0);
            allFiringSeq.remove(0);
            boolean repeat = true;

            //pętla tworząca strukture
            while (repeat)
            {
                repeat = false;
                ArrayList<ArrayList<Path>> groupsToAdd = new ArrayList<>();
                for(Path path : root)
                {
                    boolean isConnected = false;

                    for (ArrayList<Path> group : allFiringSeq ) {
                        isConnected=checkIfContains(group,path);
                        if(isConnected)
                            groupsToAdd.add(group);
                    }
                }

                if(!groupsToAdd.isEmpty())
                {
                    repeat=true;
                    for(ArrayList<Path> list :groupsToAdd) {
                        root.addAll(list);
                        allFiringSeq.remove(list);
                    }
                }
            }

            ootsukiSubNets.add(new SubNet(SubNetType.OOSTUKI, null, null, null, null, new ArrayList<>(root)));
            //dodaj nową sieć
        }
    }

    public static void generateSMC(){
        //dla každego inwariantu
        for (ArrayList<Integer> inv: invMatrixP) {
            int numberOfTokensInInv = 0;
            ArrayList<Place> invSupp = new ArrayList<Place>();
            //dla každego miejsca
            for (int i = 0 ; i<inv.size() ; i++) {
                if(inv.get(i)!=0) {
                    numberOfTokensInInv += allPlaces.get(i).getTokensNumber();
                    invSupp.add(allPlaces.get(i));
                }
            }

            if(numberOfTokensInInv==1){
                smcSubNets.add(new SubNet(SubNetType.SMC, null, invSupp, null, null, null));
            }
        }
    }


    private static boolean checkIfContains(ArrayList<Path> anotherPath, Path path, ArrayList<ArrayList<Path>> connected) {
        for (Path pa : anotherPath) {
            for (Node n1 : pa.path) {
                for (Node n2 : path.path) {
                    if (n1.getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                        if (n1.getID() == n2.getID())
                            return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean checkIfContains(ArrayList<Path> anotherPath, Path path) {
        for (Path pa : anotherPath) {
            for (Node n1 : pa.path) {
                for (Node n2 : path.path) {
                    if (n1.getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                        if (n1.getID() == n2.getID())
                            return true;
                    }
                }
            }
        }
        return false;
    }


    private static boolean depHou(ArrayList<Path> used, ArrayList<Path> unUsed, ArrayList<Node> outNodes) {
        //is cycle add
        if (outNodes.contains(used.get(used.size() - 1).endNode)) {
            houResultList.add(used);
            return true;
        }
        ArrayList<Path> possible = findPathsFrom(used.get(used.size() - 1).endNode, unUsed);
        for (Path pos : possible) {
            ArrayList<Path> NewUsed = new ArrayList<Path>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<Path>(unUsed);
            NewUsed.add(pos);
            NEWunUsed.remove(pos);
            depHou(NewUsed, NEWunUsed, outNodes);
        }

        return true;
    }

    private static boolean depNishi(ArrayList<Path> used, ArrayList<Path> unUsed, ArrayList<Node> outNodes) {
        //is cycle add
        if (outNodes.contains(used.get(used.size() - 1).endNode)) {
            for (Path paralelPath : paths) {
                if (paralelPath.endNode.getOutNodes().size() == 0 && paralelPath.startNode == paralelPath.startNode) { //to check
                    used.add(paralelPath);
                }
            }
            nishiResultList.add(used);
            return true;
        }
        ArrayList<Path> possible = findNishiPathsFrom(used.get(used.size() - 1).endNode, unUsed);
        for (Path pos : possible) {
            ArrayList<Path> NewUsed = new ArrayList<Path>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<Path>(unUsed);
            NewUsed.add(pos);
            NEWunUsed.remove(pos);

            for (Path parallelPath : paths) {
                if ((parallelPath.startNode == pos.startNode && parallelPath.endNode == pos.endNode) && !parallelPath.equals(pos)) {
                    NEWunUsed.remove(parallelPath);
                    NewUsed.add(parallelPath);
                }
            }
            depNishi(NewUsed, NEWunUsed, outNodes);
        }

        return true;
    }

    private static boolean dep(ArrayList<Path> used, ArrayList<Path> unUsed) {
        //is cycle add
        if (used.get(0).startNode == used.get(used.size() - 1).endNode) {
            tzResultList.add(used);
            return true;
        }
        //is not end
        for (int i = 1; i < used.size(); i++) {
            if (used.get(i).startNode == used.get(used.size() - 1).endNode)
                return false;
        }
        ArrayList<Path> posible = findPathsFrom(used.get(used.size() - 1).endNode, unUsed);
        for (Path pos : posible) {
            ArrayList<Path> NewUsed = new ArrayList<Path>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<Path>(unUsed);
            NewUsed.add(pos);
            NEWunUsed.remove(pos);
            dep(NewUsed, NEWunUsed);
        }

        return true;
    }

    private static ArrayList<Path> findPathsFrom(Node end, ArrayList<Path> list) {
        ArrayList<Path> possible = new ArrayList<>();
        for (Path p : list) {
            if (p.startNode == end)
                if(!p.isCycle)
                    possible.add(p);
        }
        return possible;
    }

    private static ArrayList<Path> findNishiPathsFrom(Node end, ArrayList<Path> list) {
        ArrayList<Path> possible = new ArrayList<>();
        for (Path p : list) {
            boolean duble = false;
            for (Path n : possible) {
                if (n.endNode == p.endNode && n.startNode == p.startNode)
                    duble = true;
            }
            if (p.startNode == end && !duble)
                possible.add(p);
        }
        return possible;
    }

    private static ArrayList<Node> calculatePath(Node m, ArrayList<Node> path) {
        if (path.contains(m)) {
            return path;
        }
        usedNodes.add(m);
        path.add(m);
        if (m.getOutNodes().size() > 0) {
            if (m.getOutNodes().size() == 1) {
                calculatePath(m.getOutNodes().get(0), path);
            }
        }
        return path;
    }

    private static ArrayList<Path> calculatePaths() {
        ArrayList<Path> listOfPaths = new ArrayList<>();
        for (Node n : allNodes) {
            if (n.getOutNodes().size() > 1 || n.getInNodes().size() == 0 || (n.getInNodes().size() > 1 && n.getOutNodes().size() != 0)) {

                if (n.getOutNodes().size() > 1) {
                    usedNodes.add(n);
                    for (Node m : n.getOutNodes()) {
                        ArrayList<Node> startPath = new ArrayList<Node>();
                        startPath.add(n);
                        ArrayList<Node> nodes = calculatePath(m, startPath);
                        if (nodes.get(nodes.size() - 1).getOutNodes().contains(nodes.get(0))) {
                            listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
                        } else {
                            listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                        }
                    }
                } else {
                    ArrayList<Node> nodes = calculatePath(n, new ArrayList<Node>());
                    listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                }
            }
        }

        for (Node n : allNodes) {
            if (!usedNodes.contains(n)) {
                ArrayList<Node> nodes = calculatePath(n, new ArrayList<Node>());
                listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
            }
        }
        return listOfPaths;
    }

    private static ArrayList<Path> calculatePathsHou() {
        ArrayList<Path> listOfPaths = new ArrayList<>();

        ArrayList<Node> listOfStartNodes = new ArrayList<>();
        for (Node n : allNodes) {
            if (!((n.getInNodes().size() == 1 && n.getOutNodes().size() == 1) || (n.getInNodes().size() > 0 && n.getOutNodes().size() == 0)))//(n.getOutNodes().size()>=1 || n.getInNodes().size()==0)
            {
                listOfStartNodes.add(n);
            }
        }

        for (Node n : listOfStartNodes) {
            ArrayList<Node> pathList = new ArrayList<Node>();
            pathList.add(n);
            for (Node singeOutNode : n.getOutNodes()) {
                pathList.add(singeOutNode);
                pathList = getDeeper(singeOutNode, pathList);

                if (!pathList.isEmpty()) {
                    listOfPaths.add(new Path(pathList.get(0), pathList.get(pathList.size() - 1), new ArrayList<>(pathList)));
                    pathList.clear();
                    pathList.add(n);
                }
            }
        }
        return listOfPaths;
    }

    private static ArrayList<Node> getDeeper(Node n, ArrayList<Node> list) {
        if (n.getOutNodes().size() > 1 || n.getInNodes().size() > 1) {
            return list;
        } else {
            //zabezpieczenie na źródłowe
            if (!n.getOutNodes().isEmpty()) {
                list.add(n.getOutNodes().get(0));
                list = getDeeper(n.getOutNodes().get(0), list);
            }
        }
        return list;
    }

/**
    UNUSED METHODS
 */

    private static ArrayList<Integer> getColumn(ArrayList<ArrayList<Integer>> im, int column) {
        ArrayList<Integer> newRow = new ArrayList<>();
        for (int j = 0; j < im.size(); j++) {
            newRow.add(im.get(j).get(column));
        }
        return newRow;
    }

    private static boolean isSourceOrSink(ArrayList<Integer> rowOrColumn) {
        boolean isMore = false;
        boolean isLess = false;
        for (int i = 0; i < rowOrColumn.size(); i++) {
            if (rowOrColumn.get(i) > 0)
                isMore = true;
            if (rowOrColumn.get(i) < 0)
                isLess = true;

        }
        return isMore != isLess;
    }

    private static class Path {
        public Node startNode;
        public Node endNode;
        public ArrayList<Node> path;
        public ArrayList<Node> innerpath;
        public boolean isCycle = false;

        public Path(Node s, Node e, ArrayList<Node> l) {
            startNode = s;
            endNode = e;
            path = new ArrayList<>(l);
            innerpath = l;
            innerpath.remove(s);
            innerpath.remove(e);
        }

        public Path(Node s, Node e, ArrayList<Node> l, boolean cycle) {
            startNode = s;
            endNode = e;
            path = new ArrayList<>(l);
            innerpath = l;
            innerpath.remove(s);
            innerpath.remove(e);
            isCycle = cycle;
        }
    }

    public static class SubNet {
        //Functional
        private ArrayList<Transition> subTransitions;
        private ArrayList<Place> subPlaces;
        private ArrayList<Place> subBorderPlaces;
        private ArrayList<Place> subInternalPlaces;
        ArrayList<Arc> subArcs;
        private boolean proper;

        //Snet
        private ArrayList<Transition> subBorderTransition;
        private ArrayList<Transition> subInternalTransition;
        //Universal
        int subNetID;

        SubNet(SubNetType snt, ArrayList<Transition> subTransitions, ArrayList<Place> subPlaces, ArrayList<Node> subNode, ArrayList<Integer> maxADTset, ArrayList<Path> subPath) {
            proper = true;

            switch (snt) {
                case ZAJCEV:
                    createTransirionBasedSubNet(subTransitions);
                    break;
                case SNET:
                    createPlaceBasedSubNet(subPlaces);
                    break;
                case TNET:
                    createTransirionBasedSubNet(subTransitions);
                    break;
                case ADT:
                    createTransirionBasedSubNet(getTransitionsForADT(maxADTset));
                    break;
                case ADP:
                    createPlaceBasedSubNet(getTransitionsForADP(maxADTset));
                    break;
                case TZ:
                    createPathBasedSubNet(subPath);
                    break;
                case HOU:
                    createPathBasedSubNet(subPath);
                    break;
                case NISHI:
                    createPathBasedSubNet(subPath);
                    break;
                case CYCLE:
                    createPathBasedSubNet(subPath);
                    break;
                case OOSTUKI:
                    createPathBasedSubNet(subPath);
                    break;
                case SMC:
                    createPlaceBasedSubNet(subPlaces);
                    break;
            }
        }

        private void createPathBasedSubNet(ArrayList<Path> pathList) {
            subTransitions = new ArrayList<>();
            subPlaces = new ArrayList<>();
            for (Path path : pathList
            ) {
                for (Node node : path.path) {
                    for (Transition t : allTransitions)
                        if (t.getID() == node.getID())
                            if (!subTransitions.contains(t))
                                subTransitions.add(t);
                    for (Place p : allPlaces)
                        if (p.getID() == node.getID())
                            if (!subPlaces.contains(p))
                                subPlaces.add(p);
                }
            }
            calculateInternalArcs(subPlaces, subTransitions);
        }

        private void calculateArcs(ArrayList<Place> subPlaces) {
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Place place : subPlaces) {
                for (Arc arc : place.getInArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
                for (Arc arc : place.getOutArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
            }
            this.subArcs = listOfAllArcs;
        }

        private void calculateInternalArcs(ArrayList<Place> subPlaces, ArrayList<Transition> subTransitions) {
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Place place : subPlaces) {
                for (Transition transition : subTransitions) {
                    for (Arc arc : place.getInArcs())
                        if (arc.getStartNode().getID() == transition.getID())
                            if (!listOfAllArcs.contains(arc))
                                listOfAllArcs.add(arc);
                    for (Arc arc : place.getOutArcs())
                        if (arc.getEndNode().getID() == transition.getID())
                            if (!listOfAllArcs.contains(arc))
                                listOfAllArcs.add(arc);
                }
            }
            this.subArcs = listOfAllArcs;
        }

        private ArrayList<Transition> getTransitionsForADT(ArrayList<Integer> maxADTset) {
            //ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
            ArrayList<Transition> transitionsForADT = new ArrayList<Transition>();
            for (Integer number : maxADTset) {
                transitionsForADT.add(allTransitions.get(number));
            }
            return transitionsForADT;
        }

        private ArrayList<Place> getTransitionsForADP(ArrayList<Integer> maxADPset) {
            //ArrayList<Place> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
            ArrayList<Place> transitionsForADT = new ArrayList<Place>();
            for (Integer number : maxADPset) {
                transitionsForADT.add(allPlaces.get(number));
            }
            return transitionsForADT;
        }

        private void createTransirionBasedSubNet(ArrayList<Transition> subTransitions) {
            this.subTransitions = subTransitions;
            this.subInternalPlaces = new ArrayList<>();
            this.subBorderPlaces = new ArrayList<>();

            //Zbiór wszystkich miejsc
            ArrayList<Place> listOfInOutPlaces = new ArrayList<>();
            for (Transition transition : this.subTransitions) {
                for (Place place : transition.getPostPlaces()) {
                    if (!listOfInOutPlaces.contains(place))
                        listOfInOutPlaces.add(place);
                }
                for (Place place : transition.getPrePlaces()) {
                    if (!listOfInOutPlaces.contains(place))
                        listOfInOutPlaces.add(place);
                }
            }
            this.subPlaces = listOfInOutPlaces;

            //Zbiór miejsc granicznych
            for (Place place : listOfInOutPlaces) {
                boolean border = false;
                for (Transition transition : place.getPostTransitions())
                    if (!this.subTransitions.contains(transition))
                        border = true;

                for (Transition transition : place.getPreTransitions())
                    if (!this.subTransitions.contains(transition))
                        border = true;

                if (border)
                    this.subBorderPlaces.add(place);
                else
                    this.subInternalPlaces.add(place);
            }

            //wylicz łuki
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Transition transition : subTransitions) {
                for (Arc arc : transition.getInArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
                for (Arc arc : transition.getOutArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
            }
            this.subArcs = listOfAllArcs;
        }

        private void createPlaceBasedSubNet(ArrayList<Place> subPlaces) {
            this.subPlaces = subPlaces;
            this.subInternalTransition = new ArrayList<>();
            this.subBorderTransition = new ArrayList<>();

            //Zbiór wszystkich miejsc
            ArrayList<Transition> listOfInOutTransition = new ArrayList<>();
            for (Place place : this.subPlaces) {
                for (Transition transition : place.getPostTransitions()) {
                    if (!listOfInOutTransition.contains(transition))
                        listOfInOutTransition.add(transition);
                }
                for (Transition transition : place.getPreTransitions()) {
                    if (!listOfInOutTransition.contains(transition))
                        listOfInOutTransition.add(transition);
                }
            }
            this.subTransitions = listOfInOutTransition;

            //Zbiór tranzycji granicznych
            for (Transition transition : listOfInOutTransition) {
                boolean border = false;
                for (Place place : transition.getPostPlaces())
                    if (!this.subPlaces.contains(place))
                        border = true;

                for (Place place : transition.getPrePlaces())
                    if (!this.subPlaces.contains(place))
                        border = true;

                if (border)
                    this.subBorderTransition.add(transition);
                else
                    this.subInternalTransition.add(transition);
            }

            //wylicz łuki
            calculateArcs(subPlaces);
        }

        public boolean isProper() {
            return proper;
        }

        public void setProper(boolean proper) {
            this.proper = proper;
        }

        public ArrayList<Transition> getSubBorderTransition() {
            return subBorderTransition;
        }

        public void setSubBorderTransition(ArrayList<Transition> subBorderTransition) {
            this.subBorderTransition = subBorderTransition;
        }

        public ArrayList<Transition> getSubInternalTransition() {
            return subInternalTransition;
        }

        public void setSubInternalTransition(ArrayList<Transition> subInternalTransition) {
            this.subInternalTransition = subInternalTransition;
        }

        public ArrayList<Transition> getSubTransitions() {
            return subTransitions;
        }

        public void setSubTransitions(ArrayList<Transition> subTransitions) {
            this.subTransitions = subTransitions;
        }

        public ArrayList<Node> getSubNode() {
            ArrayList<Node> listOfNodes = new ArrayList<>();
            listOfNodes.addAll(subTransitions);
            listOfNodes.addAll(subPlaces);

            return listOfNodes;
        }

        public ArrayList<Place> getSubPlaces() {
            return subPlaces;
        }

        public void setSubPlaces(ArrayList<Place> subPlaces) {
            this.subPlaces = subPlaces;
        }

        public ArrayList<Arc> getSubArcs() {
            return subArcs;
        }

        public void setSubArcs(ArrayList<Arc> subArcs) {
            this.subArcs = subArcs;
        }

        public int getSubNetID() {
            return subNetID;
        }

        public void setSubNetID(int subNetID) {
            this.subNetID = subNetID;
        }

        public ArrayList<Place> getSubBorderPlaces() {
            return subBorderPlaces;
        }

        public void setSubBorderPlaces(ArrayList<Place> subBorderPlaces) {
            this.subBorderPlaces = subBorderPlaces;
        }

        public ArrayList<Place> getSubInternalPlaces() {
            return subInternalPlaces;
        }

        public void setSubInternalPlaces(ArrayList<Place> subInternalPlaces) {
            this.subInternalPlaces = subInternalPlaces;
        }
    }
}
