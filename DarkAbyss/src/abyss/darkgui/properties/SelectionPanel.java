package abyss.darkgui.properties;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.math.Arc;
import abyss.math.ElementLocation;

/**
 * Klasa implementuj�ca panel pokazuj�cy informacje o zaznaczonych mysz� elementach sieci.
 * @author students
 *
 */
public class SelectionPanel extends JPanel {
	private static final long serialVersionUID = -7388729615923711657L;
	private GUIManager guiManager;
	private DefaultListModel<String> selectedElementLocationList;
	private DefaultListModel<String> selectedArcList;

	/**
	 * Konstruktor domy�lny obiektu klasy SelectionPanel.
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
	 * Metoda zwracaj�ca obiekt managera interfejsu.
	 * @return GUIManager - obiekt managera
	 */
	protected GUIManager getGuiManager() {
		return guiManager;
	}

	/**
	 * Metoda ustwiaj�ca nowy obiekt managera interfejsu.
	 * @return GUIManager - obiekt managera
	 */
	protected void setGuiManager(GUIManager guiManager) {
		this.guiManager = guiManager;
	}
	
	/**
	 * Metoda zwraca list� z opisami wybranych wierzcho�k�w.
	 * @return DefaultListModel[String] - lista informacji o wierzcho�kach
	 */
	public DefaultListModel<String> getSelectedElementLocationList() {
		return selectedElementLocationList;
	}

	/**
	 * Metoda ustawia now� list� z opisami wybranych wierzcho�k�w.
	 * @param selectedElementLocationList DefaultListModel[String] - lista informacji o wierzcho�kach
	 */
	public void setSelectedElementLocationList(
			DefaultListModel<String> selectedElementLocationList) {
		this.selectedElementLocationList = selectedElementLocationList;
	}

	/**
	 * Metoda zwraca list� z opisami wybranych �uk�w.
	 * @return DefaultListModel[String] - lista informacji o �ukach
	 */
	public DefaultListModel<String> getSelectedArcList() {
		return selectedArcList;
	}

	/**
	 * Metoda ustawia now� list� z opisami wybranych �uk�w.
	 * @param selectedArcList DefaultListModel[String] - lista informacji o �ukach
	 */
	public void setSelectedArcList(DefaultListModel<String> selectedArcList) {
		this.selectedArcList = selectedArcList;
	}

	/**
	 * Metoda odpowiedzialna za wype�nienie tablic przechowuj�cych informacje
	 * o wybranych (zaznaczonych) przez u�ytkownika elementach sieci - �ukach
	 * i/lub wierzcho�kach. 
	 * @param e SelectionActionEvent - obiekt zdarzenia wywo�uj�cego
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
