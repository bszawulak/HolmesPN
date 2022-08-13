package holmes.tables;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.table.DefaultTableModel;


/**
 * Model tabeli węzłow dla okna łączenia sieci
 */
public class MergeNodesTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 6346267999771986379L;
	private String[] columnNames;
	private ArrayList<InternalData463> dataMatrix;
	private int dataSize;
	public boolean changes = false;
	
	public class InternalData463 {
		public int ID;
		public String name;
		public int preNodes;
		public int postNodes;
	}

	/**
	 * Konstruktor klasy modelującej tablicę wektora danych tranzycji SPN.
	 */
	public MergeNodesTableModel() {
		columnNames = new String[4];
		columnNames[0] = "ID";
		columnNames[1] = "Name";
		columnNames[2] = "preNodes";
		columnNames[3] = "postNodes";
		dataMatrix = new ArrayList<InternalData463>();
	}
	
	/**
	 * Czyści model tablicy danych tranzycji SPN.
	 */
	public void clearModel() {
		dataMatrix.clear();
		dataSize = 0;
	}
	
	/**
	 * Dodaje nowy wiersza do modelu tablicy.
	 * @param ID int - indeks węzła
	 * @param name String - nazwa węzła
	 * @param preNodes int - liczba węzłow wejściowych
	 * @param postNodes int - liczba węzłow wyjściowych
	 */
	public void addNew(int ID, String name, int preNodes, int postNodes) {
		InternalData463 row = new InternalData463();
		row.ID = ID;
		row.name = name;
		row.preNodes = preNodes;
		row.postNodes = postNodes;
		dataMatrix.add(row);
		dataSize++;
	}
	
	/**
	 * Usuwanie wiersza tabeli
	 */
	public void removeRow(int index) {
		dataMatrix.remove(index);
		dataSize--;
	}
	
	/**
	 * Usuwa wiersz danych typu merge.
	 * @param row String - jesli jest w tablicy, zostanie usunięty
	 */
	public void removeRow(String row) {
		for(InternalData463 dataRow : dataMatrix) {
			if(dataRow.name.equals(row)) {
				dataMatrix.remove(dataRow);
				dataSize--;
				return;
			}
		}
	}
	
	/**
	 * Zwraca listę poleceń łączących sieci.
	 * @return ArrayList[String] - wektor danych łączących
	 */
	public ArrayList<String> getMergeVector() {
		ArrayList<String> result = new ArrayList<String>();
		for(InternalData463 dataRow : dataMatrix) {
			result.add(dataRow.name);
		}
		return result;
	}
	
	/**
	 * Na potrzeby szukania wiersza merge w oknie łączenia sieci
	 * @param txt String - np. t21 <= t33
	 * @return int - indeks wiersza lub -1 jeśli brak
	 */
	public int elementIndex(String txt) {
		int counter = -1;
		for(InternalData463 row : dataMatrix) {
			counter++;
			if(row.name.equals(txt)) {
				return counter;
			}
			
		}
		return -1;
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
				return dataMatrix.get(rowIndex).preNodes;
			case 3:
				return dataMatrix.get(rowIndex).postNodes;
		}
		return null;
	}
}