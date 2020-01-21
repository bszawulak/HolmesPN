package holmes.analyse;

import holmes.analyse.matrix.IncidenceMatrix;
import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import javax.swing.*;
import java.util.ArrayList;

/**
 * Klasa odpowiedzialna za dekompozycję PN do wybranych typów podsieci
 */
public class SubnetCalculator {

    public static ArrayList<SubNet> functionalSubNets = new ArrayList<>();
    public static ArrayList<SubNet> snetSubNets = new ArrayList<>();
    public static ArrayList<SubNet> tnetSubNets = new ArrayList<>();
    public static ArrayList<SubNet> adtSubNets = new ArrayList<>();
    public static ArrayList<SubNet> tzSubNets = new ArrayList<>();
    public static ArrayList<SubNet> houSubNets = new ArrayList<>();
    public static ArrayList<SubNet> nishiSubNets = new ArrayList<>();
    public static ArrayList<Path> paths = new ArrayList<>();
    //hou
    private static ArrayList<Node> source = new ArrayList<>();
    private static ArrayList<Node> destination = new ArrayList<>();
    private static ArrayList<Node> checkedNodes = new ArrayList<>();
    private static ArrayList<ArrayList<Path>> houResultList = new ArrayList<>();
    private static ArrayList<ArrayList<Path>> nishiResultList = new ArrayList<>();
    private static ArrayList<ArrayList<Path>> tzResultList = new ArrayList<>();
    private static ArrayList<ArrayList<Integer>> ajactive = new ArrayList<>();
    //private static ArrayList<Node> currentPath = new ArrayList<>();

    /**
     * Metoda odpowiedzialna za dekompozycję do podsieci funkcyjnych Zajcewa - nie mylić z sieciami funkcyjnymi
     */

    public static void generateFS() {
        cleanSubnets();
        ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

        while (!allTransitions.isEmpty()) {
            Transition firstTransition = allTransitions.get(0);
            ArrayList<Transition> temporaryList = new ArrayList<>();
            temporaryList.add(firstTransition);
            allTransitions.remove(firstTransition);

            for (Transition transition : allTransitions) {
                ArrayList<Transition> listToAdd = new ArrayList<>();
                if (chceckParalellInputOutPut(transition, temporaryList)) {
                    if (!listToAdd.contains(transition))
                        listToAdd.add(transition);
                }
                temporaryList.addAll(listToAdd);
            }

            functionalSubNets.add(new SubNet(temporaryList,new ArrayList<>()));
            allTransitions.removeAll(temporaryList);
        }


        if(!functionalSubNets.isEmpty())
        {
            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }

    }

    private static boolean chceckParalellInputOutPut(Node newTran, ArrayList<Transition> sub) {
        boolean paralel = false;
        boolean seq = false;
        for (Transition subTran : sub) {
            boolean in = chceckNodeOutIn(newTran.getInNodes(), subTran.getInNodes());
            boolean out = chceckNodeOutIn(newTran.getOutNodes(), subTran.getOutNodes());
            boolean inout = chceckNodeOutIn(newTran.getInNodes(), subTran.getOutNodes());
            boolean outin = chceckNodeOutIn(newTran.getOutNodes(), subTran.getInNodes());

            if (in || out)
                paralel = true;
            if (inout || outin)
                seq = true;
        }

        if (seq)
            return false;
        return paralel;
    }

