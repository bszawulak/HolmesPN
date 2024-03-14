package holmes.petrinet.subnets;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;
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

        String title = createMetaArcs ? "Transfer to subnet (with M-Arcs)" : "Transfer to subnet (no M-Arcs)";
        OnSubnetAction onSubnetActionDialog = new OnSubnetAction(graphPanel, title, 400, 240);
        onSubnetActionDialog.setAction(e -> {
            ComboBoxItem<Integer> selectedItem = onSubnetActionDialog.getComboBoxValue();
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
            ComboBoxItem<Integer> selectedItem = onSubnetActionDialog.getComboBoxValue();
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

        OnNodeAction onNodeActionDialog = new OnNodeAction(graphPanel, "Add node to subnet", 540, 340);
        onNodeActionDialog.setAction(e -> {
            ComboBoxItem<Place> selectedPlace = onNodeActionDialog.getPlaceComboBoxValue();
            ComboBoxItem<Transition> selectedTransition = onNodeActionDialog.getTransitionComboBoxValue();
            Node selectedNode = null;
            if (selectedPlace != null) {
                selectedNode = selectedPlace.getValue();
            } else if (selectedTransition != null) {
                selectedNode = selectedTransition.getValue();
            }

            boolean addMetaArcs = onNodeActionDialog.getCheckboxValue();

            Optional.ofNullable(selectedNode).ifPresent(node -> {
                ElementLocation newLocation = overlord.subnetsHQ.cloneNodeIntoPortal(node.getLastLocation(), graphPanel.getSheetId());
                newLocation.setPosition(graphPanel.getMousePt());
                if (addMetaArcs) {
                    ElementLocation nearMetanode = overlord.subnetsHQ.cloneLocationNearMetanode(newLocation, graphPanel.getSheetId());
                    Arc newArc = new Arc(IdGenerator.getNextId(), nearMetanode,
                            overlord.subnetsHQ.getMetanode(graphPanel.getSheetId()).orElseThrow().getFirstELoc(),
                            Arc.TypeOfArc.META_ARC);
                    overlord.getWorkspace().getProject().addArc(newArc);
                }
            });

            GUIManager.getDefaultGUIManager().getWorkspace().repaintAllGraphPanels();
            GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
            onNodeActionDialog.getDialog().dispose();
        });
        onNodeActionDialog.open();
    }

}
