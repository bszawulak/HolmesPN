package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.graphpanel.GraphPanel;

/**
 * Klasa odpowiedzialna za utworzenie menu kontekstowego dla wierzcho³ków sieci.
 * @author students
 *
 */
public class PetriNetElementPopupMenu extends GraphPanelPopupMenu {
	private static final long serialVersionUID = 3466116227209643358L;

	/**
	 * Konstruktor obiektu klasy PetriNetElementPopupMenu.
	 * @param graphPanel GraphPanel - obiekt panelu dla tworzonego menu
	 */
	public PetriNetElementPopupMenu(GraphPanel graphPanel) {
		super(graphPanel);
		
		this.addMenuItem("Delete", "cross.png", new ActionListener() {
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
