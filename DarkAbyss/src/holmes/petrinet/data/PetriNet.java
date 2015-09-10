package holmes.petrinet.data;

import holmes.analyse.MCTCalculator;
import holmes.darkgui.GUIManager;
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
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.simulators.NetSimulator;
import holmes.petrinet.simulators.NetSimulator.NetType;
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

@Root
/**
 * Klasa przechowująca listy wszystkich obiektów projektu oraz arkusze. Dodatkowo agreguje
 * metody działające na sieci - zapis, odczyt, generowanie MCT, klastrów, itd.
 * @author students - pierwsza wersja
 * @author MR - dodatkowe metody
 *
 */
public class PetriNet implements SelectionActionListener, Cloneable {
	private ArrayList<SelectionActionListener> actionListeners = new ArrayList<SelectionActionListener>();
	private ArrayList<ArrayList<Integer>> invariantsMatrix; //macierz inwariantów
	private ArrayList<String> invariantsDescriptions;
	private ArrayList<ArrayList<Transition>> mctData;
	private ArrayList<Integer> transitionMCTnumber;
	private ArrayList<String> mctNames;
	private NetSimulationDataCore simData;
	private MCSDataMatrix mcsData;
	private StatesManager statesManager;
	private FiringRatesManager firingRatesManager;
	
	private String lastFileName = "";
	private PetriNetData dataCore = new PetriNetData(new ArrayList<Node>(), new ArrayList<Arc>(), "default");
	
	private ArrayList<GraphPanel> graphPanels;
	
	private AbyssWriter ABYSSSwriter;
	private AbyssReader ABYSSReader;
	private IOprotocols communicationProtocol;
	private NetHandler handler;
	private SAXParserFactory readerSNOOPY;
	private Workspace workspace;
	private DrawModes drawMode = DrawModes.POINTER;
	private NetSimulator simulator;
	private MCTCalculator analyzer;
	private PetriNetMethods methods;
	private boolean isSimulationActive = false;
	
	public boolean anythingChanged = false;
	public GUIManager overlord;
	
	/**
	 * Konstruktor obiektu klasy PetriNet - działa dla symulatora inwariantów.
	 * @param nod ArrayList[Node] - lista wierzchołków sieci
	 * @param ar ArrayList[Arc] - lista łuków sieci
	 */
	public PetriNet(ArrayList<Node> nod, ArrayList<Arc> ar) {
		overlord = GUIManager.getDefaultGUIManager();
		getDataCore().nodes = nod;
		getDataCore().arcs = ar;
		this.communicationProtocol = new IOprotocols();
		this.dataCore.netName = "default";
		this.mcsData = new MCSDataMatrix();
		this.methods = new PetriNetMethods(this);
		this.simData = new NetSimulationDataCore();
		this.statesManager = new StatesManager(this);
	}

