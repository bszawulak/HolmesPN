package holmes.petrinet.simulators;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import holmes.darkgui.GUIController;
import holmes.darkgui.GUIManager;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.petrinet.simulators.xtpn.StateSimulatorXTPN;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;

/**
 * Nakładka na symulator stanów odpowiedzialna za wyświetlanie informacji statystycznych na obrazie sieci.
 */
public class QuickSimTools {
	private GUIManager overlord;
	private StateSimulator stateSimulatorPN;
	private StateSimulatorXTPN stateSimulatorXTPN;
	private boolean scanTransitions = true;
	private boolean scanPlaces = true;
	private boolean markArcs = true;
	
	/**
	 * Konstruktor obiektu klasy QuickSimTools.
	 * @param holmesDockWindowsTable HolmesDockWindowsTable - panel zarządzający sekcji 6 okna głównego
	 */
	public QuickSimTools(HolmesDockWindowsTable holmesDockWindowsTable) {
		this.overlord = GUIManager.getDefaultGUIManager();
	}

	/**
	 * Zbiera dane symulatorem i wyświetla na sieci.
	 * @param scanTransitions (<b>boolean</b>) true, jeżeli analizujemy tranzycje.
	 * @param scanPlaces (<b>boolean</b>) true, jeżeli analizujemy miejsca.
	 * @param markArcs (<b>boolean</b>)  true, jeżeli zaznaczamy łuki.
	 * @param repetitions (<b>boolean</b>) czy mają być powtórzenia.
	 * @param quickProgressBar (<b>JProgressBar</b>) pasek postępu z okna wywołującego.
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

	/**
	 * Pobieranie danych dla szybkiej symulacji XTPN.
	 * @param bySteps (<b>boolean</b>) czy symulacja po liczbie kroków, czy po czasie.
	 * @param steps (<b>int</b>) liczba kroków.
	 * @param time (<b>double</b>) maksymalny czas.
	 * @param repeate (<b>boolean</b>) czy mają być powtórzenia
	 * @param repetitions (<b>int</b>) liczba powtórzeń.
	 * @param quickProgressBar (<b>JProgressBar</b>) pasek postępu z okna wywołującego.
	 */
	public void acquireDataXTPN(boolean bySteps, int steps, double time,  boolean repeate, int repetitions, JProgressBar quickProgressBar) {
		if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null, "Net simulator working. Unable to retrieve transitions statistics..",
					"Simulator working", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if(GUIController.access().getCurrentNetType() == PetriNet.GlobalNetType.XTPN) {
			stateSimulatorXTPN = new StateSimulatorXTPN();
			stateSimulatorXTPN.initiateSim(overlord.simSettings);

			SimulatorGlobals ownSettings = new SimulatorGlobals();
			ownSettings.setNetType(SimulatorGlobals.SimNetType.XTPN, true);
			ownSettings.simSteps_XTPN = steps;
			ownSettings.simMaxTime_XTPN = time;
			ownSettings.simulateTime = !bySteps;
			ownSettings.simRepetitions_XTPN = repetitions;

			if(repeate) {
				stateSimulatorXTPN.setThreadDetails(2, this, quickProgressBar, ownSettings);
			} else {
				stateSimulatorXTPN.setThreadDetails(1, this, quickProgressBar, ownSettings);
			}

			Thread myThread = new Thread(stateSimulatorXTPN);
			myThread.start();
		}
	}

	private void statsData(JProgressBar quickProgressBar) {
		stateSimulatorPN = new StateSimulator();
		stateSimulatorPN.initiateSim(true, null);
		stateSimulatorPN.setThreadDetails(5, quickProgressBar, this);
		Thread myThread = new Thread(stateSimulatorPN);
		myThread.start();
	}

	private void vectorData(JProgressBar quickProgressBar) {
		stateSimulatorPN = new StateSimulator();
		stateSimulatorPN.initiateSim(true, null);
		stateSimulatorPN.setThreadDetails(6, quickProgressBar, this);
		Thread myThread = new Thread(stateSimulatorPN);
		myThread.start();
	}

