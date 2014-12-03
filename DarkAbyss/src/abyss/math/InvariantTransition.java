package abyss.math;

public class InvariantTransition {
	private Transition transition;
	private Integer amountOfFirings;
	
	public InvariantTransition(Transition trans, Integer firings) {
		setTransition(trans);
		setAmountOfFirings(firings);
	}

	public Transition getTransition() {
		return transition;
	}

	public void setTransition(Transition transition) {
		this.transition = transition;
	}

	public Integer getAmountOfFirings() {
		return amountOfFirings;
	}

	public void setAmountOfFirings(Integer amountOfFirings) {
		this.amountOfFirings = amountOfFirings;
	}

}
