package holmes.petrinet.elements;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.containers.TransitionGraphicsContainer;
import holmes.petrinet.elements.containers.TransitionQSimContainer;
import holmes.petrinet.elements.extensions.TransitionFPNExtension;
import holmes.petrinet.elements.extensions.TransitionSPNExtension;
import holmes.petrinet.elements.extensions.TransitionTimeExtention;
import holmes.petrinet.functions.FunctionContainer;
import holmes.petrinet.functions.FunctionsTools;

/**
 * Klasa implementująca tranzycję w sieci Petriego. Zapewnia implementację szeregu funkcjonalności
 * powiązanych z aktywacją i odpalaniem tranzycji na potrzeby symulacji dynamiki sieci Petriego, jak
 * rezerwowanie tokenów i wykrywanie aktywacji.
 */
public class Transition extends Node {
    @Serial
    private static final long serialVersionUID = -4981812911464514746L;
    /** PN, TPN, SPN, XTPN, CPN */
    public enum TransitionType {PN, TPN, SPN, XTPN, CPN}
    protected TransitionType transType;
    protected static final int realRadius = 15;

    //GRAPHICAL PROPERTIES:
    protected boolean isLaunching;
    protected boolean knockoutStatus = false;        // czy wyłączona (MCS, inne)
    public boolean borderFrame = false;

    public TransitionGraphicsContainer drawGraphBoxT = new TransitionGraphicsContainer();
    public TransitionQSimContainer qSimBoxT = new TransitionQSimContainer();
    public TransitionTimeExtention timeFunctions = new TransitionTimeExtention();
    public TransitionSPNExtension spnFunctions = new TransitionSPNExtension();
    public TransitionFPNExtension fpnFunctions = new TransitionFPNExtension(this);

    /**
     * Konstruktor obiektu tranzycji sieci. Używany do wczytywania sieci zewnętrznej, np. ze Snoopy
     * @param transitionId (<b>int</b>) identyfikator tranzycji.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji tranzycji.
     * @param name (<b>String</b>) nazwa tranzycji.
     * @param comment (<b>String</b>) komentarz tranzycji.
     */
    public Transition(int transitionId, ArrayList<ElementLocation> elementLocations, String name, String comment) {
        super(transitionId, elementLocations, realRadius);
        this.setName(name);
        this.setComment(comment);
        this.fpnFunctions.createNewFunctionsVector();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        spnFunctions.stochasticType = TransitionSPNExtension.StochaticsType.NONE;
    }

    /**
     * Konstruktor obiektu tranzycji sieci. Używany przez procedury tworzenia portali.
     * @param transitionId (<b>int</b>) identyfikator tranzycji.
     * @param elementLocations (<b>ArrayList[ElementLocation]</b>) lista lokalizacji tranzycji.
     */
    public Transition(int transitionId, ArrayList<ElementLocation> elementLocations) {
        super(transitionId, elementLocations, realRadius);
        this.setName("Transition" + IdGenerator.getNextTransitionId());
        this.fpnFunctions.createNewFunctionsVector();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        spnFunctions.stochasticType = TransitionSPNExtension.StochaticsType.NONE;
    }

    /**
     * Konstruktor obiektu tranzycji sieci.
     * @param transitionId (<b>int</b>) identyfikator tranzycji.
     * @param sheetId (<b>int</b>) identyfikator arkusza.
     * @param transitionPosition (<b>Point</b>) punkt lokalizacji tranzycji.
     */
    public Transition(int transitionId, int sheetId, Point transitionPosition) {
        super(sheetId, transitionId, transitionPosition, realRadius);
        this.setName("Transition" + IdGenerator.getNextTransitionId());
        this.fpnFunctions.createNewFunctionsVector();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        spnFunctions.stochasticType = TransitionSPNExtension.StochaticsType.NONE;
    }

    /**
     * Diabli wiedzą co.
     * @param error (<b>String</b>) parametr, a co?
     */
    public Transition(String error) {
        super(99, IdGenerator.getNextTransitionId(), new Point(0,0), realRadius);
        this.setName("Transition" + IdGenerator.getNextTransitionId());
        this.fpnFunctions.createNewFunctionsVector();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        spnFunctions.stochasticType = TransitionSPNExtension.StochaticsType.NONE;
        this.setName(error);
    }

