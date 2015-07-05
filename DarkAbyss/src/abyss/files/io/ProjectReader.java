package abyss.files.io;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
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
import abyss.math.Arc.TypesOfArcs;
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
	private int invariantsProcessed = 0;
	private int mctProcessed = 0;
	
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
				GUIManager.getDefaultGUIManager().log("Reading network data block failure. Invariants/MCT reading cancelled. Terminating operation.", "error", true);
				buffer.close();
				return false;
			}
			
			status = readInvariants(buffer);
			if(!status) {
				projectCore.setInvariantsMatrix(null, false);
			} else {
				GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(projectCore.getInvariantsMatrix());
			}
			
			status = readMCT(buffer);
			if(!status) {
				projectCore.setMCTMatrix(null, false);
			} else {
				GUIManager.getDefaultGUIManager().getMctBox().showMCT(projectCore.getMCTMatrix());
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
			//dane generatora, ale właściwo to po co??
			//String[] tab = line.split(";");
			//int totalIDs = Integer.parseInt(tab[0]);
			//int placesIDs = Integer.parseInt(tab[1]);
			//int transIDs = Integer.parseInt(tab[2]);
			
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
			//line = buffer.readLine();
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
			
			ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
			ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
			
			while(!((line = buffer.readLine()).contains("<Arcs data block>"))) //przewiń do łuków
				;
			
			//ARCS:
			while(!(line = buffer.readLine()).contains("Arcs data block end")) {
				Arc newArc = parseArcLine(line, places, transitions);
				arcsProcessed++;
				arcs.add(newArc);
			}
			
			
			GUIManager.getDefaultGUIManager().log("Read "+placesProcessed+" places, "+transitionsProcessed+ 
					" transitions, "+arcsProcessed+" arcs.", "text", true);
			
			status = true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Project error reading failed in network section.", "error", true);
			GUIManager.getDefaultGUIManager().log("Read so far: "+placesProcessed+" places, "+transitionsProcessed+ 
					" transitions, "+arcsProcessed+" arcs.", "error", true);
			status = false;
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
				place.getElementLocations().get(eLocIndex).forceSetPosition(newP);
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
	
	/**
	 * Metoda analizująca przeczytaną linię pliku opisującą tranzycję sieci i wprowadzające odpowiednie zmiany
	 * do tworzonego obiektu tranzycji.
	 * @param line String - przeczytana linia
	 * @param transition Transition - modyfikowany obiekt tranzycji
	 */
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
			
			query = "Transition duration:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				double duration = Double.parseDouble(line);
				transition.setDurationTime(duration);
				return;
			}
			
			query = "Transition TPN status:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true"))
					transition.setTPNstatus(true);
				else
					transition.setTPNstatus(false);
				return;
			}
			
			query = "Transition DPN status:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true"))
					transition.setDPNstatus(true);
				else
					transition.setDPNstatus(false);
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
				transition.getElementLocations().get(eLocIndex).forceSetPosition(newP);
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
	
	private Arc parseArcLine(String line, ArrayList<Place> places, ArrayList<Transition> transitions) {
		// <Arc: NORMAL; P0(0) -> T3(0); 1>
		String backup = line;
		try {
			line = line.replace(" ", "");
			String[] tab = line.split(";");
			
			String typeLine = tab[0];
			TypesOfArcs arcType = TypesOfArcs.NORMAL;
			if(typeLine.contains("READARC"))
				arcType = TypesOfArcs.READARC;
			else if(typeLine.contains("INHIBITOR"))
				arcType = TypesOfArcs.INHIBITOR;
			else if(typeLine.contains("RESET"))
				arcType = TypesOfArcs.RESET;
			else if(typeLine.contains("EQUAL"))
				arcType = TypesOfArcs.EQUAL;
			
			tab[2] = tab[2].replace(">", "");
			int weight = Integer.parseInt(tab[2]);
			
			String arcData = tab[1];
			boolean placeFirst = true;
			if(arcData.indexOf("T")==0)
				placeFirst = false;
			
			arcData = arcData.replace("P", "");
			arcData = arcData.replace("T", "");
			arcData = arcData.replace(")->", " ");
			arcData = arcData.replace(")", "");
			arcData = arcData.replace("(", " ");
			String[] arcDataTable = arcData.split(" ");
			
			int placeIndex = -1;
			int transIndex = -1;
			int placeElLoc = -1;
			int transElLoc = -1;
			if(placeFirst) {
				placeIndex = Integer.parseInt(arcDataTable[0]);
				placeElLoc = Integer.parseInt(arcDataTable[1]);
				transIndex = Integer.parseInt(arcDataTable[2]);
				transElLoc = Integer.parseInt(arcDataTable[3]);
				
				ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
				ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
				Arc newArc = new Arc(pEL, tEL, "", weight, arcType);
				//places.get(placeIndex).getElementLocations().get(placeElLoc).addOutArc(newArc);
				//transitions.get(transIndex).getElementLocations().get(transElLoc).addInArc(newArc);
				
				return newArc;
			} else {
				placeIndex = Integer.parseInt(arcDataTable[2]);
				placeElLoc = Integer.parseInt(arcDataTable[3]);
				transIndex = Integer.parseInt(arcDataTable[0]);
				transElLoc = Integer.parseInt(arcDataTable[1]);
				
				ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
				ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
				Arc newArc = new Arc(tEL, pEL, "", weight, arcType);
				//transitions.get(transIndex).getElementLocations().get(transElLoc).addOutArc(newArc);
				//places.get(placeIndex).getElementLocations().get(placeElLoc).addInArc(newArc);
				
				return newArc;
			}
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading file error in line: "+backup, "error", true);
		}
		return null;
	}

	/**
	 * Metoda pomocnicza czytająca z pliku projektu blok danych o inwariantach.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli wszystko się udało
	 */
	private boolean readInvariants(BufferedReader buffer) {
		try {
			String line = "";
			while(!((line = buffer.readLine()).contains("<Invariants data>"))) //przewiń do inwariantów
				;
			
			line = buffer.readLine();
			int transNumber = projectCore.getTransitions().size();
			String problemWithInv = "";
			int problems = 0;
			int readedLine = -1;
			
			if(!line.contains("<Invariants: 0>")) { //są miejsca
				boolean go = true;
				invariantsMatrix = new ArrayList<ArrayList<Integer>>();
				
				line = buffer.readLine();
				while(go) {
					readedLine++;
					ArrayList<Integer> invariant = new ArrayList<Integer>();
					line = line.replace(" ", "");
					String[] tab = line.split(";");
					
					for(int i=1; i<tab.length; i++) {
						invariant.add(Integer.parseInt(tab[i]));
					}

					line = buffer.readLine();
					if(line.contains("<EOI>")) {
						go = false;
					}
					
					if(invariant.size() == transNumber) {
						invariantsMatrix.add(invariant);
						invariantsProcessed++;
					} else {
						problems++;
						problemWithInv += readedLine+",";
					}
				}
				
				projectCore.setInvariantsMatrix(invariantsMatrix, false);
				
				if(problems==0) {
					while(!((line = buffer.readLine()).contains("<Invariants names>"))) //przewiń do nazw inwariantów
						;
					
					invariantsNames = new ArrayList<String>();
					line = buffer.readLine();
					int readLines = 1;
					go = true;
					while(go) {
						line = line.replace(" ", "");
						invariantsNames.add(line);
						
						line = buffer.readLine();
						if(line.contains("<EOIN>")) {
							go = false;
						} else {
							readLines++;
						}
					}
					projectCore.setInvariantsNames(invariantsNames);
					
					
					if(readLines != invariantsMatrix.size()) {
						GUIManager.getDefaultGUIManager().log("Error: different numbers of invariants ("+invariantsMatrix.size()+
								") and their names ("+readLines+"). Operation failed.", "error", true);
						return false;
					}
					
				} else {
					GUIManager.getDefaultGUIManager().log("Invariants with wrong number of elements in file:"+problemWithInv, "error", true);
					return false;
				}
			}
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading invariants failed for invariant number: \n"+invariantsProcessed, "error", true);
			return false;
		}
	}
	
	private boolean readMCT(BufferedReader buffer) {
		try {
			String line = "";
			while(!((line = buffer.readLine()).contains("<MCT data>"))) //przewiń do zbiorów MCT
				;
			
			line = buffer.readLine();
			ArrayList<Transition> transitions = projectCore.getTransitions();
			int transNumber = transitions.size();
			String problemWithMCTLines = "";
			int problems = 0;
			int readedLine = -1;
			
			if(!line.contains("<MCT: 0>")) { //są miejsca
				boolean go = true;
				mctData = new ArrayList<ArrayList<Transition>>();
				
				line = buffer.readLine();
				while(go) {
					readedLine++;
					ArrayList<Transition> mct = new ArrayList<Transition>();
					line = line.replace(" ", "");
					String[] tab = line.split(";");
					
					for(int i=0; i<tab.length; i++) {
						int mctNumber = Integer.parseInt(tab[i]);
						if(mctNumber < transNumber) {
							mct.add(transitions.get(mctNumber));
						} else {
							problems++;
							problemWithMCTLines += readedLine+",";
						}
					}

					line = buffer.readLine();
					if(line.contains("<EOM>")) {
						go = false;
					}
					mctData.add(mct);
					mctProcessed++;
				}
				projectCore.setMCTMatrix(mctData, false);
				
				if(problems==0) {
					while(!((line = buffer.readLine()).contains("<MCT names>"))) //przewiń do nazw inwariantów
						;
					
					mctNames = new ArrayList<String>();
					line = buffer.readLine();
					int readLines = 1;
					go = true;
					while(go) {
						line = line.replace(" ", "");
						mctNames.add(line);
						
						line = buffer.readLine();
						if(line.contains("<EOIMn>")) {
							go = false;
						} else {
							readLines++;
						}
					}
					projectCore.setMCTNames(mctNames);
					
					
					if(readLines != mctData.size()) {
						GUIManager.getDefaultGUIManager().log("Error: different numbers of MCT sets ("+mctData.size()+
								") and their names ("+readLines+"). Operation failed.", "error", true);
						return false;
					}
					
				} else {
					GUIManager.getDefaultGUIManager().log("MCT with wrong number ID numbers for their transitions in file:"+problemWithMCTLines, "error", true);
					return false;
				}
			}
			
			
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading MCT sets failed for MCT number: \n"+mctProcessed, "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda pomocnicza, służąca do aktualizacji współrzędnych x i y elementów sieci - zapamiętuje 
	 * największe wartości do tej pory przeczytane.
	 * @param x int - współrzędna x
	 * @param y int - współrzędna y
	 */
	private void setGlobalXY(int x, int y) {
		if(x > globalMaxWidth)
			globalMaxWidth = x;
		if(y > globalMaxHeight)
			globalMaxHeight = y;
	}
	
	/**
	 * Metoda ustalająca wymiary panelu graficznego sieci, w oparciu o aktualne wartości x i y (związana
	 * z metodą setGlobalXY(...).
	 */
	private void setGraphPanelSize() {
		int nodeSID = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().size() - 1;
		int SIN = GUIManager.getDefaultGUIManager().IDtoIndex(nodeSID);
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(SIN).getGraphPanel();
		graphPanel.setSize(new Dimension(globalMaxWidth+300, globalMaxHeight+200));
		graphPanel.setOriginSize(graphPanel.getSize());
		graphPanel.repaint();
	}
}
