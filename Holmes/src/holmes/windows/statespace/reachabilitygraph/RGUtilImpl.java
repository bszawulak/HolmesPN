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
        this.iMatrix = RGUtil.getIncidenceMatrix(net);
        this.net = net;
    }

    /**
     * Generates reachability graph for given Petri Net
     */
    public ReachabilityGraph constructReachabilityGraph() {
        ReachabilityGraph graph = new ReachabilityGraph();
        Queue<Marking> toProcess = new LinkedList<>();

        //graph.addNode(mMarking);
        toProcess.add(mMarking);

        while (!toProcess.isEmpty()) {
            Marking current = toProcess.poll();

            // Tu trzeba ustawiac tokeny w miejscach na podstawie current
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
                    } else if (newMarking.equals(mMarking)) {
                        graph.addEdge(current, transition.getName(), mMarking);
                        handled = true;
                        break;
                    } else if (newMarking.greaterThan(existing)) {
                        handled = handleBigger(newMarking, existing, graph, transition);
                        break;
                    } else if (newMarking.lessThan(existing)) {
                        handled = handleSmaller(newMarking, existing, graph, transition);
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
        graph.addNode(mMarking);
        return graph;
    }

    private boolean handleBigger(Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition) {
        if (RGUtil.postsetContainsPreset(transition)) {
            for (String place : newMarking.places.keySet()) {
                int tokenDiff = newMarking.places.get(place) - existingMarking.places.get(place);
                if (tokenDiff > 0) {
                    Place placeOrig = net.getPlaces().stream().filter(plc -> plc.getName().equals(place)).findFirst().orElse(null);
                    if (placeOrig == null) {
                        throw new NullPointerException(String.format("Place %s not found in the net", place));
                    }
                    int distance = transition.getOutputArcWeightTo(placeOrig) - transition.getInputArcWeightFrom(placeOrig);
                    if (tokenDiff % distance == 0) {
                        if (existingMarking.nTokens.get(place)) {
                            newMarking.places.put(place, existingMarking.places.get(place));
                            newMarking.nTokens.put(place, true);
                        } else {
                            newMarking.places.put(place, distance);
                            newMarking.nTokens.put(place, true); //Zapisuje informacje o n
                            existingMarking.places.put(place, distance);
                            existingMarking.nTokens.put(place, true);
                        }
                    }

                }
            }
        }
        if (newMarking.equals(existingMarking)) {
            graph.addEdge(existingMarking, transition.getName(), existingMarking);
            //Stan dodawany jest w petli nad tą metodą. newMarking to referencja
        } else if (newMarking.greaterOneThan(existingMarking) != null) {
            String greaterOne = newMarking.greaterOneThan(existingMarking);
            newMarking.nTokens.put(greaterOne, true);
            if (existingMarking.nTokens.get(greaterOne)){
                return true;
            } else {
                existingMarking.nTokens.put(greaterOne, true);
                return false;
            }
        } else {
            //graph.removeMarking(existingMarking);
            return false;
        }
        return true;
    }

    private boolean handleSmaller(Marking newMarking, Marking existingMarking, ReachabilityGraph graph, Transition transition) {
        boolean handled = true;
        if (RGUtil.presetContainsPostset(transition)) {
            Marking newNewMarking = new Marking(new HashMap<>(existingMarking.places), new HashMap<>(existingMarking.nTokens));
            for (String place : newMarking.places.keySet()) {
                int tokenDiff = existingMarking.places.get(place) - newMarking.places.get(place);
                if (tokenDiff > 0) { //Zamieniona kolejnosc wyzej
                    Place placeOrig = net.getPlaces().stream().filter(plc -> plc.getName().equals(place)).findFirst().orElse(null);
                    if (placeOrig == null) {
                        System.out.println("ERROR: Place not found in the net");
                    }
                    int distance = transition.getOutputArcWeightTo(placeOrig) - transition.getInputArcWeightFrom(placeOrig);
                    if (existingMarking.nTokens.get(place)) {
                        if (tokenDiff % distance == 0) {
                            newMarking.places.put(place, existingMarking.places.get(place));
                        }
                        newNewMarking.nTokens.put(place, false);
                        if (newNewMarking.places.get(place) > distance) {
                            newNewMarking.places.put(place, newNewMarking.places.get(place) - distance);
                        }
                    }
                }
            }
            if (isDead(newNewMarking)) {
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

    private boolean isDead(Marking newMarking) {
        Marking actualMarking = Marking.getActualMarking(net);
        net.getPlaces().forEach(place -> place.setTokensNumber(newMarking.places.get(place.getName())));
        boolean result = net.getTransitions().stream().noneMatch(Transition::isActive);
        net.getPlaces().forEach(place -> place.setTokensNumber(actualMarking.places.get(place.getName())));
        return result;
    }

    private Marking fire(Marking actualMarking, Transition transition) {

        // Return 1 when transLaunchList contains transition or 0 when not
        double[] tArray = net.getTransitions().stream()
                .mapToDouble(trans -> trans.equals(transition) ? 1 : 0)
                .toArray();

        // Convert the result array to a RealVector
        RealVector tVector = new ArrayRealVector(tArray);
        RealVector realVector = RGUtil.calculateNextState(iMatrix, actualMarking.toVector(), tVector);
        return Marking.fromVector(realVector, net, actualMarking);
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
