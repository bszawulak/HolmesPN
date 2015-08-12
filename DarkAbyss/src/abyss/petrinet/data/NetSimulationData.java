package abyss.petrinet.data;

import java.io.Serializable;
import java.util.ArrayList;

import abyss.petrinet.simulators.NetSimulator.NetType;

/**
 * Klasa kontener, do przechowywania danych o wynikach wielokrotnej symulacji sieci.
 * 
 * @author MR
 */
public class NetSimulationData implements Serializable {
	private static final long serialVersionUID = -6460640360048894282L;
	//dane o symulacji:
	public int placesNumber = 0;
	public int transNumber = 0;
	public boolean maxMode = false;
	public NetType netSimType = NetType.BASIC;
	public int steps = 0;
	public int reps = 0;
	//inne:
	public boolean refSet = false;
	public String date;
	public ArrayList<Integer> disabledTransitionsIDs;
	public ArrayList<Integer> disabledMCTids;
	public ArrayList<Integer> startingState;
	
	//wyniki symulacji:
	public ArrayList<Double> placeTokensAvg;
	public ArrayList<Double> placeTokensMin;
	public ArrayList<Double> placeTokensMax;
	public ArrayList<Double> transFiringsAvg;
	public ArrayList<Double> transFiringsMin;
	public ArrayList<Double> transFiringsMax;
	public ArrayList<Integer> transZeroFiring;
	public ArrayList<Integer> placeZeroTokens;
	
	public NetSimulationData() {
		placeTokensAvg = new ArrayList<Double>();
		placeTokensMin = new ArrayList<Double>();
		placeTokensMax = new ArrayList<Double>();
		placeZeroTokens = new ArrayList<Integer>();
		transFiringsAvg = new ArrayList<Double>();
		transFiringsMin = new ArrayList<Double>();
		transFiringsMax = new ArrayList<Double>();
		transZeroFiring = new ArrayList<Integer>();

		disabledTransitionsIDs = new ArrayList<Integer>();
		disabledMCTids = new ArrayList<Integer>();
		startingState = new ArrayList<Integer>();
	}
}
