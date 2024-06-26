package holmes.windows;

import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;

import javax.swing.JComboBox;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.*;


public class HolmesMCSanalysis extends JFrame {
    private GUIManager overlord;
    private JFrame ego;

    private JTextArea logField1stTab;
    private JTextArea logField2ndTab;

    int windowWidth = 1070;
    int windowHeight = 800;

    JComboBox<String> TorP;
    JCheckBox description;
    JCheckBox ID;
    //JCheckBox rankByTrans;
    JCheckBox oldRanking;
    JSpinner distanceSpinner;
    private int distanceVar = 3;
    JComboBox<String> transBox;
    JComboBox<String> mcsBox;
    JTextField importantTrans;
    ArrayList<Transition> transitions = null;
    ArrayList<Place> places = null;
    ArrayList<ArrayList<Integer>> invariantsMatrix = null;

    MCSDataMatrix mcsd = null;

    private class tokensRankDatabox {
        public HashSet<Place> outputPlacesOfMCS = null;
        public HashSet<Transition> disabledTransitionsSet = null;
        public int starvedTransitions = 0;
    }

    public HolmesMCSanalysis() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (691193817) | Exception:  " + ex.getMessage(), "error", true);

        }
        this.ego = this;
        setVisible(false);
        this.setTitle("MCS detailed analysis");
        this.overlord = GUIManager.getDefaultGUIManager();
        //ego = this;

        setLayout(new BorderLayout());
        setSize(new Dimension(windowWidth, windowHeight));
        setLocation(50, 50);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = createMainPanel();

        initiateListeners();
        fillTComboBox();
        add(mainPanel, BorderLayout.CENTER);
    }

    private static final HashMap<Character, Character> SUBSCRIPT_MAP = new HashMap<Character, Character>() {{
        put('0', '₀');
        put('1', '₁');
        put('2', '₂');
        put('3', '₃');
        put('4', '₄');
        put('5', '₅');
        put('6', '₆');
        put('7', '₇');
        put('8', '₈');
        put('9', '₉');
    }};

    private String toSubscript(String input) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (SUBSCRIPT_MAP.containsKey(c)) {
                output.append(SUBSCRIPT_MAP.get(c));
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }

    public void fillTComboBox(){
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        mcsBox.removeAllItems();
        mcsBox.addItem("---");
        mcsBox.setSelectedIndex(0);
        transBox.removeAllItems();
        transBox.addItem("---");
        transBox.setSelectedIndex(0);
        if(mcsd.getSize() == 0) {
            return;
        }
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        if(transitions != null && !transitions.isEmpty()) {
            for(int t=0; t < transitions.size(); t++) {
                transBox.addItem("t"+(t)+"."+transitions.get(t).getName());
            }
        }
    }

    private void showMCS(Integer transitionIndex, Integer mcsIndex){
        places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);
        ArrayList<Integer> MCS = dataVector.get(mcsIndex);
        HolmesNotepad notePad = new HolmesNotepad(800,500);
        notePad.setVisible(true);

        ArrayList<Integer> ImportantTrans = new ArrayList<>();
        if( !importantTrans.getText().isEmpty() ) {
            if(importantTrans.getText().contains(",")){
                String[] parseImportantTrans = importantTrans.getText().split(",");
                for (String elem : parseImportantTrans) {
                    ImportantTrans.add(Integer.parseInt(elem));
                }
            }
            else{
                ImportantTrans.add(Integer.parseInt(importantTrans.getText()));
            }
        }

        notePad.addTextLine("MCS#"+mcsIndex+"[\n", "text");
        for (int el : MCS) {
            if(ImportantTrans.contains(el)){
                notePad.addTextLine("\t(DENIED)_", "warning");
            }else{
                notePad.addTextLine("\t", "text");
            }
            notePad.addTextLine("t"+toSubscript(Integer.toString(el))+"_"+transitions.get(el).getName()+";\n", "text");
        }
        notePad.addTextLine("]\n", "text");

        HashSet<Place> Px = new HashSet<Place>();
        for(Integer MCStransition : MCS){
            Transition transition = transitions.get(MCStransition);
            Px.addAll(transition.getPostPlaces());
        }
        HashSet<Place> Poff = new HashSet<Place>();
        for(Place place : Px){
            boolean flag = true;
            for(Transition transition : place.getPreTransitions()){
                if(!MCS.contains(transition.getID()- places.size())){
                    flag = false;
                    break;
                }
            }
            if(flag){
                Poff.add(place);
            }
        }

        HashSet<Transition> Toff = new HashSet<Transition>();
        for(Place place : Poff){
            Toff.addAll(place.getPostTransitions());
        }
        notePad.addTextLine("(T_off: [\n", "text");

        for(Transition transition : Toff){
            if(ImportantTrans.contains(transition.getID()-places.size())){
                notePad.addTextLine("\t(DENIED)_", "error");
            }else{
                notePad.addTextLine("\t", "text");
            }
            notePad.addTextLine("t"+toSubscript(Integer.toString(transition.getID()-places.size()))+"_"+transition.getName()+";\n", "text");
        }
        notePad.addTextLine("], Poff: [\n", "text");
        for(Place place : Poff){
            notePad.addTextLine("\tp"+toSubscript(Integer.toString(place.getID()))+"_"+place.getName()+";\n", "text");
        }
        notePad.addTextLine("])", "text");

        Px.clear();
        Poff.clear();
        Toff.clear();
    }


    /**
     * Algorytm robiący ranking MCS według tego ile t-invariantów wyłączają, a w ramach tej samej liczby
     * t-invariantów : ile tranzycji znajduje się w sumie wsparć tych wyłączanych t-invariantów. Przeróbka
     * algorytmu studenta, stąd niektóre dziwniejsze nazwy zmiennych.
     */
    private void rankingByInvariantsThenTransitions(){
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
        int selectedTrans = transBox.getSelectedIndex();

        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {
            if(selectedTrans > 0) {
                if(selectedTrans-1 != transitionIndex) {
                    continue;
                }
            }

            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);
            if (dataVector == null || dataVector.isEmpty()) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            if (invariantsMatrix == null || invariantsMatrix.isEmpty()) {
                logField1stTab.append(" *** t-invariants set empty! ***\n");
            }

            logField1stTab.append(" *** MCS sets for transition ID: " + transitionIndex + " ***\n");

            Map<Integer,ArrayList<Integer>> B  =  new  HashMap<Integer,ArrayList<Integer>>();//t-inwarianty,które pozostały, bo nie zawierają we wsparciach ani jednej tranzycji z danego MCS
            Map<Integer,ArrayList<Integer>> A  =  new  HashMap<Integer,ArrayList<Integer>>();//t-inwarianty, których wsparcie zawiera choć jedną tranzycję z danego zbioru MCS
            Set<Integer> Tb = new HashSet<>();//zostawione tranzycje
            Set<Integer> Tx = new HashSet<>();//wyłączone tranzycje

            Map<String,Long> MCSrank  =  new  HashMap<String,Long>();
            Map<String,Set<Integer>> MCSsets  =  new  HashMap<String,Set<Integer>>();
            int MCScounter = 0;

            for(ArrayList<Integer> MCSset : dataVector){

                StringBuilder rankmsg = new StringBuilder("MCS#");
                rankmsg.append(MCScounter).append(" ").append("[");
                for (int el : MCSset) {
                    //toSubscript(Integer.toString(el))
                    rankmsg.append("t").append(el).append(", ");
                }
                rankmsg.append("]");
                rankmsg = new StringBuilder(rankmsg.toString().replace(", ]", "]"));

                int counter = 0;
                for(ArrayList<Integer> tInv : invariantsMatrix){
                    ArrayList<Integer> tInvSupp = InvariantsTools.getSupport(tInv);
                    boolean flag = true;
                    for(Integer MCStrans : MCSset){
                        if(tInvSupp.contains(MCStrans)){
                            flag = false;
                            A.put(counter,tInvSupp);
                            Tx.addAll(tInvSupp);
                            break;
                        }
                    }
                    if(flag){
                        B.put(counter,tInvSupp);
                        Tb.addAll(tInvSupp);
                    }
                    counter++;
                }
                rankmsg.append(" dis-inv: ").append(A.size()); //liczba wyłączonych inwariantów

                MCSrank.put(rankmsg.toString(), (long)(A.size()* 100000L)+ Tx.size() );
                MCSsets.put(rankmsg.toString(), new HashSet<Integer>(Tx));

                A.clear();
                B.clear();
                Tx.clear();
                Tb.clear();
                MCScounter++;
            }
            //logField1stTab.append(" === Liczba wyłączonych tranzycji przez każdy MCS ===\n");

            LinkedHashMap<String, Long> lhm = new LinkedHashMap<String, Long>();
            int lim = MCSrank.size();
            for (int i = 0; i <lim; i++) {
                //int min = transitions.size()+1;
                int min = Integer.MAX_VALUE;
                long originalEntryValue = 0;
                String remembered = "";
                for (Map.Entry<String, Long> entry : MCSrank.entrySet()) { //sortowanie po inwariantach
                    int invNumber = (int)(entry.getValue() / 100000);
                    if(invNumber<min){
                        remembered = entry.getKey();
                        min = invNumber;
                        originalEntryValue = entry.getValue();
                    }

                }
                lhm.put(remembered, originalEntryValue);
                MCSrank.remove(remembered);
            }
            ArrayList<Integer> ImportantTrans = new ArrayList<>();
            if(importantTrans.getText().isEmpty() == false) {
                if(importantTrans.getText().contains(",")){
                    String[] parseImportantTrans = importantTrans.getText().split(",");
                    for (String elem : parseImportantTrans) {
                        ImportantTrans.add(Integer.parseInt(elem));
                    }
                }
                else{
                    ImportantTrans.add(Integer.parseInt(importantTrans.getText()));
                }
            }
            int liczba_wynikow = 20;
            for (Map.Entry<String, Long> mapElement : lhm.entrySet()) {
                //if(liczba_wynikow==0){break;}
                String[] mcs = mapElement.getKey().split("\\[");

                int inv = -1;
                try {
                    int tmp1 = mcs[1].indexOf(":");
                    String check = mcs[1].substring(tmp1+1);
                    check = check.replace(" ","");
                    int num = Integer.parseInt(check);
                    inv = num;

                    tmp1 = mcs[1].indexOf("]");
                    mcs[1] = mcs[1].substring(0, tmp1+1);
                } catch (Exception e) {

                }

                int transNumberValue = (int)(mapElement.getValue() % 100000); //powrót do starej wartości
                float disabledTransitions = (float) (transNumberValue * 100) / transitions.size();
                String pSing = "%";
                for(Integer trans: ImportantTrans){
                    if(MCSsets.get(mapElement.getKey()).contains(trans)){
                        logField1stTab.append("(DENIED)_");
                        break;
                    }
                }

                //logField1stTab.append(String.format("%-8s %-30s%s%d, %d%s)", mcs[0],"["+mcs[1],"(Tx:", transNumberValue, percentage,percent));
                logField1stTab.append(String.format("%-8s %-30s %s%d%s%d (%.2f%s) )",
                        mcs[0],"["+mcs[1],"(Dis. inv: ", inv, " | Dis. trans: ", transNumberValue, disabledTransitions, pSing));
                logField1stTab.append("\n");
                liczba_wynikow--;
            }
            lhm.clear();
            MCSrank.clear();
            MCSsets.clear();
        }
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel mainPanel1stTab = new JPanel();
        mainPanel1stTab.setLayout(null); //  ╯°□°）╯︵  ┻━┻
        JPanel buttonPanelT = createUpperButtonPanel1stTab(0, 0, windowWidth-20, 125);
        JPanel logMainPanelT = createLogMainPanel1stTab(0, 125, windowWidth, windowHeight-120);

        mainPanel1stTab.add(buttonPanelT);
        mainPanel1stTab.add(logMainPanelT);
        mainPanel1stTab.repaint();

        tabbedPane.addTab("MCS", Tools.getResIcon22("/icons/invWindow/tInvIcon.png"), mainPanel1stTab, "T-invariants");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        main.add(tabbedPane, BorderLayout.CENTER);

        return main;
    }

    private JPanel createUpperButtonPanel1stTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setBounds(x, y, width, height);

        int posX = 10;
        int posY = 10;

        oldRanking = new JCheckBox("Old v1.6.9.5");
        oldRanking.setBounds(posX+190,posY+85, 100,15);
        panel.add(oldRanking);


        JButton button1 = new JButton("TokensRanking");
        button1.setText("<html>Tokens<br />ranking</html>");
        button1.setBounds(posX, posY, 150, 40);
        button1.setMargin(new Insets(0, 0, 0, 0));
        button1.setIcon(Tools.getResIcon32("/icons/stateSim/simpleSimTab.png"));
        button1.addActionListener(actionEvent -> {
            places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
            transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
            mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
            if(mcsd.getSize() == 0) {
                logField1stTab.append(" *** No MCS data! \n");
                return;
            }

            if(oldRanking.isSelected())  {
                jeden();
            } else {
                checkStarvedTransitions();
            }
        });
        button1.setFocusPainted(false);
        panel.add(button1);

        /*info*/
        JButton button1Info = new JButton("TKInfo");
        button1Info.setText("<html>Explanations</html>");
        button1Info.setBounds(posX, posY+40, 120, 15);
        button1Info.setMargin(new Insets(0, 0, 0, 0));
        button1Info.setIcon(Tools.getResIcon32(""));
        button1Info.addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(null,
                    "When you press the button \"Tokens ranking\", make sure that you have selected the transition for which\n" +
                            "you want to create a ranking of its MCS sets in the list to the right of the \"Clear Window\" button.\n" +
                            "If \"---\" is selected, the rankings will be created (independently) for the MCS sets of each single\n" +
                            "transition for which they were generated in the previous window.\n" +
                            "\n" +
                            "The \"Tokens ranking\" button creates a ranking of MCS sets based on how many transitions will potentially\n" +
                            "be starved if we consider MCS transitions to be permanently excluded in the simulation. Note: the algo-\n" +
                            "rithm is more complicated than the following description, but this one gives an idea of how it works. Let's\n" +
                            "assume that the transitions in MCS are a set of M. The set X is all the output places of the transitions \n" +
                            "from M. The set Y, is all the output transitions of the places from X. The algorithm checks whether in the\n" +
                            "set Y (the set of potentially starved transitions) there are some for which *all* input places have *at \n" +
                            "least one* transition that will provide tokens. Such a transition, in order to sustain token production at\n" +
                            "the input places of those in Y, must meet the conditions: not be in the M set (obviously), not be in the Y\n" +
                            "set. In addition, the distance variable (2 or 3) periodizes how far in the net structure the above relation-\n" +
                            "ships are sought. If any transition initially being in the set Y, has all its input places such that they\n" +
                            "in turn will have tokens produced by other transitions independent of M (by distance), it is removed from Y.\n" +
                            "The final ranking is based on the number of transitions remaining in Y.\n" +
                            "\n" +
                            "Version 1.6.9.5: [PL] Najkrócej rzecz ujmując sprawdzane jest tylko najbliższe otoczenie zbnioru M: liczność\n" +
                            "zbiorów X oraz Y. Algorytm w tejże wersji daje więc bardzo, ale to bardzo przybliżone informacje o potencjale\n" +
                            "wyłączającym zbioru MCS, niż algorytm z wersji 1.6.9.7 i dalej."
                    , "Info", JOptionPane.INFORMATION_MESSAGE);

        });
        button1Info.setFocusPainted(false);
        panel.add(button1Info);

        description  = new JCheckBox("Full trans/places names");
        description.setBounds(posX-5,posY+60, 170,20);
        panel.add(description);

        ID = new JCheckBox("ID of transitions/places");
        ID.setBounds(posX-5,posY+85, 160,15);
        panel.add(ID);

        JLabel comboDesc = new JLabel("Sort by:");
        comboDesc.setBounds(posX+155,posY, 150,20);
        panel.add(comboDesc);
        TorP = new JComboBox<String>();
        TorP.addItem("T_off");
        TorP.addItem("P_off");
        TorP.setBounds(posX+155, posY+20, 70, 20);
        TorP.setSelectedIndex(0);
        panel.add(TorP);

        JLabel transDesc = new JLabel("Important trans.:");
        transDesc.setBounds(posX+230, posY, 150,20);
        panel.add(transDesc);

        importantTrans = new JTextField();
        importantTrans.setBounds(posX+230, posY+20, 100, 20);
        panel.add(importantTrans);

        //iFeelCourageous = new JCheckBox("I feel courageous");
        //iFeelCourageous.setBounds(posX+165,posY+70, 130,15);
        //panel.add(iFeelCourageous);

        JLabel distLabel = new JLabel("Dist:");
        distLabel.setBounds(posX+160,posY+45, 40,20);
        panel.add(distLabel);

        SpinnerModel distSpinnerModel = new SpinnerNumberModel(3, 2, 3, 1);
        distanceSpinner = new JSpinner(distSpinnerModel);
        distanceSpinner.setBounds(posX+190, posY+45, 60, 20);
        distanceSpinner.addChangeListener(e -> {
            JSpinner spinner = (JSpinner) e.getSource();
            distanceVar = (int) spinner.getValue();
        });
        panel.add(distanceSpinner);

        //**************************************************************************

        //rankByTrans = new JCheckBox("Ranking by disabled transitions");
        //rankByTrans.setBounds(posX+471,posY+55, 220,15);
        //panel.add(rankByTrans);

        JButton transRank = new JButton("T-inv rank");
        transRank.setText("<html><center>Invariants<br />ranking</center></html>");
        transRank.setBounds(posX + 340, posY+48, 130, 43);
        transRank.setMargin(new Insets(0, 0, 0, 0));
        transRank.setIcon(Tools.getResIcon22("/icons/menu/menu_analysis_invariants.png"));
        transRank.addActionListener(actionEvent -> {
            if(oldRanking.isSelected())  {
                orgInvTransRankingAlg();
            } else {
                rankingByInvariantsThenTransitions();
            }
        });
        transRank.setFocusPainted(false);
        panel.add(transRank);

        JButton transRankInfo = new JButton("Jenson");
        transRankInfo.setText("<html>Explanations</html>");
        transRankInfo.setBounds(posX+340, posY+92, 120, 15);
        transRankInfo.setMargin(new Insets(0, 0, 0, 0));
        transRankInfo.setIcon(Tools.getResIcon32(""));
        transRankInfo.addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(null,
                    "When you press the button \"Invariants ranking\", make sure that you have selected the transition for \n" +
                            "which you want to create a ranking of its MCS sets in the dropdown list to the right of the \n" +
                            "\"Clear Window\" button. If \"---\" is selected, the rankings will be created (independently) for the MCS\n" +
                            "sets of each single transition for which they were generated in the previous window.\n" +
                            "\n" +
                            "The \"Invariants ranking\" button creates a ranking of MCS sets according to two criteria. The most \n" +
                            "important on is the number of t-invariants disabled by a given MCS (i.e., t-invariant is considered\n" +
                            "disabled, if it contains in its support at least one transition from an MCS). If the same number of \n" +
                            "t-invariants is disabled for several different MCS sets, then the order is further determined by the \n" +
                            "number of transitions found in the supports of all excluded t-invariants. The fewer, the higher the MCS\n" +
                            "is ranked because the more subtle its effects are on the net.\n" +
                            "Additionally: please leave Distance setting to 3, and be aware that other controls have currently no\n" +
                            "influence on this ranking. [To be adjusted soon]\n" +
                            "\n" +
                            "Version 1.6.9.5 and lower: [PL] ranking jest robiony tylko na podstawie drugiego opisanego kryterium, tj.\n" +
                            "im mniej tranzycji w sumie wsparć wyłączanych t-invariantów tym lepiej, ale sama liczba t-invariantów\n" +
                            "NIE jest brana pod uwagę. Druga uwaga: nie daję 100% pewności że algorytm jest 100% poprawny, bo nie\n" +
                            "ja go pisałem :)"
                    , "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        transRankInfo.setFocusPainted(false);
        panel.add(transRankInfo);

        JButton buttonData = new JButton("Button 4");
        buttonData.setText("<html><center>Clear<br />window</center></html>");
        buttonData.setBounds(posX + 340, posY, 130, 45);
        buttonData.setMargin(new Insets(0, 0, 0, 0));
        buttonData.setIcon(Tools.getResIcon22("/icons/toolbar/refresh.png"));
        buttonData.addActionListener(actionEvent -> {
            logField1stTab.setText("");

        });
        buttonData.setFocusPainted(false);
        panel.add(buttonData);

        String[] dataT = { "---" };//combobox z tranzycjami
        transBox = new JComboBox<String>(dataT);
        transBox.setBounds(posX+480, posY, 350, 20);
        transBox.setSelectedIndex(0);
        transBox.setMaximumRowCount(6);
        transBox.removeAllItems();
        transBox.addItem("---");
        transBox.addActionListener(actionEvent -> {//uzupelnianie combobox z mcsami dla wybranej tranzycji
            mcsBox.removeAllItems();
            mcsBox.addItem("---");
            mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
            if(mcsd.getSize() == 0) {
//                logField1stTab.append(" *** BRAK DANYCH MCS! \n");
                return;
            }
            int selected = transBox.getSelectedIndex();
            if(selected <= 0)
                return;
            selected--;
            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(selected);
            int counter = 0;
            for(ArrayList<Integer>MCS : dataVector){
                StringBuilder msg;
                msg = new StringBuilder("MCS#");
                msg.append(counter).append(" ").append("[");
                for (int el : MCS) {
                    msg.append(el).append(", ");
                }
                msg.append("]");
                msg = new StringBuilder(msg.toString().replace(", ]", "]"));
                mcsBox.addItem(msg.toString());
                counter++;
            }
        });
        panel.add(transBox);

        mcsBox = new JComboBox<String>(dataT);
        mcsBox.setBounds(posX+480, posY+30, 200, 20);
        mcsBox.setSelectedIndex(0);
        mcsBox.setMaximumRowCount(6);
        mcsBox.removeAllItems();
        mcsBox.addItem("---");
        mcsBox.addActionListener(actionEvent -> {});
        panel.add(mcsBox);

        JButton showMCS = new JButton("Button 888");
        showMCS.setText("Show MCS");
        showMCS.setBounds(posX + 700, posY+30, 130, 40);
        showMCS.setMargin(new Insets(0, 0, 0, 0));
        showMCS.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
        showMCS.addActionListener(actionEvent -> {
            int selectedTrans = transBox.getSelectedIndex();
            if(selectedTrans == 0)
                return;
            selectedTrans--;
            int selectedMCS = mcsBox.getSelectedIndex();
            if(selectedMCS == 0)
                return;
            selectedMCS--;
            showMCS(selectedTrans,selectedMCS);
        });
        showMCS.setFocusPainted(false);
        panel.add(showMCS);



        JButton mcsOffRank = new JButton("Off Rank");
        mcsOffRank.setText("<html><center>Minimum offline<br />places and transitions<br />ranking</center></html>");
        mcsOffRank.setBounds(posX + 840, posY, 190, 70);
        mcsOffRank.setMargin(new Insets(0, 0, 0, 0));
        mcsOffRank.setIcon(Tools.getResIcon22("/icons/toolbar/simLog.png"));
        mcsOffRank.addActionListener(actionEvent -> {
            trzy();
        });
        mcsOffRank.setFocusPainted(false);
        panel.add(mcsOffRank);

        JButton mcsOffRankInfo = new JButton("Jenson");
        mcsOffRankInfo.setText("<html>Info</html>");
        mcsOffRankInfo.setBounds(posX+840, posY+71, 40, 15);
        mcsOffRankInfo.setMargin(new Insets(0, 0, 0, 0));
        mcsOffRankInfo.setIcon(Tools.getResIcon32(""));
        mcsOffRankInfo.addActionListener(actionEvent -> {
            JOptionPane.showMessageDialog(null,
                    "Zadaniem jest wybranie jednego najlepszego zbioru MCS dla każdej tranzycji badanego\n" +
                            "modelu. Ten algorytm jest w zasadzie rozwinięciem pierwszego algorytmu, ponieważ również\n" +
                            "tworzy zbiory P_off oraz T_off , ale zamiast tworzyć ranking minimalnych zbiorów odcinających to\n" +
                            "wybiera teoretycznie najlepszy zbiór dla danej tranzycji. Według kryteriów algorytmu, najlepszy\n" +
                            "zbiór to taki, którego tranzycje bezpośrednio wyłączają najmniej miejsc i tranzycji w sieci, czyli\n" +
                            "zbiory P_off i T_off są najmniejsze. Ponadto, w sytuacji kiedy trafi się jeden lub więcej zbiorów o\n" +
                            "takim samym wyniku to sprawdzana jest liczba tranzycji w każdym ze zbiorów MCS. Im mniejszy\n" +
                            "jest zbiór MCS tym lepiej. Oczywiście w tym algorytmie również istnieje możliwość oznaczenia\n" +
                            "ważnych tranzycji, które mają pozostać włączone. Jeśli potencjalnie najlepszy zbiór MCS wyłącza\n" +
                            "taką tranzycję, to obok niego pojawi się kolejny najlepszy zbiór, który takiej tranzycji nie wyłącza.\n" +
                            "Dodatkowo, tam gdzie to możliwe, algorytm stara się nie wybierać jako najlepszy zbiór trywialnego\n" +
                            "przypadku, gdzie MCS to jedno-elementowy zbiór składający się z tranzycji, dla której szukany\n" +
                            "jest najlepszy zbiór."
                    , "Info", JOptionPane.INFORMATION_MESSAGE);

        });
        mcsOffRankInfo.setFocusPainted(false);
        panel.add(mcsOffRankInfo);

        return panel;
    }

    private JPanel createLogMainPanel1stTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("Log window"));
        panel.setBounds(x, y, width-20, height-50);

        logField1stTab = new JTextArea();
        logField1stTab.setLineWrap(true);
        logField1stTab.setEditable(true);
        logField1stTab.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret)logField1stTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField1stTab), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-35, height-110);
        panel.add(logFieldPanel);


        return panel;
    }
    private void initiateListeners() { //HAIL SITHIS
        addWindowListener(new WindowAdapter() {
            public void windowActivated(WindowEvent e) {
                fillTComboBox();
            }
        });
    }

    /**
     * Algorytm znajdowania tranzycji które nie będą mogły się uruchomić z powodu braku tokenów
     * spowołanego przez wyłączenie tranzycji z MCS oraz bezpośredniego otoczenia tychże.
     * Naciągamy tutaj ogólną teorię MCS (powstają z t-invariantów), ale może on pełnić funkcję pomocniczą.
     */
    private void checkStarvedTransitions(){
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();

        int selectedTrans = transBox.getSelectedIndex();
        int Distance = distanceVar;

        for(int objReaction = 0; objReaction<mcsd.getSize(); objReaction++) {
            //tranzycja to wyznacznik zbiorów
            if(selectedTrans > 0) {
                if(selectedTrans-1 != objReaction) {
                    continue;
                }
            }
            ArrayList<ArrayList<Integer>> MCSsForObjReaction = mcsd.getMCSlist(objReaction);
            if (MCSsForObjReaction == null || MCSsForObjReaction.isEmpty()) {
                continue;
            }
            if (invariantsMatrix == null || invariantsMatrix.isEmpty()) {
                logField1stTab.append(" *** t-invariants set empty! ***\n");
            }
            logField1stTab.append(" *** MCS sets for transition ID: " + objReaction + " ***\n");

            Map<String, tokensRankDatabox> MCSrank  =  new  HashMap<String, tokensRankDatabox>();

            int counter = -1;
            for(ArrayList<Integer> MCS : MCSsForObjReaction) {
                counter++;

                //Dane wejściowe dla danego zbioru MCS dla danej tranzycji:
                HashSet<Transition> hashSetMCS = new HashSet<Transition>(); //zbior tranzycji w MCS, a nie tylko numerki ID
                for (Integer MCStransitionID : MCS) {
                    hashSetMCS.add(transitions.get(MCStransitionID));
                }

                //dołącz do zbioru MCS te trazycje, które na pewno nie będą w stanie działać, ponieważ
                //ich miejsca wejściowe są zaopatrywane w tokeny tylko przez tranzycje z MCS:
                hashSetMCS = redefineMCS(hashSetMCS);
                HashSet<Place> outputPlacesOfMCS = createOutputPlacesOfMCS(hashSetMCS);
                HashSet<Place> safeOutputPlacesOfMCS = new HashSet<Place>();

                for (Integer MCStransition : MCS) {
                    Transition transition = transitions.get(MCStransition);
                    //outputPlacesOfMCS.addAll(transition.getPostPlaces());
                }
                HashSet<Transition> T_dng = new HashSet<Transition>();
                for (Place place : outputPlacesOfMCS) {
                    T_dng.addAll(place.getPostTransitions());
                }


                boolean stop = false;
                //ArrayList<Transition> transPotentiallyDeadOfTdng = new ArrayList<Transition>(); //miejsce wejściowe ma trans w T_dng
                ArrayList<Transition> transDeadDirectlyBecauseOfMCS = new ArrayList<Transition>(); //miejsce wejściowe ma TYLKO trans z MCS
                boolean triggerFoundReallyDeadTransitionInTdng = false;

                while (true) { //tak długo, aż wewnętrzna pętla niczego nie usunie
                    if (stop) break;

                    Transition transToRemove = null;
                    for (Transition currentTrans : T_dng) { //dla kazdej potencjalnie zaglodzonej
                        stop = true; //zostanie tak, jeżeli niczego nie usuniemy
                        boolean removeTransitionFromEndangeredSet = false;
                        boolean potentiallyRemoveTransAsStarved = false;

                        //tworzy zbiór miejsc wejściowych, które karmią tranzycję T_dng tokenami:
                        HashSet<Place> inputPlacesForTransFormT_dng = new HashSet<Place>(currentTrans.getPrePlaces()); //pobierz miejsca wejściowe

                        for(Place place : inputPlacesForTransFormT_dng) { //sprawdzamy czy miejsce może mieć tokeny
                            triggerFoundReallyDeadTransitionInTdng = false;
                            HashSet<Transition> inputTransToInputPlacesForT_dng = new HashSet<Transition>(place.getPreTransitions()); //pobierz t wejściowe
                            inputTransToInputPlacesForT_dng.removeIf(hashSetMCS::contains); //usuwamy te, które są w MCS
                            if(inputTransToInputPlacesForT_dng.isEmpty()) { //miejsce martwe, bo nie ma tokenów
                                for(Transition t_tmp : place.getPostTransitions()) {
                                    if(T_dng.contains(t_tmp)) {
                                        transDeadDirectlyBecauseOfMCS.add(t_tmp);
                                    }
                                }
                                stop = false;
                                triggerFoundReallyDeadTransitionInTdng = true;
                                break;
                            }
                            //jeżeli tranzycja jest w zbiorze T_dng:
                            inputTransToInputPlacesForT_dng.removeIf(T_dng::contains); //TU JEST TRICKY! KOLEJNOSC!!!
                            if(inputTransToInputPlacesForT_dng.isEmpty()) {
                                //właśnie znaleziono miejsce, które nie ma tokenów, bo nie ma tranzycji wejściowych, które nie są w MCS i T_dng
                                break; //break, a nie continue, bo po co sprawdzać pozostałe miejsca jak jedno i tak nie ma tokenów?
                            }

                            boolean unstarvedPlaceFound = false; //jesli jeszcze zostaly do sprawdzania te tranzycje, których nie ma w MCS i T_dng:
                            for(Transition toCheckTrans : inputTransToInputPlacesForT_dng) {
                                boolean canTransitionFire = canFire(toCheckTrans,  T_dng, Distance);
                                if(canTransitionFire) {
                                    unstarvedPlaceFound = true; //znaleziono jedna niezagrożoną tranzycję więc miejsce wejściowe będzie miało tokeny
                                    break; //wystarczy jedna
                                }
                            }
                            if(unstarvedPlaceFound) { //miejsce wejsciowe ma tokeny
                                if(outputPlacesOfMCS.contains(place)) { //jeśli wyjściowe z MCS, to dodaj do bezpiecznych:
                                    safeOutputPlacesOfMCS.add(place);
                                }

                                potentiallyRemoveTransAsStarved = true;
                                continue; //dla kolejnego miejsca wejsciowego tej tranzycji trans
                            } else { //jedno z miejsc wejsciowych nie ma tokenów:
                                potentiallyRemoveTransAsStarved = false;
                                //for(Transition t_tmp : place.getPostTransitions()) {
                                //    if(T_dng.contains(t_tmp)) {
                                //         transPotentiallyDeadOfTdng.add(t_tmp);
                                //    }
                                //}
                                //wszystkie inne tranzycje wyjściowe tego miejsca - można już sobie podarować sprawdzanie:
                                break; //nie ma tokenów w miejscu wejściowym, więc tranzycja jest zagłodzona
                            }
                        } //for(Place place : inputPlacesForTransFormT_dng)

                        if(triggerFoundReallyDeadTransitionInTdng) { //usuwamy z Tdng na stałe bo jest MARTWA
                            break;  //i to się już nie zmieni, bo ten trigger oznacza, że tylko tranzycje z MCS
                            //produkują tokeny do jednego z miejsc wejściowych tej tranzycji.
                        }

                        if(potentiallyRemoveTransAsStarved) {
                            //jeżeli wciąż true, to znaczy, że tranzycja nie jest zagłodzona bo każde jej miejsce wejściowe ma tokeny
                            //jezeli usyniemy tranzycję z T_dng to musimy sprawdzić jeszcze raz dla nowego zbioru T_dng
                            stop = false; //usunęliśmy coś, więc musimy sprawdzić jeszcze raz
                            transToRemove = currentTrans;
                            break;
                        }
                    } //for (Transition t : T_dng)
                    if(triggerFoundReallyDeadTransitionInTdng) { //usuwamy z Tdng na stałe bo jest MARTWA
                        T_dng.removeIf(transDeadDirectlyBecauseOfMCS::contains);
                        break;
                    }
                    if(stop == false) {
                        T_dng.remove(transToRemove);
                    }
                    if(T_dng.isEmpty()) {
                        stop = true;
                    }
                }  //while(true)

                //usuń miejsca wyjściowe z MCS, które są bezpieczne:
                outputPlacesOfMCS.removeAll(safeOutputPlacesOfMCS);

                //przywróć do T_dng te, które są na pewno zagłodzone bezpośrednio przez MCS:
                T_dng.addAll(transDeadDirectlyBecauseOfMCS);

                //dodawanie informacji o MCS do rankingu:
                StringBuilder rankmsg = new StringBuilder("MCS#");
                rankmsg.append(counter).append(" ").append("[");
                for (int el : MCS) {
                    rankmsg.append("t").append(el).append(", ");
                }
                rankmsg.append("]");
                rankmsg = new StringBuilder(rankmsg.toString().replace(", ]", "]"));

                tokensRankDatabox obj = new tokensRankDatabox();
                obj.starvedTransitions = T_dng.size();
                obj.outputPlacesOfMCS = outputPlacesOfMCS;

                MCSrank.put(rankmsg.toString(), obj); //dodaj do rankingu

            } //dla wszystkich MCSów

            LinkedHashMap<String, tokensRankDatabox> lhm = new LinkedHashMap<String, tokensRankDatabox>();
            int lim = MCSrank.size();
            for (int i = 0; i <lim; i++) {
                Integer min = transitions.size()+1;
                String rem = "";
                tokensRankDatabox newObject = new tokensRankDatabox();

                for (Map.Entry<String, tokensRankDatabox> entry : MCSrank.entrySet()) {
                    if(entry.getValue().starvedTransitions < min){
                        newObject = entry.getValue();
                        rem = entry.getKey();
                        min = entry.getValue().starvedTransitions;
                    }
                }
                lhm.put(rem, newObject);
                MCSrank.remove(rem);
            }
            ArrayList<Integer> ImportantTrans = new ArrayList<>();
            if(importantTrans.getText().length()>0) {
                if(importantTrans.getText().contains(",")){
                    String[] parseImportantTrans = importantTrans.getText().split(",");
                    for (String elem : parseImportantTrans) {
                        ImportantTrans.add(Integer.parseInt(elem));
                    }
                }
                else{
                    ImportantTrans.add(Integer.parseInt(importantTrans.getText()));
                }
            }
            int liczba_wynikow = 20;
            for (Map.Entry<String, tokensRankDatabox> mapElement : lhm.entrySet()) {
                //if(liczba_wynikow==0){break;}
                String[] mcs = mapElement.getKey().split("\\[");
                float percStarvedTransitions = (float) (mapElement.getValue().starvedTransitions * 100) / transitions.size();
                float percStarvedPlaces = (float) (mapElement.getValue().outputPlacesOfMCS.size() * 100) / places.size();
                String pSign = "%";
                logField1stTab.append(String.format("%-8s %-30s%s%d, %.2f%s, %s%d, %.2f%s)", mcs[0],"["+mcs[1],
                        "( Tx:", mapElement.getValue().starvedTransitions, percStarvedTransitions, pSign, "Px: ",
                        mapElement.getValue().outputPlacesOfMCS.size(), percStarvedPlaces, pSign));
                logField1stTab.append("\n");
                liczba_wynikow--;
            }
            lhm.clear();
            MCSrank.clear();
        }
    }

    /**
     * Algorytm tworzy zbiór wszystkich miejsc wyjściowych tranzycji, które są w MCS.
     * @param hashSetMCS zbiór tranzycji w MCS
     * @return zbiór miejsc wyjściowych tranzycji w MCS
     */
    private HashSet<Place> createOutputPlacesOfMCS(HashSet<Transition> hashSetMCS) {
        HashSet<Place> outputPlacesOfMCS = new HashSet<Place>();
        for(Transition t : hashSetMCS) {
            outputPlacesOfMCS.addAll(t.getPostPlaces());
        }
        return outputPlacesOfMCS;
    }

    /**
     * Algorytm dodaje do otrzymanego zbioru MCS dodatkowe tranzycje, jeżeli ich miejsca wejściowe są
     * zaopatrywane TYLKO I WYŁĄCZNIE przez tranzycje które już są w otrzymanym zbiorze MCS.
     * @param hashSetMCS oryginalny zbiór tranzycji w MCS
     * @return nowy zbiór tranzycji w MCS, tych które nie będą mogły się uruchomić z powodu braku tokenów
     */
    private HashSet<Transition> redefineMCS(HashSet<Transition> hashSetMCS) {
        HashSet<Transition> newMCS = new HashSet<Transition>();
        for(Transition t : hashSetMCS) { //dla wszystkich tranzycji w MCS
            ArrayList<Place> outPlaces = t.getPostPlaces();
            for(Place p : outPlaces) { //dla wszystkich miejsc wyjściowych tranzycji
                //sprawdzanie, czy tranzycje wejściowe do p są w CAŁOŚCI w MCS:
                HashSet<Transition> inTrans = new HashSet<Transition>(p.getPreTransitions());
                inTrans.removeIf(hashSetMCS::contains); //usuwamy te, które są w MCS
                if(inTrans.isEmpty()) { //miejsce jest zagłodzone całkowicie
                    newMCS.addAll(p.getPostTransitions());  //dodajemy wszystkie tranzycje wyjściowe tego miejsca
                    break;
                }
            }
        }
        newMCS.addAll(hashSetMCS);
        return newMCS;
    }

    private boolean canFire(Transition t_x, HashSet<Transition> T_dng, int distance) {
        for(Place p : t_x.getPrePlaces()) {
            if(AllInputTransitionsInDngSet(p, T_dng) == true) { //to znaczy, że wszystkie tranzycje wejściowe p, są w T_dng
                return false; //czyli uważamy, że tranzycja t_x jest w niebezpieczeństwie
            }
            if(distance > 1) {
                for(Transition t : p.getPreTransitions()) { //dla wszystkich tranzycji t, które prowadzą do miejsca p
                    if(CheckHigherLevels(t, T_dng, distance-1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean AllInputTransitionsInDngSet(Place p, HashSet<Transition> T_dng) {
        for(Transition t : p.getPreTransitions()) { //każda tranzycja wejściowa miejsca
            if(T_dng.contains(t) == false) {
                return false;   //jeśli chociaż jedna tranzycja nie jest w T_dng
                //to dostarczy tokeny do miejsca p
            }
        }
        return true; //jak tu dotrzemy, to znaczy, że wszystkie tranzycje są w T_dng
    }

    private boolean CheckHigherLevels(Transition t, HashSet<Transition> T_dng, int distance) {
        if(distance == 1) {
            for(Place p : t.getPrePlaces()) {
                if(AllInputTransitionsInDngSet(p, T_dng) == false) {
                    return false;
                }
            }
            //return AllInputTransitionsInDngSet(t, T_dng);
        } else {
            for(Place p : t.getPrePlaces()) {
                if(AllInputTransitionsInDngSet(p, T_dng)) {
                    return true; //jeżeli wszystkie tranzycje wejściowe do miejsca p są w T_dng
                    //to nie ma co sprawdzać dalej
                }
                if(distance > 2) {
                    for(Transition t_prime : p.getPreTransitions()) {
                        if(CheckHigherLevels(t_prime, T_dng, distance-1)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    //*****************************************************************************************************
    //*****************************************************************************************************
    //                                  Algorytmy studenta
    //*****************************************************************************************************
    //*****************************************************************************************************


    /**
     * Algorytm rankingu wg. liczby tranzycji we wsparciach wszystkich wyłączanych t-invariantów.
     * Zastąpiony przez checkStarvedTransitions()
     *
     * Opis z pracy dyplomowej:
     * Ten opracowany algorytm odpowiedzialny jest za stworzenie rankingu
     * zbiorów MCS dla każdej z tranzycji modelu, wynikającego z wpływu wyłączenia danego
     * zbioru MCS na przepływ tokenów w sieci, czyli na jej dynamikę. Na początku tworzony jest zbiór
     * P_x, który zawiera miejsca, do których prowadzą łuki z tranzycji MCS. Dalej dla każdego miejsca ze
     * zbioru P_x sprawdzane jest czy ma inne tranzycje wejściowe niż te z MCS. Miejsca, które nie mają
     * żadnych innych tranzycji wejściowych, niż te z MCS, tworzą zbiór P_off . Oznacza to, że wyłączenie
     * MCS, efektywnie wyłączą możliwość tworzenia tokenów w tych miejscach. Im mniejszy zbiór
     * P_off dla danego MCS, tym lepiej, ponieważ oznacza to że mniej miejsc zostanie całkowicie zagłodzonych
     * przez wyłączenie tranzycji z MCS. Następnie tworzony jest zbiór tranzycji T_off , który
     * zawiera wszystkie tranzycje, które jako miejsce wejściowe posiadają przynajmniej jedno miejsce
     * ze zbioru P_off . Wszystkie tranzycje ze zbioru Toff nie mogą się uruchomić, z powodu braku tokenów
     * w przynajmniej jednym swoim miejscu wejściowym, co jest efektem użycia zbioru MCS.
     * Analogicznie jak ze zbiorem P_off , im mniejszy zbiór T_off , tym lepiej w kontekście użycia danego
     * zbioru MCS. Wynika to z założenia, że użycie zbiory MCS, które z definicji niejako wyłącza jakiś
     * "element sieci (tranzycję docelową), nie powinno skutkować wyłączaniem innych elementów sieci.
     */
    private void jeden(){
        places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();

        boolean deniedTransParseFailed = false;
        int selectedTrans = transBox.getSelectedIndex();

        //for(int transitionIndex : il_trans){
        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {
            if(selectedTrans > 0) {
                if(selectedTrans-1 != transitionIndex) {
                    continue;
                }
            }

            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);

            if (dataVector == null || dataVector.isEmpty()) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            logField1stTab.append(" *** MCS sets for transition ID: " + transitionIndex + ": ***\n");

            Map<String, ArrayList<Integer>> MCSsets = new HashMap<String, ArrayList<Integer>>();
            Map<String, ArrayList<Integer>> MCSsetsindexes = new HashMap<String, ArrayList<Integer>>();
            Map<String, ArrayList<Integer>> MCStrans= new HashMap<String, ArrayList<Integer>>();
            int counter = 0;
            for(ArrayList<Integer>MCS : dataVector){
                StringBuilder rankmsg = new StringBuilder("MCS#");
                StringBuilder msg;
                //logField1stTab.append("MCS#" + counter + " ");
                rankmsg.append(counter).append(" ").append("[");
                msg = new StringBuilder("[");
                for (int el : MCS) {
                    //toSubscript(Integer.toString(el))
                    msg.append("t").append(el).append(", ");
                    rankmsg.append("t").append(el).append(", ");
                }
                msg.append("] : ");
                rankmsg.append("] ");
                msg = new StringBuilder(msg.toString().replace(", ]", "]"));
                rankmsg = new StringBuilder(rankmsg.toString().replace(", ]", "]"));

                //Dla każdego MCS sprawdzamy, do ilu bezpośrednio miejsc prowadzą łuki z jego tranzycji. Niech to będzie zbiór Px, czyli podzbiór zbioru P miejsc sieci.
                //---
                HashSet<Place> Px = new HashSet<Place>();
                for(Integer MCStransition : MCS){
                    Transition transition = transitions.get(MCStransition);
                    Px.addAll(transition.getPostPlaces());
                }
                //---
                //Teraz sprawdzamy, ile miejsc z Px nie posiada żadnych innych łuków wejściowych, tj. innych niż te wychodzące z tranzycji ze zbioru MCS.
                //Niech to będzie podzbiór Poff, czyli podzbiór zbioru Px.
                //---
                HashSet<Place> Poff = new HashSet<Place>();
                for(Place place : Px){
                    boolean flag = true;
                    for(Transition transition : place.getPreTransitions()){
                        if(!MCS.contains(transition.getID()- places.size())){
                            flag = false;
                            break;
                        }
                    }
                    if(flag){
                        Poff.add(place);
                    }
                }
                //---
                //Następnie idziemy poziom dalej. Sprawdzamy wszystkie tranzycje, które mają jako swoje miejsce wejściowe przynajmniej jedno miejsce ze zbioru Poff.
                HashSet<Transition> Toff = new HashSet<Transition>();
                for(Place place : Poff){
                    Toff.addAll(place.getPostTransitions());
                }

                ArrayList<Integer> indexes = new ArrayList<>();
                for(Transition transition : Toff){
                    indexes.add(transition.getID()-places.size());
                }
                for(Place place: Poff){
                    indexes.add(place.getID());
                }
                MCSsetsindexes.put(rankmsg.toString(),indexes);
                MCSsets.put(rankmsg.toString(),new ArrayList<Integer>(Arrays.asList(Toff.size(),Poff.size())));
                MCStrans.put(rankmsg.toString(),MCS);
                //logField1stTab.append(msg+"(Toff: "+Toff.size()+", Poff: "+Poff.size()+")\n");

                counter++;
                Px.clear();
                Poff.clear();
                Toff.clear();
            }


            LinkedHashMap<String, Integer> lhm = new LinkedHashMap<String, Integer>();
            Map<String, ArrayList<Integer>> MCSsetscp = new HashMap<String, ArrayList<Integer>>(MCSsets);
            int lim = MCSsets.size();
            for (int i = 0; i <lim; i++) {
                int min = transitions.size();
                int min_alt = transitions.size();

                String rem = null;
                for (Map.Entry<String, ArrayList<Integer>> entry : MCSsetscp.entrySet()) {
                    int other = 0;
                    if(TorP.getSelectedIndex()!=1){other=1;}
                    if(entry.getValue().get(TorP.getSelectedIndex())<min){
                        rem=entry.getKey();
                        min = entry.getValue().get(TorP.getSelectedIndex());
                        min_alt = entry.getValue().get(other);

                    } else if (entry.getValue().get(TorP.getSelectedIndex()) == min) {
                        if(entry.getValue().get(other)<min_alt){
                            rem=entry.getKey();
                            min = entry.getValue().get(TorP.getSelectedIndex());
                            min_alt = entry.getValue().get(other);
                        }
                    }
                }
                lhm.put(rem,min);
                MCSsetscp.remove(rem);
            }
            ArrayList<Integer> ImportantTrans = new ArrayList<>();
            if(!importantTrans.getText().isEmpty()) {
                try{
                    if(importantTrans.getText().contains(",")){
                        String[] parseImportantTrans = importantTrans.getText().split(",");
                        for (String elem : parseImportantTrans) {
                            ImportantTrans.add(Integer.parseInt(elem));
                        }
                    } else{
                        ImportantTrans.add(Integer.parseInt(importantTrans.getText()));
                    }
                } catch (Exception ex) {
                    if(!deniedTransParseFailed) {
                        JOptionPane.showMessageDialog(null, "Error while parsing text field, proper syntax is: 1,2,3,4,5\n\n"+
                                        " Important transitions will be ignored for this analysis."
                                , "Important transitions field error", JOptionPane.ERROR_MESSAGE);
                    }
                    deniedTransParseFailed = true;
                }

            }
            //logField1stTab.append("\t=== Ranking zbiorów MCS ===\n");
            if(!description.isSelected() && !ID.isSelected())// zwykłe wypisanie MCS#x [t1,t3] (Toff: 3, Poff 5)
                for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                    String[] mcs = mapElement.getKey().split("\\[");
                    for(Integer trans : MCSsetsindexes.get(mapElement.getKey()).subList(0,MCSsets.get(mapElement.getKey()).get(0))){
                        if(ImportantTrans.contains(trans)){
                            logField1stTab.append("(DENIED)_");
                        }
                    }
                    String deniedMCStrans = mcs[1];
                    for (int impTrans : ImportantTrans){
                        if(mcs[1].contains(Integer.toString(impTrans))){
                            deniedMCStrans = deniedMCStrans.replace("t"+Integer.toString(impTrans),"(DENIED)_t"+Integer.toString(impTrans));
                        }
                    }
                    logField1stTab.append(String.format("%-8s %-20s (T_off: %d, P_off %d)", mcs[0],"["+deniedMCStrans, MCSsets.get(mapElement.getKey()).get(0),MCSsets.get(mapElement.getKey()).get(1)));
                    //logField1stTab.append(mapElement.getKey()+"(Toff: " +MCSsets.get(mapElement.getKey()).get(0)+", Poff: "+MCSsets.get(mapElement.getKey()).get(1)+")");

                    logField1stTab.append("\n");
                }
            else if ((!description.isSelected() && ID.isSelected())) {//  wypisanie z id; MCS#x [t1,t3] (Toff: [t2, t54 ,t65], Poff: [p5])
                int liczba_wynikow = 20;
                for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                    //if(liczba_wynikow==0){break;}
                    String[] mcs = mapElement.getKey().split("\\[");
                    String deniedMCStrans = mcs[1];
                    for (int impTrans : ImportantTrans){
                        if(mcs[1].contains(Integer.toString(impTrans))){
                            deniedMCStrans = deniedMCStrans.replace("t"+Integer.toString(impTrans),"(DENIED)_t"+Integer.toString(impTrans));
                        }
                    }
                    logField1stTab.append(String.format("%-8s %-20s (T_off: [",mcs[0],"["+deniedMCStrans));

                    //String lowerMCSindexes = mcs[1].replace("t", "$t_{").replace(",","}$,").replace("]","}$");
//                    for (int impTrans : ImportantTrans){
//                        if(lowerMCSindexes.contains(Integer.toString(impTrans)));{
//                            lowerMCSindexes = lowerMCSindexes.replace("$t_{"+Integer.toString(impTrans),"(DENIED)_t_{"+Integer.toString(impTrans));
//                        }
//                    }
                    //logField1stTab.append(mcs[0].split("#")[1]+" & "+lowerMCSindexes+" & ");
                    if(MCSsets.get(mapElement.getKey()).get(0)>0){//jeśli wyłączonych tranzycji jest wiecej niz 0
                        for(int i= 0; i<MCSsets.get(mapElement.getKey()).get(0); i++){
                            if(ImportantTrans.contains(MCSsetsindexes.get(mapElement.getKey()).get(i))){
                                logField1stTab.append("(DENIED)_");
                            }
                            logField1stTab.append("t"+toSubscript(Integer.toString(MCSsetsindexes.get(mapElement.getKey()).get(i))));
                            //logField1stTab.append("$t_{"+MCSsetsindexes.get(mapElement.getKey()).get(i)+"}$");

                            if(i!= MCSsets.get(mapElement.getKey()).get(0)-1){
                                logField1stTab.append(", ");
                            }else{
                                logField1stTab.append("], P_off: [");
                                //logField1stTab.append(" & ");
                            }
                        }
                    }else{
                        logField1stTab.append("], P_off: [");
                        //logField1stTab.append(" - & ");
                    }
                    if(MCSsets.get(mapElement.getKey()).get(1)>0){
                        for(int i= MCSsets.get(mapElement.getKey()).get(0); i<MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1); i++){
                            logField1stTab.append("p"+toSubscript(Integer.toString(MCSsetsindexes.get(mapElement.getKey()).get(i))));
                            //logField1stTab.append("$p_{"+MCSsetsindexes.get(mapElement.getKey()).get(i)+"}$");
                            if(i!= MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1)-1){
                                logField1stTab.append(", ");
                            }else{
                                logField1stTab.append("])");
                                //logField1stTab.append(" & & & \\\\");
                            }
                        }
                    }
                    else{
                        logField1stTab.append("])");
                        //logField1stTab.append(" - & & & \\\\");
                    }
                    logField1stTab.append("\n");
                    liczba_wynikow--;
                }
                logField1stTab.append("\n");
            } else if ((description.isSelected() && !ID.isSelected())) {// to juz zbyt skomplikowane na demo
                for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                    String MCSname = mapElement.getKey().split(" \\[")[0];
                    logField1stTab.append(MCSname+"[ \n");
                    for(Integer trans : MCStrans.get(mapElement.getKey())){
                        if(ImportantTrans.contains(trans)){
                            logField1stTab.append("\t(DENIED)_"+transitions.get(trans).getName()+";\n");
                        }
                        else {
                            logField1stTab.append("\t" + transitions.get(trans).getName() + ";\n");
                        }
                    }
                    logField1stTab.append("]\n");

                    logField1stTab.append("(T_off: [\n");
                    if(MCSsets.get(mapElement.getKey()).get(0)>0){
                        for(int i= 0; i<MCSsets.get(mapElement.getKey()).get(0); i++){
                            if(ImportantTrans.contains(MCSsetsindexes.get(mapElement.getKey()).get(i))){
                                logField1stTab.append("\t(DENIED)_");
                            }else{
                                logField1stTab.append("\t");
                            }
                            logField1stTab.append(""+transitions.get(MCSsetsindexes.get(mapElement.getKey()).get(i)).getName()+";");

                            logField1stTab.append("\n");

                            if(i== MCSsets.get(mapElement.getKey()).get(0)-1){
                                logField1stTab.append("], P_off: [\n");
                            }
                        }
                    }else{
                        logField1stTab.append("], P_off: [\n");
                    }
                    if(MCSsets.get(mapElement.getKey()).get(1)>0){
                        for(int i= MCSsets.get(mapElement.getKey()).get(0); i<MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1); i++){
                            logField1stTab.append("\t"+places.get(MCSsetsindexes.get(mapElement.getKey()).get(i)).getName()+";\n");
                            if(i == MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1)-1){
                                logField1stTab.append("])");
                            }
                        }
                    }
                    else{
                        logField1stTab.append("])");
                    }
                    logField1stTab.append("\n\n");

                }
            } else if ((description.isSelected() && ID.isSelected())) {//to tak samo + ID w subscripcie
                for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                    String MCSname = mapElement.getKey().split(" \\[")[0];
                    logField1stTab.append(MCSname+"[ \n");
                    for(Integer trans : MCStrans.get(mapElement.getKey())){
                        if(ImportantTrans.contains(trans)){
                            logField1stTab.append("\t(DENIED)_t"+toSubscript(trans.toString())+"_"+transitions.get(trans).getName()+";\n");
                        }
                        else{
                            logField1stTab.append("\tt"+toSubscript(trans.toString())+"_"+transitions.get(trans).getName()+";\n");
                        }
                    }

                    logField1stTab.append("]\n");

                    logField1stTab.append("(T_off: [\n");
                    if(MCSsets.get(mapElement.getKey()).get(0)>0){
                        for(int i= 0; i<MCSsets.get(mapElement.getKey()).get(0); i++){
                            if(ImportantTrans.contains(MCSsetsindexes.get(mapElement.getKey()).get(i))){
                                logField1stTab.append("\t(DENIED)_");
                            }else{
                                logField1stTab.append("\t");
                            }
                            logField1stTab.append("t"+toSubscript(MCSsetsindexes.get(mapElement.getKey()).get(i).toString())+"_"+
                                    transitions.get(MCSsetsindexes.get(mapElement.getKey()).get(i)).getName()+";");

                            logField1stTab.append("\n");
                            if(i== MCSsets.get(mapElement.getKey()).get(0)-1){
                                logField1stTab.append("], P_off: [\n");
                            }
                        }
                    }else{
                        logField1stTab.append("], P_off: [\n");
                    }
                    if(MCSsets.get(mapElement.getKey()).get(1)>0){
                        for(int i= MCSsets.get(mapElement.getKey()).get(0); i<MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1); i++){
                            logField1stTab.append("\tp"+toSubscript(MCSsetsindexes.get(mapElement.getKey()).get(i).toString())+"_"+
                                    places.get(MCSsetsindexes.get(mapElement.getKey()).get(i)).getName()+";\n");
                            if(i == MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1)-1){
                                logField1stTab.append("])");
                            }
                        }
                    }
                    else{
                        logField1stTab.append("])");
                    }
                    logField1stTab.append("\n\n");

                }
            }
            MCSsets.clear();
            MCSsetscp.clear();
            MCSsetsindexes.clear();
            lhm.clear();
//            logField1stTab.append("\\hline\n" +
//                    "\\end{longtable}\n\n");
        }
    }

    /**
     * Algorytm bazujący na t-invariantach, który sortuje wyniki po liczbie wyłączonych tranzycji w sieci.
     * Zastąpiony przez rankingByInvariantsThenTransitions
     *
     * Opis z pracy dyplomowej:
     * Przycisk Invariant Ranking jest odpowiedzialny za uruchomienie kolejnego z algorytmów.
     * Ten algorytm tworzy ranking zbiorów MCS dla każdej z tranzycji badanego modelu w oparciu
     * o wpływ zbiorów MCS na jego t-inwarianty. Na początku tworzony jest zbiór A, który zawiera
     * te t-inwarianty x, których wsparcie supp(x) zawiera choć jedną tranzycję z danego zbioru MCS.
     * Dalej tworzony jest zbiór T_x, czyli zbiór tych wszystkich tranzycji, które zostały wyłączone przez
     * to, że zniknęły te t-inwarianty, które zawierały we wsparciach jakąkolwiek tranzycje z MCS. Sprowadza
     * się to do zebrania tranzycji ze wsparcia wyłączonych przez MCS t-inwariantów. Kryterium
     * oceny zbioru MCS w tym algorytmie jest rozmiar zbioru T_x, czyli zbioru tranzycji ze wsparcia
     * tych t-inwariantów, które zostały wyłączone przez dany MCS. Im mniejszy zbiór T_x tym lepiej.
     * Wyniki działania tego algorytmu zawierają rozmiar zbioru T_x oraz procent wszystkich tranzycji
     *jaki stanowi zbiór T_x.
     */
    private void orgInvTransRankingAlg(){
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();

        //for(int transitionIndex : il_trans){
        int selectedTrans = transBox.getSelectedIndex();

        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {
            if(selectedTrans > 0) {
                if(selectedTrans-1 != transitionIndex) {
                    continue;
                }
            }

            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);
            if (dataVector == null || dataVector.isEmpty()) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            if (invariantsMatrix == null || invariantsMatrix.isEmpty()) {
                logField1stTab.append(" *** t-invariants set empty! ***\n");
            }

            logField1stTab.append(" *** MCS sets for transition ID: " + transitionIndex + " ***\n");

            Map<Integer,ArrayList<Integer>> B  =  new  HashMap<Integer,ArrayList<Integer>>();//t-inwarianty,które pozostały, bo nie zawierają we wsparciach ani jednej tranzycji z danego MCS
            Map<Integer,ArrayList<Integer>> A  =  new  HashMap<Integer,ArrayList<Integer>>();//t-inwarianty, których wsparcie zawiera choć jedną tranzycję z danego zbioru MCS
            Set<Integer> Tb = new HashSet<>();//zostawione tranzycje
            Set<Integer> Tx = new HashSet<>();//wyłączone tranzycje

            Map<String,Integer> MCSrank  =  new  HashMap<String,Integer>();
            Map<String,Set<Integer>> MCSsets  =  new  HashMap<String,Set<Integer>>();
            int MCScounter = 0;

            for(ArrayList<Integer> MCSset : dataVector){

                StringBuilder rankmsg = new StringBuilder("MCS#");
                rankmsg.append(MCScounter).append(" ").append("[");
                for (int el : MCSset) {
                    //toSubscript(Integer.toString(el))
                    rankmsg.append("t").append(el).append(", ");
                }
                rankmsg.append("]");
                rankmsg = new StringBuilder(rankmsg.toString().replace(", ]", "]"));

                int counter = 0;
                for(ArrayList<Integer> tInv : invariantsMatrix){
                    ArrayList<Integer> tInvSupp = InvariantsTools.getSupport(tInv);
                    boolean flag = true;
                    for(Integer MCStrans : MCSset){
                        if(tInvSupp.contains(MCStrans)){
                            flag = false;
                            A.put(counter,tInvSupp);
                            Tx.addAll(tInvSupp);
                            break;
                        }
                    }
                    if(flag){
                        B.put(counter,tInvSupp);
                        Tb.addAll(tInvSupp);
                    }
                    counter++;
                }
                rankmsg.append(" dis-inv: ").append(A.size()); //liczba wyłączonych inwariantów

                MCSrank.put(rankmsg.toString(), Tx.size());
                MCSsets.put(rankmsg.toString(), new HashSet<Integer>(Tx));

                A.clear();
                B.clear();
                Tx.clear();
                Tb.clear();
                MCScounter++;
            }

            LinkedHashMap<String, Integer> lhm = new LinkedHashMap<String, Integer>();
            int lim = MCSrank.size();
            for (int i = 0; i <lim; i++) {
                Integer min = transitions.size()+1;
                String rem = "";
                for (Map.Entry<String, Integer> entry : MCSrank.entrySet()) {
                    if(entry.getValue()<min){
                        rem = entry.getKey();
                        min = entry.getValue();
                    }
                }
                lhm.put(rem,min);
                MCSrank.remove(rem);
            }
            ArrayList<Integer> ImportantTrans = new ArrayList<>();
            if(importantTrans.getText().length()>0) {
                if(importantTrans.getText().contains(",")){
                    String[] parseImportantTrans = importantTrans.getText().split(",");
                    for (String elem : parseImportantTrans) {
                        ImportantTrans.add(Integer.parseInt(elem));
                    }
                }
                else{
                    ImportantTrans.add(Integer.parseInt(importantTrans.getText()));
                }
            }
            int liczba_wynikow = 20;
            for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                String[] mcs = mapElement.getKey().split("\\[");

                int inv = -1;
                try {
                    int tmp1 = mcs[1].indexOf(":");
                    String check = mcs[1].substring(tmp1+1);
                    check = check.replace(" ","");
                    int num = Integer.parseInt(check);
                    inv = num;

                    tmp1 = mcs[1].indexOf("]");
                    mcs[1] = mcs[1].substring(0, tmp1+1);
                } catch (Exception e) {

                }


                float percentage = (float) (mapElement.getValue() * 100) / transitions.size();
                String pSign = "%";
                for(Integer trans: ImportantTrans){
                    if(MCSsets.get(mapElement.getKey()).contains(trans)){
                        logField1stTab.append("(DENIED)_");
                        break;
                    }
                }
                logField1stTab.append(String.format("%-8s %-30s %s%d (%.2f%s) %s%d)",
                        mcs[0],"["+mcs[1],"(Dis. trans.: ", mapElement.getValue(), percentage, pSign, " | Dis. inv.: ", inv));
                logField1stTab.append("\n");
                liczba_wynikow--;
            }
            lhm.clear();
            MCSrank.clear();
            MCSsets.clear();
        }
    }

    /**
     *
     *
     * Zadaniem jest wybranie jednego najlepszego zbioru MCS dla każdej tranzycji badanego
     * modelu. Ten algorytm jest w zasadzie rozwinięciem pierwszego algorytmu, ponieważ również
     * tworzy zbiory P_off oraz T_off , ale zamiast tworzyć ranking minimalnych zbiorów odcinających to
     * wybiera teoretycznie najlepszy zbiór dla danej tranzycji. Według kryteriów algorytmu, najlepszy
     * zbiór to taki, którego tranzycje bezpośrednio wyłączają najmniej miejsc i tranzycji w sieci, czyli
     * zbiory P_off i T_off są najmniejsze. Ponadto, w sytuacji kiedy trafi się jeden lub więcej zbiorów o
     * takim samym wyniku to sprawdzana jest liczba tranzycji w każdym ze zbiorów MCS. Im mniejszy
     * jest zbiór MCS tym lepiej. Oczywiście w tym algorytmie również istnieje możliwość oznaczenia
     * ważnych tranzycji, które mają pozostać włączone. Jeśli potencjalnie najlepszy zbiór MCS wyłącza
     * taką tranzycję, to obok niego pojawi się kolejny najlepszy zbiór, który takiej tranzycji nie wyłącza.
     * Dodatkowo, tam gdzie to możliwe, algorytm stara się nie wybierać jako najlepszy zbiór trywialnego
     * przypadku, gdzie MCS to jedno-elementowy zbiór składający się z tranzycji, dla której szukany
     * jest najlepszy zbiór."
     */
    void trzy(){
        places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();

        //for(int transitionIndex : il_trans){
        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {
            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);

            if (dataVector == null || dataVector.isEmpty()) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            logField1stTab.append(" *** Best MCS for the transition ID: " + transitionIndex + " ***\n");
            Map<String, Integer> MCSsets = new HashMap<String, Integer>();
            Map<String, ArrayList<Integer>> indexes = new HashMap<>();

            int counter = 0;
            for(ArrayList<Integer>MCS : dataVector){

                StringBuilder rankmsg = new StringBuilder("MCS#");
                StringBuilder msg;
                //logField1stTab.append("MCS#" + counter + " ");
                rankmsg.append(counter).append(" ").append("[");
                msg = new StringBuilder("[");
                for (int el : MCS) {
                    //toSubscript(Integer.toString(el))
                    msg.append("t").append(el).append(", ");
                    rankmsg.append("t").append(el).append(", ");
                }
                msg.append("] : ");
                rankmsg.append("] ");
                msg = new StringBuilder(msg.toString().replace(", ]", "]"));
                rankmsg = new StringBuilder(rankmsg.toString().replace(", ]", "]"));

                //Dla każdego MCS sprawdzamy, do ilu bezpośrednio miejsc prowadzą łuki z jego tranzycji. Niech to będzie zbiór Px, czyli podzbiór zbioru P miejsc sieci.
                //---
                HashSet<Place> Px = new HashSet<Place>();
                for(Integer MCStransition : MCS){
                    Transition transition = transitions.get(MCStransition);
                    Px.addAll(transition.getPostPlaces());
                }
                //---
                //Teraz sprawdzamy, ile miejsc z Px nie posiada żadnych innych łuków wejściowych, tj. innych niż te wychodzące z tranzycji ze zbioru MCS.
                //Niech to będzie podzbiór Poff, czyli podzbiór zbioru Px.
                //---
                HashSet<Place> Poff = new HashSet<Place>();
                for(Place place : Px){
                    boolean flag = true;
                    for(Transition transition : place.getPreTransitions()){
                        if(!MCS.contains(transition.getID()- places.size())){
                            flag = false;
                            break;
                        }
                    }
                    if(flag){
                        Poff.add(place);
                    }
                }
                //---
                //Następnie idziemy poziom dalej. Sprawdzamy wszystkie tranzycje, które mają jako swoje miejsce wejściowe przynajmniej jedno miejsce ze zbioru Poff.
                HashSet<Transition> Toff = new HashSet<Transition>();
                for(Place place : Poff){
                    Toff.addAll(place.getPostTransitions());
                }
                ArrayList<Integer> ids = new ArrayList<>();
                for(Transition trans : Toff){
                    ids.add(trans.getID()-places.size());
                }
                if(!MCS.contains(transitionIndex)){
                    indexes.put(rankmsg.toString(),new ArrayList<Integer>(ids));
                    MCSsets.put(rankmsg.toString(),Poff.size()+Toff.size());
                }else if(dataVector.size() == 1){
                    indexes.put(rankmsg.toString(),new ArrayList<Integer>(ids));
                    MCSsets.put(rankmsg.toString(),Poff.size()+Toff.size());
                }

                //logField1stTab.append(msg+"(Toff: "+Toff.size()+", Poff: "+Poff.size()+")\n");

                counter++;
                ids.clear();
                Px.clear();
                Poff.clear();
                Toff.clear();
            }
            ArrayList<Integer> ImportantTrans = new ArrayList<>();
            if(importantTrans.getText().length()>0) {
                if(importantTrans.getText().contains(",")){
                    String[] parseImportantTrans = importantTrans.getText().split(",");
                    for (String elem : parseImportantTrans) {
                        ImportantTrans.add(Integer.parseInt(elem));
                    }
                }
                else{
                    ImportantTrans.add(Integer.parseInt(importantTrans.getText()));
                }
            }
            int lim = MCSsets.size();
            int min = transitions.size();
            int min_allowed = transitions.size();
            String rem_allowed = null;
            String rem = null;
            int num_of_mcs_trans = 4;
            int num_of_mcs_trans_allowed = 4;
            for (Map.Entry<String, Integer> entry : MCSsets.entrySet()) {
                int mcsSetSize = Arrays.asList(entry.getKey().split("#")[1].split(",")).size();
                if(entry.getValue()<min){
                    rem = entry.getKey();
                    min = entry.getValue();
                    num_of_mcs_trans = mcsSetSize;
                } else if (entry.getValue().equals(min) && num_of_mcs_trans>mcsSetSize ) {
                    rem = entry.getKey();
                    min = entry.getValue();
                    num_of_mcs_trans = mcsSetSize;
                }
                boolean contains_denied = false;
                for (Integer trans : indexes.get(entry.getKey())){
                    if(ImportantTrans.contains(trans)){
                        contains_denied = true;
                        break;
                    }
                }
                if(!contains_denied && entry.getValue()<min_allowed){
                    rem_allowed = entry.getKey();
                    min_allowed = entry.getValue();
                    num_of_mcs_trans_allowed = mcsSetSize;
                } else if (!contains_denied && entry.getValue()<min_allowed && num_of_mcs_trans_allowed>mcsSetSize ) {
                    rem_allowed = entry.getKey();
                    min_allowed = entry.getValue();
                    num_of_mcs_trans = mcsSetSize;
                }
            }

            double percentage= (double)min/(transitions.size()+places.size())*100;
            String result = String.format("%.2f", percentage);
            double percentage_allowed = (double)min_allowed/(transitions.size()+places.size())*100;
            String result_allowed = String.format("%.2f", percentage_allowed);
            boolean flag = true;

            for (Integer trans : indexes.get(rem)){
                if(ImportantTrans.contains(trans)){
                    if(Objects.equals(rem_allowed, null)){
                        logField1stTab.append("Ważne tranzycje uniemożliwiają pokazanie najlepszego MCS\n");
                    }
                    else{
                        //MCS#21 [t30, t31] : 0/137, 0,00%
                        logField1stTab.append("(DENIED)_"+rem+": "+min+"/"+(transitions.size()+places.size())+", "+result+"% || "
                                +rem_allowed+": "+min_allowed+"/"+(transitions.size()+places.size())+", "+result_allowed+"%\n");
                        //String lowerMCSindexes = rem_allowed.split("#")[1].replace("t", "$t_{").replace(",","}$,").replace("]","}$");
                        //logField1stTab.append("(DENIED)\\_"+transitionIndex+" & "+lowerMCSindexes+" & "+min_allowed+"/"+(transitions.size()+places.size())+" & "+ result_allowed+" &&& \\\\");
                        //rem_allowed.split("#")[1]
                        flag = false;
                        break;
                    }
                }
            }
            if (flag)
            {
                if(!Objects.equals(rem_allowed, null)){

                    logField1stTab.append(rem + ": " + min + "/" + (transitions.size() + places.size()) + ", " + result + "%\n");
                    //rem.split("#")[1].split("\\[")[1].split("]")[0]
//                    String lowerMCSindexes = rem.split("#")[1].split("\\[")[1].replace("t", "$t_{").replace(",","}$,").replace("]","}$");
//                    logField1stTab.append(transitionIndex+" & "+rem.split("\\[")[0].split("#")[1]+" & "+lowerMCSindexes+" & "+min+"/"+(transitions.size()+places.size())+" & "+ result +" &&& \\\\");
                }
            }
            logField1stTab.append("\n");
            MCSsets.clear();
            indexes.clear();

        }
    }
}