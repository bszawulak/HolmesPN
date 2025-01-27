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
    Map<String, Integer> places;
    Map<String, Boolean> nTokens;

    private String name;

    Marking(Map<String, Integer> places) {
        this.places = places;
        this.nTokens = places.keySet().stream().collect(Collectors.toMap(k -> k, k -> false));
    }

    Marking(Map<String, Integer> places, Map<String, Boolean> nTokens) {
        this.places = places;
        this.nTokens = nTokens;
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
     * Checks if one place has more tokens than others, and other has equal
     * @param other
     * @return
     */
    String greaterOneThan(Marking other) {
        String oneGreater = null;

        for (String place : this.places.keySet()) {
            if (this.places.get(place) > other.places.get(place)) {
                if (oneGreater == null) {
                    oneGreater = place;
                } else {
                    return null;
                }
            }
        }
        return oneGreater;
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
        return nTokens.entrySet().stream()
                .map(p -> p.getValue() ? places.get(p.getKey()) + "n"
                        : places.get(p.getKey())
                )
                .map(Object::toString)
                .map(p -> "0n".equals(p) ? "0" : p)
                .map(p -> "1n".equals(p) ? "n" : p)
                .toList().toString();
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

    public static Marking fromVector(RealVector vector, PetriNet net, Marking oldMarking) {
        Map<String, Integer> placeTokens = net.getPlaces().stream()
                .collect(Collectors.toMap(
                        Place::getName,
                        place -> (int) vector.getEntry(net.getPlaces().indexOf(place))
                ));
        return new Marking(placeTokens, new HashMap<>(oldMarking.nTokens));
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
