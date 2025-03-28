package holmes.analyse;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa służąca do obliczania niedziałających elementów w przypadku, gdy wskazany wcześniej element sieci (lub zbiór)
 * nie będzie nigdy aktywny (z powodów różnych, aczkolwiek dla tej klasy zupełnie nieistotnych).
 * Zwraca listę elementów, które przestają funkcjonować w następnie dysfunkcji wskazanych elementów (lub ich zbioru) - na
 * bazie inwariantów.
 */
public class KnockoutCalculator {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	ArrayList<ArrayList<Integer>> calc_invariants;
	ArrayList<Transition> calc_transitions;
	ArrayList<Place> calc_places;

	/**
	 * Konstruktor klasy KnockoutCalculator
	 */
	public KnockoutCalculator() {
		ArrayList<ArrayList<Integer>> invariants = overlord.getWorkspace().getProject().getT_InvMatrix(); 
    	if(invariants == null || invariants.isEmpty()) { //STEP 1: EM obliczono
    		return;
    	} else {
    		calc_invariants = invariants;
    		calc_transitions = overlord.getWorkspace().getProject().getTransitions();
    		calc_places = overlord.getWorkspace().getProject().getPlaces();
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
							if(!resultSet.contains(knockedout)) {
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
