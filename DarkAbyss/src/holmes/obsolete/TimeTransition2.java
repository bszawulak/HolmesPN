package holmes.obsolete;
/*
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.IdGenerator;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Transition;
*/

/**
 * Klasa implementująca tranzycje czasowe w modelu TPN (Time Petri Net)
 * 
 * @author BS
 * <br><br>
 * [MR] implementacja przeniesiona do klasy Transition. Jak każdy inny model sieci, nie tylko czasowy
 *
 */
public class TimeTransition2 {
	
}

/*
public class TimeTransition2 extends Transition {
	private static final long serialVersionUID = -7512230002147987244L;
	
	protected double minFireTime = 0; //TPN
	protected double maxFireTime = 999;	//TPN
	//protected double absoluteFireTime = 0; diabli wiedzą od czego to, nic nie robi
	protected double internalFireTime = -1; //zmienna związana z modelem sieci TPN
	protected double internalTimer = -1;
	
	//
	// Konstruktor obiektu tranzycji czasowej.
	// @param transitionId int - identyfikator tranzycji
	// @param sheetId int - identyfikator arkusza
	// @param transitionPosition Point - punkt lokalizacji
	// @param name String - nazwa tranzycji
	// @param comment String - komentarz tranzycji
	//
	public TimeTransition2(int transitionId, int sheetId, Point transitionPosition, String name, String comment) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName(name);
		this.setComment(comment);
		//this.setType(PetriNetElementType.TIMETRANSITION);
	}

	//
	// Konstruktor obiektu tranzycji czasowej.
	// @param transitionId int - identyfikator tranzycji
	// @param elementLocation ElementLocation - lokalizacja tranzycji
	// @param name String - nazwa tranzycji
	// @param comment String - komentarz tranzycji
	//
	public TimeTransition2(int transitionId, ElementLocation elementLocation, String name, String comment) {
		super(transitionId, elementLocation, 15);
		this.setName(name);
		this.setComment(comment);
		//this.setType(PetriNetElementType.TIMETRANSITION);
	}

	//
	// Konstruktor obiektu tranzycji czasowej.
	// @param transitionId int - identyfikator tranzycji
	// @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
	// @param name String - nazwa tranzycji
	// @param comment String - komentarz tranzycji
	//
	public TimeTransition2(int transitionId, ArrayList<ElementLocation> elementLocations, String name, String comment) {
		super(transitionId, elementLocations, 15);
		this.setName(name);
		this.setComment(comment);
		//this.setType(PetriNetElementType.TIMETRANSITION);
	}

	//
	// Konstruktor obiektu tranzycji czasowej.
	// @param transitionId int - identyfikator tranzycji
	// @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
	//
	public TimeTransition2(int transitionId, ArrayList<ElementLocation> elementLocations) {
		super(transitionId, elementLocations, 15);
		this.setName("Transition" + Integer.toString(IdGenerator.getNextTransitionId()));
		//this.setType(PetriNetElementType.TIMETRANSITION);
	}

	//
	// Konstruktor obiektu tranzycji czasowej.
	// @param transitionId int - identyfikator tranzycji
	// @param sheetId int - identyfikator arkusza
	// @param transitionPosition Point - punkt lokalizacji tranzycji
	//
	public TimeTransition2(int transitionId, int sheetId, Point transitionPosition) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName("Transition" + Integer.toString(IdGenerator.getNextTransitionId()));
		//this.setType(PetriNetElementType.TIMETRANSITION);
	}
	
	//
	// Metoda zwraca typ tranzycji, w tym wypadku - czasowa
	// @return PetriNetElementType - element sieci
	//
	public PetriNetElementType getType() {
		//TODO!!!
		return PetriNetElementType.UNKNOWN;
		//return PetriNetElementType.TIMETRANSITION;
	}

	//
	// Metoda rysująca tranzycję na danym arkuszu.
	// @param g Graphics2D - grafika 2D
	// @param sheetId int - identyfikator arkusza 
	//
	public void draw(Graphics2D g, int sheetId) {
		g = ElementDraw.drawElement(this, g, sheetId);
		//drawName(g, sheetId);
	}

	//
	// Metoda umieszczająca nazwę tranzycji pod jej symbolem. Ostatnia wartość
	// w metodzie - 15 - oznacza nieco poniżej kwadratu. Jej zwiększenie
	// obniża napis.
	// @param g Graphics2D - grafika 2D
	// @param sheetId int - identyfikator arkusza 
	//
	public void unused_drawNameTT(Graphics2D g, int sheetId) {
		g.setColor(Color.black);
		g.setFont(new Font("Tahoma", Font.PLAIN, 11));
		int width = g.getFontMetrics().stringWidth(getName());
		for (Point p : this.getNodePositions(sheetId))
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
	}

	//
	// Metoda ustala dolny limit niezerowego czasu gotowości - EFT.
	// @param minFireTime double - czas EFT
	//
	public void setMinFireTime(double minFireTime) {
		if(minFireTime < 0) {
			this.minFireTime = 0;
			return;
		}
		if(minFireTime > maxFireTime) {
			this.minFireTime = maxFireTime;
			return;
		}
		this.minFireTime = minFireTime;
	}
	
	//
	// Metoda pozwala odczytać przypisany czas EFT tranzycji.
	// @return double - czas EFT
	//
	public double getMinFireTime() {
		return this.minFireTime;
	}

	//
	// Metoda ustala górny limit nieujemnego czasu krytycznego - LFT.
	// @param maxFireTime double - czas LFT (deadline na uruchomienie)
	//
	public void setMaxFireTime(double maxFireTime) {
		if(maxFireTime < minFireTime) {
			this.maxFireTime = minFireTime;
			return;
		}
		
		this.maxFireTime = maxFireTime;
	}

	//
	// Metoda pozwala odczytać przypisany czas LFT tranzycji.
	// @return double - czas LFT
	//
	public double getMaxFireTime() {
		return this.maxFireTime;
	}

	//
	// Metoda zwraca aktualny czas uruchomienia.
	// @return double - czas uruchomienia - pole FireTime
	//
	public double getInternalFireTime() {
		return internalFireTime;
	}

	//
	// Metoda pozwala ustawic czas uruchomienia tranzycji.
	// @param fireTime double - czas uruchomienia tranzycji
	//
	public void setInternalFireTime(double fireTime) {
		internalFireTime = fireTime;
	}
	
	//
	// Metoda zwraca aktualny zegar uruchomienia dla tranzycji.
	// @return double - czas uruchomienia - pole FireTime
	//
	public double getInternalTPN_Timer() {
		return internalTimer;
	}

	//
	// Metoda pozwala ustawic zegar uruchomienia tranzycji.
	// @param fireTime double - czas uruchomienia tranzycji
	//
	public void setInternalTPN_Timer(double fireTime) {
		internalTimer = fireTime;
	}
	
	//
	// Metoda informująca czy tramzycja czasowa MUSI zostać uruchomiona.
	// @return boolean - true, jeśli wewnętrzny zegar (!= -1) jest równy deadlinowi
	//
	public boolean isTPNforcedToFired() {
		if(internalFireTime != -1) {
			if(internalFireTime == internalTimer)
				return true;
			else
				return false;
		} else {
			return false; //nieaktywna
		}
	}
}
*/



