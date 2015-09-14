package holmes.windows;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import holmes.clusters.ClusterDataPackage;
import holmes.clusters.Clustering;
import holmes.clusters.ClusteringExtended;
import holmes.darkgui.GUIManager;
import holmes.files.clusters.ClusterReader;
import holmes.files.clusters.ClusteringExcelWriter;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa tworząca okno informacyjne względem tabeli klastrów. Zawiera ono informacje o
 * konkretnych wybranym klastrowaniu.
 * @author MR
 *
 */
public class HolmesClusterSubWindow extends JFrame {
	private static final long serialVersionUID = 6818230680946396781L;
	private JFrame parentFrame;
	private Clustering clusteringMetaData;
	private String nL = "\n";
	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
	private JPanel editPanel; //główny panel okna
	private JScrollPane paneScrollPane; //panel scrollbar -> editPanel
	private JButton buttonExcel;
	private JButton buttonInjectCluster;
	private JButton buttonTexTable;
    
	private String clusterPath;
	private ClusteringExtended fullData = null;
	
	final WaitLayerUI layerUI = new WaitLayerUI();
	
	/**
	 * Konstruktor domyślny obiektu klasy HolmesClusterSubWindow.
	 */
	public HolmesClusterSubWindow() {
		
	}
	
	/**
	 * Główny konstruktor parametrowy okna klasy HolmesClusterSubWindow.
	 * @param parent HolmesClusters - obiekt okna wywołującego
	 * @param clusteringMetaData Clustering - dane do wyświetlenia
	 */
	public HolmesClusterSubWindow(HolmesClusters parent, Clustering dataPackage, int mode) {
		this();
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		clusterPath = parent.getClusterPath();
		this.clusteringMetaData = dataPackage;
		this.parentFrame = parent;
		parentFrame.setEnabled(false);
		
		if(mode==0) {
			initiateSimpleMode(parent, dataPackage);
		} else {
			initiateExtendedMode(parent, dataPackage);
		}
	}

	/**
	 * Metoda tworzy najprostszą wersję okna informacyjnego.
	 * @param parent HolmesClusters - okno wywołujące
	 * @param dataPackage Clustering - dane do wyświetlenia
	 */
	private void initiateSimpleMode(HolmesClusters parent, Clustering dataPackage) {
		JTextArea area = new JTextArea();
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		add(new JScrollPane(area));
		setSize(new Dimension(450, 300));

		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
		
		setLocationRelativeTo(null);
		setVisible(true);
		
		if(clusteringMetaData != null) {
			area.append("Algorithm name: "+clusteringMetaData.algorithmName+nL);
			area.append("Metric name: "+clusteringMetaData.metricName+nL);
			area.append(nL);
			area.append("Invariants number: "+clusteringMetaData.invNumber+nL);
			area.append("Clusters number: "+clusteringMetaData.clusterNumber+nL);
			area.append("Zero-clusters: "+clusteringMetaData.zeroClusters+nL);
			area.append("MSS evaluation: "+clusteringMetaData.evalMSS+nL);
			area.append("C-H evaluation: "+clusteringMetaData.evalCH+nL);
			area.append(nL);
			
			//kolejne wiersze z MSS dla klastrów:
			for(int i=0; i<clusteringMetaData.clusterNumber; i++) {
				String value = getMSSFormatted(clusteringMetaData.clusterMSS.get(i));
				area.append("Cluster "+i+" size: "+clusteringMetaData.clusterSize.get(i)+"  MSS: "+value+nL);
				//area.append("Cluster "+i+" size: "+clusteringMetaData.clusterSize[i]+"  MSS: "+clusteringMetaData.clusterMSS[i]+nL);
			}
			area.append(nL);
			for(int i=0; i<6; i++) {
				//area.append(clusteringMetaData.vectorMSS[i]+ " | ");
				area.append(clusteringMetaData.vectorMSS.get(i)+ " | ");
			}
		}
	}
	
