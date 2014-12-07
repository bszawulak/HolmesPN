package abyss.darkgui.box;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.SelectionActionListener.SelectionActionEvent;
import abyss.math.Arc;
import abyss.math.ElementLocation;

/**
 * Klasa implementuj¹ca panel pokazuj¹cy zaznaczone elementy sieci.
 * @author students
 *
 */
public class SelectionPanel extends JPanel {
	private static final long serialVersionUID = -7388729615923711657L;
	
	private GUIManager guiManager;
	private DefaultListModel<String> selectedElementLocationList;
	private DefaultListModel<String> selectedArcList;

	/**
	 * Konstruktor domyœlny obiektu klasy SelectionPanel
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
	 * Metoda zwracaj¹ca obiekt managera interfejsu.
	 * @return GUIManager - obiekt managera
	 */
	protected GUIManager getGuiManager() {
		return guiManager;
	}

	/**
	 * Metoda ustwiaj¹ca nowy obiekt managera interfejsu.
	 * @return GUIManager - obiekt managera
	 */
	protected void setGuiManager(GUIManager guiManager) {
		this.guiManager = guiManager;
	}

	/**
	 * Metoda odpowiedzialna za coœ...
	 * @param e
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

	public DefaultListModel<String> getSelectedElementLocationList() {
		return selectedElementLocationList;
	}

	public void setSelectedElementLocationList(
			DefaultListModel<String> selectedElementLocationList) {
		this.selectedElementLocationList = selectedElementLocationList;
	}

	public DefaultListModel<String> getSelectedArcList() {
		return selectedArcList;
	}

	public void setSelectedArcList(DefaultListModel<String> selectedArcList) {
		this.selectedArcList = selectedArcList;
	}

}
