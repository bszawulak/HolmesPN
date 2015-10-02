package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;

/**
 * Klasa implementująca łuk w sieci Petriego. Przechowuje referencje
 * lokalizacji na swoim początku i  końcu (istotne: lokacji, nie bezpośrednio 
 * wierzchołków). Poprzez owe lokacje można uzyskać dostęp do wierzchołków, 
 * do których należy.
 * @author students
 * @author MR - poprawki (klasy łuków)
 *
 */
public class Arc extends PetriNetElement {
	private static final long serialVersionUID = 5365625190238686098L;
	private ElementLocation locationStart;
	private ElementLocation locationEnd = null;
	private Point tempEndPoint = null;
	private boolean selected = false;
	private boolean isCorrect = false;
	private int weight = 1;
	public boolean isTransportingTokens = false;
	private int simulationStep = 0;
	private boolean simulationForwardDirection = true;
	
	private ArrayList<Point> breakPoints = null;
	public boolean isBreakArc = false; 
	
	//read-arc parameters:
	private Arc pairedArc;
	private boolean isMainArcOfPair = false;
	private TypesOfArcs arcType;
	
	public boolean qSimForcedArc = false; //czy łuk ma być wzmocniony
	public Color qSimForcedColor = Color.BLACK; //kolor wzmocnienia

	/** NORMAL, READARC, INHIBITOR, RESET, EQUAL, META_ARC */
	public enum TypesOfArcs { NORMAL, READARC, INHIBITOR, RESET, EQUAL, META_ARC }
	
