package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
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
		this.addMenuItem("Change selected Transitions into T-Portals", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible if more than one transition is selected.", "Too few selections", 
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		this.addMenuItem("Clone one Transition into Portal", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible for one transition only.", "Too many selections", 
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}
}
