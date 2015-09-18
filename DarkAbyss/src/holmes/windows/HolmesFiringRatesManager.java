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

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.FiringRatesManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.tables.FiringRatesVectorRenderer;
import holmes.tables.FiringRatesVectorTableModel;
import holmes.tables.RXTable;
import holmes.utilities.Tools;

/**
 * Okno zarządzania wektorami firing rates dla tranzycji stochastycznych.
 * 
 * @author MR
 */
public class HolmesFiringRatesManager extends JFrame {
	private static final long serialVersionUID = 8184934957669150556L;
	private GUIManager overlord;
	private JFrame parentWindow;
	private JFrame ego;
	@SuppressWarnings("unused")
	private boolean doNotUpdate = false;
	private FiringRatesVectorRenderer tableRenderer;
	private FiringRatesVectorTableModel tableModel;
	private JTable table;
	private JPanel tablePanel;
	private JTextArea vectorDescrTextArea;
	
	private ArrayList<Transition> transitions;
	private PetriNet pn;
	private FiringRatesManager firingRatesManager;
	
	private int selectedRow;
	
	/**
	 * Główny konstruktor okna menagera stanów początkowych.
	 */
	public HolmesFiringRatesManager(JFrame parent) {
		setTitle("Holmes SPN transitions firing rates manager");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
    	this.overlord = GUIManager.getDefaultGUIManager();
    	this.pn = overlord.getWorkspace().getProject();
    	this.ego = this;
    	this.parentWindow = parent;
    	this.transitions = pn.getTransitions();
    	this.firingRatesManager = pn.accessFiringRatesManager();
    	this.selectedRow = 0;
    	
    	initalizeComponents();
    	initiateListeners();
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
		
		tableModel = new FiringRatesVectorTableModel(this);
		table = new RXTable(tableModel);
		((RXTable)table).setSelectAllForEdit(true);
		
		table.getColumnModel().getColumn(0).setHeaderValue("Sel.");
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		table.getColumnModel().getColumn(0).setMinWidth(40);
		table.getColumnModel().getColumn(0).setMaxWidth(40);
		table.getColumnModel().getColumn(1).setHeaderValue("ID");
		table.getColumnModel().getColumn(1).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setMinWidth(30);
		table.getColumnModel().getColumn(1).setMaxWidth(30);
		table.getColumnModel().getColumn(2).setHeaderValue("Firing rate vector description");
		table.getColumnModel().getColumn(2).setMinWidth(50);

        
		table.setName("FiringRatesTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		tableRenderer = new FiringRatesVectorRenderer(table);
		table.setDefaultRenderer(Object.class, tableRenderer);
		table.setDefaultRenderer(Double.class, tableRenderer);
		table.setDefaultRenderer(Integer.class, tableRenderer);
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction();
          	    }
          	 }
      	});
    	
		table.setRowSelectionAllowed(false);
    	
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
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
		firingRatesManager.accessFRMatrix().get(row).setDescription(value);
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
		
		JButton selectStateButton = new JButton("<html>Select this<br>firing rates</html>");
		selectStateButton.setBounds(posXda, posYda, 130, 40);
		selectStateButton.setMargin(new Insets(0, 0, 0, 0));
		selectStateButton.setFocusPainted(false);
		selectStateButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/selectVectorIcon.png"));
		selectStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0) {
					noNetInfo();
					return;
				}
				int selected = table.getSelectedRow();
				if(selected == -1)
					return;
				
				Object[] options = {"Set new rates", "Keep old ones",};
				int n = JOptionPane.showOptionDialog(null,
								"Set all transitions of the net according to the selected\n"
								+ "firing rates vector (table row: "+selected+") ?",
								"Set new firing rates?", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if (n == 0) {
					tableModel.setSelected(selected);
					firingRatesManager.setNetworkFRVector(selected);
					tableModel.fireTableDataChanged();

				}
			}
		});
		result.add(selectStateButton);
		
		JButton addNewStateButton = new JButton("<html>Save current<br/>&nbsp;&nbsp;firing rates</html>");
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setFocusPainted(false);
		addNewStateButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/addVectorIcon.png"));
		addNewStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0) {
					noNetInfo();
					return;
				}
				Object[] options = {"Add new vector", "Cancel",};
				int n = JOptionPane.showOptionDialog(null,
								"Remember current net firing rates in the table?",
								"Add new firing rates vactor?", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if (n == 0) {
					firingRatesManager.addCurrentStateAsFRVector();
					addLastStateToTable();
					tableModel.fireTableDataChanged();
					overlord.markNetChange();
				}
			}
		});
		result.add(addNewStateButton);
		
		JButton replaceStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Replace&nbsp;<br/>firing rates</html>");
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setFocusPainted(false);
		replaceStateButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/replaceVectorIcon.png"));
		replaceStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0) {
					noNetInfo();
					return;
				}
				replaceStateAction();
			}
		});
		result.add(replaceStateButton);
		
		JButton removeStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Remove&nbsp;&nbsp;<br/>firing rates</html>");
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setFocusPainted(false);
		removeStateButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/removeVectorIcon.png"));
		removeStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0) {
					noNetInfo();
					return;
				}
				removeStateAction();
			}
		});
		result.add(removeStateButton);
		
		JButton editStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Edit&nbsp;&nbsp;&nbsp;&nbsp;<br/>firing rates</html>");
		editStateButton.setBounds(posXda, posYda+=50, 130, 40);
		editStateButton.setMargin(new Insets(0, 0, 0, 0));
		editStateButton.setFocusPainted(false);
		editStateButton.setIcon(Tools.getResIcon32("/icons/fRatesManager/fireRateEdit.png"));
		editStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(transitions.size() == 0) {
					noNetInfo();
					return;
				}
				int selected = table.getSelectedRow();
				if(selected > -1)
					new HolmesFiringRatesEditor(ego, firingRatesManager.getFRVector(selected), selected);
			}
		});
		result.add(editStateButton);
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
					"Cannot delete!",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		Object[] options = {"Remove vector", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Remove selected firing rates vector from the table\n"
						+ "(table row: "+selected+") ?",
						"Remove firing rates vector?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		firingRatesManager.removeFRVector(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Krótki komunikat, że nie ma sieci.
	 */
	private void noNetInfo() {
		JOptionPane.showMessageDialog(this, "There are no transitions in the net!", 
				"No net", JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Zastępowanie wektora firing rates z tabeli, aktualnym stanem fr sieci.
	 */
	private void replaceStateAction() {
		int selected = table.getSelectedRow();
		Object[] options = {"Replace vector", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Replace selected firing rates vector (table row: "+selected+")\n"
								+ "with the currently set net rates?",
						"Replace firing rates vector?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		firingRatesManager.replaceFRvectorWithNetState(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Metoda dodaje ostatni wektor firing rates z listy stanów do tabeli - potrzeba do akcji przycisku dodającego
	 * aktualny stan sieci do tabeli stanów.
	 */
	private void addLastStateToTable() {
		int states = firingRatesManager.accessFRMatrix().size();
		tableModel.addNew("", states-1, firingRatesManager.getFRVectorDescription(states-1));
	}

	/**
	 * Tworzy panel dolny z informacjami o wektorze firing rates.
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
            		firingRatesManager.setFRvectorDescription(selectedRow, newComment);
            		selectedRow = table.getSelectedRow();
            		fillTable();
            		table.setRowSelectionInterval(selectedRow, selectedRow);
            		overlord.markNetChange();
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
			//doNotUpdate = true;
			selectedRow = newSelection;
			fillDescriptionField();
			//doNotUpdate = false;
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTable() {
		tableModel.clearModel();
		
		int selectedFR = firingRatesManager.selectedVector;
		
    	for(int row=0; row<firingRatesManager.accessFRMatrix().size(); row++) {
    		if(row == selectedFR) {
    			tableModel.addNew("X", row, firingRatesManager.getFRVectorDescription(row));
    		} else {
    			tableModel.addNew("", row, firingRatesManager.getFRVectorDescription(row));
    		}
    	}
    	
		tableModel.fireTableDataChanged();
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
