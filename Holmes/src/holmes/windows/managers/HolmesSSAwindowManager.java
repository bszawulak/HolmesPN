package holmes.windows.managers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SSAplacesManager;
import holmes.petrinet.data.SSAplacesVector;
import holmes.petrinet.data.SSAplacesVector.SSAdataType;
import holmes.petrinet.elements.Place;
import holmes.tables.managers.SSAplacesTableModel;
import holmes.tables.managers.SSAplacesTableRenderer;
import holmes.utilities.Tools;

/**
 * Klasa okna zarządzającego wektorami stanów symulatora SSA.
 * 
 * @author MR
 */
public class HolmesSSAwindowManager extends JFrame {
	@Serial
	private static final long serialVersionUID = 8184934957669150556L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private JFrame parentWindow;
	private JFrame ego;
	private SSAplacesTableModel tableModel;
	private JTable table;
	private JTextArea vectorDescrTextArea;
	
	private ArrayList<Place> places;
	private SSAplacesManager ssaManager;
	
	private boolean doNotUpdate = false;
	private JComboBox<String> typeCombo;
	
	private int selectedRow;
	
	/**
	 * Główny konstruktor okna menagera wektorów SSA.
	 */
	public HolmesSSAwindowManager(JFrame parent) {
		setTitle(lang.getText("HSSAWMwin_entry001title"));
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00527exception")+" "+ex.getMessage(), "error", true);
		}
		PetriNet pn = overlord.getWorkspace().getProject();
    	this.ego = this;
    	this.parentWindow = parent;
    	this.places = pn.getPlaces();
    	this.ssaManager = pn.accessSSAmanager();
    	this.selectedRow = 0;
    	
    	initalizeComponents();
    	initiateListeners();
    	fillTable();
    	setVisible(true);
    	parentWindow.setEnabled(false);
    	
    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Tworzy panel główny tablicy.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSAWMwin_entry002"))); //SSA vectors table
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new SSAplacesTableModel();
		table = new JTable(tableModel);
		
		table.getColumnModel().getColumn(0).setHeaderValue("Sel.");
		table.getColumnModel().getColumn(0).setPreferredWidth(40);
		table.getColumnModel().getColumn(0).setMinWidth(40);
		table.getColumnModel().getColumn(0).setMaxWidth(40);
		table.getColumnModel().getColumn(1).setHeaderValue("ID");
		table.getColumnModel().getColumn(1).setPreferredWidth(30);
		table.getColumnModel().getColumn(1).setMinWidth(30);
		table.getColumnModel().getColumn(1).setMaxWidth(30);
		table.getColumnModel().getColumn(2).setHeaderValue("Vector description");
		table.getColumnModel().getColumn(2).setMinWidth(50);
		table.getColumnModel().getColumn(3).setHeaderValue("Data type");
		table.getColumnModel().getColumn(3).setPreferredWidth(120);
		table.getColumnModel().getColumn(3).setMinWidth(120);
		table.getColumnModel().getColumn(3).setMaxWidth(120);
		table.getColumnModel().getColumn(4).setHeaderValue("Volume");
		table.getColumnModel().getColumn(4).setPreferredWidth(50);
		table.getColumnModel().getColumn(4).setMinWidth(50);
		table.getColumnModel().getColumn(4).setMaxWidth(50);
        
