package abyss.math;

import abyss.analyzer.EarlyInvariantsAnalyzer;
import abyss.analyzer.DarkAnalyzer;
import abyss.analyzer.InvariantsSimulator;
import abyss.analyzer.InvariantsSimulator.SimulatorMode;
import abyss.analyzer.NetPropAnalyzer;
import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener;
import abyss.graphpanel.GraphPanel;
import abyss.graphpanel.IdGenerator;
//import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.graphpanel.GraphPanel.DrawModes;
import abyss.math.Node;
import abyss.math.parser.AbyssReader;
import abyss.math.parser.AbyssWriter;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.parser.ColoredNetHandler;
import abyss.math.parser.ExtendedNetHandler;
import abyss.math.parser.INAinvariants;
import abyss.math.parser.INAinvariantsWriter;
//import abyss.math.parser.INAreader;
import abyss.math.parser.INAreader2;
import abyss.math.parser.INAwriter;
import abyss.math.parser.NetHandler;
import abyss.math.parser.StandardNetHandler;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.NetType;

//import java.awt.Graphics;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
//import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
//import java.util.Date;
import java.util.Iterator;
//import java.util.Timer;
//import java.util.TimerTask;

import javax.swing.JOptionPane;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

//import org.xml.sax.SAXException;

import abyss.settings.SettingsManager;
import abyss.workspace.Workspace;
import abyss.workspace.WorkspaceSheet;

//import org.simpleframework.xml.Element;
//import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
//import org.simpleframework.xml.core.Persister;

@Root
public class PetriNet implements SelectionActionListener, Cloneable {

	private PetriNetData data = new PetriNetData(new ArrayList<Node>(), new ArrayList<Arc>());
	private ArrayList<GraphPanel> graphPanels;
	private IdGenerator idGenerator;
	public SAXParserFactory readerSNOOPY;
	private AbyssWriter ABYSSwriter;
	private AbyssReader ABYSSReader;
	private INAreader2 readerINA2;
	private INAwriter writerINA;
	private INAinvariants invariantsINA;
	private ArrayList<ArrayList<InvariantTransition>> invariants = new ArrayList<ArrayList<InvariantTransition>>();
	public NetHandler handler;
	private Workspace workspace;
	private DrawModes drawMode = DrawModes.POINTER;
	private ArrayList<SelectionActionListener> actionListeners = new ArrayList<SelectionActionListener>();
	private boolean isSimulationActive = false;
	private NetSimulator simulator;
	private InvariantsSimulator invSimulator;
	private EarlyInvariantsAnalyzer eia;
	public ArrayList<ArrayList<Integer>> genInvariants;
	private DarkAnalyzer analyzer;
	private NetPropAnalyzer netPropAna; // Propanbutan
	private INAinvariantsWriter inaIw;

	public PetriNet(ArrayList<Node> nod, ArrayList<Arc> ar) {
		getData().nodes = nod;
		getData().arcs = ar;
	}

	public PetriNet(Workspace workspace) {
		this.setGraphPanels(new ArrayList<GraphPanel>());
		this.workspace = workspace;
		this.setSimulator(new NetSimulator(NetType.BASIC, this));
		this.setAnalyzer(new DarkAnalyzer(this));
	}

	public ArrayList<Place> getPlaces() {
		ArrayList<Place> returnPlaces = new ArrayList<Place>();
		for (Node n : this.getData().nodes) {
			if (n instanceof Place)
				returnPlaces.add((Place) n);
		}
		return returnPlaces;
	}

	public ArrayList<Transition> getTranstions() {
		ArrayList<Transition> returnTransitions = new ArrayList<Transition>();
		for (Node n : this.getData().nodes) {
			if (n instanceof Transition)
				returnTransitions.add((Transition) n);
		}
		return returnTransitions;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.getData().nodes = nodes;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setNodes(nodes);
	}

	public ArrayList<Arc> getArcs() {
		return this.getData().arcs;
	}

	public void setArcs(ArrayList<Arc> arcs) {
		this.getData().arcs = arcs;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setArcs(this.getData().arcs);
	}

	public boolean isSheetEmpty(int sheetID) {
		boolean result = true;
		for (Node node : getNodes())
			for (ElementLocation location : node.getNodeLocations())
				if (location.getSheetID() == sheetID)
					result = false;
		return result;
	}

	public void addGraphPanel(GraphPanel graphPanel) {
		graphPanel.setNodesAndArcs(this.getData().nodes, this.getData().arcs);
		this.getGraphPanels().add(graphPanel);
	}

	public GraphPanel createAndAddGraphPanel(int sheetId) {
		GraphPanel gp = new GraphPanel(sheetId, this, this.getData().nodes,
				this.getData().arcs);
		gp.setDrawMode(this.drawMode);
		this.getGraphPanels().add(gp);
		return gp;
	}

