package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

import java.util.*;
import java.util.stream.Collectors;

public class RGUtilImpl implements RGUtil {


    @Override
    public void generateReachabilityGraph() {

    }

    public ReachabilityGraph constructReachabilityGraph(List<Transition> transitions, Marking initialMarking) {
        ReachabilityGraph graph = new ReachabilityGraph();
        Queue<Marking> toProcess = new LinkedList<>();

        graph.addNode(initialMarking);
        toProcess.add(initialMarking);

        while (!toProcess.isEmpty()) {
            Marking current = toProcess.poll();

            for (Transition transition : transitions) {
                if (!transition.isActive()) {
                    continue;
                }

                Marking newMarking = null; //transition.fire(current);

                boolean handled = false;
                for (Marking existing : graph.markings) {
                    if (newMarking.equals(existing)) {
                        graph.addEdge(current, transition.getName(), existing);
                        break;
                    } else if (newMarking.greaterThan(existing)) {
                        handleBigger(newMarking, existing);
                        graph.addEdge(current, transition.getName(), existing);
                        break;
                    } else if (newMarking.lessThan(existing)) {
                        handleSmaller(newMarking, existing);
                        graph.addEdge(current, transition.getName(), existing);
                        break;
                    } else {
                        graph.addNode(newMarking);
                        graph.addEdge(current, transition.getName(), newMarking);
                        toProcess.add(newMarking);
                    }
                }

            }
        }

        return graph;
    }

    public void handleBigger(Marking newMarking, Marking existingMarking) {
        for (String place : newMarking.places.keySet()) {
            int diff = newMarking.places.get(place) - existingMarking.places.get(place);
            if (diff > 0) {
                newMarking.places.put(place, Integer.MAX_VALUE); // Reprezentacja nieskończoności
            }
        }
    }

    public void handleSmaller(Marking newMarking, Marking existingMarking) {
        for (String place : newMarking.places.keySet()) {
            int diff = existingMarking.places.get(place) - newMarking.places.get(place);
            if (diff > 0) {
                newMarking.places.put(place, 0); // Minimalizowanie tokenów
            }
        }
    }

//    Marking fire(Marking marking, Transition transition) {
//        //Macierz incydencji
//        Map<String, Integer> newPlaces = new HashMap<>(marking.places);
//
//        for (String place : transition.getInputPlaces().keySet()) {
//            newPlaces.put(place, newPlaces.getOrDefault(place, 0) - input.get(place));
//        }
//        for (String place : output.keySet()) {
//            newPlaces.put(place, newPlaces.getOrDefault(place, 0) + output.get(place));
//        }
//
//        return new Marking(newPlaces);
//    }

    public Marking getActualMarking(PetriNet net) {
        ArrayList<Place> plcs = net.getPlaces();
        if (plcs.isEmpty()) {
            System.out.println("ERR: No places in the net");
            return new Marking(new HashMap<>());
        }
        return new Marking(plcs.stream().collect(Collectors.toMap(Place::getName, Place::getTokensNumber)));
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
