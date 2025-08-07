package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.Place;
import holmes.analyse.firingalgo.petrinetstructure.Transition;

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

class StateHolder {
    /// HashMapa z aktualnym stanem algorytmu,
    /// kluczem jest ID Tranzycji
    private HashMap<Transition, State> states = new HashMap<>();
    public HashMap<BitSet, Conflict> unresolvedConflicts = new HashMap<>();
    public HashSet<TokenSource> naturalTokenSources = new HashSet<>();
    public LinkedHashMap<Place, TokenState> summedTokenStates = new LinkedHashMap<>();

    public static StateHolder instance = new StateHolder();

    public boolean isMarked(Transition transition) {
        var state = states.get(transition);
        return state != null && state.marked;
    }

    public void resetState() {
        states = new HashMap<>();
        unresolvedConflicts = new HashMap<>();
        naturalTokenSources = new HashSet<>();
        summedTokenStates = new LinkedHashMap<>();
    }

    public void markTransition(Transition transition) {
        State state = states.get(transition);
        if (state == null) {
            state = new State(transition);
        }
        state.marked = true;
        states.put(transition, state);
    }

    public void addState(Transition transition, TokenState tokenState) {
        if(states.containsKey(transition)) {
            states.get(transition).tokenState = tokenState;
            return;
        }
        states.put(transition, new State(transition, tokenState));
    }

    public void addState(Transition transition, State state) {
        states.put(transition, state);
    }

    public void addConflict(Transition transition, Conflict conflict) {
        unresolvedConflicts.put(conflict.getMask(), conflict);
        if(states.containsKey(transition)) {
            State state = states.get(transition);
            if(state.conflict != null) {
                return;
            }
            state.conflict = conflict;
            state.tokenState = null;
            states.put(transition, state);
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
}
