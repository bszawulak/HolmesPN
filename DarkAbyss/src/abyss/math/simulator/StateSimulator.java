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
	private ArrayList<ArrayList<Integer>> transitionsData = null;
	private ArrayList<Integer> transitionsCompactData = null; //wektor sumy odpaleń tranzycji
	
	/**
	 * Główny konstruktor obiektu klasy StateSimulator.
	 */
	public StateSimulator() {
		transitions = new ArrayList<Transition>();
		places = new ArrayList<Place>();
		placesData = new ArrayList<ArrayList<Integer>>();
		transitionsData = new ArrayList<ArrayList<Integer>>();
		transitionsCompactData = new ArrayList<Integer>();
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
		placesData = new ArrayList<ArrayList<Integer>>();
		transitionsData = new ArrayList<ArrayList<Integer>>();
		transitionsCompactData = new ArrayList<Integer>();
		for(int t=0; t<transitions.size(); t++)
			transitionsCompactData.add(0);
		
		simulationType = netT;
		maximumMode = mode;
		
		if(transitions.size() > 0 && places.size() > 0)
			ready = true;
		else
			ready = false;
		
		return ready;
	}
	
	/**
	 * Metoda symuluje podaną liczbę kroków sieci Petriego.
	 * @param steps int - liczba kroków do symulacji
	 * @param pBar JProgressBar - pasek postępu
	 */
	public void simulateNet(int steps, JProgressBar pBar) {
		if(ready == false) {
			JOptionPane.showMessageDialog(null,"Simulation cannot start, initializization phase failed.", 
					"State Simulation problem",JOptionPane.ERROR_MESSAGE);
		}
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveMarkingZero();
		ArrayList<Transition> launchingTransitions = null;
		int updateTime = steps / 100;
		
		for(int i=0; i<steps; i++) {
			pBar.setValue(i+1);
			
			if(i % updateTime == 0)
				pBar.update(pBar.getGraphics()); // (╯°□°）╯︵ ┻━┻)  
			
			if (isPossibleStep()){ 
				launchingTransitions = generateValidLaunchingTransitions();
				launchSubtractPhase(launchingTransitions); //zabierz tokeny poprzez aktywne tranzycje
				
				ArrayList<Integer> transRow = new ArrayList<Integer>();
				for(int t=0; t<transitions.size(); t++) {
					transRow.add(0);
				}
				for(Transition trans : launchingTransitions) {
					int index = transitions.lastIndexOf(trans);
					transRow.set(index, 1);
					
					int fired = transitionsCompactData.get(index);
					transitionsCompactData.set(index, fired+1);
				}
				transitionsData.add(transRow);
			} else {
				break;
			}
			launchAddPhase(launchingTransitions, false);
			
			//zbierz informacje o tokenach w miejsach:
			ArrayList<Integer> marking = new ArrayList<Integer>();
			for(int p=0; p<places.size(); p++) {
				marking.add(places.get(p).getTokensNumber());
			}
			placesData.add(marking);
		}
		
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		ready = false;
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
	 * Metoda zwraca wektór sumy uruchomień dla tranzycji.
	 * @return ArrayList[Integer] - wektor tranzycji po symulacji
	 */
	public ArrayList<Integer> getTransitionsCompactData() {
		return transitionsCompactData;
	}
}
