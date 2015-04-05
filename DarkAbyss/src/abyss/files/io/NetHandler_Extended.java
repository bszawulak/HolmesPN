package abyss.files.io;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
//import org.xml.sax.helpers.DefaultHandler;


import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.graphpanel.IdGenerator;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.math.Arc.TypesOfArcs;

/**
 * 
 * @author students - szkielet klasy, nazwa, 4 metody, ogólnie nic :)
 * @author MR - cała reszta, czyli w zasadzie też jeszcze nic
 *
 */
public class NetHandler_Extended extends NetHandler {
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

		private ArrayList<ElementLocation> tmpElementLocationList = new ArrayList<ElementLocation>();
		private ArrayList<Integer> graphicPointsIdList = new ArrayList<Integer>();
		public ArrayList<Point> graphicPointsList = new ArrayList<Point>();
		private ArrayList<ElementLocation> elementLocationList = new ArrayList<ElementLocation>();
		public ArrayList<Point> graphicNamesPointsList = new ArrayList<Point>();

		// Edge
		public int arcMultiplicity;
		public String arcComment = "";
		public int arcSource;
		public int arcTarget;
		public boolean variableMultiplicity = false;
		public String arcType = "";
		// Node
		public boolean variableName = false;
		public boolean variableMarking = false;
		public boolean variableLogic = false;
		public boolean variableComent = false;
		//public Node tmpNode;
		public String nodeType;
		public String nodeName;
		public int nodeID;
		public int nodeSID;
		public int nodeMarking;
		public int nodeLogic;
		public String nodeComment;
		public String readString = "";
		public ArrayList<Transition> tmpTransitionList = new ArrayList<Transition>();
		
		public int xoff_name;
		public int yoff_name;

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

			// Ustawianie typu wierzchołka
			if (qName.equalsIgnoreCase("nodeclass")) {
				if (attributes.getValue(1).equals("Place")) {
					nodeType = "Place";
				} else {
					nodeType = "Transition";
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
			}
			
			if (qName.equalsIgnoreCase("graphics")) {
				graphics = true;
			}
			
			if (qName.equalsIgnoreCase("graphic")) {
				graphic = true;
			}
			
			//wczytywanie osobnego wektora danych o przesunięciu każdego napisu względem elementu sieci
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
				
				if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("usesSnoopyOffsets").equals("1")) {
					try {
						xoff_name = (int)Float.parseFloat(xoff);
						yoff_name = (int)Float.parseFloat(yoff);
						//comment, bo i tak jest przesunięcie w lewo domyslnie w Snoopy
						//xoff_name -= 22; //25 default, 0 w oX w Abyss to ustawienie na 3 - centrum, czyli 22 (25-3)
						yoff_name -= 20; //20 default, czyli 0 w oY w Abyss
						if(yoff_name < -8)
							yoff_name = -55; //nad node, uwzględnia różnicę
					} catch (Exception e) {} 
				}

