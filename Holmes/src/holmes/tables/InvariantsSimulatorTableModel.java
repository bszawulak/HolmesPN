package holmes.tables;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import holmes.darkgui.GUIManager;

/**
 * Klasa model dla tabeli inwariantów okna HolmesNetTables.
 * @author MR
 */
public class InvariantsSimulatorTableModel extends AbstractTableModel {
	@Serial
	private static final long serialVersionUID = -1557850148390063580L;
	
	private ArrayList<Integer> infeasibleInvariants;
	private ArrayList<Integer> zeroDeadTransitions;
	private String[] columnNames;
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy inwariantów.
	 */
	public InvariantsSimulatorTableModel(int transNumber) {
		columnNames = new String[transNumber+2];
		columnNames[0] = "ID";
		columnNames[1] = "Trans.#:";
		for(int i=0; i<transNumber; i++) {
			columnNames[i+2] = "t"+i;
		}
		
		dataMatrix = new ArrayList<ArrayList<String>>();
		dataSize = 0;
	}
	
	/**
	 * Metoda służaca do dodawania nowego wiersza (inwariantu) do tabeli danych.
	 * @param dataRow ArrayList[String] - wiersz danych
	 */
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
				//int value = Integer.parseInt(returnValue.toString());
				return returnValue;
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Invariants table malfunction: non-numerical value in 1st or 2nd column.", "error", true);
			}
		} else {
			returnValue = dataMatrix.get(rowIndex).get(columnIndex);
			return returnValue.toString();
		}
        return returnValue;
	}
	
	/**
	 * Metoda ustawia nowy wektor z informacją które inwarianty mają zagłodzone tranzycje.
	 * @param infInv ArrayList[Integer] - wektor danych
	 */
	public void setInfeasibleInvariants(ArrayList<Integer> infInv) {
		this.infeasibleInvariants = infInv;
	}
	
	/**
	 * Metoda zwraca wektor danych o inwariantach z zagłodzonymi tranzycjami.
	 * @return ArrayList[Integer] - wektor danych
	 */
	public ArrayList<Integer> getInfeasibleInvariants() {
		return infeasibleInvariants;
	}
	
	/**
	 * Metoda ustawia nowy wektor z informacją które tranzycje są zagłodzone lub
	 * niepokryte inwariantami.
	 * @param zeroTrans ArrayList[Integer] - wektor danych
	 */
	public void setZeroDeadTransitions(ArrayList<Integer> zeroTrans) {
		this.zeroDeadTransitions = zeroTrans;
	}
	
	/**
	 * Metoda zwraca wektor danych z martwymi lub niepokrytymi tranzycjami.
	 * @return ArrayList[Integer] - wektor danych
	 */
	public ArrayList<Integer> getZeroDeadTransitions() {
		return zeroDeadTransitions;
	}
}
