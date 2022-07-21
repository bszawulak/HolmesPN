package holmes.windows.managers;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.P_StateManager;
import holmes.petrinet.data.StatePlacesVectorXTPN;
import holmes.petrinet.elements.Place;
import holmes.tables.RXTable;
import holmes.tables.managers.StatesPlacesEditorTableModelXTPN;
import holmes.utilities.Tools;
import holmes.windows.HolmesXTPNtokens;

public class HolmesStatesEditorXTPN extends JFrame {
    @Serial
    private static final long serialVersionUID = 3176765993380657329L;
    private HolmesStatesManager parentWindow;
    private HolmesStatesEditorXTPN ego;
    private RXTable multisetsTable;
    private StatesPlacesEditorTableModelXTPN tableModel;
    private StatePlacesVectorXTPN stateVectorXTPN;
    private int stateIndex;
    private ArrayList<Place> places;
    private P_StateManager statesManager;

    private boolean doNotListen;

    /**
     * Główny konstruktor okna edycji stanu sieci.
     * @param parent HolmesStatesManager - okno wywołujące
     * @param stateVector StatePlacesVector - wektor SSA
     * @param stateIndex int - indeks powyższego wektora w tablicy
     */
    public HolmesStatesEditorXTPN(HolmesStatesManager parent, StatePlacesVectorXTPN stateVector, int stateIndex) {
        setTitle("Holmes p-state editor XTPN");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ignored) {

        }
        doNotListen = true;
        GUIManager overlord = GUIManager.getDefaultGUIManager();
        PetriNet pn = overlord.getWorkspace().getProject();
        this.parentWindow = parent;
        ego = this;
        this.stateVectorXTPN = stateVector;
        this.stateIndex = stateIndex;
        this.places = pn.getPlaces();
        this.statesManager = pn.accessStatesManager();

        initalizeComponents();
        initiateListeners();
        fillTable();
        setVisible(true);
        //multisetsTable.setRowSelectionInterval(0, 0);
        parentWindow.setEnabled(false);
        doNotListen = false;
    }

    /**
     * Wypełnianie tabeli tokenami.
     */
    public void fillTable() {
        tableModel.clearModel();
        int size = stateVectorXTPN.getSize();
        for(int placeIndex=0; placeIndex<size; placeIndex++) {
            ArrayList<Double> multiset = stateVectorXTPN.accessMultisetK(placeIndex);
            StringBuilder line = new StringBuilder();
            if(multiset.size() == 0) { //jeśli nic nie ma w multizbiorze
                if(places.get(placeIndex).isGammaModeActiveXTPN()) {
                    line.append(" <empty> "); //puste miejsce XTPN
                } else { //liczba tokenów miejsca klasycznego:
                    line.append(places.get(placeIndex).getTokensNumber()+" <non-time place> ");
                }
            } else {
                for(Double d : multiset) {
                    line.append(d).append(" | ");
                }
            }
            tableModel.addNew(placeIndex, places.get(placeIndex).getName(), line.toString());
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

        JTextArea vectorDescrTextArea = new JTextArea(statesManager.accessStateMatrixXTPN().get(stateIndex).getDescription());
        vectorDescrTextArea.setLineWrap(true);
        vectorDescrTextArea.setEditable(true);
        vectorDescrTextArea.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                JTextArea field = (JTextArea) e.getSource();
                if(field != null) {
                    String newComment = field.getText();
                    statesManager.accessStateMatrixXTPN().get(stateIndex).setDescription(newComment);
                    fillTable();
                    parentWindow.fillDescriptionFieldXTPN();
                }
            }
        });

        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(vectorDescrTextArea), BorderLayout.CENTER);
        CreationPanel.setBounds(posX, posY+20, 600, 50);
        filler.add(CreationPanel);

        result.add(filler, BorderLayout.CENTER);
        return result;
    }

    /**
     * Tworzy panel główny tablicy stanu sieci.
     * @return JPanel - panel
     */
    public JPanel getMainTablePanel() {
        JPanel result = new JPanel(new BorderLayout());
        result.setLocation(0, 0);
        result.setBorder(BorderFactory.createTitledBorder("p-state vector table"));
        result.setPreferredSize(new Dimension(500, 500));

        tableModel = new StatesPlacesEditorTableModelXTPN(this, stateIndex);
        multisetsTable = new RXTable(tableModel);
        multisetsTable.setSelectAllForEdit(false);

        multisetsTable.getColumnModel().getColumn(0).setHeaderValue("ID");
        multisetsTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        multisetsTable.getColumnModel().getColumn(0).setMinWidth(30);
        multisetsTable.getColumnModel().getColumn(0).setMaxWidth(30);
        multisetsTable.getColumnModel().getColumn(1).setHeaderValue("Place name");
        multisetsTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        multisetsTable.getColumnModel().getColumn(1).setMinWidth(100);
        multisetsTable.getColumnModel().getColumn(2).setHeaderValue("Tokens");
        multisetsTable.getColumnModel().getColumn(2).setPreferredWidth(500);
        multisetsTable.getColumnModel().getColumn(2).setMinWidth(50);

        multisetsTable.setName("p-stateVectorTable");
        multisetsTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();
        multisetsTable.setDefaultRenderer(Integer.class, tableRenderer);
        multisetsTable.setDefaultRenderer(String.class, tableRenderer);

        multisetsTable.getSelectionModel().addListSelectionListener(event -> {
            if(!doNotListen)
                cellClickAction();
        });
        //z nieznanych mi powodów powyższy kod działa, poniższy za cholerę. Tj. gorzej: działa, ale dopiero od
        //drugiego kliknięcia w tabelę. Za pierszym tabele na selectedRow() zawsze zwraca -1...
        /*
        multisetsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if(!e.isControlDown()) {
                        //cellClickAction();
                    }
                }
            }
        });
         */

        multisetsTable.setRowSelectionAllowed(true);
        multisetsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScrollPane = new JScrollPane(multisetsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        result.add(tableScrollPane, BorderLayout.CENTER);
        return result;
    }

    /**
     * Metoda obsługująca kliknięcie dowolnej komórki.
     */
    protected void cellClickAction() {
        try {
            int x = multisetsTable.getSelectedRow();
            new HolmesXTPNtokens(places.get(x), ego);
        } catch (Exception ignored) {

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
