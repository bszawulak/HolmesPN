package holmes.analyse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypesOfArcs;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.varia.Check;
import holmes.windows.HolmesInvariants;

/**
 * 10.06.2014: Metoda stara się liczyć inwarianty. Coś nie wyszło i to ostro...<br>
 * 11.03.2015: Czas naprawy. MR <br>
 * 13.03.2015: Naprawianie w toku. Tu się takie cuda działy, że głowa mała. Poprzednia wersja miała wymagania
 * pamięciowe serwerowni PCSS. Aż cud, że była w stanie cokolwiek liczyć. O błędach lepiej nie pisać. <br>
 * 14.03.2015: Pierwszy sukces, coś działa. Poprawienie macierzy incydencji (1 na -1) w każdym polu PEWNIE TEŻ POMOGŁO!!! 
 * 17.03.2015: Pełen sukces. Działa i wiadomo dlaczego. Cud.
 * 
 * @author BR (nic nie zostało prawie, i dobrze)
 * @author MR
 */
public class InvariantsCalculator implements Runnable {
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<ArrayList<Integer>> t_invariantsList = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> p_invariantsList = new ArrayList<ArrayList<Integer>>();

	private ArrayList<ArrayList<Integer>> globalIncidenceMatrix; //aktualna macierz przekształceń liniowych
	private ArrayList<Integer> GLOBAL_INC_VECTOR;
	private int INC_MATRIX_ROW_SIZE;
	private int IDENT_MATRIX_ROW_SIZE;
	private ArrayList<ArrayList<Integer>> globalIdentityMatrix; //macierz przekształacna w inwarianty
	private ArrayList<ArrayList<Integer>> CMatrix; //oryginalna macierz incydencji sieci
	private ArrayList<Integer> removalList;
	
	private ArrayList<Integer> zeroColumnVector;
	private ArrayList<Integer> nonZeroColumnVector;
	private ArrayList<ArrayList<Integer>> doubleArcs;
	
	private boolean transCalculation = true;
	@SuppressWarnings("unused")
	private int aac = 0;
	@SuppressWarnings("unused")
	private int naac = 0;
	
	private int newRejected = 0;
	private int oldReplaced = 0;
	private int notCanonical = 0;
	
	private HolmesInvariants masterWindow = null;
	
	/**
	 * Konstruktor obiektu klasy InvariantsCalculator. Zapewnia dostęp do miejsc, tranzycji i łuków sieci.
	 * @param transCal boolean - true, jeśli liczymy T-inwarianty, false dla P-inwariantów
	 */
	public InvariantsCalculator(boolean transCal) {
		masterWindow = GUIManager.getDefaultGUIManager().accessInvariantsWindow();
		
		transCalculation = transCal;
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		
		zeroColumnVector = new ArrayList<Integer>();
		nonZeroColumnVector = new ArrayList<Integer>();
	}
	
	/**
	 * Metoda wirtualna - nadpisana, odpowiada za działanie w niezależnym wątku
	 */
	@SuppressWarnings("unused")
	public void run() {
		try {
			logInternal("Invariant calculations started.\n", true);
			
			if(transCalculation == true) {
				this.createTPIncidenceAndIdentityMatrix(false);
				this.calculateInvariants();
				
				PetriNet project = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
				GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(getInvariants(true));
				project.setINVmatrix(getInvariants(true), true);
				GUIManager.getDefaultGUIManager().reset.setInvariantsStatus(true);
				GUIManager.getDefaultGUIManager().accessNetTablesWindow().resetInvData();
				logInternal("Operation successfull, invariants found: "+getInvariants(true).size()+"\n", true);
				
				ArrayList<Integer> arcClasses =  Check.getArcClassCount();
				if(arcClasses.get(1) > 0) {
					logInternal("\n", false);
					logInternal("Read-arcs detected. There are "+(arcClasses.get(1) / 2)+" read-arcs in net.\n", false);
					logInternal("Feasible invariants computation/check is recommended. \n", false);
				}
				
				if(doubleArcs.size() > 0) {
					logInternal("\n", false);
					logInternal("WARNING! Double arcs (read-arcs) detected between nodes::\n", false);
					for(ArrayList<Integer> trouble : doubleArcs) {
						logInternal("Place: p_"+trouble.get(0)+" and Transition t_"+trouble.get(1)+"\n", false);
					}
					//logInternal("\n", false);
					logInternal("Feasible invariants computation/check is recommended - "
							+ "depending on the set it may or may not change during this procedure.\n", false);
				}
			} else { //P-invariants
				this.createPTIncidenceAndIdentityMatrix(false);
				this.calculateInvariants();
				
				ArrayList<ArrayList<Integer>> pInv = getInvariants(false);
				
				int x =1;
				
			}
		} catch (Exception e) {
			log("Critical error while generating invariants. Possible net state change.", "error", false);
			logInternal("Critical error while generating invariants. Possible net state change.\n", true);
		} finally {
			masterWindow.resetInvariantGenerator(); //odłącz obiekt
		}
	}
	
