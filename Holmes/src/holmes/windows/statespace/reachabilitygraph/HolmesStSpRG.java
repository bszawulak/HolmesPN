package holmes.windows.statespace.reachabilitygraph;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.util.ArrayList;

public class HolmesStSpRG extends JFrame {
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private JTextArea logField1stTab = null;

    //komponenty:
    private JPanel mainPanel;
    private JPanel upperPanel;
    private JPanel lowerPanel;

    public HolmesStSpRG() {
        this.setTitle("Reachability graph analysis");
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (533315487) | Exception:  " + ex.getMessage(), "error", true);
        }


        //oblokowuje główne okno po zamknięciu tego
        addWindowListener(new WindowAdapter() {
            @Override
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
        upperPanel.setBounds(0, 0, mainPanel.getWidth() - 20, 200);
        upperPanel.setLocation(0, 0);
        upperPanel.setBorder(BorderFactory.createTitledBorder("First panel:"));

        int panX = 20;
        int panY = 20;

        upperPanel.add(createReachabilityGraphGenerationButton(panX, panY));

        return upperPanel;
    }

    private JButton createReachabilityGraphGenerationButton(int panX, int panY) {
        JButton buttonRGGen = new JButton("RG_gen1");
        buttonRGGen.setText("<html><center>Generate<center></html>");
        buttonRGGen.setBounds(panX, panY, 150, 40);
        buttonRGGen.setMargin(new Insets(0, 0, 0, 0));
        buttonRGGen.setIcon(Tools.getResIcon32("/icons/componentsManager/compIcon.png"));
        buttonRGGen.addActionListener(actionEvent -> generateReachabilityGraphAction());
        buttonRGGen.setFocusPainted(false);
        return buttonRGGen;
    }

    private JPanel lowerPanel() {
        lowerPanel = new JPanel(new BorderLayout());
        lowerPanel.setLayout(null);
        lowerPanel.setBounds(0, 0, mainPanel.getWidth() - 20, 550);
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

    private void generateReachabilityGraphAction() {
        RGUtil rgUtil = new RGUtilImpl();

        PetriNet net = overlord.getWorkspace().getProject();
        Marking actualMarking = rgUtil.getActualMarking(net);
        System.out.println(actualMarking);
        RealVector M = actualMarking.toVector();
        System.out.println("M");
        System.out.println(M);


        IncidenceMatrix incidenceMatrix = new IncidenceMatrix(net);
        incidenceMatrix.printToConsole();
        RealMatrix I = incidenceMatrix.get();

        double[] TArray = {0, 0, 1};
        RealVector T = new ArrayRealVector(TArray);

        M = calculateNextState(I, M, T);

        System.out.println("M'");
        System.out.println(M);




//        ReachabilityGraph reachabilityGraph = rgUtil.constructReachabilityGraph(net.getTransitions(), actualMarking);
//        rgUtil.printRGresult(reachabilityGraph);

    }

    // Funkcja obliczająca nowy stan M' = M + I * T
    public static RealVector calculateNextState(RealMatrix I, RealVector M, RealVector T) {
        return M.add(I.operate(T));
    }

    /**
     * Metoda przykładowa, pobiera pierwsze dwie tranzycje sieci i pierwsze dwa miejsca (jeżeli istnieją), a
     * następnie wyświetla informacje o nim w polu logField1stTab (globalne).
     */
    private void exampleMethod() {
        //pobiera listę tranzycji i miejsc z projektu
        ArrayList<Transition> transitions = overlord.getWorkspace().getProject().getTransitions();
        ArrayList<Place> places = overlord.getWorkspace().getProject().getPlaces();

        if (transitions.size() > 1) {
            Transition t1 = overlord.getWorkspace().getProject().getTransitions().get(0);
            logField1stTab.append("Transition 1: " + t1.getName() + "\n");
            // pobierz miejsca wejściowe:
            ArrayList<Place> inputPlaces = t1.getInputPlaces();
            for (Place p : inputPlaces) {
                logField1stTab.append("Input place: " + p.getName() + "\n");
            }
            // pobierz miejsca wyjściowe:
            ArrayList<Place> outputPlaces = t1.getOutputPlaces();
            for (Place p : outputPlaces) {
                logField1stTab.append("Output place: " + p.getName() + "\n");
            }
        } else {
            logField1stTab.append("No transitions in the project.\n");
        }

        if (places.size() > 1) {
            Place p1 = overlord.getWorkspace().getProject().getPlaces().get(0);
            logField1stTab.append("Place 1: " + p1.getName() + "\n");
            // pobierz tranzycje wejściowe:
            ArrayList<Transition> inputTransitions = p1.getInputTransitions();
            for (Transition t : inputTransitions) {
                logField1stTab.append("Input transition: " + t.getName() + "\n");
            }
            // pobierz tranzycje wyjściowe:
            ArrayList<Transition> outputTransitions = p1.getOutputTransitions();
            for (Transition t : outputTransitions) {
                logField1stTab.append("Output transition: " + t.getName() + "\n");
            }
        } else {
            logField1stTab.append("No places in the project.\n");
        }



    }
}
