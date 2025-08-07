package holmes.analyse.firingalgo.petrinetstructure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

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
        return getInputTransitions(__ -> true);
    }

    public List<Transition> getInputTransitions(Predicate<Arc> arcFilter) {
        return getInputNodes(arcFilter);
    }

    public List<Transition> getOutputTransitions() {
        return getOutputTransitions(__ -> true);
    }

    public List<Transition> getOutputTransitions(Predicate<Arc> arcFilter) {
        return getOutputNodes(arcFilter);
    }

    public boolean isConflict() {
        return outputArcs.size() > 1;
    }

    public boolean isSumming() {
        return inputArcs.size() > 1;
    }
}
