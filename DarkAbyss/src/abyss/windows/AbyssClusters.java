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
import abyss.files.clusters.ClusterReader;
import abyss.files.clusters.RClusteringParserToXLS;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;
import abyss.darkgui.GUIManager;

/**
 * Klasa obs³uguj¹ca okno klastrów dla danej sieci.
 * @author MR
 *
 */
public class AbyssClusters extends JFrame {
	private static final long serialVersionUID = 6942814230861358341L;
	private JTable table;
	private DefaultTableModel  model;
	private int subRowsSize = 0;
    private ClusteringInfoMatrix internalDataTables;
    private String clustersPath = "";
    private final AbyssClusters myself;
    private int clustersToGenerate = 0;
    private SpinnerModel spinnerClustersModel;
    private JSpinner spinnerClusters;
    
    private int mode = 0; // 0 - tryb 56 klastrowañ
    private MyRenderer tabRenderer = new MyRenderer(mode, 18);
    /**
     * Konstruktor domyœlny obiektu okna klasy AbyssClusters. Tworzy wszystkie elementy okna
     * z tabel¹ klastrów.
     */
    public AbyssClusters() {
    	myself = this;
    	this.setTitle("Abyss Cluster Window");
    	clustersToGenerate = 0;	
    	initiateListeners();

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		this.setLocation(25, 25);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		setMinimumSize(new Dimension(900, screenSize.height-100));
		setMaximumSize(new Dimension(900, screenSize.height-100));
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
     * Metoda zwracaj¹ca panel boczny dla g³ównego okna, wype³niony przyciskami.
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
		
		spinnerClustersModel = new SpinnerNumberModel(0, 0, 1, 1);
		spinnerClusters = new JSpinner(spinnerClustersModel);
		spinnerClusters.setPreferredSize(new Dimension(100, 30));
		spinnerClusters.setMinimumSize(new Dimension(100, 30));
		spinnerClusters.setMaximumSize(new Dimension(100, 30));
		spinnerClusters.setEnabled(false);
		spinnerClusters.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				clustersToGenerate = (int) spinner.getValue();
			}
		});
		spinnerClusters.setAlignmentX(Component.CENTER_ALIGNMENT);
		textPanel.add(spinnerClusters);	

		// Przycisk rozpoczêcia procedury generowania klastrów na bazie inwariantów
		JButton generateButton = createStandardButton("Generate data", null);
		generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(clustersToGenerate > 1)
					GUIManager.getDefaultGUIManager().generateClusters(clustersToGenerate);
			}
		});
		generateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(generateButton);
        
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk wczytania katalogu z 56 klastrowaniami
        JButton case56Button = createStandardButton("Load directory", null);
        case56Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
				String choosenDir = Tools.selectDirectoryDialog(lastPath, "Select cluster dir",
							"Directory with 56 generated text R-clusters files.");
				if(choosenDir.equals(""))
					return;
				
				//setClusterPath(choosenDir);
				ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
				int result = clusterMatrix.readDataDirectory(choosenDir);
				if(result == -1) {
					JOptionPane.showMessageDialog(null, "Cluster reading failed. Possible wrong directory chosen.", "Error",JOptionPane.ERROR_MESSAGE);
				} else {
					handleStandardClusterTableCase56(clusterMatrix);
				}
				
				//test/debug
				/* 
				ClusteringInfoMatrix clusterMatrix = new ClusteringInfoMatrix();
				int result = clusterMatrix.readDataDirectory("tmp//");
				if(result == -1) {
					JOptionPane.showMessageDialog(null, "Cluster reading failed. Possible wrong directory chosen.", "Error",JOptionPane.ERROR_MESSAGE);
				} else {
					handleStandardClusterTableCase56(clusterMatrix);
				}
				*/
			}

			
		});
        case56Button.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(case56Button);
        
        textPanel.add(Box.createVerticalStrut(7));
        
        // Przycisk exportu tabeli danych do excela
        JButton excelExport = createStandardButton("Export to excel", null);
        excelExport.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent actionEvent) {
 				try{
 					//okno dialogowe do wskazania katalogu:
 					String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
 					GUIManager.getDefaultGUIManager().log("Attempting to export cluster table to excel",
 							"text", true);
 					
 					String dirPath = Tools.selectDirectoryDialog(lastPath, "Select cluster dir",
 							"Directory with 56 generated text R-clusters files.");
 					if(dirPath.equals("")) { // czy wskazano cokolwiek
 						return;
 					} else
 						setClusterPath(dirPath);
 					//jeœli powy¿sze siê uda, wtedy w 'clustersPath' bêdzie œcie¿ka do katalogu
 					
 					//sprawdziæ czy s¹ wszystkie pliki / odtworzyæ if necessary
 					ClusterReader cr = new ClusterReader();
 					if(cr.checkFiles(clustersPath) == -2) { //no cluster files
 						JOptionPane.showMessageDialog(null, "Directory does not contain a single cluster file.",
 								"Error",JOptionPane.ERROR_MESSAGE);
 						GUIManager.getDefaultGUIManager().log("Directory: "+clustersPath+ 
 								"does not contain even a single cluster file.", "error", true);
 					}
 					
 					RClusteringParserToXLS r = new abyss.files.clusters.RClusteringParserToXLS();
 					r.extractAllRClusteringToXLS(clustersPath, clustersPath+"//ClustersSummary.xls");
 					
 					File test = new File(clustersPath+"//ClustersSummary.xls");
 					if(test.exists()) {
 						FileFilter filter[] = new FileFilter[1];
 						filter[0] = new ExtensionFileFilter(".xls - Excel 2003",  new String[] { "XLS" });
 						String newLocation = Tools.selectFileDialog(dirPath, filter, "", "");
 						if(newLocation.equals("")) { //czy chcemy przenieœæ plik w inne miejsce
 							//leave it in cluster folder
 							GUIManager.getDefaultGUIManager().log("Exporting table succeed. Created file: "
 									+clustersPath+"//ClustersSummary.xls", "text", true);
 						} else {
 							if(!newLocation.contains(".xls"))
 								newLocation += ".xls";
 							Tools.copyFileByPath(clustersPath+"//ClustersSummary.xls", newLocation);
 							test.delete(); //kasujemy oryginalny
 							GUIManager.getDefaultGUIManager().log("Exporting table succeed. Created file: "
 									+newLocation, "text", true);
 						}
 						
 					} else {
 						String msg = "Unknown error, excel file does not exist.";
 						GUIManager.getDefaultGUIManager().log(msg, "error", true);
 					}
 				} catch (Exception e){
 					String msg = "Excel export procedure failed for directory: "+clustersPath;
 					GUIManager.getDefaultGUIManager().log(msg, "error", true);
 					GUIManager.getDefaultGUIManager().log(e.getMessage(), "error", true);
 				}  
 				
 			}
 		});
        excelExport.setAlignmentX(Component.CENTER_ALIGNMENT);
        textPanel.add(excelExport);
         
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
        tmp.setFont(new Font("Arial", Font.PLAIN, 9));
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
     * Metoda ustawia œcie¿kê dostêpu do katalogu klastrów.
     * @param path String - œcie¿ka do katalogu
     */
    public void setClusterPath(String path) {
    	clustersPath = path;
    }
    
    /**
     * Metoda pomocnicza konstruktora, tworzy panel z tabel¹ danych na 15 kolumn.
     * 7 x 8 = 56 podtabel klastrowañ, standardowy przypadek.
     * @return JPanel - panel zawieraj¹cy tabelê danych
     */
    private JPanel createTablePanelCase56() {
    	JPanel main = new JPanel();
    	main.setLayout(new BorderLayout());

    	model = new DefaultTableModel();
        model.addColumn("Column1");
        model.addColumn("Column2");
        model.addColumn("Column3");
        model.addColumn("Column4");
        model.addColumn("Column5");
        model.addColumn("Column6");
        model.addColumn("Column7");
        model.addColumn("Column8");
        model.addColumn("Column9");
        model.addColumn("Column10");
        model.addColumn("Column11");
        model.addColumn("Column12");
        model.addColumn("Column13");
        model.addColumn("Column14");
        model.addColumn("Column15");
        
        table = new JTable(model);
        //table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);
        table.setDefaultRenderer(Object.class, tabRenderer); // 0 - case 56
        
        table.addMouseListener(new MouseAdapter() { //listener klikniêæ
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	JTable target = (JTable)e.getSource();
          	    	int row = target.getSelectedRow();
          	    	int column = target.getSelectedColumn();
          	    	int sub = subRowsSize;
          	    	if(column != 0 && row % (sub+1) != 0) { // NIE dla I kolumny i wierszy nag³ówkowych
          	    		try {
          	    			//tutaj dzieje siê magia na liczbach - jednako¿ dzieje siê prawid³owo, dlatego
          	    			//lepiej tutaj niczego nie zmieniaæ we wzorach
          	    			int clusterNumber = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
          	    			int newC = 0;
          	    			if(column % 2 == 1)
          	    				newC = column + 1;
          	    			else
          	    				newC = column;
          	    			int algID = (newC / 2)-1; //1-2 ->0, 3,4 ->1, etc.
          	    			
          	    			//obliczanie numery wiersza nag³ówego nad klikniêtym wierszem:
          	    			int headerRowNumber = row - (clusterNumber-1);
          	    			//zmiana na nr miary, pierwszy blok, to miara 0, nastêpny 1, itd. a¿ do 7
          	    			headerRowNumber /= (subRowsSize+1);
          	    			
          	    			Clustering omg = internalDataTables.getClustering((headerRowNumber*7)+algID, clusterNumber-2);
          	    			//new AbyssClusterSubWindow(myself, omg, 0);
          	    			new AbyssClusterSubWindow(myself, omg, 1);
          	    			
          	    			//AbyssClusterSubWindow w = new AbyssClusterSubWindow(myself, omg);
            	    	 } catch (Exception ex) {
            	    		  
            	    	 }
            	    	 
            	     }
            	      //JOptionPane.showMessageDialog(null,""+row+" "+column,"test",JOptionPane.INFORMATION_MESSAGE);
            	      // do some action if appropriate column
          	    }
          	 }
      	});
        
        table.getColumnModel().getColumn(0).setHeaderValue("Metric:");
        table.getColumnModel().getColumn(1).setHeaderValue("");
        table.getColumnModel().getColumn(2).setHeaderValue("UPGMA");
        table.getColumnModel().getColumn(3).setHeaderValue("");
        table.getColumnModel().getColumn(4).setHeaderValue("Centroid");
        table.getColumnModel().getColumn(5).setHeaderValue("");
        table.getColumnModel().getColumn(6).setHeaderValue("Complete");
        table.getColumnModel().getColumn(7).setHeaderValue("");
        table.getColumnModel().getColumn(8).setHeaderValue("McQuitty");
        table.getColumnModel().getColumn(9).setHeaderValue("");
        table.getColumnModel().getColumn(10).setHeaderValue("Median");
        table.getColumnModel().getColumn(11).setHeaderValue("");
        table.getColumnModel().getColumn(12).setHeaderValue("Single");
        table.getColumnModel().getColumn(13).setHeaderValue("");
        table.getColumnModel().getColumn(14).setHeaderValue("Ward");
        
        //rozmiary kolumn:
        for(int index=0; index<table.getColumnCount(); index++) {
        	if(index % 2 == 0) {
        		table.getColumnModel().getColumn(index).setPreferredWidth(60);
        	} else {
        		table.getColumnModel().getColumn(index).setPreferredWidth(20);
        	}
        }

        JScrollPane scrollPane = new JScrollPane(table);
        main.add(scrollPane);
        return main;
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
     * Metoda s³u¿¹ca do wype³nienia tabeli w przypadku kiedy mamy 56 klastrowañ.
     * @param littleBoy ClusteringInfoMatrix - g³ówna baza-tabela danych
     */
	public void handleStandardClusterTableCase56(ClusteringInfoMatrix littleBoy) {
    	internalDataTables = littleBoy;
    	subRowsSize = littleBoy.secondaryTablesMinNumber;
    	int checkSize = littleBoy.mainTablesNumber;
    	if(checkSize != 56) {
    		//problem
    		return;
    	} 
    	
    	tabRenderer.setMode(mode);  // !!!
    	tabRenderer.setSubRows(subRowsSize); // !!! z³a wartoœæ i tabela idzie w ....
    	//CLEAR OLD TABLE ROWS:
    	model.setNumRows(0);
    	table.revalidate();
    	
    	String[] metricName = { "Binary", "Correlation", "Pearson", "Canberra", "Euclidean", "Manhattan", "Maximum", "Minkowski" };
    	
    	for(int metric=0; metric <8; metric++) { //dla ka¿dej z oœmiu metryk:
    		String[] data = { metricName[metric],"0:","MSS","0:","MSS","0:","MSS","0:","MSS","0:","MSS","0:","MSS","0:","MSS"};
			model.addRow(data);
			
    		for(int rows=0; rows < subRowsSize; rows++) { //dla odpowiedniej liczby wierszy:
    			String[] dataRow = { "","","","","","","","","","","","","","",""}; //15 elementów
    			dataRow[0] = ""+(rows+2);
    			for(int alg=0; alg < 7; alg++ ) { // dla ka¿dego wiersza jedziemy po algorytmach
    				// Average, Centroid, Complete, McQuitty, Median, Single, Ward
        			int tableIndex = (metric*7)+alg; //która tabelka
            		
        			dataRow[1+alg*2] = ""+internalDataTables.getMatrix().get(tableIndex).get(rows).zeroClusters;
        			dataRow[1+alg*2+1] = ""+internalDataTables.getMatrix().get(tableIndex).get(rows).evalMSS;
            	}
    			model.addRow(dataRow);
			}
    	}
    }
    
    public void addDataRow15(String[] data) {
    	model.addRow(data);
    }
    
    /**
     * Klasa wewnêtrzna odpowiedzialna za rysowanie poszczególnych komórek.
     * @author MR
     *
     */
    class MyRenderer implements TableCellRenderer {
    	public DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();
    	private int mode = 0;
    	private int subRows = 0;
    	/**
    	 * Konstruktor domyœlny obiektów klasy MyRenderer.
    	 */
    	public MyRenderer() {
    		
    	}
    	
    	/**
    	 * Konstruktor obiektów klasy MyRenderer przyjmuj¹cy numer trybu rysowania.
    	 * @param mode int - tryb rysowania
    	 */
    	public MyRenderer(int mode, int rows) {
    		this(); //wywo³anie konstruktora domyœlnego
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
    	 * Przeci¹¿ona metoda odpowiedzialna za zwrócenie komórki tabeli.
    	 */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
        	if(mode == 0)
        		return paintCellsCase56(value, isSelected, hasFocus, row, column);
        	else
        		return paintCellsCase56(value, isSelected, hasFocus, row, column);
        }

        /**
         * Metoda trybu rysowania dla 56 klastrów, uruchamia siê DLA KA¯DEJ komórki dodawanej do tabeli.
         * @param value Object - wartoœæ do wpisania
         * @param isSelected boolean - czy komórka jest wybrana
         * @param hasFocus boolean - czy jest aktywna
         * @param row int - nr wiersza
         * @param column int - nr kolumny
         * @return Component - konkretnie: JTextField jako komórka tabeli
         */
		private Component paintCellsCase56(Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component renderer = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		    
		    if(column==0) {
		    	renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    	renderer.setBackground(Color.white);
		    } else {
		    	if(row == 0 || row % (subRows+1) == 0) { //wiersza nag³ówkowe nazw algorytmów
		    		renderer.setFont(new Font("Arial", Font.BOLD, 12));
		    	} else {
		    		float f = -1.0f;
		    		try {
		    			f = Float.parseFloat(value.toString());	
		    		} catch (Exception e) { //invalid parse
		    			
		    		}
		    		if(column % 2 != 0) { //parzyste id kolumn (id! bo samo kolumny s¹ nieparzyste)
		    			if(f >=0 && f < 5) {
		    				renderer.setBackground(new Color(51, 212, 62));
		    			} else if(f >= 5 && f < 10) {
		    				renderer.setBackground(new Color(231 ,242, 15));
		    			} else {
		    				renderer.setBackground(new Color(242, 52, 15));
		    			}
		    		} else {
		    			f = f * 100;
		    			renderer.setBackground(getSimpleColor(f));
		    		}
		    	}
		    }
		    return renderer;
		}
		
		/**
		 * Metoda zwraca kolor w zale¿noœci od skali w formie proste skali kolorów zieleñ-czerwieñ.
		 * @param power double - wartoœæ od 0 do 100;
		 * @return Color - kolor w zale¿noœci od skali
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
    
    private void initiateListeners() {
    	addWindowListener(new WindowAdapter() {
    		public void windowOpened(WindowEvent e) {
  	    		//System.out.println("Window Opened Event");
  	    	}
  	    	
  	    	public void windowClosing(WindowEvent e) {
  	  	    	//System.out.println("Window Closing Event");
  	  	    }

  	  	    public void windowClosed(WindowEvent e) {
  	  	    	//System.out.println("Window Close Event");
  	  	    }

  	  	    public void windowIconified(WindowEvent e) {
  	  	    	//System.out.println("Window Iconified Event");
  	  	    }

  	  	    public void windowDeiconified(WindowEvent e) {
	  	    	//System.out.println("Window Deiconified Event");
  	  	    }

  	  	    public void windowActivated(WindowEvent e) {
  	  	    	try {
  	  	    		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix() != null) {
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
  	  	    		}
  	  	    	} catch (Exception ex) {
  	  	    		spinnerClusters.setEnabled(false);
  	  	    	}
  	  	    	
  	  	    	System.out.println("Window Activated Event");
  	  	    }

  	  	    public void windowDeactivated(WindowEvent e) {
  	  	    	//System.out.println("Window Deactivated Event");
  	  	    }

  	  	    public void windowStateChanged(WindowEvent e) {
  	  	    	//System.out.println("Window State Changed Event");
  	  	    }

  	  	    public void windowGainedFocus(WindowEvent e) {
  	  	    	//System.out.println("Window Gained Focus Event");
  	  	    }

  	  	    public void windowLostFocus(WindowEvent e) {
  	  	    	//System.out.println("Window Lost Focus Event");
  	  	    }
    	});
    }
}
