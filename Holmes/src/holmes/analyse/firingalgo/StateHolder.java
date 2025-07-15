package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.Transition;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

class StateHolder {
    /// HashMapa z aktualnym stanem algorytmu,
    /// kluczem jest ID Tranzycji
    private HashMap<Transition, State> states = new HashMap<>();
    public HashMap<BitSet, Conflict> unresolvedConflicts = new HashMap<>();

    public static StateHolder instance = new StateHolder();

    public boolean isMarked(Transition transition) {
        var state = states.get(transition);
        return state != null && state.marked;
    }

    public void resetState() {
        states = new HashMap<>();
        unresolvedConflicts = new HashMap<>();
    }

    public void markTransition(Transition transition) {
        State state = states.get(transition);
        if (state == null) {
            state = new State(transition);
        }
        state.marked = true;
        states.put(transition, state);
    }

    public void addState(Transition transition, TokenSource tokenSource) {
        if(states.containsKey(transition)) {
            states.get(transition).tokenState = new TokenState(tokenSource);
            return;
        }
        states.put(transition, new State(transition, tokenSource));
    }

    public void addState(Transition transition, State state) {
        if(states.containsKey(transition)) {
            state.conflict = states.get(transition).conflict != null ? states.get(transition).conflict : state.conflict;
            state.tokenState = state.tokenState != null ? state.tokenState : states.get(transition).tokenState;
            state.marked = states.get(transition).marked;
        }
        states.put(transition, state);
    }

    public void addConflict(Transition transition, Conflict conflict) {
        unresolvedConflicts.put(conflict.getMask(), conflict);
        if(states.containsKey(transition)) {
            if(states.get(transition).conflict != null) {
                //TODO problem
                return;
            }
            states.get(transition).conflict = conflict;
            return;
        }
        states.put(transition, new State(transition, conflict));
    }

    public void removeConflict(Conflict conflict) {
        var state = states.get(conflict.transition);
        state.tokenState.multiplier *= conflict.getResult();
        state.conflict = null;
        states.put(conflict.transition, state);
    }

    public State getState(Transition transition) {
        return states.get(transition);
    }

    public HashSet<Conflict> getConflicts(BitSet mask) {
        return states.values().stream()
                .filter(state -> state.conflict != null)
                .filter((state) -> state.conflict.getMask().equals(mask))
                .map(state -> state.conflict).collect(Collectors.toCollection(HashSet::new));
    }

    public HashMap<Transition, Double> getResults() {
        var result = new HashMap<Transition, Double>();
        for(Transition transition : states.keySet()) {
            result.put(transition, getResult(transition));
        }
        return result;
    }

    public Double getResult(Transition transition) {
        return states.get(transition).getResult();
    }

    public HashSet<Conflict> getConflicts() {
        return states.values().stream().map(state -> state.conflict).collect(Collectors.toCollection(HashSet::new));
    }
}
