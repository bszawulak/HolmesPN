package abyss.math.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import abyss.darkgui.GUIManager;
import abyss.math.pnElements.Arc;
import abyss.math.pnElements.Place;
import abyss.math.pnElements.Transition;
import abyss.math.pnElements.Arc.TypesOfArcs;
import abyss.math.pnElements.Transition.TransitionType;
import abyss.math.simulator.NetSimulator.NetType;

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
	private NetType simulationType;
	private boolean maximumMode = false;
	public double timeNetStepCounter = 0;
	public double timeNetPartStepCounter = 0;
	
	private ArrayList<ArrayList<Integer>> placesData = null;
	private ArrayList<Double> placesAvgData = null; //średnia liczba tokenów w miejscu
	private ArrayList<ArrayList<Integer>> transitionsData = null;
	private ArrayList<Integer> transitionsTotalFiring = null; //wektor sumy odpaleń tranzycji
	private ArrayList<Double> transitionsAvgData = null;
	private ArrayList<Integer> allTransitionsIndicesList = null;
	private ArrayList<Integer> timeTransIndicesList = null;
	
	private ArrayList<Integer> internalBackupMarkingZero = new ArrayList<Integer>();
	private boolean mainSimMaximumMode = false;
	
	ArrayList<Transition> launchableTransitions = new ArrayList<Transition>();
	
	private Random generator; // = new Random(System.currentTimeMillis());
	
	/**
	 * Główny konstruktor obiektu klasy StateSimulator.
	 */
	public StateSimulator() {
		generator = new Random(System.currentTimeMillis());
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
		allTransitionsIndicesList = new ArrayList<Integer>();
		timeTransIndicesList = new ArrayList<Integer>();
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
			allTransitionsIndicesList.add(t);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
		}
		for(int i=0; i<time_transitions.size(); i++) {
			timeTransIndicesList.add(i);
		}
		
		simulationType = netT;
		maximumMode = mode;
		
		generator = new Random(System.currentTimeMillis());
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
		allTransitionsIndicesList = new ArrayList<Integer>();
		timeTransIndicesList = new ArrayList<Integer>();
		for(int t=0; t<transitions.size(); t++) {
			transitionsTotalFiring.add(0);
			allTransitionsIndicesList.add(t);
		}
		for(int p=0; p<places.size(); p++) {
			placesAvgData.add(0.0);
		}
		for(int i=0; i<time_transitions.size(); i++) {
			timeTransIndicesList.add(i);
		}
		generator = new Random(System.currentTimeMillis());
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
	 * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
	 */
	private ArrayList<Transition> generateValidLaunchingTransitions() {
		boolean generated = false;
		int safetyCounter = 0;
		while (!generated) {
			generateLaunchingTransitions();
			if (launchableTransitions.size() > 0) {
				generated = true; 
			} else {
				if (simulationType == NetType.TIME || simulationType == NetType.HYBRID) {
					return launchableTransitions; 
				}
				
				safetyCounter++;
				if(safetyCounter == 9) { // safety measure
					if(isPossibleStep() == false) {
						GUIManager.getDefaultGUIManager().log("Error, no active transition, yet generateValidLaunchingTransitions "
								+ "has been activated. Please advise authors.", "error", true);
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
	 */
	private void generateLaunchingTransitions() {
		launchableTransitions.clear();
		if (simulationType == NetType.BASIC) {
			Collections.shuffle(allTransitionsIndicesList);

			for (int i = 0; i < allTransitionsIndicesList.size(); i++) {
				Transition transition = transitions.get(allTransitionsIndicesList.get(i));
				//if(transitions.indexOf(transition)==14 && transition.isActive() == false) {
					//@SuppressWarnings("unused")
					//int x=1;
				//}
				
				if (transition.isActive() )
					if ((generator.nextInt(10) < 5) || maximumMode) { // 50% 0-4 / 5-9
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
			}
		} else if (simulationType == NetType.HYBRID) { 
			/** 22.02.2015 : PN + TPN */
			Collections.shuffle(timeTransIndicesList); //wymieszanie T-tranzycji
			boolean ttPriority = false;
			
			for (int i = 0; i < time_transitions.size(); i++) {
				Transition timeTransition = time_transitions.get(timeTransIndicesList.get(i)); //losowo wybrana czasowa
				if(timeTransition.isActive()) { //jeśli aktywna
					if(timeTransition.isTPNforcedToFired() == true) {
						//musi zostać uruchomiona
						if(ttPriority) {
							launchableTransitions.add(timeTransition);
							timeTransition.bookRequiredTokens();
						}
						
					} else { //jest tylko aktywna
						if(timeTransition.getInternalFireTime() == -1) { //czyli poprzednio nie była aktywna
							int eft = (int) timeTransition.getMinFireTime();
							int lft = (int) timeTransition.getMaxFireTime();
							int randomTime = GUIManager.getDefaultGUIManager().getRandomInt(eft, lft);
							timeTransition.setInternalFireTime(randomTime);
							timeTransition.setInternalTPN_Timer(0);
							
							if(ttPriority) { 
								if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
									launchableTransitions.add(timeTransition);
									timeTransition.bookRequiredTokens();
									//TAK, na pewno tu, a nie w kolejne iteracji, wtedy czas wzrośnie, więc
									//byłoby wbrew idei natychmiastowości
								}
							}
						} else { //update time
							int oldTimer = (int) timeTransition.getInternalTPN_Timer();
							oldTimer++;
							timeTransition.setInternalTPN_Timer(oldTimer);
							
							//jeśli to tu zostanie, to oznacza, że TT mają pierwszeństwo nad zwykłymi
							// alternatywnie (opcje programu) można ustawić, że będzie to razem ze zwykłymi robione
							
							if(ttPriority) { 
								if(timeTransition.isTPNforcedToFired() == true) {
									launchableTransitions.add(timeTransition);
									timeTransition.bookRequiredTokens();
								}
							}
						}
					}
				} else { //reset zegara
					timeTransition.setInternalFireTime(-1);
					timeTransition.setInternalTPN_Timer(-1);
				}
			} 
			
			Collections.shuffle(allTransitionsIndicesList);
			//teraz wybieranie tranzycji do odpalenia:
			for (int i = 0; i < transitions.size(); i++) {
				Transition transition = transitions.get(allTransitionsIndicesList.get(i));
				if(launchableTransitions.contains(transition)) {
					continue;
					//TODO: czy działa to w ogóle?
					//usuwanie ze zbioru kandydatów tych t-tranzycji, które już są w kolejce do odpalenia
				}
				if(transition.getTransType() == TransitionType.TPN) { //jeśli czasowa
					if(transition.isActive()) { //i aktywna
						if(transition.isTPNforcedToFired() == true) { //i musi się uruchomić
							launchableTransitions.add(transition);
							transition.bookRequiredTokens();
						}
					} else { //reset
						transition.setInternalFireTime(-1);
						transition.setInternalTPN_Timer(-1);
					}
				} else if (transition.isActive() ) {
					if ((generator.nextInt(10) < 5) || maximumMode) { // 50% 0-4 / 5-9
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
				}
			} 
		} else if (simulationType == NetType.TIME) { 
			Collections.shuffle(timeTransIndicesList);
			ArrayList<Integer> indexTTList = new ArrayList<Integer>(timeTransIndicesList);
			ArrayList<Integer> indexDPNList = new ArrayList<Integer>();
			for(int i=0; i<time_transitions.size(); i++) {
				if(time_transitions.get(i).getDPNstatus()) {
					if(time_transitions.get(i).isDPNforcedToFire()) {
						launchableTransitions.add(time_transitions.get(i));
						indexTTList.remove((Integer)i); // ten problem z głowy
					} else {
						indexDPNList.add(i); 
					}
				}
			}
			Collections.shuffle(indexTTList); //wymieszanie T tranzycji
			Collections.shuffle(indexDPNList);
			
			for(int i=0; i < indexDPNList.size(); i++) { //mają DPN status skoro trafiły na tę listę
				int index = indexDPNList.get(i);
				Transition dpn_transition = time_transitions.get(index);
				if(dpn_transition.getTPNstatus()) {
					if(dpn_transition.isTPNforcedToFired()) { //TPN zakończyło liczenie
						//SEKCJA I-1
						double timer = dpn_transition.getInternalDPN_Timer();
						if(timer == -1 && dpn_transition.isActive()) { //może wystartować
							dpn_transition.setInternalDPN_Timer(0);
							launchableTransitions.add(dpn_transition); //immediate fire bo 0=0
							dpn_transition.bookRequiredTokens(); //odpala czy nie, rezerwuje tokeny teraz
							indexDPNList.remove((Integer)index);
							i--;
							indexTTList.remove((Integer)index);
							continue;
						}
						if(timer > -1) { //nie musi być aktywna na pewno nie będzie to ta powyżej, bo 'continue'
							timer++;
							dpn_transition.setInternalDPN_Timer(timer);
							indexDPNList.remove((Integer)index);
							if(dpn_transition.isDPNforcedToFire())
								launchableTransitions.add(dpn_transition);
							i--;
							indexTTList.remove((Integer)index);
							continue;
						}
						if(dpn_transition.isActive() == false) {
							indexTTList.remove((Integer)index);
							continue;
						}
						//SEKCJA I-1 END
					} else { //dpn_transition.isTPNforcedToFired() == false
						if(dpn_transition.isActive()) {
							if(dpn_transition.getInternalFireTime() == -1) { //czyli poprzednio nie była aktywna
								int eft = (int) dpn_transition.getMinFireTime();
								int lft = (int) dpn_transition.getMaxFireTime();
								int randomTime = GUIManager.getDefaultGUIManager().getRandomInt(eft, lft);
								dpn_transition.setInternalFireTime(randomTime);
								dpn_transition.setInternalTPN_Timer(0);
								
								if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
									//SEKCJA I-1
									double timer = dpn_transition.getInternalDPN_Timer();
									if(timer == -1 && dpn_transition.isActive()) { //może wystartować
										dpn_transition.setInternalDPN_Timer(0);
										launchableTransitions.add(dpn_transition); //immediate fire bo 0=0
										dpn_transition.bookRequiredTokens(); //odpala czy nie, rezerwuje tokeny teraz
										indexDPNList.remove((Integer)index);
										i--;
										indexTTList.remove((Integer)index);
										continue;
									}
									if(timer > -1) { //nie musi być aktywna na pewno nie będzie to ta powyżej, bo 'continue'
										timer++;
										dpn_transition.setInternalDPN_Timer(timer);
										indexDPNList.remove((Integer)index);
										if(dpn_transition.isDPNforcedToFire())
											launchableTransitions.add(dpn_transition);
										i--;
										indexTTList.remove((Integer)index);
										continue;
									}
									if(dpn_transition.isActive() == false) {
										indexTTList.remove((Integer)index);
										continue;
									}
									//SEKCJA I-1 END
								} else {
									indexTTList.remove((Integer)index);
								}
							} else { //update time
								int oldTimer = (int) dpn_transition.getInternalTPN_Timer();
								oldTimer++;
								dpn_transition.setInternalTPN_Timer(oldTimer);
								
								if(dpn_transition.isTPNforcedToFired() == true) {
									//SEKCJA I-1
									double timer = dpn_transition.getInternalDPN_Timer();
									if(timer == -1 && dpn_transition.isActive()) { //może wystartować
										dpn_transition.setInternalDPN_Timer(0);
										launchableTransitions.add(dpn_transition); //immediate fire bo 0=0
										dpn_transition.bookRequiredTokens(); //odpala czy nie, rezerwuje tokeny teraz
										indexDPNList.remove((Integer)index);
										i--;
										indexTTList.remove((Integer)index);
										continue;
									}
									if(timer > -1) { //nie musi być aktywna na pewno nie będzie to ta powyżej, bo 'continue'
										timer++;
										dpn_transition.setInternalDPN_Timer(timer);
										indexDPNList.remove((Integer)index);
										if(dpn_transition.isDPNforcedToFire())
											launchableTransitions.add(dpn_transition);
										i--;
										indexTTList.remove((Integer)index);
										continue;
									}
									if(dpn_transition.isActive() == false) {
										indexTTList.remove((Integer)index);
										continue;
									}
									//SEKCJA I-1 END
								} else {
									indexTTList.remove((Integer)index);
								}
							}
						} else { //not active
							indexTTList.remove((Integer)index);
							dpn_transition.resetTimeVariables();
							continue;
						}
					}
				} else { //pure DPN
					//SEKCJA I-1
					double timer = dpn_transition.getInternalDPN_Timer();
					if(timer == -1 && dpn_transition.isActive()) { //może wystartować
						dpn_transition.setInternalDPN_Timer(0);
						launchableTransitions.add(dpn_transition); //immediate fire bo 0=0
						dpn_transition.bookRequiredTokens(); //odpala czy nie, rezerwuje tokeny teraz
						indexDPNList.remove((Integer)index);
						i--;
						indexTTList.remove((Integer)index);
						continue;
					}
					if(timer > -1) { //nie musi być aktywna na pewno nie będzie to ta powyżej, bo 'continue'
						timer++;
						dpn_transition.setInternalDPN_Timer(timer);
						indexDPNList.remove((Integer)index);
						if(dpn_transition.isDPNforcedToFire())
							launchableTransitions.add(dpn_transition);
						i--;
						indexTTList.remove((Integer)index);
						continue;
					}
					if(dpn_transition.isActive() == false) {
						indexTTList.remove((Integer)index);
						continue;
					}
					
				}
			}
			
			for(int i=0; i < indexTTList.size(); i++) {
				int index = indexTTList.get(i);
				Transition ttransition = time_transitions.get(index);
				if(ttransition.isActive()) { //jeśli aktywna
					if(ttransition.isTPNforcedToFired() == true) { //pure DPN: eft=lft=0, dur > 0
						launchableTransitions.add(ttransition);
						ttransition.bookRequiredTokens();
					} else { //jest tylko aktywna
						if(ttransition.getInternalFireTime() == -1) { //czyli poprzednio nie była aktywna
							int eft = (int) ttransition.getMinFireTime();
							int lft = (int) ttransition.getMaxFireTime();
							int randomTime = GUIManager.getDefaultGUIManager().getRandomInt(eft, lft);
							ttransition.setInternalFireTime(randomTime);
							ttransition.setInternalTPN_Timer(0);
							
							if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
								launchableTransitions.add(ttransition);
								ttransition.bookRequiredTokens();
								//TAK, na pewno tu, a nie w kolejne iteracji, wtedy czas wzrośnie, więc
								//byłoby wbrew idei natychmiastowości
							}
						} else { //update time
							int oldTimer = (int) ttransition.getInternalTPN_Timer();
							oldTimer++;
							ttransition.setInternalTPN_Timer(oldTimer);
							
							if(ttransition.isTPNforcedToFired() == true) {
								launchableTransitions.add(ttransition);
								ttransition.bookRequiredTokens();
							}
						}
					}
				} else { //reset zegara
					ttransition.setInternalFireTime(-1);
					ttransition.setInternalTPN_Timer(-1);
				}
			}
			
			/*
			Collections.shuffle(timeTransIndicesList); //wymieszanie T tranzycji
			for (int i = 0; i < time_transitions.size(); i++) {
				Transition ttransition = time_transitions.get(timeTransIndicesList.get(i)); //losowo wybrana czasowa, cf. indexTTList
				if(ttransition.isActive()) { //jeśli aktywna
					if(ttransition.isTPNforcedToFired() == true) {
						launchableTransitions.add(ttransition);
						ttransition.bookRequiredTokens();
					} else { //jest tylko aktywna
						if(ttransition.getInternalFireTime() == -1) { //czyli poprzednio nie była aktywna
							int eft = (int) ttransition.getMinFireTime();
							int lft = (int) ttransition.getMaxFireTime();
							int randomTime = GUIManager.getDefaultGUIManager().getRandomInt(eft, lft);
							ttransition.setInternalFireTime(randomTime);
							ttransition.setInternalTPN_Timer(0);
							
							if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
								launchableTransitions.add(ttransition);
								ttransition.bookRequiredTokens();
								//TAK, na pewno tu, a nie w kolejne iteracji, wtedy czas wzrośnie, więc
								//byłoby wbrew idei natychmiastowości
							}
						} else { //update time
							int oldTimer = (int) ttransition.getInternalTPN_Timer();
							oldTimer++;
							ttransition.setInternalTPN_Timer(oldTimer);
							
							if(ttransition.isTPNforcedToFired() == true) {
								launchableTransitions.add(ttransition);
								ttransition.bookRequiredTokens();
							}
						}
					}
				} else { //reset zegara
					ttransition.setInternalFireTime(-1);
					ttransition.setInternalTPN_Timer(-1);
				}
			}
			*/
		}

		for (Transition transition : launchableTransitions) {
			transition.returnBookedTokens();
		}
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
				if(test_t.getInternalDPN_Timer() == 0 && test_t.getDurationTime() != 0) {
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
		
		int internalSteps = 0;
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				generateValidLaunchingTransitions();
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
		
		for(int i=0; i<steps; i++) {
			//internalSteps++;
			if (isPossibleStep()){ 
				generateValidLaunchingTransitions(); //wypełnia launchableTransitions
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
		
		int sum = 0;
		int internalSteps = 0;
		ArrayList<Integer> transDataVector = new ArrayList<Integer>();
		for(int i=0; i<steps; i++) {
			internalSteps++;
			if (isPossibleStep()){ 
				generateValidLaunchingTransitions();
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
			if(transition.getInternalDPN_Timer() > 0) //yeah, trust me, I'm an engineer
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
					place.modifyTokensNumber(-2);
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
				
				if(arc.getArcType() != TypesOfArcs.NORMAL)
					GUIManager.getDefaultGUIManager().log("Error: non-standard arc used to produce tokens: "+place.getName()+ 
							" arc: "+arc.toString(), "error", true);
				
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
		
		//zapis aktualnego stanu jako m0
		saveInternalMarkingZero();
		//jeżeli istnieje backup, przywróć sieć do stanu m0:
		mainSimMaximumMode = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().isMaximumMode();
		
		
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
			places.get(i).freeReservedTokens();
		}
		
		for(int i=0; i<transitions.size(); i++) {
			transitions.get(i).setLaunching(false);
			if(transitions.get(i).getTransType() == TransitionType.TPN) {
				transitions.get(i).setInternalFireTime(-1);
				transitions.get(i).setInternalTPN_Timer(-1);
				transitions.get(i).setInternalDPN_Timer(-1);
			}
		}
	}
}
