package abyss.windows;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import abyss.analyse.InvariantsTools;
import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.simulator.StateSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.tables.InvariantContainer;
import abyss.tables.InvariantsSimTableModel;
import abyss.tables.InvariantsTableModel;
import abyss.tables.PlacesTableModel;
import abyss.tables.TransitionsTableModel;
import abyss.utilities.Tools;

/**
 * Klasa z metodami obsługującymi okno tabel programu - klasy AbyssNetTables.
 * @author MR
 *
 */
public class AbyssNetTablesActions {
	private AbyssNetTables antWindow; 
	public ArrayList<InvariantContainer> dataMatrix;
	
	/**
	 * Konstruktor klasy.
	 * @param overlord AbyssNetTables - obiekt obsługiwanego okna
	 */
	public AbyssNetTablesActions(AbyssNetTables overlord) {
		antWindow = overlord;
		dataMatrix = null;
	}
	
	/**
	 * Metoda obsługująca kliknięcie komórki tabeli danych.
	 * @param table JTable - tabela danych
	 */
	public void cellClickAction(JTable table) {
		try {
			String name = table.getName();
			if(name.equals("PlacesTable")) {
	  	    	int row = table.getSelectedRow();
	  	    	antWindow.currentClickedRow = row;
	  	    	int index = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
	  	    	AbyssNodeInfo window = new AbyssNodeInfo(
	  	    			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().get(index), 
	  	    			antWindow);
	  	    	window.setVisible(true);
	  	    	//int column = table.getSelectedColumn();
	  	    	//cellClickedEvent(row, column);
			} else if(name.equals("TransitionTable")) {
				int row = table.getSelectedRow();
	  	    	antWindow.currentClickedRow = row;
	  	    	int index = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
	  	    	AbyssNodeInfo window = new AbyssNodeInfo(
	  	    			GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().get(index), 
	  	    			antWindow);
	  	    	window.setVisible(true);
			}
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Metoda wypełniająca tabelę miejsc danymi.
	 * @param modelPlaces PlacesTableModel - obiekt danych
	 */
	public void addPlacesToModel(PlacesTableModel modelPlaces) {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places.size() == 0) {
			return;
		}
		
		ArrayList<Double> resVector = null;
		StateSimulator ss = new StateSimulator();
		ss.initiateSim(NetType.BASIC, false);
		ss.simulateNetSimple(10000, true);
		resVector = ss.getPlacesAvgData();
		
		int iterIndex = -1;
		for(Place p : places) {
			iterIndex++;
			int index = places.indexOf(p);
			
			if(iterIndex != index) {
				GUIManager.getDefaultGUIManager().log("Problem with position", "warning", true);
				//compactTable
			}
			
			String name = p.getName();
			int tokens = p.getTokensNumber();
			
			int inTrans = 0;
			int outTrans = 0;
			for (ElementLocation el : p.getElementLocations()) {
				inTrans += el.getInArcs().size(); //tyle tranzycji kieruje tutaj łuk
				outTrans += el.getOutArcs().size();
			}
			
			double avgTokens = 0;
			if(resVector != null && resVector.size() > 0)
				avgTokens = resVector.get(iterIndex);
			
			modelPlaces.addNew(index, name, tokens, inTrans, outTrans, (float)avgTokens);
		}
	}

	/**
	 * Metoda wypełniająca tabelę tramzycji danymi.
	 * @param model DefaultTableModel - obiekt danych
	 */
	public void addTransitionsToModel(TransitionsTableModel modelTransitions) {
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		if(transitions.size() == 0) {
			return;
		}
		
		StateSimulator ss = new StateSimulator();
		ss.initiateSim(NetType.BASIC, false);
		ss.simulateNetSimple(10000, false);
		ArrayList<Double> resVector = ss.getTransitionsAvgData();
		
		int iterIndex = -1;
		for(Transition t : transitions) {
			iterIndex++;
			int index = transitions.indexOf(t);
			if(iterIndex != index) {
				GUIManager.getDefaultGUIManager().log("Problem with position", "warning", true);
				//compactTable
			}
			
			String name = t.getName();
			
			int preP = 0;
			int postP = 0;
			for (ElementLocation el : t.getElementLocations()) {
				preP += el.getInArcs().size(); //tyle miejsc kieruje tutaj łuk
				postP += el.getOutArcs().size();
			}
			
			double avgFired = 0;
			if(resVector.size() > 0)
				avgFired = resVector.get(iterIndex);
			avgFired *= 100;
			int inInv = 0;
			
			modelTransitions.addNew(index, name, preP, postP, (float)avgFired, inInv);
		}
	}

	/**
	 * Metoda wypełniająca tabelę inwariantów podstawowymi informacjami o nich.
	 * @param model DefaultTableModel - obiekt danych
	 */
	public void addBasicInvDataToModel(InvariantsTableModel modelInvariants) {
		//TODO:
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions.size() == 0) return;
		
		ArrayList<Arc> arcs = pn.getArcs();
		if(arcs.size() == 0) return;
		
		ArrayList<ArrayList<Integer>> invMatrix = pn.getInvariantsMatrix();
		if(invMatrix == null || invMatrix.size() == 0) return;
		int invMatrixSize = invMatrix.size();

    	if(dataMatrix == null || dataMatrix.size() == 0) {
    		dataMatrix = new ArrayList<InvariantContainer>();
    		ArrayList<ArrayList<Integer>> nonMinimalInvariants = InvariantsTools.checkSupportMinimalityThorough(invMatrix);
    		ArrayList<ArrayList<Integer>> arcsInfoMatrix = InvariantsTools.getExtendedInvariantsInfo(invMatrix);
    		ArrayList<ArrayList<Integer>> inOutInfoMatrix = InvariantsTools.getInOutTransInfo(invMatrix);
    		ArrayList<Integer> invariantsClassVector = InvariantsTools.getInvariantsClassVector(invMatrix);
    		ArrayList<Integer> feasibleVector = InvariantsTools.getFeasibilityClassesStatic(invMatrix);
    		ArrayList<Integer> canonicalVector = InvariantsTools.getCanonicalInfo(invMatrix);
    		
    		//int iterIndex = 0;
    		for(int i=0; i<invMatrixSize; i++) {
    			ArrayList<Integer> invariant = invMatrix.get(i);
    			ArrayList<Integer> support = InvariantsTools.getSupport(invariant);
    			
    			InvariantContainer ic = new InvariantContainer();
    			ic.ID = i;
    			ic.transNumber = support.size();
    			
    			if(nonMinimalInvariants.get(i).size() == 0)
    				ic.minimal = true;
    			else
    				ic.minimal = false;

    			if(feasibleVector.get(i) == -1)
    				ic.feasible = false;
    			else
    				ic.feasible = true;
    		
    			ic.pureInTransitions = inOutInfoMatrix.get(i).get(0);
    			ic.inTransitions = inOutInfoMatrix.get(i).get(1);
    			ic.outTransitions = inOutInfoMatrix.get(i).get(2);
    			
    			ic.readArcs = arcsInfoMatrix.get(i).get(0);
    			ic.inhibitors = arcsInfoMatrix.get(i).get(1);
    			
    			if(invariantsClassVector.get(i) == 0) {
    				ic.normalInv = true;
    				ic.sub = false;
    				ic.sur = false;
    			} else if(invariantsClassVector.get(i) == 1) {
    				ic.normalInv = false;
    				ic.sub = false;
    				ic.sur = true;
    			} else if(invariantsClassVector.get(i) == -1) {
    				ic.normalInv = false;
    				ic.sub = true;
    				ic.sur = false;
    			} else {
    				ic.normalInv = false;
    				ic.sub = false;
    				ic.sur = false;
    			}
    			
    			if(canonicalVector.get(i) == 0)
    				ic.canonical = true;
    			else
    				ic.canonical = false;
    			
    			
    			dataMatrix.add(ic);
    		}	
    	}
    	
    	for(InvariantContainer ic : dataMatrix) {
    		modelInvariants.addNew(ic.ID, ic.transNumber, ic.minimal, ic.feasible, ic.pureInTransitions, ic.inTransitions, 
    				ic.outTransitions, ic.readArcs, ic.inhibitors, ic.sur, ic.sub, ic.normalInv, ic.canonical, ic.name);
    			
    	}
     	
    	
	}
	
