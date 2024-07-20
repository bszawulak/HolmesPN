package holmes.analyse.XTPN;

import holmes.petrinet.elements.PlaceXTPN;
import java.util.ArrayList;
import java.util.Collections;

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

    public void updateTokensSet_N(int i) {
        tokens_N.replaceAll(integer -> integer + i);
        //usuwanie tokenów, które przekroczyły czas maxTime:
        for (int j = 0; j < tokens_N.size(); j++) {
            if(tokens_N.get(j) > gammaU_N) {
                tokens_N.remove(j);
                j--;
            }
        }
    }

    /**
     * Usuwanie i największych elementów z listy tokens_N.
     */
    public void removeTokens_N(int tokensToRemove) {
        for(int i=0; i<tokensToRemove; i++) {
            Integer max = Collections.max(tokens_N);
            tokens_N.remove(max);
        }
    }
}
