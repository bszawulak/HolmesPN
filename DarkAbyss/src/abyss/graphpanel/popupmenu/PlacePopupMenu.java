package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji związanych z miejscami.
 * @author students
 *
 */
public class PlacePopupMenu extends PetriNetElementPopupMenu {
	// BACKUP:  -5062389148117837851L   (ŁAPY PRECZ OD PONIŻSZEJ ZMIENNEJ)
	private static final long serialVersionUID = -5062389148117837851L;

	/**
	 * Konstruktor obiektu klasy PlacePopupMenu.
	 * @param graphPanel GraphPanel - panel dla którego powstaje menu
	 */
	public PlacePopupMenu(GraphPanel graphPanel) {
		super(graphPanel);
		
		this.addMenuItem("Change selected Places into P-Portals", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
			}
		});
		
		this.addMenuItem("Clone one Place into Portal", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
			}
		});
	}
}
