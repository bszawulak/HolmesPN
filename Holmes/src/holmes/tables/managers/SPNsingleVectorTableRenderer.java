package holmes.tables.managers;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import holmes.petrinet.elements.extensions.TransitionSPNExtension;

/**
 * Klasa rysująca tablicę tranzycji dla jednego wektora danych SPN.
 */
public class SPNsingleVectorTableRenderer implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private JTable table;
	private static final Font fontNormal =  new Font("Verdana", Font.PLAIN, 12);
	
	/**
	 * Konstruktor domyślny obiektów klasy SPNoneTransTableRenderer.
	 */
	public SPNsingleVectorTableRenderer(JTable table) {
		this.table = table;
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
		if(column < 2) {
			oLabel.setText(value.toString());
		} else if(column == 2) {
			
			oLabel.setText(value.toString());
		} else {
			TransitionSPNExtension.StochaticsType sType = (TransitionSPNExtension.StochaticsType)value;
			if(sType == TransitionSPNExtension.StochaticsType.ST) {
				oLabel.setText("Stochastic");
			} else if(sType == TransitionSPNExtension.StochaticsType.IM) {
				oLabel.setText("Immediate");
			}	else if(sType == TransitionSPNExtension.StochaticsType.DT) {
				oLabel.setText("Deterministic");
			}  else {
				oLabel.setText("Scheduled");
			}
		}

        return oLabel;
    }
}
