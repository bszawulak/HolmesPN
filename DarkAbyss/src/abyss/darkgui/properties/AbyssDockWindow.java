package abyss.darkgui.properties;

import java.awt.Color;
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
 * Metoda odpowiedzialna za podokno programu, w kt�rym gromadzone s� kolejne zak�adki
 * jak np. symulator, analizator, edytor, itd. Wychwytuje ona mi�dzy innymi
 * zdarzenia klikni�cia na jaki� element np. sieci, nast�pnie zlecaj�c utworzenie
 * podokna wy�wietlaj�cego odpowiednie w�a�ciwo�ci, przyciski, opcje, itd.
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
		EDITOR, SIMULATOR, SELECTOR, InvANALYZER, ClusterSELECTOR, MctANALYZER, InvSIMULATOR
	}

	/**
	 * Konstruktor obiektu klasy AbyssDockWindow. Tworzy czyste podokienko dokowane
	 * do interfejsu programu (wywo�anie pochodzi z konstruktora GUIManager).
	 * Wype�nianie okna elementami jest ju� wykonywane zdalnie, na rz�danie, odpowiedni�
	 * metod�.
	 * @param propertiesType DockWindowType - typ w�a�ciwo�ci do dodania
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
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants_analysis", scrollPane,
					"Invariants"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.MctANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("MCT_Groups", scrollPane,
					"MCT"),GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.ClusterSELECTOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Clusters_Selection", scrollPane,
					"Clusters"),GUIManager.getDefaultGUIManager().getDockingListener()));
		
		else if (type == DockWindowType.InvSIMULATOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants_simulator", scrollPane,
					"InvSim"),GUIManager.getDefaultGUIManager().getDockingListener()));

		

		position = new Point(0, 0);
		this.addDockable(getDockable(), position, position);

		if (type == DockWindowType.SELECTOR) {
			setSelectionPanel(new SelectionPanel());
			scrollPane.getViewport().add(getSelectionPanel());
		}
	}

	/**
	 * Metoda zwracaj�ca podokno dokowalne intefejsu programu.
	 * @return Dockable - obiekt dokowalny
	 */
	public Dockable getDockable() {
		return dockable;
	}

	/**
	 * Metoda ustawiaj�ca podokno dokowalne intefejsu programu.
	 * @return Dockable - nowy obiekt dokowalny
	 */
	private void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

	/**
	 * Metoda odpowiedzialna za wype�nienie sekcji symulatora sieci.
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
	 * Metoda odpowiedzialna za wype�nienie sekcji symulatora inwariant�w sieci.
	 */
	//TODO: unused
	public void createInvSimulatorProperties2() {
		if (type == DockWindowType.InvSIMULATOR) {
			//poni�sza metoda wywo�uje odpowiedni konstruktor obiektu klasy PropertiesTable
			setCurrentDockWindow(new AbyssDockWindowsTable(GUIManager.getDefaultGUIManager().getWorkspace()
					.getProject().getInvSimulator()));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}
	
	/**
	 * Metoda wywo�ywana po wygenerowaniu inwariant�w przez program. Zleca wykonanie
	 * element�w interfejsu dla pokazywania inwariant�w. W zasadzie nie r�ni si� od
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
	 * Metoda wywo�ywa w momencie, kiedy z okna klastr�w wp�yn� dane o kolorach
	 * tranzycji w ka�dym klastrze. Wtedy tworzy ca�� reszt� element�w podokna klastr�w.
	 * @param coloredClustering ArrayList[ArrayList[Color]] - macierz kolor�w
	 */
	public void showClusterSelector(ArrayList<ArrayList<Color>> coloredClustering) {
		if (type == DockWindowType.ClusterSELECTOR) {
			setCurrentDockWindow(new AbyssDockWindowsTable(coloredClustering, true));
			scrollPane.getViewport().add(getCurrentDockWindow());			
		}
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCT sieci.
	 * @param mctGroups ArrayList[ArrayList[Transition]] - macierz zbior�w MCT
	 */
	public void showMCT(ArrayList<ArrayList<Transition>> mctGroups) {
		if (type == DockWindowType.MctANALYZER) {
			setCurrentDockWindow(new AbyssDockWindowsTable(mctGroups, DockWindowType.MctANALYZER));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda odpowiedzialna za uaktualnienie w�a�ciwo�ci.
	 */
	public void updateSimulatorProperties() {
		if (type == DockWindowType.SIMULATOR) {
			//getCurrentDockWindow().updateSimulatorProperties(); //pusta metoda
		}
	}

	/**
	 * Metoda odpowiedzialna za wykrycie tego, co zosta�o klikni�te w programie, i o ile
	 * to mo�liwe - wy�wietlenie w�a�ciwo�ci tego czego�.
	 * Dzia�a dla podokna EDYTOR
	 * @param e SelectionActionEvent - zdarzenie wyboru element�w
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
	 * Metoda zwracaj�ca odpowiedni obiekt w�a�ciwo�ci, czyli obiekt zawieraj�cy komponenty
	 * kt�rego� z podokien programu wy�wietlaj�ce przyciski, napisy, itd.
	 * @return AbyssDockWindowsTable - obiekt podokna z ramach okna w�a�ciwo�ci
	 */
	public AbyssDockWindowsTable getCurrentDockWindow() {
		return dockWindowPanel;
	}

	/**
	 * Metoda ustawiaj�ca odpowiedni obiekt podokna, czyli obiekt zawieraj�cy komponenty
	 * kt�rego� z podokien programu wy�wietlaj�cego np. przyciski symulatora czy informacje
	 * o elementach sieci.
	 * @return AbyssDockWindowsTable - obiekt podokna z ramach okna w�a�ciwo�ci
	 */
	private void setCurrentDockWindow(AbyssDockWindowsTable properties) {
		this.dockWindowPanel = properties;
	}

	/**
	 * Metoda zwracaj�ca obiekt panelu wy�wietlaj�cego zaznaczone elementy sieci.
	 * @return SelectionPanel - obiekt panelu
	 */
	public SelectionPanel getSelectionPanel() {
		return selectionPanel;
	}

	/**
	 * Metoda ustawiaj�ca nowy obiekt panelu wy�wietlaj�cego zaznaczone elementy sieci.
	 * @return SelectionPanel - obiekt panelu
	 */
	private void setSelectionPanel(SelectionPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
	}
}
