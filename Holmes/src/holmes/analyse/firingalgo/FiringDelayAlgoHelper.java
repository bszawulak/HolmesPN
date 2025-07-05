package holmes.analyse.firingalgo;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.ArrayList;
import java.util.HashMap;

public class FiringDelayAlgoHelper {

    /// oznacza tranzycje jako odwiedzoną na podstawie poprzednich tranzycji
    public static void markTransition(Transition transition) {
        FiringDelayAlgoStateHolder stateHolder = FiringDelayAlgoStateHolder.getInstance();
        if (!stateHolder.isMarked(transition)) {
            return;
        }

        if (isSourceTransition(transition)) {
            stateHolder.markTransition(transition, null, 0);
            return;
        }

        HashMap<Integer, Double> markValue = new HashMap<>();
        for (Place place : transition.getInputPlaces()) {
            for (Transition inputTransition : place.getInputTransitions()) {
                double multiplier = (double) inputTransition.getOutputArcWeightTo(place) / transition.getInputArcWeightFrom(place);
                HashMap<Integer, Double> weights = stateHolder.getWeight(inputTransition);
                for (Integer conflictPlaceId : weights.keySet()) {
                    //TODO synchronizacja
                    markValue.put(conflictPlaceId, weights.get(conflictPlaceId) * multiplier);
                }
            }

            if (isConflictPlace(place)) {
                markValue.put(place.getID(), (double) transition.getInputArcWeightFrom(place));
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

    public static boolean isSourceTransition(Transition transition) {
        return transition.getInputArcs().size() <= 0;
    }

    public static boolean isConflictPlace(Place place) {
        return place.getOutputTransitions().size() > 1;
    }
}
