package holmes.analyse;

import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
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
	private static LanguageManager lang = GUIManager.getLanguageManager();
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
		purProp.add("PUR"); //Pure
		purProp.add(false);
		String[] purTxt = { 
				lang.getText("NPA_entry001"), 
				lang.getText("NPA_entry001d1"),
				lang.getText("NPA_entry001d2")};
		purProp.add(purTxt);
		NetProps.add(purProp);
		
		ArrayList<Object> ordProp = new ArrayList<Object>();
		ordProp.add("ORD"); //Ordinary
		ordProp.add(false);
		String[] ordTxt = { 
				lang.getText("NPA_entry002"), 
				lang.getText("NPA_entry002d1"),
				lang.getText("NPA_entry002d2")};
		ordProp.add(ordTxt);
		NetProps.add(ordProp);
		
		ArrayList<Object> homProp = new ArrayList<Object>();
		homProp.add("HOM"); //Homogeneous
		homProp.add(false);
		String[] homTxt = { 
				lang.getText("NPA_entry003"), 
				lang.getText("NPA_entry003d1"),
				lang.getText("NPA_entry003d2")};
		homProp.add(homTxt);
		NetProps.add(homProp);
		
		ArrayList<Object> conProp = new ArrayList<Object>();
		conProp.add("CON"); //Connected
		conProp.add(false);
		String[] conTxt = { 
				lang.getText("NPA_entry004"), 
				lang.getText("NPA_entry004d1")+lang.getText("NPA_entry004d2"),
				lang.getText("NPA_entry004d2")};
		conProp.add(conTxt);
		NetProps.add(conProp);
		
		ArrayList<Object> scProp = new ArrayList<Object>();
		scProp.add("SC"); //Strongly Connected
		scProp.add(false);
		String[] scTxt = { 
				lang.getText("NPA_entry005"),
				lang.getText("NPA_entry005d1")+lang.getText("NPA_entry005d2"),
				lang.getText("NPA_entry005d2")};
		scProp.add(scTxt);
		NetProps.add(scProp);
		
		ArrayList<Object> nbmProp = new ArrayList<Object>();
		nbmProp.add("NBM"); //Non-blocking Multiplicity
		nbmProp.add(false);
		String[] nbmTxt = {
				lang.getText("NPA_entry006"),
				lang.getText("NPA_entry006d1"),
				lang.getText("NPA_entry006d2")};
		nbmProp.add(nbmTxt);
		NetProps.add(nbmProp);
		
		ArrayList<Object> csvProp = new ArrayList<Object>();
		csvProp.add("CSV"); //Conservative
		csvProp.add(false);
		String[] csvTxt = {
				lang.getText("NPA_entry007"),
				lang.getText("NPA_entry007d1"),
				lang.getText("NPA_entry007d2")};
		csvProp.add(csvTxt);
		NetProps.add(csvProp);
		
		ArrayList<Object> scfProp = new ArrayList<Object>();
		scfProp.add("SCF"); //Static Conflict Free
		scfProp.add(false);
		String[] scfTxt = {
				lang.getText("NPA_entry008"),
				lang.getText("NPA_entry008d1"),
				lang.getText("NPA_entry008d2")};
		scfProp.add(scfTxt);
		NetProps.add(scfProp);
		
		ArrayList<Object> ft0Prop = new ArrayList<Object>();
		ft0Prop.add("Ft0"); //Input transitions
		ft0Prop.add(false);
		String[] ft0Txt = {
				lang.getText("NPA_entry009"),
				lang.getText("NPA_entry009d1"),
				lang.getText("NPA_entry009d2")};
		ft0Prop.add(ft0Txt);
		NetProps.add(ft0Prop);
		
		ArrayList<Object> tf0Prop = new ArrayList<Object>();
		tf0Prop.add("tF0");
		tf0Prop.add(false);
		String[] tf0Txt = {
				lang.getText("NPA_entry010"),
				lang.getText("NPA_entry010d1"),
				lang.getText("NPA_entry010d2")};
		tf0Prop.add(tf0Txt);
		NetProps.add(tf0Prop);
		
		ArrayList<Object> fp0Prop = new ArrayList<Object>();
		fp0Prop.add("Fp0");
		fp0Prop.add(false);
		String[] fp0Txt = {
				lang.getText("NPA_entry011"),
				lang.getText("NPA_entry011d1"),
				lang.getText("NPA_entry011d2")};
		fp0Prop.add(fp0Txt);
		NetProps.add(fp0Prop);
		
		ArrayList<Object> pf0Prop = new ArrayList<Object>();
		pf0Prop.add("pF0");
		pf0Prop.add(false);
		String[] pf0Txt = {
				lang.getText("NPA_entry012"),
				lang.getText("NPA_entry012d1"),
				lang.getText("NPA_entry012d2")};
		pf0Prop.add(pf0Txt);
		NetProps.add(pf0Prop);

		if (places.isEmpty() || transitions.isEmpty() || arcs.isEmpty()) {
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
			GUIManager.getDefaultGUIManager().log(lang.getText("NPA_entry013")+ " "+pNo+ " "
					+lang.getText("NPA_entry013a")+" "+tNo+ " "+lang.getText("NPA_entry013b")+" "
					+numberOfNodes+" "+lang.getText("NPA_entry013c")+" "+mnNo+".", "warning", true);
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
	 * @param mode boolean - diabli wiedzą co :)
	 * @return boolean - true, jeśli silnie połączona
	 */
	private boolean checkStronglyConnectionExist(Node n1, Node n2, Node last, boolean mode) {
		if (mode && n1.getID() == n2.getID())
			return false;

		if (n1.getInputArcs()!=null)
		{
			for (Arc a : n1.getInputArcs())
				if (a.getStartNode().getID() == last.getID())
					return true;
			for (Arc a : n1.getInputArcs())
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
		
		if (start.getInputArcs()!=null)
		{
			for (Arc a : start.getInputArcs()) { //łuki wchodzące do aktualnie badanego wierzchołka
				Node nod = a.getStartNode(); //wierzchołek początkowy łuku
				if(!reachable.contains(nod)) { //jeśli jeszcze nie ma
					reachable.add(nod);
					checkNetConnectivity(nod, reachable);
				}
			}
		}
		if (start.getOutputArcs()!=null)
		{
			for (Arc a : start.getOutputArcs()) { //łuki wychodzące z aktualnego
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
		if (start.getOutputArcs()!=null)
		{
			for (Arc a : start.getOutputArcs()) { //łuki wychodzące z aktualnego
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
			if(n.getInputArcs().isEmpty() || n.getOutputArcs().isEmpty())
				return false;
		}
		return true;
	}
}
