package holmes.darkgui;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

/**
 * Klasa odpowiedzialna za przechwytywanie naciśnięcia przycisków.
 * @author students
 *
 */
class KeyManager implements KeyEventDispatcher {

	@SuppressWarnings("unused")
	private GUIManager guiManager;

	/**
	 * Konstruktor obiektu klasy KeyManager.
	 * @param guiManager GUIManager - manager głównego interfejsu programu
	 */
	public KeyManager(GUIManager guiManager) {
		this.guiManager = guiManager;
	}

	public boolean dispatchKeyEvent(KeyEvent e) {
		/*
		if (e.getID() == KeyEvent.KEY_TYPED) {
			System.out.print(this.guiManager.getFrame().getFocusOwner()
					.getParent());
			System.out.print(" ? ");
			System.out.println(guiManager.getWorkspace().getWorkspaceDock());
		}*/
		return false;
	}
}
