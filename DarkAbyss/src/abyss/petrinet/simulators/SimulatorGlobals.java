package abyss.petrinet.simulators;

public class SimulatorGlobals {
	private int ARC_STEP_DELAY = 25;
	private int TRANS_FIRING_DELAY = 25;
	
	
	public SimulatorGlobals() {
		
	}
	
	public void setArcDelay(int value) {
		if(value < 5)
			this.ARC_STEP_DELAY = 5;
		
		this.ARC_STEP_DELAY = value;
	}
	
	public int getArcDelay() {
		return ARC_STEP_DELAY;
	}
	
	public void setTransDelay(int value) {
		if(value < 10)
			this.TRANS_FIRING_DELAY = 10;
		
		this.TRANS_FIRING_DELAY = value;
	}
	
	public int getTransDelay() {
		return TRANS_FIRING_DELAY;
	}
}
