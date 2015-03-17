package abyss.analyzer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import Jama.Matrix;
import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.utilities.Tools;

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
	private ArrayList<ArrayList<Integer>> globalIncidenceMatrixNewRows;
	private ArrayList<ArrayList<Integer>> globalIdentityMatrix; //macierz przekształacna w inwarianty
	private ArrayList<ArrayList<Integer>> globalIdentityMatrixNewRows;
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
	
	private int aac = 0;
	private int naac = 0;
	
	private int newRejected = 0;
	private int oldReplaced = 0;
	private int notCanonical = 0;
	
	private ArrayList<ArrayList<Integer>> invINAMatrix;
	
	private ArrayList<Integer> testList = new ArrayList<Integer>();
	
	private ArrayList<Integer> testInv = new ArrayList<Integer>();
	
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
		
		globalIncidenceMatrixNewRows = new ArrayList<ArrayList<Integer>>();
		globalIdentityMatrixNewRows = new ArrayList<ArrayList<Integer>>();
		
		log("Identity matrix created for "+transitions.size()+" transitions","text", false);
	}

	/**
	 * Główna metoda klasy odpowiedzialna za wyszukiwanie T-inwariantów.
	 */
	@SuppressWarnings("unused")
	public void searchTInvariants() {
		/*
		int[] intArray = new int[] { 3, 2, 2, 2, 2, 2, 1, 3, 0, 0, 0, 0, 8, 0, 3, 0, 3, 0, 1, 1, 3, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 2, 2, 2, 2, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 3, 0, 1, 1, 2, 1, 1, 2, 2, 0, 2, 2, 0, 0, 0, 3, 0, 0, 3, 3, 0, 4, 1, 0 };
		testInv = new ArrayList<Integer>();
		for(int intValue : intArray)
			testInv.add(intValue);
		
		boolean theQuestion = InvariantsTools.checkInvariant(CMatrix, testInv, true);
		*/
		
		// Etap I - miejsca 1-in 1-out
		ArrayList<ArrayList<Integer>> generatedRows = new ArrayList<ArrayList<Integer>>();
		log("Phase I inititated. Performing only for all 1-in/1-out places.","text", false);
		int placesNumber = globalIncidenceMatrix.get(0).size();
		
		for (int p = 0; p < placesNumber; p++) {
			if (isSimplePlace(globalIncidenceMatrix, p) == true) { // wystepuje tylko jedno wejście i wyjście z miejsca
				
				generatedRows = findNewRows(p); // na bazie globalIncidenceMatrix i Identity
				rewriteIncidenceIntegrityMatrices_MR(generatedRows, p);
				zeroColumnVectorP.add(p);
				generatedRows.clear(); //wyczyść macierz przekształceń
			} else {
				nonZeroColumnVectorP.add(p);
			}
		}

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
			
			generatedRows = findNewRows(cand); // na bazie globalIncidenceMatrix i Identity
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
			
			ArrayList<ArrayList<Integer>> problemSet = new ArrayList<ArrayList<Integer>>();
			for(int el : res.get(1)) {
				problemSet.add(globalIdentityMatrix.get(el));
			}
			
			ArrayList<ArrayList<Integer>> noInvariants = InvariantsTools.isTInvariantSet(CMatrix, problemSet);
			
			ArrayList<ArrayList<Integer>> finalInv = new ArrayList<ArrayList<Integer>>();
			for(int i=0; i<res.get(0).size(); i++) {
				ArrayList<Integer> inv = new ArrayList<Integer>(globalIdentityMatrix.get(res.get(0).get(i)));
				finalInv.add(inv);
			}
			setInvariants(finalInv);
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
	 * liniowe dodadzą najmniej nowych wierszy do macierzy przekształceń.<br>
	 * Poprawka: jeśli natrafi na pierwszą kolumnę, która zmniejszy liczbe wierszy, od razu ją wybiera (cite[1])
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
		int nonZeroColSize = nonZeroColumnVectorP.size();
		
		for(int i=0; i<nonZeroColSize; i++) {
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
			int growthFactor = (posCounter*negCounter)-(posCounter+negCounter);
			if(growthFactor < 0) {
				int[] res = new int[4];
				res[0] = p;
				res[1] = growthFactor;
				res[2] = posCounter;
				res[3] = negCounter;
				return res;
			}
			
			numberOfNewRows.add(growthFactor);
			positives.add(posCounter);
			negatives.add(negCounter);
		}
		
		int cand = nonZeroColumnVectorP.get(0);
		int nOfRows = numberOfNewRows.get(0);
		int poss = positives.get(0);
		int negg = negatives.get(0);
		
		for(int i=0; i<nonZeroColSize; i++) {
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
	private ArrayList<ArrayList<Integer>> findNewRows(int placeIndex) {
		int sizeM = globalIncidenceMatrix.size();
		ArrayList<ArrayList<Integer>> newRows = new ArrayList<ArrayList<Integer>>();
		for (int t1 = 0; t1 < sizeM; t1++) {
			int val1 = globalIncidenceMatrix.get(t1).get(placeIndex);
			if (val1 == 0) 
				continue;
			
			for (int t2 = t1; t2 < sizeM; t2++) {
				int val2 = globalIncidenceMatrix.get(t2).get(placeIndex);
				if (val2 == 0) 
					continue;
				
				if (t2 != t1) { //hmmm, to po co na górze t2=t1???
					if ((val1 > 0 && val2 < 0) || (val1 < 0 && val2 > 0)) {
						int l1 = bezwzgledna(val1);
						int l2 = bezwzgledna(val2);
						
						int nww = (l1 * l2) / nwd(l1, l2); //najmniejsza wspólna wielokrotność
						ArrayList<Integer> rowsTransformation = new ArrayList<Integer>();
						rowsTransformation.add(t1);
						rowsTransformation.add(t2);
						rowsTransformation.add(nww / l1);
						rowsTransformation.add(nww / l2);
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
		return newRows;
	}
	
	/**
	 * Metoda dodaje nowo wygenerowane wiersze do macierzy. Sprawdza jednak przed dodaniem, czy nowy
	 * wiersz jest minimalnym inwariantem.
	 * @param newRowsMatrix ArrayList[ArrayList[Integer]] - macierz przekształcen liniowych
	 * @param rowNumber int - numer wygenerowanego przekształcenia do dodania
	 */
	private void addNewRowsToMatrix(ArrayList<ArrayList<Integer>> newRowsMatrix, int rowNumber) {
		ArrayList<Integer> incMatrixNewRow = new ArrayList<Integer>();
		ArrayList<Integer> newRowData = newRowsMatrix.get(rowNumber);
		int t1 = newRowData.get(0);
		int t2 = newRowData.get(1);
		int multFactorT1 = newRowData.get(2);
		int multFactorT2 = newRowData.get(3);
		
		int incMSize = globalIncidenceMatrix.get(0).size();
		for (int b = 0; b < incMSize; b++) {
			incMatrixNewRow.add((globalIncidenceMatrix.get(t1).get(b) * multFactorT1) + 
					(globalIncidenceMatrix.get(t2).get(b) * multFactorT2));
		}
		
		int identMSize = globalIdentityMatrix.get(0).size();
		ArrayList<Integer> invCandidate = new ArrayList<Integer>();
		for (int b = 0; b < identMSize; b++) {	
			invCandidate.add((globalIdentityMatrix.get(t1).get(b) * multFactorT1) + 
					(globalIdentityMatrix.get(t2).get(b) * multFactorT2));
		}
		
		//globalIncidenceMatrix.add(incMatrixNewRow);
		//globalIdentityMatrix.add(invCandidate);
		
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
		ArrayList<Integer> candidateSupport = InvariantsTools.getSupport(invCandidate);
		
		int canonicalNWD = checkCanonityNWD(invCandidate, candidateSupport);
		if(canonicalNWD > 1) { //Burn the heretic. Kill the mutant. Purge the unclean.
			notCanonical++;
			canonize(invCandidate, canonicalNWD);
			
			ArrayList<Integer> matrixSupport = InvariantsTools.getSupport(incMatrixNewRow);
			if(matrixSupport.size() > 0) {
				int matrixNWD = checkCanonityNWD(incMatrixNewRow, matrixSupport);
				if(matrixNWD > 1) {
					
					if(nwd(canonicalNWD, matrixNWD) != canonicalNWD) {
						@SuppressWarnings("unused")
						int check=1; //athero: ani razu! Hurray!!!!
					}
					canonize(incMatrixNewRow, canonicalNWD);
					//canonize(incMatrixNewRow, matrixNWD); //TEST: BAD IDEA: po prostu nie działa, powstają nie-inwarianty, a inwariantów jest za mało
				} else {
					@SuppressWarnings("unused") 
					int check=1; //tutaj też ani razu!!!! :D
				}
			}
		}
//TODO:
		//boolean fmtResult = fastMinimalityTest(invCandidate, candidateSupport);
		//boolean fmtResult = true;
		
		boolean fmtResult = true; //nadmiarowy
		if(candidateSupport.size() > zeroColumnVectorP.size()+2)
			fmtResult = false;
		
		if(fmtResult == true) { //jak obleje, to w ogóle nie kombinujemy dalej - nie będzie dodany
			//ArrayList<Integer> resList = supportMinimalityTest(invCandidate, candidateSupport, t1, t2);
			ArrayList<Integer> resList = supportMinimalityTestNew(invCandidate, candidateSupport, t1, t2);
			boolean added = false;
			if(resList.get(0) == -1) { //jest minimalny
				globalIncidenceMatrixNewRows.add(incMatrixNewRow);
				globalIncidenceMatrix.add(incMatrixNewRow);
				globalIdentityMatrixNewRows.add(invCandidate);
				globalIdentityMatrix.add(invCandidate);
				added = true;
			}
			
			resList.remove(0); // -1 lub -99
			
			if(resList.size() > 1) {
				if(added == false) {
					System.out.println("Inwariant się nie nadaje, ale jest mniejszy niż obecne w macierzy! ERROR!");		
				}
				
				while(resList.size()>0) { //inne do usunięcia
					int remCandidate = resList.get(0);
					if(removalList.contains(remCandidate) == false) {
						removalList.add(remCandidate);
					}
					resList.remove(0);
				}
			}
		} else {
			newRejected++;
		}
	}
	
	/**
	 * Metoda sprawdza czy inwariant jest minimalny.
	 * @param invCandidate ArrayList[Integer] - inwariant z kombinacji liniowej
	 * @param invSupport ArrayList[Integer] - wsparcie inwariantu
	 * @param t1 int - wektor 1 składowy
	 * @param t2 int - wektor 2 składowy
	 * @return ArrayList[Integer] - pierwszy element mówi czy jest minimalny czy nie, kolejne to nr wektorów do usunięcia
	 *  	z macierzy przekształceń
	 */
	private ArrayList<Integer> supportMinimalityTestNew(ArrayList<Integer> invCandidate, ArrayList<Integer> invSupport, int t1, int t2) {
		ArrayList<Integer> removeList = new ArrayList<Integer>();
		removeList.add(-1); //domyślnie można dodać nowy inw
		
		int matrixSize = globalIdentityMatrix.size();
		int invSuppSize = invSupport.size();
		int successCounter = 0;
		
		for(int vector=0; vector<matrixSize; vector++) {
			if(vector == t1 || vector == t2) //nie testujemy ze składowymi
				continue;

			ArrayList<Integer> refSupport = InvariantsTools.getSupport(globalIdentityMatrix.get(vector));
			int result = checkCoverability(globalIdentityMatrix.get(vector), refSupport, invCandidate, invSupport);
			//0 - identyczne lub nie jest minimalny, nie dodajemy 
			//1 - inne powody, nie dodajemy 
			//2 - dodajemy, ale trzeba usunąć jakiegoś z macierzy 
			//3 - dodajemy, po prostu
			
			if(result == 0 || result == -1) { //identyczny jak znaleziony w macierzy lub referencyjny jest w nim zawarty: nie dodajemy
				newRejected++;
				removeList.set(0, -99);
				
				if(removeList.size() > 1) {
					@SuppressWarnings("unused")
					int x=1;
					//JAKIM CUDEM? zupełnie nie pasuje, ale jest mniejszy od znalezionego w macierzy?!
				}

				return removeList; //TODO: a co gdyby kontynuować szukanie dalej?
				
			} else if(result == 2) { //jest lepszy od testowanego referencyjnego - referencyjny do wywalenia
				successCounter++;
				oldReplaced ++;		//mamy kandydata do usunięcia z macierzy i zastąpienia aktualnym! zapamiętać vector
				if(removeList.contains(vector) == false) {
					removeList.add(vector);
				}
			} else if(result == 3) { //ok, niezależne
				successCounter++;
			}
		}
			
		if(successCounter == matrixSize - 2)
			return removeList; //dodajemy, ani razy nie było 0 lub -1
		else {
			return removeList; //WTF! nie powinno być możliwe!
		}
	}
	
	/**
	 * Metoda sprawdza relację pomiędzy dwoma inwariantami.
	 * @param refInv ArrayList[Integer] - inwariant referencyjny z macierzy przekształceń
	 * @param refSupport ArrayList[Integer] - wsparcie inwariantu referencyjnego
	 * @param candidateInv ArrayList[Integer] - inwariant utworzony z kombinacji liniowej
	 * @param candSupport ArrayList[Integer] - wsparcie inwariantu - kandydata
	 * @return int - informacja co dalej: <br>
	 * 	0 - identyczne lub nie jest minimalny, nie dodajemy <br>
	 *  1 - inne powody, nie dodajemy <br>
	 *  2 - dodajemy, ale trzeba usunąć jakiegoś z macierzy <br>
	 *  3 - dodajemy, po prostu
	 */
	private int checkCoverability(ArrayList<Integer> refInv, ArrayList<Integer> refSupport, ArrayList<Integer> candidateInv,
			ArrayList<Integer> candSupport) {
		int sizeRef = refInv.size();
		int sizeCan = candidateInv.size();
		if(sizeRef != sizeCan)
			return 1; // z czym do ludzi?
		
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
				// wsparcia się rozmijają w n-wymiarowej przestrzeni - niezależne
				return 3; // można dodać
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
			return 0; // kandydat nie jest minimalny: referencyjny się w nim zawiera
			/* Referencyjny wektor zawiera się w kandydacie na każdym elemencie wsparcia kandydata (RefInCan == candSuppSize) oraz
			 * choć na jednym elemencie jest mniejszy (a nie tylko mniejszy/równy, tj: RefInCanStrong > 0). Gdyby było inaczej to
			 * byłby identyczny (warunek wyżej), lub w ogóle niezależny (CanInRefStrong > 0 && RefInCanStrong > 0 w pętli)
			 */
		}
		
		if(CanInRef == refSuppSize && CanInRefStrong > 0) {
			return 2; // kandydat jest mniejszy niż wektor referencyjny który testujemy
			/* Kandydat zawiera się w referencyjnym na każdym elemencie wsparcia elementu referencyjnego (CanInRef == refSuppSize) oraz
			 * na jakimś elemencie jest mniejszy, a nie tylko mniejszy/równy (CanInRefStrong > 0).
			 */
		} else {
			System.out.println("checkCoverability: Niemożliwy stan został osiągnięty. Konkluzja: znajdź sobie inny zbiór inwariantów niż ten.");
			return 3; //teoretycznie NIGDY nie powinniśmy się tu pojawić
		}
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
			for (int row = 0; row < size; row++) {	
				addNewRowsToMatrix(newRowsMatrix, row);
				
				if(size > 1000) {
					if(justGotHere) {
						System.out.println();
						justGotHere = false;
					}
					if(steps == (int)interval) {
						steps = 0;
						System.out.print("*");
					} else {
						steps++;
					}
				}
			}
			
			/*
			for(int i=0; i<globalIdentityMatrix.size(); i++) {
				ArrayList<Integer> test = globalIdentityMatrix.get(i);
				if(testInv.equals(test)) {
					@SuppressWarnings("unused")
					int WUYGUDHGI = 1111;
				}
			}*/
			
			//tutaj usuwanie U+ i U-
			int removedSoFar = 0;
			Collections.sort(removalList);
			if(removalList.size()>1) {
				int x=1;
			}
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
			
			aac++;
		} else {
			//nothing to add;
			naac++;
			//System.out.println("INCIDENCE MATRIX, column: "+placeIndex);
			//printMatrix(globalIncidenceMatrix);
			//System.out.println("");
		}
	}

	private void canonize(ArrayList<Integer> invCandidate, int nwd) {
		for(int i=0; i<invCandidate.size(); i++) {
			invCandidate.set(i, invCandidate.get(i)/nwd);
		}
	}

	/**
	 * Metoda zwraca największy wspólny dzielnik elementów wsparcia wektora wejściowego. Jeśli >1, to znaczy
	 * że wektor nie jest kanoniczny.
	 * @param invCandidate ArrayList[Integer] - wektor danych
	 * @param supports ArrayList[Integer] - wsparcie wektora (niezerowe elementy)
	 * @return int - jeśli != 1, wektor nie jest kanoniczny
	 */
	private int checkCanonityNWD(ArrayList<Integer> invCandidate, ArrayList<Integer> supports) {
		int result =  bezwzgledna(invCandidate.get(supports.get(0)));
		for(int number: supports) {
			int value = bezwzgledna(invCandidate.get(number));
	        result = nwd(result, value);
		}
		return result;
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
		ArrayList<Integer> inaNotInOur = new ArrayList<Integer>(); //są w INA, nie ma u nas
		
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
				if(nonInINA.contains(invMy)) {
					repeatedNotFound++;
				} else {
					nonInINA.add(invMy);
				}
			}
		}
		
		for(int i=0; i<INAInvSize; i++) {
			if(inaFoundInOur.contains(i) == false) {
				inaNotInOur.add(i);
			}
		}
		ArrayList<Integer> repeatedVector = new ArrayList<Integer>();
		repeatedVector.add(repeated);
		repeatedVector.add(repeatedNotFound);
		
		result.add(ourFoundInINA); //część wspolna
		result.add(nonInINA); //nasze, ale nie ma INA
		result.add(inaNotInOur); //inwarianty INY których nie mamy
		result.add(repeatedVector);
		return result;
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
			ArrayList<Integer> supports = InvariantsTools.getSupport(inv);
			boolean minimal = true;
			for(int t2=0; t2<size; t2++) {
				if(t1 != t2) {
					
				}
			}
		}
	}
	
	

	private void printMatrix(ArrayList<ArrayList<Integer>> matrix) {
		String colNames = "";
		for (int jo = 0; jo < matrix.get(0).size(); jo++) {
			colNames = colNames + Tools.setToSize(jo+"", 3, true);
		}
		System.out.println(colNames);
		for (int it = 0; it < matrix.size(); it++) {
			String msg = "";
			msg = msg +  Tools.setToSize("t"+it, 4, true);
			for (int jo = 0; jo < matrix.get(0).size(); jo++) {
				//System.out.print(incMatrix.get(it).get(jo) + " ");
				msg = msg + Tools.setToSize(matrix.get(it).get(jo)+"", 3, true);
			}
			//log(msg, "text", true);
			System.out.println(msg);
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
	
	
	
	private int test1get() {
		if(testList.size() > 0) {
			int val = testList.get(0);
			int index = testList.indexOf(val);
			testList.remove(index);
			return val;
		} else 
		return 0;
	}

	private void test1Prepare() {
		testList.add(0);
		testList.add(1);
		testList.add(2);
		
		testList.add(4);
		testList.add(5);
		testList.add(6);
		testList.add(7);
		testList.add(8);
		testList.add(9);
		testList.add(10);
		testList.add(11);
		testList.add(12);
		testList.add(13);
		testList.add(14);
		testList.add(15);
		
		testList.add(17);
		testList.add(18);
		testList.add(19);
		testList.add(20);
		testList.add(21);
		testList.add(22);
		testList.add(23);
		testList.add(24);
		testList.add(25);
		testList.add(26);
		testList.add(27);
		testList.add(28);
		testList.add(29);
		testList.add(30);
		testList.add(31);
		testList.add(32);
		testList.add(33);
		testList.add(34);
		testList.add(35);
		testList.add(36);
		testList.add(37);
		testList.add(38);
		
		testList.add(40);
		testList.add(41);
		testList.add(42);
		testList.add(43);
		testList.add(44);
		testList.add(45);
		testList.add(46);
		testList.add(47);
		testList.add(48);
		testList.add(49);
		
		testList.add(51);
		testList.add(52);
		testList.add(53);
		testList.add(54);
		testList.add(55);
		testList.add(56);
		testList.add(57);
		testList.add(58);
		testList.add(59);
		testList.add(60);
		testList.add(61);
		
		testList.add(50);
		testList.add(3);
		testList.add(62);
		testList.add(16);
	}
}