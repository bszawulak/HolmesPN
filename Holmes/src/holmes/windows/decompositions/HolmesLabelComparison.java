package holmes.windows.decompositions;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import holmes.analyse.comparison.LabelNetComparator;
import holmes.darkgui.GUIManager;
import holmes.files.io.ProjectReader;
import holmes.petrinet.data.PetriNetData;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class HolmesLabelComparison extends JFrame {
    JTextArea resultArea = null;
    PetriNetData fPND = null;
    PetriNetData sPND = null;
    private JPanel mainPanel;

    public HolmesLabelComparison() {
        setLayout(new BorderLayout());
        setSize(new Dimension(900, 300));
        setLocation(15, 15);
        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);
        add(createOutputPanel(10,50,800,200),BorderLayout.SOUTH);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel();

        JButton getFirstNetButton = new JButton("Get first Net");
        getFirstNetButton.setBounds(10, 10, 150, 20);
        getFirstNetButton.addActionListener(actionEvent -> getNet(actionEvent,true));
        panel.add(getFirstNetButton);

        JButton getSecondNetButton = new JButton("Get second Net");
        getSecondNetButton.setBounds(200, 10, 150, 20);
        getSecondNetButton.addActionListener(actionEvent -> getNet(actionEvent,false));
        panel.add(getSecondNetButton);

        JButton compareButton = new JButton("Compare");
        compareButton.setBounds(400, 10, 150, 20);
        compareButton.addActionListener(actionEvent -> compare(actionEvent));
        panel.add(compareButton);

        JButton calc = new JButton("Compare");
        calc.setBounds(500, 10, 150, 20);
        calc.addActionListener(actionEvent -> calculate(actionEvent));
        panel.add(calc);

        return panel;
    }

    private void calculate(ActionEvent actionEvent) {
        HolmesGraphletsPrototype hgp = new HolmesGraphletsPrototype("C:\\Eksperyment\\Wyniki\\");
        hgp.compareDRGFwithoutDensity();
    }

    @SuppressWarnings("SameParameterValue")
    private JPanel createOutputPanel(int x, int y, int width, int height) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Data"));
        panel.setPreferredSize(new Dimension(width, height));

        resultArea = new JTextArea();
        resultArea.setText("");
        resultArea.setLayout(new BorderLayout());

        panel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        return panel;
    }

    private void compare(ActionEvent actionEvent) {
        if(fPND!=null && sPND!=null) {
            LabelNetComparator lnc = new LabelNetComparator(fPND, sPND);
            lnc.calcSimilarities();

            resultArea.append("---Differences from first Net---\n");
            for (String s : lnc.getDifferencesFromFirstNet()) {
                resultArea.append(s+ "\n");
            }

            resultArea.append("\n\n---Differences from second Net---\n");
            for (String s : lnc.getDifferencesFromSecondNet()) {
                resultArea.append(s+ "\n");
            }

            resultArea.append("\n\n---Common parts---\n");
            for (String s : lnc.getSimilarities()) {
                resultArea.append(s+ "\n");
            }
        }
    }

    public void getNet(ActionEvent actionEvent,boolean type) {
        JFileChooser firstNetChooser = new JFileChooser();
        FileNameExtensionFilter fnef = new FileNameExtensionFilter("Holmes format","project");
        firstNetChooser.setFileFilter(fnef);

        int returnVal = firstNetChooser.showSaveDialog(HolmesLabelComparison.this);

        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                ProjectReader pRdr = new ProjectReader(true);
                boolean status = pRdr.readProjectForLabelComparison(firstNetChooser.getSelectedFile().getPath());

                XStream xstream = new XStream(new StaxDriver());
                xstream.alias("petriNet", PetriNetData.class);
                File source = new File(firstNetChooser.getSelectedFile().getPath());
                if (type)
                    fPND = new PetriNetData(pRdr.getNodes(),pRdr.getArcs(), "First Net");
                else
                    sPND = new PetriNetData(pRdr.getNodes(),pRdr.getArcs(), "Second Net");
            } catch (Exception ex) {
                GUIManager.getDefaultGUIManager().log("Error (236840455) | Exception:  "+ex.getMessage(), "error", true);
            }
        }
    }
}
