package holmes.petrinet.elements;

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
import holmes.petrinet.elements.containers.ArcColoredContainer;
import holmes.petrinet.elements.containers.ArcDecompContainer;
import holmes.petrinet.elements.containers.ArcQSimContainer;
import holmes.petrinet.elements.containers.ArcXTPNContainer;
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
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private TypeOfArc arcType;
    private ElementLocation locationStart;
    private ElementLocation locationEnd = null;
    private Point tempEndPoint = null;
    private ArrayList<Point> breakPoints;
    private boolean selected = false;
    private boolean isCorrect = false;
    private int weight = 1;
    public boolean isTransportingTokens = false;
    private int graphicalSimulationSteps = 0;
    private boolean simulationForwardDirection = true;
    //read-arc parameters:
    private Arc pairedArc;
    private boolean isMainArcOfPair = false;


    /** Dekompozycja do wora, wór do jeziora. */
    public ArcDecompContainer arcDecoBox = new ArcDecompContainer();
    public ArcXTPNContainer arcXTPNbox = new ArcXTPNContainer();
    private ArcColoredContainer arcColoredBox = new ArcColoredContainer();
    public ArcQSimContainer arcQSimBox = new ArcQSimContainer();

    /** NORMAL, READARC, INHIBITOR, RESET, EQUAL, META_ARC, COLOR, XTPN */
    public enum TypeOfArc {NORMAL, READARC, INHIBITOR, RESET, EQUAL, META_ARC, COLOR}

    /**
     * Konstruktor obiektu klasy Arc - chwilowo nieużywany.
     * @param startPosition (<b>ElementLocation</b>) lokalicja żródła łuku.
     * @param endPosition (<b>ElementLocation</b>) lokalicja celu łuku.
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
     * @param arcId (<b>int</b>) identyfikator łuku.
     * @param startPosition (<b>ElementLocation</b>) lokacja źródła łuku.
     * @param endPosition (<b>ElementLocation</b>) lokacja celu łuku.
     */
    public Arc(int arcId, ElementLocation startPosition, ElementLocation endPosition, TypeOfArc type) {
        this(startPosition, type);

        this.setEndLocation(endPosition);
        this.setID(arcId);
        this.lookForArcPair();
    }

    /**
     * Konstruktor obiektu klasy Arc - odczyt sieci z pliku.
     * @param startPosition (<b>ElementLocation</b>) lokacja źródła łuku.
     * @param endPosition (<b>ElementLocation</b>) lokacja celu łuku.
     * @param comment (<b>String</b>) komentarz.
     * @param weight (<b>int</b>) waga łuku.
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
     * @param startPosition (<b>ElementLocation</b>) lokalizacja źródła łuku.
     */
    public Arc(ElementLocation startPosition, TypeOfArc type) {
        this.arcType = type;
        this.setStartLocation(startPosition);
        this.setEndPoint(startPosition.getPosition());
        this.setType(PetriNetElementType.ARC);

        this.breakPoints = new ArrayList<>();
    }

    /**
     * Metoda sprawdza, czy aktualny łuk jest łukiem odczytu (read-arc). Jeśli tak, ustala wartość obiektu łuku
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
     * @param arc (<b>Arc</b>) inny łuk niż normal idący w drugą stronę.
     */
    private void handleComplexArcGraphics(Arc arc) {
        if (this.breakPoints.isEmpty()) {
            Point startP = getStartLocation().getPosition();
            Point endP = getEndLocation().getPosition();

            Point breakPoint = new Point(((startP.x + endP.x) / 2) + 15, ((startP.y + endP.y) / 2) + 15);
            breakPoints.add(breakPoint);
        }

        if (arc == null)
            return;

        if (arc.breakPoints.isEmpty()) {
            Point startP = arc.getStartLocation().getPosition();
            Point endP = arc.getEndLocation().getPosition();

            Point breakPoint = new Point(((startP.x + endP.x) / 2) - 15, ((startP.y + endP.y) / 2) - 15);
            arc.accessBreaks().add(breakPoint);
        }
    }

    /**
     * Metoda zwracająca wagę łuku.
     * @return (<b>int</b>) waga łuku.
     */
    public int getWeight() {
        return weight;
    }

    /**
     * Metoda pozwalająca ustawić wagę łuku.
     * @param weight (<b>int</b>) waga łuku.
     */
    public void setWeight(int weight) {
        this.weight = weight;
        //if (pairedArc != null && isMainArcOfPair)
        //	pairedArc.setWeight(weight);
    }

    /**
     * Metoda zwracająca wagę łuku kolorowego.
     * @param i (<b>int</b>) nr porządkowy koloru, default 0, od 0 do 5.
     * @return (<b>int</b>) - waga dla koloru.
     */
    public int getColorWeight(int i) {
        return switch (i) {
            case 1 -> arcColoredBox.weight1green;
            case 2 -> arcColoredBox.weight2blue;
            case 3 -> arcColoredBox.weight3yellow;
            case 4 -> arcColoredBox.weight4grey;
            case 5 -> arcColoredBox.weight5black;
            default -> weight;
        };
    }

    /**
     * Metoda pozwalająca ustawić wagę kolorowego łuku.
     * @param w (<b>int</b>) waga łuku.
     * @param i (<b>int</b>) nr porządkowy koloru, default 0, od 0 do 5.
     */
    public void setColorWeight(int w, int i) {
        switch (i) {
            case 1 -> this.arcColoredBox.weight1green = w;
            case 2 -> this.arcColoredBox.weight2blue = w;
            case 3 -> this.arcColoredBox.weight3yellow = w;
            case 4 -> this.arcColoredBox.weight4grey = w;
            case 5 -> this.arcColoredBox.weight5black = w;
            default -> this.weight = w;
        }
    }
    
    /**
     * Metoda zwraca komentarz związany z łukiem.
     * @return comment (<b>String</b>String) komentarz do łuku.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Metoda ustawiająca komentarz dla łuku.
     * @param comm (<b>String</b>) komentarz do łuku.
     */
    public void setComment(String comm) {
        comment = comm;
        if (pairedArc != null && isMainArcOfPair)
            pairedArc.setComment(comm);
    }

    /**
     * Metoda pozwala pobrać wierzchołek początkowy łuku.
     * @return (<b>Node</b>) - wierzchołek wejściowy łuku.
     */
    public Node getStartNode() {
        return this.locationStart.getParentNode();
    }

    /**
     * Metoda pozwala pobrać wierzchołek końcowy łuku.
     * @return (<b>Node</b>) - wierzchołek wyjściowy łuku.
     */
    public Node getEndNode() {
        if (this.locationEnd != null)
            return this.locationEnd.getParentNode();
        return null;
    }

    /**
     * Metoda pozwala pobrać identyfikator arkusza, na którym znajduje się łuk.
     * @return (<b>int</b>) - identyfikator arkusza.
     */
    public int getLocationSheetId() {
        return this.locationStart.getSheetID();
    }

    /**
     * Metoda pozwala obliczyć długość łuku na arkuszu w pikselach.
     * @return (<b>double</b>) - długość łuku.
     */
    public double getWidth() {
        Point A = this.getStartLocation().getPosition();
        Point B = this.getEndLocation().getPosition();
        return Math.hypot(A.x - B.x, A.y - B.y);
    }

    /**
     * Metoda pozwala narysować token na łuku w czasie symulacji.
     * @param g (<b>Graphics2D</b>) grafika 2D.
     * @param sheetId (<b>int</b>) identyfikator arkusza.
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

        int GRAPHICAL_STEPS_COUNTER = overlord.simSettings.getArcGraphicDelay();
        this.graphicalSimulationSteps++;

        if (this.getGraphicalSimulationSteps() > GRAPHICAL_STEPS_COUNTER) {
            this.setGraphicalSimulationSteps(0);
            this.setTransportingTokens(false);
        }
    }

    /**
     * Metoda rysująca łuk na danym arkuszu przy zmienionych rozmiarach arkusza.
     * @param g (<b>Graphics2D</b>) obiekt grafiki.
     * @param sheetId (<b>int</b>) identyfikator arkusza.
     * @param zoom (<b>int</b>) zoom, unused.
     * @param eds (<b>ElementDrawSettings</b>) ustawienia rysowania.
     */
    public void draw(Graphics2D g, int sheetId, int zoom, ElementDrawSettings eds) {
        ElementDraw.drawArc(this, g, sheetId, zoom, eds);
    }

    /**
     * Metoda pozwala sprawdzić, czy łuk jest poprawny.
     * @return (<b>boolean</b>) - true, jeśli łuk jest poprawny; false w przeciwnym wypadku
     */
    public boolean getIsCorect() {
        return this.isCorrect;
    }

    /**
     * Metoda pozwala sprawdzić, czy łuk byłby poprawny dla danej lokalizacji wierzchołka wyjściowego.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja wierzchołka wyjściowego.
     * @return (<b>boolean</b>) - true, jeśli łuk byłby poprawny; false w przeciwnym wypadku.
     */
    public boolean checkIsCorect(ElementLocation elementLocation) {
        this.isCorrect = elementLocation != null
                && elementLocation.getParentNode().getType() != this.getStartLocation().getParentNode().getType()
                && elementLocation != this.getStartLocation();
        return this.isCorrect;
    }

    /**
     * Metoda pozwala ustawić punkt lokacji wierzchołka wyjściowego łuku.
     * @param point (<b>Point</b>) punkt lokalizacji wierzchołka wyjściowego.
     */
    public void setEndPoint(Point point) {
        this.tempEndPoint = point;
    }

    public Point getTempEndPoint() {
        return this.tempEndPoint;
    }

    /**
     * Metoda pozwala pobrać stan zaznaczenia łuku.
     * @return (<b>boolean</b>) - true, jeśli łuk jest zaznaczony; false w przeciwnym wypadku.
     */
    public boolean getSelected() {
        return selected;
    }

    /**
     * Metoda pozwala sprawdzić czy łuk zostanie zaznaczony.
     * @return (<b>boolean</b>) true, jeśli łuk zostanie zaznaczony; false w przeciwnym wypadku.
     */
    public boolean checkSelection() {
        if (this.locationEnd == null || this.locationStart == null)
            return false;
        setSelected(this.locationEnd.isSelected() && this.locationStart.isSelected());
        return this.getSelected();
    }

    /**
     * Metoda pozwala ustawić zaznaczenie łuku.
     * @param select (<b>boolean</b>) wartość zaznaczenia łuku.
     */
    public void setSelected(boolean select) {
        this.selected = select;
    }

    /**
     * Metoda pozwala sprawdzić, czy punkt jest częcią łuku.
     * @param P (<b>Point</b>) punkt (x,y).
     * @return (<b>boolean</b>) - true, jeśli łuk jest częcią łuku; false w przeciwnym wypadku.
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
     * @return startLocation (<b>ElementLocation</b>) - lokalizacja wierzchołka wejściowego łuku.
     */
    public ElementLocation getStartLocation() {
        return locationStart;
    }

    /**
     * Metoda pozwala ustawić lokalizację wierzchołka wejściowego łuku.
     * @param startLocation (<b>ElementLocation</b>) lokalizacja wierzchołka wejściowego.
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
     * @param startLocation (<b>ElementLocation</b>) nowy element location. Okrętu się pan spodziewałeś?
     */
    public void modifyStartLocation(ElementLocation startLocation) {
        this.locationStart = startLocation;
    }

    /**
     * Metoda pozwala ustawić lokację wierzchołka wyjściowego łuku.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja wierzchołka wyjściowego.
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
     * @param elementLocation (<b>ElementLocation</b>) nowy element location. Okrętu się pan spodziewałeś?
     */
    public void modifyEndLocation(ElementLocation elementLocation) {
        this.locationEnd = elementLocation;
    }

    /**
     * Metoda pozwala pobrać lokalizację wierzchołka wyjściowego łuku.
     * @return (<b>ElementLocation</b>) - lokalizacja wierzchołka wyjściowego łuku.
     */
    public ElementLocation getEndLocation() {
        return locationEnd;
    }

    /**
     * Usuwa łuk z referencji lokacji obu wierzchołków (wejściowego i wyjściowego) łuku (odłącza łuk od wierzchołków).
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
     * @return (<b>boolean</b>) - true, jeśli łuk transportuje tokeny; false w przeciwnym wypadku.
     */
    public boolean isTransportingTokens() {
        return isTransportingTokens;
    }

    /**
     * Metoda pozwala ustawić, czy łuk aktualnie transportuje tokeny.
     * @param isTransportingTokens (<b>boolean</b>) wartość określająca, czy łuk transportuje aktualnie tokeny.
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
     * @return (<b>boolean</b>) - true, jeśli symulacja zachodzi zgodnie ze skierowaniem łuku (do przodu);
     * false  w przeciwnym wypadku.
     */
    public boolean isSimulationForwardDirection() {
        return simulationForwardDirection;
    }

    /**
     * Metoda pozwala ustawić kierunek wizualizacji symulacji na łuku.
     * @param simulationForwardDirection (<b>boolean</b>) true dla symulacji 'do przodu'; false w przeciwnym wypadku.
     */
    public void setSimulationForwardDirection(boolean simulationForwardDirection) {
        this.simulationForwardDirection = simulationForwardDirection;
    }

    /**
     * Metoda zwracająca łuk odczytu dla danego łuku.
     * @return (<b>Arc</b>) łuk odczytu.
     */
    public Arc getPairedArc() {
        return pairedArc;
    }

    /**
     * Metoda ustawia wartość pairedArc jeśli łuk jest łukiem odczytu.
     * @param pairedArc (<b>Arc</b>) łuk odczytu.
     */
    private void setPairedArc(Arc pairedArc) {
        if (this.getArcType() == TypeOfArc.META_ARC)
            return;

        this.pairedArc = pairedArc;
        this.arcType = TypeOfArc.READARC;
    }

    /**
     * Metoda informuje, czy łuk jest głównym łukiem z pary (read-arc)
     * @return (<b>boolean</b>) - true jeżeli łuk jest głównym z pary; false w przeciwnym wypadku.
     */
    public boolean isMainArcOfPair() {
        return isMainArcOfPair;
    }

    /**
     * Metoda pozwala ustalić wartość flagi, czy łuk jest głównym w parze (read-arc)
     * @param isMainArcOfPair (<b>boolean</b>) true jeśli jest; false w przeciwnym wypadku.
     */
    private void setMainArcOfPair(boolean isMainArcOfPair) {
        this.isMainArcOfPair = isMainArcOfPair;
    }

    /**
     * Metoda zwraca typ łuku.
     * @return (<b>TypesOfArcs</b>) typ łuku.
     */
    public TypeOfArc getArcType() {
        return arcType;
    }

    /**
     * Tylko do użytku wczytywania danych: ustawia typ łuku.
     * @param type (<b>TypesOfArcs</b>) typ łuku.
     */
    public void setArcType(TypeOfArc type) {
        arcType = type;
    }

    //****************************************************************************************************
    //************************************     BREAKING BAD     ******************************************
    //****************************************************************************************************

    /**
     * Uzyskanie dostępu do tablicy punktów łamiących.
     * @return (<b>ArrayList[Point]</b>) - wektor punktów.
     */
    public ArrayList<Point> accessBreaks() {
        return this.breakPoints;
    }

    /**
     * Dodaje punkt łamiący dla łuku.
     * @param breakP (<b>Point</b>) obiekt współrzędnych.
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
     * @param mousePt (<b>Point</b>) tu kliknięto myszą.
     * @return (<b>Point</b>) - punkt łamiący łuku (jeśli istnieje w pobliżu mousePt).
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
     * @param breakP (<b>Point</b>) punkt kliknięty NA łuku (zapewnione przed wywołaniem tej metody!).
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
     * @param breakP (<b>Point</b>) tu kliknięto myszą, najpierw metoda sprawdzi, czy blisko tego jest break point.
     */
    public void removeBreakPoint(Point breakP) {
        Point toRemove = checkBreakIntersection(breakP);
        if (toRemove != null) {
            breakPoints.remove(toRemove);
        }
    }

    //********************************************************************************************************
    //********************************************************************************************************
    //********************************************************************************************************

    /**
     * Metoda zwracająca dane o łuku w formie łańcucha znaków.
     * @return (<>String</>) - łańcuch znaków informacji o łuku sieci.
     */
    public String toString() {
        PetriNet pn = overlord.getWorkspace().getProject();
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
}
