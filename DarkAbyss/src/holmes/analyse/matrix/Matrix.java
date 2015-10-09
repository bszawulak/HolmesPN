package holmes.analyse.matrix;

/**
 * Klasa abstrakcyjna definiująca podstawowe operacje na macierz t x p, gdzie
 * t to liczba tranzycji, p to liczba miejsc.
 * @author students
 *
 */
public abstract class Matrix {
	private int[][] matrix;
	protected int amountOfTransitions;
	protected int amountOfPlaces;

	/**
	 * Metoda tworząca tablicę.
	 * @param numberOfPlaces int - liczba miejsc
	 * @param numberOfTransitions - liczba tranzycji
	 */
	protected void initiateMatrix(int numberOfPlaces,int numberOfTransitions) {
		setMatrix(new int[numberOfTransitions][numberOfPlaces]);
		amountOfTransitions = numberOfTransitions;
		amountOfPlaces = numberOfPlaces;
	}
	
	/**
	 * Metoda pobierająca wartość z tablicy.
	 * @param transitionIndex int - indeks tranzycji
	 * @param placeIndex int - indeks miejsca
	 * @return int - wartość z tablicy
	 */
	public int getValue(int transitionIndex, int placeIndex) {
		if(transitionIndex < amountOfTransitions && placeIndex < amountOfPlaces)
			return getMatrix()[transitionIndex][placeIndex];
		else
			return -99999;
	}
	
	/**
	 * Metoda ustawiająca nową wartość w komórce tablicy.
	 * @param transitionIndex int - indeks tranzycji
	 * @param placeIndex int - indeks miejsca
	 * @param value int - nowa wartość
	 */
	protected void setValue(int transitionIndex, int placeIndex, int value) {
		if(transitionIndex < amountOfTransitions && placeIndex < amountOfPlaces)
			getMatrix()[transitionIndex][placeIndex] = value;
	}
	
	/**
	 * Metoda odpowiedzialna za zamianę wierszy.
	 * @param rowA int - indeks wiersza A
	 * @param rowB int - indeks wiersza B
	 */
	public void exchangeRows(int rowA, int rowB) {
		for (int i = 0; i < amountOfTransitions; i++) {
			int temp = getMatrix()[rowA][i];
			getMatrix()[rowA][i]  = getMatrix()[rowB][i];
			getMatrix()[rowB][i] = temp;
		}
	}

	/**
	 * Metoda zwraca macierz.
	 * @return int[][] - macierz
	 */
	public int[][] getMatrix() {
		return matrix;
	}

	/**
	 * Metoda ustawiająca nową macierz.
	 * @param matrix int[][] - nowa macierz
	 */
	private void setMatrix(int[][] matrix) {
		this.matrix = matrix;
	}
}
