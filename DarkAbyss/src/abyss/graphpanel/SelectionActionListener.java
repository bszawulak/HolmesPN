package abyss.graphpanel;

import java.util.ArrayList;

import abyss.math.Arc;
import abyss.math.ElementLocation;

public interface SelectionActionListener {
	public void actionPerformed(SelectionActionEvent e);

	public class SelectionActionEvent {

		public enum SelectionActionType {
			SELECTED_ONE, SELECTED_GROUP, SELECTED_SHEET
		}

		private ArrayList<Arc> arcGroup = new ArrayList<Arc>();
		private ArrayList<ElementLocation> elementLocationGroup = new ArrayList<ElementLocation>();
		private int sheetId;
		private SelectionActionType actionType;

		public SelectionActionEvent() {
		}
		
		@SuppressWarnings("static-access")
		public SelectionActionEvent(int sheetId)
		{
			this.setActionType(getActionType().SELECTED_SHEET);
			this.setSheetId(sheetId);
		}

		public SelectionActionEvent(ArrayList<Arc> arcGroup,
				ArrayList<ElementLocation> elementLocationGroup,
				SelectionActionType actionType) {
			this.setArcGroup(arcGroup);
			this.setElementLocationGroup(elementLocationGroup);
			this.setActionType(actionType);
		}

		public SelectionActionEvent(Arc arc, ElementLocation el,
				SelectionActionType actionType) {
			this.setArc(arc);
			this.setElementLocation(el);
			this.setActionType(actionType);
		}

		public ArrayList<Arc> getArcGroup() {
			return arcGroup;
		}

		public void setArcGroup(ArrayList<Arc> arcGroup) {
			this.arcGroup = arcGroup;
		}

		/**
		 * Returns the first element of stored Arcs collection. If Arcs
		 * collection is null or size == 0 returns null
		 * 
		 * @return
		 */
		public Arc getArc() {
			if (getArcGroup() == null || getArcGroup().size() == 0)
				return null;
			return getArcGroup().get(0);
		}

		/**
		 * Creates new Arc collection and add to it given Arc element
		 * 
		 * @param arc
		 */
		public void setArc(Arc arc) {
			setArcGroup(new ArrayList<Arc>());
			getArcGroup().add(arc);
		}

		/**
		 * Returns stored ElementLocation collection
		 * 
		 * @return
		 */
		public ArrayList<ElementLocation> getElementLocationGroup() {
			return elementLocationGroup;
		}

		/**
		 * Sets stored ElementLocation collection to given one
		 * 
		 * @param elementLocationGroup
		 */
		public void setElementLocationGroup(
				ArrayList<ElementLocation> elementLocationGroup) {
			this.elementLocationGroup = elementLocationGroup;
		}
		

		public int getSheetId() {
			return sheetId;
		}

		public void setSheetId(int sheetId) {
			this.sheetId = sheetId;
		}

		/**
		 * Returns the first element of stored ElementLocation collection. If
		 * ElementLocation collection is null or size == 0 returns null
		 * 
		 * @return
		 */
		public ElementLocation getElementLocation() {
			if (getElementLocationGroup() == null
					|| getElementLocationGroup().size() == 0)
				return null;
			return getElementLocationGroup().get(0);
		}

		/**
		 * Creates new ElementLocation collection and add to it given ElementLocation element
		 * @param el
		 */
		public void setElementLocation(ElementLocation el) {
			if (el == null)
				return;
			setElementLocationGroup(new ArrayList<ElementLocation>());
			getElementLocationGroup().add(el);
		}

		public SelectionActionType getActionType() {
			return actionType;
		}

		public void setActionType(SelectionActionType actionType) {
			this.actionType = actionType;
		}
	}
}
