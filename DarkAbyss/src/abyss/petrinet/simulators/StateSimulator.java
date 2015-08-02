package abyss.petrinet.simulators;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.elements.Arc.TypesOfArcs;
import abyss.petrinet.elements.Transition.TransitionType;
import abyss.petrinet.simulators.NetSimulator.NetType;
import abyss.windows.AbyssStateSimulator;
import abyss.windows.AbyssStateSimulatorKnockout;

/**
 * Klasa symulatora. Różnica między nią a symulatorem graficznym jest taka, że poniższe metody potrafią wygenerować
 * dziesiątki tysiący stanów na sekundę (co sprawia, że oszczędzanie pamięci staje się sine qua non w jej wypadku).
 * 
 * @author MR
 */
public class StateSimulator implements Runnable {
	private ArrayList<Transition> transitions;
	private ArrayList<Transition> time_transitions;
	private ArrayList<Place> places;
	private boolean ready = false;
	public double timeNetStepCounter = 0;
	public double timeNetPartStepCounter = 0;
	
	private ArrayList<ArrayList<Integer>> placesData = null;
	private ArrayList<Double> placesAvgData = null; //średnia liczba tokenów w miejscu
	private ArrayList<ArrayList<Integer>> transitionsData = null;
	private ArrayList<Integer> transitionsTotalFiring = null; //wektor sumy odpaleń tranzycji
	private ArrayList<Double> transitionsAvgData = null;
	private ArrayList<Integer> internalBackupMarkingZero = new ArrayList<Integer>();
	
	private boolean maxMode = false;
	private SimulatorEngine engine = null;
	private int emptySteps = 0; // 0 - bez pustych kroków, 1 - z pustymi krokami
	
	//runtime:
	public boolean terminate = false;
	public int stepsLimit;
	public JProgressBar progressBar;	//standardowy tryb symulacji
	private AbyssStateSimulator boss;	//standardowy tryb symulacji
	private int simulationType;			//standardowy tryb symulacji
	private int refReps;				//powtórki dla trybu zbierania danych referencyjnych
	

	
	/**
	 * Główny konstruktor obiektu klasy StateSimulator.
	 */
	public StateSimulator() {
		//generator = new Random(System.currentTimeMillis());
		engine = new SimulatorEngine();
	}

	/**
	 * Metoda ustawiająca obiekty dla symulacji w osobnym wątku.
	 * @param simulationType int - typ symulacji: 1 - standard; 2 - ref. zbieranie danych
	 * @param blackBox Object... - zależy od trybu powyżej
	 */
	public void setThreadDetails(int simulationType, Object... blackBox) {
		this.simulationType = simulationType;
		
		if(simulationType == 1) { //standardowy tryb symulacji
			this.boss = (AbyssStateSimulator)blackBox[0];
			this.progressBar = (JProgressBar)blackBox[1];
			this.stepsLimit = (int)blackBox[2];
		} else if(simulationType == 2) { //obliczenie zbioru referencyjnego
			this.boss = (AbyssStateSimulator)blackBox[0];
			this.progressBar = (JProgressBar)blackBox[1];
			this.stepsLimit = (int)blackBox[2];
			this.refReps = (int)blackBox[3];
		}
	}

	/**
	 * Uruchamiania zdalnie, zakłada, że wszystko co potrzebne zostało już ustawione za pomocą setThreadDetails(...)
	 */
	public void run() {
		if(simulationType == 1) {
			simulateNetAll();
			boss.completeSimulationProcedures();
		} else if(simulationType == 2) {
			NetSimulationData data = simulateNetRefKnockout();
			boss.accessKnockoutTab().completeRefSimulationResults(data);
		}
	}
	
