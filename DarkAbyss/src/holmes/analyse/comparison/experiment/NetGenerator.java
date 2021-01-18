package holmes.analyse.comparison.experiment;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.SubnetCalculator;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class NetGenerator {
    int id = 0;

    boolean conservative = false;

    int x_p = 10;
    int y_p = 10;
    int x_t = 70;
    int y_t = 10;

    IOprotocols io = new IOprotocols();
    String directory = "/home/Szavislav/Eksperyment/Wyniki";

    private void setNewDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(directory + path));
        } catch (IOException erio) {

        }
    }

    public NetGenerator(boolean non){
        int i=0;
        int j = 35;
        int p = 32;

        setNewDirectory("/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p);

        //String tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-BASE";



        //io.readPNT(tmpdir + ".pnt");//, sn.getSubPlaces(), sn.getSubTransitions(), sn.getSubArcs());
        GraphletsCalculator.generateGraphlets();
/*
        ArrayList<int[]> DGDV = new ArrayList<>();
        String tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-P3VARIANT";
        //io.readPNT(tmpdir + ".pnt");
        //io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
        DGDV = new ArrayList<>();

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs());
        //------ P6 VARIANT ------
        SubnetCalculator.SubNet nsn = addParallel3(cloneSubNet(sn));

        for (Node startNode : nsn.getSubNode()) {
            int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
            DGDV.add(vectorOrbit);
        }

        //DGDV
        writeDGDV(tmpdir + "-DGDV.txt", DGDV);

        //DGDDA
        writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);
*/
        //------ ALL VARIANT ------

        ArrayList<int[]> DGDV = new ArrayList<>();
        //String tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-P3VARIANT";

        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs());
        SubnetCalculator.SubNet nsn = addStar4(cloneSubNet(sn));
        nsn = addStar4L(cloneSubNet(nsn));
        nsn = addStar4Lk(cloneSubNet(nsn));
        nsn = addCycle6(cloneSubNet(nsn));
        nsn = addEight2(cloneSubNet(nsn));
        nsn = addParallel3(cloneSubNet(nsn));
        String tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-ALLVARIANT";
        io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
        DGDV = new ArrayList<>();

        for (Node startNode : nsn.getSubNode()) {
            int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
            DGDV.add(vectorOrbit);
        }

        //DGDV
        writeDGDV(tmpdir + "-DGDV.txt", DGDV);

        //DGDDA
        writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

    }

    public NetGenerator(int i_min, int i_max, int j_min, int j_max, int p_min, int p_max) {
        for (int i = i_min; i < i_max; i=i+5) {
            for (int j = j_min; j < j_max; j=j+5) {
                setNewDirectory("/i" + i + "j" + j);
                for (int p = p_min; p < p_max; p++) { // - próbka 100 sieci
                    SubnetCalculator.SubNet sn = generateNet(10 + i, 10 + j, (int) ((i + j + 20) * 1.3));
                    setNewDirectory("/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p);

                    String tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-BASE";

                    io.writePNT(tmpdir + ".pnt", sn.getSubPlaces(), sn.getSubTransitions(), sn.getSubArcs());
                    GraphletsCalculator.generateGraphlets();

                    ArrayList<int[]> DGDV = new ArrayList<>();

                    for (Node startNode : sn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //tu można by zacząć dodawać nowe rozszrzenia
                    //------ S4 VARIANT ------
                    SubnetCalculator.SubNet nsn = addStar4(cloneSubNet(sn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-S4VARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //cleanNetwork(sn);
                    //------ S4L VARIANT ------
                    nsn = addStar4L(cloneSubNet(sn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-K4LVARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //------ S4Lk VARIANT ------
                    nsn = addStar4Lk(cloneSubNet(sn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-K4LkVARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //------ E2 VARIANT ------
                    nsn = addEight2(cloneSubNet(sn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-E2VARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //------ C6 VARIANT ------
                    nsn = addCycle6(cloneSubNet(sn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-C6VARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //------ SS4 VARIANT ------
                    nsn = addStar4(cloneSubNet(sn));
                    nsn = addStar4(cloneSubNet(nsn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-SS4VARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //------ SSs4 VARIANT ------
                    nsn = addStar4(cloneSubNet(sn));
                    nsn = addStar4(cloneSubNet(nsn));
                    nsn = addStar4(cloneSubNet(nsn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-SSS4VARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);


                    //------ P6 VARIANT ------
                    nsn = addParallel3(cloneSubNet(sn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-P3VARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                    //------ ALL VARIANT ------

                    nsn = addStar4(cloneSubNet(sn));
                    nsn = addStar4L(cloneSubNet(nsn));
                    nsn = addStar4Lk(cloneSubNet(nsn));
                    nsn = addCycle6(cloneSubNet(nsn));
                    nsn = addEight2(cloneSubNet(nsn));
                    nsn = addParallel3(cloneSubNet(nsn));
                    tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-ALLVARIANT";
                    io.writePNT(tmpdir + ".pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : nsn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);

                }


            }
        }
    }

    private void cleanNetwork(SubnetCalculator.SubNet sn) {
        for (Node n : sn.getSubNode()) {
            ArrayList<Arc> tmp = new ArrayList<Arc>(n.getOutInArcs());
            for (int i = 0; i < tmp.size(); i++) {
                if (!sn.getSubArcs().contains(tmp.get(i))) {
                    n.getOutInArcs().remove(tmp.get(i));
                }
            }
        }
    }

    private SubnetCalculator.SubNet addStar4L(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));
        Transition t4 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 120));
        Transition t5 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 150));

        Transition t6 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 180));
        Transition t7 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 210));
        Transition t8 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 240));
        Transition t9 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 270));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));

        Place p5 = new Place(IdGenerator.getNextId(), 99, new Point(100, 150));
        Place p6 = new Place(IdGenerator.getNextId(), 99, new Point(100, 180));
        Place p7 = new Place(IdGenerator.getNextId(), 99, new Point(100, 210));
        Place p8 = new Place(IdGenerator.getNextId(), 99, new Point(100, 270));

        t1.setComment("4L");
        t2.setComment("4L");
        t3.setComment("4L");
        t4.setComment("4L");
        t5.setComment("4L");
        t6.setComment("4L");
        t7.setComment("4L");
        t8.setComment("4L");
        t9.setComment("4L");

        p1.setComment("4L");
        p2.setComment("4L");
        p3.setComment("4L");
        p4.setComment("4L");
        p5.setComment("4L");
        p6.setComment("4L");
        p7.setComment("4L");
        p8.setComment("4L");

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t1.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(t2.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(t3.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a13 = new Arc(t4.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a14 = new Arc(p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a15 = new Arc(t5.getElementLocations().get(0), p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a16 = new Arc(p8.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);
        listOfTransition.add(t4);
        listOfTransition.add(t5);
        ;
        listOfTransition.add(t6);
        listOfTransition.add(t7);
        listOfTransition.add(t8);
        listOfTransition.add(t9);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);
        listOfPlace.add(p5);
        listOfPlace.add(p6);
        listOfPlace.add(p7);
        listOfPlace.add(p8);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);
        listOfArc.add(a9);
        listOfArc.add(a10);
        listOfArc.add(a11);
        listOfArc.add(a12);
        listOfArc.add(a13);
        listOfArc.add(a14);
        listOfArc.add(a15);
        listOfArc.add(a16);

        int index = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    private int getBaseNetTtansitionIndex(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> list = sn.getSubTransitions().stream().filter(x -> x.getComment().equals("")).collect(Collectors.toCollection(ArrayList::new));
        Random r = new Random();
        int randomIndex = r.nextInt(list.size() - 1);
        return sn.getSubTransitions().indexOf(list.get(randomIndex));
    }

    public SubnetCalculator.SubNet cloneSubNet(SubnetCalculator.SubNet sn) {
        ArrayList<Place> newPlaceList = new ArrayList<>();
        ArrayList<Transition> newTransitionList = new ArrayList<>();
        ArrayList<Arc> newArcList = new ArrayList<>();

        for (int i = 0; i < sn.getSubPlaces().size(); i++) {
            newPlaceList.add(new Place(IdGenerator.getNextId(), 0, new Point(0, 0)));
        }

        for (int i = 0; i < sn.getSubTransitions().size(); i++) {
            newTransitionList.add(new Transition(IdGenerator.getNextId(), 0, new Point(0, 0)));
        }

        for (int i = 0; i < sn.getSubArcs().size(); i++) {
            if (sn.getSubArcs().get(i).getStartNode().getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                newArcList.add(new Arc(
                        newPlaceList.get(sn.getSubPlaces().indexOf(sn.getSubArcs().get(i).getStartNode())).getLastLocation(),
                        newTransitionList.get(sn.getSubTransitions().indexOf(sn.getSubArcs().get(i).getEndNode())).getLastLocation(),
                        Arc.TypeOfArc.NORMAL));
            } else {
                newArcList.add(new Arc(
                        newTransitionList.get(sn.getSubTransitions().indexOf(sn.getSubArcs().get(i).getStartNode())).getLastLocation(),
                        newPlaceList.get(sn.getSubPlaces().indexOf(sn.getSubArcs().get(i).getEndNode())).getLastLocation(),
                        Arc.TypeOfArc.NORMAL));
            }
        }

        return new SubnetCalculator.SubNet(newArcList);
    }


    private SubnetCalculator.SubNet addStar4Lk(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));
        Transition t4 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 120));
        Transition t5 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 150));

        Transition t6 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 180));
        Transition t7 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 210));
        Transition t8 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 240));
        Transition t9 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 270));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));

        Place p5 = new Place(IdGenerator.getNextId(), 99, new Point(100, 150));
        Place p6 = new Place(IdGenerator.getNextId(), 99, new Point(100, 180));
        Place p7 = new Place(IdGenerator.getNextId(), 99, new Point(100, 210));
        Place p8 = new Place(IdGenerator.getNextId(), 99, new Point(100, 270));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t1.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(t2.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(p5.getElementLocations().get(0), t6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a11 = new Arc(t6.getElementLocations().get(0), p6.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a12 = new Arc(p6.getElementLocations().get(0), t7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a13 = new Arc(t7.getElementLocations().get(0), p7.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a14 = new Arc(p7.getElementLocations().get(0), t8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a15 = new Arc(t8.getElementLocations().get(0), p8.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a16 = new Arc(p8.getElementLocations().get(0), t9.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        t1.setComment("4Lk");
        t2.setComment("4Lk");
        t3.setComment("4Lk");
        t4.setComment("4Lk");
        t5.setComment("4Lk");
        t6.setComment("4Lk");
        t7.setComment("4Lk");
        t8.setComment("4Lk");
        t9.setComment("4Lk");

        p1.setComment("4Lk");
        p2.setComment("4Lk");
        p3.setComment("4Lk");
        p4.setComment("4Lk");
        p5.setComment("4Lk");
        p6.setComment("4Lk");
        p7.setComment("4Lk");
        p8.setComment("4Lk");

        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);
        listOfTransition.add(t4);
        listOfTransition.add(t5);
        listOfTransition.add(t6);
        listOfTransition.add(t7);
        listOfTransition.add(t8);
        listOfTransition.add(t9);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);
        listOfPlace.add(p5);
        listOfPlace.add(p6);
        listOfPlace.add(p7);
        listOfPlace.add(p8);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);
        listOfArc.add(a9);
        listOfArc.add(a10);
        listOfArc.add(a11);
        listOfArc.add(a12);
        listOfArc.add(a13);
        listOfArc.add(a14);
        listOfArc.add(a15);
        listOfArc.add(a16);
        int index = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);

    }


    public SubnetCalculator.SubNet addStar4(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));
        Transition t4 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 120));
        Transition t5 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 150));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t1.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        t1.setComment("4");
        t2.setComment("4");
        t3.setComment("4");
        t4.setComment("4");
        t5.setComment("4");

        p1.setComment("4");
        p2.setComment("4");
        p3.setComment("4");
        p4.setComment("4");

        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);
        listOfTransition.add(t4);
        listOfTransition.add(t5);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);
        int index = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public SubnetCalculator.SubNet addStar4c2(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));
        Transition t4 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 120));
        Transition t5 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 150));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t1.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);
        listOfTransition.add(t4);
        listOfTransition.add(t5);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);

        int index = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));
        int index2 = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(sn.getSubTransitions().get(index2).getLastLocation(), p2.getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public SubnetCalculator.SubNet addEight2(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t3.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);


        t1.setComment("2E");
        t2.setComment("2E");
        t3.setComment("2E");

        p1.setComment("2E");
        p2.setComment("2E");
        p3.setComment("2E");
        p4.setComment("2E");

        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);

        int index = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public SubnetCalculator.SubNet addEight2c2(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t1.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t3.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(0).getLastLocation(), Arc.TypeOfArc.NORMAL));
        listOfArc.add(new Arc(sn.getSubTransitions().get(sn.getSubTransitions().size() - 1).getLastLocation(), p2.getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public SubnetCalculator.SubNet addCycle6(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));
        Transition t4 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 120));
        Transition t5 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 150));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));
        Place p5 = new Place(IdGenerator.getNextId(), 99, new Point(100, 150));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t3.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t4.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(p5.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        t1.setComment("6C");
        t2.setComment("6C");
        t3.setComment("6C");
        t4.setComment("6C");
        t5.setComment("6C");

        p1.setComment("6C");
        p2.setComment("6C");
        p3.setComment("6C");
        p4.setComment("6C");
        p5.setComment("6C");

        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);
        listOfTransition.add(t4);
        listOfTransition.add(t5);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);
        listOfPlace.add(p5);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);
        listOfArc.add(a9);
        listOfArc.add(a10);
        int index = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public SubnetCalculator.SubNet addParallel3(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t1.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t2.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        t1.setComment("3P");
        t2.setComment("3P");

        p1.setComment("3P");
        p2.setComment("3P");
        p3.setComment("3P");

        listOfTransition.add(t1);
        listOfTransition.add(t2);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);


        int index = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));
        int index2 = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(sn.getSubTransitions().get(index2).getLastLocation(), p2.getLastLocation(), Arc.TypeOfArc.NORMAL));

        //listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(0).getLastLocation(), Arc.TypeOfArc.NORMAL));
        //listOfArc.add(new Arc(p2.getLastLocation(), sn.getSubTransitions().get(sn.getSubTransitions().size()-1).getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public SubnetCalculator.SubNet addCycle6c2(SubnetCalculator.SubNet sn) {
        ArrayList<Transition> listOfTransition = new ArrayList<>();
        ArrayList<Place> listOfPlace = new ArrayList<>();
        ArrayList<Arc> listOfArc = new ArrayList<>();
        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 30));
        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 60));
        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 90));
        Transition t4 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 120));
        Transition t5 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 150));

        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 60));
        Place p3 = new Place(IdGenerator.getNextId(), 99, new Point(100, 90));
        Place p4 = new Place(IdGenerator.getNextId(), 99, new Point(100, 120));
        Place p5 = new Place(IdGenerator.getNextId(), 99, new Point(100, 150));

        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a2 = new Arc(p1.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a3 = new Arc(t2.getElementLocations().get(0), p2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a4 = new Arc(p2.getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a5 = new Arc(t3.getElementLocations().get(0), p3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a6 = new Arc(p3.getElementLocations().get(0), t4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a7 = new Arc(t4.getElementLocations().get(0), p4.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a8 = new Arc(p4.getElementLocations().get(0), t5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        Arc a9 = new Arc(t5.getElementLocations().get(0), p5.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        Arc a10 = new Arc(p5.getElementLocations().get(0), t1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);

        listOfTransition.add(t1);
        listOfTransition.add(t2);
        listOfTransition.add(t3);
        listOfTransition.add(t4);
        listOfTransition.add(t5);

        listOfPlace.add(p1);
        listOfPlace.add(p2);
        listOfPlace.add(p3);
        listOfPlace.add(p4);
        listOfPlace.add(p5);

        listOfArc.add(a1);
        listOfArc.add(a2);
        listOfArc.add(a3);
        listOfArc.add(a4);
        listOfArc.add(a5);
        listOfArc.add(a6);
        listOfArc.add(a7);
        listOfArc.add(a8);
        listOfArc.add(a9);
        listOfArc.add(a10);
        listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(0).getLastLocation(), Arc.TypeOfArc.NORMAL));
        listOfArc.add(new Arc(sn.getSubTransitions().get(sn.getSubTransitions().size() - 1).getLastLocation(), p2.getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public static void writeDGDDA(String directory, ArrayList<int[]> DGDV) {
        File file;
        file = new File(directory);


        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write("-d" + "\r\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int max = 0;
        for (int l = 0; l < DGDV.size(); l++) {
            int localMax = Arrays.stream(DGDV.get(l)).max().getAsInt();
            if (localMax > max)
                max = localMax;
        }

        int orbitNumber = DGDV.get(0).length;

        int[][] d = new int[orbitNumber][max + 1];

        for (int l = 0; l < DGDV.size(); l++) {
            for (int m = 0; m < DGDV.get(l).length; m++) {
                if (DGDV.get(l)[m] > 0) {
                    d[m][DGDV.get(l)[m]]++;
                }
            }
        }


        for (int orb = 0; orb < d.length; orb++) {
            try {
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(Arrays.toString(d[orb]) + "\r\n");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write("-s" + "\r\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[][] s = new double[d.length][d[0].length];

        for (int orb = 0; orb < d.length; orb++) {

            try {
                FileWriter fileWriter = new FileWriter(file, true);

                double[] ss = new double[d[orb].length];
                for (int k = 1; k < d[orb].length; k++) {
                    double dd = (double) d[orb][k];
                    double kd = (double) k;
                    ss[k] = dd / kd;

                    s[orb] = ss;
                }
                fileWriter.write(Arrays.toString(ss) + "\r\n");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write("-t" + "\r\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[] t = new double[d.length];

        for (int orb = 0; orb < d.length; orb++) {
            t[orb] = DoubleStream.of(s[orb]).sum();
        }
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(Arrays.toString(t) + "\r\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write("-n" + "\r\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double[][] n = new double[d.length][d[0].length];

        for (int orb = 0; orb < n.length; orb++) {
            for (int k = 0; k < n[orb].length; k++) {
                n[orb][k] = s[orb][k] / t[orb];
            }

            try {
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(Arrays.toString(n[orb]) + "\r\n");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeDGDV(String directory, ArrayList<int[]> DGDV) {

        //File file = new File(directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p +"DGDV.txt");

        File file = new File(directory);

        for (int l = 0; l < DGDV.size(); l++) {
            try {
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(Arrays.toString(DGDV.get(l)) + "\r\n");
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SubnetCalculator.SubNet generateNet(int place_number, int transition_number, int arc_number) {
        if (place_number + transition_number - 1 <= arc_number) {
            int place_index = 0;
            int transition_index = 0;
            int arc_index = 0;
            ArrayList<Place> listOfPlaces = new ArrayList<>();
            ArrayList<Transition> listOfTransitions = new ArrayList<>();
            ArrayList<Arc> listOfArcs = new ArrayList<>();

            boolean isNewPlace = false;
            boolean isNewTransition = false;
            boolean switcher = false;

            Place start_place = new Place(IdGenerator.getNextId(), 0, new Point(x_p, y_p));
            Transition start_transition = new Transition(IdGenerator.getNextId(), 0, new Point(x_t, y_t));
            Arc first_arc = new Arc(start_place.getLastLocation(), start_transition.getLastLocation(), Arc.TypeOfArc.NORMAL);
            y_p += 30;
            y_t += 30;

            listOfPlaces.add(start_place);
            listOfTransitions.add(start_transition);
            listOfArcs.add(first_arc);
            place_index++;
            transition_index++;

            Random r = new Random();

            while (place_index < place_number || transition_index < transition_number) {
                Place new_place = null;
                isNewPlace = false;
                isNewTransition = false;

                Transition new_transition = null;
                if (place_index < place_number) {// && switcher) {
                    new_place = new Place(IdGenerator.getNextId(), 0, new Point(x_p, y_p));
                    y_p += 30;
                    place_index++;
                    listOfPlaces.add(new_place);
                    isNewPlace = true;
                }

                if (transition_index < transition_number) {// && !switcher) {
                    new_transition = new Transition(IdGenerator.getNextId(), 0, new Point(x_t, y_t));
                    y_t += 30;
                    transition_index++;
                    listOfTransitions.add(new_transition);
                    isNewTransition = true;
                }


                if (new_transition == null) {
                    switcher = true;
                }

                if (new_place == null) {
                    switcher = false;
                }


                if (isNewPlace || isNewTransition) {
                    if (isNewPlace) {
                        //usuń ostatni
                        int index = r.nextInt(listOfTransitions.size());
                        if (new_transition != null && listOfTransitions.get(index).getID() == new_transition.getID()) {
                            index--;
                        }


                        if (r.nextBoolean()) {
                            Arc newArc = new Arc(new_place.getLastLocation(), listOfTransitions.get(index).getLastLocation(), Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            switcher = false;
                        } else {
                            Arc newArc = new Arc(listOfTransitions.get(index).getLastLocation(), new_place.getLastLocation(), Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            switcher = false;
                        }
                        arc_index++;

                    }
                    if (isNewTransition) {
                        int index = r.nextInt(listOfPlaces.size());
                        if (new_place != null && listOfPlaces.get(index).getID() == new_place.getID()) {
                            //listOfPlaces.get(index).getOutInArcs().get(0).setWeight(listOfPlaces.get(index).getOutInArcs().get(0).getWeight()+1);
                            index--;
                            //arc_index++;
                        }// else {

                        if (r.nextBoolean()) {
                            Arc newArc = new Arc(new_transition.getLastLocation(), listOfPlaces.get(index).getLastLocation(), Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            switcher = true;
                        } else {
                            Arc newArc = new Arc(listOfPlaces.get(index).getLastLocation(), new_transition.getLastLocation(), Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            switcher = true;
                        }
                        arc_index++;
                        //}
                    }
                }
            }

            //find read arcki

            for (Place p : listOfPlaces) {
                if (p.getInArcs().size() == 0) {
                    ArrayList<Transition> toChoose = new ArrayList<>(listOfTransitions);
                    toChoose.removeAll(p.getOutNodes());
                    int index = r.nextInt(toChoose.size());
                    Arc newArc = new Arc(toChoose.get(index).getLastLocation(), p.getLastLocation(), Arc.TypeOfArc.NORMAL);
                    listOfArcs.add(newArc);
                    arc_index++;
                } else if (p.getOutArcs().size() == 0) {
                    ArrayList<Transition> toChoose = new ArrayList<>(listOfTransitions);
                    toChoose.removeAll(p.getInNodes());
                    int index = r.nextInt(toChoose.size());
                    Arc newArc = new Arc(p.getLastLocation(), toChoose.get(index).getLastLocation(), Arc.TypeOfArc.NORMAL);
                    listOfArcs.add(newArc);
                    arc_index++;
                }
            }

            //add additional ARCs

/*
            if(listOfPlaces.stream().filter(x->x.getInArcs().size()==0 || x.getOutArcs().size()==0).count()>0)
            {
                ArrayList<Node> toModify = listOfPlaces.stream().filter(x->x.getType().equals(PetriNetElement.PetriNetElementType.PLACE) && (x.getInArcs().size()==0 || x.getInArcs().size()==0)).collect(Collectors.toCollection(ArrayList::new));
                for (int i = 0 ; i < toModify.size() ; i++)
                {
                    if(toModify.get(i).getInArcs().size()==0)
                    {
                        ArrayList<Node> nonSourceTransitions = listOfTransitions.stream().filter(x->x.getInArcs().size()==0).collect(Collectors.toCollection(ArrayList::new));
                        if(nonSourceTransitions.size()>0)
                        {
                            int index_p = listOfPlaces.indexOf(toModify.get(i));
                            Arc newArc = new Arc( nonSourceTransitions.get(0).getLastLocation(), listOfPlaces.get(index_p).getLastLocation(),Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            arc_index++;
                        }
                    }
                    else
                    {
                        ArrayList<Node> nonSinkTransitions = listOfTransitions.stream().filter(x->x.getOutArcs().size()==0).collect(Collectors.toCollection(ArrayList::new));
                        if(nonSinkTransitions.size()>0)
                        {
                            int index_p = listOfPlaces.indexOf(toModify.get(i));
                            Arc newArc = new Arc(  listOfPlaces.get(index_p).getLastLocation(),nonSinkTransitions.get(0).getLastLocation(),Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            arc_index++;
                        }
                    }
                }
            }
*/
            //SINK SOURCE TRANSITIONS

            boolean sinkTRansition = listOfPlaces.stream().anyMatch(x -> (x.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && (x.getOutArcs().size() == 0)));
            boolean sourceTRansition = listOfPlaces.stream().anyMatch(x -> (x.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && (x.getInArcs().size() == 0)));

            if (sinkTRansition && !sourceTRansition) {
                ArrayList<Node> listToModify = listOfPlaces.stream().filter(x -> (x.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && (x.getOutArcs().size() == 0))).collect(Collectors.toCollection(ArrayList::new));
                for (Node n : listToModify) {
                    ArrayList<Place> toChoose = new ArrayList<>(listOfPlaces);
                    toChoose.removeAll(n.getOutNodes());
                    int index = r.nextInt(toChoose.size());
                    Arc newArc = new Arc(n.getLastLocation(), toChoose.get(index).getLastLocation(), Arc.TypeOfArc.NORMAL);
                    listOfArcs.add(newArc);
                    arc_index++;
                }
            }

            if (!sinkTRansition && sourceTRansition) {
                ArrayList<Node> listToModify = listOfPlaces.stream().filter(x -> (x.getType().equals(PetriNetElement.PetriNetElementType.TRANSITION) && (x.getInArcs().size() == 0))).collect(Collectors.toCollection(ArrayList::new));
                for (Node n : listToModify) {
                    ArrayList<Place> toChoose = new ArrayList<>(listOfPlaces);
                    toChoose.removeAll(n.getOutNodes());
                    int index = r.nextInt(toChoose.size());
                    Arc newArc = new Arc(toChoose.get(index).getLastLocation(), n.getLastLocation(), Arc.TypeOfArc.NORMAL);
                    listOfArcs.add(newArc);
                    arc_index++;
                }
            }

            // ARCS

            while (arc_index < arc_number) {


                int indexOfPlace = r.nextInt(listOfPlaces.size());
                int indexOfTransition = r.nextInt(listOfTransitions.size());

                if (r.nextBoolean()) {
                    if (listOfArcs.stream().anyMatch(x -> x.getStartNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getEndNode().getID() == listOfTransitions.get(indexOfTransition).getID())) {
                        if (conservative) {

                        } else {
                            Optional<Arc> arcToGainWeigjt = listOfArcs.stream().filter(x -> x.getStartNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getEndNode().getID() == listOfTransitions.get(indexOfTransition).getID()).findFirst();
                            arcToGainWeigjt.get().setWeight(arcToGainWeigjt.get().getWeight() + 1);
                            arc_index++;
                        }
                    } else {
                        if (listOfArcs.stream().anyMatch(x -> x.getEndNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getStartNode().getID() == listOfTransitions.get(indexOfTransition).getID())) {
                            //read arcki pomijamy
                        } else {
                            Arc newArc = new Arc(listOfPlaces.get(indexOfPlace).getLastLocation(), listOfTransitions.get(indexOfTransition).getLastLocation(), Arc.TypeOfArc.NORMAL);
                            /*if (listOfArcs.stream().filter(x -> x.getStartNode().getID() == newArc.getEndNode().getID() && x.getEndNode().getID() == newArc.getStartNode().getID()).count() > 0) {
                                System.out.println("Wtopa");
                            }*/
                            listOfArcs.add(newArc);
                            arc_index++;
                            /*if (listOfArcs.stream().filter(x -> x.getStartNode().getID() == newArc.getEndNode().getID() && x.getEndNode().getID() == newArc.getStartNode().getID()).count() > 0) {
                                System.out.println("Wtopa");
                            }*/
                        }
                    }
                } else {
                    if (listOfArcs.stream().anyMatch(x -> x.getEndNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getStartNode().getID() == listOfTransitions.get(indexOfTransition).getID())) {
                        if (conservative) {

                        } else {
                            Optional<Arc> arcToGainWeigjt = listOfArcs.stream().filter(x -> x.getEndNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getStartNode().getID() == listOfTransitions.get(indexOfTransition).getID()).findFirst();
                            arcToGainWeigjt.get().setWeight(arcToGainWeigjt.get().getWeight() + 1);
                            arc_index++;
                        }
                    } else {
                        if (listOfArcs.stream().anyMatch(x -> x.getStartNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getEndNode().getID() == listOfTransitions.get(indexOfTransition).getID())) {

                        } else {
                            Arc newArc = new Arc(listOfTransitions.get(indexOfTransition).getLastLocation(), listOfPlaces.get(indexOfPlace).getLastLocation(), Arc.TypeOfArc.NORMAL);
                            /*if (listOfArcs.stream().filter(x -> x.getStartNode().getID() == newArc.getEndNode().getID() && x.getEndNode().getID() == newArc.getStartNode().getID()).count() > 0) {
                                System.out.println("Wtopa");
                            }*/
                            listOfArcs.add(newArc);
                            arc_index++;
                            /*if (listOfArcs.stream().filter(x -> x.getStartNode().getID() == newArc.getEndNode().getID() && x.getEndNode().getID() == newArc.getStartNode().getID()).count() > 0) {
                                System.out.println("Wtopa");
                            }*/
                        }
                    }
                }
            }

            /*for (Arc a : listOfArcs) {
                if (listOfArcs.stream().filter(x -> x.getStartNode().getID() == a.getEndNode().getID() && x.getEndNode().getID() == a.getStartNode().getID()).count() > 0) {
                    System.out.println("Wtopa");
                }
            }*/

            /*
            //remove sink source
            for (Transition t : listOfTransitions) {
                if (t.getInArcs().size() == 0) {
                    ArrayList<Place> toChoose = new ArrayList<>(listOfPlaces);
                    toChoose.removeAll(t.getOutNodes());
                    int index = r.nextInt(toChoose.size());
                    Arc newArc = new Arc(toChoose.get(index).getLastLocation(), t.getLastLocation(), Arc.TypeOfArc.NORMAL);
                    listOfArcs.add(newArc);
                } else if (t.getOutArcs().size() == 0) {
                    ArrayList<Place> toChoose = new ArrayList<>(listOfPlaces);
                    toChoose.removeAll(t.getInNodes());
                    int index = r.nextInt(toChoose.size());
                    Arc newArc = new Arc(t.getLastLocation(), toChoose.get(index).getLastLocation(), Arc.TypeOfArc.NORMAL);
                    listOfArcs.add(newArc);
                }
            }
            */


/*
            for (Arc a : listOfArcs) {
                if (listOfArcs.stream().filter(x -> x.getStartNode().getID() == a.getEndNode().getID() && x.getEndNode().getID() == a.getStartNode().getID()).count() > 0) {
                    System.out.println("Wtopa");
                }
            }
*/

/*
            ArrayList<Arc> uniqueListOfArcs = new ArrayList<>();
            for (Arc a : listOfArcs) {
                if (uniqueListOfArcs.stream().filter(x -> x.getStartNode().getID() == a.getStartNode().getID() && x.getEndNode().getID() == a.getEndNode().getID()).count() == 0) {
                    if (listOfArcs.stream().filter(x -> x.getStartNode().getID() == a.getStartNode().getID() && x.getEndNode().getID() == a.getEndNode().getID()).count() > 1) {
                        ArrayList<Arc> listOfDuplicats = listOfArcs.stream().filter(x -> x.getStartNode().getID() == a.getStartNode().getID() && x.getEndNode().getID() == a.getEndNode().getID()).collect(Collectors.toCollection(ArrayList::new));
                        Arc tobeWeighted = listOfDuplicats.get(0);
                        tobeWeighted.setWeight(listOfDuplicats.size());
                        uniqueListOfArcs.add(tobeWeighted);
                        listOfDuplicats.remove(tobeWeighted);

                        tobeWeighted.getStartNode().getLastLocation().getOutArcs().removeAll(listOfDuplicats);
                        tobeWeighted.getEndNode().getLastLocation().getInArcs().removeAll(listOfDuplicats);
                    } else {
                        uniqueListOfArcs.add(a);
                    }
                }
            }


            System.out.println("unique: " + uniqueListOfArcs.size() + " total: " + listOfArcs.size());
            */
            //return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.TNET, listOfTransitions, listOfPlaces, null, null, null);

            //TEST ratio


            return new SubnetCalculator.SubNet(listOfArcs);
        } else {
            JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), "Za mało łuków aby stworzyć spójny graf - p: " + place_number + " t:" + transition_number + " a:" + arc_number, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
}
