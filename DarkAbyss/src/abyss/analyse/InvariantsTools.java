package abyss.analyse;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.math.InvariantTransition;
import abyss.math.Transition;
import abyss.utilities.Tools;

/**
 * Klasa narzędziowa, zawierająca metody (głównie statyczne) do testów związanych z inwariantami.
 * @author MR
 */
public final class InvariantsTools {
	private InvariantsTools() {} // to + final = klasa statyczna w NORMALNYM języku, jak np. C#

	/**
	 * Metoda zwraca macierz transponowaną.
	 * @param matrix ArrayList[ArrayList[Integer]] - macierz wejściowa
	 * @return ArrayList[ArrayList[Integer]] - macierz wyjściowa
	 */
	static ArrayList<ArrayList<Integer>> transposeMatrix(ArrayList<ArrayList<Integer>> matrix) {
		if(matrix == null || matrix.size() == 0)
			return null;
		
		int rows = matrix.size();
		int columns = matrix.get(0).size();
		ArrayList<ArrayList<Integer>> resultMatrix = new ArrayList<ArrayList<Integer>>();
		
		for(int c=0; c<columns; c++) {
			ArrayList<Integer> transposedRow = new ArrayList<Integer>();
			for(int r=0; r<rows; r++) {
				transposedRow.add(matrix.get(r).get(c));
			}
			resultMatrix.add(transposedRow);
		}
		
		return resultMatrix;
	}
	
