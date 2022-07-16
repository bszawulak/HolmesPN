package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.SPNtransitionData;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.functions.FunctionContainer;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.SimulatorGlobals;

/**
 * Klasa implementująca tranzycję w sieci Petriego. Zapewnia implementację szeregu funkcjonalności
 * powiązanych z aktywacją i odpalaniem tranzycji na potrzeby symulacji dynamiki sieci Petriego, jak
 * rezerwowanie tokenów i wykrywanie aktywacji.
 *
 * @author students - pierwsza wersja
 * @author MR - tak tu namieszałem, że autorzy swojej roboty by już nie poznali :)
 * Behold: uber-tranzycja, wszystkie do tej pory zaimplementowane rodzaje tranzycji w jednej klasie
 *
 * [2022-06-29 MR] I-am-a-genius-meme.jpg; skoro wszystko w jednej klasie, to jedziemy: xTPN też tu się znajdzie.
 *          W końcu to tylko 6 liczb opisujących cechy xTPN oraz pewnie zbiorek zmiennych rządzących
 *          stylem rysowania w Holmesie. Co może pójść nie tak... ***** inżynierię oprogramowania, alleluja i
 *          do przodu. XTPN, to brzmi dumnie. Jak science fiction. Jak "Okręt zwany Francis".
 */
public class Transition extends Node {
    @Serial
    private static final long serialVersionUID = -4981812911464514746L;
    /**
     * PN, TPN, SPN, XTPN, CPNbasic
     */
    public enum TransitionType {PN, TPN, SPN, XTPN, CPNbasic} //, DPN, TDPN, CPNbasic }
    private TransitionType transType;
    private static final int realRadius = 15;

    //podstawowe właściwości:
    private boolean isLaunching;
    private boolean isGlowedINV = false;
    private boolean isGlowedMTC = false;
    //protected boolean isGlowedSub = false;
    protected boolean offline = false;        // czy wyłączona (MCS, inne)


    //wyświetlanie dodatkowych tekstów nad ikoną:
    private boolean isColorChanged = false;        //zmiana koloru - status
    private double transNumericalValue = 0.0;        //dodatkowa liczba do wyświetlenia
    private String transAdditionalText = "";
    private boolean showTransitionAddText = false;
    private Color transColorValue = new Color(255, 255, 255);
    private boolean valueVisibilityStatus = false;
    //inne napisy, np MCT (?)
    public int txtXoff = 0;
    public int txtYoff = 0;
    public int valueXoff = 0;
    public int valueYoff = 0;
    public Color defColor = new Color(224, 224, 224); //Color.LIGHT_GRAY;

    //quickSim - kolorowanie wyników symulacji
    public boolean qSimDrawed = false; // czy rysować dodatkowe oznaczenie tranzycji - okrąg
    public int qSimOvalSize = 10; //rozmiar okręgu oznaczającego
    public Color qSimOvalColor = Color.RED;
    public Color qSimFillColor = Color.WHITE; //domyślny kolor
    public boolean qSimDrawStats = false; // czy rysować wypełnienie tranzycji
    public int qSimFillValue = 0; //poziom wypełnienia
    public double qSimFired = 0; //ile razy uruchomiona
    //public String qSimText = ""; //dodatkowy tekst
    public boolean borderFrame = false;

    //opcje czasowe:
    private double TPN_eft = 0; //TPN
    private double TPN_lft = 0; //TPN
    private double TPNtimerLimit = -1; //TPN
    private double TPNtimer = -1; //TPN
    private double DPNduration = 0; //DPN
    private double DPNtimer = -1; //DPN
    private boolean TPNactive = false;
    private boolean DPNactive = false;

    //opcje kolorow (basic) ?
    private int reqT0red = 1;
    private int reqT1green = 0;
    private int reqT2blue = 0;
    private int reqT3yellow = 0;
    private int reqT4gray = 0;
    private int reqT5black = 0;

    //tranzycja funkcyjna:
    private boolean isFunctional = false;
    private ArrayList<FunctionContainer> fList;

    //tranzycja stochastyczna:
    /**
     * ST, DT, IM, SchT, NONE - Stochastic Transition, Deterministic T., Immediate T., Scheduled T.
     */
    public enum StochaticsType {ST, DT, IM, SchT, NONE}
    private StochaticsType stochasticType;
    private double firingRate = 1.0;
    private SPNtransitionData SPNbox = null;
    //SSA
    private double SPNprobTime = 0.0;

    //inne:
    private int firingValueInInvariant = 0; // ile razy uruchomiona w ramach niezmiennika


    /*  ***********************************************************************************
        ********************************    xTPN    ***************************************
        ***********************************************************************************  */
    private boolean isXTPN = false; //po narysowaniu w Holmesie będzie niezmienialne true
        //co oznacza, że tej tranzycji NIE MOŻNA przekonwertować na klasyczną inaczej, niż
        //odpowiednio ustawiając poniższe parametry. (alphaL=0; alphaU=-1; betaL=betaU=0)
    //parametry xTPN:
    private boolean alphaMode_xTPN = true;
    private boolean alphaRangeVisibility_XTPN = true;
    private double alphaMin_xTPN = 0.0;
    private double alphaMax_xTPN = 1.0;
    private boolean betaMode_xTPN = true;
    private double betaMin_xTPN = 0.0;
    private double betaMax_xTPN = 1.0;
    private boolean betaRangeVisibility_XTPN = true;
    private double tauAlpha_xTPN = -1.0;
    private double tauBeta_xTPN = -1.0;
    private double timer_Ualfa_XTPN = -1.0;
    private double timer_Vbeta_XTPN = -1.0;
    private boolean tauTimersVisibility_XTPN = false; //czy wyświetlać timery

    //jeśli miejsca wejściowe tracą szybciej tokeny ze starości niż z
    //produkcji, zmniejszamy tau Alfa i Beta (prawdopodobieństwo).
    private boolean massActionKinetics = false;
    private boolean isImmediateXTPN = false;
    private boolean isActivated_xTPN = false;
    private boolean isProducing_xTPN = false;
    //grafika:
    private int fractionDigits = 6;


