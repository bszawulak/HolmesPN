package abyss.darkgui.properties;

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

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import abyss.utilities.Tools;
import abyss.workspace.WorkspaceSheet;

/**
 * Klasa zawierająca szczegóły interfejsu podokien dokowalnych programu.
 * @author students
 * @author MR<br>
 * <br>
 * <b>Absolute positioning. Of almost absolute everything here. <br>
 * Nie obchodzi mnie, co o tym myślisz. Idź w layout i nie wracaj. ┌∩┐(◣_◢)┌∩┐
 * </b><br>
 * Właściwie, to wyleje tu swoje żale na Jave, opensourcowe podejścia w tym języku i takie
 * tam. Nie ma to NIC wspólnego ze studentami, którzy się serio postarali i zrobili okna ok.
 * Przerobiłem metody na pozycjonowanie absolutne, wywaliłem w cholerę wszystkie layouty.
 * Bo tak. Bo ludzie padający przed ich ideą na kolana i bijący pokłony "Oh, layout, jak
 * cudownie, wszystko się nam teraz automatycznie rozmieści" nie zauważają, albo nie chcą
 * zauważać, że to 'automatycznie' jest tak do dupy, tak bardzo z... że już bardziej się
 * chyba nie da. PO CO MI LATAJĄCE WE WSZYSTKIE STRONY ELEMENTY OKNA, SKORO CHCIAŁBYM
 * MIEĆ JE NA STAŁE W JEDNYM MIEJSCU?! Ok, ale o co tu chodzi? No więc albo się używa w
 * Javie layoutów, 2 polecenia na krzyż i wszystko się rozmieszcza gdzie chce i jak chce,
 * albo robi ręcznie i okazuje się, że Java w najmniejszym stopniu nie wspiera takiego podejścia.
 * Nagle miliard rzeczy należy ręcznie ustawiać, niepotrzebych na zdrowy rozsądek (PO CO MI 
 * BORDERSIZE JAK MOGĘ USTAWIĆ START LOCATION I SIZE? PO NIC. ALE BEZ NIEGO JPANEL SIE NIE
 * WYŚWIETLI. BO NIE!). Nagle okazuje się, że JPanel ręcznie należy zmusić do przerysowania się
 * (repaint) - bo tak. Z layoutami jakoś pamięta, żeby się narysować. Bez nich już nie.
 * 
 * Konkluzja. Ktoś mógłby powiedzieć, że przecież skoro chce się ręcznie wszystko rozmieścić,
 * to nie należy narzekać, że jest dużo roboty. ZOBACZCIE SOBIE DURNIE .NET MICROSOFTU!!!
 * Są panele, layouty i inne. Ale nie zmuszą się nikogo młotem do ich korzystania jak w Javie.
 * I okazuje się, że nagle jest mniej tam roboty z rozmieszczaniem, niż nawet z layoutami w Javie.
 * Ten język powinien pozostać na etapie konsoli. Jego próby udawania, że służy do
 * tworzenia także aplikacji w oknach kosztują więcej nerwów niż jest to tego warte.
 * 
 * Ostatnia rzecz, jeśli cię to nie przekonuje. Otwórz google. Wpisz dowolną frazę ze słowami
 * "java layout", "problem" względnie "does not". Pół internetu wyleci z pytaniami i (rzadziej) 
 * odpowiedziami. Takie to wspaniałe layouty. (ノ ゜Д゜)ノ ︵ ┻━━┻ 
 */
