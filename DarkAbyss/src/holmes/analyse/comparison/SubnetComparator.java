package holmes.analyse.comparison;

import holmes.analyse.SubnetCalculator;
import holmes.analyse.comparison.structures.BranchBasedSubnet;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class SubnetComparator {
    ArrayList<BranchBasedSubnet> subnetsForFirstNet = new ArrayList<>();
    ArrayList<BranchBasedSubnet> subnetsForSecondNet = new ArrayList<>();
    int[][] founded;
    public boolean firstQuestion = false;
    public boolean secondQuestion = false;
    public boolean thirdQuestion = false;

    public SubnetComparator(ArrayList<SubnetCalculator.SubNet> firstNetList, ArrayList<SubnetCalculator.SubNet> secondNetList) {
        for (SubnetCalculator.SubNet sn : firstNetList) {
            subnetsForFirstNet.add(transformSubnet(sn));
        }

        for (SubnetCalculator.SubNet sn : secondNetList) {
            subnetsForSecondNet.add(transformSubnet(sn));
        }
    }

    public SubnetComparator(SubnetCalculator.SubNet firstNetList, SubnetCalculator.SubNet secondNetList) {
        subnetsForFirstNet.add(transformSubnet(firstNetList));
        subnetsForSecondNet.add(transformSubnet(secondNetList));
    }

    public SubnetComparator(SubnetCalculator.SubNet firstNetList, SubnetCalculator.SubNet secondNetList, boolean fq, boolean sq, boolean tq) {
        subnetsForFirstNet.add(transformSubnet(firstNetList));
        subnetsForSecondNet.add(transformSubnet(secondNetList));

        firstQuestion = fq;
        secondQuestion = sq;
        thirdQuestion = tq;
    }

    public SubnetComparator(ArrayList<SubnetCalculator.SubNet> firstNetList, ArrayList<SubnetCalculator.SubNet> secondNetList, boolean fq, boolean sq, boolean tq) {
        for (SubnetCalculator.SubNet sn : firstNetList) {
            subnetsForFirstNet.add(transformSubnet(sn));
        }

        for (SubnetCalculator.SubNet sn : secondNetList) {
            subnetsForSecondNet.add(transformSubnet(sn));
        }

        firstQuestion = fq;
        secondQuestion = sq;
        thirdQuestion = tq;
    }

    public ArrayList<ArrayList<GreatCommonSubnet>> compare() {
        boolean testy = false;
        ArrayList<ArrayList<GreatCommonSubnet>> resultMatrix = new ArrayList<>();

        if (testy) {
            GreatCommonSubnet gcs = compareTwoSubnets(subnetsForFirstNet.get(0), subnetsForSecondNet.get(1));
        } else {

            for (int i = 0; i < subnetsForFirstNet.size(); i++) {
                ArrayList<GreatCommonSubnet> list = new ArrayList<>();
                for (int j = 0; j < subnetsForSecondNet.size(); j++) {
                    System.out.println("Porównywanie sieci nr: " + i + " z sieciæ : " + j);
                    if (i == 4 && j == 4) {
                        System.out.println("stop");
                    }
                    list.add(compareTwoSubnets(subnetsForFirstNet.get(i), subnetsForSecondNet.get(j)));
                    list.get(j).firstNetID = i;
                    list.get(j).secondNetID = j;
                    list.get(j).firstNetNodeSize = subnetsForFirstNet.get(i).nodes.size();
                    list.get(j).secondNetNodeSize = subnetsForSecondNet.get(j).nodes.size();
                }
                resultMatrix.add(list);
            }
        }

        return resultMatrix;
    }

    public ArrayList<ArrayList<GreatCommonSubnet>> compareInternalFirst() {
        ArrayList<ArrayList<GreatCommonSubnet>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < subnetsForFirstNet.size(); i++) {
            ArrayList<GreatCommonSubnet> list = new ArrayList<>();
            for (int j = 0; j < subnetsForFirstNet.size(); j++) {
                list.add(compareTwoSubnets(subnetsForFirstNet.get(i), subnetsForFirstNet.get(j)));
                list.get(j).firstNetID = i;
                list.get(j).secondNetID = j;
                list.get(j).firstNetNodeSize = subnetsForFirstNet.get(i).nodes.size();
                list.get(j).secondNetNodeSize = subnetsForFirstNet.get(j).nodes.size();
            }
            resultMatrix.add(list);
        }
        return resultMatrix;
    }

    public ArrayList<ArrayList<GreatCommonSubnet>> compareInternalSecond() {
        ArrayList<ArrayList<GreatCommonSubnet>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < subnetsForSecondNet.size(); i++) {
            ArrayList<GreatCommonSubnet> list = new ArrayList<>();
            for (int j = 0; j < subnetsForSecondNet.size(); j++) {
                list.add(compareTwoSubnets(subnetsForSecondNet.get(i), subnetsForSecondNet.get(j)));
                list.get(j).firstNetID = i;
                list.get(j).secondNetID = j;
                list.get(j).firstNetNodeSize = subnetsForSecondNet.get(i).nodes.size();
                list.get(j).secondNetNodeSize = subnetsForSecondNet.get(j).nodes.size();
            }
            resultMatrix.add(list);
        }
        return resultMatrix;
    }

    public ArrayList<ArrayList<GreatCommonSubnet>> compareFirstSecond() {
        ArrayList<ArrayList<GreatCommonSubnet>> resultMatrix = new ArrayList<>();

        for (int i = 0; i < subnetsForFirstNet.size(); i++) {
            ArrayList<GreatCommonSubnet> list = new ArrayList<>();
            for (int j = 0; j < subnetsForSecondNet.size(); j++) {
                list.add(compareTwoSubnets(subnetsForFirstNet.get(i), subnetsForSecondNet.get(j)));
                list.get(j).firstNetID = i;
                list.get(j).secondNetID = j;
                list.get(j).firstNetNodeSize = subnetsForFirstNet.get(i).nodes.size();
                list.get(j).secondNetNodeSize = subnetsForSecondNet.get(j).nodes.size();
            }
            resultMatrix.add(list);
        }

        System.out.println("Jak wyglada macierz 1 " + subnetsForFirstNet.size() + "   -   " + subnetsForSecondNet.size());
        return resultMatrix;
    }

    public ArrayList<ArrayList<GreatCommonSubnet>> compareSecondFirst() {
        ArrayList<ArrayList<GreatCommonSubnet>> resultMatrix = new ArrayList<>();
        for (int i = 0; i < subnetsForSecondNet.size(); i++) {
            ArrayList<GreatCommonSubnet> list = new ArrayList<>();
            for (int j = 0; j < subnetsForFirstNet.size(); j++) {
                list.add(compareTwoSubnets(subnetsForSecondNet.get(i), subnetsForFirstNet.get(j)));
                list.get(j).firstNetID = i;
                list.get(j).secondNetID = j;
                list.get(j).firstNetNodeSize = subnetsForSecondNet.get(i).nodes.size();
                list.get(j).secondNetNodeSize = subnetsForFirstNet.get(j).nodes.size();
            }
            resultMatrix.add(list);
        }

        System.out.println("Jak wyglada macierz 2 " + subnetsForFirstNet.size() + "   -   " + subnetsForSecondNet.size());
        return resultMatrix;
    }

    public GreatCommonSubnet compareTest() {

        //subnetsForFirstNet.get(0)

        /*
        for(BranchBasedSubnet.BranchVertex bv : subnetsForFirstNet.get(0).branchVertices)
        {
            for(BranchBasedSubnet.Branch tbranch : bv.tbranch)
            {
                if(tbranch.startNode.equals(tbranch.endNode) && getDirection(tbranch))
                {
                    Collections.reverse(tbranch.branchArcs);
                    Collections.reverse(tbranch.branchElements);
                }
            }
        }

        for(BranchBasedSubnet.BranchVertex bv : subnetsForSecondNet.get(0).branchVertices)
        {
            for(BranchBasedSubnet.Branch tbranch : bv.tbranch)
            {
                if(tbranch.startNode.equals(tbranch.endNode) && getDirection(tbranch))
                {
                    Collections.reverse(tbranch.branchArcs);
                    Collections.reverse(tbranch.branchElements);
                }
            }
        }
        */

        return compareTwoSubnets(subnetsForFirstNet.get(0), subnetsForSecondNet.get(0));
    }

    private GreatCommonSubnet compareTwoSubnets(BranchBasedSubnet net1, BranchBasedSubnet net2) {
        ArrayList<PartialSubnetElements> psel = new ArrayList<>();

        ////
        if (net1.branchVertices.size() == 0 && net2.branchVertices.size() == 0) {
            if (net1.paths.size() == 1 && net2.paths.size() == 1) {
                psel.addAll(comparePaths(net1.paths.get(0), net2.paths.get(0)));
                System.out.println("X1 - Obie sieci bez tranzycji branchowych");
            }
        } else if (net1.branchVertices.size() == 0 && net2.branchVertices.size() > 0) {
            //TODO identify path - cast oath to branch and compare using  varoatopn of comparePTBranches

            psel.addAll(comparePathToBranch(net1, net2));
            System.out.println("X2 - pierwsza sieć bez branchowych, druga z branchowycmi");
            ///wut? ja chcę najlepsze
            /*
            for (int p = 0; p < net2.paths.size(); p++) {
                psel.addAll(comparePaths(net1.paths.get(0), net2.paths.get(p)));
                System.out.println("X2 - pierwsza sieć bez branchowych, druga z branchowycmi");
            }
            */
        } else if (net2.branchVertices.size() == 0 && net1.branchVertices.size() > 0) {
            psel.addAll(comparePathToBranch(net2, net1));
            ///wut? ja chcę najlepsze
            /*
            for (int p = 0; p < net1.paths.size(); p++) {
                psel.addAll(comparePaths(net1.paths.get(p), net2.paths.get(0)));
                System.out.println("X3 - druga sieć bez branchowych, pierwsza z branchowymi");
            }
             */

            //TODO uprość 3 poniższe w 2 if ((net1.branchVertices.size() == 1 && net2.branchVertices.size() >= 1)||(net1.branchVertices.size() >= 1 && net2.branchVertices.size() == 1) ) {
        } else if (net1.branchVertices.size() == 1 && net2.branchVertices.size() == 1) {
            psel.addAll(comparePTBranches(net1, net2));
            System.out.println("X4 - obie sieci zawierajæ dokładnie 1 tranzycje branchowæ");
        } else if (net1.branchVertices.size() > 1 && net2.branchVertices.size() == 1) {
            psel.addAll(comparePTBranches(net1, net2));
            System.out.println("X7 - druga z 1 branchowæ, pierwsza z wiěcej niž jednæ branchowæ - tbranch");
        } else if (net2.branchVertices.size() > 1 && net1.branchVertices.size() == 1) {
            //TODO czy powinieneć odwracać
            psel.addAll(comparePTBranches(net2, net1));
            System.out.println("X8 - peirwsza z 1 branchowæ, druga z wiěcej niž jednæ branchowæ - tbranch");
        } else if (net1.branchVertices.size() > 1 && net2.branchVertices.size() > 1) {
            //brute version
            psel.addAll(comparePTMultiBranches(net1, net2));
        } else {
            JOptionPane.showMessageDialog(null, "Nieprzewidziany przypadek podczas porównywania", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }

        ////
        ArrayList<PartialSubnetElements> maxComparisons = new ArrayList<>();
        ArrayList<PartialSubnetElements> maxArcComparisons = new ArrayList<>();

        for (PartialSubnetElements pse : psel) {
            pse.check();
        }

        if (psel.size() == 0) {
            return new GreatCommonSubnet(new ArrayList<>());
        }
        double max = psel.stream().max(Comparator.comparing(PartialSubnetElements::matchingValueFunction)).get().matchingValueFunction();
        maxComparisons = (ArrayList) psel.stream().filter(x -> x.matchingValueFunction() == max).collect(Collectors.toList());

        double maxa = maxComparisons.stream().max(Comparator.comparing(PartialSubnetElements::matchingArcValueFunction)).get().matchingArcValueFunction();
        maxArcComparisons = (ArrayList) maxComparisons.stream().filter(x -> x.matchingArcValueFunction() == maxa).collect(Collectors.toList());

        /*
        if (thirdQuestion) {
            maxComparisons = reductToConnectedStructure(maxComparisons);
        }*/

        return new GreatCommonSubnet(maxArcComparisons);
    }


    public void saveSubnet(GreatCommonSubnet gcs) {
        ArrayList<ElementLocation> listOfElements = new ArrayList<>();

        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Arc a : pse.partialArcs
            ) {
                System.out.println("Arc : " + a.getStartNode().getType() + " " + a.getStartNode().getName() + " - > " + a.getEndNode().getType() + " " + a.getEndNode().getName());
            }

            for (Node n : pse.partialNodes
            ) {
                System.out.println("Node " + n.getName());

                //for (ElementLocation el : n.getElementLocations()) {
                //if (el.isSelected()) {
                //   listOfElements.add(el);
                //}
                // }
            }
        }


        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Arc a : pse.partialArcs
            ) {
                if (!listOfElements.contains(a.getStartLocation())) {
                    listOfElements.add(a.getStartLocation());
                }

                if (!listOfElements.contains(a.getEndLocation())) {
                    listOfElements.add(a.getEndLocation());
                }
            }
        }

        /*
        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Node n : pse.partialNodes
            ) {
                for (ElementLocation el : n.getElementLocations())
                {
                    //if (el.isSelected()) {
                    listOfElements.add(el);
                    //}
                }
            }
        }
        */

        IOprotocols io = new IOprotocols();
        io.exportSubnet(listOfElements, gcs.psel.get(0).partialArcs);
    }


    private ArrayList<PartialSubnetElements> reductToConnectedStructure(ArrayList<PartialSubnetElements> maxComparisons) {
        ArrayList<PartialSubnetElements> result = new ArrayList<>();

        PartialSubnetElements pse = maxComparisons.get(0);
        maxComparisons.remove(0);

        while (maxComparisons.size() > 0) {
            ArrayList<Integer> listOfIndexes = new ArrayList<>();
            boolean next = false;
            for (int i = 0; i < maxComparisons.size(); i++) {
                List<Node> common = new ArrayList<Node>(pse.partialNodes);
                common.retainAll(maxComparisons.get(i).partialNodes);
                if (!common.isEmpty()) {
                    listOfIndexes.add(i);
                    next = true;
                }
            }


        }

        return result;
    }

    private ArrayList<PartialSubnetElements> comparePathToBranch(BranchBasedSubnet net1, BranchBasedSubnet net2) {
        ArrayList<PartialSubnetElements> psel = new ArrayList<>();

        if (net1.paths.size() == 1) {
            SubnetCalculator.Path path = net1.paths.get(0);
            if (path.isCycle) {
                path.innerpath.add(path.endNode);
                path.endNode = path.startNode;
                path.path.add(path.endNode);
            }
            //get type
            PartialSubnetElements pse = new PartialSubnetElements(new ArrayList<>(), new ArrayList<>());
            if (path.startNode.getType() == PetriNetElement.PetriNetElementType.TRANSITION && path.endNode.getType() == PetriNetElement.PetriNetElementType.TRANSITION) {
                System.out.println("Typ 0");
                System.out.println("Kimkolwiek jesteś, czemu to stworzyłeś? Jako to ma sens");
                for (int i = 0; i < net2.branchVertices.size(); i++) {
                    //TODO weź max lub tej samej długości
                    if (secondQuestion && net2.branchVertices.get(i).tbranch.size() == 0) {
                        ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                        branchesList.addAll(net2.branchVertices.get(i).tbranch);
                        if (secondQuestion) {
                            branchesList.addAll(net2.branchVertices.get(i).pbranches);
                        }
                        BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);
                        BranchBasedSubnet.Branch brRev = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);

                        Collections.reverse(brRev.branchElements);
                        Collections.reverse(brRev.internalBranchElements);
                        Collections.reverse(brRev.branchArcs);
                        Node osn = brRev.startNode;
                        Node oen = brRev.endNode;
                        brRev.endNode = osn;
                        brRev.startNode = oen;

                        ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                        brl.add(br);
                        //brl.add(brRev);
                        pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                        psel.add(pse);


                        ArrayList<BranchBasedSubnet.Branch> brlr = new ArrayList<BranchBasedSubnet.Branch>();
                        brlr.add(brRev);
                        pse = comparePbranches(brlr, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                        psel.add(pse);
                    }
                    for (int j = 0; j < net2.branchVertices.get(i).tbranch.size(); j++) {

                        if(thirdQuestion && net2.branchVertices.get(i).tbranchC.contains(net2.branchVertices.get(i).tbranch.get(j))) {

                        }
                        else
                        {
                            ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                            if(!thirdQuestion)
                                branchesList.addAll(net2.branchVertices.get(i).tbranch);
                            else
                                branchesList.addAll(net2.branchVertices.get(i).tbranchS);

                            if (secondQuestion) {
                                branchesList.addAll(net2.branchVertices.get(i).pbranches);
                            }
                            // net2.branchVertices.get(i).pbranches.get(j).startNode

                            BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);
                            BranchBasedSubnet.Branch brRev = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);

                            Collections.reverse(brRev.branchElements);
                            Collections.reverse(brRev.internalBranchElements);
                            Collections.reverse(brRev.branchArcs);
                            Node osn = brRev.startNode;
                            Node oen = brRev.endNode;
                            brRev.endNode = osn;
                            brRev.startNode = oen;

                            ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                            brl.add(br);
                            //brl.add(brRev);
                            pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                            psel.add(pse);


                            ArrayList<BranchBasedSubnet.Branch> brlr = new ArrayList<BranchBasedSubnet.Branch>();
                            brlr.add(brRev);
                            pse = comparePbranches(brlr, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                            psel.add(pse);

                            //pse = comparePbranches(brl, branchesList, brl.get(0).endNode, net2.branchVertices.get(i).root);
                            //psel.add(pse);
                        }
                    }

                }

            }

            if (path.startNode.getType() == PetriNetElement.PetriNetElementType.TRANSITION && path.endNode.getType() == PetriNetElement.PetriNetElementType.PLACE) {
                System.out.println("Typ 1");
                for (int i = 0; i < net2.branchVertices.size(); i++) {
                    //TODO weź max lub tej samej długości
                    if (secondQuestion && net2.branchVertices.get(i).pbranches.size() == 0) {
                        ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                        branchesList.addAll(net2.branchVertices.get(i).pbranches);
                        if (secondQuestion) {
                            branchesList.addAll(net2.branchVertices.get(i).tbranch);
                        }
                        // net2.branchVertices.get(i).pbranches.get(j).startNode
                        BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);
                        ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                        brl.add(br);
                        pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                        psel.add(pse);
                    }
                    for (int j = 0; j < net2.branchVertices.get(i).pbranches.size(); j++) {

                        ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                        branchesList.addAll(net2.branchVertices.get(i).pbranches);
                        if (secondQuestion) {
                            branchesList.addAll(net2.branchVertices.get(i).tbranch);
                        }
                        // net2.branchVertices.get(i).pbranches.get(j).startNode
                        BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);
                        ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                        brl.add(br);
                        pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                        psel.add(pse);
                    }

                }
            }
            if (path.startNode.getType() == PetriNetElement.PetriNetElementType.PLACE && path.endNode.getType() == PetriNetElement.PetriNetElementType.TRANSITION) {
                System.out.println("Typ 1");
                for (int i = 0; i < net2.branchVertices.size(); i++) {
                    //TODO weź max lub tej samej długości
                    if (secondQuestion && net2.branchVertices.get(i).pbranches.size() == 0) {
                        ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                        branchesList.addAll(net2.branchVertices.get(i).pbranches);
                        if (secondQuestion) {
                            branchesList.addAll(net2.branchVertices.get(i).tbranch);
                        }
                        // net2.branchVertices.get(i).pbranches.get(j).startNode
                        ArrayList<Node> reverPathNode = new ArrayList<>(path.path);
                        ArrayList<Arc> reversPathArc = new ArrayList<>(getArcList(path.path));
                        BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(reverPathNode, reversPathArc, null);
                        ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                        brl.add(br);
                        pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                        psel.add(pse);
                    }
                    for (int j = 0; j < net2.branchVertices.get(i).pbranches.size(); j++) {

                        ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                        branchesList.addAll(net2.branchVertices.get(i).pbranches);
                        if (secondQuestion) {
                            branchesList.addAll(net2.branchVertices.get(i).tbranch);
                        }
                        // net2.branchVertices.get(i).pbranches.get(j).startNode
                        ArrayList<Node> reverPathNode = new ArrayList<>(path.path);
                        ArrayList<Arc> reversPathArc = new ArrayList<>(getArcList(path.path));
                        BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(reverPathNode, reversPathArc, null);
                        ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                        brl.add(br);
                        pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                        psel.add(pse);
                    }
                }
            }
            if (path.startNode.getType() == PetriNetElement.PetriNetElementType.PLACE && path.endNode.getType() == PetriNetElement.PetriNetElementType.PLACE) {

                System.out.println("Typ 2");
                //TODO przypadek dla cylki i branchy
                //sprawdź, przesuń i sprawdź jeszcze raz

                if (path.isCycle) {
                    /////////////
                    System.out.println("Typ 0");
                    System.out.println("Kimkolwiek jesteś, czemu to stworzyłeś? Jako to ma sens");
                    for (int i = 0; i < net2.branchVertices.size(); i++) {
                        //TODO weź max lub tej samej długości

                        //TODO CHYBA
                        if (secondQuestion && net2.branchVertices.get(i).tbranch.size() == 0) {
                            ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                            branchesList.addAll(net2.branchVertices.get(i).tbranch);
                            if (secondQuestion) {
                                branchesList.addAll(net2.branchVertices.get(i).pbranches);
                            }
                            BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);
                            BranchBasedSubnet.Branch brRev = new BranchBasedSubnet.Branch(path.path, getArcList(path.path), null);

                            Collections.reverse(brRev.branchElements);
                            Collections.reverse(brRev.internalBranchElements);
                            Collections.reverse(brRev.branchArcs);
                            Node osn = brRev.startNode;
                            Node oen = brRev.endNode;
                            brRev.endNode = osn;
                            brRev.startNode = oen;

                            ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                            brl.add(br);
                            //brl.add(brRev);
                            pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                            psel.add(pse);


                            ArrayList<BranchBasedSubnet.Branch> brlr = new ArrayList<BranchBasedSubnet.Branch>();
                            brlr.add(brRev);
                            pse = comparePbranches(brlr, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                            psel.add(pse);
                        }


                        for (int j = 0; j < net2.branchVertices.get(i).tbranch.size(); j++) {

                            if (net2.branchVertices.get(i).tbranch.get(j).startNode.equals(net2.branchVertices.get(i).tbranch.get(j).endNode)) {

                                ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                                branchesList.addAll(net2.branchVertices.get(i).tbranch);
                                if (secondQuestion) {
                                    branchesList.addAll(net2.branchVertices.get(i).pbranches);
                                }
                                // net2.branchVertices.get(i).pbranches.get(j).startNode
                                ArrayList<Node> movedPath = moveCycle(path.path);
                                BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(movedPath, getArcList(movedPath), null);
                                BranchBasedSubnet.Branch brRev = new BranchBasedSubnet.Branch(movedPath, getArcList(movedPath), null);

                                Collections.reverse(brRev.branchElements);
                                Collections.reverse(brRev.internalBranchElements);
                                Collections.reverse(brRev.branchArcs);
                                Node osn = brRev.startNode;
                                Node oen = brRev.endNode;
                                brRev.endNode = osn;
                                brRev.startNode = oen;

                                ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                                brl.add(br);
                                //brl.add(brRev);
                                pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                                psel.add(pse);


                                //ArrayList<BranchBasedSubnet.Branch> brlr = new ArrayList<BranchBasedSubnet.Branch>();
                                //brlr.add(brRev);
                                pse = comparePbranches(brl, branchesList, brl.get(0).endNode, net2.branchVertices.get(i).root);
                                psel.add(pse);
                            } else {

                                ArrayList<BranchBasedSubnet.Branch> branchesList = new ArrayList<>();
                                branchesList.addAll(net2.branchVertices.get(i).tbranch);
                                if (secondQuestion) {
                                    branchesList.addAll(net2.branchVertices.get(i).pbranches);
                                }
                                // net2.branchVertices.get(i).pbranches.get(j).startNode
                                ArrayList<Node> movedPath = movePath(path.path);
                                BranchBasedSubnet.Branch br = new BranchBasedSubnet.Branch(movedPath, getArcList(movedPath), null);
                                BranchBasedSubnet.Branch brRev = new BranchBasedSubnet.Branch(movedPath, getArcList(movedPath), null);

                                Collections.reverse(brRev.branchElements);
                                Collections.reverse(brRev.internalBranchElements);
                                Collections.reverse(brRev.branchArcs);
                                Node osn = brRev.startNode;
                                Node oen = brRev.endNode;
                                brRev.endNode = osn;
                                brRev.startNode = oen;

                                ArrayList<BranchBasedSubnet.Branch> brl = new ArrayList<BranchBasedSubnet.Branch>();
                                brl.add(br);
                                //brl.add(brRev);
                                pse = comparePbranches(brl, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                                psel.add(pse);


                                //ArrayList<BranchBasedSubnet.Branch> brlr = new ArrayList<BranchBasedSubnet.Branch>();
                                //brlr.add(brRev);


                                ArrayList<BranchBasedSubnet.Branch> brlr = new ArrayList<BranchBasedSubnet.Branch>();
                                brlr.add(brRev);
                                pse = comparePbranches(brlr, branchesList, brl.get(0).startNode, net2.branchVertices.get(i).root);
                                psel.add(pse);

                                //pse = comparePbranches(brl, branchesList, brl.get(0).endNode, net2.branchVertices.get(i).root);
                                //psel.add(pse);
                            }
                        }

                    }

                    //////////////
                }

                //psel.add(pse);
            }

        } else {
            //error
        }

        return psel;
    }

    private ArrayList<Node> moveCycle(ArrayList<Node> path) {
        ArrayList<Node> result = new ArrayList<>(path);
        result.remove(0);
        result.add(result.get(0));
        return result;
    }

    private ArrayList<Node> movePath(ArrayList<Node> path) {
        ArrayList<Node> result = new ArrayList<>(path);
        result.remove(0);
        //result.add(result.get(0));
        return result;
    }

    private ArrayList<Arc> getArcList(ArrayList<Node> path) {

        ArrayList<Arc> arcList = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            int finalI = i;
            Arc a = path.get(i).getOutInArcs().stream().filter(x -> x.getEndNode().getID() == path.get(finalI + 1).getID()).findFirst().orElse(null);
            if (a != null) {
                arcList.add(a);
            }
        }
        return arcList;
    }

    private GreatCommonSubnet compareTwoSubnetsOLD(BranchBasedSubnet net1, BranchBasedSubnet net2) {

        ArrayList<PartialSubnetElements> psel = new ArrayList<>();

        if (net1.branchVertices.size() == 0 && net2.branchVertices.size() == 0) {
            if (net1.paths.size() == 1 && net2.paths.size() == 1) {
                psel.addAll(comparePaths(net1.paths.get(0), net2.paths.get(0)));
                System.out.println("X1 - Obie sieci bez tranzycji branchowych");
            } else {
                //JOptionPane.showMessageDialog(null, "Error, ma kilka ścieżek a nie jest branchowa", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        } else if (net1.branchVertices.size() == 0 && net2.branchVertices.size() > 0) {
            for (int p = 0; p < net2.paths.size(); p++) {
                psel.addAll(comparePaths(net1.paths.get(0), net2.paths.get(p)));
                System.out.println("X2 - pierwsza sieć bez branchowych, druga z branchowycmi");
            }
        } else if (net2.branchVertices.size() == 0 && net1.branchVertices.size() > 0) {
            for (int p = 0; p < net1.paths.size(); p++) {
                psel.addAll(comparePaths(net1.paths.get(p), net2.paths.get(0)));
                System.out.println("X3 - druga sieć bez branchowych, pierwsza z branchowymi");
            }
        } else if (net1.branchVertices.size() == 1 && net2.branchVertices.size() == 1) {
            psel.addAll(comparePTBranches(net1, net2));//WAS TRUE
            System.out.println("X4 - obie sieci zawierajæ dokładnie 1 tranzycje branchowæ");
        } else if (net1.branchVertices.size() == 0 && net2.branchVertices.size() > 0) {
            for (int p = 0; p < net2.paths.size(); p++) {
                psel.addAll(comparePaths(net1.paths.get(0), net2.paths.get(p)));
                System.out.println("X5 - pierwsza bez branchowych, druga z wiěcej niž jednæ branchowæ - tbranch");
            }
        } else if (net2.branchVertices.size() == 0 && net1.branchVertices.size() > 0) {
            for (int p = 0; p < net1.paths.size(); p++) {
                psel.addAll(comparePaths(net1.paths.get(p), net2.paths.get(0)));
                System.out.println("X6 - druga bez branchowych, pierwsza z wiěcej niž jednæ branchowæ - tbranch");
            }
        } else if (net1.branchVertices.size() > 1 && net2.branchVertices.size() == 1) {
            psel.addAll(comparePTBranches(net1, net2));
            System.out.println("X7 - druga z 1 branchowæ, pierwsza z wiěcej niž jednæ branchowæ - tbranch");
        } else if (net2.branchVertices.size() > 1 && net1.branchVertices.size() == 1) {
            psel.addAll(comparePTBranches(net2, net1));
            System.out.println("X8 - peirwsza z 1 branchowæ, druga z wiěcej niž jednæ branchowæ - tbranch");
        } else if (net1.branchVertices.size() == 1 && net2.branchVertices.size() == 1) {
            psel.add(comparePbranches(net1.branchVertices.get(0).pbranches, net2.branchVertices.get(0).pbranches, net1.branchVertices.get(0).root, net2.branchVertices.get(0).root));
            System.out.println("X9 - obie z jednæ branchowæ");
        } else if (net1.branchVertices.size() > 1 && net2.branchVertices.size() > 1) {
            //brute version
            psel.addAll(comparePTMultiBranches(net1, net2));
        } else {
            JOptionPane.showMessageDialog(null, "Nieprzewidziany przypadek podczas porównywania", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }

        ArrayList<PartialSubnetElements> maxComparisons = new ArrayList<PartialSubnetElements>();

        if (psel.size() == 0) {
            return new GreatCommonSubnet(new ArrayList<>());
        }
        double max = psel.stream().max(Comparator.comparing(x -> x.matchingValueFunction())).get().matchingValueFunction();
        maxComparisons = (ArrayList) psel.stream().filter(x -> x.matchingValueFunction() == max).collect(Collectors.toList());


        return new GreatCommonSubnet(maxComparisons);
    }

    private ArrayList<PartialSubnetElements> addToPSE(BranchBasedSubnet.Branch branch, ArrayList<PartialSubnetElements> pseList) {
        for (int i = 0; i < pseList.size(); i++) {
            if (pseList.get(i).partialNodes.contains(branch.startNode)) {
                pseList.get(i).partialNodes.addAll(branch.internalBranchElements);
                pseList.get(i).partialNodes.add(branch.endNode);
                pseList.get(i).partialArcs.addAll(branch.branchArcs);
            } else if (pseList.get(i).partialNodes.contains(branch.endNode)) {
                pseList.get(i).partialNodes.addAll(branch.branchElements);
                pseList.get(i).partialNodes.add(branch.startNode);
                pseList.get(i).partialArcs.addAll(branch.branchArcs);
            } else {
                JOptionPane.showMessageDialog(null,
                        "You not suppose to be here... No one suppose to be here", "Hahaha... - błąd przy tbranchach dla mergowanych branchy rozłącznych",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        ArrayList<PartialSubnetElements> ndup = new ArrayList<>();
        for (PartialSubnetElements pse : pseList) {
            pse.partialNodes = pse.partialNodes.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        }
        return pseList;
    }

    private ArrayList<PartialSubnetElements> mergeBranches(BranchBasedSubnet.Branch branch, ArrayList<PartialSubnetElements> pseList) {
        ArrayList<PartialSubnetElements> indexToMerge = new ArrayList<>();
        for (int i = 0; i < pseList.size(); i++) {
            if (pseList.get(i).partialNodes.contains(branch.startNode) || pseList.get(i).partialNodes.contains(branch.endNode)) {
                indexToMerge.add(pseList.get(i));
            }
        }

        if (indexToMerge.size() == 2) {
            pseList.removeAll(indexToMerge);
            indexToMerge.get(0).partialNodes.addAll(branch.branchElements);
            indexToMerge.get(0).partialNodes = (ArrayList<Node>) indexToMerge.get(0).partialNodes.stream().distinct().collect(Collectors.toList());
            indexToMerge.get(0).partialArcs.addAll(branch.branchArcs);
            PartialSubnetElements merged = mergePartialSubnetElements(indexToMerge.get(0), indexToMerge.get(1));
            pseList.add(merged);
        } else if (indexToMerge.size() == 1) {
            pseList.removeAll(indexToMerge);
            //indexToMerge.get(0).partialNodes.addAll(branch.internalBranchElements);
            indexToMerge.get(0).partialNodes.addAll(branch.branchElements);
            indexToMerge.get(0).partialNodes = (ArrayList<Node>) indexToMerge.get(0).partialNodes.stream().distinct().collect(Collectors.toList());
            indexToMerge.get(0).partialArcs.addAll(branch.branchArcs);
            PartialSubnetElements merged = new PartialSubnetElements(indexToMerge.get(0).partialNodes, indexToMerge.get(0).partialArcs);
            pseList.add(merged);
        } else if (indexToMerge.size() == 0) {
            pseList.add(new PartialSubnetElements(branch.branchElements, branch.branchArcs));
        } else {
            JOptionPane.showMessageDialog(null,
                    "You not suppose to be here... No one suppose to be here", "Hahaha... - błąd przy tbranchach dla mergowanych branchy",
                    JOptionPane.ERROR_MESSAGE);
        }
        return pseList;
    }

    private ArrayList<BranchBasedSubnet.Branch> compareTbranch(BranchBasedSubnet.Branch firstNetBranch, BranchBasedSubnet.Branch secondNetBranch, BranchBasedSubnet bbs) {
        ArrayList<BranchBasedSubnet.Branch> listOfBranches = new ArrayList<>();

        //////
        ////////
        //////
        ////////
        //ACTHTUNG, nie wyłąpuje mniejsca w przypadku pomiędzy branchami
        ////////
        ///////
        //////
        ///
        System.out.println("Tworzymy Tbranche");

        //TODO nie wycina dość

        ArrayList<Node> commonNodesIn = new ArrayList<>();
        ArrayList<Arc> commonArcsIn = new ArrayList<>();

        if(firstNetBranch.branchElements.size() == 5 && secondNetBranch.branchElements.size()==5)
        {
            System.out.println("test");
        }

        if (firstNetBranch.branchElements.size() == secondNetBranch.branchElements.size() && !firstNetBranch.startNode.equals(firstNetBranch.endNode) && !secondNetBranch.startNode.equals(secondNetBranch.endNode)) {

            //listOfBranches.add(compareEqualSozeBranches(firstNetBranch,secondNetBranch, ));

            //TODO what if one is Loop - do not take last
            BranchBasedSubnet.Branch br;

            //TODO is it work
            /*
            if(firstNetBranch.startNode.equals(firstNetBranch.endNode) && !secondNetBranch.startNode.equals(secondNetBranch.endNode))
            {
                br = compareEqualSizeBranchesLoops(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
            }
            else if(!firstNetBranch.startNode.equals(firstNetBranch.endNode) && secondNetBranch.startNode.equals(secondNetBranch.endNode))
            {
                br = compareEqualSizeBranchesLoops(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
            } else
            {
                br = compareEqualSizeBranches(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
            }
            */
            System.out.println("tbranche prawidłowe");
            br = compareEqualSizeBranches(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
            listOfBranches.add(br);
        } else {
            System.out.println("tbranche nieprawidłowe");
            //Porównanie nierównych branchy
            //TODO What if one is loop
            //Spreparuj branch aby nie posiadał jednego z arców od pcczątku i
            ///---------------
            if (firstNetBranch.startNode.equals(firstNetBranch.endNode) && !secondNetBranch.startNode.equals(secondNetBranch.endNode)) {
                //last or first
                //firstNetBranch.branchArcs.remove(0);
                if(!thirdQuestion) {
                    BranchBasedSubnet.Branch loop = new BranchBasedSubnet.Branch(firstNetBranch.branchElements, firstNetBranch.branchArcs, firstNetBranch.parent);
                    //loop.branchArcs.remove(0);
                    loop.branchArcs.remove(loop.branchArcs.size() - 1);
                    BranchBasedSubnet.Branch br = compareBranchesSizeFromBeg(loop, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
                    listOfBranches.add(br);
                    commonNodesIn = new ArrayList<>();
                    commonArcsIn = new ArrayList<>();
                    //od końca brancha
                    loop = new BranchBasedSubnet.Branch(firstNetBranch.branchElements, firstNetBranch.branchArcs, firstNetBranch.parent);
                    loop.branchArcs.remove(0);
                    //loop.branchArcs.remove(loop.branchArcs.size()-1);
                    br = compareBranchesSizeFromEnd(loop, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
                    listOfBranches.add(br);
                    System.out.println("I");
                }
            } else if (!firstNetBranch.startNode.equals(firstNetBranch.endNode) && secondNetBranch.startNode.equals(secondNetBranch.endNode)) {

                //secondNetBranch.branchArcs.remove(secondNetBranch.branchArcs.size()-1);
                if(!thirdQuestion) {
                    BranchBasedSubnet.Branch loop = new BranchBasedSubnet.Branch(secondNetBranch.branchElements, secondNetBranch.branchArcs, secondNetBranch.parent);
                    //loop.branchArcs.remove(0);
                    loop.branchArcs.remove(loop.branchArcs.size() - 1);

                    BranchBasedSubnet.Branch br = compareBranchesSizeFromBeg(firstNetBranch, loop, bbs, commonNodesIn, commonArcsIn);
                    listOfBranches.add(br);
                    commonNodesIn = new ArrayList<>();
                    commonArcsIn = new ArrayList<>();
                    //od końca brancha'

                    //TODO potwór wyszedł stąd
                    loop = new BranchBasedSubnet.Branch(secondNetBranch.branchElements, secondNetBranch.branchArcs, secondNetBranch.parent);
                    loop.branchArcs.remove(0);
                    //loop.branchArcs.remove(loop.branchArcs.size()-1);
                    br = compareBranchesSizeFromEnd(firstNetBranch, loop, bbs, commonNodesIn, commonArcsIn);
                    listOfBranches.add(br);
                    System.out.println("II");
                }
                // br = compareEqualSizeBranchesLoops(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
            } else {

                //-------------

                BranchBasedSubnet.Branch br = compareBranchesSizeFromBeg(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
                listOfBranches.add(br);

                commonNodesIn = new ArrayList<>();
                commonArcsIn = new ArrayList<>();

                //od końca brancha
                br = compareBranchesSizeFromEnd(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
                listOfBranches.add(br);
                System.out.println("III");
            }

        }
        System.out.println("-");
        for (BranchBasedSubnet.Branch b : listOfBranches) {

            if (b.branchElements.size() > 4) {
                System.out.println("Doshite");
            }
            System.out.println("--");
            for (Arc a : b.branchArcs)
                System.out.println("Arc " + a.getStartNode().getName() + " -> " + a.getEndNode().getName() + " is " + a.isBranchEnd());
        }


        return listOfBranches;
    }

    private BranchBasedSubnet.Branch compareEqualSizeBranchesLoops(BranchBasedSubnet.Branch firstNetBranch, BranchBasedSubnet.Branch secondNetBranch, BranchBasedSubnet bbs, ArrayList<Node> commonNodesIn, ArrayList<Arc> commonArcsIn) {
        BranchBasedSubnet.Branch br;

        BranchBasedSubnet.Branch fromBeg = compareBranchesSizeFromBeg(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);
        BranchBasedSubnet.Branch fromEnd = compareBranchesSizeFromEnd(firstNetBranch, secondNetBranch, bbs, commonNodesIn, commonArcsIn);

        if (fromBeg.branchElements.size() > fromEnd.branchElements.size()) {
            br = fromBeg;
        } else {
            br = fromEnd;
        }

        return br;
    }

    private BranchBasedSubnet.Branch compareBranchesSizeFromEnd(BranchBasedSubnet.Branch firstNetBranch, BranchBasedSubnet.Branch secondNetBranch, BranchBasedSubnet bbs, ArrayList<Node> commonNodesIn, ArrayList<Arc> commonArcsIn) {

        HashMap<Node, Node> nm = new HashMap<>();
        HashMap<Arc, Arc> am = new HashMap<>();
        BranchBasedSubnet.Branch br;
        for (int i = 0; i < firstNetBranch.branchArcs.size() && i < secondNetBranch.branchArcs.size(); i++) {
            if (firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getWeight() != secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getWeight()) {
                int arcWeight = Math.min(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getWeight(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getWeight());
                if (arcWeight != 0) {

                    if (firstQuestion) {
                        firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).setMemoryOfArcWeight(arcWeight);
                        if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1)) {
                            //firstNetBranch.branchArcs.get(i).setBranchEnd(true);
                            firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).setBranchEnd(true);
                        } else {
                            commonArcsIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i));
                            am.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i));
                            //commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size()-1-i).getStartNode());
                            if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode())) {
                                commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode());
                                nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getEndNode());
                            }
                            if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode())) {
                                commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode());
                                nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getStartNode());
                            }
                        }
                    } else {
                        firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).setMemoryOfArcWeight(arcWeight);
                        if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1)) {
                            //firstNetBranch.branchArcs.get(i).setBranchEnd(true);
                            firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).setBranchEnd(true);
                        }
                        commonArcsIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i));
                        am.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i));
                        //commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size()-1-i).getStartNode());
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode());
                            nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getEndNode());
                        }
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode());
                            nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getStartNode());
                        }
                    }
                }
            } else {
                //zmiana
                if (firstQuestion) {
                    if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1)) {
                        //firstNetBranch.branchArcs.get(i).setBranchEnd(true);
                        firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).setBranchEnd(true);
                    } else {
                        commonArcsIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i));
                        am.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i));
                        //commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size()-1-i).getStartNode());
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode());
                            nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getEndNode());
                        }
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode());
                            nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getStartNode());
                        }
                    }
                } else {
                    if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1))
                        firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).setBranchEnd(true);
                    //firstNetBranch.branchArcs.get(i).setBranchEnd(true);

                    commonArcsIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i));
                    am.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i));
                    //commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size()-1-i).getStartNode());
                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode())) {
                        commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode());
                        nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getEndNode());
                    }
                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode())) {
                        commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode());
                        nm.put(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getStartNode());
                    }


                }
            }
        }
        br = new BranchBasedSubnet.Branch(commonNodesIn, commonArcsIn, bbs, nm, am);
        return br;
    }

    private BranchBasedSubnet.Branch compareBranchesSizeFromBeg(BranchBasedSubnet.Branch firstNetBranch, BranchBasedSubnet.Branch secondNetBranch, BranchBasedSubnet bbs, ArrayList<Node> commonNodesIn, ArrayList<Arc> commonArcsIn) {

        HashMap<Node, Node> nm = new HashMap<>();
        HashMap<Arc, Arc> am = new HashMap<>();
        //Od początku brancha
        for (int i = 0; i < firstNetBranch.branchArcs.size() && i < secondNetBranch.branchArcs.size(); i++) {
            //czy waga łuku jest różna
            if (firstNetBranch.branchArcs.get(i).getWeight() != secondNetBranch.branchArcs.get(i).getWeight()) {
                int arcWeight = Math.min(firstNetBranch.branchArcs.get(i).getWeight(), secondNetBranch.branchArcs.get(i).getWeight());
                if (arcWeight != 0) {
                    firstNetBranch.branchArcs.get(i).setMemoryOfArcWeight(arcWeight);
                    if (firstQuestion) {
                        if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1)) {
                            firstNetBranch.branchArcs.get(i).setBranchEnd(true);
                        } else {
                            //TODO czy to nie błąd
                            commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                            am.put(firstNetBranch.branchArcs.get(i), secondNetBranch.branchArcs.get(i));

                            if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getEndNode())) {
                                commonNodesIn.add(firstNetBranch.branchArcs.get(i).getEndNode());
                                nm.put(firstNetBranch.branchArcs.get(i).getEndNode(), secondNetBranch.branchArcs.get(i).getEndNode());
                            }
                            if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getStartNode())) {
                                commonNodesIn.add(firstNetBranch.branchArcs.get(i).getStartNode());
                                nm.put(firstNetBranch.branchArcs.get(i).getStartNode(), secondNetBranch.branchArcs.get(i).getStartNode());
                            }
                        }
                    } else {
                        if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1))
                            firstNetBranch.branchArcs.get(i).setBranchEnd(true);

                        commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                        am.put(firstNetBranch.branchArcs.get(i), secondNetBranch.branchArcs.get(i));


                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getEndNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(i).getEndNode());
                            nm.put(firstNetBranch.branchArcs.get(i).getEndNode(), secondNetBranch.branchArcs.get(i).getEndNode());
                        }
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getStartNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(i).getStartNode());
                            nm.put(firstNetBranch.branchArcs.get(i).getStartNode(), secondNetBranch.branchArcs.get(i).getStartNode());
                        }
                    }
                }
            } else {
                if (firstQuestion) {

                    if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1)) {
                        firstNetBranch.branchArcs.get(i).setBranchEnd(true);
                    } else {
                        commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                        am.put(firstNetBranch.branchArcs.get(i), secondNetBranch.branchArcs.get(i));

                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getStartNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(i).getStartNode());
                            nm.put(firstNetBranch.branchArcs.get(i).getStartNode(), secondNetBranch.branchArcs.get(i).getStartNode());
                        }
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getEndNode())) {
                            commonNodesIn.add(firstNetBranch.branchArcs.get(i).getEndNode());
                            nm.put(firstNetBranch.branchArcs.get(i).getEndNode(), secondNetBranch.branchArcs.get(i).getEndNode());
                        }
                    }
                } else {
                    if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1))
                        firstNetBranch.branchArcs.get(i).setBranchEnd(true);

                    commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                    am.put(firstNetBranch.branchArcs.get(i), secondNetBranch.branchArcs.get(i));

                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getStartNode())) {
                        commonNodesIn.add(firstNetBranch.branchArcs.get(i).getStartNode());
                        nm.put(firstNetBranch.branchArcs.get(i).getStartNode(), secondNetBranch.branchArcs.get(i).getStartNode());
                    }
                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getEndNode())) {
                        commonNodesIn.add(firstNetBranch.branchArcs.get(i).getEndNode());
                        nm.put(firstNetBranch.branchArcs.get(i).getEndNode(), secondNetBranch.branchArcs.get(i).getEndNode());
                    }

                }
            }
        }
        return new BranchBasedSubnet.Branch(commonNodesIn, commonArcsIn, bbs, nm, am);
    }

    private BranchBasedSubnet.Branch compareEqualSizeBranches(BranchBasedSubnet.Branch firstNetBranch, BranchBasedSubnet.Branch secondNetBranch, BranchBasedSubnet bbs, ArrayList<Node> commonNodesIn, ArrayList<Arc> commonArcsIn) {

        HashMap<Node, Node> nm = new HashMap<>();
        HashMap<Arc, Arc> am = new HashMap<>();
        for (int i = 0; i < firstNetBranch.branchArcs.size(); i++) {

            if (firstNetBranch.branchArcs.get(i).getWeight() != secondNetBranch.branchArcs.get(i).getWeight()) {
                int arcWeight = Math.min(firstNetBranch.branchArcs.get(i).getWeight(), secondNetBranch.branchArcs.get(i).getWeight());
                if (arcWeight != 0) {
                    firstNetBranch.branchArcs.get(i).setMemoryOfArcWeight(arcWeight);
                    am.put(firstNetBranch.branchArcs.get(i), secondNetBranch.branchArcs.get(i));
                    if ((i == firstNetBranch.branchArcs.size() - 1 && i != secondNetBranch.branchArcs.size() - 1) || (i != firstNetBranch.branchArcs.size() - 1 && i == secondNetBranch.branchArcs.size() - 1)) {
                        firstNetBranch.branchArcs.get(i).setBranchEnd(true);
                    }
                    commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                }
            } else {
                commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                am.put(firstNetBranch.branchArcs.get(i), secondNetBranch.branchArcs.get(i));
            }
            //wyciente
        }
        for (int i = 0; i < firstNetBranch.branchElements.size(); i++
        ) {
            nm.put(firstNetBranch.branchElements.get(i), secondNetBranch.branchElements.get(i));
        }
        //wklejone
        commonNodesIn.addAll(firstNetBranch.branchElements);

        return new BranchBasedSubnet.Branch(commonNodesIn, commonArcsIn, bbs, nm, am);
    }

    /**
     * @param net1 multibranch net
     * @param net2 one branch net
     */

    private ArrayList<PartialSubnetElements> comparePTBranches(BranchBasedSubnet net1, BranchBasedSubnet net2) {

        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();
        //       ArrayList<BranchBasedSubnet.Branch> tbranches = new ArrayList<>();
        for (int i = 0; i < net1.branchVertices.size(); i++) {
            for (int j = 0; j < net2.branchVertices.size(); j++) {

                if (!secondQuestion) {
                    PartialSubnetElements psePb = comparePbranches(net1.branchVertices.get(i).pbranches, net2.branchVertices.get(j).pbranches, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                    PartialSubnetElements pseAb;
                    //try branch reduction elimination
                    if (net1.branchVertices.get(i).tbranch.stream().anyMatch(x -> x.startNode.equals(x.endNode)) || net2.branchVertices.get(j).tbranch.stream().anyMatch(x -> x.startNode.equals(x.endNode))) {
                        if(!thirdQuestion) {
                            ArrayList<ArrayList<BranchBasedSubnet.Branch>> lvl1 = allPossibleLoopTransformations(net1.branchVertices.get(i).tbranch);
                            ArrayList<ArrayList<BranchBasedSubnet.Branch>> lvl2 = allPossibleLoopTransformations(net2.branchVertices.get(j).tbranch);

                            ArrayList<PartialSubnetElements> lopPse = new ArrayList<>();
                            for (ArrayList<BranchBasedSubnet.Branch> l1 : lvl1) {
                                for (ArrayList<BranchBasedSubnet.Branch> l2 : lvl2) {
                                    if (l1.stream().anyMatch(x -> x.startNode.equals(x.endNode)) && l2.stream().anyMatch(x -> x.startNode.equals(x.endNode))) {

                                    }
                                    PartialSubnetElements pseTbl = comparePbranches(l1, l2, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                                    lopPse.add(pseTbl);
                                }
                            }

                            PartialSubnetElements maxNodeSizePse = lopPse.stream().max(Comparator.comparing(PartialSubnetElements::matchingValueFunction)).orElse(new PartialSubnetElements(new ArrayList<>()));
                            ArrayList<PartialSubnetElements> listOfMaxElemets = lopPse.stream().filter(x -> x.partialNodes.size() == maxNodeSizePse.partialNodes.size()).collect(Collectors.toCollection(ArrayList::new));
                            PartialSubnetElements maxNodeArcSizePse = listOfMaxElemets.stream().max(Comparator.comparing(PartialSubnetElements::matchingArcValueFunction)).orElse(new PartialSubnetElements(new ArrayList<>()));
                            pseAb = mergePartialSubnetElements(psePb, maxNodeArcSizePse);
                        }
                        else
                        {
                            if(net1.branchVertices.get(i).tbranchC.size()>0 && net2.branchVertices.get(j).tbranchC.size()>0)
                            {
                                ArrayList<BranchBasedSubnet.Branch> listOfNoLoopBranchesF = new ArrayList<>(net1.branchVertices.get(i).tbranch);
                                listOfNoLoopBranchesF.removeAll(net1.branchVertices.get(i).tbranchC);

                                ArrayList<BranchBasedSubnet.Branch> listOfNoLoopBranchesS = new ArrayList<>(net2.branchVertices.get(j).tbranch);
                                listOfNoLoopBranchesS.removeAll(net2.branchVertices.get(j).tbranchC);

                                PartialSubnetElements pseTbS = comparePbranches(listOfNoLoopBranchesF,listOfNoLoopBranchesS, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                                PartialSubnetElements pseTbC = comparePbranches(net1.branchVertices.get(i).tbranchC, net2.branchVertices.get(j).tbranchC, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                                PartialSubnetElements pseTb = mergePartialSubnetElements(pseTbS, pseTbC);

                                pseAb = mergePartialSubnetElements(psePb, pseTb);
                            }
                            else if(net1.branchVertices.get(i).tbranchC.size()>0 )
                            {
                                ArrayList<BranchBasedSubnet.Branch> listOfNoLoopBranches = new ArrayList<>(net1.branchVertices.get(i).tbranch);
                                listOfNoLoopBranches.removeAll(net1.branchVertices.get(i).tbranchC);

                                PartialSubnetElements pseTb = comparePbranches(listOfNoLoopBranches,net2.branchVertices.get(j).tbranch, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                                pseAb = mergePartialSubnetElements(psePb, pseTb);
                            }
                            else if(net2.branchVertices.get(j).tbranchC.size()>0)
                            {
                                ArrayList<BranchBasedSubnet.Branch> listOfNoLoopBranches = new ArrayList<>(net2.branchVertices.get(j).tbranch);
                                listOfNoLoopBranches.removeAll(net2.branchVertices.get(j).tbranchC);

                                PartialSubnetElements pseTb = comparePbranches(net1.branchVertices.get(i).tbranch,listOfNoLoopBranches, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                                pseAb = mergePartialSubnetElements(psePb, pseTb);
                            }
                            else
                            {
                                //brak loopów
                                PartialSubnetElements pseTb = comparePbranches(net1.branchVertices.get(i).tbranch, net2.branchVertices.get(j).tbranch, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                                pseAb = mergePartialSubnetElements(psePb, pseTb);
                            }


                        }
                    } else {
                        PartialSubnetElements pseTb = comparePbranches(net1.branchVertices.get(i).tbranch, net2.branchVertices.get(j).tbranch, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                        pseAb = mergePartialSubnetElements(psePb, pseTb);
                    }
                    //pseList.add(pseTb);
                    pseList.add(pseAb);
                } else {

                    if (thirdQuestion) {
                        ArrayList<BranchBasedSubnet.Branch> branchNet1 = new ArrayList<>();
                        branchNet1.addAll(net1.branchVertices.get(i).pbranches);
                        branchNet1.addAll(net1.branchVertices.get(i).tbranch);
                        //tbranches.addAll(net1.branchVertices.get(i).tbranch);

                        ArrayList<BranchBasedSubnet.Branch> branchNet2 = new ArrayList<>();
                        branchNet2.addAll(net2.branchVertices.get(j).pbranches);
                        branchNet2.addAll(net2.branchVertices.get(j).tbranch);

                        PartialSubnetElements psePb = comparePbranches(branchNet1, branchNet2, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                        pseList.add(psePb);
                    } else {
                        //PartialSubnetElements psePb = comparePbranches(net1.branchVertices.get(i).pbranches, net2.branchVertices.get(j).pbranches, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                        //PartialSubnetElements pseAb;
                        if (net1.branchVertices.get(i).tbranch.stream().anyMatch(x -> x.startNode.equals(x.endNode)) || net2.branchVertices.get(j).tbranch.stream().anyMatch(x -> x.startNode.equals(x.endNode))) {
                            ArrayList<BranchBasedSubnet.Branch> branchNet1 = new ArrayList<>();
                            branchNet1.addAll(net1.branchVertices.get(i).pbranches);
                            branchNet1.addAll(net1.branchVertices.get(i).tbranch);
                            ArrayList<ArrayList<BranchBasedSubnet.Branch>> lvl1 = allPossibleLoopTransformations(branchNet1);
                            ArrayList<BranchBasedSubnet.Branch> branchNet2 = new ArrayList<>();
                            branchNet2.addAll(net2.branchVertices.get(j).pbranches);
                            branchNet2.addAll(net2.branchVertices.get(j).tbranch);
                            ArrayList<ArrayList<BranchBasedSubnet.Branch>> lvl2 = allPossibleLoopTransformations(branchNet2);

                            ArrayList<PartialSubnetElements> lopPse = new ArrayList<>();
                            for (ArrayList<BranchBasedSubnet.Branch> l1 : lvl1) {
                                for (ArrayList<BranchBasedSubnet.Branch> l2 : lvl2) {
                                    if (l1.stream().anyMatch(x -> x.startNode.equals(x.endNode)) && l2.stream().anyMatch(x -> x.startNode.equals(x.endNode))) {

                                    }
                                    PartialSubnetElements pseTbl = comparePbranches(l1, l2, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                                    lopPse.add(pseTbl);
                                }
                            }

                            PartialSubnetElements maxNodeSizePse = lopPse.stream().max(Comparator.comparing(PartialSubnetElements::matchingValueFunction)).orElse(new PartialSubnetElements(new ArrayList<>()));
                            ArrayList<PartialSubnetElements> listOfMaxElemets = lopPse.stream().filter(x -> x.partialNodes.size() == maxNodeSizePse.partialNodes.size()).collect(Collectors.toCollection(ArrayList::new));
                            PartialSubnetElements maxNodeArcSizePse = listOfMaxElemets.stream().max(Comparator.comparing(PartialSubnetElements::matchingArcValueFunction)).orElse(new PartialSubnetElements(new ArrayList<>()));
                            pseList.add(maxNodeArcSizePse);
                            //pseAb = mergePartialSubnetElements(psePb, maxNodeArcSizePse);
                            //pseList.add(pseAb);
                        } else {
                            ArrayList<BranchBasedSubnet.Branch> branchNet1 = new ArrayList<>();
                            branchNet1.addAll(net1.branchVertices.get(i).pbranches);
                            branchNet1.addAll(net1.branchVertices.get(i).tbranch);
                            //tbranches.addAll(net1.branchVertices.get(i).tbranch);

                            ArrayList<BranchBasedSubnet.Branch> branchNet2 = new ArrayList<>();
                            branchNet2.addAll(net2.branchVertices.get(j).pbranches);
                            branchNet2.addAll(net2.branchVertices.get(j).tbranch);

                            PartialSubnetElements psePb = comparePbranches(branchNet1, branchNet2, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                            pseList.add(psePb);
                        }
                    }
                }
            }
        }

        double max = pseList.stream().max(Comparator.comparing(x -> x.matchingValueFunction())).get().matchingValueFunction();
        return (ArrayList) pseList.stream().filter(x -> x.matchingValueFunction() == max).collect(Collectors.toList());
    }

    private ArrayList<ArrayList<BranchBasedSubnet.Branch>> allPossibleLoopTransformations(ArrayList<BranchBasedSubnet.Branch> tbranch) {
        ArrayList<ArrayList<BranchBasedSubnet.Branch>> result = new ArrayList<>();
        ArrayList<BranchBasedSubnet.Branch> nonLoopBranches = tbranch.stream().filter(x -> !x.startNode.equals(x.endNode)).collect(Collectors.toCollection(ArrayList::new));
        ArrayList<BranchBasedSubnet.Branch> loopBranches = tbranch.stream().filter(x -> x.startNode.equals(x.endNode)).collect(Collectors.toCollection(ArrayList::new));
        result.add(nonLoopBranches);

        ArrayList<ArrayList<BranchBasedSubnet.Branch>> extensions = new ArrayList<>();
        extensions.add(new ArrayList<>());

        while (loopBranches.size() > 0) {
            BranchBasedSubnet.Branch b1 = loopBranches.get(0);
            loopBranches.remove(b1);
            BranchBasedSubnet.Branch b2 = loopBranches.stream().filter(x -> x.branchElements.containsAll(b1.branchElements)).findFirst().orElseThrow(NoSuchElementException::new);
            loopBranches.remove(b2);
            ArrayList<BranchBasedSubnet.Branch> nb1 = new ArrayList<>();
            nb1.addAll(nonLoopBranches);


            ArrayList<BranchBasedSubnet.Branch> nb2 = new ArrayList<>();
            nb2.addAll(nonLoopBranches);

            ArrayList<ArrayList<BranchBasedSubnet.Branch>> toExtend = new ArrayList<>();

            for (ArrayList<BranchBasedSubnet.Branch> br : extensions) {
                ArrayList<BranchBasedSubnet.Branch> sum = new ArrayList<>();
                sum.addAll(br);
                sum.add(b1);
                toExtend.add(sum);
                sum = new ArrayList<>();
                sum.addAll(br);
                sum.add(b2);
                toExtend.add(sum);
            }

            extensions = toExtend;
        }

        for (ArrayList<BranchBasedSubnet.Branch> extes : extensions) {
            if (nonLoopBranches.size() > 0) {
                result.add(instertLoopsIntoList(nonLoopBranches, extes));
            } else {
                result.add(extes);
            }
        }

        result.removeIf(x -> x.size() == 0);

        return result;
    }

    private ArrayList<BranchBasedSubnet.Branch> instertLoopsIntoList(ArrayList<BranchBasedSubnet.Branch> nonLoopBranches, ArrayList<BranchBasedSubnet.Branch> extes) {
        ArrayList<BranchBasedSubnet.Branch> result = new ArrayList<>();

        while (extes.size() > 0) {
            BranchBasedSubnet.Branch br = extes.get(0);
            extes.remove(0);
            boolean added = false;
            for (int i = 0; i < nonLoopBranches.size(); i++) {
                //skierowanie dodawanego
                boolean incoming = getDirection(br);

                //skierowanie ostatniego brancha i następnego brancha

                result.add(nonLoopBranches.get(i));

                if ((nonLoopBranches.get(i).branchElements.size() >= br.branchElements.size() || getDirection(nonLoopBranches.get(i))) && !added) {
                    result.add(br);
                    added = true;
                } else if ((nonLoopBranches.get(i).branchElements.size() <= br.branchElements.size() || !getDirection(nonLoopBranches.get(i))) && !added) {
                    result.add(br);
                    added = true;
                }
            }
        }

        //ze wsględu na rozmiar


        return result;
    }

    private boolean getDirection(BranchBasedSubnet.Branch br) {
        if (br.branchElements.get(0).getInArcs().stream().anyMatch(x -> x.getStartNode().equals(br.branchElements.get(1))))
            return true;
        else
            return false;
    }


    private void printElements(BranchBasedSubnet.Branch br) {
        System.out.println("Node:");
        for (Node n : br.branchElements) {
            System.out.println(n.getName());
        }
        System.out.println("Arcs:");
        for (Arc a : br.branchArcs) {
            System.out.println(a.getStartNode().getName() + "-->" + a.getEndNode().getName());
        }
    }

    private void printElements(PartialSubnetElements psel) {
        System.out.println("Node:");
        for (Node n : psel.partialNodes) {
            System.out.println(n.getName());
        }
        System.out.println("Arcs:");
        for (Arc a : psel.partialArcs) {
            System.out.println(a.getStartNode().getName() + "-->" + a.getEndNode().getName());
        }
    }

    private ArrayList<PartialSubnetElements> comparePTMultiBranches(BranchBasedSubnet net1, BranchBasedSubnet net2) {

        ArrayList<ArrayList<RowMatch>> bm = calcBranchMatrix(net1, net2);

        bm = parseParallelBranches(bm);
        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();

        for (int i = 0; i < bm.size(); i++) {
            System.out.println("New Branch matrxi matching");
            ArrayList<BranchBasedSubnet.Branch> listofProperTbranches = new ArrayList<>();
            ArrayList<BranchBasedSubnet.Branch> listofProperTbranchesForSecond = new ArrayList<>();
            ArrayList<ArrayList<BranchBasedSubnet.Branch>> listofInroperTbranches = new ArrayList<>();
            ArrayList<ArrayList<BranchBasedSubnet.Branch>> listofInroperTbranchesForSecond = new ArrayList<>();
            //ArrayList<BranchBasedSubnet.Branch> listOfLonelyForFirst = new ArrayList<>();
            //ArrayList<BranchBasedSubnet.Branch> listOfLonelyForSecond = new ArrayList<>();
            HashMap<Integer, Integer> maping = getMaping(bm.get(i));

            //kolejne wierzchołki
            for (int j = 0; j < bm.get(i).size(); j++) {
                //System.out.println("Znajde CIĘ i:=" + i + "i zdebaguję jak psa j:=" + j);
                ArrayList<BranchBasedSubnet.Branch> list = compareTbranch(bm.get(i).get(j).firstNetBranch, bm.get(i).get(j).secondNetBranch, net1);
                if (list.size() == 1) {
                    listofProperTbranches.addAll(list);
                } else if (list.size() == 2) {
                    listofInroperTbranches.add(list);
                } else {
                    if(!thirdQuestion)
                    JOptionPane.showMessageDialog(null,
                            "Tbranch nie znalazł žadnych branchy albo za dužo... tak czy siak klocek", "TO CHECK",
                            JOptionPane.ERROR_MESSAGE);

                }
                ArrayList<PartialSubnetElements> pse = new ArrayList<>(pseList);
            }

            for (int j = 0; j < bm.get(i).size(); j++) {
                //System.out.println("Znajde CIĘ i:=" + i + "i zdebaguję jak psa j:=" + j);
                ArrayList<BranchBasedSubnet.Branch> list = compareTbranch(bm.get(i).get(j).secondNetBranch, bm.get(i).get(j).firstNetBranch, net2);
                if (list.size() == 1) {
                    listofProperTbranchesForSecond.addAll(list);
                } else if (list.size() == 2) {
                    listofInroperTbranchesForSecond.add(list);
                } else {
                    if(!thirdQuestion)
                    JOptionPane.showMessageDialog(null,
                            "Tbranch nie znalazł žadnych branchy albo za dužo... tak czy siak klocek", "TO CHECK",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

            ArrayList<BranchBasedSubnet.BranchVertex> tempBVList = new ArrayList<>(net1.branchVertices);
            ArrayList<PartialSubnetElements> listOfPBranches = new ArrayList<>();

            for (BranchBasedSubnet.BranchVertex bv : net1.branchVertices) {
                int net2BVindex = getBrenachVertexIndex(net1, net2, maping, bv);
                if (net2BVindex != -1) {
                    //find sęks
                    // TODO STEROWANIE FLAGĄ
                    ArrayList<BranchBasedSubnet.Branch> listforFirst = new ArrayList<>(bv.pbranches);
                    //listforFirst.addAll(bv.getSek());

                    ArrayList<BranchBasedSubnet.Branch> listOfSecond = new ArrayList<>(net2.branchVertices.get(net2BVindex).pbranches);
                    //listOfSecond.addAll(net2.branchVertices.get(net2BVindex).getSek());


                    //add sterowanie only t-tp-p
                    if (secondQuestion) {
                        //compapre lonely tbranches

                        //TODO SPRAWDZIĆ JESZCZE RAZ POPRAWNOŚĆ ale LOOPY NAJPIWEw
                        //Poprawia
                        //14_15
                        //8_18
                        //7_15
                        //Rypie
                        //18_8
                        //15_14

                        //niech uczestniczą wolne t-branche
                        //dla pierwszej...

                        /*
                        ArrayList<BranchBasedSubnet.Branch> fromActualBranching = new ArrayList<>(bv.tbranch);
                        fromActualBranching.removeAll(listofProperTbranches);
                        if(fromActualBranching.size()>0) {
                            ArrayList<BranchBasedSubnet.Branch> twoSides = new ArrayList<>();
                            BranchBasedSubnet.Branch rew = new BranchBasedSubnet.Branch(fromActualBranching.get(0));
                            twoSides.add(fromActualBranching.get(0));
                            twoSides.add(rew);
                            listofInroperTbranches.add(twoSides);
                            //listOfLonelyForFirst.addAll(fromActualBranching);
                        }
                        */
                        //ArrayList<BranchBasedSubnet.Branch> fromActualBranching2 = new ArrayList<>(net2.branchVertices.get(net2BVindex).tbranch);
                        //fromActualBranching2.removeAll(listofProperTbranches);
                        //listOfLonelyForSecond.addAll(fromActualBranching2);



/*
                        ArrayList<BranchBasedSubnet.Branch> listOfLonelyForFirst = new ArrayList<>(bv.tbranch);
                        //pozbyć dwukierunkowych
                        ArrayList<BranchBasedSubnet.Branch> twoSide = new ArrayList<>();
                        for (BranchBasedSubnet.Branch br: listOfLonelyForFirst ) {
                            twoSide.addAll(listOfLonelyForFirst.stream().filter(x->x.startNode.equals(br.endNode) && x.endNode.equals(br.startNode) && !x.startNode.equals(x.endNode)).collect(Collectors.toCollection(ArrayList::new)));
                        }
                        listOfLonelyForFirst.removeAll(twoSide);
                        listOfLonelyForFirst.removeAll(listofProperTbranches);
                        //listOfLonelyForFirst.removeAll(listofInroperTbranches);
                        listforFirst.addAll(listOfLonelyForFirst);
                        Collections.sort(listforFirst, new BranchBasedSubnet.Branch.LenghtSort());
                        */

                        ArrayList<BranchBasedSubnet.Branch> listOfLonelyForFirst = new ArrayList<>(bv.tbranch);
                        listOfLonelyForFirst.removeAll(listofProperTbranches);
                        for (BranchBasedSubnet.Branch toR : listofProperTbranches) {
                            ArrayList<BranchBasedSubnet.Branch> toDelate = new ArrayList<>();
                            for (BranchBasedSubnet.Branch td : listOfLonelyForFirst) {
                                if (td.internalBranchElements.containsAll(toR.internalBranchElements) && toR.internalBranchElements.containsAll(td.internalBranchElements) &&td.startNode.equals(toR.startNode) &&td.endNode.equals(toR.endNode) )
                                    //if(td.startNode.equals(toR.startNode) && td.endNode.equals(toR.endNode) )
                                    toDelate.add(td);
                            }
                            listOfLonelyForFirst.removeAll(toDelate);

                            //listOfLonelyForFirst.remove(indD)
                        }


                        for (ArrayList<BranchBasedSubnet.Branch> ls : listofInroperTbranches) {
                            for (BranchBasedSubnet.Branch toR : ls) {
                                ArrayList<BranchBasedSubnet.Branch> toDelate = new ArrayList<>();
                                for (BranchBasedSubnet.Branch td : listOfLonelyForFirst) {
                                    if (td.internalBranchElements.containsAll(toR.internalBranchElements) || toR.internalBranchElements.containsAll(td.internalBranchElements))
                                        //if(td.startNode.equals(toR.startNode) && td.endNode.equals(toR.endNode) )
                                        toDelate.add(td);
                                }
                                listOfLonelyForFirst.removeAll(toDelate);

                                //listOfLonelyForFirst.remove(indD)
                            }
                        }

                        listforFirst.addAll(listOfLonelyForFirst);
                        Collections.sort(listforFirst, new BranchBasedSubnet.Branch.LenghtSort());

                        //Czy szeregować?

                        // i dla drugiej


                        ArrayList<BranchBasedSubnet.Branch> listOfLonelyForSecond = new ArrayList<>(net2.branchVertices.get(net2BVindex).tbranch);
                        listOfLonelyForSecond.removeAll(listofProperTbranchesForSecond);
                        listOfLonelyForSecond.removeAll(listofProperTbranchesForSecond);
                        for (BranchBasedSubnet.Branch toR : listofProperTbranchesForSecond) {
                            ArrayList<BranchBasedSubnet.Branch> toDelate = new ArrayList<>();
                            for (BranchBasedSubnet.Branch td : listOfLonelyForSecond) {
                                if (td.internalBranchElements.containsAll(toR.internalBranchElements) && toR.internalBranchElements.containsAll(td.internalBranchElements)&& toR.internalBranchElements.containsAll(td.internalBranchElements) &&td.startNode.equals(toR.startNode) &&td.endNode.equals(toR.endNode) )
                                    //if(td.startNode.equals(toR.startNode) && td.endNode.equals(toR.endNode) )
                                    toDelate.add(td);
                            }
                            listOfLonelyForSecond.removeAll(toDelate);

                            //listOfLonelyForFirst.remove(indD)
                        }

                        //TODO NIE MOŻE ZACHODZIĆ DLA 13        13_07 czy nie za dużo
                        //listOfLonelyForSecond.removeAll(listofInroperTbranchesForSecond);

                        for (ArrayList<BranchBasedSubnet.Branch> ls : listofInroperTbranchesForSecond) {
                            for (BranchBasedSubnet.Branch toR : ls) {
                                if(!(ls.stream().anyMatch(x->x.startNode.equals(toR.endNode) && x.endNode.equals(toR.startNode)))) {
                                    ArrayList<BranchBasedSubnet.Branch> toDelate = new ArrayList<>();
                                    for (BranchBasedSubnet.Branch td : listOfLonelyForSecond) {
                                        if (td.internalBranchElements.containsAll(toR.internalBranchElements) || toR.internalBranchElements.containsAll(td.internalBranchElements))
                                            //if(td.startNode.equals(toR.startNode) && td.endNode.equals(toR.endNode) )
                                            toDelate.add(td);
                                    }
                                    listOfLonelyForSecond.removeAll(toDelate);
                                }
                                //listOfLonelyForFirst.remove(indD)
                            }
                        }


                        listOfSecond.addAll(listOfLonelyForSecond);

                        Collections.sort(listOfSecond, new BranchBasedSubnet.Branch.LenghtSort());

                        //get those proper

                        //compare pbranches


                    } else {

                    }


                    ///START ZMIAN
                    if (bv.getLoop().size() > 0 || net2.branchVertices.get(net2BVindex).getLoop().size() > 0) {
                        System.out.println("Loopy Pierwszek " + bv.getLoop().size() + "  Loopy Drugiej " + net2.branchVertices.get(net2BVindex).getLoop().size());
                    }

                    PartialSubnetElements psePb = comparePbranches(listforFirst, listOfSecond, bv.root, net2.branchVertices.get(net2BVindex).root);

                    if (psePb.partialNodes.size() > 0) {
                        System.out.println("Dodaję obrancg: ");
                        printElements(psePb);
                        listOfPBranches.add(psePb);
                    }
                    ////KONIEC ZMIAN

                    listforFirst = new ArrayList<>(bv.getSek());
                    listOfSecond = new ArrayList<>(net2.branchVertices.get(net2BVindex).getSek());
                    //add unsuded branches - pętle
                    //zakomentowane 16_17 17_16 14_16 16_14
                    if (bv.getLoop().size() > 0 && net2.branchVertices.get(net2BVindex).getLoop().size() > 0) {
                        System.out.print("A " + bv.getLoop().size() + "  B " + net2.branchVertices.get(net2BVindex).getLoop().size());
                    } else {
                        listforFirst.addAll(bv.getLoop());
                        listOfSecond.addAll(net2.branchVertices.get(net2BVindex).getLoop());
                    }

                    if (listforFirst.size() != 0 && listOfSecond.size() != 0) {
                        PartialSubnetElements pseTb = null;
                        if(!thirdQuestion)
                            pseTb = comparePbranches(listforFirst, listOfSecond, bv.root, net2.branchVertices.get(net2BVindex).root);
                        else
                        {
                            ArrayList<BranchBasedSubnet.Branch> cyclesF = listforFirst.stream().filter(x->x.startNode.equals(x.endNode)).collect(Collectors.toCollection(ArrayList::new));
                            ArrayList<BranchBasedSubnet.Branch> cyclesS = listOfSecond.stream().filter(x->x.startNode.equals(x.endNode)).collect(Collectors.toCollection(ArrayList::new));
                            ArrayList<BranchBasedSubnet.Branch> pathsF = new ArrayList<>(listforFirst);
                            ArrayList<BranchBasedSubnet.Branch> pathsS = new ArrayList<>(listOfSecond);
                            pathsF.removeAll(cyclesF);
                            pathsS.removeAll(cyclesS);

                            PartialSubnetElements pseTbS = comparePbranches(pathsF, pathsS, bv.root, net2.branchVertices.get(net2BVindex).root);
                            PartialSubnetElements pseTbC = comparePbranches(cyclesF, cyclesS, bv.root, net2.branchVertices.get(net2BVindex).root);
                            pseTb = mergePartialSubnetElements(pseTbS,pseTbC);
                        }

                        if (pseTb.partialNodes.size() > 0) {
                            System.out.println("Dodaję sęk: ");
                            printElements(pseTb);
                            listOfPBranches.add(pseTb);
                        }
                    }
                }

            }
            boolean finished = false;
            PartialSubnetElements pse = new PartialSubnetElements(new ArrayList<>());
            if (listOfPBranches.size() > 0) {
                pse = listOfPBranches.get(0);
                listOfPBranches.remove(0);
            }

            //while (listOfPBranches.size() > 0) {
            while (listOfPBranches.size() > 0 || listofProperTbranches.size() > 0) {
                if (finished) {
                    if (listOfPBranches.size() > 0)
                        pse = listOfPBranches.get(0);
                    else
                        pse = new PartialSubnetElements(new ArrayList<>());
                }

                boolean tbranchAdded = false;
                //add proper tbranches
                ArrayList<BranchBasedSubnet.Branch> toDelate = new ArrayList<>();
                for (BranchBasedSubnet.Branch br : listofProperTbranches) {
                    if (pse.partialNodes.size() > 0) {
                        ArrayList<Node> tmpList = new ArrayList<>(pse.partialNodes);
                        tmpList.retainAll(br.returnBorderNodes());
                        if (tmpList.size() > 0) {

                            System.out.println("Dodaję Tbranch:");
                            printElements(br);
                            pse = mergePartialSubnetElements(pse, new PartialSubnetElements(br.branchElements, br.branchArcs, br.nodeMap, br.arcMap));
                            toDelate.add(br);
                            tbranchAdded = true;
                        }
                    } else {

                        System.out.println("Dodaję Tbranch:");
                        printElements(br);
                        pse = mergePartialSubnetElements(pse, new PartialSubnetElements(br.branchElements, br.branchArcs, br.nodeMap, br.arcMap));
                        toDelate.add(br);
                        tbranchAdded = true;
                    }
                }
                listofProperTbranches.removeAll(toDelate);

                boolean pbranchAdded = false;
                //add pbranches
                ArrayList<PartialSubnetElements> toDelate2 = new ArrayList<>();

                for (PartialSubnetElements br : listOfPBranches) {
                    ArrayList<Node> tmpList = new ArrayList<>(pse.partialNodes);
                    tmpList.retainAll(br.partialNodes);

                    if (tmpList.size() > 0) {
                        System.out.println("Dodaję Pbranch:");
                        printElements(br);
                        pse = mergePartialSubnetElements(pse, br);
                        toDelate2.add(br);
                        //.remove(br);
                        pbranchAdded = true;
                    }
                }
                listOfPBranches.removeAll(toDelate2);
                //Partial NODES!

                //check if finished
                if ((tbranchAdded || pbranchAdded) && (listOfPBranches.size() > 0 || listofProperTbranches.size() > 0)) {
                    finished = false;
                } else {


                    if (secondQuestion) {

                        //listOfLonelyForFirst.size();
                        //TODO dodaj nie używane tbranche
                    }


                    if (listofInroperTbranches.size() > 0) {
                        ArrayList<PartialSubnetElements> listaPse = new ArrayList<>();
                        listaPse.add(pse);
                        if (listaPse.size() > 1) {
                            System.out.println("Warum");
                        }

                        for (ArrayList<BranchBasedSubnet.Branch> twoInpropBranch : listofInroperTbranches) {
                            ArrayList<PartialSubnetElements> tmpListPse = new ArrayList<>(listaPse);

                            for (PartialSubnetElements tmpPSE : listaPse) {
                                for (BranchBasedSubnet.Branch inpropBranch : twoInpropBranch) {

                                    ArrayList<Node> tmpList = new ArrayList<>(tmpPSE.partialNodes);
                                    tmpList.retainAll(inpropBranch.returnBorderNodes());
                                    if (tmpList.size() > 0) {
                                        //System.out.println("Dodaję inpTbranch:");
                                        //printElements(inpropBranch);

                                        PartialSubnetElements newPSE = mergePartialSubnetElements(tmpPSE, new PartialSubnetElements(inpropBranch.branchElements, inpropBranch.branchArcs, inpropBranch.nodeMap, inpropBranch.arcMap));

                                        if (newPSE.partialNodes.size() > 8) {
                                            //System.out.println("Nein Nein NEin");
                                            //System.out.println("Base");

                                            ArrayList<Node> annyConection = new ArrayList<>(tmpPSE.partialNodes);
                                            annyConection.retainAll(inpropBranch.branchElements);

                                            /*
                                            for (Node a : annyConection) {
                                                System.out.println("Node " + a.getName() + " " + a.getID());
                                            }

                                            for (Arc a : tmpPSE.partialArcs) {
                                                System.out.println("Arc " + a.getStartNode().getName() + " -> " + a.getEndNode().getName() + " is " + a.isBranchEnd());
                                            }

                                            System.out.println("Inprop");
                                            for (Arc a : inpropBranch.branchArcs) {
                                                System.out.println("Arc " + a.getStartNode().getName() + " -> " + a.getEndNode().getName() + " is " + a.isBranchEnd());
                                            }
                                            */

                                        }

                                        /*
                                        for (Arc a : newPSE.partialArcs) {
                                            System.out.println("Arc " + a.getStartNode().getName() + " -> " + a.getEndNode().getName() + " is " + a.isBranchEnd());
                                        }
                                        */

                                        //newPSE = removeBrancheEndElements(newPSE);
                                        tmpListPse.add(newPSE);

                                        /*
                                        System.out.println("WhatHaveIAdd - START");
                                        for (Arc a : newPSE.partialArcs) {
                                            System.out.println(" Arc: " + a.getStartNode().getName() + " - > " + a.getEndNode().getName());
                                        }
                                        System.out.println("WhatHaveIAdd - END");
                                        */
                                    }
                                }
                                System.out.println("clean");
                                cleanArcs(listaPse);

                            }
                            listaPse = tmpListPse;
                        }
                        finished = true;
                        //cleanArcs(listaPse);
                        pseList.addAll(listaPse);

                        if(secondQuestion)
                        {
                            /*
                            for (ArrayList<BranchBasedSubnet.Branch> ls : listofInroperTbranchesForSecond) {
                                for (BranchBasedSubnet.Branch toR : ls) {
                                    if(!(ls.stream().anyMatch(x->x.startNode.equals(toR.endNode) && x.endNode.equals(toR.startNode)))) {
                                        ArrayList<BranchBasedSubnet.Branch> toDel = new ArrayList<>();
                                        for (BranchBasedSubnet.Branch td : listOfLonelyForSecond) {
                                            if (td.internalBranchElements.containsAll(toR.internalBranchElements) || toR.internalBranchElements.containsAll(td.internalBranchElements))
                                                //if(td.startNode.equals(toR.startNode) && td.endNode.equals(toR.endNode) )
                                                toDel.add(td);
                                        }
                                        listOfLonelyForSecond.removeAll(toDel);
                                    }
                                    //listOfLonelyForFirst.remove(indD)
                                }
                            }
                            */
                        }

                    } else {
                        finished = true;
                        //pse = removeBrancheEndElements(pse);
                        cleanArcs(pse);
                        pseList.add(pse);
                    }
                }
            }
            //dodać obłusgę dla przypadka gdzie nie ma p branchy - probably copy existing part of code for pbranches
        }


        /*
        for(PartialSubnetElements pse : pseList)
        {
            ArrayList<Arc> toReduce = pse.partialArcs.stream().filter(x->x.isBranchEnd()).collect(Collectors.toCollection(ArrayList::new));

            for(Arc a : toReduce)
            {
                int k  = pse.partialArcs.stream().filter(x->x.getStartNode().equals(a.getStartNode())).collect(Collectors.toCollection(ArrayList::new)).size();

                int l = pse.partialArcs.stream().filter(x->x.getEndNode().equals(a.getEndNode())).collect(Collectors.toCollection(ArrayList::new)).size();

                if(k>1 || l>1)
                {
                    pse.partialArcs.remove(a);
                }

                //if(pse.partialArcs.stream().filter(x->x.getStartNode().equals(a.getStartNode()) || x.getEndNode().equals(a.getEndNode())).collect(Collectors.toCollection(ArrayList::new)).size()>1)
                //{

               // }
            }
        }
        */
        pseList.addAll(findSingleBranchMatching(net1, net2));

        for (PartialSubnetElements pse : pseList) {
            pse.check();
        }

        //tu trzeba by dopasowanie dodać
        if (pseList.size() == 0) {
            PartialSubnetElements pse = new PartialSubnetElements(new ArrayList<>(), new ArrayList<>());
            pseList.add(pse);
            return pseList;
        }

        double max = pseList.stream().max(Comparator.comparing(x -> x.matchingValueFunction())).get().matchingValueFunction();
        return (ArrayList) pseList.stream().filter(x -> x.matchingValueFunction() == max).collect(Collectors.toList());
    }

    private ArrayList<PartialSubnetElements> findSingleBranchMatching(BranchBasedSubnet net1, BranchBasedSubnet net2) {
        ArrayList<PartialSubnetElements> psel = new ArrayList<>();
        for (BranchBasedSubnet.BranchVertex bv1 : net1.branchVertices) {
            BranchBasedSubnet bns = new BranchBasedSubnet(bv1);
            psel.addAll(comparePTBranches(bns, net2));
        }
        return psel;
    }

    private PartialSubnetElements removeBrancheEndElements(PartialSubnetElements pse) {
        ArrayList<Arc> toReduce = pse.partialArcs.stream().filter(x -> x.isBranchEnd()).collect(Collectors.toCollection(ArrayList::new));

        for (Arc a : toReduce) {
            int k = pse.partialArcs.stream().filter(x -> x.getStartNode().equals(a.getStartNode())).collect(Collectors.toCollection(ArrayList::new)).size();

            int l = pse.partialArcs.stream().filter(x -> x.getEndNode().equals(a.getEndNode())).collect(Collectors.toCollection(ArrayList::new)).size();

            if (k > 1 || l > 1) {
                //pse.partialArcs.remove(a);

                    /*
                    ArrayList<Arc> arcsS = new ArrayList<>(a.getStartNode().getOutInArcs());
                    arcsS.retainAll(pse.partialArcs);

                    ArrayList<Arc> arcsE = new ArrayList<>(a.getEndNode().getOutInArcs());
                    arcsE.retainAll(pse.partialArcs);

                    if(arcsS.size()==0){
                        pse.partialNodes.remove(a.getStartNode());
                    }

                    if(arcsE.size()==0){
                        pse.partialNodes.remove(a.getEndNode());
                    }
                    */

            }

            if (k > 1 || l > 1) {
                pse.partialArcs.remove(a);

                ArrayList<Arc> arcsS = new ArrayList<>(a.getStartNode().getOutInArcs());
                arcsS.retainAll(pse.partialArcs);

                ArrayList<Arc> arcsE = new ArrayList<>(a.getEndNode().getOutInArcs());
                arcsE.retainAll(pse.partialArcs);

                if (arcsS.size() == 1 && k > 1) {
                    pse.partialNodes.remove(a.getStartNode());
                    pse.partialArcs.remove(a);
                }

                if (arcsE.size() == 1 && l > 1) {
                    pse.partialNodes.remove(a.getEndNode());
                    pse.partialArcs.remove(a);
                }

            }

            //if(pse.partialArcs.stream().filter(x->x.getStartNode().equals(a.getStartNode()) || x.getEndNode().equals(a.getEndNode())).collect(Collectors.toCollection(ArrayList::new)).size()>1)
            //{

            // }
        }
/*
            for(Arc a : pse.partialArcs){
                a.setBranchEnd(false);
            }
        */

        return pse;
    }

    private void cleanArcs(PartialSubnetElements pse) {
        for (Arc a : pse.partialArcs) {
            a.setBranchEnd(false);
        }
    }

    private void cleanArcs(ArrayList<PartialSubnetElements> psel) {
        for (PartialSubnetElements pse : psel) {
            for (Arc a : pse.partialArcs) {
                a.setBranchEnd(false);
            }
        }
    }

    private int getBrenachVertexIndex(BranchBasedSubnet net1, BranchBasedSubnet net2, HashMap<Integer, Integer> maping, BranchBasedSubnet.BranchVertex bv) {
        Integer net2TIndex = maping.get(net1.transitions.indexOf(bv.root));
        if (net2TIndex == null) {
            return -1;
        }
        Node jebanyNode = net2.transitions.get(net2TIndex);
        ArrayList<BranchBasedSubnet.BranchVertex> tmp = net2.branchVertices.stream().filter(x -> x.root.getID() == jebanyNode.getID()).collect(Collectors.toCollection(ArrayList::new));
        if (tmp.size() == 0) {
            return -1;
        }
        int jebanyIndex = net2.branchVertices.indexOf(tmp.get(0));
        return jebanyIndex;
    }

    private HashMap<Integer, Integer> getMaping(ArrayList<RowMatch> rowMatches) {
        HashMap<Integer, Integer> map = new HashMap<>();
        for (RowMatch rm : rowMatches) {
            map.put(rm.firstNetRowIndex, rm.mapOfFNR);
            map.put(rm.seconNetRowIndex, rm.mapOfSNR);
        }
        return map;
    }

    private ArrayList<ArrayList<RowMatch>> parseParallelBranches(ArrayList<ArrayList<RowMatch>> bm) {
        ArrayList<ArrayList<RowMatch>> parsed = new ArrayList<>();
        for (ArrayList<RowMatch> rm : bm) {
            ArrayList<ArrayList<RowMatch>> toParse = new ArrayList<>();
            int count = 0;
            System.out.println("RM - : " + rm.size());
            while (rm.size() > 0) {
                System.out.println("Count : - " + count);
                count++;
                RowMatch sinleMatcg = rm.get(0);
                //rm.remove(sinleMatcg);

                //ArrayList<RowMatch> parallel = rm.stream().filter(x -> x.startNodeNet1.getID() == sinleMatcg.startNodeNet1.getID() && x.endNodeNet1.getID() == sinleMatcg.endNodeNet1.getID()).collect(Collectors.toCollection(ArrayList::new));
                // x.firstNetBranch.equals(sinleMatcg.firstNetBranch) &&
                ArrayList<RowMatch> parallel = rm.stream().filter(x -> x.startNodeNet2.equals(sinleMatcg.startNodeNet2) && x.endNodeNet2.equals(sinleMatcg.endNodeNet2)).collect(Collectors.toCollection(ArrayList::new)); //startNodeNet1.getID() && x.endNodeNet1.getID() == sinleMatcg.endNodeNet1.getID()).collect(Collectors.toCollection(ArrayList::new));
                System.out.println("Parallel size - " + parallel.size());
                System.out.println("Znalezione - " + toParse.size());
                if (count == 9) {
                    System.out.println("STart");
                }
                if (parallel.size() > 1) {

                    ArrayList<ArrayList<RowMatch>> packts = calcPacketsToAdd(parallel);

                    rm.removeAll(parallel);
                    ///

                    ArrayList<ArrayList<RowMatch>> multipleCombined = new ArrayList<>();
                    if (toParse.size() != 0) {
                        for (ArrayList<RowMatch> para : packts) {
                            ArrayList<ArrayList<RowMatch>> multiple = new ArrayList<>(toParse);
                            for (ArrayList<RowMatch> d : multiple) {
                                ArrayList<RowMatch> tmp = new ArrayList<>(d);
                                tmp.addAll(para);
                                multipleCombined.add(tmp);
                            }
                            //starczy?
                            //multipleCombined.addAll(new ArrayList<>(multiple));
                        }

                        toParse = multipleCombined;
                    } else {
                        for (ArrayList<RowMatch> p : packts) {
                            ArrayList<RowMatch> pr = new ArrayList<>();
                            pr.addAll(p);
                            toParse.add(pr);
                            ;
                        }
                    }

                } else {
                    rm.removeAll(parallel);
                    if (toParse.size() != 0) {
                        for (ArrayList<RowMatch> m : toParse) {
                            m.addAll(parallel);
                            //m.add(parallel.get(0));
                        }
                    } else {
                        toParse.add(new ArrayList<>(parallel));
                    }
                }

            }
            parsed.addAll(toParse);
        }

        return parsed;
    }

    private ArrayList<ArrayList<RowMatch>> calcPacketsToAdd(ArrayList<RowMatch> parallel) {
        ArrayList<ArrayList<RowMatch>> result = new ArrayList<>();
        ArrayList<ArrayList<RowMatch>> sorting = new ArrayList<>();
        ArrayList<ArrayList<RowMatch>> sortingProp = new ArrayList<>();
        ArrayList<RowMatch> localCoppy = new ArrayList<>(parallel);
        while (localCoppy.size() > 0) {
            ArrayList<RowMatch> p = localCoppy.stream().filter(x -> x.firstNetBranch.equals(localCoppy.get(0).firstNetBranch)).collect(Collectors.toCollection(ArrayList::new)); //startNodeNet1.getID() && x.endNodeNet1.getID() == sinleMatcg.endNodeNet1.getID()).collect(Collectors.toCollection(ArrayList::new));
            localCoppy.removeAll(p);
            sorting.add(p);
        }

        for (int j = 0; j < sorting.size(); j++) {
            sortingProp.add(new ArrayList<>());
        }

        for (int i = 0; i < sorting.get(0).size(); i++) {
            BranchBasedSubnet.Branch branch = sorting.get(0).get(i).secondNetBranch;
            for (int j = 0; j < sorting.size(); j++) {
                RowMatch rm = sorting.get(j).stream().filter(x -> x.secondNetBranch.equals(branch)).collect(Collectors.toCollection(ArrayList::new)).get(0);
                sortingProp.get(j).add(rm);
            }
        }

        //łąciński

        int[][] latin = calcLatin(Math.max(sortingProp.size(), sortingProp.get(0).size()));
        System.out.println(sortingProp.size());
        System.out.println(sortingProp.get(0).size());
        for (int k = 0; k < sortingProp.size() || k < sortingProp.get(0).size(); k++) {
            ArrayList<RowMatch> list = new ArrayList<>();
            for (int i = 0; i < sortingProp.size(); i++) {
                for (int j = 0; j < sortingProp.get(i).size(); j++) {
                    if (latin[i][j] == k) {
                        list.add(sortingProp.get(i).get(j));
                    }

                }
            }
            result.add(list);
        }

        return result;
    }

    int[][] calcLatin(int n) {
        int[][] latin = new int[n][n];
        // A variable to control the
        // rotation point.
        int k = n + 1;

        // Loop to print rows

        for (int i = 0; i < latin.length; i++) {
            int counter = i;
            for (int j = 0; j < latin[i].length; j++) {
                latin[i][j] = counter;
                System.out.print(counter);
                counter++;
                if (counter >= n) {
                    counter = 0;
                }
            }
            System.out.println("");
        }
        return latin;
    }

    private ArrayList<PartialSubnetElements> comparePaths(SubnetCalculator.Path path1, SubnetCalculator.Path path2) {
        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();

        if (!path1.isCycle && !path2.isCycle) {
            pseList.addAll(findCommonPartsInPaths(path1, path2));
        }

        //TODO reducja tylko do tego samego typu - DONE TO TEST - nie wyłæpuje niektóŕych - check
        if (!path1.isCycle && path2.isCycle) {
            //

            ArrayList<SubnetCalculator.Path> allPaths = calcPathsFromCycle(path2);
            for (SubnetCalculator.Path path : allPaths) {
                //if(path1.path.get(0).getType().equals(path.path.get(0).getType()))
                pseList.addAll(findCommonPartsInPaths(path1, path));
            }
        }

        if (path1.isCycle && !path2.isCycle) {
            ArrayList<SubnetCalculator.Path> allPaths = calcPathsFromCycle(path1);
            for (SubnetCalculator.Path path : allPaths) {
                //if(path2.path.get(0).getType().equals(path.path.get(0).getType()))
                pseList.addAll(findCommonPartsInPaths(path, path2));
            }
        }

        if (path1.isCycle && path2.isCycle) {
            ArrayList<SubnetCalculator.Path> allPaths1 = calcPathsFromCycle(path1);
            ArrayList<SubnetCalculator.Path> allPaths2 = calcPathsFromCycle(path2);
            for (SubnetCalculator.Path pathFrom1 : allPaths1) {
                for (SubnetCalculator.Path pathFrom2 : allPaths2) {
                    //if(pathFrom1.path.get(0).getType().equals(pathFrom2.path.get(0).getType()))
                    pseList.addAll(findCommonPartsInPaths(pathFrom1, pathFrom2));
                }
            }
        }

        return pseList;
    }

    private ArrayList<SubnetCalculator.Path> calcPathsFromCycle(SubnetCalculator.Path path2) {
        ArrayList<SubnetCalculator.Path> result = new ArrayList<>();

        SubnetCalculator.Path firstPath = new SubnetCalculator.Path(path2.path.get(0), path2.path.get(0), path2.path);
        firstPath.path.add(path2.startNode);
        result.add(firstPath);
        for (int i = 1; i < path2.path.size() - 1; i++) {

            ArrayList<Node> newNodeList = new ArrayList<>();//path2.path);
            ArrayList<ArrayList<Node>> twoParts = new ArrayList<>();
            ArrayList<Node> nl = new ArrayList<>();
            ArrayList<Node> nm = new ArrayList<>();
            for (int j = i; j < path2.path.size(); j++) {
                nl.add(path2.path.get(j));
            }
            for (int j = 0; j < i; j++) {
                nm.add(path2.path.get(j));
            }
            //twoParts.add(nl);
            //twoParts.add(nm);
            newNodeList.addAll(nl);
            newNodeList.addAll(nm);
            newNodeList.add(nl.get(0));
            SubnetCalculator.Path newPath = new SubnetCalculator.Path(path2.path.get(i), path2.path.get(i), newNodeList);
            result.add(newPath);
        }

        return result;
    }

    private ArrayList<PartialSubnetElements> findCommonPartsInPaths(SubnetCalculator.Path
                                                                            path1, SubnetCalculator.Path path2) {
        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();

        //ArrayList<Node> commonNodesIn = new ArrayList<>();
        //ArrayList<Arc> commonArcsIn = new ArrayList<>();

        ArrayList<ArrayList<Node>> commonNodes = new ArrayList<>();
        ArrayList<ArrayList<Arc>> commonArcs = new ArrayList<>();

        ArrayList<Node> commonN = new ArrayList<>();
        ArrayList<Arc> commonA = new ArrayList<>();

        //Czy miesza ścieżki
        if (!secondQuestion) {
            //Nie nie miesza
            if (((path1.path.get(0).getType().equals(path2.path.get(0).getType())) && (path1.path.get(path1.path.size() - 1).getType().equals(path2.path.get(path2.path.size() - 1).getType())))
                    ||
                    ((path1.path.get(0).getType().equals(path2.path.get(path2.path.size() - 1).getType())) && (path1.path.get(path1.path.size() - 1).getType().equals(path2.path.get(0).getType())))
            ) {
                pseList.addAll(choleraJakToNazwe(path1, path2, commonNodes, commonArcs, commonN, commonA));
            } else {

            }
        } else {
            //Tak miesza
            //a to poniżej zabrania mieszaniu całkowiecie tyle że o ściezkach mówimy
            //if(path1.startNode.getType().equals(path2.startNode.getType()) && path1.endNode.getType().equals(path2.endNode.getType()))
            pseList.addAll(choleraJakToNazwe(path1, path2, commonNodes, commonArcs, commonN, commonA));
        }

        // if(!secondQuestion) {

/*
        }

        else
        {
            //T-T P-P
            if((path1.path.get(0).getType().equals(path2.path.get(0).getType())) && (path1.path.get(path1.path.size() - 1).getType().equals(path2.path.get(path2.path.size() - 1).getType()))) {
                for (int j = 0; j < path1.path.size() - 1 && j < path2.path.size() - 1; j++) {
                    int arcWeight = 0;
                    ArrayList<Arc> arcFrom1 = getArc(path1.path.get(j), path1.path.get(j + 1));
                    ArrayList<Arc> arcFrom2 = getArc(path2.path.get(j), path2.path.get(j + 1));

                    if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                        arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                    }

                    if (arcWeight != 0) {
                        arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                        commonA.add(arcFrom1.get(0));
                        if (!commonN.contains(path1.path.get(j)))
                            commonN.add(path1.path.get(j));
                        if (!commonN.contains(path1.path.get(j + 1)))
                            commonN.add(path1.path.get(j + 1));
                    } else {
                        break;
                    }
                }
                commonNodes.add(commonN);
                commonArcs.add(commonA);


                commonN = new ArrayList<>();
                commonA = new ArrayList<>();

                for (int j = Math.min(path1.path.size(), path2.path.size()) - 1; j > 0; j--) {
                    int move = Math.abs(path1.path.size() - path2.path.size());
                    int firstMove = 0;
                    int secondMobe = 0;
                    if (move != 0)
                        if (path1.path.size() > path2.path.size())
                            firstMove = move;
                        else
                            secondMobe = move;

                    int arcWeight = 0;
                    ArrayList<Arc> arcFrom1 = getArc(path1.path.get(j + firstMove - 1), path1.path.get(j + firstMove));
                    ArrayList<Arc> arcFrom2 = getArc(path2.path.get(j + secondMobe - 1), path2.path.get(j + secondMobe));
                    if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                        arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());

                        if (arcWeight != 0) {
                            arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                            commonA.add(arcFrom1.get(0));
                            if (!commonN.contains(path1.path.get(j + firstMove)))
                                commonN.add(path1.path.get(j + firstMove));
                            if (!commonN.contains(path1.path.get(j + firstMove - 1)))
                                commonN.add(path1.path.get(j + firstMove - 1));
                        } else {
                            break;
                        }
                    }
                }
                commonNodes.add(commonN);
                commonArcs.add(commonA);
            }


            //TODO reduce doubles
        }
*/


        return pseList;
    }

    private ArrayList<PartialSubnetElements> choleraJakToNazwe(SubnetCalculator.Path path1, SubnetCalculator.Path path2, ArrayList<ArrayList<Node>> commonNodes, ArrayList<ArrayList<Arc>> commonArcs, ArrayList<Node> commonN, ArrayList<Arc> commonA) {
        ArrayList<HashMap> nml = new ArrayList<>();
        ArrayList<HashMap> aml = new ArrayList<>();
        HashMap<Node, Node> nm = new HashMap<>();
        HashMap<Arc, Arc> am = new HashMap<>();

        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();
        if (path1.path.get(0).getType().equals(path2.path.get(0).getType())) {
            //OBA ZACZYNAJÆ SIĚ OD NODA TEGO SAMEGO TYPU
            for (int j = 0; j < path1.path.size() - 1 && j < path2.path.size() - 1; j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(path1.path.get(j), path1.path.get(j + 1));
                ArrayList<Arc> arcFrom2 = getArc(path2.path.get(j), path2.path.get(j + 1));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    if (!commonN.contains(path1.path.get(j))) {
                        commonN.add(path1.path.get(j));
                        nm.put(path1.path.get(j), path2.path.get(j));
                    }
                    if (!commonN.contains(path1.path.get(j + 1)) && !path2.path.get(j + 1).equals(path2.path.get(0))) {
                        commonN.add(path1.path.get(j + 1));
                        nm.put(path1.path.get(j + 1), path2.path.get(j + 1));
                        commonA.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                    //if(commonN.contains(arcFrom1.get(0).getStartNode()) && commonN.contains(arcFrom1.get(0).getEndNode()) && !arcFrom2.isEmpty())
                    //{
                    if (path1.path.get(0).equals(arcFrom1.get(0).getEndNode()) && path2.path.get(0).equals(arcFrom2.get(0).getEndNode())) {
                        commonA.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                    //}
                } else {
                    break;
                }
            }
            nml.add(nm);
            aml.add(am);
            nm = new HashMap<>();
            am = new HashMap<>();
            commonNodes.add(commonN);
            commonArcs.add(commonA);
        } else {
            //przypadek wymijajæcy I element z I sieci
            ArrayList<Node> shortPath1 = new ArrayList<>(path1.path);
            shortPath1.remove(0);

            if (!firstQuestion) {
                commonN.add(shortPath1.get(0));
                nm.put(shortPath1.get(0), path2.path.get(0));
            }

            for (int j = 0; j < shortPath1.size() - 1 && j < path2.path.size() - 1; j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(shortPath1.get(j), shortPath1.get(j + 1));
                ArrayList<Arc> arcFrom2 = getArc(path2.path.get(j), path2.path.get(j + 1));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    if (!commonN.contains(shortPath1.get(j))) {
                        commonN.add(shortPath1.get(j));
                        nm.put(shortPath1.get(j), path2.path.get(j));
                    }
                    if (!commonN.contains(shortPath1.get(j + 1)) && !path2.path.get(j + 1).equals(path2.path.get(0))) {
                        commonN.add(shortPath1.get(j + 1));
                        nm.put(shortPath1.get(j + 1), path2.path.get(j + 1));
                        commonA.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                    if (path1.path.get(0).equals(arcFrom1.get(0).getEndNode()) && path2.path.get(0).equals(arcFrom2.get(0).getEndNode())) {
                        commonA.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                    /*
                    if(commonN.contains(arcFrom1.get(0).getStartNode()) && commonN.contains(arcFrom1.get(0).getEndNode()) && !arcFrom2.isEmpty())
                    {
                        commonA.add(arcFrom1.get(0));
                    }
                    */
                } else {
                    break;
                }
            }

            commonNodes.add(commonN);
            commonArcs.add(commonA);

            nml.add(nm);
            aml.add(am);
            nm = new HashMap<>();
            am = new HashMap<>();

            commonN = new ArrayList<>();
            commonA = new ArrayList<>();
            //porównanie od końca ściežki

            //przypadek wymijajæcy I element z II sieci

            ArrayList<Node> shortPath2 = new ArrayList<>(path2.path);
            shortPath2.remove(0);

            for (int j = 0; j < path1.path.size() - 1 && j < shortPath2.size() - 1; j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(path1.path.get(j), path1.path.get(j + 1));
                ArrayList<Arc> arcFrom2 = getArc(shortPath2.get(j), shortPath2.get(j + 1));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    if (!commonN.contains(path1.path.get(j))) {
                        commonN.add(path1.path.get(j));
                        nm.put(path1.path.get(j), shortPath2.get(j));
                    }
                    if (!commonN.contains(path1.path.get(j + 1)) && !path2.path.get(j + 1).equals(path2.path.get(0))) {
                        commonN.add(path1.path.get(j + 1));
                        nm.put(path1.path.get(j + 1), shortPath2.get(j + 1));
                        commonA.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                    if (path1.path.get(0).equals(arcFrom1.get(0).getEndNode()) && path2.path.get(0).equals(arcFrom2.get(0).getEndNode())) {
                        commonA.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                    /*if(commonN.contains(arcFrom1.get(0).getStartNode()) && commonN.contains(arcFrom1.get(0).getEndNode()) && !arcFrom2.isEmpty())
                    {
                        commonA.add(arcFrom1.get(0));
                    }*/
                } else {
                    break;
                }
            }
            commonNodes.add(commonN);
            commonArcs.add(commonA);

            nml.add(nm);
            aml.add(am);
            nm = new HashMap<>();
            am = new HashMap<>();
        }

        //Backword

        commonN = new ArrayList<>();
        commonA = new ArrayList<>();

        if (path1.path.get(path1.path.size() - 1).getType().equals(path2.path.get(path2.path.size() - 1).getType())) {

            for (int j = Math.min(path1.path.size(), path2.path.size()) - 1; j > 0; j--) {
                int move = Math.abs(path1.path.size() - path2.path.size());
                int firstMove = 0;
                int secondMobe = 0;
                if (move != 0)
                    if (path1.path.size() > path2.path.size())
                        firstMove = move;
                    else
                        secondMobe = move;

                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(path1.path.get(j + firstMove - 1), path1.path.get(j + firstMove));
                ArrayList<Arc> arcFrom2 = getArc(path2.path.get(j + secondMobe - 1), path2.path.get(j + secondMobe));
                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());

                    if (arcWeight != 0) {
                        arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                        if (!commonN.contains(path1.path.get(j + firstMove))) {
                            commonN.add(path1.path.get(j + firstMove));
                            nm.put(path1.path.get(j + firstMove), path2.path.get(j + secondMobe));
                        }
                        if (!commonN.contains(path1.path.get(j + firstMove - 1)) && !path2.path.get(j + secondMobe - 1).equals(path2.path.get(0))) {
                            commonN.add(path1.path.get(j + firstMove - 1));
                            nm.put(path1.path.get(j + firstMove - 1), path2.path.get(j + secondMobe - 1));
                            commonA.add(arcFrom1.get(0));
                            am.put(arcFrom1.get(0), arcFrom2.get(0));
                        }
                        if (path1.path.get(0).equals(arcFrom1.get(0).getEndNode()) && path2.path.get(0).equals(arcFrom2.get(0).getEndNode())) {
                            commonA.add(arcFrom1.get(0));
                            am.put(arcFrom1.get(0), arcFrom2.get(0));
                        }
                        /*
                        if(commonN.contains(arcFrom1.get(0).getStartNode()) && commonN.contains(arcFrom1.get(0).getEndNode()) && !arcFrom2.isEmpty())
                        {
                            commonA.add(arcFrom1.get(0));
                        }*/

                    } else {
                        break;
                    }
                }
            }
            commonNodes.add(commonN);
            commonArcs.add(commonA);

            nml.add(nm);
            aml.add(am);
            nm = new HashMap<>();
            am = new HashMap<>();
        } else {
            ArrayList<Node> shortPath1 = new ArrayList<>(path1.path);
            shortPath1.remove(shortPath1.size() - 1);

            if (!firstQuestion)
                commonN.add(shortPath1.get(shortPath1.size() - 1));
            //-1
            for (int j = Math.min(shortPath1.size(), path2.path.size()) - 1; j > 0; j--) {
                int move = Math.abs(shortPath1.size() - path2.path.size());
                int firstMove = 0;
                int secondMobe = 0;
                if (move != 0)
                    if (shortPath1.size() > path2.path.size())
                        firstMove = move;
                    else
                        secondMobe = move;

                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(shortPath1.get(j + firstMove - 1), shortPath1.get(j + firstMove));
                ArrayList<Arc> arcFrom2 = getArc(path2.path.get(j + secondMobe - 1), path2.path.get(j + secondMobe));
                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());

                    if (arcWeight != 0) {
                        arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                        if (!commonN.contains(shortPath1.get(j + firstMove))) {
                            commonN.add(shortPath1.get(j + firstMove));
                            nm.put(shortPath1.get(j + firstMove), path2.path.get(j + secondMobe));
                        }
                        if (!commonN.contains(shortPath1.get(j + firstMove - 1)) && !path2.path.get(j + secondMobe - 1).equals(path2.path.get(0))) {
                            commonN.add(shortPath1.get(j + firstMove - 1));
                            nm.put(shortPath1.get(j + firstMove - 1), path2.path.get(j + secondMobe - 1));
                            commonA.add(arcFrom1.get(0));
                            am.put(arcFrom1.get(0), arcFrom2.get(0));
                        }
                        if (path1.path.get(0).equals(arcFrom1.get(0).getEndNode()) && path2.path.get(0).equals(arcFrom2.get(0).getEndNode())) {
                            commonA.add(arcFrom1.get(0));
                            am.put(arcFrom1.get(0), arcFrom2.get(0));
                        }
                        /*
                        if(commonN.contains(arcFrom1.get(0).getStartNode()) && commonN.contains(arcFrom1.get(0).getEndNode()) && !arcFrom2.isEmpty())
                        {
                            commonA.add(arcFrom1.get(0));
                        }*/

                    } else {
                        break;
                    }
                }
            }

            commonNodes.add(commonN);
            commonArcs.add(commonA);

            commonN = new ArrayList<>();
            commonA = new ArrayList<>();
            nml.add(nm);
            aml.add(am);
            nm = new HashMap<>();
            am = new HashMap<>();
            //1-
            ArrayList<Node> shortPath2 = new ArrayList<>(path2.path);
            shortPath2.remove(shortPath2.size() - 1);

            for (int j = Math.min(path1.path.size(), shortPath2.size()) - 1; j > 0; j--) {
                int move = Math.abs(path1.path.size() - shortPath2.size());
                int firstMove = 0;
                int secondMobe = 0;
                if (move != 0)
                    if (path1.path.size() > shortPath2.size())
                        firstMove = move;
                    else
                        secondMobe = move;

                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(path1.path.get(j + firstMove - 1), path1.path.get(j + firstMove));
                ArrayList<Arc> arcFrom2 = getArc(shortPath2.get(j + secondMobe - 1), shortPath2.get(j + secondMobe));
                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());

                    if (arcWeight != 0) {
                        arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                        if (!commonN.contains(path1.path.get(j + firstMove))) {
                            commonN.add(path1.path.get(j + firstMove));
                            nm.put(path1.path.get(j + firstMove), shortPath2.get(j + secondMobe));
                        }
                        if (!commonN.contains(path1.path.get(j + firstMove - 1)) && !shortPath2.get(j + secondMobe - 1).equals(shortPath2.get(0))) {
                            commonN.add(path1.path.get(j + firstMove - 1));
                            nm.put(path1.path.get(j + firstMove - 1), shortPath2.get(j + secondMobe - 1));
                            commonA.add(arcFrom1.get(0));
                            am.put(arcFrom1.get(0), arcFrom2.get(0));
                        }
                        if (path1.path.get(0).equals(arcFrom1.get(0).getEndNode()) && shortPath2.get(0).equals(arcFrom2.get(0).getEndNode())) {
                            commonA.add(arcFrom1.get(0));
                            am.put(arcFrom1.get(0), arcFrom2.get(0));
                        }
                        /*
                        if(commonN.contains(arcFrom1.get(0).getStartNode()) && commonN.contains(arcFrom1.get(0).getEndNode()) && !arcFrom2.isEmpty())
                        {
                            commonA.add(arcFrom1.get(0));
                        }
                        */
                    } else {
                        break;
                    }
                }
            }

            commonNodes.add(commonN);
            commonArcs.add(commonA);
            nml.add(nm);
            aml.add(am);
            nm = new HashMap<>();
            am = new HashMap<>();
        }
        for (int i = 0; i < commonNodes.size(); i++) {

            PartialSubnetElements pse = new PartialSubnetElements(commonNodes.get(i), commonArcs.get(i));
            pse.nodesMap = nml.get(i);
            pse.arcsMap = aml.get(i);
            if (pse.partialNodes.size() > 0)
                pseList.add(pse);
        }
        return pseList;
    }

    private ArrayList<PartialSubnetElements> compareTbranches
            (ArrayList<BranchBasedSubnet.Branch> firstNetBranch, ArrayList<BranchBasedSubnet.Branch> secondNetBranch, Node
                    root) {
        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();

        return pseList;
    }

    private PartialSubnetElements comparePbranches
            (ArrayList<BranchBasedSubnet.Branch> pbranches1, ArrayList<BranchBasedSubnet.Branch> pbranches2, Node
                    root1, Node root2) {
        HashMap<Node, Node> nm = new HashMap<>();
        HashMap<Arc, Arc> am = new HashMap<>();

        //before
        Collections.sort(pbranches1, new BranchBasedSubnet.Branch.LenghtSort());
        Collections.sort(pbranches2, new BranchBasedSubnet.Branch.LenghtSort());

        Collections.reverse(pbranches1);
        Collections.reverse(pbranches2);

        ArrayList<BranchBasedSubnet.Branch> incomingBranches1 = getIncomingBranches(pbranches1, root1);
        ArrayList<BranchBasedSubnet.Branch> incomingBranches2 = getIncomingBranches(pbranches2, root2);

        ArrayList<BranchBasedSubnet.Branch> outgoingBranches1 = getOutgoingBranches(pbranches1, root1);
        ArrayList<BranchBasedSubnet.Branch> outgoingBranches2 = getOutgoingBranches(pbranches2, root2);
        //or after srot
        PartialSubnetElements pse = getPartialSubnetElements(pbranches1, pbranches2, root1, root2, nm, am, incomingBranches1, incomingBranches2, outgoingBranches1, outgoingBranches2);

        return pse;
    }

    private PartialSubnetElements getPartialSubnetElements(ArrayList<BranchBasedSubnet.Branch> pbranches1, ArrayList<BranchBasedSubnet.Branch> pbranches2, Node root1, Node root2, HashMap<Node, Node> nm, HashMap<Arc, Arc> am, ArrayList<BranchBasedSubnet.Branch> incomingBranches1, ArrayList<BranchBasedSubnet.Branch> incomingBranches2, ArrayList<BranchBasedSubnet.Branch> outgoingBranches1, ArrayList<BranchBasedSubnet.Branch> outgoingBranches2) {
        ArrayList<Node> commonNodes = new ArrayList<>();
        ArrayList<Arc> commonArcs = new ArrayList<>();

        if (pbranches1.size() == 0 && pbranches2.size() == 0) {
            commonNodes.add(root1);
            //if(!thirdQuestion)
                return new PartialSubnetElements(commonNodes, commonArcs);
            //else
            //{
            //    ArrayList<BranchBasedSubnet.Branch> allCycyles = pbranches1.stream().filter(x->x.isCycle).collect(Collectors.toCollection(ArrayList::new));

            //}
        }

        if (pbranches1.size() == 0 && pbranches2.size() > 0) {
            commonNodes.add(root1);
            //if(!thirdQuestion)
                return new PartialSubnetElements(commonNodes, commonArcs);
            //else
            //{
            //    ArrayList<BranchBasedSubnet.Branch> allCycyles = pbranches2.stream().filter(x->x.isCycle).collect(Collectors.toCollection(ArrayList::new));

            //}
        }

        if (pbranches1.size() > 0 && pbranches2.size() == 0) {
            commonNodes.add(root2);
            return new PartialSubnetElements(commonNodes, commonArcs);
        }


        ArrayList<Node> usedFromsecond = new ArrayList<>();

        //cycle step
        int cF = 0;
        int cS = 0;

        for (int i = 0; i + cF < incomingBranches1.size() && i + cS < incomingBranches2.size(); i++) {

            //is cycle check and get better

            BranchBasedSubnet.Branch inf = incomingBranches1.get(i);
            BranchBasedSubnet.Branch ins = incomingBranches2.get(i);

            /*
            if(isCycle(incomingBranches1.get(i)))
            {
                if(isIncomingCycleBranch(incomingBranches1.get(i))) {
                    inf = incomingBranches1.get(i);
                    cF++;
                }
                else
                {
                    inf = incomingBranches1.get(i+1);
                    cF++;
                }
            }
            */
/*
            if(isCycle(incomingBranches2.get(i)))
            {
                if(isIncomingCycleBranch(incomingBranches2.get(i))) {
                    ins = incomingBranches2.get(i);
                    cS++;
                }
                else
                {
                    ins = incomingBranches2.get(i+1);
                    cS++;
                }
            }
            */

            //compare arcs weight
            int move = Math.abs(inf.branchElements.size() - ins.branchElements.size());
            int firstMove = 0;
            int secondMobe = 0;

            if (move != 0)
                if (inf.branchElements.size() > ins.branchElements.size())
                    firstMove = move;
                else
                    secondMobe = move;

            for (int k = 0; k < inf.branchElements.size(); k++) {
                System.out.println(inf.branchElements.get(k).getName());
            }

            System.out.println("root " + root1.getName() + " z brancha " + inf.branchElements.get(0 + firstMove).getName());


            for (int j = 0; j < Math.min(inf.branchArcs.size(), ins.branchArcs.size()); j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = new ArrayList<>();
                ArrayList<Arc> arcFrom2 = new ArrayList<>();


                arcFrom1.add(inf.branchArcs.get(j));
                arcFrom2.add(ins.branchArcs.get(j));
                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    System.out.println("Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());

                    if (!commonNodes.contains(inf.branchArcs.get(j).getStartNode()) && !usedFromsecond.contains(ins.branchArcs.get(j).getStartNode())) {
                        commonNodes.add(inf.branchArcs.get(j).getStartNode());
                        nm.put(inf.branchArcs.get(j).getStartNode(), ins.branchArcs.get(j).getStartNode());
                        usedFromsecond.add(ins.branchArcs.get(j).getStartNode());
                    }
                    if (!commonNodes.contains(inf.branchArcs.get(j).getEndNode()) && !usedFromsecond.contains(ins.branchArcs.get(j).getEndNode())) {
                        commonNodes.add(inf.branchArcs.get(j).getEndNode());
                        nm.put(inf.branchArcs.get(j).getEndNode(), ins.branchArcs.get(j).getEndNode());
                        usedFromsecond.add(ins.branchArcs.get(j).getEndNode());
                    }


                    boolean fnlc = false;
                    boolean snlc = false;
                    //loopchecker
                    if (inf.branchArcs.get(j).getStartNode().equals(root1)) {
                        fnlc = true;
                    }

                    if (ins.branchArcs.get(j).getStartNode().equals(root2)) {
                        snlc = true;
                    }


                    if (!commonArcs.contains(arcFrom1.get(0)) && commonNodes.contains(arcFrom1.get(0).getStartNode()) && commonNodes.contains(arcFrom1.get(0).getEndNode()) && nm.values().contains(arcFrom2.get(0).getStartNode()) && nm.values().contains(arcFrom2.get(0).getEndNode())) {
                        System.out.println("in Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());

                        //if((snlc && fnlc ) || (!snlc && !fnlc)) {
                        commonArcs.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                        // }
                    }

                } else {
                    break;
                }
            }

            /* OLD
            for (int j = 0; j < Math.min(inf.branchElements.size(), ins.branchElements.size()) - 1; j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(inf.branchElements.get(j + firstMove), inf.branchElements.get(j + firstMove + 1));
                ArrayList<Arc> arcFrom2 = getArc(ins.branchElements.get(j + secondMobe), ins.branchElements.get(j + secondMobe + 1));
                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    System.out.println("Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());

                    if (!commonNodes.contains(inf.branchElements.get(j + firstMove)) && !usedFromsecond.contains(ins.branchElements.get(j + secondMobe))) {
                        commonNodes.add(inf.branchElements.get(j + firstMove));
                        nm.put(inf.branchElements.get(j + firstMove), ins.branchElements.get(j + secondMobe));
                        usedFromsecond.add(ins.branchElements.get(j + secondMobe));
                    }
                    if (!commonNodes.contains(inf.branchElements.get(j + firstMove + 1)) && !usedFromsecond.contains(ins.branchElements.get(j + secondMobe + 1))) {
                        commonNodes.add(inf.branchElements.get(j + firstMove + 1));
                        nm.put(inf.branchElements.get(j + firstMove + 1), ins.branchElements.get(j + secondMobe + 1));
                        usedFromsecond.add(ins.branchElements.get(j + secondMobe + 1));
                    }

                    if (!commonArcs.contains(arcFrom1.get(0)) && commonNodes.contains(arcFrom1.get(0).getStartNode()) && commonNodes.contains(arcFrom1.get(0).getEndNode())  && nm.values().contains(arcFrom2.get(0).getStartNode() ) && nm.values().contains(arcFrom2.get(0).getEndNode())) {
                        System.out.println("in Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());
                        commonArcs.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }

                } else {
                    break;
                }
            }
            */

        }

        //TODO ERROR!!


        for (int i = 0; i < outgoingBranches1.size() && i < outgoingBranches2.size(); i++) {
            // ArrayList<Node> usedFromsecond = new ArrayList<>();


            BranchBasedSubnet.Branch onf = outgoingBranches1.get(i);
            BranchBasedSubnet.Branch ons = outgoingBranches2.get(i);

            System.out.println("out root " + root1.getName() + " z brancha " + outgoingBranches1.get(i).branchElements.get(0).getName());

            for (int j = 0; j < Math.min(onf.branchArcs.size(), ons.branchArcs.size()); j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = new ArrayList<>();
                ArrayList<Arc> arcFrom2 = new ArrayList<>();


                arcFrom1.add(onf.branchArcs.get(j));
                arcFrom2.add(ons.branchArcs.get(j));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    if (!commonNodes.contains(onf.branchArcs.get(j).getStartNode()) && !usedFromsecond.contains(ons.branchArcs.get(j).getStartNode())) {
                        commonNodes.add(onf.branchArcs.get(j).getStartNode());
                        nm.put(onf.branchArcs.get(j).getStartNode(), ons.branchArcs.get(j).getStartNode());
                        usedFromsecond.add(ons.branchArcs.get(j).getStartNode());
                    }
                    if (!commonNodes.contains(onf.branchArcs.get(j).getEndNode()) && !usedFromsecond.contains(ons.branchArcs.get(j).getEndNode())) {
                        commonNodes.add(onf.branchArcs.get(j).getEndNode());
                        nm.put(onf.branchArcs.get(j).getEndNode(), ons.branchArcs.get(j).getEndNode());
                        usedFromsecond.add(ons.branchArcs.get(j).getEndNode());
                    }

                    boolean fnlc = false;
                    boolean snlc = false;
                    //loopchecker
                    if (onf.branchArcs.get(j).getEndNode().equals(root1)) {
                        fnlc = true;
                    }

                    if (ons.branchArcs.get(j).getEndNode().equals(root2)) {
                        snlc = true;
                    }

                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    //update warunku
                    // a co gdy jest pętlą w arc2....

                    if (!commonArcs.contains(arcFrom1.get(0)) &&
                            commonNodes.contains(arcFrom1.get(0).getStartNode()) &&
                            commonNodes.contains(arcFrom1.get(0).getEndNode()) &&
                            nm.values().contains(arcFrom2.get(0).getStartNode()) &&
                            nm.values().contains(arcFrom2.get(0).getEndNode())) {//&& (ons.branchArcs.stream().anyMatch(x->(x.getStartNode().equals(getKey(nm,arcFrom2.get(0).getStartNode())) && x.getEndNode().equals(getKey(nm,arcFrom2.get(0).getEndNode()))       )))){//|| (x.getStartNode().equals() && x.getEndNode().equals() ) ))) {
                        System.out.println("out Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());

                        //TODO Loop end
                        if ((snlc && fnlc) || (!snlc && !fnlc)) {
                            commonArcs.add(arcFrom1.get(0));
                            am.put(arcFrom1.get(0), arcFrom2.get(0));
                        }
                    }
                } else {
                    break;
                }

            }


            /*

            for (int j = 0; j < outgoingBranches1.get(i).branchElements.size() - 1 && j < outgoingBranches2.get(i).branchElements.size() - 1; j++) {
                int arcWeight = 0;
                //wariant z równym
                ArrayList<Arc> arcFrom1 = getArc(outgoingBranches1.get(i).branchElements.get(j), outgoingBranches1.get(i).branchElements.get(j + 1));
                ArrayList<Arc> arcFrom2 = getArc(outgoingBranches2.get(i).branchElements.get(j), outgoingBranches2.get(i).branchElements.get(j + 1));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    if (!commonNodes.contains(outgoingBranches1.get(i).branchElements.get(j)) && !usedFromsecond.contains(outgoingBranches2.get(i).branchElements.get(j))) {
                        commonNodes.add(outgoingBranches1.get(i).branchElements.get(j));
                        nm.put(outgoingBranches1.get(i).branchElements.get(j), outgoingBranches2.get(i).branchElements.get(j));
                        usedFromsecond.add(outgoingBranches2.get(i).branchElements.get(j));
                    }
                    if (!commonNodes.contains(outgoingBranches1.get(i).branchElements.get(j + 1)) && !usedFromsecond.contains(outgoingBranches2.get(i).branchElements.get(j + 1))) {
                        commonNodes.add(outgoingBranches1.get(i).branchElements.get(j + 1));
                        nm.put(outgoingBranches1.get(i).branchElements.get(j + 1), outgoingBranches2.get(i).branchElements.get(j + 1));
                        usedFromsecond.add(outgoingBranches2.get(i).branchElements.get(j + 1));

                    }
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    //update warunku
                    // a co gdy jest pętlą w arc2....
                    if(!commonArcs.contains(arcFrom1.get(0)) && commonNodes.contains(arcFrom1.get(0).getStartNode()) && commonNodes.contains(arcFrom1.get(0).getEndNode()))
                    {

                    }

                    if (!commonArcs.contains(arcFrom1.get(0)) && commonNodes.contains(arcFrom1.get(0).getStartNode()) && commonNodes.contains(arcFrom1.get(0).getEndNode())  && nm.values().contains(arcFrom2.get(0).getStartNode() ) && nm.values().contains(arcFrom2.get(0).getEndNode() ) ){//&& (ons.branchArcs.stream().anyMatch(x->(x.getStartNode().equals(getKey(nm,arcFrom2.get(0).getStartNode())) && x.getEndNode().equals(getKey(nm,arcFrom2.get(0).getEndNode()))       )))){//|| (x.getStartNode().equals() && x.getEndNode().equals() ) ))) {
                        System.out.println("out Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());
                        commonArcs.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                } else {
                    break;
                }

            }*/
        }
        /*
            for (Arc a : commonArcs
            ) {
                System.out.println("Arc : " + a.getStartNode().getType() + " " +a.getStartNode().getName() + " - > "  + a.getEndNode().getType() + " " +a.getEndNode().getName());
            }

            for (Node n :commonNodes
            ) {
                System.out.println("Node " + n.getName());
            }
            */

        return new PartialSubnetElements(commonNodes, commonArcs, nm, am);
    }

    public <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private boolean isIncomingCycleBranch(BranchBasedSubnet.Branch branch) {

        return branch.branchArcs.get(0).getEndNode().equals(branch.startNode);
    }

    private boolean isCycle(BranchBasedSubnet.Branch branch) {

        return branch.startNode.equals(branch.endNode);
    }

    private PartialSubnetElements pBranchesCycyle(ArrayList<BranchBasedSubnet.Branch> pbranches1, ArrayList<BranchBasedSubnet.Branch> pbranches2, Node
            root1, Node root2) {
        HashMap<Node, Node> nm = new HashMap<>();
        HashMap<Arc, Arc> am = new HashMap<>();

        //before
        Collections.sort(pbranches1, new BranchBasedSubnet.Branch.LenghtSort());
        Collections.sort(pbranches2, new BranchBasedSubnet.Branch.LenghtSort());

        Collections.reverse(pbranches1);
        Collections.reverse(pbranches2);

        ArrayList<BranchBasedSubnet.Branch> incomingBranches1 = getIncomingBranches(pbranches1, root1);
        ArrayList<BranchBasedSubnet.Branch> incomingBranches2 = getIncomingBranches(pbranches2, root2);

        ArrayList<BranchBasedSubnet.Branch> outgoingBranches1 = getOutgoingBranches(pbranches1, root1);
        ArrayList<BranchBasedSubnet.Branch> outgoingBranches2 = getOutgoingBranches(pbranches2, root2);
        //or after srot


        ArrayList<Node> commonNodes = new ArrayList<>();
        ArrayList<Arc> commonArcs = new ArrayList<>();

        if (pbranches1.size() == 0 && pbranches2.size() == 0) {
            commonNodes.add(root1);
            return new PartialSubnetElements(commonNodes, commonArcs);
        }

        if (pbranches1.size() == 0 && pbranches2.size() > 0) {
            commonNodes.add(root1);
            return new PartialSubnetElements(commonNodes, commonArcs);
        }

        if (pbranches1.size() > 0 && pbranches2.size() == 0) {
            commonNodes.add(root1);
            return new PartialSubnetElements(commonNodes, commonArcs);
        }


        ArrayList<Node> usedFromsecond = new ArrayList<>();
        for (int i = 0; i < incomingBranches1.size() && i < incomingBranches2.size(); i++) {
            //compare arcs weight
            int move = Math.abs(incomingBranches1.get(i).branchElements.size() - incomingBranches2.get(i).branchElements.size());
            int firstMove = 0;
            int secondMobe = 0;

            if (move != 0)
                if (incomingBranches1.get(i).branchElements.size() > incomingBranches2.get(i).branchElements.size())
                    firstMove = move;
                else
                    secondMobe = move;

            for (int k = 0; k < incomingBranches1.get(i).branchElements.size(); k++) {
                System.out.println(incomingBranches1.get(i).branchElements.get(k).getName());
            }


            System.out.println("root " + root1.getName() + " z brancha " + incomingBranches1.get(i).branchElements.get(0 + firstMove).getName());

            for (int j = 0; j < Math.min(incomingBranches1.get(i).branchElements.size(), incomingBranches2.get(i).branchElements.size()) - 1; j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(incomingBranches1.get(i).branchElements.get(j + firstMove), incomingBranches1.get(i).branchElements.get(j + firstMove + 1));
                ArrayList<Arc> arcFrom2 = getArc(incomingBranches2.get(i).branchElements.get(j + secondMobe), incomingBranches2.get(i).branchElements.get(j + secondMobe + 1));
                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    System.out.println("Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());

                    if (!commonNodes.contains(incomingBranches1.get(i).branchElements.get(j + firstMove)) && !usedFromsecond.contains(incomingBranches2.get(i).branchElements.get(j + secondMobe))) {
                        commonNodes.add(incomingBranches1.get(i).branchElements.get(j + firstMove));
                        nm.put(incomingBranches1.get(i).branchElements.get(j + firstMove), incomingBranches2.get(i).branchElements.get(j + secondMobe));
                        usedFromsecond.add(incomingBranches2.get(i).branchElements.get(j + secondMobe));
                    }
                    if (!commonNodes.contains(incomingBranches1.get(i).branchElements.get(j + firstMove + 1)) && !usedFromsecond.contains(incomingBranches2.get(i).branchElements.get(j + secondMobe + 1))) {
                        commonNodes.add(incomingBranches1.get(i).branchElements.get(j + firstMove + 1));
                        nm.put(incomingBranches1.get(i).branchElements.get(j + firstMove + 1), incomingBranches2.get(i).branchElements.get(j + secondMobe + 1));
                        usedFromsecond.add(incomingBranches2.get(i).branchElements.get(j + secondMobe + 1));
                    }

                    if (!commonArcs.contains(arcFrom1.get(0)) && commonNodes.contains(arcFrom1.get(0).getStartNode()) && commonNodes.contains(arcFrom1.get(0).getEndNode())) {
                        System.out.println("in Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());
                        commonArcs.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }

                } else {
                    break;
                }
            }

        }

        //TODO ERROR!!
        for (int i = 0; i < outgoingBranches1.size() && i < outgoingBranches2.size(); i++) {
            // ArrayList<Node> usedFromsecond = new ArrayList<>();

            System.out.println("out root " + root1.getName() + " z brancha " + outgoingBranches1.get(i).branchElements.get(0).getName());
            for (int j = 0; j < outgoingBranches1.get(i).branchElements.size() - 1 && j < outgoingBranches2.get(i).branchElements.size() - 1; j++) {
                int arcWeight = 0;
                //wariant z równym
                ArrayList<Arc> arcFrom1 = getArc(outgoingBranches1.get(i).branchElements.get(j), outgoingBranches1.get(i).branchElements.get(j + 1));
                ArrayList<Arc> arcFrom2 = getArc(outgoingBranches2.get(i).branchElements.get(j), outgoingBranches2.get(i).branchElements.get(j + 1));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    if (!commonNodes.contains(outgoingBranches1.get(i).branchElements.get(j)) && !usedFromsecond.contains(outgoingBranches2.get(i).branchElements.get(j))) {
                        commonNodes.add(outgoingBranches1.get(i).branchElements.get(j));
                        nm.put(outgoingBranches1.get(i).branchElements.get(j), outgoingBranches2.get(i).branchElements.get(j));
                        usedFromsecond.add(outgoingBranches2.get(i).branchElements.get(j));
                    }
                    if (!commonNodes.contains(outgoingBranches1.get(i).branchElements.get(j + 1)) && !usedFromsecond.contains(outgoingBranches2.get(i).branchElements.get(j + 1))) {
                        commonNodes.add(outgoingBranches1.get(i).branchElements.get(j + 1));
                        nm.put(outgoingBranches1.get(i).branchElements.get(j + 1), outgoingBranches2.get(i).branchElements.get(j + 1));
                        usedFromsecond.add(outgoingBranches2.get(i).branchElements.get(j + 1));

                    }
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    //update warunku
                    if (!commonArcs.contains(arcFrom1.get(0)) && commonNodes.contains(arcFrom1.get(0).getStartNode()) && commonNodes.contains(arcFrom1.get(0).getEndNode())) {
                        System.out.println("out Arc : " + arcFrom1.get(0).getStartNode().getType() + " " + arcFrom1.get(0).getStartNode().getName() + " - > " + arcFrom1.get(0).getEndNode().getType() + " " + arcFrom1.get(0).getEndNode().getName());
                        commonArcs.add(arcFrom1.get(0));
                        am.put(arcFrom1.get(0), arcFrom2.get(0));
                    }
                } else {
                    break;
                }
            }
        }
        /*
            for (Arc a : commonArcs
            ) {
                System.out.println("Arc : " + a.getStartNode().getType() + " " +a.getStartNode().getName() + " - > "  + a.getEndNode().getType() + " " +a.getEndNode().getName());
            }

            for (Node n :commonNodes
            ) {
                System.out.println("Node " + n.getName());
            }
            */

        return new PartialSubnetElements(commonNodes, commonArcs, nm, am);
    }

    private ArrayList<BranchBasedSubnet.Branch> getIncomingBranches
            (ArrayList<BranchBasedSubnet.Branch> list, Node root) {
        return list.stream().filter(x -> (x.endNode.getID() == root.getID() && (!x.startNode.equals(x.endNode))) || (x.startNode.equals(x.endNode) && getDirection(x))).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<BranchBasedSubnet.Branch> getOutgoingBranches
            (ArrayList<BranchBasedSubnet.Branch> list, Node root) {
        return list.stream().filter(x -> (x.startNode.getID() == root.getID() && (!x.startNode.equals(x.endNode))) || (x.startNode.equals(x.endNode) && !getDirection(x))).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<BranchBasedSubnet.Branch> getIncomingBranchesLoop
            (ArrayList<BranchBasedSubnet.Branch> list, Node root) {
        return list.stream().filter(x -> x.endNode.getID() == root.getID() && getDirection(x)).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<BranchBasedSubnet.Branch> getOutgoingBranchesLoop
            (ArrayList<BranchBasedSubnet.Branch> list, Node root) {
        return list.stream().filter(x -> x.startNode.getID() == root.getID() && !getDirection(x)).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Arc> getArc(Node start, Node end) {
        ArrayList<Arc> sio = start.getOutInArcs();
        ArrayList<Arc> eio = end.getOutInArcs();
        ArrayList<Arc> listOfArcs = new ArrayList<>(start.getOutArcs());

        ArrayList<Arc> olistOfArcs = new ArrayList<>(start.getInArcs());
        listOfArcs.retainAll(end.getInArcs());
        return listOfArcs;
    }

    private ArrayList<PartialSubnetElements> mergePartialSubnetElements
            (ArrayList<PartialSubnetElements> psePb, ArrayList<PartialSubnetElements> pseTb) {
        ArrayList<Node> allNodes = new ArrayList<>();
        ArrayList<Arc> allArcs = new ArrayList<>();
        for (PartialSubnetElements pse : psePb
        ) {
            allNodes.addAll(pse.partialNodes);
            allArcs.addAll(pse.partialArcs);
        }
        for (PartialSubnetElements pse : pseTb
        ) {
            allNodes.addAll(pse.partialNodes);
            allArcs.addAll(pse.partialArcs);
        }
        allNodes = allNodes.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        allArcs = allArcs.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

        PartialSubnetElements pse = new PartialSubnetElements(allNodes, allArcs);

        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();
        pseList.add(pse);
        return pseList;
    }


    private PartialSubnetElements mergePartialSubnetElements
            (PartialSubnetElements psePb, PartialSubnetElements pseTb) {
        ArrayList<Node> allNodes = new ArrayList<>();
        ArrayList<Arc> allArcs = new ArrayList<>();
        HashMap<Node, Node> nm = new HashMap<>();
        HashMap<Arc, Arc> am = new HashMap<>();

        ArrayList<Node> annyConection = new ArrayList<>(psePb.partialNodes);
        annyConection.retainAll(pseTb.partialNodes);

        if (annyConection.size() > 0) {
            allNodes.addAll(psePb.partialNodes);
            allArcs.addAll(psePb.partialArcs);

            allNodes.addAll(pseTb.partialNodes);
            allArcs.addAll(pseTb.partialArcs);

            allNodes = allNodes.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
            allArcs = allArcs.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

            nm.putAll(psePb.nodesMap);
            nm.putAll(pseTb.nodesMap);

            am.putAll(psePb.arcsMap);
            am.putAll(pseTb.arcsMap);
        } else {
            if (psePb.partialNodes.size() > pseTb.partialNodes.size()) {
                allNodes.addAll(psePb.partialNodes);
                allArcs.addAll(psePb.partialArcs);
                nm.putAll(psePb.nodesMap);
                am.putAll(psePb.arcsMap);
            } else {
                allNodes.addAll(pseTb.partialNodes);
                allArcs.addAll(pseTb.partialArcs);
                nm.putAll(pseTb.nodesMap);
                am.putAll(pseTb.arcsMap);
            }
        }


        if (allNodes.size() > 7) {
            System.out.println("---------------");

            for (Arc a : allArcs) {
                System.out.println(" Arc: " + a.getStartNode().getName() + " - > " + a.getEndNode().getName());
            }

            //czy jest spójny na bank
            System.out.println("Tutaj - g");
        }
        return new PartialSubnetElements(allNodes, allArcs, nm, am);
    }

    private ArrayList<Transition> generateNewTransitions(ArrayList<Node> list, BranchBasedSubnet net) {
        ArrayList<Transition> result = new ArrayList<>();
        for (Node n : list) {
            if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                //sheet generator
                Transition t = new Transition(IdGenerator.getNextId(), 1, (Point) n.getNodePositions(0).get(0).clone());
                result.add(t);
            }
        }

        return result;
    }

    private ArrayList<Node> generateNewNodes(ArrayList<Node> list, BranchBasedSubnet net, int sheetID) {
        ArrayList<Node> result = new ArrayList<>();
        for (Node n : list) {
            if (n.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {

                Transition t = new Transition(IdGenerator.getNextId(), sheetID, (Point) n.getNodePositions(0).get(0).clone());
                result.add(t);
            }
            if (n.getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                Place p = new Place(IdGenerator.getNextId(), sheetID, (Point) n.getNodePositions(0).get(0).clone());
                result.add(p);
            }
        }

        return result;
    }

    private ArrayList<Place> generateNewPlaces(ArrayList<Node> list, BranchBasedSubnet net) {
        ArrayList<Place> result = new ArrayList<>();
        for (Node n : list) {
            if (n.getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                //sheet generator
                Place t = new Place(IdGenerator.getNextId(), 1, (Point) n.getNodePositions(0).get(0).clone());
                result.add(t);
            }
        }

        return result;
    }

    private ArrayList<ArrayList<RowMatch>> calcBranchMatrix(BranchBasedSubnet net1, BranchBasedSubnet net2) {
        RowMatch[][] bm = new RowMatch[net1.pairs.size()][net2.pairs.size()];

        //int[][] net1bdm = net1.branchDirMatrix;
        //int[][] net2bdm = net2.branchDirMatrix;

        //ArrayList<RowMatch> rm = new ArrayList<>();

        for (int i = 0; i < net1.branchDirMatrix.length; i++) {
            for (int j = 0; j < net2.branchDirMatrix.length; j++) {
                bm[i][j] = (matchRows(net1.branchDirMatrix[i], net2.branchDirMatrix[j], net1.pairs.get(i), net2.pairs.get(j), net1, net2));
            }
        }

        ArrayList<ArrayList<RowMatch>> listOfVerticesMatches = coloringTable(bm, net1, net2);

        return listOfVerticesMatches;
    }

    public static ArrayList<ArrayList<Integer>> listPermutations(ArrayList<Integer> list) {

        if (list.size() == 0) {
            ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
            result.add(new ArrayList<Integer>());
            return result;
        }

        ArrayList<ArrayList<Integer>> returnMe = new ArrayList<ArrayList<Integer>>();

        Integer firstElement = list.remove(0);

        ArrayList<ArrayList<Integer>> recursiveReturn = listPermutations(list);
        for (List<Integer> li : recursiveReturn) {

            for (int index = 0; index <= li.size(); index++) {
                ArrayList<Integer> temp = new ArrayList<Integer>(li);
                temp.add(index, firstElement);
                returnMe.add(temp);
            }
        }

        return returnMe;
    }

    private ArrayList<ArrayList<RowMatch>> coloringTable(RowMatch[][] bm, BranchBasedSubnet net1, BranchBasedSubnet net2) {
        ArrayList<ArrayList<RowMatch>> results = new ArrayList<>();
        ArrayList<Integer> listaDoPermutacji = new ArrayList<>();

        for (int i = 0; i < net2.branchVertices.size() || i < net1.branchVertices.size(); i++) {

            if (i >= net2.branchVertices.size()) {
                listaDoPermutacji.add(-1);
            } else {
                listaDoPermutacji.add(i);
            }

        }

        //BRUTE FORCE
        ArrayList<ArrayList<Integer>> dopasowania = listPermutations(listaDoPermutacji);

        //kolorowanie
        ArrayList<ArrayList<Node>> colorNet1 = new ArrayList<>();

        for (int i = 0; i < net1.branchVertices.size(); i++) {
            ArrayList<Node> list = new ArrayList<>();
            for (int j = 0; j < net1.pairs.size(); j++) {
                if (net1.branchVertices.get(i).tbranch.contains(net1.pairs.get(j))) {
                    list.add(net1.branchVertices.get(i).root);
                }
            }
            colorNet1.add(list);
        }

        ArrayList<ArrayList<Node>> colorNet2 = new ArrayList<>();
        for (int i = 0; i < net2.branchVertices.size(); i++) {

            ArrayList<Node> list = new ArrayList<>();
            for (int j = 0; j < net2.pairs.size(); j++) {
                if (net2.branchVertices.get(i).tbranch.contains(net2.pairs.get(j))) {
                    list.add(net2.branchVertices.get(i).root);
                }
            }
            colorNet2.add(list);
        }

        if (net2.branchVertices.size() > net1.branchVertices.size()) {
            dopasowania = trim(dopasowania, net2.branchVertices.size() - net1.branchVertices.size());
        }

        for (ArrayList<Integer> match : dopasowania) {
            ArrayList<RowMatch> listaRowMatchy = new ArrayList<>();

            //tu bedzie ograniczenie
            {
                for (int j = 0; j < net1.branchVertices.size(); j++) {

                    net1.branchVertices.get(j);

                }
            }
            listaRowMatchy = extractBranchesForMatchedBranchVertices(bm, match, colorNet1, colorNet2, net1, net2);

            results.add(listaRowMatchy);
        }

        /***
         * generujesz wszystkie dopasowania branchowych wierzchołków do siebie
         * -ulepszeniem byłoby ograniczenie po strukturze
         *
         * koloroawnie które znasz - wirzchołki wystěpujæ w tych rbanchach
         *
         * sprawdzasz dopasowania
         * - czy w obszarze dla tych dwuch wystěpujæ dopasowanie 2 jak tak, wiii to dopasowanie jest ok i spradzasz wystěpowanie w kolejnych paracg
         * ! juž masz przypisane wierzchołki wiěc co najwyžej pozostaje przypisanie 2 branchy o tych samych skierowaniach itp
         * ? bardzo wažne pytanie jakie zwracasz? wszystkie czy najlepsze
         * czy jakiś zakres od najlepszego
         * preferowałbym najdłužsze
         *
         * ? tablica... 2 razy wiěksza? skierowanie brancha pod uwagě? naaa
         */

        ArrayList<ArrayList<RowMatch>> max = new ArrayList<>();
        for (ArrayList<RowMatch> r : results
        ) {
            if (max.size() == 0 || r.size() > max.size()) {
                max.clear();
                max.add(r);
            } else if (r.size() == max.get(0).size()) {
                max.add(r);
            }
        }

        results.addAll(max);
        return results;
    }

    private ArrayList<RowMatch> extractBranchesForMatchedBranchVertices(RowMatch[][] bm, ArrayList<Integer> match, ArrayList<ArrayList<Node>> colorNet1, ArrayList<ArrayList<Node>> colorNet2, BranchBasedSubnet net1, BranchBasedSubnet net2) {
        ArrayList<RowMatch> results = new ArrayList<>();

        for (int i = 0; i < bm.length; i++) {
            for (int j = 0; j < bm[i].length; j++) {
                boolean first = false;
                boolean secon = false;
                //net1.branchVertices.
                for (int k = 0; k < match.size(); k++) {
                    if (match.get(k) != -1) {

                        if (bm[i][j].startNodeNet1.getID() == net1.branchVertices.get(k).root.getID() &&
                                bm[i][j].startNodeNet2.getID() == net2.branchVertices.get(match.get(k)).root.getID()
                        ) {
                            first = true;
                        }
                        if (bm[i][j].endNodeNet1.getID() == net1.branchVertices.get(k).root.getID() &&
                                bm[i][j].endNodeNet2.getID() == net2.branchVertices.get(match.get(k)).root.getID()
                        ) {
                            secon = true;
                        }
                    }
                }
                if (first && secon) {
                    results.add(bm[i][j]);
                }
            }
        }

        for (int i = 0; i < colorNet1.size(); i++) {

        }

        results = results.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

        return results;
    }

    private ArrayList<ArrayList<Integer>> trim(ArrayList<ArrayList<Integer>> dopasowania, int i) {
        for (ArrayList<Integer> list : dopasowania) {
            for (int j = 0; j < i; j++) {
                list.remove(list.size() - 1);
            }
        }
        return dopasowania;
    }

    private ArrayList<ArrayList<RowMatch>> calcMatching(ArrayList<ArrayList<RowMatch>> listOfVerticesMatches, RowMatch[][] bm) {

        ArrayList<ArrayList<RowMatch>> properMatchings = new ArrayList<>();
        for (ArrayList<RowMatch> lrm : listOfVerticesMatches) {
            ArrayList<RowMatch> used = new ArrayList<>();

            for (int i = 0; i < bm.length; i++) {
                for (int j = 0; j < bm[i].length; j++) {
                    if (lrm.contains(bm[i][j])) {
                        ArrayList<ArrayList<RowMatch>> foundedMatches = findNext(i, j, used, bm, lrm);
                        properMatchings.addAll(foundedMatches);
                    }
                }
            }
        }

        return properMatchings;
    }

    private ArrayList<ArrayList<RowMatch>> findNext(int i, int j, ArrayList<RowMatch> used, RowMatch[][] bm, ArrayList<RowMatch> lrm) {
        ArrayList<ArrayList<RowMatch>> result = new ArrayList<>();

        return result;
    }

    private ArrayList<ArrayList<RowMatch>> calcMaxMatches(RowMatch[][] rm) {

        founded = new int[rm.length][rm[0].length];
        ArrayList<ArrayList<RowMatch>> matches = new ArrayList<>();

        int type = 1;

        for (int i = 0; i < rm.length; i++) {
            for (int j = 0; j < rm[i].length; j++) {

                if (founded[i][j] == 0) {
                    ArrayList<RowMatch> match = new ArrayList<>();
                    match.add(rm[i][j]);
                    founded[i][j] = type;
                    for (int k = 0; k < rm.length; k++) {
                        for (int l = 0; l < rm[i].length; l++) {
                            if (founded[k][l] == 0) {

                                if (matchPairs(match, rm[k][l])) {
                                    founded[k][l] = type;
                                    match.add(rm[k][l]);
                                }
                                //check connections
                            }
                        }
                    }
                    matches.add(match);
                    type++;
                }
            }

        }

        return matches;
    }

    private boolean matchPairs(ArrayList<RowMatch> match, RowMatch rowMatch) {
        boolean isMatch = false;
        boolean isConflict = false;

        for (RowMatch rm : match) {
            if (rm.verticesMap.containsAll(rowMatch.verticesMap)) {
                isMatch = true;
            } else if (!Collections.disjoint(rm.verticesMap, rowMatch.verticesMap)) {
                ArrayList<ArrayList<Integer>> tmp = (ArrayList<ArrayList<Integer>>) rowMatch.verticesMap.clone();
                tmp.retainAll(rm.verticesMap);
                ArrayList<ArrayList<Integer>> allElements = new ArrayList<>();
                allElements.addAll(rm.verticesMap);
                allElements.addAll(rowMatch.verticesMap);

                allElements.removeAll(tmp);

                if (checConflict(allElements)) {
                    isConflict = true;
                }
                isMatch = true;
            }
        }
        if (isConflict)
            return false;

        return isMatch;
    }

    private boolean checConflict(ArrayList<ArrayList<Integer>> allElements) {
        boolean isConflict = false;

        HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
        for (int i = 0; i < allElements.size(); i++) {
            for (int j = i + 1; j < allElements.size() - 1; j++) {
                if (allElements.get(i).get(0).equals(allElements.get(j).get(0)) || allElements.get(i).get(1).equals(allElements.get(j).get(1)))
                    isConflict = true;
            }
        }

        return isConflict;
    }

    private RowMatch matchRows(int[] rowN1, int[] rowN2, BranchBasedSubnet.Branch br1, BranchBasedSubnet.
            Branch br2, BranchBasedSubnet net1, BranchBasedSubnet net2) {

        int start1 = 0;
        int end1 = 0;
        for (int l = 0; l < rowN1.length; l++) {
            if (rowN1[l] > 0)
                end1 = l;
            if (rowN1[l] < 0)
                start1 = l;
        }

        int start2 = 0;
        int end2 = 0;
        for (int k = 0; k < rowN2.length; k++) {
            if (rowN2[k] > 0)
                end2 = k;
            if (rowN2[k] < 0)
                start2 = k;
        }

        ArrayList<ArrayList<Integer>> map = new ArrayList<>();

        ArrayList<Integer> rowStart = new ArrayList<Integer>();
        rowStart.add(start1);
        rowStart.add(start2);
        map.add(rowStart);
        //map.add(new VertexMap(start1,start2));

        ArrayList<Integer> rowEnd = new ArrayList<>();
        rowEnd.add(end1);
        rowEnd.add(end2);
        map.add(rowEnd);
        //map.add(new VertexMap(end1,end2));

        return new RowMatch(start1, end1, map, br1, br2, net1, net2);
    }

    private void calcGCSs(char[][] branchMateix, BranchBasedSubnet net1, BranchBasedSubnet net2) {

    }

    private BranchBasedSubnet transformSubnet(SubnetCalculator.SubNet sn) {
        return new BranchBasedSubnet(sn);
    }

    public class RowMatch {
        int firstNetRowIndex;
        int seconNetRowIndex;

        public Node startNodeNet1;
        public Node endNodeNet1;
        public Node startNodeNet2;
        public Node endNodeNet2;

        public int snn1ID;
        public int snn2ID;
        public int enn1ID;
        public int enn2ID;

        int mapOfFNR;
        int mapOfSNR;

        BranchBasedSubnet.Branch firstNetBranch;
        BranchBasedSubnet.Branch secondNetBranch;
        //public HashMap<Integer, Integer> verticesMap;
        public ArrayList<ArrayList<Integer>> verticesMap = new ArrayList<>();

        RowMatch(int f, int s, ArrayList<ArrayList<Integer>> vm) {
            this.firstNetRowIndex = f;
            this.seconNetRowIndex = s;
            this.verticesMap = vm;
            this.mapOfFNR = vm.get(0).get(1);
            this.mapOfSNR = vm.get(1).get(1);
        }

        RowMatch(int f, int s, ArrayList<ArrayList<Integer>> vm, BranchBasedSubnet.Branch fnb, BranchBasedSubnet.Branch snb, BranchBasedSubnet net1, BranchBasedSubnet net2) {
            this.firstNetRowIndex = f;
            this.seconNetRowIndex = s;
            this.verticesMap = vm;
            this.firstNetBranch = fnb;
            this.secondNetBranch = snb;
            this.mapOfFNR = vm.get(0).get(1);
            this.mapOfSNR = vm.get(1).get(1);

            this.startNodeNet1 = net1.transitions.get(firstNetRowIndex);
            this.endNodeNet1 = net1.transitions.get(seconNetRowIndex);
            this.startNodeNet2 = net2.transitions.get(mapOfFNR);
            this.endNodeNet2 = net2.transitions.get(mapOfSNR);

            //this.snn1ID = net1.branchVertices.
        }

    }

    public class PartialSubnetElements {
        public ArrayList<Node> partialNodes = new ArrayList<>();
        public ArrayList<Arc> partialArcs = new ArrayList<>();
        public HashMap<Node, Node> nodesMap = new HashMap<>();
        public HashMap<Arc, Arc> arcsMap = new HashMap<>();

        public PartialSubnetElements(ArrayList<Node> pratOfNodes) {
            this.partialNodes.addAll(pratOfNodes);
        }

        public PartialSubnetElements(ArrayList<Node> pratOfNodes, ArrayList<Arc> partOfArcs) {
            this.partialNodes.addAll(pratOfNodes);
            this.partialArcs.addAll(partOfArcs);
            if (partialNodes.size() == 0 || partialArcs.size() == 0) {
                System.out.println("COs nie tak");
            }
        }

        public PartialSubnetElements(ArrayList<Node> pratOfNodes, ArrayList<Arc> partOfArcs, HashMap<Node, Node> nm, HashMap<Arc, Arc> am) {
            this.partialNodes.addAll(pratOfNodes);
            this.partialArcs.addAll(partOfArcs);
            this.arcsMap = am;
            this.nodesMap = nm;
        }

        public PartialSubnetElements(PartialSubnetElements pse1, PartialSubnetElements pse2) {
            this.partialNodes.addAll(pse1.partialNodes);
            this.partialNodes.addAll(pse2.partialNodes);
            this.partialNodes.stream().distinct();
            this.partialArcs.addAll(pse1.partialArcs);
            this.partialArcs.addAll(pse2.partialArcs);
            this.partialArcs.stream().distinct();
            if (partialNodes.size() == 0 || partialArcs.size() == 0) {
                //System.out.println("COs nie tak");
            }
        }

        public PartialSubnetElements(ArrayList<PartialSubnetElements> pseList, boolean mock) {
            for (PartialSubnetElements pse : pseList) {
                this.partialNodes.addAll(pse.partialNodes);
                this.partialArcs.addAll(pse.partialArcs);
            }
            this.partialNodes.stream().distinct();
            this.partialArcs.stream().distinct();

            if (partialNodes.size() == 0 || partialArcs.size() == 0) {
                System.out.println("COs nie tak");
            }
        }

        public int arcWeight() {
            return partialArcs.stream().mapToInt(x -> x.getWeight()).sum();
        }

        public double matchingValueFunction() {
            //mock
            return partialNodes.size();
            //return nodesMap.keySet().size();
        }

        public double matchingArcValueFunction() {
            //mock
            return partialArcs.size();
            //return arcsMap.values().size();
        }

        public void check() {
            System.out.println("new check");

            ArrayList<Node> toremove = new ArrayList<>();
            for (Node n : nodesMap.keySet()) {
                //System.out.println(n.getName() + " ---- > ----" + nodesMap.get(n).getName());

                Set<Node> keySet = getKeys(nodesMap, nodesMap.get(n));
                if (keySet.size() > 1) {
                    for (Node m : keySet) {
                        //System.out.println(m.getName() + "<<<><>>>" + nodesMap.get(m).getName());
                        if (connectedArcs(m, partialArcs) == 1) {
                            toremove.add(m);
                        }
                    }
                }
            }

            for (Node m : toremove) {
                nodesMap.remove(m);
                partialNodes.remove(m);

                ArrayList<Arc> arcToRemove = partialArcs.stream().filter(x -> x.getStartNode().equals(m) || x.getEndNode().equals(m)).collect(Collectors.toCollection(ArrayList::new));
                partialArcs.removeAll(arcToRemove);
            }

            Set<Node> valuseSet = new HashSet<Node>(nodesMap.values());
            if (nodesMap.values().size() != valuseSet.size()) {
                System.out.println("Variant F");
            }


            for (Arc a : arcsMap.keySet()) {
                if (!nodesMap.containsKey(a.getStartNode())) {
                    System.out.println("A");
                }
                if (!nodesMap.containsKey(a.getEndNode())) {
                    System.out.println("B");
                }
            }

            ArrayList<Arc> listToRemove = new ArrayList<>();
            for (Arc a : arcsMap.values()) {
                if (!nodesMap.containsValue(a.getStartNode())) {
                    listToRemove.add(a);
                    System.out.println("C");
                }
                if (!nodesMap.containsValue(a.getEndNode())) {
                    listToRemove.add(a);
                    System.out.println("D");
                }
            }

            for (Arc a : listToRemove) {
                Set<Arc> keySet = getKeys(arcsMap, a);
                for (Arc as : keySet) {
                    if (as.getStartNode().getOutInArcs().size() > 1 && as.getEndNode().getOutInArcs().size() > 1) {
                        arcsMap.remove(as);
                        partialArcs.remove(as);
                    }
                }
            }

            //remove non conected elements
            ArrayList<Arc> listaArcow = new ArrayList<>(partialArcs);

            ArrayList<ArrayList<Arc>> podzialy = new ArrayList<>();
            ArrayList<Arc> podzial = new ArrayList<>();

            if (!listaArcow.isEmpty()) {
                podzial.add(listaArcow.get(0));
                listaArcow.remove(0);

                while (listaArcow.size() > 0) {
                    ArrayList<Arc> toExtend = new ArrayList<>();
                    for (Arc a : podzial) {
                        toExtend.addAll(listaArcow.stream().filter(x -> x.getEndNode().equals(a.getEndNode()) || x.getEndNode().equals(a.getStartNode()) || x.getStartNode().equals(a.getStartNode()) || x.getStartNode().equals(a.getEndNode())).collect(Collectors.toCollection(ArrayList::new)));
                    }


                    if (toExtend.isEmpty()) {
                        podzialy.add(podzial);
                        podzial.clear();
                        podzial.add(listaArcow.get(0));
                        listaArcow.remove(0);
                    }

                    if (!toExtend.isEmpty()) {
                        podzial.add(toExtend.get(0));
                        listaArcow.remove(toExtend.get(0));
                    }

                    if (toExtend.isEmpty() && listaArcow.isEmpty()) {
                        podzialy.add(podzial);
                    }
                }
            }

            if (!podzial.isEmpty())
                podzialy.add(podzial);

            //System.out.println("Podziały : " + podzialy.size());
            if (podzialy.size() > 1) {
                //System.out.println("Wybieram najwielszy");
                ArrayList<Arc> max = new ArrayList<>();
                for (ArrayList<Arc> pod : podzialy) {
                    if (max.size() < pod.size())
                        max = pod;
                }

                partialArcs = max;

                ArrayList<Node> reducedNodeList = new ArrayList<>();
                for (Arc a : partialArcs) {
                    if (!reducedNodeList.contains(a.getEndNode()))
                        reducedNodeList.add(a.getEndNode());
                    if (!reducedNodeList.contains(a.getStartNode()))
                        reducedNodeList.add(a.getStartNode());
                }
                partialNodes = reducedNodeList;

            }

        }
    }

    public int connectedArcs(Node n, ArrayList<Arc> properArcs) {
        int counter = 0;
        for (Arc a : properArcs) {
            if (a.getEndNode().equals(n))
                counter++;

            if (a.getStartNode().equals(n))
                counter++;
        }
        return counter;
    }

    public <K, V> Set<K> getKeys(Map<K, V> map, V value) {
        Set<K> keys = new HashSet<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

}
