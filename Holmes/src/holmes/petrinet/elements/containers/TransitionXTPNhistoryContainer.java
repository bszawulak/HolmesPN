package holmes.petrinet.elements.containers;

import java.util.ArrayList;

public class TransitionXTPNhistoryContainer {
    /** Sumaryczna liczba stanów nieaktywnych */
    public int simInactiveState = 0;
    /** Sumaryczna liczba stanów aktywnych */
    public int simActiveState = 0;
    /** Sumaryczna liczba stanów produkukcji */
    public int simProductionState = 0;
    /** Sumaryczna liczba uruchomień */
    public int simFiredState = 0;
    /** Sumaryczna czas stanów nieaktywnych */
    public double simInactiveTime = 0.0;
    /** Sumaryczna czas stanów aktywnych */
    public double simActiveTime = 0.0;
    /** Sumaryczna czas stanu produkcji */
    public double simProductionTime = 0.0;
    /** Stan tranzycji w każdym kroku */
    public ArrayList<Double> statesHistory = new ArrayList<>();
    /** Czas dla danego stanu w każdym kroku */
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

    /**
     * Zapis dla każdego kroku w jakim tranzycja była stanie (i jaki to był czas).
     * @param state (<b>double</b>) stan tranzycji.
     * @param time (<b>double</b>) czas.
     */
    public void addHistoryMoment(double state, double time) {
        statesHistory.add(state);
        statesTimeHistory.add(time);
    }
}
