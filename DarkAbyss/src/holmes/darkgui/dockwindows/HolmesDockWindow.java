package holmes.darkgui.dockwindows;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JScrollPane;

import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.GUIManager;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable.SubWindow;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent.SelectionActionType;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.simulators.NetSimulator;

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
public class HolmesDockWindow extends SingleDock {
	private static final long serialVersionUID = -1966643269924197502L;
	private Dockable dockable;
	private Point position;
	private GUIManager guiManager;
	private HolmesDockWindowsTable dockWindowPanel;
	private SelectionPanel selectionPanel;
	private JScrollPane scrollPane;
	private DockWindowType type;

	/**
	 * EDITOR, SIMULATOR, SELECTOR, InvANALYZER, ClusterSELECTOR, MctANALYZER, InvSIMULATOR, MCSselector, Knockout, FIXNET
	 */
	public enum DockWindowType {
		EDITOR, SIMULATOR, SELECTOR, T_INVARIANTS, P_INVARIANTS, ClusterSELECTOR, MctANALYZER, MCSselector, Knockout, FIXNET
	}

	/**
	 * Konstruktor obiektu klasy HolmesDockWindow. Tworzy czyste podokienko dokowane
	 * do interfejsu programu (wywołanie pochodzi z konstruktora GUIManager).
	 * Wypełnianie okna elementami jest już wykonywane zdalnie, na rządanie odpowiednią
	 * metodą.
	 * @param propertiesType DockWindowType - typ właściwości do dodania
	 */ 
	public HolmesDockWindow(DockWindowType propertiesType) {
		type = propertiesType;
		scrollPane = new JScrollPane();
		guiManager = GUIManager.getDefaultGUIManager();

		if (type == DockWindowType.EDITOR) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("NetElement", scrollPane,
					"Net Element"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.SIMULATOR) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Simulator", scrollPane,
					"Simulator"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.SELECTOR) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Selection", scrollPane,
					"Selection"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.T_INVARIANTS) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Transition_Invariants", scrollPane,
					"T-inv"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.P_INVARIANTS) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Place_Invariants", scrollPane,
					"P-inv"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.MctANALYZER) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("MCT_Groups", scrollPane,
					"MCT"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.ClusterSELECTOR) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Clusters_Selection", scrollPane,
					"Clusters"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.MCSselector) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("MCS_selector", scrollPane,
					"MCS"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.FIXNET) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Fix_selector", scrollPane,
					"Fix"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} else if (type == DockWindowType.Knockout) {
			setDockable(GUIManager.externalWithListener(new DefaultDockable("Knockout_selector", scrollPane,
					"Knockout"), GUIManager.getDefaultGUIManager().getDockingListener()));
		} 

		position = new Point(0, 0);
		this.addDockable(getDockable(), position, position);

		//immediate creation:
		if (type == DockWindowType.SELECTOR) {
			setSelectionPanel(new SelectionPanel());
			scrollPane.getViewport().add(getSelectionPanel());
		} else if (type == DockWindowType.FIXNET) {
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.FIXER));
			scrollPane.getViewport().add(getCurrentDockWindow());
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
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.SIMULATOR, netSim));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}
	
	/**
	 * Metoda wywoływana po wygenerowaniu t-inwariantów przez program. Zleca wykonanie
	 * elementów interfejsu dla pokazywania t-inwariantów. 
	 * @param t_invariants ArrayList[ArrayList[InvariantTransition]] - t-inwarianty
	 */
	public void showT_invBoxWindow(ArrayList<ArrayList<Integer>> t_invariants) {
		if (type == DockWindowType.T_INVARIANTS) {
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.T_INVARIANTS, t_invariants));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}
	
	/**
	 * Metoda wywoływana po wygenerowaniu t-inwariantów przez program. Zleca wykonanie
	 * elementów interfejsu dla pokazywania t-inwariantów. 
	 * @param p_invariants ArrayList[ArrayList[InvariantTransition]] - p-inwarianty
	 */
	public void showP_invBoxWindow(ArrayList<ArrayList<Integer>> p_invariants) {
		if (type == DockWindowType.P_INVARIANTS) {
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.P_INVARIANTS, p_invariants));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}
	
	/**
	 * Metoda wywoływana w momencie, kiedy z okna klastrów wpłyną dane o kolorach
	 * tranzycji w każdym klastrze. Wtedy tworzy całą resztę elementów podokna klastrów.
	 * @param coloredClustering ArrayList[ArrayList[Color]] - macierz kolorów
	 */
	public void showClusterSelector(ClusterDataPackage data) {
		if (type == DockWindowType.ClusterSELECTOR) {
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.CLUSTERS, data));
			scrollPane.getViewport().add(getCurrentDockWindow());			
		}
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCT sieci.
	 * @param mctGroups ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
	 */
	public void showMCT(ArrayList<ArrayList<Transition>> mctGroups) {
		if (type == DockWindowType.MctANALYZER) {
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCT, mctGroups));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda odpowiedzialna za pokazanie podokna ze zbiorami MCS sieci.
	 */
	public void showMCS() {
		if (type == DockWindowType.MCSselector) {
			MCSDataMatrix mcsData = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCS, mcsData));
			scrollPane.getViewport().add(getCurrentDockWindow());

		}
	}
	
	/**
	 * Metoda odpowiedzialna za pokazanie podokna ze zbiorami Knockout sieci.
	 */
	public void showKnockout(ArrayList<ArrayList<Integer>> knockoutData) {
		if (type == DockWindowType.Knockout) {
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.KNOCKOUT, knockoutData));
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
				if (n.getType() == PetriNetElementType.PLACE) {
					setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.PLACE, (Place) n, e.getElementLocation()));
				} else if (n.getType() == PetriNetElementType.META) {
					setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.META, (MetaNode) n, e.getElementLocation()));
				} else {
					if(n.getType().equals(PetriNetElementType.TRANSITION)) {
						if(((Transition)n).getTransType() == TransitionType.PN) {
							setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.TRANSITION, (Transition) n, e.getElementLocation()));
						} else if(((Transition)n).getTransType() == TransitionType.TPN) {
							setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.TIMETRANSITION, (Transition) n,e.getElementLocation()));
						}
					}
				}
				scrollPane.getViewport().add(getCurrentDockWindow());
			} else if (e.getArcGroup().size() > 0) {
				setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.ARC, (Arc) e.getArc()));
				scrollPane.getViewport().add(getCurrentDockWindow());
			}
		} else if (e.getActionType() == SelectionActionType.SELECTED_SHEET) {
			setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.SHEET,
				guiManager.getWorkspace().getSheets().get(guiManager.getWorkspace().getIndexOfId(e.getSheetId()))));
			scrollPane.getViewport().add(getCurrentDockWindow());
		}
	}

	/**
	 * Metoda zwracająca odpowiedni obiekt właściwości, czyli obiekt zawierający komponenty
	 * któregoś z podokien programu wyświetlające przyciski, napisy, itd.
	 * @return HolmesDockWindowsTable - obiekt podokna z ramach okna właściwości
	 */
	public HolmesDockWindowsTable getCurrentDockWindow() {
		return dockWindowPanel;
	}

	/**
	 * Metoda ustawiająca odpowiedni obiekt podokna, czyli obiekt zawierający komponenty
	 * któregoś z podokien programu wyświetlającego np. przyciski symulatora czy informacje
	 * o elementach sieci.
	 * @return HolmesDockWindowsTable - obiekt podokna z ramach okna właściwości
	 */
	public void setCurrentDockWindow(HolmesDockWindowsTable properties) {
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
	 * Metoda ustawiająca nowy obiekt panelu wyświetlającego zaznaczone elementy sieci.
	 * @return SelectionPanel - obiekt panelu
	 */
	private void setSelectionPanel(SelectionPanel selectionPanel) {
		this.selectionPanel = selectionPanel;
	}
}
