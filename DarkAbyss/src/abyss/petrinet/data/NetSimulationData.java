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
	public ArrayList<Integer> startingState;
	
	//wyniki symulacji:
	public ArrayList<Double> refPlaceTokensAvg;
	public ArrayList<Double> refPlaceTokensMin;
	public ArrayList<Double> refPlaceTokensMax;
	public ArrayList<Double> refTransFiringsAvg;
	public ArrayList<Double> refTransFiringsMin;
	public ArrayList<Double> refTransFiringsMax;
	public ArrayList<Integer> simsWithZeroFiring;
	public ArrayList<Integer> simsWithZeroTokens;
	
	public NetSimulationData() {
		refPlaceTokensAvg = new ArrayList<Double>();
		refPlaceTokensMin = new ArrayList<Double>();
		refPlaceTokensMax = new ArrayList<Double>();
		refTransFiringsAvg = new ArrayList<Double>();
		refTransFiringsMin = new ArrayList<Double>();
		refTransFiringsMax = new ArrayList<Double>();
		simsWithZeroFiring = new ArrayList<Integer>();
		simsWithZeroTokens = new ArrayList<Integer>();
		
		disabledTransitionsIDs = new ArrayList<Integer>();
		startingState = new ArrayList<Integer>();
	}
}
