package abyss.analyzer.matrix;

import java.util.ArrayList;

import abyss.math.Place;
import abyss.math.Transition;

/**
 * Klasa tworz¹ca macierz incydencji, dziedziczy z Matrix.
 * W notacji sieci jest to macierz wartoœci ³uków wyjœciowych (out-arcs).
 * @author studens
 *
 */
public class IncidenceMatrix extends Matrix {
	/**
	 * Konstruktor obiektu klasy IncidenceMatrix.
	 * @param transitionList ArrayList[Transition] - lista tranzycji
	 * @param placeList ArrayList [Place] - lista miejsc
	 * @param inMatrix InputMatrix - macierz wejœciowa
	 * @param outMatrix OutputMatrix - macierz wyjœciowa
	 */
	public IncidenceMatrix(ArrayList<Transition> transitionList, 
			ArrayList<Place> placeList, InputMatrix inMatrix, OutputMatrix outMatrix) {
		initiateMatrix(placeList.size(), transitionList.size());
		for (int i = 0; i < transitionList.size(); i++)
			for (int j = 0; j < placeList.size(); j++) {
				setValue(i, j, outMatrix.getValue(i, j) - inMatrix.getValue(i, j));
			}
	}

	/**
	 * Metoda sprawdzaj¹ca, czy dla wszystkich miejsc, kolumna tranzycji jest zerowa
	 * @param l - nr kolumny tranzycji
	 * @return boolean - true, jeœli s¹ same zera w kolumnie l; false w przeciwnym wypadku
	 */
	public boolean restOfColumnZero(int l) {
		boolean result = true;
		for (int i = 0; i < amountOfPlaces; i++)
			if (getValue(i,l) != 0)
				result = false;
		return result;
	}
	
	/**
	 * Metoda znajduj¹ca niezerowy wiersz w zadanej kolumnie.
	 * @param l int - nr kolumny tranzycji
	 * @return int - numer wiersza miejsc w którym s¹ same zera
	 */
	public int findNonZeroRowInColumn(int l) {
		int row = -1;
		for (int i = 0; i < amountOfPlaces; i++)
			if (getValue(i,l) != 0) {
				row = i;
				break;
			}
		return row;
	}
	
	/**
	 * Zamiana dwóch wierszy ze sob¹ w tablicy.
	 * @param k int - pierwszy wiersz
	 * @param l int - drugi wiersz
	 */
	public void exchangeWithNonZeroRow(int k, int l) {
		exchangeRows(k, findNonZeroRowInColumn(l));
	}
}
