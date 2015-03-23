package abyss.math;

import java.util.ArrayList;
import java.util.Set;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;

public class MinCutSetData {
	private ArrayList<ArrayList<Set<Integer>>> mcsDataCore;
	private ArrayList<String> transNames;
	private int matrixSize;

	public MinCutSetData() {
		mcsDataCore = new ArrayList<ArrayList<Set<Integer>>>();
		transNames = new ArrayList<String>();
		matrixSize = 0;
	}
	
	public void resetMSC() {
		mcsDataCore = new ArrayList<ArrayList<Set<Integer>>>();
		transNames = new ArrayList<String>();
		matrixSize = 0;
	}
	
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
	
	public int returnSize() {
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
			if(warning = false)
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
	
	public ArrayList<Set<Integer>> getMCSlist(int pos) {
		if(pos < matrixSize) {
			return mcsDataCore.get(pos); 
		} else {
			GUIManager.getDefaultGUIManager().log("Unable to return MCS list. DataCore size: "+matrixSize, "error", true);
			return null;
		}
	}
	
	/**
	 * Do użytku I/O
	 * @return
	 */
	public ArrayList<ArrayList<Set<Integer>>> accessMCSdata() {
		return mcsDataCore;
	}
	
	/**
	 * Do użytku I/O
	 * @return
	 */
	public ArrayList<String> accessMCStransitions() {
		return transNames;
	}
}
