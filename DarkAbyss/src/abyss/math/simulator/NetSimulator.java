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
 * Klasa zajmująca się zarz�dzaniem całym procesem symulacji.
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

	//rodzaj symulowanej sieci (przygotowane do ewentualnych, dalszych rozszerze� o
	//kolejne rodzaje, poza podstawowym
	public enum NetType {
		BASIC, COLORED, TIME
	}

	/**
	 * Konstruktor obiektu symulatora sieci
	 * @param type NetType - typ sieci
	 * @param net PetriNet - sie� do symulacji 
	 */
	public NetSimulator(NetType type, PetriNet net) {
		simulationType = type;
		petriNet = net;

		launchingTransitions = new ArrayList<Transition>();
		actionStack = new Stack<SimulationStep>();
	}

	/**
	 * Metoda sprawdzająca, czy krok jest możliwy - czy istnieje cho� jedna aktywna
	 * tranzycja.
	 * @return boolean - true je�li jest cho� jedna aktywna tranzycja; false w przeciwnym wypadku
	 */
	private boolean isPossibleStep() {
		for (Transition transition : petriNet.getTransitions()) {
			if (transition.isActive())
				return true;
		}
		return false;
	}
	
	/**
	 * Metoda ustawiaj�ca tryb sieci do symulacji
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
	 * Metoda rozpoczyna symulacj� w odpowiednim trybie. W zale�no�ci od wybranego trybu,
	 * w oddzielnym w�tku symulacji zainicjalizowana zostaje odpowiednia klasa dziedzicz�ca
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
	 * Metoda generuje zbi�r tranzycji do uruchomienia.
	 * @return ArrayList[Transition] - zbi�r tranzycji do uruchomienia.
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
	 * kt�re tranzycje nadaj� si� do uruchomienia. Aktualnie dzia�a dla modelu klasycznego PN
	 * oraz czasowego.
	 * @return ArrayList[Transition] - zbi�r tranzycji do uruchomienia.
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
	 * Nieu�ywana, nieopisana
	 * @param t
	 * @param list
	 * @return
	 */
	public ArrayList<Transition> searchConflict(Transition t, ArrayList<Transition> list) {
		return null;
	}

	/**
	 * Metoda uruchamia faz� odejmowania token�w z miejsc wej�ciowych odpalonych tranzycji
	 * (lub wyj�ciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista uruchamianych tranzycji
	 * @param backtracking boolean - true, je�li symulator pracuje w trybie cofania;
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
	 * Metoda uruchamia faz� odejmowania token�w z miejsc wej�ciowych jednej odpalonej tranzycji
	 * (lub wyj�ciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, je�li symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, kt�rej dotyczy uruchomienie tej metody
	 * @return boolean - true, je�li faza zosta�a pomy�lnie uruchomiona; false w przeciwnym razie
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
	 * Metoda uruchamia faz� wizualizacji dodawania token�w do miejsc wyj�ciowych odpalonych tranzycji
	 * (lub wej�ciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, je�li symulator pracuje w trybie cofania;
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
	 * Metoda uruchamia faz� wizualizacji dodawania token�w do miejsc wyj�ciowych dla pojedynczej
	 * spo�r�d odpalanych tranzycji (lub wej�ciowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, je�li symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, kt�rej dotyczy uruchomienie tej metody
	 * @return boolean - true, je�li faza zosta�a pomy�lnie uruchomiona; false w przeciwnym razie
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
	 * Metoda uruchamia faz� faktycznego dodawania token�w do miejsc wyj�ciowych odpalonych
	 * tranzycji (lub wej�ciowych, dla trybu cofania). 
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, je�li symulator pracuje w trybie cofania;
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
	 * Metoda uruchamia faz� faktycznego dodawania token�w do miejsc wyj�ciowych dla pojedynczej
	 * spo�r�d odpalanych tranzycji (lub wej�ciowych, dla trybu cofania).
	 * @param transitions ArrayList[Transition] - lista odpalanych tranzycji
	 * @param backtracking boolean - true, je�li symulator pracuje w trybie cofania;
	 * 		false w przeciwnym wypadku
	 * @param chosenTransition Transition - wybrana tranzycja, kt�rej dotyczy uruchomienie tej metody
	 * @return boolean - true, je�li faza zosta�a pomy�lnie uruchomiona; false w przeciwnym razie
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
	 * Metoda zatrzymuje symulacj� po zako�czeniu aktualnego kroku (gdy zostan� odpalone
	 * wszystkie odpalane w tym momencie tranzycje).
	 */
	public void stop() {
		((SimulationPerformer) getTimer().getActionListeners()[0]).scheduleStop();
	}

	/**
	 * Je�li aktualnie trwa symulacja, metoda ta przerwie j� natychmiast, i zablokuje wszystkie
	 * inne funkcje w symulatorze, poza opcj� uruchomienia tej metody. Je�li symulacja zosta�a
	 * ju� zatrzymana t� metod�, ponowne jej wywo�anie spowoduje kontynuowanie jej od momentu, w
	 * kt�rym zosta�a przerwana.
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
	 * Metoda uruchamiana przez klasy pomocniczne dziedzic�ace z SimulationPerformer, odpowiedzialna
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
	 * Metoda uruchamiana przez klasy pomocniczne dziedzic�ace z SimulationPerformer, odpowiedzialna
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
	 * Metoda uruchamiana przez klasy pomocniczne dziedzic�ace z SimulationPerformer, odpowiedzialna
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
	 * Metoda pozwala sprawdzi�, czy symulacja jest w tym momencie aktywna.
	 * @return boolean - true, je�li symulacja jest aktywna; false w przeciwnym wypadku
	 */
	public boolean isSimulationActive() {
		return simulationActive;
	}

	/**
	 * Metoda pozwala ustawi�, czy symulacja jest w tym momencie aktywna.
	 * @param simulationActive boolean - warto�� aktywno�ci symulacji
	 */
	public void setSimulationActive(boolean simulationActive) {
		this.simulationActive = simulationActive;
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().setSimulationActive(isSimulationActive());
	}

	/**
	 * Metoda pozwala pobra� liczb� wierzcho�k�w w sieci Petriego.
	 * @return int - liczba wierzcho�k�w w sieci Petriego
	 */
	public int getNodesAmount() {
		return petriNet.getNodes().size();
	}

	/**
	 * Metoda pozwala pobra� liczb� miejsc w sieci Petriego.
	 * @return int - liczba miejsc w sieci Petriego
	 */
	public int getPlacesAmount() {
		return petriNet.getPlaces().size();
	}

	/**
	 * Metoda pozwala pobra� liczb� tranzycji w sieci Petriego.
	 * @return int  - liczba tranzycji w sieci Petriego
	 */
	public int getTransitionsAmount() {
		return petriNet.getTransitions().size();
	}

	/**
	 * Metoda pozwala pobra� liczb� �uk�w w sieci Petriego.
	 * @return int - liczba �uk�w w sieci Petriego
	 */
	public int getArcsAmount() {
		return petriNet.getArcs().size();
	}

	/**
	 * Metoda pozwala pobra� ��czn� liczb� wszystkich token�w w sieci Petriego.
	 * @return int - liczba token�w w sieci Petriego
	 */
	public int getTokensAmount() {
		int tokenAmount = 0;
		for (Place place : petriNet.getPlaces()) {
			tokenAmount += place.getTokensNumber();
		}
		return tokenAmount;
	}

	/**
	 * Metoda pozwala pobra� zegar odpowiadaj�cy za w�tek symulacji.
	 * @return Timer - zegar odpowiadaj�cy za w�tek symulacji
	 */
	public Timer getTimer() {
		return timer;
	}

	/**
	 * Metoda pozwalaj�ca ustawi� nowy zegar dla w�tku symulacji.
	 * @param timer Timer - zegar
	 */
	private void setTimer(Timer timer) {
		this.timer = timer;
	}

	/**
	 * Metoda pozwala pobra� interwa� czasowy pomi�dzy kolejnymi krokami symulacji.
	 * @return Integer - interwa� czasowy pomi�dzy kolejnymi krokami symulacji wyra�ony w milisekundach
	 */
	public Integer getDelay() {
		return delay;
	}

	/**
	 * Metoda pozwala ustawi� interwa� czasowy pomi�dzy kolejnymi krokami symulacji.
	 * @param delay Integer - interwa� czasowy pomi�dzy kolejnymi krokami symulacji wyra�ony w milisekundach
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
	 * Metoda ustawiaj�ca nowy tryb pracy dla symulatora.
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
	 * Metoda sprawdzaj�ca, czy symulator pracuje w trybie maksymalnego uruchamiania
	 * tranzycji.
	 * @return boolean - true, je�li w��czony tryb maksymalnego uruchamiania; 
	 * 		false w przeciwnym wypadku
	 */
	public boolean isMaximumMode() {
		return maximumMode;
	}

	/**
	 * Metoda ustwaiaj�ca trybie maksymalnego uruchamiania tranzycji.
	 * @return boolean - true, je�li w��czany jest tryb maksymalnego uruchamiania; 
	 * 		false w przeciwnym wypadku
	 */
	public void setMaximumMode(boolean maximumMode) {
		this.maximumMode = maximumMode;
	}

	/**
	 * Po tej klasie dziedziczy szereg klas implementuj�cych konkretne tryby pracy symulatora.
	 * Metoda actionPerformed() jest wykonywana w ka�dym kroku symulacji przez timer obiektu
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
		 * Metoda aktualizuje wy�wietlanie graficznej cz�ci symulacji po wykonaniu ka�dego kroku.
		 */
		protected void updateStep() {
			GUIManager.getDefaultGUIManager().getWorkspace().incrementSimulationStep();
			
			//tutaj nic si� nie dzieje: a chyba chodzi�o o update podokna w�a�ciwo�ci z liczb� token�w
			GUIManager.getDefaultGUIManager().getSimulatorBox().updateSimulatorProperties();
		}

		/**
		 * Metoda inicjuje zatrzymanie symulacji po zako�czeniu aktualnego kroku.
		 */
		public void scheduleStop() {
			scheduledStop = true;
		}

		/**
		 * Metoda natychmiast zatrzymuje symulacj�.
		 */
		public void executeScheduledStop() {
			stopSimulation();
			scheduledStop = false;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka�dy kolejny krok przez symulator.
		 * Szkielet oferowany przez StepPerformer automatycznie aktualizuje graficzn�
		 * cz�� symulacji na pocz�tku ka�dego kroku.
		 * @param event ActionEvent - zdarzenie, kt�re spowodowa�o wykonanie metody 
		 */
		public void actionPerformed(ActionEvent event) {
			updateStep();
		}
	}

	/**
	 * Klasa implementuj�ca cofanie wykonanych w innych trybach krok�w. Tryb ACTION_BACK
	 * wykonuje fazy analogiczne do StepPerformer lub SingleTransitionPerformer (zale�nie od
	 * tego, jaki krok zostaje cofni�ty), jednak�e w odwr�conej kolejno�ci. Cofane tranzycje
	 * nie s� losowane, lecz zdejmowane ze stosu historii wykonanych krok�w. Tryb LOOP_BACK
	 * wykonuje kroki ACTION_BACK w p�tli do czasu wyczerpania stosu historii (przywr�cenia
	 * stanu globalnego pocz�tkowego sieci Petriego).
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
		 * @param looping boolean - true, je�li w p�tli
		 */
		public StepBackPerformer(boolean looping) {
			currentTransitions = new ArrayList<Transition>();
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka�dy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, kt�re spowodowa�o wykonanie metody 
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
	 *  Klasa implementuj�ca wykonywanie krok�w dla najbardziej podstawowych tryb�w symulacji
	 *  LOOP oraz STEP. Dla trybu STEP na pocz�tku ka�dego kroku generowana jest lista tranzycji
	 *  faktycznie odpalanych spo�r�d istniej�cych w sieci Petriego tranzycji aktywnych (dla
	 *  trybu pe�nego - wszystkie aktywne tranzycje), a nast�pnie symulowany jest proces
	 *  odpalenia tranzycji w 3 roz��cznych fazach dla ka�dej pozycji z listy r�wnocze�nie:<br><br>
	 *  faza odejmowania w kt�rej tokeny zostaj� zabrane z odpowiednich miejsc wej�ciowych<br>
	 *  faza graficznego dodawania - w kt�rej wy�wietlone zostaje przejscie token�w<br>
	 *  faza dodawania - w kt�rej tokeny zostaj� dodane do odpowiednich miejsc wyj�ciowych
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
		 * @param looping boolean - true, je�li dzia�anie w p�tli
		 */
		public StepPerformer(boolean looping) {
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka�dy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, kt�re spowodowa�o wykonanie metody 
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
	 * Klasa implementuj�ca wykonywanie krok�w dla tryb�w symulacji SINGLE_TRANSITION oraz 
	 * SINGLE_TRANSITION_LOOP. Dzia�a analogicznie do StepPerformer, za wyj�tkiem 2 r�nic:
	 * w danym kroku odpalana jest tylko jedna tranzycja z listy, po czym jest z niej usuwana;
	 * nowa lista generowana jest dopiero, gdy poprzednia zostanie opr�niona z tranzycji.
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
		 * @param looping boolean - true, je�li dzia�anie w p�tli
		 */
		public SingleTransitionPerformer(boolean looping) {
			loop = looping;
		}

		/**
		 * Metoda faktycznie wykonywana jako ka�dy kolejny krok przez symulator.
		 * @param event ActionEvent - zdarzenie, kt�re spowodowa�o wykonanie metody 
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
