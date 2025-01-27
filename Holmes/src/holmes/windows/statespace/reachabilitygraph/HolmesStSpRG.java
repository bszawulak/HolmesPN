package holmes.windows.statespace.reachabilitygraph;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.utilities.Tools;
import holmes.windows.statespace.Graph_Click;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HolmesStSpRG extends JFrame {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static JTextArea logField1stTab = null;
    private static JTextArea stateDetails = null;

    //komponenty:
    private JPanel mainPanel;
    private JPanel upperPanel;
    private JPanel lowerPanel;

    public HolmesStSpRG() {
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

    private JTextArea ShortestPathDetails = new JTextArea("Displaying shortest path details.");

    private JPanel uppedPanel() {
        upperPanel = new JPanel();
        upperPanel.setLayout(null);
        upperPanel.setBounds(0, 0, mainPanel.getWidth()-20, 200);
        upperPanel.setLocation(0, 0);
        upperPanel.setBorder(BorderFactory.createTitledBorder("Graph menu"));

        int panX = 20;
        int panY = 20;

        JButton button2 = new JButton("RG for PN");
        button2.setText("<html><center>Create<br />RG for PN<center></html>");
        button2.setBounds(panX, panY, 150, 40);
        button2.setMargin(new Insets(0, 0, 0, 0));
        button2.addActionListener(actionEvent -> generateReachabilityGraphAction());
        button2.setFocusPainted(false);
        upperPanel.add(button2);

        stateDetails = new JTextArea("Displaying states details.");
        stateDetails.setEditable(false); // Make it non-editable
        stateDetails.setLineWrap(true);
        stateDetails.setWrapStyleWord(true);

        JScrollPane scrollPaneStateDetails = new JScrollPane(stateDetails);
        scrollPaneStateDetails.setBounds(panX, panY + 50, 500, 120);
        upperPanel.add(scrollPaneStateDetails);

        return upperPanel;
    }

    private JPanel lowerPanel() {
        lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setLayout(null);
        lowerPanel.setBounds(0, 0, mainPanel.getWidth()-20, 550);
        lowerPanel.setLocation(0, upperPanel.getHeight());

        lowerPanel.setBorder(BorderFactory.createTitledBorder("Graph visualization"));

        logField1stTab = new JTextArea();
        logField1stTab.setLineWrap(true);
        logField1stTab.setEditable(true);
        logField1stTab.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret)logField1stTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField1stTab), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, lowerPanel.getWidth()-35, lowerPanel.getHeight()-50);
        lowerPanel.add(logFieldPanel);

        return lowerPanel;
    }

    public JPanel getLowerPanel() {
        return lowerPanel;
    }

    public JPanel getUpperPanel() {
        return upperPanel;
    }

    public JTextArea getShortestPathDetails() {
        return ShortestPathDetails;
    }

    private void generateReachabilityGraphAction() {

        PetriNet net = overlord.getWorkspace().getProject();
        RGUtil rgUtil = new RGUtilImpl(net);

        try {
            ReachabilityGraph reachabilityGraph = rgUtil.constructReachabilityGraph();
            rgUtil.printRGresult(reachabilityGraph);

            Graph graph = graphVisualization(reachabilityGraph);
            new Graph_Click(graph, getLowerPanel(), getShortestPathDetails());

            stateDetails.append("\nReachability graph generated successfully.\n");

            printRGresult(reachabilityGraph);

        } catch (Exception ex) {
            stateDetails.append("Error: " + ex.getMessage() + "\n");
            throw ex;
        } finally {
            net.restoreMarkingZero();
        }

    }

    public void printRGresult(ReachabilityGraph graph) {
        // Wyświetlenie grafu osiągalności
        stateDetails.append("\nNodes:\n");
        for (Marking marking : graph.markings) {
            stateDetails.append(marking.getName() + marking + "\n");
        }

        stateDetails.append("\nEdges:\n");
        for (Marking marking : graph.edges.keySet()) {
            for (Map.Entry<String, Marking> edge : graph.edges.get(marking).entrySet()) {
                stateDetails.append(marking.getName() + marking
                        + " --" + edge.getKey() + "--> "
                        + edge.getValue().getName() + edge.getValue().toString() + "\n");
            }
        }
    }

    public static Set<String> transitions_involved_in_graph = new HashSet<>();
    private static Graph graphVisualization(ReachabilityGraph rg) {
        System.setProperty("org.graphstream.ui", "swing"); // or "j2d" for a 2D viewer

        Graph graph = new MultiGraph("RG");
        graph.setStrict(false);

        int idName = 0;
        for (Marking marking : rg.markings) {
            marking.setName("m_" + idName);
            idName++;

            Node node = graph.addNode(marking.getName());
            node.setAttribute("ui.label", marking.getName() + marking);
            node.setAttribute("ui.style", "text-offset: -10");
        }

        idName = 0;
        for (Marking marking : rg.markings) {
            if (rg.edges.get(marking) == null) {
                continue;
            }
            for (Map.Entry<String, Marking> edge : rg.edges.get(marking).entrySet()) {
                String transitionName = edge.getKey();
                transitions_involved_in_graph.add(transitionName);

                Edge arc = graph.addEdge(transitionName + "_" + idName, marking.getName(), edge.getValue().getName(), true);
                arc.setAttribute("ui.label", transitionName);
                arc.setAttribute("text-alignment", "along");
                idName++;
            }
        }

        // Set Graph Styling
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet", "edge { text-alignment: along; text-background-mode: rounded-box; text-background-color: white; }"); //jest along

        graph.nodes().forEach(n -> n.setAttribute("ui.style", "fill-color:  grey; size: 15px;"));
        graph.edges().forEach(e -> e.setAttribute("ui.style", "fill-color: black; size: 2px;"));

        graph.nodes().forEach(n1 -> {
            graph.nodes().forEach(n2 -> {

                if(!n1.equals(n2)){
                    long edgeCount = graph.edges().filter(e->e.getSourceNode().equals(n1) && e.getTargetNode().equals(n2)).count();

                    if(edgeCount > 1) {
                        int[] offSet = {10};
                        graph.edges().filter(e->e.getSourceNode().equals(n1) && e.getTargetNode().equals(n2)).forEach(e -> {
                            e.setAttribute("ui.style", "text-offset: " + offSet[0] + ";");

                            offSet[0] += -20;
                        });
                    }
                }
            });
        });
        return graph;

    }


}
