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
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.StatePlacesVector;
import holmes.petrinet.data.StatesManager;
import holmes.petrinet.elements.Place;
import holmes.tables.RXTable;
import holmes.tables.StatesPlacesTableModel;
import holmes.tables.StatesPlacesTableRenderer;
import holmes.utilities.Tools;

/**
 * Klasa tworząca okno managera stanów początkowych (m0) sieci.
 * 
 * @author MR
 */
public class HolmesStatesManager extends JFrame {
	private static final long serialVersionUID = -4590055483268695118L;
	private GUIManager overlord;
	private StatesPlacesTableRenderer tableRenderer;
	private StatesPlacesTableModel tableModel;
	private JTable statesTable;
	private JPanel tablePanel;
	private JTextArea stateDescrTextArea;
	
	private ArrayList<Place> places;
	private PetriNet pn;
	private StatesManager statesManager;
	
	private int selectedRow;
	private int cellWidth;
	
	/**
	 * Główny konstruktor okna menagera stanów początkowych.
	 */
	public HolmesStatesManager() {
		setTitle("Holmes starting states manager");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {
			
		}
    	overlord = GUIManager.getDefaultGUIManager();
    	pn = overlord.getWorkspace().getProject();
    	places = pn.getPlaces();
    	statesManager = pn.accessStatesManager();
    	selectedRow = 0;
    	cellWidth = 30;
    	
    	initalizeComponents();
    	initiateListeners();
    	overlord.getFrame().setEnabled(false);
    	fillTable();
    	setVisible(true);
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
	 * Tworzy panel główny tablicy.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("States table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new StatesPlacesTableModel(places.size(), this);
		//statesTable.setModel(tableModel);
		statesTable = new RXTable(tableModel);
		((RXTable)statesTable).setSelectAllForEdit(true);
		
		statesTable.getColumnModel().getColumn(0).setHeaderValue("Sel:");
		statesTable.getColumnModel().getColumn(0).setPreferredWidth(30);
		statesTable.getColumnModel().getColumn(0).setMinWidth(30);
		statesTable.getColumnModel().getColumn(0).setMaxWidth(30);
		statesTable.getColumnModel().getColumn(1).setHeaderValue("State ID");
		statesTable.getColumnModel().getColumn(1).setPreferredWidth(50);
		statesTable.getColumnModel().getColumn(1).setMinWidth(50);
		statesTable.getColumnModel().getColumn(1).setMaxWidth(50);
		for(int i=0; i<places.size(); i++) {
			statesTable.getColumnModel().getColumn(i+2).setHeaderValue("p"+i);
			statesTable.getColumnModel().getColumn(i+2).setPreferredWidth(cellWidth);
			statesTable.getColumnModel().getColumn(i+2).setMinWidth(cellWidth);
			statesTable.getColumnModel().getColumn(i+2).setMaxWidth(cellWidth);
        }
		
		//TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(statesTable.getModel());
		//statesTable.setRowSorter(sorter);
        
		statesTable.setName("StatesTable");
		statesTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		tableRenderer = new StatesPlacesTableRenderer(statesTable);
		statesTable.setDefaultRenderer(Object.class, tableRenderer);
		statesTable.setDefaultRenderer(Double.class, tableRenderer);
		
    	statesTable.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
          	    		cellClickAction();
          	    }
          	 }
      	});
    	
    	statesTable.setRowSelectionAllowed(false);
    	
    	statesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(statesTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
			
	    return result;
	}
	
	/**
	 * Metoda wywoływana przez akcję renderera tablicy, gdy następuje zmiana w komórce.
	 * @param row int - nr wiersza tablicy
	 * @param column int - nr kolumny tablicy
	 * @param value double - nowa wartość
	 */
	public void changeState(int row, int column, double value) {
		statesManager.getState(row).accessVector().set(column-2, value);
		overlord.markNetChange();
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
		selectStateButton.setFocusPainted(false);
		selectStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/selectStateIcon.png"));
		selectStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(places.size() == 0) {
					noNetInfo();
					return;
				}
				int selected = statesTable.getSelectedRow();
				Object[] options = {"Set new state", "Keep old state",};
				int n = JOptionPane.showOptionDialog(null,
								"Set all places of the net according to the selected (table row: "+(selected+1)+") state?",
								"Set new state?", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if (n == 0) {
					tableModel.setSelected(selected);
					statesManager.setNetworkState(selected);
					pn.repaintAllGraphPanels();
					tableModel.fireTableDataChanged();
					overlord.markNetChange();
				}
			}
		});
		result.add(selectStateButton);
		
		JButton addNewStateButton = new JButton("<html>Add current<br/>&nbsp;&nbsp;net state</html>");
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setFocusPainted(false);
		addNewStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/addStateIcon.png"));
		addNewStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(places.size() == 0) {
					noNetInfo();
					return;
				}
				Object[] options = {"Add new state", "Cancel",};
				int n = JOptionPane.showOptionDialog(null,
								"Add current net state to states table?",
								"Add new state?", JOptionPane.YES_NO_OPTION,
								JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				if (n == 0) {
					statesManager.addCurrentState();
					addLastStateToTable();
					tableModel.fireTableDataChanged();
				}
			}
		});
		result.add(addNewStateButton);
		
		JButton replaceStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Replace&nbsp;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;state&nbsp;</html>");
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setFocusPainted(false);
		replaceStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/replaceStateIcon.png"));
		replaceStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(places.size() == 0) {
					noNetInfo();
					return;
				}
				replaceStateAction();
			}
		});
		result.add(replaceStateButton);
		
		JButton removeStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Remove&nbsp;&nbsp;<br/>&nbsp;&nbsp;&nbsp;&nbsp;state&nbsp;&nbsp;</html>");
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setFocusPainted(false);
		removeStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/removeStateIcon.png"));
		removeStateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(places.size() == 0) {
					noNetInfo();
					return;
				}
				removeStateAction();
			}
		});
		result.add(removeStateButton);
		
	    return result;
	}
	
	/**
	 * Krótki komunikat, że nie ma sieci.
	 */
	private void noNetInfo() {
		JOptionPane.showMessageDialog(this, "There are no places in the net!", 
				"No net", JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Obsługa usuwania stanu sieci.
	 */
	private void removeStateAction() {
		int selected = statesTable.getSelectedRow();
		int states = statesManager.accessStateMatrix().size();
		if(states == 1) {
			JOptionPane.showMessageDialog(null, "At least one net state must remain!", 
					"Warning",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		Object[] options = {"Remove state", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Remove selected state (table row: "+(selected+1)+") from the states table?",
						"Remove state?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		statesManager.removeState(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Zastępowanie stanu z tabeli, aktualnym stanem sieci.
	 */
	private void replaceStateAction() {
		int selected = statesTable.getSelectedRow();
		Object[] options = {"Replace state", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Replace selected state (table row: "+(selected+1)+") with the current net state?",
						"Replace state?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		statesManager.replaceStateWithNetState(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Metoda dodaje ostatni stan z listy stanów do tabeli - potrzeba do akcji przycisku dodającego
	 * aktualny stan sieci do tabeli stanów.
	 */
	private void addLastStateToTable() {
		int states = statesManager.accessStateMatrix().size();
		StatePlacesVector psVector = statesManager.getState(states-1);
		
		ArrayList<String> rowVector = new ArrayList<String>();
		
		rowVector.add("");
		rowVector.add("m0("+(states)+")");
		for(int p=0; p<psVector.getSize(); p++) {
			rowVector.add(""+psVector.getTokens(p));
    	}
		tableModel.addNew(rowVector);
		overlord.markNetChange();
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTable() {
		tableModel.clearModel(places.size());
		
		int selectedState = statesManager.selectedState;
    	for(int row=0; row<statesManager.accessStateMatrix().size(); row++) {
    		ArrayList<String> rowVector = new ArrayList<String>();
    		
    		if(selectedState == row)
    			rowVector.add("X");
    		else
    			rowVector.add("");
    		
    		StatePlacesVector psVector = statesManager.getState(row);
    		rowVector.add("m0("+(row+1)+")");
    		
    		for(int p=0; p<psVector.getSize(); p++) {
    			rowVector.add(""+psVector.getTokens(p));
        	}
    		tableModel.addNew(rowVector);
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
		
		stateDescrTextArea = new JTextArea();
		stateDescrTextArea.setLineWrap(true);
		stateDescrTextArea.setEditable(true);
		stateDescrTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		statesManager.setStateDescription(selectedRow, newComment);
            	}
            }
        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(stateDescrTextArea), BorderLayout.CENTER);
        CreationPanel.setBounds(posXda, posYda+=25, 600, 100);
        result.add(CreationPanel);

		
	    return result;
	}
	
	/**
	 * Ustawia pole opisy wybranego stanu.
	 */
	private void fillDescriptionField() {
		String description = statesManager.getStateDescription(selectedRow);
		stateDescrTextArea.setText(description);
	}
	
	/**
	 * Metoda obsługująca kliknięcie dowolnej komórki.
	 */
	protected void cellClickAction() {
		try {
			selectedRow = statesTable.getSelectedRow();
			fillDescriptionField();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	overlord.getFrame().setEnabled(true);
		    }
		});
    }
}
