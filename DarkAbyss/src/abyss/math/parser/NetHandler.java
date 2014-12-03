package abyss.math.parser;

import java.util.ArrayList;

import org.xml.sax.helpers.DefaultHandler;

import abyss.math.Arc;
import abyss.math.Node;

public class NetHandler extends DefaultHandler {

	protected ArrayList<Arc> arcList = new ArrayList<Arc>();
	protected ArrayList<Node> nodesList = new ArrayList<Node>();

	public ArrayList<Arc> getArcList() {
		return arcList;
	}

	public ArrayList<Node> getNodesList() {
		return nodesList;
	}

}
