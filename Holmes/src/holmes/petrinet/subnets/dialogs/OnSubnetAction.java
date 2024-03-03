package holmes.petrinet.subnets.dialogs;

import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.MetaNode;

import javax.swing.*;
import java.awt.event.ActionListener;

public class OnSubnetAction extends BaseDialog {

    private JComboBox<ComboBoxItem<Integer>> comboBox;
    private JButton button;

    public OnSubnetAction(GraphPanel graphPanel, String title, int width, int height) {
        super(title, width, height);
        initComponents(graphPanel);
    }

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
                .filter(node -> node instanceof MetaNode metaNode && metaNode.getMySheetID() == graphPanel.getSheetId())
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

    public void setAction(ActionListener action) {
        button.addActionListener(action);
    }

    public ComboBoxItem<Integer> getComboBoxValue() {
        return (ComboBoxItem) comboBox.getSelectedItem();
    }
}

