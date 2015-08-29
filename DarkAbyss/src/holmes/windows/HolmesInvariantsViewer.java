package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.tables.InvariantsViewerTableModel;
import holmes.tables.RXTable;
import holmes.tables.StatesPlacesTableModel;
import holmes.tables.StatesPlacesTableRenderer;
import holmes.utilities.Tools;

/**
 * Klasa okna podglądu struktury t-inwariantu sieci.
 * 
 * @author MR
 *
 */
public class HolmesInvariantsViewer extends JFrame {
	private static final long serialVersionUID = 7735367902562553555L;
	private GUIManager overlord;
	private PetriNet pn;
	
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
	private JLabel labelProblem;
	private JTextArea descriptionProblemTextArea;
	private InvariantsViewerTableModel tableModel;
	private JTable table;
	private JScrollPane tableScrollPane;
	private int currentSelected = 0;
	
	private ArrayList<ArrayList<Integer>> invariantsMatrix;
	private ArrayList<Transition> transitions;
	private ArrayList<Integer> readArcTransLocations;
	private ArrayList<ArrayList<Integer>> incidenceMatrix;
	private ArrayList<ArrayList<Integer>> supportMatrix;

	/**
	 * Konstruktor okna podglądu inwariantów sieci.
	 */
	public HolmesInvariantsViewer() {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
		this.invariantsMatrix = pn.getINVmatrix();
		
		if(invariantsMatrix == null || invariantsMatrix.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"No invariants found, window cannot initiate itself.", 
					"Error: no ivnariants", JOptionPane.ERROR_MESSAGE);
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		
		this.currentSelected = 0;
		initiateVariables();
		initalizeComponents();
    	initiateListeners();
    	showTransitionTable(0);
    	setVisible(true);
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
		
		if(invariantsMatrix == null || invariantsMatrix.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"No invariants found, window cannot initiate itself.", 
					"Error: no ivnariants", JOptionPane.ERROR_MESSAGE);
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		
		initiateVariables();
		initalizeComponents();
    	initiateListeners();
    	showTransitionTable(currentSelected);
    	setVisible(true);
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
		} catch (Exception e) {
			
		}
	}

	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		setLayout(new BorderLayout());
		setSize(new Dimension(800, 650));
		setLocation(50, 50);
		setResizable(true);
		
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
		
		String[] dataP = { "---" };
		
		JLabel label0 = new JLabel("Invariant: ");
		label0.setBounds(posXda, posYda, 70, 20);
		result.add(label0);

		invCombo = new JComboBox<String>();
		invCombo.addItem(" ---------- ");
		for(int i=0; i < invariantsMatrix.size(); i++) {
			invCombo.addItem("Invariant "+(i));
		}
		invCombo.setBounds(posXda+75, posYda, 140, 20);
		invCombo.setSelectedIndex(currentSelected);
		invCombo.setMaximumRowCount(6);
		invCombo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				currentSelected = invCombo.getSelectedIndex();
				if(currentSelected > 0)
					fillData(currentSelected);
				else
					clearSelection();
			}
			
		});
		result.add(invCombo);
		
		JButton nextButton = new JButton("Next");
		nextButton.setBounds(posXda+220, posYda, 80, 20);
		nextButton.setMargin(new Insets(0, 0, 0, 0));
		nextButton.setIcon(Tools.getResIcon16("/icons/stateSim/aaa.png"));
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
		prevButton.setIcon(Tools.getResIcon16("/icons/stateSim/aaa.png"));
		prevButton.setToolTipText("Show previous invariant data.");
		prevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				currentSelected--;
				if(currentSelected == 0)
					currentSelected = invariantsMatrix.size();
				
				invCombo.setSelectedIndex(currentSelected);
			}
		});
		result.add(prevButton);
		
		JCheckBox maximumModeCheckBox = new JCheckBox("MCT table");
		maximumModeCheckBox.setBounds(posXda+390, posYda, 100, 20);
		maximumModeCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					showMCTTransTable(currentSelected);
				} else {
					showTransitionTable(currentSelected);
				}
			}
		});
		result.add(maximumModeCheckBox);
		
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
        
        
        //TODO:
        labelProblem = new JLabel("Sub/sur info:");
        labelProblem.setBounds(570, 20, 120, 20);
        labelProblem.setVisible(false);
		result.add(labelProblem);
		
        descriptionProblemTextArea = new JTextArea();
        descriptionProblemTextArea.setLineWrap(true);
        descriptionProblemTextArea.setEditable(false);
	
        JPanel descProblemPanel = new JPanel();
        descProblemPanel.setLayout(new BorderLayout());
        descProblemPanel.add(new JScrollPane(descriptionProblemTextArea), BorderLayout.CENTER);
        descProblemPanel.setBounds(570, 40, 200, 115);
        descProblemPanel.setVisible(false);
        result.add(descProblemPanel);
		
	    return result;
	}
	
	protected void showTransitionTable(int invNo) {
		if(invNo == 0)
			return;
		
		invNo--;
		
		tableModel = new InvariantsViewerTableModel(false);
		table.setModel(tableModel);
		table.setName("TransitionInvTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		;
          	    		//cellClickAction();
          	    }
          	 }
      	});
		
		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(1).setHeaderValue("Transition");
		table.getColumnModel().getColumn(1).setPreferredWidth(300);
		table.getColumnModel().getColumn(1).setMinWidth(100);

		table.getColumnModel().getColumn(2).setHeaderValue("Firing%");
		table.getColumnModel().getColumn(2).setPreferredWidth(80);
		table.getColumnModel().getColumn(2).setMinWidth(80);
		table.getColumnModel().getColumn(2).setMaxWidth(80);

		table.getColumnModel().getColumn(3).setHeaderValue("stdDev");
		table.getColumnModel().getColumn(3).setPreferredWidth(80);
		table.getColumnModel().getColumn(3).setMinWidth(80);
		table.getColumnModel().getColumn(3).setMaxWidth(80);

		table.setFillsViewportHeight(true);
		table.setRowSelectionAllowed(false);
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.validate();
	}

	protected void showMCTTransTable(int invNo) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Metoda ustawia nowy opis inwariantu.
	 * @param newComment
	 */
	private void changeInvDescr(String newComment) {
		if(currentSelected > 0) {
			pn.accessINVnames().set(currentSelected-1, newComment);
		}
	}

	/**
	 * Metoda wypełnia okno danymi o inwariancie.
	 * @param invNo int - nr inwariantu
	 */
	private void fillData(int invNo) {
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
		
		showTransitionTable(currentSelected);
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
		
		tableModel.clear();
	}
	
	/**
	 * Tworzy dolny panel okna - tabeli.
	 * @return JPanel - panel
	 */
	public JPanel getBottomPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createTitledBorder("Tables"));
		result.setPreferredSize(new Dimension(150, 500));
		tableModel = new InvariantsViewerTableModel(false);
		table = new JTable(tableModel);
		table.setName("StatesTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar	
		table.setRowSelectionAllowed(false);
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
		    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
		    }
		});
    }
}
