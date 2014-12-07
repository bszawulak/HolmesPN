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

/**
 * Klasa implementuj�ca wierzcho�ek sieci Petriego. Dziedzicz� po niej klasy
 * reprezentuj�ce miejsca (Place) oraz tranzycje (Transition). Zapewnia
 * implementacj� wsp�lnych dla nich funkcjonalno�ci, jak rysowanie ich na
 * odpowiednim arkuszu czy umo�liwienie tworzenia portali (wielu odno�nik�w
 * graficznych do jednego wierzcho�ka sieci).
 * @author students
 *
 */
public abstract class Node extends PetriNetElement {
	private static final long serialVersionUID = -8569201372990876149L;

	/*
	 * UWAGA!!! NIE WOLNO ZMIENIA� NAZW, DODAWA� LUB USUWA� P�L TEJ KLASY
	 * (przestanie by� mo�liwe wczytywanie zapisanych proejkt�w .abyss)
	 */
	
	@ElementList
	private ArrayList<ElementLocation> elementLocations = new ArrayList<ElementLocation>();
	@Element
	protected boolean isPortal = false;
	private int radius = 20;
	final static float dash1[] = { 2.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

	/**
	 * Konstruktor obiektu klasy Node.
	 * @param sheetId int - identyfikator arkusza
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param nodePosition Point - punkt, w kt�rym znajduje si� lokalizacja 
	 * 				tego wierzcho�ka na odpowiednim arkuszu
	 * @param radius int - promie� okr�gu, na kt�rym opisana jest figura 
	 * 				geometryczna reprezentuj�ca obiekt w edytorze graficznym
	 */
	public Node(int sheetId, int nodeId, Point nodePosition, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		this.getNodeLocations().add(
				new ElementLocation(sheetId, nodePosition, this));
	}

	/**
	 * Konstruktor obiektu klasy Node.
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocations ArrayList[ElementLocation] - lista lokacji elementu sieci Petriego
	 * @param radius int - promie� okr�gu, na kt�rym opisana jest figura 
	 * 				geometryczna reprezentuj�ca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ArrayList<ElementLocation> elementLocations, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		for (ElementLocation el : elementLocations)
			el.setParentNode(this);
		this.setNodeLocations(elementLocations);
		if (elementLocations.size() > 1) {
			isPortal = true;
		}
	}

	/**
	 * Konstruktor obiektu klasy Node.
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocation ElementLocation - lokalizacja elementu sieci Petriego
	 * @param radius int - promie� okr�gu, na kt�rym opisana jest figura 
	 * 		geometryczna reprezentuj�ca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ElementLocation elementLocation, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		elementLocation.setParentNode(this);
		this.getElementLocations().add(elementLocation);
	}

	/**
	 * Metoda pozwala pobra� list� wszystkich punkt�w lokalizacji 
	 * wierzcho�ka na arkuszu o okre�lonym identyfikatorze.
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[Point] - lista punkt�w lokalizacji wierzcho�ka na wybranym arkuszu
	 */
	public ArrayList<Point> getNodePositions(int sheetId) {
		ArrayList<Point> returnPoints = new ArrayList<Point>();
		for (ElementLocation e : this.getNodeLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e.getPosition());
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobra� list� wszystkich lokacji wierzcho�ka na arkuszu 
	 * o okre�lonym identyfikatorze
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[ElementLocation] - lista lokalizacji wierzcho�ka na wybranym arkuszu
	 */
	public ArrayList<ElementLocation> getNodeLocations(int sheetId) {
		ArrayList<ElementLocation> returnPoints = new ArrayList<ElementLocation>();
		for (ElementLocation e : this.getElementLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e);
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobra� ostatni� po�r�d dodanych dla tego wierzcho�ka lokacj�.
	 * @return ElementLocation - ostatnia dodana lokalizacja
	 */
	public ElementLocation getLastLocation() {
		if (this.getNodeLocations().size() == 0)
			return null;
		return this.getNodeLocations().get(this.getNodeLocations().size() - 1);
	}

	/**
	 * Metoda pozwala narysowa� wierzcho�ek sieci Petriego na odpowiednim arkuszu.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza
	 */
	public void draw(Graphics2D g, int sheetId) {
		g.setColor(Color.black);
		g.setFont(new Font("Tahoma", Font.PLAIN, 11));
		int width = g.getFontMetrics().stringWidth(getName());
		for (Point p : this.getNodePositions(sheetId))
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
	}

	/**
	 * Metoda pozwala sprawdzi�, czy dany punkt na danym arkuszu zawiera si�
	 * w obszarze rysowania tego wierzcho�ka sieci Petriego.
	 * @param point Point - sprawdzany punkt
	 * @param sheetId int - identyfikator arkusza
	 * @return boolean - true, je�li lokalizacja faktycznie zawiera si� w obszarze 
	 * 	rysowania tego wierzcho�ka sieci Petriego; false w przeciwnym wypadku
	 */
	public boolean contains(Point point, int sheetId) {
		for (Point p : this.getNodePositions(sheetId))
			if (p.x - getRadius() < point.x && p.y - getRadius() < point.y
					&& p.x + getRadius() > point.x
					&& p.y + getRadius() > point.y)
				return true;
		return false;
	}

	/**
	 * Metoda pozwala pobra� lokacj� wierzcho�ka sieci Petriego, kt�rej 
	 * obszar rysowania zawiera dany punkt na danym arkuszu.
	 * @param point Point - sprawdzany punkt
	 * @param sheetId int - identyfikator arkusza
	 * @return ElementLocation - lokalizacja wierzcho�ka sieci Petriego, 
	 * 		kt�rej obszar rysowania zawiera wybrany punkt na wybranym arkuszu
	 */
	public ElementLocation getLocationWhichContains(Point point, int sheetId) {
		for (ElementLocation e : this.getNodeLocations(sheetId))
			if (e.getPosition().x - getRadius() < point.x
					&& e.getPosition().y - getRadius() < point.y
					&& e.getPosition().x + getRadius() > point.x
					&& e.getPosition().y + getRadius() > point.y)
				return e;
		return null;
	}

	/**
	 * Metoda pozwala pobra� list� lokacji, kt�re zawieraj� si� w danym
	 * prostok�tnym obszarze na danym arkuszu.
	 * @param rectangle Rectangle - prostok�tny obszar
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[ElementLocation] - lista lokalizacji, kt�re
	 * 			zawieraj� si� w wybranym prostok�tnym obszarze na wybranym arkuszu
	 */
	public ArrayList<ElementLocation> getLocationsWhichAreContained(Rectangle rectangle, int sheetId) {
		ArrayList<ElementLocation> returnElementLocations = new ArrayList<ElementLocation>();
		for (ElementLocation el : this.getNodeLocations(sheetId))
			if (rectangle.contains(el.getPosition()))
				returnElementLocations.add(el);
		return returnElementLocations;
	}

	/**
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzcho�ka
	 * sieci Petriego jako wybrane.
	 */
	public void selectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			if (!el.isSelected())
				el.setPortalSelected(true);
	}

	/**
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzcho�ka
	 * sieci Petriego jako nie wybrane.
	 */
	public void deselectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			el.setPortalSelected(false);
	}

	/**
	 * Metoda pozwala sprawdzi�, czy wszystkie lokacje tego wierzcho�ka 
	 * sieci Petriego s� oznaczone jako wybrane.
	 * @return boolean - true je�li wierzcho�ek jest portalem; false w przeciwnym wypadku
	 */
	public boolean checkAllPortalsSelection() {
		if (!isPortal())
			return false;
		for (ElementLocation el : this.getNodeLocations())
			if (el.isSelected())
				return true;
		return false;
	}

	/**
	 * Metoda sprawdza, czy wierzcho�ek sieci Petriego jest portalem 
	 * (czy ma wi�cej ni� jedn� lokacj�).
	 * @return boolean - true, je�li wierzcho�ek jest portalem; false w przeciwnym wypadku
	 */
	public boolean isPortal() {
		return isPortal;
	}

	/**
	 * Metoda pozwala oznaczy�, czy wierzcho�ek jest portalem (czy ma 
	 * wi�cej ni� jedn� lokacj�).
	 * @param isPortal boolean - warto�� okre�laj�ca, czy wierzcho�ek ma by� portalem
	 */
	public void setPortal(boolean isPortal) {
		this.isPortal = isPortal;
	}

	/**
	 * Metoda pozwala pobra� promie� okr�gu, na kt�rym opisana jest figura
	 * geometryczna reprezentuj�ca obiekt w edytorze graficznym. 
	 * @return int - promie� okr�gu, na kt�rym opisana jest figura 
	 * 		geometryczna reprezentuj�ca obiekt w edytorze graficznym
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Metoda pozwala ustawi� promie� okr�gu, na kt�rym opisana jest figura
	 * geometryczna reprezentuj�ca obiekt w edytorze graficznym. 
	 * @param radius int - promie� okr�gu 
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	/**
	 * Metoda pozwala pobra� list� wszystkich lokacji wierzcho�ka.
	 * @return ArrayList[ElementLocation] - lista wszystkich lokalizacji wierzcho�ka
	 */
	public ArrayList<ElementLocation> getNodeLocations() {
		return getElementLocations();
	}

	/**
	 * Metoda pozwala ustawi� list� wszystkich lokacji wierzcho�ka.
	 * @param ArrayList[ElementLocation] nodeLocations - lista lokalizacji wierzcho�ka
	 */
	public void setNodeLocations(ArrayList<ElementLocation> nodeLocations) {
		this.setElementLocations(nodeLocations);
	}

	/**
	 * Metoda pozwala usun�� lokacj� z listy wszystkich lokacji wierzcho�ka.
	 * @param el ElementLocation - lokalizacja do usuni�cia 
	 * @return boolean - true, je�li lokalizacja zosta�a pomy�lnie usuni�ta; false w przeciwnym wypadku
	 */
	public boolean removeElementLocation(ElementLocation el) {
		this.getNodeLocations().remove(el);
		if (this.getNodeLocations().size() > 0)
			return true;
		else
			return false;
	}

	/**
	 * Metoda pozwala pobra� list� �uk�w wej�ciowych.
	 * @return ArrayList[Arc] - lista �uk�w wej�ciowych
	 */
	public ArrayList<Arc> getInArcs() {
		ArrayList<Arc> totalInArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalInArcs.addAll(location.getInArcs());
		}
		return totalInArcs;
	}

