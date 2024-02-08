package holmes.workspace;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.SelectionActionListener;
import holmes.graphpanel.GraphPanel.DrawModes;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.MetaNode.MetaType;

/**
 * Głowna klasa odpowiedzialna za zarządzanie przestrzenią programu, w której rysowana jest sieć Petriego. 
 * Posiada w sobie między innymi zakładki otwarte w programie. Stanowi szczytowe osiągnięcie w kwestii tego
 * jak nie zarządzać obiektami, vide: dockables, docks, sheets i sheetsIDtable. Wypada zapytać: tylko 4
 * tablice *ŚCIŚLE POWIĄZANYCH ZE SOBĄ* obiektów? Czemu nie 40? Acha, 2 z nich są osobnymi tablicami w DarkMenu, 
 * które należy aktualizować przy każdej zmianie liczby zakładek. Rozum nie jest w stanie tego ogarnąć, jak
 * powiedział król Desmod zaglądając po skończonej potrzebie do nocnika.
 * 30.06.2023 : po wywaleniu Javadocking zaczyna (powoli) wracać prostota i zdrowy rozsądek. Komentarze
 * pozostają - ku przestrodze.
 */
public class Workspace implements SelectionActionListener {
	private JTabbedPane tp = new JTabbedPane();
	
	/** Tablica zawierająca obiekty WorkspaceSheet, które z kolei zawierają SheetPanel (JPanel) oraz GraphPanel. By żyło się lepiej. */
	private ArrayList<WorkspaceSheet> sheets;
	
	/** Tablica identyfikatorów obiektów WorkspaceSheet przechowywanych w tablicy sheets */
	private ArrayList<Integer> sheetsIDtable;
	private PetriNet project;
	private GUIManager overlord;

	/**
	 * Konstruktor obiektu klasy Workspace.
	 * @param gui GUIManager - obiekt managera środowiska graficznego programu
	 */
	public Workspace(GUIManager gui) {
		overlord = gui;
		sheets = new ArrayList<WorkspaceSheet>();
		sheetsIDtable = new ArrayList<Integer>();

		Point position = new Point(0, 0);

		setProject(new PetriNet(this, "default"));

		setTablePane(new JTabbedPane());
		gui.setTabbedWorkspace(getTablePane());

		this.getProject().addActionListener(this);
	}

	/**
	 * Metoda odpowiedzialna za utworzenie nowej zakładki sieci w projekcie.
	 * @param addMetaNode boolean - true, jeśli do sieci I ma być dodany meta-węzeł
	 * @return int - numer będący identyfikatorem nowej zakładki
	 */
	public int newTab(boolean addMetaNode, Point pos, int whichSubnet, MetaType type) {
		int index = sheetsIDtable.size();
		int id = index;
		if (sheetsIDtable.contains(id))
			id = getMaximumSubnetID() + 1;

		//Point position = new Point(0, 0);

		sheetsIDtable.add(id);

		WorkspaceSheet ws = new WorkspaceSheet("Subnet " + id, id, this);
		sheets.add(ws);

		JScrollPane scroll = new JScrollPane(ws.getContainerPanel());
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		ws.setScrollPane(scroll);
		tp.add("Subnet " + id, scroll);

		if(addMetaNode) {
			addMetaNode(pos, whichSubnet, id, type);
		}
		return id;
	}

	/**
	 * Metoda dodaje nowy graficzny token metanode.
	 * @param pos Point - pozycja XY
	 * @param whichSubnet - do której sieci należy metanode
	 * @param representedSubnet - jaką podsieć reprezentuje
	 * @param type MetaType - jaki typ podsieci
	 */
	private void addMetaNode(Point pos, int whichSubnet, int representedSubnet, MetaType type) {
		MetaNode metanode = new MetaNode(whichSubnet, IdGenerator.getNextId(), pos, type);
		metanode.setRepresentedSheetID(representedSubnet);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes().add(metanode);
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}

	/**
	 * Metoda odpowiedzialna za ustawienie nowego trybu rysowania sieci, zależna od
	 * tego, co wybrano z prawego panelu narzedziowego programu.
	 * @param mode DrawModes - aktualny tryb rysowania sieci
	 */
	public void setGraphMode(DrawModes mode) {
		this.getProject().setDrawMode(mode);
	}

