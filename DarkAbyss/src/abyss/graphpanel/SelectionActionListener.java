package abyss.graphpanel;

import java.util.ArrayList;

import abyss.math.pnElements.Arc;
import abyss.math.pnElements.ElementLocation;

/**
 * Klasa przekazująca informacje związane ze zdarzeniem zaznaczania, wywoływanym przez SelectionManager.
 * @author students
 *
 */
public interface SelectionActionListener {
	public void actionPerformed(SelectionActionEvent e);

	public class SelectionActionEvent {

		public enum SelectionActionType { SELECTED_ONE, SELECTED_GROUP, SELECTED_SHEET }
		private ArrayList<Arc> arcGroup = new ArrayList<Arc>();
		private ArrayList<ElementLocation> elementLocationGroup = new ArrayList<ElementLocation>();
		private int sheetId;
		private SelectionActionType actionType;

		/**
		 * Konstruktor bezargumentowy obiektu klasy SelectionActionListener.
		 */
		public SelectionActionEvent() {
		}
		
		@SuppressWarnings("static-access")
		/**
		 * Konstruktor obiektu klasy SelectionActionEvent.
		 * @param sheetId int - numer arkusza
		 */
		public SelectionActionEvent(int sheetId)
		{
			this.setActionType(getActionType().SELECTED_SHEET);
			this.setSheetId(sheetId);
		}

		/**
		 * Konstruktor obiektu klasy SelectionActionEvent.
		 * @param arcGroup ArrayList[Arc] - tablica łuków
		 * @param elementLocationGroup ArrayList[ElementLocation] - tablica lokalizacji
		 * @param actionType SelectionActionType - rodzaj
		 */
		public SelectionActionEvent(ArrayList<Arc> arcGroup, ArrayList<ElementLocation> elementLocationGroup,
				SelectionActionType actionType) {
			this.setArcGroup(arcGroup);
			this.setElementLocationGroup(elementLocationGroup);
			this.setActionType(actionType);
		}

		/**
		 * Konstruktor obiektu klasy SelectionActionEvent.
		 * @param arc Arc - konkretny łuk sieci
		 * @param el ElementLocation - lokalizacja łuku
		 * @param actionType - typ akcji
		 */
		public SelectionActionEvent(Arc arc, ElementLocation el, SelectionActionType actionType) {
			this.setArc(arc);
			this.setElementLocation(el);
			this.setActionType(actionType);
		}

		/**
		 * Metoda pozwala pobrać listę łuków, których dotyczy wywołane zdarzenie zaznaczenia.
		 * @return ArrayList[Arc] - lista łuków
		 */
		public ArrayList<Arc> getArcGroup() {
			return arcGroup;
		}

		/**
		 * Metoda pozwala ustawić listę łuków, których dotyczy wywołane zdarzenie zaznaczenia.
		 * @param arcGroup ArrayList[Arc] - lista łuków
		 */
		public void setArcGroup(ArrayList<Arc> arcGroup) {
			this.arcGroup = arcGroup;
		}

		/**
		 * Metoda pozwala pobrać pojedynczy łuk, którego dotyczy wywołane zdarzenie zaznaczenia.
		 * @return Arc - zaznaczony łuk
		 */
		public Arc getArc() {
			if (getArcGroup() == null || getArcGroup().size() == 0)
				return null;
			return getArcGroup().get(0);
		}

		/**
		 * Metoda pozwala ustawić pojedynczy łuk, którego dotyczy wywołane zdarzenie zaznaczenia.
		 * @param arc Arc - zaznaczony łuk
		 */
		public void setArc(Arc arc) {
			setArcGroup(new ArrayList<Arc>());
			getArcGroup().add(arc);
		}

		/**
		 * Metoda pozwala pobrać listę lokalizacji wierzchołków, których dotyczy wywołane
		 * zdarzenie zaznaczenia.
		 * @return ArrayList[ElementLocation] - lista lokalizacji wierzchołków, w sytuacji, gdy
		 * 		żadna lokalizacja wierzchołka nie została zaznaczona, zwracana jest wartość null
		 */
		public ArrayList<ElementLocation> getElementLocationGroup() {
			return elementLocationGroup;
		}

		/**
		 * Metoda pozwala ustawić listę lokalizacji wierzchołków (Arc), których dotyczy wywołane
		 * zdarzenie zaznaczenia.
		 * @param elementLocationGroup ArrayList[ElementLocation] - lista łuków zaznaczonych
		 */
		public void setElementLocationGroup(ArrayList<ElementLocation> elementLocationGroup) {
			this.elementLocationGroup = elementLocationGroup;
		}

		/**
		 * Metoda zwraca identyfikator arkusza na którym zostało wykonane zaznaczenie.
		 * @return int - identyfikator arkusza
		 */
		public int getSheetId() {
			return sheetId;
		}

		/**
		 * Metoda pozwala ustawić identyfikator arkusza na którym zostało wykonane zaznaczenie.
		 * @param sheetId int - nowy identyfikator arkusza 
		 */
		public void setSheetId(int sheetId) {
			this.sheetId = sheetId;
		}

		/**
		 * Metoda pozwala pobrać pojedyncza lokalizację wierzchołka (ElementLocation), której dotyczy
		 * wywołane zdarzenie zaznaczenia.
		 * @return ElementLocation - lokalizacja zaznaczonego elementu
		 */
		public ElementLocation getElementLocation() {
			if (getElementLocationGroup() == null || getElementLocationGroup().size() == 0)
				return null;
			return getElementLocationGroup().get(0);
		}

		/**
		 * Metoda pozwala ustawić pojedyncza lokalizację wierzchołka (ElementLocation),
		 * której dotyczy wywołane zdarzenie.
		 * @param el ElementLocation - zaznaczona lokalizacja wierzchołka
		 */
		public void setElementLocation(ElementLocation el) {
			if (el == null)
				return;
			setElementLocationGroup(new ArrayList<ElementLocation>());
			getElementLocationGroup().add(el);
		}

		/**
		 * Metoda zwraca typ wykonanego zdarzenia, definiowany przez typ SelectionActionType.
		 * @return SelectionActionType - typ zdarzenia
		 */
		public SelectionActionType getActionType() {
			return actionType;
		}

		/**
		 * Metoda pozwala ustawić typ wykonanego zdarzenia, definiowany przez typ SelectionActionType.
		 * @param actionType SelectionActionType - typ zdarzenia
		 */
		public void setActionType(SelectionActionType actionType) {
			this.actionType = actionType;
		}
	}
}
