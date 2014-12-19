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

/**
 * Klasa odpowiedzialna za tworzenie menu kontekstowego dla sieci narysowanej na danym panelu.
 * @author students
 *
 */
public class GraphPanelPopupMenu extends JPopupMenu {
	//private static final long serialVersionUID = 1L;
	private static final long serialVersionUID = -8272632051140705976L;
	@SuppressWarnings("unused")
	private GUIManager guiManager;
	private GraphPanel graphPanel;

	protected JMenuItem cutMenuItem;
	protected JMenuItem copyMenuItem;
	protected JMenuItem pasteMenuItem;

	/**
	 * Konstruktor obiektu klasy GraphPanelPopupMenu.
	 * @param graphPanel GraphPanel - obiekt dla którego powstaje menu kontekstowe
	 */
	public GraphPanelPopupMenu(GraphPanel graphPanel) {
		this.guiManager = GUIManager.getDefaultGUIManager();
		this.setGraphPanel(graphPanel);
		this.createPredefineMenuItems();
	}

	/**
	 * Metoda pomocnicza konstruktora, tworzy podstawowe elementy menu kontekstowego sieci.
	 */
	public void createPredefineMenuItems() {
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
						@SuppressWarnings("unused")
						int x = 1;
						// getGraphPanel().removeAllSelectedElementLocations();
					}
				});

		pasteMenuItem = this.createMenuItem("Paste", "paste_plain",
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK),
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
		
		cutMenuItem.setEnabled(false);
		copyMenuItem.setEnabled(false);
		pasteMenuItem.setEnabled(false);
	}

	public void serializeObjectToXML(String xmlFileLocation, Object objectToSerialize) throws Exception {
		FileOutputStream os = new FileOutputStream(xmlFileLocation);
		XMLEncoder encoder = new XMLEncoder(os);
		encoder.writeObject(objectToSerialize);
		encoder.close();
	}

	/**
	 * Metoda pokazuj¹ca menu w danym klikniêtym miejscu.
	 * @param e MouseEvent - zdarzenie klikniêcia
	 */
	public void show(MouseEvent e) {
		super.show(this.getGraphPanel(), e.getX(), e.getY());
	}

	/**
	 * Metoda zwracaj¹ca nowy obiekt panelu graficznego.
	 * @return GraphPanel - obiekt
	 */
	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Metoda ustawiaj¹ca nowy obiekt panelu graficznego.
	 * @param graphPanel GraphPanel - obiekt
	 */
	public void setGraphPanel(GraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}

	/**
	 * Metoda generuj¹ca i dodaj¹ca do menu kontekstowego now¹ opcjê.
	 * @param text String - nazwa elementu
	 * @param iconName String - œcie¿ka do ikony
	 * @param actionListener ActionListener - obiekt nas³uchuj¹cy
	 */
	protected void addMenuItem(String text, String iconName, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(text, new ImageIcon("resources/icons/" + iconName + ".png"));
		menuItem.addActionListener(actionListener);
		this.add(menuItem);
	}

	/**
	 * Metoda odpowiedzialna za tworzenie obiektu JMenuItem
	 * @param text String - nazwa do wyœwietlania
	 * @param iconName String - œcie¿ka do ikonki
	 * @param accelerator KeyStroke - skrót klawiszowy
	 * @param actionListener ActionListener - obiekt nas³uchuj¹cy
	 * @return JMenuItem - gotowy obiekt menu
	 */
	protected JMenuItem createMenuItem(String text, String iconName,
			KeyStroke accelerator, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(text, new ImageIcon("resources/icons/" + iconName + ".png"));
		menuItem.addActionListener(actionListener);
		menuItem.setAccelerator(accelerator);
		return menuItem;
	}
}
