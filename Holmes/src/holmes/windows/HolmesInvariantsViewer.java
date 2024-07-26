package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.text.DecimalFormat;
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
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.InvariantsTools;
import holmes.analyse.TimeComputations;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.simulators.StateSimulator;
import holmes.tables.InvariantsTableRenderer;
import holmes.tables.InvariantsViewerTableModel;
import holmes.utilities.Tools;

/**
 * Klasa okna podglądu struktury t-inwariantów sieci.
 */
public class HolmesInvariantsViewer extends JFrame {
	@Serial
	private static final long serialVersionUID = 7735367902562553555L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private PetriNet pn;
	private static final DecimalFormat formatter = new DecimalFormat( "#.##" );
	
	private JComboBox<String> invCombo = null;
	private JLabel labelMinimal;
	private JLabel labelFeasible;
	private JLabel labelSub;
	private JLabel labelSur;
	private JLabel labelCanon;
	private JLabel labelPureInT;
	private JLabel labelInT;
	private JLabel labelOutT;
	private JLabel labelReadArcs;
	private JLabel labelInhibitors;
	private JTextArea descriptionTextArea;
	//time
	private JLabel labelEFT;
	private JLabel labelAVG;
	private JLabel labelLFT;
	private JLabel labelTPNtrans;
	private JLabel labelDPNtrans;
	private JLabel labelTDPNtrans;
	private JLabel labelPNtrans;
	
	private JTable table;
	private InvariantsTableRenderer tableRenderer;
	private JScrollPane tableScrollPane;
	private int currentSelected = 0;
	
	private ArrayList<ArrayList<Integer>> invariantsMatrix;
	private ArrayList<Transition> transitions;
	private ArrayList<Integer> readArcTransLocations;
	private ArrayList<ArrayList<Integer>> incidenceMatrix;
	private ArrayList<ArrayList<Integer>> supportMatrix;
	
	private ArrayList<ArrayList<Double>> transStats;
	private boolean problem = false;
	private boolean showTransTable = true;

	/**
	 * Konstruktor okna podglądu inwariantów sieci.
	 */
	public HolmesInvariantsViewer() {
		this.pn = overlord.getWorkspace().getProject();
		this.invariantsMatrix = pn.getT_InvMatrix();
		
		boolean problem = false;
		if(invariantsMatrix == null || invariantsMatrix.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					lang.getText("HIVwin_entry001"), 
					lang.getText("error"), JOptionPane.ERROR_MESSAGE);
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			problem = true;
		}

