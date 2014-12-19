package abyss.graphpanel;

import java.util.ArrayList;

import abyss.math.Arc;
import abyss.math.ElementLocation;

/**
 * Klasa przekazuj�ca informacje zwi�zane ze zdarzeniem zaznaczania, wywo�ywanym przez SelectionManager.
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
		 * @param arcGroup ArrayList[Arc] - tablica �uk�w
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
		 * @param arc Arc - konkretny �uk sieci
		 * @param el ElementLocation - lokalizacja �uku
		 * @param actionType - typ akcji
		 */
		public SelectionActionEvent(Arc arc, ElementLocation el, SelectionActionType actionType) {
			this.setArc(arc);
			this.setElementLocation(el);
			this.setActionType(actionType);
		}

		/**
		 * Metoda pozwala pobra� list� �uk�w, kt�rych dotyczy wywo�ane zdarzenie zaznaczenia.
		 * @return ArrayList[Arc] - lista �uk�w
		 */
		public ArrayList<Arc> getArcGroup() {
			return arcGroup;
		}

		/**
		 * Metoda pozwala ustawi� list� �uk�w, kt�rych dotyczy wywo�ane zdarzenie zaznaczenia.
		 * @param arcGroup ArrayList[Arc] - lista �uk�w
		 */
		public void setArcGroup(ArrayList<Arc> arcGroup) {
			this.arcGroup = arcGroup;
		}

		/**
		 * Metoda pozwala pobra� pojedynczy �uk, kt�rego dotyczy wywo�ane zdarzenie zaznaczenia.
		 * @return Arc - zaznaczony �uk
		 */
		public Arc getArc() {
			if (getArcGroup() == null || getArcGroup().size() == 0)
				return null;
			return getArcGroup().get(0);
		}

		/**
		 * Metoda pozwala ustawi� pojedynczy �uk, kt�rego dotyczy wywo�ane zdarzenie zaznaczenia.
		 * @param arc Arc - zaznaczony �uk
		 */
		public void setArc(Arc arc) {
			setArcGroup(new ArrayList<Arc>());
			getArcGroup().add(arc);
		}

		/**
		 * Metoda pozwala pobra� list� lokalizacji wierzcho�k�w, kt�rych dotyczy wywo�ane
		 * zdarzenie zaznaczenia.
		 * @return ArrayList[ElementLocation] - lista lokalizacji wierzcho�k�w, w sytuacji, gdy
		 * 		�adna lokalizacja wierzcho�ka nie zosta�a zaznaczona, zwracana jest warto�� null
		 */
		public ArrayList<ElementLocation> getElementLocationGroup() {
			return elementLocationGroup;
		}

		/**
		 * Metoda pozwala ustawi� list� lokalizacji wierzcho�k�w (Arc), kt�rych dotyczy wywo�ane
		 * zdarzenie zaznaczenia.
		 * @param elementLocationGroup ArrayList[ElementLocation] - lista �uk�w zaznaczonych
		 */
		public void setElementLocationGroup(ArrayList<ElementLocation> elementLocationGroup) {
			this.elementLocationGroup = elementLocationGroup;
		}

		/**
		 * Metoda zwraca identyfikator arkusza na kt�rym zosta�o wykonane zaznaczenie.
		 * @return int - identyfikator arkusza
		 */
		public int getSheetId() {
			return sheetId;
		}

		/**
		 * Metoda pozwala ustawi� identyfikator arkusza na kt�rym zosta�o wykonane zaznaczenie.
		 * @param sheetId int - nowy identyfikator arkusza 
		 */
		public void setSheetId(int sheetId) {
			this.sheetId = sheetId;
		}

		/**
		 * Metoda pozwala pobra� pojedyncza lokalizacj� wierzcho�ka (ElementLocation), kt�rej dotyczy
		 * wywo�ane zdarzenie zaznaczenia.
		 * @return ElementLocation - lokalizacja zaznaczonego elementu
		 */
		public ElementLocation getElementLocation() {
			if (getElementLocationGroup() == null || getElementLocationGroup().size() == 0)
				return null;
			return getElementLocationGroup().get(0);
		}

		/**
		 * Metoda pozwala ustawi� pojedyncza lokalizacj� wierzcho�ka (ElementLocation),
		 * kt�rej dotyczy wywo�ane zdarzenie.
		 * @param el ElementLocation - zaznaczona lokalizacja wierzcho�ka
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
		 * Metoda pozwala ustawi� typ wykonanego zdarzenia, definiowany przez typ SelectionActionType.
		 * @param actionType SelectionActionType - typ zdarzenia
		 */
		public void setActionType(SelectionActionType actionType) {
			this.actionType = actionType;
		}
	}
}