    private static boolean chceckSeqInputOutPut(Node newPlac, ArrayList<Place> sub) {
        boolean paralel = false;
        boolean seq = false;
        for (Place subPlace : sub) {
            boolean in = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getInNodes());
            boolean out = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getOutNodes());
            boolean inout = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getOutNodes());
            boolean outin = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getInNodes());

            if (in || out)
                paralel = true;
            if (inout || outin)
                seq = true;
        }

        if (paralel)
            return false;
        return seq;
    }

    private static boolean chceckSeqTInputOutPut(Node newPlac, ArrayList<Transition> sub) {
        boolean paralel = false;
        boolean seq = false;
        for (Transition subPlace : sub) {
            boolean in = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getInNodes());
            boolean out = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getOutNodes());
            boolean inout = chceckNodeOutIn(newPlac.getInNodes(), subPlace.getOutNodes());
            boolean outin = chceckNodeOutIn(newPlac.getOutNodes(), subPlace.getInNodes());

            if (in || out)
                paralel = true;
            if (inout || outin)
                seq = true;
        }

        if (paralel)
            return false;
        return seq;
    }


    private static boolean chceckNodeOutIn(ArrayList<Node> newNodes, ArrayList<Node> subNodes) {
        for (Node newNode : newNodes)
            for (Node subNode : subNodes)
                if (newNode.getID() == subNode.getID())
                    return true;

        return false;
    }

    private static boolean chceckNodeOutISceptic(ArrayList<Node> newNodes, ArrayList<Node> subNodes) {
        for (Node newNode : newNodes)
            for (Node subNode : subNodes)
                if (newNode.getID() == subNode.getID())
                    return false;

        return true;
    }

    public static void cleanSubnets(){
        functionalSubNets = new ArrayList<>();
        snetSubNets = new ArrayList<>();
        tnetSubNets = new ArrayList<>();
        adtSubNets = new ArrayList<>();
        tzSubNets = new ArrayList<>();
        houSubNets = new ArrayList<>();
        nishiSubNets = new ArrayList<>();
    }


    public static void generateSnets(){
        cleanSubnets();
        ArrayList<Place> allPlaces = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();

        while (!allPlaces.isEmpty()) {
            Place firstPlace = allPlaces.get(0);
            ArrayList<Place> temporaryList = new ArrayList<>();
            temporaryList.add(firstPlace);
            allPlaces.remove(firstPlace);

            for (Place place : allPlaces) {
                ArrayList<Place> listToAdd = new ArrayList<>();
                if (chceckSeqInputOutPut(place, temporaryList)) {
                    if (!listToAdd.contains(place))
                        listToAdd.add(place);
                }
                temporaryList.addAll(listToAdd);
            }

            snetSubNets.add(new SubNet(new ArrayList<>(),temporaryList));
            allPlaces.removeAll(temporaryList);
        }


        if(!functionalSubNets.isEmpty())
        {
            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    public static void generateTnets(){
        cleanSubnets();
        ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

        while (!allTransitions.isEmpty()) {
            Transition firstTransition = allTransitions.get(0);
            ArrayList<Transition> temporaryList = new ArrayList<>();
            temporaryList.add(firstTransition);
            allTransitions.remove(firstTransition);

            for (Transition transition : allTransitions) {
                ArrayList<Transition> listToAdd = new ArrayList<>();
                if (chceckSeqTInputOutPut(transition, temporaryList)) {
                    if (!listToAdd.contains(transition))
                        listToAdd.add(transition);
                }
                temporaryList.addAll(listToAdd);
            }

            tnetSubNets.add(new SubNet(temporaryList,new ArrayList<>()));
            allTransitions.removeAll(temporaryList);
        }


        if(!tnetSubNets.isEmpty())
        {
            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    public static void generateADT() {
        cleanSubnets();
        ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

        if (GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix()!= null) {
            if (!GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().isEmpty()) {
                ArrayList<ArrayList<Integer>> invMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
                ArrayList<ArrayList<Integer>> nonAssignedRows = new ArrayList<>();

                for (int i = 0; i < invMatrix.get(0).size(); i++) {
                    ArrayList<Integer> newRow = new ArrayList<>();
                    for (int j = 0; j < invMatrix.size(); j++) {
                        newRow.add(invMatrix.get(j).get(i));
                    }
                    nonAssignedRows.add(newRow);
                }

                ArrayList<Integer> listOfusedTransitions = new ArrayList<>();

                for (int i = 0; i < nonAssignedRows.size(); i++) {
                    ArrayList<Integer> newADTset = new ArrayList<Integer>();

                    if (!listOfusedTransitions.contains(i)) {
                        newADTset.add(i);
                        listOfusedTransitions.add(i);
                        for (int j = i; j < nonAssignedRows.size(); j++) {

                            if (checkADT(nonAssignedRows.get(i), nonAssignedRows.get(j))) {
                                if (!listOfusedTransitions.contains(j)) {
                                    listOfusedTransitions.add(j);
                                    newADTset.add(j);
                                }

                            }

                        }
                    }
                    if(!newADTset.isEmpty())
                        adtSubNets.add(new SubNet(newADTset));
                }


                if (!adtSubNets.isEmpty()) {
                    GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
                }
            }else{
                JOptionPane.showMessageDialog(null, "Decomposition can not be processed, because of the lack of invariants!", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(null, "Before determine ADT sets, you need to generate T-invariants.", "WARNING MESSAGE", JOptionPane.WARNING_MESSAGE);
        }

    }

    private static boolean checkADT(ArrayList<Integer> t1 , ArrayList<Integer> t2){
        boolean result = true;
        for(int i = 0 ; i<t1.size();i++){
            if(!t1.get(i).equals(t2.get(i)))
                result = false;
        }

        return result;
    }

    public static void generateTZ(){
        cleanSubnets();
        paths = calculatePaths();

        System.out.println("============================= " + "Liczba ścieżek" + paths.size()+ " ================================");

        int pathCount = 0;
        for (Path path : paths ) {
            ArrayList<Path> localListOfPaths = new ArrayList<>(paths);
            ArrayList<Path> listOfCycles = new ArrayList<>();


            pathCount++;
            System.out.println("pathCount" + pathCount);
            localListOfPaths.remove(path);
            listOfCycles.add(path);
            for (Path p: localListOfPaths
                 ) {
                if(path.endNode == p.startNode)
                {
                    dep(listOfCycles,localListOfPaths);
                }
            }

        }

        ///TESTOWE
/*
        for (int i = 0 ; i <paths.size();i++) {
            ArrayList<Path> testowa = new ArrayList<Path>();
            testowa.add(paths.get(i));
                tzSubNets.add(new SubNet(testowa,1));

        }
*/
        //WŁAŚCIWE


        for (int i = 0 ; i <tzResultList.size();i++) {
            boolean isDouble = false;
            for (int j = i+1 ; j <tzResultList.size();j++) {
                if(tzResultList.get(i).containsAll(tzResultList.get(j))){
                    isDouble = true;
                }

            }

            if(!isDouble){
                tzSubNets.add(new SubNet(tzResultList.get(i),1));
            }
        }

        tzResultList.clear();

        if(!tzSubNets.isEmpty())
        {
            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    public static void generateHou(){
        cleanSubnets();
        paths = calculatePathsHou();

        System.out.println("============================= " + "Liczba ścieżek" + paths.size()+ " ================================");
        ArrayList<Node> inNode = new ArrayList<>();
        ArrayList<Node> outNode = new ArrayList<>();

        for (Path p : paths) {
            if(p.startNode.getInNodes().size()==0 && p.startNode.getOutNodes().size()!=0){
                inNode.add(p.startNode);
            }

            if(p.endNode.getInNodes().size()!=0 && p.endNode.getOutNodes().size()==0){
                outNode.add(p.endNode);
            }
        }

        int pathCount = 0;
        for (Path path : paths ) {
            if(inNode.contains(path.startNode)) {
                ArrayList<Path> localListOfPaths = new ArrayList<>(paths);
                ArrayList<Path> listOfCycles = new ArrayList<>();


                pathCount++;
                System.out.println("pathCount" + pathCount);
                localListOfPaths.remove(path);
                listOfCycles.add(path);
                for (Path p : localListOfPaths
                ) {
                    if (path.endNode == p.startNode) {
                        depHou(listOfCycles, localListOfPaths, outNode);
                    }
                }
            }
        }


        ///TESTOWE
/*
        for (int i = 0 ; i <paths.size();i++) {
            ArrayList<Path> testowa = new ArrayList<Path>();
            testowa.add(paths.get(i));
            houSubNets.add(new SubNet(testowa,1));

        }
*/
        //WŁAŚCIWE


        for (int i = 0 ; i <houResultList.size();i++) {
            boolean isDouble = false;
            for (int j = i+1 ; j <houResultList.size();j++) {
                if(houResultList.get(i).containsAll(houResultList.get(j))){
                    isDouble = true;
                    System.out.println("Dubel");
                }

            }

            if(!isDouble){
                houSubNets.add(new SubNet(houResultList.get(i),1));
            }
        }

        houResultList.clear();

        if(!houSubNets.isEmpty())
        {
            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }


    public static void generateNishi(){
        cleanSubnets();
        paths = calculatePathsHou();

        System.out.println("============================= " + "Liczba ścieżek" + paths.size()+ " ================================");
        ArrayList<Node> inNode = new ArrayList<>();
        ArrayList<Node> outNode = new ArrayList<>();

        for (Path p : paths) {
            if(p.startNode.getInNodes().size()==0 && p.startNode.getOutNodes().size()!=0){
                inNode.add(p.startNode);
            }

            if(p.endNode.getInNodes().size()!=0 && p.endNode.getOutNodes().size()==0){
                outNode.add(p.endNode);
            }
        }

        int pathCount = 0;
        for (Path path : paths ) {
            if(inNode.contains(path.startNode)) {
                ArrayList<Path> localListOfPaths = new ArrayList<>(paths);
                ArrayList<Path> listOfCycles = new ArrayList<>();


                pathCount++;
                System.out.println("pathCount" + pathCount);
                localListOfPaths.remove(path);
                listOfCycles.add(path);
                for(Path paralelPath : paths)
                {
                    if(!(paralelPath.startNode==path.startNode&&paralelPath.endNode==path.endNode)) {
                        if (paralelPath.startNode.getInNodes().size() == 0 && paralelPath.endNode == path.endNode) {
                            localListOfPaths.remove(paralelPath);
                            listOfCycles.add(paralelPath);
                        }
                    }
                }
                for (Path p : localListOfPaths
                ) {
                    if (path.endNode == p.startNode) {
                        depNishi(listOfCycles, localListOfPaths, outNode);
                    }
                }
            }
        }


        ///TESTOWE
/*
        for (int i = 0 ; i <paths.size();i++) {
            ArrayList<Path> testowa = new ArrayList<Path>();
            testowa.add(paths.get(i));
            houSubNets.add(new SubNet(testowa,1));

        }
*/
        //WŁAŚCIWE


        for (int i = 0 ; i <nishiResultList.size();i++) {
            boolean isDouble = false;
            for (int j = i+1 ; j <nishiResultList.size();j++) {
                if(nishiResultList.get(i).containsAll(nishiResultList.get(j))){
                    isDouble = true;
                }

            }

            if(!isDouble){
                nishiSubNets.add(new SubNet(nishiResultList.get(i),1));
            }
        }

        nishiResultList.clear();

        if(!nishiSubNets.isEmpty())
        {
            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }
    }

    private static boolean depHou(ArrayList<Path> used, ArrayList<Path> unUsed, ArrayList<Node> outNodes)
    {
        //is cycle add
        if(outNodes.contains(used.get(used.size()-1).endNode)) {
            //System.out.println("Dodałem");
            houResultList.add(used);
            return true;
        }
        //is not end
        for(int i=1;i<used.size();i++) {
            //na80%b tutaj wychodzi wcześniej rpzed dojściem do końca

            //dodać warunek zabraniający cykli?


            //if (used.get(i).startNode==used.get(used.size()-1).endNode)
            //if (used.get(i).startNode==used.get(used.size()-1).endNode)
             //   return false;
        }
        ArrayList<Path> posible = findPathsFrom(used.get(used.size()-1).endNode,unUsed);
        for (Path pos: posible
        ) {
            ArrayList<Path> NewUsed = new ArrayList<Path>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<Path>(unUsed);
            NewUsed.add(pos);
            NEWunUsed.remove(pos);
            depHou(NewUsed,NEWunUsed,outNodes);
        }

        return true;
    }

    private static boolean depNishi(ArrayList<Path> used, ArrayList<Path> unUsed, ArrayList<Node> outNodes)
    {
        //is cycle add
        if(outNodes.contains(used.get(used.size()-1).endNode)) {
            //System.out.println("Dodałem");
            for(Path paralelPath : paths) {
                if (paralelPath.endNode.getOutNodes().size() == 0 && paralelPath.startNode == paralelPath.startNode) {
                    used.add(paralelPath);
                }
            }
            nishiResultList.add(used);
            return true;
        }
        //is not end
        for(int i=1;i<used.size();i++) {
            //na80%b tutaj wychodzi wcześniej rpzed dojściem do końca

            //dodać warunek zabraniający cykli?


            //if (used.get(i).startNode==used.get(used.size()-1).endNode)
            //if (used.get(i).startNode==used.get(used.size()-1).endNode)
            //   return false;
        }
        ArrayList<Path> posible = findNishiPathsFrom(used.get(used.size()-1).endNode,unUsed);
        for (Path pos: posible
        ) {

            ArrayList<Path> NewUsed = new ArrayList<Path>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<Path>(unUsed);
            NewUsed.add(pos);
            NEWunUsed.remove(pos);

            for(Path paralelPath : paths)
            {
                if((paralelPath.startNode==pos.startNode&&paralelPath.endNode==pos.endNode)&&!paralelPath.equals(pos)) {
                        NEWunUsed.remove(paralelPath);
                        NewUsed.add(paralelPath);
                }else {
                    /*
                    if (paralelPath.endNode.getOutNodes().size() == 0 && paralelPath.startNode == paralelPath.startNode) {
                        NEWunUsed.remove(paralelPath);
                        NewUsed.add(paralelPath);
                    }*/
                }
            }

            depNishi(NewUsed,NEWunUsed,outNodes);
        }

        return true;
    }

    private static boolean dep(ArrayList<Path> used, ArrayList<Path> unUsed)
    {

        //is cycle add
        if(used.get(0).startNode == used.get(used.size()-1).endNode) {
            //System.out.println("Dodałem");
            tzResultList.add(used);
            return true;
        }
        //is not end
        for(int i=1;i<used.size();i++) {
            if (used.get(i).startNode==used.get(used.size()-1).endNode)
                return false;
        }
        ArrayList<Path> posible = findPathsFrom(used.get(used.size()-1).endNode,unUsed);
        for (Path pos: posible
             ) {
            ArrayList<Path> NewUsed = new ArrayList<Path>(used);
            ArrayList<Path> NEWunUsed = new ArrayList<Path>(unUsed);
            NewUsed.add(pos);
            NEWunUsed.remove(pos);
            dep(NewUsed,NEWunUsed);
        }


        return true;
    }

    private static ArrayList<Path> findPathsFrom(Node end, ArrayList<Path> list){
        ArrayList<Path> possible = new ArrayList<>();
        for (Path p: list
             ) {
            if(p.startNode==end)
                possible.add(p);
        }
        return possible;
    }

    private static ArrayList<Path> findNishiPathsFrom(Node end, ArrayList<Path> list){
        ArrayList<Path> possible = new ArrayList<>();
        for (Path p: list
        ) {
            boolean duble = false;
            ArrayList<Path> paralel = new ArrayList<>();
            for (Path n : possible)
            {
                if(n.endNode == p.endNode && n.startNode == p.startNode)
                    duble = true;
            }
            if(p.startNode==end&&!duble)
                possible.add(p);
                /*

                if(possible.isEmpty()) {
                    possible.add(p);
                }
                else {
                    paralel = new ArrayList<>();
                    for (Path n : possible) {
                        if (n.endNode == p.endNode && n.startNode == p.endNode) {

                        } else {
                            paralel.add(p);
                        }
                    }
                }
            possible.addAll(paralel);
                */
        }
        return possible;
    }

    private static  ArrayList<Path> calculatePaths(){
        ArrayList<Path> listOfPaths = new ArrayList<>();
        ArrayList<Node> allNodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
        //while (allNodes.size()>0)
        //{
            ArrayList<Node> listOfStartNodes = new ArrayList<>();
            for (Node n: allNodes) {
                if(n.getOutNodes().size()>1 || n.getInNodes().size()>1)
                {
                    if(!(n.getOutNodes().size()==0 || n.getInNodes().size()==0))
                        listOfStartNodes.add(n);
                }
            }

            for (Node n : listOfStartNodes
                 ) {
                ArrayList<Node> pathList = new ArrayList<Node>();
                pathList.add(n);
                for (Node singeOutNode: n.getOutNodes()) {
                    if(singeOutNode.getName().equals("Transition4")){
                        System.out.println("jestem");
                    }
                    pathList.add(singeOutNode);
                    pathList = getDeeper(singeOutNode,pathList);



                    if(!pathList.isEmpty())
                    {
                        listOfPaths.add(new Path(pathList.get(0),pathList.get(pathList.size()-1),new ArrayList<>(pathList)));
                        pathList.clear();
                        pathList.add(n);
                    }
                }
        }
        return listOfPaths;
    }

    private static  ArrayList<Path> calculatePathsHou(){
        ArrayList<Path> listOfPaths = new ArrayList<>();
        ArrayList<Node> allNodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
        //while (allNodes.size()>0)
        //{
        ArrayList<Node> listOfStartNodes = new ArrayList<>();
        for (Node n: allNodes) {
            if(!((n.getInNodes().size()==1&&n.getOutNodes().size()==1)||(n.getInNodes().size()>0&&n.getOutNodes().size()==0)))//(n.getOutNodes().size()>=1 || n.getInNodes().size()==0)
            {
                    listOfStartNodes.add(n);
            }
        }

        for (Node n : listOfStartNodes
        ) {
            ArrayList<Node> pathList = new ArrayList<Node>();
            pathList.add(n);
            for (Node singeOutNode: n.getOutNodes()) {
                pathList.add(singeOutNode);
                pathList = getDeeper(singeOutNode,pathList);

                if(!pathList.isEmpty())
                {
                    listOfPaths.add(new Path(pathList.get(0),pathList.get(pathList.size()-1),new ArrayList<>(pathList)));
                    pathList.clear();
                    pathList.add(n);
                }
            }
        }
        return listOfPaths;
    }

    /*
    private static ArrayList<Path> getDeeper(Path p, ArrayList<Path> list, int deep){
        //System.out.println("liczba ścieżek " + list.size());
        //list.forEach((n) -> n.path.forEach((k)-> System.out.println(k.getName())));
        //System.out.println("ścieżeka start " + p.startNode);
        //System.out.println("ścieżeka end " + p.endNode);

        System.out.println("============================= " + deep + " ================================");

        if(list.size()<paths.size()) {

            boolean inproper = false;
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i).startNode == list.get(list.size() - 1).endNode) {
                    System.out.println("Nieprawidłowa");
                    return new ArrayList<>();
                }
            }

            if (list.get(0).startNode == list.get(list.size() - 1).endNode) {
                //tzSubNets.add(new SubNet(new ArrayList<>(list),0));
                tzResultList.add(new ArrayList<Path>(list));
                System.out.println("DODAAAAAAAAAAAAAAAAAAAAAŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁŁ");
                //return list;
            } else {
                ArrayList<Path> unusedPaths = new ArrayList<>(paths);
                unusedPaths.removeAll(list);
                if(unusedPaths.isEmpty()){
                    System.out.println("++++++++++++++++++ " + "Użył wszysto" + " ++++++++++++++++++++++");
                    return new ArrayList<>();
                }
                else
                {
                for (Path k : unusedPaths) {
                    if (!list.contains(k))
                        if (k.startNode == p.endNode) {
                            list.add(k);
                            list = getDeeper(k, new ArrayList<>(list), deep++);
                            //if(countNumberOfPathWithIdenticalInOut(k.startNode,k.endNode)>1)
                            list.remove(k);
                            //return list;//nie czyści listy dla niektórych
                        }

                }
                }
                //zabezpieczenie na źródłowe

            }
            return list;//new ArrayList<>();
        }
        else
        {
            System.out.println("============================= " + "Odcięcie" + " ================================");
        }

        return new ArrayList<>();
    }


    private static int countNumberOfPathWithIdenticalInOut(Node start, Node end){
        int count =0;
        for (Path k : paths ) {
            if(k.startNode==start&&k.endNode==end){
                count++;
            }

        }
        System.out.println("-------->" +count);
        return count;
    }
*/

    private static ArrayList<Node> getDeeper(Node n, ArrayList<Node> list){

        if(n.getOutNodes().size()>1||n.getInNodes().size()>1){
            return list;
        }
        else {
            //zabezpieczenie na źródłowe
            if(!n.getOutNodes().isEmpty()) {
                list.add(n.getOutNodes().get(0));
                list = getDeeper(n.getOutNodes().get(0), list);
            }
        }

        return list;
    }
/*

    private static boolean checkSubnets(SubNet sn1 ,SubNet sn2)
    {
        ArrayList<Transition> tran1 = sn1.getSubTransitions();
        ArrayList<Transition> tran2 = sn2.getSubTransitions();

        boolean good = true;
        for (int i = 0; i < tran2.size(); i ++) {
            if (!(tran1.contains(tran2.get(i)))) {
                good = false;
            }
        }

        ArrayList<Place> place1 = sn1.getSubPlaces();
        ArrayList<Place> place2 = sn2.getSubPlaces();

        for (int i = 0; i < place2.size(); i ++) {
            if (!(place1.contains(place2.get(i)))) {
                good = false;
                break;
            }
        }

        return good;
    }
    */

    private static ArrayList<ArrayList<Integer>> setZeroMatrix(ArrayList<ArrayList<Integer>> im)
    {
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for(int i = 0 ; i<im.size() ; i++) {
            ArrayList<Integer> row = new ArrayList<Integer>();
            for (int j = 0; j < im.get(i).size(); j++) {
                row.add(0);
            }
            result.add(row);
        }
        return result;
    }

    private static void transitionStep(ArrayList<ArrayList<Integer>> im, int row, int column, ArrayList<ArrayList<Integer>> check){
        //dodawanie do check kolejnego kroku
        //detekcja czy zrobił kółko ale nie w pierwszym elemencie
        //detekcja czy skończył - początkowy element

        for(int i =row; i< im.get(row).size() ; i++) {
            if (check.get(row).get(i) >= 1) {
                //if (check.get(row).get(i) == 2)
                    addNewSubnet(check);
            } else {
                if (im.get(row).get(i) > 0) {
                    check.get(row).set(i, 1);
                    placeStep(im, i, column, check);
                }
            }
        }
    }

    private static void addNewSubnet(ArrayList<ArrayList<Integer>> check){
        SubNet sn = new SubNet();

        ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
        ArrayList<Place> allPlaces = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();

        ArrayList<Transition> transitions = new ArrayList<>();
        ArrayList<Place> places = new ArrayList<>();
        for(int i=0;i<check.size();i++) {
            for(int j=0;j<check.get(i).size();j++) {
                if(check.get(i).get(j)!=0) {
                    if (!transitions.contains(allTransitions.get(i))) {
                        transitions.add(allTransitions.get(i));
                    }
                    if (!places.contains(allPlaces.get(j))) {
                        places.add(allPlaces.get(j));
                    }
                }
            }
        }
        sn.setSubPlaces(places);
        sn.setSubTransitions(transitions);
        sn.calculateArcs(places);
        tzSubNets.add(sn);
    }

    private static void placeStep(ArrayList<ArrayList<Integer>> im, int row, int column,ArrayList<ArrayList<Integer>> check){
        ArrayList<Integer> columnVector = getColumn(im,column);
        for(int i =column; i< columnVector.size() ; i++){
            if (check.get(i).get(row) >= 1) {
                if (check.get(i).get(row) == 2)
                    addNewSubnet(check);
            } else {
                if (columnVector.get(i) > 0) {
                    check.get(row).set(i, 1);
                    transitionStep(im, row, i, check);
                }
            }
        }/*
        }
            if(columnVector.get(i)<0) {
                check.get(row).set(i,1);
                transitionStep(im, row, i, check);
            }*/
    }

    private static ArrayList<Integer> getColumn(ArrayList<ArrayList<Integer>> im, int column){
        ArrayList<Integer> newRow = new ArrayList<>();
        for (int j = 0; j < im.size(); j++) {
            newRow.add(im.get(j).get(column));
        }
        return newRow;
    }

    private static boolean chceckIfFinished(ArrayList<ArrayList<Integer>> check, int row, int column){
        if(check.get(row).get(column)==1)
            return true;

        return false;
    }

    private static boolean isSourceOrSink(ArrayList<Integer> rowOrColumn){
        boolean isMore = false;
        boolean isLess = false;
        for(int i=0 ; i< rowOrColumn.size(); i++)
        {
            if(rowOrColumn.get(i)>0)
                isMore=true;
            if(rowOrColumn.get(i)<0)
                isLess=true;

        }
        return isMore!=isLess;
    }

    private static class Path{
        public Node startNode;
        public Node endNode;
        public ArrayList<Node> path;
        public ArrayList<Node> innerpath;

        public Path(Node s, Node e, ArrayList<Node> l){
            startNode = s;
            endNode = e;
            path=new ArrayList<>(l);
            innerpath = l;
            innerpath.remove(s);
            innerpath.remove(e);
        }

    }

    public static class SubNet {
        //Functional
        private ArrayList<Transition> subTransitions;
        private ArrayList<Place> subPlaces;
        private ArrayList<Place> subBorderPlaces;
        private ArrayList<Place> subInternalPlaces;
        ArrayList<Arc> subArcs;

        //Snet
        private ArrayList<Transition> subBorderTransition;
        private ArrayList<Transition> subInternalTransition;
        //Universal
        int subNetID;

        SubNet(){

        }

        SubNet(ArrayList<Transition> subTransitions, ArrayList<Place> subPlaces){
            if(!subTransitions.isEmpty())
            {
                createTransirionBasedSubNet(subTransitions);
            }
            else
            {
                createPlaceBasedSubNet(subPlaces);
            }
        }

        SubNet(ArrayList<Integer> maxADTset){
            createTransirionBasedSubNet(getTransitionsForADT(maxADTset));
        }

        SubNet(ArrayList<Node> list, boolean test){
            subTransitions = new ArrayList<>();
            subPlaces = new ArrayList<>();
            for(Node node : list){
                for (Transition t : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions())
                    if(t.getID() == node.getID())
                        subTransitions.add(t);
                for (Place p : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces() )
                    if(p.getID() == node.getID())
                        subPlaces.add(p);
            }

            //wylicz łuki
            calculateArcs(subPlaces);

        }

        public SubNet(ArrayList<Path> pathList, int i) {
            subTransitions = new ArrayList<>();
            subPlaces = new ArrayList<>();
            for (Path path: pathList
                 ) {
                for(Node node : path.path){
                    for (Transition t : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions())
                        if(t.getID() == node.getID())
                            if(!subTransitions.contains(t))
                                subTransitions.add(t);
                    for (Place p : GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces() )
                        if(p.getID() == node.getID())
                            if(!subPlaces.contains(p))
                                subPlaces.add(p);
                }
            }
            calculateArcs(subPlaces);
        }

        public void calculateArcs(ArrayList<Place> subPlaces) {
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Place place : subPlaces) {
                for (Arc arc : place.getInArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
                for (Arc arc : place.getOutArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
            }
            this.subArcs = listOfAllArcs;
        }

        private ArrayList<Transition> getTransitionsForADT(ArrayList<Integer> maxADTset){
            ArrayList<Transition> allTransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
            ArrayList<Transition> transitionsForADT = new ArrayList<Transition>();
            for (Integer number: maxADTset ) {
                transitionsForADT.add(allTransitions.get(number));
            }
            return transitionsForADT;
        }

        private void createTransirionBasedSubNet(ArrayList<Transition> subTransitions) {
            this.subTransitions = subTransitions;
            this.subInternalPlaces = new ArrayList<>();
            this.subBorderPlaces = new ArrayList<>();

            //Zbiór wszystkich miejsc
            ArrayList<Place> listOfInOutPlaces = new ArrayList<>();
            for (Transition transition : this.subTransitions) {
                for (Place place : transition.getPostPlaces()) {
                    if (!listOfInOutPlaces.contains(place))
                        listOfInOutPlaces.add(place);
                }
                for (Place place : transition.getPrePlaces()) {
                    if (!listOfInOutPlaces.contains(place))
                        listOfInOutPlaces.add(place);
                }
            }
            this.subPlaces = listOfInOutPlaces;

            //Zbiór miejsc granicznych
            for (Place place : listOfInOutPlaces) {
                boolean border = false;
                for (Transition transition : place.getPostTransitions())
                    if (!this.subTransitions.contains(transition))
                        border = true;

                for (Transition transition : place.getPreTransitions())
                    if (!this.subTransitions.contains(transition))
                        border = true;

                if (border)
                    this.subBorderPlaces.add(place);
                else
                    this.subInternalPlaces.add(place);
            }

            //wylicz łuki
            ArrayList<Arc> listOfAllArcs = new ArrayList<>();
            for (Transition transition : subTransitions) {
                for (Arc arc : transition.getInArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
                for (Arc arc : transition.getOutArcs())
                    if (!listOfAllArcs.contains(arc))
                        listOfAllArcs.add(arc);
            }
            this.subArcs = listOfAllArcs;
        }

        private void createPlaceBasedSubNet(ArrayList<Place> subPlaces) {
            this.subPlaces = subPlaces;
            this.subInternalTransition = new ArrayList<>();
            this.subBorderTransition = new ArrayList<>();

            //Zbiór wszystkich miejsc
            ArrayList<Transition> listOfInOutTransition = new ArrayList<>();
            for (Place place : this.subPlaces) {
                for (Transition transition : place.getPostTransitions()) {
                    if (!listOfInOutTransition.contains(transition))
                        listOfInOutTransition.add(transition);
                }
                for (Transition transition : place.getPreTransitions()) {
                    if (!listOfInOutTransition.contains(transition))
                        listOfInOutTransition.add(transition);
                }
            }
            this.subTransitions = listOfInOutTransition;

            //Zbiór tranzycji granicznych
            for (Transition transition : listOfInOutTransition) {
                boolean border = false;
                for (Place place : transition.getPostPlaces())
                    if (!this.subPlaces.contains(place))
                        border = true;

                for (Place place : transition.getPrePlaces())
                    if (!this.subPlaces.contains(place))
                        border = true;

                if (border)
                    this.subBorderTransition.add(transition);
                else
                    this.subInternalTransition.add(transition);
            }

            //wylicz łuki
            calculateArcs(subPlaces);
        }

        public ArrayList<Transition> getSubBorderTransition() {
            return subBorderTransition;
        }

        public void setSubBorderTransition(ArrayList<Transition> subBorderTransition) {
            this.subBorderTransition = subBorderTransition;
        }

        public ArrayList<Transition> getSubInternalTransition() {
            return subInternalTransition;
        }

        public void setSubInternalTransition(ArrayList<Transition> subInternalTransition) {
            this.subInternalTransition = subInternalTransition;
        }

        public ArrayList<Transition> getSubTransitions() {
            return subTransitions;
        }

        public void setSubTransitions(ArrayList<Transition> subTransitions) {
            this.subTransitions = subTransitions;
        }

        public ArrayList<Place> getSubPlaces() {
            return subPlaces;
        }

        public void setSubPlaces(ArrayList<Place> subPlaces) {
            this.subPlaces = subPlaces;
        }

        public ArrayList<Arc> getSubArcs() {
            return subArcs;
        }

        public void setSubArcs(ArrayList<Arc> subArcs) {
            this.subArcs = subArcs;
        }

        public int getSubNetID() {
            return subNetID;
        }

        public void setSubNetID(int subNetID) {
            this.subNetID = subNetID;
        }

        public ArrayList<Place> getSubBorderPlaces() {
            return subBorderPlaces;
        }

        public void setSubBorderPlaces(ArrayList<Place> subBorderPlaces) {
            this.subBorderPlaces = subBorderPlaces;
        }

        public ArrayList<Place> getSubInternalPlaces() {
            return subInternalPlaces;
        }

        public void setSubInternalPlaces(ArrayList<Place> subInternalPlaces) {
            this.subInternalPlaces = subInternalPlaces;
        }
    }
}
