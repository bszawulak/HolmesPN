package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.functions.FunctionContainer;

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
	private static final long serialVersionUID = 2346995422046987174L;
	protected static int realRadius = 18;
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
	
	//quickSim
	public boolean qSimDrawed = false; //czy rysować dodatkowe oznaczenie miejsca - okrąg
	public int qSimOvalSize = 10; //rozmiar okręgu oznaczającego
	public Color qSimOvalColor = Color.RED;
	public Color qSimFillColor = Color.WHITE; //kolor oznaczenia
	public boolean qSimDrawStats = false; //czy rysować dodatkowe dane statystyczne
	public int qSimFillValue = 0; //poziom wypełnienia danychy
	public double qSimTokens = 0; //ile średnio tokenów w symulacji
	public String qSimText = ""; //dodatkowy tekst
	
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

	/*  ***********************************************************************************
	 ********************************    xTPN    ***************************************
	 ***********************************************************************************  */

	private double gammaL_xTPN = 0.0;
	private double gammaU_xTPN = Double.MAX_VALUE - 1;

	//grafika:
	private boolean showTokenSet_xTPN = false; //czy wyświetlać zbiór tokenów
	private ArrayList<Double> multisetK;
	
	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param sheetId int - identyfikator arkusza
	 * @param placePosition Point - punkt lokalizacji
	 */
	public Place(int nodeId, int sheetId, Point placePosition) {
		super(sheetId, nodeId, placePosition, realRadius);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);

		this.multisetK = new ArrayList<Double>();
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
		super(nodeId, elementLocations, realRadius);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);

		this.multisetK = new ArrayList<Double>();
	}

	/**
	 * Konstruktor obiektu miejsca sieci - tworzenie portali.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations) {
		super(nodeId, elementLocations, realRadius);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);

		this.multisetK = new ArrayList<Double>();
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
	 * Zwraca zbiór tranzycji wejściowych *p.
	 * @return ArrayList[Transition] - lista tranzycji ze zbioru *p
	 */
	public ArrayList<Transition> getPreTransitions() {
		ArrayList<Transition> preTransitions = new ArrayList<Transition>();
		for(ElementLocation el : getElementLocations()) {
			for(Arc arc : el.getInArcs()) {
				Node n = arc.getStartNode();
				if(!preTransitions.contains(n)) {
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
		ArrayList<Transition> postTransitions = new ArrayList<Transition>();
		for(ElementLocation el : getElementLocations()) {
			for(Arc arc : el.getOutArcs()) {
				Node n = arc.getEndNode();
				if(!postTransitions.contains(n)) {
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
		switch(i) {
			case 0:
				return tokensNumber;
			case 1:
				return token1green;
			case 2:
				return token2blue;
			case 3:
				return token3yellow;
			case 4:
				return token4grey;
			case 5:
				return token5black;
			default:
				return tokensNumber;
		}
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
		switch(i) {
			case 0:
				this.tokensNumber = tokensNumber;
				if(tokensNumber < 0) {
					this.tokensNumber = 0;
				}
				break;
			case 1:
				this.token1green = tokensNumber;
				if(tokensNumber < 0) {
					this.token1green = 0;
				}
				break;
			case 2:
				this.token2blue = tokensNumber;
				if(tokensNumber < 0) {
					this.token2blue = 0;
				}
				break;
			case 3:
				this.token3yellow = tokensNumber;
				if(tokensNumber < 0) {
					this.token3yellow = 0;
				}
				break;
			case 4:
				this.token4grey = tokensNumber;
				if(tokensNumber < 0) {
					this.token4grey = 0;
				}
				break;
			case 5:
				this.token5black = tokensNumber;
				if(tokensNumber < 0) {
					this.token5black = 0;
				}
				break;
			default:
				this.tokensNumber = tokensNumber;
				if(tokensNumber < 0) {
					this.tokensNumber = 0;
				}
		}
	}

	/**
	 * Metoda pozwala zmienić liczbę tokenów w miejscu, dodając do niej określoną wartość.
	 * @param delta int - wartość o którą zmieni się liczba tokenów
	 */
	public void modifyTokensNumber(int delta) {
		this.tokensNumber += delta;
		
		if(tokensNumber < 0) {
			this.tokensNumber = 0;
			GUIManager.getDefaultGUIManager().log("Simulation error: number of tokens in place: "
					+this.getName()+ " below zero: ("+this.getTokensNumber()+").", "error", true);
		}
	}
	
	/**
	 * Metoda pozwala zmienić liczbę tokenów w miejscu, dodając do niej określoną wartość.
	 * @param delta int - wartość o którą zmieni się liczba kolorowych tokenów
	 * @param i int - nr porządkowy tokenu, default 0, od 0 do 5
	 */
	public void modifyColorTokensNumber(int delta, int i) {
		switch(i) {
			case 0:
				this.tokensNumber += delta;
				if(this.tokensNumber < 0) {
					this.tokensNumber = 0;
				}
				break;
			case 1:
				this.token1green += delta;
				if(this.tokensNumber < 0) {
					this.token1green = 0;
				}
				break;
			case 2:
				this.token2blue += delta;
				if(this.tokensNumber < 0) {
					this.token2blue = 0;
				}
				break;
			case 3:
				this.token3yellow += delta;
				if(this.tokensNumber < 0) {
					this.token3yellow = 0;
				}
				break;
			case 4:
				this.token4grey += delta;
				if(this.tokensNumber < 0) {
					this.token4grey = 0;
				}
				break;
			case 5:
				this.token5black += delta;
				if(this.tokensNumber < 0) {
					this.token5black = 0;
				}
				break;
			default:
				this.tokensNumber += delta;
				if(this.tokensNumber < 0) {
					this.tokensNumber = 0;
				}
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
	 * Metoda zwraca liczbę zarezerwowanych kolorowych tokenów (0-5)
	 * @param i - nr porzadkowy tokenu, default 0, od 0 do 5
	 * @return int - liczba zarezerwowanych tokenów
	 */
	public int getReservedColorTokens(int i) {
		switch(i) {
			case 0:
				return reservedTokens;
			case 1:
				return reserved1green;
			case 2:
				return reserved2blue;
			case 3:
				return reserved3yellow;
			case 4:
				return reserved4grey;
			case 5:
				return reserved5black;
			default:
				return reservedTokens;
		}
	}

	/**
	 * Metoda pozwala zarezerwować określoną liczbę tokenów w miejscu.
	 * @param tokensTaken int - liczba zajmowanych tokenów
	 */
	public void reserveTokens(int tokensTaken) {
		this.reservedTokens += tokensTaken;
	}
	
	/**
	 * Metoda pozwala zarezerwować określoną liczbę kolorowych tokenów w miejscu.
	 * @param tokensTaken int - liczba zajmowanych tokenów
	 * @param i - nr porządkowy kolorowanego tokeny, dafult 0, od 0 do 5
	 */
	public void reserveColorTokens(int tokensTaken, int i) {
		switch(i) {
			case 0:
				this.reservedTokens += tokensTaken;
				break;
			case 1:
				this.reserved1green += tokensTaken;
				break;
			case 2:
				this.reserved2blue += tokensTaken;
				break;
			case 3:
				this.reserved3yellow += tokensTaken;
				break;
			case 4:
				this.reserved4grey += tokensTaken;
				break;
			case 5:
				this.reserved5black += tokensTaken;
				break;
			default:
				this.reservedTokens += tokensTaken;
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
	public void freeReservedColorTokens(int i) {
		switch(i) {
			case 0:
				this.reservedTokens = 0;
				break;
			case 1:
				this.reserved1green = 0;
				break;
			case 2:
				this.reserved2blue = 0;
				break;
			case 3:
				this.reserved3yellow = 0;
				break;
			case 4:
				this.reserved4grey = 0;
				break;
			case 5:
				this.reserved5black = 0;
				break;
			default:
				this.reservedTokens = 0;
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
		switch(i) {
			case 0:
				return tokensNumber - getReservedColorTokens(0);
			case 1:
				return token1green - getReservedColorTokens(1);
			case 2:
				return token2blue - getReservedColorTokens(2);
			case 3:
				return token3yellow - getReservedColorTokens(3);
			case 4:
				return token4grey - getReservedColorTokens(4);
			case 5:
				return token5black - getReservedColorTokens(5);
			default:
				return tokensNumber - getReservedTokens();
		}
		
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

	/* Uprzejmie prosi się o nie pisane żadnego kodu niezwiązanego z xTPN POD tym komentarzem
	 **************************************************************************************
	 *********************************        xTPN      ***********************************
	 **************************************************************************************
	 */

	/**
	 * Metoda ustawia dolną wartość gammaLower dla xTPN.
	 * @param value (double) czas gammaL (=minimalny czas aktywacji.)
	 * @param force (boolean) czy wymusić wartość bez weryfikacji
	 */
	public void setGammaL_xTPN(double value, boolean force) {
		if(force) {
			this.gammaL_xTPN = value;
			return;
		}
		if (value < 0) {
			this.gammaL_xTPN = 0.0;
			return;
		}
		if (value > gammaU_xTPN) { //musi być mniejszy równy niż gammaU
			this.gammaL_xTPN = gammaU_xTPN;
			return;
		}
		this.gammaL_xTPN = value;
	}

	/**
	 * Metoda pozwala odczytać dolną wartość gammaLower dla xTPN.
	 * @return (double) : czas gammaLower, minimalny czas aktywacji.
	 */
	public double getGammaL_xTPN() {
		return this.gammaL_xTPN;
	}

	/**
	 * Metoda ustawia dolną wartość gammaUpper dla xTPN.
	 * @param value (double) czas gammaU (=token lifetime limit)
	 * @param force (boolean) czy wymusić wartość bez weryfikacji
	 */
	public void setGammaU_xTPN(double value, boolean force) {
		if(force) {
			this.gammaU_xTPN = value;
			return;
		}
		if (value < 0) {
			this.gammaU_xTPN = -1.0; //domyślnie do redukcji -> classical Place
			return;
		}
		if (value < gammaL_xTPN) { //musi być większy równy niż gammaL
			this.gammaU_xTPN = gammaL_xTPN;
			return;
		}
		this.gammaU_xTPN = value;
	}

	/**
	 * Metoda pozwala odczytać górną wartość gammaUpper dla xTPN.
	 * @return (double) : czas gammaUpper.
	 */
	public double getGammaU_xTPN() {
		return this.gammaU_xTPN;
	}
}
