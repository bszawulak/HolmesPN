package abyss.math;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import abyss.graphpanel.ElementDraw;
import abyss.graphpanel.IdGenerator;

/**
 * Klasa implementująca tranzycje czasowe w modelu TPN (Time Petri Net)
 * 
 * @author Bartłomiej Szawulak
 *
 */
public class TimeTransition extends Transition {
	//BACKUP: -7512230002147987244L   (NIE DOTYKAĆ PONIŻSZEJ ZMIENNEJ!)
	private static final long serialVersionUID = -7512230002147987244L;
	
	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAĆ NAZW, DODAWAĆ LUB USUWAĆ PÓL TEJ KLASY
	 * (przestanie być możliwe wczytywanie zapisĆnych proejktów .abyss)
	 */
	
	/**
	 * Metoda zwraca typ tranzycji, w tym wypadku - czasowa
	 * @return PetriNetElementType - element sieci
	 */
	public PetriNetElementType getType() {
		return PetriNetElementType.TIMETRANSITION;
	}

	/**
	 * Konstruktor obiektu tranzycji czasowej.
	 * @param transitionId int - identyfikator tranzycji
	 * @param sheetId int - identyfikator arkusza
	 * @param transitionPosition Point - punkt lokalizacji
	 * @param name String - nazwa tranzycji
	 * @param comment String - komentarz tranzycji
	 */
	public TimeTransition(int transitionId, int sheetId, Point transitionPosition, String name, String comment) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji czasowej.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocation ElementLocation - lokalizacja tranzycji
	 * @param name String - nazwa tranzycji
	 * @param comment String - komentarz tranzycji
	 */
	public TimeTransition(int transitionId, ElementLocation elementLocation, String name, String comment) {
		super(transitionId, elementLocation, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji czasowej.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
	 * @param name String - nazwa tranzycji
	 * @param comment String - komentarz tranzycji
	 */
	public TimeTransition(int transitionId, ArrayList<ElementLocation> elementLocations, String name, String comment) {
		super(transitionId, elementLocations, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji czasowej.
	 * @param transitionId int - identyfikator tranzycji
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji tranzycji
	 */
	public TimeTransition(int transitionId, ArrayList<ElementLocation> elementLocations) {
		super(transitionId, elementLocations, 15);
		this.setName("Transition" + Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	/**
	 * Konstruktor obiektu tranzycji czasowej.
	 * @param transitionId int - identyfikator tranzycji
	 * @param sheetId int - identyfikator arkusza
	 * @param transitionPosition Point - punkt lokalizacji tranzycji
	 */
	public TimeTransition(int transitionId, int sheetId, Point transitionPosition) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName("Transition" + Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TIMETRANSITION);
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
			if (!isLaunching) {
				if (isGlowed_MTC()) {
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
			g.setColor(Color.white);
			
			//g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			g.setColor(Color.gray);
			
			g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			g.setColor(Color.DARK_GRAY);
			g.setStroke(new BasicStroke(1.5F));
			g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			if (this.isPortal())
				g.drawRect(nodeBounds.x + 5, nodeBounds.y + 5, nodeBounds.width - 10, nodeBounds.height - 10);

			//specific for t-trans
			g.setColor(Color.black);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
			String miFT = String.valueOf(this.minFireTime);
			g.drawString(miFT, nodeBounds.x+35, nodeBounds.y + 8);

			g.setColor(Color.black);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
			String mxFT = String.valueOf(this.maxFireTime);
			g.drawString(mxFT, nodeBounds.x +35, nodeBounds.y + 28);

			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20, nodeBounds.y + 9);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20, nodeBounds.y + 21);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20, nodeBounds.y + 21);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20, nodeBounds.y + 9);

			g.setColor(EditorResources.glowTransitonTextColor);
			if (this.isGlowed && this.firingValueInInvariant > 0)
			{
				g.setColor(Color.black);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 10));
				g.drawString(
						Integer.toString(this.getFiring_INV()),
						nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics()
							.stringWidth(Integer.toString(this .getFiring_INV()))
							/ 2, nodeBounds.y + nodeBounds.height / 2 + 5);
			
			}
		}
		*/

		g = ElementDraw.drawElement(this, g, sheetId);
		drawNode(g, sheetId);
		
	}

	/**
	 * Metoda umieszczająca nazwę tranzycji pod jej symbole. Ostatnia wartość
	 * w metodzie - 15 - oznacza nieco poniżej kwadratu. Jej zwiększenie
	 * obniża napis.
	 * @param g Graphics2D - grafika 2D
	 * @param sheetId int - identyfikator arkusza 
	 */
	public void drawNode(Graphics2D g, int sheetId) {
		g.setColor(Color.black);
		g.setFont(new Font("Tahoma", Font.PLAIN, 11));
		int width = g.getFontMetrics().stringWidth(getName());
		for (Point p : this.getNodePositions(sheetId))
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
	}

	/**
	 * Metoda ustala dolny limit niezerowego czasu gotowości - EFT.
	 * @param minFireTime double - czas EFT
	 */
	public void setMinFireTime(double minFireTime) {
		if (minFireTime < 0.001 && minFireTime > 0)
			minFireTime = 0.001;
		this.minFireTime = minFireTime;
	}
	
	/**
	 * Metoda pozwala odczytać przypisany czas EFT tranzycji.
	 * @return double - czas EFT
	 */
	public double getMinFireTime() {
		return this.minFireTime;
	}

	/**
	 * Metoda ustala górny limit nieujemnego czasu krytycznego - LFT.
	 * @param maxFireTime double - czas LFT (deadline na uruchomienie)
	 */
	public void setMaxFireTime(double maxFireTime) {
		if (maxFireTime > 99)
			maxFireTime = 99;
		this.maxFireTime = maxFireTime;
	}

	/**
	 * Metoda pozwala odczytać przypisany czas LFT tranzycji.
	 * @return double - czas LFT
	 */
	public double getMaxFireTime() {
		return this.maxFireTime;
	}
	
	/**
	 * Metoda ta zwraca czas dla danej tranzcji.
	 * @return double - czas dla tranzycji - pole absoluteFireTime
	 */
	//public double getAbsoluteFireTime() {
	//	return absoluteFireTime;
	//}

	/**
	 * Metoda ustawia czas dla tranzycji.
	 * @param absoluteFireTime double - ustawia pole this.absoluteFireTime
	 */
	//public void setAbsoluteFireTime(double absoluteFireTime) {
	//	this.absoluteFireTime = absoluteFireTime;
	//}
}
