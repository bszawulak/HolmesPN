package holmes.graphpanel.popupmenu;

import java.io.Serial;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
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
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	
	/**
	 * Konstruktor obiektu klasy PlacePopupMenu.
	 * @param graphPanel (<b>GraphPanel</b>) panel, dla którego powstaje menu.
	 */
	public PlacePopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, el, pne, el.getParentNode());
		
		this.addMenuItem(lang.getText("PPM_entry001"), "smallInvisibility.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().isEmpty())
				return;

			Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
			if(n instanceof Place) {
				n.setInvisibility(!n.isInvisible());
			}
			overlord.getWorkspace().repaintAllGraphPanels();
		});
		
		if(pne == PetriNetElementType.TRANSITION || pne == PetriNetElementType.PLACE) {
			this.addMenuItem(lang.getText("PPM_entry002"), "portal.png", e -> {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
					if(overlord.reset.isSimulatorActiveWarning(
							lang.getText("PPM_entry003")
							, lang.getText("warning")))
						return;

					if(overlord.reset.isXTPNSimulatorActiveWarning(
							lang.getText("PPM_entry004")
							, lang.getText("warning")))
						return;

					//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
					getGraphPanel().getSelectionManager().cloneNodeIntoPortalV2();
					overlord.markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, lang.getText("PPM_entry005"), lang.getText("PPM_entry006"),
							JOptionPane.INFORMATION_MESSAGE);
				}
			});
		}

		this.addMenuItem(lang.getText("PPM_entry007"), "cut.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
				if(overlord.reset.isSimulatorActiveWarning(
						lang.getText("PPM_entry003"), lang.getText("warning")))
					return;
				if(overlord.reset.isXTPNSimulatorActiveWarning(
						lang.getText("PPM_entry004"), lang.getText("warning")))
					return;

				getGraphPanel().getSelectionManager().saveSubnet();
				overlord.markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, lang.getText("PPM_entry008"), lang.getText("PPM_entry009"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
