package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;

/**
 * Klasa implementująca miejsce sieci Petriego. Zapewnia implementację stanu (przechowywania tokenów) oraz 
 * powiązane z tym funkcjonalności (funkcjonalność wierzchołka dziedziczy po klasie Node).
 * 
 * @author students
 * @author MR - poprawki
 *
 */
public class Place extends Node {
	private static final long serialVersionUID = 2346995422046987174L;
	private int tokensNumber = 0;
	private int reservedTokens = 0;
	
	private boolean isColorChanged;
	private Color placeColorValue;
	private boolean valueVisibilityStatus;
	private double placeNumericalValue;
	private boolean showPlaceAddText;
	private String placeAdditionalText;
	public int txtXoff;
	public int txtYoff;
	public int valueXoff;
	public int valueYoff;
	
	public Color defColor = Color.WHITE;
	
	//SSA:
	private double ssaValue = 0.0;
	
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
	 * @param eds ElementDrawSettings - opcje rysowania
	 */
	public void draw(Graphics2D g, int sheetId, ElementDrawSettings eds)
	{
		g = ElementDraw.drawElement(this, g, sheetId, eds);
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
		this.tokensNumber += delta;
		
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
	 * Metoda zamieniająca dane o miejscu sieci na łańcuch znaków.
	 * @return String - łańcuch znaków
	 */
	public String toString() {
		String name =  getName();
		if(name == null) {
			return "(P)null";
		} else {
			//return "(P)" + getName();
			return "(P" + GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().indexOf(this)+")";
		}
	}
	
	//****************************************************************************************************************************
	//********************************************      SSA       ****************************************************************
	//****************************************************************************************************************************
	
	/**
	 * Ustawia nową wartość cząstek dla miejsca w symulacji SSA.
	 * @param value double - nowa wartość
	 */
	public void setSSAvalue(double value) {
		this.ssaValue = value;
	}
	
	/**
	 * Zwraca aktualną wartość cząstek dla miejsca w symulacji SSA.
	 * @return double - liczba cząstek
	 */
	public double getSSAvalue() {
		return this.ssaValue;
	}
	
	
	//****************************************************************************************************************************
	//****************************************************************************************************************************
	//****************************************************************************************************************************
	//****************************************************************************************************************************
	//****************************************************************************************************************************
	
	/**
	 * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
	 * @param isColorChanged boolean - true, jeśli ma rysować się w kolorze
	 * @param placeColorValue Color - na jaki kolor
	 * @param showNumber boolean - true, jeśli liczba ma się wyświetlać
	 * @param placeNumericalValue double - liczba do wyświetlenia
	 * @param showText boolean - czy pokazać dodatkowy tekst
	 * @param text String - dodatkowy tekst do wyświetlenia
	 * @param txtXoff int - przesunięcie X tekstu
	 * @param txtYoff int - przesunięcie Y tekstu
	 * @param valueXoff int - przesunięcie X liczby
	 * @param valueYoff int - przesunięcie Y liczby
	 */
	public void setColorWithNumber(boolean isColorChanged, Color placeColorValue, 
			boolean showNumber, double placeNumericalValue, boolean showText, String text,
			int txtXoff, int txtYoff, int valueXoff, int valueYoff) {
		this.isColorChanged = isColorChanged;
		this.placeColorValue = placeColorValue;
		this.valueVisibilityStatus = showNumber;
		this.placeNumericalValue = placeNumericalValue;
		this.showPlaceAddText = showText;
		this.placeAdditionalText = text;
		
		this.txtXoff = txtXoff;
		this.txtYoff = txtYoff;
		this.valueXoff = valueXoff;
		this.valueYoff = valueYoff;
	}
	
	/**
	 * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
	 * @param isColorChanged boolean - true, jeśli ma rysować się w kolorze
	 * @param placeColorValue Color - na jaki kolor
	 * @param showNumber boolean - true, jeśli liczba ma się wyświetlać
	 * @param placeNumericalValue double - liczba do wyświetlenia
	 * @param showText boolean - czy pokazać dodatkowy tekst
	 * @param text String - dodatkowy tekst do wyświetlenia
	 */
	public void setColorWithNumber(boolean isColorChanged, Color placeColorValue, 
			boolean showNumber, double placeNumericalValue, boolean showText, String text) {
		this.isColorChanged = isColorChanged;
		this.placeColorValue = placeColorValue;
		this.valueVisibilityStatus = showNumber;
		this.placeNumericalValue = placeNumericalValue;
		this.showPlaceAddText = showText;
		this.placeAdditionalText = text;
	}
	
	/**
	 * Metoda informuje, czy ma się wyświetlać dodatkowa wartośc liczbowa obok rysunku miejsca.
	 * @return boolean - true, jeśli ma się wyświetlać
	 */
	public boolean getNumericalValueVisibility() {
		return valueVisibilityStatus;
	}
	
	/**
	 * Zwraca liczbę która ma się wyświetlać obok miejsca.
	 * @return double - liczba
	 */
	public double getNumericalValueDOUBLE() {
		return placeNumericalValue;
	}
	
	/**
	 * Metoda zwraca aktualnie ustawiony kolor dla miejsca
	 * @return Color - kolor
	 */
	public Color getPlaceNewColor() {
		return placeColorValue;
	}
	
	/**
	 * Metoda informuje, czy miejsce ma być rysowane z innym kolorem wypełnienia
	 * @return boolean - true, jeśli ma mieć inny kolor niż domyślny
	 */
	public boolean isColorChanged() {
		return isColorChanged;
	}
	
	/**
	 * Metoda zwraca informację, czy ma być wyświetlany dodatkowy tekst obok rysunku miejsca.
	 * @return boolean - true, jeśli tak
	 */
	public boolean showAddText() {
		return showPlaceAddText;
	}
	
	/**
	 * Metoda zwraca dodatkowy tekst do wyświetlenia.
	 * @return String - tekst
	 */
	public String returnAddText() {
		return placeAdditionalText;
	}
	
	/**
	 * Reset przesunięć.
	 */
	public void resetOffs() {
		this.txtXoff = 0;
		this.txtYoff = 0;
		this.valueXoff = 0;
		this.valueYoff = 0;
	}
}
