package holmes.windows;

import holmes.darkgui.GUIManager;
import holmes.firingrate.FiringFreqManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Transition;
import holmes.tables.RXTable;
import holmes.tables.managers.FiringCalcTableModel;
import holmes.tables.managers.FiringCalcTableRenderer;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * Klasa tworzy okno, pozwalające uruchomić algorytmy do wyznaczania częstości uruchomienia tranzycji.
 * Kod skopiowany z innych plików tego typu. 
 *
 */
public class HolmesFiringRateAlgViewer extends JFrame {
    private JFrame ego;
    private GUIManager overlord;
    private FiringCalcTableRenderer tableRenderer;
    private FiringCalcTableModel tableModel;
    private JTable transitionsTable;
    private JPanel tablePanel;

    private PetriNet pn;

    private FiringFreqManager firingManager;
    private ArrayList<Transition> transitions;
    private int selectedRow;

    /**
     * Główny konstruktor okna menagera stanów początkowych.
     */
    public HolmesFiringRateAlgViewer() {
        setTitle("Firing Rates");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception e ) {

        }
        overlord = GUIManager.getDefaultGUIManager();
        this.ego = this;
        pn = overlord.getWorkspace().getProject();
        transitions = pn.getTransitions();
        selectedRow = 0;
        firingManager = new FiringFreqManager(pn);
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
        setSize(new Dimension(580, 650));
        setLocation(50, 50);
        setResizable(true);

        setLayout(new BorderLayout());
        JPanel main = new JPanel(new BorderLayout());

        JPanel submain = new JPanel(new BorderLayout());

        tablePanel = getMainTablePanel();
        submain.add(tablePanel, BorderLayout.CENTER);

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
        result.setBorder(BorderFactory.createTitledBorder("Transitions list"));
        result.setPreferredSize(new Dimension(450, 500));

        tableModel = new FiringCalcTableModel(this);
        transitionsTable = new RXTable(tableModel);
        ((RXTable)transitionsTable).setSelectAllForEdit(true);

        transitionsTable.getColumnModel().getColumn(0).setHeaderValue("Transition name:");
        transitionsTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        transitionsTable.getColumnModel().getColumn(0).setMinWidth(200);
        transitionsTable.getColumnModel().getColumn(0).setMaxWidth(600);
        transitionsTable.getColumnModel().getColumn(1).setHeaderValue("ID");
        transitionsTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        transitionsTable.getColumnModel().getColumn(1).setMinWidth(20);
        transitionsTable.getColumnModel().getColumn(1).setMaxWidth(50);
        transitionsTable.getColumnModel().getColumn(2).setHeaderValue("Firing frequency");
        transitionsTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        transitionsTable.getColumnModel().getColumn(2).setMinWidth(100);
        transitionsTable.getColumnModel().getColumn(2).setMaxWidth(300);


        transitionsTable.setName("TTable");
        transitionsTable.setFillsViewportHeight(true); // tabela zajmująca tyle miejsca, ale jest w panelu - związane ze scrollbar
        tableRenderer = new FiringCalcTableRenderer();
        transitionsTable.setDefaultRenderer(Object.class, tableRenderer);
        transitionsTable.setDefaultRenderer(Double.class, tableRenderer);

        transitionsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
//                    if(e.isControlDown() == false)
                        cellClickAction();
                }
            }
        });

        transitionsTable.setRowSelectionAllowed(false);

        transitionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScrollPane = new JScrollPane(transitionsTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        result.add(tableScrollPane, BorderLayout.CENTER);

        return result;
    }

    /**
     * Tworzy panel przycisków bocznych.
     * @return JPanel - panel
     */
    public JPanel getButtonsPanel() {
        JPanel result = new JPanel(null);
        result.setBorder(BorderFactory.createTitledBorder(""));
        result.setPreferredSize(new Dimension(150, 80));

        int posXda = 10;
        int posYda = 25;
        int buttonWidth = 130;
        int buttonHeight = 40;
        JButton runCalculationsButton = new JButton("<html>Equilibrium<br>algorithm</html>");
        runCalculationsButton.setBounds(posXda, posYda, buttonWidth, buttonHeight);
        runCalculationsButton.setMargin(new Insets(0, 0, 0, 0));
        runCalculationsButton.setFocusPainted(false);
        runCalculationsButton.setToolTipText("Calculates firing rates if possible.");
        runCalculationsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if(transitions.size() == 0) {
                    noNetInfo();
                    return;
                }
                int changed = firingManager.equilibriumAlgorithm();
                displayChanged(changed);
                overlord.markNetChange();
                fillTable();
            }
        });
        result.add(runCalculationsButton);

        JButton retFreeButton = new JButton("<html>Retention free<br>algorithm</html>");
        retFreeButton.setBounds(posXda, posYda+50, buttonWidth, buttonHeight);
        retFreeButton.setMargin(new Insets(0, 0, 0, 0));
        retFreeButton.setFocusPainted(false);
        retFreeButton.setToolTipText("Calculates firing rates if possible using retention free net principle.");
        retFreeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if(transitions.size() == 0) {
                    noNetInfo();
                    return;
                }
                firingManager.retentionFreeAlgorithm();
                new HolmesRFreeAlgViewer(ego);

            }
        });
        result.add(retFreeButton);
        return result;
    }

    /**
     * Krótki komunikat, że nie ma sieci.
     */
    private void noNetInfo() {
        JOptionPane.showMessageDialog(this, "There are no transitions in the net!",
                "No net", JOptionPane.WARNING_MESSAGE);
    }

    private void displayChanged(int changed) {
        String msg;
        int totalTransitionCount = pn.getTransitions().size();
        if (changed == totalTransitionCount ) {
            msg = "Done. Firing rate calculated for all: "+ changed + " transitions.";
        }
        else {
            msg = "Done. Firing rate calculated for: "+ changed + " out of " + totalTransitionCount
                    + " transitions. Check for cycles and places in conflict.";
        }

        JOptionPane.showMessageDialog(this, msg,
                "Results", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Wypełnianie tabeli nowymi danymi - tj. odświeżanie.
     */
    private void fillTable() {
        tableModel.clearModel();
        for(int i=0; i<transitions.size(); i++) {
            ArrayList<String> rowVector = new ArrayList<String>();
            rowVector.add(transitions.get(i).getName());
            final int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transitions.get(i));
            rowVector.add(String.valueOf("t" + gID));
            rowVector.add(String.valueOf(transitions.get(i).getFiringRate()));
            tableModel.addNew(rowVector);
        }
        tableModel.fireTableDataChanged();
    }


    /**
     * Metoda wywoływana przez akcję renderera tablicy, gdy następuje zmiana w komórce.
     * @param row int - nr wiersza tablicy
     * @param column int - nr kolumny tablicy
     * @param value double - nowa wartość
     */
    public void changeState(int row, int column, double value) {
        transitions.get(row).setFiringRate(value);
        overlord.markNetChange();
        tableModel.fireTableDataChanged();
    }


    /**
     * Metoda obsługująca kliknięcie dowolnej komórki.
     */
    protected void cellClickAction() {
        try {
            selectedRow = transitionsTable.getSelectedRow();
        } catch (Exception e) {

        }
    }

    private void initiateListeners() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                super.windowGainedFocus(e);
                fillTable();
            }
        });
    }

}

