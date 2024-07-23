package holmes.graphpanel.popupmenu;

import java.io.Serial;
import javax.swing.*;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.subnets.SubnetsActions;
import holmes.windows.HolmesNodeInfo;
import holmes.windows.xtpn.HolmesNodeInfoXTPN;

/**
 * Klasa odpowiedzialna za utworzenie menu kontekstowego dla wierzchołków sieci.
 */
public class NodePopupMenu extends GraphPanelPopupMenu {
	@Serial
	private static final long serialVersionUID = -8988739887642243733L;
	private static LanguageManager lang = GUIManager.getLanguageManager();
	private ElementLocation elocation;

	/**
	 * Konstruktor obiektu klasy PetriNetElementPopupMenu.
	 * @param graphPanel GraphPanel - obiekt panelu dla tworzonego menu
	 */
	public NodePopupMenu(GraphPanel graphPanel, ElementLocation eloc, PetriNetElementType pne, Object pneObject) {
		super(graphPanel, pne);
		this.elocation = eloc;
		
		if(pne != PetriNetElementType.ARC) {
			this.addMenuItem(lang.getText("NPM_entry001"), "", e -> {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().isEmpty())
					return;

				Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
				if(n instanceof Place) {
					if(n instanceof PlaceXTPN) {
						HolmesNodeInfoXTPN ani = new HolmesNodeInfoXTPN((PlaceXTPN) n, elocation, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} else {
						HolmesNodeInfo ani = new HolmesNodeInfo((Place)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					}
				} else if(n instanceof Transition) {
					if(n instanceof TransitionXTPN) {
						HolmesNodeInfoXTPN ani = new HolmesNodeInfoXTPN((TransitionXTPN) n, elocation, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} else {
						HolmesNodeInfo ani = new HolmesNodeInfo((Transition)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					}
				} else if(n instanceof MetaNode) {
					HolmesNodeInfo ani = new HolmesNodeInfo((MetaNode)n, GUIManager.getDefaultGUIManager().getFrame());
					ani.setVisible(true);
				}

				//} else {
				//	JOptionPane.showMessageDialog(null, "Warning: simulator active. Cannot proceed until manually stopped.",
				//			"Net simulator working", JOptionPane.WARNING_MESSAGE);
				//}
			});
		}
		
		if(pne != PetriNetElementType.META) {
			this.addMenuItem(lang.getText("delete"), "cross.png", e -> {
				if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
						lang.getText("NPM_entry002"), "Warning"))
					return;
				if(GUIManager.getDefaultGUIManager().reset.isXTPNSimulatorActiveWarning(
						lang.getText("NPM_entry003"), "Warning"))
					return;

				Object[] options = {lang.getText("delete"), lang.getText("cancel"),};
				int n = JOptionPane.showOptionDialog(null,
						lang.getText("NPM_entry004"), lang.getText("NPM_entry004t"), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0) {
					//GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
					getGraphPanel().getSelectionManager().deleteAllSelectedElements();
					getGraphPanel().getSelectionManager().deselectAllElementLocations();
					GUIManager.getDefaultGUIManager().markNetChange();
				}
			});
		}

		if(pne != PetriNetElementType.META) {
			this.addSeparator();
			this.add(cutMenuItem);
			this.add(copyMenuItem);
			this.add(pasteMenuItem);
		}

		if (graphPanel.getSelectionManager().getSelectedElementLocations().size() > 1 && graphPanel.getSelectionManager().getSelectedElementLocations().stream()
				.allMatch(location -> eloc.getParentNode() == location.getParentNode())) {
			this.addMenuItem(lang.getText("NPM_entry005"), "", e ->
					GUIManager.getDefaultGUIManager().subnetsHQ.mergePortals(eloc, graphPanel.getSelectionManager().getSelectedElementLocations())
			);
		}

		if (graphPanel.getSelectionManager().getSelectedElementLocations().stream()
				.map(ElementLocation::getParentNode)
				.filter(MetaNode.class::isInstance)
				.findAny().isEmpty() && !graphPanel.getSelectionManager().getSelectedElementLocations().isEmpty()
		) {
			this.addSeparator();

			this.addMenuItem(lang.getText("NPM_entry006"), "", e ->
					GUIManager.getDefaultGUIManager().subnetsHQ.createSubnetFromSelectedElements(graphPanel)
			);

			if (GUIManager.getDefaultGUIManager().subnetsHQ.getSubnetElementLocations(graphPanel.getSheetId()).stream()
					.anyMatch(location -> location.getParentNode() instanceof MetaNode)) {
				this.addMenuItem(lang.getText("NPM_entry007"), "", e ->
						SubnetsActions.openTransferElementsToSubnet(graphPanel, true)
				);
				this.addMenuItem(lang.getText("NPM_entry008"), "", e ->
						SubnetsActions.openTransferElementsToSubnet(graphPanel, false)
				);
				this.addMenuItem(lang.getText("NPM_entry009"), "", e ->
						SubnetsActions.openCopyElementsToSubnet(graphPanel)
				);
			}
		}
		this.addSeparator();
	}
}
