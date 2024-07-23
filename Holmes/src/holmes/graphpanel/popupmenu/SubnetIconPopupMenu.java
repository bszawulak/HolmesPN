package holmes.graphpanel.popupmenu;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.utilities.Tools;
import holmes.windows.HolmesSubnetsInfo;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * Klasa reprezentująca menu kontekstowe pojawiające po naciśnięciu prawym przyciskiem myszy na ikonę podsieci,
 * które wyświetlają się w górnej części panelu sieci głównej
 */
public class SubnetIconPopupMenu extends JPopupMenu {
    private static LanguageManager lang = GUIManager.getLanguageManager();
    /**
     * Prywatny konstruktor klasy SubnetIconPopupMenu. Jedynym sposobem na tworzenie obiektów tej klasy
     * jest wykorzystanie statycznej metody "createAndShow"
     */
    private SubnetIconPopupMenu() {

    }

    /**
     * Metoda statyczna tworząca i wyświetlająca menu kontekstowe
     * @param event MouseEvent - zdarzenie związane z kliknięciem przycisku myszy
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     * @param subnetID int - id klikniętej podsieci
     */
    public static void createAndShow(MouseEvent event, GraphPanel graphPanel, int subnetID) {
        SubnetIconPopupMenu popupMenu = new SubnetIconPopupMenu();
        popupMenu.addMenuItem(lang.getText("SIPM_entry001"), "", e -> HolmesSubnetsInfo.open(subnetID));
        popupMenu.show(event, graphPanel);
    }

    /**
     * Metoda dodająca element do menu kontekstowego
     * @param text String - wyświetlany tekst
     * @param iconName String - nazwa ikony
     * @param actionListener ActionListener - akcja wywoływana po kliknięciu tego elementu
     */
    protected void addMenuItem(String text, String iconName, ActionListener actionListener) {
        JMenuItem menuItem;
        if(iconName.isEmpty()) {
            menuItem = new JMenuItem(text);
        } else {
            menuItem = new JMenuItem(text, Tools.getResIcon16("/icons/" + iconName));
        }
        menuItem.addActionListener(actionListener);
        this.add(menuItem);
    }

    /**
     * Metoda wyświetlające menu kontekstowe
     * @param e MouseEvent - zdarzenie związane z kliknięciem przycisku myszy
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     */
    protected void show(MouseEvent e, GraphPanel graphPanel) {
        super.show(graphPanel, e.getX(), e.getY());
    }
}
