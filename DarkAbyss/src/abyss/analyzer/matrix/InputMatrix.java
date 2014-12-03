package abyss.analyzer.matrix;

import java.util.ArrayList;

import abyss.math.Place;
import abyss.math.Transition;

public class InputMatrix extends Matrix {
	public InputMatrix(ArrayList<Transition> transitionList,
			ArrayList<Place> placeList) {
		initiateMatrix(placeList.size(),transitionList.size());
		for (int i = 0; i < transitionList.size(); i++)
			for (int j = 0; j < placeList.size(); j++) {
				setValue(
						i,
						j,
						transitionList.get(i).getInArcWeightFrom(
								placeList.get(j)));
			}
	}
}
