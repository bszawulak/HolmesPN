package abyss.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.ElementDraw;
import abyss.petrinet.data.IdGenerator;
import abyss.petrinet.elements.Arc.TypesOfArcs;

/**
 * Klasa implementująca tranzycję w sieci Petriego. Zapewnia implementację
 * szeregu funkcjonalności powiązanych z aktywacją i odpalaniem tranzycji
 * na potrzeby symulacji dynamiki sieci Petriego, jak rezerwowanie tokenów
 * i wykrywanie aktywacji.
 * @author students - pierwsza wersja
 * @author MR - tak tu namieszałem, że autorzy swojej roboty by już nie poznali :)
 * 	Behold: uber-tranzycja, wszystkie do tej pory zaimplementowane rodzaje tranzycji w jednej klasie
 *
 */
public class Transition extends Node {
	private static final long serialVersionUID = -4981812911464514746L;

	/** NORMAL, TPN, DPN, TDPN */
	public enum TransitionType { PN, TPN, DPN, TDPN }
	private TransitionType transType;
	
	//podstawowe właściwości:
	protected boolean isLaunching;
	protected boolean isGlowedINV = false;
	protected boolean isGlowedMTC = false;
	protected boolean offline = false;		// czy wyłączona (MCS, inne)
	
	//wyświetlanie dodatkowych tekstów nad ikoną:
	protected boolean isColorChanged = false;		//zmiana koloru - status
	protected double transNumericalValue = 0.0;		//dodatkowa liczba do wyświetlenia
	protected String transAdditionalText = "";
	protected boolean showTransitionAddText = false;
	protected Color transColorValue = new Color(255,255,255);
	protected boolean showIntOnly = false; //TODO
	protected boolean valueVisibilityStatus = false;
	public int txtXoff = 0;
	public int txtYoff = 0;
	public int valueXoff = 0;
	public int valueYoff = 0;
	
	//opcje czasowe:
	protected double TPN_eft = 0; //TPN
	protected double TPN_lft = 0; //TPN
	protected double TPNtimerLimit = -1; //TPN
	protected double TPNtimer = -1; //TPN
	protected double DPNduration = 0; //DPN
	protected double DPNtimer = -1; //DPN
	protected boolean TPNactive = false;
	protected boolean DPNactive = false;
	
	//inne:
	protected int firingValueInInvariant = 0; // ile razy uruchomiona w ramach niezmiennika

	//tu było mnóstwo zbędnych konstruktorów potrzebnych równie zbędnej klasie TimeTransition
	//zostały przeniesione jako dolny komentarz do niej, czyli do pakietu abyss.obsolete

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
		transType = TransitionType.PN;
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
		transType = TransitionType.PN;
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
		transType = TransitionType.PN;
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
	
	/**
	 * Metoda zwraca informację, czy ma być wyświetlany dodatkowy tekst obok rysunku tranzycji.
	 * @return boolean - true, jeśli tak
	 */
	public boolean showAddText() {
		return showTransitionAddText;
	}
	
	/**
	 * Metoda zwraca dodatkowy tekst do wyświetlenia.
	 * @return String - tekst
	 */
	public String returnAddText() {
		return transAdditionalText;
	}
	
	/**
	 * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
	 * @param isColorChanged boolean - true, jeśli ma rysować się w kolorze
	 * @param transColorValue Color - na jaki kolor
	 * @param showNumber boolean - true, jeśli liczba ma się wyświetlać
	 * @param transNumericalValue double - liczba do wyświetlenia
	 * @param showText boolean - czy pokazać dodatkowy tekst
	 * @param text String - dodatkowy tekst do wyświetlenia
	 */
	public void setColorWithNumber(boolean isColorChanged, Color transColorValue, 
			boolean showNumber, double transNumericalValue, boolean showText, String text) {
		this.isColorChanged = isColorChanged;
		this.transColorValue = transColorValue;
		this.valueVisibilityStatus = showNumber;
		this.transNumericalValue = transNumericalValue;
		this.showTransitionAddText = showText;
		this.transAdditionalText = text;
	}
	
