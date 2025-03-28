package holmes.petrinet.simulators;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.elements.TransitionXTPN;
import holmes.petrinet.functions.FunctionsTools;
import holmes.windows.ssim.HolmesSim;

/**
 * Klasa symulatora. Różnica między nią a symulatorem graficznym jest taka, że poniższe metody potrafią wygenerować
 * dziesiątki tysięcy stanów na sekundę.
 */
public class StateSimulator implements Runnable {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private ArrayList<Transition> transitions;
    private ArrayList<Place> places;
	private boolean readyToSimulate = false;
	public double timeNetStepCounter = 0;
	public double timeNetPartStepCounter = 0;
	
	private ArrayList<ArrayList<Integer>> placesData = null;
	private ArrayList<Double> placesAvgData = null; //średnia liczba tokenów w miejscu
	private ArrayList<Long> placesTotalData = null; //średnia liczba tokenów w miejscu
	private ArrayList<ArrayList<Integer>> transitionsData = null;
	private ArrayList<Integer> transitionsTotalFiring = null; //wektor sumy odpaleń tranzycji
	private ArrayList<Double> transitionsAvgData = null;
	private ArrayList<Integer> internalBackupMarkingZero = new ArrayList<Integer>();

	private IEngine engine;
	
	//runtime:
	private boolean terminate = false;
	
	public JProgressBar progressBar;//pasek postępu symulacji
	private HolmesSim boss;	//okno nadrzędne symulatora
	private int simulationType;	//aktywny tryb symulacji

	private NetSimulationData currentDataPackage;
	private ArrayList<ArrayList<Double>> quickSimAllStats;
	private QuickSimTools quickSim;

	/**
	 * Główny konstruktor obiektu klasy StateSimulator.
	 */
	public StateSimulator() {
		engine = new SimulatorStandardPN();
	}

	/**
	 * Metoda ustawiająca obiekty dla symulacji w osobnym wątku.
	 * @param simulationType int - typ symulacji: 1 - standard; 2 - ref. zbieranie danych, 3 - knockout (jak ref)
	 * @param blackBox Object... - zależy od trybu powyżej
	 */
	public void setThreadDetails(int simulationType, Object... blackBox) {
		this.simulationType = simulationType;
		if(simulationType == 1) { //standardowy tryb symulacji
			this.boss = (HolmesSim)blackBox[0];
			this.progressBar = (JProgressBar)blackBox[1];
		} else if(simulationType == 2) { //obliczenie zbioru referencyjnego
			this.boss = (HolmesSim)blackBox[0];
			this.progressBar = (JProgressBar)blackBox[1];
			this.currentDataPackage = (NetSimulationData)blackBox[2];
		} else if(simulationType == 3) { //obliczenie danych przy knockoutcie elementów
			this.boss = (HolmesSim)blackBox[0];
			this.progressBar = (JProgressBar)blackBox[1];
			this.currentDataPackage = (NetSimulationData)blackBox[2];
		} else if(simulationType == 4) { //obliczenie danych przy knockoutcie elementów
			this.boss = (HolmesSim)blackBox[0];
			this.progressBar = (JProgressBar)blackBox[1];
			this.currentDataPackage = (NetSimulationData)blackBox[2];
		} else if(simulationType == 5) { //QuickSimReps
			this.progressBar = (JProgressBar)blackBox[0];
			this.quickSim = (QuickSimTools)blackBox[1];
		} else if(simulationType == 6) { //QuickSimNoReps
			this.progressBar = (JProgressBar)blackBox[0];
			this.quickSim = (QuickSimTools)blackBox[1];
		} else if(simulationType == 7) { //SSA v1.0
			this.progressBar = (JProgressBar)blackBox[0];
			this.quickSim = (QuickSimTools)blackBox[1];
		}
	}

	/**
	 * Uruchamiania zdalnie, zakłada, że wszystko co potrzebne zostało już ustawione za pomocą setThreadDetails(...)
	 */
	public void run() {
		this.terminate = false;
		if(simulationType == 1) {
			simulateNetAll();
			boss.completeSimulationProcedures();
		} else if(simulationType == 2) {
			NetSimulationData data = simulateNetReferenceAndKnockout();
			boss.accessKnockoutTab().action.completeRefSimulationResults(data, transitions, places);
		} else if(simulationType == 3) {
			NetSimulationData data = simulateNetReferenceAndKnockout();
			boss.accessKnockoutTab().action.completeKnockoutSimulationResults(data, transitions, places);
		} else if(simulationType == 4) {
			NetSimulationData data = simulateNetReferenceAndKnockout();
			boss.accessKnockoutTab().action.pingPongSimulation(data, transitions, places, terminate);
		} else if(simulationType == 5) {
			quickSimGatherData();
			quickSim.finishedStatsData(quickSimAllStats, transitions, places);
		} else if(simulationType == 6) {
			quickSimGatherDataNoReps();
			quickSim.finishedStatsData(quickSimAllStats, transitions, places);
		} else if(simulationType == 7) {
			quickSimGatherDataSSA();
			quickSim.finishedStatsData(quickSimAllStats, transitions, places);
		}
		this.terminate = false;
	}
	
	/**
	 * Metoda ta musi być wywołana przed każdym startem symulatora. Inicjalizuje początkowe struktury
	 * danych dla symulatora.
	 * @param useGlobals boolean - jeśli true, parametry SimulatorGlobals brane z obiektu globalSettings
	 * @param ownSettings SimulatorGlobals - jeśli powyższej jest = false, to stąd są brane parametry
	 * @return boolean - true, jeśli wszystko się udało
	 */
	public boolean initiateSim(boolean useGlobals, SimulatorGlobals ownSettings) {
		checkEngine(useGlobals);
		
		transitions = overlord.getWorkspace().getProject().getTransitions();
        ArrayList<Transition> time_transitions = overlord.getWorkspace().getProject().getTimeTransitions();
		places = overlord.getWorkspace().getProject().getPlaces();
		if(transitions == null || places == null) {
			readyToSimulate = false;
			return readyToSimulate;
		}
		//if(!(transitions.size() > 0 && places.size() > 0)) {
		if(transitions.isEmpty() || places.isEmpty()) {
			readyToSimulate = false;
			return readyToSimulate;
		}
		
		placesData = new ArrayList<ArrayList<Integer>>();
		placesAvgData = new ArrayList<Double>();
		placesTotalData = new ArrayList<Long>();
		transitionsData = new ArrayList<ArrayList<Integer>>();
		transitionsTotalFiring = new ArrayList<Integer>();
		transitionsAvgData = new ArrayList<Double>();
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
			placesTotalData.add(0L);
		}
		
