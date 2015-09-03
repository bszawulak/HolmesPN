package holmes.petrinet.functions;

import java.util.ArrayList;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;

/**
 * Klasa kontener - przechowuje danej o funkcji tranzycji.
 * 
 * @author MR
 */
public class FunctionContainer {
	public String function = "";
	public String fID = "";
	public Arc arc = null;
	public boolean enabled = false;
	public boolean correct = false;
	public boolean inTransArc = false;
	public ArrayList<Place> involvedPlaces = new ArrayList<Place>();
	
	public String toString() {
		return "fID: "+fID+" | Function: "+function+ " | Correct: "+correct+" | Enabled: "+enabled;
	}
}
