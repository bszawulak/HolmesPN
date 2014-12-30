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
 * Klasa zajmuj¹ca siê zarz¹dzaniem ca³ym procesem symulacji.
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
	 * Enumeracja przechowuj¹ca tryb pracy symulatora. Dostêpne wartoœci:<br><br>
	 * ACTION_BACK - tryb cofniêcia pojedynczej akcji<br>
	 * LOOP - tryb pêtli zwyk³ej (STEP w pêtli)<br>
	 * LOOP_BACK - tryb pêtli cofania (do najwczesniejszej zapamiêtanej akcji)<br>
	 * PAUSED - tryb pauzy (tymczasowo zawieszona symulacja)<br>
	 * SINGLE_TRANSITION - tryb odpalenia pojedynczej tranzycji<br>
	 * SINGLE_TRANSITION_LOOP - tryb odpalania pojedynczych tranzycji w pêtli<br>
	 * STEP - tryb odpalenia pojedynczego kroku (wszystkich wylosowanych tranzycji)<br>
	 * STOPPED tryb stopu (zatrzymana symulacja)
	 */
	public enum SimulatorMode {
		LOOP, SINGLE_TRANSITION_LOOP, SINGLE_TRANSITION, STEP, STOPPED, PAUSED, ACTION_BACK, LOOP_BACK
	}

	//rodzaj symulowanej sieci (przygotowane do ewentualnych, dalszych rozszerzeñ o
	//kolejne rodzaje, poza podstawowym
	public enum NetType {
		BASIC, COLORED, TIME
	}

	/**
	 * Konstruktor obiektu symulatora sieci
	 * @param type NetType - typ sieci
	 * @param net PetriNet - sieæ do symulacji 
	 */
	public NetSimulator(NetType type, PetriNet net) {
		simulationType = type;
		petriNet = net;

		launchingTransitions = new ArrayList<Transition>();
		actionStack = new Stack<SimulationStep>();
	}

	/**
	 * Metoda sprawdzaj¹ca, czy krok jest mo¿liwy - czy istnieje choæ jedna aktywna
	 * tranzycja.
	 * @return boolean - true jeœli jest choæ jedna aktywna tranzycja; false w przeciwnym wypadku
	 */
	private boolean isPossibleStep() {
		for (Transition transition : petriNet.getTransitions()) {
			if (transition.isActive())
				return true;
		}
		return false;
	}
	
	/**
	 * Metoda ustawiaj¹ca tryb sieci do symulacji
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
	 * Metoda rozpoczyna symulacjê w odpowiednim trybie. W zale¿noœci od wybranego trybu,
	 * w oddzielnym w¹tku symulacji zainicjalizowana zostaje odpowiednia klasa dziedzicz¹ca
	 * po StepPerformer.
	 * @param simulatorMode SimulatorMode - wybrany tryb symulacji
	 */
	public void startSimulation(SimulatorMode simulatorMode) {
		timeFrame.setBounds(185, 115, 80, 30);	
		timeFrame.getContentPane().add(new JLabel(String.valueOf(timeNetStepCounter)), BorderLayout.CENTER);
		timeFrame.pack();
		timeFrame.setVisible(true);
		
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
	 * które tranzycje nadaj¹ siê do uruchomienia. Aktualnie dzia³a dla modelu klasycznego PN
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
	 * Nieu¿ywana, nieopisana
	 * @param t
	 * @param list
	 * @return
	 */
	public ArrayList<Transition> searchConflict(Transition t, ArrayList<Transition> list) {
		return null;
	}

	/**
	 * Metoda uruchamia fazê odejmowania tokenów z miejsc wejœciowych odpalonych tranzycji
	 * (lub wyjœciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista uruchamianych tranzycji
	 * @param backtracking boolean - true, jeœli symulator pracuje w trybie cofania;
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
	 * Metoda uruchamia fazê odejmowania tokenów z miejsc wejœciowych jednej odpalonej tranzycji
	 * (lub wyjœciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeœli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 * @return boolean - true, jeœli faza zosta³a pomyœlnie uruchomiona; false w przeciwnym razie
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
	 * Metoda uruchamia fazê wizualizacji dodawania tokenów do miejsc wyjœciowych odpalonych tranzycji
	 * (lub wejœciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeœli symulator pracuje w trybie cofania;
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
	 * Metoda uruchamia fazê wizualizacji dodawania tokenów do miejsc wyjœciowych dla pojedynczej
	 * spoœród odpalanych tranzycji (lub wejœciowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeœli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 * @return boolean - true, jeœli faza zosta³a pomyœlnie uruchomiona; false w przeciwnym razie
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
	 * Metoda uruchamia fazê faktycznego dodawania tokenów do miejsc wyjœciowych odpalonych
	 * tranzycji (lub wejœciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeœli symulator pracuje w trybie cofania;
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
	 * Metoda uruchamia fazê faktycznego dodawania tokenów do miejsc wyjœciowych dla pojedynczej
	 * spoœród odpalanych tranzycji (lub wejœciowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, jeœli symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, której dotyczy uruchomienie tej metody
	 * @return boolean - true, jeœli faza zosta³a pomyœlnie uruchomiona; false w przeciwnym razie
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
	 * Metoda zatrzymuje symulacjê po zakoñczeniu aktualnego kroku (gdy zostan¹ odpalone
	 * wszystkie odpalane w tym momencie tranzycje).
	 */
	public void stop() {
		((SimulationPerformer) getTimer().getActionListeners()[0]).scheduleStop();
	}

	/**
	 * Jeœli aktualnie trwa symulacja, metoda ta przerwie j¹ natychmiast, i zablokuje wszystkie
	 * inne funkcje w symulatorze, poza opcj¹ uruchomienia tej metody. Jeœli symulacja zosta³a
	 * ju¿ zatrzymana t¹ metod¹, ponowne jej wywo³anie spowoduje kontynuowanie jej od momentu, w
	 * którym zosta³a przerwana.
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
	 * Metoda uruchamiana przez klasy pomocniczne dziedzic¿ace z SimulationPerformer, odpowiedzialna
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
	 * Metoda uruchamiana przez klasy pomocniczne dziedzic¿ace z SimulationPerformer, odpowiedzialna
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
	 * Metoda uruchamiana przez klasy pomocniczne dziedzic¿ace z SimulationPerformer, odpowiedzialna
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
	 * Metoda pozwala sprawdziæ, czy symulacja jest w tym momencie aktywna.
	 * @return boolean - true, jeœli symulacja jest aktywna; false w przeciwnym wypadku
	 */
	public boolean isSimulationActive() {
		return simulationActive;
	}

	/**
	 * Metoda pozwala ustawiæ, czy symulacja jest w tym momencie aktywna.
	 * @param simulationActive boolean - wartoœæ aktywnoœci symulacji
	 */
	public void setSimulationActive(boolean simulationActive) {
		this.simulationActive = simulationActive;
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setSimulationActive(isSimulationActive());
	}

	/**
	 * Metoda pozwala pobraæ liczbê wierzcho³ków w sieci Petriego.
	 * @return int - liczba wierzcho³ków w sieci Petriego
	 */
	public int getNodesAmount() {
		return petriNet.getNodes().size();
	}

	/**
	 * Metoda pozwala pobraæ liczbê miejsc w sieci Petriego.
	 * @return int - liczba miejsc w sieci Petriego
	 */
	public int getPlacesAmount() {
		return petriNet.getPlaces().size();
	}

	/**
	 * Metoda pozwala pobraæ liczbê tranzycji w sieci Petriego.
	 * @return int  - liczba tranzycji w sieci Petriego
	 */
	public int getTransitionsAmount() {
		return petriNet.getTransitions().size();
	}

	/**
	 * Metoda pozwala pobraæ liczbê ³uków w sieci Petriego.
	 * @return int - liczba ³uków w sieci Petriego
	 */
	public int getArcsAmount() {
		return petriNet.getArcs().size();
	}

	/**
	 * Metoda pozwala pobraæ ³¹czn¹ liczbê wszystkich tokenów w sieci Petriego.
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
	 * Metoda pozwala pobraæ zegar odpowiadaj¹cy za w¹tek symulacji.
	 * @return Timer - zegar odpowiadaj¹cy za w¹tek symulacji
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * Metoda pozwalaj¹ca ustawiæ nowy zegar dla w¹tku symulacji.
	 * @param timer Timer - zegar
	 */
	private void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * Metoda pozwala pobraæ interwa³ czasowy pomiêdzy kolejnymi krokami symulacji.
	 * @return Integer - interwa³ czasowy pomiêdzy kolejnymi krokami symulacji wyra¿ony w milisekundach
	 */
	public Integer getDelay() {
		return delay;
	}

	/**
	 * Metoda pozwala ustawiæ interwa³ czasowy pomiêdzy kolejnymi krokami symulacji.
	 * @param delay Integer - interwa³ czasowy pomiêdzy kolejnymi krokami symulacji wyra¿ony w milisekundach
	 */
	public void setDelay(Integer delay) {
		if (timer != null)
			timer.setDelay(delay);
		this.delay = delay;
	}

	/**
	 * Metoda pozwala pobraæ aktualny tryb pracy symulatora.
	 * @return SimulatorMode - tryb pracy
	 */
	public SimulatorMode getMode() {
		return mode;
	}

	/**
	 * Metoda ustawiaj¹ca nowy tryb pracy dla symulatora.
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
	 * Metoda sprawdzaj¹ca, czy symulator pracuje w trybie maksymalnego uruchamiania
	 * tranzycji.
	 * @return boolean - true, jeœli w³¹czony tryb maksymalnego uruchamiania; 
	 * 		false w przeciwnym wypadku
	 */
	public boolean isMaximumMode() {
		return maximumMode;
	}

	/**
	 * Metoda ustwaiaj¹ca trybie maksymalnego uruchamiania tranzycji.
	 * @return boolean - true, jeœli w³¹czany jest tryb maksymalnego uruchamiania; 
	 * 		false w przeciwnym wypadku
	 */
	public void setMaximumMode(boolean maximumMode) {
		this.maximumMode = maximumMode;
	}

	/**
	 * Po tej klasie dziedziczy szereg klas implementuj¹cych konkretne tryby pracy symulatora.
	 * Metoda actionPerformed() jest wykonywana w ka¿dym kroku symulacji przez timer obiektu
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
		 * Metoda aktualizuje wyœwietlanie graficznej czêœci symulacji po wykonaniu ka¿dego kroku.
		 */
		protected void updateStep() {
			GUIManager.getDefaultGUIManager().getWorkspace().incrementSimulationStep();
			GUIManager.getDefaultGUIManager().getSimulatorBox().updateSimulatorProperties();
		}

		/**
		 * Metoda inicjuje zatrzymanie symulacji po zakoñczeniu aktualnego kroku.
		 */
		public void scheduleStop() {
			scheduledStop = true;
		}

		/**
		 * Metoda natychmiast zatrzymuje symulacjê.
		 */
		public void executeScheduledStop() {
			stopSimulation();
			scheduledStop = false;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka¿dy kolejny krok przez symulator.
		 * Szkielet oferowany przez StepPerformer automatycznie aktualizuje graficzn¹
		 * czêœæ symulacji na pocz¹tku ka¿dego kroku.
		 * @param event ActionEvent - zdarzenie, które spowodowa³o wykonanie metody 
		 */
		public void actionPerformed(ActionEvent event) {
			updateStep();
		}
	}

	/**
	 * Klasa implementuj¹ca cofanie wykonanych w innych trybach kroków. Tryb ACTION_BACK
	 * wykonuje fazy analogiczne do StepPerformer lub SingleTransitionPerformer (zale¿nie od
	 * tego, jaki krok zostaje cofniêty), jednak¿e w odwróconej kolejnoœci. Cofane tranzycje
	 * nie s¹ losowane, lecz zdejmowane ze stosu historii wykonanych kroków. Tryb LOOP_BACK
	 * wykonuje kroki ACTION_BACK w pêtli do czasu wyczerpania stosu historii (przywrócenia
	 * stanu globalnego pocz¹tkowego sieci Petriego).
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
		 * @param looping boolean - true, jeœli w pêtli
		 */
		public StepBackPerformer(boolean looping) {
			currentTransitions = new ArrayList<Transition>();
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka¿dy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, które spowodowa³o wykonanie metody 
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
	 *  Klasa implementuj¹ca wykonywanie kroków dla najbardziej podstawowych trybów symulacji
	 *  LOOP oraz STEP. Dla trybu STEP na pocz¹tku ka¿dego kroku generowana jest lista tranzycji
	 *  faktycznie odpalanych spoœród istniej¹cych w sieci Petriego tranzycji aktywnych (dla
	 *  trybu pe³nego - wszystkie aktywne tranzycje), a nastêpnie symulowany jest proces
	 *  odpalenia tranzycji w 3 roz³¹cznych fazach dla ka¿dej pozycji z listy równoczeœnie:<br><br>
	 *  faza odejmowania w której tokeny zostaj¹ zabrane z odpowiednich miejsc wejœciowych<br>
	 *  faza graficznego dodawania - w której wyœwietlone zostaje przejscie tokenów<br>
	 *  faza dodawania - w której tokeny zostaj¹ dodane do odpowiednich miejsc wyjœciowych
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
		 * @param looping boolean - true, jeœli dzia³anie w pêtli
		 */
		public StepPerformer(boolean looping) {
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka¿dy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, które spowodowa³o wykonanie metody 
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
	 * Klasa implementuj¹ca wykonywanie kroków dla trybów symulacji SINGLE_TRANSITION oraz 
	 * SINGLE_TRANSITION_LOOP. Dzia³a analogicznie do StepPerformer, za wyj¹tkiem 2 ró¿nic:
	 * w danym kroku odpalana jest tylko jedna tranzycja z listy, po czym jest z niej usuwana;
	 * nowa lista generowana jest dopiero, gdy poprzednia zostanie opró¿niona z tranzycji.
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
		 * @param looping boolean - true, jeœli dzia³anie w pêtli
		 */
		public SingleTransitionPerformer(boolean looping) {
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka¿dy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, które spowodowa³o wykonanie metody 
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
