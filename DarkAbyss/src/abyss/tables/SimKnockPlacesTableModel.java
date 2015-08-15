package abyss.tables;

import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import abyss.petrinet.data.NetSimulationData;
import abyss.petrinet.elements.Place;
import abyss.utilities.Tools;

/**
 * Model tabeli danych statystycznych dla miejsca (symulacja knockout).
 * 
 * @author MR
 *
 */
public class SimKnockPlacesTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 7417629500439797992L;
	private static final DecimalFormat formatter = new DecimalFormat( "#.###" );
	
	public class PlaceContainer {
    	public int ID;
    	public String name;
    	public double tokenAvg;
    	public double tokenMin;
    	public double tokenMax;
    	public String noTokens;
    	public double stdDev;
    	
    	public double s1;
    	public double s2;
    	public double s3;
    	public double s4;
    	public double s5;
    }
	
	private String[] columnNames = {"ID", "Place name", "AvgT", "MinT", "MaxT", "noT", "stdDev", "S1 %", "S2 %", "S3 %", "S4 %", "S5 %"};
	private ArrayList<PlaceContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej tablicę miejsc.
	 */
	public SimKnockPlacesTableModel() {
		dataMatrix = new ArrayList<PlaceContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy miejsc.
	 * @param data NetSimulationData - obiekt danych statystycznych
	 * @param index int - index miejsca
	 * @param p Place - miejsce
	 */
	public void addNew(NetSimulationData data, int index, Place p) {
		PlaceContainer pc = new PlaceContainer();
		pc.ID = index;
		
		if(p != null)
			pc.name = p.getName();
		else
			pc.name = "Place "+index;
		
		if(data.placeTokensAvg.get(index) == 0)
			pc.name = "<KNOCKOUT>" + pc.name;
				
		
		pc.tokenAvg = data.placeTokensAvg.get(index);
		pc.tokenMin = data.placeTokensMin.get(index);
		pc.tokenMax = data.placeTokensMax.get(index);
		pc.noTokens = "" + data.placeZeroTokens.get(index); // +"/"+data.reps;
		
		pc.stdDev = data.placeStdDev.get(index);
		pc.s1 = data.placeWithinStdDev.get(index).get(0) * 100 / data.reps;
		pc.s2 = data.placeWithinStdDev.get(index).get(1) * 100 / data.reps;
		pc.s3 = data.placeWithinStdDev.get(index).get(2) * 100 / data.reps;
		pc.s4 = data.placeWithinStdDev.get(index).get(3) * 100 / data.reps;
		pc.s5 = data.placeWithinStdDev.get(index).get(4) * 100 / data.reps;
		
		dataMatrix.add(pc);
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
        	returnValue = dataMatrix.get(rowIndex).tokenAvg;
        	try{
        		//Double value = Double.parseDouble(returnValue.toString());
        		//returnValue = value;
        	} catch (Exception e) { }
            break;
        case 3:
        	returnValue = dataMatrix.get(rowIndex).tokenMin;
            break;
        case 4:
        	returnValue = dataMatrix.get(rowIndex).tokenMax;
            break;
        case 5:
        	returnValue = dataMatrix.get(rowIndex).noTokens;
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
