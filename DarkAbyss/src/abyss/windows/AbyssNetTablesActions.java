package abyss.windows;

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.simulator.StateSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.tables.PlacesTableModel;
import abyss.tables.TransitionsTableModel;

/**
 * Klasa z metodami obsługującymi okno tabel programu - klasy AbyssNetTables.
 * @author MR
 *
 */
public class AbyssNetTablesActions {
	private AbyssNetTables antWindow; 
	
	/**
	 * Konstruktor klasy.
	 * @param overlord AbyssNetTables - obiekt obsługiwanego okna
	 */
	public AbyssNetTablesActions(AbyssNetTables overlord) {
		antWindow = overlord;
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
		
		StateSimulator ss = new StateSimulator();
		ss.initiateSim(NetType.BASIC, false);
		ss.simulateNetSimple(10000, true);
		ArrayList<Double> resVector = ss.getPlacesAvgData();
		
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
			if(resVector.size() > 0)
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
			
			modelTransitions.addNew(index, name, postP, preP, (float)avgFired, inInv);
			//String[] dataRow = { ""+index, name, ""+postP, ""+preP, ""+Tools.cutValue(avgFired)+"%", ""+inInv};
			//model.addRow(dataRow);
		}
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
