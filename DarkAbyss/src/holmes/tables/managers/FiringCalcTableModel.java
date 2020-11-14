package holmes.tables.managers;

import holmes.windows.HolmesFiringRateAlgViewer;

import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

/**
 * Model tabeli dla wyświetlania częstości tranzycji w widoku uruchamiana algorytmów.
 * Nieautorskie.  Author: MR
 * 
 */
public class FiringCalcTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 520533713355418653L;
	private String[] columnNames;
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;
	private HolmesFiringRateAlgViewer boss;

	public boolean changes = false;
	
	public FiringCalcTableModel(HolmesFiringRateAlgViewer boss) {
		this.boss = boss;
		clearModel();
	}
	
	/**
	 * Czyści model tablicy.
	 */
	public void clearModel() {
		columnNames = new String[3];
		columnNames[0] = "Name";
		columnNames[1] = "ID";
		columnNames[2] = "Firing frequency";

		dataMatrix = new ArrayList<ArrayList<String>>();
		dataSize = 0;
		
	}
	
	/**
	 * Metoda służaca do dodawania nowego wiersza  do tabeli danych.
	 * @param dataRow ArrayList[String] - wiersz danych
	 */
	public void addNew(ArrayList<String> dataRow) {
		dataMatrix.add(dataRow);
		dataSize++;
	}
	
	/**
	 * Metoda ustawia flagę wyboru odpowiedniego stanu.
	 * @param row int - nr wiersza
	 */
	public void setSelected(int row) {
		for(int i=0; i<dataSize; i++) {
			if(i == row)
				dataMatrix.get(i).set(0, "X");
			else
				dataMatrix.get(i).set(0, "");
		}
	}
	
	/**
	 * Metoda zwracająca liczbę kolumn.
	 * @return int - liczba kolumn
	 */
	public int getColumnCount() {
		return columnNames.length;
	}
	
	/**
	 * Metoda zwracająca aktualną liczbę wierszy danych.
	 * @return int - liczba wierszy
	 */
	public int getRowCount() {
		return dataSize;
	}
	
	/**
	 * Metoda zwracająca nazwy kolumn.
	 * @param columnIndex int - numer kolumny
	 * @return String - nazwa kolumny
	 */
	public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
     
	/**
	 * Metoda zwraca typ pola danej kolumny.
	 * @param columnIndex int - numer kolumny
	 * @return Class - typ danych w kolumnie
	 */
    public Class<?> getColumnClass(int columnIndex) {
        if (dataMatrix.isEmpty()) {
            return Object.class;
        }
        return getValueAt(0, columnIndex).getClass();
    }
    
    /**
     * Zwraca status edytowalności komórek.
     */
    public boolean isCellEditable(int row, int column) {
    	if(column > 1) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    /**
     * Określa po ilu kliknięciach następuje edycja komórki
     * @param evt EventObject
     * @return boolean
     */
    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            int clickCount = 1;

            return ((MouseEvent)evt).getClickCount() >= clickCount;
        }
        return true;
    }

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object returnValue = null;
		if(columnIndex < 2) {
			return dataMatrix.get(rowIndex).get(columnIndex).toString();
		} else {
			try {
				returnValue = dataMatrix.get(rowIndex).get(columnIndex);
				String strVal = returnValue.toString();
				double val = Double.parseDouble(strVal);
				return val;
			} catch (Exception e) {
				return -1;
			}
		}
	}
	
	/**
	 * Ustawia nową wartość we wskazanej komórce tabeli stanów.
	 * @param value Object - nowa wartość, tokeny
	 * @param row int - nr wiersza
	 * @param col int - nr kolumny
	 */
	public void setValueAt(Object value, int row, int col) {
		double newValue = 0;
		try {
			newValue = Double.parseDouble(value.toString());
			if(newValue < 0)
				newValue = 0;
			ArrayList<String> rowVector = dataMatrix.get(row);
			rowVector.set(col, ""+newValue);

			boss.changeState(row, col, newValue);
		} catch (Exception e) {

		}
	}
}
