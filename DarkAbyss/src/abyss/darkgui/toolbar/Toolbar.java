package abyss.darkgui.toolbar;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTree;

import abyss.analyse.MDTSCalculator;
import abyss.darkgui.GUIManager;
import abyss.files.io.ProjectWriter;
import abyss.graphpanel.GraphPanel.DrawModes;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.utilities.Tools;
import abyss.varia.NetworkTransformations;
import abyss.windows.AbyssNotepad;

import com.javadocking.DockingManager;
import com.javadocking.dock.BorderDock;
import com.javadocking.dock.CompositeLineDock;
import com.javadocking.dock.LineDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.docker.BorderDocker;
import com.javadocking.dock.factory.CompositeToolBarDockFactory;
import com.javadocking.dock.factory.ToolBarDockFactory;
import com.javadocking.dockable.ButtonDockable;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockingMode;
import com.javadocking.drag.DragListener;
import com.javadocking.visualizer.SingleMaximizer;

/**
 * Klasa odpowiedzialna za tworzenie paska narzędzi zaraz poniżej paska menu programu.
 * @author students
 * @author MR
 *
 */
public class Toolbar extends BorderDock {
	private static final long serialVersionUID = 640320332920131092L;
	private GUIManager guiManager;
	private SingleMaximizer maximizePanel;
	private BorderDock toolBarBorderDock;
	private CompositeLineDock horizontalCompositeToolBarDock;
	private CompositeLineDock verticalCompositeToolBarDock;
	private LineDock defaultVerticalToolBarDock;
	
	private boolean buttonsDraggable = false;

	// simulator buttons
	ToolbarButtonAction reverseLoopButton, reverseStepButton, loopSimButton,
			singleTransitionLoopSimButton, pauseSimButton, stopSimButton,
			smallStepFwdSimButton, stepFwdSimButton, resetSimButton;

	// arrays
	ArrayList<ButtonDockable> ioButtonsDockables;
	ArrayList<ButtonDockable> analysisDockables;
	ArrayList<ButtonDockable> netTransformDockables;
	
	ArrayList<ButtonDockable> simulationDockables;

	/**
	 * Konstruktor domyślny obiektu klasy Toolbar.
	 */
	public Toolbar() {
		guiManager = GUIManager.getDefaultGUIManager();
		maximizePanel = guiManager.getMaximizer();
		ioButtonsDockables = new ArrayList<ButtonDockable>();

		createIObuttons();

		BorderDock minimizerBorderDock = new BorderDock(new ToolBarDockFactory());
		minimizerBorderDock.setMode(BorderDock.MODE_MINIMIZE_BAR);
		minimizerBorderDock.setCenterComponent(maximizePanel);
		BorderDocker borderDocker = new BorderDocker();
		borderDocker.setBorderDock(minimizerBorderDock);

		setToolBarBorderDock(new BorderDock(new CompositeToolBarDockFactory(),minimizerBorderDock));
		getToolBarBorderDock().setMode(BorderDock.MODE_TOOL_BAR);
		horizontalCompositeToolBarDock = new CompositeLineDock(CompositeLineDock.ORIENTATION_HORIZONTAL, false,
			new ToolBarDockFactory(), DockingMode.HORIZONTAL_TOOLBAR,DockingMode.VERTICAL_TOOLBAR);
		verticalCompositeToolBarDock = new CompositeLineDock(CompositeLineDock.ORIENTATION_VERTICAL, false,
			new ToolBarDockFactory(), DockingMode.HORIZONTAL_TOOLBAR,DockingMode.VERTICAL_TOOLBAR);
		getToolBarBorderDock().setDock(horizontalCompositeToolBarDock,Position.TOP);
		getToolBarBorderDock().setDock(verticalCompositeToolBarDock,Position.LEFT);

		// The line docks for the buttons
		//defaultHorizontalToolBarDock = new LineDock(LineDock.ORIENTATION_HORIZONTAL, false,
		//	DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		
		defaultVerticalToolBarDock = new LineDock(LineDock.ORIENTATION_VERTICAL, false,
			DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);

		//addAllButtonDockablesHorizontally(buttonDockables, defaultHorizontalToolBarDock);
		//horizontalCompositeToolBarDock.addChildDock(defaultHorizontalToolBarDock, new Position(0));
		
		//simulationDockables = createSimulationBar();
		//allowOnlySimulationInitiateButtons(); //na początku aktywne tylko przyciski startu symulacji
		//horizontalCompositeToolBarDock.addChildDock(createHorizontalBarDock(simulationDockables), new Position(1));
		
		ioButtonsDockables = createIObuttons();
		horizontalCompositeToolBarDock.addChildDock(createHorizontalBarDock(ioButtonsDockables), new Position(0));
		
		netTransformDockables = createNetTransormBar();
		horizontalCompositeToolBarDock.addChildDock(createHorizontalBarDock(netTransformDockables), new Position(1));
		
		analysisDockables = createAnalysisBar();
		horizontalCompositeToolBarDock.addChildDock(createHorizontalBarDock(analysisDockables), new Position(2));

		
		
		verticalCompositeToolBarDock.addChildDock(defaultVerticalToolBarDock, new Position(0));
		//horizontalCompositeToolBarDock.addChildDock(createHorizontalBarDockVaria(createSubtoolsPanel()), new Position(2));
	}

