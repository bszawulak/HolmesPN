package holmes.windows.statespace.reachabilitygraph;

import java.util.HashMap;
import java.util.Map;

public class Transition {
    String name;
    Map<String, Integer> input;
    Map<String, Integer> output;

    Transition(String name, Map<String, Integer> input, Map<String, Integer> output) {
        this.name = name;
        this.input = input;
        this.output = output;
    }

    boolean isEnabled(Marking marking) {
        for (String place : input.keySet()) {
            if (marking.places.getOrDefault(place, 0) < input.get(place)) {
                return false;
            }
        }
        return true;
    }

    Marking fire(Marking marking) { //zjebana jest ta metoda to znaczy jesli tranzycja ma dwa miejsca docelowe to dostaje tylko jedno
        Map<String, Integer> newPlaces = new HashMap<>(marking.places);

        for (String place : input.keySet()) {
            newPlaces.put(place, newPlaces.getOrDefault(place, 0) - input.get(place));
        }
        for (String place : output.keySet()) {
            newPlaces.put(place, newPlaces.getOrDefault(place, 0) + output.get(place));
        }

        return new Marking(newPlaces);
    }

}
