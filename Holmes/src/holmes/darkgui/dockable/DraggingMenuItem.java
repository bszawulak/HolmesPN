package holmes.darkgui.dockable;

import javax.swing.JRadioButtonMenuItem;


import com.javadocking.DockingManager;
import com.javadocking.drag.DraggerFactory;
import com.javadocking.drag.StaticDraggerFactory;
import com.javadocking.drag.painter.CompositeDockableDragPainter;
import com.javadocking.drag.painter.DockableDragPainter;

/**
 * A check box menu item to enable a dragger.
 */
public class DraggingMenuItem extends JRadioButtonMenuItem {
	private static final long serialVersionUID = 1L;

	public DraggingMenuItem(String title, DockableDragPainter basicDockableDragPainter,
			DockableDragPainter additionalDockableDragPainter, boolean selected) {
		super(title);
		// Create the dockable drag painter and dragger factory.
		CompositeDockableDragPainter compositeDockableDragPainter = new CompositeDockableDragPainter();
		compositeDockableDragPainter.addPainter(basicDockableDragPainter);
		if (additionalDockableDragPainter != null) {
			compositeDockableDragPainter.addPainter(additionalDockableDragPainter);
		}
		DraggerFactory draggerFactory = new StaticDraggerFactory(compositeDockableDragPainter);

		// Give this dragger factory to the docking manager.
		if (selected) {
			DockingManager.setDraggerFactory(draggerFactory);
			setSelected(true);
		}
		// Add a dragging listener as action listener.
		addActionListener(new DraggingListener(draggerFactory));
	}
}
