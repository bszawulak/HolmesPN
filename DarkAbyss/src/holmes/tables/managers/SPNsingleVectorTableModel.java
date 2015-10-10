package holmes.tables.managers;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.table.DefaultTableModel;

import holmes.petrinet.elements.Transition.StochaticsType;
import holmes.windows.managers.HolmesSPNeditor;

/**
 * Model tabeli tranzycji dla jednego wektora danych SPN.
 * 
 * @author MR
 */
public class SPNsingleVectorTableModel extends DefaultTableModel {
	private static final long serialVersionUID = -6898959322396110431L;
	private String[] columnNames;
	private ArrayList<FRDataClass> dataMatrix;
	private int dataSize;
	@SuppressWarnings("unused")
	private HolmesSPNeditor boss;
	@SuppressWarnings("unused")
	private int frVectorIndex;
	public boolean changes = false;
	
	public class FRDataClass {
		public int ID;
		public String name;
		public String dataVector;
		public StochaticsType subType;
	}

	/**
	 * Konstruktor klasy modelującej tablicę wektora danych tranzycji SPN.
	 */
	public SPNsingleVectorTableModel(HolmesSPNeditor boss, int frVectorIndex) {
		this.boss = boss;
		this.frVectorIndex = frVectorIndex;
		clearModel();
	}
	
	/**
	 * Czyści model tablicy danych tranzycji SPN.
	 */
	public void clearModel() {
		columnNames = new String[4];
		columnNames[0] = "ID";
		columnNames[1] = "Transition Name";
		columnNames[2] = "Firing rate";
		columnNames[3] = "SPN subtype";
		
		dataMatrix = new ArrayList<FRDataClass>();
		dataSize = 0;
		
	}
	
	/**
	 * Dodaje nowy wiersza do modelu tablicy danych tranzycji SPN.
	 * @param ID int - indeks trabzycji
	 * @param name String - nazwa tranzycji
	 * @param SPNtransData String - dane dla SPN
	 * @param sType StochaticsType - podtyp w SPN
	 */
	public void addNew(int ID, String name, String SPNtransData, StochaticsType sType) {
		FRDataClass row = new FRDataClass();
		row.ID = ID;
		row.name = name;
		row.dataVector = SPNtransData;
		row.subType = sType;
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
    	return false;
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
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return dataMatrix.get(rowIndex).ID;
			case 1:
				return dataMatrix.get(rowIndex).name;
			case 2:
				return dataMatrix.get(rowIndex).dataVector;
			case 3:
				return dataMatrix.get(rowIndex).subType;
		}
		return null;
	}
	
	/**
	 * Metoda pozwala zmienić edytowalne komórki tabeli.
	 * @param value Object - nowa wartość
	 * @param row int - nr wiersza (tranzycja)
	 * @param col int - nr kolumny
	 */
	public void setValueAt(Object value, int row, int col) {
		try {
			if(col == 2) {
				dataMatrix.get(row).dataVector = value.toString();
			} else if(col == 3) {
				dataMatrix.get(row).subType = (StochaticsType)value;
			}
		} catch (Exception e) {

		}
	}
}
