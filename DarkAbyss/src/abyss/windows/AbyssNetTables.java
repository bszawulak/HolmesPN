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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import abyss.darkgui.GUIManager;
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
	private JPanel rightSubPanel;
	private JPanel buttonsPanel;
	//data components:
	private JTable table;
	private DefaultTableModel model;
	private MyRenderer tableRenderer = new MyRenderer();
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
	 * Metoda pomocnicza tworząca panel boczny przycisków.
	 * @return JPanel - panel boczny przycisków
	 */
	private JPanel createButtonsPanel() {
		buttonsPanel = new JPanel(null);
		buttonsPanel.setBounds(650, 0, 140, 560);
		buttonsPanel.setBorder(BorderFactory.createTitledBorder("Buttons:"));
		
		int yPos = 20;
		int xPos = 20;
		int bWidth = 120;
		int bHeight = 30;
		
		JButton transitionsButton = createStandardButton("", Tools.getResIcon32("/icons/clustWindow/xxx.png"));
		transitionsButton.setToolTipText("    ");
		transitionsButton.setBounds(xPos, yPos, bWidth, bHeight);
		transitionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createPlacesTables();
			}
		});
		transitionsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(transitionsButton);
		buttonsPanel.add(Box.createVerticalStrut(7));
		
		yPos = yPos + bHeight + 5;
		
		JButton placesButton = createStandardButton("", Tools.getResIcon32("/icons/clustWindow/xxx.png"));
		placesButton.setToolTipText("    ");
		placesButton.setBounds(xPos, yPos, bWidth, bHeight);
		placesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				createTransitionTables();
			}
		});
		placesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(placesButton);
		buttonsPanel.add(Box.createVerticalStrut(7));
		
		yPos = yPos + bHeight + 15;
		
		return buttonsPanel;
	}

	/**
	 * Metoda tworząca panel główny okna służący do wyświetlania tabeli danych.
	 * @return JPanel - panel główny
	 */
	private JPanel createTablePanel() {
		rightSubPanel = new JPanel(new BorderLayout());
		rightSubPanel.setBounds(0, 0, 650, 560);
		rightSubPanel.setBorder(BorderFactory.createTitledBorder("Tables:"));
		
		createTableConstruct();

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		rightSubPanel.add(scrollPane, BorderLayout.CENTER);
		
		return rightSubPanel;
	}
	
	/**
	 * Metoda ta tworzy obiekty modelu i tabeli, inicjalizuje listenery tablicy.
	 */
	private void createTableConstruct() {
		model = new DefaultTableModel();
		table = new JTable(model);
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	action.cellClickAction(table);
          	    }
          	 }
      	});
	}
	
	/**
	 * Metoda tworząca tabelę miejsc sieci.
	 */
    private void createPlacesTables() {
    	model = new DefaultTableModel();
        model.addColumn("C01");
        model.addColumn("C02");
        model.addColumn("C03");
        model.addColumn("C04");
        model.addColumn("C05");
        model.addColumn("C06");
        
        table.setModel(model);
        
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
        table.getColumnModel().getColumn(5).setPreferredWidth(40);
    	table.getColumnModel().getColumn(5).setMinWidth(40);
    	table.getColumnModel().getColumn(5).setMaxWidth(40);
        
        table.setName("PlacesTable");
        tableRenderer.setMode(0); //mode: places
        
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);

        action.addPlacesToModel(model); // metoda generująca dane o miejscach
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.validate();
    }
    
    private void createTransitionTables() {
    	model = new DefaultTableModel();
        model.addColumn("C01");
        model.addColumn("C02");
        model.addColumn("C03");
        model.addColumn("C04");
        model.addColumn("C05");
        model.addColumn("C06");
        
        table.setModel(model);
        
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
        
        table.setName("TransitionTable");
        tableRenderer.setMode(1); //mode: transitions
        
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
       // TableColumnAdjuster tca = new TableColumnAdjuster(table);
       // tca.adjustColumns();
        
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        table.setDefaultRenderer(Object.class, tableRenderer);

        action.addTransitionsToModel(model); // metoda generująca dane o tranzycjach
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
        tmp.setFont(new Font("Arial", Font.PLAIN, 8));
    	resultButton.add(tmp, BorderLayout.PAGE_END);
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
		rightSubPanel.setLocation(0, 0);
		rightSubPanel.setSize(mainPanel.getWidth()-150, mainPanel.getHeight());
		buttonsPanel.setLocation(rightSubPanel.getWidth(), 0);
		buttonsPanel.setSize(150, mainPanel.getHeight());
	}
	
	
	//*************************************************************************************************************************
	//**************************************************               ********************************************************
	//**************************************************   t-RENDERER  ********************************************************
	//**************************************************               ********************************************************
	//*************************************************************************************************************************
	
	/**
     * Klasa wewnętrzna odpowiedzialna za rysowanie poszczególnych komórek.
     * @author MR
     *
     */
    class MyRenderer implements TableCellRenderer {
    	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
    	private int mode = 0; //0 -places
    	//private int subRows = 0;
    	
    	/**
    	 * Konstruktor domyślny obiektów klasy MyRenderer.
    	 */
    	public MyRenderer() {
    		
    	}
    	
    	/**
    	 * Konstruktor obiektów klasy MyRenderer przyjmujący numer trybu rysowania.
    	 * @param mode int - tryb rysowania
    	 */
    	public MyRenderer(int mode, int rows) {
    		this(); //wywołanie konstruktora domyślnego
    		this.mode = mode;
    		//this.subRows = rows;
    	}
    	
    	public void setSubRows(int rows) {
    		//this.subRows = rows;
    	}
    	
    	public void setMode(int mode) {
    		this.mode = mode;
    	}
    	
    	/**
    	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli.
    	 * @param table Jtable - 
    	 * @param value Object - 
    	 * @param isSelected boolean - 
    	 * @param hasFocus boolean - 
    	 * @param row int -
    	 * @param columnt int - 
    	 */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
        	if(mode == 0)
        		return paintCellsCase56(value, isSelected, hasFocus, row, column);
        	else
        		return paintCellsCase56(value, isSelected, hasFocus, row, column);
        }

        /**
         * Metoda trybu rysowania dla 56 klastrów, uruchamia się DLA KAŻDEJ komórki dodawanej do tabeli.
         * @param value Object - wartość do wpisania
         * @param isSelected boolean - czy komórka jest wybrana
         * @param hasFocus boolean - czy jest aktywna
         * @param row int - nr wiersza
         * @param column int - nr kolumny
         * @return Component - konkretnie: JTextField jako komórka tabeli
         */
		private Component paintCellsCase56(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);

		    
		    return renderer;
		}
    } // END CLASS MyRenderer IMPLEMENTS TableCellRenderer
}
