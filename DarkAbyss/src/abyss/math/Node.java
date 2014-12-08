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
 * Klasa implementuj¹ca wierzcho³ek sieci Petriego. Dziedzicz¹ po niej klasy
 * reprezentuj¹ce miejsca (Place) oraz tranzycje (Transition). Zapewnia
 * implementacjê wspólnych dla nich funkcjonalnoœci, jak rysowanie ich na
 * odpowiednim arkuszu czy umo¿liwienie tworzenia portali (wielu odnoœników
 * graficznych do jednego wierzcho³ka sieci).
 * @author students
 *
 */
public abstract class Node extends PetriNetElement {
	private static final long serialVersionUID = -8569201372990876149L;

	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAÆ NAZW, DODAWAÆ LUB USUWAÆ PÓL TEJ KLASY
	 * (przestanie byæ mo¿liwe wczytywanie zapisanych projektów .abyss)
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
	 * @param nodePosition Point - punkt, w którym znajduje siê lokalizacja 
	 * 				tego wierzcho³ka na odpowiednim arkuszu
	 * @param radius int - promieñ okrêgu, na którym opisana jest figura 
	 * 				geometryczna reprezentuj¹ca obiekt w edytorze graficznym
	 */
	public Node(int sheetId, int nodeId, Point nodePosition, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		this.getNodeLocations().add(
				new ElementLocation(sheetId, nodePosition, this));
	}

