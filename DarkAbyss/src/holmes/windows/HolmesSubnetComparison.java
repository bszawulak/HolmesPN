package holmes.windows;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.SubnetCalculator;
import holmes.analyse.comparison.Hungarian;
import holmes.analyse.comparison.SubnetComparator;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class HolmesSubnetComparison extends JFrame {
    PetriNet secondNet = null;
    ArrayList<SubnetCalculator.SubNet> seconNetList;
    SubnetComparator sc;
    String resultFlag = "Hungarian";
    JTextArea infoPane = new JTextArea();

    Boolean firstQuestionB = false;
    Boolean secondQuestionB = false;
    Boolean thirdQuestionB = false;
    ArrayList<JTable> listOfTables = new ArrayList<>();
    JButton generate;
    private boolean indexQuestionB = false;
    JRadioButton hungarianButton;
    JTabbedPane tabbedPane;
    int coloringMode = 0;

    ArrayList<ArrayList<ArrayList<GreatCommonSubnet>>> listOfTableContent = new ArrayList<>();


    public HolmesSubnetComparison() {
        setTitle("ADT subnet comparison");
        setSize(950, 300);
        JPanel csop = createStartOptionsPanel();
        getContentPane().add(csop, BorderLayout.NORTH);


        JScrollPane jsp = new JScrollPane(infoPane);
        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Info Panel");
        jsp.setBorder(titleF);
        getContentPane().add(jsp, BorderLayout.CENTER);
        //JPanel result = createResultsPanel(new ArrayList<>());
        //getContentPane().add(result, BorderLayout.SOUTH);
    }

    private JPanel createStartOptionsPanel() {
        JPanel jp = new JPanel();

        JPanel buttonPanel = new JPanel(new GridLayout(0, 1));

        JButton chooser = new JButton("Choose second net");
        chooser.setVisible(true);
        chooser.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();

            int returnVal = jfc.showOpenDialog(HolmesSubnetComparison.this);
            infoPane.append("Choosen file: " + jfc.getSelectedFile().getName() + "\n");
            chooseSecondNet(jfc.getSelectedFile().getAbsolutePath());
            if(returnVal==JFileChooser.APPROVE_OPTION)
            {
                generate.setEnabled(true);
            }

        });
        buttonPanel.add(chooser);

        generate = new JButton("Compare nets");
        generate.addActionListener(e -> compare());
        generate.setEnabled(false);
        buttonPanel.add(generate);
        jp.add(buttonPanel);

        JPanel firstQuestion = new JPanel(new GridLayout(0, 1));


        JRadioButton maxButton = new JRadioButton("Max common path");
        maxButton.setActionCommand("");
        maxButton.setSelected(true);
        maxButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (maxButton.isSelected()) {
                    firstQuestionB = false;
                }
            }
        });
        firstQuestion.add(maxButton);

        JRadioButton minButton = new JRadioButton("Min common path");
        minButton.setActionCommand("");
        minButton.addActionListener(e -> {
            if (minButton.isSelected()) {
                firstQuestionB = true;
            }
        });
        firstQuestion.add(minButton);


        ButtonGroup groupQ1 = new ButtonGroup();
        groupQ1.add(minButton);
        groupQ1.add(maxButton);

        TitledBorder titleF;
        titleF = BorderFactory.createTitledBorder("Path size");
        firstQuestion.setBorder(titleF);

        jp.add(firstQuestion);

        if (GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix() == null || GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().isEmpty()) {
            infoPane.append("Generate inwariants for net in workspace.\n");
        }
        if (SubnetCalculator.adtSubNets == null || SubnetCalculator.adtSubNets.isEmpty()) {
            infoPane.append("Generate ADT subnets for net in workspace.\n");
        }


        JPanel secondQuestion = new JPanel(new GridLayout(0, 1));

        JRadioButton ttButton = new JRadioButton("Same type branches comparison");
        ttButton.setActionCommand("");
        ttButton.setSelected(true);
        ttButton.addActionListener(e -> {
            if (ttButton.isSelected()) {
                secondQuestionB = false;
            }
        });
        secondQuestion.add(ttButton);

        JRadioButton tpButton = new JRadioButton("Mix type branches comparison");
        tpButton.setActionCommand("");
        tpButton.addActionListener(e -> {
            if (tpButton.isSelected()) {
                secondQuestionB = true;
            }
        });
        secondQuestion.add(tpButton);

        ButtonGroup groupQ2 = new ButtonGroup();
        groupQ2.add(ttButton);
        groupQ2.add(tpButton);

        TitledBorder titleS;
        titleS = BorderFactory.createTitledBorder("Branch restriction");
        secondQuestion.setBorder(titleS);
        jp.add(secondQuestion);


        JPanel thirdQuestion = new JPanel(new GridLayout(0, 1));
        JRadioButton loButton = new JRadioButton("With loops");
        loButton.setActionCommand("");
        loButton.addActionListener(e -> {
            if (loButton.isSelected()) {
                thirdQuestionB = false;
            }
        });
        loButton.setSelected(true);

        thirdQuestion.add(loButton);

        JRadioButton nloButton = new JRadioButton("Without loops");
        nloButton.setActionCommand("");
        nloButton.addActionListener(e -> {
            if (nloButton.isSelected()) {
                thirdQuestionB = true;
            }
        });
        thirdQuestion.add(nloButton);

        ButtonGroup groupQ3 = new ButtonGroup();
        groupQ3.add(loButton);
        groupQ3.add(nloButton);

        TitledBorder titleT;
        titleT = BorderFactory.createTitledBorder("Loop restriction");
        thirdQuestion.setBorder(titleT);
        jp.add(thirdQuestion);

        //----
        JPanel indexQuestion = new JPanel(new GridLayout(0, 1));
        JRadioButton jacButton = new JRadioButton("Jackard index");
        jacButton.setActionCommand("");
        jacButton.addActionListener(e -> {
            if (jacButton.isSelected()) {
                indexQuestionB = false;
            }
        });
        jacButton.setSelected(true);

        indexQuestion.add(jacButton);

        JRadioButton sorButton = new JRadioButton("Sørensen index");
        sorButton.setActionCommand("");
        sorButton.addActionListener(e -> {
            if (sorButton.isSelected()) {
                indexQuestionB = true;
            }
        });
        indexQuestion.add(sorButton);

        ButtonGroup groupQ4 = new ButtonGroup();
        groupQ4.add(jacButton);
        groupQ4.add(sorButton);

        TitledBorder titleIn;
        titleIn = BorderFactory.createTitledBorder("Index");
        indexQuestion.setBorder(titleIn);
        jp.add(indexQuestion);

        return jp;
    }

    private void compare() {

        if (SubnetCalculator.adtSubNets == null || SubnetCalculator.adtSubNets.isEmpty()) {
            JOptionPane jpo = new JOptionPane("No ADT sets!");
            GUIManager.getDefaultGUIManager().showDecoWindow();
        } else {
            listOfTables.clear();
            InvariantsCalculator ic = new InvariantsCalculator(secondNet);
            ic.generateInvariantsForTest(secondNet);

            infoPane.append("Second net: Invariants generated.\n");

            secondNet.setT_InvMatrix(ic.getInvariants(true), false);
            seconNetList = SubnetCalculator.generateADTFromSecondNet(secondNet);

            infoPane.append("Second net: ADT generated.\n");

            sc = new SubnetComparator(SubnetCalculator.adtSubNets, seconNetList);
            //rrayList<ArrayList<GreatCommonSubnet>> listofComparedSubnets = sc.compare();

            infoPane.append("Start comparison...\n");
            JComponent result = createResultsPanel();//stofComparedSubnets);//createPartResultTable(listofComparedSubnets);// createResultsPanel(listofComparedSubnets);
            getContentPane().add(result, BorderLayout.PAGE_END);
            this.revalidate();
        }
    }

    private void chooseSecondNet(String absolutePath) {
        IOprotocols io = new IOprotocols();
        secondNet = io.serverReadPNT(absolutePath, 99);

        //DecoComparisonCalculator dcc = new DecoComparisonCalculator();
        //dcc.calculate();
    }

    private JPanel createResultsPanel() {//ArrayList<ArrayList<GreatCommonSubnet>> gscl) {
        JPanel jp = new JPanel();
        //

        JPanel leftPanel = new JPanel();
        tabbedPane = new JTabbedPane();
        tabbedPane.setSize(200,500);

        this.setSize(950, 800);

        sc.firstQuestion = firstQuestionB;
        sc.secondQuestion = secondQuestionB;
        sc.thirdQuestion = thirdQuestionB;

        infoPane.append("Compare first net to second net.\n");
        JComponent panelFF = createPartResultTable(sc.compareFirstSecond(),false);//,gscl.size(),gscl.get(0).size());
        tabbedPane.addTab("First net to second net", null, panelFF, "Does nothing");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        infoPane.append("Compare second net to first net.\n");
        JComponent panelSS = createPartResultTable(sc.compareSecondFirst(),false);//,gscl.get(0).size(),gscl.size());
        tabbedPane.addTab("Second net to first net", null, panelSS, "Does twice as much nothing");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        infoPane.append("Compare first net internally.\n");
        JComponent panelFS = createPartResultTable(sc.compareInternalFirst(),true);//,gscl.size(),gscl.size());
        tabbedPane.addTab("Internal similarity of First net", null, panelFS, "Still does nothing");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3);

        infoPane.append("Compare second net internally.\n");
        JComponent panelSF = createPartResultTable(sc.compareInternalSecond(),true);//,gscl.get(0).size(),gscl.get(0).size());
        tabbedPane.addTab("Internal similarity of Second net", null, panelSF, "Does nothing at all");
        tabbedPane.setMnemonicAt(3, KeyEvent.VK_4);



        infoPane.append("Comparison finished.\n");

        leftPanel.add(tabbedPane);

        jp.add(leftPanel, BorderLayout.PAGE_END);
        // jp.add(new JSeparator(JSeparator.VERTICAL), BorderLayout.LINE_START);

        //

        JPanel rightPanel = new JPanel(new GridLayout(0, 1));

        hungarianButton = new JRadioButton("Hungarian method");
        hungarianButton.setMnemonic(KeyEvent.VK_B);
        hungarianButton.addActionListener(e -> {
            if (hungarianButton.isSelected()) {
                coloringMode=2;
                if(tabbedPane.getSelectedIndex()<2) {
                    int[] hungarianCels = calcHungarianCels(listOfTableContent.get(tabbedPane.getSelectedIndex()));
                    colorAllHungarianCel(hungarianCels, listOfTableContent.get(tabbedPane.getSelectedIndex()));
                }
            }
        });
        hungarianButton.setActionCommand(resultFlag);
        //hungarianButton.setActionCommand(birdString);

        //JRadioButton maxButton = new JRadioButton("Max method");
        //maxButton.setMnemonic(KeyEvent.VK_C);
        //maxButton.setActionCommand(resultFlag);
        //maxButton.setActionCommand(catString);

        JRadioButton singleHandButton = new JRadioButton("Color single matching");
        singleHandButton.setMnemonic(KeyEvent.VK_D);
        singleHandButton.setSelected(true);
        singleHandButton.addActionListener(e -> {
            if (singleHandButton.isSelected()) {
                coloringMode = 0;
            }
        });
        //dogButton.setActionCommand(dogString);

        JRadioButton handButton = new JRadioButton("Color choosen matching");
        handButton.setMnemonic(KeyEvent.VK_D);
        handButton.addActionListener(e -> {
            if (handButton.isSelected()) {
                coloringMode = 1;
            }
        });
        //dogButton.setActionCommand(dogString);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(singleHandButton);
        //group.add(maxButton);
        group.add(handButton);
        group.add(hungarianButton);

        rightPanel.add(singleHandButton);
        rightPanel.add(handButton);
        rightPanel.add(hungarianButton);
        //rightPanel.add(maxButton);

        TitledBorder title;
        title = BorderFactory.createTitledBorder("");
        rightPanel.setBorder(title);
        jp.add(rightPanel);

        JButton saveButton = new JButton("Save to .csv");
        saveButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            int returnVal = fc.showOpenDialog(HolmesSubnetComparison.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                exportToCSV(listOfTables.get(0),fc.getSelectedFile().getPath()+"1-2.csv");
                exportToCSV(listOfTables.get(1),fc.getSelectedFile().getPath()+"2-1.csv");
                exportToCSV(listOfTables.get(2),fc.getSelectedFile().getPath()+"1-1.csv");
                exportToCSV(listOfTables.get(3),fc.getSelectedFile().getPath()+"2-2.csv");
                infoPane.append("csv files saved!\n");
            }
        });

        rightPanel.add(saveButton);

        jp.add(rightPanel,BorderLayout.EAST);

        return jp;
    }

    public static boolean exportToCSV(JTable tableToExport,
                                      String pathToExportTo) {
        try {

            TableModel model = tableToExport.getModel();
            FileWriter csv = new FileWriter(new File(pathToExportTo));

            for (int i = 0; i < model.getColumnCount(); i++) {
                csv.write(model.getColumnName(i) + ",");
            }

            csv.write("\n");

            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    csv.write(model.getValueAt(i, j).toString() + ",");
                }
                csv.write("\n");
            }

            csv.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ArrayList<ArrayList<GreatCommonSubnet>> cutGcs(ArrayList<ArrayList<GreatCommonSubnet>> gscl, int i, int i1, int size, int size1) {
        ArrayList<ArrayList<GreatCommonSubnet>> result = new ArrayList<>();
        for (int j = i; j < size; j++) {
            ArrayList<GreatCommonSubnet> al = new ArrayList<>();
            for (int k = i1; k < size1; k++) {
                al.add(gscl.get(j).get(k));
            }
            result.add(al);
        }
        System.out.println("Rozmiar " + result.size() + ": " + result.get(0).size());
        return result;
    }

    public JComponent createPartResultTable(ArrayList<ArrayList<GreatCommonSubnet>> gcls, boolean isInternal) {//, int x_size, int y_size) {
        listOfTableContent.add(gcls);
        JPanel jp = new JPanel();
        jp.setPreferredSize(new Dimension(800, 300));

        //ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList = gcls;
        String[] netLabelsFirst = new String[gcls.size()];
        String[] netLabelsSecond = new String[gcls.get(0).size()];

        //do poszerzenia dla 2 sieci
        for (int i = 0; i < gcls.size(); i++) {
            netLabelsFirst[i] = "SubF :" + i;
            System.out.println(netLabelsFirst[i]);
        }
        for (int i = 0; i < gcls.get(0).size(); i++) {
            netLabelsSecond[i] = "SubS :" + i;
            System.out.println(netLabelsSecond[i]);
        }

        ListModel lm = new AbstractListModel() {
            String headers[] = netLabelsFirst;

            public int getSize() {
                return headers.length;
            }

            public Object getElementAt(int index) {
                return headers[index];
            }
        };

        String[][] results = new String[gcls.size()][gcls.get(0).size()];

        for (int i = 0; i < gcls.size(); i++) {
            for (int j = 0; j < gcls.get(i).size(); j++) {
                results[i][j] = gcls.get(i).get(j).gcsValue + "/" + gcls.get(i).get(j).firstNetNodeSize;
            }
        }

        int[] hungarianCels = calcHungarianCels(gcls);

        JTable comparisonTable = new JTable(results, netLabelsSecond) {//;
            //JTable table = new JTable(data, columnNames) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                Object value = getModel().getValueAt(row, col);
                //if (getSelectedRow() == row) {
                //colorIsomorphicCels(row, col, comp, subNetArrayList);
                //colorHungarianCels(row, col, comp, hungarianCels, gcls);
                if (isInternal) {
                    colorIsomorphicCels(row, col, comp, gcls);
                }
                else
                {
                    if(coloringMode==0)
                    {
                        //clean and single
                    }
                    if(coloringMode==1)
                    {
                        //multiple
                    }
                    if(coloringMode==2)
                    {
                        colorHungarianCels(row, col, comp,hungarianCels, gcls);
                    }
                }


                //pod przycisk
                //colorAllHungarianCel(row,col,comp,hungarianCels,gcls);

                return comp;
            }
        };
        comparisonTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JList rowHeader = new JList(lm);
        rowHeader.setFixedCellWidth(50);

        rowHeader.setFixedCellHeight(comparisonTable.getRowHeight());
        rowHeader.setCellRenderer(new RowHeaderRenderer(comparisonTable));

        comparisonTable.getSelectionModel().addListSelectionListener(event -> {

        });

        comparisonTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                GUIManager.getDefaultGUIManager().reset.clearGraphColors();
                int row = comparisonTable.rowAtPoint(evt.getPoint());
                int col = comparisonTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0 && coloringMode==0) {
                    colorHungarianCel(row,col,gcls);
                    TableCellEditor tce = comparisonTable.getCellEditor(row,col);
                    //tce.getTableCellEditorComponent(comparisonTable,)
                    //comparisonTable.getRowgetCellEditor(row,col);;
                }
            }
        });

        JScrollPane scroll = new JScrollPane(comparisonTable);
        scroll.setRowHeaderView(rowHeader);

        listOfTables.add(comparisonTable);

        return scroll;
    }

    public static String[][] transposeMatrix(String[][] m) {
        String[][] temp = new String[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    private void colorHungarianCels(int row, int col, Component comp, int[] hungarianCels, ArrayList<ArrayList<GreatCommonSubnet>> gcls) {
        if (row < hungarianCels.length)
            if (hungarianCels[row] == col) {
                comp.setBackground(Color.green);
                colorSubnet(gcls.get(row).get(col),SubnetCalculator.adtSubNets.get(row));
            } else {
                comp.setBackground(Color.white);
            }

    }

    private void colorHungarianCel(int row, int col, ArrayList<ArrayList<GreatCommonSubnet>> gcls) {
                colorSubnet(gcls.get(row).get(col),SubnetCalculator.adtSubNets.get(row));
    }

    private void colorAllHungarianCel(int[] hungarianCels, ArrayList<ArrayList<GreatCommonSubnet>> gcls) {

        for(int i = 0 ; i < gcls.size() ; i++) {
            for(int j = 0 ; j < gcls.get(i).size() ; j++) {
                if (i < hungarianCels.length)
                    if (hungarianCels[i] == j) {
                        //comp.setBackground(Color.green);
                        colorSubnetDensity(gcls.get(i).get(j), SubnetCalculator.adtSubNets.get(i), i, gcls.size());
                    } else {
                        //comp.setBackground(Color.white);
                    }
            }
        }
    }

    private void colorSubnet(GreatCommonSubnet gcs, SubnetCalculator.SubNet sn) {

        for (Node transition : sn.getSubNode()) {
            if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                ((Transition) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
            if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                ((Place) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
        }
        ArrayList<Arc> arcs = sn.getSubArcs();
        for (Arc arc : arcs) {
            arc.setColor(true, Color.red);
        }

        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Node transition : pse.partialNodes) {
                if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                    ((Transition) transition).setColorWithNumber(true, Color.GREEN, false, 0, true, "");
                if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                    ((Place) transition).setColorWithNumber(true, Color.GREEN, false, 0, true, "");
            }
            for (Arc arc : pse.partialArcs) {
                arc.setColor(true, Color.GREEN);
            }
        }


        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
    }

    private void colorSubnetDensity(GreatCommonSubnet gcs, SubnetCalculator.SubNet sn,int fr, int max) {

        Color randomColor = new Color(0,(int)(255*((double)fr/(double)max)), 0);

        for (Node transition : sn.getSubNode()) {
            if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                ((Transition) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
            if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                ((Place) transition).setColorWithNumber(true, Color.RED, false, 0, true, "");
        }
        ArrayList<Arc> arcs = sn.getSubArcs();
        for (Arc arc : arcs) {
            arc.setColor(true, Color.red);
        }

        for (SubnetComparator.PartialSubnetElements pse : gcs.psel) {
            for (Node transition : pse.partialNodes) {
                if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                    ((Transition) transition).setColorWithNumber(true, randomColor, false, 0, true, "");
                if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                    ((Place) transition).setColorWithNumber(true, randomColor, false, 0, true, "");
            }
            for (Arc arc : pse.partialArcs) {
                arc.setColor(true, randomColor);
            }
        }


        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
    }

    private int[] calcHungarianCels(ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList) {

        double[][] matrix = new double[subNetArrayList.size()][subNetArrayList.get(0).size()];

        for (int i = 0; i < subNetArrayList.size(); i++) {
            for (int j = 0; j < subNetArrayList.get(i).size(); j++) {
                matrix[i][j] = 1 - subNetArrayList.get(i).get(j).gcsValue;
            }
        }

        Hungarian h = new Hungarian(matrix);
        int[] result = h.execute();

        //TODO wybierz wspólne grafy o maxymalnych wartościach i zwróć je

        for (int i = 0; i < result.length; i++) {
            //ArrayList<Integer> row = subNetArrayList.get(i).get(result[i]).gcsValue;
            //create and add gcs


        }
        return result;
    }

    private void colorIsomorphicCels(int row, int col, Component comp, ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList) {
        if (subNetArrayList.get(row).get(col).gcsValue == subNetArrayList.get(row).get(col).firstNetNodeSize) {
            comp.setBackground(Color.green);
        } else if (subNetArrayList.get(row).get(col).gcsValue == 0) {
            comp.setBackground(Color.red);
        } else if (subNetArrayList.get(row).get(col).gcsValue > subNetArrayList.get(row).get(col).firstNetNodeSize ||
                subNetArrayList.get(row).get(col).gcsValue > subNetArrayList.get(row).get(col).secondNetNodeSize) {
            comp.setBackground(Color.blue);
        } else {
            comp.setBackground(Color.white);
        }
    }


    class RowHeaderRenderer extends JLabel implements ListCellRenderer {

        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

}
