package holmes.clusters;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Klasa kontener do przechowywania wszystkich struktur danych opisujących konkretny przypadek
 * klastrowania. Zawiera też pełne dane pliku CSV, możliwe jest więc z każdego jej obiektu
 * odtworzenie danych o inwariantach.
 * W klasie zdefiniowane są również metody obróbki danych. Zostały przetestowane, a autor modli się
 * żarliwie, aby okazały się prawidłowo napisane. W innym wypadku znajdywanie błędu może się okazać
 * 'interesujścymi czasami' z chińskiego przekleństwa.
 */
public class ClusteringExtended {
	public Clustering metaData; //meta dane o klastrowaniu
	/**
	 * Wektor przechowujący nazwy tranzycji sieci, indeks to numer porządkowy, indeks=0 - puste miejsce
	 * Pobrany z pliku csv, tak więc od pozycji nr 1 do ostatniej zawierać powinien tranzycje w takiej
	 * kolejności jak występują w csv, czyli jak występują w zbiorze tranzycji obiektu PetriNet programu.
	 * Wszystko szlag trafi, jeśli będzie inaczej :) czyli jeśli użyto nieprawidłowego pliku csv
	 * do aktualnie analizowanej sieci...
	 */
	public String[] transNames; //nazwy tranzycji, 
	/**
	 * Tablica inwariantów, przepisana z pliku CSV. Pierwsza kolumna w każdym wierszu to nr porządkowy
	 * inwariantu, następnie pola są równe 0 lub więcej w zależności, które tranzycje są wsparciem
	 * inwariantu. Nr indeksu kolumny w tej macierzy to dokładnie nr indeksu nazwy tranzycji w transNames.
	 */
	public ArrayList<ArrayList<Integer>> csvInvariants; //tablica inwariantów z pliku CSV
	/**
	 * Nr wpisany w macierzy mctSets to indeks nazwy tranzycji w wektorze String[] transNames. Pierwsza
	 * wartość indeksu mctSets to oczywiście nr zbioru MCT (wiersze)
	 * W transNames indeks to numer porządkowy (czyli tranzycja nr 1 ma tam indeks równy 1, a nie 0).
	 */
	public ArrayList<ArrayList<Integer>> mctSets;
	
	/**
	 * Macierz klastrów, każda linia to klaster zawierający id inwariantów z wektora csvInvariants[id][].
	 */
	public ArrayList<ArrayList<Integer>> clustersInv;
	
	/**
	 * Konstruktor domyślny obiektu klasy ClusteringExtended.
	 */
	public ClusteringExtended() {
		metaData = null;
		transNames = new String[1];
		csvInvariants = new ArrayList<ArrayList<Integer>>();
		mctSets = new ArrayList<ArrayList<Integer>>();
		clustersInv = new ArrayList<ArrayList<Integer>>();
	}

	/**
	 * Metoda ta zwraca wektor o liczności zbiorów MCT, każda liczba oznacza ile razy MCT 
	 * wystąpią w 'sumie' inwariantów klastra
	 * @param clusterIndex (<b>int</b>) - numer klastra
	 * @return ArrayList[<b>Integer</b>] - ile razy MCT występuje w klastrze
	 */
	public ArrayList<Integer> getMCTFrequencyInCluster(int clusterIndex) {
		ArrayList<Integer> result = new ArrayList<Integer>();

        //numery tranzycji MCT
        for (ArrayList<Integer> mctRow : mctSets) {
            //teraz, dla każdego inwariantu klastra należy sprawdzić, czy mctRow choć
            //raz się w nim znajduje
            int mctCounterInCluster = 0;

            ArrayList<Integer> invRow = clustersInv.get(clusterIndex); //numery tranzycji inwariantu
            //int rowSize = invRow.size();
            for (int invIndex : invRow) {
                ArrayList<Integer> transRow = csvInvariants.get(invIndex);

                boolean isMCTinInv = isSubset(mctRow, transRow);
                if (isMCTinInv) {
                    mctCounterInCluster++;
                }
            }
            result.add(mctCounterInCluster);
        }
		return result;
	}
	
