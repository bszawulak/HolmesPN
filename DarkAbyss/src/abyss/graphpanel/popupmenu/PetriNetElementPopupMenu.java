package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.windows.AbyssNodeInfo;

/**
 * Klasa odpowiedzialna za utworzenie menu kontekstowego dla wierzchołków sieci.
 * @author students
 *
 */
public class PetriNetElementPopupMenu extends GraphPanelPopupMenu {
	// BACKUP:  -8988739887642243733L  (ŁAPY PRECZ OD PONIŻSZEJ ZMIENNEJ)
	private static final long serialVersionUID = -8988739887642243733L;

	/**
	 * Konstruktor obiektu klasy PetriNetElementPopupMenu.
	 * @param graphPanel GraphPanel - obiekt panelu dla tworzonego menu
	 */
	public PetriNetElementPopupMenu(GraphPanel graphPanel) {
		super(graphPanel);
		
		this.addMenuItem("Show details...", "", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//NetSimulator ns = GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator();
				//if(ns.getMode() == SimulatorMode.STOPPED) {
				
				//TODO: wykrywanie kliknięcia łuku, inaczej wylatuje wyjątek:
					Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
					if(n instanceof Place) {
						AbyssNodeInfo ani = new AbyssNodeInfo((Place)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} else if(n instanceof Transition) {
						AbyssNodeInfo ani = new AbyssNodeInfo((Transition)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} 
				//} else {
				//	JOptionPane.showMessageDialog(null, "Warning: simulator active. Cannot proceed until manually stopped.",
				//			"Net simulator working", JOptionPane.WARNING_MESSAGE);
				//}
			}
		});
		
		this.addMenuItem("Delete", "cross.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: warning question
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
