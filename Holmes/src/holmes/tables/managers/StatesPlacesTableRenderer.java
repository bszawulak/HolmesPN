package holmes.tables.managers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Klasa obiektu rysującego tablicę stanów sieci (klasyczna).
 */
public class StatesPlacesTableRenderer implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private static final DecimalFormat formatter = new DecimalFormat( "#" );
	private final JTable table;
	private static final Font fontNormal =  new Font("Verdana", Font.PLAIN, 9);
	private static final Font fontBold =  new Font("Verdana", Font.BOLD, 9);
	
	/**
	 * Konstruktor domyślny obiektów klasy StatesPlacesTableRenderer.
	 */
	public StatesPlacesTableRenderer(JTable table) {
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
     * Metoda trybu rysowania dla komórek tabeli.
     * @param value Object - wartość do wpisania
     * @param isSelected boolean - czy komórka jest wybrana
     * @param hasFocus boolean - czy jest aktywna
     * @param row int - nr wiersza
     * @param column int - nr kolumny
     * @return Component - konkretnie: JTextField jako komórka tabeli
     */
	private Component paintCellsInvariants(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		//Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		//renderer.setBackground(Color.white);
    	//renderer.setFont(new Font("Arial", Font.BOLD, 9));
		//renderer.setFont(fontNormal);
    	
		JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		oLabel.setBackground(Color.white);
		oLabel.setFont(fontNormal);
		int selectedRow = table.getSelectedRow();
		Object firstCell = table.getValueAt(row, 0);

    	if(firstCell.toString().equals("X")) {
    		oLabel.setFont(fontBold);
    	}
    	if(selectedRow == row)
    		oLabel.setBackground(Color.lightGray);

    	if(value instanceof Double) {
            value = formatter.format(value);
        }
    	
    	oLabel.setText(value.toString());
        return oLabel;

	    //return renderer;
	}
}
