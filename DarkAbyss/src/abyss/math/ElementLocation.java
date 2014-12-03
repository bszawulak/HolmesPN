package abyss.math;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

import org.simpleframework.xml.Element;

import abyss.darkgui.GUIManager;
import abyss.workspace.Workspace;

public class ElementLocation implements Serializable {
	private static final long serialVersionUID = 2775375770782696276L;

	@Element
	private int sheetId;
	private Point position;
	private Point notSnappedPosition;
	private Node parentNode;
	private boolean isSelected = false;
	private boolean isPortalSelected = false;

	private ArrayList<Arc> inArcs = new ArrayList<Arc>();
	private ArrayList<Arc> outArcs = new ArrayList<Arc>();

	public int getSheetID() {
		return sheetId;
	}

	public void setSheetID(int sheetId) {
		this.sheetId = sheetId;
	}

	public Point getPosition() {
		return position;
	}

	public void setPosition(Point position) {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel()
				.isLegalLocation(position)) {
			this.position = position;
		}
	}

	public Node getParentNode() {
		return parentNode;
	}

	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

	public void updateLocation(Point delta) {
		Point tempPosition = new Point(position.x + delta.x, position.y + delta.y);
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(tempPosition)) {
			position = tempPosition;
		}
	}

	public void updateLocationWithMeshSnap(Point delta, int meshSize) {
		notSnappedPosition.setLocation(notSnappedPosition.x + delta.x,
				notSnappedPosition.y + delta.y);
		Point tempPosition = new Point((notSnappedPosition.x + delta.x) / meshSize * meshSize, (notSnappedPosition.y + delta.y) / meshSize * meshSize);
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(tempPosition)) {
			position = tempPosition;
		}
	}

	public ArrayList<Arc> getOutArcs() {
		return outArcs;
	}

	public void setOutArcs(ArrayList<Arc> outArcs) {
		this.outArcs = outArcs;
	}

	public ArrayList<Arc> getInArcs() {
		return inArcs;
	}

	public void setInArcs(ArrayList<Arc> inArcs) {
		this.inArcs = inArcs;
	}

	public void addInArc(Arc a) {
		this.getInArcs().add(a);
	}

	public void removeInArc(Arc a) {
		this.getInArcs().remove(a);
	}

	public void addOutArc(Arc a) {
		this.getOutArcs().add(a);
	}

	public void removeOutArc(Arc a) {
		this.getOutArcs().remove(a);
	}

	public ElementLocation(int sheetId, Point position, Node parentNode) {
		this.position = position;
		this.notSnappedPosition = position;
		this.sheetId = sheetId;
		this.parentNode = parentNode;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		this.setPortalSelected(false);
		if (getParentNode().checkAllPortalsSelection())
			getParentNode().selectAllPortals();
		else
			getParentNode().deselectAllPortals();
	}

	public boolean isPortalSelected() {
		return isPortalSelected;
	}

	public void setPortalSelected(boolean isPortalSelected) {
		this.isPortalSelected = isPortalSelected;
	}

	public String toString() {
		String s = "sheetID: " + this.getSheetID() + "; position"
				+ this.getPosition().toString() + "; parentNodeID"
				+ this.getParentNode().getID() + "; inArcsCount: "
				+ Integer.toString(this.getInArcs().size())
				+ "; outArcsCount: "
				+ Integer.toString(this.getOutArcs().size());
		return s;
	}
}
