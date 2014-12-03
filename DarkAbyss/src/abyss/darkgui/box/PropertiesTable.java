package abyss.darkgui.box;

//import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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

public class PropertiesTable extends JPanel {
	private static final long serialVersionUID = 4510802239873443705L;
	private ArrayList<JComponent> headers;
	private ArrayList<JComponent> values;
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

	// Arc constructor
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
		values.add(new JLabel(
				Integer.toString(simulator.getTransitionsAmount())));
		// Arcs total
		headers.add(new JLabel("Arcs:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(simulator.getArcsAmount())));
		// Tokens total
		headers.add(new JLabel("Tokens:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(simulator.getTokensAmount())));
		// put all contents on the pane
		putContents(panel);
	}

	// Place constructor
	public PropertiesTable(Place place, ElementLocation location) {
		// launch all constructors
		elementLocation = location;
		initiateContainers();
		// set mode
		mode = PLACE;
		element = place;
		// getting the data
		// ID
		headers.add(new JLabel("ID:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(place.getID())));
		// name
		headers.add(new JLabel("Name:", JLabel.TRAILING));
		JFormattedTextField nameField = new JFormattedTextField();
		nameField.setText(place.getName());
		nameField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
						try {
							field.commitEdit();
						} catch (ParseException ex) {
						}
						String newName = (String) field.getText();
						changeName(newName);
					}
				});
		values.add(nameField);
		// comment
		JFormattedTextField commentField = new JFormattedTextField();
		commentField.setText(place.getComment());
		commentField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
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
		// tokens
		headers.add(new JLabel("Tokens:", JLabel.TRAILING));
		SpinnerModel tokenSpinnerModel = new SpinnerNumberModel(
				place.getTokensNumber(), 0, Integer.MAX_VALUE, 1);
		JSpinner tokenSpinner = new JSpinner(tokenSpinnerModel);
		tokenSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int tokenz = (int) spinner.getValue();
				setTokens(tokenz);
			}
		});
		values.add(tokenSpinner);
		// location
		headers.add(new JLabel("Sheet:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(location.getSheetID())));
		headers.add(new JLabel("Location:", JLabel.TRAILING));
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(
				location.getSheetID());
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
		// is Portal
		headers.add(new JLabel("Portal:", JLabel.TRAILING));
		JCheckBox portalBox = new JCheckBox("", place.isPortal());
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
		values.add(portalBox);
		// put all contents on the pane
		putContents(panel);
	}

	// Transition constructor
	public PropertiesTable(Transition transition, ElementLocation location) {
		mode = TRANSITION;
		// launch all constructors
		elementLocation = location;
		initiateContainers();
		// set mode
		element = transition;
		// getting the data
		// ID
		headers.add(new JLabel("ID:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(transition.getID())));
		// name
		headers.add(new JLabel("Name:", JLabel.TRAILING));
		JFormattedTextField nameField = new JFormattedTextField();
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
						try {
							field.commitEdit();
						} catch (ParseException ex) {
						}
						String newName = (String) field.getValue();
						changeName(newName);
					}
				});
		values.add(nameField);
		// comment
		JFormattedTextField commentField = new JFormattedTextField();
		commentField.setValue(transition.getComment());
		commentField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
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
		// location
		headers.add(new JLabel("Sheet:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(location.getSheetID())));
		headers.add(new JLabel("Location:", JLabel.TRAILING));
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(
				location.getSheetID());
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
		// is Portal
		headers.add(new JLabel("Portal:", JLabel.TRAILING));
		JCheckBox portalBox = new JCheckBox("", transition.isPortal());
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
		values.add(portalBox);
		// put all contents on the pane
		putContents(panel);
	}

	// Transition constructor
	public PropertiesTable(final TimeTransition transition,
			ElementLocation location) {
		mode = TIMETRANSITION;
		// launch all constructors
		elementLocation = location;
		initiateContainers();
		// set mode
		element = transition;
		// getting the data
		// ID
		headers.add(new JLabel("IDictator:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(transition.getID())));
		// name
		headers.add(new JLabel("Name:", JLabel.TRAILING));
		JFormattedTextField nameField = new JFormattedTextField();
		nameField.setValue(transition.getName());
		nameField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
						try {
							field.commitEdit();
						} catch (ParseException ex) {
						}
						String newName = (String) field.getValue();
						changeName(newName);
					}
				});
		values.add(nameField);
		// comment
		JFormattedTextField commentField = new JFormattedTextField();
		commentField.setValue(transition.getComment());
		commentField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
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

		headers.add(new JLabel("Min / Max time:", JLabel.TRAILING));
		/*
		SpinnerModel minTimeSpinnerModel = new SpinnerNumberModel(
				transition.getMinFireTime(), 0, Integer.MAX_VALUE, 1);
		JSpinner minFireTimeSpinner = new JSpinner(minTimeSpinnerModel);
		minFireTimeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				double min = (double) spinner.getValue();
				System.out.println(min);
				//transition.setMinFireTime(min);
				setMinFireTime(min);
				;
			}
		});*/
		
		JFormattedTextField minTimeField = new JFormattedTextField();
		minTimeField.setValue(transition.getMinFireTime());
		minTimeField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
					public void propertyChange(PropertyChangeEvent e) {
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
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
		maxTimeField.addPropertyChangeListener("value",
				new PropertyChangeListener() {
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
		
/*
		SpinnerModel maxTimeSpinnerModel = new SpinnerNumberModel(
				transition.getMaxFireTime(), 0, Integer.MAX_VALUE, 1);
		JSpinner maxFireTimeSpinner = new JSpinner(maxTimeSpinnerModel);
		maxFireTimeSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSpinner spinner = (JSpinner) e.getSource();
				int min = (int) spinner.getValue();
				transition.setMaxFireTime(min);
				;
			}
		});*/

		JPanel minTimeSpinnerPanel = new JPanel();
		minTimeSpinnerPanel.setLayout(new BoxLayout(minTimeSpinnerPanel,
				BoxLayout.X_AXIS));
		minTimeSpinnerPanel.add(minTimeField);
		minTimeSpinnerPanel.add(new JLabel(" / "));
		minTimeSpinnerPanel.add(maxTimeField);
		values.add(minTimeSpinnerPanel);

		// location
		headers.add(new JLabel("Sheet:", JLabel.TRAILING));
		values.add(new JLabel(Integer.toString(location.getSheetID())));
		headers.add(new JLabel("Location:", JLabel.TRAILING));

		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(
				location.getSheetID());
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
		// is Portal
		headers.add(new JLabel("Portal:", JLabel.TRAILING));
		JCheckBox portalBox = new JCheckBox("", transition.isPortal());
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
		values.add(portalBox);
		// put all contents on the pane
		putContents(panel);
	}

	// Arc constructor
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
						JFormattedTextField field = (JFormattedTextField) e
								.getSource();
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
		SpinnerModel weightSpinnerModel = new SpinnerNumberModel(
				arc.getWeight(), 0, Integer.MAX_VALUE, 1);
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
				.getPosition().x)
				+ ", "
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
				.getPosition().x)
				+ ", "
				+ Integer.toString(arc.getEndLocation().getPosition().y)));
		// put all contents on the pane
		putContents(panel);
	}

	// Sheet constructor
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
		JCheckBox autoscrollBox = new JCheckBox("", sheet.getGraphPanel()
				.isAutoDragScroll());
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

	public PropertiesTable(ArrayList<ArrayList<InvariantTransition>> invariants) {
		if (invariants.size() > 0) {
			initiateContainers();
			// set mode
			mode = EXTERNAL_ANALYSIS;
			externalInvariants = invariants;
			// getting the data
			JLabel chooseInvariantLabel = new JLabel("Choose invariant: ");
			chooseInvariantLabel.setMaximumSize(chooseInvariantLabel
					.getMinimumSize());
			headers.add(chooseInvariantLabel);
			String[] invariantHeaders = new String[invariants.size() + 1];
			invariantHeaders[0] = "---";
			for (int i = 0; i < invariants.size(); i++)
				invariantHeaders[i + 1] = "Invariant no. "
						+ Integer.toString(i);
			@SuppressWarnings({ "rawtypes", "unchecked" })
			JComboBox chooseInvariantBox = new JComboBox(invariantHeaders);
			chooseInvariantBox.setMaximumSize(chooseInvariantBox
					.getMinimumSize());
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

	// konstruktor Net properties

	public PropertiesTable(ArrayList<ArrayList<Object>> prop, boolean ref) {
		initiateContainers();
		JPanel rowPanel = new JPanel();
		rowPanel.setLayout(new BoxLayout(rowPanel,
				BoxLayout.X_AXIS));
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
				rowPanel.setLayout(new BoxLayout(rowPanel,
						BoxLayout.X_AXIS));
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
			rowPanel.setLayout(new BoxLayout(rowPanel,
					BoxLayout.X_AXIS));
			row = new ArrayList<Object>();
		}
		values.add(new JLabel());

		putContents(panel);
	}
	
	//Konstruktor Symulatora Invariantów
	public PropertiesTable(InvariantsSimulator is)
	{
		initiateContainers();
		// set mode
		mode = INVARIANTSSIMULATOR;
		invSimulator = is;
		
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
						JOptionPane.showMessageDialog(new JFrame(),
							    "There is no invariants to simulate",
							    "Inane warning",
							    JOptionPane.WARNING_MESSAGE);
					}
				}
			});
		 headers.add(start);
		 values.add(new JLabel(""));
		 putContents(panel);
	}

	// konstruktor MCT
	public PropertiesTable(ArrayList<ArrayList<Transition>> mct,
			Properties.PropertiesType type) {
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
					showMct(comboBox.getSelectedIndex()-1);
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

	private void showMct(Integer mctIndex) {
		PetriNet net = GUIManager.getDefaultGUIManager().getWorkspace()
				.getProject();
		net.turnTransitionGlowingOff();
		net.setTransitionGlowedMTC(false);
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
	
	private void showMct(Integer mctIndex, boolean mc) {
		PetriNet net = GUIManager.getDefaultGUIManager().getWorkspace()
				.getProject();
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
	 * Pokazywanie inwariantów
	 * @param invariantIndex
	 * @param inv
	 */
	private void showInvariant(Integer invariantIndex, boolean inv) {
		PetriNet net = GUIManager.getDefaultGUIManager().getWorkspace()
				.getProject();
		net.turnTransitionGlowingOff(); //!!!!!
		headers.clear();
		values.clear();
		invariantPanel.removeAll();
		ArrayList<InvariantTransition> invariant = externalInvariants
				.get(invariantIndex);
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
	
	private void initiateContainers() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		headerSize = new Dimension(300, 30);
		headers = new ArrayList<JComponent>();
		values = new ArrayList<JComponent>();
		panel = new JPanel();
		invariantPanel = new JPanel();
		mainPanel = this;
	}

	private void putContents(JPanel contentPanel) {
		// setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		for (JComponent component : headers)
			component.setMaximumSize(component.getMinimumSize());
		for (JComponent component : values)
			component.setMaximumSize(component.getMinimumSize());
		contentPanel.setLayout(new SpringLayout());
		for (int i = 0; i < headers.size(); i++) {
			contentPanel.add(headers.get(i));
			contentPanel.add(values.get(i));
		}
		SpringUtilities.makeCompactGrid(contentPanel, headers.size(), 2, 5, 5,
				5, 5);
		contentPanel.setOpaque(true);
		add(contentPanel);
	}

	public JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor) editor).getTextField();
		} else {
			System.err.println("Unexpected editor type: "
					+ spinner.getEditor().getClass()
					+ " isn't a descendant of DefaultEditor");
			return null;
		}
	}

	// general operations
	private void repaintGraphPanel() {
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(
				elementLocation.getSheetID());
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

	// general petri net element operations (all modes)
	private void changeComment(String newComment) {
		element.setComment(newComment);
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

	private void changeName(String newName) {
		if (mode == PLACE || mode == TRANSITION || mode == TIMETRANSITION) {
			Node node = (Node) element;
			node.setName(newName);
			repaintGraphPanel();
		}
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

	public void updateSimulatorProperties() {
		// TODO Auto-generated method stub
	}
}