    /**
     * Konstruktor obiektu tranzycji sieci. Używany do wczytywania sieci zewnętrznej, np. ze Snoopy
     *
     * @param transitionId     int - identyfikator tranzycji
     * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
     * @param name             String - nazwa tranzycji
     * @param comment          String - komentarz tranzycji
     */
    public Transition(int transitionId, ArrayList<ElementLocation> elementLocations, String name, String comment) {
        super(transitionId, elementLocations, realRadius);
        this.setName(name);
        this.setComment(comment);
        this.fList = new ArrayList<>();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        stochasticType = StochaticsType.NONE;
    }

    /**
     * Konstruktor obiektu tranzycji sieci. Używany przez procedury tworzenia portali.
     *
     * @param transitionId     int - identyfikator tranzycji
     * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
     */
    public Transition(int transitionId, ArrayList<ElementLocation> elementLocations) {
        super(transitionId, elementLocations, realRadius);
        this.setName("Transition" + IdGenerator.getNextTransitionId());
        this.fList = new ArrayList<>();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        stochasticType = StochaticsType.NONE;
    }

    /**
     * Konstruktor obiektu tranzycji sieci.
     *
     * @param transitionId       int - identyfikator tranzycji
     * @param sheetId            int - identyfikator arkusza
     * @param transitionPosition Point - punkt lokalizacji tranzycji
     */
    public Transition(int transitionId, int sheetId, Point transitionPosition) {
        super(sheetId, transitionId, transitionPosition, realRadius);
        this.setName("Transition" + IdGenerator.getNextTransitionId());
        this.fList = new ArrayList<>();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        stochasticType = StochaticsType.NONE;
    }

