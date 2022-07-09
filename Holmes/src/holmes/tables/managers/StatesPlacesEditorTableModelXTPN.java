package holmes.tables.managers;

import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.EventObject;
import javax.swing.table.DefaultTableModel;
import holmes.windows.managers.HolmesStatesEditorXTPN;

public class StatesPlacesEditorTableModelXTPN extends DefaultTableModel {
    @Serial
    private static final long serialVersionUID = -7381011195523910278L;
    private String[] columnNames;
    private ArrayList<PlaceEditContainer> dataMatrix;
    private int dataSize;
    private HolmesStatesEditorXTPN boss;
    private int stateVectorIndex;
    public boolean changes = false;

    public static class PlaceEditContainer {
        public int ID;
        public String name;
        public String tokens;
    }

    /**
     * Konstruktor klasy modelującej tablicę miejsc i ich stanu.
     * @param boss HolmesStatesEditor - obiekt okna tablicy
     * @param stateVectorIndex int - nr wybranego wektora
     */
    public StatesPlacesEditorTableModelXTPN(HolmesStatesEditorXTPN boss, int stateVectorIndex) {
        this.boss = boss;
        this.stateVectorIndex = stateVectorIndex;
        clearModel();
    }

    /**
     * Czyści model tablicy.
     */
    public void clearModel() {
        columnNames = new String[3];
        columnNames[0] = "ID";
        columnNames[1] = "Place Name";
        columnNames[2] = "Value";

        dataMatrix = new ArrayList<PlaceEditContainer>();
        dataSize = 0;

    }

    /**
     * Dodaje nowy wektor danych do tablicy.
     * @param ID int - indeks miejsca
     * @param name String - nazwa miejsca
     * @param value double - liczba tokenów
     */
    public void addNew(int ID, String name, String value) {
        PlaceEditContainer row = new PlaceEditContainer();
        row.ID = ID;
        row.name = name;
        row.tokens = value;
        dataMatrix.add(row);
        dataSize++;
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
            return ((MouseEvent)evt).getClickCount() >= clickCount;
        }
        return true;
    }

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        return switch (columnIndex) {
            case 0 -> dataMatrix.get(rowIndex).ID;
            case 1 -> dataMatrix.get(rowIndex).name;
            case 2 -> dataMatrix.get(rowIndex).tokens;
            default -> null;
        };
    }

    /**
     * Metoda pozwala zmienić edytowalne komórki tabeli.
     * @param value Object - nowa wartość
     * @param row int - nr wiersza (tranzycja)
     * @param col int - nr kolumny
     */
    public void setValueAt(Object value, int row, int col) {
        /*
        double newValue;
        try {
            if(col == 2) {
                newValue = Double.parseDouble(value.toString());
                dataMatrix.get(row).tokens = newValue;
                boss.changeRealValue(stateVectorIndex, row, newValue);
            }
        } catch (Exception e) {
            //dataMatrix.get(row).firingRate = 1.0;
        }
        */
    }

    public void setQuietlyValueAt(Object value, int row, int col) {
        /*
        double newValue;
        try {
            if(col == 2) {
                newValue = Double.parseDouble(value.toString());
                dataMatrix.get(row).tokens = newValue;
            }
        } catch (Exception ignored) {
        }
        */
    }
}
