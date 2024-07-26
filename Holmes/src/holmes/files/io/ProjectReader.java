package holmes.files.io;

import java.awt.Dimension;
import java.awt.Point;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.*;
import holmes.petrinet.data.SSAplacesVector.SSAdataType;
import holmes.petrinet.data.SPNdataVector.SPNvectorSuperType;
import holmes.petrinet.elements.*;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;
import holmes.petrinet.elements.Transition.TransitionType;
import holmes.petrinet.elements.extensions.TransitionSPNExtension;
import holmes.petrinet.functions.FunctionsTools;
import holmes.utilities.Tools;
import holmes.windows.HolmesNotepad;

/**
 * Metoda czytająca plik danych projektu.
 */
public class ProjectReader {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private final PetriNet projectCore;
	private final ArrayList<Node> nodes;
	private ArrayList<MetaNode> metanodes = null;
	private final ArrayList<Arc> arcs;
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
	private boolean statesXTPN = false;
	private boolean functions = false;
	private boolean firingRates = false;
	private boolean pInvariants = false;
	private boolean ssaData = false;

	//obługa elementów kolorowanych:
	private boolean isPlaceColored = false;
	private boolean isTransitionColored = false;
	private String[] tabPlaceTokens = null;
	private String[] tabTransReqTokens = null;

	private boolean XTPNdataMode = false; //jeśli true, to znaczy, że odczytujemy sieć XTPN
	
	/**
	 * Konstruktor obiektu klasy odczytywania projektu.
	 */
	public ProjectReader() {
		projectCore = overlord.getWorkspace().getProject();
		projectCore.setProjectType(PetriNet.GlobalNetType.PN); //default
		nodes = projectCore.getNodes();
		metanodes = projectCore.getMetaNodes();
		arcs = projectCore.getArcs();
		t_invariantsMatrix = projectCore.getT_InvMatrix();
		t_invariantsNames = projectCore.accessT_InvDescriptions();
		p_invariantsMatrix = projectCore.getP_InvMatrix();
		p_invariantsNames = projectCore.accessP_InvDescriptions();
		mctData = projectCore.getMCTMatrix();
		mctNames = projectCore.accessMCTnames();
	}

	/**
	 * Konstruktor dla modułów Szawiego.
	 * @param isLabelCompariso (<b>boolean</b>) unused, ale co ja tam wiem [MR]
	 */
	public ProjectReader(boolean isLabelCompariso) { //[MR] compariso? caramba!
		projectCore = new PetriNet(null,"test");
		projectCore.setProjectType(PetriNet.GlobalNetType.PN); //default
		nodes = new ArrayList<>();
		arcs = new ArrayList<>();
	}

