package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GraphletsCalculator {

    public static HashMap<Integer, Node> globalOrbitMap = new HashMap<Integer, Node>();
    public static ArrayList<SubnetCalculator.SubNet> graphetsList = new ArrayList<>();

    public static void GraphletsCalculator() {
        generateGraphlets();
    }

    private static void generateGraphlets() {
        graphetsList.add(graphlet_1());
        graphetsList.add(graphlet_2());
        graphetsList.add(graphlet_3());
        graphetsList.add(graphlet_4());
        graphetsList.add(graphlet_5());
        graphetsList.add(graphlet_6());
        graphetsList.add(graphlet_7());
        graphetsList.add(graphlet_8());
    }

    public static void calcGraphlets(){
        for (Node n:
         GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes()
        ) {

        }
    }

    private static SubnetCalculator.SubNet graphlet_1() {
        Transition t1 = new Transition(1, 99, new Point(0, 0));
        Place p1 = new Place(1, 99, new Point(0, 0));
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
        Transition t1 = new Transition(2, 99, new Point(0, 0));
        Place p1 = new Place(2, 99, new Point(0, 0));
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
        Transition t1 = new Transition(3, 99, new Point(0, 0));
        Transition t2 = new Transition(4, 99, new Point(0, 0));
        Place p1 = new Place(5, 99, new Point(0, 0));
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
        Transition t1 = new Transition(6, 99, new Point(0, 0));
        Transition t2 = new Transition(7, 99, new Point(0, 0));
        Place p1 = new Place(8, 99, new Point(0, 0));
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
        Transition t1 = new Transition(11, 99, new Point(0, 0));
        Transition t2 = new Transition(21, 99, new Point(0, 0));
        Place p1 = new Place(11, 99, new Point(0, 0));
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
        Transition t1 = new Transition(61, 99, new Point(0, 0));
        Place p1 = new Place(61, 99, new Point(0, 0));
        Place p2 = new Place(62, 99, new Point(0, 0));
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
        Transition t1 = new Transition(71, 99, new Point(0, 0));
        Place p1 = new Place(71, 99, new Point(0, 0));
        Place p2 = new Place(72, 99, new Point(0, 0));
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
        Transition t1 = new Transition(81, 99, new Point(0, 0));
        Place p1 = new Place(81, 99, new Point(0, 0));
        Place p2 = new Place(82, 99, new Point(0, 0));
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

    private static void loadGraphlets() {

    }


}
