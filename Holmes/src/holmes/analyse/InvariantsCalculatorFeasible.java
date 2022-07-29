package holmes.analyse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.varia.Check;
import holmes.windows.HolmesInvariantsGenerator;

/**
 * Klasa odpowiedzialna za szukanie inwariantów wykonalnych (feasible invariants). Literatura: <br>
 * "Application of Petri net based analysis techniques to signal transduction pathways" <br>
 * Andrea Sackmann, Monika Heiner, Ina Koch; BMC Bioinformatics, 2006, 7:482
 * 
 * @author MR
 *
 */
public class InvariantsCalculatorFeasible {
	private HolmesInvariantsGenerator masterWindow;
	private String status = "";
	private boolean success = false;
	private ArrayList<ArrayList<Integer>> invariants;
	private ArrayList<ArrayList<Integer>> feasibleInv;
	private ArrayList<ArrayList<Integer>> non_feasibleInv;
	private ArrayList<ArrayList<Integer>> f_invariantsCreated;
	private ArrayList<Transition> transitions;
	private ArrayList<Integer> readArcTransLocations;
	private boolean tInv;
	
	private boolean allowSelfPropelledInvariants = true;
	private int selfPropInvariants = 0;
	
	/**
	 * Konstruktor klasy InvariantsCalculatorFeasible.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param isTInv boolean - true jeśli to T-inwarianty, false jeśli P-inw.
	 */
	public InvariantsCalculatorFeasible(ArrayList<ArrayList<Integer>> invariants, boolean isTInv) {
		masterWindow = GUIManager.getDefaultGUIManager().accessInvariantsWindow();
		this.invariants = invariants;
		this.tInv = isTInv;
		f_invariantsCreated = new ArrayList<ArrayList<Integer>>();
	}
	
