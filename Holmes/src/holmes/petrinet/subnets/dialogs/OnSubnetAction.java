package holmes.petrinet.subnets.dialogs;

import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.MetaNode;

import javax.swing.*;
import java.awt.event.ActionListener;

/**
 * Klasa tworząca dialog, w którym po wybraniu podsieci i kliknięciu przycisku, wykonywana jest
 * dowolna wcześniej przekazana akcja.
 */
public class OnSubnetAction extends BaseDialog {

    private JComboBox<ComboBoxItem<Integer>> comboBox;
    private JButton button;

    /**
     * Konstruktor klasy OnSubnetAction.
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     * @param title String - tytuł dialogu
     * @param width int - szerokość okna dialogu
     * @param height int - wysokość okna dialogu
     */
    public OnSubnetAction(GraphPanel graphPanel, String title, int width, int height) {
        super(title, width, height);
        initComponents(graphPanel);
    }

    /**
     * Metoda inicjująca wszystkie komponenty dialogu.
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     */
    private void initComponents(GraphPanel graphPanel) {
        JPanel panel = new JPanel(null);
        getDialog().add(panel);

        JLabel subnetsLabel = new JLabel("Select subnet:");
        panel.add(subnetsLabel);
        subnetsLabel.setBounds(50, 50, 90, 30);

        comboBox = new JComboBox<>();
        panel.add(comboBox);
        comboBox.setBounds(145, 50, 200, 30);
        graphPanel.getNodes().stream()
                .filter(node -> node instanceof MetaNode && ((MetaNode)node).getMySheetID() == graphPanel.getSheetId())
                .map(MetaNode.class::cast)
                .map(metaNode -> {
                    String subnetName = metaNode.getName();
                    String label = String.format(" [%d] %s", metaNode.getRepresentedSheetID(), subnetName);
                    return new ComboBoxItem<Integer>(metaNode.getRepresentedSheetID(), label);
                })
                .forEach(comboBox::addItem);

        button = new JButton("Confirm");
        panel.add(button);
        button.setBounds(130, 150, 120, 30);
    }

    /**
     * Metoda ustawiająca akcję wykonywaną po kliknięciu przycisku.
     * @param action ActionListener - dowolna akcja
     */
    public void setAction(ActionListener action) {
        button.addActionListener(action);
    }

    public ComboBoxItem<Integer> getComboBoxValue() {
        return (ComboBoxItem) comboBox.getSelectedItem();
    }
}

