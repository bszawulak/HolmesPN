package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.SPNdataVectorManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SPNdataVector.SPNdataContainer;
import holmes.petrinet.data.SPNdataVector;
import holmes.petrinet.elements.Transition;
import holmes.tables.SPNsingleVectorTableModel;
import holmes.tables.SPNsingleVectorTableRenderer;
import holmes.tables.RXTable;
import holmes.utilities.Tools;

/**
 * Okno edycji firing rate przechowywanych w danym wektorze wejściowym.
 * 
 * @author MR
 */
public class HolmesSPNeditor extends JFrame {
	private static final long serialVersionUID = -6810858686209063022L;
	private GUIManager overlord;
	private JFrame parentWindow;
	private JFrame ego;
	private TableCellRenderer tableRenderer;
	private SPNsingleVectorTableModel tableModel;
	private JTable table;
	private JPanel tablePanel;
	private SPNdataVector frData;
	private int frIndex;
	private JTextArea vectorDescrTextArea;
	
	private ArrayList<Transition> transitions;
	private PetriNet pn;
	private SPNdataVectorManager firingRatesManager;
	
	/**
	 * Główny konstruktor okna menagera danych SPN.
	 * @param parent JFrame - okno wywołujące
	 * @param frData SPNtransitionsVector - wektor danych SPN
	 * @param frIndex int - indeks powyższego wektora w tablicy
	 */
	public HolmesSPNeditor(JFrame parent, SPNdataVector frData, int frIndex) {
		setTitle("Holmes SPN data editor");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {
			
		}
    	this.overlord = GUIManager.getDefaultGUIManager();
    	this.pn = overlord.getWorkspace().getProject();
    	this.ego = this;
    	this.parentWindow = parent;
    	this.frData = frData;
    	this.frIndex = frIndex;
    	this.transitions = pn.getTransitions();
    	this.firingRatesManager = pn.accessFiringRatesManager();
    	
    	initalizeComponents();
    	initiateListeners();
    	fillTable();
    	setVisible(true);
    	parentWindow.setEnabled(false);
    	
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	public void fillTable() {
		tableModel.clearModel();
		
		//int selectedState = firingRatesManager.selectedVector;
		//int size = frData.getSize();
		
		int row = -1;
		for(SPNdataContainer frBox : frData.accessVector()) {
			row++;
			switch(frBox.sType) {
				case ST:
					tableModel.addNew(row, transitions.get(row).getName(), ""+frData.getFiringRate(row), frBox.sType);
					break;
				case DT:
					tableModel.addNew(row, transitions.get(row).getName(), ""+frBox.DET_delay, frBox.sType);
					break;
				case IM:
					tableModel.addNew(row, transitions.get(row).getName(), ""+frBox.IM_priority, frBox.sType);
					break;
				case SchT:
					tableModel.addNew(row, transitions.get(row).getName(), ""+frBox.SCH_start+"; "+frBox.SCH_rep+"; "+frBox.SCH_end, frBox.sType);
					break;
			}
		}
		tableModel.fireTableDataChanged();
	}
	
	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 650));
		setLocation(50, 50);
		setResizable(true);
		setLayout(new BorderLayout());
		
		tablePanel = getMainTablePanel();
		add(getTopPanel(), BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
	}
	
	/**
	 * Buduje i zwraca panel górny okna.
	 * @return JPanel - panel
	 */
	private JPanel getTopPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("SPN transitions data"));
		result.setPreferredSize(new Dimension(500, 100));
		
		JPanel filler = new JPanel(null);

		int posX = 5;
		int posY = 0;
		
		JLabel label0 = new JLabel("SPN data vector ID: ");
		label0.setBounds(posX, posY, 120, 20);
		filler.add(label0);
		
		JLabel labelID = new JLabel(frIndex+"");
		labelID.setBounds(posX+130, posY, 100, 20);
		filler.add(labelID);
		
		vectorDescrTextArea = new JTextArea(firingRatesManager.getSPNvectorDescription(frIndex));
		vectorDescrTextArea.setLineWrap(true);
		vectorDescrTextArea.setEditable(true);
		vectorDescrTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		firingRatesManager.setSPNvectorDescription(frIndex, newComment);
            		fillTable();
            	}
            }
        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(vectorDescrTextArea), BorderLayout.CENTER);
        CreationPanel.setBounds(posX, posY+=20, 600, 50);
        filler.add(CreationPanel);
        
        result.add(filler, BorderLayout.CENTER);
		return result;
	}
	
	/**
	 * Tworzy panel główny tablicy.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("SPN data vector transitions table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new SPNsingleVectorTableModel(this, frIndex);
		table = new RXTable(tableModel);
		((RXTable)table).setSelectAllForEdit(true);
		
		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(1).setHeaderValue("Transition name");
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(2).setHeaderValue("Data");
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(50);
		table.getColumnModel().getColumn(3).setHeaderValue("SPN sub-type");
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setMinWidth(70);
        
		table.setName("FiringRatesTransitionTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		tableRenderer = new SPNsingleVectorTableRenderer(table);
		table.setDefaultRenderer(Object.class, tableRenderer);
		table.setDefaultRenderer(Double.class, tableRenderer);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent event) {
	            // do some actions here, for example
	            // print first column value from selected row
	        	int selectedRow = table.getSelectedRow();
	        	if(selectedRow > -1)
	        		new HolmesSPNtransitionEditor(ego, frData.getSPNtransitionContainer(selectedRow), transitions.get(selectedRow), new Point(400, 400)); 
	        }
	    });
    	
		table.setRowSelectionAllowed(false);
    	
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
			
	    return result;
	}
	
	/**
	 * Metoda ustawia nową wartość firing rate, wywoływana przez metodę TableModel która odpowiada za zmianę
	 * wartości pola firing rate.
	 * @param index int - nr wektora
	 * @param transID int - nr tranzycji
	 * @param newValue String - nowa funkcja firing rate
	 */
	public void changeRealValue(int index, int transID, String newValue) {
		firingRatesManager.getSPNdataVector(index).accessVector().get(transID).ST_function = newValue;
		overlord.markNetChange();
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentWindow.setEnabled(true);
		    }
		});
    	
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillTable();
  	  	    }  
    	});
    }
}
