package holmes.clusters;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa kontener do przechowywania podstawowych metadanych o konkretnym klastrowaniu.
 * @author MR
 *
 */
public class Clustering implements Serializable {
	private static final long serialVersionUID = -5381441187018735328L;
	public String algorithmName;
	public String metricName;
	public int invNumber;
	public int clusterNumber;
	public int zeroClusters; //ile klastrów z 1 inwariantem
	public double evalMSS;
	public double evalCH;
	
	public ArrayList<Integer> clusterSize; 	//wagi poszczególnych klastrów
	public ArrayList<Float> clusterMSS;		//MSS poszczególnych klastrów
	public ArrayList<Float> vectorMSS;		//6 wartości
	
	//extended:
	public int transNumber;
	public int MCTnumber;
	
	/**
	 * Konstruktor domyślny obiektu klasy Clustering.
	 */
	public Clustering() {
		algorithmName = "";
		metricName = "";
		invNumber = 0;
		clusterNumber = 0;
		zeroClusters = 0;
		evalMSS = 0.0f;
		evalCH = 0;
		
		clusterSize = new ArrayList<Integer>();
		clusterMSS = new ArrayList<Float>();
		vectorMSS = new ArrayList<Float>();
		
		transNumber = 0;
		MCTnumber = 0;
	}
	
	/**
	 * Bezpieczne klonowanie obiektów klasy.
	 * @param source Clustering - obiekt do skopiowania
	 * @return Clustering - nowy obiekt kopia
	 */
	public static Clustering clone(Clustering source) {
		Clustering result = new Clustering();
		result.algorithmName = source.algorithmName;
		result.metricName = source.metricName;
		result.invNumber = source.invNumber;
		result.clusterNumber = source.clusterNumber;
		result.zeroClusters = source.zeroClusters;
		result.evalMSS = source.evalMSS;
		result.evalCH = source.evalCH;
		
		ArrayList<Integer> newClusterSize = new ArrayList<Integer>( source.clusterSize ); 
		result.clusterSize = newClusterSize;
		
		ArrayList<Float> newclusterMSS = new ArrayList<Float>( source.clusterMSS ); 
		result.clusterMSS = newclusterMSS;
		
		ArrayList<Float> newvectorMSS = new ArrayList<Float>( source.vectorMSS ); 
		result.vectorMSS = newvectorMSS;
		
		result.transNumber = source.transNumber;
		result.MCTnumber = source.MCTnumber;
				
		return result;
	}
}
