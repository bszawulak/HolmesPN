package holmes.tables.managers;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Klasa rysujące tabelę FiringCalcTableModel.
 * Nieautorskie.  Author: MR
 *
 */
public class FiringCalcTableRenderer extends JTextArea implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	public static final Font fontNormal =  new Font("Verdana", Font.PLAIN, 11);
	
	public FiringCalcTableRenderer() {
		setLineWrap(true);
		setWrapStyleWord(true);
	}


	/**
	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli.
	 * @param table Jtable - tabela danych
	 * @param value Object - wartość do komórki
	 * @param isSelected boolean - czy zaznaczona komórka
	 * @param hasFocus boolean - czy aktywna komórka
	 * @param row int - numer wiersza
	 * @param column int - numer kolumny
	 */
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
												   boolean hasFocus, int row, int column) {

		JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		oLabel.setBackground(Color.white);
		oLabel.setFont(fontNormal);
		int selectedRow = table.getSelectedRow();

		if(selectedRow == row)
			oLabel.setBackground(Color.lightGray);

		oLabel.setText(value.toString());
		return oLabel;
	}
}
