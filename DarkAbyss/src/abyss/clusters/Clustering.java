package abyss.clusters;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa kontener do przechowywania podstawowych metadanych o konkretnym klastrowaniu.
 * @author MR
 *
 */
public class Clustering implements Serializable {
	private static final long serialVersionUID = -4844202084986125686L;
	public String algorithmName;
	public String metricName;
	public int invNumber;
	public int clusterNumber;
	public int zeroClusters; //ile klastrów z 1 inwariantem
	public double evalMSS;
	public double evalCH;
	
	public ArrayList<Integer> clusterSize;
	public ArrayList<Float> clusterMSS;
	public ArrayList<Float> vectorMSS;
	//public int clusterSize[]; //wagi poszczególnych klastrów
	//public float clusterMSS[]; //MSS poszczególnych klastrów
	//public float vectorMSS[]; //6 wartoœci
	
	//extended:
	public int transNumber;
	public int MCTnumber;
	
	/**
	 * Konstruktor domyœlny obiektu klasy Clustering.
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
		//clusterSize = new int[2];
		//clusterMSS = new float[2];
		//vectorMSS = new float[6];
		
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
		
		/*
		int arraySize = source.clusterSize.length;
		result.clusterSize = new int[arraySize];
		for(int i=0; i< arraySize; i++)
			result.clusterSize[i] = source.clusterSize[i];
		
		arraySize = source.clusterMSS.length;
		result.clusterMSS = new float[arraySize];
		for(int i=0; i< arraySize; i++)
			result.clusterMSS[i] = source.clusterMSS[i];
		
		arraySize = source.vectorMSS.length;
		result.vectorMSS = new float[arraySize];
		for(int i=0; i< arraySize; i++)
			result.vectorMSS[i] = source.vectorMSS[i];
		*/
		
				
		return result;
	}
}
