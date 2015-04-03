package abyss.math;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import abyss.graphpanel.ElementDraw;
import abyss.graphpanel.IdGenerator;

/**
 * Klasa implementująca tranzycję w sieci Petriego. Zapewnia implementację
 * szeregu funkcjonalności powiązanych z aktywacją i odpalaniem tranzycji
 * na potrzeby symulacji dynamiki sieci Petriego, jak rezerwowanie tokenów
 * i wykrywanie aktywacji.
 * @author students
 * @author MR - poprawki, obsługa klastrów, inne
 *
 */
public class Transition extends Node {
	//BACKUP:  2673581001465115432L  ((NIE DOTYKAĆ PONIŻSZEJ ZMIENNEJ!)
	private static final long serialVersionUID = 2673581001465115432L;
	
	protected boolean isLaunching;
	protected boolean isGlowedINV = false;
	protected boolean isGlowedMTC = false;
	protected boolean offline = false;		// czy wyłączona (MCS, inne)
	
	protected boolean isColorChanged = false;		//zmiana koloru - status
	protected double transNumericalValue = 0.0;		//dodatkowa liczba do wyświetlenia
	protected Color transColorValue = new Color(255,255,255);
	//TODO:
	protected boolean showIntOnly = false;
	protected boolean valueVisibilityStatus = false;
	
	protected int firingValueInInvariant = 0; // ile razy uruchomiona w ramach niezmiennika

