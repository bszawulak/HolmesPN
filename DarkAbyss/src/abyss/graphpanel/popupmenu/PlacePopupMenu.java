package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji związanych z miejscami.
 * @author students
 *
 */
public class PlacePopupMenu extends NodePopupMenu {
	// BACKUP:  -5062389148117837851L   (ŁAPY PRECZ OD PONIŻSZEJ ZMIENNEJ)
	private static final long serialVersionUID = -5062389148117837851L;

	/**
	 * Konstruktor obiektu klasy PlacePopupMenu.
	 * @param graphPanel GraphPanel - panel dla którego powstaje menu
	 */
	public PlacePopupMenu(GraphPanel graphPanel) {
		super(graphPanel);
		
		
		
		this.addMenuItem("Change selected Places into P-Portals", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
					GUIManager.getDefaultGUIManager().markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible if more than one place is selected.", "Too few selections", 
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		
		this.addMenuItem("Clone one Place into Portal", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
					GUIManager.getDefaultGUIManager().markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible for one place only.", "Too many selections", 
							JOptionPane.INFORMATION_MESSAGE);
				}
				
			}
		});
	}
}
