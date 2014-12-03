package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;

@SuppressWarnings("serial")
public class TransitionPopupMenu extends PetriNetElementPopupMenu {

	public TransitionPopupMenu(GraphPanel graphPanel) {
		super(graphPanel);

		this.addMenuItem("Transform into portal", "portal",
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getGraphPanel().getSelectionManager()
								.transformSelectedIntoPortal();
					}
				});
	}

}
