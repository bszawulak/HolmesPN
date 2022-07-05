package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.utilities.Tools;
import holmes.varia.Check;
import holmes.workspace.ExtensionFileFilter;

/**
 * Okno generatora inwariantów i związanych z nimi narzędzi.
 * 
 * @author MR
 */
public class HolmesInvariantsGenerator extends JFrame {
	private static final long serialVersionUID = 5805567123988000425L;
	private GUIManager overlord;
	private JFrame ego;
	private JTextArea logFieldTinv;
	private JTextArea logFieldPinv;
	private InvariantsCalculator invGenerator = null;
	public boolean isGeneratorWorking = false;
	public boolean noAction = false;
	private boolean detailsTinv = true;
	private boolean detailsPinv = true;
	private int feasibleCalcMode = 1;
	
	private boolean showInvDiff = false;

	/**
	 * Główny konstruktor okna generatora inwariantów.
	 */
	public HolmesInvariantsGenerator() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {
			
		}
		this.ego = this;
		setVisible(false);
		this.setTitle("Invariants generator and tools");
		this.overlord = GUIManager.getDefaultGUIManager();
		//ego = this;
		
		setLayout(new BorderLayout());
		setSize(new Dimension(666, 560));
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
		JPanel main = new JPanel(new BorderLayout());

		JPanel panelTinv = new JPanel();
		panelTinv.setLayout(null); /**  ╯°□°）╯︵  ┻━┻   */
		JPanel buttonPanelT = createUpperButtonPanelTinv(0, 0, 660, 90);
		JPanel logMainPanelT = createLogMainPanelTinv(0, 90, 530, 400);
		JPanel sideButtonPanelT = createRightButtonPanelTinv(530, 90, 130, 400);
		
		panelTinv.add(buttonPanelT);
		panelTinv.add(logMainPanelT);
		panelTinv.add(sideButtonPanelT);
		panelTinv.repaint();
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("T-invariants", Tools.getResIcon22("/icons/invWindow/tInvIcon.png"), panelTinv, "T-invariants");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		
		JPanel panelPinv = new JPanel();
		panelPinv.setLayout(null); /**  ╯°□°）╯︵  ┻━┻   */
		JPanel buttonPanelP = createUpperButtonPanelPinv(0, 0, 660, 90);
		JPanel logMainPanelP = createLogMainPanelPinv(0, 90, 530, 400);
		JPanel sideButtonPanelP = createRightButtonPanelPinv(530, 90, 130, 400);
		
		panelPinv.add(buttonPanelP);
		panelPinv.add(logMainPanelP);
		panelPinv.add(sideButtonPanelP);
		panelPinv.repaint();
		
		
		tabbedPane.addTab("P-invariants", Tools.getResIcon22("/icons/invWindow/pInvIcon.png"), panelPinv, "P-invariants");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		
		main.add(tabbedPane, BorderLayout.CENTER);
		
