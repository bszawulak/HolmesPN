package abyss.darkgui.dockwindows;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import abyss.clusters.ClusterDataPackage;
import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
import abyss.math.Arc;
import abyss.math.MCSDataMatrix;
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
 * Metoda odpowiedzialna za podokno programu, w którym gromadzone są kolejne zakładki
 * jak np. symulator, analizator, edytor, itd. Wychwytuje ona między innymi
 * zdarzenia kliknięcia na jakiś element np. sieci, następnie zlecając utworzenie
 * podokna wyświetlającego odpowiednie właściwości, przyciski, opcje, itd.
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
		EDITOR, SIMULATOR, SELECTOR, InvANALYZER, ClusterSELECTOR, MctANALYZER, InvSIMULATOR, MCSselector, Knockout
	}

	/**
	 * Konstruktor obiektu klasy AbyssDockWindow. Tworzy czyste podokienko dokowane
	 * do interfejsu programu (wywołanie pochodzi z konstruktora GUIManager).
	 * Wypełnianie okna elementami jest już wykonywane zdalnie, na rządanie odpowiednią
	 * metodą.
	 * @param propertiesType DockWindowType - typ właściwości do dodania
	 */ 
	public AbyssDockWindow(DockWindowType propertiesType) {
		type = propertiesType;
		scrollPane = new JScrollPane();
		guiManager = GUIManager.getDefaultGUIManager();

		if (type == DockWindowType.EDITOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("NetElement", scrollPane,
					"Net Element"), GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.SIMULATOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Simulator", scrollPane,
					"Simulator"), GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.SELECTOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Selection", scrollPane,
					"Selection"), GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.InvANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants_analysis", scrollPane,
					"T-inv"), GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.MctANALYZER)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("MCT_Groups", scrollPane,
					"MCT"), GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.ClusterSELECTOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Clusters_Selection", scrollPane,
					"Clusters"), GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.MCSselector)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("MCS_selector", scrollPane,
					"MCS"), GUIManager.getDefaultGUIManager().getDockingListener()));
		else if (type == DockWindowType.Knockout)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Knockout_selector", scrollPane,
					"Knockout"), GUIManager.getDefaultGUIManager().getDockingListener()));
		
		else if (type == DockWindowType.InvSIMULATOR)
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Invariants_simulator", scrollPane,
					"InvSim"), GUIManager.getDefaultGUIManager().getDockingListener()));

		

		position = new Point(0, 0);
		this.addDockable(getDockable(), position, position);

		if (type == DockWindowType.SELECTOR) {
			setSelectionPanel(new SelectionPanel());
			scrollPane.getViewport().add(getSelectionPanel());
		}
	}

	/**
	 * Metoda zwracająca podokno dokowalne intefejsu programu.
	 * @return Dockable - obiekt dokowalny
	 */
	public Dockable getDockable() {
		return dockable;
	}

	/**
	 * Metoda ustawiająca podokno dokowalne intefejsu programu.
	 * @return Dockable - nowy obiekt dokowalny
	 */
	private void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

	/**
	 * Metoda odpowiedzialna za wypełnienie sekcji symulatora sieci.
	 */
	public void createSimulatorProperties() {
		if (type == DockWindowType.SIMULATOR) {
			NetSimulator netSim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator();
			//InvariantsSimulator invSim = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvSimulator();
			setCurrentDockWindow(new AbyssDockWindowsTable(netSim, null));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda odpowiedzialna za wypełnienie sekcji symulatora inwariantów sieci.
	 */
	//TODO: unused
	public void createInvSimulatorProperties2() {
		if (type == DockWindowType.InvSIMULATOR) {
			//poniższa metoda wywołuje odpowiedni konstruktor obiektu klasy PropertiesTable
			//setCurrentDockWindow(new AbyssDockWindowsTable(GUIManager.getDefaultGUIManager().getWorkspace()
			//		.getProject().getInvSimulator()));
			//scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}
	
	/**
	 * Metoda wywoływana po wygenerowaniu inwariantów przez program. Zleca wykonanie
	 * elementów interfejsu dla pokazywania inwariantów. W zasadzie nie różni się od
	 * showExternalInvariants(...)
	 * @param invariants ArrayList[ArrayList[InvariantTransition]] - inwarianty
	 */
	public void showInvariants(ArrayList<ArrayList<Integer>> invariants) {
		if (type == DockWindowType.InvANALYZER) {
			setCurrentDockWindow(new AbyssDockWindowsTable(invariants));
			scrollPane.getViewport().add(getCurrentDockWindow());
			//GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer().settInvariants(invariants); 
		}
	}
	
	/**
	 * Metoda wywoływana w momencie, kiedy z okna klastrów wpłyną dane o kolorach
	 * tranzycji w każdym klastrze. Wtedy tworzy całą resztę elementów podokna klastrów.
	 * @param coloredClustering ArrayList[ArrayList[Color]] - macierz kolorów
	 */
	public void showClusterSelector(ClusterDataPackage data) {
		if (type == DockWindowType.ClusterSELECTOR) {
			setCurrentDockWindow(new AbyssDockWindowsTable(data, true));
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
	 * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCS sieci.
	 */
	public void showMCS() {
		if (type == DockWindowType.MCSselector) {
			MCSDataMatrix mcsData = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
			setCurrentDockWindow(new AbyssDockWindowsTable(mcsData));
			scrollPane.getViewport().add(getCurrentDockWindow());

		}
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCS sieci.
	 */
	public void showKnockout(ArrayList<ArrayList<Integer>> knockoutData) {
		if (type == DockWindowType.Knockout) {
			setCurrentDockWindow(new AbyssDockWindowsTable(knockoutData, true, 55, true));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda odpowiedzialna za uaktualnienie właściwości.
	 */
	public void updateSimulatorProperties() {
		if (type == DockWindowType.SIMULATOR) {
			//getCurrentDockWindow().updateSimulatorProperties(); //pusta metoda
		}
	}

	/**
	 * Metoda odpowiedzialna za wykrycie tego, co zostało kliknięte w programie, i o ile
	 * to możliwe - wyświetlenie właściwości tego czegoś.
	 * Działa dla podokna EDYTOR
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
	 * Metoda zwracająca odpowiedni obiekt właściwości, czyli obiekt zawierający komponenty
	 * któregoś z podokien programu wyświetlające przyciski, napisy, itd.
	 * @return AbyssDockWindowsTable - obiekt podokna z ramach okna właściwości
	 */
	public AbyssDockWindowsTable getCurrentDockWindow() {
		return dockWindowPanel;
	}

	/**
	 * Metoda ustawiająca odpowiedni obiekt podokna, czyli obiekt zawierający komponenty
	 * któregoś z podokien programu wyświetlającego np. przyciski symulatora czy informacje
	 * o elementach sieci.
	 * @return AbyssDockWindowsTable - obiekt podokna z ramach okna właściwości
	 */
	public void setCurrentDockWindow(AbyssDockWindowsTable properties) {
		this.dockWindowPanel = properties;
	}

	/**
	 * Metoda zwracająca obiekt panelu wyświetlającego zaznaczone elementy sieci.
	 * @return SelectionPanel - obiekt panelu
	 */
	public SelectionPanel getSelectionPanel() {
		return selectionPanel;
	}

	/**
	 * Metoda ustawiająca nowy obiekt panelu wyąwietlającego zaznaczone elementy sieci.
	 * @return SelectionPanel - obiekt panelu
	 */
	private void setSelectionPanel(SelectionPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
	}
}