	/**
	 * Metoda zwraca wektor o liczności tranzycji. Pozycja oznacza nr tranzycji wg schematu z 
	 * wektora transNames, liczba oznacza ile razy dana tranzycja występuje w klastrze, z wyłączeniem
	 * zbiorów MCT. 
	 * @param clusterIndex (<b>int</b>) - nr klastra
	 * @return int[4][] - pierwszy wektor to liczba występujących w klastrze tranzycji które nie
	 * wchodzą w skład zbiorów MCT dla tego klastra, drugi wektor podobnie, z tym że wartości w nim
	 * to rzeczywiste liczby uruchomień tranzycji w ramach inwariantu (i dalej: w ramach wszystkich
	 * inwariantów badanego klastra).
	 * Trzeci wektor to liczba rzeczywistych odpaleń w klastrze tych tranzycji, które wchodzą w skład
	 * zbiorów MCT, wektor czwarty zawiera numery zbiorów MCT dla tranzycji z wektora trzeciego.
	 */
	public int[][] getTransitionFrequencyNoMCT(int clusterIndex, ArrayList<Integer> getMCTFrequencyInCluster) {
		int[] transFrequency = new int[transNames.length]; //binarne wystąpowanie
		int[] realFrequency = new int[transNames.length]; //rzeczywista liczba tranzycji w klastrze
		
		//kolumny: nr inwariantów klastra, wiersze - nr tranzycji tych klastrów
		ArrayList<ArrayList<Integer>> invTrans = new ArrayList<ArrayList<Integer>>();
		
		for(int invInd=0; invInd < clustersInv.get(clusterIndex).size(); invInd++) { //dla wszystkich inw.
			int invIndex = clustersInv.get(clusterIndex).get(invInd); //wewnętrzny indeks inwariantu w csvInvariants
			ArrayList<Integer> transRow = new ArrayList<Integer>();
			//transRow.add(-1);
			for(int i=1; i<csvInvariants.get(invIndex).size(); i++) { //dla każdej tranzycji
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
			//teraz mamy macierz występowania (binarnego) tranzycji w klastrze: suma każdej
			//kolumny to ilość wystąpień tranzycji w inwariantach klastra łącznie
		}
		
		//poniższy wektor zawiera liczbę odpaleń tranzycji ze zbiorów MCT z klastrze (binarnie)
		int[] mctTransitions = new int[transNames.length]; 
		//poniższy wektor zawiera nr zbioru MCT na odpowiedniej pozycji definiującej odp. tranzycję
		int[] mctTransNumber = new int[transNames.length];
		
		for(int mctNumber=0; mctNumber<getMCTFrequencyInCluster.size(); mctNumber++) {
			if(getMCTFrequencyInCluster.get(mctNumber) > 0) { //tylko dla faktycznie występujących MCT
				//teraz dla każdego występującego w klastrze MCT przetwarzamy go
				//na jego tranzycje składowe
				ArrayList<Integer> mctRow = mctSets.get(mctNumber);
                for (int trans : mctRow) { //dla wszystkich tranzycji tego MCT
                    mctTransitions[trans] += getMCTFrequencyInCluster.get(mctNumber);
                    mctTransNumber[trans] = (mctNumber + 1);
                }
			}
		}
		//teraz wektor mctTransitions zawiera liczność wszystkich tranzycji ze zbiorów MCT
		int[] realMCTTransFirings = new int[transNames.length];
		for(int i=1; i<transFrequency.length; i++) {
			transFrequency[i] -= mctTransitions[i];
			if(mctTransitions[i] > 0) {
				realMCTTransFirings[i] = realFrequency[i]; //mamy info o tranzycjach z MCT
				realFrequency[i] = 0;
			}
		}
        return new int[][]{ transFrequency, realFrequency, realMCTTransFirings, mctTransNumber};
	}
	
	/**
	 * Metoda ta zwraca tablicę łańcuchów znaków. Pierwszym elementem jest numer inwariantu
	 * wraz z frazą Inv. #, drugi element to wszystkie zbiory MCT wchodzące w skład inwariantu
	 * w formie [ ... ], następnie każdy element to nazwa tranzycji inwariantu poza MCT.
	 * @param invNumber (<b>int</b>) - nr inwariantu
	 * @param transIndexOnly (<b>boolean</b>) - jeśli true, zwraca tylko ID tranzycji
	 * @return ArrayList[<b>String</b>] - nazwa-opis inwariantu
	 */
	public ArrayList<String> getNormalizedInvariant(int invNumber, boolean transIndexOnly) {
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<Integer> invRow = new ArrayList<Integer>( csvInvariants.get(invNumber) ); 
		//z wyjątkiem I miejsca
		
		result.add("Inv. #"+invRow.get(0));
		StringBuilder mctCell = new StringBuilder("[");

		boolean alreadyCried = false;
		
		//dla każdego MCT sprawdź, czy wchodzi w skład inwariantu
		for(int mct=0; mct < mctSets.size(); mct++) { //dla każdego zbioru MCT
			ArrayList<Integer> mctVector = mctSets.get(mct);
			int mctSize = mctVector.size();
			int mctPartsFound = 0;
            for (Integer integer : mctVector) { //dla każdej tranzycji MCT
                if (invRow.get(integer) > 0) { //jeśli w inw. występuje tranzycja o numerze w MCT
                    mctPartsFound++;
                }
            }
			
			if(mctPartsFound == mctSize) {//dany MCT występuje w inwariancie
				mctCell.append(mct + 1).append(",");
				//usuwanie śladów po MCT w inwariancie:
                for (int transToRemove : mctVector) { //powtórka z poprzednich iteracji
                    int oldValue = invRow.get(transToRemove);
                    if (oldValue > 1) {
                        //Hmm, dziwne. A przynajmniej odkryliśmy coś, co zainteresuje Adama.
                        if (!alreadyCried)
                            alreadyCried = true;
                        //GUIManager.getDefaultGUIManager().log("Logical error MCT:invariant:transition in inv "+invRow.get(0), "warning",true);

                    }
                    invRow.set(transToRemove, 0);
                }
			}
		}
		mctCell.append("]");
		mctCell = new StringBuilder(mctCell.toString().replace(",]", "]"));
		result.add(mctCell.toString());
		
		for(int i=1; i<invRow.size(); i++) { // po wszystkich pozostałych tranzycjach
			if(invRow.get(i) > 0) {
				String transName = transNames[i];
				int firing = invRow.get(i);
				
				if(transIndexOnly)
					result.add((i-1)+"");
				else
					result.add(transName+":"+firing);
			}
		}
		return result;
	}
	
	/**
	 * Metoda pod wezwaniem ArrayListy, zwraca macierz klastrów/tranzycji, z kolorami 
	 * dla każdej tranzycji.
	 * @return ArrayList[ArrayList[<b>Color</b>]] - macierz danych dla tranzycji w klastrach
	 */
	public ArrayList<ArrayList<ClusterTransition>> getClusteringColored() {
		ArrayList<ArrayList<ClusterTransition>> coloredClustering = new ArrayList<ArrayList<ClusterTransition>>();
		if(!clustersInv.isEmpty()) {
			for(int c=0; c<clustersInv.size(); c++) {
				ArrayList<ClusterTransition> dataRow = new ArrayList<ClusterTransition>();
				ArrayList<Integer> cTrans = getTransitionFromCluster(c, false);
				ArrayList<Integer> cFired = getTransitionFromCluster(c, true);
				
				ArrayList<Double> avgFire = new ArrayList<Double>();
				double clSize = clustersInv.get(c).size();
                for (Integer integer : cFired) {
                    avgFire.add(integer.doubleValue() / clSize);
                }
				
				ArrayList<Double> tmpCTrans = new ArrayList<Double>();
                for (Integer cTran : cTrans) {
                    tmpCTrans.add(cTran.doubleValue());
                }
				
				ArrayList<Color> colorRowTransGrade = getTransitionColorsType1(c, false, tmpCTrans);  //cTrans
				ArrayList<Color> colorRowTransScale = getTransitionColorsType1(c, true, tmpCTrans); //cTrans
				ArrayList<Color> colorRowFiredGrade = getTransitionColorsType1(c, false, avgFire); //cFired
				ArrayList<Color> colorRowFiredScale = getTransitionColorsType1(c, true, avgFire); //cFired
				
				for(int trans=0; trans<colorRowTransGrade.size(); trans++) {
					ClusterTransition atomicData = new ClusterTransition(
							colorRowTransGrade.get(trans), colorRowTransScale.get(trans),
							colorRowFiredGrade.get(trans), colorRowFiredScale.get(trans),
							cTrans.get(trans), avgFire.get(trans)); //last one: cFired.get(trans)
					dataRow.add(atomicData);
				}
				coloredClustering.add(dataRow);
			}
		}
		return coloredClustering;
	}
	
	/**
	 * Zwraca wektor kolorów tranzycji dla wybranego klastra, działa w trybie binarnym
	 * @param clusterNumber (<b>int</b>) - nr klastra
	 * @param scale (<b>boolean</b>) - true jeśli chcemy skalę od zielonego do czerwonego, false
	 * 		jeśli maja być wartości krokowe kolorów
	 * @param data (ArrayList[<b>Integer</b>]) - wektor liczby tranzycji w klastrze lub ich odpaleń
	 * @return ArrayList[<b>Color</b>] - wektor kolorów tranzycji
	 */
	private ArrayList<Color> getTransitionColorsType1(int clusterNumber, boolean scale, ArrayList<Double> data) {
		double max=0;
        for (Double datum : data) {
            if (datum > max)
                max = datum;
        }
		//względem tego kolory
		if(scale)
			return getColorScale(data, max);
		else
			return getColorsForTransitions(data, max);
	}
	
	/**
	 * Metoda zwraca wektor kolorów dla każdej tranzycji w zależności od jej 'mocy'
	 * w klastrze
	 * @param clusterTransitions (ArrayList[<b>Double</b>]) - wektor tranzycji
	 * @param value (<b>double</b>) - wartość referencyjna
	 * @return ArrayList[<b>Colors</b>] - wektor kolorów dla tranzycji
	 */
	private ArrayList<Color> getColorsForTransitions(ArrayList<Double> clusterTransitions, double value) {
		ArrayList<Color> colors = new ArrayList<Color>();
		double step = value / 10;

        for (double power : clusterTransitions) {
            if (power >= 9 * step) {
                colors.add(new Color(25, 105, 0)); //dark green
            } else if (power >= 8 * step) {
                colors.add(new Color(55, 235, 0)); // light green
            } else if (power >= 7 * step) {
                colors.add(new Color(145, 255, 0)); //green-yellow
            } else if (power >= 6 * step) {
                colors.add(new Color(239, 255, 0)); //yellow
            } else if (power >= 5 * step) {
                colors.add(new Color(255, 205, 0)); //gold
            } else if (power >= 4 * step) {
                colors.add(new Color(255, 162, 0)); //orange
            } else if (power >= 3 * step) {
                colors.add(new Color(255, 94, 0)); //darker orange
            } else if (power == 0) {
                colors.add(Color.white); //null
            } else if (power < 3 * step) {
                colors.add(new Color(255, 0, 0)); //red
            }
        }
		return colors;
	}
	
	/**
	 * Metoda dla danego wektora tranzycji zwraca wektor kolorów w skali od zieleni do czerwonego.
	 * @param clusterTransitions (ArrayList[<b>Integer</b>]) - wektor tranzycji
	 * @param value (<b>double</b>) - wartość referencyjna
	 * @return ArrayList[<b>Color</b>] - wektor kolorów
	 */
	public ArrayList<Color> getColorScale(ArrayList<Double> clusterTransitions, double value) {
		double blue = 0.0;
		double green;
		double red;
		ArrayList<Color> colors = new ArrayList<Color>();
        for (double trValue : clusterTransitions) {
            if (trValue == 0) {
                colors.add(Color.white);
                continue;
            }

            double power = trValue / value; //od 0 do 1.
            power = 1 - power;

            if (power >= 0 && power < 0.5) {
                green = 1.0;
                red = 2 * power;
            } else {
                red = 1.0;
                green = 1.0 - 2 * (power - 0.5);
            }
            red *= 255;
            blue *= 255;
            green *= 255;

            colors.add(new Color((int) red, (int) green, (int) blue));
        }
		
	    //double H = power * 0.4; // Hue (note 0.4 = Green, see huge chart below)
	   // double S = 0.9; // Saturation
	    //double B = 0.9; // Brightness

	    //return Color.getHSBColor((float)H, (float)S, (float)B);
	    
	    return colors;
	}

	/**
	 * Metoda zwraca wektor o liczności tranzycji w sieci. W zależności od flagi real wektor
	 * ten zawiera sumę wszystkich odpaleń tranzycji w klastrze, lub tylko liczbę ich wystąpień
	 * w ramach inwariantów.
	 * @param clusterNumber (<b>int</b>) - nr klastra
	 * @param real (<b>boolean</b>) - false, jeśli liczymy tylko wystąpienia tranzycji w inwariantach
	 * 		klastra, true - jeśli sumaryczną wartość odpaleń
	 * @return ArrayList[<b>Integer</b>] - wektor wartości dla tranzycji
	 */
	private ArrayList<Integer> getTransitionFromCluster(int clusterNumber, boolean real) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		
		for(int inv=0; inv<clustersInv.get(clusterNumber).size(); inv++) {
			int invID = clustersInv.get(clusterNumber).get(inv); //prawdziwy nr inwariantu
			ArrayList<Integer> transVector = csvInvariants.get(invID); //pobierz wektor tranzycji
			
			if(inv==0) {
				for(int tr=1; tr<transVector.size(); tr++) {//1 element to nr porządkowy - ignore
					int value = transVector.get(tr);
					if(real) { //jeśli dodajemy rzeczywiste odpalenia
						result.add(value); //dodaj wartości odpaleń
					} else { //jeśli tylko odnotowujemy czy wsparcie jest czy go nie ma
						if(value>0)
							result.add(1);
						else
							result.add(0);
					}
				}
			} else { //jeśli wartości wektor wynikowego już istnieją
				for(int tr=1; tr<transVector.size(); tr++) {//1 element to nr porządkowy - ignore
					int value = transVector.get(tr);
					if(real) { //jeśli dodajemy rzeczywiste odpalenia
						int oldValue = result.get(tr-1);
						oldValue += value;
						result.set(tr-1, oldValue); //dodaj wartości odpaleń
					} else { //jeśli tylko odnotowujemy czy wsparcie jest czy go nie ma
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
	 * Metoda ta sprawdza, czy elementy zbioru subset (nr tranzycji) znajdują się na odpowiednich
	 * miejsach w zbiorze superset (tj. nr tranzycji z subset to indeks z superset i > 0 w tym miejscu)
	 * @param subset (ArrayList[<b>Integer</b>]) - numery tranzycji w ramach MCT
	 * @param superset (ArrayList[<b>Integer</b>]) - inwariant
	 * @return <b>boolean</b> - true, jeśli zbiór MCT wchodzi w skład inwariantu
	 */
	private boolean isSubset(ArrayList<Integer> subset, ArrayList<Integer> superset) {
		boolean transFound;
		//int supersetSize = superset.size();
        for (Integer integer : subset) { //dla każdej tranzycji z MCT
            transFound = false;
            int transId = integer;
            for (int j = 1; j < superset.size(); j++) { //dla każdej tranzycji z inwariantu
                //przy czym I element to nr inwariantu, więc pomijamy
                try {
                    if (superset.get(transId) > 0) {
                        transFound = true;
                        break;
                    }
                } catch (Exception e) {
                    return false; //to znaczy, że jakas tranzycja ma wyższy nr niż możliwe.
                    // To znaczy dalej, że mamy przesrane, bo gdzieś między setkami linii kodu
                    // operującymi na setkach tysięcy liczb jest błąd. Zapewne dość niewielki...
                }
            }
            if (!transFound) { //jeśli powyższa pętla nie znalazła tranzycji z MCT w inwariancie
                return false;
            }
        }
		return true;
	}
}
