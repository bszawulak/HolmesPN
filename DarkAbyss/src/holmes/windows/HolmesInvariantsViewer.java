package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowEvent;
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
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.NetSimulator.NetType;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.petrinet.simulators.StateSimulator;
import holmes.tables.InvariantsTableRenderer;
import holmes.tables.InvariantsViewerTableModel;
import holmes.utilities.Tools;

/**
 * Klasa okna podglądu struktury t-inwariantów sieci.
 * 
 * @author MR
 *
 */
public class HolmesInvariantsViewer extends JFrame {
	private static final long serialVersionUID = 7735367902562553555L;
	private GUIManager overlord;
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
	
	private JTable table;
	private DefaultTableModel tableModel;
	private InvariantsViewerTableModel modelTransition;
	private InvariantsViewerTableModel modelMCTandTrans;
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
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.invariantsMatrix = pn.getINVmatrix();
		
		boolean problem = false;
		if(invariantsMatrix == null || invariantsMatrix.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"No invariants found, window cannot be initialized.", 
					"Error: no invariants", JOptionPane.ERROR_MESSAGE);
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
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.invariantsMatrix = pn.getINVmatrix();
		this.currentSelected = invNumber+1;
		
		boolean problem = false;
		if(invariantsMatrix == null || invariantsMatrix.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"No invariants found, window cannot initiate itself.", 
					"Error: no ivnariants", JOptionPane.ERROR_MESSAGE);
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
			int invariantsNumber = invariantsMatrix.size();
			transitions = pn.getTransitions();
			readArcTransLocations = InvariantsTools.getReadArcTransitionsStatic(); //feasibility
			InvariantsCalculator ic = new InvariantsCalculator(true); //invariant class (sub,sur,non)
			incidenceMatrix = ic.getCMatrix();
			
			supportMatrix = new ArrayList<ArrayList<Integer>>(); // minimality
			for(int i=0; i<invariantsNumber; i++) {
				supportMatrix.add(InvariantsTools.getSupport(invariantsMatrix.get(i))); // minimality
			}
			