	/**
	 * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
	 * @param isColorChanged boolean - true, jeśli ma rysować się w kolorze
	 * @param transColorValue Color - na jaki kolor
	 * @param showNumber boolean - true, jeśli liczba ma się wyświetlać
	 * @param transNumericalValue double - liczba do wyświetlenia
	 * @param showText boolean - czy pokazać dodatkowy tekst
	 * @param text String - dodatkowy tekst do wyświetlenia
	 * @param txtXoff int - przesunięcie X tekstu
	 * @param txtYoff int - przesunięcie Y tekstu
	 * @param valueXoff int - przesunięcie X liczby
	 * @param valueYoff int - przesunięcie Y liczby
	 */
	public void setColorWithNumber(boolean isColorChanged, Color transColorValue, 
			boolean showNumber, double transNumericalValue, boolean showText, String text,
			int txtXoff, int txtYoff, int valueXoff, int valueYoff) {
		this.isColorChanged = isColorChanged;
		this.transColorValue = transColorValue;
		this.valueVisibilityStatus = showNumber;
		this.transNumericalValue = transNumericalValue;
		this.showTransitionAddText = showText;
		this.transAdditionalText = text;
		
		this.txtXoff = txtXoff;
		this.txtYoff = txtYoff;
		this.valueXoff = valueXoff;
		this.valueYoff = valueYoff;
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
	 * Metoda pozwala sprawdzić, czy tranzycja jest aktywna i może zostać odpalona.
	 * @return boolean - true, jeśli tranzycja jest aktywna i może zostać odpalona; false w przeciwnym wypadku
	 */
	public boolean isActive() {
		if(offline == true)
			return false;
		
		if(DPNactive) {
			if(DPNtimer == DPNduration) { //duration zawsze >= 0, dTimer(pre-start) = -1, więc ok
				return true; //nie ważne co mówią pre-places, ta tranzycja musi odpalić!
			}
		}
	
		for (Arc arc : getInArcs()) {
			Place arcStartPlace = (Place) arc.getStartNode();
			TypesOfArcs arcType = arc.getArcType();
			int startPlaceTokens = arcStartPlace.getNonReservedTokensNumber();
			
			if(arcType == TypesOfArcs.INHIBITOR) { 
				if(startPlaceTokens > 0)
					return false; //nieaktywna
				else
					continue; //aktywna (nie jest w danej chwili blokowana)
			} else if(arcType == TypesOfArcs.EQUAL && startPlaceTokens != arc.getWeight()) { //DOKŁADNIE TYLE CO WAGA
				return false;
			} else {
				if (startPlaceTokens < arc.getWeight())
					return false;
			}	
		}
		return true;
	}
	
	/**
	 * Metoda pozwala zarezerwować we wszystkich miejscach wejściowych
	 * niezbędne do uruchomienia tokeny. Inne tranzycje nie mogą ich odebrać.
	 */
	public void bookRequiredTokens() {
		for (Arc arc : getInArcs()) { //dla inhibitor nie działa, w ogóle tu nie wejdzie
			Place origin = (Place) arc.getStartNode();
			
			if(arc.getArcType() == TypesOfArcs.INHIBITOR) {
				//tylko gdy inhibitor jest jedynym łukiem IN dla tranzycji 'wejściowej' (w standardowym sensie)
				origin.reserveTokens(0); //więcej nie ma, bo inaczej w ogóle by nas tu nie było
			} else if(arc.getArcType() == TypesOfArcs.EQUAL) {
				origin.reserveTokens(arc.getWeight()); //więcej nie ma, bo inaczej w ogóle by nas tu nie było
			} else if(arc.getArcType() == TypesOfArcs.RESET) {
				int freeToken = origin.getNonReservedTokensNumber();
				origin.reserveTokens(freeToken); //all left
			} else { //read arc / normal
				if(arc.getArcType() == TypesOfArcs.READARC) {
					if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("simTransReadArcTokenReserv").equals("0")) {
						continue; //nie rezerwuj przez read-arc
					} else {
						origin.reserveTokens(arc.getWeight());
					}
				} else
					origin.reserveTokens(arc.getWeight());
			}
		}
	}

	/**
	 * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny we wszystkich
	 * miejscach wejściowych. Stają się one dostępne dla innych tranzycji.
	 */
	public void returnBookedTokens() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			origin.freeReservedTokens();
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
	 * Metoda ustawia podtyp tranzycji.
	 * @param value TransitionType - podtyp
	 */
	public void setTransType(TransitionType value) {
		this.transType = value;
	}
	
	/**
	 * Metoda zwraca podtyp tranzycji.
	 * @return TransitionType - podtyp
	 */
	public TransitionType getTransType() {
		return this.transType;
	}
	
	//**************************************************************************************
	//*********************************      TIME        ***********************************
	//**************************************************************************************
	
	/**
	 * Metoda ustala dolny limit niezerowego czasu gotowości - EFT.
	 * @param value double - czas EFT
	 */
	public void setEFT(double value) {
		if(value < 0) {
			this.TPN_eft = 0;
			return;
		}
		if(value > TPN_lft) {
			this.TPN_eft = TPN_lft;
			return;
		}
		this.TPN_eft = value;
	}
	
	/**
	 * Metoda pozwala odczytać przypisany czas EFT tranzycji.
	 * @return double - czas EFT
	 */
	public double getEFT() {
		return this.TPN_eft;
	}

