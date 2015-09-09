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
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.FiringRatesManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.PlacesStateVector;
import holmes.petrinet.data.StatesManager;
import holmes.petrinet.data.TransFiringRateVector;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.tables.FiringRatesTransitionsTableModel;
import holmes.tables.RXTable;
import holmes.tables.StatesPlacesTableModel;
import holmes.tables.StatesPlacesTableRenderer;
import holmes.utilities.Tools;

public class HolmesFiringRatesManager extends JFrame {
	private GUIManager overlord;
	private JFrame parentWindow;
	private DefaultTableCellRenderer tableRenderer;
	private FiringRatesTransitionsTableModel tableModel;
	private JTable table;
	private JPanel tablePanel;
	private JTextArea vectorDescrTextArea;
	
	private ArrayList<Transition> transitions;
	private PetriNet pn;
	private FiringRatesManager firingRatesManager;
	
	private int selectedRow;
	private int cellWidth;
	
	/**
	 * Główny konstruktor okna menagera stanów początkowych.
	 */
	public HolmesFiringRatesManager(JFrame parent) {
		setTitle("Holmes SPN transitions firing rates manager");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
    	overlord = GUIManager.getDefaultGUIManager();
    	pn = overlord.getWorkspace().getProject();
    	transitions = pn.getTransitions();
    	firingRatesManager = pn.accessFiringRatesManager();
    	selectedRow = 0;
    	cellWidth = 30;
    	
    	initalizeComponents();
    	initiateListeners();
    	GUIManager.getDefaultGUIManager().getFrame().setEnabled(false);
    	fillTable();
    	setVisible(true);
    	parentWindow.setEnabled(false);
	}
	
	/**
	 * Tworzy panel główny tablicy.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("Firing rates vectors table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new FiringRatesTransitionsTableModel(this);
		//statesTable.setModel(tableModel);
		table = new RXTable(tableModel);
		((RXTable)table).setSelectAllForEdit(true);
		
		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(1).setHeaderValue("Firing rate vector description");
		table.getColumnModel().getColumn(1).setMinWidth(50);

        
		table.setName("FiringRatesTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		tableRenderer = new DefaultTableCellRenderer();
		table.setDefaultRenderer(Object.class, tableRenderer);

		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction();
          	    }
          	 }
      	});
    	
		table.setRowSelectionAllowed(false);
    	
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
			
	    return result;
	}
	
	/**
	 * Główna metoda tworząca panele okna.
	 */
	private void initalizeComponents() {
		setLayout(new BorderLayout());
		setSize(new Dimension(900, 650));
		setLocation(50, 50);
		setResizable(true);
		
		setLayout(new BorderLayout());
		JPanel main = new JPanel(new BorderLayout());

		JPanel submain = new JPanel(new BorderLayout());
		
		tablePanel = getMainTablePanel();
		submain.add(tablePanel, BorderLayout.CENTER);
		submain.add(getBottomPanel(), BorderLayout.SOUTH);
		
		main.add(submain, BorderLayout.CENTER);
		main.add(getButtonsPanel(), BorderLayout.EAST);
		
		add(main, BorderLayout.CENTER);
	}
	
	/**
	 * Metoda wywoływana przez akcję renderera tablicy, gdy następuje zmiana w komórce.
	 * @param row int - nr wiersza tablicy
	 * @param column int - nr kolumny tablicy
	 * @param value String - nowy opis
	 */
	public void changeState(int row, int column, String value) {
		firingRatesManager.accessFRVectorsNames().set(row, value);
		//firingRatesManager.getState(row).accessVector().set(column-2, value);
	}
	
	/**
	 * Tworzy panel przycisków bocznych.
	 * @return JPanel - panel
	 */
	public JPanel getButtonsPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Buttons"));
		result.setPreferredSize(new Dimension(150, 500));

		int posXda = 10;
		int posYda = 25;
		
