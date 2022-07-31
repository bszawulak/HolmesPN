package holmes.darkgui.dockwindows;

import holmes.darkgui.GUIManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;
import holmes.windows.xtpn.HolmesNodeInfoXTPN;
import holmes.workspace.WorkspaceSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SharedActionsXTPN {
    private static final SharedActionsXTPN singleton = new SharedActionsXTPN();
    private GUIManager overlord = GUIManager.getDefaultGUIManager();
    private SharedActionsXTPN() {
    }

    /**
     * Metoda dostępu do kontrolera.
     * @return (<b>GUIController</b>) obiekt kontrolera.
     */
    public static SharedActionsXTPN access() {
        return singleton;
    }



    public void buttonAlphaSwitchMode(ActionEvent e, TransitionXTPN transition, JFrame caller
            , JButton tauVisibilityButton, JButton buttonClassicMode, JFormattedTextField alphaMaxTextField, ElementLocation elementLocation) {
        JButton button = (JButton) e.getSource();
        if (transition.isAlphaActiveXTPN()) {
            if(transition.isBetaActiveXTPN() && transition.getBetaMin_xTPN() < overlord.simSettings.getCalculationsAccuracy()
                    && transition.getBetaMax_xTPN() < overlord.simSettings.getCalculationsAccuracy() ) {
                //czyli jeśli BETA=ON, ale bety są ustawione na zero
                if(transition.isInputTransition() || transition.isOutputTransition()) {
                    JOptionPane.showMessageDialog(null,
                            "Input or output XTPN transitions cannot be immediate. This transition" +
                                    "\nis not in Beta-mode or Beta time values are zero. Change impossible.",
                            "Immediate int/out XTPN transitions problem", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            transition.setAlphaXTPNstatus(false);
            if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                ((HolmesRoundedButton)button).setNewText("<html>Alpha: OFF</html>");
                ((HolmesRoundedButton)button).repaintBackground("bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
            } else { //jeśli z podokna Holmesa
                button.setText("Alfa: OFF");
                button.setBackground(Color.RED);
            }

            if(!transition.isBetaActiveXTPN()) {
                //jeśli jesteśmy w trybie XTPN, tutaj przechodzimy na klasyczną tranzycję
                if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                    ((HolmesRoundedButton)buttonClassicMode).setNewText("<html>Classical<html>");
                    ((HolmesRoundedButton)buttonClassicMode).repaintBackground("bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
                    ((HolmesRoundedButton)tauVisibilityButton).setEnabled(false);
                } else {
                    buttonClassicMode.setBackground(Color.GREEN);
                    tauVisibilityButton.setEnabled(false);
                }
                transition.setTauTimersVisibility(false);
            }
        } else {
            transition.setAlphaXTPNstatus(true);
            if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                ((HolmesRoundedButton)button).setNewText("<html>Alpha: ON</html>");
                ((HolmesRoundedButton)button).repaintBackground("bMpressed_1.png", "bMpressed_2.png", "bMpressed_3.png");
            } else { //jeśli z podokna Holmesa
                button.setText("Alfa: ON");
                button.setBackground(Color.GREEN);
            }
            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            if(transition.getAlphaMin_xTPN() < overlord.simSettings.getCalculationsAccuracy()
                    && transition.getAlphaMax_xTPN() < overlord.simSettings.getCalculationsAccuracy()) {
                transition.setAlphaMax_xTPN(1.0, false);
                alphaMaxTextField.setValue(0.0);
            }

            if(!transition.isBetaActiveXTPN()) { //jeśli był tryb klasyczny
                if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                    ((HolmesRoundedButton)buttonClassicMode).setNewText("<html>XTPN<html>");
                    ((HolmesRoundedButton)buttonClassicMode).repaintBackground("bMpressed_1.png", "bMpressed_2.png", "bMpressed_3.png");
                    ((HolmesRoundedButton)tauVisibilityButton).setEnabled(true);
                } else { //jeśli z podokna Holmesa
                    buttonClassicMode.setBackground(Color.RED);
                    tauVisibilityButton.setEnabled(true);
                }
                transition.setTauTimersVisibility(true);
            }
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }

    public void buttonBetaSwitchMode(ActionEvent e, TransitionXTPN transition, JFrame caller
            , JButton tauVisibilityButton, JButton buttonClassicMode, JFormattedTextField betaMaxTextField, ElementLocation elementLocation) {
        JButton button = (JButton) e.getSource();
        if (transition.isBetaActiveXTPN()) {
            double accuracy = overlord.simSettings.getCalculationsAccuracy();
            if(transition.isAlphaActiveXTPN() && transition.getAlphaMin_xTPN() < accuracy
                    && transition.getAlphaMax_xTPN() < accuracy ) {
                //czyli jeśli ALFA=ON, ale alfy są ustawione na zero
                if(transition.isInputTransition() || transition.isOutputTransition()) {
                    JOptionPane.showMessageDialog(null,
                            "Input or output XTPN transitions cannot be immediate. This transition" +
                                    "\nis not in Alfa-mode and Beta time values are zero. Change impossible.",
                            "Immediate int/out XTPN transitions problem", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            transition.setBetaXTPNstatus(false);

            if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                ((HolmesRoundedButton)button).setNewText("<html>Beta: OFF</html>");
                ((HolmesRoundedButton)button).repaintBackground("bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
            } else { //jeśli z podokna Holmesa
                button.setText("Beta: OFF");
                button.setBackground(Color.RED);
            }

            if(!transition.isAlphaActiveXTPN()) {
                //jeśli jesteśmy w trybie XTPN, tutaj przechodzimy na klasyczną tranzycję
                if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                    ((HolmesRoundedButton)button).setNewText("<html>Classical<html>");
                    ((HolmesRoundedButton)buttonClassicMode).repaintBackground("bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
                    ((HolmesRoundedButton)tauVisibilityButton).setEnabled(false);
                } else { //jeśli z podokna Holmesa
                    buttonClassicMode.setBackground(Color.GREEN);
                    tauVisibilityButton.setEnabled(false);
                }
                transition.setTauTimersVisibility(false);
            }
        } else {
            transition.setBetaXTPNstatus(true);
            if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                ((HolmesRoundedButton)button).setNewText("<html>Beta: ON</html>");
                ((HolmesRoundedButton)button).repaintBackground("bMpressed_1.png", "bMpressed_2.png", "bMpressed_3.png");
            } else { //jeśli z podokna Holmesa
                button.setText("Beta: ON");
                button.setBackground(Color.GREEN);
            }
            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            double accuracy = overlord.simSettings.getCalculationsAccuracy();
            if(transition.getBetaMin_xTPN() < accuracy && transition.getBetaMax_xTPN() < accuracy) {
                transition.setBetaMax_xTPN(1.0, false);
                betaMaxTextField.setValue(0.0);
            }

            if(!transition.isAlphaActiveXTPN()) { //jeśli był tryb klasyczny
                if(caller instanceof HolmesNodeInfoXTPN) { //jeśli wywołanie przyszło w okna informacji o tranzycji
                    ((HolmesRoundedButton)buttonClassicMode).setNewText("<html>XTPN<html>");
                    ((HolmesRoundedButton)buttonClassicMode).repaintBackground("bMpressed_1.png", "bMpressed_2.png", "bMpressed_3.png");
                    ((HolmesRoundedButton)tauVisibilityButton).setEnabled(true);
                } else {
                    tauVisibilityButton.setEnabled(true);
                    buttonClassicMode.setBackground(Color.RED);
                }
                transition.setTauTimersVisibility(true);
            }
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }

    public void buttonTransitionToXTPN_classicSwitchMode(ActionEvent e, TransitionXTPN transition, JFrame caller
            , JFormattedTextField alphaMaxTextField, JFormattedTextField betaMaxTextField, ElementLocation elementLocation) {
        JButton button = (JButton) e.getSource();
        if (transition.isAlphaActiveXTPN() || transition.isBetaActiveXTPN()) {

            Object[] options = {"Confirm", "Cancel",};
            int n = JOptionPane.showOptionDialog(null,
                    "Reduce XTPN transition into classical one? This can reset" +
                            "\ncurrent time values assigned to Alfa/Beta ranges.",
                    "XTPN transition reduction", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            if (n == 1) {
                return;
            }

            //jeśli jesteśmy w trybie XTPN, tutaj przechodzimy na klasyczną tranzycję
            transition.setAlphaXTPNstatus(false);
            transition.setBetaXTPNstatus(false);
            transition.setTauTimersVisibility(false);
            button.setBackground(Color.RED);
        } else { //wychodzimy z trybu klasycznego
            transition.setAlphaXTPNstatus(true);
            transition.setBetaXTPNstatus(true);
            transition.setTauTimersVisibility(true);
            button.setBackground(Color.GREEN);

            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            double accuracy = overlord.simSettings.getCalculationsAccuracy();
            if(transition.getAlphaMin_xTPN() < accuracy && transition.getAlphaMax_xTPN() < accuracy) {
                transition.setAlphaMax_xTPN(1.0, false);
                alphaMaxTextField.setValue(0.0);
            }
            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            if(transition.getBetaMin_xTPN() < accuracy && transition.getBetaMax_xTPN() < accuracy) {
                transition.setBetaMax_xTPN(1.0, false);
                betaMaxTextField.setValue(0.0);
            }
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }









    /**
     * Metoda ustawia nową wartość czasu alfaMinimum dla tranzycji XTPN.
     * @param newAlphaMin (double) nowa wartość alfaMinimum.
     * @return (true) jeżeli zmiana wartości się udała.
     */
    public boolean setAlfaMinTime(double newAlphaMin, TransitionXTPN transition, ElementLocation elementLocation) {
        double alfaMax = transition.getAlphaMax_xTPN();
        if(newAlphaMin > alfaMax) {
            //String[] options = {"Increase \u03B1(max) to \u03B1(min)", "Decrease \u03B1(min) to \u03B1(max)", "Cancel"};
            String[] options = {"\u25B2 Increase \u03B1-max to "+newAlphaMin, "\u25BC Decrease \u03B1-min to "+alfaMax, "\u274C Cancel"};
            int answer = JOptionPane.showOptionDialog(null, "Proposed value \u03B1-min = " +
                            newAlphaMin + " cannot be higher than current \u03B1-max = " + alfaMax +
                            "\nIncrease old alphaMaximum (default action) or decrease new alphaMinimum?",
                    "Alpha range problem: \u03B1-min too high",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            switch(answer) {
                case 0: //powiększenie alphaMax do nowego alphaMin
                    transition.setAlphaMax_xTPN(newAlphaMin, true);
                    transition.setAlphaMin_xTPN(newAlphaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //redukcja nowego alphaMin do aktualnego alphaMax
                    transition.setAlphaMin_xTPN(alfaMax, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setAlphaMin_xTPN(newAlphaMin, false);
        repaintGraphPanel(elementLocation);

        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu alfaMaximum dla tranzycji XTPN.
     * @param newAlphaMax (double) nowa wartość alfaMaximum.
     * @return (true) jeżeli zmiana wartości się udała.
     */
    public boolean setAlfaMaxTime(double newAlphaMax, TransitionXTPN transition, ElementLocation elementLocation) {
        double alfaMin = transition.getAlphaMin_xTPN();

        //tranzycje wejściowe i wyjściowe nie mogą być XTPN z zerami, tylko klasycznymi
        double accuracy = overlord.simSettings.getCalculationsAccuracy();
        if(alfaMin < accuracy && newAlphaMax < accuracy &&
                ( ( transition.getBetaMin_xTPN() < accuracy && transition.getBetaMax_xTPN() < accuracy )
                        || !transition.isBetaActiveXTPN() ) ) { //albo bety są na zera, albo w ogóle tryb beta wyłączony
            if(transition.isInputTransition() || transition.isOutputTransition()) {
                JOptionPane.showMessageDialog(null,
                        "Input or output XTPN transitions cannot be immediate. Alternatively" +
                                "\nturn off both Alfa and Beta modes for a classical immediate transition.",
                        "Immediate int/out XTPN transitions problem", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        //jeśli alfaMax=alfaMin=0, to Alfa=OFF
        if(alfaMin < accuracy && newAlphaMax < accuracy) { //jeśli zero
            transition.setAlphaMax_xTPN(0.0, false);
            transition.setAlphaXTPNstatus(false);
            repaintGraphPanel(elementLocation);
            return true;
        }

        if(newAlphaMax < alfaMin) {
            //String[] options = {"Increase \u03B1(max) to \u03B1(min)", "Decrease \u03B1(min) to \u03B1(max)", "Cancel"};
            String[] options = {"\u25B2 Increase \u03B1-max to "+alfaMin, "\u25BC Decrease \u03B1-min "+newAlphaMax, "\u274C Cancel"};
            int answer = JOptionPane.showOptionDialog(null, "Proposed value \u03B1-max = " +
                            newAlphaMax + " cannot be lower than current \u03B1-min = " + alfaMin +
                            "\nIncrease new alphaMaximum or decrease old alphaMinimum (default action)?",
                    "Alpha range problem: \u03B1-max too low",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            switch(answer) {
                case 0: //powiększenie nowego alphaMax do starej wartości alphaMin
                    transition.setAlphaMax_xTPN(alfaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //zmniejszenie starego alphaMin do nowego alphaMax
                    transition.setAlphaMin_xTPN(newAlphaMax, true);
                    transition.setAlphaMax_xTPN(newAlphaMax, false);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setAlphaMax_xTPN(newAlphaMax, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu betaMinimum dla tranzycji XTPN.
     * @param newBetaMin (double) nowa wartość betaMinimum.
     * @return (true) jeżeli zmiana wartości się udała.
     */
    public boolean setBetaMinTime(double newBetaMin, TransitionXTPN transition, ElementLocation elementLocation) {
        double betaMax = transition.getBetaMax_xTPN();
        if(newBetaMin > betaMax) {
            //String[] options = {"Increase \u03B2(max) to \u03B2(min)", "Decrease \u03B2(min) to \u03B2(max)", "Cancel"};
            String[] options = {"\u25B2 Increase \u03B2-max to "+newBetaMin, "\u25BC Decrease \u03B2-min to "+betaMax, "\u274C Cancel"};
            int answer = JOptionPane.showOptionDialog(null, "Proposed value \u03B2-min = " +
                            newBetaMin + " cannot be higher than current \u03B2-max = " + betaMax +
                            "\nIncrease old betaMaximum (default action) or decrease new betaMinimum?",
                    "Beta range problem: \u03B2-min too high",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            switch(answer) {
                case 0: //powiększenie betaMax do nowego betaMin
                    transition.setBetaMax_xTPN(newBetaMin, true);
                    transition.setBetaMin_xTPN(newBetaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //redukcja nowego betaMin do aktualnego betaMax
                    transition.setBetaMin_xTPN(betaMax, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setBetaMin_xTPN(newBetaMin, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu betaMaximum dla tranzycji XTPN.
     * @param newBetaMax (double) nowa wartość betaMaximum.
     * @return (true) jeżeli zmiana wartości się udała.
     */
    public boolean setBetaMaxTime(double newBetaMax, TransitionXTPN transition, ElementLocation elementLocation) {
        double betaMin = transition.getBetaMin_xTPN();

        double accuracy = overlord.simSettings.getCalculationsAccuracy();
        if(betaMin < accuracy && newBetaMax < accuracy &&
                ( ( transition.getAlphaMin_xTPN() < accuracy && transition.getAlphaMax_xTPN() < accuracy)
                        || !transition.isAlphaActiveXTPN() ) ) { //albo alfy są na zero, albo cały tryb alfa jest wyłączony
            boolean input = transition.isInputTransition();
            boolean output = transition.isOutputTransition();
            if( input || output ) {
                JOptionPane.showMessageDialog(null,
                        "Input or output XTPN transitions cannot be immediate. Alternatively" +
                                "\nturn off both Alfa and Beta modes for a classical immediate transition.",
                        "Immediate int/out XTPN transitions problem", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        //jeśli betaMax=betaMin=0, to Beta=OFF
        if(betaMin < accuracy && newBetaMax < accuracy) { //jeśli zero
            transition.setBetaMax_xTPN(0.0, false);
            transition.setBetaXTPNstatus(false);
            repaintGraphPanel(elementLocation);
            return true;
        }

        if(newBetaMax < betaMin) {
            //String[] options = {"Increase \u03B2(max) to \u03B2(min)", "Decrease \u03B2(min) to \u03B2(max)", "Cancel"};
            String[] options = {"\u25B2 Increase \u03B2-max to "+betaMin, "\u25BC Decrease \u03B2-min to "+newBetaMax, "\u274C Cancel"};
            int answer = JOptionPane.showOptionDialog(null, "Proposed value \u03B2-max = " +
                            newBetaMax + " cannot be lower than current \u03B2-min = " + betaMin +
                            "\nIncrease new betaMax or decrease old betaMin (default action)?",
                    "Beta range problem: \u03B2-max too low",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            switch(answer) {
                case 0: //powiększenie nowego betaMax do starej wartości betaMin
                    transition.setBetaMax_xTPN(betaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //zmniejszenie starego betaMin do nowego betaMax
                    transition.setBetaMin_xTPN(newBetaMax, true);
                    transition.setBetaMax_xTPN(newBetaMax, false);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setBetaMax_xTPN(newBetaMax, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    public boolean setGammaMinimumTime(double newGammaMin, PlaceXTPN place, ElementLocation elementLocation) {
        //PlaceXTPN place = (PlaceXTPN) element;
        double gammaMax = place.getGammaMax_xTPN();
        if(newGammaMin > gammaMax) {
            // String[] options = {"Increase \u03B3(max) to \u03B3(min)", "Decrease \u03B3(min) to \u03B3(max)", "Cancel"};
            String[] options = {"\u25B2 Increase \u03B3-max to "+newGammaMin, "\u25BC Decrease \u03B3-min to "+gammaMax, "\u274C Cancel"};
            int answer = JOptionPane.showOptionDialog(null, "Proposed value \u03B3-min = " +
                            newGammaMin + " cannot be higher than current \u03B3-max = " + gammaMax +
                            "\nIncrease current gammaMaximum (default action) or decrease proposed gammaMinimum?",
                    "Gamma range problem: \u03B3-min too high",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            switch(answer) {
                case 0: //powiększenie gMax do nowego gMin
                    place.setGammaMax_xTPN(newGammaMin, true);
                    place.setGammaMin_xTPN(newGammaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //redukcja nowego gMin do aktualnego gMax
                    place.setGammaMin_xTPN(gammaMax, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        place.setGammaMin_xTPN(newGammaMin, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu gammaMaximum dla miejsca XTPN.
     * @param newGammaMax (double) nowa wartość gammaMaximum.
     * @return (true) jeżeli zmiana wartości się udała.
     */
    public boolean setMaxGammaTime(double newGammaMax, PlaceXTPN place, ElementLocation elementLocation) {
        //PlaceXTPN place = (PlaceXTPN) element;
        double gammaMin = place.getGammaMin_xTPN();
        if(newGammaMax < gammaMin) {
            // String[] options = {"Increase \u03B3(max) to \u03B3(min)", "Decrease \u03B3(min) to \u03B3(max)", "Cancel"};
            String[] options = {"\u25B2 Increase \u03B3-max to "+gammaMin, "\u25BC Decrease \u03B3-min to "+newGammaMax, "\u274C Cancel"};
            int answer = JOptionPane.showOptionDialog(null, "Proposed value \u03B3-max = " +
                            newGammaMax + " cannot be lower than current \u03B3-min = " + gammaMin +
                            "\nIncrease proposed gammaMaximum or decrease current gammaMinimum (default action)?",
                    "Gamma range problem: \u03B3-max too low",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
            switch(answer) {
                case 0: //powiększenie nowego gammaMax do starej wartości gammaMin
                    place.setGammaMax_xTPN(gammaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //zmniejszenie starego gammaMin do nowego gammaMax
                    place.setGammaMin_xTPN(newGammaMax, true);
                    place.setGammaMax_xTPN(newGammaMax, false);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        place.setGammaMax_xTPN(newGammaMax, false);
        repaintGraphPanel(elementLocation);

        return true;
    }

    /**
     * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
     */
    private void repaintGraphPanel(ElementLocation elementLocation) {
        int sheetIndex = overlord.IDtoIndex(elementLocation.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        graphPanel.repaint();
    }
}
