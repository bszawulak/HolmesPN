package abyss.clusters;

import java.util.ArrayList;

/**
 * Klasa kontener do przechowywania wszystkich struktur danych opisuj¹cych konkretny przypadek
 * klastrowania. Zawiera te¿ pe³ne dane pliku CSV, mo¿liwe jest wiêc z ka¿dego jej obiektu
 * odtworzenie danych o inwariantach.
 * W klasie zdefiniowane s¹ równie¿ metody obróbki danych. Zosta³y przetestowane, a autor modli siê
 * ¿arliwie, aby okaza³y siê prawid³owo napisane. W innym wypadku znajdywanie b³êdu mo¿e siê okazaæ
 * 'interesuj¹cymi czasami' z chiñskiego przekleñstwa.
 * @author MR
 *
 */
public class ClusteringExtended {
	public Clustering metaData; //meta dane o klastrowaniu
	/**
	 * Wektor przechowuj¹cy nazwy tranzycji sieci, indeks to numer porz¹dkowy, indeks=0 - puste miejsce
	 */
	public String[] transNames; //nazwy tranzycji
	/**
	 * Tablica inwariantów, przepisana z pliku CSV. Pierwsza kolumna w ka¿dym wierszu to nr porz¹dkowy
	 * inwariantu, nastêpnie pola s¹ równe 0 lub wiêcej w zale¿noœci, które tranzycje s¹ wsparciem
	 * inwariantu. Nr indeksu kolumny w tej macierzy to dok³adnie nr indeksu nazwy tranzycji w transNames.
	 */
	//public int[][] csvInvariants; //tablica inwariantów z pliku CSV
	public ArrayList<ArrayList<Integer>> csvInvariants; //tablica inwariantów z pliku CSV
	/**
	 * Nr wpisany w macierzy mctSets to indeks nazwy tranzycji w wektorze String[] transNames. Pierwsza
	 * wartoœæ indeksu mctSets to oczywiœcie nr zbioru MCT (wiersze)
	 * W transNames indeks to numer porz¹dkowy (czyli tranzycja nr 1 ma tam indeks równy 1, a nie 0).
	 */
	public ArrayList<ArrayList<Integer>> mctSets;
	
	/**
	 * Macierz klastrów, ka¿da linia to klaster zawieraj¹cy id inwariantów z wektora csvInvariants[id][].
	 */
	public ArrayList<ArrayList<Integer>> clustersInv;
	
	//public int[][] transInClusters; // ile tranzycji (na bazie binarnej licznoœci inwariantów)
	//public int[][] transInClustersReal; //ile naprawdê tranzycji w klastrze
	
	//public int MCTinClusters[][];
	//public int transInClustersNoMCT[][];
	//public int transInClustersNoMCTReal[][];
	
	public ClusteringExtended() {
		
	}
	
