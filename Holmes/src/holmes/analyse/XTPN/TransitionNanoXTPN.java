package holmes.analyse.XTPN;

import holmes.petrinet.elements.TransitionXTPN;

public class TransitionNanoXTPN {
    public double alphaL;
    public double alphaU;
    public double betaL;
    public double betaU;
    public int alphaL_N;
    public int alphaU_N;
    public int betaL_N;
    public int betaU_N;

    public double tauAlpha;
    public double tauBeta;
    public int tauAlpha_N;
    public int tauBeta_N;

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
