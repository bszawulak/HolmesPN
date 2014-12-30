package abyss.math;

import abyss.analyzer.EarlyInvariantsAnalyzer;
import abyss.analyzer.DarkAnalyzer;
import abyss.analyzer.InvariantsSimulator;
import abyss.analyzer.InvariantsSimulator.SimulatorMode;
import abyss.darkgui.GUIManager;
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
 * Klasa przechowuj¹ca listy wszystkich obiektów projektu oraz arkusze. Dodatkowo agreguje
 * metody dzia³aj¹ce na sieci - zapis, odczyt, generowanie MCT, klastrów, itd.
 * @author students - pierwsza wersja
 * @author MR - dodatkowe metody
 *
 */
public class PetriNet implements SelectionActionListener, Cloneable {
	private ArrayList<SelectionActionListener> actionListeners = new ArrayList<SelectionActionListener>();
	private ArrayList<ArrayList<InvariantTransition>> invariants2ndForm = new ArrayList<ArrayList<InvariantTransition>>();
	private ArrayList<ArrayList<Integer>> invariantsMatrix;
	private ArrayList<GraphPanel> graphPanels;
	private PetriNetData data = new PetriNetData(new ArrayList<Node>(), new ArrayList<Arc>());
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

	
	/**
	 * Konstruktor obiektu klasy PetriNet - dzia³a dla symulatora inwariantów.
	 * @param nod ArrayList[Node] - lista wierzcho³ków sieci
	 * @param ar ArrayList[Arc] - lista ³uków sieci
	 */
	public PetriNet(ArrayList<Node> nod, ArrayList<Arc> ar) {
		getData().nodes = nod;
		getData().arcs = ar;
		communicationProtocol = new IOprotocols();
		data.netName = "";
	}

	/**
	 * Konstruktor obiektu klasy PetriNet - g³ówny konstruktor dla workspace.
	 * @param workspace Workspace - obiekt obszaru roboczego dla sieci
	 */
	public PetriNet(Workspace workspace, String name) {
		this.setGraphPanels(new ArrayList<GraphPanel>());
		this.workspace = workspace;
		this.setSimulator(new NetSimulator(NetType.BASIC, this));
		this.setAnalyzer(new DarkAnalyzer(this));
		communicationProtocol = new IOprotocols();
		data.netName = "";
	}

	/**
	 * Metoda pozwala pobraæ wszystkie obiekty miejsc dla danej sieci.
	 * Wszystkie wierzcho³ki w projekcie s¹ przechowywane w obrêbie jednej
	 * listy ArrayList, jednak dziêki tej metodzie mo¿na j¹ przefiltrowaæ.
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
	 * Metoda pozwala pobraæ wszystkie obiekty tranzycji dla danej sieci.
	 * Wszystkie wierzcho³ki w projekcie s¹ przechowywane w obrêbie jednej
	 * listy ArrayList[Node], jednak dziêki tej metodzie mo¿na j¹ przefiltrowaæ.
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
	 * Metoda pozwala ustawiæ listê wszystkich wierzcho³ków zawartych w projekcie.
	 * Zmiana ta zostaje automatycznie rozpropagowana do wszystkich arkuszy
	 * GraphPanel przechowywanych w projekcie.
	 * @param nodes ArrayList[Node] - nowa lista wierzcho³ków
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.getData().nodes = nodes;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setNodes(nodes);
	}
	
	/**
	 * Metoda ustawia nazwê dla sieci Petriego.
	 * @param name String - nowa nazwa
	 */
	public void setName(String name) {
		data.netName = name;
	}
	
	/**
	 * Metoda zwraca nazwê sieci petriego.
	 * @return String - nazwa sieci
	 */
	public String getName() {
		return data.netName;
	}

	/**
	 * Metoda pozwala pobraæ wszystkie ³uki dla danej sieci.
	 * @return ArrayList[Arc] - lista ³uków zawartych w projekcie
	 */
	public ArrayList<Arc> getArcs() {
		return this.getData().arcs;
	}
	
