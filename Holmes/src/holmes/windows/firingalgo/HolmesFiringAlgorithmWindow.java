package holmes.windows.firingalgo;

import holmes.analyse.firingalgo.DetermineFiringDelayAlgo;
import holmes.analyse.firingalgo.petrinetstructure.Transition;
import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SPNdataVector;
import holmes.petrinet.data.SPNdataVectorManager;
import holmes.petrinet.elements.extensions.TransitionSPNExtension;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.function.Function;
import java.util.List;

public class HolmesFiringAlgorithmWindow extends JFrame {
    private static final int headerHeight = 200;
    private static final int rowHeight = 30;
    private static final int spaceBetweenRows = 5;
    private static final int width = 350;
    private static final int labelWidth = 200;
    private static final int verticalMargin = 5;
    private static final int horizontalMargin = 5;

    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();
    private static JTextArea logOutput = null;

    private JComboBox<String> vectorSelect = null;

    public HolmesFiringAlgorithmWindow(JFrame launcherFrame) {
        super("Estimate firing rates algorithm");

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                launcherFrame.setEnabled(true);
            }
        });
        launcherFrame.setEnabled(false);

        setLayout(null);
        setVisible(true);

        JPanel panel = createWindowPanel();
        add(panel);
        Dimension size = panel.getSize();

        Insets insets = getInsets();
        size.width += insets.left + insets.right;
        size.height += insets.top + insets.bottom;
        setSize(size);
        setResizable(false);
    }

    private JPanel createWindowPanel() {
        JPanel panel = new JPanel(null);

        List<Function<Rectangle, JPanel>> panelCreators = List.of(
                this::createIndexSelectorPanel,
                this::createRunButtonPanel
        );
        panel.setBounds(0, 0, width + horizontalMargin * 2, panelCreators.size() * (rowHeight + spaceBetweenRows) - spaceBetweenRows + verticalMargin * 2);

        Rectangle rectangle = new Rectangle(horizontalMargin, verticalMargin, width, rowHeight);
        for (Function<Rectangle, JPanel> createPanel : panelCreators) {
            panel.add(createPanel.apply(rectangle));
            rectangle.y += rowHeight + spaceBetweenRows;
        }

        return panel;
    }

    private JPanel createIndexSelectorPanel(Rectangle rectange) {
        JPanel panel = new JPanel(null);
        panel.setBounds(rectange);

        JLabel label = new JLabel(lang.getText("HSPN_FRA_select_table"));
        label.setBounds(0, 0, labelWidth, rowHeight);
        panel.add(label);

        SPNdataVectorManager firingRatesManager = overlord.getWorkspace().getProject().accessFiringRatesManager();
        int vectorNumber = firingRatesManager.accessSPNmatrix().size();
        vectorSelect = new JComboBox<String>();
        for (int i = 0; i < vectorNumber; i++) {
            vectorSelect.addItem(i + ": " + firingRatesManager.getSPNvectorDescription(i));
        }
        vectorSelect.setBounds(labelWidth, 0, width - labelWidth, rowHeight);
        panel.add(vectorSelect);

        return panel;
    }

    private JPanel createRunButtonPanel(Rectangle rectangle) {
        JPanel panel = new JPanel(null);
        panel.setBounds(rectangle);

        JButton button = new JButton(lang.getText("HSPN_FRA_run_algorithm"));
        button.addActionListener(actionEvent -> {
            runAlgorithm();
        });
        button.setFocusPainted(false);
        button.setBounds(labelWidth, 0, width - labelWidth, rowHeight);
        panel.add(button);

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

    private void runAlgorithm() {
        DetermineFiringDelayAlgo algorithm = new DetermineFiringDelayAlgo();

        int index = vectorSelect.getSelectedIndex();
        PetriNet petriNet = overlord.getWorkspace().getProject();
        SPNdataVectorManager spnManager = petriNet.accessFiringRatesManager();
        SPNdataVector firingRatesVector = spnManager.getSPNdataVector(index);

        algorithm.run(petriNet, firingRatesVector);

        HashMap<Transition, Double> result = algorithm.getResult();

        SPNdataVector dataVector = new SPNdataVector();
        for (holmes.petrinet.elements.Transition pnTransition : petriNet.getTransitions()) {
            Double value = result.keySet().stream()
                    .filter(t -> t.transitionRef.equals(pnTransition))
                    .map(t -> t.firingRate)
                    .max(Double::compare)
                    .orElse(null);
            if (value != null) {
                dataVector.addTrans(value.toString(), TransitionSPNExtension.StochaticsType.ST);
            }
            else {
                dataVector.addTrans("0", TransitionSPNExtension.StochaticsType.ST);
            }
        }
        dataVector.setDescription("Generated");

        spnManager.accessSPNmatrix().add(dataVector);
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }
}
