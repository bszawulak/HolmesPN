package holmes.windows;

import holmes.darkgui.GUIManager;
import holmes.firingrate.ConstraintException;
import holmes.firingrate.ExpressionBuilder;
import holmes.firingrate.FiringFreqManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Transition;
import holmes.tables.RXTable;
import holmes.tables.managers.FiringCalcTableRenderer;
import holmes.tables.managers.PParameterTableModel;
import holmes.tables.managers.ResultParameterTableModel;
import holmes.tables.managers.TParameterTableModel;
import holmes.utilities.Tools;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
 * Klasa tworzy okno przedstawiające wyniki drugiego algorymtu (RetentionFree z FiringFreqManager)
 * Kod skopiowany z innych plików tego typu. Część elementów pewnie jest zbędna, ale działa.
 *
 */

public class HolmesRFreeAlgViewer extends JFrame {
    private static final long serialVersionUID = 1441277645650983957L;
    private GUIManager overlord;
    private JFrame parentWindow;
    private JFrame ego;
    private int selectedRow;
    private FiringCalcTableRenderer tableRenderer;
    private PParameterTableModel pTableModel;
    private TParameterTableModel tTableModel;
    private ResultParameterTableModel resultModel;
    private JTable probabilityTable;
    private JTable tRateTable;
    private JTable resultTable;
    private JPanel leftPanel;
    private JPanel middlePanel;
    private JPanel rightPanel;
    private ArrayList<Arc> arcsForTable;
    private ArrayList<Transition> TForTable;

    private ExpressionBuilder expressionBuilder;
    private ArrayList<Transition> transitions;
    private PetriNet pn;

    public HolmesRFreeAlgViewer(JFrame parent) {
        setTitle("Result");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception e) {

        }
        this.overlord = GUIManager.getDefaultGUIManager();
        this.pn = overlord.getWorkspace().getProject();
        this.ego = this;
        this.parentWindow = parent;
        this.expressionBuilder = new ExpressionBuilder();
        this.transitions = pn.getTransitions();
//        this.firingManager = firingManager;
        arcsForTable = pn.getConflictArcs();


        initalizeComponents();
        initiateListeners();
        fillFirstTable();
        fillSecondTable();
        fillThirdTable();

        setVisible(true);
        parentWindow.setEnabled(false);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * Główna metoda tworząca panele okna.
     */
    private void initalizeComponents() {
        setSize(new Dimension(900, 650));
        setLocation(50, 50);
        setResizable(true);
        setLayout(new GridBagLayout());

        leftPanel = getLeftTablePanel();
        middlePanel = getMiddlePanel();
        rightPanel = getRightPanel();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx=0.3;
        gbc.weighty=1;
        gbc.gridx=0;
        gbc.gridy=0;
        gbc.fill = GridBagConstraints.BOTH;

        add(leftPanel, gbc);
        gbc.gridx=1;
        gbc.weightx=0.1;
        add(middlePanel, gbc);
        gbc.gridx=2;
        gbc.weightx=0.4;
        add(rightPanel, gbc);
        gbc.gridx=3;
        gbc.weightx=0.2;
        add(getButtonsPanel(), gbc);
    }

