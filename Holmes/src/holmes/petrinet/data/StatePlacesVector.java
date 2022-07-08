package holmes.petrinet.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;

/**
 * Klasa zarządzająca stanem sieci klasycznej, tj. liczbą tokenów w miejscach.
 * @author MR
 *
 */
public class StatePlacesVector implements Serializable {
	@Serial
	private static final long serialVersionUID = 6652562026923360610L;
	private ArrayList<Double> stateVector;
	private String stateType;
	private String stateDescription;
	
	/**
	 * Konstruktor obiektu klasy StatePlacesVector.
	 */
	public StatePlacesVector() {
		stateVector = new ArrayList<Double>();
		stateType = "NORMAL";
		stateDescription = "Default description for state.";
	}
	
	/**
	 * Dodaje nowe miejsce z zadaną liczbą tokenów do wektora.
	 * @param value double - liczba tokenów
	 */
	public void addPlace(double value) {
		stateVector.add(value);
	}
	
	/**
	 * Usuwa lokalizację właśnie kasowanego miejsca z wektora tokenów.
	 * @param index (<b>int</b>) indeks miejsca.
	 * @return (<b>boolean</b>) - true, jeśli operacja się udała.
	 */
	public boolean removePlace(int index) {
		if(index >= stateVector.size())
			return false;
		
		stateVector.remove(index);
		
		return true;
	}
	
	/**
	 * Zwraca liczbę miejsc.
	 * @return (<b>int</b>) - rozmiar wektora stanu (liczba miejsc).
	 */
	public int getSize() {
		return stateVector.size();
	}
	
	/**
	 * Zwraca liczbę tokenów w stanie dla zadanego miejsca.
	 * @param index (<b>int</b>) indeks miejsca.
	 * @return (<b>double</b>) - liczba tokenów w miejscu o indeksie index.
	 */
	public double getTokens(int index) {
		if(index >= stateVector.size())
			return -1;
		else
			return stateVector.get(index);
	}
	
	/**
	 * Ustawia wskazaną liczbę tokenów w wektorze stanu dla danego miejsca.
	 * @param index (<b>int</b>) indeks miejsca.
	 * @param tokens (<b>double</b>) nowa liczba tokenów w miejscu o indeksie index.
	 */
	public void setTokens(int index, double tokens) {
		if(index < stateVector.size())
			stateVector.set(index, tokens);
	}
	
	/**
	 * Dodaje wskazaną liczbę tokenów do wektora stanu dla danego miejsca.
	 * @param index (<b>int</b>) indeks miejsca.
	 * @param tokens (<b>double</b>) nowa liczba tokenów do dodania w miejscu o indeksie index.
	 */
	@SuppressWarnings("unused")
	public void addTokens(int index, double tokens) {
		if(index < stateVector.size()) {
			double oldValue = stateVector.get(index);
			stateVector.set(index, oldValue+tokens);
		}
	}
	
	/**
	 * Uaktualnia cały wektor stanu chwilowym stanem sieci (liczbą tokenów)
	 */
	@SuppressWarnings("unused")
	public void updateWholeVector() {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		int placesNumber = places.size();
		for(int p=0; p<placesNumber; p++) {
			stateVector.set(p, (double) places.get(p).getTokensNumber());
		}
	}
	
	/**
	 * Ustawia nowy opis wektora stanów (liczby tokenów sieci).
	 * @param description (<b>String</b>) opis stanu.
	 */
	public void setDescription(String description) {
		this.stateDescription = description;
	}
	
	/**
	 * Zwraca opis wektora stanu (liczby tokenów w sieci).
	 * @return (<b>String</b>) opis stanu.
	 */
	public String getDescription() {
		return this.stateDescription;
	}
	
	/**
	 * Ustawia typ wektora stanu (liczby tokenów w sieci).
	 * @param type (<b>String</b>) nazwa typu stanu.
	 */
	public void setStateType(String type) {
		this.stateType = type;
	}
	
	/**
	 * Zwraca nazwę typu wektora stanu (liczby tokenów sieci).
	 * @return (<b>String</b>) nazwa typu stanu.
	 */
	public String getStateType() {
		return this.stateType;
	}
	
	/**
	 * Umożliwia dostęp do wektora danych stanu sieci - liczby tokenów w miejscach.
	 * @return (<b>ArrayList[Double]</b>) - wektor stanu sieci klasycznej
	 */
	public ArrayList<Double> accessVector() {
		return this.stateVector;
	}
}
