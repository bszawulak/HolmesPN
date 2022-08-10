package holmes.petrinet.elements;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Line2D;
import java.io.Serial;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.varia.NetworkTransformations;

/**
 * Klasa implementująca łuk w sieci Petriego. Przechowuje referencje
 * lokalizacji na swoim początku i  końcu (istotne: lokacji, nie bezpośrednio
 * wierzchołków). Poprzez owe lokacje można uzyskać dostęp do wierzchołków,
 * do których należy.
 */
public class Arc extends PetriNetElement {
    @Serial
    private static final long serialVersionUID = 5365625190238686098L;
    private ElementLocation locationStart;
    private ElementLocation locationEnd = null;
    private Point tempEndPoint = null;
    private boolean selected = false;
    private boolean isCorrect = false;
    private int weight = 1;
    public boolean isTransportingTokens = false;
    private int graphicalSimulationSteps = 0;
    private boolean simulationForwardDirection = true;

    private ArrayList<Point> breakPoints;

    //colors:
    private int weight1green = 0;
    private int weight2blue = 0;
    private int weight3yellow = 0;
    private int weight4grey = 0;
    private int weight5black = 0;

    private boolean isColorChanged;
    private Color arcColorValue;

    //read-arc parameters:
    private Arc pairedArc;
    private boolean isMainArcOfPair = false;
    private TypeOfArc arcType;
    public ArrayList<Color> layers = new ArrayList<>();

    public boolean qSimForcedArc = false; //czy łuk ma być wzmocniony
    public Color qSimForcedColor = Color.BLACK; //kolor wzmocnienia

    private int memoryOfArcWeight = -1;
    //comparison:
    private boolean isBranchEnd = false;


    //XTPN:
    private boolean isXTPN = false;
    private boolean isXTPNinhibitor = false;
    private boolean isXTPNact = false;
    private boolean isXTPNprod = false;

    /**
     * NORMAL, READARC, INHIBITOR, RESET, EQUAL, META_ARC, COLOR, XTPN
     */
    public enum TypeOfArc {NORMAL, READARC, INHIBITOR, RESET, EQUAL, META_ARC, COLOR}

    /**
     * Konstruktor obiektu klasy Arc - chwilowo nieużywany.
     * @param startPosition ElementLocation - lokalicja żródła łuku
     * @param endPosition   ElementLocation - lokalicja celu łuku
     */
    public Arc(ElementLocation startPosition, ElementLocation endPosition, TypeOfArc type) {
        this(startPosition, type);

        this.setEndLocation(endPosition);
        this.setID(IdGenerator.getNextId());
        this.lookForArcPair();
    }

    /**
     * Konstruktor obiektu klasy Arc - mousePressed(MouseEvent) - używany w momencie wybrania prawidłowego (!)
     * wierzchołka docelowego dla łuku.
     * @param arcId         int - identyfikator łuku
     * @param startPosition ElementLocation - lokacja źródła łuku
     * @param endPosition   ElementLocation - lokacja celu łuku
     */
    public Arc(int arcId, ElementLocation startPosition, ElementLocation endPosition, TypeOfArc type) {
        this(startPosition, type);

        this.setEndLocation(endPosition);
        this.setID(arcId);
        this.lookForArcPair();
    }

    /**
     * Konstruktor obiektu klasy Arc - odczyt sieci z pliku
     * @param startPosition ElementLocation - lokacja źródła łuku
     * @param endPosition   ElementLocation - lokacja celu łuku
     * @param comment       String - komentarz
     * @param weight        int - waga łuku
     */
    public Arc(ElementLocation startPosition, ElementLocation endPosition, String comment, int weight, TypeOfArc type) {
        this(startPosition, type);

        this.setID(IdGenerator.getNextId());
        this.setEndLocation(endPosition);
        this.checkIsCorect(endPosition);
        this.setComment(comment);
        this.setWeight(weight);
        this.lookForArcPair();
    }

