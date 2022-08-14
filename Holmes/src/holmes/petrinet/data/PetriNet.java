package holmes.petrinet.data;

import holmes.analyse.MCTCalculator;
import holmes.darkgui.GUIManager;
import holmes.darkgui.GUIController;
import holmes.files.io.AbyssReader;
import holmes.files.io.AbyssWriter;
import holmes.files.io.IOprotocols;
import holmes.files.io.Snoopy.NetHandler;
import holmes.files.io.Snoopy.NetHandler_Classic;
import holmes.files.io.Snoopy.NetHandler_Colored;
import holmes.files.io.Snoopy.NetHandler_Extended;
import holmes.files.io.Snoopy.NetHandler_Time;
import holmes.files.io.Snoopy.SnoopyReader;
import holmes.files.io.Snoopy.SnoopyWriter;
import holmes.graphpanel.GraphPanel;
import holmes.graphpanel.SelectionActionListener;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.elements.extensions.TransitionSPNExtension;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.petrinet.simulators.xtpn.GraphicalSimulatorXTPN;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.workspace.Workspace;
import holmes.workspace.WorkspaceSheet;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.simpleframework.xml.Root;


/**
 * Klasa przechowująca listy wszystkich obiektów projektu oraz arkusze. Dodatkowo agreguje
 * metody działające na sieci - zapis, odczyt, generowanie MCT, klastrów, itd.
 */
@Root
public class PetriNet implements SelectionActionListener, Cloneable {
	private ArrayList<SelectionActionListener> actionListeners = new ArrayList<>();
	private ArrayList<ArrayList<Integer>> t_invariantsMatrix; //macierz t-inwariantów
	private ArrayList<String> t_invariantsDescriptions;
	private boolean t_invComputed;
	private ArrayList<Integer> t_invariantsTypes;
	private ArrayList<ArrayList<Integer>> p_invariantsMatrix; //macierz p-inwariantów
	private ArrayList<String> p_invariantsDescriptions;
	private ArrayList<ArrayList<Integer>> colorVector = null;
	private ArrayList<ArrayList<Transition>> mctData;
	private ArrayList<Integer> transitionMCTnumber;
	private ArrayList<String> mctNames;
	private NetSimulationDataCore simData;
	private MCSDataMatrix mcsData;
	private P_StateManager statesManager;
	private SPNdataVectorManager firingRatesManager;
	private SSAplacesManager ssaManager;
	private ArrayList<String> subNetNames;
	private String lastFileName = "";
	private PetriNetData dataCore = new PetriNetData(new ArrayList<>(), new ArrayList<>(), "default");
	private ArrayList<GraphPanel> graphPanels;
	private IOprotocols communicationProtocol;
	private NetHandler handler;
	private Workspace workspace;
	private DrawModes drawMode = DrawModes.POINTER;
	private GraphicalSimulator simulator;
	private GraphicalSimulatorXTPN simulatorXTPN;
	private MCTCalculator analyzer;
	private PetriNetMethods methods;
	private boolean isSimulationActive = false;
	public boolean anythingChanged = false;
	public GUIManager overlord;


	/** [<b>ext - inhibitor, reset or equal Arcs present</b>]
	 * PN, TPN, PN_extArcs, FPN, timeFPN, TPN_extArcs, FPN_extArcs, timeFPN_extArcs, SPN, functionalSPN, XTPN
	 */
	public enum GlobalNetType { PN, TPN, PN_extArcs, FPN, timeFPN, TPN_extArcs, FPN_extArcs, timeFPN_extArcs
		, SPN, functionalSPN, XTPN }

	/**
	 *  CLEAN, FUNCTIONAL, EXT_ARCS, FUN_EXT_ARCS
	 */
	public enum GlobalNetSubType {CLEAN_XTPN, FUNCTIONAL, EXT_ARCS, FUNCTIONAL_EXT_ARCS}

	private GlobalNetType projectType = GlobalNetType.PN;
	private GlobalNetSubType projectSubType = GlobalNetSubType.CLEAN_XTPN;

	/** SPPED, SPEPT, SPTPT, HOLMESPROJECT */
	public enum GlobalFileNetType { SPPED, SPEPT, SPTPT, HOLMESPROJECT }
	
	
	/**
	 * Konstruktor obiektu klasy PetriNet - działa dla symulatora inwariantów.
	 * @param nod ArrayList[Node] - lista wierzchołków sieci
	 * @param ar ArrayList[Arc] - lista łuków sieci
	 */
	public PetriNet(ArrayList<Node> nod, ArrayList<Arc> ar) {
		overlord = GUIManager.getDefaultGUIManager();
		setProjectType(GlobalNetType.PN);
		getDataCore().nodes = nod;
		getDataCore().arcs = ar;
		this.communicationProtocol = new IOprotocols();
		this.dataCore.netName = "default";
		this.mcsData = new MCSDataMatrix();
		this.methods = new PetriNetMethods(this);
		this.simData = new NetSimulationDataCore();
		this.statesManager = new P_StateManager(this);
		this.ssaManager = new SSAplacesManager(this);
		this.t_invComputed = false;
	}

