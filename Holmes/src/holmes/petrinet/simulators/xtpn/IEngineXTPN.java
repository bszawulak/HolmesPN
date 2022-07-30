package holmes.petrinet.simulators.xtpn;

import java.util.ArrayList;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.IRandomGenerator;
import holmes.petrinet.simulators.SimulatorGlobals;

/**
 * Interface silnika symulatorów typu XTPN.
 *
 */
public interface IEngineXTPN {
    public void setEngine(SimulatorGlobals.SimNetType simulationType, ArrayList<Transition> transitions,
                          ArrayList<Place> places);

    public void setNetSimType(SimulatorGlobals.SimNetType simulationType);

    public IRandomGenerator getGenerator();
}
