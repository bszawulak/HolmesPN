package holmes.windows.statespace;

import holmes.analyse.matrix.InputMatrix;
import holmes.analyse.matrix.OutputMatrix;
import holmes.graphpanel.GraphPanel;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.P_StateManager;
import holmes.petrinet.data.StatePlacesVector;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;
import holmes.windows.statespace.Graph_Click;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;


import org.graphstream.graph.*;
import org.graphstream.graph.implementations.*;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.*;
import org.graphstream.ui.spriteManager.*;
import org.graphstream.ui.view.*;
import org.graphstream.ui.swing_viewer.SwingViewer;
import javax.swing.JComponent;


public class HolmesStSpRP extends JFrame {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static JTextArea logField1stTab = null;

    // Referencja do menedżera stanów
    private P_StateManager stateManager;

    //komponenty:
    private JPanel mainPanel;
    private JPanel upperPanel;
    private JPanel lowerPanel;

    public HolmesStSpRP() {
        this.setTitle("State space analysis");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (533315487) | Exception:  " + ex.getMessage(), "error", true);
        }

        // Pobranie P_StateManager
        this.stateManager = overlord.getWorkspace().getProject().accessStatesManager();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });
        overlord.getFrame().setEnabled(false);
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
        mainPanel.setLayout(new BorderLayout()); // Główny układ w BorderLayout

        // Górny panel z przyciskiem i mniejszym polem tekstowym
        mainPanel.add(uppedPanel(), BorderLayout.NORTH);

        // Dolny panel na duże pole na graf
        mainPanel.add(lowerPanel(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel uppedPanel() {
        upperPanel = new JPanel();
        upperPanel.setLayout(new BorderLayout()); // Używamy BorderLayout dla lepszego rozmieszczenia

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Przycisk do wybrania dwóch stanów
        JButton selectStatesButton = new JButton("Uruchom Algorytm");
        selectStatesButton.addActionListener(actionEvent -> openSelectStatesDialog());
        buttonPanel.add(selectStatesButton);

        upperPanel.add(buttonPanel, BorderLayout.NORTH);

        // Mniejsze pole tekstowe poniżej przycisku
        JPanel logPanel = new JPanel();
        logPanel.setLayout(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Log"));

        logField1stTab = new JTextArea();
        logField1stTab.setLineWrap(true);
        logField1stTab.setEditable(false); // Pole logów zwykle nie jest edytowalne
        logField1stTab.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        // Dodaj JScrollPane wokół pola tekstowego
        JScrollPane logScrollPane = new JScrollPane(logField1stTab);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        logPanel.add(logScrollPane, BorderLayout.CENTER);

        logPanel.setPreferredSize(new Dimension(1024, 150)); // Ustawienie mniejszego rozmiaru
        upperPanel.add(logPanel, BorderLayout.SOUTH);

        return upperPanel;
    }


    private JPanel lowerPanel() {
        lowerPanel = new JPanel();
        lowerPanel.setLayout(new BorderLayout());
        lowerPanel.setBorder(BorderFactory.createTitledBorder("Graph Visualization"));

        // Placeholder na graf (można później zastąpić właściwą wizualizacją)
        JLabel placeholderLabel = new JLabel("Graph will be displayed here", SwingConstants.CENTER);
        placeholderLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        lowerPanel.add(placeholderLabel, BorderLayout.CENTER);

        return lowerPanel;
    }

    /**
     * Wizualizuje przestrzeń stanów w lowerPanel przy pomocy GraphStream.
     */
    private void visualizeResults(ArrayList<StatePlacesVector> pathStates, ArrayList<Transition> transitions,
                                  int[][] inputMatrix, int[][] outputMatrix) {
        if (pathStates.isEmpty()) {
            logField1stTab.append("Error: No states to visualize.\n");
            return;
        }

        // Tworzenie grafu
        Graph graph = new MultiGraph("State Space Visualization");
        System.setProperty("org.graphstream.ui", "swing");

        // Dodawanie węzłów (stanów)
        for (int i = 0; i < pathStates.size(); i++) {
            StatePlacesVector state = pathStates.get(i);
            String nodeId = "State " + i;

            // Dodanie węzła
            graph.addNode(nodeId);

            // Etykieta węzła: stan jako string
            String stateLabel = stateToString(state);
            Node node = graph.getNode(nodeId);
            node.setAttribute("ui.label", stateLabel);
            node.setAttribute("ui.style", "text-offset: -10");
        }

        // Dodawanie krawędzi (tranzycji)
        for (int i = 1; i < pathStates.size(); i++) {
            StatePlacesVector currentState = pathStates.get(i - 1);
            StatePlacesVector nextState = pathStates.get(i);

            Transition transition = findTransitionBetweenStates(
                    currentState, nextState, inputMatrix, outputMatrix, transitions);

            if (transition != null) {
                String edgeId = "Edge " + (i - 1) + "->" + i;
                String sourceNodeId = "State " + (i - 1);
                String targetNodeId = "State " + i;

                // Dodanie krawędzi
                graph.addEdge(edgeId, sourceNodeId, targetNodeId, true)
                        .setAttribute("ui.label", transition.getName());
            }
        }

        // Stylizacja grafu
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");
        graph.setAttribute("ui.stylesheet",
                "node { fill-color: blue; size: 15px; text-alignment: center; text-size: 14px; } " +
                        "edge { fill-color: black; size: 2px; text-alignment: along; text-size: 12px; }");

        // Tworzenie widoku jako komponent Swing bez dodatkowego okna
        SwingViewer viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.enableAutoLayout();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);

        View view = viewer.addDefaultView(false); // false zapobiega otwieraniu nowego okna

        // Dodanie widoku do panelu
        lowerPanel.removeAll();
        lowerPanel.setLayout(new BorderLayout());
        lowerPanel.add((Component) view, BorderLayout.CENTER);
        lowerPanel.revalidate();
        lowerPanel.repaint();

        // Tworzenie instancji klasy Graph_Click
        JTextArea shortestPathDetails = new JTextArea("Displaying shortest path details.");
        new Graph_Click(graph, lowerPanel, shortestPathDetails);
    }




    /**
     * Konwertuje stan na format tekstowy, np. [1, 0, 0, 0].
     */
    private String stateToString(StatePlacesVector state) {
        StringBuilder sb = new StringBuilder("[");
        ArrayList<Double> vector = state.accessVector();
        for (int i = 0; i < vector.size(); i++) {
            sb.append((int) Math.floor(vector.get(i))); // Konwersja na liczbę całkowitą
            if (i < vector.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Otwiera okno dialogowe do wyboru dwóch stanów z zapisanej listy.
     */
    private void openSelectStatesDialog() {
        // Pobranie listy zapisanych stanów z menedżera
        ArrayList<StatePlacesVector> allStates = stateManager.accessStateMatrix();

        if (allStates.size() < 2) {
            logField1stTab.append("Error: At least two states are required.\n");
            return;
        }

        String[] stateDescriptions = new String[allStates.size()];
        for (int i = 0; i < allStates.size(); i++) {
            stateDescriptions[i] = "State " + i + ": " + allStates.get(i).getDescription();
        }

        JComboBox<String> startStateComboBox = new JComboBox<>(stateDescriptions);
        JComboBox<String> endStateComboBox = new JComboBox<>(stateDescriptions);

        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Start State:"));
        panel.add(startStateComboBox);
        panel.add(new JLabel("End State:"));
        panel.add(endStateComboBox);

        int result = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Select Start and End States",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            int startIndex = startStateComboBox.getSelectedIndex();
            int endIndex = endStateComboBox.getSelectedIndex();

            if (startIndex == endIndex) {
                logField1stTab.append("Error: Start and End States must be different.\n");
                return;
            }

            StatePlacesVector startState = allStates.get(startIndex);
            StatePlacesVector endState = allStates.get(endIndex);

            // Przygotowanie danych wejściowych
            ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
            ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();

            InputMatrix inputMatrix = new InputMatrix(transitions, places);
            OutputMatrix outputMatrix = new OutputMatrix(transitions, places);

            int[][] input_Matrix = inputMatrix.getMatrix();
            int[][] output_Matrix = outputMatrix.getMatrix();

            // Tworzymy instancję notatnika
            HolmesNotepad notepad = new HolmesNotepad(600, 400, true);

            // Wyświetlanie danych w notatniku
            notepad.addTextLineNL("Input Matrix:", "bold");
            for (int[] row : input_Matrix) {
                notepad.addTextLineNL(java.util.Arrays.toString(row), "regular");
            }

            notepad.addTextLineNL("Output Matrix:", "bold");
            for (int[] row : output_Matrix) {
                notepad.addTextLineNL(java.util.Arrays.toString(row), "regular");
            }

            notepad.addTextLineNL("Start State:", "bold");
            notepad.addTextLineNL(formatStateInline(startState), "regular");

            notepad.addTextLineNL("End State:", "bold");
            notepad.addTextLineNL(formatStateInline(endState), "regular");

            notepad.setVisible(true);

            // Przygotowanie danych JSON do C++
            String jsonData = prepareJsonData(startState, endState, input_Matrix, output_Matrix);

            // Uruchomienie programu C++ i parsowanie wyników
            StringBuilder cppResult = runCppProgram(jsonData);
            System.out.println("Otrzymano dane z C++:");
            System.out.println(cppResult.toString());

            ArrayList<StatePlacesVector> pathStates = parseCppResult(cppResult);

            if (pathStates.isEmpty()) {
                logField1stTab.append("Error: Parsed path is empty!\n");
                return;
            }

            // Wyświetlanie ścieżki w dolnym logu
            StringBuilder resultLog = new StringBuilder();

            for (int i = 1; i < pathStates.size(); i++) {
                StatePlacesVector currentState = pathStates.get(i - 1);
                StatePlacesVector nextState = pathStates.get(i);

                Transition transition = findTransitionBetweenStates(
                        currentState, nextState, input_Matrix, output_Matrix, transitions);

                if (transition != null) {
                    if (i == 1) { // Dodaj pierwszy stan tylko raz
                        resultLog.append(formatStateInline(currentState)).append("\n");
                    }
                    resultLog.append(formatStateInline(nextState))
                            .append("  ").append(transition.getName()).append("\n");
                }
            }

            logField1stTab.setText(resultLog.toString());
            if (!pathStates.isEmpty()) {
                visualizeResults(pathStates, transitions, input_Matrix, output_Matrix);
            }


        }


    }



    /**
     * Formatuje wektor stanu jako tekst.
     */
    private String formatState(StatePlacesVector state) {
        StringBuilder sb = new StringBuilder();
        ArrayList<Double> stateVector = state.accessVector();
        for (int i = 0; i < stateVector.size(); i++) {
            sb.append("Place ").append(i).append(": ").append(stateVector.get(i)).append(" tokens\n");
        }
        return sb.toString();
    }

    /**
     * Formatuje wektor stanu jako tekst w jednej linii.
     */
    private String formatStateInline(StatePlacesVector state) {
        StringBuilder sb = new StringBuilder("[");
        ArrayList<Double> stateVector = state.accessVector();
        for (int i = 0; i < stateVector.size(); i++) {
            sb.append((int) Math.floor(stateVector.get(i))); // Zaokrąglanie do liczby całkowitej
            if (i < stateVector.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }


    /**
     * Znajduje tranzycję, która prowadzi od jednego stanu do drugiego.
     */
    private Transition findTransitionBetweenStates(
            StatePlacesVector currentState,
            StatePlacesVector nextState,
            int[][] inputMatrix,
            int[][] outputMatrix,
            ArrayList<Transition> transitions) {

        ArrayList<Double> tokens1 = currentState.accessVector();
        ArrayList<Double> tokens2 = nextState.accessVector();

        for (int t = 0; t < transitions.size(); t++) {
            boolean valid = true;

            // Iterujemy po miejscach (kolumnach w macierzy)
            for (int p = 0; p < tokens1.size(); p++) {
                // Sprawdzenie zakresów (w macierzach wiersze to tranzycje, kolumny to miejsca)
                if (p >= inputMatrix[t].length || p >= outputMatrix[t].length) {
                    logField1stTab.append("Debug: Skipping out-of-bounds index t=" + t + ", p=" + p + "\n");
                    valid = false;
                    break;
                }

                // Obliczamy różnicę w tokenach i porównujemy z wartościami w macierzach
                int delta = (int) (tokens2.get(p) - tokens1.get(p));
                int expectedDelta = outputMatrix[t][p] - inputMatrix[t][p];

                if (delta != expectedDelta) {
                    valid = false;
                    break;
                }
            }

            if (valid) {
                return transitions.get(t);
            }
        }

        logField1stTab.append("Debug: No valid transition found between the states.\n");
        return null; // Jeśli nie znaleziono tranzycji
    }








    /**
     * Parsuje wynik zwrócony przez program C++ na listę stanów.
     */
    /**
     * Parsuje wynik zwrócony przez program C++ na listę stanów.
     */
    private ArrayList<StatePlacesVector> parseCppResult(StringBuilder cppResult) {
        ArrayList<StatePlacesVector> pathStates = new ArrayList<>();
        String[] lines = cppResult.toString().split("\n");

        for (String line : lines) {
            // Sprawdzamy, czy linia reprezentuje stan (zaczyna się od "[" i kończy na "]")
            if (line.startsWith("[") && line.endsWith("]")) {
                line = line.substring(1, line.length() - 1); // Usuń nawiasy
                String[] tokens = line.split(",");
                StatePlacesVector state = new StatePlacesVector();
                for (String token : tokens) {
                    try {
                        state.addPlace(Double.parseDouble(token.trim()));
                    } catch (NumberFormatException e) {
                        // Możesz logować błędy, jeśli to konieczne
                    }
                }
                logField1stTab.append(stateToString(state) + "\n");
                pathStates.add(state);
            }
        }

        // Logowanie tylko wynikowej ścieżki
        logField1stTab.setText(""); // Wyczyszczenie logów
        for (StatePlacesVector state : pathStates) {
            logField1stTab.append(stateToString(state) + "\n");
        }

        return pathStates;
    }







    private static StringBuilder runCppProgram(String jsonData) {
        StringBuilder result = new StringBuilder();

        try {
            // Ścieżka do programu C++

            String workingDirectory = System.getProperty("user.dir");

            String cppProgramPath = workingDirectory + File.separator + "Holmes" + File.separator + "scripts" + File.separator + "RP" + File.separator + "x64" + File.separator + "Debug" + File.separator + "shortestpath.exe";

            // Uruchomienie programu jako proces
            ProcessBuilder processBuilder = new ProcessBuilder(cppProgramPath);
            Process process = processBuilder.start();

            // Przesyłanie danych wejściowych do programu C++
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(jsonData);
            writer.newLine();
            writer.flush();

            // Odczytanie danych wyjściowych z programu C++
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            process.waitFor(); // Czekaj na zakończenie programu C++

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    private String prepareJsonData(StatePlacesVector startState, StatePlacesVector endState, int[][] inputMatrix, int[][] outputMatrix) {
        StringBuilder jsonBuilder = new StringBuilder("{");

        // Dodanie macierzy inputMatrix
        jsonBuilder.append("\"inputMatrix\":").append(matrixToJson(inputMatrix)).append(",");

        // Dodanie macierzy outputMatrix
        jsonBuilder.append("\"outputMatrix\":").append(matrixToJson(outputMatrix)).append(",");

        // Dodanie stanu początkowego (m0)
        jsonBuilder.append("\"startState\":").append(stateVectorToJson(startState.accessVector())).append(",");

        // Dodanie stanu końcowego (md)
        jsonBuilder.append("\"endState\":").append(stateVectorToJson(endState.accessVector()));

        jsonBuilder.append("}");
        return jsonBuilder.toString();
    }


    // Metoda konwertująca wektor stanu na JSON
    private String stateVectorToJson(ArrayList<Double> vector) {
        StringBuilder builder = new StringBuilder("[");
        for (double value : vector) {
            builder.append((int) value).append(",");
        }
        if (builder.length() > 1) {
            builder.setLength(builder.length() - 1); // Usuń ostatni przecinek
        }
        builder.append("]");
        return builder.toString();
    }

    // Metoda konwertująca macierz na JSON
    private String matrixToJson(int[][] matrix) {
        StringBuilder builder = new StringBuilder("[");
        for (int[] row : matrix) {
            builder.append("[");
            for (int value : row) {
                builder.append(value).append(",");
            }
            if (builder.length() > 1) {
                builder.setLength(builder.length() - 1); // Usuń ostatni przecinek
            }
            builder.append("],");
        }
        if (builder.length() > 1) {
            builder.setLength(builder.length() - 1); // Usuń ostatni przecinek
        }
        builder.append("]");
        return builder.toString();

    }

}
