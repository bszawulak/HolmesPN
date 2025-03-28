package holmes.graphpanel;

import holmes.darkgui.GUIManager;
import holmes.darkgui.settings.SettingsManager;

/**
 * Klasa statyczna ElementDraw i jej metody są wywoływane przerażającą liczbę razy na sekundę, w ramach
 * przerysowywania sieci. Aby w każdym wywołaniu nie powtarzać tych samym poleceń sprawdzających ustawienia
 * wyświetlania grafiki, są one zawarte w tej klasie, która jest tworzona raz na odświeżenie w ramach
 * metody drawPetriNet w GraphPanel, i posyłana do odpowiednich metod rysujących.
 */
public class ElementDrawSettings {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	public boolean view3d = false;
	public boolean newPlacePortal = false;
	public boolean snoopyMode = false;
	public boolean color = false;
	/**
	 * True oznacza zmianę kolorów w czasie symulacji
	 */
	public boolean crazyColors = false;
	/**
	 * True oznacza zachowanie kolorów wgranych ze Snoopiego
	 */
	public boolean nonDefColors = false;
	public boolean quickSimMode = false;
	
	public int arcSize = 0;
	
	public ElementDrawSettings() {
		checkSettings();
	}
	
	/**
	 * Matoda ustawia odpowiednie flagi w zależności od ustawień programu.
	 */
	private void checkSettings() {
		SettingsManager sm = overlord.getSettingsManager();
		view3d = sm.getValue("editor3Dview").equals("1");
		newPlacePortal = sm.getValue("editorNewPortalPlace").equals("1");
		snoopyMode = sm.getValue("editorSnoopyStyleGraphic").equals("1");
		crazyColors = sm.getValue("simPlacesColors").equals("1");
		nonDefColors = sm.getValue("editorSnoopyColors").equals("1");
		quickSimMode = overlord.simSettings.quickSimToken;
		arcSize = Integer.parseInt(overlord.getSettingsManager().getValue("editorGraphArcLineSize"));
	}
}
