package holmes.tables;

import java.util.ArrayList;

import javax.swing.table.DefaultTableModel;

import holmes.petrinet.elements.Arc.TypeOfArc;

/**
 * Klasa modelująca tablicę edycji funkcji dla tranzycji.
 * 
 * @author MR
 */
public class FunctionalTransTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 7486251580290447103L;

	/**
	 * Klasa pojemnik
	 */
	public class FContainer {
    	public String pID;
    	public String name;
    	public String function;
    	public boolean correct;
    	public TypeOfArc arcType;
    	public int weight;
    	public boolean enabled;
    }
	
	private String[] columnNames = {"Place", "Place name", "Function", "OK", "Arc type", "Weight", "Enabled"};
	private ArrayList<FContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej dla tablicy funkcji.
	 */
	public FunctionalTransTableModel() {
		dataMatrix = new ArrayList<FContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy funkcji.
	 * @param pID String - ID miejsca
	 * @param name String - nazwa miejsca
	 * @param function String - funkcja
	 * @param isOK boolean - czy f jest prawidłowa
	 * @param arcType TypesOfArcs - typ łuku
	 * @param weight int - waga łuku
	 * @param enable boolean - czy f jest aktywna
	 */
	public void addNew(String pID, String name, String function, boolean isOK, TypeOfArc arcType, int weight, boolean enable) {
		FContainer fC = new FContainer();
		fC.pID = pID;
		fC.name = name;
		fC.function = function;
		fC.correct = isOK;
		fC.arcType = arcType;
		fC.weight = weight;
		fC.enabled = enable;
		dataMatrix.add(fC);
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
     * Metoda zwracająca wartość z danej komórki.
     * @param rowIndex int - numer wiersza
     * @param columnIndex int - numer kolumny
     */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object returnValue = null;

		switch (columnIndex) {
        case 0:
            returnValue = dataMatrix.get(rowIndex).pID;
            break;
        case 1:
        	returnValue = dataMatrix.get(rowIndex).name;
            break;
        case 2:
        	returnValue = dataMatrix.get(rowIndex).function;
            break;
        case 3:
        	returnValue = dataMatrix.get(rowIndex).correct;
            break;
        case 4:
        	returnValue = dataMatrix.get(rowIndex).arcType;
            break;
        case 5:
        	returnValue = dataMatrix.get(rowIndex).weight;
            break;
        case 6:
        	returnValue = dataMatrix.get(rowIndex).enabled;
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }
         
        return returnValue;
	}
	
	/**
	 * Metoda ustawia nową wartość w tabeli.
	 * @param value Object - nowa wartość
	 * @param row int - nr wiersza
	 * @param col int - nr kolumny
	 */
	public void setValueAt(Object value, int row, int col) {
		try {
			switch(col) {
				case 2:
					String f = value.toString();
					dataMatrix.get(row).function = f;
					break;
				case 3:
					boolean correct = (boolean)value;
					dataMatrix.get(row).correct = correct;
					break;
				case 4:
					TypeOfArc toa = (TypeOfArc)value;
					dataMatrix.get(row).arcType = toa;
					break;
				case 5:
					int weight = (int)value;
					dataMatrix.get(row).weight = weight;
					break;
				case 6:
					boolean enable = (boolean)value;
					dataMatrix.get(row).enabled = enable;
					break;
			}
		} catch (Exception e) {
			@SuppressWarnings("unused")
			int xxx=1;
		}
	}
}
