package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.SPNdataVector.SPNvectorSuperType;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.extensions.TransitionSPNExtension;

/**
 * Klasa zarządzająca wektorami danych SPN.
 */
public class SPNdataVectorManager {
	private GUIManager overlord;
	private PetriNet pn;
	
	private ArrayList<SPNdataVector> SPNdataMatrix;
	public int selectedVector = 0;
	
	/**
	 * Konstruktor obiektu klasy SPNdataVectorManager.
	 * @param net PetriNet - główny obiekt sieci
	 */
	public SPNdataVectorManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
		
		this.SPNdataMatrix = new ArrayList<SPNdataVector>();
		SPNdataMatrix.add(new SPNdataVector());
	}
	
	/**
	 * Dodaje wpis do wektorów SPN dla nowo utworzonej tranzycji.
	 */
	public void addTrans() {
		for(SPNdataVector frVector: SPNdataMatrix) {
			frVector.addTrans(""+1.0, TransitionSPNExtension.StochaticsType.ST);
		}
	}
	
	/**
	 * Usuwa we wszystkich wektorach danych SPN dane o właśnie kasowanej tranzycji.
	 * @param index int - indeks miejsca
	 * @return boolean - true, jeśli operacja przebiegła poprawnie
	 */
	public boolean removeTrans(int index) {
		if(index >= SPNdataMatrix.get(0).getSize())
			return false;
		
		for(SPNdataVector frVector: SPNdataMatrix) {
			boolean status = frVector.removeTrans(index);
			if(!status) {
				overlord.log("Critical error: invalid transition index in SPN data matrix.", "error", true);
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Zwraca obiekt wektora danych SPN o zadanym indeksie.
	 * @param index int - nr stanu
	 * @return SPNtransitionsVector - obiekt wektora danych SPN
	 */
	public SPNdataVector getSPNdataVector(int index) {
		if(index >= SPNdataMatrix.size())
			return null;
		else
			return SPNdataMatrix.get(index);
	}
	
	public SPNdataVector getCurrentSPNdataVector() {
		return SPNdataMatrix.get(selectedVector);
	}
	
	/**
	 * Metoda dodaje nowy stan SPN na bazie istniejącego w danej chwili w edytorze.
	 */
	public void addCurrentFRasSPNdataVector() {
		SPNdataVector frVector = new SPNdataVector();
		for(Transition trans : pn.getTransitions()) {
			frVector.addTrans(""+trans.spnExtension.getFiringRate(), trans.spnExtension.getSPNtype());
		}
		SPNdataMatrix.add(frVector);
	}
	
	/**
	 * Metoda czyści wektory SPN i tworzy nowy pierwszy wektor SPN.
	 */
	public void createCleanSPNdataVector() {
		reset(false);
		int transNo = pn.getTransitions().size();
		for(int t=0; t<transNo; t++) {
			SPNdataMatrix.get(0).addTrans(""+1.0, TransitionSPNExtension.StochaticsType.ST);
		}
	}
	
	/**
	 * Metoda ustawia nowy wektor danych SPN.
	 * @param vectorID int - nr wektora SPN data z tablicy
	 */
	public void setNetworkSPNdataVector(int vectorID) {
		ArrayList<Transition> transitions = pn.getTransitions();
		SPNdataVector frVector = SPNdataMatrix.get(vectorID);
		for(int t=0; t<transitions.size(); t++) {
			Transition trans = transitions.get(t);
			trans.spnExtension.setSPNtype(frVector.getStochasticType(t));
			trans.spnExtension.setFiringRate(frVector.getFiringRate(t));
		}
		selectedVector = vectorID;
	}
	
	/**
	 * Przywraca aktualnie wybrany wektor danych SPN.
	 */
	public void restoreSelectedSPNvector() {
		setNetworkSPNdataVector(selectedVector);
	}
	
	/**
	 * Metoda służąca do usuwania wektorów danych SPN sieci.
	 * @param vectorID int - indeks wektora fr
	 */
	public void removeSPNvector(int vectorID) {
		SPNdataMatrix.remove(vectorID);
		selectedVector = 0;
	}
	
	/**
	 * Zastępuje wskazany stan SPN aktualnym stanem firing rates sieci.
	 * @param vectorID (<b>int</b>) wskazany stan z tabeli.
	 */
	public void replaceSPNvectorWithNetFRates(int vectorID) {
		ArrayList<Transition> transitions = pn.getTransitions();
		SPNdataVector frVector = SPNdataMatrix.get(vectorID);
		for(int t=0; t<transitions.size(); t++) {
			frVector.accessVector().get(t).ST_function = ""+transitions.get(t).spnExtension.getFiringRate();
			frVector.accessVector().get(t).sType = transitions.get(t).spnExtension.getSPNtype();
		}
	}
	
	/**
	 * Zwraca opis wektora danych SPN.
	 * @param selected int - nr wektora danych SPN sieci
	 * @return String - opis
	 */
	public String getSPNvectorDescription(int selected) {
		return SPNdataMatrix.get(selected).getDescription();
	}
	
	/**
	 * Ustawia opis wektora danych SPN.
	 * @param selected int - nr stanu sieci
	 */
	public void setSPNvectorDescription(int selected, String newText) {
		SPNdataMatrix.get(selected).setDescription(newText);
	}

	/**
	 * Zwraca typ wektora danych SPN.
	 * @param selected int - nr wektora danych SPN sieci
	 * @return SPNvectorSuperType - opis
	 */
	public SPNvectorSuperType getSPNvectorType(int selected) {
		return SPNdataMatrix.get(selected).getSuperType();
	}
	
	/**
	 * Ustawia typ wektora danych SPN.
	 * @param selected int - nr stanu sieci
	 * @param type SPNvectorSuperType - typ wektora danych SPN
	 */
	public void setSPNvectorDescription(int selected, SPNvectorSuperType type) {
		SPNdataMatrix.get(selected).setSyperType(type);
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy wektorów fr.
	 * @return ArrayList[PlacesStateVector] - tablica fr sieci
	 */
	public ArrayList<SPNdataVector> accessSPNmatrix() {
		return this.SPNdataMatrix;
	}
	
	/**
	 * Metoda czyści tablicę wektorów SPN i ich nazw - tworzony jest nowy pierwszy wektor.
	 * @param isLoading boolean - jeśli true, nie tworzy pierwszych elementów (na potrzeby ProjectReader)
	 */
	public void reset(boolean isLoading) {
		if(isLoading) {
			SPNdataMatrix = new ArrayList<SPNdataVector>();
		} else {
			SPNdataMatrix = new ArrayList<SPNdataVector>();
			SPNdataMatrix.add(new SPNdataVector());
		}
		selectedVector = 0;
	}
}
