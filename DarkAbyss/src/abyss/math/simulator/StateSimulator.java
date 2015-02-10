package abyss.math.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Place;
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
		
		simulationType = netT;
		maximumMode = mode;
		ready = true;
		return ready;
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
		
		//TODO: reset symulatora głównego jeśli działał/działa!!!!
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveMarkingZero();
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
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		ready = false;
	}
	
	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego dla wybranego wcześniej trybu, tj.
	 * jeśli maximumMode = true, wtedy każda aktywna tranzycja musi się uruchomić.
	 * Od simulate różni się tym, że nie zbiera historii odpaleń i tokenów dla miejsc, tylko
	 * średnie wartości. Dzięki temu działa szybciej i nie zabiera tyle miejsca w pamięci.
	 * @param steps int - liczba kroków do symulacji
	 */
	public void simulateNetSimple(int steps) {
		if(ready == false) {
			GUIManager.getDefaultGUIManager().log("Simulation simple mode cannot start.", "error", true);
			return;
		}
		
		//TODO: reset symulatora głównego jeśli działał/działa!!!!
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveMarkingZero();
		ArrayList<Transition> launchingTransitions = null;
		
		//String max = "50% firing chance";
		//if(maximumMode)
		//	max = "maximum";
		//GUIManager.getDefaultGUIManager().log("Starting states simulation for "+steps+" steps in "+max+" mode.", "text", true);
		for(int i=0; i<steps; i++) {
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
			
			//zbierz informacje o tokenach w miejsach:
			//ArrayList<Integer> marking = new ArrayList<Integer>();
			for(int p=0; p<places.size(); p++) {
				int tokens = places.get(p).getTokensNumber();
				//marking.add(tokens);
				
				double sumOfTokens = placesAvgData.get(p);
				placesAvgData.set(p, sumOfTokens+tokens);
			}
			//placesData.add(marking);
		}
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsAvgData.add((double) ((double)transitionsTotalFiring.get(t)/(double)steps));
		}
		for(int p=0; p<places.size(); p++) {
			double sumOfTokens = placesAvgData.get(p);
			placesAvgData.set(p, sumOfTokens/(double)steps);
		}
		//GUIManager.getDefaultGUIManager().log("Simulation ended. Restoring zero marking.", "text", true);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		ready = false;
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
			GUIManager.getDefaultGUIManager().log("Simulation for place "+plc.getName()+" cannot start.", "error", true);
			return null;
		}
		
		//TODO: reset symulatora głównego jeśli działał/działa!!!!
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveMarkingZero();
		ArrayList<Transition> launchingTransitions = null;
		ArrayList<Integer> singlePlaceData = new ArrayList<Integer>();
		for(int i=0; i<steps; i++) {
			if (isPossibleStep()){ 
				launchingTransitions = generateValidLaunchingTransitions();
				launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
			} else {
				break;
			}
			launchAddPhase(launchingTransitions, false);
			singlePlaceData.add(plc.getTokensNumber());
		}
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		ready = false;
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
			GUIManager.getDefaultGUIManager().log("Simulation for transition "+trans.getName()+" cannot start.", "error", true);
			return null;
		}
		
		//TODO: reset symulatora głównego jeśli działał/działa!!!!
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveMarkingZero();
		ArrayList<Transition> launchingTransitions = null;
		ArrayList<Integer> singleTransitionData = new ArrayList<Integer>();
		for(int i=0; i<steps; i++) {
			if (isPossibleStep()){ 
				launchingTransitions = generateValidLaunchingTransitions();
				launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
			} else {
				break;
			}
			launchAddPhase(launchingTransitions, false);
			
			if(launchingTransitions.contains(trans))
				singleTransitionData.add(1);
			else
				singleTransitionData.add(0);
		}
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		ready = false;
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
	 * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
	 */
	private ArrayList<Transition> generateValidLaunchingTransitions() {
		boolean generated = false;
		ArrayList<Transition> launchingTransitions = new ArrayList<Transition>();
		int safetyCounter = 0;
		while (!generated) {
			launchingTransitions = generateLaunchingTransitions();
			if (launchingTransitions.size() > 0) {
				generated = true; 
			} else {
				safetyCounter++;
				if(safetyCounter == 999) { // safety measure
					if(isPossibleStep() == false) {
						GUIManager.getDefaultGUIManager().log("Error, no active transition, yet generateValidLaunchingTransitions "
								+ "has been activated. Please advise authors.", "error", true);
						return launchingTransitions;
					}
				}
			}
		}
		return launchingTransitions;
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
	 * Metoda pomocnicza dla generateValidLaunchingTransitions(), odpowiedzialna za sprawdzenie
	 * które tranzycje nadają się do uruchomienia. Aktualnie działa dla modelu klasycznego PN
	 * oraz czasowego.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia.
	 */
	private ArrayList<Transition> generateLaunchingTransitions() {
		Random randomLaunch = new Random();
		ArrayList<Transition> launchableTransitions = new ArrayList<Transition>();
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		int i = 0;
		for (@SuppressWarnings("unused") Transition transition : transitions) {
			indexList.add(i);
			i++;
		}
		Collections.shuffle(indexList);
		if (simulationType == NetType.BASIC)
			for (i = 0; i < transitions.size(); i++) {
				Transition transition = transitions.get(indexList.get(i));
				if (transition.isActive() )
					if ((randomLaunch.nextInt(10) < 5) || maximumMode) { // 50% 0-4 / 5-9
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
			}

		if (simulationType == NetType.TIME) {
			for (i = 0; i < transitions.size(); i++) {
				Transition transition = transitions.get(indexList.get(i));
				if (transition.getFireTime() == -1 && transition.isActive()) //!!!!!!!!!!!!!!!!!!!!!
					transition.setFireTime(timeNetStepCounter);

				if (transition.isActive()) {
					boolean deadLineTime = false;
					double tmp1 = transition.getMinFireTime() + transition.getFireTime();
					double tmp2 = (timeNetPartStepCounter / 1000) + timeNetStepCounter;
					if (tmp1 <= tmp2) {
						if (tmp2 >= transition.getMaxFireTime())
							deadLineTime = true;
						// calkowite
						if ((randomLaunch.nextInt(1000) < 4) || maximumMode || deadLineTime) {
							transition.bookRequiredTokens();
							launchableTransitions.add(transition);
							transition.setFireTime(-1);
						}
					}
				} else {
					transition.setFireTime(-1);
				}
			}
			this.timeNetPartStepCounter++;
			if (timeNetPartStepCounter == 1000) {
				this.timeNetPartStepCounter = 0;
				this.timeNetStepCounter++;

			}
		}

		for (Transition transition : launchableTransitions) {
			transition.returnBookedTokens();
		}
		return launchableTransitions;
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
}