	/**
	 * Metoda powoduje ustawienie w projekcie listy wierzcho³ków ArrayList[Node]
	 * oraz listy ³uków ArrayList[Arc] z których zostanie zbudowana sieæ. Zmiana
	 * ta zostaje automatycznie rozpropagowana na wszystkie arkusze w projekcie.
	 * @param arcs ArrayList[Arc] - nowa lista ³uków
	 * @param nodes ArrayList[Node] - nowa lista wierzcho³ków
	 */
	public void addArcsAndNodes(ArrayList<Arc> arcs, ArrayList<Node> nodes) {
		this.getArcs().addAll(arcs);
		this.getNodes().addAll(nodes);
	}
	
	/**
	 * Metoda pozwala ustawiæ nowe ³uki dla danej sieci. Zmiana
	 * ta zostaje automatycznie rozpropagowana do wszystkich .
	 * @param arcs ArrayList[Arc] - nowa lista ³uków
	 */
	public void setArcs(ArrayList<Arc> arcs) {
		this.getData().arcs = arcs;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setArcs(this.getData().arcs);
	}

	/**
	 * Metoda pozwala pobraæ listê wszystkich wierzcho³ków Node zawartych w projekcie.
	 * @return ArrayList[Node] - lista wierzcho³ków sieci
	 */
	public ArrayList<Node> getNodes() {
		return this.getData().nodes;
	}
	
	/**
	 * Metoda pozwala ustawiæ aktualny tryb rysowania definiowany przez typ DrawModes.
	 * Zmiana ta zostaje rozpropagowana  na wszystkie arkusze, przez co zmiana trybu
	 * rysowania staje siê globalna.
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
	 * Metoda zwracaj¹ca handler oczytu pliku sieci.
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
	 * Metoda zwracaj¹ca obiekt w ramach którego dzia³a aktualna sieæ.
	 * @return Workspace - obiekt zawieraj¹cy obiekt sieci.
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * Metoda ustawia nowy obiekt w ramach którego dzia³a sieæ Petriego.
	 * @param workspace Workspace - jak wy¿ej
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Metoda ustawia obiekt nas³uchuj¹cy o zmianach na sieci.
	 * @param a SelectionActionListener - obiekt nas³ucjuj¹cy
	 */
	public void addActionListener(SelectionActionListener a) {
		this.actionListeners.add(a);
	}

	/**
	 * Metoda aktywuj¹ca obiekty nas³uchuj¹ce sieci.
	 * @param e SelectionActionEvent - obiekty nas³uchuj¹ce
	 */
	private void invokeActionListeners(SelectionActionEvent e) {
		for (SelectionActionListener a : this.actionListeners)
			a.actionPerformed(e);
	}

	/**
	 * Metoda bêd¹ca implementacj¹ interfejsu SelectionActionListener,
	 * wywo³ywana dla zajœcia zdarzanie zmiany znaczenia na którymkolwiek z
	 * arkuszy GraphPan) zawartych w projekcie, dla których obiekt klasy PetriNet
	 * jest domyœlnym obiektem nas³uchuj¹cym. Zdarzenie jest propagowane dalej, 
	 * przekazuj¹c o nim informacje do innych elementów frameworku, jak np. Properties.
	 * @param arg0 SelectionActionEvent -  zdarzenie przekazuj¹ce informacje o zaznaczonych obiektach
	 */
	public void actionPerformed(SelectionActionEvent arg0) {
		this.invokeActionListeners(arg0);
	}

	/**
	 * Metoda pozwala na pobrania aktualnego obiektu symulatora Netsimulator
	 * dla bie¿¹cego projektu
	 * @return NetSimulator - symulator
	 */
	public NetSimulator getSimulator() {
		return simulator;
	}
	
