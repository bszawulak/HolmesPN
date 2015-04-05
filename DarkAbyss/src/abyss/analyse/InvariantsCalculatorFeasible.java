package abyss.analyse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.Arc.TypesOfArcs;
import abyss.varia.Check;

/**
 * Klasa odpowiedzialna za szukanie inwariantów wykonalnych (feasible invariants). Literatura: <br>
 * "Application of Petri net based analysis techniques to signal transduction pathways" <br>
 * Andrea Sackmann, Monika Heiner, Ina Koch; BMC Bioinformatics, 2006, 7:482
 * 
 * @author MR
 *
 */
public class InvariantsCalculatorFeasible {
	
	private String status = "";
	private boolean success = false;
	private ArrayList<ArrayList<Integer>> invariants;
	private ArrayList<ArrayList<Integer>> feasibleInv;
	private ArrayList<ArrayList<Integer>> non_feasibleInv;
	private ArrayList<ArrayList<Integer>> f_invariantsCreated;
	private ArrayList<Transition> transitions;
	private ArrayList<Integer> readArcTransLocations;
	private ArrayList<Place> places;
	private boolean tInv = true;
	
	/**
	 * Konstruktor klasy InvariantsCalculatorFeasible.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param isTInv boolean - true jeśli to T-inwarianty, false jeśli P-inw.
	 */
	public InvariantsCalculatorFeasible(ArrayList<ArrayList<Integer>> invariants, boolean isTInv) {
		this.invariants = invariants;
		this.tInv = isTInv;
		f_invariantsCreated = new ArrayList<ArrayList<Integer>>();
	}
	
	/**
	 * Metoda startowa szukania zbioru inwariantów wykonalnych.
	 * @return
	 */
	public ArrayList<ArrayList<Integer>> getMinFeasible() {
		ArrayList<Integer> arcClassCount = Check.getArcClassCount();
		if(arcClassCount.get(1) == 0) { //brak łuków odczytu
			status = "No read arcs. All invariants are feasible.";
			success = false;
			return invariants;
		}
		
		if(tInv == true) {
			transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
			readArcTransLocations = getReadArcTransitions();
			
			searchFTInv();
			
			status = "Set computed. Returning.";
			success = true;
			//return f_invariantsCreated;
			return feasibleSet();
		} else {
			JOptionPane.showMessageDialog(null, "Feasible P-inv search? Not implemented.", 
					"Warning", JOptionPane.WARNING_MESSAGE);
			
			status = "P-invariants set - cannot compute feasibility.";
			success = false;
			return invariants;
		}
	}
	
	/**
	 * Główna metoda odpowiedzialna za utworzenie zbioru inwariantów wykonalnych.
	 */
	private void searchFTInv() {
		partitionInvariants();
		
		int size = non_feasibleInv.size();
		for(int i=0; i<size; i++) {
			ArrayList<ArrayList<Integer>> invToCombine = new ArrayList<ArrayList<Integer>>(); //lista znalezionych
			
			ArrayList<Integer> nonFInvariant = non_feasibleInv.get(i); 
			ArrayList<Integer> trans_RA = getInfeasibleTransitions(nonFInvariant);
			
			//zidentyfikuj ile miejsc jest związanych z każdą tranzycją (może być więcej niż 1!)
			ArrayList<Place> places_RA = getRA_Places(trans_RA);
			//dla każdego z powyższych miejsc należy zapewnić tokeny poprzez minimalne inwarianty
			
			for(Place pl : places_RA) {
				ArrayList<Integer> connectedTransitions = getConnectedTransitionsSet(pl);
				ArrayList<Integer> minimalFeasibleInvariant = findMinFeasibleSimple(connectedTransitions);
				invToCombine.add(minimalFeasibleInvariant);
			}
			invToCombine.add(nonFInvariant);
			
			ArrayList<Integer> newFeasible = createFeasibleInvariant(invToCombine);
			f_invariantsCreated.add(newFeasible);
		}
	}
	
