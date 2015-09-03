package holmes.windows;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.DefaultFormatter;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.functions.FunctionContainer;
import holmes.petrinet.functions.FunctionsTools;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.tables.FunctionalTransAuxTableModel;
import holmes.tables.FunctionalTransTableModel;
import holmes.tables.FunctionalTransTableRenderer;
import holmes.tables.PTITableRenderer;
import holmes.utilities.Tools;

public class HolmesFunctionalTrans extends JFrame {
	private static final long serialVersionUID = 1235426932930026597L;
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
	
	private void initializeComponents() {
		this.setLocation(20, 20);
		setSize(new Dimension(900, 600));

		mainPanel = new JPanel(new BorderLayout());
		//mainPanel.setBounds(0, 0, 600, 450);
		
		tablePanel = createTablePanel();
		mainPanel.add(tablePanel, BorderLayout.NORTH);
		
		mainPanel.add(createAuxPanel(), BorderLayout.CENTER);
		
		int mPanelX = 0;
		int mPanelY = 0;
		
		add(mainPanel);
	}
	
	private JPanel createAuxPanel() {
		JPanel resultPanel = new JPanel(new BorderLayout());
		resultPanel.setPreferredSize(new Dimension(900, 400));
		resultPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		
		resultPanel.add(createBuilderPanel(),BorderLayout.NORTH);
		resultPanel.add(createPlacesTablePanel(), BorderLayout.CENTER);
		
		return resultPanel;
	}

	private JPanel createBuilderPanel() {
		JPanel resultPanel = new JPanel(null);
		resultPanel.setPreferredSize(new Dimension(900, 100));
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
		functionField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				//JFormattedTextField field = (JFormattedTextField) e.getSource();
				//try {
				//	field.commitEdit();
				//} catch (ParseException ex) {
				//}
				//String newName = (String) field.getText();
				//changeName(newName);
			}
		});
		resultPanel.add(functionField);
		
		JButton validateButton = new JButton(Tools.getResIcon16("/icons/aaa.png"));
		validateButton.setText("Check and add");
		validateButton.setToolTipText("Validate correctness of function");
		validateButton.setMargin(new Insets(0, 0, 0, 0));
		validateButton.setBounds(posX+420, posY+20, 120, 20);
		validateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int row = tableFunc.getSelectedRow();
				if(row == -1)
					return; 
				tableFunc.getModel().setValueAt(functionField.getText(), row, 2);
				tableFuncModel.fireTableDataChanged();
				
				String fID = (String) tableFunc.getValueAt(row, 0);
				boolean enabled = enabledCheckBox.isSelected();
				boolean correct = FunctionsTools.validateFunction(transition.getFunction(fID));
				
				transition.updateFunctionString(fID, functionField.getText(), correct, enabled);
			}
		});
		resultPanel.add(validateButton);
		
		JLabel labelEnable = new JLabel("Enabled?");
		labelEnable.setBounds(posX+550, posY, 80, 20);
		resultPanel.add(labelEnable);
		
		enabledCheckBox = new JCheckBox("");
		enabledCheckBox.setBounds(posX+550, posY+20, 50, 20);
		resultPanel.add(enabledCheckBox);
		
		return resultPanel;
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
			String ID = (String) tableFunc.getValueAt(row, 0);
			String function = (String) tableFunc.getValueAt(row, 2);
			boolean enabled = (boolean) tableFunc.getValueAt(row, 6);
			
			idField.setText(ID);
			functionField.setText(function);
			enabledCheckBox.setEnabled(enabled);
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
		auxTable.getColumnModel().getColumn(1).setHeaderValue("Place name:");
		//auxTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		auxTable.getColumnModel().getColumn(1).setMinWidth(100);
		
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
			auxTableModel.addNew("p"+index, place.getName().replace("_", " "));
			index++;
		}

		auxTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane auxTableScrollPane = new JScrollPane(auxTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultPanel.add(auxTableScrollPane, BorderLayout.CENTER);

		return resultPanel;
	}
}
