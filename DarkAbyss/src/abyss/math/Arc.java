package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;

//import org.simpleframework.xml.Element;

import abyss.graphpanel.EditorResources;
import abyss.graphpanel.IdGenerator;
import abyss.math.simulator.NetSimulator;

/**
 * Klasa implementuj¹ca ³uk w sieci Petriego. Przechowuje referencje
 * lokalizacji na swoim pocz¹tku i  koñcu (istotne: lokacji, nie bezpoœrednio 
 * wierzcho³ków). Poprzez owe lokacje mo¿na uzyskaæ dostêp do wierzcho³ków, 
 * do których nale¿¹.
 * @author students
 *
 */
public class Arc extends PetriNetElement {
	private static final long serialVersionUID = 5365625190238686098L;
	private final int STEP_COUNT = NetSimulator.DEFAULT_COUNTER - 5;

	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAÆ NAZW, DODAWAÆ LUB USUWAÆ PÓL TEJ KLASY
	 * (przestanie byæ mo¿liwe wczytywanie zapisanych proejktów .abyss)
	 */
	private ElementLocation startLocation;
	private ElementLocation endLocation = null;
	private Point tempEndPoint = null;
	private boolean selected = false;
	private boolean isCorrect = false;
	private int weight = 1;
	private boolean isTransportingTokens = false;
	private int simulationStep = 0;
	private boolean simulationForwardDirection = true;
	private Arc pairedArc;
	private boolean isMainArcOfPair = false;

