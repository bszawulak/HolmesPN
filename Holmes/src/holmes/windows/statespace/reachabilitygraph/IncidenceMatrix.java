package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Arrays;

public class IncidenceMatrix {

    private double[][] matrix;
    private RealMatrix iMatrix;

    public IncidenceMatrix(PetriNet net) {

        ArrayList<Place> places = net.getPlaces();
        ArrayList<Transition> transitions = net.getTransitions();
        ArrayList<Arc> arcs = net.getArcs();

        int numPlaces = places.size(); // Liczba miejsc
        int numTransitions = transitions.size(); // Liczba tranzycji
        matrix = new double[numPlaces][numTransitions]; // Macierz incydencji

        for (Arc arc : arcs) {
            Node startNode = arc.getStartNode();
            Node endNode = arc.getEndNode();

            if (startNode instanceof Place && endNode instanceof Transition) {
                //luk z miejsca do tranzycji
                int placeIndex = places.indexOf(startNode);
                int transitionIndex = transitions.indexOf(endNode);

                if (placeIndex != -1 && transitionIndex != -1) {
                    matrix[placeIndex][transitionIndex] -= arc.getWeight();
                }
            } else if (startNode instanceof Transition && endNode instanceof Place) {
                int placeIndex = places.indexOf(endNode);
                int transitionIndex = transitions.indexOf(startNode);

                if (placeIndex != -1 && transitionIndex != -1) {
                    matrix[placeIndex][transitionIndex] += arc.getWeight();
                }
            }
        }

        iMatrix = MatrixUtils.createRealMatrix(matrix);

    }

    public void printToConsole() {
        System.out.println("Incidence Matrix:");
        Arrays.stream(matrix).forEach(row -> System.out.println(Arrays.toString(row)));
    }


    public RealMatrix get() {
        return iMatrix;
    }


}


