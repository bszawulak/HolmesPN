package abyss.windows;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Place;
import abyss.math.Transition;

public class AbyssNetTablesActions {
	
	public AbyssNetTablesActions() {
		
	}
	
	public void cellClickAction(JTable table) {
		String name = table.getName();
		if(name.equals("PlacesTable")) {
  	    	int row = table.getSelectedRow();
  	    	int column = table.getSelectedColumn();
  	    	//cellClickedEvent(row, column);
		}
	}

	public void addPlacesToModel(DefaultTableModel model) {
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places.size() == 0) {
			return;
		}
		
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
			
			int avgTokens = 0; //TODO: SS
			
			String[] dataRow = { ""+index, name, ""+tokens, ""+inTrans, ""+outTrans, ""+avgTokens};
			model.addRow(dataRow);
		}
	}

	public void addTransitionsToModel(DefaultTableModel model) {
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		if(transitions.size() == 0) {
			return;
		}
		
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
			
			int avgFired = 0; //TODO: SS
			int inInv = 0; //TODO
			
			String[] dataRow = { ""+index, name, ""+postP, ""+preP, ""+avgFired, ""+inInv};
			model.addRow(dataRow);
		}
	}

}
