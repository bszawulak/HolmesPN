package holmes.petrinet.elements.extensions;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.functions.FunctionContainer;

import java.util.ArrayList;

public class TransitionFPNExtension {
    private boolean isFunctional = false;
    protected ArrayList<FunctionContainer> fList;
    private Transition masterTransition;

    /**
     * Ustawia połączenie rozszerzenia z tranzycją.
     * @param trans (<b>Transition</b>) obiekt tranzycji.
     */
    public TransitionFPNExtension(Transition trans) {
        masterTransition = trans;
    }

    /**
     * Metoda ustawia flagę tranzycji funkcyjnej.
     * @param value (<b>boolean</b>) true, jeśli tranzycja ma być funkcyjna.
     */
    public void setFunctional(boolean value) {
        this.isFunctional = value;
    }

    /**
     * Metoda zwraca flagę funkcyjności tranzycji.
     * @return (<b>boolean</b>) - true, jeśli funkcyjna.
     */
    public boolean isFunctional() {
        return this.isFunctional;
    }

    /**
     * Metoda zwraca pełen wektor funkcyjny tranzycji. Przed jej wywołaniem należy upewnić się funkcją
     * checkFunctions(...), że wektor ten jest aktualny.
     * @return (<b>ArrayList[FunctionContainer]</b>) - wektor funkcji.
     */
    public ArrayList<FunctionContainer> accessFunctionsList() {
        return this.fList;
    }

    /**
     * Tworzy nową tablicę funkcji.
     */
    public void createNewFunctionsVector() {
        this.fList = new ArrayList<>();
    }

    /**
     * Metoda weryfikuje wektor łuków funkcyjnych - usuwa łuki które już nie istnieją w rzeczywistych
     * połączeniach tranzycji oraz dodaje takie, których na liście funkcyjnej brakuje.
     * @param arcs (<b>ArrayList[Arc]</b>) wektor wszystkich łuków sieci.
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
     * @param fID (<b>String</b>) identyfikator funkcji dla danej tranzycji.
     * @param expression (<b>String</b>) nowa forma funkcji.
     * @param correct (<b>boolean</b>) true jeśli funkcja została zweryfikowana jako prawidłowa.
     * @param enabled (<b>boolean</b>) true, jeśli funkcja ma być aktywna (np. w symulatorze).
     * @return (<b>boolean</b>) - true, jeśli znaleziono identyfikator i podmieniono funkcję.
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
     * @param fID (<b>String</b>) identyfikator w ramach tranzycji.
     * @return (<b>FunctionContainer</b>) - obiekt kontenera funkcji.
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
     * @param arc (<b>Arc</b>) łuk tranzycji.
     * @return (<b>FunctionContainer</b>) - obiekt kontenera funkcji.
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
