package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji związanych z tranzycjami.
 * @author students
 *
 */
public class TransitionPopupMenu extends PetriNetElementPopupMenu {
	// BACKUP:  1268637178521514216L   (ŁAPY PRECZ OD PONIŻSZEJ ZMIENNEJ)
	private static final long serialVersionUID = 1268637178521514216L;

	/**
	 * Konstruktor obiektu klasy TransitionPopupMenu.
	 * @param graphPanel GraphPanel - arkusz dla którego powstaje menu
	 */
	public TransitionPopupMenu(GraphPanel graphPanel) {
		super(graphPanel);
		this.addMenuItem("Change selected Transitions into T-Portals", "portal.png",
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
					}
				});
		
		this.addMenuItem("Clone one Transition into Portal", "portal.png",
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
					}
				});
	}
}
