package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;

/**
 * Klasa zarządzająca listą stanów sieci.
 * @author MR
 *
 */
public class StatePlacesManager {
	private final GUIManager overlord;
	private final PetriNet pn;
	
	private ArrayList<StatePlacesVector> statesMatrix;
	private ArrayList<StatePlacesVectorXTPN> statesMatrixXTPN;
	public int selectedStatePN = 0;
	public int selectedStateXTPN = 0;
	
	/**
	 * Konstruktor obiektu klasy StatesManager
	 * @param net PetriNet - główny obiekt sieci
	 */
	public StatePlacesManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
		
		this.statesMatrix = new ArrayList<>();
		statesMatrix.add(new StatePlacesVector());
		statesMatrix.get(0).setDescription("Default first (0) working state for current net.");

		this.statesMatrixXTPN = new ArrayList<>();
		statesMatrixXTPN.add(new StatePlacesVectorXTPN());
		statesMatrixXTPN.get(0).setDescription("Default first (0) working state for current XTPN net.");
	}
	
	/**
	 * Dodaje stan do wektorów stanów dla nowo utworzonego miejsca.
	 * @param place (<b>Place</b>) obiekt miejsca na potrzeby wektora XTPN.
	 */
	public void addPlace(Place place) {
		for(StatePlacesVector pVector: statesMatrix) {
			pVector.addPlace(0.0);
		}

		for(StatePlacesVectorXTPN pVector: statesMatrixXTPN) {
			pVector.addPlaceXTPN( new ArrayList<>(place.accessMultiset()) );
		}
	}
	
	/**
	 * Usuwa we wszystkich stanach dane o stanie właśnie kasowanego miejsca.
	 * @param index (<b>int</b>) indeks miejsca.
	 * @return (<b>boolean</b>) - true, jeśli operacja przebiegła poprawnie.
	 */
	public boolean removePlace(int index) {
		boolean statusPN = true;
		boolean statusXTPN = true;
		for(StatePlacesVector pVector: statesMatrix) {
			boolean status = pVector.removePlace(index);
			if(!status) {
				overlord.log("Critical error: invalid place index ("+index+") in states matrix.", "error", true);
				statusPN = false;
			}
		}
		for(StatePlacesVectorXTPN pVector: statesMatrixXTPN) {
			boolean status = pVector.removePlaceXTPN(index);
			if(!status) {
				overlord.log("Critical error: invalid XTPN place index ("+index+") in XTPN states matrix.", "error", true);
				statusXTPN = false;
			}
		}
		return statusPN && statusXTPN;
	}
	
	/**
	 * Zwraca obiekt wektora stanu zwykłej sieci o zadanym indeksie.
	 * @param index (<b>int</b>) indeks stanu.
	 * @return (<b>PlacesStateVector</b>) - obiekt wektora stanów.
	 */
	public StatePlacesVector getStatePN(int index) {
		if(index >= statesMatrix.size())
			return null;
		else
			return statesMatrix.get(index);
	}

	/**
	 * Zwraca obiekt wektora stanu sieci XTPN o zadanym indeksie.
	 * @param index (<b>int</b>) indeks stanu.
	 * @return (<b>PlacesStateVector</b>) - obiekt wektora stanów.
	 */
	public StatePlacesVectorXTPN getStateXTPN(int index) {
		if(index >= statesMatrixXTPN.size())
			return null;
		else
			return statesMatrixXTPN.get(index);
	}
	
	/**
	 * Zwraca obiekt wektora stanu normalnej sieci o aktualnie ustalonym indeksie.
	 * @return (<b>PlacesStateVector</b>) obiekt wektora stanów.
	 */
	public StatePlacesVector getCurrentStatePN() {
		return statesMatrix.get(selectedStatePN);
	}

	/**
	 * Zwraca obiekt wektora stanu sieci XTPN o aktualnie ustalonym indeksie.
	 * @return (<b>StatePlacesVectorXTPN</b>) obiekt wektora stanów XTPN.
	 */
	public StatePlacesVectorXTPN getCurrentStateXTPN() { //odpowienik PN działa w StateSimulator
		return statesMatrixXTPN.get(selectedStateXTPN);
	}
	
	/**
	 * Metoda dodaje nowy stan klasycznej sieci na bazie istniejącego w danej chwili w edytorze.
	 */
	public void addCurrentStatePN() {
		StatePlacesVector pVector = new StatePlacesVector();
		for(Place place : pn.getPlaces()) {
			pVector.addPlace(place.getTokensNumber());
		}
		statesMatrix.add(pVector);
	}

	/**
	 * Metoda dodaje nowy stan sieci XTPN na bazie istniejącego w danej chwili w edytorze.
	 */
	public void addCurrentStateXTPN() {
		StatePlacesVectorXTPN pVectorXTPN = new StatePlacesVectorXTPN();
		for(Place place : pn.getPlaces()) {
			pVectorXTPN.addPlaceXTPN(new ArrayList<>(place.accessMultiset()));
		}
		statesMatrixXTPN.add(pVectorXTPN);
	}
	
	/**
	 * Metoda dodaje nowy czysty stan sieci klasycznej - zero tokenów dla każdego miejsca.
	 */
	public void addNewCleanStatePN() { //przycisk okna
		StatePlacesVector pVector = new StatePlacesVector();
		int placesNumber = pn.getPlacesNumber();
		for(int p=0; p<placesNumber; p++) {
			pVector.addPlace(0);
		}
		statesMatrix.add(pVector);
	}

	/**
	 * Metoda dodaje nowy czysty stan sieci XTPN - pusty multizbiór bez tokenów.
	 */
	public void addNewCleanStateXTPN() { //przycisk okna
		StatePlacesVectorXTPN pVector = new StatePlacesVectorXTPN();
		for(int p=0; p<pn.getPlacesNumber(); p++) {
			pVector.addPlaceXTPN(new ArrayList<>());
		}
		statesMatrixXTPN.add(pVector);
	}

	/**
	 * Czyści stany i tworzy nowy pierwszy stan sieci. Jak dotąd używana tylko do wczytywania sieci, np. Snoopiego.
	 */
	public void createCleanStatePN() {
		resetPN(true); //czyść + dodaj czysty nowy wektor
		ArrayList<Place> places = pn.getPlaces();
		for(Place place : places) {
			statesMatrix.get(0).addPlace(place.getTokensNumber());
		}
	}

	/**
	 * Czyści stany i tworzy nowy pierwszy stan sieci XTPN.
	 */
	public void createCleanStateXTPN() {
		resetXTPN(true); //czyść + dodaj czysty nowy wektor
		ArrayList<Place> places = pn.getPlaces();
		for(Place place : places) {
			statesMatrixXTPN.get(0).addPlaceXTPN( new ArrayList<>(place.accessMultiset()) );
		}
	}
	
	/**
	 * Metoda ustawia nowy stan miejsc sieci na bazie wybranego stanu.
	 * @param stateID (<b>int</b>) indeks stanu z listy.
	 */
	public void setNetworkStatePN(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVector psVector = statesMatrix.get(stateID);
		for(int p=0; p<places.size(); p++) {
			Place place = places.get(p);
			place.setTokensNumber((int)psVector.getTokens(p));
			place.freeReservedTokens();
		}
		selectedStatePN = stateID;
	}

	/**
	 * Metoda ustawia nowy stan miejsc sieci XTPN na bazie wybranego stanu.
	 * @param stateID (<b>int</b>) indeks stanu z listy.
	 */
	public void setNetworkStateXTPN(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVectorXTPN psVector = statesMatrixXTPN.get(stateID);
		for(int p=0; p<places.size(); p++) {
			Place place = places.get(p);
			place.replaceMultiset(new ArrayList<>(psVector.getMultisetK(p)));
		}
		selectedStateXTPN = stateID;
	}

	/**
	 * Metoda służąca do usuwania stanów sieci normalnej.
	 * @param stateID (<b>int</b>) indeks stanu.
	 */
	public void removeStatePN(int stateID) {
		statesMatrix.remove(stateID);
		selectedStatePN = 0;
	}

	/**
	 * Metoda służąca do usuwania stanów XTPN sieci.
	 * @param stateID (<b>int</b>) indeks stanu XTPN.
	 */
	public void removeStateXTPN(int stateID) {
		statesMatrixXTPN.remove(stateID);
		selectedStateXTPN = 0;
	}
	
	/**
	 * Zastępuje wskazany stan aktualnym stanem sieci (normalnej).
	 * @param stateID (<b>int</b>) indeks wskazanego stanu z listy.
	 */
	public void replaceStateWithNetStatePN(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVector psVector = statesMatrix.get(stateID);
		for(int p=0; p<places.size(); p++) {
			psVector.accessVector().set(p, (double) places.get(p).getTokensNumber());
		}
	}

	/**
	 * Zastępuje wskazany stan aktualnym stanem sieci XTPN.
	 * @param stateID (<b>int</b>) indeks wskazanego stanu XTPN z listy.
	 */
	public void replaceStateWithNetStateXTPN(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVectorXTPN psVector = statesMatrixXTPN.get(stateID);
		psVector.accessVector().clear();

		for(int p=0; p<places.size(); p++) {
			ArrayList<Double> currentPlaceMultiset = new ArrayList<>(places.get(p).accessMultiset());
			psVector.addPlaceXTPN(currentPlaceMultiset);
		}
	}
	
	/**
	 * Zwraca opis stanu normalnej sieci.
	 * @param selected (<b>int</b>) indeks stanu sieci normalnej.
	 * @return (<b>String</b>) opis stanu.
	 */
	public String getStateDescriptionPN(int selected) {
		return statesMatrix.get(selected).getDescription();
	}

	/**
	 * Zwraca opis stanu sieci XTPN.
	 * @param selected (<b>int</b>) indeks stanu sieci XTPN.
	 * @return (<b>String</b>) opis stanu.
	 */
	public String getStateDescriptionXTPN(int selected) {
		return statesMatrixXTPN.get(selected).getDescription();
	}
	
	/**
	 * Ustawia opis stanu sieci normalnej.
	 * @param selected (<b>int</b>) indeks stanu sieci normalnej.
	 * @param newText (<b>String</b>) opis stanu.
	 */
	public void setStateDescriptionPN(int selected, String newText) {
		statesMatrix.get(selected).setDescription(newText);
	}

	/**
	 * Ustawia opis stanu sieci XTPN.
	 * @param selected (<b>int</b>) indeks stanu sieci XTPN.
	 * @param newText (<b>String</b>) opis stanu.
	 */
	public void setStateDescriptionXTPN(int selected, String newText) {
		statesMatrixXTPN.get(selected).setDescription(newText);
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy stanów.
	 * @return (<b>ArrayList[PlacesStateVector]</b>) tablica stanów sieci.
	 */
	public ArrayList<StatePlacesVector> accessStateMatrix() {
		return this.statesMatrix;
	}

	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy stanów XTPN.
	 * @return (<b>ArrayList[StatePlacesVectorXTPN]</b>) tablica stanów sieci XTPN.
	 */
	public ArrayList<StatePlacesVectorXTPN> accessStateMatrixXTPN() {
		return this.statesMatrixXTPN;
	}

	/**
	 * Metoda czyści tablicę stanów i ich nazw- tworzony jest nowy stan pierwszy.
	 * @param createFirstVector (<b>boolean</b>) jeśli false, nie tworzy pierwszych elementów (na potrzeby <b>ProjectReader</b>)
	 */
	public void resetPN(boolean createFirstVector) {
		statesMatrix = new ArrayList<StatePlacesVector>();
		if(createFirstVector) {
			statesMatrix.add(new StatePlacesVector());
			statesMatrix.get(0).setDescription("Default first (0) working state for current net.");
		}
		selectedStatePN = 0;
	}

	/**
	 * Metoda czyści tablicę stanów i ich nazw- tworzony jest nowy stan pierwszy dla XTPN.
	 * @param createFirstVector (<b>boolean</b>) jeśli false, nie tworzy pierwszych elementów (na potrzeby <b>ProjectReader</b>)
	 */
	public void resetXTPN(boolean createFirstVector) {
		statesMatrixXTPN = new ArrayList<StatePlacesVectorXTPN>();
		if(createFirstVector) {
			statesMatrixXTPN.add(new StatePlacesVectorXTPN());
			statesMatrixXTPN.get(0).setDescription("Default first (0) working XTPN state for current net.");
		}
		selectedStateXTPN = 0;
	}
}
