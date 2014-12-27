package abyss.clusters;

import java.util.ArrayList;

/**
 * Klasa kontener do przechowywania wszystkich struktur danych opisuj�cych konkretny przypadek
 * klastrowania. Zawiera te� pe�ne dane pliku CSV, mo�liwe jest wi�c z ka�dego jej obiektu
 * odtworzenie danych o inwariantach.
 * W klasie zdefiniowane s� r�wnie� metody obr�bki danych. Zosta�y przetestowane, a autor modli si�
 * �arliwie, aby okaza�y si� prawid�owo napisane. W innym wypadku znajdywanie b��du mo�e si� okaza�
 * 'interesuj�cymi czasami' z chi�skiego przekle�stwa.
 * @author MR
 *
 */
public class ClusteringExtended {
	public Clustering metaData; //meta dane o klastrowaniu
	/**
	 * Wektor przechowuj�cy nazwy tranzycji sieci, indeks to numer porz�dkowy, indeks=0 - puste miejsce
	 */
	public String[] transNames; //nazwy tranzycji
	/**
	 * Tablica inwariant�w, przepisana z pliku CSV. Pierwsza kolumna w ka�dym wierszu to nr porz�dkowy
	 * inwariantu, nast�pnie pola s� r�wne 0 lub wi�cej w zale�no�ci, kt�re tranzycje s� wsparciem
	 * inwariantu. Nr indeksu kolumny w tej macierzy to dok�adnie nr indeksu nazwy tranzycji w transNames.
	 */
	//public int[][] csvInvariants; //tablica inwariant�w z pliku CSV
	public ArrayList<ArrayList<Integer>> csvInvariants; //tablica inwariant�w z pliku CSV
	/**
	 * Nr wpisany w macierzy mctSets to indeks nazwy tranzycji w wektorze String[] transNames. Pierwsza
	 * warto�� indeksu mctSets to oczywi�cie nr zbioru MCT (wiersze)
	 * W transNames indeks to numer porz�dkowy (czyli tranzycja nr 1 ma tam indeks r�wny 1, a nie 0).
	 */
	public ArrayList<ArrayList<Integer>> mctSets;
	
	/**
	 * Macierz klastr�w, ka�da linia to klaster zawieraj�cy id inwariant�w z wektora csvInvariants[id][].
	 */
	public ArrayList<ArrayList<Integer>> clustersInv;
	
	//public int[][] transInClusters; // ile tranzycji (na bazie binarnej liczno�ci inwariant�w)
	//public int[][] transInClustersReal; //ile naprawd� tranzycji w klastrze
	
	//public int MCTinClusters[][];
	//public int transInClustersNoMCT[][];
	//public int transInClustersNoMCTReal[][];
	
	public ClusteringExtended() {
		
	}
	