	/**
	 * Metoda służąca do wypełniania tabeli inwariantów.
	 * @param modelInvariants InvariantsTableModel - model tablicy inwariantów
	 * @param invariantsMatrix ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param invSize ArrayList[Integer] invSize - wektor wielkości inwariantów
	 */
	public void addInvariantsToModel(InvariantsSimTableModel modelInvariants, ArrayList<ArrayList<Integer>> invariantsMatrix,
			int simSteps, boolean maximumMode) {
		StateSimulator ss = new StateSimulator();
		ss.initiateSim(NetType.BASIC, maximumMode);
		ss.simulateNetSimple(simSteps, false);
		ArrayList<Double> resVector = ss.getTransitionsAvgData();
		
		ArrayList<Integer> rowsWithZero = new ArrayList<Integer>();
		ArrayList<Integer> zeroDeadTrans = new ArrayList<Integer>();
		int transNo = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
		for(int i=0; i<transNo; i++)
			zeroDeadTrans.add(0); //to co pozostanie zerem jest niepokrytą żadnym inwariantem tranzycją

		for(int row=0; row<invariantsMatrix.size(); row++) {
			ArrayList<Integer> dataV = invariantsMatrix.get(row);
			ArrayList<String> newRow = new ArrayList<String>();
			
			ArrayList<Integer> invSupport = InvariantsTools.getSupport(dataV);
			 
			newRow.add(""+row);
			//newRow.add(""+invSize.get(row));
			newRow.add(""+invSupport.size());
			
			for(int t=0; t<dataV.size(); t++) { //dla każdej tranzycji
				int value = dataV.get(t);
				if(value>0) { //jeśli działa w ramach inwariantu
					zeroDeadTrans.set(t, 1); //ustaw stan tranzycji na aktywną w inwariancie
					double avg = resVector.get(t);
					
					if(avg == 0) { //dane o inwariantach z zagłodzonymi tranzycjami
						if(rowsWithZero.contains(row) == false)
							rowsWithZero.add(row);
						zeroDeadTrans.set(t, -1); //aktywna, ale zagłodzona
					}
					
					avg *= 100; // do 100%
					String cell = ""+value+"("+Tools.cutValue(avg)+"%)";
					newRow.add(cell);
				} else {
					newRow.add("");
					
				}
			}
			modelInvariants.addNew(newRow);
		}
		modelInvariants.setInfeasibleInvariants(rowsWithZero);
		modelInvariants.setZeroDeadTransitions(zeroDeadTrans);
	}

