package abyss.files.Snoopy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Autor w³o¿y³ ogromny wysi³ek, aby ta klasa wraz z pomocniczymi potrafi³a zasymulowaæ
 * ob³êd twórców programu Snoopy, który objawia siê na ka¿dym etapie zapisu danych
 * sieci do pliku.
 * @author MR
 *
 */
public class SnoopyWriter {
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	
	private ArrayList<SnoopyPlace> snoopyPlaces = new ArrayList<SnoopyPlace>();
	private ArrayList<Integer> snoopyPlacesID = new ArrayList<Integer>();
	private ArrayList<SnoopyTransition> snoopyTransitions = new ArrayList<SnoopyTransition>();
	private ArrayList<Integer> snoopyTransitionsID = new ArrayList<Integer>();
	
	private String dateAndTime = "2015-01-02 10:44:56";

	public SnoopyWriter() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		snoopyPlaces = new ArrayList<SnoopyPlace>();
		snoopyTransitions = new ArrayList<SnoopyTransition>();
	}
	
	public void writeSPPED() {
		int startNodeId = 226; // bo tak
		int currentActiveID = startNodeId;
		int arcsNumber = 0;
		try {
			String tPath = GUIManager.getDefaultGUIManager().tmpPath;
			BufferedWriter bw = new BufferedWriter(new FileWriter(tPath+"argh.spped"));
			//NAG£ÓWEK:
			write(bw, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			write(bw, "<?xml-stylesheet type=\"text/xsl\" href=\"/xsl/spped2svg.xsl\"?>");
			write(bw, "<Snoopy version=\"2\" revision=\"1.13\">");
			
			write(bw, "  <netclass name=\"Petri Net\"/>");
			write(bw, "  <nodeclasses count=\"4\">"); //zawsze 4
			
			//MIEJSCA:
			int placesNumber = places.size();
			write(bw, "    <nodeclass count=\""+placesNumber+"\" name=\"Place\">");
			int globalPlaceId = 0;
			for(Place p : places) {
				SnoopyPlace sPlace = new SnoopyPlace(p);
				snoopyPlaces.add(sPlace);
				snoopyPlacesID.add(p.getID());
				
				ArrayList<ElementLocation> clones = p.getElementLocations();
				for(ElementLocation el : clones) {
					arcsNumber += el.getOutArcs().size(); //pobie¿ wszystkie wychodz¹ce
				}
				
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				
				if(sPlace.portal == true) { //jeœli w³aœnie dodane by³o portalem
					currentActiveID += 13; //bo tak, pytajcie w Brandenburgu 'a a a czymuuu?'
				} else {
					currentActiveID ++;
				}
				globalPlaceId++;
				
			}
			write(bw, "    </nodeclass>");
			
			// TRANZYCJE:
			int transNumber = transitions.size();
			write(bw, "    <nodeclass count=\""+transNumber+"\" name=\"Transition\">");
			int globalTransId = 0;
			for(Transition t : transitions) {
				SnoopyTransition sTransition = new SnoopyTransition(t);
				snoopyTransitions.add(sTransition);
				snoopyTransitionsID.add(t.getID());
				
				
				ArrayList<ElementLocation> clones = t.getElementLocations();
				for(ElementLocation el : clones) {
					arcsNumber += el.getOutArcs().size(); //pobie¿ wszystkie wychodz¹ce
				}
				
				
				currentActiveID = sTransition.writeTransitionInfoToFile(bw, currentActiveID, globalTransId);
				currentActiveID ++;
				globalTransId++;
			}
			write(bw, "    </nodeclass>");
			
			
			
			write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			write(bw, "  </nodeclasses>");
			
			//£UKI:
			write(bw, "  <edgeclasses count=\"1\">");
			write(bw, "    <edgeclass count=\"" + arcsNumber + "\" name=\"Edge\">");
			
			addArcInfo(bw, currentActiveID);
			
			write(bw, "    </edgeclass>");
			write(bw, "  </edgeclasses>");
			
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();
		} catch (Exception e) {
			
		}
	}
	
	/**
	 * Stopieñ odjechania poni¿szej metody przewy¿sza normy niczym Czarnobyl w kwestii promieniowania.
	 * @param bw BufferedWriter - obiekt zapisuj¹cy
	 * @param currentActiveID int - od tego ID zaczynamy dodawaæ ³uki
	 */
	private void addArcInfo(BufferedWriter bw, int currentActiveID) {
		try {
		int nextID = currentActiveID;
		int iteracja = 0;
		int xOff = 0;
		int yOff = 0;
		for(Place p : places) { //najpierw wyjœciowe z miejsc
			//ArrayList<ElementLocation> clones = p.getElementLocations();
			int location = -1;
			for(ElementLocation el : p.getElementLocations()) { // dla wszystkich jego lokalizacji
				location++; // która faktycznie, jeœli to portal
				
				ArrayList<Arc> outArcs = el.getOutArcs(); //pobie¿ listê ³uków wyjœciowych (portalu)
				
				//kolekcjonowanie danych:
				for(Arc a : outArcs) { //dla ka¿dego ³uku
					int weight = a.getWeight(); //waga ³uku
					String comment = a.getComment();
					int grParent = currentActiveID + 5;
					
					Node targetAbyss = a.getEndNode(); //tutaj trafia ³uk w Abyss
					Node sourceAbyss = a.getStartNode(); //st¹d wychodzi
					//przy czym nale¿y okresliæ, do której lokalizacji
					
					//sourceAbyss == p
					
					int addToSPPEDAsSource = snoopyPlacesID.lastIndexOf(sourceAbyss.getID()); //który to by³
					if(addToSPPEDAsSource == -1) {
						@SuppressWarnings("unused")
						int WTF= 1; //!!! IMPOSSIBRU!!!!
						return;
					}
					SnoopyPlace source = snoopyPlaces.get(addToSPPEDAsSource);
					int nodeSourceID = source.nodeID;
					int realSourceID = source.grParents.get(location); //k
					int realSourceX = source.grParentsLocation.get(location).x;
					int realSourceY = source.grParentsLocation.get(location).y;
					
					
					//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
					int addToSPPEDAsTarget = snoopyTransitionsID.lastIndexOf(targetAbyss.getID());
					SnoopyTransition target = snoopyTransitions.get(addToSPPEDAsTarget);
					//teraz nale¿y okreœli do której lokalizacji portalu trafia ³uk
					
					ElementLocation destinationLoc = a.getEndLocation();
					int counter = -1;
					for(ElementLocation whichOne : targetAbyss.getElementLocations()) {
						counter++;
						//szukamy w wêŸlie docelowym, która to w kolejnoœci lokalizacja jeœli to portal
						//jeœli to: to i tak skoñczy siê na 1 iteracji
						if(whichOne.equals(destinationLoc)) {
							break; //w counter mamy wtedy nr
						}
					}
					int nodeTargetID = target.nodeID;
					int realTargetID = target.grParents.get(counter); 
					int realTargetX = target.grParentsLocation.get(counter).x;
					int realTargetY = target.grParentsLocation.get(counter).y;
					
					int halfX = (realTargetX + realSourceX) / 2;
					int halfY = (realTargetY + realSourceY) / 2;
					
					//tutaj wchodz¹ g³ówne numery miejsc:
					write(bw, "      <edge source=\""+nodeSourceID+"\""
							+ " target=\""+nodeTargetID+"\" id=\""+nextID+"\" net=\"1\">");
					nextID++; //444
					write(bw, "        <attribute name=\"Multiplicity\" id=\""+nextID+"\" net=\"1\">");
					nextID++; //445
					write(bw, "          <![CDATA["+weight+"]]>");
					write(bw, "          <graphics count=\"1\">");
					xOff = 20;
					write(bw, "            <graphic xoff=\""+xOff+".00\""
							+ " x=\""+(halfX+xOff)+".00\""
							+ " y=\""+halfY+".00\" id=\""+nextID+"\" net=\"1\""
							+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
							+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
					nextID++; //446
					write(bw, "          </graphics>");
					write(bw, "        </attribute>");
					write(bw, "        <attribute name=\"Comment\" id=\""+nextID+"\" net=\"1\">");
					nextID++; //447
					write(bw, "          <![CDATA["+comment+"]]>");
					write(bw, "          <graphics count=\"1\">");
					xOff = 40;
					write(bw, "            <graphic xoff=\""+xOff+".00\""
							+ " x=\""+(halfX+xOff)+".00\""
							+ " y=\""+halfY+".00\" id=\""+nextID+"\" net=\"1\""
							+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
							+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
					nextID++; //448 == grParent
					write(bw, "          </graphics>");
					write(bw, "        </attribute>");
					write(bw, "        <graphics count=\"1\">");
					
					//TUTAJ WCHODZ¥ REALNE X,Y I ID PORTALI:
					write(bw, "          <graphic id=\""+grParent+"\" net=\"1\""
							+ " source=\""+realSourceID+"\""
							+ " target=\""+realTargetID+"\" state=\"1\" show=\"1\""
							+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");
					
					//teoretycznie poni¿sze powinny byæ wyliczone z uk³adu równañ do rozwi¹zywania
					//problemu wspó³rzêdnych przeciêcia prostej z okrêgiem (lub z rogiem kwadratu - tr.)
					//na szczêœcie mo¿na wpisaæ wspó³rzêdne docelowe wêz³ów, Snoopy jest tu wyrozumia³y
					write(bw, "            <points count=\"2\">"); //bez ³amañców
					write(bw, "              <point x=\""+realSourceX+".00\" y=\""+realSourceY+".00\"/>");
					write(bw, "              <point x=\""+realTargetX+".00\" y=\""+realTargetY+".00\"/>");
					write(bw, "            </points>");
					write(bw, "          </graphic>");
					write(bw, "        </graphics>");
					write(bw, "      </edge>");
					//sourceX = source.
				}
				
			} //dla wszystkich lokalizacji
			iteracja++;
		} //dla wszystkich miejsc
		} catch (Exception e) {
			int wtf = 1;
			wtf = 2;
		}
	}

	private void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception e) {
			
		}
	}
	
	private void writeEnding(BufferedWriter bw) {
		try {
			write(bw, "  <metadataclasses count=\"3\">");
			write(bw, "    <metadataclass count=\"1\" name=\"General\">");
			write(bw, "      <metadata id=\"212\" net=\"1\">");
			write(bw, "        <attribute name=\"Name\" id=\"213\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"3.00\" x=\"20.00\" y=\"20.00\" id=\"214\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Created\" id=\"215\" net=\"1\">");
			write(bw, "          <![CDATA["+dateAndTime+"]]>"); //ZMIENNA
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"20.00\" x=\"42.00\" y=\"40.00\" id=\"216\" net=\"1\" show=\"0\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Authors\" id=\"217\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"40.00\" x=\"42.00\" y=\"60.00\" id=\"218\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Keywords\" id=\"219\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"40.00\" yoff=\"25.00\" x=\"57.00\" y=\"45.00\" id=\"220\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Description\" id=\"221\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"40.00\" x=\"42.00\" y=\"60.00\" id=\"222\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"References\" id=\"223\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"40.00\" x=\"42.00\" y=\"60.00\" id=\"224\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <graphics count=\"1\">");
			write(bw, "          <graphic x=\"17.00\" y=\"20.00\" id=\"225\" net=\"1\" show=\"1\" w=\"15.00\" h=\"24.00\" state=\"1\" pen=\"255,255,255\" brush=\"255,255,255\"/>");
			write(bw, "        </graphics>");
			write(bw, "      </metadata>");
			write(bw, "    </metadataclass>");
			write(bw, "    <metadataclass count=\"0\" name=\"Comment\"/>");
			write(bw, "    <metadataclass count=\"0\" name=\"Constant Class\"/>");
			write(bw, "  </metadataclasses>");
		} catch (Exception e) {
			
		}
	}
}
