package holmes.petrinet.functions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa metod pomocniczych dla zarządzania tranzycjami funkcyjnymi.
 * 
 * @author MR
 *
 */
public class FunctionsTools {

	public FunctionsTools() {
		
	}
	
	/**
	 * Kiedy tam metoda jest wywoływana w trakcie usuwania miejsca, jest ono już wspomnieniem - prawie. Należy je jeszcze
	 * usunąć z wpisów funkcji które je zawierały.
	 * @param place Place - miejsce usuwane
	 * @param placeIndex int - jego stara lokalizacja
	 * @return boolean - true, jeśli jakakolwiek funkcja została przez to zmodyfikowana i zdezaktywowana
	 */
	public static boolean revalidateFunctions(Place place, int placeIndex) {
		boolean removedAnything = false;
		GUIManager overlord = GUIManager.getDefaultGUIManager();
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
	
		for(Transition transition : transitions) {
			for(FunctionContainer fc : transition.accessFunctionsList()) {
				if(fc.involvedPlaces.contains(place)) {
					int transIndex = transitions.indexOf(transition);
					overlord.log("Function: "+fc.function+" of transition t"+transIndex+" has been disabled due to removal of "
							+ "place p"+placeIndex, "warning", false);
					
					fc.enabled = false;
					fc.correct = false;
					fc.function.replaceAll("p"+placeIndex, " ??? ");
					fc.involvedPlaces.remove(place);
					removedAnything = true;
				}
			}
		}
		return removedAnything;
	}
	
	/**
	 * Metoda synchronizuje równania oraz wektor miejsc używanych przez równanie. Nieistniejące w sieci miejsca
	 * nie są brane pod uwagę w wektorze miejsc, są też usuwane w razie detekcji z równania.
	 * @param fc FunctionContainer - obiekt równania
	 */
	public static void resetPlaceVector(FunctionContainer fc) {
		GUIManager overlord = GUIManager.getDefaultGUIManager();
		ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
		
		int placesNumber = places.size();
		//ArrayList<String> placePatterns = new ArrayList<String>();
		//for(int p=0; p<placesNumber; p++)
		//	placePatterns.add("p"+p);
		
		//String function = fc.function;
		//function = "p0 + p2 - p13 * p45"; //test
		
		fc.involvedPlaces.clear();
		
		Pattern p = Pattern.compile("p\\d+");
		Matcher m = p.matcher(fc.function);
		ArrayList<String> foundPlaces = new ArrayList<>();
		while (m.find()) {
			String x = m.group();
			foundPlaces.add(x);
			int index = Integer.parseInt(x.substring(1));
			if(index >= placesNumber || index < 0) {
				fc.function = fc.function.replace(x, " ??? ");
			} else {
				Place place = places.get(index);
				if(!fc.involvedPlaces.contains(place))
					fc.involvedPlaces.add(place);
			}
		}
	}

	public static boolean validateFunction(FunctionContainer fc) {
		resetPlaceVector(fc);
		String equation = fc.function;
		if(equation.contains("???"))
			return false;
		
		
		
		return true;
	}
	
	
}
