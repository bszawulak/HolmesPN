package abyss.clusters;

import java.util.ArrayList;

/**
 * Klasa zarz¹dzaj¹ca informacjami o klastrowaniach danej sieci.
 * @author MR
 *
 */
public class ClusteringInfoMatrix {
	private ArrayList<ArrayList<Clustering>> bigTable; //56 przypadków, ka¿dy po liczbnie od 2 do x klastrowañ
	
	//ArrayList
	public ClusteringInfoMatrix() {
		
	}
	
	public void setMatrix(ArrayList<ArrayList<Clustering>> newTable) {
		bigTable = newTable;
	}
	
	public ArrayList<ArrayList<Clustering>> getMatrix() {
		return bigTable;
	}
}
