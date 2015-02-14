package abyss.tables;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import abyss.darkgui.GUIManager;

/**
 * Klasa model dla tabeli inwariantów.
 * @author MR
 */
public class InvariantsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1557850148390063580L;
	
	//private String[] columnNames = {"ID", "Transition name", "Pre-P", "Post-P", "Fired", "Inv"};
	private String[] columnNames;
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy inwariantów.
	 */
	public InvariantsTableModel(int transNumber) {
		columnNames = new String[transNumber+2];
		columnNames[0] = "ID";
		columnNames[1] = "Trans.#:";
		for(int i=0; i<transNumber; i++) {
			columnNames[i+2] = "t"+i;
		}
		
		dataMatrix = new ArrayList<ArrayList<String>>();
		dataSize = 0;
	}
	
	
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

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object returnValue = null;
		if(columnIndex < 2) {
			try {
				returnValue = dataMatrix.get(rowIndex).get(columnIndex);
				int value = Integer.parseInt(returnValue.toString());
				return value;
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Invariants table malfunction: non-numerical value in 1st or 2nd column.", "error", true);
			}
		} else {
			returnValue = dataMatrix.get(rowIndex).get(columnIndex);
		}
        return returnValue;
	}
}
