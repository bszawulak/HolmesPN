package abyss.math.simulator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Node;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;

/**
 * Klasa zajmująca się zarządzaniem całym procesem symulacji.
 * @author students - pierwsza wersja, klasyczne PN oraz TPN
 * @author MR - poprawki, zmiany, kolejne rodzaje trubów symulacji
 */
public class NetSimulator {
	private NetType simulationType;
	private SimulatorMode simulatorStatus = SimulatorMode.STOPPED;
	private SimulatorMode previousSimStatus = SimulatorMode.STOPPED;
	private PetriNet petriNet;
	private Integer delay = new Integer(30);	//opóźnienie
	private boolean simulationActive = false;
	private Timer timer;
	private ArrayList<Transition> launchingTransitions;
	private Stack<SimulationStep> actionStack;
	private boolean maximumMode = false;
	public static int DEFAULT_COUNTER = 25;			// wartość ta ma wpływ na szybkość poruszania się tokenów
	//public JFrame timeFrame = new JFrame("Zegar");
	public double timeNetStepCounter = 0;
	public double timeNetPartStepCounter = 0;
	
	private boolean writeHistory = true;
	private long timeCounter = -1;

	/**
	 * Enumeracja przechowująca tryb pracy symulatora. Dostępne wartości:<br><br>
	 * ACTION_BACK - tryb cofnięcia pojedynczej akcji<br>
	 * LOOP - tryb pętli zwykłej (STEP w pętli)<br>
	 * LOOP_BACK - tryb pętli cofania (do najwczesniejszej zapamiętanej akcji)<br>
	 * PAUSED - tryb pauzy (tymczasowo zawieszona symulacja)<br>
	 * SINGLE_TRANSITION - tryb odpalenia pojedynczej tranzycji<br>
	 * SINGLE_TRANSITION_LOOP - tryb odpalania pojedynczych tranzycji w pętli<br>
	 * STEP - tryb odpalenia pojedynczego kroku (wszystkich wylosowanych tranzycji)<br>
	 * STOPPED tryb stopu (zatrzymana symulacja)
	 */
	public enum SimulatorMode {
		LOOP, SINGLE_TRANSITION_LOOP, SINGLE_TRANSITION, STEP, STOPPED, PAUSED, ACTION_BACK, LOOP_BACK
	}

	/**
	 * BASIC<br>
	 * COLORED<br>
	 * TIME<br>
	 */
	public enum NetType {
		BASIC, TIME, HYBRID
	}
	

	/**
	 * Konstruktor obiektu symulatora sieci.
	 * @param type NetType - typ sieci
	 * @param net PetriNet - sieć do symulacji 
	 */
	public NetSimulator(NetType type, PetriNet net) {
		simulationType = type;
		petriNet = net;
		launchingTransitions = new ArrayList<Transition>();
		actionStack = new Stack<SimulationStep>(); //historia kroków
	}
	
	public void resetSimulator() {
		simulatorStatus = SimulatorMode.STOPPED;
		previousSimStatus = SimulatorMode.STOPPED;
		simulationActive = false;
		maximumMode = false;
		DEFAULT_COUNTER = 50;
		timeNetStepCounter = 0;
		timeNetPartStepCounter = 0;
		writeHistory = true;
		timeCounter = -1;
		
		actionStack.removeAllElements();
	}

	//@SuppressWarnings("incomplete-switch")
	/**
	 * Metoda rozpoczyna symulację w odpowiednim trybie. W zależności od wybranego trybu,
	 * w oddzielnym wątku symulacji zainicjalizowana zostaje odpowiednia klasa dziedzicząca
	 * po StepPerformer.
	 * @param simulatorMode SimulatorMode - wybrany tryb symulacji
	 */
	public void startSimulation(SimulatorMode simulatorMode) {
		//timeFrame.setBounds(185, 115, 80, 30);	
		//timeFrame.getContentPane().add(new JLabel(String.valueOf(timeNetStepCounter)), BorderLayout.CENTER);
		//timeFrame.pack();
		//timeFrame.setVisible(true);
		
		//zapisz stan tokenów w miejscach przed rozpoczęciem:
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == false) {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveMarkingZero();
		}
		
		previousSimStatus = getSimulatorStatus();
		setSimulatorStatus(simulatorMode);
		setSimulationActive(true);
		ActionListener taskPerformer = new SimulationPerformer();
		//ustawiania stanu przycisków symulacji:
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationDisruptButtons();
		
		checkSimulatorNetType(); //trust no one
		
