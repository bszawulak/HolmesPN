package abyss.darkgui.dockable;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.javadocking.DockingManager;
import com.javadocking.drag.DraggerFactory;

/**
 * A listener that installs a dragger factory.
 */
public class DraggingListener implements ActionListener {
	private DraggerFactory draggerFactory;

	public DraggingListener(DraggerFactory draggerFactory) {
		this.draggerFactory = draggerFactory;
	}

	public void actionPerformed(ActionEvent actionEvent) {
		DockingManager.setDraggerFactory(draggerFactory);
	}

}
