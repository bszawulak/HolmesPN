package abyss.darkgui;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;

class KeyManager implements KeyEventDispatcher {

	@SuppressWarnings("unused")
	private GUIManager guiManager;

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
