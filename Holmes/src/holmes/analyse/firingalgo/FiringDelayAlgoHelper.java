package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.ArrayList;
import java.util.HashMap;

public class FiringDelayAlgoHelper {

    public static void markTransition(Transition transition) {
        FiringDelayAlgoStateHolder stateHolder = FiringDelayAlgoStateHolder.getInstance();
        if (!stateHolder.isMarked(transition)) {
            return;
        }

        HashMap<Integer, Double> markValue = new HashMap<>();
        for (Place place : transition.getInputPlaces()) {
            for (Transition inputTransition : place.getInputTransitions()) {
                 markValue.putAll(stateHolder.getWeight(inputTransition));
            }
        }
        stateHolder.markTransition(transition, markValue);
    }

    public static boolean areMarked(ArrayList<Transition> transitions) {
        for (Transition transition : transitions) {
            if(!FiringDelayAlgoStateHolder.getInstance().isMarked(transition))
                return false;
        }
        return true;
    }
}