	/**
	 * Metoda pozwala pobra� list� �uk�w wyj�ciowych.
	 * @return ArrayList[Arc] - lista �uk�w wyj�ciowych
	 */
	public ArrayList<Arc> getOutArcs() {
		ArrayList<Arc> totalOutArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalOutArcs.addAll(location.getOutArcs());
		}
		return totalOutArcs;
	}

	/**
	 * Metoda pozwala pobra� list� wierzcho�k�w wej�ciowych.
	 * @return ArrayList[Node] - lista wierzcho�k�w wej�ciowych
	 */
	public ArrayList<Node> getInNodes() {
		ArrayList<Node> totalInNodes = new ArrayList<Node>();
		for (Arc arc : getInArcs()) {
			totalInNodes.add(arc.getStartNode());
		}
		return totalInNodes;
	}

	/**
	 * Metoda pozwala pobra� list� wierzcho�k�w wyj�ciowych.
	 * @return ArrayList[Node] - lista wierzcho�k�w wyj�ciowych
	 */
	public ArrayList<Node> getOutNodes() {
		ArrayList<Node> totalOutNodes = new ArrayList<Node>();
		for (Arc arc : getOutArcs()) {
			totalOutNodes.add(arc.getEndNode());
		}
		return totalOutNodes;
	}

	/**
	 * Zwraca warto�c identyfikatora w�z�a.
	 * @return String - �a�cuch znak�w ID
	 */
	public String toString() {
		String s = "ID: " + Integer.toString(this.getID());
		return s;
	}

	/**
	 * Metoda zwraca lokalizacj� obiektu.
	 * @return ArrayList[ElementLocation] - wspo�rz�dne
	 */
	public ArrayList<ElementLocation> getElementLocations() {
		return elementLocations;
	}

	/**
	 * Metoda ustawia lokalizacj� obiektu 
	 * @param elementLocations ArrayList[ElementLocation] - obiekt lokalizacji w�z�a
	 */
	public void setElementLocations(ArrayList<ElementLocation> elementLocations) {
		this.elementLocations = elementLocations;
	}
}
