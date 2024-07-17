package holmes.analyse;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.darkgui.dockwindows.HolmesDockWindowsTable;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa odpowiedzialna za wykrywanie potencjalnie problematycznych regionów sieci i ich kolorowanie.
 */
public class ProblemDetector {
	GUIManager overlord;
	PetriNet pn;
	HolmesDockWindowsTable subwindow;

	public ProblemDetector(HolmesDockWindowsTable holmesDockWindowsTable) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.subwindow = holmesDockWindowsTable;
	}
	
	/**
	 * Metoda zleca wykrycie miejsc dla sub/sur/non-inv i ich pokolorowanie.
	 */
	public void markSubSurNonInvariantsPlaces() {
		ArrayList<ArrayList<Object>> result = detectInvProblemPlaces();
		if(result == null) {
			return;
		}
		pn.resetNetColors();
		//ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		ArrayList<Object> res_places = result.get(0);
		ArrayList<Object> res_descr = result.get(1);
		
		for(int p=0; p<res_places.size(); p++) {
			Place place = (Place)res_places.get(p);
			String descr = (String)res_descr.get(p);
			
			place.drawGraphBoxP.setColorWithNumber(true, Color.GREEN, false, 0.0, true, descr, 0, 0, 0, 0);
		}
		
		pn.repaintAllGraphPanels();
	}
	
	/**
	 * Metoda zleca wykrycie a następnie koloruje miejsca wejściowe/wyjściowe.
	 */
	public void markIOPlaces() {
		ArrayList<Place> inPlaces = detectInPlaces();
		ArrayList<Place> outPlaces = detectOutPlaces();
		pn.resetNetColors();
		int inP = 0;
		int outP = 0;
		if(inPlaces != null) {
			inP = inPlaces.size();
			for(int p=0; p<inP; p++) {
				Place place = inPlaces.get(p);
				
				place.drawGraphBoxP.setColorWithNumber(true, new Color(255,0,127), false, 0.0, true, "IN-place", 0, 0, 0, 0);
			}
		}
		
		if(outPlaces != null) {
			outP = outPlaces.size();
			for(int p=0; p<outP; p++) {
				Place place = outPlaces.get(p);
				
				place.drawGraphBoxP.setColorWithNumber(true, new Color(255,153,204), false, 0.0, true, "OUT-place", 0, 0, 0, 0);
			}
		}
		subwindow.fixIOPlaces.setText("Input: "+inP+" / Output: "+outP);
		pn.repaintAllGraphPanels();
	}
	
	/**
	 * Metoda zleca detekcję tranzycji I/O i ich podświetlenie.
	 */
	public void markIOTransitions() {
		ArrayList<Transition> inTransitions = detectInTrans();
		ArrayList<Transition> outTransitions = detectOutTrans();
		pn.resetNetColors();
		int inT = 0;
		int outT = 0;
		if(inTransitions != null) {
			inT = inTransitions.size();
			for(int p=0; p<inT; p++) {
				Transition trans = inTransitions.get(p);
				trans.drawGraphBoxT.setColorWithNumber(true, new Color(0,153,153), false, 0.0, true, "IN-trans", 0, 0, 0, 0);
			}
		}
		
		if(outTransitions != null) {
			outT = outTransitions.size();
			for(int t=0; t<outT; t++) {
				Transition trans = outTransitions.get(t);
				trans.drawGraphBoxT.setColorWithNumber(true, new Color(153,255,255), false, 0.0, true, "OUT-trans", 0, 0, 0, 0);
			}
		}
		subwindow.fixIOTransitions.setText("Input: "+inT+" / Output: "+outT);
		pn.repaintAllGraphPanels();
	}
	
	/**
	 * Metoda oznacza T lub P liniowe
	 */
	public void markLinearRegions() {
		ArrayList<Place> linearPlaces = detectLinearPlaces();
		ArrayList<Transition> linearTransitions = detectLinearTrans();
		pn.resetNetColors();
		int linP = 0;
		int linT = 0;
		if(linearPlaces != null) {
			linP = linearPlaces.size();
			for(int p=0; p<linP; p++) {
				Place place = linearPlaces.get(p);
				
				place.drawGraphBoxP.setColorWithNumber(true, new Color(0,102,0), false, 0.0, true, "Linear place", 0, 0, 0, 0);
			}
		}
		
		if(linearTransitions != null) {
			linT = linearTransitions.size();
			for(int p=0; p<linT; p++) {
				Transition trans = linearTransitions.get(p);
				trans.drawGraphBoxT.setColorWithNumber(true, new Color(128,255,0), false, 0.0, true, "Linear trans", 0, 0, 0, 0);
			}
		}
		
		ArrayList<Place> places = pn.getPlaces();
		boolean regions = true;
		if(regions) {
			//int counter = -1;
			for(Place place : places) {
				if(!place.drawGraphBoxP.isColorChanged())
					continue;
				
				for(ElementLocation el : place.getElementLocations()) {
					for(Arc arc : el.getInArcs()) {
						if(((Transition)arc.getStartNode()).drawGraphBoxT.isColorChanged()) {
							((Transition)arc.getStartNode()).drawGraphBoxT.setColorWithNumber(
								true, Color.RED, false, 0.0, true, "LINEAR REGION", 0, 0, 0, 0);
							place.drawGraphBoxP.setColorWithNumber(
									true, Color.RED, false, 0.0, true, "LINEAR REGION", 0, 0, 0, 0);
						}
					}
					
					for(Arc arc : el.getOutArcs()) {
						if(((Transition)arc.getEndNode()).drawGraphBoxT.isColorChanged()) {
							((Transition)arc.getEndNode()).drawGraphBoxT.setColorWithNumber(
								true, Color.RED, false, 0.0, true, "LINEAR REGION", 0, 0, 0, 0);
							place.drawGraphBoxP.setColorWithNumber(
									true, Color.RED, false, 0.0, true, "LINEAR REGION", 0, 0, 0, 0);
						}
					}
				}
			}
		}
		
		subwindow.fixlinearTrans.setText("Transitions: "+linT+" / Places: "+linP);
		pn.repaintAllGraphPanels();
	}
	
	
	
	public ArrayList<Place> detectInPlaces() {
		ArrayList<Place> inPlaces = new ArrayList<Place>();
		ArrayList<Place> places = pn.getPlaces();
		
		if(places == null || places.isEmpty())
			return null;
		
		for(Place place : places) {
			boolean ok = true;
			for(ElementLocation el : place.getElementLocations()) {
				if(!el.getInArcs().isEmpty()) { //nie powinno być żadnych
					ok = false;
					break;
				}
			}

            if (ok) {
				inPlaces.add(place);
			}
        }
		
		return inPlaces;
	}
	
	public ArrayList<Place> detectOutPlaces() {
		ArrayList<Place> outPlaces = new ArrayList<Place>();
		ArrayList<Place> places = pn.getPlaces();
		if(places == null || places.isEmpty())
			return null;
		
		for(Place place : places) {
			boolean ok = true;
			for(ElementLocation el : place.getElementLocations()) {
				if(!el.getOutArcs().isEmpty()) { //nie powinno być żadnych
					ok = false;
					break;
				}
			}

            if (ok) {
				outPlaces.add(place);
			}
        }
		
		return outPlaces;
	}
	
	public ArrayList<Transition> detectInTrans() {
		ArrayList<Transition> inTrans = new ArrayList<Transition>();
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.isEmpty())
			return null;
		
		for(Transition trans : transitions) {
			boolean ok = true;
			for(ElementLocation el : trans.getElementLocations()) {
				if(!el.getInArcs().isEmpty()) { //nie powinno być żadnych
					
					//TODO: pure ?
					
					ok = false;
					break;
				}
			}

            if (ok) {
				inTrans.add(trans);
			}
        }
		return inTrans;
	}
	
	public ArrayList<Transition> detectOutTrans() {
		ArrayList<Transition> outTrans = new ArrayList<Transition>();
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.isEmpty())
			return null;
		
		for(Transition trans : transitions) {
			boolean ok = true;
			for(ElementLocation el : trans.getElementLocations()) {
				if(!el.getOutArcs().isEmpty()) { //nie powinno być żadnych
					
					//TODO: pure ?
					
					ok = false;
					break;
				}
			}

            if (ok) {
				outTrans.add(trans);
			}
        }
		return outTrans;
	}
	
	public ArrayList<Place> detectLinearPlaces() {
		ArrayList<Place> linearPlaces = new ArrayList<Place>();
		ArrayList<Place> places = pn.getPlaces();
		if(places == null || places.isEmpty())
			return null;
		int inArcs;
		int ourArcs;
		
		int counter = -1;
		for(Place place : places) {
			counter++;
			inArcs = 0;
			ourArcs = 0;
			boolean cancel = false;
			
			for(ElementLocation el : place.getElementLocations()) {
				inArcs += el.getInArcs().size();
				if(inArcs > 1) {
					cancel = true;
					break;
				}
			}
			
			if(cancel || inArcs==0)
				continue;
			
			//jeśli tu jesteśmy, miejsce ma dokładnie 1 łuk wejściowy
			
			for(ElementLocation el : place.getElementLocations()) {
				ourArcs += el.getOutArcs().size();
				if(ourArcs > 1) {
					cancel = true;
					break;
				}
			}

            if (!cancel && ourArcs != 0) {
                linearPlaces.add(place);
            }
        }
		
		return linearPlaces;
	}
	
	public ArrayList<Transition> detectLinearTrans() {
		ArrayList<Transition> linearTrans = new ArrayList<Transition>();
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.isEmpty())
			return null;
		int inArcs;
		int outArcs;
		
		for(Transition trans : transitions) {
			inArcs = 0;
			outArcs = 0;
			boolean cancel = false;
			
			for(ElementLocation el : trans.getElementLocations()) {
				inArcs += el.getInArcs().size();
				if(inArcs > 1) {
					cancel = true;
					break;
				}
			}
			
			if(cancel || inArcs==0)
				continue;
			
			//jeśli tu jesteśmy, miejsce ma dokładnie 1 łuk wejściowy
			
			for(ElementLocation el : trans.getElementLocations()) {
				outArcs += el.getOutArcs().size();
				if(outArcs > 1) {
					cancel = true;
					break;
				}
			}

            if (!cancel && outArcs != 0) {
                linearTrans.add(trans);
            }
        }
		return linearTrans;
	}
	
	/**
	 * Metoda wykrywa miejsca, dla których wektor nie 'wyzerował' macierzy incydencji.
	 * @return ArrayList[ArrayList[Object]] - dwa wektory, pierwszy to wektor miejsc, drugi - opisów do wyświetlenia
	 */
	public ArrayList<ArrayList<Object>> detectInvProblemPlaces() {
		ArrayList<ArrayList<Integer>> invariants = pn.getT_InvMatrix();
		if(invariants==null || invariants.isEmpty()) {
			JOptionPane.showMessageDialog(null, "T-invariants matrix has not been found.", 
					"No t-invariants", JOptionPane.WARNING_MESSAGE);
			return null;
		}
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		
		ArrayList<Object> res_places = new ArrayList<Object>();
		ArrayList<Object> res_descr = new ArrayList<Object>();
		ArrayList<ArrayList<Object>> result = new ArrayList<>();
		result.add(res_places);
		result.add(res_descr);

		InvariantsCalculator ic = new InvariantsCalculator(true);
		ArrayList<ArrayList<Integer>> matrix = InvariantsTools.analyseInvariantDetails(ic.getCMatrix(), invariants, true);
		
		int invNumber = matrix.get(0).get(0);
		int surNumber = matrix.get(0).get(1);
		int subNumber = matrix.get(0).get(2);
		int nonNumber = matrix.get(0).get(3);
		subwindow.fixInvariants.setText("Normal: "+invNumber+" / Non-inv.: "+nonNumber);
		subwindow.fixInvariants2.setText("Sub-inv.: "+subNumber+" / Sur-inv: "+surNumber);
		
		//test:
		/*
		matrix.get(1).set(0, 1); surNumber++;
		matrix.get(1).set(1, 1); surNumber++;
		matrix.get(2).set(0, 1); subNumber++;
		matrix.get(2).set(12, 1); subNumber++;
		
		matrix.get(3).set(3, 1); nonNumber++;
		matrix.get(3).set(5, 1); nonNumber++;
		matrix.get(3).set(7, 1); nonNumber++;
		matrix.get(3).set(11, 1); nonNumber++;
		*/
		if(surNumber>0) {
			ArrayList<Integer> surVector = matrix.get(1);
			for(int i=0; i<surVector.size(); i++) {
				int value = surVector.get(i);
				if(value != 0) {
					res_places.add(places.get(i));
					res_descr.add("Sur:"+value);
				}
			}
		}
		
		if(subNumber>0) {
			ArrayList<Integer> subVector = matrix.get(2);
			for(int i=0; i<subVector.size(); i++) {
				int value = subVector.get(i);
				if(value != 0) {
					Place p = places.get(i);
					if(res_places.contains(p)) {
						int index = res_places.indexOf(p);
						String oldVal = res_descr.get(index).toString();
						oldVal += (" / Sub:"+value);
						res_descr.set(index, oldVal);
					} else {
						res_places.add(places.get(i));
						res_descr.add("Sub:"+value);
					}
				}
			}
		}
		
		if(nonNumber>0) {
			ArrayList<Integer> nonVector = matrix.get(3);
			for(int i=0; i<nonVector.size(); i++) {
				int value = nonVector.get(i);
				if(value != 0) {
					Place p = places.get(i);
					if(res_places.contains(p)) {
						int index = res_places.indexOf(p);
						String oldVal = res_descr.get(index).toString();
						oldVal += (" / NonI:"+value);
						res_descr.set(index, oldVal);
					} else {
						res_places.add(places.get(i));
						res_descr.add("NonI:"+value);
					}
				}
			}
		}
		return result;
	}
}
