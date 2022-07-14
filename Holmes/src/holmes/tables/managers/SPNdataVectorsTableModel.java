package holmes.tables.managers;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import holmes.petrinet.data.SPNdataVector.SPNvectorSuperType;
import holmes.windows.managers.HolmesSPNmanager;

/**
 * Klasa tablicy wszystkich wektorów tranzycji SPN.
 * 
 * @author MR
 */
public class SPNdataVectorsTableModel extends DefaultTableModel {
	@Serial
	private static final long serialVersionUID = 3869824360655880298L;
	private String[] columnNames;
	private ArrayList<SPNvectorTableClass> dataMatrix;
	private int dataSize;
	public boolean changes = false;
	private HolmesSPNmanager boss;
	
	public class SPNvectorTableClass {
		public String selected;
		public int ID;
		public String frName;
		public SPNvectorSuperType superType;
	}

	/**
	 * Konstruktor obiektu tablicy SPNdataVectorsTableModel.
	 * @param boss HolmesSPNmanager - główne okno managera
	 */
	public SPNdataVectorsTableModel(HolmesSPNmanager boss) {
		this.boss = boss;
		clearModel();
	}
	
	/**
	 * Czyści model tablicy.
	 */
	public void clearModel() {
		columnNames = new String[4];
		columnNames[0] = "Selected";
		columnNames[1] = "ID";
		columnNames[2] = "Description";
		columnNames[3] = "SuperType";
		
		dataMatrix = new ArrayList<SPNvectorTableClass>();
		dataSize = 0;
	}
	
	/**
	 * Dodawanie nowego wiersza do tablicy wektorów SPN.
	 * @param selected String - czy wybrany wektor
	 * @param ID int - indeks wektora
	 * @param name String - opis
	 * @param superType SPNvectorSuperType - typ wektora danych SPN
	 */
	public void addNew(String selected, int ID, String name, SPNvectorSuperType superType) {
		SPNvectorTableClass row = new SPNvectorTableClass();
		row.selected = selected;
		row.ID = ID;
		row.frName = name;
		row.superType = superType;
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
		return ( column == 2 );
    }

    /**
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	public Object getValueAt(int rowIndex, int columnIndex) {
		return switch (columnIndex) {
			case 0 -> dataMatrix.get(rowIndex).selected;
			case 1 -> dataMatrix.get(rowIndex).ID;
			case 2 -> dataMatrix.get(rowIndex).frName;
			case 3 -> dataMatrix.get(rowIndex).superType;
			default -> null;
		};
	}
	
	public void setValueAt(Object value, int row, int col) {
		try {
			if(col == 2) {
				dataMatrix.get(row).frName = value.toString();
				boss.changeState(row, col, value.toString());
			}
		} catch (Exception e) {
			//dataMatrix.get(row).firingRate = 1.0;
		}
	}
	
	/**
	 * Metoda ustawia flagę wyboru odpowiedniego stanu fr.
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
}