	public boolean readProjectForLabelComparison(String filepath) {
		boolean status = true;

		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));

			overlord.log(lang.getText("LOGentry00241")+ " " + filepath, "text", true);

			status = readProjectHeader(buffer);
			if (!status) {
				overlord.log(lang.getText("LOGentry00242"), "error", true);
				buffer.close();
				return false;
			}

			status = readNetwork(buffer,true);
			if (!status) {
				overlord.log(lang.getText("LOGentry00243"), "error", true);
				buffer.close();
				return false;
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00244exception")+" "+e.getMessage(), "error", true);
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
		if(!status) {
			return false;
		}
		
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			BufferedReader buffer = new BufferedReader(new InputStreamReader(dis));
			
			overlord.log(lang.getText("LOGentry00241")+" "+filepath, "text", true);

			status = readProjectHeader(buffer);
			if (!status) {
				overlord.log(lang.getText("LOGentry00242"), "error", true);
				buffer.close();
				return false;
			}

			try {
				status = readNetwork(buffer,false);
				if(!status) {
					overlord.log(lang.getText("LOGentry00243"), "error", true);
					buffer.close();
					return false;
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00245exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			try {
				status = readTInvariants(buffer);
				if(!status) {
					projectCore.setT_InvMatrix(null, false);
				} else {
					try{
						overlord.getT_invBox().showT_invBoxWindow(projectCore.getT_InvMatrix());
						overlord.reset.setT_invariantsStatus(true);
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentry00246exception")+" "+e.getMessage(), "error", true);
					}
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00247exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			try {
				if(pInvariants) {
					status = readPInvariants(buffer);
					if(!status) {
						projectCore.setP_InvMatrix(null);
					} else {
						try{
							overlord.getP_invBox().showP_invBoxWindow(projectCore.getP_InvMatrix());
						} catch (Exception e) {
							overlord.log(lang.getText("LOGentry00248exception")+" "+e.getMessage(), "error", true);
						}
					}
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00249exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			try {
				status = readMCT(buffer);
				if(!status) {
					projectCore.setMCTMatrix(null, false);
				} else {
					try{
						overlord.getMctBox().showMCT(projectCore.getMCTMatrix());
						overlord.reset.setMCTStatus(true);
					} catch (Exception e) {
						overlord.log(lang.getText("LOGentry00250exception")+" "+e.getMessage(), "error", true);
					}
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00251exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			try {
				if(states) {
					status = readStates(buffer);
					if(!status) {
						projectCore.accessStatesManager().createCleanStatePN();
					}
				} else {
					projectCore.accessStatesManager().createCleanStatePN();
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00252exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			try {
				if(statesXTPN) {
					status = readStatesXTPN(buffer);
					if(!status) {
						projectCore.accessStatesManager().createFirstMultiset_M();
					}
				} else {
					if(XTPNdataMode) { //tylko dla XTPN
						projectCore.accessStatesManager().createFirstMultiset_M();
					}
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00253exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			try {
				if(firingRates) {
					status = readFiringRates(buffer);
					if(!status) {
						projectCore.accessFiringRatesManager().createCleanSPNdataVector();
					}
				} else {
					projectCore.accessFiringRatesManager().createCleanSPNdataVector();
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00254exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			try {
				if(ssaData) {
					status = readSSAvectors(buffer);
					if(!status) {
						projectCore.accessSSAmanager().createCleanSSAvector();
					}
				} else {
					projectCore.accessSSAmanager().createCleanSSAvector();
				}
			} catch (Exception e) {
				overlord.log(lang.getText("LOGentry00255exception")+" "+e.getMessage(), "error", true);
				return false;
			}

			overlord.subnetsGraphics.addRequiredSheets();
			//overlord.getWorkspace().setSelectedDock(0);
			buffer.close();
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00256exception")+" "+e.getMessage(), "error", true);
			return false;
		}
	}

	/**
	 * Metoda służąca do czytania bloku składowych pliku projektu.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	private boolean readProjectHeader(BufferedReader buffer) {
		try {
			String line;
			while(!((line = buffer.readLine()).contains("<Project blocks>"))) {
				parseHeaderLine(line);
			}

			//line = buffer.readLine();
			if(line.contains("<Project blocks>")) {
				while(!((line = buffer.readLine()).contains("</Project blocks>"))) {
					parseNetblocksLine(line);
				}
			} 
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00257exception")+" "+e.getMessage(), "error", true);
			return false;
		}
	}

	/**
	 * Metoda czyta linie nagłówka pliku projektu.
	 * @param line (<b>String</b>) linia do odczytu z pliku.
	 */
	private void parseHeaderLine(String line) {
		String backup = line;
		try {
			String query = "Project name";
			if(line.contains(query)) {
				line = line.substring(line.indexOf("name:")+6);
				projectCore.setName(line);
				return;
			}
			query = "Date:";
			if(line.contains(query)) {
				return;
			}
			query = "Net type:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf("type:")+6);
				projectCore.setProjectType(projectCore.getNetTypeByName(line));
				if(projectCore.getProjectType() == PetriNet.GlobalNetType.XTPN) {
					projectCore.selectProperSimulatorBox(true);
					XTPNdataMode = true; //tryb XTPN, tworzymy obiekty PlaceXTPN i TransitionXTPN zamiast zwykłych
				}
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00258exception_1")+" "+backup+" "
					+lang.getText("LOGentry00258exception_2") +" "+transitionsProcessed, "error", true);
		}
	}

	/**
	 * Metoda czyta blok informujący, które bloki są w ogóle w pliku projektu.
	 * @param line (<b>String</b>) linia do czytania.
	 */
	private void parseNetblocksLine(String line) {
		try {
			if(line.toLowerCase().contains("subnets")) {
				subnets = true;
			} else if(line.toLowerCase().contains("statesmatrix")) {
				states = true;
			} else if(line.toLowerCase().contains("statesxtpnmatrix")) {
				statesXTPN = true;
			} else if(line.toLowerCase().contains("functions")) {
				functions = true;
			} else if(line.toLowerCase().contains("firingratesdata")) {
				firingRates = true;
			} else if(line.toLowerCase().contains("placeinvdata")) {
				pInvariants = true;
			} else if(line.toLowerCase().contains("ssamatrix")) {
				ssaData = true;
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00259exception")+" "+ line, "error", true);
		}
	}

	/**
	 * Metoda odczytująca dane sieci z pliku projektu i tworząca sieć na bazie miejsc, tranzycji i łuków.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readNetwork(BufferedReader buffer, boolean isLabelComparison) {
		boolean status;
		try {
			String line; // = buffer.readLine();
			//ID GENERATOR:
			while(!((buffer.readLine()).contains("ID generator"))) //przewiń do ID generator
				;

			//PLACES:
			line = buffer.readLine();
			if(!line.contains("<Places: 0>")) { //są miejsca
				buffer.readLine(); // przewiń do -> Place: 0
				boolean go = true;
				
				while(go) {
					if(XTPNdataMode) { //tworzymy sieć XTPN
						PlaceXTPN place = new PlaceXTPN(IdGenerator.getNextId(), 0, new Point(20,20));
						while(!((line = buffer.readLine()).contains("<EOP>"))) {
							parsePlaceLine(line, place);
						}
						line = buffer.readLine(); // przewiń do --> Place : 1 albo patrz niżej:
						if(line.contains("<Places data block end>")) {
							go = false;
						}
						placesProcessed++;
						nodes.add(place);
					} else { //tryb zwykłej sieci
						int nextID = IdGenerator.getNextId();
						Place place = new Place(nextID, 0, new Point(20,20));
						while(!((line = buffer.readLine()).contains("<EOP>"))) {
							parsePlaceLine(line, place);
						}
						line = buffer.readLine(); // przewiń do --> Place : 1 albo patrz niżej:
						if(line.contains("<Places data block end>")) {
							go = false;
						}

						if(isPlaceColored) {
							PlaceColored impostor = resurrectAsColoredPlace(place, nextID);
							isPlaceColored = false;
							tabPlaceTokens = null;
							nodes.add(impostor);
						} else {
							nodes.add(place);
						}
						placesProcessed++;
					}
				}
				//przeczytano miejsca
			}

			//TRANSITIONS:
			while(!((line = buffer.readLine()).contains("<Transitions: "))) //przewiń do tranzycji
				;

			if(!line.contains("<Transitions: 0>")) { //są tranzycje
				buffer.readLine(); // przewiń do -> Transition: 0
				boolean go = true;
				
				while(go) {
					if(XTPNdataMode) { //tworzymy sieć XTPN
						TransitionXTPN transition = new TransitionXTPN(IdGenerator.getNextId(), 0, new Point(20,20));

						while(!((line = buffer.readLine()).contains("<EOT>"))) {
							parseTransitionLine(line, transition);
						}
						line = buffer.readLine(); // przewiń do -> Transition: 1 lub patrz niżej:
						if(line.contains("<Transitions data block end>")) {
							go = false;
						}
						transitionsProcessed++;
						nodes.add(transition);

					} else { //tryb zwykłej sieci
						int nextID = IdGenerator.getNextId();
						Transition transition = new Transition(nextID, 0, new Point(20,20));

						while(!((line = buffer.readLine()).contains("<EOT>"))) {
							parseTransitionLine(line, transition);
						}
						line = buffer.readLine(); // przewiń do -> Transition: 1 lub patrz niżej:
						if(line.contains("<Transitions data block end>")) {
							go = false;
						}

						if(isTransitionColored) {
							TransitionColored impostor = resurrectAsColoredTransition(transition, nextID);
							isTransitionColored = false;
							tabTransReqTokens = null;
							nodes.add(impostor);
						} else {
							nodes.add(transition);
						}
						transitionsProcessed++;
					}
				}
				//przeczytano tranzycje
			}
			
			if(subnets) {
				//METANODES:
				while(!((line = buffer.readLine()).contains("<MetaNodes: "))) //przewiń do tranzycji
					;

				if(!line.contains("<MetaNodes: 0>")) { //są tranzycje
					buffer.readLine(); // przewiń do -> Transition: 0
					boolean go = true;
					
					while(go) {
						MetaNode metanode = new MetaNode(0, IdGenerator.getNextId(), new Point(20,20), MetaType.SUBNET);
						
						while(!((line = buffer.readLine()).contains("<EOT>"))) {
							parseMetaNodesLine(line, metanode);
						}
						line = buffer.readLine(); // przewiń do -> Transition: 1 lub patrz niżej:
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
			ArrayList<Place> places;
			ArrayList<Transition> transitions;
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
			while(!((buffer.readLine()).contains("<Arcs data block>"))) //przewiń do łuków
				;

			while(!(line = buffer.readLine()).contains("Arcs data block end")) {
				Arc newArc = parseArcLine(line, places, transitions, metanodes);
				if(newArc != null) {
					arcsProcessed++;
					arcs.add(newArc);
				}
			}
			
			for(Transition transition : transitions) { //aktywacja wektorów funkcji
				transition.fpnExtension.checkFunctions(arcs, places);
			}
			
			int functionsRead = 0;
			int functionsFailed = 0;
			if(functions) {
				while(!((buffer.readLine()).contains("<Functions data block>"))) //przewiń do funkcji
					;
				
				while(!((line = buffer.readLine()).contains("<Functions data block end>"))) {
					boolean fReadStatus = parseFunction(line, transitions);
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
			
			overlord.log(lang.getText("LOGentry00260a")+" "+placesProcessed+" " +lang.getText("LOGentry00260b")
					+ " "+transitionsProcessed+ " "+lang.getText("LOGentry00260c")
					+ " "+arcsProcessed+" "+lang.getText("LOGentry00260d") 
					+ " "+functionsRead+" "+ lang.getText("LOGentry00260e"), "text", true);
			if(functionsFailed > 0)
				overlord.log(lang.getText("LOGentry00261a")+" "+functionsFailed+" "+lang.getText("LOGentry00261b"), "error", true);
			
			status = true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00262"), "error", true);
			overlord.log(lang.getText("LOGentry00263a")+" "+placesProcessed+" "+lang.getText("LOGentry00263b")
					+" "+transitionsProcessed+ " "+lang.getText("LOGentry00263c")
					+" "+arcsProcessed+" "+lang.getText("LOGentry00263d"), "error", false);
			overlord.log(lang.getText("LOGentry00263exception")+" "+e.getMessage(), "error", false);
			status = false;
		}
		return status;
	}

	/**
	 * Metoda odpowiedzialna za odczyt linii funkcji.
	 * @param functionLine String - przeczytana linia
	 * @param transitions ArrayList[Transition] - wektor tranzycji
	 * @return boolean - true, jeśli się udało
	 */
	private boolean parseFunction(String functionLine, ArrayList<Transition> transitions) {
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

			return transition.fpnExtension.updateFunctionString(table[1], table[2], correct, enabled);
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00264exception")+" "+functionLine, "warning", true);
			overlord.log(lang.getText("LOGentry00264exception")+" "+e.getMessage(), "warning", false);
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
			String query = "Place gID:";
			if(line.contains(query)) {
				//line = line.substring(line.indexOf(query)+query.length());
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
				try {
					int tokens = Integer.parseInt(line);
					place.setTokensNumber(tokens);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00265exception")+" "+placesProcessed
							+"\n"+exc.getMessage(), "error", true);
					place.setTokensNumber(0);
				}
				return;
			}

			query = "Place SSAvalue:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					double ssaValue = Double.parseDouble(line);
					place.setSSAvalue(ssaValue);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00266exception")+" "+placesProcessed
							+"\n"+exc.getMessage(), "error", true);
					place.setTokensNumber(0);
				}
				return;
			}

			query = "Place SSAconcStatus:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					boolean isConc = Boolean.parseBoolean(line);
					place.setSSAconcentrationStatus(isConc);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00267exception")+" "+placesProcessed
							+"\n"+exc.getMessage(), "error", true);
					place.setTokensNumber(0);
				}
				return;
			}

			query = "Place XTPN status:";
			if(line.contains(query) && XTPNdataMode) {
				return;
			}

			query = "Place XTPN gammaMode:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {//isGammaModeActiveXTPN
					((PlaceXTPN)place).setGammaModeStatus(true);
				} else if(line.contains("false")) {
					((PlaceXTPN)place).setGammaModeStatus(false);
				} else {
					overlord.log(lang.getText("LOGentry00268")+" "+placesProcessed, "error", true);
					((PlaceXTPN)place).setGammaModeStatus(true);
				}
				return;
			}

			query = "Place XTPN gammaVisible:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {//isGammaModeActiveXTPN
					((PlaceXTPN)place).setGammaRangeVisibility(true);
				} else if(line.contains("false")) {
					((PlaceXTPN)place).setGammaRangeVisibility(false);
				} else {
					overlord.log(lang.getText("LOGentry00269")+" "+placesProcessed, "error", true);
					((PlaceXTPN)place).setGammaRangeVisibility(true);
				}
				return;
			}

			query = "Place XTPN gammaMin:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					double gammaMin = Double.parseDouble(line);
					((PlaceXTPN)place).setGammaMinValue(gammaMin, true);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00270")+" "+placesProcessed
							+"\n"+exc.getMessage(), "error", true);
					((PlaceXTPN)place).setGammaMinValue(0, true);
				}
				return;
			}

			query = "Place XTPN gammaMax:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					double gammaMax = Double.parseDouble(line);
					((PlaceXTPN)place).setGammaMaxValue(gammaMax, true);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00271")+" "+placesProcessed
							+"\n"+exc.getMessage(), "error", true);
					if(((PlaceXTPN)place).getGammaMinValue() > 0)
						((PlaceXTPN)place).setGammaMaxValue(((PlaceXTPN)place).getGammaMinValue(), true);
					else
						((PlaceXTPN)place).setGammaMaxValue(0, true);
				}
				return;
			}

			query = "Place XTPN fractionSize:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					int fractionSize = Integer.parseInt(line);
					((PlaceXTPN)place).setFractionForPlaceXTPN(fractionSize);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00272")+" "+placesProcessed
							+"\n"+exc.getMessage(), "error", true);
					((PlaceXTPN)place).setFractionForPlaceXTPN(6);
				}
				return;
			}

			query = "Place XTPN multiset:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");

				String[] tab = line.split(":");
				for (String s : tab) {
					try {
						double token = Double.parseDouble(s);
						((PlaceXTPN)place).accessMultiset().add(token); //dodanie poza seterem, aby nie dublować wartości tokensNumber
					} catch (Exception exc) {
						overlord.log(lang.getText("LOGentry00273")+" "+placesProcessed
								+"\n"+exc.getMessage(), "error", true);
						((PlaceXTPN)place).addTokens_XTPN(1, 0);
					}
				}
				return;
			}
			
			query = "Place portal status:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					place.setPortal(true);
				} else if(line.contains("false")) {
					place.setPortal(false);
				} else {
					overlord.log(lang.getText("LOGentry00274")+" "+placesProcessed, "error", true);
					place.setPortal(false);
				}
				return;
			}
			
			query = "Place locations:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					int elLocSize = Integer.parseInt(line);
					for(int e=0; e<elLocSize-1; e++) {
						place.getElementLocations().add(new ElementLocation(0, new Point(20,20), place));
						place.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(0, new Point(0,0), place));
						if(place.getPlaceType() == Place.PlaceType.XTPN) {
							place.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(0, new Point(0,0), place));
						}
					}
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00275exception")+" "+placesProcessed
							+"\n"+exc.getMessage(), "error", true);
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
				
				place.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				place.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).forceSetPosition(newP);
				place.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}

			query = "Place gamma offset data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);

				place.getTextsLocations(GUIManager.locationMoveType.GAMMA).get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				place.getTextsLocations(GUIManager.locationMoveType.GAMMA).get(eLocIndex).forceSetPosition(newP);
				place.getTextsLocations(GUIManager.locationMoveType.GAMMA).get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}
			
			query = "Place colored:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					isPlaceColored = true;
					//place.isColored = true;
				}
				return;
			}
			
			query = "Place colors:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				tabPlaceTokens = line.split(";");
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00276exception_1")+" "+backup+" "
					+lang.getText("LOGentry00276exception_2")+" "+placesProcessed
					+"\n"+e.getMessage(), "error", true);
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
			String query = "Transition gID:";
			if(line.contains(query)) {
				//line = line.substring(line.indexOf(query)+query.length());
				return;
			}
			
			query = "Transition type:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");

				switch (line) {
					case "PN" -> transition.setTransType(TransitionType.PN);
					case "TPN" -> transition.setTransType(TransitionType.TPN);
					case "SPN" -> transition.setTransType(TransitionType.SPN);
					case "CPN" -> transition.setTransType(TransitionType.CPN);
					case "XTPN" -> transition.setTransType(TransitionType.XTPN);
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
				transition.timeExtension.forceSetEFT(eft);
				return;
			}
			
			query = "Transition lft:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				double lft = Double.parseDouble(line);
				transition.timeExtension.setLFT(lft);
				return;
			}
			
			query = "Transition duration:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				double duration = Double.parseDouble(line);
				transition.timeExtension.setDPNduration(duration);
				return;
			}
			
			query = "Transition TPN status:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				transition.timeExtension.setTPNstatus(line.contains("true"));
				return;
			}
			
			query = "Transition DPN status:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				transition.timeExtension.setDPNstatus(line.contains("true"));
				return;
			}
			
			query = "Transition function flag:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				transition.fpnExtension.setFunctional(line.contains("true"));
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

			query = "Transition XTPN status:";
			if(line.contains(query) && XTPNdataMode) {
				return;
			}

			query = "Transition XTPN alphaMode:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					((TransitionXTPN)transition).setAlphaModeStatus(true);
				} else if(line.contains("false")) {
					((TransitionXTPN)transition).setAlphaModeStatus(false);
				} else {
					overlord.log(lang.getText("LOGentry00277")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setAlphaModeStatus(true);
				}
				return;
			}

			query = "Transition XTPN alphaVisible:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {//isGammaModeActiveXTPN
					((TransitionXTPN)transition).setAlphaRangeVisibility(true);
				} else if(line.contains("false")) {
					((TransitionXTPN)transition).setAlphaRangeVisibility(false);
				} else {
					overlord.log(lang.getText("LOGentry00278")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setAlphaRangeVisibility(true);
				}
				return;
			}

			query = "Transition XTPN alphaMin:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					double gammaMin = Double.parseDouble(line);
					((TransitionXTPN)transition).setAlphaMinValue(gammaMin, true);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00279")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setAlphaMinValue(0, true);
				}
				return;
			}

			query = "Transition XTPN alphaMax:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					double gammaMax = Double.parseDouble(line);
					((TransitionXTPN)transition).setAlphaMaxValue(gammaMax, true);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00280exception")+" "+transitionsProcessed
							+"\n"+ exc.getMessage(), "error", true);
					
					if(((TransitionXTPN)transition).getAlphaMinValue() > 0)
						((TransitionXTPN)transition).setAlphaMaxValue(((TransitionXTPN)transition).getAlphaMinValue(), true);
					else
						((TransitionXTPN)transition).setAlphaMaxValue(0, true);
				}
				return;
			}

			query = "Transition XTPN betaMode:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					((TransitionXTPN)transition).setBetaModeStatus(true);
				} else if(line.contains("false")) {
					((TransitionXTPN)transition).setBetaModeStatus(false);
				} else {
					overlord.log(lang.getText("LOGentry00281")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setBetaModeStatus(true);
				}
				return;
			}

			query = "Transition XTPN betaVisible:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {//isGammaModeActiveXTPN
					((TransitionXTPN)transition).setBetaRangeVisibility(true);
				} else if(line.contains("false")) {
					((TransitionXTPN)transition).setBetaRangeVisibility(false);
				} else {
					overlord.log(lang.getText("LOGentry00282")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setBetaRangeVisibility(true);
				}
				return;
			}

			query = "Transition XTPN betaMin:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					double gammaMin = Double.parseDouble(line);
					((TransitionXTPN)transition).setBetaMinValue(gammaMin, true);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00283exception")+" "+transitionsProcessed
							+"\n"+ exc.getMessage(), "error", true);
					((TransitionXTPN)transition).setBetaMinValue(0, true);
				}
				return;
			}

			query = "Transition XTPN betaMax:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					double gammaMax = Double.parseDouble(line);
					((TransitionXTPN)transition).setBetaMaxValue(gammaMax, true);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00284exception")+" "+transitionsProcessed
							+"\n"+ exc.getMessage(), "error", true);
					if(((TransitionXTPN)transition).getBetaMinValue() > 0)
						((TransitionXTPN)transition).setBetaMaxValue(((TransitionXTPN)transition).getBetaMinValue(), true);
					else
						((TransitionXTPN)transition).setBetaMaxValue(0, true);
				}
				return;
			}

			query = "Transition XTPN tauVisible:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {//isGammaModeActiveXTPN
					((TransitionXTPN)transition).setTauTimersVisibility(true);
				} else if(line.contains("false")) {
					((TransitionXTPN)transition).setTauTimersVisibility(false);
				} else {
					overlord.log(lang.getText("LOGentry00285")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setTauTimersVisibility(true);
				}
				return;
			}

			query = "Transition XTPN massAction:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					((TransitionXTPN)transition).setMassActionKineticsXTPNstatus(true);
				} else if(line.contains("false")) {
					((TransitionXTPN)transition).setMassActionKineticsXTPNstatus(false);
				} else {
					overlord.log(lang.getText("LOGentry00286")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setMassActionKineticsXTPNstatus(false);
				}
				return;
			}

			query = "Transition XTPN fractionSize:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				try {
					int fractionSize = Integer.parseInt(line);
					((TransitionXTPN)transition).setFraction_xTPN(fractionSize);
				} catch (Exception exc) {
					overlord.log(lang.getText("LOGentry00287exception")+" "+transitionsProcessed
							+"\n"+ exc.getMessage(), "error", true);
					((TransitionXTPN)transition).setFraction_xTPN(6);
				}
				return;
			}

			query = "Transition XTPN immediate:";
			if(line.contains(query) && XTPNdataMode) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					((TransitionXTPN)transition).setImmediateStatusXTPN(true);
				} else if(line.contains("false")) {
					((TransitionXTPN)transition).setImmediateStatusXTPN(false);
				} else {
					overlord.log(lang.getText("LOGentry00288")+" "+transitionsProcessed, "error", true);
					((TransitionXTPN)transition).setImmediateStatusXTPN(false);
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
					transition.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(0, new Point(0,0), transition));
					if(transition.getTransType() == TransitionType.XTPN) {
						transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(0, new Point(0,0), transition));
						transition.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(0, new Point(0,0), transition));
						transition.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(0, new Point(0,0), transition));
					}
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
				
				transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).forceSetPosition(newP);
				transition.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}

			query = "Transition alpha offset data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);

				transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).get(eLocIndex).forceSetPosition(newP);
				transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}

			query = "Transition beta offset data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);

				transition.getTextsLocations(GUIManager.locationMoveType.BETA).get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				transition.getTextsLocations(GUIManager.locationMoveType.BETA).get(eLocIndex).forceSetPosition(newP);
				transition.getTextsLocations(GUIManager.locationMoveType.BETA).get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}

			query = "Transition tau offset data sheet/x/y/elIndex:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				String[] tab = line.split(";");
				int sheetID = Integer.parseInt(tab[0]);
				int pointX = Integer.parseInt(tab[1]);
				int pointY = Integer.parseInt(tab[2]);
				int eLocIndex = Integer.parseInt(tab[3]);

				transition.getTextsLocations(GUIManager.locationMoveType.TAU).get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				transition.getTextsLocations(GUIManager.locationMoveType.TAU).get(eLocIndex).forceSetPosition(newP);
				transition.getTextsLocations(GUIManager.locationMoveType.TAU).get(eLocIndex).setNotSnappedPosition(newP);
				return;
			}
			
			query = "Transition colored:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				if(line.contains("true")) {
					isTransitionColored = true;
				}
				return;
			}
			
			query = "Transition colors threshold:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");
				tabTransReqTokens = line.split(";");
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00289exception_1")+" "+backup+" "
					+lang.getText("LOGentry00289exception_2")+" "+transitionsProcessed
					+"\n"+e.getMessage(), "error", true);
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
			String query = "MetaNode gID:";
			if(line.contains(query)) {
				return;
			}
			
			query = "MetaNode type:";
			if(line.contains(query)) {
				line = line.substring(line.indexOf(query)+query.length());
				line = line.replace(">","");

				switch (line) {
					case "SUBNETTRANS" -> metanode.setMetaType(MetaType.SUBNETTRANS);
					case "SUBNETPLACE" -> metanode.setMetaType(MetaType.SUBNETPLACE);
					case "SUBNET" -> metanode.setMetaType(MetaType.SUBNET);
					default -> metanode.setMetaType(MetaType.UNKNOWN);
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
					metanode.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(0, new Point(0,0), metanode));
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
				
				metanode.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).setSheetID(sheetID);
				Point newP = new Point(pointX, pointY);
				metanode.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).forceSetPosition(newP);
				metanode.getTextsLocations(GUIManager.locationMoveType.NAME).get(eLocIndex).setNotSnappedPosition(newP);
				//return;
			}
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00290exception_1")+" "+backup
					+" "+lang.getText("LOGentry00290exception_2")+" "+metanodesProcessed
					+"\n"+e.getMessage(), "error", true);
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
		boolean XTPNarc = false;
		boolean XTPNinh = false;
		try {
			line = line.replace(" ", "");
			String[] tab = line.split(";");
			
			String typeLine = tab[0];
			TypeOfArc arcType = TypeOfArc.NORMAL;
			if(typeLine.contains("READARC")) {
				arcType = TypeOfArc.READARC;
			} else if(typeLine.contains("INHIBITOR")) {
				arcType = TypeOfArc.INHIBITOR;
			} else if(typeLine.contains("RESET")) {
				arcType = TypeOfArc.RESET;
			} else if(typeLine.contains("EQUAL")) {
				arcType = TypeOfArc.EQUAL;
			} else if(typeLine.contains("META_ARC")) {
				arcType = TypeOfArc.META_ARC;
			} else if(typeLine.contains("XTPN")) {
				XTPNarc = true;
			} else if(typeLine.contains("XINH")) {
				arcType = TypeOfArc.INHIBITOR;
				XTPNinh = true;
			} else if(XTPNdataMode && typeLine.contains("NORMAL")) {
				XTPNarc = true;
			}
			
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
			}
			if(arcData.indexOf("M") == 0) {
				metaFirst = true;
				metaSecond = false;
			}
			
			if(arcData.contains("T"))
				isThereTransition = true;
			
			arcData = arcData.replace("P", "");
			arcData = arcData.replace("T", "");
			arcData = arcData.replace("M", "");
			arcData = arcData.replace(")->", " ");
			arcData = arcData.replace(")", "");
			arcData = arcData.replace("(", " ");
			String[] arcDataTable = arcData.split(" ");
			
			int placeIndex;
			int transIndex;
			int metaIndex;
			int placeElLoc;
			int transElLoc;
			int metaElLoc;
			
			boolean colorReadOk = false;
			boolean colorStatus = false;
			String[] colorsWeights = null;
			try {
				int len = tab.length;
				if(len > 4) { //kolor, opcjonalnie
					if(tab[3].contains("true") || tab[3].contains("false")) { //jakimś cudem brak info o łuku łamanym
						if(tab[3].equals("true")) {
							colorStatus = true;
						}
						colorsWeights = tab[4].split(":");
						colorReadOk = true;
					} else {
						if(tab[4].equals("true")) {
							colorStatus = true;
						}
						colorsWeights = tab[5].split(":");
						colorReadOk = true;
					}
				}
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00291exception")+" "+ex.getMessage(), "error", true);
			}
			
			if(placeFirst) { //pierwsze jest miejsce
				if(!metaSecond) {
					placeIndex = Integer.parseInt(arcDataTable[0]);
					placeElLoc = Integer.parseInt(arcDataTable[1]);
					transIndex = Integer.parseInt(arcDataTable[2]);
					transElLoc = Integer.parseInt(arcDataTable[3]);
					
					ElementLocation pEL = places.get(placeIndex).getElementLocations().get(placeElLoc);
					ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
					Arc newArc = new Arc(pEL, tEL, "", weight, arcType);
					if(XTPNarc)
						newArc.arcXTPNbox.setXTPNstatus(true);
					if(XTPNinh)
						newArc.arcXTPNbox.setXTPNinhibitorStatus(true);
					
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
					if(XTPNarc)
						newArc.arcXTPNbox.setXTPNstatus(true);
					if(XTPNinh)
						newArc.arcXTPNbox.setXTPNinhibitorStatus(true);
					
					newArc.clearBreakPoints();
					if(tab.length > 3)
						addBroken(newArc, tab[3]);
					
					return newArc;
				}
			} else { //placeFirst = false
				if(metaFirst) {
					if(isThereTransition) { //druga jest tranzycja
						metaIndex = Integer.parseInt(arcDataTable[0]);
						metaElLoc = Integer.parseInt(arcDataTable[1]);
						transIndex = Integer.parseInt(arcDataTable[2]);
						transElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
						ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
						Arc newArc = new Arc(mEL, tEL, "", weight, arcType);
						if(XTPNarc)
							newArc.arcXTPNbox.setXTPNstatus(true);
						if(XTPNinh)
							newArc.arcXTPNbox.setXTPNinhibitorStatus(true);
						
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
						if(XTPNarc)
							newArc.arcXTPNbox.setXTPNstatus(true);
						if(XTPNinh)
							newArc.arcXTPNbox.setXTPNinhibitorStatus(true);
						
						newArc.clearBreakPoints();
						if(tab.length > 3)
							addBroken(newArc, tab[3]);
						
						if(colorReadOk)
							addColorsToArc(newArc, colorStatus, colorsWeights);
						return newArc;
					}
				} else { //placesFirst = false, metaFirst = false -> pierwsza jest tranzycja
					if(metaSecond) { //drugi jest meta węzeł
						transIndex = Integer.parseInt(arcDataTable[0]);
						transElLoc = Integer.parseInt(arcDataTable[1]);
						metaIndex = Integer.parseInt(arcDataTable[2]);
						metaElLoc = Integer.parseInt(arcDataTable[3]);
						
						ElementLocation tEL = transitions.get(transIndex).getElementLocations().get(transElLoc);
						ElementLocation mEL = metanodes.get(metaIndex).getElementLocations().get(metaElLoc);
						Arc newArc = new Arc(tEL, mEL, "", weight, arcType);
						if(XTPNarc)
							newArc.arcXTPNbox.setXTPNstatus(true);
						if(XTPNinh)
							newArc.arcXTPNbox.setXTPNinhibitorStatus(true);
						
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
						if(XTPNarc)
							newArc.arcXTPNbox.setXTPNstatus(true);
						if(XTPNinh)
							newArc.arcXTPNbox.setXTPNinhibitorStatus(true);
						
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
			overlord.log(lang.getText("LOGentry00292exception")+" "+backup
					+"\n"+e.getMessage(), "error", true);
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
			overlord.log(lang.getText("LOGentry00293exception")+" "+newArc.toString()
					+"\n"+e.getMessage(), "error", true);
		}
	}

	/**
	 * Dodaje nowy wektor punktów łamiących łuk.
	 * @param newArc Arc - łuk
	 * @param brokenLine String - linia punktów
	 */
	private void addBroken(Arc newArc, String brokenLine) {
		if(!brokenLine.contains("-")) { //musi mieć, jeśli to faktycznie blok danych o łuku łamanych, choćby 99999-11111 przy jego braku
			return;
		}

		String[] tab = brokenLine.split("x");
		for (String s : tab) {
			try {
				String[] coords = s.split("-");
				int x = Integer.parseInt(coords[0]);
				int y = Integer.parseInt(coords[1]);

				if (x == 99999 && y == 11111)
					return;

				newArc.addBreakPoint(new Point(x, y));
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00294exception")+" "+ex.getMessage(), "error", true);
			}
		}
	}

	/**
	 * Metoda pomocnicza czytająca z pliku projektu blok danych o t-inwariantach.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readTInvariants(BufferedReader buffer) {
		try {
			String line;
			while(!((buffer.readLine()).contains("<Invariants data>"))) //przewiń do inwariantów
				;
			
			line = buffer.readLine();
			int transNumber = projectCore.getTransitions().size();
			StringBuilder problemWithInv = new StringBuilder();
			int problems = 0;
			int readedLine = -1;
			
			if(!line.contains("<Invariants: 0>")) { //są miejsca
				boolean go = true;
				t_invariantsMatrix = new ArrayList<>();
				
				line = buffer.readLine();
				while(go) {
					readedLine++;
					ArrayList<Integer> invariant = new ArrayList<>();
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
						problemWithInv.append(readedLine).append(",");
					}
				}
				projectCore.setT_InvMatrix(t_invariantsMatrix, false);
				
				if(problems==0) {
					while(!((buffer.readLine()).contains("<Invariants names>"))) //przewiń do nazw inwariantów
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
						
						overlord.log(lang.getText("LOGentry00295a")+t_invariantsMatrix.size()+
								lang.getText("LOGentry00295b")+readLines+lang.getText("LOGentry00295c"), "error", true);
						return false;
					}
				} else {
					overlord.log(lang.getText("LOGentry00296")+" "+problemWithInv, "error", true);
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00297exception")+" "+t_invariantsProcessed
					+"\n"+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda pomocnicza czytająca z pliku projektu blok danych o p-inwariantach.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readPInvariants(BufferedReader buffer) {
		try {
			String line;
			while(!((buffer.readLine()).contains("<PlaceInv data>"))) //przewiń do inwariantów
				;
			
			line = buffer.readLine();
			int placeNumber = projectCore.getPlaces().size();
			StringBuilder problemWithInv = new StringBuilder();
			int problems = 0;
			int readedLine = -1;
			
			if(!line.contains("<PInvariants: 0>")) { //są miejsca
				boolean go = true;
				p_invariantsMatrix = new ArrayList<>();
				
				line = buffer.readLine();
				while(go) {
					readedLine++;
					ArrayList<Integer> invariant = new ArrayList<>();
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
						problemWithInv.append(readedLine).append(",");
					}
				}
				
				projectCore.setP_InvMatrix(p_invariantsMatrix);
				
				if(problems==0) {
					while(!((buffer.readLine()).contains("<PInvariants names>"))) //przewiń do nazw inwariantów
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
						overlord.log(lang.getText("LOGentry00298a")+p_invariantsMatrix.size()+
								lang.getText("LOGentry00298b")+readLines+lang.getText("LOGentry00298c"), "error", true);
						return false;
					}
				} else {
					overlord.log(lang.getText("LOGentry00299")+" "+problemWithInv, "error", true);
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00300exception")+" "+p_invariantsProcessed
					+"\n"+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda czyta blok danych o zbiorach MCT.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readMCT(BufferedReader buffer) {
		try {
			String line;
			while(!((buffer.readLine()).contains("<MCT data>"))) //przewiń do zbiorów MCT
				;
			
			line = buffer.readLine();
			ArrayList<Transition> transitions = projectCore.getTransitions();
			int transNumber = transitions.size();
			StringBuilder problemWithMCTLines = new StringBuilder();
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

					for (String s : tab) {
						int mctNumber = Integer.parseInt(s);
						if (mctNumber < transNumber) {
							mct.add(transitions.get(mctNumber));
						} else {
							problems++;
							problemWithMCTLines.append(readedLine).append(",");
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
					while(!((buffer.readLine()).contains("<MCT names>"))) //przewiń do nazw inwariantów
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
						overlord.log(lang.getText("LOGentry00301a")+mctData.size()+
								lang.getText("LOGentry00301b")+readLines+lang.getText("LOGentry00301c"), "error", true);
						return false;
					}
					
				} else {
					overlord.log(lang.getText("LOGentry00302")+" "+problemWithMCTLines, "error", true);
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00303exception")+" "+t_invariantsProcessed
					+"\n"+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda czyta blok danych o stanach sieci.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readStates(BufferedReader buffer) {
		try {
			String line;
			while(!((buffer.readLine()).contains("<States data>"))) //przewiń do wektorów stanów
				;
			
			line = buffer.readLine();
			int readedLine = 0;
			if(line.contains("States: 0"))
				return false;
			
			boolean go = true;
			P_StateManager statesMngr = projectCore.accessStatesManager();
			statesMngr.resetPN(false);

			line = buffer.readLine();
			try {
				while(go) {
					readedLine++;
					StatePlacesVector pVector = new StatePlacesVector();
					line = line.replace(" ", "");
					String[] tab = line.split(";");

					for (String s : tab) {
						pVector.accessVector().add(Double.parseDouble(s));
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
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00304exception")+" "+ex.getMessage(), "error", true);
			}
			
			if((readedLine /3) > statesMngr.accessStateMatrix().size()) {
				overlord.log(lang.getText("LOGentry00305")+" "+(readedLine), "error", true);
				if(statesMngr.accessStateMatrix().isEmpty()) {
					statesMngr.createCleanStatePN();
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00306exception")+" "+t_invariantsProcessed
					+"\n"+e.getMessage(), "error", true);
			return false;
		}
	}

	/**
	 * Metoda czyta blok danych o stanach sieci.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readStatesXTPN(BufferedReader buffer) {
		try {
			String line;
			while(!(buffer.readLine().contains("<States XTPN data>"))) //przewiń do wektorów stanów
				;

			line = buffer.readLine();
			int readedLine = 0;
			if(line.contains("States: 0"))
				return false;

			boolean go = true;
			P_StateManager statesMngr = projectCore.accessStatesManager();
			statesMngr.removeAllMultisets_M(false);

			line = buffer.readLine();
			try {
				int statesProcessed = 0;
				while(go) {
					readedLine++;
					MultisetM pVector = new MultisetM();
					line = line.replace(" ", "");
					String[] stateTable = line.split(";"); //separator multizbiorów
					int placesRead = 0;
					for (String multisetString : stateTable) {
						placesRead++;
						if(placesRead > this.placesProcessed) {
							overlord.log(lang.getText("LOGentry00307a")+statesProcessed+lang.getText("LOGentry00307b") +
									" "+lang.getText("LOGentry00307c")+this.placesProcessed+").", "error", true);
							break;
						} else {
							String[] multisetTab = multisetString.split(":"); //separator tokenów
							ArrayList<Double> multisetK = new ArrayList<>();
							int isXTPNplace = 1; //jeśli 1, to miejsce jest czasowe
							for(String token : multisetTab) {
								if(token.contains("(C)")) {
									token = token.replace("(C)", "");
									double tokenValue = Double.parseDouble(token); //musi być przeczytane jako double
									multisetK.add(tokenValue);
									isXTPNplace = 0; //miejsce klasyczne
									break; //technicznie, nie powinno być ŻADNYCH innych wartości w tym "multizbiorze" miejsca klasycznego!
								} else {
									double tokenValue = Double.parseDouble(token);
									if(tokenValue > -1.0) {//oznaczenie braku tokenów
										multisetK.add(tokenValue);
									}
								}
							}
							Collections.sort(multisetK);
							Collections.reverse(multisetK);
							pVector.addMultiset_K_toMultiset_M(multisetK, isXTPNplace);
						}
					}
					statesProcessed++;

					if(placesRead < this.placesProcessed) {
						for(int i = 0; i< this.placesProcessed -placesRead; i++) {
							pVector.addMultiset_K_toMultiset_M(new ArrayList<Double>(), 1);
						}
						overlord.log(lang.getText("LOGentry00308a")+statesProcessed+lang.getText("LOGentry00308b")+" "
								+placesRead+" "+lang.getText("LOGentry00308c")+" "+placesProcessed+" "
								+lang.getText("LOGentry00308d"), "error", true);
					}

					line = buffer.readLine(); //dane dodatkowe
					line = line.trim();
					stateTable = line.split(";");
					pVector.setStateType(stateTable[0]);

					line = buffer.readLine();
					line = line.trim();
					line = Tools.decodeString(line);
					pVector.setDescription(line);

					line = buffer.readLine();
					if(line.contains("<EOSt>")) {
						go = false;
					}

					statesMngr.accessStateMatrixXTPN().add(pVector);
				}
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00309exception")+" "+t_invariantsProcessed
						+"\n"+ex.getMessage(), "error", true);
			}
			if((readedLine /3) > statesMngr.accessStateMatrix().size()) {
				
				overlord.log(lang.getText("LOGentry00310")+" "+(readedLine), "error", true);
				if(statesMngr.accessStateMatrix().isEmpty()) {
					statesMngr.createCleanStatePN();
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00311exception") +"\n"+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Czyta dane wektorów odpaleń tranzycji w modelu SPN.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readFiringRates(BufferedReader buffer) {
		try {
			String line;
			while(!(buffer.readLine().contains("<Firing rates data>"))) //przewiń do wektorów firing rates
				;
			
			line = buffer.readLine();
			int readedLine = 0;
			if(line.contains("<FRvectors: 0>"))
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
							TransitionSPNExtension.StochaticsType subType = switch (tabType[i]) {
								case "DT" -> TransitionSPNExtension.StochaticsType.DT;
								case "IM" -> TransitionSPNExtension.StochaticsType.IM;
								case "SchT" -> TransitionSPNExtension.StochaticsType.SchT;
								default -> TransitionSPNExtension.StochaticsType.ST;
							};

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
				overlord.log(lang.getText("LOGentry00312exception")+" "+(readedLine)
						+"\n"+e.getMessage(), "error", true);
				if(frateMngr.accessSPNmatrix().isEmpty()) {
					frateMngr.createCleanSPNdataVector();
				}
			}
			
			if(readProtocol == 0) {
				if((readedLine/4) > frateMngr.accessSPNmatrix().size()) {
					overlord.log(lang.getText("LOGentry00313"), "error", true);
					if(frateMngr.accessSPNmatrix().isEmpty()) {
						frateMngr.createCleanSPNdataVector();
					} else {
						frateMngr.reset(false); //false!!!
						frateMngr.createCleanSPNdataVector();
					}
				}
			} else {
				if((readedLine/3) > frateMngr.accessSPNmatrix().size()) {
					if(frateMngr.accessSPNmatrix().isEmpty()) {
						frateMngr.createCleanSPNdataVector();
						overlord.log(lang.getText("LOGentry00313"), "error", true);
					} else {
						overlord.log(lang.getText("LOGentry00314"), "error", true);
					}
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00315exception")
					+"\n"+e.getMessage(), "error", true);
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
		if(dataVectorTable[0].contains("version101:")) {
			dataVectorTable[0] = dataVectorTable[0].replace("version101:", "");
			for(int i=0; i<transitionsProcessed; i++) {
				SPNtransitionData box = frVector.newContainer();
				box.ST_function = dataVectorTable[i*7];
				try { box.IM_priority = Integer.parseInt(dataVectorTable[(i*7)+1]); } catch(Exception ex) {

					overlord.log(lang.getText("LOGentry00316exception")+" "+ex.getMessage(), "error", true);
				}
				try { box.DET_delay = Integer.parseInt(dataVectorTable[(i*7)+2]); } catch(Exception ex) {
					overlord.log(lang.getText("LOGentry00317exception")+" "+ex.getMessage(), "error", true);
				}
				box.SCH_start = dataVectorTable[(i*7)+3];
				try { box.SCH_rep = Integer.parseInt(dataVectorTable[(i*7)+4]); } catch(Exception ex) {
					overlord.log(lang.getText("LOGentry00318exception")+" "+ex.getMessage(), "error", true);
				}
				box.SCH_end = dataVectorTable[(i*7)+5];

				switch (dataVectorTable[(i * 7) + 6]) {
					case "IM" -> box.sType = TransitionSPNExtension.StochaticsType.IM;
					case "DT" -> box.sType = TransitionSPNExtension.StochaticsType.DT;
					case "SchT" -> box.sType = TransitionSPNExtension.StochaticsType.SchT;
					default -> box.sType = TransitionSPNExtension.StochaticsType.ST;
				}
				spnVector.add(box);
			}
		}
		return spnVector;
	}

	/**
	 * Metoda czyta blok danych z wektorami SSA.
	 * @param buffer (<b>BufferedReader</b>) obiekt czytający.
	 * @return (<b>boolean</b>) - true, jeśli się udało.
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private boolean readSSAvectors(BufferedReader buffer) {
		try {
			String line;
			while(!(buffer.readLine().contains("<SSA vectors data>"))) //przewiń do wektorów SSA
				;
			
			line = buffer.readLine();
			int readedLine = 0;
			if(line.contains("<SSA vectors: 0>"))
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

					for (String s : tab) {
						pVector.accessVector().add(Double.parseDouble(s));
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
			} catch (Exception ex) {
				overlord.log(lang.getText("LOGentry00319exception")+" "+ex.getMessage(), "error", true);
			}
			
			if((readedLine /3) > ssaMngr.accessSSAmatrix().size()) {
				overlord.log(lang.getText("LOGentry00320")+" "+(readedLine), "error", true);
				if(ssaMngr.accessSSAmatrix().isEmpty()) {
					ssaMngr.createCleanSSAvector();
				}
			}
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00321exception")+"\n"+e.getMessage(), "error", true);
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

	private PlaceColored resurrectAsColoredPlace(Place place, int pID) {
		PlaceColored rise = new PlaceColored(pID, 0, new Point(20,20));
		rise.setPlaceType(Place.PlaceType.CPN);
		rise.setName(place.getName());
		rise.setComment(place.getComment());
		rise.setTokensNumber(place.getTokensNumber());
		rise.setPortal(place.isPortal());

		rise.getElementLocations().clear();
		for(ElementLocation elementLocation : place.getElementLocations()) {
			rise.getElementLocations().add(elementLocation);
		}
		rise.getTextsLocations(GUIManager.locationMoveType.NAME).clear();
		for(ElementLocation elementLocation : place.getTextsLocations(GUIManager.locationMoveType.NAME)) {
			rise.getTextsLocations(GUIManager.locationMoveType.NAME).add(elementLocation);
		}
		rise.getTextsLocations(GUIManager.locationMoveType.GAMMA).clear();
		for(ElementLocation elementLocation : place.getTextsLocations(GUIManager.locationMoveType.GAMMA)) {
			rise.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(elementLocation);
		}

		rise.isColored = true;
		if(tabPlaceTokens != null) {
			rise.setColorTokensNumber(Integer.parseInt(tabPlaceTokens[0]), 0);
			rise.setColorTokensNumber(Integer.parseInt(tabPlaceTokens[1]), 1);
			rise.setColorTokensNumber(Integer.parseInt(tabPlaceTokens[2]), 2);
			rise.setColorTokensNumber(Integer.parseInt(tabPlaceTokens[3]), 3);
			rise.setColorTokensNumber(Integer.parseInt(tabPlaceTokens[4]), 4);
			rise.setColorTokensNumber(Integer.parseInt(tabPlaceTokens[5]), 5);
		} else {
			rise.setColorTokensNumber(0, 0);
			rise.setColorTokensNumber(0, 1);
			rise.setColorTokensNumber(0, 2);
			rise.setColorTokensNumber(0, 3);
			rise.setColorTokensNumber(0, 4);
			rise.setColorTokensNumber(0, 5);
		}
		return rise;
	}

	private TransitionColored resurrectAsColoredTransition(Transition transition, int tID) {
		TransitionColored rise = new TransitionColored(tID, 0, new Point(20,20));
		rise.setTransType(TransitionType.CPN);
		rise.setName(transition.getName());
		rise.setComment(transition.getComment());
		rise.timeExtension.forceSetEFT(transition.timeExtension.getEFT());
		rise.timeExtension.setLFT(transition.timeExtension.getLFT());
		rise.timeExtension.setDPNduration(transition.timeExtension.getDPNduration());
		rise.timeExtension.setTPNstatus(transition.timeExtension.isTPN());
		rise.timeExtension.setDPNstatus(transition.timeExtension.isDPN());
		rise.fpnExtension.setFunctional(transition.fpnExtension.isFunctional());
		rise.setPortal(transition.isPortal());

		rise.getElementLocations().clear();
		for(ElementLocation elementLocation : transition.getElementLocations()) {
			rise.getElementLocations().add(elementLocation);
		}
		rise.getTextsLocations(GUIManager.locationMoveType.NAME).clear();
		for(ElementLocation elementLocation : transition.getTextsLocations(GUIManager.locationMoveType.NAME)) {
			rise.getTextsLocations(GUIManager.locationMoveType.NAME).add(elementLocation);
		}
		rise.getTextsLocations(GUIManager.locationMoveType.ALPHA).clear();
		for(ElementLocation elementLocation : transition.getTextsLocations(GUIManager.locationMoveType.ALPHA)) {
			rise.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(elementLocation);
		}
		rise.getTextsLocations(GUIManager.locationMoveType.BETA).clear();
		for(ElementLocation elementLocation : transition.getTextsLocations(GUIManager.locationMoveType.BETA)) {
			rise.getTextsLocations(GUIManager.locationMoveType.BETA).add(elementLocation);
		}

		if(tabTransReqTokens != null) {
			rise.setRequiredColoredTokens(Integer.parseInt(tabTransReqTokens[0]), 0);
			rise.setRequiredColoredTokens(Integer.parseInt(tabTransReqTokens[1]), 1);
			rise.setRequiredColoredTokens(Integer.parseInt(tabTransReqTokens[2]), 2);
			rise.setRequiredColoredTokens(Integer.parseInt(tabTransReqTokens[3]), 3);
			rise.setRequiredColoredTokens(Integer.parseInt(tabTransReqTokens[4]), 4);
			rise.setRequiredColoredTokens(Integer.parseInt(tabTransReqTokens[5]), 5);
		} else {
			rise.setRequiredColoredTokens(1, 0);
			rise.setRequiredColoredTokens(1, 1);
			rise.setRequiredColoredTokens(1, 2);
			rise.setRequiredColoredTokens(1, 3);
			rise.setRequiredColoredTokens(1, 4);
			rise.setRequiredColoredTokens(1, 5);
		}
		return rise;
	}
}
