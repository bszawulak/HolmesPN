package abyss.math.pnElements;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.files.io.MCSoperations;

/**
 * Klasa odpowiedzialna za zarządzanie informacjami o przechowywanych zbiorach MCS.
 * @author MR
 *
 */
public class MCSDataMatrix {
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
			if(warning == false) {
				mcsDataCore.set(pos, mcsList);
				mcsSetsInfo.set(pos, mcsListInfo);
			} else {
				if(mcsDataCore.get(pos).size() > 0) {
					Object[] options = {"Replace", "Cancel"};
					int decision = JOptionPane.showOptionDialog(null,
									"Existing MCS list detected in a given location. Replace?",
									"Position contains data", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options, options[1]);
					if (decision == 1) {
						return;
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
			GUIManager.getDefaultGUIManager().log("Unable to add new MCS list. DataCore size: "+matrixSize, "error", true);
		}
	}
	
	/**
	 * Metoda zwraca liczbę pamiętanych list zbiorów MCS.
	 * @return int - liczba obliczonych do tej pory list zbiorów
	 */
	public int getCalculatedMCSnumber() {
		int size = 0;
		for(ArrayList<ArrayList<Integer>> list : mcsDataCore) {
			if(list.size()>0) {
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
			GUIManager.getDefaultGUIManager().log("Unable to return MCS list. DataCore size: "+matrixSize, "warning", true);
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
			GUIManager.getDefaultGUIManager().log("Unable to return MCS list info. DataCore size: "+matrixSize, "warning", true);
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
			
			Object[] options = {"Replace", "Save & replace", "Cancel"};
			int decision = JOptionPane.showOptionDialog(null,
							"MCS data for a given net is not empty. Cancel current operation,\n"
							+ "replace data or save current MCS data to file before replacing?",
							"Net change detected", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (decision == 2) {
				return false;
			} else if (decision == 0) {
				return true;
			} else {
				boolean status = MCSoperations.saveAllMCS(this);
				if(status == false) {
					JOptionPane.showMessageDialog(null, "Saving current MCS sets failed. Loading new sets cancelled.","Operation cancel.",JOptionPane.ERROR_MESSAGE);
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