	//TODO: tak nie może być, to poniżej jest używane przez funkcję generującą MCT, ale ostatnią rzeczą
	//jaką obiekt klasy Transition potrzebuje, to kolejny wielki wektor danych...
	private ArrayList<ArrayList<Transition>> containingInvariants = new ArrayList<ArrayList<Transition>>();
	

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji
	 * @param pn int - promien okręgu na którym opisana jest figura geometryczna
	 */
	public Transition(int transitionId, ArrayList<ElementLocation> elementLocations, int pn) {
		super(transitionId, elementLocations, pn);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param sheetId int - identyfikator arkusza
	 * @param transitionId int - identyfikator tranzycji
	 * @param transitionPosition Point - punkt, w którym znajduje się lokalizacja wierzchołka
	 * @param pn int - promien okręgu na którym opisana jest figura geometryczna
	 */
	public Transition(int sheetId, int transitionId, Point transitionPosition, int pn) {
		super(sheetId, transitionId, transitionPosition, pn);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocation ElementLocation - lokalizacja elementu sieci
	 * @param pn int - promień okręgu na którym opisana jest figura geometryczna
	 */
	public Transition(int transitionId, ElementLocation elementLocation, int pn) {
		super(transitionId, elementLocation, pn);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param sheetId int - identyfikator arkusza
	 * @param transitionPosition Point - punkt lokalizacji
	 * @param name String - nazwa tranzycji
	 * @param comment String - komentarz tranzycji
	 */
	public Transition(int transitionId, int sheetId, Point transitionPosition, String name, String comment) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocation ElementLocation - lokalizacja tranzycji
	 * @param name String - nazwa tranzycji
	 * @param comment String - komentarz tranzycji
	 */
	public Transition(int transitionId, ElementLocation elementLocation, String name, String comment) {
		super(transitionId, elementLocation, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci. Używany do wczytywania sieci zewnętrznej, np. ze Snoopy
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
	 * @param name String - nazwa tranzycji
	 * @param comment String - komentarz tranzycji
	 */
	public Transition(int transitionId, ArrayList<ElementLocation> elementLocations, String name, String comment) {
		super(transitionId, elementLocations, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci. Używany przez procedury tworzenia portali.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
	 */
	public Transition(int transitionId, ArrayList<ElementLocation> elementLocations) {
		super(transitionId, elementLocations, 15);
		this.setName("Transition" + Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param sheetId int - identyfikator arkusza
	 * @param transitionPosition Point - punkt lokalizacji tranzycji
	 */
	public Transition(int transitionId, int sheetId, Point transitionPosition) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName("Transition" + Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TRANSITION);
	}

	/**
	 * Metoda rysująca tranzycję na danym arkuszu.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza 
	 */
	public void draw(Graphics2D g, int sheetId) {
		g = ElementDraw.drawElement(this, g, sheetId);
		//super.draw(g, sheetId);
	}

	/**
	 * Metoda pozwala pobrać łączną liczbę dostępnych tokenów ze wszystkich miejsc wejściowych.
	 * @return int - liczba dostępnych tokenów z pola availableTokens.
	 */
	public int getAvailableTokens() {
		int availableTokens = 0;
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			availableTokens += origin.getTokensNumber();
		}
		return availableTokens;
	}

	/**
	 * Metoda pozwala pobrać łączna liczbę tokenów niezbędnych do aktywacji tej tranzycji.
	 * @return int - liczba tokenów potrzebnych do aktywacji z pola requiredTokens
	 */
	public int getRequiredTokens() {
		int requiredTokens = 0;
		for (Arc arc : getInArcs()) {
			requiredTokens += arc.getWeight();
		}
		return requiredTokens;
	}

	/**
	 * Metoda pozwala sprawdzić, czy tranzycja jest aktywna i może zostać odpalona.
	 * @return boolean - true, jeśli tranzycja jest aktywna i może zostać odpalona; false w przeciwnym wypadku
	 */
	public boolean isActive() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			if (!(origin.getFreeTokensNumber() >= arc.getWeight()))
				return false;
		}
		return true;
	}

	/**
	 * Metoda informująca, czy tranzycja jest podświetlona kolorem
	 * @return boolean - true jeśli świeci; false w przeciwnym wypadku
	 */
	public boolean isGlowed() {
		return isGlowedINV;
	}

	/**
	 * Metoda pozwala określić, czy tranzycja ma byc podświetlona oraz ile razy
	 * występuje ona w ramach niezmiennika.
	 * @param isGlowed boolean - true, jeśli ma świecić
	 * @param numericalValueShowed int - liczba uruchomień tranzycji w niezmienniku
	 */
	public void setGlowedINV(boolean isGlowed, int numericalValueShowed) {
		this.isGlowedINV = isGlowed;
		this.firingValueInInvariant = numericalValueShowed;
	}

	/**
	 * Metoda pozwala określic, czy tranzycja ma byc podświetlona.
	 * @param isGlowedINV boolean - true, jeśli ma świecić
	 */
	public void isGlowed_INV(boolean value) {
		this.isGlowedINV = value;
	}

	/**
	 * Metoda sprawdza, czy tranzycja świeci będąc częcią zbioru MCT.
	 * @return boolean - true jeżeli świeci jako MCT; false w przeciwnym wypadku
	 */
	public boolean isGlowed_MTC() {
		return isGlowedMTC;
	}

	/**
	 * Metoda ustawia stan świecenia tranzycji jako częci MCT.
	 * @param isGlowedMTC boolean - true jeżeli ma świecić
	 */
	public void setGlowed_MTC(boolean value) {
		this.isGlowedMTC = value;
	}
	
	
	/**
	 * Metoda informuje, czy tramzycja ma być rysowana z innym kolorem wypełnienia
	 * @return boolean - true, jeśli ma mieć inny kolor niż domyślny
	 */
	public boolean isColorChanged() {
		return isColorChanged;
	}
	
	//TODO:
	/**
	 * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
	 * @param isColorChanged boolean - true, jeśli ma rysować się w kolorze
	 * @param newColor Color - na jaki kolor
	 * @param showNumber boolean - true, jeśli liczba ma się wyświetlać
	 * @param double clNumber - liczba do wyświetlenia
	 */
	public void setColorWithNumber(boolean isColorChanged, Color newColor, boolean showNumber, double numberShowed) {
		this.isColorChanged = isColorChanged;
		this.transColorValue = newColor;
		setNumericalValueVisibility(showNumber);
		this.transNumericalValue = numberShowed;
	}
	
	/**
	 * Metoda ustawia, czy liczba ma się wyświetlać obok tranzycji.
	 * @param status boolean - true, jeśli ma się wyświetlać pewna dodatkowa wartość obok rysunku tranzycji
	 */
	public void setNumericalValueVisibility(boolean status) {
		valueVisibilityStatus = status;
	}
	
	/**
	 * Metoda informuje, czy ma się wyświetlać dodatkowa wartośc liczbowa obok rysunku tranzycji.
	 * @return boolean - true, jeśli ma się wyświetlać
	 */
	public boolean getNumericalValueVisibility() {
		return valueVisibilityStatus;
	}
	
	/**
	 * Zwraca liczbę która ma się wyświetlać obok kwadratu tranzycji.
	 * @return double - liczba
	 */
	public double getNumericalValueDOUBLE() {
		return transNumericalValue;
	}
	
	/**
	 * Metoda zwraca aktualnie ustawiony kolor dla tranzycji
	 * @return Color - kolor
	 */
	public Color getTransitionNewColor() {
		return transColorValue;
	}
	
	/**
	 * Metoda zwraca liczbę wystąpień uruchomień tranzycji w ramach niezmiennika.
	 * @return int - liczba wystąpień uruchomień tranzycji w niezmienniku z pola firingNumber
	 */
	public int getFiring_INV() {
		return this.firingValueInInvariant;
	}

	/**
	 * Metoda pozwala ustawić, czy tranzycja jest teraz uruchamiana.
	 * @param isLunching boolean - true, jeśeli tranzycja jest właśnie uruchamiana;
	 * 		false w przeciwnym wypadku
	 */
	public void setLaunching(boolean isLaunching) {
		this.isLaunching = isLaunching;
	}

	/**
	 * Metoda pozwala sprawdzić, czy tranzycja jest w tej chwili odpalana.
	 * @return boolean - true, jeśli tranzycja jest aktualnie odpalana; false w przeciwnym wypadku
	 */
	public boolean isLaunching() {
		return isLaunching;
	}
	
	/**
	 * Metoda ustawia status wyłączenia tranzycji w symulatorze.
	 * @param status boolean - true, jeśli ma być wyłączona
	 */
	public void setOffline(boolean status) {
		offline = status;
	}
	
	/**
	 * Metoda zwraca status aktywności tranzycji.
	 * @return boolean - true, jeśli tranzycja jest wyłączona (MCS)
	 */
	public boolean isOffline() {
		return offline;
	}
	
	/**
	 * Metoda pozwala zarezerwować we wszystkich miejscach wejściowych
	 * niezbędne do uruchomienia tokeny. Inne tranzycje nie mogą ich odebrać.
	 */
	public void bookRequiredTokens() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			origin.bookTokens(arc.getWeight());
		}
	}

	/**
	 * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny we wszystkich
	 * miejscach wejściowych. Stają się one dostępne dla innych tranzycji.
	 */
	public void returnBookedTokens() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			origin.returnTokens();
		}
	}

	/**
	 * Metoda zwraca typ tranzycji jako elementu klasycznej PN.
	 * @return PetriNetElementType - tranzycja klasyczna
	 */
	public PetriNetElementType getType() {
		return PetriNetElementType.TRANSITION;
	}

	/**
	 * Metoda zwraca wagę łuku wyjściowego do wskazanego miejsca.
	 * @param outPlace Place - miejsce połączone z daną tranzycją (od niej)
	 * @return int - waga łuku łączącego
	 */
	public int getOutArcWeightTo(Place outPlace) {
		int weight = 0;
		for (Arc currentArc : getOutArcs()) {
			if (currentArc.getEndNode().equals(outPlace))
				weight = currentArc.getWeight();
		}
		return weight;
	}

	/**
	 * Metoda zwraca wagę łuku wejściowego do wskazanego miejsca.
	 * @param inPlace Place - miejsce połączone do danej tranzycji
	 * @return int - waga łuku łączącego
	 */
	public int getInArcWeightFrom(Place inPlace) {
		int weight = 0;
		for (Arc currentArc : getInArcs()) {
			if (currentArc.getStartNode().equals(inPlace))
				weight = currentArc.getWeight();
		}
		return weight;
	}

	/**
	 * Metoda zwraca listę inwariantów z daną tranzycją.
	 * @return ArrayList[ArrayList[Transition]] - macierz inwariantów
	 */
	public ArrayList<ArrayList<Transition>> getContainingInvariants() {
		return containingInvariants;
	}

	/**
	 * Metoda pozwala wpisać inwarianty w której jest dana tranzycja.
	 * @param containingInvariants ArrayList[ArrayList[Transition]] - macierz niezmienników
	 */
	public void setContainingInvariants(ArrayList<ArrayList<Transition>> containingInvariants) {
		this.containingInvariants = containingInvariants;
	}
	
	/**
	 * Metoda zamieniająca dane o krawędzi sieci na łańcuch znaków.
	 * @return String - łańcuch znaków
	 */
	public String toString() {
		String name =  getName();
		if(name == null) {
			return "(T)null";
		} else {
			return "(T)" + getName();
		}
	}
}
