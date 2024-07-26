package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
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
 */
public class HolmesNetTables extends JFrame {
	@Serial
	private static final long serialVersionUID = 8429744762731301629L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private JComboBox<String> invSimNetModeCombo;
	private SimulatorGlobals.SimNetType invSimNetType = SimulatorGlobals.SimNetType.BASIC;
	private boolean doNotUpdate = false;
	//data components:
	private JTable table;
	private PTITableRenderer tableRenderer;
	public int currentClickedRow;
	private int simStepsForInv = 10000;
	private boolean maxModeForSSInv = false;
	private boolean singleModeForSSInv = false;
	
	private final HolmesNetTablesActions action;
	
	/**
	 * Główny konstruktor okna tabel sieci.
	 * @param parent JFrame - ramka okna głównego
	 */
	public HolmesNetTables(JFrame parent) {
		action  = new HolmesNetTablesActions(this);
		try {
			initialize_components();
			setVisible(false);
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00473exception")+" "+e.getMessage(), "error", true);
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
		setTitle(lang.getText("HNTwin_entry001title"));
    	try { setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png")); } 
    	catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00474exception")+" "+ex.getMessage(), "error", true);
		}
    	
    	setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(20, 20);
		
		setSize(new Dimension(900, 700));

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(createTablePanel(), BorderLayout.CENTER);
		mainPanel.add(createButtonsPanel(), BorderLayout.EAST);
		add(mainPanel);
	}
	
	/**
	 * Metoda ta tworzy obiekty modelu i tabeli, inicjalizuje listenery tablicy.
	 */
	private void createTableConstruct() {
		DefaultTableModel model = new DefaultTableModel();
		table = new JTable(model);
		tableRenderer = new PTITableRenderer(table);
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
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
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(670, 560));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Tables:"));
		
		createTableConstruct();

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		
		return tablesSubPanel;
	}

