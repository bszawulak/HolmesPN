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
                        handled = handleBigger(current, newMarking, existing, graph, transition);
                        break; //TODO: czy break? Czy nie ma szansy na obsługe kliku??
                    } else if (newMarking.lessThan(existing)) {
                        handled = handleSmaller(current, newMarking, existing, graph, transition);
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

    public boolean handleBigger(Marking current, Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition) {
        if (postsetContainsPreset(transition)) {
            for (String place : newMarking.places.keySet()) {
                int tokenDiff = newMarking.places.get(place) - existingMarking.places.get(place);
                if (tokenDiff > 0) {
                    Place placeOrig = net.getPlaces().stream().filter(plc -> plc.getName().equals(place)).findFirst().orElse(null);
                    if (placeOrig == null) {
                        System.out.println("ERROR: Place not found in the net");
                    }
                    int distance = transition.getOutputArcWeightTo(placeOrig) - transition.getInputArcWeightFrom(placeOrig);
                    if (tokenDiff % distance == 0) {
                        if (true /*doesn't contain n*/ ) {
                            int n = 111; // Do testu
                            newMarking.places.put(place, n * distance);
                            existingMarking.places.put(place, n * distance);
                        } else {
                            newMarking.places.put(place, existingMarking.places.get(place));
                        }
                    }

                }
            }
        }
        if (newMarking.equals(existingMarking)) {
            graph.addEdge(existingMarking, transition.getName(), existingMarking);
        } else {
            graph.removeMarking(existingMarking);
            return false;
        }
        return true;
    }

    private boolean postsetContainsPreset(Transition transition) {
        Set<Place> preset = new HashSet<>(transition.getInputPlaces());
        Set<Place> postset = new HashSet<>(transition.getOutputPlaces());
        return postset.containsAll(preset);
    }

    private boolean presetContainsPostset(Transition transition) {
        Set<Place> preset = new HashSet<>(transition.getInputPlaces());
        Set<Place> postset = new HashSet<>(transition.getOutputPlaces());
        return preset.containsAll(postset);
    }

    public boolean handleSmaller(Marking current, Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition) {
        boolean handled = true;
        if (presetContainsPostset(transition)) {
            Marking newNewMarking = new Marking(new HashMap<>(existingMarking.places));
            for (String place : newMarking.places.keySet()) {
                int tokenDiff = existingMarking.places.get(place) - newMarking.places.get(place);
                if (tokenDiff > 0) { //Zamieniona kolejnosc wyzej
                    Place placeOrig = net.getPlaces().stream().filter(plc -> plc.getName().equals(place)).findFirst().orElse(null);
                    if (placeOrig == null) {
                        System.out.println("ERROR: Place not found in the net");
                    }
                    int distance = transition.getOutputArcWeightTo(placeOrig) - transition.getInputArcWeightFrom(placeOrig);
                    if (true /*doesn't contain n*/) {
                        if (tokenDiff % distance == 0) {
                            newMarking.places.put(place, existingMarking.places.get(place));
                        }
                        //TODO: newNewMarking.removeN(place);
                        if (newNewMarking.places.get(place) > distance) {
                            newNewMarking.places.put(place, newNewMarking.places.get(place) - distance);
                        }
                    }
                }
            }
            if (false /*isDead(newNewMarking)*/) {
                handled = false;
            }
            if (graph.markings.contains(newNewMarking) || newNewMarking.equals(mMarking)) {
                handled = false;
            }
            if (newMarking.equals(existingMarking)) {
                graph.addEdge(existingMarking, transition.getName(), existingMarking);
            }
            return handled;
        } else {
            return false;
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
