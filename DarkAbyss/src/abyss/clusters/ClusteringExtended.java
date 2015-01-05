package abyss.clusters;

import java.awt.Color;
import java.util.ArrayList;

import abyss.math.ClusterTransition;

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
	 * Pobrany z pliku csv, tak wi�c od pozycji nr 1 do ostatniej zawiera� powinien tranzycje w takiej
	 * kolejno�ci jak wyst�puj� w csv, czyli jak wyst�puj� w zbiorze tranzycji obiektu PetriNet programu.
	 * Wszystko szlag trafi, je�li b�dzie inaczej :) czyli je�li u�yto nieprawid�owego pliku csv
	 * do aktualnie analizowanej sieci...
	 */
	public String[] transNames; //nazwy tranzycji, 
	/**
	 * Tablica inwariant�w, przepisana z pliku CSV. Pierwsza kolumna w ka�dym wierszu to nr porz�dkowy
	 * inwariantu, nast�pnie pola s� r�wne 0 lub wi�cej w zale�no�ci, kt�re tranzycje s� wsparciem
	 * inwariantu. Nr indeksu kolumny w tej macierzy to dok�adnie nr indeksu nazwy tranzycji w transNames.
	 */
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
	
	/**
	 * Konstruktor domy�lny obiektu klasy ClusteringExtended.
	 */
	public ClusteringExtended() {
		metaData = null;
		transNames = new String[1];
		csvInvariants = new ArrayList<ArrayList<Integer>>();
		mctSets = new ArrayList<ArrayList<Integer>>();
		clustersInv = new ArrayList<ArrayList<Integer>>();
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
	 * @return int[4][] - pierwszy wektor to liczba wyst�puj�cych w klastrze tranzycji kt�re nie
	 * wchodz� w sk�ad zbior�w MCT dla tego klastra, drugi wektor podobnie, z tym �e warto�ci w nim
	 * to rzeczywiste liczby uruchomie� tranzycji w ramach inwariantu (i dalej: w ramach wszystkich
	 * inwariant�w badanego klastra).
	 * Trzeci wektor to liczba rzeczywistych odpale� w klastrze tych tranzycji, kt�re wchodz� w sk�ad
	 * zbior�w MCT, wektor czwarty zawiera numery zbior�w MCT dla tranzycji z wektora trzeciego.
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
		
		//poni�szy wektor zawiera liczb� odpale� tranzycji ze zbior�w MCT z klastrze (binarnie)
		int[] mctTransitions = new int[transNames.length]; 
		//poni�szy wektor zawiera nr zbioru MCT na odpowiedniej pozycji definiuj�cej odp. tranzycj�
		int[] mctTransNumber = new int[transNames.length];
		
		for(int mctNumber=0; mctNumber<getMCTFrequencyInCluster.size(); mctNumber++) {
			if(getMCTFrequencyInCluster.get(mctNumber) > 0) { //tylko dla faktycznie wyst�puj�cych MCT
				//teraz dla ka�dego wyst�puj�cego w klastrze MCT przetwarzamy go
				//na jego tranzycje sk�adowe
				ArrayList<Integer> mctRow = mctSets.get(mctNumber);
				for(int j=0; j<mctRow.size(); j++) { //dla wszystkich tranzycji tego MCT
					int trans = mctRow.get(j); 
					mctTransitions[trans] += getMCTFrequencyInCluster.get(mctNumber); 
					mctTransNumber[trans] = (mctNumber+1);
				}
			}
		}
		//teraz wektor mctTransitions zawiera liczno�� wszystkich tranzycji ze zbior�w MCT
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
	 * Metoda ta zwraca tablic� �a�cuch�w znak�w. Pierwszym elementem jest numer inwariantu
	 * wraz z fraz� Inv. #, drugi element to wszystkie zbiory MCT wchodz�ce w sk�ad inwariantu
	 * w formie [ ... ], nast�pnie ka�dy element to nazwa tranzycji inwariantu poza MCT.
	 * @param invNumber int - nr inwariantu
	 * @return ArrayList[String] - nazwa-opis inwariantu
	 */
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
	 * Metoda pod wezwaniem ArrayListy, zwraca macierz klastr�w/tranzycji, z kolorami 
	 * dla ka�dej tranzycji.
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
	 * Zwraca wektor kolor�w tranzycji dla wybranego klastra, dzia�a w trybie binarnym
	 * @param clusterNumber int - nr klastra
	 * @param scale boolean - true je�li chcemy skal� od zielonego do czerwonego, false
	 * 		je�li maj� by� warto�ci krokowe kolor�w
	 * @param data ArrayList[Integer] - wektor liczby tranzycji w klastrze lub ich odpale�
	 * @return ArrayList[Color] - wektor kolor�w tranzycji
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
		//wzgl�dem tego kolory
		if(scale)
			return getColorScale(clusterTransitions, max);
		else
			return getColorsForTransitions(clusterTransitions, max);
	}
	
	/**
	 * Metoda zwraca wektor kolor�w dla ka�dej tranzycji w zale�no�ci od jej 'mocy'
	 * w klastrze
	 * @param clusterTransitions ArrayList[Integer] - wektor tranzycji
	 * @param value int - warto�� referencyjna
	 * @return ArrayList[Colors] - wektor kolor�w dla tranzycji
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
	 * Metoda zwraca wektor o liczno�ci tranzycji w sieci. W zale�no�ci od flagi real wektor
	 * ten zawiera sum� wszystkich odpale� tranzycji w klastrze, lub tylko liczb� ich wyst�pie�
	 * w ramach inwariant�w.
	 * @param clusterNumber int - nr klastra
	 * @param real boolean - false, je�li liczymy tylko wyst�pienia tranzycji w inwariantach 
	 * 		klastra, true - je�li sumaryczn� warto�� odpale�
	 * @return ArrayList[Integer] - wektor warto�ci dla tranzycji
	 */
	private ArrayList<Integer> getTransitionFromCluster(int clusterNumber, boolean real) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for(int inv=0; inv<clustersInv.get(clusterNumber).size(); inv++) {
			int invID = clustersInv.get(clusterNumber).get(inv); //prawdziwy nr inwariantu
			ArrayList<Integer> transVector = csvInvariants.get(invID); //pobierz wektor tranzycji
			
			if(inv==0) {
				for(int tr=1; tr<transVector.size(); tr++) {//1 element to nr porz�dkowy - ignore
					int value = transVector.get(tr);
					if(real) { //je�li dodajemy rzeczywiste odpalenia
						result.add(value); //dodaj warto�ci odpale�
					} else { //je�li tylko odnotowujemy czy wsparcie jest czy go nie ma
						if(value>0)
							result.add(1);
						else
							result.add(0);
					}
				}
			} else { //je�li warto�ci wektor wynikowego ju� istniej�
				for(int tr=1; tr<transVector.size(); tr++) {//1 element to nr porz�dkowy - ignore
					int value = transVector.get(tr);
					if(real) { //je�li dodajemy rzeczywiste odpalenia
						int oldValue = result.get(tr-1);
						oldValue += value;
						result.set(tr-1, oldValue); //dodaj warto�ci odpale�
					} else { //je�li tylko odnotowujemy czy wsparcie jest czy go nie ma
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
