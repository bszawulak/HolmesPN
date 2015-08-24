package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji związanych z miejscami.
 * @author students - statyczna wersja
 * @author MR - dynamiczna wersja
 */
public class PlacePopupMenu extends NodePopupMenu {
	private static final long serialVersionUID = -5062389148117837851L;

	/**
	 * Konstruktor obiektu klasy PlacePopupMenu.
	 * @param graphPanel GraphPanel - panel dla którego powstaje menu
	 */
	public PlacePopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, pne, el.getParentNode());

		/*
		if(pne == PetriNetElementType.TRANSITION || pne == PetriNetElementType.PLACE) {
			this.addMenuItem("Change selected Places into same Portals", "portal.png", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
						if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
								"Operation impossible when simulator is working."
								, "Warning") == true)
							return;
						
						//getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
						//GUIManager.getDefaultGUIManager().markNetChange();
					} else {
						JOptionPane.showMessageDialog(null, "Option possible if more than one place is selected.", "Too few selections", 
								JOptionPane.INFORMATION_MESSAGE);
					}
				}
			});
		}
		*/
		
		if(pne == PetriNetElementType.TRANSITION || pne == PetriNetElementType.PLACE) {
			this.addMenuItem("Clone this Place into Portal", "portal.png", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
						if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
								"Operation impossible when simulator is working."
								, "Warning") == true)
							return;
						
						//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
						getGraphPanel().getSelectionManager().cloneNodeIntoPortalV2();
						GUIManager.getDefaultGUIManager().markNetChange();
					} else {
						JOptionPane.showMessageDialog(null, "Option possible for one place only.", "Too many selections", 
								JOptionPane.INFORMATION_MESSAGE);
					}
					
				}
			});
		}
	}
}
