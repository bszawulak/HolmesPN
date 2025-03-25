package holmes.tables;

import java.io.Serial;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;


/**
 * Klasa-model dla tabeli tranzycji.
 */
public class TransitionsTableModel extends AbstractTableModel {
	@Serial
	private static final long serialVersionUID = -1557850148390063580L;

	/**
	 * Wewnętrzna klasa kontener dla danych wiersza tabeli tranzycji.
	 */
	public static class TransitionContainer {
    	public int ID;
    	public String name;
    	public int preP;
    	public int postP;
    	public float fired;
    	public int inInv;
    }
	
	private String[] columnNames = {"ID", "Transition name", "Pre-P", "Post-P", "Fired", "Inv"};
	private ArrayList<TransitionContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy miejsc.
	 */
	public TransitionsTableModel() {
		dataMatrix = new ArrayList<TransitionContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy tranzycji.
	 * @param id int - ID miejsca
	 * @param name String - nazwa miejsca
	 * @param preP int - liczba miejsc wejściowych
	 * @param postP int - liczba miejsc wyjściowych
	 * @param fired float - procent uruchomień tranzycji
	 * @param inv int - w ilu inwariantach tranzycja bierze udział
	 */
	public void addNew(int id, String name, int preP, int postP, float fired, int inv) {
		TransitionContainer tc = new TransitionContainer();
		tc.ID = id;
		tc.name = name;
		tc.preP = preP;
		tc.postP = postP;
		tc.fired = fired;
		tc.inInv = inv;
		dataMatrix.add(tc);
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
			case 2 -> dataMatrix.get(rowIndex).preP;
			case 3 -> dataMatrix.get(rowIndex).postP;
			case 4 -> dataMatrix.get(rowIndex).fired;
			case 5 -> dataMatrix.get(rowIndex).inInv;
			default -> throw new IllegalArgumentException("Invalid column index");
		};
	}
}
