package holmes.tables.managers;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.windows.managers.HolmesStatesManager;

import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.EventObject;

/**
 * Model tabeli stanów początkowych sieci (klasyczny).
 */
public class StatesPlacesTableModelXTPN extends AbstractTableModel {
    @Serial
    private static final long serialVersionUID = -318971819221435499L;
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private String[] columnNames;
    private ArrayList<ArrayList<String>> dataMatrix;
    private int dataSize;
    private final HolmesStatesManager boss;

    public boolean changes = false;

    /**
     * Konstruktor klasy modelującej tablicę stanów początkowych.
     */
    public StatesPlacesTableModelXTPN(int placesNumber, HolmesStatesManager boss) {
        this.boss = boss;
        clearModel(placesNumber);
    }

    /**
     * Czyści model tablicy.
     * @param placesNumber int - liczba miejsc sieci
     */
    public void clearModel(int placesNumber) {
        columnNames = new String[placesNumber+2];
        columnNames[0] = "Selected";
        columnNames[1] = "mID";
        for(int i=0; i<placesNumber; i++) {
            columnNames[i+2] = "p"+i;
        }

        dataMatrix = new ArrayList<ArrayList<String>>();
        dataSize = 0;
    }

    /**
     * Metoda służaca do dodawania nowego wiersza (danych tranzycji) do tabeli danych.
     * @param dataRow ArrayList[String] - wiersz danych
     */
    public void addNew(ArrayList<String> dataRow) {
        dataMatrix.add(dataRow);
        dataSize++;
    }

    /**
     * Metoda ustawia flagę wyboru odpowiedniego stanu.
     * @param row int - nr wiersza
     */
    public void setSelected(int row) {
        for(int i=0; i<dataSize; i++) {
            if(i == row)
                dataMatrix.get(i).set(0, "X");
            else
                dataMatrix.get(i).set(0, "");
        }
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
    @SuppressWarnings("unused")
    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            int clickCount = 1;

            // For single-click activation
            //clickCount = 1;
            // For double-click activation
            //clickCount = 2;
            // For triple-click activation
            //clickCount = 3;

            return ((MouseEvent)evt).getClickCount() >= clickCount;
        }
        return true;
    }

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object returnValue;
        if(columnIndex < 2) {
            return dataMatrix.get(rowIndex).get(columnIndex);
        } else {
            try {
                returnValue = dataMatrix.get(rowIndex).get(columnIndex);
                return returnValue.toString();
                //return Double.parseDouble(strVal);
            } catch (Exception e) {
                return -1;
            }
        }

    }

    /**
     * Ustawia nową wartość we wskazanej komórce tabeli stanów.
     * @param value Object - nowa wartość, tokeny
     * @param row int - nr wiersza
     * @param col int - nr kolumny
     *
     *     NIEUŻYWANA, WSZYSTKIE KOLUMNY READ ONLY!
     */
    public void setValueAt(Object value, int row, int col) {
        double newValue;
        try {
            newValue = Double.parseDouble(value.toString());
            if(newValue < 0)
                newValue = 0;
            ArrayList<String> rowVector = dataMatrix.get(row);
            rowVector.set(col, ""+(int)newValue);

            //boss.changeStateXTPN(row, col, newValue);
        } catch (Exception ex) {
            overlord.log(lang.getText("LOGentry00427exception")+" "+ex.getMessage(), "error", true);
        }
    }

    public void setQuietlyValueAt(Object value, int row, int col) {
        double newValue;
        try {
            newValue = Double.parseDouble(value.toString());
            if(newValue < 0)
                newValue = 0;
            ArrayList<String> rowVector = dataMatrix.get(row);
            rowVector.set(col, ""+(int)newValue);
        } catch (Exception ex) {
            overlord.log(lang.getText("LOGentry00428exception")+" "+ex.getMessage(), "error", true);
        }
    }
}