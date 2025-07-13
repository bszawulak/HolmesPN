package holmes.analyse.firingalgo;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;

public class DetermineFiringDelayAlgo implements Runnable {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private ArrayList<Transition> LT = new ArrayList<Transition>();

    @Override
    public void run() {
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        LT.clear();

        try {
            setupLTArray(transitions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Stack<Place> stack = new Stack<>();
        HashSet<Place> markedPlaces = new HashSet<Place>();

        for (Transition transition : LT) {
            dfsPush(transition,
                    stack,
                    markedPlaces);
            if(FiringDelayAlgoHelper.isSyncTransition(transition)) {
                FiringDelayAlgoHelper.markTransition(transition);
                FiringDelayAlgoHelper.process(transition);
            }
        }
    }

    /**
     * Tablica LT zawiera przejścia ułożone w taki sposób, że każde kolejne przejście zależy tylko od przejść poprzednich.
     */
    private void setupLTArray(ArrayList<Transition> transitions) throws Exception {
        LT.clear();

        LinkedList<Transition> availableTransitions = new LinkedList<Transition>(transitions);
        HashSet<Transition> markedTransitions = new HashSet<Transition>();
        HashSet<Transition> temporaryMarkedTransitions = new HashSet<Transition>();
        while (!availableTransitions.isEmpty()) {
            Transition transition = availableTransitions.poll();
            visitTransitionLT(transition, markedTransitions, temporaryMarkedTransitions, LT);
        }

        ArrayList<Transition> output = new ArrayList<Transition>();
        List<Transition> reversed = new ArrayList<Transition>(LT);
        Collections.reverse(reversed);
        for (Transition transition : reversed) {
            if (FiringDelayAlgoHelper.isSyncTransition(transition)) {
                output.add(transition);
            }
        }

        List<Transition> sinkTransitions =
                transitions.stream().filter(FiringDelayAlgoHelper::isSinkTransition).toList();
        output.addAll(sinkTransitions);
        LT = output;
    }

    private void visitTransitionLT(
            Transition transition,
            HashSet<Transition> markedTransitions,
            HashSet<Transition> temporaryMarkedTransitions,
            ArrayList<Transition> output) throws Exception {
        if (markedTransitions.contains(transition)) {
            return;
        }
        if (temporaryMarkedTransitions.contains(transition)) {
            throw new Exception("Graph contains a loop");
        }

        temporaryMarkedTransitions.add(transition);

        for (Place place : transition.getInputPlaces()) {
            for (Transition precedingTransition: place.getInputTransitions()) {
                visitTransitionLT(precedingTransition, markedTransitions, temporaryMarkedTransitions, output);
            }
        }

        markedTransitions.add(transition);
        output.add(0, transition);
    }

    private ArrayList<Place> getInputPlacesByNormalArcs(Transition transition) {
        ArrayList<Place> places = transition.getInputPlaces();
        places.removeIf(place -> transition.getInputArcFrom(place).getArcType() != Arc.TypeOfArc.NORMAL);
        return places;
    }

    private void dfsPush(Transition transition,
                         Stack<Place> stack,
                         HashSet<Place> markedPlaces) {
        for (Place previousPlace : transition.getInputPlaces()) {
            if(FiringDelayAlgoHelper.isConflictPlace(previousPlace)) {
                if(!markedPlaces.contains(previousPlace)) {
                    FiringDelayAlgoHelper.markPlaceAsConflict(previousPlace);
                    markedPlaces.add(previousPlace);
                }
            }

            stack.push(previousPlace);
            for (Transition previousTransition : previousPlace.getInputTransitions()) {
                FiringDelayAlgoHelper.markTransition(previousTransition);

                if (FiringDelayAlgoHelper.isSourceTransition(previousTransition)
                    || FiringDelayAlgoHelper.isProcessed(previousTransition)) {
                    dfsPop(stack);
                }
                else {
                    dfsPush(previousTransition,
                            stack,
                            markedPlaces);
                }
            }
        }
    }

    private void dfsPop(Stack<Place> stack) {
        while (!stack.isEmpty()) {
            Place place = stack.peek();
            if (!FiringDelayAlgoHelper.areMarked(place.getInputTransitions())) {
                break;
            }
            else {
                for (Transition transition : place.getInputTransitions()) {
                    FiringDelayAlgoHelper.process(transition);
                }
                stack.pop();
            }
        }
    }
}
