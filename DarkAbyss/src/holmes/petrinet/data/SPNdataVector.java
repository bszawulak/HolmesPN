package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Transition.StochaticsType;

/**
 * Klasa zarządzająca wektorem danych tranzycji SPN.
 * 
 * @author MR
 */
public class SPNdataVector {
	private ArrayList<SPNdataContainer> dataVector;
	private SPNvectorSuperType SPNvectorType = SPNvectorSuperType.SPN;
	private String SPNvectorDescription = "";
	
	/** SPN, SSA */
	public enum SPNvectorSuperType { SPN, SSA }
	
	/**
	 * Klasa kontener, przechowuje dane stochastyczne tranzycji
	 * @author MR
	 */
	public class SPNdataContainer {
		public String ST_function = "";
		public int IM_priority = 0;
		public int DET_delay = 0;
		public String SCH_start = "";
		public int SCH_rep = 0;
		public String SCH_end = "";
		public StochaticsType sType = StochaticsType.ST;
		
		public SPNdataContainer() {
			
		}
		
		public SPNdataContainer(String value, StochaticsType sType) {
			this.ST_function = value;
			this.sType = sType;
		}
		
		public String returnSaveVector() {
			String data = "";
			data += (ST_function+";");
			data += (IM_priority+";");
			data += (DET_delay+";");
			data += (SCH_start+";");
			data += (SCH_rep+";");
			data += (SCH_end+";");
			data += (sType);
			
			
			return data;
		}
	}
	
	/**
	 * Konstruktor obiektu klasy SPNtransitionsVector.
	 */
	public SPNdataVector() {
		dataVector = new ArrayList<SPNdataContainer>();
		SPNvectorType = SPNvectorSuperType.SPN;
		SPNvectorDescription = "Default description for SPN transitions data vector.";
	}
	
	/**
	 * Zwraca nowy obiekt kontenera danych tranzycji SPN.
	 * @param frFunction String - funkcja/wartość fr
	 * @param subType StochaticsType - typ SPN tranzycji
	 * @return SPNdataContainer - obiekt
	 */
	public SPNdataContainer newContainer(String frFunction, StochaticsType subType) {
		return new SPNdataContainer(frFunction, subType);
	}
	
	/**
	 * Zwraca czysty kontener danych dla tranzycji SPN.
	 * @return SPNdataContainer
	 */
	public SPNdataContainer newContainer() {
		return new SPNdataContainer();
	}
	
	/**
	 * Zwraca kontener danych dla wskazanej tranzycji.
	 * @param index int - indeks tranzycji
	 * @return SPNdataContainer - dane tranzycji w modelu SPN
	 */
	public SPNdataContainer getSPNtransitionContainer(int index) {
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
	public void addTrans(String value, StochaticsType sType) {
		dataVector.add(new SPNdataContainer(value, sType));
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
				double fr = Integer.parseInt(dataVector.get(index).ST_function);
				return fr;
			} catch(Exception e) {
				GUIManager.getDefaultGUIManager().log("Firing rate function evaluation failed for t"+index+", returning 1.0.", "warning", true);
				return 1.0;
			}
		}
	}
	
	/**
	 * Zwraca podtyp stochastyczny tranzycji.
	 * @param index int - nr tranzycji
	 * @return StochaticsType - podtyp
	 */
	public StochaticsType getStochasticType(int index) {
		if(index >= dataVector.size())
			return StochaticsType.ST;
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
	 * @param description SPNvectorSuperType - typ wektora
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
	public ArrayList<SPNdataContainer> accessVector() {
		return this.dataVector;
	}
}
