package holmes.analyse.XTPN;

import holmes.petrinet.elements.PlaceXTPN;
import java.util.ArrayList;

public class PlaceNanoXTPN {
    public double gammaL;
    public double gammaU;
    public int gammaL_N;
    public int gammaU_N;

    public int globalID = -1;
    public int localID = -1;

    public String name = "";

    public ArrayList<Double> tokens = new ArrayList<Double>();
    public ArrayList<Integer> tokens_N = new ArrayList<Integer>();

    public PlaceNanoXTPN() {
    }

    public PlaceNanoXTPN(PlaceXTPN place, int localID) {
        this.localID = localID;
        this.globalID = place.getID();
        this.name = place.getName();
        this.gammaL = place.getGammaMinValue();
        this.gammaU = place.getGammaMaxValue();
    }
}
