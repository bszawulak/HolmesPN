package abyss.analyzer.matrix;

import java.util.ArrayList;

import abyss.math.Place;
import abyss.math.Transition;

/**
 * Klasa tworz¹ca macierz wejœciow¹ do obliczeñ, dziedzicz¹ca z Matrix.
 * W notacji sieci jest to macierz wartoœci ³uków wyjœciowych (in-arcs).
 * @author students
 *
 */
public class InputMatrix extends Matrix {
	/**
	 * Konstruktor obiektu klasy InputMatrix
	 * @param transitionList ArrayList[Transition] - lista tranzycji
	 * @param placeList ArrayList[Place] - lista miejsc
	 */
	public InputMatrix(ArrayList<Transition> transitionList, ArrayList<Place> placeList) {
		initiateMatrix(placeList.size(),transitionList.size());
		for (int i = 0; i < transitionList.size(); i++)
			for (int j = 0; j < placeList.size(); j++) {
				setValue(i, j, transitionList.get(i).getInArcWeightFrom( placeList.get(j)));
			}
	}
}
