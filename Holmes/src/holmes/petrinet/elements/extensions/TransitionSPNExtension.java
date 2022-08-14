package holmes.petrinet.elements.extensions;

import holmes.petrinet.data.SPNtransitionData;

public class TransitionSPNExtension {
    //tranzycja stochastyczna:
    /** ST, DT, IM, SchT, NONE - Stochastic Transition, Deterministic T., Immediate T., Scheduled T. */
    public enum StochaticsType {ST, DT, IM, SchT, NONE}
    public StochaticsType stochasticType;
    protected double firingRate = 1.0;
    protected SPNtransitionData SPNbox = null;
    //SSA
    protected double SPNprobTime = 0.0;

    /**
     * Metoda zwraca podtyp SPN tranzycji.
     * @return StochaticsType - podtyp tranzycji stochastycznej
     */
    public StochaticsType getSPNtype() {
        return this.stochasticType;
    }

    /**
     * Metoda ustawia podtyp SPN tranzycji.
     * @param value TransitionType -  podtyp tranzycji stochastycznej
     */
    public void setSPNtype(StochaticsType value) {
        this.stochasticType = value;
    }

    /**
     * Metoda zwraca wartość firing rate na potrzeby symulacji SPN.
     * @return double - wartość firing rate
     */
    public double getFiringRate() {
        return this.firingRate;
    }

    /**
     * Metoda ustawia nową wartość firing rate dla tranzycji w modelu SPN.
     * @param firingRate double - nowa wartość
     */
    public void setFiringRate(double firingRate) {
        this.firingRate = firingRate;
    }

    /**
     * Metoda zwraca kontener danych SPN tranzycji.
     * @return SPNtransitionData - kontener danych
     */
    public SPNtransitionData getSPNbox() {
        return this.SPNbox;
    }

    /**
     * Metoda ustawia nowy kontener danych SPN tranzycji.
     * param SPNbox SPNtransitionData - kontener danych
     */
    public void setSPNbox(SPNtransitionData SPNbox) {
        this.SPNbox = SPNbox;
    }

    public void setSPNprobTime(double time) {
        this.SPNprobTime = time;
    }

    public double getSPNprobTime() {
        return this.SPNprobTime;
    }
}
