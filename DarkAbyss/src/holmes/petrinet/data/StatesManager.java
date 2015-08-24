package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;

/**
 * Klasa zarządzająca listą stanów sieci.
 * 
 * @author MR
 *
 */
public class StatesManager {
	private GUIManager overlord;
	private PetriNet pn;
	
	private ArrayList<PlacesStateVector> statesMatrix;
	private ArrayList<String> statesNames;
	public int currentState = 0;
	
	/**
	 * Konstruktor obiektu klasy StatesManager
	 * @param net PetriNet - główny obiekt sieci
	 */
	public StatesManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
		
		this.statesMatrix = new ArrayList<PlacesStateVector>();
		statesMatrix.add(new PlacesStateVector());
		
		this.statesNames = new ArrayList<String>();
		statesNames.add("Default name for state 1.");
	}
	
	/**
	 * Dodaje wpis do wektorów stanów dla nowo utworzonego miejsca.
	 */
	public void addPlace() {
		for(PlacesStateVector pVector: statesMatrix) {
			pVector.addPlace(0.0);
		}
	}
	
	/**
	 * Usuwa we wszystkich stanach dane o właśnie kasowanym miejscu.
	 * @param index int - indeks miejsca
	 * @return boolean - true, jeśli operacja przebiegła poprawnie
	 */
	public boolean removePlace(int index) {
		if(index >= statesMatrix.get(0).getSize())
			return false;
		
		for(PlacesStateVector pVector: statesMatrix) {
			boolean status = pVector.removePlace(index);
			if(!status) {
				overlord.log("Critical error: invalid place index in states matrix.", "error", true);
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Zwraca obiekt wektora stanu o zadanym indeksie.
	 * @param index int - nr stanu
	 * @return PlacesStateVector - obiekt wektora stanów
	 */
	public PlacesStateVector getState(int index) {
		if(index >= statesMatrix.size())
			return null;
		else
			return statesMatrix.get(index);
	}
	
	/**
	 * Metoda dodaje nowy stan sieci na bazie istniejącego w danej chwili w edytorze.
	 */
	public void addNewState() {
		PlacesStateVector pVector = new PlacesStateVector();
		for(Place place : pn.getPlaces()) {
			pVector.addPlace(place.getTokensNumber());
		}
		statesMatrix.add(pVector);
		statesNames.add("Default name for state "+statesMatrix.size());
	}
	
	/**
	 * Metoda czyści stany sieci i tworzy nowy pierwszy stan sieci.
	 */
	public void createCleanState() {
		reset(false);
		for(Place place : pn.getPlaces()) {
			statesMatrix.get(0).addPlace(place.getTokensNumber());
		}
		statesNames.set(0, "Default name for state "+statesMatrix.size());
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy stanów.
	 * @return ArrayList[PlacesStateVector] - tablica stanów sieci
	 */
	public ArrayList<PlacesStateVector> accessStateMatrix() {
		return this.statesMatrix;
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy nazw stanów.
	 * @return ArrayList[PlacesStateVector] - tablica nazw stanów sieci
	 */
	public ArrayList<String> accessStateNames() {
		return this.statesNames;
	}
	
	/**
	 * Metoda czyści tablicę stanów i ich nazw- tworzony jest nowy stan pierwszy.
	 * @param isLoading boolean - jeśli true, nie tworzy pierwszych elementów (na potrzeby ProjectReader)
	 */
	public void reset(boolean isLoading) {
		if(isLoading) {
			statesMatrix = new ArrayList<PlacesStateVector>();
			statesNames = new ArrayList<String>();
		} else {
			statesMatrix = new ArrayList<PlacesStateVector>();
			statesMatrix.add(new PlacesStateVector());
			statesNames = new ArrayList<String>();
			statesNames.add("Default name for state 1.");
		}
	}
}
