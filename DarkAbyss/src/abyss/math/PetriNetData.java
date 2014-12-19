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

	/**
	 * Konstruktor obiektu klasy PetriNetData
	 * @param nodes ArrayList[Node] - lista wierzcho�k�w sieci
	 * @param arcs ArrayList[Arc] - lista �uk�w sieci
	 */
	public PetriNetData(ArrayList<Node> nodes, ArrayList<Arc> arcs) {
		this.nodes = nodes;
		this.arcs = arcs;
	}
}
