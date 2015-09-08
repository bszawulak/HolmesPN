package holmes.windows.ssim;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	
	private JCheckBox allowEmptySteps;

	/**
	 * Konstruktor okna ustawień symulatorów.
	 * @param parent JFrame - okno wywołujące
	 */
	public HolmesSimSetup(JFrame parent) {
		this.overlord = GUIManager.getDefaultGUIManager();
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
		setLocation(30,30);
    	try {
    		setIconImage(Tools.getImageFromIcon("/icons/blackhole.png"));
		} catch (Exception e ) {}
		setSize(new Dimension(600, 480));
		
		JPanel main = new JPanel(null); //główny panel okna
		add(main);
		
		main.add(createStdSimulatorSettingsPanel());
		main.add(createStochasticSimSettingsPanel());

		setVisible(true);
	}
	
	/**
	 * Tworzy panel opcji symulatora standardowego.
	 * @return JPanel - panel
	 */
	private JPanel createStdSimulatorSettingsPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 0, 600, 110);
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
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
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
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
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
				AbstractButton aButton = (AbstractButton) actionEvent.getSource();
				if(aButton.isSelected() == true) {
					if(doNotUpdate)
						return;
					settings.setSingleMode(true);
				}
			}
		});
		subModeModePanel.add(singleModeRadioButton);
		groupSimMode.add(singleModeRadioButton);

		JLabel simStepsLabel = new JLabel("Steps:");
		simStepsLabel.setBounds(posX+325, posY, 80, 20);
		panel.add(simStepsLabel);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(settings.getSimSteps(), 100, 10000000, 100);
		JSpinner simStepsSpinner = new JSpinner(simStepsSpinnerModel);
		simStepsSpinner.setBounds(posX+325, posY+20, 80, 20);
		simStepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int value = (int) spinner.getValue();
				settings.setSimSteps(value);
			}
		});
		panel.add(simStepsSpinner);

		JLabel repetLabel = new JLabel("Repetitions:");
		repetLabel.setBounds(posX+410, posY, 80, 20);
		panel.add(repetLabel);
		
		SpinnerModel simRepetsSpinnerModel = new SpinnerNumberModel(settings.getRepetitions(), 1, 100000, 10);
		JSpinner simRepsSpinner = new JSpinner(simRepetsSpinnerModel);
		simRepsSpinner.setBounds(posX+410, posY+20, 80, 20);
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
		
		allowEmptySteps = new JCheckBox("Allow empty steps");
		allowEmptySteps.setBounds(posX+325, posY+40, 150, 20);
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
	 * Tworzy panel opcji symulatora stochastycznego.
	 * @return JPanel - panel
	 */
	private JPanel createStochasticSimSettingsPanel() {
		JPanel panel = new JPanel(null);
		panel.setBounds(0, 150, 600, 300);
		panel.setBorder(BorderFactory.createTitledBorder("Stochastic simulator settings"));
		
		int posX = 10;
		int posY = 10;
		
		
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
