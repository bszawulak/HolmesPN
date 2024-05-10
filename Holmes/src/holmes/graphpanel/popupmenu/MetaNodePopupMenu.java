package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

public class MetaNodePopupMenu extends NodePopupMenu {
	@Serial
	private static final long serialVersionUID = 8356818331350683029L;

	public MetaNodePopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, el, pne, el.getParentNode());
		final GUIManager gui = GUIManager.getDefaultGUIManager();

		if(graphPanel.getSelectionManager().getSelectedElementLocations().size() == 1) {
			this.addMenuItem("Delete", "cross.png", e -> {
						Object[] options = {"Delete", "Cancel",};

						MetaNode metaNode = (MetaNode) el.getParentNode();
						int id = metaNode.getRepresentedSheetID();
						long uniquePlaces = GUIManager.getDefaultGUIManager().subnetsHQ.getSubnetElementLocations(id).stream()
								.map(ElementLocation::getParentNode)
								.filter(Place.class::isInstance)
								.collect(Collectors.toSet()).stream()
								.filter(node -> node.getNodeLocations().stream()
										.allMatch(location -> location.getSheetID() == id))
								.count();

						long uniqueTransitions = GUIManager.getDefaultGUIManager().subnetsHQ.getSubnetElementLocations(id).stream()
								.map(ElementLocation::getParentNode)
								.filter(Transition.class::isInstance)
								.collect(Collectors.toSet()).stream()
								.filter(node -> node.getNodeLocations().stream()
										.allMatch(location -> location.getSheetID() == id))
								.count();

						StringBuilder builder = new StringBuilder();
						builder.append(String.format(
								"This subnet (%d) contains %d place(s) and %d transition(s) which are not portals.%n",
								id, uniquePlaces, uniqueTransitions)
						);
						builder.append(String.format("Subnet has connections (portals in) the following subnets:%n"));

						String parentName = GUIManager.getDefaultGUIManager().subnetsHQ.getMetanode(metaNode.getMySheetID())
										.map(PetriNetElement::getName).orElse("Subnet0");

						builder.append(parentName).append(String.format("%n"));

						List<String> childrenNames = GUIManager.getDefaultGUIManager().subnetsHQ.getSubnetElementLocations(id).stream()
								.map(ElementLocation::getParentNode)
								.filter(MetaNode.class::isInstance)
								.map(MetaNode.class::cast)
								.map(MetaNode::getName)
								.toList();
						builder.append(String.join(String.format("%n"), childrenNames));


						int n = JOptionPane.showOptionDialog(null,
								builder.toString(), "Deletion warning", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) {
							GUIManager.getDefaultGUIManager().subnetsHQ.deleteSubnet(graphPanel.getSelectionManager().getSelectedMetanode());
							GUIManager.getDefaultGUIManager().markNetChange();
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
							GUIManager.getDefaultGUIManager().markNetChange();
						}
					}
			);

			this.addMenuItem("Delete meta-arcs ", "", e -> {
						Object[] options = {"Delete", "Cancel",};
						int n = JOptionPane.showOptionDialog(null,
								"Do you want to delete all meta-arcs?", "Unwrapping warning?", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) {
							GUIManager.getDefaultGUIManager().subnetsHQ.clearMetaArcs(List.of(el));
							getGraphPanel().repaint();
							GUIManager.getDefaultGUIManager().markNetChange();
						}
					}
			);
		}
		
		JMenu fixMenu = new JMenu("Fix meta-arcs");
		this.add(fixMenu);
		
		fixMenu.add(createMenuItem("Fix meta-arcs number", "invImportPopup.png", null, new ActionListener() {
			private ElementLocation elMeta;
			public void actionPerformed(ActionEvent arg0) {
				getGraphPanel().getSelectionManager().deselectAllElementLocations();
//				ArrayList<Integer> sheetModified = new ArrayList<Integer>();
//				sheetModified.add(((MetaNode)elMeta.getParentNode()).getRepresentedSheetID());
//				gui.subnetsHQ.validateMetaArcs(sheetModified, true, false);
				gui.subnetsHQ.fixMetaArcsNumber((MetaNode)elMeta.getParentNode());
				getGraphPanel().repaint();
				GUIManager.getDefaultGUIManager().markNetChange();
			}
			private ActionListener yesWeCan(ElementLocation inLoc){
				elMeta = inLoc;
		        return this;
		    }
		}.yesWeCan(el) ));
	}
}
