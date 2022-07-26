package holmes.windows.managers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.darkgui.GUIController;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.StatePlacesVector;
import holmes.petrinet.data.P_StateManager;
import holmes.petrinet.data.MultisetM;
import holmes.petrinet.elements.Place;
import holmes.tables.RXTable;
import holmes.tables.managers.StatesPlacesTableModel;
import holmes.tables.managers.StatesPlacesTableModelXTPN;
import holmes.tables.managers.StatesPlacesTableRenderer;
import holmes.tables.managers.StatesPlacesTableRendererXTPN;
import holmes.utilities.Tools;

/**
 * Klasa tworząca okno managera stanów początkowych (m0) sieci.
 * 
 * @author MR
 */
public class HolmesStatesManager extends JFrame {
	@Serial
	private static final long serialVersionUID = -4590055483268695118L;
	private final JFrame ego;
	private final GUIManager overlord;
	private StatesPlacesTableModel tableModelPN;
	private StatesPlacesTableModelXTPN tableModelXTPN;
	private JTable statesTablePN;
	private JTable statesTableXTPN;
	private JTextArea stateDescrTextAreaPN;
	private JTextArea stateDescrTextAreaXTPN;
	private final ArrayList<Place> places;
	private final PetriNet pn;
	private final P_StateManager statesManager;
	private int selectedRow;
	private final int cellWidth;
	
	/**
	 * Główny konstruktor okna managera stanów początkowych.
	 */
	public HolmesStatesManager() {
		setTitle("Holmes p-states states manager");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log("Error (281108210) | Exception:  "+ex.getMessage(), "error", false);
		}
    	overlord = GUIManager.getDefaultGUIManager();
    	ego = this;
    	pn = overlord.getWorkspace().getProject();
    	places = pn.getPlaces();
    	statesManager = pn.accessStatesManager();
    	selectedRow = 0;
    	cellWidth = 50;
    	
    	initalizeComponents();
    	initiateListeners();
    	overlord.getFrame().setEnabled(false);
    	fillTable();
		fillTableXTPN();

		statesTableXTPN.setRowSelectionInterval(0,0);
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
		JPanel tablePanelPN = getMainTablePanel();
		submain.add(tablePanelPN, BorderLayout.CENTER);
		submain.add(getBottomPanel(), BorderLayout.SOUTH);
		main.add(submain, BorderLayout.CENTER);
		main.add(getButtonsPanel(), BorderLayout.EAST);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("PN p-states", Tools.getResIcon22("/icons/stateManager/PNtab.png"), main, "Petri net, TPN, DPN, SPN");
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JPanel mainXTPN = new JPanel(new BorderLayout());
		JPanel submainXTPN = new JPanel(new BorderLayout());
		JPanel tablePanelXTPN = getMainTablePanelXTPN();
		submainXTPN.add(tablePanelXTPN, BorderLayout.CENTER);
		submainXTPN.add(getBottomPanelXTPN(), BorderLayout.SOUTH);
		mainXTPN.add(submainXTPN, BorderLayout.CENTER);
		mainXTPN.add(getButtonsPanelXTPN(), BorderLayout.EAST);

		//tabbedPane.addTab("XTPN", mainXTPN);
		tabbedPane.addTab("XTPN p-states", Tools.getResIcon22("/icons/stateManager/XTPNtab.png"), mainXTPN, "XTPN p-states manager");
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.addChangeListener(e -> {
			int selected = ((JTabbedPane)e.getSource()).getSelectedIndex();
			if(selected == 1 && GUIController.access().getCurrentNetType() != PetriNet.GlobalNetType.XTPN ) {
				JOptionPane.showMessageDialog(null, "XTPN state manager unavailable for normal nets.",
						"Wrong tab", JOptionPane.INFORMATION_MESSAGE);
				((JTabbedPane)e.getSource()).setSelectedIndex(0);
			} else if(selected == 0 && GUIController.access().getCurrentNetType() == PetriNet.GlobalNetType.XTPN) {
				JOptionPane.showMessageDialog(null, "Normal state manager unavailable for XTPN nets.",
						"Wrong tab", JOptionPane.INFORMATION_MESSAGE);
				((JTabbedPane) e.getSource()).setSelectedIndex(1);
			}
			//
			//System.out.println("Tab: " + tabbedPane.getSelectedIndex());
		});