	/**
	 * Konstruktor obiektu klasy Arc - chwilowo nieużywany.
	 * @param startPosition ElementLocation - lokalicja żródła łuku
	 * @param endPosition ElementLocation - lokalicja celu łuku
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition, TypesOfArcs type) {
		this(startPosition, type);

		this.setEndLocation(endPosition);
		this.setID(IdGenerator.getNextId());
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc - mousePressed(MouseEvent) - używany w momencie wybrania prawidłowego (!) 
	 * wierzchołka docelowego dla łuku.
	 * @param arcId int - identyfikator łuku
	 * @param startPosition ElementLocation - lokacja źródła łuku
	 * @param endPosition ElementLocation - lokacja celu łuku
	 */
	public Arc(int arcId, ElementLocation startPosition, ElementLocation endPosition, TypesOfArcs type) {
		this(startPosition, type);

		this.setEndLocation(endPosition);
		this.setID(arcId);
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc - odczyt sieci z pliku
	 * @param startPosition ElementLocation - lokacja źródła łuku
	 * @param endPosition ElementLocation - lokacja celu łuku
	 * @param comment String - komentarz
	 * @param weight int - waga łuku
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition, String comment, int weight, TypesOfArcs type) {
		this(startPosition, type);
		
		this.setID(IdGenerator.getNextId());
		this.setEndLocation(endPosition);
		this.checkIsCorect(endPosition);
		this.setComment(comment);
		this.setWeight(weight);
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc - bez ID, TYLKO na potrzeby rysowania konturu w momencie rozpoczęcia
	 * rysowania (prowadzenia) łuku do miejsca docelowego.
	 * @param startPosition ElementLocation - lokalizacja źródła łuku
	 */
	public Arc(ElementLocation startPosition, TypesOfArcs type) {
		this.arcType = type;
		this.setStartLocation(startPosition);
		this.setEndPoint(startPosition.getPosition());
		this.setType(PetriNetElementType.ARC);
		
		this.breakPoints = new ArrayList<Point>();
	}

	/**
	 * Metoda sprawdza, czy aktualny łuk jest łukiem odczytu (read-arc).
	 * Jeśli tak, ustala wartość obiektu łuku 
	 */
	public void lookForArcPair() {
		if(this.getArcType() == TypesOfArcs.META_ARC)
			return;
		
		ArrayList<Arc> candidates = this.getEndLocation().getOutArcs();
		for (Arc a : candidates) {
			if (a.getEndLocation() == this.getStartLocation()) {
				a.setMainArcOfPair(true);
				a.setPairedArc(this);
				this.setPairedArc(a);
			}
		}
		//TODO: flaga readarc?
	}

	/**
	 * Metoda zwracająca wagę łuku.
	 * @return int - waga łuku
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Metoda pozwalająca ustawić wagę łuku.
	 * @param weight int - waga łuku
	 */
	public void setWeight(int weight) {
		this.weight = weight;
		if (pairedArc != null && isMainArcOfPair)
			pairedArc.setWeight(weight);
	}

	/**
	 * Metoda zwraca komentarz związany z łukiem.
	 * @return comment String - komentarz do łuku
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Metoda ustawiająca komentarz dla łuku.
	 * @param com String - komentarz do łuku
	 */
	public void setComment(String com) {
		comment = com;
		if (pairedArc != null && isMainArcOfPair)
			pairedArc.setComment(com);
	}

	/**
	 * Metoda pozwala pobrać wierzchołek początkowy łuku.
	 * @return Node - wierzchołek wejściowy łuku
	 */
	public Node getStartNode() {
		return this.locationStart.getParentNode();
	}

	/**
	 * Metoda pozwala pobrać wierzchołek końcowy łuku.
	 * @return Node - wierzchołek wyjściowy łuku
	 */
	public Node getEndNode() {
		if (this.locationEnd != null)
			return this.locationEnd.getParentNode();
		return null;
	}

	/**
	 * Metoda pozwala pobrać identyfikator arkusza, na którym znajduje się łuk.
	 * @return int - identyfikator arkusza
	 */
	public int getLocationSheetId() {
		return this.locationStart.getSheetID();
	}

	/**
	 * Metoda pozwala obliczyć długość łuku na arkuszu w pikselach.
	 * @return double - długość łuku
	 */
	public double getWidth() {
		Point A = this.getStartLocation().getPosition();
		Point B = this.getEndLocation().getPosition();
		return Math.hypot(A.x - B.x, A.y - B.y);
	}

	/**
	 * Metoda pozwala narysować token na łuku w czasie symulacji.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza
	 */
	public void drawSimulationToken(Graphics2D g, int sheetId) {
		ElementDraw.drawToken(g, sheetId, this);
	}

	/**
	 * Metoda przechodzi do kolejnego kroku rysowania symulacji na łuku. W zasadzie to
	 * głównie zwiększa simulationStep, który odpowiada za liczbę 'klatek' przepływu tokenu.
	 */
	public void incrementSimulationStep() {
		if (!this.isTransportingTokens)
			return;
		
		int STEP_COUNT = GUIManager.getDefaultGUIManager().simSettings.getArcDelay();
		this.simulationStep++;
		
		if (this.getSimulationStep() > STEP_COUNT) {
			this.setSimulationStep(0);
			this.setTransportingTokens(false);
		}
	}

	/**
	 * Metoda rysująca łuk na danym arkuszu przy zmienionych rozmiarach arkusza.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza
	 * @param zoom int - zoom, unused
	 * @param eds ElementDrawSettings - ustawienia rysowania
	 */
	public void draw(Graphics2D g, int sheetId, int zoom, ElementDrawSettings eds) {
		g = ElementDraw.drawArc(this, g, sheetId, zoom, eds);
	}

	/**
	 * Metoda pozwala sprawdzić, czy łuk jest poprawny.
	 * @return boolean - true, jeśli łuk jest poprawny; false w przeciwnym wypadku
	 */
	public boolean getIsCorect() {
		return this.isCorrect;
	}

	/**
	 * Metoda pozwala sprawdzić, czy łuk byłby poprawny dla danej lokalizacji wierzchołka wyjściowego.
	 * @param e ElementLocation - lokalizacja wierzchołka wyjściowego
	 * @return boolean - true, jeśli łuk byłby poprawny; false w przeciwnym wypadku
	 */
	public boolean checkIsCorect(ElementLocation e) {
		this.isCorrect = true;
		if (e == null 
			|| e.getParentNode().getType() == this.getStartLocation().getParentNode().getType()
			|| e == this.getStartLocation()) {
			this.isCorrect = false;
		}
		return this.isCorrect;
	}

	/**
	 * Metoda pozwala ustawić punkt lokacji wierzchołka wyjściowego łuku.
	 * @param p Point - punkt lokalizacji wierzchołka wyjściowego
	 */
	public void setEndPoint(Point p) {
		this.tempEndPoint = p;
	}
	
	public Point getTempEndPoint() {
		return this.tempEndPoint;
	}

	/**
	 * Metoda pozwala pobrać stan zaznaczenia łuku.
	 * @return boolean - true, jeśli łuk jest zaznaczony; false w przeciwnym wypadku
	 */
	public boolean getSelected() {
		return selected;
	}

	/**
	 * Metoda pozwala sprawdzić czy łuk zostanie zaznaczony.
	 * @return true, jeśli łuk zostanie zaznaczony; false w przeciwnym wypadku
	 */
	public boolean checkSelection() {
		if (this.locationEnd == null || this.locationStart == null)
			return false;
		setSelected(this.locationEnd.isSelected() && this.locationStart.isSelected());
		return this.getSelected();
	}

	/**
	 * Metoda pozwala ustawić zaznaczenie łuku.
	 * @param select boolean - wartość zaznaczenia łuku
	 */
	public void setSelected(boolean select) {
		this.selected = select;
	}

	/**
	 * Metoda pozwala sprawdzić, czy punkt jest częcią łuku.
	 * @param P Point - punkt (x,y)
	 * @return boolean - true, jeśli łuk jest częcią łuku; false w przeciwnym wypadku
	 */
	public boolean checkIntersection(Point P) {
		int breaks = breakPoints.size();
		if(breaks > 0) {
			Point start = getStartLocation().getPosition();
			Point b0 = breakPoints.get(0);
			if (Line2D.ptSegDist(start.x, start.y, b0.x, b0.y, P.x, P.y) <= 3) //piewszy odcinek
				return true;
			
			for(int i=1; i<breaks; i++) {
				if (Line2D.ptSegDist(breakPoints.get(i-1).x, breakPoints.get(i-1).y, breakPoints.get(i).x, breakPoints.get(i).y, P.x, P.y) <= 3) //piewszy odcinek
					return true;
			}
			
			Point bFinal = breakPoints.get(breaks-1);
			Point end = getEndLocation().getPosition();
			
			if (Line2D.ptSegDist(bFinal.x, bFinal.y, end.x, end.y, P.x, P.y) <= 3)
				return true;
			else
				return false;
			
		} else {
			Point A = getStartLocation().getPosition();
			Point B = getEndLocation().getPosition();
			if (Line2D.ptSegDist(A.x, A.y, B.x, B.y, P.x, P.y) <= 3)
				return true;
			else
				return false;
		}
	}

	/**
	 * Metoda pozwala pobrać lokację wierzchołka wejściowego łuku.
	 * @return startLocation ElementLocation - lokalizacja wierzchołka wejściowego łuku
	 */
	public ElementLocation getStartLocation() {
		return locationStart;
	}

	/**
	 * Metoda pozwala ustawić lokalizację wierzchołka wejściowego łuku.
	 * @param startLocation ElementLocation - lokalizacja wierzchołka wejściowego
	 */
	public void setStartLocation(ElementLocation startLocation) {
		this.locationStart = startLocation;
		
		if(this.arcType == TypesOfArcs.META_ARC) {
			this.locationStart.accessMetaOutArcs().add(this);
		} else {
			this.locationStart.addOutArc(this);
		}
	}

	/**
	 * Jak setStartLocation, z tym, że nie dodaje łuku do listy łuków obiektu locationStart.
	 * @param startLocation ElementLocation - nowy element location. Okrętu się pan spodziewałeś?
	 */
	public void modifyStartLocation(ElementLocation startLocation) {
		this.locationStart = startLocation;
	}

	/**
	 * Metoda pozwala ustawić lokację wierzchołka wyjściowego łuku.
	 * @param elementLocation ElementLocation - lokalizacja wierzchołka wyjściowego
	 */
	public void setEndLocation(ElementLocation elementLocation) {
		if (elementLocation == null)
			return;
		this.locationEnd = elementLocation;
		
		if(this.arcType == TypesOfArcs.META_ARC) {
			this.locationEnd.accessMetaInArcs().add(this);
		} else {
			this.locationEnd.addInArc(this);
		}

		this.tempEndPoint = null;
		this.isCorrect = true;
	}
	
	/**
	 * Działa jak setEndLocation, ale nie dodaje łuku do listy łuków obiektu locationEnd.
	 * @param elementLocation ElementLocation - nowy element location. Okrętu się pan spodziewałeś?
	 */
	public void modifyEndLocation(ElementLocation elementLocation) {
		this.locationEnd = elementLocation;
	}
	
	/**
	 * Metoda pozwala pobrać lokalizację wierzchołka wyjściowego łuku.
	 * @return ElementLocation - lokalizacja wierzchołka wyjściowego łuku
	 */
	public ElementLocation getEndLocation() {
		return locationEnd;
	}

	/**
	 * Usuwa łuk z referencji lokacji obu wierzchołków (wejściowego i
	 * wyjściowego) łuku (odłącza łuk od wierzchołków).
	 */
	public void unlinkElementLocations() {
		if(arcType == TypesOfArcs.META_ARC) {
			if (this.locationStart != null)
				this.locationStart.accessMetaOutArcs().remove(this);
			if (this.locationEnd != null)
				this.locationEnd.accessMetaInArcs().remove(this);
		} else {
			if (this.locationStart != null)
				this.locationStart.removeOutArc(this);
			if (this.locationEnd != null)
				this.locationEnd.removeInArc(this);
		}
	}

	/**
	 * Metoda pozwala sprawdzić, czy łuk aktualnie transportuje tokeny.
	 * @return boolean - true, jeśli łuk transportuje tokeny; false w przeciwnym wypadku
	 */
	public boolean isTransportingTokens() {
		return isTransportingTokens;
	}

	/**
	 * Metoda pozwala ustawić, czy łuk aktualnie transportuje tokeny.
	 * @param isTransportingTokens boolean - wartość określająca, czy łuk transportuje aktualnie tokeny
	 */
	public void setTransportingTokens(boolean isTransportingTokens) {
		this.isTransportingTokens = isTransportingTokens;
		this.setSimulationStep(0);
		if (!isTransportingTokens) {
			if (isSimulationForwardDirection()) {
				if (getStartNode().getType() == PetriNetElementType.TRANSITION)
					((Transition) getStartNode()).setLaunching(false);
			} else {
				if (getEndNode().getType() == PetriNetElementType.TRANSITION)
					((Transition) getEndNode()).setLaunching(false);
			}
		}
	}

	/**
	 * Metoda pozwala pobrać aktualny krok wizualizacji symulacji.
	 * @return int - numer aktualnego kroku wizualizacji symulacji
	 */
	public int getSimulationStep() {
		return simulationStep;
	}

	/**
	 * Metoda pozwala ustawić aktualny krok wizualizacji symulacji.
	 * @param symulationStep int - numer kroku symulacji
	 */
	public void setSimulationStep(int symulationStep) {
		this.simulationStep = symulationStep;
	}

	/**
	 * Metoda pozwala sprawdzić, czy symulacja zachodzi zgodnie ze 
	 * skierowaniem łuku (do przodu).
	 * @return boolean - true, jeśli symulacja zachodzi zgodnie ze skierowaniem łuku (do przodu);
	 * 		false w przeciwnym wypadku
	 */
	public boolean isSimulationForwardDirection() {
		return simulationForwardDirection;
	}

	/**
	 * Metoda pozwala ustawić kierunek wizualizacji symulacji na łuku.
	 * @param simulationForwardDirection boolean - true dla symulacji 'do przodu';
	 * 		false w przeciwnym wypadku
	 */
	public void setSimulationForwardDirection(boolean simulationForwardDirection) {
		this.simulationForwardDirection = simulationForwardDirection;
	}

	/**
	 * Metoda zwracająca łuk odczytu dla danego łuku.
	 * @return Arc - łuk odczytu
	 */
	public Arc getPairedArc() {
		return pairedArc;
	}

	/**
	 * Metoda ustawia wartość pairedArc jeśli łuk jest łukiem odczytu.
	 * @param pairedArc Arc - łuk odczytu
	 */
	public void setPairedArc(Arc pairedArc) {
		if(this.getArcType() == TypesOfArcs.META_ARC)
			return;
		
		this.pairedArc = pairedArc;
		this.arcType = TypesOfArcs.READARC;
	}

	/**
	 * Metoda informuje, czy łuk jest głównym łukiem z pary (read-arc)
	 * @return boolean - true jeżeli łuk jest głównym z pary; false w przeciwnym wypadku
	 */
	public boolean isMainArcOfPair() {
		return isMainArcOfPair;
	}

	/**
	 * Metoda pozwala ustalić wartość flagi, czy łuk jest głównym w parze (read-arc)
	 * @param isMainArcOfPair boolean - true jeśli jest; false w przeciwnym wypadku
	 */
	public void setMainArcOfPair(boolean isMainArcOfPair) {
		this.isMainArcOfPair = isMainArcOfPair;
	}
	
	/**
	 * Metoda zwraca typ łuku.
	 * @return TypesOfArcs
	 */
	public TypesOfArcs getArcType() {
		return arcType;
	}
	
	/**
	 * Tylko do użytku wczytywania danych: ustawia typ łuku.
	 * @param type TypesOfArcs - typ łuku
	 */
	public void setArcType(TypesOfArcs type) {
		arcType = type;
	}
	
	//****************************************************************************************************
	//************************************     BREAKING BAD     ******************************************
	//****************************************************************************************************
	
	/**
	 * Uzyskanie dostępu do tablicy punktów łamiących.
	 * @return ArrayList[Point] - wektor punktów
	 */
	public ArrayList<Point> accessBreaks() {
		return this.breakPoints;
	}
	
	/**
	 * Dodaje punkt łamiący dla łuku.
 	 * @param breakP Point - obiekt współrzędnych
	 */
	public void addBreakPoint(Point breakP) {
		this.breakPoints.add(breakP);
		isBreakArc = true;
	}
	
	/**
	 * Czyści do zera wektor punktów łamiących.
	 */
	public void clearBreakPoints() {
		this.breakPoints.clear();
		isBreakArc = false;
	}
	
	public void updateAllBreakPointsLocations(Point delta) {
		for(Point breakP : breakPoints) {
			breakP.setLocation(breakP.x+delta.x, breakP.y+delta.y);
		}
	}
	
	/**
	 * Zwraca punkt łamiący łuk, o ile kliknięto w jego pobliżu
	 * @param mousePt Point - tu kliknięto myszą
	 * @return Point - punkt łamiący łuku (jeśli istnieje w pobliżu mousePt)
	 */
	public Point checkBreakIntersection(Point mousePt) {
		for(Point breakP : breakPoints) {
			if (breakP.x - 5 < mousePt.x && breakP.y - 5 < mousePt.y
					&& breakP.x + 5 > mousePt.x && breakP.y + 5 > mousePt.y)
				return breakP;
		}
		return null;
	}
	
	/**
	 * Dodaje nowy punkt łamiący łuku, najpierw sprawdza który odcinek łuku podzielić.
	 * @param breakP Point - punkt kliknięty NA łuku (zapewnione przed wywołaniem tej metody!)
	 */
	public void createNewBreakPoint(Point breakP) {
		Point start = this.getStartLocation().getPosition();
		int breaks = breakPoints.size();
		if(breaks == 0) {
			addBreakPoint(breakP);
		} else {
			int whereToInsert = 0;
			Point b0 = breakPoints.get(0);
			if (Line2D.ptSegDist(start.x, start.y, b0.x, b0.y, breakP.x, breakP.y) <= 3) {//piewszy odcinek
				breakPoints.add(whereToInsert, breakP);
				return;
			}
			
			for(int i=1; i<breaks; i++) {
				whereToInsert++;
				if (Line2D.ptSegDist(breakPoints.get(i-1).x, breakPoints.get(i-1).y, breakPoints.get(i).x, breakPoints.get(i).y, breakP.x, breakP.y) <= 3) {
					breakPoints.add(whereToInsert, breakP);
					return;
				}
			}
			
			//jeśli dotąd żaden odcinek, to wstawiamy na koniec listy:
			breakPoints.add(breakP);
		}
	}
	
	/**
	 * Usuwa podany punkt łamiący łuku, tj. najbliższy do breakP.
	 * @param breakP Point - tu kliknięto myszą, najpierw metoda sprawdzi, czy blisko tego jest break point
	 */
	public void removeBreakPoint(Point breakP) {
		Point toRemove = checkBreakIntersection(breakP);
		if(toRemove != null) {
			breakPoints.remove(toRemove);
			if(breakPoints.size() == 0)
				isBreakArc = false;
		}
	}
	
	//********************************************************************************************************
	//********************************************************************************************************
	//********************************************************************************************************
	
	/**
	 * Metoda zwracająca dane o łuku w formie łańcucha znaków.
	 * @return String - łańcuch znaków informacji o łuku sieci
	 */
	public String toString() {
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		String startNode = "";
		int startNodeLoc = -1;
		int startELLoc = -1;
		int startNodeID = -1;
		if(this.getStartLocation() != null) {
			Node node = this.getStartLocation().getParentNode();
			if(node != null) {
				if(node instanceof Place) {
					startNode = "P";
					startNodeLoc = pn.getPlaces().indexOf(node);
				} else if(node instanceof Transition) { 
					startNode = "T";
					startNodeLoc = pn.getTransitions().indexOf(node);
				} else if(node instanceof MetaNode) {
					startNode = "M"; 
					startNodeLoc = pn.getMetaNodes().indexOf(node);
				}
				startELLoc = node.getElementLocations().indexOf(this.getStartLocation());
				startNodeID = node.getID();
			}
		}
		String endNode = "";
		int endNodeLoc = -1;
		int endELLoc = -1;
		int endNodeID = -1;
		if(this.getEndLocation() != null) {
			Node node = this.getEndLocation().getParentNode();
			if(node != null) {
				if(node instanceof Place) {
					endNode = "P";
					endNodeLoc = pn.getPlaces().indexOf(node);
				} else if(node instanceof Transition) {
					endNode = "T";
					endNodeLoc = pn.getTransitions().indexOf(node);
				} else if(node instanceof MetaNode) {
					endNode = "M";
					endNodeLoc = pn.getMetaNodes().indexOf(node);
				}
				endELLoc = node.getElementLocations().indexOf(this.getEndLocation());
				endNodeID = node.getID();
			}
		}
		
		String s = " ArcType: "+arcType.toString()
				+" StartNode: " + startNode + startNodeLoc + "("+startELLoc+") [gID:"+startNodeID+"]  ==>  "
				+ " EndNode: " + endNode + endNodeLoc + "("+endELLoc+") [gID:"+endNodeID+"]";
		return s;
	}
}