			//simulator part:
			if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
				JOptionPane.showMessageDialog(null, "Net simulator working. Unable to retrieve transitions statistics..", 
						"Simulator working", JOptionPane.ERROR_MESSAGE);
				transStats = null;
				problem = true;
			} else {
				StateSimulator ss = new StateSimulator();
				ss.initiateSim(NetType.BASIC, false, false);
				transStats = ss.simulateForInvariantTrans(1000, 20);
				problem = false;
				if(transStats==null)
					problem = true;
			}
		} catch (Exception e) {
			overlord.log("Problems encountered while initializing variables for invariant viewer window.", "error", true);
		}
	}

	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) { }
		setLayout(new BorderLayout());
		setSize(new Dimension(800, 650));
		setLocation(50, 50);
		setResizable(true);
		setTitle("Holmes Invariants Viewer");
		setLayout(new BorderLayout());
		JPanel main = new JPanel(new BorderLayout());
		main.add(getUpperPanel(), BorderLayout.NORTH);
		main.add(getBottomPanel(), BorderLayout.CENTER);
		add(main, BorderLayout.CENTER);
	}
	
	/**
	 * Tworzy panel przycisków bocznych.
	 * @return JPanel - panel
	 */
	public JPanel getUpperPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("General information"));
		result.setPreferredSize(new Dimension(800, 160));

		int posXda = 10;
		int posYda = 25;

		JLabel label0 = new JLabel("Invariant: ");
		label0.setBounds(posXda, posYda, 70, 20);
		result.add(label0);

		invCombo = new JComboBox<String>();
		invCombo.addItem(" ---------- ");
		for(int i=0; i < invariantsMatrix.size(); i++) {
			invCombo.addItem("Invariant "+(i+1));
		}
		invCombo.setBounds(posXda+75, posYda, 140, 20);
		invCombo.setSelectedIndex(currentSelected);
		invCombo.setMaximumRowCount(6);
		invCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				currentSelected = invCombo.getSelectedIndex();
				if(currentSelected > 0) {
					fillData(currentSelected);
				} else {
					clearSelection();
				}
			}
			
		});
		result.add(invCombo);
		
		JButton nextButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Next&nbsp;</html>");
		nextButton.setBounds(posXda+220, posYda, 80, 20);
		nextButton.setMargin(new Insets(0, 0, 0, 0));
		nextButton.setIcon(Tools.getResIcon16("/icons/invViewer/nextIcon.png"));
		nextButton.setToolTipText("Show next invariant data.");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				currentSelected++;
				if(currentSelected > invariantsMatrix.size())
					currentSelected = 1;
				
				invCombo.setSelectedIndex(currentSelected);
			}
		});
		result.add(nextButton);
		
		JButton prevButton = new JButton("Previous");
		prevButton.setBounds(posXda+310, posYda, 80, 20);
		prevButton.setMargin(new Insets(0, 0, 0, 0));
		prevButton.setIcon(Tools.getResIcon16("/icons/invViewer/prevIcon.png"));
		prevButton.setToolTipText("Show previous invariant data.");
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				currentSelected--;
				if(currentSelected <= 0)
					currentSelected = invariantsMatrix.size();
				
				invCombo.setSelectedIndex(currentSelected);
			}
		});
		result.add(prevButton);

		JLabel label1 = new JLabel("Minimal:");
		label1.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label1);
		
		labelMinimal = new JLabel("---");
		labelMinimal.setBounds(posXda+75, posYda, 40, 20);
		result.add(labelMinimal);
		
		JLabel label2 = new JLabel("Feasible:");
		label2.setBounds(posXda+110, posYda, 70, 20);
		result.add(label2);
		
		labelFeasible = new JLabel("---");
		labelFeasible.setBounds(posXda+180, posYda, 40, 20);
		result.add(labelFeasible);
		
		JLabel label3 = new JLabel("Sub-inv:");
		label3.setBounds(posXda+220, posYda, 70, 20);
		result.add(label3);
		
		labelSub = new JLabel("---");
		labelSub.setBounds(posXda+290, posYda, 40, 20);
		result.add(labelSub);
		
		JLabel label4 = new JLabel("Sur-inv:");
		label4.setBounds(posXda+330, posYda, 70, 20);
		result.add(label4);
		
		labelSur = new JLabel("---");
		labelSur.setBounds(posXda+400, posYda, 40, 20);
		result.add(labelSur);
		
		JLabel label5 = new JLabel("Canonical:");
		label5.setBounds(posXda+440, posYda, 70, 20);
		result.add(label5);
		
		labelCanon = new JLabel("---");
		labelCanon.setBounds(posXda+510, posYda, 40, 20);
		result.add(labelCanon);
		
		JLabel label6 = new JLabel("pInTrans:");
		label6.setBounds(posXda, posYda+=20, 70, 20);
		result.add(label6);
		
		labelPureInT = new JLabel("---");
		labelPureInT.setBounds(posXda+75, posYda, 40, 20);
		result.add(labelPureInT);
		
		JLabel label7 = new JLabel("inTrans:");
		label7.setBounds(posXda+110, posYda, 70, 20);
		result.add(label7);
		
		labelInT = new JLabel("---");
		labelInT.setBounds(posXda+180, posYda, 40, 20);
		result.add(labelInT);
		
		JLabel label8 = new JLabel("outTrans:");
		label8.setBounds(posXda+220, posYda, 70, 20);
		result.add(label8);
		
		labelOutT = new JLabel("---");
		labelOutT.setBounds(posXda+290, posYda, 40, 20);
		result.add(labelOutT);
		
		JLabel label9 = new JLabel("ReadArcs:");
		label9.setBounds(posXda+330, posYda, 70, 20);
		result.add(label9);
		
		labelReadArcs = new JLabel("---");
		labelReadArcs.setBounds(posXda+400, posYda, 40, 20);
		result.add(labelReadArcs);
		
		JLabel label10 = new JLabel("Inhibitors:");
		label10.setBounds(posXda+440, posYda, 70, 20);
		result.add(label10);
		
		labelInhibitors = new JLabel("---");
		labelInhibitors.setBounds(posXda+510, posYda, 40, 20);
		result.add(labelInhibitors);
		
		JLabel comLabel = new JLabel("Description:");
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
        
        JButton calcButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Recalculate statistics</html>");
		calcButton.setBounds(570, 20, 200, 35);
		calcButton.setMargin(new Insets(0, 0, 0, 0));
		calcButton.setIcon(Tools.getResIcon16("/icons/invViewer/recalculateInvStats.png"));
		calcButton.setToolTipText("Show previous invariant data.");
		calcButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
					JOptionPane.showMessageDialog(null, "Net simulator working. Unable to retrieve transitions statistics..", 
							"Simulator working", JOptionPane.ERROR_MESSAGE);
					transStats = null;
					problem = true;
				} else {
					StateSimulator ss = new StateSimulator();
					ss.initiateSim(NetType.BASIC, false, false);
					transStats = ss.simulateForInvariantTrans(1000, 20);
					problem = false;
					if(transStats==null)
						problem = true;
				}
			}
		});
		result.add(calcButton);
		
		JButton showNotepadButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Show data in notepad</html>");
		showNotepadButton.setBounds(570, 60, 200, 35);
		showNotepadButton.setMargin(new Insets(0, 0, 0, 0));
		showNotepadButton.setIcon(Tools.getResIcon32("/icons/invViewer/showInNotepad.png"));
		showNotepadButton.setToolTipText("Show invariants data in internal notepad.");
		showNotepadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(currentSelected > 0)
					showInvariantNotepad(currentSelected);
			}
		});
		result.add(showNotepadButton);
		
		JCheckBox maximumModeCheckBox = new JCheckBox("MCT/transitions table");
		maximumModeCheckBox.setBounds(570, 130, 150, 20);
		maximumModeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected())
					showTransTable = false;
				else
					showTransTable = true;
				
				if(currentSelected > 0) {
					fillData(currentSelected);
				} else {
					clearSelection();
				}
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
		
		modelTransition = new InvariantsViewerTableModel(false);
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
				((InvariantsViewerTableModel)modelTransition).addNew(row);
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

		modelMCTandTrans = new InvariantsViewerTableModel(true);
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
				((InvariantsViewerTableModel)modelMCTandTrans).addNew(row);
				
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
				((InvariantsViewerTableModel)modelMCTandTrans).addNew(row);
			}
		}
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.validate();

		//tableScrollPane.setViewportView(table);
		//tableScrollPane.repaint();
	}

	/**
	 * Metoda ustawia nowy opis inwariantu.
	 * @param newComment String - nazwa
	 */
	private void changeInvDescr(String newComment) {
		if(currentSelected > 0) {
			pn.accessINVdescriptions().set(currentSelected-1, newComment);
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
		
		ArrayList<Integer> data = InvariantsTools.singleInvAnalysis(invariantsMatrix, invNo, transitions, readArcTransLocations
				, incidenceMatrix, supportMatrix);
		
		if(data.get(0) == 0) 
			labelMinimal.setText("yes");
		else
			labelMinimal.setText("no");
		
		if(data.get(1) == 1)
			labelFeasible.setText("yes");
		else
			labelFeasible.setText("no");
		
		if(data.get(2) == 0) {
			labelSub.setText("no");
			labelSur.setText("no");
		} else if(data.get(2) == -1) {
			labelSub.setText("yes");
			labelSur.setText("no");
		} else if(data.get(2) == 1){
			labelSub.setText("no");
			labelSur.setText("yes");
		} else {
			labelSub.setText("n-Inv");
			labelSur.setText("n-Inv");
		}
		
		if(data.get(3) == 1)
			labelCanon.setText("yes");
		else
			labelCanon.setText("no");

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

		descriptionTextArea.setText(overlord.getWorkspace().getProject().getINVdescription(invNo));
	}
	
	/**
	 * Metoda wywołuje okno notatnika z danymi o inwariancie.
	 * @param selectedInvIndex2 int - indeks wybranego z listy
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
		String description = overlord.getWorkspace().getProject().accessINVdescriptions().get(invNo);
		note.addTextLineNL("Invariant "+(invNo+1), "text");
		note.addTextLineNL("Descrption: "+description, "text");
		note.addTextLineNL("Total number of transitions: "+transNumber, "text");
		note.addTextLineNL("Support structure:", "text");
		for(int mct : mcts) {
			String MCTname = overlord.getWorkspace().getProject().getMCTname(mct);
			note.addTextLineNL("  [MCT: "+(mct+1)+"]: "+MCTname, "text");
		}
		for(String transName : singleT)
			note.addTextLineNL(transName, "text");
		//END OF STRUCTURE BLOCK
		
		note.addTextLineNL("", "text");
		note.addTextLineNL("All transitions of INV #" + (invNo+1)+":", "text");
		
		if(transitions.size() != invariant.size()) {
			transitions = overlord.getWorkspace().getProject().getTransitions();
			if(transitions == null || transitions.size() != invariant.size()) {
				overlord.log("Critical error in invariants subwindow. "
						+ "Invariants support size refers to non-existing transitions.", "error", true);
				return;
			}
		}
		
		String vector = "";
		for(int t=0; t<invariant.size(); t++) {
			int fireValue = invariant.get(t);
			vector += fireValue+";";
			if(fireValue == 0)
				continue;
			
			Transition realT = transitions.get(t);
			String t1 = Tools.setToSize("t"+t, 5, false);
			String t2 = Tools.setToSize("Fired: "+fireValue, 12, false);
			note.addTextLineNL(t1 + t2 + " ; "+realT.getName(), "text");
		}
		vector = vector.substring(0, vector.length()-1);
		note.addTextLineNL("", "text");
		note.addTextLineNL("Invariant vector:", "text");
		note.addTextLineNL(vector, "text");

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
		
		tableModel = new DefaultTableModel();
		table = new JTable(tableModel);
		tableRenderer = new InvariantsTableRenderer(table);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
	    return result;
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
