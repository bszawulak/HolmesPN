package abyss.analyzer;

import java.util.ArrayList;
import java.util.Collections;

import abyss.utilities.Tools;

/**
 *
 *
 */
public class InvariantsTools {


	public static ArrayList<ArrayList<Integer>> isTInvariantSet(ArrayList<ArrayList<Integer>> CMatrix, ArrayList<ArrayList<Integer>> invSet) {
		ArrayList<ArrayList<Integer>> noInv = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> inv : invSet) {
			if(checkInvariant(CMatrix, inv, true) == false) {
				noInv.add(inv);
			}
		}
		return noInv;
	}
	
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
			
			boolean isInv = true;
			for(int p=0; p<placesNumber; p++) {
				if(placesSumVector.get(p) != 0)
					return false;
			}
			return isInv;
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
			//TODO: != lub >
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
	
	public static void finalSupportMinimalityTest(ArrayList<ArrayList<Integer>> invariantsMatrix) {	
		int invNumber = invariantsMatrix.size();
		ArrayList<Integer> getRidOfMatrix = new ArrayList<Integer>();
		
		boolean justGotHere = true;
		double interval = (double)invNumber / 10; 
		int steps = 0;
		int linearDependence = 0;
		boolean lD = false;
		
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
			
			lD = false;
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
