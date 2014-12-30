package abyss.analyzer;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Klasa odpowiedzialna za analiz� w�a�ciwo�ci sieci Petriego. Tworzona r�cznie, do dzia�ania
 * wymaga istniej�cych: miejsc, tranzycji oraz �uk�w (niepuste zbiory).
 * @author students
 * @author MR - porz�dek w klasie, dodanie opis�w i dzia�aj�cego sprawdzania CN i SCN
 *
 */
public class NetPropertiesAnalyzer {
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Node> nodes = new ArrayList<Node>();

	ArrayList<Node> checked = new ArrayList<Node>();

	/**
	 * Konstruktor domy�lny obiektu klasy NetPropAnalyzer.
	 */
	public NetPropertiesAnalyzer() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
	}

	/**
	 * Metoda analizuj� w�a�ciwo�ci i zwracaj�ca je jako tablic� obiekt�w.
	 * @return ArrayList[ArrayList[Object]] - macierz obiekt�w reprezentuj�cych w�a�ciwo�ci
	 */
	public ArrayList<ArrayList<Object>> propAnalyze() {
		ArrayList<ArrayList<Object>> NetProps = new ArrayList<ArrayList<Object>>();
		
		ArrayList<Object> purProp = new ArrayList<Object>();
		purProp.add("PUR");
		purProp.add(false);
		String[] purTxt = { 
				"Pure", 
				"There are no two nodes, directly connected in both directions. This precludes "
				+ "read arcs and double arcs.",
				"No component is produced and consumed by the same reaction. Thus, enzymatic "
				+ "or enzyme-like reactions are formulated in more detail."};
		purProp.add(purTxt);
		NetProps.add(purProp);
		
		ArrayList<Object> ordProp = new ArrayList<Object>();
		ordProp.add("ORD");
		ordProp.add(false);
		String[] ordTxt = { 
				"Ordinary", 
				"All arc weights are equal to 1.",
				"Every stoichiometric coefficient of each reaction is equal to one."};
		ordProp.add(ordTxt);
		NetProps.add(ordProp);
		
		ArrayList<Object> homProp = new ArrayList<Object>();
		homProp.add("HOM");
		homProp.add(false);
		String[] homTxt = { 
				"Homogeneous", 
				"All outgoing arcs of a given place have the same multiplicity.",
				"Each consuming reaction associated with one component takes the same "
				+ "amount of molecules of this component."};
		homProp.add(homTxt);
		NetProps.add(homProp);
		
		ArrayList<Object> conProp = new ArrayList<Object>();
		conProp.add("CON");
		conProp.add(false);
		String[] conTxt = { 
				"Connected", 
				"A Petri net is connected if it holds for every two nodes a and b that "
				+ "there is an undirected path between a and b. Disconnected parts of a "
				+ "Petri net can not influence each other, so they can be usually analysed "
				+ "separately."
				+ "All components in a system are directly or indirectly connected with "
				+ "each other through a set of reactions, e.g., metabolic paths, signal flows."};
		conProp.add(conTxt);
		NetProps.add(conProp);
		
		ArrayList<Object> scProp = new ArrayList<Object>();
		scProp.add("SC");
		scProp.add(false);
		String[] scTxt = { 
				"Strongly Connected", 
				"A Petri net is strongly connected if it holds for every two nodes a and b that "
				+ "there is a directed path from a to b, vice versa. Strong connectedness "
				+ "involves connectedness and the absence of boundary nodes. It is a necessary "
				+ "condition for a Petri net to be live and bounded at the same time."
				+ "All components in a system are directly connected with each other through a set "
				+ "of reactions, e.g., metabolic paths, signal flows."};
		scProp.add(scTxt);
		NetProps.add(scProp);
		
		ArrayList<Object> nbmProp = new ArrayList<Object>();
		nbmProp.add("NBM");
		nbmProp.add(false);
		String[] nbmTxt = { 
				"Non-blocking Multiplicity", 
				"The minimum of the multiplicity of the incoming arcs for a place is not "
				+ "less than the maximum of the multiplicities of its outgoing arcs.",
				"The amount of produced and consumed molecules of a certain component "
				+ "is always equal."};
		nbmProp.add(nbmTxt);
		NetProps.add(nbmProp);
		
		ArrayList<Object> csvProp = new ArrayList<Object>();
		csvProp.add("CSV");
		csvProp.add(false);
		String[] csvTxt = { 
				"Conservative", 
				"All transitions add exactly as many tokens to their post-places as they "
				+ "subtract from their pre-places (token-preservingly firing). A conservative "
				+ "Petri net is structurally bounded.",
				"The total amount of consumed and produced molecules by a certain reaction "
				+ "is always equal."};
		csvProp.add(csvTxt);
		NetProps.add(csvProp);
		
		ArrayList<Object> scfProp = new ArrayList<Object>();
		scfProp.add("SCF");
		scfProp.add(false);
		String[] scfTxt = { 
				"Static Conflict Free", 
				"There are no two transitions sharing a pre-place. Transitions involved in a "
				+ "dynamic conflict compete for the tokens on shared places.",
				"For every reactant exist just one possible reaction or there are no two "
				+ "reactions sharing at least one reactant."};
		scfProp.add(scfTxt);
		NetProps.add(scfProp);
		
		ArrayList<Object> ft0Prop = new ArrayList<Object>();
		ft0Prop.add("FT0");
		ft0Prop.add(false);
		String[] ft0Txt = { 
				"No input transition", 
				"There exist no transitions without pre-places.",
				"Infinite source of a component."};
		ft0Prop.add(ft0Txt);
		NetProps.add(ft0Prop);
		
		ArrayList<Object> tf0Prop = new ArrayList<Object>();
		tf0Prop.add("TF0");
		tf0Prop.add(false);
		String[] tf0Txt = { 
				"No output transition", 
				"There exist no transitions without post-places.",
				"Sink of a component."};
		tf0Prop.add(tf0Txt);
		NetProps.add(tf0Prop);
		
		ArrayList<Object> fp0Prop = new ArrayList<Object>();
		fp0Prop.add("FP0");
		fp0Prop.add(false);
		String[] fp0Txt = { 
				"No input place", 
				"There exist no places without pretransitions.",
				"The component can not be produced by any reaction. Thus, such components "
				+ "are limiting."};
		fp0Prop.add(fp0Txt);
		NetProps.add(fp0Prop);
		
		ArrayList<Object> pf0Prop = new ArrayList<Object>();
		pf0Prop.add("PF0");
		pf0Prop.add(false);
		String[] pf0Txt = { 
				"No output place", 
				"There exist no places without post-transitions",
				"Components can infinitely accumulate in the system. Thus, they are not "
				+ "consumed by any reaction."};
		pf0Prop.add(pf0Txt);
		NetProps.add(pf0Prop);

		if (places.size() == 0 || transitions.size() == 0 || arcs.size() == 0) {
			return NetProps;
		}
		
		// FT0 - a transition without pre place
		boolean isFT0 = false;
		// TF0- a transitions without post place
		boolean isTF0 = false;
		// FP0 - a place without pre transitions
		boolean isFP0 = false;
		// PF0 - a place without post transitions
		boolean isPF0 = false;

		for (Transition t : transitions) {
			boolean arcIn = false;
			boolean arcOut = false;
			for (ElementLocation el : t.getElementLocations()) {
				if (!el.getInArcs().isEmpty() && arcIn == false)
					arcIn = true;
				if (!el.getOutArcs().isEmpty() && arcOut == false)
					arcOut = true;
			}
			if (arcIn == false && arcOut == true)
				isFT0 = true;
			if (arcIn == true && arcOut == false)
				isTF0 = true;
		}

		for (Place p : places) {
			boolean arcIn = false;
			boolean arcOut = false;
			for (ElementLocation el : p.getElementLocations()) {
				if (!el.getInArcs().isEmpty() && arcIn == false)
					arcIn = true;
				if (!el.getOutArcs().isEmpty() && arcOut == false)
					arcOut = true;
			}
			if (arcIn == false && arcOut == true)
				isFP0 = true;
			if (arcIn == true && arcOut == false)
				isPF0 = true;
		}
		ft0Prop.set(1, isFT0);
		tf0Prop.set(1, isTF0);
		fp0Prop.set(1, isFP0);
		pf0Prop.set(1, isPF0);
		
		// PUR - pure net
		boolean isPure = true;
		for (Transition t : transitions) {
			for (ElementLocation el : t.getElementLocations()) {
				for (Arc ar : el.getInArcs()) {
					for(ElementLocation el2 : ar.getStartNode().getElementLocations()){
						for (Arc ar2 : el2.getInArcs()) {
							if(ar2.getStartNode().getID()==t.getID())
								isPure=false;
						}
					}
				}
			}
		}
		purProp.set(1, isPure);
		
		// ORD
		boolean isOrdinary = true;
		for (Arc a : arcs)
			if (a.getWeight() != 1)
				isOrdinary = false;
		ordProp.set(1, isOrdinary);

		// HOM - homogenous net
		boolean isHomogenous = true;
		for (Place p : places) {
			int val = 0;
			for (ElementLocation el : p.getElementLocations())
				for (Arc a : el.getOutArcs())
					if (val == 0)
						val = a.getWeight();
					else if (val != a.getWeight())
						isHomogenous = false;
		}
		homProp.set(1, isHomogenous);
		
		// NBM - non blocking multiplicity net
		boolean isNonBlockingMulti = true;
		for (Place p : places) {
			int valIn = Integer.MAX_VALUE;
			int valOut = 0;
			for (ElementLocation el : p.getElementLocations()) {
				for (Arc a : el.getInArcs())
					if (a.getWeight() < valIn)
						valIn = a.getWeight();
				for (Arc a : el.getOutArcs())
					if (a.getWeight() > valOut)
						valOut = a.getWeight();

			}
			if (valOut > valIn)
				isNonBlockingMulti = false;
		}
		nbmProp.set(1, isNonBlockingMulti);

		// CSV - conservative net
		boolean isConservative = true;
		for (Transition t : transitions) {
			int arcIn = 0;
			int arcOut = 0;
			for (ElementLocation el : t.getElementLocations()) {
				for (Arc a : el.getInArcs())
					arcIn += a.getWeight();
				for (Arc a : el.getOutArcs())
					arcOut += a.getWeight();
			}
			if (arcIn != arcOut)
				isConservative = false;
		}
		csvProp.set(1, isConservative);

		// CON - connected net
		boolean isConnected = false;
		Node start = nodes.get(0);
		int tNo = transitions.size();
		int pNo = places.size();
		int nNo = nodes.size();
		if(tNo + pNo != nNo) {
			GUIManager.getDefaultGUIManager().log("Network analyzer detected critical problem within the net. "
				+ "Number of places: "+pNo+ " transitions: "+tNo+ " do not sum to the total stored "
				+ "number of nodes: "+nNo+" Please save the net in separate file, close program, open it and "
				+ "the net again. If the problem remains, please notice authors of program.", "error", true);
		}
		
		int visitedNodes = checkNetConnectivity(start, new ArrayList<Node>());
		if(visitedNodes == nNo)
			isConnected = true;
		conProp.set(1, isConnected);

		// SC - strongly connected net
		boolean isStronglyConnected = false;
		visitedNodes = checkNetStrongConnectivity(start, new ArrayList<Node>());
		if(visitedNodes == nNo)
			isStronglyConnected = true;
		scProp.set(1, isStronglyConnected);
		
		// SCF - static conflict free
		boolean isStaticConFree = true;
		for (Transition t : transitions)
			for (ElementLocation el : t.getElementLocations())
				for (Arc a1 : el.getInArcs())
					for (Transition t2 : transitions)
						if (t.getID() != t2.getID())
							for (ElementLocation el2 : t2.getElementLocations())
								for (Arc a2 : el2.getInArcs())
									if (a1.getStartNode().getID() == a2.getStartNode().getID())
										;
		isStaticConFree = false;
		scfProp.set(1, isStaticConFree);
		
		return NetProps;
	}

	@SuppressWarnings("unused")
	/**
	 * Metoda sprawdzaj�ca silne po��czenie element�w sieci.
	 * @param n1 Node - wierzcho�ek nr 1
	 * @param n2 Node - wierzcho�ek nr 2
	 * @param last Node - ostatni wierzcho�ek
	 * @param mode boolean - 
	 * @return boolean - true, je�li silnie po��czona
	 */
	private boolean checkStronglyConnectionExist(Node n1, Node n2, Node last, boolean mode) {
		if (mode == true && n1.getID() == n2.getID())
			return false;

		if (n1.getInArcs()!=null)
		{
			for (Arc a : n1.getInArcs())
				if (a.getStartNode().getID() == last.getID())
					return true;
			for (Arc a : n1.getInArcs())
				if (checkStronglyConnectionExist(a.getStartNode(), n2, last, true))
					return true;
		}else{
			return false;
		}
		
		return false;
	}

	/**
	 * Metoda sprawdza w�a�ciwo�� Connected sieci. Dla startowego w�z�a stara si� zgromadzi�
	 * identyfikatory wszystkich innych w�z��w sieci przechodz�c po �ukach IN/OUT (czyli w spos�b
	 * nieskierowany). Je�li sie� jest po��czona, liczba odwiedzonych w�z��w powinna by� r�wna
	 * ca�kowitej liczbie wierzcho�k�w sieci.
	 * @param start Node - wierzcho�ek startowy/aktualny
	 * @param reachable ArrayList<Node> - lista odwiedzonych unikalnych w�z��w
	 * @return int - liczba odwiedzonych w�z��w sieci
	 * @author MR
	 */
	private int checkNetConnectivity(Node start, ArrayList<Node> reachable) {
		if(!reachable.contains(start)) { //je�li jeszcze nie ma
			reachable.add(start);
		}
		
		if (start.getInArcs()!=null)
		{
			for (Arc a : start.getInArcs()) { //�uki wchodz�ce do aktualnie badanego wierzcho�ka
				Node nod = a.getStartNode(); //wierzcho�ek pocz�tkowy �uku
				if(!reachable.contains(nod)) { //je�li jeszcze nie ma
					reachable.add(nod);
					checkNetConnectivity(nod, reachable);
				}
			}
		}
		if (start.getOutArcs()!=null)
		{
			for (Arc a : start.getOutArcs()) { //�uki wychodz�ce z aktualnego
				Node nod = a.getEndNode(); //wierzcho�ek ko�cowy �uku
				if(!reachable.contains(nod)) {//je�li jeszcze nie ma
					reachable.add(nod);
					checkNetConnectivity(nod, reachable);
				}
			}
		}
		return reachable.size();
	}
	
	/**
	 * Jak wy�ej, tylko metoda idzie po �ukach wychodz�cych dla ka�dego wierzcho�ka.
	 * @param start Node - wierzcho�ek startowy/aktualny
	 * @param reachable ArrayList<Node> - lista odwiedzonych unikalnych w�z��w
	 * @return int - liczba odwiedzonych w�z��w sieci
	 * @author MR
	 */
	private int checkNetStrongConnectivity(Node start, ArrayList<Node> reachable) {
		if(!reachable.contains(start)) { //je�li jeszcze nie ma
			reachable.add(start);
		}
		if (start.getOutArcs()!=null)
		{
			for (Arc a : start.getOutArcs()) { //�uki wychodz�ce z aktualnego
				Node nod = a.getEndNode(); //wierzcho�ek ko�cowy �uku
				if(!reachable.contains(nod)) {//je�li jeszcze nie ma
					reachable.add(nod);
					checkNetConnectivity(nod, reachable);
				}
			}
		}
		return reachable.size();
	}
}
