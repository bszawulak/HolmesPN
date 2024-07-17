package holmes.darkgui;

import holmes.workspace.Workspace;

/**
 * Klasa implementująca interfejs DockingListener. Jedna z klas odpowiedzialnych
 * za przesuwanie elementów interfejsu programu. Jej główny zadaniem jest niedopuszczenie
 * do sytuacji, w której usunięte zostanie choć na chwilę główne podokno rysowania sieci.
 * @author students
 *
 */
public class DarkDockingListener{
	private Workspace workspace;

	/**
	 * Metoda zwraca obiekt obszaru roboczego.
	 * @return Workspace - obszar roboczy
	 */
	public Workspace getWorkspace() {
		return workspace;
	}

	/**
	 * Metoda ustawia obiekt obszaru roboczego.
	 * @param workspace (Workspace) nowy obszar roboczy
	 */
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}