    /**
     * Metoda rysująca tranzycję na danym arkuszu.
     * @param g (<b>Graphics2D</b>) grafika 2D.
     * @param sheetId (<b>int</b>) identyfikator arkusza.
     * @param eds (<b>ElementDrawSettings</b>) opcje rysowania.
     */
    public void draw(Graphics2D g, int sheetId, ElementDrawSettings eds) {
        ElementDraw.drawElement(this, g, sheetId, eds);
    }

    /**
     * Zwraca zbiór miejsc wejściowych *t.
     * @return ArrayList[Place] - lista miejsc ze zbioru *t
     */
    public ArrayList<Place> getPrePlaces() {
        ArrayList<Place> prePlaces = new ArrayList<>();
        for (ElementLocation el : getElementLocations()) {
            for (Arc arc : el.getInArcs()) {
                Node n = arc.getStartNode();
                if (!prePlaces.contains((Place)n)) {
                    prePlaces.add((Place) n);
                }
            }
        }
        return prePlaces;
    }

    /**
     * Zwraca zbiór miejsc wyjściowych t*.
     * @return ArrayList[Place] - lista miejsc ze zbioru t*
     */
    public ArrayList<Place> getPostPlaces() {
        ArrayList<Place> postPlaces = new ArrayList<>();
        for (ElementLocation el : getElementLocations()) {
            for (Arc arc : el.getOutArcs()) {
                Node n = arc.getEndNode();
                if (!postPlaces.contains((Place)n)) {
                    postPlaces.add((Place) n);
                }
            }
        }
        return postPlaces;
    }

