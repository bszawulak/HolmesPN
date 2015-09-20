package holmes.petrinet.simulators;

import java.util.ArrayList;
import java.util.Collections;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.simulators.NetSimulator.NetType;

/**
 * Silnik symulatora programu. Innymi słowy są tutaj procedury odpowiedzialne za tworzenie
 * listy tranzycji które mają odpalić w kolejnym kroku symulacji.
 * 
 * @author MR
 */
public class StandardTokenSimulator implements IEngine {
	private GUIManager overlord;
	private NetType netSimType = NetType.BASIC;
	private ArrayList<Transition> transitions = null;
	private ArrayList<Transition> time_transitions = null;
	private ArrayList<Integer> transitionsIndexList = null;
	private ArrayList<Integer> timeTransitionsIndexList = null;
	private ArrayList<Transition> launchableTransitions = null;
	private IRandomGenerator generator = null;
	private boolean maxMode = false;
	private boolean singleMode = false;
	private double planckDistance = 1.0;
	
	//jesli true, wtedy TDPN dziala tak, że clock liczy do EFT i zaraz potem wchodzi DPN
	private boolean TDPNdecision1 = false;

	/**
	 * Konstruktor obiektu klasy SimulatorEngine.
	 */
	public StandardTokenSimulator() {
		this.overlord = GUIManager.getDefaultGUIManager();
		generator = new StandardRandom(System.currentTimeMillis());
	}
	
	/**
	 * Ustawianie podstawocyh parametrów silnika symulacji.
	 * @param simulationType NetType - rodzaj symulowanej sieci
	 * @param maxMode boolean - tryb maximum
	 * @param singleMode boolean - true, jeśli tylko 1 tranzycja ma odpalić
	 * @param transitions ArrayList[Transition] - wektor wszystkich tranzycji
	 * @param time_transitions ArrayList[Transition] - wektor tranzycji czasowych
	 * @param places ArrayList[Place] - wektor miejsc
	 */
	public void setEngine(NetType simulationType, boolean maxMode, boolean singleMode, 
			ArrayList<Transition> transitions, ArrayList<Transition> time_transitions,
			ArrayList<Place> places) {
		this.netSimType = simulationType;
		this.maxMode = maxMode;
		this.singleMode = singleMode;
		
		if(overlord.simSettings.getGeneratorType() == 1) {
			this.generator = new HighQualityRandom(System.currentTimeMillis());
		} else {
			this.generator = new StandardRandom(System.currentTimeMillis());
		}
		
		if(overlord.getSettingsManager().getValue("simTDPNrunWhenEft").equals("1"))
			TDPNdecision1 = true;
		else
			TDPNdecision1 = false;
		
		//INIT:
		this.transitions = transitions;
		this.time_transitions = time_transitions;
		transitionsIndexList = new ArrayList<Integer>();
		timeTransitionsIndexList = new ArrayList<Integer>();
		launchableTransitions =  new ArrayList<Transition>();
		
		for(int t=0; t<transitions.size(); t++) {
			transitionsIndexList.add(t);
		}
		
		for(int i=0; i<time_transitions.size(); i++) {
			timeTransitionsIndexList.add(i);
		}
	}
	
	/**
	 * Metoda do zmiany trybu symulacji.
	 * @param simulationType NetType - nowy typ symulacji
	 */
	public void setNetSimType(NetType simulationType) {
		this.netSimType = simulationType;
	}
	
	/**
	 * Metoda ustawia status trybu maximum.
	 * @param value boolean - true, jeśli tryb włączony
	 */
	public void setMaxMode(boolean value) {
		this.maxMode = value;
	}
	
