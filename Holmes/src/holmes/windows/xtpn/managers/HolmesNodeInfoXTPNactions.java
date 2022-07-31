package holmes.windows.xtpn.managers;

import holmes.darkgui.GUIManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;
import holmes.workspace.WorkspaceSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HolmesNodeInfoXTPNactions {
    private JFrame parentFrame;
    private GUIManager overlord;

    public HolmesNodeInfoXTPNactions(JFrame parent) {
        parentFrame = parent;
        overlord = GUIManager.getDefaultGUIManager();
    }


    public void buttonGammaModeSwitch(ActionEvent e, PlaceXTPN place, HolmesRoundedButton tokensWindowButton, HolmesRoundedButton gammaVisibilityButton) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        int tokensNum = place.getTokensNumber();
        if (place.isGammaModeActiveXTPN()) {
            //zapytać czy wyłączyć, konwersja kasowanie arrayListy
            String[] options = {"Reduce to classical place", "Stay as XTPN"};
            int answer = JOptionPane.showOptionDialog(null, "Turning \u03B3-mode off will clear " +
                            "all times of tokens ("+tokensNum+") and \nas a result place will have classical PN features."  +
                            "\nTransform XTPN place into classical place?",
                    "Transformation into classical place",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (answer == 0) { //redukcja do klasycznego miejsca
                button.setNewText("<html>Gamma: OFF</html>");
                button.repaintBackground("bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
                place.transformXTPNintoPNpace();
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
                button.setFocusPainted(false);
                tokensWindowButton.setEnabled(false);
            }
        } else {
            //zapytać czy wyłączyć, konwersja kasowanie arrayListy
            String[] options = {"Transform into XTPN", "Stay as classical PN place"};
            int answer = JOptionPane.showOptionDialog(null, "This will transform classical " +
                            "PN place into XTPN.\nAll tokens ("+tokensNum+") will be assigned 0.0 time values."  +
                            "\nTransform into XTPN place?",
                    "Conversion into XTPN place",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            //cancel
            if (answer == 0) { //transformacja PN -> XTPN
                button.setNewText("<html>Gamma: ON</html>");
                button.repaintBackground("bMpressed_1.png", "bMpressed_2.png", "bMpressed_3.png");
                place.transformIntoXTPNplace();
                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
                button.setFocusPainted(false);
                tokensWindowButton.setEnabled(true);
                gammaVisibilityButton.setEnabled(true);
            }
        }
    }

    public void gammaVisButtonSwitch(ActionEvent e, PlaceXTPN place) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        if (place.isGammaRangeVisible()) {
            place.setGammaRangeVisibility(false);
            button.setNewText("<html>\u03B3: Hidden<html>");
            button.repaintBackground("bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
            GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
        } else {
            place.setGammaRangeVisibility(true);
            button.setNewText("<html>\u03B3: Visible<html>");
            button.repaintBackground("bMpressed_1.png", "bMpressed_2.png", "bMpressed_3.png");
            GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
    }

    /**
     * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
     */
    public void repaintGraphPanel(Node node) {
        int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(node.getElementLocations().get(0).getSheetID());
        GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        graphPanel.repaint();
    }

    public void reselectElement(ElementLocation elementLocation) {
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }
}
