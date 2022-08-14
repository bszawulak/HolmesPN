package holmes.tables.simKnock;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

/**
 * Model tabeli danych statystycznych dla miejsc
 */
public class SimKnockPlacesCompAllTableModel extends AbstractTableModel {
	@Serial
	private static final long serialVersionUID = 2403086900970134182L;
	private String[] columnNames;
	private ArrayList<ArrayList<String>> dataMatrix;
	private int dataSize;
	public ArrayList<ArrayList<DetailsPlace>> pTableData;
	
	public static class DetailsPlace {
		public DetailsPlace() {}
		
		public double refAvgTokens;
		public double knockAvgTokens;
		public int knockDisabled;
		public boolean significance1;
		public boolean significance2;
		public double diff;
	}
	
	/**
	 * Konstruktor klasy modelującej tablicę tranzycji.
	 */
	public SimKnockPlacesCompAllTableModel(int placesNumber) {
		columnNames = new String[placesNumber+3];
		columnNames[0] = "ID";
		columnNames[1] = "Offline transition name:";
		columnNames[2] = "Knocked:";
		for(int i=0; i<placesNumber; i++) {
			columnNames[i+3] = "t"+i;
		}
		
		dataMatrix = new ArrayList<ArrayList<String>>();
		dataSize = 0;
	}
	
	public DetailsPlace newDetailsInstance() {
		return new DetailsPlace();
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
		Object returnValue;
		if(columnIndex == 1) {
			return dataMatrix.get(rowIndex).get(columnIndex);
		}
		
		if(columnIndex < 3) {
			try {
				returnValue = dataMatrix.get(rowIndex).get(columnIndex);
				String strVal = returnValue.toString();
				return Integer.parseInt(strVal);
				//return returnValue;
			} catch (Exception e) {
				return "error";
			}
		} else {
			try {
				returnValue = dataMatrix.get(rowIndex).get(columnIndex);
				String strVal = returnValue.toString().replace(",", ".");
				return Double.parseDouble(strVal);
			} catch (Exception e) {
				return "error";
			}
		}
	}
}
