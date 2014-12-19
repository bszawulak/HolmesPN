package abyss.analyzer.matrix;

import java.util.ArrayList;

import abyss.math.Place;
import abyss.math.Transition;

/**
 * Klasa tworz�ca macierz incydencji, dziedziczy z Matrix.
 * W notacji sieci jest to macierz warto�ci �uk�w wyj�ciowych (out-arcs).
 * @author studens
 *
 */
public class IncidenceMatrix extends Matrix {
	/**
	 * Konstruktor obiektu klasy IncidenceMatrix.
	 * @param transitionList ArrayList[Transition] - lista tranzycji
	 * @param placeList ArrayList [Place] - lista miejsc
	 * @param inMatrix InputMatrix - macierz wej�ciowa
	 * @param outMatrix OutputMatrix - macierz wyj�ciowa
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
	 * Metoda sprawdzaj�ca, czy dla wszystkich miejsc, kolumna tranzycji jest zerowa
	 * @param l - nr kolumny tranzycji
	 * @return boolean - true, je�li s� same zera w kolumnie l; false w przeciwnym wypadku
	 */
	public boolean restOfColumnZero(int l) {
		boolean result = true;
		for (int i = 0; i < amountOfPlaces; i++)
			if (getValue(i,l) != 0)
				result = false;
		return result;
	}
	
	/**
	 * Metoda znajduj�ca niezerowy wiersz w zadanej kolumnie.
	 * @param l int - nr kolumny tranzycji
	 * @return int - numer wiersza miejsc w kt�rym s� same zera
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
	 * Zamiana dw�ch wierszy ze sob� w tablicy.
	 * @param k int - pierwszy wiersz
	 * @param l int - drugi wiersz
	 */
	public void exchangeWithNonZeroRow(int k, int l) {
		exchangeRows(k, findNonZeroRowInColumn(l));
	}
}
