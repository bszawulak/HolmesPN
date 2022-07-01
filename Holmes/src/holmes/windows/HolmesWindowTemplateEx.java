package holmes.windows;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.simulators.NetSimulator;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class HolmesWindowTemplateEx extends JFrame {
    private JFrame ego;
    private GUIManager overlord;
    private ArrayList<Double> multisetK = new ArrayList<Double>();
    private boolean mainSimulatorActive;
    private JPanel mainPanel;
    private JPanel topPanel;
    private JPanel dockPanel;

    public HolmesWindowTemplateEx(Place place) {
        overlord = GUIManager.getDefaultGUIManager();
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
        setSize(new Dimension(640, 450));
        mainPanel = new JPanel(new BorderLayout());
        topPanel =  new JPanel(new BorderLayout());
        topPanel.setPreferredSize(new Dimension(640, 150));
        topPanel.setLocation(0, 0);
        topPanel.setBorder(BorderFactory.createTitledBorder("Token selected:"));
        mainPanel.add(topPanel, BorderLayout.NORTH);


        dockPanel =  new JPanel(new BorderLayout());
        dockPanel.setPreferredSize(new Dimension(640, 300));
        dockPanel.setLocation(0, 0);
        dockPanel.setBorder(BorderFactory.createTitledBorder("Options:"));
        mainPanel.add(dockPanel, BorderLayout.CENTER);
        add(mainPanel);
    }
}
