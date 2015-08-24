package holmes.analyzer.matrix;

import java.util.ArrayList;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa tworząca macierz wejściową do obliczeń, dziedzicząca z Matrix.
 * W notacji sieci jest to macierz wartości łuków wyjściowych (in-arcs).
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
