package abyss.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import abyss.tables.SimKnockPlacesCompAllTableModel.DetailsPlace;
import abyss.tables.SimKnockTransCompAllTableModel.DetailsTrans;

public class SimKnockTableRenderer implements TableCellRenderer {
	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
	private JTable table;
	private static final DecimalFormat formatter3 = new DecimalFormat( "#.###" );
	private static final DecimalFormat formatter2 = new DecimalFormat( "#.##" );
	private static final DecimalFormat formatter1 = new DecimalFormat( "#.#" );
	private static final Font fontNormal =  new Font("Verdana", Font.PLAIN, 12);
	private static final Font fontBold =  new Font("Verdana", Font.BOLD, 12);
	
	private static final Font fontNormal2 =  new Font("Verdana", Font.PLAIN, 10);
	private static final Font fontBold2 =  new Font("Verdana", Font.BOLD, 10);
	private int mode = 0;
	
	/**
	 * Konstruktor domyślny obiektów klasy MyRenderer.
	 */
	public SimKnockTableRenderer(JTable table) {
		this.table = table;
		//formatter.setMinimumFractionDigits(2);
	}
	
	/**
	 * Tryb pracy: 0 (defult): pojedyncze, 1: porównanie
	 * @param value int - wartość trybu
	 */
	public void setMode(int value) {
		this.mode = value;
	}

