package holmes.windows;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.graphpanel.popupmenu.TransitionPopupMenu;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;

import javax.swing.JComboBox;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;


public class HolmesMCSanalysis extends JFrame {
    private GUIManager overlord;
    private JFrame ego;

    private JTextArea logField1stTab;
    private JTextArea logField2ndTab;

    int windowWidth = 1024;
    int windowHeight = 800;

    private JComboBox<String> transitionsResultsCombo;

    JComboBox<String> TorP;
    JCheckBox description;
    JCheckBox ID;

    JTextField importantTrans;
    ArrayList<Transition> transitions = null;
    ArrayList<Place> places = null;

    ArrayList<ArrayList<Integer>> X = null;

    MCSDataMatrix mcsd = null;

    public HolmesMCSanalysis() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (641103817) | Exception:  " + ex.getMessage(), "error", true);

        }
        this.ego = this;
        setVisible(false);
        this.setTitle("MCS Analysis'n'stuff ");
        this.overlord = GUIManager.getDefaultGUIManager();
        //ego = this;

        setLayout(new BorderLayout());
        setSize(new Dimension(windowWidth, windowHeight));
        setLocation(50, 50);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);


        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }
    private static final HashMap<Character, Character> SUBSCRIPT_MAP = new HashMap<Character, Character>() {{
        put('0', '\u2080');
        put('1', '\u2081');
        put('2', '\u2082');
        put('3', '\u2083');
        put('4', '\u2084');
        put('5', '\u2085');
        put('6', '\u2086');
        put('7', '\u2087');
        put('8', '\u2088');
        put('9', '\u2089');
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

    private void jeden(){


        places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {
            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);

            if (dataVector == null || dataVector.size() == 0 ) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            logField1stTab.append(" *** Zbiory MCS dla tranzycji o numerze: " + transitionIndex + ": ***\n");

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
            logField1stTab.append("\t=== Ranking zbiorów MCS ===\n");
            if(!description.isSelected() && !ID.isSelected())// zwykłe wypisanie MCS#x [t1,t3] (Toff: 3, Poff 5)
                for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                    String[] mcs = mapElement.getKey().split("\\[");
                    logField1stTab.append(String.format("%-8s %-20s (Toff: %d, Poff %d)", mcs[0],"["+mcs[1], MCSsets.get(mapElement.getKey()).get(0),MCSsets.get(mapElement.getKey()).get(1)));
                    //logField1stTab.append(mapElement.getKey()+"(Toff: " +MCSsets.get(mapElement.getKey()).get(0)+", Poff: "+MCSsets.get(mapElement.getKey()).get(1)+")");
                    for(Integer trans : MCSsetsindexes.get(mapElement.getKey()).subList(0,MCSsets.get(mapElement.getKey()).get(0))){
                        if(ImportantTrans.contains(trans)){
                            logField1stTab.append(" -!- Zawiera zabronione tranzycje -!-");
                        }
                    }
                    logField1stTab.append("\n");
                }
            else if ((!description.isSelected() && ID.isSelected())) {//  wypisanie z id MCS#x [t1,t3] (Toff: [t2, t54 ,t65], Poff: [p5])
                for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                    String[] mcs = mapElement.getKey().split("\\[");
                    logField1stTab.append(String.format("%-8s %-20s (Toff: [",mcs[0],"["+mcs[1]));
                    if(MCSsets.get(mapElement.getKey()).get(0)>0){
                        for(int i= 0; i<MCSsets.get(mapElement.getKey()).get(0); i++){
                            logField1stTab.append("t"+toSubscript(Integer.toString(MCSsetsindexes.get(mapElement.getKey()).get(i))));
                            if(ImportantTrans.contains(MCSsetsindexes.get(mapElement.getKey()).get(i))){
                                logField1stTab.append(" -!- Zabroniona tranzycja -!-");
                            }
                            if(i!= MCSsets.get(mapElement.getKey()).get(0)-1){
                                logField1stTab.append(", ");
                            }else{
                                logField1stTab.append("], Poff: [");
                            }
                        }
                    }else{
                        logField1stTab.append("], Poff: [");
                    }
                    if(MCSsets.get(mapElement.getKey()).get(1)>0){
                        for(int i= MCSsets.get(mapElement.getKey()).get(0); i<MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1); i++){
                            logField1stTab.append("p"+toSubscript(Integer.toString(MCSsetsindexes.get(mapElement.getKey()).get(i))));
                            if(i!= MCSsets.get(mapElement.getKey()).get(0)+MCSsets.get(mapElement.getKey()).get(1)-1){
                                logField1stTab.append(", ");
                            }else{
                                logField1stTab.append("])");
                            }
                        }
                    }
                    else{
                        logField1stTab.append("])");
                    }
                    logField1stTab.append("\n");
                }
            } else if ((description.isSelected() && !ID.isSelected())) {// to juz zbyt skomplikowane na demo
                for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                    String MCSname = mapElement.getKey().split(" \\[")[0];
                    logField1stTab.append(MCSname+"[ \n");
                    for(Integer trans : MCStrans.get(mapElement.getKey())){
                        logField1stTab.append("\t"+transitions.get(trans).getName()+";\n");
                    }
                    logField1stTab.append("]\n");

                    logField1stTab.append("(Toff: [\n");
                    if(MCSsets.get(mapElement.getKey()).get(0)>0){
                        for(int i= 0; i<MCSsets.get(mapElement.getKey()).get(0); i++){
                            logField1stTab.append("\t"+transitions.get(MCSsetsindexes.get(mapElement.getKey()).get(i)).getName()+";");
                            if(ImportantTrans.contains(MCSsetsindexes.get(mapElement.getKey()).get(i))){
                                logField1stTab.append(" -!- Zabroniona tranzycja -!-");
                            }
                            logField1stTab.append("\n");

                            if(i== MCSsets.get(mapElement.getKey()).get(0)-1){
                                logField1stTab.append("], Poff: [\n");
                            }
                        }
                    }else{
                        logField1stTab.append("], Poff: [\n");
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
                        logField1stTab.append("\tt"+toSubscript(trans.toString())+"_"+transitions.get(trans).getName()+";\n");
                    }
                    logField1stTab.append("]\n");

                    logField1stTab.append("(Toff: [\n");
                    if(MCSsets.get(mapElement.getKey()).get(0)>0){
                        for(int i= 0; i<MCSsets.get(mapElement.getKey()).get(0); i++){
                            logField1stTab.append("\tt"+toSubscript(MCSsetsindexes.get(mapElement.getKey()).get(i).toString())+"_"+
                                    transitions.get(MCSsetsindexes.get(mapElement.getKey()).get(i)).getName()+";");
                            if(ImportantTrans.contains(MCSsetsindexes.get(mapElement.getKey()).get(i))){
                                logField1stTab.append(" -!- Zabroniona tranzycja -!-");
                            }
                            logField1stTab.append("\n");
                            if(i== MCSsets.get(mapElement.getKey()).get(0)-1){
                                logField1stTab.append("], Poff: [\n");
                            }
                        }
                    }else{
                        logField1stTab.append("], Poff: [\n");
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
        }
    }
