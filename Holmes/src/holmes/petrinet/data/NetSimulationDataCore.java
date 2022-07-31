package holmes.petrinet.data;

import java.io.*;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

import holmes.darkgui.GUIManager;
import holmes.utilities.Tools;
import holmes.workspace.ExtensionFileFilter;

/**
 * Klasa odpowiedzialna za zarządzanie danymi symulacji knockout.
 * 
 * @author MR
 */
public class NetSimulationDataCore implements Serializable {
	@Serial
	private static final long serialVersionUID = -2180386709205258057L;
	private ArrayList<NetSimulationData> referenceSets = new ArrayList<NetSimulationData>();
	private ArrayList<NetSimulationData> knockoutSets = new ArrayList<NetSimulationData>();
	private ArrayList<Long> seriesData = new ArrayList<Long>();
	
	public boolean saved = false;
	
	/**
	 * Nieciekawy konstruktor obiektu klasy NetSimulationDataCore.
	 */
	public NetSimulationDataCore() {
		
	}
	
	/**
	 * Metoda dodaje nowy pakiet danych referencyjnych.
	 * @param refSet NetSimulationData - obiekt danych
	 * @return boolean - true, jeśli się udało.
	 */
	public boolean addNewReferenceSet(NetSimulationData refSet) {
		referenceSets.add(refSet);
		saved = false;
		return true;
	}
	
	/**
	 * Metoda zwraca wektor danych referencyjnych.
	 * @return ArrayList[NetSimulationData] - wektor danych
	 */
	public ArrayList<NetSimulationData> accessReferenceSets() {
		return this.referenceSets;
	}
	
	/**
	 * Metoda zwraca jeden, wybrany pakiet danych referencyjnych.
	 * @param index int - index pakietu
	 * @return NetSimulationData - pakiet danych
	 */
	public NetSimulationData getReferenceSet(int index) {
		if(index < referenceSets.size())
			return referenceSets.get(index);
		else
			return null;
	}
	
	/**
	 * Metoda dodaje nowy pakiet danych knockout.
	 * @param refSet NetSimulationData - obiekt danych
	 * @return boolean - true, jeśli się udało.
	 */
	public boolean addNewDataSet(NetSimulationData refSet) {
		knockoutSets.add(refSet);
		saved = false;
		return true;
	}
	
	/**
	 * Metoda zwraca wektor danych knockout.
	 * @return ArrayList[NetSimulationData] - wektor danych
	 */
	public ArrayList<NetSimulationData> accessKnockoutDataSets() {
		return this.knockoutSets;
	}
	
	/**
	 * Metoda zwraca jeden, wybrany pakiet danych knockout.
	 * @param index int - index pakietu
	 * @return NetSimulationData - pakiet danych
	 */
	public NetSimulationData getKnockoutSet(int index) {
		if(index < knockoutSets.size())
			return knockoutSets.get(index);
		else
			return null;
	}
	
	/**
	 * Dodawanie nowego ID serii danych.
	 * @param value long - ID serii
	 */
	public void addNewSeries(long value) {
		if(!seriesData.contains(value))
			seriesData.add(value);
	}
	
	/**
	 * Dostęp do wektora obliczonych serii danych.
	 */
	public ArrayList<Long> accessSeries() {
		return this.seriesData;
	}
	
	/**
	 * Usuwanie całej serii danych knockout.
	 * @param IDseries long - ID serii
	 */
	public void removeSeries(long IDseries) {
		int knockSize = knockoutSets.size();
		for(int s=0; s<knockSize; s++) {
			if(knockoutSets.get(s).getIDseries() == IDseries) {
				knockoutSets.remove(s);
				s--;
				knockSize--;
			}
		}
		seriesData.remove((Long)IDseries);
	}
	
	/**
	 * Zwraca pierwszy obiekt danych z danej serii.
	 * @param IDseries long - ID serii
	 * @return NetSimulationData - pakiet danych z serii
	 */
	public NetSimulationData returnSeriesFirst(long IDseries) {
		for(NetSimulationData nsd : knockoutSets) {
			if(nsd.getIDseries() == IDseries)
				return nsd;
		}
		
		return null;
	}
	
	/**
	 * Zwraca wszystkie pakiety danych serii.
	 * @param IDseries long - ID serii
	 * @return ArrayList[NetSimulationData] - pakiet serii
	 */
	public ArrayList<NetSimulationData> getSeriesDatasets(long IDseries) {
		ArrayList<NetSimulationData> result = new ArrayList<NetSimulationData>();
		NetSimulationData tmp = returnSeriesFirst(IDseries);
		int transSize = tmp.transNumber;
		for(NetSimulationData data : knockoutSets) {
			if(data.getIDseries() == IDseries) {
				result.add(data);
			}
		}
		if(result.size() != transSize) {
			GUIManager.getDefaultGUIManager().log("Error: data package incomplete!", "error", true);
			return null;
		}
		return result;
	}
	
	//*************************************************************************************************************
	//*************************************************************************************************************
	//*************************************************************************************************************
	
	/**
	 * Odczyt danych symulacji knockout z pliku.
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public boolean loadDataSets() {
		NetSimulationDataCore core = new NetSimulationDataCore();
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		
		String newLocation = "";
		try
		{
			FileFilter[] filter = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Simulation Data (.sim)",  new String[] { "sim" });
			newLocation = Tools.selectFileDialog(lastPath, filter, "Load data", "", "");
			if(newLocation.equals("")) 
				return false;
			
			File test = new File(newLocation);
			if(!test.exists()) 
				return false;
			
			FileInputStream fis = new FileInputStream(newLocation);
			ObjectInputStream ois = new ObjectInputStream(fis);
			core = (NetSimulationDataCore) ois.readObject();
			ois.close();
			fis.close();
			
			GUIManager.getDefaultGUIManager().getWorkspace().getProject().setNewKnockoutData(core);
			
			return true;
		} catch(Exception ioe){
			String msg = "Simulation data loading failed for file "+newLocation;
			GUIManager.getDefaultGUIManager().log(msg, "error", true);
			GUIManager.getDefaultGUIManager().log(ioe.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Zapis danych symulacji do pliku.
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public boolean saveDataSets() {
		try{
			String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
			
			FileFilter[] filter = new FileFilter[1];
			filter[0] = new ExtensionFileFilter("Simulation Data (.sim)",  new String[] { "sim" });
			String newLocation = Tools.selectFileDialog(lastPath, filter, "Save data", "", "");
			if(newLocation.equals(""))
				return false;
			
			if(!newLocation.contains(".sim"))
				newLocation += ".sim";

			FileOutputStream fos= new FileOutputStream(newLocation);
			ObjectOutputStream oos= new ObjectOutputStream(fos);
			NetSimulationDataCore core = GUIManager.getDefaultGUIManager().getWorkspace().getProject().accessSimKnockoutData();
			oos.writeObject(core);
			oos.close();
			fos.close();
			return true;
		} catch(IOException ioe){
			GUIManager.getDefaultGUIManager().log("Saving simulation data failed.", "error", true);
			return false;
		}
	}


}
