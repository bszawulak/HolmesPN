package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;

public class PetriNetElementPopupMenu extends GraphPanelPopupMenu {
	
	private static final long serialVersionUID = 3466116227209643358L;

	public PetriNetElementPopupMenu(GraphPanel graphPanel) {
		super(graphPanel);
		
		this.addMenuItem("Delete", "cross", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().getSelectionManager().deleteAllSelectedElements();
			}
		});
		
		this.addSeparator();
		
		this.add(cutMenuItem);
		this.add(copyMenuItem);
		this.add(pasteMenuItem);
		
		this.addSeparator();
	}

}
