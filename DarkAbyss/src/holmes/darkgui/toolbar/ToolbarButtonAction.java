package holmes.darkgui.toolbar;

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

	/**
	 * Konstuktor obiektów klasy ToolbarButtonAction.
	 * @param parentComponent Component - obiekt swing
	 * @param name String - nazwa
	 * @param description String - opis
	 * @param icon Icon - ikona
	 * @param message String - tekst do wyświetlenia
	 */
	public ToolbarButtonAction(Component parentComponent, String name, String description, Icon icon, String message) {
		super(null, icon);
		putValue(Action.SHORT_DESCRIPTION, description);
		this.message = message;
		this.name = name;
		this.parentComponent = parentComponent;
	}
	
	/**
	 * Konstuktor obiektów klasy ToolbarButtonAction.
	 * @param parentComponent Component - obiekt swing
	 * @param name String - nazwa
	 * @param description String - opis
	 * @param icon Icon - ikona
	 */
	public ToolbarButtonAction(Component parentComponent, String name, String description, Icon icon) {
		super(null, icon);
		putValue(Action.SHORT_DESCRIPTION, description);
		this.name = name;
		this.parentComponent = parentComponent;
	}

	/**
	 * Metoda wyświetlająca komunikat dla przycisku w ramach reakcji na zdarzenie zatrzymania kursora nad przyciskiem.
	 * @param actionEvent ActionEvent
	 */
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		JOptionPane.showMessageDialog(parentComponent, message, name, JOptionPane.INFORMATION_MESSAGE);
	}

}
