package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.FiringRatesManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.FiringRateTransVector;
import holmes.petrinet.elements.Transition;
import holmes.tables.FiringRatesOneTransTableModel;
import holmes.tables.FiringRatesOneTransTableRenderer;
import holmes.tables.RXTable;
import holmes.utilities.Tools;

/**
 * Okno edycji firing rate przechowywanych w danym wektorze wejściowym.
 * 
 * @author MR
 */
public class HolmesFiringRatesEditor extends JFrame {
	private static final long serialVersionUID = -6810858686209063022L;
	private GUIManager overlord;
	private JFrame parentWindow;
	private TableCellRenderer tableRenderer;
	private FiringRatesOneTransTableModel tableModel;
	private JTable table;
	private JPanel tablePanel;
	private FiringRateTransVector frData;
	private int frIndex;
	private JTextArea vectorDescrTextArea;
	
	private ArrayList<Transition> transitions;
	private PetriNet pn;
	private FiringRatesManager firingRatesManager;
	
	/**
	 * Główny konstruktor okna menagera stanów początkowych.
	 * @param parent JFrame - okno wywołujące
	 * @param frData TransFiringRateVector - wektor firing rates
	 * @param frIndex int - indeks powyższego wektora w tablicy
	 */
	public HolmesFiringRatesEditor(JFrame parent, FiringRateTransVector frData, int frIndex) {
		setTitle("Holmes firing rates editor");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
    	this.overlord = GUIManager.getDefaultGUIManager();
    	this.pn = overlord.getWorkspace().getProject();
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
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTable() {
		tableModel.clearModel();
		
		//int selectedState = firingRatesManager.selectedVector;
		int size = frData.getSize();
    	for(int row=0; row<size; row++) {
    		tableModel.addNew(row, transitions.get(row).getName(), frData.getFiringRate(row), frData.getStochasticType(row));
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
		result.setBorder(BorderFactory.createTitledBorder("Firing rates vector data"));
		result.setPreferredSize(new Dimension(500, 100));
		
		JPanel filler = new JPanel(null);

		int posX = 5;
		int posY = 0;
		
		JLabel label0 = new JLabel("Firing vector ID: ");
		label0.setBounds(posX, posY, 100, 20);
		filler.add(label0);
		
		JLabel labelID = new JLabel(frIndex+"");
		labelID.setBounds(posX+110, posY, 100, 20);
		filler.add(labelID);
		
		vectorDescrTextArea = new JTextArea(firingRatesManager.getFRVectorDescription(frIndex));
		vectorDescrTextArea.setLineWrap(true);
		vectorDescrTextArea.setEditable(true);
		vectorDescrTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		firingRatesManager.setFRvectorDescription(frIndex, newComment);
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
		result.setBorder(BorderFactory.createTitledBorder("Firing rates table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new FiringRatesOneTransTableModel(this, frIndex);
		table = new RXTable(tableModel);
		((RXTable)table).setSelectAllForEdit(true);
		
		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(1).setHeaderValue("Transition name");
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(2).setHeaderValue("Firing rate");
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(50);
		table.getColumnModel().getColumn(3).setHeaderValue("SPN sub-type");
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setMinWidth(70);
        
		table.setName("FiringRatesTransitionTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		tableRenderer = new FiringRatesOneTransTableRenderer(table);
		table.setDefaultRenderer(Object.class, tableRenderer);
		table.setDefaultRenderer(Double.class, tableRenderer);

		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		;	//cellClickAction();
          	    }
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
	 * @param newValue double - nowe firing rate
	 */
	public void changeRealValue(int index, int transID, double newValue) {
		firingRatesManager.getFRVector(index).accessVector().get(transID).fr = newValue;
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
    }
}
