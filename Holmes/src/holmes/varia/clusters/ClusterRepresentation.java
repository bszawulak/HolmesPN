package holmes.varia.clusters;

import java.util.ArrayList;

/**
 * Klasa pomocnicza w procesie tworzenia pliku excel tabeli klastrów
 */
public class ClusterRepresentation {
	public ArrayList<Integer> nrInvariantsPerCluster = new ArrayList<Integer>();
	public ArrayList<Double> mssPerCluster = new ArrayList<Double>();
	public double meanValue;
	public int nrClusters;
	
	public ClusterRepresentation() {}
	
	public double ParseDouble(String strNumber) {
	   if (strNumber != null && strNumber.length() > 0) {
	       try {
	          return Double.parseDouble(strNumber);
	       } catch(Exception e) {
	          return -1;   
	       }
	   }
	   else return 0;
	}
}
