package holmes.tables;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Klasa renderująca tablicę tranzycji inwariantu w oknie podglądu inwariantów.
 * 
 * @author MR
 *
 */
public class InvariantsTableRenderer implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private JTable table;
	
	/**
	 * Konstruktor domyślny obiektów klasy StatesPlacesTableRenderer.
	 */
	public InvariantsTableRenderer(JTable table) {
		this.table = table;
	}

	
	/**
	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli w zależności od ustawionego
	 * modelu tabeli.
	 * @param table Jtable - tabela danych
	 * @param value Object - wartość do komórki
	 * @param isSelected boolean - czy zaznaczona komórka
	 * @param hasFocus boolean - czy aktywna komórka
	 * @param row int - numer wiersza
	 * @param column int - numer kolumny
	 */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
    	return paintCellsInvariants(value, isSelected, hasFocus, row, column);
    }

    /**
     * Metoda trybu rysowania dla komórek tabeli inwariantów.
     * @param value Object - wartość do wpisania
     * @param isSelected boolean - czy komórka jest wybrana
     * @param hasFocus boolean - czy jest aktywna
     * @param row int - nr wiersza
     * @param column int - nr kolumny
     * @return Component - konkretnie: JTextField jako komórka tabeli
     */
	private Component paintCellsInvariants(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		oLabel.setBackground(Color.white);
		//oLabel.setFont(fontNormal);
		
		String val = value.toString();
		if(column > 1) {
			if(val.indexOf("0")==0 && val.indexOf(",")>1) {
				val = val.substring(1);
			}
		}
    	
    	
    	oLabel.setText(val);
        return oLabel;

	}
}
