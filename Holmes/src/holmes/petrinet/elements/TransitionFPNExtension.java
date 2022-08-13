package holmes.petrinet.elements;

import holmes.petrinet.functions.FunctionContainer;

import java.util.ArrayList;

public class TransitionFPNExtension {
    //tranzycja funkcyjna:
    private boolean isFunctional = false;
    protected ArrayList<FunctionContainer> fList;

    private Transition masterTransition;

    public TransitionFPNExtension(Transition trans) {
        masterTransition = trans;
    }

    /**
     * Metoda ustawia flagę tranzycji funkcyjnej.
     * @param value boolean - true, jeśli tranzycja ma być funkcyjna
     */
    public void setFunctional(boolean value) {
        this.isFunctional = value;
    }

    /**
     * Metoda zwraca flagę funkcyjności tranzycji.
     * @return boolean - true, jeśli funkcyjna
     */
    public boolean isFunctional() {
        return this.isFunctional;
    }

    /**
     * Metoda zwraca pełen wektor funkcyjny tranzycji. Przed jej wywołaniem należy upewnić się funkcją
     * checkFunctions(...), że wektor ten jest aktualny.
     * @return ArrayList[FunctionContainer] - wektor funkcji
     */
    public ArrayList<FunctionContainer> accessFunctionsList() {
        return this.fList;
    }

    /**
     * Metoda weryfikuje wektor łuków funkcyjnych - usuwa łuki które już nie istnieją w rzeczywistych
     * połączeniach tranzycji oraz dodaje takie, których na liście funkcyjnej brakuje.
     * @param arcs ArrayList[Arc] - wektor wszystkich łuków sieci
     */
    public void checkFunctions(ArrayList<Arc> arcs, ArrayList<Place> places) {
        int fSize = fList.size();
        ArrayList<Arc> fArcs = new ArrayList<>();
        //usuń funkcje związane z nieistniejącymi łukami
        for (int f = 0; f < fSize; f++) {
            FunctionContainer fc = fList.get(f);
            if (!arcs.contains(fc.arc)) {
                fList.remove(f);
                f--;
                fSize--;
            } else {
                fArcs.add(fc.arc); //lista łuków funkcyjnych
            }
        }

        ArrayList<Arc> inArcs = masterTransition.getInArcs();
        ArrayList<Arc> outArcs = masterTransition.getOutArcs();

        for (Arc arc : inArcs) {
            if (fArcs.contains(arc))
                continue;

            FunctionContainer fc = new FunctionContainer(masterTransition);
            int placeIndex = places.indexOf((Place)arc.getStartNode());
            fc.fID = "p" + placeIndex + "-->T";
            fc.arc = arc;
            fc.inTransArc = true;
            fList.add(fc);
        }

        for (Arc arc : outArcs) {
            if (fArcs.contains(arc))
                continue;

            FunctionContainer fc = new FunctionContainer(masterTransition);
            int placeIndex = places.indexOf((Place)arc.getEndNode());
            fc.fID = "T-->p" + placeIndex;
            fc.arc = arc;
            fc.inTransArc = false;
            fList.add(fc);
        }
    }

    /**
     * Metoda podmienia zapis funkcji w tranzycji.
     * @param fID        String - identyfikator funkcji dla danej tranzycji
     * @param expression String - nowa forma funkcji
     * @param correct    boolean - true jeśli funkcja została zweryfikowana jako prawidłowa
     * @param enabled    boolean - true, jeśli funkcja ma być aktywna (np. w symulatorze)
     * @return boolean - true, jeśli znaleziono identyfikator i podmieniono funkcję
     */
    public boolean updateFunctionString(String fID, String expression, boolean correct, boolean enabled) {
        for (FunctionContainer fc : accessFunctionsList()) {
            if (fc.fID.equals(fID)) {
                fc.simpleExpression = expression;
                fc.correct = correct;
                fc.enabled = enabled;
                return true;
            }
        }
        return false;
    }

    /**
     * Metoda zwraca kontener z funkcją.
     * @param fID String - identyfikator w ramach tranzycji
     * @return FunctionContainer - obiekt kontenera
     */
    public FunctionContainer getFunctionContainer(String fID) {
        for (FunctionContainer fc : accessFunctionsList()) {
            if (fc.fID.equals(fID)) {
                return fc;
            }
        }
        return null;
    }

    /**
     * /**
     * Metoda zwraca kontener z funkcją - szukanie po obiekcie łuku.
     * @param arc Arc - łuk tranzycji
     * @return FunctionContainer - obiekt kontenera
     */
    public FunctionContainer getFunctionContainer(Arc arc) {
        for (FunctionContainer fc : accessFunctionsList()) {
            if (fc.arc.equals(arc)) {
                return fc;
            }
        }
        return null;
    }
}
