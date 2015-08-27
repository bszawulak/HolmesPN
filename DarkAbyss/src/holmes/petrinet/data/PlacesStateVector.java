package holmes.petrinet.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Klasa zarządzająca stanem sieci klasycznej, tj. liczbą tokenów w miejsach.
 * 
 * @author MR
 *
 */
public class PlacesStateVector implements Serializable {
	private static final long serialVersionUID = 6652562026923360610L;
	private ArrayList<Double> stateVector;
	public String stateType = "";
	
	/**
	 * Konstruktor obiektu klasy PlacesStateVector.
	 */
	public PlacesStateVector() {
		stateVector = new ArrayList<>();
		stateType = "NORMAL";
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
	 * Umożliwia dostęp do wektora danych.
	 * @return ArrayList[Double] - wektor stanu sieci klasycznej
	 */
	public ArrayList<Double> accessVector() {
		return this.stateVector;
	}
}
