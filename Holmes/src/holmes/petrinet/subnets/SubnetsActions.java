package holmes.petrinet.subnets;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.subnets.dialogs.ComboBoxItem;
import holmes.petrinet.subnets.dialogs.OnNodeAction;
import holmes.petrinet.subnets.dialogs.OnSubnetAction;

import java.util.List;
import java.util.Optional;

public class SubnetsActions {
    private SubnetsActions() {
    }

    public static void openTransferElementsToSubnet(GraphPanel graphPanel, boolean createMetaArcs) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();

        OnSubnetAction onSubnetActionDialog = new OnSubnetAction(graphPanel, "Transfer to subnet (with M-Arcs)", 400, 240);
        onSubnetActionDialog.setAction(e -> {
            ComboBoxItem<Integer> selectedItem = (ComboBoxItem) onSubnetActionDialog.getComboBox().getSelectedItem();
            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
            GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
            onSubnetActionDialog.getDialog().dispose();
            Optional.ofNullable(selectedItem).ifPresent(item -> {
                List<ElementLocation> oldSubnetElements = List.copyOf(overlord.subnetsHQ.getSubnetElementLocations(item.getValue()));
                overlord.subnetsHQ.moveSelectedElementsToSubnet(graphPanel, item.getValue(), createMetaArcs);
                List<ElementLocation> newSubnetElements = overlord.subnetsHQ.getSubnetElementLocations(item.getValue()).stream()
                        .filter(location -> !oldSubnetElements.contains(location)).toList();
                overlord.subnetsHQ.realignElements(newSubnetElements, oldSubnetElements);
                overlord.subnetsHQ.getGraphPanel(item.getValue()).adjustOriginSize();
            });
        });
        onSubnetActionDialog.open();
    }

    public static void openCopyElementsToSubnet(GraphPanel graphPanel) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();

        OnSubnetAction onSubnetActionDialog = new OnSubnetAction(graphPanel, "Copy into subnet", 400, 240);
        onSubnetActionDialog.setAction(e -> {
            ComboBoxItem<Integer> selectedItem = (ComboBoxItem) onSubnetActionDialog.getComboBox().getSelectedItem();
            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
            GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
            onSubnetActionDialog.getDialog().dispose();
            Optional.ofNullable(selectedItem).ifPresent(item -> {
                List<ElementLocation> oldSubnetElements = List.copyOf(overlord.subnetsHQ.getSubnetElementLocations(item.getValue()));
                overlord.subnetsHQ.copySelectedElementsToSubnet(graphPanel, item.getValue());
                List<ElementLocation> newSubnetElements = overlord.subnetsHQ.getSubnetElementLocations(item.getValue()).stream()
                        .filter(location -> !oldSubnetElements.contains(location)).toList();
                overlord.subnetsHQ.realignElements(newSubnetElements, oldSubnetElements);
                overlord.subnetsHQ.getGraphPanel(item.getValue()).adjustOriginSize();
            });
        });
        onSubnetActionDialog.open();
    }


    public static void addExistingElement(GraphPanel graphPanel) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();

        OnNodeAction onNodeActionDialog = new OnNodeAction(graphPanel, "Add element to subnet", 400, 240);
        onNodeActionDialog.setAction(e -> {
            ComboBoxItem<ElementLocation> selectedItem = (ComboBoxItem) onNodeActionDialog.getComboBox().getSelectedItem();
            Optional.ofNullable(selectedItem).ifPresent(item -> {
                ElementLocation newLocation = overlord.subnetsHQ.cloneNodeIntoPortal(item.getValue(), graphPanel.getSheetId());
                newLocation.setPosition(graphPanel.getMousePt());
            });

            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
            GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
            onNodeActionDialog.getDialog().dispose();
        });
        onNodeActionDialog.open();
    }

}
