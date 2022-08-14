package holmes.analyse.comparison.experiment;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.SubnetCalculator;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class NetGenerator {
    boolean conservative = false;

    int x_p = 10;
    int y_p = 10;
    int x_t = 70;
    int y_t = 10;

    IOprotocols io = new IOprotocols();
    //String directory = "/home/Szavislav/Eksperyment/Wyniki";
    String directory = "/home/bszawulak/Dokumenty/Eksperyment/Wyniki-den-60";


    String pathToFiles = "/home/labnanobio-01/Dokumenty/Eksperyment/";

    private void setNewDirectory(String path) {
        try {
            Files.createDirectories(Paths.get(directory + path));
        } catch (IOException erio) {
            System.out.println(erio.getMessage());
        }
    }

    public NetGenerator(boolean non) {
        int i = 0;
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


    public NetGenerator(String s) {
        GraphletsCalculator.generateGraphlets();
        for (int i = 0; i < 41; i = i + 5) {
            System.out.print("i" + i);
            for (int j = 0; j < 41; j = j + 5) {
                System.out.print("j" + j);
                for (int p = 0; p < 100; p++) {
                    //PetriNet pn1 = compareSpecificType(i,j,p,path,"BASE");
                    PetriNet pn2 = compareSpecificType(i, j, p, directory, "P3OVARIANT");

                    ArrayList<int[]> DGDV = new ArrayList<>();
                    //String tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-P3VARIANT";

                    SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(pn2.getArcs());
                    String tmpdir = directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-P3OVARIANT";
                    //io.writePNT(tmpdir + ".pnt", sn.getSubPlaces(), sn.getSubTransitions(), sn.getSubArcs());
                    DGDV = new ArrayList<>();

                    for (Node startNode : sn.getSubNode()) {
                        int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                        DGDV.add(vectorOrbit);
                    }

                    //DGDV
                    writeDGDV(tmpdir + "-DGDV.txt", DGDV);

                    //DGDDA
                    writeDGDDA(tmpdir + "-DGDDA.txt", DGDV);
                }

                System.out.println("p");
            }
        }
    }

    private PetriNet compareSpecificType(int i, int j, int p, String path, String type) {
        IOprotocols io = new IOprotocols();
        return io.serverReadPNT(path + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-" + type + ".pnt", 99);
    }

    public NetGenerator(String[] st) {
        generateAllFuckingGraphlets();
    }

    private void generateAllFuckingGraphlets() {

        Transition t1 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 270));
        Place p1 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Arc a1 = new Arc(t1.getElementLocations().get(0), p1.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> listOfArc = new ArrayList<>();
        listOfArc.add(a1);
        SubnetCalculator.SubNet sn1 = new SubnetCalculator.SubNet(listOfArc);

        io.writePNT("GRAFLETY/G2/GRAFLET-0.pnt", sn1.getSubPlaces(), sn1.getSubTransitions(), sn1.getSubArcs());

        Transition t2 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 170));
        Place p2 = new Place(IdGenerator.getNextId(), 99, new Point(100, 30));
        Arc a2 = new Arc(p2.getElementLocations().get(0), t2.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
        ArrayList<Arc> listOfArc2 = new ArrayList<>();
        listOfArc2.add(a2);
        SubnetCalculator.SubNet sn2 = new SubnetCalculator.SubNet(listOfArc2);

        io.writePNT("GRAFLETY/G2/GRAFLET-1.pnt", sn2.getSubPlaces(), sn2.getSubTransitions(), sn2.getSubArcs());
        //2size
        ArrayList<SubnetCalculator.SubNet> graphlets_2 = new ArrayList<SubnetCalculator.SubNet>();
        graphlets_2.add(sn1);
        graphlets_2.add(sn2);

        ArrayList<SubnetCalculator.SubNet> graphlets_3 = new ArrayList<SubnetCalculator.SubNet>();
        ArrayList<SubnetCalculator.SubNet> graphlets_4 = new ArrayList<SubnetCalculator.SubNet>();
        ArrayList<SubnetCalculator.SubNet> graphlets_5 = new ArrayList<SubnetCalculator.SubNet>();
        ArrayList<ArrayList<SubnetCalculator.SubNet>> graphlets = new ArrayList<>();
        graphlets.add(graphlets_2);
        graphlets.add(graphlets_3);
        graphlets.add(graphlets_4);
        graphlets.add(graphlets_5);


        int graphletCounter = 2;

        int graphletRead = 0;
        for (int i = 0; i < graphlets.size() - 1; i++) {
            ArrayList<SubnetCalculator.SubNet> gsn = graphlets.get(i);

            for (int j = 0; j < gsn.size(); j++) {
                PetriNet pn = io.serverReadPNT("GRAFLETY/G" + (i + 2) + "/GRAFLET-" + graphletRead + ".pnt", 99);
                SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.Export, null, null, pn.getNodes(), null, null);
//gsn.get(j);

                for (int k = 0; k < sn.getSubNode().size(); k++) {
                    Arc arc;
                    //System.out.println("Node dla (i)" +i + " (j)" +j +" (k)"+k);
                    //Places
                    if (sn.getSubNode().get(k).getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 270));
                        arc = new Arc(sn.getSubNode().get(k).getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
                        ArrayList<Arc> arcs = new ArrayList<>(sn.getSubArcs());
                        arcs.add(arc);
                        SubnetCalculator.SubNet snNew = new SubnetCalculator.SubNet(arcs, true);

                        if (doNotContain(snNew, graphlets.get(i + 1))) {
                            graphlets.get(i + 1).add(snNew);
                            io.writePNT("GRAFLETY/G" + (i + 3) + "/GRAFLET-" + graphletCounter + ".pnt", snNew.getSubPlaces(), snNew.getSubTransitions(), snNew.getSubArcs());
                            export(snNew.getSubNode(), i, graphletCounter);
                            graphletCounter++;
                        }
                    }
                    if (sn.getSubNode().get(k).getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                        Transition t3 = new Transition(IdGenerator.getNextId(), 99, new Point(60, 270));
                        arc = new Arc(t3.getElementLocations().get(0), sn.getSubNode().get(k).getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
                        ArrayList<Arc> arcs = new ArrayList<>(sn.getSubArcs());
                        arcs.add(arc);
                        SubnetCalculator.SubNet snNew = new SubnetCalculator.SubNet(arcs);

                        if (doNotContain(snNew, graphlets.get(i + 1))) {
                            graphlets.get(i + 1).add(snNew);
                            io.writePNT("GRAFLETY/G" + (i + 3) + "/GRAFLET-" + graphletCounter + ".pnt", snNew.getSubPlaces(), snNew.getSubTransitions(), snNew.getSubArcs());
                            export(snNew.getSubNode(), i, graphletCounter);
                            graphletCounter++;
                        }
                    }

                    //Transitions
                    if (sn.getSubNode().get(k).getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                        Place t3 = new Place(IdGenerator.getNextId(), 99, new Point(60, 270));
                        arc = new Arc(sn.getSubNode().get(k).getElementLocations().get(0), t3.getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
                        ArrayList<Arc> arcs = new ArrayList<>(sn.getSubArcs());
                        arcs.add(arc);
                        SubnetCalculator.SubNet snNew = new SubnetCalculator.SubNet(arcs);

                        if (doNotContain(snNew, graphlets.get(i + 1))) {
                            graphlets.get(i + 1).add(snNew);
                            io.writePNT("GRAFLETY/G" + (i + 3) + "/GRAFLET-" + graphletCounter + ".pnt", snNew.getSubPlaces(), snNew.getSubTransitions(), snNew.getSubArcs());
                            export(snNew.getSubNode(), i, graphletCounter);
                            graphletCounter++;
                        }
                    }
                    if (sn.getSubNode().get(k).getType().equals(PetriNetElement.PetriNetElementType.TRANSITION)) {
                        Place t3 = new Place(IdGenerator.getNextId(), 99, new Point(60, 270));
                        arc = new Arc(t3.getElementLocations().get(0), sn.getSubNode().get(k).getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
                        ArrayList<Arc> arcs = new ArrayList<>(sn.getSubArcs());
                        arcs.add(arc);
                        SubnetCalculator.SubNet snNew = new SubnetCalculator.SubNet(arcs);

                        if (doNotContain(snNew, graphlets.get(i + 1))) {
                            graphlets.get(i + 1).add(snNew);
                            io.writePNT("GRAFLETY/G" + (i + 3) + "/GRAFLET-" + graphletCounter + ".pnt", snNew.getSubPlaces(), snNew.getSubTransitions(), snNew.getSubArcs());
                            export(snNew.getSubNode(), i, graphletCounter);
                            graphletCounter++;
                        }
                    }

                    //ARCS

                    //System.out.println("Arc dla (i)" +i + " (j)" +j +" (k)"+k);
                    for (int e=0; e< graphlets.get(i + 1).size() ; e++) {
                        SubnetCalculator.SubNet exNet = graphlets.get(i + 1).get(e);
                        for (int p = 0; p < exNet.getSubPlaces().size(); p++) {
                            for (int t = 0; t < exNet.getSubTransitions().size(); t++) {
                                //out arc
                                //if()
                                int finalP = p;
                                int finalT = t;
                                if (!arcExist(exNet.getSubPlaces().get(p), exNet.getSubTransitions().get(t), exNet.getSubArcs()))
                                {

                                    arc = new Arc(exNet.getSubPlaces().get(p).getElementLocations().get(0), exNet.getSubTransitions().get(t).getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
                                    ArrayList<Arc> arcs = new ArrayList<>(exNet.getSubArcs());
                                    arcs.add(arc);
                                    SubnetCalculator.SubNet snNew = new SubnetCalculator.SubNet(arcs);

                                    if (doNotContain(snNew, graphlets.get(i + 1))) {
                                        graphlets.get(i + 1).add(snNew);
                                        io.writePNT("GRAFLETY/G" + (i + 3) + "/GRAFLET-" + graphletCounter + ".pnt", snNew.getSubPlaces(), snNew.getSubTransitions(), snNew.getSubArcs());
                                        export(snNew.getSubNode(), i, graphletCounter);
                                        graphletCounter++;
                                    }

                                    arc = new Arc( exNet.getSubTransitions().get(t).getElementLocations().get(0),exNet.getSubPlaces().get(p).getElementLocations().get(0), Arc.TypeOfArc.NORMAL);
                                    arcs = new ArrayList<>(exNet.getSubArcs());
                                    arcs.add(arc);
                                    snNew = new SubnetCalculator.SubNet(arcs);

                                    if (doNotContain(snNew, graphlets.get(i + 1))) {
                                        graphlets.get(i + 1).add(snNew);
                                        io.writePNT("GRAFLETY/G" + (i + 3) + "/GRAFLET-" + graphletCounter + ".pnt", snNew.getSubPlaces(), snNew.getSubTransitions(), snNew.getSubArcs());
                                        export(snNew.getSubNode(), i, graphletCounter);
                                        graphletCounter++;
                                    }

                                }
                                //arc = new Arc(exNet.getSubPlaces().get(p).getElementLocations().get(0), exNet.getSubTransitions().get(t).getElementLocations().get(0),Arc.TypeOfArc.NORMAL);
                            }
                        }
                    }

                }
                graphletRead++;
            }
        }

        System.out.println("Graphlets 2 : " + graphlets.get(0).size());
        System.out.println("Graphlets 3 : " + graphlets.get(1).size());
        System.out.println("Graphlets 4 : " + graphlets.get(2).size());
        System.out.println("Graphlets 5 : " + graphlets.get(3).size());


        int count = 0;
        int countGroup = 0;
        for (ArrayList<SubnetCalculator.SubNet> glist : graphlets) {
            ArrayList<Arc> combineArc = new ArrayList<>();
            ArrayList<Place> combinePlaces = new ArrayList<>();
            ArrayList<Transition> combineTransitions = new ArrayList<>();

            for (SubnetCalculator.SubNet toWriteNet : glist) {
                combineArc.addAll(toWriteNet.getSubArcs());
                combinePlaces.addAll(toWriteNet.getSubPlaces());
                combineTransitions.addAll(toWriteNet.getSubTransitions());

                //   io.writePNT("GRAFLETY/GRAFLET-"+count+".pnt",toWriteNet.getSubPlaces(),toWriteNet.getSubTransitions(),toWriteNet.getSubArcs());
                //   count++;
            }
            ArrayList<Node> nodes = new ArrayList<>();
            nodes.addAll(combinePlaces);
            nodes.addAll(combineTransitions);
            SubnetCalculator.SubNet doEzportu = new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.Export, null, null, nodes, null, null);

            io.writePNT("GRAFLETY/GRAFLET_Group-" + countGroup + ".pnt", combinePlaces, combineTransitions, combineArc);
            countGroup++;
        }
    }

    private boolean arcExist(Place place, Transition transition, ArrayList<Arc> subArcs) {

        for (Arc a : subArcs) {
            if ((a.getStartNode().equals(place) && a.getEndNode().equals(transition)) || (a.getStartNode().equals(transition) && a.getEndNode().equals(place))) {
                return true;
            }
        }
        return false;
    }

    private void export(ArrayList<Node> listOfParentNodes, int number, int graphletCounter) {
        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.Export, null, null, listOfParentNodes, null, null);

        try {
            //FileDialog fDialog = new FileDialog(new JFrame(), "Save", FileDialog.SAVE);
            //fDialog.setVisible(true);
            String path = "GRAFLETY/G" + (number + 3) + "/G-" + graphletCounter + ".xml";

            FileOutputStream out = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(sn);
            oos.flush();
        } catch (Exception e) {
            System.out.println("Problem serializing: " + e);
        }

    }

    private boolean doNotContain(SubnetCalculator.SubNet snNew, ArrayList<SubnetCalculator.SubNet> subNets) {
        for (SubnetCalculator.SubNet snOld : subNets) {
            if (isomorphisc(snNew, snOld)) {
                return false;
            }
        }
        return true;
    }

    private boolean isomorphisc(SubnetCalculator.SubNet snNew, SubnetCalculator.SubNet snOld) {
        boolean isomorphic = false;
        if (snNew.getSubPlaces().size() == snOld.getSubPlaces().size() &&
                snNew.getSubTransitions().size() == snOld.getSubTransitions().size() &&
                snNew.getSubArcs().size() == snOld.getSubArcs().size()) {

            int countM = 0;
            for (Node m : snOld.getSubNode()) {
                boolean matched = false;
                for (Node n : snNew.getSubNode()) {
                    if (n.getType().equals(m.getType())) {
                        for (Node mIn : m.getInNodes()) {
                            boolean in = false;
                            boolean out = false;
                            for (Node nIn : n.getInNodes()) {
                                if (nIn.getInNodes().size() == mIn.getInNodes().size())
                                    in = true;
                            }
                            for (Node nIn : n.getOutNodes()) {
                                if (nIn.getOutNodes().size() == mIn.getOutNodes().size())
                                    out = true;
                            }

                            if (in && out) {
                                matched = true;
                            }
                        }
                    }
                }

                if (matched)
                    countM++;
            }

            //eachNode has the same surround
            int countN = 0;
            for (Node n : snNew.getSubNode()) {
                boolean matched = false;
                for (Node m : snOld.getSubNode()) {
                    if (n.getType().equals(m.getType())) {
                        for (Node nIn : n.getInNodes()) {
                            boolean in = false;
                            boolean out = false;
                            for (Node mIn : m.getInNodes()) {
                                if (nIn.getInNodes().size() == mIn.getInNodes().size())
                                    in = true;
                            }
                            for (Node mIn : m.getOutNodes()) {
                                if (nIn.getOutNodes().size() == mIn.getOutNodes().size())
                                    out = true;
                            }

                            if (in && out) {
                                matched = true;
                            }
                        }
                    }
                }

                if (matched)
                    countN++;
            }
            //z perspektywy M?

            if (countN == snNew.getSubNode().size() && countN == countM)
                isomorphic = true;
        }

        return isomorphic;
    }

    public NetGenerator() {
        GraphletsCalculator.generateGraphlets();
        PetriNet pn = io.serverReadPNT("/home/Szavislav/Eksperyment/Distortion/BASE.pnt", 99);

        ArrayList<int[]> DGDV = new ArrayList<>();
        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(pn.getArcs());
        for (Node startNode : sn.getSubNode()) {
            int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
            DGDV.add(vectorOrbit);
        }
        writeDGDV("/home/Szavislav/Eksperyment/Distortion/net-BASE" + "-DGDV.txt", DGDV);
        writeDGDDA("/home/Szavislav/Eksperyment/Distortion/net-BASE" + "-DGDDA.txt", DGDV);

        //SubnetCalculator.SubNet nsn = addStar4(cloneSubNet(sn));
        for (int t = 0; t < sn.getSubTransitions().size(); t++) {
            String tmpdir = "/home/Szavislav/Eksperyment/Distortion/net-" + t;
            SubnetCalculator.SubNet nsn = addDistortion(cloneSubNet(sn), t, true);
            io = new IOprotocols();
            io.writePNT(tmpdir + "A.pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
            DGDV = new ArrayList<>();
            for (Node startNode : nsn.getSubNode()) {
                int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                DGDV.add(vectorOrbit);
            }
            writeDGDV(tmpdir + "A" + "-DGDV.txt", DGDV);
            writeDGDDA(tmpdir + "A" + "-DGDDA.txt", DGDV);
        }

        for (int t = 0; t < sn.getSubTransitions().size(); t++) {
            String tmpdir = "/home/Szavislav/Eksperyment/Distortion/net-" + t;
            SubnetCalculator.SubNet nsn = addDistortion(cloneSubNet(sn), t, false);
            IOprotocols io = new IOprotocols();
            io.writePNT(tmpdir + "B.pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
            DGDV = new ArrayList<>();
            for (Node startNode : nsn.getSubNode()) {
                int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
                DGDV.add(vectorOrbit);
            }
            writeDGDV(tmpdir + "B" + "-DGDV.txt", DGDV);
            writeDGDDA(tmpdir + "B" + "-DGDDA.txt", DGDV);
        }
    }

    public NetGenerator(float f) {
        GraphletsCalculator.generateGraphlets();
        PetriNet pn = io.serverReadPNT("/home/Szavislav/Eksperyment/Distortion/BASE.pnt", 99);

        ArrayList<int[]> DGDV = new ArrayList<>();
        SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(pn.getArcs());

        SubnetCalculator.SubNet nsn = addIndependentDistortion(sn);
        io.writePNT("/home/Szavislav/Eksperyment/Distortion/BASE+sub.pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
        for (Node startNode : nsn.getSubNode()) {
            int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
            DGDV.add(vectorOrbit);
        }
        writeDGDV("/home/Szavislav/Eksperyment/Distortion/net-BASE+sub" + "-DGDV.txt", DGDV);
        writeDGDDA("/home/Szavislav/Eksperyment/Distortion/net-BASE+sub" + "-DGDDA.txt", DGDV);

    }

    private SubnetCalculator.SubNet addDistortion(SubnetCalculator.SubNet sn, int position, boolean direction) {
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

        //int index = getBaseNetTtansitionIndex(sn);
        if (direction)
            listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(position).getLastLocation(), Arc.TypeOfArc.NORMAL));
        else
            listOfArc.add(new Arc(sn.getSubTransitions().get(position).getLastLocation(), p1.getLastLocation(), Arc.TypeOfArc.NORMAL));

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    private SubnetCalculator.SubNet addIndependentDistortion(SubnetCalculator.SubNet sn) {
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

        //int index = getBaseNetTtansitionIndex(sn);

        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }


    public NetGenerator(int i_min, int i_max, int j_min, int j_max, int p_min, int p_max, boolean a) {
        System.out.println("P3OVARIANT");
        for (int i = i_min; i < i_max; i = i + 5) {
            System.out.print("i" + i);
            for (int j = j_min; j < j_max; j = j + 5) {
                System.out.print("j" + j);
                for (int p = p_min; p < p_max; p++) {
                    IOprotocols io = new IOprotocols();
                    PetriNet pn = io.serverReadPNT(directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-BASE.pnt", 99);
                    ArrayList<Arc> listOfArcs = pn.getArcs();
                    SubnetCalculator.SubNet sn = new SubnetCalculator.SubNet(listOfArcs);
                    SubnetCalculator.SubNet nsn = addParallel3One(sn);

                    io.writePNT(directory + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-P3OVARIANT.pnt", nsn.getSubPlaces(), nsn.getSubTransitions(), nsn.getSubArcs());
                }
                System.out.println("p100");
            }
        }
    }

    public NetGenerator(int i_min, int i_max, int j_min, int j_max, int p_min, int p_max) {
        for (int i = i_min; i < i_max; i = i + 5) {
            for (int j = j_min; j < j_max; j = j + 5) {
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

    public NetGenerator(int i_min, int i_max, int j_min, int j_max, int p_min, int p_max, int md, int density) {//, String path) {
        //this.directory = path;

        //int minDensicty = md;//i_min + j_min + 19;

        for (int d = md; d < density; d++) {
            for (int i = i_min; i < i_max; i = i + 5) {
                for (int j = j_min; j < j_max; j = j + 5) {
                    setNewDirectory("/d" + d + "i" + i + "j" + j);
                    for (int p = p_min; p < p_max; p++) { // - próbka 100 sieci
                        SubnetCalculator.SubNet sn = generateNet(10 + i, 10 + j, d);
                        setNewDirectory("/d" + d + "i" + i + "j" + j + "/d" + d + "i" + i + "j" + j + "p" + p);

                        String tmpdir = directory + "/d" + d + "i" + i + "j" + j + "/d" + d + "i" + i + "j" + j + "p" + p + "/d" + d + "i" + i + "j" + j + "p" + p + "-BASE";

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


                        /*
                        //tu można by zacząć dodawać nowe rozszrzenia
                        //------ 1S VARIANT ------
                        SubnetCalculator.SubNet nsn = addStar4(cloneSubNet(sn));
                        tmpdir = directory + "/d" + d + "i" + i + "j" + j + "/d" + d + "i" + i + "j" + j + "p" + p + "/d" + d + "i" + i + "j" + j + "p" + p + "-1S";
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

                        //------ 1S1S VARIANT ------
                        nsn = addStar4(cloneSubNet(sn));
                        nsn = addStar4sec(cloneSubNet(nsn), sn);
                        tmpdir = directory + "/d" + d + "i" + i + "j" + j + "/d" + d + "i" + i + "j" + j + "p" + p + "/d" + d + "i" + i + "j" + j + "p" + p + "-1S1S";
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

                        //------ 2S VARIANT ------
                        nsn = addStar4a2(cloneSubNet(sn));
                        tmpdir = directory + "/d" + d + "i" + i + "j" + j + "/d" + d + "i" + i + "j" + j + "p" + p + "/d" + d + "i" + i + "j" + j + "p" + p + "-1S1S";
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
                        */
                    }
                }
            }
        }
    }

    private void cleanNetwork(SubnetCalculator.SubNet sn) {
        for (Node n : sn.getSubNode()) {
            ArrayList<Arc> tmp = new ArrayList<>(n.getOutInArcs());
            for (Arc arc : tmp) {
                if (!sn.getSubArcs().contains(arc)) {
                    n.getOutInArcs().remove(arc);
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


    public SubnetCalculator.SubNet addStar4a2(SubnetCalculator.SubNet sn) {
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
        int index2 = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(sn.getSubTransitions().get(index2).getLastLocation(), p2.getLastLocation(), Arc.TypeOfArc.NORMAL));

        //int index = getBaseNetTtansitionIndex(sn);
        //listOfArc.add(new Arc(p1.getLastLocation(), sn.getSubTransitions().get(index).getLastLocation(), Arc.TypeOfArc.NORMAL));

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

    public SubnetCalculator.SubNet addStar4sec(SubnetCalculator.SubNet sn, SubnetCalculator.SubNet old) {
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
        int index = getBaseNetTtansitionIndex(old);
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
        listOfTransition.addAll(new ArrayList<>(sn.getSubTransitions()));
        listOfPlace.addAll(new ArrayList<>(sn.getSubPlaces()));
        listOfArc.addAll(new ArrayList<>(sn.getSubArcs()));

        return new SubnetCalculator.SubNet(listOfArc);
    }

    public SubnetCalculator.SubNet addParallel3One(SubnetCalculator.SubNet sn) {
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

        int index2 = getBaseNetTtansitionIndex(sn);
        listOfArc.add(new Arc(sn.getSubTransitions().get(index2).getLastLocation(), p2.getLastLocation(), Arc.TypeOfArc.NORMAL));
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
        for (int[] ints : DGDV) {
            int localMax = Arrays.stream(ints).max().getAsInt();
            if (localMax > max)
                max = localMax;
        }

        int orbitNumber = DGDV.get(0).length;

        int[][] d = new int[orbitNumber][max + 1];

        for (int[] ints : DGDV) {
            for (int m = 0; m < ints.length; m++) {
                if (ints[m] > 0) {
                    d[m][ints[m]]++;
                }
            }
        }


        for (int[] ints : d) {
            try {
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(Arrays.toString(ints) + "\r\n");
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
                    double dd = d[orb][k];
                    ss[k] = dd / (double) k;

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
        File file = new File(directory);

        for (int[] ints : DGDV) {
            try {
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(Arrays.toString(ints) + "\r\n");
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
                if (place_index < place_number) {
                    new_place = new Place(IdGenerator.getNextId(), 0, new Point(x_p, y_p));
                    y_p += 30;
                    place_index++;
                    listOfPlaces.add(new_place);
                    isNewPlace = true;
                }

                if (transition_index < transition_number) {
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
                            index--;
                        }

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
                        if (!conservative) {
                            Optional<Arc> arcToGainWeigjt = listOfArcs.stream().filter(x -> x.getStartNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getEndNode().getID() == listOfTransitions.get(indexOfTransition).getID()).findFirst();
                            arcToGainWeigjt.get().setWeight(arcToGainWeigjt.get().getWeight() + 1);
                            arc_index++;
                        }
                    } else {
                        if (listOfArcs.stream().anyMatch(x -> x.getEndNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getStartNode().getID() == listOfTransitions.get(indexOfTransition).getID())) {
                            //read arcki pomijamy
                        } else {
                            Arc newArc = new Arc(listOfPlaces.get(indexOfPlace).getLastLocation(), listOfTransitions.get(indexOfTransition).getLastLocation(), Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            arc_index++;
                        }
                    }
                } else {
                    if (listOfArcs.stream().anyMatch(x -> x.getEndNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getStartNode().getID() == listOfTransitions.get(indexOfTransition).getID())) {
                        if (!conservative) {
                            Optional<Arc> arcToGainWeigjt = listOfArcs.stream().filter(x -> x.getEndNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getStartNode().getID() == listOfTransitions.get(indexOfTransition).getID()).findFirst();
                            arcToGainWeigjt.get().setWeight(arcToGainWeigjt.get().getWeight() + 1);
                            arc_index++;
                        }
                    } else {
                        if (listOfArcs.stream().anyMatch(x -> x.getStartNode().getID() == listOfPlaces.get(indexOfPlace).getID() && x.getEndNode().getID() == listOfTransitions.get(indexOfTransition).getID())) {

                        } else {
                            Arc newArc = new Arc(listOfTransitions.get(indexOfTransition).getLastLocation(), listOfPlaces.get(indexOfPlace).getLastLocation(), Arc.TypeOfArc.NORMAL);
                            listOfArcs.add(newArc);
                            arc_index++;
                        }
                    }
                }
            }
            //TEST ratio
            return new SubnetCalculator.SubNet(listOfArcs);
        } else {
            JOptionPane.showMessageDialog(GUIManager.getDefaultGUIManager(), "Za mało łuków aby stworzyć spójny graf - p: " + place_number + " t:" + transition_number + " a:" + arc_number, "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
}

