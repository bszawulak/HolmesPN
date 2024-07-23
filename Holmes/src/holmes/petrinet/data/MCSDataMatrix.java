package holmes.petrinet.data;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.files.io.MCSoperations;
import holmes.petrinet.elements.Transition;

/**
 * Klasa odpowiedzialna za zarządzanie informacjami o przechowywanych zbiorach MCS.
 */
public class MCSDataMatrix {
	private static LanguageManager lang = GUIManager.getLanguageManager();
	private ArrayList<ArrayList<ArrayList<Integer>>> mcsDataCore;
	private ArrayList<ArrayList<ArrayList<Integer>>> mcsSetsInfo;
	private ArrayList<String> transNames;
	private int matrixSize;

	/**
	 * Konstruktor obiektu klasy MCSDataMatrix.
	 */
	public MCSDataMatrix() {
		resetMSC();
	}
	
	/**
	 * Metoda resetuje dane obiektu klasy.
	 */
	public void resetMSC() {
		mcsDataCore = new ArrayList<ArrayList<ArrayList<Integer>>>();
		mcsSetsInfo = new ArrayList<ArrayList<ArrayList<Integer>>>();
		transNames = new ArrayList<String>();
		matrixSize = 0;
	}
	
	/**
	 * Metoda tworzy nową strukturę danych umożliwiająca przechowywanie list zbiorów MCT dla
	 * każdej tranzycji sieci.
	 */
	public void initiateMCS() {
		ArrayList<Transition> transitions =  GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		matrixSize = transitions.size();
		mcsDataCore = new ArrayList<ArrayList<ArrayList<Integer>>>();
		for(int i=0; i<matrixSize; i++) {
			ArrayList<ArrayList<Integer>> newVector = new ArrayList<ArrayList<Integer>>();
			mcsDataCore.add(newVector);
			ArrayList<ArrayList<Integer>> infoVector = new ArrayList<ArrayList<Integer>>();
			mcsSetsInfo.add(infoVector);
			transNames.add(transitions.get(i).getName());
		}	
	}
	
	/**
	 * Metoda zwraca rozmiar głównej listy danych - liczba tranzycji.
	 * @return int - dla ilu tranzycji jest miejsce
	 */
	public int getSize() {
		return matrixSize;
	}

	/**
	 * Metoda wstawia listę zbiorów MCS dla pozycji danej tranzycji (objR).
	 * @param mcsList ArrayList[ArrayList[Integer]] - nowa lista MCS dla objR
	 * @param pos int - pozycja w bazie MCS
	 * @param warning boolean - true, jeśli wstawiamy bezpiecznie, tj. sprawdzając czy czegoś nie zastąpimy - wyświetli ostrzeżenie
	 */
	public void insertMCS(ArrayList<ArrayList<Integer>> mcsList, ArrayList<ArrayList<Integer>> mcsListInfo, int pos, boolean warning) {
		if(pos < matrixSize) {
			if(!warning) {
				mcsDataCore.set(pos, mcsList);
				mcsSetsInfo.set(pos, mcsListInfo);
			} else {
				if(!mcsDataCore.get(pos).isEmpty()) {
					Object[] options = {lang.getText("replace"), lang.getText("cancel")};
					int decision = JOptionPane.showOptionDialog(null,
									lang.getText("MCSDM_entry001"),
									lang.getText("MCSDM_entry001t"), JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options, options[1]);
					if (decision == 1) {

					} else {
						mcsDataCore.set(pos, mcsList);
						mcsSetsInfo.set(pos,  mcsListInfo);
					}
				} else {
					mcsDataCore.set(pos, mcsList);
					mcsSetsInfo.set(pos,  mcsListInfo);
				}
			}
		} else {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry003340")+" "+matrixSize, "error", true);
		}
	}
	
	/**
	 * Metoda zwraca liczbę pamiętanych list zbiorów MCS.
	 * @return int - liczba obliczonych do tej pory list zbiorów
	 */
	public int getCalculatedMCSnumber() {
		int size = 0;
		for(ArrayList<ArrayList<Integer>> list : mcsDataCore) {
			if(!list.isEmpty()) {
				size++;
			}
		}
		return size;
	}

	/**
	 * Metoda zwraca listę zbiorów MCS z wybranej pozycji.
	 * @param pos int - indeks tranzycji
	 * @return ArrayList[ArrayList[Integer]] - lista zbiorów MCS dla wybranej reakcji
	 */
	public ArrayList<ArrayList<Integer>> getMCSlist(int pos) {
		if(pos < matrixSize) {
			return mcsDataCore.get(pos); 
		} else {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00335")+" "+matrixSize, "warning", true);
			return null;
		}
	}
	
	/**
	 * Metoda zwraca listę informacji o zbiorach MCS z wybranej pozycji.
	 * @param pos int - indeks tranzycji (objR)
	 * @return ArrayList[Set[Integer]] - lista informacji o wszystkich zbiorach MCS danej tranzycji
	 */
	public ArrayList<ArrayList<Integer>> getMCSlistInfo(int pos) {
		if(pos < matrixSize) {
			return mcsSetsInfo.get(pos); 
		} else {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00336")+" "+matrixSize, "warning", true);
			return null;
		}
	}

	/**
	 * Metoda sprawdza, czy nastąpi zastąpienie danych. Jeśli tak, daje opcję zatrzymania operacji, kontynuacji
	 * zastępowania danych o MCS lub zapisania starych danych przed zastąpieniem.
	 * @return boolean - true - można kontynuować operację zastępowania danych, false - nie można kontynuować
	 */
	public boolean checkDataReplacing() {
		int dataSize = getCalculatedMCSnumber();
		if(dataSize > 0) {
			
			Object[] options = {lang.getText("replace"), lang.getText("saveAndReplace"), lang.getText("cancel")};
			int decision = JOptionPane.showOptionDialog(null,
							lang.getText("MCSDM_entry002"),
							lang.getText("MCSDM_entry002t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (decision == 2) {
				return false;
			} else if (decision == 0) {
				return true;
			} else {
				boolean status = MCSoperations.saveAllMCS(this);
				if(!status) {
					JOptionPane.showMessageDialog(null,
							lang.getText("MCSDM_entry003"),
							lang.getText("MCSDM_entry003t"),JOptionPane.ERROR_MESSAGE);
				}
				return status;
			}
		} else {
			return true;
		}
	}
	
	/**
	 * Do użytku I/O
	 * @return ArrayList[ArrayList[Integer]] - główna macierz danych
	 */
	public ArrayList<ArrayList<ArrayList<Integer>>> accessMCSdata() {
		return mcsDataCore;
	}
	
	public ArrayList<ArrayList<ArrayList<Integer>>> accessMCSinfo() {
		return mcsSetsInfo;
	}
	
	/**
	 * Do użytku I/O
	 * @return ArrayList[String] - wektor nazw
	 */
	public ArrayList<String> accessMCStransitions() {
		return transNames;
	}
}
