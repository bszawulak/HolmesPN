package holmes.tables;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Klasa model dla tabeli podstawowych informacji o inwariantach.
 * @author MR
 */
public class InvariantsTableModel extends AbstractTableModel {
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
	 * @Param canon boolean - true oznacza kanoniczny
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
		Object returnValue = null;
		switch (columnIndex) {
        case 0:
            returnValue = dataMatrix.get(rowIndex).ID;
            break;
        case 1:
        	returnValue = dataMatrix.get(rowIndex).transNumber;
            break;
        case 2:
        	returnValue = dataMatrix.get(rowIndex).minimal;
            break;
        case 3:
        	returnValue = dataMatrix.get(rowIndex).feasible;
            break;
        case 4:
        	returnValue = dataMatrix.get(rowIndex).pureInTransitions;
            break;
        case 5:
        	returnValue = dataMatrix.get(rowIndex).inTransitions;
            break;
        case 6:
        	returnValue = dataMatrix.get(rowIndex).outTransitions;
            break;
        case 7:
        	returnValue = dataMatrix.get(rowIndex).readArcs;
            break;
        case 8:
        	returnValue = dataMatrix.get(rowIndex).inhibitors;
            break;
        case 9:
        	returnValue = dataMatrix.get(rowIndex).sur;
            break;
        case 10:
        	returnValue = dataMatrix.get(rowIndex).sub;
            break;
        case 11:
        	returnValue = dataMatrix.get(rowIndex).normalInv;
            break;
        case 12:
        	returnValue = dataMatrix.get(rowIndex).canonical;
            break;
        case 13:
        	returnValue = dataMatrix.get(rowIndex).name;
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }
         
        return returnValue;
	}
}
