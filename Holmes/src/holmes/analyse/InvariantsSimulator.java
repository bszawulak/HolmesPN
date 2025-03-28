package holmes.analyse;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.InvariantTransition;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.SimulationStep;

/**
 * Klasa odpowiadzialna za symulację wykonywania inwariantów w sieci.
 */
public class InvariantsSimulator {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private NetType simulationType;
	private SimulatorMode mode;
	private SimulatorMode previousMode = SimulatorMode.STOPPED;
	private SimulatorType simType = SimulatorType.TIME;

	private PetriNet petriNet;
	private Integer delay = 0;
	private boolean simulationActive = false;
	private Timer timer;
	private ArrayList<Transition> launchingTransitions;

	private Stack<SimulationStep> actionStack;

	private boolean maximumMode = false;

	public static int DEFAULT_COUNTER = 50;
	public int stepValue;

	public JFrame timeFrame = new JFrame("Zegar");

	public double timeNetStepCounter = 0;
	public double timeNetPartStepCounter = 0;

	private ArrayList<ArrayList<InvariantTransition>> invariants;
	private ArrayList<Integer[]> generatedInvariants;
	private ArrayList<Integer> foundInvariants = new ArrayList<Integer>();
	private ArrayList<Integer> lastTransition = new ArrayList<Integer>();
	
	private InvariantsWriter invariantsWriter = new InvariantsWriter();

	/**
	 * LOOP<br>
	 * SINGLE_TRANSITION_LOOP<br>
	 * SINGLE_TRANSITION<br>
	 * STEP<br>
	 * STOPPED<br>
	 * PAUSED<br>
	 * ACTION_BACK<br>
	 * LOOP_BACK
	 */
	public enum SimulatorMode {
		LOOP, SINGLE_TRANSITION_LOOP, SINGLE_TRANSITION, STEP, STOPPED, PAUSED, ACTION_BACK, LOOP_BACK
	}

	/**
	 * BASIC, COLORED, TIME
	 */
	public enum NetType {
		BASIC, COLORED, TIME
	}
	
	/**
	 * TIME, CYCLE, STEP
	 */
	public enum SimulatorType {
		TIME, CYCLE, STEP
	}

	/**
	 * Konstruktor obiektu klasy InvariantsSimulator.
	 * @param type NetType - rodzaj sieci
	 * @param net PetriNet - sieć w reprezentacji wewnętrznej
	 * @param inv ArrayList[ArrayList[InvariantTransition]] - macierz inwariantów
	 * @param st int - rodzaj symulacji
	 * @param simV int - krok
	 */
	public InvariantsSimulator(NetType type, PetriNet net, ArrayList<ArrayList<InvariantTransition>> inv, int st, int simV) {
		simulationType = type;
		petriNet = net;
		invariants = inv;
		mode = SimulatorMode.LOOP;
		if(st==0)
			simType = SimulatorType.TIME;
		if(st==1)
			simType = SimulatorType.STEP;
		if(st==2)
			simType = SimulatorType.CYCLE;
			
		stepValue = simV;

		launchingTransitions = new ArrayList<Transition>();
		actionStack = new Stack<SimulationStep>();
		
		clearGenInvariants();
	}

	/**
	 * Metoda sprawdza, czy krok jest możliwy, tj. czy choć jedna tranzycja jest aktualnie
	 * aktywna.
	 * @return boolean - true, jeśli choć jedna tranzycja jest w danej chwili aktywna
	 */
	private boolean isPossibleStep() {
		for (Transition transition : petriNet.getTransitions()) {
			if (transition.isActive())
				return true;
		}
		return false;
	}

	/**
	 * Metoda ustawiająca model sieci.
	 * @param type int - 0 - BASIC, 1 - TIME
	 */
	public void setSimulatorNetType(int type) {
		switch (type) {
			case (0) -> simulationType = NetType.BASIC;
			case (1) -> simulationType = NetType.TIME;
		}

	}

