package holmes.windows.clusters;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
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
import holmes.darkgui.LanguageManager;
import holmes.files.clusters.ClusterReader;
import holmes.files.clusters.ClusteringExcelWriter;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa tworząca okno informacyjne względem tabeli klastrów. Zawiera ono informacje o
 * konkretnych wybranym klastrowaniu.
 */
public class HolmesClusterSubWindow extends JFrame {
	@Serial
	private static final long serialVersionUID = 6818230680946396781L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private JFrame parentFrame;
	private Clustering clusteringMetaData;
	private String nL = "\n";
	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
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
	 * @param dataPackage Clustering - dane do wyświetlenia
	 * @param mode int - tryb
	 */
	public HolmesClusterSubWindow(HolmesClusters parent, Clustering dataPackage, int mode) {
		this();
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00510exception")+"\n"+ex.getMessage(), "error", true);
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
			area.append(lang.getText("HCSWwin_entry002")+" "+clusteringMetaData.algorithmName+nL); //Algorithm name
			area.append(lang.getText("HCSWwin_entry003")+" "+clusteringMetaData.metricName+nL); //Metric name
			area.append(nL);
			area.append(lang.getText("HCSWwin_entry004")+" "+clusteringMetaData.invNumber+nL); //Invariants number
			area.append(lang.getText("HCSWwin_entry005")+" "+clusteringMetaData.clusterNumber+nL); //Clusters number
			area.append(lang.getText("HCSWwin_entry006")+" "+clusteringMetaData.zeroClusters+nL); //Zero-clusters
			area.append(lang.getText("HCSWwin_entry007")+" "+clusteringMetaData.evalMSS+nL); //MSS evaluation
			area.append(lang.getText("HCSWwin_entry008")+" "+clusteringMetaData.evalCH+nL); //C-H evaluation
			area.append(nL);
			
			//kolejne wiersze z MSS dla klastrów:
			for(int i=0; i<clusteringMetaData.clusterNumber; i++) {
				String value = getMSSFormatted(clusteringMetaData.clusterMSS.get(i));
				String strB = "err.";
				try {
					strB = String.format(lang.getText("HCSWwin_entry009"), i, clusteringMetaData.clusterSize.get(i), value);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HCSWwin_entry009", "error", true);
				}
				area.append(strB);
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
        StringBuilder txt = new StringBuilder();
        txt.append(df.format(clusterMSS));
        txt = new StringBuilder(txt.toString().replace(",", "."));
        if(!txt.toString().contains("."))
        	txt.append(".");
        int size = txt.length();
		txt.append("0".repeat(Math.max(0, 10 - size)));
        
		return txt.toString();
	}

	/**
	 * Metoda tworzy podstawową wersję okna informacyjnego.
	 * @param parent HolmesClusters - okno wywołujśce
	 * @param dataPackage Clustering - dane do wyświetlenia
	 */
	private void initiateExtendedMode(HolmesClusters parent, Clustering dataPackage) {
		String strB = "err.";
		try {
			strB = String.format(lang.getText("HCSWwin_entry001title"), dataPackage.clusterNumber, dataPackage.algorithmName, dataPackage.metricName);
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentryLNGexc")+" "+"HCSWwin_entry001title", "error", true);
		}
		this.setTitle(strB);

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
				doc.insertString(doc.getLength(), Tools.setToSize(lang.getText("HCSWwin_entry010")+" ",20,false), doc.getStyle("regular"));
				doc.insertString(doc.getLength(), clusteringMetaData.algorithmName+nL, doc.getStyle("bold"));
		    	
				doc.insertString(doc.getLength(), Tools.setToSize(lang.getText("HCSWwin_entry011")+" ",20,false), doc.getStyle("regular"));
				doc.insertString(doc.getLength(), clusteringMetaData.metricName+nL, doc.getStyle("bold"));
				
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize(lang.getText("HCSWwin_entry012")+" ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.invNumber+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize(lang.getText("HCSWwin_entry013")+" ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.clusterNumber+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize(lang.getText("HCSWwin_entry014")+" ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.zeroClusters+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize(lang.getText("HCSWwin_entry015")+" ",20,false), doc.getStyle("regular"));
		    	String value = getMSSFormatted(clusteringMetaData.evalMSS);
		    	doc.insertString(doc.getLength(), value+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), Tools.setToSize(lang.getText("HCSWwin_entry016")+" ",20,false), doc.getStyle("regular"));
		    	doc.insertString(doc.getLength(), clusteringMetaData.evalCH+nL, doc.getStyle("bold"));
		    	
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	//dane o klastrach
		    	for(int i=0; i<clusteringMetaData.clusterNumber; i++) {
		    		doc.insertString(doc.getLength(), lang.getText("HCSWwin_entry017a")+" "
							+Tools.setToSize((i+1)+"",3,true)+" "+lang.getText("HCSWwin_entry017b")+" ", doc.getStyle("regular"));
		    		doc.insertString(doc.getLength(), Tools.setToSize(clusteringMetaData.clusterSize.get(i)+"",4,true), doc.getStyle("bold"));
		    		doc.insertString(doc.getLength(),"  "+lang.getText("HCSWwin_entry018")+" ", doc.getStyle("regular"));
		    		
		    		value = getMSSFormatted(clusteringMetaData.clusterMSS.get(i));
		    		doc.insertString(doc.getLength(), value+nL, doc.getStyle(returnStyle(clusteringMetaData.clusterMSS.get(i))));
		    	}
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	
		    	String sepSpace = "";
		    	if(clusteringMetaData.vectorMSS.get(0) < 0)
		    		sepSpace = " ";
		    	doc.insertString(doc.getLength(), sepSpace+" "+lang.getText("HCSWwin_entry019")+nL, doc.getStyle("bold"));
		    	for(int i=0; i<6; i++) {
		    		doc.insertString(doc.getLength(), Tools.setToSize(clusteringMetaData.vectorMSS.get(i)+"",6,false)+ " | ", doc.getStyle("bold"));
		    	}
		    	
		    	textPane.setCaretPosition(0);
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00511exception")+"\n"+ex.getMessage(), "error", true);
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
		String initString = lang.getText("HCSWwin_entry020")+newline;
	    JTextPane txtPane = new JTextPane();
	    doc = txtPane.getStyledDocument();
	    addStylesToDocument(doc);
	    try {
	        doc.insertString(doc.getLength(), initString, doc.getStyle("regular"));
	    } catch (Exception e) {
	        overlord.log(lang.getText("LOGentry00512exception")+"\n"+e.getMessage(), "error", true);
	    }
	    return txtPane;
	}
	
	/**
	 * Metoda pomocnicza tworząca Panel widoku klastrowania z przyciskami
	 * @return JPanel - główny panel okna
	 */
	private JPanel createEditor() {
		//główny panel okna
		JPanel editPanel = new JPanel();
		//editPanel.setBorder(BorderFactory.createTitledBorder("SSSSSSS"));
		editPanel.setLayout(null);
		editPanel.setBounds(0, 0, 500, 500);
        
           
        textPane = createTextPane();
        textPane.setEditable(false);
		//panel scrollbar -> editPanel
		JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setBounds(5, 5, 585, 500);
        editPanel.add(paneScrollPane);

        buttonExcel = new JButton(lang.getText("HCSWwin_entry021"), Tools.getResIcon48("/icons/clustWindow/buttonExportSingleToExcel.png")); //>> Excel
        buttonExcel.setBounds(5, 510, 190, 50);
        //button.setBounds(new Rectangle(150, 40));
        buttonExcel.addActionListener(actionEvent -> {
			layerUI.start();
			turnOffButtons();
			exportDataToExcel();
			turnOnButtons();
			layerUI.stop();
		});
        editPanel.add(buttonExcel);
        
        buttonInjectCluster = new JButton(lang.getText("HCSWwin_entry022"), Tools.getResIcon48("/icons/clustWindow/buttonSendToAbyss.png"));//>> Net structure
        buttonInjectCluster.setBounds(200, 510, 190, 50);
        buttonInjectCluster.addActionListener(actionEvent -> {
			layerUI.start();
			turnOffButtons();
			exportToHolmes();
			turnOnButtons();
			layerUI.stop();
		});
        editPanel.add(buttonInjectCluster);
        
        buttonTexTable = new JButton(lang.getText("HCSWwin_entry023"), Tools.getResIcon48("/icons/menu/menu_exportTex.png"));//>> Cluster table
        buttonTexTable.setBounds(400, 510, 190, 50);
        buttonTexTable.addActionListener(actionEvent -> {
			layerUI.start();
			turnOffButtons();
			exportToLatex();
			turnOnButtons();
			layerUI.stop();
		});
        editPanel.add(buttonTexTable);
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
			overlord.log(lang.getText("LOGentry00513exception")+"\n"+e.getMessage(), "error", true);
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
			overlord.log(lang.getText("LOGentry00514exception")+"\n"+e.getMessage(), "error", true);
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
		if(!ew.isSuccess())
			return null;
		
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Microsoft Excel 97/2000/XP/2003 (.xls)", new String[] { "XLS" });
		String lastPath = overlord.getLastPath();
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("save")
				, lang.getText("HCSWwin_entry024t"), "");
		if(selectedFile.isEmpty())
			return null;
		
		if(!selectedFile.contains(".xls"))
			selectedFile += ".xls";
		//File file = new File(selectedFile);
		try {
			Tools.copyFileByPath("tmp//testSheets.xls", selectedFile);
		} catch (Exception e) {
			String strB = "err.";
			try {
				strB = String.format(lang.getText("LOGentry00515exception"), selectedFile);
			} catch (Exception ex2) {
				overlord.log(lang.getText("LOGentryLNGexc")+" "+"LOGentry00515exception", "error", true);
			}
			overlord.log(strB+"\n"+e.getMessage() , "error", true);
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
			JOptionPane.showMessageDialog(null, lang.getText("HCSWwin_entry025"),
					lang.getText("error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		String[] resultFiles = overlord.io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			// czytanie wyników:
			fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(fullData==null) {
				overlord.log(lang.getText("HCSWwin_entry026"), "error", true);
				return;
			}
		} else {
			overlord.log(lang.getText("HCSWwin_entry027"), "error", true);
			return;
		}
		
		Object[] options = {lang.getText("HCSwin_entry028op1"), lang.getText("HCSwin_entry028op2"),};
		int n = JOptionPane.showOptionDialog(null,
						lang.getText("HCSwin_entry028"),
						lang.getText("HCSwin_entry028t"), JOptionPane.YES_NO_OPTION,
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
		String targetDir;
		Object[] options = {lang.getText("HCSwin_entry029op1"), lang.getText("HCSwin_entry029op2"),};
		int n = JOptionPane.showOptionDialog(null,
						lang.getText("HCSwin_entry029"),
						lang.getText("HCSwin_entry029t"), JOptionPane.YES_NO_OPTION,
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
					overlord.log(lang.getText("LOGentry00516exception")+"\n"+e.getMessage(), "error", true);
				}
			}
			targetDir = path;
		} else {
			if(clusterPath == null || clusterPath.isEmpty()) {
				JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry030"),
						lang.getText("HCSwin_entry030t"),JOptionPane.INFORMATION_MESSAGE);
				
				FileFilter[] filters = new FileFilter[1];
				filters[0] = new ExtensionFileFilter("Invariants csv file (.csv)", new String[] { "CSV" });
				String lastPath = overlord.getLastPath();
				String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("select"), "", "");
				if(selectedFile.isEmpty())
					return null;
				
				File x = new File(selectedFile);
				String name = x.getName();
				String path = Tools.getFilePath(x);
				if(!name.equals("cluster.csv")) {
					try {
						Tools.copyFileByPath(x.getAbsolutePath(), path+"cluster.csv");
					} catch (IOException e) {
						overlord.log(lang.getText("LOGentry00517exception")+"\n"+e.getMessage(), "error", true);
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
		String lastPath = overlord.getLastPath();
		if(overlord.getWorkspace().getProject().getT_InvMatrix() == null) { //brak inwariantów
			JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry031"),
					lang.getText("warning"),JOptionPane.WARNING_MESSAGE);
			
			FileFilter[] filters = new FileFilter[1];
			filters[0] = new ExtensionFileFilter("CSV invariants file (.csv)",  new String[] { "CSV" });
			String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("HCSwin_entry032")
					, lang.getText("HCSwin_entry032t"), "");
			
			if(selectedFile.isEmpty())
				return null;
			else
				return selectedFile;
		} else {
			{
				//generowanie CSV, uda się, jeśli inwarianty istnieją
				String CSVfilePath = overlord.getTmpPath() + "cluster.csv";
				int result = overlord.getWorkspace().getProject().saveInvariantsToCSV(CSVfilePath, true, true);
				if(result == -1) {
					JOptionPane.showMessageDialog(null,lang.getText("HCSwin_entry033")
							,	"CSV export error",JOptionPane.ERROR_MESSAGE);
					overlord.log(lang.getText("HCSwin_entry033"), "error", true);
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
			Object[] options = {lang.getText("HCSwin_entry034op1"), lang.getText("HCSwin_entry034op2"),};
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HCSwin_entry034"),
							lang.getText("HCSwin_entry034t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				int transNumber = fullData.transNames.length-1;
				int netTransNumber = overlord.getWorkspace().getProject().getTransitions().size();
				if(transNumber != netTransNumber) {
					String strB = "err.";
					try {
						strB = String.format(lang.getText("HCSwin_entry035"), transNumber, netTransNumber);
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"HCSwin_entry035", "error", true);
					}
					JOptionPane.showMessageDialog(null, strB, lang.getText("error"), JOptionPane.ERROR_MESSAGE);
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
				for(int cl=0; cl<dataCore.clSize.size(); cl++) {
					//dataCore.clSize.set(cl, fullData.clustersInv.get(cl).size());
				}
				overlord.showClusterSelectionBox(dataCore); //wyślij do Holmes (JFrame)
				JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry036"),
						lang.getText("status"),JOptionPane.INFORMATION_MESSAGE);
				proceed = false;
			}
		}
		
		if(!proceed)
			return;
		
		String targetDir = getInvariantsCSVLocation();
		if(targetDir == null) {
			JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry037"),
					lang.getText("error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		//generowanie klastrowania:
		String[] resultFiles = overlord.io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(fullData==null) {
				overlord.log(lang.getText("HCSwin_entry038"), "error", true);
				return;
			}
			
			int transNumber = fullData.transNames.length-1;
			int netTransNumber = overlord.getWorkspace().getProject().getTransitions().size();
			if(transNumber != netTransNumber) {
				String strB = "err.";
				try {
					strB = String.format(lang.getText("HCSwin_entry035"), transNumber, netTransNumber);
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HCSwin_entry035", "error", true);
				}
				JOptionPane.showMessageDialog(null, strB, lang.getText("error"), JOptionPane.ERROR_MESSAGE);
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
			for(int cl=0; cl<dataCore.clSize.size(); cl++) {
				//dataCore.clSize.set(cl, fullData.clustersInv.get(cl).size());
			}

			overlord.showClusterSelectionBox(dataCore); //wyślij do Holmes (JFrame)
			deleteTmpFile(resultFiles);
			
			JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry039"),
					"Status",JOptionPane.INFORMATION_MESSAGE);
		} else {
			overlord.log(lang.getText("HCSwin_entry040"), "error", true);
		}
	}

	/**
	 * Metoda obsługuje zdarzenie kliknięcia przycisku eksportu danych do okna głównego.
	 */
	protected void exportToLatex() {
		boolean proceed = true;
		
		if(fullData != null) {
			//ask what to do
			Object[] options = {lang.getText("HCSwin_entry041op1"), lang.getText("HCSwin_entry041op2"),};
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HCSwin_entry041"),
							lang.getText("HCSwin_entry041t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
			if (n == 0) {
				int transNumber = fullData.transNames.length-1;
				int netTransNumber = overlord.getWorkspace().getProject().getTransitions().size();
				if(transNumber != netTransNumber) {
					String strB = "err.";
					try {
						strB = String.format(lang.getText("HCSwin_entry035"), transNumber, netTransNumber);
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentryLNGexc")+" "+"HCSwin_entry035", "error", true);
					}
					JOptionPane.showMessageDialog(null, strB, lang.getText("error"), JOptionPane.ERROR_MESSAGE);
					return;
				}

				overlord.tex.writeCluster(fullData);
				
				JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry042"),
						"Status",JOptionPane.INFORMATION_MESSAGE);
				proceed = false;
			}
		}
		
		if(!proceed)
			return;
		
		String targetDir = getInvariantsCSVLocation();
		if(targetDir == null) {
			JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry043"),
					lang.getText("error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String alg = clusteringMetaData.algorithmName;
		if(alg.equals("UPGMA"))
			alg = "average";
		
		//generowanie klastrowania:
		String[] resultFiles = overlord.io.generateSingleClustering(
				targetDir, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
		if(resultFiles != null) {
			ClusterReader reader = new ClusterReader();
			fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
			if(fullData==null) {
				overlord.log(lang.getText("LOGentry00518"), "error", true);
				return;
			}

			overlord.tex.writeCluster(fullData);
			deleteTmpFile(resultFiles);
			JOptionPane.showMessageDialog(null, lang.getText("HCSwin_entry044"),
					"Status",JOptionPane.INFORMATION_MESSAGE);
		} else {
			overlord.log(lang.getText("HCSwin_entry045"), "error", true);
		}
	}
}
