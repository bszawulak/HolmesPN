package holmes.petrinet.simulators;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import holmes.darkgui.GUIManager;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;

public class QuickSimTools {
	private GUIManager overlord;
	private HolmesDockWindowsTable subwindow;
	private StateSimulator quickSim;
	
	private boolean scanTransitions = true;
	private boolean scanPlaces = true;
	private boolean markArcs = true;
	
	public QuickSimTools(HolmesDockWindowsTable holmesDockWindowsTable) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.subwindow = holmesDockWindowsTable;
	}


	/**
	 * Zbiera dane symulatorem i wyświetla na sieci.
	 * @param scanTransitions
	 * @param scanPlaces
	 * @param markArcs
	 * @param quickProgressBar 
	 */
	public void acquireData(boolean scanTransitions, boolean scanPlaces, boolean markArcs, boolean repetitions, JProgressBar quickProgressBar) {
		if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null, "Net simulator working. Unable to retrieve transitions statistics..", 
					"Simulator working", JOptionPane.ERROR_MESSAGE);
		} else {
			this.scanTransitions = scanTransitions;
			this.scanPlaces = scanPlaces;
			this.markArcs = markArcs;
			
			if(repetitions) {
				statsData(quickProgressBar);
			} else {
				vectorData(quickProgressBar);
			}
		}
	}

	private void statsData(JProgressBar quickProgressBar) {
		quickSim = new StateSimulator();
		quickSim.initiateSim(true, null);
		quickSim.setThreadDetails(5, quickProgressBar, this);
		Thread myThread = new Thread(quickSim);
		myThread.start();
	}
	
	public void finishedStatsData(ArrayList<ArrayList<Double>> quickSimAllStats, ArrayList<Transition> transitions,
			ArrayList<Place> places) {
		ArrayList<Double> avgFire = quickSimAllStats.get(0);
		ArrayList<Double> stdDev = quickSimAllStats.get(1);
		ArrayList<Double> avgTokens = quickSimAllStats.get(2);
		
		overlord.simSettings.quickSimToken = true;
		int transSize = transitions.size();
		int placesSize = places.size();
		
		double max = 0;
		for(int t=0; t<transSize; t++) {
			if(avgFire.get(t) > max)
				max = avgFire.get(t);
		}
		if(overlord.simSettings.isMaxMode())
			max = 1;
		
		
		for(int t=0; t<transSize; t++) {
			Transition trans = transitions.get(t);
			double firing = avgFire.get(t);
			trans.qSimFired = firing;
			
			double reliance = firing/max;
			int fill = (int)((double)30 * reliance);
			trans.qSimFillValue = fill;
			
			if(firing < 0.05) {
				trans.qSimColor = Color.RED;
				trans.qSimFillValue = 5;
			} else if(firing < 0.15) {
				trans.qSimColor = Color.ORANGE;
			} else { 
				trans.qSimColor = Color.GREEN;
			}
			
			if(scanTransitions) {
				trans.qSimDrawed = true;
				if(firing == 0)
					trans.qSimFailed = true;
				else
					trans.qSimFailed = false;
			} else {
				trans.qSimDrawed = false;
				trans.qSimFailed = false;
			}
		}
		
		double maxT = 0;
		for(int p=0; p<placesSize; p++) {
			if(avgTokens.get(p) > maxT)
				maxT = avgTokens.get(p);
		}

		
		for(int p=0; p<placesSize; p++) {
			Place place = places.get(p);
			double avgT = avgTokens.get(p);
			
			place.qSimTokens = avgT;
			if(avgT == 0) {
				place.qSimColor = Color.BLACK;
			} else if(avgT < 1){
				place.qSimColor = Color.RED;
				place.qSimFillValue = 4;
			} else if(avgT < 5){
				place.qSimColor = Color.ORANGE;
				place.qSimFillValue = 8;
			} else {
				double reliance = avgT/maxT;
				int fill = (int)((double)30 * reliance);
				if(fill<8)
					fill = 8;
				place.qSimFillValue = fill;
				place.qSimColor = Color.GREEN;
			}
			
			
			if(scanPlaces) {
				place.qSimDrawed = true;
				
				if(avgT == 0)
					place.qSimFailed = true;
				else
					place.qSimFailed = false;
			} else {
				place.qSimDrawed = false;
				place.qSimFailed = false;
			}
		}
		
		
		ArrayList<Arc> arcs = overlord.getWorkspace().getProject().getArcs();
		for(Arc arc : arcs) {
			
			if(arc.getStartNode().qSimFailed && arc.getEndNode().qSimFailed)
				arc.qSimRed = true;
			else
				arc.qSimRed = false;
			
			if(!markArcs) {
				arc.qSimRed = false;
			}
		}

		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	private void vectorData(JProgressBar quickProgressBar) {
		quickSim = new StateSimulator();
		quickSim.initiateSim(true, null);
		quickSim.setThreadDetails(6, quickProgressBar, this);
		Thread myThread = new Thread(quickSim);
		myThread.start();

	}
}
