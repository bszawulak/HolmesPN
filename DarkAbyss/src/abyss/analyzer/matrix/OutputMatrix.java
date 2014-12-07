package abyss.analyzer.matrix;

import java.util.ArrayList;

import abyss.math.Place;
import abyss.math.Transition;

/**
 * Klasa tworz¹ca macierz wyjœciow¹ do obliczeñ, dziedzicz¹ca z Matrix
 * @author students
 *
 */
public class OutputMatrix extends Matrix {
	
	/**
	 * Konstruktor obiektu klasy OutputMatrix
	 * @param transitionList ArrayList[Transition] - lista tranzycji
	 * @param placeList ArrayList[Place] - lista miejsc
	 */
	public OutputMatrix(ArrayList<Transition> transitionList,ArrayList<Place> placeList) {
		initiateMatrix(placeList.size(), transitionList.size());
		for (int i = 0; i < transitionList.size(); i++)
			for (int j = 0; j < placeList.size(); j++) {
				setValue(i,j,transitionList.get(i).getOutArcWeightTo(placeList.get(j)));
			}
	}
}
