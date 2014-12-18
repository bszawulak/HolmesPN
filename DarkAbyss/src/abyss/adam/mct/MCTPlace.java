package abyss.adam.mct;

import java.util.*;

public class MCTPlace extends MCTNode {
	
	protected SortedSet<MCTTransition> in = new TreeSet<MCTTransition>();
	protected SortedSet<MCTTransition> out = new TreeSet<MCTTransition>();
	
	public MCTPlace(String id, MCTPetriNet petriNet)
	{
		super(id, petriNet);
	}
	
	public SortedSet<MCTTransition> getInputTransitions()
	{
		return in;
	}
	
	public SortedSet<MCTTransition> getOutputTransitions()
	{
		return out;
	}
	
	@Override
	public String getNodeShort(boolean latexMode) {
		return (latexMode ? "$p_{" : "p") + super.getNodeShort(latexMode) + (latexMode ? "}$" : "");
	}
	
}