	/**
	 * Metoda zwracająca macierz incydencji sieci.
	 * @return ArrayList[ArrayList[Integer]] - macierz incydencji
	 */
	public ArrayList<ArrayList<Integer>> getCMatrix() {
		this.createTPIncidenceAndIdentityMatrix(true);
		return CMatrix;
	}
	

	/**
	 * Metoda tworząca macierze: incydencji i jednostkową dla modelu szukania T-inwariantów
	 * (TP-macierz z literatury).
	 * @param silence boolean - true, jeśli nie ma wypisywać komunikatów
	 */
	public void createTPIncidenceAndIdentityMatrix(boolean silence) {
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
		doubleArcs = new ArrayList<ArrayList<Integer>>();

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
			
			if(oneArc.getArcType() != TypesOfArcs.NORMAL) {
				continue;
			}

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidenceValue = 1 * oneArc.getWeight();
				
				Transition tr = (Transition) oneArc.getStartNode();
				Place pl = (Place) oneArc.getEndNode();
				int tr_pos = transitions.indexOf(tr);
				int pl_pos = places.indexOf(pl);
				if(tPosition != tr_pos || pPosition != pl_pos) {
					@SuppressWarnings("unused")
					int x=1;
				}
				
				// incidenceValue = -1 * oneArc.getWeight();  // CO TO K**** JEST ?! JAKIE -1 ?!
				// 	(14.03.2015) ja go chyba zamorduję... 
				//   https://www.youtube.com/watch?v=oxiJrcFo724
				// 
			} else { //miejsca
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				incidenceValue = -1 * oneArc.getWeight();
				
				Transition tr = (Transition) oneArc.getEndNode();
				Place pl = (Place) oneArc.getStartNode();
				int tr_pos = transitions.indexOf(tr);
				int pl_pos = places.indexOf(pl);
				if(tPosition != tr_pos || pPosition != pl_pos) {
					@SuppressWarnings("unused")
					int x=1;
				}
			}
			int oldValue = globalIncidenceMatrix.get(tPosition).get(pPosition);
			if(oldValue != 0) { //detekcja łuków podwójnych
				ArrayList<Integer> hiddenReadArc = new ArrayList<Integer>();
				hiddenReadArc.add(pPosition);
				hiddenReadArc.add(tPosition);
				doubleArcs.add(hiddenReadArc);
			}
			
