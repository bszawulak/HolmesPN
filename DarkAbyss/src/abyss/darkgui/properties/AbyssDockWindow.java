package abyss.darkgui.properties;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import abyss.analyzer.InvariantsSimulator;
import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
//import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.math.Arc;
import abyss.math.InvariantTransition;
import abyss.math.Node;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;

import com.javadocking.dock.SingleDock;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;

/**
 * Metoda odpowiedzialna za podokno programu, w którym gromadzone s¹ kolejne zak³adki
 * jak np. symulator, analizator, edytor, itd. Wychwytuje ona miêdzy innymi
 * zdarzenia klikniêcia na jakiœ element np. sieci, nastêpnie zlecaj¹c utworzenie
 * podokna wyœwietlaj¹cego odpowiednie w³aœciwoœci, przyciski, opcje, itd.
 * @author students
 *
 */
public class AbyssDockWindow extends SingleDock {
	private static final long serialVersionUID = -1966643269924197502L;
	private Dockable dockable;
	private Point position;
	private GUIManager guiManager;
	private AbyssDockWindowsTable dockWindowPanel;
	private SelectionPanel selectionPanel;
	private JScrollPane scrollPane;
	private DockWindowType type;

	/**
	 * EDITOR, SIMULATOR, SELECTOR, InvANALYZER, PropANALYZER, MctANALYZER,InvSIMULATOR
	 */
	public enum DockWindowType {
		EDITOR, SIMULATOR, SELECTOR, InvANALYZER, PropANALYZER, MctANALYZER,InvSIMULATOR
	}

	/**
	 * Konstruktor obiektu klasy Properties
	 * @param propertiesType PropertiesType - typ w³aœciwoœci do dodania
	 */ 
	public AbyssDockWindow(DockWindowType propertiesType) {
		type = propertiesType;
		scrollPane = new JScrollPane();
		guiManager = GUIManager.getDefaultGUIManager();

		if (type == DockWindowType.EDITOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("NetElement", scrollPane,
					"Net Element"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.SIMULATOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Simulator", scrollPane,
					"Simulator"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.SELECTOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Selection", scrollPane,
					"Selection"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.InvANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants analysis", scrollPane,
					"Invariants"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.MctANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("MCT Groups", scrollPane,
					"MCT"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.PropANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Net properties", scrollPane,
					"Net Properties"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.InvSIMULATOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants simulator", scrollPane,
					"InvSim"),GUIManager.getDefaultGUIManager().getDockingListener()));
//		setDockable(GUIManager.getDefaultGUIManager()
//				.decorateDockableWithActions(getDockable(), false));

		position = new Point(0, 0);
		this.addDockable(getDockable(), position, position);

		if (type == DockWindowType.SELECTOR) {
			setSelectionPanel(new SelectionPanel());
			scrollPane.getViewport().add(getSelectionPanel());
		}
	}

	/**
	 * Metoda zwracaj¹ca podokno dokowalne intefejsu programu.
	 * @return Dockable - obiekt dokowalny
	 */
	public Dockable getDockable() {
		return dockable;
	}

	/**
	 * Metoda ustawiaj¹ca podokno dokowalne intefejsu programu.
	 * @return Dockable - nowy obiekt dokowalny
	 */
	private void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

