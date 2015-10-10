package holmes.windows.ssim;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import holmes.darkgui.GUIManager;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.petrinet.simulators.NetSimulator.NetType;
import holmes.utilities.Tools;
import holmes.windows.managers.HolmesSPNmanager;
import holmes.windows.managers.HolmesSSAwindowManager;
import holmes.windows.managers.HolmesStatesManager;

/**
 * Okno ustawień symulatorów programu. Pozwala ustawić parametry symulacji w globalnym obiekcie
 * SimulatorGlobals w ramach GUIManager, z którego pól korzystają symulatory.
 * 
 * @author MR
 */
public class HolmesSimSetup extends JFrame {
	private static final long serialVersionUID = -240275069200534886L;
	private GUIManager overlord;
	private JFrame parentWindow;
	private JFrame ego;
	private boolean doNotUpdate = false;
	private SimulatorGlobals settings;
	
	//settings:
	private ButtonGroup groupNetType = new ButtonGroup();
	private JRadioButton classPNRadioButton;
	private JRadioButton timeNetRadioButtion;
	private JRadioButton hybridNetRadioButton;
	
	private ButtonGroup groupSimMode = new ButtonGroup();
	private JRadioButton fiftyModeRadioButton;
	private JRadioButton maxModeRadioButton;
	private JRadioButton singleModeRadioButton;
	
	//SPN immediate
	private ButtonGroup immSPNmode = new ButtonGroup();
	private JRadioButton immOnly1RadioButton;
	private JRadioButton immSchedRadioButton;
	private JRadioButton immProbRadioButton;
	
	private JCheckBox allowEmptySteps;
	private JCheckBox useMassActionKinetics;
	private JCheckBox detRemoveMode;
	private JComboBox<String> generatorType;
	private JComboBox<String> simulatorType;

	/**
	 * Konstruktor okna ustawień symulatorów.
	 * @param parent JFrame - okno wywołujące
	 */
	public HolmesSimSetup(JFrame parent) {
		this.overlord = GUIManager.getDefaultGUIManager();
		this.ego = this;
		this.parentWindow = parent;
		this.settings = overlord.simSettings;
		
		parentWindow.setEnabled(false);
		
		initializeComponents();
		initiateListeners();
	}
	
	/**
	 * Metoda pomocnica konstuktora, odpowiada za utworzenie elementów graficznych okna.
	 */
	private void initializeComponents() {
		setTitle("Simulator settings");
		setLocation(parentWindow.getX()+150, parentWindow.getY()+150);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception e ) {}
		setSize(new Dimension(620, 490));
		
		JPanel main = new JPanel(null); //główny panel okna
		add(main);
		
		main.add(createGlobalOptionsPanel(0, 0, 600, 110));
		main.add(createStdSimulatorSettingsPanel(0, 110, 600, 110));
		main.add(createStochasticSimSettingsPanel(0, 220, 600, 160));
		main.add(createGillespieSSASimSettingsPanel(0, 380, 600, 70));

