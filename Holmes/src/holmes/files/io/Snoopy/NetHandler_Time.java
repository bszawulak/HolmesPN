package holmes.files.io.Snoopy;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.Transition.TransitionType;

/**
 * Parser sieci czasowych (Snoopy)
 * 
 * @author MR (na bazie NetHandler_Classic)
 *
 */
public class NetHandler_Time extends NetHandler {
	// Zmienne boolowskie parsera
	public boolean Snoopy = false;
	public boolean node = false;
	public boolean atribute = false;
	public boolean graphics = false;
	public boolean graphic = false;
	public boolean points = false;
	public boolean point = false;
	public boolean edgeclass = false;
	public boolean edge = false;
	public boolean metadata = false;
	public boolean endAtribute = false;
	
	//TPN:
	public boolean colTPN = false;
	public boolean colDPN = false;
	public boolean readEFT = false;
	public boolean readLFT = false;
	public boolean readDPN = false;
	public boolean timeTrans = false;

	public ArrayList<ElementLocation> elementLocationsList = new ArrayList<ElementLocation>();
	public ArrayList<Integer> graphicPointsIdList = new ArrayList<Integer>();
	public ArrayList<Integer> graphicPointsNetNumbers = new ArrayList<Integer>();
	public ArrayList<Point> graphicPointsList = new ArrayList<Point>();
	public ArrayList<ElementLocation> globalElementLocationList = new ArrayList<ElementLocation>();
	public int globalNetsCounted = 0;
	public boolean coarseCatcher = false;
	
	// Edge
	public int arcMultiplicity;
	public String arcComment = "";
	public int arcSource;
	public int arcTarget;
	public boolean variableMultiplicity = false;
	// Node
	public boolean variableName = false;
	public boolean variableMarking = false;
	public boolean variableLogic = false;
	public boolean variableComent = false;
	public boolean variableInterval = false; //odczyt TPN
	public boolean variableDuration = false; //odczyt DPN
	// Data
	public Node tmpNode;
	public String nodeType;
	public String nodeName;
	public int nodeID;
	public int nodeSID;
	public int nodeMarking;
	public int nodeLogic;
	public String nodeComment;
	// TPN/DPN
	public double nodeEFT;
	public double nodeLFT;
	public double duration;
	
	public String readString = "";
	public ArrayList<Transition> tmpTransitionList = new ArrayList<Transition>();
	public ArrayList<Point> graphicNamesPointsList = new ArrayList<Point>();
	public int xoff_name;
	public int yoff_name;
	
	private boolean anyProblems = false;

