package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.InvariantsCalculatorFeasible;
import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.files.io.IOprotocols;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypesOfArcs;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.utilities.Tools;
import holmes.varia.Check;
import holmes.workspace.ExtensionFileFilter;

/**
 * Okno generatora inwariantów i związanych z nimi narzędzi.
 * @author MR
 *
 */
public class HolmesInvariants extends JFrame {
	private static final long serialVersionUID = 5805567123988000425L;
	private boolean tInvCalculation = true;
	private JTextArea logField;
	private InvariantsCalculator invGenerator = null;
	public boolean isGeneratorWorking = false;
	public boolean noAction = false;
	private boolean details = true;
	private int feasibleCalcMode = 1; 

	/**
	 * Główny konstruktor okna generatora inwariantów.
	 */
	public HolmesInvariants() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
		setVisible(false);
		this.setTitle("Invariants generator and tools");
		//ego = this;
		
		setLayout(new BorderLayout());
		setSize(new Dimension(666, 520));
		setLocation(50, 50);
		setResizable(false);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		JPanel mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
	}

	/**
	 * Metoda tworząca główny panel okna.
	 * @return JPanel - panel główny
	 */
	private JPanel createMainPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(null); /**  ╯°□°）╯︵  ┻━┻   */
		
		//Panel wyboru opcji szukania
		JPanel buttonPanel = createUpperButtonPanel(0, 0, 660, 90);
		JPanel logMainPanel = createLogMainPanel(0, 90, 530, 400);
		JPanel sideButtonPanel = createRightButtonPanel(530, 90, 130, 400);
		
		panel.add(buttonPanel);
		panel.add(logMainPanel);
		panel.add(sideButtonPanel);
		panel.repaint();
		return panel;
	}

	/**
	 * Metoda tworząca górny panel przycisków.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createUpperButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 10;
		
		// przycisk generatora inwariantów
		JButton generateButton = new JButton("Generate");
		generateButton.setBounds(posX, posY, 110, 60);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(tInvCalculation == true) {
					if(isGeneratorWorking == true) {
						JOptionPane.showMessageDialog(null, "Invariants generation already in progress.", 
								"Generator working",JOptionPane.WARNING_MESSAGE);
					} else {
						setGeneratorStatus(true);
						invGenerator = new InvariantsCalculator(true);
						Thread myThread = new Thread(invGenerator);
						myThread.start();
					}
				} else {
					//JOptionPane.showMessageDialog(ego, "Not implementet yet! Sorry!", "Warning", JOptionPane.INFORMATION_MESSAGE);
					if(isGeneratorWorking == true) {
						JOptionPane.showMessageDialog(null, "Invariants generation already in progress.", 
								"Generator working",JOptionPane.WARNING_MESSAGE);
					} else {
						setGeneratorStatus(true);
						invGenerator = new InvariantsCalculator(false);
						Thread myThread = new Thread(invGenerator);
						myThread.start();
					}
				}
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);

		// USTAWIANIE T/P INWARIANTÓW
		ButtonGroup group = new ButtonGroup();
		JRadioButton tInvariantsMode = new JRadioButton("T-invariants (EM)");
		tInvariantsMode.setBounds(posX+120, posY-3, 120, 20);
		tInvariantsMode.setActionCommand("0");
		ActionListener tInvariantsModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				tInvCalculation = true;
			}
		};
		tInvariantsMode.addActionListener(tInvariantsModeActionListener);
		panel.add(tInvariantsMode);
		group.add(tInvariantsMode);
		group.setSelected(tInvariantsMode.getModel(), true);
		
		JRadioButton pInvariantsMode = new JRadioButton("P-invariants");
		pInvariantsMode.setBounds(posX+120, posY+15, 120, 20);
		pInvariantsMode.setActionCommand("0");
		ActionListener pInvariantsModeActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				tInvCalculation = false;
			}
		};
		pInvariantsMode.addActionListener(pInvariantsModeActionListener);
		panel.add(pInvariantsMode);
		group.add(pInvariantsMode);
		
		/*
		JCheckBox feasibilityCheckBox = new JCheckBox("Check feasibility", true);
		feasibilityCheckBox.setBounds(posX+120, posY+40, 120, 20);
		feasibilityCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					feasibilityTest = true;
				} else {
					feasibilityTest = false;
				}
			}
		});
		feasibilityCheckBox.setSelected(true);		
		panel.add(feasibilityCheckBox);
		*/
		
		// INA GENERATOR
		JButton INAgenerateButton = new JButton();
		INAgenerateButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;INA <br />generator</html>");
		INAgenerateButton.setBounds(posX+250, posY, 120, 32);
		INAgenerateButton.setMargin(new Insets(0, 0, 0, 0));
		INAgenerateButton.setIcon(Tools.getResIcon22("/icons/invWindow/inaGenerator.png"));
		INAgenerateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				setGeneratorStatus(true);
				GUIManager.getDefaultGUIManager().io.generateINAinvariants();
				GUIManager.getDefaultGUIManager().reset.setInvariantsStatus(true);
				GUIManager.getDefaultGUIManager().accessNetTablesWindow().resetInvData();
			}
		});
		INAgenerateButton.setFocusPainted(false);
		panel.add(INAgenerateButton);
		
		JButton loadInvariantsButton = new JButton();
		loadInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Load <br />invariants</html>");
		loadInvariantsButton.setBounds(posX+380, posY, 120, 32);
		loadInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		loadInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/loadInvariants.png"));
		loadInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().accessNetTablesWindow().resetInvData();
				GUIManager.getDefaultGUIManager().io.loadExternalAnalysis();
				logField.append("\n");
				logField.append("=====================================================================\n");
				logField.append("Loaded invariants: "+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix().size()+"\n");
				logField.append("=====================================================================\n");
			}
		});
		loadInvariantsButton.setFocusPainted(false);
		panel.add(loadInvariantsButton);
		
		JButton saveInvariantsButton = new JButton();
		saveInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;Export <br />invariants</html>");
		saveInvariantsButton.setBounds(posX+510, posY, 120, 32);
		saveInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/saveInvariants.png"));
		saveInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				GUIManager.getDefaultGUIManager().io.exportGeneratedInvariants();
				logField.append("\n");
				logField.append("=====================================================================\n");
				logField.append("Saved invariants: "+GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix().size()+"\n");
				logField.append("=====================================================================\n");
			}
		});
		saveInvariantsButton.setFocusPainted(false);
		panel.add(saveInvariantsButton);
		
		//**************************************************************************************************
		
		JButton showInvariantsButton = new JButton();
		showInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Show <br />invariants</html>");
		showInvariantsButton.setBounds(posX+250, posY+36, 120, 32);
		showInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		showInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		showInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//fillData();
			}
		});
		showInvariantsButton.setFocusPainted(false);
		showInvariantsButton.setEnabled(false);
		panel.add(showInvariantsButton);
		
		JButton makeFeasibleButton = new JButton();
		makeFeasibleButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Make <br />&nbsp;&nbsp;feasible</html>");
		makeFeasibleButton.setBounds(posX+380, posY+36, 120, 32);
		makeFeasibleButton.setMargin(new Insets(0, 0, 0, 0));
		makeFeasibleButton.setIcon(Tools.getResIcon22("/icons/invWindow/makeFeasible.png"));
		makeFeasibleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				checkAndMakeFeasible();
			}
		});
		makeFeasibleButton.setFocusPainted(false);
		panel.add(makeFeasibleButton);
		
		JCheckBox feasModeCheckBox = new JCheckBox("Feasible adv. mode");
		feasModeCheckBox.setBounds(posX+505, posY+36, 140, 20);
		feasModeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					feasibleCalcMode = 1;
				} else {
					feasibleCalcMode = 0;
				}
			}
		});
		feasModeCheckBox.setSelected(true);
		panel.add(feasModeCheckBox);

		return panel;
	}

	/**
	 * Metoda tworząca środkowy panel okna logów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createLogMainPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Log"));
		panel.setBounds(x, y, width, height);
		
		logField = new JTextArea();
		logField.setLineWrap(true);
		logField.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logField.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-20, height-25);
        panel.add(logFieldPanel);
        
		return panel;
	}
	
	/**
	 * Metoda tworząca prawy panel przycisków.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createRightButtonPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Tools"));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 18;
		
		JButton cardinalityButton = new JButton();
		cardinalityButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Check<br />&nbsp;&nbsp;&nbsp;canonity</html>");
		cardinalityButton.setBounds(posX, posY, 110, 32);
		cardinalityButton.setMargin(new Insets(0, 0, 0, 0));
		cardinalityButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_canon.png"));
		cardinalityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No invariants to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logField.append("\n");
					logField.append("=====================================================================\n");
					logField.append("Checking canonicality for "+invariants.size()+" invariants.\n");
					int card = InvariantsTools.checkCanonity(invariants);
					logField.append("Non canonical invariants: "+card+"\n");
					logField.append("=====================================================================\n");
				}
				
				
			}
		});
		cardinalityButton.setFocusPainted(false);
		panel.add(cardinalityButton);
		
		JButton minSuppButton = new JButton();
		minSuppButton.setText("<html>Check sup.<br />&nbsp;minimality</html>");
		minSuppButton.setBounds(posX, posY+38, 110, 32);
		minSuppButton.setMargin(new Insets(0, 0, 0, 0));
		minSuppButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_minsup.png"));
		minSuppButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No invariants to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logField.append("\n");
					logField.append("=====================================================================\n");
					logField.append("Checking support minimality for "+invariants.size()+" invariants.\n");
					int value = InvariantsTools.checkSupportMinimality(invariants);
					logField.append("Non support-minimal invariants: "+value+"\n");
					logField.append("=====================================================================\n");
				}
			}
		});
		minSuppButton.setFocusPainted(false);
		panel.add(minSuppButton);
		
		JButton checkMatrixZeroButton = new JButton();
		checkMatrixZeroButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Check<br />&nbsp;&nbsp;&nbsp;&nbsp;C&nbsp;&middot;&nbsp;x = 0</html>");
		checkMatrixZeroButton.setBounds(posX, posY+76, 110, 32);
		checkMatrixZeroButton.setMargin(new Insets(0, 0, 0, 0));
		checkMatrixZeroButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_invC.png"));
		checkMatrixZeroButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No invariants to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logField.append("\n");
					logField.append("=====================================================================\n");
					logField.append("Checking invariants correctness for "+invariants.size()+" invariants.\n");
					InvariantsCalculator ic = new InvariantsCalculator(true);
					
					//int value =  InvariantsTools.countNonInvariants(ic.getCMatrix(), invariants);
					ArrayList<ArrayList<Integer>> results = InvariantsTools.countNonT_InvariantsV2(ic.getCMatrix(), invariants);
					logField.append("Proper invariants (Cx = 0): "+results.get(0).get(0)+"\n");
					logField.append("Sur-invariants (Cx > 0): "+results.get(0).get(1)+"\n");
					logField.append("Sub-invariants (Cx < 0): "+results.get(0).get(2)+"\n");
					logField.append("Non-invariants (Cx <=> 0): "+results.get(0).get(3)+"\n");
					//logField.append("Non-invariants: "+value+"\n");
					logField.append("=====================================================================\n");
					
					if(details)
						showSubSurInvInfo(results, invariants.size());
				}
			}
		});
		checkMatrixZeroButton.setFocusPainted(false);
		panel.add(checkMatrixZeroButton);
		
		JButton loadRefButton = new JButton();
		loadRefButton.setText("<html>&nbsp;&nbsp;&nbsp;Ref. set<br />&nbsp;&nbsp;&nbsp;compare</html>");
		loadRefButton.setBounds(posX, posY+114, 110, 32);
		loadRefButton.setMargin(new Insets(0, 0, 0, 0));
		loadRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		loadRefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				testReference();
			}
		});
		loadRefButton.setFocusPainted(false);
		panel.add(loadRefButton);
		
		
		JButton testRefButton = new JButton();
		testRefButton.setText("<html>&nbsp;&nbsp;Incidence<br />&nbsp;&nbsp;&nbsp;matrix</html>");
		testRefButton.setBounds(posX, posY+180, 110, 32);
		testRefButton.setMargin(new Insets(0, 0, 0, 0));
		testRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		testRefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				getIncMatrix();
			}
		});
		testRefButton.setFocusPainted(false);
		panel.add(testRefButton);
		
		JCheckBox detailsCheckBox = new JCheckBox("Details");
		detailsCheckBox.setBounds(posX, posY+144, 110, 20);
		detailsCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					details = true;
				} else {
					details = false;
				}
			}
		});
		detailsCheckBox.setSelected(true);
		panel.add(detailsCheckBox);

		return panel;
	}
	
	protected void getIncMatrix() {
		HashMap<Place, Integer> placesMap = new HashMap<Place, Integer>();
		HashMap<Transition, Integer> transitionsMap = new HashMap<Transition, Integer>();
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		ArrayList<Arc> arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		
		for (int i = 0; i < places.size(); i++) {
			placesMap.put(places.get(i), i);
		}
		for (int i = 0; i < transitions.size(); i++) {
			transitionsMap.put(transitions.get(i), i);
		}
		
		ArrayList<ArrayList<Integer>> globalIncidenceMatrix = new ArrayList<ArrayList<Integer>>();

		//tworzenie macierzy TP - precyzyjnie do obliczeń T-inwariantów
		for (int trans = 0; trans < transitions.size(); trans++) {
			ArrayList<Integer> transRow = new ArrayList<Integer>();
			for (int place = 0; place < places.size(); place++) {
				transRow.add(0);
			}
			globalIncidenceMatrix.add(transRow);
		}
		//wypełnianie macierzy incydencji
		for (Arc oneArc : arcs) {
			int tPosition = 0;
			int pPosition = 0;
			int incidenceValue = 0;
			
			if(oneArc.getArcType() != TypesOfArcs.NORMAL) {
				continue;
			}

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidenceValue = 1 * oneArc.getWeight();
				
				//Transition tr = (Transition) oneArc.getStartNode();
				//Place pl = (Place) oneArc.getEndNode();
				//int tr_pos = transitions.indexOf(tr);
				//int pl_pos = places.indexOf(pl);
			} else { //miejsca
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				incidenceValue = -1 * oneArc.getWeight();
				
				//Transition tr = (Transition) oneArc.getEndNode();
				//Place pl = (Place) oneArc.getStartNode();
				//int tr_pos = transitions.indexOf(tr);
				//int pl_pos = places.indexOf(pl);
			}
			int oldValue = globalIncidenceMatrix.get(tPosition).get(pPosition);
			if(oldValue != 0) { //detekcja łuków podwójnych
				ArrayList<Integer> hiddenReadArc = new ArrayList<Integer>();
				hiddenReadArc.add(pPosition);
				hiddenReadArc.add(tPosition);
			}
			
			globalIncidenceMatrix.get(tPosition).set(pPosition, oldValue+incidenceValue);
		}
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		
		notePad.addTextLineNL("", "text");
		for(int t=0; t<globalIncidenceMatrix.size(); t++) {
			ArrayList<Integer> transRow = globalIncidenceMatrix.get(t);
			String text = "t"+t+" ";
			
			for(int p=0; p<transRow.size(); p++) {
				text += " "+transRow.get(p);
			}
			notePad.addTextLineNL(text, "text");
		}
	}

	/**
	 * Metoda wczytująca nowy plik inwariantów (o ile już jakieś są w systemie - celem bycia zbiorem referencyjnym w
	 * stosunku do wczytanego tutaj) oraz porównująca go ze zbiorem referencyjnym.
	 */
	protected void testReference() {
		ArrayList<ArrayList<Integer>> invariants = Check.invExistsWithWarning();
		if(invariants == null)
			return;
		
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("INA Invariants file (.inv)", new String[] { "INV" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load invariants", "Select invariant file", "");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists()) return;
		
		IOprotocols io = new IOprotocols();
		//GUIManager.getDefaultGUIManager().getWorkspace().getProject().getCommunicator();
		boolean status = io.readINV(file.getPath());
		if(status == false) {
			return;
		}
		refTest(invariants, io.getInvariantsList());
	}

	/**
	 * Metoda porównująca zbiory inwariantów: referencyjny i osobno wczytany.
	 * @param invRefMatrix ArrayList[ArrayList[Integer]] - zbiór referencyjny inwariantów
	 * @param invLoadedMatrix ArrayList[ArrayList[Integer]] - zbiór do porównania
	 */
	private void refTest(ArrayList<ArrayList<Integer>> invRefMatrix, ArrayList<ArrayList<Integer>> invLoadedMatrix) {
		if(invRefMatrix != null) {
			
			ArrayList<ArrayList<Integer>> res =  InvariantsTools.compareInv(invRefMatrix, invLoadedMatrix);
			logField.append("\n");
			logField.append("=====================================================================\n");
			logField.append("Prev. computed set size:   "+invRefMatrix.size()+"\n");
			logField.append("Loaded (now) set size:    "+invLoadedMatrix.size()+"\n");
			logField.append("Common set size (load & ref): "+res.get(0).size()+"\n");
			logField.append("Loaded invariants not in a computed set:  "+res.get(1).size()+"\n");
			logField.append("Computed invariants not in a loaded set:  "+res.get(2).size()+"\n");
			logField.append("Repetitions in common set: "+res.get(3).get(0)+"\n");
			logField.append("Total repetitions in loaded:"+res.get(3).get(1)+"\n");
			logField.append("\n");
			logField.append("Inititating further tests for the loaded set of "+invLoadedMatrix.size()+" invariants.\n");
			int card = InvariantsTools.checkCanonity(invLoadedMatrix);
			logField.append("-> Non canonical invariants found : "+card+"\n");
			int value = InvariantsTools.checkSupportMinimality(invLoadedMatrix);
			logField.append("-> Non support-minimal inv. found: "+value+"\n");
			InvariantsCalculator ic = new InvariantsCalculator(true);
			//value =  InvariantsTools.countNonInvariants(ic.getCMatrix(), invLoadedMatrix);
			
			ArrayList<ArrayList<Integer>> results = InvariantsTools.countNonT_InvariantsV2(ic.getCMatrix(), invLoadedMatrix);
			logField.append("Proper invariants (Cx = 0): "+results.get(0).get(0)+"\n");
			logField.append("Sur-invariants (Cx > 0): "+results.get(0).get(1)+"\n");
			logField.append("Sun-invariants (Cx < 0): "+results.get(0).get(2)+"\n");
			logField.append("Non-invariants (Cx <=> 0): "+results.get(0).get(3)+"\n");
			logField.append("=====================================================================\n");
			logField.append("\n");
			
			if(details)
				showSubSurInvInfo(results, invLoadedMatrix.size());
		}
	}
	
	/**
	 * Metoda obsługuje pokazywanie informacji o sub i sur-inwariantach.
	 * @param results ArrayList[ArrayList[Integer]] - macierz danych o sub i sur-inwariantach
	 * @param invMatrixSize int - rozmiar macierzy inwariantów
	 */
	private void showSubSurInvInfo(ArrayList<ArrayList<Integer>> results, int invMatrixSize) {
		int surPlaceMaxName = 0;
		int subPlaceMaxName = 0;
		ArrayList<Place> places = null;
		int size = 0;
		ArrayList<Integer> surInvVector = results.get(1);
		ArrayList<Integer> subInvVector = results.get(2);
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Vectors analysed: "+invMatrixSize, "text");
		notePad.addTextLineNL("Canonical invariants: "+results.get(0).get(0), "text");
		notePad.addTextLineNL("Sur-invariants: "+results.get(0).get(1), "text");
		notePad.addTextLineNL("Sub-invariants: "+results.get(0).get(2), "text");
		notePad.addTextLineNL("Non invariants vectors: "+results.get(0).get(3), "text");
		notePad.addTextLineNL("", "text");
		
		if(results.get(0).get(1) > 0 || results.get(0).get(2) > 0) {
			places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
			size = places.size();
			
			// ustal maksymalną długość nazwy miejsca dla obu zbiorów:
			for(int p=0; p<size; p++) {
				int value = surInvVector.get(p);
				if(value != 0) {
					int nameSize = places.get(p).getName().length();
					if(nameSize > surPlaceMaxName)
						surPlaceMaxName = nameSize;
				}
				value = subInvVector.get(p);
				if(value != 0) {
					int nameSize = places.get(p).getName().length();
					if(nameSize > subPlaceMaxName)
						subPlaceMaxName = nameSize;
				}
			}
		}
		
		if(results.get(0).get(1) > 0) {
			notePad.addTextLineNL("Places for which sur-invariants did not zeroed C-matrix:", "text");
			notePad.addTextLineNL("(in parenthesis number of sur-invariants for each place):", "text");
			
			
			for(int p=0; p<size; p++) {
				int value = surInvVector.get(p);
				if(value != 0) {
					String line = "p_"+p+Tools.setToSize(places.get(p).getName(), surPlaceMaxName+3, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
		notePad.addTextLineNL("", "text");
		if(results.get(0).get(2) > 0) {
			notePad.addTextLineNL("Places for which sub-invariants did not zeroed C-matrix:", "text");
			notePad.addTextLineNL("(in parenthesis number of sub-invariants for each place):", "text");
			
			size = places.size();
			for(int p=0; p<size; p++) {
				int value = subInvVector.get(p);
				if(value != 0) {
					String line = "p_"+p+Tools.setToSize(places.get(p).getName(), subPlaceMaxName+2, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za obsługę przycisku tworzenia zbioru wykonalnych inwariantów.
	 */
	protected void checkAndMakeFeasible() {
		ArrayList<ArrayList<Integer>> invariants = Check.invExistsWithWarning();
		if(invariants == null)
			return;
		
		if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().isBackup == true) {
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().restoreMarkingZero();
		}
		
		InvariantsCalculatorFeasible invF = new InvariantsCalculatorFeasible(invariants, true);
		invariants = invF.getMinFeasible(feasibleCalcMode);
		
		Object[] options = {"Save & replace", "Save only", "Replace only", "Cancel"};
		int n = JOptionPane.showOptionDialog(null,
						"New (feasible) invariants set computed. What to do now?\n"
						+ "Save it to file and replace the current set.\n"
						+ "Save it to file only (do not replace current set).\n"
						+ "Do not save to file, only replace current set.\n"
						+ "Discart new feasible invariants set.",
						"What to do?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[3]);
		if (n == 0) {
			PetriNet project = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
			project.setINVmatrix(invariants, false);
			GUIManager.getDefaultGUIManager().io.exportGeneratedInvariants();
			GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(project.getINVmatrix());
		} else if(n == 1) {
			ArrayList<ArrayList<Integer>> invBackup = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getINVmatrix();
			try {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().setINVmatrix(invariants, false);
				GUIManager.getDefaultGUIManager().io.exportGeneratedInvariants();
			} catch (Exception e) {}
			finally {
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().setINVmatrix(invBackup, false);
			}
		} else if(n == 2) {
			PetriNet project = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
			project.setINVmatrix(invariants, false);
			GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(project.getINVmatrix());
		} //else: nic
	}
	
	/**
	 * Metoda umożliwia dostęp do kompontentu wyświetlania logów generatora inwariantów.
	 * @return JTextArea - obiekt logów
	 */
	public JTextArea accessLogField() {
		return logField;
	}
	
	/**
	 * Metoda resetuje połączenie z wątkiem generatora.
	 */
	public void resetInvariantGenerator() {
		invGenerator = null;
		setGeneratorStatus(false);
	}
	
	/**
	 * Metoda ustawia status generatora MCS.
	 * @param status boolean - true, jeśli właśnie działa w tle
	 */
	public void setGeneratorStatus(boolean status) {
		isGeneratorWorking = status;
	}
}
