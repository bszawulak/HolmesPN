package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.petrinet.elements.Transition;

/**
 * Klasa wewnętrznych, pomocniczych metod klasy PetriNet.
 */
public class PetriNetMethods {
	PetriNet pnCore;
	
	/**
	 * Konstruktor obiektu klasy PetriNetMethods.
	 * @param overlord PetriNet - obiekt sieci
	 */
	public PetriNetMethods(PetriNet overlord) {
		this.pnCore = overlord;
	}

	/**
	 * Metoda tworząca wektor określający, w którym MCT znajduje się dana tranzycja. -1 oznacza trywialne MCT.
	 * @return ArrayList[Integer] - wektor wynikowy
	 */
	public ArrayList<Integer> getTransMCTindicesVector() {
		ArrayList<Transition> transitions = pnCore.getTransitions();
		ArrayList<ArrayList<Transition>> mcts = pnCore.getMCTMatrix();
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(int i=0; i<transitions.size(); i++)
			result.add(-1);
		
		int mctNumber = -1;
		for(ArrayList<Transition> mct : mcts) {
			mctNumber ++;
			for(Transition t : mct) {
				if(mctNumber == mcts.size()-1)
					break; //zostawiamy -1 dla trywialnych

				result.set(transitions.indexOf(t), mctNumber);
			}
		}
		return result;
	}
}
