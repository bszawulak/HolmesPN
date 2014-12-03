package abyss.darkgui.shortcuts;

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

public class ShortcutsBar extends BorderDock {
	private static final long serialVersionUID = 640320332920131092L;
	private GUIManager guiManager;
	private SingleMaximizer maximizePanel;
	private BorderDock toolBarBorderDock;
	private CompositeLineDock horizontalCompositeToolBarDock;
	private CompositeLineDock verticalCompositeToolBarDock;
	private LineDock defaultHorizontalToolBarDock;
	private LineDock defaultVerticalToolBarDock;

	// simulator buttons
	ToolbarButtonAction przyciskI, przyciskII, loopSimButton,
			singleTransitionLoopSimButton, pauseSimButton, stopSimButton,
			smallStepFwdSimButton, stepFwdSimButton;

	// arrays
	ArrayList<ButtonDockable> buttonDockables;
	ArrayList<ButtonDockable> simulationDockables;
	ArrayList<ButtonDockable> analysisDockables;

	public ShortcutsBar() {
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

	public void addAllButtonDockablesHorizontally(ArrayList<ButtonDockable> buttons, LineDock horizontalToolBarDock) {
		int i = 0;
		for (ButtonDockable button : buttons) {
			horizontalToolBarDock.addDockable(button, new Position(i));
			//addButtonHorizontally(button, i, horizontalToolBarDock);
			i++;
		}
	}

	public void addAllButtonDockablesVertically(
			ArrayList<ButtonDockable> buttons) {
		int i = 0;
		for (ButtonDockable button : buttons) {
			addButtonVertically(button, i);
			i++;
		}
	}

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

	//unused, zastapiona bezposrednim poleceniem MR
	public void addButtonHorizontally(ButtonDockable button, int index,
			LineDock horizontalToolBarDock) {
		horizontalToolBarDock.addDockable(button, new Position(index));
	}

	public void addButtonVertically(ButtonDockable button, int index) {
		defaultVerticalToolBarDock.addDockable(button, new Position(index));
	}

	private void loadButtons() {
		//nowa zak³adka
		@SuppressWarnings("serial")
		ToolbarButtonAction addButton = new ToolbarButtonAction(this, "New tab", new ImageIcon("resources/icons/add.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().newTab();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableAdd", addButton));
		
		//import projektu ze snoopiego
		@SuppressWarnings("serial")
		ToolbarButtonAction importButton = new ToolbarButtonAction(this,
				"Import project...", new ImageIcon("resources/icons/import.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().importProject();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableImport",importButton));
		
		//odswiezanie
		@SuppressWarnings("serial")
		ToolbarButtonAction refreshButton = new ToolbarButtonAction(this,
				"Refresh", new ImageIcon("resources/icons/refresh.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().restoreDefaultVisuals();
			}
		};
		buttonDockables.add(createButtonDockable("ButtonDockableRefresh",
				refreshButton));
	}

	@SuppressWarnings("serial")
	private ArrayList<ButtonDockable> createSimulationBar() {
		ArrayList<ButtonDockable> simulationDockables = new ArrayList<ButtonDockable>();
		przyciskI = new ToolbarButtonAction(
				this, "Loop back to oldest action saved",
				new ImageIcon( "resources/icons/simulation_icons/control_step_bck_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator()
						.startSimulation(SimulatorMode.LOOP_BACK);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStepBack",
				przyciskI));
		przyciskII = new ToolbarButtonAction(
				this,
				"Single action back simulation",
				new ImageIcon(
						"resources/icons/simulation_icons/control_bck_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator()
						.startSimulation(SimulatorMode.ACTION_BACK);
			}
		};
		simulationDockables.add(createButtonDockable(
				"ButtonDockableSmallStepBack", przyciskII));
		loopSimButton = new ToolbarButtonAction(
				this,
				"Loop simulation",
				new ImageIcon(
						"resources/icons/simulation_icons/control_repeat_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator().startSimulation(SimulatorMode.LOOP);
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableLoopSim",
				loopSimButton));
		singleTransitionLoopSimButton = new ToolbarButtonAction(this,
				"Loop single transition simulation", new ImageIcon(
						"resources/icons/simulation_icons/control_repeat.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator()
						.startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
			}
		};
		simulationDockables.add(createButtonDockable(
				"ButtonDockableLoopSingleTransitionSim",
				singleTransitionLoopSimButton));
		pauseSimButton = new ToolbarButtonAction(
				this,
				"Pause simulation",
				new ImageIcon(
						"resources/icons/simulation_icons/control_pause_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator().pause();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockablePauseSim",
				pauseSimButton));
		stopSimButton = new ToolbarButtonAction(
				this,
				"Schedule a stop for the simulation",
				new ImageIcon(
						"resources/icons/simulation_icons/control_stop_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator().stop();
			}
		};
		simulationDockables.add(createButtonDockable("ButtonDockableStopSim",
				stopSimButton));
		smallStepFwdSimButton = new ToolbarButtonAction(
				this,
				"Single transition forward simulation",
				new ImageIcon(
						"resources/icons/simulation_icons/control_fwd_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator()
						.startSimulation(SimulatorMode.SINGLE_TRANSITION);
			}
		};
		simulationDockables.add(createButtonDockable(
				"ButtonDockableSmallStepFwdSim", smallStepFwdSimButton));
		stepFwdSimButton = new ToolbarButtonAction(
				this,
				"Step forward simulation",
				new ImageIcon(
						"resources/icons/simulation_icons/control_step_fwd_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject()
						.getSimulator().startSimulation(SimulatorMode.STEP);
			}
		};
		simulationDockables.add(createButtonDockable(
				"ButtonDockableStepFwdSim", stepFwdSimButton));
		return simulationDockables;
	}
	
	@SuppressWarnings("serial")
	private ArrayList<ButtonDockable> createAnalysisBar() {
		ArrayList<ButtonDockable> analysisDockables = new ArrayList<ButtonDockable>();
		przyciskI = new ToolbarButtonAction(
				this, "Podpowiedz I",
				new ImageIcon( "resources/icons/simulation_icons/control_step_bck_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				//GUIManager.getDefaultGUIManager().getWorkspace().getProject()
				//		.getSimulator().startSimulation(SimulatorMode.LOOP_BACK);
			}
		};
		analysisDockables.add(createButtonDockable("Przycisk I",przyciskI));
		przyciskII = new ToolbarButtonAction(
				this, "Podpowiedz przycisku II",
				new ImageIcon("resources/icons/simulation_icons/control_bck_blue.png")) {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				//GUIManager.getDefaultGUIManager().getWorkspace().getProject()
				//	.getSimulator().startSimulation(SimulatorMode.ACTION_BACK);
			}
		};
		analysisDockables.add(createButtonDockable("Przycisk II", przyciskII));
		
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
		ToolBarButton button = new ToolBarButton(action);
		// Create the dockable with the button as component.
		ButtonDockable buttonDockable = new ButtonDockable(id, button);
		// Add a dragger to the individual dockable.
		createDockableDragger(buttonDockable);
		return buttonDockable;
	}

