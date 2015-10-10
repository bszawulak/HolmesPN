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
 * Renderer tablicy wyświetlania wszystkich wektorów danych SPN.
 * 
 * @author MR
 */
public class SPNdataVectorsRenderer implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private static final DecimalFormat formatter = new DecimalFormat( "#" );
	@SuppressWarnings("unused")
	private JTable table;
	private static final Font fontNormal =  new Font("Verdana", Font.PLAIN, 9);
	private static final Font fontBold =  new Font("Verdana", Font.BOLD, 9);
	
	/**
	 * Konstruktor domyślny obiektów klasy SPNdataVectorsRenderer.
	 */
	public SPNdataVectorsRenderer(JTable table) {
		this.table = table;
	}

	
	/**
	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli.
	 * @param table Jtable - tabela danych
	 * @param value Object - wartość do komórki
	 * @param isSelected boolean - czy zaznaczona komórka
	 * @param hasFocus boolean - czy aktywna komórka
	 * @param row int - numer wiersza
	 * @param columnt int - numer kolumny
	 */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {

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
            value = formatter.format((Number)value);
        }
    	
    	
    	oLabel.setText(value.toString());
        return oLabel;
    }
}
