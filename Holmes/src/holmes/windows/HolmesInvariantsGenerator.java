package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.Serial;
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
import holmes.darkgui.LanguageManager;
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
 */
public class HolmesInvariantsGenerator extends JFrame {
	@Serial
	private static final long serialVersionUID = 5805567123988000425L;
	private static LanguageManager lang = GUIManager.getLanguageManager();
	private static GUIManager overlord = GUIManager.getDefaultGUIManager();
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
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00440exception")+" "+ex.getMessage(), "error", true);

		}
		this.ego = this;
		setVisible(false);
		this.setTitle("Invariants generator and tools");
		//ego = this;
		
		setLayout(new BorderLayout());
		setSize(new Dimension(682, 570));
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
		panelTinv.setLayout(null); //  ╯°□°）╯︵  ┻━┻
		JPanel buttonPanelT = createUpperButtonPanelTinv(0, 0, 660, 90);
		JPanel logMainPanelT = createLogMainPanelTinv(0, 90, 530, 400);
		JPanel sideButtonPanelT = createRightButtonPanelTinv(530, 90, 130, 400);
		
		panelTinv.add(buttonPanelT);
		panelTinv.add(logMainPanelT);
		panelTinv.add(sideButtonPanelT);
		panelTinv.repaint();
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab(lang.getText("HIGwin_entry002"), Tools.getResIcon22("/icons/invWindow/tInvIcon.png")
				, panelTinv, lang.getText("HIGwin_entry002t")); //t-invariants
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
		
		JPanel panelPinv = new JPanel();
		panelPinv.setLayout(null); //  ╯°□°）╯︵  ┻━┻
		JPanel buttonPanelP = createUpperButtonPanelPinv(0, 0, 660, 90);
		JPanel logMainPanelP = createLogMainPanelPinv(0, 90, 530, 400);
		JPanel sideButtonPanelP = createRightButtonPanelPinv(530, 90, 130, 400);
		
		panelPinv.add(buttonPanelP);
		panelPinv.add(logMainPanelP);
		panelPinv.add(sideButtonPanelP);
		panelPinv.repaint();
		
		tabbedPane.addTab(lang.getText("HIGwin_entry003"), Tools.getResIcon22("/icons/invWindow/pInvIcon.png")
				, panelPinv, lang.getText("HIGwin_entry003t")); //p-invariants
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createUpperButtonPanelTinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 10;
		
		// przycisk generatora inwariantów
		JButton generateButton = new JButton(lang.getText("HIGwin_entry004")); //Generate
		generateButton.setBounds(posX, posY, 110, 60);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(actionEvent -> {
			if(isGeneratorWorking) {
				JOptionPane.showMessageDialog(null, lang.getText("HIGwin_entry005"),
						lang.getText("HIGwin_entry005t"),JOptionPane.WARNING_MESSAGE); //Generator working
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
		INAgenerateButton.setText(lang.getText("HIGwin_entry006")); //INA generator
		INAgenerateButton.setBounds(posX+130, posY, 120, 36);
		INAgenerateButton.setMargin(new Insets(0, 0, 0, 0));
		INAgenerateButton.setIcon(Tools.getResIcon22("/icons/invWindow/inaGenerator.png"));
		INAgenerateButton.addActionListener(actionEvent -> {
			if(overlord.getINAStatus()) {
				setGeneratorStatus(true);
				overlord.io.generateINAinvariants(true);
				overlord.reset.setT_invariantsStatus(true);
				overlord.accessNetTablesWindow().resetT_invData();
				overlord.markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, lang.getText("HIGwin_entry007"), lang.getText("HIGwin_entry007t"),JOptionPane.ERROR_MESSAGE);
			}
		});
		INAgenerateButton.setFocusPainted(false);
		panel.add(INAgenerateButton);
		
		JButton loadInvariantsButton = new JButton();
		loadInvariantsButton.setText(lang.getText("HIGwin_entry008")); //Load t-invariants
		loadInvariantsButton.setBounds(posX+255, posY, 120, 36);
		loadInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		loadInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/loadInvariants.png"));
		loadInvariantsButton.addActionListener(actionEvent -> {
			overlord.accessNetTablesWindow().resetT_invData();
			boolean status = overlord.io.loadExternalAnalysis(true);
			if(status) {
				logFieldTinv.append("\n");
				logFieldTinv.append("=====================================================================\n");
				logFieldTinv.append(lang.getText("LOGentry00441")+" "+overlord.getWorkspace().getProject().getT_InvMatrix().size()+"\n"); //Loaded t-invariants:
				logFieldTinv.append("=====================================================================\n");
				overlord.markNetChange();
			} else {
				logFieldTinv.append("\n");
				logFieldTinv.append(lang.getText("LOGentry00442")); //Loading t-invariants has been unsuccessfull.
			}
		});
		loadInvariantsButton.setFocusPainted(false);
		panel.add(loadInvariantsButton);
		
		JButton saveInvariantsButton = new JButton();
		saveInvariantsButton.setText(lang.getText("HIGwin_entry009")); //Export t-invariants
		saveInvariantsButton.setBounds(posX+380, posY, 120, 36);
		saveInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/saveInvariants.png"));
		saveInvariantsButton.addActionListener(actionEvent -> {
			boolean status = overlord.io.exportGeneratedInvariants(true);
			if(status) {
				logFieldTinv.append("\n");
				logFieldTinv.append("=====================================================================\n");
				logFieldTinv.append(lang.getText("LOGentry00443")+" "+overlord.getWorkspace().getProject().getT_InvMatrix().size()+"\n"); //Saved t-invariants:
				logFieldTinv.append("=====================================================================\n");
			} else {
				logFieldTinv.append("\n");
				logFieldTinv.append(lang.getText("LOGentry00444")); //Saving t-invariants has been unsuccessfull.
			}
		});
		saveInvariantsButton.setFocusPainted(false);
		panel.add(saveInvariantsButton);
		
		//**************************************************************************************************

		JButton showInvariantsButton = new JButton();
		showInvariantsButton.setText(lang.getText("HIGwin_entry010")); //Show t-invariants
		showInvariantsButton.setBounds(posX+505, posY, 120, 36);
		showInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		showInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		showInvariantsButton.addActionListener(actionEvent -> showNotepadInvariants(true));
		showInvariantsButton.setFocusPainted(false);
		panel.add(showInvariantsButton);

		JButton saveInvButton = new JButton();
		saveInvButton.setText(lang.getText("HIGwin_entry011")); //Save t-invariants
		saveInvButton.setBounds(posX+130, posY+40, 120, 36);
		saveInvButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		saveInvButton.addActionListener(actionEvent -> saveInvKajaType(true));
		saveInvButton.setFocusPainted(false);
		panel.add(saveInvButton);

		JButton makeFeasibleButton = new JButton();
		makeFeasibleButton.setText(lang.getText("HIGwin_entry012")); //Make feasible
		makeFeasibleButton.setBounds(posX+255, posY+40, 120, 36);
		makeFeasibleButton.setMargin(new Insets(0, 0, 0, 0));
		makeFeasibleButton.setIcon(Tools.getResIcon22("/icons/invWindow/makeFeasible.png"));
		makeFeasibleButton.addActionListener(actionEvent -> checkAndMakeFeasible());
		makeFeasibleButton.setFocusPainted(false);
		panel.add(makeFeasibleButton);
		
		JCheckBox feasModeCheckBox = new JCheckBox(lang.getText("HIGwin_entry013")); //Feasible advanced mode
		feasModeCheckBox.setBounds(posX+380, posY+36, 140, 20); //505
		feasModeCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				feasibleCalcMode = 1;
			} else {
				feasibleCalcMode = 0;
			}
		});
		feasModeCheckBox.setSelected(true);
		panel.add(feasModeCheckBox);
		
		JCheckBox invPurityCheckBox = new JCheckBox(lang.getText("HIGwin_entry014")); //Clean non-invariant
		invPurityCheckBox.setBounds(posX+380, posY+56, 120, 20);
		invPurityCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "1", true);
				//cleanNonInvariant = true;//TODO:
			} else {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "0", true);
				//cleanNonInvariant = false;
			}
		});
		invPurityCheckBox.setSelected(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveNonInv").equals("1"));
		panel.add(invPurityCheckBox);
		
		JCheckBox removeSingleInvCheckBox = new JCheckBox(lang.getText("HIGwin_entry015")); //Remove 1-element invariant
		removeSingleInvCheckBox.setBounds(posX+510, posY+56, 120, 20);
		removeSingleInvCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "1", true);
			} else {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "0", true);
			}
		});
		removeSingleInvCheckBox.setSelected(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveSingleElementInv").equals("1"));
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
				notePad.addTextLineNL(lang.getText("HIGwin_entry016"), "text"); //T-invariants - Kaja Style
				for (int i = 0; i < t_invariants.size(); i++) {
					ArrayList<Integer> inv = t_invariants.get(i);
					//StringBuilder vector = new StringBuilder("x" + i + " - ");
					StringBuilder vector = new StringBuilder("x" + i + ";");
					for (int j = 0 ; j < inv.size();j++) {
						int value = inv.get(j);
						if(value!=0) {
							vector.append("t").append(overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transitions.get(j))).append(";");
						}
					}
					notePad.addTextLineNL(vector.toString(), "text");
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
				notePad.addTextLineNL(lang.getText("HIGwin_entry017"), "text"); //P-invariants - Kaja Style
				for (int i = 0; i < p_invariants.size(); i++) {
					ArrayList<Integer> inv = p_invariants.get(i);
					//StringBuilder vector = new StringBuilder("x" + i + " - ");
					StringBuilder vector = new StringBuilder("x" + i + ";");
					for (int j = 0 ; j < inv.size();j++) {
						int value = inv.get(j);
						if(value!=0) {
							vector.append("p").append(overlord.getWorkspace().getProject().getPlaces().lastIndexOf(places.get(j))).append(";");
						}
					}
					notePad.addTextLineNL(vector.toString(), "text");
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createLogMainPanelTinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HIGwin_entry018"))); //t-invariants log window
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createRightButtonPanelTinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HIGwin_entry019"))); //Tools
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 18;
		
		JButton cardinalityButton = new JButton();
		cardinalityButton.setText(lang.getText("HIGwin_entry020")); //Check canonity
		cardinalityButton.setBounds(posX, posY, 110, 32);
		cardinalityButton.setMargin(new Insets(0, 0, 0, 0));
		cardinalityButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_canon.png"));
		cardinalityButton.addActionListener(actionEvent -> {
			ArrayList<ArrayList<Integer>> invariants = overlord.getWorkspace().getProject().getT_InvMatrix();
			if(invariants == null || invariants.isEmpty()) {
				JOptionPane.showMessageDialog(ego, lang.getText("HIGwin_entry021"), //No invariants to analyze.
						lang.getText("HIGwin_entry021t"), JOptionPane.INFORMATION_MESSAGE);
			} else {
				logFieldTinv.append("\n");
				logFieldTinv.append("=====================================================================\n");
				String strB = String.format(lang.getText("HIGwin_entry022"), invariants.size());
				logFieldTinv.append(strB); //Checking canonicality for
				int card = InvariantsTools.checkCanonity(invariants);
				logFieldTinv.append(lang.getText("HIGwin_entry023")+" "+card+"\n"); //Non canonical t-invariants:
				logFieldTinv.append("=====================================================================\n");
			}
		});
		cardinalityButton.setFocusPainted(false);
		panel.add(cardinalityButton);
		
		JButton minSuppButton = new JButton();
		minSuppButton.setText(lang.getText("HIGwin_entry024")); //Check support minimality
		minSuppButton.setBounds(posX, posY+38, 110, 32);
		minSuppButton.setMargin(new Insets(0, 0, 0, 0));
		minSuppButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_minsup.png"));
		minSuppButton.addActionListener(actionEvent -> {
			ArrayList<ArrayList<Integer>> invariants = overlord.getWorkspace().getProject().getT_InvMatrix();
			if(invariants == null || invariants.isEmpty()) {
				JOptionPane.showMessageDialog(ego, lang.getText("HIGwin_entry021"), //No invariants to analyze.
						lang.getText("HIGwin_entry021t"),JOptionPane.INFORMATION_MESSAGE);
			} else {
				logFieldTinv.append("\n");
				logFieldTinv.append("=====================================================================\n");
				String strB = String.format(lang.getText("HIGwin_entry025"), invariants.size());
				logFieldTinv.append(strB); //Checking support minimality for
				int value = InvariantsTools.checkSupportMinimality(invariants);
				logFieldTinv.append(lang.getText("HIGwin_entry026")+" "+value+"\n");	//Non support-minimal t-invariants:
				logFieldTinv.append("=====================================================================\n");
			}
		});
		minSuppButton.setFocusPainted(false);
		panel.add(minSuppButton);
		
		JButton checkMatrixZeroButton = new JButton();
		checkMatrixZeroButton.setText(lang.getText("HIGwin_entry027")); //Check Cx = 0
		checkMatrixZeroButton.setBounds(posX, posY+76, 110, 32);
		checkMatrixZeroButton.setMargin(new Insets(0, 0, 0, 0));
		checkMatrixZeroButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_invC.png"));
		checkMatrixZeroButton.addActionListener(actionEvent -> {
			ArrayList<ArrayList<Integer>> invariants = overlord.getWorkspace().getProject().getT_InvMatrix();
			if(invariants == null || invariants.isEmpty()) {
				JOptionPane.showMessageDialog(ego, lang.getText("HIGwin_entry021"), //No invariants to analyze.
						lang.getText("HIGwin_entry021t"),JOptionPane.INFORMATION_MESSAGE); //No invariants
			} else {
				logFieldTinv.append("\n");
				logFieldTinv.append("=====================================================================\n");
				String str = String.format(lang.getText("HIGwin_entry028"), invariants.size());
				logFieldTinv.append(str); //Checking t0invariants correctness for
				InvariantsCalculator ic = new InvariantsCalculator(true);
				ArrayList<ArrayList<Integer>> results = InvariantsTools.analyseInvariantDetails(
						ic.getCMatrix(), invariants, true);
				logFieldTinv.append(lang.getText("HIGwin_entry029")+" "+results.get(0).get(0)+"\n"); //t-invariants (Cx = 0):
				logFieldTinv.append(lang.getText("HIGwin_entry030")+" "+results.get(0).get(1)+"\n"); //Sur-invariants (Cx > 0):
				logFieldTinv.append(lang.getText("HIGwin_entry031")+" "+results.get(0).get(2)+"\n"); //Sub-invariants (Cx < 0):
				logFieldTinv.append(lang.getText("HIGwin_entry032")+" "+results.get(0).get(3)+"\n"); //Non-invariants (Cx <=> 0):
				logFieldTinv.append("=====================================================================\n");

				if(detailsTinv)
					showSubSurT_invInfo(results, invariants.size());
			}
		});
		checkMatrixZeroButton.setFocusPainted(false);
		panel.add(checkMatrixZeroButton);
		
		JButton loadRefButton = new JButton();
		loadRefButton.setText(lang.getText("HIGwin_entry033")); //Reference set compare
		loadRefButton.setBounds(posX, posY+114, 110, 32);
		loadRefButton.setMargin(new Insets(0, 0, 0, 0));
		loadRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		loadRefButton.addActionListener(actionEvent -> testReference(true));
		loadRefButton.setFocusPainted(false);
		panel.add(loadRefButton);
		
		JButton testRefButton = new JButton();
		testRefButton.setText(lang.getText("HIGwin_entry034")); //Incidence matrix
		testRefButton.setBounds(posX, posY+180, 110, 32);
		testRefButton.setMargin(new Insets(0, 0, 0, 0));
		testRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		testRefButton.addActionListener(actionEvent -> getIncMatrix(true));
		testRefButton.setFocusPainted(false);
		panel.add(testRefButton);
		
		JCheckBox detailsCheckBox = new JCheckBox(lang.getText("HIGwin_entry035")); //Details
		detailsCheckBox.setBounds(posX, posY+144, 110, 20);
		detailsCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			detailsTinv = abstractButton.getModel().isSelected();
		});
		detailsCheckBox.setSelected(true);
		panel.add(detailsCheckBox);
		
		//TODO:
		JCheckBox showDiffCheckBox = new JCheckBox(lang.getText("HIGwin_entry036")); //Show difference
		showDiffCheckBox.setBounds(posX, posY+220, 130, 20);
		showDiffCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			showInvDiff = abstractButton.getModel().isSelected();
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createUpperButtonPanelPinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 10;
		
		// przycisk generatora inwariantów
		JButton generateButton = new JButton(lang.getText("HIGwin_entry037")); //Generate p-inv.
		generateButton.setBounds(posX, posY, 110, 60);
		generateButton.setMargin(new Insets(0, 0, 0, 0));
		generateButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		generateButton.addActionListener(actionEvent -> {
			if(isGeneratorWorking) {
				JOptionPane.showMessageDialog(null, lang.getText("HIGwin_entry038"), //Generator working
						lang.getText("HIGwin_entry038t"),JOptionPane.WARNING_MESSAGE);
			} else {
				setGeneratorStatus(true);
				invGenerator = new InvariantsCalculator(false);
				Thread myThread = new Thread(invGenerator);
				myThread.start();
			}
		});
		generateButton.setFocusPainted(false);
		panel.add(generateButton);
		
		// INA GENERATOR
		JButton INAgenerateButton = new JButton();
		INAgenerateButton.setText(lang.getText("HIGwin_entry039")); //INA generator
		INAgenerateButton.setBounds(posX+130, posY, 120, 36);
		INAgenerateButton.setMargin(new Insets(0, 0, 0, 0));
		INAgenerateButton.setIcon(Tools.getResIcon22("/icons/invWindow/inaGenerator.png"));
		INAgenerateButton.addActionListener(actionEvent -> {
			if(overlord.getINAStatus()) {
				setGeneratorStatus(true);
				overlord.io.generateINAinvariants(false);
				overlord.reset.setP_invariantsStatus(true);
				ArrayList<ArrayList<Integer>> pInv = overlord.getWorkspace().getProject().getP_InvMatrix();
				overlord.markNetChange();
			} else {
				JOptionPane.showMessageDialog(null, lang.getText("HIGwin_entry040"), lang.getText("HIGwin_entry040t"),JOptionPane.ERROR_MESSAGE);
			}
		});
		INAgenerateButton.setFocusPainted(false);
		panel.add(INAgenerateButton);
		
		JButton loadInvariantsButton = new JButton();
		loadInvariantsButton.setText(lang.getText("HIGwin_entry041")); //Load p-invariants
		loadInvariantsButton.setBounds(posX+255, posY, 120, 36);
		loadInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		loadInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/loadInvariants.png"));
		loadInvariantsButton.addActionListener(actionEvent -> {
			boolean status = overlord.io.loadExternalAnalysis(false);
			if(status) {
				logFieldPinv.append("\n");
				logFieldPinv.append("=====================================================================\n");
				logFieldPinv.append(lang.getText("LOGentry00445")+" "+overlord.getWorkspace().getProject().getP_InvMatrix().size()+"\n"); //Loaded p-invariants:
				logFieldPinv.append("=====================================================================\n");
				overlord.markNetChange();
			} else {
				logFieldPinv.append("\n");
				logFieldPinv.append(lang.getText("LOGentry00446"));
			}
		});
		loadInvariantsButton.setFocusPainted(false);
		panel.add(loadInvariantsButton);
		
		JButton saveInvariantsButton = new JButton();
		saveInvariantsButton.setText(lang.getText("HIGwin_entry042")); //Export p-invariants
		saveInvariantsButton.setBounds(posX+380, posY, 120, 36);
		saveInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		saveInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/saveInvariants.png"));
		saveInvariantsButton.addActionListener(actionEvent -> {
			boolean status = overlord.io.exportGeneratedInvariants(false);
			if(status) {
				logFieldPinv.append("\n");
				logFieldPinv.append("=====================================================================\n");
				logFieldPinv.append(lang.getText("LOGentry00447")+" "+overlord.getWorkspace().getProject().getP_InvMatrix().size()+"\n"); //Saved p-invariants:
				logFieldPinv.append("=====================================================================\n");
			} else {
				logFieldPinv.append("\n");
				logFieldPinv.append(lang.getText("LOGentry00448"));
			}
		});
		saveInvariantsButton.setFocusPainted(false);
		panel.add(saveInvariantsButton);
		
		//**************************************************************************************************
		
		JButton showInvariantsButton = new JButton();
		showInvariantsButton.setText(lang.getText("HIGwin_entry043")); //Show p-invariants
		showInvariantsButton.setBounds(posX+505, posY, 120, 36);
		showInvariantsButton.setMargin(new Insets(0, 0, 0, 0));
		showInvariantsButton.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
		showInvariantsButton.addActionListener(actionEvent -> showNotepadInvariants(false));
		showInvariantsButton.setFocusPainted(false);
		panel.add(showInvariantsButton);
		
		JCheckBox invPurityCheckBox = new JCheckBox(lang.getText("HIGwin_entry044")); //Clean non-invariant
		invPurityCheckBox.setBounds(posX+380, posY+56, 120, 20);
		invPurityCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "1", true);
				//cleanNonInvariant = true;//TODO:
			} else {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveNonInv", "0", true);
				//cleanNonInvariant = false;
			}
		});
		invPurityCheckBox.setSelected(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveNonInv").equals("1"));
		panel.add(invPurityCheckBox);

		JCheckBox removeSingleInvCheckBox = new JCheckBox(lang.getText("HIGwin_entry045")); //Remove 1-element invariant
		removeSingleInvCheckBox.setBounds(posX+510, posY+56, 120, 20);
		removeSingleInvCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			if (abstractButton.getModel().isSelected()) {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "1", true);
			} else {
				GUIManager.getDefaultGUIManager().getSettingsManager().setValue("analysisRemoveSingleElementInv", "0", true);
			}
		});
		removeSingleInvCheckBox.setSelected(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("analysisRemoveSingleElementInv").equals("1"));
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createLogMainPanelPinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HIGwin_entry046"))); //P-invariants log window
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createRightButtonPanelPinv(int x, int y, int width, int height) {
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBorder(BorderFactory.createTitledBorder("Tools"));
		panel.setBounds(x, y, width, height);
		
		int posX = 10;
		int posY = 18;
		
		JButton cardinalityButton = new JButton();
		cardinalityButton.setText(lang.getText("HIGwin_entry047")); //Check canonity
		cardinalityButton.setBounds(posX, posY, 110, 32);
		cardinalityButton.setMargin(new Insets(0, 0, 0, 0));
		cardinalityButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_canon.png"));
		cardinalityButton.addActionListener(actionEvent -> {
			ArrayList<ArrayList<Integer>> p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix();
			if(p_invariants == null || p_invariants.isEmpty()) {
				JOptionPane.showMessageDialog(ego, lang.getText("HIGwin_entry048"), //No invariants to analyze.
						lang.getText("HIGwin_entry048t"),JOptionPane.INFORMATION_MESSAGE);
			} else {
				logFieldPinv.append("\n");
				logFieldPinv.append("=====================================================================\n");
				String strB = String.format(lang.getText("HIGwin_entry049"), p_invariants.size());
				logFieldPinv.append(strB); //Checking canonicality for
				int card = InvariantsTools.checkCanonity(p_invariants); 
				logFieldPinv.append(lang.getText("HIGwin_entry050")+" "+card+"\n"); //Non canonical p-invariants:
				logFieldPinv.append("=====================================================================\n");
			}
		});
		cardinalityButton.setFocusPainted(false);
		panel.add(cardinalityButton);
		
		JButton minSuppButton = new JButton();
		minSuppButton.setText(lang.getText("HIGwin_entry051")); //Check support minimality
		minSuppButton.setBounds(posX, posY+38, 110, 32);
		minSuppButton.setMargin(new Insets(0, 0, 0, 0));
		minSuppButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_minsup.png"));
		minSuppButton.addActionListener(actionEvent -> {
			ArrayList<ArrayList<Integer>> p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix();
			if(p_invariants == null || p_invariants.isEmpty()) {
				JOptionPane.showMessageDialog(null, lang.getText("HIGwin_entry048"), //No invariants to analyze.
						lang.getText("HIGwin_entry048t"),JOptionPane.INFORMATION_MESSAGE);
			} else {
				logFieldPinv.append("\n");
				logFieldPinv.append("=====================================================================\n");
				String strB = String.format(lang.getText("HIGwin_entry052"), p_invariants.size());
				logFieldPinv.append(strB); //Checking support minimality for
				int value = InvariantsTools.checkSupportMinimality(p_invariants);
				logFieldPinv.append(lang.getText("HIGwin_entry053")+" "+value+"\n"); //Non support-minimal p-invariants:
				logFieldPinv.append("=====================================================================\n");
			}
		});
		minSuppButton.setFocusPainted(false);
		panel.add(minSuppButton);
		
		JButton checkMatrixZeroButton = new JButton();
		checkMatrixZeroButton.setText(lang.getText("HIGwin_entry054")); //Check Cx = 0
		checkMatrixZeroButton.setBounds(posX, posY+76, 110, 32);
		checkMatrixZeroButton.setMargin(new Insets(0, 0, 0, 0));
		checkMatrixZeroButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_invC.png"));
		checkMatrixZeroButton.addActionListener(actionEvent -> {
			ArrayList<ArrayList<Integer>> p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix();
			if(p_invariants == null || p_invariants.isEmpty()) {
				JOptionPane.showMessageDialog(null, lang.getText("HIGwin_entry048"), //No invariants to analyze.
						lang.getText("HIGwin_entry048t"),JOptionPane.INFORMATION_MESSAGE);
			} else {
				logFieldPinv.append("\n");
				logFieldPinv.append("=====================================================================\n");
				String str = String.format(lang.getText("HIGwin_entry055"), p_invariants.size());
				logFieldPinv.append(str); //Checking invariants correctness for
				InvariantsCalculator ic = new InvariantsCalculator(true);
				ArrayList<ArrayList<Integer>> results = InvariantsTools.analyseInvariantDetails(
						ic.getCMatrix(), p_invariants, false);
				logFieldPinv.append(lang.getText("HIGwin_entry056")+" "+results.get(0).get(0)+"\n"); //T-invariants (Cx = 0):
				logFieldPinv.append(lang.getText("HIGwin_entry057")+" "+results.get(0).get(1)+"\n"); //Sur-invariants (Cx > 0):
				logFieldPinv.append(lang.getText("HIGwin_entry058")+" "+results.get(0).get(2)+"\n"); //Sub-invariants (Cx < 0):
				logFieldPinv.append(lang.getText("HIGwin_entry059")+" "+results.get(0).get(3)+"\n"); //	
				logFieldPinv.append("=====================================================================\n");

				if(detailsPinv)
					showSubSurP_invInfo(results, p_invariants.size());
			}
		});
		checkMatrixZeroButton.setFocusPainted(false);
		panel.add(checkMatrixZeroButton);
		
		JButton loadRefButton = new JButton();
		loadRefButton.setText(lang.getText("HIGwin_entry060")); //Reference set compare
		loadRefButton.setBounds(posX, posY+114, 110, 32);
		loadRefButton.setMargin(new Insets(0, 0, 0, 0));
		loadRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		loadRefButton.addActionListener(actionEvent -> testReference(false));
		loadRefButton.setFocusPainted(false);
		panel.add(loadRefButton);
		
		JButton testRefButton = new JButton();
		testRefButton.setText(lang.getText("HIGwin_entry061")); //Incidence matrix
		testRefButton.setBounds(posX, posY+180, 110, 32);
		testRefButton.setMargin(new Insets(0, 0, 0, 0));
		testRefButton.setIcon(Tools.getResIcon22("/icons/invWindow/test_ref.png"));
		testRefButton.addActionListener(actionEvent -> getIncMatrix(false));
		testRefButton.setFocusPainted(false);
		panel.add(testRefButton);
		
		JCheckBox detailsCheckBox = new JCheckBox(lang.getText("HIGwin_entry062")); //Details
		detailsCheckBox.setBounds(posX, posY+144, 110, 20);
		detailsCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			detailsPinv = abstractButton.getModel().isSelected();
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
	@SuppressWarnings("SuspiciousMethodCalls")
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
			int tPosition;
			int pPosition;
			int incidenceValue;
			
			if(oneArc.getArcType() != TypeOfArc.NORMAL) {
				continue;
			}

			if (oneArc.getStartNode().getType() == PetriNetElementType.TRANSITION) {
				tPosition = transitionsMap.get(oneArc.getStartNode());
				pPosition = placesMap.get(oneArc.getEndNode());
				incidenceValue = oneArc.getWeight();
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
				StringBuilder text = new StringBuilder("t" + t + " ");

				for (Integer integer : transRow) {
					text.append(" ").append(integer);
				}
				notePad.addTextLineNL(text.toString(), "text");
			}
		} else {
			globalIncidenceMatrix = InvariantsTools.transposeMatrix(globalIncidenceMatrix);
			
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);
			notePad.addTextLineNL("", "text");
			for(int p=0; p<globalIncidenceMatrix.size(); p++) {
				ArrayList<Integer> placeRow = globalIncidenceMatrix.get(p);
				StringBuilder text = new StringBuilder("p" + p + " ");

				for (Integer integer : placeRow) {
					text.append(" ").append(integer);
				}
				notePad.addTextLineNL(text.toString(), "text");
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
		
		String selectedFile = Tools.selectFileDialog(lastPath, filters, lang.getText("HIGwin_entry063")
				, lang.getText("HIGwin_entry063t"), "");
		if(selectedFile.isEmpty())
			return;
		
		File file = new File(selectedFile);
		if(!file.exists()) return;
		
		IOprotocols io = new IOprotocols();
		boolean status;
		if(t_inv)
			status = io.readT_invariants(file.getPath());
		else
			status = io.readP_invariants(file.getPath());
		
		if(!status) {
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
			accessLogField(t_inv).append(lang.getText("HIGwin_entry064")+"   "+invRefMatrix.size()+"\n");
			accessLogField(t_inv).append(lang.getText("HIGwin_entry065")+"    "+invLoadedMatrix.size()+"\n");
			accessLogField(t_inv).append(lang.getText("HIGwin_entry066")+" "+res.get(0).size()+"\n");
			accessLogField(t_inv).append(lang.getText("HIGwin_entry068")+" "+symbol+lang.getText("HIGwin_entry068_1")+" "+res.get(1).size()+"\n");
			accessLogField(t_inv).append(lang.getText("HIGwin_entry069")+" "+symbol+lang.getText("HIGwin_entry069_1")+" "+res.get(2).size()+"\n");
			accessLogField(t_inv).append(lang.getText("HIGwin_entry070")+" "+res.get(3).get(0)+"\n");
			accessLogField(t_inv).append(lang.getText("HIGwin_entry071")+res.get(3).get(1)+"\n");
			accessLogField(t_inv).append("\n");
			accessLogField(t_inv).append(lang.getText("HIGwin_entry072")+" "+invLoadedMatrix.size()+" "+symbol+lang.getText("HIGwin_entry072_1"));
			int card = InvariantsTools.checkCanonity(invLoadedMatrix);
			accessLogField(t_inv).append(lang.getText("HIGwin_entry073")+" "+symbol+lang.getText("HIGwin_entry073_1")+" "+card+"\n");
			int value = InvariantsTools.checkSupportMinimality(invLoadedMatrix);
			accessLogField(t_inv).append(lang.getText("HIGwin_entry074")+" "+symbol+lang.getText("HIGwin_entry074_1")+" "+value+"\n");
			InvariantsCalculator ic = new InvariantsCalculator(true);
			ArrayList<ArrayList<Integer>> results = InvariantsTools.analyseInvariantDetails(ic.getCMatrix(), invLoadedMatrix, t_inv);
			accessLogField(t_inv).append(" "+symbol+lang.getText("HIGwin_entry075")+" "+results.get(0).get(0)+"\n");
			accessLogField(t_inv).append("Sur-"+symbol+lang.getText("HIGwin_entry076")+" "+results.get(0).get(1)+"\n");
			accessLogField(t_inv).append("Sub-"+symbol+lang.getText("HIGwin_entry077")+" "+results.get(0).get(2)+"\n");
			accessLogField(t_inv).append("Non-"+symbol+lang.getText("HIGwin_entry078")+" "+results.get(0).get(3)+"\n");
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
		ArrayList<Place> places;
		int size;
		ArrayList<Integer> surInvVector = results.get(1);
		ArrayList<Integer> subInvVector = results.get(2);
		ArrayList<Integer> noInvVector = results.get(3);
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry079")+" "+invMatrixSize, "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry080")+" "+results.get(0).get(0), "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry081")+" "+results.get(0).get(1), "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry082")+" "+results.get(0).get(2), "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry083")+" "+results.get(0).get(3), "text");
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
			notePad.addTextLineNL(lang.getText("HIGwin_entry084"), "text");
			notePad.addTextLineNL(lang.getText("HIGwin_entry085"), "text");
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
			notePad.addTextLineNL(lang.getText("HIGwin_entry086"), "text");
			notePad.addTextLineNL(lang.getText("HIGwin_entry087"), "text");
			for(int p=0; p<size; p++) {
				int value = subInvVector.get(p);
				if(value != 0) {
					String line = "p_"+p+Tools.setToSize(places.get(p).getName(), subPlaceMaxName+2, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
		
		if(results.get(0).get(3) > 0) {
			notePad.addTextLineNL(lang.getText("HIGwin_entry088"), "text");
			notePad.addTextLineNL(lang.getText("HIGwin_entry089"), "text");
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
		ArrayList<Transition> transitions;
		int size;
		ArrayList<Integer> surInvVector = results.get(1);
		ArrayList<Integer> subInvVector = results.get(2);
		ArrayList<Integer> noInvVector = results.get(3);
		
		HolmesNotepad notePad = new HolmesNotepad(900,600);
		notePad.setVisible(true);
		
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry090")+" "+invMatrixSize, "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry091")+" "+results.get(0).get(0), "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry092")+" "+results.get(0).get(1), "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry093")+" "+results.get(0).get(2), "text");
		notePad.addTextLineNL(lang.getText("HIGwin_entry094")+" "+results.get(0).get(3), "text");
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
			notePad.addTextLineNL(lang.getText("HIGwin_entry095"), "text");
			notePad.addTextLineNL(lang.getText("HIGwin_entry096"), "text");
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
			notePad.addTextLineNL(lang.getText("HIGwin_entry097"), "text");
			notePad.addTextLineNL(lang.getText("HIGwin_entry098"), "text");
			for(int t=0; t<size; t++) {
				int value = subInvVector.get(t);
				if(value != 0) {
					String line = "t_"+t+Tools.setToSize(transitions.get(t).getName(), subTransMaxName+2, false) +": "+value;
					notePad.addTextLineNL(line, "text");
				}
			}
		}
		
		if(results.get(0).get(3) > 0) {
			notePad.addTextLineNL(lang.getText("HIGwin_entry099"), "text");
			notePad.addTextLineNL(lang.getText("HIGwin_entry100"), "text");
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

		//overlord.getWorkspace().getProject().restoreMarkingZero(); //TODO: zapytać, czy przywrócić?

		InvariantsCalculatorFeasible invF = new InvariantsCalculatorFeasible(invariants, true);
		invariants = invF.getMinFeasible(feasibleCalcMode);
		
		Object[] options = {lang.getText("saveAndReplace"), lang.getText("saveOnly"), lang.getText("replaceOnly"), lang.getText("cancel")};
		int n = JOptionPane.showOptionDialog(null,
						lang.getText("HIGwin_entry101"),
						lang.getText("question"), JOptionPane.YES_NO_OPTION,
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
			} catch (Exception ex) {
				GUIManager.getDefaultGUIManager().log(lang.getText("HIGwin_entry102exception")+" "+ex.getMessage(), "error", true);
			}
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
				notePad.addTextLineNL(lang.getText("HIGwin_entry103"), "text");
				for (ArrayList<Integer> inv : t_invariants) {
					StringBuilder vector = new StringBuilder();
					for (int value : inv) {
						vector.append(value).append(";");
					}
					vector = new StringBuilder(vector.substring(0, vector.length() - 1));
					notePad.addTextLineNL(vector.toString(), "text");
				}
			}
		} else {
			ArrayList<ArrayList<Integer>> p_invariants;
			if ((p_invariants = overlord.getWorkspace().getProject().getP_InvMatrix()) != null) {
				HolmesNotepad notePad = new HolmesNotepad(900,600);
				notePad.setVisible(true);
				notePad.addTextLineNL(lang.getText("HIGwin_entry104"), "text");
				for (ArrayList<Integer> inv : p_invariants) {
					StringBuilder vector = new StringBuilder();
					for (int value : inv) {
						vector.append(value).append(";");
					}
					vector = new StringBuilder(vector.substring(0, vector.length() - 1));
					notePad.addTextLineNL(vector.toString(), "text");
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
