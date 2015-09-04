package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;

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

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Arc.TypesOfArcs;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.functions.FunctionContainer;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.tables.FunctionalTransAuxTableModel;
import holmes.tables.FunctionalTransTableModel;
import holmes.tables.FunctionalTransTableRenderer;
import holmes.utilities.Tools;
import net.objecthunter.exp4j.Expression;

/**
 * Okno zarządzania funkcjami wskazanej tranzycji.
 * 
 * @author MR
 */
public class HolmesFunctionalTrans extends JFrame {
	private static final long serialVersionUID = 1235426932930026597L;
	private static final DecimalFormat formatter = new DecimalFormat( "#.###" );
	private Transition transition;
	private ArrayList<Place> places;
	private ArrayList<Arc> arcs;
	private JPanel mainPanel;
	private GUIManager overlord;
	private PetriNet pn;
	private boolean mainSimulatorActive;
	private JPanel tablePanel;
	private JTable tableFunc;
	private FunctionalTransTableModel tableFuncModel;
	private FunctionalTransTableRenderer tableRenderer;
	private JScrollPane tableScrollPane;
	
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
	public HolmesFunctionalTrans(Transition trans) {
		overlord = GUIManager.getDefaultGUIManager();
		pn = overlord.getWorkspace().getProject();
		this.transition = trans;
		this.places = pn.getPlaces();
		this.arcs = pn.getArcs();
		
		setTitle("Transition: "+trans.getName());
		try {
			setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		
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
					"Function editor unavailble when simulator is working.", 
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
		mainPanel = new JPanel(new BorderLayout());
		tablePanel = createTablePanel();
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
		resultPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		
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
		
		validateButton = new JButton(Tools.getResIcon16("/icons/aaa.png"));
		validateButton.setText("Check and add");
		validateButton.setToolTipText("Validate the equation and add it to transition functions list");
		validateButton.setMargin(new Insets(0, 0, 0, 0));
		validateButton.setBounds(posX+650, posY+20, 120, 20);
		validateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				addFunctionAction();
			}
		});
		resultPanel.add(validateButton);
		
		JButton clearButton = new JButton(Tools.getResIcon16("/icons/aaa.png"));
		clearButton.setText("Clear function");
		clearButton.setToolTipText("Clear the equation from the list");
		clearButton.setMargin(new Insets(0, 0, 0, 0));
		clearButton.setBounds(posX+650, posY+50, 120, 20);
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int row = tableFunc.getSelectedRow();
				if(row == -1)
					return;

				
				String fID = (String) tableFunc.getValueAt(row, 0);
				FunctionContainer container = transition.getFunctionContainer(fID);
				container.function = "";
				container.equation = null;
				container.correct = false;
				container.enabled = false;

				tableFunc.getModel().setValueAt("", row, 2);
				tableFunc.getModel().setValueAt(false, row, 3);
				tableFunc.getModel().setValueAt(false, row, 6);
				tableFuncModel.fireTableDataChanged();
				
				commentField.setText("");
			}
		});
		resultPanel.add(clearButton);
		
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
	
	/**
	 * Obsługa przycisku dodawania nowej funkcji
	 */
	private void addFunctionAction() {
		int row = tableFunc.getSelectedRow();
		if(row == -1)
			return;

		commentField.setText("");
		String fID = (String) tableFunc.getValueAt(row, 0);
		boolean enabled = enabledCheckBox.isSelected();
		FunctionContainer container = transition.getFunctionContainer(fID);
		boolean correct = FunctionsTools.validateFunction(container, functionField.getText(), false, commentField, places);
		
		if(!correct)
			enabled = false;
		
		transition.updateFunctionString(fID, container.function, correct, enabled);
		tableFunc.getModel().setValueAt(container.function, row, 2);
		tableFunc.getModel().setValueAt(correct, row, 3);
		tableFunc.getModel().setValueAt(enabled, row, 6);
		tableFuncModel.fireTableDataChanged();
		
		Expression exp = transition.getFunctionContainer(fID).equation;
		if(exp != null) {
			double result = exp.evaluate();
			currentResult.setText(formatter.format(result));
		}
		
		tableFunc.setRowSelectionInterval(row, row);
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
		tableFunc.getColumnModel().getColumn(0).setPreferredWidth(60);
		tableFunc.getColumnModel().getColumn(0).setMinWidth(60);
		tableFunc.getColumnModel().getColumn(0).setMaxWidth(60);
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
    	
		tableRenderer = new FunctionalTransTableRenderer(tableFunc);
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

			tableFuncModel.addNew(fc.fID, fc.arc.getStartNode().getName(), fc.function, fc.correct, fc.arc.getArcType(), fc.arc.getWeight(), fc.enabled);
		}
		
		for(FunctionContainer fc : fVector) {
			if(fc.inTransArc) // jeśli nie jest to łuk WYJŚCIOWY Z TRANZYCJI, tylko wejściowy do niej - ignoruj
				continue;

			tableFuncModel.addNew(fc.fID, fc.arc.getEndNode().getName(), fc.function, fc.correct, fc.arc.getArcType(), fc.arc.getWeight(), fc.enabled);
		}

		tableFunc.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		tableScrollPane = new JScrollPane(tableFunc, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tablesSubPanel.add(tableScrollPane, BorderLayout.CENTER);
		return tablesSubPanel;
	}
	
	/**
	 * Obsługa kliknięcia wiersza tabeli funkcji
	 */
	protected void cellClickedFuncTable() {
		int row = tableFunc.getSelectedRow();
		if(row > -1) {
			TypesOfArcs type = (TypesOfArcs) tableFunc.getValueAt(row, 4);
			if(type == TypesOfArcs.NORMAL) {
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