	/**
	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli w zależności od ustawionego modelu tabeli.
	 * @param table Jtable - tabela danych
	 * @param value Object - wartość do komórki
	 * @param isSelected boolean - czy zaznaczona komórka
	 * @param hasFocus boolean - czy aktywna komórka
	 * @param row int - numer wiersza
	 * @param columnt int - numer kolumny
	 * @return Component - zależy od kolumny
	 */
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    	if(mode == 1)
    		return paintCellsComp(value, isSelected, hasFocus, row, column);
    	else if(mode == 2)
    		return paintCellsCompAll(value, isSelected, hasFocus, row, column);
    	else
    		return paintCellsDefault(value, isSelected, hasFocus, row, column);
    }
    
    /**
     * Metoda trybu rysowania dla komórek tabeli porównawczej dwóch zbiorów knockout.
     * @param value Object - wartość do wpisania
     * @param isSelected boolean - czy komórka jest wybrana
     * @param hasFocus boolean - czy jest aktywna
     * @param row int - nr wiersza
     * @param column int - nr kolumny
     * @return Component - zależy od kolumny
     */
    private Component paintCellsComp(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    	
    	if(column == 1) {
    		String name = value.toString();
    		if(name.contains("<OFFLINE")) {
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.red);
    			oLabel.setFont(fontNormal);
    			oLabel.setText(name);
    			return oLabel;
    		} else if(name.contains("<KNOCKOUT")) {
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.lightGray);
    			oLabel.setFont(fontNormal);
    			oLabel.setText(name);
    			return oLabel;
    		} 
    	}
    	
    	//kolumna 6: róznice procentowe
		if(column == 6) {
			//JLabel oLabel = new JLabel(); //(JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			oLabel.setForeground(Color.black);
			oLabel.setFont(fontNormal);
			if(value instanceof Double) {
				double val = (double)value;
				if(val == -999999.0) 
					oLabel.setText("-inf");
				else if(val == 999999.0) 
					oLabel.setText("+inf");
				else if(val == 999991.0)
					oLabel.setText(" --- ");
				else {
					if(val > 1000) {
						oLabel.setForeground(Color.red);
						oLabel.setFont(fontBold);
						oLabel.setText(">1000 %");
					} else if(val < -1000) {
						oLabel.setForeground(Color.red);
						oLabel.setFont(fontBold);
						oLabel.setText("<-1000 %");
					} else {
						if(val > 75 || val < -75) {
							oLabel.setForeground(Color.red);
							oLabel.setFont(fontBold);
						} else if(val > 45 || val < -45) {
							oLabel.setForeground(Color.orange);
							oLabel.setFont(fontBold);
						}
						
						oLabel.setText(formatter2.format((double)value) + "%");
					}
				}
			} else {
				oLabel.setText(value+"%");
			}
			return oLabel;
		}

		//significance levels
		if(column == 8 || column == 9) {
			//JLabel oLabel = new JLabel(); //(JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			oLabel.setForeground(Color.black);
			oLabel.setFont(fontNormal);
			if(value instanceof String) {
				String val = value.toString();
				if(val.contains("OK")) {
					oLabel.setForeground(Color.black);
					oLabel.setBackground(Color.green);
					oLabel.setFont(fontBold);
					oLabel.setText("OK");
				} else {
					oLabel.setForeground(Color.black);
					oLabel.setBackground(Color.white);
					oLabel.setFont(fontBold);
					oLabel.setText("n/a");
				}
			} else {
				oLabel.setText(value.toString());
			}
			
			return oLabel;
		}
		
		
		if(value instanceof Double) {
			double val = (double)value;
			if(val < 10)
				value = formatter3.format((Number)value);
			else if(val < 100)
				value = formatter2.format((Number)value);
			else
				value = formatter1.format((Number)value);
		}
		
		Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		renderer.setForeground(Color.black);
		renderer.setBackground(Color.white);
		renderer.setFont(fontNormal);
	    return renderer;
	}
    
    /**
     * Metoda trybu rysowania dla komórek tabeli jednego zbioru knockout.
     * @param value Object - wartość do wpisania
     * @param isSelected boolean - czy komórka jest wybrana
     * @param hasFocus boolean - czy jest aktywna
     * @param row int - nr wiersza
     * @param column int - nr kolumny
     * @return Component - zależy od kolumny
     */
	private Component paintCellsDefault(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if(column == 1) {
    		String name = value.toString();
    		if(name.contains("<OFFLINE")) {
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.red);
    			oLabel.setFont(fontNormal);
    			oLabel.setText(name);
    			return oLabel;
    		} else if(name.contains("<KNOCKOUT")) {
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.lightGray);
    			oLabel.setFont(fontNormal);
    			oLabel.setText(name);
    			return oLabel;
    		} 
    	}
		
		if(value instanceof Double) {
			double val = (double)value;
			if(val < 10)
				value = formatter3.format((Number)value);
			else if(val < 100)
				value = formatter2.format((Number)value);
			else
				value = formatter1.format((Number)value);
		}
		
		Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		renderer.setForeground(Color.black);
		renderer.setBackground(Color.white);
		renderer.setFont(fontNormal);
	    return renderer;
	}
	
	 /**
     * Metoda trybu rysowania dla komórek tabeli porównawczej (wszystkie).
     * @param value Object - wartość do wpisania
     * @param isSelected boolean - czy komórka jest wybrana
     * @param hasFocus boolean - czy jest aktywna
     * @param row int - nr wiersza
     * @param column int - nr kolumny
     * @return Component - zależy od kolumny
     */
	private Component paintCellsCompAll(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if(column > 2) {
			String name = value.toString();
    		if(name.contains("-999999.0")) {
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.green);
    			oLabel.setFont(fontNormal2);
    			oLabel.setText("-inf");
    			return oLabel;
    		} else if(name.contains("999999.0")) {
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.green);
    			oLabel.setFont(fontNormal2);
    			oLabel.setText("+inf");
    			return oLabel;
    		} else if(name.contains("999990.0")) {
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.lightGray);
    			oLabel.setFont(fontNormal2);
    			oLabel.setText("0 / 0");
    			return oLabel;
    		}  else {
    			String tName = table.getName();
    			
    			JLabel oLabel = (JLabel) DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    			oLabel.setForeground(Color.black);
    			oLabel.setBackground(Color.white);
    			oLabel.setFont(fontNormal2);
    			
    			
    			if(tName.contains("Places")) {
    				SimKnockPlacesCompAllTableModel model = (SimKnockPlacesCompAllTableModel) table.getModel();
    				
    				Object firstCell = table.getValueAt(row, 0);
    				int realRow = Integer.parseInt(firstCell.toString());
    				
    				DetailsPlace det = model.pTableData.get(realRow).get(column);
    				double difference = det.diff;
    				if(difference >= 100 || difference <= -100) {
    					oLabel.setForeground(Color.black);
    	    			oLabel.setBackground(Color.red);
    	    			oLabel.setFont(fontBold2);
    	    			
    	    			if(det.diff >= 1000) {
    	    				oLabel.setText(">+1000%");
    	    				return oLabel;
    	    			} else if(det.diff <= -1000) {
    	    				oLabel.setText("<-1000%");
    	    				return oLabel;
    	    			}
    				} else if(difference > 75 || difference < -75) {
    					oLabel.setForeground(Color.black);
    	    			oLabel.setBackground(Color.orange);
    	    			oLabel.setFont(fontBold2);
    				} else if(difference > 45 || difference < -45) {
    					oLabel.setForeground(Color.black);
    	    			oLabel.setBackground(Color.yellow);
    	    			oLabel.setFont(fontBold2);
    				}
    				
    				if(difference < 0)
    					name = name+"%";
    				else
    					name = "+"+name+"%";
    			} else {
    				SimKnockTransCompAllTableModel model = (SimKnockTransCompAllTableModel) table.getModel();

    				Object firstCell = table.getValueAt(row, 0);
    				int realRow = Integer.parseInt(firstCell.toString());
    				
    				DetailsTrans det = model.tTableData.get(realRow).get(column);
    				double difference = det.diff;
    				if(difference >= 100 || difference <= -100) {
    					oLabel.setForeground(Color.black);
    	    			oLabel.setBackground(Color.red);
    	    			oLabel.setFont(fontBold2);
    	    			if(det.diff >= 1000) {
    	    				oLabel.setText(">+1000%");
    	    				return oLabel;
    	    			} else if(det.diff <= -1000) {
    	    				oLabel.setText("<-1000%");
    	    				return oLabel;
    	    			}
    				} else if(difference > 75 || difference < -75) {
    					oLabel.setForeground(Color.black);
    	    			oLabel.setBackground(Color.orange);
    	    			oLabel.setFont(fontBold2);
    				} else if(difference > 45 || difference < -45) {
    					oLabel.setForeground(Color.black);
    	    			oLabel.setBackground(Color.yellow);
    	    			oLabel.setFont(fontBold2);
    				}
    				if(difference < 0)
    					name = name+"%";
    				else
    					name = "+"+name+"%";
    			}
    			
    			oLabel.setText(name);
    			return oLabel;
    		}
		}
		
		
		Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		renderer.setForeground(Color.black);
		renderer.setBackground(Color.white);
		renderer.setFont(fontNormal2);
	    return renderer;
	}
}
