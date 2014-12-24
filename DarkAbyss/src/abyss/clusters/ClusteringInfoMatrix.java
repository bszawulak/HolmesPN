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
	
	/**
	 * Konstruktor domyœlny obiektu klasy ClusteringInfoMatrix.
	 */
	public ClusteringInfoMatrix() {
		
	}
	
	/**
	 * Metoda odpowiedzialna za wczytanie plików z danego katalogu do tabeli danych klastrów.
	 * @param path String - œcie¿ka do katalogu
	 */
	public void readDataDirectory(String path) {
		GUIManager.getDefaultGUIManager().log("Attempting to read cluster directory: "+path, "text", true);
		ClusterReader reader = new ClusterReader();
		ArrayList<ArrayList<Clustering>> send = reader.readDirectory(path);
		
		int tmp2ndSize = 0;
		for(int i=0; i<send.size(); i++) { //dla 56 tabel
			mainTablesNumber++;
			if(i==0) {
				tmp2ndSize = send.get(0).size(); //pierwsza tablica ma wielkoœæ referencyjna (liczba klastrów)
				secondaryTablesMinNumber = tmp2ndSize;
			} else {
				if(tmp2ndSize != send.get(i).size()) { //jeœli jakaœ nastêpna ma inn¹ liczbe klastrów
					secondaryTablesSameSize = false; //problem...
					
					if(secondaryTablesMinNumber > send.get(i).size()) {
						secondaryTablesMinNumber = send.get(i).size(); // przyjmujemy minimaln¹ istniej¹c¹
					}
				}
			}
			
		}
		setMatrix(send);
	}
	
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
