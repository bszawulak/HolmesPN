package abyss.graphpanel.popupmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.XMLEncoder;
import java.io.FileOutputStream;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;

public class GraphPanelPopupMenu extends JPopupMenu {
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = -8272632051140705976L;
	
	@SuppressWarnings("unused")
	private GUIManager guiManager;
	private GraphPanel graphPanel;

	protected JMenuItem cutMenuItem;
	protected JMenuItem copyMenuItem;
	protected JMenuItem pasteMenuItem;

	public GraphPanelPopupMenu(GraphPanel graphPanel) {
		this.guiManager = GUIManager.getDefaultGUIManager();
		this.setGraphPanel(graphPanel);
		this.createPredefinetMenuItems();
	}

	public void createPredefinetMenuItems() {
		cutMenuItem = this.createMenuItem("Cut", "cut",
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK),
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// getGraphPanel().removeAllSelectedElementLocations();
					}
				});

		copyMenuItem = this.createMenuItem("Copy", "copying_and_distribution",
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK),
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// getGraphPanel().removeAllSelectedElementLocations();
					}
				});

		pasteMenuItem = this.createMenuItem("Paste", "paste_plain",
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
	}

	public void serializeObjectToXML(String xmlFileLocation,
			Object objectToSerialize) throws Exception {
		FileOutputStream os = new FileOutputStream(xmlFileLocation);
		XMLEncoder encoder = new XMLEncoder(os);
		encoder.writeObject(objectToSerialize);
		encoder.close();
	}

	public void show(MouseEvent e) {
		super.show(this.getGraphPanel(), e.getX(), e.getY());
	}

	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	public void setGraphPanel(GraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}

	/**
	 * Method generates and add new menu item to current popup menu
	 * 
	 * @param text
	 *            - text that will be displayed in menu item
	 * @param iconName
	 *            - icon name that will displayed in menu item. A directory
	 *            (resources/icons/) and file extension (.png) will be added
	 *            automatically
	 * @param actionListener
	 *            - action listener of menu action
	 */
	protected void addMenuItem(String text, String iconName, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(text, new ImageIcon("resources/icons/" + iconName + ".png"));
		menuItem.addActionListener(actionListener);
		this.add(menuItem);
	}

	protected JMenuItem createMenuItem(String text, String iconName,
			KeyStroke accelerator, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(text, new ImageIcon("resources/icons/" + iconName + ".png"));
		menuItem.addActionListener(actionListener);
		menuItem.setAccelerator(accelerator);
		return menuItem;
	}
}
