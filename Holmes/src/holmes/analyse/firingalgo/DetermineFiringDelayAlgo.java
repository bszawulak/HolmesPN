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
        HashSet<Transition> markedTransitions = new HashSet<Transition>();
        HashSet<Place> markedPlaces = new HashSet<Place>();
        HashSet<Transition> Te = new HashSet<Transition>();

        for (Transition transition : LT) {
            dfsPush(transition,
                    stack,
                    markedTransitions,
                    markedPlaces,
                    Te);
            EQ1(transition);
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

        ArrayList<Transition> output = new ArrayList<Transition>();
        List<Transition> reversed = LT.reversed();
        for (Transition transition : reversed) {
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

    private void dfsPush(Transition transition,
                         Stack<Place> stack,
                         HashSet<Transition> markedTransitions,
                         HashSet<Place> markedPlaces,
                         HashSet<Transition> Te) {
        for (Place previousPlace : transition.getInputPlaces()) {
            if(previousPlace.getOutputTransitions().size() > 1)
                markedPlaces.add(previousPlace);

            stack.push(previousPlace);
            for (Transition previousTransition: previousPlace.getInputTransitions()) {

                if (!markedTransitions.contains(previousTransition))
                    markedTransitions.add(previousTransition);

                if (sourceTransitions.contains(previousTransition)
                || syncTransitions.contains(previousTransition)
                || Te.contains(previousTransition)) {
                    dfsPop();
                }
                else {
                    dfsPush(previousTransition,
                            stack,
                            markedTransitions,
                            markedPlaces,
                            Te);
                }
            }
        }
    }

    private void dfsPop() {}
}