			globalIncidenceMatrix.get(tPosition).set(pPosition, oldValue+incidenceValue);
			CMatrix.get(tPosition).set(pPosition, oldValue+incidenceValue);
		}
		
		if(!silence)
			logInternal("\nTP-class incidence matrix created for "+transitions.size()+" transitions and "+places.size()+" places.\n", false);
		
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
		
		INC_MATRIX_ROW_SIZE = globalIncidenceMatrix.get(0).size();
		IDENT_MATRIX_ROW_SIZE = transitions.size();
		GLOBAL_INC_VECTOR = new ArrayList<Integer>();
		for (int i = 0; i < INC_MATRIX_ROW_SIZE; i++)
			GLOBAL_INC_VECTOR.add(0);
		
		
		if(!silence)
			logInternal("Identity matrix created for "+transitions.size()+" transitions.\n", false);
	}
	
	/**
	 * Metoda tworząca macierze: incydencji i jednostkową dla modelu szukania P-inwariantów
	 * (PT-macierz z literatury).
	 * @param silence boolean - true, jeśli nie ma wypisywać komunikatów
	 */
	public void createPTIncidenceAndIdentityMatrix(boolean silence) {
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
		CMatrix = new ArrayList<ArrayList<Integer>>();
		removalList = new ArrayList<Integer>();
		doubleArcs = new ArrayList<ArrayList<Integer>>();
		
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
			
			if(oneArc.getArcType() != TypesOfArcs.NORMAL) {
				continue;
			}

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidenceValue = 1 * oneArc.getWeight();
			} else { //miejsca
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				incidenceValue = -1 * oneArc.getWeight();
			}
			int oldValue = globalIncidenceMatrix.get(tPosition).get(pPosition);
			if(oldValue != 0) { //detekcja łuków podwójnych
				ArrayList<Integer> hiddenReadArc = new ArrayList<Integer>();
				hiddenReadArc.add(pPosition);
				hiddenReadArc.add(tPosition);
				doubleArcs.add(hiddenReadArc);
			}
			
			globalIncidenceMatrix.get(tPosition).set(pPosition, oldValue+incidenceValue);
			CMatrix.get(tPosition).set(pPosition, oldValue+incidenceValue);
		}
		
		globalIncidenceMatrix = InvariantsTools.transposeMatrix(globalIncidenceMatrix);
		CMatrix = InvariantsTools.transposeMatrix(CMatrix);
		
		if(!silence)
			logInternal("\nPT-class incidence matrix created for "+transitions.size()+" transitions and "+places.size()+" places.\n", false);
		
		//globalIdentityMatrix = InvariantsTools.transposeMatrix(globalIdentityMatrix);
		//macierz jednostkowa
		
		globalIdentityMatrix = new ArrayList<ArrayList<Integer>>();

		for (int p = 0; p < places.size(); p++) {
			ArrayList<Integer> identRow = new ArrayList<Integer>();
			for (int p2 = 0; p2 < places.size(); p2++) {
				if (p == p2) 
					identRow.add(1);
				else
					identRow.add(0);
			}
			globalIdentityMatrix.add(identRow);
		}

		INC_MATRIX_ROW_SIZE = globalIncidenceMatrix.get(0).size(); //liczba miejsc dla liczenia t-inv, lub
		//liczba tranzycji dla liczenia p-inv
		IDENT_MATRIX_ROW_SIZE = places.size();
		GLOBAL_INC_VECTOR = new ArrayList<Integer>();
		for (int i = 0; i < INC_MATRIX_ROW_SIZE; i++)
			GLOBAL_INC_VECTOR.add(0);
		
		
		if(!silence)
			logInternal("Identity matrix created for "+places.size()+" places.\n", false);
	}
	
	/**
	 * Główna metoda klasy odpowiedzialna za wyszukiwanie inwariantów.
	 */
	public void calculateInvariants() {
		// Etap I - miejsca 1-in 1-out
		ArrayList<ArrayList<Integer>> generatedRows = new ArrayList<ArrayList<Integer>>();
		logInternal("Phase I inititated. Performing only for all 1-in/1-out columns.\n", false);
		int columnsNumber = globalIncidenceMatrix.get(0).size();
		
		for (int p = 0; p < columnsNumber; p++) {
			nonZeroColumnVector.add(p);
		}
		
		for (int col = 0; col < columnsNumber; col++) { //col = miejsce dla T-inv, tranz. dla P-inv
			if (isSimpleColumn(globalIncidenceMatrix, col) == true) { // wystepuje tylko jedno wejście i wyjście
				logInternal("Processing simple-class column: "+col+"\n", false);
				generatedRows = findNewRows(col); // na bazie globalIncidenceMatrix i Identity
				rewriteIncidenceIntegrityMatrices(generatedRows, col);
				zeroColumnVector.add(col);
				int vIndex = nonZeroColumnVector.indexOf(col);
				nonZeroColumnVector.remove(vIndex);
			}
		}

		// Etap II - cała reszta
		while(nonZeroColumnVector.size() != 0) {
			//generatedRows.clear(); //wyczyść macierz przekształceń
			int stepsToFinish = nonZeroColumnVector.size();
			int[] res = chooseNextColumn();
			int cand = res[0];
			int rowsChange = res[1];
			int oldSize = globalIncidenceMatrix.size();
			
			logInternal("Processing column: "+cand+", projected rows change: "+(oldSize+rowsChange)+", remaining steps: "+stepsToFinish, false);
			
			generatedRows = findNewRows(cand); // na bazie globalIncidenceMatrix i Identity
			rewriteIncidenceIntegrityMatrices(generatedRows, cand);
			int indCand = nonZeroColumnVector.indexOf(cand);
			nonZeroColumnVector.remove(indCand);
			zeroColumnVector.add(cand);
			
			int newSize = globalIncidenceMatrix.size();
			
			logInternal("\nNew rows number: "+newSize+ " | rejected: "+newRejected+" replaced: "+oldReplaced+" not canonical: "+notCanonical+"\n", false);
		}
		
		setInvariants(globalIdentityMatrix);
	}

	/**
	 * Metoda pomocnicza I fazy obliczeń: sprawdza, czy dana miejsce ma tylko 1 tranzycję wejściową
	 * i tylko 1 wyjściową w danej kolumnie (albo czy w ogóle ma) - dla obliczenia t-inv.<br>
	 * Z kolei dla obliczeń p-inv, kolumny to tranzycje, tak więc sprawdza kombinacje po miejsach (wierszach)
	 * @param incidenceMatrix ArrayList[ArrayList[Integer]] - macierz incydencji
	 * @param column int - indeks kolumny
	 * @return boolean - true, jeśli dana kolumna ma tylko 1 wiersz '+' i 1 wiersz '-' - tj. nawzajem się znoszą
	 */
	private boolean isSimpleColumn(ArrayList<ArrayList<Integer>> incidenceMatrix, int column) {
		int input = 0;
		int output = 0;
		for (int row = 0; row < incidenceMatrix.size(); row++) {
			//dla t-inv row to tranzycje, dla p-inv row to odpowiednie miejsce
			if (incidenceMatrix.get(row).get(column) > 0)
				input++;
			
			if (incidenceMatrix.get(row).get(column) < 0)
				output++;
			
			if(input == 2 || output == 2)
				return false;
		}
		
		if(input == 0 && output == 0) { 
			//cykl T1->P1->T2->P2->T1 (zamiast łuku odczytu) powoduje takie cuda w tej fazie - następuje zerowanie 2 kolumn
			//za pomocą jednego przekształcenia liniowego (opis dla t-inv)
			return true;
		}
		
		if (input == 1 && output == 1)
			return true; //prosta kolumna
		else
			return false; //więcej niż 1 wiersz + i -
	}

	/**
	 * Metoda odpowiedzialna za wybór kolumny dla której powstanie najmniej nowych wierszy. Działa w II fazie
	 * pracy algorytmu. Podaje pierszy indeks kolumny z najmniejszą wartością [1] - tj. kolumnę dla której przekształcenia
	 * liniowe dodadzą najmniej nowych wierszy do macierzy przekształceń.<br>
	 * Poprawka: jeśli natrafi na pierwszą kolumnę, która zmniejszy liczbę wierszy, od razu ją wybiera (cite[1])
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
		int nonZeroColSize = nonZeroColumnVector.size();
		
		for(int i=0; i<nonZeroColSize; i++) {
			int col = nonZeroColumnVector.get(i);
			posCounter = 0;
			negCounter = 0;
			for(int row=0; row<rowSize; row++) {//t-inv: tranzycje, p-inv: miejsca
				if(globalIncidenceMatrix.get(row).get(col) > 0) {
					posCounter++;
				}
				if(globalIncidenceMatrix.get(row).get(col) < 0) {
					negCounter++;
				}
			}
			int growthFactor = (posCounter*negCounter)-(posCounter+negCounter);
			if(growthFactor < 0) {
				int[] res = new int[4];
				res[0] = col;
				res[1] = growthFactor;
				res[2] = posCounter;
				res[3] = negCounter;
				return res;
			}
			
			numberOfNewRows.add(growthFactor);
			positives.add(posCounter);
			negatives.add(negCounter);
		}
		
		int cand = nonZeroColumnVector.get(0);
		int nOfRows = numberOfNewRows.get(0);
		int poss = positives.get(0);
		int negg = negatives.get(0);
		
		for(int i=0; i<nonZeroColSize; i++) {
			if(numberOfNewRows.get(i) < nOfRows) {
				nOfRows = numberOfNewRows.get(i);
				cand = nonZeroColumnVector.get(i);
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
	 * @param columnIndex int - dla t-inv jest to numer miejsca do przetworzenia dla wszystkich tranzycji, dla
	 *  p-inv jest to numer tranzycji (kolumny) do przetworzenia po wszystkich miejsach (wierszach)
	 * @return ArrayList[ArrayList[Integer]] - macierz przekształceń liniowych par wektorów
	 */
	private ArrayList<ArrayList<Integer>> findNewRows(int columnIndex) {
		int sizeM = globalIncidenceMatrix.size();
		ArrayList<ArrayList<Integer>> newRows = new ArrayList<ArrayList<Integer>>();
		for (int row1 = 0; row1 < sizeM; row1++) {
			int val1 = globalIncidenceMatrix.get(row1).get(columnIndex);
			if (val1 == 0) 
				continue;
			
			int l1 = bezwzgledna(val1);
			
			for (int row2 = row1; row2 < sizeM; row2++) {
				int val2 = globalIncidenceMatrix.get(row2).get(columnIndex);
				if (val2 == 0) 
					continue;
				
				if (row2 != row1) { //hmmm, to po co na górze t2=t1???
					if ((val1 > 0 && val2 < 0) || (val1 < 0 && val2 > 0)) {
						
						int l2 = bezwzgledna(val2);
						
						int nww = (l1 * l2) / nwd(l1, l2); //najmniejsza wspólna wielokrotność
						ArrayList<Integer> rowsTransformation = new ArrayList<Integer>();
						rowsTransformation.add(row1);
						rowsTransformation.add(row2);
						rowsTransformation.add(nww / l1);
						rowsTransformation.add(nww / l2);
						rowsTransformation.add(columnIndex);
						newRows.add(rowsTransformation);
						
						//niczego jeszcze nie usuwaj, dodaj indeksy składowych do zbioru usunięć
						if(removalList.contains(row1) == false)
							removalList.add(row1);
						if(removalList.contains(row2) == false)
							removalList.add(row2);
					}
				}
			}
		}
		return newRows;
	}
	
	/**
	 * Metoda dodaje nowo wygenerowany wiersz do macierzy. Sprawdza jednak przed dodaniem, czy nowy
	 * wiersz jest minimalnym inwariantem.
	 * @param newRowData ArrayList[Integer] - dane dla przekształcenia liniowego
	 */
	private void addNewRowsToMatrix(ArrayList<Integer> newRowData) {
		ArrayList<Integer> incMatrixNewRow = new ArrayList<Integer>(GLOBAL_INC_VECTOR);
		int row1 = newRowData.get(0);
		int row2 = newRowData.get(1);
		int multFactorT1 = newRowData.get(2);
		int multFactorT2 = newRowData.get(3);
		
		for(int validIndex : nonZeroColumnVector) {
			int value = (globalIncidenceMatrix.get(row1).get(validIndex) * multFactorT1) + 
					(globalIncidenceMatrix.get(row2).get(validIndex) * multFactorT2);
			incMatrixNewRow.set(validIndex, value);
		}

		ArrayList<Integer> invCandidate = new ArrayList<Integer>();
		for (int b = 0; b < IDENT_MATRIX_ROW_SIZE; b++) {	
			invCandidate.add((globalIdentityMatrix.get(row1).get(b) * multFactorT1) + 
					(globalIdentityMatrix.get(row2).get(b) * multFactorT2));
		}
		
		addOrNot(incMatrixNewRow, invCandidate, row1, row2);
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
					//canonize(incMatrixNewRow, matrixNWD); //TEST: BAD IDEA, po prostu nie działa, powstają nie-inwarianty, a inwariantów jest za mało
				} else {
					@SuppressWarnings("unused") 
					int check=1; //tutaj też ani razu!!!! :D
				}
			}
		}
		//TODO:
		boolean fmtResult = fastMinimalityTest(invCandidate, candidateSupport);
		
		//boolean fmtResult = true; //nadmiarowy
		//if(candidateSupport.size() > zeroColumnVector.size()+2) fmtResult = false;
		
		//if(candidateSupport.size() > aac+2)
		//	fmtResult = false;
		
		if(fmtResult == true) { //jak obleje, to w ogóle nie kombinujemy dalej - nie będzie dodany
			ArrayList<Integer> resList = supportMinimalityTest(invCandidate, candidateSupport, t1, t2);
			boolean added = false;
			if(resList.get(0) == -1) { //jest minimalny
				globalIncidenceMatrix.add(incMatrixNewRow);
				globalIdentityMatrix.add(invCandidate);
				added = true;
			}
			
			resList.remove(0); // -1 lub -99
			
			if(resList.size() > 1) {
				if(added == false) {
					GUIManager.getDefaultGUIManager().log("Critical error calculating invariants. Error 00011", "error", true);
					//System.out.println("Inwariant się nie nadaje, ale jest mniejszy niż obecne w macierzy! ERROR!");		
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
	private ArrayList<Integer> supportMinimalityTest(ArrayList<Integer> invCandidate, ArrayList<Integer> invSupport, int t1, int t2) {
		ArrayList<Integer> removeList = new ArrayList<Integer>();
		removeList.add(-1); //domyślnie można dodać nowy inw
		
		int matrixSize = globalIdentityMatrix.size();
		//int invSuppSize = invSupport.size();
		int successCounter = 0;
		
		for(int vector=0; vector<matrixSize; vector++) {
			if(vector == t1 || vector == t2) //nie testujemy ze składowymi
				continue;

			ArrayList<Integer> refSupport = InvariantsTools.getSupport(globalIdentityMatrix.get(vector));
			
			if(invSupport.equals(refSupport)) { //zależność liniowa
				newRejected++;
				removeList.set(0, -99);
				return removeList;
			}
			
			if(InvariantsTools.supportInclusionCheck(invSupport, refSupport) == true) { //zawieranie się wsparć
				newRejected++;
				removeList.set(0, -99);
				return removeList;
			}
			
			
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

				return removeList; //Q:a co gdyby kontynuować szukanie dalej? A: nic ciekawego, tylko dłużej
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
		
		//TODO: po sumie zbiorów wsparć - będzie szybciej
		ArrayList<Integer> sumOfSupport = new ArrayList<Integer>();
		for(int el : refSupport)
			sumOfSupport.add(el);
		for(int el : candSupport) {
			if(sumOfSupport.contains(el) == false)
				sumOfSupport.add(el);
		}
		
		//for(int ind=0; ind<sizeRef; ind++) { //po każdym elemencie inwariantów
		for(int ind : sumOfSupport) { //po każdym elemencie wektorów
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
	 * Metoda odpowiedzialna za dodawanie i usuwanie wierszy w wyniku wykonywania wygenerowanych przekształceń
	 * liniowych wektorów zawartych w newRowsMatrix.
	 * @param newRowsMatrix ArrayList[ArrayList[Integer]] - macierz informacji o przekształceniach
	 * @param columnIndex int - nr kolumny zerowanej, jest to miejsce dla t-inv lub tranzycja dla p-inv
	 */
	private void rewriteIncidenceIntegrityMatrices(ArrayList<ArrayList<Integer>> newRowsMatrix, int columnIndex) {
		if (newRowsMatrix.size() == 0) {
			naac++;
			return; //nothing to add;
		}
		//jeśli są nowe wiersze do dodania:
		int size = newRowsMatrix.size();
		double interval = (double)size / 50; 
		int steps = 0;
		
		if(size > 1000) {
			if(masterWindow != null) 
				masterWindow.accessLogField().append("\n");

			for(ArrayList<Integer> newRow : newRowsMatrix) { //dodawanie nowych wierszy
				addNewRowsToMatrix(newRow);
				
				if(steps == (int)interval) {
					steps = 0;
					if(masterWindow != null) 
						masterWindow.accessLogField().append("*");
				} else
					steps++;
			}
		} else {
			for(ArrayList<Integer> newRow : newRowsMatrix) { //dodawanie nowych wierszy
				addNewRowsToMatrix(newRow);
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
			if(globalIncidenceMatrix.get(row).get(columnIndex) != 0) {
				globalIncidenceMatrix.remove(row);
				globalIdentityMatrix.remove(row);
				row--;
				mSize--;
			}
		}
		
		aac++; //actively annuled columns
	}
	
	/**
	 * Szybki test minimalności.
	 * @param invCandidate ArrayList[Integer] - wektor-inwariant
	 * @param supports ArrayList[Integer] - wektor wsparcia
	 * @return boolean - true, jeśli przeszedł test
	 */
	private boolean fastMinimalityTest(ArrayList<Integer> invCandidate, ArrayList<Integer> supports) {
		int supportSize = supports.size();
		int nonZeroColumn = 0;
		int CMrowSize = CMatrix.get(0).size();
		
		for(int column=0; column<CMrowSize; column++) {//t-inv: miejsca, p-inv: tranzycje
			for(int s : supports) { //inwarianty, dla t-inv są to tranzycje, dla p-inv: miejsca
				if(CMatrix.get(s).get(column) != 0) {
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
	 * Metoda odpowiedzialna za doprowadzenie wektora do postaci kanonicznej poprzez podzielenie jego elementów
	 * przez ich największy wspólny dzielnik.
	 * @param invCandidate ArrayList[Integer] - inwariant lub inny wektor
	 * @param nwd int - największy wspólny dzielnik
	 */
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
	        
	        if(result==1) return 1;
		}
		return result;
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
	
	/**
	 * Metoda oblicza silnię. Z bliżej nieznanych powodów.
	 * @param i int - liczba wyjściowa
	 * @return int - wynik: i!
	 */
	public int silnia(int i) {
		if (i == 0)
			return 1;
		else
			return i * silnia(i - 1);
	}

	/**
	 * Metoda zwraca macierz inwariantów.
	 * @param tinv boolean - true jeśli zwracać ma t-inwarianty
	 * @return ArrayList[ArrayList[Integer]] - macierz inwariantów (wiersze)
	 * 
	 */
	public ArrayList<ArrayList<Integer>> getInvariants(boolean tinv) {
		if(tinv)
			return t_invariantsList;
		else
			return p_invariantsList;
	}

	/**
	 * Metoda ustala nową macierz inwariantów.
	 * @param invMatrix ArrayList[ArrayList[Integer]] - nowa macierz inwariantów
	 */
	public void setInvariants(ArrayList<ArrayList<Integer>> invMatrix) {
		if(transCalculation == true)
			this.t_invariantsList = invMatrix;
		else
			this.p_invariantsList = invMatrix;
	}
	
	/**
	 * Metoda zapisująca komunikaty w oknie logów.
	 * @param msg String - wiadomość
	 * @param type String - klasa wiadomości
	 * @param clean boolean - true jeśli ma być bez daty
	 */
	private void log(String msg, String type, boolean clean) {
		if(clean) {
			GUIManager.getDefaultGUIManager().log(msg, type, false);
		} else {
			GUIManager.getDefaultGUIManager().log("InvModule: "+msg, type, true);
		}
	}
	
	/**
	 * Metoda wysyłająca komunikaty do podokna logów generatora.
	 * @param msg String - tekst do logów
	 * @param date boolean - true, jeśli ma być podany czas komunikatu
	 */
	private void logInternal(String msg, boolean date) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		if(masterWindow != null) {
			if(date == false) {
				JTextArea jta = masterWindow.accessLogField();
				jta.append(msg);
				jta.setCaretPosition(jta.getDocument().getLength());
				//masterWindow.accessLogField().append(msg);
				
			} else {
				JTextArea jta = masterWindow.accessLogField();
				jta.append("["+timeStamp+"] "+msg);
				jta.setCaretPosition(jta.getDocument().getLength());
				//masterWindow.accessLogField().append("["+timeStamp+"] "+msg);
			}
		}
		
	}
}