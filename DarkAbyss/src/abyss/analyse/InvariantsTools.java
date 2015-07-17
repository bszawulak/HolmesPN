package abyss.analyse;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.InvariantTransition;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.elements.Arc.TypesOfArcs;
import abyss.utilities.Tools;

/**
 * Klasa narzędziowa, zawierająca metody (głównie statyczne) do testów związanych z inwariantami.
 * <br>Metody:
 * <br>
 * transposeMatrix - macierz transpozowana<br>
 * returnBinaryMatrix - zamiana macierzy na postać binarną<br>
 * isTInvariantSet - sprawdza, czy zbiór inwariantów zeruje macierz incydencji<br>
 * countNonInvariants - zwraca liczbę nie-inwariantó<br>
 * countNonT_InvariantsV2 - zwraca dokładną informację o sub/sur inwariantach i miejscach których nie zerują<br>
 * getInvariantsClassVector - zwraca wektor klasyfikujący każdy inwariant (o liczności zbioru inwariantów)<br>
 * checkInvariant - sprawdza czy podany inwariant zeruje macierz incydencji<br>
 * checkInvariantV2 - jak wyżej, tylko wolniejszy algorytm (mnoży i dodaje każdy element)<br>
 * getSupport - zwraca wsparcie inwariantu<br>
 * checkCoverability - sprawdza relację między 2 inwariantami (private)<br>
 * finalSupportMinimalityTest - sprawdza czy są inwarianty nie-minimalne i je usuwa<br>
 * supportInclusionCheck - sprawdza czy wsparcie inwariantu zawiera wszystkie elementy wsparcia drugiego inwariantu<br>
 * checkCanonity - zwraca liczbę niekanonicznych inwariantów<br>
 * checkCanonitySingle - sprawdza, czy podany inwariant jest kanoniczny<br>
 * getCanonicalInfo - zwraca wektor z informacją które inwarianty są kononiczne<br>
 * checkSupportMinimality - sprawdza cały zbiór inwariantów i podaje liczbę nie-minimalnych<br>
 * checkSupportMinimalityThorough - jak wyżej, ale podaje które są nie-minimalne i co zawierają<br>
 * compareInv - porównanie dwóch zbiorów inwariantów ze sobą<br>
 * printMatrix - pokazuje macierz na konsoli<br>
 * detectCovered - podaje zbiór tranzycji pokrytych przez inwarianty<br>
 * detectUncovered - j.w. : nie pokrytych<br>
 * compute2ndFormInv - macierz inwariantów 2 formy (obsolete)<br>
 * returnInvWithTransition - zwraca podzbiór inwariantów które zawierają wartość != 0 na podanej pozycji<br>
 * returnInvWithoutTransition - jak wyżej, tylko zbiór bez danej tranzycji<br>
 * getFrequency - zwraca wektor informujący w ilu inwariantach występuje każda tranzycja<br>
 * getActiveTransitions - zwraca wektor pokrytych przez inwarianty tranzycji<br>
 * getExtendedInvariantsInfo - informacje o inwariantach 'zawierających' niestandardowe łuki<br>
 * getInOutTransInfo - zwraca informacje o tym, ile tranzycji IN/OUT ma każdy inwariant<br>
 * transInInvariants - zwraca wektor z informacją w ilu inwariantach działa tranzycja<br>
 * isDoubleArc - zwraca informację, czy łuk jest podwójny
 * 
 * @author MR
 */
public final class InvariantsTools {
	private InvariantsTools() {} // to + final = klasa statyczna w NORMALNYM języku, jak np. C#

