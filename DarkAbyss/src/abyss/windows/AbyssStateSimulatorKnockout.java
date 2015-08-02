package abyss.windows;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.simulators.StateSimulator;
import abyss.petrinet.simulators.NetSimulationData;
import abyss.petrinet.simulators.NetSimulator.NetType;
import abyss.petrinet.simulators.NetSimulator.SimulatorMode;
import abyss.utilities.Tools;

/**
 * Klasa odpowiedzialna za tworzenie podstrony knockoutSim.
 * 
 * @author MR
 */
public class AbyssStateSimulatorKnockout extends JPanel {
	private static final long serialVersionUID = 4257940971120618716L;
	private boolean doNotUpdate = false;
	private int refSimSteps = 1000;			//liczba kroków dla zbioru referencyjnego
	private int refRepetitions = 100;
	
	private NetType refNetType = NetType.BASIC;		//rodzaj sieci: BASIC, TIMED, HYBRID, itd.
	private boolean refMaximumMode = false;
	private JProgressBar progressBarKnockout;
	private StateSimulator ssimKnock;
	private AbyssStateSimulator mainSimWindow;
	
	//blokowane elementy:
	private JButton acqDataButton;
	private JComboBox<String> refSimNetMode;
	private JComboBox<String> refSimMaxMode;
	private JSpinner refSimStepsSpinner;
	private JSpinner refSimRepsSpinner;
	
