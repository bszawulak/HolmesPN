package holmes.petrinet.simulators.xtpn;

import java.util.ArrayList;

import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;
import holmes.petrinet.simulators.IRandomGenerator;
import holmes.petrinet.simulators.SimulatorGlobals;

/**
 * Interface silnika symulatorów typu XTPN.
 *
 */
public interface IEngineXTPN {
    void setEngine(SimulatorGlobals.SimNetType simulationType, ArrayList<TransitionXTPN> transitions,
                          ArrayList<PlaceXTPN> places);

    void setNetSimType(SimulatorGlobals.SimNetType simulationType);

    IRandomGenerator getGenerator();
}
