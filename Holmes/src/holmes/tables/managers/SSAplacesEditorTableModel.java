package holmes.tables.managers;

import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.SSAplacesVector.SSAdataType;
import holmes.windows.managers.HolmesSSAplacesEditor;

/**
 * Model tablicy do edycji wartości komponentów w miejscach dla symulatora SSA.
 */
public class SSAplacesEditorTableModel extends DefaultTableModel {
	@Serial
	private static final long serialVersionUID = 5334544477964813872L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private String[] columnNames;
	private ArrayList<SSAdataClass> dataMatrix;
	private int dataSize;
	private HolmesSSAplacesEditor boss;
	private int ssaVectorIndex;
	public boolean changes = false;
	
	private SSAdataType dataType;
	
	public static class SSAdataClass {
		public int ID;
		public String name;
		public Double ssaValue;
	}

	/**
	 * Konstruktor klasy modelującej tablicę wektora miejsc SSA.
	 * @param boss HolmesSSAplacesEditor - obiekt okna tablicy
	 * @param ssaVectorIndex int - nr wybranego wektora
	 * @param dataType SSAdataType - rodzaj danych
	 */
	public SSAplacesEditorTableModel(HolmesSSAplacesEditor boss, int ssaVectorIndex, SSAdataType dataType) {
		this.boss = boss;
		this.ssaVectorIndex = ssaVectorIndex;
		this.dataType = dataType;
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
		
		dataMatrix = new ArrayList<SSAdataClass>();
		dataSize = 0;
		
	}
	
	public void addNew(int ID, String name, double value) {
		SSAdataClass row = new SSAdataClass();
		row.ID = ID;
		row.name = name;
		row.ssaValue = value;
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
     * Określa po ilu kliknięciach następuje edycja komórki
     * @param evt EventObject
     * @return boolean
     */
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
		switch(columnIndex) {
			case 0:
				return dataMatrix.get(rowIndex).ID;
			case 1:
				return dataMatrix.get(rowIndex).name;
			case 2:
				return dataMatrix.get(rowIndex).ssaValue;
		}
		return null;
	}
	
	/**
	 * Metoda pozwala zmienić edytowalne komórki tabeli.
	 * @param value Object - nowa wartość
	 * @param row int - nr wiersza (tranzycja)
	 * @param col int - nr kolumny
	 */
	public void setValueAt(Object value, int row, int col) {
		double newValue = 0;
		try {
			if(col == 2) {
				newValue = Double.parseDouble(value.toString());
				if(dataType == SSAdataType.MOLECULES) {
					if(newValue > 0.9) {
						dataMatrix.get(row).ssaValue = newValue;
						boss.changeRealValue(ssaVectorIndex, row, newValue);
					} else {
						String strB = "err.";
						try {
							strB = String.format(lang.getText("SSAPETM_entry001"), newValue);
						} catch (Exception e) {
							overlord.log(lang.getText("LOGentryLNGexc")+" "+"SSAPETM_entry001", "error", true);
						}
						JOptionPane.showMessageDialog(boss,
								strB, lang.getText("SSAPETM_entry001t"),JOptionPane.WARNING_MESSAGE);
					}
				} else {
					if(newValue < 10) {
						dataMatrix.get(row).ssaValue = newValue;
						boss.changeRealValue(ssaVectorIndex, row, newValue);
					} else {
						JOptionPane.showMessageDialog(boss, 
								lang.getText("SSAPETM_entry002"), lang.getText("SSAPETM_entry002t"),JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00422exception")+"\n"+e.getMessage(), "error", true);
		}
	}
}