	/**
	 * Metoda odpowiedzialna za dodawanie nowych przycisków w poziomie do podanego kontenera.
	 * @param buttons ArrayList[ButtonDockable] - tablica przycisków do dodania
	 * @param horizontalToolBarDock LineDock - obiekt kontenera przycisków
	 */
	public void addAllButtonDockablesHorizontally(ArrayList<ButtonDockable> buttons, LineDock horizontalToolBarDock) {
		int i = 0;
		for (ButtonDockable button : buttons) {
			horizontalToolBarDock.addDockable(button, new Position(i));
			i++;
		}
	}

	/**
	 * Metoda odpowiedzialna za dodawanie nowych przycisków w pionie.
	 * @param buttons ArrayList[ButtonDockable] - tablica przycisków do dodania
	 */
	public void addAllButtonDockablesVertically(ArrayList<ButtonDockable> buttons) {
		int i = 0;
		for (ButtonDockable button : buttons) {
			defaultVerticalToolBarDock.addDockable(button, new Position(i));
			i++;
		}
	}

	/**
	 * Metoda dodająca przyciski w poziomie do domyślnego kontenera przycisków.
	 * @param buttons ArrayList[ButtonDockable] - tablica przycisków
	 * @return LineDock - obiekt kontera przycisków
	 */
	public LineDock createHorizontalBarDock(ArrayList<ButtonDockable> buttons) {
		LineDock horizontalToolBarDock = new LineDock();
		int i = 0;
		for (ButtonDockable button : buttons) {
			horizontalToolBarDock.addDockable(button, new Position(i));
			//addButtonHorizontally(button, i, horizontalToolBarDock);
			i++;
		}
		return horizontalToolBarDock;
	}
	
	public LineDock createHorizontalBarDockVaria(JPanel panel) {
		LineDock horizontalToolBarDock = new LineDock();
		Dockable dockable1 = new DefaultDockable("IDPanel1", panel, "PanelDockable", null, DockingMode.ALL); 
		// DockingMode.LINE? http://www.javadocking.com/javadoc/index.html
		horizontalToolBarDock.addDockable(dockable1, new Position(0));
		return horizontalToolBarDock;
	}

