package holmes.petrinet.simulators;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.functions.FunctionsTools;
import holmes.windows.HolmesNotepad;

/**
 * Klasa zajmująca się zarządzaniem całym procesem symulacji. Kiedyś nazywała się NetSimulator
 */
public class GraphicalSimulator {
	private SimulatorGlobals.SimNetType netSimType;
	private SimulatorMode simulatorStatus = SimulatorMode.STOPPED;
	private SimulatorMode previousSimStatus = SimulatorMode.STOPPED;
	private PetriNet petriNet;
	private int delay = 30;	//opóźnienie
	private boolean simulationActive = false;
	private Timer timer;
	private ArrayList<Transition> launchingTransitions;
	private Stack<SimulationStep> actionStack;
	
	private boolean maxMode = false;
	private boolean singleMode = false;
	
	private boolean writeHistory = true;
	private long timeCounter = -1;
	private GraphicalSimulatorLogger nsl = new GraphicalSimulatorLogger();
	//private Random generator;
	
	private SimulatorStandardPN engine;
	private boolean emptySteps = false; 
	
	private GUIManager overlord;
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
		LOOP, SINGLE_TRANSITION_LOOP, SINGLE_TRANSITION, STEP, STOPPED, PAUSED, ACTION_BACK, LOOP_BACK,
		XTPNLOOP
	}

	/**
	 * Konstruktor obiektu symulatora sieci.
	 * @param type (<b>SimulatorGlobals.SimNetType</b>) typ symulacji.
	 * @param net (<b>PetriNet</b>) sieć do symulacji .
	 */
	public GraphicalSimulator(SimulatorGlobals.SimNetType type, PetriNet net) {
		netSimType = type;
		petriNet = net;
		launchingTransitions = new ArrayList<Transition>();
		actionStack = new Stack<SimulationStep>(); //historia kroków
		engine = new SimulatorStandardPN();
		overlord = GUIManager.getDefaultGUIManager();
	}
	
	/**
	 * Reset do ustawień domyślnych.
	 */
	public void resetSimulator() {
		simulatorStatus = SimulatorMode.STOPPED;
		previousSimStatus = SimulatorMode.STOPPED;
		simulationActive = false;
		setMaxMode(false);
		setSingleMode(false);
		writeHistory = true;
		timeCounter = -1;
		actionStack.removeAllElements();
		//engine = new SimulatorStandardPN();
		engine.setEngine(netSimType, isMaxMode(), isSingleMode(), petriNet.getTransitions()
				, petriNet.getTimeTransitions(), petriNet.getPlaces());
		emptySteps = false;
	}

	/**
	 * Metoda rozpoczyna symulację w odpowiednim trybie. W zależności od wybranego trybu,
	 * w oddzielnym wątku symulacji inicjalizowana zostaje odpowiednia klasa dziedzicząca
	 * po StepPerformer.
	 * @param simulatorMode (<b>SimulatorMode</b>) wybrany tryb symulacji.
	 */
	public void startSimulation(SimulatorMode simulatorMode) {
		ArrayList<Transition> transitions = petriNet.getTransitions();
		ArrayList<Transition> time_transitions = petriNet.getTimeTransitions();
		ArrayList<Place> places = petriNet.getPlaces();
		nsl.logStart(netSimType, writeHistory, simulatorMode, isMaxMode());

		//setMaxMode(isSingleMode() && overlord.getSettingsManager().getValue("simSingleMode").equals("1"));

		HolmesNotepad notepad = new HolmesNotepad(640, 480);
		boolean status = FunctionsTools.validateFunctionNet(notepad, places);
		if(status)
			notepad.setVisible(true);
		else
			notepad.dispose();

		engine.setEngine(netSimType, isMaxMode(), isSingleMode(), transitions, time_transitions, places);

		nsl.logBackupCreated();

		previousSimStatus = getSimulatorStatus();
		setSimulatorStatus(simulatorMode);
		setSimulationActive(true);
		ActionListener taskPerformer = new SimulationPerformer();
		//ustawiania stanu przycisków symulacji:
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
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
		ArrayList<Transition> transitions = petriNet.getTransitions();
		
		if(netSimType == SimulatorGlobals.SimNetType.COLOR) { //kolorowane
			for (Transition transition : transitions) {
				if(transition instanceof TransitionColored) {
					if ( ((TransitionColored)transition).isColorActive()) { //ok
						return true;
					}
				} else {
					if (transition.isActive()) {
						return true;
					}
				}
			}
		} else {
			for (Transition transition : transitions) {
				if (transition.isActive()) {
					return true;
				}
				if(transition.getDPNtimer() >= 0 && transition.getDPNduration() != 0) {
					return true;
				}
					
			}
		}
		return false;
	}
	
	/**
	 * Metoda ustawiająca tryb sieci do symulacji.
	 * @param type int - typ sieci:<br> 0 - PN;<br> 1 - TPN;<br> 2 - Hybrid mode
	 * @return int - faktyczny ustawiony tryb: 0 - PN, 1 - TPN, 2 - Hybrid, 3 - Color, -1 : crash mode
	 */
	public int setGraphicalSimulatorNetType(int type) {
		//sprawdzenie poprawności trybu, zakładamy że BASIC działa zawsze
		int check = overlord.simSettings.checkSimulatorNetType(type);
		
		if(check == 0) {
			this.netSimType = SimulatorGlobals.SimNetType.BASIC;
			engine.setNetSimType(netSimType);
			return 0;
		} else if(check == 1) {
			this.netSimType = SimulatorGlobals.SimNetType.TIME;
			engine.setNetSimType(netSimType);
			return 1;
		} else if (check == 2) {
			this.netSimType = SimulatorGlobals.SimNetType.HYBRID;
			engine.setNetSimType(netSimType);
			return 2;
		} else if (check == 3) {
			this.netSimType = SimulatorGlobals.SimNetType.COLOR;
			engine.setNetSimType(netSimType);
			return 3;
		}
		return -1;
	}
	
	/**
	 * Zwraca rodzaj ustawionej sieci do symulacji (BASIC, TIMED, HYBRID).
	 * @return NetType - j.w.
	 */
	public SimulatorGlobals.SimNetType getSimNetType() {
		return netSimType;
	}
	
	/**
	 * Metoda podobna do setSimulatorNetType(...), sprawdza, czy aktualna sieć jest poprawna
	 * z punktu widzenia ustawionego trybu symulacji
	 */
	private void checkSimulatorNetType() {
		if(netSimType == SimulatorGlobals.SimNetType.BASIC) {
			boolean colorDetected = false;
			for(Node n : petriNet.getNodes()) {
				if(n instanceof Place) { 
					if(((Place)n).isColored)
						colorDetected = true;
					break;
				}
			}
			
			if(colorDetected) {
				JOptionPane.showMessageDialog(null, "Current net contains color places.\nSimulator switched to color mode.",
						"Invalid mode", JOptionPane.ERROR_MESSAGE);
				overlord.getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(3);
				netSimType = SimulatorGlobals.SimNetType.COLOR;
				engine.setNetSimType(netSimType);
			}
		} else if(netSimType == SimulatorGlobals.SimNetType.TIME) {
			for(Node n : petriNet.getNodes()) {
				if(n instanceof Place) { //miejsca ignorujemy
					continue;
				}
				
				if(n instanceof Transition) {
					if(!(((Transition)n).getTransType() == TransitionType.TPN)) {
						JOptionPane.showMessageDialog(null, "Current net is not pure Time Petri Net.\nSimulator switched to hybrid mode.",
								"Invalid mode", JOptionPane.ERROR_MESSAGE);
						overlord.getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(2);
						netSimType = SimulatorGlobals.SimNetType.HYBRID;
						engine.setNetSimType(netSimType);
					}
				}
			}
		} else if (netSimType == SimulatorGlobals.SimNetType.HYBRID) {
			//TODO
			// simulationType = NetType.HYBRID;
		}		
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
			
			if(transition.getDPNtimer() > 0) //yeah, trust me, I'm an engineer
				continue;
			
			// odejmij odpowiednią liczbę tokenów:
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
				Place place;
				if (!backtracking) { //inArcs
					place = (Place) arc.getStartNode();
					
					if(arc.getArcType() == TypeOfArc.INHIBITOR) {
						arc.setTransportingTokens(false);
						// nic nie zabieraj
					//} else if(arc.getArcType() == TypeOfArc.READARC) {
					//	arc.setTransportingTokens(false);
						// nic nie zabieraj
					} else if(arc.getArcType() == TypeOfArc.RESET) {
						int tokens = place.getTokensNumber();
						place.modifyTokensNumber(-tokens);
					} else if(arc.getArcType() == TypeOfArc.EQUAL) {
						place.modifyTokensNumber(-arc.getWeight());
					} else if(arc.getArcType() == TypeOfArc.COLOR) {
						try {
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(0), 0);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(1), 1);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(2), 2);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(3), 3);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(4), 4);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(5), 5);
						} catch (Exception ex) {
							overlord.log("GraphicalSimulator.launchSubtractPhase() failed to modify tokens. Probably not-colored place.", "error", true);
						}

					} else {
						FunctionsTools.functionalExtraction(transition, arc, place);
					}
				} else { //outArcs
					place = (Place) arc.getEndNode();
					if(arc.getArcType() == TypeOfArc.INHIBITOR) {
						arc.setTransportingTokens(false);
						// nic nie oddawaj
					//} else if(arc.getArcType() == TypeOfArc.READARC) {
					//	arc.setTransportingTokens(false);
						// nic nie oddawaj
					} else if(arc.getArcType() == TypeOfArc.RESET) {
						place.modifyTokensNumber(-1); 
						// PROBLEM, ten łuk nie jest odwracalny, skąd mamy wiedzieć, ile kiedyś-tam zabrano?!
					}  else if(arc.getArcType() == TypeOfArc.EQUAL) {
						place.modifyTokensNumber(-arc.getWeight());
					} else if(arc.getArcType() == TypeOfArc.COLOR) {
						try {
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(0), 0);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(1), 1);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(2), 2);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(3), 3);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(4), 4);
							((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(5), 5);
						} catch (Exception ex) {
							overlord.log("(2) GraphicalSimulator.launchSubtractPhase() failed to modify tokens. Probably not-colored place.", "error", true);
						}
					} else {
						FunctionsTools.functionalExtraction(transition, arc, place);
					}
				} // if (backtracking == false)
				
			} //for (Arc arc : arcs)
		} //for (Transition transition : transitions)
	}

	/**
	 * Metoda uruchamia fazę odejmowania tokenów z miejsc wejściowych jednej odpalonej tranzycji
	 * (lub wyjściowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 */
	public void launchSingleSubtractPhase(ArrayList<Transition> transitions, boolean backtracking, Transition chosenTransition) {
		if (transitions.size() < 1)
			return;

		Transition transition;
		ArrayList<Arc> arcs;
		if (!backtracking) {
			transition = transitions.get(0);
			arcs = transition.getInArcs();
		} else {
			transition = chosenTransition;
			arcs = transition.getOutArcs();
		}
		transition.setLaunching(true);
		for (Arc arc : arcs) {
			arc.setSimulationForwardDirection(!backtracking);
			arc.setTransportingTokens(true);
			Place place;

			if (!backtracking) { //inArcs
				place = (Place) arc.getStartNode();

				if(arc.getArcType() == TypeOfArc.INHIBITOR) {
					arc.setTransportingTokens(false);
					// nic nie zabieraj
				//} else if(arc.getArcType() == TypeOfArc.READARC) {
				//	arc.setTransportingTokens(false);
					// nic nie zabieraj
				} else if(arc.getArcType() == TypeOfArc.RESET) {
					int tokens = place.getTokensNumber();
					place.modifyTokensNumber(-tokens);
				} else if(arc.getArcType() == TypeOfArc.EQUAL) {
					place.modifyTokensNumber(-arc.getWeight());
				} else if(arc.getArcType() == TypeOfArc.COLOR) {
					try {
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(0), 0);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(1), 1);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(2), 2);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(3), 3);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(4), 4);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(5), 5);
					} catch (Exception ex) {
						overlord.log("GraphicalSimulator.launchSingleSubtractPhase() failed to modify tokens. Probably not-colored place.", "error", true);
					}
				} else {
					FunctionsTools.functionalExtraction(transition, arc, place);
					//place.modifyTokensNumber(-arc.getWeight());
				}
			} else { //outArcs
				place = (Place) arc.getEndNode();
				if(arc.getArcType() == TypeOfArc.INHIBITOR) {
					arc.setTransportingTokens(false);
					// nic nie oddawaj
				//} else if(arc.getArcType() == TypeOfArc.READARC) {
				//	arc.setTransportingTokens(false);
					// nic nie oddawaj
				} else if(arc.getArcType() == TypeOfArc.RESET) {
					place.modifyTokensNumber(-1);
					// PROBLEM, ten łuk nie jest odwracalny, skąd mamy wiedzieć, ile kiedyś-tam zabrano?!
				} else if(arc.getArcType() == TypeOfArc.EQUAL) {
					place.modifyTokensNumber(-arc.getWeight());
				} else if(arc.getArcType() == TypeOfArc.COLOR) {
					try {
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(0), 0);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(1), 1);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(2), 2);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(3), 3);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(4), 4);
						((PlaceColored)place).modifyColorTokensNumber(-arc.getColorWeight(5), 5);
					} catch (Exception ex) {
						overlord.log("(2) GraphicalSimulator.launchSingleSubtractPhase() failed to modify tokens. Probably not-colored place.", "error", true);
					}
				} else {
					FunctionsTools.functionalExtraction(transition, arc, place);
					//place.modifyTokensNumber(-arc.getWeight());
				}
			} // if (backtracking == false)
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
				//if(arc.getArcType() == TypeOfArc.INHIBITOR || arc.getArcType() == TypeOfArc.READARC)
				if(arc.getArcType() == TypeOfArc.INHIBITOR )
					continue;
				
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
	 */
	public void launchSingleAddPhaseGraphics( ArrayList<Transition> transitions, boolean backtracking, Transition chosenTransition) {
		if (transitions.size() < 1)
			return;

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
			//if(arc.getArcType() == TypeOfArc.INHIBITOR || arc.getArcType() == TypeOfArc.READARC)
			if(arc.getArcType() == TypeOfArc.INHIBITOR)
				continue;

			arc.setSimulationForwardDirection(!backtracking);
			arc.setTransportingTokens(true);
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
				//if(arc.getArcType() == TypeOfArc.READARC)
				//	;//continue;
				
				Place place;
				if (!backtracking)
					place = (Place) arc.getEndNode();
				else
					place = (Place) arc.getStartNode();
				
				if(arc.getArcType() != TypeOfArc.NORMAL && arc.getArcType() != TypeOfArc.COLOR && arc.getArcType() != TypeOfArc.READARC) { //!!!!!! było bez drugiego członu po ||
					overlord.log("Error: non-standard arc used to produce tokens: "+place.getName()+ " arc: "+arc, "warning", true);
				}
				
				if(arc.getArcType() == TypeOfArc.COLOR && place.isColored) {
					try {
						((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(0), 0);
						((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(1), 1);
						((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(2), 2);
						((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(3), 3);
						((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(4), 4);
						((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(5), 5);
					} catch (Exception ex) {
						overlord.log("GraphicalSimulator.launchAddPhase() failed to modify tokens. Probably not-colored place.", "error", true);
					}
				} else {
					//tylko zwykły łuk
					FunctionsTools.functionalAddition(transition, arc, place);
					//place.modifyTokensNumber(arc.getWeight());
				}
					
			}
			transition.resetTimeVariables();
		}
		transitions.clear();  //wyczyść listę tranzycji 'do uruchomienia' (już swoje zrobiły)
	}

	/**
	 * Metoda uruchamia fazę faktycznego dodawania tokenów do miejsc wyjściowych dla pojedynczej
	 * spośród odpalanych tranzycji (lub wejściowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeśli symulator pracuje w trybie cofania; false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 */
	public void launchSingleAddPhase(ArrayList<Transition> transitions, boolean backtracking, Transition chosenTransition) {
		if (transitions.size() < 1)
			return;

		Transition transition;
		ArrayList<Arc> arcs;
		if (!backtracking) {
			transition = transitions.get(0);
			arcs = transition.getOutArcs();
		} else {
			transition = chosenTransition;
			arcs = transition.getInArcs();
		}
		for (Arc arc : arcs) {
			if(arc.getArcType() == TypeOfArc.READARC)
				continue;

			Place place;
			if (!backtracking)
				place = (Place) arc.getEndNode();
			else
				place = (Place) arc.getStartNode();

			if(arc.getArcType() != TypeOfArc.NORMAL && arc.getArcType() != TypeOfArc.COLOR && arc.getArcType() != TypeOfArc.READARC) { //!!!!!! było bez drugiego członu po ||
				overlord.log("Error: non-standard arc used to produce tokens: "+place.getName()+ " arc: "+arc, "warning", true);
			}

			if(arc.getArcType() == TypeOfArc.COLOR && place.isColored) {
				try {
					((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(0), 0);
					((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(1), 1);
					((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(2), 2);
					((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(3), 3);
					((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(4), 4);
					((PlaceColored)place).modifyColorTokensNumber(arc.getColorWeight(5), 5);
				} catch (Exception ex) {
					overlord.log("GraphicalSimulator.launchSingleAddPhase() failed to modify tokens. Probably not-colored place.", "error", true);
				}
			} else {
				//tylko zwykły łuk
				FunctionsTools.functionalAddition(transition, arc, place);
				//place.modifyTokensNumber(arc.getWeight());
			}
		}
		transitions.remove(transition);
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
		if ((getSimulatorStatus() != SimulatorMode.PAUSED) && (getSimulatorStatus() != SimulatorMode.STOPPED)) {
			pauseSimulation();
		} else if (getSimulatorStatus() == SimulatorMode.PAUSED) {
			unpauseSimulation();
		} else if (getSimulatorStatus() == SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
				"Can't pause a stopped simulation!", "The simulator is already stopped!", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za zatrzymanie symulacji.
	 */
	private void stopSimulation() {
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationInitiateButtons();
		timer.stop();
		previousSimStatus = simulatorStatus;
		setSimulatorStatus(SimulatorMode.STOPPED);

		setSimulationActive(false);
		
		nsl.logSimStopped(timeCounter);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za pauzowanie symulacji.
	 */
	private void pauseSimulation() {
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlyUnpauseButton();
		timer.stop();
		previousSimStatus = simulatorStatus;
		setSimulatorStatus(SimulatorMode.PAUSED);
		
		nsl.logSimPause(true);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za ponowne uruchomienie (po pauzie) symulacji.
	 */
	private void unpauseSimulation() {
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
		if (previousSimStatus != SimulatorMode.STOPPED) {
			timer.start();
			setSimulatorStatus(previousSimStatus);
		}
		
		nsl.logSimPause(false);
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
		overlord.getWorkspace().getProject().setSimulationActive(isSimulationActive());
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
	public int getDelay() {
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
	@SuppressWarnings("unused")
	public void setHistoryMode(boolean status) {
		this.writeHistory = status;
	}
	
	/**
	 * Metoda pozwala okreslić, czy zapisywana jest historia stanów symulacji.
	 * @return boolean - true, jeśli symulator zapisuje historię, false w przeciwnym wypadku
	 */
	public boolean isHistoryMode() {
		return writeHistory;
	}
	
	/**
	 * Metoda zwraca wartość aktualnego kroku symulacji (numer, tj. czas).
	 * @return long - nr kroku symulacji
	 */
	public long getSimulatorTimeStep() {
		return timeCounter;
	}
	
	/**
	 * Metoda zwraca obiekt zapisujący informacje o symulacji.
	 * @return NetSimulatorLogger - obiekt komunikatora
	 */
	public GraphicalSimulatorLogger getSimLogger() {
		return nsl;
	}

	private ArrayList<Transition> cloneTransitionArray(ArrayList<Transition> transitions) {
		return new ArrayList<>(transitions);
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
	public boolean isMaxMode() {
		return maxMode;
	}

	/**
	 * Metoda ustawiająca tryb maksymalnego uruchamiania tranzycji.
	 */
	public void setMaxMode(boolean value) {
		this.maxMode = value;
		this.engine.setMaxMode(value);
	}
	
	/**
	 * Tryb odpalenia tylko 1 tranzycji - status.
	 * @return boolean - true, jeśli tylko 1 ma odpalać
	 */
	public boolean isSingleMode() {
		return singleMode;
	}
	
	/**
	 * Metoda ustawia tryb pojedynczego odpalania tranzycji.
	 * @param value boolean - true, jeśli tylko 1 tranzycja ma odpalać na turę
	 */
	public void setSingleMode(boolean value) {
		this.singleMode = value;
		this.engine.setSingleMode(value);
	}

	/**
	 * Po tej klasie dziedziczy szereg klas implementujących konkretne tryby pracy symulatora.
	 * Metoda actionPerformed() jest wykonywana w każdym kroku symulacji przez timer obiektu
	 * NetSimulator. 
	 * @author students
	 *
	 */
	private class SimulationPerformer implements ActionListener {
		protected int transitionDelay = overlord.simSettings.getTransitionGraphicDelay(); // licznik kroków graficznych
		protected boolean subtractPhase = true; // true - subtract, false - add
		protected boolean finishedAddPhase = true;
		protected boolean scheduledStop = false;
		protected int remainingTransitionsAmount = launchingTransitions.size();

		/**
		 * Metoda aktualizuje wyświetlanie graficznej części symulacji po wykonaniu każdego kroku.
		 */
		protected void updateStepCounter() {
			overlord.getWorkspace().getProject().incrementGraphicalSimulationStep();
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
			updateStepCounter();
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
			int DEFAULT_COUNTER = overlord.simSettings.getTransitionGraphicDelay();
			updateStepCounter(); // rusz tokeny
			if (transitionDelay >= DEFAULT_COUNTER && subtractPhase) { // jeśli trwa faza zabierania tokenów
				//z miejsc wejściowych i oddawania ich tranzycjom
				if (scheduledStop) { // jeśli symulacja ma się zatrzymać
					executeScheduledStop();
				} else if (isPossibleStep()) { // sprawdzanie, czy są aktywne tranzycje
					if (remainingTransitionsAmount == 0) {
						timeCounter++;
						overlord.io.updateTimeStep(false, timeCounter, 0);
						overlord.simSettings.currentStep = timeCounter;
						
						launchingTransitions = engine.getTransLaunchList(emptySteps);
						remainingTransitionsAmount = launchingTransitions.size();
					}
					
					//tutaj dodawany jest nowy krok w symulacji:
					if(isHistoryMode()) {
						actionStack.push(new SimulationStep(SimulatorMode.STEP, cloneTransitionArray(launchingTransitions)));
						if (actionStack.peek().getPendingTransitions() == null) {
							overlord.log("Unknown problem in actionPerformed(ActionEvent event) in NetSimulator class.", "error", true);
						}
					}
				
					launchSubtractPhase(launchingTransitions, false); //zabierz tokeny poprzez aktywne tranzycje
					//usuń te tranzycje, które są w I fazie DPN
					for(int t=0; t<launchingTransitions.size(); t++) {
						Transition test_t = launchingTransitions.get(t);
						if(test_t.getDPNstatus()) {
							if(test_t.getDPNtimer() == 0 && test_t.getDPNduration() != 0) {
								launchingTransitions.remove(test_t);
								t--;
							}
						}
					}
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation stopped, no active transitions.",
							"Simulation stopped", JOptionPane.INFORMATION_MESSAGE);
				}
				transitionDelay = 0;
			} else if (transitionDelay >= DEFAULT_COUNTER && !subtractPhase) { 
				// koniec fazy zabierania tokenów, tutaj realizowany jest graficzny przepływ tokenów
				launchAddPhaseGraphics(launchingTransitions, false);
				finishedAddPhase = false;
				transitionDelay = 0;
			} else if (transitionDelay >= DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				boolean detailedLogging = true;
				nsl.logSimStepFinished(launchingTransitions, detailedLogging);
				
				// koniec fazy przepływu tokenów, tutaj uaktualniane są wartości tokenów dla miejsc wyjściowych
				launchAddPhase(launchingTransitions, false);
				finishedAddPhase = true;
				subtractPhase = true;
				remainingTransitionsAmount = 0; // all transitions launched
				// jeśli to nie tryb LOOP, zatrzymaj symulację
				if (!loop)
					scheduleStop();
				transitionDelay++;
			} else
				transitionDelay++; // empty steps
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
			int DEFAULT_COUNTER = overlord.simSettings.getTransitionGraphicDelay();
			updateStepCounter(); // update graphics
			if (transitionDelay >= DEFAULT_COUNTER && subtractPhase) { // jeśli trwa faza zabierania tokenów
				//z miejsc wejściowych i oddawania ich tranzycjom
				if (scheduledStop) { // jeśli symulacja ma się zatrzymać
					executeScheduledStop();
				} else if (isPossibleStep()) { // sprawdzanie, czy są aktywne tranzycje
					if (remainingTransitionsAmount == 0) {
						timeCounter++;
						overlord.io.updateTimeStep(false, timeCounter, 0);
						overlord.simSettings.currentStep = timeCounter;
						
						launchingTransitions = engine.getTransLaunchList(emptySteps);
						remainingTransitionsAmount = launchingTransitions.size();
					}
					
					//tutaj dodawany jest nowy krok w symulacji:
					if(isHistoryMode()) {
						actionStack.push(new SimulationStep(SimulatorMode.SINGLE_TRANSITION, launchingTransitions.get(0),
							cloneTransitionArray(launchingTransitions)));
					}
					launchSingleSubtractPhase(launchingTransitions, false, null);
					
					for(int t=0; t<launchingTransitions.size(); t++) {
						Transition test_t = launchingTransitions.get(t);
						if(test_t.getDPNstatus()) {
							if(test_t.getDPNtimer() == 0 && test_t.getDPNduration() != 0) {
								launchingTransitions.remove(test_t);
								t--;
							}
						}
					}
					
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation stopped, no active transitions.",
							"Simulation stopped",JOptionPane.INFORMATION_MESSAGE);
					overlord.log("Simulation ended - no more available steps.", "text", true);
				}
				transitionDelay = 0;
			} else if (transitionDelay >= DEFAULT_COUNTER && !subtractPhase) {
				// subtract phase ended, commencing add phase
				launchSingleAddPhaseGraphics(launchingTransitions, false, null);
				finishedAddPhase = false;
				transitionDelay = 0;
			} else if (transitionDelay >= DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				//nsl.logSimStepFinished(launchingTransitions, detailedLogging);
				
				// ending add phase
				launchSingleAddPhase(launchingTransitions, false, null);
				remainingTransitionsAmount = launchingTransitions.size();
				finishedAddPhase = true;
				subtractPhase = true;
				if (!loop) // if not in loop mode, a stop will be scheduled
					scheduleStop();
				transitionDelay++;
			} else
				transitionDelay++; // empty steps
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
			int DEFAULT_COUNTER = overlord.simSettings.getTransitionGraphicDelay();
			updateStepCounter(); // update graphics
			if (transitionDelay >= DEFAULT_COUNTER && subtractPhase) { // subtract phase
				if (scheduledStop) { // executing scheduled stop
					executeScheduledStop();
				} else if (!actionStack.empty()) { // if steps remaining
					timeCounter--;
					overlord.io.updateTimeStep(false,timeCounter, 0);
					overlord.simSettings.currentStep = timeCounter;
					
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
				transitionDelay = 0;
			} else if (transitionDelay >= DEFAULT_COUNTER && !subtractPhase) {
				// subtract phase ended, commencing add phase
				if (currentStep.getType() == SimulatorMode.STEP) {
					launchAddPhaseGraphics(currentStep.getPendingTransitions(), true);
				} else if (currentStep.getType() == SimulatorMode.SINGLE_TRANSITION) {
					launchSingleAddPhaseGraphics(currentStep.getPendingTransitions(), true, currentStep.getLaunchedTransition());
				}
				finishedAddPhase = false;
				transitionDelay = 0;
			} else if (transitionDelay >= DEFAULT_COUNTER - 5 && !finishedAddPhase) {
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
				transitionDelay++;
			} else
				transitionDelay++; // empty steps
		}
	}
}
