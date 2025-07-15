package holmes.analyse.firingalgo.petrinetstructure;

import java.util.HashSet;
import java.util.Set;
import java.util.List;

public class Transition extends Node {
    public holmes.petrinet.elements.Transition transitionRef = null;

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

    public List<Transition> getInputTransitions() {
        return getInputNodes();
    }

    public List<Transition> getOutputTransitions() {
        return getOutputNodes();
    }
}
