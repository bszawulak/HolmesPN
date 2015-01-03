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
 * Zadaniem klasy SelectionManager jest zarz�dzanie zaznaczeniem oraz obiektami kt�re s� aktualnie
 * zaznaczone na danym arkuszu. SelectionManager zawsze ma przypisanego arkusza-rodzica
 * GraphPanel, kt�rego obiektami zarz�dza nie wp�ywaj�c nigdy na obiekty pozosta�ych arkuszy.
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
	 * Konstruktor domy�lny obiektu klasy SelectionManager.
	 * @param parentGraphPanel GraphPanel - obiekt panelu
	 */
	public SelectionManager(GraphPanel parentGraphPanel) {
		this.setGraphPanel(parentGraphPanel);
		this.graphPanelNodes = parentGraphPanel.getNodes();
		this.graphPanelArcs = parentGraphPanel.getArcs();
	}

	/**
	 * Metoda pobieraj�ca aktualny obiekt arkusza do rysowania sieci.
	 * @return GraphPanel - obiekt arkusza
	 */
	private GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Metoda ustawiaj�ca nowy obiekt jako arkusz rysowania sieci.
	 * @param parentGraphPanel GraphPanel - nowy obiekt arkusza
	 */
	private void setGraphPanel(GraphPanel parentGraphPanel) {
		this.graphPanel = parentGraphPanel;
	}

	/**
	 * Metoda zwracaj�ca wszystkie wierzcho�ki znajduj�ce si� w danym arkuszu rysowania.
	 * @return ArrayList[Node] - lista wierzcho�k�w sieci w arkuszu
	 */
	private ArrayList<Node> getGraphPanelNodes() {
		return graphPanelNodes;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda ustawiaj�ca now� tablic� wierzcho�k�w arkusza sieci.
	 * @param graphPanelNodes ArrayList[Node] - nowa tablica w�z��w
	 */
	private void setGraphPanelNodes(ArrayList<Node> graphPanelNodes) {
		this.graphPanelNodes = graphPanelNodes;
	}

	/**
	 * Metoda zwracaj�ca list� �uk�w panelu graficznego.
	 * @return ArrayList[Arc] - lista �uk�w
	 */
	private ArrayList<Arc> getGraphPanelArcs() {
		return graphPanelArcs;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda ustawiaj�ca now� list� �uk�w panelu graficznego.
	 * @param graphPanelArcs ArrayList[Arc] - lista �uk�w
	 */
	private void setGraphPanelArcs(ArrayList<Arc> graphPanelArcs) {
		this.graphPanelArcs = graphPanelArcs;
	}

	/**
	 * Metoda umo�liwia pobranie aktualnej listy zaznaczonych lokalizacji wierzcho�k�w
	 * (jeden wierzcho�ek mo�e zawiera� wi�cej ni� jedn� lokalizacj� reprezentowan� graficznie)
	 * @return ArrayList[ElementLocation] - lista zaznaczonych lokalizacji
	 */
	public ArrayList<ElementLocation> getSelectedElementLocations() {
		return selectedElementLocations;
	}

	/**
	 * Metoda umo�liwia ustawienie aktualnej listy zaznaczonych lokalizacji wierzcho�k�w
	 * (jeden wierzcho�ek mo�e zawiera� wi�cej ni� jedn� lokalizacj� reprezentowan� graficznie)
	 * @param selectedElementLocations ArrayList[ElementLocation] - nowa lista zaznaczonych 
	 * 		lokalizacji wierzcho�k�w
	 */
	public void setSelectedElementLocations(ArrayList<ElementLocation> selectedElementLocations) {
		this.selectedElementLocations = selectedElementLocations;
	}

	/**
	 * Metoda umo�liwia pobranie aktualnej listy zaznaczonych �uk�w.
	 * @return ArrayList[Arc] - list� zaznaczonych �uk�w
	 */
	public ArrayList<Arc> getSelectedArcs() {
		return selectedArcs;
	}

	/**
	 * Metoda umo�liwia ustawienie aktualnej listy zaznaczonych �uk�w.
	 * @param selectedArcs ArrayList[Arc] - nowa lista zaznaczonych �uk�w
	 */
	public void setSelectedArcs(ArrayList<Arc> selectedArcs) {
		this.selectedArcs = selectedArcs;
	}

	/**
	 * Metoda pozwala ustawi� obiekt nas�uchuj�cy zmian zaznaczenia. Przy ka�dej
	 * zmianie zaznaczenia, zostanie wywo�ana metoda actionPerfomed w obiekcie
	 * implementuj�cym interfejs SelectionActionListener, w kt�rej parametrze zostanie
	 * przekazany obiekt SelectionActionEvent.
	 * @param e SelectionActionListener - obiekt nas�uchuj�cy, implementuj�cy interfejs SelectionActionListener
	 */
	public void setActionListener(SelectionActionListener e) {
		this.actionListener = e;
	}

	/**
	 * Metoda pozwala pobrania obiektu nas�uchuj�cego zmian zaznaczenia.
	 * @return SelectionActionListener - obiekt nas�uchuj�cy, implementuj�cy interfejs SelectionActionListener
	 */
	public SelectionActionListener getActionListener() {
		return actionListener;
	}

	/**
	 * Metoda aktywuj�ca dalsze metody, w zale�no�ci od tego, co zosta�o klikni�te lub
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
	 * Metoda dodaje podan� w parametrze lokalizacj� wierzcho�ka (pojedynczy wierzcho�ek
	 * posiada 1 lub wi�cej lokalizacj� wy�wietlania, reprezentowan� klas� ElementLocation
	 * do listy zaznaczonych lokalizacji wierzcho�k�w oraz ustawia pole 
	 * ElementLocation.isSelected = true. Powoduje to automatyczne od�wie�enie widoku rysowania
	 * oraz wywo�anie obiektu nas�uchuj�cego. Je�li po wykonaniu tej operacji, na li�cie
	 * zaznaczonych lokalizacji wierzcho�k�w istniej� wi�cej ni� dwa obiekty, prawdopodobnym
	 * jest �e s� one po��czone �ukiem, kt�ry w takiej sytuacji powinien zosta� automatycznie
	 * zaznaczony. Sprawdzenie tej mo�liwo�ci dokonywane jest za pomoc� wywo�ania metody
	 * checkArcsForSelection.
	 * @param el ElementLocation - lokalizacja wierzcho�ka kt�ra ma zosta� zaznaczona
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
	 * Metoda powoduje zaznaczenie tylko podanej w parametrze lokalizacji wierzcho�ka.
	 * Wszelkie pozosta�e lokalizacje wierzcho�k�w zostaj� odznaczone
	 * ElementLocation.isSelected = false oraz wszystkie �uki rc.isSelected = false,
	 * a listy przechowuj�cych elementy zaznaczone (zar�wno dla ElementLocation jak i
	 * Arc) zostaj� wyczyszczone. Nast�pnie korzystaj�c z metody 
	 * selectElementLocation(abyss.math.ElementLocation el podana lokalizacja wierzcho�ka
	 * zostaje zaznaczona.
	 * @param el ElementLocation - lokalizacja wierzcho�ka kt�ra jedyny ma zosta� zaznaczona
	 */
	public void selectOneElementLocation(ElementLocation el) {
		this.deselectAllElementLocations();
		this.deselectAllArcs();
		this.selectElementLocation(el);
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odwr�cenie zaznaczenia podanej w parametrze lokalizacji wierzcho�ka.
	 * Je�li podana w parametrze lokalizacja wierzcho�ka jest zaznaczona
	 * ElementLocation.isSelected == true wykonana zostaje metoda deselectElementLocation(el)
	 * , w przeciwnym przypadku wykonana zostaje metoda selectElementLocation(el).
	 * @param el ElementLocation - lokalizacja wierzcho�ka kt�rej zaznaczenie ma zosta� odwr�cone
	 */
	public void toggleElementLocationSelection(ElementLocation el) {
		if (!this.isElementLocationSelected(el))
			this.selectElementLocation(el);
		else
			this.deselectElementLocation(el);
	}

	/**
	 * Metoda zwraca pierwsz� lokalizacj� wierzcho�ka, dla kt�rej spe�niony jest warunek
	 * przeci�cia z podanym w parametrze punktem. Metoda przeszukuje wszystkie lokalizacje
	 * ElementLocation wszystkich wierzcho�k�w zawartych na danym arkuszu, dla kt�rych
	 * odleg�o�� punktu przekazanego w parametrze od punktu �rodkowego pozycji Point danej
	 * lokalizacji wierzcho�ka ElementLocation.Postion jest mniejsza od promienia danego wierzcho�ka.
	 * @param p Point - punkt dla kt�rego b�d� sprawdzane warunki przeci�cia
	 * @return ElementLocation - dla kt�rego warunek przeci�cia zosta� spe�niony. Je�li taka
	 * 		lokalizacja nie zosta�a znaleziona, zwracana jest warto�� null
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
	 * Metoda powoduje odznaczenie podanej w parametrze lokalizacji wierzcho�ka ElementLocation.
	 * Podany w parametrze \textit{ElementLocation} zostaje usuni�ty z list zaznaczonych lokalizacji
	 * wierzcho�ka \textit{ElementLocation} oraz zostaje on odznaczony ElementLocation.isSelected = false.
	 * Je�li po wykonaniu tej operacji, na li�cie zaznaczonych lokalizacji wierzcho�k�w zosta�y wi�cej
	 * ni� dwa obiekty, prawdopodobnym jest �e s� one po��czone �ukiem, kt�ry w takiej sytuacji powinien
	 * zosta� automatycznie zaznaczony. Sprawdzenie tej mo�liwo�ci dokonywane jest za pomoc� wywo�ania
	 * metody checkArcsForSelection().
	 * @param el ElementLocation - lokalizacja wierzcho�ka kt�ra ma zosta� odznaczona
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
	 * Metoda pozwala sprawdzi� czy podana w parametrze lokalizacja wierzcho�ka ElementLocation
	 * znajduje si� na li�cie zaznaczonych lokalizacji wierzcho�k�w.
	 * @param el ElementLocation - lokalizacja wierzcho�ka kt�rej obecno�� na li�cie zaznaczenia
	 * 		ma zosta� sprawdzona
	 * @return boolean - true, je�li podany w parametrze ElementLocation znajduje si� na li�cie
	 * 		zaznaczonych; false w sytuacji przeciwnej
	 */
	public boolean isElementLocationSelected(ElementLocation el) {
		return this.selectedElementLocations.contains(el);
	}

	/**
	 * Metoda usuwa podan� w parametrze lokalizacj� wierzcho�ka oraz wszystkie przy��czone do
	 * niego �uki (Arc). W sytuacji gdy podana lokalizacja by�a jedn� lokalizacj� jej
	 * wierzcho�ka-rodzica, wierzcho�ek ten r�wnie� zostaje usuni�ty.
	 * @param el ElementLocation - lokalizacja wierzcho�ka kt�ry ma zosta� usuni�ty
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
	 * Metoda powoduje zaznaczanie zbioru lokalizacji wierzcho�k�w podanego w parametrze metody.
	 * Dla ka�dej lokalizacji z podanego zbioru, sprawdzane jest czy dany obiekt nie znajduje si�
	 * ju� na li�cie lokalizacji zaznaczonych. Je�li nie, jest on do niej dodawany oraz ustawiany
	 * jest dla niego zaznaczenie (ElementLocation.isSelected = true). Je�li po wykonaniu tej
	 * operacji, na li�cie zaznaczonych lokalizacji wierzcho�k�w zosta�y wi�cej ni� dwa obiekty,
	 * prawdopodobnym jest �e s� one po��czone �ukiem (Arc), kt�ry w takiej sytuacji powinien
	 * zosta� automatycznie zaznaczony. Sprawdzenie tej mo�liwo�ci dokonywane jest za pomoc�
	 * wywo�ania metody checkArcsForSelection().
	 * @param elementLocationGroup ArrayList[ElementLocation] - zbi�r kt�ry ma zosta� zaznaczony
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
	 * wierzcho�k�w (Node) oraz co za tym idzie, wszystkich �uk�w (Arc) znajduj�cych si�
	 * na danym arkuszu. W jej wyniku wszystkie lokalizacje wierzcho�k�w z danego arkusza
	 * oraz �uki zostan� okre�lone jako zaznaczone (odpowiednio ElementLocation.isSelected = true
	 *  oraz Arc.isSelected = true) i dodane do list obiekt�w zaznaczonych.
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzcho�k�w ElementLocation
	 * oraz co za tym idzie, wszystkich �uk�w znajduj�cych si� na danym arkuszu. W jej
	 * wyniku wszystkie lokalizacje wierzcho�k�w z danego arkusza oraz �uki zostan�
	 * odznaczone (odpowiednio ElementLocation.isSelected = false oraz Arc.isSelected = false),
	 * a listy obiekt�w zaznaczonych wyczyszczone.
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
	 * Metoda zmienia aktualnie zaznaczone elementy w portal, przenosz�c je do
	 * jednego obiektu Node.
	 * Dodano komunikaty ostrzegaj�ce oraz zachowywanie starych danych pierwszego
	 * zaznaczonego obiektu w�z�a
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
				this.getGraphPanelNodes().remove(el.getParentNode()); //usuwanie w�z�a sieci z danych sieci
		}
		//tutaj jednak obiekt(y) wci�� istnieje i mo�na np. sprawdzi� jego typ
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
	 * Metoda zmienia aktualnie klikni�ty element w portal, tworz�c jego klona.
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
		Node nodeSelected = nodeSelecredEL.getParentNode(); //wybrany wierzcho�ek
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
				this.getGraphPanelNodes().remove(el.getParentNode()); //usuwanie w�z�a sieci z danych sieci
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
	 * Metoda zwi�zana w mouseClicked(MouseEvent), odpowiedzialna za zwi�kszenie token�w
	 * w miejscu, po wykryciu podw�jnego klikni�cia.
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
	 * Metoda zwi�zana w mouseClicked(MouseEvent), odpowiedzialna za zmniejszenie token�w
	 * w miejscu, po wykryciu podw�jnego klikni�cia.
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
	 * Metoda powoduje zaznaczenie �uku podanego w parametrze metody (Arc.isSelected = true)
	 * oraz dodanie go do listy zaznaczonych �uk�w.
	 * @param arc Arc - �uk kt�ry ma zosta� zaznaczony
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
	 * Metoda powoduje zaznaczenie tylko jednego �uku (Arc.isSelected = true)ze wszystkich
	 * �uk�w znajduj�cych si� na danym arkuszu. W wyniku wykonania tej metody, wszystkie
	 * �uki poza wybranym zostaj� odznaczone, a lista zaznaczonych �uk�w zostaje wyczyszczona.
	 * @param arc Arc - �uk kt�ry jako jedyny ma zosta� zaznaczony
	 */
	public void selectOneArc(Arc arc) {
		for (Arc a : this.getGraphPanelArcs())
			if (a.getLocationSheetId() == this.getGraphPanel().getSheetId())
				a.setSelected(false);
		this.getSelectedArcs().clear();
		this.selectArc(arc);
	}

	/**
	 * Metoda zwraca pierwszy �uk, dla kt�rego spe�niony jest warunek przeci�cia z podanym
	 * w parametrze punktem. Metoda przeszukuje wszystkie �uki zawarte na danym arkuszu,
	 * wybieraj�c pierwszy, dla kt�rego odleg�o�� punktu podanego w parametrze jest mniejsza
	 * ni� 2 od odcinka stanowi�cego �uk.
	 * @param p Point - punkt wzgl�dem kt�rego ma by� badane przeci�cie
	 * @return Arc - pierwszy obiekty klasy Arc, dla kt�rego warunek przeci�cia zosta� spe�niony.
	 * 		Je�li takiego �uku nie znaleziono, zostaje zwr�cona warto�� null
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
	 * Metoda pozwala sprawdzi� czy podany w parametrze �uk znajduje si� na li�cie zaznaczonych �uk�w.
	 * @param arc Arc - �uk dla kt�rego b�dzie sprawdzona obecno�� na li�cie .
	 * @return boolean - true w przypadku gdy podany �uk znajduje si� si� na li�cie zaznaczonych �uk�w.
	 * 		W przeciwnym przypadku zwraca false
	 */
	public boolean isArcSelected(Arc arc) {
		return this.getSelectedArcs().contains(arc);
	}

	/**
	 * Metoda powoduje odznaczanie podanego w parametrze �uku (Arc.isSelected = false) oraz
	 * usuni�cie go z listy zaznaczonych �uk�w.
	 * @param arc Arc - �uk kt�ry ma zosta� odznaczony
	 */
	public void deselectArc(Arc arc) {
		this.getSelectedArcs().remove(arc);
		arc.setSelected(false);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odwr�cenie zaznaczenia podanego w parametrze �uku. W sytuacji gdy
	 * podany �uk jest zaznaczony (Arc.isSelected == true), zostaje on odznaczony poprzez
	 * wywo�anie metody deselectArc(arc). Je�li natomiast nie jest on zaznaczony, wywo�ana
	 * zostaje metoda selectArc(arc) zaznaczaj�ca go.
	 * @param arc Arc - �uk kt�rego zaznaczenie ma zosta� odwr�cone
	 */
	public void toggleArcSelection(Arc arc) {
		if (arc.getSelected())
			this.deselectArc(arc);
		else
			this.selectArc(arc);
	}

	/**
	 * Metoda powoduje usuni�cie podanego w parametrze �uku.
	 * @param arc Arc - �uk kt�ry ma zosta� usuni�ty
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
	 * Metoda sprawdza dla ka�dego �uku istniej�cego w sieci, czy jego pocz�tkowa oraz
	 * ko�cowa lokalizacja wierzcho�ka s� zaznaczone. W takiej sytuacji, �uk ��cz�cy dwa
	 * zaznaczone lokalizacje wierzcho�k�w (ElementLocation.isSelected = true) zostaje
	 * r�wnie� automatycznie zaznaczony.
	 */
	public void checkArcsForSelection() {
		for (Arc a : this.getGraphPanelArcs())
			if (a.checkSelection())
				this.getSelectedArcs().add(a);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odznaczenie wszystkich �uk�w Arc.isSelected = false znajduj�cych si�
	 * na bie��cym arkuszu.
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzcho�k�w
	 * (ElementLocation.isSelected = false) oraz �uk�w (Arc.isSelected = false) znajduj�cych
	 * si� na listach zaznaczenia dla danego arkusza. Listy te nast�pnie s� czyszczone.
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzcho�k�w
	 * (ElementLocation.isSelected = false) oraz �uk�w (Arc.isSelected = false) znajduj�cych
	 * si� na bie��cym arkuszu. W przeciwie�stwie do metody deselectAllElements() przeszukiwane
	 * s� wszystkie lokalizacje wierzcho�k�w oraz �uki z kt�rych zbudowana jest sie�, a nie
	 * tylko te kt�re znajduj� si� na listach zaznaczenia. Metoda ta jest zdecydowanie bardziej
	 * obci��aj�ca dla procesora, zapewnia jednak �e wszystkie elementy zostan� odznaczone. Nie
	 * zaleca si� jednak jej stosowania, gdy� b��dy tego typu s� napotykane niezwykle rzadko.
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
	 * Metoda powoduje usuni�cie wszystkich lokalizacji wierzcho�k�w (ElementLocation) oraz �uk�w
	 * (Arc) znajduj�cych si� na listach zaznaczenia. Zasady stosowane podczas usuwanie s�
	 * identyczne z tymi pojawiaj�cymi si� w metodach deleteElementLocation(el) oraz deleteArc(arc),
	 * jednak nie s� one tutaj wywo�ywane.
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
	 * Metoda powoduje zaznaczenie wszystkich lokalizacji wierzcho�k�w (ElementLocation) oraz
	 * co za tym idzie, wszystkich �uk�w znajduj�cych si� wewn�trz podanego w parametrze
	 * prostok�tnego obszaru.
	 * @param rectangle Rectangle - prostok�tny obszar, wewn�trz kt�rego elementy maj� zosta� zaznaczone
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
	 * Metoda wywo�ywana w sytuacji zaznaczenia arkusza. Powoduje wywo�anie metody actionPerformed 
	 * biektu nas�uchuj�cego przypisanego do danego SelectionManager, z parametrami przekazuj�cymi
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
	 * Metoda wywo�ywana w sytuacji gdy grupa zaznaczonych obiekt�w jest przenoszona. Powoduje
	 * wywo�anie metody actionPerformed obiektu nas�uchuj�cego przypisanego do danego SelectionManager,
	 * z parametrami przekazuj�cymi aktualnie zaznaczone obiekty. Umo�liwia to m. in. podgl�d na
	 * bie��co zmiany wsp�rz�dnych pozycji obiektu kt�ry jest aktualnie przemieszczany.
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
	 * Metoda usuwaj�ca status �wiecenia tranzycji (zwyk�ych i czasowych). Wykorzystywana (po�rednio
	 * poprzez metod� z obiektu klasy PetriNet) przez metody odpowiedzialne za pod�wietlanie
	 * wybranych tranzycji oraz zbior�w MCT.
	 */
	public void removeTransitionsGlowing() {
		for (Node n : getGraphPanelNodes())
			if (n.getType() == PetriNetElementType.TRANSITION )
				((Transition) n).setGlowed(false, 0);
			else if (n.getType() == PetriNetElementType.TIMETRANSITION)
				((TimeTransition) n).setGlowed(false, 0);
	}
}