	/**
	 * Metoda odpowiedzialna za wype³nienie sekcji symulatora sieci.
	 */
	public void createSimulatorProperties() {
		if (type == DockWindowType.SIMULATOR) {
			NetSimulator netSim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator();
			InvariantsSimulator invSim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvSimulator();
			
			setCurrentDockWindow(new AbyssDockWindowsTable(netSim, invSim));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda odpowiedzialna za wype³nienie sekcji symulatora inwariantów sieci.
	 */
	public void createInvSimulatorProperties2() {
		if (type == DockWindowType.InvSIMULATOR) {
			//poni¿sza metoda wywo³uje odpowiedni konstruktor obiektu klasy PropertiesTable
			setCurrentDockWindow(new AbyssDockWindowsTable(GUIManager.getDefaultGUIManager().getWorkspace()
					.getProject().getInvSimulator()));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}
	
	/**
	 * Metoda wywo³ywana podczas wczytywania inwariantów z programu INA
	 * @param invariants ArrayList[ArrayList[InvariantTransition]] - inwarianty
	 */
	public void showExternalInvariants(ArrayList<ArrayList<InvariantTransition>> invariants) {
		if (type == DockWindowType.InvANALYZER) {
			setCurrentDockWindow(new AbyssDockWindowsTable(invariants));
			scrollPane.getViewport().add(getCurrentDockWindow());
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer().settInvariants(invariants); 
		}
	}
	
	/**
	 * Metoda wywo³ywana po wygenerowaniu inwariantów przez program. Zleca wykonanie
	 * elementów interfejsu dla pokazywania inwariantów. W zasadzie nie ró¿ni siê od
	 * showExternalInvariants(...)
	 * @param invariants ArrayList[ArrayList[InvariantTransition]] - inwarianty
	 */
	public void showInvariants(ArrayList<ArrayList<InvariantTransition>> invariants) {
		if (type == DockWindowType.InvANALYZER) {
			setCurrentDockWindow(new AbyssDockWindowsTable(invariants));
			scrollPane.getViewport().add(getCurrentDockWindow());
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer().settInvariants(invariants); 
		}
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie w³aœciwoœci sieci Petriego.
	 * @param arrayList ArrayList[ArrayList[Object]] - macierz obiektów z w³aœciwoœciami
	 */
	public void showNetProperties(ArrayList<ArrayList<Object>> arrayList) {
		if (type == DockWindowType.PropANALYZER) {
			setCurrentDockWindow(new AbyssDockWindowsTable(arrayList,true));
			scrollPane.getViewport().add(getCurrentDockWindow());			
		}
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCT sieci.
	 * @param mctGroups ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
	 */
	public void showMCT(ArrayList<ArrayList<Transition>> mctGroups) {
		if (type == DockWindowType.MctANALYZER) {
			setCurrentDockWindow(new AbyssDockWindowsTable(mctGroups, DockWindowType.MctANALYZER));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda odpowiedzialna za uaktualnienie w³aœciwoœci.
	 */
	public void updateSimulatorProperties() {
		if (type == DockWindowType.SIMULATOR) {
			getCurrentDockWindow().updateSimulatorProperties(); //pusta metoda
		}
	}

	/**
	 * Metoda odpowiedzialna za wykrycie tego, co zosta³o klikniête w programie, i o ile
	 * to mo¿liwe - wyœwietlenie w³aœciwoœci tego czegoœ.
	 * @param e SelectionActionEvent - zdarzenie wyboru elementów
	 */
	public void selectElement(SelectionActionEvent e) {
		if (e.getActionType() == SelectionActionType.SELECTED_ONE) {
			if (e.getElementLocationGroup().size() > 0) {
				Node n = e.getElementLocation().getParentNode();
				if (n.getType() == PetriNetElementType.PLACE)
					setCurrentDockWindow(new AbyssDockWindowsTable((Place) n, e.getElementLocation()));
				else
				{
					@SuppressWarnings("unused")
					PetriNetElementType test =  n.getType();
					
					if (n.getType().equals(PetriNetElementType.TIMETRANSITION))
						setCurrentDockWindow(new AbyssDockWindowsTable((TimeTransition) n,e.getElementLocation()));
					else
						setCurrentDockWindow(new AbyssDockWindowsTable((Transition) n,e.getElementLocation()));
				}
				scrollPane.getViewport().add(getCurrentDockWindow());
			} else if (e.getArcGroup().size() > 0) {
				setCurrentDockWindow(new AbyssDockWindowsTable((Arc) e.getArc()));
				scrollPane.getViewport().add(getCurrentDockWindow());
			}
		} else if (e.getActionType() == SelectionActionType.SELECTED_SHEET) {
			setCurrentDockWindow(new AbyssDockWindowsTable(
				guiManager.getWorkspace().getSheets().get(guiManager.getWorkspace().getIndexOfId(e.getSheetId()))));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda zwracaj¹ca odpowiedni obiekt w³aœciwoœci, czyli obiekt zawieraj¹cy komponenty
	 * któregoœ z podokien programu wyœwietlaj¹ce przyciski, napisy, itd.
	 * @return AbyssDockWindowsTable - obiekt podokna z ramach okna w³aœciwoœci
	 */
	public AbyssDockWindowsTable getCurrentDockWindow() {
		return dockWindowPanel;
	}

	/**
	 * Metoda ustawiaj¹ca odpowiedni obiekt podokna, czyli obiekt zawieraj¹cy komponenty
	 * któregoœ z podokien programu wyœwietlaj¹cego np. przyciski symulatora czy informacje
	 * o elementach sieci.
	 * @return AbyssDockWindowsTable - obiekt podokna z ramach okna w³aœciwoœci
	 */
	private void setCurrentDockWindow(AbyssDockWindowsTable properties) {
		this.dockWindowPanel = properties;
	}

	/**
	 * Metoda zwracaj¹ca obiekt panelu wyœwietlaj¹cego zaznaczone elementy sieci.
	 * @return SelectionPanel - obiekt panelu
	 */
	public SelectionPanel getSelectionPanel() {
		return selectionPanel;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt panelu wyœwietlaj¹cego zaznaczone elementy sieci.
	 * @return SelectionPanel - obiekt panelu
	 */
	private void setSelectionPanel(SelectionPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
	}
}
