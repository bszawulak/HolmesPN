package abyss.files.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;

public class ProjectReader {
	private PetriNet projectCore = null;
	private ArrayList<Place> places = null;
	private ArrayList<Transition> transitions = null;
	private ArrayList<Arc> arcs = null;
	private ArrayList<ArrayList<Integer>> invariantsMatrix = null;
	private ArrayList<String> invariantsNames = null;
	private ArrayList<ArrayList<Transition>> mctData = null;
	private ArrayList<String> mctNames = null;

	private int lineNumber = 0;
	
	public ProjectReader() {
		projectCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		places = projectCore.getPlaces();
		transitions = projectCore.getTransitions();
		arcs = projectCore.getArcs();
		invariantsMatrix = projectCore.getInvariantsMatrix();
		invariantsNames = projectCore.accessInvNames();
		mctData = projectCore.getMCTMatrix();
		mctNames = projectCore.accessMCTNames();
		
		lineNumber = 0;
	}
	
	public boolean readProject(String filepath) {
		boolean status = GUIManager.getDefaultGUIManager().reset.newProjectInitiated();
		if(status == false) {
			return false;
		}
		
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
			
			status = readNetwork(buffer);
			
			
			buffer.close();
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading project file failed.", "error", true);
			return false;
		}
	}

	private boolean readNetwork(BufferedReader buffer) {
		boolean status = false;
		try {
			
		} catch (Exception e) {
			
		}
		
		return status;
	}
	
	private boolean loadInvariants(BufferedWriter bw) {
		try {
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean loadMCT(BufferedWriter bw) {
		try {
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
