package abyss.math.simulator;

import java.util.ArrayList;

import abyss.math.Transition;
import abyss.math.simulator.NetSimulator.SimulatorMode;

/**
 * Klasa przechowuj¹ca krok symulacji. Za ka¿dym razem, gdy wykonywany jest krok symulacji
 * (równie¿ w trybach zapêtlonych), na stos dodany zostaje obiekt tej klasy przechowuj¹cy
 * informacje o nim. Dziêki temu mo¿liwa by³a implementacja trybów cofania.
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
	 * Metoda pozwala pobraæ tryb symulacji, w jakim zapisany zosta³ krok.
	 * @return SimulatorMode - tryb symulacji
	 */
	public SimulatorMode getType() {
		return type;
	}

	/**
	 * Metoda pozwala ustawiæ tryb symulacji, w jakim zapisany zosta³ krok.
	 * @param type SimulatorMode - tryb symulacji
	 */
	public void setType(SimulatorMode type) {
		this.type = type;
	}

	/**
	 * Metoda pozwala pobraæ listê uruchomionych tranzycji (lub oczekuj¹cych na
	 * odpalenie, jeœli krok dotyczy pojedynczej tranzycji).
	 * @return ArrayList[Transition] - lista tranzycji
	 */
	public ArrayList<Transition> getPendingTransitions() {
		return pendingTransitions;
	}

	/**
	 * Metoda pozwala ustawiæ listê uruchomionych tranzycji (lub oczekuj¹cych na odpalenie,
	 * jeœli krok dotyczy pojedynczej tranzycji).
	 * @param pendingTransitions ArrayList[Transition] - lista tranzycji
	 */
	public void setPendingTransitions(ArrayList<Transition> pendingTransitions) {
		this.pendingTransitions = pendingTransitions;
	}

	/**
	 * Metoda pozwala pobraæ uruchomion¹ tranzycjê.
	 * @return Transition - uruchomiona tranzycja
	 */
	public Transition getLaunchedTransition() {
		return launchedTransition;
	}

	/**
	 * Metoda pozwala ustawiæ uruchomion¹ tranzycjê.
	 * @param launchedTransition Transition - uruchomiona tranzycja
	 */
	public void setLaunchedTransition(Transition launchedTransition) {
		this.launchedTransition = launchedTransition;
	}
}
