package holmes.tables.managers;

import holmes.windows.HolmesRFreeAlgViewer;

import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;

/**
 * Model tabeli dla wyświetlania wartości firingRate Tranzycji w widoku wyników dla algorytmu uruchamiania tranzycji.
 * Nieautorskie.  Author: MR
 *
 */
public class TParameterTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 9062491299037960094L;
    private String[] columnNames;
    private ArrayList<ArrayList<String>> dataMatrix;
    private int dataSize;
    private HolmesRFreeAlgViewer boss;

    public boolean changes = false;

    /**
     * Konstruktor klasy modelującej tablicę stanów początkowych.
     */
    public TParameterTableModel(HolmesRFreeAlgViewer boss) {
        this.boss = boss;
        clearModel();
    }

    /**
     * Czyści model tablicy.
     *
     */
    public void clearModel() {
        columnNames = new String[2];
        columnNames[0] = "tID";
        columnNames[1] = "firingRate";

        dataMatrix = new ArrayList<ArrayList<String>>();
        dataSize = 0;

    }

    /**
     * Metoda służaca do dodawania nowego wiersza (danych tranzycji) do tabeli danych.
     *
     * @param dataRow ArrayList[String] - wiersz danych
     */
    public void addNew(ArrayList<String> dataRow) {
        dataMatrix.add(dataRow);
        dataSize++;
    }

    /**
     * Metoda ustawia flagę wyboru odpowiedniego stanu.
     *
     * @param row int - nr wiersza
     */
    public void setSelected(int row) {
        for (int i = 0; i < dataSize; i++) {
            if (i == row)
                dataMatrix.get(i).set(0, "X");
            else
                dataMatrix.get(i).set(0, "");
        }
    }

    /**
     * Metoda zwracająca liczbę kolumn.
     *
     * @return int - liczba kolumn
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Metoda zwracająca aktualną liczbę wierszy danych.
     *
     * @return int - liczba wierszy
     */
    public int getRowCount() {
        return dataSize;
    }

    /**
     * Metoda zwracająca nazwy kolumn.
     *
     * @param columnIndex int - numer kolumny
     * @return String - nazwa kolumny
     */
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    /**
     * Metoda zwraca typ pola danej kolumny.
     *
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
        if (column > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Określa po ilu kliknięciach następuje edycja komórki
     *
     * @param evt EventObject
     * @return boolean
     */
    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            int clickCount = 1;

            return ((MouseEvent) evt).getClickCount() >= clickCount;
        }
        return true;
    }

    /**
     * Metoda zwracająca wartość z danej komórki.
     *
     * @param rowIndex    int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object returnValue = null;

        if (columnIndex == 0) {
            return "t" + dataMatrix.get(rowIndex).get(columnIndex).toString();
        }
        return dataMatrix.get(rowIndex).get(columnIndex).toString();
    }

    /**
     * Ustawia nową wartość we wskazanej komórce tabeli stanów.
     *
     * @param value Object - nowa wartość, tokeny
     * @param row   int - nr wiersza
     * @param col   int - nr kolumny
     */
    public void setValueAt(Object value, int row, int col) {
        double newValue = 0;
        try {
            newValue = Double.parseDouble(value.toString());
            if (newValue < 0)
                newValue = 0;
            ArrayList<String> rowVector = dataMatrix.get(row);
            rowVector.set(col, "" + newValue);

            boss.changeSecondState(row, col, newValue);
        } catch (Exception e) {

        }
    }
}
