package holmes.analyse;

import holmes.analyse.comparison.SubnetComparator;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubnetComparatorFunModATest {

    @Test
    void compare0_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet0(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet1(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet2(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet3(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet4(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare0_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet0(), creatSubnet5(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet0(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet1(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet2(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet3(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet4(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare1_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet1(), creatSubnet5(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_00() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet0(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_01() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet1(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(1, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_02() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet2(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(5, gcs.gcsValue);
        assertEquals(5, gcs.psel.get(0).partialNodes.size());
        assertEquals(4, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet3(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(2, gcs.gcsValue);
        assertEquals(2, gcs.psel.get(0).partialNodes.size());
        assertEquals(0, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet4(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(3, gcs.gcsValue);
        assertEquals(3, gcs.psel.get(0).partialNodes.size());
        assertEquals(2, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare2_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet2(), creatSubnet5(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(4, gcs.gcsValue);
        assertEquals(4, gcs.psel.get(0).partialNodes.size());
        assertEquals(3, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare3_03() {
        SubnetComparator sc = new SubnetComparator(creatSubnet3(), creatSubnet3(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(5, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare4_04() {
        SubnetComparator sc = new SubnetComparator(creatSubnet4(), creatSubnet4(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    @Test
    void compare5_05() {
        SubnetComparator sc = new SubnetComparator(creatSubnet5(), creatSubnet5(), false, false, false);
        GreatCommonSubnet gcs = sc.compareFunctionalTest();
        //saveSubnet(gcs);
        assertEquals(6, gcs.gcsValue);
        assertEquals(6, gcs.psel.get(0).partialNodes.size());
        assertEquals(6, gcs.psel.get(0).partialArcs.size());
    }

    SubnetCalculator.SubNet creatSubnet0() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p2 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);
        pl.add(p2);

        Arc a1 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.ZAJCEV, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet1() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p2 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p3 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);
        pl.add(p2);
        pl.add(p3);

        Arc a1 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(IdGenerator.getNextId(), p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        al.add(a3);
        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.ZAJCEV, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet2() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p2 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p3 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);
        pl.add(p2);
        pl.add(p3);

        Arc a1 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(IdGenerator.getNextId(), p3.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t2.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.ZAJCEV, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet3() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p2 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p3 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p4 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);
        pl.add(p2);
        pl.add(p3);
        pl.add(p4);

        Arc a1 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), t2.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.ZAJCEV, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet4() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p2 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p3 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p4 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);
        pl.add(p2);
        pl.add(p3);
        pl.add(p4);

        Arc a1 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), t2.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.ZAJCEV, tl, null, null, null, null);
    }

    SubnetCalculator.SubNet creatSubnet5() {
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 0, new Point(1, 1));
        tl.add(t1);
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p2 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p3 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        Place p4 = new Place(IdGenerator.getNextId(), 0, new Point(1, 1));
        pl.add(p1);
        pl.add(p2);
        pl.add(p3);
        pl.add(p4);

        Arc a1 = new Arc(IdGenerator.getNextId(), p1.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(IdGenerator.getNextId(), p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(IdGenerator.getNextId(), t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(IdGenerator.getNextId(), t2.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(IdGenerator.getNextId(), t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.ZAJCEV, tl, null, null, null, null);
    }

    public void saveSubnet(GreatCommonSubnet gcs) {
        ArrayList<ElementLocation> listOfElements = new ArrayList<>();

        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Arc a : pse.partialArcs
            ) {
                System.out.println("Arc : " + a.getStartNode().getType() + " " +a.getStartNode().getName() + " - > "  + a.getEndNode().getType() + " " +a.getEndNode().getName());
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
                if(!listOfElements.contains(a.getStartLocation()))
                {
                    listOfElements.add(a.getStartLocation());
                }

                if(!listOfElements.contains(a.getEndLocation()))
                {
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
        for (SubnetComparator.PartialSubnetElements pse: gcs.psel
        ) {
            io.exportSubnet(listOfElements,pse.partialArcs);
        }
        //io.exportSubnet(listOfElements,gcs.psel.get(0).partialArcs);
    }
}
