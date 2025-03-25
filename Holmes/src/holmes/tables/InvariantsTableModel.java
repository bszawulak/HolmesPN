package holmes.tables;

import java.io.Serial;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Klasa model dla tabeli podstawowych informacji o inwariantach dla okna HolmesNetTables.
 */
public class InvariantsTableModel extends AbstractTableModel {
	@Serial
	private static final long serialVersionUID = 5760866352155772825L;
	private String[] columnNames = {"ID", "Tr.#", "Min.", "Feas.", "In-T", "pIn-T", "Out-T", "R-arc", "Inh.", "Sur", "Sub", "Cx0", "Canon.", "Name"};
	private ArrayList<InvariantContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy inwariantów.
	 */
	public InvariantsTableModel() {
		dataMatrix = new ArrayList<InvariantContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do tablicy danych inwariantów.
	 * @param id int - identyfikator porządkowy
	 * @param transN int - liczba tranzycji
	 * @param min boolean - true = minimalny
	 * @param feas boolean - true = feasible, wykonalny
	 * @param inT int - liczba tranzycji wejściowych
	 * @param pInT int - liczba czystych tranzycji wejściowych
	 * @param outT int - liczba tranzycji wyjściowych
	 * @param readArcs int - liczba łuków odczytu
	 * @param inhibitors int - liczba łuków blokujących
	 * @param sur int - true = sur-invariant
	 * @param sub int - true = sub-invariant
	 * @param normal boolean - normalny inwariant (Cx=0)
	 * @param canon boolean - true oznacza kanoniczny
	 * @param name String - nazwa (if any)
	 */
	public void addNew(int id, int transN, boolean min, boolean feas, int pInT, int inT, int outT, int readArcs, int inhibitors,
			boolean sur, boolean sub, boolean normal, boolean canon, String name) {
		InvariantContainer ic = new InvariantContainer();
		ic.ID = id;
		ic.transNumber = transN;
		ic.minimal = min;
		ic.feasible = feas;
		ic.pureInTransitions = pInT;
		ic.inTransitions = inT;
		ic.outTransitions = outT;
		ic.readArcs = readArcs;
		ic.inhibitors = inhibitors;
		ic.sur = sur;
		ic.sub = sub;
		ic.normalInv = normal;
		ic.canonical = canon;
		ic.name = name;
		dataMatrix.add(ic);
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
        if (dataMatrix.isEmpty() || columnIndex > columnNames.length) {
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
			case 1 -> dataMatrix.get(rowIndex).transNumber;
			case 2 -> dataMatrix.get(rowIndex).minimal;
			case 3 -> dataMatrix.get(rowIndex).feasible;
			case 4 -> dataMatrix.get(rowIndex).pureInTransitions;
			case 5 -> dataMatrix.get(rowIndex).inTransitions;
			case 6 -> dataMatrix.get(rowIndex).outTransitions;
			case 7 -> dataMatrix.get(rowIndex).readArcs;
			case 8 -> dataMatrix.get(rowIndex).inhibitors;
			case 9 -> dataMatrix.get(rowIndex).sur;
			case 10 -> dataMatrix.get(rowIndex).sub;
			case 11 -> dataMatrix.get(rowIndex).normalInv;
			case 12 -> dataMatrix.get(rowIndex).canonical;
			case 13 -> dataMatrix.get(rowIndex).name;
			default -> throw new IllegalArgumentException("Invalid column index");
		};
	}
}
