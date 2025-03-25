package holmes.files.io.snoopy;

import java.util.ArrayList;

import org.xml.sax.helpers.DefaultHandler;

import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;

/**
 * Klasa dziedzicząca po org.xml.sax.helpers.DefaultHandler.
 * Dziedziczą po niej parsery formatów zapisu programu Snoopy.
 */
public class NetHandler extends DefaultHandler {
	protected ArrayList<Arc> arcList = new ArrayList<Arc>();
	protected ArrayList<Node> nodesList = new ArrayList<Node>();

	/**
	 * Metoda pozwala pobrać listę łuków.
	 * @return arcList - lista z łukami sieci
	 */
	public ArrayList<Arc> getArcList() {
		return arcList;
	}

	/**
	 * Metoda pozwala pobrać wierzchołków.
	 * @return nodesList - zwraca listę z wierzchołkami sieci
	 */
	public ArrayList<Node> getNodesList() {
		return nodesList;
	}
}
