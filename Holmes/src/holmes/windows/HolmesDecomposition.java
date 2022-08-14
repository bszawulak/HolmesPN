package holmes.windows;

import holmes.analyse.SubnetCalculator;
import holmes.analyse.comparison.DecoComparisonCalculator;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.Math.abs;

public class HolmesDecomposition extends JFrame {
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JPanel topButtonPanel;
    private JPanel logMainPanel;
    private JPanel infoPanel;

    //deco
    private ArrayList<Integer> choosenDeco = new ArrayList<>();
    private JTextArea elementsOfDecomposedStructure;
    private int selectedSubNetindex = -1;
    private int selectedSecondSubNetindex = -1;
    private boolean colorSubNet = false;
    private boolean allSubNetsselected = false;

    private GUIManager overlord;

    private ArrayList<String> firstListElementsNames = new ArrayList<>();
    private JList decoListOne;
    private JList decoListTwo;
    private JTextArea textArea;

    DefaultListModel listModel = new DefaultListModel();
    DefaultListModel listModelTwo = new DefaultListModel();

    boolean dualMode = false;
    boolean properDecoMode = true;
    private ArrayList<JCheckBox> listOfCheckBoxs=new ArrayList<>();

    //Similarities
    boolean showSimi = false;
    //numberOfPlacesOfFirstNet
    private int npfn = 0;
    private int npsn = 0;
    private int ntfn = 0;
    private int ntsn = 0;
    private int nafn = 0;
    private int nasn = 0;
    private int npcn = 0;
    private int ntcn = 0;
    private int nacn = 0;

    private ArrayList<Integer> listOfInvDep = new ArrayList<>(Arrays.asList(1, 3, 99, 10, 11, 12));

    private float sorenIndex = 0;
    private float jackobIndex = 0;
    private float smc = 0;
    //private float tverIndex =0;

    JLabel vpfn;
    JLabel vpsn;
    JLabel vpcn;
    JLabel vtfn;
    JLabel vtsn;
    JLabel vtcn;
    JLabel vafn;
    JLabel vasn;
    JLabel vacn;

    JLabel valueOfSorensenIndex;
    JLabel valueOfJackobsenIndex;
    JLabel valueOfSMC;
    //JLabel valueOfTverskyIndex;


    public HolmesDecomposition() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
        overlord = GUIManager.getDefaultGUIManager();
        setVisible(false);
        this.setTitle("Decomposition");

