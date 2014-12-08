package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji zwi¹zanych z tranzycjami.
 * @author students
 *
 */
public class TransitionPopupMenu extends PetriNetElementPopupMenu {
	private static final long serialVersionUID = 1268637178521514216L;
	/**
	 * Konstruktor obiektu 
	 * @param graphPanel
	 */
	public TransitionPopupMenu(GraphPanel graphPanel) {
		super(graphPanel);

		this.addMenuItem("Change selected Transitions into T-Portals", "portal",
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
					}
				});
		
		this.addMenuItem("Clone one Transition into Portals", "portal",
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
					}
				});
	}

}
