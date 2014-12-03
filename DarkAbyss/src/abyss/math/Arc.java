package abyss.math;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;

import org.simpleframework.xml.Element;

import abyss.graphpanel.EditorResources;
import abyss.graphpanel.IdGenerator;
import abyss.math.simulator.NetSimulator;

public class Arc extends PetriNetElement {
	private static final long serialVersionUID = 5365625190238686098L;

	private final int STEP_COUNT = NetSimulator.DEFAULT_COUNTER - 5;

	@Element
	private ElementLocation startLocation;
	@Element
	private ElementLocation endLocation = null;
	// private Point starNodeEdgeIntersection = null;
	// private Point endNodeEdgeIntersection = null;
	private Point tempEndPoint = null;
	private boolean selected = false;
	private boolean isCorrect = false;
	@Element
	private int weight = 1;
	private boolean isTransportingTokens = false;
	private int symulationStep = 0;
	private boolean simulationForwardDirection = true;
	private Arc pairedArc;
	private boolean isMainArcOfPair = false;

	public Arc(ElementLocation startPosition, ElementLocation endPosition) {
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setID(IdGenerator.getNextId());
		this.lookForArcPair();
	}

	public Arc(int arcId, ElementLocation startPosition,
			ElementLocation endPosition) {
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setID(arcId);
		this.lookForArcPair();
	}

	public Arc(ElementLocation startPosition, ElementLocation endPosition,
			String comment) {
		this.setID(IdGenerator.getNextId());
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.checkIsCorect(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setComment(comment);
		this.lookForArcPair();
	}

	public Arc(ElementLocation startPosition, ElementLocation endPosition,
			String comment, int weight) {
		this.setID(IdGenerator.getNextId());
		this.setStartLocation(startPosition);
		this.setEndLocation(endPosition);
		this.checkIsCorect(endPosition);
		this.setType(PetriNetElementType.ARC);
		this.setComment(comment);
		this.setWeight(weight);
		this.lookForArcPair();
	}

	public Arc(ElementLocation startPosition) {
		this.setStartLocation(startPosition);
		this.setEndPoint(startPosition.getPosition());
		this.setType(PetriNetElementType.ARC);
	}

	public void lookForArcPair() {
		for (Arc a : getEndLocation().getOutArcs())
			if (a.getEndLocation() == getStartLocation()) {
				a.setMainArcOfPair(true);
				a.setPairedArc(this);
				setPairedArc(a);
			}

	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
		if (pairedArc != null && isMainArcOfPair)
			pairedArc.setWeight(weight);
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String com) {
		comment = com;
		if (pairedArc != null && isMainArcOfPair)
			pairedArc.setComment(com);
	}

	public Node getStartNode() {
		return this.startLocation.getParentNode();
	}

	public Node getEndNode() {
		if (this.endLocation != null)
			return this.endLocation.getParentNode();
		return null;
	}

	public int getLocationSheetId() {
		return this.startLocation.getSheetID();
	}

	public double getWidth() {
		Point A = this.getStartLocation().getPosition();
		Point B = this.getEndLocation().getPosition();
		return Math.hypot(A.x - B.x, A.y - B.y);
	}

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

	public void incrementSimulationStep() {
		if (!this.isTransportingTokens)
			return;
		this.symulationStep++;
		if (this.getSimulationStep() > STEP_COUNT) {
			this.setSimulationStep(0);
			this.setTransportingTokens(false);
		}
	}

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

	public boolean getIsCorect() {
		return this.isCorrect;
	}

	public boolean checkIsCorect(ElementLocation e) {
		this.isCorrect = true;
		if (e == null
				|| e.getParentNode().getType() == this.getStartLocation()
						.getParentNode().getType()
				|| e == this.getStartLocation())
			this.isCorrect = false;
		return this.isCorrect;
	}

	public void setEndPoint(Point p) {
		this.tempEndPoint = p;
	}

	public void setEndLocation(ElementLocation elementLocation) {
		if (elementLocation == null)
			return;
		this.endLocation = elementLocation;
		this.endLocation.addInArc(this);
		this.tempEndPoint = null;
		this.isCorrect = true;
	}

	public boolean getSelected() {
		return selected;
	}

	public boolean checkSelection() {
		if (this.endLocation == null || this.startLocation == null)
			return false;
		setSelected(this.endLocation.isSelected()
				&& this.startLocation.isSelected());
		return this.getSelected();
	}

	public void setSelected(boolean select) {
		this.selected = select;
	}

	public boolean checkIntersection(Point P) {
		Point A = getStartLocation().getPosition();
		Point B = getEndLocation().getPosition();
		if (Line2D.ptSegDist(A.x, A.y, B.x, B.y, P.x, P.y) <= 3)
			return true;
		else
			return false;
	}

	public ElementLocation getStartLocation() {
		return startLocation;
	}

	public void setStartLocation(ElementLocation startLocation) {
		this.startLocation = startLocation;
		this.startLocation.addOutArc(this);
	}

	public ElementLocation getEndLocation() {
		return endLocation;
	}

	public void unlinkElementLocations() {
		if (this.startLocation != null)
			this.startLocation.removeOutArc(this);
		if (this.endLocation != null)
			this.endLocation.removeInArc(this);
	}

	public boolean isTransportingTokens() {
		return isTransportingTokens;
	}

	public void setTransportingTokens(boolean isTransportingTokens) {
		this.isTransportingTokens = isTransportingTokens;
		this.setSimulationStep(0);
		if (!isTransportingTokens)
			if (isSimulationForwardDirection()) {
				if (getStartNode().getType() == PetriNetElementType.TRANSITION)
					((Transition) getStartNode()).setLunching(false);
			} else {
				if (getEndNode().getType() == PetriNetElementType.TRANSITION)
					((Transition) getEndNode()).setLunching(false);
			}
	}

	public int getSimulationStep() {
		return symulationStep;
	}

	public void setSimulationStep(int symulationStep) {
		this.symulationStep = symulationStep;
	}

	public boolean isSimulationForwardDirection() {
		return simulationForwardDirection;
	}

	public void setSimulationForwardDirection(boolean simulationForwardDirection) {
		this.simulationForwardDirection = simulationForwardDirection;
	}

	public Arc getPairedArc() {
		return pairedArc;
	}

	public void setPairedArc(Arc pairedArc) {
		this.pairedArc = pairedArc;
	}

	public boolean isMainArcOfPair() {
		return isMainArcOfPair;
	}

	public void setMainArcOfPair(boolean isMainArcOfPair) {
		this.isMainArcOfPair = isMainArcOfPair;
	}
}
