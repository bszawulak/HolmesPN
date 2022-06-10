package holmes.petrinet.data;

import holmes.petrinet.elements.Transition;

/**
 * Klasa rozszerzenie dla Transition. Definiuj dla każdej tranzycji jej liczbę uruchomień.
 * Jest ona używana jako pojedyńczy obiekt dla macierzy inwariantów II typu. 
 * @author students
 *
 */
public class InvariantTransition {
	private Transition transition;
	private Integer amountOfFirings;
	
	/**
	 * Konstruktor obiektu klasy InvariantTransition.
	 * @param trans Transition - tranzycja
	 * @param firings Integer - liczba uruchomień w inwariancie
	 */
	public InvariantTransition(Transition trans, Integer firings) {
		setTransition(trans);
		setAmountOfFirings(firings);
	}

	/**
	 * Metoda zwraca tranzycję dla danego inwariantu.
	 * @return Transition - tranzycja
	 */
	public Transition getTransition() {
		return transition;
	}

	/**
	 * Metoda ustawia tranzycję dla danego inwariantu.
	 * @return Transition - tranzycja
	 */
	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	/**
	 * Metoda zwracająca liczbę uruchomień tranzycji w inwariancie.
	 * @return Integer - liczba uruchomień
	 */
	public Integer getAmountOfFirings() {
		return amountOfFirings;
	}

	/**
	 * Metoda ustawiająca liczbę uruchomień tranzycji w inwariancie.
	 * @return Integer - nowa liczba uruchomień
	 */
	public void setAmountOfFirings(Integer amountOfFirings) {
		this.amountOfFirings = amountOfFirings;
	}
}
