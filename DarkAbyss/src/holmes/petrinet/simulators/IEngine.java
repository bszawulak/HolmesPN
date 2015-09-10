package holmes.petrinet.simulators;

import java.util.ArrayList;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.NetSimulator.NetType;

/**
 * Interface silnika symulatorów.
 * 
 * @author MR
 *
 */
public interface IEngine {
	public void setEngine(NetType simulationType, boolean maxMode, boolean singleMode, 
			ArrayList<Transition> transitions, ArrayList<Transition> time_transitions,
			ArrayList<Place> places);
	
	public void setNetSimType(NetType simulationType);
	public void setMaxMode(boolean value);
	public void setSingleMode(boolean value);
	public ArrayList<Transition> getTransLaunchList(boolean emptySteps);
	public void setGenerator(IRandomGenerator generator);
}
