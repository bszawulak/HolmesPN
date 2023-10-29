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

    int windowWidth = 1280;
    int windowHeight = 800;


    JComboBox<String> TorP;
    JCheckBox description;
    JCheckBox ID;

    JComboBox<String> transBox;
    JComboBox<String> mcsBox;
    JTextField importantTrans;
    ArrayList<Transition> transitions = null;
    ArrayList<Place> places = null;

    ArrayList<ArrayList<Integer>> X = null;

    MCSDataMatrix mcsd = null;

    ArrayList<Integer> angio_trans = new ArrayList<Integer>(Arrays.asList(33,40,50,29,69,27));
    ArrayList<Integer> hyper_trans = new ArrayList<Integer>(Arrays.asList(2,42,44,48,49,60));
    ArrayList<Integer> il_trans = new ArrayList<Integer>(Arrays.asList(18,28,66));
    ArrayList<Integer> athero_trans = new ArrayList<Integer>(Arrays.asList(11,24,41,87,101,109));

    int num_of_results = 100;

    public HolmesMCSanalysis() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (641103817) | Exception:  " + ex.getMessage(), "error", true);

        }
        this.ego = this;
        setVisible(false);
        this.setTitle("Analiza MCS");
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
        if(transitions != null && transitions.size()>0) {
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
        notePad.addTextLine("(Toff: [\n", "text");

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


    private void jeden(){


        places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();

        //for(int transitionIndex : il_trans){
        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {
            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);

            if (dataVector == null || dataVector.size() == 0 ) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            logField1stTab.append(" *** Zbiory MCS dla tranzycji o numerze: " + transitionIndex + ": ***\n");
//            logField1stTab.append("\\begin{longtable}{cccclll}\n" +
//                    "\\caption{Wynik działania algorytmu bazującego na przepływie tokenów dla tranzycji "+transitionIndex+".}\\\\\n" +
//                    "\\hline\n" +
//                    "MCS\\# &\n" +
//                    "  \\begin{tabular}[c]{@{}c@{}}Tranzycje wchodzące w\\\\ skład MCS\\end{tabular} &\n" +
//                    "  \\begin{tabular}[c]{@{}c@{}}Wyłączone tranzycje\\\\ ($T_{off}$)\\end{tabular} &\n" +
//                    "  \\begin{tabular}[c]{@{}c@{}}Wyłączone miejsca\\\\ ($T_{off}$)\\end{tabular} &\n" +
//                    "   &\n" +
//                    "   &\n" +
//                    "   \\\\ \\hline\n" +
//                    "\\endfirsthead\n" +
//                    "%\n" +
//                    "\\endhead\n" +
//                    "%\n" +
//                    "\\hline\n" +
//                    "\\endfoot\n" +
//                    "%\n" +
//                    "\\endlastfoot\n" +
//                    "%\n");

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
                    logField1stTab.append(String.format("%-8s %-20s (Toff: %d, Poff %d)", mcs[0],"["+deniedMCStrans, MCSsets.get(mapElement.getKey()).get(0),MCSsets.get(mapElement.getKey()).get(1)));
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
                    logField1stTab.append(String.format("%-8s %-20s (Toff: [",mcs[0],"["+deniedMCStrans));

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
                                logField1stTab.append("], Poff: [");
                                //logField1stTab.append(" & ");
                            }
                        }
                    }else{
                        logField1stTab.append("], Poff: [");
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

                    logField1stTab.append("(Toff: [\n");
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
                        if(ImportantTrans.contains(trans)){
                            logField1stTab.append("\t(DENIED)_t"+toSubscript(trans.toString())+"_"+transitions.get(trans).getName()+";\n");
                        }
                        else{
                            logField1stTab.append("\tt"+toSubscript(trans.toString())+"_"+transitions.get(trans).getName()+";\n");
                        }
                    }

                    logField1stTab.append("]\n");

                    logField1stTab.append("(Toff: [\n");
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
//            logField1stTab.append("\\hline\n" +
//                    "\\end{longtable}\n\n");
        }
    }

    private void dwa(){
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        X = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();

        //for(int transitionIndex : il_trans){
        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {

            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);
            if (dataVector == null || dataVector.size() == 0 ) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            if (X == null || X.size() == 0 ) {
                logField1stTab.append(" *** Brak T-invariantów ***\n");
            }

            logField1stTab.append(" *** Zbiory MCS dla tranzycji o numerze: " + transitionIndex + " ***\n");
//            logField1stTab.append("\\begin{longtable}{@{}cccclll@{}}\n" +
//                    "\\caption{Wynik działania algorytmu bazującego na t-inwariantach dla tranzycji "+transitionIndex+".}\\\\\n" +
//                    "\\toprule\n" +
//                    "MCS\\# &\n" +
//                    "  \\begin{tabular}[c]{@{}c@{}}Tranzycje wchodzące w\\\\ skład MCS\\end{tabular} &\n" +
//                    "  \\begin{tabular}[c]{@{}c@{}}Liczba wyłączonych tranzycji\\\\ w sieci ($T_{x}$)\\end{tabular} &\n" +
//                    "  \\begin{tabular}[c]{@{}c@{}}Procent wyłączonych \\\\ tranzycji (\\%)\\end{tabular} &\n" +
//                    "   &\n" +
//                    "   &\n" +
//                    "   \\\\* \\midrule\n" +
//                    "\\endfirsthead\n" +
//                    "%\n" +
//                    "\\endhead\n" +
//                    "%\n" +
//                    "\\bottomrule\n" +
//                    "\\endfoot\n" +
//                    "%\n" +
//                    "\\endlastfoot\n" +
//                    "%\n");

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
                //logField1stTab.append(rankmsg.toString()+" :   "+Tx.size());
                MCSrank.put(rankmsg.toString(), Tx.size());
                MCSsets.put(rankmsg.toString(), new HashSet<Integer>(Tx));



//                logField1stTab.append("Wylaczone tranzycje:"+Tx.size()+" Zostawione tranzycje:"+Tb.size()+"\n");
//                for(Integer tebe: Tb){
//                    if (Tx.contains(tebe)){
//                        logField1stTab.append(tebe.toString()+",");
//                    }
//                }
//                logField1stTab.append("\n");
                A.clear();
                B.clear();
                Tx.clear();
                Tb.clear();
                MCScounter++;
            }
            //logField1stTab.append(" === Liczba wyłączonych tranzycji przez każdy MCS ===\n");
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
                //if(liczba_wynikow==0){break;}
                String[] mcs = mapElement.getKey().split("\\[");
                int percentage = mapElement.getValue()  * 100 / transitions.size();
                String percent = "%";
                for(Integer trans: ImportantTrans){
                    if(MCSsets.get(mapElement.getKey()).contains(trans)){
                        logField1stTab.append("(DENIED)_");
                        break;
                    }
                }
                //String lowerMCSindexes = mcs[1].replace("t", "$t_{").replace(",","}$,").replace("]","}$");
                //logField1stTab.append(mcs[0].split("#")[1]+" & "+lowerMCSindexes+" & "+mapElement.getValue()+"  & "+percentage+"    &  &  &  \\\\");
