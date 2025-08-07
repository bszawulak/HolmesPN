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

        // firingrate ustawione przez użytkownika
        if(transition.firingRate != null) {
            TokenState tokenState = new TokenState(new TokenSource(transition.firingRate));
            tokenState.fixed = true;
            StateHolder.instance.addState(transition, tokenState);
            tryToAssignNotResolvedTokenSourceValues(transition);
            return;
        }

        Place place = getPrioritizedInputPlace(transition);
        var previousTransition = getPrioritizedInputTransition(place);
        var previousTransitionState = StateHolder.instance.getState(previousTransition);
        var copied = previousTransitionState.copyForOtherTransition(transition);
        if(copied.conflict == null && StateHolder.instance.getState(transition).conflict != null) {
            copied.conflict = StateHolder.instance.getState(transition).conflict;
        }

        if (place.isSumming()) {
            copied.tokenState = getPlaceTokenState(place);
        }
        else {
            double multiplier =  previousTransition.getOutputArcToNode(place).get().arcRef.getWeight();
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


        if(synced && copied.conflict != null) {
            double sum = 0;
            for (Place place1 : transition.getInputPlaces()) {
                for (Transition transition1 : place1.getInputTransitions()) {
                    if(StateHolder.instance.getState(transition1).conflict.getTargetMask().equals(copied.conflict.getTargetMask())) {
                        sum += StateHolder.instance.getState(transition1).conflict.weight / getWeight(place1, transition);
                    }
                }
            }
            copied.conflict.weight = sum;
        }

        StateHolder.instance.addState(transition, copied);
        tryToAssignNotResolvedTokenSourceValues(transition);
    }

    private static int getWeight(Place place, Transition transition) {
        return transition.getInputArcToNode(place).get().arcRef.getWeight();
    }

    private static TokenState getPlaceTokenState(Place place) {
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
        return (double) previousTransition.getOutputArcToNode(place).get().arcRef.getWeight()
                / getWeight(place, transition);
    }

    private static boolean SyncIfValid(Transition transition) {
        var synced = false;
        if(transition.isSync()) {
            var previousTransitionStates = getPreviousTransitionStates(transition);

            List<Conflict> previousConflicts = previousTransitionStates.stream()
                    .map(state -> state.conflict)
                    .filter(Objects::nonNull).toList();
            if(previousConflicts.size() > 1) {
                var first = previousConflicts.get(0);
                for (Conflict second : previousConflicts.subList(1, previousConflicts.size())) {
                    synced = syncConflicts(first, second, transition);
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

    public static boolean syncConflicts(Conflict conflict1, Conflict conflict2, Transition transition) {
        return syncConflicts(conflict1, conflict2, transition, conflict -> conflict::sync);
    }

    private static Place getPlaceInBetween(Transition first, Transition second) {
        var places = second.getInputPlaces();
        ArrayList<Place> outputPlaces = new ArrayList<>(first.getOutputPlaces());
        outputPlaces.retainAll(places);
        return outputPlaces.isEmpty() ? null : outputPlaces.get(0);
    }

    public static boolean syncConflicts(Conflict conflict1, Conflict conflict2, Transition transition, Function<Conflict, Consumer<Conflict>> syncAction) {
        if(!conflict1.getTargetMask().intersects(conflict2.getTargetMask())
            || conflict1.getMask().equals(conflict2.getMask())) {
            return false;
        }

        var conflict1Copy = conflict1.copyForOtherTransition(transition);
        var conflict2Copy = conflict2.copyForOtherTransition(transition);

        // uwzględnienie wag na łukach od poprzedniej tranzycji do synchronizacji
        if(transition != null) {
            conflict1Copy.weight /= getMultiplierBetweenTransitions(conflict1.transition, transition);
            conflict2Copy.weight /= getMultiplierBetweenTransitions(conflict2.transition, transition);
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

        syncAction.apply(conflict1Copy).accept(conflict2Copy);
        for (Conflict conflict : conflicts1) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict1Copy.multiplierForPropagation;
            if(conflict.isResolved()) {
                StateHolder.instance.removeConflict(conflict);
            }
        }
        for (Conflict conflict : conflicts2) {
            conflict.setMask(syncedMask);
            conflict.s *= conflict2Copy.multiplierForPropagation;
            if(conflict.isResolved()) {
                StateHolder.instance.removeConflict(conflict);
            }
        }

        if (!conflict1.isResolved()) {
            StateHolder.instance.unresolvedConflicts.put(conflict1.getMask(), conflict1);
        }
        if (!conflict2.isResolved()) {
            StateHolder.instance.unresolvedConflicts.put(conflict2.getMask(), conflict2);
        }
        return true;
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
        for (Place place : StateHolder.instance.summedTokenStates.sequencedKeySet()) {
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
        return syncConflicts(conflict1, conflict2, null, conflict -> conflict::syncByHalf);
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
