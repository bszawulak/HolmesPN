package holmes.tables;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Model tablicy danych o inwariancie na potrzeby okna podglądu inwariantów.
 * 
 * @author MR
 *
 */
public class InvariantsViewerTableModel extends AbstractTableModel {
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
		if(mctMode == true) {
			columnNames = new String[2];
			columnNames[0] = "ID";
			columnNames[1] = "Description";
		} else {
			columnNames = new String[4];
			columnNames[0] = "ID";
			columnNames[1] = "Transition name";
			columnNames[2] = "Fire%";
			columnNames[3] = "stdDev";
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
				return dataMatrix.get(rowIndex).get(columnIndex).toString();
			}
		}
	}
}