	/**
	 * Metoda rozpoczynająca symulację inwariantów.
	 * @param simulatorMode SimulatorMode - tryb pracy symulatora
	 */
	@SuppressWarnings("incomplete-switch")
	public void startSimulation(SimulatorMode simulatorMode) {
		timeFrame.setBounds(185, 115, 80, 30);
		timeFrame.getContentPane().add(
				new JLabel(String.valueOf(timeNetStepCounter)),
				BorderLayout.CENTER);
		timeFrame.pack();
		timeFrame.setVisible(true);

		previousMode = getMode();
		setMode(simulatorMode);
		setSimulationActive(true);
		ActionListener taskPerformer = new SimulationPerformer();
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
		//overlord.getShortcutsBar().allowOnlySimulationDisruptButtons();
		switch (getMode()) {
			case LOOP -> taskPerformer = new StepPerformer(true, simType, stepValue);
			case SINGLE_TRANSITION_LOOP -> taskPerformer = new SingleTransitionPerformer(true);
			case SINGLE_TRANSITION -> taskPerformer = new SingleTransitionPerformer();
			case STEP -> taskPerformer = new StepPerformer();
			case ACTION_BACK -> taskPerformer = new StepBackPerformer();
			case LOOP_BACK -> {
				launchingTransitions.clear();
				taskPerformer = new StepBackPerformer(true);
			}
		}
		setTimer(new Timer(getDelay(), taskPerformer));
		getTimer().start();
	}

	/**
	 * Metoda weryfikująca zbiór tranzycji do uruchomienia.
	 * @return ArrayList[Transition] - zbiór tranzycji
	 */
	private ArrayList<Transition> generateValidLaunchingTransitions() {
		boolean generated = false;
		ArrayList<Transition> launchingTransitions = new ArrayList<Transition>();
		while (!generated) {
			launchingTransitions = generateLaunchingTransitions();
			if (launchingTransitions.size() > 0)
				generated = true;
		}
		return launchingTransitions;
	}

	/**
	 * Metoda generująca zbiór tranzycji do uruchomienia.
	 * @return ArrayList[Transition] - zbiór tranzycji
	 */
	private ArrayList<Transition> generateLaunchingTransitions() {
		Random randomLaunch = new Random();
		ArrayList<Transition> launchableTransitions = new ArrayList<Transition>();
		ArrayList<Transition> allTransitions = petriNet.getTransitions();
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		int i = 0;
		for (Transition ignored : allTransitions) {
			indexList.add(i);
			i++;
		}
		Collections.shuffle(indexList);
		if (simulationType == NetType.BASIC)
			for (i = 0; i < allTransitions.size(); i++) {
				Transition transition = allTransitions.get(indexList.get(i));
				if (transition.isActive())
					if ((randomLaunch.nextInt(100) < 50) || maximumMode) {
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
			}

		/*
		if (simulationType == NetType.TIME) {
			for (i = 0; i < allTransitions.size(); i++) {
				Transition transition = allTransitions.get(indexList.get(i));
				if (transition.getFireTime() == -1)
					transition.setFireTime(timeNetStepCounter);

				if (transition.isActive()) {
					boolean deadLineTime = false;
					double tmp1 = transition.getMinFireTime()
							+ transition.getFireTime();
					double tmp2 = (timeNetPartStepCounter / 1000)
							+ timeNetStepCounter;
					if (tmp1 <= tmp2) {
						if (tmp2 >= transition.getMaxFireTime())
							deadLineTime = true;
						// calkowite
						if ((randomLaunch.nextInt(1000) < 4) || maximumMode
								|| deadLineTime) {
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

			// timeFrame.repaint();
			if (timeNetPartStepCounter == 1000) {
				this.timeNetPartStepCounter = 0;
				this.timeNetStepCounter++;

			}
			timeFrame.getContentPane().remove(0);
			timeFrame.getContentPane().add(
					new JLabel(
							String.valueOf((int) timeNetStepCounter)
									+ String.valueOf("."
											+ (int) timeNetPartStepCounter)),
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
	 * 
	 * @param t (<b>Transition</b>)
	 * @param list (<b>ArrayList[Transition]</b>)
	 * @return (<b>ArrayList[Transition]</b>)
	 */
	public ArrayList<Transition> searchConflict(Transition t, ArrayList<Transition> list) {
		return null;
	}

	/**
	 * Metoda uruchamiająca fazę pobierania substratów dla tranzycji.
	 * @param transitions ArrayList[Transition] - zbiór tranzycji
	 * @param backtracking boolean - true, jeśli symulacja się cofa
	 */
	public void launchSubtractPhase(ArrayList<Transition> transitions,
			boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			transition.setLaunching(true);
			if (!backtracking)
				arcs = transition.getInputArcs();
			else
				arcs = transition.getOutputArcs();
			// subtract adequate number of tokens from origins
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
				Place place;
				if (!backtracking)
					place = (Place) arc.getStartNode();
				else
					place = (Place) arc.getEndNode();
				place.addTokensNumber(-arc.getWeight());
			}
		}
	}

