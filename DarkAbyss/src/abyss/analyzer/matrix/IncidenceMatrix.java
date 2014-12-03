package abyss.analyzer.matrix;

import java.util.ArrayList;

import abyss.math.Place;
import abyss.math.Transition;

public class IncidenceMatrix extends Matrix {

	public IncidenceMatrix(ArrayList<Transition> transitionList,
			ArrayList<Place> placeList, InputMatrix inMatrix,
			OutputMatrix outMatrix) {
		initiateMatrix(placeList.size(), transitionList.size());
		for (int i = 0; i < transitionList.size(); i++)
			for (int j = 0; j < placeList.size(); j++) {
				setValue(i, j,
						outMatrix.getValue(i, j) - inMatrix.getValue(i, j));
			}
	}

	public boolean restOfColumnZero(int l) {
		boolean result = true;
		for (int i = 0; i < amountOfPlaces; i++)
			if (getValue(i,l) != 0)
				result = false;
		return result;
	}
	
	public int findNonZeroRowInColumn(int l) {
		int row = -1;
		for (int i = 0; i < amountOfPlaces; i++)
			if (getValue(i,l) != 0) {
				row = i;
				break;
			}
		return row;
	}
	
	public void exchangeWithNonZeroRow(int k, int l) {
		exchangeRows(k, findNonZeroRowInColumn(l));
	}
}
