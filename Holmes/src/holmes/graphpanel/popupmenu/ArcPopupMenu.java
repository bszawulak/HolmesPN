package holmes.graphpanel.popupmenu;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

import javax.swing.*;

/**
 * Klasa tworząca wpisy w menu kontekstowym dla łuków sieci.
 */
public class ArcPopupMenu extends NodePopupMenu {
	@Serial
	private static final long serialVersionUID = 6531877302888917900L;
	private static LanguageManager lang = GUIManager.getLanguageManager();
	
	/**
	 * Konstruktor obiektu klasy ArcPopupMenu.
	 * @param graphPanel GraphPanel - obiekt dla którego powstaje menu
	 */
	public ArcPopupMenu(GraphPanel graphPanel, Arc a, PetriNetElementType pne) {
		super(graphPanel, null, pne, a);

		boolean notMetaArc = !a.getArcType().equals(Arc.TypeOfArc.META_ARC);
		JMenuItem menuItem;

		menuItem = this.createMenuItem(lang.getText("APM_entry001"), "", null, new ActionListener() {
			Arc arc = null;
			public void actionPerformed(ActionEvent e) {
				Point breakP = getGraphPanel().arcNewBreakPoint;
				if(breakP == null)
					return;
				
				arc.createNewBreakPoint(breakP);
				getGraphPanel().repaint();
			}
			private ActionListener yesWeCan(Arc arc){
				this.arc = arc;
				return this;
		    }
		}.yesWeCan(a));
		menuItem.setEnabled(notMetaArc);
		add(menuItem);

		menuItem = this.createMenuItem(lang.getText("APM_entry002"), "", null, new ActionListener() {
			Arc arc = null;
			public void actionPerformed(ActionEvent e) {
				Point breakP = getGraphPanel().arcNewBreakPoint;
				if(breakP == null) {
					return;
				}
				arc.removeBreakPoint(breakP);
				getGraphPanel().repaint();
			}
			private ActionListener yesWeCan(Arc arc){
				this.arc = arc;
				return this;
		    }
		}.yesWeCan(a));
		menuItem.setEnabled(notMetaArc);
		add(menuItem);

		menuItem = this.createMenuItem(lang.getText("APM_entry003"), "", null, new ActionListener() {
			Arc arc = null;
			public void actionPerformed(ActionEvent e) {
				arc.clearBreakPoints();
				getGraphPanel().repaint();
			}
			private ActionListener yesWeCan(Arc arc){
				this.arc = arc;
				return this;
		    }
		}.yesWeCan(a));
		menuItem.setEnabled(notMetaArc);
		add(menuItem);
	}
}
