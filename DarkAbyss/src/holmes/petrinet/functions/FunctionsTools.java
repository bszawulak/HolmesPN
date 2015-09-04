package holmes.petrinet.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.windows.HolmesNotepad;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.ValidationResult;

/**
 * Klasa metod pomocniczych dla zarządzania tranzycjami funkcyjnymi.
 * 
 * @author MR
 *
 */
public class FunctionsTools {
	private static GUIManager overlord;
	
	public FunctionsTools() {
		overlord = GUIManager.getDefaultGUIManager();
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
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
	
		for(Transition transition : transitions) {
			for(FunctionContainer fc : transition.accessFunctionsList()) {
				
				//if(fc.involvedPlaces.contains(place)) {
				if(fc.involvedPlaces.containsKey("p"+placeIndex)) {
					int transIndex = transitions.indexOf(transition);
					overlord.log("Function: "+fc.function+" of transition t"+transIndex+" has been disabled due to removal of "
							+ "place p"+placeIndex, "warning", false);
					
					fc.enabled = false;
					fc.correct = false;
					fc.function.replaceAll("p"+placeIndex, " ??? ");
					fc.involvedPlaces.remove("p"+placeIndex);
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
	 * @param commentField JTextArea - log podokna funkcji, do wyświetlania komunikatów, null jeśli nie ma być żadnych
	 * @param places ArrayList[Place] - wektor miejsc sieci
	 */
	public static void resetPlaceVector(FunctionContainer fc, JTextArea commentField, ArrayList<Place> places) {
		int placesNumber = places.size();
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
				if(commentField != null) {
					commentField.append("Non existing place identifier used: "+x+"\n");
				}
			} else {
				Place place = places.get(index);
				String key = "p"+index;
				if(!fc.involvedPlaces.containsKey(key))
					fc.involvedPlaces.put(key, place);
			}
		}
	}
	
	/**
	 * Metoda przygotowująca całą sieć do działań funkcyjnych - aktywuje wszystkie aktywne funkcje tranzycji funkcjonalnych.
	 * @param notepad HolmesNotepad notepad - jeśli nie null, będą w nim wyświetlane logi problemów
	 * @param places ArrayList[Place] - wektor miejsc sieci
	 * @return boolean - true, jeśli metoda napotkała jakiekolwiek problemy
	 */
	public static boolean validateFunctionNet(HolmesNotepad notepad, ArrayList<Place> places) {
		GUIManager overlord = GUIManager.getDefaultGUIManager();
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		boolean logErrors = false;
		boolean errorsFlag = false;
		if(notepad != null) {
			logErrors = true;
			notepad.addTextLineNL("List of errors for previously enabled functions:", "text");
			notepad.addTextLineNL("", "text");
		}
		
		int transCounter = -1;
		for(Transition transition : transitions) {
			transCounter++;
			if(!transition.isFunctional())
				continue;
			
			for(FunctionContainer fc : transition.accessFunctionsList()) {
				if(!fc.enabled)
					continue;
				
				resetPlaceVector(fc, null, places);
				if(fc.function.contains("???") || fc.function.length()==0) {
					errorsFlag = true;
					fc.enabled = false;
					fc.correct = false;
					fc.equation = null;
					if(logErrors)
						notepad.addTextLineNL("t"+transCounter+" : "+fc.toString(), "text");
				}
				
				try {
					ExpressionBuilder builder = new ExpressionBuilder(fc.function);
					for(String key : fc.involvedPlaces.keySet()) {
						builder.variable(key);
					}
					Expression expression = builder.build();
					for(String key : fc.involvedPlaces.keySet()) {
						Place place = fc.involvedPlaces.get(key);
						expression.setVariable(key, place.getTokensNumber());
					}
					
					ValidationResult result = expression.validate();
					List<String> errors = result.getErrors();
					if(errors != null && logErrors) {
						errorsFlag = true;
						notepad.addTextLineNL("  variables initialization error (function disabled):", "text");
						for(String error : errors) {
							notepad.addTextLineNL("    * "+error, "text");
						}
					}
					
					if(result.isValid()) {
						fc.equation = expression;
					} else {
						fc.equation = null;
						fc.enabled = false;
						fc.correct = false;
					}
				} catch (Exception e) {
					errorsFlag = true;
					notepad.addTextLineNL("   CRITICAL ERROR WHILE INITIALIZATION. FUNCTION DISABLED.", "text");
					fc.equation = null;
					fc.enabled = false;
					fc.correct = false;
				}
			}
		}
		return errorsFlag;
	}

	/**
	 * Metoda przygotowuje funkcję do użycia. Jeśli wszystko jest ok, jest ona doprowadzana do postaci równania gotowego
	 * do użycia.
	 * @param fc FunctionContainer - kontener funkcji
	 * @param newEquation String - nowa wprowadzana właśnie funkcja
	 * @param silence boolean - true, jeśli nie ma być wyświetlanych komunikatów w logu programu
	 * @param commentField JTextArea - nie ważne co wyżej, wyświetla błędy w logu programu
	 * @return boolean - true, jeśli operacja się udała
	 */
	public static boolean validateFunction(FunctionContainer fc, String newEquation, boolean silence, JTextArea commentField, 
			ArrayList<Place> places) {
		
		fc.function = newEquation;
		resetPlaceVector(fc, commentField, places);
		if(fc.function.contains("???") || fc.function.length()==0)
			return false;
		
		try {
			ExpressionBuilder builder = new ExpressionBuilder(fc.function);
			for(String key : fc.involvedPlaces.keySet()) {
				builder.variable(key);
			}
			Expression expression = builder.build();
			for(String key : fc.involvedPlaces.keySet()) {
				Place place = fc.involvedPlaces.get(key);
				expression.setVariable(key, place.getTokensNumber());
			}
			
			ValidationResult result = expression.validate();
			List<String> errors = result.getErrors();
			if(errors != null && !silence) {
				HolmesNotepad note = new HolmesNotepad(640, 480);
				note.setVisible(true);
				
				for(String error : errors) {
					note.addTextLineNL(error, "text");
				}
			}
			
			if(commentField != null) {
				if(errors != null) {
					for(String error : errors) {
						commentField.append(error+"\n");
					}
				}
			}
			
			if(result.isValid()) {
				fc.equation = expression;
			} else {
				fc.equation = null;
				fc.enabled = false;
				fc.correct = false;
			}
			return true;
		} catch (Exception e) {
			commentField.append("Function creation critically failed for: "+fc.function+"\n");
			fc.equation = null;
			fc.enabled = false;
			fc.correct = false;
			return false;
		}
	}
	
	/**
	 * Metoda pomocnicza, obliczająca funkcję aktywacji (dynamiczną wagę łuku). Jeśli tokenów w miejscu początkowym
	 * łuku jest mniej niż wartość obliczonego równania, tranzycja nie jest aktywna.
	 * @param startPlaceTokens int - tokeny w miejscu
	 * @param arc Arc - łuk
	 * @param nonFuncWeight - pozafunkcyjna waga, używana gdy nie udadzą się obliczenia równania
	 * @return boolean - true, jeśli tokenów jest wystarczająco
	 */
	public static boolean getFunctionDecision(int startPlaceTokens, Arc arc, int nonFuncWeight, Transition transition) {
		//wartość funkcji musi być >= startPlaceTokens
		try {
			FunctionContainer fc = transition.getFunctionContainer(arc);
			if(fc != null) { // jeśli znaleziono, to od razu przypisujemy oryginalną wagę
				fc.currentValue = nonFuncWeight; //wartość początkowa: oryginalna waga
			}
			
			if(fc != null && fc.enabled == true && fc.correct == true) {
				fc.currentValue = getFunctionValue(fc); //wartość równania, ale:
				fc.currentValue = fc.currentValue <= 0 ? nonFuncWeight : fc.currentValue; //like a boss
				
				if (startPlaceTokens < fc.currentValue)
					return false;
				else
					return true;
				
			} else { //oryginalna waga przypisana do fc.currentValue tak czy inaczej (ważne!)
				if (startPlaceTokens < arc.getWeight())
					return false;
				else
					return true;
			}
			
		} catch (Exception e) {
			//jak coś wybuchło, sprawdzamy po staremu:
			if (startPlaceTokens < arc.getWeight())
				return false;
			else
				return true;
		}
	}
	

	/**
	 * Metoda obliczająca wartość równania funkcji, na bazie tokenów w miejsach użytych w równaniu.
	 * @param fc FunctionContainer - kontener funkcji
	 * @return double - wartość funkcji lub -1 jeśli coś nie wyszło
	 */
	private static double getFunctionValue(FunctionContainer fc) {
		try {
			for(String key : fc.involvedPlaces.keySet()) { //aktualna wartość tokenów
				Place place = fc.involvedPlaces.get(key);
				fc.equation.setVariable(key, place.getTokensNumber());
			}
			double result = fc.equation.evaluate(); //wymagana waga do aktywacji
			return result;
		} catch (Exception e) {
			return -1;
		}
	}
	
	public static void functionalExtraction(Transition transition, Arc arc, Place place) {
		if(transition.isFunctional()) {
			FunctionContainer fc = transition.getFunctionContainer(arc);
			if(fc != null) //TODO: czy to jest potrzebne? jeśli na początku symulacji wszystkie tranzycje zyskają te wektory?
				place.modifyTokensNumber(-((int) fc.currentValue));
				//nie ważne, aktywna czy nie, jeśli nie, to tu jest i tak oryginalna waga
			else
				place.modifyTokensNumber(-arc.getWeight());
		} else {
			place.modifyTokensNumber(-arc.getWeight());
		}
	}
	
	public static void functionalAddition(Transition transition, Arc arc, Place place) {
		if(transition.isFunctional()) {
			FunctionContainer fc = transition.getFunctionContainer(arc);
			if(fc != null) {//czy to jest potrzebne? jeśli na początku symulacji wszystkie tranzycje zyskają te wektory?
				
				if(fc != null && fc.enabled == true && fc.correct == true) {
					fc.currentValue = getFunctionValue(fc); //wartość równania, ale:
					fc.currentValue = fc.currentValue <= 0 ? arc.getWeight() : fc.currentValue; //like a boss
					
					place.modifyTokensNumber((int) fc.currentValue);
				} else {
					place.modifyTokensNumber(arc.getWeight());
				}
			} else {
				place.modifyTokensNumber(arc.getWeight());
			}
		} else {
			place.modifyTokensNumber(arc.getWeight());
		}
	}
}
