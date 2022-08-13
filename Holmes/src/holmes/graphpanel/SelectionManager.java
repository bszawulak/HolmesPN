package holmes.graphpanel;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.functions.FunctionsTools;

/**
 * Zadaniem klasy SelectionManager jest zarządzanie zaznaczeniem oraz obiektami które są aktualnie
 * zaznaczone na danym arkuszu. SelectionManager zawsze ma przypisanego arkusza-rodzica
 * GraphPanel, którego obiektami zarządza nie wpływając nigdy na obiekty pozostałych arkuszy.
 */
public class SelectionManager {
	private final GUIManager overlord;
	private GraphPanel graphPanel;
	private ArrayList<Node> graphPanelNodes;
	private ArrayList<Arc> graphPanelArcs;
	private ArrayList<ElementLocation> selectedElementLocations = new ArrayList<>();
	private ArrayList<Arc> selectedArcs = new ArrayList<>();
	private SelectionActionListener actionListener;

	/**
	 * Konstruktor domyślny obiektu klasy SelectionManager.
	 * @param parentGraphPanel GraphPanel - obiekt panelu
	 */
	public SelectionManager(GraphPanel parentGraphPanel) {
		this.setGraphPanel(parentGraphPanel);
		this.graphPanelNodes = parentGraphPanel.getNodes();
		this.graphPanelArcs = parentGraphPanel.getArcs();
		this.overlord = GUIManager.getDefaultGUIManager();
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


	/**
	 * Metoda ustawiająca nową tablicę wierzchołków arkusza sieci.
	 * @param graphPanelNodes ArrayList[Node] - nowa tablica węzłów
	 */
	@SuppressWarnings("unused")
	private void setGraphPanelNodes(ArrayList<Node> graphPanelNodes) {
		this.graphPanelNodes = graphPanelNodes;
	}

	/**
	 * Metoda zwracająca listę łuków sieci.
	 * @return ArrayList[Arc] - lista łuków
	 */
	public ArrayList<Arc> getGraphPanelArcs() {
		return graphPanelArcs;
	}


	/**
	 * Metoda ustawiająca nową listę łuków panelu graficznego.
	 * @param graphPanelArcs ArrayList[Arc] - lista łuków
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
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
	 * selectElementLocation(holmes.math.ElementLocation el podana lokalizacja wierzchołka
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
	 * @param p (<b>Point</b>) - punkt dla którego będą sprawdzane warunki przecięcia
	 * @param additionalRange (<b>int</b>) [2022-07] dodatkowy zakres wyszukiwania w pixelach, do tej pory
	 *                        tego nie było, więc tak jakby 0 dla działania domyślnego.
	 * @return (<b>ElementLocation</b>) - dla którego warunek przecięcia został spełniony. Jeśli taka
	 * 		lokalizacja nie została znaleziona, zwracana jest wartość null
	 */
	public ElementLocation getPossiblySelectedElementLocation(Point p, int additionalRange) {
		for (Node n : this.getGraphPanelNodes()) {
			int sheetID = this.getGraphPanel().getSheetId();
			ElementLocation el = n.getLocationWhichContains(p, sheetID, additionalRange);
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
		PetriNet pn = overlord.getWorkspace().getProject();
		ArrayList<Place> places = pn.getPlaces();
		ArrayList<Transition> transitions = pn.getTransitions();
		boolean functionWarning = false;
		boolean canDo = overlord.subnetsHQ.checkIfExpendable(el);
		if(!canDo) {
			JOptionPane.showMessageDialog(null, 
					"This element is the only one that leads to subnet\n"
					+ "with other portals of same node. Please remove all\n"
					+ "portals in correct subnets before removing THIS portal.", 
					"Cannot be removed now", JOptionPane.WARNING_MESSAGE);
			this.invokeActionListener();
			
		} else {
			ArrayList<Integer> sheetModified = new ArrayList<>();
			sheetModified.add(el.getSheetID());
			
			this.deselectElementLocation(el);
			Node n = el.getParentNode();
			if (!n.removeElementLocation(el)) {
				if(n instanceof Place) {
					int index = places.indexOf((Place)n);
					pn.accessStatesManager().removePlace(index);
					pn.accessSSAmanager().removePlace(index);
					if(FunctionsTools.revalidateFunctions((Place)n, index))
						functionWarning = true;
				}
				if(n instanceof Transition) {
					int index = transitions.indexOf((Transition)n);
					pn.accessFiringRatesManager().removeTrans(index);
				}
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
	
			for (Iterator<Arc> i = el.accessMetaInArcs().iterator(); i.hasNext();) {
				Arc a = i.next();
				this.getGraphPanelArcs().remove(a);
				a.getStartLocation().accessMetaOutArcs().remove(a);
				//a.unlinkElementLocations();
				i.remove();
			}
			
			for (Iterator<Arc> i = el.accessMetaOutArcs().iterator(); i.hasNext();) {
				Arc a = i.next();
				this.getGraphPanelArcs().remove(a);
				a.getEndLocation().accessMetaInArcs().remove(a);
				//a.unlinkElementLocations();
				i.remove();
			}
			
			overlord.subnetsHQ.validateMetaArcs(sheetModified, false, false);
			this.getGraphPanel().repaint();
			this.invokeActionListener();
			
			if(functionWarning) {
				overlord.log("Some functions have been affected by the removal operation. Please read reports above this message.", "error", true);
			}
		}
	}

	/**
	 * Metoda powoduje usunięcie wszystkich lokalizacji wierzchołków (ElementLocation) oraz łuków
	 * (Arc) znajdujących się na listach zaznaczenia. Zasady stosowane podczas usuwanie są
	 * identyczne z tymi pojawiającymi się w metodach deleteElementLocation(el) oraz deleteArc(arc),
	 * jednak nie są one tutaj wywoływane.
	 */
	public void deleteAllSelectedElements() {
		PetriNet pn = overlord.getWorkspace().getProject();
		ArrayList<Place> places = pn.getPlaces();
		ArrayList<Transition> transitions = pn.getTransitions();
		ArrayList<Integer> sheetsModified = new ArrayList<>();
		ArrayList<ElementLocation> protectedList = new ArrayList<>();
		boolean functionWarning = false;
		
		for (Iterator<ElementLocation> i = this.getSelectedElementLocations().iterator(); i.hasNext();) {
			ElementLocation el = i.next();
			
			boolean canDo = overlord.subnetsHQ.checkIfExpendable(el);
			if(!canDo) {
				protectedList.add(el);
				continue;
			}
			
			int sheetID = el.getSheetID();
			if(!sheetsModified.contains(sheetID))
				sheetsModified.add(sheetID);
			
			Node n = el.getParentNode();
			// jeżeli ElementLocation to jedyna lokalizacja dla Node, tutaj jest kasowana:
			if (!n.removeElementLocation(el)) {
				if(n instanceof Place) {
					int index = places.indexOf((Place)n);
					pn.accessStatesManager().removePlace(index);
					pn.accessSSAmanager().removePlace(index);
					if(FunctionsTools.revalidateFunctions((Place)n, index))
						functionWarning = true;
				}
				if(n instanceof Transition) {
					int index = transitions.indexOf((Transition)n);
					pn.accessFiringRatesManager().removeTrans(index);
				}
				this.getGraphPanelNodes().remove(n);
				
			}
			// kasowanie wszystkich in-arcs danej ElementLocation
			for (Iterator<Arc> j = el.getInArcs().iterator(); j.hasNext();) {
				Arc begone = j.next();
				this.getGraphPanelArcs().remove(begone);
				begone.getStartLocation().removeOutArc(begone);
				j.remove();
			}
			// kasowanie wszystkich out-arcs danej ElementLocation
			for (Iterator<Arc> j = el.getOutArcs().iterator(); j.hasNext();) {
				Arc begone = j.next();
				this.getGraphPanelArcs().remove(begone);
				begone.getEndLocation().removeInArc(begone);
				j.remove();
			}
			// kasowanie wszystkich in-meta-arcs danej ElementLocation
			for (Iterator<Arc> j = el.accessMetaInArcs().iterator(); j.hasNext();) {
				Arc a = j.next();
				this.getGraphPanelArcs().remove(a);
				a.getStartLocation().accessMetaOutArcs().remove(a);
				j.remove();
			}
			// kasowanie wszystkich out-meta-arcs danej ElementLocation
			for (Iterator<Arc> j = el.accessMetaOutArcs().iterator(); j.hasNext();) {
				Arc a = j.next();
				this.getGraphPanelArcs().remove(a);
				a.getEndLocation().accessMetaInArcs().remove(a);
				j.remove();
			}
			i.remove();
		}
		// kasuje wszystkie zaznaczone łuki:
		boolean securedDelete = protectedList.size() > 0;

		for (Iterator<Arc> i = this.getSelectedArcs().iterator(); i.hasNext();) {
			Arc a = i.next();
			
			if(securedDelete) { //nie kasuj łuków EL, który nie został skasowany
				if(protectedList.contains(a.getStartLocation()) || protectedList.contains(a.getEndLocation())) {
					continue;
				}
			}
			
			this.getGraphPanelArcs().remove(a);
			a.unlinkElementLocations();
			if (a.getPairedArc() != null) {
				Arc arc = a.getPairedArc();
				arc.unlinkElementLocations();
				getGraphPanelArcs().remove(arc);
			}
			i.remove();
		}
		
		overlord.subnetsHQ.validateMetaArcs(sheetsModified, false, false);
		
		// Kasuj wszystko. I wszystkich. Wszędzie. Kill'em all:
		this.getSelectedArcs().clear();
		this.getSelectedElementLocations().clear();
		this.getGraphPanel().repaint();
		this.invokeActionListener();
		
		if(securedDelete) {
			JOptionPane.showMessageDialog(null, 
					"Some element connected with subnets could not be deleted. Their corresponding\n"
					+ "portal within these subnets must be deleted first.", 
					"Cannot be removed", JOptionPane.WARNING_MESSAGE);
		}
		
		if(functionWarning) {
			overlord.log("Some functions have been affected by the removal operation. Please read reports above this message.", "error", true);
		}
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
	@SuppressWarnings("unused")
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
	 * Metoda zmienia aktualnie zaznaczone elementy w portal, przenosząc je do jednego obiektu Node.
	 * Dodano komunikaty ostrzegające oraz zachowywanie starych danych pierwszego zaznaczonego obiektu węzła sieci.
	 * @author students
	 * @author MR
	 */
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
		
		//overlord.getWorkspace().getProject().restoreMarkingZero();
		int selectedNodeIndex = getGraphPanelNodes().indexOf(getSelectedElementLocations().get(0).getParentNode());
		
		for (ElementLocation el : this.getSelectedElementLocations()) {
			if(selectedNodeIndex > getGraphPanelNodes().indexOf(el.getParentNode()))
				selectedNodeIndex = getGraphPanelNodes().indexOf(el.getParentNode());
			
			if (el.getParentNode().isPortal()) //usuwanie statusu portal
				for (ElementLocation e : el.getParentNode().getNodeLocations()) {
					e.setPortalSelected(false);
				}
			
			if (!el.getParentNode().removeElementLocation(el)) {
				this.getGraphPanelNodes().remove(el.getParentNode()); //usuwanie węzła sieci z danych sieci
			}
		}
		
		//tutaj jednak obiekt(y) wciąż istnieje i można np. sprawdzić jego typ
		if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.PLACE) {
			String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
			String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
			int oldTokensNumber = ((Place)getSelectedElementLocations().get(0).getParentNode()).getTokensNumber();
			int oldTokensTaken = ((Place)getSelectedElementLocations().get(0).getParentNode()).getReservedTokens();
			Place portal = new Place(IdGenerator.getNextId(), (ArrayList<ElementLocation>)getSelectedElementLocations().clone()); 
			
			// poprawić, bo teraz tylko zeruje przesunięcie napisów
			ArrayList<ElementLocation> namesLocations = new ArrayList<>();
			ArrayList<ElementLocation> alphaLocations = new ArrayList<>();
			ArrayList<ElementLocation> betaLocations = new ArrayList<>();
			ArrayList<ElementLocation> gammaLocations = new ArrayList<>();
			ArrayList<ElementLocation> tauLocations = new ArrayList<>();
			int sid = getSelectedElementLocations().get(0).getParentNode().getElementLocations().get(0).getSheetID();
			for(int i=0; i<getSelectedElementLocations().size(); i++) {	
				namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
				alphaLocations.add(new ElementLocation(sid, new Point(0,0), null));
				betaLocations.add(new ElementLocation(sid, new Point(0,0), null));
				gammaLocations.add(new ElementLocation(sid, new Point(0,0), null));
				tauLocations.add(new ElementLocation(sid, new Point(0,0), null));
			}
			portal.setTextsLocations(namesLocations, GUIManager.locationMoveType.NAME);
			portal.setTextsLocations(alphaLocations, GUIManager.locationMoveType.ALPHA);
			portal.setTextsLocations(betaLocations, GUIManager.locationMoveType.BETA);
			portal.setTextsLocations(gammaLocations, GUIManager.locationMoveType.GAMMA);
			portal.setTextsLocations(tauLocations, GUIManager.locationMoveType.TAU);
			
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.setTokensNumber(oldTokensNumber);
			portal.reserveTokens(oldTokensTaken);
			//getGraphPanelNodes().add(portal);
			getGraphPanelNodes().add(selectedNodeIndex, portal);
		} else if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TRANSITION){
			String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
			String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
			Transition selTransition = (Transition)getSelectedElementLocations().get(0).getParentNode();
			double oldEFT = selTransition.timeFunctions.getEFT();
			double oldLFT = selTransition.timeFunctions.getLFT();
			double oldDuration = selTransition.timeFunctions.getDPNduration();
			boolean oldTPNstatus = selTransition.timeFunctions.getTPNstatus();
			boolean oldDPNstatus = selTransition.timeFunctions.getDPNstatus();
			TransitionType tt = selTransition.getTransType();
			
			Transition portal = new Transition(IdGenerator.getNextId(), ((ArrayList<ElementLocation>)getSelectedElementLocations().clone()) );
			portal.setTransType(tt);
			
			// poprawić, bo teraz tylko zeruje przesunięcie napisów
			ArrayList<ElementLocation> namesLocations = new ArrayList<>();
			ArrayList<ElementLocation> alphaLocations = new ArrayList<>();
			ArrayList<ElementLocation> betaLocations = new ArrayList<>();
			ArrayList<ElementLocation> gammaLocations = new ArrayList<>();
			ArrayList<ElementLocation> tauLocations = new ArrayList<>();

			int sid = getSelectedElementLocations().get(0).getParentNode().getElementLocations().get(0).getSheetID();
			for(int i=0; i<getSelectedElementLocations().size(); i++) {	
				namesLocations.add(new ElementLocation(sid, new Point(0,0), null));
				alphaLocations.add(new ElementLocation(sid, new Point(0,0), null));
				betaLocations.add(new ElementLocation(sid, new Point(0,0), null));
				gammaLocations.add(new ElementLocation(sid, new Point(0,0), null));
				tauLocations.add(new ElementLocation(sid, new Point(0,0), null));
			}
			portal.setTextsLocations(namesLocations, GUIManager.locationMoveType.NAME);
			portal.setTextsLocations(alphaLocations, GUIManager.locationMoveType.ALPHA);
			portal.setTextsLocations(betaLocations, GUIManager.locationMoveType.BETA);
			portal.setTextsLocations(gammaLocations, GUIManager.locationMoveType.GAMMA);
			portal.setTextsLocations(tauLocations, GUIManager.locationMoveType.TAU);
			
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.timeFunctions.setEFT(oldEFT);
			portal.timeFunctions.setLFT(oldLFT);
			portal.timeFunctions.setDPNduration(oldDuration);
			portal.timeFunctions.setTPNstatus(oldTPNstatus);
			portal.timeFunctions.setDPNstatus(oldDPNstatus);
			getGraphPanelNodes().add(selectedNodeIndex, portal);
		}
		getGraphPanel().repaint();
	}

	/**
	 * Tworzenie portalu, czyli dodawanie nowego ElementLocation
	 */
	public void cloneNodeIntoPortalV2() {
		// sprawdzenie czy wszystkie elementy sa tego samego typu (Place lub Transition)
		if(this.getSelectedElementLocations().size() > 1) {
			//String type = this.getSelectedElementLocations().get(0).getParentNode().getType().toString();
			JOptionPane.showMessageDialog(null,"Cloning into Portals possible only for one selected node!",
					"Multiple selection warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		//overlord.getWorkspace().getProject().restoreMarkingZero();
		
		ElementLocation selectedEL = this.getSelectedElementLocations().get(0); //wybrana lokalizacja
		Node parent = selectedEL.getParentNode();
		
		int selectedX = selectedEL.getPosition().x + 30;
		int selectedY = selectedEL.getPosition().y + 30;
		int selectedSheedID = selectedEL.getSheetID();
		
		ElementLocation newGraphicsEL = new ElementLocation(selectedSheedID, new Point(selectedX, selectedY), parent);
		ElementLocation newNameEL = new ElementLocation(selectedSheedID, new Point(0, 0), parent);
		
		parent.getElementLocations().add(newGraphicsEL);
		parent.getTextsLocations(GUIManager.locationMoveType.NAME).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.BETA).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(newNameEL);
		parent.getTextsLocations(GUIManager.locationMoveType.TAU).add(newNameEL);
		parent.setPortal(true);
		
		getGraphPanel().repaint();
	}
	
	/**
	 * Metoda zmienia aktualnie kliknięty element w portal, tworząc jego klona.
	 * @author MR
	 */
	public void cloneNodeIntoPortal() {
		// sprawdzenie czy wszystkie elementy sa tego samego typu (Place lub Transition)
		if(this.getSelectedElementLocations().size() > 1) {
			//String type = this.getSelectedElementLocations().get(0).getParentNode().getType().toString();
			JOptionPane.showMessageDialog(null,"Cloning into Portals possible only for one selected node!",
					"Multiple selection warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		//overlord.getWorkspace().getProject().restoreMarkingZero();
		//dodawanie innych miejsc dla samego portalu do selectedElementLocations
		ElementLocation nodeSelectedEL = this.getSelectedElementLocations().get(0); //wybrana lokalizacja
		Node nodeSelected = nodeSelectedEL.getParentNode(); //wybrany wierzchołek
		ArrayList<ElementLocation> namesLocations = new ArrayList<>();
		ArrayList<ElementLocation> alphaLocations = new ArrayList<>();
		ArrayList<ElementLocation> betaLocations = new ArrayList<>();
		ArrayList<ElementLocation> gammaLocations = new ArrayList<>();
		ArrayList<ElementLocation> tauLocations = new ArrayList<>();

		int selectedNodeIndex = getGraphPanelNodes().indexOf(nodeSelected);
		
		ArrayList<ElementLocation> otherNodes = nodeSelected.getElementLocations(); //lista jego (innych?) lokacji
		
		int indClicked = nodeSelected.getElementLocations().indexOf(nodeSelectedEL);
		namesLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.NAME).get(indClicked));
		alphaLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.ALPHA).get(indClicked));
		betaLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.BETA).get(indClicked));
		gammaLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.GAMMA).get(indClicked));
		tauLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.TAU).get(indClicked));
		for (ElementLocation el : otherNodes) { 
			if(!el.equals(nodeSelectedEL)) {
				selectedElementLocations.add(el);
				indClicked = nodeSelected.getElementLocations().indexOf(el); //odtwarzanie nowej kolejności dla namesLocations
				namesLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.NAME).get(indClicked));
				alphaLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.ALPHA).get(indClicked));
				betaLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.BETA).get(indClicked));
				gammaLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.GAMMA).get(indClicked));
				tauLocations.add(nodeSelected.getTextsLocations(GUIManager.locationMoveType.TAU).get(indClicked));
			} 
		}
		
		for (ElementLocation el : this.getSelectedElementLocations()) { 
			if (el.getParentNode().isPortal()) //usuwanie statusu portal
				for (ElementLocation e : el.getParentNode().getNodeLocations())
					e.setPortalSelected(false);
			if (!el.getParentNode().removeElementLocation(el))
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
			clone.setSelected(clonedNode.isSelected());
			clone.setPortalSelected(clonedNode.isPortalSelected());
			selectedElementLocations.add(clone);
			
			Place portal = new Place(IdGenerator.getNextId(), (ArrayList<ElementLocation>)getSelectedElementLocations().clone()); 

			//klonowanie lokalizacji nazw + dodatkowy wpis:
			int sid = namesLocations.get(0).getSheetID(); //pierwszy EL
			namesLocations.add(new ElementLocation(sid, new Point(0,0), null)); //dodanie klona na koniec
			portal.setTextsLocations(namesLocations, GUIManager.locationMoveType.NAME); //ustawienie jako aktualny

			int sidA = alphaLocations.get(0).getSheetID(); //pierwszy EL
			alphaLocations.add(new ElementLocation(sidA, new Point(0,0), null)); //dodanie klona na koniec
			portal.setTextsLocations(alphaLocations, GUIManager.locationMoveType.ALPHA); //ustawienie jako aktualny

			int sidB = betaLocations.get(0).getSheetID(); //pierwszy EL
			betaLocations.add(new ElementLocation(sidB, new Point(0,0), null)); //dodanie klona na koniec
			portal.setTextsLocations(betaLocations, GUIManager.locationMoveType.BETA); //ustawienie jako aktualny

			int sidG = gammaLocations.get(0).getSheetID(); //pierwszy EL
			gammaLocations.add(new ElementLocation(sidG, new Point(0,0), null)); //dodanie klona na koniec
			portal.setTextsLocations(gammaLocations, GUIManager.locationMoveType.GAMMA); //ustawienie jako aktualny

			int sidT = tauLocations.get(0).getSheetID(); //pierwszy EL
			tauLocations.add(new ElementLocation(sidT, new Point(0,0), null)); //dodanie klona na koniec
			portal.setTextsLocations(tauLocations, GUIManager.locationMoveType.TAU); //ustawienie jako aktualny
			
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.setTokensNumber(oldTokensNumber);
			portal.reserveTokens(oldTokensTaken);
			getGraphPanelNodes().add(selectedNodeIndex, portal);
		} else if (getSelectedElementLocations().get(0).getParentNode().getType() == PetriNetElementType.TRANSITION) {
			String oldName = getSelectedElementLocations().get(0).getParentNode().getName();
			String oldComment = getSelectedElementLocations().get(0).getParentNode().getComment();
			Transition selTransition = (Transition)getSelectedElementLocations().get(0).getParentNode();
			double oldEFT = selTransition.timeFunctions.getEFT();
			double oldLFT =selTransition.timeFunctions.getLFT();
			double oldDuration = selTransition.timeFunctions.getDPNduration();
			boolean oldTPNstatus = selTransition.timeFunctions.getTPNstatus();
			boolean oldDPNstatus = selTransition.timeFunctions.getDPNstatus();
			TransitionType tt = selTransition.getTransType();
			
			ElementLocation clonedNode = getSelectedElementLocations().get(0);
			Point newPosition = new Point();
			newPosition.setLocation(clonedNode.getPosition().getX()+30, clonedNode.getPosition().getY()+30);
			
			ElementLocation clone = new ElementLocation(clonedNode.getSheetID(), newPosition, clonedNode.getParentNode());
			clone.setSelected(clonedNode.isSelected());
			clone.setPortalSelected(clonedNode.isPortalSelected());
			selectedElementLocations.add(clone);
			
			Transition portal = new Transition(IdGenerator.getNextId(), ((ArrayList<ElementLocation>)getSelectedElementLocations().clone()) );
			portal.setTransType(tt);

			//klonowanie lokalizacji nazw + dodatkowy wpis:
			int sid = namesLocations.get(0).getSheetID(); //pierwszy EL
			namesLocations.add(new ElementLocation(sid, new Point(0,0), null)); //dodanie klona
			portal.setTextsLocations(namesLocations, GUIManager.locationMoveType.NAME); //ustaw jak główną listę

			int sidA = alphaLocations.get(0).getSheetID(); //pierwszy EL
			alphaLocations.add(new ElementLocation(sidA, new Point(0,0), null)); //dodanie klona
			portal.setTextsLocations(alphaLocations, GUIManager.locationMoveType.ALPHA); //ustaw jak główną listę

			int sidB = betaLocations.get(0).getSheetID(); //pierwszy EL
			betaLocations.add(new ElementLocation(sidB, new Point(0,0), null)); //dodanie klona
			portal.setTextsLocations(betaLocations, GUIManager.locationMoveType.BETA); //ustaw jak główną listę

			int sidG = gammaLocations.get(0).getSheetID(); //pierwszy EL
			gammaLocations.add(new ElementLocation(sidG, new Point(0,0), null)); //dodanie klona
			portal.setTextsLocations(gammaLocations, GUIManager.locationMoveType.GAMMA); //ustaw jak główną listę

			int sidT = tauLocations.get(0).getSheetID(); //pierwszy EL
			tauLocations.add(new ElementLocation(sidT, new Point(0,0), null)); //dodanie klona
			portal.setTextsLocations(tauLocations, GUIManager.locationMoveType.TAU); //ustaw jak główną listę
			
			portal.setName(oldName);
			portal.setComment(oldComment);
			portal.timeFunctions.setEFT(oldEFT);
			portal.timeFunctions.setLFT(oldLFT);
			portal.timeFunctions.setDPNduration(oldDuration);
			portal.timeFunctions.setTPNstatus(oldTPNstatus);
			portal.timeFunctions.setDPNstatus(oldDPNstatus);
			getGraphPanelNodes().add(selectedNodeIndex, portal);
		}
		getGraphPanel().repaint();
	}

	/**
	 * Metoda związana w mouseClicked(MouseEvent), odpowiedzialna za zwiększenie tokenów
	 * w miejscu, po wykryciu podwójnego kliknięcia.
	 */
	public void doubleClickReactionHandler() {
		ArrayList<Node> safetyNodesList = new ArrayList<>();
		for (ElementLocation el : getSelectedElementLocations()) {
			if (el.getParentNode().getType() == PetriNetElementType.PLACE && !safetyNodesList.contains(el.getParentNode())) {
				safetyNodesList.add(el.getParentNode());
				Place place = (Place) el.getParentNode();

				if(place instanceof PlaceXTPN) {
					JOptionPane.showMessageDialog(null, "Cannot fast-increase tokens in XTPN place.",
							"Operation unavailable", JOptionPane.WARNING_MESSAGE);
					return;
				}

				place.modifyTokensNumber(1);
				if(overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN == 0) {
					int tokens = place.getTokensNumber();
					ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
					overlord.getWorkspace().getProject().accessStatesManager().getStatePN(0).setTokens(places.indexOf(place), tokens);
				}

			} else if(el.getParentNode().getType() == PetriNetElementType.META && !safetyNodesList.contains(el.getParentNode())) {
				try {
					MetaNode node = (MetaNode)el.getParentNode();
					safetyNodesList.add(node);
					int sheetID = node.getRepresentedSheetID();
					int sheetIndex = overlord.getWorkspace().getIndexOfId(sheetID);
					overlord.getWorkspace().setSelectedDock(sheetIndex);
					invokeActionListener();
					break;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Wrong sheet number linked to this meta-node.", 
							"Serious problem", JOptionPane.WARNING_MESSAGE);
					invokeActionListener();
					break;
				}
			} else {
				invokeActionListener();
			}
		}
		
	}

	/**
	 * Metoda związana w mouseClicked(MouseEvent), odpowiedzialna za zmniejszenie tokenów
	 * w miejscu, po wykryciu podwójnego kliknięcia.
	 */
	public void decreaseTokensNumber() {
		ArrayList<Node> safetyNodesList = new ArrayList<>();
		for (ElementLocation el : getSelectedElementLocations()) {
			if (el.getParentNode().getType() == PetriNetElementType.PLACE && !safetyNodesList.contains(el.getParentNode())) {
				if( el.getParentNode() instanceof PlaceXTPN) {
					JOptionPane.showMessageDialog(null, "Cannot fast-decrease tokens in XTPN place.",
							"Operation unavailable", JOptionPane.WARNING_MESSAGE);
				} else {
					safetyNodesList.add(el.getParentNode());
					int tokens = ((Place) el.getParentNode()).getTokensNumber();
					if(tokens >= 1) {
						((Place) el.getParentNode()).modifyTokensNumber(-1);

						if(overlord.getWorkspace().getProject().accessStatesManager().selectedStatePN == 0) {
							int value = ((Place) el.getParentNode()).getTokensNumber();
							ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
							overlord.getWorkspace().getProject().accessStatesManager().getStatePN(0).setTokens(places.indexOf(((Place) el.getParentNode())), value);
						}
					}
				}


			}
		}
		//invokeActionListener();
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
					//TODO:
					return a;
					
					//if (a.getPairedArc() != null && !a.isMainArcOfPair())
						//return a.getPairedArc();
					//else
						//return a;
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
	@SuppressWarnings("unused")
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
		overlord.markNetChange();
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
		for (ElementLocation el : this.getSelectedElementLocations()) {
			el.setSelected(false);
			//el.getParentNode().forceDeselection();
		}
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
			if (n.getType() == PetriNetElementType.TRANSITION ) {
				((Transition) n).drawGraphBoxT.setGlowedINV(false, 0);
			}
	}

    public void saveSubnet() {
		ArrayList<ElementLocation> listOfElements = new ArrayList<>();
		for(ElementLocation el : this.getSelectedElementLocations())
		{
			if(el.isSelected())
			{
				listOfElements.add(el);
			}
		}

		IOprotocols io = new IOprotocols();
		io.exportSubnet(listOfElements);
    }
}
