package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
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

import abyss.clusters.ClusterDataPackage;
import abyss.clusters.Clustering;
import abyss.clusters.ClusteringExtended;
import abyss.darkgui.GUIManager;
import abyss.files.clusters.ClusterReader;
import abyss.files.clusters.ExcelWriter;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa tworząca okno informacyjne względem tabeli klastrów. Zawiera ono informacje o
 * konkretnych wybranym klastrowaniu.
 * @author MR
 *
 */
public class AbyssClusterSubWindow extends JFrame {
	private static final long serialVersionUID = 6818230680946396781L;
	//private static final long serialVersionUID = -5663572683374020754L;
	private JFrame parentFrame;
	private Clustering clusteringMetaData;
	private String nL = "\n";
	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
	private JPanel editPanel; //główny panel okna
	private JScrollPane paneScrollPane; //panel scrollbar -> editPanel
	
	private String clusterPath;
	
	/**
	 * Konstruktor domyślny obiektu klasy AbyssClusterSubWindow.
	 */
	public AbyssClusterSubWindow() {
		
	}
	
	/**
	 * Główny konstruktor parametrowy okna klasy AbyssClusterSubWindow.
	 * @param parent AbyssClusters - obiekt okna wywołującego
	 * @param clusteringMetaData Clustering - dane do wyświetlenia
	 */
	public AbyssClusterSubWindow(AbyssClusters parent, Clustering dataPackage, int mode) {
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
	 * @param parent AbyssClusters - okno wywołujące
	 * @param dataPackage Clustering - dane do wyświetlenia
	 */
	private void initiateSimpleMode(AbyssClusters parent, Clustering dataPackage) {
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
			for(int i=0; i<clusteringMetaData.clusterNumber; i++) {
				area.append("Cluster "+i+" size: "+clusteringMetaData.clusterSize.get(i)+"  MSS: "+clusteringMetaData.clusterMSS.get(i)+nL);
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
	 * Metoda tworzy podstawową wersję okna informacyjnego.
	 * @param parent AbyssClusters - okno wywołujśce
	 * @param dataPackage Clustering - dane do wyświetlenia
	 */
	private void initiateExtendedMode(AbyssClusters parent, Clustering dataPackage) {
		this.setTitle("Details for "+dataPackage.clusterNumber + " clusters from " 
				+dataPackage.algorithmName + "/"+dataPackage.metricName);

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(600, 600));
		setMaximumSize(new Dimension(600, 600));
		setResizable(false);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		JPanel tablePanel = createEditor();
		tablePanel.setOpaque(true); 

		setContentPane(tablePanel);
		pack();

		addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentFrame.setEnabled(true);
		    }
		});
		
