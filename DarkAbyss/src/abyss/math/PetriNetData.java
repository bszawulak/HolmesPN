package abyss.math;

import java.util.ArrayList;

/**
 * Klasa kontener dla danych definiuj�cych sie� Petriego - wierzcho�k�w i �uk�w.
 * @author students
 *
 */
public class PetriNetData {
	public ArrayList<Node> nodes;
	public ArrayList<Arc> arcs;
	public String netName;

	/**
	 * Konstruktor obiektu klasy PetriNetData
	 * @param nodes ArrayList[Node] - lista wierzcho�k�w sieci
	 * @param arcs ArrayList[Arc] - lista �uk�w sieci
	 */
	public PetriNetData(ArrayList<Node> nodes, ArrayList<Arc> arcs, String name) {
		this.nodes = nodes;
		this.arcs = arcs;
		this.netName = name;
	}
}
