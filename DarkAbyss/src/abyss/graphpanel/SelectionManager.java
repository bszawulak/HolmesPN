package abyss.graphpanel;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

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
 * Zadaniem klasy SelectionManager jest zarz¹dzanie zaznaczeniem oraz obiektami które s¹ aktualnie
 * zaznaczone na danym arkuszu. SelectionManager zawsze ma przypisanego arkusza-rodzica
 * GraphPanel, którego obiektami zarz¹dza nie wp³ywaj¹c nigdy na obiekty pozosta³ych arkuszy.
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

	/**
	 * Konstruktor domyœlny obiektu klasy SelectionManager.
	 * @param parentGraphPanel GraphPanel - obiekt panelu
	 */
	public SelectionManager(GraphPanel parentGraphPanel) {
		this.setGraphPanel(parentGraphPanel);
		this.graphPanelNodes = parentGraphPanel.getNodes();
		this.graphPanelArcs = parentGraphPanel.getArcs();
	}

	/**
	 * Metoda pobieraj¹ca aktualny obiekt arkusza do rysowania sieci.
	 * @return GraphPanel - obiekt arkusza
	 */
	private GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt jako arkusz rysowania sieci.
	 * @param parentGraphPanel GraphPanel - nowy obiekt arkusza
	 */
	private void setGraphPanel(GraphPanel parentGraphPanel) {
		this.graphPanel = parentGraphPanel;
	}

	/**
	 * Metoda zwracaj¹ca wszystkie wierzcho³ki znajduj¹ce siê w danym arkuszu rysowania.
	 * @return ArrayList[Node] - lista wierzcho³ków sieci w arkuszu
	 */
	private ArrayList<Node> getGraphPanelNodes() {
		return graphPanelNodes;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda ustawiaj¹ca now¹ tablicê wierzcho³ków arkusza sieci.
	 * @param graphPanelNodes ArrayList[Node] - nowa tablica wêz³ów
	 */
	private void setGraphPanelNodes(ArrayList<Node> graphPanelNodes) {
		this.graphPanelNodes = graphPanelNodes;
	}

	/**
	 * Metoda zwracaj¹ca listê ³uków panelu graficznego.
	 * @return ArrayList[Arc] - lista ³uków
	 */
	private ArrayList<Arc> getGraphPanelArcs() {
		return graphPanelArcs;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda ustawiaj¹ca now¹ listê ³uków panelu graficznego.
	 * @param graphPanelArcs ArrayList[Arc] - lista ³uków
	 */
	private void setGraphPanelArcs(ArrayList<Arc> graphPanelArcs) {
		this.graphPanelArcs = graphPanelArcs;
	}

	/**
	 * Metoda umo¿liwia pobranie aktualnej listy zaznaczonych lokalizacji wierzcho³ków
	 * (jeden wierzcho³ek mo¿e zawieraæ wiêcej ni¿ jedn¹ lokalizacjê reprezentowan¹ graficznie)
	 * @return ArrayList[ElementLocation] - lista zaznaczonych lokalizacji
	 */
	public ArrayList<ElementLocation> getSelectedElementLocations() {
		return selectedElementLocations;
	}

	/**
	 * Metoda umo¿liwia ustawienie aktualnej listy zaznaczonych lokalizacji wierzcho³ków
	 * (jeden wierzcho³ek mo¿e zawieraæ wiêcej ni¿ jedn¹ lokalizacjê reprezentowan¹ graficznie)
	 * @param selectedElementLocations ArrayList[ElementLocation] - nowa lista zaznaczonych 
	 * 		lokalizacji wierzcho³ków
	 */
	public void setSelectedElementLocations(ArrayList<ElementLocation> selectedElementLocations) {
		this.selectedElementLocations = selectedElementLocations;
	}

	/**
	 * Metoda umo¿liwia pobranie aktualnej listy zaznaczonych ³uków.
	 * @return ArrayList[Arc] - listê zaznaczonych ³uków
	 */
	public ArrayList<Arc> getSelectedArcs() {
		return selectedArcs;
	}

	/**
	 * Metoda umo¿liwia ustawienie aktualnej listy zaznaczonych ³uków.
	 * @param selectedArcs ArrayList[Arc] - nowa lista zaznaczonych ³uków
	 */
	public void setSelectedArcs(ArrayList<Arc> selectedArcs) {
		this.selectedArcs = selectedArcs;
	}

	/**
	 * Metoda pozwala ustawiæ obiekt nas³uchuj¹cy zmian zaznaczenia. Przy ka¿dej
	 * zmianie zaznaczenia, zostanie wywo³ana metoda actionPerfomed w obiekcie
	 * implementuj¹cym interfejs SelectionActionListener, w której parametrze zostanie
	 * przekazany obiekt SelectionActionEvent.
	 * @param e SelectionActionListener - obiekt nas³uchuj¹cy, implementuj¹cy interfejs SelectionActionListener
	 */
	public void setActionListener(SelectionActionListener e) {
		this.actionListener = e;
	}

	/**
	 * Metoda pozwala pobrania obiektu nas³uchuj¹cego zmian zaznaczenia.
	 * @return SelectionActionListener - obiekt nas³uchuj¹cy, implementuj¹cy interfejs SelectionActionListener
	 */
	public SelectionActionListener getActionListener() {
		return actionListener;
	}

	/**
	 * Metoda aktywuj¹ca dalsze metody, w zale¿noœci od tego, co zosta³o klikniête lub
	 * zaznaczone na modelu rysowanej sieci.
	 */
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
	 * Metoda dodaje podan¹ w parametrze lokalizacjê wierzcho³ka (pojedynczy wierzcho³ek
	 * posiada 1 lub wiêcej lokalizacjê wyœwietlania, reprezentowan¹ klas¹ ElementLocation
	 * do listy zaznaczonych lokalizacji wierzcho³ków oraz ustawia pole 
	 * ElementLocation.isSelected = true. Powoduje to automatyczne odœwie¿enie widoku rysowania
	 * oraz wywo³anie obiektu nas³uchuj¹cego. Jeœli po wykonaniu tej operacji, na liœcie
	 * zaznaczonych lokalizacji wierzcho³ków istniej¹ wiêcej ni¿ dwa obiekty, prawdopodobnym
	 * jest ¿e s¹ one po³¹czone ³ukiem, który w takiej sytuacji powinien zostaæ automatycznie
	 * zaznaczony. Sprawdzenie tej mo¿liwoœci dokonywane jest za pomoc¹ wywo³ania metody
	 * checkArcsForSelection.
	 * @param el ElementLocation - lokalizacja wierzcho³ka która ma zostaæ zaznaczona
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
	 * Metoda powoduje zaznaczenie tylko podanej w parametrze lokalizacji wierzcho³ka.
	 * Wszelkie pozosta³e lokalizacje wierzcho³ków zostaj¹ odznaczone
	 * ElementLocation.isSelected = false oraz wszystkie ³uki rc.isSelected = false,
	 * a listy przechowuj¹cych elementy zaznaczone (zarówno dla ElementLocation jak i
	 * Arc) zostaj¹ wyczyszczone. Nastêpnie korzystaj¹c z metody 
	 * selectElementLocation(abyss.math.ElementLocation el podana lokalizacja wierzcho³ka
	 * zostaje zaznaczona.
	 * @param el ElementLocation - lokalizacja wierzcho³ka która jedyny ma zostaæ zaznaczona
	 */
	public void selectOneElementLocation(ElementLocation el) {
		this.deselectAllElementLocations();
		this.deselectAllArcs();
		this.selectElementLocation(el);
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odwrócenie zaznaczenia podanej w parametrze lokalizacji wierzcho³ka.
	 * Jeœli podana w parametrze lokalizacja wierzcho³ka jest zaznaczona
	 * ElementLocation.isSelected == true wykonana zostaje metoda deselectElementLocation(el)
	 * , w przeciwnym przypadku wykonana zostaje metoda selectElementLocation(el).
	 * @param el ElementLocation - lokalizacja wierzcho³ka której zaznaczenie ma zostaæ odwrócone
	 */
	public void toggleElementLocationSelection(ElementLocation el) {
		if (!this.isElementLocationSelected(el))
			this.selectElementLocation(el);
		else
			this.deselectElementLocation(el);
	}

	/**
	 * Metoda zwraca pierwsz¹ lokalizacjê wierzcho³ka, dla której spe³niony jest warunek
	 * przeciêcia z podanym w parametrze punktem. Metoda przeszukuje wszystkie lokalizacje
	 * ElementLocation wszystkich wierzcho³ków zawartych na danym arkuszu, dla których
	 * odleg³oœæ punktu przekazanego w parametrze od punktu œrodkowego pozycji Point danej
	 * lokalizacji wierzcho³ka ElementLocation.Postion jest mniejsza od promienia danego wierzcho³ka.
	 * @param p Point - punkt dla którego bêd¹ sprawdzane warunki przeciêcia
	 * @return ElementLocation - dla którego warunek przeciêcia zosta³ spe³niony. Jeœli taka
	 * 		lokalizacja nie zosta³a znaleziona, zwracana jest wartoœæ null
	 */
	public ElementLocation getPossiblySelectedElementLocation(Point p) {
		for (Node n : this.getGraphPanelNodes()) {
			ElementLocation el = n.getLocationWhichContains(p, this.getGraphPanel().getSheetId());
			if (el != null)
				return el;
		}
		return null;
	}

	/**
	 * Metoda powoduje odznaczenie podanej w parametrze lokalizacji wierzcho³ka ElementLocation.
	 * Podany w parametrze \textit{ElementLocation} zostaje usuniêty z list zaznaczonych lokalizacji
	 * wierzcho³ka \textit{ElementLocation} oraz zostaje on odznaczony ElementLocation.isSelected = false.
	 * Jeœli po wykonaniu tej operacji, na liœcie zaznaczonych lokalizacji wierzcho³ków zosta³y wiêcej
	 * ni¿ dwa obiekty, prawdopodobnym jest ¿e s¹ one po³¹czone ³ukiem, który w takiej sytuacji powinien
	 * zostaæ automatycznie zaznaczony. Sprawdzenie tej mo¿liwoœci dokonywane jest za pomoc¹ wywo³ania
	 * metody checkArcsForSelection().
	 * @param el ElementLocation - lokalizacja wierzcho³ka która ma zostaæ odznaczona
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
	 * Metoda pozwala sprawdziæ czy podana w parametrze lokalizacja wierzcho³ka ElementLocation
	 * znajduje siê na liœcie zaznaczonych lokalizacji wierzcho³ków.
	 * @param el ElementLocation - lokalizacja wierzcho³ka której obecnoœæ na liœcie zaznaczenia
	 * 		ma zostaæ sprawdzona
	 * @return boolean - true, jeœli podany w parametrze ElementLocation znajduje siê na liœcie
	 * 		zaznaczonych; false w sytuacji przeciwnej
	 */
	public boolean isElementLocationSelected(ElementLocation el) {
		return this.selectedElementLocations.contains(el);
	}

	/**
	 * Metoda usuwa podan¹ w parametrze lokalizacjê wierzcho³ka oraz wszystkie przy³¹czone do
	 * niego ³uki (Arc). W sytuacji gdy podana lokalizacja by³a jedn¹ lokalizacj¹ jej
	 * wierzcho³ka-rodzica, wierzcho³ek ten równie¿ zostaje usuniêty.
	 * @param el ElementLocation - lokalizacja wierzcho³ka który ma zostaæ usuniêty
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
	 * Metoda powoduje zaznaczanie zbioru lokalizacji wierzcho³ków podanego w parametrze metody.
	 * Dla ka¿dej lokalizacji z podanego zbioru, sprawdzane jest czy dany obiekt nie znajduje siê
	 * ju¿ na liœcie lokalizacji zaznaczonych. Jeœli nie, jest on do niej dodawany oraz ustawiany
	 * jest dla niego zaznaczenie (ElementLocation.isSelected = true). Jeœli po wykonaniu tej
	 * operacji, na liœcie zaznaczonych lokalizacji wierzcho³ków zosta³y wiêcej ni¿ dwa obiekty,
	 * prawdopodobnym jest ¿e s¹ one po³¹czone ³ukiem (Arc), który w takiej sytuacji powinien
	 * zostaæ automatycznie zaznaczony. Sprawdzenie tej mo¿liwoœci dokonywane jest za pomoc¹
	 * wywo³ania metody checkArcsForSelection().
	 * @param elementLocationGroup ArrayList[ElementLocation] - zbiór który ma zostaæ zaznaczony
	 */
	public void selectElementLocationGroup(ArrayList<ElementLocation> elementLocationGroup) {
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
	 * Metoda powoduje zaznaczanie wszystkich lokalizacji (ElementLocation) wszystkich
	 * wierzcho³ków (Node) oraz co za tym idzie, wszystkich ³uków (Arc) znajduj¹cych siê
	 * na danym arkuszu. W jej wyniku wszystkie lokalizacje wierzcho³ków z danego arkusza
	 * oraz ³uki zostan¹ okreœlone jako zaznaczone (odpowiednio ElementLocation.isSelected = true
	 *  oraz Arc.isSelected = true) i dodane do list obiektów zaznaczonych.
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzcho³ków ElementLocation
	 * oraz co za tym idzie, wszystkich ³uków znajduj¹cych siê na danym arkuszu. W jej
	 * wyniku wszystkie lokalizacje wierzcho³ków z danego arkusza oraz ³uki zostan¹
	 * odznaczone (odpowiednio ElementLocation.isSelected = false oraz Arc.isSelected = false),
	 * a listy obiektów zaznaczonych wyczyszczone.
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
	 * Metoda zmienia aktualnie zaznaczone elementy w portal, przenosz¹c je do
	 * jednego obiektu Node.
	 * Dodano komunikaty ostrzegaj¹ce oraz zachowywanie starych danych pierwszego
	 * zaznaczonego obiektu wêz³a
	 * @author students
	 * @author MR
	 */
	@SuppressWarnings("unchecked")
	public void transformSelectedIntoPortal() {
		// sprawdzenie czy wszystkie elementy sa tego samego typu (Place lub Transition)
		for (int i = 1; i < this.getSelectedElementLocations().size(); i++) {
			if (this.getSelectedElementLocations().get(i - 1).getParentNode()
					.getType() != this.getSelectedElementLocations().get(i).getParentNode().getType()) {
				JOptionPane.showMessageDialog(null,"Please select only one type of element: either transitions or places!",
						"Multiple types selection warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
		}
		for (ElementLocation el : this.getSelectedElementLocations()) { 
			if (el.getParentNode().isPortal()) //usuwanie statusu portal
				for (ElementLocation e : el.getParentNode().getNodeLocations())
					e.setPortalSelected(false);
			if (!el.getParentNode().removeElementLocation(el))
				this.getGraphPanelNodes().remove(el.getParentNode()); //usuwanie wêz³a sieci z danych sieci
		}
		//tutaj jednak obiekt(y) wci¹¿ istnieje i mo¿na np. sprawdziæ jego typ
		if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.PLACE) {
			String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
			String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
			int oldTokensNumber = ((Place)getSelectedElementLocations().get(0).getParentNode()).getTokensNumber();
			int oldTokensTaken = ((Place)getSelectedElementLocations().get(0).getParentNode()).getTokensTaken();
			Place portal = new Place(IdGenerator.getNextId(),
					(ArrayList<ElementLocation>)getSelectedElementLocations().clone()); 
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.setTokensNumber(oldTokensNumber);
			portal.bookTokens(oldTokensTaken);
			getGraphPanelNodes().add(portal);
		} else {
			@SuppressWarnings("unused")
			String test = getSelectedElementLocations().get(0).getParentNode().getType().toString();
			if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TIMETRANSITION) {
				String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
				String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
				double oldEFT = ((TimeTransition)getSelectedElementLocations().get(0).getParentNode()).getMinFireTime();
				double oldLFT = ((TimeTransition)getSelectedElementLocations().get(0).getParentNode()).getMaxFireTime();
				TimeTransition portal = new TimeTransition(IdGenerator.getNextId(),
						(ArrayList<ElementLocation>)getSelectedElementLocations().clone());
				portal.setName(oldName);
				portal.setComment(oldComment);
				portal.setMinFireTime(oldEFT);
				portal.setMaxFireTime(oldLFT);
				getGraphPanelNodes().add(portal);
			} else if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TRANSITION){
				String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
				String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
				Transition portal = new Transition(IdGenerator.getNextId(),
						((ArrayList<ElementLocation>)getSelectedElementLocations().clone()) );
				portal.setName(oldName);
				portal.setComment(oldComment);
				getGraphPanelNodes().add(portal);
			}
		}
		getGraphPanel().repaint();
	}

	/**
	 * Metoda zmienia aktualnie klikniêty element w portal, tworz¹c jego klona.
	 * @author MR
	 */
	@SuppressWarnings("unchecked")
	public void cloneNodeIntoPortal() {
		// sprawdzenie czy wszystkie elementy sa tego samego typu (Place lub Transition)
		if(this.getSelectedElementLocations().size() > 1) {
			//String type = this.getSelectedElementLocations().get(0).getParentNode().getType().toString();
			JOptionPane.showMessageDialog(null,"Cloning into Portals possible only for one selected node!",
					"Multiple selection warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		//dodawanie innych miejsc dla samego portalu do selectedElementLocations
		ElementLocation nodeSelecredEL = this.getSelectedElementLocations().get(0); //wybrana lokalizacja
		Node nodeSelected = nodeSelecredEL.getParentNode(); //wybrany wierzcho³ek
		ArrayList<ElementLocation> otherNodes = nodeSelected.getElementLocations(); //lista jego (innych?) lokacji
		for (ElementLocation el : otherNodes) { 
			if(!el.equals(nodeSelecredEL)) {
				selectedElementLocations.add(el);
			}
		}
		
		for (ElementLocation el : this.getSelectedElementLocations()) { 
			if (el.getParentNode().isPortal()) //usuwanie statusu portal
				for (ElementLocation e : el.getParentNode().getNodeLocations())
					e.setPortalSelected(false);
			if (!el.getParentNode().removeElementLocation(el))
				this.getGraphPanelNodes().remove(el.getParentNode()); //usuwanie wêz³a sieci z danych sieci
		}
		
		if (getSelectedElementLocations().get(0).getParentNode().getType() 
				== PetriNetElementType.PLACE) {
			String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
			String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
			int oldTokensNumber = ((Place)getSelectedElementLocations().get(0).getParentNode()).getTokensNumber();
			int oldTokensTaken = ((Place)getSelectedElementLocations().get(0).getParentNode()).getTokensTaken();
			
			ElementLocation clonedNode = getSelectedElementLocations().get(0);
			Point newPosition = new Point();
			newPosition.setLocation(clonedNode.getPosition().getX()+30, clonedNode.getPosition().getY()+30);
			ElementLocation clone = new ElementLocation(clonedNode.getSheetID(), 
					newPosition, clonedNode.getParentNode());
			//clone.setInArcs((ArrayList<Arc>)clonedNode.getInArcs().clone());
			//clone.setOutArcs((ArrayList<Arc>)clonedNode.getOutArcs().clone());
			clone.setSelected(clonedNode.isSelected());
			clone.setPortalSelected(clonedNode.isPortalSelected());
			selectedElementLocations.add(clone);
			
			Place portal = new Place(IdGenerator.getNextId(),
					(ArrayList<ElementLocation>)getSelectedElementLocations().clone()); 
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.setTokensNumber(oldTokensNumber);
			portal.bookTokens(oldTokensTaken);
			getGraphPanelNodes().add(portal);
		} else {
			@SuppressWarnings("unused")
			String test = getSelectedElementLocations().get(0).getParentNode().getType().toString();
			if (getSelectedElementLocations().get(0).getParentNode().getType() 
					== PetriNetElementType.TIMETRANSITION) {
				String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
				String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
				double oldEFT = ((TimeTransition)getSelectedElementLocations().get(0).getParentNode()).getMinFireTime();
				double oldLFT = ((TimeTransition)getSelectedElementLocations().get(0).getParentNode()).getMaxFireTime();
				
				ElementLocation clonedNode = getSelectedElementLocations().get(0);
				Point newPosition = new Point();
				newPosition.setLocation(clonedNode.getPosition().getX()+30, clonedNode.getPosition().getY()+30);
				ElementLocation clone = new ElementLocation(clonedNode.getSheetID(), 
						newPosition, clonedNode.getParentNode());
				//clone.setInArcs((ArrayList<Arc>)clonedNode.getInArcs().clone());
				//clone.setOutArcs((ArrayList<Arc>)clonedNode.getOutArcs().clone());
				clone.setSelected(clonedNode.isSelected());
				clone.setPortalSelected(clonedNode.isPortalSelected());
				selectedElementLocations.add(clone);
				
				TimeTransition portal = new TimeTransition(IdGenerator.getNextId(),
						(ArrayList<ElementLocation>)getSelectedElementLocations().clone());
				portal.setName(oldName);
				portal.setComment(oldComment);
				portal.setMinFireTime(oldEFT);
				portal.setMaxFireTime(oldLFT);
				
				getGraphPanelNodes().add(portal);
			} else if (getSelectedElementLocations().get(0).getParentNode().getType() 
					== PetriNetElementType.TRANSITION){
				String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
				String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
				ElementLocation clonedNode = getSelectedElementLocations().get(0);
				Point newPosition = new Point();
				newPosition.setLocation(clonedNode.getPosition().getX()+30, clonedNode.getPosition().getY()+30);
				
				ElementLocation clone = new ElementLocation(clonedNode.getSheetID(), 
						newPosition, clonedNode.getParentNode());
				//clone.setInArcs((ArrayList<Arc>)clonedNode.getInArcs().clone());
				//clone.setOutArcs((ArrayList<Arc>)clonedNode.getOutArcs().clone());
				clone.setSelected(clonedNode.isSelected());
				clone.setPortalSelected(clonedNode.isPortalSelected());
				selectedElementLocations.add(clone);
				
				Transition portal = new Transition(IdGenerator.getNextId(),
						((ArrayList<ElementLocation>)getSelectedElementLocations().clone()) );
				portal.setName(oldName);
				portal.setComment(oldComment);
				getGraphPanelNodes().add(portal);
			}
		}
		getGraphPanel().repaint();
	}

	/**
	 * Metoda zwi¹zana w mouseClicked(MouseEvent), odpowiedzialna za zwiêkszenie tokenów
	 * w miejscu, po wykryciu podwójnego klikniêcia.
	 */
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

	/**
	 * Metoda zwi¹zana w mouseClicked(MouseEvent), odpowiedzialna za zmniejszenie tokenów
	 * w miejscu, po wykryciu podwójnego klikniêcia.
	 */
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
	 * Metoda powoduje zaznaczenie ³uku podanego w parametrze metody (Arc.isSelected = true)
	 * oraz dodanie go do listy zaznaczonych ³uków.
	 * @param arc Arc - ³uk który ma zostaæ zaznaczony
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
	 * Metoda powoduje zaznaczenie tylko jednego ³uku (Arc.isSelected = true)ze wszystkich
	 * ³uków znajduj¹cych siê na danym arkuszu. W wyniku wykonania tej metody, wszystkie
	 * ³uki poza wybranym zostaj¹ odznaczone, a lista zaznaczonych ³uków zostaje wyczyszczona.
	 * @param arc Arc - ³uk który jako jedyny ma zostaæ zaznaczony
	 */
	public void selectOneArc(Arc arc) {
		for (Arc a : this.getGraphPanelArcs())
			if (a.getLocationSheetId() == this.getGraphPanel().getSheetId())
				a.setSelected(false);
		this.getSelectedArcs().clear();
		this.selectArc(arc);
	}

	/**
	 * Metoda zwraca pierwszy ³uk, dla którego spe³niony jest warunek przeciêcia z podanym
	 * w parametrze punktem. Metoda przeszukuje wszystkie ³uki zawarte na danym arkuszu,
	 * wybieraj¹c pierwszy, dla którego odleg³oœæ punktu podanego w parametrze jest mniejsza
	 * ni¿ 2 od odcinka stanowi¹cego ³uk.
	 * @param p Point - punkt wzglêdem którego ma byæ badane przeciêcie
	 * @return Arc - pierwszy obiekty klasy Arc, dla którego warunek przeciêcia zosta³ spe³niony.
	 * 		Jeœli takiego ³uku nie znaleziono, zostaje zwrócona wartoœæ null
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
	 * Metoda pozwala sprawdziæ czy podany w parametrze ³uk znajduje siê na liœcie zaznaczonych ³uków.
	 * @param arc Arc - ³uk dla którego bêdzie sprawdzona obecnoœæ na liœcie .
	 * @return boolean - true w przypadku gdy podany ³uk znajduje siê siê na liœcie zaznaczonych ³uków.
	 * 		W przeciwnym przypadku zwraca false
	 */
	public boolean isArcSelected(Arc arc) {
		return this.getSelectedArcs().contains(arc);
	}

	/**
	 * Metoda powoduje odznaczanie podanego w parametrze ³uku (Arc.isSelected = false) oraz
	 * usuniêcie go z listy zaznaczonych ³uków.
	 * @param arc Arc - ³uk który ma zostaæ odznaczony
	 */
	public void deselectArc(Arc arc) {
		this.getSelectedArcs().remove(arc);
		arc.setSelected(false);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odwrócenie zaznaczenia podanego w parametrze ³uku. W sytuacji gdy
	 * podany ³uk jest zaznaczony (Arc.isSelected == true), zostaje on odznaczony poprzez
	 * wywo³anie metody deselectArc(arc). Jeœli natomiast nie jest on zaznaczony, wywo³ana
	 * zostaje metoda selectArc(arc) zaznaczaj¹ca go.
	 * @param arc Arc - ³uk którego zaznaczenie ma zostaæ odwrócone
	 */
	public void toggleArcSelection(Arc arc) {
		if (arc.getSelected())
			this.deselectArc(arc);
		else
			this.selectArc(arc);
	}

	/**
	 * Metoda powoduje usuniêcie podanego w parametrze ³uku.
	 * @param arc Arc - ³uk który ma zostaæ usuniêty
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
	 * Metoda sprawdza dla ka¿dego ³uku istniej¹cego w sieci, czy jego pocz¹tkowa oraz
	 * koñcowa lokalizacja wierzcho³ka s¹ zaznaczone. W takiej sytuacji, ³uk ³¹cz¹cy dwa
	 * zaznaczone lokalizacje wierzcho³ków (ElementLocation.isSelected = true) zostaje
	 * równie¿ automatycznie zaznaczony.
	 */
	public void checkArcsForSelection() {
		for (Arc a : this.getGraphPanelArcs())
			if (a.checkSelection())
				this.getSelectedArcs().add(a);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odznaczenie wszystkich ³uków Arc.isSelected = false znajduj¹cych siê
	 * na bie¿¹cym arkuszu.
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzcho³ków
	 * (ElementLocation.isSelected = false) oraz ³uków (Arc.isSelected = false) znajduj¹cych
	 * siê na listach zaznaczenia dla danego arkusza. Listy te nastêpnie s¹ czyszczone.
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

	/**
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzcho³ków
	 * (ElementLocation.isSelected = false) oraz ³uków (Arc.isSelected = false) znajduj¹cych
	 * siê na bie¿¹cym arkuszu. W przeciwieñstwie do metody deselectAllElements() przeszukiwane
	 * s¹ wszystkie lokalizacje wierzcho³ków oraz ³uki z których zbudowana jest sieæ, a nie
	 * tylko te które znajduj¹ siê na listach zaznaczenia. Metoda ta jest zdecydowanie bardziej
	 * obci¹¿aj¹ca dla procesora, zapewnia jednak ¿e wszystkie elementy zostan¹ odznaczone. Nie
	 * zaleca siê jednak jej stosowania, gdy¿ b³êdy tego typu s¹ napotykane niezwykle rzadko.
	 */
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
	 * Metoda powoduje usuniêcie wszystkich lokalizacji wierzcho³ków (ElementLocation) oraz ³uków
	 * (Arc) znajduj¹cych siê na listach zaznaczenia. Zasady stosowane podczas usuwanie s¹
	 * identyczne z tymi pojawiaj¹cymi siê w metodach deleteElementLocation(el) oraz deleteArc(arc),
	 * jednak nie s¹ one tutaj wywo³ywane.
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
	 * Metoda powoduje zaznaczenie wszystkich lokalizacji wierzcho³ków (ElementLocation) oraz
	 * co za tym idzie, wszystkich ³uków znajduj¹cych siê wewn¹trz podanego w parametrze
	 * prostok¹tnego obszaru.
	 * @param rectangle Rectangle - prostok¹tny obszar, wewn¹trz którego elementy maj¹ zostaæ zaznaczone
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

	/**
	 * Metoda wywo³ywana w sytuacji zaznaczenia arkusza. Powoduje wywo³anie metody actionPerformed 
	 * biektu nas³uchuj¹cego przypisanego do danego SelectionManager, z parametrami przekazuj¹cymi
	 * aktualny arkusz.
	 */
	public void selectSheet() {
		SelectionActionEvent actionEvent = new SelectionActionEvent(getGraphPanel().getSheetId());
		this.getActionListener().actionPerformed(actionEvent);
	}

	// ================================================================================
	// Dragging operations
	// ================================================================================

	/**
	 * Metoda wywo³ywana w sytuacji gdy grupa zaznaczonych obiektów jest przenoszona. Powoduje
	 * wywo³anie metody actionPerformed obiektu nas³uchuj¹cego przypisanego do danego SelectionManager,
	 * z parametrami przekazuj¹cymi aktualnie zaznaczone obiekty. Umo¿liwia to m. in. podgl¹d na
	 * bie¿¹co zmiany wspó³rzêdnych pozycji obiektu który jest aktualnie przemieszczany.
	 */
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

	/**
	 * Metoda usuwaj¹ca œwiecenie tranzycji (zwyk³ych i czasowych). Wykorzystywana (poœrednio
	 * poprzez metodê z obiektu klasy PetriNet) przez metody odpowiedzialne za podœwietlanie
	 * wybranych tranzycji oraz zbiorów MCT.
	 */
	public void removeTransitionsGlowing() {
		for (Node n : getGraphPanelNodes())
			if (n.getType() == PetriNetElementType.TRANSITION )
				((Transition) n).setGlowed(false, 0);
			else if (n.getType() == PetriNetElementType.TIMETRANSITION)
				((TimeTransition) n).setGlowed(false, 0);
	}
}
