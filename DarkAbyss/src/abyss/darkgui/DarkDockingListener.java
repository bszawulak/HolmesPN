package abyss.darkgui;

import java.awt.Point;

import abyss.workspace.Workspace;
import abyss.workspace.WorkspaceFiller;

import com.javadocking.dock.Position;
import com.javadocking.dockable.DefaultDockable;
import com.javadocking.dockable.DockingMode;
import com.javadocking.event.DockingEvent;
import com.javadocking.event.DockingListener;

/**
 * Klasa implementująca interfejs DockingListener. Jedna z klas odpowiedzialnych
 * za przesuwanie elementów interfejsu programu. Jej główny zadaniem jest niedopuszczenie
 * do sytuacji, w której usunięte zostanie choć na chwilę główne podokno rysowania sieci.
 * @author students
 *
 */
public class DarkDockingListener implements DockingListener {
	private Workspace workspace;

	@Override
	public void dockingChanged(DockingEvent e) {
		if (e.getDestinationDock() == workspace.getWorkspaceDock())
			workspace.getWorkspaceDock().emptyChild(workspace.getFillerDock());
		// else if (this.getChildDockCount()==0) this.addChildDock(fillerDock,
		// new Position(0))
	}

	@Override
	public void dockingWillChange(DockingEvent e) {
		if ((e.getDestinationDock() != workspace.getWorkspaceDock()) && (workspace.getWorkspaceDock().getChildDockCount() == 1)) {
			workspace.setFillerDockable(GUIManager.externalWithListener(new DefaultDockable("Workspace", new WorkspaceFiller(), "Workspace"), this));
			workspace.setFillerDock(workspace.getDockFactory().createDock(workspace.getFillerDockable(), DockingMode.SINGLE));
			Point position = new Point(0, 0);
			workspace.getFillerDock().addDockable(workspace.getFillerDockable(), position, position);
			workspace.getWorkspaceDock().addChildDock(workspace.getFillerDock(), new Position(0));
		}
	}

	/**
	 * Metoda zwraca obiekt obszaru roboczego.
	 * @return Workspace - obszar roboczy
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * Metoda ustawia obiekt obszaru roboczego.
	 * @return Workspace - nowy obszar roboczy
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

}
