package abyss.files.io.Snoopy;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.IdGenerator;
import abyss.math.pnElements.Arc;
import abyss.math.pnElements.ElementLocation;
import abyss.math.pnElements.Node;
import abyss.math.pnElements.Place;
import abyss.math.pnElements.Transition;
import abyss.math.pnElements.Arc.TypesOfArcs;

/**
 * Klasa odpowiedzialna za wczytywanie tego całego syfu, jaki w pliki ładuje niemiecki geszeft 
 * (baczność!) Snoopy (spocznij!).
 * 
 * @author MR
 *
 */
public class SnoopyReader {
	private boolean classPN = false;
	private boolean TPN = false;
	private boolean extPN = false;
	private boolean otherPN = false;
	
	private ArrayList<Arc> arcList = new ArrayList<Arc>();
	private ArrayList<Node> nodesList = new ArrayList<Node>();
	private ArrayList<Integer> snoopyNodesIdList = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> snoopyNodesElLocIDList = new ArrayList<ArrayList<Integer>>();
	
	private boolean warnings = false;
	/**
	 * Główny konstruktor klasy SnoopyReader.
	 * @param type int - typ sieci do wczytania
	 * @param path String - ścieżka do pliku
	 */
	public SnoopyReader(int type, String path) {
		if(type == 1) { //extended
			extPN = true;
		} else if(type == 2) { //TPN
			TPN = true;
		} else if(type == 3) { //other
			otherPN = true;
		} else { // classic
			classPN = true;
		}
		
		coreReader(path);
	}
	
	/**
	 * Metoda główna, delegująca zadania czytania odpowiednich bloków danych w pliku do metod
	 * pomocniczych.
	 * @param filepath String - ścieżka do pliku
	 */
	private void coreReader(String filepath) {
		BufferedReader buffer = null;
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(filepath));
			buffer = new BufferedReader(new InputStreamReader(dis));
			GUIManager.getDefaultGUIManager().log("Reading Snoopy file: "+filepath, "text", true);	
			
			readNodesBlock(buffer);
			
			readArcsBlock(buffer);
			
