package holmes.petrinet.subnets;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.*;
import holmes.petrinet.subnets.dialogs.ComboBoxItem;
import holmes.petrinet.subnets.dialogs.OnNodeAction;
import holmes.petrinet.subnets.dialogs.OnSubnetAction;

import java.util.List;
import java.util.Optional;

/**
 * Klasa użytkowa grupująca metody, które wywołują dialogi powiązane z podsieciami.
 */
public class SubnetsActions {
    private static LanguageManager lang = GUIManager.getLanguageManager();
    /**
     * Konstruktor prywatny, ponieważ klasa składa się wyłącznie z metod statycznych.
     */
    private SubnetsActions() {
    }

    /**
     * Metoda otwiera dialog, który pozwala na przeniesienie zaznaczonych elementów do podsieci.
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     * @param createMetaArcs boolean - czy tworzyć meta-łuki pomiędzy otoczeniem przenoszonych elementów a meta-węzłem
     */
    public static void openTransferElementsToSubnet(GraphPanel graphPanel, boolean createMetaArcs) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();

        String title = createMetaArcs ? lang.getText("SA_entry001") : lang.getText("SA_entry002"); //Transfer to subnet (with M-Arcs) ; Transfer to subnet (no M-Arcs)
        OnSubnetAction onSubnetActionDialog = new OnSubnetAction(graphPanel, title, 400, 240);
        onSubnetActionDialog.setAction(e -> {
            ComboBoxItem<Integer> selectedItem = onSubnetActionDialog.getComboBoxValue();
            overlord.getWorkspace().repaintAllGraphPanels();
            overlord.getFrame().setEnabled(true);
            onSubnetActionDialog.getDialog().dispose();
            Optional.ofNullable(selectedItem).ifPresent(item -> {
                overlord.markNetChange();
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

    /**
     * Metoda otwiera dialog, który pozwala na skopiowanie zaznaczonych elementów do podsieci.
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     */
    public static void openCopyElementsToSubnet(GraphPanel graphPanel) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();

        OnSubnetAction onSubnetActionDialog = new OnSubnetAction(graphPanel, lang.getText("SA_entry003"), 400, 240); //Copy into subnet
        onSubnetActionDialog.setAction(e -> {
            ComboBoxItem<Integer> selectedItem = onSubnetActionDialog.getComboBoxValue();
            overlord.getWorkspace().repaintAllGraphPanels();
            overlord.getFrame().setEnabled(true);
            onSubnetActionDialog.getDialog().dispose();
            Optional.ofNullable(selectedItem).ifPresent(item -> {
                overlord.markNetChange();
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
    
    /**
     * Metoda otwiera dialog, który pozwala na dodanie istniejącego elementu do podsieci wybranego arkusza.
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     */
    public static void addExistingElement(GraphPanel graphPanel) {
        GUIManager overlord = GUIManager.getDefaultGUIManager();

        OnNodeAction onNodeActionDialog = new OnNodeAction(graphPanel, lang.getText("SA_entry004"), 540, 340); //Add node to subnet
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
                overlord.markNetChange();
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
