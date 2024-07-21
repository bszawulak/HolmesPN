package holmes.darkgui;

import javax.swing.*;

import holmes.analyse.MCTCalculator;
import holmes.clusters.ClusterDataPackage;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable.SubWindow;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.petrinet.simulators.xtpn.GraphicalSimulatorXTPN;
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
	private static LanguageManager lang = GUIManager.getLanguageManager();
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
	 * @return boolean - true, jeśli nowy projekt został zainicjowany, false w przeciwnym wypadku/
	 */
	public boolean newProjectInitiated() {
		if(isSimulatorActiveWarning(lang.getText("GUIR_reset001"), "Warning")) {
			return false;
		}
		if(isXTPNSimulatorActiveWarning(lang.getText("GUIR_reset002"), "Warning")) {
			return false;
		}

		boolean hasSomethingChanged = overlord.getNetChangeStatus();
		if(hasSomethingChanged) {
			Object[] options = {lang.getText("continue"), lang.getText("saveAndCont"), lang.getText("cancel"),};
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("GUIR_reset003"),
							lang.getText("GUIR_reset004"), JOptionPane.YES_NO_OPTION,
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
		overlord.log(lang.getText("LOGentry00034"), "text", true);
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
		overlord.log(lang.getText("LOGentry00035"), "error", true);
		clearAll();
	}
	
	/**
	 * Wewnętrzna metoda czyszcząca dane programu. Aż nie będzie niczego, Duch będzie unosić
	 * się nad wodami a Kukiz zostanie premierem.
	 */
	private void clearAll() {
		PetriNet pNet = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		overlord.log(lang.getText("GUIR_reset005"), "text", true);
		
		//CLEAR PETRI NET DATA, kolejność MA ZNACZENIE JAK CHOLERA!!! Nie zmieniać bo coś j...tj. "się" zepsuje.
		pNet.resetData(); // tylko w ten sposób!!!!
		pNet.setProjectType(PetriNet.GlobalNetType.PN);
		pNet.setT_InvMatrix(null, false);
		pNet.setP_InvMatrix(null);
		pNet.setMCTMatrix(null, false);
		pNet.accessMCTnames().clear();
		pNet.accessStatesManager().resetPN(true);
		pNet.accessStatesManager().removeAllMultisets_M(true);
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
		overlord.accessStateSimulatorXTPNWindow().resetSimWindow();
		overlord.accessClusterWindow().resetWindow();
		overlord.getSimulatorBox().createSimulatorProperties(false);
		overlord.resetModuls();
		pNet.repaintAllGraphPanels();
		
		Workspace workspace = overlord.getWorkspace();
		workspace.deleteAllSheetButFirst();
		
		reset2ndOrderData(false);
		IdGenerator.resetIDgenerator();
		
		GUIManager.getDefaultGUIManager().getFrame().setTitle(
				"Holmes "+GUIManager.getDefaultGUIManager().getSettingsManager().getValue("holmes_version"));

		overlord.markNetSaved();
	}
	
	/**
	 * Kasowanie informacji o: inwariantanch, MCT, klastrach, przede wszystkim w kontekście
	 * okien programu. Przy okazji reset protokołu I/O.
	 * @param clearWindows boolean - jeśli true, wtedy nakazuje czystkę dużych okien programu, np. klastry
	 */
	public void reset2ndOrderData(boolean clearWindows) {
		//"I nie będzie niczego." Księga Kononowicza
		PetriNet pNet = overlord.getWorkspace().getProject();
		//clearGraphColors();
		
		if(clearWindows) {
			overlord.simSettings.currentStep = 0;
			overlord.simSettings.currentTime = 0;
			overlord.accessStateSimulatorWindow().resetSimWindow();
			overlord.accessStateSimulatorXTPNWindow().resetSimWindow();
			overlord.accessClusterWindow().resetWindow();
		}
	
		if(t_invGenerated) {
			overlord.accessNetTablesWindow().resetT_invData();
			resetCommunicationProtocol();
			pNet.setT_InvMatrix(null, false);
			pNet.getMCSdataCore().resetMSC();

			if(overlord.getT_invBox().getCurrentDockWindow() != null) {
				overlord.getT_invBox().getCurrentDockWindow().cleanTINVsubwindowFields();
			}

			t_invGenerated = false;
			overlord.log(lang.getText("LOGentry00036"), "text", true);
		}
		
		if(mctGenerated) {
			pNet.setMCTMatrix(null, false);
			pNet.accessMCTnames().clear();

			if(overlord.getMctBox().getCurrentDockWindow() != null) {
				overlord.getMctBox().getCurrentDockWindow().cleanMCtsubwindowFields();
			}

			mctGenerated = false;
			overlord.log(lang.getText("LOGentry00037"), "text", true);
		}

		if(p_invGenerated) {
			//resetCommunicationProtocol();
			pNet.setP_InvMatrix(null);

			if(overlord.getP_invBox().getCurrentDockWindow() != null) {
				overlord.getP_invBox().getCurrentDockWindow().cleanPInvSubwindowData();
			}

			p_invGenerated = false;
			overlord.log(lang.getText("LOGentry00038"), "text", true);
		}
		
		if(clustersGenerated) {
			if(overlord.getClusterSelectionBox().getCurrentDockWindow() != null) {
				overlord.getClusterSelectionBox().getCurrentDockWindow().removeAll();
				overlord.getClusterSelectionBox().getCurrentDockWindow().cleanClustersSubwindowData();
			}
			overlord.getClusterSelectionBox().setCurrentDockWindow(new HolmesDockWindowsTable(SubWindow.CLUSTERS, new ClusterDataPackage()));
			
			clustersGenerated = false;
			overlord.log(lang.getText("LOGentry00039"), "text", true);
		}

		if(subNetGenerated){
			subNetGenerated = false;
			overlord.log(lang.getText("LOGentry00040"), "text", true);
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
		GraphicalSimulator obj = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator();// GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator();
		if(obj != null) {
			if(obj.getSimulatorStatus() == SimulatorMode.STOPPED) {
				return false;
			} else {
				JOptionPane.showMessageDialog(null, msg, msgTitle, JOptionPane.WARNING_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/**
	 * Metoda zwraca wartość true jeśli symulator XTPN działa. Dodatkowo wyświetla okno z ostrzeżeniem.
	 * @param msg String, komunikat
	 * @param msgTitle String, tytuł okna
	 * @return boolean - true, jeśli symulator jest włączony, false w przeciwnym wypadku
	 */
	public boolean isXTPNSimulatorActiveWarning(String msg, String msgTitle) {
		GraphicalSimulatorXTPN obj = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulatorXTPN();
		if(obj != null) {
			if(obj.getsimulatorStatusXTPN() == GraphicalSimulatorXTPN.SimulatorModeXTPN.STOPPED) {
				return false;
			} else {
				JOptionPane.showMessageDialog(null, msg, msgTitle, JOptionPane.WARNING_MESSAGE);
				return true;
			}
		}
		return false;
	}
}