	//TODO: metoda uproszczona: 
	/**
	 * (SimpleSearchMode) Metoda szuka 'najmniejszego' inwariantu zawierającego jedną z tranzycji w zbiorze 
	 * connectedTransitions.
	 * @param connectedTransitions ArrayList[Integer] - zbiór tranzycji połączony z miejscem połączonym łukiem odczytu
	 * z Tranzycją, która wchodzi w skład inwariantu dla którego szukamy min-feasible aby go nimi wzbogacić.
	 * @return ArrayList[Integer] - inwariant wynikowy
	 */
	private ArrayList<Integer> findMinFeasibleSimple(ArrayList<Integer> connectedTransitions) {
		int minSupportCardinality = feasibleInv.get(0).size();
		int minSupportSum = 9999999;
		int indexFound = -1;
		int feasibleSize = feasibleInv.size();
		
		for(int transLoc : connectedTransitions) {
			
			//for(ArrayList<Integer> inv : feasibleInv) {
			for(int i=0; i<feasibleSize; i++) {
				ArrayList<Integer> inv = feasibleInv.get(i);
				
				if(inv.get(transLoc) == 0)
					continue;
				
				int minSC = 0; //cardinality
				int minSS = 0; //sum
				boolean dontBother = false;
				
				for(int el : inv) { //przebadaj inwariant jako kandydata do minimalnego
					if(el != 0) {
						minSC++;
						minSS += el;
						if(minSC > minSupportCardinality) {
							dontBother = true; //odpada, za duży, nie marnuj więcej czasu
							break;
						}
					}
				}
				
				if(dontBother == false) { //zbadaj na minimalność (w kontekście procedury feasibility-search)
					if(minSC < minSupportCardinality) {
						minSupportCardinality = minSC;
						minSupportSum = minSS;
						indexFound = i;
					} else if(minSC == minSupportCardinality) {
						if(minSS < minSupportSum) {
							minSupportCardinality = minSC;
							minSupportSum = minSS;
							indexFound = i;
						}
					}
				}
			}
		}
		
		return feasibleInv.get(indexFound);
	}
	
	/**
	 * Metoda zwraca nowy zbiór inwariantów - minimalne wykonalne oraz nie-minimalne ale wykonalne (feasible).
	 * @return ArrayList[ArrayList[Integer]] - zbiór inwariantów
	 */
	private ArrayList<ArrayList<Integer>> feasibleSet() {
		ArrayList<ArrayList<Integer>> resultSet = new ArrayList<ArrayList<Integer>>();
		for(ArrayList<Integer> minFeasibleInvariant : feasibleInv)
			resultSet.add(minFeasibleInvariant);
		
		for(ArrayList<Integer> nonMinFeasibleInvariant : f_invariantsCreated)
			resultSet.add(nonMinFeasibleInvariant);
		
		return resultSet;
	}
	
	/**
	 * Metoda tworzy nowy inwariant poprzez połączenie wszystkich inwariantów z macierzy invToCombine w
	 * jeden - nie minimalny, ale na pewno wykonalny.
	 * @param invToCombine ArrayList[ArrayList[Integer]] - macierz inwariantów do połączenia
	 * @return ArrayList[Integer] - inwariant będący kombinacją liniową (multiplicity=1) przesłanych do metody
	 */
	private ArrayList<Integer> createFeasibleInvariant(ArrayList<ArrayList<Integer>> invToCombine) {
		ArrayList<Integer> vector = new ArrayList<Integer>();
		int invSize = invToCombine.get(0).size();
		for(int i=0; i<invSize; i++)
			vector.add(0);
		
		for(ArrayList<Integer> invariant : invToCombine) { //zbuduj kombinację liniową
			ArrayList<Integer> support = InvariantsTools.getSupport(invariant);
			for(int el : support) {
				int oldValue = vector.get(el);
				oldValue += invariant.get(el);
				vector.set(el, oldValue);
			}
		}
		return vector;
	}

	/**
	 * Metoda zwraca zbiór lokalizacji tranzycji związanych łukami wejściowymi z danym miejscem.
	 * @param place Place - miejsce
	 * @return ArrayList[Integer] - lista lokalizacji tranzycji
	 */
	private ArrayList<Integer> getConnectedTransitionsSet(Place place) {
		ArrayList<Integer> connectedTransitions = new ArrayList<Integer>();

		for(ElementLocation el : place.getElementLocations()) {
			for(Arc a : el.getInArcs()) { //tylko łuki wejściowe
				if(a.getArcType() == TypesOfArcs.READARC || a.getArcType() == TypesOfArcs.INHIBITOR) 
					continue; //czyli poniższe działa tylko dla: NORMAL, RESET i EQUAL

				Transition trans = (Transition) a.getStartNode();
				int pos = transitions.indexOf(trans);
				if(connectedTransitions.contains(pos) == false) {
					connectedTransitions.add(pos);
				} else {
					GUIManager.getDefaultGUIManager().log("Internal error, net structure not canonical.", "error", true);
				}
				
			}
		}
		return connectedTransitions;
	}

