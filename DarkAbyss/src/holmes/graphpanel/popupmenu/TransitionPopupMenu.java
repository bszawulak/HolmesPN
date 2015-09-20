package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Transition;
import holmes.windows.HolmesFunctionsBuilder;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji związanych z tranzycjami.
 * 
 * @author students - statyczna wersja
 * @author MR - dynamiczna wersja
 *
 */
public class TransitionPopupMenu extends NodePopupMenu {
	private static final long serialVersionUID = 1268637178521514216L;

	/**
	 * Konstruktor obiektu klasy TransitionPopupMenu.
	 * @param graphPanel GraphPanel - arkusz dla którego powstaje menu
	 */
	public TransitionPopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, pne, el.getParentNode());
		
		this.addMenuItem("Transition ON/OFF", "offlineSmall.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
					return;
				
				Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
				if(n instanceof Transition) {
					if(((Transition) n).isOffline() == true)
						((Transition) n).setOffline(false);
					else
						((Transition) n).setOffline(true);
				}
				GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
			}
		});
		
		
		this.addMenuItem("Functions builder...", "functionalWindowIcon.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
					return;
				
				Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
				if(n instanceof Transition) {
					new HolmesFunctionsBuilder((Transition)n);  
				}
			}
		});
		
		
		
		
		
		/*
		this.addMenuItem("Change selected Transitions into same Portals", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					//getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
					//GUIManager.getDefaultGUIManager().markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible if more than one transition is selected.", "Too few selections", 
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		*/
		this.addMenuItem("Clone this Transition into Portal", "portal.png", new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
					getGraphPanel().getSelectionManager().cloneNodeIntoPortalV2();
					GUIManager.getDefaultGUIManager().markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible for one transition only.", "Too many selections", 
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}
}
