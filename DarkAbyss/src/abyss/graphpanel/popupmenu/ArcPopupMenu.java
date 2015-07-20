package abyss.graphpanel.popupmenu;

import abyss.graphpanel.GraphPanel;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.PetriNetElement.PetriNetElementType;

/**
 * Klasa tworząca wpisy w menu kontekstowym dla łuków sieci.
 * @author students - statyczna wersja
 * @author MR - dynamiczna wersja
 */
public class ArcPopupMenu extends NodePopupMenu {
	private static final long serialVersionUID = 6531877302888917900L;

	/**
	 * Konstruktor obiektu klasy ArcPopupMenu.
	 * @param graphPanel GraphPanel - obiekt dla którego powstaje menu
	 */
	public ArcPopupMenu(GraphPanel graphPanel, Arc a, PetriNetElementType pne) {
		super(graphPanel, pne, a);
	}

}
