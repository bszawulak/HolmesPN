package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.elements.Transition;

public interface RGUtil {

    ReachabilityGraph constructReachabilityGraph();

    boolean  handleBigger(Marking current, Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition);

    boolean handleSmaller(Marking current, Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition);
    void printRGresult(ReachabilityGraph graph);


}
