package holmes.petrinet.functions;

import java.util.LinkedHashMap;
import java.util.Map;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import net.objecthunter.exp4j.Expression;

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
	public Map<String, Place> involvedPlaces = new LinkedHashMap<String, Place>();
	public Expression equation = null;
	public double currentValue = -1;
	
	private Transition parent = null;
	public FunctionContainer(Transition trans) {
		parent = trans;
	}
	
	public String toString() {
		return "fID: "+fID+" | Function: "+function+ " | Correct: "+correct+" | Enabled: "+enabled+" | Parent: "+parent.toString();
	}
}
