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
import abyss.settings.SettingsManager;

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

	public enum SimulatorMode {
		LOOP, SINGLE_TRANSITION_LOOP, SINGLE_TRANSITION, STEP, STOPPED, PAUSED, ACTION_BACK, LOOP_BACK
	}

	public enum NetType {
		BASIC, COLORED, TIME
	}

	public NetSimulator(NetType type, PetriNet net) {
		simulationType = type;
		petriNet = net;

		launchingTransitions = new ArrayList<Transition>();
		actionStack = new Stack<SimulationStep>();
	}

	private boolean isPossibleStep() {
		for (Transition transition : petriNet.getTranstions()) {
			if (transition.launchable())
				return true;
		}
		return false;
	}
	
	public void setSimulatorNetType(int type)
	{
		switch(type)
		{
		case(0) :
			simulationType = NetType.BASIC;
			break;
			
		case(1) :
			simulationType = NetType.TIME;
			break;
		}
	}

	@SuppressWarnings("incomplete-switch")
	public void startSimulation(SimulatorMode simulatorMode) {
		
		timeFrame.setBounds(185, 115, 80, 30);	
		timeFrame.getContentPane().add(new JLabel(String.valueOf(timeNetStepCounter)), BorderLayout.CENTER);
		timeFrame.pack();
		timeFrame.setVisible(true);
		
		previousMode = getMode();
		setMode(simulatorMode);
		setSimulationActive(true);
		ActionListener taskPerformer = new SimulationPerformer();
		GUIManager.getDefaultGUIManager().getSimulatorBox().getProperties().allowOnlySimulationDisruptButtons();
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

	private ArrayList<Transition> generateLaunchingTransitions() {
		Random randomLaunch = new Random();
		ArrayList<Transition> launchableTransitions = new ArrayList<Transition>();
		ArrayList<Transition> allTransitions = petriNet.getTranstions();
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
				if (transition.launchable())
					if ((randomLaunch.nextInt(10) < 4) || maximumMode) {
						transition.bookRequiredTokens();
						launchableTransitions.add(transition);
					}
			}

		if (simulationType == NetType.TIME) {
			for (i = 0; i < allTransitions.size(); i++) {
				Transition transition = allTransitions.get(indexList.get(i));
				if (transition.getFireTime() == -1)
					transition.setFireTime(timeNetStepCounter);

				if (transition.launchable()) {
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
			timeFrame.getContentPane().remove(0);
			timeFrame.getContentPane().add(new JLabel(String.valueOf((int)timeNetStepCounter) + String.valueOf("." + (int)timeNetPartStepCounter)), BorderLayout.CENTER);
			timeFrame.pack();
		}

		for (Transition transition : launchableTransitions) {
			transition.returnBookedTokens();
		}
		return launchableTransitions;
	}

	public ArrayList<Transition> searchConflict(Transition t,
			ArrayList<Transition> list) {
		return null;
	}

	public void launchSubtractPhase(ArrayList<Transition> transitions,
			boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition transition : transitions) {
			transition.setLunching(true);
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
			tran.setLunching(true);
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

	public void launchAddPhaseGraphics(ArrayList<Transition> transitions,
			boolean backtracking) {
		ArrayList<Arc> arcs;
		for (Transition tran : transitions) {
			tran.setLunching(true);
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
				arcs = tran.getOutArcs();
			} else {
				tran = chosenTransition;
				arcs = tran.getInArcs();
			}
			tran.setLunching(true);
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

	public boolean launchSingleAddPhase(ArrayList<Transition> transitions,
			boolean backtracking, Transition chosenTransition) {
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

	public void stop() {
		((SimulationPerformer) getTimer().getActionListeners()[0]).scheduleStop();
	}

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

	private void stopSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getProperties().allowOnlySimulationInitiateButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationInitiateButtons();
		timer.stop();
		previousMode = mode;
		setMode(SimulatorMode.STOPPED);
	}

	private void pauseSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getProperties().allowOnlyUnpauseButton();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlyUnpauseButton();
		timer.stop();
		previousMode = mode;
		setMode(SimulatorMode.PAUSED);
	}

	private void unpauseSimulation() {
		GUIManager.getDefaultGUIManager().getSimulatorBox().getProperties().allowOnlySimulationDisruptButtons();
		GUIManager.getDefaultGUIManager().getShortcutsBar().allowOnlySimulationDisruptButtons();
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
		GUIManager.getDefaultGUIManager().getWorkspace().getProject()
				.setSimulationActive(isSimulationActive());
	}

	public int getNodesAmount() {
		return petriNet.getNodes().size();
	}

	public int getPlacesAmount() {
		return petriNet.getPlaces().size();
	}

	public int getTransitionsAmount() {
		return petriNet.getTranstions().size();
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

	private void setMode(SimulatorMode mode) {
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
	private class SimulationPerformer implements ActionListener {
		protected int counter = DEFAULT_COUNTER;
		protected boolean subtractPhase = true; // true - subtract, false - add
		// phases
		protected boolean finishedAddPhase = true;
		protected boolean scheduledStop = false;

		protected int remainingTransitionsAmount = launchingTransitions.size();

		protected void updateStep() {
			GUIManager.getDefaultGUIManager().getWorkspace().incrementSimulationStep();
			GUIManager.getDefaultGUIManager().getSimulatorBox().updateSimulatorProperties();
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
	private class StepBackPerformer extends SimulationPerformer {
		private boolean loop;
		@SuppressWarnings("unused")
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

	// performer for LOOP and STEP mode
	private class StepPerformer extends SimulationPerformer {
		private boolean loop;

		public StepPerformer() {
			loop = false;
		}

		public StepPerformer(boolean looping) {
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
						remainingTransitionsAmount = launchingTransitions.size();
					}
					actionStack.push(new SimulationStep(SimulatorMode.STEP,
						cloneTransitionArray(launchingTransitions)));
					if (actionStack.peek().getPendingTransitions() == null)
						SettingsManager.log("Yay");
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

	// performer for SINGLE_TRANSITION and SINGLE_TRANSITION_LOOP mode
	private class SingleTransitionPerformer extends SimulationPerformer {
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
