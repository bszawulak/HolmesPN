package holmes.analyse.XTPN;

import holmes.petrinet.elements.TransitionXTPN;

public class TransitionNanoXTPN {
    /**minimalny czas aktywacji (double)*/
    public double alphaL;
    /**maksymlany czas aktywacji (double)*/
    public double alphaU;
    /**minimalny czas produkcji (double)*/
    public double betaL;
    /**maksymalny czas produkcji (double)*/
    public double betaU;
    /**minimalny czas aktywacji (int)*/
    public int alphaL_N;
    /**maksymlany czas aktywacji (int)*/
    public int alphaU_N;
    /**minimalny czas produkcji (int)*/
    public int betaL_N;
    /**maksymalny czas produkcji (double)*/
    public int betaU_N;

    /**do ilu liczy zegar aktywacji*/
    public int tauAlphaLimit;
    /**do ilu liczy zegar produkcji*/
    public int tauBetaLimit;

    public boolean isAlphaType = true;
    public boolean isBetaType = true;

    /**Zegar czasu aktywacji (double)*/
    public double tauAlpha;
    /**Zegar czasu aktywacji (double)*/
    public double tauBeta;
    /**Zegar czasu produkcji (int)*/
    public int tauAlpha_N;
    /**Zegar czasu produkcji (int)*/
    public int tauBeta_N;

    //algorytm znajdowania maksymalnej liczby kroków:
    public boolean hasReadArcToPlace = false;
    public int weightToPlace = 0;

    public int globalID = -1;
    public int localID = -1;
    public String name = "";

    public TransitionNanoXTPN() {
    }

    /**
     * Konstruktor kopiujący z klasy TransitionXTPN
     * @param trans TransitionNanoXTPN
     */
    public TransitionNanoXTPN(TransitionXTPN trans, int localID) {
        this.localID = localID;
        this.globalID = trans.getID();
        this.name = trans.getName();

        this.alphaL = trans.getAlphaMinValue();
        this.alphaU = trans.getAlphaMaxValue();
        this.betaL = trans.getBetaMinValue();
        this.betaU = trans.getBetaMaxValue();
    }
}
