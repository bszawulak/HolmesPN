package holmes.windows;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.simulators.NetSimulator;
import holmes.utilities.Tools;
import holmes.workspace.WorkspaceSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;

public class HolmesXTPNtokens extends JFrame {
    private JFrame ego;
    private GUIManager overlord;
    private Place place = null;
    private boolean mainSimulatorActive;
    private boolean listenerAllowed = true; //jeśli true, comboBoxy działają



    //komponenty:
    private JPanel mainPanel;
    private JComboBox tokensComboBox;
    private JLabel idTokenLabel;
    private JFormattedTextField tokenValueTextField;

    private JFormattedTextField addNewTextField;

    public HolmesXTPNtokens(Place placeObj) {
        overlord = GUIManager.getDefaultGUIManager();
        place = placeObj;
        ego = this;
        ego.setTitle("XPTN place tokens manager");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ignored) {

        }

        if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != NetSimulator.SimulatorMode.STOPPED)
            mainSimulatorActive = true;

        //oblokowuje główne okno
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });

        if(mainSimulatorActive) {
            JOptionPane.showMessageDialog(null,
                    "Window unavailable when simulator is working.",
                    "Error: simulation in progress", JOptionPane.ERROR_MESSAGE);
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        } else {
            overlord.getFrame().setEnabled(false);
            setResizable(false);
            initializeComponents();
            setVisible(true);
        }
    }

    /**
     * Metoda tworząca główne sekcje okna.
     */
    private void initializeComponents() {
        this.setLocation(20, 20);

        setLayout(new BorderLayout());
        setSize(new Dimension(340, 180));
        setLocation(50, 50);
        //setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 340, 180);
        mainPanel.setLocation(0, 0);
        mainPanel.add(getDockPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel getDockPanel() {
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(null);
        comboPanel.setBounds(0, 0, 320, 140);
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
        tokensComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if(listenerAllowed == false)
                    return;

                int selected = tokensComboBox.getSelectedIndex();
                idTokenLabel.setText("ID: "+selected);

                if(selected >= 0) {
                    tokenValueTextField.setValue(place.accessMultiset().get(selected));
                }
            }
        });
        comboPanel.add(tokensComboBox);

        recreateComboBox();

        JLabel tokensNoLabel = new JLabel("Tokens:"+place.getNumberOfTokens_XTPN(), JLabel.LEFT);
        tokensNoLabel.setLocation(comboPanelX +200, comboPanelY);
        tokensNoLabel.setSize(90, 20);
        comboPanel.add(tokensNoLabel);

        //ID tokenu
        idTokenLabel = new JLabel("ID: "+tokensComboBox.getSelectedIndex(), JLabel.LEFT);
        idTokenLabel.setLocation(comboPanelX+10, comboPanelY+=25);
        idTokenLabel.setSize(50, 20);
        comboPanel.add(idTokenLabel);

        // pole wartości tokenu
        NumberFormat formatter = DecimalFormat.getInstance();
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(place.getFraction_xTPN());
        formatter.setRoundingMode(RoundingMode.HALF_UP);

        tokenValueTextField = new JFormattedTextField(formatter);
        tokenValueTextField.setValue(Double.valueOf(-1.0));
        tokenValueTextField.setBounds(comboPanelX+40, comboPanelY, 80, 20);
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
        JButton changeTokenValueButton = new JButton("Change");
        changeTokenValueButton.setMargin(new Insets(0, 0, 0, 0));
        changeTokenValueButton.setBounds(comboPanelX+130, comboPanelY, 60, 20);
        changeTokenValueButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;
            int selected = tokensComboBox.getSelectedIndex();
            if(selected == -1)
                return;

            try {
                String text = tokenValueTextField.getValue().toString();
                Double val = Double.parseDouble(text);
                listenerAllowed=false;

                place.updateToken(selected, val);
                recreateComboBox();
                tokensComboBox.setSelectedIndex(selected);

                listenerAllowed=true;
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Cannot convert "+tokenValueTextField.getValue()+ " into Double",
                        "Conversion eror", JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(changeTokenValueButton);

        //potwierdzenie zmiany wartości tokenu
        JButton removeTokenValueButton = new JButton("Remove");
        removeTokenValueButton.setMargin(new Insets(0, 0, 0, 0));
        removeTokenValueButton.setBounds(comboPanelX+200, comboPanelY, 60, 20);
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
                recreateComboBox();
                listenerAllowed=true;

                tokensComboBox.setSelectedIndex(0);
            } catch (Exception exc) {
                JOptionPane.showMessageDialog(null, "Cannot conver "+tokenValueTextField.getValue()+ " into Double",
                        "Conversion eror", JOptionPane.ERROR_MESSAGE);
            }
        });
        comboPanel.add(removeTokenValueButton);

        JLabel addNewLabel = new JLabel("ID: "+tokensComboBox.getSelectedIndex(), JLabel.LEFT);
        addNewLabel.setBounds(comboPanelX+10, comboPanelY+=25, 50, 20);
        comboPanel.add(addNewLabel);

        // pole dodawania nowego tokenu
        addNewTextField = new JFormattedTextField(formatter);
        addNewTextField.setValue(Double.valueOf(0.0));
        addNewTextField.setBounds(comboPanelX+40, comboPanelY, 80, 20);
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
        JButton addNewTokenButton = new JButton("Add New");
        addNewTokenButton.setMargin(new Insets(0, 0, 0, 0));
        addNewTokenButton.setBounds(comboPanelX+130, comboPanelY, 60, 20);
        addNewTokenButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;

            Double val = Double.parseDouble((String)addNewTextField.getValue());
            place.addTokens_XTPN(1, val);
            listenerAllowed=false;
            recreateComboBox();
            tokensComboBox.setSelectedIndex(0);
            listenerAllowed=true;
        });
        comboPanel.add(addNewTokenButton);

        // dodanie nowego tokenu z wartością 0
        JButton add0TokenButton = new JButton("Add New");
        add0TokenButton.setMargin(new Insets(0, 0, 0, 0));
        add0TokenButton.setBounds(comboPanelX+10, comboPanelY+=25, 60, 30);
        add0TokenButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;

            place.addTokens_XTPN(1, 0.0);
            listenerAllowed=false;
            recreateComboBox();
            tokensComboBox.setSelectedIndex(0);
            listenerAllowed=true;
        });
        comboPanel.add(add0TokenButton);

        JButton clearAllButton = new JButton("Clear All");
        clearAllButton.setMargin(new Insets(0, 0, 0, 0));
        clearAllButton.setBounds(comboPanelX+80, comboPanelY, 60, 30);
        clearAllButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;

            //place.addTokens_XTPN(1, 0.0);
            listenerAllowed=false;
            recreateComboBox();
            tokensComboBox.setSelectedIndex(0);
            listenerAllowed=true;
        });
        comboPanel.add(clearAllButton);

        JButton pStateManagerButton = new JButton("<html>p-state<br>manager</html>");
        pStateManagerButton.setMargin(new Insets(0, 0, 0, 0));
        pStateManagerButton.setBounds(comboPanelX+150, comboPanelY, 60, 30);
        pStateManagerButton.addActionListener(e -> {
            if (!listenerAllowed)
                return;

        });
        comboPanel.add(pStateManagerButton);



        listenerAllowed = true;
        return comboPanel;
    }

    private void recreateComboBox() {
        tokensComboBox.removeAllItems();
        for(int p=0; p < place.accessMultiset().size(); p++) {
            tokensComboBox.addItem("\u03BA"+(p)+" Value:  "+place.accessMultiset().get(p));
        }
    }
}
