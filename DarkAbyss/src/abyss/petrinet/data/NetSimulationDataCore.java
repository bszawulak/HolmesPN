package abyss.petrinet.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

/**
 * Klasa odpowiedzialna za zarządzanie danymi symulacji knockout.
 * 
 * @author MR
 */
public class NetSimulationDataCore implements Serializable {
	private static final long serialVersionUID = -2180386709205258057L;
	private ArrayList<NetSimulationData> referenceSets = new ArrayList<NetSimulationData>();
	private ArrayList<NetSimulationData> knockoutSets = new ArrayList<NetSimulationData>();
	
	public boolean saved = false;
	
	public NetSimulationDataCore() {
		
	}
	
	public boolean addNewReferenceSet(NetSimulationData refSet) {
		referenceSets.add(refSet);
		saved = false;
		return true;
	}
	
	public ArrayList<NetSimulationData> getReferenceSets() {
		return this.referenceSets;
	}
	
	public boolean addNewDataSet(NetSimulationData refSet) {
		knockoutSets.add(refSet);
		saved = false;
		return true;
	}
	
	public ArrayList<NetSimulationData> getDataSets() {
		return this.knockoutSets;
	}
	
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
			FileFilter filter[] = new FileFilter[1];
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
			
			FileFilter filter[] = new FileFilter[1];
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
			GUIManager.getDefaultGUIManager().log("Saving simulation data failed.", "error", false);
			return false;
		}
	}
}
