package abyss.math;

/**
 * Klasa rozszerzenie dla Transition. Definiuj dla ka�dej tranzycji jej lizb� odpale�.
 * Jest ona u�ywana jako pojedzy�czy obiekt dla macierzy inwariant�w II typu. 
 * @author students
 *
 */
public class InvariantTransition {
	private Transition transition;
	private Integer amountOfFirings;
	
	/**
	 * Konstruktor obiektu klasy InvariantTransition.
	 * @param trans Transition - tranzycja
	 * @param firings Integer - liczba uruchomie� w inwariancie
	 */
	public InvariantTransition(Transition trans, Integer firings) {
		setTransition(trans);
		setAmountOfFirings(firings);
	}

	/**
	 * Metoda zwraca tranzycj� dla danego inwariantu.
	 * @return Transition - tranzycja
	 */
	public Transition getTransition() {
		return transition;
	}

	/**
	 * Metoda ustawia tranzycj� dla danego inwariantu.
	 * @return Transition - tranzycja
	 */
	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	/**
	 * Metoda zwracaj�ca liczb� uruchomie� tranzycji w inwariancie.
	 * @return Integer - liczba uruchomie�
	 */
	public Integer getAmountOfFirings() {
		return amountOfFirings;
	}

	/**
	 * Metoda ustawiaj�ca liczb� uruchomie� tranzycji w inwariancie.
	 * @return Integer - nowa liczba uruchomie�
	 */
	public void setAmountOfFirings(Integer amountOfFirings) {
		this.amountOfFirings = amountOfFirings;
	}
}