	/**
	 * Metoda ustala górny limit nieujemnego czasu krytycznego - LFT.
	 * @param value double - czas LFT (deadline na uruchomienie)
	 */
	public void setLFT(double value) {
		if(value < TPN_eft) {
			this.TPN_lft = TPN_eft;
			return;
		}
		
		this.TPN_lft = value;
	}

	/**
	 * Metoda pozwala odczytać przypisany czas LFT tranzycji.
	 * @return double - czas LFT
	 */
	public double getLFT() {
		return this.TPN_lft;
	}
	
	/**
	 * Metoda pozwala ustawic czas uruchomienia tranzycji.
	 * @param value double - czas uruchomienia tranzycji
	 */
	public void setTPNtimerLimit(double value) {
		TPNtimerLimit = value;
	}

	/**
	 * Metoda zwraca aktualny czas uruchomienia.
	 * @return double - czas uruchomienia - pole FireTime
	 */
	public double getTPNtimerLimit() {
		return TPNtimerLimit;
	}
	
	/**
	 * Metoda zwraca aktualny zegar uruchomienia dla tranzycji.
	 * @return double - czas uruchomienia - pole FireTime
	 */
	public double getTPNtimer() {
		return TPNtimer;
	}

	/**
	 * Metoda pozwala ustawic zegar uruchomienia tranzycji.
	 * @param value double - czas uruchomienia tranzycji
	 */
	public void setTPNtimer(double value) {
		TPNtimer = value;
	}
	
	/**
	 * Metoda ustawia nowy czas trwania odpalenia dla tranzycji DPN.
	 * @param val double - nowy czas
	 */
	public void setDPNduration(double value) {
		if(value < 0)
			DPNduration = 0;
		else
			DPNduration = value;
	}
	
	/**
	 * Metoda zwraca ustawioną dla tranzycji DPN wartość duration.
	 * @return double - czas trwania odpalenia tranzycji
	 */
	public double getDPNduration() {
		return DPNduration;
	}
	
	/**
	 * Metoda ustawia nowy wewnętrzny timer dla czasu odpalenia dla tranzycji DPN.
	 * @param val double - nowa wartość zegara dla DPN
	 */
	public void setDPNtimer(double value) {
		DPNtimer = value;
	}
	
	/**
	 * Metoda zwraca aktualną wartość zegara odliczającego czas do odpalenia tranzycji DPN (produkcji tokenów).
	 * @return double durationTimer - 
	 */
	public double getDPNtimer() {
		return DPNtimer;
	}
	
	/**
	 * Metoda pozwalająca stwierdzić, czy tranzycja DPN jest gotowa do produkcji tokenów.
	 * @return boolean - true, jeśli zegar DPN ma wartość równą ustalonemu czasowi DPN dla tranzycji
	 */
	public boolean isDPNforcedToFire() {
		if(DPNtimer >= DPNduration)
			return true;
		else
			return false;
	}
	
	/**
	 * Metoda informująca czy tranzycja TPN musi zostać uruchomiona.
	 * @return boolean - true, jeśli wewnętrzny zegar (!= -1) jest równy deadlinowi dla TPN
	 */
	public boolean isTPNforcedToFired() {
		if(TPNtimerLimit != -1) {
			if(TPNtimerLimit == TPNtimer)
				return true;
			else
				return false;
		} else {
			return false; //nieaktywna
		}
	}
	
	/**
	 * Metoda resetuje zegary tranzycji, powinna być używana przez symulatory po tym, jak wyprodukowano
	 * tokeny (faza II: AddTokens symulacji)
	 */
	public void resetTimeVariables() {
		TPNtimerLimit = -1;
		TPNtimer = -1;
		DPNtimer = -1;
	}
	
	/**
	 * Metoda włącza lub wyłącza tryb TPN
	 * @param status boolean - true, jeśli tryb TPN ma być aktywny
	 */
	public void setTPNstatus(boolean status) {
		TPNactive = status;
	}
	
	/**
	 * Metoda zwraca stan aktywności trybu TPN
	 * @return boolean - true, jeśli TPN aktywny
	 */
	public boolean getTPNstatus() {
		return TPNactive;
	}
	
	/**
	 * Metoda włącza lub wyłącza tryb DPN
	 * @param status boolean - true, jeśli tryb DPN ma być aktywny
	 */
	public void setDPNstatus(boolean status) {
		DPNactive = status;
	}
	
	/**
	 * Metoda zwraca stan aktywności trybu DPN
	 * @return boolean - true, jeśli DPN aktywny
	 */
	public boolean getDPNstatus() {
		return DPNactive;
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
			return "(T" + GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().indexOf(this)+")";
		}
	}
}