	/**
	 * Konstruktor obiektu klasy Node. Jest wywo³ywany miêdzy innymi w czasie tworzenia portalu,
	 * tj. wtedy, kiedy jeden wêze³ sieci ma 2 lub wiêcej lokalizacji
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji elementu sieci Petriego
	 * @param radius int - promieñ okrêgu, na którym opisana jest figura 
	 * 				geometryczna reprezentuj¹ca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ArrayList<ElementLocation> elementLocations, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		/*
		 * Poni¿ej, dla wszystkich lokalizacji w elemenLocations a co za tym idzie
		 * dla wszystkich przes³anych wewn¹trz tej tablicy wêz³ów, s¹ one podmieniane
		 * na aktualnie tworzony konstruktorem - czyli np. jeœli przysz³y tu dane 3
		 * starych tranzycji o id 1, 2, 3, s¹ one podmieniane na w³aœnie konstruowany
		 * obiekt, o innym ID i innych danych - jest to ten sam portal.
		 */
		for (ElementLocation el : elementLocations)
			el.setParentNode(this);
		/*
		 * Tablica elementLocations zawiera kolekcje ³uków wejœciowych i wyjœciowych, 
		 * tak wiêc powy¿sze podmienienie ParentNode nie wp³ywa na nie - zostaj¹ takie,
		 * jakie by³y dla starych kilku wêz³ów zmienianych w portal
		 */
		this.setNodeLocations(elementLocations);
		if (elementLocations.size() > 1) { // oczywiœcie konstruktor mo¿e te¿ tworzyæ zwyk³y wêze³
			setPortal(true); //skoro po coœ ta metoda w ogóle powsta³a...
		}
	}

	/**
	 * Konstruktor obiektu klasy Node.
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocation ElementLocation - lokalizacja elementu sieci Petriego
	 * @param radius int - promieñ okrêgu, na którym opisana jest figura 
	 * 		geometryczna reprezentuj¹ca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ElementLocation elementLocation, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		elementLocation.setParentNode(this);
		this.getElementLocations().add(elementLocation);
	}

	/**
	 * Metoda pozwala pobraæ listê wszystkich punktów lokalizacji 
	 * wierzcho³ka na arkuszu o okreœlonym identyfikatorze.
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[Point] - lista punktów lokalizacji wierzcho³ka na wybranym arkuszu
	 */
	public ArrayList<Point> getNodePositions(int sheetId) {
		ArrayList<Point> returnPoints = new ArrayList<Point>();
		for (ElementLocation e : this.getNodeLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e.getPosition());
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobraæ listê wszystkich lokacji wierzcho³ka na arkuszu 
	 * o okreœlonym identyfikatorze
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[ElementLocation] - lista lokalizacji wierzcho³ka na wybranym arkuszu
	 */
	public ArrayList<ElementLocation> getNodeLocations(int sheetId) {
		ArrayList<ElementLocation> returnPoints = new ArrayList<ElementLocation>();
		for (ElementLocation e : this.getElementLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e);
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobraæ ostatni¹ poœród dodanych dla tego wierzcho³ka lokacjê.
	 * @return ElementLocation - ostatnia dodana lokalizacja
	 */
	public ElementLocation getLastLocation() {
		if (this.getNodeLocations().size() == 0)
			return null;
		return this.getNodeLocations().get(this.getNodeLocations().size() - 1);
	}

	/**
	 * Metoda pozwala narysowaæ wierzcho³ek sieci Petriego na odpowiednim arkuszu.
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
	 * Metoda pozwala sprawdziæ, czy dany punkt na danym arkuszu zawiera siê
	 * w obszarze rysowania tego wierzcho³ka sieci Petriego.
	 * @param point Point - sprawdzany punkt
	 * @param sheetId int - identyfikator arkusza
	 * @return boolean - true, jeœli lokalizacja faktycznie zawiera siê w obszarze 
	 * 	rysowania tego wierzcho³ka sieci Petriego; false w przeciwnym wypadku
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
	 * Metoda pozwala pobraæ lokacjê wierzcho³ka sieci Petriego, której 
	 * obszar rysowania zawiera dany punkt na danym arkuszu.
	 * @param point Point - sprawdzany punkt
	 * @param sheetId int - identyfikator arkusza
	 * @return ElementLocation - lokalizacja wierzcho³ka sieci Petriego, 
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
	 * Metoda pozwala pobraæ listê lokacji, które zawieraj¹ siê w danym
	 * prostok¹tnym obszarze na danym arkuszu.
	 * @param rectangle Rectangle - prostok¹tny obszar
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[ElementLocation] - lista lokalizacji, które
	 * 			zawieraj¹ siê w wybranym prostok¹tnym obszarze na wybranym arkuszu
	 */
	public ArrayList<ElementLocation> getLocationsWhichAreContained(Rectangle rectangle, int sheetId) {
		ArrayList<ElementLocation> returnElementLocations = new ArrayList<ElementLocation>();
		for (ElementLocation el : this.getNodeLocations(sheetId))
			if (rectangle.contains(el.getPosition()))
				returnElementLocations.add(el);
		return returnElementLocations;
	}

	/**
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzcho³ka
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
	 * Wykonanie tej metody oznacza wszystkie lokacje tego wierzcho³ka
	 * sieci Petriego jako nie wybrane.
	 */
	public void deselectAllPortals() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations())
			el.setPortalSelected(false);
	}

	/**
	 * Metoda pozwala sprawdziæ, czy wszystkie lokacje tego wierzcho³ka 
	 * sieci Petriego s¹ oznaczone jako wybrane.
	 * @return boolean - true jeœli wierzcho³ek jest portalem; false w przeciwnym wypadku
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
	 * Metoda sprawdza, czy wierzcho³ek sieci Petriego jest portalem 
	 * (czy ma wiêcej ni¿ jedn¹ lokacjê).
	 * @return boolean - true, jeœli wierzcho³ek jest portalem; false w przeciwnym wypadku
	 */
	public boolean isPortal() {
		return isPortal;
	}

	/**
	 * Metoda pozwala oznaczyæ, czy wierzcho³ek jest portalem (czy ma 
	 * wiêcej ni¿ jedn¹ lokacjê).
	 * @param isPortal boolean - wartoœæ okreœlaj¹ca, czy wierzcho³ek ma byæ portalem
	 */
	public void setPortal(boolean isPortal) {
		this.isPortal = isPortal;
	}

	/**
	 * Metoda pozwala pobraæ promieñ okrêgu, na którym opisana jest figura
	 * geometryczna reprezentuj¹ca obiekt w edytorze graficznym. 
	 * @return int - promieñ okrêgu, na którym opisana jest figura 
	 * 		geometryczna reprezentuj¹ca obiekt w edytorze graficznym
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * Metoda pozwala ustawiæ promieñ okrêgu, na którym opisana jest figura
	 * geometryczna reprezentuj¹ca obiekt w edytorze graficznym. 
	 * @param radius int - promieñ okrêgu 
	 */
	public void setRadius(int radius) {
		this.radius = radius;
	}

	/**
	 * Metoda pozwala pobraæ listê wszystkich lokacji wierzcho³ka.
	 * @return ArrayList[ElementLocation] - lista wszystkich lokalizacji wierzcho³ka
	 */
	public ArrayList<ElementLocation> getNodeLocations() {
		return getElementLocations();
	}

	/**
	 * Metoda pozwala ustawiæ listê wszystkich lokalizacji wierzcho³ka.
	 * @param ArrayList[ElementLocation] nodeLocations - lista nowucj lokalizacji wierzcho³ka
	 */
	public void setNodeLocations(ArrayList<ElementLocation> nodeLocations) {
		this.setElementLocations(nodeLocations);
	}

	/**
	 * Metoda pozwala usun¹æ lokacjê z listy wszystkich lokacji wierzcho³ka.
	 * @param el ElementLocation - lokalizacja do usuniêcia 
	 * @return boolean - true, jeœli lokalizacja zosta³a pomyœlnie usuniêta; false w przeciwnym wypadku
	 */
	public boolean removeElementLocation(ElementLocation el) {
		this.getNodeLocations().remove(el);
		if (this.getNodeLocations().size() > 0)
			return true;
		else
			return false;
	}

	/**
	 * Metoda pozwala pobraæ listê ³uków wejœciowych.
	 * @return ArrayList[Arc] - lista ³uków wejœciowych
	 */
	public ArrayList<Arc> getInArcs() {
		ArrayList<Arc> totalInArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalInArcs.addAll(location.getInArcs());
		}
		return totalInArcs;
	}

	/**
	 * Metoda pozwala pobraæ listê ³uków wyjœciowych.
	 * @return ArrayList[Arc] - lista ³uków wyjœciowych
	 */
	public ArrayList<Arc> getOutArcs() {
		ArrayList<Arc> totalOutArcs = new ArrayList<Arc>();
		for (ElementLocation location : getNodeLocations()) {
			totalOutArcs.addAll(location.getOutArcs());
		}
		return totalOutArcs;
	}

	/**
	 * Metoda pozwala pobraæ listê wierzcho³ków wejœciowych.
	 * @return ArrayList[Node] - lista wierzcho³ków wejœciowych
	 */
	public ArrayList<Node> getInNodes() {
		ArrayList<Node> totalInNodes = new ArrayList<Node>();
		for (Arc arc : getInArcs()) {
			totalInNodes.add(arc.getStartNode());
		}
		return totalInNodes;
	}

	/**
	 * Metoda pozwala pobraæ listê wierzcho³ków wyjœciowych.
	 * @return ArrayList[Node] - lista wierzcho³ków wyjœciowych
	 */
	public ArrayList<Node> getOutNodes() {
		ArrayList<Node> totalOutNodes = new ArrayList<Node>();
		for (Arc arc : getOutArcs()) {
			totalOutNodes.add(arc.getEndNode());
		}
		return totalOutNodes;
	}

	/**
	 * Zwraca wartoœæ identyfikatora wêz³a.
	 * @return String - ³añcuch znaków ID
	 */
	public String toString() {
		String s = "ID: " + Integer.toString(this.getID());
		return s;
	}

	/**
	 * Metoda zwraca lokalizacjê obiektu - portalu.
	 * @return ArrayList[ElementLocation] - tablica wspo³rzêdnych portalu
	 */
	public ArrayList<ElementLocation> getElementLocations() {
		return elementLocations;
	}

	/**
	 * Metoda ustawia nowe lokalizacje obiektu - portalu
	 * @param elementLocations ArrayList[ElementLocation] - tablica lokalizacji wêz³a - portalu
	 */
	public void setElementLocations(ArrayList<ElementLocation> elementLocations) {
		this.elementLocations = elementLocations;
	}
}
