package holmes.windows.clusters;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import holmes.clusters.Clustering;
import holmes.clusters.ClusteringInfoMatrix;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.files.clusters.CHmetricReader;
import holmes.files.clusters.ClusterReader;
import holmes.files.clusters.RClusteringParserToXLS;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa obsługująca okno klastrów dla danej sieci. Poza tym mnóstwo przycisków przeznaczonych
 * do wykonywania operacji na klastrach: generowanie, wczytywanie, eksport do plików (także
 * Excel). Wywołuje mniejsze okno informacji o klastrowaniu po kliknięciu na odpowiednią
 * komórkę tabeli.
 */
public class HolmesClusters extends JFrame {
	@Serial
	private static final long serialVersionUID = -8420712475473581772L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private JTable table = new JTable();
	private DefaultTableModel tableModel;
	private int mode = 0; // 0 - tryb 56 klastrowań
	private ClusterTableRenderer tableRenderer = new ClusterTableRenderer(mode, 18);
	private JScrollPane scrollTablePane;
	    
	private int subRowsSize = 0;
    private final HolmesClusters myself;
    private int clustersToGenerate;


	private ClusteringInfoMatrix dataTableCase56 = null;
    private String pathCSVfile = "";
    private String pathClustersDir = "";
    
    private ArrayList<String> commandsValidate;
    private HolmesClusterConfig minion;
    
    /**
     * Konstruktor domyślny obiektu okna klasy HolmesClusters. Tworzy wszystkie elementy okna
     * z tabelą klastrów.
     */
    public HolmesClusters() {
    	myself = this;
    	myself.setTitle(lang.getText("HCwin_entry001title"));
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00505exception")+"\n"+ex.getMessage(), "error", true);
		}
    	this.setVisible(false);
    	
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

		JPanel tablePanel;
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
        
        commandsValidate = new ArrayList<String>(); //lista 'miar' (z 56) do obliczeń
        commandsAddAll();
        
        this.setContentPane(mainPanel);
        this.pack();
        
