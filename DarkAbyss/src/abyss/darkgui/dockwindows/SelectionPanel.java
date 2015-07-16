package abyss.darkgui.dockwindows;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.math.pnElements.Arc;
import abyss.math.pnElements.ElementLocation;

/**
 * Klasa implementująca panel pokazujący informacje o zaznaczonych myszą elementach sieci.
 * @author students
 *
 */
public class SelectionPanel extends JPanel {
	private static final long serialVersionUID = -7388729615923711657L;
	private GUIManager guiManager;
	private DefaultListModel<String> selectedElementLocationList;
	private DefaultListModel<String> selectedArcList;

	/**
	 * Konstruktor domyślny obiektu klasy SelectionPanel.
	 */
	public SelectionPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		//add(new JLabel("No elements selected."));
		this.setGuiManager(GUIManager.getDefaultGUIManager());
		this.setSelectedElementLocationList(new DefaultListModel<String>());
		this.setSelectedArcList(new DefaultListModel<String>());
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		JList elementLocationList = new JList(this.getSelectedElementLocationList());
		this.add(elementLocationList);

		@SuppressWarnings({ "unchecked", "rawtypes" })
		JList arcList = new JList(this.getSelectedArcList());
		this.add(arcList);
	}

	/**
	 * Metoda zwracająca obiekt managera interfejsu.
	 * @return GUIManager - obiekt managera
	 */
	protected GUIManager getGuiManager() {
		return guiManager;
	}

	/**
	 * Metoda ustwiająca nowy obiekt managera interfejsu.
	 * @return GUIManager - obiekt managera
	 */
	protected void setGuiManager(GUIManager guiManager) {
		this.guiManager = guiManager;
	}
	
	/**
	 * Metoda zwraca listę z opisami wybranych wierzchołków.
	 * @return DefaultListModel[String] - lista informacji o wierzchołkach
	 */
	public DefaultListModel<String> getSelectedElementLocationList() {
		return selectedElementLocationList;
	}

	/**
	 * Metoda ustawia nową listę z opisami wybranych wierzchołków.
	 * @param selectedElementLocationList DefaultListModel[String] - lista informacji o wierzchołkach
	 */
	public void setSelectedElementLocationList(DefaultListModel<String> selectedElementLocationList) {
		this.selectedElementLocationList = selectedElementLocationList;
	}

	/**
	 * Metoda zwraca listę z opisami wybranych łuków.
	 * @return DefaultListModel[String] - lista informacji o łukach
	 */
	public DefaultListModel<String> getSelectedArcList() {
		return selectedArcList;
	}

	/**
	 * Metoda ustawia nową listę z opisami wybranych łuków.
	 * @param selectedArcList DefaultListModel[String] - lista informacji o łukach
	 */
	public void setSelectedArcList(DefaultListModel<String> selectedArcList) {
		this.selectedArcList = selectedArcList;
	}

	/**
	 * Metoda odpowiedzialna za wypełnienie tablic przechowujących informacje
	 * o wybranych (zaznaczonych) przez użytkownika elementach sieci - łukach
	 * i/lub wierzchołkach. 
	 * @param e SelectionActionEvent - obiekt zdarzenia wywołującego
	 */
	public void actionPerformed(SelectionActionEvent e) {
		this.getSelectedElementLocationList().clear();
		this.getSelectedArcList().clear();
		if (e.getElementLocationGroup() != null && e.getElementLocationGroup().size() > 0) {			
			for (ElementLocation el : e.getElementLocationGroup())
				this.getSelectedElementLocationList().addElement(el.toString());
		}
		if (e.getArcGroup() != null && e.getArcGroup().size() > 0) {			
			for (Arc a : e.getArcGroup())
				this.getSelectedArcList().addElement(a.toString());
		}
	}
}
