package holmes.windows.statespace;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.*;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.*;

public class HolmesStSpUNF extends JFrame {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static JTextArea logField1stTab = null;

    // components:
    private JPanel mainPanel;
    private JPanel upperPanel;
    private JPanel lowerPanel;

    public HolmesStSpUNF() {
        this.setTitle("State space analysis");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (533315487) | Exception:  " + ex.getMessage(), "error", true);
        }

        // oblokowuje główne okno po zamknięciu tego
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });
        overlord.getFrame().setEnabled(false); // blokuj główne okno
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
        upperPanel.setBounds(0, 0, mainPanel.getWidth() - 20, 200);
        upperPanel.setLocation(0, 0);
        upperPanel.setBorder(BorderFactory.createTitledBorder("First panel:"));

        int panX = 20;
        int panY = 20;

        JButton button1 = new JButton("Perform Unfolding");
        button1.setText("<html><center>Perform<br />Unfolding<center></html>");
        button1.setBounds(panX, panY, 150, 40);
        button1.setMargin(new Insets(0, 0, 0, 0));
        button1.addActionListener(actionEvent -> HolmesUnfolding());
        button1.setFocusPainted(false);
        upperPanel.add(button1);

        return upperPanel;
    }

    private JPanel lowerPanel() {
        lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setLayout(null);
        lowerPanel.setBounds(0, 0, mainPanel.getWidth() - 20, 550);
        ;
        lowerPanel.setLocation(0, upperPanel.getHeight());

        lowerPanel.setBorder(BorderFactory.createTitledBorder("Second panel:"));

        logField1stTab = new JTextArea();
        logField1stTab.setLineWrap(true);
        logField1stTab.setEditable(true);
        logField1stTab.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret) logField1stTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField1stTab), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, lowerPanel.getWidth() - 35, lowerPanel.getHeight() - 50);
        lowerPanel.add(logFieldPanel);

        return lowerPanel;
    }

    /**
     * Wykonuje algorytm unfolding i wyświetla wyniki.
     */
    private void HolmesUnfolding() {
        ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces(); // Pobiera listę miejsc z projektu
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions(); // Pobiera listę tranzycji z projektu

        // Sprawdzanie i logowanie tranzycji
        if (transitions.size() > 1) { // Jeśli jest więcej niż jedna tranzycja w projekcie
            Transition t1 = transitions.get(0); // Pobierz pierwszą tranzycję
            logField1stTab.append("Transition 1: " + t1.getName() + "\n"); // Zaloguj nazwę tranzycji

            ArrayList<Place> inputPlaces = t1.getInputPlaces(); // Pobierz miejsca wejściowe tranzycji
            for (Place p : inputPlaces) {
                logField1stTab.append("Input place: " + p.getName() + "\n"); // Zaloguj każde miejsce wejściowe
            }

            ArrayList<Place> outputPlaces = t1.getOutputPlaces(); // Pobierz miejsca wyjściowe tranzycji
            for (Place p : outputPlaces) {
                logField1stTab.append("Output place: " + p.getName() + "\n"); // Zaloguj każde miejsce wyjściowe
            }
        } else {
            logField1stTab.append("No transitions in the project.\n"); // Jeśli nie ma tranzycji w projekcie, zaloguj to
        }

        // Initialize marking
        Map<Place, Integer> initialMarking = new HashMap<>(); // Tworzy mapę do przechowywania oznaczenia początkowego
        for (Place place : places) {
            initialMarking.put(place, place.getTokens()); // Dodaje liczbę tokenów dla każdego miejsca
        }

        // Initialize GraphStream graph
        Graph unfoldingGraph = new MultiGraph("Unfolding"); // Tworzy nowy graf dla algorytmu unfolding
        System.setProperty("org.graphstream.ui", "swing"); // Ustawia tryb wyświetlania grafu

        // Dodaj węzeł początkowy
        String initialNodeId = "Start"; // Identyfikator węzła początkowego
        unfoldingGraph.addNode(initialNodeId).setAttribute("ui.label", initialNodeId); // Dodaje węzeł początkowy do grafu

        // Perform unfolding
        Queue<Map<Place, Integer>> toExplore = new LinkedList<>(); // Kolejka do eksploracji nowych oznaczeń
        Set<Map<Place, Integer>> visited = new HashSet<>(); // Zbiór odwiedzonych oznaczeń
        toExplore.add(initialMarking); // Dodaje oznaczenie początkowe do kolejki
        visited.add(initialMarking); // Dodaje oznaczenie początkowe do odwiedzonych

        while (!toExplore.isEmpty()) { // Dopóki są oznaczenia do eksploracji
            Map<Place, Integer> currentMarking = toExplore.poll(); // Pobiera i usuwa pierwsze oznaczenie z kolejki

            for (Transition transition : transitions) { // Iteruje przez wszystkie tranzycje
                if (canFire(currentMarking, transition)) { // Sprawdza, czy tranzycja może zostać uruchomiona
                    Map<Place, Integer> newMarking = fireTransition(currentMarking, transition); // Tworzy nowe oznaczenie po uruchomieniu tranzycji

                    if (!visited.contains(newMarking)) { // Jeśli nowe oznaczenie nie zostało jeszcze odwiedzone
                        visited.add(newMarking); // Dodaje nowe oznaczenie do odwiedzonych
                        toExplore.add(newMarking); // Dodaje nowe oznaczenie do kolejki

                        String newNodeId = newMarking.toString(); // Tworzy identyfikator węzła dla nowego oznaczenia
                        unfoldingGraph.addNode(newNodeId).setAttribute("ui.label", newNodeId); // Dodaje węzeł dla nowego oznaczenia
                        unfoldingGraph.addEdge(currentMarking.toString() + "->" + newNodeId, currentMarking.toString(), newNodeId, true); // Dodaje krawędź między obecnym a nowym węzłem
                    } else {
                        // Jeśli oznaczenie już istnieje, tworzy tylko krawędź
                        unfoldingGraph.addEdge(currentMarking.toString() + "->" + newMarking.toString(), currentMarking.toString(), newMarking.toString(), true);
                    }
                }
            }
        }

        unfoldingGraph.display(); // Wyświetla graf unfolding
    }

    /**
     * Sprawdza, czy tranzycja może zostać uruchomiona w danym oznaczeniu.
     */
    private boolean canFire(Map<Place, Integer> marking, Transition transition) {
        for (Place place : transition.getInputPlaces()) { // Iteruje przez miejsca wejściowe tranzycji
            if (marking.getOrDefault(place, 0) <= 0) { // Sprawdza, czy miejsce ma wystarczającą liczbę tokenów
                return false;
            }
        }
        return true; // Jeśli wszystkie miejsca mają wystarczającą liczbę tokenów, zwraca true
    }

    /**
     * Uruchamia tranzycję i zwraca nowe oznaczenie.
     */
    private Map<Place, Integer> fireTransition(Map<Place, Integer> marking, Transition transition) {
        Map<Place, Integer> newMarking = new HashMap<>(marking); // Tworzy kopię obecnego oznaczenia

        for (Place place : transition.getInputPlaces()) { // Iteruje przez miejsca wejściowe tranzycji
            newMarking.put(place, newMarking.get(place) - 1); // Usuwa tokeny z miejsc wejściowych
        }

        for (Place place : transition.getOutputPlaces()) { // Iteruje przez miejsca wyjściowe tranzycji
            newMarking.put(place, newMarking.getOrDefault(place, 0) + 1); // Dodaje tokeny do miejsc wyjściowych
        }

        return newMarking; // Zwraca nowe oznaczenie
    }
}
