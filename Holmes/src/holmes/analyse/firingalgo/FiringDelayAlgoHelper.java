package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;

class FiringDelayAlgoHelper {

    /// oznacza tranzycje jako odwiedzoną na podstawie poprzednich tranzycji
    public static void markTransition(Transition transition) {
        StateHolder stateHolder = StateHolder.instance;
        if (!stateHolder.isMarked(transition)) {
            return;
        }

        if (isSourceTransition(transition)) {
            stateHolder.markTransition(transition, transition.spnExtension.getFiringRate());
            return;
        }

        HashMap<Place, Double> markValues = new HashMap<>();
        ArrayList<Conflict> syncedConflicts = new ArrayList<>();
        for (Place place : transition.getInputPlaces()) {
            HashSet<Conflict> addedConflicts = new HashSet<>();
            for (Transition inputTransition : place.getInputTransitions()) {
                double multiplier = (double) inputTransition.getOutputArcWeightTo(place) / transition.getInputArcWeightFrom(place);
                double weight = stateHolder.getResult(inputTransition);
                var currentValue = (double) Objects.requireNonNullElse(markValues.get(place), 0.0);
                currentValue += weight*multiplier;
                markValues.put(place, currentValue);
                addedConflicts.add(StateHolder.instance.getConflict(transition));
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
            for (Conflict second : syncedConflicts.subList(1, syncedConflicts.size())) {
                syncConflicts(first, second);
            }

            ArrayList<Conflict> notResolved = (ArrayList<Conflict>)
                    syncedConflicts.stream().filter(Conflict::isResolved).toList();
            if(!notResolved.isEmpty()) {
                StateHolder.instance.addConflict(transition, notResolved.get(0).copyForOtherTransaction(transition));
            }
        }

        var max = markValues.values().stream().max(Double::compareTo).get();
        StateHolder.instance.markTransition(transition, max);
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
            StateHolder.instance.addConflict(
                    transition,
                    new Conflict(transition.getID(), 1, masks.get(transition), mask)
            );
        }
    }

    public static void syncConflicts(Conflict conflict1, Conflict conflict2) {
        BitSet mask1 = conflict1.getMask();
        BitSet mask2 = conflict2.getMask();
        BitSet syncedMask = new BitSet();
        syncedMask.clear();
        syncedMask.or(mask1);
        syncedMask.or(mask2);

        HashSet<Conflict> conflicts1 = StateHolder.instance.getConflicts(mask1);
        HashSet<Conflict> conflicts2 = StateHolder.instance.getConflicts(mask2);
        conflicts1.remove(conflict1);
        conflicts2.remove(conflict2);

        conflict1.sync(conflict2);
        for (Conflict conflict : conflicts1) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict1.s;

            if (conflict.isResolved()) {
                StateHolder.instance.resolveConflict(conflict);
            }
        }
        for (Conflict conflict : conflicts2) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict2.s;

            if (conflict.isResolved()) {
                StateHolder.instance.resolveConflict(conflict);
            }
        }
    }

    public static boolean areMarked(ArrayList<Transition> transitions) {
        for (Transition transition : transitions) {
            if(!StateHolder.instance.isMarked(transition))
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
