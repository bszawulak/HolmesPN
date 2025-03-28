package holmes.windows.xtpn;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.PlaceXTPN;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.utilities.Tools;
import holmes.windows.xtpn.managers.HolmesStatesEditorXTPN;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Klasa okna dodawania i usuwania tokenów. Działa dla głównego okna oraz managera stanów XTPN.
 * UWAGA!!! Gdy miejsce jest klasyczne (GAMMA = OFF), to jego multizbiór K jest pusty, a liczba tokenów jest
 * przechowywana w tokensNumber jak dla klasycznych miejsc. ALE JEST WYJĄTEK: dla managera stanów, jeśli
 * miejsce jest przechowywane jako klasyczne, wtedy liczba tokenów to jedyna, pierwsza wartość w multizbiorze K. Np.
 * dla takiego miejsca gdy multizbiór zawiera element 8.0, to znaczy, że miejsca ma 8 klasycznych tokenów.
 */
public class HolmesXTPNtokens extends JFrame {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    //private HolmesStatesEditorXTPN parentWindow;
    private JFrame parentWindow;
    private PlaceXTPN place;
    private boolean mainSimulatorActive;
    private boolean isGammaPlace;
    private ArrayList<Double> multisetK;
    private ArrayList<Place> places;
    private boolean doNotUpdate = true; //jeśli true, comboBoxy działają
    private JComboBox<String> tokensComboBox;
    private JLabel tokensNoLabel; //liczba tokenów
    private JLabel idTokenLabel; //ID tokenu
    private JFormattedTextField tokenValueTextField; //pole z aktualną wartością tokeny
    private HolmesRoundedButton changeTokenValueButton; //zatwierdzenie zmiany wartości
    private HolmesRoundedButton removeTokenValueButton; //usunięcie wartości
    private JFormattedTextField addNewTextField; //nowa wartość możliwa do dodania jako token
    private HolmesRoundedButton clearAllButton; //czyszczenie multizbioru

    /**
     * Konstruktor okna dodawania i edycji tokenów XTPN.
     * @param placeObj (<b>PlaceXTPN</b>) obiekt miejsca.
     * @param parent (<b>JFame</b>) potencjalny obiekt okna managera stanów. Lub nie. Gdy null, to znaczy,
     *               że wywołanie nastąpiło w głównego okna Holmesa.
     */
    public HolmesXTPNtokens(PlaceXTPN placeObj, JFrame parent, ArrayList<Double> multK, boolean isGammaPlace) {
        parentWindow = parent;
        places = overlord.getWorkspace().getProject().getPlaces();
        place = placeObj;
        multisetK = multK;
        this.isGammaPlace = isGammaPlace;

        setTitle(lang.getText("HXTwin_entry001title")); //XTPN-place tokens options
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            overlord.log(lang.getText("LOGentry00615exception")+"\n"+ex.getMessage(), "error", true);
        }

