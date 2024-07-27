package holmes.windows.ssim;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;

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

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.simulators.SimulatorGlobals;
import holmes.utilities.Tools;
import holmes.windows.managers.HolmesSPNmanager;
import holmes.windows.managers.HolmesSSAwindowManager;
import holmes.windows.managers.HolmesStatesManager;

/**
 * Okno ustawień symulatorów programu. Pozwala ustawić parametry symulacji w globalnym obiekcie
 * SimulatorGlobals w ramach GUIManager, z którego pól korzystają symulatory.
 */
public class HolmesSimSetup extends JFrame {
	@Serial
	private static final long serialVersionUID = -240275069200534886L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
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
		setTitle(lang.getText("HSSetwin_entry001title")); //Simulator settings
		setLocation(parentWindow.getX()+150, parentWindow.getY()+150);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/holmesicon.png"));
		} catch (Exception ex) {
			overlord.log(lang.getText("LOGentry00566exception")+"\n"+ex.getMessage(), "error", true);
		}
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createGlobalOptionsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSetwin_entry002"))); //Global options
		
		int posX = 10;
		int posY = 15;
		
		JLabel simStepsLabel = new JLabel(lang.getText("HSSetwin_entry003")); //Steps
		simStepsLabel.setBounds(posX, posY, 80, 20);
		panel.add(simStepsLabel);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(settings.getSimSteps(), 100, 10000000, 100);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(posX, posY+20, 80, 20);
		simStepsSpinner.addChangeListener(e -> {
			if(doNotUpdate)
				return;

			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			settings.setSimSteps(value);

			if(parentWindow instanceof HolmesSim) {
				((HolmesSim)parentWindow).updateIntervalSpinner();
			}
		});
		panel.add(simStepsSpinner);

		JLabel repetLabel = new JLabel(lang.getText("HSSetwin_entry004")); //Repetitions
		repetLabel.setBounds(posX+85, posY, 80, 20);
		panel.add(repetLabel);
		
		SpinnerModel simRepetsSpinnerModel = new SpinnerNumberModel(settings.getRepetitions(), 1, 100000, 10);
		JSpinner simRepsSpinner = new JSpinner(simRepetsSpinnerModel);
		simRepsSpinner.setBounds(posX+85, posY+20, 80, 20);
		simRepsSpinner.addChangeListener(e -> {
			if(doNotUpdate)
				return;

			JSpinner spinner = (JSpinner) e.getSource();
			int value = (int) spinner.getValue();
			settings.setRepetitions(value);
		});
		panel.add(simRepsSpinner);

		JLabel generatorLabel = new JLabel(lang.getText("HSSetwin_entry005")); //Random number generator
		generatorLabel.setBounds(posX+170, posY, 180, 20);
		panel.add(generatorLabel);
		
		String[] simulator = {lang.getText("HSSetwin_entry006op1"), lang.getText("HSSetwin_entry006op2")}; //Random (Java default), HighQualityRandom (slower)
		generatorType = new JComboBox<String>(simulator);
		generatorType.setBounds(posX+170, posY+20, 200, 20);
		generatorType.setSelectedIndex(0);
		generatorType.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			int selected = generatorType.getSelectedIndex();
			if(selected == 1) {
				overlord.simSettings.setGeneratorType(1);
			} else {
				overlord.simSettings.setGeneratorType(0);
			}

			doNotUpdate = false;
		});
		panel.add(generatorType);
		
		JButton stateManagerButton = new JButton();
	    stateManagerButton.setText(lang.getText("HSSetwin_entry006")); //States Manager
	    stateManagerButton.setIcon(Tools.getResIcon32("/icons/stateManager/stManIcon.png"));
	    stateManagerButton.setBounds(posX+380, posY, 130, 40);
	    stateManagerButton.setMargin(new Insets(0, 0, 0, 0));
	    stateManagerButton.setFocusPainted(false);
	    stateManagerButton.addActionListener(actionEvent -> new HolmesStatesManager());
	    panel.add(stateManagerButton);
		
		JLabel simulatorLabel = new JLabel(lang.getText("HSSetwin_entry007")); //Simulator selection
		simulatorLabel.setBounds(posX, posY+40, 180, 20);
		panel.add(simulatorLabel);
		
		String[] simulatorName = {lang.getText("HSSetwin_entry008op1"), lang.getText("HSSetwin_entry008op2"),
				lang.getText("HSSetwin_entry008op3"), lang.getText("HSSetwin_entry008op4")}; //Standard token simulator, Stochastics simulation for SPN, Gillespie SSA (test version), Gillespie SSA (fast version)
		simulatorType = new JComboBox<String>(simulatorName);
		simulatorType.setBounds(posX, posY+60, 250, 20);
		simulatorType.setSelectedIndex(0);
		simulatorType.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			int selected = simulatorType.getSelectedIndex();
			if(selected == 0) {
				overlord.simSettings.setSimulatorType(0);
			} else if(selected == 1) {
				overlord.simSettings.setSimulatorType(1);
			} else if(selected == 2) {
				overlord.simSettings.setSimulatorType(2);
			} else {
				JOptionPane.showMessageDialog(ego, lang.getText("HSSetwin_entry009"),
						lang.getText("unimplementedTitle"), JOptionPane.INFORMATION_MESSAGE);
			}
			doNotUpdate = false;
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createStdSimulatorSettingsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSetwin_entry010"))); //Standard simulator settings
		
		int posX = 10;
		int posY = 15;

		
		//NET TYPE MODE:
		JPanel netTypeModePanel = new JPanel(null);
		netTypeModePanel.setBounds(posX, posY, 150, 90);
		netTypeModePanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSetwin_entry011"))); //Net type mode
		panel.add(netTypeModePanel);
		
		classPNRadioButton = new JRadioButton(lang.getText("HSSetwin_entry012")); //Classical Petri Net
		classPNRadioButton.setBounds(5, 20, 130, 20);
		classPNRadioButton.setActionCommand("0");
		classPNRadioButton.addActionListener(actionEvent -> {
			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				if(doNotUpdate)
					return;
				settings.setNetType(0);
			}
		});
		netTypeModePanel.add(classPNRadioButton);
		groupNetType.add(classPNRadioButton);

		timeNetRadioButtion = new JRadioButton(lang.getText("HSSetwin_entry013")); //Time(d) Petri Net
		timeNetRadioButtion.setBounds(5, 40, 130, 20);
		timeNetRadioButtion.setActionCommand("1");
		timeNetRadioButtion.addActionListener(actionEvent -> {
			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
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
		});
		netTypeModePanel.add(timeNetRadioButtion);
		groupNetType.add(timeNetRadioButtion);
		
		hybridNetRadioButton = new JRadioButton(lang.getText("HSSetwin_entry014")); //Hybrid Net
		hybridNetRadioButton.setBounds(5, 60, 130, 20);
		hybridNetRadioButton.setActionCommand("2");
		hybridNetRadioButton.addActionListener(actionEvent -> {
			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				if(doNotUpdate)
					return;
				settings.setNetType(2);
			}
		});
		netTypeModePanel.add(hybridNetRadioButton);
		groupNetType.add(hybridNetRadioButton);
		
		
		//SUB-MODE:
		JPanel subModeModePanel = new JPanel(null);
		subModeModePanel.setBounds(posX+150, posY, 170, 90);
		subModeModePanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSetwin_entry015"))); //Simulator sub-mode
		panel.add(subModeModePanel);
		
		fiftyModeRadioButton = new JRadioButton(lang.getText("HSSetwin_entry016")); //50/50 mode (async.)
		fiftyModeRadioButton.setBounds(5, 20, 140, 20);
		fiftyModeRadioButton.setActionCommand("0");
		fiftyModeRadioButton.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				settings.setSingleMode(false);
				settings.setMaxMode(false);
			}
		});
		subModeModePanel.add(fiftyModeRadioButton);
		groupSimMode.add(fiftyModeRadioButton);

		maxModeRadioButton = new JRadioButton(lang.getText("HSSetwin_entry017")); //Maximum mode (sync.)
		maxModeRadioButton.setBounds(5, 40, 160, 20);
		maxModeRadioButton.setActionCommand("1");
		maxModeRadioButton.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				settings.setSingleMode(false);
				settings.setMaxMode(true);
			}
		});
		subModeModePanel.add(maxModeRadioButton);
		groupSimMode.add(maxModeRadioButton);
		
		singleModeRadioButton = new JRadioButton(lang.getText("HSSetwin_entry018")); //Single fire mode
		singleModeRadioButton.setBounds(5, 60, 140, 20);
		singleModeRadioButton.setActionCommand("2");
		singleModeRadioButton.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				settings.setSingleMode(true);
			}
		});
		subModeModePanel.add(singleModeRadioButton);
		groupSimMode.add(singleModeRadioButton);

		
		allowEmptySteps = new JCheckBox(lang.getText("HSSetwin_entry019")); //Allow empty steps
		allowEmptySteps.setBounds(posX+325, posY, 150, 20);
		allowEmptySteps.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			settings.setEmptySteps(abstractButton.getModel().isSelected());
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createStochasticSimSettingsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSetwin_entry020"))); //Stochastic simulation settings for SPN
		
		int posX = 10;
		int posY = 20;
		
		JButton createFRWindowButton = new JButton(lang.getText("HSSetwin_entry021")); //Fire rate Manager
		createFRWindowButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/fireRateIcon.png"));
		createFRWindowButton.setBounds(posX, posY, 120, 40);
		createFRWindowButton.setFocusPainted(false);
		createFRWindowButton.setToolTipText(lang.getText("HSSetwin_entry021t"));
		createFRWindowButton.addActionListener(actionEvent -> new HolmesSPNmanager(ego));
		panel.add(createFRWindowButton);
		
		useMassActionKinetics = new JCheckBox(lang.getText("HSSetwin_entry022")); //Mass action kinetics enabled
		useMassActionKinetics.setBounds(posX+130, posY, 200, 20);
		useMassActionKinetics.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			settings.setSSAmassAction(abstractButton.getModel().isSelected());
		});
		panel.add(useMassActionKinetics);
		
		detRemoveMode = new JCheckBox(lang.getText("HSSetwin_entry023")); //Remove deactivated deterministic SPN transitions
		detRemoveMode.setToolTipText(lang.getText("HSSetwin_entry023t"));
		detRemoveMode.setBounds(posX+130, posY+20, 350, 20);
		detRemoveMode.addActionListener(actionEvent -> {
			if(doNotUpdate)
				return;

			AbstractButton abstractButton = (AbstractButton) actionEvent.getSource();
			settings.setSPNdetRemoveMode(abstractButton.getModel().isSelected());
		});
		panel.add(detRemoveMode);
		
		
		JPanel immSPNModePanel = new JPanel(null);
		immSPNModePanel.setBounds(posX, posY+45, 260, 90);
		immSPNModePanel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSetwin_entry024"))); //Immediate-transition SPN mode
		panel.add(immSPNModePanel);
		
		immOnly1RadioButton = new JRadioButton(lang.getText("HSSetwin_entry025")); //Only 1 with the highest priority fire
		immOnly1RadioButton.setBounds(5, 20, 240, 20);
		immOnly1RadioButton.setToolTipText(lang.getText("HSSetwin_entry025t"));
		immOnly1RadioButton.setActionCommand("0");
		immOnly1RadioButton.addActionListener(actionEvent -> {
			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				if(doNotUpdate)
					return;

				settings.setSPNimmediateMode(0);
			}
		});
		immSPNModePanel.add(immOnly1RadioButton);
		immSPNmode.add(immOnly1RadioButton);

		immSchedRadioButton = new JRadioButton(lang.getText("HSSetwin_entry026")); //Sequenced firing by priority
		immSchedRadioButton.setBounds(5, 40, 220, 20);
		immOnly1RadioButton.setToolTipText(lang.getText("HSSetwin_entry026t"));
		immSchedRadioButton.setActionCommand("1");
		immSchedRadioButton.addActionListener(actionEvent -> {
			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				if(doNotUpdate)
					return;

				settings.setSPNimmediateMode(1);
			}
		});
		immSPNModePanel.add(immSchedRadioButton);
		immSPNmode.add(immSchedRadioButton);
		
		immProbRadioButton = new JRadioButton(lang.getText("HSSetwin_entry027")); //Priority - probability mode
		immProbRadioButton.setBounds(5, 60, 220, 20);
		immProbRadioButton.setToolTipText(lang.getText("HSSetwin_entry027t"));
		immProbRadioButton.setActionCommand("2");
		immProbRadioButton.addActionListener(actionEvent -> {
			AbstractButton aButton = (AbstractButton) actionEvent.getSource();
			if(aButton.isSelected()) {
				if(doNotUpdate)
					return;

				settings.setSPNimmediateMode(2);
			}
		});
		immSPNModePanel.add(immProbRadioButton);
		immSPNmode.add(immProbRadioButton);
		
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
	@SuppressWarnings("SameParameterValue")
	private JPanel createGillespieSSASimSettingsPanel(int x, int y, int width, int height) {
		JPanel panel = new JPanel(null);
		panel.setBounds(x, y, width, height);
		panel.setBorder(BorderFactory.createTitledBorder(lang.getText("HSSetwin_entry028"))); //Gillespie SSA (Stochastic Simulation Algorithm) settings
		
		int posX = 10;
		int posY = 20;
		
		JButton createFRWindowButton = new JButton(lang.getText("HSSetwin_entry029")); //Fire rate Manager
		createFRWindowButton.setIcon(Tools.getResIcon16("/icons/fRatesManager/fireRateIcon.png"));
		createFRWindowButton.setBounds(posX, posY, 120, 40);
		createFRWindowButton.setFocusPainted(false);
		createFRWindowButton.setToolTipText(lang.getText("HSSetwin_entry029t"));
		createFRWindowButton.addActionListener(actionEvent -> new HolmesSPNmanager(ego));
		panel.add(createFRWindowButton);
		
		JButton createCompoundsEditorWindowButton = new JButton(lang.getText("HSSetwin_entry030")); 
		createCompoundsEditorWindowButton.setIcon(Tools.getResIcon16("/icons/componentsManager/compIcon.png"));
		createCompoundsEditorWindowButton.setMargin(new Insets(0, 0, 0, 0));
		createCompoundsEditorWindowButton.setFocusPainted(false);
		createCompoundsEditorWindowButton.setBounds(posX+130, posY, 120, 40);
		createCompoundsEditorWindowButton.setToolTipText(lang.getText("HSSetwin_entry030t"));
		createCompoundsEditorWindowButton.addActionListener(actionEvent -> new HolmesSSAwindowManager(ego));
		panel.add(createCompoundsEditorWindowButton);
		
		return panel;
	}
	
	/**
	 * Wywoływana zdalnie metoda ustawiająca odpowiednie wartości parametrów okna.
	 */
	private void configureWindow() {
		doNotUpdate = true;

		SimulatorGlobals.SimNetType netType = settings.getNetType();
		if(netType == SimulatorGlobals.SimNetType.BASIC)
			groupNetType.setSelected(classPNRadioButton.getModel(), true);
		else if(netType == SimulatorGlobals.SimNetType.TIME)
			groupNetType.setSelected(timeNetRadioButtion.getModel(), true);
		else if(netType == SimulatorGlobals.SimNetType.HYBRID)
			groupNetType.setSelected(hybridNetRadioButton.getModel(), true);
		
		if(settings.isSingleMode())
			groupSimMode.setSelected(singleModeRadioButton.getModel(), true);
		else if(settings.isMaxMode())
			groupSimMode.setSelected(maxModeRadioButton.getModel(), true);
		else //50/50:
			groupSimMode.setSelected(fiftyModeRadioButton.getModel(), true);

		allowEmptySteps.setSelected(settings.isEmptySteps());
		useMassActionKinetics.setSelected(settings.isSSAMassAction());
		detRemoveMode.setSelected(settings.isSPNdetRemoveMode());
		
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
