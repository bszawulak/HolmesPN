package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

public class RGUtilImpl implements RGUtil {

    private final Marking mMarking;
    private final RealMatrix iMatrix;
    private final PetriNet net;

    RGUtilImpl(PetriNet net) {
        this.mMarking = Marking.getActualMarking(net);
        this.iMatrix = getIncidenceMatrix(net);
        this.net = net;
    }

    /**
     * Returns incidence matrix for given Petri Net
     * @param net - Petri Net
     */
    private static RealMatrix getIncidenceMatrix(PetriNet net) {
        IncidenceMatrix incidenceMatrix = new IncidenceMatrix(net);
        incidenceMatrix.printToConsole();
        return incidenceMatrix.get();
    }

    /**
     * Generates reachability graph for given Petri Net
     */
    public ReachabilityGraph constructReachabilityGraph() {
        ReachabilityGraph graph = new ReachabilityGraph();
        Queue<Marking> toProcess = new LinkedList<>();

        graph.addNode(mMarking);
        toProcess.add(mMarking);

        while (!toProcess.isEmpty()) {
            Marking current = toProcess.poll();

            // Tu by trzeba było ustawiac tokeny w miejscach na podstawie current
            net.getPlaces().forEach(place -> place.setTokensNumber(current.places.get(place.getName())));

            for (Transition transition : net.getTransitions()) {
                if (!transition.isActive()) {
                    continue;
                }

                Marking newMarking = fire(current, transition);

                boolean handled = false;
                for (Marking existing : graph.markings) {
                    if (newMarking.equals(existing)) {
                        graph.addEdge(current, transition.getName(), existing);
                        handled = true;
                        break;
                    } else if (newMarking.greaterThan(existing)) {
                        handleBigger(current, newMarking, existing, graph, transition);
                        graph.addEdge(current, transition.getName(), existing);
                        handled = true;
                        break;
                    } else if (newMarking.lessThan(existing)) {
                        handleSmaller(newMarking, existing);
                        graph.addEdge(current, transition.getName(), existing);
                        handled = true;
                        break;
                    }
                }
                if (!handled){
                    graph.addNode(newMarking);
                    graph.addEdge(current, transition.getName(), newMarking);
                    toProcess.add(newMarking);
                }

            }
        }

        return graph;
    }

    public void handleBigger(Marking current, Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition) {
        graph.removeMarking(existingMarking); //TODO: sprawdzić czy nie usuwa za dużo informacji. Może trzeba by było oznaczać do usunięcia
        if (postsetContainsPreset(transition)) {
            for (String place : newMarking.places.keySet()) {
                int distance = newMarking.places.get(place) - existingMarking.places.get(place);
                if (distance > 0) {
                    newMarking.places.put(place, Integer.MAX_VALUE); // Reprezentacja nieskończoności
                }
            }
        }
        if (newMarking.equals(existingMarking)) {
            graph.addNode(existingMarking); //TODO: Moze bedzie do usuniecia jesli to wyzej bedzie spelnione
            graph.addEdge(existingMarking, transition.getName(), existingMarking);
        } else {
            //TODO: wtedy tylko tutaj bedzie usuwanie existing tak naprawde
            graph.addNode(newMarking);
            graph.addEdge(current, transition.getName(), newMarking);
        }
    }

    private boolean postsetContainsPreset(Transition transition) {
        Set<Place> preset = new HashSet<>(transition.getInputPlaces());
        Set<Place> postset = new HashSet<>(transition.getOutputPlaces());
        return postset.containsAll(preset);
    }

    public void handleSmaller(Marking newMarking, Marking existingMarking) {
        for (String place : newMarking.places.keySet()) {
            int diff = existingMarking.places.get(place) - newMarking.places.get(place);
            if (diff > 0) {
                newMarking.places.put(place, 0); // Minimalizowanie tokenów
            }
        }
    }

    private Marking fire(Marking actualMarking, Transition transition) {

        // Return Array 1 when transLaunchList contains transition or 0 when not
        double[] tArray = net.getTransitions().stream()
                .mapToDouble(trans -> trans.equals(transition) ? 1 : 0)
                .toArray();

        // Convert the result array to a RealVector
        RealVector tVector = new ArrayRealVector(tArray);
        RealVector realVector = calculateNextState(iMatrix, actualMarking.toVector(), tVector);
        return Marking.fromVector(realVector, net);
    }

    /**
     * Calculates next state based on incidence matrix, current marking and transition vector
     * Funkcja obliczająca nowy stan M' = M + I * T
     * @param I - incidence matrix
     * @param M - current marking
     * @param T - transition vector
     * @return new marking
     */
    public static RealVector calculateNextState(RealMatrix I, RealVector M, RealVector T) {
        return M.add(I.operate(T));
    }

    public void printRGresult(ReachabilityGraph graph) {
        // Wyświetlenie grafu osiągalności
        System.out.println("Nodes:");
        for (Marking marking : graph.markings) {
//            System.out.println(marking);
            System.out.println(marking.places.values().stream().toList());
        }

        System.out.println("Edges:");
        for (Marking marking : graph.edges.keySet()) {
            for (Map.Entry<String, Marking> edge : graph.edges.get(marking).entrySet()) {
                //System.out.println(marking + " --" + edge.getKey() + "--> " + edge.getValue());
                System.out.println(marking.places.values().stream().toList()
                        + " --" + edge.getKey() + "--> "
                        + edge.getValue().places.values().stream().toList());
            }
        }
    }

}