	/**
	 * Formatuje wartość do liczby z 8 miejscami po przecinku.
	 * @param clusterMSS double - miara MSS
	 * @return String - formatowany tekst miary
	 */
	private String getMSSFormatted(double clusterMSS) {
        DecimalFormat df = new DecimalFormat("#.########");
        String txt = "";
        txt += df.format(clusterMSS);
        txt = txt.replace(",", ".");
        if(txt.indexOf(".") == -1)
        	txt += ".";
        int size = txt.length();
        for(int i=0; i<10-size; i++) {
        	txt += "0";
        }
        
		return txt;
	}

	/**
	 * Metoda tworzy podstawową wersję okna informacyjnego.
	 * @param parent HolmesClusters - okno wywołujśce
	 * @param dataPackage Clustering - dane do wyświetlenia
	 */
	private void initiateExtendedMode(HolmesClusters parent, Clustering dataPackage) {
		this.setTitle("Details for "+dataPackage.clusterNumber + " clusters from " 
				+dataPackage.algorithmName + "/"+dataPackage.metricName);

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(600, 600));
		setMaximumSize(new Dimension(600, 600));
		setResizable(false);
		
		JPanel tablePanel = createEditor();
		tablePanel.setOpaque(true); 
	    JLayer<JPanel> jlayer = new JLayer<JPanel>(tablePanel, layerUI);
	    
	    add(jlayer);

		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
		
