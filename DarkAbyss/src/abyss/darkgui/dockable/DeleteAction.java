package abyss.darkgui.dockable;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

import abyss.darkgui.GUIManager;

import com.javadocking.dockable.Dockable;

public class DeleteAction extends AbstractAction {
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
		guiManager.getWorkspace().deleteTab(dockable);
	}

	public Dockable getDockable() {
		return dockable;
	}

	public void setDockable(Dockable dockable) {
		this.dockable = dockable;
	}

}
