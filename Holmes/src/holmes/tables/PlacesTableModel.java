package holmes.tables;

import java.io.Serial;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Klasa model dla tabeli miejsc.
 */
public class PlacesTableModel extends AbstractTableModel {
	@Serial
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
		return switch (columnIndex) {
			case 0 -> dataMatrix.get(rowIndex).ID;
			case 1 -> dataMatrix.get(rowIndex).name;
			case 2 -> dataMatrix.get(rowIndex).token;
			case 3 -> dataMatrix.get(rowIndex).intT;
			case 4 -> dataMatrix.get(rowIndex).outT;
			case 5 -> dataMatrix.get(rowIndex).avgTokens;
			default -> throw new IllegalArgumentException("Invalid column index");
		};
	}
}