	/**
	 * Metoda uruchamiająca pojedynczą fazę pobrania substratów.
	 * @param transitions ArrayList[Transition] - zbiór tranzycji
	 * @param backtracking boolean - true, jeśli symulacja się cofa
	 * @param chosenTransition Transition - wybrana tranzycja
	 * @return boolean - zwraca true, jeśli się udało coś odpalić
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
				arcs = tran.getInputArcs();
			} else {
				tran = chosenTransition;
				arcs = tran.getOutputArcs();
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
				place.addTokensNumber(-arc.getWeight());
			}
			return true;
		}
	}

	/**
	 * 
	 * @param transitions (<b>ArrayList[Transition]</b>)
	 * @param backtracking (<b>boolean</b>)
	 */
	public void launchAddPhaseGraphics(ArrayList<Transition> transitions, boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition tran : transitions) {
			tran.setLaunching(true);
			if (!backtracking)
				arcs = tran.getOutputArcs();
			else
				arcs = tran.getInputArcs();
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
			}
		}
	}

	public boolean launchSingleAddPhaseGraphics(
			ArrayList<Transition> transitions, boolean backtracking,
			Transition chosenTransition) {
		if (transitions.size() < 1)
			return false;
		else {
			Transition tran;
			ArrayList<Arc> arcs;
			if (!backtracking) {
				tran = transitions.get(0);
				arcs = tran.getOutputArcs();
			} else {
				tran = chosenTransition;
				arcs = tran.getInputArcs();
			}
			tran.setLaunching(true);
			for (Arc arc : arcs) {
				arc.setSimulationForwardDirection(!backtracking);
				arc.setTransportingTokens(true);
			}
			return true;
		}
	}

	public void launchAddPhase(ArrayList<Transition> transitions,
			boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			if (!backtracking)
				arcs = transition.getOutputArcs();
			else
				arcs = transition.getInputArcs();
			// add adequate number of tokens to destinations
			for (Arc arc : arcs) {
				Place place;
				if (!backtracking)
					place = (Place) arc.getEndNode();
				else
					place = (Place) arc.getStartNode();
				place.addTokensNumber(arc.getWeight());
			}
		}
		transitions.clear();
	}

	public boolean launchSingleAddPhase(ArrayList<Transition> transitions,
			boolean backtracking, Transition chosenTransition) {
		if (transitions.size() < 1)
			return false;
		else {
			Transition tran;
			ArrayList<Arc> arcs;
			if (!backtracking) {
				tran = transitions.get(0);
				arcs = tran.getOutputArcs();
			} else {
				tran = chosenTransition;
				arcs = tran.getInputArcs();
			}
			for (Arc arc : arcs) {
				Place place;
				if (!backtracking)
					place = (Place) arc.getEndNode();
				else
					place = (Place) arc.getStartNode();
				place.addTokensNumber(arc.getWeight());
			}
			transitions.remove(tran);
			return true;
		}
	}

	public void stop() {
		((SimulationPerformer) getTimer().getActionListeners()[0])
				.scheduleStop();
	}

	public void pause() {
		if ((getMode() != SimulatorMode.PAUSED)
				&& (getMode() != SimulatorMode.STOPPED))
			pauseSimulation();
		else if (getMode() == SimulatorMode.PAUSED)
			unpauseSimulation();
		else if (getMode() == SimulatorMode.STOPPED)
			JOptionPane.showMessageDialog(null,
					"Can't pause a stopped simulation!",
					"The simulator is already stopped!",
					JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Metoda obsługująca wciśnięcie przycisku zatrzymania symulacji.
	 */
	private void stopSimulation() {
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationInitiateButtons();
		//overlord.getShortcutsBar().allowOnlySimulationInitiateButtons();
		timer.stop();
		previousMode = mode;
		setMode(SimulatorMode.STOPPED);
	}

	private void pauseSimulation() {
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlyUnpauseButton();
		//overlord.getShortcutsBar().allowOnlyUnpauseButton();
		timer.stop();
		previousMode = mode;
		setMode(SimulatorMode.PAUSED);
	}

	private void unpauseSimulation() {
		overlord.getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
		//overlord.getShortcutsBar().allowOnlySimulationDisruptButtons();
		if (previousMode != SimulatorMode.STOPPED) {
			timer.start();
			setMode(previousMode);
		}
	}

	public boolean isSimulationActive() {
		return simulationActive;
	}

	public void setSimulationActive(boolean simulationActive) {
		this.simulationActive = simulationActive;
		overlord.getWorkspace().getProject().setSimulationActive(isSimulationActive());
	}

	public int getNodesAmount() {
		return petriNet.getNodes().size();
	}

	public int getPlacesAmount() {
		return petriNet.getPlaces().size();
	}

	public int getTransitionsAmount() {
		return petriNet.getTransitions().size();
	}

	public int getArcsAmount() {
		return petriNet.getArcs().size();
	}

	public int getTokensAmount() {
		int tokenAmount = 0;
		for (Place place : petriNet.getPlaces()) {
			tokenAmount += place.getTokensNumber();
		}
		return tokenAmount;
	}

	public Timer getTimer() {
		return timer;
	}

	private void setTimer(Timer timer) {
		this.timer = timer;
	}

	public Integer getDelay() {
		return delay;
	}

	public void setDelay(Integer delay) {
		if (timer != null)
			timer.setDelay(delay);
		this.delay = delay;
	}

	public SimulatorMode getMode() {
		return mode;
	}

	public void setMode(SimulatorMode mode) {
		this.mode = mode;
	}

	private ArrayList<Transition> cloneTransitionArray(
			ArrayList<Transition> transitions) {
		ArrayList<Transition> newArray = new ArrayList<Transition>();
		for (Transition transition : transitions)
			newArray.add(transition);
		return newArray;
	}

	// ================================================================================
	// simulation task performer classes
	// ================================================================================

	public boolean isMaximumMode() {
		return maximumMode;
	}

	public void setMaximumMode(boolean maximumMode) {
		this.maximumMode = maximumMode;
	}

	// template class for all performers
	public class SimulationPerformer implements ActionListener {
		protected int counter = DEFAULT_COUNTER;
		protected boolean subtractPhase = true; // true - subtract, false - add
		// phases
		protected boolean finishedAddPhase = true;
		protected boolean scheduledStop = false;

		protected int remainingTransitionsAmount = launchingTransitions.size();

		protected void updateStep() {
			overlord.getWorkspace().getProject().incrementGraphicalSimulationStep();

			//tutaj nic si� nie dzieje: a chyba chodzi�o o update podokna w�a�ciwo�ci z liczb� token�w
			//overlord.getSimulatorBox().updateSimulatorProperties();
		}

		public void scheduleStop() {
			scheduledStop = true;
		}

		public void executeScheduledStop() {
			stopSimulation();
			scheduledStop = false;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			updateStep();
		}
	}

	// performer for LOOP_BACK, ACTION_BACK
	public class StepBackPerformer extends SimulationPerformer {
		private boolean loop;
		ArrayList<Transition> currentTransitions;
		SimulationStep currentStep;

		public StepBackPerformer() {
			currentTransitions = new ArrayList<Transition>();
			loop = false;
		}

		public StepBackPerformer(boolean looping) {
			currentTransitions = new ArrayList<Transition>();
			loop = looping;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			updateStep(); // update graphics
			if (counter == DEFAULT_COUNTER && subtractPhase) { // subtract phase
				if (scheduledStop) { // executing scheduled stop
					executeScheduledStop();
				} else if (!actionStack.empty()) { // if steps remaining
					currentStep = actionStack.pop();

					if (currentStep.getType() == GraphicalSimulator.SimulatorMode.STEP) {
						launchSubtractPhase(
								currentStep.getPendingTransitions(), true);
					} else if (currentStep.getType() == GraphicalSimulator.SimulatorMode.SINGLE_TRANSITION) {
						launchSingleSubtractPhase(
								currentStep.getPendingTransitions(), true,
								currentStep.getLaunchedTransition());
					}
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Backtracking ended", "No more available actions to backtrack!",
							JOptionPane.INFORMATION_MESSAGE);
					overlord.log("Backtracking ended, no more available actions to backtrack.", "text", true);
				}
				counter = 0;
			} else if (counter == DEFAULT_COUNTER && !subtractPhase) {
				// subtract phase ended, commencing add phase
				if (currentStep.getType() == GraphicalSimulator.SimulatorMode.STEP)
					launchAddPhaseGraphics(currentStep.getPendingTransitions(),
							true);
				else if (currentStep.getType() == GraphicalSimulator.SimulatorMode.SINGLE_TRANSITION)
					launchSingleAddPhaseGraphics(
							currentStep.getPendingTransitions(), true,
							currentStep.getLaunchedTransition());
				finishedAddPhase = false;
				counter = 0;
			} else if (counter == DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				// ending add phase
				if (currentStep.getType() == GraphicalSimulator.SimulatorMode.STEP)
					launchAddPhase(currentStep.getPendingTransitions(), true);
				else if (currentStep.getType() == GraphicalSimulator.SimulatorMode.SINGLE_TRANSITION)
					launchSingleAddPhase(currentStep.getPendingTransitions(),
							true, currentStep.getLaunchedTransition());
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

	// performer for LOOP and STEP mode
	public class StepPerformer extends SimulationPerformer {
		private boolean loop;
		private long time;
		SimulatorType simulatorType = SimulatorType.TIME;
		int cycle = 0;
		int steps = 0;
		int stepCounter = 0;

		public StepPerformer() {
			//timeFrame.disable();
			timeFrame.setVisible(false);
			loop = false;
			Date date = new Date();
			long t = date.getTime();
			
			time = t + (5 * 60000);
		}

		public StepPerformer(boolean looping ,SimulatorType st, int value ) {
			loop = looping;
			simulatorType = st;

			Date date = new Date();
			long t = date.getTime();
			
			if(simulatorType == SimulatorType.TIME)
				time = t + (value * 60000L);
			if(simulatorType == SimulatorType.CYCLE)
				cycle = value;
			if(simulatorType == SimulatorType.STEP)
				steps = value;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			
			Date date = new Date();
			if (simulatorType == SimulatorType.TIME&&date.getTime() > time)
				loop = false;

			if (simulatorType == SimulatorType.STEP&&steps >= stepCounter)
				loop = false;
			
			if(simulatorType == SimulatorType.CYCLE)
				for(int in : foundInvariants)
					if (in == cycle)
						loop = false;			

			updateStep(); // update graphics
			if (counter == DEFAULT_COUNTER && subtractPhase) { // subtract phase
				if (scheduledStop) { // executing scheduled stop
					executeScheduledStop();
				} else if (isPossibleStep()) { // if steps remaining
					
					if (remainingTransitionsAmount == 0) {
						launchingTransitions = generateValidLaunchingTransitions();
						remainingTransitionsAmount = launchingTransitions
								.size();
					}
					
					// Invar
					if (launchingTransitions.size() > 0) {
						for (Transition lau_tran : launchingTransitions)
							for (int invariant = 0; invariant< invariants.size() ; invariant++) 
								for ( int tran = 0 ; tran < invariants.get(invariant).size();tran++)
								//for (InvariantTransition transition :  invariants.get(invariant))
								{
									if(lau_tran.getID()==invariants.get(invariant).get(tran).getTransition().getID())
										if(lastTransition.get(invariant)!=Integer.MAX_VALUE)
										{
											boolean connectionExist=false;
											for(Arc arc : invariants.get(invariant).get(tran).getTransition().getInputArcs())
												for(Arc arcPlace : arc.getStartNode().getInputArcs())
													if(arcPlace.getStartNode().getID() == lastTransition.get(invariant))
														connectionExist = true;
											if(connectionExist)
											{
												lastTransition.set(invariant,invariants.get(invariant).get(tran).getTransition().getID());
												generatedInvariants.get(invariant)[tran]++;
											}
											//
											boolean ready = true;
											boolean toomany = false;
											
											for ( int tr = 0 ; tr < invariants.get(invariant).size();tr++)
											{
												if(generatedInvariants.get(invariant)[tr] != invariants.get(invariant).get(tr).getAmountOfFirings())
													ready = false;
												if(generatedInvariants.get(invariant)[tr]>invariants.get(invariant).get(tr).getAmountOfFirings())
													toomany = true;
											}
											
											if(ready)
											{
												foundInvariants.set(invariant,foundInvariants.get(invariant) +1);
												
												for(int j = 0 ; j < invariants.get(invariant).size(); j++)
													generatedInvariants.get(invariant)[j]=0;
												lastTransition.set(invariant, Integer.MAX_VALUE);
											}
												
											if(toomany)
											{
												for(int j = 0 ; j < invariants.get(invariant).size(); j++)
													generatedInvariants.get(invariant)[j]=0;
												lastTransition.set(invariant, Integer.MAX_VALUE);
											}
										}
										else									
										{
											lastTransition.set(invariant,invariants.get(invariant).get(tran).getTransition().getID());
											generatedInvariants.get(invariant)[tran]++;
										}
								}
					} else {

					}

					//
					actionStack.push(new SimulationStep(GraphicalSimulator.SimulatorMode.STEP,
							cloneTransitionArray(launchingTransitions)));
					if (actionStack.peek().getPendingTransitions() == null) {
						//SettingsManager.log("Yay");
						overlord.log("Uknown problem in actionPerformed(ActionEvent event) in InvariantsSimulator class", "error", true);
					}
					launchSubtractPhase(launchingTransitions, false);
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation ended", "No more available steps!",
							JOptionPane.INFORMATION_MESSAGE);
					overlord.log("Simulation ended, no more available steps.", "text", true);
				}
				counter = 0;
			} else if (counter == DEFAULT_COUNTER && !subtractPhase) {
				// subtract phase ended, commencing add phase
				launchAddPhaseGraphics(launchingTransitions, false);
				finishedAddPhase = false;
				counter = 0;
			} else if (counter == DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				// ending add phase
				launchAddPhase(launchingTransitions, false);
				finishedAddPhase = true;
				subtractPhase = true;
				remainingTransitionsAmount = 0; // all transitions launched
				// if not
				// in loop mode, a stop will be
				// scheduled
				if (!loop)
				{//ZAPISYWANIE WYNIK�W
					scheduleStop();
					
					//for(Integer[] in : generatedInvariants)
						for(int i = 0 ; i < invariants.size(); i++)
							for(int j = 0 ; j < invariants.get(i).size(); j++)
								System.out.println(generatedInvariants.get(i)[j].toString());
						
						System.out.println("Znalezione Invarianty");
						for(Integer invar : foundInvariants)
							System.out.println(invar);
						
						invariantsWriter.write(invariants, foundInvariants);
						
						//overlord.getInvSimBox().getCurrentDockWindow().setEnabledInvariantSimulationInitiateButtons(true);
				}
				counter++;
			} else {
				counter++; // empty steps
			}
			stepCounter++;
		}
	}

	// performer for SINGLE_TRANSITION and SINGLE_TRANSITION_LOOP mode
	public class SingleTransitionPerformer extends SimulationPerformer {
		private boolean loop;

		public SingleTransitionPerformer() {
			loop = false;
		}

		public SingleTransitionPerformer(boolean looping) {
			loop = looping;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			updateStep(); // update graphics
			if (counter == DEFAULT_COUNTER && subtractPhase) { // subtract phase
				if (scheduledStop) { // executing scheduled stop
					executeScheduledStop();
				} else if (isPossibleStep()) { // if steps remaining
					if (remainingTransitionsAmount == 0) {
						launchingTransitions = generateValidLaunchingTransitions();
						remainingTransitionsAmount = launchingTransitions
								.size();
					}
					actionStack.push(new SimulationStep(
							GraphicalSimulator.SimulatorMode.SINGLE_TRANSITION,
							launchingTransitions.get(0), cloneTransitionArray(launchingTransitions)));
					launchSingleSubtractPhase(launchingTransitions, false, null);
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation ended", "No more available steps!",
							JOptionPane.INFORMATION_MESSAGE);
					overlord.log("Simulation ended, no more available steps.", "text", true);
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
	
	public static class InvariantsWriter{
		String zawartoscPliku = "";
		public void write(ArrayList<ArrayList<InvariantTransition>> invariants, ArrayList<Integer> counterOfInvariants) {
			try {
				//Date date = new Date();
				DateFormat df = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss");
				Date today = Calendar.getInstance().getTime();        
				String saveDate = df.format(today);	
				
				PrintWriter zapis = new PrintWriter("invariants_generation_"+saveDate+".isr");
				
				zawartoscPliku += "------Invariants Simulation Result------\n\n";
				
				for(int i = 0 ; i < invariants.size() ; i++)
				{
					zawartoscPliku += i + " : cycle = " +counterOfInvariants.get(i) + ": invariant = ";
					
					for(int j = 0 ; j < invariants.get(i).size() ; j++)
					{
						zawartoscPliku += invariants.get(i).get(j).getTransition().getName();
						if(j<invariants.get(i).size()-1)
							zawartoscPliku += ", ";
					}
					zawartoscPliku += ";\n";
				}
				zawartoscPliku += "\n";
				zapis.println(zawartoscPliku);
				zapis.close();
			} catch (Exception e) {
				System.err.println("Error: " + e.getMessage());
				JOptionPane.showMessageDialog(null,"Program cannot write invariants into file", "Error", JOptionPane.ERROR_MESSAGE);
				overlord.log(e.getMessage(), "error", true);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void clearGenInvariants()
	{
		for(ArrayList<InvariantTransition> invariant : invariants)
		{
			lastTransition.add(Integer.MAX_VALUE);
			foundInvariants.add(0);
		}
		
		generatedInvariants = new ArrayList<Integer[]>();
		for(ArrayList<InvariantTransition> in :invariants)
			generatedInvariants.add(new Integer[999]);
		//this.generatedInvariants = (ArrayList<ArrayList<InvariantTransition>>)this.invariants.clone();
		for(int i = 0 ; i < invariants.size(); i++)
			for(int j = 0 ; j < invariants.get(i).size(); j++)
				generatedInvariants.get(i)[j]=0;
		//for(ArrayList<InvariantTransition> invariant : invariants)
		//	for(InvariantTransition it : invariant)
		//		it.setAmountOfFirings(0);
	}
}