		if(clusteringMetaData != null) {
			try {
				doc.insertString(doc.getLength(), Tools.setToSize("Algorithm name: ",20,false), doc.getStyle("regular"));
				doc.insertString(doc.getLength(), clusteringMetaData.algorithmName+nL, doc.getStyle("bold"));
		    	
				doc.insertString(doc.getLength(), Tools.setToSize("Metric name: ",20,false), doc.getStyle("regular"));
				doc.insertString(doc.getLength(), clusteringMetaData.metricName+nL, doc.getStyle("bold"));
				
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize("Invariants number: ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.invNumber+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize("Clusters number: ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.clusterNumber+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize("Zero-clusters: ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.zeroClusters+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize("MSS evaluation: ",20,false), doc.getStyle("regular"));
		    	String value = getMSSFormatted(clusteringMetaData.evalMSS);
		    	doc.insertString(doc.getLength(), value+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize("C-H evaluation: ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.evalCH+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	//dane o klastrach
		    	for(int i=0; i<clusteringMetaData.clusterNumber; i++) {
		    		doc.insertString(doc.getLength(), "Cluster "
		    				+Tools.setToSize((i+1)+"",3,true)+" invariants: ", doc.getStyle("regular"));

		    		doc.insertString(doc.getLength(), 
		    				Tools.setToSize(clusteringMetaData.clusterSize.get(i)+"",4,true), doc.getStyle("bold"));
		    		
		    		doc.insertString(doc.getLength(),"  MSS: ", doc.getStyle("regular"));
		    		
		    		value = getMSSFormatted(clusteringMetaData.clusterMSS.get(i));
		    		
		    		doc.insertString(doc.getLength(), value+nL,
				    		doc.getStyle(returnStyle(clusteringMetaData.clusterMSS.get(i))));
				    
		    	}
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	String sepSpace = "";
		    	if(clusteringMetaData.vectorMSS.get(0) < 0)
		    		sepSpace = " ";
		    	doc.insertString(doc.getLength(), sepSpace+" Min.  | 1st Qu.| Median |  Mean  | 3rd Qu.|  Max."+nL, doc.getStyle("bold"));
		    	for(int i=0; i<6; i++) {
		    		//doc.insertString(doc.getLength(), addSpaceRight(clusteringMetaData.vectorMSS[i]+"",6)+ " | ", doc.getStyle("bold"));
		    		doc.insertString(doc.getLength(), Tools.setToSize(clusteringMetaData.vectorMSS.get(i)+"",6,false)+ " | ", doc.getStyle("bold"));
		    	}
		    	
		    	textPane.setCaretPosition(0);
			} catch (Exception ex1) {
				
			}
			
		}
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Metoda zwraca styl wyświetlania (kolory) w zależności od wartości liczby
	 * @param value float - od -1 do +1.0
	 * @return String - nazwa stylu - koloru
	 */
	private String returnStyle(float value) {
		value *= 100;
		if(value < 30) {
	    	return "mark1";
	    } else if(value <40) {
	    	return "mark2";
	    } else if(value <50) {
	    	return "mark3";
	    } else if(value <60) {
	    	return "mark4";
	    } else if(value <75) {
	    	return "mark5";
	    } else {
	    	return "mark6";
	    }
	}
	
	/**
	 * Metoda pomocnicza, tworzy style dla wypisywanych danych.
	 * @param doc StyledDocument - obiekt dokumentu przechowującego style
	 */
	private void addStylesToDocument(StyledDocument doc) {
        Style baseStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", baseStyle);
        StyleConstants.setFontFamily(baseStyle, "monospaced");
        StyleConstants.setFontSize(baseStyle, 16);
        
        Style mark6 = doc.addStyle("mark6", baseStyle);
        StyleConstants.setForeground(mark6, new Color(1, 153, 19));
        StyleConstants.setBold(mark6, true);
        
        Style mark5 = doc.addStyle("mark5", baseStyle);
        StyleConstants.setForeground(mark5, new Color(12, 232, 26));
        StyleConstants.setBold(mark5, true);
        
        Style mark4 = doc.addStyle("mark4", baseStyle);
        StyleConstants.setForeground(mark4, new Color(173, 217, 13));
        StyleConstants.setBold(mark4, true);
        
        Style mark3 = doc.addStyle("mark3", baseStyle);
        StyleConstants.setForeground(mark3, new Color(240, 171, 12));
        StyleConstants.setBold(mark3, true);
        
        Style mark2 = doc.addStyle("mark2", baseStyle);
        StyleConstants.setForeground(mark2, new Color(240, 92, 12));
        StyleConstants.setBold(mark2, true);
        
        Style mark1 = doc.addStyle("mark1", baseStyle);
        StyleConstants.setForeground(mark1, new Color(250, 2, 2));
        StyleConstants.setBold(mark1, true);

        Style bold = doc.addStyle("bold", baseStyle);
        StyleConstants.setBold(bold, true);
        
        Style s = doc.addStyle("italic2", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold2", regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle("time", regular);
        StyleConstants.setForeground(s, Color.darkGray);
        StyleConstants.setBold(s, true);
	}
	
	/**
	 * Metoda pomocnicza, tworzy obiekt edytora.
	 * @return JTextPane - obiekt edytora
	 */
	private JTextPane createTextPane() {
		String initString = "Clustering details:"+newline;
	    JTextPane txtPane = new JTextPane();
	    doc = txtPane.getStyledDocument();
	    addStylesToDocument(doc);
	    try {
	        doc.insertString(doc.getLength(), initString, doc.getStyle("regular"));
	    } catch (BadLocationException ble) {
	        System.err.println("Couldn't insert initial text into text pane.");
	    }
	    return txtPane;
	}
	
	/**
	 * Metoda pomocnicza tworząca Panel widoku klastrowania z przyciskami
	 * @return JPanel - główny panel okna
	 */
	private JPanel createEditor() {
		editPanel = new JPanel();
		//editPanel.setBorder(BorderFactory.createTitledBorder("SSSSSSS"));
		editPanel.setLayout(null);
		editPanel.setBounds(0, 0, 500, 500);
        
           
        textPane = createTextPane();
        textPane.setEditable(false);
        paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setBounds(5, 5, 585, 500);
        editPanel.add(paneScrollPane);

        buttonExcel = new JButton(">> Excel", Tools.getResIcon48("/icons/clustWindow/buttonExportSingleToExcel.png"));
        buttonExcel.setBounds(5, 510, 190, 50);
        //button.setBounds(new Rectangle(150, 40));
        buttonExcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				layerUI.start();
				turnOffButtons();
				exportDataToExcel();
				turnOnButtons();
				layerUI.stop();
			}	
		});
        editPanel.add(buttonExcel);
        
        buttonInjectCluster = new JButton(">> Net structure", Tools.getResIcon48("/icons/clustWindow/buttonSendToAbyss.png"));
        buttonInjectCluster.setBounds(200, 510, 190, 50);
        buttonInjectCluster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				layerUI.start();
				turnOffButtons();
				exportToHolmes();
				turnOnButtons();
				layerUI.stop();
			}	
		});
        editPanel.add(buttonInjectCluster);
        
