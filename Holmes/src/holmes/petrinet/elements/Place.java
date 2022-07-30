package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.simulators.IRandomGenerator;

import javax.swing.*;


/**
 * Klasa implementująca miejsce sieci Petriego. Zapewnia implementację stanu (przechowywania tokenów) oraz 
 * powiązane z tym funkcjonalności (funkcjonalność wierzchołka dziedziczy po klasie Node).
 * 
 * @author students
 * @author MR - poprawki
 *
 * [2022-06-29 MR] xTPN code incoming. Tokeny zyskują osobowość! (muszę, teraz już MUSZĘ doimplementować
 * 				króliczki zamiast czerwonych kulek. (Albo bombki. "W kszyłcie grzyba. I cygara.")
 *
 */
public class Place extends Node {
	@Serial
	private static final long serialVersionUID = 2346995422046987174L;
	protected static int realRadius = 18;
	protected int tokensNumber = 0;
	protected int reservedTokens = 0;
	protected boolean isColorChanged;
	protected Color placeColorValue;
	protected boolean valueVisibilityStatus;
	protected double placeNumericalValue;
	protected boolean showPlaceAddText;
	protected String placeAdditionalText;
	public int txtXoff;
	public int txtYoff;
	public int valueXoff;
	public int valueYoff;
	public Color defColor = Color.WHITE;
	
	//SSA:
	protected double ssaValue = 0.0;
	//quickSim
	public boolean qSimDrawed = false; //czy rysować dodatkowe oznaczenie miejsca - okrąg
	public int qSimOvalSize = 10; //rozmiar okręgu oznaczającego
	public Color qSimOvalColor = Color.RED;
	public Color qSimFillColor = Color.WHITE; //kolor oznaczenia
	public boolean qSimDrawStats = false; //czy rysować dodatkowe dane statystyczne
	public int qSimFillValue = 0; //poziom wypełnienia danych
	public double qSimTokens = 0; //ile średnio tokenów w symulacji
	//public String qSimText = ""; //dodatkowy tekst
	
	//colors:
	public boolean isColored = false;
	public int token1green = 0;
	public int token2blue = 0;
	public int token3yellow = 0;
	public int token4grey = 0;
	public int token5black = 0;
	public int reserved1green = 0;
	public int reserved2blue = 0;
	public int reserved3yellow = 0;
	public int reserved4grey = 0;
	public int reserved5black = 0;


	protected boolean isXTPN = false; //czy tokeny marzą o elektrycznych tranzycjach?

	/* ***********************************************************************************
	   ********************************    xTPN    ***************************************
	   *********************************************************************************** */
	/*
	private double gammaMin_xTPN = 0.0;
	private double gammaMax_xTPN = 99;
	private boolean gammaMode_xTPN = true;
	//grafika:
	private boolean showTokenSet_xTPN = false; //czy wyświetlać zbiór tokenów
	private boolean gammaRangeVisibility_XTPN = true;
	private int franctionDigits = 2;
	//tokeny:
	private ArrayList<Double> multisetK;
*/

	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId (<b>int</b>) identyfikator wierzchołka.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @param placePosition (<b>Point</b>) punkt lokalizacji.
	 */
	public Place(int nodeId, int sheetId, Point placePosition) {
		super(sheetId, nodeId, placePosition, realRadius);
		this.setName("Place" + IdGenerator.getNextPlaceId());
		this.setType(PetriNetElementType.PLACE);

		//this.multisetK = new ArrayList<>();
	}

	/**
	 * Konstruktor obiektu miejsca sieci - wczytywanie sieci zewnętrznej, np. ze Snoopy.
	 * @param nodeId (<b>int</b>) identyfikator wierzchołka.
	 * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji .
	 * @param name (<b>String</b>) nazwa miejsca.
	 * @param comment (<b>String</b>) komentarz miejsca.
	 * @param tokensNumber (<b>int</b>) liczba tokenów.
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations, String name, String comment, int tokensNumber) {
		super(nodeId, elementLocations, realRadius);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);

		//this.multisetK = new ArrayList<>();
	}

	/**
	 * Konstruktor obiektu miejsca sieci - tworzenie portali.
	 * @param nodeId (<b>int</b>) identyfikator wierzchołka.
	 * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji.
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations) {
		super(nodeId, elementLocations, realRadius);
		this.setName("Place" + IdGenerator.getNextPlaceId());
		this.setType(PetriNetElementType.PLACE);

		//this.multisetK = new ArrayList<>();
	}

	/**
	 * Metoda rysująca miejsce na danym arkuszu.
	 * @param g Graphics2D - grafika 2D 
	 * @param sheetId int - identyfikator arkusza
	 * @param eds ElementDrawSettings - opcje rysowania
	 */
	public void draw(Graphics2D g, int sheetId, ElementDrawSettings eds)
	{
		ElementDraw.drawElement(this, g, sheetId, eds);
		//g = ElementDraw.drawElement(this, g, sheetId, eds);
		//super.draw(g, sheetId);
	}
	
