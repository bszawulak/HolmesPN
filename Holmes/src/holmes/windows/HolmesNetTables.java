package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.tables.InvariantsSimulatorTableModel;
import holmes.tables.InvariantsTableModel;
import holmes.tables.PTITableRenderer;
import holmes.tables.PlacesTableModel;
import holmes.tables.TransitionsTableModel;
import holmes.utilities.Tools;

/**
 * Klasa odpowiedzialna za rysowanie i obsługę okna tabel elementów sieci.
 * 
 * @author MR
 */
public class HolmesNetTables extends JFrame {
	@Serial
	private static final long serialVersionUID = 8429744762731301629L;
	
	//interface components:
	@SuppressWarnings("unused")
	private JFrame parentFrame;
	private JPanel mainPanel;
	private JPanel tablesSubPanel;
	private JPanel buttonsPanel;
	private JScrollPane tableScrollPane;
	private JComboBox<String> invSimNetModeCombo;
	private SimulatorGlobals.SimNetType invSimNetType = SimulatorGlobals.SimNetType.BASIC;
	private boolean doNotUpdate = false;
	//data components:
	private JTable table;
	
	private DefaultTableModel model;
	private PlacesTableModel modelPlaces;
	private TransitionsTableModel modelTransition;
	private InvariantsTableModel modelBasicInvariants;
	private InvariantsSimulatorTableModel modelInvariantsSimData;
	private PTITableRenderer tableRenderer;
	public int currentClickedRow;
	private int simStepsForInv = 10000;
	private boolean maxModeForSSInv = false;
	private boolean singleModeForSSInv = false;
	
	private final HolmesNetTablesActions action;
	
	/**
	 * Główny konstruktor okna tabel sieci.
	 * @param papa JFrame - ramka okna głównego
	 */
	public HolmesNetTables(JFrame papa) {
		//ego = this;
		action  = new HolmesNetTablesActions(this);
		parentFrame = papa;
		
		try {
			initialize_components();
			setVisible(false);
		} catch (Exception e) {
			String msg = e.getMessage();
			GUIManager.getDefaultGUIManager().log("Critical error, cannot create Holmes Net Tables window:", "error", true);
			GUIManager.getDefaultGUIManager().log(msg, "error", false);
		}
	}
	
	/**
	 * Metoda resetuje macierz danych o inwariantach.
	 */
	public void resetT_invData() {
		action.dataMatrix = null;
	}
	
