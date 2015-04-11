package abyss.darkgui;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.javadocking.dockable.Dockable;

import abyss.analyse.MCTCalculator;
import abyss.clusters.ClusterDataPackage;
import abyss.darkgui.dockwindows.AbyssDockWindowsTable;
import abyss.darkgui.dockwindows.AbyssDockWindow.DockWindowType;
import abyss.graphpanel.GraphPanel;
import abyss.graphpanel.IdGenerator;
import abyss.math.MCSDataMatrix;
import abyss.math.PetriNet;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.workspace.Workspace;

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
		mastah.getWorkspace().getProject().resetTransitionGraphics();
		mastah.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda odpowiedzialna za czyszczenie danych i przywracanie programu do stanu początkowego.
	 */
	public boolean newProjectInitiated() {
		boolean status = GUIManager.getDefaultGUIManager().getNetChangeStatus();
		if(status == true) {
			Object[] options = {"Continue", "Save and continue", "Cancel",};
			int n = JOptionPane.showOptionDialog(null,
							"Network has been changed since last save. Continue and clear all data?",
							"Data clear warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
			if (n == 2) { //save the file
				return false;
			} else if (n == 1) {
				boolean savingStatus = mastah.io.saveAsGlobal();
				if(savingStatus == false)
					return false;
			}
		}
		
		
		PetriNet pNet = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		GUIManager.getDefaultGUIManager().log("Net data deletion initiated.", "text", true);

		for (GraphPanel gp : pNet.getGraphPanels()) {
			gp.getSelectionManager().forceDeselectAllElements();
		}
		
		//CLEAR PETRI NET DATA:
		pNet.resetData(); // tylko w ten sposób!!!! 
		pNet.setInvariantsMatrix(null);
		pNet.setMCSdataCore(new MCSDataMatrix());
		pNet.resetComm();
		pNet.setAnalyzer(new MCTCalculator(pNet));
		pNet.setSimulator(new NetSimulator(NetType.BASIC, pNet));
		pNet.setSimulationActive(false);
		
		pNet.repaintAllGraphPanels();
		
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
		
		reset2ndOrderData();
		IdGenerator.resetIDgenerator();
		return true;
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
			mastah.accessNetTablesWindow().resetInvData();
			
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
			//for (Transition transition : mastah.getWorkspace().getProject().getTransitions()) {
			//	transition.setContainingInvariants(new ArrayList<ArrayList<Transition>>()); //czyszczenie
			//}
			
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