		if(useGlobals || ownSettings==null) {
			engine.setEngine(
					overlord.simSettings.getNetType(), 
					overlord.simSettings.isMaxMode(),
					overlord.simSettings.isSingleMode(), 
					transitions, time_transitions, places);
		} else {
			engine.setEngine(
					ownSettings.getNetType(), 
					ownSettings.isMaxMode(),
					ownSettings.isSingleMode(), 
					transitions, time_transitions, places);
		}
		
		readyToSimulate = true;
		return readyToSimulate;
	}
	
	/**
	 * Metoda sprawdza, czy obiekt silnika zgadza się z aktualnie ustawionym w opcjach symulacji. Jeśli nie,
	 * tworzy odpowiedni obiekt. Jeśli useGlobals == false, wtedy domyślnie sprawdza czy działa normalny symulator, jeśl
	 * nie, to go przywraca.
	 * @param useGlobals boolean - true, jeśli mają być respektowane główne ustawienia symulacji
	 */
	private void checkEngine(boolean useGlobals) {
		if(useGlobals) {
			int engineType = overlord.simSettings.getSimulatorType();
			if(engineType == 0) {
				if(!(engine instanceof SimulatorStandardPN)) {
					engine = new SimulatorStandardPN();
				}
			} else if (engineType == 1) {
				if(!(engine instanceof SPNengine)) {
					engine = new SPNengine();
				}
			} else if (engineType == 2) {
				if(!(engine instanceof SPNengine)) {
					engine = new SSAengine();
				}
			} else { //domyślnie standardowy silnik
				if(!(engine instanceof SimulatorStandardPN)) {
					engine = new SimulatorStandardPN();
				}
			}
		} else { //domyślny, prosty tryb
			if(!(engine instanceof SimulatorStandardPN)) {
				engine = new SimulatorStandardPN();
			}
		}
	}

	/**
	 * Szybsza wersja initiateSim(...), metoda zeruje wektory danych.
	 */
	public void clearData() {
		placesData = new ArrayList<ArrayList<Integer>>();
		placesAvgData = new ArrayList<Double>();
		placesTotalData = new ArrayList<Long>();
		transitionsData = new ArrayList<ArrayList<Integer>>();
		transitionsTotalFiring = new ArrayList<Integer>();
		transitionsAvgData = new ArrayList<Double>();
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
			placesTotalData.add(0L);
		}
		terminate = false;
		readyToSimulate = true;
	}

	/**
	 * Metoda pracująca w wątku. Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
	 * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
	 */
	public void simulateNetAll() {
		if(!readyToSimulate) {
			JOptionPane.showMessageDialog(null,lang.getText("Simulation cannot start, engine initialization failed."), 
					lang.getText("problem"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		prepareNetM0();
		
		ArrayList<Transition> launchableTransitions;
		int stepsLimit = overlord.simSettings.getSimSteps();
		int updateTime = stepsLimit / 50;
		
		String max = lang.getText("SS_entry002");
		if(overlord.simSettings.isMaxMode())
			max = lang.getText("SS_entry003");

		String strB = "err.";
		try {
			strB = String.format(lang.getText("SS_entry004"), stepsLimit, max);
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentryLNGexc")+" "+"SS_entry004", "error", true);
		}
		overlord.log(strB, "text", true);
		
		int trueSteps = 0;
		for(int i=0; i<stepsLimit; i++) {
			if(terminate)
				break;
			
			progressBar.setValue(i+1);
			trueSteps++;
			
			if(i % updateTime == 0)
				progressBar.update(progressBar.getGraphics());

			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(overlord.simSettings.isEmptySteps());
				launchSubtractPhase(launchableTransitions); //zabierz tokeny poprzez aktywne tranzycje
				removeDPNtransition(launchableTransitions);

				ArrayList<Integer> transRow = new ArrayList<Integer>();
				for(int t=0; t<transitions.size(); t++)
					transRow.add(0);
				
				if(launchableTransitions != null) {
					for(Transition trans : launchableTransitions) {
						int index = transitions.lastIndexOf(trans);
						transRow.set(index, 1); //dodaj tylko tranzycjom, które odpaliły
						int fired = transitionsTotalFiring.get(index);
						transitionsTotalFiring.set(index, fired+1); //wektor sumy odpaleń
					}
				}
				transitionsData.add(transRow);
				
			} else {
				break;
			}
			launchAddPhase(launchableTransitions);
			
			//zbierz informacje o tokenach w miejsach:
			ArrayList<Integer> marking = new ArrayList<Integer>();
			for(int p=0; p<places.size(); p++) {
				int tokens = places.get(p).getTokensNumber();
				marking.add(tokens);
				
				double sumOfTokens = placesAvgData.get(p);
				placesAvgData.set(p, sumOfTokens+tokens);
			}
			placesData.add(marking);
		}
		
		for(int p=0; p<places.size(); p++) {
			double sumOfTokens = placesAvgData.get(p);
			placesTotalData.set(p, (long) sumOfTokens);
		}
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsAvgData.add((double)transitionsTotalFiring.get(t)/(double)trueSteps);
		}
		for(int p=0; p<places.size(); p++) {
			double sumOfTokens = placesAvgData.get(p);
			placesAvgData.set(p, sumOfTokens/(double)trueSteps);
		}
		overlord.log(lang.getText("LOGentry00414"), "text", true);
		readyToSimulate = false;
		restoreInternalMarkingZero();
	}

	/**
	 * Uniwersalna metoda pracująca w osobnym wątku, zbierająca dane o symulacji sieci przez zadaną liczbe kroków
	 * oraz przez ustaloną liczbę powtórek symulacji. Zwraca obiekt klasy kontenerowej NetSimulationData. Może być
	 * to zarówno pakiet danych referencyjnych, jak i pakiet danych dla wyłączonych odpowiednich tranzycji. Oczywiście
	 * musi być to wszystko ustawione przed jej uruchomieniem.
	 * @return <b>NetSimulationData</b> - pakiet danych z powtórzonych symulacji.
	 */
	public NetSimulationData simulateNetReferenceAndKnockout() {
		if(!readyToSimulate) {
			JOptionPane.showMessageDialog(null,lang.getText("SS_entry001"), 
					lang.getText("problem"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		prepareNetM0();
		int pBarTotal = 0;
		ArrayList<Transition> launchingTransitions; //odpalone tranzycje
		
		//	INIT MAIN VECTORS:
		ArrayList<Long> totalPlaceTokensInTurn = new ArrayList<Long>();
		ArrayList<Integer> totalTransFiringInTurn = new ArrayList<Integer>();
		
		ArrayList<ArrayList<Double>> placesAll = new ArrayList<>();
		ArrayList<ArrayList<Double>> transAll = new ArrayList<>();
		
		int stepsLimit = overlord.simSettings.getSimSteps();
		int repetitions = overlord.simSettings.getRepetitions();
		boolean emptySteps = overlord.simSettings.isEmptySteps();
		
		//NetSimulationData netData = new NetSimulationData();
		int placeNumber = places.size();
		int transNumber = transitions.size();
		currentDataPackage.maxMode = overlord.simSettings.isMaxMode();
		currentDataPackage.netSimType = overlord.simSettings.getNetType();
		currentDataPackage.steps = stepsLimit;
		currentDataPackage.reps = repetitions;
		currentDataPackage.placesNumber = placeNumber;
		currentDataPackage.transNumber = transNumber;
		for(int p=0; p<placeNumber; p++) {
			totalPlaceTokensInTurn.add((long) 0);
			currentDataPackage.placeTokensAvg.add(0.0);
			currentDataPackage.placeTokensMin.add(Double.MAX_VALUE);
			currentDataPackage.placeTokensMax.add(0.0);
			currentDataPackage.placeZeroTokens.add(0);
			
			currentDataPackage.startingState = overlord.getWorkspace().getProject().accessStatesManager().getCurrentStatePN();
		}
		for(int t=0; t<transNumber; t++) {
			totalTransFiringInTurn.add(0);
			currentDataPackage.transFiringsAvg.add(0.0);
			currentDataPackage.transFiringsMin.add(Double.MAX_VALUE);
			currentDataPackage.transFiringsMax.add(0.0);
			currentDataPackage.transZeroFiring.add(0);
			
			//poniższe dzieje się przed uruchomieniem symulatora:
			//if(transitions.get(t).isOffline())
			//	currentDataPackage.disabledTransitionsIDs.add(t);
		}
		//	INIT VECTORS COMPLETED
		
		int pBarInterval = (repetitions*stepsLimit) / 100;
		int pBarStep = 0;
		progressBar.setMaximum(repetitions*stepsLimit);
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		
		for(int turn=0; turn<repetitions; turn++) {
			int realStepCounter = 0;
			for(int p=0; p<placeNumber; p++)
				totalPlaceTokensInTurn.set(p, (long) 0);
			for(int t=0; t<transNumber; t++)
				totalTransFiringInTurn.set(t, 0);
			
			for(int i=0; i<stepsLimit; i++) {
				if(terminate)
					break;
				
				realStepCounter++; //dzielnik statystyk
				pBarTotal++; //aktualna wartość paska przewijania
				pBarStep++; //potrzeby aby wiedzieć kiedy update paska postępu
				if(pBarStep == pBarInterval) {
					pBarStep = 0;
					progressBar.setValue(pBarTotal);
					progressBar.update(progressBar.getGraphics());
				}

				if (isPossibleStep()){ 
					launchingTransitions = engine.getTransLaunchList(emptySteps);
					launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
					removeDPNtransition(launchingTransitions);
					
					if(launchingTransitions != null) {
						for(Transition trans : launchingTransitions) {
							int index = transitions.lastIndexOf(trans);
							int val = totalTransFiringInTurn.get(index) + 1;
							totalTransFiringInTurn.set(index, val);
						}
					}
				} else {
					pBarTotal--;
					break;
				}
				launchAddPhase(launchingTransitions);
				
				for(int p=0; p<placeNumber; p++) {
					long val = totalPlaceTokensInTurn.get(p) + places.get(p).getTokensNumber();
					totalPlaceTokensInTurn.set(p, val);
				}
			}
			
			ArrayList<Double> histVector = new ArrayList<Double>();
			for(int p=0; p<placeNumber; p++) {
				double simTokenValue = totalPlaceTokensInTurn.get(p);
				simTokenValue /= realStepCounter;
				histVector.add(simTokenValue);
				
				double oldVal = currentDataPackage.placeTokensAvg.get(p) + simTokenValue;
				currentDataPackage.placeTokensAvg.set(p, oldVal);
				
				double oldMin = currentDataPackage.placeTokensMin.get(p);
				if(simTokenValue < oldMin)
					currentDataPackage.placeTokensMin.set(p, simTokenValue);
				
				double oldMax = currentDataPackage.placeTokensMax.get(p);
				if(simTokenValue > oldMax) {
					currentDataPackage.placeTokensMax.set(p, simTokenValue);
				}
				
				if(simTokenValue == 0) {
					int val = currentDataPackage.placeZeroTokens.get(p) + 1;
					currentDataPackage.placeZeroTokens.set(p, val);
				}
			}
			placesAll.add(histVector);
			
			histVector = new ArrayList<Double>();
			for(int t=0; t<transNumber; t++) {
				double simFiringValue = totalTransFiringInTurn.get(t);
				simFiringValue /= realStepCounter;
				histVector.add(simFiringValue);
				
				double oldVal = currentDataPackage.transFiringsAvg.get(t) + simFiringValue;
				currentDataPackage.transFiringsAvg.set(t, oldVal);
				
				double oldMin = currentDataPackage.transFiringsMin.get(t);
				if(simFiringValue < oldMin)
					currentDataPackage.transFiringsMin.set(t, simFiringValue);
				
				double oldMax = currentDataPackage.transFiringsMax.get(t);
				if(simFiringValue > oldMax)
					currentDataPackage.transFiringsMax.set(t, simFiringValue);

				if(simFiringValue == 0) {
					int val = currentDataPackage.transZeroFiring.get(t) + 1;
					currentDataPackage.transZeroFiring.set(t, val);
				}
			}
			transAll.add(histVector);
			
			restoreInternalMarkingZero();
		} //kolejna powtórka
		
		//uśrednianie po zakończeniu powtórek:
		for(int p=0; p<placeNumber; p++) {
			double oldVal = currentDataPackage.placeTokensAvg.get(p) / (double)repetitions;
			currentDataPackage.placeTokensAvg.set(p, oldVal);
			
		}
		for(int t=0; t<transNumber; t++) {
			double oldVal = currentDataPackage.transFiringsAvg.get(t) / (double)repetitions;
			currentDataPackage.transFiringsAvg.set(t, oldVal);
		}
		
		//stats data:
		double variance;
		for(int p=0; p<placeNumber; p++) {
			variance = 0;
			double avg = currentDataPackage.placeTokensAvg.get(p);
			for(int r=0; r<repetitions;  r++) {
				double val = placesAll.get(r).get(p);
				val = avg - val;
				variance += val * val;
			}
			variance /= repetitions;
			currentDataPackage.placeStdDev.add(Math.sqrt(variance)); //standard deviation for place
		}
		for(int t=0; t<transNumber; t++) {
			variance = 0;
			double avg = currentDataPackage.transFiringsAvg.get(t);
			for(int r=0; r<repetitions;  r++) {
				double val = transAll.get(r).get(t);
				val = avg - val;
				variance += val * val;
			}
			variance /= repetitions;
			double sigma = Math.sqrt(variance);
			currentDataPackage.transStdDev.add(sigma); //standard deviation for transition
		}
		
		//within stdDev range:
		for(int p=0; p<placeNumber; p++) {
			double stdDev = currentDataPackage.placeStdDev.get(p);
			double avg = currentDataPackage.placeTokensAvg.get(p);

			int support = 0;
			int support2 = 0;
			int support3 = 0;
			int support4 = 0;
			int support5 = 0;
			for(int r=0; r<repetitions;  r++) {
				double val = placesAll.get(r).get(p);
				if((avg-5*stdDev) < val && val < (avg+5*stdDev)) {
					support5++;
					
					if((avg-4*stdDev) < val && val < (avg+4*stdDev)) {
						support4++;
						
						if((avg-3*stdDev) < val && val < (avg+3*stdDev)) {
							support3++;
							
							if((avg-2*stdDev) < val && val < (avg+2*stdDev)) {
								support2++;
								
								if((avg-stdDev) < val && val < (avg+stdDev))
									support++;
							}
						}
					}
				}
			}
			ArrayList<Integer> res = new ArrayList<>();
			res.add(support);
			res.add(support2);
			res.add(support3);
			res.add(support4);
			res.add(support5);
			currentDataPackage.placeWithinStdDev.add(res);
		}
		for(int t=0; t<transNumber; t++) {
			double stdDev = currentDataPackage.transStdDev.get(t);
			double avg = currentDataPackage.transFiringsAvg.get(t);

			int support = 0;
			int support2 = 0;
			int support3 = 0;
			int support4 = 0;
			int support5 = 0;
			for(int r=0; r<repetitions;  r++) {
				double val = transAll.get(r).get(t);
				if((avg-5*stdDev) < val && val < (avg+5*stdDev)) {
					support5++;
					
					if((avg-4*stdDev) < val && val < (avg+4*stdDev)) {
						support4++;
						
						if((avg-3*stdDev) < val && val < (avg+3*stdDev)) {
							support3++;
							
							if((avg-2*stdDev) < val && val < (avg+2*stdDev)) {
								support2++;
								
								if((avg-stdDev) < val && val < (avg+stdDev))
									support++;
							}
						}
					}
				}
			}
			ArrayList<Integer> res = new ArrayList<>();
			res.add(support);
			res.add(support2);
			res.add(support3);
			res.add(support4);
			res.add(support5);
			currentDataPackage.transWithinStdDev.add(res);
		}

		//readyToSimulate = false;
		restoreInternalMarkingZero();
		return currentDataPackage;
	}

	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
	 * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
	 * Od simulate różni się tym, że nie zbiera historii odpaleń i tokenów dla miejsc, tylko
	 * średnie wartości. Dzięki temu działa szybciej i nie zabiera tyle miejsca w pamięci.
	 * @param steps <b>int</b> - liczba kroków do symulacji
	 * @param placesToo <b>boolean</b> - true, jeśli ma gromadzić też dane dla miejsc
	 * @param emptySteps <b>boolean</b> - true, jeśli dozwolone puste przebiegi
	 * @return <b>int</b> - liczba rzeczywiście wykonanych kroków
	 */
	public int simulateNetSimple(int steps, boolean placesToo, boolean emptySteps) {
		if(!readyToSimulate) {
			overlord.log("Simulation simple mode cannot start.", "warning", true);
			return 0;
		}
		prepareNetM0(); //backup, m0, etc.
		ArrayList<Transition> launchableTransitions;
		int internalSteps = 0;
		//boolean emptySteps = overlord.simSettings.isEmptySteps();
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(emptySteps);
				launchSubtractPhase(launchableTransitions);
				removeDPNtransition(launchableTransitions);
				
				//TODO: przeniesiono znad tych dwóch polecenia wyżej
				if(launchableTransitions != null) {
					for(Transition trans : launchableTransitions) {
						int index = transitions.lastIndexOf(trans);
						int fired = transitionsTotalFiring.get(index);
						transitionsTotalFiring.set(index, fired+1); //wektor sumy odpaleń
					}
				}
			} else {
				break;
			}
			launchAddPhase(launchableTransitions);
			
			if(placesToo)
				for(int p=0; p<places.size(); p++) {
					int tokens = places.get(p).getTokensNumber();
					double sumOfTokens = placesAvgData.get(p);
					placesAvgData.set(p, sumOfTokens+tokens);
				}
		}
		
		for(int p=0; p<places.size(); p++) {
			double sumOfTokens = placesAvgData.get(p);
			placesTotalData.set(p, (long) sumOfTokens);
		}
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsAvgData.add((double)transitionsTotalFiring.get(t)/(double)internalSteps);
		}
		
		if(placesToo)
			for(int p=0; p<places.size(); p++) {
				double sumOfTokens = placesAvgData.get(p);
				placesAvgData.set(p, sumOfTokens/(double)internalSteps);
			}
	
		readyToSimulate = false;
		restoreInternalMarkingZero();
		return internalSteps;
	}
	
	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, dla konkretnego
	 * miejsca. Dane dla wybranego miejsca funkcja zwraca jako ArrayList[Integer].
	 * @param steps <b>int</b> - liczba kroków do symulacji
	 * @param place <b>Place</b> - wybrane miejsce do testowania
	 * @param emptySteps <b>boolean</b> - true, jeśli dozwolone puste przebiegi
	 * @return ArrayList[<b>Integer</b>] - wektor danych o tokenach w miejscu
	 */
	public ArrayList<Integer> simulateNetSinglePlace(int steps, Place place, boolean emptySteps) {
		if(!readyToSimulate) {
			String strB = "err.";
			try {
				strB = String.format(lang.getText("SS_entry005"), place.getName());
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"SS_entry005", "error", true);
			}
			overlord.log(strB, "warning", true);
			return null;
		}
		prepareNetM0();

		ArrayList<Integer> placeDataVector = new ArrayList<Integer>();
		//int internalSteps = 0;
		ArrayList<Transition> launchableTransitions;
		for(int i=0; i<steps; i++) {
			//internalSteps++;
			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(emptySteps); //wypełnia launchableTransitions
				launchSubtractPhase(launchableTransitions);
				removeDPNtransition(launchableTransitions);
			} else {
				break;
			}
			launchAddPhase(launchableTransitions);
			placeDataVector.add(place.getTokensNumber());
		}
		readyToSimulate = false;
		restoreInternalMarkingZero();
		return placeDataVector;
	}
	
	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, dla konkretnej
	 * tranzycji. Dane dla wybranej tranzycji funkcja zwraca jako ArrayList[Integer].
	 * @param steps <b>int</b> - liczba kroków do symulacji
	 * @param trans <b>Transition</b> - wybrana tranzycja do testowania
	 * @param emptySteps <b>boolean</b> - true, jeśli dozwolone kroki bez odpalonych tranzycji
	 * @return ArrayList[<b>Integer</b>] - wektor danych o odpalaniu tranzycji
	 */
	public ArrayList<Integer> simulateNetSingleTransition(int steps, Transition trans, boolean emptySteps) {
		if(!readyToSimulate) {
			String strB = "err.";
			try {
				strB = String.format(lang.getText("SS_entry006"), trans.getName());
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"SS_entry006", "error", true);
			}
			overlord.log(strB, "warning", true);
			return null;
		}
		prepareNetM0();
		ArrayList<Transition> launchableTransitions;
		int sum = 0;
		int internalSteps = 0;
		ArrayList<Integer> transDataVector = new ArrayList<Integer>();
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(emptySteps);
				launchSubtractPhase(launchableTransitions); //zabierz tokeny poprzez aktywne tranzycje
				removeDPNtransition(launchableTransitions);
				
				if(launchableTransitions != null) {
					if(launchableTransitions.contains(trans)) {
						transDataVector.add(1);
						sum++;
					} else {
						transDataVector.add(0);
					}
				} else {
					for(int j=0; j<transitions.size(); j++)
						transDataVector.add(0);
				}
			} else {
				break;
			}
			launchAddPhase(launchableTransitions);
		}
		readyToSimulate = false;
		transDataVector.add(sum);
		transDataVector.add(internalSteps);
		restoreInternalMarkingZero();
		return transDataVector;
	}
	
	/**
	 * Symulacja na potrzeby okna podglądu inwariantów - dane o odpaleniach tranzycji w inwariancie.
	 * @param steps <b>int</b> - ile kroków symulacji
	 * @param reps <b>int</b> - ile powtórek
	 * @param emptySteps <b>boolean</b> - true, jeśli dozwolone kroki bez odpalania tranzycji
	 * @return ArrayList[ArrayList[<b>Double</b>]] - dwa wektory danych, pierwszy to średnie, drugi - odchylenie standardowe
	 */
	public ArrayList<ArrayList<Double>> simulateForInvariantTrans(int steps, int reps, boolean emptySteps) {
		ArrayList<ArrayList<Double>> result = new ArrayList<>();
		if(!readyToSimulate) {
			overlord.log("Simulation simple mode cannot start.", "warning", true);
			return null;
		}
		prepareNetM0(); //backup, m0, etc.
		ArrayList<Transition> launchableTransitions;
		ArrayList<ArrayList<Double>> firingHistory = new ArrayList<>();
		
		for(int r=0; r<reps; r++) {
			ArrayList<Double> transFiring = new ArrayList<Double>();
			
			for(int t=0; t<transitions.size(); t++) {
				transFiring.add(0.0);
			}
			
			int internalSteps = 0;
			for(int i=0; i<steps; i++) {
				
				if (isPossibleStep()){ 
					launchableTransitions = engine.getTransLaunchList(emptySteps);
					launchSubtractPhase(launchableTransitions);
					removeDPNtransition(launchableTransitions);
					internalSteps++;
					
					if(launchableTransitions != null) {
						for(Transition trans : launchableTransitions) {
							int index = transitions.lastIndexOf(trans);
							double fired = transFiring.get(index);
							transFiring.set(index, fired+1); //wektor sumy odpaleń
						}
					}
				} else {
					break;
				}
				launchAddPhase(launchableTransitions);
			}
			//srednia liczba odpaleń:
			for(int t=0; t<transFiring.size(); t++) {
				double fired = transFiring.get(t);
				fired /= internalSteps;
				transFiring.set(t, fired);
			}
			firingHistory.add(transFiring);
			restoreInternalMarkingZero();
		}
		
		//przygotowanie wektorów wynikowych:
		ArrayList<Double> avgFiring = new ArrayList<>();
		ArrayList<Double> stdDev = new ArrayList<>();
		for(int t=0; t<transitions.size(); t++) {
			avgFiring.add(0.0);
		}
		
		//liczenie średnich:
		for(int t=0; t<transitions.size(); t++) {
			for(int r=0; r<reps; r++) {
				double value = firingHistory.get(r).get(t);
				double oldRes = avgFiring.get(t);
				avgFiring.set(t, oldRes+value);
			}
			double oldRes = avgFiring.get(t);
			oldRes /= reps;
			avgFiring.set(t, oldRes);
		}
		//stdDev
		for(int t=0; t<transitions.size(); t++) {
			double variance = 0.0;
			for(int r=0; r<reps; r++) {
				double value = avgFiring.get(t); //średnia obliczona krok wcześniej
				double diff = value - firingHistory.get(r).get(t); 
				variance += (diff*diff);
			}
			variance /= reps;
			stdDev.add(Math.sqrt(variance));
		}
		
		readyToSimulate = false;
		restoreInternalMarkingZero();
		result.add(avgFiring);
		result.add(stdDev);
		return result;
	}
	
	//********************************************************************************************************************************
	//****************************************              **************************************************************************
	//****************************************   INTERNALS  **************************************************************************
	//****************************************              **************************************************************************
	//********************************************************************************************************************************
	
	/**
	 * Ustawia status wymuszonego kończenia symulacji. 
	 * @param val <b>boolean</b> - true, jeśli symulator ma zakończyć działanie
	 */
	public void setCancelStatus(boolean val) {
		this.terminate = val;
		if(val)
			readyToSimulate = false;
	}
	
	/**
	 * Zwraca flagę wymuszonego kończenia symulacji.
	 * @return <b>boolean</b> - true, jeśli symulator został awaryjnie wyłączony
	 */
	public boolean getCancelStatus() {
		return this.terminate;
	}
	
	/**
	 * Metoda sprawdza, czy tranzycja DPN jest w fazie liczenia wewnętrznego zegara aż do punktu
	 * określonego zmienną duration. Czyli jeśli timer to 0, to tranzycja liczy. Jeśli w takim wypadku
	 * duration > 0, wtedy ją usuwamy z listy launchingTransitions (tokeny już odjęto z miejsc, dodanie
	 * nastąpi kilka kroków później). Jeśli timer = duration = 0, tranzycja zostaje na liście.
	 * @param launchingTransitions ArrayList[<b>Integer</b>] - lista tranzycji odpalających
	 */
	private void removeDPNtransition(ArrayList<Transition> launchingTransitions) {
		if(launchingTransitions == null)
			return;
		
		for(int t=0; t<launchingTransitions.size(); t++) {
			Transition test_t = launchingTransitions.get(t);
			if(test_t.timeExtension.isDPN()) {
				if(test_t.timeExtension.getDPNtimer() == 0 && test_t.timeExtension.getDPNduration() != 0) {
					launchingTransitions.remove(test_t);
					t--;
				}
			}
		}
	}
	
	/**
	 * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych do odpalonych tranzycji
	 * @param launchingTransitions ArrayList[<b>Transition</b>] - lista uruchamianych tranzycji
	 */
	private void launchSubtractPhase(ArrayList<Transition> launchingTransitions) {
		if(launchingTransitions == null)
			return;
		
		ArrayList<Arc> arcs;
		for (Transition transition : launchingTransitions) {
			if(transition.timeExtension.getDPNtimer() > 0) //yeah, trust me, I'm an engineer
				continue;
			//innymi słowy: nie odejmuj tokenów, jeśli timer DPN to 1, 2 lub więcej. Odejmuj gdy = 0, a gdy jest
			//równy -1, to w ogóle nie będzie takiej tranzycji na liście transitions tutaj.
			
			transition.setLaunching(true);
			arcs = transition.getInputArcs();
			for (Arc arc : arcs) {
				Place place = (Place)arc.getStartNode();
				
				if(arc.getArcType() == TypeOfArc.INHIBITOR) {
					// nic nie zabieraj
				} else if(arc.getArcType() == TypeOfArc.READARC) {
					// nic nie zabieraj
				} else if(arc.getArcType() == TypeOfArc.RESET) {
					int tokens = place.getTokensNumber();
					place.addTokensNumber(-tokens);
				} else if(arc.getArcType() == TypeOfArc.EQUAL) {
					place.addTokensNumber(-arc.getWeight());
				} else {
					FunctionsTools.functionalExtraction(transition, arc, place);
					//place.modifyTokensNumber(-arc.getWeight());
				}
			}
		}
	}
	
	/**
	 * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna tranzycja.
	 * @return <b>boolean</b> - true jeśli jest choć jedna aktywna tranzycja; false w przeciwnym wypadku
	 */
	private boolean isPossibleStep() {
		if(engine instanceof SimulatorStandardPN) {
			for (Transition transition : transitions) {
				if (transition.isActive())
					return true;
			}
			return false;
		} else
			return true; //sprawdzane inaczej
	}
	
	/**
	 * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych z odpalonych tranzycji. 
	 * @param launchingTransitions ArrayList[<b>Transition</b>] - lista odpalanych tranzycji
	 */
	private void launchAddPhase(ArrayList<Transition> launchingTransitions) {
		if(launchingTransitions == null)
			return;
		
		ArrayList<Arc> arcs;
		for (Transition transition : launchingTransitions) {
			transition.setLaunching(false);  // skoro tutaj dotarliśmy, to znaczy że tranzycja już
			//swoje zrobiła i jej status aktywnej się kończy w tym kroku
			arcs = transition.getOutputArcs();
			// dodaj odpowiednią liczbę tokenów do miejsc
			for (Arc arc : arcs) {
				Place place = (Place)arc.getEndNode();
				if(arc.getArcType() == TypeOfArc.READARC)
					continue;
				
				if(arc.getArcType() != TypeOfArc.NORMAL) {
					//overlord.log("Error: non-standard arc used to produce tokens: "+place.getName()+ " arc: "+ arc, "error", true);
				}
				
				FunctionsTools.functionalAddition(transition, arc, place);
				//place.modifyTokensNumber(arc.getWeight());
			}
			transition.timeExtension.resetTimeVariables();
		}
		launchingTransitions.clear(); //wyczyść listę tranzycji 'do uruchomienia' (już swoje zrobiły)
	}
	
	/**
	 * Metoda zwraca tablicę liczb tokenów w czasie dla miejsc.
	 * @return ArrayList[ArrayList[<b>Integer</b>]] - tablica po symulacji
	 */
	public ArrayList<ArrayList<Integer>> getPlacesData() {
		return placesData;
	}
	
	/**
	 * Metoda zwraca tablicę uruchomień dla tranzycji.
	 * @return ArrayList[ArrayList[<b>Integer</b>]] - tablica po symulacji
	 */
	public ArrayList<ArrayList<Integer>> getTransitionsData() {
		return transitionsData;
	}
	
	/**
	 * Metoda zwraca wektor sumy uruchomień dla tranzycji.
	 * @return ArrayList[<b>Integer</b>] - wektor tranzycji po symulacji
	 */
	public ArrayList<Integer> getTransitionsCompactData() {
		return transitionsTotalFiring;
	}
	
	/**
	 * Metoda zwraca wektor średnich uruchomień tranzycji po wszystkich krokach.
	 * @return ArrayList[<b>Double</b>] - wektor średniej liczby uruchomień tranzycji po symulacji
	 */
	public ArrayList<Double> getTransitionsAvgData() {
		return transitionsAvgData;
	}
	
	/**
	 * Metoda zwraca wektor średniej liczby tokenów w miejsach po wszystkich krokach.
	 * @return ArrayList[<b>Double</b>] - wektor średniej liczby tokenów
	 */
	public ArrayList<Double> getPlacesAvgData() {
		return placesAvgData;
	}
	
	/**
	 * Zwraca wektor zawierający sumę wszystkich tokenów w miejsach po wszystkich krokach symulacji.
	 * @return ArrayList[<b>Long</b>] - wektor sumy tokenów
	 */
	public ArrayList<Long> getPlacesTotalData() {
		return placesTotalData;
	}
	
	//********************************************************************************************************************************
	//****************************************   INTERNAL   **************************************************************************
	//****************************************              **************************************************************************
	//****************************************    BACKUP    **************************************************************************
	//********************************************************************************************************************************
	
	/**
	 * Metoda przygotowuje backup stanu sieci
	 */
	public void prepareNetM0() {
		overlord.getWorkspace().getProject().restoreMarkingZeroFast(transitions);
		saveInternalMarkingZero(); //zapis aktualnego stanu jako m0
		clearTransitionsValues();
	}

	/**
	 * Czyści dane czasowe tranzycji i ustawia każdą na nie-odpalającą.
	 */
	private void clearTransitionsValues() {
		for (Transition transition : transitions) {
			transition.setLaunching(false);
			if (transition.getTransType() == TransitionType.TPN) {
				transition.timeExtension.resetTimeVariables();
			}
			if ( transition instanceof TransitionXTPN ) {
				((TransitionXTPN)transition).resetTimeVariables_xTPN();
			}
		}
	}
	
	/**
	 * Metoda ta zapisuje liczbę tokenów każdego miejsca tworząc kopię zapasową stanu m0.
	 */
	private void saveInternalMarkingZero() {
		internalBackupMarkingZero.clear();
		for (Place place : places) {
			internalBackupMarkingZero.add(place.getTokensNumber());
		}
	}
	
	/**
	 * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji. Liczba tokenów jest przywracana
	 * z wektora danych pamiętających ostatni backup, tranzycje są resetowane wewnętrznie. 
	 */
	public void restoreInternalMarkingZero() {
		for(int i=0; i<places.size(); i++) {
			places.get(i).setTokensNumber(internalBackupMarkingZero.get(i));
			places.get(i).freeReservedTokens();
		}
		clearTransitionsValues();
	}
	
	/**
	 * Metoda tworzy nowy obiekt silnika symulacji.
	 * @param type int - typ:<br>
	 * 		1 - SS (Stochastic Simulation) for SPN<br>
	 * 		2 - Gillespie SSA (Stochastic Simulation Algorith)<br>
	 * 		0 lub każdy inny niz powyższy - standardowy symulator tokenów
	 */
	public void setEngine(int type) {
		readyToSimulate = false;
		if(type == 1) {
			engine = new SPNengine();
			overlord.simSettings.setSimulatorType(1);
		} else if(type == 2) {
			engine = new SSAengine();
			overlord.simSettings.setSimulatorType(2);
		} else {
			engine = new SimulatorStandardPN();
			overlord.simSettings.setSimulatorType(0);
		}
	}
	
	/**
	 * Zwraca obiekt/interface aktualnie ustawionego silnika symulacji.
	 * @return IEngine - interface silnika
	 */
	public IEngine getEngine() {
		return this.engine;
	}
	
	//********************************************************************************************************************************
	//****************************************    	        **************************************************************************
	//****************************************   quickSim   **************************************************************************
	//****************************************              **************************************************************************
	//********************************************************************************************************************************
	
	/**
	 * Metoda używana przez moduł quickSim, zbiera dane o średniej liczbie uruchomień tranzycji oraz tokenach
	 * w miejscach. Powtarza symulacje maksymalnie 20 razy.
	 * @return ArrayList[ArrayList[Double]] - macierz wektorów danych
	 */
	public ArrayList<ArrayList<Double>> quickSimGatherData() {
		if(!readyToSimulate) {
			overlord.log("Simulation simple mode cannot start.", "warning", true);
			return null;
		}
		prepareNetM0(); //backup, m0, etc.
		ArrayList<Transition> launchableTransitions;
		ArrayList<ArrayList<Double>> firingHistory = new ArrayList<>();
		
		ArrayList<ArrayList<Double>> tokensHistory = new ArrayList<>();
		//ArrayList<>
		
		int steps = overlord.simSettings.getSimSteps();
		int reps = overlord.simSettings.getRepetitions();
		if(reps > 20)
			reps = 20;
		boolean emptySteps = overlord.simSettings.isEmptySteps();
		progressBar.setValue(0);
		progressBar.setMaximum(reps*steps);
		int trueStep = 0;
		int updateTime = (reps*steps)/50;
		
		for(int r=0; r<reps; r++) {
			ArrayList<Double> transFiring = new ArrayList<Double>();
			ArrayList<Double> tokensSum = new ArrayList<Double>();
			
			for(int t=0; t<transitions.size(); t++) {
				transFiring.add(0.0);
			}
			for(int p=0; p<places.size(); p++) {
				tokensSum.add(0.0);
			}
			int internalSteps = 0;
			for(int i=0; i<steps; i++) {
				
				trueStep++;
				if(trueStep<0)
					trueStep = Integer.MAX_VALUE;
				
				progressBar.setValue(trueStep);
				if(trueStep % updateTime == 0) {
					progressBar.update(progressBar.getGraphics());
				}
				
				if (isPossibleStep()){ 
					launchableTransitions = engine.getTransLaunchList(emptySteps);
					launchSubtractPhase(launchableTransitions);
					removeDPNtransition(launchableTransitions);
					internalSteps++;
					
					if(launchableTransitions != null) {
						for(Transition trans : launchableTransitions) {
							int index = transitions.lastIndexOf(trans);
							double fired = transFiring.get(index);
							transFiring.set(index, fired+1); //wektor sumy odpaleń
						}
					}
				} else {
					break;
				}
				launchAddPhase(launchableTransitions);
				
				for(int p=0; p<places.size(); p++) {
					double oldVal = tokensSum.get(p);
					tokensSum.set(p, oldVal+places.get(p).getTokensNumber());
				}
			}

			//srednia liczba odpaleń:
			for(int t=0; t<transFiring.size(); t++) {
				double fired = transFiring.get(t);
				fired /= internalSteps;
				transFiring.set(t, fired);
			}
			firingHistory.add(transFiring);
			
			for(int p=0; p<places.size(); p++) {
				double tokens = tokensSum.get(p);
				tokens /= internalSteps;
				tokensSum.set(p, tokens);
			}
			tokensHistory.add(tokensSum);
			
			restoreInternalMarkingZero();
		}
		
		//przygotowanie wektorów wynikowych:
		ArrayList<Double> avgFiring = new ArrayList<Double>();
		ArrayList<Double> stdDev = new ArrayList<Double>();
		ArrayList<Double> avgTokens = new ArrayList<Double>();
		for(int t=0; t<transitions.size(); t++) {
			avgFiring.add(0.0);
		}
		for(int p=0; p<places.size(); p++) {
			avgTokens.add(0.0);
		}
		
		//liczenie średnich:
		for(int t=0; t<transitions.size(); t++) {
			for(int r=0; r<reps; r++) {
				double value = firingHistory.get(r).get(t);
				double oldRes = avgFiring.get(t);
				avgFiring.set(t, oldRes+value);
			}
			double oldRes = avgFiring.get(t);
			oldRes /= reps;
			avgFiring.set(t, oldRes);
		}
		
		for(int p=0; p<places.size(); p++) {
			for(int r=0; r<reps; r++) {
				double value = tokensHistory.get(r).get(p);
				double oldRes = avgTokens.get(p);
				avgTokens.set(p, oldRes+value);
			}
			double oldRes = avgTokens.get(p);
			oldRes /= reps;
			avgTokens.set(p, oldRes);
		}
		
		//stdDev
		for(int t=0; t<transitions.size(); t++) {
			double variance = 0.0;
			for(int r=0; r<reps; r++) {
				double value = avgFiring.get(t); //średnia obliczona krok wcześniej
				double diff = value - firingHistory.get(r).get(t); 
				variance += (diff*diff);
			}
			variance /= reps;
			stdDev.add(Math.sqrt(variance));
		}
		
		readyToSimulate = false;
		restoreInternalMarkingZero();
		quickSimAllStats = new ArrayList<>();
		quickSimAllStats.add(avgFiring);
		quickSimAllStats.add(stdDev);
		quickSimAllStats.addAll(tokensHistory);
		return quickSimAllStats;
	}
	
	/**
	 * Metoda używana przez moduł quickSim, zbiera dane o średniej liczbie uruchomień tranzycji oraz tokenach
	 * w miejscach. Nie powtarza symulacji - 1 przebieg.
	 * @return ArrayList[ArrayList[Double]] - macierz wektorów danych
	 */
	public ArrayList<ArrayList<Double>> quickSimGatherDataNoReps() {
		if(!readyToSimulate) {
			overlord.log("Simulation simple mode cannot start.", "warning", true);
			return null;
		}
		prepareNetM0(); //backup, m0, etc.
		ArrayList<Transition> launchableTransitions;
		
		int steps = overlord.simSettings.getSimSteps();
		boolean emptySteps = overlord.simSettings.isEmptySteps();
		ArrayList<Double> transFiring = new ArrayList<Double>();
		ArrayList<Double> tokensSum = new ArrayList<Double>();
		for(int t=0; t<transitions.size(); t++) {
			transFiring.add(0.0);
		}
		for(int p=0; p<places.size(); p++) {
			tokensSum.add(0.0);
		}
		
		progressBar.setValue(0);
		progressBar.setMaximum(steps);
		int updateTime = (steps)/50;
		
		int internalSteps = 0;
		for(int i=0; i<steps; i++) {
			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(emptySteps);
				launchSubtractPhase(launchableTransitions);
				removeDPNtransition(launchableTransitions);
				internalSteps++;
				
				if(launchableTransitions != null) {
					for(Transition trans : launchableTransitions) {
						int index = transitions.lastIndexOf(trans);
						double fired = transFiring.get(index);
						transFiring.set(index, fired+1); //wektor sumy odpaleń
					}
				}
			} else {
				break;
			}
			launchAddPhase(launchableTransitions);
			for(int p=0; p<places.size(); p++) {
				double oldVal = tokensSum.get(p);
				tokensSum.set(p, oldVal+places.get(p).getTokensNumber());
			}
			
			progressBar.setValue(internalSteps);
			if(internalSteps % updateTime == 0) {
				progressBar.update(progressBar.getGraphics());
			}
		}

		//srednia liczba odpaleń:
		for(int t=0; t<transFiring.size(); t++) {
			double fired = transFiring.get(t);
			fired /= internalSteps;
			transFiring.set(t, fired);
		}
		for(int p=0; p<places.size(); p++) {
			double tokens = tokensSum.get(p);
			tokens /= internalSteps;
			tokensSum.set(p, tokens);
		}
		restoreInternalMarkingZero();
		readyToSimulate = false;
		quickSimAllStats = new ArrayList<>();
		quickSimAllStats.add(transFiring);
		quickSimAllStats.add(null);
		quickSimAllStats.add(tokensSum);
		return quickSimAllStats;
	}

	public ArrayList<ArrayList<Double>> quickSimGatherDataSSA() {
		ArrayList<ArrayList<Double>> result = new ArrayList<>();
		if(!readyToSimulate) {
			overlord.log(lang.getText("LOGentry00415"), "warning", true);
			return null;
		}
		prepareNetM0(); //backup, m0, etc.
		return result;
	}
}
