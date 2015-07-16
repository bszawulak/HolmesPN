package abyss.math.simulator;

import java.util.ArrayList;

import abyss.math.pnElements.Transition;
import abyss.math.simulator.NetSimulator.SimulatorMode;

/**
 * Klasa przechowująca krok symulacji. Za każdym razem, gdy wykonywany jest krok symulacji
 * (również w trybach zapętlonych), na stos dodany zostaje obiekt tej klasy przechowujący
 * informacje o nim. Dzięki temu możliwa była implementacja trybów cofania.
 * @author students
 *
 */
public class SimulationStep {
	private SimulatorMode type;
	private ArrayList<Transition> pendingTransitions;
	private Transition launchedTransition;

	/**
	 * Konstruktor obiektu klasy SimulationStep
	 * @param type SimulatorMode - tryb pracy symulatora 
	 * @param chosenTransition Transition - odpalona tranzycja
	 * @param transitions ArrayList[Transition] - lista tranzycji do uruchomienia
	 */
	public SimulationStep(SimulatorMode type, Transition chosenTransition, ArrayList<Transition> transitions) {
		this.setType(type);
		this.setPendingTransitions(transitions);
		this.setLaunchedTransition(chosenTransition);
	}

	/**
	 * Konstruktor obiektu klasy SimulationStep
	 * @param type SimulatorMode - tryb pracy symulatora 
	 * @param launchedTransitions ArrayList[Transition] - lista tranzycji do uruchomienia
	 */
	public SimulationStep(SimulatorMode type, ArrayList<Transition> launchedTransitions) {
		this.setType(type);
		this.setPendingTransitions(launchedTransitions);
	}

	/**
	 * Metoda pozwala pobrać tryb symulacji, w jakim zapisany został krok.
	 * @return SimulatorMode - tryb symulacji
	 */
	public SimulatorMode getType() {
		return type;
	}

	/**
	 * Metoda pozwala ustawić tryb symulacji, w jakim zapisany został krok.
	 * @param type SimulatorMode - tryb symulacji
	 */
	public void setType(SimulatorMode type) {
		this.type = type;
	}

	/**
	 * Metoda pozwala pobrać listę uruchomionych tranzycji (lub oczekujących na
	 * odpalenie, jeśli krok dotyczy pojedynczej tranzycji).
	 * @return ArrayList[Transition] - lista tranzycji
	 */
	public ArrayList<Transition> getPendingTransitions() {
		return pendingTransitions;
	}

	/**
	 * Metoda pozwala ustawić listę uruchomionych tranzycji (lub oczekujących na odpalenie,
	 * jeśli krok dotyczy pojedynczej tranzycji).
	 * @param pendingTransitions ArrayList[Transition] - lista tranzycji
	 */
	public void setPendingTransitions(ArrayList<Transition> pendingTransitions) {
		this.pendingTransitions = pendingTransitions;
	}

	/**
	 * Metoda pozwala pobrać uruchomioną tranzycję.
	 * @return Transition - uruchomiona tranzycja
	 */
	public Transition getLaunchedTransition() {
		return launchedTransition;
	}

	/**
	 * Metoda pozwala ustawić uruchomioną tranzycję.
	 * @param launchedTransition Transition - uruchomiona tranzycja
	 */
	public void setLaunchedTransition(Transition launchedTransition) {
		this.launchedTransition = launchedTransition;
	}
}
