package abyss.tables;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Klasa model dla tabeli miejsc.
 * @author MR
 */
public class PlacesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -4767203046375075488L;
	/**
	 * Wewnętrzna klasa kontener dla danych wiersza tabeli miejsc.
	 * @author MR
	 *
	 */
	public class PlaceContainer {
    	public int ID;
    	public String name;
    	public int token;
    	public int intT;
    	public int outT;
    	public float avgTokens;
    }
	
	private String[] columnNames = {"ID", "Place name", "Tok", "In-T", "Out-T", "AvgTk"};
	private ArrayList<PlaceContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy miejsc.
	 */
	public PlacesTableModel() {
		dataMatrix = new ArrayList<PlaceContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy miejsc.
	 * @param id int - ID miejsca
	 * @param name String - nazwa miejsca
	 * @param token int - liczba tokenów
	 * @param intT int - liczba tranzycji wejściowych
	 * @param outT int - liczba tranzycji wyjściowych
	 * @param avgTokens int - średnia liczba tokenów
	 */
	public void addNew(int id, String name, int token, int intT, int outT, float avgTokens) {
		PlaceContainer pc = new PlaceContainer();
		pc.ID = id;
		pc.name = name;
		pc.token = token;
		pc.intT = intT;
		pc.outT = outT;
		pc.avgTokens = avgTokens;
		dataMatrix.add(pc);
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
		switch (columnIndex) {
        case 0:
            returnValue = dataMatrix.get(rowIndex).ID;
            break;
        case 1:
        	returnValue = dataMatrix.get(rowIndex).name;
            break;
        case 2:
        	returnValue = dataMatrix.get(rowIndex).token;
            break;
        case 3:
        	returnValue = dataMatrix.get(rowIndex).intT;
            break;
        case 4:
        	returnValue = dataMatrix.get(rowIndex).outT;
            break;
        case 5:
        	returnValue = dataMatrix.get(rowIndex).avgTokens;
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }
         
        return returnValue;
	}
}
