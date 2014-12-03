package abyss.graphpanel;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
//import abyss.math.PetriNetElement;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;

/**
 * @author Antrov
 * 
 */
public class SelectionManager {
	private GraphPanel graphPanel;
	private ArrayList<Node> graphPanelNodes;
	private ArrayList<Arc> graphPanelArcs;
	private ArrayList<ElementLocation> selectedElementLocations = new ArrayList<ElementLocation>();
	private ArrayList<Arc> selectedArcs = new ArrayList<Arc>();
	private SelectionActionListener actionListener;

	public SelectionManager(GraphPanel parentGraphPanel) {
		this.setGraphPanel(parentGraphPanel);
		this.graphPanelNodes = parentGraphPanel.getNodes();
		this.graphPanelArcs = parentGraphPanel.getArcs();
	}

	// ================================================================================
	// Setters & getters
	// ================================================================================

	private GraphPanel getGraphPanel() {
		return graphPanel;
	}

	private void setGraphPanel(GraphPanel parentGraphPanel) {
		this.graphPanel = parentGraphPanel;
	}

	private ArrayList<Node> getGraphPanelNodes() {
		return graphPanelNodes;
	}

	@SuppressWarnings("unused")
	private void setGraphPanelNodes(ArrayList<Node> graphPanelNodes) {
		this.graphPanelNodes = graphPanelNodes;
	}

	private ArrayList<Arc> getGraphPanelArcs() {
		return graphPanelArcs;
	}

	@SuppressWarnings("unused")
	private void setGraphPanelArcs(ArrayList<Arc> graphPanelArcs) {
		this.graphPanelArcs = graphPanelArcs;
	}

	public ArrayList<ElementLocation> getSelectedElementLocations() {
		return selectedElementLocations;
	}

	public void setSelectedElementLocations(
			ArrayList<ElementLocation> selectedElementLocations) {
		this.selectedElementLocations = selectedElementLocations;
	}

	public ArrayList<Arc> getSelectedArcs() {
		return selectedArcs;
	}

	public void setSelectedArcs(ArrayList<Arc> selectedArcs) {
		this.selectedArcs = selectedArcs;
	}

	public void setActionListener(SelectionActionListener e) {
		this.actionListener = e;
	}

	public SelectionActionListener getActionListener() {
		return actionListener;
	}

	private void invokeActionListener() {
		SelectionActionEvent actionEvent = new SelectionActionEvent();
		actionEvent.setElementLocationGroup(getSelectedElementLocations());
		actionEvent.setArcGroup(getSelectedArcs());
		if (getSelectedArcs().size() + getSelectedElementLocations().size() == 1)
			actionEvent.setActionType(SelectionActionType.SELECTED_ONE);
		else
			actionEvent.setActionType(SelectionActionType.SELECTED_GROUP);
		this.getActionListener().actionPerformed(actionEvent);

	}

	// ================================================================================
	// Single Element Location operations
	// ================================================================================

	/**
	 * Adds given Element Location to selection list and sets parent node
	 * isSelected = true
	 * 
	 * @param el
	 *            - ElementLocation to select
	 */
	public void selectElementLocation(ElementLocation el) {
		if (!this.getSelectedElementLocations().contains(el)) {
			this.getSelectedElementLocations().add(el);
			el.setSelected(true);
			this.getGraphPanel().repaint();
			this.invokeActionListener();
		}
	}

	/**
	 * Selects only one Node of given ElementLocation. All other Nodes and rc
	 * are deselected.
	 * 
	 * @param el
	 */
	public void selectOneElementLocation(ElementLocation el) {
		this.deselectAllElementLocations();
		this.deselectAllArcs();
		this.selectElementLocation(el);
		this.invokeActionListener();
	}

	/**
	 * Inverts Element Location selection
	 * 
	 * @param el
	 */
	public void toggleElementLocationSelection(ElementLocation el) {
		if (!this.isElementLocationSelected(el))
			this.selectElementLocation(el);
		else
			this.deselectElementLocation(el);
	}

	/**
	 * Checks all ElementLocation of all Nodes on current sheet for
	 * intersections with given point. If any node doesn't intersects returns
	 * null
	 * 
	 * @param p
	 * @return
	 */
	public ElementLocation getPossiblySelectedElementLocation(Point p) {
		for (Node n : this.getGraphPanelNodes()) {
			ElementLocation el = n.getLocationWhichContains(p, this
					.getGraphPanel().getSheetId());
			if (el != null)
				return el;
		}
		return null;
	}

