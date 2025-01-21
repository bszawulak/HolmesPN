package holmes.windows.statespace.reachabilitygraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReachabilityGraph {

    Set<Marking> markings = new HashSet<>();
    Map<Marking, Map<String, Marking>> edges = new HashMap<>();

    public void addNode(Marking marking) {
        markings.add(marking);
    }

    public void addEdge(Marking from, String transitionName, Marking to) {
        edges.computeIfAbsent(from, k -> new HashMap<>()).put(transitionName, to);
    }

    public void removeMarking(Marking marking) {
        if (markings.contains(marking)) {
            markings.remove(marking);
            edges.remove(marking);
        } else {
            System.out.println("ERROR: Marking should be in the graph, but it isn't");
            //TODO: throw new IllegalArgumentException("Marking not found in the graph");
        }

    }

    public boolean contains(Marking marking) {
        return markings.contains(marking);
    }

}
