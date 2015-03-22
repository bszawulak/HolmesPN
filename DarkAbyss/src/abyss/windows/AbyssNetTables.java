package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

import abyss.darkgui.GUIManager;
import abyss.math.Transition;
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
	private int simStepsForInv = 10000;
	private boolean maxModeForSSInv = false;
	
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
				setSize(new Dimension(800,600));
				resizeComponents();
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
				setSize(new Dimension(800,600));
				resizeComponents();
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
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
				double width = screenSize.getWidth();
				double height = screenSize.getHeight();
				setSize(new Dimension((int)width-50, (int)height-100));
				setLocation(20, 20);
				resizeComponents();
				
				createInvariantsTable();
			}
		});
		invariantsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsInvariantsPanel.add(invariantsButton);
		yPos += 30;
		
		JLabel ssLabel = new JLabel("StateSim:");
		ssLabel.setBounds(xPos, yPos, 80, 20);
		buttonsInvariantsPanel.add(ssLabel);
		yPos += 20;
		
		JButton acqDataButton = new JButton("SimStart");
		acqDataButton.setBounds(xPos, yPos, 110, 25);
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
		yPos += 30;
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(simStepsForInv, 0, 1000000, 10000);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(xPos, yPos, 110, 25);
		simStepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				simStepsForInv = val;
			}
		});
		buttonsInvariantsPanel.add(simStepsSpinner);
		yPos += 25;
		
		JLabel label1 = new JLabel("Mode:");
		label1.setBounds(xPos, yPos, 80, 15);
		buttonsInvariantsPanel.add(label1);
		yPos += 15;
		
		final JComboBox<String> simMode = new JComboBox<String>(new String[] {"Maximum mode", "50/50 mode"});
		simMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
		simMode.setBounds(xPos, yPos, 110, 25);
		simMode.setSelectedIndex(1);
		simMode.setMaximumRowCount(6);
		simMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = simMode.getSelectedIndex();
				if(selected == 0)
					maxModeForSSInv = true;
				else
					maxModeForSSInv = false;
			}
		});
		buttonsInvariantsPanel.add(simMode);

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
    
    /**
     * Metoda tworząca tablicę inwariantów.
     */
	private void createInvariantsTable() {
    	ArrayList<ArrayList<Integer>> invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
    	if(invariantsMatrix == null || invariantsMatrix.size() == 0) {
    		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().size() == 0) return;
    		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size() == 0) return;
    		
    		GUIManager.getDefaultGUIManager().io.generateINAinvariants();
    		invariantsMatrix = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
    	}
    	if(invariantsMatrix == null || invariantsMatrix.size() == 0) return; //final check
    	
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
            //table.getColumnModel().getColumn(i+2).setPreferredWidth(55);
        	//table.getColumnModel().getColumn(i+2).setMinWidth(55);
        	//table.getColumnModel().getColumn(i+2).setMaxWidth(55);
        }
        
        table.setName("InvariantsTable");
        tableRenderer.setMode(2); //mode: invariants
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);
		table.setRowSorter(null);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		action.addInvariantsToModel(modelInvariants, invariantsMatrix, simStepsForInv, maxModeForSSInv);
		
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
	    InvariantsTableModel itm = (InvariantsTableModel)table.getModel();
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
	
	/**
	 * Metoda odpowiedzialna za ustalenie nowych rozmiarów paneli okna.
	 */
	private void resizeComponents() {
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setSize(mainPanel.getWidth()-130, mainPanel.getHeight());
		tablesSubPanel.revalidate();
		tablesSubPanel.repaint();
		
		buttonsPanel.setLocation(tablesSubPanel.getWidth(), 0);
		buttonsPanel.setSize(130, mainPanel.getHeight());
	}
	
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
