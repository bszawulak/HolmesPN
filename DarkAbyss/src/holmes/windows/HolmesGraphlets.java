package holmes.windows;

import holmes.analyse.GraphletsCalculator;
import holmes.analyse.comparison.GraphletComparator;
import holmes.analyse.comparison.experiment.NetGenerator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.*;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

public class HolmesGraphlets extends JFrame {

    private GUIManager overlord;
    private JPanel mainPanel;
    JComboBox<String> orbit;
    JComboBox<String> nodeList;
    JComboBox<String> graphletList;


    JTextArea jtaImin;
    JTextArea jtaJmin;
    JTextArea jtaPmin;

    JTextArea jtaImax;
    JTextArea jtaJmax;
    JTextArea jtaPmax;


    JComboBox<String> orbitSorted;
    JComboBox<String> graphletListSorted;
    JComboBox<String> graphletListUniq;

    String[] orbitData = {" --O-- "};
    String[] nodeData = {" --N-- "};
    String[] graphData = {" --G-- "};

    String[] orbitDataSorted = {" --OS-- "};
    String[] graphDataSorted = {" --GS-- "};


    String[] graphDataUniq = {" --GU-- "};

    public HolmesGraphlets() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception e) {

        }
        overlord = GUIManager.getDefaultGUIManager();
        setVisible(false);
        this.setTitle("Graphlets");

        setLayout(new BorderLayout());
        setSize(new Dimension(900, 700));
        setLocation(15, 15);

        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {

        JPanel panel = new JPanel(new FlowLayout());

        int posX = 0;
        int posY = 0;

        JButton generateButton = new JButton("<html>Generate</html>");
        generateButton.setBounds(posX, posY, 120, 36);
        generateButton.setMargin(new Insets(0, 0, 0, 0));
        generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        generateButton.addActionListener(actionEvent -> generateGraphletOrbits());
        generateButton.setFocusPainted(false);
        panel.add(generateButton);//,BorderLayout.LINE_START);

        orbit = new JComboBox<String>(orbitData);
        orbit.setBounds(posX + 130, posY, 250, 20);
        orbit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
                int selected = comboBox.getSelectedIndex();
                ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < GraphletsCalculator.graphlets.get(selected).size(); i++) {
                    data.add("Node : " + i);
                }

                nodeData = data.stream().toArray(String[]::new);
                nodeList.setModel(new DefaultComboBoxModel<>(nodeData));


                ArrayList<String> dataS = new ArrayList<>();
                for (int i = 0; i < GraphletsCalculator.sortedGraphlets.get(selected).size(); i++) {
                    dataS.add("Graphlet : " + i);
                }

                graphDataSorted = dataS.stream().toArray(String[]::new);
                graphletListSorted.setModel(new DefaultComboBoxModel<>(graphDataSorted));
            }
        });
        panel.add(orbit, BorderLayout.CENTER);

        nodeList = new JComboBox<String>(nodeData);
        nodeList.setBounds(posX + 130, posY + 80, 250, 20);
        nodeList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
                int selectedOrbit = orbit.getSelectedIndex();
                int selectedNode = comboBox.getSelectedIndex();
                ArrayList<String> data = new ArrayList<>();
                for (int i = 0; i < GraphletsCalculator.graphlets.get(selectedOrbit).get(selectedNode).size(); i++) {
                    data.add("Graphlet : " + i);
                }
                graphData = data.stream().toArray(String[]::new);
                graphletList.setModel(new DefaultComboBoxModel<>(graphData));

            }
        });
        panel.add(nodeList, BorderLayout.NORTH);

        graphletList = new JComboBox<String>(graphData);
        graphletList.setBounds(posX + 130, posY + 160, 250, 20);
        graphletList.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
                int selectedOrbit = orbit.getSelectedIndex();
                int selectedNode = nodeList.getSelectedIndex();
                int selectedGraphlet = comboBox.getSelectedIndex();
                ArrayList<String> data = new ArrayList<>();

                GraphletsCalculator.Struct graphletStructure = GraphletsCalculator.graphlets.get(selectedOrbit).get(selectedNode).get(selectedGraphlet);

                paintGraphlet(graphletStructure);
            }
        });
        panel.add(graphletList, BorderLayout.EAST);


        jtaImin = new JTextArea("Set Place min number +10");
        jtaImin.setBounds(posX + 130, posY + 160, 250, 20);
        panel.add(jtaImin, BorderLayout.SOUTH);

        jtaImax = new JTextArea("Set Place max numbejtaIr +10");
        jtaImax.setBounds(posX + 130, posY + 160, 250, 20);
        panel.add(jtaImax, BorderLayout.SOUTH);

        jtaJmin = new JTextArea("Set Transition min  number +10");
        jtaJmin.setBounds(posX + 130, posY + 160, 250, 20);
        panel.add(jtaJmin, BorderLayout.SOUTH);

        jtaJmax = new JTextArea("Set Transition max number +10");
        jtaJmax.setBounds(posX + 130, posY + 160, 250, 20);
        panel.add(jtaJmax, BorderLayout.SOUTH);

        jtaPmin = new JTextArea("Set min number of probe");
        jtaPmin.setBounds(posX + 130, posY + 160, 250, 20);
        panel.add(jtaPmin, BorderLayout.SOUTH);

        jtaPmax = new JTextArea("Set max number of probe");
        jtaPmax.setBounds(posX + 130, posY + 160, 250, 20);
        panel.add(jtaPmax, BorderLayout.SOUTH);
        //sorted