	/**
	 * Metoda wywoływana przez wątek symulacji qSim gdy zakończy główną symulację sieci. Przekazywane są
	 * do niej, poza oczywiście wektorami miejsc i tranzycji, dane zebrane w symulacji, w liście quickSimAllStats.
	 * @param quickSimAllStats (<b>ArrayList[ArrayList[Double]]</b>) wektory danych z symulacji.
	 * @param transitions (<b>ArrayList[Transition]</b>) wektor tranzycji.
	 * @param places (<b>ArrayList[Place]</b>) wektor miejsc.
	 */
	public void finishedStatsData(ArrayList<ArrayList<Double>> quickSimAllStats, ArrayList<Transition> transitions,
			ArrayList<Place> places) {
		ArrayList<Double> avgFire = quickSimAllStats.get(0);
		//ArrayList<Double> stdDev = quickSimAllStats.get(1);
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
			String tmp = Tools.cutValueExt(firing, 6);
			firing = Double.parseDouble(tmp);
			trans.qSimFired = firing;
			
			//trans.
			trans.setAddText(Tools.cutValueExt(firing, 8));
			
			double reliance = firing/max;

			trans.qSimFillValue = (int)((double)30 * reliance);
			
			if(firing < 0.05) {
				trans.qSimFillColor = Color.RED;
				trans.qSimFillValue = 5;
			} else if(firing < 0.15) {
				trans.qSimFillColor = Color.ORANGE;
			} else { 
				trans.qSimFillColor = Color.GREEN;
			}
			
			if(scanTransitions) {
				for(ElementLocation el : trans.getElementLocations()) {
					el.qSimDrawed = true;
				}
				
				trans.qSimDrawed = true;
				trans.qSimDrawStats = true;
				if(firing < 0.01) {
					trans.qSimArcSign = true;
					trans.qSimOvalColor = Color.RED;
				} else {
					trans.qSimArcSign = false;
				}
			} else {
				trans.qSimDrawed = false;
				trans.qSimDrawStats = false;
				trans.qSimArcSign = false;
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
				place.qSimFillColor = Color.BLACK;
				place.qSimOvalColor = Color.RED;
			} else if(avgT < 1){
				place.qSimFillColor = Color.RED;
				place.qSimFillValue = 4;
			} else if(avgT < 5){
				place.qSimFillColor = Color.ORANGE;
				place.qSimFillValue = 8;
			} else {
				double reliance = avgT/maxT;
				int fill = (int)((double)30 * reliance);
				if(fill<8)
					fill = 8;
				place.qSimFillValue = fill;
				place.qSimFillColor = Color.GREEN;
			}
			
			if(scanPlaces) {
				place.qSimDrawed = true;
				place.qSimDrawStats = true;
				
				
				for(ElementLocation el : place.getElementLocations()) {
					el.qSimDrawed = true;
				}

				place.qSimArcSign = ( avgT == 0 );
			} else {
				place.qSimDrawed = false;
				place.qSimDrawStats = false;
				place.qSimArcSign = false;
			}
		}
		
		ArrayList<Arc> arcs = overlord.getWorkspace().getProject().getArcs();
		for(Arc arc : arcs) {
			if(arc.getStartNode().qSimArcSign && arc.getEndNode().qSimArcSign) {
				arc.qSimForcedArc = true;
				arc.qSimForcedColor = Color.RED;
			} else {
				arc.qSimForcedArc = false;
				arc.qSimForcedColor = Color.BLACK;
			}
			if(!markArcs) {
				arc.qSimForcedArc = false;
				arc.qSimForcedColor = Color.BLACK;
			}
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
	}

