package holmes.windows.managers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SSAplacesManager;
import holmes.petrinet.data.SSAplacesVector;
import holmes.petrinet.data.SSAplacesVector.SSAdataType;
import holmes.petrinet.elements.Place;
import holmes.tables.RXTable;
import holmes.tables.managers.SSAplacesEditorTableModel;
import holmes.tables.managers.SSAplacesTableRenderer;
import holmes.utilities.Tools;

public class HolmesSSAplacesEditor extends JFrame {
	@Serial
	private static final long serialVersionUID = -6810858686209063022L;
	private GUIManager overlord;
	private JFrame parentWindow;
	private SSAplacesEditorTableModel tableModel;
	private SSAplacesVector ssaVector;
	private int ssaIndex;
	private SSAdataType dataType;
	private String dataTypeUnits;
	
	private ArrayList<Place> places;
	private SSAplacesManager ssaManager;
	
	/**
	 * Główny konstruktor okna menagera miejsc symulacji SSA.
	 * @param parent JFrame - okno wywołujące
	 * @param ssaVector SSAplacesVector - wektor SSA
	 * @param ssaIndex int - indeks powyższego wektora w tablicy
	 */
	public HolmesSSAplacesEditor(JFrame parent, SSAplacesVector ssaVector, int ssaIndex) {
		setTitle("Holmes SSA components editor");
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			GUIManager.getDefaultGUIManager().log("Error (343775994) | Exception:  "+ex.getMessage(), "error", false);
		}
    	this.overlord = GUIManager.getDefaultGUIManager();
		PetriNet pn = overlord.getWorkspace().getProject();
    	this.parentWindow = parent;
    	this.ssaVector = ssaVector;
    	this.ssaIndex = ssaIndex;
    	this.places = pn.getPlaces();
    	this.ssaManager = pn.accessSSAmanager();
    	this.dataType = ssaVector.getType();
    	
    	dataTypeUnits = "";
		if(dataType == SSAdataType.MOLECULES) {
			dataTypeUnits += " [number]";
		} else {
			dataTypeUnits += " [mole/litre]";
		}
		
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
		
		//int selectedState = firingRatesManager.selectedVector;
		int size = ssaVector.getSize();
    	for(int p=0; p<size; p++) {
    		tableModel.addNew(p, places.get(p).getName(), ssaVector.getTokens(p));
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
		result.setBorder(BorderFactory.createTitledBorder("SSA components vector data"));
		result.setPreferredSize(new Dimension(500, 100));
		
		JPanel filler = new JPanel(null);

		int posX = 5;
		int posY = 0;
		
		JLabel label0 = new JLabel("SSA vector ID: ");
		label0.setBounds(posX, posY, 100, 20);
		filler.add(label0);
		
		JLabel labelID = new JLabel(ssaIndex+"");
		labelID.setBounds(posX+110, posY, 100, 20);
		filler.add(labelID);
		
		JLabel label1 = new JLabel("Data vector type:");
		label1.setBounds(posX+220, posY, 100, 20);
		filler.add(label1);
		
		
		JLabel label2 = new JLabel(""+dataType+dataTypeUnits);
		label2.setBounds(posX+330, posY, 230, 20);
		filler.add(label2);

		JTextArea vectorDescrTextArea = new JTextArea(ssaManager.accessSSAmatrix().get(ssaIndex).getDescription());
		vectorDescrTextArea.setLineWrap(true);
		vectorDescrTextArea.setEditable(true);
		vectorDescrTextArea.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
            	JTextArea field = (JTextArea) e.getSource();
            	if(field != null) {
            		String newComment = field.getText();
            		ssaManager.accessSSAmatrix().get(ssaIndex).setDescription(newComment);
            		fillTable();
            	}
            }
        });
		
		
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(vectorDescrTextArea), BorderLayout.CENTER);
        CreationPanel.setBounds(posX, posY+=20, 600, 50);
        filler.add(CreationPanel);
        
        result.add(filler, BorderLayout.CENTER);
		return result;
	}
	
	/**
	 * Tworzy panel główny tablicy.
	 * @return JPanel - panel
	 */
	public JPanel getMainTablePanel() {
		JPanel result = new JPanel(new BorderLayout());
		result.setLocation(0, 0);
		result.setBorder(BorderFactory.createTitledBorder("SSA components table"));
		result.setPreferredSize(new Dimension(500, 500));
		
		tableModel = new SSAplacesEditorTableModel(this, ssaIndex, dataType);
		RXTable table = new RXTable(tableModel);
		table.setSelectAllForEdit(true);
		
		table.getColumnModel().getColumn(0).setHeaderValue("ID");
		table.getColumnModel().getColumn(0).setPreferredWidth(30);
		table.getColumnModel().getColumn(0).setMinWidth(30);
		table.getColumnModel().getColumn(0).setMaxWidth(30);
		table.getColumnModel().getColumn(1).setHeaderValue("Place name");
		table.getColumnModel().getColumn(1).setPreferredWidth(600);
		table.getColumnModel().getColumn(1).setMinWidth(100);
		table.getColumnModel().getColumn(2).setHeaderValue(dataTypeUnits);
		table.getColumnModel().getColumn(2).setPreferredWidth(100);
		table.getColumnModel().getColumn(2).setMinWidth(50);
        
		table.setName("SSAplacesTable");
		table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
		SSAplacesTableRenderer tableRenderer = new SSAplacesTableRenderer();
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
	 * Metoda ustawia nową wartość dla miejsca w SSA, wywoływana przez metodę TableModel która odpowiada za zmianę
	 * wartości pola value.
	 * @param index int - indeks wektora
	 * @param placeID int - indeks miejsca
	 * @param newValue double - nowa wartość SSA
	 */
	public void changeRealValue(int index, int placeID, double newValue) {
		ssaManager.getSSAvector(index).accessVector().set(placeID, newValue);
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
