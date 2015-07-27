package abyss.petrinet.simulators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.elements.Transition.TransitionType;
import abyss.petrinet.simulators.NetSimulator.NetType;

public class SimulatorEngine {
	private NetType simulationType = NetType.BASIC;
	private ArrayList<Transition> transitions = null;
	private ArrayList<Transition> time_transitions = null;
	private ArrayList<Integer> allTransitionsIndicesList = null;
	private ArrayList<Integer> timeTransIndicesList = null;
	private ArrayList<Transition> launchableTransitions = null;
	private Random generator = null;
	private boolean maximumMode = false;
	
	public double planckDistance = 1.0;
	
	public SimulatorEngine() {
		generator = new Random(System.currentTimeMillis());
	}
	
	public void setEngine(NetType simulationType, boolean mode) {
		this.simulationType = simulationType;
		this.generator = new Random(System.currentTimeMillis());
		//INIT:
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		time_transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTimeTransitions();
		allTransitionsIndicesList = new ArrayList<Integer>();
		timeTransIndicesList = new ArrayList<Integer>();
		launchableTransitions =  new ArrayList<Transition>();
		
		for(int t=0; t<transitions.size(); t++) {
			allTransitionsIndicesList.add(t);
		}
		
		for(int i=0; i<time_transitions.size(); i++) {
			timeTransIndicesList.add(i);
		}
	}
	
	public void setSimulationType(NetType simulationType) {
		this.simulationType = simulationType;
	}
	
	/**
	 * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
	 */
	public ArrayList<Transition> generateValidLaunchingTransitions() {
		boolean generated = false;
		int safetyCounter = 0;
		while (!generated) {
			generateLaunchingTransitions(simulationType);
			if (launchableTransitions.size() > 0) {
				generated = true; 
			} else {
				if (simulationType == NetType.TIME || simulationType == NetType.HYBRID) {
					return launchableTransitions; 
				}
				
				safetyCounter++;
				if(safetyCounter == 9) { // safety measure
					if(isPossibleStep(transitions) == false) {
						GUIManager.getDefaultGUIManager().log("Error, no active transition, yet generateValidLaunchingTransitions "
								+ "has been activated. Please advise authors.", "error", true);
						return launchableTransitions; 
					}
				}
			}
		}
		return launchableTransitions; 
	}
	