	/**
	 * Metoda wywoływana przez wątek symulacji qSimXTPN gdy zakończy główną symulację sieci. Przekazywane są
	 * do niej, poza oczywiście wektorami miejsc i tranzycji XTPN, dane zebrane w symulacji, w obiekcie
	 * klasy QuickSimMatrix.
	 * @param result (<b>StateSimulatorXTPN.QuickSimMatrix</b>) klasa kontener danych symulacju
	 * @param transitions (<b>ArrayList[TransitionXTPN]</b>) wektor tranzycji.
	 * @param places (<b>ArrayList[PlaceXTPN]</b>) wektor miejsc.
	 */
	public void finishedStatsDataXTPN(StateSimulatorXTPN.QuickSimMatrix result
			, ArrayList<TransitionXTPN> transitions, ArrayList<PlaceXTPN> places) {

		HolmesNotepad note = new HolmesNotepad(800, 600);

		note.addTextLineNL("Simulation data", "text");
		note.addTextLineNL("Avg. steps:  " + Tools.cutValue(result.simSteps), "text");
		note.addTextLineNL("Avg. time:   " + Tools.cutValue(result.simTime), "text");
		note.addTextLineNL("Repetitions: " + (int)result.simReps, "text");

		long milisecond = result.compTime;
		long seconds = milisecond /= 1000;
		long hours = seconds / 3600;
		String h = hours+"";
		if(h.length() == 1)
			h = "0" + h;

		seconds = seconds - (hours * 3600);
		long minutes = seconds / 60;
		String m = minutes+"";
		if(m.length() == 1)
			m = "0" + m;

		seconds = seconds - (minutes * 60);
		String s = seconds+"";
		if(s.length() == 1)
			s = "0" + s;

		note.addTextLine("Simulation time recorded (h:m:s) : ", "text");
		note.addTextLineNL(h + ":" + m + ":" + s, "text");

		int transIndex = 0;
		double simSteps = result.simSteps;
		double simTime = result.simTime;

		note.addTextLineNL("", "bold");
		note.addTextLineNL("Transitions data", "bold");
		for(TransitionXTPN trans : transitions) {

			double tmpSteps = result.transDataMatrix.get(transIndex).get(0); //trans.simInactiveState
			double tmpTime = result.transDataMatrix.get(transIndex).get(4); //trans.simInactiveTime


			note.addTextLine("Transition ", "text");
			note.addTextLine(""+transIndex, "bold");
			note.addTextLine(" Type: ", "text");
			note.addTextLineNL(getTransType(trans), "bold");


			String text = "   Inactive (#):  "+(int)tmpSteps + " ("+Tools.cutValue((tmpSteps*100)/simSteps) +
					"%) | \u03C4: "+ Tools.cutValue(tmpTime) + " ("+Tools.cutValue((tmpTime * 100)/simTime)+"%)" ;
			note.addTextLineNL(text, "text");

			tmpSteps = result.transDataMatrix.get(transIndex).get(1); //trans.simActiveState
			tmpTime = result.transDataMatrix.get(transIndex).get(5); //trans.simActiveTime
			text = "   Active (#):    "+(int)tmpSteps + " ("+Tools.cutValue((tmpSteps*100)/simSteps) +
					"%) | \u03C4: "+ Tools.cutValue(tmpTime) + " ("+Tools.cutValue((tmpTime * 100)/simTime)+"%)" ;
			note.addTextLineNL(text, "text");

			tmpSteps = result.transDataMatrix.get(transIndex).get(2); //trans.simProductionState
			tmpTime = result.transDataMatrix.get(transIndex).get(6); //trans.simProductionTime
			text = "   Production (#): "+(int)tmpSteps + " ("+Tools.cutValue((tmpSteps*100)/simSteps) +
					"%) | \u03C4: "+ Tools.cutValue(tmpTime) + " ("+Tools.cutValue((tmpTime * 100)/simTime)+"%)" ;
			note.addTextLineNL(text, "text");

			tmpSteps = result.transDataMatrix.get(transIndex).get(3); //trans.simFiredState
			text = "   Fired (#): "+(int)tmpSteps ;
			note.addTextLineNL(text, "text");

			note.addTextLineNL("", "text");
			transIndex++;
		}
		note.addTextLineNL("", "bold");
		note.addTextLineNL("Places data", "bold");
		int placeIndex = 0;
		for(PlaceXTPN place : places) {
			note.addTextLine("Place ", "text");
			note.addTextLine(""+placeIndex, "bold");
			note.addTextLine(" Type: ", "text");
			note.addTextLineNL(getPlaceType(place), "bold");

			double avgTokens = result.avgTokens.get(placeIndex);
			note.addTextLineNL("   Avg. tokens: "+Tools.cutValue(avgTokens), "text");
			placeIndex++;
		}

		note.setCaretFirstLine();
		note.setVisible(true);

		/*

		transIndex = 0;
		for(TransitionXTPN trans : transitions) {
			double tmpSteps = result.transDataMatrix.get(transIndex).get(0); //trans.simInactiveState
			double tmpTime = result.transDataMatrix.get(transIndex).get(4); //trans.simInactiveTime
			trans.qSimXTPN.text1 = "Inactive (#):  "+(int)tmpSteps + " ("+Tools.cutValue((tmpSteps*100)/simSteps) +
					"%) | \u03C4: "+ Tools.cutValue(tmpTime) + " ("+Tools.cutValue((tmpTime * 100)/simTime)+"%)" ;

			tmpSteps = result.transDataMatrix.get(transIndex).get(1); //trans.simActiveState
			tmpTime = result.transDataMatrix.get(transIndex).get(5); //trans.simActiveTime
			trans.qSimXTPN.text2 = "Active (#):    "+(int)tmpSteps + " ("+Tools.cutValue((tmpSteps*100)/simSteps) +
					"%) | \u03C4: "+ Tools.cutValue(tmpTime) + " ("+Tools.cutValue((tmpTime * 100)/simTime)+"%)" ;

			tmpSteps = result.transDataMatrix.get(transIndex).get(2); //trans.simProductionState
			tmpTime = result.transDataMatrix.get(transIndex).get(6); //trans.simProductionTime
			trans.qSimXTPN.text3 = "Production (#): "+(int)tmpSteps + " ("+Tools.cutValue((tmpSteps*100)/simSteps) +
					"%) | \u03C4: "+ Tools.cutValue(tmpTime) + " ("+Tools.cutValue((tmpTime * 100)/simTime)+"%)" ;

			tmpSteps = result.transDataMatrix.get(transIndex).get(3); //trans.simFiredState
			trans.qSimXTPN.text4 = "Fired (#): "+(int)tmpSteps ;

			trans.showQSimXTPN = true;
			transIndex++;
		}
		overlord.getWorkspace().getProject().repaintAllGraphPanels();
		 */
	}

	private String getTransType(TransitionXTPN transition) {
		if(transition.isAlphaModeActive() && transition.isBetaModeActive()) {
			return "XTPN";
		} else if(transition.isAlphaModeActive()) {
			return "TPN";
		} else if(transition.isBetaModeActive()) {
			return "DPN";
		} else {
			return "classical PN";
		}
	}

	private String getPlaceType(PlaceXTPN place) {
		if(place.isGammaModeActive()) {
			return "time-place";
		} else {
			return "classical place";
		}
	}
}
