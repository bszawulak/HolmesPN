package abyss.math;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.ElementDraw;
import abyss.graphpanel.IdGenerator;

/**
 * Klasa implementująca miejsce sieci Petriego. Zapewnia implementację 
 * stanu (przechowywania tokenów) oraz powiązane z tym funkcjonalności
 * (funkcjonalność wierzchołka dziedzyczy po Node).
 * @author students
 *
 */
public class Place extends Node {
	// BACKUP: 2346995422046987174L  (NIE DOTYKAĆ PONIŻSZEJ ZMIENNEJ!)
	private static final long serialVersionUID = 2346995422046987174L;
	
	private int tokensNumber = 0;
	private int reservedTokens = 0;
	//public Place()
	
	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param sheetId int - identyfikator arkusza
	 * @param placePosition Point - punkt lokalizacji
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba tokenów
	 */
	public Place(int nodeId, int sheetId, Point placePosition, String name, String comment, int tokensNumber) {
		super(sheetId, nodeId, placePosition, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}
	
	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param sheetId int - identyfikator arkusza
	 * @param placePosition Point - punkt lokalizacji
	 */
	public Place(int nodeId, int sheetId, Point placePosition) {
		super(sheetId, nodeId, placePosition, 18);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocation ElementLocation - lokalizacja miejsca
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba tokenów
	 */
	public Place(int nodeId, ElementLocation elementLocation, String name, String comment, int tokensNumber) {
		super(nodeId, elementLocation, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Konstruktor obiektu miejsca sieci - wczytywanie sieci zewnętrznej, np. ze Snoopy
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba tokenów
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations, String name, String comment, int tokensNumber) {
		super(nodeId, elementLocations, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Konstruktor obiektu miejsca sieci - tworzenie portali.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations) {
		super(nodeId, elementLocations, 18);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Metoda rysująca miejsce na danym arkuszu.
	 * @param g Graphics2D - grafika 2D 
	 * @param sheetId int - identyfikator arkusza
	 */
	public void draw(Graphics2D g, int sheetId)
	{
		g = ElementDraw.drawElement(this, g, sheetId);
		//super.draw(g, sheetId);
	}

	/**
	 * Metoda pozwala pobrać aktualną liczbę tokenów z miejsca.
	 * @return int - liczba tokenów
	 */
	public int getTokensNumber() {
		return tokensNumber;
	}

	/**
	 * Metoda pozwala ustawić wartość liczby tokenów dla miejsca.
	 * @param tokensNumber int - nowa liczba tokenów
	 */
	public void setTokensNumber(int tokensNumber) {
		this.tokensNumber = tokensNumber;
		if(tokensNumber < 0) {
			GUIManager.getDefaultGUIManager().log("Critical simulation error. Number of tokens in place: "
					+this.getName()+ " below zero: ("+this.getTokensNumber()+").", "error", true);
		}
	}

	/**
	 * Metoda pozwala zmienić liczbę tokenów w miejscu, dodając do niej określoną wartość.
	 * @param delta int - wartość o którą zmieni się liczba tokenów
	 */
	public void modifyTokensNumber(int delta) {
		this.tokensNumber = this.tokensNumber + delta;
		
		if(tokensNumber < 0) {
			GUIManager.getDefaultGUIManager().log("Simulation error: number of tokens in place: "
					+this.getName()+ " below zero: ("+this.getTokensNumber()+").", "error", true);
		}
	}

	/**
	 * Metoda pozwala pobrać liczbę zajętych (zarezerwowanych  przez aktywowaną tranzycję) tokenów.
	 * @return int - liczba zarezerwowanych tokenów
	 */
	public int getReservedTokens() {
		return reservedTokens;
	}

	/**
	 * Metoda pozwala zarezerwować określoną liczbę tokenów w miejscu.
	 * @param tokensTaken int - liczba zajmowanych tokenów
	 */
	public void reserveTokens(int tokensTaken) {
		this.reservedTokens += tokensTaken;
	}

	/**
	 * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny.
	 */
	public void freeReservedTokens() {
		this.reservedTokens = 0;
	}

	/**
	 * Metoda pozwala pobrać liczbę wolnych (dostępnych, nie 
	 * zarezerwowanych przez żadną tranzycję) tokenów.
	 * @return int - liczba dostępnych tokenów
	 */
	public int getNonReservedTokensNumber() {
		return tokensNumber - getReservedTokens();
	}
	
	/**
	 * Metoda zamieniająca dane o krawędzi sieci na łańcuch znaków.
	 * @return String - łańcuch znaków
	 */
	public String toString() {
		String name =  getName();
		if(name == null) {
			return "(P)null";
		} else {
			return "(P)" + getName();
		}
	}
}