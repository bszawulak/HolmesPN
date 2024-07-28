package holmes.windows.ssim;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.data.NetSimulationData;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.simulators.GraphicalSimulator.SimulatorMode;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;

/**
 * Klasa metod pomocniczych klasy HolmesStateSimulatorKnockout.
 */
public class HolmesSimKnockActions {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	HolmesSimKnock boss;
	private int pingPongSimTransLimit;
	private int pingPongSimCurrentTrans;
	private long pingPongSimSeries;
	private PetriNet pn;
	
	/**
	 * Konstruktor obiektu klasy HolmesStateSimulatorKnockoutActions.
	 * @param window HolmesStateSimulatorKnockout - okno nadrzędne
	 */
	public HolmesSimKnockActions(HolmesSimKnock window) {
		this.boss = window;
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
					lang.getText("HSKAwin_entry001"), lang.getText("HSKAwin_entry001t"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					lang.getText("HSKAwin_entry002"), lang.getText("problem"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		boolean success =  boss.ssimKnock.initiateSim(true, null);
		if(!success) {
			overlord.log(lang.getText("HSKAwin_entry003"), "error", true);
			return;
		}
		
		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		NetSimulationData currentDataPackage = new NetSimulationData();
		
		for(Transition trans : transitions)
			trans.setKnockout(false); //REFERENCE
		
		boss.ssimKnock.setThreadDetails(2, boss.mainSimWindow, boss.refProgressBarKnockout, 
				currentDataPackage);
		Thread myThread = new Thread(boss.ssimKnock);
		boss.refSimInProgress = true;
		myThread.start();
	}
	
	/**
	 * Wywoływana po zakończeniu symulacji do zbierania danych referencyjnych.
	 * @param netSimData NetSimulationData - zebrane dane
	 * @param places ArrayList[Place] - wektor miejsc
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 */
	public void completeRefSimulationResults(NetSimulationData netSimData, ArrayList<Transition> transitions, ArrayList<Place> places) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		netSimData.date = dateFormat.format(date);
		netSimData.refSet = true;
		pn.accessSimKnockoutData().addNewReferenceSet(netSimData);

		if(boss.refShowNotepad) {
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);

			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL(lang.getText("HSKAwin_entry004"), "text");
			notePad.addTextLineNL("", "text");
			for(int t = 0; t< netSimData.transFiringsAvg.size(); t++) {
				String t_name = transitions.get(t).getName();
				double valAvg = netSimData.transFiringsAvg.get(t);
				double valMin = netSimData.transFiringsMin.get(t);
				double valMax = netSimData.transFiringsMax.get(t);

				String strB = "err.";
				try {
					strB = String.format("  "+lang.getText("HSKAwin_entry005"), t, t_name, Tools.cutValueExt(valAvg,4), Tools.cutValueExt(valMin,4), Tools.cutValueExt(valMax,4));
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKAwin_entry005", "error", true);
				}
				notePad.addTextLineNL(strB, "text");
			}
			
			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL("Places:", "text");
			notePad.addTextLineNL("", "text");
			for(int t = 0; t< netSimData.placeTokensAvg.size(); t++) {
				String t_name = places.get(t).getName();
				double valAvg = netSimData.placeTokensAvg.get(t);
				double valMin = netSimData.placeTokensMin.get(t);
				double valMax = netSimData.placeTokensMax.get(t);
				String strB = "err.";
				try {
					strB = String.format("  "+lang.getText("HSKAwin_entry007"), t, t_name, Tools.cutValueExt(valAvg,4), Tools.cutValueExt(valMin,4), Tools.cutValueExt(valMax,4));
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKAwin_entry007", "error", true);
				}
				notePad.addTextLineNL(strB, "text");
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
	 * @param dataSelectedTransTextArea JTextArea - pole z wpisanymi tranzycjami do wyłączenia
	 * @param manualSelection boolean - true, jeśli symulacja dotyczy tylko ręcznie wyłączonych tranzycji 
	 */
	public void acquireDataForKnockoutSet(JTextArea dataSelectedTransTextArea, boolean manualSelection) {
		if(overlord.getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
					lang.getText("HSKAwin_entry001"), lang.getText("HSKAwin_entry001t"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					lang.getText("HSKAwin_entry002"), lang.getText("problem"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		boolean success =  boss.ssimKnock.initiateSim(true, null);
		if(!success) {
			overlord.log(lang.getText("HSKAwin_entry003"), "error", true);
			return;
		}

		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		
		NetSimulationData currentDataPackage = new NetSimulationData();
		updateNetOfflineStatus(dataSelectedTransTextArea, transitions, manualSelection, currentDataPackage);
		
		boss.ssimKnock.setThreadDetails(3, boss.mainSimWindow, boss.dataProgressBarKnockout, currentDataPackage);
		Thread myThread = new Thread(boss.ssimKnock);
		boss.dataSimInProgress = true;
		myThread.start();
	}
	
	/**
	 * Wywoływana po zakończeniu symulacji zbierania danych knockout.
	 * @param netSimData NetSimulationData - zebrane dane
	 * @param transitions (<b>ArrayList[Transition]</b>) lista tranzycji.
	 * @param places (<b>ArrayList[Place]</b>) lista miejsc.
	 */
	public void completeKnockoutSimulationResults(NetSimulationData netSimData, ArrayList<Transition> transitions, ArrayList<Place> places) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		netSimData.date = dateFormat.format(date);
		netSimData.refSet = false;
		pn.accessSimKnockoutData().addNewDataSet(netSimData);

		if(boss.dataShowNotepad) {
			HolmesNotepad notePad = new HolmesNotepad(900,600);
			notePad.setVisible(true);
			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL(lang.getText("HSKAwin_entry004"), "text");
			notePad.addTextLineNL("", "text");
			for(int t = 0; t< netSimData.transFiringsAvg.size(); t++) {
				String t_name = transitions.get(t).getName();
				double valAvg = netSimData.transFiringsAvg.get(t);
				double valMin = netSimData.transFiringsMin.get(t);
				double valMax = netSimData.transFiringsMax.get(t);
				String strB = "err.";
				try {
					strB = String.format("  "+lang.getText("HSKAwin_entry008"), t, t_name, Tools.cutValueExt(valAvg,4), Tools.cutValueExt(valMin,4), Tools.cutValueExt(valMax,4));
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKAwin_entry008", "error", true);
				}
				notePad.addTextLineNL(strB, "text");

			}

			notePad.addTextLineNL("", "text");
			notePad.addTextLineNL(lang.getText("HSKAwin_entry006"), "text");
			notePad.addTextLineNL("", "text");
			for(int t = 0; t< netSimData.placeTokensAvg.size(); t++) {
				String t_name = places.get(t).getName();
				double valAvg = netSimData.placeTokensAvg.get(t);
				double valMin = netSimData.placeTokensMin.get(t);
				double valMax = netSimData.placeTokensMax.get(t);
				String strB = "err.";
				try {
					strB = String.format("  "+lang.getText("HSKAwin_entry009"), t, t_name, Tools.cutValueExt(valAvg,4), Tools.cutValueExt(valMin,4), Tools.cutValueExt(valMax,4));
				} catch (Exception e) {
					overlord.log(lang.getText("LOGentryLNGexc")+" "+"HSKAwin_entry009", "error", true);
				}
				notePad.addTextLineNL(strB, "text");
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
					lang.getText("HSKAwin_entry001"), lang.getText("HSKAwin_entry001t"), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = pn.getTransitions();
		if(transitions == null || transitions.isEmpty()) {
			JOptionPane.showMessageDialog(null,
					lang.getText("HSKAwin_entry002"), lang.getText("problem"), JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		boolean success =  boss.ssimKnock.initiateSim(true, null);
		if(!success) {
			overlord.log(lang.getText("HSKAwin_entry003"), "error", true);
			return;
		}

		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		
		for(Transition trans : transitions) {
			trans.setKnockout(false);
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
					BorderFactory.createTitledBorder(lang.getText("HSKAwin_entry010")+" "+(pingPongSimCurrentTrans+1)+"/"+pingPongSimTransLimit)); //pierwsza tranzycja
			
			pn.getTransitions().get(pingPongSimCurrentTrans).setKnockout(true);
		    
			NetSimulationData currentDataPackage = new NetSimulationData();
			boss.ssimKnock.setThreadDetails(4, boss.mainSimWindow, boss.dataProgressBarKnockout, 
					currentDataPackage);
			Thread myThread = new Thread(boss.ssimKnock);
			boss.dataSimInProgress = true;
			myThread.start();
		} else {
			transitions.get(pingPongSimCurrentTrans).setKnockout(false); //odznacz offline status poprzednio symulowanej tranzycji

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
			if(pingPongSimCurrentTrans == pingPongSimTransLimit || forceTerminate) {
				boss.mainSimWindow.setWorkInProgress(false);
				boss.setSimWindowComponentsStatus(true);
				boss.dataSimInProgress = false;
				boss.updateFreshKnockoutTab();
				return; //wszystko policzono
			}
			transitions.get(pingPongSimCurrentTrans).setKnockout(true); //następna do analizy
			
			boss.dataProgressBarKnockout.setBorder(
					BorderFactory.createTitledBorder(lang.getText("HSKAwin_entry010")+" "+(pingPongSimCurrentTrans+1)+"/"+pingPongSimTransLimit));

			NetSimulationData currentDataPackage = new NetSimulationData();
			boss.ssimKnock.setThreadDetails(4, boss.mainSimWindow, boss.dataProgressBarKnockout, 
					currentDataPackage);
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
	 * @param manualSelection boolean - true dodaje tylko tranzycje ręcznie wyłączone
	 * @param currentDataPackage NetSimulationData - obiekt danych symulacji
	 */
	private void updateNetOfflineStatus(JTextArea dataSelectedTransTextArea, ArrayList<Transition> transitions, 
			boolean manualSelection, NetSimulationData currentDataPackage) {
		
		if(manualSelection) {
			for(int t=0; t<transitions.size(); t++) {
				Transition trans = transitions.get(t);
				if(trans.isKnockedOut()) {
					currentDataPackage.disabledTransitionsIDs.add(t);
					currentDataPackage.disabledTotals.add(t);
				}
			}
		} else {
			ArrayList<ArrayList<Transition>> mcts = pn.getMCTMatrix();
			ArrayList<Transition> disableList = new ArrayList<>();
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
				trans.setKnockout(disableList.contains(trans));
			}
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
			Object[] options = {lang.getText("HSKAwin_entry011op1"), lang.getText("HSKAwin_entry011op2"),}; //Remove ref. set / Keep it
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSKAwin_entry011"), lang.getText("HSKAwin_entry011t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
			if (n == 0) {
				references.remove(i);
				return true;
			}
		} else {
			Object[] options = {lang.getText("HSKAwin_entry012op1"), lang.getText("HSKAwin_entry012op2"),};
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSKAwin_entry012"), lang.getText("HSKAwin_entry012t"), JOptionPane.YES_NO_OPTION,
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
			Object[] options = {lang.getText("HSKAwin_entry013op1"), lang.getText("HSKAwin_entry013op2"),}; //Remove ALL data series / Keep them
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSKAwin_entry013"),
							lang.getText("HSKAwin_entry013t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.ERROR_MESSAGE, null, options, options[1]);
			if (n == 0) {
				pn.accessSimKnockoutData().removeSeries(knockouts.get(i).getIDseries());
				return true;
			}
			
		} else {
		
			Object[] options = {lang.getText("HSKAwin_entry014op1"), lang.getText("HSKAwin_entry014op2"),}; //Remove selected set / Keep it
			int n = JOptionPane.showOptionDialog(null,
							lang.getText("HSKAwin_entry014"),
							lang.getText("HSKAwin_entry014t"), JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
			if (n == 0) {
				knockouts.remove(i);
				return true;
			}
		}
		return false;
	}
}
