package abyss.darkgui.box;

//import java.awt.BorderLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.util.ArrayList;
//import java.awt.Dialog;









import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatter;

import abyss.analyzer.InvariantsSimulator;
import abyss.darkgui.GUIManager;
import abyss.darkgui.SpringUtilities;
import abyss.graphpanel.GraphPanel;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.InvariantTransition;
import abyss.math.Node;
import abyss.math.PetriNet;
import abyss.math.PetriNetElement;
import abyss.math.Place;
import abyss.math.TimeTransition;
import abyss.math.Transition;
import abyss.math.simulator.NetSimulator;
import abyss.math.simulator.NetSimulator.SimulatorMode;
import abyss.workspace.WorkspaceSheet;

/**
 * Klasa zawieraj�ca szczeg�y interfejsu podokien dokowalnych programu.
 * @author students
 * @author MR
 * W�a�ciwie, to wyleje tu swoje �ale na Jave, opensourcowe podej�cia w tym j�zyku i takie
 * tam. Nie ma to NIC wsp�lnego ze studentami, kt�rzy si� serio postarali i zrobili okna ok.
 * Przerobi�em metody na pozycjonowanie absolutne, wywali�em w choler� wszystkie layouty.
 * Bo tak. Bo ludzie padaj�cy przed ich ide� na kolana i bij�cy pok�ony "Oh, layout, jak
 * cudownie, wszystko si� nam teraz automatycznie rozmie�ci" nie zauwa�aj�, albo nie chc�
 * zauwa�a�, �e to 'automatycznie' jest tak do dupy, tak bardzo z... �e ju� bardziej si�
 * chyba nie da. PO CO MI LATAJ�CE WE WSZYSTKIE STRONY ELEMENTY OKNA, SKORO CHCIA�BYM
 * MIE� JE NA STA�E W JEDNYM MIEJSCU?! Ok, ale o co tu chodzi? No wi�c albo si� u�ywa w
 * Javie layout�w, 2 polecenia na krzy� i wszystko si� rozmieszcza gdzie chce i jak chce,
 * albo robi r�cznie i okazuje si�, �e Java w najmniejszym stopniu nie wspiera takiego podej�cia.
 * Nagle miliard rzeczy nale�y r�cznie ustawia�, niepotrzebych na zdrowy rozs�dek (PO CO MI 
 * BORDERSIZE JAK MOG� USTAWI� START LOCATION I SIZE? PO NIC. ALE BEZ NIEGO JPANEL SIE NIE
 * WY�WIETLI. BO NIE!). Nagle okazuje si�, �e JPanel r�cznie nale�y zmusi� do przerysowania si�
 * (repaint) - bo tak. Z layoutami jako� pami�ta, �eby si� narysowa�. Bez nich ju� nie.
 * 
 * Konkluzja. Kto� m�g�by powiedzie�, �e przecie� skoro chce si� r�cznie wszystko rozmie�ci�,
 * to nie nale�y narzeka�, �e jest du�o roboty. ZOBACZCIE SOBIE DURNIE MICROSOFT VISUAL STUDIO.
 * S� panele, layouty i inne. Ale nie zmusz� si� nikogo m�otem do ich korzystania jak w Javie.
 * I okazuje si�, �e nagle jest mniej tam roboty z rozmieszczaniem, ni� nawet z layoutami w Javie.
 * Ten j�zyk powinien pozosta� na etapie konsoli. Jego �a�osne pr�by udawania, �e s�u�y do
 * tworzenia tak�e aplikacji w oknach nabieraj� chyba tylko jego za�lepionych fanboj�w.
 * MR
 */
public class PropertiesTable extends JPanel {
	private static final long serialVersionUID = 4510802239873443705L;
	private ArrayList<JComponent> headers;
	private ArrayList<JComponent> values;
	private ArrayList<JComponent> components;
	private int mode;
	// Containers
	private JPanel panel, invariantPanel, mainPanel;
	// sheet reference
	WorkspaceSheet currentSheet;
	// petri net reference
	PetriNetElement element;
	ElementLocation elementLocation;
	NetSimulator simulator;
	InvariantsSimulator invSimulator;
	// other
	ArrayList<ArrayList<InvariantTransition>> externalInvariants;
	ArrayList<ArrayList<Transition>> mctGroups;
	public ButtonGroup group = new ButtonGroup();
	public JSpinner spiner = new JSpinner();

	Dimension headerSize;
	
	JFrame timeFrame = new JFrame("Zegar");

	// modes
	private static final int PLACE = 0;
	private static final int TRANSITION = 1;
	private static final int ARC = 2;
	private static final int SHEET = 3;
	private static final int SIMULATOR = 4;
	private static final int EXTERNAL_ANALYSIS = 5;
	@SuppressWarnings("unused")
	private static final int MCT = 6;
	private static final int TIMETRANSITION = 7;
	private static final int INVARIANTSSIMULATOR = 8;

	// private static final JComponent new JButton = null;

