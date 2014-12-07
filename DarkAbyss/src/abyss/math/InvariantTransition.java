package abyss.math;

/**
 * Klasa definiuj¹ca inwariant dla tranzycji
 * @author students
 *
 */
public class InvariantTransition {
	private Transition transition;
	private Integer amountOfFirings;
	
	/**
	 * Konstruktor obiektu klasy InvariantTransition.
	 * @param trans Transition - tranzycja
	 * @param firings Integer - liczba uruchomieñ w inwariancie
	 */
	public InvariantTransition(Transition trans, Integer firings) {
		setTransition(trans);
		setAmountOfFirings(firings);
	}

	/**
	 * Metoda zwraca tranzycjê dla danego inwariantu.
	 * @return Transition - tranzycja
	 */
	public Transition getTransition() {
		return transition;
	}

	/**
	 * Metoda ustawia tranzycjê dla danego inwariantu.
	 * @return Transition - tranzycja
	 */
	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	/**
	 * Metoda zwracaj¹ca liczbê uruchomieñ tranzycji w inwariancie.
	 * @return Integer - liczba uruchomieñ
	 */
	public Integer getAmountOfFirings() {
		return amountOfFirings;
	}

	/**
	 * Metoda ustawiaj¹ca liczbê uruchomieñ tranzycji w inwariancie.
	 * @return Integer - nowa liczba uruchomieñ
	 */
	public void setAmountOfFirings(Integer amountOfFirings) {
		this.amountOfFirings = amountOfFirings;
	}
}
