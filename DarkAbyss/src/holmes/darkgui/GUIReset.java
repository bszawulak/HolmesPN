package holmes.darkgui;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.javadocking.dock.CompositeDock;
import com.javadocking.dockable.Dockable;

import holmes.analyse.MCTCalculator;
import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable.SubWindow;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.NetSimulator;
import holmes.petrinet.simulators.NetSimulator.NetType;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.workspace.Workspace;

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
		mastah.getWorkspace().getProject().resetNetColors();
		mastah.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda odpowiedzialna za czyszczenie danych i przywracanie programu do stanu początkowego.
	 */
	public boolean newProjectInitiated() {
		if(isSimulatorActiveWarning("Please stop simulation completely before contynuing.", "Warning") == true) {
			return false;
		}
		
		
		boolean status = mastah.getNetChangeStatus();
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
		
		
		PetriNet pNet = mastah.getWorkspace().getProject();
		mastah.log("Net data deletion initiated.", "text", true);

		for (GraphPanel gp : pNet.getGraphPanels()) {
			gp.getSelectionManager().forceDeselectAllElements();
		}
		
		clearAll();
		return true;
	}
	
	/**
	 * Używana w przypadku krytycznego błędu rysowania sieci.
	 * @return boolean - true zawsze
	 */
	public boolean emergencyRestart() {
		clearAll();
		return true;
	}
	
	/**
	 * Wewnętrzna metoda czyszcząca dane programu.
	 */
	private void clearAll() {
		PetriNet pNet = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		mastah.log("Net data deletion initiated.", "text", true);
		
		//CLEAR PETRI NET DATA:
		pNet.resetData(); // tylko w ten sposób!!!! 
		pNet.setINVmatrix(null, false);
		pNet.setMCTMatrix(null, false);
		pNet.accessMCTnames().clear();
		pNet.accessStatesManager().reset(false);
		pNet.setMCSdataCore(new MCSDataMatrix());
		pNet.clearSimKnockoutData();
		pNet.resetComm();
		pNet.setMCTanalyzer(new MCTCalculator(pNet));
		pNet.setSimulator(new NetSimulator(NetType.BASIC, pNet));
		pNet.setSimulationActive(false);
		pNet.setFileName("");
		mastah.simSettings.currentStep = 0;
		mastah.accessStateSimulatorWindow().resetSimWindow();
		mastah.accessClusterWindow().resetWindow();
		mastah.getSimulatorBox().createSimulatorProperties();
		pNet.repaintAllGraphPanels();

		
		Workspace workspace = mastah.getWorkspace();
		int dockableSize = workspace.getDockables().size();
		CompositeDock parentOfFirst = workspace.getDockables().get(0).getDock().getParentDock();
		for(int d=0; d<dockableSize; d++) {
			Dockable dockable = workspace.getDockables().get(d);
			String x = dockable.getID();
			if(x.equals("Sheet 0")) {
				continue;
			}
			workspace.deleteTab(dockable, true);
			d--;
			dockableSize--;
			
			if(dockable.getDock().getParentDock().equals(parentOfFirst))
				mastah.globalSheetsList.remove(dockable);
		}
		
		reset2ndOrderData(false);
		IdGenerator.resetIDgenerator();
		
		mastah.cleanDockables();
		mastah.markNetSaved();
	}
	
	
	/**
	 * Kasowanie informacji o: inwariantanch, MCT, klastrach, przede wszystkich w kontekście
	 * podokien programu. Przy okazji reset protokołu I/O.
	 * @param clearWindows boolean - jeśli true, wtedy nakazuje czystkę dużych podokien programu, np. klastry
	 */
	public void reset2ndOrderData(boolean clearWindows) {
		//"I nie będzie niczego."
		// Księga Kononowicza
		PetriNet pNet = mastah.getWorkspace().getProject();
		//clearGraphColors();
		
		if(clearWindows) {
			mastah.simSettings.currentStep = 0;
			mastah.accessStateSimulatorWindow().resetSimWindow();
			mastah.accessClusterWindow().resetWindow();
		}
	
		if(invGenerated == true) {
			mastah.accessNetTablesWindow().resetInvData();
			
			resetCommunicationProtocol();
			pNet.setINVmatrix(null, false);
			pNet.getMCSdataCore().resetMSC();
			
			if(mastah.getInvariantsBox().getCurrentDockWindow() != null) {
				mastah.getInvariantsBox().getCurrentDockWindow().resetInvariants();
				mastah.getInvariantsBox().getCurrentDockWindow().removeAll();
			}
			mastah.getInvariantsBox().setCurrentDockWindow(
					new HolmesDockWindowsTable(SubWindow.INVARIANTS, pNet.getINVmatrix()));	
			mastah.getInvariantsBox().validate();
			mastah.getInvariantsBox().repaint();

			invGenerated = false;
			mastah.log("Invariants data removed from memory.", "text", true);
		}
		
		if(mctGenerated == true) {
			//for (Transition transition : mastah.getWorkspace().getProject().getTransitions()) {
			//	transition.setContainingInvariants(new ArrayList<ArrayList<Transition>>()); //czyszczenie
			//}
			if(mastah.getMctBox().getCurrentDockWindow() != null) {
				mastah.getMctBox().getCurrentDockWindow().removeAll();
				mastah.getMctBox().getCurrentDockWindow().resetMCT();
			}
			mastah.getMctBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCT,
					new ArrayList<ArrayList<Transition>>()));
			mastah.getMctBox().validate();
			mastah.getMctBox().repaint();
			
			pNet.setMCTMatrix(null, false);
			pNet.accessMCTnames().clear();
			
			mctGenerated = false;
			mastah.log("MCT data removed from memory.", "text", true);
		}
		
		if(clustersGenerated == true) {
			if(mastah.getClusterSelectionBox().getCurrentDockWindow() != null) {
				mastah.getClusterSelectionBox().getCurrentDockWindow().removeAll();
				mastah.getClusterSelectionBox().getCurrentDockWindow().resetClusters();
			}
			mastah.getClusterSelectionBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.CLUSTERS, new ClusterDataPackage()));
			mastah.getClusterSelectionBox().validate();
			mastah.getClusterSelectionBox().repaint();
			
			clustersGenerated = false;
			mastah.log("Clustering data removed from memory.", "text", true);
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
		NetSimulator ns = mastah.getSimulatorBox().getCurrentDockWindow().getSimulator();
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
		NetSimulator ns = mastah.getSimulatorBox().getCurrentDockWindow().getSimulator();
		if(ns.getSimulatorStatus() == SimulatorMode.STOPPED) {
			return false;
		} else {
			JOptionPane.showMessageDialog(null, msg, msgTitle, JOptionPane.WARNING_MESSAGE);
			return true;
		}
	}
}
