package abyss.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * 10.06.2014: Metoda stara się liczyć inwarianty. Coś nie wyszło i to ostro...<br>
 * 11.03.2015: No więc czas ją naprawić. MR <br>
 * 13.03.2015: Naprawianie w toku. Tu się takie cuda działy, że głowa mała. Poprzednia wersja miała wymagania
 * pamięciowe serwerowni PCSS. Aż cud, że była w stanie cokolwiek liczyć. O błędach lepiej nie pisać. <br>
 * 14.03.2015: Pierwszy sukces, coś działa. Poprawienie macierzy incydencji (1 na -1) w każdym polu PEWNIE TEŻ POMOGŁO!!! 
 * 
 * @author BR
 * @author MR
 */
public class InvariantsAnalyzer implements Runnable {
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<ArrayList<Integer>> invariantsList = new ArrayList<ArrayList<Integer>>();

	private ArrayList<ArrayList<Integer>> globalIncidenceMatrix; //aktualna macierz przekształceń liniowych
	private ArrayList<ArrayList<Integer>> globalIdentityMatrix; //macierz przekształacna w inwarianty
	private ArrayList<ArrayList<Integer>> CMatrix; //oryginalna macierz incydencji sieci
	private ArrayList<Integer> removalList;
	
	private ArrayList<Integer> zeroColumnVectorT;
	private ArrayList<Integer> nonZeroColumnVectorT;
	private ArrayList<Integer> zeroColumnVectorP;
	private ArrayList<Integer> nonZeroColumnVectorP;
	
	//private int placesNumber = 0;
	//private int transNumber = 0;
	private int vectorSize = 0;
	private boolean transCalculation = true;
	
	private int newRejected = 0;
	private int oldReplaced = 0;
	private int notCanonical = 0;
	
	private ArrayList<ArrayList<Integer>> invINAMatrix;
	