	/**
	 * Metoda pozwala na ustawienie symulatora NetSimulator dla bie¿¹cego projektu
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
	 * Metoda zwracaj¹ca obiekt analizatora, np. na potrzeby generacji MCT.
	 * @return DarkAnalyzer - obiekt analizatora
	 */
	public DarkAnalyzer getAnalyzer() {
		return analyzer;
	}

	/**
	 * Metoda ustawiaj¹ca aktualny analizator dla obiektu sieci.
	 * @param analyzer DarkAnalyzer - analizator dla sieci
	 */
	private void setAnalyzer(DarkAnalyzer analyzer) {
		this.analyzer = analyzer;
	}
	
	/**
	 * Metoda zwraca tablicê arkuszy sieci.
	 * @return ArrayList[GraphPanel] - lista arkuszy.
	 */
	public ArrayList<GraphPanel> getGraphPanels() {
		return graphPanels;
	}

	/**
	 * Metoda ustawiaj¹ca tablicê arkuszy sieci.
	 * @param graphPanels ArrayList[GraphPanel] - arkusze sieci
	 */
	public void setGraphPanels(ArrayList<GraphPanel> graphPanels) {
		this.graphPanels = graphPanels;
	}

	/**
	 * Metoda zwracaj¹ca pe³ne dane sieci - wierzcho³ki i ³uki.
	 * @return PetriNetData - wierzcho³ki i ³uki sieci
	 */
	public PetriNetData getData() {
		return data;
	}