    /**
     * Konstruktor obiektu klasy Arc - bez ID, TYLKO na potrzeby rysowania konturu w momencie rozpoczęcia
     * rysowania (prowadzenia) łuku do miejsca docelowego.
     * @param startPosition ElementLocation - lokalizacja źródła łuku
     */
    public Arc(ElementLocation startPosition, TypeOfArc type) {
        this.arcType = type;
        this.setStartLocation(startPosition);
        this.setEndPoint(startPosition.getPosition());
        this.setType(PetriNetElementType.ARC);

        this.breakPoints = new ArrayList<>();
    }

    /**
     * Metoda sprawdza, czy aktualny łuk jest łukiem odczytu (read-arc).
     * Jeśli tak, ustala wartość obiektu łuku
     */
    private void lookForArcPair() {
        if (this.getArcType() == TypeOfArc.META_ARC)
            return;

        if (this.getArcType() != TypeOfArc.NORMAL) { //LOAD PROJECT SUBROUTINE
            if (this.getArcType() == TypeOfArc.READARC) {
                for (Arc a : this.getEndLocation().getOutArcs()) {
                    if (a.getEndLocation() == this.getStartLocation()) {
                        if (a.getArcType() != TypeOfArc.READARC)
                            continue; //load project purpose

                        a.setMainArcOfPair(true);
                        a.setPairedArc(this);
                        this.setPairedArc(a);
                    }
                }
            }

            handleComplexArcGraphics(null);
            return;
        }

        ArrayList<Arc> candidates = this.getEndLocation().getOutArcs();
        for (Arc a : candidates) {
            if (a.getEndLocation() == this.getStartLocation()) {
                if (a.getArcType() != TypeOfArc.NORMAL) {//tylko normal + normal = readarc
                    handleComplexArcGraphics(a);
                    continue;

                }
                a.setMainArcOfPair(true);
                a.setPairedArc(this);
                this.setPairedArc(a);
            }
        }
        //TODO: flaga readarc?
    }

    /**
     * Rozsuwa łuki (łamiąc je) względem siebie.
     * @param arc Arc - inny łuk niż normal idący w drugą stronę
     */
    private void handleComplexArcGraphics(Arc arc) {
        if (this.breakPoints.size() == 0) {
            Point startP = getStartLocation().getPosition();
            Point endP = getEndLocation().getPosition();

            Point breakPoint = new Point(((startP.x + endP.x) / 2) + 15, ((startP.y + endP.y) / 2) + 15);
            breakPoints.add(breakPoint);
        }

        if (arc == null)
            return;

        if (arc.breakPoints.size() == 0) {
            Point startP = arc.getStartLocation().getPosition();
            Point endP = arc.getEndLocation().getPosition();

            Point breakPoint = new Point(((startP.x + endP.x) / 2) - 15, ((startP.y + endP.y) / 2) - 15);
            arc.accessBreaks().add(breakPoint);
        }
    }

    /**
     * Metoda zwracająca wagę łuku.
     * @return int - waga łuku
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Metoda pozwalająca ustawić wagę łuku.
     * @param weight int - waga łuku
     */
    public void setWeight(int weight) {
        this.weight = weight;
        //if (pairedArc != null && isMainArcOfPair)
        //	pairedArc.setWeight(weight);
    }

    /**
     * Metoda zwracająca wagę łuku kolorowego.
     * @param i int - nr porządkowy koloru, default 0, od 0 do 5
     * @return int - waga dla koloru
     */
    public int getColorWeight(int i) {
        return switch (i) {
            case 1 -> weight1green;
            case 2 -> weight2blue;
            case 3 -> weight3yellow;
            case 4 -> weight4grey;
            case 5 -> weight5black;
            default -> weight;
        };
    }

    /**
     * Metoda pozwalająca ustawić wagę kolorowego łuku.
     * @param w int - waga łuku
     * @param i int - nr porządkowy koloru, default 0, od 0 do 5
     */
    public void setColorWeight(int w, int i) {
        switch (i) {
            case 1 -> this.weight1green = w;
            case 2 -> this.weight2blue = w;
            case 3 -> this.weight3yellow = w;
            case 4 -> this.weight4grey = w;
            case 5 -> this.weight5black = w;
            default -> this.weight = w;
        }
    }