	/**
	 * Adds a drag listener on the content component of a dockable.
	 */
	private void createDockableDragger(Dockable dockable) {
		// Create the dragger for the dockable.
		DragListener dragListener = DockingManager.getDockableDragListenerFactory().createDragListener(dockable);
		dockable.getContent().addMouseListener(dragListener);
		dockable.getContent().addMouseMotionListener(dragListener);
	}

	public BorderDock getToolBarBorderDock() {
		return toolBarBorderDock;
	}

	private void setToolBarBorderDock(BorderDock toolBarBorderDock) {
		this.toolBarBorderDock = toolBarBorderDock;
	}

	public void setEnabledSimulationInitiateButtons(boolean enabled) {
		for (int i = 0; i < simulationDockables.size(); i++) {
			if (i != 4 && i != 5) //4 i 5 to pauza i stop
				simulationDockables.get(i).getContent().setEnabled(enabled);
		}
	}

	/**
	 * uaktywnia przyciski Pauza i Stop dla symulacji
	 * @param enabled
	 */
	public void setEnabledSimulationDisruptButtons(boolean enabled) {
		simulationDockables.get(4).getContent().setEnabled(enabled);
		simulationDockables.get(5).getContent().setEnabled(enabled);
	}

	/**
	 * Na pocz¹tku aktywne wszystkie przyciski poza dwoma: Pauza i Stop dla
	 * symulatora
	 */
	public void allowOnlySimulationInitiateButtons() {
		setEnabledSimulationInitiateButtons(true);
		setEnabledSimulationDisruptButtons(false);
	}

	/**
	 * Uaktywnia tylko przyciski Pauzy i Stopu, reszta nieaktywna - gdy dzia³a symulacja
	 */
	public void allowOnlySimulationDisruptButtons() {
		setEnabledSimulationInitiateButtons(false);
		setEnabledSimulationDisruptButtons(true);
	}

	public void allowOnlyUnpauseButton() {
		allowOnlySimulationDisruptButtons();
		simulationDockables.get(5).getContent().setEnabled(false);
	}
}
