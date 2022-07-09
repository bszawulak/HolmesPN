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
import javax.swing.JOptionPane;
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
    private StatesPlacesEditorTableModelXTPN tableModel;
    private StatePlacesVectorXTPN stateVectorXTPN;
    private int stateIndex;
    private ArrayList<Place> places;
    private P_StateManager statesManager;

    private long globalTokensNumber = 0;

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
        parentWindow.setEnabled(false);
    }

    /**
     * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
     */
    public void fillTable() {
        tableModel.clearModel();
        int size = stateVectorXTPN.getSize();
        for(int p=0; p<size; p++) {
            ArrayList<Double> multiset = stateVectorXTPN.getMultisetK(p);
            StringBuilder line = new StringBuilder();
            for(Double d : multiset) {
                line.append(d).append(" | ");
            }
            tableModel.addNew(p, places.get(p).getName(), line.toString());
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
                    parentWindow.fillDescriptionField();
                }
            }
        });

        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(vectorDescrTextArea), BorderLayout.CENTER);
        CreationPanel.setBounds(posX, posY+=20, 600, 50);
        filler.add(CreationPanel);

        /*
        JButton changeAllButton = new JButton("<html>&nbsp;Set tokens&nbsp;<br>in all places</html>");
        changeAllButton.setBounds(posX+620, posY, 120, 40);
        changeAllButton.setMargin(new Insets(0, 0, 0, 0));
        changeAllButton.setFocusPainted(false);
        changeAllButton.setToolTipText("Sets same number of tokens in all places.");
        changeAllButton.setIcon(Tools.getResIcon16("/icons/stateManager/changeAll.png"));
        changeAllButton.addActionListener(actionEvent -> {
            if(places.size() == 0) {
                return;
            }
            changeGlobalTokensNumber();
        });
        result.add(changeAllButton);

        JLabel locLabel = new JLabel("New tokens number:", JLabel.LEFT);
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
        */


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
            int size = stateVectorXTPN.getSize();
            for(int p=0; p<size; p++) {
                //TODO:
                //stateVector.setTokens(p, globalTokensNumber);
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
        result.setBorder(BorderFactory.createTitledBorder("p-state vector table"));
        result.setPreferredSize(new Dimension(500, 500));

        tableModel = new StatesPlacesEditorTableModelXTPN(this, stateIndex);
        JTable table = new RXTable(tableModel);
        ((RXTable) table).setSelectAllForEdit(true);

        table.getColumnModel().getColumn(0).setHeaderValue("ID");
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(0).setMinWidth(30);
        table.getColumnModel().getColumn(0).setMaxWidth(30);
        table.getColumnModel().getColumn(1).setHeaderValue("Place name");
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(1).setMinWidth(100);
        table.getColumnModel().getColumn(2).setHeaderValue("Tokens");
        table.getColumnModel().getColumn(2).setPreferredWidth(500);
        table.getColumnModel().getColumn(2).setMinWidth(50);

        table.setName("p-stateVectorTable");
        table.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer();
        table.setDefaultRenderer(Object.class, tableRenderer);
        table.setDefaultRenderer(Double.class, tableRenderer);
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if(!e.isControlDown()) {
                        int row = table.getSelectedRow();
                        new HolmesXTPNtokens(places.get(row), ego);
                    }
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
        //TODO:
        //statesManager.getStateXTPN(index).accessVector().set(placeID, newValue);
        //parentWindow.changeTableCell(index, placeID+2, newValue, true);
        //overlord.markNetChange();
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
