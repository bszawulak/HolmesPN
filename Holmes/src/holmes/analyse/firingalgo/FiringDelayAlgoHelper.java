package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.Place;
import holmes.analyse.firingalgo.petrinetstructure.Transition;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class FiringDelayAlgoHelper {

    /// oznacza tranzycje jako odwiedzoną na podstawie poprzednich tranzycji
    public static void markTransition(Transition transition) {
        StateHolder.instance.markTransition(transition);
    }

    public static void process(Transition transition) {
        if (transition.isSource()) {
            StateHolder.instance.addState(transition, new TokenSource(transition.transitionRef.spnExtension.getFiringRate()));
            return;
        }

        SyncIfValid(transition);

        //jak jest więcej miejsc wejściowych to jest to synchronizacja,
        //gdzie z założenia wejście obu jest równe
        var place = transition.getInputPlaces().get(0);
        //na razie nie jeszcze nie ma obsługi tokenów z dwóch źródeł
        var previousTransition = place.getInputTransitions().get(0);
        var previousTransitionState = StateHolder.instance.getState(previousTransition);
        var copied = previousTransitionState.copyForOtherTransition(transition);

        double multiplier = (double) previousTransition.getOutputArcToNode(place).get().arcRef.getWeight()
                                           / transition.getInputArcToNode(place).get().arcRef.getWeight();
        copied.updateWeights(multiplier);
        StateHolder.instance.addState(transition, copied);
    }

    private static void SyncIfValid(Transition transition) {
        if(transition.isSync()) {
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
        syncConflicts(conflict1, conflict2, conflict -> conflict::sync);
    }

    public static boolean syncConflicts(Conflict conflict1, Conflict conflict2, Function<Conflict, Consumer<Conflict>> syncAction) {
        if(!conflict1.getTargetMask().intersects(conflict2.getTargetMask())
            || conflict1.getMask().equals(conflict2.getMask())) {
            return false;
        }

        StateHolder.instance.unresolvedConflicts.remove(conflict1.getMask());
        StateHolder.instance.unresolvedConflicts.remove(conflict2.getMask());

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

        syncAction.apply(conflict1).accept(conflict2);
        for (Conflict conflict : conflicts1) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict1.s;
            if(conflict.isResolved()) {
                StateHolder.instance.removeConflict(conflict);
            }
        }
        for (Conflict conflict : conflicts2) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict2.s;
            if(conflict.isResolved()) {
                StateHolder.instance.removeConflict(conflict);
            }
        }

        if (conflict1.isResolved()) {
            StateHolder.instance.removeConflict(conflict1);
        } else {
            StateHolder.instance.unresolvedConflicts.put(conflict1.getTargetMask(), conflict1);
        }
        if(conflict2.isResolved()) {
            StateHolder.instance.removeConflict(conflict2);
        } else if (conflict2.getTargetMask().equals(conflict1.getTargetMask())) {
            StateHolder.instance.unresolvedConflicts.put(conflict2.getTargetMask(), conflict2);
        }
        return true;
    }

    public static void tieLooseConflicts() {
        while (!StateHolder.instance.unresolvedConflicts.isEmpty()) {
            var reset = false;
            for(Conflict conflict1 : StateHolder.instance.unresolvedConflicts.values()) {
                for(Conflict conflict2 : StateHolder.instance.unresolvedConflicts.values()) {
                    reset = tieLooseConflicts(conflict1, conflict2);
                    if(reset) {
                        break;
                    }
                }
                if(reset) {
                    break;
                }
            }
        }
    }

    private static boolean tieLooseConflicts(Conflict conflict1, Conflict conflict2) {
        return syncConflicts(conflict1, conflict2, conflict -> conflict::syncByHalf);
    }

    public static HashMap<Transition, Double> getResult() {
        return StateHolder.instance.getResults();
    }

    public static void resetState() {
        StateHolder.instance.resetState();
        MaskOffsetManager.instance.resetState();
    }

    public static boolean areMarked(List<Transition> transitions) {
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
}
