package holmes.graphpanel.popupmenu;

import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.XMLEncoder;
import java.io.FileOutputStream;
import java.io.Serial;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.utilities.Tools;

/**
 * Klasa odpowiedzialna za tworzenie menu kontekstowego dla sieci narysowanej na danym panelu.
 */
public class GraphPanelPopupMenu extends JPopupMenu {
	@Serial
	private static final long serialVersionUID = 2192129184059718857L;
	private GraphPanel graphPanel;
	protected JMenuItem cutMenuItem;
	protected JMenuItem copyMenuItem;
	protected JMenuItem pasteMenuItem;

	/**
	 * Konstruktor obiektu klasy GraphPanelPopupMenu.
	 * @param graphPanel GraphPanel - obiekt dla którego powstaje menu kontekstowe
	 */
	public GraphPanelPopupMenu(GraphPanel graphPanel, PetriNetElementType pne) {
		this.setGraphPanel(graphPanel);
		
		if(pne != PetriNetElementType.META)
			this.createPredefineMenuItems();
	}

	/**
	 * Metoda pomocnicza konstruktora, tworzy podstawowe elementy menu kontekstowego sieci.
	 */
	public void createPredefineMenuItems() {
		cutMenuItem = this.createMenuItem("Cut", "cut.png",
				KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK),
				e -> {
					// getGraphPanel().removeAllSelectedElementLocations();
				});

		copyMenuItem = this.createMenuItem("Copy", "copying_and_distribution.png",
				KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK),
				e -> {
					@SuppressWarnings("unused")
					int x = 1;
					// getGraphPanel().removeAllSelectedElementLocations();
				});

		pasteMenuItem = this.createMenuItem("Paste", "paste_plain.png",
				KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK),
				e -> {
					//TODO
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
	 * Metoda pokazująca menu w danym klikniętym miejscu.
	 * @param e MouseEvent - zdarzenie kliknięcia
	 */
	public void show(MouseEvent e) {
		super.show(this.getGraphPanel(), e.getX(), e.getY());
	}

	/**
	 * Metoda zwracająca nowy obiekt panelu graficznego.
	 * @return GraphPanel - obiekt
	 */
	public GraphPanel getGraphPanel() {
		return graphPanel;
	}

	/**
	 * Metoda ustawiająca nowy obiekt panelu graficznego.
	 * @param graphPanel GraphPanel - obiekt
	 */
	public void setGraphPanel(GraphPanel graphPanel) {
		this.graphPanel = graphPanel;
	}

	/**
	 * Metoda generująca i dodająca do menu kontekstowego nową opcję.
	 * @param text String - nazwa elementu
	 * @param iconName String - ścieżka do ikony
	 * @param actionListener ActionListener - obiekt nasłuchujący
	 */
	protected void addMenuItem(String text, String iconName, ActionListener actionListener) {
		try {
			JMenuItem menuItem;
			if(!iconName.equals(""))
				menuItem = new JMenuItem(text, Tools.getResIcon16("/icons/" + iconName));
			else
				menuItem = new JMenuItem(text);
			menuItem.addActionListener(actionListener);
			this.add(menuItem);
		} catch (Exception e) {
			JMenuItem menuItem = new JMenuItem(text);
			menuItem.addActionListener(actionListener);
			this.add(menuItem);
		}
	}

	/**
	 * Metoda odpowiedzialna za tworzenie obiektu JMenuItem
	 * @param text String - nazwa do wyświetlania
	 * @param iconName String - ścieżka do ikonki
	 * @param accelerator KeyStroke - skrót klawiszowy
	 * @param actionListener ActionListener - obiekt nasłuchujący
	 * @return JMenuItem - gotowy obiekt menu
	 */
	protected JMenuItem createMenuItem(String text, String iconName, KeyStroke accelerator, ActionListener actionListener) {
		try {
			JMenuItem menuItem;
			if(!iconName.equals(""))
				menuItem = new JMenuItem(text, Tools.getResIcon16("/icons/" + iconName));
			else
				menuItem = new JMenuItem(text);
			menuItem.addActionListener(actionListener);
			menuItem.setAccelerator(accelerator);
			return menuItem;
		} catch (Exception e) {
			JMenuItem menuItem = new JMenuItem(text);
			menuItem.addActionListener(actionListener);
			menuItem.setAccelerator(accelerator);
			return menuItem;
		}
	}
}
