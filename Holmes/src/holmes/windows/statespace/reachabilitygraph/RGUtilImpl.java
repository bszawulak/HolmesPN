package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.simulators.SimulatorStandardPN;
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
                        handleBigger(newMarking, existing);
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
    private Marking fire2(Marking actualMarking) {
        ArrayList<Place> places = net.getPlaces();
        places.forEach(place -> place.setTokensNumber(actualMarking.places.get(place.getName())));

        SimulatorStandardPN simulator = new SimulatorStandardPN();
        simulator.setEngine(SimulatorGlobals.SimNetType.BASIC, false, false,
                net.getTransitions(), net.getTimeTransitions(), net.getPlaces());
        // UWAGA - ta metoda zwraca listę tranzycji, które można odpalić w danej chwili już z ustaleniem pierwszeństwa
        // jeśli jest więcej niż jedna możliwość odpalenia to prawdopodobnie stracimy info o nieodpalonych
        // TA metoda pracuje na aktualnej sieci w Holmes a nie na aktualnym markingu
        // Czy wystarczy podmienić net.getPlaces na podłożone?
        // Mozna zmienic na szybko Marking? TAK, to działa
        ArrayList<Transition> transLaunchList = simulator.getTransLaunchList(false);


        // Return Array 1 when transLaunchList contains transition or 0 when not
        double[] resultArray = net.getTransitions().stream()
                .mapToDouble(transition -> transLaunchList.contains(transition) ? 1 : 0)
                .toArray();

        // Convert the result array to a RealVector
        RealVector resultVector = new ArrayRealVector(resultArray);
        RealVector realVector = calculateNextState(iMatrix, actualMarking.toVector(), resultVector);
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
