package abyss.math.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator.NetType;

/**
 * Klasa symulująca główny symulator programu :) Różnica jest taka, że poniższe metody potrafią wygenerować
 * tysiące stanów na sekundę (raczej dziesiątki tysięcy).
 * @author MR
 *
 */
public class StateSimulator {
	private ArrayList<Transition> transitions;
	private ArrayList<TimeTransition> ttransitions;
	private ArrayList<Place> places;
	private boolean ready = false;
	private NetType simulationType;
	private boolean maximumMode = false;
	public double timeNetStepCounter = 0;
	public double timeNetPartStepCounter = 0;
	
	private ArrayList<ArrayList<Integer>> placesData = null;
	private ArrayList<Double> placesAvgData = null; //średnia liczba tokenów w miejscu
	private ArrayList<ArrayList<Integer>> transitionsData = null;
	private ArrayList<Integer> transitionsTotalFiring = null; //wektor sumy odpaleń tranzycji
	private ArrayList<Double> transitionsAvgData = null;
	private ArrayList<Integer> indexList = null;
	private ArrayList<Integer> indexTTList = null;
	
	private ArrayList<Integer> internalBackupMarkingZero = new ArrayList<Integer>();
	private boolean mainSimMaximumMode = false;
	
	ArrayList<Transition> launchableTransitions = new ArrayList<Transition>();
	
	/**
	 * Główny konstruktor obiektu klasy StateSimulator.
	 */
	public StateSimulator() {
		
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
		ttransitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTimeTransitions();
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
		indexList = new ArrayList<Integer>();
		indexTTList = new ArrayList<Integer>();
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
			indexList.add(t);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
		}
		for(int i=0; i<ttransitions.size(); i++) {
			indexTTList.add(i);
		}
		
