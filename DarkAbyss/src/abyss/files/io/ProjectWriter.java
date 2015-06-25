package abyss.files.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.utilities.Tools;
import abyss.workspace.ExtensionFileFilter;

public class ProjectWriter {
	private ArrayList<Place> places = null;
	private ArrayList<Transition> transitions = null;
	private ArrayList<Arc> arcs = null;
	private ArrayList<ArrayList<Integer>> invariantsMatrix = null;
	private ArrayList<String> invariantsNames = null;
	private ArrayList<ArrayList<Transition>> mctData = null;
	private ArrayList<String> mctNames = null;

	private PetriNet projectCore = null;
	
	public ProjectWriter() {
		
	}
	
	public int writeProject() {
		//String filePath = getProjectFilePath();
		initalizeEngine();
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("test.apf"));
		
		} catch (Exception e) {
			
		}
		return -1;
	}

	private void initalizeEngine() {
		projectCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		places = projectCore.getPlaces();
		transitions = projectCore.getTransitions();
		arcs = projectCore.getArcs();
		invariantsMatrix = projectCore.getInvariantsMatrix();
		invariantsNames = projectCore.accessInvNames();
		mctData = projectCore.getMCTMatrix();
		mctNames = projectCore.accessMCTNames();
	}

	/**
	 * Metoda służąca do ustalania nazwy i ścieżki dla zapisywanego pliku projektu.
	 * @return String - ścieżka + nazwa pliku
	 */
	private String getProjectFilePath() {
		String lastPath = GUIManager.getDefaultGUIManager().getLastPath();
		FileFilter[] filters = new FileFilter[1];
		filters[0] = new ExtensionFileFilter("Abyss Project (.apf)", new String[] { "APF" });
		String selectedFile = Tools.selectFileDialog(lastPath, filters, "Save", "", projectCore.getFileName());
		if(selectedFile.equals(""))
			return null;
		
		File file = new File(selectedFile);
		String fileExtension = ".apf";
		if(selectedFile.toLowerCase().contains(".apf"))
			fileExtension = "";
		
		//projectCore.saveAsPNT(file.getPath() + fileExtension);
		GUIManager.getDefaultGUIManager().setLastPath(file.getParentFile().getPath());
		return file.getPath() + fileExtension;
	}
}