    /**
     * Metoda pozwala pobrać łączną liczbę dostępnych tokenów ze wszystkich miejsc wejściowych.
     * @return int - liczba dostępnych tokenów z pola availableTokens.
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public int getRequiredTokens() {
        int requiredTokens = 0;
        for (Arc arc : getInArcs()) {
            requiredTokens += arc.getWeight();
        }
        return requiredTokens;
    }

    public void setFrame(boolean is){
        this.borderFrame = is;
        /*
        if(borderFrame)
            this.borderFrame = false;
        else
            this.borderFrame = true;
        */
    }

    /**
     * Metoda pozwala ustawić, czy tranzycja jest teraz uruchamiana.
     * @param isLaunching (<b>boolean</b>) true, jeśeli tranzycja jest właśnie uruchamiana
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
     * Metoda ustawia status wyłączenia (knockout) tranzycji w symulatorze.
     * @param status (<b>boolean</b>) true, jeśli ma być wyłączona (knockout).
     */
    public void setKnockout(boolean status) {
        knockoutStatus = status;
    }

    /**
     * Metoda zwraca status aktywności (knockoutu) tranzycji.
     * @return (<b>boolean</b>) - true, jeśli tranzycja jest wyłączona (knocked out).
     */
    public boolean isKnockedOut() {
        return knockoutStatus;
    }

    /**
     * Metoda pozwala sprawdzić, czy tranzycja jest aktywna i może zostać uruchomiona.
     * @return (<b>boolean</b>) - true, jeśli tranzycja jest aktywna i może zostać uruchomiona; false w przeciwnym wypadku.
     */
    public boolean isActive() {
        if (knockoutStatus)
            return false;

        if (timeFunctions.isDPN()) {
            if (timeFunctions.getDPNtimer() == timeFunctions.getDPNduration() ) { //duration zawsze >= 0, dTimer(pre-start) = -1, więc ok
                return true; //nie ważne co mówią pre-places, ta tranzycja musi odpalić!
            }
        }

        for (Arc arc : getInArcs()) {
            Place arcStartPlace = (Place) arc.getStartNode();
            TypeOfArc arcType = arc.getArcType();
            int startPlaceTokens = arcStartPlace.getNonReservedTokensNumber();

            if (arcType == TypeOfArc.INHIBITOR) {
                if (startPlaceTokens >= arc.getWeight())
                    return false; //nieaktywna w przecwiwnym wypadku aktywna (nie jest w danej chwili blokowana)
            } else if (arcType == TypeOfArc.EQUAL && startPlaceTokens != arc.getWeight()) { //DOKŁADNIE TYLE CO WAGA
                return false;
            } else {
                if (fpnFunctions.isFunctional()) {
                    boolean status = FunctionsTools.getFunctionDecision(startPlaceTokens, arc, arc.getWeight(), this);
                    if (!status)
                        return false; //zwróc tylko jesli false
                } else {
                    if (startPlaceTokens < arc.getWeight())
                        return false;
                }
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

            if (arc.getArcType() == TypeOfArc.INHIBITOR) {
                //tylko gdy inhibitor jest jedynym łukiem IN dla tranzycji 'wejściowej' (w standardowym sensie)
                origin.reserveTokens(0); //więcej nie ma, bo inaczej w ogóle by nas tu nie było
            } else if (arc.getArcType() == TypeOfArc.EQUAL) {
                origin.reserveTokens(arc.getWeight()); //więcej nie ma, bo inaczej w ogóle by nas tu nie było
            } else if (arc.getArcType() == TypeOfArc.RESET) {
                int freeToken = origin.getNonReservedTokensNumber();
                origin.reserveTokens(freeToken); //all left
            } else if (arc.getArcType() == TypeOfArc.COLOR) {
                try {
                    ((PlaceColored)origin).reserveColorTokens(arc.getColorWeight(0), 0);
                    ((PlaceColored)origin).reserveColorTokens(arc.getColorWeight(1), 1);
                    ((PlaceColored)origin).reserveColorTokens(arc.getColorWeight(2), 2);
                    ((PlaceColored)origin).reserveColorTokens(arc.getColorWeight(3), 3);
                    ((PlaceColored)origin).reserveColorTokens(arc.getColorWeight(4), 4);
                    ((PlaceColored)origin).reserveColorTokens(arc.getColorWeight(5), 5);
                } catch (Exception ex) {
                    GUIManager.getDefaultGUIManager().log("Transition.bookRequiredTokens() error while booking colored tokens", "error", true);
                }
            } else { //read arc / normal
                if (arc.getArcType() == TypeOfArc.READARC) {
                    if (GUIManager.getDefaultGUIManager().getSettingsManager().getValue("simTransReadArcTokenReserv").equals("0")) {
                        continue; //nie rezerwuj przez read-arc
                    } else {
                        origin.reserveTokens(arc.getWeight());
                    }
                } else { //normalny łuk
                    if (fpnFunctions.isFunctional()) { //fast, no method!
                        FunctionContainer fc = fpnFunctions.getFunctionContainer(arc);
                        if (fc != null) //TODO: czy to jest potrzebne? jeśli na początku symulacji wszystkie tranzycje zyskają te wektory?
                            origin.reserveTokens((int) fc.currentValue); //nie ważne, aktywna czy nie, jeśli nie, to tu jest i tak oryginalna waga
                        else
                            origin.reserveTokens(arc.getWeight());
                    } else {
                        origin.reserveTokens(arc.getWeight());
                    }
                }
            }
        }
    }

    /**
     * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny we wszystkich
     * miejscach wejściowych. Stają się one dostępne dla innych tranzycji.
     */
    public void returnBookedTokens() {
        for (Arc arc : getInArcs()) {
            ((Place) arc.getStartNode()).freeReservedTokens();
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
     *
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
     * Metoda zwraca podtyp tranzycji.
     * @return TransitionType - podtyp
     */
    public TransitionType getTransType() {
        return this.transType;
    }

    /**
     * Metoda ustawia podtyp tranzycji.
     *
     * @param value TransitionType - podtyp
     */
    public void setTransType(TransitionType value) {
        this.transType = value;
    }

    /**
     * Metoda zamieniająca dane o krawędzi sieci na łańcuch znaków.
     *
     * @return String - łańcuch znaków
     */
    public String toString() {
        String name = getName();
        if (name == null) {
            return "(T)null";
        } else {
            return "(T" + GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().indexOf(this) + ")";
        }
    }

    /**
     * Metoda informujaca, czy tranzycja jest kolorowana
     * @return boolean - true, jeśli colored, false jeśli nie
     */
    public boolean isColored() {
        return transType == TransitionType.CPN;
    }
}
