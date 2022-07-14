package holmes.files.io.Snoopy;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.petrinet.elements.Transition.TransitionType;

/**
 * Klasa odpowiedzialna za wczytywanie tego całego syfu, jaki w pliki ładuje niemiecki geszeft 
 * (baczność!) Snoopy (spocznij!).
 * 
 * @author MR
 *
 */
public class SnoopyReader {
	//private boolean classPN = false;
	//private boolean TPN = false;
	//private boolean extPN = false;
	//private boolean otherPN = false;
	
	private ArrayList<Arc> arcList = new ArrayList<Arc>();
	private ArrayList<Node> nodesList = new ArrayList<Node>();
	
	private ArrayList<Integer> snoopyNodesIdList = new ArrayList<Integer>();
	/** Chwilowo do niczego nie potrzebny wektor, łuki idą do snoopyCoarseNodesElLocIDList */
	private ArrayList<Integer> snoopyCoarseNodesIdList = new ArrayList<Integer>();
	private ArrayList<ArrayList<Integer>> snoopyNodesElLocIDList = new ArrayList<ArrayList<Integer>>();
	private ArrayList<ArrayList<Integer>> snoopyCoarseNodesElLocIDList = new ArrayList<ArrayList<Integer>>();
	
	private boolean warnings = false;
	/**
	 * Główny konstruktor klasy SnoopyReader.
	 * @param type int - typ sieci do wczytania
	 * @param path String - ścieżka do pliku
	 */
	public SnoopyReader(int type, String path) {
		coreReader(path);
		
		if(warnings)
			GUIManager.getDefaultGUIManager().log("Warnings while reading file.", "error", true);
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
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy net failed.", "error", true);
		} finally {
			try { 
				buffer.close(); 
			}
			catch (Exception ignored) {

			}
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
	@SuppressWarnings("StatementWithEmptyBody")
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
				
				if(nodeSnoopyID == 951675) {
					@SuppressWarnings("unused")
					int x=1;
				}
				
				goBoldly = true;
				int logicalELNumber_names = -1;
				int logicalELNumber_graphics = -1;
				boolean firstPass = true;
				boolean readAnything = false;
				while(goBoldly) { //czytanie właściwości miejsca	
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //1st
					
					if(!line.contains("<attribute name=\""))
						line = buffer.readLine();
					
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					if(!firstPass) {
						if(!readAnything) {
							while(!(line = buffer.readLine()).contains("</attribute>") ) {

							}
							line = buffer.readLine();
						}
					}
					firstPass = false;
					readAnything = false;
					
					// Nazwa miejsca
					if(line.contains("<attribute name=\"Name\"")) {
						readAnything = true;
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
								
								yOff -= 20; //20 default, czyli 0 w oY w Holmes
								if(yOff < -8)
									yOff = -55; //nad node, uwzględnia różnicę
								
								if(!GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorUseSnoopyOffsets").equals("1")) {
									xOff = 0;
									yOff = 0;
								}
								
								if(logicalELNumber_names == 0) { 
									//użyty konstruktor dla place w super-klasie Node utworzył już pierwszy ElementLocation (names_off)
									place.getTextsLocations(GUIManager.locationMoveType.NAME).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
									//XTPN
									place.getTextsLocations(GUIManager.locationMoveType.ALPHA).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
									place.getTextsLocations(GUIManager.locationMoveType.BETA).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
									place.getTextsLocations(GUIManager.locationMoveType.GAMMA).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
									place.getTextsLocations(GUIManager.locationMoveType.TAU).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
								} else {
									place.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(xOff,yOff), place));
									//XTPN
									place.getTextsLocations(GUIManager.locationMoveType.ALPHA).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
									place.getTextsLocations(GUIManager.locationMoveType.BETA).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
									place.getTextsLocations(GUIManager.locationMoveType.GAMMA).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
									place.getTextsLocations(GUIManager.locationMoveType.TAU).set(0, new ElementLocation(sub, new Point(xOff,yOff), place));
								}
							}
						}
						line = buffer.readLine();
					}
					
					// ID
					if(line.contains("<attribute name=\"ID\"")) {
						readAnything = true;
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore

						}
						line = buffer.readLine();
					}
					
					// Tokeny
					if(line.contains("<attribute name=\"Marking\"")) {
						readAnything = true;
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
						readAnything = true;
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore

						}
						line = buffer.readLine();
					}

					// Komentarz do miejsca
					if(line.contains("<attribute name=\"Comment\"")) {
						readAnything = true;
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
						readAnything = true;
						ArrayList<Integer> subIDs = new ArrayList<Integer>();
						while(!(line = buffer.readLine()).contains("</graphics>")) {
							if(line.contains("<graphic ")) {
								logicalELNumber_graphics++;
								int x = (int) getAttributeValue(line, " x=\"", 100);
								int y = (int) getAttributeValue(line, " y=\"", 100);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								int elLocID = (int) getAttributeValue(line, " id=\"", 0);
								String brush = getAttributeValueStr(line, " brush=\"", "255,255,255");
								setBrushColor(place, brush);
								
								if(sub != 0)
									sub--;
								
								double resizeFactor = 1;
								try {
									int addF = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("programSnoopyLoaderNetExtFactor"));
									resizeFactor = ((double)addF/(double)100);
									
									if(resizeFactor==0)
										resizeFactor=1;
								} catch (Exception ignored) { }
								
								x *= resizeFactor;
								y *= resizeFactor;
								
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
				
				if(logicalELNumber_graphics > 0)
					place.setPortal(true);
				
				if(logicalELNumber_graphics != logicalELNumber_names) {
					warnings = true;
					GUIManager.getDefaultGUIManager().log("Warning: names locations number and graphics locations number vary for place "
							+place.getName(), "warning", true);
					GUIManager.getDefaultGUIManager().log(" Fix: resetting names locations (offsets) array.", "warning", true);
					
					place.getTextsLocations(GUIManager.locationMoveType.NAME).clear();
					place.getTextsLocations(GUIManager.locationMoveType.ALPHA).clear();
					place.getTextsLocations(GUIManager.locationMoveType.BETA).clear();
					place.getTextsLocations(GUIManager.locationMoveType.GAMMA).clear();
					place.getTextsLocations(GUIManager.locationMoveType.TAU).clear();

					for(int i=0; i<place.getElementLocations().size(); i++) {
						int sub = place.getElementLocations().get(i).getSheetID();
						place.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(0, 0), place));
						place.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sub, new Point(0, 0), place));
						place.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sub, new Point(0, 0), place));
						place.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sub, new Point(0, 0), place));
						place.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sub, new Point(0, 0), place));

					}
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
	 * Metoda ustawia kolor miejsca lub tranzycji na wczytany z pliku Snoopiego.
	 * @param node Node - miejsce lub tranzycja
	 * @param brush String - np. 255,255,255
	 */
	private void setBrushColor(Node node, String brush) {
		try {
			String[] tableRGB = brush.split(",");
			int r = Integer.parseInt(tableRGB[0]);
			int g = Integer.parseInt(tableRGB[1]);
			int b = Integer.parseInt(tableRGB[2]);
			
			if(r == 255 && g == 255 && b == 255)
				return; //zostaw domyślny
					
			if(node instanceof Place) {
				((Place)node).defColor = new Color(r,g,b);
			} else if(node instanceof Transition) {
				((Transition)node).defColor = new Color(r,g,b);
			}
		} catch (Exception ignored) {
			
		}
	}

	/**
	 * Metoda czytająca blok tranzycji pliku Snoopiego.
	 * @param buffer BufferedReader - obiekt czytający
	 * @param line String - ostatnio przeczytana linia
	 */
	@SuppressWarnings("StatementWithEmptyBody")
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
				boolean firstPass = true;
				boolean readAnything = false;
				while(goBoldly) { //czytanie właściwości miejsca
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //1st

					if(!line.contains("<attribute name=\""))
						line = buffer.readLine();
					
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					if(!firstPass) {
						if(!readAnything) {
							while(!(line = buffer.readLine()).contains("</attribute>") ) {

							}
							line = buffer.readLine();
						}
					}
					firstPass = false;
					readAnything = false;
					// Nazwa tranzycji
					if(line.contains("<attribute name=\"Name\"")) {
						readAnything = true;
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
								
								yOff -= 20; //20 default, czyli 0 w oY w Holmes
								if(yOff < -8)
									yOff = -55; //nad node, uwzględnia różnicę
								
								if(!GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorUseSnoopyOffsets").equals("1")) {
									xOff = 0;
									yOff = 0;
								}
								
								if(logicalELNumber_names == 0) { 
									//użyty konstruktor dla transition w super-klasie Node utworzył już pierwszy ElementLocation (names_off)
									transition.getTextsLocations(GUIManager.locationMoveType.NAME).set(0, new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).set(0, new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.BETA).set(0, new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.GAMMA).set(0, new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.TAU).set(0, new ElementLocation(sub, new Point(xOff,yOff), transition));
								} else {
									transition.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sub, new Point(xOff,yOff), transition));
									transition.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sub, new Point(xOff,yOff), transition));
								}
							}
						}
						line = buffer.readLine();
					}
					
					// ID
					if(line.contains("<attribute name=\"ID\"")) {
						readAnything = true;
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore

						}
						line = buffer.readLine();
					}
					
					// Logic
					if(line.contains("<attribute name=\"Logic\"")) {
						readAnything = true;
						while(!(line = buffer.readLine()).contains("</attribute>")) { //ignore

						}
						line = buffer.readLine();
					}

					// Komentarz do tranzycji
					if(line.contains("<attribute name=\"Comment\"")) {
						readAnything = true;
						String wittyComment = "";
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								wittyComment = readStrCDATA(buffer, line, "");
							}
						}
						transition.setComment(wittyComment);
						line = buffer.readLine();
					}
					
					// Duration part
					if(line.contains("<attribute name=\"Duration\"")) {
						readAnything = true;
						ArrayList<Object> tableVector = new ArrayList<Object>();
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								tableVector.add(readStrCDATA(buffer, line, ""));
							}
						}
						parseDuration(transition, tableVector);
						line = buffer.readLine();
					}
					
					// Duration part
					if(line.contains("<attribute name=\"Interval\"")) {
						readAnything = true;
						ArrayList<Object> tableVector = new ArrayList<Object>();
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								tableVector.add(readStrCDATA(buffer, line, ""));
							}
						}
						parseIntervals(transition, tableVector);
						line = buffer.readLine();
					}
					
					// XY Locations
					if(line.contains("<graphics count=\"")) {
						readAnything = true;
						ArrayList<Integer> subIDs = new ArrayList<Integer>();
						while(!(line = buffer.readLine()).contains("</graphics>")) {
							if(line.contains("<graphic ")) {
								logicalELNumber_graphics++;
								int x = (int) getAttributeValue(line, " x=\"", 100);
								int y = (int) getAttributeValue(line, " y=\"", 100);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								int elLocID = (int) getAttributeValue(line, " id=\"", 0);
								String brush = getAttributeValueStr(line, " brush=\"", "255,255,255");
								setBrushColor(transition, brush);
								if(sub != 0)
									sub--;
								
								double resizeFactor = 1;
								try {
									int addF = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("programSnoopyLoaderNetExtFactor"));
									resizeFactor = ((double)addF/(double)100);
									
									if(resizeFactor==0)
										resizeFactor=1;
								} catch (Exception ignored) { }
								
								x *= resizeFactor;
								y *= resizeFactor;
								
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
						
						if(transition.getDPNstatus() || transition.getTPNstatus())
							transition.setTransType(TransitionType.TPN);
					}
				} //czytanie właściwości węzła
				
				if(logicalELNumber_graphics > 0)
					transition.setPortal(true);
				
				if(logicalELNumber_graphics != logicalELNumber_names) {
					warnings = true;
					GUIManager.getDefaultGUIManager().log("Warning: names locations number and graphics locations number vary for transition "
							+transition.getName(), "warning", true);
					GUIManager.getDefaultGUIManager().log(" Fix: resetting names locations (offsets) array.", "warning", true);
					
					transition.getTextsLocations(GUIManager.locationMoveType.NAME).clear();
					transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).clear();
					transition.getTextsLocations(GUIManager.locationMoveType.BETA).clear();
					transition.getTextsLocations(GUIManager.locationMoveType.GAMMA).clear();
					transition.getTextsLocations(GUIManager.locationMoveType.TAU).clear();
					for(int i=0; i<transition.getElementLocations().size(); i++) {
						int sub = transition.getElementLocations().get(i).getSheetID();
						transition.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(0, 0), transition));
						transition.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sub, new Point(0, 0), transition));
						transition.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sub, new Point(0, 0), transition));
						transition.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sub, new Point(0, 0), transition));
						transition.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sub, new Point(0, 0), transition));
					}
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
	
	private void parseDuration(Transition transition, ArrayList<Object> tableVector) {
		// 0, 1 pierwszy wiersz (nazwy), 2 - tytuł wiersza, 3 wartość I wiersz, 4 jak 2, 5 jak 3
		try {
			transition.setDPNduration(Double.parseDouble((String) tableVector.get(3)));
			transition.setDPNstatus(transition.getDPNduration() != 0);
		} catch (Exception e) {
			transition.setDPNduration(0);
			transition.setDPNstatus(false);
		}
	}
	
	private void parseIntervals(Transition transition, ArrayList<Object> tableVector) {
		// 0,1,2 - tytuł, eft, lft, 3,4,5 - I wiersz (4,5 - wartości), 6,7,8 - tak jak 3,4,5
		//uwaga, mogą być znaki '?'
		try {
			double eft = Double.parseDouble((String) tableVector.get(4));
			double lft = Double.parseDouble((String) tableVector.get(5));
			transition.setLFT(lft);
			transition.setEFT(eft);
			transition.setTPNstatus(true);
		} catch (Exception e) {
			transition.setEFT(0);
			transition.setLFT(0);
			transition.setTPNstatus(true);
		}
	}

	/**
	 * Metoda czytająca blok coarse-places w pliku Snoopiego.
	 * @param buffer BufferedReader - obiekt odczytujący
	 * @param line String - ostatnio przeczytana linia
	 */
	private void readSnoopyCoarsePlaces(BufferedReader buffer, String line) {
		String backFirstLine = line;
		int coarsePlacesCounter = -1;
		int coarsePlacesLimit = 0;
		try {
			//policz teoretyczną liczbę c-miejsc
			line = line.substring(line.indexOf("count=")+7); // <nodeclass count="1" name="Coarse Place">
			line = line.substring(0, line.indexOf("\""));
			coarsePlacesLimit = Integer.parseInt(line) - 1;
			
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
				int nodeCoarseNumber = (int) getAttributeValue(line, " coarse=\"", -1);
				if(nodeSnoopyID == -1) {
					GUIManager.getDefaultGUIManager().log("Catastrophic error: could not read Snoopy Coarse Place ID from line: "+backFirstLine, "error", true);
					break;
				} else {
					snoopyNodesIdList.add(nodeSnoopyID);
					snoopyCoarseNodesIdList.add(nodeSnoopyID);
				}
				int subNet = (int) getAttributeValue(line, " net=\"", 0);
				if(subNet > 0)
					subNet--;
				
				MetaNode metaNode = new MetaNode(subNet, IdGenerator.getNextId(), new Point(100,100), MetaType.SUBNETPLACE);
				metaNode.setRepresentedSheetID(nodeCoarseNumber-1);
				nodesList.add(metaNode);
				coarsePlacesCounter++;
				
				goBoldly = true;
				int logicalELNumber_names = -1;
				int logicalELNumber_graphics = -1;
				boolean firstPass = true;
				boolean readAnything = false;
				while(goBoldly) { //czytanie właściwości c-miejsca	
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //1st
					
					if(!line.contains("<attribute name=\""))
						line = buffer.readLine();
					
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					if(!firstPass) {
						if(!readAnything) {
							while(!(line = buffer.readLine()).contains("</attribute>") ) {

							}
							line = buffer.readLine();
						}
					}
					firstPass = false;
					readAnything = false;
					
					// Nazwa c-miejsca
					if(line.contains("<attribute name=\"Name\"")) {
						readAnything = true;
						String name = "cPlace"+coarsePlacesCounter;
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								name = readStrCDATA(buffer, line, name);
								metaNode.setName(name);
							} 
							
							if(line.contains("<graphic ")) { //lokalizacje nazwy (offsets)
								logicalELNumber_names++;
								int xOff = (int) getAttributeValue(line, " xoff=\"", 0);
								int yOff = (int) getAttributeValue(line, " yoff=\"", 0);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								if(sub != 0)
									sub--;
								
								yOff -= 20; //20 default, czyli 0 w oY w Holmes
								if(yOff < -8)
									yOff = -55; //nad node, uwzględnia różnicę
								
								if(!GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorUseSnoopyOffsets").equals("1")) {
									xOff = 0;
									yOff = 0;
								}
								
								if(logicalELNumber_names == 0) { 
									//użyty konstruktor dla place w super-klasie Node utworzył już pierwszy ElementLocation (names_off)
									metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
								} else {
									metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
								}
							}
						}
						line = buffer.readLine();
					}

					// Komentarz do c-miejsca
					if(line.contains("<attribute name=\"Comment\"")) {
						readAnything = true;
						String wittyComment = "";
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								wittyComment = readStrCDATA(buffer, line, "");
							}
						}
						metaNode.setComment(wittyComment);
						line = buffer.readLine();
					}
					
					// XY Locations
					if(line.contains("<graphics count=\"")) {
						readAnything = true;
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
								
								double resizeFactor = 1;
								try {
									int addF = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("programSnoopyLoaderNetExtFactor"));
									resizeFactor = ((double)addF/(double)100);
									
									if(resizeFactor==0)
										resizeFactor=1;
								} catch (Exception ignored) { }
								
								x *= resizeFactor;
								y *= resizeFactor;
								
								if(logicalELNumber_graphics == 0) { 
									//użyty konstruktor dla place w super-klasie Node utworzył już pierwszy ElementLocation
									metaNode.getElementLocations().set(0, new ElementLocation(sub, new Point(x, y), metaNode));
									subIDs.add(elLocID);
								} else {
									metaNode.getElementLocations().add(new ElementLocation(sub, new Point(x, y), metaNode));
									subIDs.add(elLocID);
								}
							}
						}
						snoopyNodesElLocIDList.add(subIDs);
						snoopyCoarseNodesElLocIDList.add(subIDs);
					}
				} //czytanie właściwości węzła
				
				if(logicalELNumber_graphics != logicalELNumber_names) {
					warnings = true;
					GUIManager.getDefaultGUIManager().log("Warning: names locations number and graphics locations number vary for c-place "
							+metaNode.getName(), "warning", true);
					GUIManager.getDefaultGUIManager().log(" Fix: resetting names locations (offsets) array.", "warning", true);
					
					metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).clear();
					for(int i=0; i<metaNode.getElementLocations().size(); i++) {
						int sub = metaNode.getElementLocations().get(i).getSheetID();
						metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sub, new Point(0, 0), metaNode));
					}
				}
			}
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy coarse places failed in line:", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "error", true);
		}
		
		if(coarsePlacesCounter != coarsePlacesLimit) {
			warnings = true;
			GUIManager.getDefaultGUIManager().log("Warning: c-places read: "+(coarsePlacesCounter+1)+
					", c-places number set in file: "+(coarsePlacesLimit+1), "warning", true);
		}
	}
	
	/**
	 * Metoda czytająca blok danych c-tranzycji.
	 * @param buffer BufferedReader - obiekt czytający
	 * @param line String - ostatnio przeczytana linia
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private void readSnoopyCoarseTransitions(BufferedReader buffer, String line) {
		String backFirstLine = line;
		int coarseTransitionsCounter = -1;
		int coarseTransitionsLimit = 0;
		try {
			//policz teoretyczną liczbę c-tranzycji
			line = line.substring(line.indexOf("count=")+7); //<nodeclass count="44" name="Place">
			line = line.substring(0, line.indexOf("\""));
			coarseTransitionsLimit = Integer.parseInt(line) - 1;
			
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
				int nodeCoarseNumber = (int) getAttributeValue(line, " coarse=\"", -1);
				if(nodeSnoopyID == -1) {
					GUIManager.getDefaultGUIManager().log("Catastrophic error: could not read Snoopy Coarse Transition ID from line: "+backFirstLine, "error", true);
					break;
				} else {
					snoopyNodesIdList.add(nodeSnoopyID);
					snoopyCoarseNodesIdList.add(nodeSnoopyID);
				}
				int subNet = (int) getAttributeValue(line, " net=\"", 0);
				if(subNet > 0)
					subNet--;
				
				MetaNode metaNode = new MetaNode(subNet, IdGenerator.getNextId(), new Point(100,100), MetaType.SUBNETTRANS);
				metaNode.setRepresentedSheetID(nodeCoarseNumber-1);
				nodesList.add(metaNode);
				coarseTransitionsCounter++;
				
				goBoldly = true;
				int logicalELNumber_names = -1;
				int logicalELNumber_graphics = -1;
				boolean firstPass = true;
				boolean readAnything = false;
				while(goBoldly) { //czytanie właściwości c-tranzycji
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //1st

					if(!line.contains("<attribute name=\""))
						line = buffer.readLine();
					
					if(line.contains("</node>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					if(!firstPass) {
						if(!readAnything) {
							while(!(line = buffer.readLine()).contains("</attribute>") ) {

							}
							line = buffer.readLine();
						}
					}
					firstPass = false;
					readAnything = false;
					// Nazwa c-tranzycji
					if(line.contains("<attribute name=\"Name\"")) {
						readAnything = true;
						String name = "cTransition"+coarseTransitionsCounter;
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								name = readStrCDATA(buffer, line, name);
								metaNode.setName(name);
							} 
							
							if(line.contains("<graphic ")) { //lokalizacje nazwy (offsets)
								logicalELNumber_names++;
								int xOff = (int) getAttributeValue(line, " xoff=\"", 0);
								int yOff = (int) getAttributeValue(line, " yoff=\"", 0);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								if(sub != 0)
									sub--;
								
								yOff -= 20; //20 default, czyli 0 w oY w Holmes
								if(yOff < -8)
									yOff = -55; //nad node, uwzględnia różnicę
								
								if(!GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorUseSnoopyOffsets").equals("1")) {
									xOff = 0;
									yOff = 0;
								}
								
								if(logicalELNumber_names == 0) { 
									//użyty konstruktor dla transition w super-klasie Node utworzył już pierwszy ElementLocation (names_off)
									metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).set(0, new ElementLocation(sub, new Point(xOff,yOff), metaNode));
								} else {
									metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
									metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sub, new Point(xOff,yOff), metaNode));
								}
							}
						}
						line = buffer.readLine();
					}

					// Komentarz do c-tranzycji
					if(line.contains("<attribute name=\"Comment\"")) {
						readAnything = true;
						String wittyComment = "";
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								wittyComment = readStrCDATA(buffer, line, "");
							}
						}
						metaNode.setComment(wittyComment);
						line = buffer.readLine();
					}
					
					// XY Locations
					if(line.contains("<graphics count=\"")) {
						readAnything = true;
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
								
								double resizeFactor = 1;
								try {
									int addF = Integer.parseInt(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("programSnoopyLoaderNetExtFactor"));
									resizeFactor = ((double)addF/(double)100);
									
									if(resizeFactor==0)
										resizeFactor=1;
								} catch (Exception ignored) { }
								
								x *= resizeFactor;
								y *= resizeFactor;
								
								if(logicalELNumber_graphics == 0) { 
									//użyty konstruktor dla place w super-klasie Node utworzył już pierwszy ElementLocation
									metaNode.getElementLocations().set(0, new ElementLocation(sub, new Point(x, y), metaNode));
									subIDs.add(elLocID);
								} else {
									metaNode.getElementLocations().add(new ElementLocation(sub, new Point(x, y), metaNode));
									subIDs.add(elLocID);
								}
							}
						}
						snoopyNodesElLocIDList.add(subIDs);
						snoopyCoarseNodesElLocIDList.add(subIDs);
					}
				} //czytanie właściwości węzła
				
				if(logicalELNumber_graphics != logicalELNumber_names) {
					warnings = true;
					GUIManager.getDefaultGUIManager().log("Warning: names locations number and graphics locations number vary for c-transition "
							+metaNode.getName(), "warning", true);
					GUIManager.getDefaultGUIManager().log(" Fix: resetting names locations (offsets) array.", "warning", true);
					
					metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).clear();
					metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).clear();
					for(int i=0; i<metaNode.getElementLocations().size(); i++) {
						int sub = metaNode.getElementLocations().get(i).getSheetID();
						metaNode.getTextsLocations(GUIManager.locationMoveType.NAME).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.ALPHA).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.BETA).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.GAMMA).add(new ElementLocation(sub, new Point(0, 0), metaNode));
						metaNode.getTextsLocations(GUIManager.locationMoveType.TAU).add(new ElementLocation(sub, new Point(0, 0), metaNode));
					}
				}
			}
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy coarse transitions failed in line: ", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "error", true);
		}
		
		if(coarseTransitionsCounter != coarseTransitionsLimit) {
			warnings = true;
			GUIManager.getDefaultGUIManager().log("Warning: c-transitions read: "+(coarseTransitionsCounter+1)+
					", c-transitions number set in file: "+(coarseTransitionsLimit+1), "warning", true);
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
		@SuppressWarnings("unused")
		int arcClasses = Integer.parseInt(txtNumber);
	
		// Edges
		line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypeOfArc.NORMAL);
		}
		
		// Read Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Read Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypeOfArc.READARC);
		}
		
		// Inhibitor Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Inhibitor Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypeOfArc.INHIBITOR);
		}
		
		// Reset Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Reset Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypeOfArc.RESET);
		}
		
		// Equal Edge
		if(!line.contains("</edgeclasses>"))
			line = buffer.readLine();
		while(!line.contains("<edgeclass count=") && !line.contains("</edgeclasses>")) {//przewiń do łuków
			line = buffer.readLine();
		}
		if(!line.contains("<edgeclass count=\"0\"") && line.contains("name=\"Equal Edge\"")) { //są jakieś łuki?
			readEdges(buffer, line, TypeOfArc.EQUAL);
		}
		
	}
	
	/**
	 * Metoda czyta dane łuków sieci.
	 * @param buffer BufferedReader - obiekt czytający
	 * @param line String - ostatnio przeczytana linia
	 * @param arcType TypesOfArcs - typ łuku wykryty w metodzie nadrzędnej
	 */
	@SuppressWarnings("StatementWithEmptyBody")
	private void readEdges(BufferedReader buffer, String line, TypeOfArc arcType) {
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
				boolean firstPass = true;
				boolean readAnything = false;
				while(goBoldly) { //czytanie właściwości miejsca	
					if(line.contains("</edge>")) {
						goBoldly = false;
						continue;
					} //1st
					
					if(!line.contains("<attribute name=\""))
						line = buffer.readLine();
					
					if(line.contains("</edge>")) {
						goBoldly = false;
						continue;
					} //2nd
					
					if(!firstPass) {
						if(!readAnything) {
							while(!(line = buffer.readLine()).contains("</attribute>") ) {

							}
							line = buffer.readLine();
						}
					}
					firstPass = false;
					readAnything = false;
					
					// Waga łuku
					if(line.contains("<attribute name=\"Multiplicity\"")) {
						readAnything = true;
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								multiplicity = (int) readDoubleCDATA(buffer, line, 0);
							} 
						}
						line = buffer.readLine();
					}

					// Komentarz łuku
					if(line.contains("<attribute name=\"Comment\"")) {
						readAnything = true;
						while(!(line = buffer.readLine()).contains("</attribute>")) {
							if(line.contains("<![CDATA[")) {
								comment = readStrCDATA(buffer, line, "");
							}
						}
						line = buffer.readLine();
					}
					
					// XY Locations
					if(line.contains("<graphics count=\"")) {
						readAnything = true;
						ArrayList<Point> newBreakPoints = new ArrayList<Point>();
						Arc nArc = null;
						while(!(line = buffer.readLine()).contains("</graphics>")) {
							if(line.contains("<graphic ")) {
								int sourceID = (int) getAttributeValue(line, " source=\"", -1);
								int targetID = (int) getAttributeValue(line, " target=\"", -1);
								int sub = (int) getAttributeValue(line, " net=\"", 0);
								if(sub != 0)
									sub--;
								
								// Arc source ID analysis:
								boolean createArc = true;
								try {
									ElementLocation sourceEL = null;
									ElementLocation targetEL = null;
									TypeOfArc currentType = arcType; 
									int eLsourceLocation = snoopyNodesElLocIDList.get(sourceLocationInNodes).indexOf(sourceID);
									if(eLsourceLocation == -1) { //szukaj wśród elementów wektora coarse-nodes
										int coarseIndex = -1;
										int coarseSubLocation = -1;
										boolean foundInCoarses = false;
										for(ArrayList<Integer> vector : snoopyCoarseNodesElLocIDList) {
											coarseIndex++;
											if(vector.contains(sourceID)) {
												coarseSubLocation = vector.indexOf(sourceID);
												foundInCoarses = true;
												break;
											}
										}

										boolean snoopyErrorFixed = false;
										if(!foundInCoarses) { //jeśli nie ma w wektorze coarse-nodes, zrob dokladne przeszukanie wektora
											//zwyklych węzłów (czasochłonne)
											int counter = -1;
											int location = -1;
											for(ArrayList<Integer> vector2 : snoopyNodesElLocIDList) {
												counter++;
												if(vector2.contains(sourceID)) {
													location = vector2.indexOf(sourceID);
													snoopyErrorFixed = true;
													break;
												}
											}
											if(snoopyErrorFixed) {
												sourceEL = nodesList.get(counter).getElementLocations().get(location);
												//currentType = TypesOfArcs.META_ARC;
												snoopyErrorFixed = false; //aby nie weszło poniżej
												GUIManager.getDefaultGUIManager().log(" Fixed: wrong data for arc from (SnoopySourceID: "+sourceID+
														") to (SnoopyTargetID: "+targetID+"). Arc fixed.", "error", true);
											} else {
												GUIManager.getDefaultGUIManager().log("Error: cannot create arc from (SnoopySourceID: "+sourceID+
														") to (SnoopyTargetID: "+targetID+")", "error", true);
											}
										}
										
										if(foundInCoarses || snoopyErrorFixed) {
											int coarseID = snoopyNodesIdList.size() - snoopyCoarseNodesIdList.size() + coarseIndex;
											sourceEL = nodesList.get(coarseID).getElementLocations().get(coarseSubLocation);
											currentType = TypeOfArc.META_ARC;
											if(!snoopyErrorFixed) //tylko dla coarse-nodes
												edgesCounter--;
										}
									} else {
										sourceEL = nodesList.get(sourceLocationInNodes).getElementLocations().get(eLsourceLocation);
									}
									
									// Arc target ID analysis:
									int eLtargetLocation = snoopyNodesElLocIDList.get(targetLocationInNodes).indexOf(targetID);
									if(eLtargetLocation == -1) {
										int coarseIndex = -1;
										int coarseSubLocation = -1;
										boolean found = false;
										for(ArrayList<Integer> vector : snoopyCoarseNodesElLocIDList) {
											coarseIndex++;
											if(vector.contains(targetID)) {
												coarseSubLocation = vector.indexOf(targetID);
												found = true;
												break;
											}
										}
										
										boolean snoopyErrorFixed = false;
										if(!found) { //jeśli nie ma w wektorze coarse-nodes, zrob dokladne przeszukanie wektora
											//zwyklych węzłów (czasochłonne)
											int counter = -1;
											int location = -1;
											for(ArrayList<Integer> vector2 : snoopyNodesElLocIDList) {
												counter++;
												if(vector2.contains(targetID)) {
													location = vector2.indexOf(targetID);
													snoopyErrorFixed = true;
													break;
												}
											}
											if(snoopyErrorFixed) {
												sourceEL = nodesList.get(counter).getElementLocations().get(location);
												//currentType = TypesOfArcs.META_ARC;
												snoopyErrorFixed = false; //aby nie weszło poniżej
												GUIManager.getDefaultGUIManager().log(" Fixed: wrong data for arc from (SnoopySourceID: "+sourceID+
														") to (SnoopyTargetID: "+targetID+"). Arc fixed.", "error", true);
											} else {
												GUIManager.getDefaultGUIManager().log("Error: cannot create arc from (SnoopySourceID: "+sourceID+
														") to (SnoopyTargetID: "+targetID+")", "error", true);
											}
										}
										if(found || snoopyErrorFixed) {
											int coarseID = snoopyNodesIdList.size() - snoopyCoarseNodesIdList.size() + coarseIndex;
											targetEL = nodesList.get(coarseID).getElementLocations().get(coarseSubLocation);
											currentType = TypeOfArc.META_ARC;
											if(!snoopyErrorFixed) //tylko dla coarse-nodes
												edgesCounter--;
										}
									} else {
										targetEL = nodesList.get(targetLocationInNodes).getElementLocations().get(eLtargetLocation);
									}
									
									if(createArc) {
										nArc = new Arc(sourceEL, targetEL, comment, multiplicity, currentType);
										arcList.add(nArc);
										edgesCounter++;
									} else {
										GUIManager.getDefaultGUIManager().log("Error: could not create arc from (SnoopySourceID: "+sourceID+
												") to (SnoopyTargetID: "+targetID+").", "error", true);
									}
								} catch (Exception e) {
									boolean foundCoarse = false;
									for(ArrayList<Integer> vector : snoopyCoarseNodesElLocIDList) {
										if(vector.contains(sourceID) || vector.contains(targetID)) {
											foundCoarse = true;
											break;
										}
									}
									if(!foundCoarse)
										GUIManager.getDefaultGUIManager().log("Error: unable to load arc data (SnoopySourceID:"+sourceID+
												", SnoopyTargetID:"+targetID+")", "error", true);
									else {
										GUIManager.getDefaultGUIManager().log("Warning: meta-node to/from coarse graphic elements could not "
												+ "be created. This should not influence invariants-based analysis.", "warning", true);
										warnings = true;
									}
								}
							} else if(line.contains("<point ")) {
								//TODO:
								int posX = (int) getAttributeValue(line, " x=\"", -1);
								int posY = (int) getAttributeValue(line, " y=\"", -1);
								newBreakPoints.add(new Point(posX, posY));
							}
						}
						
						if(newBreakPoints.size() > 2 && nArc != null) {
							newBreakPoints.remove(0);
							newBreakPoints.remove(newBreakPoints.size()-1);
							
							for(Point point : newBreakPoints) {
								if(point.x > 0 && point.y > 0) {
									nArc.addBreakPoint((Point)point.clone());
								}
							}
						}
					}
				} //czytanie właściwości węzła
			}
			
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Reading Snoopy arcs failed in line: ", "error", true);
			GUIManager.getDefaultGUIManager().log(line, "error", true);
		}
		
		if(edgesCounter != edgesLimit) {
			warnings = true;
			GUIManager.getDefaultGUIManager().log("Warning: arcs ("+arcType+") read: "+(edgesCounter+1)+
					", arcs number set in file: "+(edgesLimit+1), "warning", true);
		}
	}

	/**
	 * Metoda wycina wartość liczbową zadanego parametru z linii.
	 * @param line String - linia z pliku
	 * @param signature String - atrybut (format: ' nazwa="'    )
	 * @param defaultVal double - wartość domyślna, jeśli nie da się przeczytać
	 * @return double - wartość odczytana atrybutu
	 */
	private double getAttributeValue(String line, String signature, double defaultVal) {
		double result = defaultVal;
		try {
			int location = line.indexOf(signature);
			String tmp = line.substring(location + signature.length());
			location = tmp.indexOf("\"");
			tmp = tmp.substring(0, location);
			result = Double.parseDouble(tmp);
		} catch (Exception ignored) {
		}
		return result;
	}
	
	/**
	 * Metoda wycina wartość zadanego parametru z linii.
	 * @param line String - linia z pliku
	 * @param signature String - atrybut (format: ' nazwa="'    )
	 * @param defaultVal String - wartość domyślna, jeśli nie da się przeczytać
	 * @return String - wartość odczytana atrybutu
	 */
	private String getAttributeValueStr(String line, String signature, String defaultVal) {
		String result = defaultVal;
		try {
			int location = line.indexOf(signature);
			String tmp = line.substring(location + signature.length());
			location = tmp.indexOf("\"");
			result = tmp.substring(0, location);
		} catch (Exception ignored) {
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
			StringBuilder resultLine = new StringBuilder(line);
			boolean reset = false;
			while(!(line.contains("]]>"))) {
				buffer.mark(1024);
				line = buffer.readLine();
				resultLine.append(line);
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
			resultLine = new StringBuilder(resultLine.substring(location + 7));
			location = resultLine.indexOf("]]>");
			resultLine = new StringBuilder(resultLine.substring(0, location));
			return resultLine.toString();
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
			StringBuilder resultLine = new StringBuilder(line);
			boolean reset = false;
			while(!(line.contains("]]>"))) {
				buffer.mark(1024);
				line = buffer.readLine();
				resultLine.append(line);
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
			resultLine = new StringBuilder(resultLine.substring(location + 7));
			location = resultLine.indexOf("]]>");
			resultLine = new StringBuilder(resultLine.substring(0, location));
			return Double.parseDouble(resultLine.toString());
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
