package holmes.analyse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Transition;

/**
 * Maximal Dependend Transition sets (MDT sets): 
 * "Understanding network behavior by structured representations of transitions invariants"
 * M. Heiner, Algorithmic Bioprocesses, Natural Computing Series, 2009, pp. 367-389
 * 
 * @author MR
 *
 */
public class MDTSCalculator {
	ArrayList<ArrayList<Integer>> tmpInvariantsMatrix;
	ArrayList<Transition> calc_transitions;
	int invariantsNumber;
	int transitionsNumber;
	
	/**
	 * Konstruktor klasy MDTSCalculator przygotowujący podstawowe struktury danych.
	 */
	public MDTSCalculator() {
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix(); 
    	if(invariants == null || invariants.size() == 0) { //STEP 1: EM obliczono
    		GUIManager.getDefaultGUIManager().log("No invariants found!", "errer", true);
    		return;
    	} else {
    		invariantsNumber = invariants.size(); //wiersze w notacji pierwszej
    		tmpInvariantsMatrix = InvariantsTools.transposeMatrix(invariants); //na potrzeby algorytmu: teraz inw to kolumny
    		calc_transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
    		transitionsNumber = calc_transitions.size();
    	}
	}
	
	public ArrayList<Set<Integer>> calculateMDTS() {
		ArrayList<Set<Integer>> resultList = new ArrayList<Set<Integer>>();
		ArrayList<Integer> unassignedRows = new ArrayList<Integer>();
		ArrayList<Integer> assignedRows = new ArrayList<Integer>();
		for(int i=0; i<tmpInvariantsMatrix.size(); i++)
			unassignedRows.add(i);
		
		tmpInvariantsMatrix = InvariantsTools.returnBinaryMatrix(tmpInvariantsMatrix);
		
		while(unassignedRows.size() > 0) {
			Set<Integer> mdts = new HashSet<Integer>();
			int rowValue = unassignedRows.get(0);
			mdts.add(rowValue); //dodaj pierwszy nieprzypisany
			assignedRows.add(rowValue);
			unassignedRows.remove(0);
			
			ArrayList<Integer> removeList = new ArrayList<Integer>();
			for(int otherRow : unassignedRows) {
				if(tmpInvariantsMatrix.get(rowValue).equals(tmpInvariantsMatrix.get(otherRow))) { //są w tych samych inwariantach
					mdts.add(otherRow);
					assignedRows.add(otherRow);
					removeList.add(unassignedRows.indexOf(otherRow));
					//unassignedRows.remove(unassignedRows.indexOf(otherRow)); // ???
				}
			}
			resultList.add(mdts);
			
			int removedSoFar = 0;
			for(int el : removeList) {
				int index = el - removedSoFar;
				unassignedRows.remove(index);
				removedSoFar++;
			}
		}
			
			
		return resultList;
	}
}
