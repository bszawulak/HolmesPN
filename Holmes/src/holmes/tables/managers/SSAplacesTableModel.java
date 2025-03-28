package holmes.tables.managers;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import holmes.petrinet.data.SSAplacesVector.SSAdataType;

/**
 * Model tabeli do wyświetlania listy wektorów stanów miejsc w SSA.
 */
public class SSAplacesTableModel extends DefaultTableModel {
	@Serial
	private static final long serialVersionUID = -7214250232934584183L;
	private String[] columnNames;
	private ArrayList<SSAtableContainer> dataMatrix;
	private int dataSize;
	
	public static class SSAtableContainer {
		public String selected;
		public int ID;
		public String description;
		public SSAdataType type;
		public double volume;
	}
	
	/**
	 * Konstruktor klasy modelującej dla tablicy inwariantów.
	 */
	public SSAplacesTableModel() {
		clearModel();
	}
	
	/**
	 * Czyści model tablicy.
	 */
	public void clearModel() {
		columnNames = new String[5];
		columnNames[0] = "selected";
		columnNames[1] = "ID";
		columnNames[2] = "Vector description";
		columnNames[3] = "Data type";
		columnNames[4] = "Volume";
		
		dataMatrix = new ArrayList<SSAtableContainer>();
		dataSize = 0;
	}
	
	public void changeType(int row, SSAdataType type) {
		dataMatrix.get(row).type = type;
	}
	
	/**
	 * Metoda służaca do dodawania nowego wiersza do tabeli danych.
	 * @param selected String - X lub brak
	 * @param id int - identyfikator wektora
	 * @param description String - opis danych
	 * @param type SSAdataType - typ danych
	 * @param volume double - objętość symulowanego systemu
	 */
	public void addNew(String selected, int id, String description, SSAdataType type, double volume) {
		SSAtableContainer data = new SSAtableContainer();
		data.selected = selected;
		data.ID = id;
		data.description = description;
		data.type = type;
		data.volume = volume;
		dataMatrix.add(data);
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
     * Zwraca status edytowalności komórek.
     */
	@Override
    public boolean isCellEditable(int row, int column) {
    	return false;
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
		return switch (columnIndex) {
			case 0, 2, 3 -> String.class;
			case 1 -> Integer.class;
			case 4 -> Double.class;
			default -> Object.class;
		};

	}
    
    /**
	 * Metoda ustawia flagę wyboru odpowiedniego wektora.
	 * @param row int - nr wiersza
	 */
	public void setSelected(int row) {
		for(int i=0; i<dataSize; i++) {
			if(i == row)
				dataMatrix.get(i).selected = "X";
			else
				dataMatrix.get(i).selected = "";
		}
	}

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			return switch (columnIndex) {
				case 0 -> dataMatrix.get(rowIndex).selected;
				case 1 -> dataMatrix.get(rowIndex).ID;
				case 2 -> dataMatrix.get(rowIndex).description;
				case 3 -> dataMatrix.get(rowIndex).type;
				case 4 -> dataMatrix.get(rowIndex).volume;
				default -> -1;
			};
		} catch (Exception e) {
			return -1;
		}
	}
}
