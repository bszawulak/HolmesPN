package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.PetriNetStructure;
import holmes.analyse.firingalgo.petrinetstructure.Place;
import holmes.analyse.firingalgo.petrinetstructure.Transition;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNetData;

import java.util.*;

public class DetermineFiringDelayAlgo implements Runnable {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private PetriNetStructure structure;
    private ArrayList<Transition> LT = new ArrayList<Transition>();

    @Override
    public void run() {
        PetriNetData data = overlord.getWorkspace().getProject().getDataCore();
        structure = new PetriNetStructure(data);

        List<Transition> transitions = structure.getTransitions();
        LT.clear();
        FiringDelayAlgoHelper.resetState();

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

            FiringDelayAlgoHelper.markTransition(transition);
            FiringDelayAlgoHelper.process(transition);
        }

        FiringDelayAlgoHelper.tieLooseConflicts();
    }

    /**
     * Tablica LT zawiera przejścia ułożone w taki sposób, że każde kolejne przejście zależy tylko od przejść poprzednich.
     */
    private void setupLTArray(List<Transition> transitions) throws Exception {
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
            if (transition.isSync()) {
                output.add(transition);
            }
        }

        List<Transition> sinkTransitions =
                transitions.stream().filter(Transition::isSink).toList();
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

    private void dfsPush(Transition transition,
                         Stack<Place> stack,
                         HashSet<Place> markedPlaces) {
        for (Place previousPlace : transition.getInputPlaces()) {
            if(previousPlace.isConflict()) {
                if(!markedPlaces.contains(previousPlace)) {
                    FiringDelayAlgoHelper.markPlaceAsConflict(previousPlace);
                    markedPlaces.add(previousPlace);
                }
            }

            stack.push(previousPlace);
            for (Transition previousTransition : previousPlace.getInputTransitions()) {
                FiringDelayAlgoHelper.markTransition(previousTransition);

                if (previousTransition.isSource()
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
                    if(!FiringDelayAlgoHelper.isProcessed(transition)) {
                        FiringDelayAlgoHelper.process(transition);
                    }
                }
                stack.pop();
            }
        }
    }

    public HashMap<Transition, Double> getResult() {
        return FiringDelayAlgoHelper.getResult();
    }
}
