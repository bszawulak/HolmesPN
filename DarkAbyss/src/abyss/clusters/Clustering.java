package abyss.clusters;

public class Clustering {
	public String algorithmName;
	public String metricName;
	public int invNumber;
	public int clusterNumber;
	public int zeroClusters; //ile klastrów z 1 inwariantem
	public float evalMSS;
	public int evalCH;
	
	public int clusterSize[]; //wagi poszczególnych klastrów
	public float clusterMSS[]; //MSS poszczególnych klastrów
	public float vectorMSS[]; //6 wartoœci
	
	public Clustering() {
		algorithmName = "";
		metricName = "";
		invNumber = 0;
		clusterNumber = 0;
		zeroClusters = 0;
		evalMSS = 0.0f;
		evalCH = 0;
		
		clusterSize = new int[2];
		clusterMSS = new float[2];
		vectorMSS = new float[6];
	}
}
