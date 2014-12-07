package abyss.workspace;

import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener;
import abyss.graphpanel.GraphPanel.DrawModes;
import abyss.math.PetriNet;

import com.javadocking.dock.CompositeTabDock;
import com.javadocking.dock.Dock;
import com.javadocking.dock.Position;
import com.javadocking.dock.factory.DockFactory;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.Dockable;
import com.javadocking.dockable.DockingMode;

public class Workspace implements SelectionActionListener {
	@SuppressWarnings("unused")
	private static final long serialVersionUID = -7304351849692823097L;

	// misc
	private DockFactory dockFactory;

	// arrays
	private ArrayList<Dockable> dockables;
	private ArrayList<Dock> docks;
	private ArrayList<WorkspaceSheet> sheets;
	private ArrayList<Integer> sheetsIDtable;

	// filler
//	private WorkspaceFiller filler;
	private Dock fillerDock;
	private Dockable fillerDockable;

	// project data
	private PetriNet project;

	// manager
	private GUIManager guiManager;

	// composite tab dock
	private CompositeTabDock workspaceDock;

	public Workspace(GUIManager gui) {
		setWorkspaceDock(new CompositeTabDock());
		guiManager = gui;
		setDockFactory(getWorkspaceDock().getChildDockFactory());
		dockables = new ArrayList<Dockable>();
		docks = new ArrayList<Dock>();
		sheets = new ArrayList<WorkspaceSheet>();
		sheetsIDtable = new ArrayList<Integer>();

		// filler = new WorkspaceFiller();
		setFillerDockable(new DefaultDockable("Workspace",
				new WorkspaceFiller(), "Workspace"));
		setFillerDock(getDockFactory().createDock(getFillerDockable(), DockingMode.SINGLE));
		Point position = new Point(0, 0);
		getFillerDock().addDockable(getFillerDockable(), position, position);

		setProject(new PetriNet(this));
		this.getProject().addActionListener(this);

		newTab();

	}

	public int newTab() {
		int index = sheetsIDtable.size();
		int id = index;
		if (sheetsIDtable.indexOf(id) != -1)
			id = getMaximumTabIndex() + 1;
		Point position = new Point(0, 0);
		sheetsIDtable.add(id);
		sheets.add(new WorkspaceSheet("I am sheet " + Integer.toString(id), id, this));
		Dockable tempDockable = new DefaultDockable("Sheet "
				+ Integer.toString(id), sheets.get(index), "Sheet "
				+ Integer.toString(id));
		dockables.add(index, withListener(tempDockable));
		docks.add(getDockFactory().createDock(dockables.get(index),
				DockingMode.SINGLE));
		docks.get(index).addDockable(dockables.get(index), position, position);
		getWorkspaceDock().addChildDock(docks.get(index), new Position(index));
		// add menu item to the menu
		guiManager.getMenu().addSheetItem(dockables.get(index));
		return id;
	}

	public void setGraphMode(DrawModes mode) {
		this.getProject().setDrawMode(mode);
	}

	public void deleteSheetFromArrays(WorkspaceSheet sheet) {
		int id = sheets.indexOf(sheet) + 1 - 1;
		getProject().removeGraphPanel(id);
		getWorkspaceDock().emptyChild(docks.get(id));
		docks.remove(id);
		sheets.remove(id);
		sheetsIDtable.remove(id);
	}

