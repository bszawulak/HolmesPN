package holmes.windows.statespace;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class HolmesStSpRGtpn extends JFrame {
    private GUIManager overlord;
    ArrayList<Transition> transitions;
    ArrayList<Place> places;


    //komponenty:
    private JPanel mainPanel;
    private JPanel upperPanel;
    private JPanel lowerPanel;

    public HolmesStSpRGtpn() {
        overlord = GUIManager.getDefaultGUIManager();
        this.setTitle("State space analysis");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (533315487) | Exception:  "+ex.getMessage(), "error", true);
        }

        //pobiera listę tranzycji i miejsc z projektu
        transitions = overlord.getWorkspace().getProject().getTransitions();
        places = overlord.getWorkspace().getProject().getPlaces();


        //oblokowuje główne okno po zamknięciu tego
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });
        overlord.getFrame().setEnabled(false); //blokuj główne okno
        setResizable(false);
        initializeComponents();
        setVisible(true);
    }

    /**
     * Metoda tworząca główne sekcje okna.
     */
    private void initializeComponents() {
        this.setLocation(20, 20);

        setLayout(new BorderLayout());
        setSize(new Dimension(1024, 768));
        setLocation(50, 50);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 1024, 768);
        mainPanel.setLocation(0, 0);
        mainPanel.add(uppedPanel());
        mainPanel.add(lowerPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel uppedPanel() {
        upperPanel = new JPanel();
        upperPanel.setLayout(null);
        upperPanel.setBounds(0, 0, mainPanel.getWidth()-20, 200);
        upperPanel.setLocation(0, 0);
        upperPanel.setBorder(BorderFactory.createTitledBorder("Token selected:"));

        int comboPanelX = 0;
        int comboPanelY = 0;

        return upperPanel;
    }

    private JPanel lowerPanel() {
        lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setLayout(null);
        lowerPanel.setBounds(0, 0, mainPanel.getWidth()-20, 550);;
        lowerPanel.setLocation(0, upperPanel.getHeight());

        lowerPanel.setBorder(BorderFactory.createTitledBorder("Options:"));

        return lowerPanel;
    }
}
