package holmes.analyse;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.ArrayList;

/**
 * Klasa odpowiedzialna za dekompozycję PN do wybranych typów podsieci
 */
public class SubnetCalculator {

    public static ArrayList<SubNet> functionalSubNets = new ArrayList<>();

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
                if (chceckTransInputOutPut(transition, temporaryList)) {
                    if (!listToAdd.contains(transition))
                        listToAdd.add(transition);
                }
                temporaryList.addAll(listToAdd);
            }

            functionalSubNets.add(new SubNet(temporaryList));
            allTransitions.removeAll(temporaryList);
        }


        if(!functionalSubNets.isEmpty())
        {
            GUIManager.getDefaultGUIManager().reset.setDecompositionStatus(true);
        }

    }

    private static boolean chceckTransInputOutPut(Transition newTran, ArrayList<Transition> sub) {
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
    }

    public static class SubNet {
        ArrayList<Transition> subTransitions;
        ArrayList<Place> subPlaces;
        private ArrayList<Place> subBorderPlaces;
        private ArrayList<Place> subInternalPlaces;
        ArrayList<Arc> subArcs;
        int subNetID;

        SubNet(ArrayList<Transition> subTransitions) {
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
