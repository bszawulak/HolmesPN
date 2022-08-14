package holmes.petrinet.simulators;

import java.util.ArrayList;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Interface silnika symulatorów.
 */
public interface IEngine {
	void setEngine(SimulatorGlobals.SimNetType simulationType, boolean maxMode, boolean singleMode,
						  ArrayList<Transition> transitions, ArrayList<Transition> time_transitions,
						  ArrayList<Place> places);
	
	void setNetSimType(SimulatorGlobals.SimNetType simulationType);
	void setMaxMode(boolean value);
	void setSingleMode(boolean value);
	ArrayList<Transition> getTransLaunchList(boolean emptySteps);
	//public void setGenerator(IRandomGenerator generator);
	IRandomGenerator getGenerator();
}
