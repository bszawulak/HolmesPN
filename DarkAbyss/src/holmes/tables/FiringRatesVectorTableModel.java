package holmes.tables;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import holmes.windows.HolmesFiringRatesManager;

/**
 * Klasa tablicy firing rates dla tranzycji.
 * 
 * @author MR
 */
public class FiringRatesVectorTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 3869824360655880298L;
	private String[] columnNames;
	private ArrayList<FRvectorClass> dataMatrix;
	private int dataSize;
	public boolean changes = false;
	private HolmesFiringRatesManager boss;
	
	public class FRvectorClass {
		String selected;
		int ID;
		String frName;
	}

	/**
	 * Konstruktor obiektu tablicy FiringRatesTransitionsTableModel.
	 * @param boss HolmesFiringRatesManager - główne okno managera
	 */
	public FiringRatesVectorTableModel(HolmesFiringRatesManager boss) {
		this.boss = boss;
		clearModel();
	}
	
	/**
	 * Czyści model tablicy.
	 */
	public void clearModel() {
		columnNames = new String[3];
		columnNames[0] = "Selected";
		columnNames[1] = "ID";
		columnNames[2] = "Vector name";
		
		dataMatrix = new ArrayList<FRvectorClass>();
		dataSize = 0;
	}
	
	/**
	 * Dodawanie nowego wiersza do tablicy wektorów firing rates.
	 * @param selected String - czy wybrany wektor
	 * @param ID int - indeks wektora
	 * @param name String - opis
	 */
	public void addNew(String selected, int ID, String name) {
		FRvectorClass row = new FRvectorClass();
		row.selected = selected;
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
    	if(column == 2)
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
				return dataMatrix.get(rowIndex).selected;
			case 1:
				return dataMatrix.get(rowIndex).ID;
			case 2:
				return dataMatrix.get(rowIndex).frName;
		}
		return null;
	}
	
	public void setValueAt(Object value, int row, int col) {
		try {
			if(col == 2) {
				dataMatrix.get(row).frName = value.toString();
				boss.changeState(row, col, value.toString());
			}
		} catch (Exception e) {
			//dataMatrix.get(row).firingRate = 1.0;
		}
	}
	
	/**
	 * Metoda ustawia flagę wyboru odpowiedniego stanu fr.
	 * @param row int - nr wiersza
	 */
	public void setSelected(int row) {
		for(int i=0; i<dataSize; i++) {
			if(i == row)
				dataMatrix.get(i).selected = "X";
			else
				dataMatrix.get(i).selected = "";
		}
	}
}
