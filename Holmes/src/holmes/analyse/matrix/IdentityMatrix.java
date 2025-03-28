package holmes.analyse.matrix;

/**
 * Klasa pomocnicza, dziedzicząca z klasy Matrix. Definiuje macierz n x n
 */
public class IdentityMatrix extends Matrix {
	/**
	 * Konstruktor obiektu klasy IdentityMatrix
	 * @param n int - rozmiar macierz
	 */
	public IdentityMatrix(int n) {
		initiateMatrix(n, n);
	}
}
