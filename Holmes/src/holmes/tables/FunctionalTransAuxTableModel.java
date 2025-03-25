package holmes.tables;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

/**
 * Pomocniczy model tabeli do wyświetlania wszystkich miejsc sieci.
 */
public class FunctionalTransAuxTableModel extends DefaultTableModel {
	@Serial
	private static final long serialVersionUID = -304515182472851604L;
	private String[] columnNames = {"ID", "Tokens", "Place name"};
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy funkcji.
	 */
	public FunctionalTransAuxTableModel() {
		dataMatrix = new ArrayList<>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy funkcji.
	 * @param pID String - ID miejsca
	 * @param tokens String - liczba tokenów w miejscu
	 * @param name String - nazwa miejsca
	 */
	public void addNew(String pID, String tokens, String name) {
		ArrayList<String> newRow = new ArrayList<String>();
		newRow.add(pID);
		newRow.add(tokens);
		newRow.add(name);
		dataMatrix.add(newRow);
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
    	return false;
    }

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
        return dataMatrix.get(rowIndex).get(columnIndex);
	}
}