//Ranking w oparciu o t-inwarianty:
//    -Identyfikujemy te t-inwarianty, których wsparcie zawiera choć jedną tranzycję z danego zbioru MCS.
//      Niech to będzie zbiór A. Niech zbiór X to zbiór wszystkich t-inwariantów. Zbiór B więc to te t-inwarianty,
//      które pozostały, bo nie zawierają we wsparciach ani jednej tranzycji z danego MCS. Czyli podsumowując X = A + B.
//
//    -Dla zbioru t-inwariantów ze zbioru B (czyli tego, którego t-inwarianty nie mają we wsparciach niczego z danego MCSa)
//      sprawdzamy ile one zawierają tranzycji i jakie to tranzycje. Albo jeszcze innymi słowy: z pełnego zbioru T tranzycji sieci tworzymy podzbiór takich tranzycji,
//      które wciąż są w (tych aktywnych) t-inwariantach ze zbioru B. Niech zbiór tych tranzycji to Tb. Tak więc: T = Tb  Tx. Zbiór Tb to zbiór wszystkich tranzycji,
//      które są we wsparciach inwariantów ze zbioru B (czyli wciąż tego, który reprezentuje inwarianty które nie zostały wyłączone przez tranzycje z MCS).
//      Tak więc ten drugi podzbiór nazwany Tx to zbiór tych wszystkich tranzycji, które zostały wyłączone, bo zniknęły t-inwarianty (te z podzbioru A wspomnianego wcześniej),
//      które zawierały we wsparciach jakieś tranzycje z MCS.
//    -Ranking tutaj polega na tym, że im mniejszy rozmiar zbioru Tx, tym lepiej, czyli prezentacja wyników może być w stylu:
//        o	MCS17[t7, t20]  (Tx:  3)
//        o	MCS8[t7, t20]  (Tx:  4)   , itd.
//    -Dodatkowe kryterium oceny:
//        o	Niech będzie można określi pewne tranzycje, których nie chcemy wyłączać. Czyli takie, których nie chcemy widzieć w zbiorze Tx zdefiniowanym powyżej. A
//        nalizę przeprowadzamy co do przecinka jak wyżej, ale dodatkowo w wynikach które algorytm poda będzie napisane (np. na czerwono),
//        że dany MCS zawiera tranzycje zakazane (te które wskazaliśmy i których nie chcemy wyłączać MCSem). Można np. zrobić drugi oddzielny podranking,
//        gdzie będą tylko takie MCS, które wyłączyły coś, czego nie miały.

    private void dwa(){
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        X = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();

        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {

            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);
            if (dataVector == null || dataVector.size() == 0 ) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            if (X == null || X.size() == 0 ) {
                logField1stTab.append(" *** Brak T-invariantów ***\n");
            }

            logField1stTab.append(" *** Zbiory MCS dla tranzycji o numerze: " + transitionIndex + ": ***\n");

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
                for(ArrayList<Integer> tInv : X){
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
                MCSrank.put(rankmsg.toString(), Tx.size());
                MCSsets.put(rankmsg.toString(), Tx);

                A.clear();
                B.clear();
                Tx.clear();
                Tb.clear();
                MCScounter++;
            }
            logField1stTab.append(" === Liczba wyłączonych tranzycji przez każdy MCS ===\n");
            LinkedHashMap<String, Integer> lhm = new LinkedHashMap<String, Integer>();
            int lim = MCSrank.size();
            for (int i = 0; i <lim; i++) {
                Integer min = transitions.size();
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
            for (Map.Entry<String, Integer> mapElement : lhm.entrySet()) {
                String[] mcs = mapElement.getKey().split("\\[");
                logField1stTab.append(String.format("%-8s %-20s%s%d)", mcs[0],"["+mcs[1],"(Tx:", mapElement.getValue()));

                for(Integer trans: ImportantTrans){
                    if(MCSsets.get(mapElement.getKey()).contains(trans)){
                        logField1stTab.append(" -!- Zawiera zabronione tranzycje -!-");
                        break;
                    }
                }
                logField1stTab.append("\n");
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
        JPanel buttonPanelT = createUpperButtonPanel1stTab(0, 0, windowWidth, 90);
        JPanel logMainPanelT = createLogMainPanel1stTab(0, 90, windowWidth, windowHeight-120);

        mainPanel1stTab.add(buttonPanelT);
        mainPanel1stTab.add(logMainPanelT);
        mainPanel1stTab.repaint();

        tabbedPane.addTab("1st Tab", Tools.getResIcon22("/icons/invWindow/tInvIcon.png"), mainPanel1stTab, "T-invariants");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JPanel mainPanel2ndTab = new JPanel();
        mainPanel2ndTab.setLayout(null); //  ╯°□°）╯︵  ┻━┻
        JPanel buttonPanelP = createUpperButtonPanel2ndTab(0, 0, windowWidth, 90);
        JPanel logMainPanelP = createLogMainPanel2ndTab(0, 90, windowWidth, windowHeight-120);

        mainPanel2ndTab.add(buttonPanelP);
        mainPanel2ndTab.add(logMainPanelP);
        mainPanel2ndTab.repaint();


        tabbedPane.addTab("2nd Tab", Tools.getResIcon22("/icons/invWindow/pInvIcon.png"), mainPanel2ndTab, "P-invariants");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

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

        JButton button1 = new JButton("Jenson");
        button1.setText("MCS ranking");
        button1.setBounds(posX, posY, 150, 40);
        button1.setMargin(new Insets(0, 0, 0, 0));
        button1.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        button1.addActionListener(actionEvent -> {
            places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
            transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
            mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();


            if(mcsd.getSize() == 0) {
                logField1stTab.append(" *** BRAK DANYCH MCS! \n");
                return;
            }

            jeden();
        });
        button1.setFocusPainted(false);
        panel.add(button1);

        description  = new JCheckBox("Pełne nazwy tranzycji/miejsc");
        description.setBounds(posX-5,posY+40, 250,20);
        panel.add(description);

        ID = new JCheckBox("ID tranzycji/miejsc");
        ID.setBounds(posX-5,posY+60, 250,15);
        panel.add(ID);

        JLabel comboDesc = new JLabel("Sortuj po:");
        comboDesc.setBounds(posX+155,posY, 150,20);
        panel.add(comboDesc);
        TorP = new JComboBox<String>();
        TorP.addItem("Toff");
        TorP.addItem("Poff");
        TorP.setBounds(posX+155, posY+20, 70, 20);
        TorP.setSelectedIndex(0);
        panel.add(TorP);

        JLabel transDesc = new JLabel("Ważne tranzycje:");
        transDesc.setBounds(posX+230, posY, 150,20);
        panel.add(transDesc);

        importantTrans = new JTextField();
        importantTrans.setBounds(posX+230, posY+20, 100, 20);
        panel.add(importantTrans);

        JButton transRank = new JButton("Inv rank");
        transRank.setText("Inv rank");
        transRank.setBounds(posX + 480, posY, 130, 70);
        transRank.setMargin(new Insets(0, 0, 0, 0));
        transRank.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
        transRank.addActionListener(actionEvent -> {
            dwa();

        });
        transRank.setFocusPainted(false);
        panel.add(transRank);

        JButton buttonData = new JButton("Button 4");
        buttonData.setText("Wyczyść okno");
        buttonData.setBounds(posX + 340, posY, 130, 70);
        buttonData.setMargin(new Insets(0, 0, 0, 0));
        buttonData.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
        buttonData.addActionListener(actionEvent -> {
            logField1stTab.setText("");

        });
        buttonData.setFocusPainted(false);
        panel.add(buttonData);


        return panel;
    }

    private JPanel createLogMainPanel1stTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("1st tab log window"));
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
        logFieldPanel.setBounds(10, 20, width-35, height-75);
        panel.add(logFieldPanel);

        return panel;
    }


    private JPanel createUpperButtonPanel2ndTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setBounds(x, y, width, height);

        int posX = 10;
        int posY = 10;

        JButton buttonX = new JButton("<html>Button<br>X</html>");
        buttonX.setBounds(posX, posY, 110, 60);
        buttonX.setMargin(new Insets(0, 0, 0, 0));
        buttonX.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        buttonX.addActionListener(actionEvent -> {
            ;
        });
        buttonX.setFocusPainted(false);
        panel.add(buttonX);

        JButton buttonY = new JButton("<html>Button<br>Y</html>");
        buttonY.setBounds(posX+120, posY, 110, 60);
        buttonY.setMargin(new Insets(0, 0, 0, 0));
        buttonY.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        buttonY.addActionListener(actionEvent -> {

        });
        buttonY.setFocusPainted(false);
        panel.add(buttonY);



        return panel;
    }

    private JPanel createLogMainPanel2ndTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("Log window 2nd tab"));
        panel.setBounds(x, y, width-20, height-50);

        logField2ndTab = new JTextArea();
        logField2ndTab.setLineWrap(true);
        //logField2ndTab.setEditable(false);
        DefaultCaret caret = (DefaultCaret)logField2ndTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField2ndTab), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-35, height-75);
        panel.add(logFieldPanel);

        return panel;
    }
}
