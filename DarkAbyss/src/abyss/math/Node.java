package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;

public abstract class Node extends PetriNetElement {
	private static final long serialVersionUID = -8569201372990876149L;

	@ElementList
	private ArrayList<ElementLocation> elementLocations = new ArrayList<ElementLocation>();
	@Element
	protected boolean isPortal = false;
	private int radius = 20;
	final static float dash1[] = { 2.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

	/**
	 * Construct a new node.
	 */
	public Node(int sheetId, int nodeId, Point nodePosition, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		this.getNodeLocations().add(
				new ElementLocation(sheetId, nodePosition, this));
	}

	public Node(int nodeId, ArrayList<ElementLocation> elementLocations,
			int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		for (ElementLocation el : elementLocations)
			el.setParentNode(this);
		this.setNodeLocations(elementLocations);
		if (elementLocations.size() > 1) {
			isPortal = true;
		}
	}

	public Node(int nodeId, ElementLocation elementLocation, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		elementLocation.setParentNode(this);
		this.getElementLocations().add(elementLocation);
	}

	/**
	 * Calculate this node's rectangular boundary.
	 */

	public ArrayList<Point> getNodePositions(int sheetId) {
		ArrayList<Point> returnPoints = new ArrayList<Point>();
		for (ElementLocation e : this.getNodeLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e.getPosition());
		return returnPoints;
	}

	public ArrayList<ElementLocation> getNodeLocations(int sheetId) {
		ArrayList<ElementLocation> returnPoints = new ArrayList<ElementLocation>();
		for (ElementLocation e : this.getElementLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e);
		return returnPoints;
	}

	public ElementLocation getLastLocation() {
		if (this.getNodeLocations().size() == 0)
			return null;
		return this.getNodeLocations().get(this.getNodeLocations().size() - 1);
	}

	/**
	 * Draw this node.
	 */
	public void draw(Graphics2D g, int sheetId) {
		g.setColor(Color.black);
		g.setFont(new Font("Tahoma", Font.PLAIN, 11));
		int width = g.getFontMetrics().stringWidth(getName());
		for (Point p : this.getNodePositions(sheetId))
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
	}

	/**
	 * Return true if this node contains p.
	 */
	public boolean contains(Point point, int sheetId) {
		for (Point p : this.getNodePositions(sheetId))
			if (p.x - getRadius() < point.x && p.y - getRadius() < point.y
					&& p.x + getRadius() > point.x
					&& p.y + getRadius() > point.y)
				return true;
		return false;
	}

	public ElementLocation getLocationWhichContains(Point point, int sheetId) {
		for (ElementLocation e : this.getNodeLocations(sheetId))
			if (e.getPosition().x - getRadius() < point.x
					&& e.getPosition().y - getRadius() < point.y
					&& e.getPosition().x + getRadius() > point.x
					&& e.getPosition().y + getRadius() > point.y)
				return e;
		return null;
	}

	public ArrayList<ElementLocation> getLocationsWhichAreContained(
			Rectangle rectangle, int sheetId) {
		ArrayList<ElementLocation> returnElementLocations = new ArrayList<ElementLocation>();
		for (ElementLocation el : this.getNodeLocations(sheetId))
			if (rectangle.contains(el.getPosition()))
				returnElementLocations.add(el);
		return returnElementLocations;
	}

	public void selectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			if (!el.isSelected())
				el.setPortalSelected(true);
	}

	public void deselectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			el.setPortalSelected(false);
	}

	public boolean checkAllPortalsSelection() {
		if (!isPortal())
			return false;
		for (ElementLocation el : this.getNodeLocations())
			if (el.isSelected())
				return true;
		return false;
	}

	public boolean isPortal() {
		return isPortal;
	}

	public void setPortal(boolean isPortal) {
		this.isPortal = isPortal;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public ArrayList<ElementLocation> getNodeLocations() {
		return getElementLocations();
	}

	public void setNodeLocations(ArrayList<ElementLocation> nodeLocations) {
		this.setElementLocations(nodeLocations);
	}

	/**
	 * Removes given as parameter ElementLocation from current Node list.
	 * 
	 * @param el
	 *            - ElementLocation to remove
	 * @return true - Node has one or more nodeLocations; false - Node has no
	 *         nodeLocations, so should be removed
	 */
	public boolean removeElementLocation(ElementLocation el) {
		this.getNodeLocations().remove(el);
		if (this.getNodeLocations().size() > 0)
			return true;
		else
			return false;
	}

	public ArrayList<Arc> getInArcs() {
		ArrayList<Arc> totalInArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalInArcs.addAll(location.getInArcs());
		}
		return totalInArcs;
	}

	public ArrayList<Arc> getOutArcs() {
		ArrayList<Arc> totalOutArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalOutArcs.addAll(location.getOutArcs());
		}
		return totalOutArcs;
	}

	public ArrayList<Node> getInNodes() {
		ArrayList<Node> totalInNodes = new ArrayList<Node>();
		for (Arc arc : getInArcs()) {
			totalInNodes.add(arc.getStartNode());
		}
		return totalInNodes;
	}

	public ArrayList<Node> getOutNodes() {
		ArrayList<Node> totalOutNodes = new ArrayList<Node>();
		for (Arc arc : getOutArcs()) {
			totalOutNodes.add(arc.getEndNode());
		}
		return totalOutNodes;
	}

	public String toString() {
		String s = "ID: " + Integer.toString(this.getID());
		return s;
	}

	public ArrayList<ElementLocation> getElementLocations() {
		return elementLocations;
	}

	public void setElementLocations(ArrayList<ElementLocation> elementLocations) {
		this.elementLocations = elementLocations;
	}
}
