package holmes.windows.managers;

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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.StatePlacesVector;
import holmes.petrinet.data.StatePlacesManager;
import holmes.petrinet.elements.Place;
import holmes.tables.RXTable;
import holmes.tables.managers.StatesPlacesEditorTableModel;
import holmes.utilities.Tools;

public class HolmesStatesEditor extends JFrame {
	private static final long serialVersionUID = -2088768019289555918L;
	private GUIManager overlord;
	private HolmesStatesManager parentWindow;
	private DefaultTableCellRenderer tableRenderer;
	private StatesPlacesEditorTableModel tableModel;
	private JTable table;
	private JPanel tablePanel;
	private StatePlacesVector stateVector;
	private int stateIndex;
	private JTextArea vectorDescrTextArea;
	
	private ArrayList<Place> places;
	private PetriNet pn;
	private StatePlacesManager statesManager;
	
	private long globalTokensNumber = 0;
	
	/**
	 * Główny konstruktor okna edycji stanu sieci.
	 * @param parent HolmesStatesManager - okno wywołujące
	 * @param stateVector StatePlacesVector - wektor SSA
	 * @param stateIndex int - indeks powyższego wektora w tablicy
	 */
	public HolmesStatesEditor(HolmesStatesManager parent, StatePlacesVector stateVector, int stateIndex) {
		setTitle("Holmes state editor");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {
			
		}
    	this.overlord = GUIManager.getDefaultGUIManager();
    	this.pn = overlord.getWorkspace().getProject();
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
		
		tablePanel = getMainTablePanel();
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
		result.setBorder(BorderFactory.createTitledBorder("State vector data"));
		result.setPreferredSize(new Dimension(500, 100));
		
		JPanel filler = new JPanel(null);

		int posX = 5;
		int posY = 0;
		
		JLabel label0 = new JLabel("State vector ID: ");
		label0.setBounds(posX, posY, 100, 20);
		filler.add(label0);
		
		JLabel labelID = new JLabel(stateIndex+"");
		labelID.setBounds(posX+110, posY, 100, 20);
		filler.add(labelID);
		
		vectorDescrTextArea = new JTextArea(statesManager.accessStateMatrix().get(stateIndex).getDescription());
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
        
        JButton changeAllButton = new JButton("<html>&nbsp;Set tokens&nbsp;<br>in all places</html>");
        changeAllButton.setBounds(posX+620, posY, 120, 40);
        changeAllButton.setMargin(new Insets(0, 0, 0, 0));
		changeAllButton.setFocusPainted(false);
		changeAllButton.setToolTipText("Sets same number of tokens in all places.");
		changeAllButton.setIcon(Tools.getResIcon16("/icons/stateManager/changeAll.png"));
		changeAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(places.size() == 0) {
					return;
				}
				
				changeGlobalTokensNumber();
			}
		});
		result.add(changeAllButton);
		
		JLabel locLabel = new JLabel("New tokens number:", JLabel.LEFT);
		locLabel.setBounds(posX+750, posY, 120, 20);
		result.add(locLabel);
		
		SpinnerModel tokensSpinnerModel = new SpinnerNumberModel(0, 0, Long.MAX_VALUE, 1);
		JSpinner tokensSpinner = new JSpinner(tokensSpinnerModel);
		tokensSpinner.setBounds(posX+750, posY+20, 120, 20);
		tokensSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double tokens = (double) ((JSpinner) e.getSource()).getValue();
				globalTokensNumber = (int) tokens;
			}
		});
		result.add(tokensSpinner);
		
        result.add(filler, BorderLayout.CENTER);
		return result;
	}
	
	/**
	 * Metoda zmienia liczbę tokenów w wektorze na podaną w oknie.
	 */
	protected void changeGlobalTokensNumber() {
		Object[] options = {"Change all", "Cancel",};
		int n = JOptionPane.showOptionDialog(null,
						"Change ALL current tokens in state to the new value: "+globalTokensNumber+"?",
						"Change whole state?", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, options, options[1]);
		if (n == 0) {
			int size = stateVector.getSize();
			for(int p=0; p<size; p++) {
				stateVector.setTokens(p, globalTokensNumber);
				tableModel.setQuietlyValueAt(globalTokensNumber, p, 2);
				
				if(p == size-1)
					parentWindow.changeTableCell(stateIndex, p+2, globalTokensNumber, true);
				else
					parentWindow.changeTableCell(stateIndex, p+2, globalTokensNumber, false);
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
		result.setBorder(BorderFactory.createTitledBorder("State vector table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new StatesPlacesEditorTableModel(this, stateIndex);
		table = new RXTable(tableModel);
		((RXTable)table).setSelectAllForEdit(true);
		
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
		tableRenderer = new DefaultTableCellRenderer();
		table.setDefaultRenderer(Object.class, tableRenderer);
		table.setDefaultRenderer(Double.class, tableRenderer);

		
		table.addMouseListener(new MouseAdapter() {
        	public void mouseClicked(MouseEvent e) {
          	    if (e.getClickCount() == 1) {
          	    	if(e.isControlDown() == false)
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
		statesManager.getState(index).accessVector().set(placeID, newValue);
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
