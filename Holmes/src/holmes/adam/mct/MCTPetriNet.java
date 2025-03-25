package holmes.adam.mct;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class MCTPetriNet {
	private SortedMap<String, MCTPlace> places = new TreeMap<String, MCTPlace>();
	private SortedMap<String, MCTTransition> transitions = new TreeMap<String, MCTTransition>();
	
	public SortedSet<MCTPlace> getPlaces()
	{
		return new TreeSet<MCTPlace>(places.values());
	}
	
	public SortedSet<MCTTransition> getTransitions()
	{
		return new TreeSet<MCTTransition>(transitions.values());
	}
	
	public MCTTransition getTransition(String id)
	{
		return transitions.get(id);
	}
	
	public MCTTransition getOrCreateTransition(String id)
	{
		MCTTransition t = transitions.get(id);
		if (t != null)
			return t;
		t = new MCTTransition(id, this);
		transitions.put(id, t);
		return t;
	}
}