	/**
	 * Konstruktor
	 * @param startPosition ElementLocation - lokalicja Ÿród³a ³uku
	 * @param endPosition ElementLocation - lokalicja celu ³uku
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition) {
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setID(IdGenerator.getNextId());
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc.
	 * @param arcId int - identyfikator ³uku
	 * @param startPosition ElementLocation - lokacja Ÿród³a ³uku
	 * @param endPosition ElementLocation - lokacja celu ³uku
	 */
	public Arc(int arcId, ElementLocation startPosition, ElementLocation endPosition) {
		//this(startPosition,endPosition); //uwaga, ponowna generacja ID, naprawa poni¿ej
		//this.setID(arcId);
		//IdGenerator.setStartId(arcId+1); //zapobiega zwiêkszaniu ID dwukrotnie	
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setID(arcId);
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc.
	 * @param startPosition ElementLocation - lokacja Ÿród³a ³uku
	 * @param endPosition ElementLocation - lokacja celu ³uku
	 * @param comment String - komentarz
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition, String comment) {
		this.setID(IdGenerator.getNextId());
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.checkIsCorect(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setComment(comment);
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc.
	 * @param startPosition ElementLocation - lokacja Ÿród³a ³uku
	 * @param endPosition ElementLocation - lokacja celu ³uku
	 * @param comment String - komentarz
	 * @param weight int - waga ³uku
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition, String comment, int weight) {
		this.setID(IdGenerator.getNextId());
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.checkIsCorect(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setComment(comment);
		this.setWeight(weight);
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc.
	 * @param startPosition ElementLocation - lokalizacja Ÿród³a ³uku
	 */
	public Arc(ElementLocation startPosition) {
		this.setStartLocation(startPosition);
		this.setEndPoint(startPosition.getPosition());
		this.setType(PetriNetElementType.ARC);
	}

	/**
	 * Metoda sprawdza, czy aktualny ³uk jest ³ukiem odczytu (read-arc).
	 * Jeœli tak, ustala wartoœæ obiektu ³uku 
	 */
	public void lookForArcPair() {
		for (Arc a : getEndLocation().getOutArcs())
			if (a.getEndLocation() == getStartLocation()) {
				a.setMainArcOfPair(true);
				a.setPairedArc(this);
				setPairedArc(a);
			}
	}

	/**
	 * Metoda zwracaj¹ca wagê ³uku.
	 * @return int - waga ³uku
	 */
	public int getWeight() {
		return weight;
	}

	/**
	 * Metoda pozwalaj¹ca ustawiæ wagê ³uku.
	 * @param weight int - waga ³uku
	 */
	public void setWeight(int weight) {
		this.weight = weight;
		if (pairedArc != null && isMainArcOfPair)
			pairedArc.setWeight(weight);
	}

	/**
	 * Metoda zwraca komentarz zwi¹zany z ³ukiem.
	 * @return comment String - komentarz do ³uku
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Metoda ustawiaj¹ca komentarz dla ³uku.
	 * @param com String - komentarz do ³uku
	 */
	public void setComment(String com) {
		comment = com;
		if (pairedArc != null && isMainArcOfPair)
			pairedArc.setComment(com);
	}

	/**
	 * Metoda pozwala pobraæ wierzcho³ek pocz¹tkowy ³uku.
	 * @return Node - wierzcho³ek wejœciowy ³uku
	 */
	public Node getStartNode() {
		return this.startLocation.getParentNode();
	}

	/**
	 * Metoda pozwala pobraæ wierzcho³ek koñcowy ³uku.
	 * @return Node - wierzcho³ek wyjœciowy ³uku
	 */
	public Node getEndNode() {
		if (this.endLocation != null)
			return this.endLocation.getParentNode();
		return null;
	}

	/**
	 * Metoda pozwala pobraæ identyfikator arkusza, na którym znajduje siê ³uk.
	 * @return int - identyfikator arkusza
	 */
	public int getLocationSheetId() {
		return this.startLocation.getSheetID();
	}

	/**
	 * Metoda pozwala obliczyæ d³ugoœæ ³uku na arkuszu w pikselach.
	 * @return double - d³ugoœæ ³uku
	 */
	public double getWidth() {
		Point A = this.getStartLocation().getPosition();
		Point B = this.getEndLocation().getPosition();
		return Math.hypot(A.x - B.x, A.y - B.y);
	}

	/**
	 * Metoda zwracaj¹ca dane o ³uku w formie ³añcucha znaków.
	 * @return String - ³añcuch znaków informacji o ³uku sieci
	 */
	public String toString() {
		// return String.format("%s weight: %d; width: %f;", this.getName(),
		// this.getWeight(), this.getWidth());
		String s = "startNodeELParentNodeID: "
				+ Integer.toString(this.getStartLocation().getParentNode()
						.getID())
				+ "; endNodeELParentNodeID: "
				+ Integer.toString(this.getEndLocation().getParentNode()
						.getID()) + "; isPaired: "
				+ Boolean.toString(getPairedArc() != null);
		return s;
	}

	/**
	 * Metoda pozwala narysowaæ token na ³uku w czasie symulacji.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza
	 */
	public void drawSimulationToken(Graphics2D g, int sheetId) {
		if (!this.isTransportingTokens || this.getLocationSheetId() != sheetId
				|| this.weight == 0 || this.getSimulationStep() > STEP_COUNT)
			return;
		// if(this.getEndNodeEdgeIntersection() == null)
		// return;
		Point A = this.getStartLocation().getPosition();
		Point B = this.getEndLocation().getPosition();
		double arcWidth = Math.hypot(A.x - B.x, A.y - B.y);
		double stepSize = arcWidth / (double) STEP_COUNT;
		double a = 0;
		double b = 0;
		if (this.isSimulationForwardDirection()) {
			a = A.x - stepSize * getSimulationStep() * (A.x - B.x) / arcWidth;
			b = A.y - stepSize * getSimulationStep() * (A.y - B.y) / arcWidth;
		} else {
			a = B.x + stepSize * getSimulationStep() * (A.x - B.x) / arcWidth;
			b = B.y + stepSize * getSimulationStep() * (A.y - B.y) / arcWidth;
		}
		g.setColor(EditorResources.tokenDefaultColor);
		g.fillOval((int) a - 5, (int) b - 5, 10, 10);
		g.setColor(Color.black);
		g.setStroke(EditorResources.tokenDefaultStroke);
		g.drawOval((int) a - 5, (int) b - 5, 10, 10);
		Font font1 = new Font("Tahoma", Font.BOLD, 14);
		Font font2 = new Font("Tahoma", Font.BOLD, 13);
		Font font3 = new Font("Tahoma", Font.PLAIN, 12);
		TextLayout textLayout1 = new TextLayout(Integer.toString(this.weight),
				font1, g.getFontRenderContext());
		TextLayout textLayout2 = new TextLayout(Integer.toString(this.weight),
				font2, g.getFontRenderContext());
		TextLayout textLayout3 = new TextLayout(Integer.toString(this.weight),
				font3, g.getFontRenderContext());
		g.setColor(new Color(255, 255, 255, 70));
		textLayout1.draw(g, (int) a + 10, (int) b);
		g.setColor(new Color(255, 255, 255, 150));
		textLayout2.draw(g, (int) a + 10, (int) b);
		g.setColor(Color.black);
		textLayout3.draw(g, (int) a + 10, (int) b);

	}

	/**
	 * Metoda przechodzi do kolejnego kroku rysowania symulacji na ³uku.
	 */
	public void incrementSimulationStep() {
		if (!this.isTransportingTokens)
			return;
		this.simulationStep++;
		if (this.getSimulationStep() > STEP_COUNT) {
			this.setSimulationStep(0);
			this.setTransportingTokens(false);
		}
	}

	/**
	 * Metoda rysuj¹ca ³uk na danym arkuszu przy zmienionych rozmiarach arkusza.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza
	 * @param zoom int - zoom, unused
	 */
	public void draw(Graphics2D g, int sheetId, int zoom) {
		if (this.getLocationSheetId() != sheetId)
			return;
		Point p1 = (Point)getStartLocation().getPosition();
		Point p2 = new Point();
		int endRadius = 0;
		if (getEndLocation() == null)
			p2 = (Point)this.tempEndPoint;
		else {
			p2 = (Point)getEndLocation().getPosition().clone();
			endRadius = getEndLocation().getParentNode().getRadius();// * zoom / 100;
		}/*
		if(zoom != 100){
			p1.x = p1.x * zoom / 100;
			p1.y = p1.y * zoom / 100;
			p2.x = p2.x * zoom / 100;
			p2.y = p2.y * zoom / 100;
		}*/
		// int startRadius = getStartLocation().getParentNode().getRadius();
		double alfa = p2.x - p1.x + p2.y - p1.y == 0 ? 0 : Math
				.atan(((double) p2.y - (double) p1.y)
						/ ((double) p2.x - (double) p1.x));

		double alfaCos = Math.cos(alfa);
		double alfaSin = Math.sin(alfa);
		double sign = p2.x < getStartLocation().getPosition().x ? 1 : -1;
		double M = 4;
		double xp = p2.x + endRadius * alfaCos * sign;
		double yp = p2.y + endRadius * alfaSin * sign;
		// double xs = p1.x + startRadius * alfaCos * sign * -1;
		// double ys = p1.y + startRadius * alfaSin * sign * -1;
		double xl = p2.x + (endRadius + 10) * alfaCos * sign + M * alfaSin;
		double yl = p2.y + (endRadius + 10) * alfaSin * sign - M * alfaCos;
		double xk = p2.x + (endRadius + 10) * alfaCos * sign - M * alfaSin;
		double yk = p2.y + (endRadius + 10) * alfaSin * sign + M * alfaCos;
		if (this.selected) {
			g.setColor(EditorResources.selectionColorLevel3);
			g.setStroke(EditorResources.glowStrokeArc);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			g.drawPolygon(new int[] { (int) xp, (int) xl, (int) xk },
					new int[] { (int) yp, (int) yl, (int) yk }, 3);
		}
		// this.starNodeEdgeIntersection = new Point((int) xs, (int) ys);
		// this.endNodeEdgeIntersection = new Point((int) xp, (int) yp);
		g.setStroke(new BasicStroke(1.0f));
		if (this.isCorrect)
			g.setColor(Color.darkGray);
		else
			g.setColor(new Color(176, 23, 31));

		g.fillPolygon(new int[] { (int) xp, (int) xl, (int) xk }, new int[] {
				(int) yp, (int) yl, (int) yk }, 3);
		if (getPairedArc() == null || isMainArcOfPair())
			g.drawLine(p1.x, p1.y, (int) xp, (int) yp);

		int k = (p2.x + p1.x) / 2;
		int j = (p2.y + p1.y) / 2;
		if (this.getWeight() > 1)
			g.drawString(Integer.toString(this.getWeight()), k, j + 10);
	}

	/**
	 * Metoda pozwala sprawdziæ, czy ³uk jest poprawny.
	 * @return boolean - true, jeœli ³uk jest poprawny; false w przeciwnym wypadku
	 */
	public boolean getIsCorect() {
		return this.isCorrect;
	}

	/**
	 * Metoda pozwala sprawdziæ, czy ³uk by³by poprawny dla danej lokalizacji
	 * wierzcho³ka wyjœciowego.
	 * @param e ElementLocation - lokalizacja wierzcho³ka wyjœciowego
	 * @return boolean - true, jeœli ³uk by³by poprawny; false w przeciwnym wypadku
	 */
	public boolean checkIsCorect(ElementLocation e) {
		this.isCorrect = true;
		if (e == null || e.getParentNode().getType() == this.getStartLocation().getParentNode().getType()
				|| e == this.getStartLocation())
			this.isCorrect = false;
		return this.isCorrect;
	}

	/**
	 * Metoda pozwala ustawiæ punkt lokacji wierzcho³ka wyjœciowego ³uku.
	 * @param p Point - punkt lokalizacji wierzcho³ka wyjœciowego
	 */
	public void setEndPoint(Point p) {
		this.tempEndPoint = p;
	}

	/**
	 * Metoda pozwala ustawiæ lokacjê wierzcho³ka wyjœciowego ³uku.
	 * @param elementLocation ElementLocation - lokalizacja wierzcho³ka wyjœciowego
	 */
	public void setEndLocation(ElementLocation elementLocation) {
		if (elementLocation == null)
			return;
		this.endLocation = elementLocation;
		this.endLocation.addInArc(this);
		this.tempEndPoint = null;
		this.isCorrect = true;
	}

	/**
	 * Metoda pozwala pobraæ stan zaznaczenia ³uku.
	 * @return boolean - true, jeœli ³uk jest zaznaczony; false w przeciwnym wypadku
	 */
	public boolean getSelected() {
		return selected;
	}

	/**
	 * Metoda pozwala sprawdziæ czy ³uk zostanie zaznaczony.
	 * @return true, jeœli ³uk zostanie zaznaczony; false w przeciwnym wypadku
	 */
	public boolean checkSelection() {
		if (this.endLocation == null || this.startLocation == null)
			return false;
		setSelected(this.endLocation.isSelected()
				&& this.startLocation.isSelected());
		return this.getSelected();
	}

	/**
	 * Metoda pozwala ustawiæ zaznaczenie ³uku.
	 * @param select boolean - wartoœæ zaznaczenia ³uku
	 */
	public void setSelected(boolean select) {
		this.selected = select;
	}

	/**
	 * Metoda pozwala sprawdziæ, czy punkt jest czêœci¹ ³uku.
	 * @param P Point - punkt (x,y)
	 * @return boolean - true, jeœli ³uk jest czêœci¹ ³uku; false w przeciwnym wypadku
	 */
	public boolean checkIntersection(Point P) {
		Point A = getStartLocation().getPosition();
		Point B = getEndLocation().getPosition();
		if (Line2D.ptSegDist(A.x, A.y, B.x, B.y, P.x, P.y) <= 3)
			return true;
		else
			return false;
	}

	/**
	 * Metoda pozwala pobraæ lokacjê wierzcho³ka wejœciowego ³uku.
	 * @return startLocation ElementLocation - lokalizacja wierzcho³ka wejœciowego ³uku
	 */
	public ElementLocation getStartLocation() {
		return startLocation;
	}

	/**
	 * Metoda pozwala ustawiæ lokalizacjê wierzcho³ka wejœciowego ³uku.
	 * @param startLocation ElementLocation - lokalizacja wierzcho³ka wejœciowego
	 */
	public void setStartLocation(ElementLocation startLocation) {
		this.startLocation = startLocation;
		this.startLocation.addOutArc(this);
	}

	/**
	 * Metoda pozwala pobraæ lokalizacjê wierzcho³ka wyjœciowego ³uku.
	 * @return ElementLocation - lokalizacja wierzcho³ka wyjœciowego ³uku
	 */
	public ElementLocation getEndLocation() {
		return endLocation;
	}

	/**
	 * Usuwa ³uk z referencji lokacji obu wierzcho³ków (wejœciowego i
	 * wyjœciowego) ³uku (od³¹cza ³uk od wierzcho³ków).
	 */
	public void unlinkElementLocations() {
		if (this.startLocation != null)
			this.startLocation.removeOutArc(this);
		if (this.endLocation != null)
			this.endLocation.removeInArc(this);
	}

	/**
	 * Metoda pozwala sprawdziæ, czy ³uk aktualnie transportuje tokeny.
	 * @return boolean - true, jeœli ³uk transportuje tokeny; false w przeciwnym wypadku
	 */
	public boolean isTransportingTokens() {
		return isTransportingTokens;
	}

	/**
	 * Metoda pozwala ustawiæ, czy ³uk aktualnie transportuje tokeny.
	 * @param isTransportingTokens boolean - wartoœæ okreœlaj¹ca, czy ³uk transportuje aktualnie tokeny
	 */
	public void setTransportingTokens(boolean isTransportingTokens) {
		this.isTransportingTokens = isTransportingTokens;
		this.setSimulationStep(0);
		if (!isTransportingTokens)
			if (isSimulationForwardDirection()) {
				if (getStartNode().getType() == PetriNetElementType.TRANSITION)
					((Transition) getStartNode()).setLaunching(false);
			} else {
				if (getEndNode().getType() == PetriNetElementType.TRANSITION)
					((Transition) getEndNode()).setLaunching(false);
			}
	}

	/**
	 * Metoda pozwala pobraæ aktualny krok wizualizacji symulacji.
	 * @return int - numer aktualnego kroku wizualizacji symulacji
	 */
	public int getSimulationStep() {
		return simulationStep;
	}

	/**
	 * Metoda pozwala ustawiæ aktualny krok wizualizacji symulacji.
	 * @param symulationStep int - numer kroku symulacji
	 */
	public void setSimulationStep(int symulationStep) {
		this.simulationStep = symulationStep;
	}

	/**
	 * Metoda pozwala sprawdziæ, czy symulacja zachodzi zgodnie ze 
	 * skierowaniem ³uku (do przodu).
	 * @return boolean - true, jeœli symulacja zachodzi zgodnie ze skierowaniem ³uku (do przodu);
	 * 		false w przeciwnym wypadku
	 */
	public boolean isSimulationForwardDirection() {
		return simulationForwardDirection;
	}

	/**
	 * Metoda pozwala ustawiæ kierunek wizualizacji symulacji na ³uku.
	 * @param simulationForwardDirection boolean - true dla symulacji 'do przodu';
	 * 		false w przeciwnym wypadku
	 */
	public void setSimulationForwardDirection(boolean simulationForwardDirection) {
		this.simulationForwardDirection = simulationForwardDirection;
	}

	/**
	 * Metoda zwracaj¹ca ³uk odczytu dla danego ³uku.
	 * @return Arc - ³uk odczytu
	 */
	public Arc getPairedArc() {
		return pairedArc;
	}

	/**
	 * Metoda ustawia wartoœæ pairedArc jeœli ³uk jest ³ukiem odczytu.
	 * @param pairedArc Arc - ³uk odczytu
	 */
	public void setPairedArc(Arc pairedArc) {
		this.pairedArc = pairedArc;
	}

	/**
	 * Metoda informuje, czy ³uk jest g³ównym ³ukiem z pary (read-arc)
	 * @return boolean - true je¿eli ³uk jest g³ównym z pary; false w przeciwnym wypadku
	 */
	public boolean isMainArcOfPair() {
		return isMainArcOfPair;
	}

	/**
	 * Metoda pozwala ustaliæ wartoœæ flagi, czy ³uk jest g³ównym w parze (read-arc)
	 * @param isMainArcOfPair boolean - true jeœli jest; false w przeciwnym wypadku
	 */
	public void setMainArcOfPair(boolean isMainArcOfPair) {
		this.isMainArcOfPair = isMainArcOfPair;
	}
}
