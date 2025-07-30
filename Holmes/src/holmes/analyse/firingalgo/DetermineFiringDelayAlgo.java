package holmes.analyse.firingalgo;

import holmes.analyse.firingalgo.petrinetstructure.Arc;
import holmes.analyse.firingalgo.petrinetstructure.PetriNetStructure;
import holmes.analyse.firingalgo.petrinetstructure.Place;
import holmes.analyse.firingalgo.petrinetstructure.Transition;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SPNdataVector;

import java.util.*;

public class DetermineFiringDelayAlgo {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private PetriNetStructure structure;
    private ArrayList<Transition> LT = new ArrayList<Transition>();

    public void run(PetriNet petriNet, SPNdataVector spnVector) {
        structure = new PetriNetStructure(petriNet, spnVector);

        List<Transition> transitions = structure.getTransitions();
        LT.clear();
        FiringDelayAlgoHelper.resetState();

        setupLTArray(transitions);

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
    private void setupLTArray(List<Transition> transitions) {
        LT.clear();

        LinkedList<Transition> availableTransitions = new LinkedList<Transition>(transitions);
        HashSet<Transition> markedTransitions = new HashSet<Transition>();
        HashSet<Transition> temporaryMarkedTransitions = new HashSet<Transition>();
        while (!availableTransitions.isEmpty()) {
            Transition transition = availableTransitions.poll();
            visitTransitionLT(transition, null, markedTransitions, temporaryMarkedTransitions, LT);
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
            Place targetPlace,
            HashSet<Transition> markedTransitions,
            HashSet<Transition> temporaryMarkedTransitions,
            ArrayList<Transition> output) {
        if (markedTransitions.contains(transition)) {
            return;
        }
        if (temporaryMarkedTransitions.contains(transition)) {
            // rozcięcie cyklu poprzez stworzenie nowej tranzycji
            Transition newSource = structure.createNewTransition();
            Arc arc = transition.getOutputArcToNode(targetPlace).get();
            newSource.transitionRef = transition.transitionRef;
            arc.setIn(newSource);
            markedTransitions.add(newSource);
            return;
        }

        temporaryMarkedTransitions.add(transition);

        for (Place place : transition.getInputPlaces()) {
            for (Transition precedingTransition: place.getInputTransitions()) {
                visitTransitionLT(precedingTransition, place, markedTransitions, temporaryMarkedTransitions, output);
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
