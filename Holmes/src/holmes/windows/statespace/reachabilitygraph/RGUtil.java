package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.data.PetriNet;

import java.util.List;

public interface RGUtil {

    void generateReachabilityGraph();

    ReachabilityGraph constructReachabilityGraph(List<Transition> transitions, Marking initialMarking);

    void handleBigger(Marking newMarking, Marking existingMarking);

    void handleSmaller(Marking newMarking, Marking existingMarking);

    Marking getActualMarking(PetriNet net);

    void printRGresult(ReachabilityGraph graph);

    List<Transition> getTransitionsFromHolmes(PetriNet net);

}
