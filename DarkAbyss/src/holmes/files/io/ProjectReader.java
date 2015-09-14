package holmes.files.io;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.FiringRatesManager;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.PlacesStateVector;
import holmes.petrinet.data.StatesManager;
import holmes.petrinet.data.TransFiringRateVector;
import holmes.petrinet.data.TransFiringRateVector.FRContainer;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypesOfArcs;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.Transition.StochaticsType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.functions.FunctionsTools;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;

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
	private ArrayList<MetaNode> metanodes = null;
	private ArrayList<Arc> arcs = null;
	private ArrayList<ArrayList<Integer>> t_invariantsMatrix = null;
	private ArrayList<String> t_invariantsNames = null;
	private ArrayList<ArrayList<Integer>> p_invariantsMatrix = null;
	private ArrayList<String> p_invariantsNames = null;
	private ArrayList<ArrayList<Transition>> mctData = null;
	private ArrayList<String> mctNames = null;

	private int placesProcessed = 0;
	private int transitionsProcessed = 0;
	private int metanodesProcessed = 0;
	private int arcsProcessed = 0;
	private int t_invariantsProcessed = 0;
	private int p_invariantsProcessed = 0;
	private int mctProcessed = 0;
	
	private int globalMaxHeight = 0;
	private int globalMaxWidth = 0;
	
	//które bloki w ogóle próbowac czytać (zależne od tagów w sekcji [Project blocks]
	private boolean subnets = false; //bloki coarse
	private boolean states = false;
	private boolean functions = false;
	private boolean firingRates = false;
	private boolean pInvariants = false;

	
	/**
	 * Konstruktor obiektu klasy odczytywania projektu.
	 */
	public ProjectReader() {
		projectCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		nodes = projectCore.getNodes();
		metanodes = projectCore.getMetaNodes();
		arcs = projectCore.getArcs();
		t_invariantsMatrix = projectCore.getT_InvMatrix();
		t_invariantsNames = projectCore.accessT_InvDescriptions();
		p_invariantsMatrix = projectCore.getP_InvMatrix();
		p_invariantsNames = projectCore.accessP_InvDescriptions();
		mctData = projectCore.getMCTMatrix();
		mctNames = projectCore.accessMCTnames();
		
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
			
			status = readProjectHeader(buffer);
			if(status == false) {
				GUIManager.getDefaultGUIManager().log("Reading project data block failure.", "error", true);
				buffer.close();
				return false;
			}
			
			status = readNetwork(buffer);
			if(status == false) {
				GUIManager.getDefaultGUIManager().log("Reading network data block failure. Invariants/MCT reading cancelled. Terminating operation.", "error", true);
				buffer.close();
				return false;
			}
			
			status = readTInvariants(buffer);
			if(!status) {
				projectCore.setT_InvMatrix(null, false);
			} else {
				GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(projectCore.getT_InvMatrix());
			}
			
			if(pInvariants) {
				status = readPInvariants(buffer);
				if(!status) {
					projectCore.setP_InvMatrix(null);
				} else {
					//GUIManager.getDefaultGUIManager().getInvariantsBox().showInvariants(projectCore.getP_InvMatrix());
				}
			}
			
			status = readMCT(buffer);
			if(!status) {
				projectCore.setMCTMatrix(null, false);
			} else {
				GUIManager.getDefaultGUIManager().getMctBox().showMCT(projectCore.getMCTMatrix());
			}
			
			if(states) {
				status = readStates(buffer);
				if(!status) {
					projectCore.accessStatesManager().createCleanState();
				}
			} else {
				projectCore.accessStatesManager().createCleanState();
			}
			
			if(firingRates) {
				status = readFiringRates(buffer);
				if(!status) {
					projectCore.accessFiringRatesManager().createCleanFRVector();
				}
			} else {
				projectCore.accessFiringRatesManager().createCleanFRVector();
			}
			
			GUIManager.getDefaultGUIManager().subnetsGraphics.addRequiredSheets();
			GUIManager.getDefaultGUIManager().getWorkspace().setSelectedDock(0);
			buffer.close();
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading project file failed.", "error", true);
			return false;
		}
	}

	/**
	 * Metoda służąca do czytania bloku składowych pliku projektu.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli się udało
	 */
	private boolean readProjectHeader(BufferedReader buffer) {
		try {
			String line = buffer.readLine();
			if(line.contains("Project name")) {
				line = line.substring(line.indexOf("name:")+6);
				projectCore.setName(line);
			} else {
				GUIManager.getDefaultGUIManager().log("No project name tag in file.", "error", true);
				return false;
			}
			
			line = buffer.readLine();
			if(line.contains("Date:")) {
				//line = line.substring(line.indexOf("name:")+6);
				//projectCore.setName(line);
			} else {
				GUIManager.getDefaultGUIManager().log("No project date tag in file.", "error", true);
				return false;
			}
			
			line = buffer.readLine();
			if(line.contains("<Project blocks>")) {
				while(!((line = buffer.readLine()).contains("</Project blocks>"))) {
					parseNetblocksLine(line);
				}
			} 
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Uknown error while reading project header.", "error", true);
			return false;
		}
	}

	/**
	 * Metoda czyta blok informujący jakie podbloki są w ogóle w pliku projektu.
	 * @param line String - linia do czytania
	 */
	private void parseNetblocksLine(String line) {
		String backup = line;
		String query = "";
		try {
			query = "subnets";
			if(line.toLowerCase().contains(query)) {
				subnets = true;
				return;
			}
			query = "statesmatrix";
			if(line.toLowerCase().contains(query)) {
				states = true;
				return;
			}
			query = "functions";
			if(line.toLowerCase().contains(query)) {
				functions = true;
				return;
			}
			query = "firingratesdata";
			if(line.toLowerCase().contains(query)) {
				firingRates = true;
				return;
			}
			query = "placeinvdata";
			if(line.toLowerCase().contains(query)) {
				pInvariants = true;
				return;
			}
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading error in line: "+backup, "error", true);
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
			String line; // = buffer.readLine();
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

			//TRANSITIONS:
			while(!((line = buffer.readLine()).contains("<Transitions: "))) //przewiń do tranzycji
				;
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
			
			if(subnets) {
				//METANODES:
				while(!((line = buffer.readLine()).contains("<MetaNodes: "))) //przewiń do tranzycji
					;
				if(!line.contains("<MetaNodes: 0>")) { //są tranzycje
					line = buffer.readLine(); // -> Transition: 0
					boolean go = true;
					
					while(go) {
						MetaNode metanode = new MetaNode(0, IdGenerator.getNextId(), new Point(20,20), MetaType.SUBNET);
						
						while(!((line = buffer.readLine()).contains("<EOT>"))) {
							parseMetaNodesLine(line, metanode);
						}
						line = buffer.readLine();
						if(line.contains("<MetaNodes data block end>")) {
							go = false;
						}
						metanodesProcessed++;
						metanodes.add(metanode);
						nodes.add(metanode);
					}
					//przeczytano tranzycje
				}
			}

			ArrayList<Place> places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
			ArrayList<Transition> transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();

			//ARCS:
			while(!((line = buffer.readLine()).contains("<Arcs data block>"))) //przewiń do łuków
				;
			while(!(line = buffer.readLine()).contains("Arcs data block end")) {
				Arc newArc = parseArcLine(line, places, transitions, metanodes);
				if(newArc != null) {
					arcsProcessed++;
					arcs.add(newArc);
				}
			}
			
			for(Transition transition : transitions) { //aktywacja wektorów funkcji
				transition.checkFunctions(arcs, places);
			}
			
			int functionsRead = 0;
			int functionsFailed = 0;
			if(functions) {
				while(!((line = buffer.readLine()).contains("<Functions data block>"))) //przewiń do funkcji
					;
				
				while(!((line = buffer.readLine()).contains("<Functions data block end>"))) {
					boolean fReadStatus = parseFunction(line, transitions, places);
					if(fReadStatus)
						functionsRead++;
					else
						functionsFailed++;
				}
			}
			HolmesNotepad notepad = new HolmesNotepad(640, 480);
			notepad.setVisible(true);
			boolean errStatus = FunctionsTools.validateFunctionNet(notepad, places);
			if(errStatus)
				notepad.setVisible(true);
			else
				notepad.dispose();
			
			GUIManager.getDefaultGUIManager().log("Read "+placesProcessed+" places, "+transitionsProcessed+ 
					" transitions, "+arcsProcessed+" arcs, "+functionsRead+" functions.", "text", true);
			if(functionsFailed > 0)
				GUIManager.getDefaultGUIManager().log("Failed to correctly parse "+functionsFailed+" functions.", "error", true);
			
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
	 * Metoda odpowiedzialna za odczyt linii funkcji.
	 * @param functionLine String - przeczytana linia
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 * @param places ArrayList[Place] - wektor miejsc
	 * @return boolean - true, jeśli się udało
	 */
	private boolean parseFunction(String functionLine, ArrayList<Transition> transitions, ArrayList<Place> places) {
		try {
			//TODO:
			String[] table = functionLine.split(";");
			String transNumber = table[0].replace("<T", "");
			
			int transIndex = Integer.parseInt(transNumber.trim());
			Transition transition = transitions.get(transIndex);
			
			boolean correct = false;
			boolean enabled = false;
			if(table[3].contains("true"))
				correct = true;
			if(table[4].contains("true"))
				enabled = true;
			
			boolean status = transition.updateFunctionString(table[1], table[2], correct, enabled);
	
			return status;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Failed to correctly parse line: "+functionLine, "warning", true);
			return false;
		}
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
				transition.setEFT(eft);
				return;
			}
			
			query = "Transition lft:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				double lft = Double.parseDouble(line);
				transition.setLFT(lft);
				return;
			}
			
			query = "Transition duration:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				double duration = Double.parseDouble(line);
				transition.setDPNduration(duration);
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
			
			query = "Transition function flag:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true"))
					transition.setFunctional(true);
				else
					transition.setFunctional(false);
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
	
	/**
	 * Metoda analizująca przeczytaną linię pliku opisującą meta-węzeł sieci i wprowadzająca odpowiednie zmiany
	 * do tworzonego obiektu.
	 * @param line String - przeczytana linia
	 * @param metanode MetaNode - modyfikowany obiekt tranzycji
	 */
	private void parseMetaNodesLine(String line, MetaNode metanode) {
		String backup = line;
		try {
			String query = "";
			query = "MetaNode gID:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				return;
			}
			
			query = "MetaNode type:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				
				if(line.equals("SUBNETTRANS")) {
					metanode.setMetaType(MetaType.SUBNETTRANS);
				} else if(line.equals("SUBNETPLACE")) {
					metanode.setMetaType(MetaType.SUBNETPLACE);
				} else if(line.equals("SUBNET")) {
					metanode.setMetaType(MetaType.SUBNET);
				} else {
					metanode.setMetaType(MetaType.UNKNOWN);
				} 

				return;
			}
			
			query = "MetaNode name:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				metanode.setName(line);
				return;
			}
			
			query = "MetaNode comment:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				line = Tools.decodeString(line);
				metanode.setComment(line);
				return;
			}
			
			query = "MetaNode representedSheet:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				int sheetR = Integer.parseInt(line);
				metanode.setRepresentedSheetID(sheetR);
				return;
			}
			
			query = "MetaNode locations:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				int elLocSize = Integer.parseInt(line);
				for(int e=0; e<elLocSize-1; e++) {
					metanode.getElementLocations().add(new ElementLocation(0, new Point(20,20), metanode));
					metanode.getNamesLocations().add(new ElementLocation(0, new Point(0,0), metanode));
				}
				
				return;
			}
			//poniższa część MUSI się wywołać PO tej wyżej, inaczej nie będzie odpowiednio dużo pól ElementLocation!
			
			query = "MetaNode location data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);
				
				metanode.getElementLocations().get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				metanode.getElementLocations().get(eLocIndex).forceSetPosition(newP);
				metanode.getElementLocations().get(eLocIndex).setNotSnappedPosition(newP);

				setGlobalXY(pointX, pointY); //update graph panel
				
				return;
			}
			query = "MetaNode name offset data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);
				
				metanode.getNamesLocations().get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				metanode.getNamesLocations().get(eLocIndex).forceSetPosition(newP);
				metanode.getNamesLocations().get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading file error in line: "+backup+" for MetaNode "+metanodesProcessed, "error", true);
		}
	}
	
	/**
	 * Metoda czytające dane o łukach w pliku projektu.
	 * @param line String - ostatnio przeczytana linia
	 * @param places ArrayList[Place] - wektor miejsc
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 * @param metanodes ArrayList[MetaNode] - wektor meta-węzłów
	 * @return Arc - obiekt łuku
	 */
	private Arc parseArcLine(String line, ArrayList<Place> places, ArrayList<Transition> transitions, ArrayList<MetaNode> metanodes) {
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
			else if(typeLine.contains("META_ARC"))
				arcType = TypesOfArcs.META_ARC;
			
			tab[2] = tab[2].replace(">", "");
			int weight = Integer.parseInt(tab[2]);
			
			String arcData = tab[1];
			boolean placeFirst = false;
			boolean metaFirst = false;
			boolean metaSecond = false;
			boolean isThereTransition = false;
			if(arcData.indexOf("P")==0)
				placeFirst = true;
			
			if(arcData.indexOf("M") > 0) {
				metaSecond = true;
				metaFirst = false;
			}
			if(arcData.indexOf("M") == 0) {
				metaFirst = true;
				metaSecond = false;
			}
			
			if(arcData.indexOf("T") != -1)
				isThereTransition = true;
			
			arcData = arcData.replace("P", "");
			arcData = arcData.replace("T", "");
			arcData = arcData.replace("M", "");
			arcData = arcData.replace(")->", " ");
			arcData = arcData.replace(")", "");
			arcData = arcData.replace("(", " ");
			String[] arcDataTable = arcData.split(" ");
			
			int placeIndex = -1;
			int transIndex = -1;
			int metaIndex = -1;
			int placeElLoc = -1;
			int transElLoc = -1;
			int metaElLoc = -1;
			
			if(backup.contains("<Arc: NORMAL; T21(0) -> P34(0); 1>")) {
				@SuppressWarnings("unused")
				boolean error = true;
			}
			
			if(placeFirst) { //pierwsze jest miejsce
				if(metaSecond == false) {
					placeIndex = Integer.parseInt(arcDataTable[0]);
					placeElLoc = Integer.parseInt(arcDataTable[1]);
					transIndex = Integer.parseInt(arcDataTable[2]);
					transElLoc = Integer.parseInt(arcDataTable[3]);
					
					ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
					ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
					Arc newArc = new Arc(pEL, tEL, "", weight, arcType);
					
					if(arcType == TypesOfArcs.META_ARC) {
						@SuppressWarnings("unused")
						int error = 1;
					}
					
					return newArc;
				} else { //metaSecond == true
					placeIndex = Integer.parseInt(arcDataTable[0]);
					placeElLoc = Integer.parseInt(arcDataTable[1]);
					metaIndex = Integer.parseInt(arcDataTable[2]);
					metaElLoc = Integer.parseInt(arcDataTable[3]);
					
					ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
					ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
					Arc newArc = new Arc(pEL, mEL, "", weight, arcType);
					
					if(arcType != TypesOfArcs.META_ARC) {
						@SuppressWarnings("unused")
						int error = 1;
					}
					
					return newArc;
				}
			} else { //placeFirst = false
				if(metaFirst == true) { 
					if(isThereTransition == true) { //druga jest tranzycja
						metaIndex = Integer.parseInt(arcDataTable[0]);
						metaElLoc = Integer.parseInt(arcDataTable[1]);
						transIndex = Integer.parseInt(arcDataTable[2]);
						transElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
						ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
						Arc newArc = new Arc(mEL, tEL, "", weight, arcType);
						return newArc;
					} else { //drugie jest miejsce
						metaIndex = Integer.parseInt(arcDataTable[0]);
						metaElLoc = Integer.parseInt(arcDataTable[1]);
						placeIndex = Integer.parseInt(arcDataTable[2]);
						placeElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
						ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
						Arc newArc = new Arc(mEL, pEL, "", weight, arcType);
						return newArc;
					}
				} else { //placesFirst = false, metaFirst = false -> pierwsza jest tranzycja
					if(metaSecond == true) { //drugi jest meta węzeł
						transIndex = Integer.parseInt(arcDataTable[0]);
						transElLoc = Integer.parseInt(arcDataTable[1]);
						metaIndex = Integer.parseInt(arcDataTable[2]);
						metaElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
						ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
						Arc newArc = new Arc(tEL, mEL, "", weight, arcType);
						return newArc;
					} else { //drugie jest miejsce
						transIndex = Integer.parseInt(arcDataTable[0]);
						transElLoc = Integer.parseInt(arcDataTable[1]);
						placeIndex = Integer.parseInt(arcDataTable[2]);
						placeElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
						ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
						Arc newArc = new Arc(tEL, pEL, "", weight, arcType);
						return newArc;
					}
				}
			}
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading file error in line: "+backup, "error", true);
		}
		return null;
	}

	/**
	 * Metoda pomocnicza czytająca z pliku projektu blok danych o t-inwariantach.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli wszystko się udało
	 */
	private boolean readTInvariants(BufferedReader buffer) {
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
				t_invariantsMatrix = new ArrayList<ArrayList<Integer>>();
				
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
						t_invariantsMatrix.add(invariant);
						t_invariantsProcessed++;
					} else {
						problems++;
						problemWithInv += readedLine+",";
					}
				}
				
				projectCore.setT_InvMatrix(t_invariantsMatrix, false);
				
				if(problems==0) {
					while(!((line = buffer.readLine()).contains("<Invariants names>"))) //przewiń do nazw inwariantów
						;
					
					t_invariantsNames = new ArrayList<String>();
					line = buffer.readLine();
					int readLines = 1;
					go = true;
					while(go) {
						line = line.trim();
						line = Tools.decodeString(line);
						t_invariantsNames.add(line);
						
						line = buffer.readLine();
						if(line.contains("<EOIN>")) {
							go = false;
						} else {
							readLines++;
						}
					}
					projectCore.setT_InvDescriptions(t_invariantsNames);
					
					
					if(readLines != t_invariantsMatrix.size()) {
						GUIManager.getDefaultGUIManager().log("Error: different numbers of t-invariants ("+t_invariantsMatrix.size()+
								") and their names ("+readLines+"). Operation failed.", "error", true);
						return false;
					}
					
				} else {
					GUIManager.getDefaultGUIManager().log("T-invariants with wrong number of elements in file:"+problemWithInv, "error", true);
					return false;
				}
			}
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading invariants failed for t-invariant number: \n"+t_invariantsProcessed, "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda pomocnicza czytająca z pliku projektu blok danych o p-inwariantach.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli wszystko się udało
	 */
	private boolean readPInvariants(BufferedReader buffer) {
		try {
			String line = "";
			while(!((line = buffer.readLine()).contains("<PlaceInv data>"))) //przewiń do inwariantów
				;
			
			line = buffer.readLine();
			int placeNumber = projectCore.getPlaces().size();
			String problemWithInv = "";
			int problems = 0;
			int readedLine = -1;
			
			if(!line.contains("<PInvariants: 0>")) { //są miejsca
				boolean go = true;
				p_invariantsMatrix = new ArrayList<ArrayList<Integer>>();
				
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
					if(line.contains("<EOPI>")) {
						go = false;
					}
					
					if(invariant.size() == placeNumber) {
						p_invariantsMatrix.add(invariant);
						p_invariantsProcessed++;
					} else {
						problems++;
						problemWithInv += readedLine+",";
					}
				}
				
				projectCore.setP_InvMatrix(p_invariantsMatrix);
				
				if(problems==0) {
					while(!((line = buffer.readLine()).contains("<PInvariants names>"))) //przewiń do nazw inwariantów
						;
					
					p_invariantsNames = new ArrayList<String>();
					line = buffer.readLine();
					int readLines = 1;
					go = true;
					while(go) {
						line = line.trim();
						line = Tools.decodeString(line);
						p_invariantsNames.add(line);
						
						line = buffer.readLine();
						if(line.contains("<EOPIN>")) {
							go = false;
						} else {
							readLines++;
						}
					}
					projectCore.setP_InvDescriptions(p_invariantsNames);
					
					
					if(readLines != p_invariantsMatrix.size()) {
						GUIManager.getDefaultGUIManager().log("Error: different numbers of p-invariants ("+p_invariantsMatrix.size()+
								") and their names ("+readLines+"). Operation failed.", "error", true);
						return false;
					}
					
				} else {
					GUIManager.getDefaultGUIManager().log("P-invariants with wrong number of elements in file:"+problemWithInv, "error", true);
					return false;
				}
			}
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading p-invariants failed for invariant number: \n"+p_invariantsProcessed, "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda czyta blok danych o zbiorach MCT.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli wszystko dobrze poszło
	 */
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
						line = line.trim();
						line = Tools.decodeString(line);
						mctNames.add(line);
						
						line = buffer.readLine();
						if(line.contains("<EOIMn>") || line.contains("<EOMn>")) {
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
	 * Metoda czyta blok danych o stanach sieci.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli wszystko dobrze poszło
	 */
	private boolean readStates(BufferedReader buffer) {
		try {
			String line = "";
			while(!((line = buffer.readLine()).contains("<States data>"))) //przewiń do zbiorów MCT
				;
			
			line = buffer.readLine();
			int problems = 0;
			int readedLine = 0;
			
			boolean go = true;
			StatesManager statesMngr = projectCore.accessStatesManager();
			statesMngr.reset(true);

			line = buffer.readLine();
			try {
				while(go) {
					readedLine++;
					PlacesStateVector pVector = new PlacesStateVector();
					line = line.replace(" ", "");
					String[] tab = line.split(";");
					
					for(int i=0; i<tab.length; i++) {
						pVector.accessVector().add(Double.parseDouble(tab[i]));
					}

					line = buffer.readLine();
					if(line.contains("<EOSt>")) {
						go = false;
					}
					
					statesMngr.accessStateMatrix().add(pVector);
				}
			} catch (Exception e) {}
			
			if(readedLine > statesMngr.accessStateMatrix().size()) {
				GUIManager.getDefaultGUIManager().log("Error reading state vector number "+(readedLine), "error", true);
				if(statesMngr.accessStateMatrix().size() == 0) {
					statesMngr.createCleanState();
					problems = 1;
				}
			}

			if(problems==0) {
				while(!((line = buffer.readLine()).contains("<States names>"))) //przewiń do nazw stanów
					;
				
				ArrayList<String> statesNames = new ArrayList<String>();
				line = buffer.readLine();
				go = true;
				while(go) {
					//line = line.replace(" ", "");
					line = line.trim();
					line = Tools.decodeString(line);
					
					if(statesNames.size() < statesMngr.accessStateMatrix().size())
						statesNames.add(line);
					
					line = buffer.readLine();
					if(line.contains("<EOStn>")) {
						go = false;
					}
				}
				statesMngr.accessStateNames().addAll(statesNames);
		
			} else {
				GUIManager.getDefaultGUIManager().log("Problems reading state vectors", "error", true);
				return false;
			}
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading state vectors failed.", "error", true);
			return false;
		}
	}
	
	/**
	 * Czyta dane wektorów odpaleń tranzycji w modelu SPN.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli wszystko poszło ok.
	 */
	private boolean readFiringRates(BufferedReader buffer) {
		try {
			String line = "";
			while(!((line = buffer.readLine()).contains("<Firing rates data>"))) //przewiń do zbiorów MCT
				;
			
			line = buffer.readLine();
			int problems = 0;
			int readedLine = 0;
			
			boolean go = true;
			FiringRatesManager frateMngr = projectCore.accessFiringRatesManager();
			frateMngr.reset(true);

			line = buffer.readLine();
			try {
				while(go) {
					readedLine++;
					TransFiringRateVector frVector = new TransFiringRateVector();
					line = line.replace(" ", "");
					String[] tabFR = line.split(";");
					
					line = buffer.readLine();//typ tranzycji
					line = line.replace(" ", "");
					String[] tabType = line.split(";");
					
					ArrayList<FRContainer> dataVector = new ArrayList<FRContainer>();
					for(int i=0; i<tabFR.length; i++) {
						StochaticsType subType = StochaticsType.ST;
						if(tabType[i].equals("DT"))
							subType = StochaticsType.DT;
						else if(tabType[i].equals("IM"))
							subType = StochaticsType.IM;
						else if(tabType[i].equals("SchT"))
							subType = StochaticsType.SchT;
						
						FRContainer frc = frVector.newContainer(Double.parseDouble(tabFR[i]), subType);

						dataVector.add(frc);
					}
					frVector.accessVector().addAll(dataVector); //boxing

					line = buffer.readLine();
					if(line.contains("<EOFRv>")) {
						go = false;
					}
					
					frateMngr.accessFRMatrix().add(frVector); //boxing in manager
				}
			} catch (Exception e) {
				GUIManager.getDefaultGUIManager().log("Operation failed, wrong firing rates data "+(readedLine), "error", true);
				if(frateMngr.accessFRMatrix().size() == 0) {
					frateMngr.createCleanFRVector();
					problems = 1;
				}
			}
			
			if((readedLine/2) > frateMngr.accessFRMatrix().size()) {
				GUIManager.getDefaultGUIManager().log("Operation failed, wrong firing rates data "+(readedLine), "error", true);
				if(frateMngr.accessFRMatrix().size() == 0) {
					frateMngr.createCleanFRVector();
					problems = 1;
				} else {
					frateMngr.reset(false); //false!!!
					frateMngr.createCleanFRVector();
					problems = 1;
				}
			}

			if(problems==0) {
				while(!((line = buffer.readLine()).contains("<Firing rates vector names>"))) //przewiń do nazw fr
					;
				
				ArrayList<String> frNames = new ArrayList<String>();
				line = buffer.readLine();
				go = true;
				while(go) {
					line = line.trim();
					line = Tools.decodeString(line);
					if(frNames.size() < frateMngr.accessFRMatrix().size())
						frNames.add(line);
					
					line = buffer.readLine();
					if(line.contains("<EOFRVn>")) {
						go = false;
					}
				}
				frateMngr.accessFRVectorsNames().addAll(frNames);
		
			} else {
				GUIManager.getDefaultGUIManager().log("Problems reading firing rates vector names.", "error", true);
				return false;
			}
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading firing rates vectors failed.", "error", true);
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
	@SuppressWarnings("unused")
	private void setGraphPanelSize() {
		int nodeSID = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().size() - 1;
		int SIN = GUIManager.getDefaultGUIManager().IDtoIndex(nodeSID);
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(SIN).getGraphPanel();
		graphPanel.setSize(new Dimension(globalMaxWidth+300, globalMaxHeight+200));
		graphPanel.setOriginSize(graphPanel.getSize());
		graphPanel.repaint();
	}
}
