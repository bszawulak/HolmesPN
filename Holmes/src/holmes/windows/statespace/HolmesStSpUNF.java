package holmes.windows.statespace;

import com.google.gson.JsonObject;
import holmes.graphpanel.GraphPanel;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.PetriNetMethods;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.ColorPalette;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;
import holmes.petrinet.elements.extensions.TransitionTimeExtention;
import javax.swing.*;
import javax.swing.plaf.nimbus.State;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.*;
import holmes.windows.statespace.Graph_Click;


import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.*;
import org.graphstream.ui.spriteManager.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.graphstream.ui.swing_viewer.DefaultView;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.graphstream.ui.view.camera.Camera;
import org.graphstream.ui.view.util.GraphMetrics;
import org.graphstream.ui.view.util.InteractiveElement;
import org.graphstream.ui.view.util.MouseManager;

public class HolmesStSpUNF extends JFrame {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();

    //komponenty:
    private JPanel mainPanel;
    private JPanel upperPanel;
    private JPanel lowerPanel;

    public HolmesStSpUNF() {
        this.setTitle("State space analysis");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (533315487) | Exception:  "+ex.getMessage(), "error", true);
        }

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
        setSize(new Dimension(1024, 700));
        setLocation(50, 50);
        setResizable(false);

        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 1024, 600);
        mainPanel.setLocation(0, 0);
        mainPanel.add(uppedPanel());
        mainPanel.add(lowerPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JTextArea ShortestPathDetails = new JTextArea("Displaying shortest path details.");

    private JPanel uppedPanel() {
        upperPanel = new JPanel();
        upperPanel.setLayout(null);
        upperPanel.setBounds(0, 0, mainPanel.getWidth() - 20, 100); // Zmniejszamy wysokość panelu o połowę
        upperPanel.setLocation(0, 0);
        upperPanel.setBorder(BorderFactory.createTitledBorder("Graph menu"));

        int panelHeight = 100; // Nowa wysokość panelu
        int buttonHeight = 40; // Wysokość przycisku
        int panX = 20; // Odstęp od lewej krawędzi
        int panY = (panelHeight - buttonHeight) / 2; // Obliczamy wyśrodkowanie przycisku w zmniejszonym panelu

        JButton button2 = new JButton("Unfolding");
        button2.setText("<html><center>Create<br />Unfolding<center></html>");
        button2.setBounds(panX, panY, 150, buttonHeight); // Pozycjonujemy przycisk
        button2.setMargin(new Insets(0, 0, 0, 0));
        button2.addActionListener(actionEvent -> {
            manage_RGtpn();
        });
        button2.setFocusPainted(false);
        upperPanel.add(button2);

        return upperPanel;
    }

    private JPanel lowerPanel() {
        lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setLayout(null);
        lowerPanel.setBounds(0, 0, mainPanel.getWidth()-20, 550);
        lowerPanel.setLocation(0, upperPanel.getHeight());

        lowerPanel.setBorder(BorderFactory.createTitledBorder("Graph visualization"));

        return lowerPanel;
    }

    public JPanel getLowerPanel() {
        return lowerPanel;
    }

    public JTextArea getShortestPathDetails() {
        return ShortestPathDetails;
    }

    private void manage_RGtpn() {

        Gson gson = new Gson();
        StringBuilder jsonBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader("C:/Users/filip/Desktop/output4.json"))) {
            String response;
            while ((response = reader.readLine()) != null) {
                System.out.println(response); // Print each line (optional)
                jsonBuilder.append(response); // Append line to StringBuilder
            }

            // Convert the StringBuilder to a String
            String jsonString = jsonBuilder.toString();

            // Print or process the JSON string
            System.out.println("Full JSON Content:");
            System.out.println(jsonString);

        } catch (IOException e) {
            e.printStackTrace();
        }



        Graph graph = GraphVisualization(jsonBuilder);

        new Graph_Click(graph, getLowerPanel(), getShortestPathDetails());

    }

    private static Graph GraphVisualization(StringBuilder jsonBuilder) {

        String jsonInput = jsonBuilder.toString();
        System.setProperty("org.graphstream.ui", "swing"); // or "j2d" for a 2D viewer

        Graph graph = new MultiGraph("RG_TPN");
        graph.setStrict(false);

        Gson gson = new Gson();
        java.lang.reflect.Type jsonType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> petriNetData = gson.fromJson(jsonBuilder.toString(), jsonType);

        // Extract components from JSON
        List<String> places = (List<String>) petriNetData.get("Place");
        List<String> transitions = (List<String>) petriNetData.get("Transition");
        List<List<Double>> matrix = (List<List<Double>>) petriNetData.get("matrix");
        List<Double> marking = (List<Double>) petriNetData.get("Marking");

        // Step 3: Create the Petri Net graph
        Graph petriNetGraph = new MultiGraph("Petri Net");
        petriNetGraph.setStrict(false);
        System.setProperty("org.graphstream.ui", "swing");

        // Add places as nodes
        for (int i = 0; i < places.size(); i++) {
            String place = places.get(i);
            petriNetGraph.addNode(place);
            petriNetGraph.getNode(place).setAttribute("ui.label", place + " (" + marking.get(i).intValue() + ")");
            petriNetGraph.getNode(place).setAttribute("ui.style", "fill-color: lightblue; size: 20px;");
        }

        // Add transitions as edges
        for (int col = 0; col < transitions.size(); col++) {
            String transition = transitions.get(col);
            for (int row = 0; row < places.size(); row++) {
                double value = matrix.get(row).get(col);
                if (value < 0) {
                    // Outgoing edge from place to transition
                    String place = places.get(row);
                    String transitionNode = "t" + transition; // Unique transition node
                    if (petriNetGraph.getNode(transitionNode) == null) {
                        petriNetGraph.addNode(transitionNode);
                        petriNetGraph.getNode(transitionNode).setAttribute("ui.label", transition);
                        petriNetGraph.getNode(transitionNode).setAttribute("ui.style", "fill-color: orange; size: 15px; shape: box;");
                    }
                    String edgeId = place + "-" + transitionNode;
                    petriNetGraph.addEdge(edgeId, place, transitionNode, true);
                } else if (value > 0) {
                    // Incoming edge from transition to place
                    String place = places.get(row);
                    String transitionNode = "t" + transition; // Unique transition node
                    String edgeId = transitionNode + "-" + place;
                    petriNetGraph.addEdge(edgeId, transitionNode, place, true);
                }
            }
        }

        return petriNetGraph;
    }
}