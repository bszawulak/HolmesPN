package abyss.graphpanel;

import java.util.ArrayList;

import abyss.math.Arc;
import abyss.math.ElementLocation;

/**
 * Klasa przekazuj¹ca informacje zwi¹zane ze zdarzeniem zaznaczania, wywo³ywanym przez SelectionManager.
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
		 * @param arcGroup ArrayList[Arc] - tablica ³uków
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
		 * @param arc Arc - konkretny ³uk sieci
		 * @param el ElementLocation - lokalizacja ³uku
		 * @param actionType - typ akcji
		 */
		public SelectionActionEvent(Arc arc, ElementLocation el, SelectionActionType actionType) {
			this.setArc(arc);
			this.setElementLocation(el);
			this.setActionType(actionType);
		}

		/**
		 * Metoda pozwala pobraæ listê ³uków, których dotyczy wywo³ane zdarzenie zaznaczenia.
		 * @return ArrayList[Arc] - lista ³uków
		 */
		public ArrayList<Arc> getArcGroup() {
			return arcGroup;
		}

		/**
		 * Metoda pozwala ustawiæ listê ³uków, których dotyczy wywo³ane zdarzenie zaznaczenia.
		 * @param arcGroup ArrayList[Arc] - lista ³uków
		 */
		public void setArcGroup(ArrayList<Arc> arcGroup) {
			this.arcGroup = arcGroup;
		}

		/**
		 * Metoda pozwala pobraæ pojedynczy ³uk, którego dotyczy wywo³ane zdarzenie zaznaczenia.
		 * @return Arc - zaznaczony ³uk
		 */
		public Arc getArc() {
			if (getArcGroup() == null || getArcGroup().size() == 0)
				return null;
			return getArcGroup().get(0);
		}

		/**
		 * Metoda pozwala ustawiæ pojedynczy ³uk, którego dotyczy wywo³ane zdarzenie zaznaczenia.
		 * @param arc Arc - zaznaczony ³uk
		 */
		public void setArc(Arc arc) {
			setArcGroup(new ArrayList<Arc>());
			getArcGroup().add(arc);
		}

		/**
		 * Metoda pozwala pobraæ listê lokalizacji wierzcho³ków, których dotyczy wywo³ane
		 * zdarzenie zaznaczenia.
		 * @return ArrayList[ElementLocation] - lista lokalizacji wierzcho³ków, w sytuacji, gdy
		 * 		¿adna lokalizacja wierzcho³ka nie zosta³a zaznaczona, zwracana jest wartoœæ null
		 */
		public ArrayList<ElementLocation> getElementLocationGroup() {
			return elementLocationGroup;
		}

		/**
		 * Metoda pozwala ustawiæ listê lokalizacji wierzcho³ków (Arc), których dotyczy wywo³ane
		 * zdarzenie zaznaczenia.
		 * @param elementLocationGroup ArrayList[ElementLocation] - lista ³uków zaznaczonych
		 */
		public void setElementLocationGroup(ArrayList<ElementLocation> elementLocationGroup) {
			this.elementLocationGroup = elementLocationGroup;
		}

		/**
		 * Metoda zwraca identyfikator arkusza na którym zosta³o wykonane zaznaczenie.
		 * @return int - identyfikator arkusza
		 */
		public int getSheetId() {
			return sheetId;
		}

		/**
		 * Metoda pozwala ustawiæ identyfikator arkusza na którym zosta³o wykonane zaznaczenie.
		 * @param sheetId int - nowy identyfikator arkusza 
		 */
		public void setSheetId(int sheetId) {
			this.sheetId = sheetId;
		}

		/**
		 * Metoda pozwala pobraæ pojedyncza lokalizacjê wierzcho³ka (ElementLocation), której dotyczy
		 * wywo³ane zdarzenie zaznaczenia.
		 * @return ElementLocation - lokalizacja zaznaczonego elementu
		 */
		public ElementLocation getElementLocation() {
			if (getElementLocationGroup() == null || getElementLocationGroup().size() == 0)
				return null;
			return getElementLocationGroup().get(0);
		}

		/**
		 * Metoda pozwala ustawiæ pojedyncza lokalizacjê wierzcho³ka (ElementLocation),
		 * której dotyczy wywo³ane zdarzenie.
		 * @param el ElementLocation - zaznaczona lokalizacja wierzcho³ka
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
		 * Metoda pozwala ustawiæ typ wykonanego zdarzenia, definiowany przez typ SelectionActionType.
		 * @param actionType SelectionActionType - typ zdarzenia
		 */
		public void setActionType(SelectionActionType actionType) {
			this.actionType = actionType;
		}
	}
}
