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

public class Place extends Node {
	private static final long serialVersionUID = 2346995422046987174L;

	@Element
	private int tokensNumber = 0;
	private int tokensTaken = 0;

	
	//public Place()
	
	public Place(int nodeId, int sheetId, Point placePosition, String name,
			String comment, int tokensNumber) {
		super(sheetId, nodeId, placePosition, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}

	public Place(int nodeId, int sheetId, Point placePosition) {
		super(sheetId, nodeId, placePosition, 18);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);
	}

	public Place(int nodeId, ElementLocation elementLocation, String name,
			String comment, int tokensNumber) {
		super(nodeId, elementLocation, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}

	public Place(int nodeId, ArrayList<ElementLocation> elementLocations,
			String name, String comment, int tokensNumber) {
		super(nodeId, elementLocations, 18);
		this.setName(name);
		this.setComment(comment);
		this.setTokensNumber(tokensNumber);
		this.setType(PetriNetElementType.PLACE);
	}

	public Place(int nodeId, ArrayList<ElementLocation> elementLocations) {
		super(nodeId, elementLocations, 18);
		this.setName("Place" + Integer.toString(IdGenerator.getNextPlaceId()));
		this.setType(PetriNetElementType.PLACE);
	}

	public void draw(Graphics2D g, int sheetId) {
		for (ElementLocation el : this.getNodeLocations(sheetId)) {
			Rectangle nodeBounds = new Rectangle(el.getPosition().x
					- getRadius(), el.getPosition().y - getRadius(),
					this.getRadius() * 2, this.getRadius() * 2);
			if (el.isSelected() && !el.isPortalSelected()) {
				g.setColor(EditorResources.selectionColorLevel1);
				g.setStroke(EditorResources.glowStrokeLevel1);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);

				g.setColor(EditorResources.selectionColorLevel2);
				g.setStroke(EditorResources.glowStrokeLevel2);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);

				g.setColor(EditorResources.selectionColorLevel3);
				g.setStroke(EditorResources.glowStrokeLevel3);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);
			} else if (el.isPortalSelected()) {
				g.setColor(EditorResources.glowPortalColorLevel1);
				g.setStroke(EditorResources.glowStrokeLevel1);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);

				g.setColor(EditorResources.glowPortalColorLevel2);
				g.setStroke(EditorResources.glowStrokeLevel2);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);

				g.setColor(EditorResources.glowPortalColorLevel3);
				g.setStroke(EditorResources.glowStrokeLevel3);
				g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);
			}
			g.setColor(Color.white);
			g.fillOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
					nodeBounds.height);
			g.setColor(Color.DARK_GRAY);
			g.setStroke(new BasicStroke(1.5F));
			g.drawOval(nodeBounds.x, nodeBounds.y, nodeBounds.width,
					nodeBounds.height);
			if (this.getTokensNumber() > 0)
				g.drawString(
						Integer.toString(this.getTokensNumber()),
						nodeBounds.x
								+ nodeBounds.width
								/ 2
								- g.getFontMetrics()
										.stringWidth(
												Integer.toString(this
														.getTokensNumber()))
								/ 2, nodeBounds.y + nodeBounds.height / 2 + 5);
			if (isPortal) {
				g.drawOval(nodeBounds.x + 10, nodeBounds.y + 10,
						nodeBounds.width - 20, nodeBounds.height - 20);
			}
		}
		super.draw(g, sheetId);
	}

	public int getTokensNumber() {
		return tokensNumber;
	}

	public void setTokensNumber(int tokensNumber) {
		this.tokensNumber = tokensNumber;
	}

	public void modifyTokensNumber(int delta) {
		this.tokensNumber = this.tokensNumber + delta;
	}

	public int getTokensTaken() {
		return tokensTaken;
	}

	public void bookTokens(int tokensTaken) {
		this.tokensTaken = tokensTaken;
	}

	public void returnTokens() {
		this.tokensTaken = 0;
	}

	public int getFreeTokensNumber() {
		return tokensNumber - tokensTaken;
	}

}
