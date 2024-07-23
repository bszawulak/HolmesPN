package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIController;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.PlaceXTPN;

/**
 * Klasa zarządzająca listą stanów sieci.
 */
public class P_StateManager {
	private final GUIManager overlord;
	private static LanguageManager lang = GUIManager.getLanguageManager();
	private final PetriNet pn;
	private ArrayList<StatePlacesVector> statesMatrix;
	private ArrayList<MultisetM> statesMatrixXTPN;
	public int selectedStatePN = 0;
	public int selectedStateXTPN = 0;
	
	/**
	 * Konstruktor obiektu klasy StatesManager
	 * @param net PetriNet - główny obiekt sieci
	 */
	public P_StateManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
		
		this.statesMatrix = new ArrayList<>();
		statesMatrix.add(new StatePlacesVector());
		statesMatrix.get(0).setDescription(lang.getText("PSM_entry001"));

		this.statesMatrixXTPN = new ArrayList<>();
		statesMatrixXTPN.add(new MultisetM());
		statesMatrixXTPN.get(0).setDescription(lang.getText("PSM_entry001"));
	}
	
	/**
	 * Dodaje stan do wektorów stanów dla nowo utworzonego miejsca.
	 * @param place (<b>Place</b>) obiekt miejsca na potrzeby wektora XTPN.
	 */
	public void addPlace(Place place) {
		for(StatePlacesVector pVector: statesMatrix) {
			pVector.addPlace(0.0);
		}
		if(place instanceof PlaceXTPN) {
			for(MultisetM multisetM: statesMatrixXTPN) {
				if( ((PlaceXTPN)place).isGammaModeActive())
					multisetM.addMultiset_K_toMultiset_M( new ArrayList<>( ((PlaceXTPN)place).accessMultiset()), 1 );
				else
					multisetM.addMultiset_K_toMultiset_M( new ArrayList<>( ((PlaceXTPN)place).accessMultiset()), 0 );
			}
		}
	}
	
	/**
	 * Usuwa we wszystkich stanach dane o stanie właśnie kasowanego miejsca.
	 * Wspólna metoda dla sieci zwykłych i XTPN.
	 * @param index (<b>int</b>) indeks miejsca.
	 */
	public void removePlace(int index) {
		for(StatePlacesVector pVector: statesMatrix) {
			boolean status = pVector.removePlace(index);
			if(!status) {
				String strB = String.format(lang.getText("LOGentry00341critError"), index);
				overlord.log(strB, "error", true);
			}
		}
		if(GUIController.access().getCurrentNetType() == PetriNet.GlobalNetType.XTPN) {
			for(MultisetM multisetM: statesMatrixXTPN) {
				boolean status = multisetM.removePlaceFromMultiset_M(index);
				if(!status) {
					String strB = String.format(lang.getText("LOGentry00342critErr"), index);
					overlord.log(strB, "error", true);
				}
			}
		}
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
	 * Zwraca obiekt wektora stanu normalnej sieci o aktualnie ustalonym indeksie.
	 * @return (<b>PlacesStateVector</b>) obiekt wektora stanów.
	 */
	public StatePlacesVector getCurrentStatePN() {
		return statesMatrix.get(selectedStatePN);
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
	 * Metoda ustawia nowy stan miejsc sieci na bazie wybranego stanu.
	 * @param stateID (<b>int</b>) indeks stanu z listy.
	 */
	public void setNetworkStatePN(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVector psVector = statesMatrix.get(stateID);
		for(int placeIndex=0; placeIndex<places.size(); placeIndex++) {
			Place place = places.get(placeIndex);
			place.setTokensNumber((int)psVector.getTokens(placeIndex));
			place.freeReservedTokens();
		}
		selectedStatePN = stateID;
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
	 * Nadpisuje wskazany stan przechowywany w managerze aktualnym stanem sieci (normalnej).
	 * @param stateID (<b>int</b>) indeks wskazanego stanu z listy.
	 */
	public void replaceStoredStateWithNetStatePN(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		StatePlacesVector psVector = statesMatrix.get(stateID);
		for(int p=0; p<places.size(); p++) {
			psVector.accessVector().set(p, (double) places.get(p).getTokensNumber());
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
	 *  Ustawia opis stanu sieci XTPN.
	 * @param selected (<b>int</b>) indeks stanu sieci normalnej.
	 * @param newText (<b>String</b>) nowy opis stanu.
	 */
	public void setStateDescriptionPN(int selected, String newText) {
		statesMatrix.get(selected).setDescription(newText);
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy stanów.
	 * @return (<b>ArrayList[PlacesStateVector]</b>) tablica stanów sieci.
	 */
	public ArrayList<StatePlacesVector> accessStateMatrix() {
		return this.statesMatrix;
	}

	/**
	 * Metoda czyści tablicę stanów i ich nazw- tworzony jest nowy stan pierwszy.
	 * @param createFirstVector (<b>boolean</b>) jeśli false, nie tworzy pierwszych elementów (na potrzeby <b>ProjectReader</b>)
	 */
	public void resetPN(boolean createFirstVector) {
		statesMatrix = new ArrayList<>();
		if(createFirstVector) {
			statesMatrix.add(new StatePlacesVector());
			statesMatrix.get(0).setDescription("Default first (0) working state for current net.");
		}
		selectedStatePN = 0;
	}

	// ********************************************************************************************
	// **************************************              ****************************************
	// **************************************     XTPN     ****************************************
	// **************************************              ****************************************
	// ********************************************************************************************

	/**
	 * Zwraca multizbiór M o zadanym indeksie z przechowywanych w managerze stanów.
	 * @param index (<b>int</b>) indeks przechowywanego stanu.
	 * @return (<b>PlacesStateVector</b>) - multizbiór M.
	 */
	public MultisetM getMultiset_M(int index) {
		if(index >= statesMatrixXTPN.size())
			return null;
		else
			return statesMatrixXTPN.get(index);
	}

	/**
	 * Metoda dodaje nowy stan sieci XTPN (multizbiór M) na bazie istniejącego w danej chwili p-stanu aktualnej sieci.
	 */
	public void createNewMultiset_M_basedOnNet() {
		MultisetM multisetM = new MultisetM();
		for(Place place : pn.getPlaces()) {
			if( !(place instanceof PlaceXTPN) ) {
				overlord.log(lang.getText("LOGentry00343critErr"), "error", true);
				return;
			}

			if( ((PlaceXTPN)place).isGammaModeActive())
				multisetM.addMultiset_K_toMultiset_M(new ArrayList<>( ((PlaceXTPN)place).accessMultiset()), 1);
			else {
				int tokens = place.getTokensNumber();
				ArrayList<Double> fakeMultiset = new ArrayList<>();
				fakeMultiset.add((double) tokens);
				multisetM.addMultiset_K_toMultiset_M(fakeMultiset, 0);
			}
			//multisetM.addPlaceToMultiset_M(new ArrayList<>(place.accessMultiset()), 0);
		}
		statesMatrixXTPN.add(multisetM);
	}

	/**
	 * Metoda dodaje nowy czysty stan sieci XTPN - multizbiór M z czystymi (bez tokenów) multizbiorami K.
	 */
	public void addNewCleanMultiset_M() { //przycisk okna
		MultisetM multisetM = new MultisetM();
		for(int placeID = 0; placeID<pn.getPlacesNumber(); placeID++) {
			multisetM.addMultiset_K_toMultiset_M(new ArrayList<>(), 1);
			//nowy stan tylko dla miejsc XTPN, TODO?
		}
		statesMatrixXTPN.add(multisetM);
	}

	/**
	 * Czyści stany i tworzy nowy pierwszy stan sieci XTPN (multizbiór M).
	 */
	public void createFirstMultiset_M() {
		removeAllMultisets_M(true); //czyść + dodaj czysty nowy wektor
		ArrayList<Place> places = pn.getPlaces();
		for(Place place : places) {
			if( !(place instanceof PlaceXTPN) ) {
				overlord.log(lang.getText("LOGentry00344critErr"), "error", true);
				return;
			}

			if( ((PlaceXTPN)place).isGammaModeActive())
				statesMatrixXTPN.get(0).addMultiset_K_toMultiset_M( new ArrayList<>( ((PlaceXTPN)place).accessMultiset()), 1 );
			else
				statesMatrixXTPN.get(0).addMultiset_K_toMultiset_M( new ArrayList<>( ((PlaceXTPN)place).accessMultiset()), 0 );
		}
	}

	/**
	 * Metoda nadpisuje aktualny stan miejsc w sieci XTPN na bazie wybranego multizbioru M z managera stanów.
	 * @param stateID (<b>int</b>) indeks multizbioru M.
	 * @return (<b>boolean</b>) - true, jeżeli się udało.
	 */
	public boolean replaceNetStateWithSelectedMultiset_M(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		MultisetM multisetM = statesMatrixXTPN.get(stateID);
		if(multisetM.getMultiset_M_Size() == places.size()) {
			for (int placeIndex = 0; placeIndex < places.size(); placeIndex++) {
				Place place = places.get(placeIndex);
				if( !(place instanceof PlaceXTPN) ) {
					overlord.log(lang.getText("LOGentry00345critErr"), "error", true);
					return false;
				}

				if(multisetM.isPlaceStoredAsGammaActive(placeIndex)) { //jeśli w managerze miejsce przechowywane jest jako XTPN
					((PlaceXTPN)place).setGammaModeStatus(true);
					((PlaceXTPN)place).replaceMultiset( new ArrayList<>(multisetM.accessMultiset_K(placeIndex)) );
					place.setTokensNumber( multisetM.accessMultiset_K(placeIndex).size() );
				} else { //jeśli w managerze miejsce jest przechowywane jako klasyczne
					((PlaceXTPN)place).setGammaModeStatus(false);
					((PlaceXTPN)place).accessMultiset().clear();
					//place.replaceMultiset( new ArrayList<>(multisetM.accessMultiset_K(placeIndex)) );
					double tokensNo = multisetM.accessMultiset_K(placeIndex).get(0);
					place.setTokensNumber( (int)tokensNo );

				}
			}
			selectedStateXTPN = stateID;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Usuwa wskazany stan sieci - multizbiór M przechowywany w managerze.
	 * @param stateID (<b>int</b>) indeks p-stanu XTPN.
	 */
	public void removeMultiset_M(int stateID) {
		statesMatrixXTPN.remove(stateID);
		selectedStateXTPN = 0;
	}

	/**
	 * Nadpisuje wskazany multizbiór M przechowywany w managerze aktualnym stanem sieci XTPN.
	 * @param stateID (<b>int</b>) indeks wskazanego stanu XTPN z listy.
	 */
	public void replaceStoredMultiset_M_withCurrentNetState(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		MultisetM multisetM = statesMatrixXTPN.get(stateID);
		multisetM.accessArrayListSOfMultiset_M().clear();
		multisetM.accessPlacesGammaVector().clear();

		for (Place place : places) {
			if( !(place instanceof PlaceXTPN) ) {
				overlord.log(lang.getText("LOGentry00346critErr"), "error", true);
				return;
			}

			ArrayList<Double> currentPlaceMultiset = new ArrayList<>( ((PlaceXTPN)place).accessMultiset());
			int placeTag = 1; //zakładamy, że miejsce czasowe
			if( !((PlaceXTPN)place).isGammaModeActive() ) { //w przypadku gdy klasyczne, jedyna liczba w multizbiorze to liczba tokenów klasycznych
				currentPlaceMultiset.clear();
				currentPlaceMultiset.add((double)place.getTokensNumber());
				placeTag = 0; //jednak miejsce zwykłe
			}
			multisetM.addMultiset_K_toMultiset_M(currentPlaceMultiset, placeTag);
		}
	}

	/**
	 * Zwraca opis wskazanego multizbioru M.
	 * @param selected (<b>int</b>) indeks stanu sieci XTPN.
	 * @return (<b>String</b>) opis multizbioru M.
	 */
	public String getMultiset_M_Description(int selected) {
		return statesMatrixXTPN.get(selected).getDescription();
	}

	/**
	 * Ustawia opis wskazanego multizbioru M.
	 * @param selected (<b>int</b>) indeks stanu sieci XTPN.
	 * @param newText (<b>String</b>) nowy opis multizbioru M.
	 */
	public void setMultiset_M_Description(int selected, String newText) {
		statesMatrixXTPN.get(selected).setDescription(newText);
	}

	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy stanów XTPN.
	 * @return (<b>ArrayList[StatePlacesVectorXTPN]</b>) tablica stanów sieci XTPN.
	 */
	public ArrayList<MultisetM> accessStateMatrixXTPN() {
		return this.statesMatrixXTPN;
	}

	/**
	 * Metoda czyści tablicę stanów i ich nazw - tworzony jest nowy stan pierwszy dla XTPN.
	 * @param createFirstVector (<b>boolean</b>) jeśli false, nie tworzy pierwszych elementów (na potrzeby <b>ProjectReader</b>)
	 */
	public void removeAllMultisets_M(boolean createFirstVector) {
		statesMatrixXTPN = new ArrayList<>();
		if(createFirstVector) {
			statesMatrixXTPN.add(new MultisetM());
			statesMatrixXTPN.get(0).setDescription("Default first (0) working XTPN state for current net.");
		}
		selectedStateXTPN = 0;
	}
}
