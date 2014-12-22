package abyss.clusters;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.files.io.ClusterReader;

/**
 * Klasa zarz�dzaj�ca informacjami o klastrowaniach danej sieci.
 * @author MR
 *
 */
public class ClusteringInfoMatrix {
	private ArrayList<ArrayList<Clustering>> bigTable; //56 przypadk�w, ka�dy po liczbnie od 2 do x klastrowa�
	
	/**
	 * Konstruktor domy�lny obiektu klasy ClusteringInfoMatrix.
	 */
	public ClusteringInfoMatrix() {
		
	}
	
	public void readDataDirectory(String path) {
		GUIManager.getDefaultGUIManager().log("Attempting to read cluster directory: "+path, "text", true);
		ClusterReader reader = new ClusterReader();
		ArrayList<ArrayList<Clustering>> send = reader.readDirectory(path);
		setMatrix(send);
	}
	
	/**
	 * Metoda ustawia now� tablic� klastrowa�.
	 * @param newTable ArrayList[ArrayList[Clustering]] - nowa tablica
	 */
	public void setMatrix(ArrayList<ArrayList<Clustering>> newTable) {
		bigTable = newTable;
	}
	
	/**
	 * Metoda zwraca tablic� klastrowa�.
	 * @return ArrayList[ArrayList[Clustering]] - tablica
	 */
	public ArrayList<ArrayList<Clustering>> getMatrix() {
		return bigTable;
	}
}