    public void setColor(boolean isColorChanged, Color arcColorValue) {
        this.isColorChanged = isColorChanged;
        this.arcColorValue = arcColorValue;
    }

    public boolean isColorChanged() {
        return isColorChanged;
    }

    public Color getArcNewColor() {
        return arcColorValue;
    }

    /**
     * Metoda zwraca komentarz związany z łukiem.
     * @return comment String - komentarz do łuku
     */
    public String getComment() {
        return comment;
    }

    /**
     * Metoda ustawiająca komentarz dla łuku.
     * @param com String - komentarz do łuku
     */
    public void setComment(String com) {
        comment = com;
        if (pairedArc != null && isMainArcOfPair)
            pairedArc.setComment(com);
    }

    /**
     * Metoda pozwala pobrać wierzchołek początkowy łuku.
     * @return Node - wierzchołek wejściowy łuku
     */
    public Node getStartNode() {
        return this.locationStart.getParentNode();
    }

    /**
     * Metoda pozwala pobrać wierzchołek końcowy łuku.
     * @return Node - wierzchołek wyjściowy łuku
     */
    public Node getEndNode() {
        if (this.locationEnd != null)
            return this.locationEnd.getParentNode();
        return null;
    }

    /**
     * Metoda pozwala pobrać identyfikator arkusza, na którym znajduje się łuk.
     * @return int - identyfikator arkusza
     */
    public int getLocationSheetId() {
        return this.locationStart.getSheetID();
    }

    /**
     * Metoda pozwala obliczyć długość łuku na arkuszu w pikselach.
     * @return double - długość łuku
     */
    public double getWidth() {
        Point A = this.getStartLocation().getPosition();
        Point B = this.getEndLocation().getPosition();
        return Math.hypot(A.x - B.x, A.y - B.y);
    }

    /**
     * Metoda pozwala narysować token na łuku w czasie symulacji.
     * @param g       Graphics2D - grafika 2D
     * @param sheetId int - identyfikator arkusza
     */
    public void drawSimulationMovingToken(Graphics2D g, int sheetId) {
        ElementDraw.drawMovingToken(g, sheetId, this);
    }

    /**
     * Metoda przechodzi do kolejnego kroku rysowania symulacji na łuku. W zasadzie to
     * głównie zwiększa simulationStep, który odpowiada za liczbę 'klatek' przepływu tokenu.
     */
    public void incrementSimulationStep() {
        if (!this.isTransportingTokens)
            return;

        int GRAPHICAL_STEPS_COUNTER = GUIManager.getDefaultGUIManager().simSettings.getArcGraphicDelay();
        this.graphicalSimulationSteps++;

        if (this.getGraphicalSimulationSteps() > GRAPHICAL_STEPS_COUNTER) {
            this.setGraphicalSimulationSteps(0);
            this.setTransportingTokens(false);
        }
    }

    /**
     * Metoda rysująca łuk na danym arkuszu przy zmienionych rozmiarach arkusza.
     * @param g       Graphics2D - grafika 2D
     * @param sheetId int - identyfikator arkusza
     * @param zoom    int - zoom, unused
     * @param eds     ElementDrawSettings - ustawienia rysowania
     */
    public void draw(Graphics2D g, int sheetId, int zoom, ElementDrawSettings eds) {
        ElementDraw.drawArc(this, g, sheetId, zoom, eds);
    }

    /**
     * Metoda pozwala sprawdzić, czy łuk jest poprawny.
     * @return boolean - true, jeśli łuk jest poprawny; false w przeciwnym wypadku
     */
    public boolean getIsCorect() {
        return this.isCorrect;
    }

    /**
     * Metoda pozwala sprawdzić, czy łuk byłby poprawny dla danej lokalizacji wierzchołka wyjściowego.
     * @param e ElementLocation - lokalizacja wierzchołka wyjściowego
     * @return boolean - true, jeśli łuk byłby poprawny; false w przeciwnym wypadku
     */
    public boolean checkIsCorect(ElementLocation e) {
        this.isCorrect = e != null
                && e.getParentNode().getType() != this.getStartLocation().getParentNode().getType()
                && e != this.getStartLocation();
        return this.isCorrect;
    }