    public JPanel getLeftTablePanel() {
        JPanel result = new JPanel(new BorderLayout());
        result.setLocation(0, 0);
        TitledBorder title = BorderFactory.createTitledBorder("Probability table");
        title.setTitleJustification(TitledBorder.CENTER);
        result.setBorder(title);
        result.setPreferredSize(new Dimension(650, 250));

        pTableModel = new PParameterTableModel(this);
        probabilityTable = new RXTable(pTableModel);
        ((RXTable) probabilityTable).setSelectAllForEdit(true);

        probabilityTable.getColumnModel().getColumn(0).setHeaderValue("sID");
        probabilityTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        probabilityTable.getColumnModel().getColumn(0).setMinWidth(20);
        probabilityTable.getColumnModel().getColumn(1).setHeaderValue("pID");
        probabilityTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        probabilityTable.getColumnModel().getColumn(1).setMinWidth(20);
        probabilityTable.getColumnModel().getColumn(2).setHeaderValue("tID");
        probabilityTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        probabilityTable.getColumnModel().getColumn(2).setMinWidth(20);
        probabilityTable.getColumnModel().getColumn(3).setHeaderValue("Probability");
        probabilityTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        probabilityTable.getColumnModel().getColumn(3).setMinWidth(100);

        probabilityTable.setName("probabilityTable");
        tableRenderer = new FiringCalcTableRenderer();
        probabilityTable.setDefaultRenderer(Object.class, tableRenderer);
        probabilityTable.setDefaultRenderer(Double.class, tableRenderer);

        probabilityTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if (e.isControlDown() == false)
                        cellClickAction();
                }
            }
        });

        probabilityTable.setRowSelectionAllowed(false);

        probabilityTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScrollPane = new JScrollPane(probabilityTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        result.add(tableScrollPane, BorderLayout.CENTER);

        return result;
    }

    public JPanel getMiddlePanel() {
        JPanel result = new JPanel(new BorderLayout());
        TitledBorder title = BorderFactory.createTitledBorder("Tsource table");
        title.setTitleJustification(TitledBorder.CENTER);
        result.setBorder(title);
        result.setPreferredSize(new Dimension(150, 250));

        tTableModel = new TParameterTableModel(this);
        tRateTable = new RXTable(tTableModel);
        ((RXTable) tRateTable).setSelectAllForEdit(true);

        tRateTable.getColumnModel().getColumn(0).setHeaderValue("tID");
        tRateTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        tRateTable.getColumnModel().getColumn(0).setMinWidth(20);
        tRateTable.getColumnModel().getColumn(1).setHeaderValue("Firing rate");
        tRateTable.getColumnModel().getColumn(1).setPreferredWidth(50);
        tRateTable.getColumnModel().getColumn(1).setMinWidth(20);
        tRateTable.setName("tTable");
        tableRenderer = new FiringCalcTableRenderer();
        tRateTable.setDefaultRenderer(Object.class, tableRenderer);
        tRateTable.setDefaultRenderer(Double.class, tableRenderer);

        tRateTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if (e.isControlDown() == false)
                        cellClickAction();
                }
            }
        });

        tRateTable.setRowSelectionAllowed(false);

        tRateTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScrollPane = new JScrollPane(tRateTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        result.add(tableScrollPane, BorderLayout.CENTER);

        return result;
    }

    public JPanel getRightPanel() {
        JPanel result = new JPanel(new BorderLayout());
        result.setLocation(0, 0);
        TitledBorder title = BorderFactory.createTitledBorder("Results");
        title.setTitleJustification(TitledBorder.CENTER);
        result.setBorder(title);

        result.setPreferredSize(new Dimension(500, 500));

        resultModel = new ResultParameterTableModel();
        resultTable = new RXTable(resultModel);
        ((RXTable) resultTable).setSelectAllForEdit(true);


        resultTable.getColumnModel().getColumn(0).setHeaderValue("Functions");
        resultTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        resultTable.getColumnModel().getColumn(0).setMinWidth(150);
        resultTable.getColumnModel().getColumn(0).setCellRenderer(new WordWrapCellRenderer());


        resultTable.setName("ResultTable");
        tableRenderer = new FiringCalcTableRenderer();

        resultTable.setDefaultRenderer(Object.class, tableRenderer);
        resultTable.setDefaultRenderer(Double.class, tableRenderer);

        resultTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if (e.isControlDown() == false)
                        cellClickAction();
                }
            }
        });

        resultTable.setRowSelectionAllowed(false);

        resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        JScrollPane tableScrollPane = new JScrollPane(resultTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        result.add(tableScrollPane, BorderLayout.CENTER);

        return result;
    }


    /**
     * Metoda obsługująca kliknięcie dowolnej komórki.
     */
    protected void cellClickAction() {
        try {
            selectedRow = probabilityTable.getSelectedRow();
        } catch (Exception e) {

        }
    }

    /**
     * Tworzy panel przycisków bocznych.
     *
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
        JButton runCalculationsButton = new JButton("<html>Calculate<br>frequencies</html>");
        runCalculationsButton.setBounds(posXda, posYda, buttonWidth, buttonHeight);
        runCalculationsButton.setMargin(new Insets(0, 0, 0, 0));
        runCalculationsButton.setFocusPainted(false);
        runCalculationsButton.setToolTipText("Calculates firing frequencies\n");
        runCalculationsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        runCalculationsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                calculateConstraints();
            }
        });
        result.add(runCalculationsButton, BorderLayout.CENTER);

        JButton resetButton = new JButton("Reset functions");
        resetButton.setBounds(posXda, posYda + 50, buttonWidth, buttonHeight);
        resetButton.setMargin(new Insets(0, 0, 0, 0));
        resetButton.setFocusPainted(false);
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.setToolTipText("Shows functions with parameters");
        resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                resetValues();
            }
        });
        result.add(resetButton, BorderLayout.CENTER);
        return result;
    }


    private void fillFirstTable() {
        pTableModel.clearModel();

        for (int i = 0; i < arcsForTable.size(); i++) {
            ArrayList<String> rowVector = new ArrayList<String>();
            final int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(arcsForTable.get(i).getEndNode());
            rowVector.add(String.valueOf(i));
            rowVector.add(String.valueOf(arcsForTable.get(i).getStartNode().getID()));
            rowVector.add(String.valueOf(gID));
            rowVector.add(String.valueOf(arcsForTable.get(i).getfProbability()));
            pTableModel.addNew(rowVector);
        }
        pTableModel.fireTableDataChanged();
    }

    public void changeFirstState(int row, int column, double value) {
        arcsForTable.get(row).setfProbability(value);
        overlord.markNetChange();
        pTableModel.fireTableDataChanged();
    }

    private void calculateConstraints() {
        transitions = pn.getTransitions();
        ArrayList<ArrayList<String>> rowVectors;
        try {
            rowVectors = expressionBuilder.calculateAndPrint(transitions, pn.getConflictArcs());
            resultModel.clearModel();
            tTableModel.clearModel();
            fillSecondTable();
            for (ArrayList<String> rowVector : rowVectors) {
                resultModel.addNew(rowVector);
            }

            resultModel.fireTableDataChanged();
            tTableModel.fireTableDataChanged();
        }
        catch (ConstraintException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void resetValues() {
        resultModel.clearModel();
        ArrayList<ArrayList<String>> rowVectors;
        rowVectors = expressionBuilder.printWithParams(transitions, pn.getConflictArcs());
        for (ArrayList<String> rowVector : rowVectors) {
            resultModel.addNew(rowVector);
        }
        resultModel.fireTableDataChanged();
    }

    private void fillSecondTable() {
        tTableModel.clearModel();
        TForTable = new ArrayList<>();
        transitions = pn.getTransitions();
        for (Transition transition : transitions) {
            if(transition.getInArcs().size() == 0 ) {
                ArrayList<String> rowVector = new ArrayList<String>();
                final int gID = overlord.getWorkspace().getProject().getTransitions().lastIndexOf(transition);
                rowVector.add(String.valueOf(gID));
                rowVector.add(String.valueOf(transition.getFiringRate()));
                TForTable.add(transition);
                tTableModel.addNew(rowVector);
            }
        }
        tTableModel.fireTableDataChanged();
    }

    public void changeSecondState(int row, int column, double value) {
        TForTable.get(row).setFiringRate(value);
        overlord.markNetChange();
        tTableModel.fireTableDataChanged();
    }

    private void fillThirdTable() {
        resultModel.clearModel();
        transitions = pn.getTransitions();
        ArrayList<ArrayList<String>> rowVectors;
        rowVectors = expressionBuilder.printWithParams(transitions, pn.getConflictArcs());
        for (ArrayList<String> rowVector : rowVectors) {
            resultModel.addNew(rowVector);
        }
        resultModel.fireTableDataChanged();
    }

    private void initiateListeners() {
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parentWindow.setEnabled(true);

            }
        });
    }

    /**
     * https://stackoverflow.com/a/37768834
     */
    static class WordWrapCellRenderer extends JTextArea implements TableCellRenderer {
        WordWrapCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);

        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            setFont(FiringCalcTableRenderer.fontNormal);
            setText(value.toString());
            setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
            if (table.getRowHeight(row) != getPreferredSize().height) {
                table.setRowHeight(row, getPreferredSize().height);
            }
            return this;
        }
    }
}

