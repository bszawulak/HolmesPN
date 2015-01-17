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
	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAĆ NAZW, DODAWAĆ LUB USUWAĆ PÓL TEJ KLASY
	 * (przestanie być możliwe wczytywanie zapisĆnych proejktów .abyss)
	 */
	
	protected double minFireTime = 0; //TPN
	protected double maxFireTime = 999;	//TPN
	//protected double absoluteFireTime = 0; diabli wiedzą od czego to, nic nie robi
	protected double FireTime = -1; //zmienna związana z modelem sieci TPN
	protected boolean isLaunching;
	protected boolean isGlowed = false;
	protected boolean isGlowedMTC = false;
	
	protected boolean isGlowetCl = false;
	protected Color clusterColorForTransition = new Color(255,255,255);
	protected double clNumber = 0.0;
	
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
	 * Metoda rysująca tranzycję na danym arkuszu.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza 
	 */
	public void draw(Graphics2D g, int sheetId) {
		/*
		for (ElementLocation el : this.getNodeLocations(sheetId)) {
			Rectangle nodeBounds = new Rectangle(
				el.getPosition().x - getRadius(), el.getPosition().y - getRadius(),
					this.getRadius() * 2, this.getRadius() * 2);
			if (!isLaunching) { //jeśli nieaktywna
				if (isGlowedMTC()) { //jeśli ma się świecić jako MCT
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
					
					try {
						BufferedImage img = ImageIO.read(getClass().getResource("/icons/selectedSign.png"));
						g.drawImage(img, null, 
								nodeBounds.x-(this.getRadius()+2), 
								nodeBounds.y-(this.getRadius()+2));
					} catch (Exception e) {
						
					}
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
			
			if (isGlowed()) {
				g.setColor(EditorResources.glowTransitonColorLevel3);
			}
			else if(isGlowedMTC()) {
				g.setColor(EditorResources.glowMTCTransitonColorLevel3);
			}
			else if(isGlowedCluster()) {
				g.setColor(clusterColorForTransition);
			}
			else {
				g.setColor(new Color(224,224,224));
			}
			
			
			g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			g.setColor(Color.DARK_GRAY);
			g.setStroke(new BasicStroke(1.5F));
			g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			
			if (this.isPortal())
				g.drawRect(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.width - 20, nodeBounds.height - 20);
			
			g.setColor(EditorResources.glowTransitonTextColor);
			
			//WYŚWIETLANIE DANYCH O ODPALENIACH
			if (this.isGlowed && this.firingValueInInvariant > 0) {
				int posX = nodeBounds.x + nodeBounds.width / 2 
						- g.getFontMetrics().stringWidth(Integer.toString(this.getTokensNumber())) / 2;
				int posY = nodeBounds.y + nodeBounds.height / 2 + 5;
				g.drawString(Integer.toString(this.getTokensNumber()), posX, posY);
			}
			
			//WYŚWIETLANIE DANYCH ODNOŚNIE WYSTĘPOWANIA TRANZYCJI W KLASTRZE:
			if(this.isGlowetCl && this.clNumber > 0) {
				//int posX = nodeBounds.x + (nodeBounds.width / 2)
				//		- (g.getFontMetrics().stringWidth(Integer.toString(this.clNumber)) / 2);
				int posX = nodeBounds.x + nodeBounds.width
						- (g.getFontMetrics().stringWidth(Integer.toString(this.clNumber)) / 2);
				int posY = nodeBounds.y - 1;// + (nodeBounds.height / 2) + 5;
				Font old = g.getFont();
				Color oldC = g.getColor();
				
				g.setFont(new Font("TimesRoman", Font.BOLD, 14)); 
				g.setColor(Color.black);
				g.drawString(Integer.toString(this.clNumber), posX, posY);
				
				g.setFont(old);
				g.setColor(oldC);
			}
			
		}
		*/
		
		g = ElementDraw.drawElement(this, g, sheetId);
		super.draw(g, sheetId);
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
		return isGlowed;
	}

	/**
	 * Metoda pozwala określić, czy tranzycja ma byc podświetlona oraz ile razy
	 * występuje ona w ramach niezmiennika.
	 * @param isGlowed boolean - true, jeśli ma świecić
	 * @param firingNumber int - liczba uruchomień tranzycji w niezmienniku
	 */
	public void setGlowed_INV(boolean isGlowed, int firingNumber) {
		this.isGlowed = isGlowed;
		this.firingValueInInvariant = firingNumber;
	}

	/**
	 * Metoda pozwala określic, czy tranzycja ma byc podświetlona.
	 * @param isGlowed boolean - true, jeśli ma świecić
	 */
	public void isGlowed_INV(boolean value) {
		this.isGlowed = value;
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
	 * Metoda zwraca wartość flagi koloru dla klastra
	 * @return boolean - true, jeśli ma mieć dany kolor
	 */
	public boolean isGlowed_Cluster() {
		return isGlowetCl;
	}
	
	/**
	 * Metoda ustawia flagę koloru dla tranzycji w ramach klastra oraz kolor.
	 * @param value boolean - true, jeśli ma rysować się w kolorze
	 * @param clColor Color - na jaki kolor
	 * @param double clNumber - średnia liczba uruchomień
	 */
	public void setGlowed_Cluster(boolean value, Color clColor, double clNumber) {
		this.isGlowetCl = value;
		this.clusterColorForTransition = clColor;
		this.clNumber = clNumber;
	}
	
	/**
	 * Zwraca liczbę średnich uruchomień tranzycji w ramach inwariantów w ramach klastra.
	 * @return double - liczba wystąpień
	 */
	public double getFreq_Cluster() {
		return clNumber;
	}
	
	/**
	 * Metoda zwraca aktualnie ustawiony kolor dla trybu wyświetlania klastrów.
	 * @return Color - kolor
	 */
	public Color getColor_Cluster() {
		return clusterColorForTransition;
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
		//this.containingInvariants = containingInvariants;
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