    /**
     * Metoda pozwala ustawić punkt lokacji wierzchołka wyjściowego łuku.
     * @param p Point - punkt lokalizacji wierzchołka wyjściowego
     */
    public void setEndPoint(Point p) {
        this.tempEndPoint = p;
    }

    public Point getTempEndPoint() {
        return this.tempEndPoint;
    }

    /**
     * Metoda pozwala pobrać stan zaznaczenia łuku.
     * @return boolean - true, jeśli łuk jest zaznaczony; false w przeciwnym wypadku
     */
    public boolean getSelected() {
        return selected;
    }

    /**
     * Metoda pozwala sprawdzić czy łuk zostanie zaznaczony.
     * @return true, jeśli łuk zostanie zaznaczony; false w przeciwnym wypadku
     */
    public boolean checkSelection() {
        if (this.locationEnd == null || this.locationStart == null)
            return false;
        setSelected(this.locationEnd.isSelected() && this.locationStart.isSelected());
        return this.getSelected();
    }

    /**
     * Metoda pozwala ustawić zaznaczenie łuku.
     * @param select boolean - wartość zaznaczenia łuku
     */
    public void setSelected(boolean select) {
        this.selected = select;
    }

    /**
     * Metoda pozwala sprawdzić, czy punkt jest częcią łuku.
     * @param P Point - punkt (x,y)
     * @return boolean - true, jeśli łuk jest częcią łuku; false w przeciwnym wypadku
     */
    public boolean checkIntersection(Point P) {
        int breaks = breakPoints.size();
        if (breaks > 0) {
            Point start = getStartLocation().getPosition();
            Point b0 = breakPoints.get(0);
            if (Line2D.ptSegDist(start.x, start.y, b0.x, b0.y, P.x, P.y) <= 3) //piewszy odcinek
                return true;

            for (int i = 1; i < breaks; i++) {
                if (Line2D.ptSegDist(breakPoints.get(i - 1).x, breakPoints.get(i - 1).y, breakPoints.get(i).x, breakPoints.get(i).y, P.x, P.y) <= 3) //piewszy odcinek
                    return true;
            }

            Point bFinal = breakPoints.get(breaks - 1);
            Point end = getEndLocation().getPosition();

            return Line2D.ptSegDist(bFinal.x, bFinal.y, end.x, end.y, P.x, P.y) <= 3;
        } else {
            Point A = getStartLocation().getPosition();
            Point B = getEndLocation().getPosition();
            return Line2D.ptSegDist(A.x, A.y, B.x, B.y, P.x, P.y) <= 3;
        }
    }

    /**
     * Metoda pozwala pobrać lokację wierzchołka wejściowego łuku.
     * @return startLocation ElementLocation - lokalizacja wierzchołka wejściowego łuku
     */
    public ElementLocation getStartLocation() {
        return locationStart;
    }

    /**
     * Metoda pozwala ustawić lokalizację wierzchołka wejściowego łuku.
     * @param startLocation ElementLocation - lokalizacja wierzchołka wejściowego
     */
    private void setStartLocation(ElementLocation startLocation) {
        this.locationStart = startLocation;

        if (this.arcType == TypeOfArc.META_ARC) {
            this.locationStart.accessMetaOutArcs().add(this);
        } else {
            this.locationStart.addOutArc(this);
        }
    }

    /**
     * Jak setStartLocation, z tym, że nie dodaje łuku do listy łuków obiektu locationStart.
     * @param startLocation ElementLocation - nowy element location. Okrętu się pan spodziewałeś?
     */
    public void modifyStartLocation(ElementLocation startLocation) {
        this.locationStart = startLocation;
    }

    /**
     * Metoda pozwala ustawić lokację wierzchołka wyjściowego łuku.
     * @param elementLocation ElementLocation - lokalizacja wierzchołka wyjściowego
     */
    private void setEndLocation(ElementLocation elementLocation) {
        if (elementLocation == null)
            return;
        this.locationEnd = elementLocation;

        if (this.arcType == TypeOfArc.META_ARC) {
            this.locationEnd.accessMetaInArcs().add(this);
        } else {
            this.locationEnd.addInArc(this);
        }

        this.tempEndPoint = null;
        this.isCorrect = true;
    }

