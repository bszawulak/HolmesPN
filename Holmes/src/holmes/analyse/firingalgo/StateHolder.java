package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Transition;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

class StateHolder {
    /// HashMapa z wynikami działania algorytmu,
    /// kluczem jest ID Tranzycji
    private HashMap<Integer, Double> results;
    /// HashMapa z konfliktami,
    /// kluczem jest ID Tranzycji
    private HashMap<Integer, Conflict> conflicts;

    public static StateHolder instance = new StateHolder();

    public boolean isMarked(Transition transition) {
        return results.containsKey(transition.getID());
    }

    public void resetState() {
        results = new HashMap<>();
    }

    public void markTransition(Transition transition, double firingDealy) {
        results.put(transition.getID(), firingDealy);
    }

    public void addConflict(Transition transition, Conflict conflict) {
        if(conflicts.containsKey(transition.getID())) {
            return;
            //TODO zrobić tabele na konflikty, rzucać błąd albo spróbować ze zmianą docelowej maski
        }
        conflicts.put(transition.getID(), conflict);
    }

    public Conflict getConflict(Transition transition) {
        return conflicts.get(transition.getID());
    }

    public HashSet<Conflict> getConflicts(BitSet mask) {
        return conflicts.values().stream().filter((conflict) -> conflict.getMask().equals(mask))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public void resolveConflict(Conflict conflict) {
        double res = results.get(conflict.transactionId);
        res *= conflict.getResult();
        results.put(conflict.transactionId, res);

        conflicts.remove(conflict.transactionId);
    }

    public double getResult(Transition transition) {
        return results.get(transition.getID());
    }
}
