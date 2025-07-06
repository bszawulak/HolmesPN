package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

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

        double markValue = 0;
        ArrayList<FiringDelayConflict> syncedConflicts = new ArrayList<>();
        for (Place place : transition.getInputPlaces()) {
            HashSet<FiringDelayConflict> addedConflicts = new HashSet<>();
            for (Transition inputTransition : place.getInputTransitions()) {
                double multiplier = (double) inputTransition.getOutputArcWeightTo(place) / transition.getInputArcWeightFrom(place);
                double weight = stateHolder.getResult(inputTransition);
                markValue += weight*multiplier;
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
                FiringDelayAlgoStateHolder.instance.addConflict(transition, notResolved.get(0));
            }
        }
        FiringDelayAlgoStateHolder.instance.markTransition(transition, markValue);
    }

    public static void markPlaceAsConflict(Place place) {
        long mask = 0;
        HashMap<Transition, Long> masks = new HashMap<>();
        for (Transition transition : place.getOutputTransitions()) {
            long newMask = MaskOffsetManager.instance.getNewMask();
            mask |= newMask;
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
        long mask1 = conflict1.mask;
        long mask2 = conflict2.mask;
        long syncedMask = mask1 | mask2;

        HashSet<FiringDelayConflict> conflicts1 = FiringDelayAlgoStateHolder.instance.getConflicts(mask1);
        HashSet<FiringDelayConflict> conflicts2 = FiringDelayAlgoStateHolder.instance.getConflicts(mask2);
        conflicts1.remove(conflict1);
        conflicts2.remove(conflict2);

        conflict1.sync(conflict2);
        for (FiringDelayConflict conflict : conflicts1) {
            conflict.mask = syncedMask;
            conflict.s *= conflict1.s;

            if (conflict.isResolved()) {
                FiringDelayAlgoStateHolder.instance.resolveConflict(conflict);
            }
        }
        for (FiringDelayConflict conflict : conflicts2) {
            conflict.mask = syncedMask;
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