public class AbyssDockWindowsTable extends JPanel {
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
	 * Konstruktor odpowiedzialny za tworzenie elementów podokna dla symulatora sieci.
	 * @param sim NetSimulator - obiekt symulatora sieci
	 */
	public AbyssDockWindowsTable(NetSimulator sim, InvariantsSimulator is) {
		int columnA_posX = 10;
		int columnB_posX = 80;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 70;
		
		initiateContainers();
		
		String[] simModeName = {"Classic", "Time"};
		mode = SIMULATOR;
		simulator = sim;
		invSimulator = is;
		
		// SIMULATION MODE
		JLabel netTypeLabel = new JLabel("Net:");
		netTypeLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(netTypeLabel);
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final JComboBox simMode = new JComboBox(simModeName); //final, aby listener przycisku odczytał wartość
		simMode.setLocation(columnB_posX, columnB_Y += 10);
		simMode.setSize(colBCompLength, 20);
		simMode.setSelectedIndex(0);
		simMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				int selectedModeIndex = simMode.getSelectedIndex();
				simulator.setSimulatorNetType(selectedModeIndex);
				
				if(invSimulator != null)
					invSimulator.setSimulatorNetType(selectedModeIndex);
			}
			
		});
		components.add(simMode);
		
		
		// SIMULATOR CONTROLS
		// metoda startSimulation obiektu simulator troszczy się o wygaszanie
		// i aktywowanie odpowiednich przycisków
		JLabel controlsLabel = new JLabel("Simulation options:");
		controlsLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength*2, 20);
		components.add(controlsLabel);
		columnB_Y += 20;
		
		JButton oneActionBack = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_back.png"));
		oneActionBack.setName("simB1");
		oneActionBack.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 30);
		//oneActionBack.setLocation(columnA_posX, columnA_Y);
		//oneActionBack.setSize(colACompLength, 30);
		oneActionBack.setToolTipText("One action back");
		oneActionBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.ACTION_BACK);
			}
		});
		components.add(oneActionBack);
		
		JButton oneTransitionForward = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_fwd.png"));
		oneTransitionForward.setName("simB2");
		oneTransitionForward.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 30);
		//oneTransitionForward.setLocation(columnB_posX, columnB_Y);
		//oneTransitionForward.setSize(colBCompLength, 30);
		oneTransitionForward.setToolTipText("One transition forward");
		oneTransitionForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION);
			}
		});
		components.add(oneTransitionForward);
		
		JButton loopBack = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_backLoop.png"));
		loopBack.setName("simB3");
		loopBack.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
		//loopBack.setLocation(columnA_posX, columnA_Y);
		//loopBack.setSize(colACompLength, 30);
		loopBack.setToolTipText("Loop back to oldest saved action");
		loopBack.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.LOOP_BACK);
			}
		});
		components.add(loopBack);
		JButton oneStepForward = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_fwdLoop.png"));
		oneStepForward.setName("simB4");
		oneStepForward.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
		//oneStepForward.setLocation(columnB_posX, columnB_Y);
		//oneStepForward.setSize(colBCompLength, 30);
		oneStepForward.setToolTipText("One step forward");
		oneStepForward.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.STEP);
			}
		});
		components.add(oneStepForward);
		JButton loopSimulation = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_loop.png"));
		loopSimulation.setName("simB5");
		loopSimulation.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
		//loopSimulation.setLocation(columnA_posX, columnA_Y);
		//loopSimulation.setSize(colACompLength, 30);
		loopSimulation.setToolTipText("Loop simulation");
		loopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.LOOP);
			}
		});
		components.add(loopSimulation);
		JButton singleTransitionLoopSimulation = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_1transLoop.png"));
		singleTransitionLoopSimulation.setName("simB6");
		singleTransitionLoopSimulation.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
		//singleTransitionLoopSimulation.setLocation(columnB_posX, columnB_Y);
		//singleTransitionLoopSimulation.setSize(colBCompLength, 30);
		singleTransitionLoopSimulation.setToolTipText("Loop single transition simulation");
		singleTransitionLoopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.startSimulation(SimulatorMode.SINGLE_TRANSITION_LOOP);
			}
		});
		components.add(singleTransitionLoopSimulation);
		
		JButton pauseSimulation = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_pause.png"));
		pauseSimulation.setName("stop");
		pauseSimulation.setBounds(columnA_posX, columnA_Y += 30, colACompLength, 30);
		//pauseSimulation.setLocation(columnA_posX, columnA_Y);
		//pauseSimulation.setSize(colACompLength, 30);
		pauseSimulation.setToolTipText("Pause simulation");
		pauseSimulation.setEnabled(false);
		pauseSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.pause();
			}
		});
		components.add(pauseSimulation);
		
		JButton stopSimulation = new JButton(
				Tools.getResIcon22("/icons/simulation/control_sim_stop.png"));
		stopSimulation.setName("stop");
		stopSimulation.setBounds(columnB_posX, columnB_Y += 30, colBCompLength, 30);
		//stopSimulation.setLocation(columnB_posX, columnB_Y);
		//stopSimulation.setSize(colBCompLength, 30);
		stopSimulation.setToolTipText("Schedule a stop for the simulation");
		stopSimulation.setEnabled(false);
		stopSimulation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				simulator.stop();
			}
		});
		components.add(stopSimulation);
		
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
		
		JLabel maxLabel = new JLabel("Maximum mode", JLabel.LEFT);
		maxLabel.setBounds(columnB_posX-50, columnB_Y += 30, colBCompLength+40, 20);
		components.add(maxLabel);
		
		JCheckBox maximumMode = new JCheckBox("");
		maximumMode.setBounds(columnA_posX, columnA_Y += 30, 20, 20);
		//maximumMode.setLocation(columnA_posX-4, columnA_Y);
		maximumMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					simulator.setMaximumMode(true);
				} else {
					simulator.setMaximumMode(false);
				}
			}
		});
		components.add(maximumMode);
		
		//PANEL SYMULATORA INWARIANTÓW
		JPanel staticPropertiesPanel = new JPanel();
		staticPropertiesPanel.setLayout(null);
		staticPropertiesPanel.setBorder(BorderFactory.createTitledBorder("Invariants simulator"));
		staticPropertiesPanel.setBounds(columnA_posX-5, columnA_Y += 20, 160, 160);
		
		int internalXA = 10;
		int internalXB = 60;
		int internalY = 20;
		
		JLabel simTypeLabel = new JLabel("Mode:");
		simTypeLabel.setBounds(internalXA, internalY, 50, 20);
		staticPropertiesPanel.add(simTypeLabel);
		
		JRadioButton TimeMode = new JRadioButton("Time Mode");
		TimeMode.setBounds(internalXB, internalY, 90, 20);
		//TimeMode.setLocation(internalXB, internalY);
		internalY+=20;
		TimeMode.setSize(90, 20);
		TimeMode.setActionCommand("0");
		staticPropertiesPanel.add(TimeMode);
		group.add(TimeMode);
		
		columnA_Y += 20;
		JRadioButton StepMode = new JRadioButton("Step Mode");
		StepMode.setBounds(internalXB, internalY, 90, 20);
		//StepMode.setLocation(internalXB, internalY);
		internalY+=20;
		StepMode.setSize(90, 20);
		StepMode.setActionCommand("1");
		staticPropertiesPanel.add(StepMode);
		group.add(StepMode);
		
		columnA_Y += 20;
		JRadioButton CycleMode = new JRadioButton("Cycle Mode");
		CycleMode.setBounds(internalXB, internalY, 90, 20);
		//CycleMode.setLocation(internalXB, internalY);
		internalY+=20;
		CycleMode.setSize(90, 20);
		CycleMode.setActionCommand("2");
		staticPropertiesPanel.add(CycleMode);
		group.add(CycleMode);
		group.setSelected(TimeMode.getModel(), true);
		
		JLabel timeLabel = new JLabel("Time (min):");
		timeLabel.setBounds(internalXA, internalY, 70, 20);
		staticPropertiesPanel.add(timeLabel);
		
		SpinnerModel timeCycle = new SpinnerNumberModel(1,1,9999,1);
		spiner = new JSpinner(timeCycle);
		spiner.setLocation(internalXB+20, internalY+3);
		spiner.setSize(70, 20);
		internalY+=25;
		staticPropertiesPanel.add(spiner);
		
		// INVARIANTS SIMULATION START BUTTON
		//JButton startButton = new JButton("Start");
		JButton startButton = new JButton(Tools.getResIcon22("/icons/simulation/control_sim_fwd.png"));
		startButton.setBounds(internalXA, internalY, 80, 30);
		//startButton.setLocation(internalXA, internalY);
		//startButton.setSize(80, 40);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().get2ndFormInvariantsList().size()>0)
				{
					setEnabledSimulationInitiateButtons(false);
					setEnabledSimulationDisruptButtons(false);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationInitiateButtons(false);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationDisruptButtons(false);
					
					try {
						GUIManager.getDefaultGUIManager().startInvariantsSimulation(
								Integer.valueOf(group.getSelection().getActionCommand()), 
								(Integer) spiner.getValue()); //jaki tryb
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
					//STOP:
					setEnabledSimulationInitiateButtons(true);
					setEnabledSimulationDisruptButtons(false);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationInitiateButtons(true);
					GUIManager.getDefaultGUIManager().getShortcutsBar().setEnabledSimulationDisruptButtons(false);
					
				}
				else
				{
					JOptionPane.showMessageDialog(new JFrame(),"There are no invariants to simulate.",
						    "Invariant simulator",JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		staticPropertiesPanel.add(startButton);
		components.add(staticPropertiesPanel);
		
		panel.setLayout(null); 
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}

	/**
	 * Konstruktor podokna wyświetlającego właściwości klikniętego miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param location ElementLocation - lokalizacja miejsca
	 */
	public AbyssDockWindowsTable(Place place, ElementLocation location) {
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

		// ID
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		//components.add(idLabel);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(place.getID()));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, 50, 20);
		components.add(idLabel2);

		// NAME
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
		
		// KOMENTARZE WIERZCHOŁKA
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
        
		// PLACE TOKEN
        JLabel tokenLabel = new JLabel("Tokens:", JLabel.LEFT);
        tokenLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
        components.add(tokenLabel);
        int tok = place.getTokensNumber();
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(tok, 0, 
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
		zoomLabel.setBounds(columnB_posX+100, columnB_Y, colBCompLength, 20);
		if(zoom != 100)
			zoomLabel.setForeground(Color.red);
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
	 * Metoda odpowiedzialna za wyświetlenie właściwości klikniętej tranzycji.
	 * @param transition Transition - obiekt tranzycji sieci
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public AbyssDockWindowsTable(Transition transition, ElementLocation location) {
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
		
		//KOMENTARZE WIERZCHOŁKA:
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);	
		
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
		zoomLabel.setBounds(columnB_posX+100, columnB_Y, colBCompLength, 20);
		if(zoom != 100)
			zoomLabel.setForeground(Color.red);
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
	 * Metoda odpowiedzialna za wyświetlenie właściwości klikniętej tranzycji czasowej.
	 * @param transition TimeTransition - obiekt tranzycji czasowej
	 * @param location ElementLocation - lokalizacja tranzycji
	 */
	public AbyssDockWindowsTable(final TimeTransition transition, ElementLocation location) {
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
		
		// T-TRANSITION COMMENT
		JLabel comLabel = new JLabel("Comment:", JLabel.LEFT);
		comLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(comLabel);
		
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

		// T-TRANSITION SHEET ID
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
		zoomLabel.setBounds(columnB_posX+100, columnB_Y, colBCompLength, 20);
		if(zoom != 100)
			zoomLabel.setForeground(Color.red);
		components.add(zoomLabel);	
		
		// T-TRANSITION LOCATION:
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
		
		// T-TRANSITION PORTAL STATUS
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

		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za utworzenie elementów podokna właściwości klikniętego
	 * łuku sieci.
	 * @param arc Arc - obiekt łuku
	 */
	public AbyssDockWindowsTable(Arc arc) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		initiateContainers();
		// set mode
		mode = ARC;
		element = arc;
		elementLocation = arc.getStartLocation();
		
		// ARC ID
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(arc.getID()));
		idLabel2.setBounds(columnB_posX, columnB_Y += 10, colACompLength, 20);
		components.add(idLabel2);
		
		// ARC COMMENT
		JLabel commLabel = new JLabel("Comment:", JLabel.LEFT);
		commLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		columnA_Y += 20;
		components.add(commLabel);
		
		JTextArea commentField = new JTextArea(arc.getComment());
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
		
		// ARC WEIGHT
        JLabel weightLabel = new JLabel("Weight:", JLabel.LEFT);
        weightLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(weightLabel);
		
		SpinnerModel weightSpinnerModel = new SpinnerNumberModel(arc.getWeight(), 0, Integer.MAX_VALUE, 1);
		JSpinner weightSpinner = new JSpinner(weightSpinnerModel);
		weightSpinner.setLocation(columnB_posX, columnB_Y += 20);
		weightSpinner.setSize(colBCompLength/3, 20);
		weightSpinner.setMaximumSize(new Dimension(colBCompLength/3,20));
		weightSpinner.setMinimumSize(new Dimension(colBCompLength/3,20));
		weightSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int tokenz = (int) spinner.getValue();
				setWeight(tokenz);
			}
		});
		components.add(weightSpinner);

		// startNode
		columnB_posX+= 30;
		colACompLength += 40;
		JLabel label1A = new JLabel("StartNode ID:", JLabel.LEFT);
		label1A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label1A);
		JLabel label1B = new JLabel(Integer.toString(arc.getStartNode().getID()));
		label1B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label1B);
		
		JLabel label2A = new JLabel("StartNode Name:", JLabel.LEFT);
		label2A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label2A);
		JLabel label2B = new JLabel(arc.getStartNode().getName());
		label2B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label2B);
		
		JLabel label3A = new JLabel("StartNode Sheet:", JLabel.LEFT);
		label3A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label3A);
		JLabel label3B = new JLabel(Integer.toString(arc.getStartLocation().getSheetID()));
		label3B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label3B);
		
		JLabel label4A = new JLabel("Location:", JLabel.LEFT);
		label4A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label4A);
		JLabel label4B = new JLabel(Integer.toString(arc.getStartLocation().getPosition().x)+ ", "
				+ Integer.toString(arc.getStartLocation().getPosition().y));
		label4B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label4B);
		
		// endNode
		JLabel label5A = new JLabel("EndNode ID:", JLabel.LEFT);
		label5A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label5A);
		JLabel label5B = new JLabel(Integer.toString(arc.getEndNode().getID()));
		label5B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label5B);
		
		JLabel label6A = new JLabel("EndNode Name:", JLabel.LEFT);
		label6A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label6A);
		JLabel label6B = new JLabel(arc.getEndNode().getName());
		label6B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label6B);
		
		JLabel label7A = new JLabel("EndNode Sheet:", JLabel.LEFT);
		label7A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label7A);
		JLabel label7B = new JLabel(Integer.toString(arc.getEndLocation().getSheetID()));
		label7B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label7B);
		
		JLabel label8A = new JLabel("EndNode Location:", JLabel.LEFT);
		label8A.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(label8A);
		JLabel label8B = new JLabel(Integer.toString(arc.getEndLocation().getPosition().x)+ ", "
				+ Integer.toString(arc.getEndLocation().getPosition().y));
		label8B.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		components.add(label8B);
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za wypełnienie podokna właściwości dla wybranego arkusza sieci. 
	 * @param sheet WorkspaceSheet - obiekt arkusza
	 */
	public AbyssDockWindowsTable(WorkspaceSheet sheet) {
		int columnA_posX = 10;
		int columnB_posX = 100;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 70;
		int colBCompLength = 200;
		
		initiateContainers();
		mode = SHEET;
		currentSheet = sheet;

		// SHEET ID
		JLabel netNameLabel = new JLabel("PN Name:", JLabel.LEFT);
		netNameLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(netNameLabel);

		JFormattedTextField netNameField = new JFormattedTextField();
		netNameField.setLocation(columnB_posX, columnB_Y += 10);
		netNameField.setSize(colBCompLength, 20);
		netNameField.setMaximumSize(new Dimension(colBCompLength,20));
		netNameField.setMinimumSize(new Dimension(colBCompLength,20));
		netNameField.setText(GUIManager.getDefaultGUIManager().getWorkspace().getProject().getName());
		netNameField.addPropertyChangeListener("value", new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				JFormattedTextField field = (JFormattedTextField) e.getSource();
				try {
					field.commitEdit();
				} catch (ParseException ex) {
				}
				String newName = (String) field.getText();
				
				GUIManager.getDefaultGUIManager().getWorkspace().getProject().setName(newName);
			}
		});
		components.add(netNameField);
		
		
		JLabel idLabel = new JLabel("ID:", JLabel.LEFT);
		idLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(idLabel);
		JLabel idLabel2 = new JLabel(Integer.toString(sheet.getId()));
		idLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		components.add(idLabel2);
		
		// SHEET ZOOM
		int zoom = sheet.getGraphPanel().getZoom();
		//
		Dimension x = sheet.getGraphPanel().getOriginSize();
		int widthOrg = 0;
		int heightOrg = 0;
		if(x != null) {
			widthOrg = (int) x.getWidth();
			heightOrg =  (int) x.getHeight();
		} else {
			widthOrg = sheet.getGraphPanel().getSize().width;
			heightOrg =  sheet.getGraphPanel().getSize().height;
		}
		
		widthOrg = (int) (((double)100/(double)zoom) * widthOrg);
		heightOrg = (int) (((double)100/(double)zoom) * heightOrg);
		
		JLabel zoomLabel1 = new JLabel("Zoom:", JLabel.LEFT);
		zoomLabel1.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(zoomLabel1);
		JLabel zoomLabel2 = new JLabel(Integer.toString(zoom)+"%");
		zoomLabel2.setBounds(columnB_posX, columnB_Y += 20, colACompLength, 20);
		if(zoom != 100)
			zoomLabel2.setForeground(Color.red);
		components.add(zoomLabel2);
		
		// SHEET SIZE
		JLabel widthLabel = new JLabel("Width:", JLabel.LEFT);
		widthLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(widthLabel);
		
		SpinnerModel widthSpinnerModel = new SpinnerNumberModel(sheet.getGraphPanel().getSize().width,
				0, Integer.MAX_VALUE, 1);
		JSpinner widthSpinner = new JSpinner(widthSpinnerModel);
		widthSpinner.setLocation(columnB_posX, columnB_Y += 20);
		widthSpinner.setSize(colBCompLength/2, 20);
		widthSpinner.setMaximumSize(new Dimension(colBCompLength/2,20));
		widthSpinner.setMinimumSize(new Dimension(colBCompLength/2,20));
		widthSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int width = (int) spinner.getValue();
				setSheetWidth(width);
			}
		});
		components.add(widthSpinner);
		
		JLabel widthLabel2 = new JLabel(Integer.toString(widthOrg), JLabel.LEFT);
		widthLabel2.setBounds(columnB_posX+110, columnA_Y, colACompLength, 20);
		components.add(widthLabel2);
		JLabel widthLabel3 = new JLabel("(orig.)", JLabel.LEFT);
		widthLabel3.setBounds(columnB_posX+150, columnA_Y, colACompLength, 20);
		components.add(widthLabel3);
		
		// SHEET HEIGHT
		JLabel heightLabel = new JLabel("Height:", JLabel.LEFT);
		heightLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(heightLabel);
		
		SpinnerModel heightSpinnerModel = new SpinnerNumberModel(sheet.getGraphPanel().getSize().height,
				0, Integer.MAX_VALUE, 1);
		JSpinner heightSpinner = new JSpinner(heightSpinnerModel);
		heightSpinner.setLocation(columnB_posX, columnB_Y += 20);
		heightSpinner.setSize(colBCompLength/2, 20);
		heightSpinner.setMaximumSize(new Dimension(colBCompLength/2,20));
		heightSpinner.setMinimumSize(new Dimension(colBCompLength/2,20));
		heightSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int height = (int) spinner.getValue();
				setSheetHeight(height);
			}
		});
		components.add(heightSpinner);
		if(zoom != 100) {
			widthSpinner.setEnabled(false);
			heightSpinner.setEnabled(false);
		}
		
		JLabel heightLabel2 = new JLabel(Integer.toString(heightOrg), JLabel.LEFT);
		heightLabel2.setBounds(columnB_posX+110, columnB_Y, colACompLength, 20);
		components.add(heightLabel2);
		JLabel heightLabel3 = new JLabel("(orig.)", JLabel.LEFT);
		heightLabel3.setBounds(columnB_posX+150, columnB_Y, colACompLength, 20);
		components.add(heightLabel3);
		
		// is auto scroll when dragging automatic
		JLabel autoSrclLabel = new JLabel("Autoscroll:", JLabel.LEFT);
		autoSrclLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(autoSrclLabel);
		
		JCheckBox autoscrollBox = new JCheckBox("", sheet.getGraphPanel().isAutoDragScroll());
		autoscrollBox.setBounds(columnB_posX-4, columnB_Y, colACompLength, 20);
		autoscrollBox.setLocation(columnB_posX-4, columnB_Y += 20);
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
		components.add(autoscrollBox);
		
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		add(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za wypełnienie podokna umożliwiającego wybór poszczególnych
	 * inwariantów sieci.
	 * @param invariants ArrayList[ArrayList[InvariantTransition]] - macierz inwariantów
	 */
	public AbyssDockWindowsTable(ArrayList<ArrayList<InvariantTransition>> invariants) {
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
	 * Konstruktor podokna właściwości elementów sieci. Wypełniany w zależności od
	 * tego, co przyszło jako argument - tj. które właściwości.
	 * @param prop ArrayList[ArrayList[Object]] - "wektor" właściwości
	 * @param ref boolean - wartość logiczna nie mająca na nic wpływu :)
	 */
	public AbyssDockWindowsTable(ArrayList<ArrayList<Object>> prop, boolean ref) {
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
	 * Konstruktor odpowiedzialny za utworzenie elementów podokna symulatora inwariantów
	 * @param is
	 */
	//TODO: UNUSED
	public AbyssDockWindowsTable(InvariantsSimulator is)
	{
		int columnA_posX = 10;
		int columnB_posX = 60;
		int columnA_Y = 0;
		int columnB_Y = 0;
		int colACompLength = 50;
		int colBCompLength = 140;
		String[] simModeName = {"Classic", "TPN"};
		
		initiateContainers();
		mode = INVARIANTSSIMULATOR;
		invSimulator = is;
		
		// INVARIANTS SIMULATION NETWORK TYPE
		JLabel netTypeLabel = new JLabel("Net:");
		netTypeLabel.setBounds(columnA_posX, columnA_Y += 10, colACompLength, 20);
		components.add(netTypeLabel);
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		JComboBox simMode = new JComboBox(simModeName);
		simMode.setLocation(columnB_posX+5, columnB_Y += 10);
		simMode.setSize(colBCompLength-60, 20);
		simMode.setSelectedIndex(0);
		simMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				@SuppressWarnings("rawtypes")
				JComboBox comboBox = (JComboBox) actionEvent.getSource();
				if (comboBox.getSelectedIndex() == 0) {
					if(invSimulator != null)
						invSimulator.setSimulatorNetType(0);
				} else {
					if(invSimulator != null)
						invSimulator.setSimulatorNetType(1);
				}
			}
		});
		components.add(simMode);
		
		// INVARIANTS SIMULATION MODE
		JLabel simTypeLabel = new JLabel("Mode:");
		simTypeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(simTypeLabel);
		
		JRadioButton TimeMode = new JRadioButton("Time Mode");
		TimeMode.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		TimeMode.setLocation(columnB_posX, columnB_Y);
		TimeMode.setSize(colBCompLength, 20);
		TimeMode.setActionCommand("0");
		components.add(TimeMode);
		group.add(TimeMode);
		
		columnA_Y += 20;
		JRadioButton StepMode = new JRadioButton("Step Mode");
		StepMode.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		StepMode.setLocation(columnB_posX, columnB_Y);
		StepMode.setSize(colBCompLength, 20);
		StepMode.setActionCommand("1");
		components.add(StepMode);
		group.add(StepMode);

		columnA_Y += 20;
		JRadioButton CycleMode = new JRadioButton("Cycle Mode");
		CycleMode.setBounds(columnB_posX, columnB_Y += 20, colBCompLength, 20);
		CycleMode.setLocation(columnB_posX, columnB_Y);
		CycleMode.setSize(colBCompLength, 20);
		CycleMode.setActionCommand("2");
		components.add(CycleMode);
		group.add(CycleMode);
		group.setSelected(TimeMode.getModel(), true);
		
		// INVARIANTS SIMULATION TIME
		JLabel timeLabel = new JLabel("Time:");
		timeLabel.setBounds(columnA_posX, columnA_Y += 20, colACompLength, 20);
		components.add(timeLabel);
		
		SpinnerModel timeCycle = new SpinnerNumberModel(1,1,9999,1);
		spiner = new JSpinner(timeCycle);
		spiner.setLocation(columnB_posX+5, columnB_Y += 20);
		spiner.setSize(colBCompLength-60, 20);
		components.add(spiner);
		
		// INVARIANTS SIMULATION START
		JButton startButton = new JButton("Start");
		startButton.setBounds(columnA_posX, columnB_Y += 40, colBCompLength*2, 40);
		startButton.setLocation(columnA_posX, columnB_Y);
		//startButton.setSize(colBCompLength, 40);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(GUIManager.getDefaultGUIManager().getWorkspace().getProject().get2ndFormInvariantsList().size()>0)
				{
					//blokowanie
					//setEnabledInvariantSimulationInitiateButtons(false);
					//odpalanie
					try {
						GUIManager.getDefaultGUIManager().startInvariantsSimulation(
								Integer.valueOf(group.getSelection().getActionCommand()), 
								(Integer) spiner.getValue()); //jaki tryb
					} catch (CloneNotSupportedException e) {
						e.printStackTrace();
					}
				}
				else
				{
					JOptionPane.showMessageDialog(new JFrame(),"There are no invariants to simulate",
						    "Ina warning",JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		components.add(startButton);
		 
		panel.setLayout(null);
		for (int i = 0; i < components.size(); i++)
			 panel.add(components.get(i));
		panel.setOpaque(true);
		panel.repaint();
		panel.setVisible(true);
		add(panel);
	}

	/**
	 * Konstruktor odpowiedzialny za utworzenie podokna wyboru zbiorów MCT.
	 * @param mct ArrayList[ArrayList[Transition]] - macierz zbiorów MCT
	 * @param type Properties.PropertiesType - nic nie znaczący tutaj element...
	 */
	public AbyssDockWindowsTable(ArrayList<ArrayList<Transition>> mct, AbyssDockWindow.DockWindowType type) {
		initiateContainers();
		// set mode
		mode = EXTERNAL_ANALYSIS;
		//mode = MCT;
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
	
	 //Metoda odpowiedzialna za pokazanie szczegółów wybranego zbioru MCT.
	 //@param mctIndex Integer - numer wybranego zbioru
	 
	@SuppressWarnings("unused")
	private void showMct(Integer mctIndex) {
		PetriNet net = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		net.turnTransitionGlowingOff();
		net.setTransitionGlowedMTC(false); //wyłączanie podświetlenia MCT
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
	 * Metoda odpowiedzialna za pokazanie szczegółów wybranego zbioru MCT.
	 * @param mctIndex Integer - numer wybranego zbioru
	 * @param mc boolean - true, jeśli dane mają być pokazane
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
	 * Metoda odpowiedzialna za podświetlanie inwariantów.
	 * @param invariantIndex Integer - numer wybranego inwariantu
	 * @param inv boolean - true, jeśli mają być pokazane dane szczegółowe w panelu
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
				headers.add(new JLabel(Integer.toString(transition.getTransition().getID())
						+ " : " + transition.getTransition().getName()));
				values.add(new JLabel(transition.getAmountOfFirings().toString()));
				transition.getTransition().setGlowed(true,transition.getAmountOfFirings());
				counter++;
				number.setText(Integer.toString(counter));
				putContents(invariantPanel);
			}
		}
		net.repaintAllGraphPanels();
	}
	
	/**
	 * Metoda pomocnicza tworząca szkielet głównych komponentów podokna właściwości.
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
	 * Metoda pomocnicza odpowiedzialna za wypełnanie okna danymi przy użyciu
	 * Layout Managera.
	 * @param contentPanel JPanel - panel z zawartością
	 */
	private void putContents(JPanel contentPanel) {
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

	/**
	 * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
	 */
	private void repaintGraphPanel() {
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(elementLocation.getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager()
				.getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		graphPanel.repaint();
	}

	/**
	 * Metoda zmienia szerokość arkusza dla sieci.
	 * @param width int - nowa szerokość
	 */
	private void setSheetWidth(int width) {
		if (mode == SHEET) {
			setContainerWidth(width, currentSheet.getGraphPanel());
			setContainerWidth(width, currentSheet.getContainerPanel());
			currentSheet.getGraphPanel().setOriginSize(currentSheet.getGraphPanel().getSize());
			//Dimension dim = currentSheet.getGraphPanel().getSize();
			//dim.setSize(width, dim.height);
			//currentSheet.getGraphPanel().setSize(dim);
			//currentSheet.getContainerPanel().setSize(dim);
		}
	}

	/**
	 * Metoda zmienia wysokość arkusza dla sieci.
	 * @param width int - nowa wysokość
	 */
	private void setSheetHeight(int height) {
		if (mode == SHEET) {
			setContainerHeight(height, currentSheet.getGraphPanel());
			setContainerHeight(height, currentSheet.getContainerPanel());
			currentSheet.getGraphPanel().setOriginSize(currentSheet.getGraphPanel().getSize());
			//Dimension dim = currentSheet.getGraphPanel().getSize();
			//dim.setSize(dim.width, height);
			//currentSheet.getGraphPanel().setSize(dim);
			//currentSheet.getContainerPanel().setSize(dim);
		}
	}

	/**
	 * Metoda zmienia szerokość wymiaru dla arkusza dla sieci.
	 * @param width int - nowa szerokość
	 * @param container JComponent - obiekt dla którego zmieniany jest wymiar
	 */
	private void setContainerWidth(int width, JComponent container) {
		if (mode == SHEET) {
			Dimension dim = container.getSize();
			dim.setSize(width, dim.height);
			container.setSize(dim);
		}
	}

	/**
	 * Metoda zmienia wysokość dla wymiaru dla arkusza dla sieci.
	 * @param width int - nowa wysokość
	 * @param container JComponent - obiekt dla którego zmieniany jest wymiar
	 */
	private void setContainerHeight(int height, JComponent container) {
		if (mode == SHEET) {
			Dimension dim = container.getSize();
			dim.setSize(dim.width, height);
			container.setSize(dim);
		}
	}

	/**
	 * Metoda ustawia opcję autoscroll dla panelu graficznego w arkuszu sieci.
	 * @param value boolean - true, jeśli autoscroll włączony
	 */
	private void setAutoscroll(boolean value) {
		if (mode == SHEET) {
			currentSheet.getGraphPanel().setAutoDragScroll(value);
		}
	}
	
	/**
	 * Metoda ustawia nową wartość czasu EFT dla tranzycji czasowej.
	 * @param x int - nowe EFT
	 */
	private void setMinFireTime(double x) {
		if (mode == TIMETRANSITION) {
			TimeTransition transition = (TimeTransition) element;
			transition.setMinFireTime(x);
			repaintGraphPanel();
		}
	}
	
	/**
	 * Metoda ustawia nową wartość czasu LFT dla tranzycji czasowej.
	 * @param x int - nowe LFT
	 */
	private void setMaxFireTime(double x) {
		if (mode == TIMETRANSITION) {
			TimeTransition transition = (TimeTransition) element;
			transition.setMaxFireTime(x);
			repaintGraphPanel();
		}
	}

	/**
	 * Metoda zmienia współrzędną X dla wierzchołka sieci.
	 * @param x int - nowa wartość
	 */
	private void setX(int x) {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			elementLocation.setPosition(new Point(x, elementLocation.getPosition().y));
			repaintGraphPanel();
		}
	}

	/**
	 * Metoda zmienia współrzędną Y dla wierzchołka sieci.
	 * @param y int - nowa wartość
	 */
	private void setY(int y) {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			elementLocation.setPosition(new Point(elementLocation.getPosition().x, y));
			repaintGraphPanel();
		}
	}
	
	/**
	 * Zmiana nazwy elementu sieci, dokonywana poza listenerem, który
	 * jest klasa anonimową (i nie widzi pola element).
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
	 * Metoda zmienia komentarz dla elementu sieci, poza listenerem, który
	 * jest klasą anonimową (i nie widzi pola element).
	 * @param newComment String - nowy komentarz
	 */
	private void changeComment(String newComment) {
		element.setComment(newComment);	
	}

	private void makePortal() {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			@SuppressWarnings("unused")
			Node node = (Node) element;
		}
	}

	private void unPortal() {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			@SuppressWarnings("unused")
			Node node = (Node) element;
		}
	}

	/**
	 * Metoda zmienia liczbę tokenów dla miejsca sieci, poza listenerem, który
	 * jest klasą anonimową (i nie widzi pola element).
	 * @param tokenz int - nowa liczba tokenów
	 */
	private void setTokens(int tokenz) {
		Place place = (Place) element;
		if (mode == PLACE) {
			place.setTokensNumber(tokenz);
			repaintGraphPanel();
		}
	}

	/**
	 * Metoda zmienia wagę dla łuku sieci, poza listenerem, który
	 * jest klasą anonimową (i nie widzi pola element).
	 * @param weight int - nowa waga
	 */
	private void setWeight(int weight) {
		Arc arc = (Arc) element;
		if (mode == ARC) {
			arc.setWeight(weight);
			repaintGraphPanel();
		}
	}
	


	// ================================================================================
	// ================================================================================

	/**
	 * Metoda ustawia status wszystkich przycisków rozpoczęcia symulacji za wyjątkiem
	 * Pauzy i Stopu
	 * @param enabled boolean - true, jeśli mają być aktywne
	 */
	public void setEnabledSimulationInitiateButtons(boolean enabled) {
		for(JComponent comp: components) {
			if(comp instanceof JButton && comp != null && comp.getName() != null) {
				if(comp.getName().equals("simB1") || comp.getName().equals("simB2")
						|| comp.getName().equals("simB3") || comp.getName().equals("simB4") ) {
					comp.setEnabled(enabled);
				}
			}
		}
	}

	/**
	 * Metoda ustawia status przycisków Stop i Pauza.
	 * @param enabled boolean - true, jeśli mają być aktywne
	 */
	public void setEnabledSimulationDisruptButtons(boolean enabled) {
		for(JComponent comp: components) {
			if(comp instanceof JButton && comp != null && comp.getName() != null) {
				if(comp.getName().equals("stop") || comp.getName().equals("pause") ) {
					comp.setEnabled(enabled);
				}
			}
		}
	}

	/**
	 * Metoda uaktywnia tylko przycisku startu dla symulatora, bloku stop i pauzę.
	 */
	public void allowOnlySimulationInitiateButtons() {
		setEnabledSimulationInitiateButtons(true);
		setEnabledSimulationDisruptButtons(false);
	}

	/**
	 * Metoda uaktywnia tylko przyciski stop i pauza dla symulatora. Cała reszta - nieaktywna.
	 */
	public void allowOnlySimulationDisruptButtons() {
		setEnabledSimulationInitiateButtons(false);
		setEnabledSimulationDisruptButtons(true);
	}

	/**
	 * Metoda zostawia aktywny tylko przycisku od-pauzowania.
	 */
	public void allowOnlyUnpauseButton() {
		allowOnlySimulationDisruptButtons();
		//values.get(4).setEnabled(false);
		for(JComponent comp: components) {
			if(comp instanceof JButton && comp != null && comp.getName() != null) {
				if(comp.getName().equals("stop") ) {
					comp.setEnabled(false);
					break;
				}
			}
		}
	}

	/**
	 * hmmm....
	 */
	public void updateSimulatorProperties() {
		// TODO Auto-generated method stub
	}
}
