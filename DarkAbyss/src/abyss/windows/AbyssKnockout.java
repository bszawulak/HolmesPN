package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import abyss.analyse.InvariantsTools;
import abyss.analyse.MCTCalculator;
import abyss.darkgui.GUIManager;
import abyss.graphpanel.MauritiusMapPanel;
import abyss.math.MCSDataMatrix;
import abyss.math.MauritiusMapBT;
import abyss.math.MauritiusMapBT.BTNode;
import abyss.math.Transition;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa implementująca okno analizy poprzez mechanizm knockout oraz ścieżki Mauritiusa.:
 * 
 * "Petri net modelling of gene regulation of the Duchenne muscular dystrophy"
 * Stefanie Grunwald, Astrid Speer, Jorg Ackermann, Ina Koch
 * BioSystems, 2008, 92, pp.189-205
 * 
 * 
 * @author MR
 *
 */
public class AbyssKnockout extends JFrame {
	private static final long serialVersionUID = -9038958824842964847L;

	private JComboBox<String> transitionsCombo;
	private MauritiusMapPanel mmp;
	private int intX=0;
	private int intY=0;
	private JPanel mainPanel;
	private JPanel buttonPanel;
	private JPanel logMainPanel;
	private JScrollPane scroller;
	private MauritiusMapBT mmCurrentObject;
	
	private ArrayList<Integer> disabledSetByObjR = null;
	private ArrayList<Integer> commonSetToObjR = null;
	
	/**
	 * Konstruktor obiektu klasy AbyssKnockout
	 */
	public AbyssKnockout() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Knockout analysis");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 700));
		setLocation(15, 15);
		
		mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillComboBoxData();
  	  	    }  
    	});
		
		/*
		addComponentListener(this);
		addWindowStateListener(new WindowAdapter() {
			public void windowStateChanged(WindowEvent e) {
				if(e.getNewState() == JFrame.MAXIMIZED_BOTH) {
					//ego.setExtendedState(JFrame.NORMAL);
					//resizeComponents();
				}
			}
		});
		*/
	}



	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel główny
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		//Panel wyboru opcji szukania
		buttonPanel = createUpperButtonPanel(0, 0, 900, 90);
		logMainPanel = createGraphPanel(0, 90, 900, 600);
		
		panel.add(buttonPanel, BorderLayout.NORTH);
		panel.add(logMainPanel, BorderLayout.CENTER);
		panel.repaint();
		return panel;
	}

	/**
	 * Metoda tworząca górny panel przycisków.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 * @param width int - szerokość panelu
	 * @param height int - wysokość panelu
	 * @return JPanel - panel przycisków
	 */
	private JPanel createUpperButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Search options"));
		//panel.setBounds(x, y, width, height);
		panel.setLocation(x, y);
		panel.setPreferredSize(new Dimension(width, height));
		
		int posX = 10;
		int posY = 20;
		
		JLabel mcsLabel1 = new JLabel("Obj. reaction:");
		mcsLabel1.setBounds(posX, posY, 80, 20);
		panel.add(mcsLabel1);
		
		String[] dataT = { "---" };
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		transitionsCombo = new JComboBox<String>(dataT);
		transitionsCombo.setBounds(posX+90, posY, 400, 20);
		transitionsCombo.setSelectedIndex(0);
		transitionsCombo.setMaximumRowCount(6);
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		if(transitions != null && transitions.size()>0) {
			for(int t=0; t < transitions.size(); t++) {
				transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
			}
		} 
		panel.add(transitionsCombo);
		
		JButton generateButton = new JButton("Generate");
		generateButton.setBounds(posX, posY+30, 110, 30);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				mmCurrentObject = generateMap();
				paintMap();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		JButton showKnockoutButton = new JButton("Show...");
		showKnockoutButton.setBounds(posX+120, posY+30, 110, 30);
		showKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		showKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/showNotepad.png"));
		showKnockoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
				MauritiusMapBT infoMap = generateMap();
				if(infoMap != null) {
					AbyssNotepad notePad = new AbyssNotepad(900,600);
					notePad.setVisible(true);
					getKnockoutInfo(infoMap, notePad);
				}
			}
		});
		showKnockoutButton.setFocusPainted(false);
		panel.add(showKnockoutButton);
		
		JButton toNetKnockoutButton = new JButton("Show Net");
		toNetKnockoutButton.setBounds(posX+240, posY+30, 110, 30);
		toNetKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		toNetKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/sendToNet.png"));
		toNetKnockoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				
				MauritiusMapBT infoMap = generateMap();
				if(infoMap != null) {
					getKnockoutInfoToNet(infoMap);
				}
			}
		});
		toNetKnockoutButton.setFocusPainted(false);
		panel.add(toNetKnockoutButton);
		
		JButton fullDataKnockoutButton = new JButton("Net stats");
		fullDataKnockoutButton.setBounds(posX+360, posY+30, 110, 30);
		fullDataKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		fullDataKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/sendToNet.png"));
		fullDataKnockoutButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {

				AbyssNotepad notePad = new AbyssNotepad(900,600);
				notePad.setVisible(true);
				getKnockoutFullInfo(notePad);
			}
		});
		fullDataKnockoutButton.setFocusPainted(false);
		panel.add(fullDataKnockoutButton);
		
		JButton monaLisaButton = new JButton("Load MonaLisa");
		monaLisaButton.setBounds(posX+480, posY+30, 110, 30);
		monaLisaButton.setMargin(new Insets(0, 0, 0, 0));
		monaLisaButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/sendToNet.png"));
		monaLisaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbyssNotepad notePad = new AbyssNotepad(900,600);
				notePad.setVisible(true);
				showMonaLisaResults(notePad);
			}
		});
		monaLisaButton.setFocusPainted(false);
		panel.add(monaLisaButton);
		
		JButton monaLisaToNetButton = new JButton("MonaL.->Net");
		monaLisaToNetButton.setBounds(posX+600, posY+30, 110, 30);
		monaLisaToNetButton.setMargin(new Insets(0, 0, 0, 0));
		monaLisaToNetButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/sendToNet.png"));
		monaLisaToNetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				exportMonaLisaToNet();
			}
		});
		monaLisaToNetButton.setFocusPainted(false);
		panel.add(monaLisaToNetButton);
		
		JCheckBox shortTextCheckBox = new JCheckBox("Show full names");
		shortTextCheckBox.setBounds(posX+490, posY, 130, 20);
		shortTextCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					mmp.setFullName(true);
				} else {
					mmp.setFullName(false);
				}
				mmp.repaint();
			}
		});
		shortTextCheckBox.setSelected(true);
		panel.add(shortTextCheckBox);
		
		return panel;
	}
	
	/**
	 * Metoda tworząca główmu panel mapy.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 * @param width int - szerokość panelu
	 * @param height int - wysokość panelu
	 * @return JPanel - panel przycisków
	 */
	private JPanel createGraphPanel(int x, int y, int width, int height) {
		JPanel gr_panel = new JPanel(new BorderLayout());
		//gr_panel.setBounds(x, y, width, height);
		
		gr_panel.setLocation(x, y);
		gr_panel.setPreferredSize(new Dimension(width, height));
		
		mmp = new MauritiusMapPanel();
		
		intX = width - 10;
		intY = height - 25;
		
		scroller = new JScrollPane(mmp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		gr_panel.add(scroller, BorderLayout.CENTER);

		return gr_panel;
	}

	/**
	 * Metoda wyświetla w notatniku dwa zbiory: zbiór tranzycji o tym samym stopniu występowania w inwariantach
	 * co objectiveR, oraz zbiór reakcji które są zależne od objR.
	 * @param infoMap MauritiusMapBT - obiekt danych mapy
	 * @param notePad AbyssNotepad - obiekt notatnika dla wyników
	 */
	protected void getKnockoutInfo(MauritiusMapBT infoMap, AbyssNotepad notePad) {
		ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(infoMap);
		
		//int noteValue = infoMap.getRoot().transFrequency;
		//knockOutDataFailed = new ArrayList<Integer>();
		//knockOutDataObjR = new ArrayList<Integer>();
		//collectInfo(infoMap.getRoot(), noteValue);
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		notePad.addTextLineNL("Objective reaction (knock-out reaction): "+infoMap.getRoot().transName, "text");
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Reactions knocked out: ", "text");
		Collections.sort(dataMatrix.get(0));
		for(int element : dataMatrix.get(0)) {
			notePad.addTextLineNL("[t_"+element+"] : "+transitions.get(element).getName(), "text");
		}
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Common-frequency reactions: ", "text");
		
		Collections.sort(dataMatrix.get(1));
		for(int element : dataMatrix.get(1)) {
			notePad.addTextLineNL("[t_"+element+"] : "+transitions.get(element).getName(), "text");
		}
		//knockOutData
	}
	
	/**
	 * Metoda pokazuje wyliczone wszystkie tranzycje.
	 * @param notePad AbyssNotepad - obiekt notatnika
	 */
	//TODO:
	protected void getKnockoutFullInfo(AbyssNotepad notePad) {
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		if(invariants == null || invariants.size() < 1) {
			JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
					"Warning", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		int transNumber = transitions.size();
		ArrayList<Integer> transFailDependency = new ArrayList<Integer>();
		ArrayList<Integer> transCommonSetSize = new ArrayList<Integer>();
		
		//oblicz wszystkie:
		for(int t=0; t<transNumber; t++) {
			MauritiusMapBT mm = new MauritiusMapBT(invariants, t);
			ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(mm);
			transFailDependency.add(dataMatrix.get(0).size());
			transCommonSetSize.add(dataMatrix.get(1).size());
		}


		notePad.addTextLineNL("Data collected for "+transNumber+ "transitions.", "text");
		notePad.addTextLineNL("", "text");
		
		for(int t=0; t<transNumber; t++) {
			notePad.addTextLine("[t_"+t+ "]|"+transitions.get(t).getName()+":", "text");
			notePad.addTextLineNL("| Knocked-out: "+transFailDependency.get(t)+ 
					"| Common: "+transCommonSetSize.get(t), "text");
		}
		
		//knockOutData
	}
	
	/**
	 * Metoda wczytuje wyniki z MonyLisy i posyła na strukturę sieci
	 */
	private void exportMonaLisaToNet() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("MonaLisa Knockout transitions (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load", "", "");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null, "Incorrect file location.", "Operation failed.", 
					JOptionPane.ERROR_MESSAGE);
			return;
		} 
		//TRANZYCJE:
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		int transSize = transitions.size();
		
		
		ArrayList<ArrayList<Integer>> knockoutMatrix = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<transSize; i++) {
			knockoutMatrix.add(new ArrayList<Integer>());
		}
		
		
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			
			String line = "";
			line = buffer.readLine(); //first line
			line = buffer.readLine();
			while(line != null && line.length() > 4) {
				int tmp = line.indexOf(" ");
				String name = line.substring(0, tmp);
				
				for(int t=0; t<transSize; t++) {
					if(transitions.get(t).getName().equals(name)) {
						String next = line.substring(line.indexOf("->")+2);
						if(next.length() == 0) {
							break;
						} else { //z MCT
							next = next.trim();
							String[] elements = next.split(";");
							ArrayList<Integer> knockoutVector = new ArrayList<Integer>();
							
							for(String tr : elements) {
								for(int t2=0; t2<transSize; t2++) {
									if(transitions.get(t2).getName().equals(tr)) {
										knockoutVector.add(t2);
										break;
									}
								}
							}
							knockoutMatrix.set(t,knockoutVector);
							break;
						}
					}
				}
				
				line = buffer.readLine();
			}
		} catch (Exception e) {
			
		}

		GUIManager.getDefaultGUIManager().showKnockout(knockoutMatrix);
	}
	
	/**
	 * Metoda wczytuje plik z wynikami Knockout dla wszystkich tranzycji sieci wygenerowane w programie
	 * MonaLisa, a następnie wyświetla je w zunifikowanej formie w oknie.
	 * @param notePad AbyssNotepad - okno wyświetlania wyników
	 */
	private void showMonaLisaResults(AbyssNotepad notePad) {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("MonaLisa Knockout transitions (.txt)",  new String[] { "TXT" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load", "", "");
		
		if(selectedFile.equals("")) {
			JOptionPane.showMessageDialog(null, "Incorrect file location.", "Operation failed.", 
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		//MCT:
		MCTCalculator analyzer = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getAnalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		mct = MCTCalculator.getSortedMCT(mct);
		//TRANZYCJE:
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		int transSize = transitions.size();
		//WYNIKOWE LINIE:
		ArrayList<String> resultLines = new ArrayList<String>();
		ArrayList<String> mctOrNot = new ArrayList<String>();
		ArrayList<Integer> mctSize = new ArrayList<Integer>();
		for(int i=0; i<transSize; i++) {
			resultLines.add("");
			mctOrNot.add("");
			mctSize.add(0);
		}
		
		//WEKTOR DANYCH O MCT DLA KAŻDEJ TRANZYCJI
		int mctNo= 0;
		for(ArrayList<Transition> arr : mct) {
			mctNo++;
			for(Transition t : arr) {
				int id = transitions.indexOf(t);
				mctOrNot.set(id, "MCT_"+mctNo);
				mctSize.set(id, arr.size());
			}
		}
		
		
		//czytanie pliku z MonyLisy
		
		try {
			DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(in));
			
			String line = "";
			line = buffer.readLine(); //first line
			line = buffer.readLine();
			while(line != null && line.length() > 4) {
				int tmp = line.indexOf(" ");
				String name = line.substring(0, tmp);
				
				for(int t=0; t<transSize; t++) {
					if(transitions.get(t).getName().equals(name)) {
						String next = line.substring(line.indexOf("->")+2);
						if(next.length() == 0) {
							String newLine = "t"+t+"_"+name+" | Knockout: 0% (0 / "+transSize+")  ";
							newLine += mctOrNot.get(t);
							
							resultLines.set(t, newLine);
							break;
						} else { //z MCT
							String[] elements = next.split(";");
							float knockoutPercent = (float)((float)elements.length/(float)transSize);
							knockoutPercent *= 100;
							String newLine = "t"+t+"_"+name+" | Knockout: "+String.format("%.2f", knockoutPercent)+ "% ("+elements.length+"/"+transSize+")  ";
							newLine += mctOrNot.get(t);
							
							resultLines.set(t, newLine);
							break;
						}
					}
				}
				
				line = buffer.readLine();
			}
		} catch (Exception e) {
			
		}
		
		notePad.addTextLineNL("", "text");
		
		//wyświetlanie I bloku wyników:
		for(int t=0; t<transSize; t++) {
			notePad.addTextLineNL(resultLines.get(t), "text");
		}
		
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("", "text");
		
		//przygotowanie II bloku wyników:
		ArrayList<String> mctTmpVector = new ArrayList<String>();
		for(int m=0; m < mct.size(); m++) {
			mctTmpVector.add("MCT_"+(m+1));
		}
		
		ArrayList<Integer> transInInvVector = InvariantsTools.transInInvariants();
		int invNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix().size();
		
		ArrayList<String> secondResultLines = new ArrayList<String>();
		for(int t=0; t<transSize; t++) {
			if(resultLines.get(t).contains(" MCT_")) {
				String line = resultLines.get(t);
				String mctName = line.substring(line.indexOf( "MCT_"));
				if(mctTmpVector.contains(mctName)) {
					mctTmpVector.remove(mctName);
					
					line = line.substring(1); // - wycinamy literkę t
					float invPercent = -1;
					String id = line.substring(0, line.indexOf("_"));
					try {
						int ident = Integer.parseInt(id);
						invPercent = (float)((float)transInInvVector.get(ident)/(float)invNumber);
						invPercent *= 100;
					} catch (Exception e) { }
					
					line = line.substring(line.indexOf("_")+1);
					//String name = line.substring(0, line.indexOf("|")-1); //nazwa tranzycji
					
					line = line.substring(line.indexOf(":")+2);
					String transPercent = line.substring(0, line.indexOf("%")+1);
					
					secondResultLines.add(mctName+"   "+transPercent+"  "+String.format("%.2f", invPercent)+"%");
				}
				
			} else {
				String line = resultLines.get(t);
				line = line.substring(1); // - wycinamy literkę t
				float invPercent = -1;
				String id = line.substring(0, line.indexOf("_"));
				
				try {
					int ident = Integer.parseInt(id);
					invPercent = (float)((float)transInInvVector.get(ident)/(float)invNumber);
					invPercent *= 100;
				} catch (Exception e) { }
						
				line = line.substring(line.indexOf("_")+1);
				String name = line.substring(0, line.indexOf("|")-1); //nazwa tranzycji
				
				line = line.substring(line.indexOf(":")+2);
				String transPercent = line.substring(0, line.indexOf("%")+1);
				
				secondResultLines.add("t"+id+"_"+name+"   "+transPercent+"  "+String.format("%.2f", invPercent)+"%");
			}
		}
		
		
		//wyświetlanie II bloku wyników:
		for(int i=0; i<secondResultLines.size(); i++) {
			notePad.addTextLineNL(secondResultLines.get(i), "text");
		}

		//knockOutData
	}
	
	
	
	/**
	 * Metoda przesyła dane o knockout na obraz sieci.
	 * @param infoMap
	 */
	protected void getKnockoutInfoToNet(MauritiusMapBT infoMap) {
		ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(infoMap);
		
		try {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().turnTransitionGlowingOff();
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().setTransitionGlowedMTC(false);
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().resetTransitionGraphics();

			Transition trans_TMP;
			ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			
			for(int id : dataMatrix.get(0)) { //wyłączane przez objR
				trans_TMP= transitions.get(id);
				trans_TMP.setColorWithNumber(true, Color.black, false, -1, false, "");
			}
			for(int id : dataMatrix.get(1)) { //równorzędne do objR
				trans_TMP= transitions.get(id);
				trans_TMP.setColorWithNumber(true, Color.blue, false, -1, false, "");
			}
			
			int rootID = infoMap.getRoot().transLocation;
			trans_TMP= transitions.get(rootID);
			trans_TMP.setColorWithNumber(true, Color.red, false, -1, false, "");
			
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Metoda uruchamia przeglądanie mapy a następnie agreguje wynik do obiektu wyjściowego.
	 * @param infoMap MauritiusMapBT - obiekt mapu
	 * @return ArrayList[ArrayList[Integer]] - pierszy zbiór (.get(0) = disabledSetByObjR) to reakcje wyłączane
	 *  przez objR, drugi zbiór (.get(1) = commonSetToObjR) to reakcje o tej samej frekwencji co objR.
	 */
	private ArrayList<ArrayList<Integer>> collectMapData(MauritiusMapBT infoMap) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		
		int noteValue = infoMap.getRoot().transFrequency;
		disabledSetByObjR = new ArrayList<Integer>();
		commonSetToObjR = new ArrayList<Integer>();
		collectInfo(infoMap.getRoot(), noteValue);
		
		result.add(new ArrayList<Integer>(disabledSetByObjR));
		result.add(new ArrayList<Integer>(commonSetToObjR));
		return result;
	}

	/**
	 * Rekurencyjna metoda przeszukująca mapę i tworząca zbiory reakcji zależnych i niezależnych od
	 * objR (korzeń drzewa). Zwraca wynik w formie wektorów globalnych commonSetToObjR (tranzycje o
	 * identycznej liczności co objR) oraz disabledSetByObjR - tranzycje wyłączane przez te ze zbioru
	 * commonSetToObjR.
	 * @param node BTNode - węzeł drzewa
	 * @param startSetValue int - frequency dla objR
	 */
	private void collectInfo(BTNode node, int startSetValue) {
		int freq = node.transFrequency;
		int transID = node.transLocation;
		if(freq == startSetValue) {
			if(commonSetToObjR.contains(transID) == false) {
				commonSetToObjR.add(transID);
			}
		} else {
			if(disabledSetByObjR.contains(transID) == false) {
				disabledSetByObjR.add(transID);
			}
		}
		
		if(node.rightChild != null) {
			collectInfo(node.rightChild, startSetValue);
    	}
    	
    	if(node.leftChild != null) {
    		collectInfo(node.leftChild, startSetValue);
    	}
	}

	/**
	 * Metoda odpowiedzialna za wygenerowanie mapy.
	 * @return MauritiusMapBT - obiekt mapy
	 */
	private MauritiusMapBT generateMap() {
		int selection = transitionsCombo.getSelectedIndex();
		if(selection == 0) {
			JOptionPane.showMessageDialog(null, "Please choose main reaction for Mauritius Map.", 
					"No selection", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		
		selection--;
		
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsMatrix();
		if(invariants == null || invariants.size() < 1) {
			JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
					"Warning", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		MauritiusMapBT mm = new MauritiusMapBT(invariants, selection);
		return mm;
	}
	
	/**
	 * Metoda uruchamiania przerysowanie wyliczonej mapy.
	 */
	private void paintMap() {
		mmp.addNewMap(mmCurrentObject);
		mmp.repaint();
	}
	
	/**
	 * Metoda odpowiedzialna za dopasowywanie elementów okna.
	 */
	protected void resizeComponents() {
		buttonPanel.setBounds(0, 0, mainPanel.getWidth(), 90);
		logMainPanel.setBounds(0, 90, mainPanel.getWidth(), mainPanel.getHeight()-90);
		
		intX = logMainPanel.getWidth();
		intY = logMainPanel.getHeight();
		//scroller.setBounds(0, 0, intX, intY);
		scroller.setPreferredSize(new Dimension(intX, intY));
	}
	
	/**
	 * Metoda ustawia odpowiednie wartości komponentów okna za każdym razem gdy okno jest aktywowane.
	 */
	protected void fillComboBoxData() {
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		int selection = transitionsCombo.getSelectedIndex();
		transitionsCombo.removeAllItems();
		transitionsCombo.addItem("---");
		for(int t=0; t < transitions.size(); t++) {
			transitionsCombo.addItem("t"+(t)+"."+transitions.get(t).getName());
		}
		if(selection < transitions.size()+1)
			transitionsCombo.setSelectedIndex(selection);
		
	}
}
