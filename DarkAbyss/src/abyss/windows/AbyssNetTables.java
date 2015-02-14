package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import abyss.darkgui.GUIManager;
import abyss.math.simulator.StateSimulator;
import abyss.math.simulator.NetSimulator.NetType;
import abyss.tables.InvariantsTableModel;
import abyss.tables.PTITableRenderer;
import abyss.tables.PlacesTableModel;
import abyss.tables.TransitionsTableModel;
import abyss.utilities.Tools;

/**
 * Klasa odpowiedzialna za rysowanie i obsługę okna tabel elementów sieci.
 * @author MR
 *
 */
public class AbyssNetTables extends JFrame implements ComponentListener {
	private static final long serialVersionUID = 8429744762731301629L;
	
	//interface components:
	private final JFrame ego;
	@SuppressWarnings("unused")
	private JFrame parentFrame;
	private JPanel mainPanel;
	private JPanel tablesSubPanel;
	private JPanel buttonsPanel;
	private JScrollPane tableScrollPane;
	//data components:
	private JTable table;
	
	private DefaultTableModel model;
	private PlacesTableModel modelPlaces;
	private TransitionsTableModel modelTransition;
	private InvariantsTableModel modelInvariants;
	private PTITableRenderer tableRenderer; // = new PTITableRenderer();
	public int currentClickedRow;
	
	private final AbyssNetTablesActions action;
	
	/**
	 * Główny konstruktor okna tabel sieci.
	 * @param papa JFrame - ramka okna głównego
	 */
	public AbyssNetTables(JFrame papa) {
		ego = this;
		action  = new AbyssNetTablesActions(this);
		parentFrame = papa;
		
		try {
			initialize_components();
			setVisible(false);
		} catch (Exception e) {
			String msg = e.getMessage();
			GUIManager.getDefaultGUIManager().log("Critical error, cannot create Abyss Net Tables window:", "error", true);
			GUIManager.getDefaultGUIManager().log(msg, "error", false);
		}
	}
	
	/**
	 * Metoda inicjalizująca komponenty interfejsu okna.
	 */
	private void initialize_components() {
		setTitle("Net data tables");
    	try { setIconImage(Tools.getImageFromIcon("/icons/blackhole.png")); } 
    	catch (Exception e ) { }
    	
    	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(20, 20);
		
		setSize(new Dimension(800,600));
		addComponentListener(this); //konieczne, aby listenery (przede wszystkim resize) działały
		addWindowStateListener(new WindowAdapter() {
			public void windowStateChanged(WindowEvent e) {
				if(e.getNewState() == JFrame.MAXIMIZED_BOTH) {
					ego.setExtendedState(JFrame.NORMAL);
					resizeComponents();
				}
			}
		});
		
		mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.add(createTablePanel());
		mainPanel.add(createButtonsPanel());
		add(mainPanel);
	}
	