/*
        orbitSorted = new JComboBox<String>(orbitDataSorted);
        orbitSorted.setBounds(posX+130, posY, 250, 20);
        orbitSorted.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
                int selected = comboBox.getSelectedIndex();
                ArrayList<String> data = new ArrayList<>();
                for(int i = 0 ; i < GraphletsCalculator.sortedGraphlets.get(selected).size() ; i++){
                    data.add("Node : " + i );
                }

                nodeData = data.stream().toArray(String[]::new);
                nodeList.setModel(new DefaultComboBoxModel<>(nodeData));
            }
        });
        panel.add(orbitSorted, BorderLayout.CENTER);
*/
        graphletListSorted = new JComboBox<String>(graphDataSorted);
        graphletListSorted.setBounds(posX + 130, posY + 160, 250, 20);
        graphletListSorted.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
                int selectedOrbit = orbit.getSelectedIndex();
                int selectedNode = nodeList.getSelectedIndex();
                int selectedGraphlet = comboBox.getSelectedIndex();
                ArrayList<String> data = new ArrayList<>();

                GraphletsCalculator.Struct graphletStructure = GraphletsCalculator.sortedGraphlets.get(selectedOrbit).get(selectedGraphlet);

                paintGraphlet(graphletStructure);
            }
        });
        panel.add(graphletListSorted, BorderLayout.EAST);

        graphletListUniq = new JComboBox<String>(graphDataUniq);
        graphletListUniq.setBounds(posX + 130, posY + 160, 250, 20);
        graphletListUniq.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                @SuppressWarnings("unchecked")
                JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
                int selectedGraphlet = comboBox.getSelectedIndex();
                GraphletsCalculator.Struct graphletStructure = GraphletsCalculator.uniqGraphlets.get(selectedGraphlet);
                paintGraphlet(graphletStructure);
            }
        });
        panel.add(graphletListUniq, BorderLayout.EAST);

        panel.add(generateButton, BorderLayout.WEST);


        //NetGenerator ng = new NetGenerator();
        JButton genNetsButton = new JButton("<html>Generate nets</html>");
        genNetsButton.setBounds(posX, posY, 120, 36);
        genNetsButton.setMargin(new Insets(0, 0, 0, 0));
        genNetsButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        genNetsButton.addActionListener(actionEvent ->
                new NetGenerator(Integer.parseInt(jtaImin.getText()), Integer.parseInt(jtaImax.getText()), Integer.parseInt(jtaJmin.getText()), Integer.parseInt(jtaJmax.getText()), Integer.parseInt(jtaPmin.getText()), Integer.parseInt(jtaPmax.getText())));
        genNetsButton.setFocusPainted(false);
        panel.add(genNetsButton);


        JButton compNets = new JButton("<html>Compare graphlets</html>");
        compNets.setBounds(posX, posY, 120, 36);
        compNets.setMargin(new Insets(0, 0, 0, 0));
        compNets.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        compNets.addActionListener(actionEvent -> compareNets());
        compNets.setFocusPainted(false);
        panel.add(compNets);

        JButton saveGDD = new JButton("<html>Get GDD</html>");
        saveGDD.setBounds(posX, posY, 120, 36);
        saveGDD.setMargin(new Insets(0, 0, 0, 0));
        saveGDD.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        saveGDD.addActionListener(actionEvent -> getAndSaveGdd());
        saveGDD.setFocusPainted(false);
        panel.add(saveGDD);

        JButton comp = new JButton("<html>Comp old nets</html>");
        comp.setBounds(posX, posY, 120, 36);
        comp.setMargin(new Insets(0, 0, 0, 0));
        comp.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        comp.addActionListener(actionEvent -> compOldNets());
        comp.setFocusPainted(false);
        panel.add(comp);

        JButton compGDDA = new JButton("<html>Compare GDDA for all nets</html>");
        compGDDA.setBounds(posX, posY, 120, 36);
        compGDDA.setMargin(new Insets(0, 0, 0, 0));
        compGDDA.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        compGDDA.addActionListener(actionEvent -> compareAllGDDA());
        compGDDA.setFocusPainted(false);
        panel.add(compGDDA);

        JButton compGDDAstar = new JButton("<html>Compare GDDA BASE-STAR</html>");
        compGDDAstar.setBounds(posX, posY, 120, 36);
        compGDDAstar.setMargin(new Insets(0, 0, 0, 0));
        compGDDAstar.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        compGDDAstar.addActionListener(actionEvent -> compareAllGDDAstar());
        compGDDAstar.setFocusPainted(false);
        panel.add(compGDDAstar);

        JButton compGDDALk = new JButton("<html>Compare GDDA BASE-CYCLE</html>");
        compGDDALk.setBounds(posX, posY, 120, 36);
        compGDDALk.setMargin(new Insets(0, 0, 0, 0));
        compGDDALk.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        compGDDALk.addActionListener(actionEvent -> compareAllGDDALk());
        compGDDALk.setFocusPainted(false);
        panel.add(compGDDALk);


        JButton compGDDAcycle = new JButton("<html>Compare GDDA BASE-CYCLE</html>");
        compGDDAcycle.setBounds(posX, posY, 120, 36);
        compGDDAcycle.setMargin(new Insets(0, 0, 0, 0));
        compGDDAcycle.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        compGDDAcycle.addActionListener(actionEvent -> compareAllGDDAcycle());
        compGDDAcycle.setFocusPainted(false);
        panel.add(compGDDAcycle);


        JButton nnn = new JButton("<html>Compare GDDA with all variants</html>");
        nnn.setBounds(posX, posY, 120, 36);
        nnn.setMargin(new Insets(0, 0, 0, 0));
        nnn.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        nnn.addActionListener(actionEvent -> compareALLVariantsGDDA());
        nnn.setFocusPainted(false);
        panel.add(nnn);


        JButton test = new JButton("<html>Test Button/html>");
        test.setBounds(posX, posY, 120, 36);
        test.setMargin(new Insets(0, 0, 0, 0));
        test.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        test.addActionListener(actionEvent -> collectGDDAFromFiles());
        test.setFocusPainted(false);
        panel.add(test);

        return panel;
    }

    private void compareALLVariantsGDDA() {
        //NetGenerator ng = new NetGenerator(true);

        //compareAllGDDA();
        compareGDDA("BASE", "P3VARIANT");
        compareGDDA("BASE", "C6VARIANT");
        compareGDDA("BASE", "E2VARIANT");
        compareGDDA("BASE", "K4LkVARIANT");
        compareGDDA("BASE", "K4LVARIANT");
        compareGDDA("BASE", "S4VARIANT");
        compareGDDA("BASE", "SS4VARIANT");
        compareGDDA("BASE", "SSS4VARIANT");
        compareGDDA("BASE", "ALLVARIANT");

        compareGDDA("K4LkVARIANT", "K4LkVARIANT");

        compareGDDA("S4VARIANT", "SS4VARIANT");
        compareGDDA("S4VARIANT", "SSS4VARIANT");
        compareGDDA("SS4VARIANT", "SSS4VARIANT");

        compareGDDA("P3VARIANT", "C6VARIANT");
        compareGDDA("P3VARIANT", "E2VARIANT");
        compareGDDA("P3VARIANT", "K4LkVARIANT");
        compareGDDA("P3VARIANT", "K4LVARIANT");

        compareGDDA("C6VARIANT", "E2VARIANT");
        compareGDDA("C6VARIANT", "K4LkVARIANT");
        compareGDDA("C6VARIANT", "K4LVARIANT");

        compareGDDA("E2VARIANT", "K4LkVARIANT");
        compareGDDA("E2VARIANT", "K4LVARIANT");
    }

    private void collectGDDAFromFiles(){
/*
        collectGDDAFromFiles("BASE-SS4VARIANT4");
        collectGDDAFromFiles("BASE-SS4VARIANT5");


        collectGDDAFromFiles("BASE-SSS4VARIANT4");
        collectGDDAFromFiles("BASE-SSS4VARIANT5");
*/

        collectGDDAFromFiles("BASE-ALLVARIANT4");
        collectGDDAFromFiles("BASE-ALLVARIANT5");
        /*
        collectGDDAFromFiles("BASE-C6VARIANT4");
        collectGDDAFromFiles("BASE-C6VARIANT5");

        collectGDDAFromFiles("BASE-S4VARIANT4");
        collectGDDAFromFiles("BASE-S4VARIANT5");

        collectGDDAFromFiles("BASE-P3VARIANT4");
        collectGDDAFromFiles("BASE-P3VARIANT5");

        collectGDDAFromFiles("BASE-E2VARIANT4");
        collectGDDAFromFiles("BASE-E2VARIANT5");

        collectGDDAFromFiles("BASE-K4LVARIANT4");
        collectGDDAFromFiles("BASE-K4LVARIANT5");

        collectGDDAFromFiles("BASE-K4LkVARIANT4");
        collectGDDAFromFiles("BASE-K4LkVARIANT5");
        */
    }



    private void collectGDDAFromFiles(String name) {
        String sciezka = "/home/Szavislav/Eksperyment/Wyniki/";

        Double[][] minTab = new Double[50][50];
        Double[][] maxTab = new Double[50][50];
        Double[][] avarage = new Double[50][50];
        Double[][] ravarage = new Double[50][50];

        DataInputStream in = null;
        try {
            in = new DataInputStream(new FileInputStream(sciezka + name+".txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
        String wczytanaLinia = null;
        try {
            int i = -1;
            int j = -1;
            double min = -1;
            double max = -1;
            double avg = -1;
            double ravg = -1;

            while ((wczytanaLinia = buffer.readLine()) != null) {

                if (wczytanaLinia.contains("==")) {
                    wczytanaLinia = wczytanaLinia.replace("==", "");
                    String[] line = wczytanaLinia.split("-");
                    i = Integer.parseInt(line[0]);
                    j = Integer.parseInt(line[1]);
                }

                if (wczytanaLinia.contains("min")) {
                    String[] line = wczytanaLinia.split("\t");
                    min = Double.parseDouble(line[1]);
                }
                if (wczytanaLinia.contains("max")) {
                    String[] line = wczytanaLinia.split("\t");
                    max = Double.parseDouble(line[1]);
                }
                if (wczytanaLinia.contains("avarage") && !wczytanaLinia.contains("reduced")) {
                    String[] line = wczytanaLinia.split("\t");
                    avg = Double.parseDouble(line[1]);
                }
                if (wczytanaLinia.contains("reduced")) {
                    String[] line = wczytanaLinia.split("\t");
                    ravg = Double.parseDouble(line[1]);

                    if (i < 45 && j < 45) {
                        minTab[i][j] = min;
                        maxTab[i][j] = max;
                        avarage[i][j] = avg;
                        ravarage[i][j] = ravg;
                    }
                    min = -1;
                    max = -1;
                    avg = -1;
                    ravg = -1;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            writeToCSV(minTab, maxTab, avarage, ravarage, sciezka + name+".csv");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeToCSV(Double[][] minmatrix, Double[][] maxmatrix, Double[][] avgmatrix, Double[][] ravgmatrix, String path) throws IOException {
        FileWriter writer = new FileWriter(path);

        int x = 0;
        int y = 0;
        for (int i = 0; i < minmatrix.length; i = i + 5) {

            for (int j = 0; j < minmatrix[0].length; j = j + 5) {
                writer.append(String.valueOf(minmatrix[i][j]));
                if (j != minmatrix[0].length - 1) {
                    writer.append(",");
                }
                x++;
            }
            y++;
            writer.append("\n");
        }
        writer.append("\n");

        x = 0;
        y = 0;
        for (int i = 0; i < maxmatrix.length; i = i + 5) {

            for (int j = 0; j < maxmatrix[0].length; j = j + 5) {
                writer.append(String.valueOf(maxmatrix[i][j]));
                if (j != maxmatrix[0].length - 1) {
                    writer.append(",");
                }
                x++;
            }
            y++;
            writer.append("\n");
        }
        writer.append("\n");

        x = 0;
        y = 0;
        for (int i = 0; i < avgmatrix.length; i = i + 5) {

            for (int j = 0; j < avgmatrix[0].length; j = j + 5) {
                writer.append(String.valueOf(avgmatrix[i][j]));
                if (j != avgmatrix[0].length - 1) {
                    writer.append(",");
                }
                x++;
            }
            y++;
            writer.append("\n");
        }
        writer.append("\n");

        x = 0;
        y = 0;
        for (int i = 0; i < ravgmatrix.length; i = i + 5) {

            for (int j = 0; j < ravgmatrix[0].length; j = j + 5) {
                writer.append(String.valueOf(ravgmatrix[i][j]));
                if (j != ravgmatrix[0].length - 1) {
                    writer.append(",");
                }
                x++;
            }
            y++;
            writer.append("\n");
        }
        writer.append("\n");

        writer.close();
    }


    private void compareGDDA(String first, String second) {
        System.out.println(first + " - " + second);
        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 45; i++) {
            for (int j = 0; j < 45; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 0; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, p1, first), gc98.getPath(i, j, p1, second));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, p1, first), gc600.getPath(i, j, p1, second));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/" + first + "-" + second + "4"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/" + first + "-" + second + "4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/" + first + "-" + second + "5"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/" + first + "-" + second + "5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void compareAllGDDAP() {
        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 0; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, p1, "BASE"), gc98.getPath(i, j, p1, "P3VARIANT"));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, p1, "BASE"), gc600.getPath(i, j, p1, "P3VARIANT"));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-P-4.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-P-4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-P-5.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-P-5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void compareAllGDDAE() {
        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 0; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, p1, "BASE"), gc98.getPath(i, j, p1, "E2VARIANT"));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, p1, "BASE"), gc600.getPath(i, j, p1, "E2VARIANT"));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-E-4.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-E-4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-E-5.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-E-5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void compareAllGDDAL() {
        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 0; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, p1, "BASE"), gc98.getPath(i, j, p1, "K4LVARIANT"));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, p1, "BASE"), gc600.getPath(i, j, p1, "K4LVARIANT"));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-L-4.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-L-4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-L-5.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-L-5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    private void compareAllGDDALk() {
        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 0; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, p1, "BASE"), gc98.getPath(i, j, p1, "K4LkVARIANT"));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, p1, "BASE"), gc600.getPath(i, j, p1, "K4LkVARIANT"));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-Lk-4.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-Lk-4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-Lk-5.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-Lk-5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    private void compareAllGDDAcycle() {
        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 0; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, p1, "BASE"), gc98.getPath(i, j, p1, "C6VARIANT"));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, p1, "BASE"), gc600.getPath(i, j, p1, "C6VARIANT"));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-CYCLE-4.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-CYCLE-4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-CYCLE-5.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-CYCLE-5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private void compareAllGDDAstar() {


        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 0; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, p1, "BASE"), gc98.getPath(i, j, p1, "S4VARIANT"));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, p1, "BASE"), gc600.getPath(i, j, p1, "S4VARIANT"));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-STAR-4.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-STAR-4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-STAR-5.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-STAR-5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    private void compareAllGDDA() {


        GraphletComparator gc98 = new GraphletComparator(98);
        GraphletComparator gc600 = new GraphletComparator(600);
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 100; j++) {
                if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/"))) {

                    double[] resultForGDDA4 = new double[100];
                    double[] resultForGDDA5 = new double[100];
                    for (int p1 = 1; p1 < 100; p1++) {

                        if (Files.exists(Paths.get("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/i" + i + "j" + j + "p" + p1 + "/"))) {
                            resultForGDDA4[p1] = gc98.calcDGDDA(gc98.getPath(i, j, 0, "BASE"), gc98.getPath(i, j, p1, "BASE"));
                            resultForGDDA5[p1] = gc600.calcDGDDA(gc600.getPath(i, j, 0, "BASE"), gc600.getPath(i, j, p1, "BASE"));
                        } else {
                            resultForGDDA4[p1] = -1;
                        }
                    }

                    //write for 4
                    String toSave4 = createBlockToSave(resultForGDDA4, i, j);
                    BufferedWriter fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-BASE-4.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    BufferedWriter totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-BASE-4.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave4);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    //Write for 5

                    String toSave5 = createBlockToSave(resultForGDDA5, i, j);
                    fileWriter = null;
                    try {
                        fileWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/i" + i + "j" + j + "/BASE-BASE-5.txt"));

                        PrintWriter printWriter = new PrintWriter(fileWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    totalResultWriter = null;
                    try {
                        totalResultWriter = new BufferedWriter(new FileWriter("/home/Szavislav/Eksperyment/Wyniki/BASE-BASE-5.txt", true));

                        PrintWriter printWriter = new PrintWriter(totalResultWriter);
                        printWriter.append(toSave5);
                        printWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private String createBlockToSave(double[] resultForGDDA, int i, int j) {
        String result = "";

        result += "==" + i + "-" + j + "==\n\r";

        double min = Arrays.stream(resultForGDDA).filter(x -> x > 0).min().getAsDouble();
        int minID = IntStream.range(0, resultForGDDA.length)
                .filter(x -> min == resultForGDDA[x])
                .findFirst()
                .orElse(-1);

        double max = Arrays.stream(resultForGDDA).max().getAsDouble();
        int maxID = IntStream.range(0, resultForGDDA.length)
                .filter(x -> max == resultForGDDA[x])
                .findFirst()
                .orElse(-1);

        double avarage = Arrays.stream(resultForGDDA).sum() / resultForGDDA.length;


        ArrayList<Double> reduced = new ArrayList<>();
        ArrayList<Double> toWrite = new ArrayList<>();
        boolean minThrown = false;
        boolean maxThrown = false;
        for (int k = 0; k < resultForGDDA.length; k++) {
            if (resultForGDDA[k] == min && !minThrown) {
                minThrown = true;
            } else if (resultForGDDA[k] == max && !maxThrown) {
                maxThrown = true;
            } else {
                reduced.add(resultForGDDA[k]);
            }
            toWrite.add(resultForGDDA[k]);
        }


        double redAvarage = reduced.stream().mapToDouble(f -> f.doubleValue()).sum() / reduced.size();

        result += "min\t" + min + "\t net: " + minID + "\n";

        result += "max\t" + max + "\t net: " + maxID + "\n";

        result += "avarage\t" + avarage + "\n";

        result += "reduced avarage\t" + redAvarage + "\n";

        result += "tab\n";

        result += toWrite + "\n";

        return result;
    }

    private void compOldNets() {

        JFileChooser jFileChooser = new JFileChooser();
        int returnVal = jFileChooser.showSaveDialog(this);

        String path = jFileChooser.getSelectedFile().getPath();

        JFileChooser jFileChooser2 = new JFileChooser();
        int returnVal2 = jFileChooser2.showSaveDialog(this);

        String path2 = jFileChooser2.getSelectedFile().getPath();

        GraphletComparator gc = new GraphletComparator(98);

        //System.out.println("GDDA dla covida = > " + gc.calcDGDDA(path, path2));
    }

    private void getAndSaveGdd() {
        JFileChooser jFileChooser = new JFileChooser();
        int returnVal = jFileChooser.showSaveDialog(this);

        String path = jFileChooser.getSelectedFile().getPath();

        ArrayList<int[]> DGDV = new ArrayList<>();

        GraphletsCalculator.generateGraphlets();

        for (Node startNode : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes()) {
            int[] vectorOrbit = GraphletsCalculator.vectorOrbit(startNode, false);
            DGDV.add(vectorOrbit);
        }

        //System.out.println("PATH -> " + path);
        NetGenerator.writeDGDDA(path, DGDV);
    }


    private void compareNets() {
        GraphletComparator gc = new GraphletComparator(98);
        gc.compare();
    }

    private void paintGraphlet(GraphletsCalculator.Struct graphletStructure) {
        ColorPalette cp = new ColorPalette();
        overlord.getWorkspace().getProject().resetNetColors();

        for (Node element : graphletStructure.getNodeMap().values()) {

            if (element.getType().equals(PetriNetElement.PetriNetElementType.PLACE)) {
                ((Place) element).setColorWithNumber(true, Color.red, false, 0, false, "");
            } else {
                ((Transition) element).setColorWithNumber(true, Color.blue, false, 0, true, "");
            }
        }


        overlord.getWorkspace().getProject().repaintAllGraphPanels();

        for (Arc element : graphletStructure.getArcMap().values()) {
            element.setColor(true, Color.green);
        }
    }

    private void generateGraphletOrbits() {
        GraphletsCalculator.generateGraphlets();
        GraphletsCalculator.getFoundGraphlets();


        for(int j = 0 ; j < GraphletsCalculator.graphlets.get(0).size();j++)
        {
        for (int i = 0; i < GraphletsCalculator.graphlets.size(); i++) {
                System.out.print(GraphletsCalculator.graphlets.get(i).get(j).size() + ",");
            }
            System.out.println();
        }

        ArrayList<String> data = new ArrayList<>();
        for (int i = 0; i < GraphletsCalculator.graphlets.size(); i++) {
            data.add("Orbita : " + i);
        }
        orbitData = data.stream().toArray(String[]::new);

        //orbitDataSorted =
        orbit.setModel(new DefaultComboBoxModel<>(orbitData));

        ArrayList<String> dataG = new ArrayList<>();
        for (int i = 0; i < GraphletsCalculator.uniqGraphlets.size(); i++) {
            dataG.add("Graphlet " + GraphletsCalculator.uniqGraphlets.get(i).getGraphletID() + " : " + i);
        }

        graphDataUniq = dataG.stream().toArray(String[]::new);

        graphletListUniq.setModel(new DefaultComboBoxModel<>(graphDataUniq));
    }


}