	/**
	 * Zwraca zbiór tranzycji wejściowych *p.
	 * @return ArrayList[Transition] - lista tranzycji ze zbioru *p
	 */
	public ArrayList<Transition> getPreTransitions() {
		ArrayList<Transition> preTransitions = new ArrayList<Transition>();
		for(ElementLocation el : getElementLocations()) {
			for(Arc arc : el.getInArcs()) {
				Node n = arc.getStartNode();
				if(!preTransitions.contains((Transition) n)) {
					preTransitions.add((Transition)n);
				}
			}
		}
		return preTransitions;
	}
	
	/**
	 * Zwraca zbiór tranzycji wyjściowych p*.
	 * @return ArrayList[Transition] - lista tranzycji ze zbioru p*
	 */
	public ArrayList<Transition> getPostTransitions() {
		ArrayList<Transition> postTransitions = new ArrayList<>();
		for(ElementLocation el : getElementLocations()) {
			for(Arc arc : el.getOutArcs()) {
				Node n = arc.getEndNode();
				if(!postTransitions.contains((Transition)n)) {
					postTransitions.add((Transition)n);
				}
			}
		}
		return postTransitions;
	}

	/**
	 * Metoda pozwala odczytać aktualną liczbę tokenów z miejsca.
	 * @return int - liczba tokenów
	 */
	public int getTokensNumber() {
		return tokensNumber;
	}
	
