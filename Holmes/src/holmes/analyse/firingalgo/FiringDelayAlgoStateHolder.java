package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

class FiringDelayAlgoStateHolder {
    /// HashMapa z wynikami działania algorytmu,
    /// kluczem jest ID Tranzycji
    private HashMap<Integer, Double> results;
    /// HashMapa z konfliktami,
    /// kluczem jest ID Tranzycji
    private HashMap<Integer, FiringDelayConflict> conflicts;

    public static FiringDelayAlgoStateHolder instance = new FiringDelayAlgoStateHolder();

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

    public FiringDelayConflict getConflict(Transition transition) {
        return conflicts.get(transition.getID());
    }

    public HashSet<FiringDelayConflict> getConflicts(BitSet mask) {
        return conflicts.values().stream().filter((conflict) -> conflict.getMask().equals(mask))
                .collect(Collectors.toCollection(HashSet::new));
    }

    public void resolveConflict(FiringDelayConflict firingDelayConflict) {
        double res = results.get(firingDelayConflict.transactionId);
        res *= firingDelayConflict.getResult();
        results.put(firingDelayConflict.transactionId, res);

        conflicts.remove(firingDelayConflict.transactionId);
    }

    public double getResult(Transition transition) {
        return results.get(transition.getID());
    }
}
