package abyss.math;

import abyss.analyzer.EarlyInvariantsAnalyzer;
import abyss.analyzer.DarkAnalyzer;
import abyss.analyzer.InvariantsSimulator;
import abyss.analyzer.InvariantsSimulator.SimulatorMode;
import abyss.darkgui.GUIManager;
import abyss.files.Snoopy.SnoopyWriter;
import abyss.files.io.AbyssReader;
import abyss.files.io.AbyssWriter;
import abyss.files.io.IOprotocols;
import abyss.files.io.NetHandler;
import abyss.files.io.NetHandler_Classic;
import abyss.files.io.NetHandler_Colored;
import abyss.files.io.NetHandler_Extended;
import abyss.files.io.NetHandler_Time;
import abyss.graphpanel.SelectionActionListener;
import abyss.graphpanel.GraphPanel;
import abyss.graphpanel.IdGenerator;
import abyss.graphpanel.GraphPanel.DrawModes;
import abyss.math.Node;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.NetType;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import abyss.workspace.Workspace;
import abyss.workspace.WorkspaceSheet;

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
	private ArrayList<ArrayList<InvariantTransition>> invariants2ndForm = new ArrayList<ArrayList<InvariantTransition>>();
	private ArrayList<ArrayList<Integer>> invariantsMatrix; //macierz inwariantów
	private ArrayList<GraphPanel> graphPanels;		// panele sieci
	private PetriNetData dataCore = new PetriNetData(new ArrayList<Node>(), new ArrayList<Arc>(), "default");
	private IdGenerator idGenerator;
	
	private AbyssWriter ABYSSwriter;
	private AbyssReader ABYSSReader;
	private IOprotocols communicationProtocol;
	private NetHandler handler;
	private SAXParserFactory readerSNOOPY;
	
	private Workspace workspace;
	private DrawModes drawMode = DrawModes.POINTER;
	
	private boolean isSimulationActive = false;
	private NetSimulator simulator;
	private InvariantsSimulator invSimulator;
	private DarkAnalyzer analyzer;
	private EarlyInvariantsAnalyzer eia;

	//wektor tokenów dla miejsc:
	private ArrayList<Integer> backupMarkingZero = new ArrayList<Integer>();
	/** Wartość flagi == true jeżeli został już utworzony backup PRZEZ symulator */
	public boolean isBackup = false;
	
	/**
	 * Konstruktor obiektu klasy PetriNet - działa dla symulatora inwariantów.
	 * @param nod ArrayList[Node] - lista wierzchołków sieci
	 * @param ar ArrayList[Arc] - lista łuków sieci
	 */
	public PetriNet(ArrayList<Node> nod, ArrayList<Arc> ar) {
		getData().nodes = nod;
		getData().arcs = ar;
		communicationProtocol = new IOprotocols();
		dataCore.netName = "default";
	}

	/**
	 * Konstruktor obiektu klasy PetriNet - główny konstruktor dla workspace.
	 * @param workspace Workspace - obiekt obszaru roboczego dla sieci
	 */
	public PetriNet(Workspace workspace, String name) {
		this.setGraphPanels(new ArrayList<GraphPanel>());
		this.workspace = workspace;
		this.setSimulator(new NetSimulator(NetType.BASIC, this));
		this.setAnalyzer(new DarkAnalyzer(this));
		communicationProtocol = new IOprotocols();
		dataCore.netName = name;
	}

	/**
	 * Metoda pozwala pobrać wszystkie obiekty miejsc dla danej sieci.
	 * Wszystkie wierzchołki w projekcie są przechowywane w obrębie jednej
	 * listy ArrayList, jednak dzięki tej metodzie można ją przefiltrować.
	 * @return ArrayList[Place] - lista miejsc projektu sieci
	 */
	public ArrayList<Place> getPlaces() {
		ArrayList<Place> returnPlaces = new ArrayList<Place>();
		for (Node n : this.getData().nodes) {
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
		for (Node n : this.getData().nodes) {
			if (n instanceof Transition)
				returnTransitions.add((Transition) n);
		}
		return returnTransitions;
	}

	/**
	 * Metoda pozwala ustawić listę wszystkich wierzchołków zawartych w projekcie.
	 * Zmiana ta zostaje automatycznie rozpropagowana do wszystkich arkuszy
	 * GraphPanel przechowywanych w projekcie.
	 * @param nodes ArrayList[Node] - nowa lista wierzchołków
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.getData().nodes = nodes;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setNodes(nodes);
	}
	
	/**
	 * Metoda ustawia nazwę dla sieci Petriego.
	 * @param name String - nowa nazwa
	 */
	public void setName(String name) {
		dataCore.netName = name;
	}
	
	/**
	 * Metoda zwraca nazwę sieci petriego.
	 * @return String - nazwa sieci
	 */
	public String getName() {
		return dataCore.netName;
	}

	/**
	 * Metoda pozwala pobrać wszystkie łuki dla danej sieci.
	 * @return ArrayList[Arc] - lista łuków zawartych w projekcie
	 */
	public ArrayList<Arc> getArcs() {
		return this.getData().arcs;
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
		this.getData().arcs = arcs;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setArcs(this.getData().arcs);
	}

	/**
	 * Metoda pozwala pobrać listę wszystkich wierzchołków Node zawartych w projekcie.
	 * @return ArrayList[Node] - lista wierzchołków sieci
	 */
	public ArrayList<Node> getNodes() {
		return this.getData().nodes;
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
	 * Metoda zwraca numer generatora identyfikatorów
	 * @return IdGenerator - obiekt generatora
	 */
	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	/**
	 * Metoda ustawia nowy generator identyfikatorów elementów sieci.
	 * @param idGenerator IdGenerator - nowy generator
	 */
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
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
	 * Metoda zwraca obiekt symulatora inwariantów w ramach sieci.
	 * @return InvariantsSimulator - symulator inwariantów
	 */
	public InvariantsSimulator getInvSimulator() {
		return invSimulator;
	}

	/**
	 * Metoda zwracająca obiekt analizatora, np. na potrzeby generacji MCT.
	 * @return DarkAnalyzer - obiekt analizatora
	 */
	public DarkAnalyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * Metoda ustawiająca aktualny analizator dla obiektu sieci.
	 * @param analyzer DarkAnalyzer - analizator dla sieci
	 */
	private void setAnalyzer(DarkAnalyzer analyzer) {
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
	public PetriNetData getData() {
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
	 * Metoda ustala nowy obiekt macierzy inwariantów 2ej formy.
	 * @param invariants ArrayList[ArrayList[InvariantTransition]] - macierz inwariantów
	 */
	public void set2ndFormInvariantsList(ArrayList<ArrayList<InvariantTransition>> invariants) {
		this.invariants2ndForm = invariants;
	}
	
	/**
	 * Metoda zwraca obiekt macierzy inwariantów 2ej formy.
	 * @return ArrayList[ArrayList[InvariantTransition]] - macierz inwariantów
	 */
	public ArrayList<ArrayList<InvariantTransition>> get2ndFormInvariantsList() {
		return invariants2ndForm;
	}
	
	/**
	 * Metoda ustawia nową macierz inwariantów sieci.
	 * @param invariants ArrayList[ArrayList[Integer]] - macierz inwariantów
	 */
	public void setInvariantsMatrix(ArrayList<ArrayList<Integer>> invariants) {
		this.invariantsMatrix = invariants;
	}
	
	/**
	 * Metoda zwraca macierz inwariantów sieci.
	 * @return ArrayList[ArrayList[Integer]] - macierz inwariantów
	 */
	public ArrayList<ArrayList<Integer>> getInvariantsMatrix() {
		return invariantsMatrix;
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
		graphPanel.setNodesAndArcs(this.getData().nodes, this.getData().arcs);
		this.getGraphPanels().add(graphPanel);
	}

	/**
	 * Metoda pozwala na stworzenie, a następnie dodanie do projektu nowego arkusza
	 * GraphPanel, o zadanym w parametrze identyfikatorze sheetId.
	 * @param sheetId int - identyfikator nowego arkusza
	 * @return GraphPanel - który w wyniku wywołania metody został utworzony
	 */
	public GraphPanel createAndAddGraphPanel(int sheetId) {
		GraphPanel gp = new GraphPanel(sheetId, this, this.getData().nodes, this.getData().arcs);
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
	
	//*********************************************************************************

	/**
	 * Metoda ta zapisuje liczbę tokenów każdego miejsca
	 */
	public void saveMarkingZero() {
		ArrayList<Place> places = getPlaces();
		//ArrayList<Transition> transitions = getTransitions();
		
		for(int i=0; i<places.size(); i++) {
			backupMarkingZero.add(places.get(i).getTokensNumber());
		}
	}
	
	/**
	 * Metoda ta przywraca stan sieci przed rozpoczęciem symulacji.
	 */
	public void restoreMarkingZero() {
		ArrayList<Place> places = getPlaces();
		ArrayList<Transition> transitions = getTransitions();
		
		for(int i=0; i<places.size(); i++) {
			places.get(i).setTokensNumber(backupMarkingZero.get(i));
			places.get(i).returnTokens();
			//backupMarkingZero.add(places.get(i).getTokensNumber());
		}
		
		for(int i=0; i<transitions.size(); i++) {
			transitions.get(i).setLaunching(false);
			transitions.get(i).setFireTime(-1);
		}
		isBackup = false;
		
		setSimulator(new NetSimulator(NetType.BASIC, this));
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().
			setSimulator(getSimulator()); //podmienia ten z podokna na nowo utworzony
		GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().simMode.setSelectedIndex(0);
		repaintAllGraphPanels();
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
	 * Metoda pozwala na wyczyszczenie projektu z całej sieci. Zostają usunięte
	 * wszystkie wierzchołki, łuki oraz arkusze.
	 */
	public void clearProject() {
		for (GraphPanel gp : getGraphPanels())
			gp.getSelectionManager().forceDeselectAllElements();

		ArrayList<GraphPanel> newGraphPanels = new ArrayList<GraphPanel>();
		setNodes(new ArrayList<Node>());
		setArcs(new ArrayList<Arc>());
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
	 * Metoda pozwala na zapis całej sieci z projektu do pliku PNT
	 * @param filePath String - ścieżka do pliku zapisu
	 */
	public void saveAsPNT(String filePath) {
		communicationProtocol.writePNT(filePath, getPlaces(), getTransitions(), getArcs());
	}
	
	/**
	 * Metoda pozwala na zapis całej sieci z projektu do pliku ABYSS.
	 * @param filePath String - ścieżka do pliku zapisu
	 */
	public void saveAsAbyss(String filePath) {
		ABYSSwriter = new AbyssWriter();
		ABYSSwriter.write(filePath);
	}
	
	/**
	 * Metoda pozwala zapisać sieć do formatu SPPED programu Snoopy.
	 * @param filePath String - ścieżka docelowa pliku
	 */
	public void saveAsSPPED(String filePath) {
		SnoopyWriter sWr = new SnoopyWriter();
		sWr.writeSPPED(filePath);
	}

	/**
	 * Metoda pozwala na odczyt całej sieci z pliku podanego w parametrze
	 * metody. Wczytana sieć zostaje dodana do istniejącego już projektu,
	 * bez naruszania jego struktury.
	 * @param path String - ścieżka do pliku odczytu
	 */
	public void loadFromFile(String path) {
		//TODO: czyszczenie projektu!!!!!!!!!!!!!!!!!!!!!!!
		
		
		readerSNOOPY = SAXParserFactory.newInstance();
		try {
			// Format wlasny
			if (path.endsWith(".abyss")) {
				ABYSSReader = new AbyssReader();
				ABYSSReader.read(path);
				addArcsAndNodes(ABYSSReader.getArcArray(), ABYSSReader.getNodeArray());
				setName(ABYSSReader.getPNname());
			}
			// Formaty Snoopiego
			if (path.endsWith(".spped") || path.endsWith(".spept") || path.endsWith(".colpn")
					|| path.endsWith(".sptpt")) {
				InputStream xmlInput = new FileInputStream(path);

				SAXParser saxParser = readerSNOOPY.newSAXParser();
				// Wybor parsera
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
				
				String name = path;
				int ind = name.lastIndexOf("\\");
				if(ind > 1)
					name = name.substring(ind+1);
				name = name.replace(".spped", "");
				name = name.replace(".spept", "");
				name = name.replace(".colpn", "");
				name = name.replace(".sptpt", "");
				setName(name);
				
				int nodeSID = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().size() - 1;
				int SIN = GUIManager.getDefaultGUIManager().IDtoIndex(nodeSID);
				GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(SIN).getGraphPanel();
				graphPanel.setOriginSize(graphPanel.getSize());
				
			}
			// Format INY
			if (path.endsWith(".pnt")) {
				//communicationProtocol = new INAprotocols();
				communicationProtocol.readPNT(path);
				addArcsAndNodes(communicationProtocol.getArcArray(),communicationProtocol.getNodeArray());
			}
			GUIManager.getDefaultGUIManager().log("Snoopy Petri net successfully imported from file "+path, "text", true);
		} catch (Throwable err) {
			err.printStackTrace();
			GUIManager.getDefaultGUIManager().log("Error: " + err.getMessage(), "error", true);
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
			if (getInvariantsMatrix() != null) {
				communicationProtocol.writeInvToCSV(path, getInvariantsMatrix(), getTransitions());
				//GUIManager.getDefaultGUIManager().log("Invariants saved as CSV file.","text", true);
				if(!silence)
					JOptionPane.showMessageDialog(null,  "Invariants saved to file:\n"+path,
						"Success",JOptionPane.INFORMATION_MESSAGE);
				
				result = 0;
			} else {
				if(!silence)
					JOptionPane.showMessageDialog(null, "There are no invariants to export.",
						"Warning",JOptionPane.WARNING_MESSAGE);
				GUIManager.getDefaultGUIManager().log("No invariants, saving into CSV file failed.","error", true);
				result = -1;
			}
		} catch (Throwable err) {
			err.printStackTrace();
			GUIManager.getDefaultGUIManager().log("Error: " + err.getMessage(), "error", true);
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
			if (getInvariantsMatrix() != null) {
				communicationProtocol.writeINV(path, getInvariantsMatrix(), getTransitions());
				JOptionPane.showMessageDialog(null,
						"Invariants saved to file:\n"+path,
						"Success",JOptionPane.INFORMATION_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Invariants saved into .inv INA file.","text", true);
				result = 0;
			} else {
				JOptionPane.showMessageDialog(null,
						"There are no invariants to export",
						"Warning",JOptionPane.WARNING_MESSAGE);
				GUIManager.getDefaultGUIManager().log("No invariants, saving into CSV file failed.","error", true);
				result = -1;
			}
		} catch (Throwable err) {
			err.printStackTrace();
			GUIManager.getDefaultGUIManager().log("Error: " + err.getMessage(), "error", true);
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
			if (getInvariantsMatrix() != null) {
				communicationProtocol.writeCharlieInv(path, getInvariantsMatrix(), getTransitions());
				JOptionPane.showMessageDialog(null, 
						"Invariants saved to file:\n"+path,
						"Success",JOptionPane.INFORMATION_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Invariants saved in Charlie file format.","text", true);
				result = 0;
			} else {
				JOptionPane.showMessageDialog(null,
						"There are no invariants to export.",
						"Warning",JOptionPane.WARNING_MESSAGE);
				GUIManager.getDefaultGUIManager().log("No invariants, saving into CSV file failed.","error", true);
				result = -1;
			}
		} catch (Throwable err) {
			err.printStackTrace();
			GUIManager.getDefaultGUIManager().log("Error: " + err.getMessage(), "error", true);
		}
		return result;
	}
	
	/**
	 * Metoda wczytująca plik inwariantów z pliku .inv
	 * @param sciezka String - ścieżka do pliku INA
	 */
	public void loadInvariantsFromFile(String sciezka) {
		try {
			communicationProtocol.readINV(sciezka);
			setInvariantsMatrix(communicationProtocol.getInvariantsList());
			//invariantsMatrix = communicationProtocol.getInvariantsList();
		} catch (Throwable err) {
			err.printStackTrace();
			GUIManager.getDefaultGUIManager().log("Error: " + err.getMessage(), "error", true);
		}
	}

	/**
	 * Metoda rozpoczynająca analizę inwariantów dla otwartej sieci w osobnym
	 * watku.
	 */
	public void tInvariantsAnalyze() {
		try {
			eia = new EarlyInvariantsAnalyzer();
			Thread myThread = new Thread(eia);
			myThread.start();
		} catch (Throwable err) {
			err.printStackTrace();
			GUIManager.getDefaultGUIManager().log("Analyzer Error: " + err.getMessage(), "error", true);
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
	 * nową zakłądkę i zwraca jej numer. 
	 * @return int - id arkusza sieci do zapełnienia
	 */
	public int returnCleanSheetID() {
		int SID = 0;

		if (getData().nodes.isEmpty()) {
			SID = 0;
		} else {
			int tSID;// nodes.get(0).getNodeLocations()
			ArrayList<Integer> sheetList = new ArrayList<Integer>();
			int k = 0;
			for (k = 0; k < GUIManager.getDefaultGUIManager().getWorkspace().getSheets().size(); k++)
				sheetList.add(GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(k).getId());
			//System.out.println(sheetList.size());
			//System.out.println(GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(k - 1).getId());
			int[] tabSID = new int[GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(k - 1).getId() + 1];

			for (int j = 0; j < getData().nodes.size(); j++) {
				for (int i = 0; i < getData().nodes.get(j).getNodeLocations().size(); i++) {
					tSID = getData().nodes.get(j).getNodeLocations().get(i).getSheetID();
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
				SID = GUIManager.getDefaultGUIManager().getWorkspace().newTab();
			}
		}
		return SID;
	}

	/**
	 * Metoda zwracająca macierz inwariantów. 
	 * @return ArrayList[ArrayList[InvariantTransition]] - macierz inw.
	 */
	public ArrayList<ArrayList<InvariantTransition>> getInaInvariants() {
		ArrayList<ArrayList<Integer>> invariantsBinaryList = new ArrayList<ArrayList<Integer>>();
		InvariantTransition currentTransition;
		invariantsBinaryList = communicationProtocol.getInvariantsList();
		
		if (invariantsBinaryList.size() > 0) {
			set2ndFormInvariantsList(new ArrayList<ArrayList<InvariantTransition>>());
			if (invariantsBinaryList.get(0).size() == getTransitions().size()) {
				ArrayList<InvariantTransition> currentInvariant;
				int i; //iterator po tranzycjach sieci
				for (ArrayList<Integer> binaryInvariant : invariantsBinaryList) {
					currentInvariant = new ArrayList<InvariantTransition>();
					i = 0;
					for (Integer amountOfFirings : binaryInvariant) {
						if (amountOfFirings > 0) {
							currentTransition = new InvariantTransition(getTransitions().get(i), amountOfFirings);
							currentInvariant.add(currentTransition);
						}
						i++;
					}
					// SettingsManager.log(invariantLog);
					get2ndFormInvariantsList().add(currentInvariant);
				}
			} else {
				JOptionPane.showMessageDialog(null,
					"The currently opened project does not match with loaded external invariants. Please make sure you are loading the correct invariant file for the correct Petri net.",
					"Project mismatch error!", JOptionPane.ERROR_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Error: the currently opened project does not match with loaded external invariants. Please make sure you are loading the correct invariant file for the correct Petri net.", "error", true);
			}
		} else {
			JOptionPane.showMessageDialog(null,
				"There is something wrong with loaded external invariant file, as there are no invariants in it.",
				"Invariant file does not contain invariants", JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: preparing invariants internal representation failed.", "error", true);
		}
		return get2ndFormInvariantsList();
	}

	/**
	 * Metoda zwracająca macierz wygenerowanych inwariantów.
	 * @return ArrayList[ArrayList[InvariantTransition]] - macierz inw.
	 */
	public ArrayList<ArrayList<InvariantTransition>> getGeneratedInvariants() {
		ArrayList<ArrayList<Integer>> invariantsBinaryList = new ArrayList<ArrayList<Integer>>();
		InvariantTransition currentTransition;
		invariantsBinaryList = eia.getListaInvatianow();
		set2ndFormInvariantsList(new ArrayList<ArrayList<InvariantTransition>>());
		if (invariantsBinaryList.size() > 0) {
			if (invariantsBinaryList.get(0).size() == getTransitions().size()) {
				ArrayList<InvariantTransition> currentInvariant;
				int i;
				for (ArrayList<Integer> binaryInvariant : invariantsBinaryList) {
					currentInvariant = new ArrayList<InvariantTransition>();
					i = 0;
					// kolejny inwariant
					for (Integer amountOfFirings : binaryInvariant) {
						if (amountOfFirings > 0) {
							currentTransition = new InvariantTransition( getTransitions().get(i), amountOfFirings);
							currentInvariant.add(currentTransition);
						}
						i++;
					}
					get2ndFormInvariantsList().add(currentInvariant);
				}
			} else {
				JOptionPane.showMessageDialog(null,
					"The currently opened project does not match with loaded external invariants. Please make sure you are loading the correct invariant file for the correct Petri net.",
					"Project mismatch error!", JOptionPane.ERROR_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Error: currently opened project does not match with loaded external invariants. Please make sure you are loading the correct invariant file for the correct Petri net.", "error", true);
			}
		} else {
			JOptionPane.showMessageDialog(null,
				"There is something terribly wrong with your loaded external invariant file, as there are no invariants in it, actually. It's corrupt, is my guess.",
				"Invariant file does not contain invariants", JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error: preparing invariants internal representation failed.", "error", true);
		}
		return get2ndFormInvariantsList();
	}

	/**
	 * Metoda ustawia podświetlanie zbiorów MCT.
	 * @param isGlowedMTC boolean - true jeśli MCT ma być podświetlony
	 */
	public void setTransitionGlowedMTC(boolean isGlowedMTC) {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.TRANSITION)
				((Transition) n).setGlowed_MTC(isGlowedMTC);
			else if (n.getType() == PetriNetElementType.TIMETRANSITION)
				((TimeTransition) n).setGlowed_MTC(isGlowedMTC);
	}
	
	/**
	 * Metoda wygasza kolorowanie tranzycji w ramach klastra
	 * @param isColorActive boolean - true jeśli ma się wyświetlać
	 */
	public void setColorClusterToNeutral() {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.TRANSITION)
				((Transition) n).setGlowed_Cluster(false, Color.white, -1);
			else if (n.getType() == PetriNetElementType.TIMETRANSITION)
				((TimeTransition) n).setGlowed_Cluster(false, Color.white, -1);
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
		
		this.invSimulator = new InvariantsSimulator(
				abyss.analyzer.InvariantsSimulator.NetType.BASIC, new PetriNet(
						getData().nodes, getData().arcs), get2ndFormInvariantsList(),type, value);

		invSimulator.startSimulation(SimulatorMode.LOOP);
	}
}
