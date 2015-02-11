package abyss.darkgui;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import abyss.clusters.ClusterDataPackage;
import abyss.darkgui.dockwindows.AbyssDockWindowsTable;
import abyss.darkgui.dockwindows.AbyssDockWindow.DockWindowType;
import abyss.math.InvariantTransition;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.SimulatorMode;

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
			
			mastah.getWorkspace().getProject().set2ndFormInvariantsList(new ArrayList<ArrayList<InvariantTransition>>());
			mastah.getWorkspace().getProject().setInvariantsMatrix(null);		
			
			mastah.getInvariantsBox().getCurrentDockWindow().resetInvariants();
			mastah.getInvariantsBox().getCurrentDockWindow().removeAll();
			mastah.getInvariantsBox().setCurrentDockWindow(new AbyssDockWindowsTable(
					mastah.getWorkspace().getProject().get2ndFormInvariantsList()));	
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
}
