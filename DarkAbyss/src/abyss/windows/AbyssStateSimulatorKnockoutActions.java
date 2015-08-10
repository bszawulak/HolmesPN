package abyss.windows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileFilter;

import abyss.clusters.ClusteringInfoMatrix;
import abyss.darkgui.GUIManager;
import abyss.petrinet.data.NetSimulationData;
import abyss.petrinet.data.NetSimulationDataCore;
import abyss.petrinet.data.PetriNet;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.simulators.NetSimulator.SimulatorMode;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Metoda pomocnicze klasy AbyssStateSimulatorKnockout.
 * 
 * @author MR
 */
public class AbyssStateSimulatorKnockoutActions {
	AbyssStateSimulatorKnockout boss;
	
	public AbyssStateSimulatorKnockoutActions(AbyssStateSimulatorKnockout window) {
		this.boss = window;
	}
	
	/**
	 * Metoda wywoływana przyciskiem aktywującym zbieranie danych referencyjnych.
	 */
	public void acquireDataForRefSet() {
		if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
					"Main simulator active. Please turn if off before starting state simulator process", 
					"Main simulator active", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		if(transitions == null || transitions.size() == 0)
			return;
		
		boolean success =  boss.ssimKnock.initiateSim(boss.refNetType, boss.refMaximumMode);
		if(success == false)
			return;
		
		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		
		boss.ssimKnock.setThreadDetails(2, boss.mainSimWindow, boss.refProgressBarKnockout, boss.refSimSteps, boss.refRepetitions);
		Thread myThread = new Thread(boss.ssimKnock);
		myThread.start();
	}
	
	/**
	 * Wywoływana po zakończeniu symulacji do zbierania danych referencyjnych.
	 * @param data NetSimulationData - zebrane dane
	 */
	public void completeRefSimulationResults(NetSimulationData data) {
		NetSimulationData netSimData = data;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		netSimData.date = dateFormat.format(date);
		netSimData.refSet = true;
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().accessSimKnockoutData().addNewReferenceSet(netSimData);
		

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
		
		boss.mainSimWindow.setWorkInProgress(false);
		boss.setSimWindowComponentsStatus(true);
	}
	
	public void acquireDataForKnockoutSet(JTextArea dataSelectedTransTextArea) {
		if(GUIManager.getDefaultGUIManager().getSimulatorBox().getCurrentDockWindow().getSimulator().getSimulatorStatus() != SimulatorMode.STOPPED) {
			JOptionPane.showMessageDialog(null,
					"Main simulator active. Please turn if off before starting state simulator process", 
					"Main simulator active", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		if(transitions == null || transitions.size() == 0)
			return;
		
		boolean success =  boss.ssimKnock.initiateSim(boss.dataNetType, boss.dataMaximumMode);
		if(success == false)
			return;
		//TODO:
		boss.setSimWindowComponentsStatus(false);
		boss.mainSimWindow.setWorkInProgress(true);
		
		updateNetOfflineStatus(dataSelectedTransTextArea, transitions, true);
		
		boss.ssimKnock.setThreadDetails(3, boss.mainSimWindow, boss.refProgressBarKnockout, boss.refSimSteps, boss.refRepetitions);
		Thread myThread = new Thread(boss.ssimKnock);
		myThread.start();
	}

	/**
	 * 
	 * @param dataSelectedTransTextArea
	 * @param transitions
	 * @param status boolean - true blokuje tranzycję, false odblokowuje
	 */
	private void updateNetOfflineStatus(JTextArea dataSelectedTransTextArea, ArrayList<Transition> transitions, boolean status) {
		PetriNet pn = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		ArrayList<ArrayList<Transition>> mcts = pn.getMCTMatrix();
		//TODO:
		String dataTxt = dataSelectedTransTextArea.getText();
		
	}

	/**
	 * Zapis danych symulacji do pliku.
	 */
	public void saveDataSets() {
		try{
			String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
			
			FileFilter filter[] = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Simulation Data (.sim)",  new String[] { "sim" });
			String newLocation = Tools.selectFileDialog(lastPath, filter, "Save data", "", "");
			if(newLocation.equals(""))
				return;
			
			if(!newLocation.contains(".sim"))
				newLocation += ".sim";

			FileOutputStream fos= new FileOutputStream(newLocation);
			ObjectOutputStream oos= new ObjectOutputStream(fos);
			NetSimulationDataCore core = GUIManager.getDefaultGUIManager().getWorkspace().getProject().accessSimKnockoutData();
			oos.writeObject(core);
			oos.close();
			fos.close();
		} catch(IOException ioe){
			GUIManager.getDefaultGUIManager().log("Saving simulation data failed.", "error", false);
		}
	}
	
	/**
	 * Odczyt danych z pliku.
	 */
	public void loadDataSets() {
		NetSimulationDataCore core = new NetSimulationDataCore();
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		
		String newLocation = "";
		try
		{
			FileFilter filter[] = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Simulation Data (.sim)",  new String[] { "sim" });
			newLocation = Tools.selectFileDialog(lastPath, filter, "Load data", "", "");
			if(newLocation.equals("")) 
				return;
			
			File test = new File(newLocation);
			if(!test.exists()) 
				return;
			
			FileInputStream fis = new FileInputStream(newLocation);
			ObjectInputStream ois = new ObjectInputStream(fis);
			core = (NetSimulationDataCore) ois.readObject();
			ois.close();
			fis.close();
			
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().setNewKnockoutData(core);
			boss.resetWindow();
			boss.updateFreshKnockoutTab();
		} catch(Exception ioe){
			String msg = "Simulation data loading failed for file "+newLocation;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			return;
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

}