	/**
	 * Metoda sprawdza czy zbiór inwariantów faktycznie w całości zeruje macierz incydencji. Zwraca macierz wektorów,
	 * które okazały się nie być inwariantami.
	 * @param CMatrix ArrayList[ArrayList[Integer]] - macierz incydencji
	 * @param invSet ArrayList[ArrayList[ArrayList]] - macierz inwariantów (nie wiadomo)
	 * @return ArrayList[ArrayList[Integer]] - null jeśli wszystkie wektory to inwarianty, zwraca w formie tej macierzy listę
	 *  wektorów, które inwariantami jednak nie są
	 */
	public static ArrayList<ArrayList<Integer>> isTInvariantSet(ArrayList<ArrayList<Integer>> CMatrix, ArrayList<ArrayList<Integer>> invSet) {
		ArrayList<ArrayList<Integer>> noInv = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> inv : invSet) {
			if(checkInvariant(CMatrix, inv, true) == false) {
				noInv.add(inv);
			}
		}
		return noInv;
	}
	
	/**
	 * Jak wyżej, ale zwraca tylko liczbę nie-inwariantów.
	 * @param CMatrix ArrayList[ArrayList[Integer]] - macierz incydencji
	 * @param invSet ArrayList[ArrayList[ArrayList]] - macierz inwariantów (nie wiadomo)
	 * @return int - ile wektorów nie jest prawidłowymi inwariantami
	 */
	public static int countNonInvariants(ArrayList<ArrayList<Integer>> CMatrix, ArrayList<ArrayList<Integer>> invSet) {
		int result = 0;
		for(ArrayList<Integer> inv : invSet) {
			if(checkInvariant(CMatrix, inv, true) == false) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * Metoda sprawdza, czy inwariant jest prawidłowy, tj. czy zeruje macierz incydencji podaną jako parametr.
	 * @param CMatrix ArrayList[ArrayList[Integer]] - macierz incydencji sieci, każdy wiersza to wektor po miejscach (kolumny)
	 * @param inv ArrayList[Integer] - inwariant
	 * @param tInv boolean - true, jeśli testujemy T-inwarianty
	 * @return boolean - true jeśli inwariant przeszedł test, false jeśli nie wyzerował macierzy
	 */
	public static boolean checkInvariant(ArrayList<ArrayList<Integer>> CMatrix, ArrayList<Integer> inv, boolean tInv) {
		//CMatrix - każdy wiersz to wektor miejsc indeksowany numerem tranzycji [2][3] - 2 tranzycja, 3 miejsce
		if(tInv == true && CMatrix.size() > 0) {
			ArrayList<Integer> invSupport = getSupport(inv);
			ArrayList<Integer> placesSumVector = new ArrayList<Integer>();
			if(inv.size() != CMatrix.size())
				return false;
			
			int placesNumber = CMatrix.get(0).size();
			for(int i=0; i<placesNumber; i++) {
				placesSumVector.add(0);
			}
			
			for(int sup : invSupport) { //dla wszystkich wektorów CMatrix ze wsparcia inwariantu
				ArrayList<Integer> row = CMatrix.get(sup);
				int multFactor = inv.get(sup);
				
				for(int p=0; p<placesNumber; p++) {
					int oldVal = placesSumVector.get(p);
					oldVal += row.get(p) * multFactor;
					placesSumVector.set(p, oldVal);
				}
			}
			
			for(int p=0; p<placesNumber; p++) {
				if(placesSumVector.get(p) != 0)
					return false;
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Metoda zwraca wsparcie wektora wejściowego - numery pozycji na których w wektorze
	 * znajdują się wartości dodatnie.
	 * @param inv ArrayList[Integer] - wektor wejściowy
	 * @return ArrayList[Integer] - wsparcie
	 */
	public static ArrayList<Integer> getSupport(ArrayList<Integer> inv) {
		ArrayList<Integer> supports = new ArrayList<Integer>();
		int size = inv.size();
		for(int i=0; i<size; i++) {
			if(inv.get(i) != 0) { //jeśli na danej pozycji jest wartość >0
				supports.add(i); //dodaj pozycję jako wsparcie
			}
		}
		return supports;
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
	private static int checkCoverability(ArrayList<Integer> refInv, ArrayList<Integer> refSupport, ArrayList<Integer> candidateInv,
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
	 * Metoda działająca dla całego zbioru inwariantów, sprawdza czy są w nim inwarianty nie mające
	 * <i>minimalnego wsparcia</i> (<i>minimal support</i> test, nie mylić z mimalnością inwariantu jako
	 * takiego). Jeśli są, to je usuwa.
	 * @param invariantsMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów, wierszami
	 */
	public static void finalSupportMinimalityTest(ArrayList<ArrayList<Integer>> invariantsMatrix) {	
		int invNumber = invariantsMatrix.size();
		ArrayList<Integer> getRidOfMatrix = new ArrayList<Integer>();
		
		boolean justGotHere = true;
		double interval = (double)invNumber / 10; 
		int steps = 0;

		for(int i=0; i<invNumber; i++) {
			if(justGotHere) {
				System.out.println();
				justGotHere = false;
			}
			if(steps == (int)interval) {
				steps = 0;
				System.out.print("*");
			} else
				steps++;
			
			ArrayList<Integer> inv = invariantsMatrix.get(i);
			ArrayList<Integer> invSupport = getSupport(inv);
			for(int j=0; j<invNumber; j++) {
				if(i==j)
					continue;
				
				ArrayList<Integer> ref = invariantsMatrix.get(j);
				ArrayList<Integer> refSupport = getSupport(ref);
				
				int x = checkCoverability(ref, refSupport, inv, invSupport);
				
				if(invSupport.equals(refSupport)) { //zależność liniowa
					if(getRidOfMatrix.contains(i) == false)
						getRidOfMatrix.add(i);
				}
				
				if(supportInclusionCheck(invSupport, refSupport) == true) { //zawieranie się wsparć
					if(getRidOfMatrix.contains(i) == false)
						getRidOfMatrix.add(i);
				}
				
				if(x==0 || x==1) {
					if(getRidOfMatrix.contains(i) == false)
						getRidOfMatrix.add(i);
				} else if(x==2) {
					if(getRidOfMatrix.contains(j) == false)
						getRidOfMatrix.add(j);
				}
			}
		}
		
		Collections.sort(getRidOfMatrix);
		int removedSoFar = 0;
		for(int el : getRidOfMatrix) {
			int index = el - removedSoFar;
			invariantsMatrix.remove(index);
			removedSoFar++;
		}
	}
	
	/**
	 * Metoda sprawdza zawieranie się wsparcia wektora refencyjnego w badanym inwariancie. Jeżeli każdy element
	 * wsparcia z referencyjnego zawiera się w inwariancie, to znaczy, że nie jest on minimalny.
	 * @param invSupp ArrayList[Integer] - wsparcie inwariantu
	 * @param refSupp ArrayList[Integer] - wsparcie wektora referencyjnego
	 * @return boolean - true, jeżeli inwariant zawiera wszystkie elementy wsparcia z referencyjnego - wtedy albo
	 * 		jest identyczny, albo nie jest minimalny.
	 */
	static boolean supportInclusionCheck(ArrayList<Integer> invSupp, ArrayList<Integer> refSupp) {
		for(int el : refSupp) {
			if(invSupp.contains(el) == false)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Metoda zwraca liczbę niekanonicznych inwariantów.
	 * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return int - liczba niekanonicznych
	 */
	public static int checkCanonity(ArrayList<ArrayList<Integer>> invMatrix) {
		int nonCardinal = 0;
		int size = invMatrix.size();
		for(int i=0; i<size; i++) {
			ArrayList<Integer> inv = invMatrix.get(i);
			int nextT = 0;
			int result = 0;
			
			for(int t=0; t<inv.size(); t++) { //znajdź pierwszy element wsparcia
				int value = inv.get(t);
				if(value == 0) {
					continue;
				} else {
					result =  bezwzgledna(value);
					nextT = t;
					break;
				}
			}
			
			boolean card = false;
			
			for(int t=nextT+1; t<inv.size(); t++) {
				int value = inv.get(t);
				if(value == 0)
					continue;
				
				value = bezwzgledna(value);
		        result = nwd(result, value);
		        
		        if(result == 1) {
		        	card = true;
		        	break;
		        }
			}
			
			if(card == false)
				nonCardinal++;
		}
		
		return nonCardinal;
	}
	
	public static int checkSupportMinimality(ArrayList<ArrayList<Integer>> invMatrix) {
		int nonMinimal = 0;
		int invSize = invMatrix.size();
		
		for(int i=0; i<invSize; i++) {
			ArrayList<Integer> invSupport = getSupport(invMatrix.get(i));
			
			for(int j=0; j<invSize; j++) {
				if(j == i)
					continue;
				ArrayList<Integer> refSupport = getSupport(invMatrix.get(j));
				
				if(InvariantsTools.supportInclusionCheck(invSupport, refSupport) == true) {
					nonMinimal++;
					break;
				}
			}
		}

		return nonMinimal;
	}
	
	/**
	 * Metoda porównująca zbiór inwariantów bazowy oraz referencyjny. 
	 * @return ArrayList[ArrayList[Integer]] - macierz wyników, 3 wektory: <br>
	 *  1 - część wspólna inwariantów ze zbiorem referencyjnym <br>
	 *  2 - inwarianty których nie ma w referencyjnym<br>
	 *  3 - inwarianty referencyjnego których nie ma w wygenerowanym zbiorze
	 */
	public static ArrayList<ArrayList<Integer>> compareInv(ArrayList<ArrayList<Integer>> invMatrix,
			ArrayList<ArrayList<Integer>> refInvMatrix) {
		
		int coreInvSize = invMatrix.size();
		int refInvSize = refInvMatrix.size();
		
		ArrayList<Integer> coreFoundInRef = new ArrayList<Integer>();
		ArrayList<Integer> refFoundInCore = new ArrayList<Integer>();
		ArrayList<Integer> nonInRef = new ArrayList<Integer>(); //są u nas, nie ma w INA - minimalne??
		ArrayList<Integer> refNotInCore = new ArrayList<Integer>(); //są w INA, nie ma u nas
		
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		
		int repeated = 0;
		int repeatedNotFound = 0;

		boolean presentInReferenceSet = false;
		for(int invMy=0; invMy<coreInvSize; invMy++) {
			presentInReferenceSet = false;
			ArrayList<Integer> myInvariant = invMatrix.get(invMy);
			
			for(int invRefID=0; invRefID < refInvSize; invRefID++) {
				if(refInvMatrix.get(invRefID).equals(myInvariant)) {
					
					coreFoundInRef.add(invMy);
					if(refFoundInCore.contains(invRefID)) {
						repeated++;
					} else {
						refFoundInCore.add(invRefID);
					}
					
					presentInReferenceSet = true;
					break;
				}
			}
			if(presentInReferenceSet == false) {
				if(nonInRef.contains(invMy)) {
					repeatedNotFound++;
				} else {
					nonInRef.add(invMy);
				}
			}
		}
		
		for(int i=0; i<refInvSize; i++) {
			if(refFoundInCore.contains(i) == false) {
				refNotInCore.add(i);
			}
		}
		ArrayList<Integer> repeatedVector = new ArrayList<Integer>();
		repeatedVector.add(repeated);
		repeatedVector.add(repeatedNotFound);
		
		result.add(coreFoundInRef); //część wspolna
		result.add(nonInRef); //core, ale nie ma Ref
		result.add(refNotInCore); //inwarianty Ref których nie ma w CoreSet
		result.add(repeatedVector);
		return result;
	}
	
	public static void printMatrix(ArrayList<ArrayList<Integer>> matrix) {
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
	 * Metoda wykrywa, które tranzycje są pokryte przez inwarianty.
	 * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[Integer] - zbiór ID tranzycji pokrytych inwariantami
	 */
	public static ArrayList<Integer> detectCovered(ArrayList<ArrayList<Integer>> invMatrix) {
		ArrayList<Integer> coveredTransSet = new ArrayList<Integer>();
		//for(int i=0; i<getTransitions().size(); i++)
		//	uncoveredTransitions.add(0);
		
		if(invMatrix.size() > 0) {
			int invSize = invMatrix.get(0).size();
			for (ArrayList<Integer> inv : invMatrix) {
				for(int t=0; t<invSize; t++) {
					if(inv.get(t) != 0) { //TODO ?
						if(coveredTransSet.contains(t) == false)
							coveredTransSet.add(t);
					}
				}
				if(coveredTransSet.size() == invSize)
					break;
			}
		}
		Collections.sort(coveredTransSet);
		return coveredTransSet;
	}
	
	/**
	 * Metoda wykrywa, które tranzycje nie są pokryte przez inwarianty.
	 * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[Integer] - zbiór ID tranzycji nie pokrytych inwariantami
	 */
	public static ArrayList<Integer> detectUncovered(ArrayList<ArrayList<Integer>> invMatrix) {
		ArrayList<Integer> uncoveredTransSet = null;
		ArrayList<Integer> coveredSet = detectCovered(invMatrix);
		
		if(invMatrix != null && invMatrix.size() > 0) {
			int invSize = invMatrix.get(0).size();
			uncoveredTransSet = new ArrayList<Integer>();
			for(int t=0; t<invSize; t++) {
				if(coveredSet.contains(t) == false)
					uncoveredTransSet.add(t);
			}
			
		} else {
			ArrayList<Transition> trans = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			if(trans != null && trans.size()>0) {
				int transSize = trans.size();
				uncoveredTransSet = new ArrayList<Integer>();
				for(int t=0; t<transSize; t++) {
					uncoveredTransSet.add(t);
				}
			}
		}
		return uncoveredTransSet;
	}
	
	/**
	 * Metoda tworząca macierz (drugiego typu) inwariantów - macierz obiektów klasy InvariantTransition.
	 * @param invMatrix ArrayList[ArrayList[Integer] - macierz inwariantów
	 * @return ArrayList[ArrayList[InvariantTransition]] - macierz inwariantów II typu
	 */
	public static ArrayList<ArrayList<InvariantTransition>> compute2ndFormInv(ArrayList<ArrayList<Integer>> invMatrix) {
		InvariantTransition currentTransition;
		ArrayList<ArrayList<InvariantTransition>> invariants2ndForm = null;
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		if (invMatrix != null && invMatrix.size() > 0) {
			invariants2ndForm = new ArrayList<ArrayList<InvariantTransition>>();
			if (transitions != null && invMatrix.get(0).size() == transitions.size()) {
				ArrayList<InvariantTransition> currentInvariant;
				int i; //iterator po tranzycjach sieci
				for (ArrayList<Integer> binaryInvariant : invMatrix) {
					currentInvariant = new ArrayList<InvariantTransition>();
					i = 0;
					for (Integer amountOfFirings : binaryInvariant) {
						if (amountOfFirings > 0) { // dla tranzycji odpalananych
							currentTransition = new InvariantTransition(transitions.get(i), amountOfFirings);
							currentInvariant.add(currentTransition);
						}
						i++;
					}
					invariants2ndForm.add(currentInvariant);
				}
			} else {
				JOptionPane.showMessageDialog(null,
					"The currently opened project does not match with loaded external invariants. \nPlease make sure you are loading the correct invariant file for the correct Petri net.",
					"Project mismatch error!", JOptionPane.ERROR_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Error: the currently opened project does not match with loaded external invariants. Please make sure you are loading the correct invariant file for the correct Petri net.", "error", true);
			}
		} else {
			JOptionPane.showMessageDialog(null,
				"Invariants data matrix unavailable. Possible cause: invariants generation or reading from file failed.",
				"No invariants data", JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: preparing invariants internal representation failed. 1st level invariants matrix unavailable.", "error", true);
		}
		
		return invariants2ndForm;
	}
	
	/**
	 * Zwraca wartość bezwzględną liczby podanej jako argument.
	 * @param i int - liczba
	 * @return int - |i|
	 */
	private static int bezwzgledna(int i) {
		if (i < 0)
			return -i;
		else
			return i;
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
	 * Metoda zwraca podzbiór inwariantów w których występuje dana reakcja.
	 * @param globalInv ArrayList[ArrayList[Integer]] - macierz inwariantów 
	 * @param transLoc id - nr reakcji
	 * @return ArrayList[ArrayList[Integer]] - podzbiór inwariantów z reakcją
	 */
	public static ArrayList<ArrayList<Integer>> returnInvWithTransition(ArrayList<ArrayList<Integer>> globalInv, int transLoc) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> vector : globalInv) {
			if(vector.get(transLoc) != 0) {
				result.add(new ArrayList<Integer>(vector));
			}
		}
		
		return null;
	}
	
	/**
	 * Metoda zwraca podzbiór inwariantów w których NIE występuje dana reakcja.
	 * @param globalInv ArrayList[ArrayList[Integer]] - macierz inwariantów 
	 * @param transLoc id - nr reakcji
	 * @return ArrayList[ArrayList[Integer]] - podzbiór inwariantów BEZ reakcji
	 */
	public static ArrayList<ArrayList<Integer>> returnInvWithoutTransition(ArrayList<ArrayList<Integer>> globalInv, int transLoc) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> vector : globalInv) {
			if(vector.get(transLoc) == 0) {
				result.add(new ArrayList<Integer>(vector));
			}
		}
		
		return null;
	}
	
	/**
	 * Metoda zwraca wektor mówiący o liczbie wystąpień danej tranzycji w inwariantach.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[Integer] - wektor frekwencji wystąpień tranzycji
	 */
	public static ArrayList<Integer> getFrequency(ArrayList<ArrayList<Integer>> invariants) {
		ArrayList<Integer> frequency = new ArrayList<Integer>();
		if(invariants == null || invariants.size() ==0)
			return frequency;
		
		int invNumber = invariants.size();
		int invSize = invariants.get(0).size();
		
		int freq = 0;
		for(int column=0; column<invSize; column++) {
			freq = 0;
			for(int row=0; row<invNumber; row++) {
				if(invariants.get(row).get(column) != 0) {
					freq++;
				}
			}
			frequency.add(freq);
		}
		
		return frequency;
	}
	
	/*
	int supSize = candidateSupport.size();
	ArrayList<ArrayList<Integer>> Hk = new ArrayList<ArrayList<Integer>>();
	for(int s : candidateSupport) {
		Hk.add(CMatrix.get(s));
	}
	double[][] A = new double[supSize][CMatrix.get(0).size()];
	for(int x=0; x < supSize; x++) {
		for(int y=0; y < CMatrix.get(0).size(); y++) {
			A[x][y] = Hk.get(x).get(y);
		}
	}
	Matrix xx = new Matrix(A);
	int rank = xx.transpose().rank();
	
	if(supSize <= rank + 1) {
		globalIncidenceMatrix.add(incMatrixNewRow);
		globalIdentityMatrix.add(invCandidate);
	} else {
		newRejected++;
	}
	*/
	
}
