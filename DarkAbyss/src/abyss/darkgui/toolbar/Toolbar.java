package abyss.darkgui.toolbar;

import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import abyss.darkgui.GUIManager;
import abyss.math.simulator.NetSimulator.SimulatorMode;

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
 * Klasa odpowiedzialna za tworzenie paska narzêdzi zaraz poni¿ej paska menu
 * programu.
 * @author students
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
			smallStepFwdSimButton, stepFwdSimButton;

	// arrays
	ArrayList<ButtonDockable> buttonDockables;
	ArrayList<ButtonDockable> simulationDockables;
	ArrayList<ButtonDockable> analysisDockables;

	/**
	 * Konstruktor domyœlny obiektu klasy Toolbar.
	 */
	public Toolbar() {
		guiManager = GUIManager.getDefaultGUIManager();
		maximizePanel = guiManager.getMaximizer();
		buttonDockables = new ArrayList<ButtonDockable>();

		// Create the buttons with a dockable around.
		loadButtons();
		simulationDockables = createSimulationBar();		
		allowOnlySimulationInitiateButtons(); //na pocz¹tku aktywne tylko przyciski startu symulacji
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
	 * Metoda dodaj¹ca przyciski w poziomie do domyœlnego kontenera przycisków.
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
	 * Metoda odpowiedzialna za tworzenie konkretnych instancji przycisków g³ównych.
	 */
	private void loadButtons() {
		//nowa zak³adka
		@SuppressWarnings("serial")
		ToolbarButtonAction addButton = new ToolbarButtonAction(this, 
				"New tab", new ImageIcon("resources/icons/toolbar/add_panel.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().newTab();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableAdd", addButton));
		
		@SuppressWarnings("serial")
		ToolbarButtonAction openButton = new ToolbarButtonAction(this,
				"Open project...", new ImageIcon("resources/icons/toolbar/open.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().openProject();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableOpen", openButton));

		//import projektu ze snoopiego
		@SuppressWarnings("serial")
		ToolbarButtonAction importButton = new ToolbarButtonAction(this,
				"Import project...", new ImageIcon("resources/icons/toolbar/import_net.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().importProject();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableImport", importButton));
		
		//zapis obrazu sieci do pliku
		@SuppressWarnings("serial")
		ToolbarButtonAction pictureButton = new ToolbarButtonAction(this,
				"Save picture...", new ImageIcon("resources/icons/toolbar/save_picture.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().exportProjectToImage();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableImport", pictureButton));
		
		//odswiezanie
		@SuppressWarnings("serial")
		ToolbarButtonAction refreshButton = new ToolbarButtonAction(this,
				"Refresh", new ImageIcon("resources/icons/toolbar/refresh.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().restoreDefaultVisuals();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableRefresh", refreshButton));
		
		//czyszczenie arkuszy
		@SuppressWarnings("serial")
		ToolbarButtonAction clearProject = new ToolbarButtonAction(this,
				"Clear project", new ImageIcon("resources/icons/toolbar/clear_project.png")) {
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
	 * @return ArrayList[ButtonDockable] - tablica zawieraj¹ca obiekty przycisków
	 */
	private ArrayList<ButtonDockable> createSimulationBar() {
		ArrayList<ButtonDockable> simulationDockables = new ArrayList<ButtonDockable>();
		reverseLoopButton = new ToolbarButtonAction(this, "Loop back to oldest action saved",
			new ImageIcon( "resources/icons/toolbar/sim_back.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
					.getSimulator().startSimulation(SimulatorMode.LOOP_BACK);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStepBack",reverseLoopButton));
		
		reverseStepButton = new ToolbarButtonAction(this,"Single action back simulation",
				new ImageIcon("resources/icons/toolbar/sim_back_step.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
					.getSimulator().startSimulation(SimulatorMode.ACTION_BACK);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableSmallStepBack", reverseStepButton));
		
		loopSimButton = new ToolbarButtonAction(this,"Loop simulation",
				new ImageIcon("resources/icons/toolbar/sim_start.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
					.getSimulator().startSimulation(SimulatorMode.LOOP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableLoopSim",loopSimButton));
		
		singleTransitionLoopSimButton = new ToolbarButtonAction(this, "Loop single transition simulation", 
				new ImageIcon("resources/icons/toolbar/sim_start_single.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
					.getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableLoopSingleTransitionSim",
				singleTransitionLoopSimButton));
		
		pauseSimButton = new ToolbarButtonAction(this,"Pause simulation",
				new ImageIcon("resources/icons/toolbar/sim_pause.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().getSimulator().pause();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockablePauseSim",pauseSimButton));
		
		stopSimButton = new ToolbarButtonAction(this,"Schedule a stop for the simulation",
				new ImageIcon("resources/icons/toolbar/sim_stop.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
					.getSimulator().stop();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStopSim",stopSimButton));
		
		smallStepFwdSimButton = new ToolbarButtonAction(this,"Single transition forward simulation",
				new ImageIcon("resources/icons/toolbar/sim_forward_step.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator().startSimulation(SimulatorMode.SINGLE_TRANSITION);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableSmallStepFwdSim", smallStepFwdSimButton));
		
		stepFwdSimButton = new ToolbarButtonAction(this,"Step forward simulation",
				new ImageIcon("resources/icons/toolbar/sim_forward.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator().startSimulation(SimulatorMode.STEP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStepFwdSim", stepFwdSimButton));
		return simulationDockables;
	}
	
	@SuppressWarnings("serial")
	/**
	 * Metoda odpowiedzialna za tworzenie tablicy przycisków analizatora.
	 * @return ArrayList[ButtonDockable] - tablica zawieraj¹ca obiekty przycisków
	 */
	private ArrayList<ButtonDockable> createAnalysisBar() {
		ArrayList<ButtonDockable> analysisDockables = new ArrayList<ButtonDockable>();
		ToolbarButtonAction generateINAinvariants = new ToolbarButtonAction(this, "Generate INA Invariants", 
				new ImageIcon( "resources/icons/toolbar/terminal.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().generateINAinvariants();
			}
		};
		analysisDockables.add(createButtonDockable("GenerateINAinv",generateINAinvariants));
		
		ToolbarButtonAction clusterButton = new ToolbarButtonAction(this, "Cluster analysis",
				new ImageIcon("resources/icons/toolbar/clusters.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showClusterWindow(); 
			}
		};
		analysisDockables.add(createButtonDockable("Clusters", clusterButton));
		
		ToolbarButtonAction consoleButton = new ToolbarButtonAction(this, "Show console",
				new ImageIcon("resources/icons/toolbar/terminal2.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().showConsole(true); 

			}
		};
		analysisDockables.add(createButtonDockable("Test button", consoleButton));
		
		ToolbarButtonAction testButton = new ToolbarButtonAction(this, "Debug test purpose",
				new ImageIcon("resources/icons/toolbar/clusterWindow.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				//GUIManager.getDefaultGUIManager().generateAllCHindexes(20);
				//CHmetricReader chReader = new CHmetricReader();
				//chReader.executeReader("");
			}
		};
		testButton.setEnabled(false);
		analysisDockables.add(createButtonDockable("Testing", testButton));
		
		return analysisDockables;
	}

	/**
	 * Creates a dockable with a button as content.
	 * 
	 * @param id
	 *            The ID of the dockable that has to be created.
	 * @param title
	 *            The title of the dialog that will be displayed.
	 * @param icon
	 *            The icon that will be put on the button.
	 * @param message
	 *            The message that will be displayed when the action is
	 *            performed.
	 * @return The dockable with a button as content.
	 */
	private ButtonDockable createButtonDockable(String id,ToolbarButtonAction action) {
		// Create the action.
		// ToolbarButtonAction action = new ToolbarButtonAction(this, title,
		// icon, message);
		// Create the button.
		ToolbarButton button = new ToolbarButton(action);
		// Create the dockable with the button as component.
		ButtonDockable buttonDockable = new ButtonDockable(id, button);
		// Add a dragger to the individual dockable.
		createDockableDragger(buttonDockable);
		return buttonDockable;
	}

	/**
	 * Metoda ustawia obiekt nas³uchuj¹cy, który monitoruje zdarzenia przeci¹gniêcia
	 * grupy przycisków lub jednego z jednej lokalizacji w inn¹.
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
	 * Metoda ta ustawia stan wszystkich przycisków symulatora poza dwoma: pauz¹
	 * i przyciskiem zatrzymania symulacji.
	 * @param enabled boolean - true, jeœli maj¹ byæ aktywne
	 */
	public void setEnabledSimulationInitiateButtons(boolean enabled) {
		for (int i = 0; i < simulationDockables.size(); i++) {
			if (i != 4 && i != 5) //4 i 5 to pauza i stop
				simulationDockables.get(i).getContent().setEnabled(enabled);
		}
	}

	/**
	 * Metoda ta uaktywnia przyciski Pauza i Stop dla symulacji.
	 * @param enabled boolean - true jeœli Pauza i Stop maj¹ byæ aktywne
	 */
	public void setEnabledSimulationDisruptButtons(boolean enabled) {
		simulationDockables.get(4).getContent().setEnabled(enabled);
		simulationDockables.get(5).getContent().setEnabled(enabled);
	}

	/**
	 * Metoda odpowiedzialna za to, ¿e aktywne s¹ wszystkie przyciski 
	 * poza dwoma: Pauza i Stop dla symulatora
	 */
	public void allowOnlySimulationInitiateButtons() {
		setEnabledSimulationInitiateButtons(true);
		setEnabledSimulationDisruptButtons(false);
	}

	/**
	 * Metoda ta uaktywnia tylko przyciski Pauzy i Stopu, reszta nieaktywna - gdy dzia³a symulacja.
	 */
	public void allowOnlySimulationDisruptButtons() {
		setEnabledSimulationInitiateButtons(false);
		setEnabledSimulationDisruptButtons(true);
	}

	/**
	 * Metoda ustawia na aktywny tylko przycisk przerwania trwaj¹cej pauzy.
	 */
	public void allowOnlyUnpauseButton() {
		allowOnlySimulationDisruptButtons();
		simulationDockables.get(5).getContent().setEnabled(false);
	}
}
