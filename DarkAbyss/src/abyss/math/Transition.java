package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import abyss.graphpanel.EditorResources;
import abyss.graphpanel.IdGenerator;

public class Transition extends Node {
	private static final long serialVersionUID = 2673581001465115432L;

	protected double minFireTime = 0;
	protected double maxFireTime = 999;
	protected double absoluteFireTime = 0;
	private double FireTime = -1;
	
	protected boolean isLunching;
	protected boolean isGlowed = false;
	private boolean isGlowedMTC = false;
	protected int firingNumber = 0;
	private ArrayList<ArrayList<Transition>> containingInvariants = new ArrayList<ArrayList<Transition>>();

	public Transition(int transitionId,
			ArrayList<ElementLocation> elementLocations, int pn) {
		super(transitionId, elementLocations, pn);
	}

	public Transition(int sheetId, int transitionId, Point transitionPosition,
			int pn) {
		super(sheetId, transitionId, transitionPosition, pn);
	}

	public Transition(int transitionId, ElementLocation elementLocation, int pn) {
		super(transitionId, elementLocation, pn);
	}

	public Transition(int transitionId, int sheetId, Point transitionPosition,
			String name, String comment) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TRANSITION);
	}

	public Transition(int transitionId, ElementLocation elementLocation,
			String name, String comment) {
		super(transitionId, elementLocation, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TRANSITION);
	}

	public Transition(int transitionId,
			ArrayList<ElementLocation> elementLocations, String name,
			String comment) {
		super(transitionId, elementLocations, 15);
		this.setName(name);
		this.setComment(comment);
		this.setType(PetriNetElementType.TRANSITION);
	}

	public Transition(int transitionId,
			ArrayList<ElementLocation> elementLocations) {
		super(transitionId, elementLocations, 15);
		this.setName("Transition"
				+ Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TRANSITION);
	}

	public Transition(int transitionId, int sheetId, Point transitionPosition) {
		super(sheetId, transitionId, transitionPosition, 15);
		this.setName("Transition"
				+ Integer.toString(IdGenerator.getNextTransitionId()));
		this.setType(PetriNetElementType.TRANSITION);
	}

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
				g.drawRect(nodeBounds.x + 10, nodeBounds.y + 10,
						nodeBounds.width - 20, nodeBounds.height - 20);
			g.setColor(EditorResources.glowTransitonTextColor);
			if (this.isGlowed && this.firingNumber > 0)
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
		super.draw(g, sheetId);
	}

	public int getAvailableTokens() {
		int availableTokens = 0;
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			availableTokens += origin.getTokensNumber();
		}
		return availableTokens;
	}

	public int getRequiredTokens() {
		int requiredTokens = 0;
		for (Arc arc : getInArcs()) {
			requiredTokens += arc.getWeight();
		}
		return requiredTokens;
	}

	public boolean launchable() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			if (!(origin.getFreeTokensNumber() >= arc.getWeight()))
				return false;
		}
		return true;
	}

	public boolean isLunching() {
		return isLunching;
	}

	public boolean isGlowed() {
		return isGlowed;
	}

	public void setGlowed(boolean isGlowed, int firingNumber) {
		this.isGlowed = isGlowed;
		this.firingNumber = firingNumber;
	}

	public void setGlowed(boolean isGlowed) {
		this.isGlowed = isGlowed;
	}

	public boolean isGlowedMTC() {
		return isGlowedMTC;
	}

	public void setGlowedMTC(boolean isGlowedMTC) {
		this.isGlowedMTC = isGlowedMTC;
	}

	public int getTokensNumber() {
		return this.firingNumber;
	}

	public void setLunching(boolean isLunching) {
		this.isLunching = isLunching;
	}

	// books tokens required for launch of this transition as taken
	// in order to make it impossible for other transition
	// to use the same tokens
	public void bookRequiredTokens() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			origin.bookTokens(arc.getWeight());
		}
	}

	public void returnBookedTokens() {
		for (Arc arc : getInArcs()) {
			Place origin = (Place) arc.getStartNode();
			origin.returnTokens();
		}
	}

	@Override
	public PetriNetElementType getType() {
		return PetriNetElementType.TRANSITION;
	}

	public int getOutArcWeightTo(Place outPlace) {
		int weight = 0;
		for (Arc currentArc : getOutArcs()) {
			if (currentArc.getEndNode().equals(outPlace))
				weight = currentArc.getWeight();
		}
		return weight;
	}

	public int getInArcWeightFrom(Place inPlace) {
		int weight = 0;
		for (Arc currentArc : getInArcs()) {
			if (currentArc.getStartNode().equals(inPlace))
				weight = currentArc.getWeight();
		}
		return weight;
	}

	public ArrayList<ArrayList<Transition>> getContainingInvariants() {
		return containingInvariants;
	}

	public void setContainingInvariants(
			ArrayList<ArrayList<Transition>> containingInvariants) {
		this.containingInvariants = containingInvariants;
	}
	
	public double getMinFireTime() {
		return minFireTime;
	}

	public double getMaxFireTime() {
		return maxFireTime;
	}

	public double getFireTime() {
		return FireTime;
	}

	public void setFireTime(double fireTime) {
		FireTime = fireTime;
	}
}
