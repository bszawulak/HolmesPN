package holmes.windows.statespace.reachabilitygraph;

import java.util.List;
import java.util.Map;

public class RGMain {

    public static void main(String[] args) {
        Map<String, Integer> initialPlaces = Map.of("P1", 1, "P2", 0, "P3", 1, "P4", 0);
        Marking initialMarking = new Marking(initialPlaces);

        List<Transition> transitions = List.of(
                new Transition("T1", Map.of("P2", 1, "P3", 1, "P1", 1), Map.of("P1", 1)),
                new Transition("T2", Map.of("P4", 1), Map.of("P3", 1, "P2", 1)),
                new Transition("T3", Map.of("P3", 1), Map.of("P4", 1))
        );

        RGUtil rgUtil = new RGUtilImpl();
        ReachabilityGraph graph = rgUtil.constructReachabilityGraph(transitions, initialMarking);
        rgUtil.printRGresult(graph);
    }




}
