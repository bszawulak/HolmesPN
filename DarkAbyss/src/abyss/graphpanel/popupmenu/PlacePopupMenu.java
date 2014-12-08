package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;


/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji zwi¹zanych z miejscami.
 * @author students
 *
 */
public class PlacePopupMenu extends PetriNetElementPopupMenu {
	private static final long serialVersionUID = -5062389148117837851L;

	public PlacePopupMenu(GraphPanel graphPanel) {
		super(graphPanel);
		
		this.addMenuItem("Change selected Places into P-Portals", "portal", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
			}
		});
		
		this.addMenuItem("Clone one Place into Portals", "portal", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
			}
		});
	}
}
