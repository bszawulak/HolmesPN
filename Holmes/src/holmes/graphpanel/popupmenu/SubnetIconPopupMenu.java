package holmes.graphpanel.popupmenu;

import holmes.graphpanel.GraphPanel;
import holmes.utilities.Tools;
import holmes.windows.HolmesSubnetsInfo;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class SubnetIconPopupMenu extends JPopupMenu {

    private SubnetIconPopupMenu() {

    }

    public static void createAndShow(MouseEvent event, GraphPanel graphPanel, int subnetID) {
        SubnetIconPopupMenu popupMenu = new SubnetIconPopupMenu();
        popupMenu.addMenuItem("Show details...", "", e -> HolmesSubnetsInfo.open(subnetID));
        popupMenu.show(event, graphPanel);
    }

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

    public void show(MouseEvent e, GraphPanel graphPanel) {
        super.show(graphPanel, e.getX(), e.getY());
    }
}
