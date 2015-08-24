package holmes.analyzer.matrix;

import java.util.ArrayList;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa tworząca macierz wyjściową do obliczeń, dziedzicząca z Matrix
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
