package holmes.windows.firingalgo;

import holmes.analyse.firingalgo.DetermineFiringDelayAlgo;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.darkgui.toolbar.Toolbar;
import holmes.analyse.firingalgo.petrinetstructure.Transition;
import holmes.utilities.Tools;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;

public class HolmesFiringAlgorithmWindow extends JFrame {
    private static final int headerHeight = 200;

    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private static JTextArea logOutput = null;

    public static JButton createButton(int x, int y, int width, int height) {
        JButton button = new JButton(lang.getText("HSPN_determine_rates_button_label"));
        button.setBounds(x, y, width, height);
        button.setMargin(new Insets(0, 0,0,0));
        button.setIcon(Tools.getResIcon48("/icons/holmesicon.png"));
        button.addActionListener(actionEvent -> {
            HolmesFiringAlgorithmWindow firingAlgorithmWindow = new HolmesFiringAlgorithmWindow();
        });
        return button;
    }

    public HolmesFiringAlgorithmWindow() {
        this.setTitle("Estimate firing rates algorithm");

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                overlord.getFrame().setEnabled(true);
            }
        });
        overlord.getFrame().setEnabled(false);

        add(createDebugLayout(), BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createDebugLayout() {
        setLayout(new BorderLayout());
        setSize(new Dimension(1024, 768));
        setResizable(false);

        JPanel mainPanel = new JPanel(null);
        mainPanel.setBounds(0, 0, 1024, 768);
        mainPanel.setLocation(0, 0);

        mainPanel.add(createHeaderPanel(mainPanel));
        mainPanel.add(createLogOutputPanel(mainPanel));
        return mainPanel;
    }

    private JPanel createHeaderPanel(JPanel parent) {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, parent.getWidth() - 20, headerHeight);
        panel.setBorder(BorderFactory.createTitledBorder("Menu:"));

        int x = 20;
        int y = 20;
        int buttonHeight = headerHeight - 40;
        int buttonWidth = headerHeight - 40;
        int space = 10;

        JButton runAlgorithmButton = new JButton("Run algorithm");
        runAlgorithmButton.setText("<html><center>Run algorithm<center></html>");
        runAlgorithmButton.setBounds(x, y, buttonWidth, buttonHeight);
        runAlgorithmButton.setMargin(new Insets(0, 0, 0, 0));
        runAlgorithmButton.addActionListener(actionEvent -> {
            DetermineFiringDelayAlgo algorithm = new DetermineFiringDelayAlgo();
            algorithm.run();


            StringBuilder sb = new StringBuilder();
            var result = algorithm.getResult();
            for (Transition t : result.keySet()) {
                sb.append(t.transitionRef.getName());
                sb.append("\n");
                sb.append(result.get(t));
                sb.append("\n");
            }

            logOutput.append(sb.toString());

            //TODO
            //overlord.getWorkspace().getProject().accessFiringRatesManager().
        });
        runAlgorithmButton.setFocusPainted(false);
        panel.add(runAlgorithmButton);

        x += buttonHeight + space;

        return panel;
    }

    private JPanel createLogOutputPanel(JPanel parent) {
        JPanel panel = new JPanel(null);
        panel.setBounds(0, 0, parent.getWidth() - 20, parent.getHeight() - headerHeight - 20);
        panel.setLocation(0, headerHeight + 10);

        panel.setBorder(BorderFactory.createTitledBorder("Output:"));

        logOutput = new JTextArea();
        logOutput.setLineWrap(true);
        logOutput.setEditable(false);
        logOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultCaret caret = (DefaultCaret)logOutput.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JPanel logFieldPanel = new JPanel();
        logFieldPanel.setLayout(new BorderLayout());
        logFieldPanel.add(new JScrollPane(logOutput), BorderLayout.CENTER);
        logFieldPanel.setBounds(10, 20, panel.getWidth()-35, panel.getHeight()-50);
        panel.add(logFieldPanel);

        return panel;
    }
}
