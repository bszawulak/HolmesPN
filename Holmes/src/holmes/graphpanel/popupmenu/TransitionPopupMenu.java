package holmes.graphpanel.popupmenu;

import java.io.Serial;
import java.util.ArrayList;

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
	@Serial
	private static final long serialVersionUID = 1268637178521514216L;

	/**
	 * Konstruktor obiektu klasy TransitionPopupMenu.
	 * @param graphPanel GraphPanel - arkusz dla którego powstaje menu
	 */
	public TransitionPopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, el, pne, el.getParentNode());
		
		this.addMenuItem("Transition ON/OFF", "offlineSmall.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
				return;

			ArrayList<Node> listOfSelectedNodes =  new ArrayList<>();
			for (ElementLocation el1 : getGraphPanel().getSelectionManager().getSelectedElementLocations()) {
				if(!listOfSelectedNodes.contains(el1.getParentNode()))
					listOfSelectedNodes.add(el1.getParentNode());
			}

			for (Node n : listOfSelectedNodes) {
				if (n instanceof Transition) {
					((Transition) n).setOffline(!((Transition) n).isOffline());
				}
			}

			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		
		this.addMenuItem("Invisibility ON/OFF", "smallInvisibility.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
				return;

			ArrayList<Node> listOfSelectedNodes =  new ArrayList<>();
			for (ElementLocation el12 : getGraphPanel().getSelectionManager().getSelectedElementLocations()) {
				if(!listOfSelectedNodes.contains(el12.getParentNode()))
					listOfSelectedNodes.add(el12.getParentNode());
			}

			for (Node n : listOfSelectedNodes) {
				if (n instanceof Transition) {
					n.setInvisibility( !(n.isInvisible()) ); //odwrotność isInvisible, czyli switcher
				}
			}
			GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
		});
		
		
		this.addMenuItem("Functions builder...", "functionalWindowIcon.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
				return;

			Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
			if(n instanceof Transition) {
				new HolmesFunctionsBuilder((Transition)n);
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
		this.addMenuItem("Clone this Transition into Portal", "portal.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
				if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
						"Operation impossible when simulator is working."
						, "Warning"))
					return;
				if(GUIManager.getDefaultGUIManager().reset.isXTPNSimulatorActiveWarning(
						"Operation impossible when XTPN simulator is working."
						, "Warning"))
					return;

				//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
				getGraphPanel().getSelectionManager().cloneNodeIntoPortalV2();
				GUIManager.getDefaultGUIManager().markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, "Option possible for one transition only.", "Too many selections",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});


		this.addMenuItem("Export subnet to File", "cut.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
				if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
						"Operation impossible when simulator is working."
						, "Warning"))
					return;
				if(GUIManager.getDefaultGUIManager().reset.isXTPNSimulatorActiveWarning(
						"Operation impossible when XTPN simulator is working."
						, "Warning"))
					return;

				//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
				getGraphPanel().getSelectionManager().saveSubnet();
				GUIManager.getDefaultGUIManager().markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, "Option possible for one transition only.", "Too many selections",
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