	/**
	 * Konstruktor odpowiedzialny za tworzenie element�w podokna dla symulatora sieci.
	 * @param sim NetSimulator - obiekt symulatora sieci
	 */
	public PropertiesTable(NetSimulator sim) {
		initiateContainers();
		
		String[] simModeName = {"Klasyczny", "Czasowy"};
		// set mode
		mode = SIMULATOR;
		simulator = sim;
				
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final JComboBox simMode = new JComboBox(simModeName);
		simMode.setSelectedIndex(0);
		simMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.setSimulatorNetType(simMode.getSelectedIndex());
			}
		});
		headers.add(simMode);
		// simulator controls
		JButton oneActionBack = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_bck_blue.png"));
		oneActionBack.setToolTipText("One action back");
		oneActionBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.ACTION_BACK);
			}
		});
		headers.add(oneActionBack);
		values.add(new JLabel());
		JButton oneTransitionForward = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_fwd_blue.png"));
		oneTransitionForward.setToolTipText("One transition forward");
		oneTransitionForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION);
			}
		});
		values.add(oneTransitionForward);
		JButton loopBack = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_step_bck_blue.png"));
		loopBack.setToolTipText("Loop back to oldest saved action");
		loopBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.LOOP_BACK);
			}
		});
		headers.add(loopBack);
		JButton oneStepForward = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_step_fwd_blue.png"));
		oneStepForward.setToolTipText("One step forward");
		oneStepForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.STEP);
			}
		});
		values.add(oneStepForward);
		JButton loopSimulation = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_repeat_blue.png"));
		loopSimulation.setToolTipText("Loop simulation");
		loopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.LOOP);
			}
		});
		headers.add(loopSimulation);
		JButton singleTransitionLoopSimulation = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_repeat.png"));
		singleTransitionLoopSimulation
				.setToolTipText("Loop single transition simulation");
		singleTransitionLoopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
			}
		});
		values.add(singleTransitionLoopSimulation);
		JButton pauseSimulation = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_pause_blue.png"));
		pauseSimulation.setToolTipText("Pause simulation");
		pauseSimulation.setEnabled(false);
		pauseSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.pause();
			}
		});
		headers.add(pauseSimulation);
		JButton stopSimulation = new JButton(new ImageIcon(
				"resources/icons/simulation_icons/control_stop_blue.png"));
		stopSimulation.setToolTipText("Schedule a stop for the simulation");
		stopSimulation.setEnabled(false);
		stopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.stop();
			}
		});
		values.add(stopSimulation);
		// JButton saveState = new JButton(new ImageIcon(
		// "resources/icons/simulation_icons/control_cursor_blue.png"));
		// saveState.setEnabled(false);
		// saveState.setToolTipText("Save current state");
		// headers.add(saveState);
		// JButton revertSimulation = new JButton(new ImageIcon(
		// "resources/icons/simulation_icons/control_equalizer_blue.png"));
		// revertSimulation.setToolTipText("Revert to saved state");
		// revertSimulation.setEnabled(false);
		// values.add(revertSimulation);
		// ===============================================
		// tryb maximum
		headers.add(new JLabel("Maximum mode: ", JLabel.TRAILING));
		JCheckBox maximumMode = new JCheckBox("");
		maximumMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent
						.getSource();
				if (abstractButton.getModel().isSelected()) {
					simulator.setMaximumMode(true);
				} else {
					simulator.setMaximumMode(false);
				}
			}
		});
		values.add(maximumMode);
		// getting the data
		// Arcs total
		headers.add(new JLabel("General", JLabel.TRAILING));
		values.add(new JLabel("Information"));
		// Nodes total
		headers.add(new JLabel("Nodes:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(simulator.getNodesAmount())));
		// Places total
		headers.add(new JLabel("Places:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(simulator.getPlacesAmount())));
		// Transitions total
		headers.add(new JLabel("Transitions:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(simulator.getTransitionsAmount())));
		// Arcs total
		headers.add(new JLabel("Arcs:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(simulator.getArcsAmount())));
		// Tokens total
		headers.add(new JLabel("Tokens:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(simulator.getTokensAmount())));
		// put all contents on the pane
		putContents(panel);
	}

	/**
	 * Konstruktor podokna wy�wietlaj�cego w�a�ciwo�ci klikni�tego miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param location ElementLocation - lokalizacja miejsca
	 */
	public PropertiesTable(Place place, ElementLocation location) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		elementLocation = location;
		initiateContainers();
		mode = PLACE;
		element = place;

		//ID:
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		//components.add(idLabel);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(place.getID()));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, 50, 20);
		components.add(idLabel2);

		//NAME:
		JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
		nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(nameLabel);

		JFormattedTextField nameField = new JFormattedTextField();
		nameField.setLocation(columnB_posX, columnB_Y += 20);
		nameField.setSize(colBCompLength, 20);
		nameField.setMaximumSize(new Dimension(colBCompLength,20));
		nameField.setMinimumSize(new Dimension(colBCompLength,20));
		nameField.setText(place.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeName(newName);
			}
		});
		components.add(nameField);
		
		//KOMENTARZE WIERZCHO�KA:
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);	
		JTextArea commentField = new JTextArea(place.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent e) {
	            	JTextArea field = (JTextArea) e.getSource();
	            	String newComment = "";
	            	if(field != null)
	            		newComment = field.getText();
					changeComment(newComment);
	            }
	        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
        
        
		// token
        JLabel tokenLabel = new JLabel("Tokens:", JLabel.LEFT);
        tokenLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(tokenLabel);
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(place.getTokensNumber(), 0, 
				Integer.MAX_VALUE, 1);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.setLocation(columnB_posX, columnB_Y += 20);
		tokenSpinner.setSize(colBCompLength, 20);
		tokenSpinner.setMaximumSize(new Dimension(colBCompLength,20));
		tokenSpinner.setMinimumSize(new Dimension(colBCompLength,20));
		tokenSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int tokenz = (int) spinner.getValue();
				setTokens(tokenz);
			}
		});
		components.add(tokenSpinner);

		//SHEET ID
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		int xPos = location.getPosition().x;
		int width =  graphPanel.getSize().width;
		int zoom = graphPanel.getZoom();
		int yPos = location.getPosition().y;
		int height =  graphPanel.getSize().height;
		width = (int) (((double)100/(double)zoom) * width);
		height = (int) (((double)100/(double)zoom) * height);
		
		JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
		sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(sheetLabel);
		JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
		sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(sheetIdLabel);		
		JLabel zoomLabel = new JLabel("Zoom: "+zoom);
		zoomLabel.setBounds(columnB_posX+50, columnB_Y, colBCompLength, 20);
		components.add(zoomLabel);	
		//LOKALIZACJA:
		JLabel locLabel = new JLabel("Location:", JLabel.LEFT);
		locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(locLabel);
		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(
				xPos, 0, width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(
				yPos, 0, height, 1);

		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		if(zoom != 100) {
			locationXSpinner.setEnabled(false);
			locationYSpinner.setEnabled(false);
		}
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		
		locationSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, colBCompLength, 20);
		components.add(locationSpinnerPanel);
		/*
		headers.add(new JLabel("Location:", JLabel.TRAILING));
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(
				location.getPosition().x, 0, graphPanel.getSize().width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(
				location.getPosition().y, 0, graphPanel.getSize().height, 1);
		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,
				BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		values.add(locationSpinnerPanel);
		*/
		
		
		// PORTAL
		JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
		portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(portalLabel);
		JCheckBox portalBox = new JCheckBox("", place.isPortal());
		portalBox.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		portalBox.setEnabled(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					unPortal();
				else
					makePortal();
			}
		});
		components.add(portalBox);
		
		panel.setLayout(null);
		for (JComponent component : components) {
			panel.add(component);
		}
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	/**
	 * Metoda odpowiedzialna za wy�wietlenie w�a�ciwo�ci klikni�tej tranzycji.
	 * @param transition Transition - obiekt tranzycji sieci
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public PropertiesTable(Transition transition, ElementLocation location) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;

		mode = TRANSITION;
		elementLocation = location;
		initiateContainers();
		element = transition;
		
		// ID:
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(transition.getID()));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
		components.add(idLabel2);

		// TRANSITION NAME:
		JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
		nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(nameLabel);
		
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setLocation(columnB_posX, columnB_Y += 20);
		nameField.setSize(colBCompLength, 20);
		nameField.setMaximumSize(new Dimension(colBCompLength,20));
		nameField.setMinimumSize(new Dimension(colBCompLength,20));
	    
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeName(newName);
			}
		});
		components.add(nameField);
		
		//KOMENTARZE WIERZCHO�KA:
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);	
		/*
		JFormattedTextField commentField = new JFormattedTextField();
		commentField.setLocation(columnB_posX, columnB_Y += 20);
		commentField.setSize(colBCompLength, 20);
		commentField.setMaximumSize(new Dimension(colBCompLength,20));
		commentField.setMinimumSize(new Dimension(colBCompLength,20));
		commentField.setText(transition.getComment());
		commentField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newComment = (String) field.getText();
				changeComment(newComment);
			}
		});
		components.add(commentField);
		*/
		JTextArea commentField = new JTextArea(transition.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent e) {
	            	JTextArea field = (JTextArea) e.getSource();
	            	String newComment = "";
	            	if(field != null)
	            		newComment = field.getText();
					changeComment(newComment);
	            }
	        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
		
		//SHEET ID
        int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		int xPos = location.getPosition().x;
		int width =  graphPanel.getSize().width;
		int zoom = graphPanel.getZoom();
		int yPos = location.getPosition().y;
		int height =  graphPanel.getSize().height;
		width = (int) (((double)100/(double)zoom) * width);
		height = (int) (((double)100/(double)zoom) * height);
		
		JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
		sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(sheetLabel);
		JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
		sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(sheetIdLabel);
		JLabel zoomLabel = new JLabel("Zoom: "+zoom);
		zoomLabel.setBounds(columnB_posX+50, columnB_Y, colBCompLength, 20);
		components.add(zoomLabel);	
		//LOKALIZACJA:
		JLabel locLabel = new JLabel("Location:", JLabel.LEFT);
		locLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(locLabel);
		
		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
		
		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		if(zoom != 100) {
			locationXSpinner.setEnabled(false);
			locationYSpinner.setEnabled(false);
		}
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		
		locationSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, colBCompLength, 20);
		components.add(locationSpinnerPanel);
		
		// PORTAL
		JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
		portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(portalLabel);
		JCheckBox portalBox = new JCheckBox("", transition.isPortal());
		portalBox.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		portalBox.setEnabled(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					unPortal();
				else
					makePortal();
			}
		});
		components.add(portalBox);
		// put all contents on the pane
		 
		//putContents(panel);
		panel.setLayout(null);
		for (JComponent component : components) {
			panel.add(component);
		}

		//contentPanel.setLayout(new SpringLayout());
		for (int i = 0; i < components.size(); i++) {
			//panel.add(components.get(i));
		}
		//SpringUtilities.makeCompactGrid(contentPanel, headers.size(), 2, 5, 2, 2, 5);
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	/**
	 * Metoda odpowiedzialna za wy�wietlenie w�a�ciwo�ci klikni�tej tranzycji czasowej.
	 * @param transition TimeTransition - obiekt tranzycji czasowej
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public PropertiesTable(final TimeTransition transition, ElementLocation location) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		
		mode = TIMETRANSITION;
		elementLocation = location;
		initiateContainers(); //!!!
		element = transition;
		
		// ID
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(transition.getID()));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
		components.add(idLabel2);

		// T-TRANSITION NAME
		JLabel nameLabel = new JLabel("Name:", JLabel.LEFT);
		nameLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(nameLabel);
		DefaultFormatter format = new DefaultFormatter();
	    format.setOverwriteMode(false);
		JFormattedTextField nameField = new JFormattedTextField(format);
		nameField.setLocation(columnB_posX, columnB_Y += 20);
		nameField.setSize(colBCompLength, 20);
		nameField.setMaximumSize(new Dimension(colBCompLength,20));
		nameField.setMinimumSize(new Dimension(colBCompLength,20));
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				changeName(newName);
			}
		});
		components.add(nameField);
		
		// T-TRANSITION COMMENT:
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);
		/*
		JFormattedTextField commentField = new JFormattedTextField();
		commentField.setLocation(columnB_posX, columnB_Y += 20);
		commentField.setSize(200, 20);
		commentField.setMaximumSize(new Dimension(200,20));
		commentField.setMinimumSize(new Dimension(200,20));
		commentField.setValue(transition.getComment());
		commentField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newComment = (String) field.getText();
				changeComment(newComment);
			}
		});
		components.add(commentField);
		*/
		JTextArea commentField = new JTextArea(transition.getComment());
		commentField.setLineWrap(true);
		commentField.addFocusListener(new FocusAdapter() {
	            public void focusLost(FocusEvent e) {
	            	JTextArea field = (JTextArea) e.getSource();
	            	String newComment = "";
	            	if(field != null)
	            		newComment = field.getText();
					changeComment(newComment);
	            }
	        });
		
        JPanel CreationPanel = new JPanel();
        CreationPanel.setLayout(new BorderLayout());
        CreationPanel.add(new JScrollPane(commentField),BorderLayout.CENTER);
        CreationPanel.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 40);
        columnB_Y += 20;
        components.add(CreationPanel);
        
		// EFT / LFT TIMES:
		JLabel minMaxLabel = new JLabel("EFT / LFT:", JLabel.LEFT);
		minMaxLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(minMaxLabel);
		JFormattedTextField minTimeField = new JFormattedTextField();
		minTimeField.setValue(transition.getMinFireTime());
		minTimeField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}						
				double min = (double) field.getValue();
				setMinFireTime(min);
			}
		});

		JFormattedTextField maxTimeField = new JFormattedTextField();
		maxTimeField.setValue(transition.getMaxFireTime());
		maxTimeField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e
						.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}						
				double max = (double) field.getValue();
				setMaxFireTime(max);
			}
		});

		JPanel minTimeSpinnerPanel = new JPanel();
		minTimeSpinnerPanel.setLayout(new BoxLayout(minTimeSpinnerPanel, BoxLayout.X_AXIS));
		minTimeSpinnerPanel.add(minTimeField);
		minTimeSpinnerPanel.add(new JLabel(" / "));
		minTimeSpinnerPanel.add(maxTimeField);
		minTimeSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, 200, 20);
		components.add(minTimeSpinnerPanel);

		// T-TRANSITION SHEET ID:
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(location.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		int xPos = location.getPosition().x;
		int width =  graphPanel.getSize().width;
		int zoom = graphPanel.getZoom();
		int yPos = location.getPosition().y;
		int height =  graphPanel.getSize().height;
		width = (int) (((double)100/(double)zoom) * width);
		height = (int) (((double)100/(double)zoom) * height);

		JLabel sheetLabel = new JLabel("Sheet:", JLabel.LEFT);
		sheetLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(sheetLabel);
		JLabel sheetIdLabel = new JLabel(Integer.toString(location.getSheetID()));
		sheetIdLabel.setBounds(columnB_posX, columnB_Y += 20, 100, 20);
		components.add(sheetIdLabel);
		JLabel zoomLabel = new JLabel("Zoom: "+zoom);
		zoomLabel.setBounds(columnB_posX+50, columnB_Y, colBCompLength, 20);
		components.add(zoomLabel);	
		// T-TRANSITION LOCATION:
		//headers.add(new JLabel("Location:", JLabel.TRAILING));
		JLabel comLabel2 = new JLabel("Location:", JLabel.LEFT);
		comLabel2.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(comLabel2);

		SpinnerModel locationXSpinnerModel = new SpinnerNumberModel(xPos, 0, width, 1);
		SpinnerModel locationYSpinnerModel = new SpinnerNumberModel(yPos, 0, height, 1);
		JSpinner locationXSpinner = new JSpinner(locationXSpinnerModel);
		locationXSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int x = (int) spinner.getValue();
				setX(x);
			}
		});
		JSpinner locationYSpinner = new JSpinner(locationYSpinnerModel);
		locationYSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int y = (int) spinner.getValue();
				setY(y);
			}
		});
		if(zoom != 100) {
			locationXSpinner.setEnabled(false);
			locationYSpinner.setEnabled(false);
		}
		JPanel locationSpinnerPanel = new JPanel();
		locationSpinnerPanel.setLayout(new BoxLayout(locationSpinnerPanel,BoxLayout.X_AXIS));
		locationSpinnerPanel.add(locationXSpinner);
		locationSpinnerPanel.add(new JLabel(" , "));
		locationSpinnerPanel.add(locationYSpinner);
		locationSpinnerPanel.setBounds(columnA_posX+90, columnB_Y += 20, 200, 20);
		components.add(locationSpinnerPanel);
		
		// PORTAL STATUS
		JLabel portalLabel = new JLabel("Portal:", JLabel.LEFT);
		portalLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(portalLabel);

		JCheckBox portalBox = new JCheckBox("", transition.isPortal());
		portalBox.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		portalBox.setEnabled(false);
		portalBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					unPortal();
				else
					makePortal();
			}
		});
		components.add(portalBox);
		// put all contents on the pane
		//putContents(panel);
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za utworzenie element�w podokna w�a�ciwo�ci klikni�tego
	 * �uku sieci.
	 * @param arc Arc - obiekt �uku
	 */
	public PropertiesTable(Arc arc) {
		initiateContainers();
		// set mode
		mode = ARC;
		element = arc;
		elementLocation = arc.getStartLocation();
		// getting the data
		// ID
		headers.add(new JLabel("ID:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(arc.getID())));
		// comment
		JFormattedTextField commentField = new JFormattedTextField();
		commentField.setValue(arc.getComment());
		commentField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e.getSource();
						try {
							field.commitEdit();
						} catch (ParseException ex) {
						}
						String newComment = (String) field.getValue();
						changeComment(newComment);
					}
				});
		headers.add(new JLabel("Comment:", JLabel.TRAILING));
		values.add(commentField);
		// weight
		headers.add(new JLabel("Weight:", JLabel.TRAILING));
		SpinnerModel weightSpinnerModel = new SpinnerNumberModel(arc.getWeight(), 0, Integer.MAX_VALUE, 1);
		JSpinner weightSpinner = new JSpinner(weightSpinnerModel);
		weightSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int tokenz = (int) spinner.getValue();
				setWeight(tokenz);
			}
		});
		values.add(weightSpinner);
		// startNode
		headers.add(new JLabel("StartNode ID:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(arc.getStartNode().getID())));
		headers.add(new JLabel("StartNode Name:", JLabel.TRAILING));
		values.add(new JLabel(arc.getStartNode().getName()));
		headers.add(new JLabel("StartNode Sheet:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(arc.getStartLocation()
				.getSheetID())));
		headers.add(new JLabel("Location:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(arc.getStartLocation()
				.getPosition().x)+ ", "
				+ Integer.toString(arc.getStartLocation().getPosition().y)));
		// endNode
		headers.add(new JLabel("EndNode ID:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(arc.getEndNode().getID())));
		headers.add(new JLabel("EndNode Name:", JLabel.TRAILING));
		values.add(new JLabel(arc.getEndNode().getName()));
		headers.add(new JLabel("EndNode Sheet:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(arc.getEndLocation()
				.getSheetID())));
		headers.add(new JLabel("EndNode Location:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(arc.getEndLocation()
				.getPosition().x) + ", "
				+ Integer.toString(arc.getEndLocation().getPosition().y)));
		// put all contents on the pane
		putContents(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za utworzenie podokna w�a�ciwo�ci klikni�tego
	 * arkusza sieci. 
	 * @param sheet WorkspaceSheet - obiekt arkusza
	 */
	public PropertiesTable(WorkspaceSheet sheet) {
		initiateContainers();
		// set mode
		mode = SHEET;
		currentSheet = sheet;
		// getting the data
		// ID
		headers.add(new JLabel("Sheet ID:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(sheet.getId())));
		// size
		headers.add(new JLabel("Width:", JLabel.TRAILING));
		SpinnerModel widthSpinnerModel = new SpinnerNumberModel(sheet
				.getGraphPanel().getSize().width, 0, Integer.MAX_VALUE, 1);
		JSpinner widthSpinner = new JSpinner(widthSpinnerModel);
		widthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int width = (int) spinner.getValue();
				setSheetWidth(width);
			}
		});
		values.add(widthSpinner);
		headers.add(new JLabel("Height:", JLabel.TRAILING));
		SpinnerModel heightSpinnerModel = new SpinnerNumberModel(sheet
				.getGraphPanel().getSize().height, 0, Integer.MAX_VALUE, 1);
		JSpinner heightSpinner = new JSpinner(heightSpinnerModel);
		heightSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int height = (int) spinner.getValue();
				setSheetHeight(height);
			}
		});
		values.add(heightSpinner);
		// is auto scroll when dragging automatic
		headers.add(new JLabel("Autoscroll:", JLabel.TRAILING));
		JCheckBox autoscrollBox = new JCheckBox("", sheet.getGraphPanel().isAutoDragScroll());
		autoscrollBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox box = (JCheckBox) e.getSource();
				if (box.isSelected())
					setAutoscroll(true);
				else
					setAutoscroll(false);
			}
		});
		values.add(autoscrollBox);
		// put all contents on the pane
		putContents(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za wype�nienie podokna umo�liwiaj�cego wyb�r poszczeg�lnych
	 * inwariant�w sieci.
	 * @param invariants ArrayList[ArrayList[InvariantTransition]] - macierz inwariant�w
	 */
	public PropertiesTable(ArrayList<ArrayList<InvariantTransition>> invariants) {
		if (invariants.size() > 0) {
			initiateContainers();
			// set mode
			mode = EXTERNAL_ANALYSIS;
			externalInvariants = invariants;
			// getting the data
			JLabel chooseInvariantLabel = new JLabel("Choose invariant: ");
			chooseInvariantLabel.setMaximumSize(chooseInvariantLabel.getMinimumSize());
			headers.add(chooseInvariantLabel);
			String[] invariantHeaders = new String[invariants.size() + 1];
			invariantHeaders[0] = "---";
			for (int i = 0; i < invariants.size(); i++)
				invariantHeaders[i + 1] = "Invariant no. " + Integer.toString(i);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			JComboBox chooseInvariantBox = new JComboBox(invariantHeaders);
			chooseInvariantBox.setMaximumSize(chooseInvariantBox.getMinimumSize());
			chooseInvariantBox.setVisible(true);
			chooseInvariantBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					@SuppressWarnings("rawtypes")
					JComboBox comboBox = (JComboBox) actionEvent.getSource();
					if (comboBox.getSelectedIndex() == 0) {
						showInvariant(0, false);
					} else {
						showInvariant(comboBox.getSelectedIndex() - 1, true);
					}
				}
			});
			values.add(chooseInvariantBox);
			// put all contents on the pane
			putContents(panel);
		}
	}

	/**
	 * Konstruktor podokna w�a�ciwo�ci element�w sieci. Wype�niany w zale�no�ci od
	 * tego, co przysz�o jako argument - tj. kt�re w�a�ciwo�ci
	 * @param prop ArrayList[ArrayList[Object]] - macierz w�a�ciwo�ci
	 * @param ref boolean - warto�� logiczna nie maj�ca na nic wp�ywu :)
	 */
	public PropertiesTable(ArrayList<ArrayList<Object>> prop, boolean ref) {
		initiateContainers();
		JPanel rowPanel = new JPanel();
		rowPanel.setLayout(new BoxLayout(rowPanel,BoxLayout.X_AXIS));
		ArrayList<Object> row = new ArrayList<Object>();
		
		
		for (ArrayList<Object> pr : prop) {
			JButton pButton = new JButton();
			if (pr.size() == 1) {
				pButton.setBackground(Color.GRAY);
			} else {
				if ((Boolean) pr.get(1) == true) {
					pButton.setBackground(Color.blue);
					pButton.setForeground(Color.WHITE);
				} else {
					pButton.setBackground(Color.red);
				}
			}
			//pButton.setBounds(0, 0, 60,30);
			//pButton.setPreferredSize(new Dimension(40, 40));
			pButton.setText(pr.get(0).toString());
			pButton.setVisible(true);
			
			if(row.size()<3)
			{
				row.add(pButton);
				row.add(Box.createHorizontalStrut(10));
			}
			else				
			{
				row.add(pButton);
				for(Object com : row)
					rowPanel.add((Component) com);
				headers.add(rowPanel);
				rowPanel = new JPanel();
				rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
				row = new ArrayList<Object>();
			}
			values.add(new JLabel());
		}
		if(row.size()>0)
		{
			for(Object com : row)
				rowPanel.add((Component) com);
			headers.add(rowPanel);
			rowPanel = new JPanel();
			rowPanel.setLayout(new BoxLayout(rowPanel,BoxLayout.X_AXIS));
			row = new ArrayList<Object>();
		}
		values.add(new JLabel());
		putContents(panel);
	}
	
	/**
	 * Konstruktor odpowiedzialny za utworzenie element�w podokna symulatora inwariant�w
	 * @param is
	 */
	public PropertiesTable(InvariantsSimulator is)
	{
		initiateContainers();
		// set mode
		mode = INVARIANTSSIMULATOR;
		invSimulator = is;
		
		String[] simModeName = {"Klasyczny", "Czasowy"};
		// set mode
		//mode = SIMULATOR;
		//simulator = sim;
				
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox simMode = new JComboBox(simModeName);
		simMode.setSelectedIndex(0);
		/*
		simMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				invSimulator.setSimulatorNetType(simMode.getSelectedIndex());
				//simulator.setSimulatorNetType(simMode.getSelectedIndex());
			}
		});
		*/
		simMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				@SuppressWarnings("rawtypes")
				JComboBox comboBox = (JComboBox) actionEvent.getSource();
				if (comboBox.getSelectedIndex() == 0) {
					invSimulator.setSimulatorNetType(0);
				} else {
					invSimulator.setSimulatorNetType(1);
				}
			}
		});

		headers.add(simMode);
		//group.add(simMode);
		values.add(new JLabel("Typ symulacji"));
		
		 JRadioButton TimeMode = new JRadioButton("Time Mode");
		 TimeMode.setActionCommand("0");
		 headers.add(TimeMode);
		 group.add(TimeMode);	
		 values.add(new JLabel(""));
		 JRadioButton StepMode = new JRadioButton("Step Mode");
		 StepMode.setActionCommand("1");
		 headers.add(StepMode);
		 group.add(StepMode);
		 values.add(new JLabel(""));
		 JRadioButton CycleMode = new JRadioButton("Cycle Mode");
		 CycleMode.setActionCommand("2");
		 headers.add(CycleMode);
		 group.add(CycleMode);
		 values.add(new JLabel(""));
		 
		 SpinnerModel timeCycle = new SpinnerNumberModel(5,1,9999,1);
		 spiner = new JSpinner(timeCycle);
		 
		 headers.add(spiner);
		 values.add(new JLabel(""));
		 group.setSelected(TimeMode.getModel(), true);
		 JButton start = new JButton("Start");
		 
		 start.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent actionEvent) {
					
					if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getInvariantsList().size()>0)
					{
					//blokowanie
					setEnabledInvariantSimulationInitiateButtons(false);
					
					//odpalanie
					try {
						GUIManager.getDefaultGUIManager().startInvariantsSimulation(Integer.valueOf(group.getSelection().getActionCommand()),(Integer) spiner.getValue());
					} catch (CloneNotSupportedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					}
					else
					{
						JOptionPane.showMessageDialog(new JFrame(),"There is no invariants to simulate",
							    "Inane warning",JOptionPane.WARNING_MESSAGE);
					}
				}
			});
		 headers.add(start);
		 values.add(new JLabel(""));
		 putContents(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za utworzenie podokna wyboru zbior�w MCT.
	 * @param mct ArrayList[ArrayList[Transition]] - macierz zbior�w MCT
	 * @param type Properties.PropertiesType - nic nie znacz�cy tutaj element...
	 */
	public PropertiesTable(ArrayList<ArrayList<Transition>> mct, Properties.PropertiesType type) {
		initiateContainers();
		// set mode
		mode = EXTERNAL_ANALYSIS;
		this.mctGroups = mct;
		// getting the data
		JLabel chooseMctLabel = new JLabel("Choose invariant: ");
		chooseMctLabel.setMaximumSize(chooseMctLabel.getMinimumSize());
		headers.add(chooseMctLabel);
				
		String[] mctHeaders = new String[mctGroups.size() + 1];
		mctHeaders[0] = "---";
		for (int i = 0; i < mctGroups.size(); i++)
			mctHeaders[i + 1] = "MCT no. " + Integer.toString(i);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox chooseMctBox = new JComboBox(mctHeaders);
		chooseMctBox.setMaximumSize(chooseMctBox.getMinimumSize());
		chooseMctBox.setVisible(true);
		chooseMctBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				@SuppressWarnings("rawtypes")
				JComboBox comboBox = (JComboBox) actionEvent.getSource();
				if (comboBox.getSelectedIndex() == 0) {
					showMct(0, false);
				} else {
					showMct(comboBox.getSelectedIndex()-1, true);
				}
				
				invariantPanel.repaint();
				mainPanel.repaint();
				invariantPanel.repaint();
			}
		});
		values.add(chooseMctBox);
		// put all contents on the pane
		putContents(panel);
	}

	/*
	
	 //Metoda odpowiedzialna za pokazanie szczeg��w wybranego zbioru MCT.
	 //@param mctIndex Integer - numer wybranego zbioru
	 
	@SuppressWarnings("unused")
	private void showMct(Integer mctIndex) {
		PetriNet net = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		net.turnTransitionGlowingOff();
		net.setTransitionGlowedMTC(false); //wy��czanie pod�wietlenia MCT
		headers.clear();
		values.clear();
		invariantPanel.removeAll();
		this.remove(invariantPanel);
		invariantPanel = new JPanel();
		this.add(invariantPanel);
		
		ArrayList<Transition> mct = mctGroups.get(mctIndex);
		JLabel number = new JLabel("0");
		headers.add(new JLabel("Transitions: "));
		values.add(number);
		headers.add(new JLabel(""));
		values.add(new JLabel(""));
		headers.add(new JLabel("ID"));
		values.add(new JLabel("Name"));
		headers.add(new JLabel(""));
		values.add(new JLabel(""));
		//int i = 0;
		int counter = 0;
		for (Transition transition : mct) {
			headers.add(new JLabel(Integer.toString(transition.getID())));
			values.add(new JLabel(transition.getName()));
			transition.setGlowedMTC(true);
			counter++;
			number.setText(Integer.toString(counter));
			putContents(invariantPanel);
		}
		net.repaintAllGraphPanels();
	}
	*/
	
	/**
	 * Metoda odpowiedzialna za pokazanie szczeg��w wybranego zbioru MCT.
	 * @param mctIndex Integer - numer wybranego zbioru
	 * @param mc boolean - true, je�li dane maj� by� pokazane
	 */
	private void showMct(Integer mctIndex, boolean mc) {
		PetriNet net = GUIManager.getDefaultGUIManager().getWorkspace() .getProject();
		net.turnTransitionGlowingOff();
		net.setTransitionGlowedMTC(false);
		headers.clear();
		values.clear();
		invariantPanel.removeAll();
		this.remove(invariantPanel);
		invariantPanel = new JPanel();
		this.add(invariantPanel);
		
		if(mc)
		{
			ArrayList<Transition> mct = mctGroups.get(mctIndex);
			JLabel number = new JLabel("0");
			headers.add(new JLabel("Transitions: "));
			values.add(number);
			headers.add(new JLabel(""));
			values.add(new JLabel(""));
			headers.add(new JLabel("ID"));
			values.add(new JLabel("Name"));
			headers.add(new JLabel(""));
			values.add(new JLabel(""));
			//int i = 0;
			int counter = 0;
			for (Transition transition : mct) {
				headers.add(new JLabel(Integer.toString(transition.getID())));
				values.add(new JLabel(transition.getName()));
				transition.setGlowedMTC(true);
				counter++;
				number.setText(Integer.toString(counter));
				putContents(invariantPanel);
			}
		}
		net.repaintAllGraphPanels();
	}

	
	/**
	 * Metoda odpowiedzialna za pod�wietlanie inwariant�w.
	 * @param invariantIndex Integer - numer wybranego inwariantu
	 * @param inv boolean - true, je�li maj� by� pokazane dane szczeg�owe w panelu
	 */
	private void showInvariant(Integer invariantIndex, boolean inv) {
		PetriNet net = GUIManager.getDefaultGUIManager().getWorkspace() .getProject();
		net.turnTransitionGlowingOff(); //!!!!!
		headers.clear();
		values.clear();
		invariantPanel.removeAll();
		ArrayList<InvariantTransition> invariant = externalInvariants.get(invariantIndex);
		if (inv) {
			JLabel number = new JLabel("0");
			headers.add(new JLabel("Transitions: "));
			values.add(number);
			long mintime = 0;
			long maxtime = 0;
			
			for (InvariantTransition transition : invariant) {
				mintime+=transition.getTransition().getMinFireTime();
				maxtime+=transition.getTransition().getMaxFireTime();
			}
			
			headers.add(new JLabel("Min. Time: "));
			values.add(new JLabel(String.valueOf(mintime)));
			
			headers.add(new JLabel("Max Time: "));
			values.add(new JLabel(String.valueOf(maxtime)));
			
			headers.add(new JLabel(""));
			values.add(new JLabel(""));
			headers.add(new JLabel("Transition"));
			values.add(new JLabel("Firings"));
			headers.add(new JLabel(""));
			values.add(new JLabel(""));
			//int i = 0;
			int counter = 0;
			for (InvariantTransition transition : invariant) {
				headers.add(new JLabel(Integer.toString(transition
						.getTransition().getID())
						+ " : "
						+ transition.getTransition().getName()));
				values.add(new JLabel(transition.getAmountOfFirings()
						.toString()));
				transition.getTransition().setGlowed(true,
						transition.getAmountOfFirings());
				counter++;
				number.setText(Integer.toString(counter));
				putContents(invariantPanel);
			}
		}
		net.repaintAllGraphPanels();
	}
	
	/**
	 * Metoda pomocnicza tworz�ca szkielet g��wnych komponent�w podokna w�a�ciwo�ci
	 */
	private void initiateContainers() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		headerSize = new Dimension(300, 30);
		headers = new ArrayList<JComponent>();
		values = new ArrayList<JComponent>();
		components = new ArrayList<JComponent>();
		panel = new JPanel();
		invariantPanel = new JPanel();
		mainPanel = this;
	}

	/**
	 * Metoda pomocnicza odpowiedzialna za wype�nanie okna danymi.
	 * @param contentPanel JPanel - panel z zawarto�ci�
	 */
	private void putContents(JPanel contentPanel) {
		/*
		panel.setLayout(null);
		for (JComponent component : headers) {

		}
		for (JComponent component : values) {
			
		}

		//contentPanel.setLayout(new SpringLayout());
		for (int i = 0; i < headers.size(); i++) {
			contentPanel.add(headers.get(i));
			contentPanel.add(values.get(i));
		}
		//SpringUtilities.makeCompactGrid(contentPanel, headers.size(), 2, 5, 2, 2, 5);
		contentPanel.setOpaque(true);
		contentPanel.repaint();
		add(contentPanel);
		*/
		
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		for (JComponent component : headers) {
			component.setMaximumSize(component.getMinimumSize());
			
		}
		for (JComponent component : values)
			component.setMaximumSize(component.getMinimumSize());
		
		contentPanel.setLayout(new SpringLayout());
		for (int i = 0; i < headers.size(); i++) {
			contentPanel.add(headers.get(i));
			contentPanel.add(values.get(i));
		}
		SpringUtilities.makeCompactGrid(contentPanel, headers.size(), 2, 5, 5, 5, 5);
		contentPanel.setOpaque(true);
		//contentPanel.repaint();
		add(contentPanel);
		
	}

	/**
	 * Metoda zwraca okno tekstowe na bazie podanego comboBox.
	 * @param spinner JSpinner - ComboBox po ludzku
	 * @return JFormattedTextField - chyba TextBox?
	 */
	public JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor) editor).getTextField();
		} else {
			System.err.println("Unexpected editor type: " + spinner.getEditor().getClass()
					+ " isn't a descendant of DefaultEditor");
			return null;
		}
	}

	// general operations
	private void repaintGraphPanel() {
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(elementLocation.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		graphPanel.repaint();
	}

	// sheet operations
	private void setSheetWidth(int width) {
		if (mode == SHEET) {
			setContainerWidth(width, currentSheet.getGraphPanel());
			setContainerWidth(width, currentSheet.getContainerPanel());
		}
	}

	private void setSheetHeight(int height) {
		if (mode == SHEET) {
			setContainerHeight(height, currentSheet.getGraphPanel());
			setContainerHeight(height, currentSheet.getContainerPanel());
		}
	}

	private void setContainerWidth(int width, JComponent container) {
		if (mode == SHEET) {
			Dimension dim = container.getSize();
			dim.setSize(width, dim.height);
			container.setSize(dim);
		}
	}

	private void setContainerHeight(int height, JComponent container) {
		if (mode == SHEET) {
			Dimension dim = container.getSize();
			dim.setSize(dim.width, height);
			container.setSize(dim);
		}
	}

	private void setAutoscroll(boolean value) {
		if (mode == SHEET) {
			currentSheet.getGraphPanel().setAutoDragScroll(value);
		}
	}
	
	// time petri net operations
	private void setMinFireTime(double x) {
		if (mode == TIMETRANSITION) {
			TimeTransition transition = (TimeTransition) element;
			transition.setMinFireTime(x);
			repaintGraphPanel();
		}
	}
	
	private void setMaxFireTime(double x) {
		if (mode == TIMETRANSITION) {
			TimeTransition transition = (TimeTransition) element;
			transition.setMaxFireTime(x);
			repaintGraphPanel();
		}
	}

	// general node operations (PLACE or TRANSITION modes)
	private void setX(int x) {
		if (mode == PLACE || mode == TRANSITION) {
			elementLocation.setPosition(new Point(x, elementLocation
					.getPosition().y));
			repaintGraphPanel();
		}
	}

	private void setY(int y) {
		if (mode == PLACE || mode == TRANSITION) {
			elementLocation.setPosition(new Point(
					elementLocation.getPosition().x, y));
			repaintGraphPanel();
		}
	}
	
	/**
	 * Zmiana nazwy elementu sieci, dokonywana poza listenerem, kt�ry
	 * jest klasa anonimow� (i nie widzi pola element).
	 * @param newName String - nowa nazwa
	 */
	private void changeName(String newName) {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			Node node = (Node) element;
			node.setName(newName);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Zmiana warto�ci komentarza dla elementu sieci, poza listenerem, kt�ry
	 * jest klas� anonimow� (i nie widzi pola element).
	 * @param newComment String - nowy komentarz
	 */
	private void changeComment(String newComment) {
		element.setComment(newComment);	
	}

	private void makePortal() {
		if (mode == PLACE || mode == TRANSITION) {
			@SuppressWarnings("unused")
			Node node = (Node) element;
		}
	}

	private void unPortal() {
		if (mode == PLACE || mode == TRANSITION) {
			@SuppressWarnings("unused")
			Node node = (Node) element;
		}
	}

	private void setTokens(int tokenz) {
		Place place = (Place) element;
		if (mode == PLACE) {
			place.setTokensNumber(tokenz);
			repaintGraphPanel();
		}
	}

	// arc specific operations
	private void setWeight(int weight) {
		Arc arc = (Arc) element;
		if (mode == ARC) {
			arc.setWeight(weight);
			repaintGraphPanel();
		}
	}
	
	// ================================================================================
	// invariants simulation specific operations
	// ================================================================================

	public void setEnabledInvariantSimulationInitiateButtons(boolean enabled) {
		for (int i = 0; i < 5; i++) {
			headers.get(i).setEnabled(enabled);
			values.get(i).setEnabled(enabled);
		}
	}

	public void setEnabledInvariantSimulationDisruptButtons(boolean enabled) {
		headers.get(5).setEnabled(enabled);
		values.get(5).setEnabled(enabled);
	}
	

	// ================================================================================
	// simulation specific operations
	// ================================================================================

	public void setEnabledSimulationInitiateButtons(boolean enabled) {
		for (int i = 0; i < 4; i++) {
			headers.get(i).setEnabled(enabled);
			values.get(i).setEnabled(enabled);
		}
	}

	public void setEnabledSimulationDisruptButtons(boolean enabled) {
		headers.get(4).setEnabled(enabled);
		values.get(4).setEnabled(enabled);
	}

	public void allowOnlySimulationInitiateButtons() {
		setEnabledSimulationInitiateButtons(true);
		setEnabledSimulationDisruptButtons(false);
	}

	public void allowOnlySimulationDisruptButtons() {
		setEnabledSimulationInitiateButtons(false);
		setEnabledSimulationDisruptButtons(true);
	}

	public void allowOnlyUnpauseButton() {
		allowOnlySimulationDisruptButtons();
		values.get(4).setEnabled(false);
	}

	/**
	 * hmmm....
	 */
	public void updateSimulatorProperties() {
		// TODO Auto-generated method stub
	}
}