	/**
	 * Metoda pozwala odczytać aktualną liczbę kolorowych tokenów z miejsca.
	 * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
	 * @return int - liczba tokenów kolorowych
	 */
	public int getColorTokensNumber(int i) {
		return switch (i) {
			case 1 -> token1green;
			case 2 -> token2blue;
			case 3 -> token3yellow;
			case 4 -> token4grey;
			case 5 -> token5black;
			default -> tokensNumber;
		};
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
	 * Metoda pozwala ustawić wartość liczby kolorowych tokenów dla miejsca.
	 * @param tokensNumber int - nowa liczba tokenów
	 * @param i int - nr porządkowy tokenu, default 0, od 0 do 1
	 */
	public void setColorTokensNumber(int tokensNumber, int i) {
		switch (i) {
			case 1 -> this.token1green = Math.max(tokensNumber, 0);
			case 2 -> this.token2blue = Math.max(tokensNumber, 0);
			case 3 -> this.token3yellow = Math.max(tokensNumber, 0);
			case 4 -> this.token4grey = Math.max(tokensNumber, 0);
			case 5 -> this.token5black = Math.max(tokensNumber, 0);
			default -> this.tokensNumber = Math.max(tokensNumber, 0);
		}
	}

	/**
	 * Metoda pozwala zmienić liczbę tokenów w miejscu, dodając ich określoną wartość.
	 * @param delta (<b>int</b>) wartość, o którą zmieni się liczba tokenów
	 */
	public void modifyTokensNumber(int delta) {
		if((tokensNumber + delta) < 0) {
			this.tokensNumber = 0;
			GUIManager.getDefaultGUIManager().log("Error: something tried to lower tokens below zero! Place: "
					+this.getName(), "error", true);
		} else {
			this.tokensNumber += delta;
		}

	}
	
	/**
	 * Metoda pozwala zmienić liczbę tokenów w miejscu, dodając do niej określoną wartość.
	 * @param delta int - wartość o którą zmieni się liczba kolorowych tokenów
	 * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
	 */
	public void modifyColorTokensNumber(int delta, int i) {
		switch (i) {
			case 1 -> {
				this.token1green += delta;
				if (this.tokensNumber < 0) {
					this.token1green = 0;
				}
			}
			case 2 -> {
				this.token2blue += delta;
				if (this.tokensNumber < 0) {
					this.token2blue = 0;
				}
			}
			case 3 -> {
				this.token3yellow += delta;
				if (this.tokensNumber < 0) {
					this.token3yellow = 0;
				}
			}
			case 4 -> {
				this.token4grey += delta;
				if (this.tokensNumber < 0) {
					this.token4grey = 0;
				}
			}
			case 5 -> {
				this.token5black += delta;
				if (this.tokensNumber < 0) {
					this.token5black = 0;
				}
			}
			default -> {
				this.tokensNumber += delta;
				if (this.tokensNumber < 0) {
					this.tokensNumber = 0;
				}
			}
		}
	}

	/**
	 * Metoda pozwala pobrać liczbę zajętych (zarezerwowanych  przez aktywowaną tranzycję) tokenów.
	 * @return (<b>int</b>) - liczba zarezerwowanych tokenów
	 */
	public int getReservedTokens() {
		return reservedTokens;
	}
	
	/**
	 * Metoda zwraca liczbę zarezerwowanych kolorowych tokenów (0-5)
	 * @param i (<b>int</b>) nr porządkowy tokenu, default 0, od 0 do 5
	 * @return (<b>int</b>) - liczba zarezerwowanych tokenów
	 */
	public int getReservedColorTokens(int i) {
		return switch (i) {
			case 1 -> reserved1green;
			case 2 -> reserved2blue;
			case 3 -> reserved3yellow;
			case 4 -> reserved4grey;
			case 5 -> reserved5black;
			default -> reservedTokens;
		};
	}

	/**
	 * Metoda pozwala zarezerwować określoną liczbę tokenów w miejscu.
	 * @param tokensTaken (int) liczba zajmowanych tokenów
	 */
	public void reserveTokens(int tokensTaken) {
		this.reservedTokens += tokensTaken;
	}
	
	/**
	 * Metoda pozwala zarezerwować określoną liczbę kolorowych tokenów w miejscu.
	 * @param tokensTaken (int) liczba zajmowanych tokenów
	 * @param i (int) nr porządkowy kolorowanego tokeny, dafult 0, od 0 do 5
	 */
	public void reserveColorTokens(int tokensTaken, int i) {
		switch (i) {
			case 1 -> this.reserved1green += tokensTaken;
			case 2 -> this.reserved2blue += tokensTaken;
			case 3 -> this.reserved3yellow += tokensTaken;
			case 4 -> this.reserved4grey += tokensTaken;
			case 5 -> this.reserved5black += tokensTaken;
			default -> this.reservedTokens += tokensTaken;
		}
		
	}

	/**
	 * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny.
	 */
	public void freeReservedTokens() {
		this.reservedTokens = 0;
		this.reserved1green = 0;
		this.reserved2blue = 0;
		this.reserved3yellow = 0;
		this.reserved4grey = 0;
		this.reserved5black = 0;
	}

	
	/**
	 * Metoda zwalnia wszystkie zarezerwowane kolorowe tokeny.
	 * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
	 */
	@SuppressWarnings("unused")
	public void freeReservedColorTokens(int i) {
		switch (i) {
			case 1 -> this.reserved1green = 0;
			case 2 -> this.reserved2blue = 0;
			case 3 -> this.reserved3yellow = 0;
			case 4 -> this.reserved4grey = 0;
			case 5 -> this.reserved5black = 0;
			default -> this.reservedTokens = 0;
		}
		
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
	 * Metoda pobiera zarezerwowane wolne kolorowe tokeny.
	 * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
	 * @return int - liczba dostępnych tokenów
	 */
	public int getNonReservedColorTokensNumber(int i) {
		return switch (i) {
			case 0 -> tokensNumber - getReservedColorTokens(0);
			case 1 -> token1green - getReservedColorTokens(1);
			case 2 -> token2blue - getReservedColorTokens(2);
			case 3 -> token3yellow - getReservedColorTokens(3);
			case 4 -> token4grey - getReservedColorTokens(4);
			case 5 -> token5black - getReservedColorTokens(5);
			default -> tokensNumber - getReservedTokens();
		};
		
	}
	
	/**
	 * Metoda zamieniająca dane o miejscu sieci na łańcuch znaków.
	 * @return (<b>String</b>) - łańcuch znaków reprezentujący miejsca.
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
