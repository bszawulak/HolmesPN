package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;

import javax.swing.*;

import static java.lang.Double.valueOf;

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

	/* ***********************************************************************************
	   ********************************    xTPN    ***************************************
	   *********************************************************************************** */
	private boolean isXTPN = false; //czy tokeny marzą o elektrycznych tranzycjach?
	private double gammaMin_xTPN = 0.0;
	private double gammaMax_xTPN = 99;
	private boolean gammaMode_xTPN = true;

	//grafika:
	private boolean showTokenSet_xTPN = false; //czy wyświetlać zbiór tokenów

	private boolean gammaRangeVisibility_XTPN = true;
	private int franctionDigits = 6;

	private double accuracyLevel = 0.000000001;

	//tokeny:
	private ArrayList<Double> multisetK;
	private ArrayList<Double> reservedMultisetK;

	//private int numberOfTokens_XTPN = 0;
	//private int numberOfReservTokens_XTPN = 0;



	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param sheetId int - identyfikator arkusza
	 * @param placePosition Point - punkt lokalizacji
	 */
	public Place(int nodeId, int sheetId, Point placePosition) {
		super(sheetId, nodeId, placePosition, realRadius);
		this.setName("Place" + IdGenerator.getNextPlaceId());
		this.setType(PetriNetElementType.PLACE);

		this.multisetK = new ArrayList<>();
		this.reservedMultisetK = new ArrayList<>();
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

		this.multisetK = new ArrayList<>();
		this.reservedMultisetK = new ArrayList<>();
	}

	/**
	 * Konstruktor obiektu miejsca sieci - tworzenie portali.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations) {
		super(nodeId, elementLocations, realRadius);
		this.setName("Place" + IdGenerator.getNextPlaceId());
		this.setType(PetriNetElementType.PLACE);

		this.multisetK = new ArrayList<>();
		this.reservedMultisetK = new ArrayList<>();
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
	 * @param delta (<b>int</b>) wartość o którą zmieni się liczba tokenów
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
	 * Metoda ustawia dolną wartość gammaMinimum dla xTPN.
	 * @param value (double) czas gammaMinimum (=minimalny czas aktywacji.)
	 * @param force (boolean) czy wymusić wartość bez weryfikacji
	 */
	public void setGammaMin_xTPN(double value, boolean force) {
		if(force) {
			this.gammaMin_xTPN = value;
			return;
		}
		if (value < 0) {
			this.gammaMin_xTPN = 0.0;
			return;
		}
		if (value > gammaMax_xTPN) { //musi być mniejszy równy niż gammaU
			this.gammaMin_xTPN = gammaMax_xTPN;
			return;
		}
		this.gammaMin_xTPN = value;
	}

	/**
	 * Metoda pozwala odczytać dolną wartość gammaMinimum dla xTPN.
	 * @return (double) czas gammaMinimum, minimalny czas aktywacji.
	 */
	public double getGammaMin_xTPN() {
		return this.gammaMin_xTPN;
	}

	/**
	 * Metoda ustawia dolną wartość gammaMaximum dla xTPN.
	 * @param value (double) czas gammaMaximum (=token lifetime limit)
	 * @param force (boolean) czy wymusić wartość bez weryfikacji
	 */
	public void setGammaMax_xTPN(double value, boolean force) {
		if(value > Integer.MAX_VALUE)
			value = Integer.MAX_VALUE - 1;

		if(force) {
			this.gammaMax_xTPN = value;
			return;
		}
		if (value < 0) {
			this.gammaMax_xTPN = -1.0; //domyślnie do redukcji -> classical Place
			return;
		}
		if (value < gammaMin_xTPN) { //musi być większy równy niż gammaL
			this.gammaMax_xTPN = gammaMin_xTPN;
			return;
		}
		this.gammaMax_xTPN = value;
	}

	/**
	 * Metoda pozwala odczytać górną wartość gammaUpper dla xTPN.
	 * @return (double) czas gammaUpper.
	 */
	public double getGammaMax_xTPN() {
		return this.gammaMax_xTPN;
	}

	/**
	 * Metoda włącza status miejsca typu XTPN.
	 * @param status (boolean) true, jeśli tryb XTPN ma być aktywny dla miejsca.
	 */
	public void setXTPNplaceStatus(boolean status) {
		isXTPN = status;
	}

	/**
	 * Metoda zwraca status XTPN dla miejsca.
	 * @return (boolean) - true, jeśli to miejsce typu XTPN
	 */
	public boolean isXTPNplace() {
		return isXTPN;
	}

	/**
	 * Metoda włącza tryb gamma-XTPN dla miejsca.
	 * @param status (boolean) true, jeśli tryb gamma-XTPN ma być aktywny
	 */
	public void setGammaModeXTPNstatus(boolean status) {
		gammaMode_xTPN = status;
		setGammaRangeStatus(status);
	}

	/**
	 * Metoda zwraca status gamma miejsca XTPN.
	 * @return (boolean) - true, jeśli status gamma-XTPN miejsca
	 */
	public boolean isGammaModeActiveXTPN() {
		return gammaMode_xTPN;
	}

	/**
	 * Metoda ustawia status zakresów gamma - pokazywać czy nie.
	 * @param status (<b>boolean</b>) true, jeśli zakresy gamma mają być pokazywane.
	 */
	public void setGammaRangeStatus(boolean status) {
		gammaRangeVisibility_XTPN = status;
	}

	/**
	 * Metoda zwraca status zakresów gamma - pokazywać czy nie.
	 * @return (<b>boolean</b>) - true, jeśli zakresy gamma mają być pokazywane.
	 */
	public boolean isGammaRangeVisible() {
		return gammaRangeVisibility_XTPN;
	}

	/**
	 * Metoda ustawia wyświetlaną dokładność po przecinku.
	 * @param value (int) nowa wartość liczby cyfr przecinku.
	 */
	public void setFraction_xTPN(int value) {
		franctionDigits = value;
	}

	/**
	 * Metoda zwraca wyświetlaną dokładność po przecinku.
	 * @return (int) aktualna wartość liczby cyfr przecinku.
	 */
	public int getFraction_xTPN() {
		return franctionDigits;
	}

	/**
	 * Dodawanie nowych tokenów do multizbioru K.
	 * @param howMany (int) ile tokenów dodać
	 * @param initialTime (double) wartość początkowa
	 */
	public void addTokens_XTPN(int howMany, double initialTime) {
		if(isGammaModeActiveXTPN()) { //tylko gdy XTPN włączone
			for (int i = 0; i < howMany; i++) {
				multisetK.add(valueOf(initialTime));
			}
		}
		modifyTokensNumber(howMany);

		if(initialTime > 0) {
			Collections.sort(multisetK);
			Collections.reverse(multisetK);
		}
	}

	/**
	 * Usuwa tokeny których czas życia jest większy GammaMax.
	 * @return int - liczba usuniętych tokenów
	 */
	public int removeOldTokens_XTPN() {
		int removed = 0;
		if(isGammaModeActiveXTPN()) { //tylko gdy XTPN włączone
			for (Double token : multisetK) {
				if (token + accuracyLevel > gammaMax_xTPN) {
					multisetK.remove(token);
					removed++;
				}
			}
		}
		modifyTokensNumber(-removed);
		return removed;
	}

	/**
	 * Usuwa tokeny na potrzeby produkcji tranzycji XTPN.
	 * @param howMany (int) - ile usunąć
	 * @param mode (int) tryb: 0 - najstarsze, 1 - najmłodsze, 2 - losowe
	 * @param genetaror (Random) generator dla mode=2
	 * @return (int) - liczba usuniętych tokenów lub -1 gdy wystąpił błąd
	 */
	public int removeTokensForProduction(int howMany, int mode, Random genetaror) {
		if(!isGammaModeActiveXTPN()) { //gdy XTPN wyłączone, tylko usuwamy liczbę
			modifyTokensNumber(-howMany);
			return howMany;
		}

		int counter = howMany;
		if(howMany > multisetK.size()) {
			return -1;
		}

		if(mode == 0) { //najstarsze
			for(Double token : multisetK) {
				multisetK.remove(token);
				counter--;

				if(counter == 0)
					break;
			}
		} else if (mode == 1) { //najmłodsze
			Collections.reverse(multisetK);
			for(Double token : multisetK) {
				multisetK.remove(token);
				counter--;

				if(counter == 0) {
					Collections.reverse(multisetK);
					break;
				}
			}
		} else { //losowo
			for(int i=0; i<howMany; i++) {
				int index = genetaror.nextInt(multisetK.size());
				multisetK.remove(index);
			}
		}
		modifyTokensNumber(-howMany);
		return howMany;
	}

	/**
	 * Zwiększanie czasu życia wszystkich tokenów na liście.
	 * @param tau (double) o ile zwiększyć czas życia tokenów.
	 */
	public void incTokensTime_XTPN(double tau) {
		if(isGammaModeActiveXTPN()) {
			multisetK.replaceAll(aDouble -> aDouble + tau);
		} else {
			JOptionPane.showMessageDialog(null, "Critical error - tokens time update when XTPN status OFF" +
							"\nfor place"+this.getName(),
					"Error 587654", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Aktualizacja wartości czasowej tokenu. Potem sortowanie multizbioru.
	 * @param ID (<b>ID</b>) indeks tokenu.
	 * @param value (<b>value</b>) nowa wartość tokenu.
	 * @return (<b>boolean</b>) - true jeśli się udało.
	 */
	public boolean updateToken(int ID, Double value) {
		boolean status = false;
		if(ID > -1 && ID < multisetK.size()) {
			multisetK.set(ID, value);
		}
		Collections.sort(multisetK);
		Collections.reverse(multisetK);
		return status;
	}

	/**
	 * Usuwanie tokenu po ID.
	 * @param id (<b>int</b>) indeks tokenu.
	 */
	public void removeTokenByID(int id) {
		if(ID > -1 && ID < multisetK.size()) {
			multisetK.remove(id);
		}
		modifyTokensNumber(-1);
		//Collections.sort(multisetK);
		//Collections.reverse(multisetK);
	}

	/**
	 * Metoda umożliwia dostęp do multizbioru K tokenów.
	 * @return (<b>ArrayList[Double]</b>) - multizbiór K miejsca XTPN.
	 */
	public ArrayList<Double> accessMultiset() {
		return multisetK;
	}

	/**
	 * Podmienia multizbiór na nowy (np. przy zmianie stanu na jeden z przechowywanych).
	 * @param newMultiset (<b>ArrayList[Double]</b>) nowy multizbiór.
	 */
	public void replaceMultiset(ArrayList<Double> newMultiset) {
		multisetK = newMultiset;
		reservedMultisetK.clear(); // ?
	}

	/**
	 * Metoda kasuje multizbiór K, pozostawia tylko liczbę tokenów jako int.
	 */
	public void transformXTPNintoPNpace() {
		setGammaModeXTPNstatus(false);
		multisetK.clear();
	}

	/**
	 * Na podstawie liczby tokenów metoda wypełnia zerami nowy multizbiór K.
	 */
	public void transformIntoXTPNplace() {
		setGammaModeXTPNstatus(true);
		for(int i=0; i<tokensNumber; i++) {
			multisetK.add(valueOf(0.0));
		}
	}
}
