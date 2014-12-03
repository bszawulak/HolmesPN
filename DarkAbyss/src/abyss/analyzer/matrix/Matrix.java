package abyss.analyzer.matrix;

public abstract class Matrix {
	private int[][] matrix;
	protected int amountOfTransitions,amountOfPlaces;

	protected void initiateMatrix(int numberOfPlaces,int numberOfTransitions) {
		setMatrix(new int[numberOfTransitions][numberOfPlaces]);
		amountOfTransitions = numberOfTransitions;
		amountOfPlaces = numberOfPlaces;
	}
	
	public int getValue(int transitionIndex, int placeIndex) {
		return getMatrix()[transitionIndex][placeIndex];
	}
	
	protected void setValue(int transitionIndex, int placeIndex, int value) {
		getMatrix()[transitionIndex][placeIndex] = value;
	}
	
	public void exchangeRows(int rowA, int rowB) {
		for (int i = 0; i < amountOfTransitions; i++) {
			int temp = getMatrix()[rowA][i];
			getMatrix()[rowA][i]  = getMatrix()[rowB][i];
			getMatrix()[rowB][i] = temp;
		}
	}

	public int[][] getMatrix() {
		return matrix;
	}

	private void setMatrix(int[][] matrix) {
		this.matrix = matrix;
	}
}
