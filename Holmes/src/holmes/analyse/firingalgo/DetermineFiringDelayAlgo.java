package holmes.analyse.firingalgo;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;

public class DetermineFiringDelayAlgo implements Runnable {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private ArrayList<Transition> sourceTransitions = new ArrayList<Transition>();
    private ArrayList<Transition> syncTransitions = new ArrayList<Transition>();
    private ArrayList<Transition> sinkTransitions = new ArrayList<Transition>();
    private LinkedList<Transition> LT = new LinkedList<Transition>();

    @Override
    public void run() {
        ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();

        setupTransitions(transitions);

        try {
            setupLTArray(transitions);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            VisitTransitionLT(transition, markedTransitions, temporaryMarkedTransitions, LT);
        }

        LinkedList<Transition> output = new LinkedList<Transition>();
        Iterator<Transition> iterator = LT.descendingIterator();
        while (iterator.hasNext()) {
            Transition transition = iterator.next();
            if (syncTransitions.contains(transition)) {
                output.add(transition);
            }
        }
        output.addAll(sinkTransitions);
        LT = output;
    }

    private void VisitTransitionLT(
            Transition transition,
            HashSet<Transition> markedTransitions,
            HashSet<Transition> temporaryMarkedTransitions,
            LinkedList<Transition> output) throws Exception {
        if (markedTransitions.contains(transition)) {
            return;
        }
        if (temporaryMarkedTransitions.contains(transition)) {
            throw new Exception("Graph contains a loop");
        }

        temporaryMarkedTransitions.add(transition);

        for (Place place : transition.getInputPlaces()) {
            for (Transition precedingTransition: place.getInputTransitions()) {
                VisitTransitionLT(precedingTransition, markedTransitions, temporaryMarkedTransitions, output);
            }
        }

        markedTransitions.add(transition);
        output.add(0, transition);
    }

    private double EQ1(Transition transition) {
        ArrayList<Place> previousPlaces = transition.getInputPlaces();

        double sumForPlaces = 0;
        for (Place place : previousPlaces) {
            ArrayList<Transition> previousTransitions = place.getInputTransitions();
            double sumForTransitions = 0;
            for (Transition previousTransition : previousTransitions) {
                sumForTransitions += CalculateFiringRateForSinglePath(previousTransition);
            }
            sumForPlaces += sumForTransitions / transition.getInputArcWeightFrom(place);
        }
        return sumForPlaces;
    }

    private double CalculateFiringRateForSinglePath(Transition transition) {
        if(transition.getInputPlaces().isEmpty())
            return transition.spnExtension.getFiringRate();

        Place previousPlace = transition.getInputPlaces().get(0);
        Transition previousTransition = previousPlace.getInputTransitions().get(0);
        double alpha = transition.getInputArcWeightFrom(previousPlace);
        double beta = previousTransition.getOutputArcWeightTo(previousPlace);
        return previousTransition.spnExtension.getFiringRate() * beta / alpha;
    }
}
