package abyss.math.simulator;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Klasa zajmująca się zarządzaniem całym procesem symulacji.
 * @author students
 *
 */
public class NetSimulator {
	private NetType simulationType;
	private SimulatorMode mode = SimulatorMode.STOPPED;
	private SimulatorMode previousMode = SimulatorMode.STOPPED;
	private PetriNet petriNet;
	private Integer delay = new Integer(30);
	private boolean simulationActive = false;
	private Timer timer;
	private ArrayList<Transition> launchingTransitions;
	private Stack<SimulationStep> actionStack;
	private boolean maximumMode = false;
	public static int DEFAULT_COUNTER = 50;
	public JFrame timeFrame = new JFrame("Zegar");
	public double timeNetStepCounter = 0;
	public double timeNetPartStepCounter = 0;

	//tryb symulacji
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

	//rodzaj symulowanej sieci (przygotowane do ewentualnych, dalszych rozszerzeń o
	//kolejne rodzaje, poza podstawowym
	public enum NetType {
		BASIC, COLORED, TIME
	}

	/**
	 * Konstruktor obiektu symulatora sieci
	 * @param type NetType - typ sieci
	 * @param net PetriNet - sieć do symulacji 
	 */
	public NetSimulator(NetType type, PetriNet net) {
		simulationType = type;
		petriNet = net;

		launchingTransitions = new ArrayList<Transition>();
		actionStack = new Stack<SimulationStep>();
	}

	/**
	 * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje choć jedna aktywna
	 * tranzycja.
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
	 * Metoda ustawiająca tryb sieci do symulacji
	 * @param type int - typ sieci.
	 */
	public void setSimulatorNetType(int type)
	{
		switch(type) {
			case(0) :
				simulationType = NetType.BASIC;
				break;
			case(1) :
				simulationType = NetType.TIME;
				break;
		}
	}

	@SuppressWarnings("incomplete-switch")
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
		
