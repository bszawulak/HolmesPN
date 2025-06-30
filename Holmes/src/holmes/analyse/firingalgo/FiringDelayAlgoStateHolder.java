package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.HashMap;

public class FiringDelayAlgoStateHolder {
    private HashMap<Integer, HashMap<Integer, Double>> state;

    private static FiringDelayAlgoStateHolder instance;
    public static FiringDelayAlgoStateHolder getInstance() {
        if (instance == null) {
            instance = new FiringDelayAlgoStateHolder();
        }
        return instance;
    }

    public boolean isMarked(Transition transition) {
        return state.containsKey(transition.getID());
    }

    public void resetState() {
        state = new HashMap<>();
    }

    public void markTransition(Transition transition, Place conflictPlace, double weight) {
        HashMap<Integer, Double> transitionState;
        if(!state.containsKey(transition.getID())) {
            transitionState = new HashMap<>();
        }
        else {
            transitionState = state.get(transition.getID());
        }
        transitionState.putIfAbsent(conflictPlace.getID(), weight);
        state.put(transition.getID(), transitionState);
    }

    public void markTransition(Transition transition, HashMap<Integer, Double> transitionState) {
        state.putIfAbsent(transition.getID(), transitionState);
    }

    public double getWeight(Transition transition, Place conflictPlace) {
        return state.get(transition.getID()).get(conflictPlace.getID());
    }

    public HashMap<Integer, Double> getWeight(Transition transition) {
        return state.get(transition.getID());
    }
}