	/**
	 * Metoda startowa szukania zbioru inwariantów wykonalnych.
	 * @return (<b>ArrayList[ArrayList[Integer]]</b>) zbiór feasible invariants.
	 */
	public ArrayList<ArrayList<Integer>> getMinFeasible(int mode) {
		ArrayList<Integer> arcClassCount = Check.getArcClassCount();
		if(arcClassCount.get(1) == 0 && arcClassCount.get(5) == 0 ) { //brak łuków odczytu
			status = "No read arcs. All invariants are feasible.";
			success = false;
			return invariants;
		}
		
		if(tInv) {
			transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			//places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
			readArcTransLocations = getReadArcTransitions();

			allowSelfPropelledInvariants = GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisFeasibleSelfPropAccepted").equals("1");
			
			if(mode == 0)
				searchFTInvSimple();
			else
				searchFTInvAdvanced();
			
			status = "Set computed. Returning.";
			success = true;
			
			logInternal("Created non-minimal feasible invariants: "+f_invariantsCreated.size()+"\n", false);
			logInternal("Self-propelled readarc/double arcs invariants left unchanged: "+selfPropInvariants+"\n", false);
			
			return makeFeasibleSet();
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
	private void searchFTInvSimple() {
		partitionInvariants();

		for (ArrayList<Integer> integers : non_feasibleInv) {
			ArrayList<ArrayList<Integer>> invToCombine = new ArrayList<ArrayList<Integer>>(); //lista znalezionych
			ArrayList<Integer> nonFInvSupport = InvariantsTools.getSupport(integers);
			ArrayList<Integer> trans_RA = getInfeasibleTransitions(integers);

			//zidentyfikuj ile miejsc jest związanych z każdą tranzycją (może być więcej niż 1!)
			ArrayList<Place> places_RA = getRA_Places(trans_RA);
			//dla każdego z powyższych miejsc należy zapewnić tokeny poprzez minimalne inwarianty

			for (Place pl : places_RA) {
				ArrayList<Integer> connectedTransitions = getConnectedTransitionsSet(pl);

				if (allowSelfPropelledInvariants) {
					if (intersection(nonFInvSupport, connectedTransitions).size() > 0) {
						continue;
					}
				}

				ArrayList<Integer> minimalFeasibleInvariant = findMinFeasibleSimple(connectedTransitions);

				if (!invToCombine.contains(minimalFeasibleInvariant))
					invToCombine.add(minimalFeasibleInvariant);
			}
			invToCombine.add(integers);

			if (invToCombine.size() == 1) {
				feasibleInv.add(integers);
				selfPropInvariants++;
			} else {
				ArrayList<Integer> newFeasible = createFeasibleInvariant(invToCombine);
				f_invariantsCreated.add(newFeasible);
			}
		}
	}
	
	/**
	 * Tworzenie wykonalnych inwariantów - wersja rozszerzona: mniejsze nie-minimalne, ale dłuższe obliczenia
	 */
	private void searchFTInvAdvanced() {
		partitionInvariants();
		f_invariantsCreated = new ArrayList<ArrayList<Integer>>();

		for (ArrayList<Integer> integers : non_feasibleInv) {
			ArrayList<ArrayList<Integer>> invToCombine = new ArrayList<ArrayList<Integer>>(); //lista znalezionych
			ArrayList<Integer> trans_RA = getInfeasibleTransitions(integers);

			//zidentyfikuj ile miejsc jest związanych z każdą tranzycją (może być więcej niż 1!)
			ArrayList<Place> places_RA = getRA_Places(trans_RA);

			//dla każdego z powyższych miejsc należy zapewnić tokeny poprzez minimalne inwarianty
			ArrayList<ArrayList<Integer>> connectedTransitionsSet = new ArrayList<ArrayList<Integer>>();
			for (Place pl : places_RA) {
				ArrayList<Integer> connectedTransitions = getConnectedTransitionsSet(pl);
				connectedTransitionsSet.add(connectedTransitions);
			}

			//sam inwariant może zawiera jakieś tranzycje które zasilają inne przez łuk odczytu?

			int oldSize = connectedTransitionsSet.size();
			ArrayList<ArrayList<Integer>> connectedTransitionsSetNew = checkSelfPropelledInv(integers, connectedTransitionsSet);
			int newSize = connectedTransitionsSetNew.size();

			if (allowSelfPropelledInvariants) {
				connectedTransitionsSet = connectedTransitionsSetNew;
			}

			//teraz szukamy najmniejszego zbioru inwariantów pokrywającego min 1 elementent każdego zbioru w connectedTransitionsSet			
			int setsNumber = connectedTransitionsSet.size();
			int found = 0;
			int upperSearchBound = setsNumber;

			while (found != setsNumber) {
				ArrayList<Integer> foundVectors = searchCandidate(connectedTransitionsSet, upperSearchBound);

				if (foundVectors == null) {
					upperSearchBound--;
				} else {
					found += upperSearchBound;

					if (upperSearchBound > connectedTransitionsSet.size())
						upperSearchBound = connectedTransitionsSet.size();
					//usuń znalezione zbiory z connectedTransitionsSet i zacznij kolejną iterację
					if (!invToCombine.contains(foundVectors)) {
						invToCombine.add(foundVectors);
					}
				}

				if (upperSearchBound == 0 || connectedTransitionsSet.size() == 0) {
					break;
				}
			}
			invToCombine.add(integers);

			if (invToCombine.size() == 1) {
				feasibleInv.add(integers);
				selfPropInvariants++;
			} else {
				ArrayList<Integer> newFeasible = createFeasibleInvariant(invToCombine);
				int feasibleNumber = feasibleInv.size();
				if (oldSize != newSize) {
					logInternal("Non-feasible invariant (pos: " + (feasibleNumber + f_invariantsCreated.size()) +
							") already contains some required transitions: " + (oldSize - newSize) + " out of " + oldSize + ".\n", false);
				}

				if (!InvariantsTools.checkCanonitySingle(newFeasible)) {
					logInternal("Created feasible invariant number " + (feasibleNumber + f_invariantsCreated.size()) + " is not canonical.\n", false);
				}

				f_invariantsCreated.add(newFeasible);
			}
		}
	}

	/**
	 * Metoda sprawdzająca, czy potrzebne tranzycje ze zbiorów 'zasilających' odpowiednie miejsca z łukami
	 * odczytu są już zawarte w danym non-feasible (?) inwariancie.
	 * @param nonFInvariant ArrayList[Integer] - inwariant 
	 * @param connectedTransitionsSet ArrayList[ArrayList[Integer]] - zbiór potrzebnych tranzycji
	 * @return ArrayList[ArrayList[Integer]] - nowy zbiór connectedTransitionsSet
	 */
	private ArrayList<ArrayList<Integer>> checkSelfPropelledInv(ArrayList<Integer> nonFInvariant, ArrayList<ArrayList<Integer>> connectedTransitionsSet) {
		ArrayList<ArrayList<Integer>> newConnTransSet = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> support = InvariantsTools.getSupport(nonFInvariant);
		
		for(ArrayList<Integer> set : connectedTransitionsSet) {
			
			if(intersection(support, set).size() > 0) {
				//jeśli true, wtedy nie dodajemy, i tego zbioru dalej nie będziemy szukać...
				//newConnTransSet.add(set);
			} else {
				newConnTransSet.add(set);
			}
		}
		
		return newConnTransSet;
	}
	
	/**
	 * Zadaniem metody jest znalezionie takiego inwariantu, który pokrywa #upperSearchBound tranzycji z każdego zbioru w 
	 * connectedTransitionsSet. Jesli jest ich więcej - wybranie 'minimalnego'.
	 * 
	 * @param connectedTransitionsSet ArrayList[ArrayList[Integer]] - zbiór tranzycji związanych (z łukami odczytu) - ich ID
	 * @param upperSearchBound int - liczba zbiorów do pokrycia
	 * @return ArrayList[Integer] - pierwszy wektor to informacja
	 */
	private ArrayList<Integer> searchCandidate(ArrayList<ArrayList<Integer>> connectedTransitionsSet, int upperSearchBound) {
		int connSetsSize = connectedTransitionsSet.size();
		int currentFound;
		int currentNotFound;
		int fCandidatesFound = 0;
		
		ArrayList<ArrayList<Integer>> results = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> forWhichConnSets = new ArrayList<ArrayList<Integer>>();
		
		for(ArrayList<Integer> f_invariant : feasibleInv) {
			currentFound = 0;
			currentNotFound = 0;
			ArrayList<Integer> f_inv_support = InvariantsTools.getSupport(f_invariant); //potrzebne ID tranzycji inwariantu
			ArrayList<Integer> intersectionSets = new ArrayList<Integer>();
			for(int cs=0; cs<connSetsSize; cs++) {
				ArrayList<Integer> connectedSet = connectedTransitionsSet.get(cs);
			
				if(!intersection(f_inv_support, connectedSet).isEmpty()) {
					currentFound++;
					intersectionSets.add(cs);
					
					if(currentFound == upperSearchBound) { //jeśli obejmuje tyle zbiorów ile trzeba
						results.add(f_invariant); //dodaj kandydata
						forWhichConnSets.add(intersectionSets); //który zbiór obejmuje
						fCandidatesFound++;
						break; //success
					}
				} else {
					currentNotFound++;
					if(connSetsSize - currentNotFound < upperSearchBound)
						break; //failure
				}
			}
			
		}
		
		if(fCandidatesFound == 0) {
			return null;
		} else if(fCandidatesFound == 1) {

			ArrayList<Integer> sets = forWhichConnSets.get(0);
			ArrayList<ArrayList<Integer>> setsToRemove = new ArrayList<ArrayList<Integer>>();
			for (int setIDToRemove : sets) {
				ArrayList<Integer> setToRemove = connectedTransitionsSet.get(setIDToRemove);
				setsToRemove.add(setToRemove);

			}
			for(ArrayList<Integer> set : setsToRemove)
				connectedTransitionsSet.remove(set);
			
			 //usuń objęty inwariantem zbiór tranzycji wejściowych do P z read-arc
			return results.get(0);
		} else {
			//procedura znajdowania najmniejszego kandydata:
			
			int minSupportCardinality = results.get(0).size();
			int minSupportSum = 9999999;
			int indexFound = -1;
			int foundNumber = results.size();
			for(int i=0; i<foundNumber; i++) {
				ArrayList<Integer> cand_invariant = results.get(i);
				int minSC = 0; //cardinality
				int minSS = 0; //sum
				boolean dontBother = false;	
				for(int el : cand_invariant) { //przebadaj inwariant jako kandydata do minimalnego
					if(el != 0) {
						minSC++;
						minSS += el;
						if(minSC > minSupportCardinality) {
							dontBother = true; //odpada, za duży niż do tej pory wybrany, nie marnuj więcej czasu
							break;
						}
					}
				}
				
				if(!dontBother) { //zbadaj na minimalność (w kontekście procedury feasibility-search)
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
			
			ArrayList<Integer> sets = forWhichConnSets.get(indexFound);
			ArrayList<ArrayList<Integer>> setsToRemove = new ArrayList<ArrayList<Integer>>();
			for (int setIDToRemove : sets) {
				ArrayList<Integer> setToRemove = connectedTransitionsSet.get(setIDToRemove);
				setsToRemove.add(setToRemove);
			}
			for(ArrayList<Integer> set : setsToRemove)
				connectedTransitionsSet.remove(set);
			
			//ArrayList<Integer> setToRemove = connectedTransitionsSet.get(forWhichConnSet.get(indexFound));
			//connectedTransitionsSet.remove(setToRemove); //usuń objęty inwariantem zbiór tranzycji wejściowych do P z read-arc
			return results.get(indexFound);
		}
		
		//return null;
	}
	
	/**
	 * Metoda zwraca wspólną część dwóch list.
	 * @param list1 ArrayList[Integer] - I lista
	 * @param list2 ArrayList[Integer] - II lista
	 * @return ArrayList[Integer] - część wspólna
	 */
	public ArrayList<Integer> intersection(ArrayList<Integer> list1, ArrayList<Integer> list2) {   
	    ArrayList<Integer> result = new ArrayList<Integer>(list1);
	    result.retainAll(list2);
	    return result;
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
				ArrayList<Integer> f_invariant = feasibleInv.get(i);
				
				if(f_invariant.get(transLoc) == 0)
					continue; //jeśli nie, to znaczy, że mamy kandydata
				
				int minSC = 0; //cardinality
				int minSS = 0; //sum
				boolean dontBother = false;
				
				for(int el : f_invariant) { //przebadaj inwariant jako kandydata do minimalnego
					if(el != 0) {
						minSC++;
						minSS += el;
						if(minSC > minSupportCardinality) {
							dontBother = true; //odpada, za duży, nie marnuj więcej czasu
							break;
						}
					}
				}
				
				if(!dontBother) { //zbadaj na minimalność (w kontekście procedury feasibility-search)
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
	private ArrayList<ArrayList<Integer>> makeFeasibleSet() {
		ArrayList<ArrayList<Integer>> resultSet = new ArrayList<ArrayList<Integer>>(feasibleInv);
		resultSet.addAll(f_invariantsCreated);
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
	 * Metoda zwraca zbiór lokalizacji tranzycji związanych łukami wejściowymi z danym miejscem które posiada łuk odczytu.
	 * @param place Place - miejsce
	 * @return ArrayList[Integer] - lista lokalizacji tranzycji
	 */
	private ArrayList<Integer> getConnectedTransitionsSet(Place place) {
		ArrayList<Integer> connectedTransitions = new ArrayList<Integer>();

		for(ElementLocation el : place.getElementLocations()) {
			for(Arc a : el.getInArcs()) { //tylko łuki wejściowe
				if(a.getArcType() == TypeOfArc.READARC || a.getArcType() == TypeOfArc.INHIBITOR) 
					continue; //czyli poniższe działa tylko dla: NORMAL, XTPN, RESET i EQUAL

				Transition trans = (Transition) a.getStartNode();
				int pos = transitions.indexOf(trans);
				if(!connectedTransitions.contains(pos)) {
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
					if(a.getArcType() != TypeOfArc.READARC) { //if łuk odczytu idź dalej
						if(a.getArcType() == TypeOfArc.NORMAL) { //jeśli normalny łuk to
							if(!InvariantsTools.isDoubleArc(a)) //sprawdź, czy to przypadkiem nie łuk podwójny
								continue; //jeśli nie, spadamy stąd
						} else {	
							continue;
						}
					}
					
					Place p = (Place) a.getEndNode();
					if(!resultPlaces.contains(p))
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
		for (ArrayList<Integer> invariant : invariants) {
			ArrayList<Integer> support = InvariantsTools.getSupport(invariant);

			if (isNonFeasible(support))
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
			if(a.getArcType() == TypeOfArc.READARC) { //tylko łuki odczytu
				Node node = a.getEndNode();
				if(node instanceof Transition) { 
					// nie trzeba dodatkowo dla Place, readarc to w programie 2 łuki: jeden w tą, drugi w drugą stronę
					Place p = (Place) a.getStartNode(); //jeśli node = Transition, to StartNode musi być typu Place
					if(p.getTokensNumber() > 0) //nie spełnia def. infeasible invariant
						continue;
					
					int position = transitions.indexOf((Transition)node);
					if(!raTrans.contains(position))
						raTrans.add(position);
				}
			} else if(a.getArcType() == TypeOfArc.NORMAL){
				if(InvariantsTools.isDoubleArc(a)) {
					Node n = a.getEndNode();
					if(n instanceof Place) {
						if(((Place) n).getTokensNumber() > 0)
							continue;
						else {
							int position = transitions.indexOf((Transition)a.getStartNode());
							if(!raTrans.contains(position))
								raTrans.add(position);
						}
					} else { //a.getEndNode(); = tranzycja
						n = a.getStartNode();
						if(((Place) n).getTokensNumber() > 0)
							continue;
						else {
							int position = transitions.indexOf((Transition)a.getEndNode());
							if(!raTrans.contains(position))
								raTrans.add(position);
						}
					}
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
    		if(support.contains(trans))
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
	
	/**
	 * Metoda wysyłająca komunikaty do podokna logów generatora.
	 * @param msg String - tekst do logów
	 * @param date boolean - true, jeśli ma być podany czas komunikatu
	 */
	private void logInternal(String msg, boolean date) {
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
		if(masterWindow != null) {
			if(!date) {
				masterWindow.accessLogFieldTinv().append(msg);
			} else {
				masterWindow.accessLogFieldTinv().append("["+timeStamp+"] "+msg);
			}
		}
	}
}
