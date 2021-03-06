package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;

/**
 * Klasa kontener dla danych definiujących sieć Petriego - wierzchołków i łuków.
 * @author students
 *
 */
public class PetriNetData {
	public ArrayList<Node> nodes;
	public ArrayList<Arc> arcs;
	public String netName;

	/**
	 * Konstruktor obiektu klasy PetriNetData
	 * @param nodes ArrayList[Node] - lista wierzchołków sieci
	 * @param arcs ArrayList[Arc] - lista łuków sieci
	 */
	public PetriNetData(ArrayList<Node> nodes, ArrayList<Arc> arcs, String name) {
		this.nodes = nodes;
		this.arcs = arcs;
		this.netName = name;
	}
	
}
