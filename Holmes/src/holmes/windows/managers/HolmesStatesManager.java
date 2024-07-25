package holmes.windows.managers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.*;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.*;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
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
import holmes.windows.xtpn.managers.HolmesStatesEditorXTPN;

/**
 * Klasa tworząca okno managera stanów początkowych (m0) sieci.
 */
public class HolmesStatesManager extends JFrame {
	@Serial
	private static final long serialVersionUID = -4590055483268695118L;
	private final JFrame ego;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
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
		setTitle(lang.getText("HSMwin_entry001title"));
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00530exception")+" "+ex.getMessage(), "error", true);
		}
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
		tabbedPane.addTab(lang.getText("HSMwin_entry002"), Tools.getResIcon22("/icons/stateManager/PNtab.png"), //PN p-states 
				main, lang.getText("HSMwin_entry002t")); //
		tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

		JPanel mainXTPN = new JPanel(new BorderLayout());
		JPanel submainXTPN = new JPanel(new BorderLayout());
		JPanel tablePanelXTPN = getMainTablePanelXTPN();
		submainXTPN.add(tablePanelXTPN, BorderLayout.CENTER);
		submainXTPN.add(getBottomPanelXTPN(), BorderLayout.SOUTH);
		mainXTPN.add(submainXTPN, BorderLayout.CENTER);
		mainXTPN.add(getButtonsPanelXTPN(), BorderLayout.EAST);

		//tabbedPane.addTab("XTPN", mainXTPN);
		tabbedPane.addTab(lang.getText("HSMwin_entry003"), Tools.getResIcon22("/icons/stateManager/XTPNtab.png") //XTPN p-states
				, mainXTPN, lang.getText("HSMwin_entry003t"));
		tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
		add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.addChangeListener(e -> {
			int selected = ((JTabbedPane)e.getSource()).getSelectedIndex();
			if(selected == 1 && GUIController.access().getCurrentNetType() != PetriNet.GlobalNetType.XTPN ) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry004"),
						lang.getText("problem"), JOptionPane.INFORMATION_MESSAGE);
				((JTabbedPane)e.getSource()).setSelectedIndex(0);
			} else if(selected == 0 && GUIController.access().getCurrentNetType() == PetriNet.GlobalNetType.XTPN) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry005"),
						lang.getText("problem"), JOptionPane.INFORMATION_MESSAGE);
				((JTabbedPane) e.getSource()).setSelectedIndex(1);
			}
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
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSMwin_entry006"))); //p-states table
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
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSMwin_entry007"))); //Buttons
		result.setPreferredSize(new Dimension(150, 500));

		int posXda = 10;
		int posYda = 25;

		JButton selectStateButton = new JButton(lang.getText("HSMwin_entry008")); //Set net state
		selectStateButton.setBounds(posXda, posYda, 130, 40);
		selectStateButton.setMargin(new Insets(0, 0, 0, 0));
		selectStateButton.setFocusPainted(false);
		selectStateButton.setToolTipText(lang.getText("HSMwin_entry008t"));
		selectStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/selectStateIcon.png"));
		selectStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry009")
						, lang.getText("problem"), JOptionPane.INFORMATION_MESSAGE);
			}

			Object[] options = {lang.getText("HSMwin_entry010op1"), lang.getText("HSMwin_entry010op2"),}; //Set new state, Keep current state
			String strB = String.format(lang.getText("HSMwin_entry010"), (selected+1));
			int n = JOptionPane.showOptionDialog(null,
					strB, lang.getText("HSMwin_entry010t"), JOptionPane.YES_NO_OPTION,
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
		
		JButton addNewStateButton = new JButton(lang.getText("HSMwin_entry011")); //Add new state
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setFocusPainted(false);
		addNewStateButton.setToolTipText(lang.getText("HSMwin_entry011t"));
		addNewStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/addStateIcon.png"));
		addNewStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			Object[] options = {lang.getText("HSMwin_entry012op1"), lang.getText("HSMwin_entry012op2"),}; //Add new state, Cancel
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSMwin_entry012"),
							lang.getText("HSMwin_entry012t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.addCurrentStatePN();
				addLastStateToTable();
				tableModelPN.fireTableDataChanged();
			}
		});
		result.add(addNewStateButton);
		
		JButton addNewCleanStateButton = new JButton(lang.getText("HSMwin_entry013")); //Create new state vector
		addNewCleanStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewCleanStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewCleanStateButton.setFocusPainted(false);
		addNewCleanStateButton.setToolTipText(lang.getText("HSMwin_entry013t"));
		addNewCleanStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/addCleanState.png"));
		addNewCleanStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			Object[] options = {lang.getText("HSMwin_entry014op1"), lang.getText("HSMwin_entry014op2"),}; //Add new state, Cancel
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSMwin_entry014"),
							lang.getText("HSMwin_entry014t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.addNewCleanStatePN();
				addLastStateToTable();
				tableModelPN.fireTableDataChanged();
			}
		});
		result.add(addNewCleanStateButton);
		
		JButton replaceStateButton = new JButton(lang.getText("HSMwin_entry015")); //Replace state
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setFocusPainted(false);
		replaceStateButton.setToolTipText(lang.getText("HSMwin_entry015t"));
		replaceStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/replaceStateIcon.png"));
		replaceStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry016"), lang.getText("problem"),
						JOptionPane.INFORMATION_MESSAGE);
			}
			replaceStateAction();
		});
		result.add(replaceStateButton);
		
		JButton removeStateButton = new JButton(lang.getText("HSMwin_entry017")); //Remove state
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setFocusPainted(false);
		removeStateButton.setToolTipText(lang.getText("HSMwin_entry017t"));
		removeStateButton.setIcon(Tools.getResIcon16("/icons/stateManager/removeStateIcon.png"));
		removeStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry018"), lang.getText("problem"),
						JOptionPane.INFORMATION_MESSAGE);
			}
			removeStateAction();
		});
		result.add(removeStateButton);
		
		JButton editStateButton = new JButton(lang.getText("HSMwin_entry019")); //Edit state
		editStateButton.setBounds(posXda, posYda+=50, 130, 40);
		editStateButton.setMargin(new Insets(0, 0, 0, 0));
		editStateButton.setFocusPainted(false);
		editStateButton.setIcon(Tools.getResIcon32("/icons/stateManager/stateEdit.png"));
		editStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			int selected = statesTablePN.getSelectedRow();
			if(selected > -1) {
				new HolmesStatesEditor((HolmesStatesManager)ego, statesManager.getStatePN(selected), selected);
			} else {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry020"), lang.getText("problem"),
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
		JOptionPane.showMessageDialog(this, lang.getText("HSMwin_entry021"), 
				lang.getText("problem"), JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Obsługa usuwania stanu sieci.
	 */
	private void removeStateAction() {
		int selected = statesTablePN.getSelectedRow();
		int states = statesManager.accessStateMatrix().size();
		if(states == 1) {
			JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry022"), 
					lang.getText("warning"),JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		Object[] options = {lang.getText("HSMwin_entry023op1"), lang.getText("HSMwin_entry023op2"),}; //Remove state, Cancel
		String str = String.format(lang.getText("HSMwin_entry023"), (selected+1));
		int n = JOptionPane.showOptionDialog(null,
				str, lang.getText("HSMwin_entry023t"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
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
		Object[] options = {lang.getText("HSMwin_entry024op1"), lang.getText("HSMwin_entry024op2"),}; //Replace state, Cancel
		String strB = String.format(lang.getText("HSMwin_entry024"), (selected+1));
		int n = JOptionPane.showOptionDialog(null,
				strB, lang.getText("HSMwin_entry024t"), JOptionPane.YES_NO_OPTION,
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
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSMwin_entry025"))); //State description:
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
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00531exception")+" "+ex.getMessage(), "error", true);
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
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSMwin_entry026title")));
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
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSMwin_entry027"))); //Buttons
		result.setPreferredSize(new Dimension(150, 500));

		int posXda = 10;
		int posYda = 25;

		HolmesRoundedButton selectStateButton = new HolmesRoundedButton(lang.getText("HSMwin_entry028") //Set net state
				, "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
		selectStateButton.setBounds(posXda, posYda, 130, 40);
		selectStateButton.setMargin(new Insets(0, 0, 0, 0));
		selectStateButton.setFocusPainted(false);
		selectStateButton.setToolTipText(lang.getText("HSMwin_entry028t"));
		selectStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfoXTPN();
				return;
			}
			int selected = statesTableXTPN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry029"), lang.getText("HSMwin_entry029t"),
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			Object[] options = {lang.getText("HSMwin_entry030op1"), lang.getText("HSMwin_entry030op2"),}; //Set new state, Keep current state
			String strB = String.format(lang.getText("HSMwin_entry030"), (selected+1));
			int n = JOptionPane.showOptionDialog(null,
					strB, lang.getText("HSMwin_entry030t"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				tableModelXTPN.setSelected(selected);
				boolean status = statesManager.replaceNetStateWithSelectedMultiset_M(selected);
				if(status) {
					pn.repaintAllGraphPanels();
					tableModelXTPN.fireTableDataChanged();
					overlord.markNetChange();
				} else {
					JOptionPane.showMessageDialog(this, lang.getText("HSMwin_entry031"),
							lang.getText("error"), JOptionPane.ERROR_MESSAGE);
				}

			}
		});
		result.add(selectStateButton);

		HolmesRoundedButton addNewStateButton = new HolmesRoundedButton(lang.getText("HSMwin_entry032") //Add new state
				, "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setFocusPainted(false);
		addNewStateButton.setToolTipText(lang.getText("HSMwin_entry032t"));
		addNewStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfoXTPN();
				return;
			}
			Object[] options = {lang.getText("HSMwin_entry033op1"), lang.getText("HSMwin_entry033op2"),}; //Add new XTPN state, Cancel
			int n = JOptionPane.showOptionDialog(null,
					lang.getText("HSMwin_entry033"),
					lang.getText("HSMwin_entry033t"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.createNewMultiset_M_basedOnNet();
				addLastStateToTableXTPN();
				tableModelXTPN.fireTableDataChanged();
			}
		});
		result.add(addNewStateButton);

		HolmesRoundedButton addNewCleanStateButton = new HolmesRoundedButton(lang.getText("HSMwin_entry034") //Create new state vector
				, "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
		addNewCleanStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewCleanStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewCleanStateButton.setFocusPainted(false);
		addNewCleanStateButton.setToolTipText(lang.getText("HSMwin_entry034t"));
		addNewCleanStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfoXTPN();
				return;
			}
			Object[] options = {lang.getText("HSMwin_entry035op1"), lang.getText("HSMwin_entry035op2"),}; //Add new XTPN state, Cancel
			int n = JOptionPane.showOptionDialog(null,
					lang.getText("HSMwin_entry035"),
					lang.getText("HSMwin_entry035t"), JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				statesManager.addNewCleanMultiset_M();
				addLastStateToTableXTPN();
				tableModelXTPN.fireTableDataChanged();
			}
		});
		result.add(addNewCleanStateButton);

		HolmesRoundedButton replaceStateButton = new HolmesRoundedButton(lang.getText("HSMwin_entry036") //Overwrite selected state
				, "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setFocusPainted(false);
		replaceStateButton.setToolTipText(lang.getText("HSMwin_entry036t"));
		replaceStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfoXTPN();
				return;
			}

			int selected = statesTableXTPN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry037"), lang.getText("problem"),
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				replaceStateActionXTPN();
			}
		});
		result.add(replaceStateButton);

		HolmesRoundedButton removeStateButton = new HolmesRoundedButton(lang.getText("HSMwin_entry038") //Remove stored state
				, "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setFocusPainted(false);
		removeStateButton.setToolTipText(lang.getText("HSMwin_entry038t"));
		removeStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfoXTPN();
				return;
			}

			int selected = statesTableXTPN.getSelectedRow();
			if(selected == -1) {
				JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry039"), lang.getText("problem"),
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				removeStateActionXTPN();
			}
		});
		result.add(removeStateButton);

		HolmesRoundedButton editStateButton = new HolmesRoundedButton(lang.getText("HSMwin_entry040") //XTNP state editor
				, "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
		editStateButton.setBounds(posXda, posYda+50, 130, 40);
		editStateButton.setMargin(new Insets(0, 0, 0, 0));
		editStateButton.setFocusPainted(false);
		editStateButton.setToolTipText(lang.getText("HSMwin_entry041"));
		editStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfoXTPN();
				return;
			}
			int selected = statesTableXTPN.getSelectedRow();
			if(selected > -1)
				new HolmesStatesEditorXTPN((HolmesStatesManager)ego, statesManager.getMultiset_M(selected), selected);
			else {
				JOptionPane.showMessageDialog(ego, lang.getText("HSMwin_entry042"),
						lang.getText("HSMwin_entry042t"), JOptionPane.WARNING_MESSAGE);
			}
		});
		result.add(editStateButton);
		return result;
	}

	/**
	 * Krótki komunikat, że nie ma sieci.
	 */
	private void noNetInfoXTPN() {
		JOptionPane.showMessageDialog(this, lang.getText("HSMwin_entry043"),
				lang.getText("error"), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Obsługa usuwania stanu sieci.
	 */
	private void removeStateActionXTPN() {
		int selected = statesTableXTPN.getSelectedRow();
		int states = statesManager.accessStateMatrixXTPN().size();
		if(states == 1) {
			JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry044"),
					lang.getText("warning"),JOptionPane.WARNING_MESSAGE);
			return;
		}

		Object[] options = {lang.getText("HSMwin_entry045op1"), lang.getText("HSMwin_entry045op2"),}; //Remove XTPN state, Cancel
		String str = String.format(lang.getText("HSMwin_entry045"), (selected+1));
		int n = JOptionPane.showOptionDialog(null,
				str, lang.getText("HSMwin_entry045t"), JOptionPane.YES_NO_OPTION,
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
			JOptionPane.showMessageDialog(null, lang.getText("HSMwin_entry046"), lang.getText("problem"),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Object[] options = {lang.getText("HSMwin_entry047op1"), lang.getText("HSMwin_entry047op2"),}; //Replace XTPN state, Cancel
		String strB = String.format(lang.getText("HSMwin_entry047"), (selected+1));
		int n = JOptionPane.showOptionDialog(null,
				strB, lang.getText("HSMwin_entry047t"), JOptionPane.YES_NO_OPTION,
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
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSMwin_entry048")));
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
			if(selectedRow > -1)
				return;

			fillDescriptionFieldXTPN();
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00532exception")+" "+ex.getMessage(), "error", true);
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