		if(GUIController.access().getCurrentNetType() == PetriNet.GlobalNetType.XTPN) {
			tabbedPane.setSelectedIndex(1);
		}
	}
	
	/**
	 * Tworzy panel główny tablicy.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("p-states table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModelPN = new StatesPlacesTableModel(places.size(), this);
		//statesTable.setModel(tableModel);
		statesTablePN = new RXTable(tableModelPN);
		((RXTable) statesTablePN).setSelectAllForEdit(true);
		
		statesTablePN.getColumnModel().getColumn(0).setHeaderValue("Sel:");
		statesTablePN.getColumnModel().getColumn(0).setPreferredWidth(30);
		statesTablePN.getColumnModel().getColumn(0).setMinWidth(30);
		statesTablePN.getColumnModel().getColumn(0).setMaxWidth(30);
		statesTablePN.getColumnModel().getColumn(1).setHeaderValue("State ID");
		statesTablePN.getColumnModel().getColumn(1).setPreferredWidth(50);
		statesTablePN.getColumnModel().getColumn(1).setMinWidth(50);
		statesTablePN.getColumnModel().getColumn(1).setMaxWidth(50);
		for(int i=0; i<places.size(); i++) {
			statesTablePN.getColumnModel().getColumn(i+2).setHeaderValue("p"+i);
			statesTablePN.getColumnModel().getColumn(i+2).setPreferredWidth(cellWidth);
			statesTablePN.getColumnModel().getColumn(i+2).setMinWidth(cellWidth);
			statesTablePN.getColumnModel().getColumn(i+2).setMaxWidth(cellWidth);
        }
		
		//TableRowSorter<TableModel> sorter  = new TableRowSorter<TableModel>(statesTable.getModel());
		//statesTable.setRowSorter(sorter);
        
		statesTablePN.setName("StatesTable");
		statesTablePN.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		StatesPlacesTableRenderer tableRendererPN = new StatesPlacesTableRenderer(statesTablePN);
		statesTablePN.setDefaultRenderer(Object.class, tableRendererPN);
		statesTablePN.setDefaultRenderer(Double.class, tableRendererPN);
		
    	statesTablePN.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
          	    		cellClickAction();
          	    }
          	 }
      	});
    	
    	statesTablePN.setRowSelectionAllowed(false);
    	
    	statesTablePN.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(statesTablePN, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
			
	    return result;
	}
	
	/**
	 * Tworzy panel przycisków bocznych.
	 * @return JPanel - panel
	 */
	@SuppressWarnings("UnusedAssignment")
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
		selectStateButton.setToolTipText("Sets selected state as the active one and changes number of tokens in\n"
				+ "net places according to values of the selected state.");
		selectStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/selectStateIcon.png"));
		selectStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
						JOptionPane.INFORMATION_MESSAGE);
			}

			Object[] options = {"Set new state", "Keep current state",};
			int n = JOptionPane.showOptionDialog(null,
							"Set all places of the net according to the selected (table row: "+(selected+1)+") state?",
							"Setting new state", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				tableModelPN.setSelected(selected);
				statesManager.setNetworkStatePN(selected);
				pn.repaintAllGraphPanels();
				tableModelPN.fireTableDataChanged();
				overlord.markNetChange();
			}
		});
		result.add(selectStateButton);
		
		JButton addNewStateButton = new JButton("<html>Add current<br/>&nbsp;&nbsp;net state</html>");
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setFocusPainted(false);
		addNewStateButton.setToolTipText("Create new state vector based on current net state (distribution of tokens in places)");
		addNewStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/addStateIcon.png"));
		addNewStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfo();
				return;
			}
			Object[] options = {"Add new state", "Cancel",};
			int n = JOptionPane.showOptionDialog(null,
							"Add current net state to states table?",
							"Adding new state", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.addCurrentStatePN();
				addLastStateToTable();
				tableModelPN.fireTableDataChanged();
			}
		});
		result.add(addNewStateButton);
		
		JButton addNewCleanStateButton = new JButton("<html>Create new<br/>state vector</html>");
		addNewCleanStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewCleanStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewCleanStateButton.setFocusPainted(false);
		addNewCleanStateButton.setToolTipText("Create new and clean state vector (all tokens values set to 0).");
		addNewCleanStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/addCleanState.png"));
		addNewCleanStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfo();
				return;
			}
			Object[] options = {"Add new state", "Cancel",};
			int n = JOptionPane.showOptionDialog(null,
							"Add clean net state to states table?",
							"Adding new clean state", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.addNewCleanStatePN();
				addLastStateToTable();
				tableModelPN.fireTableDataChanged();
			}
		});
		result.add(addNewCleanStateButton);
		
		JButton replaceStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;Replace&nbsp;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;state&nbsp;</html>");
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setFocusPainted(false);
		replaceStateButton.setToolTipText("Replace the values of the selected state from the table with the current net places values.");
		replaceStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/replaceStateIcon.png"));
		replaceStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
						JOptionPane.INFORMATION_MESSAGE);
			}
			replaceStateAction();
		});
		result.add(replaceStateButton);
		
		JButton removeStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;Remove&nbsp;&nbsp;<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;state&nbsp;&nbsp;</html>");
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setFocusPainted(false);
		removeStateButton.setToolTipText("Removes state vector from project data.");
		removeStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/removeStateIcon.png"));
		removeStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
						JOptionPane.INFORMATION_MESSAGE);
			}
			removeStateAction();
		});
		result.add(removeStateButton);
		
		JButton editStateButton = new JButton("<html>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Edit&nbsp;&nbsp;&nbsp;&nbsp;<br/>state vector</html>");
		editStateButton.setBounds(posXda, posYda+=50, 130, 40);
		editStateButton.setMargin(new Insets(0, 0, 0, 0));
		editStateButton.setFocusPainted(false);
		editStateButton.setIcon(Tools.getResIcon32("/icons/stateManager/stateEdit.png"));
		editStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected > -1) {
				new HolmesStatesEditor((HolmesStatesManager)ego, statesManager.getStatePN(selected), selected);
			} else {
				JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
						JOptionPane.INFORMATION_MESSAGE);
			}

		});
		result.add(editStateButton);
		
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
		int selected = statesTablePN.getSelectedRow();
		int states = statesManager.accessStateMatrix().size();
		if(states == 1) {
			JOptionPane.showMessageDialog(null, "At least one net state must remain!", 
					"Warning",JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		Object[] options = {"Remove state", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Remove selected state (table row: "+(selected+1)+") from the states table?",
						"Removing state", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		statesManager.removeStatePN(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Zastępowanie stanu z tabeli, aktualnym stanem sieci.
	 */
	private void replaceStateAction() {
		int selected = statesTablePN.getSelectedRow();
		Object[] options = {"Replace state", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Replace selected state (table row: "+(selected+1)+") with the current net state?",
						"Replacing state", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		statesManager.replaceStoredStateWithNetStatePN(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Zmienia komórkę tabeli, wywoływane z poziomu okna edytora stanów.
	 * @param index int - nr wiersza
	 * @param placeID int - nr kolumny
	 * @param newValue double - liczba tokenów
	 * @param update boolean - jeśli true, odświeża tablicę
	 */
	public void changeTableCell(int index, int placeID, double newValue, boolean update) {
		tableModelPN.setQuietlyValueAt(newValue, index, placeID);
		if(update)
			tableModelPN.fireTableDataChanged();
	}
	
	/**
	 * Metoda dodaje ostatni stan z listy stanów do tabeli - potrzeba do akcji przycisku dodającego
	 * aktualny stan sieci do tabeli stanów.
	 */
	private void addLastStateToTable() {
		int states = statesManager.accessStateMatrix().size();
		StatePlacesVector psVector = statesManager.getStatePN(states-1);
		
		ArrayList<String> rowVector = new ArrayList<>();
		
		rowVector.add("");
		rowVector.add("m0("+(states)+")");
		for(int p=0; p<psVector.getSize(); p++) {
			rowVector.add(""+psVector.getTokens(p));
    	}
		tableModelPN.addNew(rowVector);
		overlord.markNetChange();
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTable() {
		tableModelPN.clearModel(places.size());
    	for(int row=0; row<statesManager.accessStateMatrix().size(); row++) {
    		ArrayList<String> rowVector = new ArrayList<>();
    		
    		if(statesManager.selectedStatePN == row)
    			rowVector.add("X");
    		else
    			rowVector.add("");
    		
    		StatePlacesVector psVector = statesManager.getStatePN(row);
    		rowVector.add("m0("+(row+1)+")");
    		
    		for(int p=0; p<psVector.getSize(); p++) {
    			rowVector.add(""+psVector.getTokens(p));
        	}
    		tableModelPN.addNew(rowVector);
    	}
    	
		tableModelPN.fireTableDataChanged();
	}
	
	/**
	 * Tworzy panel dolny z informacjami o stanie.
	 * @return (<b>JPanel</b>) panel.
	 */
	public JPanel getBottomPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createTitledBorder("State description:"));
		result.setPreferredSize(new Dimension(900, 150));

		int posXda = 10;
		int posYda = 15;
		
		//JLabel label0 = new JLabel("State description:");
		//label0.setBounds(posXda, posYda, 140, 20);
		//result.add(label0);
		
		stateDescrTextAreaPN = new JTextArea();
		stateDescrTextAreaPN.setLineWrap(true);
		stateDescrTextAreaPN.setEditable(true);
		stateDescrTextAreaPN.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		statesManager.setStateDescriptionPN(selectedRow, newComment);
            	}
            }
        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(stateDescrTextAreaPN), BorderLayout.CENTER);
        CreationPanel.setBounds(posXda, posYda+25, 600, 100);
        result.add(CreationPanel, BorderLayout.CENTER);

		
	    return result;
	}
	
	/**
	 * Metoda wywoływana przez akcję renderera tablicy, gdy następuje zmiana w komórce.
	 * @param row (<b>int</b>) nr wiersza tablicy.
	 * @param column (<b>int</b>) nr kolumny tablicy.
	 * @param value (<b>double</b>) - nowa wartość.
	 */
	public void changeState(int row, int column, double value) {
		statesManager.getStatePN(row).accessVector().set(column-2, value);
		overlord.markNetChange();
		tableModelPN.fireTableDataChanged();
	}
	
	/**
	 * Ustawia pole opisy wybranego stanu.
	 */
	public void fillDescriptionField() {
		String description = statesManager.getStateDescriptionPN(selectedRow);
		stateDescrTextAreaPN.setText(description);
	}
	
	/**
	 * Metoda obsługująca kliknięcie dowolnej komórki.
	 */
	protected void cellClickAction() {
		try {
			selectedRow = statesTablePN.getSelectedRow();
			fillDescriptionField();
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log("Error (581229108) | Exception:  "+ex.getMessage(), "error", false);
		}
	}



	//******************************************************************************
	//******************************************************************************
	//********************************    XTPN    **********************************
	//******************************************************************************
	//******************************************************************************



	/**
	 * Tworzy panel główny tablicy stanów XTPN.
	 * @return (<b>Panel</b>) - panel.
	 */
	public JPanel getMainTablePanelXTPN() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("p-states table"));
		result.setPreferredSize(new Dimension(500, 500));

		tableModelXTPN = new StatesPlacesTableModelXTPN(places.size(), this);
		statesTableXTPN = new RXTable(tableModelXTPN);
		((RXTable)statesTableXTPN).setSelectAllForEdit(true);

		statesTableXTPN.getColumnModel().getColumn(0).setHeaderValue("Sel:");
		statesTableXTPN.getColumnModel().getColumn(0).setPreferredWidth(30);
		statesTableXTPN.getColumnModel().getColumn(0).setMinWidth(30);
		statesTableXTPN.getColumnModel().getColumn(0).setMaxWidth(30);
		statesTableXTPN.getColumnModel().getColumn(1).setHeaderValue("State ID");
		statesTableXTPN.getColumnModel().getColumn(1).setPreferredWidth(50);
		statesTableXTPN.getColumnModel().getColumn(1).setMinWidth(50);
		statesTableXTPN.getColumnModel().getColumn(1).setMaxWidth(50);
		for(int i=0; i<places.size(); i++) {
			statesTableXTPN.getColumnModel().getColumn(i+2).setHeaderValue("p"+i);
			statesTableXTPN.getColumnModel().getColumn(i+2).setPreferredWidth(cellWidth);
			statesTableXTPN.getColumnModel().getColumn(i+2).setMinWidth(cellWidth);
			statesTableXTPN.getColumnModel().getColumn(i+2).setMaxWidth(cellWidth);
		}

		statesTableXTPN.setName("XTPNStatesTable");
		statesTableXTPN.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		StatesPlacesTableRendererXTPN tableRendererXTPN = new StatesPlacesTableRendererXTPN(statesTableXTPN);
		statesTableXTPN.setDefaultRenderer(Object.class, tableRendererXTPN);
		statesTableXTPN.setDefaultRenderer(Double.class, tableRendererXTPN);
		statesTableXTPN.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					if(!e.isControlDown())
						cellClickActionXTPN();
				}
			}
		});

		statesTableXTPN.setRowSelectionAllowed(false);
		statesTableXTPN.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JScrollPane tableScrollPane = new JScrollPane(statesTableXTPN, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		result.add(tableScrollPane, BorderLayout.CENTER);
		return result;
	}

	/**
	 * Tworzy panel przycisków bocznych.
	 * @return JPanel - panel
	 */
	public JPanel getButtonsPanelXTPN() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Buttons"));
		result.setPreferredSize(new Dimension(150, 500));

		int posXda = 10;
		int posYda = 25;

		HolmesRoundedButton selectStateButton = new HolmesRoundedButton("<html><center>Restore<br>selected state</center></html>"
				, "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
		selectStateButton.setBounds(posXda, posYda, 130, 40);
		selectStateButton.setMargin(new Insets(0, 0, 0, 0));
		selectStateButton.setFocusPainted(false);
		selectStateButton.setToolTipText("<html>Use selected XTPN p-state as the active one and change number of tokens in<br>"
				+ "all net places according to values of the selected p-state.<html>");
		selectStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfoXTPN();
				return;
			}
			int selected = statesTableXTPN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			Object[] options = {"Set new state", "Keep current state",};
			int n = JOptionPane.showOptionDialog(null,
					"Set all places of the net according to the selected (table row: "+(selected+1)+") state?",
					"Setting new state", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				tableModelXTPN.setSelected(selected);
				boolean status = statesManager.replaceNetStateWithSelectedMultiset_M(selected);
				if(status) {
					pn.repaintAllGraphPanels();
					tableModelXTPN.fireTableDataChanged();
					overlord.markNetChange();
				} else {
					JOptionPane.showMessageDialog(this, "Selected state corrupted, operation cancelled.",
							"Error", JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		result.add(selectStateButton);

		HolmesRoundedButton addNewStateButton = new HolmesRoundedButton("<html><center>Store current<br>net state</center></html>"
				, "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setFocusPainted(false);
		addNewStateButton.setToolTipText("Store current XTPN net p-state as a state vector.");
		addNewStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfoXTPN();
				return;
			}
			Object[] options = {"Add new XTPN state", "Cancel",};
			int n = JOptionPane.showOptionDialog(null,
					"Add current XTPN net state to states table?",
					"New state", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.createNewMultiset_M_basedOnNet();
				addLastStateToTableXTPN();
				tableModelXTPN.fireTableDataChanged();
			}
		});
		result.add(addNewStateButton);

		HolmesRoundedButton addNewCleanStateButton = new HolmesRoundedButton("<html><center>Create clean<br>state</center></html>"
				, "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
		addNewCleanStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewCleanStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewCleanStateButton.setFocusPainted(false);
		addNewCleanStateButton.setToolTipText("Create new clean p-state vector (no tokens, clean multisets).");
		addNewCleanStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfoXTPN();
				return;
			}
			Object[] options = {"Add new XTPN state", "Cancel",};
			int n = JOptionPane.showOptionDialog(null,
					"Add clean XTPN net state to states table?",
					"New clean state", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.addNewCleanMultiset_M();
				addLastStateToTableXTPN();
				tableModelXTPN.fireTableDataChanged();
			}
		});
		result.add(addNewCleanStateButton);

		HolmesRoundedButton replaceStateButton = new HolmesRoundedButton("<html><center>Overwrite<br>selected state</center></html>"
				, "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setFocusPainted(false);
		replaceStateButton.setToolTipText("<html>Replace the values of the selected XTPN p-state from the table<br>with the current XTPN net p-state.</html>");
		replaceStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfoXTPN();
				return;
			}

			int selected = statesTableXTPN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				replaceStateActionXTPN();
			}
		});
		result.add(replaceStateButton);

		HolmesRoundedButton removeStateButton = new HolmesRoundedButton("<html><center>Remove stored<br>state</center></html>"
				, "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setFocusPainted(false);
		removeStateButton.setToolTipText("Removes XTPN p-state vector from the project data.");
		removeStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfoXTPN();
				return;
			}

			int selected = statesTableXTPN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				removeStateActionXTPN();
			}
		});
		result.add(removeStateButton);

		HolmesRoundedButton editStateButton = new HolmesRoundedButton("<html><center>XTPN<br>state editor</center></html>"
				, "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
		editStateButton.setBounds(posXda, posYda+50, 130, 40);
		editStateButton.setMargin(new Insets(0, 0, 0, 0));
		editStateButton.setFocusPainted(false);
		editStateButton.setToolTipText("Opens p-state editor window based on the currently selected p-state.");
		editStateButton.addActionListener(actionEvent -> {
			if(places.size() == 0) {
				noNetInfoXTPN();
				return;
			}
			int selected = statesTableXTPN.getSelectedRow();
			if(selected > -1)
				new HolmesStatesEditorXTPN((HolmesStatesManager)ego, statesManager.getMultiset_M(selected), selected);
			else {
				JOptionPane.showMessageDialog(ego, "Please click on any XTPN state row.",
						"No XTPN state selected", JOptionPane.WARNING_MESSAGE);
			}
		});
		result.add(editStateButton);
		return result;
	}

	/**
	 * Krótki komunikat, że nie ma sieci.
	 */
	private void noNetInfoXTPN() {
		JOptionPane.showMessageDialog(this, "There are no places in the net.",
				"No places", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Obsługa usuwania stanu sieci.
	 */
	private void removeStateActionXTPN() {
		int selected = statesTableXTPN.getSelectedRow();
		int states = statesManager.accessStateMatrixXTPN().size();
		if(states == 1) {
			JOptionPane.showMessageDialog(null, "At least one XTPN net state must remain.",
					"Warning",JOptionPane.WARNING_MESSAGE);
			return;
		}

		Object[] options = {"Remove XTPN state", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
				"Remove selected XTPN state (table row: "+(selected+1)+") from the states table?",
				"Remove XTPN state", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}

		statesManager.removeMultiset_M(selected);
		fillTableXTPN();
		overlord.markNetChange();
	}

	/**
	 * Zastępowanie stanu z tabeli, aktualnym stanem sieci.
	 */
	private void replaceStateActionXTPN() {
		int selected = statesTableXTPN.getSelectedRow();
		if(selected == -1) {
			JOptionPane.showMessageDialog(null, "Please select state from the table.", "Selection problem",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Object[] options = {"Replace XTPN state", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
				"Replace selected XTPN state (table row: "+(selected+1)+") with the current net state?",
				"Replace XTPN state", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}

		statesManager.replaceStoredMultiset_M_withCurrentNetState(selected);
		fillTableXTPN();
		overlord.markNetChange();
	}

	/**
	 * Zmienia komórkę tabeli, wywoływane z poziomu okna edytora stanów.
	 * @param index int - nr wiersza
	 * @param placeID int - nr kolumny
	 * @param newValue double - liczba tokenów
	 * @param update boolean - jeśli true, odświeża tablicę
	 */
	@SuppressWarnings("unused")
	public void changeTableCellXTPN(int index, int placeID, double newValue, boolean update) {
		tableModelXTPN.setQuietlyValueAt(newValue, index, placeID);
		if(update)
			tableModelXTPN.fireTableDataChanged();
	}

	/**
	 * Metoda dodaje ostatni stan z listy stanów do tabeli - potrzeba do akcji przycisku dodającego
	 * aktualny stan sieci do tabeli stanów.
	 */
	private void addLastStateToTableXTPN() {
		int states = statesManager.accessStateMatrixXTPN().size();
		MultisetM multisetM = statesManager.getMultiset_M(states-1);
		ArrayList<String> rowVector = new ArrayList<>();

		rowVector.add("");
		rowVector.add("m0("+(states)+")");
		for(int p = 0; p<multisetM.getMultiset_M_Size(); p++) {
			if(multisetM.isPlaceStoredAsGammaActive(p)) {
				int value = multisetM.accessMultiset_K(p).size();
				rowVector.add(""+value);
			} else {
				double value = multisetM.accessMultiset_K(p).get(0);
				rowVector.add((int)value+" (C)");
			}
		}
		tableModelXTPN.addNew(rowVector);
		overlord.markNetChange();
	}

	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTableXTPN() {
		tableModelXTPN.clearModel(places.size());
		for(int row=0; row<statesManager.accessStateMatrixXTPN().size(); row++) {
			ArrayList<String> rowVector = new ArrayList<>();

			if(statesManager.selectedStateXTPN == row)
				rowVector.add("X");
			else
				rowVector.add("");

			MultisetM multisetM = statesManager.getMultiset_M(row);
			rowVector.add("m0("+(row+1)+")");

			for(int placeIndex = 0; placeIndex<multisetM.getMultiset_M_Size(); placeIndex++) {
				if( multisetM.isPlaceStoredAsGammaActive(placeIndex) ) {
					int value = multisetM.accessMultiset_K(placeIndex).size();
					rowVector.add(""+value);
				}
				else {
					double value = multisetM.accessMultiset_K(placeIndex).get(0);
					rowVector.add((int)value+" (C)");
				}

			}
			tableModelXTPN.addNew(rowVector);
		}

		tableModelXTPN.fireTableDataChanged();
	}

	/**
	 * Tworzy panel dolny z informacjami o stanie.
	 * @return JPanel - panel
	 */
	public JPanel getBottomPanelXTPN() {
		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createTitledBorder("XTPN state description:"));
		result.setPreferredSize(new Dimension(900, 150));

		int posXda = 10;
		int posYda = 15;

		stateDescrTextAreaXTPN = new JTextArea();
		stateDescrTextAreaXTPN.setLineWrap(true);
		stateDescrTextAreaXTPN.setEditable(true);
		stateDescrTextAreaXTPN.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				JTextArea field = (JTextArea) e.getSource();
				if(field != null) {
					String newComment = field.getText();
					statesManager.setMultiset_M_Description(selectedRow, newComment);
				}
			}
		});

		JPanel CreationPanel = new JPanel();
		CreationPanel.setLayout(new BorderLayout());
		CreationPanel.add(new JScrollPane(stateDescrTextAreaXTPN), BorderLayout.CENTER);
		CreationPanel.setBounds(posXda, posYda+25, 600, 100);
		result.add(CreationPanel, BorderLayout.CENTER);
		return result;
	}

	/**
	 * Metoda wywoływana przez akcję renderera tablicy, gdy następuje zmiana w komórce.
	 * @param row int - nr wiersza tablicy.
	 * @param column int - nr kolumny tablicy.
	 * @param value double - nowa wartość.
	 *     NIEUŻYWANA, WSZYSTKIE KOLUMNY READ ONLY!
	 */
	@SuppressWarnings("unused")
	public void changeStateXTPN(int row, int column, double value) {
		tableModelXTPN.fireTableDataChanged();
	}

	/**
	 * Ustawia pole opisy wybranego stanu.
	 */
	public void fillDescriptionFieldXTPN() {
		String description = statesManager.getMultiset_M_Description(selectedRow);
		stateDescrTextAreaXTPN.setText(description);
	}

	/**
	 * Metoda obsługująca kliknięcie dowolnej komórki.
	 */
	protected void cellClickActionXTPN() {
		try {
			selectedRow = statesTableXTPN.getSelectedRow();
			fillDescriptionFieldXTPN();
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log("Error (164166738) | Exception:  "+ex.getMessage(), "error", false);
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