//                String deniedMCStrans = mcs[1];
//                for (int impTrans : ImportantTrans){
//                    if(mcs[1].contains(Integer.toString(impTrans))){
//                        deniedMCStrans = deniedMCStrans.replace("t"+Integer.toString(impTrans),"(DENIED)_t"+Integer.toString(impTrans));
//                    }
//                }
                logField1stTab.append(String.format("%-8s %-20s%s%d, %d%s)", mcs[0],"["+mcs[1],"(Tx:", mapElement.getValue(), percentage,percent));
                //logField1stTab.append(String.format("%-8s %-20s%s%d, %d%s)", mcs[0],"["+mapElement.getKey(),"(Tx:", mapElement.getValue(), percentage,percent));
                logField1stTab.append("\n");
                liczba_wynikow--;
            }
            lhm.clear();
            MCSrank.clear();
            MCSsets.clear();
//            logField1stTab.append("* \\bottomrule\n" +
//                    "\\end{longtable}\n\n");
        }

    }

    void trzy(){
        places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
        transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();


//        logField1stTab.append("\\begin{table}[htbp]\n" +
//                "\\caption{Najlepsze zbiory MCS dla każdej z wybranych tranzycji.}\n" +
//                "\\begin{tabular}{@{}ccccclll@{}}\n" +
//                "\\toprule\n" +
//                "ID tranzycji &\n" +
//                "  MCS\\# &\n" +
//                "  \\begin{tabular}[c]{@{}c@{}}Tranzycje wchodzące w\\\\ skład MCS\\end{tabular} &\n" +
//                "  \\begin{tabular}[c]{@{}c@{}}Liczba wyłączonych \\\\węzłów sieci\\end{tabular} &\n" +
//                "  \\begin{tabular}[c]{@{}c@{}}Procent wyłączonych \\\\ węzłów sieci (\\%)\\end{tabular} &\n" +
//                "   &\n" +
//                "   &\n" +
//                "   \\\\ \\midrule\n");

        //for(int transitionIndex : il_trans){
        for(int transitionIndex = 0; transitionIndex<mcsd.getSize(); transitionIndex++) {
            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);

            if (dataVector == null || dataVector.size() == 0 ) {
                //logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                continue;
            }
            logField1stTab.append(" *** Najlepszy MCS dla tranzycji o numerze: " + transitionIndex + " ***\n");
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
//            String deniedMCStrans = rem;
//            String deniedMCStrans_allowed = rem_allowed;


//            for (int impTrans : ImportantTrans){
//                if(rem.contains(Integer.toString(impTrans))){
//                    deniedMCStrans = deniedMCStrans.replace("t"+Integer.toString(impTrans),"(DENIED)_t"+Integer.toString(impTrans));
//                }
//                if(rem_allowed.contains(Integer.toString(impTrans))){
//                    deniedMCStrans_allowed = deniedMCStrans_allowed.replace("t"+Integer.toString(impTrans),"(DENIED)_t"+Integer.toString(impTrans));
//                }
//            }
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
//        logField1stTab.append("\\bottomrule\n" +
//                "\\end{tabular}\n" +
//                "\\end{table}\n\n");
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

        tabbedPane.addTab("MCS", Tools.getResIcon22("/icons/invWindow/tInvIcon.png"), mainPanel1stTab, "T-invariants");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

//        JPanel mainPanel2ndTab = new JPanel();
//        mainPanel2ndTab.setLayout(null); //  ╯°□°）╯︵  ┻━┻
//        JPanel buttonPanelP = createUpperButtonPanel2ndTab(0, 0, windowWidth, 90);
//        JPanel logMainPanelP = createLogMainPanel2ndTab(0, 90, windowWidth, windowHeight-120);
//
//        mainPanel2ndTab.add(buttonPanelP);
//        mainPanel2ndTab.add(logMainPanelP);
//        mainPanel2ndTab.repaint();
//
//
//        tabbedPane.addTab("2nd Tab", Tools.getResIcon22("/icons/invWindow/pInvIcon.png"), mainPanel2ndTab, "P-invariants");
//        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

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
        button1.setText("Token rank");
        button1.setBounds(posX, posY, 150, 40);
        button1.setMargin(new Insets(0, 0, 0, 0));
        button1.setIcon(Tools.getResIcon32("/icons/invWindow/showInvariants.png"));
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

        JButton transRank = new JButton("T-inv rank");
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

        String[] dataT = { "---" };//combobox z tranzycjami
        transBox = new JComboBox<String>(dataT);
        transBox.setBounds(posX+620, posY, 350, 20);
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
        mcsBox.setBounds(posX+620, posY+30, 200, 20);
        mcsBox.setSelectedIndex(0);
        mcsBox.setMaximumRowCount(6);
        mcsBox.removeAllItems();
        mcsBox.addItem("---");
        mcsBox.addActionListener(actionEvent -> {});
        panel.add(mcsBox);

        JButton showMCS = new JButton("Button 888");
        showMCS.setText("Pokaz MCS");
        showMCS.setBounds(posX + 840, posY+30, 130, 40);
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
        mcsOffRank.setText("Off Rank");
        mcsOffRank.setBounds(posX + 1000, posY, 180, 70);
        mcsOffRank.setMargin(new Insets(0, 0, 0, 0));
        mcsOffRank.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
        mcsOffRank.addActionListener(actionEvent -> {
            trzy();
        });
        mcsOffRank.setFocusPainted(false);
        panel.add(mcsOffRank);

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
        logFieldPanel.setBounds(10, 20, width-35, height-75);
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


//    private JPanel createUpperButtonPanel2ndTab(int x, int y, int width, int height) {
//        JPanel panel = new JPanel();
//        panel.setLayout(null);
//        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//        panel.setBounds(x, y, width, height);
//
//        int posX = 10;
//        int posY = 10;
//
//        JButton buttonX = new JButton("<html>Button<br>X</html>");
//        buttonX.setBounds(posX, posY, 110, 60);
//        buttonX.setMargin(new Insets(0, 0, 0, 0));
//        buttonX.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
//        buttonX.addActionListener(actionEvent -> {
//            ;
//        });
//        buttonX.setFocusPainted(false);
//        panel.add(buttonX);
//
//        JButton buttonY = new JButton("<html>Button<br>Y</html>");
//        buttonY.setBounds(posX+120, posY, 110, 60);
//        buttonY.setMargin(new Insets(0, 0, 0, 0));
//        buttonY.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
//        buttonY.addActionListener(actionEvent -> {
//
//        });
//        buttonY.setFocusPainted(false);
//        panel.add(buttonY);
//
//
//
//        return panel;
//    }
//
//    private JPanel createLogMainPanel2ndTab(int x, int y, int width, int height) {
//        JPanel panel = new JPanel();
//        panel.setLayout(null);
//        panel.setBorder(BorderFactory.createTitledBorder("Log window 2nd tab"));
//        panel.setBounds(x, y, width-20, height-50);
//
//        logField2ndTab = new JTextArea();
//        logField2ndTab.setLineWrap(true);
//        //logField2ndTab.setEditable(false);
//        DefaultCaret caret = (DefaultCaret)logField2ndTab.getCaret();
//        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
//
//        JPanel logFieldPanel = new JPanel();
//        logFieldPanel.setLayout(new BorderLayout());
//        logFieldPanel.add(new JScrollPane(logField2ndTab), BorderLayout.CENTER);
//        logFieldPanel.setBounds(10, 20, width-35, height-75);
//        panel.add(logFieldPanel);
//
//        return panel;
//    }
}