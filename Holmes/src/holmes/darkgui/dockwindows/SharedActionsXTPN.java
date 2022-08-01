package holmes.darkgui.dockwindows;

import holmes.darkgui.GUIManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.elements.TransitionXTPN;
import holmes.workspace.WorkspaceSheet;

import javax.swing.*;
import java.awt.event.ActionEvent;

@SuppressWarnings({"unused", "UnusedReturnValue"})
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

    /**
     * Metoda przełącza status trybu Alfa dla tranzycji. Używana przez okno główne oraz okno informacji o tranzycji.
     * @param e (<b>ActionEvent</b>) obiekt zdarzenia wywołującego.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji.
     * @param caller (<b>JFrame</b>) obiekt okna wywołującego (null dla głównego okna programu).
     * @param tauVisibilityButton (<b>HolmesRoundedButton</b>) obiekt przycisku tau.
     * @param buttonClassicMode (<b>HolmesRoundedButton</b>) obiekt przycisku przełączania trybu klasczynego/XTPN.
     * @param alphaMaxTextField (<b>JFormattedTextField</b>) obiekt pola tekstowego wartości alpha-minimum.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętej tranzycji.
     */
    public void buttonAlphaSwitchMode(ActionEvent e, TransitionXTPN transition, JFrame caller
            , HolmesRoundedButton tauVisibilityButton, HolmesRoundedButton buttonClassicMode
            , JFormattedTextField alphaMaxTextField, ElementLocation elementLocation) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        if (transition.isAlphaModeActive()) {
            if(transition.isBetaModeActive() && transition.getBetaMinValue() < overlord.simSettings.getCalculationsAccuracy()
                    && transition.getBetaMaxValue() < overlord.simSettings.getCalculationsAccuracy() ) {
                //czyli jeśli BETA=ON, ale bety są ustawione na zero
                if(transition.isInputTransition() || transition.isOutputTransition()) {
                    JOptionPane.showMessageDialog(null,
                            "Input or output XTPN transitions cannot be immediate. This transition" +
                                    "\nis not in Beta-mode or Beta time values are zero. Change impossible.",
                            "Immediate int/out XTPN transitions problem", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            transition.setAlphaModeStatus(false);
            button.setNewText("<html>Alpha: OFF</html>");
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            if(!transition.isBetaModeActive()) {
                //jeśli jesteśmy w trybie XTPN, tutaj przechodzimy na klasyczną tranzycję
                buttonClassicMode.setNewText("<html>Classical<html>");
                buttonClassicMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                tauVisibilityButton.setEnabled(false);

                transition.setTauTimersVisibility(false);
            }
        } else {
            transition.setAlphaModeStatus(true);
            button.setNewText("<html>Alpha: ON</html>");
            button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            if(transition.getAlphaMinValue() < overlord.simSettings.getCalculationsAccuracy()
                    && transition.getAlphaMaxValue() < overlord.simSettings.getCalculationsAccuracy()) {
                transition.setAlphaMaxValue(1.0, false);
                alphaMaxTextField.setValue(0.0);
            }

            if(!transition.isBetaModeActive()) { //jeśli był tryb klasyczny
                buttonClassicMode.setNewText("<html>XTPN<html>");
                buttonClassicMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                tauVisibilityButton.setEnabled(true);
                transition.setTauTimersVisibility(true);
            }
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }

    /**
     * Metoda przełącza status trybu Beta dla tranzycji. Używana przez okno główne oraz okno informacji o tranzycji.
     * @param e (<b>ActionEvent</b>) obiekt zdarzenia wywołującego.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji.
     * @param caller (<b>JFrame</b>) obiekt okna wywołującego (null dla głównego okna programu).
     * @param tauVisibilityButton (<b>HolmesRoundedButton</b>) obiekt przycisku tau.
     * @param buttonClassicMode (<b>HolmesRoundedButton</b>) obiekt przycisku przełączania trybu klasczynego/XTPN.
     * @param betaMaxTextField (<b>JFormattedTextField</b>) obiekt pola tekstowego wartości beta-maximum.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętej tranzycji.
     */
    public void buttonBetaSwitchMode(ActionEvent e, TransitionXTPN transition, JFrame caller
            , HolmesRoundedButton tauVisibilityButton, HolmesRoundedButton buttonClassicMode
            , JFormattedTextField betaMaxTextField, ElementLocation elementLocation) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        if (transition.isBetaModeActive()) {
            double accuracy = overlord.simSettings.getCalculationsAccuracy();
            if(transition.isAlphaModeActive() && transition.getAlphaMinValue() < accuracy
                    && transition.getAlphaMaxValue() < accuracy ) {
                //czyli jeśli ALFA=ON, ale alfy są ustawione na zero
                if(transition.isInputTransition() || transition.isOutputTransition()) {
                    JOptionPane.showMessageDialog(null,
                            "Input or output XTPN transitions cannot be immediate. This transition" +
                                    "\nis not in Alfa-mode and Beta time values are zero. Change impossible.",
                            "Immediate int/out XTPN transitions problem", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            transition.setBetaModeStatus(false);
            button.setNewText("<html>Beta: OFF</html>");
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            if(!transition.isAlphaModeActive()) {
                //jeśli jesteśmy w trybie XTPN, tutaj przechodzimy na klasyczną tranzycję
                button.setNewText("<html>Classical<html>");
                buttonClassicMode.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
                tauVisibilityButton.setEnabled(false);
                transition.setTauTimersVisibility(false);
            }
        } else {
            transition.setBetaModeStatus(true);
            button.setNewText("<html>Beta: ON</html>");
            button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            double accuracy = overlord.simSettings.getCalculationsAccuracy();
            if(transition.getBetaMinValue() < accuracy && transition.getBetaMaxValue() < accuracy) {
                transition.setBetaMaxValue(1.0, false);
                betaMaxTextField.setValue(0.0);
            }

            if(!transition.isAlphaModeActive()) { //jeśli był tryb klasyczny
                buttonClassicMode.setNewText("<html>XTPN<html>");
                buttonClassicMode.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
                tauVisibilityButton.setEnabled(true);
                transition.setTauTimersVisibility(true);
            }
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }

    /**
     * Metoda przełącza status trybu klasycznego/XTPN dla tranzycji. Używana przez okno główne oraz okno informacji o tranzycji.
     * @param e (<b>ActionEvent</b>) obiekt zdarzenia wywołującego.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji.
     * @param caller (<b>JFrame</b>) obiekt okna wywołującego (null dla głównego okna programu).
     * @param alphaMaxTextField (<b>JFormattedTextField</b>) obiekt pola tekstowego wartości alpha-maximum.
     * @param betaMaxTextField (<b>JFormattedTextField</b>) obiekt pola tekstowego wartości beta-maximum.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętej tranzycji.
     */
    public void buttonTransitionToXTPN_classicSwitchMode(ActionEvent e, TransitionXTPN transition, JFrame caller
            , JFormattedTextField alphaMaxTextField, JFormattedTextField betaMaxTextField, ElementLocation elementLocation) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        if (transition.isAlphaModeActive() || transition.isBetaModeActive()) {

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
            transition.setAlphaModeStatus(false);
            transition.setBetaModeStatus(false);
            transition.setTauTimersVisibility(false);

            button.setNewText("<html>Classical<html>");
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            //button.setBackground(Color.RED);
        } else { //wychodzimy z trybu klasycznego
            transition.setAlphaModeStatus(true);
            transition.setBetaModeStatus(true);
            transition.setTauTimersVisibility(true);

            button.setNewText("<html>XTPN<html>");
            button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            //button.setBackground(Color.GREEN);

            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            double accuracy = overlord.simSettings.getCalculationsAccuracy();
            if(transition.getAlphaMinValue() < accuracy && transition.getAlphaMaxValue() < accuracy) {
                transition.setAlphaMaxValue(1.0, false);
                alphaMaxTextField.setValue(0.0);
            }
            //jeśli obie wartości są na zerze i włączamy tryb Alfa, przywróć zakres [0,1]
            if(transition.getBetaMinValue() < accuracy && transition.getBetaMaxValue() < accuracy) {
                transition.setBetaMaxValue(1.0, false);
                betaMaxTextField.setValue(0.0);
            }
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);
    }

    /**
     * Metoda przełącza status trybu Gamma dla miejsca. Używana przez okno główne oraz okno informacji o tranzycji.
     * @param e (<b>ActionEvent</b>) obiekt zdarzenia wywołującego.
     * @param place (<b>PlaceXTPN</b>) obiekt miejsca.
     * @param caller (<b>JFrame</b>) obiekt okna wywołującego (null dla głównego okna programu).
     * @param gammaVisibilityButton (<b>HolmesRoundedButton</b>) obiekt przycisku widoczności zakresu gamma.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego miejsca.
     */
    public void buttonGammaSwitchMode(ActionEvent e, PlaceXTPN place, JFrame caller, HolmesRoundedButton gammaVisibilityButton, ElementLocation elementLocation) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        int tokensNum = place.getTokensNumber();
        if (place.isGammaModeActive()) {
            //zapytać czy wyłączyć, konwersja kasowanie arrayListy
            String[] options = {"Reduce to classical place", "Stay as XTPN"};
            int answer = JOptionPane.showOptionDialog(null, "Turning \u03B3-mode off will clear " +
                            "all times of tokens ("+tokensNum+") and \nas a result place will have classical PN features."  +
                            "\nTransform XTPN place into classical place?",
                    "Transformation into classical place",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            //cancel
            if (answer == 0) { //redukcja do klasycznego miejsca
                button.setNewText("<html><center>Gamma<br>OFF</center></html>");
                button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

                place.transformXTPNintoPNpace();

                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
                button.setFocusPainted(false);
                WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);

                gammaVisibilityButton.setEnabled(false);
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
                button.setNewText("<html><center>Gamma<br>ON</center></html>");
                button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

                place.transformIntoXTPNplace();

                GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
                button.setFocusPainted(false);
                WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
                ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);

                gammaVisibilityButton.setEnabled(true);
            }
        }
    }

    /**
     * Obsługa przełącznika widoczności zakresu alfa nad tranzycją.
     * @param e (<b>ActionEvent</b>) obiekt zdarzenia wywołującego.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji XTPN.
     * @param alphaLocChangeButton (<b>HolmesRoundedButton</b>) przycisk zmiany lokalizacji zakresów alfa.
     * @param alphaLocChangeMode (<b>boolean</b>) jeśli true, to znaczy, że kliknięto tryb zmiany lokalizacji w oknie głównym.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętej tranzycji.
     * @return (<b>boolean</b>) alphaLocChangeMode
     */
    public boolean alphaVisButtonAction(ActionEvent e, TransitionXTPN transition, HolmesRoundedButton alphaLocChangeButton
            , boolean alphaLocChangeMode, ElementLocation elementLocation) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        if (transition.isAlphaRangeVisible()) { //wyłączamy
            transition.setAlphaRangeVisibility(false);
            button.setNewText("<html>\u03B1:hidden<html>");
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            alphaLocChangeButton.setEnabled(false);
        } else { // włączamy
            transition.setAlphaRangeVisibility(true);
            button.setNewText("<html>\u03B1:visible<html>");
            button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            alphaLocChangeMode = false;
            overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);

        return alphaLocChangeMode;
    }

    /**
     * Obsługa przełącznika widoczności zakresu beta nad tranzycją.
     * @param e (<b>ActionEvent</b>) obiekt zdarzenia wywołującego.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji XTPN.
     * @param betaLocChangeButton (<b>HolmesRoundedButton</b>) przycisk zmiany lokalizacji zakresów beta.
     * @param betaLocChangeMode (<b>boolean</b>) jeśli true, to znaczy, że kliknięto tryb zmiany lokalizacji w oknie głównym.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętej tranzycji.
     * @return (<b>boolean</b>) alphaLocChangeMode
     */
    public boolean betaVisButtonAction(ActionEvent e, TransitionXTPN transition, HolmesRoundedButton betaLocChangeButton
            , boolean betaLocChangeMode, ElementLocation elementLocation) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        if (transition.isBetaRangeVisible()) { //wyłączamy
            transition.setBetaRangeVisibility(false);
            button.setNewText("<html>\u03B2:hidden<html>");
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");

            betaLocChangeButton.setEnabled(false);
        } else { //włączamy
            transition.setBetaRangeVisibility(true);
            button.setNewText("<html>\u03B2:visible<html>");
            button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");

            betaLocChangeMode = false;
            overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);

        return betaLocChangeMode;
    }

    /**
     * Obsługa przełącznika widoczności czasów tau nad tranzycją.
     * @param e (<b>ActionEvent</b>) obiekt zdarzenia wywołującego.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji XTPN.
     * @param tauLocChangeButton (<b>HolmesRoundedButton</b>) przycisk zmiany lokalizacji czasów tau.
     * @param tauLocChangeMode (<b>boolean</b>) jeśli true, to znaczy, że kliknięto tryb zmiany lokalizacji w oknie głównym.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętej tranzycji.
     * @return (<b>boolean</b>) alphaLocChangeMode
     */
    public boolean tauVisButtonAction(ActionEvent e, TransitionXTPN transition, HolmesRoundedButton tauLocChangeButton
            , boolean tauLocChangeMode, ElementLocation elementLocation) {
        HolmesRoundedButton button = (HolmesRoundedButton) e.getSource();
        if (transition.isTauTimerVisible()) { //wyłączamy
            transition.setTauTimersVisibility(false);
            button.setNewText("<html>\u03C4:hidden<html>");
            button.repaintBackground("amber_bH1_neutr.png", "jade_bH2_hover.png", "jade_bH3_press.png");
            tauLocChangeButton.setEnabled(false);
        } else { //włączamy
            transition.setTauTimersVisibility(true);
            button.setNewText("<html>\u03C4:visible<html>");
            button.repaintBackground("jade_bH1_neutr.png", "amber_bH2_hover.png", "amber_bH3_press.png");
            tauLocChangeMode = false;
            overlord.setNameLocationChangeMode(null, null, GUIManager.locationMoveType.NONE);
        }
        GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
        button.setFocusPainted(false);
        WorkspaceSheet ws = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0);
        ws.getGraphPanel().getSelectionManager().selectOneElementLocation(elementLocation);

        return tauLocChangeMode;
    }

    /**
     * Metoda ustawia nową wartość czasu alfaMinimum dla tranzycji XTPN.
     * @param newAlphaMin (<b>double</b>>) nowa wartość alfaMinimum.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji XTPN.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego węzła tranzycji.
     * @return (<b>boolean</b>) - true, jeżeli zmiana wartości się udała.
     */
    public boolean setAlfaMinTime(double newAlphaMin, TransitionXTPN transition, ElementLocation elementLocation) {
        double alfaMax = transition.getAlphaMaxValue();
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
                    transition.setAlphaMaxValue(newAlphaMin, true);
                    transition.setAlphaMinValue(newAlphaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //redukcja nowego alphaMin do aktualnego alphaMax
                    transition.setAlphaMinValue(alfaMax, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setAlphaMinValue(newAlphaMin, false);
        repaintGraphPanel(elementLocation);

        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu alfaMaximum dla tranzycji XTPN.
     * @param newAlphaMax (<b>double</b>) nowa wartość alfaMaximum.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji XTPN.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego węzła tranzycji.
     * @return (<b>boolean</b>) - true, jeżeli zmiana wartości się udała.
     */
    public boolean setAlfaMaxTime(double newAlphaMax, TransitionXTPN transition, ElementLocation elementLocation) {
        double alfaMin = transition.getAlphaMinValue();

        //tranzycje wejściowe i wyjściowe nie mogą być XTPN z zerami, tylko klasycznymi
        double accuracy = overlord.simSettings.getCalculationsAccuracy();
        if(alfaMin < accuracy && newAlphaMax < accuracy &&
                ( ( transition.getBetaMinValue() < accuracy && transition.getBetaMaxValue() < accuracy )
                        || !transition.isBetaModeActive() ) ) { //albo bety są na zera, albo w ogóle tryb beta wyłączony
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
            transition.setAlphaMaxValue(0.0, false);
            transition.setAlphaModeStatus(false);
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
                    transition.setAlphaMaxValue(alfaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //zmniejszenie starego alphaMin do nowego alphaMax
                    transition.setAlphaMinValue(newAlphaMax, true);
                    transition.setAlphaMaxValue(newAlphaMax, false);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setAlphaMaxValue(newAlphaMax, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu betaMinimum dla tranzycji XTPN.
     * @param newBetaMin (<b>double</b>) nowa wartość betaMinimum.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji XTPN.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego węzła tranzycji.
     * @return (<b>boolean</b>) - true, jeżeli zmiana wartości się udała.
     */
    public boolean setBetaMinTime(double newBetaMin, TransitionXTPN transition, ElementLocation elementLocation) {
        double betaMax = transition.getBetaMaxValue();
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
                    transition.setBetaMaxValue(newBetaMin, true);
                    transition.setBetaMinValue(newBetaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //redukcja nowego betaMin do aktualnego betaMax
                    transition.setBetaMinValue(betaMax, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setBetaMinValue(newBetaMin, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu betaMaximum dla tranzycji XTPN.
     * @param newBetaMax (<b>double</b>) nowa wartość betaMaximum.
     * @param transition (<b>TransitionXTPN</b>) obiekt tranzycji XTPN.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego węzła tranzycji.
     * @return (<b>boolean</b>) - true, jeżeli zmiana wartości się udała.
     */
    public boolean setBetaMaxTime(double newBetaMax, TransitionXTPN transition, ElementLocation elementLocation) {
        double betaMin = transition.getBetaMinValue();

        double accuracy = overlord.simSettings.getCalculationsAccuracy();
        if(betaMin < accuracy && newBetaMax < accuracy &&
                ( ( transition.getAlphaMinValue() < accuracy && transition.getAlphaMaxValue() < accuracy)
                        || !transition.isAlphaModeActive() ) ) { //albo alfy są na zero, albo cały tryb alfa jest wyłączony
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
            transition.setBetaMaxValue(0.0, false);
            transition.setBetaModeStatus(false);
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
                    transition.setBetaMaxValue(betaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //zmniejszenie starego betaMin do nowego betaMax
                    transition.setBetaMinValue(newBetaMax, true);
                    transition.setBetaMaxValue(newBetaMax, false);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        transition.setBetaMaxValue(newBetaMax, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu gamma-minimum dla miejsca XTPN.
     * @param newGammaMin (<b>double</b>) nowa wartość gammaMin.
     * @param place (<b>PlaceXTPN</b>) obiekt miejsca XTPN.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego węzła miejsca.
     * @return (<b>boolean</b>) - true, jeżeli zmiana wartości się udała.
     */
    public boolean setGammaMinTime(double newGammaMin, PlaceXTPN place, ElementLocation elementLocation) {
        //PlaceXTPN place = (PlaceXTPN) element;
        double gammaMax = place.getGammaMaxValue();
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
                    place.setGammaMaxValue(newGammaMin, true);
                    place.setGammaMinValue(newGammaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //redukcja nowego gMin do aktualnego gMax
                    place.setGammaMinValue(gammaMax, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        place.setGammaMinValue(newGammaMin, false);
        repaintGraphPanel(elementLocation);
        return true;
    }

    /**
     * Metoda ustawia nową wartość czasu gamma-maximum dla miejsca XTPN.
     * @param newGammaMax (<b>double</b>) nowa wartość gammaMax.
     * @param place (<b>PlaceXTPN</b>) obiekt miejsca XTPN.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego węzła miejsca.
     * @return (<b>boolean</b>) - true, jeżeli zmiana wartości się udała.
     */
    public boolean setGammaMaxTime(double newGammaMax, PlaceXTPN place, ElementLocation elementLocation) {
        //PlaceXTPN place = (PlaceXTPN) element;
        double gammaMin = place.getGammaMinValue();
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
                    place.setGammaMaxValue(gammaMin, true);
                    repaintGraphPanel(elementLocation);
                    return true;
                case 1: //zmniejszenie starego gammaMin do nowego gammaMax
                    place.setGammaMinValue(newGammaMax, true);
                    place.setGammaMaxValue(newGammaMax, false);
                    repaintGraphPanel(elementLocation);
                    return true;
                default: //cancel
                    return false;
            }
        }
        place.setGammaMaxValue(newGammaMax, false);
        repaintGraphPanel(elementLocation);

        return true;
    }

    /**
     * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
     * @param elementLocation (<b>ElementLocation</b>) lokalizacja klikniętego obiektu sieci.
     */
    private void repaintGraphPanel(ElementLocation elementLocation) {
        int sheetIndex = overlord.IDtoIndex(elementLocation.getSheetID());
        GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
        graphPanel.repaint();
    }
}
