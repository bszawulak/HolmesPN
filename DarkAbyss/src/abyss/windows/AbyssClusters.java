package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import abyss.clusters.Clustering;
import abyss.clusters.ClusteringInfoMatrix;
import abyss.files.clusters.CHmetricReader;
import abyss.files.clusters.ClusterReader;
import abyss.files.clusters.RClusteringParserToXLS;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;
import abyss.darkgui.GUIManager;

/**
 * Klasa obs�uguj�ca okno klastr�w dla danej sieci. Poza tym mn�stwo przycisk�w przeznaczonych
 * do wykonywania operacji na klastrach: generowanie, wczytywanie, eksport do plik�w (tak�e
 * Excel). Wywo�uje mniejsze okno informacji o klastrowaniu po klikni�ciu na odpowiedni�
 * kom�rk� tabeli.
 * @author MR
 *
 */
public class AbyssClusters extends JFrame {
	private static final long serialVersionUID = -8420712475473581772L;
	//private static final long serialVersionUID = 6942814230861358341L;
	private JTable table;
	private DefaultTableModel  model;
	private int subRowsSize = 0;
    private final AbyssClusters myself;
    private int clustersToGenerate = 0;
    private SpinnerModel spinnerClustersModel;
    private JSpinner spinnerClusters;
    
    private int mode = 0; // 0 - tryb 56 klastrowa�
    private MyRenderer tabRenderer = new MyRenderer(mode, 18);
    
    private ClusteringInfoMatrix dataTableCase56 = null;
    private String pathCSVfile = "";
    private String pathClustersDir = "";
    private String pathCHmetricsDir = "";
    
    /**
     * Konstruktor domy�lny obiektu okna klasy AbyssClusters. Tworzy wszystkie elementy okna
     * z tabel� klastr�w.
     */
    public AbyssClusters() {
    	myself = this;
    	this.setTitle("Abyss Cluster Window");
    	clustersToGenerate = 20;	
    	initiateListeners();

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(20, 20);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setMinimumSize(new Dimension(1000, screenSize.height-100));
		setMaximumSize(new Dimension(1000, screenSize.height-100));
		setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
        
        JPanel tablePanel; // = new JPanel();
        if(mode==0)
        	tablePanel = createTablePanelCase56(); // !!!
        else
        	tablePanel = new JPanel();
        
        tablePanel.setOpaque(true); 
        mainPanel.add(tablePanel, gbc);
        
        JPanel textPanel = createButtonsPanelCase56();

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        mainPanel.add(textPanel, gbc);
        
        this.setContentPane(mainPanel);
        this.pack();
        this.setVisible(false);
    }

