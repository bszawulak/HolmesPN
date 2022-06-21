package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.workspace.Workspace;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));

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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(pl,tl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        SubnetCalculator.compileTestElements(pl,tl,nl,tc.getInvariants(true),tc.getInvariants(false));
    }

    void ceateTestNetSMCOne(){
        ArrayList<Place> tl = new ArrayList<>();
        Place t1 = new Place(0,0,new Point(1,1));
        t1.setTokensNumber(1);
        tl.add(t1);
        Place t2 = new Place(1,0,new Point(1,2));
        t2.setTokensNumber(1);
        tl.add(t2);
        Place t3 = new Place(2,0,new Point(1,3));
        tl.add(t3);

        Place t4 = new Place(3,0,new Point(1,4));
        tl.add(t4);
        Place t5 = new Place(4,0,new Point(1,5));
        tl.add(t5);
        Place t6 = new Place(5,0,new Point(1,6));
        t6.setTokensNumber(1);
        tl.add(t6);

        ArrayList<Transition> pl = new ArrayList<>();
        Transition p1 = new Transition(99,0,new Point(1,1));
        pl.add(p1);
        Transition p2 = new Transition(98,0,new Point(1,1));
        pl.add(p2);
        Transition p3 = new Transition(97,0,new Point(1,1));
        pl.add(p3);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(t2);
        nl.add(t1);
        nl.add(t3);
        nl.add(t4);
        nl.add(t5);
        nl.add(t6);

        Arc a1 = new Arc(50,p2.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t1.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(50,p1.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(51,t4.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(55,p2.getElementLocations().get(0),t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(56,t5.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(57,p3.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(58,t2.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(59,p3.getElementLocations().get(0),t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(60,t6.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(61,p1.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(62,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


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
        tc = new InvariantsCalculator(tl,pl,al,false);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(pl,tl,nl,tc.getInvariants(true),tc.getInvariants(false));
    }

    void ceateTestNetSMCTwo(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);
        Transition t4 = new Transition(3,0,new Point(1,1));
        tl.add(t4);
        Transition t5 = new Transition(4,0,new Point(1,2));
        tl.add(t5);
        Transition t6 = new Transition(5,0,new Point(1,3));
        tl.add(t6);
        Transition t7 = new Transition(6,0,new Point(1,1));
        tl.add(t7);
        Transition t8 = new Transition(7,0,new Point(1,2));
        tl.add(t8);
        Transition t9 = new Transition(8,0,new Point(1,3));
        tl.add(t9);
        Transition t10 = new Transition(9,0,new Point(1,1));
        tl.add(t10);
        Transition t11 = new Transition(10,0,new Point(1,1));
        tl.add(t11);
        Transition t12 = new Transition(11,0,new Point(1,2));
        tl.add(t12);
        Transition t13 = new Transition(12,0,new Point(1,3));
        tl.add(t13);
        Transition t14 = new Transition(13,0,new Point(1,2));
        tl.add(t14);
        Transition t15 = new Transition(14,0,new Point(1,3));
        tl.add(t15);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        p1.setTokensNumber(1);
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);

        Place p5 = new Place(79,0,new Point(1,1));
        pl.add(p5);
        Place p6 = new Place(78,0,new Point(1,1));
        pl.add(p6);
        Place p7 = new Place(77,0,new Point(1,1));
        pl.add(p7);
        Place p8 = new Place(76,0,new Point(1,1));
        pl.add(p8);

        Place p9 = new Place(69,0,new Point(1,1));
        pl.add(p9);
        Place p10 = new Place(68,0,new Point(1,1));
        pl.add(p10);
        Place p11 = new Place(67,0,new Point(1,1));
        pl.add(p11);
        Place p12 = new Place(66,0,new Point(1,1));
        pl.add(p12);

        Place p13 = new Place(59,0,new Point(1,1));
        pl.add(p13);
        Place p14 = new Place(58,0,new Point(1,1));
        pl.add(p14);
        Place p15 = new Place(57,0,new Point(1,1));
        pl.add(p15);
        Place p16 = new Place(56,0,new Point(1,1));
        pl.add(p16);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(p5);
        nl.add(p6);
        nl.add(p7);
        nl.add(p8);
        nl.add(p9);
        nl.add(p10);
        nl.add(p11);
        nl.add(p12);
        nl.add(p13);
        nl.add(p14);
        nl.add(p15);
        nl.add(p16);

        nl.add(t1);
        nl.add(t2);
        nl.add(t3);
        nl.add(t4);
        nl.add(t5);
        nl.add(t6);
        nl.add(t7);
        nl.add(t8);
        nl.add(t9);
        nl.add(t10);
        nl.add(t11);
        nl.add(t12);
        nl.add(t13);
        nl.add(t14);
        nl.add(t15);

        Arc a29 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a30 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a31 = new Arc(52,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a32 = new Arc(50,t2.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a33 = new Arc(51,p4.getElementLocations().get(0),t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a34 = new Arc(52,t6.getElementLocations().get(0),p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a1 = new Arc(50,p5.getElementLocations().get(0),t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t7.getElementLocations().get(0),p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p6.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(10,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(11,p3.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(12,t4.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(21,p3.getElementLocations().get(0),t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(22,t5.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(32,t2.getElementLocations().get(0),p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(32,p7.getElementLocations().get(0),t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(30,t8.getElementLocations().get(0),p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(41,p8.getElementLocations().get(0),t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a13 = new Arc(42,t9.getElementLocations().get(0),p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a14 = new Arc(71,p9.getElementLocations().get(0),t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a15 = new Arc(72,t10.getElementLocations().get(0),p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a16 = new Arc(72,p10.getElementLocations().get(0),t11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a17 = new Arc(80,t11.getElementLocations().get(0),p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a18 = new Arc(81,p11.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a19 = new Arc(82,t2.getElementLocations().get(0),p12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a20 = new Arc(92,p12.getElementLocations().get(0),t12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a21 = new Arc(90,t12.getElementLocations().get(0),p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(91,p13.getElementLocations().get(0),t13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a23 = new Arc(112,t13.getElementLocations().get(0),p14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a24 = new Arc(151,p14.getElementLocations().get(0),t14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a25 = new Arc(152,t14.getElementLocations().get(0),p15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a26 = new Arc(252,p15.getElementLocations().get(0),t15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a27 = new Arc(250,t15.getElementLocations().get(0),p16.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a28 = new Arc(251,p16.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);



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
        al.add(a13);
        al.add(a14);
        al.add(a15);
        al.add(a16);
        al.add(a17);
        al.add(a18);
        al.add(a19);
        al.add(a20);
        al.add(a21);
        al.add(a22);
        al.add(a23);
        al.add(a24);
        al.add(a25);
        al.add(a26);
        al.add(a27);
        al.add(a28);
        al.add(a29);
        al.add(a30);
        al.add(a31);
        al.add(a32);
        al.add(a33);
        al.add(a34);

        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,false);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
    }

    void ceateTestNetSMCThree(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);
        Transition t4 = new Transition(3,0,new Point(1,1));
        tl.add(t4);
        Transition t5 = new Transition(4,0,new Point(1,2));
        tl.add(t5);
        Transition t6 = new Transition(5,0,new Point(1,3));
        tl.add(t6);
        Transition t7 = new Transition(6,0,new Point(1,1));
        tl.add(t7);
        Transition t8 = new Transition(7,0,new Point(1,2));
        tl.add(t8);
        Transition t9 = new Transition(8,0,new Point(1,3));
        tl.add(t9);
        Transition t10 = new Transition(9,0,new Point(1,1));
        tl.add(t10);
        Transition t11 = new Transition(10,0,new Point(1,1));
        tl.add(t11);
        Transition t12 = new Transition(11,0,new Point(1,2));
        tl.add(t12);
        Transition t13 = new Transition(12,0,new Point(1,3));
        tl.add(t13);
        Transition t14 = new Transition(13,0,new Point(1,2));
        tl.add(t14);
        Transition t15 = new Transition(14,0,new Point(1,3));
        tl.add(t15);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);

        Place p5 = new Place(79,0,new Point(1,1));
        pl.add(p5);
        Place p6 = new Place(78,0,new Point(1,1));
        pl.add(p6);
        Place p7 = new Place(77,0,new Point(1,1));
        p7.setTokensNumber(1);
        pl.add(p7);
        Place p8 = new Place(76,0,new Point(1,1));
        pl.add(p8);

        Place p9 = new Place(69,0,new Point(1,1));
        pl.add(p9);
        Place p10 = new Place(68,0,new Point(1,1));
        pl.add(p10);
        Place p11 = new Place(67,0,new Point(1,1));
        pl.add(p11);
        Place p12 = new Place(66,0,new Point(1,1));
        pl.add(p12);

        Place p13 = new Place(59,0,new Point(1,1));
        pl.add(p13);
        Place p14 = new Place(58,0,new Point(1,1));
        pl.add(p14);
        Place p15 = new Place(57,0,new Point(1,1));
        pl.add(p15);
        Place p16 = new Place(56,0,new Point(1,1));
        pl.add(p16);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(p5);
        nl.add(p6);
        nl.add(p7);
        nl.add(p8);
        nl.add(p9);
        nl.add(p10);
        nl.add(p11);
        nl.add(p12);
        nl.add(p13);
        nl.add(p14);
        nl.add(p15);
        nl.add(p16);

        nl.add(t1);
        nl.add(t2);
        nl.add(t3);
        nl.add(t4);
        nl.add(t5);
        nl.add(t6);
        nl.add(t7);
        nl.add(t8);
        nl.add(t9);
        nl.add(t10);
        nl.add(t11);
        nl.add(t12);
        nl.add(t13);
        nl.add(t14);
        nl.add(t15);

        Arc a29 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a30 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a31 = new Arc(52,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a32 = new Arc(50,t2.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a33 = new Arc(51,p4.getElementLocations().get(0),t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a34 = new Arc(52,t6.getElementLocations().get(0),p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a1 = new Arc(50,p5.getElementLocations().get(0),t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t7.getElementLocations().get(0),p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p6.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(10,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(11,p3.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(12,t4.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(21,p3.getElementLocations().get(0),t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(22,t5.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(32,t2.getElementLocations().get(0),p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(32,p7.getElementLocations().get(0),t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(30,t8.getElementLocations().get(0),p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(41,p8.getElementLocations().get(0),t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a13 = new Arc(42,t9.getElementLocations().get(0),p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a14 = new Arc(71,p9.getElementLocations().get(0),t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a15 = new Arc(72,t10.getElementLocations().get(0),p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a16 = new Arc(72,p10.getElementLocations().get(0),t11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a17 = new Arc(80,t11.getElementLocations().get(0),p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a18 = new Arc(81,p11.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a19 = new Arc(82,t2.getElementLocations().get(0),p12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a20 = new Arc(92,p12.getElementLocations().get(0),t12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a21 = new Arc(90,t12.getElementLocations().get(0),p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(91,p13.getElementLocations().get(0),t13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a23 = new Arc(112,t13.getElementLocations().get(0),p14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a24 = new Arc(151,p14.getElementLocations().get(0),t14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a25 = new Arc(152,t14.getElementLocations().get(0),p15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a26 = new Arc(252,p15.getElementLocations().get(0),t15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a27 = new Arc(250,t15.getElementLocations().get(0),p16.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a28 = new Arc(251,p16.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);



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
        al.add(a13);
        al.add(a14);
        al.add(a15);
        al.add(a16);
        al.add(a17);
        al.add(a18);
        al.add(a19);
        al.add(a20);
        al.add(a21);
        al.add(a22);
        al.add(a23);
        al.add(a24);
        al.add(a25);
        al.add(a26);
        al.add(a27);
        al.add(a28);
        al.add(a29);
        al.add(a30);
        al.add(a31);
        al.add(a32);
        al.add(a33);
        al.add(a34);

        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,false);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
    }

    void ceateTestNetSMCFour(){
        ArrayList<Transition> tl = new ArrayList<>();
        Transition t1 = new Transition(0,0,new Point(1,1));
        tl.add(t1);
        Transition t2 = new Transition(1,0,new Point(1,2));
        tl.add(t2);
        Transition t3 = new Transition(2,0,new Point(1,3));
        tl.add(t3);
        Transition t4 = new Transition(3,0,new Point(1,1));
        tl.add(t4);
        Transition t5 = new Transition(4,0,new Point(1,2));
        tl.add(t5);
        Transition t6 = new Transition(5,0,new Point(1,3));
        tl.add(t6);
        Transition t7 = new Transition(6,0,new Point(1,1));
        tl.add(t7);
        Transition t8 = new Transition(7,0,new Point(1,2));
        tl.add(t8);
        Transition t9 = new Transition(8,0,new Point(1,3));
        tl.add(t9);
        Transition t10 = new Transition(9,0,new Point(1,1));
        tl.add(t10);
        Transition t11 = new Transition(10,0,new Point(1,1));
        tl.add(t11);
        Transition t12 = new Transition(11,0,new Point(1,2));
        tl.add(t12);
        Transition t13 = new Transition(12,0,new Point(1,3));
        tl.add(t13);
        Transition t14 = new Transition(13,0,new Point(1,2));
        tl.add(t14);
        Transition t15 = new Transition(14,0,new Point(1,3));
        tl.add(t15);

        ArrayList<Place> pl = new ArrayList<>();
        Place p1 = new Place(99,0,new Point(1,1));
        pl.add(p1);
        Place p2 = new Place(98,0,new Point(1,1));
        pl.add(p2);
        Place p3 = new Place(97,0,new Point(1,1));
        pl.add(p3);
        Place p4 = new Place(96,0,new Point(1,1));
        pl.add(p4);

        Place p5 = new Place(79,0,new Point(1,1));
        pl.add(p5);
        Place p6 = new Place(78,0,new Point(1,1));
        pl.add(p6);
        Place p7 = new Place(77,0,new Point(1,1));
        pl.add(p7);
        Place p8 = new Place(76,0,new Point(1,1));
        pl.add(p8);

        Place p9 = new Place(69,0,new Point(1,1));
        pl.add(p9);
        Place p10 = new Place(68,0,new Point(1,1));
        pl.add(p10);
        Place p11 = new Place(67,0,new Point(1,1));
        pl.add(p11);
        Place p12 = new Place(66,0,new Point(1,1));
        pl.add(p12);

        Place p13 = new Place(59,0,new Point(1,1));
        pl.add(p13);
        Place p14 = new Place(58,0,new Point(1,1));
        pl.add(p14);
        Place p15 = new Place(57,0,new Point(1,1));
        pl.add(p15);
        Place p16 = new Place(56,0,new Point(1,1));
        pl.add(p16);


        ArrayList<Node> nl = new ArrayList<>();
        nl.add(p1);
        nl.add(p2);
        nl.add(p3);
        nl.add(p4);
        nl.add(p5);
        nl.add(p6);
        nl.add(p7);
        nl.add(p8);
        nl.add(p9);
        nl.add(p10);
        nl.add(p11);
        nl.add(p12);
        nl.add(p13);
        nl.add(p14);
        nl.add(p15);
        nl.add(p16);

        nl.add(t1);
        nl.add(t2);
        nl.add(t3);
        nl.add(t4);
        nl.add(t5);
        nl.add(t6);
        nl.add(t7);
        nl.add(t8);
        nl.add(t9);
        nl.add(t10);
        nl.add(t11);
        nl.add(t12);
        nl.add(t13);
        nl.add(t14);
        nl.add(t15);

        Arc a29 = new Arc(50,p1.getElementLocations().get(0),t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a30 = new Arc(51,t1.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a31 = new Arc(52,p2.getElementLocations().get(0),t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a32 = new Arc(50,t2.getElementLocations().get(0),p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a33 = new Arc(51,p4.getElementLocations().get(0),t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a34 = new Arc(52,t6.getElementLocations().get(0),p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a1 = new Arc(50,p5.getElementLocations().get(0),t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(51,t7.getElementLocations().get(0),p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(52,p6.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(10,t3.getElementLocations().get(0),p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a5 = new Arc(11,p3.getElementLocations().get(0),t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(12,t4.getElementLocations().get(0),p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(21,p3.getElementLocations().get(0),t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(22,t5.getElementLocations().get(0),p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(32,t2.getElementLocations().get(0),p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(32,p7.getElementLocations().get(0),t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(30,t8.getElementLocations().get(0),p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(41,p8.getElementLocations().get(0),t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a13 = new Arc(42,t9.getElementLocations().get(0),p9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a14 = new Arc(71,p9.getElementLocations().get(0),t10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a15 = new Arc(72,t10.getElementLocations().get(0),p10.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a16 = new Arc(72,p10.getElementLocations().get(0),t11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a17 = new Arc(80,t11.getElementLocations().get(0),p11.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a18 = new Arc(81,p11.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a19 = new Arc(82,t2.getElementLocations().get(0),p12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a20 = new Arc(92,p12.getElementLocations().get(0),t12.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a21 = new Arc(90,t12.getElementLocations().get(0),p13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a22 = new Arc(91,p13.getElementLocations().get(0),t13.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a23 = new Arc(112,t13.getElementLocations().get(0),p14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a24 = new Arc(151,p14.getElementLocations().get(0),t14.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a25 = new Arc(152,t14.getElementLocations().get(0),p15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a26 = new Arc(252,p15.getElementLocations().get(0),t15.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a27 = new Arc(250,t15.getElementLocations().get(0),p16.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a28 = new Arc(251,p16.getElementLocations().get(0),t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);



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
        al.add(a13);
        al.add(a14);
        al.add(a15);
        al.add(a16);
        al.add(a17);
        al.add(a18);
        al.add(a19);
        al.add(a20);
        al.add(a21);
        al.add(a22);
        al.add(a23);
        al.add(a24);
        al.add(a25);
        al.add(a26);
        al.add(a27);
        al.add(a28);
        al.add(a29);
        al.add(a30);
        al.add(a31);
        al.add(a32);
        al.add(a33);
        al.add(a34);

        PetriNet pn = new PetriNet(nl,al);
        tc = new InvariantsCalculator(pl,tl,al,false);
        tc.generateInvariantsForTest(pn);
        SubnetCalculator.compileTestElements(tl,pl,nl,tc.getInvariants(true),tc.getInvariants(false));
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
        /*
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
        */

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
        assertEquals(2,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateTZ();
        assertEquals(2,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateTZ();
        assertEquals(4,SubnetCalculator.tzSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateTZ();
        assertEquals(4,SubnetCalculator.tzSubNets.size());
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
        SubnetCalculator.generateCycle(false);
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateCycle(false);
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateCycle(false);
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateCycle(false);
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateCycle(false);
        assertEquals(1,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateCycle(false);
        assertEquals(2,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateCycle(false);
        assertEquals(2,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateCycle(false);
        assertEquals(2,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateCycle(false);
        assertEquals(4,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateCycle(false);
        assertEquals(4,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateCycle(false);
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateCycle(false);
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateCycle(false);
        assertEquals(0,SubnetCalculator.cycleSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateCycle(false);
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
        assertEquals(3,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateNishi();
        assertEquals(3,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateNishi();
        assertEquals(3,SubnetCalculator.nishiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateNishi();
        assertEquals(3,SubnetCalculator.nishiSubNets.size());
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
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateOotsuki();
        assertEquals(2,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateOotsuki();
        assertEquals(2,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());//poprawka
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());//poprawka
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateOotsuki();
        assertEquals(1,SubnetCalculator.ootsukiSubNets.size());//poprwka
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
        assertEquals(3,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateOotsuki();
        assertEquals(3,SubnetCalculator.ootsukiSubNets.size());
        SubnetCalculator.cleanSubnets();
    }

    @Test
    void generateSMC() {
        ceateTestNetSMCOne();
        SubnetCalculator.generateSMC();
        assertEquals(4, SubnetCalculator.smcSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSMCTwo();
        SubnetCalculator.generateSMC();
        assertEquals(3, SubnetCalculator.smcSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSMCThree();
        SubnetCalculator.generateSMC();
        assertEquals(1, SubnetCalculator.smcSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSMCFour();
        SubnetCalculator.generateSMC();
        assertEquals(0, SubnetCalculator.smcSubNets.size());
        SubnetCalculator.cleanSubnets();

    }

    @Test
    void generateBV() {
        ceateTestNetOne();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(0, SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwo();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(0,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThree();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(0,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFour();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(0,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFive();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(0,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSix();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(0,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetSeven();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(1,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetEight();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(1,SubnetCalculator.bvSubNets.size());//poprawka
        SubnetCalculator.cleanSubnets();

        ceateTestNetNine();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(2,SubnetCalculator.bvSubNets.size());//poprawka
        SubnetCalculator.cleanSubnets();

        ceateTestNetTen();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(2,SubnetCalculator.bvSubNets.size());//poprwka
        SubnetCalculator.cleanSubnets();

        ceateTestNetEleven();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(1,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetTwelve();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(1,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetThirteen();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(1,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();

        ceateTestNetFourteen();
        SubnetCalculator.generateBranchesVerticles();
        assertEquals(1,SubnetCalculator.bvSubNets.size());
        SubnetCalculator.cleanSubnets();
    }
}