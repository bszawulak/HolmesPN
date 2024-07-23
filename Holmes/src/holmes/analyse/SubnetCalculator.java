package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.windows.decompositions.HolmesBranchVerticesPrototype;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Klasa odpowiedzialna za dekompozycję PN do wybranych typów podsieci
 */
public class SubnetCalculator implements Serializable {
    private static LanguageManager lang = GUIManager.getLanguageManager();
    public enum SubNetType {ZAJCEV, SNET, TNET, ADT, ADTcomp, ADP, OOSTUKI, TZ, HOU, NISHI, CYCLE, NotTzCycles, SMC, MCT, TINV, PINV, BV, Export}

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
    public static ArrayList<SubNet> mctSubNets = new ArrayList<>();
    public static ArrayList<SubNet> tinvSubNets = new ArrayList<>();
    public static ArrayList<SubNet> pinvSubNets = new ArrayList<>();
    public static ArrayList<SubNet> bvSubNets = new ArrayList<>();
    public static ArrayList<SubNet> btSubNets = new ArrayList<>();
    public static ArrayList<SubNet> bpSubNets = new ArrayList<>();
    public static ArrayList<SubNet> notTzCyclesiSubNets = new ArrayList<>();
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
    private static ArrayList<Arc> allArcs;

    private static ArrayList<Integer> inN = new ArrayList<>();
    private static ArrayList<Integer> outN = new ArrayList<>();

    /**
     * Metoda odpowiedzialna za dekompozycję do podsieci funkcyjnych Zajcewa - nie mylić z sieciami funkcyjnymi
     */
    public static void compileElements() {
        allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        allPlaces = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        invMatrixT = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
        invMatrixP = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix();
        allNodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
        allArcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
        usedNodes = new ArrayList<>();
    }

    public static void compileTestElements(ArrayList<Transition> at, ArrayList<Place> ap, ArrayList<Node> an, ArrayList<ArrayList<Integer>> ati, ArrayList<ArrayList<Integer>> api) {
        allTransitions = at;
        allPlaces = ap;
        invMatrixT = ati;
        invMatrixP = api;
        allNodes = an;
        usedNodes = new ArrayList<>();
    }

    public static void generateFS() {
        while (!allTransitions.isEmpty()) {
            Transition firstTransition = allTransitions.get(0);
            ArrayList<Transition> temporaryList = new ArrayList<>();
            temporaryList.add(firstTransition);
            allTransitions.remove(firstTransition);
            temporaryList = findFunctionalTransition(temporaryList, allTransitions);
            functionalSubNets.add(new SubNet(SubNetType.ZAJCEV, temporaryList, null, null, null, null));

            //GraphletComparator gc = new GraphletComparator(600);
            //gc.compareNetdiv();
        }
    }

    private static ArrayList<Transition> findFunctionalTransition(ArrayList<Transition> temporaryList, ArrayList<Transition> allTransitionsT) {
        boolean found = false;
        for (Transition transition : allTransitionsT) {
            ArrayList<Transition> listToAdd = new ArrayList<>();
            if (chceckParalellInputOutPut(transition, temporaryList)) {
                listToAdd.add(transition);
                found = true;
            }
            temporaryList.addAll(listToAdd);
        }

        allTransitionsT.removeAll(temporaryList);
        if (found) {
            temporaryList = findFunctionalTransition(temporaryList, allTransitionsT);
        }
        return temporaryList;
    }