		if(clusteringMetaData != null) {
			try {
				doc.insertString(doc.getLength(), addSpaceRight("Algorithm name: ",20), doc.getStyle("regular"));
				doc.insertString(doc.getLength(), clusteringMetaData.algorithmName+nL, doc.getStyle("bold"));
		    	
				doc.insertString(doc.getLength(), addSpaceRight("Metric name: ",20), doc.getStyle("regular"));
				doc.insertString(doc.getLength(), clusteringMetaData.metricName+nL, doc.getStyle("bold"));
				
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	doc.insertString(doc.getLength(), addSpaceRight("Invariants number: ",20), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.invNumber+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), addSpaceRight("Clusters number: ",20), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.clusterNumber+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), addSpaceRight("Zero-clusters: ",20), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.zeroClusters+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), addSpaceRight("MSS evaluation: ",20), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.evalMSS+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), addSpaceRight("C-H evaluation: ",20), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.evalCH+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	//dane o klastrach
		    	for(int i=0; i<clusteringMetaData.clusterNumber; i++) {
		    		doc.insertString(doc.getLength(), "Cluster "+addSpaceLeft((i+1)+"",3)+" invariants: ",
		    				doc.getStyle("regular"));
		    		//doc.insertString(doc.getLength(), addSpaceLeft(clusteringMetaData.clusterSize[i]+"",4), doc.getStyle("bold"));
		    		doc.insertString(doc.getLength(), addSpaceLeft(clusteringMetaData.clusterSize.get(i)+"",4), doc.getStyle("bold"));
		    		
		    		doc.insertString(doc.getLength(),"  MSS: ", doc.getStyle("regular"));
		    		//doc.insertString(doc.getLength(), addSpaceLeft(clusteringMetaData.clusterMSS[i]+"",12)+nL,
		    		//		doc.getStyle(returnStyle(clusteringMetaData.clusterMSS[i])));
		    		doc.insertString(doc.getLength(), addSpaceLeft(clusteringMetaData.clusterMSS.get(i)+"",12)+nL,
				    		doc.getStyle(returnStyle(clusteringMetaData.clusterMSS.get(i))));
				    		
		    		//doc.insertString(doc.getLength(), "Cluster "+i+" size: "+data.clusterSize[i]+"  MSS: "+data.clusterMSS[i]+nL, doc.getStyle("regular"));
		    	}
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	// 
		    	String sepSpace = "";
		    	//if(clusteringMetaData.vectorMSS[0] < 0)
		    	if(clusteringMetaData.vectorMSS.get(0) < 0)
		    		sepSpace = " ";
		    	doc.insertString(doc.getLength(), sepSpace+" Min.  | 1st Qu.| Median |  Mean  | 3rd Qu.|  Max."+nL, doc.getStyle("bold"));
		    	for(int i=0; i<6; i++) {
		    		//doc.insertString(doc.getLength(), addSpaceRight(clusteringMetaData.vectorMSS[i]+"",6)+ " | ", doc.getStyle("bold"));
		    		doc.insertString(doc.getLength(), addSpaceRight(clusteringMetaData.vectorMSS.get(i)+"",6)+ " | ", doc.getStyle("bold"));
		    	}
		    	
		    	textPane.setCaretPosition(0);
			} catch (Exception ex1) {
				
			}
			
		}
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	//TODO: Tools się kłania
	/**
	 * Metoda pomocnicza, dodaje spacje na prawo od tekstu.
	 * @param text String - tekst do rozszerzenia
	 * @param value int - maksymalna szerokość tekstu
	 * @return String - nowy tekst
	 */
	private String addSpaceRight(String text, int value) {
		String result = text;
		String spaces = "";
		if(result.length() < value) {
			for(int i=0; i< value-result.length(); i++ )
				spaces += " ";
		}
		return result+spaces;
	}
	//TODO: Tools się kłania
	/**
	 * Metoda pomocnicza, dodaje spacje na lewo od tekstu.
	 * @param text String - tekst do rozszerzenia
	 * @param value int - maksymalna szerokość tekstu
	 * @return String - nowy tekst
	 */
	private String addSpaceLeft(String text, int value) {
		String result = text;
		String spaces = "";
		if(result.length() < value) {
			for(int i=0; i< value-result.length(); i++ )
				spaces += " ";
		}
		
		return spaces+result;
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
        
        
        JButton button = new JButton("Export clustering", 
        		Tools.getResIcon48("/icons/clustWindow/buttonExportSingleToExcel.png"));
        button.setBounds(5, 510, 240, 50);
        //button.setBounds(new Rectangle(150, 40));
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				exportDataToExcel();
			}	
		});
        editPanel.add(button);
        
        JButton buttonInjectCluster = new JButton("Send to net", 
        		Tools.getResIcon48("/icons/clustWindow/button_sendToAbyss.png"));
        buttonInjectCluster.setBounds(350, 510, 240, 50);
        buttonInjectCluster.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				exportToAbyss();
			}	
		});
        editPanel.add(buttonInjectCluster);
        
        editPanel.repaint();
        return editPanel;
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
		ExcelWriter ew = new ExcelWriter(0, fullData, "tmp\\testSheets.xls");
		if(ew.isSuccess() == false)
			return null;
		
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Microsoft Excel 97/2000/XP/2003 (.xls)", new String[] { "XLS" });
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "Save clusters as Excel document");
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
		String targetDir = getCSVLocation();
		if(targetDir == null)
			return;
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		//generowanie klastrowania:
		String resultFiles[] = GUIManager.getDefaultGUIManager().io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			// czytanie wyników:
			ClusteringExtended fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(fullData==null) {
				GUIManager.getDefaultGUIManager().log("Reading data files failed. Extraction to Excel cannot begin.", "error", true);
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
			
		} else {
			GUIManager.getDefaultGUIManager().log("Error accured while extracting data. While "
					+ "contacting authors about the problem please attach *all* three files mentioned in"
					+ "this log above this message.", "error", true);
		}
		
	}

	/**
	 * Metoda ta zwraca ścieżki do katalogu, w którym znajduje się cluster.csv.
	 * @return String - ścieżka do katalogu
	 */
	protected String getCSVLocation() {
		String targetDir = "";
		if(clusterPath == null || clusterPath.equals("")) {
			JOptionPane.showMessageDialog(null, "Please select csv file containing invariants.", 
					"Selection",JOptionPane.INFORMATION_MESSAGE);
			
			FileFilter[] filters = new FileFilter[1];
			filters[0] = new ExtensionFileFilter("Invariants csv file (.csv)", new String[] { "CSV" });
			String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
			String selectedFile = Tools.selectFileDialog(lastPath, filters, "Select", "");
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
		return targetDir;
	}
	
	/**
	 * Metoda obsługuje zdarzenie kliknięcia przycisku eksportu danych do okna głównego.
	 */
	protected void exportToAbyss() {
		String targetDir = getCSVLocation();
		if(targetDir == null)
			return;
		
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		//generowanie klastrowania:
		String resultFiles[] = GUIManager.getDefaultGUIManager().io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			ClusteringExtended clExtData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(clExtData==null) {
				GUIManager.getDefaultGUIManager().log("Reading data files failed. Extraction to Excel cannot begin.", "error", true);
				return;
			}
			
			ClusterDataPackage dataCore = new ClusterDataPackage();
			dataCore.dataMatrix = clExtData.getClusteringColored(); //najbardziej czasochłonne
			dataCore.algorithm = clExtData.metaData.algorithmName;
			dataCore.metric = clExtData.metaData.metricName;
			dataCore.clNumber = clExtData.metaData.clusterNumber;
			dataCore.clSize = new ArrayList<Integer>(clExtData.metaData.clusterSize);
			
			GUIManager.getDefaultGUIManager().showClusterSelectionBox(dataCore); //wyślij do Abyss (JFrame)
			
			deleteTmpFile(resultFiles);
			
			JOptionPane.showMessageDialog(null, "Operation successfull. Clusters are ready to show.", 
					"Status",JOptionPane.INFORMATION_MESSAGE);
		} else {
			GUIManager.getDefaultGUIManager().log("Error accured while extracting data. While "
					+ "contacting authors about the problem please attach *all* three files mentioned in"
					+ "this log above this message.", "error", true);
		}
		
	}
}
