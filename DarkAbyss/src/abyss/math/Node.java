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
 * Klasa implementująca wierzchołek sieci Petriego. Dziedziczą po niej klasy
 * reprezentujące miejsca (Place) oraz tranzycje (Transition). Zapewnia
 * implementację wspólnych dla nich funkcjonalności, jak rysowanie ich na
 * odpowiednim arkuszu czy umożliwienie tworzenia portali (wielu odnośników
 * graficznych do jednego wierzchołka sieci).
 * @author students
 *
 */
public abstract class Node extends PetriNetElement {
	// BACKUP: -8569201372990876149L;  (NIE DOTYKAĆ PONIŻSZEJ ZMIENNEJ!)
	private static final long serialVersionUID = -8569201372990876149L;

	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAĆ NAZW, DODAWAĆ LUB USUWAĆ PÓL TEJ KLASY
	 * (przestanie być możliwe wczytywanie zapisanych projektów w formacie .abyss)
	 */
	
	@ElementList
	private ArrayList<ElementLocation> elementLocations = new ArrayList<ElementLocation>();
	@Element
	protected boolean isPortal = false;
	private int radius = 20;
	final static float dash1[] = { 2.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
	private int nameOffsetX = 0;
	private int nameOffsetY = 0;

	/**
	 * Konstruktor obiektu klasy Node.
	 * @param sheetId int - identyfikator arkusza
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param nodePosition Point - punkt, w którym znajduje się lokalizacja 
	 * 		tego wierzchołka na odpowiednim arkuszu
	 * @param radius int - promień okręgu, na którym opisana jest figura 
	 * 		geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public Node(int sheetId, int nodeId, Point nodePosition, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		this.getNodeLocations().add(
				new ElementLocation(sheetId, nodePosition, this));
	}

	/**
	 * Konstruktor obiektu klasy Node. Jest wywoływany między innymi w czasie tworzenia portalu,
	 * tj. wtedy, kiedy jeden węzeł sieci ma 2 lub więcej lokalizacji
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji elementu sieci Petriego
	 * @param radius int - promień okręgu, na którym opisana jest figura 
	 * 				geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ArrayList<ElementLocation> elementLocations, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		/*
		 * Poniżej, dla wszystkich lokalizacji w elemenLocations a co za tym idzie
		 * dla wszystkich przesłanych wewnątrz tej tablicy węzłów, są one podmieniane
		 * na aktualnie tworzony konstruktorem - czyli np. jeśli przyszły tu dane 3
		 * starych tranzycji o id 1, 2, 3, są one podmieniane na właśnie konstruowany
		 * obiekt, o innym ID i innych danych - jest to ten sam portal.
		 */
		for (ElementLocation el : elementLocations)
			el.setParentNode(this);
		/*
		 * Tablica elementLocations zawiera kolekcje łuków wejściowych i wyjściowych, 
		 * tak więc powyższe podmienienie ParentNode nie wpływa na nie - zostają takie,
		 * jakie były dla starych kilku węzłów zmienianych w portal
		 */
		this.setNodeLocations(elementLocations);
		if (elementLocations.size() > 1) { // oczywiście konstruktor może też tworzyć zwykły węzeł
			setPortal(true); //skoro po coś ta metoda w ogóle powstała...
		}
	}

