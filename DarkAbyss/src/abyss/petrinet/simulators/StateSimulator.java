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

/**
 * Klasa symulująca główny symulator programu :) Różnica jest taka, że poniższe metody potrafią wygenerować
 * tysiące stanów na sekundę (raczej dziesiątki tysięcy).
 * @author MR
 *
 */
public class StateSimulator {
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
	
	private boolean maximumMode = false;
	//private boolean mainSimMaximumMode = false;
	//private Random generator; // = new Random(System.currentTimeMillis());
	
	private SimulatorEngine engine = null;
	
	private int modeSteps = 0; // 0 - bez pustych kroków, 1 - z pustymi krokami
	
	/**
	 * Główny konstruktor obiektu klasy StateSimulator.
	 */
	public StateSimulator() {
		//generator = new Random(System.currentTimeMillis());
		engine = new SimulatorEngine();
	}
	
	/**
	 * Metoda ta musi być wywołana przed każdym startem symulatora. Inicjalizuje początkowe struktury
	 * danych dla symulatora.
	 * @param netT NetType - typ sieci
	 * @param mode boolean - maximum (true), lub nie
	 * @return boolean - true, jeśli wszystko się udało
	 */
	public boolean initiateSim(NetType netT, boolean mode) {
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
		
		maximumMode = mode;
		//generator = new Random(System.currentTimeMillis());
		engine.setEngine(netT, mode, transitions, time_transitions);
		
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
		//allTransitionsIndicesList = new ArrayList<Integer>();
		//timeTransIndicesList = new ArrayList<Integer>();
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
			//allTransitionsIndicesList.add(t);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
		}
		//for(int i=0; i<time_transitions.size(); i++) {
		//	timeTransIndicesList.add(i);
		//}
		//generator = new Random(System.currentTimeMillis());
		ready = true;
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
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
	 * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
	 * @param steps int - liczba kroków do symulacji
	 * @param pBar JProgressBar - pasek postępu
	 */
	public void simulateNet(int steps, JProgressBar pBar) {
		if(ready == false) {
			JOptionPane.showMessageDialog(null,"Simulation cannot start, no network found.", 
					"State Simulation problem",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		prepareNetM0();
		
		ArrayList<Transition> launchingTransitions = null;
		int updateTime = steps / 100;
		
		String max = "50% firing chance";
		if(maximumMode)
			max = "maximum";
		GUIManager.getDefaultGUIManager().log("Starting states simulation for "+steps+" steps in "+max+" mode.", "text", true);
		for(int i=0; i<steps; i++) {
			pBar.setValue(i+1);
			
			if(i % updateTime == 0)
				pBar.update(pBar.getGraphics()); // (╯°□°）╯︵ ┻━┻)  
			
			if (isPossibleStep()){ 
				launchingTransitions = engine.getTransLaunchList(modeSteps);
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
			launchAddPhase(launchingTransitions, false);
			
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
			transitionsAvgData.add((double) ((double)transitionsTotalFiring.get(t)/(double)steps));
		}
		for(int p=0; p<places.size(); p++) {
			double sumOfTokens = placesAvgData.get(p);
			placesAvgData.set(p, sumOfTokens/(double)steps);
		}
		GUIManager.getDefaultGUIManager().log("Simulation ended. Restoring zero marking.", "text", true);
		ready = false;
		
		restoreInternalMarkingZero();
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
				launchableTransitions = engine.getTransLaunchList(modeSteps);
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
			launchAddPhase(launchableTransitions, false);
			
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
				launchableTransitions = engine.getTransLaunchList(modeSteps); //wypełnia launchableTransitions
				launchSubtractPhase(launchableTransitions);
				removeDPNtransition(launchableTransitions);
			} else {
				break;
			}
			launchAddPhase(launchableTransitions, false);
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
				launchableTransitions = engine.getTransLaunchList(modeSteps);
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
			launchAddPhase(launchableTransitions, false);
		}
		ready = false;
		transDataVector.add(sum);
		transDataVector.add(internalSteps);
		restoreInternalMarkingZero();
		return transDataVector;
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
	 * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych z odpalonych
	 * tranzycji. 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 */
	private void launchAddPhase(ArrayList<Transition> transitions, boolean backtracking) {
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
	
	//******************************************************************************************************
	//****************************************   INTERNAL   ************************************************
	//****************************************              ************************************************
	//****************************************    BACKUP    ************************************************
	//******************************************************************************************************
	
	/**
	 * Metoda przygotowuje backup stanu sieci
	 */
	public void prepareNetM0() {
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		}
		saveInternalMarkingZero(); //zapis aktualnego stanu jako m0
		
		//mainSimMaximumMode = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().isMaximumMode();
	}
	
	/**
	 * Metoda ta zapisuje liczbę tokenów każdego miejsca tworząc kopię zapasową stanu m0.
	 */
	public void saveInternalMarkingZero() {
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
		//GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().setMaximumMode(mainSimMaximumMode);
		
		for(int i=0; i<places.size(); i++) {
			places.get(i).setTokensNumber(internalBackupMarkingZero.get(i));
			places.get(i).freeReservedTokens();
		}
		
		for(int i=0; i<transitions.size(); i++) {
			transitions.get(i).setLaunching(false);
			if(transitions.get(i).getTransType() == TransitionType.TPN) {
				transitions.get(i).resetTimeVariables();
			}
		}
	}
}
