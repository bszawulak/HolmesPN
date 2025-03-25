package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.extensions.TransitionSPNExtension;

/**
 * Klasa zarządzająca wektorem danych tranzycji SPN.
 */
public class SPNdataVector {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private ArrayList<SPNtransitionData> dataVector;
	private SPNvectorSuperType SPNvectorType;
	private String SPNvectorDescription;
	
	/** SPN, SSA */
	public enum SPNvectorSuperType { SPN, SSA }
	
	/**
	 * Konstruktor obiektu klasy SPNtransitionsVector.
	 */
	public SPNdataVector() {
		dataVector = new ArrayList<SPNtransitionData>();
		SPNvectorType = SPNvectorSuperType.SPN;
		SPNvectorDescription = lang.getText("SPNDV_entry001"); //Default description for SPN transitions data vector.
	}
	
	/**
	 * Zwraca nowy obiekt kontenera danych tranzycji SPN.
	 * @param frFunction String - funkcja/wartość fr
	 * @param subType StochaticsType - typ SPN tranzycji
	 * @return SPNdataContainer - obiekt
	 */
	public SPNtransitionData newContainer(String frFunction, TransitionSPNExtension.StochaticsType subType) {
		return new SPNtransitionData(frFunction, subType);
	}
	
	/**
	 * Zwraca czysty kontener danych dla tranzycji SPN.
	 * @return SPNdataContainer
	 */
	public SPNtransitionData newContainer() {
		return new SPNtransitionData();
	}
	
	/**
	 * Zwraca kontener danych dla wskazanej tranzycji.
	 * @param index int - indeks tranzycji
	 * @return SPNdataContainer - dane tranzycji w modelu SPN
	 */
	public SPNtransitionData getSPNtransitionContainer(int index) {
		if(index >= dataVector.size())
			return null;
		else
			return dataVector.get(index);
	}
	
	/**
	 * Dodaje nową tranzycję z zadaną funkcją firing rate
	 * @param value String - fire rate (funkcja)
	 * @param sType StochaticsType - typ stochastyczny tranzycji
	 */
	public void addTrans(String value, TransitionSPNExtension.StochaticsType sType) {
		dataVector.add(new SPNtransitionData(value, sType));
	}
	
	/**
	 * Usuwa lokalizację właśnie kasowanej tranzycji z wektora firing rate.
	 * @param index int - nr tranzycji
	 * @return boolean - true, jeśli operacja się udała
	 */
	public boolean removeTrans(int index) {
		if(index >= dataVector.size())
			return false;
		
		dataVector.remove(index);
		
		return true;
	}
	
	/**
	 * Zwraca liczbę tranzycji.
	 * @return int - liczba tranzycji w wektorze firingVector
	 */
	public int getSize() {
		return dataVector.size();
	}
	
	/**
	 * Zwraca wartość funkcji firing rate dla tranzycji o zadanej lokalizacji.
	 * @param index int - nr tranzycji
	 * @return double - firing rate
	 */
	public double getFiringRate(int index) {
		if(index >= dataVector.size())
			return -1;
		else {
			try {
				//TODO: moduł obliczania z funkcji:
				return Double.parseDouble(dataVector.get(index).ST_function);
			} catch(Exception e) {
				String strB = "err.";
				try {
					strB = String.format(lang.getText("LOGentry00374exception"), index);
				} catch (Exception ex2) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"LOGentry00374exception", "error", true);
				}
				overlord.log(strB+"\n"+e.getMessage(), "warning", true);
				return 1.0;
			}
		}
	}
	
	/**
	 * Zwraca podtyp stochastyczny tranzycji.
	 * @param index int - nr tranzycji
	 * @return StochaticsType - podtyp
	 */
	public TransitionSPNExtension.StochaticsType getStochasticType(int index) {
		if(index >= dataVector.size())
			return TransitionSPNExtension.StochaticsType.ST;
		else
			return dataVector.get(index).sType;
	}
	
	/**
	 * Ustawia nowy opis wektora danych tranzycji SPN.
	 * @param description String - opis
	 */
	public void setDescription(String description) {
		this.SPNvectorDescription = description;
	}
	
	/**
	 * Zwraca opis wektora danych tranzycji SPN.
	 * @return String - opis
	 */
	public String getDescription() {
		return this.SPNvectorDescription;
	}
	
	/**
	 * Ustawia nowy typ wektora danych tranzycji SPN.
	 * @param type SPNvectorSuperType - typ wektora
	 */
	public void setSyperType(SPNvectorSuperType type) {
		this.SPNvectorType = type;
	}
	
	/**
	 * Zwraca typ wektora danych tranzycji SPN.
	 * @return SPNvectorSuperType - typ wektora SPN
	 */
	public SPNvectorSuperType getSuperType() {
		return this.SPNvectorType;
	}
	
	/**
	 * Ustawia nowy typ wektora danych tranzycji SPN.
	 * @param type SPNvectorSuperType - typ wektora
	 */
	public void setSPNtype(SPNvectorSuperType type) {
		this.SPNvectorType = type;
	}
	
	/**
	 * Zwraca nazwę typu wektora danych tranzycji SPN.
	 * @return SPNvectorSuperType - opis
	 */
	public SPNvectorSuperType getSPNtype() {
		return this.SPNvectorType;
	}
	
	/**
	 * Umożliwia dostęp do wektora danych tranzycji SPN.
	 * @return ArrayList[SPNdataContainer] - wektor danych tranzycji SPN dla wybranej tranzycji
	 */
	public ArrayList<SPNtransitionData> accessVector() {
		return this.dataVector;
	}
}