	/**
	 * Konstruktor obiektu klasy InvariantsAnalyzer. Zapewnia dostęp do miejsc, tranzycji i łuków sieci.
	 * @param transCal boolean - true, jeśli liczymy T-inwarianty, false dla P-inwariantów
	 */
	public InvariantsAnalyzer(boolean transCal) {
		transCalculation = transCal;
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		
		if(transCalculation == true)
			vectorSize = transitions.size();
		else
			vectorSize = places.size(); 

		zeroColumnVectorT = new ArrayList<Integer>();
		nonZeroColumnVectorT = new ArrayList<Integer>();
		zeroColumnVectorP = new ArrayList<Integer>();
		nonZeroColumnVectorP = new ArrayList<Integer>();
		
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix() != null) {
			invINAMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		}
	}
	
	/**
	 * Metoda wirtualna - nadpisana, odpowiada za działanie w niezależnym wątku
	 */
	public void run() {
		this.createTPIncidenceAndIdentityMatrix();
		this.searchTInvariants();
		PetriNet project = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(project.getEIAgeneratedInv_old());
		project.setInvariantsMatrix(getInvariants());
	}
	
	private void log(String msg, String type, boolean clean) {
		if(clean) {
			GUIManager.getDefaultGUIManager().log(msg, type, false);
		} else {
			GUIManager.getDefaultGUIManager().log("InvModule: "+msg, type, true);
		}
	}

	/**
	 * Metoda tworząca macierze: incydencji i jednostkową dla modelu szukania T-inwariantów
	 * (TP-macierz z literatury)
	 */
	public void createTPIncidenceAndIdentityMatrix() {
		//hashmapy do ustalania lokalizacji miejsca/tranzycji. Równie dobrze 
		//działałoby (niżej, gdy są używane): np. places.indexOf(...)
		HashMap<Place, Integer> placesMap = new HashMap<Place, Integer>();
		HashMap<Transition, Integer> transitionsMap = new HashMap<Transition, Integer>();
		for (int i = 0; i < places.size(); i++) {
			placesMap.put(places.get(i), i);
		}
		for (int i = 0; i < transitions.size(); i++) {
			transitionsMap.put(transitions.get(i), i);
		}
		
		globalIncidenceMatrix = new ArrayList<ArrayList<Integer>>();
		globalIdentityMatrix = new ArrayList<ArrayList<Integer>>();
		CMatrix = new ArrayList<ArrayList<Integer>>();
		removalList = new ArrayList<Integer>();

		//tworzenie macierzy TP - precyzyjnie do obliczeń T-inwariantów
		for (int trans = 0; trans < transitions.size(); trans++) {
			ArrayList<Integer> transRow = new ArrayList<Integer>();
			ArrayList<Integer> transRow2 = new ArrayList<Integer>();
			for (int place = 0; place < places.size(); place++) {
				transRow.add(0);
				transRow2.add(0);
			}
			globalIncidenceMatrix.add(transRow);
			CMatrix.add(transRow2);
		}
		//wypełnianie macierzy incydencji
		for (Arc oneArc : arcs) {
			int tPosition = 0;
			int pPosition = 0;
			int incidenceValue = 0;

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION
					|| oneArc.getStartNode().getType() == PetriNetElementType.TIMETRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				// incidenceValue = -1 * oneArc.getWeight();  // CO TO K**** JEST ?! JAKIE -1 ?!
				// 	(14.03.2015) ja go chyba zamorduję... 
				// 	
				//   https://www.youtube.com/watch?v=oxiJrcFo724
				// 		
				incidenceValue = 1 * oneArc.getWeight();
			} else {
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				//incidenceValue = 1 * oneArc.getWeight(); ///JAK WYŻEJ...!!!!
				incidenceValue = -1 * oneArc.getWeight();
			}
			globalIncidenceMatrix.get(tPosition).set(pPosition, incidenceValue);
			CMatrix.get(tPosition).set(pPosition, incidenceValue);
		}
		log("TP-class incidence matrix created for "+transitions.size()+" transitions and "+places.size()+" places.","text", false);
		
		//macierz jednostkowa
		for (int trans = 0; trans < transitions.size(); trans++) {
			ArrayList<Integer> identRow = new ArrayList<Integer>();
			for (int trans2 = 0; trans2 < transitions.size(); trans2++) {
				if (trans == trans2) 
					identRow.add(1);
				else
					identRow.add(0);
			}
			globalIdentityMatrix.add(identRow);
		}
		
		log("Identity matrix created for "+transitions.size()+" transitions","text", false);
	}

	/**
	 * Główna metoda klasy odpowiedzialna za wyszukiwanie T-inwariantów. Algorytm
	 */
	@SuppressWarnings("unused")
	public void searchTInvariants() {
		printMatrix(globalIncidenceMatrix);
		printMatrix(globalIdentityMatrix);
		
		// Etap I - miejsca 1-in 1-out
		ArrayList<ArrayList<Integer>> generatedRows = new ArrayList<ArrayList<Integer>>();
		log("Phase I inititated. Performing only for all 1-in/1-out places.","text", false);
		int placesNumber = globalIncidenceMatrix.get(0).size();
		for (int p = 0; p < placesNumber; p++) {
			if (isSimplePlace(globalIncidenceMatrix, p) == true) { // wystepuje tylko jedno wejście i wyjście z miejsca
				generatedRows = findNewRows_MR(p); // na bazie globalIncidenceMatrix i Identity
				rewriteIncidenceIntegrityMatrices_MR(generatedRows, p);
				zeroColumnVectorP.add(p);
				generatedRows.clear(); //wyczyść macierz przekształceń
			} else {
				//rowsPhaseI.clear(); 
				nonZeroColumnVectorP.add(p);
			}
		}
		
		//TODO: sprawdzić co z niekanonicznymi - usunąć, czy zmienić w kanoniczne!

		int breakPoint = 1;
		// Etap II - cała reszta
		while(nonZeroColumnVectorP.size() != 0) {
			generatedRows.clear(); //wyczyść macierz przekształceń
			
			int stepsToFinish = nonZeroColumnVectorP.size();
			int[] res = chooseNextColumn();
			int cand = res[0];
			int rowsChange = res[1];
			//int pos = res[2];
			//int neg = res[3];
			int oldSize = globalIncidenceMatrix.size();
			
			generatedRows = findNewRows_MR(cand); // na bazie globalIncidenceMatrix i Identity
			rewriteIncidenceIntegrityMatrices_MR(generatedRows, cand);
			int indCand = nonZeroColumnVectorP.indexOf(cand);
			nonZeroColumnVectorP.remove(indCand);
			zeroColumnVectorP.add(indCand);
			int newRej = newRejected;
			int oldRep = oldReplaced;
			int nonCanon = notCanonical;
			int newSize = globalIncidenceMatrix.size();
			
			int xxx=1;
		}
		
		//invariantsList = new ArrayList<ArrayList<Integer>>(globalIdentityMatrix);
		setInvariants(globalIdentityMatrix);
		
		if(invINAMatrix != null) {
			ArrayList<ArrayList<Integer>> res =  compareInv();
			System.out.println();
			System.out.println("Reference set size:   "+invINAMatrix.size());
			System.out.println("Computed set size:    "+getInvariants().size());
			System.out.println("Common set size:      "+res.get(0).size());
			System.out.println("Not in reference set: "+res.get(1).size());
			System.out.println("Not in computed set:  "+res.get(2).size());
			
			System.out.println("Repeated in common set: "+res.get(3).get(0));
			System.out.println("Repeated not in ref.set:"+res.get(3).get(1));
		}
		
	}
	
	/**
	 * Metoda pomocnicza I fazy obliczeń: sprawdza, czy dane miejsce ma tylko 1 tranzycję wejściową
	 * i tylko 1 wyjściową. Albo czy w ogóle ma.
	 * @param incidenceMatrix ArrayList[ArrayList[Integer]] - macierz incydencji
	 * @param place int - indeks miejsca
	 * @return boolean - true, jeśli dane miejsce ma tylko 1 tranzycję IN i 1 OUT
	 */
	private boolean isSimplePlace(ArrayList<ArrayList<Integer>> incidenceMatrix, int place) {
		int input = 0;
		int output = 0;
		for (int t = 0; t < incidenceMatrix.size(); t++) {
			if (incidenceMatrix.get(t).get(place) > 0)
				input++;
			if (incidenceMatrix.get(t).get(place) < 0)
				output++;
			
			if(input == 2 || output == 2)
				return false;
		}
		
		if(input == 0 && output == 0) { 
			//cykl T1->P1->T2->P2->T1 (zamiast łuku odczytu) powoduje takie cuda w tej fazie - następuje zerowanie 2 kolumn
			//za pomocą jednego przekształcenia liniowego
			return true;
		}
		
		if (input == 1 && output == 1)
			return true;
		else
			return false;
	}
	
	
	/**
	 * Metoda odpowiedzialna za wybór kolumny dla której powstanie najmniej nowych wierszy. Działa w II fazie
	 * pracy algorytmu. Podaje pierszy indeks kolumny z najmniejszą wartością [1] - tj. kolumnę dla której przekształcenia
	 * liniowe dodadzą najmniej nowych wierszy do macierzy przekształceń.
	 * @return int[] - tablica int[4], gdzie [0] to nr kolumny z najmniejsza liczbą nowych wierszy które będą utworzone 
	 * 		po utworzeniu kombinacji liniowych [1]; [2] to liczba wierszy z wartością >0 w wybranej kolumnie,
	 * 		[3] to liczba wierszy z wartością <0 w wybranej kolumnie
	 */
	private int[] chooseNextColumn() {
		ArrayList<Integer> numberOfNewRows = new ArrayList<Integer>();
		ArrayList<Integer> positives = new ArrayList<Integer>();
		ArrayList<Integer> negatives = new ArrayList<Integer>();
		int posCounter = 0;
		int negCounter = 0;
		int rowSize = globalIncidenceMatrix.size();
		int candidateNumber = nonZeroColumnVectorP.size();
		
		for(int i=0; i<candidateNumber; i++) {
			int p = nonZeroColumnVectorP.get(i);
			posCounter = 0;
			negCounter = 0;
			for(int t=0; t<rowSize; t++) {
				if(globalIncidenceMatrix.get(t).get(p) > 0) {
					posCounter++;
				}
				if(globalIncidenceMatrix.get(t).get(p) < 0) {
					negCounter++;
				}
			}
			numberOfNewRows.add((posCounter*negCounter)-(posCounter+negCounter));
			positives.add(posCounter);
			negatives.add(negCounter);
		}
		
		int cand = nonZeroColumnVectorP.get(0);
		int nOfRows = numberOfNewRows.get(0);
		int poss = positives.get(0);
		int negg = negatives.get(0);
		
		for(int i=0; i<candidateNumber; i++) {
			if(numberOfNewRows.get(i) < nOfRows) {
				nOfRows = numberOfNewRows.get(i);
				cand = nonZeroColumnVectorP.get(i);
				poss = positives.get(i);
				negg = negatives.get(i);
			}
		}
		int[] tab = new int[4];
		tab[0] = cand;
		tab[1] = nOfRows;
		tab[2] = poss;
		tab[3] = negg;
		return tab;
	}

	/**
	 * Metoda ta zwraca macierz z informacjami o przekształceniach par wektorów miejsc.
	 * @param placeIndex int - numer miejsca do przetworzenia dla wszystkich tranzycji
	 * @return ArrayList[ArrayList[Integer]] - macierz przekształceń liniowych par wektorów
	 */
	private ArrayList<ArrayList<Integer>> findNewRows_MR(int placeIndex) {
		int sizeM = globalIncidenceMatrix.size();
		ArrayList<ArrayList<Integer>> newRows = new ArrayList<ArrayList<Integer>>();
		for (int t1 = 0; t1 < sizeM; t1++) {
			int val1 = globalIncidenceMatrix.get(t1).get(placeIndex);
			if (val1 != 0) {
				for (int t2 = t1; t2 < sizeM; t2++) {
					int val2 = globalIncidenceMatrix.get(t2).get(placeIndex);
					if (val2 != 0) {
						if (t2 != t1) { //hmmm, to po co na górze t2=t1???
							if ((val1 > 0 && val2 < 0) || (val1 < 0 && val2 > 0)) {
								int l1 = bezwzgledna(val1);
								int l2 = bezwzgledna(val2);
								
								int nwd = (l1 * l2) / nwd(l1, l2);
								ArrayList<Integer> rowsTransformation = new ArrayList<Integer>();
								rowsTransformation.add(t1);
								rowsTransformation.add(t2);
								rowsTransformation.add(nwd / globalIncidenceMatrix.get(t1).get(placeIndex));
								rowsTransformation.add(nwd / globalIncidenceMatrix.get(t2).get(placeIndex));
								rowsTransformation.add(placeIndex);
								newRows.add(rowsTransformation);
								
								//niczego jeszcze nie usuwaj, dodaj indeksy składowych do zbioru usunięć
								if(removalList.contains(t1) == false)
									removalList.add(t1);
								if(removalList.contains(t2) == false)
									removalList.add(t2);
							}
						}
					}
				}
			}
		}
		return newRows;
	}
	
	/**
	 * Metoda odpowiedzialna za dodawnie i usuwanie wierszy w wyniku wykonywania wygenerowanych przekształceń
	 * liniowych wektorów zawartych w newRowsMatrix.
	 * @param newRowsMatrix ArrayList[ArrayList[Integer]] - macierz informacji o przekształceniach
	 * @param placeIndex int - nr kolumny zerowanej
	 */
	private void rewriteIncidenceIntegrityMatrices_MR(ArrayList<ArrayList<Integer>> newRowsMatrix, int placeIndex) {
		if (newRowsMatrix.size() > 0) { //jeśli są nowe wiersze do dodania
			boolean justGotHere = true;
			
			//dodawanie nowych wierszy
			int size = newRowsMatrix.size();
			double interval = (double)size / 100; 
			int steps = 0;
			int percent = 0;
			for (int row = 0; row < size; row++) {
				int tr = newRowsMatrix.get(row).get(0); //wiersz
				int pl = newRowsMatrix.get(row).get(4); //miejsce (kolumna)
				
				if (globalIncidenceMatrix.get(tr).get(pl) < 0) { //OK, działa poniższe (tutaj), sprawdzono
					addNewRowsToMatrix(newRowsMatrix, row, -1);
				} else {
					addNewRowsToMatrix(newRowsMatrix, row, 1);
				}
				
				if(size > 1000) {
					if(justGotHere) {
						System.out.println();
						justGotHere = false;
					}
					if(steps == (int)interval) {
						percent++;
						steps = 0;
						System.out.print("*");
					} else {
						steps++;
					}
				}
			}
			
			//tutaj usuwanie U+ i U-
			int removedSoFar = 0;
			Collections.sort(removalList);
			for(int el : removalList) {
				int index = el - removedSoFar;
				globalIncidenceMatrix.remove(index);
				globalIdentityMatrix.remove(index);
				removedSoFar++;
			}
			
			removalList.clear();
			
			//usuwanie wierszy z niezerowym elementem w zerowanej kolumnie
			int mSize = globalIncidenceMatrix.size();
			for(int row=0; row < mSize; row++) {
				if(globalIncidenceMatrix.get(row).get(placeIndex) != 0) {
					globalIncidenceMatrix.remove(row);
					globalIdentityMatrix.remove(row);
					row--;
					mSize--;
				}
			}
		}
	}
	
	/**
	 * Metoda dodaje nowo wygenerowane wiersze do macierzy. Sprawdza jednak przed dodaniem, czy nowy
	 * wiersz jest minimalnym inwariantem.
	 * @param newRowsMatrix ArrayList[ArrayList[Integer]] - macierz przekształcen liniowych
	 * @param rowNumber int - numer wygenerowanego przekształcenia do dodania
	 * @param factor int - 1 lub -1 w zależności od znaku elementu wektora macierzy C w kolumnie zerowanej
	 */
	private void addNewRowsToMatrix(ArrayList<ArrayList<Integer>> newRowsMatrix, int rowNumber, int factor) {
		ArrayList<Integer> incMatrixNewRow = new ArrayList<Integer>();
		ArrayList<Integer> newRowData = newRowsMatrix.get(rowNumber);
		int t1 = newRowData.get(0);
		int t2 = newRowData.get(1);
		int multFactorT1 = newRowData.get(2);
		int multFactorT2 = newRowData.get(3);
		
		for (int b = 0; b < places.size(); b++) {
			incMatrixNewRow.add((factor)*(globalIncidenceMatrix.get(t1).get(b) * multFactorT1) + 
					(-factor)*(globalIncidenceMatrix.get(t2).get(b) * multFactorT2)); //ok
		}
		
		ArrayList<Integer> invCandidate = new ArrayList<Integer>();
		for (int b = 0; b < transitions.size(); b++) {	
			invCandidate.add(bezwzgledna((factor)*(globalIdentityMatrix.get(t1).get(b) * multFactorT1) + 
					(-factor)*(globalIdentityMatrix.get(t2).get(b) * multFactorT2)));
		}
		addOrNot(incMatrixNewRow, invCandidate, t1, t2);
	}
	
	/**
	 * Metoda odpowiedzialna za decyzję, czy inwariant dodajemy do tablicy czy nie. Uruchamia dwa testy - 
	 * szybki bazujący na analizie macierzy incydencji (oryginalnej) oraz dokładny - zawieranie wsparć
	 * wektorów w tablicy roboczej.
	 * @param incMatrixNewRow ArrayList[Integer] - nowy wektor dla roboczej macierzy którą zerujemy
	 * @param invCandidate ArrayList[Integer] - inwariant-wanna-be
	 * @param t1 int - pierwsza składowa inwariantu
	 * @param t2 int - druga składowa inwariantu
	 */
	private void addOrNot(ArrayList<Integer> incMatrixNewRow, ArrayList<Integer> invCandidate, int t1, int t2) {
		ArrayList<Integer> candidateSupport = getSupport(invCandidate);
		
		boolean canonical = checkCanonity(invCandidate, candidateSupport);
		if(!canonical) {
			//Burn the heretic. Kill the mutant. Purge the unclean.
			notCanonical++;
			return;
		}
		
		boolean fmtResult = fastMinimalityTest(invCandidate, candidateSupport);
		if(fmtResult == true) { //jak obleje, to w ogóle nie kombinujemy dalej - nie będzie dodany
			//ArrayList<Integer> resList = supportMinimalityTest(invCandidate, candidateSupport, t1, t2);
			ArrayList<Integer> resList = supportMinimalityTestNew(invCandidate, candidateSupport, t1, t2);
			if(resList.get(0) == -1) { //jest minimalny
				globalIncidenceMatrix.add(incMatrixNewRow);
				globalIdentityMatrix.add(invCandidate);
			}
			
			if(resList.get(0) == -99) {
				return;
			}
			resList.remove(0); // -1 lub -99
			
			if(resList.size() > 1) {
				System.out.println("Inwariant się nie nadaje, ale jest jest mniejszy niż obecne w macierzy! ERROR!");
				int check=1;
			}
			
			while(resList.size()>0) { //inne do usunięcia
				int remCandidate = resList.get(0);
				if(removalList.contains(remCandidate) == false) {
					removalList.add(remCandidate);
				}
				resList.remove(0);
			}
		} else {
			newRejected++;
		}
	}

	private boolean checkCanonity(ArrayList<Integer> invCandidate, ArrayList<Integer> supports) {
		int result = invCandidate.get(supports.get(0));
		for(int number: supports) {
			int value = invCandidate.get(number);
	        result = nwd(result, value);
		}
	    if(result == 1)
	    	return true;
	    else
	    	return false;
	}

	private boolean fastMinimalityTest(ArrayList<Integer> invCandidate, ArrayList<Integer> supports) {
		int supportSize = supports.size();
		int nonZeroColumn = 0;
		for(int pl=0; pl<CMatrix.get(0).size(); pl++) {
			for(int s : supports) { //inwarianty
				int el = CMatrix.get(s).get(pl);
				if(el != 0) {
					nonZeroColumn++;
					break;
				}
			}
		}
		if(supportSize > nonZeroColumn+1) {
			return false; //non-minimal inv.
		} else {
			return true;
		}
	}
	
	/**
	 * Zgroza. Jeśli coś nie działa, to problem jest tutaj na 99%.
	 * 
	 * @param invCandidate
	 * @param candSupport
	 * @param t1
	 * @param t2
	 * @return
	 */
	@SuppressWarnings("unused")
	private ArrayList<Integer> supportMinimalityTest(ArrayList<Integer> invCandidate, ArrayList<Integer> candSupport, int t1, int t2) {
		ArrayList<Integer> removeList = new ArrayList<Integer>();
		removeList.add(-1); //domyślnie można dodać nowy inw
		
		int size = globalIdentityMatrix.size();
		int supportSize = candSupport.size();
		int matrixElementSupportSize = 0;
		int invNotMinimalFactor = 0; // ">=" czy nowy jest minimalny >
		int invNotMinimalFactorStrong = 0; // ">"
		int invBelongToMVector = 0; // ">=" czy może jest lepszy od którego już w macierzy...?
		int invBelongToMVectorStrong = 0; // ">"
		for(int vector=0; vector<size; vector++) {
			if(vector == t1 || vector == t2)
				continue;

			matrixElementSupportSize = getSupport(globalIdentityMatrix.get(vector)).size();
			invNotMinimalFactor = 0;
			invBelongToMVector = 0;
			invBelongToMVectorStrong = 0;
			invNotMinimalFactorStrong = 0;
			
			for(int i=0; i<vectorSize; i++) { //dla każdego elementu inwariantu
				int matrixVectorElement = globalIdentityMatrix.get(vector).get(i);
				int invariantElement = invCandidate.get(i);

				if(matrixVectorElement > 0 && matrixVectorElement >= invariantElement) {
					invBelongToMVector++;  //jeśli element kandydata zawiera się w niezerowym elemencie już obecnym w macierzy I
					if(matrixVectorElement > invariantElement) {
						invBelongToMVectorStrong++;
					}
				} 
				if(invariantElement > 0 && invariantElement >= matrixVectorElement) {
					invNotMinimalFactor++; //jeśli element wsparcia inwariantu zawiera element wsparcia już obecnego w macierzy wiersza
					if(invariantElement > matrixVectorElement)
						invNotMinimalFactorStrong++;
				}
				
				//if(invNotMinimalFactor > 0 && invBelongToMVectorStrong > 0 ) {
				if(invNotMinimalFactorStrong > 0 && invBelongToMVectorStrong > 0 ) {
					// wsparcia się rozmijają w n-wymiarowej przestrzeni
					invNotMinimalFactor = invBelongToMVector = -1;
					break;
				}
			}
			
			if(invNotMinimalFactor > 0 && invNotMinimalFactor == supportSize) {
				//każdy element wsparcia inwariantu jest >= od elementu w testowanym inwariance z macierzy 
				if(invBelongToMVectorStrong == 0) {
					// ten warunek oznacza, że nie pominięto żadnego elementu wsparcia w sprawdzanym wektorze 
					// z tablicy, tj. nie ma takiego niezerowego elementu w wierszu z macierzy incydencji, 
					// który byłby większy od swojego odpowiednika w sprawdzanym inwariancie
					newRejected ++; 		//także jesli matrixElement_(i) == invCandidate.get(i)
					if(removeList.size() > 1) { // to niby jak? kandydat odpada, bo nie jest minimalny, a w macierzy sa jeszcze większe?!
						int somethingVERYWRONG = 1;
					}
					removeList.clear();
					removeList.add(-99); //jednak nie dodajemy, już jest mniejszy od niego
					return removeList;
				}
			} 
			
			if(invBelongToMVector > 0 && invBelongToMVector == matrixElementSupportSize) { 
				if(invNotMinimalFactorStrong == 0) {
					//dopiero jako drugie, bo jeśli są równe, to nowego nie dodajemy i w ogóle do poniższych polecenie algorytm nie dojdzie:
					oldReplaced ++;		//mamy kandydata do usunięcia z macierzy i zastąpienia aktualnym! zapamiętać vector
					removeList.add(vector);
				}
			}
		}
		return removeList;
	}
	
	/**
	 * Metoda porównująca aktualny zbiór inwariantów (referencyjny, skądkolwiek wcześniej wzięty i
	 * obecny w programie w chwili uruchamiania generatora) z tym, który powstał z generatora programu.
	 * @return ArrayList[ArrayList[Integer]] - macierz wyników, 3 wektory: <br>
	 *  1 - część wspólna inwariantów ze zbiorem referencyjnym <br>
	 *  2 - inwarianty których nie ma w referencyjnym<br>
	 *  3 - inwarianty referencyjnego których nie ma w wygenerowanym zbiorze
	 */
	private ArrayList<ArrayList<Integer>> compareInv() {
		int myInvSize = getInvariants().size();
		int INAInvSize = invINAMatrix.size();
		
		ArrayList<Integer> ourFoundInINA = new ArrayList<Integer>();
		ArrayList<Integer> inaFoundInOur = new ArrayList<Integer>();
		ArrayList<Integer> nonInINA = new ArrayList<Integer>(); //są u nas, nie ma w INA - minimalne??
		ArrayList<Integer> nonInMySet = new ArrayList<Integer>(); //są w INA, nie ma u nas
		
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		
		int repeated = 0;
		int repeatedNotFound = 0;

		boolean presentInINASet = false;
		for(int invMy=0; invMy<myInvSize; invMy++) {
			presentInINASet = false;
			ArrayList<Integer> myInvariant = getInvariants().get(invMy);
			
			for(int invINA=0; invINA < INAInvSize; invINA++) {
				if(invINAMatrix.get(invINA).equals(myInvariant)) {
					
					ourFoundInINA.add(invMy);
					if(inaFoundInOur.contains(invINA)) {
						repeated++;
					} else {
						inaFoundInOur.add(invINA);
					}
					
					presentInINASet = true;
					break;
				}
			}
			if(presentInINASet == false) {
				if(nonInINA.contains(invMy))
					repeatedNotFound++;
				nonInINA.add(invMy);
			}
		}
		
		for(int i=0; i<INAInvSize; i++) {
			if(inaFoundInOur.contains(i) == false) {
				nonInMySet.add(i);
			}
		}
		ArrayList<Integer> repeatedVector = new ArrayList<Integer>();
		repeatedVector.add(repeated);
		repeatedVector.add(repeatedNotFound);
		
		result.add(ourFoundInINA); //część wspolna
		result.add(nonInINA); //nasze, ale nie ma INA
		result.add(nonInMySet); //inwarianty INY których nie mamy
		result.add(repeatedVector);
		return result;
	}
	
	private ArrayList<Integer> supportMinimalityTestNew(ArrayList<Integer> invCandidate, ArrayList<Integer> invSupport, int t1, int t2) {
		ArrayList<Integer> removeList = new ArrayList<Integer>();
		removeList.add(-1); //domyślnie można dodać nowy inw
		
		int matrixSize = globalIdentityMatrix.size();
		int invSuppSize = invSupport.size();
		int successCounter = 0;
		
		for(int vector=0; vector<matrixSize; vector++) {
			if(vector == t1 || vector == t2) //nie testujemy ze składowymi
				continue;

			ArrayList<Integer> refSupport = getSupport(globalIdentityMatrix.get(vector));
			int result = checkCoverability(globalIdentityMatrix.get(vector), refSupport, invCandidate, invSupport);
			
			if(result == 0 || result == -1) { //identyczny jak znaleziony w macierzy lub referencyjny jest w nim zawarty: nie dodajemy
				newRejected++;
				removeList.set(0, -99);
				if(removeList.size() > 1) { // to niby jak? kandydat odpada, bo nie jest minimalny, a w macierzy sa jeszcze większe?!
					int somethingVERYWRONG = 1;
				}
				
				return removeList;
			} else if(result == 1 ) { //jest lepszy od testowanego referencyjnego:
				//referencyjny do wywalenia
				successCounter++;
				oldReplaced ++;		//mamy kandydata do usunięcia z macierzy i zastąpienia aktualnym! zapamiętać vector
				removeList.add(vector);
			} else if(result == -99) {
				successCounter++;
			}
		}
			
		if(successCounter == matrixSize - 2)
			return removeList;
		else {
			return removeList; //WTF! nie powinno być możliwe!
		}
	}
	
	private int checkCoverability(ArrayList<Integer> refInv, ArrayList<Integer> refSupport, ArrayList<Integer> candidateInv,
			ArrayList<Integer> candSupport) {
		int sizeRef = refInv.size();
		int sizeCan = candidateInv.size();
		if(sizeRef != sizeCan)
			return -99; // z czym do ludzi?
		
		int refElement = 0;
		int canElement = 0;
		int CanInRef = 0; // >=  - każdy element wsparcia referencyjnego jest >= od elementu kandydata
		int CanInRefStrong = 0; // >
		int RefInCan = 0; // >=  - każdy element wsparcia kandydata jest >= od elementu referencyjnego
		int RefInCanStrong = 0; // >
		int refSuppSize = refSupport.size();
		int candSuppSize = candSupport.size();
		
		for(int ind=0; ind<sizeRef; ind++) { //po każdym elemencie inwariantów
			refElement = refInv.get(ind);
			canElement = candidateInv.get(ind);
			
			if(refElement > 0 && refElement >= canElement) {
				CanInRef++;  // zawieranie kandydata w refencyjnym
				if(refElement > canElement) { 
					CanInRefStrong++;
				}
			} 
			if(canElement > 0 && canElement >= refElement) {
				RefInCan++; // zawieranie referencyjnego w kandydacie
				if(canElement > refElement)
					RefInCanStrong++;
			}
			
			if(CanInRefStrong > 0 && RefInCanStrong > 0 ) {
				// wsparcia się rozmijają w n-wymiarowej przestrzeni
				// niezależne
				return -99;
			}
		}
		
		//decyzja:
		if(CanInRef == refSuppSize && RefInCan == candSuppSize && CanInRefStrong == 0 && RefInCanStrong == 0) {
			return 0; //identyczne
			/* Każdy element wsparcia dla referencyjnego jest >= od el. kandydata (CanInRef == refSuppSize) a precyzyjnie to
			 * równy (bo CanInRefStrong == 0), i odwrotnie: każdy element wsparcia kandydata jest >= od el. refencyjnego 
			 * (RefInCan == candSuppSize) a dokładnie to = (bo: RefInCanStrong == 0)
			 */
		}
		
		if(RefInCan == candSuppSize && RefInCanStrong > 0) {
			return -1; // kandydat nie jest minimalny: referencyjny się w nim zawiera
			/* Referencyjny wektor zawiera się w kandydacie na każdym elemencie wsparcia kandydata (RefInCan == candSuppSize) oraz
			 * choć na jednym elemencie jest mniejszy (a nie tylko mniejszy/równy, tj: RefInCanStrong > 0). Gdyby było inaczej to
			 * byłby identyczny (warunek wyżej), lub w ogóle niezależny (CanInRefStrong > 0 && RefInCanStrong > 0 w pętli)
			 */
		}
		
		if(CanInRef == refSuppSize && CanInRefStrong > 0) {
			return 1; // kandydat jest mniejszy niż wektor referencyjny który testujemy
			/* Kandydat zawiera się w referencyjnym na każdym elemencie wsparcia elementu referencyjnego (CanInRef == refSuppSize) oraz
			 * na jakimś elemencie jest mniejszy, a nie tylko mniejszy/równy (CanInRefStrong > 0).
			 */
		} else {
			System.out.println("Niemożliwy stan został osiągnięty. Konkluzja: znajdź sobie inny zbiór inwariantów niż ten.");
			return -99; //teoretycznie NIGDY nie powinniśmy się tu pojawić
		}
	}
	
	private void finalSupportMinimalityTest() {	
		int size = globalIdentityMatrix.size();
		ArrayList<ArrayList<Integer>> getRidOfMatrix = new ArrayList<ArrayList<Integer>>();
		
		boolean allChecked = true;
		int tested1 = 0;
		while(allChecked == false) {
			ArrayList<Integer> inv = globalIdentityMatrix.get(tested1);
			
		}
		
		for(int t1=0; t1<size; t1++) {
			ArrayList<Integer> inv = globalIdentityMatrix.get(t1);
			ArrayList<Integer> supports = getSupport(inv);
			boolean minimal = true;
			for(int t2=0; t2<size; t2++) {
				if(t1 != t2) {
					
				}
			}
		}
	}
	
	/**
	 * Metoda zwraca wsparcie wektora wejściowego - numery pozycji na których w wektorze
	 * znajdują się wartości dodatnie.
	 * @param inv ArrayList[Integer] - wektor wejściowy
	 * @return ArrayList[Integer] - wsparcie
	 */
	private ArrayList<Integer> getSupport(ArrayList<Integer> inv) {
		ArrayList<Integer> supports = new ArrayList<Integer>();
		int size = inv.size();
		for(int i=0; i<size; i++) {
			if(inv.get(i) > 0) { //jeśli na danej pozycji jest wartość >0
				supports.add(i); //dodaj pozycję jako wsparcie
			}
		}
		return supports;
	}

	private void printMatrix(ArrayList<ArrayList<Integer>> matrix) {
		for (int it = 0; it < matrix.size(); it++) {
			//System.out.println();
			String msg = "";
			for (int jo = 0; jo < matrix.get(0).size(); jo++) {
				//System.out.print(incMatrix.get(it).get(jo) + " ");
				msg = msg + matrix.get(it).get(jo) + "  ";
			}
			log(msg, "text", true);
		}
	}
	
	/**
	 * Metoda zwraca największy wspólny dzielnik dwóch liczb naturalnych dodatnich.
	 * @param x int - I liczba
	 * @param y int - II liczba
	 * @return int - największy wspólny dzielnik
	 */
	public static int nwd(int x, int y) {
		while (x != y) {
			if (x > y)
				x -= y;
			else
				y -= x;
		}
		return x;
	}
	
	/**
	 * Zwraca wartość bezwzględną liczby podanej jako argument.
	 * @param i int - liczba
	 * @return int - |i|
	 */
	public int bezwzgledna(int i) {
		if (i < 0)
			return -i;
		else
			return i;
	}
	
	public int silnia(int i) {
		if (i == 0)
			return 1;
		else
			return i * silnia(i - 1);
	}


	/**
	 * Metoda zwraca macierz inwariantów.
	 * @return ArrayList[ArrayList[Integer]] - macierz inwariantów (wiersze)
	 */
	public ArrayList<ArrayList<Integer>> getInvariants() {
		return invariantsList;
	}

	/**
	 * Metoda ustala nową macierz inwariantów.
	 * @param invMatrix ArrayList[ArrayList[Integer]] - nowa macierz inwariantów
	 */
	public void setInvariants(ArrayList<ArrayList<Integer>> invMatrix) {
		this.invariantsList = invMatrix;
	}
	
}

