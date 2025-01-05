package holmes.windows.statespace.reachabilitygraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReachabilityGraph {

    Set<Marking> markings = new HashSet<>();
    Map<Marking, Map<String, Marking>> edges = new HashMap<>();

    void addNode(Marking marking) {
        markings.add(marking);
    }

    void addEdge(Marking from, String transitionName, Marking to) {
        edges.computeIfAbsent(from, k -> new HashMap<>()).put(transitionName, to);
    }

    boolean contains(Marking marking) {
        return markings.contains(marking);
    }

}
