package holmes.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.windows.HolmesNodeInfo;

/**
 * Klasa odpowiedzialna za utworzenie menu kontekstowego dla wierzchołków sieci.
 * @author students - wersja statyczna
 * @author MR - dynamiczna wersja
 */
public class NodePopupMenu extends GraphPanelPopupMenu {
	private static final long serialVersionUID = -8988739887642243733L;

	/**
	 * Konstruktor obiektu klasy PetriNetElementPopupMenu.
	 * @param graphPanel GraphPanel - obiekt panelu dla tworzonego menu
	 */
	public NodePopupMenu(GraphPanel graphPanel, PetriNetElementType pne, Object pneObject) {
		super(graphPanel, pne);
		
		if(pne != PetriNetElementType.ARC) {
			this.addMenuItem("Show details...", "", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(getGraphPanel().getSelectionManager().getSelectedElementLocations().size() == 0)
						return;
					
					Node n = getGraphPanel().getSelectionManager().getSelectedElementLocations().get(0).getParentNode();
					if(n instanceof Place) {
						HolmesNodeInfo ani = new HolmesNodeInfo((Place)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} else if(n instanceof Transition) {
						HolmesNodeInfo ani = new HolmesNodeInfo((Transition)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} else if(n instanceof MetaNode) {
						HolmesNodeInfo ani = new HolmesNodeInfo((MetaNode)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} 
					
					//} else {
					//	JOptionPane.showMessageDialog(null, "Warning: simulator active. Cannot proceed until manually stopped.",
					//			"Net simulator working", JOptionPane.WARNING_MESSAGE);
					//}
				}
			});
		}
		
		boolean proceed = true;
		if(pne == PetriNetElementType.ARC) {
			Arc arc = (Arc)pneObject;
			if(arc.getArcType() == TypeOfArc.META_ARC)
				proceed = false;
		}
		
		if(pne != PetriNetElementType.META && proceed) {
			this.addMenuItem("Delete", "cross.png", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(GUIManager.getDefaultGUIManager().reset.isSimulatorActiveWarning(
							"Operation impossible when simulator is working.", "Warning") == true)
						return;
					
					Object[] options = {"Delete", "Cancel",};
					int n = JOptionPane.showOptionDialog(null,
							"Do you want to delete selected elements?", "Deletion warning?", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
					if (n == 0) {
						GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						getGraphPanel().getSelectionManager().deleteAllSelectedElements();
						getGraphPanel().getSelectionManager().deselectAllElementLocations();
						GUIManager.getDefaultGUIManager().markNetChange();
					}
				}
			});
		}
		this.addSeparator();
		if(pne != PetriNetElementType.META) {
			this.add(cutMenuItem);
			this.add(copyMenuItem);
			this.add(pasteMenuItem);
		}
		
		this.addSeparator();
	}
}
