package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.workspace.Workspace;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class SubnetCalculatorTest {

    InvariantsCalculator tc;

    //paths
    void ceateTestNetOne(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t3);
        nl.add(t1);

        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,p2.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();

        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));

    }

    void ceateTestNetTwo(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(p3);
        nl.add(t1);

        Arc a1 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,t2.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetThree(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t1);

        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetFour(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t1);

        Arc a1 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }
    //cycles
    void ceateTestNetFive(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t1);

        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,p2.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetSix(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);
        Transition t4 = new Transition(3,0,new Point(1,4));
        tl.add(t4);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t1);
        nl.add(p3);
        nl.add(p4);
        nl.add(t3);
        nl.add(t4);


        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,p2.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(54,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(55,p3.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(56,t4.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(57,p4.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        al.add(a7);
        al.add(a8);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetSeven(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t1);
        nl.add(p3);
        nl.add(p4);
        nl.add(t3);


        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,p2.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(54,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(55,p3.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(56,t2.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(57,p4.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        al.add(a7);
        al.add(a8);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetEight(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);
        Transition t4 = new Transition(3,0,new Point(1,4));
        tl.add(t4);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(t2);
        nl.add(t1);
        nl.add(p3);
        nl.add(t3);
        nl.add(t4);


        Arc a1 =new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 =new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 =new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 =new Arc(53,p2.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 =new Arc(54,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 =new Arc(55,p3.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 =new Arc(56,t4.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 =new Arc(57,p2.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        al.add(a7);
        al.add(a8);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetNine(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);
        Transition t4 = new Transition(3,0,new Point(1,4));
        tl.add(t4);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);
        Place p5 = new Place(95,0,new Point(1,1));
        pl.add(p5);
        Place p6 = new Place(94,0,new Point(1,1));
        pl.add(p6);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(p5);
        nl.add(p6);
        nl.add(t2);
        nl.add(t1);
        nl.add(t3);
        nl.add(t4);


        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p1.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,p2.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(54,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(55,p3.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a7 = new Arc(56,t3.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(57,p4.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(58,t2.getElementLocations().get(0),p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(59,p5.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(60,t4.getElementLocations().get(0),p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(61,p6.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        al.add(a7);
        al.add(a8);
        al.add(a9);
        al.add(a10);
        al.add(a11);
        al.add(a12);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetTen(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);
        Transition t4 = new Transition(3,0,new Point(1,4));
        tl.add(t4);
        Transition t5 = new Transition(4,0,new Point(1,3));
        tl.add(t5);
        Transition t6 = new Transition(5,0,new Point(1,4));
        tl.add(t6);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);

        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(t5);
        nl.add(t6);
        nl.add(t2);
        nl.add(t1);
        nl.add(t3);
        nl.add(t4);


        Arc a1 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(53,t2.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(54,p3.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(55,t3.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a7 = new Arc(56,p3.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(57,t4.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(58,p2.getElementLocations().get(0),t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(59,t5.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(60,p4.getElementLocations().get(0),t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(61,t6.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);
        al.add(a7);
        al.add(a8);
        al.add(a9);
        al.add(a10);
        al.add(a11);
        al.add(a12);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }
    //trees
    void ceateTestNetEleven(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(t2);
        nl.add(t1);
        nl.add(t3);

        Arc a1 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p3.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(50,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(51,p4.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(52,t3.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetTwelve(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(t2);
        nl.add(t1);
        nl.add(t3);

        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p2.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(50,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(51,t3.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(52,p2.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);


        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true));
    }

    void ceateTestNetThirteen(){
        ArrayList<Place> tl = new ArrayList<>();
        Place t1 = new Place(0,0,new Point(1,1));
        tl.add(t1);
        Place t2 = new Place(1,0,new Point(1,2));
        tl.add(t2);
        Place t3 = new Place(2,0,new Point(1,3));
        tl.add(t3);

        ArrayList<Transition> pl = new ArrayList<>();
        Transition p1 = new Transition(99,0,new Point(1,1));
        pl.add(p1);
        Transition p2 = new Transition(98,0,new Point(1,1));
        pl.add(p2);
        Transition p3 = new Transition(97,0,new Point(1,1));
        pl.add(p3);
        Transition p4 = new Transition(96,0,new Point(1,1));
        pl.add(p4);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(t2);
        nl.add(t1);
        nl.add(t3);

        Arc a1 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p3.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(50,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(51,p4.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(52,t3.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(tl,pl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(pl,tl,nl,tc.getInvariants(true));
    }

    void ceateTestNetFourteen(){
        ArrayList<Place> tl = new ArrayList<>();
        Place t1 = new Place(0,0,new Point(1,1));
        tl.add(t1);
        Place t2 = new Place(1,0,new Point(1,2));
        tl.add(t2);
        Place t3 = new Place(2,0,new Point(1,3));
        tl.add(t3);

        ArrayList<Transition> pl = new ArrayList<>();
        Transition p1 = new Transition(99,0,new Point(1,1));
        pl.add(p1);
        Transition p2 = new Transition(98,0,new Point(1,1));
        pl.add(p2);
        Transition p3 = new Transition(97,0,new Point(1,1));
        pl.add(p3);
        Transition p4 = new Transition(96,0,new Point(1,1));
        pl.add(p4);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(t2);
        nl.add(t1);
        nl.add(t3);

        Arc a1 = new Arc(50,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,p2.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,t2.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(50,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(51,t3.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(52,p2.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        ArrayList<Arc> al = new ArrayList<>();
        al.add(a1);
        al.add(a2);
        al.add(a3);
        al.add(a4);
        al.add(a5);
        al.add(a6);

        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(tl,pl,al,true);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(pl,tl,nl,tc.getInvariants(true));
    }

    @Test
    void generateFS() {
        ceateTestNetOne();
        SubnetCalculator.generateFS();
        assertEquals(3,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateFS();
        assertEquals(2,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateFS();
        assertEquals(2,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateFS();
        assertEquals(2,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateFS();
        assertEquals(2,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateFS();
        assertEquals(4,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateFS();
        assertEquals(3,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateFS();
        assertEquals(2,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateFS();
        assertEquals(4,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateFS();
        assertEquals(4,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateFS();
        assertEquals(1,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateFS();
        assertEquals(1,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateFS();
        assertEquals(4,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateFS();
        assertEquals(4,SubnetCalculator.functionalSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    @Test
    void generateSnets() {
        ceateTestNetOne();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateSnets();
        assertEquals(2,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateSnets();
        assertEquals(2,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateSnets();
        assertEquals(2,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateSnets();
        assertEquals(2,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateSnets();
        assertEquals(3,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateSnets();
        assertEquals(3,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateSnets();
        assertEquals(1,SubnetCalculator.snetSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    @Test
    void generateTnets() {
        ceateTestNetOne();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateTnets();
        assertEquals(2,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateTnets();
        assertEquals(2,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateTnets();
        assertEquals(4,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateTnets();
        assertEquals(3,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateTnets();
        assertEquals(3,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateTnets();
        assertEquals(1,SubnetCalculator.tnetSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    @Test
    void generateADT() {
        ceateTestNetOne();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateADT();
        assertEquals(2,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateADT();
        assertEquals(2,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateADT();
        assertEquals(4,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateADT();
        assertEquals(3,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateADT();
        assertEquals(3,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateADT();
        assertEquals(1,SubnetCalculator.adtSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    @Test
    void generateTZ() {
        ceateTestNetOne();
        SubnetCalculator.generateTZ();
        assertEquals(0, SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateTZ();
        assertEquals(1,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateTZ();
        assertEquals(2,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    /*
    @Test
    void generateTZ1() {
        ceateTestNetOne();
        SubnetCalculator.generateTZ();
        assertEquals(0, SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ2() {
        ceateTestNetTwo();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ3() {
        ceateTestNetThree();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ4() {
        ceateTestNetFour();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ5() {
        ceateTestNetFive();
        SubnetCalculator.generateTZ();
        assertEquals(1,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ6() {
        ceateTestNetSix();
        SubnetCalculator.generateTZ();
        assertEquals(2,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ7() {
        ceateTestNetSeven();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ8() {
        ceateTestNetEight();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ9() {
        ceateTestNetNine();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ10() {
        ceateTestNetTen();
        SubnetCalculator.generateTZ();
        assertEquals(3,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ11() {
        ceateTestNetEleven();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ12() {
        ceateTestNetTwelve();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ13() {
        ceateTestNetThirteen();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
    @Test
    void generateTZ14() {
        ceateTestNetFourteen();
        SubnetCalculator.generateTZ();
        assertEquals(0,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
*/
    @Test
    void generateCycle() {

        ceateTestNetOne();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();
/*
        ceateTestNetFive();
        SubnetCalculator.generateCycle();
        assertEquals(1,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateCycle();
        assertEquals(2,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();
*/
        ceateTestNetSeven();
        SubnetCalculator.generateCycle();
        assertEquals(2,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateCycle();
        assertEquals(2,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateCycle();
        assertEquals(4,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateCycle();
        assertEquals(4,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateCycle();
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

    }

    @Test
    void generateHou() {
        ceateTestNetOne();
        SubnetCalculator.generateHou();
        assertEquals(1,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateHou();
        assertEquals(1,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateHou();
        assertEquals(1,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateHou();
        assertEquals(1,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateHou();
        assertEquals(0,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateHou();
        assertEquals(0,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateHou();
        assertEquals(0,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateHou();
        assertEquals(0,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateHou();
        assertEquals(0,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateHou();
        assertEquals(0,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateHou();
        assertEquals(3,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateHou();
        assertEquals(3,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateHou();
        assertEquals(3,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateHou();
        assertEquals(3,SubnetCalculator.houSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    @Test
    void generateNishi() {
        ceateTestNetOne();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateNishi();
        assertEquals(0,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateNishi();
        assertEquals(0,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateNishi();
        assertEquals(0,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateNishi();
        assertEquals(0,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateNishi();
        assertEquals(0,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateNishi();
        assertEquals(0,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateNishi();
        assertEquals(1,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    @Test
    void generateOotsuki() {
        ceateTestNetOne();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateOotsuki();
        assertEquals(0,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateOotsuki();
        assertEquals(0,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateOotsuki();
        assertEquals(0,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateOotsuki();
        assertEquals(0,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateOotsuki();
        assertEquals(0,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateOotsuki();
        assertEquals(0,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
}