	/**
	 * Metoda zwraca macierz transponowaną.
	 * @param matrix ArrayList[ArrayList[Integer]] - macierz wejściowa
	 * @return ArrayList[ArrayList[Integer]] - macierz wyjściowa
	 */
	public static ArrayList<ArrayList<Integer>> transposeMatrix(ArrayList<ArrayList<Integer>> matrix) {
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
	 * Zamienia macierz na postać binarną.
	 * @param matrix ArrayList[ArrayList[Integer]] - macierz
	 * @return ArrayList[ArrayList[Integer]] - macierz 0-01
	 */
	public static ArrayList<ArrayList<Integer>> returnBinaryMatrix(ArrayList<ArrayList<Integer>> matrix) {
		if(matrix == null || matrix.size() == 0)
			return null;
		
		int rows = matrix.size();
		int columns = matrix.get(0).size();
		ArrayList<ArrayList<Integer>> resultMatrix = new ArrayList<ArrayList<Integer>>();
		
		for(int r=0; r<rows; r++) {
			ArrayList<Integer> binaryRow = new ArrayList<Integer>();
			for(int c=0; c<columns; c++) {
				if(matrix.get(r).get(c) != 0)
					binaryRow.add(1);
				else
					binaryRow.add(0);
			}
			resultMatrix.add(binaryRow);
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
	 * Metoda agregująca wyniki testu zerowania macierzy incydencji. Dla sub/sur inwariantów zwraca informację o miejsach
	 * dla których nie udało się dla pewnych inwariantów wyzerować kolumn.
	 * @param CMatrix ArrayList[ArrayList[Integer]] - macierz incydencji
	 * @param invSet ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[ArrayList[Integer]] - macierz wynikowa, pierwszy wektor to 4 elementowa informacja zbiorcza o:
	 * 	inwariantach, sub, sur-inwariantach oraz nie-inwariantach. Kolejne dwa wektory to informacja o miejscach w ramach
	 *  odpowiednio sur i sub-inwariantów.
	 */
	public static ArrayList<ArrayList<Integer>> countNonT_InvariantsV2(ArrayList<ArrayList<Integer>> CMatrix, ArrayList<ArrayList<Integer>> invSet) {
		ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>();
		
		int subInv = 0; // neg
		int surInv = 0; // pos
		int nonInv = 0;
		int zeroInvariants = 0;
		
		ArrayList<Integer> surPlacesVector = new ArrayList<Integer>();
		ArrayList<Integer> subPlacesVector = new ArrayList<Integer>();
		int placesSize = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size();
		for(int p=0; p<placesSize; p++) {
			surPlacesVector.add(0);
			subPlacesVector.add(0);
		}
		
		for(ArrayList<Integer> inv : invSet) {
			ArrayList<Integer> vector = checkInvariantV2(CMatrix, inv, true);
			if(vector.get(0) == 0) {
				zeroInvariants++;
			} else if(vector.get(0) == -1) {
				subInv++;
				subPlacesVector = quickSumT_inv(subPlacesVector, vector);
			} else if(vector.get(0) == 1) {
				surInv++;
				surPlacesVector = quickSumT_inv(surPlacesVector, vector);
			} else {
				nonInv++;
			}
		}
		ArrayList<Integer> summaryVector = new ArrayList<Integer>();
		summaryVector.add(zeroInvariants);
		summaryVector.add(surInv);
		summaryVector.add(subInv);
		summaryVector.add(nonInv);
		
		results.add(summaryVector);
		results.add(surPlacesVector);
		results.add(subPlacesVector);
		
		return results;
	}
	
	/**
	 * Metoda zwraca wektor o wielkości zbioru inwariantów. Każda pozycja określa za pomocą pojedynczej
	 * wartości liczbowej rodzaj odpowiedniego inwariantu.
	 * @param invSet ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[Integer] - wektor klasy inwariantów: 0: normalny, -1: sub-inv., 1: sur-inv., -99: non-inv.
	 */
	public static ArrayList<Integer> getInvariantsClassVector(ArrayList<ArrayList<Integer>> invSet)
	{
		ArrayList<Integer> results = new ArrayList<Integer>();
		InvariantsCalculator ic = new InvariantsCalculator(true);
		ArrayList<ArrayList<Integer>> incMatrix = ic.getCMatrix();
		
		for(ArrayList<Integer> inv : invSet) {
			ArrayList<Integer> vector = checkInvariantV2(incMatrix, inv, true);
			if(vector.get(0) == 0) {
				results.add(0);
			} else if(vector.get(0) == -1) {
				results.add(-1);
			} else if(vector.get(0) == 1) {
				results.add(1);
			} else {
				results.add(-99);
			}
		}
	
		
		return results;
	}
	
	/**
	 * Metoda pomocnicza dla countNonT_InvariantsV2 - dla każdego wystąpienia liczby różne od zera w wektorze 
	 * resultVector, powiększa o 1 wartość odpowiedniego elementu wektora base. Wektor resultVector od pozycji [1] do
	 * ostatniej zawiera informacje wektora wynikowego dla próby zerowania macierzy incydencji. Jakakolwiek wartość 
	 * różna od zera oznacza, że dla danego miejsca nie udało się wyzerować macierzy (dla T-inwariantów)
	 * @param base ArrayList[Integer] - wektor danych
	 * @param vector ArrayList[Integer] - wynik testu T-inwariantu
	 * @return ArrayList[Integer] base - zwraca nową wartość dla base
	 */
	private static ArrayList<Integer> quickSumT_inv(ArrayList<Integer> base, ArrayList<Integer> resultVector) {
		int size = resultVector.size();
		int baseSize = base.size();
		
		if(baseSize + 1 != size)
			return base;
		
		for(int i=0; i<baseSize; i++) {
			if(resultVector.get(i+1) != 0) {
				int oldV = base.get(i);
				oldV++;
				base.set(i, oldV);
			}
		}
		
		return base;
	}
	
	/**
	 * Metoda sprawdza, czy inwariant jest prawidłowy, tj. czy zeruje macierz incydencji podaną jako parametr.
	 * @param CMatrix ArrayList[ArrayList[Integer]] - macierz incydencji sieci, każdy wiersz to wektor po miejscach (kolumny)
	 * @param invariant ArrayList[Integer] - inwariant
	 * @param tInv boolean - true, jeśli testujemy T-inwarianty
	 * @return boolean - true, jeśli inwariant przeszedł test, false jeśli nie wyzerował macierzy
	 */
	public static boolean checkInvariant(ArrayList<ArrayList<Integer>> CMatrix, ArrayList<Integer> invariant, boolean tInv) {
		//CMatrix - każdy wiersz to wektor miejsc indeksowany numerem tranzycji [2][3] - 2 tranzycja, 3 miejsce
		if(tInv == true && CMatrix.size() > 0) {
			ArrayList<Integer> placesSumVector = new ArrayList<Integer>();
			if(invariant.size() != CMatrix.size())
				return false;
			
			int placesNumber = CMatrix.get(0).size();
			int transitionsNumber = invariant.size();

			for(int p=0; p<placesNumber; p++) {
				int sumForPlace = 0;
				for(int t=0; t<transitionsNumber; t++) {
					sumForPlace += CMatrix.get(t).get(p) * invariant.get(t);
				}
				placesSumVector.add(sumForPlace);
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
	 * Metoda sprawdza dokładnie wektor (inwariant) próbując za jego pomocą wyzerować macierz incydencji.
	 * @param CMatrix ArrayList[ArrayList[Integer]] - macierz incydencji
	 * @param inv ArrayList[Integer] - inwariant (?)
	 * @param tInv boolean - true: T-inwariant
	 * @return ArrayList[Integer] - pierwsza wartość ([0]) to wynik, kolejne miejsca do wektor wynikowy testu (miejsca dla T-inw)
	 */
	public static ArrayList<Integer> checkInvariantV2(ArrayList<ArrayList<Integer>> CMatrix, ArrayList<Integer> inv, boolean tInv) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		if(tInv != true || CMatrix.size() == 0)
			return null;
		
		ArrayList<Integer> invSupport = getSupport(inv);
		ArrayList<Integer> placesSumVector = new ArrayList<Integer>();
		if(inv.size() != CMatrix.size())
			return null;
		
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
		
		int positives = 0;
		int negatives = 0;
		int zeroes = 0;
		
		results.add(0);
		
		for(int p=0; p<placesNumber; p++) {
			int value = placesSumVector.get(p);
			if(value == 0) {
				zeroes++;
			} else if(value > 0) {
				positives++;
			} else if(value < 0)
				negatives++;
			
			results.add(value);
		}
		
		if(positives>0 && negatives>0) {
			results.set(0, -50);
			return results; //coś wybitnie nie tak z tym inwariantem
		} 
		if(zeroes == placesNumber) {
			results.set(0, 0);
			return results;
		}
		
		if(positives>0) {
			results.set(0, 1);
			return results;
		}
		if(negatives>0) {
			results.set(0, -1);
			return results;
		}
		
		return null;
	}
	
	/**
	 * Metoda zwraca wsparcie wektora wejściowego - numery pozycji na których w wektorze
	 * znajdują się wartości dodatnie.
	 * @param invariant ArrayList[Integer] - wektor wejściowy
	 * @return ArrayList[Integer] - wsparcie
	 */
	public static ArrayList<Integer> getSupport(ArrayList<Integer> invariant) {
		ArrayList<Integer> supports = new ArrayList<Integer>();
		int size = invariant.size();
		for(int i=0; i<size; i++) {
			if(invariant.get(i) != 0) { //jeśli na danej pozycji jest wartość =/= 0
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
			GUIManager.getDefaultGUIManager().log("Error #5a4f7ff3d45 - please advise authors of the program. Thank you."
					+ " P.S. Do not trust this invariant set. Use INA generator instead. Apologies again!", "warning", true);
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
	public static boolean supportInclusionCheck(ArrayList<Integer> invSupp, ArrayList<Integer> refSupp) {
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
	
	/**
	 * Metoda sprawdza kanoniczność inwariantu
	 * @param invariant ArrayList[Integer] - inwariant
	 * @return boolean - true, jeśli kanoniczny
	 */
	public static boolean checkCanonitySingle(ArrayList<Integer> invariant) {
		int nextT = 0;
		int result = 0;
		
		for(int t=0; t<invariant.size(); t++) { //znajdź pierwszy element wsparcia
			int value = invariant.get(t);
			if(value == 0) {
				continue;
			} else {
				result =  bezwzgledna(value);
				nextT = t;
				break;
			}
		}
		for(int t=nextT+1; t<invariant.size(); t++) {
			int value = invariant.get(t);
			if(value == 0)
				continue;
			
			value = bezwzgledna(value);
	        result = nwd(result, value);
	        
	        if(result == 1) {
	        	return true;
	        }
		}
		return false;
	}
	
	/**
	 * 
	 * @param invSet ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[Integer] - wektor inwariantów: 0 - kanoniczny, -1 - niekanoniczny
	 */
	public static ArrayList<Integer> getCanonicalInfo(ArrayList<ArrayList<Integer>> invSet) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		for(ArrayList<Integer> invariant : invSet) {
			boolean status = checkCanonitySingle(invariant);
			if(status == true)
				results.add(0);
			else
				results.add(-1);
		}
		return results;
	}
	
	/**
	 * Metoda sprawdza zbiór inwariantów i testuje ich minimalność. 
	 * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return int - liczba nie-minimalnych inwariantów
	 */
	public static int checkSupportMinimality(ArrayList<ArrayList<Integer>> invMatrix) {
		int nonMinimal = 0;
		int invSize = invMatrix.size();
		
		ArrayList<ArrayList<Integer>> supportMatrix = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<invSize; i++) {
			supportMatrix.add(getSupport(invMatrix.get(i)));
		}
		
		for(int i=0; i<invSize; i++) {
			//ArrayList<Integer> invSupport = getSupport(invMatrix.get(i));
			
			for(int j=0; j<invSize; j++) {
				if(j == i)
					continue;
				//ArrayList<Integer> refSupport = getSupport(invMatrix.get(j));
				
				if(InvariantsTools.supportInclusionCheck(supportMatrix.get(i), supportMatrix.get(j)) == true) {
					nonMinimal++;
					break;
				}
			}
		}

		return nonMinimal;
	}
	
	/**
	 * Metoda sprawdza zawieranie się wsparć inwariantów.
	 * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[ArrayList[Integer]] - macierz w której każdy wiersz zawiera numery inwariantów które 
	 * się w nim zawierają; jeśli wiersz na pozycji [0] nie zawiera numeru to znaczy że danych inwariant jest minimalny
	 */
	public static ArrayList<ArrayList<Integer>> checkSupportMinimalityThorough(ArrayList<ArrayList<Integer>> invMatrix) {
		ArrayList<ArrayList<Integer>> resNonMinimal = new ArrayList<ArrayList<Integer>>();
		
		int invSize = invMatrix.size();
		ArrayList<Integer> nonMinimalInfo = null;
		
		ArrayList<ArrayList<Integer>> supportMatrix = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<invSize; i++) {
			supportMatrix.add(getSupport(invMatrix.get(i)));
		}
		
		for(int i=0; i<invSize; i++) {
			nonMinimalInfo = new ArrayList<Integer>();
			
			for(int j=0; j<invSize; j++) {
				if(j == i)
					continue;
				
				if(InvariantsTools.supportInclusionCheck(supportMatrix.get(i), supportMatrix.get(j)) == true) {
					nonMinimalInfo.add(j);
				}
			}
			resNonMinimal.add(nonMinimalInfo);
		}

		return resNonMinimal;
	}
	
	/**
	 * Metoda porównująca zbiór inwariantów bazowy oraz referencyjny. 
	 * @param ArrayList[ArrayList[Integer]] - macierz referencyjna (z programu)
	 * @param ArrayList[ArrayList[Integer]] - macierz wczytana celem porównania z referencyjną
	 * @return ArrayList[ArrayList[Integer]] - macierz wyników, 3 wektory: <br>
	 *  1 - część wspólna inwariantów ze zbiorem referencyjnym <br>
	 *  2 - inwarianty których nie ma w referencyjnym ale są w załadowanym<br>
	 *  3 - inwarianty referencyjnego których nie ma w załadowanym zbiorze
	 */
	public static ArrayList<ArrayList<Integer>> compareInv(ArrayList<ArrayList<Integer>> refInvMatrix,
			ArrayList<ArrayList<Integer>> invLoadedMatrix) {
		
		int loadedInvSize = invLoadedMatrix.size();
		int refInvSize = refInvMatrix.size();
		
		ArrayList<Integer> loadedFoundInRef = new ArrayList<Integer>();
		ArrayList<Integer> loadedNotInRef = new ArrayList<Integer>(); //są w Loaded, nie ma w Ref
		ArrayList<Integer> refNotInCore = new ArrayList<Integer>(); //są w Ref, nie ma w Loaded
		
		ArrayList<Integer> refFoundInLoaded = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		
		ArrayList<ArrayList<Integer>> invLoadedRepetition = new ArrayList<ArrayList<Integer>>();
		
		int foundRepetitions = 0;
		int totalRepetitions = 0;

		boolean presentInReferenceSet = false;
		for(int invMy=0; invMy<loadedInvSize; invMy++) {
			presentInReferenceSet = false;
			ArrayList<Integer> loadedInvariant = invLoadedMatrix.get(invMy);
			
			if(invLoadedRepetition.contains(loadedInvariant) == false) {
				invLoadedRepetition.add(loadedInvariant);
			} else {
				totalRepetitions++;
			}
			
			for(int invRefID=0; invRefID < refInvSize; invRefID++) {
				if(refInvMatrix.get(invRefID).equals(loadedInvariant)) {
					
					loadedFoundInRef.add(invMy);
					if(refFoundInLoaded.contains(invRefID)) {
						foundRepetitions++;
					} else {
						refFoundInLoaded.add(invRefID);
					}
					
					presentInReferenceSet = true;
					break;
				}
			}
			
			if(presentInReferenceSet == false) {
				loadedNotInRef.add(invMy);
				//if(loadedNotInRef.contains(invMy)) {
				//	repeatedNotFound++;
				//} 
			}
		}
		
		for(int i=0; i<refInvSize; i++) {
			if(refFoundInLoaded.contains(i) == false) {
				refNotInCore.add(i);
			}
		}
		ArrayList<Integer> repeatedVector = new ArrayList<Integer>();
		repeatedVector.add(foundRepetitions);
		repeatedVector.add(totalRepetitions);
		
		result.add(loadedFoundInRef); //część wspolna
		result.add(loadedNotInRef); //Loaded, których nie ma w Reference
		result.add(refNotInCore); //inwarianty Reference których nie ma w Loaded
		result.add(repeatedVector);
		return result;
	}
	
	/**
	 * Metoda pokazuje na konsoli macierz.
	 * @param matrix ArrayList[ArrayList[Integer]] - macierz
	 */
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
		
		if(invMatrix.size() > 0) {
			int invSize = invMatrix.get(0).size();
			for (ArrayList<Integer> inv : invMatrix) {
				for(int t=0; t<invSize; t++) {
					if(inv.get(t) != 0) {
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
		
		return result;
	}
	
	/**
	 * Metoda zwraca indeksy inwariantów w których występuje dana reakcja.
	 * @param globalInv ArrayList[ArrayList[Integer]] - macierz inwariantów 
	 * @param transLoc id - nr reakcji
	 * @return ArrayList[Integer] - indeksy inwariantów z reakcją
	 */
	public static ArrayList<Integer> returnInvIndicesWithTransition(ArrayList<ArrayList<Integer>> globalInv, int transLoc) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int i=0; i<globalInv.size(); i++) {
			if(globalInv.get(i).get(transLoc) != 0) {
				result.add(i);
			}
		}
		
		return result;
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
		
		return result;
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
	
	/**
     * Zwraca wektor tranzycji które są w jakichkolwiek inwariantach.
     * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
     * @return ArrayList[Integer] - wektor tranzycji pokrytych
     */
    public static ArrayList<Integer> getActiveTransitions(ArrayList<ArrayList<Integer>> invMatrix) { 
    	ArrayList<Integer> trans = new ArrayList<Integer>();
    	for(int i=0; i<invMatrix.get(0).size(); i++) {
    		for(int r=0; r<invMatrix.size(); r++) {
    			if(invMatrix.get(r).get(i) > 0) {
    				trans.add(i);
    				break;
    			}
    		}
    	}
    	return trans;
    }
    
    /**
     * Metoda zwraca informację o inwariantach które zawierają łuki odczytu, hamujące, resetujące i wyrównujące
     * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
     * @return ArrayList[ArrayList[Integer]] - każdy wiersz: [0] # readarc [1] # inhibitor
     *  [2] # reset [3] # equal
     */
    public static ArrayList<ArrayList<Integer>> getExtendedInvariantsInfo(ArrayList<ArrayList<Integer>> invMatrix) {
    	ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>();
    	ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
    	
    	int invMatrixSize = invMatrix.size();
    	for(int i=0; i<invMatrixSize; i++) {
    		ArrayList<Integer> invariant = invMatrix.get(i);
    		int readArcs = 0;
    		int inhibitors = 0;
    		int resets = 0;
    		int equals = 0;
    		
    		int invSize = invariant.size();
    		for(int e=0; e<invSize; e++) {
    			int supp = invariant.get(e);
    			if(supp == 0) continue;
    			
    			Transition transition = transitions.get(e);
    			for(ElementLocation el : transition.getElementLocations()) {
    				for(Arc arc : el.getInArcs()) {
    					if(arc.getArcType() == TypesOfArcs.NORMAL)
    						continue;
    					
    					if(arc.getArcType() == TypesOfArcs.READARC) {
    						readArcs++;
    					} else if(arc.getArcType() == TypesOfArcs.INHIBITOR) {
    						inhibitors++;
    					} else if(arc.getArcType() == TypesOfArcs.RESET) {
    						resets++;
    					} else if(arc.getArcType() == TypesOfArcs.EQUAL) {
    						equals++;
    					}
    				}
    			}
    		}
    		ArrayList<Integer> invInfo = new ArrayList<Integer>();
			invInfo.add(readArcs);
			invInfo.add(inhibitors);
			invInfo.add(resets);
			invInfo.add(equals);
			
			results.add(invInfo);	
    	}
    	return results;
    }

    /**
     * Metoda zwraca informację ile tranzycji wejściowych i ile wyjściowych znajduje się w
     * inwariancie.
     * @param invMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
     * @return ArrayList[ArrayList[Integer]] - macierz wyników, w postaci 3-el. wektorów: [0] - pure in-trans,
     * 		[1] - in-trans (with inhibotors and/or readarcs), [2] - out-transitions
     */
    public static ArrayList<ArrayList<Integer>> getInOutTransInfo(ArrayList<ArrayList<Integer>> invMatrix) {
    	ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>();
    	ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
    	
    	int invMatrixSize = invMatrix.size();
    	for(int i=0; i<invMatrixSize; i++) {
    		ArrayList<Integer> invariant = invMatrix.get(i);
    		int inTrans = 0;
    		int pureInTrans = 0;
    		int outTrans = 0;
    		
    		int invSize = invariant.size();
    		for(int e=0; e<invSize; e++) {
    			int supp = invariant.get(e);
    			if(supp == 0) continue;
    		
    			Transition transition = transitions.get(e);
    			int inArcs = 0;
    			int extInArcs = 0;
    			int outArcs = 0;
    			for(ElementLocation el : transition.getElementLocations()) {
    				for(Arc a : el.getInArcs()) {
    					if(a.getArcType() == TypesOfArcs.INHIBITOR || a.getArcType() == TypesOfArcs.READARC) {
    						extInArcs++;
    					} else {
    						inArcs++;
    					}
    				}
    				
    				for(Arc a : el.getOutArcs()) {
    					if(a.getArcType() != TypesOfArcs.READARC) //nie liczy się: to jest in-arc w myśl dziwnych definicji!
    						outArcs++;
    				}
    			}
    			
    			if(inArcs == 0 && extInArcs == 0)
    				pureInTrans++;
    			
    			if(inArcs == 0 && extInArcs > 0)
    				inTrans++;
    			
    			if(outArcs == 0)
    				outTrans++;
    		}
    		
    		ArrayList<Integer> transInfo = new ArrayList<Integer>();
    		transInfo.add(pureInTrans);
    		transInfo.add(inTrans);
    		transInfo.add(outTrans);
    		results.add(transInfo);
    	}
    	return results;
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
    
	/**
	 * Metoda zwraca wektor określający, czy dany inwariant jest wykonalny czy nie.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @return ArrayList[Integer] - wektor wynikowy: -1: non-feasible; 1: feasible
	 */
	public static ArrayList<Integer> getFeasibilityClassesStatic(ArrayList<ArrayList<Integer>> invariants) {
		int invSize = invariants.size();
		ArrayList<Integer> results = new ArrayList<Integer>();
		ArrayList<Integer> readArcTransLocations = getReadArcTransitionsStatic();
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		for(int i=0; i<invSize; i++) {
			ArrayList<Integer> invariant = invariants.get(i);
			ArrayList<Integer> support = InvariantsTools.getSupport(invariant);
			
			if(isNonFeasibleStatic(support, readArcTransLocations, transitions) == true)
				results.add(-1);
			else
				results.add(1);
		}
		
		return results;
	}
    
    /**
	 * (STATIC) Metoda ustala czy wsparcie inwariantu zawiera którąkolwiek z tranzycji związanych z łukiem odczytu.
	 * @param support ArrayList[Integer] - wsparcie inwariantu
	 * @param readArcTransLocations ArrayList[Integer] - wektor lokazalicji tranzycji z łukami odczytu
	 * @return boolean - true, jeśli wsparcie zawiera choć jedną z tranzycji
	 */
	private static boolean isNonFeasibleStatic(ArrayList<Integer> support, ArrayList<Integer> readArcTransLocations,
			ArrayList<Transition> transitions) {
    	
		ArrayList<Integer> readarcTransitions = new ArrayList<Integer>();
    	//
    	
		for(int trans : readArcTransLocations) {
    		if(support.contains(trans) == true) {
    			readarcTransitions.add(trans);
    		}
    	}
		
		if(readarcTransitions.size() > 0) {
			for(int tID : readarcTransitions) { //dla każdej tranzycji 'readarc'
				Transition transition = transitions.get(tID);
				ArrayList<Place> connPlaces = new ArrayList<Place>();
				
				for(ElementLocation el : transition.getElementLocations()) {
					
					for(Arc arc : el.getInArcs()) {
						if(arc.getArcType() == TypesOfArcs.READARC) {
							Place p = (Place) arc.getStartNode();
							
							if((p.getTokensNumber() == 0) && connPlaces.contains(p) == false)
								connPlaces.add(p);
						}
					}	
				}
				//mamy zbiór miejsc które muszą dostaczyć tokeny
				ArrayList<ArrayList<Integer>> connectedTransitions = new ArrayList<ArrayList<Integer>>();
				for(Place p : connPlaces) {
					connectedTransitions.add(getConnectedTransitionsSet(p, transitions));
				}
				//teraz: inwariant musi zawiera min 1 tranzycję w każdym zbiorze, albo nie jest wykonalny
				
				
				for(ArrayList<Integer> set : connectedTransitions) {
					//intersection:
					ArrayList<Integer> test = new ArrayList<Integer>(set);
					
				    test.retainAll(support);
				    if(test.size() == 0)
				    	return true; //non feasible   
				}
			}
			return false; //feasible
		} else { //if(readarcTransitions.size() > 0) 
 			return false; //feasible
		}
    }
	
	/**
	 * (STATIC) Metoda zwraca pozycje wszystkich tranzycji, do których prowadzą łuki odczytu.
	 * @return ArrayList[Integer] - pozycje tranzycji z readarc
	 */
	private static ArrayList<Integer> getReadArcTransitionsStatic() {
		ArrayList<Integer> raTrans = new ArrayList<Integer>();
		ArrayList<Arc> arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		for(Arc a : arcs) {
			if(a.getArcType() == TypesOfArcs.READARC) { //tylko łuki odczytu
				Node node = a.getEndNode();
				if(node instanceof Transition) { 
					// nie trzeba dodatkowo dla Place, readarc to w programie 2 łuki: jeden w tą, drugi w drugą stronę
					Place p = (Place) a.getStartNode(); //jeśli node = Transition, to StartNode musi być typu Place
					if(p.getTokensNumber() > 0) //nie spełnia def. infeasible invariant
						continue;
					
					int position = transitions.indexOf((Transition)node);
					if(raTrans.contains(position) == false)
						raTrans.add(position);
				}
			}
		}
		return raTrans;
	}
	
	/**
	 * Metoda zwraca zbiór lokalizacji tranzycji związanych łukami wejściowymi z danym miejscem.
	 * @param place Place - miejsce
	 * @return ArrayList[Integer] - lista lokalizacji tranzycji
	 */
	private static ArrayList<Integer> getConnectedTransitionsSet(Place place, ArrayList<Transition> transitions) {
		ArrayList<Integer> connectedTransitions = new ArrayList<Integer>();

		for(ElementLocation el : place.getElementLocations()) {
			for(Arc a : el.getInArcs()) { //tylko łuki wejściowe
				if(a.getArcType() == TypesOfArcs.READARC || a.getArcType() == TypesOfArcs.INHIBITOR) 
					continue; //czyli poniższe działa tylko dla: NORMAL, RESET i EQUAL

				Transition trans = (Transition) a.getStartNode();
				int pos = transitions.indexOf(trans);
				if(connectedTransitions.contains(pos) == false) {
					connectedTransitions.add(pos);
				}// else {
					//GUIManager.getDefaultGUIManager().log("Internal error, net structure not canonical.", "error", true);
				//}
			}
		}
		return connectedTransitions;
	}
	
	/**
	 * Metoda sprawdza odległość między dwoma wierzchołkami.
	 * @param currentNode Node - wierzchołek startowy/aktualny
	 * @param target Node - wierzchołek poszukiwany
	 * @param visited ArrayList[Node] - lista odwiedzonych unikalnych węzłów
	 * @return int - liczba odwiedzonych węzłów sieci
	 */
	public static int calculateNodesDistance(Node currentNode, Node target, ArrayList<Node> visited) {
		if(!visited.contains(currentNode)) { //jeśli jeszcze nie ma
			visited.add(currentNode);
		}
		
		if(currentNode.equals(target))
			return 0;
		
		if (currentNode.getOutArcs()!=null) {
			for (Arc a : currentNode.getOutArcs()) { //łuki wychodzące z aktualnego wierzchołka
				Node node = a.getEndNode(); //wierzchołek końcowy łuku
				if(visited.contains(node) == false) {//jeśli jeszcze nie ma
					visited.add(node);
					
					if(currentNode.equals(target)) {
						return 0;
					} else {
						int path = calculateNodesDistance(node, target, visited);
						
						if(path > -1) {
							path++;
							return path;
						}
					}
					
				}
			}
		}
		return -1;
	}
	
	/**
	 * Metoda zwraca wektor o liczności tranzycji z informacją, w ilu inwariantach dana tranzycja występuje.
	 * @return ArrayList[Integer] - wektor tranzycji
	 */
	public static ArrayList<Integer> transInInvariants() {
		ArrayList<Integer> results = new ArrayList<Integer>();
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		for(int t=0; t<transitions.size(); t++)
			results.add(0);
		
		ArrayList<ArrayList<Integer>> invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		if(invariantsMatrix == null || invariantsMatrix.size() == 0)
			return results;
		
		for(int inv=0; inv < invariantsMatrix.size(); inv++) { // po wszystkich inwariantach
			for(int t=0; t < invariantsMatrix.get(0).size(); t++) { //po wszystkich tranzycjach
				if(invariantsMatrix.get(inv).get(t) > 0) {
					int oldVal = results.get(t);
					oldVal++;
					results.set(t, oldVal);
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Metoda wykrywająca łuk podwójny.
	 * @param arc Arc - łuk
	 * @return boolean - true jeśli istnieje łuk podwójny
	 */
	public static boolean isDoubleArc(Arc arc) {
		Node startN = arc.getStartNode();
		Node endN = arc.getEndNode();
		
		for(Arc a : endN.getOutArcs()) {
			if(a.getEndNode() == startN) {
				return true;
			}
		}
		return false;
	}
}
