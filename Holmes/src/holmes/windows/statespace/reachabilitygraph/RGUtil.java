package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.HashSet;
import java.util.Set;

public interface RGUtil {

    ReachabilityGraph constructReachabilityGraph();

    void printRGresult(ReachabilityGraph graph);


    /**
     * Returns incidence matrix for given Petri Net
     * @param net - Petri Net
     */
    static RealMatrix getIncidenceMatrix(PetriNet net) {
        IncidenceMatrix incidenceMatrix = new IncidenceMatrix(net);
        incidenceMatrix.printToConsole();
        return incidenceMatrix.get();
    }

    static boolean postsetContainsPreset(Transition transition) {
        Set<Place> preset = new HashSet<>(transition.getInputPlaces());
        Set<Place> postset = new HashSet<>(transition.getOutputPlaces());
        return postset.containsAll(preset);
    }

    static boolean presetContainsPostset(Transition transition) {
        Set<Place> preset = new HashSet<>(transition.getInputPlaces());
        Set<Place> postset = new HashSet<>(transition.getOutputPlaces());
        return preset.containsAll(postset);
    }

    /**
     * Calculates next state based on incidence matrix, current marking and transition vector
     * Funkcja obliczająca nowy stan M' = M + I * T
     * @param I - incidence matrix
     * @param M - current marking
     * @param T - transition vector
     * @return new marking
     */
    static RealVector calculateNextState(RealMatrix I, RealVector M, RealVector T) {
        return M.add(I.operate(T));
    }

}