		switch (getSimulatorStatus()) {
			case LOOP:
				taskPerformer = new StepLoopPerformer(true); //główny tryb
				break;
			case SINGLE_TRANSITION_LOOP:
				taskPerformer = new SingleTransitionPerformer(true);
				break;
			case SINGLE_TRANSITION:
				taskPerformer = new SingleTransitionPerformer();
				break;
			case STEP:
				taskPerformer = new StepLoopPerformer();
				break;
			case ACTION_BACK:
				taskPerformer = new StepBackPerformer();
				break;
			case LOOP_BACK:
				launchingTransitions.clear();
				taskPerformer = new StepBackPerformer(true);
				break;
			case PAUSED:
				break;
			case STOPPED:
				break;
			default:
				break;
		}
		setTimer(new Timer(getDelay(), taskPerformer));
		getTimer().start();
	}

	/**
	 * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna tranzycja.
	 * Nie ma znaczenie jaki jest tryb symulacji, jesli żadna tranzycja nie ma odpowiedniej liczby tokenów
	 * w miejsach wejściowych, symulacja musi się zakończyć.
	 * @return boolean - true jeśli jest choć jedna aktywna tranzycja; false w przeciwnym wypadku
	 */
	private boolean isPossibleStep() {
		for (Transition transition : petriNet.getTransitions()) {
			if (transition.isActive())
				return true;
		}
		return false;
	}
	
	/**
	 * Metoda ustawiająca tryb sieci do symulacji.
	 * @param type int - typ sieci:<br> 0 - PN;<br> 1 - TPN;<br> 2 - Hybrid mode
	 */
	public void setSimulatorNetType(int type)
	{
		//sprawdzenie poprawności trybu, zakładamy że Basic działa zawsze
		if(type == 0) {
			simulationType = NetType.BASIC;
		} else if(type == 1) {
			for(Node n : petriNet.getNodes()) {
				if(n instanceof Place) { //miejsca ignorujemy
					continue;
				}
				
				if(!(n instanceof TimeTransition)) {
					JOptionPane.showMessageDialog(null, "Current net is not pure Time Petri Net.\nSimulator switched to hybrid mode.",
							"Invalid mode", JOptionPane.ERROR_MESSAGE);
					GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(2);
					simulationType = NetType.HYBRID;
					return;
				}
			}
			
			simulationType = NetType.TIME;
		} else if (type == 2) {
			simulationType = NetType.HYBRID;
		}		
	}
	
	/**
	 * Metoda podobna do setSimulatorNetType(...), sprawdza, czy aktualna sieć jest poprawna
	 * z punktu widzenia ustawionego trybu symulacji
	 */
	private void checkSimulatorNetType() {
		if(simulationType == NetType.BASIC) {
			return;
		} else if(simulationType == NetType.TIME) {
			for(Node n : petriNet.getNodes()) {
				if(n instanceof Place) { //miejsca ignorujemy
					continue;
				}
				
				if(!(n instanceof TimeTransition)) {
					JOptionPane.showMessageDialog(null, "Current net is not pure Time Petri Net.\nSimulator switched to hybrid mode.",
							"Invalid mode", JOptionPane.ERROR_MESSAGE);
					GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(2);
					simulationType = NetType.HYBRID;
					return;
				}
			}
		} else if (simulationType == NetType.HYBRID) {
			//simulationType = NetType.HYBRID;
		}		
	}

	/**
	 * Metoda generuje zbiór tranzycji do uruchomienia w ramach symulatora. Jej działanie jest zależne od 
	 * sieci jaka jest symulowana, tj. w jakim trybie
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia
	 */
	private ArrayList<Transition> generateValidLaunchingTransitions() {
		boolean generated = false;
		ArrayList<Transition> launchingTransitions = new ArrayList<Transition>();
		int safetyCounter = 0;
		while (!generated) {
			launchingTransitions = generateLaunchingTransitions();
			if (launchingTransitions.size() > 0) {
				generated = true; // wcześniej w algorytmie stwierdzono, że są jakieś aktywne 
				// tranzycje, tak więc jeśli to nie tryb maksimum i żadna się nie wygenerowała 
				// (przez pechowe rzuty kostką:) ) to powtarzamy do skutku. Prawie...:
			} else {
				if (simulationType == NetType.TIME || simulationType == NetType.HYBRID) {
					return launchingTransitions; //koniec symulacji
				}
				
				safetyCounter++;
				if(safetyCounter == 99) { // safety measure
					if(isPossibleStep() == false) {
						GUIManager.getDefaultGUIManager().log("Error, no active transition but generateValidLaunchingTransitions "
								+ "has been activated. Please advise authors.", "error", true);
						return launchingTransitions;
					}
				}
			}
		}
		return launchingTransitions;
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
		ArrayList<Transition> allTransitions = petriNet.getTransitions();
		
		
		if (simulationType == NetType.BASIC) {
			ArrayList<Integer> indexList = new ArrayList<Integer>();
			for(int i=0; i<allTransitions.size(); i++)
				indexList.add(i);
			Collections.shuffle(indexList);

			for (int i = 0; i < allTransitions.size(); i++) {
				Transition transition = allTransitions.get(indexList.get(i));
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
			ArrayList<TimeTransition> timeTransitions = petriNet.getTimeTransitions();
			
			ArrayList<Integer> indexTTList = new ArrayList<Integer>();
			for(int i=0; i<timeTransitions.size(); i++)
				indexTTList.add(i);
			Collections.shuffle(indexTTList); //wymieszanie T tranzycji
			
			boolean ttPriority = false;
			
			for (int i = 0; i < timeTransitions.size(); i++) {
				TimeTransition ttransition = timeTransitions.get(indexTTList.get(i)); //losowo wybrana czasowa, cf. indexTTList
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
			
			if(ttPriority) { //usuwanie ze zbioru kandydatów tych t-tranzycji, które już są w kolejce do odpalenia
				for(Transition t : launchableTransitions) {
					allTransitions.remove(t);
				}
			}
			
			ArrayList<Integer> indexList = new ArrayList<Integer>();
			for(int i=0; i<allTransitions.size(); i++)
				indexList.add(i);
			Collections.shuffle(indexList);
			
			for (int i = 0; i < allTransitions.size(); i++) {
				Transition transition = allTransitions.get(indexList.get(i));
				
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
			ArrayList<TimeTransition> timeTransitions = petriNet.getTimeTransitions(); //nie ma innych
			ArrayList<Integer> indexTTList = new ArrayList<Integer>();
			for(int i=0; i<timeTransitions.size(); i++)
				indexTTList.add(i);
			Collections.shuffle(indexTTList); //wymieszanie T tranzycji
			
			for (int i = 0; i < timeTransitions.size(); i++) {
				TimeTransition ttransition = timeTransitions.get(indexTTList.get(i)); //losowo wybrana czasowa, cf. indexTTList
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
		/*
		if (simulationType == NetType.TIME) {
			for (i = 0; i < allTransitions.size(); i++) {
				Transition transition = allTransitions.get(indexList.get(i));
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
			
			//timeFrame.repaint();
			if (timeNetPartStepCounter == 1000) {
				this.timeNetPartStepCounter = 0;
				this.timeNetStepCounter++;
				
			}
			timeFrame.getContentPane().removeAll();// remove(0);
			timeFrame.getContentPane().add(
					new JLabel(String.valueOf((int)timeNetStepCounter) + String.valueOf("." + (int)timeNetPartStepCounter)),
					BorderLayout.CENTER);
			timeFrame.pack();
		}
		*/

		for (Transition transition : launchableTransitions) {
			transition.returnBookedTokens();
		}
		return launchableTransitions;
	}

	/**
	 * Nieużywana, nieopisana
	 * @param t
	 * @param list
	 * @return
	 */
	public ArrayList<Transition> searchConflict(Transition t, ArrayList<Transition> list) {
		return null;
	}

	/**
	 * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych odpalonych tranzycji
	 * (lub wyjściowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista uruchamianych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 */
	public void launchSubtractPhase(ArrayList<Transition> transitions, boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			transition.setLaunching(true);
			if (!backtracking)
				arcs = transition.getInArcs();
			else
				arcs = transition.getOutArcs();
			// subtract adequate number of tokens from origins
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
				Place place;
				if (!backtracking)
					place = (Place) arc.getStartNode();
				else
					place = (Place) arc.getEndNode();
				place.modifyTokensNumber(-arc.getWeight());
			}
		}
	}

	/**
	 * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych jednej odpalonej tranzycji
	 * (lub wyjściowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 * @return boolean - true, jeśli faza została pomyślnie uruchomiona; false w przeciwnym razie
	 */
	public boolean launchSingleSubtractPhase(ArrayList<Transition> transitions,
			boolean backtracking, Transition chosenTransition) {
		if (transitions.size() < 1)
			return false;
		else {
			Transition tran;
			ArrayList<Arc> arcs;
			if (!backtracking) {
				tran = transitions.get(0);
				arcs = tran.getInArcs();
			} else {
				tran = chosenTransition;
				arcs = tran.getOutArcs();
			}
			tran.setLaunching(true);
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
				Place place;
				if (!backtracking)
					place = (Place) arc.getStartNode();
				else
					place = (Place) arc.getEndNode();
				place.modifyTokensNumber(-arc.getWeight());
			}
			return true;
		}
	}

	/**
	 * Metoda uruchamia fazę wizualizacji dodawania tokenów do miejsc wyjściowych odpalonych tranzycji
	 * (lub wejściowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 */
	public void launchAddPhaseGraphics(ArrayList<Transition> transitions, boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition tran : transitions) {
			tran.setLaunching(true);
			if (!backtracking)
				arcs = tran.getOutArcs();
			else
				arcs = tran.getInArcs();
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
			}
		}
	}

	/**
	 * Metoda uruchamia fazę wizualizacji dodawania tokenów do miejsc wyjściowych dla pojedynczej
	 * spośród odpalanych tranzycji (lub wejściowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 * @return boolean - true, jeśli faza została pomyślnie uruchomiona; false w przeciwnym razie
	 */
	public boolean launchSingleAddPhaseGraphics( ArrayList<Transition> transitions, boolean backtracking,
			Transition chosenTransition) {
		if (transitions.size() < 1)
			return false;
		else {
			Transition tran;
			ArrayList<Arc> arcs;
			if (!backtracking) {
				tran = transitions.get(0);
				arcs = tran.getOutArcs();
			} else {
				tran = chosenTransition;
				arcs = tran.getInArcs();
			}
			tran.setLaunching(true);
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
			}
			return true;
		}
	}

	/**
	 * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych odpalonych
	 * tranzycji (lub wejściowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 */
	public void launchAddPhase(ArrayList<Transition> transitions, boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			transition.setLaunching(false);  // skoro tutaj dotarliśmy, to znaczy że tranzycja już
			//swoje zrobiła i jej status aktywnej się kończy w tym kroku
			if (!backtracking)
				arcs = transition.getOutArcs();
			else
				arcs = transition.getInArcs();
			//dodaj odpowiednią liczbę tokenów do miejsc
			for (Arc arc : arcs) {
				Place place;
				if (!backtracking)
					place = (Place) arc.getEndNode();
				else
					place = (Place) arc.getStartNode();
				place.modifyTokensNumber(arc.getWeight());
			}
			
			if(transition instanceof TimeTransition) {
				((TimeTransition)transition).setInternalFireTime(-1);
				((TimeTransition)transition).setInternalTimer(-1);
			}
		}
		
		
		transitions.clear();  //wyczyść listę tranzycji 'do uruchomienia' (już swoje zrobiły)
	}

	/**
	 * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych dla pojedynczej
	 * spośród odpalanych tranzycji (lub wejściowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 * @return boolean - true, jeśli faza została pomyślnie uruchomiona; false w przeciwnym razie
	 */
	public boolean launchSingleAddPhase(ArrayList<Transition> transitions, boolean backtracking, Transition chosenTransition) {
		if (transitions.size() < 1)
			return false;
		else {
			Transition tran;
			ArrayList<Arc> arcs;
			if (!backtracking) {
				tran = transitions.get(0);
				arcs = tran.getOutArcs();
			} else {
				tran = chosenTransition;
				arcs = tran.getInArcs();
			}
			for (Arc arc : arcs) {
				Place place;
				if (!backtracking)
					place = (Place) arc.getEndNode();
				else
					place = (Place) arc.getStartNode();
				place.modifyTokensNumber(arc.getWeight());
			}
			transitions.remove(tran);
			return true;
		}
	}

	/**
	 * Metoda zatrzymuje symulację po zakończeniu aktualnego kroku (gdy zostaną odpalone
	 * wszystkie odpalane w tym momencie tranzycje).
	 */
	public void stop() {
		((SimulationPerformer) getTimer().getActionListeners()[0]).scheduleStop();
	}

	/**
	 * Jeśli aktualnie trwa symulacja, metoda ta przerwie ją natychmiast, i zablokuje wszystkie
	 * inne funkcje w symulatorze, poza opcją uruchomienia tej metody. Jeśli symulacja została
	 * już zatrzymana tą metodą, ponowne jej wywołanie spowoduje kontynuowanie jej od momentu, w
	 * którym została przerwana.
	 */
	public void pause() {
		if ((getSimulatorStatus() != SimulatorMode.PAUSED) && (getSimulatorStatus() != SimulatorMode.STOPPED))
			pauseSimulation();
		else if (getSimulatorStatus() == SimulatorMode.PAUSED)
			unpauseSimulation();
		else if (getSimulatorStatus() == SimulatorMode.STOPPED)
			JOptionPane.showMessageDialog(null,
				"Can't pause a stopped simulation!", "The simulator is already stopped!", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za zatrzymanie symulacji.
	 */
	private void stopSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlySimulationInitiateButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationInitiateButtons();
		timer.stop();
		previousSimStatus = simulatorStatus;
		setSimulatorStatus(SimulatorMode.STOPPED);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za pauzowanie symulacji.
	 */
	private void pauseSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlyUnpauseButton();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlyUnpauseButton();
		timer.stop();
		previousSimStatus = simulatorStatus;
		setSimulatorStatus(SimulatorMode.PAUSED);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za ponowne uruchomienie (po pauzie) symulacji.
	 */
	private void unpauseSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationDisruptButtons();
		if (previousSimStatus != SimulatorMode.STOPPED) {
			timer.start();
			setSimulatorStatus(previousSimStatus);
		}
	}

	/**
	 * Metoda pozwala sprawdzić, czy symulacja jest w tym momencie aktywna.
	 * @return boolean - true, jeśli symulacja jest aktywna; false w przeciwnym wypadku
	 */
	public boolean isSimulationActive() {
		return simulationActive;
	}

	/**
	 * Metoda pozwala ustawić, czy symulacja jest w tym momencie aktywna.
	 * @param simulationActive boolean - wartość aktywności symulacji
	 */
	public void setSimulationActive(boolean simulationActive) {
		this.simulationActive = simulationActive;
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setSimulationActive(isSimulationActive());
	}

	/**
	 * Metoda pozwala pobrać liczbę wierzchołków w sieci Petriego.
	 * @return int - liczba wierzchołków w sieci Petriego
	 */
	public int getNodesAmount() {
		return petriNet.getNodes().size();
	}

	/**
	 * Metoda pozwala pobrać liczbę miejsc w sieci Petriego.
	 * @return int - liczba miejsc w sieci Petriego
	 */
	public int getPlacesAmount() {
		return petriNet.getPlaces().size();
	}

	/**
	 * Metoda pozwala pobrać liczbę tranzycji w sieci Petriego.
	 * @return int  - liczba tranzycji w sieci Petriego
	 */
	public int getTransitionsAmount() {
		return petriNet.getTransitions().size();
	}

	/**
	 * Metoda pozwala pobrać liczbę łuków w sieci Petriego.
	 * @return int - liczba łuków w sieci Petriego
	 */
	public int getArcsAmount() {
		return petriNet.getArcs().size();
	}

	/**
	 * Metoda pozwala pobrać łączną liczbę wszystkich tokenów w sieci Petriego.
	 * @return int - liczba tokenów w sieci Petriego
	 */
	public int getTokensAmount() {
		int tokenAmount = 0;
		for (Place place : petriNet.getPlaces()) {
			tokenAmount += place.getTokensNumber();
		}
		return tokenAmount;
	}

	/**
	 * Metoda pozwala pobrać zegar odpowiadający za wątek symulacji.
	 * @return Timer - zegar odpowiadający za wątek symulacji
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * Metoda pozwalająca ustawić nowy zegar dla wątku symulacji.
	 * @param timer Timer - zegar
	 */
	private void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * Metoda pozwala pobrać interwał czasowy pomiędzy kolejnymi krokami symulacji.
	 * @return Integer - interwał czasowy pomiędzy kolejnymi krokami symulacji wyrażony w milisekundach
	 */
	public Integer getDelay() {
		return delay;
	}

	/**
	 * Metoda pozwala ustawić interwał czasowy pomiędzy kolejnymi krokami symulacji.
	 * @param delay Integer - interwał czasowy pomiędzy kolejnymi krokami symulacji wyrażony w milisekundach
	 */
	public void setDelay(Integer delay) {
		if (timer != null)
			timer.setDelay(delay);
		this.delay = delay;
	}

	/**
	 * Metoda pozwala pobrać aktualny tryb pracy symulatora.
	 * @return SimulatorMode - tryb pracy
	 */
	public SimulatorMode getSimulatorStatus() {
		return simulatorStatus;
	}

	/**
	 * Metoda ustawiająca nowy tryb pracy dla symulatora.
	 * @param mode SimulatorMode - tryb pracy
	 */
	private void setSimulatorStatus(SimulatorMode mode) {
		this.simulatorStatus = mode;
	}
	
	/**
	 * Metoda pozwala ustawić czy symulator będzie zapamiętywac historię kroków.
	 * @param status boolean - true, jeśli ma być zapisywana historia stanów
	 */
	public void setHistoryMode(boolean status) {
		this.writeHistory = status;
	}
	
	/**
	 * Metoda pozwala okreslić, czy zapisywana jest historia stanów symulacji.
	 * @return boolean - true, jeśli symulator zapisuje historię, false w przeciwnym wypadku
	 */
	public boolean getHistoryMode() {
		return writeHistory;
	}
	
	/**
	 * Metoda zwraca wartość aktualnego kroku symulacji (numer, tj. czas).
	 * @return long - nr kroku symulacji
	 */
	public long getSimulatorTimeStep() {
		return timeCounter;
	}

	private ArrayList<Transition> cloneTransitionArray(ArrayList<Transition> transitions) {
		ArrayList<Transition> newArray = new ArrayList<Transition>();
		for (Transition transition : transitions)
			newArray.add(transition);
		return newArray;
	}

	// ================================================================================
	// simulation task performer classes
	// ================================================================================

	/**
	 * Metoda sprawdzająca, czy symulator pracuje w trybie maksymalnego uruchamiania
	 * tranzycji.
	 * @return boolean - true, jeśli włączony tryb maksymalnego uruchamiania; 
	 * 		false w przeciwnym wypadku
	 */
	public boolean isMaximumMode() {
		return maximumMode;
	}

	/**
	 * Metoda ustwaiająca trybie maksymalnego uruchamiania tranzycji.
	 * @return boolean - true, jeśli włączany jest tryb maksymalnego uruchamiania; 
	 * 		false w przeciwnym wypadku
	 */
	public void setMaximumMode(boolean maximumMode) {
		this.maximumMode = maximumMode;
	}

	/**
	 * Po tej klasie dziedziczy szereg klas implementujących konkretne tryby pracy symulatora.
	 * Metoda actionPerformed() jest wykonywana w każdym kroku symulacji przez timer obiektu
	 * NetSimulator. 
	 * @author students
	 *
	 */
	private class SimulationPerformer implements ActionListener {
		protected int counter = DEFAULT_COUNTER;		// licznik kroków graficznych
		protected boolean subtractPhase = true; // true - subtract, false - add
		// phases
		protected boolean finishedAddPhase = true;
		protected boolean scheduledStop = false;
		protected int remainingTransitionsAmount = launchingTransitions.size();

		/**
		 * Metoda aktualizuje wyświetlanie graficznej części symulacji po wykonaniu każdego kroku.
		 */
		protected void updateStep() {
			GUIManager.getDefaultGUIManager().getWorkspace().incrementSimulationStep();
			//tutaj nic się nie dzieje: a chyba chodziło o update podokna właściwości z liczbą tokenów
			GUIManager.getDefaultGUIManager().getSimulatorBox().updateSimulatorProperties();
		}

		/**
		 * Metoda inicjuje zatrzymanie symulacji po zakończeniu aktualnego kroku.
		 */
		public void scheduleStop() {
			scheduledStop = true;
		}

		/**
		 * Metoda natychmiast zatrzymuje symulację.
		 */
		public void executeScheduledStop() {
			stopSimulation();
			scheduledStop = false;
		}

		/**
		 * Metoda faktycznie wykonywana jako każdy kolejny krok przez symulator.
		 * Szkielet oferowany przez StepPerformer automatycznie aktualizuje graficzną
		 * część symulacji na początku każdego kroku.
		 * @param event ActionEvent - zdarzenie, które spowodowało wykonanie metody 
		 */
		public void actionPerformed(ActionEvent event) {
			updateStep();
		}
	}

	/**
	 *  Klasa implementująca wykonywanie kroków dla najbardziej podstawowych trybów symulacji
	 *  LOOP oraz STEP. Dla trybu STEP na początku każdego kroku generowana jest lista tranzycji
	 *  faktycznie odpalanych spośród istniejących w sieci Petriego tranzycji aktywnych (dla
	 *  trybu pełnego - wszystkie aktywne tranzycje), a następnie symulowany jest proces
	 *  odpalenia tranzycji w 3 rozłącznych fazach dla każdej pozycji z listy równocześnie:<br><br>
	 *  faza odejmowania w której tokeny zostają zabrane z odpowiednich miejsc wejściowych<br>
	 *  faza graficznego dodawania - w której wyświetlone zostaje przejscie tokenów<br>
	 *  faza dodawania - w której tokeny zostaja dodane do odpowiednich miejsc wyjściowych
	 *  @author students
	 *  @author MR
	 */
	private class StepLoopPerformer extends SimulationPerformer {
		private boolean loop;

		/**
		 * Konstruktor bezparametrowy obiektu klasy StepPerformer
		 */
		public StepLoopPerformer() {
			loop = false;
		}

		/**
		 * Konstruktor obiektu klasy StepPerformer
		 * @param looping boolean - true, jeśli działanie w pętli
		 */
		public StepLoopPerformer(boolean looping) {
			loop = looping;
		}

		/**
		 * Metoda wykonywana jako kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, które spowodowało wykonanie metody 
		 */
		public void actionPerformed(ActionEvent event) {
			updateStep(); // rusz tokeny
			if (counter == DEFAULT_COUNTER && subtractPhase) { // jeśli trwa faza zabierania tokenów
				//z miejsc wejściowych i oddawania ich tranzycjom
				if (scheduledStop) { // jeśli symulacja ma się zatrzymać
					executeScheduledStop();
				} else if (isPossibleStep()) { // sprawdzanie, czy są aktywne tranzycje
					if (remainingTransitionsAmount == 0) {
						timeCounter++;
						GUIManager.getDefaultGUIManager().io.updateTimeStep(""+timeCounter);
						
						launchingTransitions = generateValidLaunchingTransitions();
						remainingTransitionsAmount = launchingTransitions.size();
					}
					
					//tutaj dodawany jest nowy krok w symulacji:
					if(getHistoryMode() == true) {
						actionStack.push(new SimulationStep(SimulatorMode.STEP, cloneTransitionArray(launchingTransitions)));
						if (actionStack.peek().getPendingTransitions() == null) {
							GUIManager.getDefaultGUIManager().log("Unknown problem in actionPerformed(ActionEvent event) in NetSimulator class.", "error", true);
						}
					}
				
					launchSubtractPhase(launchingTransitions, false); //zabierz tokeny poprzez aktywne tranzycje
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation ended, no active transitions.",
							"Simulation ended", JOptionPane.INFORMATION_MESSAGE);
				}
				counter = 0;
			} else if (counter == DEFAULT_COUNTER && !subtractPhase) { 
				// koniec fazy zabierania tokenów, tutaj realizowany jest graficzny przepływ tokenów
				launchAddPhaseGraphics(launchingTransitions, false);
				finishedAddPhase = false;
				counter = 0;
			} else if (counter == DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				// koniec fazy przepływu tokenów, tutaj uaktualniane są wartości tokenów dla miejsc wyjściowych
				launchAddPhase(launchingTransitions, false);
				finishedAddPhase = true;
				subtractPhase = true;
				remainingTransitionsAmount = 0; // all transitions launched
				// jeśli to nie tryb LOOP, zatrzymaj symulację
				if (!loop)
					scheduleStop();
				counter++;
			} else
				counter++; // empty steps
		}
	}

	/**
	 * Klasa implementująca wykonywanie kroków dla trybów symulacji SINGLE_TRANSITION oraz 
	 * SINGLE_TRANSITION_LOOP. Działa analogicznie do StepPerformer, za wyjątkiem 2 różnic:
	 * w danym kroku odpalana jest tylko jedna tranzycja z listy, po czym jest z niej usuwana;
	 * nowa lista generowana jest dopiero, gdy poprzednia zostanie opróżniona z tranzycji.
	 * SINGLE_TRANSITION odpowiada trybowi STEP w StepPerformer, SINGLE_TRANSITION_LOOP natomiast LOOP.
	 * @author students
	 *
	 */
	private class SingleTransitionPerformer extends SimulationPerformer {
		private boolean loop;

		/**
		 * Konstruktor bezparametrowy obiektu klasy SingleTransitionPerformer
		 */
		public SingleTransitionPerformer() {
			loop = false;
		}

		/**
		 * Konstruktor obiektu klasy SingleTransitionPerformer
		 * @param looping boolean - true, jeśli działanie w pętli
		 */
		public SingleTransitionPerformer(boolean looping) {
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako każdy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, które spowodowało wykonanie metody 
		 */
		public void actionPerformed(ActionEvent event) {
			updateStep(); // update graphics
			if (counter == DEFAULT_COUNTER && subtractPhase) { // subtract phase
				if (scheduledStop) { // executing scheduled stop
					executeScheduledStop();
				} else if (isPossibleStep()) { // if steps remaining
					if (remainingTransitionsAmount == 0) {
						timeCounter++;
						GUIManager.getDefaultGUIManager().io.updateTimeStep(""+timeCounter);
						launchingTransitions = generateValidLaunchingTransitions();
						remainingTransitionsAmount = launchingTransitions.size();
					}
					
					//tutaj dodawany jest nowy krok w symulacji:
					if(getHistoryMode() == true) {
						actionStack.push(new SimulationStep(SimulatorMode.SINGLE_TRANSITION, launchingTransitions.get(0),
							cloneTransitionArray(launchingTransitions)));
					}
					launchSingleSubtractPhase(launchingTransitions, false, null);
					
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation ended","No more available steps!",JOptionPane.INFORMATION_MESSAGE);
					GUIManager.getDefaultGUIManager().log("Simulation ended - no more available steps.", "text", true);
				}
				counter = 0;
			} else if (counter == DEFAULT_COUNTER && !subtractPhase) {
				// subtract phase ended, commencing add phase
				launchSingleAddPhaseGraphics(launchingTransitions, false, null);
				finishedAddPhase = false;
				counter = 0;
			} else if (counter == DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				// ending add phase
				launchSingleAddPhase(launchingTransitions, false, null);
				remainingTransitionsAmount = launchingTransitions.size();
				finishedAddPhase = true;
				subtractPhase = true;
				if (!loop) // if not in loop mode, a stop will be scheduled
					scheduleStop();
				counter++;
			} else
				counter++; // empty steps
		}
	}
	
	/**
	 * Klasa implementująca cofanie wykonanych w innych trybach kroków. Tryb ACTION_BACK
	 * wykonuje fazy analogiczne do StepPerformer lub SingleTransitionPerformer (zależnie od
	 * tego, jaki krok zostaje cofnięty), jednakże w odwróconej kolejności. Cofane tranzycje
	 * nie są losowane, lecz zdejmowane ze stosu historii wykonanych kroków. Tryb LOOP_BACK
	 * wykonuje kroki ACTION_BACK w pętli do czasu wyczerpania stosu historii (przywrócenia
	 * stanu globalnego początkowego sieci Petriego).
	 * @author students
	 *
	 */
	private class StepBackPerformer extends SimulationPerformer {
		private boolean loop;
		@SuppressWarnings("unused")
		ArrayList<Transition> currentTransitions;
		SimulationStep currentStep;

		/**
		 * Konstruktor bezparametrowy obiektu klasy StepBackPerformer.
		 */
		public StepBackPerformer() {
			currentTransitions = new ArrayList<Transition>();
			loop = false;
		}

		/**
		 * Konstruktor obiektu klasy StepBackPerformer.
		 * @param looping boolean - true, jeśli w pętli
		 */
		public StepBackPerformer(boolean looping) {
			currentTransitions = new ArrayList<Transition>();
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako każdy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, które spowodowało wykonanie metody 
		 */
		public void actionPerformed(ActionEvent event) {
			updateStep(); // update graphics
			if (counter == DEFAULT_COUNTER && subtractPhase) { // subtract phase
				if (scheduledStop) { // executing scheduled stop
					executeScheduledStop();
				} else if (!actionStack.empty()) { // if steps remaining
					
					//tutaj zdejmowany jest ostatni wykonany krok w symulacji:
					currentStep = actionStack.pop();
					if (currentStep.getType() == SimulatorMode.STEP) {
						launchSubtractPhase(currentStep.getPendingTransitions(), true);
					} else if (currentStep.getType() == SimulatorMode.SINGLE_TRANSITION) {
						launchSingleSubtractPhase(currentStep.getPendingTransitions(), true, currentStep.getLaunchedTransition());
					}
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Backtracking ended",
						"No more available actions to backtrack!", JOptionPane.INFORMATION_MESSAGE);
				}
				counter = 0;
			} else if (counter == DEFAULT_COUNTER && !subtractPhase) {
				// subtract phase ended, commencing add phase
				if (currentStep.getType() == SimulatorMode.STEP) {
					launchAddPhaseGraphics(currentStep.getPendingTransitions(), true);
				} else if (currentStep.getType() == SimulatorMode.SINGLE_TRANSITION) {
					launchSingleAddPhaseGraphics(currentStep.getPendingTransitions(), true, currentStep.getLaunchedTransition());
				}
				finishedAddPhase = false;
				counter = 0;
			} else if (counter == DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				// ending add phase
				if (currentStep.getType() == SimulatorMode.STEP) {
					launchAddPhase(currentStep.getPendingTransitions(), true);
				} else if (currentStep.getType() == SimulatorMode.SINGLE_TRANSITION) {
					launchSingleAddPhase(currentStep.getPendingTransitions(), true, currentStep.getLaunchedTransition());
				}
				finishedAddPhase = true;
				subtractPhase = true;
				// if not
				// in loop mode, a stop will be
				// scheduled
				if (!loop)
					scheduleStop();
				counter++;
			} else
				counter++; // empty steps
		}
	}
}
