package abyss.files.io;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.graphpanel.IdGenerator;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.PetriNetElement.PetriNetElementType;
import abyss.math.Transition.TransitionType;
import abyss.utilities.Tools;

/**
 * Metoda czytająca plik danych projektu: sieć, inwarianty, MCT.
 * 
 * Modyfikacje danych sieci: głównie poprzez dodawanie kolejnych elementów do metod:
 * 		parsePlaceLine(...)
 * 		parseTransitionLine(...)
 * 		parseArcLine(...)
 * 
 * @author MR
 *
 */
public class ProjectReader {
	private PetriNet projectCore = null;
	private ArrayList<Node> nodes = null;
	private ArrayList<Arc> arcs = null;
	private ArrayList<ArrayList<Integer>> invariantsMatrix = null;
	private ArrayList<String> invariantsNames = null;
	private ArrayList<ArrayList<Transition>> mctData = null;
	private ArrayList<String> mctNames = null;

	private int placesProcessed = 0;
	private int transitionsProcessed = 0;
	private int arcsProcessed = 0;
	private int globalMaxHeight = 0;
	private int globalMaxWidth = 0;
	
	/**
	 * Konstruktor obiektu klasy odczytywania projektu.
	 */
	public ProjectReader() {
		projectCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		nodes = projectCore.getNodes();
		arcs = projectCore.getArcs();
		invariantsMatrix = projectCore.getInvariantsMatrix();
		invariantsNames = projectCore.accessInvNames();
		mctData = projectCore.getMCTMatrix();
		mctNames = projectCore.accessMCTNames();
		
		placesProcessed = 0;
		transitionsProcessed = 0;
		arcsProcessed = 0;
	}
	
	/**
	 * Metoda odpowiedzialna za odczyt danych projektu, do którego ścieżka podana jest jako parametr. 
	 * @param filepath String - pełna ścieżka do pliku projektu
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public boolean readProject(String filepath) {
		boolean status = GUIManager.getDefaultGUIManager().reset.newProjectInitiated();
		if(status == false) {
			return false;
		}
		
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
			
			GUIManager.getDefaultGUIManager().log("Reading project file: "+filepath, "text", true);
			
			status = readNetwork(buffer);
			if(status == false) {
				GUIManager.getDefaultGUIManager().log("Reading network data failure. Terminating operation.", "error", true);
				buffer.close();
				return false;
			}
			
			setGraphPanelSize();
			
			buffer.close();
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading project file failed.", "error", true);
			return false;
		}
	}

	/**
	 * Metoda odczytująca dane sieci z pliku projektu i tworząca sieć na bazie miejsc, tranzycji i łuków.
	 * @param buffer BufferedReader - obiekt odczytujący
	 * @return boolean - true, jeśli nie było problemów przy odczycie
	 */
	private boolean readNetwork(BufferedReader buffer) {
		boolean status = false;
		try {
			String line = buffer.readLine();
			if(line.contains("Project name")) {
				line = line.substring(line.indexOf("name:")+6);
				projectCore.setName(line);
			}
			//ID GENERATOR:
			while(!((line = buffer.readLine()).contains("ID generator"))) //przewiń do ID generator
				;
			line = line.substring(line.indexOf("state:")+6);
			line = line.replace(">", "");
			String[] tab = line.split(";");
			int totalIDs = Integer.parseInt(tab[0]);
			int placesIDs = Integer.parseInt(tab[1]);
			int transIDs = Integer.parseInt(tab[2]);
			
			
			//PLACES:
			line = buffer.readLine();
			if(!line.contains("<Places: 0>")) { //są miejsca
				line = buffer.readLine(); // -> Place: 0
				boolean go = true;
				
				while(go) {
					Place place = new Place(IdGenerator.getNextId(), 0, new Point(20,20));
					
					while(!((line = buffer.readLine()).contains("<EOP>"))) {
						parsePlaceLine(line, place);
					}
					line = buffer.readLine();
					if(line.contains("<Places data block end>")) {
						go = false;
					}
					placesProcessed++;
					nodes.add(place);
				}
				//przeczytano miejsca
			}
			
			while(!((line = buffer.readLine()).contains("<Transitions: "))) //przewiń do tranzycji
				;
			
			//TRANSITIONS:
			line = buffer.readLine();
			if(!line.contains("<Transitions: 0>")) { //są tranzycje
				line = buffer.readLine(); // -> Transition: 0
				boolean go = true;
				
				while(go) {
					Transition transition = new Transition(IdGenerator.getNextId(), 0, new Point(20,20));
					
					while(!((line = buffer.readLine()).contains("<EOT>"))) {
						parseTransitionLine(line, transition);
					}
					line = buffer.readLine();
					if(line.contains("<Transitions data block end>")) {
						go = false;
					}
					transitionsProcessed++;
					nodes.add(transition);
				}
				//przeczytano tranzycje
			}
			
			
			status = true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Project error reading failed in network section.", "error", true);
		}
		return status;
	}

