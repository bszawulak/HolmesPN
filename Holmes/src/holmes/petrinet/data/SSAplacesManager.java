package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.Place;

/**
 * Klasa zarządzająca danymi do eksperymentów SSA.
 */
public class SSAplacesManager {
	private GUIManager overlord;
	private static LanguageManager lang = GUIManager.getLanguageManager();
	private PetriNet pn;
	
	private ArrayList<SSAplacesVector> ssaMatrix;
	public int selectedSSAvector = 0;
	
	/**
	 * Konstruktor obiektu klasy StatesManager
	 * @param net PetriNet - główny obiekt sieci
	 */
	public SSAplacesManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
		
		this.ssaMatrix = new ArrayList<SSAplacesVector>();
		ssaMatrix.add(new SSAplacesVector());
	}
	
	/**
	 * Dodaje wpis do wektorów SSA dla nowo utworzonego miejsca.
	 */
	public void addPlace() {
		for(SSAplacesVector pVector: ssaMatrix) {
			pVector.addPlace(0.0);
		}
	}
	
	/**
	 * Usuwa we wszystkich wektorach SSA dane o właśnie kasowanym miejscu.
	 * @param index (<b>int</b>) - indeks miejsca
	 * @return (<b>boolean</b>) - true, jeśli operacja przebiegła poprawnie
	 */
	public boolean removePlace(int index) {
		if(index >= ssaMatrix.get(0).getSize())
			return false;
		
		for(SSAplacesVector pVector: ssaMatrix) {
			boolean status = pVector.removePlace(index);
			if(!status) {
				overlord.log(lang.getText("LOGentry00376critErr"), "error", true);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Zwraca obiekt wektora stanu o zadanym indeksie.
	 * @param index (<b>int</b>) - nr stanu
	 * @return (<b>PlacesStateVector</b>) - obiekt wektora stanów
	 */
	public SSAplacesVector getSSAvector(int index) {
		if(index >= ssaMatrix.size())
			return null;
		else
			return ssaMatrix.get(index);
	}
	
	public SSAplacesVector getCurrentSSAvector() {
		return ssaMatrix.get(selectedSSAvector);
	}
	
	/**
	 * Metoda dodaje nowy wektor SSA sieci na bazie istniejącego w danej chwili w edytorze.
	 */
	public void addCurrentStateAsSSAvector() {
		SSAplacesVector pVector = new SSAplacesVector();
		for(Place place : pn.getPlaces()) {
			pVector.addPlace(place.getSSAvalue());
		}
		ssaMatrix.add(pVector);
	}
	
	/**
	 * Metoda czyści stany sieci i tworzy nowy pierwszy stan sieci.
	 */
	public void createCleanSSAvector() {
		reset(false);
		int placesNumber = pn.getPlacesNumber();
		
		for(int p=0; p<placesNumber; p++) {
			ssaMatrix.get(0).addPlace(0.0);
		}
	}
	
	/**
	 * Metoda ustawia nowy stan SSA miejsc sieci.
	 * @param stateID (<b>int</b>) - nr stanu z tablicy
	 */
	public void setNetworkSSAvector(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		SSAplacesVector psVector = ssaMatrix.get(stateID);
		for(int p=0; p<places.size(); p++) {
			Place place = places.get(p);
			place.setSSAvalue(psVector.getTokens(p));
		}
		selectedSSAvector = stateID;
	}
	
	/**
	 * Przywraca aktualnie wskazywany wektor SSA.
	 */
	public void restoreCurrentSSAvector() {
		ArrayList<Place> places = pn.getPlaces();
		SSAplacesVector psVector = ssaMatrix.get(selectedSSAvector);
		for(int p=0; p<places.size(); p++) {
			Place place = places.get(p);
			place.setSSAvalue(psVector.getTokens(p));
		}
	}
	
	/**
	 * Metoda służąca do usuwania wektorów SSA.
	 * @param stateID int - indeks stanu
	 */
	public void removeSSAvector(int stateID) {
		ssaMatrix.remove(stateID);
		selectedSSAvector = 0;
	}
	
	/**
	 * Zastępuje wskazany wektor aktualnym stanem sieci.
	 * @param stateID int - wskazany stan z tabeli
	 */
	public void replaceSSAvectorWithNetState(int stateID) {
		ArrayList<Place> places = pn.getPlaces();
		SSAplacesVector psVector = ssaMatrix.get(stateID);
		for(int p=0; p<places.size(); p++) {
			psVector.accessVector().set(p, places.get(p).getSSAvalue());
		}
	}
	
	/**
	 * Zwraca opis wektora SSA.
	 * @param selected int - nr wektora SSA
	 * @return String - opis
	 */
	public String getSSAvectorDescription(int selected) {
		return ssaMatrix.get(selected).getDescription();
	}
	
	/**
	 * Ustawia opis wektora SA.
	 * @param selected int - nr stanu sieci
	 * @param newText String - opis
	 */
	public void setSSAvectorDescription(int selected, String newText) {
		ssaMatrix.get(selected).setDescription(newText);
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy stanów.
	 * @return ArrayList[SSAplacesVector] - tablica stanów sieci
	 */
	public ArrayList<SSAplacesVector> accessSSAmatrix() {
		return this.ssaMatrix;
	}
	
	/**
	 * Metoda czyści tablicę stanów i ich nazw - tworzony jest nowy wektor SSA (pierwszy).
	 * @param isLoading boolean - jeśli true, nie tworzy pierwszych elementów (na potrzeby ProjectReader)
	 */
	public void reset(boolean isLoading) {
		ssaMatrix = new ArrayList<SSAplacesVector>();
		if(!isLoading) {
			ssaMatrix.add(new SSAplacesVector());
		}
		selectedSSAvector = 0;
	}
}
