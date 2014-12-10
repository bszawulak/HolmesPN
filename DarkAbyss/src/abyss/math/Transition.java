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
 * Klasa implementuj�ca tranzycj� w sieci Petriego. Zapewnia implementacj�
 * szeregu funkcjonalno�ci powi�zanych z aktywacj� i odpalaniem tranzycji
 * na potrzeby symulacji dynamiki sieci Petriego, jak rezerwowanie token�w
 * i wykrywanie aktywacji.
 * @author students
 *
 */
public class Transition extends Node {
	private static final long serialVersionUID = 2673581001465115432L;
	
	/*
	 * UWAGA!!! NIE WOLNO ZMIENIA� NAZW, DODAWA� LUB USUWA� P�L TEJ KLASY
	 * (przestanie by� mo�liwe wczytywanie zapisanych projekt�w .abyss)
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
	 * @param pn int - promien okr�gu na kt�rym opisana jest figura geometryczna
	 */
	public Transition(int transitionId, ArrayList<ElementLocation> elementLocations, int pn) {
		super(transitionId, elementLocations, pn);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param sheetId int - identyfikator arkusza
	 * @param transitionId int - identyfikator tranzycji
	 * @param transitionPosition Point - punkt, w kt�rym znajduje si� lokalizacja wierzcho�ka
	 * @param pn int - promien okr�gu na kt�rym opisana jest figura geometryczna
	 */
	public Transition(int sheetId, int transitionId, Point transitionPosition, int pn) {
		super(sheetId, transitionId, transitionPosition, pn);
	}

	/**
	 * Konstruktor obiektu tranzycji sieci.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocation ElementLocation - lokalizacja elementu sieci
	 * @param pn int - promie� okr�gu na kt�rym opisana jest figura geometryczna
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
	 * Metoda rysuj�ca tranzycj� na danym arkuszu.
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
			//g.setColor(Color.white);
			
			if (isGlowed())
				g.setColor(Color.blue);
			else if(isGlowedMTC())
				g.setColor(Color.green);
			else
				g.setColor(new Color(224,224,224));
				//g.setColor(Color.lightGray);
			
				//g.setColor(new Color(0,102,0));
				//g.setColor(Color.lightGray);
			
			
			
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
	 * Metoda pozwala pobra� ��czn� liczb� dost�pnych token�w ze wszystkich miejsc wej�ciowych.
	 * @return int - liczba dost�pnych token�w z pola availableTokens.
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
	 * Metoda pozwala pobra� ��czna liczb� token�w niezb�dnych do aktywacji tej tranzycji.
	 * @return int - liczba token�w potrzebnych do aktywacji z pola requiredTokens
	 */
	public int getRequiredTokens() {
		int requiredTokens = 0;
		for (Arc arc : getInArcs()) {
			requiredTokens += arc.getWeight();
		}
		return requiredTokens;
	}

	/**
	 * Metoda pozwala sprawdzi�, czy tranzycja jest aktywna i mo�e zosta� odpalona.
	 * @return boolean - true, je�li tranzycja jest aktywna i mo�e zosta� odpalona; false w przeciwnym wypadku
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
	 * Metoda pozwala sprawdzi�, czy tranzycja jest w tej chwili odpalana.
	 * @return boolean - true, je�li tranzycja jest aktualnie odpalana; false w przeciwnym wypadku
	 */
	public boolean isLaunching() {
		return isLaunching;
	}

	/**
	 * Metoda informuj�ca, czy tranzycja jest pod�wietlona kolorem
	 * @return boolean - true je�li �wieci; false w przeciwnym wypadku
	 */
	public boolean isGlowed() {
		return isGlowed;
	}

	/**
	 * Metoda pozwala okre�li, czy tranzycja ma byc pod�wietlona oraz ile razy
	 * wyst�puje ona w ramach niezmiennika.
	 * @param isGlowed boolean - true, je�li ma �wieci�
	 * @param firingNumber int - liczba uruchomie� tranzycji w niezmienniku
	 */
	public void setGlowed(boolean isGlowed, int firingNumber) {
		this.isGlowed = isGlowed;
		this.firingNumber = firingNumber;
	}

	/**
	 * Metoda pozwala okre�lic, czy tranzycja ma byc pod�wietlona.
	 * @param isGlowed boolean - true, je�li ma �wiecic
	 */
	public void setGlowed(boolean isGlowed) {
		this.isGlowed = isGlowed;
	}

	/**
	 * Metoda sprawdza, czy tranzycja �wieci b�d�c cz�ci� zbioru MCT.
	 * @return boolean - true je�eli �wieci jako MCT; false w przeciwnym wypadku
	 */
	public boolean isGlowedMTC() {
		return isGlowedMTC;
	}

	/**
	 * Metoda ustawia stan �wiecenia tranzycji jako cz�ci MCT.
	 * @param isGlowedMTC boolean - true je�eli ma �wieci�
	 */
	public void setGlowedMTC(boolean isGlowedMTC) {
		this.isGlowedMTC = isGlowedMTC;
	}

	/**
	 * Metoda zwraca liczb� wyst�pie� uruchomie� tranzycji w ramach niezmiennika.
	 * @return int - liczba wyst�pie� uruchomie� tranzycji w niezmienniku z pola firingNumber
	 */
	public int getTokensNumber() {
		return this.firingNumber;
	}

	/**
	 * Metoda pozwala ustawi�, czy tranzycja jest teraz uruchamiana.
	 * @param isLunching boolean - true, je�eli tranzycja jest w�a�nie uruchamiana;
	 * 		false w przeciwnym wypadku
	 */
	public void setLaunching(boolean isLaunching) {
		this.isLaunching = isLaunching;
	}

	/**
	 * Metoda pozwala zarezerwowa� we wszystkich miejscach wej�ciowych
	 * niezb�dne do uruchomienia tokeny. Inne tranzycje nie mog� ich odebra�.
	 */
	public void bookRequiredTokens() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			origin.bookTokens(arc.getWeight());
		}
	}

	/**
	 * Metoda pozwala zwolni� wszystkie zarezerwowane tokeny we wszystkich
	 * miejscach wej�ciowych. Staj� si� one dost�pne dla innych tranzycji.
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
	 * Metoda zwraca wag� �uku wyj�ciowego do wskazanego miejsca.
	 * @param outPlace Place - miejsce po��czone z dan� tranzycj� (od niej)
	 * @return int - waga �uku ��cz�cego
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
	 * Metoda zwraca wag� �uku wej�ciowego do wskazanego miejsca.
	 * @param inPlace Place - miejsce po��czone do danej tranzycji
	 * @return int - waga �uku ��cz�cego
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
	 * Metoda zwraca list� inwariant�w z dan� tranzycj�.
	 * @return ArrayList[ArrayList[Transition]] - macierz inwariantow
	 */
	public ArrayList<ArrayList<Transition>> getContainingInvariants() {
		return containingInvariants;
	}

	/**
	 * Metoda pozwala wpisa� inwarianty w kt�rej jest dana tranzycja.
	 * @param containingInvariants ArrayList[ArrayList[Transition]] - macierz niezmiennik�w
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