	/**
	 * Metoda inicjalizująca komponenty interfejsu okna.
	 */
	private void initialize_components() {
		setTitle("Net data tables");
    	try { setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png")); } 
    	catch (Exception e ) { }
    	
    	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(20, 20);
		
		setSize(new Dimension(900, 700));
		
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(createTablePanel(), BorderLayout.CENTER);
		mainPanel.add(createButtonsPanel(), BorderLayout.EAST);
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
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
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
		//buttonsPanel.setBounds(670, 0, 130, 560);
		
		buttonsPanel.setPreferredSize(new Dimension(130, 560));
		buttonsPanel.setLocation(670, 0);
		//********************************************** NODES ****************************************************
		
		JPanel upperButtonsPanel = new JPanel(null);
		upperButtonsPanel.setBounds(0, 0, 130, 160);
		upperButtonsPanel.setBorder(BorderFactory.createTitledBorder("P / T / Inv. data"));
		
		int yPos = 20;
		int xPos = 10;
		int bWidth = 110;
		int bHeight = 30;
		
		//JButton placesButton = createStandardButton("Places", Tools.getResIcon32(""));
		JButton placesButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Places&nbsp;&nbsp;&nbsp;&nbsp;</html>");
		placesButton.setFocusPainted(false);
		placesButton.setMargin(new Insets(0, 0, 0, 0));
		placesButton.setIcon(Tools.getResIcon16("/icons/netTables/placeIcon.png"));
		placesButton.setToolTipText("Shows places table");
		placesButton.setBounds(xPos, yPos, bWidth, bHeight);
		placesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createPlacesTable();
			}
		});
		placesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		upperButtonsPanel.add(placesButton);
		yPos = yPos + bHeight + 5;
		
		//JButton transitionsButton = createStandardButton("Transitions", Tools.getResIcon32(""));
		JButton transitionsButton = new JButton("Transitions");
		transitionsButton.setFocusPainted(false);
		transitionsButton.setMargin(new Insets(0, 0, 0, 0));
		transitionsButton.setIcon(Tools.getResIcon16("/icons/netTables/transIcon.png"));
		transitionsButton.setToolTipText("Shows transitions table");
		transitionsButton.setBounds(xPos, yPos, bWidth, bHeight);
		transitionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createTransitionTable();
			}
		});
		transitionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		upperButtonsPanel.add(transitionsButton);
		yPos = yPos + bHeight + 5;
		
		//JButton switchButton = createStandardButton("Switch P/T", Tools.getResIcon32(""));
		JButton switchButton = new JButton("Switch P / T");
		switchButton.setFocusPainted(false);
		switchButton.setMargin(new Insets(0, 0, 0, 0));
		switchButton.setIcon(Tools.getResIcon16("/icons/netTables/switchIcon.png"));
		switchButton.setToolTipText("Switch internal localization of two places or two transitions.");
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
		upperButtonsPanel.add(switchButton);
		
		yPos = yPos + bHeight - 5;
		
		//JButton invButton = createStandardButton("Invariants", Tools.getResIcon32(""));
		JButton invButton = new JButton("t-invariants");
		invButton.setFocusPainted(false);
		invButton.setMargin(new Insets(0, 0, 0, 0));
		invButton.setIcon(Tools.getResIcon16("/icons/netTables/invIcon.png"));
		invButton.setToolTipText("Shows invariants information table");
		invButton.setBounds(xPos, yPos, bWidth, bHeight);
		invButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createSimpleInvTable();
			}
		});
		invButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		upperButtonsPanel.add(invButton);
		yPos = yPos + bHeight + 5;
		
		
		buttonsPanel.add(upperButtonsPanel);
		
		//********************************************** INVARIANTS ****************************************************
		
		JPanel buttonsInvariantsPanel = new JPanel(null);
		buttonsInvariantsPanel.setBounds(0, upperButtonsPanel.getHeight(), 130, 220);
		buttonsInvariantsPanel.setBorder(BorderFactory.createTitledBorder("Invariants sim."));
		
		yPos = 20;
		xPos = 10;
		
		//JButton invariantsButton = createStandardButton("Show data", Tools.getResIcon32(""));
		JButton invariantsButton = new JButton("Show data");
		invariantsButton.setFocusPainted(false);
		invariantsButton.setMargin(new Insets(0, 0, 0, 0));
		invariantsButton.setIcon(Tools.getResIcon16("/icons/netTables/invIcon.png"));
		invariantsButton.setToolTipText("Show invariants table with transition firing rates.");
		invariantsButton.setBounds(xPos, yPos, bWidth, bHeight);
		invariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createInvariantsTable();
			}
		});
		invariantsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsInvariantsPanel.add(invariantsButton);
		
		JLabel ssLabel = new JLabel("State simulator");
		ssLabel.setBounds(xPos, yPos+=30, 110, 20);
		buttonsInvariantsPanel.add(ssLabel);
		
		JButton acqDataButton = new JButton("SimStart");
		acqDataButton.setFocusPainted(false);
		acqDataButton.setBounds(xPos, yPos+=20, 110, 25);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText("Compute new transitions firing statistics.");
		acqDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				String name = table.getName();
				if(name == null)
					return;
				else if(name.equals("InvariantsTable")) {
					createInvariantsTable();
				}
			}
		});
		buttonsInvariantsPanel.add(acqDataButton);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simStepsForInv, 0, 1000000, 10000);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(xPos, yPos+=30, 110, 25);
		simStepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				simStepsForInv = val;
			}
		});
		buttonsInvariantsPanel.add(simStepsSpinner);
		
		JLabel label0 = new JLabel("Net type:");
		label0.setBounds(xPos, yPos+=25, 80, 15);
		buttonsInvariantsPanel.add(label0);
		
		String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
		invSimNetModeCombo = new JComboBox<String>(simModeName);
		invSimNetModeCombo.setBounds(xPos, yPos+=15, 110, 25);
		invSimNetModeCombo.setSelectedIndex(0);
		invSimNetModeCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				int selectedModeIndex = invSimNetModeCombo.getSelectedIndex();
				selectedModeIndex = GUIManager.getDefaultGUIManager().simSettings.checkSimulatorNetType(selectedModeIndex);
				doNotUpdate = true;
				switch(selectedModeIndex) {
					case 0:
						invSimNetType = SimulatorGlobals.SimNetType.BASIC;
						invSimNetModeCombo.setSelectedIndex(0);
						break;
					case 1:
						invSimNetType = SimulatorGlobals.SimNetType.TIME;
						invSimNetModeCombo.setSelectedIndex(1);
						break;
					case 2:
						invSimNetType = SimulatorGlobals.SimNetType.HYBRID;
						invSimNetModeCombo.setSelectedIndex(2);
						break;
					case -1:
						invSimNetType = SimulatorGlobals.SimNetType.BASIC;
						invSimNetModeCombo.setSelectedIndex(1);
						GUIManager.getDefaultGUIManager().log("Error while changing simulator mode. Set for BASIC.", "error", true);
						break;
				}
				doNotUpdate = false;
			}
		});
		buttonsInvariantsPanel.add(invSimNetModeCombo);
		
		JLabel label1 = new JLabel("Submode:");
		label1.setBounds(xPos, yPos+=25, 80, 15);
		buttonsInvariantsPanel.add(label1);
		
		final JComboBox<String> simMode = new JComboBox<String>(new String[] {"50/50 mode", "Maximum mode", "Single mode"});
		simMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing,\n"
				+ "in single mode only one transition will fire.");
		simMode.setBounds(xPos, yPos+=15, 110, 25);
		simMode.setSelectedIndex(0);
		simMode.setMaximumRowCount(6);
		simMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = simMode.getSelectedIndex();
				if(selected == 0)
					maxModeForSSInv = false;
				else if(selected == 0)
					maxModeForSSInv = true;
				else
					singleModeForSSInv = true;
			}
		});
		buttonsInvariantsPanel.add(simMode);
		
		//TODO:
		
		JButton timeDataButton = new JButton("Time & Inv.");
		timeDataButton.setFocusPainted(false);
		timeDataButton.setBounds(xPos, yPos+=210, 110, 30);
		timeDataButton.setMargin(new Insets(0, 0, 0, 0));
		timeDataButton.setIcon(Tools.getResIcon22("/icons/stateSim/aaa.png"));
		timeDataButton.setToolTipText("Show time data for t-invariants");
		timeDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				action.showTimeDataNotepad();
			}
		});
		buttonsPanel.add(timeDataButton);

		//****
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
    	table.getColumnModel().getColumn(5).setMinWidth(70);
    	table.getColumnModel().getColumn(5).setMaxWidth(70);
    	
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
    
    /**
     * Metoda przygotowująca tabelę dla tranzycji.
     */
    private void createSimpleInvTable() { 
        modelBasicInvariants = new InvariantsTableModel();
        table.setModel(modelBasicInvariants);
        
        table.getColumnModel().getColumn(0).setHeaderValue("ID");
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
    	table.getColumnModel().getColumn(0).setMinWidth(40);
    	table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setHeaderValue("Tr.#");
        table.getColumnModel().getColumn(1).setPreferredWidth(40);
    	table.getColumnModel().getColumn(1).setMinWidth(40);
    	table.getColumnModel().getColumn(1).setMaxWidth(40);
        table.getColumnModel().getColumn(2).setHeaderValue("Min.");
        table.getColumnModel().getColumn(2).setPreferredWidth(50);
    	table.getColumnModel().getColumn(2).setMinWidth(50);
    	table.getColumnModel().getColumn(2).setMaxWidth(50);
        table.getColumnModel().getColumn(3).setHeaderValue("Feas.");
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
    	table.getColumnModel().getColumn(3).setMinWidth(50);
    	table.getColumnModel().getColumn(3).setMaxWidth(50);
    	table.getColumnModel().getColumn(4).setHeaderValue("pInT");
        table.getColumnModel().getColumn(4).setPreferredWidth(40);
    	table.getColumnModel().getColumn(4).setMinWidth(40);
    	table.getColumnModel().getColumn(4).setMaxWidth(40);
    	table.getColumnModel().getColumn(5).setHeaderValue("inT");
        table.getColumnModel().getColumn(5).setPreferredWidth(40);
    	table.getColumnModel().getColumn(5).setMinWidth(40);
    	table.getColumnModel().getColumn(5).setMaxWidth(40);
        table.getColumnModel().getColumn(6).setHeaderValue("outT");
        table.getColumnModel().getColumn(6).setPreferredWidth(40);
    	table.getColumnModel().getColumn(6).setMinWidth(40);
    	table.getColumnModel().getColumn(6).setMaxWidth(40);
    	table.getColumnModel().getColumn(7).setHeaderValue("r-Arc");
        table.getColumnModel().getColumn(7).setPreferredWidth(40);
    	table.getColumnModel().getColumn(7).setMinWidth(40);
    	table.getColumnModel().getColumn(7).setMaxWidth(40);
    	table.getColumnModel().getColumn(8).setHeaderValue("Inh.");
        table.getColumnModel().getColumn(8).setPreferredWidth(40);
    	table.getColumnModel().getColumn(8).setMinWidth(40);
    	table.getColumnModel().getColumn(8).setMaxWidth(40);
    	table.getColumnModel().getColumn(9).setHeaderValue("Sur.");
        table.getColumnModel().getColumn(9).setPreferredWidth(40);
    	table.getColumnModel().getColumn(9).setMinWidth(40);
    	table.getColumnModel().getColumn(9).setMaxWidth(40);
    	table.getColumnModel().getColumn(10).setHeaderValue("Sub.");
        table.getColumnModel().getColumn(10).setPreferredWidth(40);
    	table.getColumnModel().getColumn(10).setMinWidth(40);
    	table.getColumnModel().getColumn(10).setMaxWidth(40);
    	table.getColumnModel().getColumn(11).setHeaderValue("Cx=0");
        table.getColumnModel().getColumn(11).setPreferredWidth(40);
    	table.getColumnModel().getColumn(11).setMinWidth(40);
    	table.getColumnModel().getColumn(11).setMaxWidth(40);
    	table.getColumnModel().getColumn(12).setHeaderValue("Canonical");
        table.getColumnModel().getColumn(12).setPreferredWidth(40);
    	table.getColumnModel().getColumn(12).setMinWidth(40);
    	table.getColumnModel().getColumn(12).setMaxWidth(40);
    	table.getColumnModel().getColumn(13).setHeaderValue("Name");
        table.getColumnModel().getColumn(13).setPreferredWidth(100);
    	table.getColumnModel().getColumn(13).setMinWidth(100);
    	
    	
    	TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(sorter);
		
        table.setName("BasicInvTable");
        tableRenderer.setMode(3); //mode: basic invaritans
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);

        action.addBasicInvDataToModel(modelBasicInvariants); // metoda generująca dane o tranzycjach
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.validate();
	}
    
    /**
     * Metoda tworząca tablicę inwariantów.
     */
	private void createInvariantsTable() {
    	ArrayList<ArrayList<Integer>> invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
    	if(invariantsMatrix == null || invariantsMatrix.size() == 0) {
    		JOptionPane.showMessageDialog(this, "Please generate T-invariants (Elementary Modes)", "No invariants", JOptionPane.INFORMATION_MESSAGE);
    			return;
    	}
    	if(invariantsMatrix == null || invariantsMatrix.size() == 0) return; //final check
    	
    	int transNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
    	
    	modelInvariantsSimData = new InvariantsSimulatorTableModel(transNumber);        
        table.setModel(modelInvariantsSimData);
        
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
        }
        
        table.setName("InvariantsTable");
        tableRenderer.setMode(2); //mode: invariants
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);
		table.setRowSorter(null);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		action.addInvariantsToModel(modelInvariantsSimData, invariantsMatrix, simStepsForInv, maxModeForSSInv, singleModeForSSInv, invSimNetType);
		
		//ustawianie komentarzy dla kolumn:
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		JTableHeader header = table.getTableHeader();
		ArrayList<Transition> transition = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
	    for (int c = 2; c < table.getColumnCount(); c++) {
	    	TableColumn col = table.getColumnModel().getColumn(c);
	    	tips.setToolTip(col, "t"+(c-2)+"_"+transition.get(c-2).getName());
	    }
	    header.addMouseMotionListener(tips);
		
		resizeColumnWidth(table);
        //table.validate();
    }
	
	/**
	 * Metoda pomocnicza dla tablicy inwariantów, dostosowująca szerokość kolumn.
	 * @param table JTable - tablica danych
	 */
	public void resizeColumnWidth(JTable table) {
	    TableColumnModel columnModel = table.getColumnModel();
	    InvariantsSimulatorTableModel itm = (InvariantsSimulatorTableModel)table.getModel();
	    ArrayList<Integer> dTrans = itm.getZeroDeadTransitions();
	    for (int column = 2; column < table.getColumnCount(); column++) {
	        if(dTrans.get(column-2) == 0) {
	        	columnModel.getColumn(column).setPreferredWidth(35);
	        	columnModel.getColumn(column).setMinWidth(35);
	        	columnModel.getColumn(column).setMaxWidth(35);
	        } else if(dTrans.get(column-2) == -1) {
	        	columnModel.getColumn(column).setPreferredWidth(40);
	        	columnModel.getColumn(column).setMinWidth(40);
	        	columnModel.getColumn(column).setMaxWidth(40);
	        } else if(dTrans.get(column-2) == 1) {
	        	columnModel.getColumn(column).setPreferredWidth(50);
	        	columnModel.getColumn(column).setMinWidth(50);
	        	columnModel.getColumn(column).setMaxWidth(50);
	        }
	    }
	}
	
	/**
	 * Metoda pomocnicza do tworzenia przycisków do panelu bocznego.
	 * @param text String - tekst przycisku
	 * @param icon Icon - ikona
	 * @return JButton - nowy przycisk
	 */
	@SuppressWarnings("unused")
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
		
	/**
	 * Metoda interfejsu ComponentListener, odpowiada za dopasowanie rozmiaru paneli
	 * tabel i przycisków.
	 */
	//public void componentResized(ComponentEvent e) { resizeComponents(); }
	//public void componentHidden(ComponentEvent e) {} //unused
	//public void componentMoved(ComponentEvent e) {} //unused
	//public void componentShown(ComponentEvent e) {} //unused
	
	
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	
	/**
	 * Klasa wewnętrzna, służy do wyświetlania komentarz gdy kursor znajduje się nad nazwą kolumny
	 * danej tabeli danych.
	 * @author MR
	 *
	 */
	class ColumnHeaderToolTips extends MouseMotionAdapter {
		TableColumn curCol;
		Map<TableColumn, String> tips = new HashMap<TableColumn, String>();
		public void setToolTip(TableColumn col, String tooltip) {
			if (tooltip == null) {
				tips.remove(col);
			} else {
				tips.put(col, tooltip);
			}
		}
		
		public void mouseMoved(MouseEvent evt) {
			JTableHeader header = (JTableHeader) evt.getSource();
			JTable table = header.getTable();
			TableColumnModel colModel = table.getColumnModel();
			int vColIndex = colModel.getColumnIndexAtX(evt.getX());
			TableColumn col = null;
			if (vColIndex >= 0) {
				col = colModel.getColumn(vColIndex);
			}
			if (col != curCol) {
				header.setToolTipText((String) tips.get(col));
				curCol = col;
			}
		}
	}
}
