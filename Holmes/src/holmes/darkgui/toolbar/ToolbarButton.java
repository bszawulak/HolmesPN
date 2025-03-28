package holmes.darkgui.toolbar;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.Border;

public class ToolbarButton extends JLabel {
	@Serial
	private static final long serialVersionUID = 3034370365062643549L;
	/** The button will always have this size. */
	private static final Dimension DEFAULT_SIZE = new Dimension(48, 48);
	/** The border width of the button. */
	private static final int BORDER = 4;
	/** The empty border of the button. */
	private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER);
	/** The border of the button when the mouse is over it. */
	private static final Border LINE_BORDER = BorderFactory.createCompoundBorder(
			BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(BORDER - 2, BORDER - 2, BORDER - 2, BORDER - 2), 
					BorderFactory.createLineBorder(Color.gray)),
			BorderFactory.createEmptyBorder(1, 1, 1, 1));
	
	/** The action that will be performed when this button is clicked. */
	private Action action;

	/**
	 * Constructs an icon button with the given action.
	 * @param	action 		The action that will be performed when this button is clicked.
	 */
	public ToolbarButton(Action action) {		
		setDimensions();
		setAction(action);
		addMouseListener(new ClickListener());
		setBorder(EMPTY_BORDER);
		setOpaque(false);
	}
	
	/**
	 * Gets the action that will be performed when this button is clicked.
	 * @return	The action that will be performed when this button is clicked.
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * Sets the action that will be performed when this button is clicked.
	 * 
	 * @param action - The action that will be performed when this button is clicked.
	 */
	public void setAction(Action action) {
		if (action == null) {
			throw new IllegalArgumentException("Acion is null.");
		}
		
		this.action = action;
		if (action instanceof AbstractAction) {
			ImageIcon icon = (ImageIcon)action.getValue(Action.SMALL_ICON);
			this.setIcon(icon);
			String description = (String)action.getValue(Action.SHORT_DESCRIPTION);
			this.setToolTipText(description);
		}
	}

	/**
	 * This mouse listener performs the action when the mouse is clicked.
	 */
	private class ClickListener extends MouseAdapter {
		// Overwritten methods of MouseAdapter.
		public void mouseClicked(MouseEvent mouseEvent) {
			// Create the action event.
			ActionEvent actionEvent = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, 
					(String)action.getValue(Action.SHORT_DESCRIPTION), mouseEvent.getModifiers());
			
			// Perform the action.
			action.actionPerformed(actionEvent);
		}
		public void mouseEntered(MouseEvent mouseEvent) {
			setBorder(LINE_BORDER);
		}
		public void mouseExited(MouseEvent mouseEvent) {
			setBorder(EMPTY_BORDER);
		}
	}
	
	/**
	 * Sets the preferred, maximum and minimum size of the button.
	 */
	private void setDimensions() {
		setPreferredSize(DEFAULT_SIZE);
		setMaximumSize(DEFAULT_SIZE);
		setMinimumSize(DEFAULT_SIZE);
	}
}