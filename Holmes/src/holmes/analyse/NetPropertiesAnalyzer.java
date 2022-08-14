package holmes.analyse;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;

/**
 * Klasa odpowiedzialna za analizę właściwości sieci Petriego. Tworzona ręcznie, do działania
 * wymaga istniejących: miejsc, tranzycji oraz łuków (niepuste zbiory).
 */
public class NetPropertiesAnalyzer {
	private ArrayList<Arc> arcs;
	private ArrayList<Place> places;
	private ArrayList<Transition> transitions;
	private ArrayList<Node> nodes;
	private ArrayList<MetaNode> metaNodes;

	ArrayList<Node> checked = new ArrayList<Node>();

	/**
	 * Konstruktor domyślny obiektu klasy NetPropAnalyzer.
	 */
	public NetPropertiesAnalyzer() {
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		places = pn.getPlaces();
		transitions = pn.getTransitions();
		arcs = pn.getArcs();
		nodes = pn.getNodes();
		metaNodes = pn.getMetaNodes();
	}

	/**
	 * Metoda analizująca właściwości i zwracająca je jako tablicę obiektów.
	 * @return ArrayList[ArrayList[Object]] - macierz obiektów reprezentujących właściwości
	 */
	public ArrayList<ArrayList<Object>> propAnalyze() {
		ArrayList<ArrayList<Object>> NetProps = new ArrayList<ArrayList<Object>>();
		
		ArrayList<Object> purProp = new ArrayList<Object>();
		purProp.add("PUR");
		purProp.add(false);
		String[] purTxt = { 
				"Pure", 
				"There are no two nodes, directly connected in both directions. This precludes "
				+ "\nread arcs and double arcs.",
				"No component is produced and consumed by the same reaction."};
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
				+ "\namount of molecules of this component."};
		homProp.add(homTxt);
		NetProps.add(homProp);
		
		ArrayList<Object> conProp = new ArrayList<Object>();
		conProp.add("CON");
		conProp.add(false);
		String[] conTxt = { 
				"Connected", 
				"A Petri net is connected if it holds for every two nodes a and b that "
				+ "\nthere is an undirected path between a and b. Disconnected parts of a "
				+ "\nPetri net can not influence each other, so they can be usually analysed "
				+ "\nseparately.",
				"All components in a system are directly or indirectly connected with "
				+ "\neach other through a set of reactions, e.g., metabolic paths, signal flows."};
		conProp.add(conTxt);
		NetProps.add(conProp);
		
		ArrayList<Object> scProp = new ArrayList<Object>();
		scProp.add("SC");
		scProp.add(false);
		String[] scTxt = { 
				"Strongly Connected", 
				"A Petri net is strongly connected if it holds for every two nodes a and b that "
				+ "\nthere is a directed path from a to b, vice versa. Strong connectedness "
				+ "\ninvolves connectedness and the absence of boundary nodes. It is a necessary "
				+ "\ncondition for a Petri net to be live and bounded at the same time.",
				"All components in a system are directly connected with each other through a set "
				+ "\nof reactions, e.g., metabolic paths, signal flows."};
		scProp.add(scTxt);
		NetProps.add(scProp);
		
		ArrayList<Object> nbmProp = new ArrayList<Object>();
		nbmProp.add("NBM");
		nbmProp.add(false);
		String[] nbmTxt = { 
				"Non-blocking Multiplicity", 
				"The minimum of the multiplicity of the incoming arcs for a place is not "
				+ "\nless than the maximum of the multiplicities of its outgoing arcs.",
				"The amount of produced and consumed molecules of a certain component "
				+ "\nis always equal."};
		nbmProp.add(nbmTxt);
		NetProps.add(nbmProp);
		
		ArrayList<Object> csvProp = new ArrayList<Object>();
		csvProp.add("CSV");
		csvProp.add(false);
		String[] csvTxt = { 
				"Conservative", 
				"All transitions add exactly as many tokens to their post-places as they "
				+ "\nsubtract from their pre-places (token-preservingly firing). A conservative "
				+ "\nPetri net is structurally bounded.",
				"The total amount of consumed and produced molecules by a certain reaction "
				+ "\nis always equal."};
		csvProp.add(csvTxt);
		NetProps.add(csvProp);
		
