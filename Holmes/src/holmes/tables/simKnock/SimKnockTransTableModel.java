package holmes.tables.simKnock;

import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.elements.Transition;

/**
 * Model tabeli danych statystycznych dla tranzycji (symulacja knockout).
 * 
 * @author MR
 *
 */
public class SimKnockTransTableModel extends AbstractTableModel {
	@Serial
	private static final long serialVersionUID = -5809682023062187908L;
	private static final DecimalFormat formatter3 = new DecimalFormat( "#.#####" );
	
	public class TransContainer {
    	public int ID;
    	public String name;
    	public double firingAvg;
    	public double firingMin;
    	public double firingMax;
    	public String noFiring;
    	public double stdDev;
    	
    	public double s1;
    	public double s2;
    	public double s3;
    	public double s4;
    	public double s5;
    }
	
	private String[] columnNames = {"ID", "Transition name", "AvgF", "MinF", "MaxF", "noF", "stdDev", "S1 %", "S2 %", "S3 %", "S4 %", "S5 %"};
	private ArrayList<TransContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej tablicę tranzycji.
	 */
	public SimKnockTransTableModel() {
		dataMatrix = new ArrayList<TransContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy tranzycji.
	 * @param data NetSimulationData - obiekt danych statystycznych
	 * @param index int - index tranzycji
	 * @param t Transition - tranzycja
	 */
	@SuppressWarnings("IntegerDivisionInFloatingPointContext")
	public void addNew(NetSimulationData data, int index, Transition t) {
		TransContainer tc = new TransContainer();
		tc.ID = index;
		
		if(t != null)
			tc.name = t.getName();
		else
			tc.name = "Transition "+index;
		
		if(data.disabledTotals.contains(index)) {
			tc.name = " <OFFLINE> " + tc.name;
		} else if(data.transFiringsAvg.get(index) == 0) {
			tc.name = " <KNOCKOUT> " + tc.name;
		}

		tc.firingAvg = data.transFiringsAvg.get(index);
		tc.firingMin = data.transFiringsMin.get(index);
		tc.firingMax = data.transFiringsMax.get(index);
		tc.noFiring = "" + data.transZeroFiring.get(index); // +"/"+data.reps;
		
		tc.stdDev = data.transStdDev.get(index);
		tc.s1 = data.transWithinStdDev.get(index).get(0) * 100 / data.reps;
		tc.s2 = data.transWithinStdDev.get(index).get(1) * 100 / data.reps;
		tc.s3 = data.transWithinStdDev.get(index).get(2) * 100 / data.reps;
		tc.s4 = data.transWithinStdDev.get(index).get(3) * 100 / data.reps;
		tc.s5 = data.transWithinStdDev.get(index).get(4) * 100 / data.reps;
		
		dataMatrix.add(tc);
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
		return switch (columnIndex) {
			case 0 -> dataMatrix.get(rowIndex).ID;
			case 1 -> dataMatrix.get(rowIndex).name;
			case 2 -> formatter3.format((Number) dataMatrix.get(rowIndex).firingAvg);
			//returnValue = dataMatrix.get(rowIndex).firingAvg;returnValue = formatter3.format((Number)dataMatrix.get(rowIndex).firingAvg);
			case 3 -> formatter3.format((Number) dataMatrix.get(rowIndex).firingMin);
			//returnValue = dataMatrix.get(rowIndex).firingMin;
			case 4 -> formatter3.format((Number) dataMatrix.get(rowIndex).firingMax);
			//returnValue = dataMatrix.get(rowIndex).firingMax;
			case 5 -> dataMatrix.get(rowIndex).noFiring;
			case 6 -> dataMatrix.get(rowIndex).stdDev;
			case 7 -> dataMatrix.get(rowIndex).s1;
			case 8 -> dataMatrix.get(rowIndex).s2;
			case 9 -> dataMatrix.get(rowIndex).s3;
			case 10 -> dataMatrix.get(rowIndex).s4;
			case 11 -> dataMatrix.get(rowIndex).s5;
			default -> throw new IllegalArgumentException("Invalid column index");
		};
	}
}
