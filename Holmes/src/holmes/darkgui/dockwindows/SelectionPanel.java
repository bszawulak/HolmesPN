package holmes.darkgui.dockwindows;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.SelectionActionListener.SelectionActionEvent;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;

import java.io.Serial;

/**
 * Klasa implementująca panel pokazujący informacje o zaznaczonych myszą elementach sieci.
 */
public class SelectionPanel extends JPanel {
	@Serial
	private static final long serialVersionUID = -7388729615923711657L;
	private GUIManager guiManager = GUIManager.getDefaultGUIManager();
	private static LanguageManager lang = GUIManager.getLanguageManager();
	private DefaultListModel<String> selectedElementLocationList;
	private DefaultListModel<String> selectedArcList;

	/**
	 * Konstruktor domyślny obiektu klasy SelectionPanel.
	 */
	public SelectionPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JLabel(lang.getText("SP_toolTip001")));
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
		if (e.getElementLocationGroup() != null && !e.getElementLocationGroup().isEmpty()) {
			for (ElementLocation el : e.getElementLocationGroup())
				this.getSelectedElementLocationList().addElement(el.toString());
		}
		if (e.getArcGroup() != null && !e.getArcGroup().isEmpty()) {
			for (Arc a : e.getArcGroup())
				this.getSelectedArcList().addElement(a.toString());
		}
	}
}