        buttonTexTable = new JButton(">> Cluster table", Tools.getResIcon48("/icons/menu/menu_exportTex.png"));
        buttonTexTable.setBounds(400, 510, 190, 50);
        buttonTexTable.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				layerUI.start();
				turnOffButtons();
				exportToLatex();
				turnOnButtons();
				layerUI.stop();
			}	
		});
        editPanel.add(buttonTexTable);
        /*
        buttonInjectCluster = new JButton("test", 
        		Tools.getResIcon48(""));
        buttonInjectCluster.setBounds(400, 510, 190, 50);
        buttonInjectCluster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				layerUI.start();
			}	
		});
        editPanel.add(buttonInjectCluster);
        */
        editPanel.repaint();
        return editPanel;
	}

	/**
	 * Metoda włącza dostęp do przycisków.
	 */
	protected void turnOnButtons() {
		buttonExcel.setEnabled(true);
		buttonInjectCluster.setEnabled(true);
		buttonTexTable.setEnabled(true);
	}

	/**
	 * Metoda na czas trwania operacji klastrowania wyłącza dostęp do przycisków.
	 */
	protected void turnOffButtons() {
		buttonExcel.setEnabled(false);
		buttonInjectCluster.setEnabled(false);
		buttonTexTable.setEnabled(false);
	}

	/**
	 * Metoda usuwająca pliki powstałe w wyniku tworzenie konkretnego klastrowania.
	 * @param resultFiles String[5] - 5 plików, usuwanie od 2 do 5 (1szy to cluster.csv)
	 */
	private void deleteTmpFile(String[] resultFiles) {
		try {
			for(int i=1; i<resultFiles.length; i++) { // z wyjątkiem pliku csv
				File del = new File(resultFiles[i]);
				if(del.exists())
					del.delete();
			}
			
			//dodatkowo:
			File excelTmp = new File("tmp\\testSheets.xls");
			if(excelTmp.exists())
				excelTmp.delete();
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Removing temporary files failed.", "error", true);
		}
	}
	
	/**
	 * Metdoa odpowiedzialna za zapis pliku excela oraz plików powstałych w wyniku działania
	 * procesu tworzenia konkretnego klastrowania dla sieci.
	 * @param fullData ClusteringExtended - pakiet danych dla pliku excel
	 * @param files String[] - pliki pomocnicze
	 * @return boolean - true, jeśli wszystko się udało
	 */
	private boolean saveAllFiles(ClusteringExtended fullData, String[] files) {
		String whereExcelIs = saveExcelOnly(fullData);
		if(whereExcelIs == null)
			return false;
		
		File test = new File(whereExcelIs);
		String excelDestinationFolder = test.getAbsolutePath();
		excelDestinationFolder = excelDestinationFolder.
			    substring(0,excelDestinationFolder.lastIndexOf(File.separator));
		try {
			//copy cluster pdf where excel already is:
			File pdf1 = new File(files[3]);
			if(pdf1.exists()) {
				String pdf1Name = pdf1.getName();
				Tools.copyFileByPath(files[3], excelDestinationFolder+"\\"+pdf1Name);
			}
			
			//copy cluster dendrogram where excel already is:
			File pdf2 = new File(files[4]);
			if(pdf2.exists()) {
				String pdf2Name = pdf2.getName();
				Tools.copyFileByPath(files[4], excelDestinationFolder+"\\"+pdf2Name);
			}

			//copy cluster dendrogram where excel already is:
			File mctFile = new File(files[2]);
			if(mctFile.exists()) {
				String mcfFileName = mctFile.getName();
				Tools.copyFileByPath(files[2], excelDestinationFolder+"\\"+mcfFileName);
		}
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("File copy error.", "error", true);
			return false;
		}
		return true;
	}
	
	/**
	 * Metoda odpowiedzialna za zapis wyników klastrowania do pliku excela.
	 * @param fullData ClusteringExtended - pakiet danych dla pliku .xls
	 * @return String - ścieżka do pliku excela
	 */
	private String saveExcelOnly(ClusteringExtended fullData) {
		ClusteringExcelWriter ew = new ClusteringExcelWriter(0, fullData, "tmp\\testSheets.xls");
		if(ew.isSuccess() == false)
			return null;
		
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Microsoft Excel 97/2000/XP/2003 (.xls)", new String[] { "XLS" });
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Save clusters as Excel document", "");
		if(selectedFile.equals(""))
			return null;
		
		if(!selectedFile.contains(".xls"))
			selectedFile += ".xls";
		//File file = new File(selectedFile);
		try {
			Tools.copyFileByPath("tmp//testSheets.xls", selectedFile);
		} catch (IOException e) {
			GUIManager.getDefaultGUIManager().log("Copying tmp\\testSheets.xls to "+selectedFile
					+" failed." , "error", true);
			return null;
		}
		return selectedFile;
	}

	/**
	 * Metoda obsługuje zdarzenie kliknięcia na przycisk eksportu danych do .xls
	 */
	protected void exportDataToExcel() {
		//generowanie klastrowania:
		String targetDir = getInvariantsCSVLocation();
		if(targetDir == null) {
			JOptionPane.showMessageDialog(null, "Operation failed. Unable to obtain invariants CSV file.", 
					"Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		String resultFiles[] = GUIManager.getDefaultGUIManager().io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			// czytanie wyników:
			fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(fullData==null) {
				GUIManager.getDefaultGUIManager().log("Reading data files failed. Extraction to Excel cannot begin.", "error", true);
				return;
			}
		} else {
			GUIManager.getDefaultGUIManager().log("Error accured while extracting data. While "
					+ "contacting authors about the problem please attach *all* three files mentioned in"
					+ "this log above this message.", "error", true);
			return;
		}
		
		Object[] options = {"Save data files and Excel file", "Make Excel file only",};
		int n = JOptionPane.showOptionDialog(null,
						"Clustering data extraction succeed. What to do now?",
						"Choose output file", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == 0) { //both files
			boolean success = saveAllFiles(fullData, resultFiles);
			if(success) {
				deleteTmpFile(resultFiles);
			}
		} else { //only excel
			String path = saveExcelOnly(fullData);
			if(path != null) {
				deleteTmpFile(resultFiles);
			}
		}
	}

	/**
	 * Metoda ta zwraca ścieżki do katalogu, w którym znajduje się plik invariantów o nazwie cluster.csv.
	 * @return String - ścieżka do katalogu
	 */
	protected String getInvariantsCSVLocation() {
		String targetDir = "";
		Object[] options = {"Use computed invariants", "Load CSV invariant file",};
		int n = JOptionPane.showOptionDialog(null,
						"Select CSV invariants manually or export from net computed invariants?",
						"Choose output file", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == 0) {
			targetDir = selectionOfSource();
			File x = new File(targetDir);
			String name = x.getName();
			String path = Tools.getFilePath(x);
			if(!name.equals("cluster.csv")) {
				try {
					Tools.copyFileByPath(x.getAbsolutePath(), path+"cluster.csv");
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			targetDir = path;
		} else {
			if(clusterPath == null || clusterPath.equals("")) {
				JOptionPane.showMessageDialog(null, "Please select csv file containing invariants.", 
						"Selection",JOptionPane.INFORMATION_MESSAGE);
				
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter("Invariants csv file (.csv)", new String[] { "CSV" });
				String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
				String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select", "", "");
				if(selectedFile.equals(""))
					return null;
				
				File x = new File(selectedFile);
				String name = x.getName();
				String path = Tools.getFilePath(x);
				if(!name.equals("cluster.csv")) {
					try {
						Tools.copyFileByPath(x.getAbsolutePath(), path+"cluster.csv");
						
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				targetDir = path;
			} else {
				targetDir = clusterPath;
			}
		}
		
		return targetDir;
	}
	
	private String selectionOfSource() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix() == null) { //brak inwariantów
			JOptionPane.showMessageDialog(null, "No invariants computed! Please select CSV invariants file!", 
					"Warning",JOptionPane.WARNING_MESSAGE);
			
			FileFilter[] filters = new FileFilter[1];
			filters[0] = new ExtensionFileFilter("CSV invariants file (.csv)",  new String[] { "CSV" });
			String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select CSV", "Select CSV file", "");
			
			if(selectedFile.equals(""))
				return null;
			else
				return selectedFile;
		} else {
			{
				//generowanie CSV, uda się, jeśli inwarianty istnieją
				String CSVfilePath = GUIManager.getDefaultGUIManager().getTmpPath() + "cluster.csv";
				int result = GUIManager.getDefaultGUIManager().getWorkspace().getProject().saveInvariantsToCSV(CSVfilePath, true);
				if(result == -1) {
					String msg = "Exporting invariants into CSV file failed. \nCluster procedure cannot begin without invariants.";
					JOptionPane.showMessageDialog(null,msg,	"CSV export error",JOptionPane.ERROR_MESSAGE);
					GUIManager.getDefaultGUIManager().log(msg, "error", true);
					return null;
				}
				
				return CSVfilePath;
			} 
		}
	}
	
	/**
	 * Metoda obsługuje zdarzenie kliknięcia przycisku eksportu danych do okna głównego.
	 */
	protected void exportToHolmes() {
		boolean proceed = true;
		if(fullData != null) {
			//ask what to do
			Object[] options = {"Use existing data", "Create anew",};
			int n = JOptionPane.showOptionDialog(null,
							"Detailed clustering data already exists. Use it or create anew?",
							"Data package found", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				int transNumber = fullData.transNames.length-1;
				int netTransNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
				if(transNumber != netTransNumber) {
					JOptionPane.showMessageDialog(null, "Transition number discrepancy! \n"
							+ "Data table transition number: "+transNumber
							+"\nLoaded network transition number: "+netTransNumber
							+"\nExporting cannon proceed.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				//TODO:
				ClusterDataPackage dataCore = new ClusterDataPackage();
				dataCore.dataMatrix = fullData.getClusteringColored(); //najbardziej czasochłonne
				dataCore.clustersInvariants = fullData.clustersInv;
				dataCore.clMSS = fullData.metaData.clusterMSS;
				dataCore.algorithm = fullData.metaData.algorithmName;
				dataCore.metric = fullData.metaData.metricName;
				dataCore.clNumber = fullData.metaData.clusterNumber;
				dataCore.clSize = new ArrayList<Integer>(fullData.metaData.clusterSize);
				GUIManager.getDefaultGUIManager().showClusterSelectionBox(dataCore); //wyślij do Holmes (JFrame)
				JOptionPane.showMessageDialog(null, "Operation successfull. Clusters are ready to show.", 
						"Status",JOptionPane.INFORMATION_MESSAGE);
				proceed = false;
			}
		}
		
		if(!proceed)
			return;
		
		String targetDir = getInvariantsCSVLocation();
		if(targetDir == null) {
			JOptionPane.showMessageDialog(null, "Operation failed. Unable to obtain invariants CSV file.", 
					"Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		//generowanie klastrowania:
		String resultFiles[] = GUIManager.getDefaultGUIManager().io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(fullData==null) {
				GUIManager.getDefaultGUIManager().log("Reading data files failed. Sending to net cannot begin.", "error", true);
				return;
			}
			
			int transNumber = fullData.transNames.length-1;
			int netTransNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
			if(transNumber != netTransNumber) {
				JOptionPane.showMessageDialog(null, "Transition number discrepancy! \n"
						+ "Data table transition number: "+transNumber
						+"\nLoaded network transition number: "+netTransNumber
						+"\nExporting cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			ClusterDataPackage dataCore = new ClusterDataPackage();
			dataCore.dataMatrix = fullData.getClusteringColored(); //najbardziej czasochłonne
			dataCore.clustersInvariants = fullData.clustersInv;
			dataCore.clMSS = fullData.metaData.clusterMSS;
			dataCore.algorithm = fullData.metaData.algorithmName;
			dataCore.metric = fullData.metaData.metricName;
			dataCore.clNumber = fullData.metaData.clusterNumber;
			dataCore.clSize = new ArrayList<Integer>(fullData.metaData.clusterSize);
			
			GUIManager.getDefaultGUIManager().showClusterSelectionBox(dataCore); //wyślij do Holmes (JFrame)
			deleteTmpFile(resultFiles);
			
			JOptionPane.showMessageDialog(null, "Operation successfull. Clusters are ready to show.", 
					"Status",JOptionPane.INFORMATION_MESSAGE);
		} else {
			GUIManager.getDefaultGUIManager().log("Error accured while extracting data. While "
					+ "contacting authors about the problem please attach *all* three files mentioned in"
					+ "log above this message.", "error", true);
		}
	}

	/**
	 * Metoda obsługuje zdarzenie kliknięcia przycisku eksportu danych do okna głównego.
	 */
	protected void exportToLatex() {
		boolean proceed = true;
		
		if(fullData != null) {
			//ask what to do
			Object[] options = {"Use existing data", "Create anew",};
			int n = JOptionPane.showOptionDialog(null,
							"Detailed clustering data already exists. Use it or create anew?",
							"Data package found", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				int transNumber = fullData.transNames.length-1;
				int netTransNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().size();
				if(transNumber != netTransNumber) {
					JOptionPane.showMessageDialog(null, "Transition number discrepancy! \n"
							+ "Data table transition number: "+transNumber
							+"\nLoaded network transition number: "+netTransNumber
							+"\nExporting cannot proceed.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				GUIManager.getDefaultGUIManager().tex.writeCluster(fullData);
				
				JOptionPane.showMessageDialog(null, "Operation successfull. Clusters exportet to files", 
						"Status",JOptionPane.INFORMATION_MESSAGE);
				proceed = false;
			}
		}
		
		if(proceed == false)
			return;
		
		String targetDir = getInvariantsCSVLocation();
		if(targetDir == null) {
			JOptionPane.showMessageDialog(null, "Operation failed. Unable to obtain invariants CSV file.", 
					"Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		//generowanie klastrowania:
		String resultFiles[] = GUIManager.getDefaultGUIManager().io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(fullData==null) {
				GUIManager.getDefaultGUIManager().log("Reading data files failed. Extraction to tables cannot begin.", "error", true);
				return;
			}
			
			GUIManager.getDefaultGUIManager().tex.writeCluster(fullData);
			deleteTmpFile(resultFiles);
			JOptionPane.showMessageDialog(null, "Operation successfull. Clusters exportet to files", 
					"Status",JOptionPane.INFORMATION_MESSAGE);
		} else {
			GUIManager.getDefaultGUIManager().log("Error accured while extracting data. While "
					+ "contacting authors about the problem please attach *all* three files mentioned in"
					+ "this log above this message.", "error", true);
		}
	}
}
