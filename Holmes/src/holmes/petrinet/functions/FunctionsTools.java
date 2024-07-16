package holmes.petrinet.functions;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;

import holmes.petrinet.elements.PlaceXTPN;
import org.nfunk.jep.JEP;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.windows.HolmesNotepad;

/**
 * Klasa metod zarządzania tranzycjami funkcyjnymi.
 */
public class FunctionsTools {
	private static GUIManager overlord = GUIManager.getDefaultGUIManager();
	
	/**
	 * Konstruktor obiektów klasy FunctionsTools.
	 */
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
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
	
		for(Transition transition : transitions) {
			for(FunctionContainer fc : transition.fpnExtension.accessFunctionsList()) {
				if(fc.involvedPlaces.containsKey("p"+placeIndex)) {
					int transIndex = transitions.indexOf(transition);
					overlord.log("Function: '"+fc.simpleExpression+"' (fID: "+fc.fID+") of transition t"+transIndex+
							" has been disabled due to removal of place p"+placeIndex, "warning", true);
					
					fc.enabled = false;
					fc.correct = false;
					fc.simpleExpression = fc.simpleExpression.replaceAll("p"+placeIndex, " ??? ");
					fc.involvedPlaces.remove("p"+placeIndex);
					fc.currentValue = -1;
					removedAnything = true;
				}
			}
		}
		return removedAnything;
	}
	
	/**
	 * Metoda synchronizuje równanie z wektorem miejsc używanych przez równanie. Nieistniejące w sieci miejsca
	 * nie są brane pod uwagę w wektorze miejsc, są też usuwane z równania w razie detekcji.
	 * @param fc FunctionContainer - obiekt równania
	 * @param commentField JTextArea - log podokna funkcji, do wyświetlania komunikatów, null jeśli nie ma być żadnych
	 * @param places ArrayList[Place] - wektor miejsc sieci
	 */
	public static void resetPlaceVector(FunctionContainer fc, JTextArea commentField, ArrayList<Place> places) {
		int placesNumber = places.size();
		fc.involvedPlaces.clear();
		Pattern p = Pattern.compile("p\\d+");
		Matcher m = p.matcher(fc.simpleExpression);
		ArrayList<String> foundPlaces = new ArrayList<>();
		while (m.find()) {
			String x = m.group();
			foundPlaces.add(x);
			int index = Integer.parseInt(x.substring(1));
			if(index >= placesNumber || index < 0) {
				fc.simpleExpression = fc.simpleExpression.replace(x, " ??? ");
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
			if(!transition.fpnExtension.isFunctional())
				continue;
			
			for(FunctionContainer fc : transition.fpnExtension.accessFunctionsList()) {
				if(!fc.enabled)
					continue;
				
				resetPlaceVector(fc, null, places);
				if(fc.simpleExpression.contains("???") || fc.simpleExpression.length()==0) {
					errorsFlag = true;
					fc.enabled = false;
					fc.correct = false;
					if(logErrors)
						notepad.addTextLineNL("t"+transCounter+" : "+ fc, "text");
				}
				
				try {
					JEP myParser = new JEP();
					myParser.addStandardFunctions();
					for(String key : fc.involvedPlaces.keySet()) {
						Place place = fc.involvedPlaces.get(key);
						myParser.addVariable(key, place.getTokensNumber());
					}

					myParser.parseExpression(fc.simpleExpression);
					if(myParser.hasError()) {
						HolmesNotepad note = new HolmesNotepad(640, 480);
						note.setVisible(true);
						notepad.addTextLineNL("  variables initialization error (function disabled):", "text");
						note.addTextLineNL("    * "+myParser.getErrorInfo(), "text");
						fc.enabled = false;
						fc.correct = false;
					} else {
						fc.correct = true;
					}
				} catch (Exception e) {
					errorsFlag = true;
					notepad.addTextLineNL("   CRITICAL ERROR WHILE INITIALIZATION. FUNCTION DISABLED.", "text");
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
		
		fc.simpleExpression = newEquation;
		resetPlaceVector(fc, commentField, places);
		if(fc.simpleExpression.contains("???") || fc.simpleExpression.length()==0)
			return false;
		
		try {
			JEP myParser = new JEP();
			myParser.addStandardFunctions();
			for(String key : fc.involvedPlaces.keySet()) {
				Place place = fc.involvedPlaces.get(key);
				myParser.addVariable(key, place.getTokensNumber());
			}

			myParser.parseExpression(fc.simpleExpression);
			boolean errors = myParser.hasError();
			if(errors) {
				HolmesNotepad note = new HolmesNotepad(640, 480);
				note.setVisible(true);
				note.addTextLineNL(myParser.getErrorInfo(), "text");
				
				if(commentField != null) {
					commentField.append(myParser.getErrorInfo()+"\n");
				}
				
				fc.enabled = false;
				fc.correct = false;
			} else {
				fc.correct = true;
			}
			
			return (!errors);
		} catch (Exception e) {
			commentField.append("Function creation critically failed for: "+fc.simpleExpression+"\n");
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
			FunctionContainer fc = transition.fpnExtension.getFunctionContainer(arc);
			if(fc != null) { // jeśli znaleziono, to od razu przypisujemy oryginalną wagę
				fc.currentValue = nonFuncWeight; //wartość początkowa: oryginalna waga
			}
			
			if(fc != null && fc.enabled && fc.correct) {
				fc.currentValue = getFunctionValue(fc); //wartość równania, ale:
				fc.currentValue = fc.currentValue <= 0 ? nonFuncWeight : fc.currentValue; //like a boss
				return !(startPlaceTokens < fc.currentValue);
				
			} else { //oryginalna waga przypisana do fc.currentValue tak czy inaczej (ważne!)
				return startPlaceTokens >= arc.getWeight();
			}
		} catch (Exception e) {
			//jak coś wybuchło, sprawdzamy po staremu:
			return startPlaceTokens >= arc.getWeight();
		}
	}
	

	/**
	 * Metoda obliczająca wartość równania funkcji, na bazie tokenów w miejsach użytych w równaniu.
	 * @param fc (<b>FunctionContainer</b>) kontener funkcji.
	 * @return (<b>double</b>) - wartość funkcji lub -1 jeśli coś nie wyszło.
	 */
	private static double getFunctionValue(FunctionContainer fc) {
		try {
			JEP myParser = new JEP();
			myParser.addStandardFunctions();
			for(String key : fc.involvedPlaces.keySet()) {
				Place place = fc.involvedPlaces.get(key);

				if(place instanceof PlaceXTPN) {
					if( ((PlaceXTPN)place).isGammaModeActive() ) {
						myParser.addVariable(key, ((PlaceXTPN)place).accessMultiset().size() );
					} else {
						myParser.addVariable(key, place.getTokensNumber());
					}
				} else {
					myParser.addVariable(key, place.getTokensNumber());
				}
			}
			myParser.parseExpression(fc.simpleExpression);
			double result = myParser.getValue();
			return result;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Parsing equation failed for "+fc.simpleExpression, "error", true);
			return -1;
		}
	}
	
	/**
	 * Metoda odpowiedzialna za pobieranie tokenów z miejsca z uwzględnieniem funkcji łuków.
	 * @param transition Transition - tranzycja funkcyjna
	 * @param arc Arc - łuk z funkcją
	 * @param place Place - miejsce
	 */
	public static void functionalExtraction(Transition transition, Arc arc, Place place) {
		if(transition.fpnExtension.isFunctional()) {
			FunctionContainer fc = transition.fpnExtension.getFunctionContainer(arc);
			if(fc != null) //TODO: czy to jest potrzebne? jeśli na początku symulacji wszystkie tranzycje zyskają te wektory?
			{
				fc.currentValue = getFunctionValue(fc); //wartość równania, ale:
				fc.currentValue = fc.currentValue <= 0 ? arc.getWeight() : fc.currentValue; //like a boss
				place.addTokensNumber(-((int) fc.currentValue));
				//nie ważne, aktywna czy nie, jeśli nie, to tu jest i tak oryginalna waga
			} else {
				place.addTokensNumber(-arc.getWeight());
			}
		} else {
			place.addTokensNumber(-arc.getWeight());
		}
	}

	/**
	 * Na potrzeby symulatora XTPN, na bazie functionalExtraction.
	 * @return int
	 */
	public static int getFunctionalArcWeight(Transition transition, Arc arc, Place place) {
		if(transition.fpnExtension.isFunctional()) {
			FunctionContainer fc = transition.fpnExtension.getFunctionContainer(arc);

			if(fc != null && fc.enabled && fc.correct) {
				fc.currentValue = getFunctionValue(fc); //wartość równania, ale:
				fc.currentValue = fc.currentValue <= 0 ? arc.getWeight() : fc.currentValue; //like a boss
				return (int) fc.currentValue;
			} else {
				return arc.getWeight();
			}
		} else {
			return arc.getWeight();
		}
	}
	
	/**
	 * Metoda odpowiedzialna za dodawanie tokenów do miejsca z uwzględnieniem funkcji łuków.
	 * @param transition Transition - tranzycja funkcyjna
	 * @param arc Arc - łuk z funkcją
	 * @param place Place - miejsce
	 */
	public static void functionalAddition(Transition transition, Arc arc, Place place) {
		if(transition.fpnExtension.isFunctional()) {
			FunctionContainer fc = transition.fpnExtension.getFunctionContainer(arc);
			if(fc != null) {//czy to jest potrzebne? jeśli na początku symulacji wszystkie tranzycje zyskają te wektory?
				if(fc != null && fc.enabled && fc.correct) {
					fc.currentValue = getFunctionValue(fc); //wartość równania, ale:
					fc.currentValue = fc.currentValue <= 0 ? arc.getWeight() : fc.currentValue; //like a boss
					
					place.addTokensNumber((int) fc.currentValue);
				} else {
					place.addTokensNumber(arc.getWeight());
				}
			} else {
				place.addTokensNumber(arc.getWeight());
			}
		} else {
			place.addTokensNumber(arc.getWeight());
		}
	}
}
