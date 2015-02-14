package abyss.tables;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;


/**
 * Klasa-model dla tabeli tranzycji.
 * @author MR
 */
public class TransitionsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -1557850148390063580L;

	/**
	 * Wewnętrzna klasa kontener dla danych wiersza tabeli tranzycji.
	 * @author MR
	 *
	 */
	public class TransitionContainer {
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
		Object returnValue = null;
		switch (columnIndex) {
        case 0:
            returnValue = dataMatrix.get(rowIndex).ID;
            break;
        case 1:
        	returnValue = dataMatrix.get(rowIndex).name;
            break;
        case 2:
        	returnValue = dataMatrix.get(rowIndex).preP;
            break;
        case 3:
        	returnValue = dataMatrix.get(rowIndex).postP;
            break;
        case 4:
        	returnValue = dataMatrix.get(rowIndex).fired;
            break;
        case 5:
        	returnValue = dataMatrix.get(rowIndex).inInv;
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }
         
        return returnValue;
	}
}