	/**
	 * Metoda zwraca listę miejsc związanych łukami odczytu z tranzycjami inwariantu.
	 * @param trans_RA ArrayList[Integer] - lista tranzycji związanych z read-arc 
	 * @return ArrayList[Place] - wektor miejsc
	 */
	private ArrayList<Place> getRA_Places(ArrayList<Integer> trans_RA) {
		ArrayList<Place> resultPlaces = new ArrayList<Place>();
		for(int tr : trans_RA) {
			Transition transition = transitions.get(tr);
			
			for(ElementLocation el : transition.getElementLocations()) {
				for(Arc a : el.getOutArcs()) {
					if(a.getArcType() != TypesOfArcs.READARC)
						continue;
					
					Place p = (Place) a.getEndNode();
					if(resultPlaces.contains(p) == false)
						resultPlaces.add(p);
					else {
						GUIManager.getDefaultGUIManager().log("Internal error, multiple readarcs, net not canonical. Place: "+p.getName(), "error", true);
					}
				}
			}
			
		}
		
		return resultPlaces;
	}

	/**
	 * Metoda dzieli zbiór inwariantów na wykonalne i niewykonalne.
	 */
	private void partitionInvariants() {
		non_feasibleInv = new ArrayList<ArrayList<Integer>>();
		feasibleInv = new ArrayList<ArrayList<Integer>>();
		int invSize = invariants.size();
		for(int i=0; i<invSize; i++) {
			ArrayList<Integer> invariant = invariants.get(i);
			ArrayList<Integer> support = InvariantsTools.getSupport(invariant);
			
			if(isNonFeasible(support) == true)
				non_feasibleInv.add(invariant);
			else
				feasibleInv.add(invariant);
		}
	}
	
	/**
	 * Metoda zwraca pozycje wszystkich tranzycji, do których prowadzą łuki odczytu.
	 * @return ArrayList[Integer] - pozycje tranzycji z readarc
	 */
	private ArrayList<Integer> getReadArcTransitions() {
		ArrayList<Integer> raTrans = new ArrayList<Integer>();
		ArrayList<Arc> arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		for(Arc a : arcs) {
			if(a.getArcType() == TypesOfArcs.READARC) { //tylko łuki odczytu
				Node node = a.getEndNode();
				if(node instanceof Transition) { 
					// nie trzeba dodatkowo dla Place, readarc to w programie 2 łuki: jeden w tą, drugi w drugą stronę
					Place p = (Place) a.getStartNode(); //jeśli node = Transition, to StartNode musi być typu Place
					if(p.getTokensNumber() > 0) //nie spełnia def. infeasible invariant
						continue;
					
					int position = transitions.indexOf((Transition)node);
					if(raTrans.contains(position) == false)
						raTrans.add(position);
				}
			}
		}
		return raTrans;
	}
	
	/**
	 * Metoda ustala, czy wsparcie inwariantu zawiera którąkolwiek z tranzycji związanych z łukiem odczytu.
	 * @param support ArrayList[Integer] - wsparcie inwariantu
	 * @return boolean - true, jeśli wsparcie zawiera choć jedną z tranzycji
	 */
	private boolean isNonFeasible(ArrayList<Integer> support) {
    	for(int trans : readArcTransLocations) {
    		if(support.contains(trans) == true)
    			return true;
    	}
        return false;
    }
	
	/**
	 * Metoda zwraca pozycje tranzycji w inwariancie, które mają połączenie z łukami odczytu. 
	 * @param invariant ArrayList[Integer] - inwariant
	 * @return ArrayList[Integer] - tranzycji z łukami odczytu
	 */
	private ArrayList<Integer> getInfeasibleTransitions(ArrayList<Integer> invariant) {
		ArrayList<Integer> vector = new ArrayList<Integer>();
		ArrayList<Integer> support = InvariantsTools.getSupport(invariant);
		
		for(int trans : support) {
			if(readArcTransLocations.contains(trans))
				vector.add(trans);
		}
		return vector;
	}

	/**
	 * Metoda zwraca status końcowy operacji szukania inwariantów.
	 * @return boolean - status końcowy
	 */
	public boolean getStatus() {
		return success;
	}

	/**
	 * Metoda zwraca status tekstowy końca operacji szukania inwariantów.
	 * @return String - status końcowy
	 */
	public String getTextStatus() {
		return status;
	}
}