	/**
	 * Konstruktor obiektu klasy PetriNet - główny konstruktor dla workspace.
	 * @param workspace Workspace - obiekt obszaru roboczego dla sieci
	 */
	public PetriNet(Workspace workspace, String name) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.setGraphPanels(new ArrayList<>());
		this.workspace = workspace;
		this.setSimulator(new GraphicalSimulator(SimulatorGlobals.SimNetType.BASIC, this));
		this.setSimulatorXTPN(new GraphicalSimulatorXTPN(SimulatorGlobals.SimNetType.XTPN, this));
		this.setMCTanalyzer(new MCTCalculator(this));
		this.statesManager = new P_StateManager(this);
		this.ssaManager = new SSAplacesManager(this);
		this.firingRatesManager = new SPNdataVectorManager(this);
		resetComm();
		this.dataCore.netName = name;
		this.mcsData = new MCSDataMatrix();
		this.methods = new PetriNetMethods(this);
		this.simData = new NetSimulationDataCore();
		this.t_invComputed = false;
	}
	
	/**
	 * Reset obiektu protokołu komunikacyjnego. Odpowiada on za większość operacji I/O, posiada
	 * sporo pół wewnętrznych, które najłatwiej zresetować wymieniając cały obiekt. Czyszczeniem
	 * pamięci niech zajmie się JRE.
	 */
	public void resetComm() {
		communicationProtocol = new IOprotocols();
	}
	
	/**
	 * Metoda TYLKO do użytku przygotowania przestrzeni dla nowego projektu. Czyści dane sieci, nieodracalnie.
	 * Należy to zrobić w ten sposób, gdyż powiązania elementów logiki programu między sobą powodują, że proste
	 * stworzenie nowego obiektu tablicy nodes oraz arcs powoduje błędy w wyniku braku propagacji tej zmiany w
	 * w wielu obiektach programu.
	 */
	public void resetData() {
		overlord.log("Removing all nodes (places and transition) and all arcs.", "text", true);
		getDataCore().nodes.clear();
		getDataCore().arcs.clear();
		getDataCore().netName = "default";
	}

	/**
	 * Metoda pozwala pobrać wszystkie obiekty miejsc dla danej sieci.
	 * Wszystkie wierzchołki w projekcie są przechowywane w obrębie jednej
	 * listy ArrayList, jednak dzięki tej metodzie można ją przefiltrować.
	 * @return ArrayList[Place] - lista miejsc projektu sieci
	 */
	public ArrayList<Place> getPlaces() {
		ArrayList<Place> returnPlaces = new ArrayList<>();
		for (Node n : this.dataCore.nodes) {
			if (n instanceof Place)
				returnPlaces.add((Place) n);
		}
		return returnPlaces;
	}
	
	/**
	 * Zwraca liczbę miejsc sieci.
	 * @return int - liczba miejsc
	 */
	public int getPlacesNumber() {
		int counter = 0;
		for (Node n : this.dataCore.nodes) {
			if (n instanceof Place)
				counter++;
		}
		return counter;
	}

	/**
	 * Metoda pozwala pobrać wszystkie obiekty tranzycji dla danej sieci.
	 * Wszystkie wierzchołki w projekcie są przechowywane w obrębie jednej
	 * listy ArrayList[Node], jednak dzięki tej metodzie można ją przefiltrować.
	 * @return ArrayList[Transition] - lista tranzycji projektu sieci
	 */
	public ArrayList<Transition> getTransitions() {
		ArrayList<Transition> returnTransitions = new ArrayList<>();
		for (Node n : this.dataCore.nodes) {
			if (n instanceof Transition)
				returnTransitions.add((Transition) n);
		}
		return returnTransitions;
	}
	
	/**
	 * Metoda pozwala pobrać wszystkie obiekty tranzycji czasowych w danej sieci.
	 * @return ArrayList[TimeTransition] - lista tranzycji czasowych projektu sieci
	 */
	public ArrayList<Transition> getTimeTransitions() {
		ArrayList<Transition> returnTransitions = new ArrayList<>();
		for (Node n : this.dataCore.nodes) {
			if (n instanceof Transition) {
				if (((Transition)n).getTransType() == TransitionType.TPN)
					returnTransitions.add((Transition) n);
			}
			
		}
		return returnTransitions;
	}
	
	/**
	 * Zwraca macierz z wektorami elementów sieci: kolejne miejsc, tranzycji, tranzycji czasowych i metawęzłów
	 * @return ArrayList[ArrayList[Node]] - macierz z elementami sieci
	 */
	public ArrayList<ArrayList<Node>> getPNelements() {
		ArrayList<ArrayList<Node>> result = new ArrayList<>();
		ArrayList<Node> places = new ArrayList<>();
		ArrayList<Node> transitions = new ArrayList<>();
		ArrayList<Node> timeTransitions = new ArrayList<>();
		ArrayList<Node> metaNodes = new ArrayList<>();
		
		for(Node n : this.dataCore.nodes) {
			if (n instanceof Place) {
				places.add(n);
			} else if(n instanceof Transition) {
				if (((Transition)n).getTransType() == TransitionType.TPN)
					timeTransitions.add(n);
				else
					transitions.add(n);
			} else if(n instanceof MetaNode) {
				metaNodes.add(n);
			}
		}
		result.add(places);
		result.add(transitions);
		result.add(timeTransitions);
		result.add(metaNodes);
		return result;
	}
	
	/**
	 * Metoda zwraca nowy wektor wypełniony istniejącymi meta-węzłami
	 * @return ArrayList[MetaNoda] - lista meta-węzłów
	 */
	public ArrayList<MetaNode> getMetaNodes() {
		ArrayList<MetaNode> returnNodes = new ArrayList<>();
		for (Node n : this.dataCore.nodes) {
			if (n instanceof MetaNode) {
				returnNodes.add((MetaNode) n);
			}
		}
		return returnNodes;
	}

	/**
	 * Metoda pozwala ustawić listę wszystkich wierzchołków zawartych w projekcie.
	 * Zmiana ta zostaje automatycznie rozpropagowana do wszystkich arkuszy
	 * GraphPanel przechowywanych w projekcie.
	 * @param nodes ArrayList[Node] - nowa lista wierzchołków
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.getDataCore().nodes = nodes;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setNodes(nodes);
	}
	
	/**
	 * Metoda ustawia nazwę dla sieci Petriego.
	 * @param name String - nowa nazwa
	 */
	public void setName(String name) {
		getDataCore().netName = name;
	}
	
	/**
	 * Metoda pozwala uzyskać dostęp do nazwy pliku sieci.
	 * @return String - nazwa pliku
	 */
	public String getFileName() {
		return lastFileName;
	}
	
	/**
	 * Metoda ustawia nową nazwę pliku sieci.
	 * @param newName String - nowa nazwa pliku sieci
	 */
	public void setFileName(String newName) {
		lastFileName = newName;
	}
	
	/**
	 * Metoda zwraca nazwę sieci petriego.
	 * @return String - nazwa sieci
	 */
	public String getName() {
		return getDataCore().netName;
	}

	/**
	 * Metoda pozwala pobrać wszystkie łuki dla danej sieci.
	 * @return ArrayList[Arc] - lista łuków zawartych w projekcie
	 */
	public ArrayList<Arc> getArcs() {
		return this.getDataCore().arcs;
	}
	
	/**
	 * Metoda powoduje ustawienie w projekcie listy wierzchołków ArrayList[Node]
	 * oraz listy łuków ArrayList[Arc] z których zostanie zbudowana sieć. Zmiana
	 * ta zostaje automatycznie rozpropagowana na wszystkie arkusze w projekcie.
	 * @param arcs ArrayList[Arc] - nowa lista łuków
	 * @param nodes ArrayList[Node] - nowa lista wierzchołków
	 */
	private void addArcsAndNodes(ArrayList<Arc> arcs, ArrayList<Node> nodes) {
		this.getArcs().addAll(arcs);
		this.getNodes().addAll(nodes);
	}

	public void addArc(Arc a){
		this.getArcs().add(a);
	}
	
	/**
	 * Metoda pozwala ustawić nowe łuki dla danej sieci. Zmiana
	 * ta zostaje automatycznie rozpropagowana do wszystkich .
	 * @param arcs ArrayList[Arc] - nowa lista łuków
	 */
	public void setArcs(ArrayList<Arc> arcs) {
		this.getDataCore().arcs = arcs;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setArcs(this.getDataCore().arcs);
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich wierzchołków Node zawartych w projekcie.
	 * @return ArrayList[Node] - lista wierzchołków sieci
	 */
	public ArrayList<Node> getNodes() {
		return this.getDataCore().nodes;
	}
	
	/**
	 * Metoda pozwala ustawić aktualny tryb rysowania definiowany przez typ DrawModes.
	 * Zmiana ta zostaje rozpropagowana  na wszystkie arkusze, przez co zmiana trybu
	 * rysowania staje się globalna.
	 * @param mode DrawModes - nowy tryb rysowania
	 */
	public void setDrawMode(DrawModes mode) {
		this.drawMode = mode;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setDrawMode(mode);
	}

	/**
	 * Metoda pobiera aktualny tryb rysowania definiowany przez tryb DrawModes.
	 * @return DrawModes - tryb rysowania
	 */
	@SuppressWarnings("unused")
	public DrawModes getDrawMode() {
		return this.drawMode;
	}
	
	/**
	 * Metoda zwracająca handler oczytu pliku sieci.
	 * @return NetHandler - obiekt dla parsera w czasie czytania pliku
	 */
	@SuppressWarnings("unused")
	public NetHandler getHandler() {
		return handler;
	}

	/**
	 * Metoda zwracająca obiekt w ramach którego działa aktualna sieć.
	 * @return Workspace - obiekt zawierający obiekt sieci.
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * Metoda ustawia nowy obiekt w ramach którego działa sieś Petriego.
	 * @param workspace Workspace - jak wyżej
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Metoda ustawia obiekt nasłuchujący o zmianach na sieci.
	 * @param a SelectionActionListener - obiekt nasłuchujący
	 */
	public void addActionListener(SelectionActionListener a) {
		this.actionListeners.add(a);
	}

	/**
	 * Metoda aktywująca obiekty nasłuchujące sieci.
	 * @param e SelectionActionEvent - obiekty nasłuchujące
	 */
	private void invokeActionListeners(SelectionActionEvent e) {
		for (SelectionActionListener a : this.actionListeners)
			a.actionPerformed(e);
	}

	/**
	 * Metoda będąca implementacją interfejsu SelectionActionListener,
	 * wywoływana dla zajęcia zdarzanie zmiany znaczenia na którymkolwiek z
	 * arkuszy GraphPan) zawartych w projekcie, dla których obiekt klasy PetriNet
	 * jest domyślnym obiektem nasłuchującym. Zdarzenie jest propagowane dalej, 
	 * przekazując o nim informacje do innych elementów frameworku, jak np. DockWindows.
	 * @param arg0 SelectionActionEvent -  zdarzenie przekazujące informacje o zaznaczonych obiektach
	 */
	public void actionPerformed(SelectionActionEvent arg0) {
		this.invokeActionListeners(arg0);
	}

	/**
	 * Metoda pozwala na pobrania aktualnego obiektu symulatora dla bieżącego projektu.
	 * @return (<b>NetSimulator</b>) - symulator sieci klasycznych.
	 */
	public GraphicalSimulator getSimulator() {
		return simulator;
	}
	
	/**
	 * Metoda pozwala na ustawienie symulatora NetSimulator dla bieżącego projektu
	 * @param simulator (<b>NetSimulator</b>) nowy symulator klasy NetSimulator (zwykły)
	 */
	public void setSimulator(GraphicalSimulator simulator) {
		this.simulator = simulator;
	}

	/**
	 * Metoda pozwala na pobrania aktualnego obiektu symulatora XTPN.
	 * @return (<b>NetSimulatorXTPN</b>) - symulator sieci XTPN.
	 */
	public GraphicalSimulatorXTPN getSimulatorXTPN() {
		return simulatorXTPN;
	}

	/**
	 * Metoda pozwala na ustawienie symulatora XTPN dla bieżącego projektu
	 * @param simulator (<b>NetSimulatorXTPN</b>) nowy symulator klasy NetSimulatorXTPN (XTPN)
	 */
	public void setSimulatorXTPN(GraphicalSimulatorXTPN simulator) {
		this.simulatorXTPN = simulator;
	}

	/**
	 * Metoda zwracająca obiekt analizatora, np. na potrzeby generacji MCT.
	 * @return DarkAnalyzer - obiekt analizatora
	 */
	public MCTCalculator getMCTanalyzer() {
		return analyzer;
	}

	/**
	 * Metoda ustawiająca aktualny analizator dla obiektu sieci.
	 * @param analyzer DarkAnalyzer - analizator dla sieci
	 */
	public void setMCTanalyzer(MCTCalculator analyzer) {
		this.analyzer = analyzer;
	}
	
	/**
	 * Metoda zwraca tablicę arkuszy sieci.
	 * @return ArrayList[GraphPanel] - lista arkuszy.
	 */
	public ArrayList<GraphPanel> getGraphPanels() {
		return graphPanels;
	}

	/**
	 * Metoda ustawiająca tablicę arkuszy sieci.
	 * @param graphPanels ArrayList[GraphPanel] - arkusze sieci
	 */
	private void setGraphPanels(ArrayList<GraphPanel> graphPanels) {
		this.graphPanels = graphPanels;
	}

	/**
	 * Metoda zwracająca pełne dane sieci - wierzchołki i łuki.
	 * @return PetriNetData - wierzchołki i łuki sieci
	 */
	public PetriNetData getDataCore() {
		return dataCore;
	}

	/**
	 * Metoda ustawiająca pełne dane sieci - wierzchołki i łuki.
	 * @param data PetriNetData - wierzchołki i łuki sieci
	 */
	public void setData(PetriNetData data) {
		this.dataCore = data;
	}
	
	/**
	 * Klonowanie obiektu klasy PetriNet.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	
	/**
	 * Metoda ustawia nową macierz t-inwariantów sieci. Przy okazji resetuje macierz typów inwariantów.
	 * @param t_invariants ArrayList[ArrayList[Integer]] - macierz t-inwariantów
	 * @param generateMCT boolean - true, jeśli mają być wygenerowane zbiory MCT 
	 */
	public void setT_InvMatrix(ArrayList<ArrayList<Integer>> t_invariants, boolean generateMCT) {
		this.t_invariantsMatrix = t_invariants;
		this.t_invariantsDescriptions = null;
		this.t_invariantsTypes = null;
		this.t_invComputed = false;
		
		if(t_invariants == null)
			return;
		else {
			this.t_invariantsDescriptions = new ArrayList<>();
			this.t_invariantsTypes = new ArrayList<>();
		}
		
		for(int i=0; i<t_invariantsMatrix.size(); i++) {
			t_invariantsDescriptions.add("Default description of t-invariant #"+(i+1));
			t_invariantsTypes.add(99999);
		}
		
		if(generateMCT) {
			MCTCalculator analyzer = getWorkspace().getProject().getMCTanalyzer();
			ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
			getWorkspace().getProject().setMCTMatrix(mct, true);
			overlord.getMctBox().showMCT(mct);
		}
	}
	
	/**
	 * Metoda ustawia nową macierz p-inwariantów sieci.
	 * @param p_invariants ArrayList[ArrayList[Integer]] - macierz p-inwariantów
	 */
	public void setP_InvMatrix(ArrayList<ArrayList<Integer>> p_invariants) {
		this.p_invariantsMatrix = p_invariants;
		this.p_invariantsDescriptions = null;
		
		if(p_invariants == null)
			return;
		else 
			this.p_invariantsDescriptions = new ArrayList<>();
		
		for(int i=0; i<p_invariantsMatrix.size(); i++) {
			p_invariantsDescriptions.add("Default description of p-invariant #"+(i+1));
		}
	}
	
	/**
	 * Metoda ustawia nowy wektor opisów dla t-inwariantów.
	 * @param namesVector ArrayList[String] - nazwy inwariantów
	 */
	public void setT_InvDescriptions(ArrayList<String> namesVector) {
		if(t_invariantsMatrix == null)
			return;
		if(namesVector.size() == t_invariantsMatrix.size())
			this.t_invariantsDescriptions = namesVector;
	}
	
	/**
	 * Metoda ustawia nowy wektor typów t-inwariantów
	 * @param typesVector ArrayList[Integer] - nowy wektor typów t-inw.
	 */
	public void setT_InvTypes(ArrayList<Integer> typesVector) {
		if(t_invariantsMatrix == null)
			return;
		if(typesVector.size() == t_invariantsMatrix.size())
			this.t_invariantsTypes = typesVector;
	}
	
	/**
	 * Metoda ustawia nowy wektor opisów dla p-inwariantów.
	 * @param namesVector ArrayList[String] - nazwy p-inwariantów
	 */
	public void setP_InvDescriptions(ArrayList<String> namesVector) {
		if(p_invariantsMatrix == null)
			return;
		if(namesVector.size() == p_invariantsMatrix.size())
			this.p_invariantsDescriptions = namesVector;
	}
	
	/**
	 * Zwraca opis t-inwariantu o podanym numerze.
	 * @param index int - nr t-inwariantu, od zera
	 * @return String - nazwa t-inwariantu
	 */
	public String getT_InvDescription(int index) {
		if(t_invariantsMatrix == null)
			return "";
		
		if(index < t_invariantsDescriptions.size() && index >= 0)
			return this.t_invariantsDescriptions.get(index);
		else
			return "";
	}
	
	/**
	 * Zwraca opis p-inwariantu o podanym numerze.
	 * @param index int - nr p-inwariantu, od zera
	 * @return String - nazwa p-inwariantu
	 */
	@SuppressWarnings("unused")
	public String getP_InvDescription(int index) {
		if(p_invariantsMatrix == null)
			return "";
		
		if(index < p_invariantsDescriptions.size() && index >= 0)
			return this.p_invariantsDescriptions.get(index);
		else
			return "";
	}

	/**
	 * Metoda zwraca macierz t-inwariantów sieci.
	 * @return ArrayList[ArrayList[Integer]] - macierz t-inwariantów
	 */
	public ArrayList<ArrayList<Integer>> getT_InvMatrix() {
		return t_invariantsMatrix;
	}
	
	/**
	 * Metoda zwraca macierz p-inwariantów sieci.
	 * @return ArrayList[ArrayList[Integer]] - macierz p-inwariantów
	 */
	public ArrayList<ArrayList<Integer>> getP_InvMatrix() {
		return p_invariantsMatrix;
	}
	
	/**
	 * Metoda pozwala na dostęp do wektora nazw t-inwariantów.
	 * @return ArrayList[String] - nazwy t-inwariantów
	 */
	public ArrayList<String> accessT_InvDescriptions() {
		return t_invariantsDescriptions;
	}
	
	/**
	 * Metoda zwraca wektor informacji o type t-inwariantu.
	 * @return ArrayList[Integer] - wektor typów t-inwariantów
	 */
	public ArrayList<Integer> accessT_InvTypesVector() {
		return t_invariantsTypes;
	}
	
	/**
	 * Metoda pozwala na dostęp do wektora nazw p-inwariantów.
	 * @return ArrayList[String] - nazwy p-inwariantów
	 */
	public ArrayList<String> accessP_InvDescriptions() {
		return p_invariantsDescriptions;
	}
	
	/**
	 * Ustawia status wektora typów t-inwariantów, tj. czy został już obliczony, czy nie.
	 * @param status boolean - status
	 */
	public void setT_invTypesComputed(boolean status) {
		this.t_invComputed = status;
	}
	
	/**
	 * Zwraca true, jeśli istnieje wektor typów t-inwariantów (został obliczony)
	 * @return boolean t_invComputed - status
	 */
	public boolean getT_invTypesComputed() {
		return this.t_invComputed;
	}
	
	/**
	 * Metoda ustawia nową macierz zbiorów MCT.
	 * @param mct ArrayList[ArrayList[Transition]] - macierz MCT
	 * @param sort boolean - true, jeśli mają być posortowane
	 */
	public void setMCTMatrix(ArrayList<ArrayList<Transition>> mct, boolean sort) {
		this.mctData = mct;
		this.mctNames = new ArrayList<>();
		if(mct == null)
			return;
		
		if(sort)
			mct = MCTCalculator.getSortedMCT(mct, true);
		
		for(int m=0; m<mct.size(); m++) {
			mctNames.add("default name for mct"+(m+1));
		}
		
		transitionMCTnumber = methods.getTransMCTindicesVector();
	}
	
	/**
	 * Metoda zwraca wektor okreslający w którym MCT znajduje się dana tranzycja. -1 oznacza trywialne.
	 * @return ArrayList[Integer] - wektor
	 */
	public ArrayList<Integer> getMCTtransIndicesVector() {
		return this.transitionMCTnumber;
	}
	
	/**
	 * Metoda zwraca macierz zbiorów MCT.
	 * @return ArrayList[ArrayList[Transition]] - zbiory MCT
	 */
	public ArrayList<ArrayList<Transition>> getMCTMatrix() {
		return mctData;
	}
	
	/**
	 * Metoda zwraca obiekt wektora nazw zbiorów MCT.
	 * @return (<b>ArrayList[String]</b>) wektor nazw.
	 */
	public ArrayList<String> accessMCTnames() {
		return mctNames;
	}
	
	/**
	 * Metoda ustawia nowy wektor nazw dla zbiorów MCT.
	 * @param namesVector (<b>ArrayList[String]</b>) wektor nazw MCT.
	 */
	public void setMCTNames(ArrayList<String> namesVector) {
		if(mctData == null)
			return;
		if(namesVector.size() == mctData.size())
			this.mctNames = namesVector;
	}

	public ArrayList<String> accessSubNetNames() {
		return subNetNames;
	}
	
	/**
	 * Zwraca nazwę zbioru MCT wg podanego indeksu.
	 * @param index int - nr zbioru MCT (od zera)
	 * @return String - nazwa
	 */
	public String getMCTname(int index) {
		if(mctData == null)
			return "";
		
		if(index < mctNames.size() && index >= 0)
			return this.mctNames.get(index);
		else
			return "";
	}

	/**
	 * Metoda pozwala na dodanie do projektu nowego podanego w parametrze
	 * arkusza GraphPanel. Zostanie automatycznie ustawiona dla niego lista
	 * wierzchołków oraz łuków.
	 * @param sheetID int - identyfikator nowego arkusza
	 * @return boolean - true jeżeli arkusz nie ma węzłów;
	 * 		false w przeciwnym wypadku
	 */
	@SuppressWarnings("unused")
	public boolean isSheetEmpty(int sheetID) {
		boolean result = true;
		for (Node node : getNodes())
			for (ElementLocation location : node.getNodeLocations())
				if (location.getSheetID() == sheetID) {
					result = false;
					break;
				}
		return result;
	}

	/**
	 * Metoda pozwala na dodanie do projektu nowego podanego w parametrze
	 * arkusza GraphPanel. Zostanie automatycznie ustawiona dla niego lista
	 * wierzchołków oraz łuków.
	 * @param graphPanel - nowy arkusz
	 */
	public void addGraphPanel(GraphPanel graphPanel) {
		graphPanel.setNodesAndArcs(this.getDataCore().nodes, this.getDataCore().arcs);
		this.getGraphPanels().add(graphPanel);
	}

	/**
	 * Metoda pozwala na stworzenie, a następnie dodanie do projektu nowego arkusza
	 * GraphPanel, o zadanym w parametrze identyfikatorze sheetId.
	 * @param sheetId int - identyfikator nowego arkusza
	 * @return GraphPanel - który w wyniku wywołania metody został utworzony
	 */
	public GraphPanel createAndAddGraphPanel(int sheetId) {
		GraphPanel gp = new GraphPanel(sheetId, this, this.getDataCore().nodes, this.getDataCore().arcs);
		gp.setDrawMode(this.drawMode);
		this.getGraphPanels().add(gp);
		return gp;
	}

	/**
	 * Metoda pozwala pobrać arkusz GraphPanel o podanym w parametrze identyfikatorze sheetId.
	 * @param sheetID int - identyfikator arkusza
	 * @return GraphPanel - o podanym identyfikatorze.
	 * 		Jeśli taki nie istnieje, metoda zwraca wartość null
	 */
	public GraphPanel getGraphPanel(int sheetID) {
		for (GraphPanel gp : this.getGraphPanels())
			if (gp.getSheetId() == sheetID)
				return gp;
		return null;
	}

	/**
	 * Metoda zwraca obiekt głównego komunikatora obiektu sieci, zawierającego
	 * metody I/O dla programu / formatu INA.
	 * @return INAprotocols - obiekt klasy odpowiedzialnej za pliki
	 */
	public IOprotocols getCommunicator() {
		return communicationProtocol;
	}
	
	/**
	 * Metoda zwraca obiekt przechowujący zbiory MCS;
	 * @return MinCutSetData - obiekt MCS
	 */
	public MCSDataMatrix getMCSdataCore() {
		return mcsData;
	}
	
	/**
	 * Metoda ustawia nowy obiekt danych zbiorów MCS.
	 * @param newMCS MCSDataMatrix - nowy obiekt
	 */
	public void setMCSdataCore(MCSDataMatrix newMCS) {
		mcsData = newMCS;
	}
	
	/**
	 * Dostęp do obiektu danych symulacji knockout.
	 * @return NetSimulationDataCore - obiekt
	 */
	public NetSimulationDataCore accessSimKnockoutData() {
		return this.simData;
	}
	
	/**
	 * Ustawia nowy obiekt danych symulacji.
	 * @param data NetSimulationDataCore - dane
	 */
	public void setNewKnockoutData(NetSimulationDataCore data) {
		this.simData = data;
	}
	
	/**
	 * Pozwala wyczyścić obiekt danych symulacji knockout.
	 */
	public void clearSimKnockoutData() {
		this.simData = null;
		this.simData = new NetSimulationDataCore();
	}
	
	/**
	 * Umożliwia dostęp do managera stanów sieci.
	 * @return (<b>StatesManager</b>) obiekt managera stanów.
	 */
	public P_StateManager accessStatesManager() {
		return this.statesManager;
	}
	
	/**
	 * Umożliwia dostęp do managera wektorów SSA sieci.
	 * @return SSAplacesManager - obiekt managera
	 */
	public SSAplacesManager accessSSAmanager() {
		return this.ssaManager;
	}
	
	public void replaceStatesManager(P_StateManager newStatesMngr) {
		statesManager = newStatesMngr;
	}
	
	/**
	 * Umożliwia dostęp do managera odpaleń tranzycji SPN sieci.
	 * @return FiringRatesManager - obiekt managera fr
	 */
	public SPNdataVectorManager accessFiringRatesManager() {
		return this.firingRatesManager;
	}


	public void replaceStatesManager(SPNdataVectorManager newStatesMngr) {
		this.firingRatesManager = newStatesMngr;
	}
	
	//*********************************************************************************

	/**
	 * Ustawia odpowiedni symulator w oknach symulacji. W sumie to chodzi o XTPN vs cała reszta.
	 * @param XTPN (<b>boolean</b>) true, jeśli ma być ustawiony XTPN.
	 */
	public void selectProperSimulatorBox(boolean XTPN) {
		int simulatorType = overlord.getSimulatorBox().getCurrentDockWindow().simulatorType;
		if(XTPN) {
			if(simulatorType == 0) { //jeśli wybrane są klasyczne, przełącz na XTPN
				overlord.getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(4);
			}
		} else {
			if(simulatorType == 1) { //wybrany jest XTPN, przełaczamy na normalne
				overlord.getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(0);
			}
		}
	}


	/**
	 * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji. Liczba tokenów jest przywracana
	 * z aktualnie wskazanego stanu w managerze stanów, tranzycje są resetowane wewnętrznie.
	 */
	public void restoreMarkingZero() {
		try {
			if(GUIController.access().getCurrentNetType() == GlobalNetType.XTPN) {
				restoreMarkingZeroXTPN();
				return;
			}
			accessStatesManager().setNetworkStatePN(accessStatesManager().selectedStatePN);

			for(Transition trans : getTransitions()) {
				trans.setLaunching(false);
				if(trans.getTransType() == TransitionType.TPN) {
					trans.timeExtension.resetTimeVariables();
				}
			}

			SimulatorGlobals.SimNetType nt = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimNetType();
			getSimulator().resetSimulator();

			//getSimulatorXTPN().resetSimulator();

			//setSimulator(new GraphicalSimulator(nt, this));
			//setSimulatorXTPN(new GraphicalSimulatorXTPN(SimulatorGlobals.SimNetType.XTPN, this));
			//overlord.getSimulatorBox().getCurrentDockWindow().setSimulator(getSimulator(), getSimulatorXTPN()); //ustawia nowe instancje symulatorów
			overlord.simSettings.currentStep = getSimulator().getSimulatorTimeStep(); //-1, jak wyżej
			overlord.getSimulatorBox().getCurrentDockWindow().timeStepLabelValue.setText("" + getSimulator().getSimulatorTimeStep());
			repaintAllGraphPanels();
			getSimulator().getSimLogger().logSimReset();
		} catch (Exception e) {
			overlord.log("Unknown error: unable to restore state m0 on request.", "error", true);
		}
	}

	/**
	 * Wywoływana TYLKO przez restoreMarkingZero(), jeśli ta wykryje elementy XTPN. Jedyny wyjątek to
	 * dwa wywołania przy tworzeniu nowego miejsca i nowej tranzycji XTPN.
	 */
	public void restoreMarkingZeroXTPN() {
		try {
			accessStatesManager().replaceNetStateWithSelectedMultiset_M(accessStatesManager().selectedStateXTPN);
			for(Transition trans : getTransitions()) {
				if( !(trans instanceof TransitionXTPN)) {
					overlord.log("Critical error (834528336), wrong place object.", "error", true);
					return;
				}

				trans.setLaunching(false);
				((TransitionXTPN)trans).resetTimeVariables_xTPN();
			}

			SimulatorGlobals.SimNetType nt = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimNetType();
			//getSimulator().resetSimulator();
			getSimulatorXTPN().resetSimulator();

			//setSimulator(new GraphicalSimulator(nt, this));
			//setSimulatorXTPN(new GraphicalSimulatorXTPN(SimulatorGlobals.SimNetType.XTPN, this));
			//overlord.getSimulatorBox().getCurrentDockWindow().setSimulator(getSimulator(), getSimulatorXTPN()); //ustawia nowe instancje symulatorów
			overlord.io.updateTimeStep(true, getSimulatorXTPN().getStepsCounterXTPN(), getSimulatorXTPN().getTimeCounterXTPN()); //-1 po resecie symulatorów
			overlord.simSettings.currentStep = getSimulatorXTPN().getStepsCounterXTPN(); //-1, jak wyżej
			overlord.simSettings.currentTime = getSimulatorXTPN().getTimeCounterXTPN();

			repaintAllGraphPanels();
			getSimulator().getSimLogger().logSimReset();
		} catch (Exception e) {
			overlord.log("Unknown error: unable to restore XTOM p-state m0 on request.", "error", true);
		}
	}
	
	/**
	 * Szybsza wersja przywracania stanu sieci, bez zawracania głowy okna SimulatorBox j.w.
	 * @param transitions (<b>ArrayList[Transition]</b>) wektor tranzycji.
	 */
	public void restoreMarkingZeroFast(ArrayList<Transition> transitions) {
		try {
			if(checkIfXTPNpresent()) {
				accessStatesManager().replaceNetStateWithSelectedMultiset_M(accessStatesManager().selectedStateXTPN);
			} else {
				accessStatesManager().setNetworkStatePN(accessStatesManager().selectedStatePN);
			}
	
			for(Transition trans : transitions) {
				trans.setLaunching(false);
				if(trans.getTransType() == TransitionType.TPN) {
					trans.timeExtension.resetTimeVariables();
				}
				if( trans instanceof TransitionXTPN ) {
					((TransitionXTPN)trans).resetTimeVariables_xTPN();
				}
			}
			getSimulator().getSimLogger().logSimReset();
		} catch (Exception e) {
			overlord.log("Unknown error: unable to restore state m0 on request.", "error", true);
		}
	}
	
	/**
	 * Metoda przechowuje stan kolorowany m0 sieci.
	 */
	public void storeColors() {
		colorVector = new ArrayList<>();
		for(Place place : getPlaces()) {
			ArrayList<Integer> tokensC = new ArrayList<>();
			tokensC.add( ((PlaceColored)place).getColorTokensNumber(0));
			tokensC.add( ((PlaceColored)place).getColorTokensNumber(1));
			tokensC.add( ((PlaceColored)place).getColorTokensNumber(2));
			tokensC.add( ((PlaceColored)place).getColorTokensNumber(3));
			tokensC.add( ((PlaceColored)place).getColorTokensNumber(4));
			tokensC.add( ((PlaceColored)place).getColorTokensNumber(5));
			colorVector.add(tokensC);
		}
	}
	
	/**
	 * Metoda przywraca stan początkowy sieci kolorowej.
	 */
	public void restoreColors() {
		if(checkIfXTPNpresent())
			return;
		try {
			ArrayList<Place> places = getPlaces();
			for (Place place : places) {
				place.setTokensNumber(0);
			}
			if(colorVector == null)
				return;
			
			places = getPlaces();
			for(int p = 0; p<places.size(); p++) {
				PlaceColored place = (PlaceColored)places.get(p);
				place.setColorTokensNumber(colorVector.get(p).get(0), 0);
				place.setColorTokensNumber(colorVector.get(p).get(1), 1);
				place.setColorTokensNumber(colorVector.get(p).get(2), 2);
				place.setColorTokensNumber(colorVector.get(p).get(3), 3);
				place.setColorTokensNumber(colorVector.get(p).get(4), 4);
				place.setColorTokensNumber(colorVector.get(p).get(5), 5);
				place.setTokensNumber(0);
			}
			ArrayList<Transition> transitions = getTransitions();
			for(Transition trans : transitions) {
				trans.setLaunching(false);
				
				if(trans.getTransType() == TransitionType.TPN) {
					trans.timeExtension.resetTimeVariables();
				}
			}

			SimulatorGlobals.SimNetType nt = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimNetType();
			getSimulator().resetSimulator();
			getSimulatorXTPN().resetSimulator();
			//setSimulator(new GraphicalSimulator(nt, this));
			//setSimulatorXTPN(new GraphicalSimulatorXTPN(SimulatorGlobals.SimNetType.XTPN, this));
			overlord.getSimulatorBox().getCurrentDockWindow().setSimulator(getSimulator(), getSimulatorXTPN());
			overlord.io.updateTimeStep(false, getSimulator().getSimulatorTimeStep(), 0);
			overlord.simSettings.currentStep = getSimulator().getSimulatorTimeStep();
			
			repaintAllGraphPanels();
			getSimulator().getSimLogger().logSimReset();
		} catch (Exception e) {
			overlord.log("Unknown error: unable to restore colored state m0 on request.", "error", true);
		}
	}
	
	/**
	 * Metoda wyłączająca świecenie tranzycji np. w ramach aktywacji niezmiennika.
	 */
	private void turnTransitionGlowingOff() {
		for (GraphPanel gp : getGraphPanels()) {
			gp.getSelectionManager().removeTransitionsGlowing();
			gp.repaint();
		}
	}

	/**
	 * Metoda powoduje usunięcie arkusza GraphPanel o identyfikatorze podanym w
	 * parametrze. Nie powoduje to jednak usunięcia zawartych na nim wierzchołków
	 * oraz łuków.
	 * @param sheetID int - identyfikator który ma zostać usunięty
	 * @return boolean - true w sytuacji powodzenia operacji usunięcia, gdy GraphPanel o podanym identyfikatorze istniał;
	 *		false w przypadku przeciwnym
	 */
	public boolean removeGraphPanel(int sheetID) {
		System.out.println("Before deletion:");
		for (GraphPanel g : getGraphPanels())
			System.out.println(g);
		for (Node n : getNodes())
			System.out.println(n);
		for (Arc n : getArcs())
			System.out.println(n);
		for (Iterator<GraphPanel> gpIterator = getGraphPanels().iterator(); gpIterator.hasNext();)
			if (gpIterator.next().getSheetId() == sheetID) {
				gpIterator.remove();
				for (Iterator<Node> nodeIterator = getNodes().iterator(); nodeIterator.hasNext();) {
					Node n = nodeIterator.next();
					for (ElementLocation el : n.getNodeLocations(sheetID)) {
						if (!n.removeElementLocation(el)) {
							nodeIterator.remove();
						}
						for (Iterator<Arc> j = el.getInArcs().iterator(); j.hasNext(); ) {
							this.getArcs().remove(j.next());
							j.remove();
						}
						// deletes all out arcs of current ElementLocation
						for (Iterator<Arc> j = el.getOutArcs().iterator(); j.hasNext(); ) {
							this.getArcs().remove(j.next());
							j.remove();
						}
					}
				}
				System.out.println("After deletion:");
				for (GraphPanel g : getGraphPanels())
					System.out.println(g);
				for (Node n : getNodes())
					System.out.println(n);
				for (Arc n : getArcs())
					System.out.println(n);
				return true;
			}
		return false;
	}

	/**
	 * Metoda sprawdza potencjalne konflikty w nazwach wierzchołków.
	 * @param name - nazwa wierzchołka
	 * @return boolean - true jeżeli nazwa już istnieje; false w przeciwnym wypadku
	 */
	public boolean checkNameConflict(String name) {
		for (Node n : this.getNodes())
			if (n.getName().equals(name))
				return true;
		for (Arc a : this.getArcs())
			if (a.getName().equals(name))
				return true;
		return false;
	}

	/**
	 * Metoda zwraca listę obrazów utworzonych ze wszystkich istniejących arkuszy.
	 * @return ArrayList[BufferedImage] - lista obrazów
	 */
	public ArrayList<BufferedImage> getImagesFromGraphPanels() {
		ArrayList<BufferedImage> images = new ArrayList<>();
		for (GraphPanel g : this.getGraphPanels())
			images.add(g.createImageFromSheet());
		return images;
	}

	/**
	 * Metoda pozwala na zapis całej sieci z projektu do pliku PNT
	 * @param filePath String - ścieżka do pliku zapisu
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean saveAsPNT(String filePath) {
		return communicationProtocol.writePNT(filePath, getPlaces(), getTransitions(), getArcs());
	}
	
	/**
	 * Metoda pozwala na zapis całej sieci do pliku projektu.
	 * @param filePath String - ścieżka do pliku zapisu
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean saveAsAbyss(String filePath) {
		AbyssWriter ABYSSSwriter = new AbyssWriter();
		return ABYSSSwriter.write(filePath);
	}
	
	/**
	 * Metoda pozwala zapisać sieć do formatu SPPED programu Snoopy.
	 * @param filePath String - ścieżka docelowa pliku
	 * @return boolean - status operacji: true jeśli nie było błędów
	 */
	public boolean saveAsSPPED(String filePath) {
		SnoopyWriter sWr = new SnoopyWriter();
		return sWr.writeSPPED(filePath);
	}
	
	/**
	 * Metoda pozwala zapisać sieć do formatu SPEPT (Extended) programu Snoopy.
	 * @param filePath String - ścieżka docelowa pliku
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean saveAsSPEPT(String filePath) {
		SnoopyWriter sWr = new SnoopyWriter();
		return sWr.writeSPEPT(filePath);
	}
	
	/**
	 * Metoda pozwala zapisać sieć do formatu SPTPT (czasowe) programu Snoopy.
	 * @param filePath String - ścieżka docelowa pliku
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean saveAsSPTPT(String filePath) {
		SnoopyWriter sWr = new SnoopyWriter();
		return sWr.writeSPTPT(filePath);
	}

	/**
	 * Metoda pozwala na odczyt całej sieci z pliku podanego w parametrze metody.
	 * @param path String - ścieżka do pliku odczytu
	 */
	public boolean loadFromFile(String path) {
		boolean status = overlord.reset.newProjectInitiated();
		if(!status) {
			return false;
		}

		SAXParserFactory readerSNOOPY = SAXParserFactory.newInstance();
		try {
			// Format własny
			if (path.endsWith(".abyss")) {
				AbyssReader ABYSSReader = new AbyssReader();
				ABYSSReader.read(path);
				addArcsAndNodes(ABYSSReader.getArcArray(), ABYSSReader.getNodeArray());
				setName(ABYSSReader.getPNname());
				
				String name = path;
				int ind = name.lastIndexOf("\\");
				if(ind > 1)
					name = name.substring(ind+1);
				
				setFileName(name);
			}
			// Formaty Snoopiego
			if (path.endsWith(".spped") || path.endsWith(".spept") || path.endsWith(".colpn") || path.endsWith(".sptpt") || path.endsWith(".pn") || path.endsWith(".xpn")) {
				if(overlord.getSettingsManager().getValue("programUseOldSnoopyLoaders").equals("1")) {
					overlord.log("Activating old Snoopy loader. Will fail for hierarchical networks.", "text", true);
					InputStream xmlInput = new FileInputStream(path);
					SAXParser saxParser = readerSNOOPY.newSAXParser();
					if (path.endsWith(".spped") || path.endsWith(".pn")) {
						handler = new NetHandler_Classic();
					}
					if (path.endsWith(".spept")|| path.endsWith(".xpn")) {
						handler = new NetHandler_Extended();
					}
					if (path.endsWith(".colpn")) {
						handler = new NetHandler_Colored();
					}
					if (path.endsWith(".sptpt")) {
						handler = new NetHandler_Time();
					}
					saxParser.parse(xmlInput, handler);
					addArcsAndNodes(handler.getArcList(), handler.getNodesList());
					accessStatesManager().createCleanStatePN();
					accessSSAmanager().createCleanSSAvector();
					accessFiringRatesManager().createCleanSPNdataVector();
					
					String name = path;
					int ind = name.lastIndexOf("\\");
					if(ind > 1)
						name = name.substring(ind+1);
					
					setFileName(name);
					name = name.replace(".spped", "");
					name = name.replace(".spept", "");
					name = name.replace(".colpn", "");
					name = name.replace(".sptpt", "");
					name = name.replace(".pn", "");
					name = name.replace(".xpn", "");
					setName(name);
				} else { //new loader
					SnoopyReader reader = new SnoopyReader(0, path);
					addArcsAndNodes(reader.getArcList(), reader.getNodesList());
					accessStatesManager().createCleanStatePN();
					accessFiringRatesManager().createCleanSPNdataVector();
					accessSSAmanager().createCleanSSAvector();
					overlord.subnetsGraphics.addRequiredSheets();
					overlord.subnetsGraphics.resizePanels();
					overlord.getWorkspace().setSelectedDock(0);
				}
				
				int nodeSID = overlord.getWorkspace().getSheets().size() - 1;
				int SIN = overlord.IDtoIndex(nodeSID);
				GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(SIN).getGraphPanel();
				graphPanel.setOriginSize(graphPanel.getSize());
				graphPanel.repaint();
			}

			if (path.endsWith(".pnt")) {
				communicationProtocol.readPNT(path);
				addArcsAndNodes(communicationProtocol.getArcArray(), communicationProtocol.getNodeArray());
				accessStatesManager().createCleanStatePN();
				accessFiringRatesManager().createCleanSPNdataVector();
			}
			
			overlord.log("Petri net successfully imported from file "+path, "text", true);

			ArrayList<Place> places = getPlaces();
			ArrayList<Arc> arcs = getArcs();
			ArrayList<Transition> transitions = getTransitions();
			for(Transition transition : transitions) { //aktywacja wektorów funkcji
				transition.fpnExtension.checkFunctions(arcs, places);
			}
			
			return true;
		} catch (Exception e) {
			overlord.log("Critical error while loading network: " + e.getMessage(), "error", true);
			return false;
		}
	}

	/**
	 * Metoda zapisująca inwarianty do pliku w formacie CSV.
	 * @param path String - ścieżka do pliku zapisu
	 * @param silence boolean - true, jeśli mają być komunikaty
	 * @param t_inv boolean - true, jeśli chodzi o t-inwarianty
	 * @return int - 0 jeśli operacja się udała, -1 w przeciwnym wypadku
	 */
	public int saveInvariantsToCSV(String path, boolean silence, boolean t_inv) {
		int result = -1;
		try {
			if(t_inv) {
				if (getT_InvMatrix() != null) {
					communicationProtocol.writeT_invCSV(path, getT_InvMatrix(), getTransitions());
					if(!silence)
						JOptionPane.showMessageDialog(null,  "T-invariants saved to file:\n"+path,
							"Success",JOptionPane.INFORMATION_MESSAGE);
					result = 0;
				} else {
					if(!silence)
						JOptionPane.showMessageDialog(null, "There are no t-invariants to export.",
							"Warning",JOptionPane.WARNING_MESSAGE);
					overlord.log("No t-invariants, saving into CSV file failed.","error", true);
					//result = -1;
				}
			} else {
				if (getP_InvMatrix() != null) {
					communicationProtocol.writeP_invCSV(path, getP_InvMatrix(), getPlaces());
					if(!silence)
						JOptionPane.showMessageDialog(null,  "P-invariants saved to file:\n"+path,
							"Success",JOptionPane.INFORMATION_MESSAGE);
					
					result = 0;
				} else {
					if(!silence)
						JOptionPane.showMessageDialog(null, "There are no p-invariants to export.",
							"Warning",JOptionPane.WARNING_MESSAGE);
					overlord.log("No p-invariants, saving into CSV file failed.","error", true);
					//result = -1;
				}
			}
		} catch (Exception e) {
			overlord.log("Error: " + e.getMessage(), "error", true);
		}
		return result;
	}
	
	/**
	 * Metoda zapisująca inwarianty do pliku w formacie INA.
	 * @param path String - ścieżka do pliku zapisu
	 * @param t_inv boolean - true, jeśli chodzi o t-inwarianty
	 * @return int - 0 jeśli operacja się udała, -1 w przeciwnym wypadku
	 */
	public int saveInvariantsToInaFormat(String path, boolean t_inv) {
		int result = -1;
		try {
			if(t_inv) {
				if (getT_InvMatrix() != null) {
					communicationProtocol.writeT_invINA(path, getT_InvMatrix(), getTransitions());
					JOptionPane.showMessageDialog(null, "T-invariants saved to file:\n"+path,
							"Success", JOptionPane.INFORMATION_MESSAGE);
					overlord.log("T-invariants saved into .inv INA file.","text", true);
					result = 0;
				} else {
					JOptionPane.showMessageDialog(null, "There are no t-invariants to export",
							"Warning",JOptionPane.WARNING_MESSAGE);
					overlord.log("No t-invariants, saving into CSV file failed.","error", true);
					//result = -1;
				}
			} else {
				if (getP_InvMatrix() != null) {
					communicationProtocol.writeP_invINA(path, getP_InvMatrix(), getPlaces());
					JOptionPane.showMessageDialog(null, "P-invariants saved to file:\n"+path,
							"Success", JOptionPane.INFORMATION_MESSAGE);
					overlord.log("P-invariants saved into .inv INA file.","text", true);
					result = 0;
				} else {
					JOptionPane.showMessageDialog(null, "There are no p-invariants to export",
							"Warning",JOptionPane.WARNING_MESSAGE);
					overlord.log("No p-invariants, saving into CSV file failed.","error", true);
					//result = -1;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			overlord.log("Error: " + e.getMessage(), "error", true);
		}
		return result;
	}
	
	/**
	 * Metoda zapisująca inwarianty do pliku w formacie CSV.
	 * @param path String - ścieżka do pliku zapisu
	 * @param t_inv boolean - true, jeśli chodzi o t-inwarianty
	 * @return int - 0 jeśli operacja się udała, -1 w przeciwnym wypadku
	 */
	public int saveInvariantsToCharlie(String path, boolean t_inv) {
		int result = -1;
		try {
			if(t_inv) {
				if (getT_InvMatrix() != null) {
					communicationProtocol.writeT_invCharlie(path, getT_InvMatrix(), getTransitions());
					JOptionPane.showMessageDialog(null, "T-invariants saved to file:\n"+path,
							"Success",JOptionPane.INFORMATION_MESSAGE);
					overlord.log("T-invariants saved in Charlie file format.","text", true);
					result = 0;
				} else {
					JOptionPane.showMessageDialog(null, "There are no t-invariants to export.",
							"Warning",JOptionPane.WARNING_MESSAGE);
					overlord.log("No t-invariants, saving into CSV file failed.","error", true);
					//result = -1;
				}
			} else {
				if (getP_InvMatrix() != null) {
					communicationProtocol.writeP_invCharlie(path, getP_InvMatrix(), getPlaces());
					JOptionPane.showMessageDialog(null, "P-invariants saved to file:\n"+path,
							"Success",JOptionPane.INFORMATION_MESSAGE);
					overlord.log("P-invariants saved in Charlie file format.","text", true);
					result = 0;
				} else {
					JOptionPane.showMessageDialog(null, "There are no p-invariants to export.",
							"Warning",JOptionPane.WARNING_MESSAGE);
					overlord.log("No p-invariants, saving into CSV file failed.","error", true);
					//result = -1;
				}
			}
			
		} catch (Exception e) {
			//e.printStackTrace();
			overlord.log("Error: " + e.getMessage(), "error", true);
		}
		return result;
	}
	
	/**
	 * Metoda wczytująca plik inwariantów z pliku .inv (format INA) i zastępująca aktualnie
	 * wynegerowane w programie inwarianty jego zawartością (o ile został poprawnie wczytany).
	 * @param path String - ścieżka do pliku INA
	 * @param t_inv boolean - true, jeśli chodzi o t-inwarianty
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public boolean loadTPinvariantsFromFile(String path, boolean t_inv) {
		try {
			if(t_inv) {
				boolean status = communicationProtocol.readT_invariants(path);
				if(!status)
					return false;
				setT_InvMatrix(communicationProtocol.getInvariantsList(), true);
				overlord.reset.setT_invariantsStatus(true); //status t-inwariantów: wczytane
			} else {
				boolean status = communicationProtocol.readP_invariants(path);
				if(!status)
					return false;
				setP_InvMatrix(communicationProtocol.getInvariantsList());
				overlord.reset.setP_invariantsStatus(true); //status p-inwariantów: wczytane
			}
			
			return true;
		} catch (Exception e) {
			overlord.log("Invariants reading and/or adding to program failed.", "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda przerysowująca arkusze wyświetlające wszystkie części sieci.
	 */
	public void repaintAllGraphPanels() {
		for (GraphPanel g : this.getGraphPanels()) {
			g.invalidate();
			g.repaint();
		}
	}

	/**
	 * Metoda pozwala na zwiększenie kroku symulacji o 1. Jest ona wywoływana przez
	 * NetSimulator danego projektu, co powoduje wywołanie metody incrementSimulationStep() 
	 * dla każdego łuku zawartego w projekcie, odpowiedzialnego za wyświetlanie animacji 
	 * tokenów przemieszczających się w trakcie symulacji.
	 */
	public void incrementGraphicalSimulationStep() {
		for (Arc a : getArcs())
			a.incrementSimulationStep();
		for (GraphPanel g : this.getGraphPanels()) {
			g.invalidate();
			g.repaint();
		}
	}

	/**
	 * Metoda pozwala na pobranie stanu symulacji. W sytuacji gdy jest ona aktywna
	 * (isSimulationActive = true) wszelka interakcja z arkuszami oraz obiektami
	 * znajdującymi się na nich jest zablokowana.
	 * @return boolean - true w sytuacji gdy symulacja jest aktualnie aktywna;
	 * 		false w przypadku przeciwnym
	 */
	public boolean isSimulationActive() {
		return isSimulationActive;
	}

	/**
	 * Metoda pozwala na ustawienie stanu symulacji. W sytuacji gdy jest ona aktywna
	 * (isSimulationActive = true) wszelka interakcja z arkuszami oraz obiektami
	 * znajdującymi się na nich jest zablokowana.
	 * @param isSimulationActive boolean - nowy stan symulacji
	 */
	public void setSimulationActive(boolean isSimulationActive) {
		this.isSimulationActive = isSimulationActive;
		for (GraphPanel g : this.getGraphPanels())
			g.setSimulationActive(isSimulationActive);
	}

	/**
	 * Metoda zwracająca identyfikator czystego arkusza sieci. Jeśli już jest jakaś sieć - tworzy
	 * nową zakładkę i zwraca jej numer. 
	 * @return int - id arkusza sieci do zapełnienia
	 */
	public int returnCleanSheetID() {
		int SID = 0;

		if (!getDataCore().nodes.isEmpty()) { //zakładamy, że zawsze istnieje arkusz zerowy
			int tSID;
			ArrayList<Integer> sheetList = new ArrayList<>();

			for (WorkspaceSheet ws : overlord.getWorkspace().getSheets())
				sheetList.add(ws.getId());

			int[] tabSID = new int[overlord.getWorkspace().getSheets().get(overlord.getWorkspace().getSheets().size() - 1).getId() + 1];

			for (int j = 0; j < getDataCore().nodes.size(); j++) {
				for (int i = 0; i < getDataCore().nodes.get(j).getNodeLocations().size(); i++) {
					tSID = getDataCore().nodes.get(j).getNodeLocations().get(i).getSheetID();
					tabSID[tSID]++;
				}
			}
			boolean emptySheet = false;
			int emptySheetIndex = 999999999;
			for (Integer integer : sheetList) {
				if (tabSID[integer] == 0) {
					emptySheet = true;
					if (emptySheetIndex > integer) {
						emptySheetIndex = integer;
					}
				}
			}
			if (emptySheet) {
				SID = emptySheetIndex;
			} else {
				SID = overlord.getWorkspace().newTab(false, new Point(0,0), 1, MetaType.SUBNET);
			}
		}
		return SID;
	}

	/**
	 * Metoda ustawia podświetlanie zbiorów MCT.
	 * @param isGlowedMTC boolean - true jeśli MCT ma być podświetlony
	 */
	private void setTransitionGlowedMTC(boolean isGlowedMTC) {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.TRANSITION) {
				((Transition) n).drawGraphBoxT.setGlowed_MTC(isGlowedMTC);
			}
	}

	/**
	 * Metoda ustawia podświetlanie podsieci.
	 * @param isGlowedSubnet boolean - true jeśli MCT ma być podświetlony
	 */
	private void setGlowedSubnet(boolean isGlowedSubnet) {
		for (Node n : getNodes()){
				n.setGlowedSub(isGlowedSubnet);
			}
		for(Arc a : getArcs()){
			a.setGlowedSub(isGlowedSubnet);
		}
	}
	
	/**
	 * Resetuje kolory sieci do domyślnych.
	 */
	public void resetNetColors() {
		turnTransitionGlowingOff();
		setTransitionGlowedMTC(false);
		setGlowedSubnet(false);
		resetSimulationGraphics_Transitions();
		resetSimulationGraphics_Places();
		resetArcGraphics();
		resetNodes();
		//reset frame
	}
	
	/**
	 * Resetuje markery wyświetlania koloru każdego węzła i każdego element location
	 */
	private void resetNodes() {
		for (Node n : getNodes()) {
			n.qSimArcSign = false;
			for(ElementLocation el : n.getElementLocations()) {
				el.qSimArcSign = false;
				el.qSimDrawed = false;
			}
		}
	}

	/**
	 * Resetuje marker wyświetlania wzmocnionej grafiki rysowania łuku.
	 */
	private void resetArcGraphics() {
		for(Arc a : getArcs()) {
			a.arcQSimBox.qSimForcedArc = false;
			if (a.getType() == PetriNetElementType.ARC) {
				a.arcDecoBox.setColor(false, Color.BLACK);
			}

			a.arcXTPNbox.showQSimXTPN = false;
		}
	}

	/**
	 * Metoda wygasza kolorowanie tranzycji, zeruje dodatkowe wyświetlanie liczb czy tekstów.
	 */
	private void resetSimulationGraphics_Transitions() {
		for (Node n : getNodes()) {
			if (n.getType() == PetriNetElementType.TRANSITION) {
				Transition trans = ((Transition) n);
				trans.drawGraphBoxT.setColorWithNumber(false, Color.white, false, -1, false, "");
				trans.drawGraphBoxT.resetOffs();
				trans.qSimBoxT.qSimDrawed = false;
				trans.qSimBoxT.qSimDrawStats = false;
				trans.qSimBoxT.qSimOvalSize = 10;

				n.qSimArcSign = false;  //NODE level
			}

			if(n instanceof TransitionXTPN) {
				((TransitionXTPN)n).qSimXTPN.clean();
				((TransitionXTPN)n).showQSimXTPN = false;
			}
		}
	}
	
	/**
	 * Metoda wygasza kolorowanie miejsca, zeruje dodatkowe wyświetlanie liczb czy tekstów.
	 */
	private void resetSimulationGraphics_Places() {
		for (Node n : getNodes()) {
			if (n.getType() == PetriNetElementType.PLACE) {
				Place place = ((Place) n);
				place.drawGraphBoxP.setColorWithNumber(false, Color.white, false, -1, false, "");
				place.drawGraphBoxP.resetOffs();
				place.qSimBoxP.qSimDrawed = false;
				place.qSimBoxP.qSimDrawStats = false;
				place.qSimBoxP.qSimOvalSize = 10;

				n.qSimArcSign = false; //NODE level
			}

			if(n instanceof PlaceXTPN) {
				((PlaceXTPN)n).showQSimXTPN = false;
			}
		}
	}
	
	/**
	 * Metoda sprawdza czy istnieje już rysowana wcześniej sieć.
	 * @return boolean - false, jeśli nie należy kontynuować
	 */
	@SuppressWarnings("unused")
	private boolean checkIfEmpty() {
		//TODO: UNUSED
		if(getNodes().size() == 0) {
			return true;
		} else {
			Object[] options = {"Load and replace project", "Cancel operation",};
			int n = JOptionPane.showOptionDialog(null,
							"New net will replace an old one already drawn. If the latter has not been saved,\n"
							+ "please do it before continuing or it will be lost.",
							"Continue loading new net?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			return n == 0;
		}
	}
	
	/**
	 * Metoda rozpoczynająca proces symulatora wykonań inwariantów.
	 * @param type int - typ symulacji: 0 - zwykła, 1 - czasowa
	 * @param value int
	 */
	public void startInvSim(int type, int value)  {
		//Timer timer = new Timer();
		//Date date = new Date();
		//this.invSimulator = new InvariantsSimulator(abyss.analyzer.InvariantsSimulator.NetType.BASIC, new PetriNet(
		//				getData().nodes, getData().arcs), get2ndFormInvariantsList(),type, value);
		//invSimulator.startSimulation(SimulatorMode.LOOP);
	}
	
	/**
	 * UNUSED, OBSOLETE, MORE: DOES NOT WORK, ONLY MESSES UP
	 * FOR EDUCATIONAL (EXAMPLE TYPE: "DON'T DO IT") PURPOSES
	 */
	public void deleteProjectData() {
		overlord.log("Net data deletion initiated.", "text", true);
		for (GraphPanel gp : getGraphPanels()) {
			gp.getSelectionManager().forceDeselectAllElements();
		}
		setNodes(new ArrayList<>());
		setArcs(new ArrayList<>());
		repaintAllGraphPanels();
		ArrayList<GraphPanel> newGraphPanels = new ArrayList<>();
		for (GraphPanel gp : getGraphPanels()) {
			int sheetID = gp.getSheetId();
			WorkspaceSheet.SheetPanel sheetPanel = (WorkspaceSheet.SheetPanel) gp.getParent();
			sheetPanel.remove(gp);
			GraphPanel newGraphPanel = new GraphPanel(sheetID, this,getNodes(), getArcs());
			sheetPanel.add(newGraphPanel);
			newGraphPanels.add(newGraphPanel);
		}
		setGraphPanels(newGraphPanels);
		repaintAllGraphPanels();
	}

	/**
	 * Metoda zwraca typ sieci po nazwie.
	 * @param name (<b>String</b>) nazwa typu.
	 * @return (<b>GlobalNetType</b>) enum typ sieci.
	 */
	public GlobalNetType getNetTypeByName(String name) {
		return switch (name) {
			case "TPN" -> GlobalNetType.TPN;
			case "PN_extArcs" -> GlobalNetType.PN_extArcs;
			case "FPN" -> GlobalNetType.FPN;
			case "timeFPN" -> GlobalNetType.timeFPN;
			case "TPN_extArcs" -> GlobalNetType.TPN_extArcs;
			case "FPN_extArcs" -> GlobalNetType.FPN_extArcs;
			case "timeFPN_extArcs" -> GlobalNetType.timeFPN_extArcs;
			case "SPN" -> GlobalNetType.SPN;
			case "functionalSPN" -> GlobalNetType.functionalSPN;
			case "XTPN" -> GlobalNetType.XTPN;
			default -> GlobalNetType.PN;
		};
	}

	/**
	 * Ustawia główny typ sieci. Także w obiekcie kontrolera.
	 * @param projectType (<b>GlobalNetType</b>) - PN, timePN, <b>ext</b>PN, funcPN, timeFuncPN, time<b>Ext</b>PN, func<b>Ext</b>PN, timeFunc<b>Ext</b>PN, stochasticPN, stochasticFuncPN, XTPN
	 */
	public void setProjectType(GlobalNetType projectType) {
		this.projectType = projectType;
		GUIController.access().setCurrentNetType(projectType);
	}

	/**
	 * Zwraca typ sieci. Używać tylko dla zapisu lub odczytu. Poza tym używać kontrolera do ustalenia, jaki jest główny AKTYWNY typ sieci programu.
	 * @return (<b>GlobalNetType</b>) - np. PN, timePN, <b>ext</b>PN, funcPN, timeFuncPN, time<b>Ext</b>PN, func<b>Ext</b>PN, timeFunc<b>Ext</b>PN, stochasticPN, stochasticFuncPN, XTPN
	 */
	public GlobalNetType getProjectType() {
		return projectType;
	}


	/**
	 * Zwraca podtyp sieci.
	 * @return (<b>getProjectSubType</b>) CLEAN, FUNCTIONAL, EXT_ARCS
	 */
	public GlobalNetSubType getProjectSubType() {
		return projectSubType;
	}

	/**
	 * Ustawia podtyp sieci.
	 * @param projectSubType (<b>getProjectSubType</b>) CLEAN, FUNCTIONAL, EXT_ARCS
	 */
	public void setProjectSubType(GlobalNetSubType projectSubType) {
		this.projectSubType = projectSubType;
	}

	/**
	 * Metoda sprawdza, czy sieć zawiera węzły inne niż XTPN.
	 * @return (<b>boolean</b>) true, jeżeli są inne niż XTPN
	 */
	public boolean hasNonXTPNnodes() {
		boolean result = false;
		for (Node n : this.dataCore.nodes) {
			if( (n instanceof Place) && !(n instanceof PlaceXTPN)) {
				return true;
			}
			if( (n instanceof Transition) && !(n instanceof TransitionXTPN)) {
				return true;
			}
		}
		return result;
	}

	/**
	 * Metoda sprawdza, czy istnieją elementy XTPN.
	 * @return (<b>boolean</b>) true, jeżeli są węzły XTPN.
	 */
	public boolean checkIfXTPNpresent() {
		boolean result = false;
		for (Node n : this.dataCore.nodes) {
			if (n instanceof PlaceXTPN) {
				return true; //zawiera node XTPN
			} else if (n instanceof TransitionXTPN){
				return true; //zawiera node XTPN
			}
		}
		return result;
	}

	/**
	 * Zmienia wszystkie non-XTPN nodes w XTPN
	 */
	public void transformAllIntoXTPNnodes_TODO() { //TODO
		for (Node n : this.dataCore.nodes) {
			if (n instanceof Place) {
				Place place = ((Place) n);
				//place.setXTPNplaceStatus(true);
				//place.setGammaMin_xTPN(99, true);
				//place.setGammaMax_xTPN(99, true);
				//place.setGammaModeXTPNstatus(false); //!! tak! można ręcznie każde miejsce potem zmienić
			} else if (n instanceof Transition){
				Transition transition = ((Transition) n);
				//transition.setXTPNstatus(true);
				transition.setTransType(TransitionType.XTPN);
				transition.timeExtension.setTPNstatus(false);
				transition.timeExtension.setDPNstatus(false);
				transition.spnExtension.setSPNtype(TransitionSPNExtension.StochaticsType.NONE);
			}
		}
	}
}
