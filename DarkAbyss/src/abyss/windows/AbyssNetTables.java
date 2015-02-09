package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
import abyss.windows.AbyssClusters.MyRenderer;

public class AbyssNetTables extends JFrame implements ComponentListener {
	private static final long serialVersionUID = 8429744762731301629L;
	
	//interface components:
	private JFrame parentFrame;
	private JPanel mainPanel;
	private JPanel rightSubPanel;
	private JPanel buttonsPanel;
	//data components:
	private JTable table;
	private DefaultTableModel  model;
	private MyRenderer tabRenderer = new MyRenderer();
	
	/**
	 * Główny konstruktor okna tabel sieci.
	 * @param parent JFrame - ramka okna głównego
	 */
	public AbyssNetTables(JFrame parent) {
		parentFrame = parent;
		
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		
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
				createTablePanelCase565();
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
				createTablePanelCase56();
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
		
		
		model = new DefaultTableModel();
		table = new JTable(model);
		
		//createTablePanelCase56();
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		
		rightSubPanel.add(scrollPane, BorderLayout.CENTER);
		
		return rightSubPanel;
	}
	
    private void createTablePanelCase56() {
    	model = new DefaultTableModel();
        model.addColumn("Column1"); //miara odległości
        model.addColumn("Column2"); //zerowe klastry dla algorytmu klastrowania
        model.addColumn("Column3"); //MSS algorytmu
        model.addColumn("Column4"); //CH algorytmu
        table.setModel(model);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, tabRenderer); // 0 - case 56
        
        table.addMouseListener(new MouseAdapter() { //listener kliknięć
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	JTable target = (JTable)e.getSource();
          	    	int row = target.getSelectedRow();
          	    	int column = target.getSelectedColumn();
          	    	//cellClickedEvent(row, column);
          	    }
          	 }
      	});
        
        //rozmiary kolumn:
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        for(int index=0; index<table.getColumnCount(); index++) {
        	table.getColumnModel().getColumn(index).setPreferredWidth(50);
        	table.getColumnModel().getColumn(index).setMinWidth(50);
        	table.getColumnModel().getColumn(index).setMaxWidth(50);
        }
        
        String[] dataRow = { "a","b","b","b"};
        model.addRow(dataRow);
        table.validate();
    }
    
    private void createTablePanelCase565() {
    	model = new DefaultTableModel();
        model.addColumn("Column1"); //miara odległości
        model.addColumn("Column2"); //zerowe klastry dla algorytmu klastrowania
        model.addColumn("Column3"); //MSS algorytmu
        model.addColumn("Column4"); //CH algorytmu
        model.addColumn("Column5"); //CH algorytmu
        model.addColumn("Column6"); //CH algorytmu

        table.setModel(model);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, tabRenderer); // 0 - case 56
        
        table.addMouseListener(new MouseAdapter() { //listener kliknięć
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	JTable target = (JTable)e.getSource();
          	    	int row = target.getSelectedRow();
          	    	int column = target.getSelectedColumn();
          	    	//cellClickedEvent(row, column);
          	    }
          	 }
      	});
        //rozmiary kolumn:
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        for(int index=0; index<table.getColumnCount(); index++) {
        	table.getColumnModel().getColumn(index).setPreferredWidth(50);
        	table.getColumnModel().getColumn(index).setMinWidth(50);
        	table.getColumnModel().getColumn(index).setMaxWidth(50);
        }
        
        String[] dataRow = { "a","b","b","b"};
        model.addRow(dataRow);
        model.addRow(dataRow);
        model.addRow(dataRow);
        model.addRow(dataRow);
        model.addRow(dataRow);
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
		rightSubPanel.setLocation(0, 0);
		rightSubPanel.setSize(mainPanel.getWidth()-150, mainPanel.getHeight());
		buttonsPanel.setLocation(rightSubPanel.getWidth(), 0);
		buttonsPanel.setSize(150, mainPanel.getHeight());
	}
	public void componentHidden(ComponentEvent e) {} //unused
	public void componentMoved(ComponentEvent e) {} //unused
	public void componentShown(ComponentEvent e) {} //unused
	
	
	
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
    	private int mode = 0;
    	private int subRows = 0;
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
    		this.subRows = rows;
    	}
    	
    	public void setSubRows(int rows) {
    		this.subRows = rows;
    	}
    	
    	public void setMode(int mode) {
    		this.mode = mode;
    	}
    	
    	/**
    	 * Przeciążona metoda odpowiedzialna za zwrócenie komórki tabeli.
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
