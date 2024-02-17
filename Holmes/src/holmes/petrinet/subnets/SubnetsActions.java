package holmes.petrinet.subnets;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.utilities.Tools;

import javax.swing.*;
import java.util.List;
import java.util.Optional;

public class SubnetsActions {

    private SubnetsActions() {}

    public static void openAddSelectedElementsToSubnet(GraphPanel graphPanel) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();
        JDialog dialog = createBaseDialog("Add selected elements to subnet", 400, 240);

        JPanel panel = new JPanel(null);
        dialog.add(panel);

        JLabel subnetsLabel = new JLabel("Select subnet:");
        panel.add(subnetsLabel);
        subnetsLabel.setBounds(50, 50, 90, 30);

        JComboBox<ComboBoxItem<Integer>> comboBox = new JComboBox<>();
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

        JButton button = new JButton("Add elements");
        panel.add(button);
        button.setBounds(130, 150, 120, 30);
        button.addActionListener(e1 -> {
            ComboBoxItem<Integer> selectedItem = (ComboBoxItem) comboBox.getSelectedItem();
            Optional.ofNullable(selectedItem).ifPresent(item -> {
                        List<ElementLocation> oldSubnetElements = List.copyOf(overlord.subnetsHQ.getSubnetElementLocations(item.getValue()));
                        overlord.subnetsHQ.moveSelectedElementsToSubnet(graphPanel, item.getValue());
                        List<ElementLocation> newSubnetElements = overlord.subnetsHQ.getSubnetElementLocations(item.getValue()).stream()
                                .filter(location -> !oldSubnetElements.contains(location)).toList();
                        overlord.subnetsHQ.realignElements(newSubnetElements, oldSubnetElements);
                        overlord.subnetsHQ.getGraphPanel(item.getValue()).adjustOriginSize();
                    });
            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
            GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    public static void addExistingElement(GraphPanel graphPanel) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();
        JDialog dialog = createBaseDialog("Add element to subnet", 400, 240);

        JPanel panel = new JPanel(null);
        dialog.add(panel);

        JLabel elementsLabel = new JLabel("Select element:");
        panel.add(elementsLabel);
        elementsLabel.setBounds(45, 50, 90, 30);

        JComboBox<ComboBoxItem<ElementLocation>> comboBox = new JComboBox<>();
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

        JButton button = new JButton("Add element");
        panel.add(button);
        button.setBounds(130, 150, 120, 30);
        button.addActionListener(e1 -> {
            ComboBoxItem<ElementLocation> selectedItem = (ComboBoxItem) comboBox.getSelectedItem();
            Optional.ofNullable(selectedItem).ifPresent(item -> {
                        ElementLocation newLocation = overlord.subnetsHQ.cloneNodeIntoPortal(item.getValue(), graphPanel.getSheetId());
                        newLocation.setPosition(graphPanel.getMousePt());
                    });

            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
            GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
            dialog.dispose();
        });

        dialog.setVisible(true);
    }

    public static class ComboBoxItem<T> {
        private T value;
        private String label;
        public ComboBoxItem (T value, String label) {
            this.value = value;
            this.label = label;
        }

        public T getValue() {
            return value;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static JDialog createBaseDialog(String title, int width, int height) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();
        JFrame frame = overlord.getFrame();
        JDialog dialog = new JDialog(frame, title);
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(dialog.getParent());
        dialog.setResizable(false);
        frame.setEnabled(false);
        try {
            dialog.setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            overlord.log("Error (488078573) | Exception:  " + ex.getMessage(), "error", true);
        }
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                frame.setEnabled(true);
            }
        });
        return dialog;
    }
}