	public GraphPanel getGraphPanel(int sheetID) {
		for (GraphPanel gp : this.getGraphPanels())
			if (gp.getSheetId() == sheetID)
				return gp;
		return null;
	}

	public void turnTransitionGlowingOff() {
		for (GraphPanel gp : getGraphPanels()) {
			gp.getSelectionManager().removeTransitionsGlowing();
			gp.repaint();
		}
	}

	public void addArcsAndNodes(ArrayList<Arc> arcs, ArrayList<Node> nodes) {
		this.getArcs().addAll(arcs);
		this.getNodes().addAll(nodes);
	}

	public ArrayList<Node> getNodes() {
		return this.getData().nodes;
	}

	public boolean removeGraphPanel(int sheetID) {
		System.out.println("Przed usunieciem:");
		for (GraphPanel g : getGraphPanels())
			System.out.println(g);
		for (Node n : getNodes())
			System.out.println(n);
		for (Arc n : getArcs())
			System.out.println(n);
		for (Iterator<GraphPanel> gpIterator = getGraphPanels().iterator(); gpIterator
				.hasNext();)
			if (gpIterator.next().getSheetId() == sheetID) {
				gpIterator.remove();
				for (Iterator<Node> nodeIterator = getNodes().iterator(); nodeIterator
						.hasNext();) {
					Node n = nodeIterator.next();
					for (Iterator<ElementLocation> elIterator = n
							.getNodeLocations(sheetID).iterator(); elIterator
							.hasNext();) {
						ElementLocation el = elIterator.next();
						if (!n.removeElementLocation(el)) {
							nodeIterator.remove();
						}
						for (Iterator<Arc> j = el.getInArcs().iterator(); j
								.hasNext();) {

							this.getArcs().remove(j.next());
							j.remove();
						}
						// deletes all out arcs of current ElementLocation
						for (Iterator<Arc> j = el.getOutArcs().iterator(); j
								.hasNext();) {

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

	public void setDrawMode(DrawModes mode) {
		this.drawMode = mode;
		for (GraphPanel gp : this.getGraphPanels())
			gp.setDrawMode(mode);
	}

	public DrawModes getDrawMode() {
		return this.drawMode;
	}

	/**
	 * Looking for name conflicts in collection of arcs and nodes
	 * 
	 * @param name
	 * @return true - conflict exists, false - there is no conflicts
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

	public ArrayList<BufferedImage> getImagesFromGraphPanels() {
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
		for (GraphPanel g : this.getGraphPanels())
			images.add(g.createImageFromSheet());
		return images;
	}

	public void clearProject() {
		ArrayList<GraphPanel> newGraphPanels = new ArrayList<GraphPanel>();
		setNodes(new ArrayList<Node>());
		setArcs(new ArrayList<Arc>());
		for (GraphPanel gp : getGraphPanels()) {
			int sheetID = gp.getSheetId();
			WorkspaceSheet.SheetPanel sheetPanel = (WorkspaceSheet.SheetPanel) gp
					.getParent();
			sheetPanel.remove(gp);
			GraphPanel newGraphPanel = new GraphPanel(sheetID, this,
					getNodes(), getArcs());
			sheetPanel.add(newGraphPanel);
			newGraphPanels.add(newGraphPanel);
		}
		setGraphPanels(newGraphPanels);
		repaintAllGraphPanels();
	}

	public void saveToFile(String sciezka) {

		if (sciezka.endsWith(".pnt")) {
			sciezka = sciezka.substring(0, sciezka.length() - 4);
			writerINA = new INAwriter();
			writerINA.write(sciezka, getPlaces(), getTranstions(), getArcs());
		}
		if (sciezka.endsWith(".abyss")) {
			sciezka = sciezka.substring(0, sciezka.length() - 6);
			ABYSSwriter = new AbyssWriter();
			ABYSSwriter.write(sciezka);
		}
		if (sciezka.endsWith(".png")) {

		}
	}

	public void loadFromFile(String sciezka) {

		readerSNOOPY = SAXParserFactory.newInstance();
		try {
			// Format wlasny
			if (sciezka.endsWith(".abyss")) {
				ABYSSReader = new AbyssReader();
				ABYSSReader.read(sciezka);
				addArcsAndNodes(ABYSSReader.getArcArray(),
						ABYSSReader.getNodeArray());
			}
			// Formaty Snoopiego
			if (sciezka.endsWith(".spped") || sciezka.endsWith(".spept")
					|| sciezka.endsWith(".colpn")) {
				InputStream xmlInput = new FileInputStream(sciezka);

				SAXParser saxParser = readerSNOOPY.newSAXParser();
				// Wyb�r parsera
				if (sciezka.endsWith(".spped")) {
					handler = new StandardNetHandler();
				}
				if (sciezka.endsWith(".spept")) {
					handler = new ExtendedNetHandler();
				}
				if (sciezka.endsWith(".colpn")) {
					handler = new ColoredNetHandler();
				}
				saxParser.parse(xmlInput, handler);
				addArcsAndNodes(handler.getArcList(), handler.getNodesList());
			}
			// Format INY
			if (sciezka.endsWith(".pnt")) {
				readerINA2 = new INAreader2();
				readerINA2.read(sciezka);

				addArcsAndNodes(readerINA2.getArcArray(),
						readerINA2.getNodeArray());
			}

		} catch (Throwable err) {
			err.printStackTrace();
		}

	}

	public void writeInvariantsToInaFormat(String path) {
		try {
			if (genInvariants != null) {
				inaIw = new INAinvariantsWriter();
				inaIw.write(path, genInvariants, getTranstions());
			} else {
				JOptionPane.showMessageDialog(null,
						"There are no invariants to export",
						"Uwaga!  Achtung!  Attention!",
						JOptionPane.WARNING_MESSAGE);

			}
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

	public void tInvariantsAnalyze() {
		try {

			eia = new EarlyInvariantsAnalyzer();
			Thread myThread = new Thread(eia);
			myThread.start();

		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

	/*
	 * Wczytywanie inwariantow z INY, wywolanie z: loaxExternalAnalysis
	 */
	public void loadInvariantsFromFile(String sciezka) {
		try {

			invariantsINA = new INAinvariants();
			invariantsINA.read(sciezka);

		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

	public NetHandler getHandler() {
		return handler;
	}

	public IdGenerator getIdGenerator() {
		return idGenerator;
	}

	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public void repaintAllGraphPanels() {
		for (GraphPanel g : this.getGraphPanels()) {
			g.invalidate();
			g.repaint();
		}
	}

	public void addActionListener(SelectionActionListener a) {
		this.actionListeners.add(a);
	}

	private void invokeActionListeners(SelectionActionEvent e) {
		for (SelectionActionListener a : this.actionListeners)
			a.actionPerformed(e);
	}

	@Override
	public void actionPerformed(SelectionActionEvent arg0) {
		this.invokeActionListeners(arg0);
	}

	public NetSimulator getSimulator() {
		return simulator;
	}
	
	public InvariantsSimulator getInvSimulator() {
		return invSimulator;
	}

	public void setSimulator(NetSimulator simulator) {
		this.simulator = simulator;
	}

	public boolean isSimulationActive() {
		return isSimulationActive;
	}

	public void incrementSimulationStep() {
		for (Arc a : getArcs())
			a.incrementSimulationStep();
		for (GraphPanel g : this.getGraphPanels()) {
			g.invalidate();
			g.repaint();
		}
	}

	public void setSimulationActive(boolean isSimulationActive) {
		this.isSimulationActive = isSimulationActive;
		for (GraphPanel g : this.getGraphPanels())
			g.setSimulationActive(isSimulationActive);
	}

	public DarkAnalyzer getAnalyzer() {
		return analyzer;
	}

	private void setAnalyzer(DarkAnalyzer analyzer) {
		this.analyzer = analyzer;
	}

	public int checkSheetID() {
		int SID = 0;

		if (getData().nodes.isEmpty()) {
			SID = 0;
		} else {
			int tSID;// nodes.get(0).getNodeLocations()
			ArrayList<Integer> sheetList = new ArrayList<Integer>();
			int k = 0;
			for (k = 0; k < GUIManager.getDefaultGUIManager().getWorkspace()
					.getSheets().size(); k++)
				sheetList.add(GUIManager.getDefaultGUIManager().getWorkspace()
						.getSheets().get(k).getId());
			System.out.println(sheetList.size());
			System.out.println(GUIManager.getDefaultGUIManager().getWorkspace()
					.getSheets().get(k - 1).getId());
			int[] tabSID = new int[GUIManager.getDefaultGUIManager()
					.getWorkspace().getSheets().get(k - 1).getId() + 1];

			for (int j = 0; j < getData().nodes.size(); j++) {
				for (int i = 0; i < getData().nodes.get(j).getNodeLocations()
						.size(); i++) {
					tSID = getData().nodes.get(j).getNodeLocations().get(i)
							.getSheetID();
					// System.out.println(tSID);
					tabSID[tSID]++;
					// System.out.println(tabSID[tSID]);
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

	public ArrayList<ArrayList<InvariantTransition>> getInvariants() {
		ArrayList<ArrayList<Integer>> invariantsBinaryList = new ArrayList<ArrayList<Integer>>();
		InvariantTransition currentTransition;
		invariantsBinaryList = invariantsINA.getInvariantsList();
		setInvariantsList(new ArrayList<ArrayList<InvariantTransition>>());
		SettingsManager.log("start logging");
		//String invariantLog = new String("");
		if (invariantsBinaryList.size() > 0) {
			if (invariantsBinaryList.get(0).size() == getTranstions().size()) {
				ArrayList<InvariantTransition> currentInvariant;
				//int count = 0;
				int i;
				for (ArrayList<Integer> binaryInvariant : invariantsBinaryList) {
					currentInvariant = new ArrayList<InvariantTransition>();
					i = 0;
					// SettingsManager.log("next invariant");
					//String invariantLog = new String("");
					for (Integer amountOfFirings : binaryInvariant) {
						if (amountOfFirings > 0) {
							// invariantLog += " " + amountOfFirings.toString();
							currentTransition = new InvariantTransition(
									getTranstions().get(i), amountOfFirings);
							// invariantLog += " " + amountOfFirings.toString();
							currentInvariant.add(currentTransition);
							// invariantLog += " " +
							// currentInvariant.get(currentInvariant.size()-1).getAmountOfFirings().toString();
						}
						i++;
					}
					// if (count < 5)
					// SettingsManager.log(invariantLog);
					getInvariantsList().add(currentInvariant);
					//count++;
				}
			} else {
				JOptionPane
						.showMessageDialog(
								null,
								"The currently opened project does not match with loaded external invariants. Please make sure you are loading the correct invariant file for the correct Petri net.",
								"Project mismatch error!",
								JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane
					.showMessageDialog(
							null,
							"There is something terribly wrong with your loaded external invariant file, as there are no invariants in it, actually. It's corrupt, is my guess.",
							"Invariant file does not contain invariants",
							JOptionPane.ERROR_MESSAGE);
		}
		return getInvariantsList();
	}

	public ArrayList<ArrayList<InvariantTransition>> getGeneratedInvariants() {
		ArrayList<ArrayList<Integer>> invariantsBinaryList = new ArrayList<ArrayList<Integer>>();
		InvariantTransition currentTransition;
		invariantsBinaryList = eia.getListaInvatianow();
		setInvariantsList(new ArrayList<ArrayList<InvariantTransition>>());
		SettingsManager.log("start logging");
		//String invariantLog;
		if (invariantsBinaryList.size() > 0) {
			if (invariantsBinaryList.get(0).size() == getTranstions().size()) {
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
							currentTransition = new InvariantTransition(
									getTranstions().get(i), amountOfFirings);
							// invariantLog += " " + amountOfFirings.toString();
							currentInvariant.add(currentTransition);
							// invariantLog += " " +
							// currentInvariant.get(currentInvariant.size()-1).getAmountOfFirings().toString();
						}
						i++;
					}
					// if (count < 5)
					// SettingsManager.log(invariantLog);
					getInvariantsList().add(currentInvariant);
					//count++;
				}
			} else {
				JOptionPane
						.showMessageDialog(
								null,
								"The currently opened project does not match with loaded external invariants. Please make sure you are loading the correct invariant file for the correct Petri net.",
								"Project mismatch error!",
								JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane
					.showMessageDialog(
							null,
							"There is something terribly wrong with your loaded external invariant file, as there are no invariants in it, actually. It's corrupt, is my guess.",
							"Invariant file does not contain invariants",
							JOptionPane.ERROR_MESSAGE);
		}
		return getInvariantsList();
	}

	public ArrayList<GraphPanel> getGraphPanels() {
		return graphPanels;
	}

	public void setGraphPanels(ArrayList<GraphPanel> graphPanels) {
		this.graphPanels = graphPanels;
	}

	public void setTransitionGlowedMTC(boolean isGlowedMTC) {
		for (Node n : getNodes())
			if (n.getType() == PetriNetElementType.TRANSITION)
				((Transition) n).setGlowedMTC(isGlowedMTC);
			//else if (n.getType() == PetriNetElementType.TIMETRANSITION)
			//	((Transition) n).setGlowedMTC(isGlowedMTC);
	}

	public PetriNetData getData() {
		return data;
	}

	public void setData(PetriNetData data) {
		this.data = data;
	}

	public NetPropAnalyzer getNetPropAnal() {
		return netPropAna;
	}

	public void setNetPropAna(NetPropAnalyzer netPropAnal) {
		this.netPropAna = netPropAnal;
	}

	public void startInvSim(int type, int value) throws CloneNotSupportedException {
		//Timer timer = new Timer();
		//Date date = new Date();
		
		this.invSimulator = new InvariantsSimulator(
				abyss.analyzer.InvariantsSimulator.NetType.BASIC, new PetriNet(
						getData().nodes, getData().arcs), getInvariantsList(),type, value);

		invSimulator.startSimulation(SimulatorMode.LOOP);
		
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public ArrayList<ArrayList<InvariantTransition>> getInvariantsList() {
		return invariants;
	}

	public void setInvariantsList(ArrayList<ArrayList<InvariantTransition>> invariants) {
		this.invariants = invariants;
	}
}
