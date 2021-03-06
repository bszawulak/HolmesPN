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
public class StatePlacesManager {
	private GUIManager overlord;
	private PetriNet pn;
	
	private ArrayList<StatePlacesVector> statesMatrix;
	public int selectedState = 0;
	
	/**
	 * Konstruktor obiektu klasy StatesManager
	 * @param net PetriNet - główny obiekt sieci
	 */
	public StatePlacesManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
		
		this.statesMatrix = new ArrayList<StatePlacesVector>();
		statesMatrix.add(new StatePlacesVector());
		statesMatrix.get(0).setDescription("Default first (0) working state for current net editing. "
					+ "For analytical purposes please use new states 1, 2 or higher.");
	}
	
	/**
	 * Dodaje wpis do wektorów stanów dla nowo utworzonego miejsca.
	 */
	public void addPlace() {
		for(StatePlacesVector pVector: statesMatrix) {
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
		
		for(StatePlacesVector pVector: statesMatrix) {
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
	public StatePlacesVector getState(int index) {
		if(index >= statesMatrix.size())
			return null;
		else
			return statesMatrix.get(index);
	}
	
	/**
	 * Zwraca obiekt wektora stanu o aktualnie ustalonym indeksie.
	 * @return PlacesStateVector - obiekt wektora stanów
	 */
	public StatePlacesVector getCurrentState() {
		return statesMatrix.get(selectedState);
	}
	
	/**
	 * Metoda dodaje nowy stan sieci na bazie istniejącego w danej chwili w edytorze.
	 */
	public void addCurrentState() {
		StatePlacesVector pVector = new StatePlacesVector();
		for(Place place : pn.getPlaces()) {
			pVector.addPlace(place.getTokensNumber());
		}
		statesMatrix.add(pVector);
	}
	
	/**
	 * Metoda dodaje nowy czysty stan sieci.
	 */
	public void addNewCleanState() {
		StatePlacesVector pVector = new StatePlacesVector();
		int placesNumber = pn.getPlacesNumber();
		for(int p=0; p<placesNumber; p++) {
			pVector.addPlace(0);
		}
		statesMatrix.add(pVector);
	}
	
	/**
	 * Metoda czyści stany sieci i tworzy nowy pierwszy stan sieci.
	 */
	public void createCleanState() {
		reset(false);
		ArrayList<Place> places = pn.getPlaces();
		for(Place place : places) {
			statesMatrix.get(0).addPlace(place.getTokensNumber());
		}
	}
	
	/**
	 * Metoda ustawia nowy stan miejsc sieci.
	 * @param stateID int - nr stanu z tablicy
	 */
	public void setNetworkState(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVector psVector = statesMatrix.get(stateID);
		for(int p=0; p<places.size(); p++) {
			Place place = places.get(p);
			place.setTokensNumber((int)psVector.getTokens(p));
			place.freeReservedTokens();
		}
		selectedState = stateID;
	}
	
	/**
	 * Przywraca aktualnie wskazywany stan m0.
	 */
	public void restoreSelectedState() {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVector psVector = statesMatrix.get(selectedState);
		for(int p=0; p<places.size(); p++) {
			Place place = places.get(p);
			place.setTokensNumber((int)psVector.getTokens(p));
			place.freeReservedTokens();
		}
	}
	
	/**
	 * Metoda służąca do usuwania stanów sieci.
	 * @param stateID int - indeks stanu
	 */
	public void removeState(int stateID) {
		statesMatrix.remove(stateID);
		selectedState = 0;
	}
	
	/**
	 * Zastępuje wskazany stan aktualnym stanem sieci.
	 * @param stateID int - wskazany stan z tabeli
	 */
	public void replaceStateWithNetState(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVector psVector = statesMatrix.get(stateID);
		for(int p=0; p<places.size(); p++) {
			psVector.accessVector().set(p, (double) places.get(p).getTokensNumber());
		}
	}
	
	/**
	 * Zwraca opis stanu.
	 * @param selected int - nr stanu sieci
	 * @return String - opis
	 */
	public String getStateDescription(int selected) {
		return statesMatrix.get(selected).getDescription();
	}
	
	/**
	 * Ustawia opis stanu sieci.
	 * @param selected int - nr stanu sieci
	 * @param newText String - opis
	 */
	public void setStateDescription(int selected, String newText) {
		statesMatrix.get(selected).setDescription(newText);;
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy stanów.
	 * @return ArrayList[PlacesStateVector] - tablica stanów sieci
	 */
	public ArrayList<StatePlacesVector> accessStateMatrix() {
		return this.statesMatrix;
	}

	/**
	 * Metoda czyści tablicę stanów i ich nazw- tworzony jest nowy stan pierwszy.
	 * @param isLoading boolean - jeśli true, nie tworzy pierwszych elementów (na potrzeby ProjectReader)
	 */
	public void reset(boolean isLoading) {
		statesMatrix = new ArrayList<StatePlacesVector>();
		if(isLoading == false) {
			statesMatrix.add(new StatePlacesVector());
			statesMatrix.get(0).setDescription("Default first (0) working state for current net editing. "
					+ "For analytical purposes please use new states 1, 2 or higher.");
		}
		selectedState = 0;
	}
}
