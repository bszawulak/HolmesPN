package holmes.analyse.comparison;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.SubnetCalculator;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class GraphletComparator {

    int orbNumber = 98;
    int testNetExtensionsNumber = 6;

    public GraphletComparator(int orb) {
        this.orbNumber = orb;
    }

    public void compare() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                for (int p = 0; p < 10; p++) {
                    double[][] comparisonTable = new double[testNetExtensionsNumber][testNetExtensionsNumber];
                    for (int variant = 0; variant < testNetExtensionsNumber; variant++) {
                        for (int variant2 = 0; variant2 < testNetExtensionsNumber; variant2++) {
                            if (variant != variant2) {
                                comparisonTable[variant][variant2] = calcDGDDA(getPath(i, j, p, getName(variant)), getPath(i, j, p, getName(variant2)));
                            } else {
                                comparisonTable[variant][variant2] = -1;
                            }
                        }
                    }

                    try {
                        FileWriter myWriter = new FileWriter(getPath(i, j, p, "WYNIK"));
                        for (double[] line : comparisonTable) {
                            for (int k = 0; k < line.length; k++) {
                                String str = String.valueOf(line[k]);
                                if (k + 1 < line.length) {
                                    str += ",";
                                }
                                myWriter.write(str);
                            }
                            myWriter.write("\n");
                        }
                        myWriter.close();
                        System.out.println("Successfully wrote to the file - i:" + i + " j:" + j + " p:" + p);
                    } catch (IOException e) {
                        System.out.println("An error occurred - i:" + i + " j:" + j + " p:" + p);
                        e.printStackTrace();
                    }
                }
            }
        }

		/*
		GraphletComparator gc = new GraphletComparator();

		double result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-S4VARIANT-DGDDA.txt");
		System.out.println("-------B-S4 " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LVARIANT-DGDDA.txt");
		System.out.println("-------B-S4L " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LkVARIANT-DGDDA.txt");
		System.out.println("-------B-S4Lk " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-E2VARIANT-DGDDA.txt");
		System.out.println("-------B-E2 " + result);
		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-BASE-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-C6VARIANT-DGDDA.txt");
		System.out.println("-------B-C6 " + result);

		result = gc.calcDGDDA("/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LVARIANT-DGDDA.txt","/home/Szavislav/Eksperyment/Wyniki/i0j0/i0j0p0/i0j0p0-K4LkVARIANT-DGDDA.txt");
		System.out.println("-------S4L-S4Lk " + result);
		*/

    }

    public String getPath(int i, int j, int p, String name) {
        return "/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-" + name + "-DGDDA.txt";
    }

    public String getPath(int d, int i, int j, int p, String name) {
        return "/home/bartek/Eksperyment/Wyniki/d" + d + "i" + i + "j" + j + "/d" + d + "i" + i + "j" + j + "p" + p + "/d" + d + "i" + i + "j" + j + "p" + p + "-" + name + "-DGDDA.txt";
    }

    public void subNetcompare(String path) {
        for (int i = 0; i < 41; i = i + 5) {
            for (int j = 0; j < 41; j = j+5) {
                for (int p = 0; p < 100; p++) {
                    PetriNet pn1 = compareSpecificType(i,j,p,path,"BASE");
                    PetriNet pn2 = compareSpecificType(i,j,p,path,"P3OVARIANT");

                }
            }
        }
    }

    public void complexNDcompare(String path) {
        for (int i = 0; i < 41; i = i + 5) {
            for (int j = 0; j < 41; j = j+5) {
                for (int p = 0; p < 100; p++) {
                    PetriNet pn1 = compareSpecificType(i,j,p,path,"BASE");
                    PetriNet pn2 = compareSpecificType(i,j,p,path,"P3OVARIANT");
                }
            }
        }
    }

    private PetriNet  compareSpecificType(int i, int j, int p, String path,String type) {
        IOprotocols io = new IOprotocols();
        return io.serverReadPNT(path + "/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p + "/i" + i + "j" + j + "p" + p + "-" + type + ".pnt", 99);
    }

    public String compareNetdiv(int k, int deep, PetriNet pn1, PetriNet pn2) {
        //int deep = 3;
        //PetriNet pn1 = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
        //PetriNet pn2 = GUIManager.getDefaultGUIManager().getWorkspace().getProject();

        ArrayList<SubnetCalculator.SubNet> egoPN2 = new ArrayList<>();
        ArrayList<int[]> orbitsPN2 = new ArrayList<>();
        ArrayList<long[]> graphletsPN2 = new ArrayList<>();

        ArrayList<EgoNetwork> egs2 = new ArrayList<>();

        for (Node n : pn2.getNodes()) {
            SubnetCalculator.SubNet sn = createEgoNetwork(n, deep);
            //System.out.println("N1 - "+ sn.getSubNode().size());
            egoPN2.add(sn);
            GraphletsCalculator.GraphletsCalculator();
            GraphletsCalculator.getFoundGraphlets();
            ArrayList<Node> list = sn.getSubNode().stream().filter(x->x.getName().equals(n.getName())).collect(Collectors.toCollection(ArrayList::new));
            int[] orbits = GraphletsCalculator.vectorOrbit(list.get(0), false);
            long[] singleDRGF = new long[GraphletsCalculator.graphetsList.size()];
            for (int i = 0; i < GraphletsCalculator.graphetsList.size(); i++) {
                int finalI = i;
                long val = GraphletsCalculator.uniqGraphlets.stream().filter(x -> x.getGraphletID() == finalI).count();
                singleDRGF[i] = val;
            }
            orbitsPN2.add(orbits);
            graphletsPN2.add(singleDRGF);
            GraphletsCalculator.cleanAll();

            EgoNetwork eg = new EgoNetwork(sn.getSubNode().size(), orbits, singleDRGF, sn);
            egs2.add(eg);
        }

        ArrayList<SubnetCalculator.SubNet> egoPN1 = new ArrayList<>();
        ArrayList<int[]> orbitsPN1 = new ArrayList<>();
        ArrayList<long[]> graphletsPN1 = new ArrayList<>();

        ArrayList<EgoNetwork> egs1 = new ArrayList<>();

        for (Node n : pn1.getNodes()) {
            SubnetCalculator.SubNet sn = createEgoNetwork(n, deep);
            egoPN1.add(sn);
            GraphletsCalculator.GraphletsCalculator();
            GraphletsCalculator.getFoundGraphlets();
            //TODO nie dla n ale dla całęgo sn
            ArrayList<Node> list = sn.getSubNode().stream().filter(x->x.getName().equals(n.getName())).collect(Collectors.toCollection(ArrayList::new));
            int[] orbits = GraphletsCalculator.vectorOrbit(list.get(0), false);
            //int[] orbits = GraphletsCalculator.vectorOrbit(n, false);
            long[] singleDRGF = new long[GraphletsCalculator.graphetsList.size()];
            for (int i = 0; i < GraphletsCalculator.graphetsList.size(); i++) {
                int finalI = i;
                long val = GraphletsCalculator.uniqGraphlets.stream().filter(x -> x.getGraphletID() == finalI).count();
                singleDRGF[i] = val;
            }
            orbitsPN1.add(orbits);
            graphletsPN1.add(singleDRGF);
            GraphletsCalculator.cleanAll();

            EgoNetwork eg = new EgoNetwork(sn.getSubNode().size(), orbits, singleDRGF, sn);
            egs1.add(eg);
        }

        //FOR ALL OF SIZE K

        //int k = 3;

        double m1 = 0;
        double m2 = 0;
        double m = 0;

        //główna wartość mierzona
        double ss = 0;

        GraphletsCalculator.generateGraphlets();

        for (int g = 0; g < GraphletsCalculator.graphetsList.size(); g++) {
            if (GraphletsCalculator.graphetsList.get(g).getSubNode().size() == k) {
                //calc M(k)
                //S_w(G)
                int S_wG = 0;
                for (EgoNetwork eg : egs1) {
                    S_wG += eg.graphlets[g];
                }

                int S_wH = 0;
                for (EgoNetwork eg : egs2) {
                    S_wH += eg.graphlets[g];
                }

                if (S_wG != 0 || S_wH != 0) {
                    m1 += (double) (S_wG ^ 2) / (double) Math.sqrt(S_wG ^ 2 + S_wH ^ 2);
                    m2 += (double) (S_wH ^ 2) / (double) Math.sqrt(S_wG ^ 2 + S_wH ^ 2);

                    ss += ((double) S_wG * S_wH) / (double) Math.sqrt(S_wG ^ 2 + S_wH ^ 2);
                }
            }
        }
        m = m1 * m2;

        double netD = ss / m;
        double netd = 0.5 * (1 - netD);

        //SubnetCalculator.functionalSubNets = egoPN1;

        return "k: " + k + " deep: " + deep + " netDiv: " + Double.toString(netd) + "\n\r";
    }

    private SubnetCalculator.SubNet createEgoNetwork(Node n, int i) {
        ArrayList<Node> listOfNodes = new ArrayList<>();
        listOfNodes.add(n);
        listOfNodes = deepDown(n, i, listOfNodes);


        if(n.getName().contains("srodek"))
        {
            System.out.println("ie " + i );
            System.out.println("how " + listOfNodes.size());
            /*
            listOfNodes = new ArrayList<>();
            listOfNodes = deepDown(n, 1, listOfNodes);
            listOfNodes = new ArrayList<>();
            listOfNodes = deepDown(n, 2, listOfNodes);
            listOfNodes = new ArrayList<>();
            listOfNodes = deepDown(n, 3, listOfNodes);
            listOfNodes = new ArrayList<>();
            listOfNodes = deepDown(n, 4, listOfNodes);
            listOfNodes = new ArrayList<>();
            listOfNodes = deepDown(n, 5, listOfNodes);
            listOfNodes = new ArrayList<>();
            listOfNodes = deepDown(n, 6, listOfNodes);

             */
        }


        return new SubnetCalculator.SubNet(SubnetCalculator.SubNetType.Export, null, null, listOfNodes, null, null);
    }

    private ArrayList<Node> deepDown(Node n, int i, ArrayList<Node> listOfNodes) {
        i--;
        if (i < 0) {
            return listOfNodes;
        }
        for (Node m : n.getOutInNodes()) {
            if (!listOfNodes.contains(m)) {
                listOfNodes.add(m);
                //listOfNodes.addAll(deepDown(m,i,listOfNodes));
                listOfNodes = addUnique(deepDown(m, i, listOfNodes), listOfNodes);
            }
        }
        return listOfNodes;
    }

    private ArrayList<Node> addUnique(ArrayList<Node> l1, ArrayList<Node> l2) {
        for (Node n : l1) {
            if (!l2.contains(n))
                l2.add(n);
        }
        return l2;
    }


    private String getName(int variant) {
        switch (variant) {
            case 0:
                return "BASE";
            case 1:
                return "S4VARIANT";
            case 2:
                return "K4LVARIANT";
            case 3:
                return "K4LkVARIANT";
            case 4:
                return "E2VARIANT";
            case 5:
                return "C6VARIANT";
            case 6:
                return "P3VARIANT";
            case 7:
                return "P3OVARIANT";
            default:
                return "BASE";
        }
    }

    public double calcDGDDA(String path1, String path2) {

        double[][] nG = lodaN(path1);
        double[][] nH = lodaN(path2);

        if (nH == null || nH.length == 0) {
            return -1;
        }
        int maxk = Math.max(nG[0].length, nH[0].length);

        double[] d = new double[orbNumber];

        for (int orb = 0; orb < orbNumber; orb++) {
            double di = 0;

            for (int k = 0; k < maxk; k++) {
                if (k >= nG[orb].length) {
                    di += Math.pow(0 - nH[orb][k], 2);
                } else if (k >= nH[orb].length) {
                    di += Math.pow(nG[orb][k] - 0, 2);
                } else {
                    di += Math.pow(nG[orb][k] - nH[orb][k], 2);
                }
            }

            d[orb] = 1 - (1 / Math.sqrt(2)) * Math.sqrt(di);
        }

        return DoubleStream.of(d).sum() / orbNumber;
    }

    public double[] calcDGDDApartitioned(String path1, String path2) {

        double[][] nG = lodaN(path1);
        double[][] nH = lodaN(path2);

        if (nH == null || nH.length == 0) {
            return new double[0];
        }
        int maxk = Math.max(nG[0].length, nH[0].length);

        double[] d = new double[orbNumber];

        for (int orb = 0; orb < orbNumber; orb++) {
            double di = 0;

            for (int k = 0; k < maxk; k++) {
                if (k >= nG[orb].length) {
                    di += Math.pow(0 - nH[orb][k], 2);
                } else if (k >= nH[orb].length) {
                    di += Math.pow(nG[orb][k] - 0, 2);
                } else {
                    di += Math.pow(nG[orb][k] - nH[orb][k], 2);
                }
            }

            d[orb] = 1 - (1 / Math.sqrt(2)) * Math.sqrt(di);
        }

        return d;
    }

    private double[][] lodaN(String path) {

        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        boolean startRead = false;

        List<double[]> result = new ArrayList<>();

        if (scanner != null) {
            while (scanner.hasNext()) {

                String line = scanner.nextLine();

                if (line.contains("-n")) {
                    startRead = true;
                    line = scanner.nextLine();
                }

                if (startRead) {
                    line = line.replace("[", "");
                    line = line.replace("]", "");
                    //to test
                    line = line.replace("NaN", "0.0");

                    String[] lin = line.split(",");

                    double[] shortResult = new double[lin.length];

                    for (int i = 0; i < lin.length; i++) {
                        //System.out.println(lin[i]);
                        try {
                            shortResult[i] = Double.parseDouble(lin[i]);
                        } catch (Exception ex) {
                            //System.out.println("Line: " + lin);
                        }
                    }

                    result.add(shortResult);
                }


            }
        }
        scanner.close();

        double[][] matrix = new double[result.size()][];
        return result.toArray(matrix);

        //return (double[][]) result.toArray();
    }

    public class EgoNetwork {

        public int size = 0;
        public int[] orbits = new int[0];
        public long[] graphlets = new long[0];
        public SubnetCalculator.SubNet subNet;


        public EgoNetwork(int s, int[] o, long[] g, SubnetCalculator.SubNet sn) {
            this.size = s;
            this.orbits = o;
            this.graphlets = g;
            this.subNet = sn;
        }
    }
}
