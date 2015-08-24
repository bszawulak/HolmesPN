package holmes.tables;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.elements.Place;

/**
 * Model tabeli danych statystycznych dla miejsca (symulacja knockout).
 * 
 * @author MR
 *
 */
public class SimKnockPlacesCompTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5141687750658780227L;
	
	public class PlaceCompContainer {
    	public int ID;
    	public String name;
    	
    	public double tokenAvgRef;
    	public double stdDevRef;
    	public double tokenAvgKnock;
    	public double stdDevKnock;
    	
    	public Double tokenAvgPercDiff;
    	public String noTokensKnock;
    	public String signifLvl1; //czy zawiera się w sigma
    	public String signifLvl2; //czy min <> max Ref/Knock
    }
	
	private String[] columnNames = {"ID", "Place name", "AvgTRef", "stdDRef", "AvgTKnock", "stdDKnock", "diffPerc", "noT", "sig1", "sig2"};
	private ArrayList<PlaceCompContainer> dataMatrix;
	private int dataSize;
	
	/**
	 * Konstruktor klasy modelującej tablicę miejsc.
	 */
	public SimKnockPlacesCompTableModel() {
		dataMatrix = new ArrayList<PlaceCompContainer>();
		dataSize = 0;
	}
	
	/**
	 * Metoda dodająca nowy wiersz do modelu tablicy miejsc.
	 * @param dataRef NetSimulationData - obiekt danych referencyjnych
	 * @param dataKnock NetSimulationData - obiekt danych symulacji knockout
	 * @param index int - index miejsca
	 * @param p Place - miejsce
	 */
	public void addNew(NetSimulationData dataRef, NetSimulationData dataKnock, int index, Place p) {
		PlaceCompContainer pc = new PlaceCompContainer();
		pc.ID = index;
		
		if(p != null)
			pc.name = p.getName();
		else
			pc.name = "Place "+index;
		
		pc.tokenAvgRef = dataRef.placeTokensAvg.get(index);
		pc.stdDevRef = dataRef.placeStdDev.get(index);
		pc.tokenAvgKnock = dataKnock.placeTokensAvg.get(index);
		pc.stdDevKnock = dataKnock.placeStdDev.get(index);
		
		if(dataKnock.placeTokensAvg.get(index) == 0)
			pc.name = "<KNOCKOUT>" + pc.name;
		
		//difference %:
		pc.tokenAvgPercDiff = 0.0; 
		double diff = pc.tokenAvgRef - pc.tokenAvgKnock;
		if(pc.tokenAvgRef != 0) {
			diff = (diff / pc.tokenAvgRef)*100;
			if(diff < 0) { //zwiększyło (sic!) się po symulacji
				diff *= -1;
				pc.tokenAvgPercDiff = diff; //"+"+formatter.format(diff) + "%";
			} else {
				diff *= -1;
				pc.tokenAvgPercDiff = diff; //"-"+formatter.format(diff) + "%";
			}
			if(pc.tokenAvgKnock == 0)
				pc.tokenAvgPercDiff =  -999999.0; // -inf
		} else {
			if(pc.tokenAvgKnock == 0)
				pc.tokenAvgPercDiff = 999999.1; // ---
			else
				pc.tokenAvgPercDiff = 999999.0; // +inf
		}
    	pc.noTokensKnock = ""+dataKnock.placeZeroTokens.get(index);//+"/"+dataKnock.reps;
    	
    	//significance level 1 - min <> max ref/knock
    	if(pc.tokenAvgRef > pc.tokenAvgKnock) {
    		if(dataRef.placeTokensMin.get(index) > dataKnock.placeTokensMax.get(index)) {
    			pc.signifLvl1 = "OK";
    		} else {
    			pc.signifLvl1 = "no";
    		}
    	} else {
    		if(dataRef.placeTokensMax.get(index) < dataKnock.placeTokensMin.get(index)) {
    			pc.signifLvl1 = "OK";
    		} else {
    			pc.signifLvl1 = "no";
    		}
    	}

    	//significance level 2 - avg+-stdDev <> avg+-stdDev ref/knock
    	if(pc.tokenAvgRef > pc.tokenAvgKnock) {
    		if(pc.tokenAvgRef - pc.stdDevRef > pc.tokenAvgKnock + pc.stdDevKnock) {
    			pc.signifLvl2 = "OK";
    		} else {
    			pc.signifLvl2 = "no";
    		}
    	} else { //ref mniejsze niż knock
    		if(pc.tokenAvgRef + pc.stdDevRef < pc.tokenAvgKnock - pc.stdDevKnock) {
    			pc.signifLvl2 = "OK";
    		} else {
    			pc.signifLvl2 = "no";
    		}
    	}
    	
		
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
        	returnValue = dataMatrix.get(rowIndex).tokenAvgRef;
            break;
        case 3:
        	returnValue = dataMatrix.get(rowIndex).stdDevRef;
            break;
        case 4:
        	returnValue = dataMatrix.get(rowIndex).tokenAvgKnock;
            break;
        case 5:
        	returnValue = dataMatrix.get(rowIndex).stdDevKnock;
            break;
        case 6:
        	returnValue = dataMatrix.get(rowIndex).tokenAvgPercDiff;
            break;
        case 7:
        	returnValue = dataMatrix.get(rowIndex).noTokensKnock;
            break;
        case 8:
        	returnValue = dataMatrix.get(rowIndex).signifLvl1;
            break;
        case 9:
        	returnValue = dataMatrix.get(rowIndex).signifLvl2;
            break;
        default:
            throw new IllegalArgumentException("Invalid column index");
        }
         
        return returnValue;
	}
}
