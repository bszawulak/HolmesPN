package abyss.windows;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.StateSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.utilities.Tools;

public class AbyssNetTablesActions {
	private AbyssNetTables antWindow; 
	
	public AbyssNetTablesActions(AbyssNetTables overlord) {
		antWindow = overlord;
	}
	
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
				
			}
		} catch (Exception e) {
			
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
		
		StateSimulator ss = new StateSimulator();
		ss.initiateSim(NetType.BASIC, false);
		ss.simulateNetSimple(10000);
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
				avgFired = resVector.get(iterIndex); //TODO: SS
			avgFired *= 100;
			int inInv = 0; //TODO
			
			String[] dataRow = { ""+index, name, ""+postP, ""+preP, ""+Tools.cutValue(avgFired)+"%", ""+inInv};
			model.addRow(dataRow);
		}
	}

	public class SimState implements Callable<Boolean> {
		NetSimulator ns;
		public SimState(NetSimulator ns) {
			this.ns = ns;
		}
        public Boolean call() throws Exception {
        	StateSimulator ss = new StateSimulator();
    		ss.initiateSim(NetType.BASIC, false);
    		ss.simulateNetSimple(10000);
            return true;
        }
    }
}
