package holmes.windows;

import holmes.darkgui.GUIManager;
import holmes.darkgui.holmesInterface.HolmesRoundedButton;
import holmes.petrinet.data.P_StateManager;
import holmes.petrinet.data.StatePlacesVectorXTPN;
import holmes.petrinet.elements.Place;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.utilities.Tools;
import holmes.windows.managers.HolmesStatesEditorXTPN;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class HolmesXTPNtokens extends JFrame {
    private final GUIManager overlord;

    private HolmesStatesEditorXTPN parentWindow;
    private Place place;
    private boolean mainSimulatorActive;
    private boolean listenerAllowed = true; //jeśli true, comboBoxy działają

    private JComboBox<String> tokensComboBox;
    private JLabel idTokenLabel;
    private JFormattedTextField tokenValueTextField;
    private JLabel tokensNoLabel;

    private P_StateManager spm;
    private StatePlacesVectorXTPN vectorXTPN;
    private ArrayList<Place> places;

    private JFormattedTextField addNewTextField;

    public HolmesXTPNtokens(Place placeObj, HolmesStatesEditorXTPN parent) {
        overlord = GUIManager.getDefaultGUIManager();
        parentWindow = parent;
        place = placeObj;
        spm = overlord.getWorkspace().getProject().accessStatesManager();
        vectorXTPN = spm.getCurrentStateXTPN();
        places = overlord.getWorkspace().getProject().getPlaces();
        setTitle("XPTN tokens window");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ignored) {

        }

        if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != GraphicalSimulator.SimulatorMode.STOPPED)
            mainSimulatorActive = true;

        //odblokowuje okno wywoławcze
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if(parentWindow == null) {
                    overlord.getFrame().setEnabled(true);
                } else {
                    parentWindow.fillTable();
                    parentWindow.setEnabled(true);
                }
            }
        });

        if(mainSimulatorActive) {
            JOptionPane.showMessageDialog(null,
                    "Window unavailable when simulator is working.",
                    "Error: simulation in progress", JOptionPane.ERROR_MESSAGE);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else {

            if(parentWindow == null) {
                overlord.getFrame().setEnabled(false);
            } else {
                parentWindow.setEnabled(false);
            }

            setResizable(false);
            initializeComponents();
            //setLocationRelativeTo(overlord);
            int x = place.getElementLocations().get(0).getPosition().x;
            int y = place.getElementLocations().get(0).getPosition().y;
            x += 250;
            setLocation(x,y);
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
        mainPanel.add(getDockPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel getDockPanel() {
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(null);
        comboPanel.setBounds(0, 0, 360, 140);
        comboPanel.setLocation(0, 0);
        comboPanel.setBorder(BorderFactory.createTitledBorder("XTPN tokens options"));

        listenerAllowed = false;
        int comboPanelX = 0;
        int comboPanelY = 0;

        String[] dataP = { "---" };
        tokensComboBox = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
        tokensComboBox.setLocation(comboPanelX +10, comboPanelY+=20);
        tokensComboBox.setSize(180, 20);
        tokensComboBox.setSelectedIndex(0);
        tokensComboBox.setMaximumRowCount(10);
        tokensComboBox.addActionListener(actionEvent -> {
            if(!listenerAllowed)
                return;

            int selected = tokensComboBox.getSelectedIndex();
            idTokenLabel.setText("ID: "+selected);

            if(selected >= 0) {
                tokenValueTextField.setValue(place.accessMultiset().get(selected));
            }
        });
        comboPanel.add(tokensComboBox);

        recreateComboBox();

        tokensNoLabel = new JLabel("Tokens:"+place.getTokensNumber(), JLabel.LEFT);
        tokensNoLabel.setLocation(comboPanelX +200, comboPanelY);
        tokensNoLabel.setSize(90, 20);
        comboPanel.add(tokensNoLabel);

        //ID tokenu
        idTokenLabel = new JLabel("ID: "+tokensComboBox.getSelectedIndex(), JLabel.LEFT);
        idTokenLabel.setLocation(comboPanelX+10, comboPanelY+=30);
        idTokenLabel.setSize(50, 20);
        comboPanel.add(idTokenLabel);

        // pole wartości tokenu
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(place.getFraction_xTPN());
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        tokenValueTextField = new JFormattedTextField(formatter);
        tokenValueTextField.setValue(0.0);
        tokenValueTextField.setBounds(comboPanelX+40, comboPanelY, 110, 20);
        tokenValueTextField.addPropertyChangeListener("value", e -> {
            if (!listenerAllowed)
                return;

            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
        });
        comboPanel.add(tokenValueTextField);

        //potwierdzenie zmiany wartości tokenu

        //HolmesRoundedButton changeTokenValueButton = new HolmesRoundedButton("", "XTPNtokensWindow/HTWchange1.png", "XTPNtokensWindow/HTWchange2.png", "XTPNtokensWindow/HTWchange3.png");
        HolmesRoundedButton changeTokenValueButton = new HolmesRoundedButton("Change value"
                , "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
        changeTokenValueButton.setMargin(new Insets(0, 0, 0, 0));
        changeTokenValueButton.setBounds(comboPanelX+150, comboPanelY-5, 100, 30);
        changeTokenValueButton.setFocusPainted(false);
        changeTokenValueButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;
            int selected = tokensComboBox.getSelectedIndex();
            if(selected == -1)
                return;

            try {
                String text = tokenValueTextField.getValue().toString();
                double val = Double.parseDouble(text);
                if(val < 0.0)
                    val = 0.0;
                listenerAllowed=false;

                place.updateToken(selected, val);
                int location = places.indexOf(place);
                vectorXTPN.setNewMultisetK(location, new ArrayList<>(place.accessMultiset()) );

                recreateComboBox();
                tokensComboBox.setSelectedIndex(selected);

                listenerAllowed=true;
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Cannot convert "+tokenValueTextField.getValue()+ " into Double",
                        "Conversion eror", JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(changeTokenValueButton);

        //usunięcie tokenu
        // HolmesRoundedButton removeTokenValueButton = new HolmesRoundedButton("", "XTPNtokensWindow/HTWremove1.png", "XTPNtokensWindow/HTWremove2.png", "XTPNtokensWindow/HTWremove3.png");
        HolmesRoundedButton removeTokenValueButton = new HolmesRoundedButton("Remove"
                , "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
        removeTokenValueButton.setMargin(new Insets(0, 0, 0, 0));
        removeTokenValueButton.setBounds(comboPanelX+250, comboPanelY-5, 100, 30);
        removeTokenValueButton.setFocusPainted(false);
        removeTokenValueButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;
            int selected = tokensComboBox.getSelectedIndex();
            if(selected == -1)
                return;

            try {
                //Double val = Double.parseDouble((String) tokenValueTextField.getValue());
                listenerAllowed=false;

                place.removeTokenByID(selected);
                int location = places.indexOf(place);
                vectorXTPN.setNewMultisetK(location, new ArrayList<>(place.accessMultiset()) );

                recreateComboBox();
                listenerAllowed=true;
                tokensNoLabel.setText("Tokens:"+place.getTokensNumber());
                tokensComboBox.setSelectedIndex(0);
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Cannot convert "+tokenValueTextField.getValue()+ " into Double",
                        "Conversion eror", JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(removeTokenValueButton);

        JLabel addNewLabel = new JLabel("New:", JLabel.LEFT);
        addNewLabel.setBounds(comboPanelX+10, comboPanelY+=30, 50, 20);
        comboPanel.add(addNewLabel);

        // pole dodawania nowego tokenu
        addNewTextField = new JFormattedTextField(formatter);
        addNewTextField.setValue(0.0);
        addNewTextField.setBounds(comboPanelX+40, comboPanelY, 110, 20);
        addNewTextField.addPropertyChangeListener("value", e -> {
            if (!listenerAllowed)
                return;

            JFormattedTextField field = (JFormattedTextField) e.getSource();
            try {
                field.commitEdit();
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            }
        });
        comboPanel.add(addNewTextField);

        //potwierdzenie dodania nowego tokenu
        //HolmesRoundedButton addNewTokenButton = new HolmesRoundedButton("Add new"
        //        , "XTPNtokensWindow/HTWaddNew1.png", "XTPNtokensWindow/HTWaddNew2.png"
        //        , "XTPNtokensWindow/HTWaddNew3.png");
        HolmesRoundedButton addNewTokenButton = new HolmesRoundedButton("Add new token"
                , "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
        addNewTokenButton.setBounds(comboPanelX+150, comboPanelY-5, 100, 30);
        addNewTokenButton.setMargin(new Insets(0, 0, 0, 0));
        addNewTokenButton.setFocusPainted(false);
        addNewTokenButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;
            try {
                String text = addNewTextField.getValue().toString();
                double val = Double.parseDouble(text);
                if(val < 0.0)
                    val = 0.0;
                place.addTokens_XTPN(1, val);
                int location = places.indexOf(place);
                vectorXTPN.setNewMultisetK(location, new ArrayList<>(place.accessMultiset()) );

                //listenerAllowed=false;

                //int oldSelectedIndex = tokensComboBox.getSelectedIndex();
                recreateComboBox();

                //if(oldSelectedIndex < 0)
                //    oldSelectedIndex = 0;

                tokenValueTextField.setEnabled(true);
                changeTokenValueButton.setEnabled(true);
                removeTokenValueButton.setEnabled(true);
                recalculateTokens();
                //idTokenLabel.setText("ID: "+tokensComboBox.getSelectedIndex());
                tokensComboBox.setSelectedIndex(0);
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Cannot convert "+tokenValueTextField.getValue()+ " into Double",
                        "Conversion eror", JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(addNewTokenButton);


        //JButton clearAllButton = new JButton("Clear All");
        HolmesRoundedButton clearAllButton = new HolmesRoundedButton("Clear all"
                , "bMtemp1.png", "bMtemp2.png", "bMtemp3.png");
        clearAllButton.setMargin(new Insets(0, 0, 0, 0));
        clearAllButton.setBounds(comboPanelX+10, comboPanelY+25, 100, 30);
        clearAllButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;

            int n = JOptionPane.showConfirmDialog(null, "This will clear all the tokens data. Continue?", "Clean warning",
                    JOptionPane.YES_NO_OPTION);
            if(n == 0){
                listenerAllowed=false;

                place.accessMultiset().clear();
                int location = places.indexOf(place);
                vectorXTPN.setNewMultisetK(location, new ArrayList<>(place.accessMultiset()) );

                recreateComboBox();
                recalculateTokens();

                listenerAllowed=true;
            }
        });
        comboPanel.add(clearAllButton);

        /*
        JButton pStateManagerButton = new JButton("<html>p-state<br>manager</html>");
        pStateManagerButton.setMargin(new Insets(0, 0, 0, 0));
        pStateManagerButton.setBounds(comboPanelX+150, comboPanelY, 60, 30);
        pStateManagerButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;

        });
        comboPanel.add(pStateManagerButton);
        */

        listenerAllowed = true;

        if(tokensComboBox.getItemCount() > 0) {
            tokensComboBox.setSelectedIndex(0);
        } else {
            tokenValueTextField.setEnabled(false);
            changeTokenValueButton.setEnabled(false);
            removeTokenValueButton.setEnabled(false);
        }
        return comboPanel;
    }

    /**
     * Metoda przelicza ile jest tokenów i wyświetla w tokensNoLabel.
     */
    private void recalculateTokens() {
        int val = place.accessMultiset().size();
        tokensNoLabel.setText("Tokens: "+val);
    }

    /**
     * Odtwarza combobox na podstawie listy tokenów w miejscu.
     */
    private void recreateComboBox() {
        tokensComboBox.removeAllItems();
        for(int p=0; p < place.accessMultiset().size(); p++) {
            tokensComboBox.addItem("(\u03BA"+(p)+")  "+place.accessMultiset().get(p));
        }
    }
}
