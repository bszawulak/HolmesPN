package abyss.files.clusters;

import java.util.ArrayList;

public class ClusterRepresentation {
	ArrayList<Integer> nrInvariantsPerCluster = new ArrayList<Integer>();
	ArrayList<Double> mssPerCluster = new ArrayList<Double>();
	double meanValue;
	int nrClusters;
	
	ClusterRepresentation() {}
	
	double ParseDouble(String strNumber) {
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
