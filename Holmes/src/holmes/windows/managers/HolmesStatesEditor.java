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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableCellRenderer;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.StatePlacesVector;
import holmes.petrinet.data.P_StateManager;
import holmes.petrinet.elements.Place;
import holmes.tables.RXTable;
import holmes.tables.managers.StatesPlacesEditorTableModel;
import holmes.utilities.Tools;

public class HolmesStatesEditor extends JFrame {
	@Serial
	private static final long serialVersionUID = -2088768019289555918L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private final HolmesStatesManager parentWindow;
	private StatesPlacesEditorTableModel tableModel;
	private final StatePlacesVector stateVector;
	private final int stateIndex;
	private final ArrayList<Place> places;
	private final P_StateManager statesManager;
	
	private long globalTokensNumber = 0;
	
	/**
	 * Główny konstruktor okna edycji stanu sieci.
	 * @param parent HolmesStatesManager - okno wywołujące
	 * @param stateVector StatePlacesVector - wektor SSA
	 * @param stateIndex int - indeks powyższego wektora w tablicy
	 */
	public HolmesStatesEditor(HolmesStatesManager parent, StatePlacesVector stateVector, int stateIndex) {
		setTitle(lang.getText("HSEwin_entry001title"));
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00529exception")+" "+ex.getMessage(), "error", true);
		}
		PetriNet pn = overlord.getWorkspace().getProject();
    	this.parentWindow = parent;
    	this.stateVector = stateVector;
    	this.stateIndex = stateIndex;
    	this.places = pn.getPlaces();
    	this.statesManager = pn.accessStatesManager();
    	
    	initalizeComponents();
    	initiateListeners();
    	fillTable();
    	setVisible(true);
    	parentWindow.setEnabled(false);
	}
	
	/**
	 * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
	 */
	private void fillTable() {
		tableModel.clearModel();
		int size = stateVector.getSize();
    	for(int p=0; p<size; p++) {
    		tableModel.addNew(p, places.get(p).getName(), stateVector.getTokens(p));
    	}
		tableModel.fireTableDataChanged();
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

		JPanel tablePanel = getMainTablePanel();
		add(getTopPanel(), BorderLayout.NORTH);
		add(tablePanel, BorderLayout.CENTER);
	}
	
	/**
	 * Buduje i zwraca panel górny okna.
	 * @return JPanel - panel
	 */
	private JPanel getTopPanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSEwin_entry002"))); //State vector data
		result.setPreferredSize(new Dimension(500, 100));
		
		JPanel filler = new JPanel(null);

		int posX = 5;
		int posY = 0;
		
		JLabel label0 = new JLabel(lang.getText("HSEwin_entry003")); //State vector ID
		label0.setBounds(posX, posY, 100, 20);
		filler.add(label0);
		
		JLabel labelID = new JLabel(stateIndex+"");
		labelID.setBounds(posX+110, posY, 100, 20);
		filler.add(labelID);

		JTextArea vectorDescrTextArea = new JTextArea(statesManager.accessStateMatrix().get(stateIndex).getDescription());
		vectorDescrTextArea.setLineWrap(true);
		vectorDescrTextArea.setEditable(true);
		vectorDescrTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		statesManager.accessStateMatrix().get(stateIndex).setDescription(newComment);
            		fillTable();
            		parentWindow.fillDescriptionField();
            	}
            }
        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(vectorDescrTextArea), BorderLayout.CENTER);
        CreationPanel.setBounds(posX, posY+=20, 600, 50);
        filler.add(CreationPanel);
        
        JButton changeAllButton = new JButton(lang.getText("HSEwin_entry004")); //Set tokens in all places
        changeAllButton.setBounds(posX+620, posY, 120, 40);
        changeAllButton.setMargin(new Insets(0, 0, 0, 0));
		changeAllButton.setFocusPainted(false);
		changeAllButton.setToolTipText(lang.getText("HSEwin_entry004t"));
		changeAllButton.setIcon(Tools.getResIcon16("/icons/stateManager/changeAll.png"));
		changeAllButton.addActionListener(actionEvent -> {
			if(places.isEmpty()) {
				return;
			}

			changeGlobalTokensNumber();
		});
		result.add(changeAllButton);
		
		JLabel locLabel = new JLabel(lang.getText("HSEwin_entry005"), JLabel.LEFT); //New tokens number
		locLabel.setBounds(posX+750, posY, 120, 20);
		result.add(locLabel);
		
		SpinnerModel tokensSpinnerModel = new SpinnerNumberModel(0, 0, Long.MAX_VALUE, 1);
		JSpinner tokensSpinner = new JSpinner(tokensSpinnerModel);
		tokensSpinner.setBounds(posX+750, posY+20, 120, 20);
		tokensSpinner.addChangeListener(e -> {
			double tokens = (double) ((JSpinner) e.getSource()).getValue();
			globalTokensNumber = (int) tokens;
		});
		result.add(tokensSpinner);
		
        result.add(filler, BorderLayout.CENTER);
		return result;
	}
	
	/**
	 * Metoda zmienia liczbę tokenów w wektorze na podaną w oknie.
	 */
	protected void changeGlobalTokensNumber() {
		Object[] options = {lang.getText("HSEwin_entry006op1"), lang.getText("HSEwin_entry006op2"),}; //Change all, Cancel
		int n = JOptionPane.showOptionDialog(null,
						lang.getText("HSEwin_entry006")+" "+globalTokensNumber+"?",
						lang.getText("HSEwin_entry006t"), JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 0) {
			int size = stateVector.getSize();
			for(int p=0; p<size; p++) {
				stateVector.setTokens(p, globalTokensNumber);
				tableModel.setQuietlyValueAt(globalTokensNumber, p, 2);
				parentWindow.changeTableCell(stateIndex, p+2, globalTokensNumber, ( p == size - 1 ) );
			}
			tableModel.fireTableDataChanged();
		}
	}

	/**
	 * Tworzy panel główny tablicy stanu sieci.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder(lang.getText("HSEwin_entry007"))); //State vector table
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new StatesPlacesEditorTableModel(this, stateIndex);
		RXTable table = new RXTable(tableModel);
		table.setSelectAllForEdit(true);
		
		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(1).setHeaderValue("Place name");
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(2).setHeaderValue("Tokens");
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(50);
        
		table.setName("SSAplacesTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();
		table.setDefaultRenderer(Object.class, tableRenderer);
		table.setDefaultRenderer(Double.class, tableRenderer);

		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(!e.isControlDown())
          	    		;	//cellClickAction();
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
	 * Metoda ustawia nową wartość dla miejsca w wektorze stanu, wywoływana przez metodę TableModel która odpowiada za zmianę
	 * wartości pola value.
	 * @param index int - indeks wektora
	 * @param placeID int - indeks miejsca
	 * @param newValue double - nowa wartość tokenów
	 */
	public void changeRealValue(int index, int placeID, double newValue) {
		statesManager.getStatePN(index).accessVector().set(placeID, newValue);
		parentWindow.changeTableCell(index, placeID+2, newValue, true);
		overlord.markNetChange();
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
