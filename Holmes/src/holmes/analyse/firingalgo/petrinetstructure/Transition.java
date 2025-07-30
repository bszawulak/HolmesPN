package holmes.analyse.firingalgo.petrinetstructure;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.function.Predicate;

public class Transition extends Node {
    public holmes.petrinet.elements.Transition transitionRef = null;
    public Double firingRate = null;

    protected Transition() {
        this(new HashSet<>(), new HashSet<>(), null);
    }

    protected Transition(holmes.petrinet.elements.Transition transitionRef) {
        this(new HashSet<>(), new HashSet<>(), transitionRef);
    }

    protected Transition(Set<Arc> inputArcs, Set<Arc> outputArcs) {
        this(inputArcs, outputArcs, null);
    }

    protected Transition(Set<Arc> inputArcs, Set<Arc> outputArcs, holmes.petrinet.elements.Transition transitionRef) {
        super(inputArcs, outputArcs);
        this.transitionRef = transitionRef;
    }

    public List<Place> getInputPlaces() {
        return getInputPlaces(__ -> true);
    }

    public List<Place> getInputPlaces(Predicate<Arc> arcFilter) {
        return getInputNodes(arcFilter);
    }

    public List<Place> getOutputPlaces() {
        return getOutputPlaces(__ -> true);
    }

    public List<Place> getOutputPlaces(Predicate<Arc> arcFilter) {
        return getOutputNodes(arcFilter);
    }

    public boolean isSource() {
        return inputArcs.size() <= 0;
    }

    public boolean isSync() {
        return inputArcs.size() > 1;
    }

    public boolean isSink() {
        return outputArcs.size() <=0 ;
    }
}
