package holmes.windows.ssim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.NetSimulator.SimulatorMode;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;

/**
 * Klasa metod pomocniczych klasy HolmesStateSimulatorKnockout.
 * 
 * @author MR
 */
public class HolmesStateSimKnockActions {
	HolmesStateSimKnock boss;
	private int pingPongSimTransLimit;
	private int pingPongSimCurrentTrans;
	private long pingPongSimSeries;
	private GUIManager overlord;
	private PetriNet pn;
	
	/**
	 * Konstruktor obiektu klasy HolmesStateSimulatorKnockoutActions.
	 * @param window HolmesStateSimulatorKnockout - okno nadrzędne
	 */
	public HolmesStateSimKnockActions(HolmesStateSimKnock window) {
		this.boss = window;
		this.overlord = GUIManager.getDefaultGUIManager();
		this.pn = overlord.getWorkspace().getProject();
	}
	
	//*****************************************************************************************************************
	//*****************************************************************************************************************
	//*****************************************************************************************************************
	
	/**
	 * Metoda wywoływana przyciskiem aktywującym zbieranie danych referencyjnych.
	 */
	public void acquireDataForRefSet() {
		if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
					"Main simulator active. Please turn if off before starting state simulator process", 
					"Main simulator active", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"At least single place and transition required for simulation.", 
					"Net not found", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		boolean success =  boss.ssimKnock.initiateSim(boss.refNetType, boss.refMaximumMode, boss.refSingleMode);
		if(success == false) {
			GUIManager.getDefaultGUIManager().log("State simulator initialization failed.", "error", true);
			return;
		}
		
		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		NetSimulationData currentDataPackage = new NetSimulationData();
		
		for(Transition trans : transitions)
			trans.setOffline(false); //REFERENCE
		
		boss.ssimKnock.setThreadDetails(2, boss.mainSimWindow, boss.refProgressBarKnockout, 
				boss.refSimSteps, boss.refRepetitions, currentDataPackage);
		Thread myThread = new Thread(boss.ssimKnock);
		boss.refSimInProgress = true;
		myThread.start();
	}
	