	/**
	 * Metoda ta zwraca wektor o liczno�ci zbior�w MCT, ka�da liczba oznacza ile razy MCT 
	 * wyst�pi� w 'sumie' inwariant�w klastra
	 * @param clusterIndex int - numer klastra
	 * @return ArrayList<Integer> - ile razy MCT wyst�puje w klastrze
	 */
	public ArrayList<Integer> getMCTFrequencyInCluster(int clusterIndex) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for(int mctInd=0; mctInd < mctSets.size(); mctInd++) {
			ArrayList<Integer> mctRow = mctSets.get(mctInd); //numery tranzycji MCT
			//teraz, dla ka�dego inwariantu klastra nale�y sprawdzi�, czy mctRow cho�
			//raz si� w nim znajduje
			int mctCounterInCluster = 0;

			ArrayList<Integer> invRow = clustersInv.get(clusterIndex); //numery tranzycji inwariantu
			int rowSize = invRow.size();
			for(int invInd=0; invInd < rowSize; invInd++) {
				int invIndex = invRow.get(invInd);
				ArrayList<Integer> transRow = csvInvariants.get(invIndex);
				
				boolean isMCTinInv = isSubset(mctRow, transRow);
				if(isMCTinInv) {
					mctCounterInCluster++;
				}

			}
			result.add(mctCounterInCluster);
		}
		return result;
	}
	
	/**
	 * Metoda zwraca wektor o liczno�ci tranzycji. Pozycja oznacza nr tranzycji wg schematu z 
	 * wektora transNames, liczba oznacza ile razy dana tranzycja wyst�puje w klastrze, z wy��czeniem
	 * zbior�w MCT. 
	 * @param clusterIndex int - nr klastra
	 * @return int[2][] - pierwszy wektor to liczba wyst�puj�cych w klastrze tranzycji kt�re nie
	 * wchodz� w sk�ad zbior�w MCT dla tego klastra, drugi wektor podobnie, z tym �e warto�ci w nim
	 * to rzeczywiste liczby uruchomie� tranzycji w ramach inwariantu (i dalej: w ramach wszystkich
	 * inwariant�w badanego klastra)
	 */
	public int[][] getTransitionFrequencyNoMCT(int clusterIndex, ArrayList<Integer> getMCTFrequencyInCluster) {
		int[] transFrequency = new int[transNames.length]; //binarne wyst�powanie
		int[] realFrequency = new int[transNames.length]; //rzeczywista liczba tranzycji w klastrze
		
		//kolumny: nr inwariant�w klastra, wiersze - nr tranzycji tych klastr�w
		ArrayList<ArrayList<Integer>> invTrans = new ArrayList<ArrayList<Integer>>();
		
		for(int invInd=0; invInd < clustersInv.get(clusterIndex).size(); invInd++) { //dla wszystkich inw.
			int invIndex = clustersInv.get(clusterIndex).get(invInd); //wewn�trzny indeks inwariantu w csvInvariants
			ArrayList<Integer> transRow = new ArrayList<Integer>();
			//transRow.add(-1);
			for(int i=1; i<csvInvariants.get(invIndex).size(); i++) { //dla ka�dej tranzycji
				if(csvInvariants.get(invIndex).get(i) > 0) {
					transRow.add(1); //binarnie: jest, nie ma
					transFrequency[i]++;
					realFrequency[i] += csvInvariants.get(invIndex).get(i);
				}
				else {
					transRow.add(0);
				}
			}
			invTrans.add(transRow); 
			//teraz mamy macierz wyst�powania (binarnego) tranzycji w klastrze: suma ka�dej
			//kolumny to ilo�� wyst�pie� tranzycji w inwariantach klastra ��cznie
		}
		
		int[] mctTransitions = new int[transNames.length];
		for(int mctNumber=0; mctNumber<getMCTFrequencyInCluster.size(); mctNumber++) {
			if(getMCTFrequencyInCluster.get(mctNumber) > 0) { //tylko dla faktycznie wyst�puj�cych MCT
				//teraz dla ka�dego wyst�puj�cego w klastrze MCT przetwarzamy go
				//na jego tranzycje sk�adowe
				ArrayList<Integer> mctRow = mctSets.get(mctNumber);
				for(int j=0; j<mctRow.size(); j++) { //dla wszystkich tranzycji tego MCT
					int inv = mctRow.get(j); 
					mctTransitions[inv] += getMCTFrequencyInCluster.get(mctNumber); 
				}
			}
		}
		//teraz wektor mctTransitions zawiera liczno�� wszystkich tranzycji ze zbior�w MCT
		for(int i=1; i<transFrequency.length; i++) {
			transFrequency[i] -= mctTransitions[i];
			if(mctTransitions[i] > 0)
				realFrequency[i] = 0;
		}
		
		int[][] result = { transFrequency, realFrequency };
		return result;
	}
	
	public ArrayList<String> getNormalizedInvariant(int invNumber) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Integer> invRow = new ArrayList<Integer>( csvInvariants.get(invNumber) ); 
		//z wyj�tkiem I miejsca
		
		result.add("Inv. #"+invRow.get(0));
		String mctCell = "[";

		boolean alreadyCried = false;
		
		//dla ka�dego MCT sprawd�, czy wchodzi w sk�ad inwariantu
		for(int mct=0; mct < mctSets.size(); mct++) { //dla ka�dego zbioru MCT
			ArrayList<Integer> mctVector = mctSets.get(mct);
			int mctSize = mctVector.size();
			int mctPartsFound = 0;
			for(int tr=0; tr < mctVector.size(); tr++) { //dla ka�dej tranzycji MCT
				if(invRow.get(mctVector.get(tr)) > 0) { //je�li w inw. wyst�puje tranzycja o numerze w MCT
					mctPartsFound++;
				}
			}
			
			if(mctPartsFound == mctSize) {//dany MCT wyst�puje w inwariancie
				mctCell += (mct+1)+",";
				//usuwanie �lad�w po MCT w inwariancie:
				for(int tr=0; tr < mctVector.size(); tr++) { //powt�rka z poprzednich iteracji
					int transToRemove = mctVector.get(tr);
					int oldValue = invRow.get(transToRemove);
					if(oldValue > 1) {
						//Hmm, dziwne. A przynajmniej odkryli�my co�, co zainteresuje Adama.
						if(alreadyCried == false) 
							alreadyCried=true;
							//GUIManager.getDefaultGUIManager().log("Logical error MCT:invariant:transition in inv "+invRow.get(0), "warning",true);
						
					}
					invRow.set(transToRemove, 0);
				}
			}
		}
		mctCell += "]";
		mctCell = mctCell.replace(",]", "]");
		result.add(mctCell);
		
		for(int i=1; i<invRow.size(); i++) { // po wszystkich pozosta�ych tranzycjach
			if(invRow.get(i) > 0) {
				String transName = transNames[i];
				int firing = invRow.get(i);
				result.add(transName+":"+firing);
			}
		}
		
		
		return result;
	}
	
	/**
	 * Metoda ta sprawdza, czy elementy zbioru subset (nr tranzycji) znajduj� si� na odpowiednich
	 * miejsach w zbiorze superset (tj. nr tranzycji z subset to indeks z superset i > 0 w tym miejscu)
	 * @param subset ArrayList<Integer> - numery tranzycji w ramach MCT
	 * @param superset ArrayList<Integer> - inwariant
	 * @return boolean - true, je�li zbi�r MCT wchodzi w sk�ad inwariantu
	 */
	private boolean isSubset(ArrayList<Integer> subset, ArrayList<Integer> superset) {
		boolean transFound = false;
		//int supersetSize = superset.size();
		for(int i=0; i<subset.size(); i++) { //dla ka�dej tranzycji z MCT
			transFound = false;
			int transId = subset.get(i);
			for(int j=1; j<superset.size(); j++) { //dla ka�dej tranzycji z inwariantu
				//przy czym I element to nr inwariantu, wi�c pomijamy
				try {
					if(superset.get(transId)>0) {
						transFound = true;
						break;
					}
				} catch (Exception e) {
					return false; //to znaczy, �e jakas tranzycja ma wy�szy nr ni� mo�liwe.
					// To znaczy dalej, �e mamy przesrane, bo gdzie� mi�dzy setkami linii kodu
					// operuj�cymi na setkach tysi�cy liczb jest b��d. Zapewne do�� niewielki...
				}
			}
			if(transFound == false) { //je�li powy�sza p�tla nie znalaz�a tranzycji z MCT w inwariancie
				return false;
			}
		}
		return true;
	}
}
