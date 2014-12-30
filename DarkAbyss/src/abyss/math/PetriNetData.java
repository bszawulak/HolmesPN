package abyss.math;

import java.util.ArrayList;

/**
 * Klasa kontener dla danych definiuj¹cych sieæ Petriego - wierzcho³ków i ³uków.
 * @author students
 *
 */
public class PetriNetData {
	public ArrayList<Node> nodes;
	public ArrayList<Arc> arcs;
	public String netName;

	/**
	 * Konstruktor obiektu klasy PetriNetData
	 * @param nodes ArrayList[Node] - lista wierzcho³ków sieci
	 * @param arcs ArrayList[Arc] - lista ³uków sieci
	 */
	public PetriNetData(ArrayList<Node> nodes, ArrayList<Arc> arcs, String name) {
		this.nodes = nodes;
		this.arcs = arcs;
		this.netName = name;
	}
}
