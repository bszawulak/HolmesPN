package holmes.petrinet.subnets.dialogs;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;

/**
 * Klasa tworząca dialog, w którym po wybraniu miejsca/tranzycji i kliknięciu przycisku, wykonywana jest
 * dowolna wcześniej przekazana akcja.
 */
public class OnNodeAction extends BaseDialog {
    private static LanguageManager lang = GUIManager.getLanguageManager();
    private JComboBox<ComboBoxItem<Place>> placeComboBox;
    private JComboBox<ComboBoxItem<Transition>> transitionComboBox;
    private JButton button;

    private JCheckBox checkBox;

    /**
     * Konstruktor klasy OnNodeAction.
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     * @param title String - tytuł dialogu
     * @param width int - szerokość okna dialogu
     * @param height int - wysokość okna dialogu
     */
    public OnNodeAction(GraphPanel graphPanel, String title, int width, int height) {
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

        JLabel placeLabel = new JLabel(lang.getText("ONA_entry001"));
        panel.add(placeLabel);
        placeLabel.setBounds(25, 50, 90, 25);

        placeComboBox = new JComboBox<>();
        panel.add(placeComboBox);
        placeComboBox.setBounds(100, 50, 400, 25);
        graphPanel.getNodes().stream()
                .filter(node -> node.getNodeLocations().stream().anyMatch(location -> location.getSheetID() != graphPanel.getSheetId()))
                .filter(Place.class::isInstance)
                .map(Place.class::cast)
                .map(place -> {
                    List<String> subnets = place.getElementLocations().stream()
                            .map(ElementLocation::getSheetID)
                            .filter(i -> i != 0)
                            .map(Object::toString)
                            .sorted().distinct().toList();
                    String subnetsLabel = subnets.isEmpty() ? "" : String.format(" (%s)", String.join(",", subnets));
                    String label = String.format("[%d]%s: %s", place.getID(), subnetsLabel, place.getName());
                    return new ComboBoxItem<Place>(place, label);
                })
                .forEach(placeComboBox::addItem);

        JLabel transitionLabel = new JLabel(lang.getText("ONA_entry002"));
        panel.add(transitionLabel);
        transitionLabel.setBounds(25, 80, 90, 25);

        transitionComboBox = new JComboBox<>();
        panel.add(transitionComboBox);
        transitionComboBox.setBounds(100, 80, 400, 25);
        graphPanel.getNodes().stream()
                .filter(node -> node.getNodeLocations().stream().anyMatch(location -> location.getSheetID() != graphPanel.getSheetId()))
                .filter(Transition.class::isInstance)
                .map(Transition.class::cast)
                .map(transition -> {
                    List<String> subnets = transition.getElementLocations().stream()
                            .map(ElementLocation::getSheetID)
                            .filter(i -> i != 0)
                            .map(Object::toString)
                            .sorted().distinct().toList();
                    String subnetsLabel = subnets.isEmpty() ? "" : String.format(" (%s)", String.join(",", subnets));
                    String label = String.format("[%d]%s: %s", transition.getID(), subnetsLabel, transition.getName());
                    return new ComboBoxItem<Transition>(transition, label);
                })
                .forEach(transitionComboBox::addItem);

        placeComboBox.setSelectedIndex(-1);
        transitionComboBox.setSelectedIndex(-1);

        JLabel typeLabel = new JLabel("");
        panel.add(typeLabel);
        typeLabel.setBounds(25, 130, 500, 25);

        JLabel idLabel = new JLabel("");
        panel.add(idLabel);
        idLabel.setBounds(25, 150, 500, 25);

        JLabel subnetsLabel = new JLabel("");
        panel.add(subnetsLabel);
        subnetsLabel.setBounds(25, 170, 500, 25);

        placeComboBox.addItemListener(e -> {
            button.setEnabled(true);
            if (e.getStateChange() != ItemEvent.DESELECTED) {
                typeLabel.setText(lang.getText("ONA_entry003")); //Type:   Place
                ComboBoxItem<Place> item = (ComboBoxItem) e.getItem();
                int gID = item.getValue().getID();
                int arrID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().indexOf(item.getValue());
                idLabel.setText(String.format("gID:  %d, ID:  %d", gID, arrID));
                List<String> subnets = item.getValue().getElementLocations().stream()
                        .map(ElementLocation::getSheetID)
                        .map(integer -> GUIManager.getDefaultGUIManager().subnetsHQ.getMetanode(integer)
                                .map(PetriNetElement::getName).orElse("Subnet0"))
                        .sorted().distinct().toList();
                subnetsLabel.setText(String.format(lang.getText("ONA_entry004"), String.join(", ", subnets))); //Portals in subnets:
                if (transitionComboBox.getSelectedIndex() != -1) {
                    transitionComboBox.setSelectedIndex(-1);
                }
            }
        });
        transitionComboBox.addItemListener(e -> {
            button.setEnabled(true);
            if (e.getStateChange() != ItemEvent.DESELECTED) {
                typeLabel.setText(lang.getText("ONA_entry005")); //Type:   Transition
                ComboBoxItem<Transition> item = (ComboBoxItem) e.getItem();
                int gID = item.getValue().getID();
                int arrID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().indexOf(item.getValue());
                idLabel.setText(String.format("gID:  %d, ID:  %d", gID, arrID));
                List<String> subnets = item.getValue().getElementLocations().stream()
                        .map(ElementLocation::getSheetID)
                        .map(integer -> GUIManager.getDefaultGUIManager().subnetsHQ.getMetanode(integer)
                                .map(PetriNetElement::getName).orElse("Subnet0"))
                        .sorted().distinct().toList();
                subnetsLabel.setText(String.format(lang.getText("ONA_entry006"), String.join(", ", subnets))); //Portals in subnets:
                if (placeComboBox.getSelectedIndex() != -1) {
                    placeComboBox.setSelectedIndex(-1);
                }
            }
        });

        checkBox = new JCheckBox(lang.getText("ONA_entry007")); //Also add Meta-Arcs with portals
        panel.add(checkBox);
        checkBox.setBounds(20, 205, 250, 25);

        button = new JButton(lang.getText("ONA_entry008"));
        panel.add(button);
        button.setEnabled(false);
        button.setBounds(190, 245, 150, 30);
    }

    /**
     * Metoda ustawiająca akcję wykonywaną po kliknięciu przycisku.
     * @param action ActionListener - dowolna akcja
     */
    public void setAction(ActionListener action) {
        button.addActionListener(action);
    }

    public ComboBoxItem<Place> getPlaceComboBoxValue() {
        return (ComboBoxItem) placeComboBox.getSelectedItem();
    }

    public ComboBoxItem<Transition> getTransitionComboBoxValue() {
        return (ComboBoxItem) transitionComboBox.getSelectedItem();
    }

    public boolean getCheckboxValue() {
        return checkBox.isSelected();
    }
}
