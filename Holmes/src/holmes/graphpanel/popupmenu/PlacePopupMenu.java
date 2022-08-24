package holmes.graphpanel.popupmenu;

import java.io.Serial;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Place;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji związanych z miejscami.
 */
public class PlacePopupMenu extends NodePopupMenu {
	@Serial
	private static final long serialVersionUID = -5062389148117837851L;

	/**
	 * Konstruktor obiektu klasy PlacePopupMenu.
	 * @param graphPanel (<b>GraphPanel</b>) panel, dla którego powstaje menu.
	 */
	public PlacePopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, el, pne, el.getParentNode());
		
		this.addMenuItem("Invisibility (invariants) ON/OFF", "smallInvisibility.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
				return;

			Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
			if(n instanceof Place) {
				n.setInvisibility(!n.isInvisible());
			}
			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		
		if(pne == PetriNetElementType.TRANSITION || pne == PetriNetElementType.PLACE) {
			this.addMenuItem("Clone this Place into Portal", "portal.png", e -> {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning"))
						return;

					if(GUIManager.getDefaultGUIManager().reset.isXTPNSimulatorActiveWarning(
							"Operation impossible when XTPN simulator is working."
							, "Warning"))
						return;

					//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
					getGraphPanel().getSelectionManager().cloneNodeIntoPortalV2();
					GUIManager.getDefaultGUIManager().markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible for one place only.", "Too many selections",
							JOptionPane.INFORMATION_MESSAGE);
				}

			});
		}

		this.addMenuItem("Export subnet to File", "cut.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
				if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
						"Operation impossible when simulator is working."
						, "Warning"))
					return;
				if(GUIManager.getDefaultGUIManager().reset.isXTPNSimulatorActiveWarning(
						"Operation impossible when XTPN simulator is working."
						, "Warning"))
					return;

				getGraphPanel().getSelectionManager().saveSubnet();
				GUIManager.getDefaultGUIManager().markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, "Option possible for one transition only.", "Too many selections",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
