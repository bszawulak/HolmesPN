package holmes.tables;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.table.DefaultTableModel;

import holmes.petrinet.elements.Transition.StochaticsType;
import holmes.windows.HolmesFiringRatesEditor;

/**
 * Model tabeli firing rates tranzycji sieci.
 * 
 * @author MR
 */
public class FiringRatesOneTransTableModel extends DefaultTableModel {
	private static final long serialVersionUID = -6898959322396110431L;
	private String[] columnNames;
	private ArrayList<FRDataClass> dataMatrix;
	private int dataSize;
	private HolmesFiringRatesEditor boss;
	private int frVectorIndex;
	public boolean changes = false;
	
	public class FRDataClass {
		public int ID;
		public String name;
		public Double firingRate;
		public StochaticsType subType;
	}

	/**
	 * Konstruktor klasy modelującej tablicę wektora firing rate.
	 */
	public FiringRatesOneTransTableModel(HolmesFiringRatesEditor boss, int frVectorIndex) {
		this.boss = boss;
		this.frVectorIndex = frVectorIndex;
		clearModel();
	}
	
	/**
	 * Czyści model tablicy.
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
	
	public void addNew(int ID, String name, double firingRate, StochaticsType sType) {
		FRDataClass row = new FRDataClass();
		row.ID = ID;
		row.name = name;
		row.firingRate = firingRate;
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
    	if(column == 2) {
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
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex) {
			case 0:
				return dataMatrix.get(rowIndex).ID;
			case 1:
				return dataMatrix.get(rowIndex).name;
			case 2:
				return dataMatrix.get(rowIndex).firingRate;
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
		double newValue = 0;
		try {
			if(col == 2) {
				newValue = Double.parseDouble(value.toString());
				dataMatrix.get(row).firingRate = newValue;
				boss.changeRealValue(frVectorIndex, row, newValue);
			}
		} catch (Exception e) {
			//dataMatrix.get(row).firingRate = 1.0;
		}
	}
}
