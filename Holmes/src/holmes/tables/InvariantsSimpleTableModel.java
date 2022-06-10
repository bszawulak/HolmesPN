package holmes.tables;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

/**
 * Model tabeli prostego wyświetlania inwariantów - tylko ID i opis.
 * 
 * @author MR
 */
public class InvariantsSimpleTableModel extends DefaultTableModel {
	private static final long serialVersionUID = -1188248363088935217L;
	private String[] columnNames;
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy inwariantów.
	 */
	public InvariantsSimpleTableModel() {
		columnNames = new String[2];
		columnNames[0] = "ID";
		columnNames[1] = "Invariant name";

		dataMatrix = new ArrayList<ArrayList<String>>();
		dataSize = 0;
	}
	
	/**
	 * Metoda służaca do dodawania nowego wiersza (inwariantu) do tabeli danych.
	 * @param id int - id inwariantu
	 * @param name String - opis
	 */
	public void addNew(int id, String name) {
		ArrayList<String> vector = new ArrayList<String>();
		vector.add(""+id);
		vector.add(name);
		dataMatrix.add(vector);
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

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		String returnValue = dataMatrix.get(rowIndex).get(columnIndex);
        return returnValue;
	}
}