	/**
	 * Metoda ustawiaj¹ca pe³ne dane sieci - wierzcho³ki i ³uki.
	 * @param data PetriNetData - wierzcho³ki i ³uki sieci
	 */
	public void setData(PetriNetData data) {
		this.data = data;
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
	 * Metoda ustawia now¹ macierz inwariantów sieci.
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
	 * wierzcho³ków oraz ³uków.
	 * @param sheetID int - identyfikator nowego arkusza
	 * @return boolean - true je¿eli arkusz nie ma wêz³ów;
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
	 * wierzcho³ków oraz ³uków.
	 * @param graphPanel - nowy arkusz
	 */
	public void addGraphPanel(GraphPanel graphPanel) {
		graphPanel.setNodesAndArcs(this.getData().nodes, this.getData().arcs);
		this.getGraphPanels().add(graphPanel);
	}

	/**
	 * Metoda pozwala na stworzenie, a nastêpnie dodanie do projektu nowego arkusza
	 * GraphPanel, o zadanym w parametrze identyfikatorze sheetId.
	 * @param sheetId int - identyfikator nowego arkusza
	 * @return GraphPanel - który w wyniku wywo³ania metoda zosta³ utworzony
	 */
	public GraphPanel createAndAddGraphPanel(int sheetId) {
		GraphPanel gp = new GraphPanel(sheetId, this, this.getData().nodes, this.getData().arcs);
		gp.setDrawMode(this.drawMode);
		this.getGraphPanels().add(gp);
		return gp;
	}

	/**
	 * Metoda pozwala pobraæ arkusz GraphPanel o podanym w parametrze identyfikatorze sheetId.
	 * @param sheetID int - identyfikator arkusza
	 * @return GraphPanel - o podanym identyfikatorze.
	 * 		Jeœli taki nie istnieje, metoda zwraca wartoœæ null
	 */
	public GraphPanel getGraphPanel(int sheetID) {
		for (GraphPanel gp : this.getGraphPanels())
			if (gp.getSheetId() == sheetID)
				return gp;
		return null;
	}

	/**
	 * Metoda zwraca obiekt g³ównego komunikatora obiektu sieci, zawieraj¹cego
	 * metody I/O dla programu / formatu INA.
	 * @return INAprotocols - obiekt klasy odpowiedzialnej za pliki
	 */
	public IOprotocols getCommunicator() {
		return communicationProtocol;
	}
	
	//*********************************************************************************

	/**
	 * Metoda wy³¹czaj¹ca œwiecenie tranzycji np. w ramach aktywacji niezmiennika.
	 */
	public void turnTransitionGlowingOff() {
		for (GraphPanel gp : getGraphPanels()) {
			gp.getSelectionManager().removeTransitionsGlowing();
			gp.repaint();
		}
	}

	/**
	 * Metoda powoduje usuniêcie arkusza GraphPanel o identyfikatorze podanym w
	 * parametrze. Nie powoduje to jednak usuniêcia zawartych na nim wierzcho³ków
	 * oraz ³uków.
	 * @param sheetID int - identyfikator który ma zostaæ usuniêty
	 * @return boolean - true w sytuacji powodzenia operacji GraphPanel od podanym identyfikatorze istnia³
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
	 * Metoda sprawdza potencjalne konflikty w nazwach wierzcho³ków.
	 * @param name - nazwa wierzcho³ka
	 * @return boolean - true je¿eli nazwa ju¿ istnieje; false w przeciwnym wypadku
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
	 * Metoda zwraca listê obrazów utworzonych ze wszystkich istniej¹cych arkuszy.
	 * @return ArrayList[BufferedImage] - lista obrazów
	 */
	public ArrayList<BufferedImage> getImagesFromGraphPanels() {
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		for (GraphPanel g : this.getGraphPanels())
			images.add(g.createImageFromSheet());
		return images;
	}

	/**
	 * Metoda pozwala na wyczyszczenie projektu z ca³ej sieci. Zostaj¹ usuniête
	 * wszystkie wierzcho³ki, ³uki oraz arkusze.
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
	 * Metoda pozwala na zapis ca³ej sieci z projektu do pliku. Format
	 * pliku zostaje okreœlony na podstawie rozszerzenia podanego w œcie¿ce
	 * do pliku przekazanej w parametrze metody
	 * @param filePath String - œcie¿ka do pliku zapisu
	 */
	public void saveToFile(String filePath) {
		if (filePath.endsWith(".pnt")) {
			filePath = filePath.substring(0, filePath.length() - 4);
			communicationProtocol.writePNT(filePath, getPlaces(), getTransitions(), getArcs());
		}
		if (filePath.endsWith(".abyss")) {
			filePath = filePath.substring(0, filePath.length() - 6);
			ABYSSwriter = new AbyssWriter();
			ABYSSwriter.write(filePath);
		}
		//if (sciezka.endsWith(".png")) { } //wykonywane w GUIManager.exportProjectToImage
	}

	/**
	 * Metoda pozwala na odczyt ca³ej sieci z pliku podanego w parametrze
	 * metody. Wczytana sieæ zostaje dodana do istniej¹cego ju¿ projektu,
	 * bez naruszania jego struktury.
	 * @param path String - œcie¿ka do pliku odczytu
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
	 * Metoda zapisuj¹ca inwarianty do pliku w formacie CSV.
	 * @param path String - œcie¿ka do pliku zapisu
	 * @return int - 0 jeœli operacja siê uda³a, -1 w przeciwnym wypadku
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
	 * Metoda zapisuj¹ca inwarianty do pliku w formacie INA.
	 * @param path String - œcie¿ka do pliku zapisu
	 * @return int - 0 jeœli operacja siê uda³a, -1 w przeciwnym wypadku
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
	 * Metoda zapisuj¹ca inwarianty do pliku w formacie CSV.
	 * @param path String - œcie¿ka do pliku zapisu
	 * @return int - 0 jeœli operacja siê uda³a, -1 w przeciwnym wypadku
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
	 * Metoda wczytuj¹ca plik inwariantów z pliku .inv
	 * @param sciezka String - œcie¿ka do pliku INA
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
	 * Metoda rozpoczynaj¹ca analizê inwariantów dla otwartej sieci w osobnym
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
	 * Metoda przerysowuj¹ca arkusze wyœwietlaj¹ce wszystkie czêœci sieci.
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
	 * znajduj¹cymi siê na nich jest zablokowana.
	 * @return boolean - true w sytuacji gdy symulacja jest aktualnie aktywna;
	 * 		false w przypadku przeciwnym
	 */
	public boolean isSimulationActive() {
		return isSimulationActive;
	}

	/**
	 * Metoda pozwala na zwiêkszenie koku symulacji o 1. Jest ona wywo³ywana przez
	 * symulator danego projektu NetSimulator, co powoduje wywo³anie metody 
	 * incrementSimulationStep() dla ka¿dego ³uku zawartego w projekcie, odpowiedzialnego
	 * za wyœwietlanie animacji tokenów przemieszczaj¹cych siê w trakcie symulacji.
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
	 * znajduj¹cymi siê na nich jest zablokowana.
	 * @param isSimulationActive boolean - nowy stan symulacji
	 */
	public void setSimulationActive(boolean isSimulationActive) {
		this.isSimulationActive = isSimulationActive;
		for (GraphPanel g : this.getGraphPanels())
			g.setSimulationActive(isSimulationActive);
	}

	/**
	 * Metoda zwracaj¹ca identyfikator arkusza sieci.
	 * @return int - id arkusza sieci
	 */
	public int checkSheetID() {
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
	 * Metoda zwracaj¹ca macierz inwariantów. 
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
							currentTransition = new InvariantTransition(
									getTransitions().get(i), amountOfFirings);
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
	 * Metoda zwracaj¹ca macierz wygenerowanych inwariantów.
	 * @return ArrayList[ArrayList[InvariantTransition]] - macierz inw.
	 */
	public ArrayList<ArrayList<InvariantTransition>> getGeneratedInvariants() {
		ArrayList<ArrayList<Integer>> invariantsBinaryList = new ArrayList<ArrayList<Integer>>();
		InvariantTransition currentTransition;
		invariantsBinaryList = eia.getListaInvatianow();
		set2ndFormInvariantsList(new ArrayList<ArrayList<InvariantTransition>>());
		//SettingsManager.log("start logging");
		//String invariantLog;
		if (invariantsBinaryList.size() > 0) {
			if (invariantsBinaryList.get(0).size() == getTransitions().size()) {
				ArrayList<InvariantTransition> currentInvariant;
				//int count = 0;
				int i;
				for (ArrayList<Integer> binaryInvariant : invariantsBinaryList) {
					currentInvariant = new ArrayList<InvariantTransition>();
					i = 0;
					// SettingsManager.log("next invariant");
					//invariantLog = new String("");
					for (Integer amountOfFirings : binaryInvariant) {
						if (amountOfFirings > 0) {
							// invariantLog += " " + amountOfFirings.toString();
							currentTransition = new InvariantTransition( getTransitions().get(i), amountOfFirings);
							// invariantLog += " " + amountOfFirings.toString();
							currentInvariant.add(currentTransition);
							// invariantLog += " " +
							// currentInvariant.get(currentInvariant.size()-1).getAmountOfFirings().toString();
						}
						i++;
					}
					// if (count < 5)
					// SettingsManager.log(invariantLog);
					get2ndFormInvariantsList().add(currentInvariant);
					//count++;
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
	 * Metoda ustawia podœwietlanie zbiorów MCT.
	 * @param isGlowedMTC boolean - true jeœli MCT ma byæ podœwietlony
	 */
	public void setTransitionGlowedMTC(boolean isGlowedMTC) {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.TRANSITION)
				((Transition) n).setGlowedMTC(isGlowedMTC);
			else if (n.getType() == PetriNetElementType.TIMETRANSITION)
				((TimeTransition) n).setGlowedMTC(isGlowedMTC);
	}
	
	/**
	 * Metoda rozpoczynaj¹ca proces symulatora wykonañ inwariantów.
	 * @param type int - typ symulacji: 0 - zwyk³a, 1 - czasowa
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
