package holmes.windows.decompositions;

import holmes.analyse.SubnetCalculator;
import holmes.analyse.comparison.DecoComparisonCalculator;
import holmes.analyse.comparison.structures.GreatCommonSubnet;
import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class HolmesPrototypeComparison extends JFrame {
    private GUIManager overlord;
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JPanel upPanel;
    private JPanel downPanel;
    public JTable comparisonTable;

    ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList;

    public HolmesPrototypeComparison() {
        //super("Row Header Example");
        setSize(1800, 700);

        ListModel lm = new AbstractListModel() {
            String headers[] = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};

            public int getSize() {
                return headers.length;
            }

            public Object getElementAt(int index) {
                return headers[index];
            }
        };

        DefaultTableModel dm = new DefaultTableModel(lm.getSize(), 20);
        JTable table = new JTable(dm);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JList rowHeader = new JList(lm);
        rowHeader.setFixedCellWidth(50);
        rowHeader.setFixedCellHeight(18);

        //rowHeader.setFixedCellHeight(table.getRowHeight()  + table.getRowMargin() + table.getIntercellSpacing().height);
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setRowHeaderView(rowHeader);
        getContentPane().add(scroll, BorderLayout.WEST);

        rightPanel = createInfoPanel(320, 0, 400, 150);
        getContentPane().add(rightPanel, BorderLayout.EAST);

    }


    public HolmesPrototypeComparison(ArrayList<ArrayList<GreatCommonSubnet>> subNetArrayList) {
        //super("Row Header Example");
        setSize(1800, 700);

        createComparisonTable(subNetArrayList);

        //for net I
        ArrayList<int[][]> subMatrix = new ArrayList<>();

        rightPanel = createInfoPanel(320, 0, 400, 150);
        getContentPane().add(rightPanel, BorderLayout.EAST);

    }

    private void createComparisonTable(ArrayList<ArrayList<GreatCommonSubnet>> snl) {
        subNetArrayList = snl;
        String[] netLabels = new String[subNetArrayList.size()];

        //do poszerzenia dla 2 sieci
        for (int i = 0; i < subNetArrayList.size(); i++) {
            netLabels[i] = "Sub :" + i;
        }

        ListModel lm = new AbstractListModel() {
            String headers[] = netLabels;

            public int getSize() {
                return headers.length;
            }

            public Object getElementAt(int index) {
                return headers[index];
            }
        };

        DefaultTableModel dm = new DefaultTableModel(lm.getSize(), netLabels.length);
        dm.setColumnIdentifiers(netLabels);

        String[][] results = new String[subNetArrayList.size()][subNetArrayList.get(0).size()];

        for (int i = 0; i < subNetArrayList.size(); i++) {
            for (int j = 0; j < subNetArrayList.get(i).size(); j++) {

                results[i][j] = String.valueOf(subNetArrayList.get(i).get(j).gcsValue + " / " + subNetArrayList.get(i).get(j).firstNetNodeSize);
            }
        }

        comparisonTable = new JTable(results, netLabels) {//;
            //JTable table = new JTable(data, columnNames) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
                Component comp = super.prepareRenderer(renderer, row, col);
                Object value = getModel().getValueAt(row, col);
                //if (getSelectedRow() == row) {
                if (subNetArrayList.get(row).get(col).gcsValue == subNetArrayList.get(row).get(col).firstNetNodeSize) {
                    comp.setBackground(Color.green);
                } else if (subNetArrayList.get(row).get(col).gcsValue == 0) {
                    comp.setBackground(Color.red);
                } else if (subNetArrayList.get(row).get(col).gcsValue > subNetArrayList.get(row).get(col).firstNetNodeSize ||
                        subNetArrayList.get(row).get(col).gcsValue > subNetArrayList.get(row).get(col).secondNetNodeSize) {
                    comp.setBackground(Color.blue);
                } else {
                    comp.setBackground(Color.white);
                }

                /*} else {
                    comp.setBackground(Color.white);
                }
                */
                return comp;
            }
        };
        comparisonTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        JList rowHeader = new JList(lm);
        rowHeader.setFixedCellWidth(50);

        rowHeader.setFixedCellHeight(comparisonTable.getRowHeight()
                + comparisonTable.getRowMargin());
        //                           + table.getIntercellSpacing().height);
        rowHeader.setCellRenderer(new RowHeaderRenderer(comparisonTable));


        comparisonTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                calcIndexes(comparisonTable.getSelectedRow(), comparisonTable.getSelectedColumn());
                showSubNet(comparisonTable.getSelectedRow(), comparisonTable.getSelectedColumn());

            }
        });

        JScrollPane scroll = new JScrollPane(comparisonTable);
        scroll.setRowHeaderView(rowHeader);
        //scroll.setColumnHeaderView(rowHeader);
        getContentPane().add(scroll);
    }

    private void showSubNet(int selectedRow, int selectedColumn) {
        subNetArrayList.get(selectedColumn).get(selectedRow);

    }

    private void calcIndexes(int selectedRow, int selectedColumn) {

    }


    @SuppressWarnings("SameParameterValue")
    private JPanel createInfoPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Info"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));

        int posX = 10;
        int posY = 10;

        //nie buffered image a....

        BufferedImage bi = null;
        try {
            bi = ImageIO.read(new File(getClass().getResource("/MockImageGraphComparison.png").getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }


        JLabel graphComp = new JLabel(new ImageIcon(bi));

        panel.add(graphComp, BorderLayout.NORTH);
        JLabel fs1 = new JLabel("There will be math");
        fs1.setLayout(new GridLayout());
        fs1.setEnabled(true);

        panel.add(fs1, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        //leftPanel = createLeftPanel(0, 0, 180, 230);
        //rightPanel = createGraphPanel(0, 130, 500, 300);
        panel.add(leftPanel, BorderLayout.WEST);
        //panel.add(rightPanel, BorderLayout.EAST);
        panel.repaint();
        return panel;
    }

    private JPanel createLeftPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("Decomposition types"));
        panel.setLocation(x, y);
        panel.setPreferredSize(new Dimension(width, height));

        int posX = 10;
        int posY = 20;


        //


        //panel.add(scroll,BorderLayout.CENTER);
        ///


        return panel;
    }


    class RowHeaderRenderer extends JLabel implements ListCellRenderer {

        RowHeaderRenderer(JTable table) {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        public Component getListCellRendererComponent(JList list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
}
