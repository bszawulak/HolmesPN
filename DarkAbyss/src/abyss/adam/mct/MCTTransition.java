package abyss.adam.mct;

import java.util.*;

public class MCTTransition extends MCTNode {
	protected SortedSet<MCTPlace> in = new TreeSet<MCTPlace>();
	protected SortedSet<MCTPlace> out = new TreeSet<MCTPlace>();
	protected MCTSet mct;
	
	public MCTTransition(String id, MCTPetriNet petriNet)
	{
		super(id, petriNet);
	}

	public SortedSet<MCTPlace> getInputPlaces()
	{
		return in;
	}
	
	public SortedSet<MCTPlace> getOutputPlaces()
	{
		return out;
	}
	
	@Override
	public String getNodeShort(boolean latexMode) {
		return (latexMode ? "$t_{" : "t") + super.getNodeShort(latexMode) + (latexMode ? "}$" : "");
	}
	
	/**
	 * @return - zbiór MCT do którego nale¿y tranzycja
	 */
	public MCTSet getMCTSet() {
		return mct;
	}
	
	/**
	 * Ustawienie zbioru MCT do którego tranzycja nale¿y
	 * @param mct - zbiór MCT
	 */
	public void setMCTSet(MCTSet mct) {
		this.mct = mct;
	}

}
