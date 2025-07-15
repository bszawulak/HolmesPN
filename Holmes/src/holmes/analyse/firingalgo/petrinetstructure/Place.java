package holmes.analyse.firingalgo.petrinetstructure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Place extends Node {
    public holmes.petrinet.elements.Place placeRef = null;

    protected Place() {
        this(new HashSet<>(), new HashSet<>(), null);
    }

    protected Place(holmes.petrinet.elements.Place placeRef) {
        this(new HashSet<>(), new HashSet<>(), placeRef);
    }

    protected Place(Set<Arc> inputArcs, Set<Arc> outputArcs) {
        this(inputArcs, outputArcs, null);
    }

    protected Place(Set<Arc> inputArcs, Set<Arc> outputArcs, holmes.petrinet.elements.Place placeRef) {
        super(inputArcs, outputArcs);
        this.placeRef = placeRef;
    }

    public List<Transition> getInputTransitions() {
        return getInputNodes();
    }

    public List<Transition> getOutputTransitions() {
        return getOutputNodes();
    }
}
