package abyss.darkgui;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.javadocking.dockable.Dockable;

import abyss.analyse.MCTCalculator;
import abyss.clusters.ClusterDataPackage;
import abyss.darkgui.dockwindows.AbyssDockWindowsTable;
import abyss.darkgui.dockwindows.AbyssDockWindow.DockWindowType;
import abyss.graphpanel.GraphPanel;
import abyss.math.Arc;
import abyss.math.MCSDataMatrix;
import abyss.math.Node;
import abyss.math.PetriNet;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.workspace.Workspace;
import abyss.workspace.WorkspaceSheet;

/**
 * Klasa odpowiedzialna za różne rzeczy związane z czyszczeniem wszystkiego i niczego w ramach
 * programu.
 * @author MR
 *
 */
public class GUIReset {
	private GUIManager mastah = GUIManager.getDefaultGUIManager();
	private boolean invGenerated = false;
	private boolean mctGenerated = false;
	private boolean clustersGenerated = false;

	/**
	 * Metoda przywraca szaro-burą domyslną kolorystykę wyświetlanej sieci - czyści kolory wprowadzone
	 * w wyniku zaznaczania np. inwariantów, mct, etc.
	 */
	public void clearGraphColors() {
		mastah.getWorkspace().getProject().turnTransitionGlowingOff();
		mastah.getWorkspace().getProject().setTransitionGlowedMTC(false);
		mastah.getWorkspace().getProject().setColorClusterToNeutral();
		mastah.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	public void newProjectInitiated() {
		PetriNet pNet = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		
		GUIManager.getDefaultGUIManager().log("Net data deletion initiated.", "text", true);

		for (GraphPanel gp : pNet.getGraphPanels()) {
			gp.getSelectionManager().forceDeselectAllElements();
		}
		
		//CLEAR PETRI NET DATA:
		pNet.setNodes(new ArrayList<Node>());
		pNet.setArcs(new ArrayList<Arc>());
		pNet.setInvariantsMatrix(null);
		pNet.setMCSdataCore(new MCSDataMatrix());
		pNet.resetComm();
		pNet.setAnalyzer(new MCTCalculator(pNet));
		pNet.setSimulator(new NetSimulator(NetType.BASIC, pNet));
		pNet.setSimulationActive(false);
		
		pNet.repaintAllGraphPanels();
		
		//podmiana SheetPanels na nowe, czyste wersje
		/*
		ArrayList<GraphPanel> newGraphPanels = new ArrayList<GraphPanel>();
		for (GraphPanel gp : pNet.getGraphPanels()) {
			int sheetID = gp.getSheetId();
			WorkspaceSheet.SheetPanel sheetPanel = (WorkspaceSheet.SheetPanel) gp.getParent();
			sheetPanel.remove(gp);
			GraphPanel newGraphPanel = new GraphPanel(sheetID, pNet, pNet.getNodes(), pNet.getArcs());
			sheetPanel.add(newGraphPanel);
			newGraphPanels.add(newGraphPanel);
		}
		pNet.setGraphPanels(newGraphPanels);
		*/
		
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		
		int dockableSize = workspace.getDockables().size();
		
		for(int d=0; d<dockableSize; d++) {
			Dockable dockable = workspace.getDockables().get(d);
			String x = dockable.getID();
			if(x.equals("Sheet 0")) {
				continue;
			}
			
			workspace.deleteTab(dockable, true);
			d--;
			dockableSize--;
		}
		
		//pNet.repaintAllGraphPanels();
		
	}
	
	/**
	 * Kasowanie informacji o: inwariantanch, MCT, klastrach, przede wszystkich w kontekście
	 * podokien programu. Przy okazji reset protokołu I/O.
	 */
	public void reset2ndOrderData() {
		clearGraphColors();
		
		//"I nie będzie niczego."
		// Księga Kononowicza
		if(invGenerated == true) {
			resetCommunicationProtocol();
			mastah.getWorkspace().getProject().setInvariantsMatrix(null);
			mastah.getWorkspace().getProject().getMCSdataCore().resetMSC();
			
			mastah.getInvariantsBox().getCurrentDockWindow().resetInvariants();
			mastah.getInvariantsBox().getCurrentDockWindow().removeAll();
			mastah.getInvariantsBox().setCurrentDockWindow(new AbyssDockWindowsTable(
					mastah.getWorkspace().getProject().getInvariantsMatrix()));	
			mastah.getInvariantsBox().validate();
			mastah.getInvariantsBox().repaint();

			invGenerated = false;
			GUIManager.getDefaultGUIManager().log("Invariants data removed from memory.", "text", true);
		}
		
		if(mctGenerated == true) {
			for (Transition transition : mastah.getWorkspace().getProject().getTransitions()) {
				transition.setContainingInvariants(new ArrayList<ArrayList<Transition>>()); //czyszczenie
			}
			
			mastah.getMctBox().getCurrentDockWindow().removeAll();
			mastah.getMctBox().getCurrentDockWindow().resetMCT();
			mastah.getMctBox().setCurrentDockWindow(new AbyssDockWindowsTable(
					new ArrayList<ArrayList<Transition>>(), DockWindowType.MctANALYZER));
			mastah.getMctBox().validate();
			mastah.getMctBox().repaint();
			
			mctGenerated = false;
			GUIManager.getDefaultGUIManager().log("MCT data removed from memory.", "text", true);
		}
		
		if(clustersGenerated == true) {
			mastah.getClusterSelectionBox().getCurrentDockWindow().removeAll();
			mastah.getClusterSelectionBox().getCurrentDockWindow().resetClusters();
			mastah.getClusterSelectionBox().setCurrentDockWindow(new AbyssDockWindowsTable(new ClusterDataPackage(), false));
			mastah.getClusterSelectionBox().validate();
			mastah.getClusterSelectionBox().repaint();
			
			clustersGenerated = false;
			GUIManager.getDefaultGUIManager().log("Clustering data removed from memory.", "text", true);
		}
	}
	
	/**
	 * Podmienianie protokołu I/O na nowy obiekt.
	 */
	public void resetCommunicationProtocol() {
		mastah.getWorkspace().getProject().resetComm();
	}
	
	
	//*****************************************************************************************************
	//*****************************************************************************************************
	//*****************************************************************************************************
	
	/**
	 * Metoda ta ustawia status inwariantów w programie.
	 * @param status boolean - true, jeśli są dostepne
	 */
	public void setInvariantsStatus(boolean status) {
		invGenerated = status;
	}
	
	/**
	 * Metoda ta ustawia status zbiorów MCT w programie.
	 * @param status boolean - true, jeśli są dostepne
	 */
	public void setMCTStatus(boolean status) {
		mctGenerated = status;
	}
	
	/**
	 * Metoda ta ustawia status klastrów w programie.
	 * @param status boolean - true, jeśli są dostepne
	 */
	public void setClustersStatus(boolean status) {
		clustersGenerated = status;
	}
	
	/**
	 * Metoda zwraca wartość true jeśli symulator działa.
	 * @return boolean - true, jeśli symulator jest włączony, false w przeciwnym wypadku
	 */
	public boolean isSimulatorActive() {
		NetSimulator ns = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator();
		if(ns.getSimulatorStatus() == SimulatorMode.STOPPED) {
			return false;
		} else {
			return true;
		}
	}
	
	/**
	 * Metoda zwraca wartość true jeśli symulator działa. Dodatkowo wyświetla okno z ostrzeżeniem
	 * @return boolean - true, jeśli symulator jest włączony, false w przeciwnym wypadku
	 */
	public boolean isSimulatorActiveWarning(String msg, String msgTitle) {
		NetSimulator ns = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator();
		if(ns.getSimulatorStatus() == SimulatorMode.STOPPED) {
			return false;
		} else {
			JOptionPane.showMessageDialog(null, msg, msgTitle, JOptionPane.WARNING_MESSAGE);
			return true;
		}
	}
}
