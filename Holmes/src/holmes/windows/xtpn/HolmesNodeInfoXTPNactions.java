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
            String[] options = {lang.getText("HNIXTPNAwin_entry001op1"), lang.getText("HNIXTPNAwin_entry001op2")}; //Reduce to classical place, Stay as XTPN
            int answer = JOptionPane.showOptionDialog(null, lang.getText("HNIXTPNAwin_entry001a")+tokensNum
                            +lang.getText("HNIXTPNAwin_entry001b"),
                    lang.getText("HNIXTPNAwin_entry001t"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            if (answer == 0) { //redukcja do klasycznego miejsca
                button.setNewText(lang.getText("HNIXTPNAwin_entry002")); //Gamma: OFF
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                place.transformXTPNintoPNpace();
                overlord.getWorkspace().getProject().repaintAllGraphPanels();
                button.setFocusPainted(false);
                tokensWindowButton.setEnabled(false);
                gammaVisibilityButton.setEnabled(false);
            }
        } else {
            //zapytać czy wyłączyć, konwersja kasowanie arrayListy
            String[] options = {lang.getText("HNIXTPNAwin_entry003op1"), lang.getText("HNIXTPNAwin_entry003op2")}; //Transform into XTPN, Stay as classical PN place
            int answer = JOptionPane.showOptionDialog(null, lang.getText("HNIXTPNAwin_entry003a")
                            +tokensNum+lang.getText("HNIXTPNAwin_entry003b"),
                    lang.getText("HNIXTPNAwin_entry003t"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            //cancel
            if (answer == 0) { //transformacja PN -> XTPN
                button.setNewText(lang.getText("HNIXTPNAwin_entry004")); //Gamma: ON
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
            button.setNewText(lang.getText("HNIXTPNAwin_entry005invis")); //\u03B3: Hidden
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            overlord.getWorkspace().getProject().repaintAllGraphPanels();
            button.setFocusPainted(false);
        } else {
            place.setGammaRangeVisibility(true);
            button.setNewText(lang.getText("HNIXTPNAwin_entry005vis")); //\u03B3: Visible
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
