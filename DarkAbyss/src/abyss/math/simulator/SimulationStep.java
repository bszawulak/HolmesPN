package abyss.math.simulator;

import java.util.ArrayList;

import abyss.math.Transition;
import abyss.math.simulator.NetSimulator.SimulatorMode;

/**
 * Klasa przechowuj�ca krok symulacji. Za ka�dym razem, gdy wykonywany jest krok symulacji
 * (r�wnie� w trybach zap�tlonych), na stos dodany zostaje obiekt tej klasy przechowuj�cy
 * informacje o nim. Dzi�ki temu mo�liwa by�a implementacja tryb�w cofania.
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
	 * Metoda pozwala pobra� tryb symulacji, w jakim zapisany zosta� krok.
	 * @return SimulatorMode - tryb symulacji
	 */
	public SimulatorMode getType() {
		return type;
	}

	/**
	 * Metoda pozwala ustawi� tryb symulacji, w jakim zapisany zosta� krok.
	 * @param type SimulatorMode - tryb symulacji
	 */
	public void setType(SimulatorMode type) {
		this.type = type;
	}

	/**
	 * Metoda pozwala pobra� list� uruchomionych tranzycji (lub oczekuj�cych na
	 * odpalenie, je�li krok dotyczy pojedynczej tranzycji).
	 * @return ArrayList[Transition] - lista tranzycji
	 */
	public ArrayList<Transition> getPendingTransitions() {
		return pendingTransitions;
	}

	/**
	 * Metoda pozwala ustawi� list� uruchomionych tranzycji (lub oczekuj�cych na odpalenie,
	 * je�li krok dotyczy pojedynczej tranzycji).
	 * @param pendingTransitions ArrayList[Transition] - lista tranzycji
	 */
	public void setPendingTransitions(ArrayList<Transition> pendingTransitions) {
		this.pendingTransitions = pendingTransitions;
	}

	/**
	 * Metoda pozwala pobra� uruchomion� tranzycj�.
	 * @return Transition - uruchomiona tranzycja
	 */
	public Transition getLaunchedTransition() {
		return launchedTransition;
	}

	/**
	 * Metoda pozwala ustawi� uruchomion� tranzycj�.
	 * @param launchedTransition Transition - uruchomiona tranzycja
	 */
	public void setLaunchedTransition(Transition launchedTransition) {
		this.launchedTransition = launchedTransition;
	}
}
