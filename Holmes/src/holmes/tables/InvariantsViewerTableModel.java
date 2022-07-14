package holmes.tables;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

/**
 * Model tablicy danych o inwariancie na potrzeby okna podglądu inwariantów.
 * 
 * @author MR
 *
 */
public class InvariantsViewerTableModel extends DefaultTableModel  {
	@Serial
	private static final long serialVersionUID = -6625961285405380868L;
	private String[] columnNames;
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;
	boolean mctMode;
	
	public boolean changes = false;

	/**
	 * Konstruktor klasy modelującej tablicę stanów początkowych.
	 */
	public InvariantsViewerTableModel(boolean mctMode) {
		this.mctMode = mctMode;
		
		if(mctMode) {
			columnNames = new String[2];
			columnNames[0] = "ID";
			columnNames[1] = "Element";
		} else {
			columnNames = new String[5];
			columnNames[0] = "ID";
			columnNames[1] = "Transition name";
			columnNames[2] = "Support";
			columnNames[3] = "Fire%";
			columnNames[4] = "stdDev";
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
	
	public void clear() {
		dataMatrix.clear();
		dataSize=0;
	}
	
	/**
	 * Zwraca wartość edytowalności pola.
	 */
	public boolean isCellEditable(int row, int column) {
		return false;
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

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object returnValue;
		if(mctMode) {
			if(columnIndex > 1)
				return null;
			else
				return dataMatrix.get(rowIndex).get(columnIndex);
		} else {
			if(columnIndex < 3) {
				return dataMatrix.get(rowIndex).get(columnIndex);
			} else {
				try {
					returnValue = dataMatrix.get(rowIndex).get(columnIndex);
					String strVal = returnValue.toString();
					return Double.parseDouble(strVal);
				} catch (Exception e) {
					return dataMatrix.get(rowIndex).get(columnIndex);
				}
			}
		}
	}
}
