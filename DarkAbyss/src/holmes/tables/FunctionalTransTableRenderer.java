package holmes.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * Klasa renderująca tablę funkcji dla tranzycji.
 * 
 * @author MR
 */
public class FunctionalTransTableRenderer  implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private static final Font fontNormal =  new Font("Verdana", Font.PLAIN, 12);
	//private JTable table;
	
	/**
	 * Konstruktor domyślny obiektów klasy FunctionalTransTableRenderer.
	 */
	public FunctionalTransTableRenderer(JTable table) {
		//this.table = table;
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
    	
    	boolean correct = (boolean) table.getModel().getValueAt(row, 3);
		boolean enabled = (boolean) table.getModel().getValueAt(row, 6);
		
    	if(column != 3 && column != 6) {
    		JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    		oLabel.setBackground(Color.white);

    		oLabel.setFont(fontNormal);
    		
    		String val = value.toString();
    		if(column > 1) {
    			if(val.indexOf("0")==0 && val.indexOf(",")>1) {
    				val = val.substring(1);
    			}
    		}
        	
        	
        	oLabel.setText(val);
            return oLabel;
    	} else {
    		JCheckBox res = new JCheckBox();
    		//res.setEnabled(false);
    		if(column == 3) {
        		res.setSelected(correct);
        		if(!correct)
        			res.setBackground(Color.RED);
        		return res;
    		}
    		if(column == 6) {
        		res.setSelected(enabled);
        		if(!enabled)
        			res.setBackground(Color.DARK_GRAY);
        		return res;
    		}
    		
    		return null;
    	}
    }
}