	/**
	 * Metoda wykrywająca rozpoczęcie nowego elementu.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 * @param attributes - atrybut elementu
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("Snoopy")) {
			Snoopy = true;
			nodeSID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().returnCleanSheetID();//GUIManager.getDefaultGUIManager().getWorkspace().newTab();
		}
		// Ustawianie typu noda
		if (qName.equalsIgnoreCase("nodeclass")) {
			if (attributes.getValue(1).equals("Place")) {
				coarseCatcher = false;
				nodeType = "Place";
			} else if (attributes.getValue(1).equals("Coarse Place")) {
				coarseCatcher = true;
				nodeType = "Place";
			} else if (attributes.getValue(1).equals("Transition")) {
				coarseCatcher = false;
				nodeType = "Transition";
			} else if (attributes.getValue(1).equals("Coarse Transition")){
				coarseCatcher = true;
				nodeType = "Transition";
			}
		}
		
		if (qName.equalsIgnoreCase("metadataclass")) {
			if(anyProblems) {
				anyProblems = false;
				GUIManager.getDefaultGUIManager().log("Problems encountered during loading file.", "error", true);
			}
		}
		
		if (qName.equalsIgnoreCase("node")) {
			node = true;
			nodeID = IdGenerator.getNextId(); // Integer.parseInt(attributes.getValue(0));
		}
		
		if (qName.equalsIgnoreCase("attribute")) {
			atribute = true;
			endAtribute = false;
			if (attributes.getValue(0).equals("Name")) {
				variableName = true;
			}
			if (attributes.getValue(0).equals("Marking")) {
				variableMarking = true;
			}
			if (attributes.getValue(0).equals("Logic")) {
				variableLogic = true;
			}
			if (attributes.getValue(0).equals("Comment")) {
				variableComent = true;
			}
			if (attributes.getValue(0).equals("Multiplicity")) {
				variableMultiplicity = true;
			}
			if (attributes.getValue(0).equals("Interval")) {
				variableInterval = true; //sekcja Interval TPN, sklada sie z dalszych podwezlow (inaczej, niz powyzsze if'y)
			}
			if (attributes.getValue(0).equals("Duration")) {
				variableDuration = true; //sekcja Interval TPN, sklada sie z dalszych podwezlow (inaczej, niz powyzsze if'y)
			}
		}
		
		if (qName.equalsIgnoreCase("colList_body") && variableDuration == true) {
			colDPN = true; //jestesmy w sekcji gdzie bedą czasy udpalenia
		}
		
		if(readDPN) {
			try {
				duration = Double.parseDouble(readString);
			} catch (Exception e) {
				duration = 0.0;
				anyProblems = true;
				GUIManager.getDefaultGUIManager().log("Warning. Node "+nodeName+" had unassigned duration value. Duration set to zero.", "warning", true);
			}
			
			readDPN = false;
			timeTrans = true;
			colDPN = false;
			variableDuration = false;
		}
		
		
		if(colDPN == true && readString.equals("Main")) {
			readDPN = true;
		}
		
		
		if (qName.equalsIgnoreCase("colList_body") && variableInterval == true) {
			colTPN = true; //jestesmy w sekcji gdzie beda zmienne czasowe EFT i LFT
		}
		if(readLFT) {
			try {
				nodeLFT = Double.parseDouble(readString);
			} catch (Exception e) {
				nodeLFT = 0.0;
				anyProblems = true;
				GUIManager.getDefaultGUIManager().log("Warning. Node "+nodeName+" had unassigned lft value. Lft set to zero.", "warning", true);
			}
			readLFT = false;
			readEFT = false;
			colTPN = false;
			variableInterval = false;
			timeTrans = true;
		}
		if(readEFT) {
			try {
				nodeEFT = Double.parseDouble(readString);
			} catch (Exception e) {
				nodeEFT = 0.0;
				anyProblems = true;
				GUIManager.getDefaultGUIManager().log("Warning. Node "+nodeName+" had unassigned eft value. Eft set to zero.", "warning", true);
			}
			
			readLFT = true;
			readEFT = false;
		}
		
		if(colTPN == true && readString.equals("Main")) {
			readEFT = true;
		}
		
		if (qName.equalsIgnoreCase("graphics")) graphics = true;
		if (qName.equalsIgnoreCase("graphic")) graphic = true;

		// Wczytywanie informacji odnosnie ID i pozycji noda

		if ((endAtribute == true) && (atribute == false) && (graphics == true)
				&& (graphic == true) && (metadata == false)
				&& (edgeclass == false) && (point == false)
				&& (points == false) && (node == true)) {
			if (attributes.getQName(0).equals("x")) {
				double xPos = Double.parseDouble(attributes.getValue(0));
				double yPos = Double.parseDouble(attributes.getValue(1));
				int snoopyID = Integer.parseInt(attributes.getValue(2));
				int netNumber = Integer.parseInt(attributes.getValue(3));
				
				//TODO:
				double resizeFactor = 1;
				try {
					int addF = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("programSnoopyLoaderNetExtFactor"));
					resizeFactor += ((double)addF/(double)100);
				} catch (Exception e) { }
				
				xPos *= resizeFactor;
				yPos *= resizeFactor;
				
				int p1 = (int) xPos;
				int p2 = (int) yPos;
				
				graphicPointsList.add(new Point(p1, p2));
				graphicPointsIdList.add(snoopyID);
				graphicPointsNetNumbers.add(netNumber);
			}
		}
		if (qName.equalsIgnoreCase("points")) {
			points = true;
		}
		if (qName.equalsIgnoreCase("point")) {
			point = true;
		}

		if (qName.equalsIgnoreCase("edgeclass")) {
			edgeclass = true;
		}
		if (qName.equalsIgnoreCase("edge")) {
			edge = true;
		}
		
		if(variableName && graphics && qName.equalsIgnoreCase("graphic")) {
			String tmp1 = attributes.getLocalName(0);
			String tmp2 = attributes.getLocalName(1);
			String xoff = "";
			String yoff = "";
			
			if(tmp1.equals("xoff") && tmp2.equals("yoff") ) { //oba na miejscu
				xoff = attributes.getValue(0);
				yoff = attributes.getValue(1);
			} else if(tmp1.equals("xoff") && !tmp2.equals("yoff") ) { 
				//brak y, durne Snoopy nie pisze 0 tylko wywala cały atrybut... co za barany to pisały...
				xoff = attributes.getValue(0);
				yoff = "0.00";
			} else if(tmp1.equals("yoff") ) { //brak x
				yoff = attributes.getValue(0);
				xoff = "0.00";
			} else if(!tmp1.equals("xoff") && !tmp2.equals("yoff")) { //brak x i y
				xoff = "0.00";
				yoff = "0.00";
			}
			
			xoff_name = 0;
			yoff_name = 0;
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorUseSnoopyOffsets").equals("1")) {
				try {
					xoff_name = (int)Float.parseFloat(xoff);
					yoff_name = (int)Float.parseFloat(yoff);
					//comment, bo i tak jest przesunięcie w lewo domyslnie w Snoopy
					//xoff_name -= 22; //25 default, 0 w oX w Holmes to ustawienie na 3 - centrum, czyli 22 (25-3)
					yoff_name -= 20; //20 default, czyli 0 w oY w Holmes
					if(yoff_name < -8)
						yoff_name = -55; //nad node, uwzględnia różnicę
				} catch (Exception e) {} 
			}
			graphicNamesPointsList.add(new Point(xoff_name, yoff_name)); //dodanie do listy (portal)
		}

		// Zapis do zmiennej globalnej ID source i targetArc

		if ((endAtribute == true) && (atribute == false) && (graphics == true)
				&& (graphic == true) && (metadata == false)
				&& (edgeclass == true) && (point == false) && (points == false)
				&& (node == false) && (edge == true)) {

			arcSource = Integer.parseInt(attributes.getValue(2));
			arcTarget = Integer.parseInt(attributes.getValue(3));
		}
		
		if (qName.equalsIgnoreCase("metadata")) {
			metadata = true;
		}
	}

	/**
	 * Metoda wykrywająca koniec bieżącego. To w niej po wczytaniu elementu i
	 * wszystkich jego własności, zostaje uruchomiony konkretny konstruktor
	 * odpowiedzialny za utworzenie nowego wierzchołka, lub łuku.
	 * @param uri - adres zasobu
	 * @param localName - lokalna nazwa elementu
	 * @param qName - nazwa elementu
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equalsIgnoreCase("points")) points = false;
		if (qName.equalsIgnoreCase("point")) point = false;
		if (qName.equalsIgnoreCase("edgeclass")) edgeclass = false;
		if (qName.equalsIgnoreCase("edge")) edge = false;
		if (qName.equalsIgnoreCase("node")) node = false;
		if (qName.equalsIgnoreCase("graphics")) graphics = false;
		if (qName.equalsIgnoreCase("graphic")) graphic = false;
		
		// Zapis atrybutow noda i arca

		if (qName.equalsIgnoreCase("attribute")) {
			if (node == true) {
				if (variableName == true) {
					nodeName = readString;
					variableName = false;
					readString = "";
				}
				if (variableMarking == true) {
					if (readString.equals("")) {
						nodeMarking = 0;
					} else {
						nodeMarking = Integer.parseInt(readString);
						variableMarking = false;
						readString = "";
					}
				}
				if (variableLogic == true) {
					if (readString.equals("")) {
						nodeLogic = 0;
					} else {
						nodeLogic = Integer.parseInt(readString);
						variableLogic = false;
						readString = "";
					}
				}
				if (variableComent == true) {
					nodeComment = readString;
					variableComent = false;
					readString = "";
				}
				if (variableInterval == true && colTPN == true) {
					if(!readString.equals("Main")) {
						nodeLFT = Double.parseDouble(readString);
					}
					colTPN = false;
					readString = "";
				}
			}
			if (edge == true && atribute == true) {

				if (variableMultiplicity == true) {
					if (readString.equals("")) {
						arcMultiplicity = 0;
					} else {
						arcMultiplicity = Integer.parseInt(readString);
					}
					variableMultiplicity = false;
					readString = "";
				}
				if (variableComent == true) {
					nodeComment = readString;
					variableComent = false;
					readString = "";
				}
			}
			endAtribute = true;
			atribute = false;
		}

		if (qName.equalsIgnoreCase("Snoopy")) {
			int wid = Toolkit.getDefaultToolkit().getScreenSize().width - 20;
			int hei = Toolkit.getDefaultToolkit().getScreenSize().height - 20;
			int SIN = GUIManager.getDefaultGUIManager().IDtoIndex(nodeSID);
			int tmpX = 0;
			int tmpY = 0;
			boolean xFound = false;
			boolean yFound = false;
			GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(SIN).getGraphPanel();
			for (int l = 0; l < globalElementLocationList.size(); l++) {
				if (globalElementLocationList.get(l).getPosition().x > wid) {
					tmpX = l;
					xFound = true;
					wid = globalElementLocationList.get(l).getPosition().x;
				}
				if (globalElementLocationList.get(l).getPosition().y > hei) {
					tmpY = l;
					yFound = true;
					hei = globalElementLocationList.get(l).getPosition().y;
				}
			}
			if (xFound == true && yFound == false) {
				graphPanel.setSize(new Dimension(globalElementLocationList.get(tmpX).getPosition().x + 90, graphPanel.getSize().height));
			}
			if (yFound == true && xFound == false) {
				graphPanel.setSize(new Dimension(graphPanel.getSize().width,globalElementLocationList.get(tmpY).getPosition().y + 90));
			}
			if (xFound == true && yFound == true) {
				graphPanel.setSize(new Dimension(globalElementLocationList.get(tmpX)
					.getPosition().x + 90, globalElementLocationList.get(tmpY).getPosition().y + 90));
			}

			// Tablice lukow dla element location
			nodesList.addAll(tmpTransitionList);
		}

		// Tworzenie noda i wszystkich jego element location
		if (qName.equalsIgnoreCase("node")) {
			elementLocationsList = new ArrayList<ElementLocation>();
			ArrayList<ElementLocation> namesElLocations = new ArrayList<ElementLocation>();
			ArrayList<ElementLocation> alphaLoc = new ArrayList<ElementLocation>();
			ArrayList<ElementLocation> betaLoc = new ArrayList<ElementLocation>();
			ArrayList<ElementLocation> gammaLoc = new ArrayList<ElementLocation>();
			ArrayList<ElementLocation> tauLoc = new ArrayList<ElementLocation>();

			if(graphicPointsList.size() != graphicNamesPointsList.size()) {
				GUIManager.getDefaultGUIManager().log("Critical error reading Snoopy file. Wrong number of names locations and nodes locations.", "error", true);
			}
			
			for (int k = 0; k < graphicPointsList.size(); k++) {
				elementLocationsList.add(new ElementLocation(nodeSID, graphicPointsList.get(k), null));
				namesElLocations.add(new ElementLocation(nodeSID, graphicNamesPointsList.get(k), null));
			}

			for (int u = 0; u < elementLocationsList.size(); u++) {
				globalElementLocationList.add(elementLocationsList.get(u));
			}
			
			if (nodeType == "Place") {
				Place tmpPlace = new Place(nodeID, elementLocationsList, nodeName, nodeComment, nodeMarking);
				tmpPlace.setTextsLocations(namesElLocations, GUIManager.locationMoveType.NAME);

				//XTPN node preparation (just in case)
				for(int i=0; i<namesElLocations.size(); i++) {
					int x = namesElLocations.get(i).getPosition().x;
					int y = namesElLocations.get(i).getPosition().y;
					int _sheetID = namesElLocations.get(i).getSheetID();
					alphaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
					betaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
					gammaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
					tauLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
				}
				tmpPlace.setTextsLocations(alphaLoc, GUIManager.locationMoveType.ALPHA);
				tmpPlace.setTextsLocations(betaLoc, GUIManager.locationMoveType.BETA);
				tmpPlace.setTextsLocations(gammaLoc, GUIManager.locationMoveType.GAMMA);
				tmpPlace.setTextsLocations(tauLoc, GUIManager.locationMoveType.TAU);

				nodesList.add(tmpPlace);
				IdGenerator.getNextPlaceId();
				
				//tmpNode = new Place(nodeID, tmpElementLocationList, nodeName, nodeComment, nodeMarking);
				//nodesList.add(tmpNode);
			} else {	
				if(timeTrans) {			
					timeTrans = false;
					//TimeTransition tmpTTran = new TimeTransition(nodeID, tmpElementLocationList, nodeName, nodeComment);
					Transition tmpTTran = new Transition(nodeID, elementLocationsList, nodeName, nodeComment);
					tmpTTran.setEFT(nodeEFT);
					tmpTTran.setLFT(nodeLFT);
					tmpTTran.setDPNduration(duration);
					tmpTTran.setTextsLocations(namesElLocations, GUIManager.locationMoveType.NAME);

					//XTPN node preparation (just in case)
					for(int i=0; i<namesElLocations.size(); i++) {
						int x = namesElLocations.get(i).getPosition().x;
						int y = namesElLocations.get(i).getPosition().y;
						int _sheetID = namesElLocations.get(i).getSheetID();
						alphaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
						betaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
						gammaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
						tauLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
					}
					tmpTTran.setTextsLocations(alphaLoc, GUIManager.locationMoveType.ALPHA);
					tmpTTran.setTextsLocations(betaLoc, GUIManager.locationMoveType.BETA);
					tmpTTran.setTextsLocations(gammaLoc, GUIManager.locationMoveType.GAMMA);
					tmpTTran.setTextsLocations(tauLoc, GUIManager.locationMoveType.TAU);

					tmpTTran.setTransType(TransitionType.TPN);
					if(duration > 0)
						tmpTTran.setDPNstatus(true);
					
					if(nodeEFT == nodeLFT && nodeEFT == 0) 
						tmpTTran.setTPNstatus(false);
					
					tmpTTran.setTPNstatus(true);
					tmpTransitionList.add(tmpTTran);
					
					IdGenerator.getNextTransitionId();
				} else {
					Transition tmpTran = new Transition(nodeID, elementLocationsList, nodeName, nodeComment);
					tmpTran.setTextsLocations(namesElLocations, GUIManager.locationMoveType.NAME);

					//XTPN node preparation (just in case)
					for(int i=0; i<namesElLocations.size(); i++) {
						int x = namesElLocations.get(i).getPosition().x;
						int y = namesElLocations.get(i).getPosition().y;
						int _sheetID = namesElLocations.get(i).getSheetID();
						alphaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
						betaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
						gammaLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
						tauLoc.add(new ElementLocation(_sheetID, new Point(x, y), null));
					}
					tmpTran.setTextsLocations(alphaLoc, GUIManager.locationMoveType.ALPHA);
					tmpTran.setTextsLocations(betaLoc, GUIManager.locationMoveType.BETA);
					tmpTran.setTextsLocations(gammaLoc, GUIManager.locationMoveType.GAMMA);
					tmpTran.setTextsLocations(tauLoc, GUIManager.locationMoveType.TAU);

					tmpTransitionList.add(tmpTran);
					IdGenerator.getNextTransitionId();
				}
			}

			// Zerowanie zmiennych
			nodeName = "";
			nodeID = 0;
			nodeMarking = 0;
			nodeLogic = 0;
			nodeComment = "";
			nodeEFT = 0.0;
			nodeLFT = 0.0;
			node = false;
			graphicPointsList.clear();
			graphicNamesPointsList.clear();
		}

		// Tworzenie łuku
		if (qName.equalsIgnoreCase("edge")) {
			int tmpSource = 0;
			int tmpTarget = 0;
			for (int j = 0; j < graphicPointsIdList.size(); j++) {
				if (graphicPointsIdList.get(j) == arcSource) {
					tmpSource = j;
				}
				if (graphicPointsIdList.get(j) == arcTarget) {
					tmpTarget = j;
				}
			}
			Arc nArc = new Arc(globalElementLocationList.get(tmpSource),
					globalElementLocationList.get(tmpTarget), arcComment, arcMultiplicity, TypeOfArc.NORMAL);
			arcList.add(nArc);
			edge = false;
		}
	}

	/**
	 * Metoda odczytująca zawartość elementu.
	 * @param ch (char[]) tablica wczytanych znaków.
	 * @param start (int) indeks początkowy.
	 * @param length (int) ilość wczytanych znaków.
	 */
	public void characters(char ch[], int start, int length) throws SAXException {
		// Wyluskiwanie zawartosci <![CDATA[]]>
		if (((node == true) || edge == true) && (atribute == true)) {
			String temper = "";
			char tm[] = new char[20000];

			for (int i = 0; i < length; i++) {
				tm[i] = ch[i + start];
				temper = temper + ch[i + start];
			}
			if (tm[0] != '\n') {
				readString = temper;
			}
		}
	}

	/**
	 * Metoda służąca do wyłapywania i ignorowania pustych przestrzeni.
	 * @param ch (char[]) tablica wczytanych znaków.
	 * @param start (int) indeks początkowy.
	 * @param length (int) wielkość pustej przestrzeni.
	 */
	public void ignorableWhitespace(char ch[], int start, int length)
			throws SAXException {
	}
}
