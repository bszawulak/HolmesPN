package holmes.windows.xtpn;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.workspace.WorkspaceSheet;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class HolmesNodeInfoXTPNactions {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private JFrame parentFrame;

    public HolmesNodeInfoXTPNactions(JFrame parent) {
        parentFrame = parent;
    }


    public void buttonGammaModeSwitch(ActionEvent e, PlaceXTPN place, HolmesRoundedButton tokensWindowButton, HolmesRoundedButton gammaVisibilityButton) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        int tokensNum = place.getTokensNumber();
        if (place.isGammaModeActive()) {
            //zapytać czy wyłączyć, konwersja kasowanie arrayListy
            String[] options = {"Reduce to classical place", "Stay as XTPN"}; //Reduce to classical place, Stay as XTPN
            int answer = JOptionPane.showOptionDialog(null, "Turning \u03B3-mode off will clear " +
                            "all times of tokens ("+tokensNum+") and \nas a result place will have classical PN features."  +
                            "\nTransform XTPN place into classical place?",
                    "Transformation into classical place",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (answer == 0) { //redukcja do klasycznego miejsca
                button.setNewText("<html>Gamma: OFF</html>"); //Gamma: OFF
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                place.transformXTPNintoPNpace();
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                button.setFocusPainted(false);
                tokensWindowButton.setEnabled(false);
                gammaVisibilityButton.setEnabled(false);
            }
        } else {
            //zapytać czy wyłączyć, konwersja kasowanie arrayListy
            String[] options = {"Transform into XTPN", "Stay as classical PN place"}; //Transform into XTPN, Stay as classical PN place
            int answer = JOptionPane.showOptionDialog(null, "This will transform classical " +
                            "PN place into XTPN.\nAll tokens ("+tokensNum+") will be assigned 0.0 time values."  +
                            "\nTransform into XTPN place?",
                    "Conversion into XTPN place",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            //cancel
            if (answer == 0) { //transformacja PN -> XTPN
                button.setNewText("<html>Gamma: ON</html>"); //Gamma: ON
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                place.transformIntoXTPNplace();
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
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
            button.setNewText("<html>\u03B3: Hidden<html>"); //\u03B3: Hidden
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
        } else {
            place.setGammaRangeVisibility(true);
            button.setNewText("<html>\u03B3: Visible<html>"); //\u03B3: Visible
            button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
        }
        overlord.getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
    }

    /**
     * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
     */
    public void repaintGraphPanel(Node node) {
        int sheetIndex = overlord.IDtoIndex(node.getElementLocations().get(0).getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        graphPanel.repaint();
    }

    public void reselectElement(ElementLocation elementLocation) {
        WorkspaceSheet ws = overlord.getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }
}