		ArrayList<Object> scfProp = new ArrayList<Object>();
		scfProp.add("SCF");
		scfProp.add(false);
		String[] scfTxt = { 
				"Static Conflict Free", 
				"There are no two transitions sharing a pre-place. Transitions involved in a "
				+ "\ndynamic conflict compete for the tokens on shared places.",
				"For every reactant exist just one possible reaction or there are no two "
				+ "\nreactions sharing at least one reactant."};
		scfProp.add(scfTxt);
		NetProps.add(scfProp);
		
		ArrayList<Object> ft0Prop = new ArrayList<Object>();
		ft0Prop.add("Ft0");
		ft0Prop.add(false);
		String[] ft0Txt = { 
				"Input transitions", 
				"There are transitions without a pre-place: Ft = {}",
				"Infinite source of a component."};
		ft0Prop.add(ft0Txt);
		NetProps.add(ft0Prop);
		
		ArrayList<Object> tf0Prop = new ArrayList<Object>();
		tf0Prop.add("tF0");
		tf0Prop.add(false);
		String[] tf0Txt = { 
				"Output transitions", 
				"There are transitions without a post-place: tF = {}",
				"Sink of a component."};
		tf0Prop.add(tf0Txt);
		NetProps.add(tf0Prop);
		
		ArrayList<Object> fp0Prop = new ArrayList<Object>();
		fp0Prop.add("Fp0");
		fp0Prop.add(false);
		String[] fp0Txt = { 
				"Input places", 
				"There are places without pre-transitions: Fp = {}",
				"Every component can be consumed by a reaction."};
		fp0Prop.add(fp0Txt);
		NetProps.add(fp0Prop);
		
		ArrayList<Object> pf0Prop = new ArrayList<Object>();
		pf0Prop.add("pF0");
		pf0Prop.add(false);
		String[] pf0Txt = { 
				"Output places", 
				"There are places without post-transitions: pF = {}",
				"Components can infinitely accumulate in the system."};
		pf0Prop.add(pf0Txt);
		NetProps.add(pf0Prop);

		if (places.size() == 0 || transitions.size() == 0 || arcs.size() == 0) {
			return NetProps;
		}
		
		
		boolean isFT0 = false; // FT0 - a transition without pre place: input transitions
		boolean isTF0 = false; // TF0- a transitions without post place: output transitions
		boolean isFP0 = false; // FP0 - a place without pre transitions
		boolean isPF0 = false; // PF0 - a place without post transitions

		for (Transition t : transitions) {
			boolean arcIn = false;
			boolean arcOut = false;
			for (ElementLocation el : t.getElementLocations()) {
				if (!el.getInArcs().isEmpty() && !arcIn)
					arcIn = true;
				if (!el.getOutArcs().isEmpty() && !arcOut)
					arcOut = true;
			}
			if (!arcIn && arcOut)
				isFT0 = true;
			if (arcIn && !arcOut)
				isTF0 = true;
		}

		for (Place p : places) {
			boolean arcIn = false;
			boolean arcOut = false;
			for (ElementLocation el : p.getElementLocations()) {
				if (!el.getInArcs().isEmpty() && !arcIn)
					arcIn = true;
				if (!el.getOutArcs().isEmpty() && !arcOut)
					arcOut = true;
			}
			if (!arcIn && arcOut)
				isFP0 = true;
			if (arcIn && !arcOut)
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
			if (a.getWeight() != 1) {
				isOrdinary = false;
				break;
			}

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
			if (valOut > valIn) {
				isNonBlockingMulti = false;
				break;
			}
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
			if (arcIn != arcOut) {
				isConservative = false;
				break;
			}
		}
		csvProp.set(1, isConservative);

		// CON - connected net
		boolean isConnected = false;
		Node start = nodes.get(0);
		int tNo = transitions.size();
		int pNo = places.size();
		int mnNo = metaNodes.size();
		int numberOfNodes = nodes.size();
		if(tNo + pNo != numberOfNodes - mnNo) {
			GUIManager.getDefaultGUIManager().log("Network analyzer detected a problem within the net. "
				+ "Number of places: "+pNo+ " and number transitions: "+tNo+ " do not sum to the total stored "
				+ "number of nodes: "+numberOfNodes+" minus number of meta-nodes: "+mnNo+".", "warning", true);
		}
		
		int visitedNodes = checkNetConnectivity(start, new ArrayList<Node>());
		if(visitedNodes == numberOfNodes)
			isConnected = true;
		conProp.set(1, isConnected);

