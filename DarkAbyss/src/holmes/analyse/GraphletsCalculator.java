package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GraphletsCalculator {
    public static int sheetID = 0;
    private static int totalGrahletID = 0;

    public static HashMap<Integer, Node> globalOrbitMap = new HashMap<>();
    public static HashMap<Integer, Integer> orbitOraphMap = new HashMap<>();
    public static ArrayList<SubnetCalculator.SubNet> graphetsList = new ArrayList<>();

    public static ArrayList<ArrayList<ArrayList<GraphletsCalculator.Struct>>> graphlets = new ArrayList<>();

    public static ArrayList<ArrayList<GraphletsCalculator.Struct>> sortedGraphlets = new ArrayList<>();

    public static ArrayList<GraphletsCalculator.Struct> uniqGraphlets = new ArrayList<>();

    static int graphletNumber = 151;

    public static boolean multipleArcCheck = true;

    public static void GraphletsCalculator() {
        generateGraphlets();
    }

    public static void generateGraphlets() {
        totalGrahletID = 0;
        graphetsList.clear();
        graphetsList.add(graphlet_1());
        graphetsList.add(graphlet_2());
        graphetsList.add(graphlet_3());
        graphetsList.add(graphlet_4());
        graphetsList.add(graphlet_5());
        graphetsList.add(graphlet_6());
        graphetsList.add(graphlet_7());
        graphetsList.add(graphlet_8());

        graphetsList.add(graphlet_9());
        graphetsList.add(graphlet_10());
        graphetsList.add(graphlet_11());
        graphetsList.add(graphlet_12());
        graphetsList.add(graphlet_13());
        graphetsList.add(graphlet_14());
        graphetsList.add(graphlet_15());
        graphetsList.add(graphlet_16());

        graphetsList.add(graphlet_17());
        graphetsList.add(graphlet_18());
        graphetsList.add(graphlet_19());
        graphetsList.add(graphlet_20());
        graphetsList.add(graphlet_21());
        graphetsList.add(graphlet_22());
        graphetsList.add(graphlet_23());
        graphetsList.add(graphlet_24());

        graphetsList.add(graphlet_25());
        graphetsList.add(graphlet_26());
        graphetsList.add(graphlet_27());
        graphetsList.add(graphlet_28());
        graphetsList.add(graphlet_29());
        graphetsList.add(graphlet_30());
        graphetsList.add(graphlet_31());


        //---
        graphetsList.add(graphlet_32());
        graphetsList.add(graphlet_33());
        graphetsList.add(graphlet_34());
        graphetsList.add(graphlet_35());
        graphetsList.add(graphlet_36());
        graphetsList.add(graphlet_37());
        graphetsList.add(graphlet_38());
        graphetsList.add(graphlet_39());
        graphetsList.add(graphlet_40());
        graphetsList.add(graphlet_41());
        //graphetsList.add(graphlet_42());
        //graphetsList.add(graphlet_43());
        graphetsList.add(graphlet_44());
        graphetsList.add(graphlet_45());
        graphetsList.add(graphlet_46());
        graphetsList.add(graphlet_47());
        graphetsList.add(graphlet_48());
        graphetsList.add(graphlet_49());
        graphetsList.add(graphlet_50());
        graphetsList.add(graphlet_51());
        graphetsList.add(graphlet_52());
        graphetsList.add(graphlet_53());
        //graphetsList.add(graphlet_54());
        //graphetsList.add(graphlet_55());
        graphetsList.add(graphlet_56());
        graphetsList.add(graphlet_57());
        graphetsList.add(graphlet_58());
        graphetsList.add(graphlet_59());
        graphetsList.add(graphlet_60());
        graphetsList.add(graphlet_61());
        graphetsList.add(graphlet_62());
        graphetsList.add(graphlet_63());
        graphetsList.add(graphlet_64());
        graphetsList.add(graphlet_65());
        graphetsList.add(graphlet_66());
        graphetsList.add(graphlet_67());
        graphetsList.add(graphlet_68());
        graphetsList.add(graphlet_69());

        graphetsList.add(graphlet_70());
        graphetsList.add(graphlet_71());
        graphetsList.add(graphlet_72());
        graphetsList.add(graphlet_73());
        graphetsList.add(graphlet_74());
        graphetsList.add(graphlet_75());
        graphetsList.add(graphlet_76());
        graphetsList.add(graphlet_77());
        graphetsList.add(graphlet_78());
        graphetsList.add(graphlet_79());

        graphetsList.add(graphlet_80());
        graphetsList.add(graphlet_81());
        graphetsList.add(graphlet_82());
        graphetsList.add(graphlet_83());
        graphetsList.add(graphlet_84());
        graphetsList.add(graphlet_85());
        graphetsList.add(graphlet_86());
        graphetsList.add(graphlet_87());
        graphetsList.add(graphlet_88());
        graphetsList.add(graphlet_89());

        graphetsList.add(graphlet_90());
        graphetsList.add(graphlet_91());
        graphetsList.add(graphlet_92());
        graphetsList.add(graphlet_93());
        graphetsList.add(graphlet_94());
        graphetsList.add(graphlet_95());
        graphetsList.add(graphlet_96());
        graphetsList.add(graphlet_97());
        graphetsList.add(graphlet_98());
        graphetsList.add(graphlet_99());


        graphetsList.add(graphlet_100());
        graphetsList.add(graphlet_101());
        graphetsList.add(graphlet_102());
        graphetsList.add(graphlet_103());
        graphetsList.add(graphlet_104());
        graphetsList.add(graphlet_105());
        graphetsList.add(graphlet_106());
        graphetsList.add(graphlet_107());
        graphetsList.add(graphlet_108());
        graphetsList.add(graphlet_109());

        graphetsList.add(graphlet_110());
        graphetsList.add(graphlet_111());
        graphetsList.add(graphlet_112());
        graphetsList.add(graphlet_113());
        graphetsList.add(graphlet_114());
        graphetsList.add(graphlet_115());
        graphetsList.add(graphlet_116());
        graphetsList.add(graphlet_117());
        graphetsList.add(graphlet_118());
        graphetsList.add(graphlet_119());

        graphetsList.add(graphlet_120());
        graphetsList.add(graphlet_121());
        graphetsList.add(graphlet_122());
        graphetsList.add(graphlet_123());
        graphetsList.add(graphlet_124());
        graphetsList.add(graphlet_125());
        graphetsList.add(graphlet_126());
        graphetsList.add(graphlet_127());
        graphetsList.add(graphlet_128());
        graphetsList.add(graphlet_129());

        graphetsList.add(graphlet_130());
        graphetsList.add(graphlet_131());
        graphetsList.add(graphlet_132());
        graphetsList.add(graphlet_133());
        graphetsList.add(graphlet_154());
        graphetsList.add(graphlet_134());
        graphetsList.add(graphlet_135());
        graphetsList.add(graphlet_136());
        graphetsList.add(graphlet_137());
        graphetsList.add(graphlet_138());
        graphetsList.add(graphlet_139());

        graphetsList.add(graphlet_140());
        graphetsList.add(graphlet_141());
        graphetsList.add(graphlet_142());
        graphetsList.add(graphlet_143());
        graphetsList.add(graphlet_144());
        graphetsList.add(graphlet_145());
        graphetsList.add(graphlet_155());
        graphetsList.add(graphlet_146());
        graphetsList.add(graphlet_147());
        graphetsList.add(graphlet_148());
        graphetsList.add(graphlet_149());

        graphetsList.add(graphlet_150());
        graphetsList.add(graphlet_151());
        graphetsList.add(graphlet_152());
        graphetsList.add(graphlet_153());


    }


    public static void getDirectedGraphletDegreeVector(SubnetCalculator.SubNet sn) {
        int[][] DGDV = new int[sn.getSubNode().size()][98];


    }

    public static void generateGraphletsNode3() {
        graphetsList.clear();
        //2-NODE
        graphetsList.add(graphlet_1());
        graphetsList.add(graphlet_2());
        //3-NODE
        graphetsList.add(graphlet_3());
        graphetsList.add(graphlet_4());
        graphetsList.add(graphlet_5());
        graphetsList.add(graphlet_6());
        graphetsList.add(graphlet_7());
        graphetsList.add(graphlet_8());
    }

    public static void generateGraphletsNode4() {
        generateGraphletsNode3();
        graphetsList.add(graphlet_9());
        graphetsList.add(graphlet_10());
        graphetsList.add(graphlet_11());
        graphetsList.add(graphlet_12());
        graphetsList.add(graphlet_13());
        graphetsList.add(graphlet_14());
        graphetsList.add(graphlet_15());
        graphetsList.add(graphlet_16());

        graphetsList.add(graphlet_17());
        graphetsList.add(graphlet_18());
        graphetsList.add(graphlet_19());
        graphetsList.add(graphlet_20());
        graphetsList.add(graphlet_21());
        graphetsList.add(graphlet_22());
        graphetsList.add(graphlet_23());
        graphetsList.add(graphlet_24());

        graphetsList.add(graphlet_25());
        graphetsList.add(graphlet_26());
        graphetsList.add(graphlet_27());
        graphetsList.add(graphlet_28());
        graphetsList.add(graphlet_29());
        graphetsList.add(graphlet_30());
        graphetsList.add(graphlet_31());
    }

    public static void generateGraphletsNode5() {
        generateGraphletsNode4();
        graphetsList.add(graphlet_32());
        graphetsList.add(graphlet_33());
        graphetsList.add(graphlet_34());
        graphetsList.add(graphlet_35());
        graphetsList.add(graphlet_36());
        graphetsList.add(graphlet_37());
        graphetsList.add(graphlet_38());
        graphetsList.add(graphlet_39());
        graphetsList.add(graphlet_40());
        graphetsList.add(graphlet_41());
        //graphetsList.add(graphlet_42());
        //graphetsList.add(graphlet_43());
        graphetsList.add(graphlet_44());
        graphetsList.add(graphlet_45());
        graphetsList.add(graphlet_46());
        graphetsList.add(graphlet_47());
        graphetsList.add(graphlet_48());
        graphetsList.add(graphlet_49());
        graphetsList.add(graphlet_50());
        graphetsList.add(graphlet_51());
        graphetsList.add(graphlet_52());
        graphetsList.add(graphlet_53());
        //graphetsList.add(graphlet_54());
        //graphetsList.add(graphlet_55());
        graphetsList.add(graphlet_56());
        graphetsList.add(graphlet_57());
        graphetsList.add(graphlet_58());
        graphetsList.add(graphlet_59());
        graphetsList.add(graphlet_60());
        graphetsList.add(graphlet_61());
        graphetsList.add(graphlet_62());
        graphetsList.add(graphlet_63());
        graphetsList.add(graphlet_64());
        graphetsList.add(graphlet_65());
        graphetsList.add(graphlet_66());
        graphetsList.add(graphlet_67());
        graphetsList.add(graphlet_68());
        graphetsList.add(graphlet_69());

        graphetsList.add(graphlet_70());
        graphetsList.add(graphlet_71());
        graphetsList.add(graphlet_72());
        graphetsList.add(graphlet_73());
        graphetsList.add(graphlet_74());
        graphetsList.add(graphlet_75());
        graphetsList.add(graphlet_76());
        graphetsList.add(graphlet_77());
        graphetsList.add(graphlet_78());
        graphetsList.add(graphlet_79());

        graphetsList.add(graphlet_80());
        graphetsList.add(graphlet_81());
        graphetsList.add(graphlet_82());
        graphetsList.add(graphlet_83());
        graphetsList.add(graphlet_84());
        graphetsList.add(graphlet_85());
        graphetsList.add(graphlet_86());
        graphetsList.add(graphlet_87());
        graphetsList.add(graphlet_88());
        graphetsList.add(graphlet_89());

        graphetsList.add(graphlet_90());
        graphetsList.add(graphlet_91());
        graphetsList.add(graphlet_92());
        graphetsList.add(graphlet_93());
        graphetsList.add(graphlet_94());
        graphetsList.add(graphlet_95());
        graphetsList.add(graphlet_96());
        graphetsList.add(graphlet_97());
        graphetsList.add(graphlet_98());
        graphetsList.add(graphlet_99());


        graphetsList.add(graphlet_100());
        graphetsList.add(graphlet_101());
        graphetsList.add(graphlet_102());
        graphetsList.add(graphlet_103());
        graphetsList.add(graphlet_104());
        graphetsList.add(graphlet_105());
        graphetsList.add(graphlet_106());
        graphetsList.add(graphlet_107());
        graphetsList.add(graphlet_108());
        graphetsList.add(graphlet_109());

        graphetsList.add(graphlet_110());
        graphetsList.add(graphlet_111());
        graphetsList.add(graphlet_112());
        graphetsList.add(graphlet_113());
        graphetsList.add(graphlet_114());
        graphetsList.add(graphlet_115());
        graphetsList.add(graphlet_116());
        graphetsList.add(graphlet_117());
        graphetsList.add(graphlet_118());
        graphetsList.add(graphlet_119());

        graphetsList.add(graphlet_120());
        graphetsList.add(graphlet_121());
        graphetsList.add(graphlet_122());
        graphetsList.add(graphlet_123());
        graphetsList.add(graphlet_124());
        graphetsList.add(graphlet_125());
        graphetsList.add(graphlet_126());
        graphetsList.add(graphlet_127());
        graphetsList.add(graphlet_128());
        graphetsList.add(graphlet_129());

        graphetsList.add(graphlet_130());
        graphetsList.add(graphlet_131());
        graphetsList.add(graphlet_132());
        graphetsList.add(graphlet_133());
        graphetsList.add(graphlet_154());
        graphetsList.add(graphlet_134());
        graphetsList.add(graphlet_135());
        graphetsList.add(graphlet_136());
        graphetsList.add(graphlet_137());
        graphetsList.add(graphlet_138());
        graphetsList.add(graphlet_139());

        graphetsList.add(graphlet_140());
        graphetsList.add(graphlet_141());
        graphetsList.add(graphlet_142());
        graphetsList.add(graphlet_143());
        graphetsList.add(graphlet_144());
        graphetsList.add(graphlet_145());
        graphetsList.add(graphlet_155());
        graphetsList.add(graphlet_146());
        graphetsList.add(graphlet_147());
        graphetsList.add(graphlet_148());
        graphetsList.add(graphlet_149());

        graphetsList.add(graphlet_150());
        graphetsList.add(graphlet_151());
        graphetsList.add(graphlet_152());
        graphetsList.add(graphlet_153());

        ArrayList<Place> places = new ArrayList<>();
        ArrayList<Transition> transitions = new ArrayList<>();
        ArrayList<Arc> arcs = new ArrayList<>();
        IOprotocols io = new IOprotocols();

        for (SubnetCalculator.SubNet st : graphetsList) {
            places.addAll(st.getSubPlaces());
            transitions.addAll(st.getSubTransitions());
            arcs.addAll(st.getSubArcs());
        }

        //io.writePNT("ramadan.pnt", places, transitions, arcs);
    }

    public static void cleanAll() {
        graphetsList.clear();
        globalOrbitMap.clear();
        uniqGraphlets.clear();
        sortedGraphlets.clear(); //TODO dupikaty
        graphlets.clear();


    }

    public static void getDirectedRelativeGraphletFrequencyDistributionDistance() {
        int[] N = new int[graphletNumber];


        for (Struct struct : uniqGraphlets) {
            //int i = struct.graphletID;
            N[struct.graphletID]++;
            //DGDV[]
        }

        int T = IntStream.of(N).sum();

        double[] NzDaszkiem = new double[graphletNumber];

        for (int i = 0; i < graphletNumber; i++) {
            NzDaszkiem[i] = Math.log(N[i]) / T;
        }


    }

    public static void getDirectedGraphletDegreeDistributionAgreement() {

    }

    public static void getDirectedGraphDegreeVectorSimilarity() {

    }

    private static int fromWhichGrahlet(Node n) {
        int index = -1;
        int wiel = 0;
        for (SubnetCalculator.SubNet subNet : graphetsList) {
            if (subNet.getSubNode().stream().anyMatch(x -> x.getID() == n.getID()))
            //if (graphetsList.get(i).getSubNode().contains(n))
            {
                index = subNet.getSubNetID();
                //index = i;
                wiel++;
            }
        }
        if (wiel > 1) {
            System.out.println("Cos SPierdolono!!!");
        }
        return index;
    }

    public static int[] vectorOrbit(Node n, boolean test) {
        int[] ov = new int[globalOrbitMap.size()];
        int iterac = 0;

        StringBuilder text = new StringBuilder();
        if (test) {
            ov[59] = getOrbitValue(n, globalOrbitMap.get(59));
        } else {
            for (Entry<Integer, Node> map : globalOrbitMap.entrySet()) {
                Node m = map.getValue();
                int result = getOrbitValue(n, m);
                ov[iterac] = result;
                //System.out.println("Orbita " + iterac + " = " + result);
                text.append(result).append(", ");
                iterac++;
            }
        }

        //JOptionPane.showMessageDialog(null,text);
        return ov;
    }

    public static void getFoundGraphlets() {
        ArrayList<ArrayList<ArrayList<Struct>>> resultList = new ArrayList<>();

        int orbitIndex = 0;
        for (Entry<Integer, Node> entry : globalOrbitMap.entrySet()) {
            ArrayList<ArrayList<Struct>> fromThisOrbit = new ArrayList<>();

            for (Node n : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes()) {
                if (n.getType() == entry.getValue().getType()) {
                    ArrayList<Struct> foundGraphletsApp = findGraphlet(entry.getValue(), n);
                    fromThisOrbit.add(foundGraphletsApp);
                } else {
                    fromThisOrbit.add(new ArrayList<>());
                }
            }
            resultList.add(fromThisOrbit);
        }

        //sprawdź czy map jest praw

        graphlets = resultList;

        sortGraphletsByOrbit();
        uniqGraphlets();
        //return resultList;
    }

    public static void getFoundServerGraphlets(PetriNet pn) {
        ArrayList<ArrayList<ArrayList<Struct>>> resultList = new ArrayList<>();

        int orbitIndex = 0;
        for (Entry<Integer, Node> entry : globalOrbitMap.entrySet()) {
            ArrayList<ArrayList<Struct>> fromThisOrbit = new ArrayList<>();

            for (Node n : pn.getNodes()) {
                if (n.getType() == entry.getValue().getType()) {
                    ArrayList<Struct> foundGraphletsApp = findGraphlet(entry.getValue(), n);
                    fromThisOrbit.add(foundGraphletsApp);
                } else {
                    fromThisOrbit.add(new ArrayList<>());
                }
            }
            resultList.add(fromThisOrbit);
        }

        //sprawdź czy map jest praw

        graphlets = resultList;

        sortGraphletsByOrbit();
        uniqGraphlets();
        //return resultList;
    }

    private static int getOrbitValue(Node n, Node m) {
        if (n.getType() == m.getType()) {
            ArrayList<Struct> foundGraphletsApp;
            foundGraphletsApp = findGraphlet(m, n);

            //catch multiple arcs
            if (multipleArcCheck) {
                ArrayList<Struct> nonMultiArcStruct = new ArrayList<>();
                for (Struct graphlet : foundGraphletsApp) {
                    int multiplier = 1;
                    for (Entry<Arc, Arc> entry : graphlet.mapaArcow.entrySet()) {
                        multiplier = multiplier * entry.getValue().getWeight();
                    }


                    for (int i = 0; i < multiplier; i++) {
                        nonMultiArcStruct.add(graphlet);
                    }
                    //System.out.println("------->Multi--->" + multiplier);
                }
                foundGraphletsApp = nonMultiArcStruct;
            }
            else
            {

            }

            return foundGraphletsApp.size();
        } else {
            return -1;
        }
    }

    private static ArrayList<Struct> findGraphlet(Node orbita, Node node) {

        ArrayList<Struct> listOfProperGraphlets = new ArrayList<>();
        ArrayList<Struct> listOfPosibleGraphlets = new ArrayList<>();
        int graphletID = fromWhichGrahlet(orbita);
        ArrayList<Node> nodesFromGraohlet = new ArrayList<>(graphetsList.get(graphletID).getSubNode());
        ArrayList<Arc> arcsFromGraohlet = new ArrayList<>(graphetsList.get(graphletID).getSubArcs());

        HashMap<Node, Node> nodeMap = new HashMap<>();
        HashMap<Arc, Arc> arcMap = new HashMap<>();
        nodeMap.put(orbita, node);

        Struct startStructure = new Struct(nodeMap, arcMap);
        startStructure.graphletID = graphletID;

        listOfPosibleGraphlets.add(startStructure);

        while (listOfPosibleGraphlets.size() > 0) {
            Struct toExtend = listOfPosibleGraphlets.get(0);
            listOfPosibleGraphlets.remove(0);

            if (toExtend.mapa.entrySet().size() == graphetsList.get(graphletID).getSubNode().size() && toExtend.mapaArcow.entrySet().size() == graphetsList.get(graphletID).getSubArcs().size()) {

                boolean properGraphlet = true;

                ArrayList<Arc> properArcList = new ArrayList<>(toExtend.mapaArcow.values());
                ArrayList<Node> properNodeList = new ArrayList<>(toExtend.mapa.values());

                for (Arc a : properArcList) {
                    if (!properNodeList.contains(a.getStartNode()) || !properNodeList.contains(a.getEndNode()))
                        properGraphlet = false;
                }

                for (Node n : toExtend.mapa.values()) {
                    for (Arc a : n.getOutInArcs()) {
                        if (properNodeList.contains(a.getEndNode()) && properNodeList.contains(a.getStartNode())) {
                            if (!properArcList.contains(a)) {
                                properGraphlet = false;
                                //System.out.println("-> nieprawidlowy graphlet");
                            }
                        }
                    }
                }

                /*
                for (Node n : toExtend.mapa.values()) {
                    for (Arc a : n.getOutArcs()) {
                        if (properNodeList.contains(a.getEndNode()) && properNodeList.contains(a.getStartNode())) {
                            if (!properArcList.contains(a)) {
                                properGraphlet = false;
                                //System.out.println("-> nieprawidlowy graphlet");
                            }
                        }
                    }
                    for (Arc a : n.getInArcs()) {
                        if (properNodeList.contains(a.getStartNode()) && properNodeList.contains(a.getEndNode())) {
                            if (!properArcList.contains(a)) {
                                properGraphlet = false;
                                //System.out.println("-> nieprawidlowy graphlet");
                            }
                        }
                    }
                }
                */

                if (properGraphlet) {
                    listOfProperGraphlets.add(toExtend);
                }
            } else {
                Arc freeGrahletArcToAdd = takeFreeGraphletArcToAdd(toExtend, arcsFromGraohlet);
                if (freeGrahletArcToAdd != null) {
                    Node freeGrahletNodeToAdd = takeFreeGraphletNodeToAdd(toExtend, freeGrahletArcToAdd);
                    ArrayList<Arc> possibleExtensions = findExtensions(freeGrahletArcToAdd, toExtend);

                    for (Arc nextArc : possibleExtensions) {
                        if (!toExtend.mapaArcow.containsValue(nextArc)) {
                            Struct structureToAdd = new Struct((HashMap<Node, Node>) toExtend.mapa.clone(), (HashMap<Arc, Arc>) toExtend.mapaArcow.clone());
                            structureToAdd.graphletID = graphletID;

                            if (!structureToAdd.mapa.containsValue(nextArc.getStartNode())) {
                                structureToAdd.mapa.put(freeGrahletNodeToAdd, nextArc.getStartNode());
                            } else if (!structureToAdd.mapa.containsValue(nextArc.getEndNode())) {
                                structureToAdd.mapa.put(freeGrahletNodeToAdd, nextArc.getEndNode());
                            }

                            structureToAdd.mapaArcow.put(freeGrahletArcToAdd, nextArc);
                            listOfPosibleGraphlets.add(structureToAdd);
                        }
                    }
                }
            }
        }


        //tylko unikalne
        ArrayList<Struct> uniqeListOfProperGraphlets = new ArrayList<>();

        for (int i = 0; i < listOfProperGraphlets.size(); i++) {
            Struct struct = listOfProperGraphlets.get(i);
            int counter = 0;
            for (int j = i; j < listOfProperGraphlets.size(); j++) {
                Struct struct2 = listOfProperGraphlets.get(j);

                if (struct.mapa.values().containsAll(struct2.mapa.values()) && struct.mapaArcow.values().containsAll(struct2.mapaArcow.values()) && struct.mapa.keySet().equals(struct2.mapa.keySet()) && struct.mapaArcow.keySet().equals(struct2.mapaArcow.keySet()))
                    counter++;
            }
            if (counter == 1) {
                uniqeListOfProperGraphlets.add(listOfProperGraphlets.get(i));
            }
        }
        ArrayList<Struct> ProperlyMapGraphlets = new ArrayList<>();
        for (Struct st : uniqeListOfProperGraphlets) {
            SubnetCalculator.SubNet graphlet = graphetsList.get(st.graphletID);

            boolean bubel = false;

            for (Node n : graphlet.getSubNode()) {
                for (Arc a : n.getInArcs()) {
                    Arc mapedArc = st.mapaArcow.get(a);

                    Node startNode = mapedArc.getStartNode();
                    Node endNode = mapedArc.getEndNode();
                    Node s1 = keys(st.mapa, startNode).findFirst().get();
                    Node s2 = keys(st.mapa, endNode).findFirst().get();

                    if (graphlet.getSubArcs().stream().noneMatch(x -> x.getStartNode().getID() == s1.getID() && x.getEndNode().getID() == s2.getID())) {
                        bubel = true;
                    }

                    //if(getKeysByValue(st.mapa,startNode) && st.mapa.containsValue(endNode))

                    //int originalID = a.getStartNode().getID();
                    //if(!st.mapa.containsKey(mapedArc.getStartNode()))
                    //    bubel=true;

                        /*
                    int mapedID = st.mapa.get(mapedArc.getStartNode()).getID();
                    if(originalID!= mapedID)
                        bubel=true;
                    */

                    if (!st.mapaArcow.containsKey(a))
                        bubel = true;
                }
            }

            for (Arc a : graphlet.getSubArcs()) {
                if (!st.mapaArcow.containsKey(a)) {
                    bubel = true;
                    break;
                }
            }

            if (!bubel)
                ProperlyMapGraphlets.add(st);
        }

        return ProperlyMapGraphlets;
    }

    public static <K, V> Stream<K> keys(Map<K, V> map, V value) {
        return map
                .entrySet()
                .stream()
                .filter(entry -> value.equals(entry.getValue()))
                .map(Map.Entry::getKey);
    }

    private static Node takeFreeGraphletNodeToAdd(Struct toExtend, Arc freeGrahletArcToAdd) {
        ArrayList<Node> result = new ArrayList<>();

        //potencjalny pronlem przy pętlach
        for (Entry<Node, Node> entry : toExtend.mapa.entrySet()) {

            //połączony łukiem weścowym
            if (freeGrahletArcToAdd.getEndNode().getID() == entry.getKey().getID()) {
                return freeGrahletArcToAdd.getStartNode();
            }

            //freeGrahletArcToAdd łukiem wyjściowym
            if (freeGrahletArcToAdd.getStartNode().getID() == entry.getKey().getID()) {
                return freeGrahletArcToAdd.getEndNode();
            }

        }

        return null;
    }


    private static ArrayList<Arc> findExtensions(Arc freeGrahletArcToAdd, Struct toExtend) {
        ArrayList<Arc> result = new ArrayList<>();

        for (Entry<Node, Node> entry : toExtend.mapa.entrySet()) {

            //połączony łukiem weścowym
            if (freeGrahletArcToAdd.getEndNode().getID() == entry.getKey().getID()) {

                //wcześniej sprawdzasz czy to nie jest cykl w ramach grafletu

                //zbió© potencjalnych rozszrrzeń - zwracasz ich start nody
                result.addAll(entry.getValue().getInArcs());
            }

            //freeGrahletArcToAdd łukiem wyjściowym
            if (freeGrahletArcToAdd.getStartNode().getID() == entry.getKey().getID()) {
                //result.add(a.getEndNode());
                result.addAll(entry.getValue().getOutArcs());
            }
        }

        return result;
    }

    private static Arc takeFreeGraphletArcToAdd(Struct toExtend, ArrayList<Arc> arcsFromGraohlet) {
        Arc result = null;

        for (Entry<Node, Node> entry : toExtend.mapa.entrySet()) {
            ArrayList<Arc> connectedToRoot = entry.getKey().getOutInArcs();
            connectedToRoot.removeAll(toExtend.mapaArcow.keySet());
            if (connectedToRoot.size() > 0) {
                result = connectedToRoot.get(0);
                break;
            }
        }
        return result;
    }

    private static void sortGraphletsByOrbit() {

        for (ArrayList<ArrayList<Struct>> listForNode : graphlets) {
            ArrayList<Struct> gartherd = new ArrayList<>();
            for (ArrayList<Struct> list : listForNode)
                gartherd.addAll(list);

            sortedGraphlets.add(gartherd);
        }
    }

    private static ArrayList<Integer> getKeyNodeIDs(HashMap<Node, Node> hm) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Node n : hm.keySet()) {
            ids.add(n.getID());
        }
        return ids;
    }

    private static ArrayList<Integer> getStructNodeIDs(ArrayList<Node> nl) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (Node n : nl) {
            ids.add(n.getID());
        }
        return ids;
    }

    private static void uniqGraphlets() {

        ArrayList<Struct> propList = new ArrayList<>();
        //is you id porpper
        for (ArrayList<ArrayList<Struct>> listForNode : graphlets) {
            for (ArrayList<Struct> list : listForNode)
                for (Struct s : list)
                    if (getKeyNodeIDs(s.mapa).containsAll(getStructNodeIDs(graphetsList.get(s.graphletID).getSubNode())) && s.mapaArcow.keySet().containsAll(graphetsList.get(s.graphletID).getSubArcs())) {
                        propList.add(s);
                    } else {
                        System.out.println("Błędny id");
                    }
        }


        //for (ArrayList<ArrayList<Struct>> listForNode : graphlets) {
        //for(ArrayList<Struct> list : listForNode)
        for (Struct s : propList)
            //if(uniqGraphlets.stream().noneMatch(x-> x.mapa.values().containsAll(s.mapa.values())) && uniqGraphlets.stream().noneMatch(x-> x.mapaArcow.values().containsAll(s.mapaArcow.values())))
            if (uniqGraphlets.stream().noneMatch(x -> x.mapa.keySet().equals(s.mapa.keySet()) && x.mapa.values().containsAll(s.mapa.values()) && x.mapaArcow.keySet().equals(s.mapaArcow.keySet()) && x.graphletID == s.graphletID)) {
                if (s.graphletID == 999) {
                    System.out.println("You not suppost to be here");
                    System.out.println(s.graphletID);
                    System.out.println(s.mapa.keySet());
                    System.out.println(s.mapa.values());
                    System.out.println(s.mapaArcow.keySet());
                    System.out.println(s.mapaArcow.values());//wek
                }
                uniqGraphlets.add(s);
            }
        //}


        if (multipleArcCheck) {
            ArrayList<Struct> nonMultiArcStruct = new ArrayList<>();
            for (Struct graphlet : uniqGraphlets) {
                int multiplier = 1;
                for (Entry<Arc, Arc> entry : graphlet.mapaArcow.entrySet()) {
                    multiplier = multiplier * entry.getValue().getWeight();
                }


                for (int i = 0; i < multiplier; i++) {
                    nonMultiArcStruct.add(graphlet);
                }
                //System.out.println("------->Multi--->" + multiplier);
            }
            uniqGraphlets = nonMultiArcStruct;
        }



        ArrayList<Struct> inproper = new ArrayList<>();
        for (Struct s : uniqGraphlets) {
            ArrayList<Struct> ip = uniqGraphlets.stream().filter(x -> x.mapaArcow.values().containsAll(s.mapaArcow.values())).collect(Collectors.toCollection(ArrayList::new));
        }
    }

    private static SubnetCalculator.SubNet graphlet_1() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(0, t1);
        sn.orbitMap.put(1, p1);
        globalOrbitMap.putAll(sn.orbitMap);
        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_2() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(2, p1);
        sn.orbitMap.put(3, t1);
        globalOrbitMap.putAll(sn.orbitMap);
        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_3() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);
        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_4() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(7, t1);
        sn.orbitMap.put(8, p1);
        globalOrbitMap.putAll(sn.orbitMap);
        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_5() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(9, t1);
        sn.orbitMap.put(10, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_6() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(12, t1);
        sn.orbitMap.put(11, p1);
        sn.orbitMap.put(13, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_7() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(15, t1);
        sn.orbitMap.put(14, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_8() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(17, t1);
        sn.orbitMap.put(16, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }
///

    private static SubnetCalculator.SubNet graphlet_9() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(18, t1);
        sn.orbitMap.put(19, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_10() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(20, t1);
        sn.orbitMap.put(21, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_11() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(22, t1);
        sn.orbitMap.put(23, p1);
        sn.orbitMap.put(24, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_12() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(25, t1);
        sn.orbitMap.put(26, p1);
        sn.orbitMap.put(27, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


///

    private static SubnetCalculator.SubNet graphlet_13() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(29, t1);
        sn.orbitMap.put(28, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_14() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(31, t1);
        sn.orbitMap.put(30, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_15() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(33, t1);
        sn.orbitMap.put(32, p1);
        sn.orbitMap.put(34, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_16() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(36, t1);
        sn.orbitMap.put(35, p1);
        sn.orbitMap.put(37, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    // 3

    private static SubnetCalculator.SubNet graphlet_17() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(38, t1);
        sn.orbitMap.put(39, p1);
        sn.orbitMap.put(40, t2);
        sn.orbitMap.put(41, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_18() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(42, t1);
        sn.orbitMap.put(43, p1);
        sn.orbitMap.put(44, t2);
        sn.orbitMap.put(45, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_19() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(46, t1);
        sn.orbitMap.put(47, p1);
        sn.orbitMap.put(48, t2);
        sn.orbitMap.put(49, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_20() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(50, t1);
        sn.orbitMap.put(51, p1);
        sn.orbitMap.put(52, t2);
        sn.orbitMap.put(53, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    // 3.1

    private static SubnetCalculator.SubNet graphlet_21() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        /* stare
        sn.orbitMap.put(54, p1);
        sn.orbitMap.put(55, t1);
        sn.orbitMap.put(56, p2);
        sn.orbitMap.put(57, t2);
*/

        sn.orbitMap.put(54, t1);
        sn.orbitMap.put(55, p1);
        sn.orbitMap.put(56, t2);
        sn.orbitMap.put(57, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_22() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        /*
        sn.orbitMap.put(58, p1);
        sn.orbitMap.put(59, t1);
        sn.orbitMap.put(60, p2);
        sn.orbitMap.put(61, t2);
        */

        sn.orbitMap.put(58, t1);
        sn.orbitMap.put(59, p1);
        sn.orbitMap.put(60, t2);
        sn.orbitMap.put(61, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_23() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
  /*
        sn.orbitMap.put(62, p1);
        sn.orbitMap.put(63, t1);
        sn.orbitMap.put(64, p2);
        sn.orbitMap.put(65, t2);
*/
        sn.orbitMap.put(62, t1);
        sn.orbitMap.put(63, p1);
        sn.orbitMap.put(64, t2);
        sn.orbitMap.put(65, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_24() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
/*
        sn.orbitMap.put(66, p1);
        sn.orbitMap.put(67, t1);
        sn.orbitMap.put(68, p2);
        sn.orbitMap.put(69, t2);
*/
        sn.orbitMap.put(66, t1);
        sn.orbitMap.put(67, p1);
        sn.orbitMap.put(68, t2);
        sn.orbitMap.put(69, p2);


        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    // 5

    private static SubnetCalculator.SubNet graphlet_25() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(70, t1);
        sn.orbitMap.put(71, p1);
        sn.orbitMap.put(72, t2);
        sn.orbitMap.put(73, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_26() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(74, t1);
        sn.orbitMap.put(75, p1);
        sn.orbitMap.put(76, t2);
        sn.orbitMap.put(77, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_27() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(78, t1);
        sn.orbitMap.put(79, p1);
        sn.orbitMap.put(80, t2);
        sn.orbitMap.put(81, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_28() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(82, t1);
        sn.orbitMap.put(83, p1);
        sn.orbitMap.put(84, t2);
        sn.orbitMap.put(85, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_29() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(86, t1);
        sn.orbitMap.put(87, p1);
        sn.orbitMap.put(88, t2);
        sn.orbitMap.put(89, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_30() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(90, t1);
        sn.orbitMap.put(91, p1);
        sn.orbitMap.put(92, t2);
        sn.orbitMap.put(93, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_31() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(94, t1);
        sn.orbitMap.put(95, p1);
        sn.orbitMap.put(96, t2);
        sn.orbitMap.put(97, p2);

        //89 do poprawy
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //  9 t-t

    private static SubnetCalculator.SubNet graphlet_32() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(98, t1);
        sn.orbitMap.put(99, p1);
        sn.orbitMap.put(100, t2);
        sn.orbitMap.put(101, p2);
        sn.orbitMap.put(102, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_33() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(103, t1);
        sn.orbitMap.put(104, p1);
        sn.orbitMap.put(105, t2);
        sn.orbitMap.put(106, p2);
        sn.orbitMap.put(107, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_34() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(108, t1);
        sn.orbitMap.put(109, p1);
        sn.orbitMap.put(110, t2);
        sn.orbitMap.put(111, p2);
        sn.orbitMap.put(112, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_35() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(113, t1);
        sn.orbitMap.put(114, p1);
        sn.orbitMap.put(115, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_36() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(116, t1);
        sn.orbitMap.put(117, p1);
        sn.orbitMap.put(118, t2);
        sn.orbitMap.put(119, p2);
        sn.orbitMap.put(120, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_37() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(121, t1);
        sn.orbitMap.put(122, p1);
        sn.orbitMap.put(123, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_38() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(124, t1);
        sn.orbitMap.put(125, p1);
        sn.orbitMap.put(126, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_39() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(127, t1);
        sn.orbitMap.put(128, p1);
        sn.orbitMap.put(129, t2);
        sn.orbitMap.put(130, p2);
        sn.orbitMap.put(131, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_40() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(132, t1);
        sn.orbitMap.put(133, p1);
        sn.orbitMap.put(134, t2);
        sn.orbitMap.put(135, p2);
        sn.orbitMap.put(136, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_41() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(137, t1);
        sn.orbitMap.put(138, p1);
        sn.orbitMap.put(139, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    /*
    //duplikat
    private static SubnetCalculator.SubNet graphlet_42() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(103, t1);
        sn.orbitMap.put(104, p1);
        sn.orbitMap.put(105, t2);
        sn.orbitMap.put(106, p2);
        sn.orbitMap.put(107, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //duplikat
    private static SubnetCalculator.SubNet graphlet_43() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }
*/
    // 9 p-p


    private static SubnetCalculator.SubNet graphlet_44() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(140, t1);
        sn.orbitMap.put(141, p1);
        sn.orbitMap.put(142, t2);
        sn.orbitMap.put(143, p2);
        sn.orbitMap.put(144, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_45() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(145, t1);
        sn.orbitMap.put(146, p1);
        sn.orbitMap.put(147, t2);
        sn.orbitMap.put(148, p2);
        sn.orbitMap.put(149, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_46() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(150, t1);
        sn.orbitMap.put(151, p1);
        sn.orbitMap.put(152, t2);
        sn.orbitMap.put(153, p2);
        sn.orbitMap.put(154, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_47() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(155, t1);
        sn.orbitMap.put(156, p1);
        sn.orbitMap.put(157, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_48() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(158, t1);
        sn.orbitMap.put(159, p1);
        sn.orbitMap.put(160, t2);
        sn.orbitMap.put(161, p2);
        sn.orbitMap.put(162, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_49() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(163, t1);
        sn.orbitMap.put(164, p1);
        sn.orbitMap.put(165, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_50() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(166, t1);
        sn.orbitMap.put(167, p1);
        sn.orbitMap.put(168, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_51() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(169, t1);
        sn.orbitMap.put(170, p1);
        sn.orbitMap.put(171, t2);
        sn.orbitMap.put(172, p2);
        sn.orbitMap.put(173, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_52() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(174, t1);
        sn.orbitMap.put(175, p1);
        sn.orbitMap.put(176, t2);
        sn.orbitMap.put(177, p2);
        sn.orbitMap.put(178, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_53() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(179, t1);
        sn.orbitMap.put(180, p1);
        sn.orbitMap.put(181, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    /*
    private static SubnetCalculator.SubNet graphlet_54() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_55() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }
*/

    // 10

    private static SubnetCalculator.SubNet graphlet_56() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(182, p1);
        sn.orbitMap.put(183, t1);
        sn.orbitMap.put(184, p2);
        sn.orbitMap.put(185, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_57() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(186, p1);
        sn.orbitMap.put(187, t1);
        sn.orbitMap.put(188, p2);
        sn.orbitMap.put(189, t2);
        sn.orbitMap.put(190, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_58() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(191, p1);
        sn.orbitMap.put(192, t1);
        sn.orbitMap.put(193, p2);
        sn.orbitMap.put(194, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_59() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(195, p1);
        sn.orbitMap.put(196, t1);
        sn.orbitMap.put(197, p2);
        sn.orbitMap.put(198, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_60() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(199, p1);
        sn.orbitMap.put(200, t1);
        sn.orbitMap.put(201, p2);
        sn.orbitMap.put(202, t2);
        sn.orbitMap.put(203, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_61() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(204, p1);
        sn.orbitMap.put(205, t1);
        sn.orbitMap.put(206, p2);
        sn.orbitMap.put(207, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //part

    private static SubnetCalculator.SubNet graphlet_62() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(208, p1);
        sn.orbitMap.put(209, t1);
        sn.orbitMap.put(210, p2);
        sn.orbitMap.put(211, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_63() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(212, p1);
        sn.orbitMap.put(213, t1);
        sn.orbitMap.put(214, p2);
        sn.orbitMap.put(215, t2);
        sn.orbitMap.put(216, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_64() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(217, p1);
        sn.orbitMap.put(218, t1);
        sn.orbitMap.put(219, p2);
        sn.orbitMap.put(220, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //part

    private static SubnetCalculator.SubNet graphlet_65() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(221, p1);
        sn.orbitMap.put(222, t1);
        sn.orbitMap.put(223, p2);
        sn.orbitMap.put(224, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_66() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(225, p1);
        sn.orbitMap.put(226, t1);
        sn.orbitMap.put(227, p2);
        sn.orbitMap.put(228, t2);
        sn.orbitMap.put(229, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_67() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(230, p1);
        sn.orbitMap.put(231, t1);
        sn.orbitMap.put(232, p2);
        sn.orbitMap.put(233, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //p

    private static SubnetCalculator.SubNet graphlet_68() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(234, p1);
        sn.orbitMap.put(235, t1);
        sn.orbitMap.put(236, p2);
        sn.orbitMap.put(237, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_69() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(238, p1);
        sn.orbitMap.put(239, t1);
        sn.orbitMap.put(240, p2);
        sn.orbitMap.put(241, t2);
        sn.orbitMap.put(242, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_70() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(243, p1);
        sn.orbitMap.put(244, t1);
        sn.orbitMap.put(245, p2);
        sn.orbitMap.put(246, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_71() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(247, p1);
        sn.orbitMap.put(248, t1);
        sn.orbitMap.put(249, p2);
        sn.orbitMap.put(250, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_72() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(251, p1);
        sn.orbitMap.put(252, t1);
        sn.orbitMap.put(253, p2);
        sn.orbitMap.put(254, t2);
        sn.orbitMap.put(255, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_73() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(256, p1);
        sn.orbitMap.put(257, t1);
        sn.orbitMap.put(258, p2);
        sn.orbitMap.put(259, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //part

    private static SubnetCalculator.SubNet graphlet_74() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(260, p1);
        sn.orbitMap.put(261, t1);
        sn.orbitMap.put(262, p2);
        sn.orbitMap.put(263, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_75() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(264, p1);
        sn.orbitMap.put(265, t1);
        sn.orbitMap.put(266, p2);
        sn.orbitMap.put(267, t2);
        sn.orbitMap.put(268, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_76() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(269, p1);
        sn.orbitMap.put(270, t1);
        sn.orbitMap.put(271, p2);
        sn.orbitMap.put(272, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //part

    private static SubnetCalculator.SubNet graphlet_77() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(273, p1);
        sn.orbitMap.put(274, t1);
        sn.orbitMap.put(275, p2);
        sn.orbitMap.put(276, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_78() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(277, p1);
        sn.orbitMap.put(278, t1);
        sn.orbitMap.put(279, p2);
        sn.orbitMap.put(280, t2);
        sn.orbitMap.put(281, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_79() {
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(282, p1);
        sn.orbitMap.put(283, t1);
        sn.orbitMap.put(284, p2);
        sn.orbitMap.put(285, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //11

    private static SubnetCalculator.SubNet graphlet_80() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t4.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(286, p1);
        sn.orbitMap.put(287, t1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_81() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t3.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(288, p1);
        sn.orbitMap.put(289, t1);
        sn.orbitMap.put(290, t4);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_82() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(291, p1);
        sn.orbitMap.put(292, t1);
        sn.orbitMap.put(293, t3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_83() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(294, p1);
        sn.orbitMap.put(295, t1);
        sn.orbitMap.put(296, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_84() {
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p1.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(297, p1);
        sn.orbitMap.put(298, t1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_85() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(299, t1);
        sn.orbitMap.put(300, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_86() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p4.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(301, t1);
        sn.orbitMap.put(302, p1);
        sn.orbitMap.put(303, p4);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_87() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p4.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(304, t1);
        sn.orbitMap.put(305, p1);
        sn.orbitMap.put(306, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_88() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p4.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(307, t1);
        sn.orbitMap.put(308, p1);
        sn.orbitMap.put(309, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_89() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p4.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(310, t1);
        sn.orbitMap.put(311, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //16

    private static SubnetCalculator.SubNet graphlet_90() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(312, t1);
        sn.orbitMap.put(313, p1);
        sn.orbitMap.put(314, t2);
        sn.orbitMap.put(315, p2);
        sn.orbitMap.put(316, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_91() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(317, t1);
        sn.orbitMap.put(318, p1);
        sn.orbitMap.put(319, t2);
        sn.orbitMap.put(320, p2);
        sn.orbitMap.put(321, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_92() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(322, t1);
        sn.orbitMap.put(323, p1);
        sn.orbitMap.put(324, t2);
        sn.orbitMap.put(325, p2);
        sn.orbitMap.put(326, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_93() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(327, t1);
        sn.orbitMap.put(328, p1);
        sn.orbitMap.put(329, t2);
        sn.orbitMap.put(330, p2);
        sn.orbitMap.put(331, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_94() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(332, t1);
        sn.orbitMap.put(333, p1);
        sn.orbitMap.put(334, t2);
        sn.orbitMap.put(335, p2);
        sn.orbitMap.put(336, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_95() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(337, t1);
        sn.orbitMap.put(338, p1);
        sn.orbitMap.put(339, t2);
        sn.orbitMap.put(340, p2);
        sn.orbitMap.put(341, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    // // //

    private static SubnetCalculator.SubNet graphlet_96() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(342, t1);
        sn.orbitMap.put(343, p1);
        sn.orbitMap.put(344, t2);
        sn.orbitMap.put(345, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_97() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(346, t1);
        sn.orbitMap.put(347, p1);
        sn.orbitMap.put(348, t2);
        sn.orbitMap.put(349, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_98() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(350, t1);
        sn.orbitMap.put(351, p1);
        sn.orbitMap.put(352, t2);
        sn.orbitMap.put(353, p2);
        sn.orbitMap.put(354, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_99() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(355, t1);
        sn.orbitMap.put(356, p1);
        sn.orbitMap.put(357, t2);
        sn.orbitMap.put(358, p2);
        sn.orbitMap.put(359, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_100() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(360, t1);
        sn.orbitMap.put(361, p1);
        sn.orbitMap.put(362, t2);
        sn.orbitMap.put(363, p2);
        sn.orbitMap.put(364, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_101() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(365, t1);
        sn.orbitMap.put(366, p1);
        sn.orbitMap.put(367, t2);
        sn.orbitMap.put(368, p2);
        sn.orbitMap.put(369, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_102() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(370, t1);
        sn.orbitMap.put(371, p1);
        sn.orbitMap.put(372, t2);
        sn.orbitMap.put(373, p2);
        sn.orbitMap.put(374, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_103() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(375, t1);
        sn.orbitMap.put(376, p1);
        sn.orbitMap.put(377, t2);
        sn.orbitMap.put(378, p2);
        sn.orbitMap.put(379, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_104() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(380, t1);
        sn.orbitMap.put(381, p1);
        sn.orbitMap.put(382, t2);
        sn.orbitMap.put(383, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_105() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(384, t1);
        sn.orbitMap.put(385, p1);
        sn.orbitMap.put(386, t2);
        sn.orbitMap.put(387, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_106() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(388, t1);
        sn.orbitMap.put(389, p1);
        sn.orbitMap.put(390, t2);
        sn.orbitMap.put(391, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_107() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(392, t1);
        sn.orbitMap.put(393, p1);
        sn.orbitMap.put(394, t2);
        sn.orbitMap.put(395, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_108() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(396, t1);
        sn.orbitMap.put(397, p1);
        sn.orbitMap.put(398, t2);
        sn.orbitMap.put(399, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_109() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(400, t1);
        sn.orbitMap.put(401, p1);
        sn.orbitMap.put(402, t2);
        sn.orbitMap.put(403, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //16 p variant

    private static SubnetCalculator.SubNet graphlet_110() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(404, t1);
        sn.orbitMap.put(405, p1);
        sn.orbitMap.put(406, t2);
        sn.orbitMap.put(407, p2);
        sn.orbitMap.put(408, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_111() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(409, t1);
        sn.orbitMap.put(410, p1);
        sn.orbitMap.put(411, t2);
        sn.orbitMap.put(412, p2);
        sn.orbitMap.put(413, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_112() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(414, t1);
        sn.orbitMap.put(415, p1);
        sn.orbitMap.put(416, t2);
        sn.orbitMap.put(417, p2);
        sn.orbitMap.put(418, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_113() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(419, t1);
        sn.orbitMap.put(420, p1);
        sn.orbitMap.put(421, t2);
        sn.orbitMap.put(422, p2);
        sn.orbitMap.put(423, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_114() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(424, t1);
        sn.orbitMap.put(425, p1);
        sn.orbitMap.put(426, t2);
        sn.orbitMap.put(427, p2);
        sn.orbitMap.put(428, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_115() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(429, t1);
        sn.orbitMap.put(430, p1);
        sn.orbitMap.put(431, t2);
        sn.orbitMap.put(432, p2);
        sn.orbitMap.put(433, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    // // //

    private static SubnetCalculator.SubNet graphlet_116() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(434, t1);
        sn.orbitMap.put(435, p1);
        sn.orbitMap.put(436, t2);
        sn.orbitMap.put(437, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_117() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(438, t1);
        sn.orbitMap.put(439, p1);
        sn.orbitMap.put(440, t2);
        sn.orbitMap.put(441, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_118() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(442, t1);
        sn.orbitMap.put(443, p1);
        sn.orbitMap.put(444, t2);
        sn.orbitMap.put(445, p2);
        sn.orbitMap.put(446, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_119() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(447, t1);
        sn.orbitMap.put(448, p1);
        sn.orbitMap.put(449, t2);
        sn.orbitMap.put(450, p2);
        sn.orbitMap.put(451, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_120() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(452, t1);
        sn.orbitMap.put(453, p1);
        sn.orbitMap.put(454, t2);
        sn.orbitMap.put(455, p2);
        sn.orbitMap.put(456, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_121() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(457, t1);
        sn.orbitMap.put(458, p1);
        sn.orbitMap.put(459, t2);
        sn.orbitMap.put(460, p2);
        sn.orbitMap.put(461, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_122() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(462, t1);
        sn.orbitMap.put(463, p1);
        sn.orbitMap.put(464, t2);
        sn.orbitMap.put(465, p2);
        sn.orbitMap.put(466, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_123() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(467, t1);
        sn.orbitMap.put(468, p1);
        sn.orbitMap.put(469, t2);
        sn.orbitMap.put(470, p2);
        sn.orbitMap.put(471, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_124() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(472, t1);
        sn.orbitMap.put(473, p1);
        sn.orbitMap.put(474, t2);
        sn.orbitMap.put(475, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_125() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(476, t1);
        sn.orbitMap.put(477, p1);
        sn.orbitMap.put(478, t2);
        sn.orbitMap.put(479, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_126() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(480, t1);
        sn.orbitMap.put(481, p1);
        sn.orbitMap.put(482, t2);
        sn.orbitMap.put(483, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_127() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(484, t1);
        sn.orbitMap.put(485, p1);
        sn.orbitMap.put(486, t2);
        sn.orbitMap.put(487, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_128() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(488, t1);
        sn.orbitMap.put(489, p1);
        sn.orbitMap.put(490, t2);
        sn.orbitMap.put(491, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_129() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(492, t1);
        sn.orbitMap.put(493, p1);
        sn.orbitMap.put(494, t2);
        sn.orbitMap.put(495, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    // 20 t

    private static SubnetCalculator.SubNet graphlet_130() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(496, t1);
        sn.orbitMap.put(497, p1);
        sn.orbitMap.put(498, t2);
        sn.orbitMap.put(499, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_131() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(500, t1);
        sn.orbitMap.put(501, p1);
        sn.orbitMap.put(502, t2);
        sn.orbitMap.put(503, p2);
        sn.orbitMap.put(504, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //+
    private static SubnetCalculator.SubNet graphlet_132() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(505, t1);
        sn.orbitMap.put(506, p1);
        sn.orbitMap.put(507, t2);
        sn.orbitMap.put(508, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_133() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(509, t1);
        sn.orbitMap.put(510, p1);
        sn.orbitMap.put(511, t2);
        sn.orbitMap.put(512, p2);
        sn.orbitMap.put(513, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_154() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(514, t1);
        sn.orbitMap.put(515, p1);
        sn.orbitMap.put(516, t2);
        sn.orbitMap.put(517, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_134() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(518, t1);
        sn.orbitMap.put(519, p1);
        sn.orbitMap.put(520, t2);
        sn.orbitMap.put(521, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_135() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(522, t1);
        sn.orbitMap.put(523, p1);
        sn.orbitMap.put(524, t2);
        sn.orbitMap.put(525, p2);
        sn.orbitMap.put(526, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_137() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(527, t1);
        sn.orbitMap.put(528, p1);
        sn.orbitMap.put(529, t2);
        sn.orbitMap.put(530, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_138() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a3 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(531, t1);
        sn.orbitMap.put(532, p1);
        sn.orbitMap.put(533, t2);
        sn.orbitMap.put(534, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_139() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(535, t1);
        sn.orbitMap.put(536, p1);
        sn.orbitMap.put(537, t2);
        sn.orbitMap.put(538, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_140() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(539, t1);
        sn.orbitMap.put(540, p1);
        sn.orbitMap.put(541, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_136() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(542, t1);
        sn.orbitMap.put(543, p1);
        sn.orbitMap.put(544, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_141() {
        Transition t1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(545, t1);
        sn.orbitMap.put(546, p1);
        sn.orbitMap.put(547, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    // 20 p

    private static SubnetCalculator.SubNet graphlet_142() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(548, t1);
        sn.orbitMap.put(549, p1);
        sn.orbitMap.put(550, t2);
        sn.orbitMap.put(551, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_143() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(552, t1);
        sn.orbitMap.put(553, p1);
        sn.orbitMap.put(554, t2);
        sn.orbitMap.put(555, p2);
        sn.orbitMap.put(556, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_144() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(557, t1);
        sn.orbitMap.put(558, p1);
        sn.orbitMap.put(559, t2);
        sn.orbitMap.put(560, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_145() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(561, t1);
        sn.orbitMap.put(562, p1);
        sn.orbitMap.put(563, t2);
        sn.orbitMap.put(564, p2);
        sn.orbitMap.put(565, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_155() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(566, t1);
        sn.orbitMap.put(567, p1);
        sn.orbitMap.put(568, t2);
        sn.orbitMap.put(569, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    //+


    private static SubnetCalculator.SubNet graphlet_146() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(570, t1);
        sn.orbitMap.put(571, p1);
        sn.orbitMap.put(572, t2);
        sn.orbitMap.put(573, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_147() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(574, t1);
        sn.orbitMap.put(575, p1);
        sn.orbitMap.put(576, t2);
        sn.orbitMap.put(577, p2);
        sn.orbitMap.put(578, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_149() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(579, t1);
        sn.orbitMap.put(580, p1);
        sn.orbitMap.put(581, t2);
        sn.orbitMap.put(582, p3);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_150() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a3 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(583, t1);
        sn.orbitMap.put(584, p1);
        sn.orbitMap.put(585, t2);
        sn.orbitMap.put(586, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_151() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(587, t1);
        sn.orbitMap.put(588, p1);
        sn.orbitMap.put(589, t2);
        sn.orbitMap.put(590, p2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_148() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(591, t1);
        sn.orbitMap.put(592, p1);
        sn.orbitMap.put(593, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_152() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(594, t1);
        sn.orbitMap.put(595, p1);
        sn.orbitMap.put(596, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_153() {
        Place t1 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextId(), sheetID, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(597, t1);
        sn.orbitMap.put(598, p1);
        sn.orbitMap.put(599, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        sn.setSubNetID(totalGrahletID);
        totalGrahletID++;
        return sn;
    }


    private static void loadGraphlets() {

    }


    public static class Struct {
        HashMap<Node, Node> mapa;
        HashMap<Arc, Arc> mapaArcow;
        int graphletID = -1;

        public Struct(HashMap<Node, Node> m, HashMap<Arc, Arc> ma) {
            this.mapa = m;
            this.mapaArcow = ma;
        }

        public HashMap<Arc, Arc> getArcMap() {
            return mapaArcow;
        }

        public HashMap<Node, Node> getNodeMap() {
            return mapa;
        }

        public void setGraphletID(int id) {
            this.graphletID = id;
        }

        public int getGraphletID() {
            return this.graphletID;
        }
    }
}
