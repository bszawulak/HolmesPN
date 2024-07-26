package holmes.graphpanel.popupmenu;

import java.io.Serial;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Transition;
import holmes.windows.HolmesFunctionsBuilder;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

/**
 * Klasa odpowiedzialna za dodanie do menu kontekstowego opcji związanych z tranzycjami.
 *
 */
public class TransitionPopupMenu extends NodePopupMenu {
	@Serial
	private static final long serialVersionUID = 1268637178521514216L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	
	/**
	 * Konstruktor obiektu klasy TransitionPopupMenu.
	 * @param graphPanel GraphPanel - arkusz dla którego powstaje menu
	 */
	public TransitionPopupMenu(GraphPanel graphPanel, ElementLocation el, PetriNetElementType pne) {
		super(graphPanel, el, pne, el.getParentNode());
		
		this.addMenuItem(lang.getText("TPM_entry001"), "offlineSmall.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().isEmpty())
				return;

			ArrayList<Node> listOfSelectedNodes =  new ArrayList<>();
			for (ElementLocation el1 : getGraphPanel().getSelectionManager().getSelectedElementLocations()) {
				if(!listOfSelectedNodes.contains(el1.getParentNode()))
					listOfSelectedNodes.add(el1.getParentNode());
			}

			for (Node n : listOfSelectedNodes) {
				if (n instanceof Transition) {
					((Transition) n).setKnockout(!((Transition) n).isKnockedOut());
				}
			}
			overlord.getWorkspace().repaintAllGraphPanels();
		});
		
		this.addMenuItem(lang.getText("TPM_entry002"), "smallInvisibility.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().isEmpty())
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
			overlord.getWorkspace().repaintAllGraphPanels();
		});

		this.addMenuItem(lang.getText("TPM_entry003"), "functionalWindowIcon.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().isEmpty())
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
					if(overlord.reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working."
							, "Warning") == true)
						return;
					
					//getGraphPanel().getSelectionManager().transformSelectedIntoPortal();
					//overlord.markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "Option possible if more than one transition is selected.", "Too few selections", 
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		*/
		this.addMenuItem(lang.getText("TPM_entry004"), "portal.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 1) {
				if(overlord.reset.isSimulatorActiveWarning(
						lang.getText("TPM_entry005"), lang.getText("Warning")))
					return;
				if(overlord.reset.isXTPNSimulatorActiveWarning(
						lang.getText("TPM_entry006"), lang.getText("Warning")))
					return;

				//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
				getGraphPanel().getSelectionManager().cloneNodeIntoPortalV2();
				overlord.markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, lang.getText("TPM_entry007"), lang.getText("TPM_entry008"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		});


		this.addMenuItem(lang.getText("TPM_entry008"), "cut.png", e -> {
			if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() > 1) {
				if(overlord.reset.isSimulatorActiveWarning(
						lang.getText("TPM_entry005"), lang.getText("Warning")))
					return;
				if(overlord.reset.isXTPNSimulatorActiveWarning(
						lang.getText("TPM_entry006"), lang.getText("Warning")))
					return;

				//getGraphPanel().getSelectionManager().cloneNodeIntoPortal();
				getGraphPanel().getSelectionManager().saveSubnet();
				overlord.markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, lang.getText("TPM_entry007"), lang.getText("TPM_entry007t"),
						JOptionPane.INFORMATION_MESSAGE);
			}
		});
	}
}