		previousMode = getMode();
		setMode(simulatorMode);
		setSimulationActive(true);
		ActionListener taskPerformer = new SimulationPerformer();
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationDisruptButtons();
		switch (getMode()) {
		case LOOP:
			taskPerformer = new StepPerformer(true);
			break;
		case SINGLE_TRANSITION_LOOP:
			taskPerformer = new SingleTransitionPerformer(true);
			break;
		case SINGLE_TRANSITION:
			taskPerformer = new SingleTransitionPerformer();
			break;
		case STEP:
			taskPerformer = new StepPerformer();
			break;
		case ACTION_BACK:
			taskPerformer = new StepBackPerformer();
			break;
		case LOOP_BACK:
			launchingTransitions.clear();
			taskPerformer = new StepBackPerformer(true);
			break;
		}
		setTimer(new Timer(getDelay(), taskPerformer));
		getTimer().start();
	}

	/**
	 * Metoda generuje zbiór tranzycji do uruchomienia.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia.
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
	 * Metoda pomocnicza dla generateValidLaunchingTransitions(), odpowiedzialna za sprawdzenie
	 * które tranzycje nadają się do uruchomienia. Aktualnie działa dla modelu klasycznego PN
	 * oraz czasowego.
	 * @return ArrayList[Transition] - zbiór tranzycji do uruchomienia.
	 */
	private ArrayList<Transition> generateLaunchingTransitions() {
		Random randomLaunch = new Random();
		ArrayList<Transition> launchableTransitions = new ArrayList<Transition>();
		ArrayList<Transition> allTransitions = petriNet.getTransitions();
		ArrayList<Integer> indexList = new ArrayList<Integer>();
		int i = 0;
		for (@SuppressWarnings("unused") Transition transition : allTransitions) {
			indexList.add(i);
			i++;
		}
		Collections.shuffle(indexList);
		if (simulationType == NetType.BASIC)
			for (i = 0; i < allTransitions.size(); i++) {
				Transition transition = allTransitions.get(indexList.get(i));
				if (transition.isActive() )
					if ((randomLaunch.nextInt(10) < 5) || maximumMode) { // why 4?
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
			}

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
			timeFrame.getContentPane().add(new JLabel(String.valueOf((int)timeNetStepCounter) + 
					String.valueOf("." + (int)timeNetPartStepCounter)), BorderLayout.CENTER);
			timeFrame.pack();
		}

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
	public void launchAddPhase(ArrayList<Transition> transitions,
			boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			if (!backtracking)
				arcs = transition.getOutArcs();
			else
				arcs = transition.getInArcs();
			// add adequate number of tokens to destinations
			for (Arc arc : arcs) {
				Place place;
				if (!backtracking)
					place = (Place) arc.getEndNode();
				else
					place = (Place) arc.getStartNode();
				place.modifyTokensNumber(arc.getWeight());
			}
		}
		transitions.clear();
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
		if ((getMode() != SimulatorMode.PAUSED)
				&& (getMode() != SimulatorMode.STOPPED))
			pauseSimulation();
		else if (getMode() == SimulatorMode.PAUSED)
			unpauseSimulation();
		else if (getMode() == SimulatorMode.STOPPED)
			JOptionPane.showMessageDialog(null,
				"Can't pause a stopped simulation!", "The simulator is already stopped!",JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za zatrzymanie symulacji.
	 */
	private void stopSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlySimulationInitiateButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationInitiateButtons();
		timer.stop();
		previousMode = mode;
		setMode(SimulatorMode.STOPPED);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za pauzowanie symulacji.
	 */
	private void pauseSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlyUnpauseButton();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlyUnpauseButton();
		timer.stop();
		previousMode = mode;
		setMode(SimulatorMode.PAUSED);
	}

	/**
	 * Metoda uruchamiana przez klasy pomocniczne dziedziczące z SimulationPerformer, odpowiedzialna
	 * za ponowne uruchomienie (po pauzie) symulacji.
	 */
	private void unpauseSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().allowOnlySimulationDisruptButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationDisruptButtons();
		if (previousMode != SimulatorMode.STOPPED) {
			timer.start();
			setMode(previousMode);
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
	public SimulatorMode getMode() {
		return mode;
	}

	/**
	 * Metoda ustawiająca nowy tryb pracy dla symulatora.
	 * @param mode SimulatorMode - tryb pracy
	 */
	private void setMode(SimulatorMode mode) {
		this.mode = mode;
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
		protected int counter = DEFAULT_COUNTER;
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
					currentStep = actionStack.pop();
					if (currentStep.getType() == SimulatorMode.STEP) {
						launchSubtractPhase(
								currentStep.getPendingTransitions(), true);
					} else if (currentStep.getType() == SimulatorMode.SINGLE_TRANSITION) {
						launchSingleSubtractPhase(
								currentStep.getPendingTransitions(), true,
								currentStep.getLaunchedTransition());
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
				if (currentStep.getType() == SimulatorMode.STEP)
					launchAddPhaseGraphics(currentStep.getPendingTransitions(), true);
				else if (currentStep.getType() == SimulatorMode.SINGLE_TRANSITION)
					launchSingleAddPhaseGraphics(currentStep.getPendingTransitions(), true,
						currentStep.getLaunchedTransition());
				finishedAddPhase = false;
				counter = 0;
			} else if (counter == DEFAULT_COUNTER - 5 && !finishedAddPhase) {
				// ending add phase
				if (currentStep.getType() == SimulatorMode.STEP)
					launchAddPhase(currentStep.getPendingTransitions(), true);
				else if (currentStep.getType() == SimulatorMode.SINGLE_TRANSITION)
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

	/**
	 *  Klasa implementująca wykonywanie kroków dla najbardziej podstawowych trybów symulacji
	 *  LOOP oraz STEP. Dla trybu STEP na początku każdego kroku generowana jest lista tranzycji
	 *  faktycznie odpalanych spośród istniejących w sieci Petriego tranzycji aktywnych (dla
	 *  trybu pełnego - wszystkie aktywne tranzycje), a następnie symulowany jest proces
	 *  odpalenia tranzycji w 3 rozłącznych fazach dla każdej pozycji z listy równocześnie:<br><br>
	 *  faza odejmowania w której tokeny zostają zabrane z odpowiednich miejsc wejściowych<br>
	 *  faza graficznego dodawania - w której wyświetlone zostaje przejscie tokenów<br>
	 *  faza dodawania - w której tokeny zostaja dodane do odpowiednich miejsc wyjściowych
	 * @author students
	 */
	private class StepPerformer extends SimulationPerformer {
		private boolean loop;

		/**
		 * Konstruktor bezparametrowy obiektu klasy StepPerformer
		 */
		public StepPerformer() {
			loop = false;
		}

		/**
		 * Konstruktor obiektu klasy StepPerformer
		 * @param looping boolean - true, jeśli działanie w pętli
		 */
		public StepPerformer(boolean looping) {
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
						launchingTransitions = generateValidLaunchingTransitions();
						remainingTransitionsAmount = launchingTransitions.size();
					}
					actionStack.push(new SimulationStep(SimulatorMode.STEP,
						cloneTransitionArray(launchingTransitions)));
					if (actionStack.peek().getPendingTransitions() == null) {
						//SettingsManager.log("Yay");
						GUIManager.getDefaultGUIManager().log("Uknown problem in actionPerformed(ActionEvent event) in NetSimulator class", "error", true);
					}
					launchSubtractPhase(launchingTransitions, false);
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation ended",
							"No more available steps!",
							JOptionPane.INFORMATION_MESSAGE);
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
						launchingTransitions = generateValidLaunchingTransitions();
						remainingTransitionsAmount = launchingTransitions
								.size();
					}
					actionStack.push(new SimulationStep(
							SimulatorMode.SINGLE_TRANSITION,
							launchingTransitions.get(0),
							cloneTransitionArray(launchingTransitions)));
					launchSingleSubtractPhase(launchingTransitions, false, null);
					subtractPhase = false;
				} else {
					// simulation ends, no possible steps remaining
					setSimulationActive(false);
					stopSimulation();
					JOptionPane.showMessageDialog(null, "Simulation ended",
						"No more available steps!",JOptionPane.INFORMATION_MESSAGE);
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
}
