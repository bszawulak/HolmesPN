package holmes.darkgui;

import java.util.ArrayList;

import javax.swing.*;

import com.javadocking.dock.CompositeDock;
import com.javadocking.dockable.Dockable;

import holmes.analyse.MCTCalculator;
import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable.SubWindow;
import holmes.darkgui.toolbar.GUIController;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.petrinet.simulators.GraphicalSimulatorXTPN;
import holmes.petrinet.simulators.SimulatorGlobals;
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
	private boolean subNetGenerated = false;

	/**
	 * Metoda przywraca szaro-burą domyslną kolorystykę wyświetlanej sieci - czyści kolory wprowadzone
	 * w wyniku zaznaczania np. inwariantów, mct, etc.
	 */
	public void clearGraphColors() {
		overlord.getWorkspace().getProject().resetNetColors();
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
		overlord.simSettings.quickSimToken = false;
	}
	
	/**
	 * Metoda odpowiedzialna za czyszczenie danych i przywracanie programu do stanu początkowego.
	 */
	public boolean newProjectInitiated() {
		if(isSimulatorActiveWarning("Please stop simulation completely before continuing.", "Warning")) {
			return false;
		}
		if(isXTPNSimulatorActiveWarning("Please stop XTPN simulation completely before continuing.", "Warning")) {
			return false;
		}


		boolean hasSomethingChanged = overlord.getNetChangeStatus();
		if(hasSomethingChanged) {
			Object[] options = {"Continue", "Save and continue", "Cancel",};
			int n = JOptionPane.showOptionDialog(null,
							"Net has been changed since the last save. Continue and clear all data?",
							"Data clear warning", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
			if (n == 2) { //save the file
				return false;
			} else if (n == 1) {
				boolean savingStatus = overlord.io.saveAsGlobal();
				if(!savingStatus)
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
	 */
	public void emergencyRestart() {
		overlord.log("Something went terribly wrong. Holmes emergency restart initiated.", "error", true);
		clearAll();
	}
	
	/**
	 * Wewnętrzna metoda czyszcząca dane programu. Aż nie będzie niczego, Duch będzie unosić
	 * się nad wodami a Kukiz zostanie premierem.
	 */
	private void clearAll() {
		PetriNet pNet = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		overlord.log("Net data deletion initiated.", "text", true);
		
		//CLEAR PETRI NET DATA, kolejność MA ZNACZENIE JAK CHOLERA!!! Nie zmieniać bo coś j... się zepsuje.
		pNet.resetData(); // tylko w ten sposób!!!!
		pNet.setProjectType(PetriNet.GlobalNetType.PN);
		pNet.setT_InvMatrix(null, false);
		pNet.setP_InvMatrix(null);
		pNet.setMCTMatrix(null, false);
		pNet.accessMCTnames().clear();
		pNet.accessStatesManager().resetPN(true);
		pNet.accessStatesManager().resetXTPN(true);
		pNet.accessSSAmanager().reset(false);
		pNet.accessFiringRatesManager().reset(false);
		pNet.setMCSdataCore(new MCSDataMatrix());
		overlord.simSettings.reset();
		pNet.clearSimKnockoutData();
		pNet.resetComm();
		pNet.setMCTanalyzer(new MCTCalculator(pNet));
		pNet.setSimulator(new GraphicalSimulator(SimulatorGlobals.SimNetType.BASIC, pNet));
		pNet.setSimulatorXTPN(new GraphicalSimulatorXTPN(SimulatorGlobals.SimNetType.XTPN, pNet));
		pNet.setSimulationActive(false);
		pNet.setFileName("");

		overlord.simSettings.currentStep = 0;
		overlord.simSettings.currentTime = 0;
		overlord.accessStateSimulatorWindow().resetSimWindow();
		overlord.accessClusterWindow().resetWindow();
		overlord.getSimulatorBox().createSimulatorProperties(false);
		overlord.resetModuls();
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
		
		GUIManager.getDefaultGUIManager().getFrame().setTitle(
				"Holmes "+GUIManager.getDefaultGUIManager().getSettingsManager().getValue("holmes_version"));
		
		overlord.cleanDockables();
		overlord.markNetSaved();
	}
	
	/**
	 * Kasowanie informacji o: inwariantanch, MCT, klastrach, przede wszystkim w kontekście
	 * okien programu. Przy okazji reset protokołu I/O.
	 * @param clearWindows boolean - jeśli true, wtedy nakazuje czystkę dużych okien programu, np. klastry
	 */
	public void reset2ndOrderData(boolean clearWindows) {
		//"I nie będzie niczego."
		// Księga Kononowicza
		PetriNet pNet = overlord.getWorkspace().getProject();
		//clearGraphColors();
		
		if(clearWindows) {
			overlord.simSettings.currentStep = 0;
			overlord.simSettings.currentTime = 0;
			overlord.accessStateSimulatorWindow().resetSimWindow();
			overlord.accessClusterWindow().resetWindow();
		}
	
		if(t_invGenerated) {
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
		
		if(p_invGenerated) {
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
		
		if(mctGenerated) {
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
		
		if(clustersGenerated) {
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

		if(subNetGenerated){
			/*
			if(overlord.getDecompositionBox().getCurrentDockWindow() != null) {
				DefaultComboBoxModel model = new DefaultComboBoxModel();
				((JComboBox)overlord.getDecompositionBox().getCurrentDockWindow().getPanel(). getComponent(3)).setModel(model);
			}
			overlord.getDecompositionBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.DECOMPOSITION, null));
			overlord.getDecompositionBox().validate();
			overlord.getDecompositionBox().repaint();
*/

			subNetGenerated = false;
			overlord.log("Decomposition data removed from memory.", "text", true);
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

	@SuppressWarnings("unused")
	public void setDecompositionStatus(boolean status) {
		subNetGenerated = status;
	}
	
	/**
	 * Metoda zwraca wartość true jeśli symulator działa.
	 * @return boolean - true, jeśli symulator jest włączony, false w przeciwnym wypadku
	 */
	@SuppressWarnings("unused")
	public boolean isSimulatorActive() {
		GraphicalSimulator ns = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator();
		return ns.getSimulatorStatus() != SimulatorMode.STOPPED; // STOPPED => return false (czyli NOT active);
	}
	
	/**
	 * Metoda zwraca wartość true jeśli symulator działa. Dodatkowo wyświetla okno z ostrzeżeniem
	 * @return (<b>boolean</b>) - true, jeśli symulator jest włączony, false w przeciwnym wypadku
	 */
	public boolean isSimulatorActiveWarning(String msg, String msgTitle) {
		GraphicalSimulator obj = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator();
		if(obj instanceof GraphicalSimulator) {
			if(obj.getSimulatorStatus() == SimulatorMode.STOPPED) {
				return false;
			} else {
				JOptionPane.showMessageDialog(null, msg, msgTitle, JOptionPane.WARNING_MESSAGE);
				return true;
			}
		}
		return false;
	}

	public boolean isXTPNSimulatorActiveWarning(String msg, String msgTitle) {
		GraphicalSimulatorXTPN obj = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulatorXTPN();
		if(obj instanceof GraphicalSimulatorXTPN) {
			if(obj.getXTPNsimulatorStatus() == GraphicalSimulatorXTPN.SimulatorModeXTPN.STOPPED) {
				return false;
			} else {
				JOptionPane.showMessageDialog(null, msg, msgTitle, JOptionPane.WARNING_MESSAGE);
				return true;
			}
		}
		return false;
	}
}
