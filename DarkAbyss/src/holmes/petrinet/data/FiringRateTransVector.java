package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.petrinet.elements.Transition.StochaticsType;

/**
 * Klasa zarządzająca wektorem firing rates dla tranzycji.
 * 
 * @author MR
 */
public class FiringRateTransVector {
	private ArrayList<FRContainer> firingVector;
	private String frType = "";
	private String frDescription = "";
	
	/**
	 * Klasa kontener, przechowuje dane stochastyczne tranzycji
	 * @author MR
	 */
	public class FRContainer {
		public double fr;
		public StochaticsType sType;
		
		public FRContainer(double value, StochaticsType sType) {
			this.fr = value;
			this.sType = sType;
		}
	}
	
	/**
	 * Konstruktor obiektu klasy TransFiringRateVector.
	 */
	public FiringRateTransVector() {
		firingVector = new ArrayList<FRContainer>();
		frType = "SSA1";
		frDescription = "Default description for transitions firing rates vector.";
	}
	
	/**
	 * Zwraca nowy obiekt kontenera firing rates.
	 * @param fr double - wartość firing rates
	 * @param subType StochaticsType - typ SPN tranzycji
	 * @return FRContainer - obiekt
	 */
	public FRContainer newContainer(double fr, StochaticsType subType) {
		return new FRContainer(fr, subType);
	}
	
	/**
	 * Dodaje nową tranzycję z zadanym firing rate
	 * @param value double - fire rate
	 * @param sType StochaticsType - typ stochastyczny tranzycji
	 */
	public void addTrans(double value, StochaticsType sType) {
		firingVector.add(new FRContainer(value, sType));
	}
	
	/**
	 * Usuwa lokalizację właśnie kasowanej tranzycji z wektora firing rate.
	 * @param index int - nr tranzycji
	 * @return boolean - true, jeśli operacja się udała
	 */
	public boolean removeTrans(int index) {
		if(index >= firingVector.size())
			return false;
		
		firingVector.remove(index);
		
		return true;
	}
	
	/**
	 * Zwraca liczbę tranzycji.
	 * @return int - liczba tranzycji w wektorze firingVector
	 */
	public int getSize() {
		return firingVector.size();
	}
	
	/**
	 * Zwraca wartość firing rate dla tranzycji o zadanej lokalizacji.
	 * @param index int - nr tranzycji
	 * @return double - firing rate
	 */
	public double getFiringRate(int index) {
		if(index >= firingVector.size())
			return -1;
		else
			return firingVector.get(index).fr;
	}
	
	/**
	 * Zwraca podtyp stochastyczny tranzycji.
	 * @param index int - nr tranzycji
	 * @return StochaticsType - podtyp
	 */
	public StochaticsType getStochasticType(int index) {
		if(index >= firingVector.size())
			return StochaticsType.ST;
		else
			return firingVector.get(index).sType;
	}
	
	/**
	 * Ustawia nowy opis wektora firing rates.
	 * @param description String - opis
	 */
	public void setDescription(String description) {
		this.frDescription = description;
	}
	
	/**
	 * Zwraca opis wektora firing rates.
	 * @return String - opis
	 */
	public String getDescription() {
		return this.frDescription;
	}
	
	/**
	 * Ustawia nowy typ wektora firing rates.
	 * @param description String - opis
	 */
	public void setFrType(String description) {
		this.frType = description;
	}
	
	/**
	 * Zwraca nazwę typu wektora firing rates.
	 * @return String - opis
	 */
	public String getFrType() {
		return this.frType;
	}
	
	/**
	 * Umożliwia dostęp do wektora danych.
	 * @return ArrayList[FRContainer] - wektor firing rates dla tranzycji
	 */
	public ArrayList<FRContainer> accessVector() {
		return this.firingVector;
	}
}