	/**
	 * Metoda ta zwraca wektor o licznoœci zbiorów MCT, ka¿da liczba oznacza ile razy MCT 
	 * wyst¹pi³ w 'sumie' inwariantów klastra
	 * @param clusterIndex int - numer klastra
	 * @return ArrayList<Integer> - ile razy MCT wystêpuje w klastrze
	 */
	public ArrayList<Integer> getMCTFrequencyInCluster(int clusterIndex) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for(int mctInd=0; mctInd < mctSets.size(); mctInd++) {
			ArrayList<Integer> mctRow = mctSets.get(mctInd); //numery tranzycji MCT
			//teraz, dla ka¿dego inwariantu klastra nale¿y sprawdziæ, czy mctRow choæ
			//raz siê w nim znajduje
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
	 * Metoda zwraca wektor o licznoœci tranzycji. Pozycja oznacza nr tranzycji wg schematu z 
	 * wektora transNames, liczba oznacza ile razy dana tranzycja wystêpuje w klastrze, z wy³¹czeniem
	 * zbiorów MCT. 
	 * @param clusterIndex int - nr klastra
	 * @return int[4][] - pierwszy wektor to liczba wystêpuj¹cych w klastrze tranzycji które nie
	 * wchodz¹ w sk³ad zbiorów MCT dla tego klastra, drugi wektor podobnie, z tym ¿e wartoœci w nim
	 * to rzeczywiste liczby uruchomieñ tranzycji w ramach inwariantu (i dalej: w ramach wszystkich
	 * inwariantów badanego klastra).
	 * Trzeci wektor to liczba rzeczywistych odpaleñ w klastrze tych tranzycji, które wchodz¹ w sk³ad
	 * zbiorów MCT, wektor czwarty zawiera numery zbiorów MCT dla tranzycji z wektora trzeciego.
	 */
	public int[][] getTransitionFrequencyNoMCT(int clusterIndex, ArrayList<Integer> getMCTFrequencyInCluster) {
		int[] transFrequency = new int[transNames.length]; //binarne wystêpowanie
		int[] realFrequency = new int[transNames.length]; //rzeczywista liczba tranzycji w klastrze
		
		//kolumny: nr inwariantów klastra, wiersze - nr tranzycji tych klastrów
		ArrayList<ArrayList<Integer>> invTrans = new ArrayList<ArrayList<Integer>>();
		
		for(int invInd=0; invInd < clustersInv.get(clusterIndex).size(); invInd++) { //dla wszystkich inw.
			int invIndex = clustersInv.get(clusterIndex).get(invInd); //wewnêtrzny indeks inwariantu w csvInvariants
			ArrayList<Integer> transRow = new ArrayList<Integer>();
			//transRow.add(-1);
			for(int i=1; i<csvInvariants.get(invIndex).size(); i++) { //dla ka¿dej tranzycji
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
			//teraz mamy macierz wystêpowania (binarnego) tranzycji w klastrze: suma ka¿dej
			//kolumny to iloœæ wyst¹pieñ tranzycji w inwariantach klastra ³¹cznie
		}
		
		//poni¿szy wektor zawiera liczbê odpaleñ tranzycji ze zbiorów MCT z klastrze (binarnie)
		int[] mctTransitions = new int[transNames.length]; 
		//poni¿szy wektor zawiera nr zbioru MCT na odpowiedniej pozycji definiuj¹cej odp. tranzycjê
		int[] mctTransNumber = new int[transNames.length];
		
		for(int mctNumber=0; mctNumber<getMCTFrequencyInCluster.size(); mctNumber++) {
			if(getMCTFrequencyInCluster.get(mctNumber) > 0) { //tylko dla faktycznie wystêpuj¹cych MCT
				//teraz dla ka¿dego wystêpuj¹cego w klastrze MCT przetwarzamy go
				//na jego tranzycje sk³adowe
				ArrayList<Integer> mctRow = mctSets.get(mctNumber);
				for(int j=0; j<mctRow.size(); j++) { //dla wszystkich tranzycji tego MCT
					int trans = mctRow.get(j); 
					mctTransitions[trans] += getMCTFrequencyInCluster.get(mctNumber); 
					mctTransNumber[trans] = (mctNumber+1);
				}
			}
		}
		//teraz wektor mctTransitions zawiera licznoœæ wszystkich tranzycji ze zbiorów MCT
		int[] realMCTTransFirings = new int[transNames.length];
		for(int i=1; i<transFrequency.length; i++) {
			transFrequency[i] -= mctTransitions[i];
			if(mctTransitions[i] > 0) {
				realMCTTransFirings[i] = realFrequency[i]; //mamy info o tranzycjach z MCT
				realFrequency[i] = 0;
			}
		}
		
		int[][] result = { transFrequency, realFrequency, realMCTTransFirings, mctTransNumber};
		return result;
	}
	
	public ArrayList<String> getNormalizedInvariant(int invNumber) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Integer> invRow = new ArrayList<Integer>( csvInvariants.get(invNumber) ); 
		//z wyj¹tkiem I miejsca
		
		result.add("Inv. #"+invRow.get(0));
		String mctCell = "[";

		boolean alreadyCried = false;
		
		//dla ka¿dego MCT sprawdŸ, czy wchodzi w sk³ad inwariantu
		for(int mct=0; mct < mctSets.size(); mct++) { //dla ka¿dego zbioru MCT
			ArrayList<Integer> mctVector = mctSets.get(mct);
			int mctSize = mctVector.size();
			int mctPartsFound = 0;
			for(int tr=0; tr < mctVector.size(); tr++) { //dla ka¿dej tranzycji MCT
				if(invRow.get(mctVector.get(tr)) > 0) { //jeœli w inw. wystêpuje tranzycja o numerze w MCT
					mctPartsFound++;
				}
			}
			
			if(mctPartsFound == mctSize) {//dany MCT wystêpuje w inwariancie
				mctCell += (mct+1)+",";
				//usuwanie œladów po MCT w inwariancie:
				for(int tr=0; tr < mctVector.size(); tr++) { //powtórka z poprzednich iteracji
					int transToRemove = mctVector.get(tr);
					int oldValue = invRow.get(transToRemove);
					if(oldValue > 1) {
						//Hmm, dziwne. A przynajmniej odkryliœmy coœ, co zainteresuje Adama.
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
		
		for(int i=1; i<invRow.size(); i++) { // po wszystkich pozosta³ych tranzycjach
			if(invRow.get(i) > 0) {
				String transName = transNames[i];
				int firing = invRow.get(i);
				result.add(transName+":"+firing);
			}
		}
		return result;
	}
	
	/**
	 * Metoda ta sprawdza, czy elementy zbioru subset (nr tranzycji) znajduj¹ siê na odpowiednich
	 * miejsach w zbiorze superset (tj. nr tranzycji z subset to indeks z superset i > 0 w tym miejscu)
	 * @param subset ArrayList<Integer> - numery tranzycji w ramach MCT
	 * @param superset ArrayList<Integer> - inwariant
	 * @return boolean - true, jeœli zbiór MCT wchodzi w sk³ad inwariantu
	 */
	private boolean isSubset(ArrayList<Integer> subset, ArrayList<Integer> superset) {
		boolean transFound = false;
		//int supersetSize = superset.size();
		for(int i=0; i<subset.size(); i++) { //dla ka¿dej tranzycji z MCT
			transFound = false;
			int transId = subset.get(i);
			for(int j=1; j<superset.size(); j++) { //dla ka¿dej tranzycji z inwariantu
				//przy czym I element to nr inwariantu, wiêc pomijamy
				try {
					if(superset.get(transId)>0) {
						transFound = true;
						break;
					}
				} catch (Exception e) {
					return false; //to znaczy, ¿e jakas tranzycja ma wy¿szy nr ni¿ mo¿liwe.
					// To znaczy dalej, ¿e mamy przesrane, bo gdzieœ miêdzy setkami linii kodu
					// operuj¹cymi na setkach tysiêcy liczb jest b³¹d. Zapewne doœæ niewielki...
				}
			}
			if(transFound == false) { //jeœli powy¿sza pêtla nie znalaz³a tranzycji z MCT w inwariancie
				return false;
			}
		}
		return true;
	}
}
