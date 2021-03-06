package holmes.files.io;

import java.awt.Dimension;
import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.SPNdataVectorManager;
import holmes.petrinet.data.SPNtransitionData;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SSAplacesManager;
import holmes.petrinet.data.SSAplacesVector;
import holmes.petrinet.data.SSAplacesVector.SSAdataType;
import holmes.petrinet.data.StatePlacesVector;
import holmes.petrinet.data.StatePlacesManager;
import holmes.petrinet.data.SPNdataVector;
import holmes.petrinet.data.SPNdataVector.SPNvectorSuperType;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.StochaticsType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.functions.FunctionsTools;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;

/**
 * Metoda czytająca plik danych projektu.
 * 
 * @author MR
 */
public class ProjectReader {
	private GUIManager overlord = null;
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
	private boolean ssaData = false;

	
	/**
	 * Konstruktor obiektu klasy odczytywania projektu.
	 */
	public ProjectReader() {
		overlord = GUIManager.getDefaultGUIManager();
		projectCore = overlord.getWorkspace().getProject();
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

	public ProjectReader(boolean isLabelCompariso) {
		overlord = GUIManager.getDefaultGUIManager();
		projectCore = new PetriNet(null,"test");
		nodes = new ArrayList<>();
		arcs = new ArrayList<>();
		placesProcessed = 0;
		transitionsProcessed = 0;
		arcsProcessed = 0;
	}

	public boolean readProjectForLabelComparison(String filepath) {
		boolean status = true;

		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));

			overlord.log("Reading project file: " + filepath, "text", true);

			status = readProjectHeader(buffer);
			if (status == false) {
				overlord.log("Reading project data block failure.", "error", true);
				buffer.close();
				return false;
			}

			status = readNetwork(buffer,true);
			if (status == false) {
				overlord.log("Reading network data block failure. Invariants/MCT reading cancelled. Terminating operation.", "error", true);
				buffer.close();
				return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return status;
	}

	/**
	 * Metoda odpowiedzialna za odczyt danych projektu, do którego ścieżka podana jest jako parametr. 
	 * @param filepath String - pełna ścieżka do pliku projektu
	 * @return boolean - true, jeśli operacja się powiodła
	 */
	public boolean readProject(String filepath) {
		boolean status = overlord.reset.newProjectInitiated();
		if(status == false) {
			return false;
		}
		
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
			
			overlord.log("Reading project file: "+filepath, "text", true);
			
			status = readProjectHeader(buffer);
			if(status == false) {
				overlord.log("Reading project data block failure.", "error", true);
				buffer.close();
				return false;
			}
			
			status = readNetwork(buffer,false);
			if(status == false) {
				overlord.log("Reading network data block failure. Invariants/MCT reading cancelled. Terminating operation.", "error", true);
				buffer.close();
				return false;
			}
			
			status = readTInvariants(buffer);
			if(!status) {
				projectCore.setT_InvMatrix(null, false);
			} else {
				overlord.getT_invBox().showT_invBoxWindow(projectCore.getT_InvMatrix());
			}

			//overlord.getDecompositionBox().showDecompositionBoxWindows();
			
			if(pInvariants) {
				status = readPInvariants(buffer);
				if(!status) {
					projectCore.setP_InvMatrix(null);
				} else {
					overlord.getP_invBox().showP_invBoxWindow(projectCore.getP_InvMatrix());
				}
			}
			
			status = readMCT(buffer);
			if(!status) {
				projectCore.setMCTMatrix(null, false);
			} else {
				overlord.getMctBox().showMCT(projectCore.getMCTMatrix());
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
					projectCore.accessFiringRatesManager().createCleanSPNdataVector();
				}
			} else {
				projectCore.accessFiringRatesManager().createCleanSPNdataVector();
			}
			
			if(ssaData) {
				status = readSSAvectors(buffer);
				if(!status) {
					projectCore.accessSSAmanager().createCleanSSAvector();
				}
			} else {
				projectCore.accessSSAmanager().createCleanSSAvector();
			}
			
