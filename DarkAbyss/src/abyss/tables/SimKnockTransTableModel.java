package abyss.tables;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import abyss.petrinet.data.NetSimulationData;
import abyss.petrinet.elements.Transition;

/**
 * Model tabeli danych statystycznych dla tranzycji (symulacja knockout).
 * 
 * @author MR
 *
 */
public class SimKnockTransTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -5809682023062187908L;

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
	public void addNew(NetSimulationData data, int index, Transition t) {
		TransContainer tc = new TransContainer();
		tc.ID = index;
		tc.name = t.getName();
		tc.firingAvg = data.transFiringsAvg.get(index);
		tc.firingMin = data.transFiringsMin.get(index);
		tc.firingMax = data.transFiringsMax.get(index);
		tc.noFiring = "" + data.transZeroFiring.get(index) +"/"+data.reps;
		
		tc.stdDev = data.placeStdDev.get(index);
		tc.s1 = (data.transWithinStdDev.get(index).get(0) / data.reps)*100;
		tc.s2 = (data.transWithinStdDev.get(index).get(1) / data.reps)*100;
		tc.s3 = (data.transWithinStdDev.get(index).get(2) / data.reps)*100;
		tc.s4 = (data.transWithinStdDev.get(index).get(3) / data.reps)*100;
		tc.s5 = (data.transWithinStdDev.get(index).get(4) / data.reps)*100;
		
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
		Object returnValue = null;
		switch (columnIndex) {
        case 0:
            returnValue = dataMatrix.get(rowIndex).ID;
            break;
        case 1:
        	returnValue = dataMatrix.get(rowIndex).name;
            break;
        case 2:
        	returnValue = dataMatrix.get(rowIndex).firingAvg;
            break;
        case 3:
        	returnValue = dataMatrix.get(rowIndex).firingMin;
            break;
        case 4:
        	returnValue = dataMatrix.get(rowIndex).firingMax;
            break;
        case 5:
        	returnValue = dataMatrix.get(rowIndex).noFiring;
            break;
        case 6:
        	returnValue = dataMatrix.get(rowIndex).stdDev;
            break;
        case 7:
        	returnValue = dataMatrix.get(rowIndex).s1;
            break;
        case 8:
        	returnValue = dataMatrix.get(rowIndex).s2;
            break;
        case 9:
        	returnValue = dataMatrix.get(rowIndex).s3;
            break;
        case 10:
        	returnValue = dataMatrix.get(rowIndex).s4;
            break;
        case 11:
        	returnValue = dataMatrix.get(rowIndex).s5;
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }
         
        return returnValue;
	}
}