        minion = new HolmesClusterConfig(commandsValidate, myself);
    }

    /**
     * Metoda zwracająca panel boczny dla głównego okna, wypełniony przyciskami.
     * @return JPanel - obiekt panelu bocznego
     */
	private JPanel createButtonsPanelCase56() {
		JPanel textPanel = new JPanel();
		textPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
		
		JLabel clLabel = new JLabel(lang.getText("HCwin_entry002")); //Generate clusters:
		clLabel.setFont(new Font("Arial", Font.BOLD, 11));
		clLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		textPanel.add(clLabel);	
		
		// Komponent określenie górnego limitu klastrów dla obliczeń
		SpinnerModel spinnerClustersModel = new SpinnerNumberModel(20, 2, 300, 1);
		clustersToGenerate = 20;
		JSpinner spinnerClusters = new JSpinner(spinnerClustersModel);
		spinnerClusters.setPreferredSize(new Dimension(100, 30));
		spinnerClusters.setMinimumSize(new Dimension(100, 30));
		spinnerClusters.setMaximumSize(new Dimension(100, 30));
		spinnerClusters.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			clustersToGenerate = (int) spinner.getValue();
		});
		spinnerClusters.setAlignmentX(Component.CENTER_ALIGNMENT);
		textPanel.add(spinnerClusters);	

		// Przycisk rozpoczęcia procedury generowania klastrów na bazie inwariantów
		JButton generateButton = createStandardButton("", 
				Tools.getResIcon48("/icons/clustWindow/buttonGen56.png"));
		generateButton.setToolTipText(lang.getText("HCwin_entry003t"));
		generateButton.addActionListener(actionEvent -> buttonGenerateClusterings(commandsValidate));
		generateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(generateButton);
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk rozpoczęcia procedury generowania metryk Celińskiego-Harabasza dla tabeli klastrowań
     	JButton generateCHButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonComputeCH.png"));
     	generateCHButton.setToolTipText(lang.getText("HCwin_entry004t"));
     	generateCHButton.addActionListener(actionEvent -> buttonComputeCHmetrics(commandsValidate));
     	generateCHButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(generateCHButton);
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk wczytania katalogu z 56 klastrowaniami
        JButton case56Button = createStandardButton("", 
        		Tools.getResIcon48("/icons/clustWindow/buttonLoadClusterDir.png"));
        case56Button.setToolTipText(lang.getText("HCwin_entry005t"));
        case56Button.addActionListener(actionEvent -> buttonLoadClusteringDirectory());
        case56Button.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(case56Button);
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk wczytywania metryk C-H dla tabeli
     	JButton loadCHButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonLoadCHDir.png"));
     	loadCHButton.setToolTipText(lang.getText("HCwin_entry006t"));
     	loadCHButton.addActionListener(actionEvent -> buttonLoadCHmetricIntoTables());
     	loadCHButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(loadCHButton);
     	textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk zapisu tabeli danych
     	JButton saveTableButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonSaveTable.png"));
     	saveTableButton.setToolTipText(lang.getText("HCwin_entry007t"));
     	saveTableButton.addActionListener(actionEvent -> buttonSerializeDataTable());
     	saveTableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(saveTableButton);
     	textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk zapisu tabeli danych
     	JButton loadTableButton = createStandardButton("", 
     			Tools.getResIcon48("/icons/clustWindow/buttonLoadTable.png"));
     	loadTableButton.setToolTipText(lang.getText("HCwin_entry008t"));
     	loadTableButton.addActionListener(actionEvent -> buttonDeserializeFile());
     	loadTableButton.setAlignmentX(Component.CENTER_ALIGNMENT);
     	textPanel.add(loadTableButton);
     	textPanel.add(Box.createVerticalStrut(7));
     	
     	// Przycisk exportu tabeli danych do excela
        JButton excelExport = createStandardButton("", 
        		Tools.getResIcon48("/icons/clustWindow/buttonExportToExcel.png"));
        excelExport.setToolTipText(lang.getText("HCwin_entry009t"));
        excelExport.addActionListener(actionEvent -> buttonExportTableToExcel());
        excelExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(excelExport);
        textPanel.add(Box.createVerticalStrut(7));
        
        
        JButton configButton = createStandardButton("", 
        		Tools.getResIcon48("/icons/clustWindow/buttonConfig.png"));
        configButton.setToolTipText(lang.getText("HCwin_entry010t"));
        configButton.addActionListener(actionEvent -> minion.setVisible(true));
        configButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(configButton);
        textPanel.add(Box.createVerticalStrut(7));

		return textPanel;
	}
	
	/**
	 * Metoda pomocnicza do tworzenia przycisków do panelu bocznego.
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
    public HolmesClusters(int mode) {
    	this();
    	this.mode = mode;
    }
    
    /**
     * Metoda ustawia ścieżkę dostępu do katalogu klastrów.
     * @param path String - ścieżka do katalogu
     */
    public void setClusterPath(String path) {
    	pathClustersDir = path;
    }
    
    /**
     * Metoda zwraca ścieżkę dostępu do katalogu klastrów.
     * @return String - ścieżka do katalogu
     */
    public String getClusterPath() {
    	return pathClustersDir;
    }
    
    /**
     * Metoda pomocnicza konstruktora, tworzy panel z tabelą danych na 15 kolumn.
     * 7 x 8 = 56 podtabel klastrowań, standardowy przypadek.
     * @return JPanel - panel zawierający tabelę danych
     */
    private JPanel createTablePanelCase56() {
    	JPanel main = new JPanel();
    	main.setLayout(new BorderLayout());

    	//table = new JTable(new DefaultTableModel());
    	setTableModel56();
        //table = new JTable(tableModel);

        table.setDefaultRenderer(Object.class, tableRenderer); // 0 - case 56
        table.addMouseListener(new MouseAdapter() { //listener kliknięć
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	JTable target = (JTable)e.getSource();
          	    	int row = target.getSelectedRow();
          	    	int column = target.getSelectedColumn();
          	    	
          	    	cellClickedEvent(row, column);
          	    }
          	 }
      	});

        scrollTablePane = new JScrollPane(table);
        main.add(scrollTablePane);
        return main;
    }
    
    private void setTableModel56() {
    	tableModel = new DefaultTableModel();
    	
        tableModel.addColumn("Column1"); //miara odległości
        tableModel.addColumn("Column2"); //zerowe klastry dla algorytmu klastrowania
        tableModel.addColumn("Column3"); //MSS algorytmu
        tableModel.addColumn("Column4"); //CH algorytmu
        tableModel.addColumn("Column5"); //kolejna trójka jak dla 2-4, itd,
        tableModel.addColumn("Column6");
        tableModel.addColumn("Column7");
        tableModel.addColumn("Column8"); //kolejna trójka jak dla 2-4, itd,
        tableModel.addColumn("Column9");
        tableModel.addColumn("Column10");
        tableModel.addColumn("Column11");
        tableModel.addColumn("Column12");
        tableModel.addColumn("Column13");
        tableModel.addColumn("Column14");
        tableModel.addColumn("Column15");
        tableModel.addColumn("Column16");
        tableModel.addColumn("Column17");
        tableModel.addColumn("Column18");
        tableModel.addColumn("Column19");
        tableModel.addColumn("Column20");
        tableModel.addColumn("Column21");
        tableModel.addColumn("Column22");
        
        table.setModel(tableModel);
        
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
        
        table.setFillsViewportHeight(true);

        //rozmiary kolumn:
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        for(int index=1; index<table.getColumnCount(); index++) {
        	if((index+2) % 3 == 0) { //trójka
        		table.getColumnModel().getColumn(index).setPreferredWidth(20);
        		table.getColumnModel().getColumn(index+1).setPreferredWidth(50);
        		table.getColumnModel().getColumn(index+2).setPreferredWidth(50);
        		index++;
        		index++;
        	}
        }
    }
    
    /**
     * Obsługa zdarzenia kliknięcia na komórkę. Metoda przelicza lokalizację komórki na lokalizację
     * w tabeli danych. Następnie wywołuje podokno informacyjne.
     * @param row int - nr klikniętego wiersza
     * @param column int - nr klikniętej komórki
     */
    private void cellClickedEvent(int row, int column) {
		int sub = subRowsSize;
		if(column != 0 && row % (sub+1) != 0) { // NIE dla I kolumny i wierszy nagłówkowych
			try {
				//tutaj dzieje się magia na liczbach - jednakoż dzieje się prawidłowo, dlatego
				//lepiej tutaj niczego nie zmieniać we wzorach
				int clusterNumber = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
				int algID = (column-1) / 3; //1,2,3 -> 0; 4,5,6->1, itd.
				
				//obliczanie numery wiersza nagłówkowego nad klikniętym wierszem:
				int headerRowNumber = row - (clusterNumber-1);
				//zmiana na nr miary, pierwszy blok, to miara 0, następny 1, itd. aż do 7
				headerRowNumber /= (subRowsSize+1);
				
				Clustering omg = dataTableCase56.getClustering((headerRowNumber*7)+algID, clusterNumber-2);
				new HolmesClusterSubWindow(myself, omg, 1);
				
			 } catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00506exception")
						  +"\n"+ex.getMessage(), "error", true);
			 }
		 }
	}
    
    /**
     * Metoda służąca do wypełnienia tabeli w przypadku kiedy mamy 56 klastrowań.
     * @param newTable ClusteringInfoMatrix - główna baza-tabela danych
     */
	public void registerDataCase56(ClusteringInfoMatrix newTable) {
		if(newTable == null) return; //you no take candle!
		
    	dataTableCase56 = newTable;
    	subRowsSize = newTable.secondaryTablesMinNumber;
    	int checkSize = newTable.mainTablesNumber;
    	if(checkSize != 56) {
    		overlord.log(lang.getText("HCwin_entry011"), "error", true);
    		return;
    	}

		overlord.log(lang.getText("HCwin_entry012"), "text", true);

    	tableRenderer.setMode(mode);  // !!!
    	tableRenderer.setSubRows(subRowsSize); // !!! zła wartość i tabela idzie w ....
    	
    	setTableModel56();
    	//CLEAR OLD TABLE ROWS:
    	//tableModel.setRowCount(0);
    	
    	
    	String[] metricName = { "Correlation", "Pearson", "Binary", "Canberra", "Euclidean", "Manhattan", "Maximum", "Minkowski" };
    	
    	for(int metric=0; metric <8; metric++) { //dla każdej z ośmiu metryk:
    		String[] data = { metricName[metric],"0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H","0:","MSS","C-H"};
			tableModel.addRow(data);
			
    		for(int rows=0; rows < subRowsSize; rows++) { //dla odpowiedniej liczby wierszy:
    			String[] dataRow = { "","","","","","","","","","","","","","","","","","","","","",""}; //22 elementów
    			dataRow[0] = ""+(rows+2);
    			for(int alg=0; alg < 7; alg++ ) { // dla każdego wiersza jedziemy po algorytmach
    				// Average, Centroid, Complete, McQuitty, Median, Single, Ward
        			int tableIndex = (metric*7)+alg; //która tabelka
            		
        			dataRow[1+alg*3] = ""+dataTableCase56.getMatrix().get(tableIndex).get(rows).zeroClusters;
        			
        			double val = dataTableCase56.getMatrix().get(tableIndex).get(rows).evalMSS;
        			String cuttedValue = cutValueMSS(val);
        			dataRow[1+alg*3+1] = cuttedValue;
        			
        			double val2 = dataTableCase56.getMatrix().get(tableIndex).get(rows).evalCH;
        			String cuttedValue2 = cutValueCH(val2);
        			dataRow[1+alg*3+2] = cuttedValue2;
            	}
    			tableModel.addRow(dataRow);
			}
    	}
		overlord.log(lang.getText("HCwin_entry013"), "text", true);
    	tableModel.fireTableDataChanged();
    	table.revalidate();
    	
    	scrollTablePane.repaint();
    }
    
	/**
	 * Metoda formatuje liczbę typu double do wyznaczonej liczby miejsc po przecinku, a następnie
	 * zwraca ją jako String.
	 * @param evalMSS double - liczba do przycięcia
	 * @return String - reprezentacja liczby
	 */
    private String cutValueMSS(double evalMSS) {
    	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(getLocale());
    	otherSymbols.setDecimalSeparator('.');
    	DecimalFormat df = new DecimalFormat("#.####", otherSymbols);
		return df.format(evalMSS);
	}
    
    /**
	 * Metoda formatuje liczbę typu double do wyznaczonej liczby miejsc po przecinku, a następnie
	 * zwraca ją jako String.
	 * @param evalCH double - liczba do przycięcia
	 * @return String - reprezentacja liczby
	 */
    private String cutValueCH(double evalCH) {
    	DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(getLocale());
    	otherSymbols.setDecimalSeparator('.');
    	DecimalFormat df = new DecimalFormat("#.#", otherSymbols);
		return df.format(evalCH);
	}

    /**
     * Metoda zwraca skalowalny kolor dla liczby.
     * @param power (<b>double</b>) wartość
     * @return (<b>Color</b>) kolor
     */
    public Color getColor(double power)
	{
	    //double H = power * 0.4; // Hue (note 0.4 = Green, see huge chart below)
	    //double S = 0.9; // Saturation
	    //double B = 0.9; // Brightness
	    //return Color.getHSBColor((float)H, (float)S, (float)B);
		double R = (255 * power) / 100;
		double G = (255 * (100 - power)) / 100; 
		//double B = 0;
		try {
			return new Color((int)R,(int)G, 0);
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
     * Metoda realizuje generowanie klastrowań dla inwariantów z sieci. Inwarianty będą 
     * w formie pliku CSV, który będzie utworzony automatycznie, tak więc sieć i inwarianty
     * muszą już istnieć w programie. W jej efekcie powstaje katalog z klastrowaniami.
     * @param commandsValidate ArrayList[String] - lista wywołań w R do uruchomienia
     */
    private void buttonGenerateClusterings(ArrayList<String> commandsValidate) {
		if(clustersToGenerate > 1) {
			pathCSVfile = overlord.io.generateClustersCase56(clustersToGenerate, commandsValidate);
			if(pathCSVfile == null) { //jeśli coś się nie udało
				//pathClustersDir = ""; //ścieżka do katalogu klastrowań
				pathCSVfile = ""; //ścieżka do pliku CSV
			} else {
				JOptionPane.showMessageDialog(null, lang.getText("HCwin_entry014"),
						lang.getText("HCwin_entry014t"), JOptionPane.INFORMATION_MESSAGE);
				//pathClustersDir = new File(pathCSVfile).getParent(); //uzyskanie katalogu z klastrowaniami
				//przy czym tam wciąż pewnie trwa praca (dopiero się zaczęła) tworzenia w osobnym
				//wątku plików klastrowań poprzez skrypty R
			}
		}
	}
    
    /**
     * Metoda odpowiada za utworzenie tabeli danych i jej wyświetlenie. Tabela będzie
     * tworzona na podstawie danych ze wskazanego katalogu z wygenerowanymi plikami
     * klastrowań.
     */
    private void buttonLoadClusteringDirectory() {
		String lastPath = overlord.getLastPath();
		/*
		String lastPath2 = getClusterPath();
		String chosenPath="";
		if(lastPath2.equals(""))
			chosenPath = lastPath;
		else
			chosenPath = lastPath2;
		 */
		
		String choosenDir = Tools.selectDirectoryDialog(lastPath, lang.getText("HCwin_entry015"),
					lang.getText("HCwin_entry015t")); //Select cluster dir
		if(choosenDir.isEmpty())
			return;
		
		ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
		int result = clusterMatrix.readDataDirectory(choosenDir);
		if(result == -1) {
			JOptionPane.showMessageDialog(null, lang.getText("HCwin_entry016"),
					lang.getText("error"), JOptionPane.ERROR_MESSAGE);
		} else {
			registerDataCase56(clusterMatrix);
			setClusterPath(choosenDir);
		}
		
		//czy plik startowy jest na miejscu?
		pathCSVfile = choosenDir+"/cluster.csv";
		if(!Tools.ifExist(pathCSVfile)) {
			JOptionPane.showMessageDialog(null, lang.getText("HCwin_entry017"), "warning",JOptionPane.ERROR_MESSAGE);
			overlord.log(lang.getText("HCwin_entry017"), "warning", true);
		}
	}
    
    /**
     * Metoda odpowiedzialna za utworzenie dokumentu w formacie .xls programu Excel
     * wyświetlanej w oknie tabeli danych o klastrowaniach.
     */
    private void buttonExportTableToExcel() {
		try{
			String lastPath = overlord.getLastPath();
			//String lastPath = overlord.getLastPath();
			overlord.log(lang.getText("HCwin_entry018"),"text", true);
			
			String dirPath = Tools.selectDirectoryDialog(lastPath, lang.getText("HCwin_entry019"),
					lang.getText("HCwin_entry019t"));
			if(dirPath.isEmpty()) { // czy wskazano cokolwiek
				return;
			} 
			
			//sprawdzić czy są wszystkie pliki / odtworzyć if necessary
			ClusterReader cr = new ClusterReader();
			if(cr.checkFiles(dirPath) == -2) { //no cluster files
				JOptionPane.showMessageDialog(null, lang.getText("HCwin_entry020"),
						lang.getText("error"),JOptionPane.ERROR_MESSAGE);

				String strB = "err.";
				try {
					strB = String.format(lang.getText("HCwin_entry021"), dirPath);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HCwin_entry021", "error", true);
				}
				overlord.log(strB, "error", true);
			}
			
			RClusteringParserToXLS r = new holmes.files.clusters.RClusteringParserToXLS();
			r.extractAllRClusteringToXLS(dirPath, dirPath+"//ClustersSummary.xls");
			
			File test = new File(dirPath+"//ClustersSummary.xls");
			if(test.exists()) {
				FileFilter[] filter = new FileFilter[1];
				filter[0] = new ExtensionFileFilter(".xls - Excel 2003",  new String[] { "XLS" });
				String newLocation = Tools.selectFileDialog(dirPath, filter, "Save", "", "");
				if(newLocation.isEmpty()) { //czy chcemy przenieść plik w inne miejsce
					//leave it in cluster folder
					String strB = "err.";
					try {
						strB = String.format("Exporting table succeed. Created file: %s//ClustersSummary.xls", dirPath);
					} catch (Exception e) {
						overlord.log(lang.getText("in-code")+" "+"code", "error", true);
					}
					overlord.log(strB, "text", true);
				} else {
					if(!newLocation.contains(".xls"))
						newLocation += ".xls";
					Tools.copyFileByPath(dirPath+"//ClustersSummary.xls", newLocation);
					test.delete(); //kasujemy oryginalny
					overlord.log(lang.getText("HCwin_entry023")+" "
							+newLocation, "text", true);
				}
				
			} else {
				String msg = lang.getText("HCwin_entry024");
				overlord.log(msg, "error", true);
			}
		} catch (Exception e){
			String msg = lang.getText("HCwin_entry025")+getClusterPath();
			overlord.log(msg, "error", true);
			overlord.log(e.getMessage(), "error", true);
		}
	}
    
    /**
     * Metoda odpowiedzialna za wygenerowanie metryk Celińskiego-Harabasza dla
     * zadanego limitu liczby klastrów.
     * @param commandsValidate ArrayList[String] - lista wywołań w R do uruchomienia
     */
    private void buttonComputeCHmetrics(ArrayList<String> commandsValidate) {
		if(clustersToGenerate > 1) {
			@SuppressWarnings("unused")
			String newCHpath = overlord.io.generateAllCHindexes(clustersToGenerate, commandsValidate);
			//uwaga! w powyższym katalogu miary dopiero powstają!
		}
	}
    
    /**
     * Obsługa wczytywania miar Celińskiego-Harabasz do tabeli głównej.
     */
	private void buttonLoadCHmetricIntoTables() {
		String lastPath = overlord.getLastPath();
		if(dataTableCase56 == null) {
			JOptionPane.showMessageDialog(null,lang.getText("HCwin_entry026")
					,lang.getText("warning"),JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		
		//String dirPath = Tools.selectDirectoryDialog(pathCHmetricsDir, "Select directory",
		//		"Directory with 56 R-generated files containing Celiński-Harabasz metrics");
		String dirPath = Tools.selectDirectoryDialog(lastPath, lang.getText("HCwin_entry027"),
				lang.getText("HCwin_entry027t"));
		
		if(dirPath.isEmpty()) { // czy wskazano cokolwiek
			return;
		} 
		CHmetricReader chReader = new CHmetricReader();
		ArrayList<ArrayList<Double>> chDataCore = chReader.readCHmetricsDirectory(dirPath);
		
		integrateCHIntoDatabase(chDataCore);
	}
	
	/**
	 * Metoda wczytuje miary Celińskiego-Harabasz do tabeli głównej. Tak się miło składa, że
	 * indeksy podtabel (algorytm-miara) są te same, bo pliki w obu przypadkach (klastrowanie 
	 * oraz miary C-H) były czytane w tym samym porzśdku. A jeśli się nie dało, to w ogóle nie
	 * były przeczytane, więc i tak nie ma co wczytywać :)
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
					String strB = "err.";
					try {
						strB = String.format(lang.getText("LOGentry00507exception"), i, cl);
					} catch (Exception ex) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"LOGentry00507exception", "error", true);
					}
					overlord.log(strB + " " +e.getMessage(), "error", true);
					dataTableCase56.getMatrix().get(i).get(cl).evalCH = 0.0;
				}
			}
		}
		registerDataCase56(dataTableCase56);
	}

	/**
	 * Metoda odpowiedzialna za zapis tabeli danych do pliku za pomocą mechanizmu
	 * serializacji.
	 */
	private void buttonSerializeDataTable() {
		try{
			String lastPath = overlord.getLastPath();
			
			FileFilter[] filter = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Holmes CLustering file (.hcl)",  new String[] { "hcl", "acl" });
			String newLocation = Tools.selectFileDialog(lastPath, filter, lang.getText("HCwin_entry028"), "", "");
			if(newLocation.isEmpty())
				return;
			
			if(!newLocation.contains(".hcl"))
				newLocation += ".hcl";

			FileOutputStream fos= new FileOutputStream(newLocation);
			ObjectOutputStream oos= new ObjectOutputStream(fos);
			oos.writeObject(dataTableCase56);
			oos.close();
			fos.close();
		} catch(IOException ioe){
			overlord.log(lang.getText("LOGentry00508exception")+" "+ioe.getMessage(), "error", true);
		}
	}
	
	/**
	 * Metoda odpowiedzialna za odczyt pliku z zserializowanymi danymi obiektów 
	 * tablicy danych do... tablicy danych. Makes sense actually.
	 */
	private void buttonDeserializeFile() {
		ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
		String lastPath = overlord.getLastPath();
		
		String newLocation = "";
		try
		{
			FileFilter[] filter = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Holmes CLustering file (.hcl)",  new String[] { "hcl", "acl" });
			newLocation = Tools.selectFileDialog(lastPath, filter, lang.getText("HCwin_entry029"), "", "");
			if(newLocation.isEmpty())
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
			String msg = lang.getText("LOGentry00509exception")+" "+newLocation;
			overlord.log(msg, "error", true);
		} 
	}
    
	/**
	 * Inicjalizacja agentów nasłuchujących różne zdarzenia dla okna klastrowań.
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
	  	     * Kiedy okno staje się aktywne
	  	     */
  	  	    public void windowActivated(WindowEvent e) {
  	  	    /*
  	  	    	try {
  	  	    		if(overlord.getWorkspace().getProject().getInvariantsMatrix() != null) {
  	  	    			
  	  	    			if(spinnerBlocked) {
  	  	    				spinnerBlocked=false;
  	  	    			} else {
  	  	    				return;
  	  	    			}
  	  	    			int invNumber = overlord.getWorkspace().getProject().getInvariantsMatrix().size();
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
    
    public void resetWindow() {
    	//tablePanel = createTablePanelCase56();
    	tableModel.setRowCount(0);
    }
    
    /**
     * Metoda wpisuje wszystkie kombinacje klastrowania na listę zadań
     */
    private void commandsAddAll() {
    	commandsValidate.add("\"correlation\",\"average\"");
    	commandsValidate.add("\"correlation\",\"centroid\"");
    	commandsValidate.add("\"correlation\",\"complete\"");
    	commandsValidate.add("\"correlation\",\"mcquitty\"");
    	commandsValidate.add("\"correlation\",\"median\"");
    	commandsValidate.add("\"correlation\",\"single\"");
    	commandsValidate.add("\"correlation\",\"ward\"");
    	
    	commandsValidate.add("\"pearson\",\"average\"");
    	commandsValidate.add("\"pearson\",\"centroid\"");
    	commandsValidate.add("\"pearson\",\"complete\"");
    	commandsValidate.add("\"pearson\",\"mcquitty\"");
    	commandsValidate.add("\"pearson\",\"median\"");
    	commandsValidate.add("\"pearson\",\"single\"");
    	commandsValidate.add("\"pearson\",\"ward\"");
    	
    	commandsValidate.add("\"binary\",\"average\"");
    	commandsValidate.add("\"binary\",\"centroid\"");
    	commandsValidate.add("\"binary\",\"complete\"");
    	commandsValidate.add("\"binary\",\"mcquitty\"");
    	commandsValidate.add("\"binary\",\"median\"");
    	commandsValidate.add("\"binary\",\"single\"");
    	commandsValidate.add("\"binary\",\"ward.D\"");
    	
    	commandsValidate.add("\"canberra\",\"average\"");
    	commandsValidate.add("\"canberra\",\"centroid\"");
    	commandsValidate.add("\"canberra\",\"complete\"");
    	commandsValidate.add("\"canberra\",\"mcquitty\"");
    	commandsValidate.add("\"canberra\",\"median\"");
    	commandsValidate.add("\"canberra\",\"single\"");
    	commandsValidate.add("\"canberra\",\"ward.D\"");
    	
    	commandsValidate.add("\"euclidean\",\"average\"");
    	commandsValidate.add("\"euclidean\",\"centroid\"");
    	commandsValidate.add("\"euclidean\",\"complete\"");
    	commandsValidate.add("\"euclidean\",\"mcquitty\"");
    	commandsValidate.add("\"euclidean\",\"median\"");
    	commandsValidate.add("\"euclidean\",\"single\"");
    	commandsValidate.add("\"euclidean\",\"ward.D\"");
    	
    	commandsValidate.add("\"manhattan\",\"average\"");
    	commandsValidate.add("\"manhattan\",\"centroid\"");
    	commandsValidate.add("\"manhattan\",\"complete\"");
    	commandsValidate.add("\"manhattan\",\"mcquitty\"");
    	commandsValidate.add("\"manhattan\",\"median\"");
    	commandsValidate.add("\"manhattan\",\"single\"");
    	commandsValidate.add("\"manhattan\",\"ward.D\"");
    	
    	commandsValidate.add("\"maximum\",\"average\"");
    	commandsValidate.add("\"maximum\",\"centroid\"");
    	commandsValidate.add("\"maximum\",\"complete\"");
    	commandsValidate.add("\"maximum\",\"mcquitty\"");
    	commandsValidate.add("\"maximum\",\"median\"");
    	commandsValidate.add("\"maximum\",\"single\"");
    	commandsValidate.add("\"maximum\",\"ward.D\"");
    	
    	commandsValidate.add("\"minkowski\",\"average\"");
    	commandsValidate.add("\"minkowski\",\"centroid\"");
    	commandsValidate.add("\"minkowski\",\"complete\"");
    	commandsValidate.add("\"minkowski\",\"mcquitty\"");
    	commandsValidate.add("\"minkowski\",\"median\"");
    	commandsValidate.add("\"minkowski\",\"single\"");
    	commandsValidate.add("\"minkowski\",\"ward.D\"");
    }
    
    //**************************************************************************************************
    //**************************************  MyRenderer  **********************************************
    //**************************************              **********************************************
    //**************************************    Class     **********************************************
    //**************************************************************************************************

    /**
     * Klasa wewnętrzna odpowiedzialna za rysowanie poszczególnych komórek.
     * @author MR
     *
     */
    class ClusterTableRenderer implements TableCellRenderer {
    	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
    	private int mode = 0;
    	private int subRows = 0;
    	/**
    	 * Konstruktor domyślny obiektów klasy MyRenderer.
    	 */
    	public ClusterTableRenderer() {
    		
    	}
    	
    	/**
    	 * Konstruktor obiektów klasy MyRenderer przyjmujący numer trybu rysowania.
    	 * @param mode int - tryb rysowania
    	 */
    	public ClusterTableRenderer(int mode, int rows) {
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
			Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		    if(column==0) {
		    	((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		    	renderer.setFont(new Font("Arial", Font.BOLD, 10));
		    	renderer.setBackground(Color.white);
		    } else {
		    	if(row == 0 || row % (subRows+1) == 0) { //wiersza nagłówkowe nazw algorytmów
		    		((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
		    		renderer.setFont(new Font("Arial", Font.BOLD, 10));
		    	} else { //cała reszta wierszy
		    		((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
		    		renderer.setFont(new Font("Arial", Font.PLAIN, 12));
		    		float cellValue;
		    		
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
		    		else { //C-H pozostałe
		    			 ((DefaultTableCellRenderer)renderer).setHorizontalAlignment(DefaultTableCellRenderer.RIGHT);
		    			 renderer.setBackground(Color.lightGray);
		    			 try {
		    				 int r = (row) % (subRows+1); 
		    				 // r - bezwzględny nr wiersza: 1 to pierwszy wiersz pod linią nazw algorytmów
		    				 // r = SubRows to ostatni wiersz przed kolejną ramką

		    				 if(r == 1) {
		    					 Object nextCell = table.getValueAt(row+1, column);	
		    					 float next = Float.parseFloat(nextCell.toString());
		    					 if(cellValue > next) {
		    						 renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    						 //renderer.setBackground(Color.lightGray);
		    						 
		    					 } else {
		    						 //renderer.setBackground(Color.white);
		    					 }
		    				 } else if (r == subRows) {
		    					 Object previousCell = table.getValueAt(row-1, column);
		    					 float previous = Float.parseFloat(previousCell.toString());
		    					 if(cellValue > previous) {
		    						 renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    						 //renderer.setBackground(Color.lightGray);
		    					 } else {
		    						 //renderer.setBackground(Color.white);
		    					 }
		    				 } else {
		    					 Object nextCell = table.getValueAt(row+1, column);
		    					 Object previousCell = table.getValueAt(row-1, column);
		    					 float next = Float.parseFloat(nextCell.toString());
		    					 float previous = Float.parseFloat(previousCell.toString());
		    					 if(cellValue > previous && cellValue > next) {
		    						 renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    						 renderer.setBackground(Color.gray);
		    					 } else {
		    						 //renderer.setBackground(Color.white);
		    					 }
		    				 }	    				 
		    			 } catch (Exception ex) {
							 overlord.log("Error (747155147) | Exception:  "+ex.getMessage(), "error", true);
		    			 } 
		    		}
		    	}
		    }
		    return renderer;
		}
		
		/**
		 * Metoda zwraca kolor w zależności od skali w formie proste skali kolorów zieleń-czerwień.
		 * @param power double - wartość od 0 do 100;
		 * @return Color - kolor w zależności od skali
		 */
		public Color getSimpleColor(double power)
		{
			Color result;
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
