package holmes.analyse.comparison;

import holmes.analyse.SubnetCalculator;
import holmes.analyse.comparison.structures.BranchBasedSubnet;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
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

    public ArrayList<ArrayList<GreatCommonSubnet>> compare() {
        boolean testy = false;
        ArrayList<ArrayList<GreatCommonSubnet>> resultMatrix = new ArrayList<>();

        if (testy) {
            GreatCommonSubnet gcs = compareTwoSubnets(subnetsForFirstNet.get(4), subnetsForSecondNet.get(8));
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
                    list.get(j).secondNetNodeSize = subnetsForSecondNet.get(i).nodes.size();
                }
                resultMatrix.add(list);
            }
        }

        return resultMatrix;
    }

    public GreatCommonSubnet compareTest() {
        return compareTwoSubnets(subnetsForFirstNet.get(0), subnetsForSecondNet.get(0));
    }

    private GreatCommonSubnet compareTwoSubnets(BranchBasedSubnet net1, BranchBasedSubnet net2) {

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
            psel.addAll(comparePTBranches(net1, net2, true));
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
            psel.addAll(comparePTBranches(net1, net2, false));
            System.out.println("X7 - druga z 1 branchowæ, pierwsza z wiěcej niž jednæ branchowæ - tbranch");
        } else if (net2.branchVertices.size() > 1 && net1.branchVertices.size() == 1) {
            psel.addAll(comparePTBranches(net2, net1, false));
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

        ArrayList<Node> commonNodesIn = new ArrayList<>();
        ArrayList<Arc> commonArcsIn = new ArrayList<>();

        if (firstNetBranch.branchElements.size() == secondNetBranch.branchElements.size()) {
            for (int i = 0; i < firstNetBranch.branchArcs.size(); i++) {

                if (firstNetBranch.branchArcs.get(i).getWeight() != secondNetBranch.branchArcs.get(i).getWeight()) {
                    int arcWeight = Math.min(firstNetBranch.branchArcs.get(i).getWeight(), secondNetBranch.branchArcs.get(i).getWeight());
                    if (arcWeight != 0) {
                        firstNetBranch.branchArcs.get(i).setMemoryOfArcWeight(arcWeight);
                        commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                    }
                } else {
                    commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                }
                //wyciente
            }
            //wklejone
            commonNodesIn.addAll(firstNetBranch.branchElements);
            listOfBranches.add(new BranchBasedSubnet.Branch(commonNodesIn, commonArcsIn, bbs));
        } else {
            //commonNodesIn.add(firstNetBranch.branchArcs.get(0).getStartNode());
            for (int i = 0; i < firstNetBranch.branchArcs.size() && i < secondNetBranch.branchArcs.size(); i++) {
                if (firstNetBranch.branchArcs.get(i).getWeight() != secondNetBranch.branchArcs.get(i).getWeight()) {
                    int arcWeight = Math.min(firstNetBranch.branchArcs.get(i).getWeight(), secondNetBranch.branchArcs.get(i).getWeight());
                    if (arcWeight != 0) {
                        firstNetBranch.branchArcs.get(i).setMemoryOfArcWeight(arcWeight);
                        commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getEndNode()))
                            commonNodesIn.add(firstNetBranch.branchArcs.get(i).getEndNode());
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getStartNode()))
                            commonNodesIn.add(firstNetBranch.branchArcs.get(i).getStartNode());
                    }
                } else {
                    commonArcsIn.add(firstNetBranch.branchArcs.get(i));
                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getEndNode()))
                        commonNodesIn.add(firstNetBranch.branchArcs.get(i).getEndNode());
                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(i).getStartNode()))
                        commonNodesIn.add(firstNetBranch.branchArcs.get(i).getStartNode());
                }
            }
            listOfBranches.add(new BranchBasedSubnet.Branch(commonNodesIn, commonArcsIn, bbs));
            commonNodesIn = new ArrayList<>();
            commonArcsIn = new ArrayList<>();

            //commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size()-1).getEndNode());
            for (int i = 0; i < firstNetBranch.branchArcs.size() && i < secondNetBranch.branchArcs.size(); i++) {
                if (firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getWeight() != secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getWeight()) {
                    int arcWeight = Math.min(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getWeight(), secondNetBranch.branchArcs.get(secondNetBranch.branchArcs.size() - 1 - i).getWeight());
                    if (arcWeight != 0) {
                        firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).setMemoryOfArcWeight(arcWeight);
                        commonArcsIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i));
                        //commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size()-1-i).getStartNode());
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode()))
                            commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode());
                        if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode()))
                            commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode());
                    }
                } else {
                    //zmiana
                    commonArcsIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i));
                    //commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size()-1-i).getStartNode());
                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode()))
                        commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getEndNode());
                    if (!commonNodesIn.contains(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode()))
                        commonNodesIn.add(firstNetBranch.branchArcs.get(firstNetBranch.branchArcs.size() - 1 - i).getStartNode());
                }
            }
            listOfBranches.add(new BranchBasedSubnet.Branch(commonNodesIn, commonArcsIn, bbs));
        }
        return listOfBranches;
    }

    /**
     * @param net1 multibranch net
     * @param net2 one branch net
     */

    private ArrayList<PartialSubnetElements> comparePTBranches(BranchBasedSubnet net1, BranchBasedSubnet net2, boolean mode) {

        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();
        ArrayList<BranchBasedSubnet.Branch> tbranches = new ArrayList<>();
        for (int i = 0; i < net1.branchVertices.size(); i++) {
            for (int j = 0; j < net2.branchVertices.size(); j++) {

                if (mode) {

                    ArrayList<BranchBasedSubnet.Branch> branchNet1 = new ArrayList<>();
                    branchNet1.addAll(net1.branchVertices.get(i).pbranches);
                    branchNet1.addAll(net1.branchVertices.get(i).tbranch);
                    tbranches.addAll(net1.branchVertices.get(i).tbranch);

                    ArrayList<BranchBasedSubnet.Branch> branchNet2 = new ArrayList<>();
                    branchNet2.addAll(net2.branchVertices.get(j).pbranches);
                    branchNet2.addAll(net2.branchVertices.get(j).tbranch);

                    PartialSubnetElements psePb = comparePbranches(branchNet1, branchNet2, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                    pseList.add(psePb);
                } else {
                    PartialSubnetElements psePb = comparePbranches(net1.branchVertices.get(i).pbranches, net2.branchVertices.get(j).pbranches, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                    PartialSubnetElements pseTb = comparePbranches(net1.branchVertices.get(i).tbranch, net2.branchVertices.get(j).tbranch, net1.branchVertices.get(i).root, net2.branchVertices.get(j).root);
                    PartialSubnetElements pseAb = mergePartialSubnetElements(psePb, pseTb);

                    pseList.add(pseAb);
                }
            }
        }

        double max = pseList.stream().max(Comparator.comparing(x -> x.matchingValueFunction())).get().matchingValueFunction();
        return (ArrayList) pseList.stream().filter(x -> x.matchingValueFunction() == max).collect(Collectors.toList());
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
            ArrayList<ArrayList<BranchBasedSubnet.Branch>> listofInroperTbranches = new ArrayList<>();
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
                    JOptionPane.showMessageDialog(null,
                            "Tbranch nie znalazł žadnych branchy albo za dužo... tak czy siak klocek", "TO CHECK",
                            JOptionPane.ERROR_MESSAGE);
                }
                ArrayList<PartialSubnetElements> pse = new ArrayList<>(pseList);
            }

            ArrayList<BranchBasedSubnet.BranchVertex> tempBVList = new ArrayList<>(net1.branchVertices);
            ArrayList<PartialSubnetElements> listOfPBranches = new ArrayList<>();

            for (BranchBasedSubnet.BranchVertex bv : net1.branchVertices) {
                int net2BVindex = getBrenachVertexIndex(net1, net2, maping, bv);
                if (net2BVindex != -1) {
                    //find sęks
                    ArrayList<BranchBasedSubnet.Branch> listforFirst = new ArrayList<>(bv.pbranches);
                    listforFirst.addAll(bv.getSek());

                    ArrayList<BranchBasedSubnet.Branch> listOfSecond = new ArrayList<>(net2.branchVertices.get(net2BVindex).pbranches);
                    listOfSecond.addAll(net2.branchVertices.get(net2BVindex).getSek());

                    PartialSubnetElements psePb = comparePbranches(listforFirst, listOfSecond, bv.root, net2.branchVertices.get(net2BVindex).root);
                    if (psePb.partialNodes.size() > 0) {
                        System.out.println("Dodaję sęk: ");
                        printElements(psePb);
                        listOfPBranches.add(psePb);
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
                            pse = mergePartialSubnetElements(pse, new PartialSubnetElements(br.branchElements, br.branchArcs));
                            toDelate.add(br);
                            tbranchAdded = true;
                        }
                    } else {

                        System.out.println("Dodaję Tbranch:");
                        printElements(br);
                        pse = mergePartialSubnetElements(pse, new PartialSubnetElements(br.branchElements, br.branchArcs));
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

                    if (listofInroperTbranches.size() > 0) {
                        ArrayList<PartialSubnetElements> listaPse = new ArrayList<>();
                        listaPse.add(pse);

                        for (ArrayList<BranchBasedSubnet.Branch> twoInpropBranch : listofInroperTbranches) {
                            ArrayList<PartialSubnetElements> tmpListPse = new ArrayList<>(listaPse);

                            for (PartialSubnetElements tmpPSE : listaPse) {
                                for (BranchBasedSubnet.Branch inpropBranch : twoInpropBranch) {

                                    ArrayList<Node> tmpList = new ArrayList<>(tmpPSE.partialNodes);
                                    tmpList.retainAll(inpropBranch.returnBorderNodes());
                                    if (tmpList.size() > 0) {
                                        System.out.println("Dodaję inpTbranch:");
                                        printElements(inpropBranch);

                                        PartialSubnetElements newPSE = mergePartialSubnetElements(tmpPSE, new PartialSubnetElements(inpropBranch.branchElements, inpropBranch.branchArcs));
                                        tmpListPse.add(newPSE);
                                    }
                                }

                            }
                            listaPse = tmpListPse;
                        }
                        finished = true;
                        pseList.addAll(listaPse);

                    } else {
                        finished = true;
                        pseList.add(pse);
                    }
                }
            }
            //dodać obłusgę dla przypadka gdzie nie ma p branchy - probably copy existing part of code for pbranches
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

        SubnetCalculator.Path firstPath = new SubnetCalculator.Path(path2.path.get(0), path2.path.get(path2.path.size() - 1), path2.path);
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
        } else {
            //przypadek wymijajæcy I element z I sieci
            ArrayList<Node> shortPath1 = new ArrayList<>(path1.path);
            shortPath1.remove(0);

            for (int j = 0; j < shortPath1.size() - 1 && j < path2.path.size() - 1; j++) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(shortPath1.get(j), shortPath1.get(j + 1));
                ArrayList<Arc> arcFrom2 = getArc(path2.path.get(j), path2.path.get(j + 1));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    commonA.add(arcFrom1.get(0));
                    if (!commonN.contains(shortPath1.get(j)))
                        commonN.add(shortPath1.get(j));
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
        } else {
            ArrayList<Node> shortPath1 = new ArrayList<>(path1.path);
            shortPath1.remove(shortPath1.size() - 1);
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
                        commonA.add(arcFrom1.get(0));
                        if (!commonN.contains(shortPath1.get(j + firstMove)))
                            commonN.add(shortPath1.get(j + firstMove));
                        if (!commonN.contains(shortPath1.get(j + firstMove - 1)))
                            commonN.add(shortPath1.get(j + firstMove - 1));
                    } else {
                        break;
                    }
                }
            }

            commonNodes.add(commonN);
            commonArcs.add(commonA);

            commonN = new ArrayList<>();
            commonA = new ArrayList<>();
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
        for (int i = 0; i < commonNodes.size(); i++) {

            PartialSubnetElements pse = new PartialSubnetElements(commonNodes.get(i), commonArcs.get(i));
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
        ArrayList<PartialSubnetElements> pseList = new ArrayList<>();

        //before
        Collections.sort(pbranches1, new BranchBasedSubnet.Branch.LenghtSort());
        Collections.sort(pbranches2, new BranchBasedSubnet.Branch.LenghtSort());

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

            for (int j = Math.min(incomingBranches1.get(i).branchElements.size(), incomingBranches2.get(i).branchElements.size()) - 1; j > 0; j--) {
                int arcWeight = 0;
                ArrayList<Arc> arcFrom1 = getArc(incomingBranches1.get(i).branchElements.get(j + firstMove - 1), incomingBranches1.get(i).branchElements.get(j + firstMove));
                ArrayList<Arc> arcFrom2 = getArc(incomingBranches2.get(i).branchElements.get(j + secondMobe - 1), incomingBranches2.get(i).branchElements.get(j + secondMobe));
                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    commonArcs.add(arcFrom1.get(0));
                    if (!commonNodes.contains(incomingBranches1.get(i).branchElements.get(j + firstMove)))
                        commonNodes.add(incomingBranches1.get(i).branchElements.get(j + firstMove));
                    if (!commonNodes.contains(incomingBranches1.get(i).branchElements.get(j + firstMove - 1)))
                        commonNodes.add(incomingBranches1.get(i).branchElements.get(j + firstMove - 1));
                } else {
                    break;
                }
            }
        }

        //TODO ERROR!!
        for (int i = 0; i < outgoingBranches1.size() && i < outgoingBranches2.size(); i++) {
            for (int j = 0; j < outgoingBranches1.get(i).branchElements.size() - 1 && j < outgoingBranches2.get(i).branchElements.size() - 1; j++) {
                int arcWeight = 0;

                //wariant z równym
                ArrayList<Arc> arcFrom1 = getArc(outgoingBranches1.get(i).branchElements.get(j), outgoingBranches1.get(i).branchElements.get(j + 1));
                ArrayList<Arc> arcFrom2 = getArc(outgoingBranches2.get(i).branchElements.get(j), outgoingBranches2.get(i).branchElements.get(j + 1));

                if (!arcFrom1.isEmpty() && !arcFrom2.isEmpty()) {
                    arcWeight = Math.min(arcFrom1.get(0).getWeight(), arcFrom2.get(0).getWeight());
                }

                if (arcWeight != 0) {
                    arcFrom1.get(0).setMemoryOfArcWeight(arcWeight);
                    commonArcs.add(arcFrom1.get(0));
                    if (!commonNodes.contains(outgoingBranches1.get(i).branchElements.get(j)))
                        commonNodes.add(outgoingBranches1.get(i).branchElements.get(j));
                    if (!commonNodes.contains(outgoingBranches1.get(i).branchElements.get(j + 1)))
                        commonNodes.add(outgoingBranches1.get(i).branchElements.get(j + 1));
                } else {
                    break;
                }
            }
        }

        return new PartialSubnetElements(commonNodes, commonArcs);
    }

    private ArrayList<BranchBasedSubnet.Branch> getIncomingBranches
            (ArrayList<BranchBasedSubnet.Branch> list, Node root) {
        return list.stream().filter(x -> x.endNode.getID() == root.getID()).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<BranchBasedSubnet.Branch> getOutgoingBranches
            (ArrayList<BranchBasedSubnet.Branch> list, Node root) {
        return list.stream().filter(x -> x.startNode.getID() == root.getID()).collect(Collectors.toCollection(ArrayList::new));
    }

    private ArrayList<Arc> getArc(Node start, Node end) {
        ArrayList<Arc> listOfArcs = new ArrayList<>(start.getOutArcs());
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

        allNodes.addAll(psePb.partialNodes);
        allArcs.addAll(psePb.partialArcs);

        allNodes.addAll(pseTb.partialNodes);
        allArcs.addAll(pseTb.partialArcs);

        allNodes = allNodes.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
        allArcs = allArcs.stream().distinct().collect(Collectors.toCollection(ArrayList::new));

        return new PartialSubnetElements(allNodes, allArcs);
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

        int[][] net1bdm = net1.branchDirMatrix;
        int[][] net2bdm = net2.branchDirMatrix;

        ArrayList<RowMatch> rm = new ArrayList<>();

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

        public PartialSubnetElements(PartialSubnetElements pse1, PartialSubnetElements pse2) {
            this.partialNodes.addAll(pse1.partialNodes);
            this.partialNodes.addAll(pse2.partialNodes);
            this.partialNodes.stream().distinct();
            this.partialArcs.addAll(pse1.partialArcs);
            this.partialArcs.addAll(pse2.partialArcs);
            this.partialArcs.stream().distinct();
            if (partialNodes.size() == 0 || partialArcs.size() == 0) {
                System.out.println("COs nie tak");
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
        }
    }
}
