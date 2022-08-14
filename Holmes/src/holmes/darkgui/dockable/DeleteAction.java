package holmes.darkgui.dockable;

import java.awt.event.ActionEvent;
import java.io.Serial;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import com.javadocking.dockable.Dockable;

import holmes.darkgui.GUIManager;

/**
 * Klasa odpowiedzialna za operacje na arkuszach 
 * 
 * @author students
 */
public class DeleteAction extends AbstractAction {
	@Serial
	private static final long serialVersionUID = 1577108502403600290L;
	private GUIManager guiManager;
	private Dockable dockable;
	
	public DeleteAction(GUIManager gui, String name, Icon icon) {
		super(null, icon);
		putValue(Action.SHORT_DESCRIPTION, name);
		this.guiManager = gui;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		guiManager.getWorkspace().deleteTab(dockable, false);
	}

	public Dockable getDockable() {
		return dockable;
	}

	public void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

}
