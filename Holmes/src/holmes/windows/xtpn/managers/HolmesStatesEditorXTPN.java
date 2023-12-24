package holmes.windows.xtpn.managers;

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
import holmes.petrinet.data.MultisetM;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.tables.RXTable;
import holmes.tables.managers.StatesPlacesEditorTableModelXTPN;
import holmes.utilities.Tools;
import holmes.windows.managers.HolmesStatesManager;
import holmes.windows.xtpn.HolmesXTPNtokens;

public class HolmesStatesEditorXTPN extends JFrame {
    @Serial
    private static final long serialVersionUID = 3176765993380657329L;
    private HolmesStatesManager parentWindow;
    private RXTable multisetsTable;
    private StatesPlacesEditorTableModelXTPN tableModel;
    private MultisetM multisetM;
    private int stateIndex;
    private ArrayList<Place> places;
    private P_StateManager statesManager;

    public boolean doNotUpdate;

    /**
     * Główny konstruktor okna edycji stanu sieci.
     * @param parent <b>HolmesStatesManager</b> - okno wywołujące
     * @param stateVector <b>StatePlacesVector</b> - wektor SSA
     * @param stateIndex <b>int</b> - indeks powyższego wektora w tablicy
     */
    public HolmesStatesEditorXTPN(HolmesStatesManager parent, MultisetM stateVector, int stateIndex) {
        setTitle("Holmes p-state editor XTPN");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (640125897) | Exception:  "+ex.getMessage(), "error", true);
        }
        doNotUpdate = true;
        GUIManager overlord = GUIManager.getDefaultGUIManager();
        PetriNet pn = overlord.getWorkspace().getProject();
        this.parentWindow = parent;
        this.multisetM = stateVector;
        this.stateIndex = stateIndex;
        this.places = pn.getPlaces();
        this.statesManager = pn.accessStatesManager();

        initalizeComponents();
        initiateListeners();
        fillTable();
        setVisible(true);
        //multisetsTable.setRowSelectionInterval(0, 0);
        parentWindow.setEnabled(false);
        doNotUpdate = false;
    }

    /**
     * Wypełnianie tabeli tokenami.
     */
    public void fillTable() {
        tableModel.clearModel();
        int size = multisetM.getMultiset_M_Size();
        if(size != places.size()) {
            GUIManager.getDefaultGUIManager().log("Error, state corrupted. State size: "+size+", places number:"+places.size(),"error", true);
            return;
        }
        for(int placeIndex=0; placeIndex<size; placeIndex++) {
            ArrayList<Double> multisetK = multisetM.accessMultiset_K(placeIndex);
            boolean isXTPNplace = multisetM.isPlaceStoredAsGammaActive(placeIndex);
            StringBuilder line = new StringBuilder();
            if(multisetK.size() == 0) { //jeśli nic nie ma w multizbiorze
                if(isXTPNplace) {
                    line.append(" <empty> "); //puste miejsce XTPN
                } else { //liczba tokenów miejsca klasycznego:
                    line.append(0).append(" <ClassicalPlace> ");
                }
            } else {
                if(isXTPNplace) {
                    for(Double d : multisetK) {
                        line.append(d).append(" | ");
                    }
                } else {
                    double tokensNumber = multisetK.get(0);
                    line.append((int)tokensNumber).append(" <ClassicalPlace> ");
                }
            }
            tableModel.addNew(placeIndex, places.get(placeIndex).getName(), line.toString());
        }
        tableModel.fireTableDataChanged();
    }

    /**
     * Główna metoda tworząca wygląd okna.
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
     * @return (<b>JPanel</b>) - panel górny.
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
     * @return (<b>JPanel</b>) - panel główny okna.
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
            if(doNotUpdate)
                return;

            cellClickAction();
        });
        //z nieznanych mi powodów powyższy kod działa, a poniższy za cholerę. Tj. gorzej: działa, ale dopiero od
        //drugiego kliknięcia w tabelę. Za pierszym tabela na kliknięcie i na selectedRow() zawsze zwraca -1...
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
     * Metoda obsługująca kliknięcie dowolnej komórki w edytorze stanów. Powoduje wywołanie okna
     * edycji tokenów, do którego przesyła obiekt miejsca, obiekt wywołujący (to okno), multizbiór K miejsca
     * oraz informację, czy w przechowywanym stanie (multizbiorze M) miejsce jest oznaczone jako czasowe czy nie.
     */
    protected void cellClickAction() {
        try {
            int selectedPlace = multisetsTable.getSelectedRow();
            new HolmesXTPNtokens( (PlaceXTPN)places.get(selectedPlace)
                    , this, multisetM.accessMultiset_K(selectedPlace)
                    , multisetM.isPlaceStoredAsGammaActive(selectedPlace));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (860120239) | Exception:  "+ex.getMessage(), "error", true);
        }
    }

    /**
     * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
     */
    private void initiateListeners() { //HAIL SITHIS
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                //gdy okno jest zamykane, odblokowuje okno wywołujące - zapewne XTPN state manager
                if(parentWindow != null)
                    parentWindow.setEnabled(true);
            }
        });
    }
}
