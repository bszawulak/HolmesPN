package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.HashMap;

public class FiringDelayAlgoStateHolder {
    /// HashMapa z wynikami działania algorytmu,
    /// kluczem jest ID Tranzycji
    private HashMap<Integer, Double> results;
    /// HashMapa z konfliktami,
    /// kluczem jest ID Tranzycji
    private HashMap<Integer, FiringDelayConflict> conflicts;

    private static FiringDelayAlgoStateHolder instance;
    public static FiringDelayAlgoStateHolder getInstance() {
        if (instance == null) {
            instance = new FiringDelayAlgoStateHolder();
        }
        return instance;
    }

    public boolean isMarked(Transition transition) {
        return results.containsKey(transition.getID());
    }

    public void resetState() {
        results = new HashMap<>();
    }

    public void markTransition(Transition transition, double firingDealy) {
        results.put(transition.getID(), firingDealy);
    }

    public void addConflict(Transition transition, FiringDelayConflict firingDelayConflict) {
        if(conflicts.containsKey(transition.getID())) {
            return;
            //TODO zrobić tabele na konflikty, rzucać błąd albo spróbować ze zmianą docelowej maski
        }
        conflicts.put(transition.getID(), firingDelayConflict);
    }

    /*public double getWeight(Transition transition, Place conflictPlace) {
        return results.get(transition.getID()).get(conflictPlace.getID());
    }

    public HashMap<Integer, Double> getWeight(Transition transition) {
        return results.get(transition.getID());
    }*/
}
