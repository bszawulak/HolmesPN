package abyss.clusters;

import java.io.Serializable;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.files.clusters.ClusterReader;

/**
 * Klasa zarz�dzaj�ca informacjami o klastrowaniach danej sieci.
 * @author MR
 *
 */
public class ClusteringInfoMatrix implements Serializable {
	private static final long serialVersionUID = 5927650729868670543L;
	private ArrayList<ArrayList<Clustering>> bigTable; //56 przypadk�w, ka�dy po liczbnie od 2 do x klastrowa�
	public int mainTablesNumber = 0;
	public boolean secondaryTablesSameSize = true;
	public int secondaryTablesMinNumber = 0;
	//public boolean matrixFull = false;
	
	/**
	 * Konstruktor domy�lny obiektu klasy ClusteringInfoMatrix.
	 */
	public ClusteringInfoMatrix() {
		
	}
	
	/**
	 * Metoda odpowiedzialna za wczytanie plik�w z danego katalogu do tabeli danych klastr�w.
	 * @param path String - �cie�ka do katalogu
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
				tmp2ndSize = receivedMatrix.get(0).size(); //pierwsza tablica ma wielko�� referencyjna (liczba klastr�w)
				secondaryTablesMinNumber = tmp2ndSize;
			} else {
				if(tmp2ndSize != receivedMatrix.get(i).size()) { //je�li jaka� nast�pna ma inn� liczbe klastr�w
					secondaryTablesSameSize = false; //problem...
					
					if(secondaryTablesMinNumber > receivedMatrix.get(i).size()) {
						secondaryTablesMinNumber = receivedMatrix.get(i).size(); // przyjmujemy minimaln� istniej�c�
					}
				}
			}
			
		}
		setMatrix(receivedMatrix);
		return 0;
	}
	
	/**
	 * Metoda zwraca podtabel� danych z g��wnej tabeli danych.
	 * @param id56 int - id I rz�dzu
	 * @param idRow int - id II rzedu (podtabela)
	 * @return Clustering - krotka danych, klasa kontener informacji o klastrowaniu
	 */
	public Clustering getClustering(int id56, int idRow) {
		return bigTable.get(id56).get(idRow);		
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
