package abyss.darkgui.toolbar;

import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;

/**
 * Klasa pomocnicza do tworzenia przycisków paska zadań.
 */
public class ToolbarButtonAction extends AbstractAction {
	private static final long serialVersionUID = -7314939115369528810L;
	private Component parentComponent;
	private String message = "";
	private String name;

	public ToolbarButtonAction(Component parentComponent, String name, String description, Icon icon, String message) {
		super(null, icon);
		putValue(Action.SHORT_DESCRIPTION, description);
		this.message = message;
		this.name = name;
		this.parentComponent = parentComponent;
	}
	
	public ToolbarButtonAction(Component parentComponent, String name, String description, Icon icon) {
		super(null, icon);
		putValue(Action.SHORT_DESCRIPTION, description);
		this.name = name;
		this.parentComponent = parentComponent;
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		JOptionPane.showMessageDialog(parentComponent, message, name, JOptionPane.INFORMATION_MESSAGE);
	}

}
