package holmes.analyse.firingalgo.petrinetstructure;

import java.util.*;

public abstract class Node {
    protected HashSet<Arc> inputArcs = new HashSet<>();
    protected HashSet<Arc> outputArcs = new HashSet<>();

    protected Node(Set<Arc> inputArcs, Set<Arc> outputArcs) {
        inputArcs.forEach(this::addInputArc);
        outputArcs.forEach(this::addOutputArc);
    }

    public Set<Arc> getInputArcs() {
        return Collections.unmodifiableSet(inputArcs);
    }

    protected <T extends Node> List<T> getInputNodes() {
        ArrayList<T> result = new ArrayList<T>();
        getInputArcs().stream()
                .map(arc -> {return (T) arc.getIn();})
                .forEach(result::add);
        return Collections.unmodifiableList(result);
    }

    public void addInputArc(Arc arc) {
        if (inputArcs.contains(arc)) {
            return;
        }

        inputArcs.add(arc);
        arc.setOut(this);
    }

    public void removeInputArc(Arc arc) {
        if (!inputArcs.contains(arc)) {
            return;
        }

        inputArcs.remove(arc);
        arc.setOut(null);
    }

    public Set<Arc> getOutputArcs() {
        return Collections.unmodifiableSet(outputArcs);
    }

    protected <T extends Node> List<T> getOutputNodes() {
        ArrayList<T> result = new ArrayList<T>();
        getOutputArcs().stream()
                .map(arc -> {return (T) arc.getOut();})
                .forEach(result::add);
        return Collections.unmodifiableList(result);
    }

    public void addOutputArc(Arc arc) {
        if (outputArcs.contains(arc)) {
            return;
        }

        outputArcs.add(arc);
        arc.setIn(this);
    }

    public void removeOutputArc(Arc arc) {
        if (!outputArcs.contains(arc)) {
            return;
        }

        outputArcs.remove(arc);
        arc.setIn(null);
    }
}
