package holmes.petrinet.data;

import java.io.Serializable;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;

/**
 * Klasa zarządzająca stanem sieci klasycznej, tj. liczbą tokenów w miejsach.
 * 
 * @author MR
 *
 */
public class StatePlacesVector implements Serializable {
	private static final long serialVersionUID = 6652562026923360610L;
	private ArrayList<Double> stateVector;
	private String stateType = "";
	private String stateDescription;
	
	/**
	 * Konstruktor obiektu klasy PlacesStateVector.
	 */
	public StatePlacesVector() {
		stateVector = new ArrayList<Double>();
		stateType = "NORMAL";
		stateDescription = "Default description for state.";
	}
	
	/**
	 * Dodaje nowe miejsce z zadaną liczba tokenów do wektora.
	 * @param value double - liczba tokenów
	 */
	public void addPlace(double value) {
		stateVector.add(value);
	}
	
	/**
	 * Usuwa lokalizację właśnie kasowanego miejsca z wektora tokenów.
	 * @param index int - nr miejsca
	 * @return boolean - true, jeśli operacja się udała
	 */
	public boolean removePlace(int index) {
		if(index >= stateVector.size())
			return false;
		
		stateVector.remove(index);
		
		return true;
	}
	
	/**
	 * Zwraca liczbę miejsc.
	 * @return int
	 */
	public int getSize() {
		return stateVector.size();
	}
	
	/**
	 * Zwraca liczbę tokenów w stanie dla zadanego miejsca.
	 * @param index int - nr miejsca
	 * @return double - liczba tokenów
	 */
	public double getTokens(int index) {
		if(index >= stateVector.size())
			return -1;
		else
			return stateVector.get(index);
	}
	
	/**
	 * Ustawia wskazaną liczbę tokenów w wektorze stanu dla danego miejsca.
	 * @param index int - indeks miejsca
	 * @param tokens double - liczba tokenów
	 */
	public void setTokens(int index, double tokens) {
		if(index < stateVector.size())
			stateVector.set(index, tokens);
	}
	
	/**
	 * Dodaje wskazaną liczbę tokenów do wektora stanu dla danego miejsca.
	 * @param index int - indeks miejsca
	 * @param tokens double - liczba tokenów
	 */
	public void addTokens(int index, double tokens) {
		if(index < stateVector.size()) {
			double oldValue = stateVector.get(index);
			stateVector.set(index, oldValue+tokens);
		}
	}
	
	/**
	 * Uaktualnia cały wektor stanu chwilowym stanem sieci.
	 */
	public void updateWholeVector() {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		int placesNumber = places.size();
		for(int p=0; p<placesNumber; p++) {
			stateVector.set(p, (double) places.get(p).getTokensNumber());
		}
	}
	
	/**
	 * Ustawia nowy opis wektora stanów.
	 * @param description String - opis
	 */
	public void setDescription(String description) {
		this.stateDescription = description;
	}
	
	/**
	 * Zwraca opis wektora stanów.
	 * @return String - opis
	 */
	public String getDescription() {
		return this.stateDescription;
	}
	
	/**
	 * Ustawia typ wektora stanów.
	 * @param type String - nazwa typu
	 */
	public void setStateType(String type) {
		this.stateType = type;
	}
	
	/**
	 * Zwraca nazwę typu wektora stanów/
	 * @return String - nazwa typu
	 */
	public String getStateType() {
		return this.stateType;
	}
	
	/**
	 * Umożliwia dostęp do wektora danych.
	 * @return ArrayList[Double] - wektor stanu sieci klasycznej
	 */
	public ArrayList<Double> accessVector() {
		return this.stateVector;
	}
}