	/**
	 * Konstruktor obiektu klasy Node.
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocation ElementLocation - lokalizacja elementu sieci Petriego
	 * @param radius int - promień okręgu, na którym opisana jest figura 
	 * 		geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ElementLocation elementLocation, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		elementLocation.setParentNode(this);
		this.getElementLocations().add(elementLocation);
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich punktów lokalizacji 
	 * wierzchołka na arkuszu o określonym identyfikatorze.
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[Point] - lista punktów lokalizacji wierzchołka na wybranym arkuszu
	 */
	public ArrayList<Point> getNodePositions(int sheetId) {
		ArrayList<Point> returnPoints = new ArrayList<Point>();
		for (ElementLocation e : this.getNodeLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e.getPosition());
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich lokacji wierzchołka na arkuszu 
	 * o określonym identyfikatorze
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[ElementLocation] - lista lokalizacji wierzchołka na wybranym arkuszu
	 */
	public ArrayList<ElementLocation> getNodeLocations(int sheetId) {
		ArrayList<ElementLocation> returnPoints = new ArrayList<ElementLocation>();
		for (ElementLocation e : this.getElementLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e);
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobrać ostatnią pośród dodanych dla tego wierzchołka lokację.
	 * @return ElementLocation - ostatnia dodana lokalizacja
	 */
	public ElementLocation getLastLocation() {
		if (this.getNodeLocations().size() == 0)
			return null;
		return this.getNodeLocations().get(this.getNodeLocations().size() - 1);
	}

	/**
	 * Metoda pozwala narysować NAZWĘ wierzchołka sieci Petriego na odpowiednim arkuszu.
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
	
	public void drawName(Graphics2D g, int sheetId) {
		g.setColor(Color.black);
		g.setFont(new Font("Tahoma", Font.PLAIN, 11));
		int width = g.getFontMetrics().stringWidth(getName());
		for (Point p : this.getNodePositions(sheetId))
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
	}

	/**
	 * Metoda pozwala sprawdzić, czy dany punkt na danym arkuszu zawiera się
	 * w obszarze rysowania tego wierzchołka sieci Petriego.
	 * @param point Point - sprawdzany punkt
	 * @param sheetId int - identyfikator arkusza
	 * @return boolean - true, jeśli lokalizacja faktycznie zawiera się w obszarze 
	 * 	rysowania tego wierzchołka sieci Petriego; false w przeciwnym wypadku
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
	 * Metoda pozwala pobrać lokację wierzchołka sieci Petriego, której 
	 * obszar rysowania zawiera dany punkt na danym arkuszu.
	 * @param point Point - sprawdzany punkt
	 * @param sheetId int - identyfikator arkusza
	 * @return ElementLocation - lokalizacja wierzchołka sieci Petriego, 
	 * 		której obszar rysowania zawiera wybrany punkt na wybranym arkuszu
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
	 * Metoda pozwala pobrać listę lokacji, które zawierają się w danym
	 * prostokątnym obszarze na danym arkuszu.
	 * @param rectangle Rectangle - prostokątny obszar
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[ElementLocation] - lista lokalizacji, które
	 * 			zawierają się w wybranym prostokątnym obszarze na wybranym arkuszu
	 */
	public ArrayList<ElementLocation> getLocationsWhichAreContained(Rectangle rectangle, int sheetId) {
		ArrayList<ElementLocation> returnElementLocations = new ArrayList<ElementLocation>();
		for (ElementLocation el : this.getNodeLocations(sheetId))
			if (rectangle.contains(el.getPosition()))
				returnElementLocations.add(el);
		return returnElementLocations;
	}

	/**
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzchołka
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
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzchołka
	 * sieci Petriego jako nie wybrane.
	 */
	public void deselectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			el.setPortalSelected(false);
	}

	/**
	 * Metoda pozwala sprawdzić, czy wszystkie lokacje tego wierzchołka 
	 * sieci Petriego są oznaczone jako wybrane.
	 * @return boolean - true jeśli wierzchołek jest portalem; false w przeciwnym wypadku
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
	 * Metoda sprawdza, czy wierzchołek sieci Petriego jest portalem 
	 * (czy ma więcej niż jedną lokację).
	 * @return boolean - true, jeśli wierzchołek jest portalem; false w przeciwnym wypadku
	 */
	public boolean isPortal() {
		return isPortal;
	}

	/**
	 * Metoda pozwala oznaczyć, czy wierzchołek jest portalem (czy ma 
	 * więcej niż jedną lokację).
	 * @param isPortal boolean - wartość określająca, czy wierzchołek ma być portalem
	 */
	public void setPortal(boolean isPortal) {
		this.isPortal = isPortal;
	}

	/**
	 * Metoda pozwala pobrać promień okręgu, na którym opisana jest figura
	 * geometryczna reprezentująca obiekt w edytorze graficznym. 
	 * @return int - promień okręgu, na którym opisana jest figura 
	 * 		geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Metoda pozwala ustawić promień okręgu, na którym opisana jest figura
	 * geometryczna reprezentująca obiekt w edytorze graficznym. 
	 * @param radius int - promien okręgu 
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich lokacji wierzchołka.
	 * @return ArrayList[ElementLocation] - lista wszystkich lokalizacji wierzchołka
	 */
	public ArrayList<ElementLocation> getNodeLocations() {
		return getElementLocations();
	}

	/**
	 * Metoda pozwala ustawić listę wszystkich lokalizacji wierzchołka.
	 * @param ArrayList[ElementLocation] nodeLocations - lista nowucj lokalizacji wierzchołka
	 */
	public void setNodeLocations(ArrayList<ElementLocation> nodeLocations) {
		this.setElementLocations(nodeLocations);
	}

	/**
	 * Metoda pozwala usunąć lokalizację z listy wszystkich lokalizacji wierzchołka.
	 * @param el ElementLocation - lokalizacja do usunięcia 
	 * @return boolean - true, jeśli usunięto jedyną (czyli ostatnią) lokalizację wierzchołka, 
	 * 		false jeżeli wierzchołek ma jeszcze inne lokalizacje
	 */
	public boolean removeElementLocation(ElementLocation el) {
		this.getNodeLocations().remove(el);
		
		if (this.getNodeLocations().size() > 0)
			return true;
		else
			return false;
	}

	/**
	 * Metoda pozwala pobrać listę łuków wejściowych.
	 * @return ArrayList[Arc] - lista łuków wejściowych
	 */
	public ArrayList<Arc> getInArcs() {
		ArrayList<Arc> totalInArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalInArcs.addAll(location.getInArcs());
		}
		return totalInArcs;
	}

