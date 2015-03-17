package abyss.analyzer;

import java.util.ArrayList;

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
