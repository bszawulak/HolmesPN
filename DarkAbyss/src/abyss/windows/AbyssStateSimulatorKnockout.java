package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import abyss.darkgui.GUIManager;
import abyss.petrinet.simulators.NetSimulator.NetType;
import abyss.utilities.Tools;

/**
 * Klasa odpowiedzialna za tworzenie podstrony knockoutSim.
 * 
 * @author MR
 */
public class AbyssStateSimulatorKnockout extends JPanel {
	private boolean doNotUpdate = false;
	private JSpinner refSimStepsSpinner;
	private int refSimSteps = 1000;			//liczba kroków dla zbioru referencyjnego
	private JComboBox<String> refSimNetMode;
	private NetType refNetType = NetType.BASIC;		//rodzaj sieci: BASIC, TIMED, HYBRID, itd.
	private JComboBox<String> refSimMode;
	private boolean refMaximumMode = false;
	private JProgressBar progressBar;
	
	/**
	 * Konstruktor obiektu klasy AbyssStateSimulatorKnockout.
	 */
	public AbyssStateSimulatorKnockout() {
		setLayout(new BorderLayout());
		
		add(getTopPanel(), BorderLayout.NORTH);
		add(getProgressPanel(), BorderLayout.LINE_START);
		
		add(new JPanel(), BorderLayout.CENTER);
	}
	
	/**
	 * Metoda tworząca panel tworzenia zbioru referencyjnego.
	 * @return JPanel - obiekt panelu
	 */
	public JPanel getTopPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Data acquisition"));
		result.setPreferredSize(new Dimension(670, 100));
	
		int posXda = 10;
		int posYda = 30;
		
		JButton acqDataButton = new JButton("SimStart");
		acqDataButton.setBounds(posXda, posYda, 110, 30);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
		acqDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				//acquireDataFromSimulation();
			}
		});
		result.add(acqDataButton);
		
		//Main mode:
		JLabel simMainModeLabel = new JLabel("Main mode:");
		simMainModeLabel.setBounds(posXda+120, posYda-20, 80, 20);
		result.add(simMainModeLabel);

		String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
		refSimNetMode = new JComboBox<String>(simModeName);
		refSimNetMode.setBounds(posXda+120, posYda, 120, 30);
		refSimNetMode.setSelectedIndex(0);
		refSimNetMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				if(doNotUpdate)
					return;
				
				int selectedModeIndex = refSimNetMode.getSelectedIndex();
				selectedModeIndex = GUIManager.getDefaultGUIManager().simSettings.checkSimulatorNetType(selectedModeIndex);
				doNotUpdate = true;
				switch(selectedModeIndex) {
					case 0:
						refNetType = NetType.BASIC;
						refSimNetMode.setSelectedIndex(0);
						break;
					case 1:
						refNetType = NetType.TIME;
						refSimNetMode.setSelectedIndex(1);
						break;
					case 2:
						refNetType = NetType.HYBRID;
						refSimNetMode.setSelectedIndex(2);
						break;
					case -1:
						refNetType = NetType.BASIC;
						refSimNetMode.setSelectedIndex(1);
						GUIManager.getDefaultGUIManager().log("Error while changing simulator mode for reference set. Set for BASIC.", "error", true);
						break;
				}
				doNotUpdate = false;
			}
		});
		result.add(refSimNetMode);
		
		//Sub-mode:
		JLabel label1 = new JLabel("Sub-mode:");
		label1.setBounds(posXda+250, posYda-20, 90, 20);
		result.add(label1);
		
		refSimMode = new JComboBox<String>(new String[] {"50/50 mode", "Maximum mode",});
		refSimMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
		refSimMode.setBounds(posXda+250, posYda, 120, 30);
		refSimMode.setSelectedIndex(0);
		refSimMode.setMaximumRowCount(6);
		refSimMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = refSimMode.getSelectedIndex();
				if(selected == 0)
					refMaximumMode = true;
				else
					refMaximumMode = false;
			}
		});
		result.add(refSimMode);

		JLabel simStepsLabel = new JLabel("Steps:");
		simStepsLabel.setBounds(posXda+380, posYda-20, 80, 20);
		result.add(simStepsLabel);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(refSimSteps, 100, 1000000, 100);
		refSimStepsSpinner = new JSpinner(simStepsSpinnerModel);
		refSimStepsSpinner.setBounds(posXda+380, posYda, 80, 30);
		refSimStepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				refSimSteps = val;
			}
		});
		result.add(refSimStepsSpinner);
		

		
		posYda += 40;
		
		
		return result;
	}
	
	
	/**
	 * Metoda tworząca panel paska postępu obliczeń.
	 * @return JPanel - obiekt panelu
	 */
	public JPanel getProgressPanel() {
		JPanel result = new JPanel(null);
		result.setBorder(BorderFactory.createTitledBorder("Data acquisition"));
		result.setPreferredSize(new Dimension(670, 50));
	
		int posXda = 10;
		int posYda = 30;
		
		progressBar = new JProgressBar();
		progressBar.setBounds(posXda, posYda-7, 550, 40);
		progressBar.setMaximum(100);
		progressBar.setMinimum(0);
	    progressBar.setValue(0);
	    progressBar.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBar.setBorder(border);
	    result.add(progressBar);
	    
	    return result;
	}
	
	
	public void resetWindow() {
		doNotUpdate = true;
		
		refNetType = NetType.BASIC;
		refSimNetMode.setSelectedIndex(0);
		
		refMaximumMode = false;
		refSimMode.setSelectedIndex(0);

		refSimSteps = 1000;
		SpinnerNumberModel spinnerClustersModel = new SpinnerNumberModel(refSimSteps, 100, 1000000, 100);
		refSimStepsSpinner.setModel(spinnerClustersModel);
		
		progressBar.setValue(0);
		
		doNotUpdate = false;
	}
}