        if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != GraphicalSimulator.SimulatorMode.STOPPED)
            mainSimulatorActive = true;

        //odblokowuje okno wywoławcze
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
            if(parentWindow == null) {
                overlord.getFrame().setEnabled(true);
            } else {
                if(parentWindow instanceof HolmesStatesEditorXTPN) {
                    ((HolmesStatesEditorXTPN) parentWindow).fillTable();
                    parentWindow.setEnabled(true);
                    ((HolmesStatesEditorXTPN) parentWindow).doNotUpdate = false;
                } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                    parentWindow.setEnabled(true);
                }
            }
            }
        });

        if(mainSimulatorActive) {
            JOptionPane.showMessageDialog(null,
                    lang.getText("HXTwin_entry002"),
                    lang.getText("problem"), JOptionPane.ERROR_MESSAGE);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else {
            if(parentWindow == null) {
                overlord.getFrame().setEnabled(false);
            } else {
                if(parentWindow instanceof HolmesStatesEditorXTPN) {
                    parentWindow.setEnabled(false);
                    ((HolmesStatesEditorXTPN) parentWindow).doNotUpdate = true;
                } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                    parentWindow.setEnabled(false);
                }
            }
            setResizable(false);
            initializeComponents();
            if(parent != null) {
                setLocationRelativeTo(parent);
            } else {
                //int x = place.getElementLocations().get(0).getPosition().x;
                //int y = place.getElementLocations().get(0).getPosition().y;
                //x += 250;
                setLocation(480,220);
            }
            setVisible(true);
        }
    }

    /**
     * Metoda tworząca główne sekcje okna.
     */
    private void initializeComponents() {
        this.setLocation(20, 20);

        setLayout(new BorderLayout());
        setSize(new Dimension(380, 180));
        setLocation(50, 50);
        //setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);

        //komponenty:
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 380, 180);
        mainPanel.setLocation(0, 0);
        mainPanel.add(createMainPanel());
        mainPanel.setBackground(Color.WHITE);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(null);
        comboPanel.setBounds(2, 0, 364, 143);
        //comboPanel.setLocation(0, 0);
        comboPanel.setBorder(BorderFactory.createTitledBorder("Tokens:"));
        comboPanel.setBackground(Color.WHITE);

        doNotUpdate = false;
        int comboPanelX = 0;
        int comboPanelY = 0;

        String[] dataP = { "---" };
        tokensComboBox = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
        tokensComboBox.setLocation(comboPanelX +10, comboPanelY+=20);
        tokensComboBox.setSize(180, 20);
        tokensComboBox.setSelectedIndex(0);
        tokensComboBox.setMaximumRowCount(10);
        tokensComboBox.setBackground(Color.WHITE);
        tokensComboBox.addActionListener(actionEvent -> {
            if(!doNotUpdate)
                return;

            int selected = tokensComboBox.getSelectedIndex();
            idTokenLabel.setText(lang.getText("HXTwin_entry003")+" "+selected);

            if(selected >= 0) {
                if(parentWindow == null) {
                    //wywołanie z Holmesa - wyświetl rzeczywisty multizbiór miejsca
                    tokenValueTextField.setValue(place.accessMultiset().get(selected));
                } else { //wywołanie z managera - wyświetl token z multizbioru wziętego z przechowywanego p-stanu
                    if(parentWindow instanceof HolmesStatesEditorXTPN) {
                        tokenValueTextField.setValue(multisetK.get(selected));
                    } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                        parentWindow.setEnabled(false);
                    }
                }
            }
        });
        comboPanel.add(tokensComboBox);

        recreateComboBox();

        tokensNoLabel = new JLabel(lang.getText("HXTwin_entry005")+place.getTokensNumber(), JLabel.LEFT); //Tokens:
        tokensNoLabel.setLocation(comboPanelX +200, comboPanelY);
        tokensNoLabel.setSize(90, 20);
        comboPanel.add(tokensNoLabel);

        //ID tokenu
        idTokenLabel = new JLabel(lang.getText("HXTwin_entry006")+" "+tokensComboBox.getSelectedIndex(), JLabel.LEFT); //ID:
        idTokenLabel.setLocation(comboPanelX+10, comboPanelY+=30);
        idTokenLabel.setSize(50, 20);
        comboPanel.add(idTokenLabel);

        // pole wartości tokenu
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(place.getFractionForPlaceXTPN());
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        tokenValueTextField = new JFormattedTextField(formatter);
        tokenValueTextField.setValue(0.0);
        tokenValueTextField.setBounds(comboPanelX+50, comboPanelY, 100, 20);
        tokenValueTextField.addPropertyChangeListener("value", e -> {
            if (!doNotUpdate)
                return;
            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00616exception")+"\n"+ex.getMessage(), "error", true);
                System.out.println(ex.getMessage());
            }
        });
        comboPanel.add(tokenValueTextField);

        //potwierdzenie zmiany wartości tokenu
        changeTokenValueButton = new HolmesRoundedButton(lang.getText("HXTwin_entry007") //Change value
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        changeTokenValueButton.setMargin(new Insets(0, 0, 0, 0));
        changeTokenValueButton.setBounds(comboPanelX+150, comboPanelY-5, 100, 30);
        changeTokenValueButton.setFocusPainted(false);
        changeTokenValueButton.addActionListener(e -> {
            if (!doNotUpdate)
                return;
            int selected = tokensComboBox.getSelectedIndex();
            if(selected == -1)
                return;

            try {
                String text = tokenValueTextField.getValue().toString();
                double val = Double.parseDouble(text);
                if(val < 0.0)
                    val = 0.0;
                doNotUpdate =false;

                if(parentWindow == null) { //to znaczy, że modyfikujemy bezpośrednio miejsca
                    place.updateToken_XTPN(selected, val);
                    overlord.getWorkspace().getProject().repaintAllGraphPanels();
                } else { // modyfikujemy tylko przechowywany p-stan
                    if(parentWindow instanceof HolmesStatesEditorXTPN) {
                        //int location = places.indexOf(place);
                        multisetK.set(selected, val);
                        Collections.sort(multisetK);
                        Collections.reverse(multisetK);
                    } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                        parentWindow.setEnabled(true);
                    }
                }
                recreateComboBox();
                tokensComboBox.setSelectedIndex(selected);
                doNotUpdate =true;
            } catch (Exception exc) {
                String strB = "err.";
                try {
                    strB = String.format(lang.getText("HXTwin_entry008"), tokenValueTextField.getValue());
                } catch (Exception ex2) {
                    overlord.log(lang.getText("LOGentryLNGexc")+" "+"HXTwin_entry008", "error", true);
                }
                JOptionPane.showMessageDialog(null, strB,
                        lang.getText("HXTwin_entry008t"), JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(changeTokenValueButton);

        //usunięcie tokenu
        removeTokenValueButton = new HolmesRoundedButton(lang.getText("HXTwin_entry009") //Remove
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        removeTokenValueButton.setMargin(new Insets(0, 0, 0, 0));
        removeTokenValueButton.setBounds(comboPanelX+250, comboPanelY-5, 100, 30);
        removeTokenValueButton.setFocusPainted(false);
        removeTokenValueButton.addActionListener(e -> {
            if (!doNotUpdate)
                return;
            int selected = tokensComboBox.getSelectedIndex();
            if(selected == -1)
                return;

            try {
                doNotUpdate =false;

                if(isGammaPlace) {
                    if(parentWindow == null) { //to znaczy, że usuwamy bezpośrednio z miejsca
                        place.removeTokenByID_XTPN(selected); //poprzez metodę, a nie bezpośrednio z multisetK!
                        overlord.getWorkspace().getProject().repaintAllGraphPanels();
                    } else { // usuwamy token tylko z przechowywanego p-stanu
                        if(parentWindow instanceof HolmesStatesEditorXTPN) {
                            if(selected > -1 && selected < multisetK.size()) {
                                multisetK.remove(selected);
                            } else {
                                String strB = "err.";
                                try {
                                    strB = String.format(lang.getText("HXTwin_entry010"), selected, places.indexOf(place));
                                } catch (Exception ex) {
                                    overlord.log(lang.getText("LOGentryLNGexc")+" "+"HXTwin_entry010", "error", true);
                                }
                                overlord.log(strB, "error", true);
                            }
                        } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                            ((HolmesNodeInfoXTPN)parentWindow).printTokenNumber();
                        }
                    }
                } else { //miejsca klasyczne
                    if(parentWindow == null) { //to znaczy, że usuwamy bezpośrednio z miejsca
                        place.addTokensNumber(-1); //poprzez metodę, a nie bezpośrednio z multisetK!
                        overlord.getWorkspace().getProject().repaintAllGraphPanels();
                    } else {
                        if(parentWindow instanceof HolmesStatesEditorXTPN) { // usuwamy token tylko z przechowywanego p-stanu z managera:
                            if(selected > -1 && selected < multisetK.size()) {
                                double tokensNumber = multisetK.get(0);
                                int tokens = (int)tokensNumber;
                                if(tokens >= 0) {
                                    tokensNumber--;
                                    multisetK.set(0, tokensNumber);
                                }
                            } else {
                                String strB = "err.";
                                try {
                                    strB = String.format(lang.getText("HXTwin_entry011"), selected, places.indexOf(place));
                                } catch (Exception ex2) {
                                    overlord.log(lang.getText("LOGentryLNGexc")+" "+"HXTwin_entry011", "error", true);
                                }
                                overlord.log(strB, "error", true);
                            }
                        } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                            ((HolmesNodeInfoXTPN)parentWindow).printTokenNumber();
                        }
                    }
                }
                recreateComboBox();
                writeTokensNumberInLabel();
                checkInterfaceConditions();
                doNotUpdate =true;

                if(isGammaPlace) {
                    if(!multisetK.isEmpty())
                        tokensComboBox.setSelectedIndex(0);
                }
            } catch (Exception exc) {
                String strB = "err.";
                try {
                    strB = String.format(lang.getText("HXTwin_entry012"), tokenValueTextField.getValue());
                } catch (Exception ex2) {
                    overlord.log(lang.getText("LOGentryLNGexc")+" "+"HXTwin_entry012", "error", true);
                }
                JOptionPane.showMessageDialog(null, strB,
                        lang.getText("HXTwin_entry008t"), JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(removeTokenValueButton);

        JLabel addNewLabel = new JLabel(lang.getText("HXTwin_entry013"), JLabel.LEFT); //New:
        addNewLabel.setBounds(comboPanelX+10, comboPanelY+=30, 50, 20);
        comboPanel.add(addNewLabel);

        // pole dodawania nowego tokenu
        addNewTextField = new JFormattedTextField(formatter);
        addNewTextField.setValue(0.0);
        addNewTextField.setBounds(comboPanelX+50, comboPanelY, 100, 20);
        addNewTextField.addPropertyChangeListener("value", e -> {
            if (!doNotUpdate)
                return;

            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                overlord.log(lang.getText("LOGentry00617exception")+"\n"+ex.getMessage(), "error", true);
                System.out.println(ex.getMessage());
            }
        });
        comboPanel.add(addNewTextField);

        //potwierdzenie dodania nowego tokenu
        //przycisk dodania tokeny z nową wartością
        HolmesRoundedButton addNewTokenButton = new HolmesRoundedButton(lang.getText("HXTwin_entry014") //Add new token
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        addNewTokenButton.setBounds(comboPanelX+150, comboPanelY-5, 100, 30);
        addNewTokenButton.setMargin(new Insets(0, 0, 0, 0));
        addNewTokenButton.setFocusPainted(false);
        addNewTokenButton.addActionListener(e -> {
            if (!doNotUpdate)
                return;
            try {
                if(isGammaPlace) {
                    String text = addNewTextField.getValue().toString();
                    double val = Double.parseDouble(text);
                    if(val < 0.0)
                        val = 0.0;

                    if(parentWindow == null) { //to znaczy, że dodajemy bezpośrednio do miejsca
                        place.addTokens_XTPN(1, val);
                        overlord.getWorkspace().getProject().repaintAllGraphPanels();
                    } else { // dodajemy token tylko do przechowywanego p-stanu
                        if(parentWindow instanceof HolmesStatesEditorXTPN) {
                            multisetK.add(val);
                            if(val > 0) {
                                Collections.sort(multisetK);
                                Collections.reverse(multisetK);
                            }
                        } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                            ((HolmesNodeInfoXTPN)parentWindow).printTokenNumber();
                        }
                    }
                    recreateComboBox();
                    writeTokensNumberInLabel();
                    checkInterfaceConditions();
                    tokensComboBox.setSelectedIndex(0);
                } else { //miejsce klasyczne
                    if(parentWindow == null) { //to znaczy, że dodajemy bezpośrednio do miejsca
                        place.addTokens_XTPN(1, 0.0);
                        overlord.getWorkspace().getProject().repaintAllGraphPanels();
                    } else { // dodajemy token tylko do przechowywanego p-stanu
                        if(parentWindow instanceof HolmesStatesEditorXTPN) {
                            double tokensNumber = multisetK.get(0);
                            tokensNumber++;
                            multisetK.set(0, tokensNumber);
                        } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                            ((HolmesNodeInfoXTPN)parentWindow).printTokenNumber();
                        }
                    }
                    checkInterfaceConditions();
                    writeTokensNumberInLabel();
                }
            } catch (Exception exc) {
                String strB = "err.";
                try {
                    strB = String.format(lang.getText("HXTwin_entry015"), addNewTextField.getValue());
                } catch (Exception ex2) {
                    overlord.log(lang.getText("LOGentryLNGexc")+" "+"HXTwin_entry015", "error", true);
                }
                JOptionPane.showMessageDialog(null, strB,
                        lang.getText("HXTwin_entry008t"), JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(addNewTokenButton);

        clearAllButton = new HolmesRoundedButton(lang.getText("HXTwin_entry016") //Clear all
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        clearAllButton.setMargin(new Insets(0, 0, 0, 0));
        clearAllButton.setBounds(comboPanelX+10, comboPanelY+25, 120, 30);
        clearAllButton.addActionListener(e -> {
            if (!doNotUpdate)
                return;

            int n = JOptionPane.showConfirmDialog(null, lang.getText("HXTwin_entry017")
                    , lang.getText("warning"), JOptionPane.YES_NO_OPTION);
            if(n == 0){
                doNotUpdate =false;
                if(parentWindow == null) { //to znaczy, że czyścimy bezpośrednio z miejsca
                    place.accessMultiset().clear();
                    place.setTokensNumber(0);
                    overlord.getWorkspace().getProject().repaintAllGraphPanels();
                } else { // czyścimy tylko przechowywany p-stan
                    if(parentWindow instanceof HolmesStatesEditorXTPN) {
                        multisetK.clear();
                    } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                        ((HolmesNodeInfoXTPN)parentWindow).printTokenNumber();
                    }
                }
                checkInterfaceConditions();
                recreateComboBox();
                writeTokensNumberInLabel();
                doNotUpdate =true;
            }
        });
        comboPanel.add(clearAllButton);

        HolmesRoundedButton addMultipleTokensButton = new HolmesRoundedButton(lang.getText("HXTwin_entry018") //Add #[New] tokens
                , "pearl_bH1_neutr.png", "pearl_bH2_hover.png", "pearl_bH3_press.png");
        addMultipleTokensButton.setBounds(comboPanelX+130, comboPanelY+25, 120, 30);
        addMultipleTokensButton.setMargin(new Insets(0, 0, 0, 0));
        addMultipleTokensButton.setFocusPainted(false);
        addMultipleTokensButton.addActionListener(e -> {
            if (!doNotUpdate)
                return;
            try {
                String text = addNewTextField.getValue().toString();
                double val = Double.parseDouble(text);
                if(val < 1.0)
                    val = 1.0;

                if(isGammaPlace) {
                    if(parentWindow == null) { //to znaczy, że dodajemy bezpośrednio do miejsca
                        place.addTokens_XTPN((int)val, 0.0);
                        overlord.getWorkspace().getProject().repaintAllGraphPanels();
                    } else { // dodajemy token tylko do przechowywanego p-stanu
                        if(parentWindow instanceof HolmesStatesEditorXTPN) {
                            for(int i=0; i<val; i++) {
                                multisetK.add(0.0);
                            }
                        } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                            ((HolmesNodeInfoXTPN)parentWindow).printTokenNumber();
                        }
                    }
                    recreateComboBox();
                    writeTokensNumberInLabel();
                    checkInterfaceConditions();
                    tokensComboBox.setSelectedIndex(0);
                } else { //miejsce klasyczne
                    if(parentWindow == null) { //to znaczy, że dodajemy bezpośrednio do miejsca
                        place.addTokens_XTPN((int)val, 0.0);
                        overlord.getWorkspace().getProject().repaintAllGraphPanels();
                    } else { // dodajemy token tylko do przechowywanego p-stanu
                        if(parentWindow instanceof HolmesStatesEditorXTPN) {
                            double tokensNumber = multisetK.get(0);
                            tokensNumber += val;
                            multisetK.set(0, tokensNumber);
                        } else if(parentWindow instanceof HolmesNodeInfoXTPN) {
                            ((HolmesNodeInfoXTPN)parentWindow).printTokenNumber();
                        }
                    }
                    checkInterfaceConditions();
                    writeTokensNumberInLabel();
                }
            } catch (Exception exc) {
                String strB = "err.";
                try {
                    strB = String.format(lang.getText("HXTwin_entry019"), addNewTextField.getValue());
                } catch (Exception ex2) {
                    overlord.log(lang.getText("LOGentryLNGexc")+" "+"HXTwin_entry019", "error", true);
                }
                JOptionPane.showMessageDialog(null, strB,
                        lang.getText("HXTwin_entry008t"), JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(addMultipleTokensButton);

        doNotUpdate = true;

        if(tokensComboBox.getItemCount() > 0) {
            tokensComboBox.setSelectedIndex(0);
        }
        checkInterfaceConditions();
        return comboPanel;
    }

    /**
     * Dostosowuje aktywność przycisków i pól do tego czy są tokeny XTPN.
     */
    private void checkInterfaceConditions() {
        if(isGammaPlace) {
            tokensComboBox.setEnabled(true);
            if(tokensComboBox.getItemCount() > 0) { //jeśli brak tokenów
                tokenValueTextField.setEnabled(true);
                changeTokenValueButton.setEnabled(true);
                removeTokenValueButton.setEnabled(true);
                clearAllButton.setEnabled(true);
            } else {
                tokenValueTextField.setEnabled(false);
                changeTokenValueButton.setEnabled(false);
                removeTokenValueButton.setEnabled(false);
                clearAllButton.setEnabled(false);
            }
            addNewTextField.setEnabled(true);
        } else { //zwykłe miejsce
            tokenValueTextField.setEnabled(false);
            changeTokenValueButton.setEnabled(false);
            clearAllButton.setEnabled(false);
            removeTokenValueButton.setEnabled(true);
            tokensComboBox.setEnabled(false);
            addNewTextField.setEnabled(false);
        }
    }

    /**
     * Metoda przelicza ile jest tokenów i wyświetla w tokensNoLabel.
     */
    private void writeTokensNumberInLabel() {
        if(isGammaPlace) {
            int tokensNo = multisetK.size();
            tokensNoLabel.setText(lang.getText("HXTwin_entry020")+" " + tokensNo ); //Tokens:

            if(parentWindow == null) { //tylko dla głównego okna odwołujemy się do miejsca
                if(tokensNo != place.getTokensNumber()) {
                    overlord.log(lang.getText("HXTwin_entry021")+place.getID(), "error", true);
                }
            }
        } else { //classical place
            double tokensNo = multisetK.get(0);
            tokensNoLabel.setText("Tokens:"+" " + (int)tokensNo );

            if(parentWindow == null) { //tylko dla głównego okna odwołujemy się do miejsca
                if(tokensNo != place.getTokensNumber()) {
                    overlord.log(lang.getText("HXTwin_entry022")+place.getID(), "error", true);
                }
            }
        }
    }

    /**
     * Odtwarza combobox na podstawie listy tokenów w miejscu.
     */
    private void recreateComboBox() {
        tokensComboBox.removeAllItems();

        if(parentWindow == null) {
            for(int p=0; p < place.accessMultiset().size(); p++) {
                double token = place.accessMultiset().get(p);
                tokensComboBox.addItem("(\u03BA"+(p)+")  " + Tools.cutValueExt(token, place.getFractionForPlaceXTPN()) );
            }
        } else { //dla obkna p-state manager:
            if(parentWindow instanceof HolmesStatesEditorXTPN) {
                for(int p=0; p < multisetK.size(); p++) {
                    double token = multisetK.get(p);
                    tokensComboBox.addItem("(\u03BA"+(p)+")  " + Tools.cutValueExt(token, place.getFractionForPlaceXTPN()) );
                }
            } else if(parentWindow instanceof HolmesNodeInfoXTPN) {

            }
        }
    }
}
