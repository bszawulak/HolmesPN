package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import abyss.clusters.Clustering;
import abyss.clusters.ClusteringExtended;
import abyss.darkgui.GUIManager;
import abyss.files.clusters.ClusterReader;
import abyss.files.clusters.ExcelWriter;

/**
 * Klasa tworz�ca okno informacyjne wzgl�dem tabeli klastr�w. Zawiera ono informacje o
 * konkretnych wybranym klastrowaniu.
 * @author MR
 *
 */
public class AbyssClusterSubWindow extends JFrame {
	private static final long serialVersionUID = -5663572683374020754L;
	private JFrame parentFrame;
	private Clustering clusteringMetaData;
	private String nL = "\n";
	private String newline = "\n";
	private StyledDocument doc; //
	private JTextPane textPane; //panel z tekstem -> paneScrollPane
	private JPanel editPanel; //g��wny panel okna
	private JScrollPane paneScrollPane; //panel scrollbar -> editPanel
	
	private String clusterPath;
	/**
	 * Konstruktor domy�lny obiektu klasy AbyssClusterSubWindow.
	 */
	public AbyssClusterSubWindow() {
		
	}
	
	/**
	 * G��wny konstruktor parametrowy okna klasy AbyssClusterSubWindow.
	 * @param parent AbyssClusters - obiekt okna wywo�uj�cego
	 * @param clusteringMetaData Clustering - dane do wy�wietlenia
	 */
	public AbyssClusterSubWindow(AbyssClusters parent, Clustering dataPackage, int mode) {
		this();
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
	 * Metoda tworzy najprostsz� wersj� okna informacyjnego.
	 * @param parent AbyssClusters - okno wywo�uj�ce
	 * @param dataPackage Clustering - dane do wy�wietlenia
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
				area.append("Cluster "+i+" size: "+clusteringMetaData.clusterSize[i]+"  MSS: "+clusteringMetaData.clusterMSS[i]+nL);
			}
			area.append(nL);
			for(int i=0; i<6; i++) {
				area.append(clusteringMetaData.vectorMSS[i]+ " | ");
			}
		}
	}

	/**
	 * Metoda tworzy podstawow� wersj� okna informacyjnego.
	 * @param parent AbyssClusters - okno wywo�uj�ce
	 * @param dataPackage Clustering - dane do wy�wietlenia
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
		    		doc.insertString(doc.getLength(), addSpaceLeft(clusteringMetaData.clusterSize[i]+"",4), doc.getStyle("bold"));
		    		
		    		doc.insertString(doc.getLength(),"  MSS: ", doc.getStyle("regular"));
		    		doc.insertString(doc.getLength(), addSpaceLeft(clusteringMetaData.clusterMSS[i]+"",12)+nL,
		    				doc.getStyle(returnStyle(clusteringMetaData.clusterMSS[i])));
		    		
		    		//doc.insertString(doc.getLength(), "Cluster "+i+" size: "+data.clusterSize[i]+"  MSS: "+data.clusterMSS[i]+nL, doc.getStyle("regular"));
		    	}
		    	doc.insertString(doc.getLength(), nL, doc.getStyle("regular"));
		    	// 
		    	String sepSpace = "";
		    	if(clusteringMetaData.vectorMSS[0] < 0)
		    		sepSpace = " ";
		    	doc.insertString(doc.getLength(), sepSpace+" Min.  | 1st Qu.| Median |  Mean  | 3rd Qu.|  Max."+nL, doc.getStyle("bold"));
		    	for(int i=0; i<6; i++) {
		    		doc.insertString(doc.getLength(), addSpaceRight(clusteringMetaData.vectorMSS[i]+"",6)+ " | ", doc.getStyle("bold"));
		    	}
		    	
		    	textPane.setCaretPosition(0);
			} catch (Exception ex1) {
				
			}
			
		}
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	/**
	 * Metoda pomocnicza, dodaje spacje na prawo od tekstu.
	 * @param text String - tekst do rozszerzenia
	 * @param value int - maksymalna szeroko�� tekstu
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
	
	/**
	 * Metoda pomocnicza, dodaje spacje na lewo od tekstu.
	 * @param text String - tekst do rozszerzenia
	 * @param value int - maksymalna szeroko�� tekstu
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
	 * Metoda zwraca styl wy�wietlania (kolory) w zale�no�ci od warto�ci liczby
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
	 * @param doc StyledDocument - obiekt dokumentu przechowuj�cego style
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
	 * Metoda pomocnicza tworz�ca Panel widoku klastrowania z przyciskami
	 * @return JPanel - g��wny panel okna
	 */
	private JPanel createEditor() {
		editPanel = new JPanel();
		//this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		editPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.FIRST_LINE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.1;
        gbc.weighty = 1.0;
           
        textPane = createTextPane();
        textPane.setEditable(false);
        paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      
        editPanel.add(paneScrollPane, gbc);
        gbc.insets = new Insets(5,10,5,10);  //top padding
        gbc.gridy = 1; 
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.SOUTH;
        
        JButton button = new JButton("Create detailed clustering");
        button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(clusterPath != null && !clusterPath.equals("")) {
					String alg = clusteringMetaData.algorithmName;
					if(alg.equals("UPGMA"))
						alg = "average";
					
					String resultFiles[] = GUIManager.getDefaultGUIManager().generateSingleClustering(
							clusterPath, alg, clusteringMetaData.metricName, clusteringMetaData.clusterNumber);
					
					if(resultFiles != null) {
						ClusterReader reader = new ClusterReader();
						ClusteringExtended fullData = reader.readSingleClustering(resultFiles, clusteringMetaData);
						
						ExcelWriter ew;
						
						ew = new ExcelWriter(0, fullData, "tmp//testSheets.xls");
						int x=1;
						x=2;
						
						/*
						Object[] options = {"Save data file and Excel file", "Make Excel file only",};
						int n = JOptionPane.showOptionDialog(null,
										"Clustering data extraction succeed. What to do now?",
										"Choose output file", JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
						if (n == 0) { //both files
							ew = new ExcelWriter(0, fullData, "tmp//testSheets.xls");
						} else { //only excel
							ew = new ExcelWriter(0, fullData, "tmp//testSheets.xls");
						}
						*/
					} else {
						GUIManager.getDefaultGUIManager().log("Error accured while extracting data. While "
								+ "contacting authors about the problem please attach *all* three files mentioned in"
								+ "this log above this message.", "error", true);
					}
				}
			}
		});
        editPanel.add(button, gbc);
        return editPanel;
	}
}