    /**
     * Działa jak setEndLocation, ale nie dodaje łuku do listy łuków obiektu locationEnd.
     * @param elementLocation ElementLocation - nowy element location. Okrętu się pan spodziewałeś?
     */
    public void modifyEndLocation(ElementLocation elementLocation) {
        this.locationEnd = elementLocation;
    }

    /**
     * Metoda pozwala pobrać lokalizację wierzchołka wyjściowego łuku.
     * @return ElementLocation - lokalizacja wierzchołka wyjściowego łuku
     */
    public ElementLocation getEndLocation() {
        return locationEnd;
    }

    /**
     * Usuwa łuk z referencji lokacji obu wierzchołków (wejściowego i
     * wyjściowego) łuku (odłącza łuk od wierzchołków).
     */
    public void unlinkElementLocations() {
        if (arcType == TypeOfArc.META_ARC) {
            if (this.locationStart != null)
                this.locationStart.accessMetaOutArcs().remove(this);
            if (this.locationEnd != null)
                this.locationEnd.accessMetaInArcs().remove(this);
        } else {
            if (this.locationStart != null)
                this.locationStart.removeOutArc(this);
            if (this.locationEnd != null)
                this.locationEnd.removeInArc(this);
        }
    }

    /**
     * Metoda pozwala sprawdzić, czy łuk aktualnie transportuje tokeny.
     * @return boolean - true, jeśli łuk transportuje tokeny; false w przeciwnym wypadku
     */
    public boolean isTransportingTokens() {
        return isTransportingTokens;
    }

    /**
     * Metoda pozwala ustawić, czy łuk aktualnie transportuje tokeny.
     * @param isTransportingTokens boolean - wartość określająca, czy łuk transportuje aktualnie tokeny
     */
    public void setTransportingTokens(boolean isTransportingTokens) {
        this.isTransportingTokens = isTransportingTokens;
        this.setGraphicalSimulationSteps(0);
        if (!isTransportingTokens) {
            if (isSimulationForwardDirection()) {
                if (getStartNode().getType() == PetriNetElementType.TRANSITION)
                    ((Transition) getStartNode()).setLaunching(false);
            } else {
                if (getEndNode().getType() == PetriNetElementType.TRANSITION)
                    ((Transition) getEndNode()).setLaunching(false);
            }
        }
    }

    /**
     * Metoda pozwala pobrać aktualny krok wizualizacji symulacji.
     * @return (<b>int</b>) - numer aktualnego kroku wizualizacji symulacji
     */
    public int getGraphicalSimulationSteps() {
        return graphicalSimulationSteps;
    }

    /**
     * Metoda pozwala ustawić aktualny krok wizualizacji symulacji.
     * @param value (<b>int</b>) numer kroku symulacji.
     */
    private void setGraphicalSimulationSteps(int value) {
        this.graphicalSimulationSteps = value;
    }

    /**
     * Metoda pozwala sprawdzić, czy symulacja zachodzi zgodnie zeskierowaniem łuku (do przodu).
     * @return boolean - true, jeśli symulacja zachodzi zgodnie ze skierowaniem łuku (do przodu);
     * false  w przeciwnym wypadku
     */
    public boolean isSimulationForwardDirection() {
        return simulationForwardDirection;
    }

    /**
     * Metoda pozwala ustawić kierunek wizualizacji symulacji na łuku.
     * @param simulationForwardDirection boolean - true dla symulacji 'do przodu';
     *                                   false w przeciwnym wypadku
     */
    public void setSimulationForwardDirection(boolean simulationForwardDirection) {
        this.simulationForwardDirection = simulationForwardDirection;
    }

    /**
     * Metoda zwracająca łuk odczytu dla danego łuku.
     * @return Arc - łuk odczytu
     */
    public Arc getPairedArc() {
        return pairedArc;
    }

