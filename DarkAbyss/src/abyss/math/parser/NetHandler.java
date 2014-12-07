package abyss.math.parser;

import java.util.ArrayList;

import org.xml.sax.helpers.DefaultHandler;

import abyss.math.Arc;
import abyss.math.Node;

/**
 * Klasa dziedzicz�ca po org.xml.sax.helpers.DefaultHandler.
 * Dziedzicz� po niej parsery format�w zapisu programu Snoopy. 
 * @author students
 *
 */
public class NetHandler extends DefaultHandler {
	protected ArrayList<Arc> arcList = new ArrayList<Arc>();
	protected ArrayList<Node> nodesList = new ArrayList<Node>();

	/**
	 * Metoda pozwala pobra� list� �uk�w.
	 * @return arcList - lista z �ukami sieci
	 */
	public ArrayList<Arc> getArcList() {
		return arcList;
	}

	/**
	 * Metoda pozwala pobra� wierzcho�k�w.
	 * @return nodesList - zwraca list� z wierzcho�kami sieci
	 */
	public ArrayList<Node> getNodesList() {
		return nodesList;
	}

}