			//buffer.close();
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy net failed.", "error", true);
		} finally {
			try { buffer.close(); }
			catch (Exception e2) {}
		}
	}

	/**
	 * Metoda czyta blok węzłów sieci: P/T/cP/cT.
	 * @param buffer BufferedReader - obiekt czytający
	 * @throws IOException - coś wybuchło
	 */
	private void readNodesBlock(BufferedReader buffer) throws IOException {
		String line = buffer.readLine();
		// PLACES
		if(!line.contains("</nodeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<nodeclass count=") && !line.contains("</nodeclasses>")) {//przewiń do miejsc
			line = buffer.readLine();
		}
		if(!line.contains("<nodeclass count=\"0\"") && line.contains("name=\"Place\"")) { //są jakieś miejsca
			readSnoopyPlaces(buffer, line);
		}
		
		// TRANSITIONS
		if(!line.contains("</nodeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<nodeclass count=") && !line.contains("</nodeclasses>")) {//przewiń do tranzycji
			line = buffer.readLine();
		}
		if(!line.contains("<nodeclass count=\"0\"") && line.contains("name=\"Transition\"")) { //są jakieś tranzycje
			readSnoopyTransitions(buffer, line);
		}
		
		// COARSE PLACES
		if(!line.contains("</nodeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<nodeclass count=") && !line.contains("</nodeclasses>")) { //przewiń do c-miejsc
			line = buffer.readLine();
		}
		if(!line.contains("<nodeclass count=\"0\"")) { //są jakieś c-miejsca
			readSnoopyCoarsePlaces(buffer, line);
		}
		
		// COARSE TRANSITIONS
		if(!line.contains("</nodeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<nodeclass count=") && !line.contains("</nodeclasses>")) { //przewiń do c-tranzycji
			line = buffer.readLine();
		}
		if(!line.contains("<nodeclass count=\"0\"")) { //są jakieś c-tranzycje
			readSnoopyCoarseTransitions(buffer, line);
		}
	}
	
	/**
	 * Metoda czytająca blok miejsc pliku Snoopiego.
	 * @param buffer BufferedReader - obiekt czytający
	 * @param line String - ostatnio przeczytana linia
	 */
	private void readSnoopyPlaces(BufferedReader buffer, String line) {
		String backFirstLine = line;
		int placesCounter = -1;
		int placesLimit = 0;
		try {
			//policz teoretyczną liczbę miejsc
			line = line.substring(line.indexOf("count=")+7); //<nodeclass count="44" name="Place">
			line = line.substring(0, line.indexOf("\""));
			placesLimit = Integer.parseInt(line) - 1;
			
			boolean readForward = true;
			boolean goBoldly = true;
			
			while(true) {
				while(!line.contains("<node id=\"")) { //na początku: <node id="226" net="1">
					line = buffer.readLine();
					
					if(line.contains("</nodeclass>")) {
						readForward = false;
						break;
					}
				}
				if(!readForward)
					break;
				
				//buffLine = buffer.readLine(); 
				
				if(line.contains("</nodeclass>")) {
					break;
				}
				
				
				int nodeSnoopyID = (int) getAttributeValue(line, " id=\"", -1);
				if(nodeSnoopyID == -1) {
					GUIManager.getDefaultGUIManager().log("Catastrophic error: could not read Snoopy Place ID from line: "+backFirstLine, "error", true);
					break;
				} else {
					snoopyNodesIdList.add(nodeSnoopyID);
				}
				int subNet = (int) getAttributeValue(line, " net=\"", 0);
				if(subNet > 0)
					subNet--;
				
				Place place = new Place(IdGenerator.getNextId(), subNet, new Point(100,100));
				nodesList.add(place);
				placesCounter++;
				
				goBoldly = true;
				int logicalELNumber_names = -1;
				int logicalELNumber_graphics = -1;
				
				while(goBoldly) { //czytanie właściwości miejsca	
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //1st
					
					line = buffer.readLine();
					
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					// Nazwa miejsca
					if(line.contains("<attribute name=\"Name\"")) {
						String name = "Locus"+placesCounter;
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								name = readStrCDATA(buffer, line, name);
								place.setName(name);
							} 
							
							if(line.contains("<graphic ")) { //lokalizacje nazwy (offsets)
								logicalELNumber_names++;
								int xOff = (int) getAttributeValue(line, " xoff=\"", 0);
								int yOff = (int) getAttributeValue(line, " yoff=\"", 0);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								if(sub != 0)
									sub--;
								
								yOff -= 20; //20 default, czyli 0 w oY w Abyss
								if(yOff < -8)
									yOff = -55; //nad node, uwzględnia różnicę
								
								if(!GUIManager.getDefaultGUIManager().getSettingsManager().getValue("usesSnoopyOffsets").equals("1")) {
									xOff = 0;
									yOff = 0;
								}
								
								if(logicalELNumber_names == 0) { 
									//użyty konstruktor dla place w super-klasie Node utworzył już pierwszy ElementLocation (names_off)
									place.getNamesLocations().set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
								} else {
									place.getNamesLocations().add(new ElementLocation(sub, new Point(xOff,yOff), place));
								}
							}
						}
						line = buffer.readLine();
					}
					
					// ID
					if(line.contains("<attribute name=\"ID\"")) {
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore
							;
						}
						line = buffer.readLine();
					}
					
					// Tokeny
					if(line.contains("<attribute name=\"Marking\"")) {
						int tokens = 0;
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								tokens = (int) readDoubleCDATA(buffer, line, 0);
							} 
						}
						place.setTokensNumber(tokens);
						line = buffer.readLine();
					}
					
					// Logic
					if(line.contains("<attribute name=\"Logic\"")) {
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore
							;
						}
						line = buffer.readLine();
					}

					// Komentarz do miejsca
					if(line.contains("<attribute name=\"Comment\"")) {
						String wittyComment = "";
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								wittyComment = readStrCDATA(buffer, line, "");
							}
						}
						place.setComment(wittyComment);
						line = buffer.readLine();
					}
					
					// XY Locations
					if(line.contains("<graphics count=\"")) {
						ArrayList<Integer> subIDs = new ArrayList<Integer>();
						while(!(line = buffer.readLine()).contains("</graphics>")) {
							if(line.contains("<graphic ")) {
								logicalELNumber_graphics++;
								int x = (int) getAttributeValue(line, " x=\"", 100);
								int y = (int) getAttributeValue(line, " y=\"", 100);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								int elLocID = (int) getAttributeValue(line, " id=\"", 0);
								if(sub != 0)
									sub--;
								
								if(logicalELNumber_graphics == 0) { 
									//użyty konstruktor dla place w super-klasie Node utworzył już pierwszy ElementLocation
									place.getElementLocations().set(0, new ElementLocation(sub, new Point(x, y), place));
									subIDs.add(elLocID);
								} else {
									place.getElementLocations().add(new ElementLocation(sub, new Point(x, y), place));
									subIDs.add(elLocID);
								}
							}
						}
						snoopyNodesElLocIDList.add(subIDs);
					}
				} //czytanie właściwości węzła
				
				if(logicalELNumber_graphics > 1)
					place.setPortal(true);
				
				if(logicalELNumber_graphics != logicalELNumber_names) {
					GUIManager.getDefaultGUIManager().log("Critical error: names locations number and portals locations number vary for place "
							+place.getName(), "error", true);
				}
			}
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy places failed in line:", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "error", true);
		}
		
		if(placesCounter != placesLimit) {
			warnings = true;
			GUIManager.getDefaultGUIManager().log("Warning: places read: "+(placesCounter+1)+
					", places number set in file: "+(placesLimit+1), "warning", true);
		}
	}

	/**
	 * Metoda czytająca blok tranzycji pliku Snoopiego.
	 * @param buffer BufferedReader - obiekt czytający
	 * @param line String - ostatnio przeczytana linia
	 */
	private void readSnoopyTransitions(BufferedReader buffer, String line) {
		String backFirstLine = line;
		int transitionsCounter = -1;
		int transitionsLimit = 0;
		try {
			//policz teoretyczną liczbę tranzycji
			line = line.substring(line.indexOf("count=")+7); //<nodeclass count="44" name="Place">
			line = line.substring(0, line.indexOf("\""));
			transitionsLimit = Integer.parseInt(line) - 1;
			
			boolean readForward = true;
			boolean goBoldly = true;
			
			while(true) {
				while(!line.contains("<node id=\"")) { //na początku:  <node id="270" net="1">
					line = buffer.readLine();
					
					if(line.contains("</nodeclass>")) {
						readForward = false;
						break;
					}
				}
				if(!readForward)
					break;

				if(line.contains("</nodeclass>")) {
					break;
				}
				
				
				int nodeSnoopyID = (int) getAttributeValue(line, " id=\"", -1);
				if(nodeSnoopyID == -1) {
					GUIManager.getDefaultGUIManager().log("Catastrophic error: could not read Snoopy Transition ID from line: "+backFirstLine, "error", true);
					break;
				} else {
					snoopyNodesIdList.add(nodeSnoopyID);
				}
				int subNet = (int) getAttributeValue(line, " net=\"", 0);
				if(subNet > 0)
					subNet--;
				
				Transition transition = new Transition(IdGenerator.getNextId(), subNet, new Point(100,100));
				nodesList.add(transition);
				transitionsCounter++;
				
				goBoldly = true;
				int logicalELNumber_names = -1;
				int logicalELNumber_graphics = -1;
				
				while(goBoldly) { //czytanie właściwości miejsca	
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //1st
					
					line = buffer.readLine();
					
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					// Nazwa tranzycji
					if(line.contains("<attribute name=\"Name\"")) {
						String name = "Locus"+transitionsCounter;
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								name = readStrCDATA(buffer, line, name);
								transition.setName(name);
							} 
							
							if(line.contains("<graphic ")) { //lokalizacje nazwy (offsets)
								logicalELNumber_names++;
								int xOff = (int) getAttributeValue(line, " xoff=\"", 0);
								int yOff = (int) getAttributeValue(line, " yoff=\"", 0);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								if(sub != 0)
									sub--;
								
								yOff -= 20; //20 default, czyli 0 w oY w Abyss
								if(yOff < -8)
									yOff = -55; //nad node, uwzględnia różnicę
								
								if(!GUIManager.getDefaultGUIManager().getSettingsManager().getValue("usesSnoopyOffsets").equals("1")) {
									xOff = 0;
									yOff = 0;
								}
								
								if(logicalELNumber_names == 0) { 
									//użyty konstruktor dla transition w super-klasie Node utworzył już pierwszy ElementLocation (names_off)
									transition.getNamesLocations().set(0, new ElementLocation(sub, new Point(xOff,yOff), transition));
								} else {
									transition.getNamesLocations().add(new ElementLocation(sub, new Point(xOff,yOff), transition));
								}
							}
						}
						line = buffer.readLine();
					}
					
					// ID
					if(line.contains("<attribute name=\"ID\"")) {
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore
							;
						}
						line = buffer.readLine();
					}
					
					// Logic
					if(line.contains("<attribute name=\"Logic\"")) {
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore
							;
						}
						line = buffer.readLine();
					}

					// Komentarz do tranzycji
					if(line.contains("<attribute name=\"Comment\"")) {
						String wittyComment = "";
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								wittyComment = readStrCDATA(buffer, line, "");
							}
						}
						transition.setComment(wittyComment);
						line = buffer.readLine();
					}
					
					// XY Locations
					if(line.contains("<graphics count=\"")) {
						ArrayList<Integer> subIDs = new ArrayList<Integer>();
						while(!(line = buffer.readLine()).contains("</graphics>")) {
							if(line.contains("<graphic ")) {
								logicalELNumber_graphics++;
								int x = (int) getAttributeValue(line, " x=\"", 100);
								int y = (int) getAttributeValue(line, " y=\"", 100);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								int elLocID = (int) getAttributeValue(line, " id=\"", 0);
								if(sub != 0)
									sub--;
								
								if(logicalELNumber_graphics == 0) { 
									//użyty konstruktor dla place w super-klasie Node utworzył już pierwszy ElementLocation
									transition.getElementLocations().set(0, new ElementLocation(sub, new Point(x, y), transition));
									subIDs.add(elLocID);
								} else {
									transition.getElementLocations().add(new ElementLocation(sub, new Point(x, y), transition));
									subIDs.add(elLocID);
								}
							}
						}
						snoopyNodesElLocIDList.add(subIDs);
					}
				} //czytanie właściwości węzła
				
				if(logicalELNumber_graphics > 0)
					transition.setPortal(true);
				
				if(logicalELNumber_graphics != logicalELNumber_names) {
					GUIManager.getDefaultGUIManager().log("Critical error: names locations number and portals locations number vary for transition "
							+transition.getName(), "error", true);
				}
			}
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy transitions failed in line:", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "error", true);
		}
		
		if(transitionsCounter != transitionsLimit) {
			warnings = true;
			GUIManager.getDefaultGUIManager().log("Warning: places read: "+(transitionsCounter+1)+
					", places number set in file: "+(transitionsLimit+1), "warning", true);
		}
	}
	
	private void readSnoopyCoarsePlaces(BufferedReader buffer, String line) {
		String buffLine = line;
		try {
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy coarse places failed in line:", "error", true);
			GUIManager.getDefaultGUIManager().log(buffLine, "error", true);
		}
	}
	
	private void readSnoopyCoarseTransitions(BufferedReader buffer, String line) {
		String buffLine = line;
		try {

			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy coarse transitions failed in line: ", "error", true);
			GUIManager.getDefaultGUIManager().log(buffLine, "error", true);
		}
	}
	
	/**
	 * Metoda czyta blok łuków sieci
	 * @param buffer BufferedReader - obiekt czytający
	 * @throws IOException - coś wybuchło
	 */
	private void readArcsBlock(BufferedReader buffer) throws IOException {
		String line = "";
		
		while(!((line = buffer.readLine()).contains(" <edgeclasses count=\""))) //przewiń do łuków
			;
		
		int loc = line.indexOf("count=\"");
		String txtNumber = line.substring(loc + 7);
		loc = txtNumber.indexOf("\"");
		txtNumber = txtNumber.substring(0, loc);
		int arcClasses = Integer.parseInt(txtNumber);
	
		// Edges
		line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypesOfArcs.NORMAL);
		}
		
		// Read Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Read Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypesOfArcs.READARC);
		}
		
		// Inhibitor Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Inhibitor Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypesOfArcs.INHIBITOR);
		}
		
		// Reset Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Reset Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypesOfArcs.RESET);
		}
		
		// Equal Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Equal Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypesOfArcs.EQUAL);
		}
		
	}
	
	private void readEdges(BufferedReader buffer, String line, TypesOfArcs arcType) {
		String backFirstLine = line;
		int edgesCounter = -1;
		int edgesLimit = 0;
		try {
			//policz teoretyczną liczbę tranzycji
			line = line.substring(line.indexOf("count=")+7); // <edgeclass count="4" name="Edge">
			line = line.substring(0, line.indexOf("\""));
			edgesLimit = Integer.parseInt(line) - 1;
			
			boolean readForward = true;
			boolean goBoldly = true;
			
			while(true) {
				while(!line.contains("<edge source=\"")) { //na początku:  <node id="270" net="1">
					line = buffer.readLine();
					
					if(line.contains("</edgeclass>")) {
						readForward = false;
						break;
					}
				}
				if(!readForward)
					break;

				if(line.contains("</edgeclass>")) {
					break;
				}
				
				
				int nodeSourceID = (int) getAttributeValue(line, " source=\"", -1);
				int nodeTargetID = (int) getAttributeValue(line, " target=\"", -1);
				if(nodeSourceID == -1 || nodeTargetID == -1) {
					GUIManager.getDefaultGUIManager().log("Catastrophic error: could not read Snoopy source/target ID for arc from line: "+backFirstLine, "error", true);
					break;
				}
				
				int subNet = (int) getAttributeValue(line, " net=\"", 0);
				if(subNet > 0)
					subNet--;

				int sourceLocationInNodes = snoopyNodesIdList.indexOf(nodeSourceID);
				int targetLocationInNodes = snoopyNodesIdList.indexOf(nodeTargetID);
					
				goBoldly = true;
				int multiplicity = 0;
				String comment = ""; 
				
				while(goBoldly) { //czytanie właściwości miejsca	
					if(line.contains("</edge>")) {
						goBoldly = false;
						continue;
					} //1st
					
					line = buffer.readLine();
					
					if(line.contains("</edge>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					// Waga łuku
					if(line.contains("<attribute name=\"Multiplicity\"")) {
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								multiplicity = (int) readDoubleCDATA(buffer, line, 0);
							} 
						}
						line = buffer.readLine();
					}

					// Komentarz łuku
					if(line.contains("<attribute name=\"Comment\"")) {
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								comment = readStrCDATA(buffer, line, "");
							}
						}
						line = buffer.readLine();
					}
					
					// XY Locations
					if(line.contains("<graphics count=\"")) {
						while(!(line = buffer.readLine()).contains("</graphics>")) {
							if(line.contains("<graphic ")) {
								int sourceID = (int) getAttributeValue(line, " source=\"", -1);
								int targetID = (int) getAttributeValue(line, " target=\"", -1);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								if(sub != 0)
									sub--;
								
								int eLsourceLocation = snoopyNodesElLocIDList.get(sourceLocationInNodes).indexOf(sourceID);
								int eLtargetLocation = snoopyNodesElLocIDList.get(targetLocationInNodes).indexOf(targetID);
								
								
								ElementLocation sourceEL = nodesList.get(sourceLocationInNodes).getElementLocations().get(eLsourceLocation);
								ElementLocation targetEL = nodesList.get(targetLocationInNodes).getElementLocations().get(eLtargetLocation);
								
								Arc nArc = new Arc(sourceEL, targetEL, comment, multiplicity, arcType);
								arcList.add(nArc);
								edgesCounter++;
							}
						}
					}
				} //czytanie właściwości węzła
			}
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy edges failed in line: ", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "error", true);
		}
		
		if(edgesCounter != edgesLimit) {
			warnings = true;
			GUIManager.getDefaultGUIManager().log("Warning: edges ("+arcType+") read: "+(edgesCounter+1)+
					", number set in file: "+(edgesLimit+1), "warning", true);
		}
	}

	/**
	 * Metoda wycina wartość liczbową zadanego parametru z linii.
	 * @param line String - linia z pliku
	 * @param signature String - atrybut (format: ' nazwa="'    )
	 * @param defaultVal double - wartość domyślna, jeśli nie da się przeczytać
	 * @return double - wartość atrybutu
	 */
	private double getAttributeValue(String line, String signature, double defaultVal) {
		double result = defaultVal;
		try {
			int location = line.indexOf(signature);
			String tmp = line.substring(location + signature.length());
			location = tmp.indexOf("\"");
			tmp = tmp.substring(0, location);
			result = Double.parseDouble(tmp);
		} catch (Exception e) {	
		}
		return result;
	}
	
	/**
	 * Metoda zwraca wartość we sekcji  <![CDATA[wartosc]]>. W razie potrzeby doczytuje brakujące linie.
	 * @param buffer BufferedReader - obiekt czytający
	 * @param line String - ostatnio pobrana linia z pliku
	 * @param defaultVal String - wartość domyślna jeśli wystąpi błąd
	 * @return String - wartość wewnątrz CDATA[...]
	 */
	private String readStrCDATA(BufferedReader buffer, String line, String defaultVal) {
		try {
			String resultLine = line;
			boolean reset = false;
			while(!(line.contains("]]>"))) {
				buffer.mark(1024);
				line = buffer.readLine();
				resultLine += line;
				reset = true;
				
				//if(line.contains("    <"))
					//break;
			}
			if(reset)
				buffer.reset();
			
			//e.g.:
			//          <![CDATA[DNA containing an APE-bound AP site
			//
			//do tego miejsca wchodza wszystkie monofunkcyjne]]>
			
			int location = resultLine.indexOf("[CDATA[");
			resultLine = resultLine.substring(location + 7);
			location = resultLine.indexOf("]]>");
			resultLine = resultLine.substring(0, location);
			return resultLine;
		} catch (Exception e) {	
			return defaultVal;
		}
	}
	
	/**
	 * Metoda zwraca wartość we sekcji  <![CDATA[wartosc]]>. W razie potrzeby doczytuje brakujące linie.
	 * Wartość zwracana jest jako double.
	 * @param buffer BufferedReader - obiekt czytający
	 * @param line String - ostatnio pobrana linia z pliku
	 * @param defaultVal double - wartość domyślna jeśli wystąpi błąd
	 * @return double - wartość wewnątrz CDATA[...]
	 */
	private double readDoubleCDATA(BufferedReader buffer, String line, double defaultVal) {
		try {
			String resultLine = line;
			boolean reset = false;
			while(!(line.contains("]]>"))) {
				buffer.mark(1024);
				line = buffer.readLine();
				resultLine += line;
				reset = true;
				
				//if(line.contains("    <"))
					//break;
			}
			if(reset)
				buffer.reset();
			
			//e.g.:
			//          <![CDATA[DNA containing an APE-bound AP site
			//
			//do tego miejsca wchodza wszystkie monofunkcyjne]]>
			int location = resultLine.indexOf("[CDATA[");
			resultLine = resultLine.substring(location + 7);
			location = resultLine.indexOf("]]>");
			resultLine = resultLine.substring(0, location);
			return Double.parseDouble(resultLine);
		} catch (Exception e) {	
			return defaultVal;
		}
	}
	
	/**
	 * Metoda pozwala pobrać listę łuków.
	 * @return arcList - lista z łukami sieci
	 */
	public ArrayList<Arc> getArcList() {
		return arcList;
	}

	/**
	 * Metoda pozwala pobrać wierzchołków.
	 * @return nodesList - zwraca listę z wierzchołkami sieci
	 */
	public ArrayList<Node> getNodesList() {
		return nodesList;
	}
}
