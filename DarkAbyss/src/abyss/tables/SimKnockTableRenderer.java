package abyss.tables;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class SimKnockTableRenderer implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private JTable table;
	
	/**
	 * Konstruktor domyślny obiektów klasy MyRenderer.
	 */
	public SimKnockTableRenderer(JTable table) {
		this.table = table;
	}

	/**
	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli w zależności od ustawionego modelu tabeli.
	 * @param table Jtable - tabela danych
	 * @param value Object - wartość do komórki
	 * @param isSelected boolean - czy zaznaczona komórka
	 * @param hasFocus boolean - czy aktywna komórka
	 * @param row int - numer wiersza
	 * @param columnt int - numer kolumny
	 */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

    	return paintCellsDefault(value, isSelected, hasFocus, row, column);
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
	private Component paintCellsDefault(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		//renderer.setFont(new Font("Arial", Font.BOLD, 9));
    	//renderer.setBackground(Color.white);
	    return renderer;
	}
}