    /**
     * Metoda zwracaj�ca panel boczny dla g��wnego okna, wype�niony przyciskami.
     * @return JPanel - obiekt panelu bocznego
     */
	private JPanel createButtonsPanelCase56() {
		JPanel textPanel = new JPanel();
		textPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
		
		JLabel clLabel = new JLabel("Generate clusters:");
		clLabel.setFont(new Font("Arial", Font.BOLD, 11));
		clLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		textPanel.add(clLabel);	
		
		// Komponent okre�lenie g�rnego limitu klastr�w dla oblicze�
		spinnerClustersModel = new SpinnerNumberModel(20, 2, 300, 1);
		clustersToGenerate = 20;
		spinnerClusters = new JSpinner(spinnerClustersModel);
		spinnerClusters.setPreferredSize(new Dimension(100, 30));
		spinnerClusters.setMinimumSize(new Dimension(100, 30));
		spinnerClusters.setMaximumSize(new Dimension(100, 30));
		//spinnerClusters.setEnabled(false);
		spinnerClusters.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				clustersToGenerate = (int) spinner.getValue();
			}
		});
		spinnerClusters.setAlignmentX(Component.CENTER_ALIGNMENT);
		textPanel.add(spinnerClusters);	

		// Przycisk rozpocz�cia procedury generowania klastr�w na bazie inwariant�w
		JButton generateButton = createStandardButton("", 
				Tools.getResIcon48("/icons/clustWindow/buttonGen56.png"));
		generateButton.setToolTipText("Generate all 56 clusterings for a selected number of clusters.");
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				buttonGenerateClusterings();
			}
		});
		generateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(generateButton);
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk rozpocz�cia procedury generowania metryk Celi�skiego-Harabasza dla tabeli klastrowa�
     	JButton generateCHButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonComputeCH.png"));
     	generateCHButton.setToolTipText("Compute Cali�ski-Harabasz metrics for a given number of clusters.");
     	generateCHButton.addActionListener(new ActionListener() {
     		public void actionPerformed(ActionEvent actionEvent) {
     			buttonComputeCHmetrics();
     		}
     	});
     	generateCHButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(generateCHButton);
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk wczytania katalogu z 56 klastrowaniami
        JButton case56Button = createStandardButton("", 
        		Tools.getResIcon48("/icons/clustWindow/buttonLoadClusterDir.png"));
        case56Button.setToolTipText("Load 56 clusterings into table from the selected directory.");
        case56Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				buttonLoadClusteringDirectory();
			}
		});
        case56Button.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(case56Button);
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk wczytywania metryk C-H dla tabeli
     	JButton loadCHButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonLoadCHDir.png"));
     	loadCHButton.setToolTipText("Load Cali�ski-Harabasz metrics from the selected directory.");
     	loadCHButton.addActionListener(new ActionListener() {
     		public void actionPerformed(ActionEvent actionEvent) {
     			buttonLoadCHmetricIntoTables();
     		}
     	});
     	loadCHButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(loadCHButton);
     	textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk zapisu tabeli danych
     	JButton saveTableButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonSaveTable.png"));
     	saveTableButton.setToolTipText("Save table data into the selected file");
     	saveTableButton.addActionListener(new ActionListener() {
     		public void actionPerformed(ActionEvent actionEvent) {
     			buttonSerializeDataTable();
     		}
     	});
     	saveTableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(saveTableButton);
     	textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk zapisu tabeli danych
     	JButton loadTableButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonLoadTable.png"));
     	loadTableButton.setToolTipText("Load table data from the selected file");
     	loadTableButton.addActionListener(new ActionListener() {
     		public void actionPerformed(ActionEvent actionEvent) {
     			buttonDeserializeFile();
     		}
     	});
     	loadTableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(loadTableButton);
     	textPanel.add(Box.createVerticalStrut(7));
     	
     	// Przycisk exportu tabeli danych do excela
        JButton excelExport = createStandardButton("", 
        		Tools.getResIcon48("/icons/clustWindow/buttonExportToExcel.png"));
        excelExport.setToolTipText("Export results files into Excel document.");
        excelExport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent actionEvent) {
 				buttonExportTableToExcel();
 			}
 		});
        excelExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(excelExport);
        textPanel.add(Box.createVerticalStrut(7));
        
		return textPanel;
	}
	
	/**
	 * Metoda pomocnicza do tworzenia przycisk�w do panelu bocznego.
	 * @param text String - tekst przycisku
	 * @param icon Icon - ikona
	 * @return JButton - nowy przycisk
	 */
	private JButton createStandardButton(String text, Icon icon) {
		JButton resultButton = new JButton(); 
        //resultButton.setLayout(new BoxLayout(resultButton,BoxLayout.Y_AXIS));
		resultButton.setLayout(new BorderLayout());
        
        JLabel tmp;
        tmp = new JLabel(text);
        tmp.setFont(new Font("Arial", Font.PLAIN, 8));
    	resultButton.add(tmp, BorderLayout.PAGE_END);
        /*
        for(int i=0; i<text.length; i++) {
        	tmp = new JLabel(text[i]);
        	tmp.setFont(new Font("Arial", Font.PLAIN, 8));
        	tmp.setAlignmentX(Component.TOP_ALIGNMENT);
        	resultButton.add(tmp);
        }
        */
		   
        resultButton.setPreferredSize(new Dimension(100, 60));
        resultButton.setMinimumSize(new Dimension(100, 60));
        resultButton.setMaximumSize(new Dimension(100, 60));
        resultButton.setIcon(icon);
		return resultButton;
	}
    
	/**
	 * Metoda ustawia tryb pracy okna.
	 * @param mode int: 0 - tabela 56-u podtabel
	 */
    public AbyssClusters(int mode) {
    	this();
    	this.mode = mode;
    }
    
    /**
     * Metoda ustawia �cie�k� dost�pu do katalogu klastr�w.
     * @param path String - �cie�ka do katalogu
     */
    public void setClusterPath(String path) {
    	pathClustersDir = path;
    }
    
    /**
     * Metoda zwraca �cie�k� dost�pu do katalogu klastr�w.
     * @return String - �cie�ka do katalogu
     */
    public String getClusterPath() {
    	return pathClustersDir;
    }
    
    /**
     * Metoda pomocnicza konstruktora, tworzy panel z tabel� danych na 15 kolumn.
     * 7 x 8 = 56 podtabel klastrowa�, standardowy przypadek.
     * @return JPanel - panel zawieraj�cy tabel� danych
     */
    private JPanel createTablePanelCase56() {
    	JPanel main = new JPanel();
    	main.setLayout(new BorderLayout());

    	model = new DefaultTableModel();
        model.addColumn("Column1"); //miara odleg�o�ci
        model.addColumn("Column2"); //zerowe klastry dla algorytmu klastrowania
        model.addColumn("Column3"); //MSS algorytmu
        model.addColumn("Column4"); //CH algorytmu
        model.addColumn("Column5"); //kolejna tr�jka jak dla 2-4, itd,
        model.addColumn("Column6");
        model.addColumn("Column7");
        model.addColumn("Column8"); //kolejna tr�jka jak dla 2-4, itd,
        model.addColumn("Column9");
        model.addColumn("Column10");
        model.addColumn("Column11");
        model.addColumn("Column12");
        model.addColumn("Column13");
        model.addColumn("Column14");
        model.addColumn("Column15");
        model.addColumn("Column16");
        model.addColumn("Column17");
        model.addColumn("Column18");
        model.addColumn("Column19");
        model.addColumn("Column20");
        model.addColumn("Column21");
        model.addColumn("Column22");
        
       
        
        table = new JTable(model);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, tabRenderer); // 0 - case 56
        
        table.addMouseListener(new MouseAdapter() { //listener klikni��
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	JTable target = (JTable)e.getSource();
          	    	int row = target.getSelectedRow();
          	    	int column = target.getSelectedColumn();
          	    	
          	    	cellClickedEvent(row, column);
          	    }
          	 }
      	});
        
       
        table.getColumnModel().getColumn(0).setHeaderValue("Metric:");
        table.getColumnModel().getColumn(1).setHeaderValue("");
        table.getColumnModel().getColumn(2).setHeaderValue("UPGMA");
        table.getColumnModel().getColumn(3).setHeaderValue("");
        table.getColumnModel().getColumn(4).setHeaderValue("");
        table.getColumnModel().getColumn(5).setHeaderValue("Centroid");
        table.getColumnModel().getColumn(6).setHeaderValue("");
        table.getColumnModel().getColumn(7).setHeaderValue("");
        table.getColumnModel().getColumn(8).setHeaderValue("Complete");
        table.getColumnModel().getColumn(9).setHeaderValue("");
        table.getColumnModel().getColumn(10).setHeaderValue("");
        table.getColumnModel().getColumn(11).setHeaderValue("McQuitty");
        table.getColumnModel().getColumn(12).setHeaderValue("");
        table.getColumnModel().getColumn(13).setHeaderValue("");
        table.getColumnModel().getColumn(14).setHeaderValue("Median");
        table.getColumnModel().getColumn(15).setHeaderValue("");
        table.getColumnModel().getColumn(16).setHeaderValue("");
        table.getColumnModel().getColumn(17).setHeaderValue("Single");
        table.getColumnModel().getColumn(18).setHeaderValue("");
        table.getColumnModel().getColumn(19).setHeaderValue("");
        table.getColumnModel().getColumn(20).setHeaderValue("Ward");
        table.getColumnModel().getColumn(21).setHeaderValue("");
        
        //rozmiary kolumn:
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        for(int index=1; index<table.getColumnCount(); index++) {
        	if((index+2) % 3 == 0) { //tr�jka
        		table.getColumnModel().getColumn(index).setPreferredWidth(20);
        		table.getColumnModel().getColumn(index+1).setPreferredWidth(50);
        		table.getColumnModel().getColumn(index+2).setPreferredWidth(50);
        		index++;
        		index++;
        	} else {
        		//table.getColumnModel().getColumn(index).setPreferredWidth(20);
        	}
        }

        JScrollPane scrollPane = new JScrollPane(table);
        main.add(scrollPane);
        return main;
    }
    
    /**
     * Obs�uga zdarzenia klikni�cia na kom�rk�. Metoda przelicza lokalizacj� kom�rki na lokalizacj�
     * w tabeli danych. Nast�pnie wywo�uje podokno informacyjne.
     * @param row int - nr klikni�tego wiersza
     * @param column int - nr klikni�tej kom�rki
     */
    private void cellClickedEvent(int row, int column) {
		int sub = subRowsSize;
		if(column != 0 && row % (sub+1) != 0) { // NIE dla I kolumny i wierszy nag��wkowych
			try {
				//tutaj dzieje si� magia na liczbach - jednako� dzieje si� prawid�owo, dlatego
				//lepiej tutaj niczego nie zmienia� we wzorach
				int clusterNumber = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
				int algID = (int)((column-1) / 3); //1,2,3 -> 0; 4,5,6->1, itd.
				
				//obliczanie numery wiersza nag��wego nad klikni�tym wierszem:
				int headerRowNumber = row - (clusterNumber-1);
				//zmiana na nr miary, pierwszy blok, to miara 0, nast�pny 1, itd. a� do 7
				headerRowNumber /= (subRowsSize+1);
				
				Clustering omg = dataTableCase56.getClustering((headerRowNumber*7)+algID, clusterNumber-2);
				new AbyssClusterSubWindow(myself, omg, 1);
				
			 } catch (Exception ex) {
				  
			 }
			 
		 }
	}
    /*
    protected void changeCellsInRowCase56(TableModelEvent e) {
    	try {
    		DefaultTableModel m = (DefaultTableModel) e.getSource();
    		int rowNumber = m.getDataVector().size();
    		for (int col = 0; col < 15; col++) {
    			table.getModel().getValueAt(rowNumber-1, col);
			}
    	} catch (ClassCastException exc) {
			
		}
	}
	*/
    
    /**
     * Metoda s�u��ca do wype�nienia tabeli w przypadku kiedy mamy 56 klastrowa�.
     * @param newTable ClusteringInfoMatrix - g��wna baza-tabela danych
     */
	public void registerDataCase56(ClusteringInfoMatrix newTable) {
		if(newTable == null) return; //you no take candle!
		
    	dataTableCase56 = newTable;
    	subRowsSize = newTable.secondaryTablesMinNumber;
    	int checkSize = newTable.mainTablesNumber;
    	if(checkSize != 56) {
    		GUIManager.getDefaultGUIManager().log("Invalid number of subtables. Operation terminated.", "error", true);
    		return;
    	} 
    	
    	GUIManager.getDefaultGUIManager().log("Clearing old clusterings data table", "text", true);
    	
    	tabRenderer.setMode(mode);  // !!!
    	tabRenderer.setSubRows(subRowsSize); // !!! z�a warto�� i tabela idzie w ....
    	//CLEAR OLD TABLE ROWS:
    	model.setNumRows(0);
    	table.revalidate();
    	
    	String[] metricName = { "Correlation", "Pearson", "Binary", "Canberra", "Euclidean", "Manhattan", "Maximum", "Minkowski" };
    	
    	for(int metric=0; metric <8; metric++) { //dla ka�dej z o�miu metryk:
    		String[] data = { metricName[metric],"0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H"};
			model.addRow(data);
			
    		for(int rows=0; rows < subRowsSize; rows++) { //dla odpowiedniej liczby wierszy:
    			String[] dataRow = { "","","","","","","","","","","","","","","","","","","","","",""}; //22 element�w
    			dataRow[0] = ""+(rows+2);
    			for(int alg=0; alg < 7; alg++ ) { // dla ka�dego wiersza jedziemy po algorytmach
    				// Average, Centroid, Complete, McQuitty, Median, Single, Ward
        			int tableIndex = (metric*7)+alg; //kt�ra tabelka
            		
        			dataRow[1+alg*3] = ""+dataTableCase56.getMatrix().get(tableIndex).get(rows).zeroClusters;
        			
        			Double val = dataTableCase56.getMatrix().get(tableIndex).get(rows).evalMSS;
        			String cuttedValue = cutValueMSS(val);
        			dataRow[1+alg*3+1] = ""+cuttedValue;
        			
        			Double val2 = dataTableCase56.getMatrix().get(tableIndex).get(rows).evalCH;
        			String cuttedValue2 = cutValueCH(val2);
        			dataRow[1+alg*3+2] = ""+cuttedValue2;
            	}
    			model.addRow(dataRow);
			}
    	}
    	GUIManager.getDefaultGUIManager().log("New clustering data table has been successfully read.", "text", true);
    }
    
    private String cutValueMSS(double evalMSS) {
    	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(getLocale());
    	otherSymbols.setDecimalSeparator('.');
    	//otherSymbols.setGroupingSeparator('.'); 
    	DecimalFormat df = new DecimalFormat("#.####", otherSymbols);
    	//DecimalFormat df = new DecimalFormat("#.####");
        
		return df.format(evalMSS);
	}
    
    private String cutValueCH(double evalMSS) {
    	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(getLocale());
    	otherSymbols.setDecimalSeparator('.');
    	//otherSymbols.setGroupingSeparator('.'); 
    	DecimalFormat df = new DecimalFormat("#.#", otherSymbols);
    	//DecimalFormat df = new DecimalFormat("#.####");
        
		return df.format(evalMSS);
	}

    public Color getColor(double power)
	{
	    //double H = power * 0.4; // Hue (note 0.4 = Green, see huge chart below)
	    //double S = 0.9; // Saturation
	    //double B = 0.9; // Brightness
	    //return Color.getHSBColor((float)H, (float)S, (float)B);
		double R = (255 * power) / 100;
		double G = (255 * (100 - power)) / 100; 
		double B = 0;
		try {
			return new Color((int)R,(int)G,(int)B);
		} catch (Exception e) {
			return Color.white;
		}
	}
    
    //**************************************************************************************************
    //**************************************           *************************************************
    //**************************************  BUTTONS  *************************************************
    //**************************************           *************************************************
    //**************************************************************************************************
    
    /**
     * Metoda realizuje generowanie klastrowa� dla inwariant�w z sieci. Inwarianty b�d� 
     * w formie pliku CSV, kt�ry b�dzie utworzony automatycznie, tak wi�c sie� i inwarianty
     * musz� ju� istnie� w programie. W jej efekcie powstaje katalog z klastrowaniami.
     */
    private void buttonGenerateClusterings() {
		if(clustersToGenerate > 1) {
			pathCSVfile = GUIManager.getDefaultGUIManager().generateClustersCase56(clustersToGenerate);
			if(pathCSVfile == null) { //je�li co� si� nie uda�o
				//pathClustersDir = ""; //�cie�ka do katalogu klastrowa�
				pathCSVfile = ""; //�cie�ka do pliku CSV
			} else {
				JOptionPane.showMessageDialog(null, "Clustering procedure for all cases initiated. This make take so time to finish.", 
						"R computation initiated", JOptionPane.INFORMATION_MESSAGE);
				//pathClustersDir = new File(pathCSVfile).getParent(); //uzyskanie katalogu z klastrowaniami
				//przy czym tam wci�� pewnie trwa praca (dopiero si� zacz�a) tworzenia w osobnym
				//w�tku plik�w klastrowa� poprzez skrypty R
			}
		}
	}
    
    /**
     * Metoda odpowiada za utworzenie tabeli danych i jej wy�wietlenie. Tabela b�dzie
     * tworzona na podstawie danych ze wskazanego katalogu z wygenerowanymi plikami
     * klastrowa�.
     */
    private void buttonLoadClusteringDirectory() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		String lastPath2 = pathClustersDir;
		String chosenPath="";
		if(lastPath2.equals(""))
			chosenPath = lastPath;
		else
			chosenPath = lastPath2;
		
		String choosenDir = Tools.selectDirectoryDialog(chosenPath, "Select cluster dir",
					"Directory with 56 generated text R-clusters files.");
		if(choosenDir.equals(""))
			return;
		
		ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
		int result = clusterMatrix.readDataDirectory(choosenDir);
		if(result == -1) {
			JOptionPane.showMessageDialog(null, "Cluster reading failed. Possible wrong directory chosen.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		} else {
			registerDataCase56(clusterMatrix);
			pathClustersDir = choosenDir;
		}
		
		//czy plik startowy jest na miejscu?
		pathCSVfile = choosenDir+"/cluster.csv";
		File csv = new File(pathCSVfile);
		if(csv.exists() == false) {
			String msg = "Selected directory does not contain cluster.csv file. Further calculations may not be possible.";
			JOptionPane.showMessageDialog(null, "msg", "warning",JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log(msg, "warning", true);
		}
		
		// test / debug
		/*
		ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
		int result = clusterMatrix.readDataDirectory("tmp//IL18C40");
		setClusterPath("tmp//IL18C40");
		if(result == -1) {
			JOptionPane.showMessageDialog(null, "Cluster reading failed. Possible wrong directory chosen.", "Error",JOptionPane.ERROR_MESSAGE);
		} else {
			handleStandardClusterTableCase56(clusterMatrix);
		}
		*/
	}
    
    /**
     * Metoda odpowiedzialna za utworzenie dokumentu w formacie .xls programu Excel
     * wy�wietlanej w oknie tabeli danych o klastrowaniach.
     */
    private void buttonExportTableToExcel() {
		try{
			//String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
			GUIManager.getDefaultGUIManager().log("Attempting to export cluster table to excel",
					"text", true);
			
			String dirPath = Tools.selectDirectoryDialog(pathClustersDir, "Select cluster dir",
					"Directory with 56 generated R-clusters text files.");
			if(dirPath.equals("")) { // czy wskazano cokolwiek
				return;
			} 
			
			//sprawdzi� czy s� wszystkie pliki / odtworzy� if necessary
			ClusterReader cr = new ClusterReader();
			if(cr.checkFiles(dirPath) == -2) { //no cluster files
				JOptionPane.showMessageDialog(null, "Directory does not contain a single cluster file.",
						"Error",JOptionPane.ERROR_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Directory: "+dirPath+ 
						"does not contain even a single cluster file.", "error", true);
			}
			
			RClusteringParserToXLS r = new abyss.files.clusters.RClusteringParserToXLS();
			r.extractAllRClusteringToXLS(dirPath, dirPath+"//ClustersSummary.xls");
			
			File test = new File(dirPath+"//ClustersSummary.xls");
			if(test.exists()) {
				FileFilter filter[] = new FileFilter[1];
				filter[0] = new ExtensionFileFilter(".xls - Excel 2003",  new String[] { "XLS" });
				String newLocation = Tools.selectFileDialog(dirPath, filter, "", "");
				if(newLocation.equals("")) { //czy chcemy przenie�� plik w inne miejsce
					//leave it in cluster folder
					GUIManager.getDefaultGUIManager().log("Exporting table succeed. Created file: "
							+dirPath+"//ClustersSummary.xls", "text", true);
				} else {
					if(!newLocation.contains(".xls"))
						newLocation += ".xls";
					Tools.copyFileByPath(dirPath+"//ClustersSummary.xls", newLocation);
					test.delete(); //kasujemy oryginalny
					GUIManager.getDefaultGUIManager().log("Exporting table succeed. Created file: "
							+newLocation, "text", true);
				}
				
			} else {
				String msg = "Unknown error, excel file does not exist.";
				GUIManager.getDefaultGUIManager().log(msg, "error", true);
			}
		} catch (Exception e){
			String msg = "Excel export procedure failed for directory: "+pathClustersDir;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			GUIManager.getDefaultGUIManager().log(e.getMessage(), "error", true);
		}
	}
    
    /**
     * Metoda odpowiedzialna za wygenerowanie metryk Celi�skiego-Harabasza dla
     * zadanego limitu liczby klastr�w.
     */
    private void buttonComputeCHmetrics() {
		if(clustersToGenerate > 1) {
			String newCHpath = GUIManager.getDefaultGUIManager().generateAllCHindexes(clustersToGenerate);
			if(newCHpath != null) //je�li co� si� sta�o si�... :)
				pathCHmetricsDir = newCHpath;
			//uwaga! w powy�szym katalogu miary dopiero powstaj�!
		}
	}
    
    /**
     * Obs�uga wczytywania miar Celi�skiego-Harabasz do tabeli g��wnej.
     */
	private void buttonLoadCHmetricIntoTables() {
		if(dataTableCase56 == null) {
			JOptionPane.showMessageDialog(null,"There is no table to fill.","Warning",JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		String dirPath = Tools.selectDirectoryDialog(pathCHmetricsDir, "Select directory",
				"Directory with 56 R-generated files containing Celi�ski-Harabasz metrics");
		if(dirPath.equals("")) { // czy wskazano cokolwiek
			return;
		} 
		CHmetricReader chReader = new CHmetricReader();
		ArrayList<ArrayList<Double>> chDataCore = chReader.readCHmetricsDirectory(dirPath);
		
		integrateCHIntoDatabase(chDataCore);
	}
	
	/**
	 * Metoda wczytuje miary Celi�skiego-Harabasz do tabeli g��wnej. Tak si� mi�o sk�ada, �e
	 * indeksy podtabel (algorytm-miara) s� te same, bo pliki w obu przypadkach (klastrowanie 
	 * oraz miary C-H) by�y czytane w tym samym porz�dku. A je�li si� nie da�o, to w og�le nie
	 * by�y przeczytane, wi�c i tak nie ma co wczytywa� :)
	 * @param chDataCore ArrayList[ArrayList[Double]] - metryka CH
	 */
	private void integrateCHIntoDatabase(ArrayList<ArrayList<Double>> chDataCore) {
		int size1Order = dataTableCase56.getMatrix().size();
		int sizeCH = chDataCore.size();
		
		if(size1Order != sizeCH || size1Order != 56)
			return;
		
		int clusters = dataTableCase56.getMatrix().get(0).size(); //bo od 2 do cNumber
		
		
		for(int i=0; i<size1Order; i++) {
			for(int cl=0; cl<clusters; cl++) { //od 2 do limitu w tabeli
				try {
					dataTableCase56.getMatrix().get(i).get(cl).evalCH = chDataCore.get(i).get(cl);
				} catch (Exception e) {
					GUIManager.getDefaultGUIManager().log("Filling CH metric failed for subtable "+i+" row: "+cl, "error", false);
				}
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za zapis tabeli danych do pliku, za pomoc� mechanizmu
	 * serializacji.
	 */
	private void buttonSerializeDataTable() {
		try{
			FileFilter filter[] = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Abyss CLustering file (.acl)",  new String[] { "acl" });
			String newLocation = Tools.selectFileDialog(pathClustersDir, filter, "", "");
			if(newLocation.equals(""))
				return;
			
			if(!newLocation.contains(".acl"))
				newLocation += ".acl";

			FileOutputStream fos= new FileOutputStream(newLocation);
			ObjectOutputStream oos= new ObjectOutputStream(fos);
			oos.writeObject(dataTableCase56);
			oos.close();
			fos.close();
		} catch(IOException ioe){
			GUIManager.getDefaultGUIManager().log("Saving data table failed.", "error", false);
		    //ioe.printStackTrace();
		}
	}
	
	/**
	 * Metoda odpowiedzialna za odczyt pliku z zserializowanymi danymi obiekt�w 
	 * tablicy danych do... tablicy danych. Makes sense actually.
	 */
	private void buttonDeserializeFile() {
		ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
		String newLocation = "";
		try
		{
			FileFilter filter[] = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Abyss CLustering file (.acl)",  new String[] { "acl" });
			newLocation = Tools.selectFileDialog(pathClustersDir, filter, "", "");
			if(newLocation.equals("")) 
				return;
			
			File test = new File(newLocation);
			if(!test.exists()) 
				return;
			
			FileInputStream fis = new FileInputStream(newLocation);
			ObjectInputStream ois = new ObjectInputStream(fis);
			clusterMatrix = (ClusteringInfoMatrix) ois.readObject();
			ois.close();
			fis.close();
			registerDataCase56(clusterMatrix);
		} catch(Exception ioe){
			String msg = "Program was unable to load data table from "+newLocation;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			return;
		} 
	}
    
	/**
	 * Inicjalizacja agent�w nas�uchuj�cych r�nych zdarze� dla okna klastrowa�.
	 */
    private void initiateListeners() {
    	addWindowListener(new WindowAdapter() {
    		public void windowOpened(WindowEvent e) {}
  	    	public void windowClosing(WindowEvent e) {}
  	  	    public void windowClosed(WindowEvent e) {}
  	  	    public void windowIconified(WindowEvent e) {}
  	  	    public void windowDeiconified(WindowEvent e) {}
  	  	    public void windowDeactivated(WindowEvent e) {}
	  	    public void windowStateChanged(WindowEvent e) {}
	  	    public void windowGainedFocus(WindowEvent e) {}
	  	    public void windowLostFocus(WindowEvent e) {}
	  	    
	  	    /**
	  	     * Kiedy okno staje si� aktywne
	  	     */
  	  	    public void windowActivated(WindowEvent e) {
  	  	    /*
  	  	    	try {
  	  	    		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix() != null) {
  	  	    			
  	  	    			if(spinnerBlocked) {
  	  	    				spinnerBlocked=false;
  	  	    			} else {
  	  	    				return;
  	  	    			}
  	  	    			int invNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix().size();
  	  	    			int currentValue = 20;
  	  	    			if(invNumber < currentValue)
  	  	    				currentValue = invNumber;

  	  	    			int minNumber = 2;
  	  	    			if(invNumber < minNumber)
  	  	    				minNumber = 0;
  	  	    			
  	  	    			int maxNumber = invNumber;
  	  	    			clustersToGenerate = currentValue;
  	  	    			spinnerClustersModel = new SpinnerNumberModel(currentValue, minNumber, maxNumber, 1);
  	  	    			spinnerClusters.setModel(spinnerClustersModel);
  	  	    			spinnerClusters.setEnabled(true);
  	  	    		} else {
  	  	    			spinnerClusters.setEnabled(false);
  	  	    			spinnerBlocked = true;
  	  	    		}
  	  	    	} catch (Exception ex) {
  	  	    		spinnerClusters.setEnabled(false);
  	  	    		spinnerBlocked = true;
  	  	    	}
  	  	    	*/
  	  	    }  
    	});
    }
    
    //**************************************************************************************************
    //**************************************              **********************************************
    //**************************************  MyRenderer  **********************************************
    //**************************************    Class     **********************************************
    //**************************************************************************************************
    
    
    /**
     * Klasa wewn�trzna odpowiedzialna za rysowanie poszczeg�lnych kom�rek.
     * @author MR
     *
     */
    class MyRenderer implements TableCellRenderer {
    	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
    	private int mode = 0;
    	private int subRows = 0;
    	/**
    	 * Konstruktor domy�lny obiekt�w klasy MyRenderer.
    	 */
    	public MyRenderer() {
    		
    	}
    	
    	/**
    	 * Konstruktor obiekt�w klasy MyRenderer przyjmuj�cy numer trybu rysowania.
    	 * @param mode int - tryb rysowania
    	 */
    	public MyRenderer(int mode, int rows) {
    		this(); //wywo�anie konstruktora domy�lnego
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
    	 * Przeci��ona metoda odpowiedzialna za zwr�cenie kom�rki tabeli.
    	 */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
        	if(mode == 0)
        		return paintCellsCase56(value, isSelected, hasFocus, row, column);
        	else
        		return paintCellsCase56(value, isSelected, hasFocus, row, column);
        }

        /**
         * Metoda trybu rysowania dla 56 klastr�w, uruchamia si� DLA KA�DEJ kom�rki dodawanej do tabeli.
         * @param value Object - warto�� do wpisania
         * @param isSelected boolean - czy kom�rka jest wybrana
         * @param hasFocus boolean - czy jest aktywna
         * @param row int - nr wiersza
         * @param column int - nr kolumny
         * @return Component - konkretnie: JTextField jako kom�rka tabeli
         */
		private Component paintCellsCase56(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(
					table, value, isSelected, hasFocus, row, column);

		    if(column==0) {
		    	((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		    	renderer.setFont(new Font("Arial", Font.BOLD, 10));
		    	renderer.setBackground(Color.white);
		    } else {
		    	if(row == 0 || row % (subRows+1) == 0) { //wiersza nag��wkowe nazw algorytm�w
		    		((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		    		renderer.setFont(new Font("Arial", Font.BOLD, 10));
		    	} else { //ca�a reszta wierszy
		    		((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		    		renderer.setFont(new Font("Arial", Font.PLAIN, 12));
		    		float cellValue = -1.0f;
		    		
		    		try {
		    			cellValue = Float.parseFloat(value.toString());	
		    		} catch (Exception e) { //invalid parse
		    			cellValue = -1.0f;
		    		}
		    		
		    		if((column+2) % 3 == 0) { // 0-Clusters: kolumny 1, 4, 7, 10, 13, 16, 19
		    			if(cellValue >=0 && cellValue < 5) {
		    				renderer.setBackground(new Color(51, 212, 62));
		    			} else if(cellValue >= 5 && cellValue < 10) {
		    				renderer.setBackground(new Color(231 ,242, 15));
		    			} else {
		    				renderer.setBackground(new Color(242, 52, 15));
		    			}
		    		} else if((column+1) % 3 == 0) { //MSS kolumny: 2, 5, 8, 11, 14, 17, 20
		    			cellValue = cellValue * 100;
		    			renderer.setBackground(getSimpleColor(cellValue));
		    		}
		    		else { //C-H pozosta�e
		    			 ((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
		    			 renderer.setBackground(Color.lightGray);
		    			 try {
		    				 int r = (row) % (subRows+1); 
		    				 // r - bezwzgl�dny nr wiersza: 1 to pierwszy wiersz pod lini� nazw algorytm�w
		    				 // r = SubRows to ostatni wiersz przed kolejn� ramk�

		    				 if(r == 1) {
		    					 Object nextCell = table.getValueAt(row+1, column);	
		    					 Float next = Float.parseFloat(nextCell.toString());
		    					 if(cellValue > next) {
		    						 renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    						 //renderer.setBackground(Color.lightGray);
		    						 
		    					 } else {
		    						 //renderer.setBackground(Color.white);
		    					 }
		    				 } else if (r == subRows) {
		    					 Object previousCell = table.getValueAt(row-1, column);
		    					 Float previous = Float.parseFloat(previousCell.toString());
		    					 if(cellValue > previous) {
		    						 renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    						 //renderer.setBackground(Color.lightGray);
		    					 } else {
		    						 //renderer.setBackground(Color.white);
		    					 }
		    				 } else {
		    					 Object nextCell = table.getValueAt(row+1, column);
		    					 Object previousCell = table.getValueAt(row-1, column);
		    					 Float next = Float.parseFloat(nextCell.toString());
		    					 Float previous = Float.parseFloat(previousCell.toString());
		    					 if(cellValue > previous && cellValue > next) {
		    						 renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    						 renderer.setBackground(Color.gray);
		    					 } else {
		    						 //renderer.setBackground(Color.white);
		    					 }
		    				 }	    				 
		    			 } catch (Exception e) {
		    				 
		    			 } 
		    		}
		    	}
		    }
		    return renderer;
		}
		
		/**
		 * Metoda zwraca kolor w zale�no�ci od skali w formie proste skali kolor�w ziele�-czerwie�.
		 * @param power double - warto�� od 0 do 100;
		 * @return Color - kolor w zale�no�ci od skali
		 */
		public Color getSimpleColor(double power)
		{
			Color result = Color.white;
		    if(power < 30) {
		    	result = new Color(250, 2, 2);
		    } else if(power <40) {
		    	result = new Color(240, 92, 12);
		    } else if(power <50) {
		    	result = new Color(240, 171, 12);
		    } else if(power <60) {
		    	result = new Color(173, 217, 13);
		    } else if(power <75) {
		    	result = new Color(12, 232, 26);
		    } else {
		    	result = new Color(1, 153, 19);
		    }
		    return result;
		}
    } // END CLASS MyRenderer IMPLEMENTS TableCellRenderer
    
}