	public void deleteTab(Dockable dockable) {
		int index = dockables.indexOf(dockable);
		int n = JOptionPane
				.showOptionDialog(
						null,
						"Are you sure you want to delete this sheet? You will not be able to retrieve it later.",
						"Are you sure?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);
		if ((n == 0) && (sheets.size() > 1)) {
			deleteSheetFromArrays(sheets.get(index));
			JOptionPane.showMessageDialog(null, "Sheet deleted.");
			guiManager.getMenu().deleteSheetItem(dockables.get(index));
		} else {
			if (sheets.size() == 1 && n == 0)
				JOptionPane
						.showMessageDialog(
								null,
								"Can't delete this sheet! A project must contain at least one sheet!",
								"Can't delete this sheet!",
								JOptionPane.ERROR_MESSAGE);
			Point position = new Point(0, 0);
			dockables.set(
					index,
					withListener(new DefaultDockable("Sheet "
							+ Integer.toString(sheets.get(index).getId()),
							sheets.get(index), "Sheet "
									+ Integer.toString(sheets.get(index)
											.getId()))));

			docks.get(index).addDockable(dockables.get(index), position,
					position);
		}

	}

	private Dockable withListener(Dockable dockable) {
		Dockable wrapper = guiManager.decorateDockableWithActions(dockable, true);
		wrapper.addDockingListener(GUIManager.getDefaultGUIManager().getDockingListener());
		return wrapper;
	}

	private int getMaximumTabIndex() {
		int index = 0;
		for (int x : sheetsIDtable) {
			if (x > index)
				index = x;
		}
		return index;
	}

	public ArrayList<Dockable> getDockables() {
		return dockables;
	}


	public ArrayList<WorkspaceSheet> getSheets() {
		return sheets;
	}

	/**
	 * Metoda zwracaj¹ca obiekt zawieraj¹cy sieæ Petriego.
	 * @return PetriNet - obiekt z danymi sieci
	 */
	public PetriNet getProject() {
		return project;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt zawieraj¹cy sieæ Petriego.
	 * @return PetriNet - obiekt z danymi sieci
	 */
	private void setProject(PetriNet project) {
		this.project = project;
	}

	/**
	 * Metoda zwracaj¹ca ostatni indeks arkusza w programie.
	 * @param id int - nr arkusza
	 * @return int - nr arkusza. Nie, te¿ nie ogarniam o co tu chodzi (MR).
	 */
	public int getIndexOfId(int id) {
		Integer ajDi = new Integer(id);
		return sheetsIDtable.lastIndexOf(ajDi);
	}

	public void redockSheets() {
		int i = 0;
		Point position = new Point(0, 0);
		setWorkspaceDock(new CompositeTabDock());
		setDockFactory(getWorkspaceDock().getChildDockFactory());
		for (WorkspaceSheet sheet : getSheets()) {
			Dock dock = docks.get(sheet.getId());
			Dockable dockable = dockables.get(sheet.getId());
			dockable.setDock(null);
			dock.addDockable(dockable, position, position);
			getWorkspaceDock().addChildDock(dock, new Position(i));
			i++;
		}
	}

	/**
	 * Metoda odpowiedzialna za zainicjowanie sekwencji rozkazów zwi¹zanych z wyœwietleniem
	 * w³aœciwoœci (lub wype³nieniem którychœ podokien zawartoœci¹) w zale¿noœci od tego, co
	 * w³aœnie zosta³o klikniête.
	 * @param e SelectionActionEvent - zdarzenie wyboru czegoœ mysz¹
	 */
	public void actionPerformed(SelectionActionEvent e) {
		guiManager.getPropertiesBox().selectElement(e);
		guiManager.getSelectionBox().getSelectionPanel().actionPerformed(e);
	}

	/**
	 * Metoda s³u¿¹ca do pobierania aktualnie klikniêtego arkusza sieci.
	 * @return WorkspaceSheet - klikniêty arkusz
	 */
	public WorkspaceSheet getSelectedSheet() {
		int index = docks.indexOf(workspaceDock.getSelectedDock());
		return sheets.get(index);
	}

	/**
	 * Metoda zwracaj¹ca obiekt dokowalny.
	 * @return CompositeTabDock - obiekt
	 */
	public CompositeTabDock getWorkspaceDock() {
		return workspaceDock;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt dokowalny.
	 * @return CompositeTabDock - obiekt
	 */
	private void setWorkspaceDock(CompositeTabDock workspaceDock) {
		this.workspaceDock = workspaceDock;
	}

	/**
	 * Metoda inicjuj¹ca przerysowanie wszystkich paneli.
	 */
	public void repaintAllGraphPanels() {
		this.getProject().repaintAllGraphPanels();
	}

	/**
	 * Metoda zwiêkszaj¹ca krok symulacji.
	 */
	public void incrementSimulationStep() {
		this.getProject().incrementSimulationStep();
	}

	/**
	 * Metoda zwracaj¹ca obiekt dokowalny.
	 * @return Dock - obiekt
	 */
	public Dock getFillerDock() {
		return fillerDock;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt dokowalny.
	 * @return Dock - obiekt
	 */
	public void setFillerDock(Dock fillerDock) {
		this.fillerDock = fillerDock;
	}

	/**
	 * Metoda zwracaj¹ca obiekt dokowalny-wype³niaj¹cy.
	 * @return Dock - obiekt
	 */
	public Dockable getFillerDockable() {
		return fillerDockable;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt dokowalny-wype³niaj¹cy.
	 * @return Dock - obiekt
	 */
	public void setFillerDockable(Dockable fillerDockable) {
		this.fillerDockable = fillerDockable;
	}

	/**
	 * Metoda zwracaj¹ca obiekt fabryki dokowalnej.
	 * @return Dock - obiekt
	 */
	public DockFactory getDockFactory() {
		return dockFactory;
	}

	/**
	 * Metoda ustawiaj¹ca nowy fabryki dokowalnej.
	 * @return Dock - obiekt
	 */
	private void setDockFactory(DockFactory dockFactory) {
		this.dockFactory = dockFactory;
	}
}