		simulationType = netT;
		maximumMode = mode;
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
		indexList = new ArrayList<Integer>();
		indexTTList = new ArrayList<Integer>();
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
			indexList.add(t);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
		}
		for(int i=0; i<ttransitions.size(); i++) {
			indexTTList.add(i);
		}
		ready = true;
	}
	
	/**
	 * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna
	 * tranzycja.
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
	 * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
	 */
	private ArrayList<Transition> generateValidLaunchingTransitions() {
		boolean generated = false;
		//ArrayList<Transition> launchingTransitions = new ArrayList<Transition>();
		int safetyCounter = 0;
		while (!generated) {
			//launchingTransitions = generateLaunchingTransitions();
			generateLaunchingTransitions();
			//if (launchingTransitions.size() > 0) { //launchableTransitions
			if (launchableTransitions.size() > 0) {
				generated = true; 
			} else {
				if (simulationType == NetType.TIME || simulationType == NetType.HYBRID) {
					//return launchingTransitions; //koniec symulacji
					return launchableTransitions; 
				}
				
				safetyCounter++;
				if(safetyCounter == 999) { // safety measure
					if(isPossibleStep() == false) {
						GUIManager.getDefaultGUIManager().log("Error, no active transition, yet generateValidLaunchingTransitions "
								+ "has been activated. Please advise authors.", "error", true);
						//return launchingTransitions;
						return launchableTransitions; 
					}
				}
			}
		}
		//return launchingTransitions;
		return launchableTransitions; 
	}

	/**
	 * Metoda pomocnicza dla generateValidLaunchingTransitions(), odpowiedzialna za sprawdzenie
	 * które tranzycje nadają się do uruchomienia. Aktualnie działa dla modelu klasycznego PN
	 * oraz czasowego.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia.
	 */
	private ArrayList<Transition> generateLaunchingTransitions() {
		Random randomLaunch = new Random();
		//ArrayList<Transition> launchableTransitions = new ArrayList<Transition>();

		launchableTransitions.clear();
		if (simulationType == NetType.BASIC) {
			Collections.shuffle(indexList);

			for (int i = 0; i < transitions.size(); i++) {
				Transition transition = transitions.get(indexList.get(i));
				if (transition.isActive() )
					if ((randomLaunch.nextInt(10) < 5) || maximumMode) { // 50% 0-4 / 5-9
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
			}
		} else if (simulationType == NetType.HYBRID) { 
			/**
			 * 22.02.2015 : PN + TPN
			 */
			//podzbiór tranzycji TT które MUSZĄ być już uruchomione

			Collections.shuffle(indexTTList); //wymieszanie T tranzycji
			boolean ttPriority = false;
			
			for (int i = 0; i < ttransitions.size(); i++) {
				TimeTransition ttransition = ttransitions.get(indexTTList.get(i)); //losowo wybrana czasowa, cf. indexTTList
				if(ttransition.isActive()) { //jeśli aktywna
					if(ttransition.isForcedToFired() == true) {
						//musi zostać uruchomiona
						
						if(ttPriority) {
							launchableTransitions.add(ttransition);
							ttransition.bookRequiredTokens();
						}
						
					} else { //jest tylko aktywna
						if(ttransition.getInternalFireTime() == -1) { //czyli poprzednio nie była aktywna
							int eft = (int) ttransition.getMinFireTime();
							int lft = (int) ttransition.getMaxFireTime();
							int randomTime = GUIManager.getDefaultGUIManager().getRandomInt(eft, lft);
							ttransition.setInternalFireTime(randomTime);
							ttransition.setInternalTimer(0);
							
							if(ttPriority) { 
								if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
									launchableTransitions.add(ttransition);
									ttransition.bookRequiredTokens();
									//TAK, na pewno tu, a nie w kolejne iteracji, wtedy czas wzrośnie, więc
									//byłoby wbrew idei natychmiastowości
								}
							}
						} else { //update time
							int oldTimer = (int) ttransition.getInternalTimer();
							oldTimer++;
							ttransition.setInternalTimer(oldTimer);
							
							//jeśli to tu zostanie, to oznacza, że TT mają pierwszeństwo nad zwykłymi
							// alternatywnie (opcje programu) można ustawić, że będzie to razem ze zwykłymi robione
							
							if(ttPriority) { 
								if(ttransition.isForcedToFired() == true) {
									launchableTransitions.add(ttransition);
									ttransition.bookRequiredTokens();
								}
							}
						}
					}
				} else { //reset zegara
					ttransition.setInternalFireTime(-1);
					ttransition.setInternalTimer(-1);
				}
			}
			//teraz wybieranie tranzycji do odpalenia:
			
			Collections.shuffle(indexList);
			
			for (int i = 0; i < transitions.size(); i++) {
				Transition transition = transitions.get(indexList.get(i));
				if(launchableTransitions.contains(transition)) {
					continue;
					//TODO: czy działa to w ogóle?
					//usuwanie ze zbioru kandydatów tych t-tranzycji, które już są w kolejce do odpalenia
				}
				if(transition instanceof TimeTransition) { //jeśli czasowa
					if(transition.isActive()) { //i aktywna
						if(((TimeTransition)transition).isForcedToFired() == true) { //i musi się uruchomić
							launchableTransitions.add(transition);
							transition.bookRequiredTokens();
						}
					} else { //reset
						((TimeTransition)transition).setInternalFireTime(-1);
						((TimeTransition)transition).setInternalTimer(-1);
					}
				} else if (transition.isActive() ) {
					if ((randomLaunch.nextInt(10) < 5) || maximumMode) { // 50% 0-4 / 5-9
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
				}
			} 
		} else if (simulationType == NetType.TIME) { 
			Collections.shuffle(indexTTList); //wymieszanie T tranzycji
			
			for (int i = 0; i < ttransitions.size(); i++) {
				TimeTransition ttransition = ttransitions.get(indexTTList.get(i)); //losowo wybrana czasowa, cf. indexTTList
				if(ttransition.isActive()) { //jeśli aktywna
					if(ttransition.isForcedToFired() == true) {
						launchableTransitions.add(ttransition);
						ttransition.bookRequiredTokens();
					} else { //jest tylko aktywna
						if(ttransition.getInternalFireTime() == -1) { //czyli poprzednio nie była aktywna
							int eft = (int) ttransition.getMinFireTime();
							int lft = (int) ttransition.getMaxFireTime();
							int randomTime = GUIManager.getDefaultGUIManager().getRandomInt(eft, lft);
							ttransition.setInternalFireTime(randomTime);
							ttransition.setInternalTimer(0);
							
							if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
								launchableTransitions.add(ttransition);
								ttransition.bookRequiredTokens();
								//TAK, na pewno tu, a nie w kolejne iteracji, wtedy czas wzrośnie, więc
								//byłoby wbrew idei natychmiastowości
							}
						} else { //update time
							int oldTimer = (int) ttransition.getInternalTimer();
							oldTimer++;
							ttransition.setInternalTimer(oldTimer);
							
							if(ttransition.isForcedToFired() == true) {
								launchableTransitions.add(ttransition);
								ttransition.bookRequiredTokens();
							}
						}
					}
				} else { //reset zegara
					ttransition.setInternalFireTime(-1);
					ttransition.setInternalTimer(-1);
				}
			}
		}

		for (Transition transition : launchableTransitions) {
			transition.returnBookedTokens();
		}
		return launchableTransitions;
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
				launchingTransitions = generateValidLaunchingTransitions();
				launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
				
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
		
		prepareNetM0();
		
		ArrayList<Transition> launchingTransitions = null;
		int internalSteps = 0;
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				launchingTransitions = generateValidLaunchingTransitions();
				launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
				
				for(Transition trans : launchingTransitions) {
					int index = transitions.lastIndexOf(trans);
					int fired = transitionsTotalFiring.get(index);
					transitionsTotalFiring.set(index, fired+1); //wektor sumy odpaleń
				}
			} else {
				break;
			}
			launchAddPhase(launchingTransitions, false);
			
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
	public ArrayList<Integer> simulateNetSinglePlace(int steps, Place plc) {
		if(ready == false) {
			GUIManager.getDefaultGUIManager().log("Simulation for place "+plc.getName()+" cannot start.", "warning", true);
			return null;
		}
		prepareNetM0();
		
		//ArrayList<Transition> launchingTransitions = null;
		ArrayList<Integer> singlePlaceData = new ArrayList<Integer>();
		
		@SuppressWarnings("unused")
		int internalSteps = 0;
		
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				generateValidLaunchingTransitions(); //wypełnia launchableTransitions
				launchSubtractPhase(launchableTransitions);
			} else {
				break;
			}
			launchAddPhase(launchableTransitions, false);
			singlePlaceData.add(plc.getTokensNumber());
		}
		ready = false;
		restoreInternalMarkingZero();
		return singlePlaceData;
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
		
		int sum = 0;
		int internalSteps = 0;
		ArrayList<Transition> launchingTransitions = null;
		ArrayList<Integer> singleTransitionData = new ArrayList<Integer>();
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				launchingTransitions = generateValidLaunchingTransitions();
				launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
			} else {
				break;
			}
			if(launchingTransitions.contains(trans)) {
				singleTransitionData.add(1);
				sum++;
			} else {
				singleTransitionData.add(0);
			}
			launchAddPhase(launchingTransitions, false);
		}
		ready = false;
		singleTransitionData.add(sum);
		singleTransitionData.add(internalSteps);
		restoreInternalMarkingZero();
		return singleTransitionData;
	}
	
	/**
	 * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych do odpalonych tranzycji
	 * @param transitions ArrayList[Transition] - lista uruchamianych tranzycji
	 */
	private void launchSubtractPhase(ArrayList<Transition> transitions) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			transition.setLaunching(true);
			arcs = transition.getInArcs();
			for (Arc arc : arcs) {
				((Place) arc.getStartNode()).modifyTokensNumber(-arc.getWeight());
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
				((Place) arc.getEndNode()).modifyTokensNumber(arc.getWeight());
			}
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
	
	public void prepareNetM0() {
		//zapis aktualnego stanu jako m0
		saveInternalMarkingZero();
		//jeżeli istnieje backup, przywróć sieć do stanu m0:
		mainSimMaximumMode = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().isMaximumMode();
		
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		}
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
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().setMaximumMode(mainSimMaximumMode);
		
		for(int i=0; i<places.size(); i++) {
			places.get(i).setTokensNumber(internalBackupMarkingZero.get(i));
			places.get(i).returnTokens();
		}
		
		for(int i=0; i<transitions.size(); i++) {
			transitions.get(i).setLaunching(false);
			if(transitions.get(i) instanceof TimeTransition) {
				((TimeTransition)transitions.get(i)).setInternalFireTime(-1);
				((TimeTransition)transitions.get(i)).setInternalTimer(-1);
			}
		}
	}
}
