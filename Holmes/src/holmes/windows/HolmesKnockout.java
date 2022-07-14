package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileFilter;

import holmes.analyse.InvariantsTools;
import holmes.analyse.MCTCalculator;
import holmes.darkgui.GUIManager;
import holmes.graphpanel.MauritiusMapPanel;
import holmes.petrinet.data.MauritiusMap;
import holmes.petrinet.data.MauritiusMap.BTNode;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.utilities.HolmesFileView;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa implementująca okno analizy poprzez mechanizm knockout oraz ścieżki Mauritiusa.:
 * 
 * "Petri net modelling of gene regulation of the Duchenne muscular dystrophy"
 * Stefanie Grunwald, Astrid Speer, Jorg Ackermann, Ina Koch
 * BioSystems, 2008, 92, pp.189-205
 * 
 * @author MR
 *
 */
public class HolmesKnockout extends JFrame {
	@Serial
	private static final long serialVersionUID = -9038958824842964847L;
	private JComboBox<String> transitionsCombo;
	private MauritiusMapPanel mmp;
	private int intX=0;
	private int intY=0;
	private JPanel mainPanel;
	private JPanel buttonPanel;
	private JPanel logMainPanel;
	private JScrollPane scroller;
	private MauritiusMap mmCurrentObject;
	
	private ArrayList<Integer> disabledSetByObjR = null;
	private ArrayList<Integer> commonSetToObjR = null;
	
	private int currentTreshold = 100;
	private boolean contractedMode = true;
	
	private int globalMode = 0;
	
