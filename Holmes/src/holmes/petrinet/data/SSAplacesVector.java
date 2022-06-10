package holmes.petrinet.data;

import java.util.ArrayList;

/**
 * Klasa definiująca wektor danych wejściowych SSA.
 * 
 * @author MR
 */
public class SSAplacesVector {
	private ArrayList<Double> ssaVector;
	private String description = "";
	private SSAdataType type;
	private double experimentVolume;
	
	public enum SSAdataType { MOLECULES, CONCENTRATION };
	
	/**
	 * Konstruktor obiektu klasy SSAplacesVector.
	 */
	public SSAplacesVector() {
		ssaVector = new ArrayList<Double>();
		description = "Default SSA data vector description";
		type = SSAdataType.MOLECULES;
		experimentVolume = 0.0;
	}
	
	/**
	 * Dodaje nowe miejsce z zadaną liczba cząsteczek do wektora danych SSA.
	 * @param value double - liczba tokenów
	 */
	public void addPlace(double value) {
		ssaVector.add(value);
	}
	
	/**
	 * Usuwa lokalizację właśnie kasowanego miejsca z wektora SSA liczby cząsteczek.
	 * @param index int - nr miejsca
	 * @return boolean - true, jeśli operacja się udała
	 */
	public boolean removePlace(int index) {
		if(index >= ssaVector.size())
			return false;
		
		ssaVector.remove(index);
		return true;
	}
	
	/**
	 * Zwraca rozmiar wektora SSA czyli liczbę miejsc.
	 * @return int - rozmiar wektora SSA.
	 */
	public int getSize() {
		return ssaVector.size();
	}
	
	/**
	 * Zwraca liczbę cząsteczek w stanie SSA dla zadanego miejsca.
	 * @param index int - nr miejsca
	 * @return double - liczba cząstek
	 */
	public double getTokens(int index) {
		if(index >= ssaVector.size())
			return -1;
		else
			return ssaVector.get(index);
	}
	
	/**
	 * Ustawia typ wektora SSA.
	 * @param type SSAdataType - nowy typ
	 */
	public void setType(SSAdataType type) {
		this.type = type;
	}
	
	/**
	 * Zwraca typ danych zawarty w wektorze SSA.
	 * @return SSAdataType - czyli liczba cząstek albo pojemność
	 */
	public SSAdataType getType() {
		return this.type;
	}
	
	/**
	 * Ustawia nowy opis wektora danych SSA.
	 * @param description String - opis
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * Zwraca opis wektora danych dla SSA.
	 * @return String - opis
	 */
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Ustawia nową wartość objętości.
	 * @param volume double - nowa wartość
	 */
	public void setVolume(double volume) {
		this.experimentVolume = volume;
	}
	
	/**
	 * Zwraca całkowitą (TEORETYCZNĄ, A PRIORI USTALONĄ!) objętość modelowanego systemu.
	 * @return double - objętość
	 */
	public double getVolume() {
		return this.experimentVolume;
	}
	
	/**
	 * Umożliwia dostęp do wektora danych SSA.
	 * @return ArrayList[Double] - wektor stanu SSA
	 */
	public ArrayList<Double> accessVector() {
		return this.ssaVector;
	}
}
