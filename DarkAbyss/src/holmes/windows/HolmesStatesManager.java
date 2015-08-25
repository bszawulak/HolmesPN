package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.PlacesStateVector;
import holmes.petrinet.data.StatesManager;
import holmes.petrinet.elements.Place;
import holmes.tables.RXTable;
import holmes.tables.StatesPlacesTableModel;
import holmes.tables.StatesPlacesTableRenderer;
import holmes.utilities.Tools;

/**
 * Klasa tworząca okno managera stanów początkowych (m0) sieci.
 * 
 * @author MR
 *
 */
public class HolmesStatesManager extends JFrame {
	private static final long serialVersionUID = -4590055483268695118L;
	private StatesPlacesTableRenderer tableRenderer;
	private StatesPlacesTableModel tableModel;
	private JTable statesTable;
	
	private ArrayList<Place> places;
	private PetriNet pn;
	private StatesManager statesManager;
	
	/**
	 * Główny konstruktor okna menagera stanów początkowych.
	 */
	public HolmesStatesManager() {
		setTitle("Holmes starting states manager");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
    	
    	pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
    	places = pn.getPlaces();
    	statesManager = pn.accessStatesManager();
    	
    	initalizeComponents();
    	initiateListeners();
    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(false);
    	setVisible(true);
	}

	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 650));
		setLocation(50, 50);
		setResizable(false);
		
		setLayout(new BorderLayout());
		JPanel main = new JPanel(new BorderLayout());

		JPanel submain = new JPanel(new BorderLayout());
		submain.add(getMainTablePanel(), BorderLayout.CENTER);
		submain.add(getBottomPanel(), BorderLayout.SOUTH);
		
		main.add(submain, BorderLayout.CENTER);
		main.add(getButtonsPanel(), BorderLayout.EAST);
		
		add(main, BorderLayout.CENTER);
		
		statesTable.validate();
	}
	
	/**
	 * Tworzy panel główny tablicy.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("States table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		//statesTable = new JTable(new DefaultTableModel());
		tableModel = new StatesPlacesTableModel(places.size());
		//statesTable.setModel(tableModel);
		statesTable = new RXTable(tableModel);
		((RXTable)statesTable).setSelectAllForEdit(true);
		
		statesTable.getColumnModel().getColumn(0).setHeaderValue("Sel:");
		statesTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		statesTable.getColumnModel().getColumn(0).setMinWidth(30);
		statesTable.getColumnModel().getColumn(0).setMaxWidth(30);
		statesTable.getColumnModel().getColumn(1).setHeaderValue("State ID");
		statesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		statesTable.getColumnModel().getColumn(1).setMinWidth(50);
		statesTable.getColumnModel().getColumn(1).setMaxWidth(50);
		for(int i=0; i<places.size(); i++) {
			statesTable.getColumnModel().getColumn(i+2).setHeaderValue("p"+i);
			statesTable.getColumnModel().getColumn(i+2).setPreferredWidth(45);
			statesTable.getColumnModel().getColumn(i+2).setMinWidth(45);
			statesTable.getColumnModel().getColumn(i+2).setMaxWidth(45);
        }
		
		//TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(statesTable.getModel());
		//statesTable.setRowSorter(sorter);
        
		statesTable.setName("StatesTable");
		statesTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		tableRenderer = new StatesPlacesTableRenderer(statesTable);
		statesTable.setDefaultRenderer(Object.class, tableRenderer);
		statesTable.setDefaultRenderer(Double.class, tableRenderer);
		
    	statesTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction();
          	    }
          	 }
      	});
    	
    	
    	int selectedState = statesManager.selectedState;
    	for(int row=0; row<statesManager.accessStateMatrix().size(); row++) {
    		ArrayList<String> rowVector = new ArrayList<String>();
    		
    		if(selectedState == row)
    			rowVector.add("X");
    		else
    			rowVector.add("X");
    		
    		PlacesStateVector psVector = statesManager.getState(row);
    		rowVector.add("m0("+(row+1)+")");
    		
    		for(int p=0; p<psVector.getSize(); p++) {
    			rowVector.add(""+psVector.getTokens(p));
        	}
    		tableModel.addNew(rowVector);
    	}
    	
    	//statesTable.setCellEditor(new MyCellEditor());

    	statesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(statesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
		
		
	    return result;
	}

	/**
	 * Tworzy panel przycisków bocznych.
	 * @return JPanel - panel
	 */
	public JPanel getButtonsPanel() {
		JPanel result = new JPanel();
		result.setBorder(BorderFactory.createTitledBorder("Buttons"));
		result.setPreferredSize(new Dimension(150, 500));

		int posXda = 10;
		int posYda = 15;
		
	    return result;
	}
	
	/**
	 * Tworzy panel dolny z informacjami o stanie.
	 * @return JPanel - panel
	 */
	public JPanel getBottomPanel() {
		JPanel result = new JPanel();
		result.setBorder(BorderFactory.createTitledBorder("Others"));
		result.setPreferredSize(new Dimension(900, 150));

		int posXda = 10;
		int posYda = 15;
		
	    return result;
	}
	
	protected void cellClickAction() {
		try {
			int selectedRow = statesTable.getSelectedRow();
			int selectedColumn = statesTable.getSelectedColumn();
			
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
		    }
		});

    }
}