	/**
	 * Metoda pozwala pobrać listę łuków wyjściowych.
	 * @return ArrayList[Arc] - lista łuków wyjściowych
	 */
	public ArrayList<Arc> getOutArcs() {
		ArrayList<Arc> totalOutArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalOutArcs.addAll(location.getOutArcs());
		}
		return totalOutArcs;
	}

	/**
	 * Metoda pozwala pobrać listę wierzchołków wejściowych.
	 * @return ArrayList[Node] - lista wierzchołków wejściowych
	 */
	public ArrayList<Node> getInNodes() {
		ArrayList<Node> totalInNodes = new ArrayList<Node>();
		for (Arc arc : getInArcs()) {
			totalInNodes.add(arc.getStartNode());
		}
		return totalInNodes;
	}

	/**
	 * Metoda pozwala pobrać listę wierzchołków wyjściowych.
	 * @return ArrayList[Node] - lista wierzchołków wyjściowych
	 */
	public ArrayList<Node> getOutNodes() {
		ArrayList<Node> totalOutNodes = new ArrayList<Node>();
		for (Arc arc : getOutArcs()) {
			totalOutNodes.add(arc.getEndNode());
		}
		return totalOutNodes;
	}

	/**
	 * Zwraca wartość identyfikatora węzła.
	 * @return String - łańcuch znaków ID
	 */
	public String toString() {
		String s = "ID: " + Integer.toString(this.getID());
		return s;
	}

	/**
	 * Metoda zwraca lokalizację obiektu - portalu.
	 * @return ArrayList[ElementLocation] - tablica wspołrzędnych portalu
	 */
	public ArrayList<ElementLocation> getElementLocations() {
		return elementLocations;
	}

	/**
	 * Metoda ustawia nowe lokalizacje obiektu - portalu
	 * @param elementLocations ArrayList[ElementLocation] - tablica lokalizacji węzła - portalu
	 */
	public void setElementLocations(ArrayList<ElementLocation> elementLocations) {
		this.elementLocations = elementLocations;
	}
	
	public int getNameOffX() {
		return nameOffsetX;
	}
	
	public void setNameOffX(int value) {
		nameOffsetX = value;
	}
	
	public int getNameOffY() {
		return nameOffsetY;
	}
	
	public void setNameOffY(int value) {
		nameOffsetY = value;
	}
}
