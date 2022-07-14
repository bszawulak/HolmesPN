package holmes.tables;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import holmes.petrinet.elements.Arc.TypeOfArc;

/**
 * Klasa modelująca tablicę edycji funkcji dla tranzycji.
 * 
 * @author MR
 */
public class FunctionalTransTableModel extends DefaultTableModel {
	@Serial
	private static final long serialVersionUID = 7486251580290447103L;

	/**
	 * Klasa pojemnik
	 */
	public class FContainer {
    	public String pID;
    	public String name;
    	public String function;
    	public boolean correct;
    	public TypeOfArc arcType;
    	public int weight;
    	public boolean enabled;
    }
	
	private String[] columnNames = {"Place", "Place name", "Function", "OK", "Arc type", "Weight", "Enabled"};
	private ArrayList<FContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy funkcji.
	 */
	public FunctionalTransTableModel() {
		dataMatrix = new ArrayList<FContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy funkcji.
	 * @param pID String - ID miejsca
	 * @param name String - nazwa miejsca
	 * @param function String - funkcja
	 * @param isOK boolean - czy f jest prawidłowa
	 * @param arcType TypesOfArcs - typ łuku
	 * @param weight int - waga łuku
	 * @param enable boolean - czy f jest aktywna
	 */
	public void addNew(String pID, String name, String function, boolean isOK, TypeOfArc arcType, int weight, boolean enable) {
		FContainer fC = new FContainer();
		fC.pID = pID;
		fC.name = name;
		fC.function = function;
		fC.correct = isOK;
		fC.arcType = arcType;
		fC.weight = weight;
		fC.enabled = enable;
		dataMatrix.add(fC);
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
		return switch (columnIndex) {
			case 0 -> dataMatrix.get(rowIndex).pID;
			case 1 -> dataMatrix.get(rowIndex).name;
			case 2 -> dataMatrix.get(rowIndex).function;
			case 3 -> dataMatrix.get(rowIndex).correct;
			case 4 -> dataMatrix.get(rowIndex).arcType;
			case 5 -> dataMatrix.get(rowIndex).weight;
			case 6 -> dataMatrix.get(rowIndex).enabled;
			default -> throw new IllegalArgumentException("Invalid column index");
		};
	}
	
	/**
	 * Metoda ustawia nową wartość w tabeli.
	 * @param value Object - nowa wartość
	 * @param row int - nr wiersza
	 * @param col int - nr kolumny
	 */
	public void setValueAt(Object value, int row, int col) {
		try {
			switch (col) {
				case 2 -> dataMatrix.get(row).function = value.toString();
				case 3 -> dataMatrix.get(row).correct = (boolean) value;
				case 4 -> dataMatrix.get(row).arcType = (TypeOfArc) value;
				case 5 -> dataMatrix.get(row).weight = (int) value;
				case 6 -> dataMatrix.get(row).enabled = (boolean) value;
			}
		} catch (Exception ignored) {
		}
	}
}