		table.setName("SSAvectorsTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		SSAplacesTableRenderer tableRenderer = new SSAplacesTableRenderer();
		table.setDefaultRenderer(Object.class, tableRenderer);
		table.setDefaultRenderer(String.class, tableRenderer);
		table.setDefaultRenderer(Double.class, tableRenderer);
		table.setDefaultRenderer(Integer.class, tableRenderer);
		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
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

		JPanel tablePanel = getMainTablePanel();
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
	@SuppressWarnings("unused")
	public void changeState(int row, int column, String value) {
		//TODO
		//firingRatesManager.accessFRMatrix().get(row).setDescription(value);
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
		
		JButton selectStateButton = new JButton(lang.getText("HSSAWMwin_entry003")); //Select SSA vector
		selectStateButton.setBounds(posXda, posYda, 130, 40);
		selectStateButton.setMargin(new Insets(0, 0, 0, 0));
		selectStateButton.setFocusPainted(false);
		selectStateButton.setIcon(Tools.getResIcon16("/icons/ssaWindow/selectSSAVectorIcon.png"));
		selectStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			int selected = table.getSelectedRow();
			if(selected == -1)
				return;

			Object[] options = {lang.getText("HSSAWMwin_entry004op1"), lang.getText("HSSAWMwin_entry004op2"),}; //Set new SSA values, Keep old ones
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSSAWMwin_entry004")+" "+selected+") ?",
							lang.getText("HSSAWMwin_entry004t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (n == 0) {
				ssaManager.setNetworkSSAvector(selected);
				tableModel.setSelected(selected);
				tableModel.fireTableDataChanged();
			}
		});
		result.add(selectStateButton);
		
		JButton addNewStateButton = new JButton(lang.getText("HSSAWMwin_entry005")); //Add current SSA values
		addNewStateButton.setBounds(posXda, posYda+=50, 130, 40);
		addNewStateButton.setMargin(new Insets(0, 0, 0, 0));
		addNewStateButton.setFocusPainted(false);
		addNewStateButton.setIcon(Tools.getResIcon16("/icons/ssaWindow/addSSAVectorIcon.png"));
		addNewStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			Object[] options = {lang.getText("HSSAWMwin_entry006op1"), lang.getText("HSSAWMwin_entry006op2"),}; //Add new SSA vector, Cancel
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSSAWMwin_entry006"),
							lang.getText("HSSAWMwin_entry006t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (n == 0) {
				ssaManager.addCurrentStateAsSSAvector();
				addLastStateToTable();
				tableModel.fireTableDataChanged();
			}
		});
		result.add(addNewStateButton);
		
		JButton replaceStateButton = new JButton(lang.getText("HSSAWMwin_entry007")); //Replace SSA vector
		replaceStateButton.setBounds(posXda, posYda+=50, 130, 40);
		replaceStateButton.setMargin(new Insets(0, 0, 0, 0));
		replaceStateButton.setFocusPainted(false);
		replaceStateButton.setIcon(Tools.getResIcon16("/icons/ssaWindow/replaceSSAVectorIcon.png"));
		replaceStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			replaceStateAction();
		});
		result.add(replaceStateButton);
		
		JButton removeStateButton = new JButton(lang.getText("HSSAWMwin_entry008")); //Remove SSA vector
		removeStateButton.setBounds(posXda, posYda+=50, 130, 40);
		removeStateButton.setMargin(new Insets(0, 0, 0, 0));
		removeStateButton.setFocusPainted(false);
		removeStateButton.setIcon(Tools.getResIcon16("/icons/ssaWindow/removeSSAVectorIcon.png"));
		removeStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			removeStateAction();
		});
		result.add(removeStateButton);
		
		JButton editStateButton = new JButton(lang.getText("HSSAWMwin_entry009")); //Edit SSA vector
		editStateButton.setBounds(posXda, posYda+=50, 130, 40);
		editStateButton.setMargin(new Insets(0, 0, 0, 0));
		editStateButton.setFocusPainted(false);
		editStateButton.setIcon(Tools.getResIcon32("/icons/ssaWindow/ssaEditor.png"));
		editStateButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				noNetInfo();
				return;
			}
			int selected = table.getSelectedRow();
			if(selected > -1)
				new HolmesSSAplacesEditor(ego, ssaManager.getSSAvector(selected), selected);
		});
		result.add(editStateButton);
		
	    return result;
	}
	
	/**
	 * Obsługa usuwania stanu sieci.
	 */
	private void removeStateAction() {
		int selected = table.getSelectedRow();
		int states = ssaManager.accessSSAmatrix().size();
		if(states == 1) {
			JOptionPane.showMessageDialog(null, lang.getText("HSSAWMwin_entry010"), 
					lang.getText("problem"),JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		Object[] options = {lang.getText("HSSAWMwin_entry011op1"), lang.getText("HSSAWMwin_entry011op2"),}; //Remove SSA vector, Cancel
		int n = JOptionPane.showOptionDialog(null,
						lang.getText("HSSAWMwin_entry011")+" "+selected+") ?",
						lang.getText("HSSAWMwin_entry011t"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		ssaManager.removeSSAvector(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Krótki komunikat, że nie ma sieci.
	 */
	private void noNetInfo() {
		JOptionPane.showMessageDialog(this, lang.getText("HSSAWMwin_entry012"), 
				lang.getText("problem"), JOptionPane.WARNING_MESSAGE);
	}
	
	/**
	 * Zastępowanie wektora SSA z tabeli, aktualnym stanem SSA sieci.
	 */
	private void replaceStateAction() {
		int selected = table.getSelectedRow();
		Object[] options = {lang.getText("HSSAWMwin_entry013op1"), lang.getText("HSSAWMwin_entry013op2"),}; //
		String strB = String.format(lang.getText("HSSAWMwin_entry013"), selected);
		int n = JOptionPane.showOptionDialog(null,
				strB,lang.getText("HSSAWMwin_entry013t"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 1) {
			return;
		}
		
		ssaManager.replaceSSAvectorWithNetState(selected);
		fillTable();
		overlord.markNetChange();
	}
	
	/**
	 * Metoda dodaje ostatni wektor SSA z listy stanów do tabeli - potrzeba do akcji przycisku dodającego
	 * aktualny stan sieci do tabeli stanów.
	 */
	private void addLastStateToTable() {
		int states = ssaManager.accessSSAmatrix().size();
		SSAplacesVector ssaV = ssaManager.getSSAvector(states-1);
		tableModel.addNew("", states-1, ssaV.getDescription(), ssaV.getType(), ssaV.getVolume());
		overlord.markNetChange();
	}

	/**
	 * Tworzy panel dolny z informacjami o wektorze SSA.
	 * @return JPanel - panel
	 */
	public JPanel getBottomPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSAWMwin_entry014"))); //Others
		result.setPreferredSize(new Dimension(900, 150));

		int posX = 10;
		int posY = 15;
		
		JLabel label0 = new JLabel(lang.getText("HSSAWMwin_entry015")); //State description
		label0.setBounds(posX, posY, 140, 20);
		result.add(label0);
		
		vectorDescrTextArea = new JTextArea();
		vectorDescrTextArea.setLineWrap(true);
		vectorDescrTextArea.setEditable(true);
		vectorDescrTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		ssaManager.setSSAvectorDescription(selectedRow, newComment);
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
        CreationPanel.setBounds(posX, posY+=25, 500, 100);
        result.add(CreationPanel);
        
        String[] data = new String[2];
        data[0] = "Molecules [number]";
        data[1] = "Concentration [mole^-1]";
		typeCombo = new JComboBox<String>(data);
		typeCombo.setBounds(posX+510, posY, 180, 20);
		
		if(ssaManager.getCurrentSSAvector().getType() == SSAdataType.MOLECULES)
			typeCombo.setSelectedIndex(0);
		else
			typeCombo.setSelectedIndex(1);
		
		typeCombo.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			JComboBox<String> comboBox = (JComboBox<String>)actionEvent.getSource();
			int mode = comboBox.getSelectedIndex();
			if(mode == 0) {
				ssaManager.getSSAvector(selectedRow).setType(SSAdataType.MOLECULES);
				tableModel.changeType(selectedRow, SSAdataType.MOLECULES);
			} else {
				ssaManager.getSSAvector(selectedRow).setType(SSAdataType.CONCENTRATION);
				tableModel.changeType(selectedRow, SSAdataType.CONCENTRATION);
			}
			tableModel.fireTableDataChanged();
		});

		result.add(typeCombo);
	    return result;
	}
	
	/**
	 * Ustawia pole opisu wybranego stanu.
	 */
	private void fillDescriptionField() {
		String description = ssaManager.accessSSAmatrix().get(selectedRow).getDescription();
		vectorDescrTextArea.setText(description);
	}
	
	/**
	 * Metoda obsługująca kliknięcie dowolnej komórki.
	 */
	protected void cellClickAction() {
		try {
            selectedRow = table.getSelectedRow();
			if(selectedRow == -1)
				return;

			doNotUpdate = true;
			fillDescriptionField();
			
			if(ssaManager.getSSAvector(selectedRow).getType() == SSAdataType.MOLECULES)
				typeCombo.setSelectedIndex(0);
			else
				typeCombo.setSelectedIndex(1);
			
			doNotUpdate = false;
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00528exception")+" "+ex.getMessage(), "error", true);
		}
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTable() {
		tableModel.clearModel();
		int selectedVector = ssaManager.selectedSSAvector;
    	for(int row=0; row<ssaManager.accessSSAmatrix().size(); row++) {
    		SSAplacesVector ssaV = ssaManager.getSSAvector(row);
    		if(row == selectedVector) {
    			tableModel.addNew("X", row, ssaV.getDescription(), ssaV.getType(), ssaV.getVolume());
    		} else {
    			tableModel.addNew("", row, ssaV.getDescription(), ssaV.getType(), ssaV.getVolume());
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