	/**
	 * Wywoływana po zakończeniu symulacji do zbierania danych referencyjnych.
	 * @param data NetSimulationData - zebrane dane
	 * @param places ArrayList[Place] - wektor miejsc
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 */
	public void completeRefSimulationResults(NetSimulationData data, ArrayList<Transition> transitions, ArrayList<Place> places) {
		NetSimulationData netSimData = data;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		netSimData.date = dateFormat.format(date);
		netSimData.refSet = true;
		pn.accessSimKnockoutData().addNewReferenceSet(netSimData);

		if(boss.refShowNotepad) {
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);

			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL("Transitions: ", "text");
			notePad.addTextLineNL("", "text");
			for(int t=0; t<netSimData.transFiringsAvg.size(); t++) {
				String t_name = transitions.get(t).getName();
				double valAvg = netSimData.transFiringsAvg.get(t);
				double valMin = netSimData.transFiringsMin.get(t);
				double valMax = netSimData.transFiringsMax.get(t);

				notePad.addTextLineNL("  t"+t+"_"+t_name+" : "+Tools.cutValueExt(valAvg,4)+" min: "+Tools.cutValueExt(valMin,4)+
						" max: "+Tools.cutValueExt(valMax,4), "text");

			}
			
			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL("Places: ", "text");
			notePad.addTextLineNL("", "text");
			for(int t=0; t<netSimData.placeTokensAvg.size(); t++) {
				String t_name = places.get(t).getName();
				double valAvg = netSimData.placeTokensAvg.get(t);
				double valMin = netSimData.placeTokensMin.get(t);
				double valMax = netSimData.placeTokensMax.get(t);

				notePad.addTextLineNL("  p"+t+"_"+t_name+" : "+Tools.cutValueExt(valAvg,4)+" min: "+Tools.cutValueExt(valMin,4)+
						" max: "+Tools.cutValueExt(valMax,4), "text");
			}
		}
		boss.refSimInProgress = false;
		boss.mainSimWindow.setWorkInProgress(false);
		boss.setSimWindowComponentsStatus(true);
		boss.updateFreshKnockoutTab();
	}
	
	//*****************************************************************************************************************
	//*****************************************************************************************************************
	//*****************************************************************************************************************
	
	/**
	 * Metoda wywoływana przyciskiem aktywującym zbieranie danych symulacji typu knockout.
	 */
	public void acquireDataForKnockoutSet(JTextArea dataSelectedTransTextArea) {
		if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
					"Main simulator active. Please turn if off before starting state simulator process", 
					"Main simulator active", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"At least single place and transition required for simulation.", 
					"Net not found", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		boolean success =  boss.ssimKnock.initiateSim(boss.dataNetType, boss.dataMaximumMode, boss.dataSingleMode);
		if(success == false) {
			GUIManager.getDefaultGUIManager().log("State simulator initialization failed.", "error", true);
			return;
		}

		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		
		
		NetSimulationData currentDataPackage = new NetSimulationData();
		updateNetOfflineStatus(dataSelectedTransTextArea, transitions, true, currentDataPackage);
		
		boss.ssimKnock.setThreadDetails(3, boss.mainSimWindow, boss.dataProgressBarKnockout, 
				boss.dataSimSteps, boss.dataRepetitions, currentDataPackage);
		Thread myThread = new Thread(boss.ssimKnock);
		boss.dataSimInProgress = true;
		myThread.start();
	}
	
	/**
	 * Wywoływana po zakończeniu symulacji zbierania danych knockout.
	 * @param data NetSimulationData - zebrane dane
	 * @param places2 
	 * @param transitions2 
	 */
	public void completeKnockoutSimulationResults(NetSimulationData data, ArrayList<Transition> transitions, ArrayList<Place> places) {
		NetSimulationData netSimData = data;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		netSimData.date = dateFormat.format(date);
		netSimData.refSet = false;
		pn.accessSimKnockoutData().addNewDataSet(netSimData);

		if(boss.dataShowNotepad) {
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);
			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL("Transitions: ", "text");
			notePad.addTextLineNL("", "text");
			for(int t=0; t<netSimData.transFiringsAvg.size(); t++) {
				String t_name = transitions.get(t).getName();
				double valAvg = netSimData.transFiringsAvg.get(t);
				double valMin = netSimData.transFiringsMin.get(t);
				double valMax = netSimData.transFiringsMax.get(t);

				notePad.addTextLineNL("  t"+t+"_"+t_name+" : "+Tools.cutValueExt(valAvg,4)+" min: "+Tools.cutValueExt(valMin,4)+
						" max: "+Tools.cutValueExt(valMax,4), "text");

			}

			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL("Places: ", "text");
			notePad.addTextLineNL("", "text");
			for(int t=0; t<netSimData.placeTokensAvg.size(); t++) {
				String t_name = places.get(t).getName();
				double valAvg = netSimData.placeTokensAvg.get(t);
				double valMin = netSimData.placeTokensMin.get(t);
				double valMax = netSimData.placeTokensMax.get(t);

				notePad.addTextLineNL("  p"+t+"_"+t_name+" : "+Tools.cutValueExt(valAvg,4)+" min: "+Tools.cutValueExt(valMin,4)+
						" max: "+Tools.cutValueExt(valMax,4), "text");
			}
		}
		boss.dataSimInProgress = false;
		boss.mainSimWindow.setWorkInProgress(false);
		boss.setSimWindowComponentsStatus(true);
		boss.updateFreshKnockoutTab();
	}
	
	//*****************************************************************************************************************
	//*****************************************************************************************************************
	//*****************************************************************************************************************
	
	/**
	 * Metoda przygotowuje symulator do obliczenia knockout dla każdej tranzycji oddzielnie.
	 */
	public void acquireAll() {
		if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
					"Main simulator active. Please turn if off before starting state simulator process", 
					"Main simulator active", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.size() == 0) {
			JOptionPane.showMessageDialog(null,
					"At least single place and transition required for simulation.", 
					"Net not found", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		boolean success =  boss.ssimKnock.initiateSim(boss.dataNetType, boss.dataMaximumMode, boss.dataSingleMode);
		if(success == false) {
			GUIManager.getDefaultGUIManager().log("State simulator initialization failed.", "error", true);
			return;
		}

		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		
		for(Transition trans : transitions) {
			trans.setOffline(false);
		}
		
		pingPongSimTransLimit = transitions.size();
		pingPongSimCurrentTrans = -1;
		pingPongSimSeries = overlord.randGen.nextLong();
		pn.accessSimKnockoutData().addNewSeries(pingPongSimSeries);
		pingPongSimulation(null, null, null, false);
	}

	/**
	 * Metoda odpowiedzialna za cykliczne wywoływanie symulatora celem wykonania kompletu symulacji knockout,
	 * w każdym przypadku z inną tranzycją ustawioną jako offline.
	 * @param returningData NetSimulationData - zbiór danych odesłanych przez symulator
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 * @param places ArrayList[Place] - wektor miejsc
	 */
	public void pingPongSimulation(NetSimulationData returningData, ArrayList<Transition> transitions, ArrayList<Place> places, 
			boolean forceTerminate) {
		if(returningData == null) { //piersze uruchomienie
			
			pingPongSimCurrentTrans++;
			boss.dataProgressBarKnockout.setBorder(
					BorderFactory.createTitledBorder("Progress: "+(pingPongSimCurrentTrans+1)+"/"+pingPongSimTransLimit));
			
			pn.getTransitions().get(pingPongSimCurrentTrans).setOffline(true);
		    
			NetSimulationData currentDataPackage = new NetSimulationData();
			boss.ssimKnock.setThreadDetails(4, boss.mainSimWindow, boss.dataProgressBarKnockout, 
					boss.dataSimSteps, boss.dataRepetitions, currentDataPackage);
			Thread myThread = new Thread(boss.ssimKnock);
			boss.dataSimInProgress = true;
			myThread.start();
		} else {
			transitions.get(pingPongSimCurrentTrans).setOffline(false); //odznacz offline status poprzednio symulowanej tranzycji

			//dodaj nowe dane do bazy
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			returningData.date = dateFormat.format(date);
			returningData.refSet = false;
			returningData.disabledTransitionsIDs.add(pingPongSimCurrentTrans);
			returningData.disabledTotals.add(pingPongSimCurrentTrans);
			returningData.setIDseries(pingPongSimSeries);
			
			//decyzja co dalej:
			if(forceTerminate)  {
				pn.accessSimKnockoutData().removeSeries(pingPongSimSeries);
			} else {
				//dodaj tylko wtedy, gdy symulator nie został siłowo zatrzymany
				pn.accessSimKnockoutData().addNewDataSet(returningData);
			}
			pingPongSimCurrentTrans++;
			if(pingPongSimCurrentTrans == pingPongSimTransLimit || forceTerminate == true) {
				boss.mainSimWindow.setWorkInProgress(false);
				boss.setSimWindowComponentsStatus(true);
				boss.dataSimInProgress = false;
				boss.updateFreshKnockoutTab();
				return; //wszystko policzono
			}
			transitions.get(pingPongSimCurrentTrans).setOffline(true); //następna do analizy
			
			boss.dataProgressBarKnockout.setBorder(
					BorderFactory.createTitledBorder("Progress: "+(pingPongSimCurrentTrans+1)+"/"+pingPongSimTransLimit));

			NetSimulationData currentDataPackage = new NetSimulationData();
			boss.ssimKnock.setThreadDetails(4, boss.mainSimWindow, boss.dataProgressBarKnockout, 
					boss.dataSimSteps, boss.dataRepetitions, currentDataPackage);
			Thread myThread = new Thread(boss.ssimKnock);
			myThread.start();
		}
	}
	
	//*****************************************************************************************************************
	//*****************************************************************************************************************
	//*****************************************************************************************************************

	/**
	 * Zapis danych symulacji do pliku.
	 */
	public void saveDataSets() {
		boolean status = pn.accessSimKnockoutData().saveDataSets();
		if(status) {
			//anything?
		}
	}
	
	/**
	 * Odczyt danych symulacji knockout z pliku.
	 */
	public void loadDataSets() {
		boolean status = pn.accessSimKnockoutData().loadDataSets();
		if(status) {
			boss.resetWindow();
			boss.updateFreshKnockoutTab();
		} 
	}
	
	/**
	 * Metoda ustawiająca status offline dla odpowiednich tranzycji.
	 * @param dataSelectedTransTextArea JTextArea - pole tekstowe z T i MCT do wyłączenia
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 * @param status boolean - true blokuje tranzycję, false odblokowuje
	 * @param currentDataPackage NetSimulationData - obiekt danych symulacji
	 */
	private void updateNetOfflineStatus(JTextArea dataSelectedTransTextArea, ArrayList<Transition> transitions, 
			boolean status, NetSimulationData currentDataPackage) {
		ArrayList<ArrayList<Transition>> mcts = pn.getMCTMatrix();

		ArrayList<Transition> disableList = new ArrayList<Transition>();
		String dataTxt = dataSelectedTransTextArea.getText();
		while(dataTxt.contains("tr#")) {
			int index = dataTxt.indexOf("tr#");
			String tmp = dataTxt.substring(index);
			int semiIndex = tmp.indexOf(";");
			int hashIndex = tmp.indexOf("#");
			
			String value = tmp.substring(hashIndex+1, semiIndex);
			int transIndex = Integer.parseInt(value);
			if(!disableList.contains(transitions.get(transIndex))) {
				disableList.add(transitions.get(transIndex));
				currentDataPackage.disabledTransitionsIDs.add(transIndex);
				
				if(!currentDataPackage.disabledTotals.contains(transIndex))
					currentDataPackage.disabledTotals.add(transIndex);
			}
			
			tmp = tmp.substring(0, semiIndex+1);
			dataTxt = dataTxt.replace(tmp, "");
		}
		
		//deaktywuj wszystkie tranzycje ze zbioru MCT:
		while(dataTxt.contains("MCT#")) {
			int index = dataTxt.indexOf("MCT#");
			String tmp = dataTxt.substring(index);
			int semiIndex = tmp.indexOf(";");
			int hashIndex = tmp.indexOf("#");
			String value = tmp.substring(hashIndex+1, semiIndex);
			int mctIndex = Integer.parseInt(value);
			
			ArrayList<Transition> mct = mcts.get(mctIndex-1);
			currentDataPackage.disabledMCTids.add(mctIndex-1);
			for(Transition trans : mct) {
				if(!disableList.contains(trans))
					disableList.add(trans);
				
				if(!currentDataPackage.disabledTotals.contains(transitions.indexOf(trans)))
					currentDataPackage.disabledTotals.add(transitions.indexOf(trans));
			}
			
			tmp = tmp.substring(0, semiIndex+1);
			dataTxt = dataTxt.replace(tmp, "");
		}
		
		for(Transition trans : transitions) {
			if(disableList.contains(trans))
				trans.setOffline(true);
			else
				trans.setOffline(false);
		}
	}

	/**
	 * Dodaje do textArea element do wyłączenia w symulacji: tranzycja lub cały MCT
	 * @param i int - 1 = tranzycja, 2 = mct
	 * @param selected int - index elementu
	 * @param dataSelectedTransTextArea JTextArea - obiekt modyfikowany
	 */
	public void addOfflineElement(int i, int selected, JTextArea dataSelectedTransTextArea) {
		if(i == 1) { //tranzycje
			String txt = dataSelectedTransTextArea.getText();
			String trans = "tr#"+selected+";";
			if(!txt.contains(trans)) {
				txt += trans;
			}
			dataSelectedTransTextArea.setText(txt);
		} else if(i == 2) { //mct
			String txt = dataSelectedTransTextArea.getText();
			String MCT = "MCT#"+selected+";";
			if(!txt.contains(MCT)) {
				txt += MCT;
			}
			dataSelectedTransTextArea.setText(txt);
		}
	}

	/**
	 * Odejmuje od textArea element do wyłączenia w symulacji: tranzycja lub cały MCT
	 * @param i int - 1 = tranzycja, 2 = mct
	 * @param selected int - index elementu
	 * @param dataSelectedTransTextArea JTextArea - obiekt modyfikowany
	 */
	public void removeOfflineElement(int i, int selected, JTextArea dataSelectedTransTextArea) {
		if(i == 1) { //tranzycje
			String txt = dataSelectedTransTextArea.getText();
			String trans = "tr#"+selected+";";
			if(txt.contains(trans)) {
				txt = txt.replace(trans, "");
			}
			dataSelectedTransTextArea.setText(txt);
		} else if(i == 2) { //mct
			String txt = dataSelectedTransTextArea.getText();
			String MCT = "MCT#"+selected+";";
			if(txt.contains(MCT)) {
				txt = txt.replace(MCT, "");
			}
			dataSelectedTransTextArea.setText(txt);
		}
	}

	/**
	 * Usuwanie wskazanego zbioru refencyjnego.
	 * @param i int - pozycja zbioru
	 * @return boolean - true, jeśli usunięto 
	 */
	public boolean removeRedDataSet(int i) {
		ArrayList<NetSimulationData> references = pn.accessSimKnockoutData().accessReferenceSets();
		if(references.size() == 1) {
			Object[] options = {"Remove ref. set", "Keep it",};
			int n = JOptionPane.showOptionDialog(null,
							"This is the only available reference set. Without it,\ncomparing knockout data will be impossible.",
							"Remove last reference set?", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (n == 0) {
				references.remove(i);
				return true;
			}
		} else {
			Object[] options = {"Remove ref. set", "Keep it",};
			int n = JOptionPane.showOptionDialog(null,
							"Remove this reference set?",
							"Please confirm", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				references.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Usuwanie wskazanego zbioru danych knockout.
	 * @param i int - pozycja zbioru
	 * @return boolean - true, jeśli usunięto 
	 */
	public boolean removeKnockDataSet(int i) {
		ArrayList<NetSimulationData> knockouts = pn.accessSimKnockoutData().accessKnockoutDataSets();

		if(knockouts.get(i).getIDseries() != -1) {
			Object[] options = {"Remove ALL data series", "Keep them",};
			int n = JOptionPane.showOptionDialog(null,
							"WARNING! This dataset belongs to data series (e.g. single transition knockout\n"
							+ "simulation for the whole net.\n"
							+ "THIS WILL REMOVE ALL THE DATASETS, NOT ONLY THE SELECTED ONE. Proceed?",
							"Datasets series remove", JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE, null, options, options[1]);
			if (n == 0) {
				pn.accessSimKnockoutData().removeSeries(knockouts.get(i).getIDseries());
				return true;
			}
			
		} else {
		
			Object[] options = {"Remove selected set", "Keep it",};
			int n = JOptionPane.showOptionDialog(null,
							"Remove this knockout dataset?",
							"Please confirm", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				knockouts.remove(i);
				return true;
			}
		}
		return false;
	}
}
