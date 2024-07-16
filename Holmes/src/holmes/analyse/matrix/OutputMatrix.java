package holmes.analyse.matrix;

import java.util.ArrayList;

import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa tworząca macierz wyjściową do obliczeń, dziedzicząca z Matrix
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
				setValue(i,j,transitionList.get(i).getOutputArcWeightTo(placeList.get(j)));
			}
	}
}
