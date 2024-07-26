package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

public class MetaNodePopupMenu extends NodePopupMenu {
	@Serial
	private static final long serialVersionUID = 8356818331350683029L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();

	public MetaNodePopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, el, pne, el.getParentNode());
		if(graphPanel.getSelectionManager().getSelectedElementLocations().size() == 1) {
			this.addMenuItem(lang.getText("MNPM_entry001"), "cross.png", e -> {
						Object[] options = {lang.getText("delete"), lang.getText("cancel"),};

						MetaNode metaNode = (MetaNode) el.getParentNode();
						int id = metaNode.getRepresentedSheetID();
						long uniquePlaces = overlord.subnetsHQ.getSubnetElementLocations(id).stream()
								.map(ElementLocation::getParentNode)
								.filter(Place.class::isInstance)
								.collect(Collectors.toSet()).stream()
								.filter(node -> node.getNodeLocations().stream()
										.allMatch(location -> location.getSheetID() == id))
								.count();

						long uniqueTransitions = overlord.subnetsHQ.getSubnetElementLocations(id).stream()
								.map(ElementLocation::getParentNode)
								.filter(Transition.class::isInstance)
								.collect(Collectors.toSet()).stream()
								.filter(node -> node.getNodeLocations().stream()
										.allMatch(location -> location.getSheetID() == id))
								.count();

						StringBuilder builder = new StringBuilder();
						builder.append(String.format(//This subnet (%d) contains %d place(s) and %d transition(s) which are not portals.%n
								lang.getText("MNPM_entry002"),
								id, uniquePlaces, uniqueTransitions)
						);
						builder.append(String.format(lang.getText("MNPM_entry003")));

						String parentName = overlord.subnetsHQ.getMetanode(metaNode.getMySheetID())
										.map(PetriNetElement::getName).orElse("Subnet0");

						builder.append(parentName).append(String.format("%n"));

						List<String> childrenNames = overlord.subnetsHQ.getSubnetElementLocations(id).stream()
								.map(ElementLocation::getParentNode)
								.filter(MetaNode.class::isInstance)
								.map(MetaNode.class::cast)
								.map(MetaNode::getName)
								.toList();
						builder.append(String.join(String.format("%n"), childrenNames));


						int n = JOptionPane.showOptionDialog(null,
								builder.toString(), lang.getText("MNPM_entry004"), JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) {
							overlord.subnetsHQ.deleteSubnet(graphPanel.getSelectionManager().getSelectedMetanode());
							overlord.markNetChange();
						}
					}
			);

			this.addMenuItem(lang.getText("unwrap"), "", e -> {
						Object[] options = {lang.getText("unwrap"), lang.getText("cancel"),};
						int n = JOptionPane.showOptionDialog(null,
								lang.getText("MNPM_entry005"), lang.getText("MNPM_entry005t"), JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) {
							overlord.subnetsHQ.unwrapSubnet(graphPanel);
							overlord.markNetChange();
						}
					}
			);

			this.addMenuItem(lang.getText("MNPM_entry006"), "", e -> {
						Object[] options = {lang.getText("delete"), lang.getText("cancel"),};
						int n = JOptionPane.showOptionDialog(null,
								lang.getText("MNPM_entry007"), lang.getText("MNPM_entry007t"), JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) {
							overlord.subnetsHQ.clearMetaArcs(List.of(el));
							getGraphPanel().repaint();
							overlord.markNetChange();
						}
					}
			);
		}
		
		JMenu fixMenu = new JMenu(lang.getText("MNPM_entry008"));
		this.add(fixMenu);
		
		fixMenu.add(createMenuItem(lang.getText("MNPM_entry009"), "invImportPopup.png", null, new ActionListener() {
			private ElementLocation elMeta;
			public void actionPerformed(ActionEvent arg0) {
				getGraphPanel().getSelectionManager().deselectAllElementLocations();
//				ArrayList<Integer> sheetModified = new ArrayList<Integer>();
//				sheetModified.add(((MetaNode)elMeta.getParentNode()).getRepresentedSheetID());
//				gui.subnetsHQ.validateMetaArcs(sheetModified, true, false);
				overlord.subnetsHQ.fixMetaArcsNumber((MetaNode)elMeta.getParentNode());
				getGraphPanel().repaint();
				overlord.markNetChange();
			}
			private ActionListener yesWeCan(ElementLocation inLoc){
				elMeta = inLoc;
		        return this;
		    }
		}.yesWeCan(el) ));
	}
}