			overlord.subnetsGraphics.addRequiredSheets();
			overlord.getWorkspace().setSelectedDock(0);
			buffer.close();
			return true;
		} catch (Exception e) {
			overlord.log("Reading project file failed.", "error", true);
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
				overlord.log("No project name tag in file.", "error", true);
				return false;
			}
			
			line = buffer.readLine();
			if(line.contains("Date:")) {
				//line = line.substring(line.indexOf("name:")+6);
				//projectCore.setName(line);
			} else {
				overlord.log("No project date tag in file.", "error", true);
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
			overlord.log("Uknown error while reading project header.", "error", true);
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
			query = "ssamatrix";
			if(line.toLowerCase().contains(query)) {
				ssaData = true;
				return;
			}
		} catch (Exception e) {
			overlord.log("Reading error in line: "+backup, "error", true);
		}
	}

	/**
	 * Metoda odczytująca dane sieci z pliku projektu i tworząca sieć na bazie miejsc, tranzycji i łuków.
	 * @param buffer BufferedReader - obiekt odczytujący
	 * @return boolean - true, jeśli nie było problemów przy odczycie
	 */
	private boolean readNetwork(BufferedReader buffer, boolean isLabelComparison) {
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
			ArrayList<Place> places = new ArrayList<>();
			ArrayList<Transition> transitions = new ArrayList<>();
			if(isLabelComparison)
			{
				places = nodes.stream().filter(x->x.getType()==PetriNetElementType.PLACE).map(obj -> (Place) obj).collect(Collectors.toCollection(ArrayList::new));
				transitions = nodes.stream().filter(x->x.getType()==PetriNetElementType.TRANSITION).map(obj -> (Transition) obj).collect(Collectors.toCollection(ArrayList::new));
			}
			else {
				places = overlord.getWorkspace().getProject().getPlaces();
				transitions = overlord.getWorkspace().getProject().getTransitions();
			}

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
			
			overlord.log("Read "+placesProcessed+" places, "+transitionsProcessed+ 
					" transitions, "+arcsProcessed+" arcs, "+functionsRead+" functions.", "text", true);
			if(functionsFailed > 0)
				overlord.log("Failed to correctly parse "+functionsFailed+" functions.", "error", true);
			
			status = true;
		} catch (Exception e) {
			overlord.log("Project error reading failed in network section.", "error", true);
			overlord.log("Read so far: "+placesProcessed+" places, "+transitionsProcessed+ 
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
			overlord.log("Failed to correctly parse line: "+functionLine, "warning", true);
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
			
			query = "Place colored:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					place.isColored = true;
				}
				return;
			}
			
			query = "Place colors:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				try {
					place.setColorTokensNumber(Integer.parseInt(tab[0]), 0);
					place.setColorTokensNumber(Integer.parseInt(tab[1]), 1);
					place.setColorTokensNumber(Integer.parseInt(tab[2]), 2);
					place.setColorTokensNumber(Integer.parseInt(tab[3]), 3);
					place.setColorTokensNumber(Integer.parseInt(tab[4]), 4);
					place.setColorTokensNumber(Integer.parseInt(tab[5]), 5);
				} catch (Exception e) {}
				
				return;
			}
		} catch (Exception e) {
			overlord.log("Reading file error in line: "+backup+" for Place "+placesProcessed, "error", true);
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
				transition.forceSetEFT(eft);
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
			
			query = "Transition colored:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					transition.setTransType(TransitionType.CPNbasic);
				}
				return;
			}
			
			query = "Transition colors threshold:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				try {
					transition.setRequiredColoredTokens(Integer.parseInt(tab[0]), 0);
					transition.setRequiredColoredTokens(Integer.parseInt(tab[1]), 1);
					transition.setRequiredColoredTokens(Integer.parseInt(tab[2]), 2);
					transition.setRequiredColoredTokens(Integer.parseInt(tab[3]), 3);
					transition.setRequiredColoredTokens(Integer.parseInt(tab[4]), 4);
					transition.setRequiredColoredTokens(Integer.parseInt(tab[5]), 5);
				} catch (Exception e) {}
				
				return;
			}
			
		} catch (Exception e) {
			overlord.log("Reading file error in line: "+backup+" for Transition "+transitionsProcessed, "error", true);
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
			overlord.log("Reading file error in line: "+backup+" for MetaNode "+metanodesProcessed, "error", true);
		}
	}
	
	/**
	 * Metoda czytające dane o łukach z pliku projektu Holmes.
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
			TypeOfArc arcType = TypeOfArc.NORMAL;
			if(typeLine.contains("READARC"))
				arcType = TypeOfArc.READARC;
			else if(typeLine.contains("INHIBITOR"))
				arcType = TypeOfArc.INHIBITOR;
			else if(typeLine.contains("RESET"))
				arcType = TypeOfArc.RESET;
			else if(typeLine.contains("EQUAL"))
				arcType = TypeOfArc.EQUAL;
			else if(typeLine.contains("META_ARC"))
				arcType = TypeOfArc.META_ARC;
			
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
			
			boolean colorReadOk = false;
			boolean colorStatus = false;
			String[] colorsWeights = null;
			try {
				int len = tab.length;
				if(len > 4) {
					if(tab[4].equals("true")) {
						colorStatus = true;
					}
					colorsWeights = tab[5].split(":");
					colorReadOk = true;
				}
			} catch (Exception e) {}
			
			if(placeFirst) { //pierwsze jest miejsce
				if(metaSecond == false) {
					placeIndex = Integer.parseInt(arcDataTable[0]);
					placeElLoc = Integer.parseInt(arcDataTable[1]);
					transIndex = Integer.parseInt(arcDataTable[2]);
					transElLoc = Integer.parseInt(arcDataTable[3]);
					
					ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
					ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
					Arc newArc = new Arc(pEL, tEL, "", weight, arcType);
					
					newArc.clearBreakPoints();
					if(tab.length > 3)
						addBroken(newArc, tab[3]);
					
					if(colorReadOk)
						addColorsToArc(newArc, colorStatus, colorsWeights);
					return newArc;
				} else { //metaSecond == true
					placeIndex = Integer.parseInt(arcDataTable[0]);
					placeElLoc = Integer.parseInt(arcDataTable[1]);
					metaIndex = Integer.parseInt(arcDataTable[2]);
					metaElLoc = Integer.parseInt(arcDataTable[3]);
					
					ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
					ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
					Arc newArc = new Arc(pEL, mEL, "", weight, arcType);
					
					newArc.clearBreakPoints();
					if(tab.length > 3)
						addBroken(newArc, tab[3]);
					
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
						
						newArc.clearBreakPoints();
						if(tab.length > 3)
							addBroken(newArc, tab[3]);

						return newArc;
					} else { //drugie jest miejsce
						metaIndex = Integer.parseInt(arcDataTable[0]);
						metaElLoc = Integer.parseInt(arcDataTable[1]);
						placeIndex = Integer.parseInt(arcDataTable[2]);
						placeElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
						ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
						Arc newArc = new Arc(mEL, pEL, "", weight, arcType);
						
						newArc.clearBreakPoints();
						if(tab.length > 3)
							addBroken(newArc, tab[3]);
						
						if(colorReadOk)
							addColorsToArc(newArc, colorStatus, colorsWeights);
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
						
						newArc.clearBreakPoints();
						if(tab.length > 3)
							addBroken(newArc, tab[3]);
						
						if(colorReadOk)
							addColorsToArc(newArc, colorStatus, colorsWeights);
						return newArc;
					} else { //drugie jest miejsce
						transIndex = Integer.parseInt(arcDataTable[0]);
						transElLoc = Integer.parseInt(arcDataTable[1]);
						placeIndex = Integer.parseInt(arcDataTable[2]);
						placeElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
						ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
						Arc newArc = new Arc(tEL, pEL, "", weight, arcType);
						
						newArc.clearBreakPoints();
						if(tab.length > 3)
							addBroken(newArc, tab[3]);
						
						if(colorReadOk)
							addColorsToArc(newArc, colorStatus, colorsWeights);
						return newArc;
					}
				}
			}
		} catch (Exception e) {
			overlord.log("Reading file error in line: "+backup, "error", true);
		}
		return null;
	}

	/**
	 * Dodaje dane wag kolorów do łuku.
	 * @param newArc Arc - obiekt łuku
	 * @param colorStatus boolean - status koloru
	 * @param colorsWeights string[] - tablica wag (string!)
	 */
	private void addColorsToArc(Arc newArc, boolean colorStatus, String[] colorsWeights) {
		try {
			if(colorStatus)
				newArc.setArcType(TypeOfArc.COLOR);
			
			newArc.setColorWeight(Integer.parseInt(colorsWeights[0]), 0);
			newArc.setColorWeight(Integer.parseInt(colorsWeights[1]), 1);
			newArc.setColorWeight(Integer.parseInt(colorsWeights[2]), 2);
			newArc.setColorWeight(Integer.parseInt(colorsWeights[3]), 3);
			newArc.setColorWeight(Integer.parseInt(colorsWeights[4]), 4);
			newArc.setColorWeight(Integer.parseInt(colorsWeights[5]), 5);
		} catch (Exception e) {
			overlord.log("Error while adding color data to arc: "+newArc.toString(), "error", true);
		}
	}

	/**
	 * Dodaje nowy wektor punktów łamiących łuk.
	 * @param newArc Arc - łuk
	 * @param brokenLine String - linia punktów
	 */
	private void addBroken(Arc newArc, String brokenLine) {
		String[] tab = brokenLine.split("x");
		for(int i=0; i<tab.length; i++) {
			String s = tab[i];
			try {
				String[] coords = s.split("-");
				int x = Integer.parseInt(coords[0]);
				int y = Integer.parseInt(coords[1]);
				
				if(x == 99999 && y == 11111)
					return;
				
				newArc.addBreakPoint(new Point(x,y));;
			} catch (Exception e) {}
		}
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
						overlord.log("Error: different numbers of t-invariants ("+t_invariantsMatrix.size()+
								") and their names ("+readLines+"). Operation failed.", "error", true);
						return false;
					}
					
				} else {
					overlord.log("T-invariants with wrong number of elements in file:"+problemWithInv, "error", true);
					return false;
				}
			}
			
			return true;
		} catch (Exception e) {
			overlord.log("Reading invariants failed for t-invariant number: \n"+t_invariantsProcessed, "error", true);
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
						overlord.log("Error: different numbers of p-invariants ("+p_invariantsMatrix.size()+
								") and their names ("+readLines+"). Operation failed.", "error", true);
						return false;
					}
					
				} else {
					overlord.log("P-invariants with wrong number of elements in file:"+problemWithInv, "error", true);
					return false;
				}
			}
			
			return true;
		} catch (Exception e) {
			overlord.log("Reading p-invariants failed for invariant number: \n"+p_invariantsProcessed, "error", true);
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
						overlord.log("Error: different numbers of MCT sets ("+mctData.size()+
								") and their names ("+readLines+"). Operation failed.", "error", true);
						return false;
					}
					
				} else {
					overlord.log("MCT with wrong number ID numbers for their transitions in file:"+problemWithMCTLines, "error", true);
					return false;
				}
			}
			
			
			
			return true;
		} catch (Exception e) {
			overlord.log("Reading MCT sets failed for MCT number: \n"+mctProcessed, "error", true);
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
			while(!((line = buffer.readLine()).contains("<States data>"))) //przewiń do wektorów stanów
				;
			
			line = buffer.readLine();
			int readedLine = 0;
			if(line.equals("States: 0"))
				return false;
			
			boolean go = true;
			StatePlacesManager statesMngr = projectCore.accessStatesManager();
			statesMngr.reset(true);

			line = buffer.readLine();
			try {
				while(go) {
					readedLine++;
					StatePlacesVector pVector = new StatePlacesVector();
					line = line.replace(" ", "");
					String[] tab = line.split(";");
					
					for(int i=0; i<tab.length; i++) {
						pVector.accessVector().add(Double.parseDouble(tab[i]));
					}
					
					line = buffer.readLine(); //dane dodatkowe
					line = line.trim();
					tab = line.split(";");
					pVector.setStateType(tab[0]);

					line = buffer.readLine();
					line = line.trim();
					line = Tools.decodeString(line);
					pVector.setDescription(line);

					line = buffer.readLine();
					if(line.contains("<EOSt>")) {
						go = false;
					}
					
					statesMngr.accessStateMatrix().add(pVector);
				}
			} catch (Exception e) {}
			
			if(((int)readedLine/3) > statesMngr.accessStateMatrix().size()) {
				overlord.log("Error reading state vector number "+(readedLine), "error", true);
				if(statesMngr.accessStateMatrix().size() == 0) {
					statesMngr.createCleanState();
				}
			}
			
			return true;
		} catch (Exception e) {
			overlord.log("Reading state vectors failed.", "error", true);
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
			while(!((line = buffer.readLine()).contains("<Firing rates data>"))) //przewiń do wektorów firing rates
				;
			
			line = buffer.readLine();
			int readedLine = 0;
			if(line.equals("FRvectors: 0"))
				return false;
			
			boolean go = true;
			SPNdataVectorManager frateMngr = projectCore.accessFiringRatesManager();
			frateMngr.reset(true);

			line = buffer.readLine();
			readedLine++;
			int readProtocol = 0;
			try {
				while(go) {
					
					SPNdataVector frVector = new SPNdataVector();
					line = line.replace(" ", "");
					
					String[] dataVectorTable = line.split(";");
					
					if(dataVectorTable.length < (transitionsProcessed + 3)) {
						readProtocol = 0;
						//old project file
						line = buffer.readLine();//typ tranzycji
						readedLine++;
						line = line.replace(" ", "");
						String[] tabType = line.split(";");
						
						ArrayList<SPNtransitionData> dataVector = new ArrayList<SPNtransitionData>();
						for(int i=0; i<dataVectorTable.length; i++) {
							StochaticsType subType = StochaticsType.ST;
							if(tabType[i].equals("DT"))
								subType = StochaticsType.DT;
							else if(tabType[i].equals("IM"))
								subType = StochaticsType.IM;
							else if(tabType[i].equals("SchT"))
								subType = StochaticsType.SchT;
							
							//TODO: nowy konstruktor, wektor zapisu
							SPNtransitionData frc = frVector.newContainer(dataVectorTable[i], subType);

							dataVector.add(frc);
						}
						frVector.accessVector().addAll(dataVector); //boxing
					} else {
						readProtocol = 1;
						//new SPN block
						frVector.setSPNtype(SPNvectorSuperType.SPN);
						frVector.setDescription("Read errors");
						ArrayList<SPNtransitionData> dataVector = parseSPNdataVector(dataVectorTable, frVector);
						frVector.accessVector().addAll(dataVector);
					}

					line = buffer.readLine(); //dane dodatkowe
					readedLine++;
					line = line.trim();
					dataVectorTable = line.split(";");
					
					if(dataVectorTable[0].equals("SSA")) {
						frVector.setSPNtype(SPNvectorSuperType.SSA);
					} else {
						frVector.setSPNtype(SPNvectorSuperType.SPN);
					}

					line = buffer.readLine();
					readedLine++;
					line = line.trim();
					line = Tools.decodeString(line);
					frVector.setDescription(line);

					line = buffer.readLine();
					readedLine++;
					if(line.contains("<EOFRv>")) {
						go = false;
						readedLine--;
					}
					
					frateMngr.accessSPNmatrix().add(frVector); //boxing in manager
				}
			} catch (Exception e) {
				overlord.log("Operation failed, wrong SPN data in line "+(readedLine), "error", true);
				if(frateMngr.accessSPNmatrix().size() == 0) {
					frateMngr.createCleanSPNdataVector();
				}
			}
			
			if(readProtocol == 0) {
				if((readedLine/4) > frateMngr.accessSPNmatrix().size()) {
					overlord.log("Error: SPN vector could not be read. Creating clean vector.", "error", true);
					if(frateMngr.accessSPNmatrix().size() == 0) {
						frateMngr.createCleanSPNdataVector();
					} else {
						frateMngr.reset(false); //false!!!
						frateMngr.createCleanSPNdataVector();
					}
				}
			} else {
				if((readedLine/3) > frateMngr.accessSPNmatrix().size()) {
					
					if(frateMngr.accessSPNmatrix().size() == 0) {
						frateMngr.createCleanSPNdataVector();
						overlord.log("Error: SPN vector could not be read. Creating clean vector.", "error", true);
					} else {
						overlord.log("Warning: some SPN vector could not be read properly. Possible data corruption.", "error", true);
						//frateMngr.reset(false); //false!!!
						//frateMngr.createCleanSPNdataVector();
					}
				}
			}
			
			return true;
		} catch (Exception e) {
			overlord.log("Reading SPN data vectors failed.", "error", true);
			return false;
		}
	}
	
	/**
	 * Czyta tablicę danych wektora SPN składającą się z tekstowego zapisu obiektów SPNtransitionData.
	 * @param dataVectorTable String[] - tablica danych
	 * @param frVector SPNdataVector
	 * @return ArrayList[SPNtransitionData] - wektor danych obiektów SPNtransitionData
	 */
	private ArrayList<SPNtransitionData> parseSPNdataVector(String[] dataVectorTable, SPNdataVector frVector) {
		ArrayList<SPNtransitionData> spnVector = new ArrayList<SPNtransitionData>();
		
		/*
		 * public String ST_function = "";
		public int IM_priority = 0;
		public int DET_delay = 0;
		public String SCH_start = "";
		public int SCH_rep = 0;
		public String SCH_end = "";
		public StochaticsType sType = StochaticsType.ST;
		 */
		if(dataVectorTable[0].contains("version101:")) {
			dataVectorTable[0] = dataVectorTable[0].replace("version101:", "");
			for(int i=0; i<transitionsProcessed; i++) {
				SPNtransitionData box = frVector.newContainer();
				box.ST_function = dataVectorTable[i*7];
				try { box.IM_priority = Integer.parseInt(dataVectorTable[(i*7)+1]); } catch(Exception e) {}
				try { box.DET_delay = Integer.parseInt(dataVectorTable[(i*7)+2]); } catch(Exception e) {}
				box.SCH_start = dataVectorTable[(i*7)+3];
				try { box.SCH_rep = Integer.parseInt(dataVectorTable[(i*7)+4]); } catch(Exception e) {}
				box.SCH_end = dataVectorTable[(i*7)+5];
				
				if(dataVectorTable[(i*7)+6].equals("IM"))
					box.sType = StochaticsType.IM;
				else if(dataVectorTable[(i*7)+6].equals("DT"))
					box.sType = StochaticsType.DT;
				else if(dataVectorTable[(i*7)+6].equals("SchT"))
					box.sType = StochaticsType.SchT;
				else
					box.sType = StochaticsType.ST;
				
				spnVector.add(box);
			}
		}
		return spnVector;
	}

	/**
	 * Metoda czyta blok danych z wektorami SSA.
	 * @param buffer BufferedReader - obiekt czytający
	 * @return boolean - true, jeśli wszystko dobrze poszło
	 */
	private boolean readSSAvectors(BufferedReader buffer) {
		try {
			String line = "";
			while(!((line = buffer.readLine()).contains("<SSA vectors data>"))) //przewiń do wektorów SSA
				;
			
			line = buffer.readLine();
			int readedLine = 0;
			if(line.equals("SSA vectors: 0"))
				return false;
			
			boolean go = true;
			SSAplacesManager ssaMngr = projectCore.accessSSAmanager();
			ssaMngr.reset(true);

			line = buffer.readLine();
			try {
				while(go) {
					readedLine++;
					SSAplacesVector pVector = new SSAplacesVector();
					line = line.replace(" ", "");
					String[] tab = line.split(";");
					
					for(int i=0; i<tab.length; i++) {
						pVector.accessVector().add(Double.parseDouble(tab[i]));
					}

					line = buffer.readLine(); //dane dodatkowe
					line = line.trim();
					tab = line.split(";");
					if(tab[0].equals("CAPACITY") || tab[0].equals("MOLECULES")) {
						pVector.setType(SSAdataType.MOLECULES);
					} else {
						pVector.setType(SSAdataType.CONCENTRATION);
					}
					double volume = Double.parseDouble(tab[1]);
					pVector.setVolume(volume);
					
					line = buffer.readLine(); //nazwa
					line = line.trim();
					line = Tools.decodeString(line);
					pVector.setDescription(line);
					
					line = buffer.readLine();
					if(line.contains("<EOSSA>")) {
						go = false;
					}
					ssaMngr.accessSSAmatrix().add(pVector);
				}
			} catch (Exception e) {}
			
			if(((int)readedLine/3) > ssaMngr.accessSSAmatrix().size()) {
				overlord.log("Error reading state vector number "+(readedLine), "error", true);
				if(ssaMngr.accessSSAmatrix().size() == 0) {
					ssaMngr.createCleanSSAvector();
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log("Reading SSA vectors failed.", "error", true);
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
		int nodeSID = overlord.getWorkspace().getSheets().size() - 1;
		int SIN = overlord.IDtoIndex(nodeSID);
		GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(SIN).getGraphPanel();
		graphPanel.setSize(new Dimension(globalMaxWidth+300, globalMaxHeight+200));
		graphPanel.setOriginSize(graphPanel.getSize());
		graphPanel.repaint();
	}


	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public ArrayList<Arc> getArcs() {
		return arcs;
	}


}
