package holmes.firingrate;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.ArrayList;

/**
 * Klasa definiująca ograniczenia, reprezentująca poprzednie tranzycje
 */
public class Constraint {
    private Transition transition;
    private int alpha = 1;
    private int beta = 1;
    private ArrayList<Arc> conflictArcs;


    public Constraint() {
        conflictArcs = new ArrayList<>();
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getBeta() {
        return beta;
    }

    public void setBeta(int beta) {
        this.beta = beta;
    }

    public ArrayList<Arc> getConflictArcs() {
        return conflictArcs;
    }

    public void setConflictArcs(ArrayList<Arc> conflictArcs) {
        this.conflictArcs = conflictArcs;
    }

    public Transition getTransition() {
        return transition;
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }
}