		JButton selectStateButton = new JButton("Set net state");
		selectStateButton.setBounds(posXda, posYda, 130, 40);
		selectStateButton.setMargin(new Insets(0, 0, 0, 0));
		selectStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/aaa.png"));
		selectStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0)
					return;
				
				int selected = table.getSelectedRow();
				Object[] options = {"Set new state", "Keep old state",};
				int n = JOptionPane.showOptionDialog(null,
								"Set all transitions of the net according to the selected (table row: "+selected+") vector?",
								"Set new firing rows?", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if (n == 0) {
					
					firingRatesManager.setNetworkFRVector(selected);
					pn.repaintAllGraphPanels();
					//tableModel.fireTableDataChanged();
				}
			}
		});
		result.add(selectStateButton);
		
		JButton addNewStateButton = new JButton("<html>Add current<br/>&nbsp;&nbsp;net state</html>");
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/addStateIcon.png"));
		addNewStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0)
					return;
				
				Object[] options = {"Add new vector", "Cancel",};
				int n = JOptionPane.showOptionDialog(null,
								"Add current net firing rates vector to the table?",
								"Add new firing rates??", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if (n == 0) {
					firingRatesManager.addCurrentStateAsFRVector();
					addLastStateToTable();
					tableModel.fireTableDataChanged();
					//fillTable();
				}
			}
		});
		result.add(addNewStateButton);
		
		JButton replaceStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Replace&nbsp;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;state&nbsp;</html>");
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/replaceStateIcon.png"));
		replaceStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0)
					return;
				
				replaceStateAction();
			}
		});
		result.add(replaceStateButton);
		
		JButton removeStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Remove&nbsp;&nbsp;<br/>&nbsp;&nbsp;&nbsp;&nbsp;state&nbsp;&nbsp;</html>");
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/removeStateIcon.png"));
		removeStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0)
					return;
				
				removeStateAction();
			}
		});
		result.add(removeStateButton);
		
	    return result;
	}
	
	/**
	 * Obsługa usuwania stanu sieci.
	 */
	private void removeStateAction() {
		int selected = table.getSelectedRow();
		int states = firingRatesManager.accessFRMatrix().size();
		if(states == 1) {
			JOptionPane.showMessageDialog(null, "At least one net firing rates vector must remain!", 
					"Warning",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		Object[] options = {"Remove vector", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Remove selected firing rates vector (table row: "+selected+") from the table?",
						"Remove firing rates vector?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		firingRatesManager.removeFRVector(selected);
		fillTable();
	}
	
	/**
	 * Zastępowanie stanu z tabeli, aktualnym stanem sieci.
	 */
	private void replaceStateAction() {
		int selected = table.getSelectedRow();
		Object[] options = {"Replace vector", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Replace selected firing rates vector (table row: "+selected+") with the current net state?",
						"Replace firing rates vector?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		firingRatesManager.replaceVectorWithNetState(selected);
		fillTable();
	}
	
	/**
	 * Metoda dodaje ostatni stan z listy stanów do tabeli - potrzeba do akcji przycisku dodającego
	 * aktualny stan sieci do tabeli stanów.
	 */
	private void addLastStateToTable() {
		int states = firingRatesManager.accessFRMatrix().size();
		TransFiringRateVector frVector = firingRatesManager.getFRVector(states-1);
		tableModel.addNew(states-1, firingRatesManager.getFRVectorDescription(states-1));
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTable() {
		tableModel.clearModel();
		
		int selectedState = firingRatesManager.selectedVector;
    	for(int row=0; row<firingRatesManager.accessFRMatrix().size(); row++) {
    		tableModel.addNew(row, firingRatesManager.getFRVectorDescription(row));
    	}
    	
		tableModel.fireTableDataChanged();
	}
	
	/**
	 * Tworzy panel dolny z informacjami o stanie.
	 * @return JPanel - panel
	 */
	public JPanel getBottomPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Others"));
		result.setPreferredSize(new Dimension(900, 150));

		int posXda = 10;
		int posYda = 15;
		
		JLabel label0 = new JLabel("State description:");
		label0.setBounds(posXda, posYda, 140, 20);
		result.add(label0);
		
		vectorDescrTextArea = new JTextArea();
		vectorDescrTextArea.setLineWrap(true);
		vectorDescrTextArea.setEditable(true);
		vectorDescrTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		firingRatesManager.setStateDescription(selectedRow, newComment);
            	}
            }
        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(vectorDescrTextArea), BorderLayout.CENTER);
        CreationPanel.setBounds(posXda, posYda+=25, 600, 100);
        result.add(CreationPanel);

		
	    return result;
	}
	
	/**
	 * Ustawia pole opisu wybranego stanu.
	 */
	private void fillDescriptionField() {
		String description = firingRatesManager.getFRVectorDescription(selectedRow);
		vectorDescrTextArea.setText(description);
	}
	
	/**
	 * Metoda obsługująca kliknięcie dowolnej komórki.
	 */
	protected void cellClickAction() {
		try {
			int newSelection = table.getSelectedRow();
			if(newSelection != selectedRow) {
				selectedRow = newSelection;
				//fillDescriptionField();
			}
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	parentWindow.setEnabled(true);
		    }
		});
    }
}
