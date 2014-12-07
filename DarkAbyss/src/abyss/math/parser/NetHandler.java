package abyss.math.parser;

import java.util.ArrayList;

import org.xml.sax.helpers.DefaultHandler;

import abyss.math.Arc;
import abyss.math.Node;

/**
 * Klasa dziedzicz¹ca po org.xml.sax.helpers.DefaultHandler.
 * Dziedzicz¹ po niej parsery formatów zapisu programu Snoopy. 
 * @author students
 *
 */
public class NetHandler extends DefaultHandler {
	protected ArrayList<Arc> arcList = new ArrayList<Arc>();
	protected ArrayList<Node> nodesList = new ArrayList<Node>();

	/**
	 * Metoda pozwala pobraæ listê ³uków.
	 * @return arcList - lista z ³ukami sieci
	 */
	public ArrayList<Arc> getArcList() {
		return arcList;
	}

	/**
	 * Metoda pozwala pobraæ wierzcho³ków.
	 * @return nodesList - zwraca listê z wierzcho³kami sieci
	 */
	public ArrayList<Node> getNodesList() {
		return nodesList;
	}

}
