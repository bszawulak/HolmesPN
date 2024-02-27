package holmes.petrinet.subnets.dialogs;

import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;

import javax.swing.*;
import java.awt.event.ActionListener;

public class OnNodeAction extends BaseDialog {

    private JComboBox<ComboBoxItem<ElementLocation>> comboBox;
    private JButton button;

    public OnNodeAction(GraphPanel graphPanel, String title, int width, int height) {
        super(title, width, height);
        initComponents(graphPanel);
    }

    private void initComponents(GraphPanel graphPanel) {
        JPanel panel = new JPanel(null);
        getDialog().add(panel);

        JLabel elementsLabel = new JLabel("Select element:");
        panel.add(elementsLabel);
        elementsLabel.setBounds(45, 50, 90, 30);

        comboBox = new JComboBox<>();
        panel.add(comboBox);
        comboBox.setBounds(145, 50, 200, 30);
        graphPanel.getNodes().stream()
                .filter(node -> !(node instanceof MetaNode))
                .flatMap(node -> node.getNodeLocations().stream())
                .filter(elementLocation -> elementLocation.getSheetID() != graphPanel.getSheetId())
                .map(location -> {
                    Node node = location.getParentNode();
                    String label = String.format("[%d] %s", node.getID(), node.getName());
                    return new ComboBoxItem<ElementLocation>(location, label);
                })
                .forEach(comboBox::addItem);

        button = new JButton("Add element");
        panel.add(button);
        button.setBounds(130, 150, 120, 30);
    }

    public void setAction(ActionListener action) {
        button.addActionListener(action);
    }

    public JComboBox<ComboBoxItem<ElementLocation>> getComboBox() {
        return comboBox;
    }

    public JButton getButton() {
        return button;
    }
}
