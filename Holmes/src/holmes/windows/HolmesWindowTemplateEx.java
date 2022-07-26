package holmes.windows;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.simulators.GraphicalSimulator;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class HolmesWindowTemplateEx extends JFrame {
    private JFrame ego;
    private GUIManager overlord;
    private ArrayList<Double> multisetK = new ArrayList<Double>();
    private Place place = null;
    private boolean mainSimulatorActive;
    private boolean listenerAllowed = true; //jeśli true, comboBoxy działają

    //komponenty:
    private JPanel mainPanel;
    private JComboBox tokensComboBox;

    public HolmesWindowTemplateEx(Place placeObj) {
        overlord = GUIManager.getDefaultGUIManager();
        place = placeObj;
        ego = this;
        ego.setTitle("XPTN place tokens manager");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (533315487) | Exception:  "+ex.getMessage(), "error", false);
        }

        if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != GraphicalSimulator.SimulatorMode.STOPPED)
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
        setSize(new Dimension(640, 450));
        setLocation(50, 50);
        //setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 640, 450);
        mainPanel.setLocation(0, 0);
        mainPanel.add(getComboTopPanel());
        mainPanel.add(getDockPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel getComboTopPanel() {
        JPanel comboPanel = new JPanel();
        comboPanel.setLayout(null);
        comboPanel.setBounds(0, 0, 640, 150);
        //comboPanel.setPreferredSize(new Dimension(640, 150));
        comboPanel.setLocation(0, 0);
        comboPanel.setBorder(BorderFactory.createTitledBorder("Token selected:"));

        int comboPanelX = 0;
        int comboPanelY = 0;

        String[] dataP = { "---" };
        tokensComboBox = new JComboBox<String>(dataP); //final, aby listener przycisku odczytał wartość
        tokensComboBox.setLocation(comboPanelX +=15, comboPanelY+=15);
        tokensComboBox.setSize(400, 20);
        tokensComboBox.setSelectedIndex(0);
        tokensComboBox.setMaximumRowCount(10);
        tokensComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                if(listenerAllowed == false)
                    return;
                int selected = tokensComboBox.getSelectedIndex();
                if(selected > 0) {

                } else {
                    // clearSubPanel();
                }
            }
        });
        comboPanelX += 25;
        comboPanel.add(tokensComboBox);

        //multisetK.addAll(place.accessMultiset());

        tokensComboBox.removeAllItems();
        tokensComboBox.addItem("---");
        for(int p=0; p < multisetK.size(); p++) {
            tokensComboBox.addItem("\u03BA"+(p)+"."+multisetK.get(p));
        }

        return comboPanel;
    }

    private JPanel getDockPanel() {
        JPanel dockPanel = new JPanel(new BorderLayout());
        dockPanel.setLayout(null);
        dockPanel.setBounds(0, 0, 640, 300);
        //dockPanel.setPreferredSize(new Dimension(640, 150));
        dockPanel.setLocation(0, 150);

        dockPanel.setBorder(BorderFactory.createTitledBorder("Options:"));

        return dockPanel;
    }
}
