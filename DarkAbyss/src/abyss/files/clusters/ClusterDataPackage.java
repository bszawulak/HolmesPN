package abyss.files.clusters;

import java.util.ArrayList;

import abyss.math.ClusterTransition;

/**
 * Klasa kontener s�u��ca do przechowywania danych przesy�anych z podokna klastrowania
 * szczeg�owego do podokna programu Abyss s�u��cego pod�wietlaniu element�w klastra.
 * @author MR
 *
 */
public class ClusterDataPackage {
	public ArrayList<ArrayList<ClusterTransition>> dataMatrix;
	public String algorithm;
	public String metric;
	public int clNumber;
	
	public boolean showFirings = false;
	public boolean showScale = false;
}
