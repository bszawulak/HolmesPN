package abyss.math.simulator;

import java.util.ArrayList;

import abyss.math.Transition;
import abyss.math.simulator.NetSimulator.SimulatorMode;

public class SimulationStep {
	private SimulatorMode type;
	private ArrayList<Transition> pendingTransitions;
	private Transition launchedTransition;

	// single transition constructor
	public SimulationStep(SimulatorMode type, Transition chosenTransition,
			ArrayList<Transition> transitions) {
		this.setType(type);
		this.setPendingTransitions(transitions);
		this.setLaunchedTransition(chosenTransition);
	}

	// step constructor
	public SimulationStep(SimulatorMode type,
			ArrayList<Transition> launchedTransitions) {
		this.setType(type);
		this.setPendingTransitions(launchedTransitions);
	}

	public SimulatorMode getType() {
		return type;
	}

	public void setType(SimulatorMode type) {
		this.type = type;
	}

	public ArrayList<Transition> getPendingTransitions() {
		return pendingTransitions;
	}

	public void setPendingTransitions(ArrayList<Transition> pendingTransitions) {
		this.pendingTransitions = pendingTransitions;
	}

	public Transition getLaunchedTransition() {
		return launchedTransition;
	}

	public void setLaunchedTransition(Transition launchedTransition) {
		this.launchedTransition = launchedTransition;
	}
}
