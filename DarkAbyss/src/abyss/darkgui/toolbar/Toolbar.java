package abyss.darkgui.toolbar;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import abyss.analyse.KnockoutCalculator;
import abyss.analyse.MCSCalculator;
import abyss.analyse.MCSCalculatorShort;
import abyss.analyse.MDTSCalculator;
import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel.DrawModes;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.utilities.Tools;

import com.javadocking.DockingManager;
import com.javadocking.dock.BorderDock;
import com.javadocking.dock.CompositeLineDock;
import com.javadocking.dock.LineDock;
import com.javadocking.dock.Position;
import com.javadocking.dock.docker.BorderDocker;
import com.javadocking.dock.factory.CompositeToolBarDockFactory;
import com.javadocking.dock.factory.ToolBarDockFactory;
import com.javadocking.dockable.ButtonDockable;
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
	private LineDock defaultHorizontalToolBarDock;
	private LineDock defaultVerticalToolBarDock;

	// simulator buttons
	ToolbarButtonAction reverseLoopButton, reverseStepButton, loopSimButton,
			singleTransitionLoopSimButton, pauseSimButton, stopSimButton,
			smallStepFwdSimButton, stepFwdSimButton, resetSimButton;

	// arrays
	ArrayList<ButtonDockable> buttonDockables;
	ArrayList<ButtonDockable> simulationDockables;
	ArrayList<ButtonDockable> analysisDockables;

	/**
	 * Konstruktor domyślny obiektu klasy Toolbar.
	 */
	public Toolbar() {
		guiManager = GUIManager.getDefaultGUIManager();
		maximizePanel = guiManager.getMaximizer();
		buttonDockables = new ArrayList<ButtonDockable>();

		// Create the buttons with a dockable around.
		loadButtons();
		simulationDockables = createSimulationBar();		
		allowOnlySimulationInitiateButtons(); //na początku aktywne tylko przyciski startu symulacji
		//w ponizszej metodzie dodawac kolejne przyciski analizy!
		analysisDockables = createAnalysisBar();

		BorderDock minimizerBorderDock = new BorderDock(new ToolBarDockFactory());
		minimizerBorderDock.setMode(BorderDock.MODE_MINIMIZE_BAR);
		minimizerBorderDock.setCenterComponent(maximizePanel);
		BorderDocker borderDocker = new BorderDocker();
		borderDocker.setBorderDock(minimizerBorderDock);

		setToolBarBorderDock(new BorderDock(new CompositeToolBarDockFactory(),minimizerBorderDock));
		getToolBarBorderDock().setMode(BorderDock.MODE_TOOL_BAR);
		horizontalCompositeToolBarDock = new CompositeLineDock(
			CompositeLineDock.ORIENTATION_HORIZONTAL, false,
			new ToolBarDockFactory(), DockingMode.HORIZONTAL_TOOLBAR,DockingMode.VERTICAL_TOOLBAR);
		verticalCompositeToolBarDock = new CompositeLineDock(
			CompositeLineDock.ORIENTATION_VERTICAL, false,
			new ToolBarDockFactory(), DockingMode.HORIZONTAL_TOOLBAR,DockingMode.VERTICAL_TOOLBAR);
		getToolBarBorderDock().setDock(horizontalCompositeToolBarDock,Position.TOP);
		getToolBarBorderDock().setDock(verticalCompositeToolBarDock,Position.LEFT);

		// The line docks for the buttons
		defaultHorizontalToolBarDock = new LineDock(LineDock.ORIENTATION_HORIZONTAL, false,
			DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);
		defaultVerticalToolBarDock = new LineDock(LineDock.ORIENTATION_VERTICAL, false,
			DockingMode.HORIZONTAL_TOOLBAR, DockingMode.VERTICAL_TOOLBAR);

		// add button to line docks
		addAllButtonDockablesHorizontally(buttonDockables, defaultHorizontalToolBarDock);

		// docking the line docks
		horizontalCompositeToolBarDock.addChildDock(defaultHorizontalToolBarDock, new Position(0));
		horizontalCompositeToolBarDock.addChildDock(createHorizontalBarDock(simulationDockables), new Position(1));
		verticalCompositeToolBarDock.addChildDock(defaultVerticalToolBarDock, new Position(0));
		//nowy pasek przyciskow:
		horizontalCompositeToolBarDock.addChildDock(createHorizontalBarDock(analysisDockables), new Position(2));
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
			//addButtonHorizontally(button, i, horizontalToolBarDock);
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
			//addButtonVertically(button, i);
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

	/**
	 * Metoda odpowiedzialna za tworzenie konkretnych instancji przycisków głównych.
	 */
	private void loadButtons() {
		//nowa zakładka
		@SuppressWarnings("serial")
		ToolbarButtonAction addButton = new ToolbarButtonAction(this, 
				"New tab", Tools.getResIcon48("/icons/toolbar/add_panel.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().newTab();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableAdd", addButton));
		
		@SuppressWarnings("serial")
		ToolbarButtonAction openButton = new ToolbarButtonAction(this,
				"Open project...", Tools.getResIcon48("/icons/toolbar/open.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.openAbyssProject();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableOpen", openButton));

		//import projektu ze snoopiego
		@SuppressWarnings("serial")
		ToolbarButtonAction importButton = new ToolbarButtonAction(this,
				"Import project...", Tools.getResIcon48("/icons/toolbar/import_net.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.importProject();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableImport", importButton));
		
		//zapis obrazu sieci do pliku
		@SuppressWarnings("serial")
		ToolbarButtonAction pictureButton = new ToolbarButtonAction(this,
				"Save picture...", Tools.getResIcon48("/icons/toolbar/save_picture.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.exportProjectToImage();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableImport", pictureButton));
		
		//odswiezanie
		@SuppressWarnings("serial")
		ToolbarButtonAction refreshButton = new ToolbarButtonAction(this,
				"Refresh", Tools.getResIcon48("/icons/toolbar/refresh.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().restoreDefaultVisuals();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableRefresh", refreshButton));
		
		//czyszczenie arkuszy
		@SuppressWarnings("serial")
		ToolbarButtonAction clearProject = new ToolbarButtonAction(this,
				"Clear project", Tools.getResIcon48("/icons/toolbar/clear_project.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().clearProject();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableRefresh", clearProject));
	}

	@SuppressWarnings("serial")
	/**
	 * Metoda odpowiedzialna za tworzenie tablicy przycisków symulatora.
	 * @return ArrayList[ButtonDockable] - tablica zawierająca obiekty przycisków
	 */
	private ArrayList<ButtonDockable> createSimulationBar() {
		ArrayList<ButtonDockable> simulationDockables = new ArrayList<ButtonDockable>();
		reverseLoopButton = new ToolbarButtonAction(this, "Loop back to oldest action saved",
				Tools.getResIcon48("/icons/toolbar/sim_back.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.LOOP_BACK);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStepBack",reverseLoopButton));
		
		reverseStepButton = new ToolbarButtonAction(this,"Single action back simulation",
				Tools.getResIcon48("/icons/toolbar/sim_back_step.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.ACTION_BACK);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableSmallStepBack", reverseStepButton));
		
		loopSimButton = new ToolbarButtonAction(this,"Loop simulation",
				Tools.getResIcon48("/icons/toolbar/sim_start.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.LOOP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableLoopSim",loopSimButton));
		
		singleTransitionLoopSimButton = new ToolbarButtonAction(this, "Loop single transition simulation", 
				Tools.getResIcon48("/icons/toolbar/sim_start_single.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableLoopSingleTransitionSim",
				singleTransitionLoopSimButton));
		
		pauseSimButton = new ToolbarButtonAction(this,"Pause simulation",
				Tools.getResIcon48("/icons/toolbar/sim_pause.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().pause();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockablePauseSim",pauseSimButton));
		
		stopSimButton = new ToolbarButtonAction(this,"Schedule a stop for the simulation",
				Tools.getResIcon48("/icons/toolbar/sim_stop.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().stop();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStopSim",stopSimButton));
		
		smallStepFwdSimButton = new ToolbarButtonAction(this,"Single transition forward simulation",
				Tools.getResIcon48("/icons/toolbar/sim_forward_step.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableSmallStepFwdSim", smallStepFwdSimButton));
		
		stepFwdSimButton = new ToolbarButtonAction(this,"Step forward simulation",
				Tools.getResIcon48("/icons/toolbar/sim_forward.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().setGraphMode(DrawModes.POINTER);
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().startSimulation(SimulatorMode.STEP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStepFwdSim", stepFwdSimButton));
		
		resetSimButton = new ToolbarButtonAction(this,"Reset simulator",
				Tools.getResIcon48("/icons/toolbar/sim_reset.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true)
					GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableResetSim", resetSimButton));
		
		return simulationDockables;
	}
	
	@SuppressWarnings("serial")
	/**
	 * Metoda odpowiedzialna za tworzenie tablicy przycisków analizatora.
	 * @return ArrayList[ButtonDockable] - tablica zawierająca obiekty przycisków
	 */
	private ArrayList<ButtonDockable> createAnalysisBar() {
		ArrayList<ButtonDockable> analysisDockables = new ArrayList<ButtonDockable>();
		ToolbarButtonAction generateINAinvariants = new ToolbarButtonAction(this, "Generate INA Invariants", 
				Tools.getResIcon48("/icons/toolbar/terminal.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.generateINAinvariants();
			}
		};
		analysisDockables.add(createButtonDockable("GenerateINAinv",generateINAinvariants));
		
		ToolbarButtonAction clusterButton = new ToolbarButtonAction(this, "Cluster analysis",
				Tools.getResIcon48("/icons/toolbar/clusters.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showClusterWindow(); 
			}
		};
		analysisDockables.add(createButtonDockable("Clusters", clusterButton));
		
		ToolbarButtonAction netTablesButton = new ToolbarButtonAction(this, "Net data tables",
				Tools.getResIcon48("/icons/toolbar/netTables.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showNetTablesWindow(); 
			}
		};
		analysisDockables.add(createButtonDockable("NetTables", netTablesButton));
		
		ToolbarButtonAction netSimLogButton = new ToolbarButtonAction(this, "Net simulation log",
				Tools.getResIcon32("/icons/toolbar/simLog.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showSimLogWindow();
			}
		};
		netSimLogButton.setEnabled(false);
		analysisDockables.add(createButtonDockable("SimLog", netSimLogButton));

		ToolbarButtonAction consoleButton = new ToolbarButtonAction(this, "Show console",
				Tools.getResIcon48("/icons/toolbar/terminal2.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showConsole(true); 

			}
		};
		analysisDockables.add(createButtonDockable("ShowConsole", consoleButton));
		
		ToolbarButtonAction cleanButton = new ToolbarButtonAction(this, "Clear all colors",
				Tools.getResIcon48("/icons/toolbar/cleanGraphColors.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().reset.clearGraphColors();
			}
		};
		analysisDockables.add(createButtonDockable("CleanColor", cleanButton));
		
		ToolbarButtonAction testButton = new ToolbarButtonAction(this, "Debug test purpose",
				Tools.getResIcon48("/icons/toolbar/clusterWindow.png")) {
			public void actionPerformed(ActionEvent actionEvent) {	
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().loadFromFile(
						"C:/Users/Rince/Desktop/Sieci/BER3.7.2/BERv3.8.2ab.spped");
				GUIManager.getDefaultGUIManager().getSimulatorBox().createSimulatorProperties();
				
				GUIManager.getDefaultGUIManager().setLastPath("C:/Users/Rince/Desktop/Sieci/BER3.7.2/");
				
				/*
				ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
				try
				{
					FileInputStream fis = new FileInputStream(
							"C:/Users/Rince/Desktop/Sieci/BER371/BER371table.acl");
					ObjectInputStream ois = new ObjectInputStream(fis);
					clusterMatrix = (ClusteringInfoMatrix) ois.readObject();
					ois.close();
					fis.close();
					GUIManager.getDefaultGUIManager().windowClusters.registerDataCase56(clusterMatrix);
				} catch (Exception ee) {}
				*/
				
			}
		};
		testButton.setEnabled(false);
		analysisDockables.add(createButtonDockable("Testing", testButton));
		
		
		ToolbarButtonAction testButton2 = new ToolbarButtonAction(this, "DEBUG2", Tools.getResIcon48("/icons/toolbar/a.png")) {
			@SuppressWarnings("unused")
			public void actionPerformed(ActionEvent actionEvent) { 
				GUIManager.getDefaultGUIManager().showMCS();
				//ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
				//Transition test = transitions.get(8);
				//MCSCalculatorShort mcs = new MCSCalculatorShort(2);
				//List<Set<Short>> sets = mcs.findMcs(16);

				//ArrayList<Transition> knockoutList = new ArrayList<Transition>();
				//knockoutList.add(test);
				//KnockoutCalculator kc = new KnockoutCalculator();
				//ArrayList<Transition> result = kc.calculateKnockout(knockoutList);
				
				//MDTSCalculator mdts = new MDTSCalculator();
				//List<Set<Integer>> results = mdts.calculateMDTS();
			}
		};
		analysisDockables.add(createButtonDockable("Testing2", testButton2));
		
		return analysisDockables;
	}

	/**
	 * Creates a dockable with a button as content.
	 * 
	 * @param id String - The ID of the dockable that has to be created.
	 * @param title - The title of the dialog that will be displayed.
	 * @param icon - The icon that will be put on the button.
	 * @param message - The message that will be displayed when the action is performed.
	 * @return ButtonDockable - The dockable with a button as content.
	 */
	private ButtonDockable createButtonDockable(String id,ToolbarButtonAction action) {
		// Create the action.
		// ToolbarButtonAction action = new ToolbarButtonAction(this, title, icon, message);
		// Create the button.
		ToolbarButton button = new ToolbarButton(action);
		// Create the dockable with the button as component.
		ButtonDockable buttonDockable = new ButtonDockable(id, button);
		// Add a dragger to the individual dockable.
		createDockableDragger(buttonDockable);
		return buttonDockable;
	}

	/**
	 * Metoda ustawia obiekt nasłuchujący, który monitoruje zdarzenia przeciągnięcia
	 * grupy przycisków lub jednego z jednej lokalizacji w inną.
	 * @param dockable Dockable - obiekt monitorowany
	 */
	private void createDockableDragger(Dockable dockable) {
		// Create the dragger for the dockable.
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
		//simulationDockables.get(8).getContent().setEnabled(enabled);
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
}
