package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.darkgui.settings.SettingsManager;
import abyss.graphpanel.EditorResources;
import abyss.graphpanel.IdGenerator;
import abyss.math.simulator.NetSimulator;
import abyss.utilities.Tools;

/**
 * Klasa implementująca łuk w sieci Petriego. Przechowuje referencje
 * lokalizacji na swoim początku i  końcu (istotne: lokacji, nie bezpośrednio 
 * wierzchołków). Poprzez owe lokacje można uzyskać dostęp do wierzchołków, 
 * do których należy.
 * @author students
 *
 */
public class Arc extends PetriNetElement {
	//BACKUP: 5365625190238686098L; (NIE DOTYKAĆ PONIŻSZEJ ZMIENNEJ!)
	private static final long serialVersionUID = 5365625190238686098L;
	private final int STEP_COUNT = NetSimulator.DEFAULT_COUNTER - 5;

	/*
	 * UWAGA!!! NIE ZMIENIAĆ NAZW, NIE DODAWAĆ LUB USUWAĆ PÓL TEJ KLASY
	 * (przestanie być możliwe wczytywanie zapisanych projektów .abyss)
	 */
	private ElementLocation locationStart;
	private ElementLocation locationEnd = null;
	private Point tempEndPoint = null;
	private boolean selected = false;
	private boolean isCorrect = false;
	private int weight = 1;
	private boolean isTransportingTokens = false;
	private int simulationStep = 0;
	private boolean simulationForwardDirection = true;
	
	//read-arc parameters:
	private Arc pairedArc;
	private boolean isMainArcOfPair = false;
	private TypesOfArcs arcType;

	/** NORMAL, READARC, INHIBITOR, RESET, EQUAL */
	public enum TypesOfArcs { NORMAL, READARC, INHIBITOR, RESET, EQUAL }
	
