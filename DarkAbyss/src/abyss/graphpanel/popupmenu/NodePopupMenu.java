package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Arc.TypesOfArcs;
import abyss.petrinet.elements.PetriNetElement.PetriNetElementType;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.windows.AbyssNodeInfo;

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
						AbyssNodeInfo ani = new AbyssNodeInfo((Place)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} else if(n instanceof Transition) {
						AbyssNodeInfo ani = new AbyssNodeInfo((Transition)n, GUIManager.getDefaultGUIManager().getFrame());
						ani.setVisible(true);
					} else if(n instanceof MetaNode) {
						AbyssNodeInfo ani = new AbyssNodeInfo((MetaNode)n, GUIManager.getDefaultGUIManager().getFrame());
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
			if(arc.getArcType() == TypesOfArcs.META_ARC)
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
						if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
							GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
						}
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