	/**
	 * Konstruktor obiektu klasy AbyssStateSimulatorKnockout.
	 * @param abyssStateSimulator 
	 */
	public AbyssStateSimulatorKnockout(AbyssStateSimulator abyssStateSimulator) {
		this.mainSimWindow = abyssStateSimulator;
		ssimKnock = new StateSimulator();
		
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
		int posYda = 10;
		
		acqDataButton = new JButton("SimStart");
		acqDataButton.setBounds(posXda, posYda+10, 110, 40);
		acqDataButton.setMargin(new Insets(0, 0, 0, 0));
		acqDataButton.setIcon(Tools.getResIcon32("/icons/stateSim/computeData.png"));
		acqDataButton.setToolTipText("Compute steps from zero marking through the number of states given on the right.");
		acqDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				acquireDataForRefSet();
			}
		});
		result.add(acqDataButton);
		
		JButton cancelButton = new JButton();
		cancelButton.setText("STOP");
		cancelButton.setBounds(posXda, posYda+55, 110, 25);
		cancelButton.setMargin(new Insets(0, 0, 0, 0));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				ssimKnock.terminate = true;
			}
		});
		cancelButton.setFocusPainted(false);
		result.add(cancelButton);
		
		//Main mode:
		JLabel simMainModeLabel = new JLabel("Main mode:");
		simMainModeLabel.setBounds(posXda+120, posYda, 80, 20);
		result.add(simMainModeLabel);

		String[] simModeName = {"Petri Net", "Timed Petri Net", "Hybrid mode"};
		refSimNetMode = new JComboBox<String>(simModeName);
		refSimNetMode.setBounds(posXda+120, posYda+20, 120, 20);
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
		label1.setBounds(posXda+120, posYda+40, 90, 20);
		result.add(label1);
		
		refSimMaxMode = new JComboBox<String>(new String[] {"50/50 mode", "Maximum mode",});
		refSimMaxMode.setToolTipText("In maximum mode each active transition fire at once, 50/50 means 50% chance for firing.");
		refSimMaxMode.setBounds(posXda+120, posYda+60, 120, 20);
		refSimMaxMode.setSelectedIndex(0);
		refSimMaxMode.setMaximumRowCount(6);
		refSimMaxMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int selected = refSimMaxMode.getSelectedIndex();
				if(selected == 0)
					refMaximumMode = false;
				else
					refMaximumMode = true;
			}
		});
		result.add(refSimMaxMode);

		JLabel simStepsLabel = new JLabel("Steps:");
		simStepsLabel.setBounds(posXda+250, posYda, 80, 20);
		result.add(simStepsLabel);
		
		SpinnerModel simStepsSpinnerModel = new SpinnerNumberModel(refSimSteps, 100, 1000000, 100);
		refSimStepsSpinner = new JSpinner(simStepsSpinnerModel);
		refSimStepsSpinner.setBounds(posXda+250, posYda+20, 80, 20);
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

		JLabel repetLabel = new JLabel("Repetitions:");
		repetLabel.setBounds(posXda+250, posYda+40, 80, 20);
		result.add(repetLabel);
		
		SpinnerModel simRepetsSpinnerModel = new SpinnerNumberModel(refRepetitions, 1, 100000, 10);
		refSimRepsSpinner = new JSpinner(simRepetsSpinnerModel);
		refSimRepsSpinner.setBounds(posXda+250, posYda+60, 80, 20);
		refSimRepsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(doNotUpdate)
					return;
				
				JSpinner spinner = (JSpinner) e.getSource();
				int val = (int) spinner.getValue();
				refRepetitions = val;
			}
		});
		result.add(refSimRepsSpinner);
		
		posYda += 40;
		return result;
	}
	
	/**
	 * Metoda wywoływana przyciskiem aktywującym zbieranie danych referencyjnych.
	 */
	private void acquireDataForRefSet() {
		if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
					"Main simulator active. Please turn if off before starting state simulator process", 
					"Main simulator active", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		if(places == null || places.size() == 0)
			return;
		
		boolean success =  ssimKnock.initiateSim(refNetType, refMaximumMode);
		if(success == false)
			return;
		
		blockSimWindowComponents();
		mainSimWindow.setWorkInProgress(true);
		
		ssimKnock.setThreadDetails(2, mainSimWindow, progressBarKnockout, refSimSteps, refRepetitions);
		Thread myThread = new Thread(ssimKnock);
		myThread.start();
	}
	
	/**
	 * Wywoływana po zakończeniu symulacji do zbierania danych referencyjnych.
	 * @param data NetSimulationData - zebrane dane
	 */
	public void completeRefSimulationResults(NetSimulationData data) {
		NetSimulationData netSimData = data;
		

		AbyssNotepad notePad = new AbyssNotepad(900,600);
		notePad.setVisible(true);
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Transitions: ", "text");
		notePad.addTextLineNL("", "text");
		for(int t=0; t<netSimData.refTransFiringsAvg.size(); t++) {
			String t_name = transitions.get(t).getName();
			double valAvg = netSimData.refTransFiringsAvg.get(t);
			double valMin = netSimData.refTransFiringsMin.get(t);
			double valMax = netSimData.refTransFiringsMax.get(t);

			notePad.addTextLineNL("  t"+t+"_"+t_name+" : "+Tools.cutValueExt(valAvg,4)+" min: "+Tools.cutValueExt(valMin,4)+
					" max: "+Tools.cutValueExt(valMax,4), "text");

		}
		
		ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		notePad.addTextLineNL("", "text");
		notePad.addTextLineNL("Places: ", "text");
		notePad.addTextLineNL("", "text");
		for(int t=0; t<netSimData.refPlaceTokensAvg.size(); t++) {
			String t_name = places.get(t).getName();
			double valAvg = netSimData.refPlaceTokensAvg.get(t);
			double valMin = netSimData.refPlaceTokensMin.get(t);
			double valMax = netSimData.refPlaceTokensMax.get(t);

			notePad.addTextLineNL("  p"+t+"_"+t_name+" : "+Tools.cutValueExt(valAvg,4)+" min: "+Tools.cutValueExt(valMin,4)+
					" max: "+Tools.cutValueExt(valMax,4), "text");

		}
		
		mainSimWindow.setWorkInProgress(false);
		unblockSimWindowComponents();
	}
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************

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
		
		progressBarKnockout = new JProgressBar();
		progressBarKnockout.setBounds(posXda, posYda-7, 550, 40);
		progressBarKnockout.setMaximum(100);
		progressBarKnockout.setMinimum(0);
	    progressBarKnockout.setValue(0);
	    progressBarKnockout.setStringPainted(true);
	    Border border = BorderFactory.createTitledBorder("Progress");
	    progressBarKnockout.setBorder(border);
	    result.add(progressBarKnockout);
	    
	    return result;
	}
	
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	//*******************************************************************************************************************************
	
	/**
	 * Blokuje komponenty na czas symulacji.
	 */
	private void blockSimWindowComponents() {
		acqDataButton.setEnabled(false);
		refSimNetMode.setEnabled(false);
		refSimMaxMode.setEnabled(false);
		refSimStepsSpinner.setEnabled(false);
		refSimRepsSpinner.setEnabled(false);
		
		GUIManager.getDefaultGUIManager().getFrame().setEnabled(false);
		//TODO: dodawać kolejne tutaj:
		
		
	}
	
	/**
	 * Odblokowuje komponenty po zakończonej / przerwanej symulacji.
	 */
	private void unblockSimWindowComponents() {
		acqDataButton.setEnabled(true);
		refSimNetMode.setEnabled(true);
		refSimMaxMode.setEnabled(true);
		refSimStepsSpinner.setEnabled(true);
		refSimRepsSpinner.setEnabled(true);
		
		GUIManager.getDefaultGUIManager().getFrame().setEnabled(true);
		//TODO: dodawać kolejne tutaj:
		
		
	}
	
	public void resetWindow() {
		doNotUpdate = true;
		
		refNetType = NetType.BASIC;
		refSimNetMode.setSelectedIndex(0);
		
		refMaximumMode = false;
		refSimMaxMode.setSelectedIndex(0);

		refSimSteps = 1000;
		SpinnerNumberModel spinnerClustersModel = new SpinnerNumberModel(refSimSteps, 100, 1000000, 100);
		refSimStepsSpinner.setModel(spinnerClustersModel);
		
		progressBarKnockout.setValue(0);
		
		doNotUpdate = false;
	}
}
