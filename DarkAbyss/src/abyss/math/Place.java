package abyss.math;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import org.simpleframework.xml.Element;

import abyss.graphpanel.ElementDraw;
import abyss.graphpanel.IdGenerator;

/**
 * Klasa implementująca miejsce sieci Petriego. Zapewnia implementację 
 * stanu (przechowywania tokenów) oraz powiązane z tym funkcjonalności
 * (funkcjonalność wierzchołka dziedzyczy po Node).
 * @author students
 *
 */
public class Place extends Node {
	// BACKUP: 2346995422046987174L  (NIE DOTYKAĆ PONIŻSZEJ ZMIENNEJ!)
	private static final long serialVersionUID = 2346995422046987174L;
	
	/*
	 * UWAGA!!! NIE WOLNO ZMIENIAĆ NAZW, DODAWAĆ LUB USUWAĆ PÓL TEJ KLASY
	 * (przestanie być możliwe wczytywanie zapisĆnych proejktów .abyss)
	 */
	
	@Element
	private int tokensNumber = 0;
	private int tokensTaken = 0;
	//public Place()
	
	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param sheetId int - identyfikator arkusza
	 * @param placePosition Point - punkt lokalizacji
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba tokenów
	 */
	public Place(int nodeId, int sheetId, Point placePosition, String name, String comment, int tokensNumber) {
		super(sheetId, nodeId, placePosition, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}
	
	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param sheetId int - identyfikator arkusza
	 * @param placePosition Point - punkt lokalizacji
	 */
	public Place(int nodeId, int sheetId, Point placePosition) {
		super(sheetId, nodeId, placePosition, 18);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocation ElementLocation - lokalizacja miejsca
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba tokenów
	 */
	public Place(int nodeId, ElementLocation elementLocation, String name, String comment, int tokensNumber) {
		super(nodeId, elementLocation, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba tokenów
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations, String name, String comment, int tokensNumber) {
		super(nodeId, elementLocations, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzchołka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations) {
		super(nodeId, elementLocations, 18);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Metoda rysująca miejsce na danym arkuszu.
	 * @param g Graphics2D - grafika 2D 
	 * @param sheetId int - identyfikator arkusza
	 */
	public void draw(Graphics2D g, int sheetId)
	{
		/*
		for (ElementLocation el : this.getNodeLocations(sheetId)) {
			Rectangle nodeBounds = new Rectangle(
					el.getPosition().x - getRadius(), el.getPosition().y - getRadius(), 
					this.getRadius() * 2, this.getRadius() * 2);
			if (el.isSelected() && !el.isPortalSelected()) {
				g.setColor(EditorResources.selectionColorLevel1);
				g.setStroke(EditorResources.glowStrokeLevel1);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				g.setColor(EditorResources.selectionColorLevel2);
				g.setStroke(EditorResources.glowStrokeLevel2);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				g.setColor(EditorResources.selectionColorLevel3);
				g.setStroke(EditorResources.glowStrokeLevel3);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
				
				try {
					BufferedImage img = ImageIO.read(getClass().getResource("/icons/selectedSign.png"));
					g.drawImage(img, null, 
							nodeBounds.x-(this.getRadius()-4), 
							nodeBounds.y-(this.getRadius()-4));
				} catch (Exception e) {
					
				}
			} else if (el.isPortalSelected()) {
				g.setColor(EditorResources.glowPortalColorLevel1);
				g.setStroke(EditorResources.glowStrokeLevel1);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				g.setColor(EditorResources.glowPortalColorLevel2);
				g.setStroke(EditorResources.glowStrokeLevel2);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);

				g.setColor(EditorResources.glowPortalColorLevel3);
				g.setStroke(EditorResources.glowStrokeLevel3);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			}
			g.setColor(Color.white);
			g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			g.setColor(Color.DARK_GRAY);
			g.setStroke(new BasicStroke(1.5F));
			g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width, nodeBounds.height);
			if (this.getTokensNumber() > 0)
				g.drawString(
						Integer.toString(this.getTokensNumber()),
						nodeBounds.x + nodeBounds.width / 2 - g.getFontMetrics()
						.stringWidth(Integer.toString(this.getTokensNumber())) / 2, 
						nodeBounds.y + nodeBounds.height / 2 + 5);
			if (isPortal) {
				g.drawOval(nodeBounds.x + 10, nodeBounds.y + 10, nodeBounds.width - 20, nodeBounds.height - 20);
			}
		}
		*/
		g = ElementDraw.drawElement(this, g, sheetId);
		super.draw(g, sheetId);
	}

	/**
	 * Metoda pozwala pobrać aktualną liczbę tokenów.
	 * @return int - liczba tokenów
	 */
	public int getTokensNumber() {
		return tokensNumber;
	}

	/**
	 * Metoda pozwala ustawić liczbę tokenów
	 * @param tokensNumber int - liczba tokenów
	 */
	public void setTokensNumber(int tokensNumber) {
		this.tokensNumber = tokensNumber;
		if(tokensNumber < 0) {
			@SuppressWarnings("unused")
			int error = 1;
		}
	}

	/**
	 * Metoda pozwala zmienić liczbę tokenów, dodając do niej określoną wartość.
	 * @param delta int - wartość o którą zmieni się liczba tokenów
	 */
	public void modifyTokensNumber(int delta) {
		this.tokensNumber = this.tokensNumber + delta;
		if(tokensNumber < 0) {
			@SuppressWarnings("unused")
			int error = 1;
			error = 2;
		}
	}

	/**
	 * Metoda pozwala pobrać liczbę zajętych (zarezerwowanych 
	 * przez aktywowaną tranzycję) tokenów.
	 * @return int - liczba zarezerwowanych tokenów
	 */
	public int getTokensTaken() {
		return tokensTaken;
	}

	/**
	 * Metoda pozwala zarezerwować określoną liczbę tokenów
	 * @param tokensTaken int - liczba zajmowanych tokenów
	 */
	public void bookTokens(int tokensTaken) {
		this.tokensTaken = tokensTaken;
	}

	/**
	 * Metoda pozwala zwolnić wszystkie zarezerwowane tokeny.
	 */
	public void returnTokens() {
		this.tokensTaken = 0;
	}

	/**
	 * Metoda pozwala pobrać liczbę wolnych (dostępnych, nie 
	 * zarezerwowanych przez żadną tranzycję) tokenów.
	 * @return int - liczba dostępnych tokenów
	 */
	public int getFreeTokensNumber() {
		return tokensNumber - getTokensTaken();
	}
}