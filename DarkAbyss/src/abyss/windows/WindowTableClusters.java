package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class WindowTableClusters extends JPanel {
	private static final long serialVersionUID = 6942814230861358341L;
	private boolean DEBUG = false;
    public final JTable table;
    
    public WindowTableClusters() {
        //super(new GridLayout(1,0));
    	super(new BorderLayout());
        //super(new GridLayout(1,0));

    	DefaultTableModel  model = new DefaultTableModel();
        model.addColumn("First Name");
        model.addColumn("Last Name");
        model.addColumn("Years");
        model.addColumn("Years");
        model.addColumn("Years");
        model.addColumn("Years");
        model.addColumn("Years");
        //model.add

        String[] socrates = { "Socrates", "", "469-399 B.C." };
        model.addRow(socrates);

        String[] plato = { "Plato", "", "428-347 B.C." };
        model.addRow(plato);

        String[] aquinas = { "Thomas", "Aquinas", "1225-1274" };
        model.addRow(aquinas);

        String[] kierkegaard = { "Soren", "Kierkegaard", "1813-1855" };
        model.addRow(kierkegaard);

        String[] kant = { "Immanuel", "Kant", "1724-1804" };
        model.addRow(kant);

        String[] nietzsche = { "Friedrich", "Nietzsche", "1844-1900" };
        model.addRow(nietzsche);

        String[] arendt = { "Hannah", "Arendt", "1906-1975" };
        model.addRow(arendt);
        
    	//model.addColumn("aaaa");
        table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);

        //TableColumnModel tcm = table.getColumnModel();
        //for(int i=0; i<tcm.getColumnCount(); i++) {
        //	tcm.getColumn(i).setWidth(20);
       //     tcm.getColumn(i).setMaxWidth(20);
       // }
        table.addMouseListener(new MouseAdapter() {
        	  public void mouseClicked(MouseEvent e) {
        	    if (e.getClickCount() == 1) {
        	      JTable target = (JTable)e.getSource();
        	      int row = target.getSelectedRow();
        	      int column = target.getSelectedColumn();
        	      JOptionPane.showMessageDialog(null,""+row+" "+column,"test",JOptionPane.INFORMATION_MESSAGE);
        	      // do some action if appropriate column
        	    }
        	  }
        	});
        
        
        //String xxx = table.getValueAt(2, 2).toString();
        
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Add the scroll pane to this panel.
        add(scrollPane);
    }

    class MyTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 5550113827920372795L;
		private String[] columnNames = {"First Name",
                                        "Last Name",
                                        "Sport",
                                        "# of Years",
                                        "Vegetarian"};
        private Object[][] data = {
	    {"Kathy", "Smith",
	     "Snowboarding", new Integer(5), new Boolean(false)},
	    {"John", "Doe",
	     "Rowing", new Integer(3), new Boolean(true)},
	    {"Sue", "Black",
	     "Knitting", new Integer(2), new Boolean(false)},
	    {"Jane", "White",
	     "Speed reading", new Integer(20), new Boolean(true)},
	    {"Joe", "Brown",
	     "Pool", new Integer(10), new Boolean(false)}
        };

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 2) {
                return false;
            } else {
                return true;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }

            data[row][col] = value;
            fireTableCellUpdated(row, col);

            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }
}
        /*
	private static final long serialVersionUID = 6942814230861358341L;
	private boolean DEBUG = false;
    public final JTable table;
    
    public PanelTable() {
        //super(new GridLayout(1,0));
    	super(new BorderLayout());
        String[] columnNames = {"First Name", "Last Name", "Sport", "# of Years", "Vegetarian"};

        Object[][] data = {
        		{"Kathy", "Smith", "Snowboarding", new Integer(5), new Boolean(false)},
        	    {"John", "Doe", "Rowing", new Integer(3), new Boolean(true)},
        	    {"Sue", "Black", "Knitting", new Integer(2), new Boolean(false)},
        	    {"Jane", "White", "Speed reading", new Integer(20), new Boolean(true)},
        	    {"Joe", "Brown", "Pool", new Integer(10), new Boolean(false)}
        };

        table = new JTable(data, columnNames);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.addColumn(new TableColumn());

        if (DEBUG) {
            table.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    printDebugData(table);
                }
            });
        }

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Add the scroll pane to this panel.
        add(scrollPane, BorderLayout.CENTER);
    }

    public String[] getRowAt(int row) {
        String[] result = new String[table.getColumnCount()];

        for (int i = 0; i < table.getColumnCount(); i++) {
            result[i] = table.getModel().getValueAt(row, i).toString();
        }

        return result;
   }
    
    private void printDebugData(JTable table) {
        int numRows = table.getRowCount();
        int numCols = table.getColumnCount();
        javax.swing.table.TableModel model = table.getModel();

        System.out.println("Value of data: ");
        for (int i=0; i < numRows; i++) {
            System.out.print("    row " + i + ":");
            for (int j=0; j < numCols; j++) {
                System.out.print("  " + model.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
    */

