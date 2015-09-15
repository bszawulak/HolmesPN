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
 * Klasa odpowiedzialna za różne rzeczy związane z czyszczeniem wszystkiego i wszystkich w ramach
 * programu.
 * 
 * @author MR
 */
public class GUIReset {
	private GUIManager overlord = GUIManager.getDefaultGUIManager();
	private boolean t_invGenerated = false;
	private boolean p_invGenerated = false;
	private boolean mctGenerated = false;
	private boolean clustersGenerated = false;

	/**
	 * Metoda przywraca szaro-burą domyslną kolorystykę wyświetlanej sieci - czyści kolory wprowadzone
	 * w wyniku zaznaczania np. inwariantów, mct, etc.
	 */
	public void clearGraphColors() {
		overlord.getWorkspace().getProject().resetNetColors();
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda odpowiedzialna za czyszczenie danych i przywracanie programu do stanu początkowego.
	 */
	public boolean newProjectInitiated() {
		if(isSimulatorActiveWarning("Please stop simulation completely before contynuing.", "Warning") == true) {
			return false;
		}

		boolean status = overlord.getNetChangeStatus();
		if(status == true) {
			Object[] options = {"Continue", "Save and continue", "Cancel",};
			int n = JOptionPane.showOptionDialog(null,
							"Network has been changed since last save. Continue and clear all data?",
							"Data clear warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
			if (n == 2) { //save the file
				return false;
			} else if (n == 1) {
				boolean savingStatus = overlord.io.saveAsGlobal();
				if(savingStatus == false)
					return false;
			}
		}

		PetriNet pNet = overlord.getWorkspace().getProject();
		overlord.log("Net data deletion initiated.", "text", true);

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
	 * Wewnętrzna metoda czyszcząca dane programu. Aż nie będzie niczego, Duch będzie unosić
	 * się nad wodami a Kukiz zostanie premierem.
	 */
	private void clearAll() {
		PetriNet pNet = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		overlord.log("Net data deletion initiated.", "text", true);
		
		//CLEAR PETRI NET DATA, kolejność MA ZNACZENIE JAK CHOLERA. Nie zmieniać!
		pNet.resetData(); // tylko w ten sposób!!!! 
		pNet.setT_InvMatrix(null, false);
		pNet.setP_InvMatrix(null);
		pNet.setMCTMatrix(null, false);
		pNet.accessMCTnames().clear();
		pNet.accessStatesManager().reset(false);
		pNet.accessFiringRatesManager().reset(false);
		pNet.setMCSdataCore(new MCSDataMatrix());
		overlord.simSettings.reset();
		pNet.clearSimKnockoutData();
		pNet.resetComm();
		pNet.setMCTanalyzer(new MCTCalculator(pNet));
		pNet.setSimulator(new NetSimulator(NetType.BASIC, pNet));
		pNet.setSimulationActive(false);
		pNet.setFileName("");
		overlord.simSettings.currentStep = 0;
		overlord.accessStateSimulatorWindow().resetSimWindow();
		overlord.accessClusterWindow().resetWindow();
		overlord.getSimulatorBox().createSimulatorProperties();
		pNet.repaintAllGraphPanels();
		
		Workspace workspace = overlord.getWorkspace();
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
				overlord.globalSheetsList.remove(dockable);
		}
		
		reset2ndOrderData(false);
		IdGenerator.resetIDgenerator();
		
		overlord.cleanDockables();
		overlord.markNetSaved();
	}
	
	/**
	 * Kasowanie informacji o: inwariantanch, MCT, klastrach, przede wszystkich w kontekście
	 * podokien programu. Przy okazji reset protokołu I/O.
	 * @param clearWindows boolean - jeśli true, wtedy nakazuje czystkę dużych podokien programu, np. klastry
	 */
	public void reset2ndOrderData(boolean clearWindows) {
		//"I nie będzie niczego."
		// Księga Kononowicza
		PetriNet pNet = overlord.getWorkspace().getProject();
		//clearGraphColors();
		
		if(clearWindows) {
			overlord.simSettings.currentStep = 0;
			overlord.accessStateSimulatorWindow().resetSimWindow();
			overlord.accessClusterWindow().resetWindow();
		}
	
		if(t_invGenerated == true) {
			overlord.accessNetTablesWindow().resetT_invData();
			
			resetCommunicationProtocol();
			pNet.setT_InvMatrix(null, false);
			pNet.getMCSdataCore().resetMSC();
			
			if(overlord.getT_invBox().getCurrentDockWindow() != null) {
				overlord.getT_invBox().getCurrentDockWindow().resetT_invariants();
				overlord.getT_invBox().getCurrentDockWindow().removeAll();
			}
			overlord.getT_invBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.T_INVARIANTS, pNet.getT_InvMatrix()));	
			overlord.getT_invBox().validate();
			overlord.getT_invBox().repaint();

			t_invGenerated = false;
			overlord.log("T-invariants data removed from memory.", "text", true);
		}
		
		if(p_invGenerated == true) {
			resetCommunicationProtocol();
			pNet.setP_InvMatrix(null);
			
			if(overlord.getP_invBox().getCurrentDockWindow() != null) {
				overlord.getP_invBox().getCurrentDockWindow().resetP_invariants();
				overlord.getP_invBox().getCurrentDockWindow().removeAll();
			}
			overlord.getP_invBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.P_INVARIANTS, pNet.getP_InvMatrix()));	
			overlord.getP_invBox().validate();
			overlord.getP_invBox().repaint();
			