	/**
	 * Konstruktor obiektu klasy PetriNet - główny konstruktor dla workspace.
	 * @param workspace Workspace - obiekt obszaru roboczego dla sieci
	 */
	public PetriNet(Workspace workspace, String name) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.setGraphPanels(new ArrayList<GraphPanel>());
		this.workspace = workspace;
		this.setSimulator(new NetSimulator(NetType.BASIC, this));
		this.setMCTanalyzer(new MCTCalculator(this));
		this.statesManager = new StatesManager(this);
		this.firingRatesManager = new FiringRatesManager(this);
		resetComm();
		this.dataCore.netName = name;
		this.mcsData = new MCSDataMatrix();
		this.methods = new PetriNetMethods(this);
		this.simData = new NetSimulationDataCore();
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
		ArrayList<Place> returnPlaces = new ArrayList<Place>();
		for (Node n : this.dataCore.nodes) {
			if (n instanceof Place)
				returnPlaces.add((Place) n);
		}
		return returnPlaces;
	}

	/**
	 * Metoda pozwala pobrać wszystkie obiekty tranzycji dla danej sieci.
	 * Wszystkie wierzchołki w projekcie są przechowywane w obrębie jednej
	 * listy ArrayList[Node], jednak dzięki tej metodzie można ją przefiltrować.
	 * @return ArrayList[Transition] - lista tranzycji projektu sieci
	 */
	public ArrayList<Transition> getTransitions() {
		ArrayList<Transition> returnTransitions = new ArrayList<Transition>();
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
		ArrayList<Transition> returnTransitions = new ArrayList<Transition>();
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
		ArrayList<Node> places = new ArrayList<Node>();
		ArrayList<Node> transitions = new ArrayList<Node>();
		ArrayList<Node> timeTransitions = new ArrayList<Node>();
		ArrayList<Node> metaNodes = new ArrayList<Node>();
		
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
		ArrayList<MetaNode> returnNodes = new ArrayList<MetaNode>();
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
	public void addArcsAndNodes(ArrayList<Arc> arcs, ArrayList<Node> nodes) {
		this.getArcs().addAll(arcs);
		this.getNodes().addAll(nodes);
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
	public DrawModes getDrawMode() {
		return this.drawMode;
	}
	
	/**
	 * Metoda zwracająca handler oczytu pliku sieci.
	 * @return NetHandler - obiekt dla parsera w czasie czytania pliku
	 */
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
	 * Metoda pozwala na pobrania aktualnego obiektu symulatora Netsimulator
	 * dla bieżącego projektu
	 * @return NetSimulator - symulator
	 */
	public NetSimulator getSimulator() {
		return simulator;
	}
	
	/**
	 * Metoda pozwala na ustawienie symulatora NetSimulator dla bieżącego projektu
	 * @param simulator NetSimulator - nowy symulator
	 */
	public void setSimulator(NetSimulator simulator) {
		this.simulator = simulator;
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
	public void setGraphPanels(ArrayList<GraphPanel> graphPanels) {
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
	 * Metoda ustawia nową macierz inwariantów sieci.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 * @param generateMCT boolean - true, jeśli mają być wygenerowane zbiory MCT 
	 */
	public void setINVmatrix(ArrayList<ArrayList<Integer>> invariants, boolean generateMCT) {
		this.invariantsMatrix = invariants;
		this.invariantsDescriptions = null;
		
		if(invariants == null)
			return;
		else 
			this.invariantsDescriptions = new ArrayList<String>();
		
		for(int i=0; i<invariantsMatrix.size(); i++) {
			invariantsDescriptions.add("Default description of invariant #"+(i+1));
		}
		
		if(generateMCT) {
			MCTCalculator analyzer = getWorkspace().getProject().getMCTanalyzer();
			ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
			getWorkspace().getProject().setMCTMatrix(mct, true);
			overlord.getMctBox().showMCT(mct);
		}
	}
	
	/**
	 * Metoda ustawia nowy wektor opisów dla inwariantów.
	 * @param namesVector ArrayList[String] - nazwy inwariantów
	 */
	public void setINVdescriptions(ArrayList<String> namesVector) {
		if(invariantsMatrix == null)
			return;
		if(namesVector.size() == invariantsMatrix.size())
			this.invariantsDescriptions = namesVector;
	}
	
	/**
	 * Zwraca opis inwariantu o podanym numerze.
	 * @param index int - nr inwariantu, od zera
	 * @return String - nazwa inwariantu
	 */
	public String getINVdescription(int index) {
		if(invariantsMatrix == null)
			return "";
		
		if(index < invariantsDescriptions.size() && index >= 0)
			return this.invariantsDescriptions.get(index);
		else
			return "";
	}

	/**
	 * Metoda zwraca macierz inwariantów sieci.
	 * @return ArrayList[ArrayList[Integer]] - macierz inwariantów
	 */
	public ArrayList<ArrayList<Integer>> getINVmatrix() {
		return invariantsMatrix;
	}
	
	/**
	 * Metoda pozwala na dostęp do wektora nazw inwariantów.
	 * @return ArrayList[String] - nazwy inwariantów
	 */
	public ArrayList<String> accessINVdescriptions() {
		return invariantsDescriptions;
	}
	
	/**
	 * Metoda ustawia nową macierz zbiorów MCT.
	 * @param mct ArrayList[ArrayList[Transition]] - macierz MCT
	 * @param sort boolean - true, jeśli mają być posortowane
	 */
	public void setMCTMatrix(ArrayList<ArrayList<Transition>> mct, boolean sort) {
		this.mctData = mct;
		this.mctNames = new ArrayList<String>();
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
	 * @return
	 */
	public ArrayList<String> accessMCTnames() {
		return mctNames;
	}
	
	/**
	 * Metoda ustawia nowy wektor nazw dla zbiorów MCT.
	 * @param namesVector ArrayList[String] - nazwy MCT
	 */
	public void setMCTNames(ArrayList<String> namesVector) {
		if(mctData == null)
			return;
		if(namesVector.size() == mctData.size())
			this.mctNames = namesVector;
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
	public boolean isSheetEmpty(int sheetID) {
		boolean result = true;
		for (Node node : getNodes())
			for (ElementLocation location : node.getNodeLocations())
				if (location.getSheetID() == sheetID)
					result = false;
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
	 * @return StatesManager - obiekt managera
	 */
	public StatesManager accessStatesManager() {
		return this.statesManager;
	}
	
	public void replaceStatesManager(StatesManager newStatesMngr) {
		statesManager = newStatesMngr;
	}
	
	/**
	 * Umożliwia dostęp do managera odpaleń tranzycji SPN sieci.
	 * @return FiringRatesManager - obiekt managera fr
	 */
	public FiringRatesManager accessFiringRatesManager() {
		return this.firingRatesManager;
	}
	
	public void replaceStatesManager(FiringRatesManager newStatesMngr) {
		this.firingRatesManager = newStatesMngr;
	}
	
	//*********************************************************************************
	
	/**
	 * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji. Liczba tokenów jest przywracana
	 * z aktualnie wskazanego stanu w managerze stanów, tranzycje są resetowane wewnętrznie.
	 */
	public void restoreMarkingZero() {
		try {
			ArrayList<Transition> transitions = getTransitions();
			accessStatesManager().restoreSelectedState();
	
			for(int i=0; i<transitions.size(); i++) {
				Transition trans = transitions.get(i);
				trans.setLaunching(false);
				
				if(trans.getTransType() == TransitionType.TPN) {
					trans.resetTimeVariables();
				}
			}
			
			NetType nt = overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimNetType();
			setSimulator(new NetSimulator(nt, this));
			overlord.getSimulatorBox().getCurrentDockWindow().setSimulator(getSimulator());
			overlord.io.updateTimeStep(""+getSimulator().getSimulatorTimeStep());
			overlord.simSettings.currentStep = getSimulator().getSimulatorTimeStep();
			
			repaintAllGraphPanels();
			getSimulator().getSimLogger().logSimReset();
		} catch (Exception e) {
			overlord.log("Unknown error: unable to restore state m0 on request.", "error", true);
		}
	}
	
	/**
	 * Szybsza wersja przywracania stanu sieci, bez zawracania głowy okna SimulatorBox j.w.
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 */
	public void restoreMarkingZeroFast(ArrayList<Transition> transitions) {
		try {
			accessStatesManager().restoreSelectedState();
	
			for(int i=0; i<transitions.size(); i++) {
				Transition trans = transitions.get(i);
				trans.setLaunching(false);
				if(trans.getTransType() == TransitionType.TPN) {
					trans.resetTimeVariables();
				}
			}
			getSimulator().getSimLogger().logSimReset();
		} catch (Exception e) {
			overlord.log("Unknown error: unable to restore state m0 on request.", "error", true);
		}
	}
	
	/**
	 * Metoda wyłączająca świecenie tranzycji np. w ramach aktywacji niezmiennika.
	 */
	public void turnTransitionGlowingOff() {
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
	 * @return boolean - true w sytuacji powodzenia operacji GraphPanel od podanym identyfikatorze istniał
	 *		false w przypadku przeciwnym
	 */
	public boolean removeGraphPanel(int sheetID) {
		System.out.println("Przed usunieciem:");
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
					for (Iterator<ElementLocation> elIterator = n.getNodeLocations(sheetID).iterator(); elIterator.hasNext();) {
						ElementLocation el = elIterator.next();
						if (!n.removeElementLocation(el)) {
							nodeIterator.remove();
						}
						for (Iterator<Arc> j = el.getInArcs().iterator(); j.hasNext();) {
							this.getArcs().remove(j.next());
							j.remove();
						}
						// deletes all out arcs of current ElementLocation
						for (Iterator<Arc> j = el.getOutArcs().iterator(); j.hasNext();) {
							this.getArcs().remove(j.next());
							j.remove();
						}
					}
				}
				System.out.println("Po usunieciu:");
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
			if (n.getName() == name)
				return true;
		for (Arc a : this.getArcs())
			if (a.getName() == name)
				return true;
		return false;
	}

	/**
	 * Metoda zwraca listę obrazów utworzonych ze wszystkich istniejących arkuszy.
	 * @return ArrayList[BufferedImage] - lista obrazów
	 */
	public ArrayList<BufferedImage> getImagesFromGraphPanels() {
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
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
		boolean status = communicationProtocol.writePNT(filePath, getPlaces(), getTransitions(), getArcs());
		return status;
	}
	
	/**
	 * Metoda pozwala na zapis całej sieci do pliku projektu.
	 * @param filePath String - ścieżka do pliku zapisu
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean saveAsAbyss(String filePath) {
		ABYSSSwriter = new AbyssWriter();
		boolean status = ABYSSSwriter.write(filePath);
		return status;
	}
	
	/**
	 * Metoda pozwala zapisać sieć do formatu SPPED programu Snoopy.
	 * @param filePath String - ścieżka docelowa pliku
	 * @return boolean - status operacji: true jeśli nie było błędów
	 */
	public boolean saveAsSPPED(String filePath) {
		SnoopyWriter sWr = new SnoopyWriter();
		boolean status = sWr.writeSPPED(filePath);
		return status;
	}
	
	/**
	 * Metoda pozwala zapisać sieć do formatu SPEPT (Extended) programu Snoopy.
	 * @param filePath String - ścieżka docelowa pliku
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean saveAsSPEPT(String filePath) {
		SnoopyWriter sWr = new SnoopyWriter();
		boolean status = sWr.writeSPEPT(filePath);
		return status;
	}

	/**
	 * Metoda pozwala na odczyt całej sieci z pliku podanego w parametrze metody.
	 * @param path String - ścieżka do pliku odczytu
	 */
	public boolean loadFromFile(String path) {
		boolean status = overlord.reset.newProjectInitiated();
		if(status == false) {
			return false;
		}
		
		readerSNOOPY = SAXParserFactory.newInstance();
		try {
			// Format własny
			if (path.endsWith(".abyss")) {
				ABYSSReader = new AbyssReader();
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
			if (path.endsWith(".spped") || path.endsWith(".spept") || path.endsWith(".colpn") || path.endsWith(".sptpt")) {
				if(overlord.getSettingsManager().getValue("programUseOldSnoopyLoaders").equals("1")) {
					overlord.log("Activating old Snoopy loader. Will fail for hierarchical networks.", "text", true);
					InputStream xmlInput = new FileInputStream(path);
					SAXParser saxParser = readerSNOOPY.newSAXParser();
					if (path.endsWith(".spped")) {
						handler = new NetHandler_Classic();
					}
					if (path.endsWith(".spept")) {
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
					accessStatesManager().createCleanState();
					accessFiringRatesManager().createCleanFRVector();
					
					String name = path;
					int ind = name.lastIndexOf("\\");
					if(ind > 1)
						name = name.substring(ind+1);
					
					setFileName(name);
					name = name.replace(".spped", "");
					name = name.replace(".spept", "");
					name = name.replace(".colpn", "");
					name = name.replace(".sptpt", "");
					setName(name);
					
				} else { //Holmes project
					SnoopyReader reader = new SnoopyReader(0, path);
					addArcsAndNodes(reader.getArcList(), reader.getNodesList());
					accessStatesManager().createCleanState();
					accessFiringRatesManager().createCleanFRVector();
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
				accessStatesManager().createCleanState();
				accessFiringRatesManager().createCleanFRVector();
			}
			
			overlord.log("Petri net successfully imported from file "+path, "text", true);

			ArrayList<Place> places = getPlaces();
			ArrayList<Arc> arcs = getArcs();
			ArrayList<Transition> transitions = getTransitions();
			for(Transition transition : transitions) { //aktywacja wektorów funkcji
				transition.checkFunctions(arcs, places);
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
	 * @return int - 0 jeśli operacja się udała, -1 w przeciwnym wypadku
	 */
	public int saveInvariantsToCSV(String path, boolean silence) {
		int result = -1;
		try {
			if (getINVmatrix() != null) {
				communicationProtocol.writeInvToCSV(path, getINVmatrix(), getTransitions());
				//GUIManager.getDefaultGUIManager().log("Invariants saved as CSV file.","text", true);
				if(!silence)
					JOptionPane.showMessageDialog(null,  "Invariants saved to file:\n"+path,
						"Success",JOptionPane.INFORMATION_MESSAGE);
				
				result = 0;
			} else {
				if(!silence)
					JOptionPane.showMessageDialog(null, "There are no invariants to export.",
						"Warning",JOptionPane.WARNING_MESSAGE);
				overlord.log("No invariants, saving into CSV file failed.","error", true);
				result = -1;
			}
		} catch (Throwable err) {
			err.printStackTrace();
			overlord.log("Error: " + err.getMessage(), "error", true);
		}
		return result;
	}
	
	/**
	 * Metoda zapisująca inwarianty do pliku w formacie INA.
	 * @param path String - ścieżka do pliku zapisu
	 * @return int - 0 jeśli operacja się udała, -1 w przeciwnym wypadku
	 */
	public int saveInvariantsToInaFormat(String path) {
		int result = -1;
		try {
			if (getINVmatrix() != null) {
				communicationProtocol.writeINV(path, getINVmatrix(), getTransitions());
				JOptionPane.showMessageDialog(null,
						"Invariants saved to file:\n"+path,
						"Success",JOptionPane.INFORMATION_MESSAGE);
				overlord.log("Invariants saved into .inv INA file.","text", true);
				result = 0;
			} else {
				JOptionPane.showMessageDialog(null,
						"There are no invariants to export",
						"Warning",JOptionPane.WARNING_MESSAGE);
				overlord.log("No invariants, saving into CSV file failed.","error", true);
				result = -1;
			}
		} catch (Throwable err) {
			err.printStackTrace();
			overlord.log("Error: " + err.getMessage(), "error", true);
		}
		return result;
	}
	
	/**
	 * Metoda zapisująca inwarianty do pliku w formacie CSV.
	 * @param path String - ścieżka do pliku zapisu
	 * @return int - 0 jeśli operacja się udała, -1 w przeciwnym wypadku
	 */
	public int saveInvariantsToCharlie(String path) {
		int result = -1;
		try {
			if (getINVmatrix() != null) {
				communicationProtocol.writeCharlieInv(path, getINVmatrix(), getTransitions());
				JOptionPane.showMessageDialog(null, "Invariants saved to file:\n"+path,
						"Success",JOptionPane.INFORMATION_MESSAGE);
				overlord.log("Invariants saved in Charlie file format.","text", true);
				result = 0;
			} else {
				JOptionPane.showMessageDialog(null, "There are no invariants to export.",
						"Warning",JOptionPane.WARNING_MESSAGE);
				overlord.log("No invariants, saving into CSV file failed.","error", true);
				result = -1;
			}
		} catch (Throwable err) {
			err.printStackTrace();
			overlord.log("Error: " + err.getMessage(), "error", true);
		}
		return result;
	}
	
	/**
	 * Metoda wczytująca plik inwariantów z pliku .inv (format INA)
	 * @param sciezka String - ścieżka do pliku INA
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public boolean loadInvariantsFromFile(String sciezka) {
		try {
			boolean status = communicationProtocol.readINV(sciezka);
			if(status == false)
				return false;
			
			setINVmatrix(communicationProtocol.getInvariantsList(), true);
			overlord.reset.setInvariantsStatus(true); //status inwariantów: wczytane
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
	 * Metoda pozwala na zwiększenie kroku symulacji o 1. Jest ona wywoływana przez
	 * NetSimulator danego projektu, co powoduje wywołanie metody incrementSimulationStep() 
	 * dla każdego łuku zawartego w projekcie, odpowiedzialnego za wyświetlanie animacji 
	 * tokenów przemieszczających się w trakcie symulacji.
	 */
	public void incrementSimulationStep() {
		for (Arc a : getArcs())
			a.incrementSimulationStep();
		for (GraphPanel g : this.getGraphPanels()) {
			g.invalidate();
			g.repaint();
		}
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

		if (getDataCore().nodes.isEmpty()) { //zakładamy, że zawsze istnieje arkusz zerowy
			SID = 0;
		} else {
			int tSID;
			ArrayList<Integer> sheetList = new ArrayList<Integer>();
			int k = 0;
			for (k = 0; k < overlord.getWorkspace().getSheets().size(); k++)
				sheetList.add(overlord.getWorkspace().getSheets().get(k).getId());

			int[] tabSID = new int[overlord.getWorkspace().getSheets().get(k - 1).getId() + 1];

			for (int j = 0; j < getDataCore().nodes.size(); j++) {
				for (int i = 0; i < getDataCore().nodes.get(j).getNodeLocations().size(); i++) {
					tSID = getDataCore().nodes.get(j).getNodeLocations().get(i).getSheetID();
					tabSID[tSID]++;
				}
			}
			boolean emptySheet = false;
			int emptySheetIndex = 999999999;
			for (int l = 0; l < sheetList.size(); l++) {
				if (tabSID[sheetList.get(l)] == 0) {
					emptySheet = true;
					if (emptySheetIndex > sheetList.get(l)) {
						emptySheetIndex = sheetList.get(l);
					}
				}
			}
			if (emptySheet == true) {
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
	public void setTransitionGlowedMTC(boolean isGlowedMTC) {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.TRANSITION) {
				((Transition) n).setGlowed_MTC(isGlowedMTC);
			}
	}
	
	/**
	 * Metoda czyści kolory sieci do domyślnych.
	 */
	public void resetNetColors() {
		turnTransitionGlowingOff();
		setTransitionGlowedMTC(false);
		resetTransitionGraphics();
		resetPlaceGraphics();
	}
	
	/**
	 * Metoda wygasza kolorowanie tranzycji, zeruje dodatkowe wyświetlanie liczb czy tekstów.
	 */
	public void resetTransitionGraphics() {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.TRANSITION) {
				Transition trans = ((Transition) n);
				trans.setColorWithNumber(false, Color.white, false, -1, false, "");
				trans.resetOffs();
			}
	}
	
	/**
	 * Metoda wygasza kolorowanie miejsca, zeruje dodatkowe wyświetlanie liczb czy tekstów.
	 */
	public void resetPlaceGraphics() {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.PLACE) {
				Place place = ((Place) n);
				place.setColorWithNumber(false, Color.white, false, -1, false, "");
				place.resetOffs();
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
			if (n == 0) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Metoda rozpoczynająca proces symulatora wykonać inwariantów.
	 * @param type int - typ symulacji: 0 - zwykła, 1 - czasowa
	 * @param value - 
	 * @throws CloneNotSupportedException
	 */
	public void startInvSim(int type, int value) throws CloneNotSupportedException {
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
		
		setNodes(new ArrayList<Node>());
		setArcs(new ArrayList<Arc>());
		
		repaintAllGraphPanels();
		
		ArrayList<GraphPanel> newGraphPanels = new ArrayList<GraphPanel>();
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
}