	/**
	 * Metoda pomocnicza tworząca panel boczny przycisków.
	 * @return JPanel - panel boczny przycisków
	 */
	@SuppressWarnings("ConstantConditions")
	private JPanel createButtonsPanel() {
		JPanel buttonsPanel = new JPanel(null);
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
		JButton placesButton = new JButton(lang.getText("HNTwin_entry002")); //Places
		placesButton.setFocusPainted(false);
		placesButton.setMargin(new Insets(0, 0, 0, 0));
		placesButton.setIcon(Tools.getResIcon16("/icons/netTables/placeIcon.png"));
		placesButton.setToolTipText(lang.getText("HNTwin_entry002t"));
		placesButton.setBounds(xPos, yPos, bWidth, bHeight);
		placesButton.addActionListener(actionEvent -> createPlacesTable());
		placesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		upperButtonsPanel.add(placesButton);
		yPos = yPos + bHeight + 5;
		
		//JButton transitionsButton = createStandardButton("Transitions", Tools.getResIcon32(""));
		JButton transitionsButton = new JButton(lang.getText("HNTwin_entry003")); //Transitions
		transitionsButton.setFocusPainted(false);
		transitionsButton.setMargin(new Insets(0, 0, 0, 0));
		transitionsButton.setIcon(Tools.getResIcon16("/icons/netTables/transIcon.png"));
		transitionsButton.setToolTipText(lang.getText("HNTwin_entry003t"));
		transitionsButton.setBounds(xPos, yPos, bWidth, bHeight);
		transitionsButton.addActionListener(actionEvent -> createTransitionTable());
		transitionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		upperButtonsPanel.add(transitionsButton);
		yPos = yPos + bHeight + 5;
		
		//JButton switchButton = createStandardButton("Switch P/T", Tools.getResIcon32(""));
		JButton switchButton = new JButton(lang.getText("HNTwin_entry004")); //Switch P/T
		switchButton.setFocusPainted(false);
		switchButton.setMargin(new Insets(0, 0, 0, 0));
		switchButton.setIcon(Tools.getResIcon16("/icons/netTables/switchIcon.png"));
		switchButton.setToolTipText(lang.getText("HNTwin_entry004t"));
		switchButton.setBounds(xPos, yPos, bWidth, 20);
		switchButton.addActionListener(actionEvent -> {
			boolean status = action.switchSelected(table);
			if(status) {
				if(table.getName().equals("PlacesTable")) {
					createPlacesTable();
				} else if(table.getName().equals("TransitionTable")) {
					createTransitionTable();
				}
			}
		});
		switchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		upperButtonsPanel.add(switchButton);
		
		yPos = yPos + bHeight - 5;
		
		//JButton invButton = createStandardButton("Invariants", Tools.getResIcon32(""));
		JButton invButton = new JButton(lang.getText("HNTwin_entry005")); //Invariants
		invButton.setFocusPainted(false);
		invButton.setMargin(new Insets(0, 0, 0, 0));
		invButton.setIcon(Tools.getResIcon16("/icons/netTables/invIcon.png"));
		invButton.setToolTipText(lang.getText("HNTwin_entry005t"));
		invButton.setBounds(xPos, yPos, bWidth, bHeight);
		invButton.addActionListener(actionEvent -> createSimpleInvTable());
		invButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		upperButtonsPanel.add(invButton);
		yPos = yPos + bHeight + 5;
		
		
		buttonsPanel.add(upperButtonsPanel);
		
		//********************************************** INVARIANTS ****************************************************
		
		JPanel buttonsInvariantsPanel = new JPanel(null);
		buttonsInvariantsPanel.setBounds(0, upperButtonsPanel.getHeight(), 130, 220);
		buttonsInvariantsPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HNTwin_entry006")));
		
		yPos = 20;
		xPos = 10;
		
		//JButton invariantsButton = createStandardButton("Show data", Tools.getResIcon32(""));
		JButton invariantsButton = new JButton(lang.getText("HNTwin_entry007")); //Show data
		invariantsButton.setFocusPainted(false);
		invariantsButton.setMargin(new Insets(0, 0, 0, 0));
		invariantsButton.setIcon(Tools.getResIcon16("/icons/netTables/invIcon.png"));
		invariantsButton.setToolTipText(lang.getText("HNTwin_entry007t"));
		invariantsButton.setBounds(xPos, yPos, bWidth, bHeight);
		invariantsButton.addActionListener(actionEvent -> createInvariantsTable());
		invariantsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsInvariantsPanel.add(invariantsButton);
		
		JLabel ssLabel = new JLabel(lang.getText("HNTwin_entry008")); //State simulator
		ssLabel.setBounds(xPos, yPos+=30, 110, 20);
		buttonsInvariantsPanel.add(ssLabel);
		
		JButton acqDataButton = new JButton(lang.getText("HNTwin_entry009")); //SimStart
		acqDataButton.setFocusPainted(false);
		acqDataButton.setBounds(xPos, yPos+=20, 110, 25);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText(lang.getText("HNTwin_entry009t"));
		acqDataButton.addActionListener(actionEvent -> {
			String name = table.getName();
			if(name == null)
				return;
			else if(name.equals("InvariantsTable")) {
				createInvariantsTable();
			}
		});
		buttonsInvariantsPanel.add(acqDataButton);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simStepsForInv, 0, 1000000, 10000);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(xPos, yPos+=30, 110, 25);
		simStepsSpinner.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			simStepsForInv = (int) spinner.getValue();
		});
		buttonsInvariantsPanel.add(simStepsSpinner);
		
		JLabel label0 = new JLabel(lang.getText("HNTwin_entry010")); //Net type
		label0.setBounds(xPos, yPos+=25, 80, 15);
		buttonsInvariantsPanel.add(label0);
		
		String[] simModeName = {lang.getText("HNTwin_entry011op1"), lang.getText("HNTwin_entry011op2"), lang.getText("HNTwin_entry011op3")}; //Petri Net, Timed Petri Net, Hybrid mode
		invSimNetModeCombo = new JComboBox<String>(simModeName);
		invSimNetModeCombo.setBounds(xPos, yPos+=15, 110, 25);
		invSimNetModeCombo.setSelectedIndex(0);
		invSimNetModeCombo.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			int selectedModeIndex = invSimNetModeCombo.getSelectedIndex();
			selectedModeIndex = overlord.simSettings.checkSimulatorNetType(selectedModeIndex);
			doNotUpdate = true;
			switch (selectedModeIndex) {
				case 0 -> {
					invSimNetType = SimulatorGlobals.SimNetType.BASIC;
					invSimNetModeCombo.setSelectedIndex(0);
				}
				case 1 -> {
					invSimNetType = SimulatorGlobals.SimNetType.TIME;
					invSimNetModeCombo.setSelectedIndex(1);
				}
				case 2 -> {
					invSimNetType = SimulatorGlobals.SimNetType.HYBRID;
					invSimNetModeCombo.setSelectedIndex(2);
				}
				case -1 -> {
					invSimNetType = SimulatorGlobals.SimNetType.BASIC;
					invSimNetModeCombo.setSelectedIndex(1);
					overlord.log(lang.getText("LOGentry00475"), "error", true);
				}
			}
			doNotUpdate = false;
		});
		buttonsInvariantsPanel.add(invSimNetModeCombo);
		
		JLabel label1 = new JLabel(lang.getText("HNTwin_entry012")); //Submode
		label1.setBounds(xPos, yPos+=25, 80, 15);
		buttonsInvariantsPanel.add(label1);
		
		final JComboBox<String> simMode = new JComboBox<String>(
				new String[] {lang.getText("HNTwin_entry013op1"), lang.getText("HNTwin_entry013op2"), lang.getText("HNTwin_entry013op3")});
		simMode.setToolTipText(lang.getText("HNTwin_entry013t"));
		simMode.setBounds(xPos, yPos+=15, 110, 25);
		simMode.setSelectedIndex(0);
		simMode.setMaximumRowCount(6);
		simMode.addActionListener(actionEvent -> {
			int selected = simMode.getSelectedIndex();
			if(selected == 0) {
				maxModeForSSInv = false;
			} else if(selected == 1) {
				maxModeForSSInv = true;
			} else {
				singleModeForSSInv = true;
			}
		});
		buttonsInvariantsPanel.add(simMode);
		
		//TODO:
		
		JButton timeDataButton = new JButton(lang.getText("HNTwin_entry014")); //Time & Inv.
		timeDataButton.setFocusPainted(false);
		timeDataButton.setBounds(xPos, yPos+=210, 110, 30);
		timeDataButton.setMargin(new Insets(0, 0, 0, 0));
		timeDataButton.setIcon(Tools.getResIcon22("/icons/stateSim/aaa.png"));
		timeDataButton.setToolTipText(lang.getText("HNTwin_entry014t"));
		timeDataButton.addActionListener(actionEvent -> action.showTimeDataNotepad());
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
		PlacesTableModel modelPlaces = new PlacesTableModel();
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
		TransitionsTableModel modelTransition = new TransitionsTableModel();
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
		InvariantsTableModel modelBasicInvariants = new InvariantsTableModel();
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
    	ArrayList<ArrayList<Integer>> invariantsMatrix = overlord.getWorkspace().getProject().getT_InvMatrix();
    	if(invariantsMatrix == null || invariantsMatrix.isEmpty()) {
    		JOptionPane.showMessageDialog(this, lang.getText("HNTwin_entry015"), lang.getText("HNTwin_entry016"), JOptionPane.INFORMATION_MESSAGE);
    			return;
    	}
    	//if(invariantsMatrix == null || invariantsMatrix.size() == 0) return; //final check
    	
    	int transNumber = overlord.getWorkspace().getProject().getTransitions().size();

		InvariantsSimulatorTableModel modelInvariantsSimData = new InvariantsSimulatorTableModel(transNumber);
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
		ArrayList<Transition> transition = overlord.getWorkspace().getProject().getTransitions();
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

	
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	//*********************************************************************************************************************
	
	/**
	 * Klasa wewnętrzna, służy do wyświetlania komentarz gdy kursor znajduje się nad nazwą kolumny
	 * danej tabeli danych.
	 */
	static class ColumnHeaderToolTips extends MouseMotionAdapter {
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
				header.setToolTipText(tips.get(col));
				curCol = col;
			}
		}
	}
}
