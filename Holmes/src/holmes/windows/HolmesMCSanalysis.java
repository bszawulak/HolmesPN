package holmes.windows;

import holmes.analyse.InvariantsCalculator;
import holmes.analyse.InvariantsTools;
import holmes.darkgui.GUIManager;
import holmes.petrinet.data.MCSDataMatrix;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.utilities.Tools;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class HolmesMCSanalysis extends JFrame {
    private GUIManager overlord;
    private JFrame ego;

    private JTextArea logField1stTab;
    private JTextArea logField2ndTab;

    int windowWidth = 1024;
    int windowHeight = 800;

    ArrayList<Transition> transitions = null;
    ArrayList<Place> places = null;

    MCSDataMatrix mcsd = null;

    public HolmesMCSanalysis() {
        try {
            setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            GUIManager.getDefaultGUIManager().log("Error (641103817) | Exception:  " + ex.getMessage(), "error", true);

        }
        this.ego = this;
        setVisible(false);
        this.setTitle("MCS Analysis'n'stuff ");
        this.overlord = GUIManager.getDefaultGUIManager();
        //ego = this;

        setLayout(new BorderLayout());
        setSize(new Dimension(windowWidth, windowHeight));
        setLocation(50, 50);
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        JPanel mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createMainPanel() {
        JPanel main = new JPanel(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel mainPanel1stTab = new JPanel();
        mainPanel1stTab.setLayout(null); //  ╯°□°）╯︵  ┻━┻
        JPanel buttonPanelT = createUpperButtonPanel1stTab(0, 0, windowWidth, 90);
        JPanel logMainPanelT = createLogMainPanel1stTab(0, 90, windowWidth, windowHeight-120);

        mainPanel1stTab.add(buttonPanelT);
        mainPanel1stTab.add(logMainPanelT);
        mainPanel1stTab.repaint();

        tabbedPane.addTab("1st Tab", Tools.getResIcon22("/icons/invWindow/tInvIcon.png"), mainPanel1stTab, "T-invariants");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        JPanel mainPanel2ndTab = new JPanel();
        mainPanel2ndTab.setLayout(null); //  ╯°□°）╯︵  ┻━┻
        JPanel buttonPanelP = createUpperButtonPanel2ndTab(0, 0, windowWidth, 90);
        JPanel logMainPanelP = createLogMainPanel2ndTab(0, 90, windowWidth, windowHeight-120);

        mainPanel2ndTab.add(buttonPanelP);
        mainPanel2ndTab.add(logMainPanelP);
        mainPanel2ndTab.repaint();


        tabbedPane.addTab("2nd Tab", Tools.getResIcon22("/icons/invWindow/pInvIcon.png"), mainPanel2ndTab, "P-invariants");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);

        main.add(tabbedPane, BorderLayout.CENTER);

        return main;
    }

    private JPanel createUpperButtonPanel1stTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setBounds(x, y, width, height);

        int posX = 10;
        int posY = 10;

        JButton button1 = new JButton("Button 1");
        button1.setBounds(posX, posY, 110, 40);
        button1.setMargin(new Insets(0, 0, 0, 0));
        button1.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        button1.addActionListener(actionEvent -> {
            ; // do smth
        });
        button1.setFocusPainted(false);
        panel.add(button1);

        JButton button2 = new JButton("Button 2");
        button2.setText("Button 2");
        button2.setBounds(posX + 120, posY, 110, 40);
        button2.setMargin(new Insets(0, 0, 0, 0));
        button2.setIcon(Tools.getResIcon22("/icons/invWindow/inaGenerator.png"));
        button2.addActionListener(actionEvent -> {

            ; //doAnything
        });
        button2.setFocusPainted(false);
        panel.add(button2);

        JButton button3 = new JButton("Load MCS");
        button3.setText("Load MCS");
        button3.setBounds(posX + 240, posY, 110, 40);
        button3.setMargin(new Insets(0, 0, 0, 0));
        button3.setIcon(Tools.getResIcon22("/icons/invWindow/makeFeasible.png"));
        button3.addActionListener(actionEvent -> {
            places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
            transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

            mcsd = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMCSdataCore();

            if(mcsd.getSize() == 0) {
                logField1stTab.append(" *** BRAK DANYCH MCS! \n");
                return;
            }

            int transitionIndex = 0;

            ArrayList<ArrayList<Integer>> dataVector = mcsd.getMCSlist(transitionIndex);

            if(dataVector == null) {
                logField1stTab.append(" *** Brak zbiorów MCS dla tranzycji o numerze: " + transitionIndex + "\n");
                return;
            }

            Transition objR = transitions.get(transitionIndex);
            logField1stTab.append("==========================================================\n");
            logField1stTab.append("Tranzycja objR: "+objR.getName()+"\n");
            logField1stTab.append("Liczba zbiorów MCS: "+dataVector.size()+"\n");

            int counter = 0;
            StringBuilder msg;
            for(ArrayList<Integer> set : dataVector) {
                logField1stTab.append("MSC#"+counter+" ");
                msg = new StringBuilder("[");
                for(int el : set) {
                    msg.append(el).append(", ");
                }
                msg.append("]   : ");
                msg = new StringBuilder(msg.toString().replace(", ]", "]"));

                if(true) {
                    int transSize = transitions.size();
                    StringBuilder names = new StringBuilder();
                    for(int el : set) {
                        if(el < transSize) {
                            names.append("t").append(el).append("_").append(transitions.get(el).getName()).append("; ");
                        }
                    }
                    msg.append(names);
                }

                logField1stTab.append(msg+"\n");
                counter++;
            }

            logField1stTab.append("==========================================================\n");
            logField1stTab.append("\n");


        });
        button3.setFocusPainted(false);
        panel.add(button3);

        JButton buttonData = new JButton("Button 4");
        buttonData.setText("Button TEST");
        buttonData.setBounds(posX + 600, posY, 120, 60);
        buttonData.setMargin(new Insets(0, 0, 0, 0));
        buttonData.setIcon(Tools.getResIcon22("/icons/invWindow/showInvariants.png"));
        buttonData.addActionListener(actionEvent -> {
            places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
            transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

            logField1stTab.append("********************************\n");
            logField1stTab.append("Pobrano danych z projektu: \n");
            logField1stTab.append("Tranzycji: "+transitions.size()+"\n");
            logField1stTab.append("Miejsc: "+places.size()+"\n");
            logField1stTab.append("********************************\n");

            if(places.size() == 0 || transitions.size() == 0)
                return;

            //przykład pobrania danych o jednym inwariancie, tym pierwszym z indeksu [0] jeśli jakieś istnieją
            ArrayList<ArrayList<Integer>> invariants = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getT_InvMatrix();
            if(invariants.size() > 0) {
                ArrayList<Integer> inv0 = invariants.get(9);
                for(int element : inv0) {
                    logField1stTab.append(element + " ");
                }
                logField1stTab.append("\n");

                logField1stTab.append("Wsparcie niezmiennika: \n");
                ArrayList<Integer> support0 = InvariantsTools.getSupport(inv0);
                for(int element : support0) {
                    logField1stTab.append(element + " ");
                }
                logField1stTab.append("\n");

            }

            //pobranie listy miejsc wyjściowych i wejściowych danej tranzycji:
            Transition trans0 = transitions.get(0);
            ArrayList<Place> outputPlaces =  trans0.getPostPlaces();
            ArrayList<Place> intputPlaces =  trans0.getPrePlaces();
            //i teraz, gdybyśmy chcieli, to np. dla każdego elementu obu list można sprawdzić ile mają tranzycji,
            //jakich, itd.


            ; //doAnything
        });
        buttonData.setFocusPainted(false);
        panel.add(buttonData);

        return panel;
    }

    private JPanel createLogMainPanel1stTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("1st tab log window"));
        panel.setBounds(x, y, width-20, height-50);

        logField1stTab = new JTextArea();
        logField1stTab.setLineWrap(true);
        logField1stTab.setEditable(true);
        logField1stTab.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret)logField1stTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField1stTab), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-35, height-75);
        panel.add(logFieldPanel);

        return panel;
    }


    private JPanel createUpperButtonPanel2ndTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        panel.setBounds(x, y, width, height);

        int posX = 10;
        int posY = 10;

        JButton buttonX = new JButton("<html>Button<br>X</html>");
        buttonX.setBounds(posX, posY, 110, 60);
        buttonX.setMargin(new Insets(0, 0, 0, 0));
        buttonX.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        buttonX.addActionListener(actionEvent -> {
            ;
        });
        buttonX.setFocusPainted(false);
        panel.add(buttonX);

        JButton buttonY = new JButton("<html>Button<br>Y</html>");
        buttonY.setBounds(posX+120, posY, 110, 60);
        buttonY.setMargin(new Insets(0, 0, 0, 0));
        buttonY.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
        buttonY.addActionListener(actionEvent -> {
            ;
        });
        buttonY.setFocusPainted(false);
        panel.add(buttonY);



        return panel;
    }

    private JPanel createLogMainPanel2ndTab(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBorder(BorderFactory.createTitledBorder("Log window 2nd tab"));
        panel.setBounds(x, y, width-20, height-50);

        logField2ndTab = new JTextArea();
        logField2ndTab.setLineWrap(true);
        //logField2ndTab.setEditable(false);
        DefaultCaret caret = (DefaultCaret)logField2ndTab.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logField2ndTab), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, width-35, height-75);
        panel.add(logFieldPanel);

        return panel;
    }
}
