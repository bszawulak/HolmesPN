package holmes.petrinet.elements.containers;

import java.util.ArrayList;

public class TransitionXTPNhistoryContainer {
    //sim:
    public int simInactiveState = 0;
    public int simActiveState = 0;
    public int simProductionState = 0;
    public int simFiredState = 0;
    public double simInactiveTime = 0.0;
    public double simActiveTime = 0.0;
    public double simProductionTime = 0.0;
    public ArrayList<Double> statesHistory = new ArrayList<>();
    public ArrayList<Double> statesTimeHistory = new ArrayList<>();
    public boolean storeHistory = false;

    public void resetSimVariables_XTPN() {
        simInactiveState = 0;
        simActiveState = 0;
        simProductionState = 0;
        simFiredState = 0;

        simInactiveTime = 0.0;
        simActiveTime = 0.0;
        simProductionTime = 0.0;
    }

    public void cleanHistoryVectors() {
        storeHistory = false;
        statesHistory.clear();
        statesTimeHistory.clear();
    }

    public void addHistoryMoment(double state, double time) {
        statesHistory.add(state);
        statesTimeHistory.add(time);
    }
}
