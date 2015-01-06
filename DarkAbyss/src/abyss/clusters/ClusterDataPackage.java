package abyss.clusters;

import java.util.ArrayList;

/**
 * Klasa kontener służąca do przechowywania danych przesyłanych z podokna klastrowania
 * szczegółowego do podokna programu Abyss służącego podświetlaniu elementów klastra.
 * @author MR
 *
 */
public class ClusterDataPackage {
	public ArrayList<ArrayList<ClusterTransition>> dataMatrix;
	public String algorithm;
	public String metric;
	public int clNumber;
	public ArrayList<Integer> clSize;
	
	public boolean showFirings = false;
	public boolean showScale = false;
}
