package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JMenu;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.PetriNetElement.PetriNetElementType;

public class MetaNodePopupMenu extends NodePopupMenu {
	private static final long serialVersionUID = 8356818331350683029L;

	public MetaNodePopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, pne, el.getParentNode());
		final GUIManager gui = GUIManager.getDefaultGUIManager();
		
		JMenu fixMenu = new JMenu("Fix meta-arcs");
		this.add(fixMenu);
		
		fixMenu.add(createMenuItem("Fix meta-arcs number", "invImportPopup.png", null, new ActionListener() {
			private ElementLocation elMeta;
			public void actionPerformed(ActionEvent arg0) {
				getGraphPanel().getSelectionManager().deselectAllElementLocations();
				ArrayList<Integer> sheetModified = new ArrayList<Integer>();
				sheetModified.add(((MetaNode)elMeta.getParentNode()).getRepresentedSheetID());
				gui.netsHQ.validateMetaArcs(sheetModified, true, false);
				getGraphPanel().repaint();
			}
			private ActionListener yesWeCan(ElementLocation inLoc){
				elMeta = inLoc;
		        return this;
		    }
		}.yesWeCan(el) ));
		
		
	}

}