	/**
	 * Deselects given Element Location and sets parent node isSelected = false
	 * 
	 * @param el
	 *            - Element Location to deselect
	 */
	public void deselectElementLocation(ElementLocation el) {
		this.getSelectedElementLocations().remove(el);
		el.setSelected(false);
		if (this.getSelectedElementLocations().size() > 1)
			this.checkArcsForSelection();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Checks if given ElementLocation is on the selection list
	 * 
	 * @param el
	 * @return Element Location selection state
	 */
	public boolean isElementLocationSelected(ElementLocation el) {
		return this.selectedElementLocations.contains(el);
	}

	/**
	 * Deletes given ElementLocation and all Arcs connected to him. If this
	 * ElementLocation was the only Node location removes it from parent
	 * GrahpPanel
	 * 
	 * @param el
	 */
	public void deleteElementLocation(ElementLocation el) {
		this.deselectElementLocation(el);
		Node n = el.getParentNode();
		if (!n.removeElementLocation(el)) {
			this.getGraphPanelNodes().remove(n);
		}
		for (Iterator<Arc> i = el.getInArcs().iterator(); i.hasNext();) {

			this.getGraphPanelArcs().remove(i.next());
			i.remove();
		}
		for (Iterator<Arc> i = el.getOutArcs().iterator(); i.hasNext();) {

			this.getGraphPanelArcs().remove(i.next());
			i.remove();
		}
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	// ================================================================================
	// Group Element Location operations
	// ================================================================================

	/**
	 * Adds given ElementLocation list to selection list and sets parent node
	 * isSelected = true and if more than one ElementLocation is selected,
	 * checks if some arc should be also selected
	 * 
	 * @param elementLocationGroup
	 *            - Element Location list to select
	 */
	public void selectElementLocationGroup(
			ArrayList<ElementLocation> elementLocationGroup) {
		if (elementLocationGroup == null)
			return;
		for (ElementLocation el : elementLocationGroup)
			if (!this.getSelectedElementLocations().contains(el)) {
				this.getSelectedElementLocations().add(el);
				el.setSelected(true);
			}
		if (this.getSelectedElementLocations().size() > 1)
			this.checkArcsForSelection();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Selects all ElementLocations and Arcs on current sheet
	 */
	public void selectAllElementLocations() {
		this.getSelectedElementLocations().clear();
		this.getSelectedArcs().clear();
		for (Node n : this.getGraphPanelNodes())
			for (ElementLocation el : n.getNodeLocations(getGraphPanel()
					.getSheetId())) {
				this.getSelectedElementLocations().add(el);
				el.setSelected(true);
			}
		for (Arc a : this.getGraphPanelArcs())
			if (a.getLocationSheetId() == getGraphPanel().getSheetId()) {
				a.setSelected(true);
				this.getSelectedArcs().add(a);
			}
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Deselects all ElementLocations and Arcs on current sheet
	 */
	public void deselectAllElementLocations() {
		for (ElementLocation el : this.getSelectedElementLocations())
			el.setSelected(false);
		this.getSelectedElementLocations().clear();
		this.deselectAllArcs();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Transform all selected ElementLocation into portal, moving them to one
	 * Node, given as parameter mainNode
	 * 
	 * @param mainNode
	 */
	@SuppressWarnings("unchecked")
	public void transformSelectedIntoPortal() {
		// sprawdzenie czy wszystkie elementy sa tego samego typu (Place lub
		// Transition)
		for (int i = 1; i < this.getSelectedElementLocations().size(); i++) {
			if (this.getSelectedElementLocations().get(i - 1).getParentNode()
					.getType() != this.getSelectedElementLocations().get(i)
					.getParentNode().getType())
				return;
		}
		for (ElementLocation el : this.getSelectedElementLocations()) {
			if (el.getParentNode().isPortal())
				for (ElementLocation e : el.getParentNode().getNodeLocations())
					e.setPortalSelected(false);
			if (!el.getParentNode().removeElementLocation(el))
				this.getGraphPanelNodes().remove(el.getParentNode());
		}
		if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.PLACE) {
			getGraphPanelNodes()
					.add(new Place(
							IdGenerator.getNextId(),
							(ArrayList<ElementLocation>) getSelectedElementLocations()
									.clone()));
		} else {
			@SuppressWarnings("unused")
			String test = getSelectedElementLocations().get(0).getParentNode().getType().toString();
			if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TIMETRANSITION) {
				getGraphPanelNodes()
						.add(new TimeTransition(
								IdGenerator.getNextId(),
								(ArrayList<ElementLocation>) getSelectedElementLocations()
										.clone()));
			} else {
				getGraphPanelNodes()
						.add(new Transition(
								IdGenerator.getNextId(),
								(ArrayList<ElementLocation>) getSelectedElementLocations()
										.clone()));
			}
		}
		getGraphPanel().repaint();
	}

	public void increaseTokensNumber() {
		ArrayList<Node> safetyNodesList = new ArrayList<Node>();
		for (ElementLocation el : getSelectedElementLocations()) {
			if (el.getParentNode().getType() == PetriNetElementType.PLACE
					&& !safetyNodesList.contains(el.getParentNode())) {
				safetyNodesList.add(el.getParentNode());
				((Place) el.getParentNode()).modifyTokensNumber(1);
			}
		}
		invokeActionListener();
	}

	public void decreaseTokensNumber() {
		ArrayList<Node> safetyNodesList = new ArrayList<Node>();
		for (ElementLocation el : getSelectedElementLocations()) {
			if (el.getParentNode().getType() == PetriNetElementType.PLACE
					&& !safetyNodesList.contains(el.getParentNode())) {
				safetyNodesList.add(el.getParentNode());
				((Place) el.getParentNode()).modifyTokensNumber(-1);
			}
		}
		invokeActionListener();
	}

	// ================================================================================
	// Single Arc operations
	// ================================================================================
	/**
	 * Selects given Arc by adding it to selected Arcs list and sets isSelected
	 * = true
	 * 
	 * @param arc
	 */
	public void selectArc(Arc arc) {
		if (!this.getSelectedArcs().contains(arc)) {
			this.getSelectedArcs().add(arc);
			arc.setSelected(true);
			this.getGraphPanel().repaint();
			this.invokeActionListener();
		}
	}

	/**
	 * Selects only one given Arc and all other on current sheet are deselected
	 * 
	 * @param arc
	 */
	public void selectOneArc(Arc arc) {
		for (Arc a : this.getGraphPanelArcs())
			if (a.getLocationSheetId() == this.getGraphPanel().getSheetId())
				a.setSelected(false);
		this.getSelectedArcs().clear();
		this.selectArc(arc);
	}

	/**
	 * Checks list of Arcs on current sheet for intersections with given point
	 * 
	 * @param p
	 *            - point of possibly intersection
	 * @return - first Arc which intersects with given point
	 */
	public Arc getPossiblySelectedArc(Point p) {
		for (Arc a : this.getGraphPanelArcs()) {
			if (a.getLocationSheetId() == this.getGraphPanel().getSheetId())
				if (a.checkIntersection(p)) {
					if (a.getPairedArc() != null && !a.isMainArcOfPair())
						return a.getPairedArc();
					else
						return a;
				}
		}
		return null;
	}

	/**
	 * Checks if given Arc is selected
	 * 
	 * @param arc
	 * @return
	 */
	public boolean isArcSelected(Arc arc) {
		return this.getSelectedArcs().contains(arc);
	}

	/**
	 * Deselects given Arc
	 * 
	 * @param arc
	 */
	public void deselectArc(Arc arc) {
		this.getSelectedArcs().remove(arc);
		arc.setSelected(false);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Invert selection of given Arc
	 * 
	 * @param arc
	 */
	public void toggleArcSelection(Arc arc) {
		if (arc.getSelected())
			this.deselectArc(arc);
		else
			this.selectArc(arc);
	}

	/**
	 * Deletes given Arc
	 * 
	 * @param arc
	 */
	public void deleteArc(Arc arc) {
		arc.unlinkElementLocations();
		this.deselectArc(arc);
		this.getGraphPanelArcs().remove(arc);
		this.getGraphPanel().repaint();
		if (arc.getPairedArc() != null) {
			Arc a = arc.getPairedArc();
			a.unlinkElementLocations();
			getGraphPanelArcs().remove(a);
		}
		this.invokeActionListener();
	}

	// ================================================================================
	// Group Arc operations
	// ================================================================================

	/**
	 * Checks for every Arc on current sheet if his start and end node are
	 * selected. If they are sets Arc's isSelected = true and adds it to
	 * selected arcs list
	 */
	public void checkArcsForSelection() {
		for (Arc a : this.getGraphPanelArcs())
			if (a.checkSelection())
				this.getSelectedArcs().add(a);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Deselects all Arcs
	 */
	public void deselectAllArcs() {
		for (Arc a : this.getSelectedArcs())
			a.setSelected(false);
		this.getSelectedArcs().clear();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	// ================================================================================
	// Multi-element (Arc and ElementLocation) operations
	// ================================================================================

	/**
	 * Deselects all selected ElementLocations and Arcs
	 */
	public void deselectAllElements() {
		for (Arc a : this.getSelectedArcs())
			a.setSelected(false);
		for (ElementLocation el : this.getSelectedElementLocations())
			el.setSelected(false);
		this.getSelectedArcs().clear();
		this.getSelectedElementLocations().clear();
		this.invokeActionListener();
	}

	public void forceDeselectAllElements() {
		for (Arc a : this.getGraphPanelArcs())
			if (a.getLocationSheetId() == this.getGraphPanel().getSheetId())
				a.setSelected(false);
		for (ElementLocation el : this.getSelectedElementLocations())
			if (el.getSheetID() == this.getGraphPanel().getSheetId())
				el.setSelected(false);
		this.getSelectedArcs().clear();
		this.getSelectedElementLocations().clear();
		this.invokeActionListener();
	}

	/**
	 * Delete all selected ElementLocations and Arc. If current ElementLocation
	 * was the only location of Node it's also deleted. It also deletes all in
	 * ad out Arc of deleted ElementLocation.
	 */
	public void deleteAllSelectedElements() {
		// code below looks similar to other function but not use them to reduce
		// the number of requests repaint
		for (Iterator<ElementLocation> i = this.getSelectedElementLocations()
				.iterator(); i.hasNext();) {
			ElementLocation el = i.next();
			Node n = el.getParentNode();
			// if ElementLocation was the only Node location, it's deleted
			if (!n.removeElementLocation(el)) {
				this.getGraphPanelNodes().remove(n);
			}
			// deletes all in arcs of current ElementLocation
			for (Iterator<Arc> j = el.getInArcs().iterator(); j.hasNext();) {

				this.getGraphPanelArcs().remove(j.next());
				j.remove();
			}
			// deletes all out arcs of current ElementLocation
			for (Iterator<Arc> j = el.getOutArcs().iterator(); j.hasNext();) {

				this.getGraphPanelArcs().remove(j.next());
				j.remove();
			}
			i.remove();
		}
		// deletes all selected Arcs
		for (Iterator<Arc> i = this.getSelectedArcs().iterator(); i.hasNext();) {
			Arc a = i.next();
			this.getGraphPanelArcs().remove(a);
			a.unlinkElementLocations();
			if (a.getPairedArc() != null) {
				Arc arc = a.getPairedArc();
				arc.unlinkElementLocations();
				getGraphPanelArcs().remove(arc);
			}
			i.remove();
		}
		// as a fascist, clears all the selection list
		this.getSelectedArcs().clear();
		this.getSelectedElementLocations().clear();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Selects all Nodes and Arc which are in given rectangle area
	 * 
	 * @param rectangle
	 *            - Selection area
	 * 
	 */
	public void selectInRect(Rectangle rectangle) {
		for (ElementLocation el : this.getSelectedElementLocations())
			el.setSelected(false);
		for (Arc a : this.getSelectedArcs())
			a.setSelected(false);
		this.getSelectedArcs().clear();
		this.getSelectedElementLocations().clear();
		for (Node n : this.getGraphPanel().getNodes()) {
			for (ElementLocation el : n.getLocationsWhichAreContained(
					rectangle, this.getGraphPanel().getSheetId()))
				if (!this.getSelectedElementLocations().contains(el)) {
					this.getSelectedElementLocations().add(el);
					el.setSelected(true);
				}
		}
		if (this.getSelectedElementLocations().size() > 1)
			this.checkArcsForSelection();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	// ================================================================================
	// Sheet Operation
	// ================================================================================

	public void selectSheet() {
		SelectionActionEvent actionEvent = new SelectionActionEvent(
				getGraphPanel().getSheetId());
		this.getActionListener().actionPerformed(actionEvent);
	}

	// ================================================================================
	// Dragging operations
	// ================================================================================

	public void dragSelected() {
		SelectionActionEvent actionEvent = new SelectionActionEvent();
		actionEvent.setElementLocationGroup(getSelectedElementLocations());
		actionEvent.setArcGroup(getSelectedArcs());
		if (getSelectedArcs().size() + getSelectedElementLocations().size() == 1)
			actionEvent.setActionType(SelectionActionType.SELECTED_ONE);
		else
			actionEvent.setActionType(SelectionActionType.SELECTED_GROUP);
		this.getActionListener().actionPerformed(actionEvent);
	}

	// ================================================================================
	// Transition glowing
	// ================================================================================

	public void removeTransitionsGlowing() {
		for (Node n : getGraphPanelNodes())
			if (n.getType() == PetriNetElementType.TRANSITION)
				((Transition) n).setGlowed(false, 0);
			else if (n.getType() == PetriNetElementType.TIMETRANSITION)
				((Transition) n).setGlowed(false, 0);
	}
}
