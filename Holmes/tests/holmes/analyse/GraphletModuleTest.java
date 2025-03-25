package holmes.analyse;

import holmes.analyse.comparison.SubnetComparator;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.*;
import java.util.ArrayList;



public class GraphletModuleTest {

    ArrayList<Node> structure = new ArrayList<>();

    void generateTestStructureNr1() {
        structure.clear();
        Transition t0 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p0 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t0.getElementLocations().get(0), p0.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p0.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t0.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p0.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        structure.add(t0);
        structure.add(t1);
        structure.add(t2);
        structure.add(p0);
        structure.add(p1);
    }

    void generateTestStructureNr2() {
        structure.clear();
        Transition t0 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t1 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Transition t3 = new Transition(IdGenerator.getNextTransitionId(), 99, new Point(0, 0));
        Place p0 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextPlaceId(), 99, new Point(0, 0));
        Arc a1 = new Arc(t0.getElementLocations().get(0), p0.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p0.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p1.getElementLocations().get(0), t0.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(t2.getElementLocations().get(0), p0.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p0.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        structure.add(t0);
        structure.add(t1);
        structure.add(t2);
        structure.add(t3);
        structure.add(p0);
        structure.add(p1);
    }


    void generateTestStructureNr3() {
        structure.clear();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(0, 0));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(0, 0));
        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(0, 0));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(0, 0));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(0, 0));
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
        structure.add(t1);
        structure.add(t2);
        structure.add(p1);
        structure.add(p2);
        structure.add(p3);
    }

    @Test
    void testGraphlet86Net3() {
        generateTestStructureNr3();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        //orbit 0
        int[] vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[312]);
    }

    //net 1

    @Test
    void testGraphlet1Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        //orbit 0
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[0]);

        //orbit 1
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(2, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[1]);
    }

    @Test
    void testGraphlet2Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        //orbit 0
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[2]);

        //orbit 1
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[3]);
    }

    @Test
    void testGraphlet3Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        //orbit 4
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[4]);

        //orbit 5
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(2, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[5]);

        //orbit 6
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(2, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[6]);
    }

    @Test
    void testGraphlet4Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 7;
        //orbit 7
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 8;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet5Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 9;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 10;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet6Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 11;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);

        orbit = 12;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 13;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet7Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 14;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 15;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet8Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 16;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 17;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet9Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 18;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 19;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet10Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 20;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 21;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet11Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 22;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 23;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 24;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet12Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 25;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 26;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 27;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet13Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 28;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 29;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet14Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 30;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 31;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet15Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 32;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 33;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 34;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet16Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 35;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 36;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 37;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet17Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 38;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 39;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 40;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 41;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet18Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 42;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 43;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 44;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 45;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet19Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 46;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 47;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 48;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 49;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet20Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 50;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 51;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 52;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 53;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet21Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 54;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 55;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 56;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 57;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet22Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 58;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 59;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 60;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 61;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }



    @Test
    void testGraphlet23Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 62;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 63;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 64;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 65;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet24Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 66;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 67;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 68;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 69;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet25Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 70;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 71;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);

        orbit = 72;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 73;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet26Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 74;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 75;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 76;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 77;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet27Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 78;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 79;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 80;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 81;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet28Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 82;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 83;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 84;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 85;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet29Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 86;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 87;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 88;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 89;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet30Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 90;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 91;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 92;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 93;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet31Net1() {
        generateTestStructureNr1();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 94;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 95;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 96;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 97;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    //net 2


    @Test
    void testGraphlet1Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        //orbit 0
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[0]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[0]);

        //orbit 1
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(2, vectorOrbit[1]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[1]);
    }

    @Test
    void testGraphlet2Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        //orbit 0
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(2, vectorOrbit[2]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[2]);

        //orbit 1
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[3]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[3]);
    }

    @Test
    void testGraphlet3Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        //orbit 4
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(2, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(2, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[4]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[4]);

        //orbit 5
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(4, vectorOrbit[5]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[5]);

        //orbit 6
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(2, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(2, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[6]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[6]);
    }

    @Test
    void testGraphlet4Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 7;
        //orbit 7
        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 8;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet5Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 9;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 10;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet6Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 11;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

        orbit = 12;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 13;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet7Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 14;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 15;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet8Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 16;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 17;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet9Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 18;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 19;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet10Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 20;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 21;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet11Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 22;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 23;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(2, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 24;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(2, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(2, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet12Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 25;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 26;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(2, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 27;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(2, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(2, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet13Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 28;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 29;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet14Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 30;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 31;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet15Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 32;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 33;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 34;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet16Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 35;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 36;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 37;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet17Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 38;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 39;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 40;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 41;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet18Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 42;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 43;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 44;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 45;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet19Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 46;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 47;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 48;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 49;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet20Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 50;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 51;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 52;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 53;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet21Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 54;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 55;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 56;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 57;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet22Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 58;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 59;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 60;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 61;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

    }



    @Test
    void testGraphlet23Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 62;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 63;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 64;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 65;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet24Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 66;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 67;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 68;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 69;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet25Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 70;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 71;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

        orbit = 72;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 73;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(1, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet26Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 74;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 75;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 76;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 77;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet27Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 78;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 79;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 80;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 81;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet28Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 82;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 83;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 84;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 85;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

    @Test
    void testGraphlet29Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 86;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 87;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 88;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 89;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet30Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 90;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 91;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 92;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 93;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }


    @Test
    void testGraphlet31Net2() {
        generateTestStructureNr2();
        GraphletsCalculator gc = new GraphletsCalculator();
        gc.generateGraphlets();

        int orbit = 94;

        int[] vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 95;
        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

        orbit = 96;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(-1, vectorOrbit[orbit]);

        orbit = 97;

        vectorOrbit = gc.vectorOrbit(structure.get(0), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(1), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(2), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(3), false);
        assertEquals(-1, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(4), false);
        assertEquals(0, vectorOrbit[orbit]);
        vectorOrbit = gc.vectorOrbit(structure.get(5), false);
        assertEquals(0, vectorOrbit[orbit]);

    }

}
