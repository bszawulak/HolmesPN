package holmes.analyse.firingalgo.petrinetstructure;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SPNdataVector;

import java.util.*;

/**
 * Klasa pozwalająca na stworzenie pomocniczej edytowalnej kopii sieci bez ingerencji w sieć w projekcie.
 */
public class PetriNetStructure {
    private ArrayList<Place> places = new ArrayList<Place>();
    private ArrayList<Transition> transitions = new ArrayList<Transition>();
    private ArrayList<Arc> arcs = new ArrayList<Arc>();

    public PetriNetStructure(PetriNet petriNet, SPNdataVector spnVector) {
        petriNet.getArcs().forEach(pnArc -> {
            holmes.petrinet.elements.Node pnIn = pnArc.getStartNode();
            holmes.petrinet.elements.Node pnOut = pnArc.getEndNode();
            Node in = initializeOrGetNode(pnIn);
            Node out = initializeOrGetNode(pnOut);
            Arc arc = new Arc(in, out, pnArc);
            arcs.add(arc);
        });

        ArrayList<holmes.petrinet.elements.Transition> pnTransitions = petriNet.getTransitions();
        for (int i = 0; i < pnTransitions.size(); i++) {
            holmes.petrinet.elements.Transition pnTransition = pnTransitions.get(i);
            double fr = spnVector.getFiringRate(i);
            transitions.stream().filter(t -> t.transitionRef.equals(pnTransition))
                    .forEach(t -> t.firingRate = fr >= 0 ? fr : null);
        }
    }

    private Node initializeOrGetNode(holmes.petrinet.elements.Node pnNode) {
        Optional<Place> place = places.stream().filter(p -> p.placeRef == pnNode).findFirst();
        if (place.isPresent()) {
            return place.get();
        }

        Optional<Transition> transition = transitions.stream().filter(t -> t.transitionRef == pnNode).findFirst();
        if (transition.isPresent()) {
            return transition.get();
        }

        Node node = null;
        if (pnNode instanceof holmes.petrinet.elements.Place pnPlace) {
            node = new Place(pnPlace);
            places.add((Place) node);
        } else if (pnNode instanceof holmes.petrinet.elements.Transition pnTransition) {
            node = new Transition(pnTransition);
            transitions.add((Transition) node);
        }
        return node;
    }

    public Transition createNewTransition() {
        Transition transition = new Transition();
        transitions.add(transition);
        return transition;
    }

    public Arc createNewArc(Transition in, Place out) {
        return createNewArc(in, (Node)out);
    }

    public Arc createNewArc(Place in, Transition out) {
        return createNewArc(in, (Node)out);
    }

    private Arc createNewArc(Node in, Node out) {
        Arc arc = new Arc(in, out);
        arcs.add(arc);
        return arc;
    }

    public List<Place> getPlaces() {
        return Collections.unmodifiableList(places);
    }

    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    public List<Arc> getArcs() {
        return Collections.unmodifiableList(arcs);
    }
}
