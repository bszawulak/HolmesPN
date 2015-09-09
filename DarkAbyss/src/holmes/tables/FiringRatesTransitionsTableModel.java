package holmes.tables;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import holmes.windows.HolmesFiringRatesManager;

public class FiringRatesTransitionsTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 3869824360655880298L;
	private String[] columnNames;
	private ArrayList<FRvectorClass> dataMatrix;
	private int dataSize;
	public boolean changes = false;
	private HolmesFiringRatesManager boss;
	
	public class FRvectorClass {
		int ID;
		String frName;
	}

	public FiringRatesTransitionsTableModel(HolmesFiringRatesManager boss) {
		this.boss = boss;
		clearModel();
	}
	
	/**
	 * Czyści model tablicy.
	 */
	public void clearModel() {
		columnNames = new String[2];
		columnNames[0] = "ID";
		columnNames[1] = "Vector name";
		
		dataMatrix = new ArrayList<FRvectorClass>();
		dataSize = 0;
		
	}
	
	/**
	 * Dodawanie nowego wiersza do tablicy wektorów firing rates.
	 * @param ID int - indeks wektora
	 * @param name String - opis
	 */
	public void addNew(int ID, String name) {
		FRvectorClass row = new FRvectorClass();
		row.ID = ID;
		row.frName = name;
		dataMatrix.add(row);
		dataSize++;
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
    	if(column == 1)
    		return true;
    	else
    		return false;
    }

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return dataMatrix.get(rowIndex).ID;
			case 1:
				return dataMatrix.get(rowIndex).frName;
		}
		return null;
	}
	
	public void setValueAt(Object value, int row, int col) {
		try {
			if(col == 1) {
				dataMatrix.get(row).frName = value.toString();
				boss.changeState(row, col, value.toString());
			}
		} catch (Exception e) {
			//dataMatrix.get(row).firingRate = 1.0;
		}
	}
}
