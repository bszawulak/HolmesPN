package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;

public class FiringDelayAlgoHelper {

    /// oznacza tranzycje jako odwiedzoną na podstawie poprzednich tranzycji
    public static void markTransition(Transition transition) {
        FiringDelayAlgoStateHolder stateHolder = FiringDelayAlgoStateHolder.instance;
        if (!stateHolder.isMarked(transition)) {
            return;
        }

        if (isSourceTransition(transition)) {
            stateHolder.markTransition(transition, transition.spnExtension.getFiringRate());
            return;
        }

        HashMap<Place, Double> markValues = new HashMap<>();
        ArrayList<FiringDelayConflict> syncedConflicts = new ArrayList<>();
        for (Place place : transition.getInputPlaces()) {
            HashSet<FiringDelayConflict> addedConflicts = new HashSet<>();
            for (Transition inputTransition : place.getInputTransitions()) {
                double multiplier = (double) inputTransition.getOutputArcWeightTo(place) / transition.getInputArcWeightFrom(place);
                double weight = stateHolder.getResult(inputTransition);
                var currentValue = (double) Objects.requireNonNullElse(markValues.get(place), 0.0);
                currentValue += weight*multiplier;
                markValues.put(place, currentValue);
                addedConflicts.add(FiringDelayAlgoStateHolder.instance.getConflict(transition));
            }
            if(addedConflicts.size() > 1) {
                //TODO tu powinno być dodawanie konfliktów
            }
            else {
                syncedConflicts.addAll(addedConflicts);
            }
        }

        if(syncedConflicts.size() > 1) {
            var first = syncedConflicts.get(0);
            for (FiringDelayConflict second : syncedConflicts.subList(1, syncedConflicts.size())) {
                syncConflicts(first, second);
            }

            ArrayList<FiringDelayConflict> notResolved = (ArrayList<FiringDelayConflict>)
                    syncedConflicts.stream().filter(FiringDelayConflict::isResolved).toList();
            if(!notResolved.isEmpty()) {
                FiringDelayAlgoStateHolder.instance.addConflict(transition, notResolved.get(0).copyForOtherTransaction(transition));
            }
        }

        var max = markValues.values().stream().max(Double::compareTo).get();
        FiringDelayAlgoStateHolder.instance.markTransition(transition, max);
    }

    public static void markPlaceAsConflict(Place place) {
        BitSet mask = new BitSet();
        mask.clear();
        HashMap<Transition, BitSet> masks = new HashMap<>();
        for (Transition transition : place.getOutputTransitions()) {
            BitSet newMask = MaskOffsetManager.instance.getNewMask();
            mask.or(newMask);
            masks.put(transition, newMask);
        }
        for (Transition transition : place.getOutputTransitions()) {
            FiringDelayAlgoStateHolder.instance.addConflict(
                    transition,
                    new FiringDelayConflict(transition.getID(), 1, masks.get(transition), mask)
            );
        }
    }

    public static void syncConflicts(FiringDelayConflict conflict1, FiringDelayConflict conflict2) {
        BitSet mask1 = conflict1.getMask();
        BitSet mask2 = conflict2.getMask();
        BitSet syncedMask = new BitSet();
        syncedMask.clear();
        syncedMask.or(mask1);
        syncedMask.or(mask2);

        HashSet<FiringDelayConflict> conflicts1 = FiringDelayAlgoStateHolder.instance.getConflicts(mask1);
        HashSet<FiringDelayConflict> conflicts2 = FiringDelayAlgoStateHolder.instance.getConflicts(mask2);
        conflicts1.remove(conflict1);
        conflicts2.remove(conflict2);

        conflict1.sync(conflict2);
        for (FiringDelayConflict conflict : conflicts1) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict1.s;

            if (conflict.isResolved()) {
                FiringDelayAlgoStateHolder.instance.resolveConflict(conflict);
            }
        }
        for (FiringDelayConflict conflict : conflicts2) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict2.s;

            if (conflict.isResolved()) {
                FiringDelayAlgoStateHolder.instance.resolveConflict(conflict);
            }
        }
    }

    public static boolean areMarked(ArrayList<Transition> transitions) {
        for (Transition transition : transitions) {
            if(!FiringDelayAlgoStateHolder.instance.isMarked(transition))
                return false;
        }
        return true;
    }

    public static boolean isSourceTransition(Transition transition) {
        return transition.getInputArcs().size() <= 0;
    }

    public static boolean isConflictPlace(Place place) {
        return place.getOutputTransitions().size() > 1;
    }
}
