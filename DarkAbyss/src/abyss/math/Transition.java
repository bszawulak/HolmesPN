package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import abyss.graphpanel.EditorResources;
import abyss.graphpanel.IdGenerator;

/**
 * Klasa implementuj¹ca tranzycjê w sieci Petriego. Zapewnia implementacjê
 * szeregu funkcjonalnoœci powi¹zanych z aktywacj¹ i odpalaniem tranzycji
 * na potrzeby symulacji dynamiki sieci Petriego, jak rezerwowanie tokenów
 * i wykrywanie aktywacji.
 * @author students
 *
 */
public class Transition extends Node {
	private static final long serialVersionUID = 2673581001465115432L;
	
	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAÆ NAZW, DODAWAÆ LUB USUWAÆ PÓL TEJ KLASY
	 * (przestanie byæ mo¿liwe wczytywanie zapisanych proejktów .abyss)
	 */
	protected double minFireTime = 0;
	protected double maxFireTime = 999;
	protected double absoluteFireTime = 0;
	private double FireTime = -1;
	
	protected boolean isLaunching;
	protected boolean isGlowed = false;
	private boolean isGlowedMTC = false;
	protected int firingNumber = 0;
	private ArrayList<ArrayList<Transition>> containingInvariants = new ArrayList<ArrayList<Transition>>();

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji
	 * @param pn int - promien okrêgu na którym opisana jest figura geometryczna
	 */
	public Transition(int transitionId, ArrayList<ElementLocation> elementLocations, int pn) {
		super(transitionId, elementLocations, pn);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param sheetId int - identyfikator arkusza
	 * @param transitionId int - identyfikator tranzycji
	 * @param transitionPosition Point - punkt, w którym znajduje siê lokalizacja wierzcho³ka
	 * @param pn int - promien okrêgu na którym opisana jest figura geometryczna
	 */
	public Transition(int sheetId, int transitionId, Point transitionPosition, int pn) {
		super(sheetId, transitionId, transitionPosition, pn);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocation ElementLocation - lokalizacja elementu sieci
	 * @param pn int - promieñ okrêgu na którym opisana jest figura geometryczna
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
	 * Konstruktor obiektu tranzycji sieci.
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
	 * Konstruktor obiektu tranzycji sieci.
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
	 * Metoda rysuj¹ca tranzycjê na danym arkuszu.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza 
	 */
	public void draw(Graphics2D g, int sheetId) {
		for (ElementLocation el : this.getNodeLocations(sheetId)) {
			Rectangle nodeBounds = new Rectangle(
				el.getPosition().x - getRadius(), el.getPosition().y - getRadius(),
					this.getRadius() * 2, this.getRadius() * 2);
			if (!isLaunching) {
				if (isGlowedMTC()) {
					g.setColor(EditorResources.glowMTCTransitonColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowMTCTransitonColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowMTCTransitonColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				} else if (isGlowed()) {
					g.setColor(EditorResources.glowTransitonColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowTransitonColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowTransitonColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				} else if (el.isSelected() && !el.isPortalSelected()) {
					g.setColor(EditorResources.selectionColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				} else if (el.isPortalSelected()) {
					g.setColor(EditorResources.glowPortalColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowPortalColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

					g.setColor(EditorResources.glowPortalColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				}
			}
			if (isLaunching()) {
				g.setColor(EditorResources.launchColorLevel1);
				g.setStroke(EditorResources.glowStrokeLevel1);
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				g.setColor(EditorResources.launchColorLevel2);
				g.setStroke(EditorResources.glowStrokeLevel2);
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				g.setColor(EditorResources.launchColorLevel3);
				g.setStroke(EditorResources.glowStrokeLevel3);
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			}
			g.setColor(Color.white);
			g.setColor(Color.lightGray);
			g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			g.setColor(Color.DARK_GRAY);
			g.setStroke(new BasicStroke(1.5F));
			g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			if (this.isPortal())
				g.drawRect(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.width - 20, nodeBounds.height - 20);
			g.setColor(EditorResources.glowTransitonTextColor);
			if (this.isGlowed && this.firingNumber > 0)
				g.drawString(
						Integer.toString(this.getTokensNumber()),
						nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics()
						.stringWidth(Integer.toString(this.getTokensNumber())) / 2, 
						nodeBounds.y + nodeBounds.height / 2 + 5);
		}
		super.draw(g, sheetId);
	}

	/**
	 * Metoda pozwala pobraæ ³¹czn¹ liczbê dostêpnych tokenów ze wszystkich miejsc wejœciowych.
	 * @return int - liczba dostêpnych tokenów z pola availableTokens.
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
	 * Metoda pozwala pobraæ ³¹czna liczbê tokenów niezbêdnych do aktywacji tej tranzycji.
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
	 * Metoda pozwala sprawdziæ, czy tranzycja jest aktywna i mo¿e zostaæ odpalona.
	 * @return boolean - true, jeœli tranzycja jest aktywna i mo¿e zostaæ odpalona; false w przeciwnym wypadku
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
	 * Metoda pozwala sprawdziæ, czy tranzycja jest w tej chwili odpalana.
	 * @return boolean - true, jeœli tranzycja jest aktualnie odpalana; false w przeciwnym wypadku
	 */
	public boolean isLaunching() {
		return isLaunching;
	}

	/**
	 * Metoda informuj¹ca, czy tranzycja jest podœwietlona kolorem
	 * @return boolean - true jeœli œwieci; false w przeciwnym wypadku
	 */
	public boolean isGlowed() {
		return isGlowed;
	}

	/**
	 * Metoda pozwala okreœli, czy tranzycja ma byc podœwietlona oraz ile razy
	 * wystêpuje ona w ramach niezmiennika.
	 * @param isGlowed boolean - true, jeœli ma œwieciæ
	 * @param firingNumber int - liczba uruchomieñ tranzycji w niezmienniku
	 */
	public void setGlowed(boolean isGlowed, int firingNumber) {
		this.isGlowed = isGlowed;
		this.firingNumber = firingNumber;
	}

	/**
	 * Metoda pozwala okreœlic, czy tranzycja ma byc podœwietlona.
	 * @param isGlowed boolean - true, jeœli ma œwiecic
	 */
	public void setGlowed(boolean isGlowed) {
		this.isGlowed = isGlowed;
	}

	/**
	 * Metoda sprawdza, czy tranzycja œwieci bêd¹c czêœci¹ zbioru MCT.
	 * @return boolean - true je¿eli œwieci jako MCT; false w przeciwnym wypadku
	 */
	public boolean isGlowedMTC() {
		return isGlowedMTC;
	}

	/**
	 * Metoda ustawia stan œwiecenia tranzycji jako czêœci MCT.
	 * @param isGlowedMTC boolean - true je¿eli ma œwieciæ
	 */
	public void setGlowedMTC(boolean isGlowedMTC) {
		this.isGlowedMTC = isGlowedMTC;
	}

	/**
	 * Metoda zwraca liczbê wyst¹pieñ uruchomieñ tranzycji w ramach niezmiennika.
	 * @return int - liczba wyst¹pieñ uruchomieñ tranzycji w niezmienniku z pola firingNumber
	 */
	public int getTokensNumber() {
		return this.firingNumber;
	}

	/**
	 * Metoda pozwala ustawiæ, czy tranzycja jest teraz uruchamiana.
	 * @param isLunching boolean - true, je¿eli tranzycja jest w³aœnie uruchamiana;
	 * 		false w przeciwnym wypadku
	 */
	public void setLaunching(boolean isLaunching) {
		this.isLaunching = isLaunching;
	}

	/**
	 * Metoda pozwala zarezerwowaæ we wszystkich miejscach wejœciowych
	 * niezbêdne do uruchomienia tokeny. Inne tranzycje nie mog¹ ich odebraæ.
	 */
	public void bookRequiredTokens() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			origin.bookTokens(arc.getWeight());
		}
	}

	/**
	 * Metoda pozwala zwolniæ wszystkie zarezerwowane tokeny we wszystkich
	 * miejscach wejœciowych. Staj¹ siê one dostêpne dla innych tranzycji.
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
	 * Metoda zwraca wagê ³uku wyjœciowego do wskazanego miejsca.
	 * @param outPlace Place - miejsce po³¹czone z dan¹ tranzycj¹ (od niej)
	 * @return int - waga ³uku ³¹cz¹cego
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
	 * Metoda zwraca wagê ³uku wejœciowego do wskazanego miejsca.
	 * @param inPlace Place - miejsce po³¹czone do danej tranzycji
	 * @return int - waga ³uku ³¹cz¹cego
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
	 * Metoda zwraca listê inwariantów z dan¹ tranzycj¹.
	 * @return ArrayList[ArrayList[Transition]] - macierz inwariantow
	 */
	public ArrayList<ArrayList<Transition>> getContainingInvariants() {
		return containingInvariants;
	}

	/**
	 * Metoda pozwala wpisaæ inwarianty w której jest dana tranzycja.
	 * @param containingInvariants ArrayList[ArrayList[Transition]] - macierz niezmienników
	 */
	public void setContainingInvariants(ArrayList<ArrayList<Transition>> containingInvariants) {
		this.containingInvariants = containingInvariants;
	}
	
	/**
	 * Metoda zwraca minimalny czas uruchomienia tranzycji.
	 * @return double - czas minimalny uruchomienia tranzycji, z pola minFireTime
	 */
	public double getMinFireTime() {
		return minFireTime;
	}

	/**
	 * Metoda zwraca maksymalny czas uruchomienia tranzycji.
	 * @return double - czas maksymalny do uruchomienia, z pola maxFireTime
	 */
	public double getMaxFireTime() {
		return maxFireTime;
	}

	/**
	 * Metoda zwraca aktualny czas uruchomienia.
	 * @return double - czas uruchomienia - pole FireTime
	 */
	public double getFireTime() {
		return FireTime;
	}

	/**
	 * Metoda pozwala ustawic czas uruchomienia tranzycji.
	 * @param fireTime double - czas uruchomienia tranzycji
	 */
	public void setFireTime(double fireTime) {
		FireTime = fireTime;
	}
}
