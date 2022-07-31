package holmes.graphpanel.popupmenu;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

/**
 * Klasa tworząca wpisy w menu kontekstowym dla łuków sieci.
 * @author students - statyczna wersja
 * @author MR - dynamiczna wersja
 */
public class ArcPopupMenu extends NodePopupMenu {
	@Serial
	private static final long serialVersionUID = 6531877302888917900L;

	/**
	 * Konstruktor obiektu klasy ArcPopupMenu.
	 * @param graphPanel GraphPanel - obiekt dla którego powstaje menu
	 */
	public ArcPopupMenu(GraphPanel graphPanel, Arc a, PetriNetElementType pne) {
		super(graphPanel, null, pne, a);
		
		this.addMenuItem("Create break point here", "", new ActionListener() {
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
		}.yesWeCan(a) ); 
		
		this.addMenuItem("Remove break point", "", new ActionListener() {
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
		}.yesWeCan(a) );
		
		this.addMenuItem("Remove ALL break points", "", new ActionListener() {
			Arc arc = null;
			public void actionPerformed(ActionEvent e) {
				arc.clearBreakPoints();
				getGraphPanel().repaint();
			}
			private ActionListener yesWeCan(Arc arc){
				this.arc = arc;
				return this;
		    }
		}.yesWeCan(a) );
	}
}
