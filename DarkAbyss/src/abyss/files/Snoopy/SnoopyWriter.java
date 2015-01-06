package abyss.files.Snoopy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Autor włożył ogromny wysiłek, aby ta klasa wraz z pomocniczymi potrafiła zasymulować
 * obłęd twórców programu Snoopy, który objawia się na każdym etapie zapisu danych
 * sieci do pliku. I nawet nie chodzi o ręczne symulowanie parsera, który jest tam
 * użyty. Ten parser działa błędnie. Np. komentarze dla tranzycji są przesuniętę względem
 * osi oX, a powinny oY jak dla miejsc. Poza tym parser pluje danymi jak karabin maszynowy,
 * z czego 60% tych danych jest redundantnych, a kilka zupełnie ignorowanych przez Snoopiego
 * wczytującego tenże plik (np. punkty startu i końca dla łuków).
 * 
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

	/**
	 * Konstruktor obiektu klasy SnoopyWriter uzyskujący dostęp do zasobów sieci.
	 */
	public SnoopyWriter() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		
		snoopyPlaces = new ArrayList<SnoopyPlace>();
		snoopyTransitions = new ArrayList<SnoopyTransition>();
	}
	
	/**
	 * Metoda realizująca zapis do pliku SPPED. Działa - 03.01.2015. I na tym
	 * się zatrzymajmy w opisach.
	 */
	public void writeSPPED(String filePath) {
		int startNodeId = 226; // bo tak
		int currentActiveID = startNodeId;
		int arcsNumber = 0;
		try {
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
			
			//NAGŁÓWEK:
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
					arcsNumber += el.getOutArcs().size(); //pobież wszystkie wychodzące
				}
				
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				
				if(sPlace.portal == true) { //jeśli właśnie dodane było portalem
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
					arcsNumber += el.getOutArcs().size(); //pobież wszystkie wychodzące
				}
				
				
				currentActiveID = sTransition.writeTransitionInfoToFile(bw, currentActiveID, globalTransId);
				currentActiveID ++;
				globalTransId++;
			}
			write(bw, "    </nodeclass>");
			
			//TEGO NA RAZIE NIE RUSZAMY (DA BÓG: NIGDY)
			write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			write(bw, "  </nodeclasses>");
			
			//ŁUKI:
			write(bw, "  <edgeclasses count=\"1\">");
			write(bw, "    <edgeclass count=\"" + arcsNumber + "\" name=\"Edge\">");
			addArcInfo(bw, currentActiveID);
			write(bw, "    </edgeclass>");
			write(bw, "  </edgeclasses>");
			
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();
			
			GUIManager.getDefaultGUIManager().log("Net has been exported as SPPED file: "+filePath, "text", true);
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Critical error while exporting net to the SPPED file: "+filePath, "error", true);		
		}
	}
	
	/**
	 * Stopień odjechania poniższej metody przewyższa normy niczym Czarnobyl w kwestii promieniowania.
	 * A skoro już o tym mowa...
	 * -Вот это от усталости, это от нервного напряжения, а это от депрессии...
	 * -Спасибо, доктор, спасибо... А у вас, кроме водки, ничего нет?
	 * 
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param currentActiveID int - od tego ID zaczynamy dodawać łuki
	 */
	private void addArcInfo(BufferedWriter bw, int currentActiveID) {
		int howMany = 0;
		int nextID = currentActiveID;
		//int iteracja = 0;
		int xOff = 0;
		//int yOff = 0;
		for(Place p : places) { //najpierw wyjściowe z miejsc
			//ArrayList<ElementLocation> clones = p.getElementLocations();
			int location = -1;
			for(ElementLocation el : p.getElementLocations()) { // dla wszystkich jego lokalizacji
				location++; // która faktycznie to jest, jeśli przetwarzamy portal
				
				ArrayList<Arc> outArcs = el.getOutArcs(); //pobież listę łuków wyjściowych (portalu)
				
				//kolekcjonowanie danych:
				for(Arc a : outArcs) { //dla każdego łuku
					int weight = a.getWeight(); //waga łuku
					String comment = a.getComment();
					int grParent = currentActiveID + 5;
					
					Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss
					Node sourceAbyss = a.getStartNode(); //stąd wychodzi
					//przy czym należy okreslić, do której lokalizacji
					
					//sourceAbyss == p
					
					int addToSPPEDAsSource = snoopyPlacesID.lastIndexOf(sourceAbyss.getID()); //który to był
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
					//teraz należy określi do której lokalizacji portalu trafia łuk
					
					ElementLocation destinationLoc = a.getEndLocation();
					int counter = -1;
					for(ElementLocation whichOne : targetAbyss.getElementLocations()) {
						counter++;
						//szukamy w węźlie docelowym, która to w kolejności lokalizacja jeśli to portal
						//jeśli to: to i tak skończy się na 1 iteracji
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
					
					//tutaj wchodzą główne numery główne:
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
					
					//TUTAJ WCHODZĄ REALNE X,Y I ID PORTALI:
					write(bw, "          <graphic id=\""+grParent+"\" net=\"1\""
							+ " source=\""+realSourceID+"\""
							+ " target=\""+realTargetID+"\" state=\"1\" show=\"1\""
							+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");
					
					//teoretycznie poniższe powinny być wyliczone z układu równań do rozwiązywania
					//problemu współrzędnych przecięcia prostej z okręgiem (lub z rogiem kwadratu - tr.)
					//na szczęście można wpisać współrzędne docelowe węzłów, Snoopy jest tu wyrozumiały
					write(bw, "            <points count=\"2\">"); //bez łamańców
					write(bw, "              <point x=\""+realSourceX+".00\" y=\""+realSourceY+".00\"/>");
					write(bw, "              <point x=\""+realTargetX+".00\" y=\""+realTargetY+".00\"/>");
					write(bw, "            </points>");
					write(bw, "          </graphic>");
					write(bw, "        </graphics>");
					write(bw, "      </edge>");
					
					howMany++;
				}
				
			} //dla wszystkich lokalizacji
			//iteracja++;
		} //dla wszystkich miejsc
		
		//teraz wszystkie wychodzące z tranzycji:
		for(Transition t : transitions) { //najpierw wyjściowe z tranzycji
			int location = -1;
			for(ElementLocation el : t.getElementLocations()) { // dla wszystkich jego lokalizacji
				location++; // która faktycznie to jest, jeśli trafiliśmy w portal
				ArrayList<Arc> outArcs = el.getOutArcs(); //pobież listę łuków wyjściowych (portalu)
				
				//kolekcjonowanie danych:
				for(Arc a : outArcs) { //dla każdego łuku
					int weight = a.getWeight(); //waga łuku
					String comment = a.getComment();
					int grParent = currentActiveID + 5;
					
					Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss (w miejsce)
					Node sourceAbyss = a.getStartNode(); //stąd wychodzi (tranzycja)
					//przy czym należy okreslić, do której lokalizacji
					
					//sourceAbyss == t
					
					int addToSPPEDAsSource = snoopyTransitionsID.lastIndexOf(sourceAbyss.getID()); //który to był
					if(addToSPPEDAsSource == -1) {
						@SuppressWarnings("unused")
						int WTF= 1; //!!! IMPOSSIBRU!!!!
						return;
					}
					SnoopyTransition source = snoopyTransitions.get(addToSPPEDAsSource);
					int nodeSourceID = source.nodeID;
					int realSourceID = source.grParents.get(location); //k
					int realSourceX = source.grParentsLocation.get(location).x;
					int realSourceY = source.grParentsLocation.get(location).y;
					
					
					//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
					int addToSPPEDAsTarget = snoopyPlacesID.lastIndexOf(targetAbyss.getID());
					SnoopyPlace target = snoopyPlaces.get(addToSPPEDAsTarget);
					//teraz należy określi do której lokalizacji portalu trafia łuk
					
					ElementLocation destinationLoc = a.getEndLocation();
					int counter = -1;
					for(ElementLocation whichOne : targetAbyss.getElementLocations()) {
						counter++;
						//szukamy w węźlie docelowym, która to w kolejności lokalizacja jeśli to portal
						//jeśli to: to i tak skończy się na 1 iteracji
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
					
					//tutaj wchodzą główne numery:
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
					
					//TUTAJ WCHODZĄ REALNE X,Y I ID PORTALI:
					write(bw, "          <graphic id=\""+grParent+"\" net=\"1\""
							+ " source=\""+realSourceID+"\""
							+ " target=\""+realTargetID+"\" state=\"1\" show=\"1\""
							+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");
					
					//teoretycznie poniższe powinny być wyliczone z układu równań do rozwiązywania
					//problemu współrzędnych przecięcia prostej z okręgiem (lub z rogiem kwadratu - tr.)
					//na szczęście można wpisać współrzędne docelowe węzłów, Snoopy jest tu wyrozumiały
					write(bw, "            <points count=\"2\">"); //bez łamańców
					write(bw, "              <point x=\""+realSourceX+".00\" y=\""+realSourceY+".00\"/>");
					write(bw, "              <point x=\""+realTargetX+".00\" y=\""+realTargetY+".00\"/>");
					write(bw, "            </points>");
					write(bw, "          </graphic>");
					write(bw, "        </graphics>");
					write(bw, "      </edge>");

					howMany++;
				}
				
			} //dla wszystkich lokalizacji
			//iteracja++;
		} //dla wszystkich tranzycji
		
		if(howMany != GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs().size()) {
			GUIManager.getDefaultGUIManager().log("Arcs saved do not match size of Arcs internal set."
					+ " Meaning: Snoopy SPPED write error. File may be there, but the saved net may be"
					+ " corrupt.", "error", true);
		}
	}

	private void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception e) {
			return;
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
			GUIManager.getDefaultGUIManager().log("File access error", "error", true);
		}
	}
}
