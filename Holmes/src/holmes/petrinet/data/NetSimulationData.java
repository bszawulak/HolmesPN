package holmes.petrinet.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import holmes.petrinet.simulators.SimulatorGlobals;

/**
 * Klasa kontener, do przechowywania danych o wynikach wielokrotnej symulacji sieci.
 */
public class NetSimulationData implements Serializable {
	@Serial
	private static final long serialVersionUID = -6460640360048894282L;
	//dane o symulacji:
	public int placesNumber = 0;
	public int transNumber = 0;
	public boolean maxMode = false;
	public SimulatorGlobals.SimNetType netSimType = SimulatorGlobals.SimNetType.BASIC;
	public int steps = 0;
	public int reps = 0;
	//inne:
	public boolean refSet = false;
	public String date;
	public ArrayList<Integer> disabledTransitionsIDs;
	public ArrayList<Integer> disabledMCTids;
	public ArrayList<Integer> disabledTotals;
	public StatePlacesVector startingState;
	
	//wyniki symulacji:
	public ArrayList<Double> placeTokensAvg;
	public ArrayList<Double> placeTokensMin;
	public ArrayList<Double> placeTokensMax;
	public ArrayList<Integer> placeZeroTokens;
	public ArrayList<Double> placeStdDev;
	public ArrayList<ArrayList<Integer>> placeWithinStdDev;
	public ArrayList<Double> transFiringsAvg;
	public ArrayList<Double> transFiringsMin;
	public ArrayList<Double> transFiringsMax;
	public ArrayList<Integer> transZeroFiring;
	public ArrayList<Double> transStdDev;
	public ArrayList<ArrayList<Integer>> transWithinStdDev;
	
	private long idSeries = -1;
	
	//private String data = "";
	
	public NetSimulationData() {
		placeTokensAvg = new ArrayList<Double>();
		placeTokensMin = new ArrayList<Double>();
		placeTokensMax = new ArrayList<Double>();
		placeZeroTokens = new ArrayList<Integer>();
		placeStdDev = new ArrayList<Double>();
		placeWithinStdDev = new ArrayList<ArrayList<Integer>>();
		
		transFiringsAvg = new ArrayList<Double>();
		transFiringsMin = new ArrayList<Double>();
		transFiringsMax = new ArrayList<Double>();
		transZeroFiring = new ArrayList<Integer>();
		transStdDev = new ArrayList<Double>();
		transWithinStdDev = new ArrayList<ArrayList<Integer>>();
		
		disabledTransitionsIDs = new ArrayList<Integer>();
		disabledMCTids = new ArrayList<Integer>();
		disabledTotals = new ArrayList<Integer>();
		//startingState = new ArrayList<Integer>();
	}
	
	public void setIDseries(long value) {
		this.idSeries = value;
	}
	
	public long getIDseries() {
		return this.idSeries;
	}
}