				graphicNamesPointsList.add(new Point(xoff_name, yoff_name)); //dodanie do listy (portal)
			}

			// Wczytywanie informacji odnosnie ID i pozycji noda

			if ((endAtribute == true) && (atribute == false) && (graphics == true)
					&& (graphic == true) && (metadata == false)
					&& (edgeclass == false) && (point == false)
					&& (points == false) && (node == true)) {
				if (attributes.getQName(0).equals("x")) {
					double o1 = Double.parseDouble(attributes.getValue(0));
					double o2 = Double.parseDouble(attributes.getValue(1));
					int o3 = Integer.parseInt(attributes.getValue(2));
					
					//TODO:
					double resizeFactor = 1;
					try {
						int addF = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("netExtFactor"));
						resizeFactor = ((double)addF/(double)100);
						
						if(resizeFactor==0)
							resizeFactor=1;
					} catch (Exception e) { }
					
					o1 *= resizeFactor;
					o2 *= resizeFactor;
					
					int p1 = (int) o1;
					int p2 = (int) o2;
					graphicPointsList.add(new Point(p1, p2));
					graphicPointsIdList.add(o3);
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
				arcType= attributes.getValue(1);
			}
			if (qName.equalsIgnoreCase("edge")) {
				edge = true;
			}

			// Zapis do zmiennej globalnej ID sorce i target Arca
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
			if (qName.equalsIgnoreCase("points")) {
				points = false;
			}
			if (qName.equalsIgnoreCase("point")) {
				point = false;
			}

			if (qName.equalsIgnoreCase("edgeclass")) {
				edgeclass = false;
			}
			if (qName.equalsIgnoreCase("edge")) {
				edge = false;
			}
			if (qName.equalsIgnoreCase("node")) {
				node = false;
			}
			if (qName.equalsIgnoreCase("graphics")) {
				graphics = false;
			}
			if (qName.equalsIgnoreCase("graphic")) {
				graphic = false;
			}

			// Zapis atrybutów wierzchołka i łuku
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
				for (int l = 0; l < elementLocationList.size(); l++) {
					if (elementLocationList.get(l).getPosition().x > wid) {
						tmpX = l;
						xFound = true;
						wid = elementLocationList.get(l).getPosition().x;
					}
					if (elementLocationList.get(l).getPosition().y > hei) {
						tmpY = l;
						yFound = true;
						hei = elementLocationList.get(l).getPosition().y;
					}
				}
				if (xFound == true && yFound == false) {
					graphPanel.setSize(new Dimension(elementLocationList.get(tmpX).getPosition().x + 90, graphPanel.getSize().height));
				}
				if (yFound == true && xFound == false) {
					graphPanel.setSize(new Dimension(graphPanel.getSize().width, elementLocationList.get(tmpY).getPosition().y + 90));
				}
				if (xFound == true && yFound == true) { //z każdym nowym punktem dostosowujemy rozmiar sieci
					graphPanel.setSize(new Dimension(elementLocationList.get(tmpX).getPosition().x + 90, 
							elementLocationList.get(tmpY).getPosition().y + 90));
				}

				// Tablice łuków dla ElementLocation
				nodesList.addAll(tmpTransitionList);
			}

			// Tworzenie wierzchołka i wszystkich jego ElementLocation

			if (qName.equalsIgnoreCase("node")) {
				tmpElementLocationList = new ArrayList<ElementLocation>();
				ArrayList<ElementLocation> namesElLocations = new ArrayList<ElementLocation>();
				
				if(graphicPointsList.size() != graphicNamesPointsList.size()) {
					GUIManager.getDefaultGUIManager().log("Critical error reading Snoopy file. Wrong number of names locations and nodes locations.", "error", true);
				}
				
				for (int i = 0; i < graphicPointsList.size(); i++) {
					tmpElementLocationList.add(new ElementLocation(nodeSID, graphicPointsList.get(i), null));
					namesElLocations.add(new ElementLocation(nodeSID, graphicNamesPointsList.get(i), null));
				}
				
				for (int j = 0; j < tmpElementLocationList.size(); j++) {
					elementLocationList.add(tmpElementLocationList.get(j));
				}
				
				if (nodeType == "Place") {
					Place tmpPlace = new Place(nodeID, tmpElementLocationList, nodeName, nodeComment, nodeMarking);
					tmpPlace.setNamesLocations(namesElLocations);
					nodesList.add(tmpPlace);
				} else {	
					Transition tmpTran = new Transition(nodeID, tmpElementLocationList, nodeName, nodeComment);
					tmpTran.setNamesLocations(namesElLocations);
					tmpTransitionList.add(tmpTran);
				}

				// zerowanie zmiennych
				nodeName = "";
				nodeID = 0;
				nodeMarking = 0;
				nodeLogic = 0;
				nodeComment = "";
				node = false;
				graphicPointsList.clear();
				graphicNamesPointsList.clear();
			}

			// tworzenie łuku

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
				//arcComment += arcType;
				Arc newArc = new Arc(elementLocationList.get(tmpSource), elementLocationList.get(tmpTarget), arcComment, arcMultiplicity, TypesOfArcs.NORMAL);
				arcList.add(newArc);
				
				if(arcType.equals("Read Edge")) {
					Arc nArc2 = new Arc(elementLocationList.get(tmpTarget), elementLocationList.get(tmpSource), arcComment, arcMultiplicity, TypesOfArcs.READARC);
					arcList.add(nArc2);
				} else if(arcType.equals("Inhibitor Edge")) {
					newArc.setArcType(TypesOfArcs.INHIBITOR);
				} else if(arcType.equals("Reset Edge")) {
					newArc.setArcType(TypesOfArcs.RESET);
				} else if(arcType.equals("Equal Edge")) {
					newArc.setArcType(TypesOfArcs.EQUAL);
				}
				
				edge = false;
				
				//arcType = "";
				arcComment = "";
				arcMultiplicity = 0;
			}
		}

		/**
		 * Metoda odczytująca zawartość elementu.
		 * @param ch[] - tablica wczytanych znaków
		 * @param start - indeks początkowy
		 * @param length - ilość wczytanych znaków
		 */
		public void characters(char ch[], int start, int length) throws SAXException {
			// Wyłuskiwanie zawartosci <![CDATA[]]>
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
		 * @param ch[] - tablica wczytanych znaków
		 * @param start - indeks początkowy
		 * @param length - wielkość pustej przestrzeni
		 */
		public void ignorableWhitespace(char ch[], int start, int length)
			throws SAXException { }
}