	/**
	 * Metoda pomocnicza dla generateValidLaunchingTransitions(), odpowiedzialna za sprawdzenie
	 * które tranzycje nadają się do uruchomienia. Aktualnie działa dla modelu klasycznego PN
	 * oraz czasowego.
	 */
	private void generateLaunchingTransitions(NetType simulationType) {
		launchableTransitions.clear();

		if (simulationType == NetType.BASIC) {
			Collections.shuffle(allTransitionsIndicesList);

			for (int i = 0; i < allTransitionsIndicesList.size(); i++) {
				Transition transition = transitions.get(allTransitionsIndicesList.get(i));
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
				if(time_transitions.get(i).getDPNstatus() == true) {
					//sprawdź które tranzycje DPN muszą odpalić:
					if(time_transitions.get(i).isDPNforcedToFire() == true) {
						launchableTransitions.add(time_transitions.get(i));
						indexTTList.remove((Integer)i); // ten problem z głowy
					} else {
						indexDPNList.add(i); 
					}
				}
			}
			Collections.shuffle(indexTTList); //wymieszanie T tranzycji (bez odpalanych DPN)
			Collections.shuffle(indexDPNList);
			
			for(int i=0; i < indexDPNList.size(); i++) { //mają DPN status skoro trafiły na tę listę
				int index = indexDPNList.get(i);
				Transition dpn_transition = time_transitions.get(index);
				if(dpn_transition.getTPNstatus() == true) {
					if(dpn_transition.isTPNforcedToFired() == true) { //TPN zakończyło liczenie
						int decision = DPNdecision(dpn_transition, index, indexDPNList, indexTTList);
						if(decision == -1) {
							i--;
							continue;
						} else if(decision == 0) {
							continue;
						}
						
					} else { //dpn_transition.isTPNforcedToFired() == false
						if(dpn_transition.isActive()) {
							if(dpn_transition.getInternalFireTime() == -1) { //czyli poprzednio nie była aktywna
								int eft = (int) dpn_transition.getMinFireTime();
								int lft = (int) dpn_transition.getMaxFireTime();
								int randomTime = GUIManager.getDefaultGUIManager().getRandomInt(eft, lft);
								dpn_transition.setInternalFireTime(randomTime);
								dpn_transition.setInternalTPN_Timer(0);
								
								if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
									int decision = DPNdecision(dpn_transition, index, indexDPNList, indexTTList);
									if(decision == -1) {
										i--;
										continue;
									} else if(decision == 0) {
										continue;
									}
								} else {
									indexTTList.remove((Integer)index);
								}
							} else { //update time
								int oldTimer = (int) dpn_transition.getInternalTPN_Timer();
								oldTimer++;
								dpn_transition.setInternalTPN_Timer(oldTimer);
								
								if(dpn_transition.isTPNforcedToFired() == true) {
									int decision = DPNdecision(dpn_transition, index, indexDPNList, indexTTList);
									if(decision == -1) {
										i--;
										continue;
									} else if(decision == 0) {
										continue;
									}
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
					int decision = DPNdecision(dpn_transition, index, indexDPNList, indexTTList);
					if(decision == -1) {
						i--;
						continue;
					} else if(decision == 0) {
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
		}

		for (Transition transition : launchableTransitions) {
			transition.returnBookedTokens();
		}
	}
	
	/**
	 * Metoda odpowiedzialna za decyzję co zrobić z DPN - uruchomić, kontynuować tykanie zegara
	 * lub zdeaktywować. 
	 * @param dpn_transition Transition - tranzycja
	 * @param index int - jej indeks na listach indeksów
	 * @param indexDPNList ArrayList[Integer] - wektor indeksów DPN
	 * @param indexTTList ArrayList[Integer] - wektor indeksów TPN
	 * @return
	 */
	private int DPNdecision(Transition dpn_transition, int index, ArrayList<Integer> indexDPNList,  ArrayList<Integer> indexTTList) {
		double timer = dpn_transition.getInternalDPN_Timer();
		if(timer == -1 && dpn_transition.isActive()) { //może wystartować
			//ustaw zegar na start
			dpn_transition.setInternalDPN_Timer(0);
			//dodaj do odpalonych (tylko połknie tokeny, nie wyprodukuje)
			launchableTransitions.add(dpn_transition); //immediate fire bo 0=0
			dpn_transition.bookRequiredTokens(); //odpala czy nie, rezerwuje tokeny teraz
			indexDPNList.remove((Integer)index);
			indexTTList.remove((Integer)index);
			return -1;
		}
		if(timer > -1) { //jeśli zegar to 0 lub więcej : liczenie
			timer += planckDistance;
			dpn_transition.setInternalDPN_Timer(timer);
			indexDPNList.remove((Integer)index);
			
			if(dpn_transition.isDPNforcedToFire()) {
				//doliczyła do końca
				launchableTransitions.add(dpn_transition);
			}
			indexTTList.remove((Integer)index);
			return -1;
		}
		if(dpn_transition.isActive() == false) {
			indexTTList.remove((Integer)index);
			return 0;
		}
		
		return 1;
	}
	
	/**
	 * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna tranzycja.
	 * @return boolean - true jeśli jest choć jedna aktywna tranzycja; false w przeciwnym wypadku
	 */
	private boolean isPossibleStep(ArrayList<Transition> transitions) {
		for (Transition transition : transitions) {
			if (transition.isActive())
				return true;
		}
		return false;
	}
}
