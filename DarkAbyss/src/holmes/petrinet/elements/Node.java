package holmes.petrinet.elements;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.settings.SettingsManager;

/**
 * Klasa implementująca wierzchołek sieci Petriego. Dziedziczą po niej klasy
 * reprezentujące miejsca (Place) oraz tranzycje (Transition). Zapewnia
 * implementację wspólnych dla nich funkcjonalności, jak rysowanie ich na
 * odpowiednim arkuszu czy umożliwienie tworzenia portali (wielu odnośników
 * graficznych do jednego wierzchołka sieci).
 * @author students - główna forma
 * @author MR - drobne poprawki
 */
public abstract class Node extends PetriNetElement {
	private static final long serialVersionUID = -8569201372990876149L;
	private ArrayList<ElementLocation> elementLocations = new ArrayList<ElementLocation>();
	private ArrayList<ElementLocation> namesLocations = new ArrayList<ElementLocation>();
	private boolean isPortal = false;
	private int radius = 20;
	final static float dash1[] = { 2.0f };
	final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);

	/**
	 * Konstruktor obiektu klasy Node. Ustawia też początkowe wartości przesunięcia nazwy wierzchołka.
	 * @param sheetId int - identyfikator arkusza
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param nodePosition Point - punkt, w którym znajduje się lokalizacja tego wierzchołka na odpowiednim arkuszu
	 * @param radius int - promień okręgu, na którym opisana jest figura geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public Node(int sheetId, int nodeId, Point nodePosition, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		//lokalizacje graficzne:
		this.getNodeLocations().add(new ElementLocation(sheetId, nodePosition, this));
		//napis - przesunięcie startowe:
		this.getNamesLocations().add(new ElementLocation(sheetId, new Point(0,0), this));
	}

	/**
	 * Konstruktor obiektu klasy Node. Jest wywoływany między innymi w czasie tworzenia portalu,
	 * tj. wtedy, kiedy jeden węzeł sieci ma 2 lub więcej lokalizacji
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji elementu sieci Petriego
	 * @param radius int - promień okręgu, na którym opisana jest figura geometryczna reprezentująca obiekt w edytorze graficznym
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
		this.setElementLocations(elementLocations);
		if (elementLocations.size() > 1) { // oczywiście konstruktor może też tworzyć zwykły węzeł
			setPortal(true); //skoro po coś ta metoda w ogóle powstała...
		}
	}

	/**
	 * Konstruktor obiektu klasy Node. Ustawia też początkowe wartości przesunięcia nazwy wierzchołka.
	 * @param nodeId int - identyfikator elementu sieci Petriego
	 * @param elementLocation ElementLocation - lokalizacja elementu sieci Petriego
	 * @param radius int - promień okręgu, na którym opisana jest figura geometryczna reprezentująca obiekt w edytorze graficznym
	 */
	public Node(int nodeId, ElementLocation elementLocation, int radius) {
		this.setRadius(radius);
		this.setID(nodeId);
		elementLocation.setParentNode(this);
		this.getElementLocations().add(elementLocation);
		//napis - przesunięcie startowe:
		this.getNamesLocations().add(new ElementLocation(elementLocation.getSheetID(), new Point(0,0), this));
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich punktów lokalizacji wierzchołka na arkuszu o określonym identyfikatorze.
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
	 * Metoda pozwala pobrać listę wszystkich punktów lokalizacji nazwy wierzchołka na arkuszu o określonym identyfikatorze.
	 * @param sheetId int - identyfikator arkusza
	 * @return ArrayList[Point] - lista punktów lokalizacji nazwy wierzchołka na wybranym arkuszu
	 */
	public ArrayList<Point> getNodeNamePositions(int sheetId) {
		ArrayList<Point> returnPoints = new ArrayList<Point>();
		for (ElementLocation e : this.getNamesLocations())
			if (e.getSheetID() == sheetId)
				returnPoints.add(e.getPosition());
		return returnPoints;
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich lokacji wierzchołka na arkuszu o określonym identyfikatorze
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
		for (Point p : this.getNodePositions(sheetId)) {
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
		}
	}
	
	/**
	 * Metoda odpowiedzialna za rysowanie nazwy wierzchołka na obrazie sieci.
	 * @param g Graphics2D - obiekt rysujący
	 * @param sheetId int - identyfikator arkusza
	 * @param places ArrayList[Place] - wektor miejsc
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 * @param metanodes ArrayList[MetaNode] - wektor metawęzłów
	 */
	public void drawName(Graphics2D g, int sheetId, ArrayList<Place> places, ArrayList<Transition> transitions,
			 ArrayList<MetaNode> metanodes) {
		SettingsManager sm = GUIManager.getDefaultGUIManager().getSettingsManager();
		String name = getName();
		if(sm.getValue("editorShowShortNames").equals("1")) {
			if(this instanceof Place) {
				int x = places.indexOf(this);
				name = "p"+x;
			} else if (this instanceof Transition) {
				int x = transitions.indexOf(this);
				name = "t"+x;
			} else {
				int x = metanodes.indexOf(this);
				name = "M"+x;
			}
		}
		
		g.setColor(Color.black);
		if(sm.getValue("editorGraphFontBold").equals("0")) {
			g.setFont(new Font("Tahoma", Font.PLAIN, Integer.parseInt(sm.getValue("editorGraphFontSize"))));
		} else {
			g.setFont(new Font("Tahoma", Font.BOLD, Integer.parseInt(sm.getValue("editorGraphFontSize"))));
		}
		
		int name_width = g.getFontMetrics().stringWidth(name);
		
		ArrayList<Point> namesPoints = getNodeNamePositions(sheetId);
		ArrayList<Point> nodePoints = this.getNodePositions(sheetId);
		
		for (int i=0; i<nodePoints.size(); i++) {
			Point nodePoint = nodePoints.get(i);
			Point namePoint = namesPoints.get(i);
			
			int drawX = (nodePoint.x - name_width / 2) + namePoint.x;
			int drawY =  (nodePoint.y + getRadius() + 15) + namePoint.y;
			
			if(drawX < 0 )
				drawX = (nodePoint.x - name_width / 2); //oryginalny kod
			if(drawY < 0 )
				drawY = nodePoint.y + getRadius() + 15; //oryginalny kod
			g.drawString(name, drawX, drawY);
		}
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
					&& p.x + getRadius() > point.x && p.y + getRadius() > point.y)
				return true;
		return false;
	}

	/**
	 * Metoda pozwala pobrać lokalizację wierzchołka sieci Petriego, której 
	 * obszar rysowania zawiera dany punkt na danym arkuszu.
	 * @param point Point - sprawdzany punkt
	 * @param sheetId int - identyfikator arkusza
	 * @return ElementLocation - lokalizacja wierzchołka sieci Petriego, 
	 * 		której obszar rysowania zawiera wybrany punkt na wybranym arkuszu
	 */
	public ElementLocation getLocationWhichContains(Point point, int sheetId) {
		for (ElementLocation e : this.getNodeLocations(sheetId)) {
			if (e.getPosition().x - getRadius() < point.x && e.getPosition().y - getRadius() < point.y
					&& e.getPosition().x + getRadius() > point.x && e.getPosition().y + getRadius() > point.y)
				return e;
		}
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
	
	public void forceDeselection() {
		if (!isPortal())
			return;
		for (ElementLocation el : this.getNodeLocations()) {
			el.setPortalSelected(false);
			el.setSelected(false);
		}
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
	 * Metoda pozwala usunąć lokalizację z listy wszystkich lokalizacji wierzchołka.
	 * @param el ElementLocation - lokalizacja do usunięcia 
	 * @return boolean - false, jeśli usunięto jedyną (czyli ostatnią) lokalizację wierzchołka, 
	 * 		true, jeżeli wierzchołek ma jeszcze inne lokalizacje
	 */
	public boolean removeElementLocation(ElementLocation el) {
		int nodeElLocIndex = this.getNodeLocations().indexOf(el);
		this.getNamesLocations().remove(nodeElLocIndex);
		this.getNodeLocations().remove(el);
		
		int subNet = el.getSheetID();
		if(subNet > 0) {
			boolean found = false;
			for(ElementLocation element : getElementLocations()) {
				if(element.getSheetID() == subNet) {
					found = true;
					break;
				}
			}
			
			if(!found) {
				GUIManager.getDefaultGUIManager().subnetsHQ.clearAllMetaArcs(this, subNet);
			}
		}
		
		
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
	 * Metoda pozwala pobrać listę WSZYSTKICH łuków wyjściowych wierzchołka.
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
	 * Metoda pozwala pobrać listę WSZYSTKICH wierzchołków wejściowych wierzchołka.
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
		String type = "";
		if(this instanceof Place)
			type = "(P)";
		else if(this instanceof Transition)
			type = "(T)";
		else if(this instanceof MetaNode)
			type = "(M)";
		else
			type = "(?)";
		
		String s = "ID: " + Integer.toString(this.getID())+type;
		return s;
	}

	/**
	 * Metoda zwraca wektor lokalizacji wierzchołka.
	 * @return ArrayList[ElementLocation] - tablica lokalizacji
	 */
	public ArrayList<ElementLocation> getElementLocations() {
		return elementLocations;
	}

	/**
	 * Metoda pozwala ustawić listę wszystkich lokalizacji wierzchołka.
	 * @param elementLocations ArrayList[ElementLocation] - wektor lokalizacji wierzchołka
	 */
	public void setElementLocations(ArrayList<ElementLocation> elementLocations) {
		this.elementLocations = elementLocations;
	}
	
	/**
	 * Metoda zwraca wektor lokalizacji nazwy wierzchołka.
	 * @return ArrayList[ElementLocation] - wektor lokalizacji nazw
	 */
	public ArrayList<ElementLocation> getNamesLocations() {
		return namesLocations;
	}

	/**
	 * Metoda ustawia nowy wektor lokalizacji nazw.
	 * @param namesLocations ArrayList[ElementLocation] - wektor lokalizacji nazw.
	 */
	public void setNamesLocations(ArrayList<ElementLocation> namesLocations) {
		this.namesLocations = namesLocations;
	}
	
	/**
	 * Metoda zwraca pozycję X nazwy wierzchołka o pozycji danej jako argument.
	 * @param index int - indeks w wektorze ElementLocations
	 * @return int - pozycja X napisu
	 */
	public int getXNameLoc(int index) {
		if(index >= namesLocations.size()) {
			GUIManager.getDefaultGUIManager().log("Internal error: invalid index for name location (X position). Node: "+getName(), "error", true);
			return 0;
		}
		return namesLocations.get(index).getPosition().x;
	}
	
	/**
	 * Metoda zwraca pozycję Y nazwy wierzchołka o pozycji danej jako argument.
	 * @param index int - indeks w wektorze ElementLocations
	 * @return int - pozycja Y napisu
	 */
	public int getYNameLoc(int index) {
		if(index >= namesLocations.size()) {
			GUIManager.getDefaultGUIManager().log("Internal error: invalid index for name location (Y position). Node: "+getName(), "error", true);
			return 0;
		}
		return namesLocations.get(index).getPosition().y;
	}
}
