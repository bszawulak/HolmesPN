package holmes.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * PTI - Places, Transitions, Invariants. Klasa-renderer dla tabel miejsc, tranzycji i inwariantów.
 * @author MR
 *
 */
public class PTITableRenderer implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private int mode = 0; //0 - places, 1 - transitions, 2 - invSimulationData 3 - inv.Simple,
	private JTable table;
	
	/**
	 * Konstruktor domyślny obiektów klasy MyRenderer.
	 */
	public PTITableRenderer(JTable table) {
		this.table = table;
	}
	
	/**
	 * Konstruktor obiektów klasy MyRenderer przyjmujący numer trybu rysowania.
	 * @param mode int - tryb rysowania
	 */
	public PTITableRenderer(int mode, int rows, JTable table) {
		this(table); //wywołanie konstruktora domyślnego
		this.mode = mode;
	}
	
	public void setSubRows(int rows) {
		//this.subRows = rows;
	}
	
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	/**
	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli w zależności od ustawionego
	 * modelu tabeli.
	 * @param table Jtable - tabela danych
	 * @param value Object - wartość do komórki
	 * @param isSelected boolean - czy zaznaczona komórka
	 * @param hasFocus boolean - czy aktywna komórka
	 * @param row int - numer wiersza
	 * @param columnt int - numer kolumny
	 */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
    	if(mode == 2) { //model tablicy inwariantów w wersji szczegółowej (symulacji wykonań)
    		return paintCellsInvariants(value, isSelected, hasFocus, row, column);
    	} else { //cała reszta
    		return paintCellsDefault(value, isSelected, hasFocus, row, column);
    	}
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
		Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		renderer.setBackground(Color.white);
    	renderer.setFont(new Font("Arial", Font.BOLD, 9));
    	InvariantsSimTableModel modelInvariants = (InvariantsSimTableModel) table.getModel();
    	
		if(modelInvariants.getInfeasibleInvariants().contains(row) == true) { //cały inw. na jasno szary
			renderer.setBackground(Color.lightGray);
			if(column > 1) {
				if(((String)modelInvariants.getValueAt(row, column)).contains("(0%)")) { // zagłodzona tranz. na ciemno szary
        			renderer.setBackground(Color.gray);
        		}
			}
		}
		
		if(column > 1) {
			if(modelInvariants.getZeroDeadTransitions().get(column-2) == 0) { //niepokryta tranzycja
				renderer.setBackground(Color.red);
			} else if(modelInvariants.getZeroDeadTransitions().get(column-2) == -1) { //zagłodzona
				renderer.setBackground(Color.gray);
			}
		}
		
	    return renderer;
	}
	
	/**
     * Metoda trybu ogólnego rysowania komórek tabeli.
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
