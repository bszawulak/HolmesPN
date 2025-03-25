package holmes.windows;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.utilities.Tools;
import org.jfree.ui.tabbedui.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

/**
 * Klasa pozwalająca otworzyć okno właściwości podsieci.
 */
public class HolmesSubnetsInfo {
    private final GraphPanel graphPanel;
    private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
    private static final LanguageManager lang = GUIManager.getLanguageManager();

    /**
     * Konstruktor klasy HolmesSubnetsInfo.
     * @param graphPanel GraphPanel - aktualnie wybrany arkusz
     */
    public HolmesSubnetsInfo(GraphPanel graphPanel) {
        this.graphPanel = graphPanel;
    }

    /**
     * Metoda inicjująca wszystkie komponenty dialogu.
     * @return JPanel - główny panel okna
     */
    private JPanel initMainPanel() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setBounds(0, 0, 600, 480);

        JPanel infoPanel = new JPanel(null);
        mainPanel.add(infoPanel);
        infoPanel.setBounds(5, 5, mainPanel.getWidth() - 25, 130);
        infoPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSIwin_entry001")));


        JLabel labelID = new JLabel(lang.getText("HSIwin_entry002")); //ID
        infoPanel.add(labelID);
        labelID.setBounds(10, 20, 20, 20);

        JTextField idTextBox = new JTextField(String.valueOf(graphPanel.getSheetId()));
        infoPanel.add(idTextBox);
        idTextBox.setBounds(30, 20, 30, 20);
        idTextBox.setEditable(false);


        JLabel labelName = new JLabel(lang.getText("HSIwin_entry003"));
        infoPanel.add(labelName);
        labelName.setBounds(10, 40, 40, 20);