		// SC - strongly connected net
		boolean isStronglyConnected = false;
		if(fastStrongConnectivityTest()) {
			visitedNodes = checkNetStrongConnectivity(start, new ArrayList<Node>());
			if(visitedNodes == numberOfNodes)
				isStronglyConnected = true;
		}
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
									if (a1.getStartNode().getID() == a2.getStartNode().getID()) {
										//nothing
									}
		isStaticConFree = false;
		scfProp.set(1, isStaticConFree);
		
		return NetProps;
	}

	@SuppressWarnings("unused")
	/*
	 * Metoda sprawdzająca silne połączenie elementów sieci.
	 * @param n1 Node - wierzchołek nr 1
	 * @param n2 Node - wierzchołek nr 2
	 * @param last Node - ostatni wierzchołek
	 * @param mode boolean - 
	 * @return boolean - true, jeśli silnie połączona
	 */
	private boolean checkStronglyConnectionExist(Node n1, Node n2, Node last, boolean mode) {
		if (mode && n1.getID() == n2.getID())
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
	 * Metoda sprawdza właściwość Connected sieci. Dla startowego węzła stara się zgromadzić
	 * identyfikatory wszystkich innych węzłów sieci przechodząc po łukach IN/OUT (czyli w sposób
	 * nieskierowany). Jeśli sieć jest połączona, liczba odwiedzonych węzłów powinna być równa
	 * całkowitej liczbie wierzchołków sieci.
	 * @param start Node - wierzchołek startowy/aktualny
	 * @param reachable ArrayList<Node> - lista odwiedzonych unikalnych węzłów
	 * @return int - liczba odwiedzonych węzłów sieci
	 */
	private int checkNetConnectivity(Node start, ArrayList<Node> reachable) {
		if(!reachable.contains(start)) { //jeśli jeszcze nie ma
			reachable.add(start);
		}
		
		if (start.getInArcs()!=null)
		{
			for (Arc a : start.getInArcs()) { //łuki wchodzące do aktualnie badanego wierzchołka
				Node nod = a.getStartNode(); //wierzchołek początkowy łuku
				if(!reachable.contains(nod)) { //jeśli jeszcze nie ma
					reachable.add(nod);
					checkNetConnectivity(nod, reachable);
				}
			}
		}
		if (start.getOutArcs()!=null)
		{
			for (Arc a : start.getOutArcs()) { //łuki wychodzące z aktualnego
				Node nod = a.getEndNode(); //wierzchołek końcowy łuku
				if(!reachable.contains(nod)) {//jeśli jeszcze nie ma
					reachable.add(nod);
					checkNetConnectivity(nod, reachable);
				}
			}
		}
		return reachable.size();
	}
	
	/**
	 * Jak wyżej, tylko metoda idzie po łukach wychodzących dla każdego wierzchołka.
	 * @param start Node - wierzchołek startowy/aktualny
	 * @param reachable ArrayList<Node> - lista odwiedzonych unikalnych węzłów
	 * @return int - liczba odwiedzonych węzłów sieci
	 */
	private int checkNetStrongConnectivity(Node start, ArrayList<Node> reachable) {
		if(!reachable.contains(start)) { //jeśli jeszcze nie ma
			reachable.add(start);
		}
		if (start.getOutArcs()!=null)
		{
			for (Arc a : start.getOutArcs()) { //łuki wychodzące z aktualnego
				Node nod = a.getEndNode(); //wierzchołek końcowy łuku
				if(!reachable.contains(nod)) {//jeśli jeszcze nie ma
					reachable.add(nod);
					checkNetStrongConnectivity(nod, reachable);
				}
			}
		}
		return reachable.size();
	}
	
	/**
	 * Szybkie sprawdzanie BRAKU właściwości Strong Connection - jeśli znajdzie się choć jeden wierzchołek
	 * którego zbiór łuków IN lub łuków OUT jest pusty - sieć nie może być SC.
	 * @return boolean - false, jeśli sieć na pewno nie jest silnie spójna, nie oznacza to, że JEST silnie spójna!
	 */
	private boolean fastStrongConnectivityTest() {
		for(Node n : nodes) {
			if(n.getInArcs().size() == 0 || n.getOutArcs().size() == 0)
				return false;
		}
		return true;
	}
}
