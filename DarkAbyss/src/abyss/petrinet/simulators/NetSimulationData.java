package abyss.petrinet.simulators;

import java.util.ArrayList;

import abyss.petrinet.simulators.NetSimulator.NetType;

/**
 * Klasa kontener, do przechowywania danych o wynikach wielokrotnej symulacji sieci
 * @author Rince
 */
public class NetSimulationData {
	//dane o symulacji:
	public int placesNumber = 0;
	public int transNumber = 0;
	public boolean maxMode = false;
	public NetType netSimType = NetType.BASIC;
	public int steps = 0;
	public int reps = 0;
	//inne:
	public boolean refSet = false;
	
	//wyniki symulacji:
	public ArrayList<Double> refPlaceTokensAvg = null;
	public ArrayList<Double> refPlaceTokensMin = null;
	public ArrayList<Double> refPlaceTokensMax = null;
	public ArrayList<Double> refTransFiringsAvg = null;
	public ArrayList<Double> refTransFiringsMin = null;
	public ArrayList<Double> refTransFiringsMax = null;
	public ArrayList<Integer> simsWithZeroFiring = null;
	public ArrayList<Integer> simsWithZeroTokens = null;
	
	public NetSimulationData() {
		refPlaceTokensAvg = new ArrayList<Double>();
		refPlaceTokensMin = new ArrayList<Double>();
		refPlaceTokensMax = new ArrayList<Double>();
		refTransFiringsAvg = new ArrayList<Double>();
		refTransFiringsMin = new ArrayList<Double>();
		refTransFiringsMax = new ArrayList<Double>();
		simsWithZeroFiring = new ArrayList<Integer>();
		simsWithZeroTokens = new ArrayList<Integer>();
	}
}