/*

public class EarlyInvariantsAnalyzer implements Runnable {
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();

	private HashMap<Place, Integer> placesMap = new HashMap<Place, Integer>();
	private HashMap<Transition, Integer> transitionsMap = new HashMap<Transition, Integer>();
	private ArrayList<ArrayList<Integer>> invariantsList = new ArrayList<ArrayList<Integer>>();

	private ArrayList<ArrayList<Integer>> incidanceMatrixL;
	private ArrayList<ArrayList<Integer>> TxTMatrixL;
	@SuppressWarnings("unused")
	private int glebokosc = 0;
	private boolean znalazl = false;
	//private NetPropertiesAnalyzer NPA = new NetPropertiesAnalyzer();

	@Override
	public void run() {
		this.CreateIncidanceMatrixAndTxTMatrix();
		this.Analyze();
		PetriNet project = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(project.getEIAgeneratedInv_old());
		project.setInvariantsMatrix(invariantsList);
	}

	public EarlyInvariantsAnalyzer() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		createDictionary();
	}

	private void createDictionary() {
		for (int i = 0; i < places.size(); i++)
			placesMap.put(places.get(i), i);
		for (int i = 0; i < transitions.size(); i++)
			transitionsMap.put(transitions.get(i), i);
	}

	public void CreateIncidanceMatrixAndTxTMatrix() {
		incidanceMatrixL = new ArrayList<ArrayList<Integer>>();
		TxTMatrixL = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < transitions.size(); i++) {
			ArrayList<Integer> tmpList = new ArrayList<Integer>();
			for (int j = 0; j < places.size(); j++) {
				tmpList.add(0);
			}
			incidanceMatrixL.add(tmpList);
		}

		for (Arc oneArc : arcs) {
			int tPosition = 0;
			int pPosition = 0;
			int incidanceValue = 0;

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION
					|| oneArc.getStartNode().getType() == PetriNetElementType.TIMETRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidanceValue = -1 * oneArc.getWeight(); // sprawd�
			} else {
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				incidanceValue = 1 * oneArc.getWeight(); // sprawd�
			}

			incidanceMatrixL.get(tPosition).set(pPosition, incidanceValue);
		}

		for (int i = 0; i < transitions.size(); i++) {
			ArrayList<Integer> tmpList = new ArrayList<Integer>();
			for (int j = 0; j < transitions.size(); j++) {
				if (i == j) {
					tmpList.add(1);
				} else {
					tmpList.add(0);
				}
			}
			TxTMatrixL.add(tmpList);
		}
	}

	@SuppressWarnings("unused")
	public void Analyze() {

		ArrayList<ArrayList<Integer>> incidanceMatrixTMPL = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> TxTMatrixTMPL = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<ArrayList<Integer>>> incidanceListL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<ArrayList<Integer>>> tListL = new ArrayList<ArrayList<ArrayList<Integer>>>();

		incidanceListL.add(incidanceMatrixL);
		tListL.add(TxTMatrixL);

		incidanceMatrixTMPL = incidanceListL.get(incidanceListL.size() - 1);
		TxTMatrixTMPL = tListL.get(tListL.size() - 1);
		ArrayList<ArrayList<Integer>> rowsForEtapI = new ArrayList<ArrayList<Integer>>();

		System.out.println("------->Czysta nie naruszona siec<---------");
		System.out.println("------->Macierz incydencji<---------");
		for (int it = 0; it < incidanceMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < places.size(); jo++)
				System.out.print(incidanceMatrixTMPL.get(it).get(jo) + " ");
		}
		System.out.println();
		System.out.println("------->Macierz TxT<---------");
		for (int it = 0; it < TxTMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < transitions.size(); jo++)
				System.out.print(TxTMatrixTMPL.get(it).get(jo) + " ");
		}

		// Etap I z artykulu
		for (int p = 0; p < places.size(); p++) {
			// Wystepuje tylko jedno wejscie i wyjscie z miejsca
			if (checkEtapI(incidanceMatrixTMPL, p)) {
				findAndCreateNewRowL(incidanceMatrixTMPL, rowsForEtapI, p);
				sumRowsForIEtap(incidanceMatrixTMPL, TxTMatrixTMPL, rowsForEtapI);

			}
			rowsForEtapI.clear();
			incidanceListL.clear();
			incidanceListL.add(incidanceMatrixTMPL);
			tListL.clear();
			tListL.add(TxTMatrixTMPL);
		}

		System.out.println("------->EtapI - zakonczony<---------");
		System.out.println("------->Macierz incydencji<---------");
		for (int it = 0; it < incidanceMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < places.size(); jo++)
				System.out.print(incidanceMatrixTMPL.get(it).get(jo) + " ");
		}
		System.out.println();
		System.out.println("------->Macierz TxT<---------");
		for (int it = 0; it < TxTMatrixTMPL.size(); it++) {
			System.out.println();
			for (int jo = 0; jo < transitions.size(); jo++)
				System.out.print(TxTMatrixTMPL.get(it).get(jo) + " ");
		}

		incidanceMatrixTMPL = incidanceListL.get(incidanceListL.size() - 1);
		incidanceListL.remove(incidanceListL.size() - 1);
		TxTMatrixTMPL = tListL.get(tListL.size() - 1);
		tListL.remove(tListL.size() - 1);

		ArrayList<ArrayList<ArrayList<Integer>>> iLL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<ArrayList<Integer>>> tLL = new ArrayList<ArrayList<ArrayList<Integer>>>();
		ArrayList<ArrayList<Integer>> newRowL = new ArrayList<ArrayList<Integer>>();

		// Etap II z artykulu

		ArrayList<ArrayList<Integer>> im = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < incidanceMatrixTMPL.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int j = 0; j < incidanceMatrixTMPL.get(i).size(); j++)
				tmp.add(incidanceMatrixTMPL.get(i).get(j));
			im.add(tmp);
		}

		ArrayList<ArrayList<Integer>> txt = new ArrayList<ArrayList<Integer>>();

		for (int i = 0; i < TxTMatrixTMPL.size(); i++) {
			ArrayList<Integer> tmp = new ArrayList<Integer>();
			for (int j = 0; j < TxTMatrixTMPL.get(i).size(); j++)
				tmp.add(TxTMatrixTMPL.get(i).get(j));
			txt.add(tmp);
		}

		etapII(im, txt, newRowL);
		setFoundedInvariantsL(im, txt);
		getMinimal(invariantsList);

		System.out.print("Koniec pracy analizatora");
	}
	
	@SuppressWarnings("unused")
	private void propAnalyze()
	{
		//PUR - pure net
		boolean isPure = true;
		for(Transition t : transitions)
		{
			boolean arcIn = false;
			boolean arcOut = false;
			for(ElementLocation el : t.getElementLocations())
			{
				if(!el.getInArcs().isEmpty()&&arcIn==false)
					arcIn = true;
				if(!el.getOutArcs().isEmpty()&&arcOut==false)
					arcOut = true;
			}
			if(arcIn==false||arcOut==false)
				isPure = false;
		}		
		for(Place p : places)
		{
			boolean arcIn = false;
			boolean arcOut = false;
			for(ElementLocation el : p.getElementLocations())
			{
				if(!el.getInArcs().isEmpty()&&arcIn==false)
					arcIn = true;
				if(!el.getOutArcs().isEmpty()&&arcOut==false)
					arcOut = true;
			}
			if(arcIn==false||arcOut==false)
				isPure = false;
		}
		
		//ORD - ordinary net
		boolean isOrdinary = true;
		for(Arc a : arcs)
			if(a.getWeight()!=1)
				isOrdinary = false;
		
		//HOM - homogenous net
		boolean isHomogenous = true;
		for(Place p : places)
		{
			int val = 0;
			for(ElementLocation el : p.getElementLocations())
				for(Arc a : el.getOutArcs())
					if(val==0)
						val = a.getWeight();
					else if(val!=a.getWeight())
						isHomogenous = false;			
		}
		//NBM - non blocking multiplicity net
		boolean isNonBlockingMulti = true;
		for(Place p : places)
		{
			int valIn = Integer.MAX_VALUE;
			int valOut = 0;
			for(ElementLocation el : p.getElementLocations())
			{
				for(Arc a : el.getInArcs())
					if(a.getWeight() < valIn)
						valIn = a.getWeight(); 
				for(Arc a : el.getOutArcs())
					if(a.getWeight() > valOut)
						valOut = a.getWeight();
				
			}
			if(valOut>valIn)
				isNonBlockingMulti = false;				
		}
		
		ArrayList<Boolean> NetProps = new ArrayList<Boolean>();
		NetProps.add(isPure);
		NetProps.add(isOrdinary);
		NetProps.add(isHomogenous);
		NetProps.add(isNonBlockingMulti);
		//GUIManager.getDefaultGUIManager()
	}

	@SuppressWarnings("unused")
	private int chceckInv(ArrayList<Integer> in1, ArrayList<Integer> in2) {
		int zgodnosc1 = 0;
		int zgodnosc2 = 0;
		int zgodnosc3 = 0;
		// czy in1 jest bardziej polak... minimalny niz nv2
		for (int i = 0; i < in1.size(); i++) {

			if (in1.get(i) > in2.get(i))
				zgodnosc1++;
			if (in1.get(i) < in2.get(i))
				zgodnosc2++;
			if (in1.get(i) == 0 && in2.get(i) > 0)
				zgodnosc3++;
		}

		if (zgodnosc1 > 0 && zgodnosc2 == 0)
			return 1;
		if (zgodnosc1 > 0 && zgodnosc2 > 0)
			return 2;
		else
			return 0;
	}

	private void etapII(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL) {
		for (int p = 0; p < places.size(); p++) {
			if (!znalazl) {

				if (checkEtapII(incidanceMatrixTMPL, p)) {

					ArrayList<ArrayList<Integer>> im = new ArrayList<ArrayList<Integer>>();

					for (int i = 0; i < incidanceMatrixTMPL.size(); i++) {
						ArrayList<Integer> tmp = new ArrayList<Integer>();
						for (int j = 0; j < incidanceMatrixTMPL.get(i).size(); j++)
							tmp.add(incidanceMatrixTMPL.get(i).get(j));
						im.add(tmp);
					}

					ArrayList<ArrayList<Integer>> txt = new ArrayList<ArrayList<Integer>>();

					for (int i = 0; i < TxTMatrixTMPL.size(); i++) {
						ArrayList<Integer> tmp = new ArrayList<Integer>();
						for (int j = 0; j < TxTMatrixTMPL.get(i).size(); j++)
							tmp.add(TxTMatrixTMPL.get(i).get(j));
						txt.add(tmp);
					}

					findAndCreateNewRowL(im, newRowL, p);

					if (newRowL.size() > 0) {
						addRows(im, txt, newRowL, true);
					}
					newRowL.clear();

					boolean pusty = true;
					pusty = checkIfHasFinishL(im, pusty);
					if (pusty) {
						setFoundedInvariantsL(im, txt);
						znalazl = true;
					} else {

						glebokosc++;

						if (p + 1 == places.size()) {

						} else {
							etapII(im, txt, newRowL);
						}
						glebokosc--;
					}

					setFoundedInvariantsL(im, txt);
				}
			}
		}
	}

	private void sumRowsForIEtap(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL) {
		if (newRowL.size() > 0) {
			ArrayList<ArrayList<Integer>> incidanceMatrixTML = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> TxTMatrixTML = new ArrayList<ArrayList<Integer>>();

			for (int l = 0; l < newRowL.size(); l++) {
				int tr = newRowL.get(l).get(0);
				int pl = newRowL.get(l).get(4);

				if (incidanceMatrixTMPL.get(tr).get(pl) < 0)
					addNewRowVerIL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL,
							incidanceMatrixTML, TxTMatrixTML, l);
				else
					addNewRowVerIIL(incidanceMatrixTMPL, TxTMatrixTMPL,
							newRowL, incidanceMatrixTML, TxTMatrixTML, l);

			}

			addOldRowsL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL, incidanceMatrixTML, TxTMatrixTML);

			incidanceMatrixTMPL.clear();
			for (int i = 0; i < incidanceMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < incidanceMatrixTML.get(i).size(); j++)
					tmp.add(incidanceMatrixTML.get(i).get(j));
				incidanceMatrixTMPL.add(tmp);
			}
			// incidanceMatrixTMPL.add(incidanceMatrixTML.get(i));
			TxTMatrixTMPL.clear();
			for (int i = 0; i < TxTMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < TxTMatrixTML.get(i).size(); j++)
					tmp.add(TxTMatrixTML.get(i).get(j));
				TxTMatrixTMPL.add(tmp);
			}
			// TxTMatrixTMPL.add(TxTMatrixTML.get(i));
		}
	}

	private void addRows(ArrayList<ArrayList<Integer>> incidanceMatrixTMPL,
			ArrayList<ArrayList<Integer>> TxTMatrixTMPL,
			ArrayList<ArrayList<Integer>> newRowL, boolean etap) {
		if (newRowL.size() > 0) {
			ArrayList<ArrayList<Integer>> incidanceMatrixTML = new ArrayList<ArrayList<Integer>>();
			ArrayList<ArrayList<Integer>> TxTMatrixTML = new ArrayList<ArrayList<Integer>>();

			for (int l = 0; l < newRowL.size(); l++) {

				if (incidanceMatrixTMPL.get(newRowL.get(l).get(0)).get(
						newRowL.get(l).get(4)) < 0)
					addNewRowVerIL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL,
							incidanceMatrixTML, TxTMatrixTML, l);
				else
					addNewRowVerIIL(incidanceMatrixTMPL, TxTMatrixTMPL,
							newRowL, incidanceMatrixTML, TxTMatrixTML, l);

			}

			addOldRowsL(incidanceMatrixTMPL, TxTMatrixTMPL, newRowL, incidanceMatrixTML, TxTMatrixTML);

			incidanceMatrixTMPL.clear();
			for (int i = 0; i < incidanceMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < incidanceMatrixTML.get(i).size(); j++)
					tmp.add(incidanceMatrixTML.get(i).get(j));
				incidanceMatrixTMPL.add(tmp);
			}

			TxTMatrixTMPL.clear();
			for (int i = 0; i < TxTMatrixTML.size(); i++) {
				ArrayList<Integer> tmp = new ArrayList<Integer>();
				for (int j = 0; j < TxTMatrixTML.get(i).size(); j++)
					tmp.add(TxTMatrixTML.get(i).get(j));
				TxTMatrixTMPL.add(tmp);
			}
		}
	}

	private void setFoundedInvariantsL(ArrayList<ArrayList<Integer>> iL, ArrayList<ArrayList<Integer>> tL) {
		for (int it = 0; it < iL.size(); it++) {
			boolean zero = true;
			for (int jo = 0; jo < places.size(); jo++) {
				if (iL.get(it).get(jo) != 0)
					zero = false;
			}
			if (zero)
			{
				if (checkIfExist(invariantsList, tL.get(it)) == 0) {
					invariantsList.add(tL.get(it));
					System.out.println("!!!Wstawiam!!!");
				}
				
				//if (checkIfExist(listaInvatianow, tL.get(it),x) == 2) {
				//	listaInvatianow.set(x, tL.get(it));
				//	System.out.println("!!!Wstawiam2!!!");
				//}
				
			}
		}
	}
	
	private void getMinimal(ArrayList<ArrayList<Integer>> iL) {
		ArrayList<ArrayList<Integer>> minList = new ArrayList<ArrayList<Integer>>();
		
		for (int it = 0; it < iL.size(); it++) {
			boolean isSimp = true;
			for (int it2 = 0; it2 < iL.size(); it2++) {
				if(it!=it2)
				{
					boolean tmp = isSimpler(iL,it,it2);
					if(isSimp==false&&tmp==true)
					{
						
					}
					else
					{
						isSimp = tmp;
					}
				}				
			}
			
			if(isSimp)
				minList.add(iL.get(it));
		}
		invariantsList = minList;
	}
	
	@SuppressWarnings("unused")
	private boolean isSimpler(ArrayList<ArrayList<Integer>> iL, int it1, int it2)
	{
		int u = 0;
		int n = 0;
		int r = 0;
		
		for (int i = 0; i < iL.get(0).size(); i++)
		{
			if(iL.get(it1).get(i)>iL.get(it2).get(i))
				n++;
			if(iL.get(it1).get(i)<iL.get(it2).get(i))
				u++;
			if(iL.get(it1).get(i)==iL.get(it2).get(i))
				r++;
		}
		
		if(u==0&&n>0)
			return false;
		
		return true;
	}

	@SuppressWarnings("unused")
	private int checkIfExist(ArrayList<ArrayList<Integer>> LI,
			ArrayList<Integer> INV) {
		//Tu wsadzi minimalny
		
		int exist = 0;
		for (int i = 0; i < LI.size(); i++) {
			ArrayList<Integer> oldinv = LI.get(i);
			boolean check = true;
			boolean min = true;
			for (int j = 0; j < oldinv.size(); j++) {
				if (oldinv.get(j) != INV.get(j))
					check = false;
				//if (oldinv.get(j) > INV.get(j)&&INV.get(j)!=0)
				//	min = false;//spr
			}
			if (check)
				exist = 1;
		}

		return exist;
	}

	private boolean checkIfHasFinishL(ArrayList<ArrayList<Integer>> list,
			boolean pusty) {
		pusty = true;
		for (int t = 0; t < list.size(); t++)
			for (int p = 0; p < list.get(t).size(); p++)
				if (list.get(t).get(p) != 0)
					pusty = false;

		return pusty;
	}

	private void addOldRowsL(ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM) {

		for (int i = 0; i < TxTMatrixTMP.size(); i++) {
			boolean kist = false;

			for (int k = 0; k < newRow.size(); k++)
				if ((i == newRow.get(k).get(0)) || i == newRow.get(k).get(1)) {
					kist = true;
				}

			if (kist == false) {
				ArrayList<Integer> NR = new ArrayList<Integer>();

				for (int b = 0; b < places.size(); b++) {
					NR.add(incidanceMatrixTMP.get(i).get(b));
				}
				incidanceMatrixTM.add(NR);
				NR = new ArrayList<Integer>();
				for (int b = 0; b < transitions.size(); b++) {
					NR.add(TxTMatrixTMP.get(i).get(b));
				}
				TxTMatrixTM.add(NR);
			}
		}
	}

	private boolean checkEtapI(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, int place) {
		int input = 0;
		int output = 0;
		for (int t = 0; t < incidanceMatrixTMP.size(); t++) {
			if (incidanceMatrixTMP.get(t).get(place) > 0)
				input++;
			if (incidanceMatrixTMP.get(t).get(place) < 0)
				output++;
		}
		if (input == 1 && output == 1)
			return true;
		else
			return false;
	}

	private boolean checkEtapII(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, int place) {
		int input = 0;
		int output = 0;
		for (int t = 0; t < incidanceMatrixTMP.size(); t++) {
			if (incidanceMatrixTMP.get(t).get(place) > 0)
				input++;
			if (incidanceMatrixTMP.get(t).get(place) < 0)
				output++;
		}
		if (input > 0 && output > 0)
			return true;
		else
			return false;
	}

	private void addNewRowVerIL(
			ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM, int l) {

		ArrayList<Integer> NR = new ArrayList<Integer>();
		for (int b = 0; b < places.size(); b++)
			NR.add(-(incidanceMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					+ (incidanceMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3)));

		incidanceMatrixTM.add(NR);
		NR = new ArrayList<Integer>();

		for (int b = 0; b < transitions.size(); b++)
			NR.add(bezwzgledna(-(TxTMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					+ (TxTMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3))));

		TxTMatrixTM.add(NR);
	}

	private void addNewRowVerIIL(
			ArrayList<ArrayList<Integer>> incidanceMatrixTMP,
			ArrayList<ArrayList<Integer>> TxTMatrixTMP,
			ArrayList<ArrayList<Integer>> newRow,
			ArrayList<ArrayList<Integer>> incidanceMatrixTM,
			ArrayList<ArrayList<Integer>> TxTMatrixTM, int l) {

		ArrayList<Integer> NR = new ArrayList<Integer>();
		for (int b = 0; b < places.size(); b++)
			NR.add((incidanceMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					- (incidanceMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3)));

		incidanceMatrixTM.add(NR);
		NR = new ArrayList<Integer>();

		for (int b = 0; b < transitions.size(); b++)
			NR.add(bezwzgledna((TxTMatrixTMP.get(newRow.get(l).get(0)).get(b) * newRow
					.get(l).get(2))
					- (TxTMatrixTMP.get(newRow.get(l).get(1)).get(b) * newRow
							.get(l).get(3))));

		TxTMatrixTM.add(NR);
	}

	private void findAndCreateNewRowL(ArrayList<ArrayList<Integer>> incidanceMatrixTMP, ArrayList<ArrayList<Integer>> newRow, int j) {
		int sizeM = incidanceMatrixTMP.size();
		for (int t1 = 0; t1 < sizeM; t1++) {
			int val1 = incidanceMatrixTMP.get(t1).get(j); // #
			//if (incidanceMatrixTMP.get(t1).get(j) != 0) {
			if (val1 != 0) { //#
				for (int t2 = t1; t2 < sizeM; t2++) {
					int val2 = incidanceMatrixTMP.get(t2).get(j); // #
					//if (incidanceMatrixTMP.get(t2).get(j) != 0)
					if (val2 != 0) {
						if (t2 != t1) {
							//if ((incidanceMatrixTMP.get(t1).get(j) > 0 && incidanceMatrixTMP.get(t2).get(j) < 0)
							//		|| (incidanceMatrixTMP.get(t1).get(j) < 0 && incidanceMatrixTMP.get(t2).get(j) > 0)) {
							//	int l1 = bezwzgledna(incidanceMatrixTMP.get(t1).get(j));
							//	int l2 = bezwzgledna(incidanceMatrixTMP.get(t2).get(j));
							if ((val1 > 0 && val2 < 0) || (val1 < 0 && val2 > 0)) {
								//int l1 = bezwzgledna(incidanceMatrixTMP.get(t1).get(j));
								//int l2 = bezwzgledna(incidanceMatrixTMP.get(t2).get(j));
								int l1 = bezwzgledna(val1);
								int l2 = bezwzgledna(val2);
								
								// ((x*y)/nwd(x,y))
								int nwd = (l1 * l2) / nwd(l1, l2);
								ArrayList<Integer> tab = new ArrayList<Integer>();
								tab.add(t1);
								tab.add(t2);
								tab.add(nwd / incidanceMatrixTMP.get(t1).get(j));
								tab.add(nwd / incidanceMatrixTMP.get(t2).get(j));
								tab.add(j);
								newRow.add(tab);
							}
						}
					}
				}
			}
		}
	}

	public void SetArcForAnalization(ArrayList<Arc> a) {
		arcs = a;

	}

	public static int nwd(int x, int y) {
		while (x != y) {
			if (x > y)
				x -= y;
			else
				y -= x;
		}
		return x;
	}

	public int silnia(int i) {
		if (i == 0)
			return 1;
		else
			return i * silnia(i - 1);
	}

	public int bezwzgledna(int i) {
		if (i < 0)
			return -i;
		else
			return i;
	}

	public ArrayList<ArrayList<Integer>> getListaInvatianow() {
		return invariantsList;
	}

	public void setListaInvatianow(ArrayList<ArrayList<Integer>> listaInvatianow) {
		this.invariantsList = listaInvatianow;
	}
}

*/