	/**
	 * Metoda ta musi być wywołana przed każdym startem symulatora. Inicjalizuje początkowe struktury
	 * danych dla symulatora.
	 * @param simNetType NetType - typ sieci
	 * @param maxMode boolean - maximum (true), lub nie
	 * @return boolean - true, jeśli wszystko się udało
	 */
	public boolean initiateSim(NetType simNetType, boolean maxMode) {
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		time_transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTimeTransitions();
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(transitions == null || places == null) {
			ready = false;
			return ready;
		}
		if(!(transitions.size() > 0 && places.size() > 0)) {
			ready = false;
			return ready;
		}
		
		placesData = new ArrayList<ArrayList<Integer>>();
		placesAvgData = new ArrayList<Double>();
		transitionsData = new ArrayList<ArrayList<Integer>>();
		transitionsTotalFiring = new ArrayList<Integer>();
		transitionsAvgData = new ArrayList<Double>();
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
		}
		
		this.maxMode = maxMode;
		engine.setEngine(simNetType, maxMode, transitions, time_transitions);
		ready = true;
		return ready;
	}
	
	/**
	 * Szybsza wersja initiateSim(...), metoda zeruje wektory danych.
	 */
	public void clearData() {
		placesData = new ArrayList<ArrayList<Integer>>();
		placesAvgData = new ArrayList<Double>();
		transitionsData = new ArrayList<ArrayList<Integer>>();
		transitionsTotalFiring = new ArrayList<Integer>();
		transitionsAvgData = new ArrayList<Double>();
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
		}
		ready = true;
	}

	/**
	 * Metoda pracująca w wątku. Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
	 * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
	 */
	public void simulateNetAll() {
		if(ready == false) {
			JOptionPane.showMessageDialog(null,"Simulation cannot start, no network found.", 
					"State Simulation problem",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		prepareNetM0();
		
		ArrayList<Transition> launchingTransitions = null;
		int updateTime = stepsLimit / 50;
		
		String max = "50% firing chance";
		if(maxMode)
			max = "maximum";
		
		GUIManager.getDefaultGUIManager().log("Starting states simulation for "+stepsLimit+" steps in "+max+" mode.", "text", true);
		
		int trueSteps = 0;
		for(int i=0; i<stepsLimit; i++) {
			if(terminate)
				break;
			
			progressBar.setValue(i+1);
			trueSteps++;
			
			if(i % updateTime == 0)
				progressBar.update(progressBar.getGraphics());

			if (isPossibleStep()){ 
				launchingTransitions = engine.getTransLaunchList(emptySteps);
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
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsAvgData.add((double) ((double)transitionsTotalFiring.get(t)/(double)trueSteps));
		}
		for(int p=0; p<places.size(); p++) {
			double sumOfTokens = placesAvgData.get(p);
			placesAvgData.set(p, sumOfTokens/(double)trueSteps);
		}
		GUIManager.getDefaultGUIManager().log("Simulation ended. Restoring zero marking.", "text", true);
		ready = false;
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
	public NetSimulationData simulateNetRefKnockout() {
		if(ready == false) {
			JOptionPane.showMessageDialog(null,"Simulation cannot start, no network found.", "State Simulation problem",JOptionPane.ERROR_MESSAGE);
			return null;
		}
		prepareNetM0();
		int pBarTotal = 0;
		ArrayList<Transition> launchingTransitions = null; //odpalone tranzycje
		
		//	INIT MAIN VECTORS:
		ArrayList<Long> totalPlacesTokensInTurn = new ArrayList<Long>();
		ArrayList<Integer> totalTransFiringInTurn = new ArrayList<Integer>();
		NetSimulationData netData = new NetSimulationData();
		int placeNumber = places.size();
		int transNumber = transitions.size();
		netData.maxMode = engine.getMaxMode();
		netData.netSimType = engine.getNetSimMode();
		netData.steps = stepsLimit;
		netData.reps = refReps;
		netData.placesNumber = placeNumber;
		netData.transNumber = transNumber;
		for(int p=0; p<placeNumber; p++) {
			totalPlacesTokensInTurn.add((long) 0);
			netData.refPlaceTokensAvg.add(0.0);
			netData.refPlaceTokensMin.add(Double.MAX_VALUE);
			netData.refPlaceTokensMax.add(0.0);
			netData.simsWithZeroTokens.add(0);
		}
		for(int t=0; t<transNumber; t++) {
			totalTransFiringInTurn.add(0);
			netData.refTransFiringsAvg.add(0.0);
			netData.refTransFiringsMin.add(Double.MAX_VALUE);
			netData.refTransFiringsMax.add(0.0);
			netData.simsWithZeroFiring.add(0);
		}
		//	INIT VECTORS COMPLETED
		
		int pBarInterval = (refReps*stepsLimit) / 100;
		int pBarStep = 0;
		progressBar.setMaximum(refReps*stepsLimit);
		progressBar.setMinimum(0);
		progressBar.setValue(0);
		
		for(int turn=0; turn<refReps; turn++) {
			int realStepCounter = 0;
			for(int p=0; p<placeNumber; p++)
				totalPlacesTokensInTurn.set(p, (long) 0);
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
					long val = totalPlacesTokensInTurn.get(p) + places.get(p).getTokensNumber();
					totalPlacesTokensInTurn.set(p, val);
				}
			}
			
			//uśrednianie dla skończonej (jednej) symulacji:
			for(int p=0; p<placeNumber; p++) {
				double simTokenValue = totalPlacesTokensInTurn.get(p);
				simTokenValue /= (double)realStepCounter;
				
				double oldVal = netData.refPlaceTokensAvg.get(p) + simTokenValue;
				netData.refPlaceTokensAvg.set(p, oldVal);
				
				double oldMin = netData.refPlaceTokensMin.get(p);
				if(simTokenValue < oldMin)
					netData.refPlaceTokensMin.set(p, (double) simTokenValue);
				
				double oldMax = netData.refPlaceTokensMax.get(p);
				if(simTokenValue > oldMax) {
					netData.refPlaceTokensMax.set(p, (double) simTokenValue);
				}
				
				if(simTokenValue == 0) {
					int val = netData.simsWithZeroTokens.get(p) + 1;
					netData.simsWithZeroTokens.set(p, val);
				}
			}
			for(int t=0; t<transNumber; t++) {
				double simFiringValue = totalTransFiringInTurn.get(t);
				simFiringValue /= (double)realStepCounter;
				
				double oldVal = netData.refTransFiringsAvg.get(t) + simFiringValue;
				netData.refTransFiringsAvg.set(t, oldVal);
				
				double oldMin = netData.refTransFiringsMin.get(t);
				if(simFiringValue < oldMin)
					netData.refTransFiringsMin.set(t, (double) simFiringValue);
				
				double oldMax = netData.refTransFiringsMax.get(t);
				if(simFiringValue > oldMax)
					netData.refTransFiringsMax.set(t, (double) simFiringValue);

				if(simFiringValue == 0) {
					int val = netData.simsWithZeroFiring.get(t) + 1;
					netData.simsWithZeroFiring.set(t, val);
				}
			}
			
			restoreInternalMarkingZero();
		} //kolejna powtórka
		
		//uśrednianie po zakończeniu powtórek:
		for(int p=0; p<placeNumber; p++) {
			double oldVal = netData.refPlaceTokensAvg.get(p) / (double)refReps;
			netData.refPlaceTokensAvg.set(p, oldVal);
			
		}
		for(int t=0; t<transNumber; t++) {
			double oldVal = netData.refTransFiringsAvg.get(t) / (double)refReps;
			netData.refTransFiringsAvg.set(t, oldVal);
		}

		ready = false;
		restoreInternalMarkingZero();
		
		return netData;
	}

	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
	 * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
	 * Od simulate różni się tym, że nie zbiera historii odpaleń i tokenów dla miejsc, tylko
	 * średnie wartości. Dzięki temu działa szybciej i nie zabiera tyle miejsca w pamięci.
	 * @param steps int - liczba kroków do symulacji
	 * @param placesToo boolean - true, jeśli ma gromadzić też dane dla miejsc
	 * @return int - liczba rzeczywiście wykonanych kroków
	 */
	public int simulateNetSimple(int steps, boolean placesToo) {
		if(ready == false) {
			GUIManager.getDefaultGUIManager().log("Simulation simple mode cannot start.", "warning", true);
			return 0;
		}
		prepareNetM0(); //backup, m0, etc.
		ArrayList<Transition> launchableTransitions = null;
		int internalSteps = 0;
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				launchableTransitions = engine.getTransLaunchList(emptySteps);
				launchSubtractPhase(launchableTransitions);
				removeDPNtransition(launchableTransitions);
				
				for(Transition trans : launchableTransitions) {
					int index = transitions.lastIndexOf(trans);
					int fired = transitionsTotalFiring.get(index);
					transitionsTotalFiring.set(index, fired+1); //wektor sumy odpaleń
				}
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
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsAvgData.add((double) ((double)transitionsTotalFiring.get(t)/(double)internalSteps));
		}
		
		if(placesToo == true)
			for(int p=0; p<places.size(); p++) {
				double sumOfTokens = placesAvgData.get(p);
				placesAvgData.set(p, sumOfTokens/(double)internalSteps);
			}
	
		ready = false;
		restoreInternalMarkingZero();
		return internalSteps;
	}
	
	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, dla konkretnego
	 * miejsca. Dane dla wybranego miejsca funkcja zwraca jako ArrayList[Integer].
	 * @param steps int - liczba kroków do symulacji
	 * @param plc Place - wybrane miejsce do testowania
	 * @return ArrayList[Integer] - wektor danych o tokenach w miejscu
	 */
	public ArrayList<Integer> simulateNetSinglePlace(int steps, Place place) {
		if(ready == false) {
			GUIManager.getDefaultGUIManager().log("Simulation for place "+place.getName()+" cannot start.", "warning", true);
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
		ready = false;
		restoreInternalMarkingZero();
		return placeDataVector;
	}
	
	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, dla konkretnej
	 * tranzycji. Dane dla wybranej tranzycji funkcja zwraca jako ArrayList[Integer].
	 * @param steps int - liczba kroków do symulacji
	 * @param trans Transition - wybrana tranzycja do testowania
	 * @return ArrayList[Integer] - wektor danych o odpalaniu tranzycji
	 */
	public ArrayList<Integer> simulateNetSingleTransition(int steps, Transition trans) {
		if(ready == false) {
			GUIManager.getDefaultGUIManager().log("Simulation for transition "+trans.getName()+" cannot start.", "warning", true);
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
				launchSubtractPhase(launchableTransitions); //zabierz tokeny poprzez aktywne tranzycje
				removeDPNtransition(launchableTransitions);
			} else {
				break;
			}
			
			if(launchableTransitions.contains(trans)) {
				transDataVector.add(1);
				sum++;
			} else {
				transDataVector.add(0);
			}
			launchAddPhase(launchableTransitions);
		}
		ready = false;
		transDataVector.add(sum);
		transDataVector.add(internalSteps);
		restoreInternalMarkingZero();
		return transDataVector;
	}
	
	//********************************************************************************************************************************
	//****************************************              **************************************************************************
	//****************************************   INTERNALS  **************************************************************************
	//****************************************              **************************************************************************
	//********************************************************************************************************************************
	
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
					place.modifyTokensNumber(-arc.getWeight());
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
					GUIManager.getDefaultGUIManager().log("Error: non-standard arc used to produce tokens: "+place.getName()+ 
							" arc: "+arc.toString(), "error", true);
				}
				place.modifyTokensNumber(arc.getWeight());
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
	
	//********************************************************************************************************************************
	//****************************************   INTERNAL   **************************************************************************
	//****************************************              **************************************************************************
	//****************************************    BACKUP    **************************************************************************
	//********************************************************************************************************************************
	
	/**
	 * Metoda przygotowuje backup stanu sieci
	 */
	public void prepareNetM0() {
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		}
		saveInternalMarkingZero(); //zapis aktualnego stanu jako m0
		clearTransitionsValues();
		
		//mainSimMaximumMode = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().isMaximumMode();
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
	 * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji. Liczba tokenów jest przywracana
	 * z wektora danych pamiętających ostatni backup, tranzycje są resetowane wewnętrznie. 
	 */
	public void restoreInternalMarkingZero() {
		//GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().setMaximumMode(mainSimMaximumMode);
		for(int i=0; i<places.size(); i++) {
			places.get(i).setTokensNumber(internalBackupMarkingZero.get(i));
			places.get(i).freeReservedTokens();
		}
		clearTransitionsValues();
	}
}
