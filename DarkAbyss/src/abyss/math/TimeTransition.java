package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import abyss.graphpanel.EditorResources;
import abyss.graphpanel.IdGenerator;

public class TimeTransition extends Transition {
	
	public PetriNetElementType getType() {
		return PetriNetElementType.TIMETRANSITION;
	}

	public TimeTransition(int transitionId, int sheetId,
			Point transitionPosition, String name, String comment) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	public TimeTransition(int transitionId, ElementLocation elementLocation,
			String name, String comment) {
		super(transitionId, elementLocation, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	public TimeTransition(int transitionId,
			ArrayList<ElementLocation> elementLocations, String name,
			String comment) {
		super(transitionId, elementLocations, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	public TimeTransition(int transitionId,
			ArrayList<ElementLocation> elementLocations) {
		super(transitionId, elementLocations, 15);
		this.setName("Transition"
				+ Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	public TimeTransition(int transitionId, int sheetId,
			Point transitionPosition) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName("Transition"
				+ Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TIMETRANSITION);
	}

	@Override
	public void draw(Graphics2D g, int sheetId) {
		for (ElementLocation el : this.getNodeLocations(sheetId)) {
			Rectangle nodeBounds = new Rectangle(el.getPosition().x
					- getRadius(), el.getPosition().y - getRadius(),
					this.getRadius() * 2, this.getRadius() * 2);
			if (!isLunching) {
				if (isGlowedMTC()) {
					g.setColor(EditorResources.glowMTCTransitonColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.glowMTCTransitonColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.glowMTCTransitonColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);
				} else if (isGlowed()) {
					g.setColor(EditorResources.glowTransitonColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.glowTransitonColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.glowTransitonColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);
				} else if (el.isSelected() && !el.isPortalSelected()) {
					g.setColor(EditorResources.selectionColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.selectionColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);
				} else if (el.isPortalSelected()) {
					g.setColor(EditorResources.glowPortalColorLevel1);
					g.setStroke(EditorResources.glowStrokeLevel1);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.glowPortalColorLevel2);
					g.setStroke(EditorResources.glowStrokeLevel2);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);

					g.setColor(EditorResources.glowPortalColorLevel3);
					g.setStroke(EditorResources.glowStrokeLevel3);
					g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
							nodeBounds.height);
				}
			}
			if (isLunching()) {
				g.setColor(EditorResources.launchColorLevel1);
				g.setStroke(EditorResources.glowStrokeLevel1);
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);

				g.setColor(EditorResources.launchColorLevel2);
				g.setStroke(EditorResources.glowStrokeLevel2);
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);

				g.setColor(EditorResources.launchColorLevel3);
				g.setStroke(EditorResources.glowStrokeLevel3);
				g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
						nodeBounds.height);
			}
			g.setColor(Color.white);
			g.fillRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
					nodeBounds.height);
			g.setColor(Color.DARK_GRAY);
			g.setStroke(new BasicStroke(1.5F));
			g.drawRect(nodeBounds.x, nodeBounds.y, nodeBounds.width,
					nodeBounds.height);
			if (this.isPortal())
				g.drawRect(nodeBounds.x + 5, nodeBounds.y + 5,
						nodeBounds.width - 10, nodeBounds.height - 10);
			// Draw min and max firing time

			g.setColor(Color.black);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
			String miFT = String.valueOf(this.minFireTime);

			g.drawString(miFT, nodeBounds.x+35, nodeBounds.y + 8);

			g.setColor(Color.black);
			g.setFont(new Font("TimesRoman", Font.PLAIN, 7));
			
			String mxFT = String.valueOf(this.maxFireTime);
			
			g.drawString(mxFT,
					nodeBounds.x +35, nodeBounds.y + 28);

			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20,
					nodeBounds.y + 9);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20,
					nodeBounds.y + 21);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 9, nodeBounds.x + 20,
					nodeBounds.y + 21);
			g.drawLine(nodeBounds.x + 10, nodeBounds.y + 21, nodeBounds.x + 20,
					nodeBounds.y + 9);

			g.setColor(EditorResources.glowTransitonTextColor);
			if (this.isGlowed && this.firingNumber > 0)
			{
				g.setColor(Color.black);
				g.setFont(new Font("TimesRoman", Font.PLAIN, 10));
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
			
		}

		}
		drawNode(g, sheetId);
	}

	public void drawNode(Graphics2D g, int sheetId) {
		g.setColor(Color.black);
		g.setFont(new Font("Tahoma", Font.PLAIN, 11));
		int width = g.getFontMetrics().stringWidth(getName());
		for (Point p : this.getNodePositions(sheetId))
			g.drawString(getName(), p.x - width / 2, p.y + getRadius() + 15);
	}

	public void setMinFireTime(double minFireTime) {
		if (minFireTime < 0.001 && minFireTime > 0)
			minFireTime = 0.001;
		this.minFireTime = minFireTime;
	}

	public void setMaxFireTime(double maxFireTime) {
		if (maxFireTime > 99)
			maxFireTime = 99;
		this.maxFireTime = maxFireTime;
	}

	public double getAbsoluteFireTime() {
		return absoluteFireTime;
	}

	public void setAbsoluteFireTime(double absoluteFireTime) {
		this.absoluteFireTime = absoluteFireTime;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7512230002147987244L;

}