        setLayout(new BorderLayout());
        setSize(new Dimension(1100, 700));
        setLocation(15, 15);

        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        buttonPanel = createLeftButtonPanel(0, 0, 300, 230);
        logMainPanel = createGraphPanel(0, 130, 500, 300);
        topButtonPanel = createTopButtonPanel(200, 130, 900, 75);
        //topButtonPanel = createTopButtonPanel(200, 130, 900, 75);
        //infoPanel = createInfoPanel(400, 130, 200, 300);
        panel.add(buttonPanel, BorderLayout.WEST);
        panel.add(logMainPanel, BorderLayout.CENTER);
        //panel.add(infoPanel, BorderLayout.EAST);
        panel.add(topButtonPanel, BorderLayout.NORTH);
        panel.repaint();
        return panel;
    }

    private JPanel createLeftButtonPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(14,1));
        panel.setBorder(BorderFactory.createTitledBorder("Decomposition types"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));

        int posX = 10;
        int posY = 20;

        ItemListener itemListener = itemEvent -> {
            AbstractButton abstractButton = (AbstractButton)itemEvent.getSource();
            Color foreground = abstractButton.getForeground();
            Color background = abstractButton.getBackground();
            int state = itemEvent.getStateChange();
            if (state == ItemEvent.SELECTED || state == ItemEvent.DESELECTED) {
                abstractButton.setForeground(background);
                abstractButton.setBackground(foreground);
            }

        };

        JLabel prop = new JLabel("Disjoined Decompositions", SwingConstants.CENTER);
        panel.add(prop);

        JCheckBox functionalCheckBox = new JCheckBox();
        functionalCheckBox.setBounds(posX + 10, posY + 30, 150, 20);
        functionalCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, functionalCheckBox, panel, 0));
        functionalCheckBox.setSelected(false);
        functionalCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(functionalCheckBox,"Functional nets"));

        JCheckBox snetCheckBox = new JCheckBox();
        snetCheckBox.setBounds(posX + 10, posY + 60, 150, 20);
        snetCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, snetCheckBox, panel, 1));
        snetCheckBox.setSelected(false);
        snetCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(snetCheckBox,"S-net"));

        JCheckBox adtCheckBox = new JCheckBox();
        adtCheckBox.setBounds(posX + 10, posY + 90, 150, 20);
        adtCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, adtCheckBox, panel, 2));
        adtCheckBox.setSelected(false);
        adtCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(adtCheckBox,"T-net"));

        JCheckBox ssnetCheckBox = new JCheckBox();
        ssnetCheckBox.setBounds(posX + 10, posY + 120, 150, 20);
        ssnetCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, ssnetCheckBox, panel, 3));
        ssnetCheckBox.setSelected(false);
        ssnetCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(ssnetCheckBox,"connected ADT"));

        JCheckBox mctCheckBox = new JCheckBox();
        mctCheckBox.setBounds(posX + 10, posY + 150, 150, 20);
        mctCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, mctCheckBox, panel, 10));
        mctCheckBox.setSelected(false);
        mctCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(mctCheckBox,"maximal ADT (MCT)"));

        JCheckBox smcCheckBox = new JCheckBox();
        smcCheckBox.setBounds(posX + 10, posY + 330, 150, 20);
        smcCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, smcCheckBox, panel, 9));
        smcCheckBox.setSelected(false);
        smcCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(smcCheckBox,"State Machine Component (P1/S1-net)"));



        JLabel unprop = new JLabel("Other Decompositions", SwingConstants.CENTER);
        panel.add(unprop);


        JCheckBox tinvCheckBox = new JCheckBox();
        tinvCheckBox.setBounds(posX + 10, posY + 360, 150, 20);
        tinvCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, tinvCheckBox, panel, 11));
        tinvCheckBox.setSelected(false);
        tinvCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(tinvCheckBox,"T-component (t-inv.)"));

        JCheckBox pinvCheckBox = new JCheckBox();
        pinvCheckBox.setBounds(posX + 10, posY + 390, 150, 20);
        pinvCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, pinvCheckBox, panel, 12));
        pinvCheckBox.setSelected(false);
        pinvCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(pinvCheckBox,"S-component (p-inv.)"));

        JCheckBox tzCheckBox = new JCheckBox();
        tzCheckBox.setBounds(posX + 10, posY + 180, 150, 20);
        tzCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, tzCheckBox, panel, 4));
        tzCheckBox.setSelected(false);
        tzCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(tzCheckBox,"Teng-Zeng subnets"));

        JCheckBox houCheckBox = new JCheckBox();
        houCheckBox.setBounds(posX + 10, posY + 210, 150, 20);
        houCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, houCheckBox, panel, 5));
        houCheckBox.setSelected(false);
        houCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(houCheckBox,"All source-sink paths"));

        JCheckBox nishiCheckBox = new JCheckBox();
        nishiCheckBox.setBounds(posX + 10, posY + 240, 150, 20);
        nishiCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, nishiCheckBox, panel, 6));
        nishiCheckBox.setSelected(false);
        //panel.add(setOnPanel(nishiCheckBox,"Augmented Sequential Paths"));
        nishiCheckBox.setToolTipText("Augmented Sequential Paths");
        nishiCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(nishiCheckBox,"Augmented Sequential Paths"));

        JCheckBox cycleCheckBox = new JCheckBox();
        cycleCheckBox.setBounds(posX + 10, posY + 270, 150, 20);
        cycleCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, cycleCheckBox, panel, 7));
        cycleCheckBox.setSelected(false);
        cycleCheckBox.addItemListener(itemListener);
        panel.add(setOnPanel(cycleCheckBox,"Cycles"));



        JCheckBox bvCheckBox = new JCheckBox("BV");
        bvCheckBox.setBounds(posX + 10, posY + 420, 150, 20);
        bvCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, bvCheckBox, panel, 13));
        bvCheckBox.setSelected(false);
        //panel.add(bvCheckBox);

        JCheckBox btCheckBox = new JCheckBox("BT");
        btCheckBox.setBounds(posX + 10, posY + 450, 150, 20);
        btCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, btCheckBox, panel, 14));
        btCheckBox.setSelected(false);
        //panel.add(btCheckBox);

        JCheckBox bpCheckBox = new JCheckBox("BP");
        bpCheckBox.setBounds(posX + 10, posY + 480, 150, 20);
        bpCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, bpCheckBox, panel, 15));
        bpCheckBox.setSelected(false);
        //panel.add(bpCheckBox);

        JCheckBox ntzcCheckBox = new JCheckBox("Not SS cycle");
        ntzcCheckBox.setBounds(posX + 10, posY + 510, 150, 20);
        ntzcCheckBox.addActionListener(actionEvent -> checkBoxAction(actionEvent, ntzcCheckBox, panel, 16));
        ntzcCheckBox.setSelected(false);
        //panel.add(ntzcCheckBox);


        listOfCheckBoxs.add(functionalCheckBox);
        listOfCheckBoxs.add(snetCheckBox);
        listOfCheckBoxs.add(ssnetCheckBox);
        listOfCheckBoxs.add(adtCheckBox);
        listOfCheckBoxs.add(mctCheckBox);
        listOfCheckBoxs.add(tzCheckBox);
        listOfCheckBoxs.add(nishiCheckBox);
        listOfCheckBoxs.add(houCheckBox);
        listOfCheckBoxs.add(cycleCheckBox);
        listOfCheckBoxs.add(smcCheckBox);

        return panel;
    }

    private JPanel setOnPanel(JCheckBox functionalCheckBox,String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(text, JLabel.LEFT), BorderLayout.WEST);
        p.add(functionalCheckBox, BorderLayout.EAST);
        p.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        return p;
    }

    public void checkBoxAction(ActionEvent actionEvent, JCheckBox checkBox, JPanel panel, Integer type) {

        AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
        if (!dualMode) {
            for (JCheckBox box : listOfCheckBoxs) {
                if(!checkBox.equals(box))
                    box.setSelected(false);
            }
            choosenDeco.clear();
            choosenDeco.add(type);
        } else {
            if (checkBox.isSelected()) {
                choosenDeco.add(type);
            } else {
                choosenDeco.remove(type);
            }
            if (choosenDeco.size() > 2) {
                checkBox.setSelected(false);
                choosenDeco.remove(type);
            }
        }
    }

    private JPanel createInfoPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Info"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));

        int posX = 10;
        int posY = 10;

        GridBagConstraints c = new GridBagConstraints();

        ///////first subnet//////////
        JLabel fs1 = new JLabel("FIRST SUBNET");
        fs1.setLayout(new GridLayout());
        fs1.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 4;
        panel.add(fs1, c);

        JLabel fs2 = new JLabel("Number of arcs");
        fs2.setLayout(new GridLayout());
        fs2.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 5;
        panel.add(fs2, c);

        JLabel fs3 = new JLabel("Number of places");
        fs3.setLayout(new GridLayout());
        fs3.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 6;
        panel.add(fs3, c);

        JLabel fs4 = new JLabel("Number of transitions");
        fs4.setLayout(new GridLayout());
        fs4.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 7;
        panel.add(fs4, c);

        JSeparator separ = new JSeparator();
        separ.setLayout(new GridLayout());
        separ.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 8;
        panel.add(separ, c);
        ///////second subnet//////////

        JLabel ss1 = new JLabel("SECOND SUBNET");
        ss1.setLayout(new GridLayout());
        ss1.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 9;
        panel.add(ss1, c);

        JLabel ss2 = new JLabel("Number of arcs");
        ss2.setLayout(new GridLayout());
        ss2.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 10;
        panel.add(ss2, c);

        JLabel ss3 = new JLabel("Number of places");
        ss3.setLayout(new GridLayout());
        ss3.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 11;
        panel.add(ss3, c);

        JLabel ss4 = new JLabel("Number of transitions");
        ss4.setLayout(new GridLayout());
        ss4.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 12;
        panel.add(ss4, c);

        JSeparator separ2 = new JSeparator();
        separ2.setLayout(new GridLayout());
        separ2.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 13;
        panel.add(separ2, c);

        //////common subnet//////

        JLabel cs1 = new JLabel("COMMON SUBNET");
        cs1.setLayout(new GridLayout());
        cs1.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 14;
        panel.add(cs1, c);

        JLabel cs2 = new JLabel("Number of arcs");
        cs2.setLayout(new GridLayout());
        cs2.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 15;
        panel.add(cs2, c);

        JLabel cs3 = new JLabel("Number of places");
        cs3.setLayout(new GridLayout());
        cs3.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 16;
        panel.add(cs3, c);

        JLabel cs4 = new JLabel("Number of transitions");
        cs4.setLayout(new GridLayout());
        cs4.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 17;
        panel.add(cs4, c);

        JSeparator separ3 = new JSeparator();
        separ3.setLayout(new GridLayout());
        separ3.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 18;
        panel.add(separ3, c);
        ////////////////indexy

        JLabel in0 = new JLabel("SMC Index");
        in0.setLayout(new GridLayout());
        in0.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 19;
        panel.add(in0, c);

        JLabel in1 = new JLabel("Sørensen Index");
        in1.setLayout(new GridLayout());
        in1.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 20;
        panel.add(in1, c);

        JLabel in2 = new JLabel("Jaccard  Index");
        in2.setLayout(new GridLayout());
        in2.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 21;
        panel.add(in2, c);

        /////Values

        vafn = new JLabel(String.valueOf(nafn));
        vafn.setLayout(new GridLayout());
        vafn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 5;
        panel.add(vafn, c);

        vpfn = new JLabel(String.valueOf(npfn));
        vafn.setLayout(new GridLayout());
        vpfn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 6;
        panel.add(vpfn, c);

        vtfn = new JLabel(String.valueOf(ntfn));
        vtfn.setLayout(new GridLayout());
        vtfn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 7;
        panel.add(vtfn, c);

        vasn = new JLabel(String.valueOf(nasn));
        vasn.setLayout(new GridLayout());
        vasn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 10;
        panel.add(vasn, c);

        vpsn = new JLabel(String.valueOf(npsn));
        vpsn.setLayout(new GridLayout());
        vpsn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 11;
        panel.add(vpsn, c);

        vtsn = new JLabel(String.valueOf(ntsn));
        vtsn.setLayout(new GridLayout());
        vtsn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 12;
        panel.add(vtsn, c);

        vacn = new JLabel(String.valueOf(nacn));
        vacn.setLayout(new GridLayout());
        vacn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 15;
        panel.add(vacn, c);

        vpcn = new JLabel(String.valueOf(npcn));
        vpcn.setLayout(new GridLayout());
        vpcn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 16;
        panel.add(vpcn, c);

        vtcn = new JLabel(String.valueOf(ntcn));
        vtcn.setLayout(new GridLayout());
        vtcn.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 17;
        panel.add(vtcn, c);

        valueOfSMC = new JLabel(String.valueOf(smc));
        valueOfSMC.setLayout(new GridLayout());
        valueOfSMC.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 19;
        panel.add(valueOfSMC, c);

        valueOfSorensenIndex = new JLabel(String.valueOf(sorenIndex));
        valueOfSorensenIndex.setLayout(new GridLayout());
        valueOfSorensenIndex.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 20;
        panel.add(valueOfSorensenIndex, c);

        valueOfJackobsenIndex = new JLabel(String.valueOf(jackobIndex));
        valueOfJackobsenIndex.setLayout(new GridLayout());
        valueOfJackobsenIndex.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 21;
        panel.add(valueOfJackobsenIndex, c);

        /*
        valueOfTverskyIndex = new JLabel(String.valueOf(tverIndex));
        valueOfTverskyIndex.setLayout(new GridLayout());
        valueOfTverskyIndex.setEnabled(true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 2;
        c.gridy = 17;
        panel.add(valueOfTverskyIndex, c);
*/

        //////////////////////
        //button for copy

        return panel;
    }

    private JPanel createTopButtonPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel(new GridLayout(1,4));
        //panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("Options"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));

        int posX = 10;
        int posY = 20;

        JButton expDecoButton = new JButton("<html>Decompose</html>");
        expDecoButton.setBounds(posX, posY, 120, 36);
        expDecoButton.setMargin(new Insets(0, 0, 0, 0));
        expDecoButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        expDecoButton.addActionListener(actionEvent -> calculateDeco());
        expDecoButton.setFocusPainted(false);
        panel.add(expDecoButton);//,BorderLayout.LINE_START);

        JCheckBox dualCheckBox = new JCheckBox();
        dualCheckBox.setBounds(posX + 300, posY + 10, 250, 20);
        dualCheckBox.addActionListener(actionEvent -> setDual());
        dualCheckBox.setSelected(false);
        panel.add(setOnPanel(dualCheckBox,"Compare two decompositions"));
        //panel.add(dualCheckBox);//,BorderLayout.CENTER);


        JButton compButton = new JButton("Compare");//"<html>Decomposition\nDescriptions<html>");
        compButton.setBounds(posX + 620, posY, 120, 36);
        compButton.setMargin(new Insets(0, 0, 0, 0));
        //compButton.setIcon(Tools.getResIcon32("icons/stateSim/showNotepad.png"));
        compButton.addActionListener(actionEvent -> compareSubnets());
        compButton.setFocusPainted(false);
        compButton.setEnabled(true);
        panel.add(compButton);


        JButton infoButton = new JButton("Info");//"<html>Decomposition\nDescriptions<html>");
        infoButton.setBounds(posX + 750, posY, 120, 36);
        infoButton.setMargin(new Insets(0, 0, 0, 0));
        infoButton.setIcon(Tools.getResIcon32("icons/stateSim/showNotepad.png"));
        infoButton.addActionListener(actionEvent -> showInfo());
        infoButton.setFocusPainted(false);
        infoButton.setEnabled(true);
        //panel.add(infoButton);//,BorderLayout.LINE_END);

        //this.add(panel);
        return panel;
    }

    private JPanel createGraphPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("Subnets"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));
        JPanel listPanel = new JPanel();

        listModel.addElement("--");


        decoListOne = new JList(listModel);
        decoListOne.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        decoListOne.addListSelectionListener(evt -> {
                    JList<String> comboBox = (JList<String>) evt.getSource();
                    int selected = comboBox.getSelectedIndex();
                    int subnetsize = getSubnetSize(choosenDeco.get(0));
                    int cimboboxSize = comboBox.getModel().getSize();

                    if (subnetsize + 3 == cimboboxSize) {

                        if (selected == 0) {
                            selectedSubNetindex = -1;
                            allSubNetsselected = false;
                            showSubNet(choosenDeco.get(0));
                        } else if (selected == comboBox.getModel().getSize() - 1) {
                            allSubNetsselected = true;
                            showAllSubColors(true, choosenDeco.get(0));
                        } else if (selected == comboBox.getModel().getSize() - 2) {
                            allSubNetsselected = true;
                            showAllSubColors(false, choosenDeco.get(0));
                        } else {
                            if (dualMode) {
                                if (!decoListTwo.isSelectionEmpty())
                                    showTwoSubnets();
                            } else {
                                selectedSubNetindex = selected - 1;
                                allSubNetsselected = false;
                                showSubNet(choosenDeco.get(0));
                            }
                        }
                    } else {
                        if (selected == 0) {
                            selectedSubNetindex = -1;
                            allSubNetsselected = false;
                            showSubNet(choosenDeco.get(0));
                        } else if (selected == comboBox.getModel().getSize() - 1) {
                            allSubNetsselected = true;
                            showCoverageColors(choosenDeco.get(0));
                            //showAllSubColors(true, choosenDeco.get(0));
                        } else {
                            if (dualMode) {
                                if (!decoListTwo.isSelectionEmpty())
                                    showTwoSubnets();
                            } else {
                                selectedSubNetindex = selected - 1;
                                allSubNetsselected = false;
                                showSubNet(choosenDeco.get(0));
                            }
                        }
                    }
                }
        );
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        JScrollPane jsp = new JScrollPane(decoListOne);
        decoListOne.setLayoutOrientation(JList.VERTICAL);
        listPanel.setLayout(new GridBagLayout());
        listPanel.add(jsp, c);

        listModelTwo.addElement("--");
        decoListTwo = new JList(listModelTwo);
        decoListTwo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        decoListTwo.addListSelectionListener(evt -> {
                    JList<String> comboBox = (JList<String>) evt.getSource();
                    int selected = comboBox.getSelectedIndex();
                    int subnetsize = getSubnetSize(choosenDeco.size() - 1);
                    int cimboboxSize = comboBox.getModel().getSize();

                    if (subnetsize + 3 == cimboboxSize) {
                        if (selected == 0) {
                            selectedSecondSubNetindex = -1;
                            allSubNetsselected = false;
                            showSubNet(choosenDeco.get(choosenDeco.size() - 1));
                        } else if (selected == comboBox.getModel().getSize() - 1) {
                            allSubNetsselected = true;
                            showAllSubColors(true, choosenDeco.get(choosenDeco.size() - 1));
                        } else if (selected == comboBox.getModel().getSize() - 2) {
                            allSubNetsselected = true;
                            showAllSubColors(false, choosenDeco.get(choosenDeco.size() - 1));
                        } else {
                            if (!decoListOne.isSelectionEmpty()) {
                                showTwoSubnets();
                            }
                        }
                    } else {
                        if (selected == 0) {
                            selectedSubNetindex = -1;
                            allSubNetsselected = false;
                            showSubNet(choosenDeco.size() - 1);
                        } else if (selected == comboBox.getModel().getSize() - 1) {
                            allSubNetsselected = true;
                            showCoverageColors(choosenDeco.size() - 1);
                            //showAllSubColors(true, choosenDeco.get(0));
                        } else {
                            if (dualMode) {
                                if (!decoListTwo.isSelectionEmpty())
                                    showTwoSubnets();
                            } else {
                                selectedSubNetindex = selected - 1;
                                allSubNetsselected = false;
                                showSubNet(choosenDeco.size() - 1);
                            }
                        }
                    }
                }
        );
        decoListTwo.disable();
        decoListTwo.hide();
        decoListTwo.setForeground(Color.blue);
        panel.add(decoListTwo);

        JPanel listPanel2 = new JPanel();

        JScrollPane jsp2 = new JScrollPane(decoListTwo);

        jsp2.setViewportView(decoListTwo);
        decoListTwo.setLayoutOrientation(JList.VERTICAL);
        listPanel2.setLayout(new GridBagLayout());
        listPanel2.add(jsp2, c);
        listPanel2.setVisible(true);
        listPanel.setVisible(true);
        this.add(listPanel);
        this.add(listPanel2);


        panel.add(listPanel);
        panel.add(listPanel2);

        panel.setLayout(new GridLayout());
        return panel;
    }


    private void setDual() {

        if (!dualMode) {
            dualMode = true;
            decoListTwo.enable();
            decoListTwo.show();
            decoListOne.setForeground(Color.RED);
        } else {
            dualMode = false;
            decoListTwo.disable();
            decoListTwo.hide();
            decoListOne.setForeground(Color.BLACK);
            choosenDeco.clear();
            for(JCheckBox b : listOfCheckBoxs)
                b.setSelected(false);
        }
    }

    private void calculateDeco() {
        SubnetCalculator.cleanSubnets();
        getSubnetOfType();
    }

    private void calcWhat1() {
        ArrayList<SubnetCalculator.SubNet> deco = getCorrectSubnet(choosenDeco.get(0));
        ArrayList<int[][]> subMatrix = new ArrayList<>();
        for (SubnetCalculator.SubNet subnet : deco) {
            subMatrix.add(DecoComparisonCalculator.subIncMat(subnet).getMatrix());
        }

        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("decoMacierze.txt"));
            for (int i = 0; i < subMatrix.size(); i++) {
                bw.write("sub: " + i);
                bw.newLine();

                for (int j = 0; j < subMatrix.get(i).length; j++) {
                    for (int k = 0; k < subMatrix.get(i)[j].length; k++) {
                        if (k == subMatrix.get(i)[j].length - 1) {
                            bw.write(subMatrix.get(i)[j][k] + "");
                        } else {
                            bw.write(subMatrix.get(i)[j][k] + " ");
                        }
                    }
                    bw.newLine();
                }
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //DecoComparisonCalculator.vertexOrderAlgorithm(DecoComparisonCalculator.subIncMat(SubnetCalculator.adtSubNets.get(0)));
    }

    private void calcWhat2() {
        //SubnetCalculator.generateADT();
        SubnetCalculator.generateMCT();

        SubnetCalculator.SubNet subnet = SubnetCalculator.mctSubNets.get(11);
        int size = SubnetCalculator.mctSubNets.size();
        ColorPalette cp = new ColorPalette();
        //places


        for (int i = 0; i < SubnetCalculator.adtSubNets.size(); i++) {
            if (subnet.getSubPlaces().containsAll(SubnetCalculator.adtSubNets.get(i).getSubPlaces())) {
                Color cl = cp.getColor(i);
                System.out.println("dla i równego : " + i + " - B " + cl.getBlue() + " G " + cl.getGreen() + "R" + cl.getRed());
                for (Place place : SubnetCalculator.adtSubNets.get(i).getSubInternalPlaces()) {
                    place.drawGraphBoxP.setColorWithNumber(true, cp.getColor(i), false, 0, true, "[Sub net " + (i + 1) + "]");
                }
                for (Place place : SubnetCalculator.adtSubNets.get(i).getSubBorderPlaces()) {
                    place.drawGraphBoxP.setColorWithNumber(true, Color.lightGray, false, 0, true, "[Sub net " + (i + 1) + "]");
                }
            }
        }

        /*
        for (Place place : subnet.getSubPlaces()) {
            for (int i = 0; i < SubnetCalculator.adtSubNets.size(); i++) {
                if (subnet.getSubPlaces().containsAll(SubnetCalculator.adtSubNets.get(i).getSubPlaces()))
                    place.setColorWithNumber(true, cp.getColor(i), false, 0, true, "[Sub net " + (i + 1) + "]");
            }
        }
        */

        //arcs
        for (Arc arc : subnet.getSubArcs()) {
            if (!colorSubNet)
                arc.setGlowedSub(true);
        }

        //transitions

        for (int i = 0; i < SubnetCalculator.adtSubNets.size(); i++) {
            if (subnet.getSubTransitions().containsAll(SubnetCalculator.adtSubNets.get(i).getSubTransitions())) {
                Color cl = cp.getColor(i);
                System.out.println("dla i równego : " + i + " - B " + cl.getBlue() + " G " + cl.getGreen() + "R" + cl.getRed());
                for (Transition transition : SubnetCalculator.adtSubNets.get(i).getSubTransitions()) {
                    transition.drawGraphBoxT.setColorWithNumber(true, cl, false, 0, true, "[Sub net " + (i + 1) + "]");
                }
            }
        }

        overlord.getWorkspace().getProject().getNodes().retainAll(subnet.getSubNode());
        overlord.getWorkspace().getProject().getArcs().retainAll(subnet.getSubArcs());

        /*
        for (Transition transition : subnet.getSubTransitions()) {
        for (int i = 0; i < SubnetCalculator.adtSubNets.size(); i++) {

                if (subnet.getSubTransitions().containsAll(SubnetCalculator.adtSubNets.get(i).getSubTransitions()) && subnet.getSubTransitions().size() > 0 && SubnetCalculator.adtSubNets.get(i).getSubTransitions().size() > 0) {

                    Color cl = cp.getColor(i);
                    System.out.println("dla i równego : " + i + " - dla tanzycji " + transition.getID() + " - B " + cl.getBlue() + " G " + cl.getGreen() + "R" + cl.getRed());
                    transition.setColorWithNumber(true, cl, false, 0, true, "[Sub net " + (i + 1) + "]");
                }
            }
        }
        */
        overlord.getWorkspace().getProject().repaintAllGraphPanels();

        //name field:
        if (overlord.getWorkspace().getProject().accessSubNetNames() != null) {
            String name = overlord.getWorkspace().getProject().accessSubNetNames().get(selectedSubNetindex);
            elementsOfDecomposedStructure.setText(name);
        }
    }

    private void compareSubnets() {
        //SubnetComparator sc = new SubnetComparator(getCorrectSubnet(choosenDeco.get(0)), getCorrectSubnet(choosenDeco.get(0)));
        //ArrayList<ArrayList<GreatCommonSubnet>> result = sc.compare();

        //JFrame windowsComp = new HolmesPrototypeComparison(result);
        //windowsComp.show();
        GUIManager.getDefaultGUIManager().createComparisonnWindow();
        GUIManager.getDefaultGUIManager().showCompWindow();
    }

    private void getSubnetOfType() {
        if (choosenDeco.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Empty list", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        } else {
            generateProperSubNet(choosenDeco.get(0));
            //int listIndex = components.stream().map(Component::getLocation).collect(Collectors.toList()).indexOf(new Point(10, 70));

            JList newCB = generateButton(choosenDeco.get(0));
            decoListOne.setModel(newCB.getModel());

            if (listOfInvDep.contains(choosenDeco.get(0)) && getSubnetSize(choosenDeco.get(0)) == 0)
                GUIManager.getDefaultGUIManager().showInvariantsWindow();

            if (dualMode) {
                if (choosenDeco.size() > 1) {
                    generateProperSubNet(choosenDeco.get(choosenDeco.size() - 1));
                }
                JList secondnewCB = generateButton(choosenDeco.get(choosenDeco.size() - 1));
                decoListTwo.setModel(secondnewCB.getModel());
            }
        }
        logMainPanel.updateUI();

        this.repaint();
    }

    private void generateProperSubNet(int index) {
        SubnetCalculator.compileElements();
        switch (index) {
            case 0 -> SubnetCalculator.generateFS();
            case 1 -> SubnetCalculator.generateSnets();
            case 2 -> SubnetCalculator.generateTnets();
            case 3 -> SubnetCalculator.generateADT();
            case 4 -> SubnetCalculator.generateTZ();
            case 5 -> SubnetCalculator.generateHou();
            case 6 -> SubnetCalculator.generateNishi();
            case 7 -> SubnetCalculator.generateCycle(false);
            case 8 -> SubnetCalculator.generateOotsuki();
            case 9 -> SubnetCalculator.generateSMC();
            case 10 -> SubnetCalculator.generateMCT();
            case 11 -> SubnetCalculator.generateTInv();
            case 12 -> SubnetCalculator.generatePInv();
            case 13 -> SubnetCalculator.generateBranchesVerticles();
            case 14 -> SubnetCalculator.generateBranchesTransitions();
            case 15 -> SubnetCalculator.generateBranchesPlaces();
            case 16 -> SubnetCalculator.generateNotTzCycles();
        }
    }

    private int getSubnetSize(int index) {
        return switch (index) {
            case 0 -> SubnetCalculator.functionalSubNets.size();
            case 1 -> SubnetCalculator.snetSubNets.size();
            case 2 -> SubnetCalculator.tnetSubNets.size();
            case 3 -> SubnetCalculator.adtSubNets.size();
            case 4 -> SubnetCalculator.tzSubNets.size();
            case 5 -> SubnetCalculator.houSubNets.size();
            case 6 -> SubnetCalculator.nishiSubNets.size();
            case 7 -> SubnetCalculator.cycleSubNets.size();
            case 8 -> SubnetCalculator.ootsukiSubNets.size();
            case 9 -> SubnetCalculator.smcSubNets.size();
            case 10 -> SubnetCalculator.mctSubNets.size();
            case 11 -> SubnetCalculator.tinvSubNets.size();
            case 12 -> SubnetCalculator.pinvSubNets.size();
            case 13 -> SubnetCalculator.bvSubNets.size();
            case 14 -> SubnetCalculator.btSubNets.size();
            case 15 -> SubnetCalculator.bpSubNets.size();
            case 16 -> SubnetCalculator.notTzCyclesiSubNets.size();
            default -> 0;
        };
    }

    private String getProperSubNetName(int index) {
        return switch (index) {
            case 0 -> "Functional ";
            case 1 -> "S-net ";
            case 2 -> "T-net ";
            case 3 -> "connected ADT ";
            case 4 -> "Teng-Zeng subnet ";
            case 5 -> "Path ";
            case 6 -> "Augmented Seqential Path ";
            case 7 -> "Cycle ";
            case 8 -> "Ootsuki ";
            case 9 -> "SMC ";
            case 10 -> "MCT ";
            case 11 -> "T-inv";
            case 12 -> "P-inv";
            case 13 -> "Branch Vertices";
            case 14 -> "Branch Transitions";
            case 15 -> "Branch Places";
            case 16 -> "Not SS cycle";
            default -> "";
        };
    }

    private ArrayList<SubnetCalculator.SubNet> getCorrectSubnet(int type) {
        return switch (type) {
            case 0 -> SubnetCalculator.functionalSubNets;
            case 1 -> SubnetCalculator.snetSubNets;
            case 2 -> SubnetCalculator.tnetSubNets;
            case 3 -> SubnetCalculator.adtSubNets;
            case 4 -> SubnetCalculator.tzSubNets;
            case 5 -> SubnetCalculator.houSubNets;
            case 6 -> SubnetCalculator.nishiSubNets;
            case 7 -> SubnetCalculator.cycleSubNets;
            case 8 -> SubnetCalculator.ootsukiSubNets;
            case 9 -> SubnetCalculator.smcSubNets;
            case 10 -> SubnetCalculator.mctSubNets;
            case 11 -> SubnetCalculator.tinvSubNets;
            case 12 -> SubnetCalculator.pinvSubNets;
            case 13 -> SubnetCalculator.bvSubNets;
            case 14 -> SubnetCalculator.btSubNets;
            case 15 -> SubnetCalculator.bpSubNets;
            case 16 -> SubnetCalculator.notTzCyclesiSubNets;
            default -> SubnetCalculator.functionalSubNets;
        };
    }

    private JList generateButton(int index) {
        /*
        if (index == 0 || index == 1 || index == 2 || index == 3 || index == 8 || index == 10)
            return generateProperDecoButton(index);
        if (index == 4 || index == 5 || index == 6 || index == 7 || index == 9 || index == 11 || index == 12 || index == 13||index == 14 ||index ==15 || index==16)
            return generateInProperDecoButton(index);
        */
        return switch (index) {
            case 0, 1, 2, 3, 8, 10 -> generateProperDecoButton(index);
            case 4, 5, 6, 7, 9, 11, 12, 13, 14, 15, 16 -> generateInProperDecoButton(index);
            default -> generateProperDecoButton(index);
        };

    }


    private JList generateProperDecoButton(int index) {
        int size = getSubnetSize(index);
        String[] newComoList = new String[size + 3];
        for (int i = 0; i < size; i++) {
            newComoList[i + 1] = getProperSubNetName(index) + i;
            listModel.addElement(getProperSubNetName(index) + i);
        }
        newComoList[0] = "--";
        newComoList[size + 1] = "All non trivial subnets";
        newComoList[size + 2] = "All subnets";
        JList newCB = new JList(newComoList);
        newCB.setBounds(10, 70, 150, 20);
        newCB.addListSelectionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                selectedSubNetindex = -1;
                allSubNetsselected = false;
                showSubNet(index);
                elementsOfDecomposedStructure.setText("");
            } else if (selected == comboBox.getItemCount() - 1) {
                allSubNetsselected = true;
                showAllSubColors(true, index);
            } else if (selected == comboBox.getItemCount() - 2) {
                allSubNetsselected = true;
                showAllSubColors(false, index);
            } else {
                selectedSubNetindex = selected - 1;
                allSubNetsselected = false;
                showSubNet(index);
            }
        });
        newCB.setVisible(true);
        return newCB;
    }

    private JList generateInProperDecoButton(int index) {
        int size = getSubnetSize(index);
        String[] newComoList = new String[size + 2];
        for (int i = 0; i < size; i++) {
            newComoList[i + 1] = getProperSubNetName(index) + i;
        }
        newComoList[0] = "--";
        newComoList[size + 1] = "Net coverage";
        JList newCB = new JList(newComoList);
        newCB.setBounds(10, 70, 150, 20);
        newCB.addListSelectionListener(actionEvent -> {
            JComboBox<String> comboBox = (JComboBox<String>) actionEvent.getSource();
            int selected = comboBox.getSelectedIndex();
            if (selected == 0) {
                selectedSubNetindex = -1;
                allSubNetsselected = false;
                showSubNet(index);
                elementsOfDecomposedStructure.setText("");
            } else if (selected == comboBox.getItemCount() - 1) {
                allSubNetsselected = true;
                showCoverageColors(index);
            } else {
                selectedSubNetindex = selected - 1;
                allSubNetsselected = false;
                showSubNet(index);
            }
        });
        newCB.setVisible(true);
        return newCB;
    }

    private void showSubNet(int typeOfDecomposition) {
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        if (selectedSubNetindex == -1)
            return;
        SubnetCalculator.SubNet subnet = null;
        int size = 0;
        switch (typeOfDecomposition) {
            case 0 -> {
                subnet = SubnetCalculator.functionalSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.functionalSubNets.size();
            }
            case 1 -> {
                subnet = SubnetCalculator.snetSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.snetSubNets.size();
            }
            case 2 -> {
                subnet = SubnetCalculator.tnetSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.tnetSubNets.size();
            }
            case 3 -> {
                subnet = SubnetCalculator.adtSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.adtSubNets.size();
            }
            case 4 -> {
                subnet = SubnetCalculator.tzSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.tzSubNets.size();
            }
            case 5 -> {
                subnet = SubnetCalculator.houSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.houSubNets.size();
            }
            case 6 -> {
                subnet = SubnetCalculator.nishiSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.nishiSubNets.size();
            }
            case 7 -> {
                subnet = SubnetCalculator.cycleSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.cycleSubNets.size();
            }
            case 8 -> {
                subnet = SubnetCalculator.ootsukiSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.ootsukiSubNets.size();
            }
            case 9 -> {
                subnet = SubnetCalculator.smcSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.smcSubNets.size();
            }
            case 10 -> {
                subnet = SubnetCalculator.mctSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.mctSubNets.size();
            }
            case 11 -> {
                subnet = SubnetCalculator.tinvSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.tinvSubNets.size();
            }
            case 12 -> {
                subnet = SubnetCalculator.pinvSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.pinvSubNets.size();
            }
            case 13 -> {
                subnet = SubnetCalculator.bvSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.bvSubNets.size();
            }
            case 14 -> {
                subnet = SubnetCalculator.btSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.btSubNets.size();
            }
            case 15 -> {
                subnet = SubnetCalculator.bpSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.bpSubNets.size();
            }
            case 16 -> {
                subnet = SubnetCalculator.notTzCyclesiSubNets.get(selectedSubNetindex);
                size = SubnetCalculator.notTzCyclesiSubNets.size();
            }
        }

        ColorPalette cp = new ColorPalette();

        ArrayList<SubnetCalculator.SubNet> nety = getCorrectSubnet(typeOfDecomposition);
        for (int i = 0; i < size; i++) {
            SubnetCalculator.SubNet sub = nety.get(i);

            System.out.println("Podsieć numer : " + i);

            System.out.println("Lista Tranzycji");
            for (Transition t : sub.getSubTransitions()) {
                System.out.println(t.getID() + " & " + t.getName() + "\\\\");
            }

            ArrayList<Place> borderPlaces = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    ArrayList<Place> listaMiejsc = new ArrayList<>(sub.getSubPlaces());
                    SubnetCalculator.SubNet subT = nety.get(j);
                    listaMiejsc.retainAll(subT.getSubPlaces());
                    borderPlaces.addAll(listaMiejsc);
                }
            }

            ArrayList<Place> listaMiejsc = new ArrayList<>(sub.getSubPlaces());
            listaMiejsc.removeAll(borderPlaces);

            listaMiejsc.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
            System.out.println("Lista miejsc");
            for (Place p : listaMiejsc
            ) {
                System.out.println(p.getID() + " & " + p.getName() + "\\\\");
            }
            borderPlaces = borderPlaces.stream().distinct().collect(Collectors.toCollection(ArrayList::new));
            System.out.println("Lista miejsc granicznych");
            for (Place p : borderPlaces
            ) {
                System.out.println(p.getID() + " & " + p.getName() + "\\\\");
            }

        }
        /*
        System.out.println("Pre");
        for (Place t : subnet.getSubPlaces()
        ) {
            ArrayList<Transition> tmp = new ArrayList<>(t.getPreTransitions());
            tmp.retainAll(subnet.getSubTransitions());
            if(tmp.size()>0)
                System.out.println(t.getID()+ " & " + t.getName() + "\\\\");
        }
        System.out.println("Tra");
        for (Transition t : subnet.getSubTransitions()
             ) {
            System.out.println(t.getID()+ " & " + t.getName() + "\\\\");
        }
        System.out.println("Post");
        for (Place t : subnet.getSubPlaces()
        ) {
            ArrayList<Transition> tmp = new ArrayList<>(t.getPostTransitions());
            tmp.retainAll(subnet.getSubTransitions());
            if(tmp.size()>0)
            System.out.println(t.getID()+ " & " + t.getName() + "\\\\");
        }
        */


        //places
        for (Place place : subnet.getSubPlaces()) {
            if (!colorSubNet) {
                place.setGlowedSub(true);

            } else {
                if (selectedSubNetindex == size - 1)
                    place.drawGraphBoxP.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[trivial]");
                else
                    place.drawGraphBoxP.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
        }

        //arcs
        for (Arc arc : subnet.getSubArcs()) {
            if (!colorSubNet)
                arc.setGlowedSub(true);
        }

        //transitions
        for (Transition transition : subnet.getSubTransitions()) {
            if (!colorSubNet) {
                transition.drawGraphBoxT.setGlowed_MTC(true);

            } else {
                if (selectedSubNetindex == size - 1)
                    transition.drawGraphBoxT.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[trivial]");
                else
                    transition.drawGraphBoxT.setColorWithNumber(true, cp.getColor(selectedSubNetindex), false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();

        //name field:
        if (overlord.getWorkspace().getProject().accessSubNetNames() != null) {
            String name = overlord.getWorkspace().getProject().accessSubNetNames().get(selectedSubNetindex);
            elementsOfDecomposedStructure.setText(name);
        }
    }

    private void showTwoSubnets() {
        //zerowanie
        npfn = 0;
        npsn = 0;
        npcn = 0;
        ntfn = 0;
        ntsn = 0;
        ntcn = 0;
        nafn = 0;
        nasn = 0;
        nacn = 0;

        int firsttype = decoListOne.getSelectedIndex();
        int secondtype = decoListTwo.getSelectedIndex();

        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        SubnetCalculator.SubNet firstsubnet = null;
        int firstsize = 0;
        switch (choosenDeco.get(0)) {
            case 0 -> firstsubnet = SubnetCalculator.functionalSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 1 -> firstsubnet = SubnetCalculator.snetSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 2 -> firstsubnet = SubnetCalculator.tnetSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 3 -> firstsubnet = SubnetCalculator.adtSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 4 -> firstsubnet = SubnetCalculator.tzSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 5 -> firstsubnet = SubnetCalculator.houSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 6 -> firstsubnet = SubnetCalculator.nishiSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 7 -> firstsubnet = SubnetCalculator.cycleSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 8 -> firstsubnet = SubnetCalculator.ootsukiSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 9 -> firstsubnet = SubnetCalculator.smcSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 10 -> firstsubnet = SubnetCalculator.mctSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 11 -> firstsubnet = SubnetCalculator.tinvSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 12 -> firstsubnet = SubnetCalculator.pinvSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 13 -> firstsubnet = SubnetCalculator.bvSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 14 -> firstsubnet = SubnetCalculator.btSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 15 -> firstsubnet = SubnetCalculator.bpSubNets.get(decoListOne.getSelectedIndex() - 1);
            case 16 -> firstsubnet = SubnetCalculator.notTzCyclesiSubNets.get(decoListOne.getSelectedIndex() - 1);
        }


        SubnetCalculator.SubNet secondsubnet = null;
        int secondsize = 0;
        switch (choosenDeco.get(choosenDeco.size() - 1)) {
            case 0 -> secondsubnet = SubnetCalculator.functionalSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 1 -> secondsubnet = SubnetCalculator.snetSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 2 -> secondsubnet = SubnetCalculator.tnetSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 3 -> secondsubnet = SubnetCalculator.adtSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 4 -> secondsubnet = SubnetCalculator.tzSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 5 -> secondsubnet = SubnetCalculator.houSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 6 -> secondsubnet = SubnetCalculator.nishiSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 7 -> secondsubnet = SubnetCalculator.cycleSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 8 -> secondsubnet = SubnetCalculator.ootsukiSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 9 -> secondsubnet = SubnetCalculator.smcSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 10 -> secondsubnet = SubnetCalculator.mctSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 11 -> secondsubnet = SubnetCalculator.tinvSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 12 -> secondsubnet = SubnetCalculator.pinvSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 13 -> secondsubnet = SubnetCalculator.bvSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 14 -> secondsubnet = SubnetCalculator.btSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 15 -> secondsubnet = SubnetCalculator.bpSubNets.get(decoListTwo.getSelectedIndex() - 1);
            case 16 -> secondsubnet = SubnetCalculator.notTzCyclesiSubNets.get(decoListTwo.getSelectedIndex() - 1);
        }

        ColorPalette cp = new ColorPalette();

        for (Place elementTwo : secondsubnet.getSubPlaces()) {
            if (firstsubnet.getSubPlaces().contains(elementTwo)) {
                elementTwo.drawGraphBoxP.setColorWithNumber(true, Color.green, false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
                npcn++;
            } else {
                elementTwo.drawGraphBoxP.setColorWithNumber(true, Color.blue, false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
            npsn++;
        }

        for (Place element : firstsubnet.getSubPlaces()) {
            if (!secondsubnet.getSubPlaces().contains(element)) {

            } else {
                element.drawGraphBoxP.setColorWithNumber(true, Color.red, false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
            npfn++;
        }

        for (Transition elementTwo : secondsubnet.getSubTransitions()) {
            if (firstsubnet.getSubTransitions().contains(elementTwo)) {
                elementTwo.drawGraphBoxT.setColorWithNumber(true, Color.green, false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
                ntcn++;
            } else {
                elementTwo.drawGraphBoxT.setColorWithNumber(true, Color.blue, false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
            ntsn++;
        }


        for (Transition element : firstsubnet.getSubTransitions()) {
            if (secondsubnet.getSubTransitions().contains(element)) {

            } else {
                element.drawGraphBoxT.setColorWithNumber(true, Color.red, false, 0, true, "[Sub net " + (selectedSubNetindex + 1) + "]");
            }
            ntfn++;
        }


        for (Arc elementTwo : secondsubnet.getSubArcs()) {
            if (firstsubnet.getSubArcs().contains(elementTwo)) {
                elementTwo.arcDecoBox.setColor(true, Color.green);
                nacn++;
            } else {
                elementTwo.arcDecoBox.setColor(true, Color.blue);
            }
            nasn++;
        }

        for (Arc element : firstsubnet.getSubArcs()) {
            if (secondsubnet.getSubArcs().contains(element)) {
            } else {
                element.arcDecoBox.setColor(true, Color.red);
            }
            nafn++;
        }
        calcStatistics();
    }

    private void calcStatistics() {
        vafn.setText(String.valueOf(nafn));
        vafn.updateUI();
        vpfn.setText(String.valueOf(npfn));
        vpfn.updateUI();
        vtfn.setText(String.valueOf(ntfn));
        vtfn.updateUI();
        vasn.setText(String.valueOf(nasn));
        vasn.updateUI();
        vpsn.setText(String.valueOf(npsn));
        vpsn.updateUI();
        vtsn.setText(String.valueOf(ntsn));
        vtsn.updateUI();

        vacn.setText(String.valueOf(nacn));
        vacn.updateUI();
        vpcn.setText(String.valueOf(npcn));
        vpcn.updateUI();
        vtcn.setText(String.valueOf(ntcn));
        vtcn.updateUI();

        //Simple matching coefficient
        smc = ((float) ntcn + npcn + nacn) / ((ntfn + ntsn + npfn + npsn + nafn + nasn) - (ntcn + npcn + nacn));
        valueOfSMC.setText(String.valueOf(smc));
        valueOfSMC.updateUI();

        //Sorensen index
        //(2* common subgraph)/(subnet_1 + subnet_2)
        sorenIndex = (2 * ((float) ntcn + npcn + nacn)) / (ntfn + ntsn + npfn + npsn + nafn + nasn);
        valueOfSorensenIndex.setText(String.valueOf(sorenIndex));
        valueOfSorensenIndex.updateUI();
        //Jaccard index
        //(common subgraph)/(subnet_1+subnet_2 - common subgraph)
        jackobIndex = ((float) ntcn + npcn + nacn) / ((ntfn + ntsn + npfn + npsn + nafn + nasn) - (ntcn + npcn + nacn));
        valueOfJackobsenIndex.setText(String.valueOf(jackobIndex));
        valueOfJackobsenIndex.updateUI();
        //Tversky index
        //(common subgraph)/(subnet_1+subnet_2 - common subgraph)
        //dodac sterowanie wartościami
        /*
        float a = 0.75f;
        float b = 0.75f;
        tverIndex = ((float) ntcn+npcn+nacn)/((ntcn+npcn+nacn) + a*abs(((ntfn+npfn+nafn)-(ntsn+npsn+nasn))) + b*abs(((ntsn+npsn+nasn)-(ntfn+npfn+nafn))) );
        valueOfTverskyIndex.setText(String.valueOf(tverIndex));
        valueOfTverskyIndex.updateUI();
        */

    }

    private void showAllSubColors(boolean trivial, int subnetType) {
        ArrayList<SubnetCalculator.SubNet> subnets = getCorrectSubnet(subnetType);
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();

        ColorPalette cp = new ColorPalette();

        if (subnetType == 0) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor = cp.getColor();
                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Transition> transitions = subNet.getSubTransitions();
                if (transitions.size() > 1 || trivial) {
                    for (Transition transition : transitions) {
                        transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.drawGraphBoxP.setColorWithNumber(true, calcMiddleColor(currentColor, place.drawGraphBoxP.getPlaceNewColor()), false, m, true, "");
                        else
                            place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
                    }
                }
            }
        }
        if (subnetType == 1 || subnetType == 8) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor = cp.getColor();
                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Place> places = subNet.getSubPlaces();
                if (places.size() > 1 || trivial) {
                    for (Place place : places) {
                        place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m) + " (" + places.size() + ")");
                    }

                    ArrayList<Transition> transitions = subnets.get(m).getSubTransitions();
                    for (Transition transition : transitions) {
                        if (subNet.getSubBorderTransition().contains(transition)) {
                            transition.drawGraphBoxT.setColorWithNumber(true, calcMiddleColor(currentColor, transition.drawGraphBoxT.getTransitionNewColor()), false, m, true, "");
                            transition.setFrame(true);
                        } else
                            transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
                    }
                }
            }
        }
        if (subnetType == 2 || subnetType == 10) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor = cp.getColor();
                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Transition> transitions = subNet.getSubTransitions();
                if (transitions.size() > 1 || trivial) {
                    for (Transition transition : transitions) {
                        transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.drawGraphBoxP.setColorWithNumber(true, calcMiddleColor(currentColor, place.drawGraphBoxP.getPlaceNewColor()), false, m, true, "");
                        else
                            place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
                    }
                }
            }
        }

        if (subnetType == 3) {
            for (int m = 0; m < subnets.size(); m++) {
                Color currentColor;
                /*if(subnets.get(m).isProper()) {
                 */
                currentColor = cp.getColor();
                  /*  if(currentColor == Color.red)
                        currentColor = cp.getColor();
                }
                else{*/
                //  currentColor=Color.red;
                //}

                SubnetCalculator.SubNet subNet = subnets.get(m);
                ArrayList<Transition> transitions = subNet.getSubTransitions();
                if (transitions.size() > 1 || trivial) {
                    for (Transition transition : transitions) {
                        transition.drawGraphBoxT.setColorWithNumber(true, currentColor, false, m, true, "Sub #" + (m) + " (" + transitions.size() + ")");
                    }

                    ArrayList<Place> places = subnets.get(m).getSubPlaces();
                    for (Place place : places) {
                        if (subNet.getSubBorderPlaces().contains(place))
                            place.drawGraphBoxP.setColorWithNumber(true, calcMiddleColor(currentColor, place.drawGraphBoxP.getPlaceNewColor()), false, m, true, "");
                        else
                            place.drawGraphBoxP.setColorWithNumber(true, currentColor, false, m, true, "");
                    }
                    ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
                    for (Arc arc : arcs) {
                        arc.arcDecoBox.setColor(true, currentColor);
                    }
                }

            }
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    private void showCoverageColors(int subnetType) {
        ArrayList<SubnetCalculator.SubNet> subnets = getCorrectSubnet(subnetType);
        PetriNet pn = overlord.getWorkspace().getProject();
        pn.resetNetColors();


        for (int m = 0; m < subnets.size(); m++) {

            SubnetCalculator.SubNet subNet = subnets.get(m);
            ArrayList<Node> transitions = subNet.getSubNode();

            for (Node transition : transitions) {
                if (transition.getType() == PetriNetElement.PetriNetElementType.TRANSITION)
                    ((Transition) transition).drawGraphBoxT.setColorWithNumber(true, Color.red, false, m, true, "");
                if (transition.getType() == PetriNetElement.PetriNetElementType.PLACE)
                    ((Place) transition).drawGraphBoxP.setColorWithNumber(true, Color.red, false, m, true, "");
            }
            ArrayList<Arc> arcs = subnets.get(m).getSubArcs();
            for (Arc arc : arcs) {
                arc.arcDecoBox.setColor(true, Color.red);
            }
        }

        overlord.getWorkspace().getProject().repaintAllGraphPanels();
    }

    private Color calcMiddleColor(Color one, Color two) {
        int blue = 0;
        int red = 0;
        int green = 0;
        int absBlue = abs(one.getBlue() - two.getBlue());
        int absRed = abs(one.getRed() - two.getRed());
        int absGreen = abs(one.getGreen() - two.getGreen());

        if (one.getBlue() > two.getBlue())
            blue = one.getBlue() - (absBlue / 2);
        else
            blue = two.getBlue() - (absBlue / 2);

        if (one.getRed() > two.getRed())
            red = one.getRed() - (absRed / 2);
        else
            red = two.getRed() - (absRed / 2);

        if (one.getGreen() > two.getGreen())
            green = one.getGreen() - (absGreen / 2);
        else
            green = two.getGreen() - (absGreen / 2);

        //return new Color(red, green, blue);
        return Color.GRAY;
    }

    private void showInfo() {
        JFrame infoWindow = new JFrame();
        //infoWindow.setSize(100,100);
        infoWindow.setLayout(new FlowLayout());


        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPaneIfi = new JScrollPane(infoPanel);

        /*
        JLabel infoFunctional = new JLabel("tekst tekst tekst");
        JLabel infoTengZeng = new JLabel("tekst tekst tekst");
        JLabel infoTnet = new JLabel("tekst tekst tekst");
        JLabel infoSnet = new JLabel("tekst tekst tekst");
        JLabel infoADT = new JLabel("tekst \n tekst \n tekst");
        JLabel infoHou = new JLabel("tekst tekst tekst");
        JLabel infoCycle = new JLabel("tekst tekst tekst");
        JLabel infoNishi = new JLabel("tekst tekst tekst");
        JLabel infoOotsuki = new JLabel("tekst tekst tekst");
        infoPanel.add(infoFunctional);
        infoPanel.add(infoTengZeng);
        infoPanel.add(infoTnet);
        infoPanel.add(infoSnet);
        infoPanel.add(infoADT);
        infoPanel.add(infoHou);
        infoPanel.add(infoCycle);
        infoPanel.add(infoNishi);
        infoPanel.add(infoOotsuki);
        */
        //infoWindow.add(infoPanel);

        JTextArea textA = new JTextArea(10, 25);
        textA.setEditable(false);

        textA.append("Functional subnets \n \n");

        textA.append("W swojej pracy z 2004 roku Zaitsev zdefiniował podsieci funkcyjne dla sieci Petriego oraz odpowiedni algorytm dekompozycji.\n" +
                " W przeciwieństwie do innych metod opracowanych dla sieci Petriego, dekomponuje ona do struktur niepowiązanych z inwariantami, \n" +
                "czy sekwencjami uruchomień i jednocześnie jest niezależna od ich występowania w sieci.\n" +
                "Podsiecią funkcyjną nazywamy strukturę zbudowaną wokół zbioru tranzycji.Tranzycje wchodzące w jej skład posiadają zbiory miejsc \n" +
                "wejściowych rozłączne z miejscami wyjściowymi innych tranzycji wchodzących w skład podsieci.\n\n");

        JScrollPane scrollPane = new JScrollPane(textA);
        infoWindow.setSize(new Dimension(1100, 700));

        JLabel image1 = new JLabel();
        image1.setIcon(new ImageIcon(getClass().getResource("/images/Abtss-Zaj2.png")));
        image1.setBounds(new Rectangle(300, 300));
        image1.setText("Functional \n" +
                " subnets");


        JTextArea textB = new JTextArea(10, 25);
        textB.setEditable(false);

        textB.append("T-net subnets \n \n");
        textB.append("Lorem ipsum dolor sit amet, voluptua placerat maluisset mel at. Sit feugiat liberavisse deterruisset eu,\n" +
                " tamquam denique pericula qui in. No usu dico laboramus. Vero omnium conclusionemque vis in,\n" +
                " eu ius augue numquam. Per ex euismod offendit, ea vim errem recusabo. Quo ut hinc lorem consetetur. \n" +
                "Integre detraxit ex eam, ut est congue patrioque disputando. Mel ei scripta tacimates, \n" +
                "ad nonumes appareat duo. In alii constituto ius, ut pri dicat intellegam.\n\n");

        JScrollPane scrollPane1 = new JScrollPane(textB);

        JLabel image2 = new JLabel();
        image2.setIcon(new ImageIcon(getClass().getResource("/images/Abyss-ADT-1.png")));
        image2.setBounds(new Rectangle(300, 300));
        image2.setText("T-net(ADT) \n" +
                " subnets");

        JTextArea textC = new JTextArea(10, 25);
        textC.setEditable(false);

        textC.append("S-net subnets \n\n");
        textC.append("Lorem ipsum dolor sit amet, voluptua placerat maluisset mel at. Sit feugiat liberavisse deterruisset eu,\n" +
                " tamquam denique pericula qui in. No usu dico laboramus. Vero omnium conclusionemque vis in,\n" +
                " eu ius augue numquam. Per ex euismod offendit, ea vim errem recusabo. Quo ut hinc lorem consetetur. \n" +
                "Integre detraxit ex eam, ut est congue patrioque disputando. Mel ei scripta tacimates, \n" +
                "ad nonumes appareat duo. In alii constituto ius, ut pri dicat intellegam.\n\n");

        JScrollPane scrollPane2 = new JScrollPane(textC);

        JLabel image3 = new JLabel();
        image3.setIcon(new ImageIcon(getClass().getResource("/images/Abyss-Zeng-0.png")));
        image3.setBounds(new Rectangle(300, 300));
        image3.setText("S-net \n" +
                " subnets");

        JTextArea textD = new JTextArea(10, 25);
        textD.setEditable(false);

        textD.append("MCT inducted subnets \n\n");
        textD.append("Lorem ipsum dolor sit amet, voluptua placerat maluisset mel at. Sit feugiat liberavisse deterruisset eu,\n" +
                " tamquam denique pericula qui in. No usu dico laboramus. Vero omnium conclusionemque vis in,\n" +
                " eu ius augue numquam. Per ex euismod offendit, ea vim errem recusabo. Quo ut hinc lorem consetetur. \n" +
                "Integre detraxit ex eam, ut est congue patrioque disputando. Mel ei scripta tacimates, \n" +
                "ad nonumes appareat duo. In alii constituto ius, ut pri dicat intellegam.\n\n");

        JScrollPane scrollPane3 = new JScrollPane(textD);

        JLabel image4 = new JLabel();
        image4.setIcon(new ImageIcon(getClass().getResource("/images/MCT1.png")));
        image4.setBounds(new Rectangle(300, 300));
        image4.setText("MCT inducted \n\r" +
                " subnets");


        JTextArea textE = new JTextArea(10, 25);
        textE.setEditable(false);

        textE.append("P1 subnets \n\n");
        textE.append("Lorem ipsum dolor sit amet, voluptua placerat maluisset mel at. Sit feugiat liberavisse deterruisset eu,\n" +
                " tamquam denique pericula qui in. No usu dico laboramus. Vero omnium conclusionemque vis in,\n" +
                " eu ius augue numquam. Per ex euismod offendit, ea vim errem recusabo. Quo ut hinc lorem consetetur. \n" +
                "Integre detraxit ex eam, ut est congue patrioque disputando. Mel ei scripta tacimates, \n" +
                "ad nonumes appareat duo. In alii constituto ius, ut pri dicat intellegam.\n\n");

        JScrollPane scrollPane4 = new JScrollPane(textE);

        JLabel image5 = new JLabel();
        image5.setIcon(new ImageIcon(getClass().getResource("/images/Abyss-Ootsuki-3-Alternatywa.png")));
        image5.setBounds(new Rectangle(300, 300));
        image5.setText("P1 \n\r" +
                " subnets");


        infoPanel.add(scrollPane, BorderLayout.CENTER);
        infoPanel.add(image1);
        infoPanel.add(scrollPane1, BorderLayout.CENTER);
        infoPanel.add(image2);
        infoPanel.add(scrollPane2, BorderLayout.CENTER);
        infoPanel.add(image3);
        infoPanel.add(scrollPane3, BorderLayout.CENTER);
        infoPanel.add(image4);
        infoPanel.add(scrollPane4, BorderLayout.CENTER);
        infoPanel.add(image5);

        scrollPaneIfi.setBorder(BorderFactory.createLineBorder(Color.red));
        scrollPaneIfi.setPreferredSize(new Dimension(900, 600));

        infoWindow.add(scrollPaneIfi, BorderLayout.CENTER);
        //infoWindow.add(image1, BorderLayout.CENTER);
        infoWindow.setVisible(true);

    }

    public void calcSubnetsForSecondNet() {

    }
}
