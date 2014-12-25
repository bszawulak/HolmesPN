package abyss.clusters;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.files.io.ClusterReader;

/**
 * Klasa zarz¹dzaj¹ca informacjami o klastrowaniach danej sieci.
 * @author MR
 *
 */
public class ClusteringInfoMatrix {
	private ArrayList<ArrayList<Clustering>> bigTable; //56 przypadków, ka¿dy po liczbnie od 2 do x klastrowañ
	public int mainTablesNumber = 0;
	public boolean secondaryTablesSameSize = true;
	public int secondaryTablesMinNumber = 0;
	public boolean matrixFull = false;
	
	/**
	 * Konstruktor domyœlny obiektu klasy ClusteringInfoMatrix.
	 */
	public ClusteringInfoMatrix() {
		
	}
	
	/**
	 * Metoda odpowiedzialna za wczytanie plików z danego katalogu do tabeli danych klastrów.
	 * @param path String - œcie¿ka do katalogu
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
				tmp2ndSize = receivedMatrix.get(0).size(); //pierwsza tablica ma wielkoœæ referencyjna (liczba klastrów)
				secondaryTablesMinNumber = tmp2ndSize;
			} else {
				if(tmp2ndSize != receivedMatrix.get(i).size()) { //jeœli jakaœ nastêpna ma inn¹ liczbe klastrów
					secondaryTablesSameSize = false; //problem...
					
					if(secondaryTablesMinNumber > receivedMatrix.get(i).size()) {
						secondaryTablesMinNumber = receivedMatrix.get(i).size(); // przyjmujemy minimaln¹ istniej¹c¹
					}
				}
			}
			
		}
		setMatrix(receivedMatrix);
		return 0;
	}
	
	/**
	 * Metoda zwraca podtabelê danych z g³ównej tabeli danych.
	 * @param id56 int - id I rzêdzu
	 * @param idRow int - id II rzedu (podtabela)
	 * @return Clustering - krotka danych, klasa kontener informacji o klastrowaniu
	 */
	public Clustering getClustering(int id56, int idRow) {
		return bigTable.get(id56).get(idRow);		
	}
	
	/**
	 * Metoda ustawia now¹ tablicê klastrowañ.
	 * @param newTable ArrayList[ArrayList[Clustering]] - nowa tablica
	 */
	public void setMatrix(ArrayList<ArrayList<Clustering>> newTable) {
		bigTable = newTable;
	}
	
	/**
	 * Metoda zwraca tablicê klastrowañ.
	 * @return ArrayList[ArrayList[Clustering]] - tablica
	 */
	public ArrayList<ArrayList<Clustering>> getMatrix() {
		return bigTable;
	}
}
