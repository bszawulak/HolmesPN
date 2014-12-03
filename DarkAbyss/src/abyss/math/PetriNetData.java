package abyss.math;

import java.util.ArrayList;

public class PetriNetData {

	public ArrayList<Node> nodes;

	public ArrayList<Arc> arcs;

	public PetriNetData(ArrayList<Node> nodes, ArrayList<Arc> arcs) {
		this.nodes = nodes;
		this.arcs = arcs;
	}
}