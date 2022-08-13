package holmes.petrinet.functions;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa kontener - przechowuje danej o funkcji tranzycji.
 */
public class FunctionContainer implements Serializable {
    //public String function = "";
    public String fID = "";
    public Arc arc = null;
    public boolean enabled = false;
    public boolean correct = false;
    public boolean inTransArc = false;
    public Map<String, Place> involvedPlaces;
    //public Expression equation = null;
    public String simpleExpression = "";
    public double currentValue = -1;

    public Transition parent = null;

    public FunctionContainer(Transition trans) {
        parent = trans;
        involvedPlaces = new LinkedHashMap<String, Place>();
    }

    public String toString() {
        return "fID: " + fID + " | Function: " + simpleExpression + " | Correct: " + correct + " | Enabled: " + enabled + " | Parent: " + parent.toString();
    }
}
