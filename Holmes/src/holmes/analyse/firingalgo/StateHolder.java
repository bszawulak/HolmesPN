package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Transition;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

class StateHolder {
    /// HashMapa z aktualnym stanem algorytmu,
    /// kluczem jest ID Tranzycji
    private HashMap<Transition, State> states;

    public static StateHolder instance = new StateHolder();

    public boolean isMarked(Transition transition) {
        var state = states.get(transition);
        if (state == null || state.tokenState == null) {
            return false;
        }
        return true;
    }

    public void resetState() {
        states = new HashMap<>();
    }

    public void markTransition(Transition transition, TokenSource tokenSource) {
        if(states.containsKey(transition)) {
            states.get(transition).tokenState = new TokenState(tokenSource);
            return;
        }
        states.put(transition, new State(transition, tokenSource));
    }

    public void markTransition(Transition transition, State state) {
        if(states.containsKey(transition)) {
            if(state.conflict != null) {
                //TODO problem
                return;
            }
            state.conflict = states.get(transition).conflict;
        }
        states.put(transition, state);
    }

    public void addConflict(Transition transition, Conflict conflict) {
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

    public State getState(Transition transition) {
        return states.get(transition);
    }

    public Conflict getConflict(Transition transition) {
        return states.get(transition).conflict;
    }

    public HashSet<Conflict> getConflicts(BitSet mask) {
        return states.values().stream().filter((state) -> state.conflict.getMask().equals(mask))
                .map(state -> state.conflict).collect(Collectors.toCollection(HashSet::new));
    }

    public double getResult(Transition transition) {
        return states.get(transition).getResult();
    }

    public HashSet<Conflict> getConflicts() {
        return states.values().stream().map(state -> state.conflict).collect(Collectors.toCollection(HashSet::new));
    }
}