	/**
	 * Konstruktor obiektu klasy HolmesKnockout
	 */
	public HolmesKnockout() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ignored) {
			
		}
		setVisible(false);
		this.setTitle("Knockout analysis");
		
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 750));
		setLocation(15, 15);
		
		mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
		
		addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	fillComboBoxData();
  	  	    }  
    	});
	}

	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel główny
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		//Panel wyboru opcji szukania
		buttonPanel = createUpperButtonPanel(0, 0, 900, 130);
		logMainPanel = createGraphPanel(0, 130, 900, 600);
		
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
	@SuppressWarnings("SameParameterValue")
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
		
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(currentTreshold, 0, 100, 1);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.setBounds(posX+620, posY, 70, 20);
		tokenSpinner.addChangeListener(e -> {
			JSpinner spinner = (JSpinner) e.getSource();
			currentTreshold = (int) spinner.getValue();
		});
		panel.add(tokenSpinner);

		final JComboBox<String> modeCombo = new JComboBox<String>(dataT);
		modeCombo.setBounds(posX+560, posY+20, 270, 20);
		modeCombo.setSelectedIndex(0);
		modeCombo.setMaximumRowCount(6);
		modeCombo.removeAllItems();
		modeCombo.addItem("Show tree for ramaining transitions");
		modeCombo.addItem("Show tree for knocked transitions");
		modeCombo.addItem("Show tree for knocked transitions (threshold)");
		modeCombo.addActionListener(actionEvent -> {
			int selected = modeCombo.getSelectedIndex();
			if(selected == 0) {
				globalMode = 0;
			} else if (selected == 1) {
				globalMode = 1;
			} else if (selected == 2) {
				globalMode = 2;
			} else {
				globalMode = 1;
			}
		});
		
		panel.add(modeCombo);
		
		JCheckBox contractedModeBox = new JCheckBox("Contracted");
		contractedModeBox.setBounds(posX+690, posY, 90, 20);
		contractedModeBox.setSelected(true);
		contractedModeBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			contractedMode = abstractButton.getModel().isSelected();
		});
		panel.add(contractedModeBox);
		
		JButton generateButton = new JButton("<html>&nbsp;Generate</html>");
		generateButton.setBounds(posX, posY+25, 120, 36);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/computeData.png"));
		generateButton.setToolTipText("Generate knockout map for the currently selected transition.");
		generateButton.addActionListener(actionEvent -> {
			//globalMode = 0;
			mmCurrentObject = generateMap(globalMode);
			if(mmCurrentObject != null) {
				paintMap();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		JButton showKnockoutButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Show<br>&nbsp;&nbsp;notepad</html>");
		showKnockoutButton.setBounds(posX, posY+65, 120, 36);
		showKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		showKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/showNotepad.png"));
		showKnockoutButton.setToolTipText("For the selected transition show knockout detailed data in the notepad.");
		showKnockoutButton.addActionListener(actionEvent -> {
			try {
				//globalMode = 1;
				MauritiusMap infoMap;
				if(globalMode == 0)
					infoMap = generateMap(1); //nie ma sensu dla 'remaining transitions', tylko dla knockoutowanych
				else
					infoMap = generateMap(globalMode);

				if(infoMap != null) {
					HolmesNotepad notePad = new HolmesNotepad(900,600);
					notePad.setVisible(true);
					getKnockoutInfo(infoMap, notePad);
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Exception in invariants section.\n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Error"+e.getMessage(), "error", true);
			} catch (Error e2) {
				JOptionPane.showMessageDialog(null, "Error in invariants section.\n"+e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				GUIManager.getDefaultGUIManager().log("Error"+e2.getMessage(), "error", true);
			}
		});
		showKnockoutButton.setFocusPainted(false);
		panel.add(showKnockoutButton);
		
		JButton fullDataKnockoutButton = new JButton("<html>Whole net<br>knockout</html>");
		fullDataKnockoutButton.setBounds(posX+130, posY+25, 120, 36);
		fullDataKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		fullDataKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/fullKnockout.png"));
		fullDataKnockoutButton.addActionListener(actionEvent -> {

			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);
			getKnockoutFullInfo(notePad);
		});
		fullDataKnockoutButton.setFocusPainted(false);
		panel.add(fullDataKnockoutButton);
		
		JButton toNetKnockoutButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Color&nbsp;&nbsp;&nbsp;<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;net&nbsp;&nbsp;&nbsp;</html>");
		toNetKnockoutButton.setBounds(posX+130, posY+65, 120, 36);
		toNetKnockoutButton.setMargin(new Insets(0, 0, 0, 0));
		toNetKnockoutButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/sendToNet.png"));
		toNetKnockoutButton.addActionListener(actionEvent -> {
			//globalMode = 1;
			MauritiusMap infoMap = generateMap(globalMode);
			if(infoMap != null) {
				getKnockoutInfoToNet(infoMap);
			}
		});
		toNetKnockoutButton.setFocusPainted(false);
		panel.add(toNetKnockoutButton);
		
		
		
		JButton monaLisaButton = new JButton("<html>Load Mona<br>Lisa data</html>");
		monaLisaButton.setBounds(posX+260, posY+25, 120, 36);
		monaLisaButton.setMargin(new Insets(0, 0, 0, 0));
		monaLisaButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/monaLisa.png"));
		monaLisaButton.addActionListener(actionEvent -> {
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);
			showMonaLisaResults(notePad);
		});
		monaLisaButton.setFocusPainted(false);
		panel.add(monaLisaButton);
		
		JButton monaLisaToNetButton = new JButton("<html>&nbsp;&nbsp;Mona Lisa<br>-> Color net</html>");
		monaLisaToNetButton.setBounds(posX+260, posY+65, 120, 36);
		monaLisaToNetButton.setMargin(new Insets(0, 0, 0, 0));
		monaLisaToNetButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/monaLisa.png"));
		monaLisaToNetButton.addActionListener(actionEvent -> exportMonaLisaToNet());
		monaLisaToNetButton.setFocusPainted(false);
		panel.add(monaLisaToNetButton);
		
		JButton saveImgButton = new JButton("<html>&nbsp;Save image</html>");
		saveImgButton.setBounds(posX+390, posY+25, 120, 36);
		saveImgButton.setMargin(new Insets(0, 0, 0, 0));
		saveImgButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/saveImage.png"));
		saveImgButton.addActionListener(actionEvent -> exportToPicture());
		saveImgButton.setFocusPainted(false);
		panel.add(saveImgButton);
		
		JButton expInvKnockButton = new JButton("<html>Inv. knockout</html>");
		expInvKnockButton.setBounds(posX+390, posY+65, 120, 36);
		expInvKnockButton.setMargin(new Insets(0, 0, 0, 0));
		expInvKnockButton.setIcon(Tools.getResIcon32("/icons/knockoutWindow/saaaamage.png"));
		expInvKnockButton.addActionListener(actionEvent -> invData());
		expInvKnockButton.setFocusPainted(false);
		panel.add(expInvKnockButton);
		
		JCheckBox shortTextCheckBox = new JCheckBox("Show full names");
		shortTextCheckBox.setBounds(posX+490, posY, 130, 20);
		shortTextCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			mmp.setFullName(abstractButton.getModel().isSelected());
			mmp.repaint();
		});
		shortTextCheckBox.setSelected(true);
		panel.add(shortTextCheckBox);
		
		return panel;
	}
	
	protected void invData() {
		// TODO Auto-generated method stub
		//HolmesNotepad notePad = new HolmesNotepad(900,600);
		//notePad.setVisible(true);
		
		
		
	}

	/**
	 * Metoda tworząca główmu panel mapy.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 * @param width int - szerokość panelu
	 * @param height int - wysokość panelu
	 * @return JPanel - panel przycisków
	 */
	@SuppressWarnings("SameParameterValue")
	private JPanel createGraphPanel(int x, int y, int width, int height) {
		JPanel gr_panel = new JPanel(new BorderLayout());
		//gr_panel.setBounds(x, y, width, height);
		
		gr_panel.setLocation(x, y);
		gr_panel.setPreferredSize(new Dimension(width, height));
		
		mmp = new MauritiusMapPanel(this);
		
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
	 * @param notePad HolmesNotepad - obiekt notatnika dla wyników
	 */
	protected void getKnockoutInfo(MauritiusMap infoMap, HolmesNotepad notePad) {
		ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(infoMap);
		
		if(dataMatrix.get(0).contains(-1))
			dataMatrix.get(0).remove(dataMatrix.get(0).indexOf(-1));
		if(dataMatrix.get(1).contains(-1))
			dataMatrix.get(1).remove(dataMatrix.get(1).indexOf(-1));
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		try {
			notePad.addTextLineNL("Objective reaction (knock-out reaction): "+infoMap.getRoot().transName, "text");
			notePad.addTextLineNL("", "text");
			
			if(dataMatrix.get(0).size() == 0) {
				notePad.addTextLine("Reactions knocked out: ", "text");
				notePad.addTextLineNL(" 0  (zero, all transitions present in some unaffected t-invariants).", "text");
			} else {
				notePad.addTextLineNL("Reactions knocked out: ", "text");
				Collections.sort(dataMatrix.get(0));
				for(int element : dataMatrix.get(0)) {
					notePad.addTextLineNL("[t_"+element+"] : "+transitions.get(element).getName(), "text");
				}
			}
			
			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL("Common-group (e.g., MCT set) reactions: ", "text");
			
			Collections.sort(dataMatrix.get(1));
			for(int element : dataMatrix.get(1)) {
				notePad.addTextLineNL("[t_"+element+"] : "+transitions.get(element).getName(), "text");
			}
			//knockOutData
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error in transition section.\n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error"+e.getMessage(), "error", true);
		}
		
		try {
			int rootTransition = transitionsCombo.getSelectedIndex()-1;
			ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
			ArrayList<Integer> invIndices = InvariantsTools.returnInvIndicesWithTransition(invariants, rootTransition);
			
			notePad.addTextLineNL("", "text");
			
			notePad.addTextLineNL("t-invariants unaffected by knockout: "+(invariants.size() - invIndices.size()) , "text");
			notePad.addTextLineNL("t-invariants disabled: "+invIndices.size() , "text");
			
			String name = transitions.get(rootTransition).getName();
			notePad.addTextLineNL("t-ivariants ("+invIndices.size() + ") with transition t_"+rootTransition+"_"+name+ " (disabled t-inv.):", "text");
			notePad.addTextLineNL("", "text");
			for(int element : invIndices) {
				notePad.addTextLine("i_"+element+" , ", "text");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error in invariants section.\n"+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			GUIManager.getDefaultGUIManager().log("Error"+e.getMessage(), "error", true);
		}
		notePad.setCaretFirstLine();
	}
	
	/**
	 * Metoda pokazuje wyliczone wszystkie tranzycje.
	 * @param notePad HolmesNotepad - obiekt notatnika
	 */
	//TODO:
	protected void getKnockoutFullInfo(HolmesNotepad notePad) {
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
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
		int mode = 1;
		if(globalMode != 0)
			mode = globalMode; //nie ma sensu dla 'remaining transitions' (case 0), a tylko dla knockoutowanych
		
		for(int t=0; t<transNumber; t++) {
			MauritiusMap mm = new MauritiusMap(invariants, t, currentTreshold, mode);
			ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(mm);
			transFailDependency.add(dataMatrix.get(0).size());
			transCommonSetSize.add(dataMatrix.get(1).size());
		}

		notePad.addTextLineNL("Data collected for "+transNumber+ " transitions.", "text");
		notePad.addTextLineNL("", "text");
		
		for(int t=0; t<transNumber; t++) {
			notePad.addTextLine("[t_"+t+ "]|"+transitions.get(t).getName()+":", "text");
			int kn = transFailDependency.get(t) + transCommonSetSize.get(t) - 1;
			//notePad.addTextLineNL("| Knocked-out: "+transFailDependency.get(t)+ "| Common: "+transCommonSetSize.get(t), "text");
			notePad.addTextLineNL("| Knocked-out: "+kn+ "| Common: "+transCommonSetSize.get(t), "text");
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
			String line = buffer.readLine(); //first line
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
		} catch (Exception ignored) {
			
		}

		GUIManager.getDefaultGUIManager().showKnockout(knockoutMatrix);
	}
	
	/**
	 * Metoda wczytuje plik z wynikami Knockout dla wszystkich tranzycji sieci wygenerowane w programie
	 * MonaLisa, a następnie wyświetla je w zunifikowanej formie w oknie.
	 * @param notePad HolmesNotepad - okno wyświetlania wyników
	 */
	private void showMonaLisaResults(HolmesNotepad notePad) {
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
		MCTCalculator analyzer = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCTanalyzer();
		ArrayList<ArrayList<Transition>> mct = analyzer.generateMCT();
		mct = MCTCalculator.getSortedMCT(mct, false);
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
			
			String line = buffer.readLine(); //first line
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
							float knockoutPercent = (float)elements.length/(float)transSize;
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
		} catch (Exception ignored) {
			
		}
		
		notePad.addTextLineNL("", "text");
		
		//wyświetlanie I bloku wyników:
		for(int t=0; t<transSize; t++) {
			notePad.addTextLineNL(resultLines.get(t), "text");
		}
		
		//przygotowanie II bloku wyników:
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("", "text");
		ArrayList<String> mctTmpVector = new ArrayList<String>();
		for(int m=0; m < mct.size(); m++) {
			mctTmpVector.add("MCT_"+(m+1));
		}
		
		ArrayList<Integer> transInInvVector = InvariantsTools.transInT_invariants();
		int invNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix().size();
		
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
						invPercent = (float)transInInvVector.get(ident)/(float)invNumber;
						invPercent *= 100;
					} catch (Exception ignored) { }
					
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
					invPercent = (float)transInInvVector.get(ident)/(float)invNumber;
					invPercent *= 100;
				} catch (Exception ignored) { }
						
				line = line.substring(line.indexOf("_")+1);
				String name = line.substring(0, line.indexOf("|")-1); //nazwa tranzycji
				
				line = line.substring(line.indexOf(":")+2);
				String transPercent = line.substring(0, line.indexOf("%")+1);
				
				secondResultLines.add("t"+id+"&_"+name+"   "+transPercent+"  "+String.format("%.2f", invPercent)+"%");
			}
		}
		
		
		//wyświetlanie II bloku wyników:
		for (String secondResultLine : secondResultLines) {
			notePad.addTextLineNL(secondResultLine, "text");
		}

		///III BLOK: sortowanie po inwariantach:
		
		int linesNumber = secondResultLines.size();
		for(int i=0; i<linesNumber; i++) {
			if(i >= linesNumber-1)
				break;
			//extract last column:
			String line = secondResultLines.get(i);
			line = line.substring(line.indexOf(" "));
			line = line.trim();
			line = line.substring(line.indexOf(" "));
			line = line.trim();
			line = line.substring(0, line.length()-1);
			line = line.replace(",", ".");
			int nextMax = -1;
			try {
				float value = Float.parseFloat(line);
				for(int j=i+1; j<linesNumber; j++) {
					String line2 = secondResultLines.get(j);
					line2 = line2.substring(line2.indexOf(" "));
					line2 = line2.trim();
					line2 = line2.substring(line2.indexOf(" "));
					line2 = line2.trim();
					line2 = line2.substring(0, line2.length()-1);
					line2 = line2.replace(",", ".");
					float value2 = Float.parseFloat(line2);
					
					if(value2 > value) {
						nextMax = j;
						value = value2;
					}
				}
				
				if(nextMax != -1) {
					String tmpLine = secondResultLines.get(i);
					String maxLine = secondResultLines.get(nextMax);
					secondResultLines.set(i, maxLine);
					secondResultLines.set(nextMax, tmpLine);
				}
				
			} catch (Exception ignored) {}
		}
		
		//wyświetlanie III bloku wyników:
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("", "text");
		
		notePad.addTextLineNL("{\\footnotesize", "text");
		notePad.addTextLineNL("\\begin{longtable}{| p{2.1cm} | p{8.2cm} | p{1.9cm} | p{1.9cm} |}", "text");
		notePad.addTextLineNL("\\caption{The impact of knockout of net elements depending on affected transitions} ", "text");
		notePad.addTextLineNL("\\label{tab:knockout} \\\\", "text");
		notePad.addTextLineNL("\\hline ", "text");
		notePad.addTextLineNL("\\bf MCT-set & \\bf \\centering{Biological function}  & \\bf Affected & \\bf Affected	\\\\", "text");
		notePad.addTextLineNL("\\bf / transition & \\bf  & \\bf transition & \\bf invariants \\\\  \\hline ", "text");
		for (String line2 : secondResultLines) {
			if (line2.contains("MCT")) {
				line2 = line2.replace("   ", " & & ");
			} else {
				line2 = line2.replace("   ", " & ");

			}
			line2 = line2.replace("%  ", "% & ");
			line2 = line2 + "  \\\\ \\hline  ";
			line2 = line2.replace("%", "\\%");
			line2 = line2.replace("_", " ");
			notePad.addTextLineNL(line2, "text");
		}
		notePad.addTextLineNL("\\end{longtable}", "text");
		notePad.addTextLineNL("}", "text");
	}
	
	
	
	/**
	 * Metoda przesyła dane o knockout na obraz sieci.
	 * @param infoMap (<b>MauritiusMap</b>) obiekt mapy.
	 */
	protected void getKnockoutInfoToNet(MauritiusMap infoMap) {
		ArrayList<ArrayList<Integer>> dataMatrix = collectMapData(infoMap);
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		
		try {
			pn.resetNetColors();

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
		} catch (Exception ignored) {
			
		}
	}
	
	/**
	 * Metoda uruchamia przeglądanie mapy a następnie agreguje wynik do obiektu wyjściowego.
	 * @param infoMap MauritiusMapBT - obiekt mapu
	 * @return ArrayList[ArrayList[Integer]] - pierszy zbiór (.get(0) = disabledSetByObjR) to reakcje wyłączane
	 *  przez objR, drugi zbiór (.get(1) = commonSetToObjR) to reakcje o tej samej frekwencji co objR.
	 */
	private ArrayList<ArrayList<Integer>> collectMapData(MauritiusMap infoMap) {
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
			if(!commonSetToObjR.contains(transID)) {
				commonSetToObjR.add(transID);
			}
		} else {
			if(!disabledSetByObjR.contains(transID)) {
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
	 * @param mode int - 0: tworzy drzewo dla pozostałych inv/trans, 1: tworzy drzewo dla deaktywowanych inv/trans,
	 * 		2: tworzy drzewo dla deaktywowanych inv/trans z progiem
	 * @return MauritiusMapBT - obiekt mapy
	 */
	private MauritiusMap generateMap(int mode) {
		int selection = transitionsCombo.getSelectedIndex();
		if(selection == 0) {
			JOptionPane.showMessageDialog(null, "Please choose main reaction for Mauritius Map.", 
					"No selection", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}
		
		selection--;
		
		ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
		if(invariants == null || invariants.size() < 1) {
			JOptionPane.showMessageDialog(null, "Invariants matrix empty! Operation cannot start.", 
					"Warning", JOptionPane.INFORMATION_MESSAGE);
			return null;
		}

		MauritiusMap mm;
		try {
			mm = new MauritiusMap(invariants, selection, currentTreshold, mode);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Mauritius Map creation failed.", 
					"Error", JOptionPane.INFORMATION_MESSAGE);
			mm = null;
		}
		return mm;
	}
	
	/**
	 * Metoda uruchamiania przerysowanie wyliczonej mapy.
	 */
	private void paintMap() {
		mmp.registerNewMap(mmCurrentObject, contractedMode);
		mmp.repaint();
	}
	
	/**
	 * Metoda konwertująca obraz na panelu MM do obrazka
	 * @return BufferedImage - obrazek
	 */
	public BufferedImage getImageFromPanel() {
		MauritiusMap mm = generateMap(globalMode);
		mmp.registerNewMap(mm, contractedMode);
		mmp.repaint();
		
		int w = mmp.getWidth();
	    int h = mmp.getHeight();
	    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = bi.createGraphics();
	    g.setColor(Color.white);
	    mmp.paint(g);
	    return bi;
		
		//Rectangle r = getBounds();
		//BufferedImage image = new BufferedImage(r.width, r.height, BufferedImage.TYPE_INT_RGB);
		//Graphics g = image.getGraphics();
		//g.setColor(Color.white);
		//g.fillRect(0, 0, getWidth(), getHeight());
		//drawPetriNet((Graphics2D) g.create());
		//return image;
	}
	
	private void exportToPicture() {
		//String lastPath = getGraphPanel().getPetriNet().getWorkspace().getGUI().getLastPath();
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		JFileChooser fc;
		if(lastPath == null)
			fc = new JFileChooser();
		else
			fc = new JFileChooser(lastPath);
		
		fc.setFileView(new HolmesFileView());
		FileFilter pngFilter = new ExtensionFileFilter(".png - Portable Network Graphics", new String[] { "png" });
		FileFilter bmpFilter = new ExtensionFileFilter(".bmp -  Bitmap Image File", new String[] { "bmp" });
		FileFilter jpegFilter = new ExtensionFileFilter(".jpeg - JPEG Image File", new String[] { "jpeg" });
		FileFilter jpgFilter = new ExtensionFileFilter(".jpg - JPEG Image File", new String[] { "jpg" });
		fc.setFileFilter(pngFilter);
		fc.addChoosableFileFilter(pngFilter);
		fc.addChoosableFileFilter(bmpFilter);
		fc.addChoosableFileFilter(jpegFilter);
		fc.addChoosableFileFilter(jpgFilter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			String ext = "";
			String extension = fc.getFileFilter().getDescription();
			if (extension.contains(".png")) ext = ".png";
			if (extension.contains(".bmp")) ext = ".bmp";
			if (extension.contains(".jpeg") || extension.contains(".jpg")) ext = ".jpeg";
			
			BufferedImage image = getImageFromPanel();
			try {
				String ext2 = "";
				String path = file.getPath();
				if(ext.equals(".png") && !(path.contains(".png"))) ext2 = ".png";
				if(ext.equals(".bmp") && !file.getPath().contains(".bmp")) ext2 = ".bmp";
				if(ext.equals(".jpeg") && !file.getPath().contains(".jpeg")) ext2 = ".jpeg";
				if(ext.equals(".jpeg") && !file.getPath().contains(".jpg")) ext2 = ".jpg";
				
				ImageIO.write(image, ext.substring(1), new File(file.getPath() + ext2));
				
				GUIManager.getDefaultGUIManager().setLastPath(file.getParentFile().getPath());
				
				//getGraphPanel().getPetriNet().getWorkspace().getGUI().setLastPath(
				//		file.getParentFile().getPath()); //  ╯°□°）╯ ︵  ┻━━━┻
			} catch (IOException ex) {
				ex.printStackTrace();
				JOptionPane.showMessageDialog(null,
						"Saving net sheet into picture failed.",
						"Export Picture Error",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	/**
	 * Metoda odpowiedzialna za dopasowywanie elementów okna.
	 */
	@SuppressWarnings("unused")
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