	/**
	 * Metoda ta zamienia miejscami obiekty w tablicy miejsc lub tranzycji, wpływając na identyfikatory
	 * powyższych.
	 * @param table JTable - tablica z zaznaczonymi wierszami
	 * @return boolean - true, jeśli udało się zamienić
	 */
	public boolean switchSelected(JTable table) {
		try {
			String name = table.getName();
			if(!name.equals("PlacesTable") && !name.equals("TransitionsTable")) {
				JOptionPane.showMessageDialog(null, "Swap operation allowed only for places or transitions.",
						"Invalid table", JOptionPane.WARNING_MESSAGE);
				return false;
			}
			
			int selRows[] = table.getSelectedRows();
			if(selRows.length != 2) {
				JOptionPane.showMessageDialog(null, "Please select two rows (SHIFT key + mouse click).",
						"Invalid number of rows selected", JOptionPane.WARNING_MESSAGE);
				return false;
			}
			
			GUIManager.getDefaultGUIManager().reset.reset2ndOrderData();
			
			if(name.equals("PlacesTable")) {
				ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
				int pos1 = selRows[0];
				int pos2 = selRows[1];
				Place p1 = places.get(pos1);
				Place p2 = places.get(pos2);
				
				pos1 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(p1); 
				pos2 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(p2);
				Collections.swap(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes(), pos1, pos2);
				
				GUIManager.getDefaultGUIManager().log("Swapping places "+p1.getName()+" and "+p2.getName()+" successfull.", "text", true);
			} else if(name.equals("TransitionTable")) {
				ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
				int pos1 = selRows[0];
				int pos2 = selRows[1];
				Transition t1 = transitions.get(pos1);
				Transition t2 = transitions.get(pos2);
				
				pos1 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(t1); 
				pos2 = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().indexOf(t2);
				Collections.swap(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes(), pos1, pos2);
				GUIManager.getDefaultGUIManager().log("Swapping transitions "+t1.getName()+" and "+t2.getName()+" successfull.", "text", true);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
