package holmes.tables.simKnock;

import java.io.Serial;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.elements.Transition;

/**
 * Model tabeli danych statystycznych dla tranzycji (symulacja knockout) - porównanie.
 */
public class SimKnockTransCompTableModel extends AbstractTableModel {
	@Serial
	private static final long serialVersionUID = -2454782753413053173L;
	public int tableType = 0;
	
	public static class TransCompContainer {
    	public int ID;
    	public String name;
    	
    	public double firingAvgRef;
    	public double stdDevRef;
    	public double firingAvgKnock;
    	public double stdDevKnock;
    	
    	public Double firingAvgPercDiff;
    	public String noFiringKnock;
    	public String signifLvl1; //min<>max
    	public String signifLvl2; //stdDev
    }
	
	private String[] columnNames = {"ID", "Transition name", "AvgFRef", "stdDevRef", "AvgFKnock", "stdDevKnock", "diffPerc", "noF", "sig1", "sig2"};
	private ArrayList<TransCompContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej tablicę tranzycji.
	 */
	public SimKnockTransCompTableModel() {
		dataMatrix = new ArrayList<TransCompContainer>();
		dataSize = 0;
	}
	
	public ArrayList<TransCompContainer> accessDataMatrix() {
		return dataMatrix;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy tranzycji.
	 * @param dataRef NetSimulationData - obiekt danych referencyjnych
	 * @param dataKnock NetSimulationData - obiekt danych symulacji knockout
	 * @param index int - index tranzycji
	 * @param t Transition - tranzycja
	 */
	public void addNew(NetSimulationData dataRef, NetSimulationData dataKnock, int index, Transition t) {
		TransCompContainer tc = new TransCompContainer();
		tc.ID = index;
		
		if(t != null)
			tc.name = t.getName();
		else
			tc.name = "Transition "+index;
		
		tc.firingAvgRef = dataRef.transFiringsAvg.get(index);
		tc.stdDevRef = dataRef.transStdDev.get(index);
		tc.firingAvgKnock = dataKnock.transFiringsAvg.get(index);
		tc.stdDevKnock = dataKnock.transStdDev.get(index);
		
		if(dataKnock.disabledTotals.contains(index)) {
			tc.name = " <OFFLINE> " + tc.name;
		} else if(dataKnock.transFiringsAvg.get(index) == 0) {
			tc.name = " <KNOCKOUT> " + tc.name;
		}
		
		tc.firingAvgPercDiff = 0.0;
		double diff = tc.firingAvgRef - tc.firingAvgKnock;
		if(tc.firingAvgRef != 0) {
			diff = (diff / tc.firingAvgRef)*100;
			if(diff < 0) { //zwiększyło (sic!) się po symulacji
				diff *= -1;
				tc.firingAvgPercDiff = diff;
				
				tc.firingAvgPercDiff = (tc.firingAvgKnock - tc.firingAvgRef)*100;
			} else {
				diff *= -1;
				tc.firingAvgPercDiff = diff;
				
				tc.firingAvgPercDiff = (tc.firingAvgKnock - tc.firingAvgRef)*100;
			}
			
			if(tc.firingAvgKnock == 0)
				tc.firingAvgPercDiff = -999999.0; // -inf
		} else {
			if(tc.firingAvgKnock == 0)
				tc.firingAvgPercDiff = 999991.0; //" --- ";
			else
				tc.firingAvgPercDiff = 999999.0; // +inf
		}
    	tc.noFiringKnock = ""+dataKnock.transZeroFiring.get(index);//+"/"+dataKnock.reps;
    	
    	tc.firingAvgKnock *= 100;
    	tc.firingAvgRef *= 100;
    	
    	
    	//significance level 1 - min <> max ref/knock
    	if(tc.firingAvgRef > tc.firingAvgKnock) {
    		if(dataRef.transFiringsMin.get(index) > dataKnock.transFiringsMax.get(index)) {
    			tc.signifLvl1 = "OK";
    		} else {
    			tc.signifLvl1 = "no";
    		}
    	} else {
    		if(dataRef.transFiringsMax.get(index) < dataKnock.transFiringsMin.get(index)) {
    			tc.signifLvl1 = "OK";
    		} else {
    			tc.signifLvl1 = "no";
    		}
    	}

    	//significance level 2 - avg+-stdDev <> avg+-stdDev ref/knock
    	if(tc.firingAvgRef > tc.firingAvgKnock) {
    		if(tc.firingAvgRef - tc.stdDevRef > tc.firingAvgKnock + tc.stdDevKnock) {
    			tc.signifLvl2 = "OK";
    		} else {
    			tc.signifLvl2 = "no";
    		}
    	} else { //ref mniejsze niż knock
    		if(tc.firingAvgRef + tc.stdDevRef < tc.firingAvgKnock - tc.stdDevKnock) {
    			tc.signifLvl2 = "OK";
    		} else {
    			tc.signifLvl2 = "no";
    		}
    	}
		
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
			case 2 -> dataMatrix.get(rowIndex).firingAvgRef;
			case 3 -> dataMatrix.get(rowIndex).stdDevRef;
			case 4 -> dataMatrix.get(rowIndex).firingAvgKnock;
			case 5 -> dataMatrix.get(rowIndex).stdDevKnock;
			case 6 -> dataMatrix.get(rowIndex).firingAvgPercDiff;
			case 7 -> dataMatrix.get(rowIndex).noFiringKnock;
			case 8 -> dataMatrix.get(rowIndex).signifLvl1;
			case 9 -> dataMatrix.get(rowIndex).signifLvl2;
			default -> throw new IllegalArgumentException("Invalid column index");
		};
	}
}