	/**
	 * Metoda usuwa dane arkusza z listy arkuszy obiektu klasy Workspace.
	 * @param sheet WorkspaceSheet - arkusz do usunięcia
	 */
	public void deleteSheetFromArrays(WorkspaceSheet sheet) {
		//int gpIndex = getProject().getGraphPanels().indexOf(sheet.getGraphPanel());
		int sheetID = sheet.getId();
		boolean result = getProject().removeGraphPanel(sheetID);
		if(!result) {
			GUIManager.getDefaultGUIManager().log("Error, removing graph panel in Workspace.deleteSheetFromArrays() failed" +
					"for WorkspaceSheet "+sheet.getId(), "error", true);
		}
		
		int id = sheets.indexOf(sheet);
		sheets.remove(id);
		sheetsIDtable.remove(id);
		tp.remove(id);
	}

	/**
	 * Używana przez GUIReset, do czyszczenia wszystkich paneli sieci poza pierwszym.
	 */
	public void deleteAllSheetButFirst() {
		for(WorkspaceSheet sheet : sheets) { //TODO 03072023 coś nie działa jak się doda podsieć, przy wyjściu z programu, sprawdzić
			if(sheet.getId() != 0) {
				int sheetID = sheet.getId();
				boolean result = getProject().removeGraphPanel(sheetID);
				if(!result) {
					GUIManager.getDefaultGUIManager().log("Error, removing graph panel in Workspace.deleteSheetFromArrays() failed" +
							"for WorkspaceSheet "+sheet.getId(), "error", true);
				}
				int id = sheets.indexOf(sheet);
				sheets.remove(id);
				sheetsIDtable.remove(id);
				tp.remove(id);
			}

		}
	}

	public void globalDeselection() {
		for(WorkspaceSheet ws : sheets) {
			ws.getGraphPanel().getSelectionManager().deselectAllElements();
		}
	}

	/**
	 * Metoda zwraca maksymalną wartość identyfikatora z tablicy zakładek (czyli
	 * id ostatniej dodanej)
	 * @return int - id ostatniej zakładki
	 */
	public int getMaximumSubnetID() {
		int index = 0;
		for (int x : sheetsIDtable) {
			if (x > index)
				index = x;
		}
		return index;
	}
	
	public ArrayList<Integer> accessSheetsIDtable() {
		return sheetsIDtable;
	}

	/**
	 * Metoda zwraca tablicę zadokowanych zakładek w Workspace.
	 * @return ArrayList[WorkspaceSheet] - tablica zakładek
	 */
	public ArrayList<WorkspaceSheet> getSheets() {
		return sheets;
	}

	/**
	 * Metoda zwracająca obiekt zawierający sieć Petriego.
	 * @return PetriNet - obiekt z danymi sieci
	 */
	public PetriNet getProject() {
		return project;
	}

	/**
	 * Metoda ustawiająca nowy obiekt zawierający sieć Petriego.
	 */
	private void setProject(PetriNet project) {
		this.project = project;
	}

	/**
	 * Metoda zwracająca ostatni indeks arkusza w programie.
	 * @param id int - id arkusza
	 * @return int - pozycja arkusza na liście sheetsIDtable
	 */
	public int getIndexOfId(int id) {
		Integer ajDi = id; //było: new Integer(id)
		return sheetsIDtable.lastIndexOf(ajDi); //wymuszenie odpowiedniej metody przez boxing inta w Integer
	}

	/**
	 * Metoda odpowiedzialna za zainicjowanie sekwencji rozkazów związanych z wyświetleniem
	 * właściwości (lub wypełnieniem którychś podokien zawartością) w zależności od tego, co
	 * właśnie zostało kliknięte.
	 * @param e SelectionActionEvent - zdarzenie wyboru elementu myszą
	 */
	public void actionPerformed(SelectionActionEvent e) {
		overlord.getPropertiesBox().selectElement(e);
		overlord.getSelectionBox().getSelectionPanel().actionPerformed(e);
	}

	/**
	 * Metoda służąca do pobierania aktualnie klikniętego arkusza sieci.
	 * @return WorkspaceSheet - kliknięty arkusz
	 */
	public WorkspaceSheet getSelectedSheet() {
		try {
			//TODO wybór
			int index = 0;// docks.indexOf(workspaceDock.getSelectedDock());
			return sheets.get(index);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Metoda inicjująca przerysowanie wszystkich paneli.
	 */
	public void repaintAllGraphPanels() {
		this.getProject().repaintAllGraphPanels();
	}

	/**
	 * Pobieranie obiektu zakładek sieci.
	 * @return JTabbedPane - obiekt z zakładami.
	 */
	public JTabbedPane getTablePane() {
		return tp;
	}

	/**
	 * Ustawianie referencji głównego obiektu zakładek.
	 * @param tp (<b>JTabbedPane</b>) - obiekt zakładek.
	 */
	public void setTablePane(JTabbedPane tp) {
		this.tp = tp;
	}
}
