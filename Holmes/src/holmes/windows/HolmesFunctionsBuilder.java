package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.nfunk.jep.JEP;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.functions.FunctionContainer;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.tables.FunctionalTransAuxTableModel;
import holmes.tables.FunctionalTransTableModel;
import holmes.tables.FunctionalTransTableRenderer;
import holmes.utilities.Tools;

import static java.lang.Double.valueOf;

/**
 * Okno zarządzania funkcjami wskazanej tranzycji.
 * 
 * @author MR
 */
public class HolmesFunctionsBuilder extends JFrame {
	@Serial
	private static final long serialVersionUID = 1235426932930026597L;
	private static final DecimalFormat formatter = new DecimalFormat( "#.###" );
	private Transition transition;
	private ArrayList<Place> places;
	private ArrayList<Arc> arcs;
	private GUIManager overlord;
	private PetriNet pn;
	private boolean mainSimulatorActive;
	private JTable tableFunc;
	private FunctionalTransTableModel tableFuncModel;

	private JTextField idField;
	private JTextField functionField;
	private JCheckBox enabledCheckBox;
	private JButton validateButton;
	private JTextField currentResult;
	private JTextArea commentField;
	
	/**
	 * Konstruktor okna zarządzania funkcjami tranzycji.
	 * @param trans Transition - wskazana tranzycja
	 */
	public HolmesFunctionsBuilder(Transition trans) {
		overlord = GUIManager.getDefaultGUIManager();
		pn = overlord.getWorkspace().getProject();
		this.transition = trans;
		this.places = pn.getPlaces();
		this.arcs = pn.getArcs();
		
		setTitle("Transition: "+trans.getName());
		try {
			setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ignored) {}
		
		if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED)
			mainSimulatorActive = true;

		addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	overlord.getFrame().setEnabled(true);
		    }
		});
		
		if(places.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"At least one places required.", 
					"Error: to few places", JOptionPane.ERROR_MESSAGE);
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		} else if(mainSimulatorActive) {
			JOptionPane.showMessageDialog(null,
					"Function editor unavailable when simulator is working.",
					"Error: simulation in progress", JOptionPane.ERROR_MESSAGE);
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		} else {
			overlord.getFrame().setEnabled(false);
			setResizable(false);
			initializeComponents();
			setVisible(true);
		}
	}
	
	/**
	 * Metoda tworząca główne sekcje okna.
	 */
	private void initializeComponents() {
		this.setLocation(20, 20);
		setSize(new Dimension(900, 650));
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel tablePanel = createTablePanel();
		mainPanel.add(tablePanel, BorderLayout.NORTH);
		mainPanel.add(createAuxPanel(), BorderLayout.CENTER);
		add(mainPanel);
	}
	
	/**
	 * Metoda tworzenia panelu z sekcjami: edytora funkcji oraz tablicy miejsc.
	 * @return JPanel - panel
	 */
	private JPanel createAuxPanel() {
		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel.setPreferredSize(new Dimension(900, 400));
		//resultPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		
		resultPanel.add(createBuilderPanel(),BorderLayout.NORTH);
		resultPanel.add(createPlacesTablePanel(), BorderLayout.CENTER);
		
		return resultPanel;
	}

	/**
	 * Metoda tworząca panel edycji funkcji.
	 * @return JPanel - panel
	 */
	private JPanel createBuilderPanel() {
		JPanel resultPanel = new JPanel(null);
		resultPanel.setPreferredSize(new Dimension(900, 160));
		resultPanel.setBorder(BorderFactory.createTitledBorder("Function builder"));
		
		int posX = 15;
		int posY = 15;
		
		//DefaultFormatter format = new DefaultFormatter();
	   // format.setOverwriteMode(false);
		JLabel labelID = new JLabel("ID:");
		labelID.setBounds(posX, posY, 50, 20);
		resultPanel.add(labelID);
		
		idField = new JTextField();
		idField.setBounds(posX, posY+20, 60, 20);
		idField.setEditable(false);
		resultPanel.add(idField);
		
		JLabel labelFunction = new JLabel("Function edit field:");
		labelFunction.setBounds(posX+65, posY, 150, 20);
		resultPanel.add(labelFunction);
		
		functionField = new JTextField();
		functionField.setBounds(posX+65, posY+20, 350, 20);
		resultPanel.add(functionField);
		
		JLabel labelEnable = new JLabel("Enabled?");
		labelEnable.setBounds(posX+420, posY, 60, 20);
		resultPanel.add(labelEnable);
		
		enabledCheckBox = new JCheckBox("");
		enabledCheckBox.setBounds(posX+435, posY+20, 40, 20);
		resultPanel.add(enabledCheckBox);
		
		JLabel resultLabel = new JLabel("Result:");
		resultLabel.setBounds(posX+490, posY, 80, 20);
		resultPanel.add(resultLabel);
		
		currentResult = new JTextField();
		currentResult.setBounds(posX+490, posY+20, 110, 20);
		resultPanel.add(currentResult);
		
		validateButton = new JButton(Tools.getResIcon16("/icons/functionsWindow/addFIcon.png"));
		validateButton.setText("Check and add");
		validateButton.setToolTipText("Validate the equation and add it to transition functions list");
		validateButton.setMargin(new Insets(0, 0, 0, 0));
		validateButton.setBounds(posX+650, posY+20, 120, 22);
		validateButton.addActionListener(actionEvent -> addFunctionAction());
		resultPanel.add(validateButton);
		
		JButton clearButton = new JButton(Tools.getResIcon16("/icons/functionsWindow/removeFIcon.png"));
		clearButton.setText("Clear function");
		clearButton.setToolTipText("Clear the equation from the list");
		clearButton.setMargin(new Insets(0, 0, 0, 0));
		clearButton.setBounds(posX+650, posY+50, 120, 22);
		clearButton.addActionListener(actionEvent -> {
			int row = tableFunc.getSelectedRow();
			if(row == -1)
				return;


			String fID = (String) tableFunc.getValueAt(row, 0);
			FunctionContainer container = transition.getFunctionContainer(fID);
			container.simpleExpression = "";
			container.correct = false;
			container.enabled = false;

			tableFunc.getModel().setValueAt("", row, 2);
			tableFunc.getModel().setValueAt(false, row, 3);
			tableFunc.getModel().setValueAt(false, row, 6);
			tableFuncModel.fireTableDataChanged();

			idField.setText("");
			functionField.setText("");
			enabledCheckBox.setSelected(false);
			currentResult.setText("");
			commentField.setText("");

			overlord.markNetChange();
		});
		resultPanel.add(clearButton);
		
		JButton helpButton = new JButton(Tools.getResIcon16("/icons/functionsWindow/helpIcon.png"));
		helpButton.setText("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Help&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</html>");
		helpButton.setToolTipText("Show list of operations and functions");
		helpButton.setMargin(new Insets(0, 0, 0, 0));
		helpButton.setBounds(posX+650, posY+80, 120, 22);
		helpButton.addActionListener(actionEvent -> helpNotepad());
		resultPanel.add(helpButton);
		
		JCheckBox functionalActiveButton = new JCheckBox("Functional transition");
		functionalActiveButton.setBounds(posX+650, posY+110, 150, 20);
		functionalActiveButton.setSelected(transition.isFunctional());

		functionalActiveButton.addActionListener(actionEvent -> {
			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			transition.setFunctional(abstractButton.getModel().isSelected());
			pn.repaintAllGraphPanels();
			overlord.markNetChange();
		});
		resultPanel.add(functionalActiveButton);
		
		JLabel errLabel = new JLabel("Error log:", JLabel.LEFT);
		errLabel.setBounds(posX, posY+=40, 140, 20);
		resultPanel.add(errLabel);	
		
		commentField = new JTextArea();
		commentField.setLineWrap(true);
		commentField.setEditable(false);
        JPanel errorSubPanel = new JPanel();
        errorSubPanel.setLayout(new BorderLayout());
        errorSubPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        errorSubPanel.setBounds(posX, posY+=20, 600, 70);
        resultPanel.add(errorSubPanel);
		
		return resultPanel;
	}
	
	protected void helpNotepad() {
		HolmesNotepad notePad = new HolmesNotepad(640,500);
		notePad.setVisible(true);
		notePad.addTextLineNL("Arithemic and logic operators:", "text");
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("                                      |Double |Complex|String |Vector|", "text");
		notePad.addTextLineNL(" Power                         ^      |   X   |   X   |       |      |", "text");
		notePad.addTextLineNL(" Boolean Not                   !      |   X   |       |       |      |", "text");
		notePad.addTextLineNL(" Unary Plus, Unary Minus       +x, -x |   X   |   X   |       |      |", "text");
		notePad.addTextLineNL(" Modulus                       %      |   X   |       |       |      |", "text");
		notePad.addTextLineNL(" Division                      /      |   X   |   X   |       |   X  |", "text");
		notePad.addTextLineNL(" Multiplication                *      |   X   |   X   |       |   X  |", "text");
		notePad.addTextLineNL(" Addition, Subtraction         +, -   |   X   |   X   |   X   |      |", "text");
		notePad.addTextLineNL(" Less of Equal, More or Equal  <=, >= |   X   |       |       |      |", "text");
		notePad.addTextLineNL(" Less than, Greater than       <, >   |   X   |       |       |      |", "text");
		notePad.addTextLineNL(" Not Equal, Equal              !=, == |   X   |   X   |   X   |      |", "text");
		notePad.addTextLineNL(" Boolean And                   &&     |   X   |       |       |      |", "text");
		notePad.addTextLineNL(" Boolean Or                    ||     |   X   |       |       |      |", "text");
		notePad.addTextLineNL("                                                                     ", "text");
		
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL(" Mathematical functions table:", "text");
		notePad.addTextLineNL("                                      |Double |Complex|", "text");
		notePad.addTextLineNL(" Sine                        sin()    |   X   |   X   |", "text");
		notePad.addTextLineNL(" Cosine                      cos()    |   X   |   X   |", "text");
		notePad.addTextLineNL(" Tangent                     tan()    |   X   |   X   |", "text");
		notePad.addTextLineNL(" Arc Sine                    asin()   |   X   |   X   |", "text");
		notePad.addTextLineNL(" Arc Cosine                  acos()   |   X   |   X   |", "text");
		notePad.addTextLineNL(" Arc Tangent                 atan()   |   X   |   X   |", "text");
		notePad.addTextLineNL(" Hyperbolic Sine             sinh()   |   X   |   X   |", "text");
		notePad.addTextLineNL(" Hyperbolic Cosine           cosh()   |   X   |   X   |", "text");
		notePad.addTextLineNL(" Hyperbolic Tangent          tanh()   |   X   |   X   |", "text");
		notePad.addTextLineNL(" Inverse Hyperbolic Sine     asinh()  |   X   |   X   |", "text");
		notePad.addTextLineNL(" Inverse Hyperbolic Cosine   acosh()  |   X   |   X   |", "text");
		notePad.addTextLineNL(" Inverse Hyperbolic Tangent  atanh()  |   X   |   X   |", "text");
		notePad.addTextLineNL(" Natural Logarithm           ln()     |   X   |   X   |", "text");
		notePad.addTextLineNL(" Logarithm base 10           log()    |   X   |   X   |", "text");
		notePad.addTextLineNL(" Angle                       angle()  |   X   |       |", "text");
		notePad.addTextLineNL(" Absolute Value / Magnitude  abs()    |   X   |   X   |", "text");
		notePad.addTextLineNL(" Random number (between 0-1) rand()   |       |       |", "text");
		notePad.addTextLineNL(" Modulus                     mod()    |   X   |       |", "text");
		notePad.addTextLineNL(" Square Root                 sqrt()   |   X   |   X   |", "text");
		notePad.addTextLineNL(" Sum                         sum()    |   X   |       |", "text");
		notePad.addTextLineNL("               Complex numbers arythmetic:             ", "text");
		notePad.addTextLineNL(" Real Component              re()     |       |   X   |", "text");
		notePad.addTextLineNL(" Imaginary Component         im()     |       |   X   |", "text");
		notePad.setCaretFirstLine();
	}

	/**
	 * Obsługa przycisku dodawania nowej funkcji
	 */
	private void addFunctionAction() {
		int row = tableFunc.getSelectedRow();
		if(row == -1)
			return;

		commentField.setText("");
		currentResult.setText("");
		String fID = (String) tableFunc.getValueAt(row, 0);
		boolean enabled = enabledCheckBox.isSelected();
		FunctionContainer container = transition.getFunctionContainer(fID);
		boolean correct = FunctionsTools.validateFunction(container, functionField.getText(), false, commentField, places);
		
		if(!correct)
			enabled = false;
		
		transition.updateFunctionString(fID, container.simpleExpression, correct, enabled);
		tableFunc.getModel().setValueAt(container.simpleExpression, row, 2);
		tableFunc.getModel().setValueAt(correct, row, 3);
		tableFunc.getModel().setValueAt(enabled, row, 6);
		
		JEP myParser = new JEP();
		myParser.addStandardFunctions();
		for(String key : container.involvedPlaces.keySet()) {
			Place place = container.involvedPlaces.get(key);
			myParser.addVariable(key, place.getTokensNumber());
		}

		myParser.parseExpression(container.simpleExpression);

		double result = myParser.getValue();
		if((valueOf(result)).isNaN()) {
			currentResult.setText("Not-A-Number");
			container.correct = false;
			container.enabled = false;
			tableFunc.getModel().setValueAt(container.correct, row, 3);
			tableFunc.getModel().setValueAt(container.enabled, row, 6);
		} else {
			currentResult.setText(formatter.format(result));
		}
		
		/*
		Expression exp = transition.getFunctionContainer(fID).equation;
		if(exp != null && correct) {
			double result = exp.evaluate();
			
			if((new Double(result)).isNaN()) {
				currentResult.setText("Not-A-Number");
				container.correct = false;
				container.enabled = false;
				tableFunc.getModel().setValueAt(container.correct, row, 3);
				tableFunc.getModel().setValueAt(container.enabled, row, 6);
			} else
				currentResult.setText(formatter.format(result));
		}
		*/
		
		tableFuncModel.fireTableDataChanged();
		
		tableFunc.setRowSelectionInterval(row, row);
		overlord.markNetChange();
	}

	/**
	 * Metoda tworzy panel tablicy funkcji.
	 * @return JPanel - panel z tablicą.
	 */
	private JPanel createTablePanel() {
		JPanel tablesSubPanel = new JPanel(new BorderLayout());
		tablesSubPanel.setPreferredSize(new Dimension(640, 200));
		tablesSubPanel.setLocation(0, 0);
		tablesSubPanel.setBorder(BorderFactory.createTitledBorder("Tables:"));
		
		tableFuncModel = new FunctionalTransTableModel();
		tableFunc = new JTable(tableFuncModel);
		
		tableFunc.getColumnModel().getColumn(0).setHeaderValue("fID");
		tableFunc.getColumnModel().getColumn(0).setPreferredWidth(70);
		tableFunc.getColumnModel().getColumn(0).setMinWidth(70);
		tableFunc.getColumnModel().getColumn(0).setMaxWidth(70);
		tableFunc.getColumnModel().getColumn(1).setHeaderValue("Place name");
		tableFunc.getColumnModel().getColumn(1).setPreferredWidth(300);
		tableFunc.getColumnModel().getColumn(1).setMinWidth(100);
		tableFunc.getColumnModel().getColumn(2).setHeaderValue("Function");
		tableFunc.getColumnModel().getColumn(2).setPreferredWidth(300);
		tableFunc.getColumnModel().getColumn(2).setMinWidth(100);
		tableFunc.getColumnModel().getColumn(3).setHeaderValue("Correct");
		tableFunc.getColumnModel().getColumn(3).setPreferredWidth(60);
		tableFunc.getColumnModel().getColumn(3).setMinWidth(60);
		tableFunc.getColumnModel().getColumn(3).setMaxWidth(60);
		tableFunc.getColumnModel().getColumn(4).setHeaderValue("Arc type");
		tableFunc.getColumnModel().getColumn(4).setPreferredWidth(80);
		tableFunc.getColumnModel().getColumn(4).setMinWidth(80);
		tableFunc.getColumnModel().getColumn(4).setMaxWidth(80);
		tableFunc.getColumnModel().getColumn(5).setHeaderValue("Weight");
		tableFunc.getColumnModel().getColumn(5).setPreferredWidth(50);
		tableFunc.getColumnModel().getColumn(5).setMinWidth(50);
		tableFunc.getColumnModel().getColumn(5).setMaxWidth(50);
		tableFunc.getColumnModel().getColumn(6).setHeaderValue("Enabled");
		tableFunc.getColumnModel().getColumn(6).setPreferredWidth(60);
		tableFunc.getColumnModel().getColumn(6).setMinWidth(60);
		tableFunc.getColumnModel().getColumn(6).setMaxWidth(60);

		FunctionalTransTableRenderer tableRenderer = new FunctionalTransTableRenderer(tableFunc);
		tableFunc.setDefaultRenderer(Object.class, tableRenderer);
		tableFunc.setDefaultRenderer(Integer.class, tableRenderer);
		tableFunc.setDefaultRenderer(Boolean.class, tableRenderer);
		
		tableFunc.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickedFuncTable();
          	    }
          	 }
      	});
		
		
		transition.checkFunctions(arcs, places);
		ArrayList<FunctionContainer> fVector = transition.accessFunctionsList();
		for(FunctionContainer fc : fVector) { //najpierw łuki wejściowe
			if(!fc.inTransArc) // jeśli nie jest to łuk WEJŚCIOWY DO TRANZYCJI - ignoruj
				continue;

			tableFuncModel.addNew(fc.fID, fc.arc.getStartNode().getName(), fc.simpleExpression, fc.correct, fc.arc.getArcType(), fc.arc.getWeight(), fc.enabled);
		}
		
		for(FunctionContainer fc : fVector) {
			if(fc.inTransArc) // jeśli nie jest to łuk WYJŚCIOWY Z TRANZYCJI, tylko wejściowy do niej - ignoruj
				continue;

			tableFuncModel.addNew(fc.fID, fc.arc.getEndNode().getName(), fc.simpleExpression, fc.correct, fc.arc.getArcType(), fc.arc.getWeight(), fc.enabled);
		}

		tableFunc.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane tableScrollPane = new JScrollPane(tableFunc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		return tablesSubPanel;
	}
	
	/**
	 * Obsługa kliknięcia wiersza tabeli funkcji
	 */
	protected void cellClickedFuncTable() {
		int row = tableFunc.getSelectedRow();
		if(row > -1) {
			TypeOfArc type = (TypeOfArc) tableFunc.getValueAt(row, 4);
			if(type == TypeOfArc.NORMAL) {
				functionField.setEnabled(true);
				enabledCheckBox.setEnabled(true);
				validateButton.setEnabled(true);
				currentResult.setEnabled(true);
				
				String ID = (String) tableFunc.getValueAt(row, 0);
				String function = (String) tableFunc.getValueAt(row, 2);
				boolean enabled = (boolean) tableFunc.getValueAt(row, 6);
				
				idField.setText(ID);
				functionField.setText(function);
				enabledCheckBox.setSelected(enabled);
			} else {
				functionField.setEnabled(false);
				enabledCheckBox.setEnabled(false);
				validateButton.setEnabled(false);
				currentResult.setEnabled(false);
				
				functionField.setText("Function possible only for a normal arc");
				enabledCheckBox.setSelected(false);
			}
		}
	}

	/**
	 * Tworzy panel tabeli informacyjnej z listą miejsc sieci.
	 * @return JPanel - panel, a co ma być?
	 */
	private JPanel createPlacesTablePanel() {
		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel.setPreferredSize(new Dimension(900, 200));
		resultPanel.setBorder(BorderFactory.createTitledBorder("Places table for selection"));
		
		FunctionalTransAuxTableModel auxTableModel = new FunctionalTransAuxTableModel();
		JTable auxTable = new JTable(auxTableModel);
		
		auxTable.getColumnModel().getColumn(0).setHeaderValue("ID");
		auxTable.getColumnModel().getColumn(0).setPreferredWidth(40);
		auxTable.getColumnModel().getColumn(0).setMinWidth(40);
		auxTable.getColumnModel().getColumn(0).setMaxWidth(40);
		auxTable.getColumnModel().getColumn(1).setHeaderValue("Tokens");
		auxTable.getColumnModel().getColumn(1).setPreferredWidth(60);
		auxTable.getColumnModel().getColumn(1).setMinWidth(60);
		auxTable.getColumnModel().getColumn(1).setMaxWidth(60);
		auxTable.getColumnModel().getColumn(2).setHeaderValue("Place name:");
		auxTable.getColumnModel().getColumn(2).setMinWidth(100);
		
		auxTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		;
          	    }
          	 }
      	});
		
		int index = 0;
		for(Place place : places) {
			auxTableModel.addNew("p"+index, ""+place.getTokensNumber(), place.getName().replace("_", " "));
			index++;
		}

		auxTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane auxTableScrollPane = new JScrollPane(auxTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPanel.add(auxTableScrollPane, BorderLayout.CENTER);

		return resultPanel;
	}
}
