package holmes.windows.statespace.reachabilitygraph;

import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Marking {
    Map<String, Integer> places; //TODO: jak zaznaczyc infinity? i krotnosci infinity?
    //Może Map<String, Integer>

    Marking(Map<String, Integer> places) {
        this.places = places;
    }

    boolean equals(Marking other) {
        return this.places.equals(other.places);
    }

    /**
     * Checks if every place has at least as many tokens as in the other marking
     * @param other
     * @return
     */
    boolean greaterThan(Marking other) {
        for (String place : this.places.keySet()) {
            if (this.places.get(place) < other.places.get(place)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if every place has maximum as many tokens as in the other marking
     * @param other
     * @return
     */
    boolean lessThan(Marking other) {
        for (String place : this.places.keySet()) {
            if (this.places.get(place) > other.places.get(place)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return places.toString();
    }

    public RealVector toVector() {
        double[] markingVector = new double[places.size()];
        int i = 0;
        for (String place : places.keySet()) {
            markingVector[i] = places.get(place);
            i++;
        }
        return new ArrayRealVector(markingVector);
    }

    public static Marking fromVector(RealVector vector, PetriNet net) {
        Map<String, Integer> placeTokens = net.getPlaces().stream()
                .collect(Collectors.toMap(
                        Place::getName,
                        place -> (int) vector.getEntry(net.getPlaces().indexOf(place))
                ));
        return new Marking(placeTokens);
    }

    public static Marking getActualMarking(PetriNet net) {
        ArrayList<Place> plcs = net.getPlaces();
        if (plcs.isEmpty()) {
            System.out.println("ERR: No places in the net");
            return new Marking(new HashMap<>());
        }
        // Name można by było zamienic na ID
        return new Marking(plcs.stream().collect(Collectors.toMap(Place::getName, Place::getTokensNumber)));
    }

}
