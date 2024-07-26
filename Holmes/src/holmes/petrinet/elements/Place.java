package holmes.petrinet.elements;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.containers.PlaceGraphicsContainer;
import holmes.petrinet.elements.containers.PlaceQSimContainer;

/**
 * Klasa implementująca miejsce sieci Petriego. Zapewnia implementację stanu (przechowywania tokenów) oraz 
 * powiązane z tym funkcjonalności (funkcjonalność wierzchołka dziedziczy po klasie Node).
 */
public class Place extends Node {
	@Serial
	private static final long serialVersionUID = 2346995422046987174L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	/** PN, CPN, XTPN */
	public enum PlaceType {PN, CPN, XTPN}
	protected PlaceType placeType = PlaceType.PN; //default
	protected static int realRadius = 18;
	protected int tokensNumber = 0;
	protected int reservedTokens = 0;

	public PlaceGraphicsContainer drawGraphBoxP = new PlaceGraphicsContainer();
	public PlaceQSimContainer qSimBoxP = new PlaceQSimContainer();

	public boolean isColored = false;
	protected boolean isXTPN = false; //czy tokeny marzą o elektrycznych tranzycjach?


	//SSA:
	protected double ssaValue = 0.0;
	protected boolean isConcentration = false;

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
	}

	/**
	 * Metoda rysująca miejsce na danym arkuszu.
	 * @param g (<b>Graphics2D</b>) obiekt grafiki.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @param eds (<b>ElementDrawSettings</b>) opcje rysowania.
	 */
	public void draw(Graphics2D g, int sheetId, ElementDrawSettings eds)
	{
		ElementDraw.drawElement(this, g, sheetId, eds);
	}
	
	/**
	 * Zwraca zbiór tranzycji wejściowych *p.
	 * @return (<b>ArrayList[Transition]</b>) - lista tranzycji ze zbioru *p.
	 */
	public ArrayList<Transition> getInputTransitions() {
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
	 * @return (<b>ArrayList[Transition]</b>) - lista tranzycji ze zbioru p*.
	 */
	public ArrayList<Transition> getOutputTransitions() {
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
	 * @return (<b>int</b>) - liczba tokenów.
	 */
	public int getTokensNumber() {
		return tokensNumber;
	}

	/**
	 * Metoda pozwala ustawić wartość liczby tokenów dla miejsca.
	 * @param tokensNumber (<b>int</b>) nowa liczba tokenów.
	 */
	public void setTokensNumber(int tokensNumber) {
		this.tokensNumber = tokensNumber;
		if(tokensNumber < 0) {
			String strB = String.format(lang.getText("LOGentry00389"), this.getName(), this.getTokensNumber());
			overlord.log(strB, "error", true);
		}
	}

	/**
	 * Metoda pozwala zmienić liczbę tokenów w miejscu, dodając ich określoną wartość.
	 * @param delta (<b>int</b>) wartość, o którą zmieni się liczba tokenów.
	 */
	public void addTokensNumber(int delta) {
		if((tokensNumber + delta) < 0) {
			this.tokensNumber = 0;

			overlord.log(lang.getText("LOGentry00390")+" "
					+this.getName(), "error", true);
		} else {
			this.tokensNumber += delta;
		}

	}

	/**
	 * Metoda pozwala pobrać liczbę zajętych (zarezerwowanych  przez aktywowaną tranzycję) tokenów.
	 * @return (<b>int</b>) - liczba zarezerwowanych tokenów.
	 */
	public int getReservedTokens() {
		return reservedTokens;
	}

	/**
	 * Metoda pozwala zarezerwować określoną liczbę tokenów w miejscu.
	 * @param tokensTaken (<b>int</b>) liczba zajmowanych tokenów.
	 */
	public void reserveTokens(int tokensTaken) {
		this.reservedTokens += tokensTaken;
	}

	/**
	 * Metoda pozwala pobrać liczbę wolnych (dostępnych, nie 
	 * zarezerwowanych przez żadną tranzycję) tokenów.
	 * @return (<b>int</b>) - liczba dostępnych tokenów.
	 */
	public int getNonReservedTokensNumber() {
		return tokensNumber - getReservedTokens();
	}

	/**
	 * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny.
	 */
	public void freeReservedTokens() {
		this.reservedTokens = 0;
	}

	/**
	 * Metoda zwraca typ miejsca.
	 * @return (<b>PlaceType</b>) typ miejsca: PN, CPN, XTPN.
	 */
	public PlaceType getPlaceType() {
		return this.placeType;
	}

	/**
	 * Metoda ustawia typ miejsca.
	 * @param value (<b>PlaceType</b>) typ miejsca: PN, CPN, XTPN.
	 */
	public void setPlaceType(PlaceType value) {
		this.placeType = value;
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
			return "(P" + overlord.getWorkspace().getProject().getPlaces().indexOf(this)+")";
		}
	}
	
	//****************************************************************************************************************************
	//********************************************      SSA       ****************************************************************
	//****************************************************************************************************************************
	
	/**
	 * Ustawia nową wartość cząstek dla miejsca w symulacji SSA.
	 * @param value (<b>double</b>) nowa wartość.
	 */
	public void setSSAvalue(double value) {
		this.ssaValue = value;
	}
	
	/**
	 * Zwraca aktualną wartość cząstek dla miejsca w symulacji SSA.
	 * @return (<b>double</b>) - liczba cząstek
	 */
	public double getSSAvalue() {
		return this.ssaValue;
	}

	/**
	 * Ustawia status dla ssaValue - czy mole czy koncentracja na litr
	 * @param value (<b>double</b>) nowa wartość.
	 */
	public void setSSAconcentrationStatus(boolean value) {
		this.isConcentration = value;
	}

	/**
	 * Zwraca aktualny status dla ssaValue - czy mole czy koncentracja na litr.
	 * @return (<b>double</b>) - liczba cząstek
	 */
	public boolean isSSAconcentration() {
		return this.isConcentration;
	}
}
