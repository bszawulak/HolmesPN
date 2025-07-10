package holmes.analyse.firingalgo;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;

public class DetermineFiringDelayAlgo implements Runnable {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private ArrayList<Transition> sourceTransitions = new ArrayList<Transition>();
    private ArrayList<Transition> syncTransitions = new ArrayList<Transition>();
    private ArrayList<Transition> sinkTransitions = new ArrayList<Transition>();
    private ArrayList<Transition> LT = new ArrayList<Transition>();
    private Hashtable<Transition, ArrayList<String>> transitionToEquation6 = new Hashtable<Transition, ArrayList<String>>();

    @Override
    public void run() {
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        transitionToEquation6.clear();

        setupTransitions(transitions);

        try {
            setupLTArray(transitions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Stack<Place> stack = new Stack<>();
        HashSet<Transition> Te = new HashSet<Transition>();

        for (Transition transition : LT) {
            dfsPush(transition,
                    stack,
                    Te);
            if (syncTransitions.contains(transition)) {
                equation6(transition);
            }
        }


    }

    private void setupTransitions(ArrayList<Transition> transitions) {
        sourceTransitions.clear();
        syncTransitions.clear();
        sinkTransitions.clear();

        for (Transition transition : transitions) {
            if (transition.getInputArcs().size() <= 0) {
                sourceTransitions.add(transition);
            } else if (transition.getInputArcs().size() > 1) {
                syncTransitions.add(transition);
            }
            if (transition.getOutputArcs().size() <= 0) {
                sinkTransitions.add(transition);
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
            if (syncTransitions.contains(transition)) {
                output.add(transition);
            }
        }
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

    private void equation6(Transition transition) {
        ArrayList<String> equality = new ArrayList<String>();
        for (Place place: getInputPlacesByNormalArcs(transition)) {
            equality.add(equation3(place));
        }
        transitionToEquation6.put(transition, equality);
    }

    private String equation3(Place place) {
        StringJoiner sum = new StringJoiner("+");
        for (Transition transition : place.getInputTransitions()) {
            if (transition.getInputPlaces().isEmpty()) {
                sum.add(transition.spnExtension.getFiringRate() + '*' + transition.getName());
                continue;
            }

            Place previousPlace = getInputPlacesByNormalArcs(transition).get(0);
            double alpha = transition.getInputArcWeightFrom(previousPlace);
            double beta = transition.getOutputArcWeightTo(place);
            double newWeight = beta / alpha;

            if (transitionToEquation6.contains(transition)) {
                String savedEquation6 = transitionToEquation6.get(transition).get(0);
                sum.add(newWeight + '*' + savedEquation6);
            }
            else {
                sum.add(newWeight + '*' + equation3(previousPlace));
            }
        }
        return sum.toString();
    }

    private void dfsPush(Transition transition,
                         Stack<Place> stack,
                         HashSet<Transition> Te) {
        for (Place previousPlace : transition.getInputPlaces()) {
            if(FiringDelayAlgoHelper.isConflictPlace(previousPlace)) {
                FiringDelayAlgoHelper.markPlaceAsConflict(previousPlace);
            }

            stack.push(previousPlace);
            for (Transition previousTransition : previousPlace.getInputTransitions()) {
                FiringDelayAlgoHelper.markTransition(transition);

                if (sourceTransitions.contains(previousTransition)
                    || syncTransitions.contains(previousTransition)
                    || Te.contains(previousTransition)) {
                    dfsPop(stack);
                }
                else {
                    dfsPush(previousTransition,
                            stack,
                            Te);
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
                stack.pop();
                if (FiringDelayAlgoHelper.isConflictPlace(place)) {
                    //EQ2
                }
                else {
                    //EQ3
                }
            }
        }
    }
}