	/**
	 * Metoda analizująca przeczytaną linię pliku opisującą miejsce sieci i wprowadzające odpowiednie zmiany
	 * do tworzonego obiektu miejsca.
	 * @param line String - przeczytana linia
	 * @param place Place - obiekt miejsca
	 */
	private void parsePlaceLine(String line, Place place) {
		String backup = line;
		try {
			String query = "";
			query = "Place gID:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				
				return;
			}
			
			query = "Place name:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				place.setName(line);
				return;
			}
			
			query = "Place comment:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				line = Tools.decodeString(line);
				place.setComment(line);
				return;
			}
			
			query = "Place tokens:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				int tokens = Integer.parseInt(line);
				place.setTokensNumber(tokens);
				return;
			}
			
			query = "Place portal status:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					place.setPortal(true);
				}
				return;
			}
			
			query = "Place locations:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				int elLocSize = Integer.parseInt(line);
				for(int e=0; e<elLocSize-1; e++) {
					place.getElementLocations().add(new ElementLocation(0, new Point(20,20), place));
					place.getNamesLocations().add(new ElementLocation(0, new Point(0,0), place));
				}
				
				return;
			}
			//poniższa część MUSI się wywołać PO tej wyżej, inaczej nie będzie odpowiednio dużo pól ElementLocation!
			
			query = "Place location data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);
				
				place.getElementLocations().get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				place.getElementLocations().get(eLocIndex).setPosition(newP);
				place.getElementLocations().get(eLocIndex).setNotSnappedPosition(newP);

				setGlobalXY(pointX, pointY); //update graph panel
				
				return;
			}
			query = "Place name offset data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);
				
				place.getNamesLocations().get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				place.getNamesLocations().get(eLocIndex).forceSetPosition(newP);
				place.getNamesLocations().get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading file error in line: "+backup+" for Place "+placesProcessed, "error", true);
		}
	}
	
	
	private void parseTransitionLine(String line, Transition transition) {
		String backup = line;
		try {
			String query = "";
			query = "Transition gID:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				
				return;
			}
			
			query = "Transition type:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				
				if(line.equals("PN")) {
					transition.setTransType(TransitionType.PN);
				} else if(line.equals("TPN")) {
					transition.setTransType(TransitionType.TPN);
					transition.setType(PetriNetElementType.TIMETRANSITION);
				}

				return;
			}
			
			query = "Transition name:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				transition.setName(line);
				return;
			}
			
			query = "Transition comment:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				line = Tools.decodeString(line);
				transition.setComment(line);
				return;
			}
			
			query = "Transition eft:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				double eft = Double.parseDouble(line);
				transition.setMinFireTime(eft);
				return;
			}
			
			query = "Transition lft:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				double lft = Double.parseDouble(line);
				transition.setMaxFireTime(lft);
				return;
			}
			
			query = "Transition portal status:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					transition.setPortal(true);
				}
				return;
			}
			
			query = "Transition locations:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				int elLocSize = Integer.parseInt(line);
				for(int e=0; e<elLocSize-1; e++) {
					transition.getElementLocations().add(new ElementLocation(0, new Point(20,20), transition));
					transition.getNamesLocations().add(new ElementLocation(0, new Point(0,0), transition));
				}
				
				return;
			}
			//poniższa część MUSI się wywołać PO tej wyżej, inaczej nie będzie odpowiednio dużo pól ElementLocation!
			
			query = "Transition location data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);
				
				transition.getElementLocations().get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				transition.getElementLocations().get(eLocIndex).setPosition(newP);
				transition.getElementLocations().get(eLocIndex).setNotSnappedPosition(newP);

				setGlobalXY(pointX, pointY); //update graph panel
				
				return;
			}
			query = "Transition name offset data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);
				
				transition.getNamesLocations().get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				transition.getNamesLocations().get(eLocIndex).forceSetPosition(newP);
				transition.getNamesLocations().get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading file error in line: "+backup+" for Transition "+transitionsProcessed, "error", true);
		}
	}
	
	private void setGlobalXY(int x, int y) {
		if(x > globalMaxWidth)
			globalMaxWidth = x;
		if(y > globalMaxHeight)
			globalMaxHeight = y;
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
	
	private void setGraphPanelSize() {
		int nodeSID = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().size() - 1;
		int SIN = GUIManager.getDefaultGUIManager().IDtoIndex(nodeSID);
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(SIN).getGraphPanel();
		graphPanel.setOriginSize(graphPanel.getSize());
		graphPanel.repaint();
	}
}
