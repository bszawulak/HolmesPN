package holmes.tables;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

/**
 * Model tabeli stanów początkowych sieci.
 * 
 * @author MR
 *
 */
public class StatesPlacesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 7776195572631920285L;
	private String[] columnNames;
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;

	/**
	 * Konstruktor klasy modelującej tablicę stanów początkowych.
	 */
	public StatesPlacesTableModel(int placesNumber) {
		columnNames = new String[placesNumber+2];
		columnNames[0] = "Selected";
		columnNames[1] = "mID";
		for(int i=0; i<placesNumber; i++) {
			columnNames[i+2] = "t"+i;
		}
		
		dataMatrix = new ArrayList<ArrayList<String>>();
		dataSize = 0;
	}
	
	/**
	 * Metoda służaca do dodawania nowego wiersza (danych tranzycji) do tabeli danych.
	 * @param dataRow ArrayList[String] - wiersz danych
	 */
	public void addNew(ArrayList<String> dataRow) {
		dataMatrix.add(dataRow);
		dataSize++;
	}
	
	/**
	 * Metoda zwracająca liczbę kolumn.
	 * @return int - liczba kolumn
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}
	
	/**
	 * Metoda zwracająca aktualną liczbę wierszy danych.
	 * @return int - liczba wierszy
	 */
	@Override
	public int getRowCount() {
		return dataSize;
	}
	
	/**
	 * Metoda zwracająca nazwy kolumn.
	 * @param columnIndex int - numer kolumny
	 * @return String - nazwa kolumny
	 */
	@Override
	public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
     
	/**
	 * Metoda zwraca typ pola danej kolumny.
	 * @param columnIndex int - numer kolumny
	 * @return Class - typ danych w kolumnie
	 */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (dataMatrix.isEmpty()) {
            return Object.class;
        }
        return getValueAt(0, columnIndex).getClass();
    }
    
    public boolean isCellEditable(int row, int column) {
    	if(column > 1) {
    		return true;
    	} else { 
    		return false;
    	}
    }
    
    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            int clickCount = 1;

            // For single-click activation
            //clickCount = 1;
            // For double-click activation
            //clickCount = 2;
            // For triple-click activation
            //clickCount = 3;

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
	
	public void setValueAt(Object value, int row, int col) {
		ArrayList<String> rowVector = dataMatrix.get(row);
		rowVector.set(col, ""+value);
	}
}