    private static boolean chceckParalellInputOutPut(Node newTran, ArrayList<Transition> sub) {
        boolean paralel = false;
        boolean seq = false;
        for (Transition subTran : sub) {
            boolean in = chceckNodeOutIn(newTran.getInputNodes(), subTran.getInputNodes());
            boolean out = chceckNodeOutIn(newTran.getOutputNodes(), subTran.getOutputNodes());
            boolean inout = chceckNodeOutIn(newTran.getInputNodes(), subTran.getOutputNodes());
            boolean outin = chceckNodeOutIn(newTran.getOutputNodes(), subTran.getInputNodes());

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
            boolean in = chceckNodeOutIn(newPlac.getInputNodes(), subPlace.getInputNodes());
            boolean out = chceckNodeOutIn(newPlac.getOutputNodes(), subPlace.getOutputNodes());
            boolean inout = chceckNodeOutIn(newPlac.getInputNodes(), subPlace.getOutputNodes());
            boolean outin = chceckNodeOutIn(newPlac.getOutputNodes(), subPlace.getInputNodes());

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
            boolean in = chceckNodeOutIn(newPlac.getInputNodes(), subPlace.getInputNodes());
            boolean out = chceckNodeOutIn(newPlac.getOutputNodes(), subPlace.getOutputNodes());
            boolean inout = chceckNodeOutIn(newPlac.getInputNodes(), subPlace.getOutputNodes());
            boolean outin = chceckNodeOutIn(newPlac.getOutputNodes(), subPlace.getInputNodes());

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
        tinvSubNets = new ArrayList<>();
        pinvSubNets = new ArrayList<>();
        bvSubNets = new ArrayList<>();
        mctSubNets = new ArrayList<>();
        btSubNets = new ArrayList<>();
        bpSubNets = new ArrayList<>();
        notTzCyclesiSubNets = new ArrayList<>();
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
                            for (Place ip : invSupport.get(i).getOutputPlaces()) {
                                for (Place jp : invSupport.get(j).getInputPlaces()) {
                                    if (ip.getID() == jp.getID() && !inv.contains(ip))
                                        invPlace.add(ip);
                                }
                            }

                            for (Place ip : invSupport.get(i).getInputPlaces()) {
                                for (Place jp : invSupport.get(j).getOutputPlaces()) {
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
            } else { //Decomposition can not be processed, because of the lack of invariants!
                JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else { //Before determine ADT sets, you need to generate T-invariants.
            JOptionPane.showMessageDialog(null, lang.getText("SCal_entry002"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void generateTnets() {

        HashMap<Place, Integer> placesMap = new HashMap<>();
        HashMap<Transition, Integer> transitionsMap = new HashMap<>();
        for (int i = 0; i < allPlaces.size(); i++) {
            placesMap.put(allPlaces.get(i), i);
        }
        for (int i = 0; i < allTransitions.size(); i++) {
            transitionsMap.put(allTransitions.get(i), i);
        }

        ArrayList<ArrayList<Integer>> globalIncidenceMatrix = new ArrayList<>();
        ArrayList<ArrayList<Integer>> globalIdentityMatrix = new ArrayList<>();
        ArrayList<ArrayList<Integer>> CMatrix = new ArrayList<>();
        ArrayList<ArrayList<Integer>> removalList = new ArrayList<>();
        ArrayList<ArrayList<Integer>> doubleArcs = new ArrayList<>();

        //tworzenie macierzy TP - precyzyjnie do obliczeń T-inwariantów
        for (int trans = 0; trans < allTransitions.size(); trans++) {
            ArrayList<Integer> transRow = new ArrayList<>();
            for (int place = 0; place < allPlaces.size(); place++) {
                transRow.add(0);
            }
            globalIncidenceMatrix.add(transRow);
            CMatrix.add(new ArrayList<>(transRow));
        }
        //wypełnianie macierzy incydencji
        //int disabledArcs = 0;
        for (Arc oneArc : allArcs) {
            int tPosition = 0;
            int pPosition = 0;
            int incidenceValue = 0;

            //if (oneArc.getArcType() != Arc.TypeOfArc.NORMAL && !(oneArc.getArcType() == Arc.TypeOfArc.READARC)) {
            //    continue;
            //}

            if (oneArc.getArcType() == Arc.TypeOfArc.NORMAL || oneArc.getArcType() == Arc.TypeOfArc.READARC) {
            } else {
                continue;
            }

            if (oneArc.getStartNode().isInvisible() || oneArc.getEndNode().isInvisible()) {
                //disabledArcs++;
                continue;
            }

            if (oneArc.getStartNode().getType() == PetriNetElement.PetriNetElementType.TRANSITION) {
                tPosition = transitionsMap.get(oneArc.getStartNode());
                pPosition = placesMap.get(oneArc.getEndNode());
                incidenceValue = 1 * oneArc.getWeight();
            } else { //miejsca
                tPosition = transitionsMap.get(oneArc.getEndNode());
                pPosition = placesMap.get(oneArc.getStartNode());
                incidenceValue = -1 * oneArc.getWeight();
            }


            int oldValue = globalIncidenceMatrix.get(tPosition).get(pPosition);
            if (oldValue != 0) { //detekcja łuków podwójnych
                ArrayList<Integer> hiddenReadArc = new ArrayList<>();
                hiddenReadArc.add(pPosition);
                hiddenReadArc.add(tPosition);
                doubleArcs.add(hiddenReadArc);
            }

            globalIncidenceMatrix.get(tPosition).set(pPosition, oldValue + incidenceValue); //TODO:... uwaga na to!
            CMatrix.get(tPosition).set(pPosition, oldValue + incidenceValue);
        }


        //////////

        for (int i = 0; i < allTransitions.size(); i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < allTransitions.size(); j++) {
                if (i == j) {
                    row.add(1);
                } else {
                    row.add(0);
                }
            }
            removalList.add(row);
        }

        int numberOfColumns = CMatrix.get(0).size();
        for (int c = 0; c < numberOfColumns; c++) {
            if (columnReductable(CMatrix, c)) {
                ArrayList<Integer> row1 = CMatrix.get(inN.get(0));
                ArrayList<Integer> row2 = CMatrix.get(outN.get(0));
                ArrayList<Integer> rowV = new ArrayList<>();


                ArrayList<Integer> rrow1 = removalList.get(inN.get(0));
                ArrayList<Integer> rrow2 = removalList.get(outN.get(0));
                ArrayList<Integer> rrowV = new ArrayList<>();

                CMatrix.remove(row1);
                CMatrix.remove(row2);
                removalList.remove(rrow1);
                removalList.remove(rrow2);


                for(int col = 0 ; col < allPlaces.size() ; col++)
                {
                    rowV.add(row1.get(col) + row2.get(col));
                }
                for(int col = 0 ; col < allTransitions.size() ; col++)
                {
                    rrowV.add(rrow1.get(col) + rrow2.get(col));
                }
                CMatrix.add(rowV);
                removalList.add(rrowV);
            }
        }

        for(int i = 0 ; i < CMatrix.size() ; i++) {
            for (int j = 0; j < CMatrix.get(i).size(); j++) {
                System.out.print(CMatrix.get(i).get(j));
            }
            System.out.println();
        }

        for (ArrayList<Integer> transitionVector : removalList ) {

            ArrayList<Integer> listOfPositions = new ArrayList<>();
            for (int i = 0 ; i < transitionVector.size() ; i++)
            {
                if(transitionVector.get(i)>0)
                    listOfPositions.add(i);
            }
            tnetSubNets.add(new SubNet(SubNetType.TNET, allTransitions, null, null, listOfPositions, null));
        }
    }

    private static boolean columnReductable(ArrayList<ArrayList<Integer>> cMatrix, int c) {
        inN = new ArrayList<>();
        outN = new ArrayList<>();

        for (int r = 0; r < cMatrix.size(); r++) {
            if (cMatrix.get(r).get(c) > 0) {
                outN.add(r);
            }
            if (cMatrix.get(r).get(c) < 0) {
                inN.add(r);
            }
        }
        return outN.size() == 1 && inN.size() == 1 && Math.abs(cMatrix.get(outN.get(0)).get(c)) == Math.abs(cMatrix.get(inN.get(0)).get(c));
    }

    private static ArrayList<Integer> getMaxSubConnectionTnet(ArrayList<Integer> newADTset) {
        ArrayList<ArrayList<Integer>> partialResults = new ArrayList<>();
        while (newADTset.size() > 0) {
            //Integer start = newADTset.get(0);
            //newADTset.remove(0);
            ArrayList<Integer> local = new ArrayList<>(newADTset);


            boolean newConnections = true;
            while (newConnections) {
                newConnections = false;
                for (Integer next : newADTset) {
                    if (!local.contains(next))
                        if (checkConnection(local, next, GUIManager.getDefaultGUIManager().getWorkspace().getProject())) {
                            local.add(next);
                            newConnections = true;
                        }
                }
            }
            partialResults.add(local);
            newADTset.removeAll(local);
        }

        int size = 0;
        int longest = -1;
        for (int i = 0; i < partialResults.size(); i++) {
            if (partialResults.get(i).size() > size) {
                longest = i;
                size = partialResults.get(i).size();
            }
        }

        return partialResults.get(longest);
    }

    public static void generateMCT() {
        ArrayList<ArrayList<Transition>> mctsets = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCTMatrix();
        if (mctsets != null && !mctsets.isEmpty()) {
            for (ArrayList<Transition> mct : mctsets) {
                mctSubNets.add(new SubNet(SubNetType.MCT, mct, null, null, null, null));
            }
            mctSubNets.remove(mctSubNets.size() - 1);
        } else { //Decomposition can not be processed, because of the lack of invariants!
            JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void generateTInv() {
        if (invMatrixT != null) {
            if (!invMatrixT.isEmpty()) {
                for (ArrayList<Integer> inv : invMatrixT) {
                    ArrayList<Transition> subTransitions = new ArrayList<>();
                    for (int i = 0; i < inv.size(); i++) {
                        if (inv.get(i) > 0)
                            subTransitions.add(allTransitions.get(i));
                    }
                    tinvSubNets.add(new SubNet(SubNetType.TINV, subTransitions, null, null, null, null));
                }
            } else { //Decomposition can not be processed, because of the lack of invariants!
                JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else { //Decomposition can not be processed, because of the lack of invariants!
            JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void generatePInv() {
        if (invMatrixT != null) {
            if (!invMatrixT.isEmpty()) {
                for (ArrayList<Integer> inv : invMatrixP) {
                    ArrayList<Place> subPlaces = new ArrayList<>();
                    for (int i = 0; i < inv.size(); i++) {
                        if (inv.get(i) > 0)
                            subPlaces.add(allPlaces.get(i));
                    }
                    pinvSubNets.add(new SubNet(SubNetType.PINV, null, subPlaces, null, null, null));
                }
            } else { 
                JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void generateADT() {
        //cleanSubnets();


        if (invMatrixT != null) {

            for (int i = 0; i < invMatrixT.size(); i++) {
                for (int j = 0; j < invMatrixT.get(i).size(); j++) {
                    System.out.print(" " + invMatrixT.get(i).get(j) + " ");
                }
                System.out.println();
            }


            if (!invMatrixT.isEmpty()) {
                //ArrayList<ArrayList<Integer>> invMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
                ArrayList<ArrayList<Integer>> nonAssignedRows = new ArrayList<>();

                for (int i = 0; i < invMatrixT.get(0).size(); i++) {
                    ArrayList<Integer> newRow = new ArrayList<>();
                    for (ArrayList<Integer> integers : invMatrixT) {
                        //newRow.add(integers.get(i));
                        if (integers.get(i) > 0)
                            newRow.add(1);
                        else
                            newRow.add(0);
                    }
                    nonAssignedRows.add(newRow);
                }

                ArrayList<Integer> listOfusedTransitions = new ArrayList<>();

                for (int i = 0; i < nonAssignedRows.size(); i++) {

                    ArrayList<Integer> localListOfusedTransitions = new ArrayList<>(listOfusedTransitions);
                    ArrayList<Integer> newADTset = new ArrayList<>();

                    if (!localListOfusedTransitions.contains(i)) {
                        newADTset.add(i);
                        localListOfusedTransitions.add(i);
                        for (int j = i; j < nonAssignedRows.size(); j++) {
                            if (checkADT(nonAssignedRows.get(i), nonAssignedRows.get(j))) {
                                if (!localListOfusedTransitions.contains(j)) {
                                    localListOfusedTransitions.add(j);
                                    newADTset.add(j);
                                }
                            }
                        }
                    }


                    if (!newADTset.isEmpty()) {
                        newADTset = getMaxSubConnection(newADTset, GUIManager.getDefaultGUIManager().getWorkspace().getProject());

                        listOfusedTransitions.addAll(newADTset);
                        List<Integer> UniqueNumbers = listOfusedTransitions.stream().distinct().collect(Collectors.toList());
                        listOfusedTransitions = (ArrayList<Integer>) UniqueNumbers;
                    }

                    /*
                    if(checkConnection(newADTset,j))
                                    {
                     */

                    if (!newADTset.isEmpty()) {
                        adtSubNets.add(new SubNet(SubNetType.ADTcomp, GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions(), null, null, newADTset, null));
                        listOfusedTransitions.addAll(listOfusedTransitions);
                    }
                }
            } else { //Decomposition can not be processed, because of the lack of invariants!
                JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else { //Before determine ADT sets, you need to generate T-invariants.
            JOptionPane.showMessageDialog(null, lang.getText("SCal_entry002"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static ArrayList<Integer> getMaxSubConnection(ArrayList<Integer> newADTset, PetriNet pn) {
        ArrayList<ArrayList<Integer>> partialResults = new ArrayList<>();
        while (newADTset.size() > 0) {
            //Integer start = newADTset.get(0);
            //newADTset.remove(0);
            //ArrayList<Integer> local = new ArrayList<>(newADTset);

            Integer start = newADTset.get(0);
            newADTset.remove(0);
            ArrayList<Integer> inicjator = new ArrayList<Integer>();
            inicjator.add(start);
            ArrayList<Integer> local = new ArrayList<>(inicjator);

            boolean newConnections = true;
            while (newConnections) {
                newConnections = false;
                for (Integer next : newADTset) {
                    if (!local.contains(next))
                        if (checkConnection(local, next, pn)) {
                            local.add(next);
                            newConnections = true;
                        }
                }
            }
            partialResults.add(local);
            newADTset.removeAll(local);
        }

        int size = 0;
        int longest = -1;
        for (int i = 0; i < partialResults.size(); i++) {
            if (partialResults.get(i).size() > size) {
                longest = i;
                size = partialResults.get(i).size();
            }
        }

        return partialResults.get(longest);
    }

    private static boolean checkConnection(ArrayList<Integer> newADTset, int j, PetriNet pn) {
        boolean connected = false;

        for (Integer i : newADTset) {
            ArrayList<Node> common = new ArrayList<Node>(pn.getTransitions().get(i).getNeighborsNodes());
            common.retainAll(new ArrayList<>(pn.getTransitions().get(j).getNeighborsNodes()));
            if (common.size() > 0) {
                connected = true;
            }

        }

        return connected;
    }

    public static ArrayList<SubNet> generateTnetFromSecondNet(PetriNet pn) {

        ArrayList<SubNet> result = new ArrayList<>();


        HashMap<Place, Integer> placesMap = new HashMap<>();
        HashMap<Transition, Integer> transitionsMap = new HashMap<>();
        for (int i = 0; i < pn.getPlaces().size(); i++) {
            placesMap.put(pn.getPlaces().get(i), i);
        }
        for (int i = 0; i < pn.getTransitions().size(); i++) {
            transitionsMap.put(pn.getTransitions().get(i), i);
        }

        ArrayList<ArrayList<Integer>> globalIncidenceMatrix = new ArrayList<>();
        ArrayList<ArrayList<Integer>> globalIdentityMatrix = new ArrayList<>();
        ArrayList<ArrayList<Integer>> CMatrix = new ArrayList<>();
        ArrayList<ArrayList<Integer>> removalList = new ArrayList<>();
        ArrayList<ArrayList<Integer>> doubleArcs = new ArrayList<>();

        //tworzenie macierzy TP - precyzyjnie do obliczeń T-inwariantów
        for (int trans = 0; trans < pn.getTransitions().size(); trans++) {
            ArrayList<Integer> transRow = new ArrayList<>();
            for (int place = 0; place < pn.getPlaces().size(); place++) {
                transRow.add(0);
            }
            globalIncidenceMatrix.add(transRow);
            CMatrix.add(new ArrayList<>(transRow));
        }
        //wypełnianie macierzy incydencji
        //int disabledArcs = 0;
        for (Arc oneArc : pn.getArcs()) {
            int tPosition = 0;
            int pPosition = 0;
            int incidenceValue = 0;

            //if (oneArc.getArcType() != Arc.TypeOfArc.NORMAL && !(oneArc.getArcType() == Arc.TypeOfArc.READARC)) {
            //    continue;
            //}

            if (oneArc.getArcType() == Arc.TypeOfArc.NORMAL || oneArc.getArcType() == Arc.TypeOfArc.READARC) {
            } else {
                continue;
            }

            if (oneArc.getStartNode().isInvisible() || oneArc.getEndNode().isInvisible()) {
                //disabledArcs++;
                continue;
            }

            if (oneArc.getStartNode().getType() == PetriNetElement.PetriNetElementType.TRANSITION) {
                tPosition = transitionsMap.get(oneArc.getStartNode());
                pPosition = placesMap.get(oneArc.getEndNode());
                incidenceValue = 1 * oneArc.getWeight();
            } else { //miejsca
                tPosition = transitionsMap.get(oneArc.getEndNode());
                pPosition = placesMap.get(oneArc.getStartNode());
                incidenceValue = -1 * oneArc.getWeight();
            }


            int oldValue = globalIncidenceMatrix.get(tPosition).get(pPosition);
            if (oldValue != 0) { //detekcja łuków podwójnych
                ArrayList<Integer> hiddenReadArc = new ArrayList<>();
                hiddenReadArc.add(pPosition);
                hiddenReadArc.add(tPosition);
                doubleArcs.add(hiddenReadArc);
            }

            globalIncidenceMatrix.get(tPosition).set(pPosition, oldValue + incidenceValue); //TODO:... uwaga na to!
            CMatrix.get(tPosition).set(pPosition, oldValue + incidenceValue);
        }


        //////////

        for (int i = 0; i < pn.getTransitions().size(); i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < pn.getTransitions().size(); j++) {
                if (i == j) {
                    row.add(1);
                } else {
                    row.add(0);
                }
            }
            removalList.add(row);
        }

        int numberOfColumns = CMatrix.get(0).size();
        for (int c = 0; c < numberOfColumns; c++) {
            if (columnReductable(CMatrix, c)) {
                ArrayList<Integer> row1 = CMatrix.get(inN.get(0));
                ArrayList<Integer> row2 = CMatrix.get(outN.get(0));
                ArrayList<Integer> rowV = new ArrayList<>();


                ArrayList<Integer> rrow1 = removalList.get(inN.get(0));
                ArrayList<Integer> rrow2 = removalList.get(outN.get(0));
                ArrayList<Integer> rrowV = new ArrayList<>();

                CMatrix.remove(row1);
                CMatrix.remove(row2);
                removalList.remove(rrow1);
                removalList.remove(rrow2);


                for(int col = 0 ; col < pn.getPlaces().size() ; col++)
                {
                    rowV.add(row1.get(col) + row2.get(col));
                }
                for(int col = 0 ; col < pn.getTransitions().size() ; col++)
                {
                    rrowV.add(rrow1.get(col) + rrow2.get(col));
                }
                CMatrix.add(rowV);
                removalList.add(rrowV);
            }
        }

        for(int i = 0 ; i < CMatrix.size() ; i++) {
            for (int j = 0; j < CMatrix.get(i).size(); j++) {
                System.out.print(CMatrix.get(i).get(j));
            }
            System.out.println();
        }

        for (ArrayList<Integer> transitionVector : removalList ) {

            ArrayList<Integer> listOfPositions = new ArrayList<>();
            for (int i = 0 ; i < transitionVector.size() ; i++)
            {
                if(transitionVector.get(i)>0)
                    listOfPositions.add(i);
            }
            result.add(new SubNet(SubNetType.TNET, pn.getTransitions(), null, null, listOfPositions, null));
        }

        return result;
    }

    public static ArrayList<SubNet> generateTcomponentFromSecondNet(PetriNet pn) {

        ArrayList<SubNet> result = new ArrayList<>();
        if (pn.getT_InvMatrix() != null) {
            if (!pn.getT_InvMatrix() .isEmpty()) {
                for (ArrayList<Integer> inv : pn.getT_InvMatrix()) {
                    ArrayList<Transition> subTransitions = new ArrayList<>();
                    for (int i = 0; i < inv.size(); i++) {
                        if (inv.get(i) > 0)
                            subTransitions.add(pn.getTransitions().get(i));
                    }
                    result.add(new SubNet(SubNetType.TINV, subTransitions, null, null, null, null));
                }
            } else {
                JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }

        return result;
    }

    public static ArrayList<SubNet> generateADTFromSecondNet(PetriNet pn) {
        //TODO
        // TO NIE JEST ADT CZT

        //cleanSubnets();
        ArrayList<SubNet> result = new ArrayList<>();

        if (pn.getT_InvMatrix() != null) {
            if (!pn.getT_InvMatrix().isEmpty()) {
                //ArrayList<ArrayList<Integer>> invMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
                ArrayList<ArrayList<Integer>> nonAssignedRows = new ArrayList<>();

                for (int i = 0; i < pn.getT_InvMatrix().get(0).size(); i++) {
                    ArrayList<Integer> newRow = new ArrayList<>();
                    for (ArrayList<Integer> integers : pn.getT_InvMatrix()) {
                        //newRow.add(integers.get(i));
                        if (integers.get(i) > 0)
                            newRow.add(1);
                        else
                            newRow.add(0);
                    }
                    nonAssignedRows.add(newRow);
                }

                ArrayList<Integer> listOfusedTransitions = new ArrayList<>();

                for (int i = 0; i < nonAssignedRows.size(); i++) {
                    ArrayList<Integer> localListOfusedTransitions = new ArrayList<>(listOfusedTransitions);
                    ArrayList<Integer> newADTset = new ArrayList<>();

                    if (!localListOfusedTransitions.contains(i)) {
                        newADTset.add(i);
                        localListOfusedTransitions.add(i);
                        for (int j = i; j < nonAssignedRows.size(); j++) {
                            if (checkADT(nonAssignedRows.get(i), nonAssignedRows.get(j))) {
                                if (!localListOfusedTransitions.contains(j)) {
                                    localListOfusedTransitions.add(j);
                                    newADTset.add(j);
                                }
                            }
                        }
                    }
                    //if (!newADTset.isEmpty())
                    //    result.add(new SubNet(SubNetType.ADTcomp, pn.getTransitions(), null, null, newADTset, null));

                    if (!newADTset.isEmpty()) {
                        newADTset = getMaxSubConnection(newADTset, pn);

                        listOfusedTransitions.addAll(newADTset);
                        List<Integer> UniqueNumbers = listOfusedTransitions.stream().distinct().collect(Collectors.toList());
                        listOfusedTransitions = (ArrayList<Integer>) UniqueNumbers;
                    }


                    if (!newADTset.isEmpty()) {
                        result.add(new SubNet(SubNetType.ADTcomp, pn.getTransitions(), null, null, newADTset, null));
                        listOfusedTransitions.addAll(listOfusedTransitions);
                    }
                }
            } else { //Decomposition can not be processed, because of the lack of invariants!
                JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else { //Before determine ADT sets, you need to generate T-invariants.
            JOptionPane.showMessageDialog(null, lang.getText("SCal_entry002"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
        return result;
    }

    public static ArrayList<SubNet> generateFunctionalFromSecondNet(PetriNet pn) {
        ArrayList<Transition> transitionsFromSecondNet = new ArrayList<>(pn.getTransitions());
        ArrayList<SubNet> result = new ArrayList<>();
        while (!transitionsFromSecondNet.isEmpty()) {
            Transition firstTransition = transitionsFromSecondNet.get(0);
            ArrayList<Transition> temporaryList = new ArrayList<>();
            temporaryList.add(firstTransition);
            transitionsFromSecondNet.remove(firstTransition);
            temporaryList = findFunctionalTransition(temporaryList, transitionsFromSecondNet);
            result.add(new SubNet(SubNetType.ZAJCEV, temporaryList, null, null, null, null));

            //GraphletComparator gc = new GraphletComparator(600);
            //gc.compareNetdiv();
        }

        return result;
    }


    public static void generateADP() {
        //cleanSubnets();

        if (GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix() != null) {
            if (!GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix().isEmpty()) {
                ArrayList<ArrayList<Integer>> invMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getP_InvMatrix();
                ArrayList<ArrayList<Integer>> nonAssignedRows = new ArrayList<>();

                for (int i = 0; i < invMatrix.get(0).size(); i++) {
                    ArrayList<Integer> newRow = new ArrayList<>();
                    for (ArrayList<Integer> matrix : invMatrix) {
                        newRow.add(matrix.get(i));
                    }
                    nonAssignedRows.add(newRow);
                }

                ArrayList<Integer> listOfusedTransitions = new ArrayList<>();

                for (int i = 0; i < nonAssignedRows.size(); i++) {
                    ArrayList<Integer> newADPset = new ArrayList<>();

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
            } else { //Decomposition can not be processed, because of the lack of invariants!
                JOptionPane.showMessageDialog(null, lang.getText("SCal_entry001"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else { //Before determine ADT sets, you need to generate T-invariants.
            JOptionPane.showMessageDialog(null, lang.getText("SC_entry002"), "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static boolean checkADT(ArrayList<Integer> t1, ArrayList<Integer> t2) {
        boolean result = true;
        for (int i = 0; i < t1.size(); i++) {
            if (!t1.get(i).equals(t2.get(i))) {
                result = false;
                break;
            }
        }

        return result;
    }

    public static void generateTZ() {
        //cleanSubnets();
        paths = calculatePaths();

        for (Path path : paths) {

            if (path.isCycle) {
                ArrayList<Path> list = new ArrayList<>();
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
    }

    public static void generateNotTzCycles() {
        //cleanSubnets();
        paths = calculatePaths();

        for (Path path : paths) {

            if (path.isCycle) {
                ArrayList<Path> list = new ArrayList<>();
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
            boolean isSink = checkSinkToSubnets(tzResultList.get(i));

            if (!isDouble && !isSink) {
                temp.add(tzResultList.get(i));
                notTzCyclesiSubNets.add(new SubNet(SubNetType.NotTzCycles, null, null, null, null, tzResultList.get(i)));
            }
        }

        tzResultList = temp;
        tzResultList.clear();
    }


    public static void addSinkToSubnets(ArrayList<Path> paths) {
        ArrayList<Path> sinkSourcePaths = new ArrayList<>();
        for (Path path : paths) {

            for (Node n : path.path) {
                for (Node chceck : n.getOutputNodes()) {
                    if (chceck.getOutputNodes().isEmpty()) {
                        ArrayList<Node> list = new ArrayList<>();
                        list.add(n);
                        list.add(chceck);
                        Path sinkPath = new Path(n, chceck, list);
                        sinkSourcePaths.add(sinkPath);
                    }
                }

                for (Node chceck : n.getInputNodes()) {
                    if (chceck.getInputNodes().isEmpty()) {
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

    public static boolean checkSinkToSubnets(ArrayList<Path> paths) {
        ArrayList<Path> sinkSourcePaths = new ArrayList<>();

        boolean isSink = false;

        for (Path path : paths) {

            for (Node n : path.path) {
                for (Node chceck : n.getOutputNodes()) {
                    if (chceck.getOutputNodes().isEmpty()) {
                        ArrayList<Node> list = new ArrayList<>();
                        list.add(n);
                        list.add(chceck);
                        Path sinkPath = new Path(n, chceck, list);
                        sinkSourcePaths.add(sinkPath);
                        isSink = true;
                    }
                }

                for (Node chceck : n.getInputNodes()) {
                    if (chceck.getInputNodes().isEmpty()) {
                        ArrayList<Node> list = new ArrayList<>();
                        list.add(n);
                        list.add(chceck);
                        Path sinkPath = new Path(chceck, n, list);
                        sinkSourcePaths.add(sinkPath);
                        isSink = true;
                    }
                }
            }
        }
        return isSink;
    }

    public static void generateCycle(boolean isOotsuki) {
        //cleanSubnets();
        paths = calculatePaths();

        for (Path path : paths) {
            if (path.isCycle) {
                ArrayList<Path> list = new ArrayList<>();
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
        if (!isOotsuki)
            tzResultList.clear();
    }

    public static void generateHou() {
        //cleanSubnets();
        paths = calculatePathsHou();

        ArrayList<Node> inNode = new ArrayList<>();
        ArrayList<Node> outNode = new ArrayList<>();

        for (Path p : paths) {
            if (p.startNode.getInputNodes().isEmpty() && !p.startNode.getOutputNodes().isEmpty()) {
                inNode.add(p.startNode);
            }

            if (!p.endNode.getInputNodes().isEmpty() && p.endNode.getOutputNodes().isEmpty()) {
                outNode.add(p.endNode);
            }
        }

        for (Path path : paths) {
            if (inNode.contains(path.startNode) && outNode.contains((path.endNode))) {
                ArrayList<Path> pl = new ArrayList<>();
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
    }

    public static void generateNishi() {
        //cleanSubnets();
        paths = calculatePathsHou();

        ArrayList<Node> inNode = new ArrayList<>();
        ArrayList<Node> outNode = new ArrayList<>();

        for (Path p : paths) {
            if (p.startNode.getInputNodes().isEmpty() && !p.startNode.getOutputNodes().isEmpty()) {
                inNode.add(p.startNode);
            }

            if (!p.endNode.getInputNodes().isEmpty() && p.endNode.getOutputNodes().isEmpty()) {
                outNode.add(p.endNode);
            }
        }

        for (Path path : paths) {
            if (inNode.contains(path.startNode) && outNode.contains((path.endNode))) {
                ArrayList<Path> pl = new ArrayList<>();
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
                            if (paralelPath.startNode.getInputNodes().isEmpty() && paralelPath.endNode == path.endNode) {
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

        while (!allFiringSeq.isEmpty()) {
            //weź pierwszy element
            ArrayList<Path> root = allFiringSeq.get(0);
            allFiringSeq.remove(0);
            boolean repeat = true;

            //pętla tworząca strukture
            while (repeat) {
                repeat = false;
                ArrayList<ArrayList<Path>> groupsToAdd = new ArrayList<>();
                for (Path path : root) {
                    boolean isConnected = false;

                    for (ArrayList<Path> group : allFiringSeq) {
                        isConnected = checkIfContains(group, path);
                        if (isConnected)
                            groupsToAdd.add(group);
                    }
                }

                if (!groupsToAdd.isEmpty()) {
                    repeat = true;
                    for (ArrayList<Path> list : groupsToAdd) {
                        root.addAll(list);
                        allFiringSeq.remove(list);
                    }
                }
            }

            ootsukiSubNets.add(new SubNet(SubNetType.OOSTUKI, null, null, null, null, new ArrayList<>(root)));
            //dodaj nową sieć
        }
    }

    public static void generateSMC() {
        //dla každego inwariantu
        for (ArrayList<Integer> inv : invMatrixP) {
            int numberOfTokensInInv = 0;
            ArrayList<Place> invSupp = new ArrayList<>();
            //dla každego miejsca
            for (int i = 0; i < inv.size(); i++) {
                if (inv.get(i) != 0) {
                    numberOfTokensInInv += allPlaces.get(i).getTokensNumber();
                    invSupp.add(allPlaces.get(i));
                }
            }

            if (numberOfTokensInInv == 1) {
                smcSubNets.add(new SubNet(SubNetType.SMC, null, invSupp, null, null, null));
            }
        }
    }

    public static void generateBranchesVerticles() {

        for (Node n : allNodes) {
            if ((n.getOutputNodes().size() > 1 || n.getInputNodes().size() > 1)) {
                HolmesBranchVerticesPrototype.BranchStructure bs = new HolmesBranchVerticesPrototype.BranchStructure(n);
                bvSubNets.add(new SubNet(SubNetType.BV, null, null, null, null, bs.paths));
            }
        }
    }

    public static void generateBranchesTransitions() {

        for (Node n : allTransitions) {
            if ((n.getOutputNodes().size() > 1 || n.getInputNodes().size() > 1)) {
                HolmesBranchVerticesPrototype.BranchStructure bs = new HolmesBranchVerticesPrototype.BranchStructure(n);
                btSubNets.add(new SubNet(SubNetType.BV, null, null, null, null, bs.paths));
            }
        }
    }

    public static void generateBranchesPlaces() {

        for (Node n : allPlaces) {
            if ((n.getOutputNodes().size() > 1 || n.getInputNodes().size() > 1)) {
                HolmesBranchVerticesPrototype.BranchStructure bs = new HolmesBranchVerticesPrototype.BranchStructure(n);
                bpSubNets.add(new SubNet(SubNetType.BV, null, null, null, null, bs.paths));
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
            ArrayList<Path> NewUsed = new ArrayList<>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<>(unUsed);
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
                if (paralelPath.endNode.getOutputNodes().isEmpty()) {// && paralelPath.startNode == paralelPath.startNode) { //to check
                    used.add(paralelPath);
                }
            }
            nishiResultList.add(used);
            return true;
        }
        ArrayList<Path> possible = findNishiPathsFrom(used.get(used.size() - 1).endNode, unUsed);
        for (Path pos : possible) {
            ArrayList<Path> NewUsed = new ArrayList<>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<>(unUsed);
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
            ArrayList<Path> NewUsed = new ArrayList<>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<>(unUsed);
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
                if (!p.isCycle)
                    possible.add(p);
        }
        return possible;
    }

    private static ArrayList<Path> findNishiPathsFrom(Node end, ArrayList<Path> list) {
        ArrayList<Path> possible = new ArrayList<>();
        for (Path p : list) {
            boolean duble = false;
            for (Path n : possible) {
                if (n.endNode == p.endNode && n.startNode == p.startNode) {
                    duble = true;
                    break;
                }
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
        if (!m.getOutputNodes().isEmpty()) {
            if (m.getOutputNodes().size() == 1) {
                calculatePath(m.getOutputNodes().get(0), path);
            }
        }
        return path;
    }

    private static ArrayList<Path> calculatePaths() {
        ArrayList<Path> listOfPaths = new ArrayList<>();
        for (Node n : allNodes) {
            if (n.getOutputNodes().size() > 1 || n.getInputNodes().isEmpty() || (n.getInputNodes().size() > 1 && !n.getOutputNodes().isEmpty())) {

                if (n.getOutputNodes().size() > 1) {
                    usedNodes.add(n);
                    for (Node m : n.getOutputNodes()) {
                        ArrayList<Node> startPath = new ArrayList<>();
                        startPath.add(n);
                        ArrayList<Node> nodes = calculatePath(m, startPath);
                        if (nodes.get(nodes.size() - 1).getOutputNodes().contains(nodes.get(0))) {
                            listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
                        } else {
                            listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                        }
                    }
                } else {
                    ArrayList<Node> nodes = calculatePath(n, new ArrayList<>());
                    listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes)));
                }
            }
        }

        for (Node n : allNodes) {
            if (!usedNodes.contains(n)) {
                ArrayList<Node> nodes = calculatePath(n, new ArrayList<>());
                listOfPaths.add(new Path(nodes.get(0), nodes.get(nodes.size() - 1), new ArrayList<>(nodes), true));
            }
        }
        return listOfPaths;
    }

    private static ArrayList<Path> calculatePathsHou() {
        ArrayList<Path> listOfPaths = new ArrayList<>();

        ArrayList<Node> listOfStartNodes = new ArrayList<>();
        for (Node n : allNodes) {
            if (!((n.getInputNodes().size() == 1 && n.getOutputNodes().size() == 1)
                    || (!n.getInputNodes().isEmpty() && n.getOutputNodes().isEmpty())))//(n.getOutNodes().size()>=1 || n.getInNodes().size()==0)
            {
                listOfStartNodes.add(n);
            }
        }

        for (Node n : listOfStartNodes) {
            ArrayList<Node> pathList = new ArrayList<>();
            pathList.add(n);
            for (Node singeOutNode : n.getOutputNodes()) {
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
        if (n.getOutputNodes().size() > 1 || n.getInputNodes().size() > 1) {
            return list;
        } else {
            //zabezpieczenie na źródłowe
            if (!n.getOutputNodes().isEmpty()) {
                list.add(n.getOutputNodes().get(0));
                list = getDeeper(n.getOutputNodes().get(0), list);
            }
        }
        return list;
    }

    /**
     * UNUSED METHODS
     */

    private static ArrayList<Integer> getColumn(ArrayList<ArrayList<Integer>> im, int column) {
        ArrayList<Integer> newRow = new ArrayList<>();
        for (ArrayList<Integer> integers : im) {
            newRow.add(integers.get(column));
        }
        return newRow;
    }

    private static boolean isSourceOrSink(ArrayList<Integer> rowOrColumn) {
        boolean isMore = false;
        boolean isLess = false;
        for (Integer integer : rowOrColumn) {
            if (integer > 0)
                isMore = true;
            if (integer < 0)
                isLess = true;

        }
        return isMore != isLess;
    }

    public static class Path implements Serializable {
        public Node startNode;
        public Node endNode;
        public ArrayList<Node> path;
        public ArrayList<Node> innerpath;
        public boolean isCycle = false;
        public boolean isRevers = false;
        //public boolean isCOnservarive = false;

        public Path(Node s, Node e, ArrayList<Node> l) {
            startNode = s;
            endNode = e;
            path = new ArrayList<>(l);
            innerpath = new ArrayList<>(l);
            innerpath.remove(s);
            innerpath.remove(e);
        }

        public Path(Node s, Node e, ArrayList<Node> l, boolean cycle) {
            startNode = s;
            endNode = e;
            path = new ArrayList<>(l);
            innerpath = new ArrayList<>(l);
            innerpath.remove(s);
            innerpath.remove(e);
            isCycle = cycle;
        }

        public Path(Node s, Node e, ArrayList<Node> l, boolean cycle, boolean revers) {
            startNode = s;
            endNode = e;
            path = new ArrayList<>(l);
            innerpath = new ArrayList<>(l);
            innerpath.remove(s);
            innerpath.remove(e);
            isCycle = cycle;
            isRevers = revers;
        }

        private boolean checkConservativnes() {
            for (int i = 0; i < path.size(); i++) {
                //if(paths.get(i).)
            }

            return true;
        }
    }

    public static class SubNet implements Serializable {
        //Functional
        private ArrayList<Transition> subTransitions;
        private ArrayList<Place> subPlaces;
        private ArrayList<Place> subBorderPlaces;
        private ArrayList<Place> subInternalPlaces;
        ArrayList<Arc> subArcs;
        private boolean proper;
        public HashMap<Integer, Node> orbitMap = new HashMap<Integer, Node>();
        //Snet
        private ArrayList<Transition> subBorderTransition;
        private ArrayList<Transition> subInternalTransition;
        //Universal
        int subNetID;

        public SubNet() {

        }

        public SubNet(SubNetType snt, ArrayList<Transition> subTransitions, ArrayList<Place> subPlaces, ArrayList<Node> subNode, ArrayList<Integer> maxADTset, ArrayList<Path> subPath) {
            proper = true;

            switch (snt) {
                case ZAJCEV:
                    createTransirionBasedSubNet(subTransitions);
                    break;
                case SNET:
                    createPlaceBasedSubNet(subPlaces);
                    break;
                case TNET:
                    createTransirionBasedSubNet(getTransitionsForADT(maxADTset, subTransitions));
                    break;
                case ADT:
                    createTransirionBasedSubNet(getTransitionsForADT(maxADTset));
                    break;
                case ADTcomp:
                    createTransirionBasedSubNet(getTransitionsForADT(maxADTset, subTransitions));
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
                    createPathOotsukiBasedSubNet(subPath);
                    break;
                case SMC:
                    createPlaceBasedSubNet(subPlaces);
                    break;
                case MCT:
                    createTransirionBasedSubNet(subTransitions);
                    break;
                case TINV:
                    createTransirionBasedSubNet(subTransitions);
                    break;
                case PINV:
                    createPlaceBasedSubNet(subPlaces);
                    break;
                case BV:
                    createBranchBasedSubNet(subPath);
                    break;
                case NotTzCycles:
                    createPathBasedSubNet(subPath);
                    break;
                case Export:
                    createExportSubnet(subNode);
                    break;
            }
        }

        private void createExportSubnet(ArrayList<Node> subNode) {
            subTransitions = new ArrayList<>();
            subPlaces = new ArrayList<>();
            subArcs = new ArrayList<>();

            for (Node n : subNode) {
                if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                    subTransitions.add((Transition) n);
                } else {
                    subPlaces.add((Place) n);
                }
            }

            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Transition transition : subTransitions) {
                for (Arc arc : transition.getInputArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
                for (Arc arc : transition.getOutputArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
            }
            this.subArcs = listOfAllArcs;

        }

        public SubNet(ArrayList<Arc> al) {
            subTransitions = new ArrayList<>();
            subPlaces = new ArrayList<>();
            subArcs = al;

            for (Arc a : al) {
                if (a.getStartNode().getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                    if (!subTransitions.contains(a.getStartNode()))
                        subTransitions.add((Transition) a.getStartNode());
                } else {
                    if (!subPlaces.contains(a.getStartNode()))
                        subPlaces.add((Place) a.getStartNode());
                }

                if (a.getEndNode().getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                    if (!subTransitions.contains(a.getEndNode()))
                        subTransitions.add((Transition) a.getEndNode());
                } else {
                    if (!subPlaces.contains(a.getEndNode()))
                        subPlaces.add((Place) a.getEndNode());
                }
            }
        }

        public SubNet(ArrayList<Arc> al, boolean mock) {
            subTransitions = new ArrayList<>();
            subPlaces = new ArrayList<>();
            subArcs = al;

            for (Arc a : al) {
                if (a.getStartNode().getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                    if (!subTransitions.contains(a.getStartNode()))
                        subTransitions.add((Transition) a.getStartNode());
                } else {
                    if (!subPlaces.contains(a.getStartNode()))
                        subPlaces.add((Place) a.getStartNode());
                }

                if (a.getEndNode().getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                    if (!subTransitions.contains(a.getEndNode()))
                        subTransitions.add((Transition) a.getEndNode());
                } else {
                    if (!subPlaces.contains(a.getEndNode()))
                        subPlaces.add((Place) a.getEndNode());
                }
            }


        }

        private void createBranchBasedSubNet(ArrayList<Path> pathList) {
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
            calculateBranchInternalArcs(subPlaces, subTransitions, pathList);
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
            calculateInternalArcs(subPlaces, subTransitions, pathList);
        }

        private void calculateArcs(ArrayList<Place> subPlaces) {
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Place place : subPlaces) {
                for (Arc arc : place.getInputArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
                for (Arc arc : place.getOutputArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
            }
            this.subArcs = listOfAllArcs;
        }

        private void createPathOotsukiBasedSubNet(ArrayList<Path> pathList) {

            ArrayList<Place> localSubPlaces = new ArrayList<>();
            for (Path path : pathList
            ) {
                for (Node node : path.path) {
                    for (Place p : allPlaces)
                        if (p.getID() == node.getID())
                            if (!localSubPlaces.contains(p))
                                localSubPlaces.add(p);
                }
            }

            createPlaceBasedSubNet(localSubPlaces);
        }

        private void calculateInternalArcs(ArrayList<Place> subPlaces, ArrayList<Transition> subTransitions, ArrayList<Path> pathList) {
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Path path : pathList) {
                for (int i = 0; i < path.path.size() - 1; i++) {
                    Node startNode = path.path.get(i);
                    Node endNode = path.path.get(i + 1);
                    for (Arc arc : startNode.getOutputArcs()) {
                        if (arc.getEndNode().getID() == endNode.getID())
                            listOfAllArcs.add(arc);
                    }
                }

                for (Arc arc : path.endNode.getOutputArcs()) {
                    if (arc.getEndNode().getID() == path.startNode.getID())
                        listOfAllArcs.add(arc);
                }
            }



            /* OLD VAIRANT
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
            */
            this.subArcs = listOfAllArcs;
        }

        private void calculateBranchInternalArcs(ArrayList<Place> subPlaces, ArrayList<Transition> subTransitions, ArrayList<Path> pathList) {
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Path path : pathList) {
                for (int i = 0; i < path.path.size() - 1; i++) {
                    Node startNode = path.path.get(i);
                    Node endNode = path.path.get(i + 1);
                    for (Arc arc : startNode.getOutputArcs()) {
                        if (arc.getEndNode().getID() == endNode.getID())
                            listOfAllArcs.add(arc);
                    }
                    for (Arc arc : startNode.getInputArcs()) {
                        if (arc.getStartNode().getID() == endNode.getID())
                            listOfAllArcs.add(arc);
                    }
                }

                for (Arc arc : path.endNode.getOutputArcs()) {
                    if (arc.getEndNode().getID() == path.startNode.getID())
                        listOfAllArcs.add(arc);
                }
            }
            this.subArcs = listOfAllArcs;
        }

        private ArrayList<Transition> getTransitionsForADT(ArrayList<Integer> maxADTset, ArrayList<Transition> transitions) {
            //ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
            ArrayList<Transition> transitionsForADT = new ArrayList<>();
            for (Integer number : maxADTset) {
                transitionsForADT.add(transitions.get(number));
            }
            return transitionsForADT;
        }

        private ArrayList<Transition> getTransitionsForADT(ArrayList<Integer> maxADTset) {
            //ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
            ArrayList<Transition> transitionsForADT = new ArrayList<>();
            for (Integer number : maxADTset) {
                transitionsForADT.add(allTransitions.get(number));
            }
            return transitionsForADT;
        }

        private ArrayList<Place> getTransitionsForADP(ArrayList<Integer> maxADPset) {
            //ArrayList<Place> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
            ArrayList<Place> transitionsForADT = new ArrayList<>();
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
                for (Place place : transition.getOutputPlaces()) {
                    if (!listOfInOutPlaces.contains(place))
                        listOfInOutPlaces.add(place);
                }
                for (Place place : transition.getInputPlaces()) {
                    if (!listOfInOutPlaces.contains(place))
                        listOfInOutPlaces.add(place);
                }
            }
            this.subPlaces = listOfInOutPlaces;

            //Zbiór miejsc granicznych
            for (Place place : listOfInOutPlaces) {
                boolean border = false;
                for (Transition transition : place.getOutputTransitions())
                    if (!this.subTransitions.contains(transition)) {
                        border = true;
                        break;
                    }

                for (Transition transition : place.getInputTransitions())
                    if (!this.subTransitions.contains(transition)) {
                        border = true;
                        break;
                    }

                if (border)
                    this.subBorderPlaces.add(place);
                else
                    this.subInternalPlaces.add(place);
            }

            //wylicz łuki
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Transition transition : subTransitions) {
                for (Arc arc : transition.getInputArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
                for (Arc arc : transition.getOutputArcs())
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
                for (Transition transition : place.getOutputTransitions()) {
                    if (!listOfInOutTransition.contains(transition))
                        listOfInOutTransition.add(transition);
                }
                for (Transition transition : place.getInputTransitions()) {
                    if (!listOfInOutTransition.contains(transition))
                        listOfInOutTransition.add(transition);
                }
            }
            this.subTransitions = listOfInOutTransition;

            //Zbiór tranzycji granicznych
            for (Transition transition : listOfInOutTransition) {
                boolean border = false;
                for (Place place : transition.getOutputPlaces())
                    if (!this.subPlaces.contains(place)) {
                        border = true;
                        break;
                    }

                for (Place place : transition.getInputPlaces())
                    if (!this.subPlaces.contains(place)) {
                        border = true;
                        break;
                    }

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

        public void addTransitions(ArrayList<Transition> tl) {
            subTransitions.addAll(tl);
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
