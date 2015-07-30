package abyss.analyse;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;

/**
 * Klasa służąca do obliczania niedziałających elementów w przypadku, gdy wskazany wcześniej element sieci (lub zbiór)
 * nie będzie nigdy aktywny (z powodów różnych, aczkolwiek dla tej klasy zupełnie nieistotnych).
 * Zwraca listę elementów, które przestają funkcjonować w następnie dysfunkcji wskazanych elementów (lub ich zbioru) - na
 * bazie inwariantów.
 * @author MR
 *
 */
public class KnockoutCalculator {
	ArrayList<ArrayList<Integer>> calc_invariants;
	ArrayList<Transition> calc_transitions;
	ArrayList<Place> calc_places;

	/**
	 * Konstruktor klasy KnockoutCalculator
	 */
	public KnockoutCalculator() {
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix(); 
    	if(invariants == null || invariants.size() == 0) { //STEP 1: EM obliczono
    		return;
    	} else {
    		calc_invariants = invariants;
    		calc_transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
    		calc_places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
    	}
	}
	
	
	public ArrayList<Transition> calculateKnockout(ArrayList<Transition> knockout) {
		ArrayList<Transition> resultSet = new ArrayList<Transition>();
		int invNumber = calc_invariants.size();
		int transNumber = calc_transitions.size();
		
		for(Transition trans : knockout) { //dla każdej testowanej tramzcji
			int transLocation = calc_transitions.indexOf(trans);

			for(int i=0; i<invNumber; i++) { // dla każdego inwariantu
				if(calc_invariants.get(i).get(transLocation) > 0) { // jeśli testowana się w nim zawiera
					for(int t=0; t<transNumber; t++) { // dla każdej tranzycji
						if(calc_invariants.get(i).get(t) > 0) {
							Transition knockedout = calc_transitions.get(t);
							if(resultSet.contains(knockedout) == false) {
								resultSet.add(knockedout);
							}
						}
					}
				}
			}
		}
		
		for(Transition trans : knockout) {
			resultSet.remove(trans);
		}
		
		return resultSet;
	}
}