    /**
     * Metoda ustawia wartość pairedArc jeśli łuk jest łukiem odczytu.
     * @param pairedArc Arc - łuk odczytu
     */
    private void setPairedArc(Arc pairedArc) {
        if (this.getArcType() == TypeOfArc.META_ARC)
            return;

        this.pairedArc = pairedArc;
        this.arcType = TypeOfArc.READARC;
    }

    /**
     * Metoda informuje, czy łuk jest głównym łukiem z pary (read-arc)
     * @return boolean - true jeżeli łuk jest głównym z pary; false w przeciwnym wypadku
     */
    public boolean isMainArcOfPair() {
        return isMainArcOfPair;
    }

    /**
     * Metoda pozwala ustalić wartość flagi, czy łuk jest głównym w parze (read-arc)
     * @param isMainArcOfPair boolean - true jeśli jest; false w przeciwnym wypadku
     */
    private void setMainArcOfPair(boolean isMainArcOfPair) {
        this.isMainArcOfPair = isMainArcOfPair;
    }

    /**
     * Metoda zwraca typ łuku.
     * @return TypesOfArcs
     */
    public TypeOfArc getArcType() {
        return arcType;
    }

    /**
     * Tylko do użytku wczytywania danych: ustawia typ łuku.
     * @param type TypesOfArcs - typ łuku
     */
    public void setArcType(TypeOfArc type) {
        arcType = type;
    }

    //****************************************************************************************************
    //************************************     BREAKING BAD     ******************************************
    //****************************************************************************************************

    /**
     * Uzyskanie dostępu do tablicy punktów łamiących.
     * @return ArrayList[Point] - wektor punktów
     */
    public ArrayList<Point> accessBreaks() {
        return this.breakPoints;
    }

    /**
     * Dodaje punkt łamiący dla łuku.
     * @param breakP Point - obiekt współrzędnych
     */
    public void addBreakPoint(Point breakP) {
        this.breakPoints.add(breakP);
    }

    /**
     * Czyści do zera wektor punktów łamiących.
     */
    public void clearBreakPoints() {
        this.breakPoints.clear();
    }

    public void updateAllBreakPointsLocations(Point delta) {
        for (Point breakP : breakPoints) {
            breakP.setLocation(breakP.x + delta.x, breakP.y + delta.y);
        }
    }

    public void updateAllBreakPointsLocationsNetExtension(boolean magnify) {
        for (Point breakP : breakPoints) {
            double oldX = breakP.x;
            double oldY = breakP.y;
            if(magnify) {
                oldX *= 1.1;
                oldY *= 1.1;
            } else {
                oldX /= 1.1;
                oldY /= 1.1;
            }
            breakP.setLocation((int)oldX, (int)oldY);
        }
    }

    public void alignBreakPoints() {
        for (Point breakP : breakPoints) {
            breakP.setLocation(NetworkTransformations.alignToGrid(breakP) );
        }
    }

    /**
     * Zwraca punkt łamiący łuk, o ile kliknięto w jego pobliżu
     * @param mousePt Point - tu kliknięto myszą
     * @return Point - punkt łamiący łuku (jeśli istnieje w pobliżu mousePt)
     */
    public Point checkBreakIntersection(Point mousePt) {
        for (Point breakP : breakPoints) {
            if (breakP.x - 5 < mousePt.x && breakP.y - 5 < mousePt.y
                    && breakP.x + 5 > mousePt.x && breakP.y + 5 > mousePt.y)
                return breakP;
        }
        return null;
    }

    /**
     * Dodaje nowy punkt łamiący łuku, najpierw sprawdza który odcinek łuku podzielić.
     * @param breakP Point - punkt kliknięty NA łuku (zapewnione przed wywołaniem tej metody!)
     */
    public void createNewBreakPoint(Point breakP) {
        Point start = this.getStartLocation().getPosition();
        int breaks = breakPoints.size();
        if (breaks == 0) {
            addBreakPoint(breakP);
        } else {
            int whereToInsert = 0;
            Point b0 = breakPoints.get(0);
            if (Line2D.ptSegDist(start.x, start.y, b0.x, b0.y, breakP.x, breakP.y) <= 3) {//piewszy odcinek
                breakPoints.add(whereToInsert, breakP);
                return;
            }

            for (int i = 1; i < breaks; i++) {
                whereToInsert++;
                if (Line2D.ptSegDist(breakPoints.get(i - 1).x, breakPoints.get(i - 1).y, breakPoints.get(i).x, breakPoints.get(i).y, breakP.x, breakP.y) <= 3) {
                    breakPoints.add(whereToInsert, breakP);
                    return;
                }
            }
            //jeśli dotąd żaden odcinek, to wstawiamy na koniec listy:
            breakPoints.add(breakP);
        }
    }