	/**
	 * Ustawia tryb pojedynczego odpalania.
	 * @param value boolean - true, jeśli tylko 1 tranzycja ma odpalić na turę.
	 */
	public void setSingleMode(boolean value) {
		this.singleMode = value;
		
		if(singleMode != false)
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("simSingleMode").equals("1")) {
				maxMode = true;
			}
	}

	/**
	 * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora.
	 * @param emptySteps boolean - true, jeśli może być wygenerowany krok bez odpalania tranzycji
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
	 */
	public ArrayList<Transition> getTransLaunchList(boolean emptySteps) {
		if(emptySteps == true) {
			generateNormal();
		} else {
			generateWithoutEmptySteps();
		}
		return launchableTransitions; 
	}
	
	/**
	 * Metoda generowania nowych tranzycji do odpalenia bez pustych kroków (pusty wektor tranzycji
	 * do odpalenia możliwy tylko w przypadku sieci czasowych).
	 * @return ArrayList[Transition] - wektor tranzycji do odpalenia
	 */
	private ArrayList<Transition> generateWithoutEmptySteps() {
		boolean generated = false;
		int safetyCounter = 0;
		while (!generated) {
			generateLaunchingTransitions(netSimType);
			if (launchableTransitions.size() > 0) {
				generated = true; 
			} else {
				if (netSimType == NetType.TIME || netSimType == NetType.HYBRID) {
					return launchableTransitions;
				} else {
					safetyCounter++;
					if(safetyCounter == 9) { // safety measure
						if(isPossibleStep(transitions) == false) {
							GUIManager.getDefaultGUIManager().log("Error, no active transition, yet generateValidLaunchingTransitions "
									+ "has been activated. Please advise authors if this error show up frequently.", "error", true);
							generated = true; 
							//return launchableTransitions; 
						}
					}
				}
			}
		}
		//TODO: check
		if(singleMode) {
			int happyWinner = generator.nextInt(launchableTransitions.size());
			Transition winner = launchableTransitions.get(happyWinner);
			launchableTransitions.clear();
			launchableTransitions.add(winner);
		} else {
			return launchableTransitions; 
		}	
		return launchableTransitions; //bez tej linii będzie błąd, tak, wiem, że to vs. powyższe jest bez sensu.
	}
	
	/**
	 * Metoda generowania nowych tranzycji do odpalenia dopuszczający puste kroki.
	 * @return ArrayList[Transition] - wektor tranzycji do odpalenia
	 */
	private ArrayList<Transition> generateNormal() {
		generateLaunchingTransitions(netSimType);	
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
			Collections.shuffle(transitionsIndexList);

			for (int i = 0; i < transitionsIndexList.size(); i++) {
				Transition transition = transitions.get(transitionsIndexList.get(i));
				if (transition.isActive() ) {
					if ((generator.nextInt(10) < 5) || maxMode) { // 50% 0-4 / 5-9
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
				}
			}
		} else if (simulationType == NetType.HYBRID) { 
			timeTransDecisions();
			
			Collections.shuffle(transitionsIndexList);
			//teraz wybieranie tranzycji do odpalenia:
			for (int i = 0; i < transitions.size(); i++) {
				Transition transition = transitions.get(transitionsIndexList.get(i));
				if(transition.getTransType() != TransitionType.PN)
					continue;
				
				if(launchableTransitions.contains(transition)) {
					continue;
				}
				
				if (transition.isActive() ) {
					if ((generator.nextInt(10) < 5) || maxMode) { // 50% 0-4 / 5-9
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
				}
			}
			//oldHybrid(); 
			
		} else if (simulationType == NetType.TIME) { //czysty model czasowy TPN / DPN
			timeTransDecisions();
		}

		for (Transition transition : launchableTransitions) {
			transition.returnBookedTokens();
		}
	}

	/**
	 * Metoda odpowiedzialna za cały proces decyzyjny związany z tranzycjami czasowymi.
	 */
	private void timeTransDecisions() {
		Collections.shuffle(timeTransitionsIndexList);
		ArrayList<Integer> indexTTList = new ArrayList<Integer>(timeTransitionsIndexList);
		ArrayList<Integer> indexDPNList = new ArrayList<Integer>();
		
		//podziel tranzycje: na TPN, DPN, a jesli DPN skończyła liczyć - dodaj ją do listy odpaleń
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
		Collections.shuffle(indexTTList); //wymieszanie TPN (bez odpalanych DPN)
		Collections.shuffle(indexDPNList); //odpalonych DPN też tu nie ma, patrz wyżej
		
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
					
				} else { //nie-TPN
					if(dpn_transition.isActive()) {
						if(dpn_transition.getTPNtimerLimit() == -1) { //czyli poprzednio nie była aktywna
							int eft = (int) dpn_transition.getEFT();
							int lft = (int) dpn_transition.getLFT();
							if(TDPNdecision1)
								dpn_transition.setTPNtimerLimit(eft);
							else
								dpn_transition.setTPNtimerLimit(getRandomInt(eft, lft));
							dpn_transition.setTPNtimer(0); //start timer
							
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
							double oldTimer = dpn_transition.getTPNtimer();
							oldTimer += planckDistance;
							dpn_transition.setTPNtimer(oldTimer);
							
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
			} else { // czyli: dpn_transition.getTPNstatus() == false
				int decision = DPNdecision(dpn_transition, index, indexDPNList, indexTTList);
				if(decision == -1) {
					i--;
					continue;
				} else if(decision == 0) {
					continue;
				}
			}
		}
		
		for(int i=0; i < indexTTList.size(); i++) { //czyste TPN, DPNy zostały obsłużone wyżej
			int index = indexTTList.get(i);
			Transition ttransition = time_transitions.get(index);
			if(ttransition.isActive()) { //jeśli aktywna
				if(ttransition.isTPNforcedToFired() == true) { //pure DPN: eft=lft=0, dur > 0
					launchableTransitions.add(ttransition);
					ttransition.bookRequiredTokens();
				} else { //jest tylko aktywna
					if(ttransition.getTPNtimerLimit() == -1) { //czyli poprzednio nie była aktywna
						int eft = (int) ttransition.getEFT();
						int lft = (int) ttransition.getLFT();
						ttransition.setTPNtimerLimit(getRandomInt(eft, lft));
						ttransition.setTPNtimer(0);
						
						if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
							launchableTransitions.add(ttransition);
							ttransition.bookRequiredTokens();
							//TAK, na pewno tu, a nie w kolejne iteracji, wtedy czas wzrośnie, więc
							//byłoby wbrew idei natychmiastowości
						}
					} else { //update time
						double oldTimer = ttransition.getTPNtimer();
						oldTimer += planckDistance;
						ttransition.setTPNtimer(oldTimer);
						
						if(ttransition.isTPNforcedToFired() == true) {
							launchableTransitions.add(ttransition);
							ttransition.bookRequiredTokens();
						}
					}
				}
			} else { //reset zegara
				ttransition.setTPNtimerLimit(-1);
				ttransition.setTPNtimer(-1);
			}
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
		double timer = dpn_transition.getDPNtimer();
		if(timer == -1 && dpn_transition.isActive()) { //może wystartować
			//ustaw zegar na start
			dpn_transition.setDPNtimer(0);
			//dodaj do odpalonych (tylko połknie tokeny, nie wyprodukuje)
			launchableTransitions.add(dpn_transition); //immediate fire bo 0=0
			dpn_transition.bookRequiredTokens(); //odpala czy nie, rezerwuje tokeny teraz
			indexDPNList.remove((Integer)index);
			indexTTList.remove((Integer)index);
			return -1;
		}
		if(timer > -1) { //jeśli zegar to 0 lub więcej : liczenie
			timer += planckDistance;
			dpn_transition.setDPNtimer(timer);
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
	
	/**
	 * Metoda zwraca liczbę losową typu int z podanego zakresu.
	 * @param min int - dolna granica
	 * @param max int - górna granica
	 * @return int - liczba z zakresu [min, max]
	 */
	private int getRandomInt(int min, int max) {
		if(min == 0 && max == 0)
			return 0;
		if(min == max)
			return min;
		
		return generator.nextInt((max - min) + 1) + min; //OK, zakres np. 3do6 daje: 3,4,5,6 (graniczne obie też!)
	}
	
	@SuppressWarnings("unused")
	private void oldHybrid() {
		/** 22.02.2015 : PN + TPN */
		Collections.shuffle(timeTransitionsIndexList); //wymieszanie T-tranzycji
		boolean ttPriority = false;
		
		for (int i = 0; i < time_transitions.size(); i++) {
			Transition timeTransition = time_transitions.get(timeTransitionsIndexList.get(i)); //losowo wybrana czasowa
			if(timeTransition.isActive()) { //jeśli aktywna
				if(timeTransition.isTPNforcedToFired() == true) {
					//musi zostać uruchomiona
					if(ttPriority) {
						launchableTransitions.add(timeTransition);
						timeTransition.bookRequiredTokens();
					}
					
				} else { //jest tylko aktywna
					if(timeTransition.getTPNtimerLimit() == -1) { //czyli poprzednio nie była aktywna
						int eft = (int) timeTransition.getEFT();
						int lft = (int) timeTransition.getLFT();
						int randomTime = getRandomInt(eft, lft);
						timeTransition.setTPNtimerLimit(randomTime);
						timeTransition.setTPNtimer(0);
						
						if(ttPriority) { 
							if(lft == 0) { // eft:lft = 0:0, natychmiastowo odpalalna tranzycja
								launchableTransitions.add(timeTransition);
								timeTransition.bookRequiredTokens();
								//TAK, na pewno tu, a nie w kolejne iteracji, wtedy czas wzrośnie, więc
								//byłoby wbrew idei natychmiastowości
							}
						}
					} else { //update time
						int oldTimer = (int) timeTransition.getTPNtimer();
						oldTimer++;
						timeTransition.setTPNtimer(oldTimer);
						
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
				timeTransition.setTPNtimerLimit(-1);
				timeTransition.setTPNtimer(-1);
			}
		} 
		
		Collections.shuffle(transitionsIndexList);
		//teraz wybieranie tranzycji do odpalenia:
		for (int i = 0; i < transitions.size(); i++) {
			Transition transition = transitions.get(transitionsIndexList.get(i));
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
					transition.setTPNtimerLimit(-1);
					transition.setTPNtimer(-1);
				}
			} else if (transition.isActive() ) {
				if ((generator.nextInt(10) < 5) || maxMode) { // 50% 0-4 / 5-9
					transition.bookRequiredTokens();
					launchableTransitions.add(transition);
				}
			}
		}
	}

	/**
	 * Ustawia nowy obiekt generator liczb pseudo-losowych.
	 * @param generator IRandomGenerator - nowy obiekt
	 */
	//@Override
	//public void setGenerator(IRandomGenerator generator) {
	//	this.generator = generator;
	//}
	
	/**
	 * Zwraca aktualnie ustawiony generator liczb pseudo-losowych.
	 * @return IRandomGenerator
	 */
	public IRandomGenerator getGenerator() {
		return this.generator;
	}
}
