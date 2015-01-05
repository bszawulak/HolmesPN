package abyss.clusters;

import java.awt.Color;
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
	 * Pobrany z pliku csv, tak wiêc od pozycji nr 1 do ostatniej zawieraæ powinien tranzycje w takiej
	 * kolejnoœci jak wystêpuj¹ w csv, czyli jak wystêpujê w zbiorze tranzycji obiektu PetriNet programu.
	 * Wszystko szlag trafi, jeœli bêdzie inaczej :) czyli jeœli u¿yto nieprawid³owego pliku csv
	 * do aktualnie analizowanej sieci...
	 */
	public String[] transNames; //nazwy tranzycji, 
	/**
	 * Tablica inwariantów, przepisana z pliku CSV. Pierwsza kolumna w ka¿dym wierszu to nr porz¹dkowy
	 * inwariantu, nastêpnie pola s¹ równe 0 lub wiêcej w zale¿noœci, które tranzycje s¹ wsparciem
	 * inwariantu. Nr indeksu kolumny w tej macierzy to dok³adnie nr indeksu nazwy tranzycji w transNames.
	 */
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
	
	/**
	 * Konstruktor domyœlny obiektu klasy ClusteringExtended.
	 */
	public ClusteringExtended() {
		metaData = null;
		transNames = new String[1];
		csvInvariants = new ArrayList<ArrayList<Integer>>();
		mctSets = new ArrayList<ArrayList<Integer>>();
		clustersInv = new ArrayList<ArrayList<Integer>>();
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
	
	/**
	 * Metoda ta zwraca tablicê ³añcuchów znaków. Pierwszym elementem jest numer inwariantu
	 * wraz z fraz¹ Inv. #, drugi element to wszystkie zbiory MCT wchodz¹ce w sk³ad inwariantu
	 * w formie [ ... ], nastêpnie ka¿dy element to nazwa tranzycji inwariantu poza MCT.
	 * @param invNumber int - nr inwariantu
	 * @return ArrayList[String] - nazwa-opis inwariantu
	 */
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
	 * Metoda pod wezwaniem ArrayListy, zwraca macierz klastrów/tranzycji, z kolorami 
	 * dla ka¿dej tranzycji.
	 * @return ArrayList[ArrayList[Color]] - macierz danych dla tranzycji w klastrach
	 */
	public ArrayList<ArrayList<ClusterTransition>> getClusteringColored() {
		ArrayList<ArrayList<ClusterTransition>> coloredClustering = new ArrayList<ArrayList<ClusterTransition>>();
		if(clustersInv.size() > 0) {
			for(int c=0; c<clustersInv.size(); c++) {
				ArrayList<ClusterTransition> dataRow = new ArrayList<ClusterTransition>();
				ArrayList<Integer> cTrans = getTransitionFromCluster(c, false);
				ArrayList<Integer> cFired = getTransitionFromCluster(c, true);
				
				ArrayList<Color> colorRowTransGrade = getTransitionColorsType1(c, false, cTrans);
				ArrayList<Color> colorRowTransScale = getTransitionColorsType1(c, true, cTrans);
				ArrayList<Color> colorRowFiredGrade = getTransitionColorsType1(c, false, cFired);
				ArrayList<Color> colorRowFiredScale = getTransitionColorsType1(c, true, cFired);
				
				for(int trans=0; trans<colorRowTransGrade.size(); trans++) {
					ClusterTransition atomicData = new ClusterTransition(
							colorRowTransGrade.get(trans), colorRowTransScale.get(trans),
							colorRowFiredGrade.get(trans), colorRowFiredScale.get(trans),
							cTrans.get(trans), cFired.get(trans));
					dataRow.add(atomicData);
				}
				coloredClustering.add(dataRow);
			}
		}
		return coloredClustering;
	}
	
	/**
	 * Zwraca wektor kolorów tranzycji dla wybranego klastra, dzia³a w trybie binarnym
	 * @param clusterNumber int - nr klastra
	 * @param scale boolean - true jeœli chcemy skalê od zielonego do czerwonego, false
	 * 		jeœli maj¹ byæ wartoœci krokowe kolorów
	 * @param data ArrayList[Integer] - wektor liczby tranzycji w klastrze lub ich odpaleñ
	 * @return ArrayList[Color] - wektor kolorów tranzycji
	 */
	private ArrayList<Color> getTransitionColorsType1(int clusterNumber, boolean scale,
			ArrayList<Integer> data) {
		ArrayList<Integer> clusterTransitions = data;
		//policz maks
		int max=0;
		for(int i=0; i<clusterTransitions.size(); i++) {
			if(clusterTransitions.get(i) > max)
				max = clusterTransitions.get(i);
		}
		//wzglêdem tego kolory
		if(scale)
			return getColorScale(clusterTransitions, max);
		else
			return getColorsForTransitions(clusterTransitions, max);
	}
	
	/**
	 * Metoda zwraca wektor kolorów dla ka¿dej tranzycji w zale¿noœci od jej 'mocy'
	 * w klastrze
	 * @param clusterTransitions ArrayList[Integer] - wektor tranzycji
	 * @param value int - wartoœæ referencyjna
	 * @return ArrayList[Colors] - wektor kolorów dla tranzycji
	 */
	private ArrayList<Color> getColorsForTransitions(ArrayList<Integer> clusterTransitions, int value) {
		ArrayList<Color> colors = new ArrayList<Color>();
		double max = value;
		double step = max / 10;
		
		for(int i=0; i<clusterTransitions.size(); i++) {
			double power = clusterTransitions.get(i);
			if(power >= 9*step) {
				colors.add(new Color(25, 105, 0)); //dark green
			} else if(power >= 8*step) {
				colors.add(new Color(55, 235, 0)); // light green
			} else if(power >= 7*step) {
				colors.add(new Color(145, 255, 0)); //green-yellow
			} else if(power >= 6*step) {
				colors.add(new Color(239, 255, 0)); //yellow
			} else if(power >= 5*step) {
				colors.add(new Color(255, 205, 0)); //gold
			} else if(power >= 4*step) {
				colors.add(new Color(255, 162, 0)); //orange
			} else if(power >= 3*step) {
				colors.add(new Color(255, 94, 0)); //darker orange
			} else if(power == 0) {
				colors.add(Color.white); //null
			} else if(power < 3*step) {
				colors.add(new Color(255, 0, 0)); //red
			}
		}
		return colors;
	}
	
	public ArrayList<Color> getColorScale(ArrayList<Integer> clusterTransitions, int value)
	{
		double max = value;
		double blue = 0.0;
		double green = 0.0;
		double red = 0.0;
		ArrayList<Color> colors = new ArrayList<Color>();
		for(int i=0; i<clusterTransitions.size(); i++) {
			double trValue = clusterTransitions.get(i);
			
			if(trValue == 0) {
				colors.add(Color.white);
				continue;
			}
			
			double power = trValue / max; //od 0 do 1.
			power = 1 - power;
			
			if(power >= 0 && power < 0.5) {
				green = 1.0;
				red = 2 * power;
			} else {
				red = 1.0;
				green = 1.0 - 2 * (power - 0.5);
			}
			red *= 255;
			blue *= 255;
			green *= 255;   
			
			colors.add(new Color((int)red, (int)green, (int)blue));
		}
		
	    //double H = power * 0.4; // Hue (note 0.4 = Green, see huge chart below)
	   // double S = 0.9; // Saturation
	    //double B = 0.9; // Brightness

	    //return Color.getHSBColor((float)H, (float)S, (float)B);
	    
	    return colors;
	}

	/**
	 * Metoda zwraca wektor o licznoœci tranzycji w sieci. W zale¿noœci od flagi real wektor
	 * ten zawiera sumê wszystkich odpaleñ tranzycji w klastrze, lub tylko liczbê ich wyst¹pieñ
	 * w ramach inwariantów.
	 * @param clusterNumber int - nr klastra
	 * @param real boolean - false, jeœli liczymy tylko wyst¹pienia tranzycji w inwariantach 
	 * 		klastra, true - jeœli sumaryczn¹ wartoœæ odpaleñ
	 * @return ArrayList[Integer] - wektor wartoœci dla tranzycji
	 */
	private ArrayList<Integer> getTransitionFromCluster(int clusterNumber, boolean real) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for(int inv=0; inv<clustersInv.get(clusterNumber).size(); inv++) {
			int invID = clustersInv.get(clusterNumber).get(inv); //prawdziwy nr inwariantu
			ArrayList<Integer> transVector = csvInvariants.get(invID); //pobierz wektor tranzycji
			
			if(inv==0) {
				for(int tr=1; tr<transVector.size(); tr++) {//1 element to nr porz¹dkowy - ignore
					int value = transVector.get(tr);
					if(real) { //jeœli dodajemy rzeczywiste odpalenia
						result.add(value); //dodaj wartoœci odpaleñ
					} else { //jeœli tylko odnotowujemy czy wsparcie jest czy go nie ma
						if(value>0)
							result.add(1);
						else
							result.add(0);
					}
				}
			} else { //jeœli wartoœci wektor wynikowego ju¿ istniej¹
				for(int tr=1; tr<transVector.size(); tr++) {//1 element to nr porz¹dkowy - ignore
					int value = transVector.get(tr);
					if(real) { //jeœli dodajemy rzeczywiste odpalenia
						int oldValue = result.get(tr-1);
						oldValue += value;
						result.set(tr-1, oldValue); //dodaj wartoœci odpaleñ
					} else { //jeœli tylko odnotowujemy czy wsparcie jest czy go nie ma
						if(value>0) {
							int oldValue = result.get(tr-1);
							result.set(tr-1, oldValue+1); //odnotuj wsparcie
						}
					}
				}
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