/**
 * Konstruktor obiektu tranzycji sieci.
 * @param transitionId int - identyfikator tranzycji
 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji
 * @param pn int - promien okręgu na którym opisana jest figura geometryczna
 */
//public Transition(int transitionId, ArrayList<ElementLocation> elementLocations, int pn) {
//	super(transitionId, elementLocations, pn);
//	transType = TransitionType.PN;
//}

/**
 * Konstruktor obiektu tranzycji sieci.
 * @param sheetId int - identyfikator arkusza
 * @param transitionId int - identyfikator tranzycji
 * @param transitionPosition Point - punkt, w którym znajduje się lokalizacja wierzchołka
 * @param pn int - promien okręgu na którym opisana jest figura geometryczna
 */
//public Transition(int sheetId, int transitionId, Point transitionPosition, int pn) {
//	super(sheetId, transitionId, transitionPosition, pn);
//	transType = TransitionType.PN;
//}

/**
 * Konstruktor obiektu tranzycji sieci.
 * @param transitionId int - identyfikator tranzycji
 * @param elementLocation ElementLocation - lokalizacja elementu sieci
 * @param pn int - promień okręgu na którym opisana jest figura geometryczna
 */
//public Transition(int transitionId, ElementLocation elementLocation, int pn) {
//	super(transitionId, elementLocation, pn);
//	transType = TransitionType.PN;
//}

/**
 * Konstruktor obiektu tranzycji sieci.
 * @param transitionId int - identyfikator tranzycji
 * @param sheetId int - identyfikator arkusza
 * @param transitionPosition Point - punkt lokalizacji
 * @param name String - nazwa tranzycji
 * @param comment String - komentarz tranzycji
 */
//public Transition(int transitionId, int sheetId, Point transitionPosition, String name, String comment) {
//	super(sheetId, transitionId, transitionPosition, 15);
//	this.setName(name);
//	this.setComment(comment);
//	this.setType(PetriNetElementType.TRANSITION);
//	transType = TransitionType.PN;
//}

/**
 * Konstruktor obiektu tranzycji sieci.
 * @param transitionId int - identyfikator tranzycji
 * @param elementLocation ElementLocation - lokalizacja tranzycji
 * @param name String - nazwa tranzycji
 * @param comment String - komentarz tranzycji
 */
//public Transition(int transitionId, ElementLocation elementLocation, String name, String comment) {
//	super(transitionId, elementLocation, 15);
//	this.setName(name);
//	this.setComment(comment);
//	this.setType(PetriNetElementType.TRANSITION);
//	transType = TransitionType.PN;
//}

