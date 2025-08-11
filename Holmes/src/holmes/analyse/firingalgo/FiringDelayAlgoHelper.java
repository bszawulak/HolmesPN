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
            TokenSource tokenSource = new TokenSource(transition.firingRate);
            StateHolder.instance.naturalTokenSources.add(tokenSource);
            StateHolder.instance.addState(transition, new TokenState(tokenSource));
            return;
        }

        var synced = SyncIfValid(transition);

        Place place = getPrioritizedInputPlace(transition);
        var previousTransition = getPrioritizedInputTransition(place);
        var previousTransitionState = StateHolder.instance.getState(previousTransition);
        var copied = previousTransitionState.copyForOtherTransition(transition);
        if(StateHolder.instance.getState(transition).conflict != null) {
            if(copied.conflict != null) {
                forceResolveConflictBranch(copied.conflict);
                copied = previousTransitionState.copyForOtherTransition(transition);
            }
            copied.conflict = StateHolder.instance.getState(transition).conflict;
        }

        if (place.isSumming()) {
            copied.tokenState = getPlaceTokenState(place);
        }
        else {
            double multiplier =  getWeight(previousTransition, place);
            if(place.isConflict())
            {
                copied.tokenState.multiplier *= multiplier;
            }
            else {
                copied.updateWeights(multiplier);
            }
        }
        double multiplier =  (double) 1 / getWeight(place, transition);
        copied.updateWeights(multiplier);


        if(synced && previousTransitionState.conflict != null && copied.conflict != null
                && previousTransitionState.conflict.getTargetMask().equals(copied.conflict.getTargetMask())) {
            copied.conflict.weight = getNotFullySyncedConflictWeight(transition, copied.conflict.getTargetMask());
            StateHolder.instance.unresolvedConflicts.put(copied.conflict.getMask(), copied.conflict);
        }

        if(StateHolder.instance.getState(transition).tokenState != null) {
            copied.tokenState.multiplier *= StateHolder.instance.getState(transition).tokenState.multiplier;
        }

        StateHolder.instance.addState(transition, copied);

        // firingrate ustawione przez użytkownika
        if(transition.firingRate != null) {
            Double tokens = copied.tokenState.getTokens();
            if(copied.conflict != null) {
                forceResolveConflictBranch(copied.conflict, tokens != null ? transition.firingRate/tokens : null);
            }

            if(tokens != null) {
                copied.tokenState.multiplier *= transition.firingRate/ tokens;
            }
            else {
                copied.tokenState = new TokenState(new TokenSource(transition.firingRate));
            }
        }
        tryToAssignNotResolvedTokenSourceValues(transition);
    }

    private static double getNotFullySyncedConflictWeight(Transition transition, BitSet targetMask) {
        double sum = 0;
        for (Place place1 : transition.getInputPlaces()) {
            for (Transition transition1 : place1.getInputTransitions()) {
                if(StateHolder.instance.getState(transition1).conflict.getTargetMask().equals(targetMask)) {
                    sum += StateHolder.instance.getState(transition1).conflict.weight / getWeight(place1, transition);
                }
            }
        }
        return sum;
    }

    private static int getWeight(Place place, Transition transition) {
        return transition.getInputArcToNode(place).get().arcRef.getWeight();
    }

    private static int getWeight(Transition transition, Place place) {
        return place.getInputArcToNode(transition).get().arcRef.getWeight();
    }

    private static TokenState getPlaceTokenState(Place place) {
        if(!place.isSumming())
        {
            return StateHolder.instance.getState(place.getInputTransitions().get(0)).tokenState;
        }

        var summedTokenState = StateHolder.instance.summedTokenStates.get(place);
        if(summedTokenState != null) {
            return summedTokenState.copyForOtherTransition();
        }

        TokenState newTokenState = new TokenState(new TokenSource(getPlaceTokens(place)));
        StateHolder.instance.summedTokenStates.put(place, newTokenState);
        return newTokenState.copyForOtherTransition();
    }

    private static Double getPlaceTokens(Place place) {
        double sum = 0;
        for (Transition transition : place.getInputTransitions()) {
            if(!StateHolder.instance.getState(transition).isResolved()) {
                return null;
            }
            sum += StateHolder.instance.getResult(transition);
        }
        return sum;
    }

    private static Place getPrioritizedInputPlace(Transition transition) {
        var inputPlacesWithUnresolvedConflicts = transition.getInputPlaces().stream()
                .filter(place -> place.isConflict()
                        || place.getInputTransitions().stream().anyMatch(
                                transition1 -> StateHolder.instance.getState(transition1).conflict != null))
                .toList();

        var inputPlacesWithTokenSource = transition.getInputPlaces().stream()
                .filter(place -> place.getInputTransitions().stream()
                        .anyMatch(transition1 -> StateHolder.instance.getState(transition1).tokenState.isResolved()))
                .toList();

        if(!inputPlacesWithUnresolvedConflicts.isEmpty()) {
            return inputPlacesWithUnresolvedConflicts.get(0);
        }
        else if(!inputPlacesWithTokenSource.isEmpty()) {
            return inputPlacesWithTokenSource.get(0);
        }
        else {
            return transition.getInputPlaces().get(0);
        }
    }

    private static Transition getPrioritizedInputTransition(Place place) {
        var inputTransitionsWithUnresolvedConflicts = place.getInputTransitions().stream()
                        .filter(transition1 -> StateHolder.instance.getState(transition1).conflict != null)
                .toList();

        var inputTransitionsWithTokenSource = place.getInputTransitions().stream()
                        .filter(transition1 -> StateHolder.instance.getState(transition1).tokenState.isResolved())
                .toList();

        if(!inputTransitionsWithUnresolvedConflicts.isEmpty()) {
            return inputTransitionsWithUnresolvedConflicts.get(0);
        }
        else if(!inputTransitionsWithTokenSource.isEmpty()) {
            return inputTransitionsWithTokenSource.get(0);
        }
        else {
            return place.getInputTransitions().get(0);
        }
    }

    private static double getMultiplierBetweenTransitions(Transition previousTransition, Transition transition) {
        var place = getPlaceInBetween(previousTransition, transition);
        return (double) getWeight(previousTransition, place)
                / getWeight(place, transition);
    }

    private static boolean SyncIfValid(Transition transition) {
        var synced = false;

        ArrayList<Conflict> conflictsToSynchronize = new ArrayList<>();

        synced = syncForEachPlace(transition, conflictsToSynchronize, synced);

        synced = syncForTransition(transition, conflictsToSynchronize, synced);
        return synced;
    }

    private static boolean syncForTransition(Transition transition, ArrayList<Conflict> conflictsToSynchronize, boolean synced) {
        ArrayList<BitSet> targetMasks = conflictsToSynchronize.stream().map(Conflict::getTargetMask)
                .distinct().collect(Collectors.toCollection(ArrayList::new));
        for(BitSet targetMask : targetMasks) {
            var conflictsWithTheSameTargetMask = conflictsToSynchronize.stream().filter(
                            conflict -> conflict.getTargetMask().equals(targetMask))
                    .collect(Collectors.toCollection(ArrayList::new));
            if(conflictsWithTheSameTargetMask.size() > 1) {
                var copyOfFirst = conflictsWithTheSameTargetMask.get(0).copy();
                copyOfFirst.weight *= getWeight(getPlaceInBetween(copyOfFirst.transition, transition), transition);

                for (Conflict second : conflictsWithTheSameTargetMask.subList(1, conflictsWithTheSameTargetMask.size())) {
                    var copyOfSecond = second.copy();
                    copyOfSecond.weight *= getWeight(getPlaceInBetween(copyOfSecond.transition, transition), transition);

                    var localSynced = syncConflicts(copyOfFirst, copyOfSecond);
                    if(localSynced) {
                        copyOfFirst.weight += copyOfSecond.weight;
                    }
                    synced |= localSynced;
                }
            }
        }
        return synced;
    }

    private static boolean syncForEachPlace(Transition transition, ArrayList<Conflict> conflictsToSynchronize, boolean synced) {
        for (Place place : transition.getInputPlaces()) {
            var conflicts = place.getInputTransitions().stream()
                    .map(transition1 -> StateHolder.instance.getState(transition1).conflict)
                    .filter(Objects::nonNull).toList();
            ArrayList<BitSet> targetMasks = conflicts.stream().map(Conflict::getTargetMask)
                    .distinct().collect(Collectors.toCollection(ArrayList::new));
            for(BitSet targetMask : targetMasks) {
                var conflictsWithTheSameTargetMask = conflicts.stream().filter(
                        conflict -> conflict.getTargetMask().equals(targetMask))
                        .collect(Collectors.toCollection(ArrayList::new));
                if(conflictsWithTheSameTargetMask.size() > 1) {
                    var copyOfFirst = conflictsWithTheSameTargetMask.get(0).copy();
                    copyOfFirst.weight /= getMultiplierBetweenTransitions(copyOfFirst.transition, transition);

                    for (Conflict second : conflictsWithTheSameTargetMask.subList(1, conflictsWithTheSameTargetMask.size())) {
                        var conflict2Copy = second.copy();
                        conflict2Copy.weight /= getMultiplierBetweenTransitions(second.transition, transition);

                        var localSynced = syncConflicts(copyOfFirst, conflict2Copy);
                        if(localSynced) {
                            copyOfFirst.weight += conflict2Copy.weight;
                        }
                        synced |= localSynced;
                    }
                }
                var notResolved = conflictsWithTheSameTargetMask.stream().filter(
                        conflict -> !conflict.isResolved()).collect(Collectors.toCollection(ArrayList::new));
                if(!notResolved.isEmpty()) {
                    var combinedConflict = new Conflict(
                            notResolved.get(0).transition,
                            notResolved.stream().mapToDouble(conflict1 -> conflict1.weight /
                                    getWeight(conflict1.transition, getPlaceInBetween(conflict1.transition, transition))).sum()
                                    / conflictsWithTheSameTargetMask.size(),
                            notResolved.get(0).getMask(),
                            notResolved.get(0).getTargetMask(),
                            notResolved.stream().mapToDouble(conflict1 -> conflict1.s).sum());
                    conflictsToSynchronize.add(combinedConflict);
                }
            }
        }
        return synced;
    }

    private static ArrayList<State> getPreviousTransitionStates(Transition transition) {
        var previousTransitionStates = new ArrayList<State>();
        for (var place : transition.getInputPlaces()) {
            for (Transition previousTransition : place.getInputTransitions())
            {
                previousTransitionStates.add(StateHolder.instance.getState(previousTransition));
            }
        }
        return previousTransitionStates;
    }

    private static void tryToAssignNotResolvedTokenSourceValues(Transition transition) {
        var previousTransitionStates = getPreviousTransitionStates(transition);
        if(!StateHolder.instance.getState(transition).isResolved()
        && StateHolder.instance.getState(transition).conflict == null) {
            for (Place place : transition.getInputPlaces()) {
                var placeTokens = getPlaceTokens(place);
                if(placeTokens != null) {
                    StateHolder.instance.getState(transition).tokenState.forceSetTokenSourceValue(
                            placeTokens / getWeight(place, transition));
                    break;
                }
            }
        }
        if(StateHolder.instance.getState(transition).isResolved()) {
            var notValid = previousTransitionStates.stream()
                    .filter(state -> !state.isResolved())
                    .filter(state -> !getPlaceInBetween(state.transition, transition).isSumming())
                    .collect(Collectors.toCollection(HashSet::new));
            for (var state : notValid) {
                Double result = StateHolder.instance.getState(transition).getResult();
                if(result != null && !state.tokenState.isResolved()) {
                    state.tokenState.forceSetTokenSourceValue(
                            result/getMultiplierBetweenTransitions(state.transition, transition));
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

    public static boolean syncConflicts(Conflict conflict1, Conflict conflict2) {
        return syncConflicts(conflict1, conflict2, conflict -> conflict::sync);
    }

    private static Place getPlaceInBetween(Transition first, Transition second) {
        var places = second.getInputPlaces();
        ArrayList<Place> outputPlaces = new ArrayList<>(first.getOutputPlaces());
        outputPlaces.retainAll(places);
        return outputPlaces.isEmpty() ? null : outputPlaces.get(0);
    }

    public static boolean syncConflicts(Conflict conflict1, Conflict conflict2, Function<Conflict, Consumer<Conflict>> syncAction) {
        if(!conflict1.getTargetMask().intersects(conflict2.getTargetMask())
            || conflict1.getMask().equals(conflict2.getMask())) {
            return false;
        }

        StateHolder.instance.unresolvedConflicts.remove(conflict1.getMask());
        StateHolder.instance.unresolvedConflicts.remove(conflict2.getMask());

        HashSet<Conflict> conflicts1 = StateHolder.instance.getConflicts(conflict1.getMask());
        HashSet<Conflict> conflicts2 = StateHolder.instance.getConflicts(conflict2.getMask());

        syncAction.apply(conflict1).accept(conflict2);
        propagateResultOfSynchronisation(conflict1, conflicts1);
        propagateResultOfSynchronisation(conflict2, conflicts2);
        return true;
    }

    private static void propagateResultOfSynchronisation(Conflict conflict1, HashSet<Conflict> conflicts1) {
        for (Conflict conflict : conflicts1) {
            conflict.setMask(conflict1.getMask());
            conflict.s *= conflict1.multiplierForPropagation;
            if(conflict.isResolved()) {
                StateHolder.instance.removeConflict(conflict);
            }
        }
    }

    private static void propagateResultOfForcedSynchronisationToOtherConflicts(HashSet<Conflict> conflicts1, Conflict conflict1, BitSet originalMask) {
        var reversedMultiplierForPropagation = 1 - conflict1.multiplierForPropagation;
        for (Conflict conflict : conflicts1) {
            StateHolder.instance.unresolvedConflicts.remove(conflict.getMask());

            BitSet mask = (BitSet) originalMask.clone();
            mask.or(conflict.getMask());
            conflict.setMask(mask);
            conflict.s *= reversedMultiplierForPropagation;
            if(conflict.isResolved()) {
                StateHolder.instance.removeConflict(conflict);
            }
            else {
                StateHolder.instance.unresolvedConflicts.put(conflict.getMask(), conflict);
            }
        }
    }

    public static void forceResolveConflictBranch(Conflict conflict1) {
         forceResolveConflictBranch(conflict1, null);
    }

    public static void forceResolveConflictBranch(Conflict conflict1, Double newS) {
        var copiedConflict = conflict1.copy();
        StateHolder.instance.unresolvedConflicts.remove(copiedConflict.getMask());
        var conflictsToUpdate = StateHolder.instance.getConflictsByTargetMask(copiedConflict.getTargetMask());
        var conflictsWithTheSameMask = conflictsToUpdate.stream()
                .filter(conflict -> conflict.getMask().equals(copiedConflict.getMask()))
                .collect(Collectors.toCollection(HashSet::new));
        var conflictsWithOtherMask = conflictsToUpdate.stream()
                .filter(conflict -> !conflict.getMask().equals(copiedConflict.getMask()))
                .collect(Collectors.toCollection(HashSet::new));

        var conflict1Mask = copiedConflict.getMask();
        copiedConflict.forceResolve(newS);
        propagateResultOfSynchronisation(copiedConflict, conflictsWithTheSameMask);
        propagateResultOfForcedSynchronisationToOtherConflicts(conflictsWithOtherMask, copiedConflict, conflict1Mask);
    }

    public static void tryToAssignNotResolvedTokenSourceValues(ArrayList<Transition> syncTransitions) {
        for(Transition transition : syncTransitions) {
            tryToAssignNotResolvedTokenSourceValues(transition);
        }
    }

    public static void assignOnesToNotResolvedTokenSources() {
        StateHolder.instance.naturalTokenSources.stream()
                .filter(tokenSource -> !tokenSource.isResolved())
                .forEach(tokenSource -> tokenSource.firingRate = 1d);
    }

    public static void tryToAssignNotResolvedArtificalTokenSourceValues() {
        for (Place place : StateHolder.instance.summedTokenStates.keySet()) {
            for (Transition transition : place.getOutputTransitions()) {
                if(!StateHolder.instance.getState(transition).tokenState.isResolved()) {
                    var previousTokens = getPlaceTokens(place);
                    if(previousTokens != null) {
                        StateHolder.instance.getState(transition).tokenState
                                .setTokenSourceValue(previousTokens/getWeight(place, transition));
                    }
                }
            }
        }
    }

    public static void tieLooseConflicts() {
        while (StateHolder.instance.unresolvedConflicts.values().iterator().hasNext()) {
            var conflict = StateHolder.instance.unresolvedConflicts.values().iterator().next();
            if(!conflict.isResolved()) {
                forceResolveConflictBranch(conflict);
            }
            else {
                StateHolder.instance.unresolvedConflicts.remove(conflict.getMask());
            }
        }
    }

    private static boolean syncByHalf(Conflict conflict1, Conflict conflict2) {
        return syncConflicts(conflict1.copy(), conflict2.copy(),
                conflict -> conflict::syncByHalf);
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
