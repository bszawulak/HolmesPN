package holmes.petrinet.data;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Transition.StochaticsType;

public class FiringRatesManager {
	private GUIManager overlord;
	private PetriNet pn;
	
	private ArrayList<TransFiringRateVector> firingRatesMatrix;
	private ArrayList<String> frNames;
	public int selectedVector = 0;
	
	/**
	 * Konstruktor obiektu klasy FiringRatesManager
	 * @param net PetriNet - główny obiekt sieci
	 */
	public FiringRatesManager(PetriNet net) {
		overlord = GUIManager.getDefaultGUIManager();
		this.pn = net;
		
		this.firingRatesMatrix = new ArrayList<TransFiringRateVector>();
		firingRatesMatrix.add(new TransFiringRateVector());
		
		this.frNames = new ArrayList<String>();
		frNames.add("Default name for firing rate vector 1.");
	}
	
	/**
	 * Dodaje wpis do wektorów firing rates dla nowo utworzonej tranzycji.
	 */
	public void addTrans() {
		for(TransFiringRateVector frVector: firingRatesMatrix) {
			frVector.addTrans(1.0, StochaticsType.ST);
		}
	}
	
	/**
	 * Usuwa we wszystkich wektorach firing rates dane o właśnie kasowanej tranzycji.
	 * @param index int - indeks miejsca
	 * @return boolean - true, jeśli operacja przebiegła poprawnie
	 */
	public boolean removeTrans(int index) {
		if(index >= firingRatesMatrix.get(0).getSize())
			return false;
		
		for(TransFiringRateVector frVector: firingRatesMatrix) {
			boolean status = frVector.removeTrans(index);
			if(!status) {
				overlord.log("Critical error: invalid transition index in firing rates matrix.", "error", true);
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Zwraca obiekt wektora firing rates o zadanym indeksie.
	 * @param index int - nr stanu
	 * @return TransFiringRateVector - obiekt wektora firing rates
	 */
	public TransFiringRateVector getFRVector(int index) {
		if(index >= firingRatesMatrix.size())
			return null;
		else
			return firingRatesMatrix.get(index);
	}
	
	public TransFiringRateVector getCurrentFRVector() {
		return firingRatesMatrix.get(selectedVector);
	}
	
	/**
	 * Metoda dodaje nowy stan firing rates na bazie istniejącego w danej chwili w edytorze.
	 */
	public void addCurrentStateAsFRVector() {
		TransFiringRateVector frVector = new TransFiringRateVector();
		for(Transition trans : pn.getTransitions()) {
			frVector.addTrans(trans.getFiringRate(), trans.getStochasticType());
		}
		firingRatesMatrix.add(frVector);
		frNames.add("Default name");
	}
	
	/**
	 * Metoda czyści stany sieci i tworzy nowy pierwszy stan sieci.
	 */
	public void createCleanFRVector() {
		reset(false);
		int transNo = pn.getTransitions().size();
		for(int t=0; t<transNo; t++) {
			firingRatesMatrix.get(0).addTrans(1.0, StochaticsType.ST);
		}
		frNames.set(0, "Default name");
	}
	
	/**
	 * Metoda ustawia nowy wektor firing rates.
	 * @param vectorID int - nr wektora fr z tablicy
	 */
	public void setNetworkFRVector(int vectorID) {
		ArrayList<Transition> transitions = pn.getTransitions();
		TransFiringRateVector frVector = firingRatesMatrix.get(vectorID);
		for(int t=0; t<transitions.size(); t++) {
			Transition trans = transitions.get(t);
			trans.setStochasticType(frVector.getStochasticType(t));
			trans.setFiringRate(frVector.getFiringRate(t));
		}
		selectedVector = vectorID;
	}
	
	/**
	 * Przywraca aktualnie wybrany wektor firing rates.
	 */
	public void restoreSelectedState() {
		setNetworkFRVector(selectedVector);
	}
	
	/**
	 * Metoda służąca do usuwania wektorów firing rates sieci.
	 * @param vectorID int - indeks wektora fr
	 */
	public void removeFRVector(int vectorID) {
		firingRatesMatrix.remove(vectorID);
		selectedVector = 0;
	}
	
	/**
	 * Zastępuje wskazany stan aktualnym stanem sieci.
	 * @param stateID int - wskazany stan z tabeli
	 */
	public void replaceVectorWithNetState(int vectorID) {
		ArrayList<Transition> transitions = pn.getTransitions();
		TransFiringRateVector frVector = firingRatesMatrix.get(vectorID);
		for(int t=0; t<transitions.size(); t++) {
			frVector.accessVector().get(t).fr = transitions.get(t).getFiringRate();
			frVector.accessVector().get(t).sType = transitions.get(t).getStochasticType();
		}
	}
	
	/**
	 * Zwraca opis wektora fr.
	 * @param selected int - nr wektora fr sieci
	 * @return String - opis
	 */
	public String getFRVectorDescription(int selected) {
		return frNames.get(selected);
	}
	
	/**
	 * Ustawia opis wektora fr.
	 * @param selected int - nr stanu sieci
	 * @return String - opis
	 */
	public void setStateDescription(int selected, String newText) {
		frNames.set(selected, newText);
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy wektorów fr.
	 * @return ArrayList[PlacesStateVector] - tablica fr sieci
	 */
	public ArrayList<TransFiringRateVector> accessFRMatrix() {
		return this.firingRatesMatrix;
	}
	
	/**
	 * NA POTRZEBY ZAPISU PROJEKTU: dostęp do tablicy nazw wektorów fr.
	 * @return ArrayList[PlacesStateVector] - tablica nazw fr sieci
	 */
	public ArrayList<String> accessFRVectorsNames() {
		return this.frNames;
	}
	
	/**
	 * Metoda czyści tablicę stanów i ich nazw- tworzony jest nowy stan pierwszy.
	 * @param isLoading boolean - jeśli true, nie tworzy pierwszych elementów (na potrzeby ProjectReader)
	 */
	public void reset(boolean isLoading) {
		if(isLoading) {
			firingRatesMatrix = new ArrayList<TransFiringRateVector>();
			frNames = new ArrayList<String>();
		} else {
			firingRatesMatrix = new ArrayList<TransFiringRateVector>();
			firingRatesMatrix.add(new TransFiringRateVector());
			frNames = new ArrayList<String>();
			frNames.add("Default name for firing rate vector 1.");
		}
		selectedVector = 0;
	}
}
