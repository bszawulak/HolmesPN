package holmes.petrinet.simulators;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypesOfArcs;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.functions.FunctionsTools;
import holmes.windows.ssim.HolmesSim;

/**
 * Klasa symulatora. Różnica między nią a symulatorem graficznym jest taka, że poniższe metody potrafią wygenerować
 * dziesiątki tysiący stanów na sekundę (co sprawia, że oszczędzanie pamięci staje się sine qua non w jej wypadku).
 * 
 * @author MR
 */
public class StateSimulator implements Runnable {
	private GUIManager overlord;
	private ArrayList<Transition> transitions;
	private ArrayList<Transition> time_transitions;
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

	/**
	 * Główny konstruktor obiektu klasy StateSimulator.
	 */
	public StateSimulator() {
		engine = new StandardTokenSimulator();
		overlord = GUIManager.getDefaultGUIManager();
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
		time_transitions = overlord.getWorkspace().getProject().getTimeTransitions();
		places = overlord.getWorkspace().getProject().getPlaces();
		if(transitions == null || places == null) {
			readyToSimulate = false;
			return readyToSimulate;
		}
		if(!(transitions.size() > 0 && places.size() > 0)) {
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
				if(!(engine instanceof StandardTokenSimulator)) {
					engine = new StandardTokenSimulator();
				}
			} else if (engineType == 1) {
				if(!(engine instanceof SSAengine)) {
					engine = new SSAengine();
				}
			} else { //domyślnie standardowy silnik
				if(!(engine instanceof StandardTokenSimulator)) {
					engine = new StandardTokenSimulator();
				}
			}
			
		} else { //domyślny, prosty tryb
			if(!(engine instanceof StandardTokenSimulator)) {
				engine = new StandardTokenSimulator();
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
		if(readyToSimulate == false) {
			JOptionPane.showMessageDialog(null,"Simulation cannot start, engine initialization failed.", 
					"Simulation problem",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		prepareNetM0();
		
		ArrayList<Transition> launchingTransitions = null;
		int stepsLimit = overlord.simSettings.getSimSteps();
		int updateTime = stepsLimit / 50;
		
		String max = "50% firing chance";
		if(overlord.simSettings.isMaxMode())
			max = "maximum";
		
		overlord.log("Starting states simulation for "+stepsLimit+" steps in "+max+" mode.", "text", true);
		
		int trueSteps = 0;
		for(int i=0; i<stepsLimit; i++) {
			if(terminate)
				break;
			
			progressBar.setValue(i+1);
			trueSteps++;
			
			if(i % updateTime == 0)
				progressBar.update(progressBar.getGraphics());

			if (isPossibleStep()){ 
				launchingTransitions = engine.getTransLaunchList(overlord.simSettings.isEmptySteps());
				launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
				removeDPNtransition(launchingTransitions);
				
				ArrayList<Integer> transRow = new ArrayList<Integer>();
				for(int t=0; t<transitions.size(); t++)
					transRow.add(0);
				
				for(Transition trans : launchingTransitions) {
					int index = transitions.lastIndexOf(trans);
					transRow.set(index, 1); //dodaj tylko tranzycjom, które odpaliły
					
					int fired = transitionsTotalFiring.get(index);
					transitionsTotalFiring.set(index, fired+1); //wektor sumy odpaleń
				}
				transitionsData.add(transRow);
			} else {
				break;
			}
			launchAddPhase(launchingTransitions);
			
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
			transitionsAvgData.add((double) ((double)transitionsTotalFiring.get(t)/(double)trueSteps));
		}
		for(int p=0; p<places.size(); p++) {
			double sumOfTokens = placesAvgData.get(p);
			placesAvgData.set(p, sumOfTokens/(double)trueSteps);
		}
		overlord.log("Simulation ended. Restoring zero marking.", "text", true);
		readyToSimulate = false;
		restoreInternalMarkingZero();
	}
	
	//TODO:
	/**
	 * Uniwersalna metoda pracująca w osobnym wątku, zbierająca dane o symulacji sieci przez zadaną liczbe kroków
	 * oraz przez ustaloną liczbę powtórek symulacji. Zwraca obiekt klasy kontenerowej NetSimulationData. Może być
	 * to zarówno pakiet danych referencyjnych, jak i pakiet danych dla wyłączonych odpowiednich tranzycji. Oczywiście
	 * musi być to wszystko ustawione przed jej uruchomieniem.
	 * @return NetSimulationData - pakiet danych z powtórzonych symulacji.
	 */
	public NetSimulationData simulateNetReferenceAndKnockout() {
		if(readyToSimulate == false) {
			JOptionPane.showMessageDialog(null,"Simulation cannot start, engine initialization failed.", 
					"Simulation problem",JOptionPane.ERROR_MESSAGE);
			return null;
		}
		prepareNetM0();
		int pBarTotal = 0;
		ArrayList<Transition> launchingTransitions = null; //odpalone tranzycje
		
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
			
			currentDataPackage.startingState = overlord.getWorkspace().getProject().accessStatesManager().getCurrentState();
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
					
					for(Transition trans : launchingTransitions) {
						int index = transitions.lastIndexOf(trans);
						int val = totalTransFiringInTurn.get(index) + 1;
						totalTransFiringInTurn.set(index, val);
					}
					
					launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
					removeDPNtransition(launchingTransitions);
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
				simTokenValue /= (double)realStepCounter;
				histVector.add(simTokenValue);
				
				double oldVal = currentDataPackage.placeTokensAvg.get(p) + simTokenValue;
				currentDataPackage.placeTokensAvg.set(p, oldVal);
				
				double oldMin = currentDataPackage.placeTokensMin.get(p);
				if(simTokenValue < oldMin)
					currentDataPackage.placeTokensMin.set(p, (double) simTokenValue);
				
				double oldMax = currentDataPackage.placeTokensMax.get(p);
				if(simTokenValue > oldMax) {
					currentDataPackage.placeTokensMax.set(p, (double) simTokenValue);
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
				simFiringValue /= (double)realStepCounter;
				histVector.add(simFiringValue);
				
				double oldVal = currentDataPackage.transFiringsAvg.get(t) + simFiringValue;
				currentDataPackage.transFiringsAvg.set(t, oldVal);
				
				double oldMin = currentDataPackage.transFiringsMin.get(t);
				if(simFiringValue < oldMin)
					currentDataPackage.transFiringsMin.set(t, (double) simFiringValue);
				
				double oldMax = currentDataPackage.transFiringsMax.get(t);
				if(simFiringValue > oldMax)
					currentDataPackage.transFiringsMax.set(t, (double) simFiringValue);

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
		double variance = 0;
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
	 * @param steps int - liczba kroków do symulacji
	 * @param placesToo boolean - true, jeśli ma gromadzić też dane dla miejsc
	 * @param emptySteps boolean - true, jeśli dozwolone puste przebiegi
	 * @return int - liczba rzeczywiście wykonanych kroków
	 */
	public int simulateNetSimple(int steps, boolean placesToo, boolean emptySteps) {
		if(readyToSimulate == false) {
			overlord.log("Simulation simple mode cannot start.", "warning", true);
			return 0;
		}
		prepareNetM0(); //backup, m0, etc.
		ArrayList<Transition> launchableTransitions = null;
		int internalSteps = 0;
		//boolean emptySteps = overlord.simSettings.isEmptySteps();
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(emptySteps);
				
				for(Transition trans : launchableTransitions) {
					int index = transitions.lastIndexOf(trans);
					int fired = transitionsTotalFiring.get(index);
					transitionsTotalFiring.set(index, fired+1); //wektor sumy odpaleń
				}
				
				launchSubtractPhase(launchableTransitions);
				removeDPNtransition(launchableTransitions);
			} else {
				break;
			}
			launchAddPhase(launchableTransitions);
			
			if(placesToo == true)
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
			transitionsAvgData.add((double) ((double)transitionsTotalFiring.get(t)/(double)internalSteps));
		}
		
		if(placesToo == true)
			for(int p=0; p<places.size(); p++) {
				double sumOfTokens = placesAvgData.get(p);
				placesAvgData.set(p, (double)(sumOfTokens/(double)internalSteps));
			}
	
		readyToSimulate = false;
		restoreInternalMarkingZero();
		return internalSteps;
	}
	
	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, dla konkretnego
	 * miejsca. Dane dla wybranego miejsca funkcja zwraca jako ArrayList[Integer].
	 * @param steps int - liczba kroków do symulacji
	 * @param place Place - wybrane miejsce do testowania
	 * @param emptySteps boolean - true, jeśli dozwolone puste przebiegi
	 * @return ArrayList[Integer] - wektor danych o tokenach w miejscu
	 */
	public ArrayList<Integer> simulateNetSinglePlace(int steps, Place place, boolean emptySteps) {
		if(readyToSimulate == false) {
			overlord.log("Simulation for place "+place.getName()+" cannot start.", "warning", true);
			return null;
		}
		prepareNetM0();

		ArrayList<Integer> placeDataVector = new ArrayList<Integer>();
		//int internalSteps = 0;
		ArrayList<Transition> launchableTransitions = null;
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
	 * @param steps int - liczba kroków do symulacji
	 * @param trans Transition - wybrana tranzycja do testowania
	 * @param emptySteps boolean - true, jeśli dozwolone kroki bez odpalonych tranzycji
	 * @return ArrayList[Integer] - wektor danych o odpalaniu tranzycji
	 */
	public ArrayList<Integer> simulateNetSingleTransition(int steps, Transition trans, boolean emptySteps) {
		if(readyToSimulate == false) {
			overlord.log("Simulation for transition "+trans.getName()+" cannot start.", "warning", true);
			return null;
		}
		prepareNetM0();
		ArrayList<Transition> launchableTransitions = null;
		int sum = 0;
		int internalSteps = 0;
		ArrayList<Integer> transDataVector = new ArrayList<Integer>();
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(emptySteps);
				
				if(launchableTransitions.contains(trans)) {
					transDataVector.add(1);
					sum++;
				} else {
					transDataVector.add(0);
				}
				
				launchSubtractPhase(launchableTransitions); //zabierz tokeny poprzez aktywne tranzycje
				removeDPNtransition(launchableTransitions);
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
	 * @param steps int - ile kroków symulacji
	 * @param reps int - ile powtórek
	 * @param emptySteps boolean - true, jeśli dozwolone kroki bez odpalania tranzycji
	 * @return ArrayList[ArrayList[Double]] - dwa wektory danych, pierwszy to średnie, drugi - odchylenie standardowe
	 */
	public ArrayList<ArrayList<Double>> simulateForInvariantTrans(int steps, int reps, boolean emptySteps) {
		ArrayList<ArrayList<Double>> result = new ArrayList<>();
		if(readyToSimulate == false) {
			overlord.log("Simulation simple mode cannot start.", "warning", true);
			return null;
		}
		prepareNetM0(); //backup, m0, etc.
		ArrayList<Transition> launchableTransitions = null;
		
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
					
					for(Transition trans : launchableTransitions) {
						int index = transitions.lastIndexOf(trans);
						double fired = transFiring.get(index);
						transFiring.set(index, fired+1); //wektor sumy odpaleń
					}
					
					launchSubtractPhase(launchableTransitions);
					removeDPNtransition(launchableTransitions);
					internalSteps++;
				} else {
					break;
				}
				launchAddPhase(launchableTransitions);
			}

			//srednia liczba odpaleń:
			for(int t=0; t<transFiring.size(); t++) {
				double fired = transFiring.get(t);
				fired /= (double)internalSteps;
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
			oldRes /= (double)reps;
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
			variance /= (double)reps;
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
	 * @param val boolean - true, jeśli symulator ma zakończyć działanie
	 */
	public void setCancelStatus(boolean val) {
		this.terminate = val;
		if(val == true)
			readyToSimulate = false;
	}
	
	/**
	 * Zwraca flagę wymuszonego kończenia symulacji.
	 * @return boolean - true, jeśli symulator został awaryjnie wyłączony
	 */
	public boolean getCancelStatus() {
		return this.terminate;
	}
	
	/**
	 * Metoda sprawdza, czy tranzycja DPN jest w fazie liczenia wewnętrznego zegara aż do punktu
	 * określonego zmienną duration. Czyli jeśli timer to 0, to tranzycja liczy. Jeśli w takim wypadku
	 * duration > 0, wtedy ją usuwamy z listy launchingTransitions (tokeny już odjęto z miejsc, dodanie
	 * nastąpi kilka kroków później). Jeśli timer = duration = 0, tranzycja zostaje na liście.
	 * @param launchingTransitions ArrayList[Integer] - lista tranzycji odpalających
	 */
	private void removeDPNtransition(ArrayList<Transition> launchingTransitions) {
		for(int t=0; t<launchingTransitions.size(); t++) {
			Transition test_t = launchingTransitions.get(t);
			if(test_t.getDPNstatus()) {
				if(test_t.getDPNtimer() == 0 && test_t.getDPNduration() != 0) {
					launchingTransitions.remove(test_t);
					t--;
				}
			}
		}
	}
	
	/**
	 * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych do odpalonych tranzycji
	 * @param transitions ArrayList[Transition] - lista uruchamianych tranzycji
	 */
	private void launchSubtractPhase(ArrayList<Transition> transitions) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			if(transition.getDPNtimer() > 0) //yeah, trust me, I'm an engineer
				continue;
			//innymi słowy: nie odejmuj tokenów, jeśli timer DPN to 1, 2 lub więcej. Odejmuj gdy = 0, a gdy jest
			//równy -1, to w ogóle nie będzie takiej tranzycji na liście transitions tutaj.
			
			transition.setLaunching(true);
			arcs = transition.getInArcs();
			for (Arc arc : arcs) {
				Place place = (Place)arc.getStartNode();
				
				if(arc.getArcType() == TypesOfArcs.INHIBITOR) {
					// nic nie zabieraj
				} else if(arc.getArcType() == TypesOfArcs.READARC) {
					// nic nie zabieraj
				} else if(arc.getArcType() == TypesOfArcs.RESET) {
					int tokens = place.getTokensNumber();
					place.modifyTokensNumber(-tokens);
				} else if(arc.getArcType() == TypesOfArcs.EQUAL) {
					place.modifyTokensNumber(-arc.getWeight());
				} else {
					FunctionsTools.functionalExtraction(transition, arc, place);
					//place.modifyTokensNumber(-arc.getWeight());
				}
			}
		}
	}
	
	/**
	 * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna tranzycja.
	 * @return boolean - true jeśli jest choć jedna aktywna tranzycja; false w przeciwnym wypadku
	 */
	private boolean isPossibleStep() {
		for (Transition transition : transitions) {
			if (transition.isActive())
				return true;
		}
		return false;
	}
	
	/**
	 * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych z odpalonych tranzycji. 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 */
	private void launchAddPhase(ArrayList<Transition> transitions) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			transition.setLaunching(false);  // skoro tutaj dotarliśmy, to znaczy że tranzycja już
			//swoje zrobiła i jej status aktywnej się kończy w tym kroku
			arcs = transition.getOutArcs();
			// dodaj odpowiednią liczbę tokenów do miejsc
			for (Arc arc : arcs) {
				Place place = (Place)arc.getEndNode();
				if(arc.getArcType() == TypesOfArcs.READARC)
					continue;
				
				if(arc.getArcType() != TypesOfArcs.NORMAL) {
					overlord.log("Error: non-standard arc used to produce tokens: "+place.getName()+ 
							" arc: "+arc.toString(), "error", true);
				}
				
				FunctionsTools.functionalAddition(transition, arc, place);
				//place.modifyTokensNumber(arc.getWeight());
			}
			transition.resetTimeVariables();
		}
		transitions.clear(); //wyczyść listę tranzycji 'do uruchomienia' (już swoje zrobiły)
	}
	
	/**
	 * Metoda zwraca tablicę liczb tokenów w czasie dla miejsc.
	 * @return ArrayList[ArrayList[Integer]] - tablica po symulacji
	 */
	public ArrayList<ArrayList<Integer>> getPlacesData() {
		return placesData;
	}
	
	/**
	 * Metoda zwraca tablicę uruchomień dla tranzycji.
	 * @return ArrayList[ArrayList[Integer]] - tablica po symulacji
	 */
	public ArrayList<ArrayList<Integer>> getTransitionsData() {
		return transitionsData;
	}
	
	/**
	 * Metoda zwraca wektor sumy uruchomień dla tranzycji.
	 * @return ArrayList[Integer] - wektor tranzycji po symulacji
	 */
	public ArrayList<Integer> getTransitionsCompactData() {
		return transitionsTotalFiring;
	}
	
	/**
	 * Metoda zwraca wektor średnich uruchomień tranzycji po wszystkich krokach.
	 * @return ArrayList[Double] - wektor średniej liczby uruchomień tranzycji po symulacji
	 */
	public ArrayList<Double> getTransitionsAvgData() {
		return transitionsAvgData;
	}
	
	/**
	 * Metoda zwraca wektor średniej liczby tokenów w miejsach po wszystkich krokach.
	 * @return ArrayList[Double] - wektor średniej liczby tokenów
	 */
	public ArrayList<Double> getPlacesAvgData() {
		return placesAvgData;
	}
	
	/**
	 * Zwraca wektor zawierający sumę wszystkich tokenów w miejsach po wszystkich krokach symulacji.
	 * @return ArrayList[Long] - wektor sumy tokenów
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
		for(int i=0; i<transitions.size(); i++) {
			transitions.get(i).setLaunching(false);
			if(transitions.get(i).getTransType() == TransitionType.TPN) {
				transitions.get(i).resetTimeVariables();
			}
		}
	}
	
	/**
	 * Metoda ta zapisuje liczbę tokenów każdego miejsca tworząc kopię zapasową stanu m0.
	 */
	private void saveInternalMarkingZero() {
		internalBackupMarkingZero.clear();
		for(int i=0; i<places.size(); i++) {
			internalBackupMarkingZero.add(places.get(i).getTokensNumber());
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
	 * 		1 - SSA<br>
	 * 		2 - Gillespie SSA<br>
	 * 		0 lub każdy inny niz powyższy - standardowy symulator tokenów
	 */
	public void setEngine(int type) {
		readyToSimulate = false;
		if(type == 1) {
			engine = new SSAengine();
			overlord.simSettings.setSimulatorType(1);
		} else {
			engine = new StandardTokenSimulator();
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
}