		return main;
	}

	/**
	 * Metoda tworząca górny panel przycisków dla t-inwariantów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createUpperButtonPanelTinv(int x, int y, int width, int height) {
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
		generateButton.addActionListener(actionEvent -> {
			if(isGeneratorWorking == true) {
				JOptionPane.showMessageDialog(null, "Invariants generation already in progress.",
						"Generator working",JOptionPane.WARNING_MESSAGE);
			} else {
				setGeneratorStatus(true);
				invGenerator = new InvariantsCalculator(true);
				invGenerator.setShowInvDiff(showInvDiff);
				Thread myThread = new Thread(invGenerator);
				myThread.start();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);
		
		// INA GENERATOR
		JButton INAgenerateButton = new JButton();
		INAgenerateButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;INA<br>generator</html>");
		INAgenerateButton.setBounds(posX+130, posY, 120, 36);
		INAgenerateButton.setMargin(new Insets(0, 0, 0, 0));
		INAgenerateButton.setIcon(Tools.getResIcon22("/icons/invWindow/inaGenerator.png"));
		INAgenerateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(overlord.getINAStatus()) {
					setGeneratorStatus(true);
					overlord.io.generateINAinvariants(true);
					overlord.reset.setT_invariantsStatus(true);
					overlord.accessNetTablesWindow().resetT_invData();
					overlord.markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "INAwin32.exe status set to non ready. Please read initial warnings\n"
							+ "in the Holmes log windows for more information.", "INAwin32 problem",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		INAgenerateButton.setFocusPainted(false);
		panel.add(INAgenerateButton);
		
		JButton loadInvariantsButton = new JButton();
		loadInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Load<br>t-invariants</html>");
		loadInvariantsButton.setBounds(posX+255, posY, 120, 36);
		loadInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		loadInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/loadInvariants.png"));
		loadInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				overlord.accessNetTablesWindow().resetT_invData();
				boolean status = overlord.io.loadExternalAnalysis(true);
				if(status) {
					logFieldTinv.append("\n");
					logFieldTinv.append("=====================================================================\n");
					logFieldTinv.append("Loaded t-invariants: "+overlord.getWorkspace().getProject().getT_InvMatrix().size()+"\n");
					logFieldTinv.append("=====================================================================\n");
					overlord.markNetChange();
				} else {
					logFieldTinv.append("\n");
					logFieldTinv.append("Loading t-invariants from file has been unsuccessfull.\n");
				}
			}
		});
		loadInvariantsButton.setFocusPainted(false);
		panel.add(loadInvariantsButton);
		
		JButton saveInvariantsButton = new JButton();
		saveInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;Export<br>t-invariants</html>");
		saveInvariantsButton.setBounds(posX+380, posY, 120, 36);
		saveInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/saveInvariants.png"));
		saveInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				boolean status = overlord.io.exportGeneratedInvariants(true);
				if(status) {
					logFieldTinv.append("\n");
					logFieldTinv.append("=====================================================================\n");
					logFieldTinv.append("Saved t-invariants: "+overlord.getWorkspace().getProject().getT_InvMatrix().size()+"\n");
					logFieldTinv.append("=====================================================================\n");
				} else {
					logFieldTinv.append("\n");
					logFieldTinv.append("Saving t-invariants has been unsuccessfull.\n");
				}
			}
		});
		saveInvariantsButton.setFocusPainted(false);
		panel.add(saveInvariantsButton);
		
		//**************************************************************************************************

		JButton showInvariantsButton = new JButton();
		showInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Show<br>t-invariants</html>");
		showInvariantsButton.setBounds(posX+505, posY, 120, 36);
		showInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		showInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		showInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showNotepadInvariants(true);
			}
		});
		showInvariantsButton.setFocusPainted(false);
		panel.add(showInvariantsButton);

		JButton saveInvButton = new JButton();
		saveInvButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Save<br>t-invariants</html>");
		saveInvButton.setBounds(posX+130, posY+40, 120, 36);
		saveInvButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		saveInvButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				saveInvKajaType(true);
			}
		});
		saveInvButton.setFocusPainted(false);
		panel.add(saveInvButton);

		JButton makeFeasibleButton = new JButton();
		makeFeasibleButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Make<br>&nbsp;&nbsp;feasible</html>");
		makeFeasibleButton.setBounds(posX+255, posY+40, 120, 36);
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
		feasModeCheckBox.setBounds(posX+380, posY+36, 140, 20); //505
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
		
		JCheckBox invPurityCheckBox = new JCheckBox("Clean non-inv.");
		invPurityCheckBox.setBounds(posX+380, posY+56, 120, 20);
		invPurityCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "1", true);
					//cleanNonInvariant = true;//TODO:
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "0", true);
					//cleanNonInvariant = false;
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveNonInv").equals("1")) 
			invPurityCheckBox.setSelected(true);
		else
			invPurityCheckBox.setSelected(false);
		panel.add(invPurityCheckBox);
		
		JCheckBox removeSingleInvCheckBox = new JCheckBox("Remove 1-el. inv.");
		removeSingleInvCheckBox.setBounds(posX+510, posY+56, 120, 20);
		removeSingleInvCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveSingleElementInv").equals("1")) 
			removeSingleInvCheckBox.setSelected(true);
		else
			removeSingleInvCheckBox.setSelected(false);
		panel.add(removeSingleInvCheckBox);

		return panel;
	}

	private void saveInvKajaType(boolean t_inv) {
		if(t_inv) {
			ArrayList<ArrayList<Integer>> t_invariants;
			ArrayList<Transition> transitions;
			if ((t_invariants = overlord.getWorkspace().getProject().getT_InvMatrix()) != null) {
				transitions = overlord.getWorkspace().getProject().getTransitions();
				HolmesNotepad notePad = new HolmesNotepad(900, 600);
				notePad.setVisible(true);
				notePad.addTextLineNL("T-invariants - Kaja Style:", "text");
				for (int i = 0; i < t_invariants.size(); i++) {
					ArrayList<Integer> inv = t_invariants.get(i);
					String vector = "x" + i + " - ";
					for (int j = 0 ; j < inv.size();j++) {
						int value = inv.get(j);
						if(value!=0) {
							vector += "t"+ overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transitions.get(j)) + ";";
						}
					}
					notePad.addTextLineNL(vector, "text");
				}
			}
		}
		else
		{
			ArrayList<ArrayList<Integer>> p_invariants;
			ArrayList<Place> places;
			if ((p_invariants = overlord.getWorkspace().getProject().getT_InvMatrix()) != null) {
				places = overlord.getWorkspace().getProject().getPlaces();
				HolmesNotepad notePad = new HolmesNotepad(900, 600);
				notePad.setVisible(true);
				notePad.addTextLineNL("P-invariants - Kaja Style:", "text");
				for (int i = 0; i < p_invariants.size(); i++) {
					ArrayList<Integer> inv = p_invariants.get(i);
					String vector = "x" + i + " - ";
					for (int j = 0 ; j < inv.size();j++) {
						int value = inv.get(j);
						if(value!=0) {
							vector += "p"+overlord.getWorkspace().getProject().getPlaces().lastIndexOf(places.get(j)) + ";";
						}
					}
					notePad.addTextLineNL(vector, "text");
				}
			}
		}
	}

	/**
	 * Metoda tworząca środkowy panel okna logów dla t-inwariantów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createLogMainPanelTinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("T-invariants log window"));
		panel.setBounds(x, y, width, height);
		
		logFieldTinv = new JTextArea();
		logFieldTinv.setLineWrap(true);
		logFieldTinv.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logFieldTinv.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logFieldTinv), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-20, height-25);
        panel.add(logFieldPanel);
        
		return panel;
	}
	
	/**
	 * Metoda tworząca prawy panel przycisków t-inwariantów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createRightButtonPanelTinv(int x, int y, int width, int height) {
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
				ArrayList<ArrayList<Integer>> invariants = overlord.getWorkspace().getProject().getT_InvMatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(ego, "No t-invariants to analyze.", 
							"No invariants", JOptionPane.INFORMATION_MESSAGE);
				} else {
					logFieldTinv.append("\n");
					logFieldTinv.append("=====================================================================\n");
					logFieldTinv.append("Checking canonicality for "+invariants.size()+" t-invariants.\n");
					int card = InvariantsTools.checkCanonity(invariants);
					logFieldTinv.append("Non canonical t-invariants: "+card+"\n");
					logFieldTinv.append("=====================================================================\n");
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
				ArrayList<ArrayList<Integer>> invariants = overlord.getWorkspace().getProject().getT_InvMatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(ego, "No t-invariants to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logFieldTinv.append("\n");
					logFieldTinv.append("=====================================================================\n");
					logFieldTinv.append("Checking support minimality for "+invariants.size()+" t-invariants.\n");
					int value = InvariantsTools.checkSupportMinimality(invariants);
					logFieldTinv.append("Non support-minimal t-invariants: "+value+"\n");
					logFieldTinv.append("=====================================================================\n");
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
				ArrayList<ArrayList<Integer>> invariants = overlord.getWorkspace().getProject().getT_InvMatrix();
				if(invariants == null || invariants.size() == 0) {
					JOptionPane.showMessageDialog(ego, "No t-invariants to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logFieldTinv.append("\n");
					logFieldTinv.append("=====================================================================\n");
					logFieldTinv.append("Checking t0invariants correctness for "+invariants.size()+" invariants.\n");
					InvariantsCalculator ic = new InvariantsCalculator(true);
					ArrayList<ArrayList<Integer>> results = InvariantsTools.analyseInvariantDetails(
							ic.getCMatrix(), invariants, true);
					logFieldTinv.append("t-invariants (Cx = 0): "+results.get(0).get(0)+"\n");
					logFieldTinv.append("Sur-invariants (Cx > 0): "+results.get(0).get(1)+"\n");
					logFieldTinv.append("Sub-invariants (Cx < 0): "+results.get(0).get(2)+"\n");
					logFieldTinv.append("Non-invariants (Cx <=> 0): "+results.get(0).get(3)+"\n");
					logFieldTinv.append("=====================================================================\n");
					
					if(detailsTinv)
						showSubSurT_invInfo(results, invariants.size());
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
				testReference(true);
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
				getIncMatrix(true);
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
					detailsTinv = true;
				} else {
					detailsTinv = false;
				}
			}
		});
		detailsCheckBox.setSelected(true);
		panel.add(detailsCheckBox);
		
		//TODO:
		JCheckBox showDiffCheckBox = new JCheckBox("Show difference");
		showDiffCheckBox.setBounds(posX, posY+220, 130, 20);
		showDiffCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					showInvDiff = true;
				} else {
					showInvDiff = false;
				}
			}
		});
		showDiffCheckBox.setSelected(false);
		panel.add(showDiffCheckBox);

		return panel;
	}

	/**
	 * Metoda tworząca górny panel przycisków dla p-invariantów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createUpperButtonPanelPinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 10;
		
		// przycisk generatora inwariantów
		JButton generateButton = new JButton("<html>Generate<br>p-inv.</html>");
		generateButton.setBounds(posX, posY, 110, 60);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
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
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);
		
		// INA GENERATOR
		JButton INAgenerateButton = new JButton();
		INAgenerateButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;INA<br>generator</html>");
		INAgenerateButton.setBounds(posX+130, posY, 120, 36);
		INAgenerateButton.setMargin(new Insets(0, 0, 0, 0));
		INAgenerateButton.setIcon(Tools.getResIcon22("/icons/invWindow/inaGenerator.png"));
		INAgenerateButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unused")
			public void actionPerformed(ActionEvent actionEvent) {
				if(overlord.getINAStatus()) {
					setGeneratorStatus(true);
					overlord.io.generateINAinvariants(false);
					overlord.reset.setP_invariantsStatus(true);
					ArrayList<ArrayList<Integer>> pInv = overlord.getWorkspace().getProject().getP_InvMatrix();
					overlord.markNetChange();
				} else {
					JOptionPane.showMessageDialog(null, "INAwin32.exe status set to non ready. Please read initial warnings\n"
							+ "in the Holmes log windows for more information.", "INAwin32 problem",JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		INAgenerateButton.setFocusPainted(false);
		panel.add(INAgenerateButton);
		
		JButton loadInvariantsButton = new JButton();
		loadInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Load<br>p-invariants</html>");
		loadInvariantsButton.setBounds(posX+255, posY, 120, 36);
		loadInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		loadInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/loadInvariants.png"));
		loadInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				boolean status = overlord.io.loadExternalAnalysis(false);
				if(status) {
					logFieldPinv.append("\n");
					logFieldPinv.append("=====================================================================\n");
					logFieldPinv.append("Loaded p-invariants: "+overlord.getWorkspace().getProject().getP_InvMatrix().size()+"\n");
					logFieldPinv.append("=====================================================================\n");
					overlord.markNetChange();
				} else {
					logFieldPinv.append("\n");
					logFieldPinv.append("P-invariants reading has been unsuccessfull.\n");
				}
			}
		});
		loadInvariantsButton.setFocusPainted(false);
		panel.add(loadInvariantsButton);
		
		JButton saveInvariantsButton = new JButton();
		saveInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;Export<br>p-invariants</html>");
		saveInvariantsButton.setBounds(posX+380, posY, 120, 36);
		saveInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/saveInvariants.png"));
		saveInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				boolean status = overlord.io.exportGeneratedInvariants(false);
				if(status) {
					logFieldPinv.append("\n");
					logFieldPinv.append("=====================================================================\n");
					logFieldPinv.append("Saved p-invariants: "+overlord.getWorkspace().getProject().getP_InvMatrix().size()+"\n");
					logFieldPinv.append("=====================================================================\n");
				} else {
					logFieldPinv.append("\n");
					logFieldPinv.append("Saving p-invariants has been unsuccessfull.\n");
				}
			}
		});
		saveInvariantsButton.setFocusPainted(false);
		panel.add(saveInvariantsButton);
		
		//**************************************************************************************************
		
		JButton showInvariantsButton = new JButton();
		showInvariantsButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Show<br>p-invariants</html>");
		showInvariantsButton.setBounds(posX+505, posY, 120, 36);
		showInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		showInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		showInvariantsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				showNotepadInvariants(false);
			}
		});
		showInvariantsButton.setFocusPainted(false);
		panel.add(showInvariantsButton);


		JCheckBox invPurityCheckBox = new JCheckBox("Clean non-inv.");
		invPurityCheckBox.setBounds(posX+380, posY+56, 120, 20);
		invPurityCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "1", true);
					//cleanNonInvariant = true;//TODO:
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "0", true);
					//cleanNonInvariant = false;
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveNonInv").equals("1"))
			invPurityCheckBox.setSelected(true);
		else
			invPurityCheckBox.setSelected(false);
		panel.add(invPurityCheckBox);

		JCheckBox removeSingleInvCheckBox = new JCheckBox("Remove 1-el. inv.");
		removeSingleInvCheckBox.setBounds(posX+510, posY+56, 120, 20);
		removeSingleInvCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "1", true);
				} else {
					GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "0", true);
				}
			}
		});
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveSingleElementInv").equals("1"))
			removeSingleInvCheckBox.setSelected(true);
		else
			removeSingleInvCheckBox.setSelected(false);
		panel.add(removeSingleInvCheckBox);

		return panel;
	}

	/**
	 * Metoda tworząca środkowy panel okna logów dla p-inwariantów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createLogMainPanelPinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("P-invariants log window"));
		panel.setBounds(x, y, width, height);
		
		logFieldPinv = new JTextArea();
		logFieldPinv.setLineWrap(true);
		logFieldPinv.setEditable(false);
		DefaultCaret caret = (DefaultCaret)logFieldPinv.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logFieldPinv), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-20, height-25);
        panel.add(logFieldPanel);
        
		return panel;
	}
	
	/**
	 * Metoda tworząca prawy panel przycisków dla p-inwariantów.
	 * @param x int - pozycja X
	 * @param y int - pozycja Y
	 * @param width int - szerokość panelu
	 * @param height - wysokość panelu
	 * @return JPanel - utworzony panel
	 */
	private JPanel createRightButtonPanelPinv(int x, int y, int width, int height) {
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
				ArrayList<ArrayList<Integer>> p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix();
				if(p_invariants == null || p_invariants.size() == 0) {
					JOptionPane.showMessageDialog(ego, "No p-invariants to analyze.", 
							"No p-invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logFieldPinv.append("\n");
					logFieldPinv.append("=====================================================================\n");
					logFieldPinv.append("Checking canonicality for "+p_invariants.size()+" p-invariants.\n");
					int card = InvariantsTools.checkCanonity(p_invariants);
					logFieldPinv.append("Non canonical p-invariants: "+card+"\n");
					logFieldPinv.append("=====================================================================\n");
				}
			}
		});
		cardinalityButton.setFocusPainted(false);
		panel.add(cardinalityButton);
		
		JButton minSuppButton = new JButton();
		minSuppButton.setText("<html>Check sup.<br>&nbsp;minimality</html>");
		minSuppButton.setBounds(posX, posY+38, 110, 32);
		minSuppButton.setMargin(new Insets(0, 0, 0, 0));
		minSuppButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_minsup.png"));
		minSuppButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix();
				if(p_invariants == null || p_invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No p-invariants to analyze.", 
							"No p-invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logFieldPinv.append("\n");
					logFieldPinv.append("=====================================================================\n");
					logFieldPinv.append("Checking support minimality for "+p_invariants.size()+" p-invariants.\n");
					int value = InvariantsTools.checkSupportMinimality(p_invariants);
					logFieldPinv.append("Non support-minimal p-invariants: "+value+"\n");
					logFieldPinv.append("=====================================================================\n");
				}
			}
		});
		minSuppButton.setFocusPainted(false);
		panel.add(minSuppButton);
		
		JButton checkMatrixZeroButton = new JButton();
		checkMatrixZeroButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;Check<br>&nbsp;&nbsp;&nbsp;&nbsp;C&nbsp;&middot;&nbsp;x = 0</html>");
		checkMatrixZeroButton.setBounds(posX, posY+76, 110, 32);
		checkMatrixZeroButton.setMargin(new Insets(0, 0, 0, 0));
		checkMatrixZeroButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_invC.png"));
		checkMatrixZeroButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ArrayList<ArrayList<Integer>> p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix();
				if(p_invariants == null || p_invariants.size() == 0) {
					JOptionPane.showMessageDialog(null, "No invariants to analyze.", 
							"No invariants",JOptionPane.INFORMATION_MESSAGE);
				} else {
					logFieldPinv.append("\n");
					logFieldPinv.append("=====================================================================\n");
					logFieldPinv.append("Checking invariants correctness for "+p_invariants.size()+" invariants.\n");
					InvariantsCalculator ic = new InvariantsCalculator(true);
					ArrayList<ArrayList<Integer>> results = InvariantsTools.analyseInvariantDetails(
							ic.getCMatrix(), p_invariants, false);
					logFieldPinv.append("T-invariants (Cx = 0): "+results.get(0).get(0)+"\n");
					logFieldPinv.append("Sur-invariants (Cx > 0): "+results.get(0).get(1)+"\n");
					logFieldPinv.append("Sub-invariants (Cx < 0): "+results.get(0).get(2)+"\n");
					logFieldPinv.append("Non-invariants (Cx <=> 0): "+results.get(0).get(3)+"\n");
					logFieldPinv.append("=====================================================================\n");
					
					if(detailsPinv)
						showSubSurP_invInfo(results, p_invariants.size());
				}
			}
		});
		checkMatrixZeroButton.setFocusPainted(false);
		panel.add(checkMatrixZeroButton);
		
		JButton loadRefButton = new JButton();
		loadRefButton.setText("<html>&nbsp;&nbsp;&nbsp;Ref. set<br>&nbsp;&nbsp;&nbsp;compare</html>");
		loadRefButton.setBounds(posX, posY+114, 110, 32);
		loadRefButton.setMargin(new Insets(0, 0, 0, 0));
		loadRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		loadRefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				testReference(false);
			}
		});
		loadRefButton.setFocusPainted(false);
		panel.add(loadRefButton);
		
		
		JButton testRefButton = new JButton();
		testRefButton.setText("<html>&nbsp;&nbsp;Incidence<br>&nbsp;&nbsp;&nbsp;matrix</html>");
		testRefButton.setBounds(posX, posY+180, 110, 32);
		testRefButton.setMargin(new Insets(0, 0, 0, 0));
		testRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		testRefButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				getIncMatrix(false);
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
					detailsPinv = true;
				} else {
					detailsPinv = false;
				}
			}
		});
		detailsCheckBox.setSelected(true);
		panel.add(detailsCheckBox);

		return panel;
	}
	
	/**
	 * Zwraca macierz incydencji.
	 * @param Tinv boolean - true, wtedy P to kolumny, T to wiersze, czyli na takiej macierzy działa
	 * 			algorytm generowania T-inwariantów
	 */
	protected void getIncMatrix(boolean Tinv) {
		HashMap<Place, Integer> placesMap = new HashMap<Place, Integer>();
		HashMap<Transition, Integer> transitionsMap = new HashMap<Transition, Integer>();
		ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();
		ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
		ArrayList<Arc> arcs = overlord.getWorkspace().getProject().getArcs();
		
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
			
			if(oneArc.getArcType() == TypeOfArc.NORMAL || oneArc.getArcType() == TypeOfArc.XTPN) {
				//idziemy dalej
			} else {
				continue;
			}

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidenceValue = 1 * oneArc.getWeight();
			} else { //miejsca
				tPosition = transitionsMap.get(oneArc.getEndNode());
				pPosition = placesMap.get(oneArc.getStartNode());
				incidenceValue = -1 * oneArc.getWeight();
			}
			int oldValue = globalIncidenceMatrix.get(tPosition).get(pPosition);
			if(oldValue != 0) { //detekcja łuków podwójnych
				ArrayList<Integer> hiddenReadArc = new ArrayList<Integer>();
				hiddenReadArc.add(pPosition);
				hiddenReadArc.add(tPosition);
			}
			
			globalIncidenceMatrix.get(tPosition).set(pPosition, oldValue+incidenceValue);
		}
		
		if(Tinv) {
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
		} else {
			globalIncidenceMatrix = InvariantsTools.transposeMatrix(globalIncidenceMatrix);
			
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);
			notePad.addTextLineNL("", "text");
			for(int p=0; p<globalIncidenceMatrix.size(); p++) {
				ArrayList<Integer> placeRow = globalIncidenceMatrix.get(p);
				String text = "p"+p+" ";
				
				for(int t=0; t<placeRow.size(); t++) {
					text += " "+placeRow.get(t);
				}
				notePad.addTextLineNL(text, "text");
			}
		}
	}

	/**
	 * Metoda wczytująca nowy plik inwariantów (o ile już jakieś są w systemie - celem bycia zbiorem referencyjnym w
	 * stosunku do wczytanego tutaj) oraz porównująca go ze zbiorem referencyjnym.
	 * @param t_inv boolean - true, jeśli chodzi o t-inwarianty, false dla p-inwariantów
	 */
	protected void testReference(boolean t_inv) {
		ArrayList<ArrayList<Integer>> invariants = Check.invExistsWithWarning(t_inv);
		if(invariants == null)
			return;
		
		String lastPath = overlord.getLastPath();
		FileFilter[] filters = new FileFilter[1];
		if(t_inv)
			filters[0] = new ExtensionFileFilter("INA t-nvariants file (.inv)", new String[] { "INV" });
		else
			filters[0] = new ExtensionFileFilter("INA p-invariants file (.inv)", new String[] { "INV" });
		
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Load invariants", "Select invariant file", "");
		if(selectedFile.equals(""))
			return;
		
		File file = new File(selectedFile);
		if(!file.exists()) return;
		
		IOprotocols io = new IOprotocols();
		boolean status = false;
		if(t_inv)
			status = io.readT_invariants(file.getPath());
		else
			status = io.readP_invariants(file.getPath());
		
		if(status == false) {
			return;
		}
		refTest(invariants, io.getInvariantsList(), t_inv);
	}

	/**
	 * Metoda porównująca zbiory inwariantów: referencyjny i osobno wczytany.
	 * @param invRefMatrix ArrayList[ArrayList[Integer]] - zbiór referencyjny inwariantów
	 * @param invLoadedMatrix ArrayList[ArrayList[Integer]] - zbiór do porównania
	 * @param t_inv boolean - true, jeśli chodz o t-inwarianty, false dla p-inwariantów
	 */
	private void refTest(ArrayList<ArrayList<Integer>> invRefMatrix, ArrayList<ArrayList<Integer>> invLoadedMatrix, boolean t_inv) {
		if(invRefMatrix != null) {
			String symbol = "t-";
			if(!t_inv)
				symbol = "p-";
			
			ArrayList<ArrayList<Integer>> res =  InvariantsTools.compareTwoInvariantsSets(invRefMatrix, invLoadedMatrix);
			accessLogField(t_inv).append("\n");
			accessLogField(t_inv).append("=====================================================================\n");
			accessLogField(t_inv).append("Prev. computed set size:   "+invRefMatrix.size()+"\n");
			accessLogField(t_inv).append("Loaded (now) set size:    "+invLoadedMatrix.size()+"\n");
			accessLogField(t_inv).append("Common set size (load & ref): "+res.get(0).size()+"\n");
			accessLogField(t_inv).append("Loaded "+symbol+"invariants not in a computed set:  "+res.get(1).size()+"\n");
			accessLogField(t_inv).append("Computed "+symbol+"invariants not in a loaded set:  "+res.get(2).size()+"\n");
			accessLogField(t_inv).append("Repetitions in common set: "+res.get(3).get(0)+"\n");
			accessLogField(t_inv).append("Total repetitions in loaded:"+res.get(3).get(1)+"\n");
			accessLogField(t_inv).append("\n");
			accessLogField(t_inv).append("Inititating further tests for the loaded set of "+invLoadedMatrix.size()+" "+symbol+"invariants.\n");
			int card = InvariantsTools.checkCanonity(invLoadedMatrix);
			accessLogField(t_inv).append("-> Non canonical "+symbol+"invariants found : "+card+"\n");
			int value = InvariantsTools.checkSupportMinimality(invLoadedMatrix);
			accessLogField(t_inv).append("-> Non support-minimal "+symbol+"invariants found: "+value+"\n");
			InvariantsCalculator ic = new InvariantsCalculator(true);
			ArrayList<ArrayList<Integer>> results = InvariantsTools.analyseInvariantDetails(ic.getCMatrix(), invLoadedMatrix, t_inv);
			accessLogField(t_inv).append(" "+symbol+"invariants (Cx = 0): "+results.get(0).get(0)+"\n");
			accessLogField(t_inv).append("Sur-"+symbol+"invariants (Cx > 0): "+results.get(0).get(1)+"\n");
			accessLogField(t_inv).append("Sun-"+symbol+"invariants (Cx < 0): "+results.get(0).get(2)+"\n");
			accessLogField(t_inv).append("Non-"+symbol+"invariants (Cx <=> 0): "+results.get(0).get(3)+"\n");
			accessLogField(t_inv).append("=====================================================================\n");
			accessLogField(t_inv).append("\n");
			
			if(detailsPinv)
				showSubSurT_invInfo(results, invLoadedMatrix.size());
		}
	}
	
	/**
	 * Metoda obsługuje pokazywanie informacji o sub- i sur- t-inwariantach.
	 * @param results ArrayList[ArrayList[Integer]] - macierz danych o sub- i sur- t-inwariantach
	 * @param invMatrixSize int - rozmiar macierzy t-inwariantów
	 */
	private void showSubSurT_invInfo(ArrayList<ArrayList<Integer>> results, int invMatrixSize) {
		int surPlaceMaxName = 0;
		int subPlaceMaxName = 0;
		ArrayList<Place> places = null;
		int size = 0;
		ArrayList<Integer> surInvVector = results.get(1);
		ArrayList<Integer> subInvVector = results.get(2);
		ArrayList<Integer> noInvVector = results.get(3);
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Vectors analysed: "+invMatrixSize, "text");
		notePad.addTextLineNL("Canonical t-invariants: "+results.get(0).get(0), "text");
		notePad.addTextLineNL("Sur-t-invariants: "+results.get(0).get(1), "text");
		notePad.addTextLineNL("Sub-t-invariants: "+results.get(0).get(2), "text");
		notePad.addTextLineNL("Non t-invariants vectors: "+results.get(0).get(3), "text");
		notePad.addTextLineNL("", "text");
		
		places = overlord.getWorkspace().getProject().getPlaces();
		size = places.size();
		if(results.get(0).get(1) > 0 || results.get(0).get(2) > 0) {
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
			notePad.addTextLineNL("Places for which sur-t-invariants leaves tokens (tokens>0) :", "text");
			notePad.addTextLineNL("(in parenthesis number of sur-t-invariants for each place):", "text");
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
			notePad.addTextLineNL("Places for which sub-t-invariants takes tokens (tokens<0):", "text");
			notePad.addTextLineNL("(in parenthesis number of sub-t-invariants for each place):", "text");
			for(int p=0; p<size; p++) {
				int value = subInvVector.get(p);
				if(value != 0) {
					String line = "p_"+p+Tools.setToSize(places.get(p).getName(), subPlaceMaxName+2, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
		
		if(results.get(0).get(3) > 0) {
			notePad.addTextLineNL("Places for which non-invariants takes or produces tokens:", "text");
			notePad.addTextLineNL("(in parenthesis number of non-t-invariants for each place):", "text");
			for(int p=0; p<size; p++) {
				int value = noInvVector.get(p);
				if(value != 0) {
					String line = "p_"+p+Tools.setToSize(places.get(p).getName(), subPlaceMaxName+2, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
	}
	
	/**
	 * Metoda obsługuje pokazywanie informacji o sub- i sur- p-inwariantach.
	 * @param results ArrayList[ArrayList[Integer]] - macierz danych o sub- i sur- p-inwariantach
	 * @param invMatrixSize int - rozmiar macierzy p-inwariantów
	 */
	private void showSubSurP_invInfo(ArrayList<ArrayList<Integer>> results, int invMatrixSize) {
		int surTransMaxName = 0;
		int subTransMaxName = 0;
		ArrayList<Transition> transitions = null;
		int size = 0;
		ArrayList<Integer> surInvVector = results.get(1);
		ArrayList<Integer> subInvVector = results.get(2);
		ArrayList<Integer> noInvVector = results.get(3);
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Vectors analysed: "+invMatrixSize, "text");
		notePad.addTextLineNL("Canonical p-invariants: "+results.get(0).get(0), "text");
		notePad.addTextLineNL("Sur-p-invariants: "+results.get(0).get(1), "text");
		notePad.addTextLineNL("Sub-p-invariants: "+results.get(0).get(2), "text");
		notePad.addTextLineNL("Non p-invariants vectors: "+results.get(0).get(3), "text");
		notePad.addTextLineNL("", "text");
		
		transitions = overlord.getWorkspace().getProject().getTransitions();
		size = transitions.size();
		if(results.get(0).get(1) > 0 || results.get(0).get(2) > 0) {
			// ustal maksymalną długość nazwy miejsca dla obu zbiorów:
			for(int t=0; t<size; t++) {
				int value = surInvVector.get(t);
				if(value != 0) {
					int nameSize = transitions.get(t).getName().length();
					if(nameSize > surTransMaxName)
						surTransMaxName = nameSize;
				}
				value = subInvVector.get(t);
				if(value != 0) {
					int nameSize = transitions.get(t).getName().length();
					if(nameSize > subTransMaxName)
						subTransMaxName = nameSize;
				}
			}
		}
		
		if(results.get(0).get(1) > 0) {
			notePad.addTextLineNL("Problematics transitions for sur-p-invariants:", "text");
			notePad.addTextLineNL("(in parenthesis number of sur-p-invariants for each transition):", "text");
			for(int t=0; t<size; t++) {
				int value = surInvVector.get(t);
				if(value != 0) {
					String line = "t_"+t+Tools.setToSize(transitions.get(t).getName(), surTransMaxName+3, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
		notePad.addTextLineNL("", "text");
		if(results.get(0).get(2) > 0) {
			notePad.addTextLineNL("Problematics transitions for sub-p-invariants:", "text");
			notePad.addTextLineNL("(in parenthesis number of sub-p-invariants for each transition):", "text");
			for(int t=0; t<size; t++) {
				int value = subInvVector.get(t);
				if(value != 0) {
					String line = "t_"+t+Tools.setToSize(transitions.get(t).getName(), subTransMaxName+2, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
		
		if(results.get(0).get(3) > 0) {
			notePad.addTextLineNL("Problematics transitions for non p-invariant vector:", "text");
			notePad.addTextLineNL("(in parenthesis number of non p-invariants for each transition):", "text");
			for(int t=0; t<size; t++) {
				int value = noInvVector.get(t);
				if(value != 0) {
					String line = "t_"+t+Tools.setToSize(transitions.get(t).getName(), subTransMaxName+2, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
	}

	/**
	 * Metoda odpowiedzialna za obsługę przycisku tworzenia zbioru wykonalnych inwariantów.
	 */
	protected void checkAndMakeFeasible() {
		ArrayList<ArrayList<Integer>> invariants = Check.invExistsWithWarning(true);
		if(invariants == null)
			return;

		overlord.getWorkspace().getProject().restoreMarkingZero();
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
			PetriNet project = overlord.getWorkspace().getProject();
			project.setT_InvMatrix(invariants, false);
			overlord.io.exportGeneratedInvariants(true);
			overlord.getT_invBox().showT_invBoxWindow(project.getT_InvMatrix());
			overlord.markNetChange();
		} else if(n == 1) {
			ArrayList<ArrayList<Integer>> invBackup = overlord.getWorkspace().getProject().getT_InvMatrix();
			try {
				overlord.getWorkspace().getProject().setT_InvMatrix(invariants, false);
				overlord.io.exportGeneratedInvariants(true);
			} catch (Exception e) {}
			finally {
				overlord.getWorkspace().getProject().setT_InvMatrix(invBackup, false);
			}
		} else if(n == 2) {
			PetriNet project = overlord.getWorkspace().getProject();
			project.setT_InvMatrix(invariants, false);
			overlord.getT_invBox().showT_invBoxWindow(project.getT_InvMatrix());
			overlord.markNetChange();
		} //else: nic
	}
	
	/**
	 * Metoda pokazuje w notatniku inwarianty jako wektory rozdzielone średnikiem.
	 * @param t_inv boolean - true, jeśli t-inwarianty, false dla p-inwariantów
	 */
	protected void showNotepadInvariants(boolean t_inv) {
		if(t_inv) {
			ArrayList<ArrayList<Integer>> t_invariants;
			if ((t_invariants = overlord.getWorkspace().getProject().getT_InvMatrix()) != null) {
				HolmesNotepad notePad = new HolmesNotepad(900,600);
				notePad.setVisible(true);
				notePad.addTextLineNL("T-invariants:", "text");
				for(int i=0; i<t_invariants.size(); i++) {
					ArrayList<Integer> inv = t_invariants.get(i);
					String vector = "";
					for(int value : inv) {
						vector += value+";";
					}
					vector = vector.substring(0, vector.length()-1);
					notePad.addTextLineNL(vector, "text");
				}
			}
		} else {
			ArrayList<ArrayList<Integer>> p_invariants;
			if ((p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix()) != null) {
				HolmesNotepad notePad = new HolmesNotepad(900,600);
				notePad.setVisible(true);
				notePad.addTextLineNL("P-invariants:", "text");
				for(int i=0; i<p_invariants.size(); i++) {
					ArrayList<Integer> inv = p_invariants.get(i);
					String vector = "";
					for(int value : inv) {
						vector += value+";";
					}
					vector = vector.substring(0, vector.length()-1);
					notePad.addTextLineNL(vector, "text");
				}
			}
		}
	}
	
	/**
	 * Metoda zwraca obiekt pola tekstowego logów dla odpowiednich inwariantów.
	 * @param t_inv boolean - true, jeśli t-inwarianty, false jeśli p-inwarianty
	 * @return JTextAres - pole tekstowe
	 */
	public JTextArea accessLogField(boolean t_inv) {
		if(t_inv)
			return accessLogFieldTinv();
		else
			return accessLogFieldPinv();
	}
	
	/**
	 * Metoda umożliwia dostęp do kompontentu wyświetlania logów generatora t-inwariantów.
	 * @return JTextArea - obiekt logów
	 */
	public JTextArea accessLogFieldTinv() {
		return logFieldTinv;
	}
	
	/**
	 * Metoda umożliwia dostęp do kompontentu wyświetlania logów generatora p-inwariantów.
	 * @return JTextArea - obiekt logów
	 */
	public JTextArea accessLogFieldPinv() {
		return logFieldPinv;
	}
	
	/**
	 * Metoda resetuje połączenie z wątkiem generatora.
	 */
	public void resetInvariantGenerator() {
		invGenerator = null;
		setGeneratorStatus(false);
	}
	
	/**
	 * Metoda ustawia status generatora inwariantów.
	 * @param status boolean - true, jeśli właśnie działa w tle
	 */
	public void setGeneratorStatus(boolean status) {
		isGeneratorWorking = status;
	}
}
