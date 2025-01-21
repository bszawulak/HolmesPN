package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.elements.Transition;

public interface RGUtil {

    ReachabilityGraph constructReachabilityGraph();

    void handleBigger(Marking current, Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition);

    void handleSmaller(Marking newMarking, Marking existingMarking);

    void printRGresult(ReachabilityGraph graph);


}