	/**
	 * Metoda odpowiedzialna za tworzenie konkretnych instancji przycisków głównych.
	 */
	private ArrayList<ButtonDockable> createIObuttons() {
		ArrayList<ButtonDockable> ioDockables = new ArrayList<ButtonDockable>();
		//nowa zakładka
		ToolbarButtonAction addButton = new ToolbarButtonAction(this, 
				"New tab", "Add new network tab/sheet", Tools.getResIcon48("/icons/toolbar/add_panel.png")) {
					private static final long serialVersionUID = -3039335266465055547L;

			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().newTab();
			}
		};
		ioDockables.add(createButtonDockable("ButtonDockableAdd", addButton));
		
		ToolbarButtonAction openButton = new ToolbarButtonAction(this,
				"Open project...", "Open Abyss project file (.abyss)", Tools.getResIcon48("/icons/toolbar/open.png")) {
					private static final long serialVersionUID = -8017306615290773915L;

			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.openAbyssProject();
			}
		};
		ioDockables.add(createButtonDockable("ButtonDockableOpen", openButton));
		
		//import projektu ze snoopiego
		ToolbarButtonAction importButton = new ToolbarButtonAction(this,
				"Import project...", "Import Petri net saved in other file formats", Tools.getResIcon48("/icons/toolbar/import_net.png")) {
					private static final long serialVersionUID = 5723070117312880726L;

			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.importProject();
			}
		};
		ioDockables.add(createButtonDockable("ButtonDockableImport", importButton));
		
		//zapis obrazu sieci do pliku
		ToolbarButtonAction pictureButton = new ToolbarButtonAction(this,
				"Save picture...", "Save the network as picture", Tools.getResIcon48("/icons/toolbar/save_picture.png")) {
					private static final long serialVersionUID = 932011484445458070L;

			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.exportProjectToImage();
			}
		};
		ioDockables.add(createButtonDockable("ButtonDockableImport", pictureButton));
		ToolbarButtonAction refreshButton = new ToolbarButtonAction(this,
				"Refresh", "Refresh all graph panels with net structures", Tools.getResIcon48("/icons/toolbar/refresh.png")) {
					private static final long serialVersionUID = -5107926050446252487L;

			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
			}
		};
		ioDockables.add(createButtonDockable("ButtonDockableRefresh", refreshButton));

		ToolbarButtonAction clearProject = new ToolbarButtonAction(this,
				"Clear project", "Clears all data of the project", Tools.getResIcon48("/icons/toolbar/clear_project.png")) {
					private static final long serialVersionUID = 2969893062492924300L;

			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().reset.newProjectInitiated();
			}
		};
		ioDockables.add(createButtonDockable("ButtonDockableRefresh", clearProject));
		
		return ioDockables;
	}

	@SuppressWarnings("serial")
	/**
	 * Metoda odpowiedzialna za tworzenie tablicy przycisków analizatora.
	 * @return ArrayList[ButtonDockable] - tablica zawierająca obiekty przycisków
	 */
	private ArrayList<ButtonDockable> createAnalysisBar() {
		ArrayList<ButtonDockable> analysisDockables = new ArrayList<ButtonDockable>();
		ToolbarButtonAction generateINAinvariants = new ToolbarButtonAction(this, "GenerateINA", "Generate invariants using INA", 
				Tools.getResIcon48("/icons/toolbar/terminal.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.generateINAinvariants();
			}
		};
		analysisDockables.add(createButtonDockable("GenerateINAinv",generateINAinvariants));
		
		ToolbarButtonAction clusterButton = new ToolbarButtonAction(this, "ClusterAnalysis", "Cluster creation and analysis", 
				Tools.getResIcon48("/icons/toolbar/clusters.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showClusterWindow(); 
			}
		};
		analysisDockables.add(createButtonDockable("Clusters", clusterButton));
		
		ToolbarButtonAction netTablesButton = new ToolbarButtonAction(this, "NetDataTables", "Show net data as tables", 
				Tools.getResIcon48("/icons/toolbar/netTables.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showNetTablesWindow(); 
			}
		};
		analysisDockables.add(createButtonDockable("NetTables", netTablesButton));
		
		ToolbarButtonAction netSimLogButton = new ToolbarButtonAction(this, "NetSimLog", "Network simulation log", 
				Tools.getResIcon32("/icons/toolbar/simLog.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showSimLogWindow();
			}
		};
		netSimLogButton.setEnabled(false);
		analysisDockables.add(createButtonDockable("SimLog", netSimLogButton));

		ToolbarButtonAction consoleButton = new ToolbarButtonAction(this, "ShowConsole", "Show program log console", 
				Tools.getResIcon48("/icons/toolbar/terminal2.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showConsole(true); 

			}
		};
		analysisDockables.add(createButtonDockable("ShowConsole", consoleButton));
		
		ToolbarButtonAction cleanButton = new ToolbarButtonAction(this, "ClearColors", "Restore net default colors", 
				Tools.getResIcon48("/icons/toolbar/cleanGraphColors.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().reset.clearGraphColors();
			}
		};
		analysisDockables.add(createButtonDockable("CleanColor", cleanButton));
		
		ToolbarButtonAction testButton = new ToolbarButtonAction(this, "Debug1", "Debug", 
				Tools.getResIcon48("/icons/toolbar/clusterWindow.png")) {
			public void actionPerformed(ActionEvent actionEvent) {	
				ProjectWriter pWrt = new ProjectWriter();
				pWrt.writeProject("test.apf");
			}
		};
		testButton.setEnabled(false);
		analysisDockables.add(createButtonDockable("Testing", testButton));
		
		ToolbarButtonAction testButton2 = new ToolbarButtonAction(this, "DEBUG2", "Debug2", Tools.getResIcon48("/icons/toolbar/a.png")) {
			@SuppressWarnings("unused")
			public void actionPerformed(ActionEvent actionEvent) 
			{ 
				JTree test = GUIManager.getDefaultGUIManager().getToolBox().getTree();
				GUIManager.getDefaultGUIManager().getToolBox().selectPointer();
				//test.setSelectionPath(new TreePath());
				int x = 1;
				
				MDTSCalculator mdts = new MDTSCalculator();
				ArrayList<Set<Integer>> results = mdts.calculateMDTS();
				
				AbyssNotepad notePad = new AbyssNotepad(900,600);
				notePad.setVisible(true);
				
				notePad.addTextLineNL("", "text");
				notePad.addTextLineNL("Maximal Dependend Transition sets:", "text");
				String text = "";
				int setNo = 0;
				for(Set<Integer> set : results) {
					text = "";
					setNo++;
					text += "Set #"+setNo+": [";
					for(int i : set) {
						text += "t"+i+", ";
					}
					text += "]";
					text = text.replace(", ]", "]");
					notePad.addTextLineNL(text, "text");
				}
				
				
			}
		};
		analysisDockables.add(createButtonDockable("Testing2", testButton2));
		
		return analysisDockables;
	}
	
	@SuppressWarnings("serial")
	private ArrayList<ButtonDockable> createNetTransormBar() {
		ArrayList<ButtonDockable> analysisDockables = new ArrayList<ButtonDockable>();
		ToolbarButtonAction extendNetButton = new ToolbarButtonAction(this, "ExtNet",  "Extend the net by 10%",
				Tools.getResIcon32("/icons/toolbar/resizeMax.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				NetworkTransformations nt = new NetworkTransformations();
				nt.extendNetwork(true);
			}
		};
		analysisDockables.add(createButtonDockable("EXTnetButton", extendNetButton));
		
		ToolbarButtonAction shrinkNetButton = new ToolbarButtonAction(this, "ShrNet", "Shrink the net by 10%", 
				Tools.getResIcon32("/icons/toolbar/resizeMin.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				NetworkTransformations nt = new NetworkTransformations();
				nt.extendNetwork(false);
			}
		};
		analysisDockables.add(createButtonDockable("SHRButton", shrinkNetButton));
		
		ToolbarButtonAction gridButton = new ToolbarButtonAction(this, "ShowGrid", "Show grid line", 
				Tools.getResIcon32("/icons/toolbar/grid.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("gridLines").equals("1"))
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("gridLines", "0", true);
				else
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("gridLines", "1", true);
				
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
			}
		};
		analysisDockables.add(createButtonDockable("GridButton", gridButton));

		ToolbarButtonAction gridAlignButton = new ToolbarButtonAction(this, "GridAlign", "Align net to grid line", 
				Tools.getResIcon32("/icons/toolbar/gridAlign.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				NetworkTransformations nt = new NetworkTransformations();
				nt.alignNetToGrid();
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
			}
		};
		analysisDockables.add(createButtonDockable("GridAlignButton", gridAlignButton));
		
		
		return analysisDockables;
	}
	
	/**
	 * Tworzy dokowalny (lub nie) przycisk. 
	 * @param id String - identyfikator obiektu dokowalnego
	 * @param action ToolbarButtonAction - obiekt przycisku
	 * @return ButtonDockable - otoczka na przycisk umożliwiająca dokowanie
	 */
	private ButtonDockable createButtonDockable(String id, ToolbarButtonAction action) {
		ToolbarButton button = new ToolbarButton(action);
		ButtonDockable buttonDockable = new ButtonDockable(id, button);
		if(buttonsDraggable)
			createDockableDragger(buttonDockable);
		
		return buttonDockable;
	}

	/**
	 * Metoda ustawia obiekt nasłuchujący, który monitoruje zdarzenia przeciągnięcia obiektu.
	 * grupy przycisków lub jednego z jednej lokalizacji w inną.
	 * @param dockable Dockable - obiekt monitorowany
	 */
	private void createDockableDragger(Dockable dockable) {
		DragListener dragListener = DockingManager.getDockableDragListenerFactory().createDragListener(dockable);
		dockable.getContent().addMouseListener(dragListener);
		dockable.getContent().addMouseMotionListener(dragListener);
	}

	/**
	 * Metoda zwraca obiekt dokowalny kontenera przycisków.
	 * @return BorderDock - obiekt
	 */
	public BorderDock getToolBarBorderDock() {
		return toolBarBorderDock;
	}

	/**
	 * Metoda ustawia nowy obiekt dokowalny kontenera przycisków.
	 * @param toolBarBorderDock BorderDock - obiekt
	 */
	private void setToolBarBorderDock(BorderDock toolBarBorderDock) {
		this.toolBarBorderDock = toolBarBorderDock;
	}

	//***************************************************************************************************
	//***************************************************************************************************
	//***************************************************************************************************
	//***************************************************************************************************
	//***************************************************************************************************
	
	/**
	 * Metoda ta ustawia stan wszystkich przycisków symulatora poza dwoma: pauzą
	 * i przyciskiem zatrzymania symulacji.
	 * @param enabled boolean - true, jeśli mają być aktywne
	 */
	public void setEnabledSimulationInitiateButtons(boolean enabled) {
		for (int i = 0; i < simulationDockables.size(); i++) {
			if (i != 4 && i != 5) //4 i 5 to pauza i stop
				simulationDockables.get(i).getContent().setEnabled(enabled);
		}
	}

	/**
	 * Metoda ta uaktywnia przyciski Pauza i Stop dla symulacji.
	 * @param enabled boolean - true jeśli Pauza i Stop mają być aktywne
	 */
	public void setEnabledSimulationDisruptButtons(boolean enabled) {
		simulationDockables.get(4).getContent().setEnabled(enabled);
		simulationDockables.get(5).getContent().setEnabled(enabled);
	}

	/**
	 * Metoda odpowiedzialna za to, że aktywne są wszystkie przyciski 
	 * poza dwoma: Pauza i Stop dla symulatora
	 */
	public void allowOnlySimulationInitiateButtons() {
		setEnabledSimulationInitiateButtons(true);
		setEnabledSimulationDisruptButtons(false);
	}

	/**
	 * Metoda ta uaktywnia tylko przyciski Pauzy i Stopu, reszta nieaktywna - gdy działa symulacja.
	 */
	public void allowOnlySimulationDisruptButtons() {
		setEnabledSimulationInitiateButtons(false);
		setEnabledSimulationDisruptButtons(true);
	}

	/**
	 * Metoda ustawia na aktywny tylko przycisk przerwania trwającej pauzy.
	 */
	public void allowOnlyUnpauseButton() {
		allowOnlySimulationDisruptButtons();
		simulationDockables.get(5).getContent().setEnabled(false);
	}
	
	
	
	@SuppressWarnings({ "serial", "unused" })
	/**
	 * Metoda odpowiedzialna za tworzenie tablicy przycisków symulatora.
	 * @return ArrayList[ButtonDockable] - tablica zawierająca obiekty przycisków
	 */
	private ArrayList<ButtonDockable> createSimulationBar() {
		ArrayList<ButtonDockable> simulationDockables = new ArrayList<ButtonDockable>();
		reverseLoopButton = new ToolbarButtonAction(this, "LoopBack", "Loop back to oldest action saved",
				Tools.getResIcon48("/icons/toolbar/sim_back.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.LOOP_BACK);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStepBack",reverseLoopButton));
		
		reverseStepButton = new ToolbarButtonAction(this, "StepBack", "Single action back simulation",
				Tools.getResIcon48("/icons/toolbar/sim_back_step.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.ACTION_BACK);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableSmallStepBack", reverseStepButton));
		
		loopSimButton = new ToolbarButtonAction(this, "Loop", "Loop simulation",
				Tools.getResIcon48("/icons/toolbar/sim_start.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.LOOP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableLoopSim",loopSimButton));
		
		singleTransitionLoopSimButton = new ToolbarButtonAction(this, "LoopSingleTrans", "Loop single transition simulation", 
				Tools.getResIcon48("/icons/toolbar/sim_start_single.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableLoopSingleTransitionSim",
				singleTransitionLoopSimButton));
		
		pauseSimButton = new ToolbarButtonAction(this, "Pause", "Pause simulation",
				Tools.getResIcon48("/icons/toolbar/sim_pause.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().pause();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockablePauseSim",pauseSimButton));
		
		stopSimButton = new ToolbarButtonAction(this, "Stop", "Schedule a stop for the simulation",
				Tools.getResIcon48("/icons/toolbar/sim_stop.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().stop();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStopSim",stopSimButton));
		
		smallStepFwdSimButton = new ToolbarButtonAction(this, "SingleForward", "Single transition forward simulation",
				Tools.getResIcon48("/icons/toolbar/sim_forward_step.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableSmallStepFwdSim", smallStepFwdSimButton));
		
		stepFwdSimButton = new ToolbarButtonAction(this, "StepForward", "Step forward simulation",
				Tools.getResIcon48("/icons/toolbar/sim_forward.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.STEP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStepFwdSim", stepFwdSimButton));
		
		resetSimButton = new ToolbarButtonAction(this, "Reset", "Reset simulator",
				Tools.getResIcon48("/icons/toolbar/sim_reset.png")) {
			public void actionPerformed(ActionEvent actionEvent) {
				if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true)
					GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableResetSim", resetSimButton));
		
		return simulationDockables;
	}
	
	@SuppressWarnings("unused")
	private JPanel createSubtoolsPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		panel.setPreferredSize(new Dimension(200, 60));
		panel.setMaximumSize(new Dimension(200, 60));
		
		JButton button1 = new JButton("Ext.Net");
		button1.setName("extNet");
		button1.setPreferredSize(new Dimension(60,40));
		//button1.setBounds(0, 0, 60, 60);
		button1.setToolTipText("Extend net elements");
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				
			}
		});
		panel.add(button1);
		
		return panel;
	}
}
