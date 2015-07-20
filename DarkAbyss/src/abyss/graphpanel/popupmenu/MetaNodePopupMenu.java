package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.PetriNetElement.PetriNetElementType;

public class MetaNodePopupMenu extends NodePopupMenu {
	private static final long serialVersionUID = 8356818331350683029L;

	public MetaNodePopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, pne, el.getParentNode());
		
		this.addMenuItem("Welcome to metaNode", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
	}

}
