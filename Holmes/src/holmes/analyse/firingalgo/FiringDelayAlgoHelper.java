package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;
import java.util.stream.Collectors;

class FiringDelayAlgoHelper {

    /// oznacza tranzycje jako odwiedzoną na podstawie poprzednich tranzycji
    public static void markTransition(Transition transition) {
        StateHolder.instance.markTransition(transition);
    }

    public static void process(Transition transition) {
        if (isSourceTransition(transition)) {
            StateHolder.instance.addState(transition, new TokenSource(transition.spnExtension.getFiringRate()));
            return;
        }

        //jak jest więcej miejsc wejściowych to jest to synchronizacja,
        //gdzie z założenia wejście obu jest równe
        var place = transition.getInputPlaces().get(0);
        //na razie nie jeszcze nie ma obsługi tokenów z dwóch źródeł
        var previousTransition = place.getInputTransitions().get(0);
        var previousTransitionState = StateHolder.instance.getState(previousTransition);
        StateHolder.instance.addState(transition, previousTransitionState.copyForOtherTransition(transition));

        SyncIfValid(transition);
    }

    private static void SyncIfValid(Transition transition) {
        if(isSyncTransition(transition)) {
            var previousTransitionStates = new ArrayList<State>();
            for (var place : transition.getInputPlaces()) {
                //na razie nie jeszcze nie ma obsługi tokenów z dwóch źródeł TODO btw
                var previousTransition = place.getInputTransitions().get(0);
                previousTransitionStates.add(StateHolder.instance.getState(previousTransition));
            }

            List<Conflict> previousConflicts = previousTransitionStates.stream()
                    .map(state -> state.conflict)
                    .filter(Objects::nonNull).toList();
            if(previousConflicts.size() > 1) {
                var first = previousConflicts.get(0);
                for (Conflict second : previousConflicts.subList(1, previousConflicts.size())) {
                    syncConflicts(first, second);
                }
            }

            if(previousTransitionStates.stream().anyMatch(State::isResolved) &&
                    previousTransitionStates.stream().anyMatch(state -> !state.isResolved())) {
                var maxValid = previousTransitionStates.stream()
                        .filter(State::isResolved)
                        .map(State::getResult)
                        .max(Comparator.naturalOrder())
                        .get();
                var notValid = previousTransitionStates.stream()
                        .filter(state -> !state.isResolved())
                        .map(state -> state.tokenState)
                        .collect(Collectors.toCollection(HashSet::new));
                for (var tokenSource : notValid) {
                    tokenSource.setTokenSourceValue(maxValid);
                }
            }
        }
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
                    new Conflict(transition, 1, masks.get(transition), mask)
            );
        }
    }

    public static void syncConflicts(Conflict conflict1, Conflict conflict2) {
        if(!conflict1.getMask().intersects(conflict2.getMask())) {
            return;
        }

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
        }
        for (Conflict conflict : conflicts2) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict2.s;
        }
    }

    public static boolean areMarked(ArrayList<Transition> transitions) {
        for (Transition transition : transitions) {
            if(!StateHolder.instance.isMarked(transition))
                return false;
        }
        return true;
    }

    public static boolean isMarked(Transition transition) {
        return StateHolder.instance.isMarked(transition);
    }

    public static boolean isProcessed(Transition transition) {
        return StateHolder.instance.getState(transition).tokenState != null;
    }

    public static boolean isSourceTransition(Transition transition) {
        return transition.getInputArcs().size() <= 0;
    }

    public static boolean isSyncTransition(Transition transition) {
        return  transition.getInputArcs().size() > 1;
    }

    public static boolean isConflictPlace(Place place) {
        return place.getOutputTransitions().size() > 1;
    }
}
