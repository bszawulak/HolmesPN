package holmes.graphpanel.popupmenu;

import java.io.Serial;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.Arc.TypeOfArc;
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
	private ElementLocation elocation;

	/**
	 * Konstruktor obiektu klasy PetriNetElementPopupMenu.
	 * @param graphPanel GraphPanel - obiekt panelu dla tworzonego menu
	 */
	public NodePopupMenu(GraphPanel graphPanel, ElementLocation eloc, PetriNetElementType pne, Object pneObject) {
		super(graphPanel, pne);
		this.elocation = eloc;
		
		if(pne != PetriNetElementType.ARC) {
			this.addMenuItem("Show details...", "", e -> {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
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
		
		boolean proceed = true;
		if(pne == PetriNetElementType.ARC) {
			Arc arc = (Arc)pneObject;
			if(arc.getArcType() == TypeOfArc.META_ARC)
				proceed = false;
		}
		
		if(pne != PetriNetElementType.META && proceed) {
			this.addMenuItem("Delete", "cross.png", e -> {
				if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
						"Operation impossible when simulator is working.", "Warning"))
					return;
				if(GUIManager.getDefaultGUIManager().reset.isXTPNSimulatorActiveWarning(
						"Operation impossible when XTPN simulator is working.", "Warning"))
					return;

				Object[] options = {"Delete", "Cancel",};
				int n = JOptionPane.showOptionDialog(null,
						"Do you want to delete selected elements?", "Deletion warning?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
				if (n == 0) {
					//GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
					getGraphPanel().getSelectionManager().deleteAllSelectedElements();
					getGraphPanel().getSelectionManager().deselectAllElementLocations();
					GUIManager.getDefaultGUIManager().markNetChange();
				}
			});
		}
		this.addSeparator();
		if(pne != PetriNetElementType.META) {
			this.add(cutMenuItem);
			this.add(copyMenuItem);
			this.add(pasteMenuItem);
		}

		if(pne == PetriNetElementType.META && graphPanel.getSelectionManager().getSelectedElementLocations().size() == 1) {
			this.addMenuItem("Delete", "cross.png", e -> {
						Object[] options = {"Delete", "Cancel",};
						int n = JOptionPane.showOptionDialog(null,
								"Do you want to delete selected subnet?", "Deletion warning?", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) {
							GUIManager.getDefaultGUIManager().subnetsHQ.deleteSubnet(graphPanel.getSelectionManager().getSelectedMetanode());
							//GUIManager.getDefaultGUIManager().markNetChange();
						}
					}
			);

			this.addMenuItem("Unwrap ", "", e -> {
						Object[] options = {"Unwrap", "Cancel",};
						int n = JOptionPane.showOptionDialog(null,
								"Do you want to unwrap selected subnet?", "Unwrapping warning?", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) {
							GUIManager.getDefaultGUIManager().subnetsHQ.unwrapSubnet(graphPanel);
							//GUIManager.getDefaultGUIManager().markNetChange();
						}
					}
			);
		}

		if (graphPanel.getSelectionManager().getSelectedElementLocations().stream()
				.map(ElementLocation::getParentNode)
				.filter(MetaNode.class::isInstance)
				.findAny().isEmpty()
		) {
			this.addSeparator();

			this.addMenuItem("Create subnet", "", e ->
					GUIManager.getDefaultGUIManager().subnetsHQ.createSubnetFromSelectedElements(graphPanel)
			);

			if (GUIManager.getDefaultGUIManager().subnetsHQ.getSubnetElementLocations(graphPanel.getSheetId()).stream()
					.anyMatch(location -> location.getParentNode() instanceof MetaNode)) {
				this.addMenuItem("Transfer to subnet (with M-Arcs)", "", e ->
						SubnetsActions.openTransferElementsToSubnet(graphPanel, true)
				);
				this.addMenuItem("Transfer to subnet (no M-Arcs)", "", e ->
						SubnetsActions.openTransferElementsToSubnet(graphPanel, false)
				);
				this.addMenuItem("Copy into subnet", "", e ->
						SubnetsActions.openCopyElementsToSubnet(graphPanel)
				);
			}

			if (graphPanel.getSelectionManager().getSelectedElementLocations().size() > 1 && graphPanel.getSelectionManager().getSelectedElementLocations().stream()
					.allMatch(location -> eloc.getParentNode() == location.getParentNode())) {
				this.addMenuItem("Merge portals", "", e ->
						GUIManager.getDefaultGUIManager().subnetsHQ.mergePortals(eloc, graphPanel.getSelectionManager().getSelectedElementLocations())
				);
			}
		}

		this.addSeparator();
	}
}
