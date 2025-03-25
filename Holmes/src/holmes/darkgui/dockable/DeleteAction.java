package holmes.darkgui.dockable;

import java.awt.event.ActionEvent;
import java.io.Serial;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;


import holmes.darkgui.GUIManager;

/**
 * Klasa odpowiedzialna za operacje na arkuszach
 * @author students
 */
public class DeleteAction extends AbstractAction {
	@Serial
	private static final long serialVersionUID = 1577108502403600290L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	//private Dockable dockable;
	
	public DeleteAction(String name, Icon icon) {
		super(null, icon);
		putValue(Action.SHORT_DESCRIPTION, name);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		//guiManager.getWorkspace().deleteTab(dockable, false);
	}
}
