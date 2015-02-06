package abyss.darkgui;

/**
 * Klasa odpowiedzialna za różne rzeczy związane z czyszczeniem wszystkiego i niczego w ramach
 * programu.
 * @author MR
 *
 */
public class GUIReset {
	private GUIManager mastah = GUIManager.getDefaultGUIManager();

	public void clearGraphColors() {
		mastah.getWorkspace().getProject().turnTransitionGlowingOff();
		mastah.getWorkspace().getProject().setTransitionGlowedMTC(false);
		mastah.getWorkspace().getProject().setColorClusterToNeutral();
		mastah.getWorkspace().getProject().repaintAllGraphPanels();
	}
}