    public Transition(String error) {
        super(99, IdGenerator.getNextTransitionId(), new Point(0,0), realRadius);
        this.setName("Transition" + IdGenerator.getNextTransitionId());
        this.fList = new ArrayList<>();
        this.setType(PetriNetElementType.TRANSITION);
        transType = TransitionType.PN;
        stochasticType = StochaticsType.NONE;
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
     *
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
     *
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

    /**
     * Metoda informująca, czy tranzycja jest podświetlona kolorem
     *
     * @return boolean - true jeśli świeci; false w przeciwnym wypadku
     */
    public boolean isGlowed() {
        return isGlowedINV;
    }

    /**
     * Metoda pozwala określić, czy tranzycja ma byc podświetlona oraz ile razy
     * występuje ona w ramach niezmiennika.
     *
     * @param isGlowed             boolean - true, jeśli ma świecić
     * @param numericalValueShowed int - liczba uruchomień tranzycji w niezmienniku
     */
    public void setGlowedINV(boolean isGlowed, int numericalValueShowed) {
        this.isGlowedINV = isGlowed;
        this.firingValueInInvariant = numericalValueShowed;
    }

    /**
     * Metoda sprawdza, czy tranzycja świeci będąc częcią zbioru MCT.
     *
     * @return boolean - true jeżeli świeci jako MCT; false w przeciwnym wypadku
     */
    public boolean isGlowed_MTC() {
        return isGlowedMTC;
    }

    /**
     * Metoda ustawia stan świecenia tranzycji jako częci MCT.
     *
     * @param value boolean - true jeżeli ma świecić
     */
    public void setGlowed_MTC(boolean value) {
        this.isGlowedMTC = value;
    }


    /**
     * Metoda informuje, czy tramzycja ma być rysowana z innym kolorem wypełnienia
     *
     * @return boolean - true, jeśli ma mieć inny kolor niż domyślny
     */
    public boolean isColorChanged() {
        return isColorChanged;
    }

    /**
     * Metoda zwraca informację, czy ma być wyświetlany dodatkowy tekst obok rysunku tranzycji.
     *
     * @return boolean - true, jeśli tak
     */
    public boolean isShowedAddText() {
        return showTransitionAddText;
    }

    public void setAddText(String txt) {
        showTransitionAddText = true;
        transAdditionalText = txt;
    }

    /**
     * Metoda zwraca dodatkowy tekst do wyświetlenia.
     *
     * @return String - tekst
     */
    public String returnAddText() {
        return transAdditionalText;
    }

    /**
     * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
     *
     * @param isColorChanged      boolean - true, jeśli ma rysować się w kolorze
     * @param transColorValue     Color - na jaki kolor
     * @param showNumber          boolean - true, jeśli liczba ma się wyświetlać
     * @param transNumericalValue double - liczba do wyświetlenia
     * @param showText            boolean - czy pokazać dodatkowy tekst
     * @param text                String - dodatkowy tekst do wyświetlenia
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
     * Metoda ustawia stan zmiany koloru oraz liczbę do wyświetlenia.
     *
     * @param isColorChanged      boolean - true, jeśli ma rysować się w kolorze
     * @param transColorValue     Color - na jaki kolor
     * @param showNumber          boolean - true, jeśli liczba ma się wyświetlać
     * @param transNumericalValue double - liczba do wyświetlenia
     * @param showText            boolean - czy pokazać dodatkowy tekst
     * @param text                String - dodatkowy tekst do wyświetlenia
     * @param txtXoff             int - przesunięcie X tekstu
     * @param txtYoff             int - przesunięcie Y tekstu
     * @param valueXoff           int - przesunięcie X liczby
     * @param valueYoff           int - przesunięcie Y liczby
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
     *
     * @return boolean - true, jeśli ma się wyświetlać
     */
    public boolean getNumericalValueVisibility() {
        return valueVisibilityStatus;
    }

    /**
     * Zwraca liczbę która ma się wyświetlać obok kwadratu tranzycji.
     *
     * @return double - liczba
     */
    public double getNumericalValueDOUBLE() {
        return transNumericalValue;
    }

    /**
     * Metoda zwraca aktualnie ustawiony kolor dla tranzycji
     *
     * @return Color - kolor
     */
    public Color getTransitionNewColor() {
        return transColorValue;
    }

    /**
     * Metoda zwraca liczbę wystąpień uruchomień tranzycji w ramach niezmiennika.
     *
     * @return int - liczba wystąpień uruchomień tranzycji w niezmienniku z pola firingNumber
     */
    public int getFiring_INV() {
        return this.firingValueInInvariant;
    }

    /**
     * Metoda pozwala ustawić, czy tranzycja jest teraz uruchamiana.
     *
     * @param isLaunching boolean - true, jeśeli tranzycja jest właśnie uruchamiana;
     *                    false w przeciwnym wypadku
     */
    public void setLaunching(boolean isLaunching) {
        this.isLaunching = isLaunching;
    }

    /**
     * Metoda pozwala sprawdzić, czy tranzycja jest w tej chwili odpalana.
     *
     * @return boolean - true, jeśli tranzycja jest aktualnie odpalana; false w przeciwnym wypadku
     */
    public boolean isLaunching() {
        return isLaunching;
    }

    /**
     * Metoda ustawia status wyłączenia tranzycji w symulatorze.
     *
     * @param status boolean - true, jeśli ma być wyłączona
     */
    public void setOffline(boolean status) {
        offline = status;
    }

    /**
     * Metoda zwraca status aktywności tranzycji.
     *
     * @return boolean - true, jeśli tranzycja jest wyłączona (MCS)
     */
    public boolean isOffline() {
        return offline;
    }

    /**
     * Metoda pozwala sprawdzić, czy tranzycja jest aktywna i może zostać uruchomiona.
     * @return (<b>boolean</b>) - true, jeśli tranzycja jest aktywna i może zostać uruchomiona; false w przeciwnym wypadku.
     */
    public boolean isActive() {
        if (offline)
            return false;

        if (DPNactive) {
            if (DPNtimer == DPNduration) { //duration zawsze >= 0, dTimer(pre-start) = -1, więc ok
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
                if (isFunctional) { //fast, no method
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
     * Metoda pozwala sprawdzić, czy tranzycja kolorowana jest aktywna i może zostać odpalona.
     *
     * @return boolean - true, jeśli tranzycja jest aktywna i może zostać odpalona; false w przeciwnym wypadku
     */
    public boolean isColorActive() {
        if (offline)
            return false;

        int req0red = 0;
        int req1green = 0;
        int req2blue = 0;
        int req3yellow = 0;
        int req4grey = 0;
        int req5black = 0;

        int tokens0red = 0;
        int tokens1green = 0;
        int tokens2blue = 0;
        int tokens3yellow = 0;
        int tokens4grey = 0;
        int tokens5black = 0;

        for (Arc arc : getInArcs()) {
            Place arcStartPlace = (Place) arc.getStartNode();
            TypeOfArc arcType = arc.getArcType();

            if (arcType != TypeOfArc.COLOR) { //dla zwykłych łuków
                int startPlaceTokens = arcStartPlace.getNonReservedTokensNumber();
                if (arcType == TypeOfArc.INHIBITOR) {
                    if (startPlaceTokens > arc.getWeight())
                        return false; //nieaktywna
                    else
                        continue; //aktywna (nie jest w danej chwili blokowana)
                } else if (arcType == TypeOfArc.EQUAL && startPlaceTokens != arc.getWeight()) { //DOKŁADNIE TYLE CO WAGA
                    return false;
                } else {
                    if (isFunctional) { //fast, no method
                        boolean status = FunctionsTools.getFunctionDecision(startPlaceTokens, arc, arc.getWeight(), this);
                        if (!status)
                            return false; //zwróc tylko jesli false
                    } else {
                        if (startPlaceTokens < arc.getWeight())
                            return false;
                    }
                }
            }

            req0red += arc.getColorWeight(0);
            req1green += arc.getColorWeight(1);
            req2blue += arc.getColorWeight(2);
            req3yellow += arc.getColorWeight(3);
            req4grey += arc.getColorWeight(4);
            req5black += arc.getColorWeight(5);

            tokens0red += arcStartPlace.getNonReservedColorTokensNumber(0);
            tokens1green += arcStartPlace.getNonReservedColorTokensNumber(1);
            tokens2blue += arcStartPlace.getNonReservedColorTokensNumber(2);
            tokens3yellow += arcStartPlace.getNonReservedColorTokensNumber(3);
            tokens4grey += arcStartPlace.getNonReservedColorTokensNumber(4);
            tokens5black += arcStartPlace.getNonReservedColorTokensNumber(5);
        }
        return req0red > tokens0red || req1green > tokens1green || req2blue > tokens2blue || req3yellow > tokens3yellow ||
                req4grey > tokens4grey || req5black > tokens5black;
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
                origin.reserveColorTokens(arc.getColorWeight(0), 0);
                origin.reserveColorTokens(arc.getColorWeight(1), 1);
                origin.reserveColorTokens(arc.getColorWeight(2), 2);
                origin.reserveColorTokens(arc.getColorWeight(3), 3);
                origin.reserveColorTokens(arc.getColorWeight(4), 4);
                origin.reserveColorTokens(arc.getColorWeight(5), 5);
            } else { //read arc / normal
                if (arc.getArcType() == TypeOfArc.READARC) {
                    if (GUIManager.getDefaultGUIManager().getSettingsManager().getValue("simTransReadArcTokenReserv").equals("0")) {
                        continue; //nie rezerwuj przez read-arc
                    } else {
                        origin.reserveTokens(arc.getWeight());
                    }
                } else { //normalny łuk
                    if (isFunctional) { //fast, no method!
                        //TODO:
                        FunctionContainer fc = getFunctionContainer(arc);
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
     *
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
     *
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
     *
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

    //**************************************************************************************
    //*********************************      TIME        ***********************************
    //**************************************************************************************

    /**
     * Metoda ustala dolny limit niezerowego czasu gotowości - EFT.
     *
     * @param value double - czas EFT
     */
    public void setEFT(double value) {
        if (value < 0) {
            this.TPN_eft = 0;
            return;
        }
        if (value > TPN_lft) {
            this.TPN_eft = TPN_lft;
            return;
        }
        this.TPN_eft = value;
    }

    /**
     * Na potrzeby wczytywania pliku projektu, bez porownania z LFT
     * @param value - double
     */
    public void forceSetEFT(double value) {
        if (value < 0) {
            this.TPN_eft = 0;
            return;
        }
        this.TPN_eft = value;
    }

    /**
     * Metoda pozwala odczytać przypisany czas EFT tranzycji.
     *
     * @return double - czas EFT
     */
    public double getEFT() {
        return this.TPN_eft;
    }

    /**
     * Metoda ustala górny limit nieujemnego czasu krytycznego - LFT.
     *
     * @param value double - czas LFT (deadline na uruchomienie)
     */
    public void setLFT(double value) {
        if (value < TPN_eft) {
            this.TPN_lft = TPN_eft;
            return;
        }

        this.TPN_lft = value;
    }

    /**
     * Metoda pozwala odczytać przypisany czas LFT tranzycji.
     *
     * @return double - czas LFT
     */
    public double getLFT() {
        return this.TPN_lft;
    }

    /**
     * Metoda pozwala ustawic czas uruchomienia tranzycji.
     *
     * @param value double - czas uruchomienia tranzycji
     */
    public void setTPNtimerLimit(double value) {
        TPNtimerLimit = value;
    }

    /**
     * Metoda zwraca aktualny czas uruchomienia.
     *
     * @return double - czas uruchomienia - pole FireTime
     */
    public double getTPNtimerLimit() {
        return TPNtimerLimit;
    }

    /**
     * Metoda zwraca aktualny zegar uruchomienia dla tranzycji.
     *
     * @return double - czas uruchomienia - pole FireTime
     */
    public double getTPNtimer() {
        return TPNtimer;
    }

    /**
     * Metoda pozwala ustawic zegar uruchomienia tranzycji.
     *
     * @param value double - czas uruchomienia tranzycji
     */
    public void setTPNtimer(double value) {
        TPNtimer = value;
    }

    /**
     * Metoda ustawia nowy czas trwania odpalenia dla tranzycji DPN.
     *
     * @param value double - nowy czas
     */
    public void setDPNduration(double value) {
        if (value < 0)
            DPNduration = 0;
        else
            DPNduration = value;
    }

    /**
     * Metoda zwraca ustawioną dla tranzycji DPN wartość duration.
     *
     * @return double - czas trwania odpalenia tranzycji
     */
    public double getDPNduration() {
        return DPNduration;
    }

    /**
     * Metoda ustawia nowy wewnętrzny timer dla czasu odpalenia dla tranzycji DPN.
     *
     * @param value double - nowa wartość zegara dla DPN
     */
    public void setDPNtimer(double value) {
        DPNtimer = value;
    }

    /**
     * Metoda zwraca aktualną wartość zegara odliczającego czas do odpalenia tranzycji DPN (produkcji tokenów).
     *
     * @return double durationTimer -
     */
    public double getDPNtimer() {
        return DPNtimer;
    }

    /**
     * Metoda pozwalająca stwierdzić, czy tranzycja DPN jest gotowa do produkcji tokenów.
     *
     * @return boolean - true, jeśli zegar DPN ma wartość równą ustalonemu czasowi DPN dla tranzycji
     */
    public boolean isDPNforcedToFire() {
        return DPNtimer >= DPNduration;
    }

    /**
     * Metoda informująca czy tranzycja TPN musi zostać uruchomiona.
     *
     * @return boolean - true, jeśli wewnętrzny zegar (!= -1) jest równy deadlinowi dla TPN
     */
    public boolean isTPNforcedToFired() {
        if (TPNtimerLimit != -1) {
            return TPNtimerLimit == TPNtimer;
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
     *
     * @return boolean - true, jeśli TPN aktywny
     */
    public boolean getTPNstatus() {
        return TPNactive;
    }

    /**
     * Metoda włącza lub wyłącza tryb DPN
     *
     * @param status boolean - true, jeśli tryb DPN ma być aktywny
     */
    public void setDPNstatus(boolean status) {
        DPNactive = status;
    }

    /**
     * Metoda zwraca stan aktywności trybu DPN
     *
     * @return boolean - true, jeśli DPN aktywny
     */
    public boolean getDPNstatus() {
        return DPNactive;
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

    //**************************************************************************************
    //*********************************     FUNCTIONS    ***********************************
    //**************************************************************************************

    /**
     * Metoda ustawia flagę tranzycji funkcyjnej.
     *
     * @param value boolean - true, jeśli tranzycja ma być funkcyjna
     */
    public void setFunctional(boolean value) {
        this.isFunctional = value;
    }

    /**
     * Metoda zwraca flagę funkcyjności tranzycji.
     *
     * @return boolean - true, jeśli funkcyjna
     */
    public boolean isFunctional() {
        return this.isFunctional;
    }

    /**
     * Metoda zwraca pełen wektor funkcyjny tranzycji. Przed jej wywołaniem należy upewnić się funkcją
     * checkFunctions(...), że wektor ten jest aktualny.
     *
     * @return ArrayList[FunctionContainer] - wektor funkcji
     */
    public ArrayList<FunctionContainer> accessFunctionsList() {
        return this.fList;
    }

    /**
     * Metoda weryfikuje wektor łuków funkcyjnych - usuwa łuki które już nie istnieją w rzeczywistych
     * połączeniach tranzycji oraz dodaje takie, których na liście funkcyjnej brakuje.
     *
     * @param arcs ArrayList[Arc] - wektor wszystkich łuków sieci
     */
    public void checkFunctions(ArrayList<Arc> arcs, ArrayList<Place> places) {
        int fSize = fList.size();
        ArrayList<Arc> fArcs = new ArrayList<>();
        //usuń funkcje związane z nieistniejącymi łukami
        for (int f = 0; f < fSize; f++) {
            FunctionContainer fc = fList.get(f);
            if (!arcs.contains(fc.arc)) {
                fList.remove(f);
                f--;
                fSize--;
            } else {
                fArcs.add(fc.arc); //lista łuków funkcyjnych
            }
        }

        ArrayList<Arc> inArcs = getInArcs();
        ArrayList<Arc> outArcs = getOutArcs();

        for (Arc arc : inArcs) {
            if (fArcs.contains(arc))
                continue;

            FunctionContainer fc = new FunctionContainer(this);
            int placeIndex = places.indexOf((Place)arc.getStartNode());
            fc.fID = "p" + placeIndex + "-->T";
            fc.arc = arc;
            fc.inTransArc = true;
            fList.add(fc);
        }

        for (Arc arc : outArcs) {
            if (fArcs.contains(arc))
                continue;

            FunctionContainer fc = new FunctionContainer(this);
            int placeIndex = places.indexOf((Place)arc.getEndNode());
            fc.fID = "T-->p" + placeIndex;
            fc.arc = arc;
            fc.inTransArc = false;
            fList.add(fc);
        }
    }

    /**
     * Metoda podmienia zapis funkcji w tranzycji.
     *
     * @param fID        String - identyfikator funkcji dla danej tranzycji
     * @param expression String - nowa forma funkcji
     * @param correct    boolean - true jeśli funkcja została zweryfikowana jako prawidłowa
     * @param enabled    boolean - true, jeśli funkcja ma być aktywna (np. w symulatorze)
     * @return boolean - true, jeśli znaleziono identyfikator i podmieniono funkcję
     */
    public boolean updateFunctionString(String fID, String expression, boolean correct, boolean enabled) {
        for (FunctionContainer fc : accessFunctionsList()) {
            if (fc.fID.equals(fID)) {
                fc.simpleExpression = expression;
                fc.correct = correct;
                fc.enabled = enabled;
                return true;
            }
        }
        return false;
    }

    /**
     * Metoda zwraca kontener z funkcją.
     *
     * @param fID String - identyfikator w ramach tranzycji
     * @return FunctionContainer - obiekt kontenera
     */
    public FunctionContainer getFunctionContainer(String fID) {
        for (FunctionContainer fc : accessFunctionsList()) {
            if (fc.fID.equals(fID)) {
                return fc;
            }
        }
        return null;
    }

    /**
     * /**
     * Metoda zwraca kontener z funkcją - szukanie po obiekcie łuku.
     *
     * @param arc Arc - łuk tranzycji
     * @return FunctionContainer - obiekt kontenera
     */
    public FunctionContainer getFunctionContainer(Arc arc) {
        for (FunctionContainer fc : accessFunctionsList()) {
            if (fc.arc.equals(arc)) {
                return fc;
            }
        }
        return null;
    }

    //**************************************************************************************
    //*********************************    STOCHASTIC    ***********************************
    //**************************************************************************************

    /**
     * Metoda zwraca podtyp SPN tranzycji.
     *
     * @return StochaticsType - podtyp tranzycji stochastycznej
     */
    public StochaticsType getSPNtype() {
        return this.stochasticType;
    }

    /**
     * Metoda ustawia podtyp SPN tranzycji.
     *
     * @param value TransitionType -  podtyp tranzycji stochastycznej
     */
    public void setSPNtype(StochaticsType value) {
        this.stochasticType = value;
    }

    /**
     * Metoda zwraca wartość firing rate na potrzeby symulacji SPN.
     *
     * @return double - wartość firing rate
     */
    public double getFiringRate() {
        return this.firingRate;
    }

    /**
     * Metoda ustawia nową wartość firing rate dla tranzycji w modelu SPN.
     *
     * @param firingRate double - nowa wartość
     */
    public void setFiringRate(double firingRate) {
        this.firingRate = firingRate;
    }

    /**
     * Metoda zwraca kontener danych SPN tranzycji.
     *
     * @return SPNtransitionData - kontener danych
     */
    public SPNtransitionData getSPNbox() {
        return this.SPNbox;
    }

    /**
     * Metoda ustawia nowy kontener danych SPN tranzycji.
     * param SPNbox SPNtransitionData - kontener danych
     */
    public void setSPNbox(SPNtransitionData SPNbox) {
        this.SPNbox = SPNbox;
    }


    public void setSPNprobTime(double time) {
        this.SPNprobTime = time;
    }

    public double getSPNprobTime() {
        return this.SPNprobTime;
    }

    /**
     * Metoda informujaca, czy tranzycja jest kolorowana
     *
     * @return boolean - true, jeśli colored, false jeśli nie
     */
    public boolean isColored() {
        return transType == TransitionType.CPNbasic;
    }

    /**
     * Metoda zwraca liczbę potrzebnych tokenów do produkcji (z danego koloru)
     *
     * @param i int - nr porzadkowy koloru, default 0, od 0 do 5
     * @return int - wymagana liczba tokenów danego koloru
     */
    public int getRequiredColoredTokens(int i) {
        return switch (i) {
            case 0 -> reqT0red;
            case 1 -> reqT1green;
            case 2 -> reqT2blue;
            case 3 -> reqT3yellow;
            case 4 -> reqT4gray;
            case 5 -> reqT5black;
            default -> reqT0red;
        };
    }

    /**
     * Metoda ustawia wymaganą liczbę tokenów danego koloru dla aktywacji tranzycji.
     *
     * @param tokens int - liczba tokenów
     * @param i      int - nr porządkowy koloru, default 0, od 0 do 5
     */
    public void setRequiredColoredTokens(int tokens, int i) {
        switch (i) {
            case 0 -> reqT0red = tokens;
            case 1 -> reqT1green = tokens;
            case 2 -> reqT2blue = tokens;
            case 3 -> reqT3yellow = tokens;
            case 4 -> reqT4gray = tokens;
            case 5 -> reqT5black = tokens;
            default -> reqT0red = tokens;
        }
    }

    /* Uprzejmie prosi się o nie pisane żadnego kodu niezwiązanego z xTPN POD tym komentarzem
       **************************************************************************************
       *********************************        xTPN      ***********************************
       **************************************************************************************
     */

    /**
     * Metoda resetuje zegary tranzycji XTPN.
     */
    public void resetTimeVariables_xTPN() {
        //TODO: pewnie coś, wyjdzie przy symulatorze
        tauAlpha_xTPN = -1.0;
        tauBeta_xTPN = -1.0;
        timer_Ualfa_XTPN = -1.0;
        timer_Vbeta_XTPN = -1.0;
        isActivated_xTPN = false;
        isProducing_xTPN = false;
    }

    /**
     * Metoda ustawia dolną wartość alfaMin dla xTPN.
     * @param value (<b>double</b>) czas alfaMin (=EFT)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setAlphaMin_xTPN(double value, boolean force) {
        if(force) {
            this.alphaMin_xTPN = value;
            return;
        }
        if (value < 0) {
            this.alphaMin_xTPN = 0.0;
            return;
        }
        if (value > alphaMax_xTPN) { //musi być mniejszy równy niż alphaU
            this.alphaMin_xTPN = alphaMax_xTPN;
            return;
        }
        this.alphaMin_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać dolną wartość alfaMin dla xTPN.
     * @return (<b>double</b>) - czas alfaMin.
     */
    public double getAlphaMin_xTPN() {
        return this.alphaMin_xTPN;
    }

    /**
     * Metoda ustawia górną wartość alfaMax dla xTPN.
     * @param value (<b>double</b>) czas alfaMax (=LFT)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setAlphaMax_xTPN(double value, boolean force) {
        if(force) {
            this.alphaMax_xTPN = value;
            return;
        }
        if (value < 0) {
            this.alphaMax_xTPN = -1.0; //domyślnie do redukcji -> classicalPN
            return;
        }
        if (value < alphaMin_xTPN) { //musi być większy równy niż alphaL
            this.alphaMax_xTPN = alphaMin_xTPN;
            return;
        }
        this.alphaMax_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać górną wartość alfaMax dla xTPN.
     * @return (double) : czas alfaMax.
     */
    public double getAlphaMax_xTPN() {
        return this.alphaMax_xTPN;
    }

    /**
     * Metoda ustawia dolną wartość betaMin dla xTPN.
     * @param value (<b>double</b>) czas betaMin (=DPN duration lower value)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setBetaMin_xTPN(double value, boolean force) {
        if(force) {
            this.betaMin_xTPN = value;
            return;
        }
        if (value < 0) {
            this.betaMin_xTPN = 0.0;
            return;
        }
        if (value > betaMax_xTPN) { //musi być mniejszy równy niż betaU
            this.betaMin_xTPN = betaMax_xTPN;
            return;
        }
        this.betaMin_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać dolną wartość betaMin dla xTPN.
     * @return (<b>double</b>) - czas betaMin.
     */
    public double getBetaMin_xTPN() {
        return this.betaMin_xTPN;
    }

    /**
     * Metoda ustawia dolną wartość betaMax dla xTPN.
     * @param value (<b>double</b>) czas betaMax (=DPN duration upper value)
     * @param force (<b>boolean</b>) czy wymusić wartość bez weryfikacji
     */
    public void setBetaMax_xTPN(double value, boolean force) {
        if(force) {
            this.betaMax_xTPN = value;
            return;
        }
        if (value < 0) {
            this.betaMax_xTPN = 0.0; //domyślnie do redukcji -> classical DPN
            return;
        }
        if (value < betaMin_xTPN) { //musi być większy równy niż betaL
            this.betaMax_xTPN = betaMin_xTPN;
            return;
        }
        this.betaMax_xTPN = value;
    }

    /**
     * Metoda pozwala odczytać górną wartość betaMax dla xTPN.
     * @return (<b>double</b>) - czas betaMax.
     */
    public double getBetaMax_xTPN() {
        return this.betaMax_xTPN;
    }

    /**
     * Metoda ustawia wartość docelową zegara U - tauAlpha tranzycji.
     * @param value (<b>double</b>) nowa wartość tauAlpha.
     */
    public void setTauAlpha_xTPN(double value) {
        tauAlpha_xTPN = value;
    }

    /**
     * Metoda modyfikuje wartość docelową zegara U - tauAlfa tranzycji.
     * @param delta (<b>double</b>) o ile zmienić zegar tauAlpha.
     */
    public void updateTauAlpha_xTPN(double delta) {
        tauAlpha_xTPN += delta;
    }

    /**
     * Metoda zwraca wartość docelową zegara U - tauAlfa tranzycji.
     * @return (<b>double</b>) aktualny czas tauAlpha.
     */
    public double getTauAlpha_xTPN() {
        return tauAlpha_xTPN;
    }

    /**
     * Metoda ustawia wartość docelową zegara V - tauBeta tranzycji.
     * @param value (<b>double</b>) nowa wartość tauBeta.
     */
    public void setTauBeta_xTPN(double value) {
        tauBeta_xTPN = value;
    }

    /**
     * Metoda modyfikuje wartość docelową zegara V - tauBeta tranzycji.
     * @param delta (<b>double</b>) o ile zmienić zegar tauBeta.
     */
    public void updateTauBeta_xTPN(double delta) {
        tauBeta_xTPN += delta;
    }

    /**
     * Metoda zwraca wartość docelową zegara V - tauBeta tranzycji.
     * @return (<b>double</b>) aktualny czas tauBeta.
     */
    public double getTauBeta_xTPN() {
        return tauBeta_xTPN;
    }

    /**
     * Metoda ustawia status tauAlpha i tauBeta - pokazywać czy nie.
     * @param status (<b>boolean</b>) true, jeśli zegary mają być pokazywane.
     */
    public void setTauTimersVisibility(boolean status) {
        tauTimersVisibility_XTPN = status;
    }

    /**
     * Metoda zwraca status zegarów zegarów alpha - pokazywać czy nie.
     * @return (<b>boolean</b>) - true, jeśli zegary mają być pokazywane.
     */
    public boolean isTauTimerVisible() {
        return tauTimersVisibility_XTPN;
    }

    /**
     * Metoda ustawia status zakresów zegarów alpha - pokazywać czy nie.
     * @param status (<b>boolean</b>) true, jeśli zakresy mają być pokazywane.
     */
    public void setAlphaRangeVisibility(boolean status) {
        alphaRangeVisibility_XTPN = status;
    }

    /**
     * Metoda zwraca status zakresów alpha - pokazywać czy nie.
     * @return (<b>boolean</b>) - true, jeśli zakresy alpha mają być pokazywane.
     */
    public boolean isAlphaRangeVisible() {
        return alphaRangeVisibility_XTPN;
    }

    /**
     * Metoda ustawia status zakresów zegarów beta - pokazywać czy nie.
     * @param status (<b>boolean</b>) true, jeśli zakresy beta mają być pokazywane.
     */
    public void setBetaRangeVisibility(boolean status) {
        betaRangeVisibility_XTPN = status;
    }

    /**
     * Metoda zwraca status zakresów beta - pokazywać czy nie.
     * @return (<b>boolean</b>) - true, jeśli zakresy beta mają być pokazywane.
     */
    public boolean isBetaRangeVisible() {
        return betaRangeVisibility_XTPN;
    }

    /**
     * Metoda ustawia wyświetlaną dokładność po przecinku.
     * @param value (<b>int</b>) nowa wartość liczby cyfr przecinku.
     */
    public void setFraction_xTPN(int value) {
        fractionDigits = value;
    }

    /**
     * Metoda zwraca wyświetlaną dokładność po przecinku.
     * @return (<b>int</b>) - aktualna wartość liczby cyfr przecinku.
     */
    public int getFraction_xTPN() {
        return fractionDigits;
    }

    /**
     * Metoda ustawia wartość docelową zegara U-alfa tranzycji.
     * @param value (<b>double</b>) - nowa wartość zegara U-alfa.
     */
    public void setTimerAlfa_XTPN(double value) {
        timer_Ualfa_XTPN = value;
    }

    /**
     * Metoda modyfikuje wartość docelową zegara U-alfa tranzycji.
     * @param delta (<b>double</b>) o ile zmienić zegar U-alfa.
     */
    public void updateTimerAlfa_XTPN(double delta) {
        timer_Ualfa_XTPN += delta;
    }

    /**
     * Metoda zwraca wartość docelową zegara U-alfa tranzycji.
     * @return (<b>double</b>) aktualny czas U-alfa.
     */
    public double getTimerAlfa_XTPN() {
        return timer_Ualfa_XTPN;
    }

    /**
     * Metoda ustawia wartość docelową zegara V-beta tranzycji.
     * @param value (<b>double</b>) nowa wartość zegara V-beta.
     */
    public void setTimerBeta_XTPN(double value) {
        timer_Vbeta_XTPN = value;
    }

    /**
     * Metoda modyfikuje wartość docelową zegara V-beta tranzycji.
     * @param delta (<b>double</b>) o ile zmienić zegar V-beta.
     */
    public void updateTimerBeta_XTPN(double delta) {
        timer_Vbeta_XTPN += delta;
    }

    /**
     * Metoda zwraca wartość docelową zegara V-beta tranzycji.
     * @return (<b>double</b>) aktualny czas V-beta.
     */
    public double getTimerBeta_XTPN() {
        return timer_Vbeta_XTPN;
    }

    /**
     * Metoda ustawia status aktywacji tranzycji xTPN. Jeśli na true, to ustawia false dla produkcji.
     * @param status (<b>boolean</b>) true, jeśli tranzycja jest aktywna.
     */
    public void setActivationStatusXTPN(boolean status) {
        isActivated_xTPN = status;

        if(status) { //jeśli true
            isProducing_xTPN = false; //zawsze odwrotność
        }
    }

    /**
     * Metoda zwraca status aktywacji tranzycji xTPN.
     * @return (<b>boolean</b>) - true, jeśli tranzycja jest aktywna.
     */
    public boolean isActivated_xTPN() {
        return isActivated_xTPN;
    }

    /**
     * Metoda ustawia status produkcji tranzycji xTPN. Jeśli na true, to ustawia false dla aktywacji.
     * @param status (<b>boolean</b>) true, jeśli tranzycja rozpoczęła produkcję.
     */
    public void setProductionStatus_xTPN(boolean status) {
        isProducing_xTPN = status;

        if(status) { //jeśli true
            isActivated_xTPN = false; //zawsze odwrotność
        }
    }

    /**
     * Metoda zwraca status status produkcji tranzycji xTPN.
     * @return (<b>boolean</b>) - true, jeśli tranzycja rozpoczęła produkcję.
     */
    public boolean isProducing_xTPN() {
        return isProducing_xTPN;
    }

    /**
     * Metoda włącza tryb XTPN dla tranzycji.
     * @param status (<b>boolean</b>) true, jeśli ma to być tranzycja XTPN.
     */
    public void setXTPNstatus(boolean status) {
        isXTPN = status;
    }

    /**
     * Metoda zwraca status tranzycji XTPN / wszystkie inne.
     * @return (<b>boolean</b>) true, jeśli to tranzycja typu XTPN.
     */
    public boolean isXTPNtransition() {
        return isXTPN;
    }

    /**
     * Metoda włącza tryb alfa-XTPN dla tranzycji.
     * @param status (<b>boolean</b>) true, jeśli tryb alfa-XTPN ma być aktywny
     */
    public void setAlphaXTPNstatus(boolean status) {
        alphaMode_xTPN = status;
        //setAlphaRangeStatus(status);
    }

    /**
     * Metoda zwraca status alfa tranzycji XTPN.
     * @return (<b>boolean</b>) - true, jeśli tryb alfa-XTPN aktywny.
     */
    public boolean isAlphaActiveXTPN() {
        return alphaMode_xTPN;
    }
    /**
     * Metoda włącza tryb beta-XTPN dla tranzycji.
     * @param status (<b>boolean</b>) true, jeśli tryb beta-XTPN ma być aktywny.
     */
    public void setBetaXTPNstatus(boolean status) {
        betaMode_xTPN = status;
    }

    /**
     * Metoda zwraca status beta tranzycji XTPN / wszystkie inne.
     * @return (<b>boolean</b>) - true, jeśli tryb beta-XTPN aktywny.
     */
    public boolean isBetaActiveXTPN() {
        return betaMode_xTPN;
    }

    /**
     * Metoda włącza tryb mass-action kinetics XTPN dla tranzycji.
     * @param status (<b>boolean</b>) true, jeśli tryb mass-action kinetics XTPN ma być aktywny.
     */
    public void setMassActionKineticsXTPNstatus(boolean status) {
        massActionKinetics = status;
    }

    /**
     * Metoda zwraca status trybu natychmiastowej tranzycji XTPN.
     * @return (<b>boolean</b>) - true, jeśli tryb natychmiastowy.
     */
    public boolean isImmediateXTPN() {
        return isImmediateXTPN;
    }

    /**
     * Metoda ustawia status trybu natychmiastowej tranzycji XTPN.
     * @param immediateXTPN (<b>boolean</b>) true, jeśli ma być natychmiastowa.
     */
    public void setImmediateXTPN(boolean immediateXTPN) {
        isImmediateXTPN = immediateXTPN;
    }

    /**
     * Metoda zwraca status trybu mass-action kinetics XTPN dla tranzycji.
     * @return (<b>boolean</b>) - true, jeśli tryb mass-action kinetics XTPN ma być aktywny
     */
    public boolean isMassActionKineticsActiveXTPN() {
        return massActionKinetics;
    }

    public boolean isInputTransition() {
        return getInArcs().size() == 0;
    }
    public boolean isOutputTransition() {
        return getOutArcs().size() == 0;
    }

    /**
     * [2022-07-12] Dła łuków: normal, inhibitor
     * Metoda pozwala sprawdzić, czy tranzycja XTPN może być aktywowana i w jaki sposób. Pomija produkujące - wtedy zwraca false.
     * @return (<b>boolean</b>) true, jeżeli aktywna
     */
    public boolean getActiveStatusXTPN() {
        if (offline || isProducing_xTPN())
            return false;

        double accuracy = SimulatorGlobals.calculationsAccuracy;

        for (Arc arc : getInArcs()) { //jeśli brak, to aktywna wejściowa
            Place arcStartPlace = (Place) arc.getStartNode();
            TypeOfArc arcType = arc.getArcType();
            int arcWeight = arc.getWeight();

            if (arcStartPlace.isGammaModeActiveXTPN()) { //czy miejsce typu XTPN
                if (arcStartPlace.accessMultiset().size() < arcWeight) {
                    //jeśli nie istnieje podzbiór aktywujący (bo sam multizbiór jest mniejszy niż waga łuku), to:
                    if(arcType == TypeOfArc.INHIBITOR) { //to dobrze, że nie ma zbioru, tranzycja wciąż aktywna
                        ;
                    } else {
                        return false; //nie ma co sprawdzać, za mały multizbiór
                    }
                } else { //multizbiór ma przynajmniej tyle tokenów ile wynosi waga łuku, sprawdzamy podzbiór aktywujący:
                    if (!isActivationMultiset(arcWeight, arcStartPlace.getGammaMin_xTPN(), arcStartPlace.accessMultiset(), accuracy)) {
                        //jeśli nie istnieje podzbiór aktywujący (powyżej), to:
                        if(arcType == TypeOfArc.INHIBITOR) { //to dobrze, że nie ma zbioru, tranzycja wciąż aktywna
                            ;
                        } else {
                            return false; //brak multizbioru aktywującego, nieaktywna
                        }
                    }
                }
            } else { //miejsce traktowane jak klasyczne
                int startPlaceTokens = arcStartPlace.getNonReservedTokensNumber();
                if (arcWeight > startPlaceTokens) {
                    if(arcType == TypeOfArc.INHIBITOR) {
                        ; //wciąż aktywna
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
        //jeśli wciąż tutaj dojdziemy, to znaczy, że aktywna
    }

    /**
     * Deaktywacja tranzycji XTPN. Kiedy przestaje być aktywna z powodu utraty tokenó LUB po wyprodukowaniu
     */
    public void deactivateXTPN() {
        setActivationStatusXTPN(false);
        if(alphaMode_xTPN) {
            setTimerAlfa_XTPN(-1.0);
            setTauAlpha_xTPN(-1.0);
        }
        if(betaMode_xTPN) {
            setTimerBeta_XTPN(-1.0);
            setTauBeta_xTPN(-1.0);
        }
    }

    /**
     * Metoda sprawdza czy istnieje podzbiór aktywujący o zadanych parametrach.
     * @param size (<b>int</b>) minimalny rozmiar zbioru.
     * @param gammaMin (<b>double</b>) gammaMinimum dla tranzycji.
     * @param multiset (<b>ArrayList[Double]</b>)
     * @param accuracy (<b>double</b>) dokładność obliczeń.
     * @return (<b>double</b>) - true, jeżeli istnieje podzbiór aktywujący
     */
    private boolean isActivationMultiset(int size, double gammaMin, ArrayList<Double> multiset, double accuracy) {
        int counter = 0;
        for(Double kappa : multiset) {
            if(kappa + accuracy > gammaMin) {
                counter++;
                if(counter == size)
                    return true;
            }
        }
        return false;
    }
}
