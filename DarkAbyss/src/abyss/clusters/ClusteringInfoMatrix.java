package abyss.clusters;

import java.io.Serializable;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.files.clusters.ClusterReader;

/**
 * Klasa zarządzająca informacjami o klastrowaniach danej sieci.
 * @author MR
 *
 */
public class ClusteringInfoMatrix implements Serializable {
	private static final long serialVersionUID = 5927650729868670543L;
	private ArrayList<ArrayList<Clustering>> bigTable; //56 przypadków, każdy po liczbnie od 2 do x klastrowań
	public int mainTablesNumber = 0;
	public boolean secondaryTablesSameSize = true;
	public int secondaryTablesMinNumber = 0;
	//public boolean matrixFull = false;
	
	/**
	 * Konstruktor domyślny obiektu klasy ClusteringInfoMatrix.
	 */
	public ClusteringInfoMatrix() {
		
	}
	
	/**
	 * Metoda odpowiedzialna za wczytanie plików z danego katalogu do tabeli danych klastrów.
	 * @param path String - ścieżka do katalogu
	 */
	public int readDataDirectory(String path) {
		GUIManager.getDefaultGUIManager().log("Attempting to read cluster directory: "+path, "text", true);
		ClusterReader reader = new ClusterReader();
		ArrayList<ArrayList<Clustering>> receivedMatrix = reader.readDirectory(path);
		
		if(receivedMatrix == null) {
			GUIManager.getDefaultGUIManager().log("Reading operation failed. Clusters data matrix has not been created.", "error", true);
			return -1;
		}
		
		int tmp2ndSize = 0;
		for(int i=0; i<receivedMatrix.size(); i++) { //dla 56 tabel
			mainTablesNumber++;
			if(i==0) {
				tmp2ndSize = receivedMatrix.get(0).size(); //pierwsza tablica ma wielkość referencyjna (liczba klastrów)
				secondaryTablesMinNumber = tmp2ndSize;
			} else {
				if(tmp2ndSize != receivedMatrix.get(i).size()) { //jeśli jakaś następna ma inną liczbe klastrów
					secondaryTablesSameSize = false; //problem...
					
					if(secondaryTablesMinNumber > receivedMatrix.get(i).size()) {
						secondaryTablesMinNumber = receivedMatrix.get(i).size(); // przyjmujemy minimalną istniejącą
					}
				}
			}
			
		}
		setMatrix(receivedMatrix);
		return 0;
	}
	
	/**
	 * Metoda zwraca podtabelę danych z głównej tabeli danych.
	 * @param id56 int - id I rzędzu
	 * @param idRow int - id II rzędu (podtabela)
	 * @return Clustering - krotka danych, klasa kontener informacji o klastrowaniu
	 */
	public Clustering getClustering(int id56, int idRow) {
		return bigTable.get(id56).get(idRow);		
	}
	
	/**
	 * Metoda ustawia nową tablicę klastrowań.
	 * @param newTable ArrayList[ArrayList[Clustering]] - nowa tablica
	 */
	public void setMatrix(ArrayList<ArrayList<Clustering>> newTable) {
		bigTable = newTable;
	}
	
	/**
	 * Metoda zwraca tablicę klastrowań.
	 * @return ArrayList[ArrayList[Clustering]] - tablica
	 */
	public ArrayList<ArrayList<Clustering>> getMatrix() {
		return bigTable;
	}
}
