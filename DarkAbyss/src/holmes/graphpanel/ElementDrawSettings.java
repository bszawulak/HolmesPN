package holmes.graphpanel;

import holmes.darkgui.GUIManager;
import holmes.darkgui.settings.SettingsManager;

/**
 * Klasa statyczna ElementDraw i jej metody są wywoływane przerażającą liczbę razy na sekundę, w ramach
 * przerysowywania sieci. Aby w każdym wywołaniu nie powtarzać tych samym poleceń sprawdzających ustawienia
 * wyświetlania grafiki, są one zawarte w tej klasie, która jest tworzona raz na odświeżenie w ramach
 * metody drawPetriNet w GraphPanel, i posyłana do odpowiednich metod rysujących.
 * @author MR
 *
 */
public class ElementDrawSettings {
	public boolean view3d = false;
	public boolean snoopyMode = false;
	public boolean crazyColors = false;
	public boolean nonDefColors = false;
	
	public ElementDrawSettings() {
		checkSettings();
	}
	
	/**
	 * Matoda ustawia odpowiednie flagi w zależności od ustawień programu.
	 */
	private void checkSettings() {
		SettingsManager sm = GUIManager.getDefaultGUIManager().getSettingsManager();
		if(sm.getValue("editor3Dview").equals("1")) {
			view3d = true;
		} else {
			view3d = false;
		}
		
		if(sm.getValue("editorSnoopyStyleGraphic").equals("1")) {
			snoopyMode = true;
		} else {
			snoopyMode = false;
		}
		
		if(sm.getValue("simPlacesColors").equals("1")) {
			crazyColors = true;
		} else {
			crazyColors = false;
		}
		
		if(sm.getValue("editorSnoopyColors").equals("1")) {
			nonDefColors = true;
		} else {
			nonDefColors = false;
		}
	}
}
