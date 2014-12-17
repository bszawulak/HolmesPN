package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import org.simpleframework.xml.Element;

import abyss.graphpanel.EditorResources;
import abyss.graphpanel.IdGenerator;

/**
 * Klasa implementuj�ca miejsce sieci Petriego. Zapewnia implementacj� 
 * stanu (przechowywania token�w) oraz powi�zane z tym funkcjonalno�ci
 * (funkcjonalno�� wierzcho�ka dziedzyczy po Node).
 * @author students
 *
 */
public class Place extends Node {
	private static final long serialVersionUID = 2346995422046987174L;
	
	/*
	 * UWAGA!!! NIE WOLNO ZMIENIA� NAZW, DODAWA� LUB USUWA� P�L TEJ KLASY
	 * (przestanie by� mo�liwe wczytywanie zapisanych proejkt�w .abyss)
	 */
	
	@Element
	private int tokensNumber = 0;
	private int tokensTaken = 0;
	//public Place()
	
	/**
	 * Konstruktor obiektu miejsca sieci.
	 * @param nodeId int - identyfikator wierzcho�ka
	 * @param sheetId int - identyfikator arkusza
	 * @param placePosition Point - punkt lokalizacji
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba token�w
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
	 * @param nodeId int - identyfikator wierzcho�ka
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
	 * @param nodeId int - identyfikator wierzcho�ka
	 * @param elementLocation ElementLocation - lokalizacja miejsca
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba token�w
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
	 * @param nodeId int - identyfikator wierzcho�ka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 * @param name String - nazwa miejsca
	 * @param comment String - komentarz miejsca
	 * @param tokensNumber int - liczba token�w
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
	 * @param nodeId int - identyfikator wierzcho�ka
	 * @param elementLocations ArrayList[ElementLocation] - lista lokalizacji 
	 */
	public Place(int nodeId, ArrayList<ElementLocation> elementLocations) {
		super(nodeId, elementLocations, 18);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);
	}

	/**
	 * Metoda rysuj�ca miejsce na danym arkuszu.
	 * @param g Graphics2D - grafika 2D 
	 * @param sheetId int - identyfikator arkusza
	 */
	public void draw(Graphics2D g, int sheetId)
	{
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
		super.draw(g, sheetId);
	}

	/**
	 * Metoda pozwala pobra� aktualn� liczb� token�w.
	 * @return int - liczba token�w
	 */
	public int getTokensNumber() {
		return tokensNumber;
	}

	/**
	 * Metoda pozwala ustawi� liczb� token�w
	 * @param tokensNumber int - liczba token�w
	 */
	public void setTokensNumber(int tokensNumber) {
		this.tokensNumber = tokensNumber;
		if(tokensNumber < 0) {
			@SuppressWarnings("unused")
			int error = 1;
		}
	}

	/**
	 * Metoda pozwala zmieni� liczb� token�w, dodaj�c do niej okre�lon� warto��.
	 * @param delta int - warto�� o kt�r� zmieni si� liczba token�w
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
	 * Metoda pozwala pobra� liczb� zaj�tych (zarezerwowanych 
	 * przez aktywowan� tranzycj�) token�w.
	 * @return int - liczba zarezerwowanych token�w
	 */
	public int getTokensTaken() {
		return tokensTaken;
	}

	/**
	 * Metoda pozwala zarezerwowa� okre�lon� liczb� token�w
	 * @param tokensTaken int - liczba zajmowanych token�w
	 */
	public void bookTokens(int tokensTaken) {
		this.tokensTaken = tokensTaken;
	}

	/**
	 * Metoda pozwala zwolni� wszystkie zarezerwowane tokeny.
	 */
	public void returnTokens() {
		this.tokensTaken = 0;
	}

	/**
	 * Metoda pozwala pobra� liczb� wolnych (dost�pnych, nie 
	 * zarezerwowanych przez �adn� tranzycj�) token�w.
	 * @return int - liczba dost�pnych token�w
	 */
	public int getFreeTokensNumber() {
		return tokensNumber - tokensTaken;
	}
}