package abyss.graphpanel;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;

/**
 * Zadaniem klasy SelectionManager jest zarządzanie zaznaczeniem oraz obiektami które są aktualnie
 * zaznaczone na danym arkuszu. SelectionManager zawsze ma przypisanego arkusza-rodzica
 * GraphPanel, którego obiektami zarządza nie wpływając nigdy na obiekty pozostałych arkuszy.
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
	 * Konstruktor domyślny obiektu klasy SelectionManager.
	 * @param parentGraphPanel GraphPanel - obiekt panelu
	 */
	public SelectionManager(GraphPanel parentGraphPanel) {
		this.setGraphPanel(parentGraphPanel);
		this.graphPanelNodes = parentGraphPanel.getNodes();
		this.graphPanelArcs = parentGraphPanel.getArcs();
	}

	/**
	 * Metoda pobierająca aktualny obiekt arkusza do rysowania sieci.
	 * @return GraphPanel - obiekt arkusza
	 */
	private GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Metoda ustawiająca nowy obiekt jako arkusz rysowania sieci.
	 * @param parentGraphPanel GraphPanel - nowy obiekt arkusza
	 */
	private void setGraphPanel(GraphPanel parentGraphPanel) {
		this.graphPanel = parentGraphPanel;
	}

	/**
	 * Metoda zwracająca wszystkie wierzchołki sieci.
	 * @return ArrayList[Node] - lista wierzchołków sieci w arkuszu
	 */
	private ArrayList<Node> getGraphPanelNodes() {
		return graphPanelNodes;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda ustawiająca nową tablicę wierzchołków arkusza sieci.
	 * @param graphPanelNodes ArrayList[Node] - nowa tablica węzłów
	 */
	private void setGraphPanelNodes(ArrayList<Node> graphPanelNodes) {
		this.graphPanelNodes = graphPanelNodes;
	}

	/**
	 * Metoda zwracająca listę łuków sieci.
	 * @return ArrayList[Arc] - lista łuków
	 */
	private ArrayList<Arc> getGraphPanelArcs() {
		return graphPanelArcs;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda ustawiająca nową listę łuków panelu graficznego.
	 * @param graphPanelArcs ArrayList[Arc] - lista łuków
	 */
	private void setGraphPanelArcs(ArrayList<Arc> graphPanelArcs) {
		this.graphPanelArcs = graphPanelArcs;
	}

	/**
	 * Metoda umożliwia pobranie aktualnej listy zaznaczonych lokalizacji wierzchołków
	 * (jeden wierzchołek może zawierać więcej niż jedną lokalizację reprezentowaną graficznie)
	 * @return ArrayList[ElementLocation] - lista zaznaczonych lokalizacji
	 */
	public ArrayList<ElementLocation> getSelectedElementLocations() {
		return selectedElementLocations;
	}

	/**
	 * Metoda umożliwia ustawienie aktualnej listy zaznaczonych lokalizacji wierzchołków
	 * (jeden wierzchołek może zawierać więcej niż jedną lokalizację reprezentowaną graficznie)
	 * @param selectedElementLocations ArrayList[ElementLocation] - nowa lista zaznaczonych 
	 * 		lokalizacji wierzchołków
	 */
	public void setSelectedElementLocations(ArrayList<ElementLocation> selectedElementLocations) {
		this.selectedElementLocations = selectedElementLocations;
	}

	/**
	 * Metoda umożliwia pobranie aktualnej listy zaznaczonych łuków.
	 * @return ArrayList[Arc] - lista zaznaczonych łuków
	 */
	public ArrayList<Arc> getSelectedArcs() {
		return selectedArcs;
	}

	/**
	 * Metoda umożliwia ustawienie aktualnej listy zaznaczonych łuków.
	 * @param selectedArcs ArrayList[Arc] - nowa lista zaznaczonych łuków
	 */
	public void setSelectedArcs(ArrayList<Arc> selectedArcs) {
		this.selectedArcs = selectedArcs;
	}

	/**
	 * Metoda pozwala ustawić obiekt nasłuchujący zmian zaznaczenia. Przy każdej
	 * zmianie zaznaczenia, zostanie wywołana metoda actionPerfomed w obiekcie
	 * implementującym interfejs SelectionActionListener, w której parametrze zostanie
	 * przekazany obiekt SelectionActionEvent.
	 * @param e SelectionActionListener - obiekt nasłuchujący, implementujący interfejs SelectionActionListener
	 */
	public void setActionListener(SelectionActionListener e) {
		this.actionListener = e;
	}

	/**
	 * Metoda pozwala pobrania obiektu nasłuchującego zmian zaznaczenia.
	 * @return SelectionActionListener - obiekt nasłuchujący, implementujący interfejs SelectionActionListener
	 */
	public SelectionActionListener getActionListener() {
		return actionListener;
	}

	/**
	 * Metoda aktywująca dalsze metody, w zaleźności od tego, co zostało kliknięte lub
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
	 * Metoda dodaje podaną w parametrze lokalizację wierzchołka (pojedynczy wierzchołek
	 * posiada 1 lub więcej lokalizacji wyświetlania, reprezentowaną klasą ElementLocation
	 * do listy zaznaczonych lokalizacji wierzchołków oraz ustawia pole 
	 * ElementLocation.isSelected = true. Powoduje to automatyczne odświeżenie widoku rysowania
	 * oraz wywołanie obiektu nasłuchującego. Jeśli po wykonaniu tej operacji, na liście
	 * zaznaczonych lokalizacji wierzchołków istnieją więcej niż dwa obiekty, prawdopodobnym
	 * jest że są one połączone łukiem, który w takiej sytuacji powinien zostać automatycznie
	 * zaznaczony. Sprawdzenie tej możliwości dokonywane jest za pomocą wywołania metody
	 * checkArcsForSelection.
	 * @param el ElementLocation - lokalizacja wierzchołka która ma zostać zaznaczona
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
	 * Metoda powoduje zaznaczenie tylko podanej w parametrze lokalizacji wierzchołka.
	 * Wszelkie pozostałe lokalizacje wierzchołków zostają odznaczone
	 * ElementLocation.isSelected = false oraz wszystkie łuki rc.isSelected = false,
	 * a listy przechowujących elementy zaznaczone (zarówno dla ElementLocation jak i
	 * Arc) zostają wyczyszczone. Następnie korzystając z metody 
	 * selectElementLocation(abyss.math.ElementLocation el podana lokalizacja wierzchołka
	 * zostaje zaznaczona.
	 * @param el ElementLocation - lokalizacja wierzchołka który jedyny ma zostać zaznaczony
	 */
	public void selectOneElementLocation(ElementLocation el) {
		this.deselectAllElementLocations();
		this.deselectAllArcs();
		this.selectElementLocation(el);
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odwrócenie zaznaczenia podanej w parametrze lokalizacji wierzchołka.
	 * Jeśli podana w parametrze lokalizacja wierzchołka jest zaznaczona
	 * ElementLocation.isSelected == true wykonana zostaje metoda deselectElementLocation(el)
	 * , w przeciwnym przypadku wykonana zostaje metoda selectElementLocation(el).
	 * @param el ElementLocation - lokalizacja wierzchołka której zaznaczenie ma zostać odwrócone
	 */
	public void toggleElementLocationSelection(ElementLocation el) {
		if (!this.isElementLocationSelected(el))
			this.selectElementLocation(el);
		else
			this.deselectElementLocation(el);
	}

	/**
	 * Metoda zwraca pierwszą lokalizację wierzchołka, dla której spełniony jest warunek
	 * przecięcia z podanym w parametrze punktem. Metoda przeszukuje wszystkie lokalizacje
	 * ElementLocation wszystkich wierzchołków zawartych na danym arkuszu, dla których
	 * odległość punktu przekazanego w parametrze od punktu środkowego pozycji Point danej
	 * lokalizacji wierzchołka ElementLocation.Postion jest mniejsza od promienia danego wierzchołka.
	 * @param p Point - punkt dla którego będą sprawdzane warunki przecięcia
	 * @return ElementLocation - dla którego warunek przecięcia został spełniony. Jeśli taka
	 * 		lokalizacja nie została znaleziona, zwracana jest wartość null
	 */
	public ElementLocation getPossiblySelectedElementLocation(Point p) {
		for (Node n : this.getGraphPanelNodes()) {
			int sheetID = this.getGraphPanel().getSheetId();
			ElementLocation el = n.getLocationWhichContains(p, sheetID);
			if (el != null)
				return el;
		}
		return null;
	}

	/**
	 * Metoda powoduje odznaczenie podanej w parametrze lokalizacji wierzchołka ElementLocation.
	 * Podany w parametrze \textit{ElementLocation} zostaje usunięty z list zaznaczonych lokalizacji
	 * wierzchołka \textit{ElementLocation} oraz zostaje on odznaczony ElementLocation.isSelected = false.
	 * Jeśli po wykonaniu tej operacji, na liście zaznaczonych lokalizacji wierzchołków zostały więcej
	 * niż dwa obiekty, prawdopodobnym jest że są one połączone łukiem, który w takiej sytuacji powinien
	 * zostać automatycznie zaznaczony. Sprawdzenie tej możliwości dokonywane jest za pomocą wywołania
	 * metody checkArcsForSelection().
	 * @param el ElementLocation - lokalizacja wierzchołka która ma zostać odznaczona
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
	 * Metoda pozwala sprawdzić czy podana w parametrze lokalizacja wierzchołka ElementLocation
	 * znajduje się na liście zaznaczonych lokalizacji wierzchołków.
	 * @param el ElementLocation - lokalizacja wierzchołka której obecność na liście zaznaczenia
	 * 		ma zostać sprawdzona
	 * @return boolean - true, jeśli podany w parametrze ElementLocation znajduje się na liście
	 * 		zaznaczonych; false w sytuacji przeciwnej
	 */
	public boolean isElementLocationSelected(ElementLocation el) {
		return this.selectedElementLocations.contains(el);
	}

	/**
	 * Metoda usuwa podaną w parametrze lokalizację wierzchołka oraz wszystkie przyłączone do
	 * niego łuki (Arc). W sytuacji gdy podana lokalizacja była jedną lokalizacją jej
	 * wierzchołka-rodzica, wierzchołek ten również zostaje usunięty.
	 * @param el ElementLocation - lokalizacja wierzchołka który ma został usunięty
	 */
	public void deleteElementLocation(ElementLocation el) {
		this.deselectElementLocation(el);
		Node n = el.getParentNode();
		if (n.removeElementLocation(el) == false) {
			this.getGraphPanelNodes().remove(n);
		}
		
		for(Arc arc : el.getInArcs()) {
			this.getGraphPanelArcs().remove(arc);
			//dostań się do lokacji startowej łuku i usuń go tam z listy wyjściowych:
			arc.getStartLocation().removeOutArc(arc);
		}
		
		for(Arc arc : el.getOutArcs()) {
			this.getGraphPanelArcs().remove(arc);
			//dostań się do lokacji docelowej łuku i usuń go tam z listy wejściowych:
			arc.getEndLocation().removeInArc(arc);
		}
		
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	// ================================================================================
	// Group Element Location operations
	// ================================================================================

	/**
	 * Metoda powoduje zaznaczanie zbioru lokalizacji wierzchołków podanego w parametrze metody.
	 * Dla każdej lokalizacji z podanego zbioru, sprawdzane jest czy dany obiekt nie znajduje się
	 * juz na liście lokalizacji zaznaczonych. Jeśli nie, jest on do niej dodawany oraz ustawiany
	 * jest dla niego zaznaczenie (ElementLocation.isSelected = true). Jeśli po wykonaniu tej
	 * operacji, na liście zaznaczonych lokalizacji wierzchołków zostały więcej niż dwa obiekty,
	 * prawdopodobnym jest że są one połączone łukiem (Arc), który w takiej sytuacji powinien
	 * został automatycznie zaznaczony. Sprawdzenie tej możliwości dokonywane jest za pomocą
	 * wywołania metody checkArcsForSelection().
	 * @param elementLocationGroup ArrayList[ElementLocation] - zbiór który ma zostać zaznaczony
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
	 * wierzchołków (Node) oraz co za tym idzie, wszystkich łuków (Arc) znajdujących się
	 * na danym arkuszu. W jej wyniku wszystkie lokalizacje wierzchołków z danego arkusza
	 * oraz łuki zostaną określone jako zaznaczone (odpowiednio ElementLocation.isSelected = true
	 *  oraz Arc.isSelected = true) i dodane do list obiektów zaznaczonych.
	 */
	public void selectAllElementLocations() {
		this.getSelectedElementLocations().clear();
		this.getSelectedArcs().clear();
		for (Node n : this.getGraphPanelNodes())
			for (ElementLocation el : n.getNodeLocations(getGraphPanel().getSheetId())) {
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzchołków ElementLocation
	 * oraz co za tym idzie, wszystkich łuków znajdujących się na danym arkuszu. W jej
	 * wyniku wszystkie lokalizacje wierzchołków z danego arkusza oraz łuki zostaną
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
	 * Metoda zmienia aktualnie zaznaczone elementy w portal, przenosząc je do
	 * jednego obiektu Node.
	 * Dodano komunikaty ostrzegające oraz zachowywanie starych danych pierwszego
	 * zaznaczonego obiektu węzła sieci.
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
		
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		}
		int selectedNodeIndex = getGraphPanelNodes().indexOf(getSelectedElementLocations().get(0).getParentNode());
		
		for (ElementLocation el : this.getSelectedElementLocations()) {
			if(selectedNodeIndex > getGraphPanelNodes().indexOf(el.getParentNode()))
				selectedNodeIndex = getGraphPanelNodes().indexOf(el.getParentNode());
			
			if (el.getParentNode().isPortal()) //usuwanie statusu portal
				for (ElementLocation e : el.getParentNode().getNodeLocations())
					e.setPortalSelected(false);
			if (!el.getParentNode().removeElementLocation(el))
				this.getGraphPanelNodes().remove(el.getParentNode()); //usuwanie węzła sieci z danych sieci
		}
		
		//tutaj jednak obiekt(y) wciąż istnieje i można np. sprawdzić jego typ
		if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.PLACE) {
			String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
			String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
			int oldTokensNumber = ((Place)getSelectedElementLocations().get(0).getParentNode()).getTokensNumber();
			int oldTokensTaken = ((Place)getSelectedElementLocations().get(0).getParentNode()).getReservedTokens();
			Place portal = new Place(IdGenerator.getNextId(),
					(ArrayList<ElementLocation>)getSelectedElementLocations().clone()); 
			
			//TODO: poprawić, bo teraz tylko zeruje przesunięcie napisów
			ArrayList<ElementLocation> namesLocations = new ArrayList<ElementLocation>();
			int sid = getSelectedElementLocations().get(0).getParentNode().getElementLocations().get(0).getSheetID();
			for(int i=0; i<getSelectedElementLocations().size(); i++) {	
				namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
			}
			portal.setNamesLocations(namesLocations);
			
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.setTokensNumber(oldTokensNumber);
			portal.reserveTokens(oldTokensTaken);
			//getGraphPanelNodes().add(portal);
			getGraphPanelNodes().add(selectedNodeIndex, portal);
		} else {
			//@SuppressWarnings("unused")
			//String test = getSelectedElementLocations().get(0).getParentNode().getType().toString();
			if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TIMETRANSITION) {
				String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
				String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
				double oldEFT = ((TimeTransition)getSelectedElementLocations().get(0).getParentNode()).getMinFireTime();
				double oldLFT = ((TimeTransition)getSelectedElementLocations().get(0).getParentNode()).getMaxFireTime();
				TimeTransition portal = new TimeTransition(IdGenerator.getNextId(),
						(ArrayList<ElementLocation>)getSelectedElementLocations().clone());
				
				//TODO: poprawić, bo teraz tylko zeruje przesunięcie napisów
				ArrayList<ElementLocation> namesLocations = new ArrayList<ElementLocation>();
				int sid = getSelectedElementLocations().get(0).getParentNode().getElementLocations().get(0).getSheetID();
				for(int i=0; i<getSelectedElementLocations().size(); i++) {	
					namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
				}
				portal.setNamesLocations(namesLocations);
				
				portal.setName(oldName);
				portal.setComment(oldComment);
				portal.setMinFireTime(oldEFT);
				portal.setMaxFireTime(oldLFT);
				//getGraphPanelNodes().add(portal);
				getGraphPanelNodes().add(selectedNodeIndex, portal);
			} else if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TRANSITION){
				String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
				String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
				Transition portal = new Transition(IdGenerator.getNextId(),
						((ArrayList<ElementLocation>)getSelectedElementLocations().clone()) );
				
				//TODO: poprawić, bo teraz tylko zeruje przesunięcie napisów
				ArrayList<ElementLocation> namesLocations = new ArrayList<ElementLocation>();
				int sid = getSelectedElementLocations().get(0).getParentNode().getElementLocations().get(0).getSheetID();
				for(int i=0; i<getSelectedElementLocations().size(); i++) {	
					namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
				}
				portal.setNamesLocations(namesLocations);
				
				portal.setName(oldName);
				portal.setComment(oldComment);
				//getGraphPanelNodes().add(portal);
				getGraphPanelNodes().add(selectedNodeIndex, portal);
			}
		}
		getGraphPanel().repaint();
	}

	/**
	 * Metoda zmienia aktualnie kliknięty element w portal, tworząc jego klona.
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
		
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		}
		//dodawanie innych miejsc dla samego portalu do selectedElementLocations
		ElementLocation nodeSelectedEL = this.getSelectedElementLocations().get(0); //wybrana lokalizacja
		Node nodeSelected = nodeSelectedEL.getParentNode(); //wybrany wierzchołek
		//ArrayList<ElementLocation> namesLocations = new ArrayList<ElementLocation>(nodeSelected.getNamesLocations());
		ArrayList<ElementLocation> namesLocations = new ArrayList<ElementLocation>();
		int selectedNodeIndex = getGraphPanelNodes().indexOf(nodeSelected);
		
		ArrayList<ElementLocation> otherNodes = nodeSelected.getElementLocations(); //lista jego (innych?) lokacji
		
		int indClicked = nodeSelected.getElementLocations().indexOf(nodeSelectedEL);
		namesLocations.add(nodeSelected.getNamesLocations().get(indClicked));
		for (ElementLocation el : otherNodes) { 
			if(el.equals(nodeSelectedEL) == false) {
				selectedElementLocations.add(el);
				indClicked = nodeSelected.getElementLocations().indexOf(el); //odtwarzanie nowej kolejności dla namesLocations
				namesLocations.add(nodeSelected.getNamesLocations().get(indClicked));
			} 
		}
		
		for (ElementLocation el : this.getSelectedElementLocations()) { 
			if (el.getParentNode().isPortal()) //usuwanie statusu portal
				for (ElementLocation e : el.getParentNode().getNodeLocations())
					e.setPortalSelected(false);
			if (el.getParentNode().removeElementLocation(el) == false)
				this.getGraphPanelNodes().remove(el.getParentNode()); //usuwanie węzła sieci z danych sieci
		}
		
		if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.PLACE) {
			String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
			String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
			int oldTokensNumber = ((Place)getSelectedElementLocations().get(0).getParentNode()).getTokensNumber();
			int oldTokensTaken = ((Place)getSelectedElementLocations().get(0).getParentNode()).getReservedTokens();
			
			ElementLocation clonedNode = getSelectedElementLocations().get(0);
			Point newPosition = new Point();
			newPosition.setLocation(clonedNode.getPosition().getX()+30, clonedNode.getPosition().getY()+30);
			ElementLocation clone = new ElementLocation(clonedNode.getSheetID(), newPosition, clonedNode.getParentNode());
			//clone.setInArcs((ArrayList<Arc>)clonedNode.getInArcs().clone());
			//clone.setOutArcs((ArrayList<Arc>)clonedNode.getOutArcs().clone());
			clone.setSelected(clonedNode.isSelected());
			clone.setPortalSelected(clonedNode.isPortalSelected());
			selectedElementLocations.add(clone);
			
			Place portal = new Place(IdGenerator.getNextId(), (ArrayList<ElementLocation>)getSelectedElementLocations().clone()); 

			//klonowanie lokalizacji nazw + dodatkowy wpis:
			int sid = namesLocations.get(0).getSheetID();
			namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
			portal.setNamesLocations(namesLocations);
			//portal.getNamesLocations().add(e)
			
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.setTokensNumber(oldTokensNumber);
			portal.reserveTokens(oldTokensTaken);
			//getGraphPanelNodes().add(portal);
			getGraphPanelNodes().add(selectedNodeIndex, portal);
		} else {
			@SuppressWarnings("unused")
			String test = getSelectedElementLocations().get(0).getParentNode().getType().toString();
			if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TIMETRANSITION) {
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
				
				//klonowanie lokalizacji nazw + dodatkowy wpis:
				//ArrayList<ElementLocation> namesLocations = nodeSelected.getNamesLocations();
				int sid = namesLocations.get(0).getSheetID();
				namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
				portal.setNamesLocations(namesLocations);
				
				portal.setName(oldName);
				portal.setComment(oldComment);
				portal.setMinFireTime(oldEFT);
				portal.setMaxFireTime(oldLFT);
				
				//getGraphPanelNodes().add(portal);
				getGraphPanelNodes().add(selectedNodeIndex, portal);
			} else if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TRANSITION){
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
				
				//klonowanie lokalizacji nazw + dodatkowy wpis:
				//ArrayList<ElementLocation> namesLocations = nodeSelected.getNamesLocations();
				int sid = namesLocations.get(0).getSheetID();
				namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
				portal.setNamesLocations(namesLocations);
				
				portal.setName(oldName);
				portal.setComment(oldComment);
				//getGraphPanelNodes().add(portal);
				getGraphPanelNodes().add(selectedNodeIndex, portal);
			}
		}
		getGraphPanel().repaint();
	}

	/**
	 * Metoda związana w mouseClicked(MouseEvent), odpowiedzialna za zwiększenie tokenów
	 * w miejscu, po wykryciu podwójnego kliknięcia.
	 */
	public void increaseTokensNumber() {
		ArrayList<Node> safetyNodesList = new ArrayList<Node>();
		for (ElementLocation el : getSelectedElementLocations()) {
			if (el.getParentNode().getType() == PetriNetElementType.PLACE && !safetyNodesList.contains(el.getParentNode())) {
				safetyNodesList.add(el.getParentNode());
				((Place) el.getParentNode()).modifyTokensNumber(1);
			}
		}
		invokeActionListener();
	}

	/**
	 * Metoda związana w mouseClicked(MouseEvent), odpowiedzialna za zmniejszenie tokenów
	 * w miejscu, po wykryciu podwójnego kliknięcia.
	 */
	public void decreaseTokensNumber() {
		ArrayList<Node> safetyNodesList = new ArrayList<Node>();
		for (ElementLocation el : getSelectedElementLocations()) {
			if (el.getParentNode().getType() == PetriNetElementType.PLACE && !safetyNodesList.contains(el.getParentNode())) {
				safetyNodesList.add(el.getParentNode());
				int tokens = ((Place) el.getParentNode()).getTokensNumber();
				if(tokens >= 1)
					((Place) el.getParentNode()).modifyTokensNumber(-1);
			}
		}
		invokeActionListener();
	}

	// ================================================================================
	// Single Arc operations
	// ================================================================================
	/**
	 * Metoda powoduje zaznaczenie łuku podanego w parametrze metody (Arc.isSelected = true)
	 * oraz dodanie go do listy zaznaczonych łuków.
	 * @param arc Arc - łuk który ma zostać zaznaczony
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
	 * Metoda powoduje zaznaczenie tylko jednego łuku (Arc.isSelected = true)ze wszystkich
	 * łuków znajdujących się na danym arkuszu. W wyniku wykonania tej metody, wszystkie
	 * łuki poza wybranym zostają odznaczone, a lista zaznaczonych łuków zostaje wyczyszczona.
	 * @param arc Arc - łuk który jako jedyny ma zostać zaznaczony
	 */
	public void selectOneArc(Arc arc) {
		for (Arc a : this.getGraphPanelArcs())
			if (a.getLocationSheetId() == this.getGraphPanel().getSheetId())
				a.setSelected(false);
		this.getSelectedArcs().clear();
		this.selectArc(arc);
	}

	/**
	 * Metoda zwraca pierwszy łuk, dla którego spełniony jest warunek przecięcia z podanym
	 * w parametrze punktem. Metoda przeszukuje wszystkie łuki zawarte na danym arkuszu,
	 * wybierając pierwszy, dla którego odległość punktu podanego w parametrze jest mniejsza
	 * niż 2 od odcinka stanowiącego łuk.
	 * @param p Point - punkt względem którego ma być badane przecięcie
	 * @return Arc - pierwszy obiekty klasy Arc, dla którego warunek przecięcia został spełniony.
	 * 		Jeśli takiego łuku nie znaleziono, zostaje zwrócona wartość null
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
	 * Metoda pozwala sprawdzić czy podany w parametrze łuk znajduje się na liście zaznaczonych łuków.
	 * @param arc Arc - łuk dla którego będzie sprawdzona obecność na liście .
	 * @return boolean - true w przypadku gdy podany łuk znajduje się się na liście zaznaczonych łuków.
	 * 		W przeciwnym przypadku zwraca false
	 */
	public boolean isArcSelected(Arc arc) {
		return this.getSelectedArcs().contains(arc);
	}

	/**
	 * Metoda powoduje odznaczanie podanego w parametrze łuku (Arc.isSelected = false) oraz
	 * usunięcie go z listy zaznaczonych łuków.
	 * @param arc Arc - łuk który ma zostać odznaczony
	 */
	public void deselectArc(Arc arc) {
		this.getSelectedArcs().remove(arc);
		arc.setSelected(false);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odwrócenie zaznaczenia podanego w parametrze łuku. W sytuacji gdy
	 * podany łuk jest zaznaczony (Arc.isSelected == true), zostaje on odznaczony poprzez
	 * wywołanie metody deselectArc(arc). Jeśli natomiast nie jest on zaznaczony, wywołana
	 * zostaje metoda selectArc(arc) zaznaczająca go.
	 * @param arc Arc - łuk którego zaznaczenie ma zostać odwrócone
	 */
	public void toggleArcSelection(Arc arc) {
		if (arc.getSelected())
			this.deselectArc(arc);
		else
			this.selectArc(arc);
	}

	/**
	 * Metoda powoduje usunięcie podanego w parametrze łuku.
	 * @param arc Arc - łuk który ma zostać usunięty
	 */
	public void deleteArc(Arc arc) {
		arc.unlinkElementLocations();
		this.deselectArc(arc);
		this.getGraphPanelArcs().remove(arc);
		this.getGraphPanel().repaint();
		if (arc.getPairedArc() != null) { // jeśli to read-arc, usuń też łuk sparowany
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
	 * Metoda sprawdza dla każdego łuku istniejącego w sieci, czy jego początkowa oraz
	 * końcowa lokalizacja wierzchołka są zaznaczone. W takiej sytuacji, łuk łączący dwa
	 * zaznaczone lokalizacje wierzchołków (ElementLocation.isSelected = true) zostaje
	 * również automatycznie zaznaczony.
	 */
	public void checkArcsForSelection() {
		for (Arc a : this.getGraphPanelArcs())
			if (a.checkSelection())
				this.getSelectedArcs().add(a);
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje odznaczenie wszystkich łuków Arc.isSelected = false znajdujących się
	 * na bieżącym arkuszu.
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzchołków
	 * (ElementLocation.isSelected = false) oraz łuków (Arc.isSelected = false) znajdujących
	 * się na listach zaznaczenia dla danego arkusza. Listy te następnie są czyszczone.
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
	 * Metoda powoduje odznaczenie wszystkich lokalizacji wierzchołków
	 * (ElementLocation.isSelected = false) oraz łuków (Arc.isSelected = false) znajdujących
	 * się na bieżącym arkuszu. W przeciwieństwie do metody deselectAllElements() przeszukiwane
	 * są wszystkie lokalizacje wierzchołków oraz łuki z których zbudowana jest sieć, a nie
	 * tylko te które znajdują się na listach zaznaczenia. Metoda ta jest zdecydowanie bardziej
	 * obciążająca dla procesora, zapewnia jednak że wszystkie elementy zostaną odznaczone. Nie
	 * zaleca się jednak jej stosowania, gdyż błędy tego typu są napotykane niezwykle rzadko.
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
	 * Metoda powoduje usunięcie wszystkich lokalizacji wierzchołków (ElementLocation) oraz łuków
	 * (Arc) znajdujących się na listach zaznaczenia. Zasady stosowane podczas usuwanie są
	 * identyczne z tymi pojawiającymi się w metodach deleteElementLocation(el) oraz deleteArc(arc),
	 * jednak nie są one tutaj wywoływane.
	 */
	public void deleteAllSelectedElements() {
		// code below looks similar to other function but not use them to reduce
		// the number of requests repaint
		for (Iterator<ElementLocation> i = this.getSelectedElementLocations().iterator(); i.hasNext();) {
			ElementLocation el = i.next();
			Node n = el.getParentNode();
			// jeżeli ElementLocation to jedyna lokalizacja dla Node, tutaj jest kasowana:
			if (n.removeElementLocation(el) == false) {
				this.getGraphPanelNodes().remove(n);
			}
			// kasowanie wszystkich in-arcs danej ElementLocation
			for (Iterator<Arc> j = el.getInArcs().iterator(); j.hasNext();) {

				this.getGraphPanelArcs().remove(j.next());
				j.remove();
			}
			// kasowanie wszystkich out-arcs danej ElementLocation
			for (Iterator<Arc> j = el.getOutArcs().iterator(); j.hasNext();) {

				this.getGraphPanelArcs().remove(j.next());
				j.remove();
			}
			i.remove();
		}
		// kasuje wszystkie zaznaczone łuki:
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
		// Kasuj wszystko. I wszystkich. Wszędzie. Kill'em all:
		this.getSelectedArcs().clear();
		this.getSelectedElementLocations().clear();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
	}

	/**
	 * Metoda powoduje zaznaczenie wszystkich lokalizacji wierzchołków (ElementLocation) oraz
	 * co za tym idzie, wszystkich łuków znajdujących się wewnątrz podanego w parametrze
	 * prostokątnego obszaru.
	 * @param rectangle Rectangle - prostokątny obszar, wewnątrz którego elementy mają zostać zaznaczone
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
	 * Metoda wywoływana w sytuacji zaznaczenia arkusza. Powoduje wywołanie metody actionPerformed 
	 * biektu nasłuchującego przypisanego do danego SelectionManager, z parametrami przekazującymi
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
	 * Metoda wywoływana w sytuacji gdy grupa zaznaczonych obiektów jest przenoszona. Powoduje
	 * wywołanie metody actionPerformed obiektu nasłuchującego przypisanego do danego SelectionManager,
	 * z parametrami przekazującymi aktualnie zaznaczone obiekty. Umożliwia to m. in. podgląd na
	 * bieżąco zmiany współrzędnych pozycji obiektu który jest aktualnie przemieszczany.
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
	 * Metoda usuwająca status świecenia tranzycji (zwykłych i czasowych). Wykorzystywana (pośrednio
	 * poprzez metodę z obiektu klasy PetriNet) przez metody odpowiedzialne za podświetlanie
	 * wybranych tranzycji oraz zbiorów MCT.
	 */
	public void removeTransitionsGlowing() {
		for (Node n : getGraphPanelNodes())
			if (n.getType() == PetriNetElementType.TRANSITION )
				((Transition) n).setGlowedINV(false, 0);
			else if (n.getType() == PetriNetElementType.TIMETRANSITION)
				((TimeTransition) n).setGlowedINV(false, 0);
	}
}
