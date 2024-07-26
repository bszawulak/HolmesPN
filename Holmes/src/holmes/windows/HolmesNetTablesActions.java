package holmes.windows;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import holmes.analyse.InvariantsTools;
import holmes.analyse.TimeComputations;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.simulators.StateSimulator;
import holmes.tables.InvariantContainer;
import holmes.tables.InvariantsSimulatorTableModel;
import holmes.tables.InvariantsTableModel;
import holmes.tables.PlacesTableModel;
import holmes.tables.TransitionsTableModel;
import holmes.utilities.Tools;

/**
 * Klasa z metodami obsługującymi okno tabel programu - klasy HolmesNetTables.
 */
public class HolmesNetTablesActions {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private HolmesNetTables antWindow; 
	public ArrayList<InvariantContainer> dataMatrix;
	
	/**
	 * Konstruktor klasy.
	 * @param overlord HolmesNetTables - obiekt obsługiwanego okna
	 */
	public HolmesNetTablesActions(HolmesNetTables overlord) {
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
	  	    	//int index = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
	  	    	int index = Integer.parseInt(table.getValueAt(row, 0).toString());
	  	    	
	  	    	HolmesNodeInfo window = new HolmesNodeInfo(
	  	    			overlord.getWorkspace().getProject().getPlaces().get(index), antWindow);
	  	    	window.setVisible(true);
	  	    	//int column = table.getSelectedColumn();
	  	    	//cellClickedEvent(row, column);
			} else if(name.equals("TransitionTable")) {
				int row = table.getSelectedRow();
	  	    	antWindow.currentClickedRow = row;
	  	    	//int index = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
	  	    	int index = Integer.parseInt(table.getValueAt(row, 0).toString());
	  	    	HolmesNodeInfo window = new HolmesNodeInfo(
						overlord.getWorkspace().getProject().getTransitions().get(index), 
	  	    			antWindow);
	  	    	window.setVisible(true);
			}
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00476exception")+" "+ex.getMessage(), "error", true);
		}
	}
	
	/**
	 * Metoda wypełniająca tabelę miejsc danymi.
	 * @param modelPlaces PlacesTableModel - obiekt danych
	 */
	public void addPlacesToModel(PlacesTableModel modelPlaces) {
		ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
		if(places.isEmpty()) {
			return;
		}
		
		ArrayList<Double> resVector;
		StateSimulator ss = new StateSimulator();
		
		SimulatorGlobals ownSettings = new SimulatorGlobals();
		ownSettings.setNetType(SimulatorGlobals.SimNetType.BASIC, false);
		ownSettings.setMaxMode(false);
		ownSettings.setSingleMode(false);
		ss.initiateSim(false, ownSettings);
		ss.simulateNetSimple(10000, true, false);
		resVector = ss.getPlacesAvgData();
		
		int iterIndex = -1;
		for(Place p : places) {
			iterIndex++;
			int index = places.indexOf(p);
			
			if(iterIndex != index) {
				overlord.log(lang.getText("LOGentry00477"), "warning", true);
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
			if(resVector != null && !resVector.isEmpty())
				avgTokens = resVector.get(iterIndex);
			
			modelPlaces.addNew(index, name, tokens, inTrans, outTrans, (float)avgTokens);
		}
	}

	/**
	 * Metoda wypełniająca tabelę tramzycji danymi.
	 * @param modelTransitions DefaultTableModel - obiekt danych
	 */
	public void addTransitionsToModel(TransitionsTableModel modelTransitions) {
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		if(transitions.isEmpty()) {
			return;
		}
		
		StateSimulator ss = new StateSimulator();
		SimulatorGlobals ownSettings = new SimulatorGlobals();
		ownSettings.setNetType(SimulatorGlobals.SimNetType.BASIC, false);
		ownSettings.setMaxMode(false);
		ownSettings.setSingleMode(false);
		ss.initiateSim(false, ownSettings);
		ss.simulateNetSimple(10000, false, false);
		ArrayList<Double> resVector = ss.getTransitionsAvgData();
		
		int iterIndex = -1;
		for(Transition t : transitions) {
			iterIndex++;
			int index = transitions.indexOf(t);
			if(iterIndex != index) {
				overlord.log(lang.getText("LOGentry00477"), "warning", true);
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
			if(!resVector.isEmpty())
				avgFired = resVector.get(iterIndex);
			avgFired *= 100;
			int inInv = 0;
			
			modelTransitions.addNew(index, name, preP, postP, (float)avgFired, inInv);
		}
	}

	/**
	 * Metoda wypełniająca tabelę inwariantów podstawowymi informacjami o nich.
	 * @param modelInvariants DefaultTableModel - obiekt danych
	 */
	public void addBasicInvDataToModel(InvariantsTableModel modelInvariants) {
		//TODO:
		PetriNet pn = overlord.getWorkspace().getProject();
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions.isEmpty()) return;
		
		ArrayList<Arc> arcs = pn.getArcs();
		if(arcs.isEmpty()) return;
		
		ArrayList<ArrayList<Integer>> invMatrix = pn.getT_InvMatrix();
		if(invMatrix == null || invMatrix.isEmpty()) return;
		int invMatrixSize = invMatrix.size();

    	if(dataMatrix == null || dataMatrix.isEmpty()) {
    		dataMatrix = new ArrayList<InvariantContainer>();
    		ArrayList<ArrayList<Integer>> nonMinimalInvariants = InvariantsTools.checkSupportMinimalityThorough(invMatrix);
    		ArrayList<ArrayList<Integer>> arcsInfoMatrix = InvariantsTools.getExtendedT_invariantsInfo(invMatrix, true);
    		ArrayList<ArrayList<Integer>> inOutInfoMatrix = InvariantsTools.getT_invInOutTransInfo(invMatrix);
    		ArrayList<Integer> invariantsClassVector = InvariantsTools.getT_invariantsClassVector(invMatrix);
    		ArrayList<Integer> feasibleVector = InvariantsTools.getT_invFeasibilityClassesStatic(invMatrix);
    		ArrayList<Integer> canonicalVector = InvariantsTools.getCanonicalInfo(invMatrix);
    		
    		//int iterIndex = 0;
    		for(int i=0; i<invMatrixSize; i++) {
    			ArrayList<Integer> invariant = invMatrix.get(i);
    			ArrayList<Integer> support = InvariantsTools.getSupport(invariant);
    			
    			InvariantContainer ic = new InvariantContainer();
    			ic.ID = i;
    			ic.name = pn.accessT_InvDescriptions().get(i);
    			ic.transNumber = support.size();

				ic.minimal = nonMinimalInvariants.get(i).isEmpty();
				ic.feasible = feasibleVector.get(i) != -1;
    		
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
				ic.canonical = canonicalVector.get(i) == 0;
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
	 * @param simSteps int - ile kroków symulacji
	 * @param maximumMode boolean - true: tryb maximum
	 * @param singleMode boolean - true: 1 odpalenie tranzycji na turę(krok)
	 * @param invSimNetType NetType - rodzaj symulacji sieci
	 */
	public void addInvariantsToModel(InvariantsSimulatorTableModel modelInvariants, ArrayList<ArrayList<Integer>> invariantsMatrix,
			int simSteps, boolean maximumMode, boolean singleMode, SimulatorGlobals.SimNetType invSimNetType) {
		StateSimulator ss = new StateSimulator();
		
		SimulatorGlobals ownSettings = new SimulatorGlobals();
		ownSettings.setNetType(invSimNetType, false);
		ownSettings.setMaxMode(maximumMode);
		ownSettings.setSingleMode(singleMode);
		ss.initiateSim(false, ownSettings);
		ss.simulateNetSimple(simSteps, false, false);
		ArrayList<Double> resVector = ss.getTransitionsAvgData();
		
		ArrayList<Integer> rowsWithZero = new ArrayList<Integer>();
		ArrayList<Integer> zeroDeadTrans = new ArrayList<Integer>();
		int transNo = overlord.getWorkspace().getProject().getTransitions().size();
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
						if(!rowsWithZero.contains(row))
							rowsWithZero.add(row);
						zeroDeadTrans.set(t, -1); //aktywna, ale zagłodzona
					}
					
					avg *= 100; // do 100%
					String cell = value+"("+Tools.cutValue(avg)+"%)";
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
				JOptionPane.showMessageDialog(null, lang.getText("HNTAwin_entry001"),
						lang.getText("HNTAwin_entry001t"), JOptionPane.WARNING_MESSAGE);
				return false;
			}
			
			int[] selRows = table.getSelectedRows();
			if(selRows.length != 2) {
				JOptionPane.showMessageDialog(null, lang.getText("HNTAwin_entry002"),
						lang.getText("HNTAwin_entry002t"), JOptionPane.WARNING_MESSAGE);
				return false;
			}

			overlord.reset.reset2ndOrderData(true);
			
			if(name.equals("PlacesTable")) {
				ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
				int pos1 = selRows[0];
				int pos2 = selRows[1];
				Place p1 = places.get(pos1);
				Place p2 = places.get(pos2);
				
				pos1 = overlord.getWorkspace().getProject().getNodes().indexOf(p1); 
				pos2 = overlord.getWorkspace().getProject().getNodes().indexOf(p2);
				Collections.swap(overlord.getWorkspace().getProject().getNodes(), pos1, pos2);

				overlord.log("Swapping places "+p1.getName()+" and "+p2.getName()+" successfull.", "text", true);
			} else if(name.equals("TransitionTable")) { //TODO coś tu jest nie tak!!!!!
				ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
				int pos1 = selRows[0];
				int pos2 = selRows[1];
				Transition t1 = transitions.get(pos1);
				Transition t2 = transitions.get(pos2);
				
				pos1 = overlord.getWorkspace().getProject().getNodes().indexOf(t1); 
				pos2 = overlord.getWorkspace().getProject().getNodes().indexOf(t2);
				Collections.swap(overlord.getWorkspace().getProject().getNodes(), pos1, pos2);
				overlord.log("Swapping transitions "+t1.getName()+" and "+t2.getName()+" successfull.", "text", true);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Metoda pokazuje dane czasowe inwariantów.
	 */
	public void showTimeDataNotepad() {
		PetriNet pn = overlord.getWorkspace().getProject();
		ArrayList<Transition> transitions = pn.getTransitions();
		ArrayList<ArrayList<Integer>> invMatrix = pn.getT_InvMatrix();
		if(invMatrix == null || invMatrix.isEmpty())
			return;
		int invMatrixSize = invMatrix.size();
		
		HolmesNotepad note = new HolmesNotepad(800, 600);
		
		note.addTextLineNL(" No.           Min.         Avg.          Max.    PN   TPN  DPN TDPN", "text");
		for(int i=0; i<invMatrixSize; i++) {
			ArrayList<Integer> invariant = invMatrix.get(i);
			ArrayList<Double> timeVector = TimeComputations.getT_InvTimeValues(invariant, transitions);
			
			String line = "";
			String eftStr = String.format("%.2f", timeVector.get(0)+timeVector.get(3));
			String lftStr = String.format("%.2f", timeVector.get(1)+timeVector.get(3));
			String avgStr = String.format("%.2f", timeVector.get(2)+timeVector.get(3));

			String normal = ""+timeVector.get(4).intValue();
			String tpn = ""+timeVector.get(5).intValue();
			String dpn = ""+timeVector.get(6).intValue();
			String tdpn = ""+timeVector.get(7).intValue();
			
			line += Tools.setToSize("i_"+i, 5, false);
			line += Tools.setToSize(eftStr, 14, true);
			line += Tools.setToSize(avgStr, 14, true);
			line += Tools.setToSize(lftStr, 14, true);
			line += "    ";
			line += Tools.setToSize(normal, 5, false);
			line += Tools.setToSize(tpn, 5, false);
			line += Tools.setToSize(dpn, 5, false);
			line += Tools.setToSize(tdpn, 5, false);
			note.addTextLineNL(line, "text");
		}
		
		note.setCaretFirstLine();
		note.setVisible(true);
	}
}