	/**
	 * Konstruktor obiektu klasy Arc - chwilowo nieużywany.
	 * @param startPosition ElementLocation - lokalicja żródła łuku
	 * @param endPosition ElementLocation - lokalicja celu łuku
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition, TypesOfArcs type) {
		this.arcType = type; 
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setID(IdGenerator.getNextId());
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc - mousePressed(MouseEvent) - używany w momencie wybrania prawidłowego (!) 
	 * wierzchołka docelowego dla łuku.
	 * @param arcId int - identyfikator łuku
	 * @param startPosition ElementLocation - lokacja źródła łuku
	 * @param endPosition ElementLocation - lokacja celu łuku
	 */
	public Arc(int arcId, ElementLocation startPosition, ElementLocation endPosition, TypesOfArcs type) {
		this.arcType = type;
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setID(arcId);
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc - chwilowo nieużywany.
	 * @param startPosition ElementLocation - lokacja źródła łuku
	 * @param endPosition ElementLocation - lokacja celu łuku
	 * @param comment String - komentarz
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition, String comment, TypesOfArcs type) {
		this.arcType = type;
		this.setID(IdGenerator.getNextId());
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.checkIsCorect(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setComment(comment);
		this.lookForArcPair();
	}

	/**
	 * Konstruktor obiektu klasy Arc - odczyt sieci z pliku
	 * @param startPosition ElementLocation - lokacja źródła łuku
	 * @param endPosition ElementLocation - lokacja celu łuku
	 * @param comment String - komentarz
	 * @param weight int - waga łuku
	 */
	public Arc(ElementLocation startPosition, ElementLocation endPosition, String comment, int weight, TypesOfArcs type) {
		this.arcType = type;
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
	 * Konstruktor obiektu klasy Arc - bez ID, TYLKO na potrzeby rysowania konturu w momencie rozpoczęcia
	 * rysowania (prowadzenia) łuku do miejsca docelowego.
	 * @param startPosition ElementLocation - lokalizacja źródła łuku
	 */
	public Arc(ElementLocation startPosition, TypesOfArcs type) {
		this.arcType = type;
		this.setStartLocation(startPosition);
		this.setEndPoint(startPosition.getPosition());
		this.setType(PetriNetElementType.ARC);
	}

	/**
	 * Metoda sprawdza, czy aktualny łuk jest łukiem odczytu (read-arc).
	 * Jeśli tak, ustala wartość obiektu łuku 
	 */
	public void lookForArcPair() {
		ArrayList<Arc> candidates = this.getEndLocation().getOutArcs();
		for (Arc a : candidates) {
			if (a.getEndLocation() == this.getStartLocation()) {
				a.setMainArcOfPair(true);
				a.setPairedArc(this);
				this.setPairedArc(a);
			}
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
		if (pairedArc != null && isMainArcOfPair)
			pairedArc.setWeight(weight);
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
		TextLayout textLayout1 = new TextLayout(Integer.toString(this.weight), font1, g.getFontRenderContext());
		TextLayout textLayout2 = new TextLayout(Integer.toString(this.weight), font2, g.getFontRenderContext());
		TextLayout textLayout3 = new TextLayout(Integer.toString(this.weight), font3, g.getFontRenderContext());
		
		g.setColor(new Color(255, 255, 255, 70));
		textLayout1.draw(g, (int) a + 10, (int) b);
		g.setColor(new Color(255, 255, 255, 150));
		textLayout2.draw(g, (int) a + 10, (int) b);
		g.setColor(Color.black);
		textLayout3.draw(g, (int) a + 10, (int) b);

	}

	/**
	 * Metoda przechodzi do kolejnego kroku rysowania symulacji na łuku.
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
	 * Metoda rysująca łuk na danym arkuszu przy zmienionych rozmiarach arkusza.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza
	 * @param zoom int - zoom, unused
	 */
	public void draw(Graphics2D g, int sheetId, int zoom) {
		if (this.getLocationSheetId() != sheetId)
			return;
		
		Stroke sizeStroke = g.getStroke();
		
		Point p1 = new Point((Point)getStartLocation().getPosition());
		Point p2 = new Point();
		int endRadius = 0;
		if (getEndLocation() == null) {
			p2 = (Point)this.tempEndPoint;
		} else {
			p2 = (Point)getEndLocation().getPosition().clone();
			endRadius = getEndLocation().getParentNode().getRadius();// * zoom / 100;
		}
		
		int distX = Tools.absolute(p1.x - p2.x);
		int distY = Tools.absolute(p1.y - p2.y);
		
		if(distX == distY) {
			p1.setLocation(p1.x+1, p1.y);
		}
		
		double alfa = p2.x - p1.x + p2.y - p1.y == 0 ? 0 : Math.atan(((double) p2.y - (double) p1.y) / ((double) p2.x - (double) p1.x));
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

		int leftRight = 0; //im wieksze, tym bardziej w prawo
		int upDown = 0; //im większa, tym mocniej w dół

		
		g.setStroke(sizeStroke);
		if(getArcType() == TypesOfArcs.NORMAL || getArcType() == TypesOfArcs.READARC) {
			g.fillPolygon(new int[] { (int) xp+leftRight, (int) xl+leftRight, (int) xk+leftRight }, 
					new int[] { (int) yp+upDown, (int) yl+upDown, (int) yk+upDown }, 3);
		} else if (getArcType() == TypesOfArcs.INHIBITOR) {
			//g.fillOval((int)xp-4, (int)yp, 8, 8);
			int xPos = (int) ((xl + xk)/2);
	    	int yPos = (int) ((yl + yk)/2);
	    	int xT = (int) ((xPos - xp)/3.14);
	    	int yT = (int) ((yPos - yp)/3.14);
	    	
	    	g.drawOval((int)(xPos-5-xT), (int)(yPos-5-yT), 10, 10);
		} else if (getArcType() == TypesOfArcs.RESET) {
			g.fillPolygon(new int[] { (int) xp+leftRight, (int) xl+leftRight, (int) xk+leftRight }, 
					new int[] { (int) yp+upDown, (int) yl+upDown, (int) yk+upDown }, 3);
			
			//int xPos = (int) ((xl + xk)/2);
	    	//int yPos = (int) ((yl + yk)/2);
			xl = p2.x + (endRadius + 30) * alfaCos * sign + M * alfaSin;
			yl = p2.y + (endRadius + 30) * alfaSin * sign - M * alfaCos;
			xk = p2.x + (endRadius + 30) * alfaCos * sign - M * alfaSin;
			yk = p2.y + (endRadius + 30) * alfaSin * sign + M * alfaCos;
			double newxp = p2.x - (endRadius-45) * alfaCos * sign;
			double newyp = p2.y - (endRadius-45) * alfaSin * sign;
			
			g.fillPolygon(new int[] { (int) newxp, (int) xl+leftRight, (int) xk+leftRight }, 
					new int[] { (int) newyp, (int) yl+upDown, (int) yk+upDown }, 3);
		} else if (getArcType() == TypesOfArcs.EQUAL) {
			int xPos = (int) ((xl + xk)/2);
	    	int yPos = (int) ((yl + yk)/2);
	    	int xT = (int) ((xPos - xp)/3.14);
	    	int yT = (int) ((yPos - yp)/3.14);
	    	
	    	g.fillOval((int)(xPos-4-xT), (int)(yPos-4-yT), 8, 8);
	    	
	    	xT = (int) ((xPos - xp));
	    	yT = (int) ((yPos - yp));
	    	
	    	g.fillOval((int)(xPos-4+xT), (int)(yPos-4+yT), 8, 8);
		}
			
		if (getPairedArc() == null || isMainArcOfPair()) { 
			//czyli nie rysuje kreski tylko wtedy, jeśli to podrzędny łuk w ramach read-arc - żeby nie dublować
			g.drawLine(p1.x, p1.y, (int) xp, (int) yp);
		}
		
		int k = (p2.x + p1.x) / 2;
		int j = (p2.y + p1.y) / 2;
		
		//double atang = Math.atan2(p2.y-p1.y,p2.x-p1.x)*180.0/Math.PI;
		double atang = Math.toDegrees(Math.atan2(p2.y-p1.y,p2.x-p1.x));
		if(atang < 0){
			atang += 360;
	    }
		atang = atang % 90;
		if(atang < 45.0) {
			j = j + 5;
			k = k - 5;
		} else {
			j = j - 15;
		}
		
		if (this.getWeight() > 1) {
			g.setFont(new Font("Tahoma", Font.PLAIN, 18));
			g.drawString(Integer.toString(this.getWeight()), k, j + 10);
		}
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
		this.isCorrect = true;
		if (e == null || e.getParentNode().getType() == this.getStartLocation().getParentNode().getType()
				|| e == this.getStartLocation())
			this.isCorrect = false;
		return this.isCorrect;
	}

	/**
	 * Metoda pozwala ustawić punkt lokacji wierzchołka wyjściowego łuku.
	 * @param p Point - punkt lokalizacji wierzchołka wyjściowego
	 */
	public void setEndPoint(Point p) {
		this.tempEndPoint = p;
	}

	/**
	 * Metoda pozwala ustawić lokację wierzchołka wyjściowego łuku.
	 * @param elementLocation ElementLocation - lokalizacja wierzchołka wyjściowego
	 */
	public void setEndLocation(ElementLocation elementLocation) {
		if (elementLocation == null)
			return;
		this.locationEnd = elementLocation;
		this.locationEnd.addInArc(this);
		this.tempEndPoint = null;
		this.isCorrect = true;
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
		setSelected(this.locationEnd.isSelected()
				&& this.locationStart.isSelected());
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
		Point A = getStartLocation().getPosition();
		Point B = getEndLocation().getPosition();
		if (Line2D.ptSegDist(A.x, A.y, B.x, B.y, P.x, P.y) <= 3)
			return true;
		else
			return false;
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
	public void setStartLocation(ElementLocation startLocation) {
		this.locationStart = startLocation;
		this.locationStart.addOutArc(this);
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
		if (this.locationStart != null)
			this.locationStart.removeOutArc(this);
		if (this.locationEnd != null)
			this.locationEnd.removeInArc(this);
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
	 * Metoda pozwala pobrać aktualny krok wizualizacji symulacji.
	 * @return int - numer aktualnego kroku wizualizacji symulacji
	 */
	public int getSimulationStep() {
		return simulationStep;
	}

	/**
	 * Metoda pozwala ustawić aktualny krok wizualizacji symulacji.
	 * @param symulationStep int - numer kroku symulacji
	 */
	public void setSimulationStep(int symulationStep) {
		this.simulationStep = symulationStep;
	}

	/**
	 * Metoda pozwala sprawdzić, czy symulacja zachodzi zgodnie ze 
	 * skierowaniem łuku (do przodu).
	 * @return boolean - true, jeśli symulacja zachodzi zgodnie ze skierowaniem łuku (do przodu);
	 * 		false w przeciwnym wypadku
	 */
	public boolean isSimulationForwardDirection() {
		return simulationForwardDirection;
	}

	/**
	 * Metoda pozwala ustawić kierunek wizualizacji symulacji na łuku.
	 * @param simulationForwardDirection boolean - true dla symulacji 'do przodu';
	 * 		false w przeciwnym wypadku
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
	public void setPairedArc(Arc pairedArc) {
		this.pairedArc = pairedArc;
		this.arcType = TypesOfArcs.READARC;
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
	public void setMainArcOfPair(boolean isMainArcOfPair) {
		this.isMainArcOfPair = isMainArcOfPair;
	}
	
	/**
	 * Metoda zwraca typ łuku.
	 * @return TypesOfArcs
	 */
	public TypesOfArcs getArcType() {
		return arcType;
	}
	
	/**
	 * Tylko do użytku wczytywania danych: ustawia typ łuku.
	 * @param type TypesOfArcs - typ łuku
	 */
	public void setArcType(TypesOfArcs type) {
		arcType = type;
	}
	
	/**
	 * Metoda zwracająca dane o łuku w formie łańcucha znaków.
	 * @return String - łańcuch znaków informacji o łuku sieci
	 */
	public String toString() {
		int startNodeID = -1;
		if(this.getStartLocation() != null)
			if(this.getStartLocation().getParentNode() != null)
				startNodeID = this.getStartLocation().getParentNode().getID();
		
		int endNodeID = -1;
		if(this.getEndLocation() != null)
			if(this.getEndLocation().getParentNode() != null)
				endNodeID = this.getEndLocation().getParentNode().getID();

		String s = "StartNodeID: " + startNodeID
				+ "; EndNodeID: " + endNodeID
				+ "; Type: " + arcType.toString();
				//+ "; TYPE: "+ Boolean.toString(getPairedArc() != null);
		return s;
	}
}
