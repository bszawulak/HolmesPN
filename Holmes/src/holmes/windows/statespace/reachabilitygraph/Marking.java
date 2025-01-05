package holmes.windows.statespace.reachabilitygraph;

import java.util.HashMap;
import java.util.Map;

public class Marking {
    Map<String, Integer> places;

    Marking(Map<String, Integer> places) {
        this.places = new HashMap<>(places);
    }

    boolean equals(Marking other) {
        return this.places.equals(other.places);
    }

    boolean greaterThan(Marking other) {
        for (String place : this.places.keySet()) {
            if (this.places.get(place) < other.places.get(place)) {
                return false;
            }
        }
        return true;
    }

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

}
