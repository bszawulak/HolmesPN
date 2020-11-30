package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GraphletsCalculator {

    public static HashMap<Integer, Node> globalOrbitMap = new HashMap<Integer, Node>();
    public static HashMap<Integer, Integer> orbitOraphMap = new HashMap<>();
    public static ArrayList<SubnetCalculator.SubNet> graphetsList = new ArrayList<>();

    public static void GraphletsCalculator() {
        generateGraphlets();
    }

    public static void generateGraphlets() {
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
    }

    private static int fromWhichGrahlet(Node n) {
        int index = -1;
        for (int i = 0; i < graphetsList.size(); i++) {
            if (graphetsList.get(i).getSubNode().contains(n))
                index = i;
        }
        return index;
    }

    public static void generateRelativeFrequencyOfGraphlets() {

        ArrayList<Node> siec = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
        for (int i = 0; i < siec.size(); i++) {
            for (int g = 0; g < graphetsList.size(); g++) {
                //for(int j = 0 ;j< graphetsList.get(g).getSubNode().size();j++) {


                int[] covered = new int[graphetsList.size()];
                Arrays.fill(covered, -1);
                covered[0] = i;
                checkGraphlet(siec.get(i), graphetsList.get(g), graphetsList.get(g).getSubNode().get(0), covered);
                //}
            }
        }
    }

    public static int[] vectorOrbit(Node n) {
        int[] ov = new int[globalOrbitMap.size()];
        int iterac = 0;

        String text = "";
        boolean test = true;
        if (test) {
            ov[5]=getOrbitValue(n, globalOrbitMap.get(5));
            //ov[23]=getOrbitValue(n, globalOrbitMap.get(23));
            //ov[24]=getOrbitValue(n, globalOrbitMap.get(24));
        } else {
            for (Map.Entry<Integer, Node> map : globalOrbitMap.entrySet()) {
                Node m = map.getValue();
                int result = getOrbitValue(n, m);
                ov[iterac] = result;
                System.out.println("Orbita " + iterac + " = " + result);
                text+= result +", ";
                iterac++;
            }
        }

        //JOptionPane.showMessageDialog(null,text);
        return ov;
    }

    private static int getOrbitValue(Node n, Node m) {
        if (n.getType() == m.getType()) {
            HashMap<Node, Node> mapa = new HashMap<>();
            HashMap<Arc, Arc> mapaArcow = new HashMap<>();
            mapa.put(n, m);
            ArrayList<ArrayList<Node>> foundGraphletsApp = new ArrayList<>();
            foundGraphletsApp = findGraphlet(n, m, mapa,mapaArcow, foundGraphletsApp);

            //get unical
            //foundGraphletsApp.

            for (ArrayList<Node> l: foundGraphletsApp
                 ) {
                for (Node k : l
                     ) {
                    System.out.print(k.getName());
                }
                System.out.println("");
            }
            System.out.println("next");
            return foundGraphletsApp.size();
        } else {
            return -1;
        }
    }

    private static ArrayList<ArrayList<Node>> findGraphlet(Node n, Node m, HashMap<Node, Node> mapa,HashMap<Arc, Arc> mapaArcow , ArrayList<ArrayList<Node>> resultNet) {
        //n Node z sieci
        //m Node z graphletu
        //jedną masz szukasz czy jest po drugiej stronie i ileif ()

        ArrayList<HashMap<Node,Node>> listaMapIn = new ArrayList<>();
        for (Arc arc2 : m.getInArcs()) {
            for (Arc arc1 : n.getInArcs()) {

                if(!mapaArcow.containsValue(arc2)) {

                    Node nod2 = arc2.getStartNode();
                    Node nod = arc1.getStartNode();
                    System.out.print("nod2 " + nod2.getName());
                    System.out.print("nod "+nod.getName());


                    if (!mapa.containsKey(nod)&& !mapa.containsValue(nod2)) {
                        //jeśli ma mniej niż graflet... koniec
                        if (nod.getInNodes().size() < nod2.getInNodes().size())// || nod.getOutNodes().size()<nod2.getOutNodes().size())
                        {
                            //break;
                            //return resultNet;
                        } else {
                            ArrayList<Node> neighbourNet = new ArrayList<>(mapa.keySet());
                            ArrayList<Node> neighbourGraph = new ArrayList<>(mapa.values());
                            neighbourNet.retainAll(nod.getOutInNodes());
                            neighbourGraph.retainAll(nod2.getOutInNodes());

                            //warunek poprawności (nie ma dodatkowych połączeń)
                            if (neighbourNet.size() <= neighbourGraph.size()) {
                                HashMap<Node, Node> newMapa = (HashMap<Node, Node>) mapa.clone();
                                HashMap<Arc, Arc> newMapaArc = (HashMap<Arc, Arc>) mapaArcow.clone();
                                newMapa.put(nod, nod2);
                                newMapaArc.put(arc1, arc2);
                                //Warunek końca

                                if (newMapa.size() == graphetsList.get(fromWhichGrahlet(m)).getSubNode().size()) {
                                    ArrayList<Node> newNet = new ArrayList<>(newMapa.keySet());

                                    boolean isRightGraphlet = checkIsRightGraphlet(newNet, graphetsList.get(fromWhichGrahlet(m)).getSubNode());
                                    if (!resultNet.contains(newNet) && isRightGraphlet) {
                                        resultNet.add(newNet);
                                    }
                                    //return resultNet;
                                } else {
                                    ArrayList<ArrayList<Node>> tmp = findGraphlet(nod, nod2, newMapa, newMapaArc, resultNet);
                                    if (tmp.size() == 0) {
                                        //chyba nie zawsze;
                                        mapa = newMapa;
                                    } else {
                                        for (ArrayList<Node> list : tmp) {
                                            if (!resultNet.contains(list))
                                                resultNet.add(list);
                                        }
                                    }
                                    //return resultNet;
                                }
                            } else {
                                //return resultNet;
                            }
                        }
                    }
                    else
                    {

                    }
                }
            }
            listaMapIn.add(mapa);
        }


        ArrayList<HashMap<Node,Node>> listaMapOut = new ArrayList<>();
        for (Arc arc2 : m.getOutArcs()) {
            for (Arc arc1 : n.getOutArcs()) {

                Node nod2 = arc2.getEndNode();
                Node nod = arc1.getEndNode();
                if (!mapa.containsKey(nod) && !mapa.containsValue(nod2)) {
                    if (nod.getOutNodes().size() < nod2.getOutNodes().size()) {
                        //return resultNet;
                    } else {
                        ArrayList<Node> neighbourNet = new ArrayList<>(mapa.keySet());
                        ArrayList<Node> neighbourGraph = new ArrayList<>(mapa.values());
                        neighbourNet.retainAll(nod.getOutInNodes());
                        neighbourGraph.retainAll(nod2.getOutInNodes());

                        //warunek poprawności (nie ma dodatkowych połączeń)
                        if (neighbourNet.size() <= neighbourGraph.size()) {
                            //HashMap<Node, Node> newMapa = (HashMap<Node, Node>) mapa.clone();
                            //newMapa.put(nod, nod2);

                            HashMap<Node, Node> newMapa = (HashMap<Node, Node>) mapa.clone();
                            HashMap<Arc, Arc> newMapaArc = (HashMap<Arc, Arc>) mapaArcow.clone();
                            newMapa.put(nod, nod2);
                            newMapaArc.put(arc1,arc2);

                            //Warunek końca
                            if (newMapa.size() == graphetsList.get(fromWhichGrahlet(m)).getSubNode().size()) {
                                ArrayList<Node> newNet = new ArrayList<>(newMapa.keySet());
                                boolean isRightGraphlet = checkIsRightGraphlet(newNet,graphetsList.get(fromWhichGrahlet(m)).getSubNode());
                                if(!resultNet.contains(newNet) && isRightGraphlet)
                                    resultNet.add(newNet);
                                //return resultNet;
                                //resultNet.add(newNet);
                                //return resultNet;
                            } else {
                                ArrayList<ArrayList<Node>> tmp = findGraphlet(nod, nod2, newMapa,newMapaArc, resultNet);
                                if (tmp.size() == 0) {
                                    //chyba nie zawsze;
                                    mapa = newMapa;
                                } else {
                                    for (ArrayList<Node> list: tmp) {
                                        if(!resultNet.contains(list))
                                            resultNet.add(list);
                                    }
                                    //resultNet.addAll(tmp);
                                }

                                //resultNet = findGraphlet(nod, nod2, newMapa, resultNet);
                                //return resultNet;
                            }
                        } else {
                            //return resultNet;
                        }
                    }
                }
            }
            listaMapOut.add(mapa);
        }

        resultNet = combineFromMaps(listaMapIn,listaMapOut,resultNet);

        return resultNet;
    }

    private static ArrayList<ArrayList<Node>> combineFromMaps(ArrayList<HashMap<Node, Node>> listaMapIn, ArrayList<HashMap<Node, Node>> listaMapOut, ArrayList<ArrayList<Node>> resultNet) {

        return resultNet;
    }

    private static boolean checkIsRightGraphlet(ArrayList<Node> newNet, ArrayList<Node> subNode) {
        int place = 0 ;
        int trans = 0 ;
        int placeGrap = 0 ;
        int transGrap = 0 ;
        for (Node n : newNet) {
            if(n.getType().equals(PetriNetElement.PetriNetElementType.PLACE))
                place++;
            else
                trans++;
        }
        for (Node n : subNode) {
            if(n.getType().equals(PetriNetElement.PetriNetElementType.PLACE))
                placeGrap++;
            else
                transGrap++;
        }
        if(place==placeGrap&&trans==transGrap)
            return true;

        return false;
    }

    private static void checkGraphlet(Node n, SubnetCalculator.SubNet graphlet, Node graphletNode, int[] coverage) {

    }

    private static void generateGraphlets(int transition_number, int place_number) {

        //wersja pamięciowo zachłanna... bo tak... bo powyżej 7 nie oczekuję

        ArrayList<Transition> listOfTransition = new ArrayList<>();
        for (int i = 0; i < transition_number; i++) {
            listOfTransition.add(new Transition(i, 0, new Point(40, 40 * (i + 1))));
        }

        ArrayList<Place> listOfPlace = new ArrayList<>();
        for (int i = 0; i < place_number; i++) {
            listOfPlace.add(new Place(i, 0, new Point(80, 40 * (i + 1))));
        }


        /**
         * bierzesz dowolną tanzycję i dowolne miejsce
         * wybierasz dla niego jedną z 3 opcji
         *  - incoming
         *  - outgoing
         *  - brak
         * i przechodzisz wgłąb sprawdzając dla kolejnej pracy 3 opcje
         * kończysz gdy ustawi wartości dla wszystkich - macierz incydencji?
         *
         **/

        ArrayList<Integer> arki = new ArrayList<>();
        arki.add(-1);
        arki.add(1);
        arki.add(0);

        ArrayList<int[][]> listOfMatrixes = new ArrayList<>();

        int[][] matrix = new int[listOfTransition.size()][listOfPlace.size()];

        for (int i = 0; i < listOfTransition.size(); i++) {
            for (int j = 0; j < listOfPlace.size(); j++) {
                for (int k = 0; k < arki.size(); k++) {
                    matrix[i][j] = arki.get(k);
                }
            }
        }


        for (int t = 1; t <= transition_number; t++) {
            for (int p = 1; p <= place_number; p++) {
                generateGraphlet(t, p);
            }
        }
    }

    private static void generateGraphlet(int t, int p) {
        for (int i = 1; i <= t; i++) {

        }
    }

    public static void calcGraphlets() {
        for (Node n :
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes()
        ) {

        }
    }

    private static SubnetCalculator.SubNet graphlet_1() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(0, t1);
        sn.orbitMap.put(1, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_2() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(2, p1);
        sn.orbitMap.put(3, t1);
        globalOrbitMap.putAll(sn.orbitMap);

        return new SubnetCalculator.SubNet(al);
    }

    private static SubnetCalculator.SubNet graphlet_3() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_4() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(7, t1);
        sn.orbitMap.put(8, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_5() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(9, t1);
        sn.orbitMap.put(10, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_6() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_7() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(15, t1);
        sn.orbitMap.put(14, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_8() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(17, t1);
        sn.orbitMap.put(16, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }
///

    private static SubnetCalculator.SubNet graphlet_9() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_10() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_11() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_12() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }


///

    private static SubnetCalculator.SubNet graphlet_13() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_14() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_15() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_16() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }


    // 3

    private static SubnetCalculator.SubNet graphlet_17() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_18() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_19() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_20() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    // 3.1

    private static SubnetCalculator.SubNet graphlet_21() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_22() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_23() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_24() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(al);
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    // 5

    private static SubnetCalculator.SubNet graphlet_25() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_26() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_27() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_28() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_29() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_30() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_31() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //  9 t-t

    private static SubnetCalculator.SubNet graphlet_32() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_33() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_34() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_35() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_36() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_37() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_38() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_39() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_40() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_41() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_42() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_43() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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

        return sn;
    }

    // 9 p-p


    private static SubnetCalculator.SubNet graphlet_44() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_45() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_46() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_47() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_48() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_49() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_50() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_51() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_52() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_53() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_54() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_55() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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

        return sn;
    }

    // 10

    private static SubnetCalculator.SubNet graphlet_56() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_57() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_58() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_59() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_60() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_61() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //part

    private static SubnetCalculator.SubNet graphlet_62() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_63() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_64() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //part


    private static SubnetCalculator.SubNet graphlet_65() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_66() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_67() {
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //p

    private static SubnetCalculator.SubNet graphlet_68() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_69() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_70() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_71() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_72() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_73() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //part

    private static SubnetCalculator.SubNet graphlet_74() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_75() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_76() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //part

    private static SubnetCalculator.SubNet graphlet_77() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_78() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_79() {
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //11

    private static SubnetCalculator.SubNet graphlet_80() {
        Place p1 = new Place(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_81() {
        Place p1 = new Place(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_82() {
        Place p1 = new Place(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_83() {
        Place p1 = new Place(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_84() {
        Place p1 = new Place(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition t4 = new Transition(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_85() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_86() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_87() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_88() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_89() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p4 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //16

    private static SubnetCalculator.SubNet graphlet_90() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_91() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_92() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_93() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_94() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_95() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    // // //

    private static SubnetCalculator.SubNet graphlet_96() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_97() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_98() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_99() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_100() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_101() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_102() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_103() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_104() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_105() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_106() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_107() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_108() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_109() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //16 p variant

    private static SubnetCalculator.SubNet graphlet_110() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_111() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_112() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_113() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_114() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_115() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    // // //

    private static SubnetCalculator.SubNet graphlet_116() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_117() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_118() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_119() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_120() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_121() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_122() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_123() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_124() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_125() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //

    private static SubnetCalculator.SubNet graphlet_126() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_127() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }


    private static SubnetCalculator.SubNet graphlet_128() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_129() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    // 20 t

    private static SubnetCalculator.SubNet graphlet_130() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_131() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_132() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //+
    private static SubnetCalculator.SubNet graphlet_133() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_134() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_135() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_136() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_137() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_138() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_139() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_140() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_141() {
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    // 20 p

    private static SubnetCalculator.SubNet graphlet_142() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_143() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_144() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    //+
    private static SubnetCalculator.SubNet graphlet_145() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_146() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_147() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_148() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_149() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_150() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_151() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_152() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }

    private static SubnetCalculator.SubNet graphlet_153() {
        Place t1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place t2 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Transition p1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition p3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
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
        sn.orbitMap.put(4, t1);
        sn.orbitMap.put(5, p1);
        sn.orbitMap.put(6, t2);
        globalOrbitMap.putAll(sn.orbitMap);

        return sn;
    }


    private static void loadGraphlets() {

    }


}
