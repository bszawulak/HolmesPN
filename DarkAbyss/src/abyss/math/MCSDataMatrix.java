package abyss.math;

import java.util.ArrayList;
import java.util.Set;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.files.io.MCSoperations;

/**
 * Klasa odpowiedzialna za zarządzanie informacjami o przechowywanych zbiorach MCS.
 * @author MR
 *
 */
public class MCSDataMatrix {
	private ArrayList<ArrayList<Set<Integer>>> mcsDataCore;
	private ArrayList<String> transNames;
	private int matrixSize;

	/**
	 * Konstruktor obiektu klasy MCSDataMatrix.
	 */
	public MCSDataMatrix() {
		resetMSC();
	}
	
	public void resetMSC() {
		mcsDataCore = new ArrayList<ArrayList<Set<Integer>>>();
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
		mcsDataCore = new ArrayList<ArrayList<Set<Integer>>>();
		for(int i=0; i<matrixSize; i++) {
			ArrayList<Set<Integer>> newVector = new ArrayList<Set<Integer>>();
			mcsDataCore.add(newVector);
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
	 * @param mcsList ArrayList[Set[Integer]] - nowa lista MCS dla objR
	 * @param pos int - pozycja w bazie MCS
	 * @param warning boolean - true, jeśli wstawiamy bezpiecznie, tj. sprawdzając czy czegoś nie zastąpimy - wyświetli ostrzeżenie
	 */
	public void insertMCS(ArrayList<Set<Integer>> mcsList, int pos, boolean warning) {
		if(pos < matrixSize) {
			if(warning == false)
				mcsDataCore.set(pos, mcsList);
			else {
				if(mcsDataCore.get(pos).size() > 0) {
					Object[] options = {"Yes", "No"};
					int decision = JOptionPane.showOptionDialog(null,
									"MCS list detected on a given location. Replace?",
									"Position not empty", JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE, null, options, options[1]);
					if (decision == 1) {
						return;
					} else {
						mcsDataCore.set(pos, mcsList);
					}
				} else {
					mcsDataCore.set(pos, mcsList);
				}
			}
		} else {
			GUIManager.getDefaultGUIManager().log("Unable to add new MCS list. DataCore size: "+matrixSize, "error", true);
		}
	}
	
	/**
	 * Metoda zwraca liczbę obliczonych list zbiorów MCS.
	 * @return int - liczba obliczonych list zbiorów
	 */
	public int getCalculatedMCSnumber() {
		int size = 0;
		for(ArrayList<Set<Integer>> list : mcsDataCore) {
			if(list.size()>0) {
				size++;
			}
		}
		return size;
	}

	/**
	 * Metoda zwraca listę zbiorów MCS z wybranej pozycji.
	 * @param pos int - indeks tranzycji
	 * @return ArrayList[Set[Integer]] - lista zbiorów MCS dla wybranej reakcji
	 */
	public ArrayList<Set<Integer>> getMCSlist(int pos) {
		if(pos < matrixSize) {
			return mcsDataCore.get(pos); 
		} else {
			GUIManager.getDefaultGUIManager().log("Unable to return MCS list. DataCore size: "+matrixSize, "error", true);
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
			
			Object[] options = {"Cancel", "Replace", "Save & replace"};
			int decision = JOptionPane.showOptionDialog(null,
							"MCS data for a given net is not empty. Cancel current operation, replace data or\n"
							+ "save old data to file before replacing?",
							"Net change detected", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (decision == 0) {
				return false;
			} else if (decision == 1) {
				return true;
			} else {
				MCSoperations.saveAllMCS(this);
				return true;
			}
		} else {
			return true;
		}
	}
	
	/**
	 * Do użytku I/O
	 * @return ArrayList[ArrayList[Integer]] - główna macierz danych
	 */
	public ArrayList<ArrayList<Set<Integer>>> accessMCSdata() {
		return mcsDataCore;
	}
	
	/**
	 * Do użytku I/O
	 * @return ArrayList[String] - wektor nazw
	 */
	public ArrayList<String> accessMCStransitions() {
		return transNames;
	}
}
