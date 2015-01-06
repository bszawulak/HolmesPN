package abyss.darkgui.dockable;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;



import com.javadocking.dockable.Dockable;

/**
 * Klasa odpowiedzialna za dodawanie nowych pozycji w menu programu, precyzyjniej
 * w podmenu odpowiedzialnym za wyświetlanie statusu widoczność podokien programu.
 * @author students
 * @author MR
 */
public class DockableMenuItem extends JCheckBoxMenuItem {
	// BACKUP: -690559183213842322L;
	private static final long serialVersionUID = -690559183213842322L;

	/**
	 * Konstruktor tworzący nowy wpis w menu, oraz ustawiający jego ikonę.
	 * @param dockable Dockable - obiekt do dodania
	 * @param icon ImageIcon - ikonka
	 */
	public DockableMenuItem(Dockable dockable, ImageIcon icon) {
		super(dockable.getTitle(), dockable.getIcon());
		
		if(icon != null)
			this.setIcon(icon);
		setSelected(dockable.getDock() != null);

		DockableMediator dockableMediator = new DockableMediator(dockable, this);
		dockable.addDockingListener(dockableMediator);
		//dockable.set
		addItemListener(dockableMediator);
	}
}