		setVisible(true);
	}
	
	/**
	 * Tworzy panel opcji ogólnych symulatorów.
	 * @param x int - pozycja x panelu
	 * @param y int - pozycja y panelu
	 * @param width int - szerokość preferowana
	 * @param height int - wysokość preferowana
	 * @return JPanel - panel, okrętu się pan spodziewałeś?
	 */
	private JPanel createGlobalOptionsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder("Global settings"));
		
		int posX = 10;
		int posY = 15;
		
		JLabel simStepsLabel = new JLabel("Steps:");
		simStepsLabel.setBounds(posX, posY, 80, 20);
		panel.add(simStepsLabel);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(settings.getSimSteps(), 100, 10000000, 100);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(posX, posY+20, 80, 20);
		simStepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int value = (int) spinner.getValue();
				settings.setSimSteps(value);
				
				if(parentWindow instanceof HolmesSim) {
					((HolmesSim)parentWindow).updateIntervalSpinner();
				}
			}
		});
		panel.add(simStepsSpinner);

		JLabel repetLabel = new JLabel("Repetitions:");
		repetLabel.setBounds(posX+85, posY, 80, 20);
		panel.add(repetLabel);
		
		SpinnerModel simRepetsSpinnerModel = new SpinnerNumberModel(settings.getRepetitions(), 1, 100000, 10);
		JSpinner simRepsSpinner = new JSpinner(simRepetsSpinnerModel);
		simRepsSpinner.setBounds(posX+85, posY+20, 80, 20);
		simRepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int value = (int) spinner.getValue();
				settings.setRepetitions(value);
			}
		});
		panel.add(simRepsSpinner);

		JLabel generatorLabel = new JLabel("Random number generator:");
		generatorLabel.setBounds(posX+170, posY, 180, 20);
		panel.add(generatorLabel);
		
		String[] simulator = {"Random (Java default)", "HighQualityRandom (slower)"};
		generatorType = new JComboBox<String>(simulator);
		generatorType.setBounds(posX+170, posY+20, 200, 20);
		generatorType.setSelectedIndex(0);
		generatorType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;

				int selected = generatorType.getSelectedIndex();
				if(selected == 1) {
					overlord.simSettings.setGeneratorType(1);
				} else {
					overlord.simSettings.setGeneratorType(0);
				}
				
				doNotUpdate = false;
			}
		});
		panel.add(generatorType);
		
		JButton stateManagerButton = new JButton();
	    stateManagerButton.setText("<html>States<br>Manager</html>");
	    stateManagerButton.setIcon(Tools.getResIcon32("/icons/stateManager/stManIcon.png"));
	    stateManagerButton.setBounds(posX+380, posY, 130, 40);
	    stateManagerButton.setMargin(new Insets(0, 0, 0, 0));
	    stateManagerButton.setFocusPainted(false);
	    stateManagerButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				new HolmesStatesManager();
			}
		});
	    panel.add(stateManagerButton);
		
		JLabel simulatorLabel = new JLabel("Simulator selection:");
		simulatorLabel.setBounds(posX, posY+40, 180, 20);
		panel.add(simulatorLabel);
		
		String[] simulatorName = {"Standard token simulator", "Stochastics simulation for SPN", 
				"Gillespie SSA (exact version)", "Gillespie SSA (fast version)"};
		simulatorType = new JComboBox<String>(simulatorName);
		simulatorType.setBounds(posX, posY+60, 250, 20);
		simulatorType.setSelectedIndex(0);
		simulatorType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				int selected = simulatorType.getSelectedIndex();
				if(selected == 0) {
					overlord.simSettings.setSimulatorType(0);
				} else if(selected == 1) {
					overlord.simSettings.setSimulatorType(1);
				} else {
					JOptionPane.showMessageDialog(ego, "This feature is not yet implemented.", 
							"Simulator unavailable", JOptionPane.INFORMATION_MESSAGE);
				}
				
				doNotUpdate = false;
			}
		});
		panel.add(simulatorType);
		
		return panel;
	}
	
	/**
	 * Tworzy panel opcji symulatora standardowego.
	 * @param x int - pozycja x panelu
	 * @param y int - pozycja y panelu
	 * @param width int - szerokość preferowana
	 * @param height int - wysokość preferowana
	 * @return JPanel - panel
	 */
	private JPanel createStdSimulatorSettingsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder("Standard simulator settings"));
		
		int posX = 10;
		int posY = 15;

		
		//NET TYPE MODE:
		JPanel netTypeModePanel = new JPanel(null);
		netTypeModePanel.setBounds(posX, posY, 150, 90);
		netTypeModePanel.setBorder(BorderFactory.createTitledBorder("Net type mode:"));
		panel.add(netTypeModePanel);
		
		classPNRadioButton = new JRadioButton("Classical Petri Net");
		classPNRadioButton.setBounds(5, 20, 130, 20);
		classPNRadioButton.setActionCommand("0");
		classPNRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
					settings.setNetType(0);
				}
			}
		});
		netTypeModePanel.add(classPNRadioButton);
		groupNetType.add(classPNRadioButton);

		timeNetRadioButtion = new JRadioButton("Time(d) Petri Net");
		timeNetRadioButtion.setBounds(5, 40, 130, 20);
		timeNetRadioButtion.setActionCommand("1");
		timeNetRadioButtion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
					//doNotUpdate = true;
					int res = settings.setNetType(1);
					
					if(res == 0) {
						groupNetType.setSelected(classPNRadioButton.getModel(), true);
					} else if(res == 2) {
						groupNetType.setSelected(hybridNetRadioButton.getModel(), true);
					}		
					
					doNotUpdate = false;
				}
			}
		});
		netTypeModePanel.add(timeNetRadioButtion);
		groupNetType.add(timeNetRadioButtion);
		
		hybridNetRadioButton = new JRadioButton("Hybrid Net");
		hybridNetRadioButton.setBounds(5, 60, 130, 20);
		hybridNetRadioButton.setActionCommand("2");
		hybridNetRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
					settings.setNetType(2);
				}
			}
		});
		netTypeModePanel.add(hybridNetRadioButton);
		groupNetType.add(hybridNetRadioButton);
		
		
		//SUB-MODE:
		JPanel subModeModePanel = new JPanel(null);
		subModeModePanel.setBounds(posX+150, posY, 170, 90);
		subModeModePanel.setBorder(BorderFactory.createTitledBorder("Simulator sub-mode:"));
		panel.add(subModeModePanel);
		
		fiftyModeRadioButton = new JRadioButton("50/50 mode (async.)");
		fiftyModeRadioButton.setBounds(5, 20, 140, 20);
		fiftyModeRadioButton.setActionCommand("0");
		fiftyModeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					settings.setSingleMode(false);
					settings.setMaxMode(false);
				}
			}
		});
		subModeModePanel.add(fiftyModeRadioButton);
		groupSimMode.add(fiftyModeRadioButton);

		maxModeRadioButton = new JRadioButton("Maximum mode (sync.)");
		maxModeRadioButton.setBounds(5, 40, 160, 20);
		maxModeRadioButton.setActionCommand("1");
		maxModeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					settings.setSingleMode(false);
					settings.setMaxMode(true);
				}
			}
		});
		subModeModePanel.add(maxModeRadioButton);
		groupSimMode.add(maxModeRadioButton);
		
		singleModeRadioButton = new JRadioButton("Single fire mode");
		singleModeRadioButton.setBounds(5, 60, 140, 20);
		singleModeRadioButton.setActionCommand("2");
		singleModeRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					settings.setSingleMode(true);
				}
			}
		});
		subModeModePanel.add(singleModeRadioButton);
		groupSimMode.add(singleModeRadioButton);

		
		allowEmptySteps = new JCheckBox("Allow empty steps");
		allowEmptySteps.setBounds(posX+325, posY, 150, 20);
		allowEmptySteps.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;

				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					settings.setEmptySteps(true);
				} else {
					settings.setEmptySteps(false);
				}
			}
		});
		panel.add(allowEmptySteps);
		
		return panel;
	}

	/**
	 * Tworzy panel opcji symulatora SPN.
	 * @param x int - pozycja x panelu
	 * @param y int - pozycja y panelu
	 * @param width int - szerokość preferowana
	 * @param height int - wysokość preferowana
	 * @return JPanel - panel
	 */
	private JPanel createStochasticSimSettingsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder("Stochastic simulation settings for SPN"));
		
		int posX = 10;
		int posY = 20;
		
		JButton createFRWindowButton = new JButton("<html>Fire rate<br>Manager</html>");
		createFRWindowButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/fireRateIcon.png"));
		createFRWindowButton.setBounds(posX, posY, 120, 40);
		createFRWindowButton.setFocusPainted(false);
		createFRWindowButton.setToolTipText("Loop single transition simulation");
		createFRWindowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				new HolmesSPNmanager(ego);
			}
		});
		panel.add(createFRWindowButton);
		
		useMassActionKinetics = new JCheckBox("Mass action kinetics enabled");
		useMassActionKinetics.setBounds(posX+130, posY, 200, 20);
		useMassActionKinetics.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;

				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					settings.setSSAmassAction(true);
				} else {
					settings.setSSAmassAction(false);
				}
			}
		});
		panel.add(useMassActionKinetics);
		
		detRemoveMode = new JCheckBox("Remove deactivated deterministic SPN transitions");
		detRemoveMode.setToolTipText("When this mode is selected, deterministic SPN transition which after its delay time\n"
									+ "is no longer active, will be remove from the firing sequence. Otherwise, it will fire\n"
									+ "as soon as it become activated again = will act as immediate in that moment.");
		detRemoveMode.setBounds(posX+130, posY+20, 350, 20);
		detRemoveMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;

				AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
				if (abstractButton.getModel().isSelected()) {
					settings.setSPNdetRemoveMode(true);
				} else {
					settings.setSPNdetRemoveMode(false);
				}
			}
		});
		panel.add(detRemoveMode);
		
		
		JPanel immSPNModePanel = new JPanel(null);
		immSPNModePanel.setBounds(posX, posY+45, 240, 90);
		immSPNModePanel.setBorder(BorderFactory.createTitledBorder("Immediate-transition SPN mode:"));
		panel.add(immSPNModePanel);
		
		immOnly1RadioButton = new JRadioButton("Only 1 with the highest priority fire");
		immOnly1RadioButton.setBounds(5, 20, 220, 20);
		immOnly1RadioButton.setToolTipText("Only 1 transition with the highest priority will fire, then all will be evaluated from the beginning.");
		immOnly1RadioButton.setActionCommand("0");
		immOnly1RadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
					
					settings.setSPNimmediateMode(0);
				}
			}
		});
		immSPNModePanel.add(immOnly1RadioButton);
		groupNetType.add(immOnly1RadioButton);

		immSchedRadioButton = new JRadioButton("Sequenced firing by priority");
		immSchedRadioButton.setBounds(5, 40, 220, 20);
		immOnly1RadioButton.setToolTipText("Sequenced firing of active immediate transition by priority.");
		immSchedRadioButton.setActionCommand("1");
		immSchedRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
					
					settings.setSPNimmediateMode(1);
				}
			}
		});
		immSPNModePanel.add(immSchedRadioButton);
		groupNetType.add(immSchedRadioButton);
		
		immProbRadioButton = new JRadioButton("Priority - probability mode");
		immProbRadioButton.setBounds(5, 60, 220, 20);
		immProbRadioButton.setToolTipText("1 from active IM transition will fire, probability depends on priority set for transition.");
		immProbRadioButton.setActionCommand("2");
		immProbRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
					
					settings.setSPNimmediateMode(2);
				}
			}
		});
		immSPNModePanel.add(immProbRadioButton);
		groupNetType.add(immProbRadioButton);
		
		return panel;
	}
	/**
	 * Tworzy panel opcji symulatora stochastycznego.
	 * @param x int - pozycja x panelu
	 * @param y int - pozycja y panelu
	 * @param width int - szerokość preferowana
	 * @param height int - wysokość preferowana
	 * @return JPanel - panel
	 */
	private JPanel createGillespieSSASimSettingsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder("Gillespie SSA (Stochastic Simulation Algorithm) settings"));
		
		int posX = 10;
		int posY = 20;
		
		JButton createFRWindowButton = new JButton("<html>Fire rate<br>Manager</html>");
		createFRWindowButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/fireRateIcon.png"));
		createFRWindowButton.setBounds(posX, posY, 120, 40);
		createFRWindowButton.setFocusPainted(false);
		createFRWindowButton.setToolTipText("Set transition firing rates for SSA.");
		createFRWindowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				new HolmesSPNmanager(ego);
			}
		});
		panel.add(createFRWindowButton);
		
		JButton createCompoundsEditorWindowButton = new JButton("<html>Components<br>&nbsp;&nbsp;Manager&nbsp;</html>");
		createCompoundsEditorWindowButton.setIcon(Tools.getResIcon16("/icons/componentsManager/compIcon.png"));
		createCompoundsEditorWindowButton.setMargin(new Insets(0, 0, 0, 0));
		createCompoundsEditorWindowButton.setFocusPainted(false);
		createCompoundsEditorWindowButton.setBounds(posX+130, posY, 120, 40);
		createCompoundsEditorWindowButton.setToolTipText("Component vectors managaer for SSA.");
		createCompoundsEditorWindowButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				new HolmesSSAwindowManager(ego);
			}
		});
		panel.add(createCompoundsEditorWindowButton);
		
		return panel;
	}
	
	/**
	 * Wywoływana zdalnie metoda ustawiająca odpowiednie wartości parametrów okna.
	 */
	private void configureWindow() {
		doNotUpdate = true;
		
		NetType netType = settings.getNetType();
		if(netType == NetType.BASIC)
			groupNetType.setSelected(classPNRadioButton.getModel(), true);
		else if(netType == NetType.TIME)
			groupNetType.setSelected(timeNetRadioButtion.getModel(), true);
		else if(netType == NetType.HYBRID)
			groupNetType.setSelected(hybridNetRadioButton.getModel(), true);
		
		
		if(settings.isSingleMode())
			groupSimMode.setSelected(singleModeRadioButton.getModel(), true);
		else if(settings.isMaxMode())
			groupSimMode.setSelected(maxModeRadioButton.getModel(), true);
		else //50/50:
			groupSimMode.setSelected(fiftyModeRadioButton.getModel(), true);
		
		if(settings.isEmptySteps())
			allowEmptySteps.setSelected(true);
		else
			allowEmptySteps.setSelected(false);
		
		if(settings.isSSAMassAction())
			useMassActionKinetics.setSelected(true);
		else
			useMassActionKinetics.setSelected(false);
		
		if(settings.isSPNdetRemoveMode())
			detRemoveMode.setSelected(true);
		else
			detRemoveMode.setSelected(false);
		
		if(settings.getGeneratorType() == 0)
			generatorType.setSelectedIndex(0);
		else
			generatorType.setSelectedIndex(1);
		
		if(settings.getSimulatorType() == 0)
			simulatorType.setSelectedIndex(0);
		else
			simulatorType.setSelectedIndex(1);
		
		int imSPNmode = settings.getSPNimmediateMode();
		if(imSPNmode == 0)
			immSPNmode.setSelected(immOnly1RadioButton.getModel(), true);
		else if(imSPNmode == 1)
			immSPNmode.setSelected(immSchedRadioButton.getModel(), true);
		else if(imSPNmode == 2)
			immSPNmode.setSelected(immProbRadioButton.getModel(), true);
		
		doNotUpdate = false;
	}

	/**
	 * Inicjalizacja agentów nasłuchujących różnych zdarzeń dla okna poszukiwania.
	 */
    private void initiateListeners() { //HAIL SITHIS
    	addWindowListener(new WindowAdapter() {
  	  	    public void windowActivated(WindowEvent e) {
  	  	    	configureWindow();
  	  	    }  
    	});
    	
    	addWindowListener(new java.awt.event.WindowAdapter() {
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    	parentWindow.setEnabled(true);
		    }
		});
    }
}