	/**
	 * Metoda ta tworzy obiekty modelu i tabeli, inicjalizuje listenery tablicy.
	 */
	private void createTableConstruct() {
		model = new DefaultTableModel();
		table = new JTable(model);
		tableRenderer = new PTITableRenderer(table);
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		action.cellClickAction(table);
          	    }
          	 }
      	});
	}
	
	/**
	 * Metoda tworząca panel główny okna służący do wyświetlania tabeli danych.
	 * @return JPanel - panel główny
	 */
	private JPanel createTablePanel() {
		tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setBounds(0, 0, 670, 560);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Tables:"));
		
		createTableConstruct();

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		
		return tablesSubPanel;
	}

	/**
	 * Metoda pomocnicza tworząca panel boczny przycisków.
	 * @return JPanel - panel boczny przycisków
	 */
	private JPanel createButtonsPanel() {
		buttonsPanel = new JPanel(null);
		buttonsPanel.setBounds(670, 0, 130, 560);
		//buttonsPanel.setBorder(BorderFactory.createTitledBorder("Buttons:"));
		
		//********************************************** NODES ****************************************************
		
		JPanel buttonNodePanel = new JPanel(null);
		buttonNodePanel.setBounds(0, 0, 130, 120);
		buttonNodePanel.setBorder(BorderFactory.createTitledBorder("Nodes tables"));
		
		int yPos = 20;
		int xPos = 10;
		int bWidth = 110;
		int bHeight = 30;
		
		JButton transitionsButton = createStandardButton("Places", Tools.getResIcon32(""));
		transitionsButton.setToolTipText("    ");
		transitionsButton.setBounds(xPos, yPos, bWidth, bHeight);
		transitionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createPlacesTable();
			}
		});
		transitionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonNodePanel.add(transitionsButton);
		yPos = yPos + bHeight + 5;
		
		JButton placesButton = createStandardButton("Transitions", Tools.getResIcon32(""));
		placesButton.setToolTipText("    ");
		placesButton.setBounds(xPos, yPos, bWidth, bHeight);
		placesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createTransitionTable();
			}
		});
		placesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonNodePanel.add(placesButton);
		yPos = yPos + bHeight + 5;
		
		JButton switchButton = createStandardButton("Switch pos.", Tools.getResIcon32(""));
		switchButton.setToolTipText("    ");
		switchButton.setBounds(xPos, yPos, bWidth, 20);
		switchButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				boolean status = action.switchSelected(table);
				if(status == true) {
					if(table.getName().equals("PlacesTable")) {
						createPlacesTable();
					} else if(table.getName().equals("TransitionTable")) {
						createTransitionTable();
					}
				}
			}
		});
		switchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonNodePanel.add(switchButton);
		
		buttonsPanel.add(buttonNodePanel);
		
		//********************************************** INVARIANTS ****************************************************
		
		JPanel buttonsInvariantsPanel = new JPanel(null);
		buttonsInvariantsPanel.setBounds(0, buttonNodePanel.getHeight(), 130, 220);
		buttonsInvariantsPanel.setBorder(BorderFactory.createTitledBorder("Invariants table"));
		
		yPos = 20;
		xPos = 10;
		
		JButton invariantsButton = createStandardButton("Invariants", Tools.getResIcon32(""));
		invariantsButton.setToolTipText("    ");
		invariantsButton.setBounds(xPos, yPos, bWidth, bHeight);
		invariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createInvariantsTable();
			}
		});
		invariantsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsInvariantsPanel.add(invariantsButton);
		
		buttonsPanel.add(buttonsInvariantsPanel);
		
		yPos = yPos + bHeight + 15;
		
		return buttonsPanel;
	}

	/**
	 * Metoda tworząca tabelę miejsc
	 */
    private void createPlacesTable() {
    	modelPlaces = new PlacesTableModel();
        table.setModel(modelPlaces);
        
        table.getColumnModel().getColumn(0).setHeaderValue("ID");
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
    	table.getColumnModel().getColumn(0).setMinWidth(30);
    	table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setHeaderValue("Place name:");
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
    	table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(2).setHeaderValue("Tok:");
        table.getColumnModel().getColumn(2).setPreferredWidth(40);
    	table.getColumnModel().getColumn(2).setMinWidth(40);
    	table.getColumnModel().getColumn(2).setMaxWidth(40);
        table.getColumnModel().getColumn(3).setHeaderValue("In-T");
        table.getColumnModel().getColumn(3).setPreferredWidth(40);
    	table.getColumnModel().getColumn(3).setMinWidth(40);
    	table.getColumnModel().getColumn(3).setMaxWidth(40);
        table.getColumnModel().getColumn(4).setHeaderValue("Out-T");
        table.getColumnModel().getColumn(4).setPreferredWidth(40);
    	table.getColumnModel().getColumn(4).setMinWidth(40);
    	table.getColumnModel().getColumn(4).setMaxWidth(40);
        table.getColumnModel().getColumn(5).setHeaderValue("Avg.Tk");
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
    	table.getColumnModel().getColumn(5).setMinWidth(50);
    	table.getColumnModel().getColumn(5).setMaxWidth(50);
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(sorter);
        
        table.setName("PlacesTable");
        tableRenderer.setMode(0); //mode: places
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);

        action.addPlacesToModel(modelPlaces); // metoda generująca dane o miejscach
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.validate();
    }
    
    /**
     * Metoda przygotowująca tabelę dla tranzycji.
     */
    private void createTransitionTable() { 
        modelTransition = new TransitionsTableModel();
        table.setModel(modelTransition);
        
        table.getColumnModel().getColumn(0).setHeaderValue("ID");
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
    	table.getColumnModel().getColumn(0).setMinWidth(30);
    	table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setHeaderValue("Transition name");
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
    	table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(2).setHeaderValue("Pre-P");
        table.getColumnModel().getColumn(2).setPreferredWidth(40);
    	table.getColumnModel().getColumn(2).setMinWidth(40);
    	table.getColumnModel().getColumn(2).setMaxWidth(40);
        table.getColumnModel().getColumn(3).setHeaderValue("Post-P");
        table.getColumnModel().getColumn(3).setPreferredWidth(40);
    	table.getColumnModel().getColumn(3).setMinWidth(40);
    	table.getColumnModel().getColumn(3).setMaxWidth(40);
        table.getColumnModel().getColumn(4).setHeaderValue("Fired");
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
    	table.getColumnModel().getColumn(4).setMinWidth(60);
    	table.getColumnModel().getColumn(4).setMaxWidth(60);
        table.getColumnModel().getColumn(5).setHeaderValue("Inv");
        table.getColumnModel().getColumn(5).setPreferredWidth(40);
    	table.getColumnModel().getColumn(5).setMinWidth(40);
    	table.getColumnModel().getColumn(5).setMaxWidth(40);
        
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(sorter);
		
        table.setName("TransitionTable");
        tableRenderer.setMode(1); //mode: transitions
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);

        action.addTransitionsToModel(modelTransition); // metoda generująca dane o tranzycjach
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.validate();
	}
    
	private void createInvariantsTable() {
    	ArrayList<ArrayList<Integer>> invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
    	if(invariantsMatrix == null || invariantsMatrix.size() == 0) {
    		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size() == 0) return;
    		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size() == 0) return;
    		
    		GUIManager.getDefaultGUIManager().io.generateINAinvariants();
    		invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
    	}
    	if(invariantsMatrix == null || invariantsMatrix.size() == 0) return; //final check
    	
    	ArrayList<Integer> invSize = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsSize();
    	int transNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
    	
    	modelInvariants = new InvariantsTableModel(transNumber);        
        table.setModel(modelInvariants);
        
        table.getColumnModel().getColumn(0).setHeaderValue("ID");
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
    	table.getColumnModel().getColumn(0).setMinWidth(40);
    	table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setHeaderValue("Trans. #:");
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
    	table.getColumnModel().getColumn(1).setMinWidth(50);
    	table.getColumnModel().getColumn(1).setMaxWidth(50);
    	for(int i=0; i<transNumber; i++) {
        	table.getColumnModel().getColumn(i+2).setHeaderValue("t"+i);
            table.getColumnModel().getColumn(i+2).setPreferredWidth(70);
        	table.getColumnModel().getColumn(i+2).setMinWidth(70);
        	table.getColumnModel().getColumn(i+2).setMaxWidth(70);
        }
        
        table.setName("InvariantsTable");
        tableRenderer.setMode(2); //mode: invariants
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);
		table.setRowSorter(null);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        StateSimulator ss = new StateSimulator();
		ss.initiateSim(NetType.BASIC, false);
		ss.simulateNetSimple(10000, false);
		ArrayList<Double> resVector = ss.getTransitionsAvgData();
		
		for(int i=0; i<invariantsMatrix.size(); i++) {
			ArrayList<Integer> dataV = invariantsMatrix.get(i);
			ArrayList<String> newRow = new ArrayList<String>();
			newRow.add(""+i);
			newRow.add(""+invSize.get(i));
			
			for(int t=0; t<dataV.size(); t++) {
				int value = dataV.get(t);
				if(value>0) {
					double avg = resVector.get(t);
					avg *= 100; // do 100%
					String cell = ""+value+"("+Tools.cutValue(avg)+"%)";
					newRow.add(cell);
				} else {
					newRow.add("");
				}
			}
			modelInvariants.addNew(newRow);
		}
        table.validate();
    }
	
	/**
	 * Metoda pomocnicza do tworzenia przycisków do panelu bocznego.
	 * @param text String - tekst przycisku
	 * @param icon Icon - ikona
	 * @return JButton - nowy przycisk
	 */
	private JButton createStandardButton(String text, Icon icon) {
		JButton resultButton = new JButton(); 
		resultButton.setLayout(new BorderLayout());
        JLabel tmp;
        tmp = new JLabel(text);
        tmp.setFont(new Font("Arial", Font.PLAIN, 12));
    	resultButton.add(tmp, BorderLayout.CENTER);
        resultButton.setPreferredSize(new Dimension(100, 60));
        resultButton.setMinimumSize(new Dimension(100, 60));
        resultButton.setMaximumSize(new Dimension(100, 60));
        resultButton.setIcon(icon);
		return resultButton;
	}
	
	public void updateRow(String data, int column) {
		table.getModel().setValueAt(data, currentClickedRow, column);
	}
	
	//*************************************************************************************************************************
	//**************************************************               ********************************************************
	//**************************************************   LISTENERS   ********************************************************
	//**************************************************               ********************************************************
	//*************************************************************************************************************************
	
	/**
	 * Metoda interfejsu ComponentListener, odpowiada za dopasowanie rozmiaru paneli
	 * tabel i przycisków.
	 */
	public void componentResized(ComponentEvent e) {
		resizeComponents();
	}
	public void componentHidden(ComponentEvent e) {} //unused
	public void componentMoved(ComponentEvent e) {} //unused
	public void componentShown(ComponentEvent e) {} //unused
	
	private void resizeComponents() {
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setSize(mainPanel.getWidth()-130, mainPanel.getHeight());
		buttonsPanel.setLocation(tablesSubPanel.getWidth(), 0);
		buttonsPanel.setSize(130, mainPanel.getHeight());
	}
}