    /**
     * Usuwa podany punkt łamiący łuku, tj. najbliższy do breakP.
     * @param breakP Point - tu kliknięto myszą, najpierw metoda sprawdzi, czy blisko tego jest break point
     */
    public void removeBreakPoint(Point breakP) {
        Point toRemove = checkBreakIntersection(breakP);
        if (toRemove != null) {
            breakPoints.remove(toRemove);
        }
    }

    public boolean isXTPN() {
        return isXTPN;
    }

    public void setXTPNstatus(boolean status) {
        isXTPN = status;
    }

    public boolean isXTPNinhibitor() {
        return isXTPNinhibitor;
    }

    public void setXTPNinhibitorStatus(boolean status) {
        isXTPNinhibitor = status;
    }

    public void setXTPNactStatus(boolean value) {
        isXTPNact = value;
    }

    public boolean getXTPNactStatus() {
        return isXTPNact;
    }

    public void setXTPNprodStatus(boolean value) {
        isXTPNprod = value;
    }

    public boolean getXTPNprodStatus() {
        return isXTPNprod;
    }

    //********************************************************************************************************
    //********************************************************************************************************
    //********************************************************************************************************

    /**
     * Metoda zwracająca dane o łuku w formie łańcucha znaków.
     * @return String - łańcuch znaków informacji o łuku sieci
     */
    public String toString() {
        PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
        String startNode = "";
        int startNodeLoc = -1;
        int startELLoc = -1;
        int startNodeID = -1;
        if (this.getStartLocation() != null) {
            Node node = this.getStartLocation().getParentNode();
            if (node != null) {
                if (node instanceof Place) {
                    startNode = "P";
                    startNodeLoc = pn.getPlaces().indexOf(node);
                } else if (node instanceof Transition) {
                    startNode = "T";
                    startNodeLoc = pn.getTransitions().indexOf(node);
                } else if (node instanceof MetaNode) {
                    startNode = "M";
                    startNodeLoc = pn.getMetaNodes().indexOf(node);
                }
                startELLoc = node.getElementLocations().indexOf(this.getStartLocation());
                startNodeID = node.getID();
            }
        }
        String endNode = "";
        int endNodeLoc = -1;
        int endELLoc = -1;
        int endNodeID = -1;
        if (this.getEndLocation() != null) {
            Node node = this.getEndLocation().getParentNode();
            if (node != null) {
                if (node instanceof Place) {
                    endNode = "P";
                    endNodeLoc = pn.getPlaces().indexOf(node);
                } else if (node instanceof Transition) {
                    endNode = "T";
                    endNodeLoc = pn.getTransitions().indexOf(node);
                } else if (node instanceof MetaNode) {
                    endNode = "M";
                    endNodeLoc = pn.getMetaNodes().indexOf(node);
                }
                endELLoc = node.getElementLocations().indexOf(this.getEndLocation());
                endNodeID = node.getID();
            }
        }

        return " ArcType: " + arcType.toString()
                + " " + startNode + startNodeLoc + "(" + startELLoc + ") [gID:" + startNodeID + "]  ==>  "
                + " " + endNode + endNodeLoc + "(" + endELLoc + ") [gID:" + endNodeID + "]";
    }


    @SuppressWarnings("unused")
    public int getMemoryOfArcWeight() {
        return memoryOfArcWeight;
    }

    public void setMemoryOfArcWeight(int memoryOfArcWeight) {
        this.memoryOfArcWeight = memoryOfArcWeight;
    }

    public boolean isBranchEnd() {
        return isBranchEnd;
    }

    public void setBranchEnd(boolean branchEnd) {
        isBranchEnd = branchEnd;
    }
}