        JTextField nameField = new JTextField();
        infoPanel.add(nameField);
        nameField.setBounds(100, 40, 465, 20);
        MetaNode metaNode = overlord.subnetsHQ.getMetanode(graphPanel.getSheetId()).orElseThrow();
        nameField.setText(metaNode.getName());
        nameField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            String newValue = nameField.getText();
            metaNode.setName(newValue);
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(mainPanel);
            frame.setTitle(newValue);
        });


        JLabel commmentLabel = new JLabel(lang.getText("HSIwin_entry004"));
        infoPanel.add(commmentLabel);
        commmentLabel.setBounds(10, 60, 80, 20);

        JTextArea commentField = new JTextArea(metaNode.getComment());
        infoPanel.add(commentField);
        commentField.setLineWrap(true);
        commentField.getDocument().addDocumentListener((SimpleDocumentListener) e -> {
            String newValue = commentField.getText();
            metaNode.setComment(newValue);
        });

        JPanel creationPanel = new JPanel();
        infoPanel.add(creationPanel);
        creationPanel.setLayout(new BorderLayout());
        creationPanel.add(new JScrollPane(commentField), BorderLayout.CENTER);
        creationPanel.setBounds(100, 60, 465, 60);


        JPanel statisticsPanel = new JPanel(null);
        mainPanel.add(statisticsPanel);
        statisticsPanel.setBounds(5, 135, 230, 300);
        statisticsPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSIwin_entry005"))); //Subnet statistics

        JLabel nodesCountLabel = new JLabel(lang.getText("HSIwin_entry006")); //Number of element locations
        statisticsPanel.add(nodesCountLabel);
        nodesCountLabel.setBounds(10, 25, 200, 20);

        List<ElementLocation> elements = overlord.subnetsHQ.getSubnetElementLocations(graphPanel.getSheetId());
        JTextField nodesCountField = new JTextField(String.valueOf(elements.size()));
        statisticsPanel.add(nodesCountField);
        nodesCountField.setBounds(190, 25, 30, 20);
        nodesCountField.setEditable(false);

        List<Node> uniqueNodes = elements.stream()
                .map(ElementLocation::getParentNode)
                .sorted(Comparator.comparingInt(PetriNetElement::getID))
                .distinct().toList();


        JLabel placeCountLabel = new JLabel(lang.getText("HSIwin_entry007")); //Number of places
        statisticsPanel.add(placeCountLabel);
        placeCountLabel.setBounds(10, 50, 200, 20);

        long placeCount = uniqueNodes.stream().filter(Place.class::isInstance).count();
        JTextField placeCountField = new JTextField(String.valueOf(placeCount));
        statisticsPanel.add(placeCountField);
        placeCountField.setBounds(190, 50, 30, 20);
        placeCountField.setEditable(false);


        JLabel transitionCountLabel = new JLabel(lang.getText("HSIwin_entry008")); //Number of transitions
        statisticsPanel.add(transitionCountLabel);
        transitionCountLabel.setBounds(10, 75, 200, 20);

        long transitionCount = uniqueNodes.stream().filter(Transition.class::isInstance).count();
        JTextField transitionCountField = new JTextField(String.valueOf(transitionCount));
        statisticsPanel.add(transitionCountField);
        transitionCountField.setBounds(190, 75, 30, 20);
        transitionCountField.setEditable(false);

        JLabel tokensCountLabel = new JLabel(lang.getText("HSIwin_entry009")); //Number of tokens
        statisticsPanel.add(tokensCountLabel);
        tokensCountLabel.setBounds(10, 100, 200, 20);

        int tokenCount = uniqueNodes.stream()
                .filter(Place.class::isInstance)
                .map(node -> ((Place) node).getTokensNumber())
                .reduce(Integer::sum).orElse(0);
        JTextField tokenCountField = new JTextField(String.valueOf(tokenCount));
        statisticsPanel.add(tokenCountField);
        tokenCountField.setBounds(190, 100, 30, 20);
        tokenCountField.setEditable(false);

        JPanel connectionsPanel = new JPanel(null);
        mainPanel.add(connectionsPanel);
        connectionsPanel.setBounds(240, 135, 340, 300);
        connectionsPanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSIwin_entry010"))); //Connected subnets

        JLabel parentIdLabel = new JLabel(lang.getText("HSIwin_entry011")); //Parent ID
        connectionsPanel.add(parentIdLabel);
        parentIdLabel.setBounds(10, 25, 100, 20);

        String parentId = String.valueOf(metaNode.getMySheetID());
        JTextField parentIdField = new JTextField(parentId);
        connectionsPanel.add(parentIdField);
        parentIdField.setBounds(110, 25, 220, 20);
        parentIdField.setEditable(false);


        JLabel parentNameLabel = new JLabel(lang.getText("HSIwin_entry012")); //Parent name
        connectionsPanel.add(parentNameLabel);
        parentNameLabel.setBounds(10, 50, 100, 20);

        String parentName = overlord.subnetsHQ.getMetanode(metaNode.getMySheetID()).map(PetriNetElement::getName).orElse("Subnet0");
        JTextField parentNameField = new JTextField(parentName);
        connectionsPanel.add(parentNameField);
        parentNameField.setBounds(110, 50, 220, 20);
        parentNameField.setEditable(false);

        JLabel childrenLabel = new JLabel(lang.getText("HSIwin_entry013")); //Subnets
        connectionsPanel.add(childrenLabel);
        childrenLabel.setBounds(10, 90, 100, 20);

        JLabel tableHeader = new JLabel(" "+lang.getText("HSIwin_entry014")); //ID:     Name:
        connectionsPanel.add(tableHeader);
        tableHeader.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        tableHeader.setBounds(10, 115, 220, 25);

        JPanel subnetsList = new JPanel(new VerticalLayout());

        JPanel subnetsWrapper = new JPanel();
        connectionsPanel.add(subnetsWrapper);
        subnetsWrapper.setLayout(new BorderLayout());
        subnetsWrapper.setBounds(10, 138, 220, 155);
        subnetsWrapper.add(new JScrollPane(subnetsList), BorderLayout.CENTER);


        List<MetaNode> subnets = elements.stream()
                .filter(location -> location.getParentNode() instanceof MetaNode)
                .map(location -> (MetaNode) location.getParentNode())
                .toList();

        for (MetaNode subnet : subnets) {
            JLabel test = new JLabel(String.format(" %d     %s", subnet.getRepresentedSheetID(), subnet.getName()));
            subnetsList.add(test);
            test.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            test.setPreferredSize(new Dimension(200, 40));
        }

        return mainPanel;
    }

    /**
     * Metoda łączy panel właściwości z oknem aplikacji.
     * @param frame JFrame - okno aplikacji
     */
    public void bind(JFrame frame) {
        frame.add(initMainPanel());
    }

    /**
     * Metoda otwiera panel właściwości aktualnie wybranej podsieci.
     */
    public static void open() {
        int subnetID = overlord.getWorkspace().getSelectedSheet().getId();
        open(subnetID);
    }

    /**
     * Metoda otwiera panel właściwości podanej podsieci.
     * @param subnetID int - id podsieci
     */
    public static void open(int subnetID) {
        GraphPanel graphPanel = overlord.subnetsHQ.getGraphPanel(subnetID);
        if (graphPanel.getSheetId() == 0) {
            JOptionPane.showMessageDialog(null, lang.getText("HSIwin_entry015"), lang.getText("HSIwin_entry015t"),
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFrame mainFrame = overlord.getFrame();
        JFrame targetFrame = new JFrame();
        try {
            targetFrame.setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
        } catch (Exception ex) {
            overlord.log(lang.getText("LOGentry00499exception")+" " + ex.getMessage(), "error", true);
        }

        mainFrame.setEnabled(false);
        targetFrame.setResizable(false);
        targetFrame.setLocation(20, 20);
        targetFrame.setSize(new Dimension(600, 480));

        targetFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                mainFrame.setEnabled(true);
            }
        });

        MetaNode metaNode = overlord.subnetsHQ.getMetanode(graphPanel.getSheetId()).orElseThrow();

        targetFrame.setTitle(metaNode.getName());
        HolmesSubnetsInfo holmesSubnetsInfo = new HolmesSubnetsInfo(graphPanel);
        holmesSubnetsInfo.bind(targetFrame);
        targetFrame.setVisible(true);
    }
}
