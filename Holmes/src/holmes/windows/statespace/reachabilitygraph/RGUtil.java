package holmes.windows.statespace.reachabilitygraph;

public interface RGUtil {

    ReachabilityGraph constructReachabilityGraph();

    void handleBigger(Marking newMarking, Marking existingMarking);

    void handleSmaller(Marking newMarking, Marking existingMarking);

    void printRGresult(ReachabilityGraph graph);


}