		if(!problem) {
			this.currentSelected = 0;
			initiateVariables();
			initalizeComponents();
	    	initiateListeners();
	    	fillData(currentSelected);
	    	setVisible(true);
		}
	}
	
	/**
	 * Konstruktor pozwalający wskazać inwariant który ma być wyświetlony jako pierwszy.
	 * @param invNumber int - indeks inwariantu
	 */
	public HolmesInvariantsViewer(int invNumber) {
		this.pn = overlord.getWorkspace().getProject();
		this.invariantsMatrix = pn.getT_InvMatrix();
		this.currentSelected = invNumber+1;
		
		boolean problem = false;
		if(invariantsMatrix == null || invariantsMatrix.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					lang.getText("HIVwin_entry001"), 
					lang.getText("error"), JOptionPane.ERROR_MESSAGE);
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
			problem = true;
		}
		
		if(!problem) {
			initiateVariables();
			initalizeComponents();
	    	initiateListeners();
	    	//showTransitionTable(currentSelected);
	    	fillData(currentSelected);
	    	setVisible(true);
		}
	}

	/**
	 * Aby zaoszczędzić czas i pamięć, największe struktury danych zostają wypełnione przez obiekt okna
	 * i przekazywane funkcji obliczającej właściwości inwariantu.
	 */
	private void initiateVariables() {
		try {
			//int invariantsNumber = invariantsMatrix.size();
			transitions = pn.getTransitions();
			readArcTransLocations = InvariantsTools.getReadArcTransitionsStatic(); //feasibility
			InvariantsCalculator ic = new InvariantsCalculator(true); //invariant class (sub,sur,non)
			incidenceMatrix = ic.getCMatrix();
			
			supportMatrix = new ArrayList<ArrayList<Integer>>(); // minimality
			for (ArrayList<Integer> matrix : invariantsMatrix) {
				supportMatrix.add(InvariantsTools.getSupport(matrix)); // minimality
			}
			
			//simulator part:
			if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
				JOptionPane.showMessageDialog(null, lang.getText("HIVwin_entry002"), 
						lang.getText("HIVwin_entry002t"), JOptionPane.ERROR_MESSAGE);
				transStats = null;
				problem = true;
			} else {
				StateSimulator ss = new StateSimulator();
				SimulatorGlobals ownSettings = new SimulatorGlobals();
				ownSettings.setNetType(SimulatorGlobals.SimNetType.BASIC, false);
				ownSettings.setMaxMode(false);
				ownSettings.setSingleMode(false);
				ss.initiateSim(false, ownSettings);

				transStats = ss.simulateForInvariantTrans(1000, 20, false);
				problem = ( transStats == null );
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00449exception")+" "+e.getMessage(), "error", true);
		}
	}

	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00449exception")+" "+e.getMessage(), "error", true);
		}
		setLayout(new BorderLayout());
		setSize(new Dimension(800, 650));
		setLocation(50, 50);
		setResizable(true);
		setTitle(lang.getText("HIVwin_entry003title"));
		setLayout(new BorderLayout());
		JPanel main = new JPanel(new BorderLayout());
		main.add(getUpperPanel(), BorderLayout.NORTH);
		main.add(getBottomPanel(), BorderLayout.CENTER);
		add(main, BorderLayout.CENTER);
	}
	
	/**
	 * Tworzy panel przycisków górnych.
	 * @return JPanel - panel
	 */
	@SuppressWarnings("UnusedAssignment")
	public JPanel getUpperPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HIVwin_entry05"))); //General information
		result.setPreferredSize(new Dimension(800, 200));

		int posXda = 10;
		int posYda = 25;

		JLabel label0 = new JLabel(lang.getText("HIVwin_entry06")); //Invariant:
		label0.setBounds(posXda, posYda, 70, 20);
		result.add(label0);

		invCombo = new JComboBox<String>();
		invCombo.addItem(" ---------- ");
		for(int i=0; i < invariantsMatrix.size(); i++) {
			invCombo.addItem(lang.getText("HIVwin_entry07")+" "+(i+1)+" (ID:"+i+")"); //Invariant
		}
		invCombo.setBounds(posXda+75, posYda, 160, 20);
		invCombo.setSelectedIndex(currentSelected);
		invCombo.setMaximumRowCount(6);
		invCombo.addActionListener(actionEvent -> {
			currentSelected = invCombo.getSelectedIndex();
			if(currentSelected > 0) {
				fillData(currentSelected);
			} else {
				clearSelection();
			}
		});
		result.add(invCombo);

		JButton prevButton = new JButton(lang.getText("previous")); //Previous
		prevButton.setBounds(posXda+245, posYda, 80, 20);
		prevButton.setMargin(new Insets(0, 0, 0, 0));
		prevButton.setIcon(Tools.getResIcon16("/icons/invViewer/prevIcon.png"));
		prevButton.setToolTipText(lang.getText("HIVwin_entry08"));
		prevButton.addActionListener(actionEvent -> {
			currentSelected--;
			if(currentSelected <= 0)
				currentSelected = invariantsMatrix.size();

			invCombo.setSelectedIndex(currentSelected);
		});
		result.add(prevButton);
		
		JButton nextButton = new JButton(lang.getText("HIVwin_entry09")); //Next
		nextButton.setBounds(posXda+335, posYda, 80, 20);
		nextButton.setMargin(new Insets(0, 0, 0, 0));
		nextButton.setIcon(Tools.getResIcon16("/icons/invViewer/nextIcon.png"));
		nextButton.setToolTipText(lang.getText("HIVwin_entry09t"));
		nextButton.addActionListener(actionEvent -> {
			currentSelected++;
			if(currentSelected > invariantsMatrix.size())
				currentSelected = 1;

			invCombo.setSelectedIndex(currentSelected);
		});
		result.add(nextButton);

		JLabel label1 = new JLabel(lang.getText("HIVwin_entry10")); //Minimal:
		label1.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label1);
		
		labelMinimal = new JLabel("---");
		labelMinimal.setBounds(posXda+75, posYda, 40, 20);
		result.add(labelMinimal);
		
		JLabel label2 = new JLabel(lang.getText("HIVwin_entry11")); //Feasible:
		label2.setBounds(posXda+110, posYda, 70, 20);
		result.add(label2);
		
		labelFeasible = new JLabel("---");
		labelFeasible.setBounds(posXda+180, posYda, 40, 20);
		result.add(labelFeasible);
		
		JLabel label3 = new JLabel(lang.getText("HIVwin_entry12")); //Sub-inv:
		label3.setBounds(posXda+220, posYda, 70, 20);
		result.add(label3);
		
		labelSub = new JLabel("---");
		labelSub.setBounds(posXda+290, posYda, 40, 20);
		result.add(labelSub);
		
		JLabel label4 = new JLabel(lang.getText("HIVwin_entry13")); //Sur-inv:
		label4.setBounds(posXda+330, posYda, 70, 20);
		result.add(label4);
		
		labelSur = new JLabel("---");
		labelSur.setBounds(posXda+400, posYda, 40, 20);
		result.add(labelSur);
		
		JLabel label5 = new JLabel(lang.getText("HIVwin_entry14")); //Canonical:
		label5.setBounds(posXda+440, posYda, 70, 20);
		result.add(label5);
		
		labelCanon = new JLabel("---");
		labelCanon.setBounds(posXda+510, posYda, 40, 20);
		result.add(labelCanon);
		
		JLabel label6 = new JLabel(lang.getText("HIVwin_entry15")); //pInTrans:
		label6.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label6);
		
		labelPureInT = new JLabel("---");
		labelPureInT.setBounds(posXda+75, posYda, 40, 20);
		result.add(labelPureInT);
		
		JLabel label7 = new JLabel(lang.getText("HIVwin_entry16")); //inTrans:
		label7.setBounds(posXda+110, posYda, 70, 20);
		result.add(label7);
		
		labelInT = new JLabel("---");
		labelInT.setBounds(posXda+180, posYda, 40, 20);
		result.add(labelInT);
		
		JLabel label8 = new JLabel(lang.getText("HIVwin_entry17")); //outTrans:
		label8.setBounds(posXda+220, posYda, 70, 20);
		result.add(label8);
		
		labelOutT = new JLabel("---");
		labelOutT.setBounds(posXda+290, posYda, 40, 20);
		result.add(labelOutT);
		
		JLabel label9 = new JLabel(lang.getText("HIVwin_entry18")); //ReadArcs:
		label9.setBounds(posXda+330, posYda, 70, 20);
		result.add(label9);
		
		labelReadArcs = new JLabel("---");
		labelReadArcs.setBounds(posXda+400, posYda, 40, 20);
		result.add(labelReadArcs);
		
		JLabel label10 = new JLabel(lang.getText("HIVwin_entry19")); //Inhibitors:
		label10.setBounds(posXda+440, posYda, 70, 20);
		result.add(label10);
		
		labelInhibitors = new JLabel("---");
		labelInhibitors.setBounds(posXda+510, posYda, 40, 20);
		result.add(labelInhibitors);
		
		//TIME:
		JLabel label11 = new JLabel(lang.getText("HIVwin_entry20")); //Min. time:
		label11.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label11);
		
		labelEFT = new JLabel("---");
		labelEFT.setBounds(posXda+75, posYda, 40, 20);
		result.add(labelEFT);
		
		JLabel label12 = new JLabel(lang.getText("HIVwin_entry21")); //Avg. time:
		label12.setBounds(posXda+140, posYda, 70, 20);
		result.add(label12);
		
		labelAVG = new JLabel("---");
		labelAVG.setBounds(posXda+210, posYda, 40, 20);
		result.add(labelAVG);
		
		JLabel label13 = new JLabel(lang.getText("HIVwin_entry22")); //Max. time:
		label13.setBounds(posXda+280, posYda, 70, 20);
		result.add(label13);
		
		labelLFT = new JLabel("---");
		labelLFT.setBounds(posXda+350, posYda, 40, 20);
		result.add(labelLFT);
		
		JLabel label16 = new JLabel(lang.getText("HIVwin_entry23")); //TPN trans:
		label16.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label16);
		
		labelTPNtrans = new JLabel("---");
		labelTPNtrans.setBounds(posXda+75, posYda, 40, 20);
		result.add(labelTPNtrans);
		
		JLabel label17 = new JLabel(lang.getText("HIVwin_entry24")); //DPN trans
		label17.setBounds(posXda+110, posYda, 70, 20);
		result.add(label17);
		
		labelDPNtrans = new JLabel("---");
		labelDPNtrans.setBounds(posXda+180, posYda, 40, 20);
		result.add(labelDPNtrans);
		
		JLabel label18 = new JLabel(lang.getText("HIVwin_entry25")); //TDPN trans:
		label18.setBounds(posXda+220, posYda, 70, 20);
		result.add(label18);
		
		labelTDPNtrans = new JLabel("---");
		labelTDPNtrans.setBounds(posXda+290, posYda, 40, 20);
		result.add(labelTDPNtrans);
		
		JLabel label19 = new JLabel(lang.getText("HIVwin_entry26")); //PN trans:
		label19.setBounds(posXda+330, posYda, 70, 20);
		result.add(label19);
		
		labelPNtrans = new JLabel("---");
		labelPNtrans.setBounds(posXda+400, posYda, 40, 20);
		result.add(labelPNtrans);
		
		//END TIME
		JLabel comLabel = new JLabel(lang.getText("HIVwin_entry27")); //Description:
		comLabel.setBounds(posXda, posYda+=20, 70, 20);
		result.add(comLabel);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	String newComment = "";
            	if(field != null)
            		newComment = field.getText();

				changeInvDescr(newComment);
				overlord.markNetChange();
            }
        });
			
        JPanel descPanel = new JPanel();
        descPanel.setLayout(new BorderLayout());
        descPanel.add(new JScrollPane(descriptionTextArea), BorderLayout.CENTER);
        descPanel.setBounds(posXda, posYda+=20, 550, 50);
        result.add(descPanel);

		/*
        labelProblem = new JLabel("Sub/sur info:");
        labelProblem.setBounds(570, 20, 120, 20);
        labelProblem.setVisible(true);
		result.add(labelProblem);
        descriptionProblemTextArea = new JTextArea();
        descriptionProblemTextArea.setLineWrap(true);
        descriptionProblemTextArea.setEditable(true);
        JPanel descProblemPanel = new JPanel();
        descProblemPanel.setLayout(new BorderLayout());
        descProblemPanel.add(new JScrollPane(descriptionProblemTextArea), BorderLayout.CENTER);
        descProblemPanel.setBounds(570, 40, 200, 95);
        descProblemPanel.setVisible(true);
        result.add(descProblemPanel);
        */
        
        JButton calcButton = new JButton(lang.getText("HIVwin_entry28")); //Recalculate statistics
		calcButton.setBounds(570, 20, 200, 35);
		calcButton.setMargin(new Insets(0, 0, 0, 0));
		calcButton.setIcon(Tools.getResIcon16("/icons/invViewer/recalculateInvStats.png"));
		calcButton.setToolTipText(lang.getText("HIVwin_entry28t"));
		calcButton.addActionListener(actionEvent -> {
			if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
				JOptionPane.showMessageDialog(null, lang.getText("HIVwin_entry002"),
						lang.getText("HIVwin_entry002t"), JOptionPane.ERROR_MESSAGE);
				transStats = null;
				problem = true;
			} else {
				StateSimulator ss = new StateSimulator();

				SimulatorGlobals ownSettings = new SimulatorGlobals();
				ownSettings.setNetType(SimulatorGlobals.SimNetType.BASIC, false);
				ownSettings.setMaxMode(false);
				ownSettings.setSingleMode(false);
				ss.initiateSim(false, ownSettings);

				transStats = ss.simulateForInvariantTrans(1000, 20, false);
				problem = ( transStats == null );
			}
		});
		result.add(calcButton);
		
		JButton showNotepadButton = new JButton(lang.getText("HIVwin_entry29")); //Show data in notepad
		showNotepadButton.setBounds(570, 60, 200, 35);
		showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
		showNotepadButton.setIcon(Tools.getResIcon32("/icons/invViewer/showInNotepad.png"));
		showNotepadButton.setToolTipText(lang.getText("HIVwin_entry29t"));
		showNotepadButton.addActionListener(actionEvent -> {
			if(currentSelected > 0)
				showInvariantNotepad(currentSelected);
		});
		result.add(showNotepadButton);
		
		JCheckBox maximumModeCheckBox = new JCheckBox(lang.getText("HIVwin_entry30")); //MCT/transitions table
		maximumModeCheckBox.setBounds(570, 130, 150, 20);
		maximumModeCheckBox.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			showTransTable = !abstractButton.getModel().isSelected();
			if(currentSelected > 0) {
				fillData(currentSelected);
			} else {
				clearSelection();
			}
		});
		result.add(maximumModeCheckBox);
	    return result;
	}
	
	/**
	 * Metoda wypełnia tabelę informacji o inwariance tranzycjami tego inwariantu.
	 * @param invNo int - indeks inwairantu
	 */
	protected void showTransitionTable(int invNo) {
		invNo--;
		if(invNo == -1)
			return;

		InvariantsViewerTableModel modelTransition = new InvariantsViewerTableModel(false);
		table.setModel(modelTransition);

		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(1).setHeaderValue("Transition");
		table.getColumnModel().getColumn(1).setPreferredWidth(800);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		
		table.getColumnModel().getColumn(2).setHeaderValue("Supp.");
		table.getColumnModel().getColumn(2).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setMinWidth(50);
		table.getColumnModel().getColumn(2).setMaxWidth(50);

		table.getColumnModel().getColumn(3).setHeaderValue("Firing%");
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setMinWidth(80);
		table.getColumnModel().getColumn(4).setMaxWidth(80);

		table.getColumnModel().getColumn(4).setHeaderValue("stdDev");
		table.getColumnModel().getColumn(4).setPreferredWidth(80);
		table.getColumnModel().getColumn(4).setMinWidth(80);
		table.getColumnModel().getColumn(4).setMaxWidth(80);
		
		TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(sorter);
		table.setName("InvTransTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		table.setDefaultRenderer(Object.class, tableRenderer);
		//table.setRowSelectionAllowed(false);

		ArrayList<Integer> invariant = invariantsMatrix.get(invNo);
		
		for(int t=0; t<transitions.size(); t++) {
			if(invariant.get(t) != 0) { //wsparcie
				ArrayList<String> row = new ArrayList<String>();
				row.add(""+t);
				row.add(transitions.get(t).getName());
				row.add(""+invariant.get(t));
				
				if(problem) {
					row.add("n/a");
					row.add("n/a");
				} else {
					String value = formatter.format((Number)(transStats.get(0).get(t)*100));
					int index = value.indexOf(",");
					if(index == 1)
						value = "0"+value;
					
					row.add(value+"%");
					value = formatter.format((Number)(transStats.get(1).get(t)*100));
					row.add(value+"%");
				}
				modelTransition.addNew(row);
			}
		}
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.validate();
	}

	/**
	 * Metoda wyświetla tablicę struktury inwariantu z uwzględnieniem zbiorów MCT
	 * @param invNo int - indeks inwariantu
	 */
	protected void showMCTTransTable(int invNo) {
		invNo--;
		if(invNo == -1)
			return;

		InvariantsViewerTableModel modelMCTandTrans = new InvariantsViewerTableModel(true);
		table.setModel(modelMCTandTrans);

		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(0).setMinWidth(60);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(1).setHeaderValue("Element");
		table.getColumnModel().getColumn(1).setPreferredWidth(800);
		table.getColumnModel().getColumn(1).setMinWidth(100);

		TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(table.getModel());
		table.setRowSorter(sorter);
		table.setName("TransitionMCTInvTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		table.setDefaultRenderer(Object.class, tableRenderer);
		//table.setRowSelectionAllowed(false);

		ArrayList<ArrayList<Transition>> mcts = pn.getMCTMatrix();
		ArrayList<Integer> invariant = new ArrayList<Integer>(invariantsMatrix.get(invNo));
		
		for(int mctIndex=0; mctIndex<mcts.size()-1; mctIndex++) {
			ArrayList<Transition> mctSet = mcts.get(mctIndex);
			int firstTindex = transitions.indexOf(mctSet.get(0));
			if(invariant.get(firstTindex) != 0) { //zawiera ten MCT
				ArrayList<String> row = new ArrayList<String>();
				row.add("MCT"+(mctIndex+1));
				row.add(pn.getMCTname(mctIndex));
				modelMCTandTrans.addNew(row);
				
				//czyszczenie inwariantu:
				for(Transition trans : mctSet) {
					int tIndex = transitions.indexOf(trans);
					invariant.set(tIndex, 0);
				}
			}
		}
		
		for(int t=0; t<transitions.size(); t++) {
			if(invariant.get(t) != 0) { //wsparcie
				ArrayList<String> row = new ArrayList<String>();
				row.add("t"+t);
				row.add(transitions.get(t).getName());
				modelMCTandTrans.addNew(row);
			}
		}
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.validate();
	}

	/**
	 * Metoda ustawia nowy opis inwariantu.
	 * @param newComment String - nazwa
	 */
	private void changeInvDescr(String newComment) {
		if(currentSelected > 0) {
			pn.accessT_InvDescriptions().set(currentSelected-1, newComment);
		}
	}

	/**
	 * Metoda wypełnia okno danymi o inwariancie.
	 * @param invNo int - nr inwariantu
	 */
	private void fillData(int invNo) {
		if(invNo == 0)
			return;
		invNo--;
		
		ArrayList<Integer> data = InvariantsTools.singleT_invAnalysis(invariantsMatrix, invNo, transitions, readArcTransLocations
				, incidenceMatrix, supportMatrix);
		
		if(data.get(0) == 0) 
			labelMinimal.setText(lang.getText("yes"));
		else
			labelMinimal.setText(lang.getText("no"));
		
		if(data.get(1) == 1)
			labelFeasible.setText(lang.getText("yes"));
		else
			labelFeasible.setText(lang.getText("no"));
		
		if(data.get(2) == 0) {
			labelSub.setText(lang.getText("no"));
			labelSur.setText(lang.getText("no"));
		} else if(data.get(2) == -1) {
			labelSub.setText(lang.getText("yes"));
			labelSur.setText(lang.getText("no"));
		} else if(data.get(2) == 1){
			labelSub.setText(lang.getText("no"));
			labelSur.setText(lang.getText("yes"));
		} else {
			labelSub.setText("n-Inv");
			labelSur.setText("n-Inv");
		}
		
		if(data.get(3) == 1)
			labelCanon.setText(lang.getText("yes"));
		else
			labelCanon.setText(lang.getText("no"));

		labelInT.setText(data.get(4)+"");
		labelPureInT.setText(data.get(5)+"");
		labelOutT.setText(data.get(6)+"");
		labelReadArcs.setText(data.get(7)+"");
		labelInhibitors.setText(data.get(8)+"");
		
		if(showTransTable)
			showTransitionTable(currentSelected);
		else
			showMCTTransTable(currentSelected);
		
		tableScrollPane.setViewportView(table);
		tableScrollPane.repaint();
		descriptionTextArea.setText(overlord.getWorkspace().getProject().getT_InvDescription(invNo));
		
		
		//TIME:
		ArrayList<Double> timeVector = TimeComputations.getT_InvTimeValues(invariantsMatrix.get(invNo), transitions);

		try {
			assert timeVector != null;
			labelEFT.setText(String.format("%.2f", timeVector.get(0)+timeVector.get(3)));
			labelAVG.setText(String.format("%.2f", timeVector.get(2)+timeVector.get(3)));
			labelLFT.setText(String.format("%.2f", timeVector.get(1)+timeVector.get(3)));

			labelTPNtrans.setText(""+timeVector.get(5).intValue());
			labelDPNtrans.setText(""+timeVector.get(6).intValue());
			labelTDPNtrans.setText(""+timeVector.get(7).intValue());
			labelPNtrans.setText(""+timeVector.get(4).intValue());
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00451exception")+" "+e.getMessage(), "error", true);
		}

	}
	
	/**
	 * Metoda wywołuje okno notatnika z danymi o inwariancie.
	 * @param invNo int - indeks wybranego z listy
	 */
	protected void showInvariantNotepad(int invNo) {
		if(invNo == 0)
			return;
		invNo--;
		
		HolmesNotepad note = new HolmesNotepad(800, 600);
		ArrayList<Integer> invariant = invariantsMatrix.get(invNo);
		
		//MCT:
		ArrayList<Integer> mcts = new ArrayList<Integer>();
		ArrayList<String> singleT = new ArrayList<String>();
		ArrayList<Integer> transMCTvector = overlord.getWorkspace().getProject().getMCTtransIndicesVector();
		int transNumber = 0;
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			if(fireValue == 0)
				continue;
			
			transNumber++;
			int mctNo = transMCTvector.get(t);
			if(mctNo == -1) { 
				singleT.add("T"+t+"_"+transitions.get(t).getName());
			} else {
				if(!mcts.contains(mctNo)) {
					mcts.add(mctNo);
				}
			}
		}
		Collections.sort(mcts);
		String description = overlord.getWorkspace().getProject().accessT_InvDescriptions().get(invNo);
		note.addTextLineNL(lang.getText("HIVwin_entry32")+" "+(invNo+1), "text"); //T-Invariant
		note.addTextLineNL(lang.getText("HIVwin_entry33")+" "+description, "text"); //Description
		note.addTextLineNL(lang.getText("HIVwin_entry34")+" "+transNumber, "text"); //Total number of transitions
		//TIME DATA:
		ArrayList<Double> timeVector = TimeComputations.getT_InvTimeValues(invariant, transitions);
		assert timeVector != null;
		note.addTextLineNL(lang.getText("HIVwin_entry35")+" "+String.format("%.2f", timeVector.get(0)+timeVector.get(3)), "text"); //Minimum firing time
		note.addTextLineNL(lang.getText("HIVwin_entry36")+" "+String.format("%.2f", timeVector.get(1)+timeVector.get(3)), "text"); //Maximum firing time
		note.addTextLineNL(lang.getText("HIVwin_entry37")+" "+String.format("%.2f", timeVector.get(2)+timeVector.get(3)), "text"); //Average firing time

		String str = String.format(lang.getText("HIVwin_entry38")
				, timeVector.get(5).intValue(), timeVector.get(6).intValue(), timeVector.get(7).intValue(), timeVector.get(4).intValue());
		note.addTextLineNL(str, "text");
		note.addTextLineNL(" ", "text");
		
		
		note.addTextLineNL(lang.getText("HIVwin_entry39"), "text"); //Support structure
		for(int mct : mcts) {
			String MCTname = overlord.getWorkspace().getProject().getMCTname(mct);
			note.addTextLineNL("  [MCT: "+(mct+1)+"]: "+MCTname, "text");
		}
		for(String transName : singleT)
			note.addTextLineNL(transName, "text");
		//END OF STRUCTURE BLOCK
		
		note.addTextLineNL("", "text");
		note.addTextLineNL(lang.getText("HIVwin_entry40") + (invNo+1)+":", "text"); //All transitions of INV #
		
		if(transitions.size() != invariant.size()) {
			transitions = overlord.getWorkspace().getProject().getTransitions();
			if(transitions == null || transitions.size() != invariant.size()) {
				overlord.log(lang.getText("HIVwin_entry41"), "error", true);
				return;
			}
		}
		
		StringBuilder vector = new StringBuilder();
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			vector.append(fireValue).append(";");
			if(fireValue == 0)
				continue;
			
			Transition realT = transitions.get(t);
			String t1 = Tools.setToSize("t"+t, 5, false);
			String t2 = Tools.setToSize(lang.getText("HIVwin_entry42")+" "+fireValue, 12, false); //Fired:
			note.addTextLineNL(t1 + t2 + " ; "+realT.getName(), "text");
		}
		vector = new StringBuilder(vector.substring(0, vector.length() - 1));
		note.addTextLineNL("", "text");
		note.addTextLineNL(lang.getText("HIVwin_entry43"), "text"); //Invariant vector
		note.addTextLineNL(vector.toString(), "text");

		note.setCaretFirstLine();
		note.setVisible(true);
	}
	
	/**
	 * Metoda odpalana przy wyborze --- w comboBox
	 */
	private void clearSelection() {
		labelMinimal.setText("---");
		labelFeasible.setText("---");
		labelSub.setText("---");
		labelSur.setText("---");
		labelCanon.setText("---");
		labelPureInT.setText("---");
		labelInT.setText("---");
		labelOutT.setText("---");
		labelReadArcs.setText("---");
		labelInhibitors.setText("---");
		
		labelEFT.setText("---");
		labelAVG.setText("---");
		labelLFT.setText("---");
		labelTPNtrans.setText("---");
		labelDPNtrans.setText("---");
		labelTDPNtrans.setText("---");
		labelPNtrans.setText("---");
		
		table.setModel(new DefaultTableModel());
		//tableModel.clear();
	}
	
	/**
	 * Tworzy dolny panel okna - tabeli.
	 * @return JPanel - panel
	 */
	public JPanel getBottomPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createTitledBorder("Tables"));
		result.setPreferredSize(new Dimension(150, 500));

		DefaultTableModel tableModel = new DefaultTableModel();
		table = new JTable(tableModel);
		tableRenderer = new InvariantsTableRenderer(table);
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
          	    		cellClickAction();
          	    }
          	 }
      	});
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
	    return result;
	}
	
	/**
	 * Metoda odpowiedzialna za obsługę kliknięcia elementu tabeli inwariantów.
	 */
	protected void cellClickAction() {
		try {
			int row = table.getSelectedRow();
			if(table.getName().equals("InvTransTable")) {
				String id = table.getValueAt(row, 0).toString();
				int transId = Integer.parseInt(id);
				new HolmesNodeInfo(transitions.get(transId), this);
			}
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00452exception")+" "+ex.getMessage(), "error", true);
		}
	}

	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	//parentWindow.setEnabled(true);
		    }
		});
    }
}