			p_invGenerated = false;
			overlord.log("P-invariants data removed from memory.", "text", true);
		}
		
		if(mctGenerated == true) {
			if(overlord.getMctBox().getCurrentDockWindow() != null) {
				overlord.getMctBox().getCurrentDockWindow().removeAll();
				overlord.getMctBox().getCurrentDockWindow().resetMCT();
			}
			overlord.getMctBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.MCT,
					new ArrayList<ArrayList<Transition>>()));
			overlord.getMctBox().validate();
			overlord.getMctBox().repaint();
			
			pNet.setMCTMatrix(null, false);
			pNet.accessMCTnames().clear();
			
			mctGenerated = false;
			overlord.log("MCT data removed from memory.", "text", true);
		}
		
		if(clustersGenerated == true) {
			if(overlord.getClusterSelectionBox().getCurrentDockWindow() != null) {
				overlord.getClusterSelectionBox().getCurrentDockWindow().removeAll();
				overlord.getClusterSelectionBox().getCurrentDockWindow().resetClusters();
			}
			overlord.getClusterSelectionBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.CLUSTERS, new ClusterDataPackage()));
			overlord.getClusterSelectionBox().validate();
			overlord.getClusterSelectionBox().repaint();
			
			clustersGenerated = false;
			overlord.log("Clustering data removed from memory.", "text", true);
		}
	}
	
	/**
	 * Podmienianie protokołu I/O na nowy obiekt.
	 */
	public void resetCommunicationProtocol() {
		overlord.getWorkspace().getProject().resetComm();
	}
	
	
	//*****************************************************************************************************
	//*****************************************************************************************************
	//*****************************************************************************************************
	
	/**
	 * Metoda ta ustawia status t-inwariantów w programie.
	 * @param status boolean - true, jeśli są dostepne
	 */
	public void setT_invariantsStatus(boolean status) {
		t_invGenerated = status;
	}
	
	/**
	 * Metoda ta ustawia status p-inwariantów w programie.
	 * @param status boolean - true, jeśli są dostepne
	 */
	public void setP_invariantsStatus(boolean status) {
		p_invGenerated = status;
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
		NetSimulator ns = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator();
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
		NetSimulator ns = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator();
		if(ns.getSimulatorStatus() == SimulatorMode.STOPPED) {
			return false;
		} else {
			JOptionPane.showMessageDialog(null, msg, msgTitle, JOptionPane.WARNING_MESSAGE);
			return true;
		}
	}
}
