package abyss.files.clusters;

import java.util.ArrayList;

import abyss.math.ClusterTransition;

/**
 * Klasa kontener s³u¿¹ca do przechowywania danych przesy³anych z podokna klastrowania
 * szczegó³owego do podokna programu Abyss s³u¿¹cego podœwietlaniu elementów klastra.
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
