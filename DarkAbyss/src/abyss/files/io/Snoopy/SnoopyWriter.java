package abyss.files.io.Snoopy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.MetaNode.MetaType;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.elements.Arc.TypesOfArcs;
import abyss.varia.Check;

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
	private ArrayList<MetaNode> metanodes = new ArrayList<MetaNode>();
	private ArrayList<Arc> arcs;
	private ArrayList<MetaNode> coarsePlaces = new ArrayList<MetaNode>();
	private ArrayList<MetaNode> coarseTransitions = new ArrayList<MetaNode>();
	
	private ArrayList<SnoopyWriterPlace> snoopyWriterPlaces = new ArrayList<SnoopyWriterPlace>();
	
	private ArrayList<Integer> abyssPlacesID = new ArrayList<Integer>();
	private ArrayList<SnoopyWriterTransition> snoopyWriterTransitions = new ArrayList<SnoopyWriterTransition>();
	private ArrayList<Integer> abyssTransitionsID = new ArrayList<Integer>();
	private ArrayList<SnoopyWriterCoarse> snoopyWriterCoarsePlaces = new ArrayList<SnoopyWriterCoarse>();
	private ArrayList<Integer> abyssCoarsePlacesID = new ArrayList<Integer>();
	private ArrayList<SnoopyWriterCoarse> snoopyWriterCoarseTransitions = new ArrayList<SnoopyWriterCoarse>();
	private ArrayList<Integer> abyssCoarseTransitionsID = new ArrayList<Integer>();
	
	
	private String dateAndTime = "2015-01-02 10:44:56";

	/**
	 * Konstruktor obiektu klasy SnoopyWriter uzyskujący dostęp do zasobów sieci.
	 */
	public SnoopyWriter() {
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		metanodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMetaNodes();
		for(MetaNode meta : metanodes) {
			if(meta.getMetaType() == MetaType.SUBNETPLACE)
				coarsePlaces.add(meta);
			if(meta.getMetaType() == MetaType.SUBNETTRANS)
				coarseTransitions.add(meta);
		}
		
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs(); 

		snoopyWriterPlaces = new ArrayList<SnoopyWriterPlace>();
		snoopyWriterTransitions = new ArrayList<SnoopyWriterTransition>();
		snoopyWriterCoarsePlaces = new ArrayList<SnoopyWriterCoarse>();
		snoopyWriterCoarseTransitions = new ArrayList<SnoopyWriterCoarse>();
	}
	
	/**
	 * Metoda realizująca zapis do pliku SPPED. Działa - 03.01.2015. I na tym
	 * się zatrzymajmy w opisach.
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean writeSPPED(String filePath) {
		boolean status = GUIManager.getDefaultGUIManager().netsHQ.checkSnoopyCompatibility();
		if(!status) {
			//return false;
		}
		
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
				SnoopyWriterPlace sPlace = new SnoopyWriterPlace(p);
				snoopyWriterPlaces.add(sPlace);
				abyssPlacesID.add(p.getID());
				
				ArrayList<ElementLocation> clones = p.getElementLocations();
				for(ElementLocation el : clones) {
					arcsNumber += el.getOutArcs().size(); //pobież wszystkie wychodzące
				}
				
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				
				if(sPlace.portal == true) { //jeśli właśnie dodane było portalem
					currentActiveID += 13; //bo tak, pytajcie w Brandenburgu 'a czymuuu?'
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
				SnoopyWriterTransition sTransition = new SnoopyWriterTransition(t);
				snoopyWriterTransitions.add(sTransition);
				abyssTransitionsID.add(t.getID());
				
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
			//21-07-2015 you wish...
			boolean weAreInDeepShit = false;
			if(coarsePlaces.size() == 0) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			} else {
				int coarsePnumber = coarsePlaces.size();
				write(bw, "    <nodeclass count=\""+coarsePnumber+"\" name=\"Coarse Place\">");
				int globalCoarsePlaceId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarsePlaces) {
					SnoopyWriterCoarse sCoarseP = new SnoopyWriterCoarse(m);
					snoopyWriterCoarsePlaces.add(sCoarseP);
					abyssCoarsePlacesID.add(m.getID());
					
					ArrayList<ElementLocation> clones = m.getElementLocations();
					for(ElementLocation el : clones) {
						arcsNumber += el.accessMetaInArcs().size();
						arcsNumber += el.accessMetaOutArcs().size();
					}
					
					currentActiveID = sCoarseP.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarsePlaceId);
					currentActiveID ++;
					globalCoarsePlaceId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			if(coarseTransitions.size() == 0) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			} else {
				int coarsePnumber = coarsePlaces.size();
				write(bw, "    <nodeclass count=\""+coarsePnumber+"\" name=\"Coarse Place\">");
				int globalCoarsePlaceId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarsePlaces) {
					SnoopyWriterCoarse sCoarseP = new SnoopyWriterCoarse(m);
					snoopyWriterCoarseTransitions.add(sCoarseP);
					abyssCoarsePlacesID.add(m.getID());
					
					ArrayList<ElementLocation> clones = m.getElementLocations();
					for(ElementLocation el : clones) {
						arcsNumber += el.accessMetaInArcs().size();
						arcsNumber += el.accessMetaOutArcs().size();
					}
					
					currentActiveID = sCoarseP.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarsePlaceId);
					currentActiveID ++;
					globalCoarsePlaceId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			write(bw, "  </nodeclasses>");
			
			//ŁUKI:
			write(bw, "  <edgeclasses count=\"1\">");
			write(bw, "    <edgeclass count=\"" + arcsNumber + "\" name=\"Edge\">");
			if(weAreInDeepShit)
				addArcsAndCoarseToFile(bw, currentActiveID);
			else
				addArcsToFile(bw, currentActiveID);
			write(bw, "    </edgeclass>");
			write(bw, "  </edgeclasses>");
			
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();
			
			GUIManager.getDefaultGUIManager().log("Net has been exported as SPPED file: "+filePath, "text", true);
			GUIManager.getDefaultGUIManager().markNetSaved();
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Critical error while exporting net to the SPPED file: "+filePath, "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda realizująca zapis do pliku SPPED. Działa - 08.04.2015. I na tym zakończmy jej opis.
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean writeSPEPT(String filePath) {
		int startNodeId = 226; // bo tak
		int currentActiveID = startNodeId;
		//int arcsNumber = 0;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
			
			//NAGŁÓWEK:
			write(bw, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			write(bw, "<?xml-stylesheet type=\"text/xsl\" href=\"/xsl/spped2svg.xsl\"?>");
			write(bw, "<Snoopy version=\"2\" revision=\"1.13\">");
			
			write(bw, "  <netclass name=\"Extended Petri Net\"/>");
			write(bw, "  <nodeclasses count=\"4\">"); //zawsze 4
			
			//MIEJSCA:
			int placesNumber = places.size();
			write(bw, "    <nodeclass count=\""+placesNumber+"\" name=\"Place\">");
			int globalPlaceId = 0;
			for(Place p : places) {
				SnoopyWriterPlace sPlace = new SnoopyWriterPlace(p);
				snoopyWriterPlaces.add(sPlace);
				abyssPlacesID.add(p.getID());
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				if(sPlace.portal == true) { //jeśli właśnie dodane było portalem
					currentActiveID += 13; //bo tak, 13, pytajcie w Brandenburgu 'a czymuuu?' Nie ja pisałem Snoopiego.
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
				SnoopyWriterTransition sTransition = new SnoopyWriterTransition(t);
				snoopyWriterTransitions.add(sTransition);
				abyssTransitionsID.add(t.getID());
				currentActiveID = sTransition.writeTransitionInfoToFile(bw, currentActiveID, globalTransId);
				currentActiveID ++;
				globalTransId++;
			}
			write(bw, "    </nodeclass>");
			
			//TEGO NA RAZIE NIE RUSZAMY (DA BÓG: NIGDY)
			boolean weAreInDeepShit = false;
			if(coarsePlaces.size() == 0) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			} else {
				int coarsePnumber = coarsePlaces.size();
				write(bw, "    <nodeclass count=\""+coarsePnumber+"\" name=\"Coarse Place\">");
				int globalCoarsePlaceId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarsePlaces) {
					SnoopyWriterCoarse sCoarseP = new SnoopyWriterCoarse(m);
					snoopyWriterCoarsePlaces.add(sCoarseP);
					abyssCoarsePlacesID.add(m.getID());		
					currentActiveID = sCoarseP.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarsePlaceId);
					currentActiveID ++;
					globalCoarsePlaceId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			if(coarseTransitions.size() == 0) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			} else {
				int coarseTnumber = coarseTransitions.size();
				write(bw, "    <nodeclass count=\""+coarseTnumber+"\" name=\"Coarse Transition\">");
				int globalCoarseTransitionId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarseTransitions) {
					SnoopyWriterCoarse sCoarseT = new SnoopyWriterCoarse(m);
					snoopyWriterCoarseTransitions.add(sCoarseT);
					abyssCoarseTransitionsID.add(m.getID());
					currentActiveID = sCoarseT.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarseTransitionId);
					currentActiveID ++;
					globalCoarseTransitionId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			write(bw, "  </nodeclasses>");
			
			//ŁUKI:
			write(bw, "  <edgeclasses count=\"5\">");
			
			ArrayList<Integer> arcClasses = Check.getArcClassCount();
			if(arcClasses.get(0) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Edge\">");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(0) + "\" name=\"Edge\">");
				if(weAreInDeepShit)
					currentActiveID = addArcsAndCoarsesInfoExtended(bw, currentActiveID, TypesOfArcs.NORMAL, arcClasses.get(0));
				else
					currentActiveID = addArcsInfoExtended(bw, currentActiveID, TypesOfArcs.NORMAL, arcClasses.get(0));
				write(bw, "    </edgeclass>");
			}
		
			if(arcClasses.get(1) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Read Edge\">");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(1) + "\" name=\"Read Edge\">");
				if(weAreInDeepShit)
					currentActiveID = addArcsAndCoarsesInfoExtended(bw, currentActiveID, TypesOfArcs.READARC, arcClasses.get(0));
				else
					currentActiveID = addArcsInfoExtended(bw, currentActiveID, TypesOfArcs.READARC, arcClasses.get(1));
				write(bw, "    </edgeclass>");
			}
			
			if(arcClasses.get(2) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Inhibitor Edge\">");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(2) + "\" name=\"Inhibitor Edge\">");
				if(weAreInDeepShit)
					currentActiveID = addArcsAndCoarsesInfoExtended(bw, currentActiveID, TypesOfArcs.INHIBITOR, arcClasses.get(0));
				else
					currentActiveID = addArcsInfoExtended(bw, currentActiveID, TypesOfArcs.INHIBITOR, arcClasses.get(2));
				write(bw, "    </edgeclass>");
			}
			
			if(arcClasses.get(3) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Reset Edge\">");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(3) + "\" name=\"Reset Edge\">");
				if(weAreInDeepShit)
					currentActiveID = addArcsAndCoarsesInfoExtended(bw, currentActiveID, TypesOfArcs.RESET, arcClasses.get(0));
				else
					currentActiveID = addArcsInfoExtended(bw, currentActiveID, TypesOfArcs.RESET, arcClasses.get(3));
				write(bw, "    </edgeclass>");
			}
			
			if(arcClasses.get(4) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Equal Edge\">");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(4) + "\" name=\"Equal Edge\">");
				if(weAreInDeepShit)
					currentActiveID = addArcsAndCoarsesInfoExtended(bw, currentActiveID, TypesOfArcs.EQUAL, arcClasses.get(0));
				else
					currentActiveID = addArcsInfoExtended(bw, currentActiveID, TypesOfArcs.EQUAL, arcClasses.get(4));
				write(bw, "    </edgeclass>");
			}
			
			
			write(bw, "  </edgeclasses>");
			
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();
			
			GUIManager.getDefaultGUIManager().log("Net has been exported as SPPED file: "+filePath, "text", true);
			GUIManager.getDefaultGUIManager().markNetSaved();
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Critical error while exporting net to the SPPED file: "+filePath, "error", true);
			return false;
		}
	}
	
	/**
	 * Stopień odjechania poniższej metody przewyższa normy niczym Czarnobyl w kwestii promieniowania.
	 * A skoro już o tym mowa...<br>
	 * -Вот это от усталости, это от нервного напряжения, а это от депрессии...
	 * -Спасибо, доктор, спасибо... А у вас, кроме водки, ничего нет?
	 * 
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param currentActiveID int - od tego ID zaczynamy dodawać łuki
	 */
	private void addArcsToFile(BufferedWriter bw, int currentActiveID) {
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
					try {
						int weight = a.getWeight(); //waga łuku
						String comment = a.getComment();
						int grParent = currentActiveID + 5;
						
						Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss
						Node sourceAbyss = a.getStartNode(); //stąd wychodzi
						//przy czym należy okreslić, do której lokalizacji
						
						//sourceAbyss == p
						
						int addToSPPEDAsSource = abyssPlacesID.lastIndexOf(sourceAbyss.getID()); //który to był
						if(addToSPPEDAsSource == -1) {
							@SuppressWarnings("unused")
							int WTF= 1; //!!! IMPOSSIBRU!!!!
							return;
						}
						SnoopyWriterPlace source = snoopyWriterPlaces.get(addToSPPEDAsSource);
						int nodeSourceID = source.snoopyStartingID;
						int realSourceID = source.grParents.get(location); //k
						int realSourceX = source.grParentsLocation.get(location).x;
						int realSourceY = source.grParentsLocation.get(location).y;
						
						
						//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
						int addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(targetAbyss.getID());
						SnoopyWriterTransition target = snoopyWriterTransitions.get(addToSPPEDAsTarget);
						//teraz należy określić do której lokalizacji portalu trafia łuk
						
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
						int nodeTargetID = target.snoopyStartingID;
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
					} catch (Exception e) {
						GUIManager.getDefaultGUIManager().log("Unable to save arc from "+a.getStartNode().getName()+" to "
								+ a.getEndNode().getName(), "error", true);
					}
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
					try {
						int weight = a.getWeight(); //waga łuku
						String comment = a.getComment();
						int grParent = currentActiveID + 5;
						
						Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss (w miejsce)
						Node sourceAbyss = a.getStartNode(); //stąd wychodzi (tranzycja)
						//przy czym należy okreslić, do której lokalizacji
						
						//sourceAbyss == t
						
						int addToSPPEDAsSource = abyssTransitionsID.lastIndexOf(sourceAbyss.getID()); //który to był
						if(addToSPPEDAsSource == -1) {
							@SuppressWarnings("unused")
							int WTF= 1; //!!! IMPOSSIBRU!!!!
							return;
						}
						SnoopyWriterTransition source = snoopyWriterTransitions.get(addToSPPEDAsSource);
						int nodeSourceID = source.snoopyStartingID;
						int realSourceID = source.grParents.get(location); //k
						int realSourceX = source.grParentsLocation.get(location).x;
						int realSourceY = source.grParentsLocation.get(location).y;
						
						
						//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
						int addToSPPEDAsTarget = abyssPlacesID.lastIndexOf(targetAbyss.getID());
						SnoopyWriterPlace target = snoopyWriterPlaces.get(addToSPPEDAsTarget);
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
						int nodeTargetID = target.snoopyStartingID;
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
					} catch (Exception e) {
						GUIManager.getDefaultGUIManager().log("Unable to save arc from "+a.getStartNode().getName()+" to "
								+ a.getEndNode().getName(), "error", true);
					}
				}
				
			} //dla wszystkich lokalizacji
			//iteracja++;
		} //dla wszystkich tranzycji
		int arcNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs().size();
		if(howMany != arcNumber) {
			GUIManager.getDefaultGUIManager().log("Arcs saved do not match size of Arcs internal set."
					+ " Meaning: Snoopy SPPED write error. Please ensure after loading that net is correct.",
					 "error", true);
			
			if(howMany > arcNumber) 
				GUIManager.getDefaultGUIManager().log("More arcs saved than should be present in the model. Please advise "
						+ "authors of the program as this may be element-removal algorithmic error.", "error", true);
			else
				GUIManager.getDefaultGUIManager().log("Less arcs saved than should be present in the model. Please advise "
						+ "authors of the program.", "error", true);
		}
	}
	
	//TODO:
	private void addArcsAndCoarseToFile(BufferedWriter bw, int currentActiveID) {
		int howMany = 0;
		int xOff = 0;
		int baseIDforNode = currentActiveID;
		
		ArrayList<Arc> normalArcs = new ArrayList<Arc>();
		ArrayList<Arc> metaArcs = new ArrayList<Arc>();
		
		//podziel łuki na dwa zbiory:
		for(Arc arc : arcs) {
			if(arc.getArcType() == TypesOfArcs.META_ARC)
				metaArcs.add(arc);
			else
				normalArcs.add(arc);
		}
		

		//utwórz listę interfejsów:
		ArrayList<Node> interfacesIN = new ArrayList<Node>(); //łuki wchodzące do podsieci
		ArrayList<Node> interfacesOUT = new ArrayList<Node>(); //łuki wychodzące Z podsieci
		for(MetaNode mnode : metanodes) {
			ElementLocation alpha = mnode.getElementLocations().get(0);
			for(Arc a : alpha.accessMetaInArcs()) {
				Node inter = a.getStartNode();
				if(!interfacesIN.contains(inter))
					interfacesIN.add(inter);
			}
			for(Arc a : alpha.accessMetaOutArcs()) {
				Node inter = a.getEndNode();
				if(!interfacesOUT.contains(inter))
					interfacesOUT.add(inter);
			}
		}
		
		
		for(Arc arc : normalArcs) {
			ElementLocation arcStartElLocation = arc.getStartLocation();
			ElementLocation arcEndElLocation = arc.getEndLocation();
			Node startN = arc.getStartNode();
			Node endN = arc.getEndNode();
			
			//normalny pojedynczy łuk
			int weight = arc.getWeight(); //waga łuku
			String comment = arc.getComment();

			int nodeSourceID = 0; // <edge source="1112" target="1129" id="1123" net="1"> //duże Nonde'y
			int realSourceID = 0; // <graphic id="1128" net="1" source="1122" target="1111" state="4" show="1" pen="0,0,0" brush="0,0,0" edge_designtype="3">
			int realSourceX = 0;
			int realSourceY = 0;
			int nodeTargetID = 0; // <edge source="1112" target="1129" id="1123" net="1"> //duże Nonde'y
			int realTargetID = 0; // <graphic id="1128" net="1" source="1122" target="1111" state="4" show="1" pen="0,0,0" brush="0,0,0" edge_designtype="3">
			int realTargetX = 0;
			int realTargetY = 0;
			int halfX = 0;
			int halfY = 0;
			
			//int NET1nodeSourceID = 0;
			int NET1realSourceID = 0;
			int NET1realSourceX = 0;
			int NET1realSourceY = 0;
			//int NET1nodeTargetID = 0;
			int NET1realTargetID = 0;
			int NET1realTargetX = 0;
			int NET1realTargetY = 0;
			int NET1halfX = 0;
			int NET1halfY = 0;
			
			if(interfacesIN.contains(startN) && !interfacesOUT.contains(endN) && !(arcStartElLocation.getSheetID() == 0)) {
				//interesuje nas startN (wejście do podsieci)
				int subnet = arcStartElLocation.getSheetID();
				MetaNode metanode = null;
				for(MetaNode metaN : metanodes) {
					if(metaN.getRepresentedSheetID() == subnet) {
						metanode = metaN;
						break;
					}
				}
				
				if(startN instanceof Place) {
					if(metanode.getMetaType() != MetaType.SUBNETTRANS) {
						GUIManager.getDefaultGUIManager().log("Critical error: wrong subnet type for interface node.", "error", true);
					}
					//znajdź pasujący łuk do metanode
					Arc metaInArc = null;
		
					for(Arc cand_arc : metaArcs) {
						if(cand_arc.getEndNode().equals(metanode)) {
							if(cand_arc.getStartNode().equals(startN)) {
								metaInArc = cand_arc;
								break;
							}
						}
					}
					boolean ok = metaArcs.remove(metaInArc);
					if(!ok) {
						GUIManager.getDefaultGUIManager().log("Error: no meta-arc for existing arc of the interface node.", "error", true);
					}
					
					//metaInArc to łuk wejściowy, z niego wyciągamy ElementLocation startNode'a
					//   SUBNET section:
					int addToSPPEDAsSource = abyssPlacesID.lastIndexOf(startN.getID()); //który to był
					int startLocIndex = startN.getElementLocations().indexOf(arcStartElLocation);
					SnoopyWriterPlace source = snoopyWriterPlaces.get(addToSPPEDAsSource);
					nodeSourceID = source.snoopyStartingID;
					realSourceID = source.grParents.get(startLocIndex);
					realSourceX = source.grParentsLocation.get(startLocIndex).x;
					realSourceY = source.grParentsLocation.get(startLocIndex).y;
					
					int addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(endN.getID());
					int endLocIndex = endN.getElementLocations().indexOf(arcEndElLocation);
					SnoopyWriterTransition target = snoopyWriterTransitions.get(addToSPPEDAsTarget);
					nodeTargetID = target.snoopyStartingID;
					realTargetID = target.grParents.get(endLocIndex); 
					realTargetX = target.grParentsLocation.get(endLocIndex).x;
					realTargetY = target.grParentsLocation.get(endLocIndex).y;
					
					halfX = (realTargetX + realSourceX) / 2;
					halfY = (realTargetY + realSourceY) / 2;
					
					//   NET1 = sieć z grafiką metanode/coarse-cośtam:
					ElementLocation NET1el = metaInArc.getStartLocation();
					int NET1startLocIndex = startN.getElementLocations().indexOf(NET1el);
					int NET1addToSPPEDAsSource = abyssPlacesID.lastIndexOf(startN.getID());
					SnoopyWriterPlace NET1source = snoopyWriterPlaces.get(NET1addToSPPEDAsSource);
					//NET1nodeSourceID = NET1source.snoopyStartingID;
					NET1realSourceID = NET1source.grParents.get(NET1startLocIndex);
					NET1realSourceX = NET1source.grParentsLocation.get(NET1startLocIndex).x;
					NET1realSourceY = NET1source.grParentsLocation.get(NET1startLocIndex).y;
					
					int NET1addToSPPEDAsTarget = abyssCoarseTransitionsID.lastIndexOf(metanode);
					SnoopyWriterCoarse NET1target = snoopyWriterCoarseTransitions.get(NET1addToSPPEDAsTarget);
					//NET1nodeTargetID = NET1target.snoopyStartingID;
					NET1realTargetID = NET1target.grParents.get(0);
					NET1realTargetX = NET1target.grParentsLocation.get(0).x;
					NET1realTargetY = NET1target.grParentsLocation.get(0).y;
					
					NET1halfX = (NET1realTargetX + NET1realSourceX) / 2;
					NET1halfY = (NET1realTargetY + NET1realSourceY) / 2;
				} else { //(startN instanceof Transition)
					if(metanode.getMetaType() != MetaType.SUBNETPLACE) {
						GUIManager.getDefaultGUIManager().log("Critical error: wrong subnet type for interface node.", "error", true);
					}
					//znajdź pasujący łuk do metanode
					Arc metaInArc = null;
		
					for(Arc cand_arc : metaArcs) {
						if(cand_arc.getEndNode().equals(metanode)) {
							if(cand_arc.getStartNode().equals(startN)) {
								metaInArc = cand_arc;
								break;
							}
						}
					}
					boolean ok = metaArcs.remove(metaInArc);
					if(!ok) {
						GUIManager.getDefaultGUIManager().log("Error: no meta-arc for existing arc of the interface node.", "error", true);
					}
					
					//metaInArc to łuk wejściowy, z niego wyciągamy ElementLocation startNode'a
					//   SUBNET section:
					int addToSPPEDAsSource = abyssTransitionsID.lastIndexOf(startN.getID());
					SnoopyWriterTransition source = snoopyWriterTransitions.get(addToSPPEDAsSource);
					int startLocIndex = startN.getElementLocations().indexOf(arcStartElLocation);
					nodeSourceID = source.snoopyStartingID;
					realSourceID = source.grParents.get(startLocIndex);
					realSourceX = source.grParentsLocation.get(startLocIndex).x;
					realSourceY = source.grParentsLocation.get(startLocIndex).y;
					
					int addToSPPEDAsTarget = abyssPlacesID.lastIndexOf(endN.getID());
					int endLocIndex = endN.getElementLocations().indexOf(arcEndElLocation);
					SnoopyWriterPlace target = snoopyWriterPlaces.get(addToSPPEDAsTarget);
					nodeTargetID = target.snoopyStartingID;
					realTargetID = target.grParents.get(endLocIndex); 
					realTargetX = target.grParentsLocation.get(endLocIndex).x;
					realTargetY = target.grParentsLocation.get(endLocIndex).y;
					
					halfX = (realTargetX + realSourceX) / 2;
					halfY = (realTargetY + realSourceY) / 2;
					
					//   NET1 = sieć z grafiką metanode/coarse-cośtam:
					ElementLocation NET1el = metaInArc.getStartLocation();
					int NET1startLocIndex = startN.getElementLocations().indexOf(NET1el);
					int NET1addToSPPEDAsSource = abyssTransitionsID.lastIndexOf(startN.getID());
					SnoopyWriterTransition NET1source = snoopyWriterTransitions.get(NET1addToSPPEDAsSource);
					//NET1nodeSourceID = NET1source.snoopyStartingID;
					NET1realSourceID = NET1source.grParents.get(NET1startLocIndex);
					NET1realSourceX = NET1source.grParentsLocation.get(NET1startLocIndex).x;
					NET1realSourceY = NET1source.grParentsLocation.get(NET1startLocIndex).y;
					
					int NET1addToSPPEDAsTarget = abyssCoarsePlacesID.lastIndexOf(metanode);
					SnoopyWriterCoarse NET1target = snoopyWriterCoarsePlaces.get(NET1addToSPPEDAsTarget);
					//NET1nodeTargetID = NET1target.snoopyStartingID;
					NET1realTargetID = NET1target.grParents.get(0);
					NET1realTargetX = NET1target.grParentsLocation.get(0).x;
					NET1realTargetY = NET1target.grParentsLocation.get(0).y;
					
					NET1halfX = (NET1realTargetX + NET1realSourceX) / 2;
					NET1halfY = (NET1realTargetY + NET1realSourceY) / 2;
				}
				
				
				//baseIDforNode
				int grParent = baseIDforNode + 5; //dla meta-arc
				int grParent2 = baseIDforNode + 20;
				
				int sheetMainID = metanode.getElementLocations().get(0).getSheetID() + 1;
				int subNetID = metanode.getRepresentedSheetID() + 1;
				
				write(bw, "      <edge source=\""+nodeSourceID+"\" target=\""+nodeTargetID+"\" id=\""+(baseIDforNode)+"\" net=\""+sheetMainID+"\">");
				write(bw, "        <attribute name=\"Multiplicity\" id=\""+(baseIDforNode+1)+"\" net=\""+sheetMainID+"\">");
				write(bw, "          <![CDATA["+weight+"]]>");
				write(bw, "          <graphics count=\"2\">");
				xOff = 20;
				//P/T -> metanode
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(NET1halfX+xOff)+".00\""
						+ " y=\""+NET1halfY+".00\" id=\""+(baseIDforNode+2)+"\" net=\""+sheetMainID+"\""
						+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//P/T - T/P in subnet
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(halfX+xOff)+".00\""
						+ " y=\""+halfY+".00\" id=\""+(baseIDforNode+21)+"\" net=\""+subNetID+"\""
						+ " show=\"1\" grparent=\""+grParent2+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				write(bw, "          </graphics>");
				write(bw, "        </attribute>");
				write(bw, "        <attribute name=\"Comment\" id=\""+(baseIDforNode+3)+"\" net=\""+sheetMainID+"\">");
				write(bw, "          <![CDATA["+comment+"]]>");
				write(bw, "          <graphics count=\"2\">");
				xOff = 40;
				//P/T -> metanode
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(NET1halfX+xOff)+".00\""
						+ " y=\""+NET1halfY+".00\" id=\""+(baseIDforNode+4)+"\" net=\""+sheetMainID+"\""
						+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//P/T - T/P in subnet
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(halfX+xOff)+".00\""
						+ " y=\""+halfY+".00\" id=\""+(baseIDforNode+22)+"\" net=\""+subNetID+"\""
						+ " show=\"1\" grparent=\""+grParent2+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				
				write(bw, "          </graphics>");
				write(bw, "        </attribute>");
				write(bw, "        <graphics count=\"2\">");
				
				//TUTAJ WCHODZĄ REALNE X,Y I ID PORTALI:
				//P/T -> metanode
				write(bw, "          <graphic id=\""+grParent+"\" net=\""+sheetMainID+"\""
						+ " source=\""+NET1realSourceID+"\""
						+ " target=\""+NET1realTargetID+"\" state=\"4\" show=\"1\""
						+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");			
				write(bw, "            <points count=\"2\">"); //bez łamańców
				write(bw, "              <point x=\""+NET1realSourceX+".00\" y=\""+NET1realSourceY+".00\"/>");
				write(bw, "              <point x=\""+NET1realTargetX+".00\" y=\""+NET1realTargetY+".00\"/>");
				write(bw, "            </points>");
				write(bw, "          </graphic>");
				//P/T - T/P in subnet
				write(bw, "          <graphic id=\""+grParent2+"\" net=\""+subNetID+"\""
						+ " source=\""+realSourceID+"\""
						+ " target=\""+realTargetID+"\" state=\"8\" show=\"1\""
						+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");			
				write(bw, "            <points count=\"2\">"); //bez łamańców
				write(bw, "              <point x=\""+realSourceX+".00\" y=\""+realSourceY+".00\"/>");
				write(bw, "              <point x=\""+realTargetX+".00\" y=\""+realTargetY+".00\"/>");
				write(bw, "            </points>");
				write(bw, "          </graphic>");
				write(bw, "        </graphics>");
				write(bw, "      </edge>");
				howMany++;
				
				baseIDforNode += 23;
				
			} else if(!interfacesIN.contains(startN) && interfacesOUT.contains(endN) && arcStartElLocation.getSheetID() == 0) {
				//interesuje nas endN (wyjście z podsieci)
				//interesuje nas startN (wejście do podsieci)
				int subnet = arcEndElLocation.getSheetID();
				MetaNode metanode = null;
				for(MetaNode metaN : metanodes) {
					if(metaN.getRepresentedSheetID() == subnet) {
						metanode = metaN;
						break;
					}
				}
				
				if(endN instanceof Place) {
					if(metanode.getMetaType() != MetaType.SUBNETTRANS) {
						GUIManager.getDefaultGUIManager().log("Critical error: wrong subnet type for interface node.", "error", true);
					}
					//znajdź pasujący łuk do metanode
					Arc metaOutArc = null;
		
					for(Arc cand_arc : metaArcs) {
						if(cand_arc.getStartNode().equals(metanode)) {
							if(cand_arc.getEndNode().equals(endN)) {
								metaOutArc = cand_arc;
								break;
							}
						}
					}
					boolean ok = metaArcs.remove(metaOutArc);
					if(!ok) {
						GUIManager.getDefaultGUIManager().log("Error: no meta-arc for existing arc of the interface node.", "error", true);
					}
					
					//metaOutArc to łuk wyjściowy, z niego wyciągamy ElementLocation startNode'a
					//   SUBNET section: 
					int addToSPPEDAsSource = abyssTransitionsID.lastIndexOf(startN.getID()); //który to był
					int startLocIndex = startN.getElementLocations().indexOf(arcStartElLocation);
					SnoopyWriterTransition source = snoopyWriterTransitions.get(addToSPPEDAsSource);
					nodeSourceID = source.snoopyStartingID;
					realSourceID = source.grParents.get(startLocIndex);
					realSourceX = source.grParentsLocation.get(startLocIndex).x;
					realSourceY = source.grParentsLocation.get(startLocIndex).y;
					
					int addToSPPEDAsTarget = abyssPlacesID.lastIndexOf(endN.getID());
					int endLocIndex = endN.getElementLocations().indexOf(arcEndElLocation);
					SnoopyWriterPlace target = snoopyWriterPlaces.get(addToSPPEDAsTarget);
					nodeTargetID = target.snoopyStartingID;
					realTargetID = target.grParents.get(endLocIndex); 
					realTargetX = target.grParentsLocation.get(endLocIndex).x;
					realTargetY = target.grParentsLocation.get(endLocIndex).y;
					
					halfX = (realTargetX + realSourceX) / 2;
					halfY = (realTargetY + realSourceY) / 2;
					
					//   NET1 = sieć z grafiką metanode/coarse-cośtam:
					
					int NET1addToSPPEDAsSource = abyssCoarseTransitionsID.lastIndexOf(metanode);
					SnoopyWriterCoarse NET1source = snoopyWriterCoarseTransitions.get(NET1addToSPPEDAsSource);
					//NET1nodeTargetID = NET1target.snoopyStartingID;
					NET1realSourceID = NET1source.grParents.get(0);
					NET1realSourceX = NET1source.grParentsLocation.get(0).x;
					NET1realSourceY = NET1source.grParentsLocation.get(0).y;

					ElementLocation NET1el = metaOutArc.getEndLocation();
					int NET1endLocIndex = endN.getElementLocations().indexOf(NET1el);
					int NET1addToSPPEDAsTarget = abyssPlacesID.lastIndexOf(endN.getID());
					SnoopyWriterPlace NET1target = snoopyWriterPlaces.get(NET1addToSPPEDAsTarget);
					//NET1nodeSourceID = NET1target.snoopyStartingID;
					NET1realTargetID = NET1target.grParents.get(NET1endLocIndex);
					NET1realTargetX = NET1target.grParentsLocation.get(NET1endLocIndex).x;
					NET1realTargetY = NET1target.grParentsLocation.get(NET1endLocIndex).y;
					
					NET1halfX = (NET1realTargetX + NET1realSourceX) / 2;
					NET1halfY = (NET1realTargetY + NET1realSourceY) / 2;
				} else { //(endN instanceof Transition)
					if(metanode.getMetaType() != MetaType.SUBNETPLACE) {
						GUIManager.getDefaultGUIManager().log("Critical error: wrong subnet type for interface node.", "error", true);
					}
					//znajdź pasujący łuk do metanode
					Arc metaOutArc = null;
		
					for(Arc cand_arc : metaArcs) {
						if(cand_arc.getStartNode().equals(metanode)) {
							if(cand_arc.getEndNode().equals(endN)) {
								metaOutArc = cand_arc;
								break;
							}
						}
					}
					boolean ok = metaArcs.remove(metaOutArc);
					if(!ok) {
						GUIManager.getDefaultGUIManager().log("Error: no meta-arc for existing arc of the interface node.", "error", true);
					}
					
					//metaInArc to łuk wejściowy, z niego wyciągamy ElementLocation startNode'a
					//   SUBNET section:
					int addToSPPEDAsSource = abyssPlacesID.lastIndexOf(startN.getID()); //który to był
					SnoopyWriterPlace source = snoopyWriterPlaces.get(addToSPPEDAsSource);
					int startLocIndex = startN.getElementLocations().indexOf(arcStartElLocation);
					nodeSourceID = source.snoopyStartingID;
					realSourceID = source.grParents.get(startLocIndex);
					realSourceX = source.grParentsLocation.get(startLocIndex).x;
					realSourceY = source.grParentsLocation.get(startLocIndex).y;
					
					
					int addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(endN.getID());
					SnoopyWriterTransition target = snoopyWriterTransitions.get(addToSPPEDAsTarget);
					int endLocIndex = endN.getElementLocations().indexOf(arcEndElLocation);
					nodeTargetID = target.snoopyStartingID;
					realTargetID = target.grParents.get(endLocIndex); 
					realTargetX = target.grParentsLocation.get(endLocIndex).x;
					realTargetY = target.grParentsLocation.get(endLocIndex).y;
					
					halfX = (realTargetX + realSourceX) / 2;
					halfY = (realTargetY + realSourceY) / 2;
					
					//   NET1 = sieć z grafiką metanode/coarse-cośtam:
					int NET1addToSPPEDAsSource = abyssCoarsePlacesID.lastIndexOf(metanode);
					SnoopyWriterCoarse NET1source = snoopyWriterCoarsePlaces.get(NET1addToSPPEDAsSource);
					//NET1nodeTargetID = NET1source.snoopyStartingID;
					NET1realSourceID = NET1source.grParents.get(0);
					NET1realSourceX = NET1source.grParentsLocation.get(0).x;
					NET1realSourceY = NET1source.grParentsLocation.get(0).y;
					
					
					ElementLocation NET1el = metaOutArc.getEndLocation();
					int NET1EndLocIndex = endN.getElementLocations().indexOf(NET1el);
					int NET1addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(endN.getID());
					SnoopyWriterTransition NET1target = snoopyWriterTransitions.get(NET1addToSPPEDAsTarget);
					//NET1nodeSourceID = NET1target.snoopyStartingID;
					NET1realTargetID = NET1target.grParents.get(NET1EndLocIndex);
					NET1realTargetX = NET1target.grParentsLocation.get(NET1EndLocIndex).x;
					NET1realTargetY = NET1target.grParentsLocation.get(NET1EndLocIndex).y;

					
					NET1halfX = (NET1realTargetX + NET1realSourceX) / 2;
					NET1halfY = (NET1realTargetY + NET1realSourceY) / 2;
				}
				
				//baseIDforNode
				int grParent = baseIDforNode + 5; //dla meta-arc
				int grParent2 = baseIDforNode + 20;
				
				int sheetMainID = metanode.getElementLocations().get(0).getSheetID() + 1;
				int subNetID = metanode.getRepresentedSheetID() + 1;
				
				write(bw, "      <edge source=\""+nodeSourceID+"\" target=\""+nodeTargetID+"\" id=\""+(baseIDforNode)+"\" net=\""+sheetMainID+"\">");
				write(bw, "        <attribute name=\"Multiplicity\" id=\""+(baseIDforNode+1)+"\" net=\""+sheetMainID+"\">");
				write(bw, "          <![CDATA["+weight+"]]>");
				write(bw, "          <graphics count=\"2\">");
				xOff = 20;
				//P/T -> metanode
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(NET1halfX+xOff)+".00\""
						+ " y=\""+NET1halfY+".00\" id=\""+(baseIDforNode+2)+"\" net=\""+sheetMainID+"\""
						+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//P/T - T/P in subnet
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(halfX+xOff)+".00\""
						+ " y=\""+halfY+".00\" id=\""+(baseIDforNode+21)+"\" net=\""+subNetID+"\""
						+ " show=\"1\" grparent=\""+grParent2+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				write(bw, "          </graphics>");
				write(bw, "        </attribute>");
				write(bw, "        <attribute name=\"Comment\" id=\""+(baseIDforNode+3)+"\" net=\""+sheetMainID+"\">");
				write(bw, "          <![CDATA["+comment+"]]>");
				write(bw, "          <graphics count=\"2\">");
				xOff = 40;
				//P/T -> metanode
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(NET1halfX+xOff)+".00\""
						+ " y=\""+NET1halfY+".00\" id=\""+(baseIDforNode+4)+"\" net=\""+sheetMainID+"\""
						+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//P/T - T/P in subnet
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(halfX+xOff)+".00\""
						+ " y=\""+halfY+".00\" id=\""+(baseIDforNode+22)+"\" net=\""+subNetID+"\""
						+ " show=\"1\" grparent=\""+grParent2+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				
				write(bw, "          </graphics>");
				write(bw, "        </attribute>");
				write(bw, "        <graphics count=\"2\">");
				
				//TUTAJ WCHODZĄ REALNE X,Y I ID PORTALI:
				//P/T -> metanode
				write(bw, "          <graphic id=\""+grParent+"\" net=\""+sheetMainID+"\""
						+ " source=\""+NET1realSourceID+"\""
						+ " target=\""+NET1realTargetID+"\" state=\"4\" show=\"1\""
						+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");			
				write(bw, "            <points count=\"2\">"); //bez łamańców
				write(bw, "              <point x=\""+NET1realSourceX+".00\" y=\""+NET1realSourceY+".00\"/>");
				write(bw, "              <point x=\""+NET1realTargetX+".00\" y=\""+NET1realTargetY+".00\"/>");
				write(bw, "            </points>");
				write(bw, "          </graphic>");
				//P/T - T/P in subnet
				write(bw, "          <graphic id=\""+grParent2+"\" net=\""+subNetID+"\""
						+ " source=\""+realSourceID+"\""
						+ " target=\""+realTargetID+"\" state=\"8\" show=\"1\""
						+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");			
				write(bw, "            <points count=\"2\">"); //bez łamańców
				write(bw, "              <point x=\""+realSourceX+".00\" y=\""+realSourceY+".00\"/>");
				write(bw, "              <point x=\""+realTargetX+".00\" y=\""+realTargetY+".00\"/>");
				write(bw, "            </points>");
				write(bw, "          </graphic>");
				write(bw, "        </graphics>");
				write(bw, "      </edge>");
				howMany++;
				
				baseIDforNode += 23;

				
			} else if(interfacesIN.contains(startN) && interfacesOUT.contains(endN) && arcStartElLocation.getSheetID() == 0) {
				GUIManager.getDefaultGUIManager().log("Error - SnoopyWriter encountered problem with net structure.", "error", true);
			} else { 
			
				if(startN instanceof Place) {
					int addToSPPEDAsSource = abyssPlacesID.lastIndexOf(startN.getID()); //który to był
					int startLocIndex = startN.getElementLocations().indexOf(arcStartElLocation);
					SnoopyWriterPlace source = snoopyWriterPlaces.get(addToSPPEDAsSource);
					nodeSourceID = source.snoopyStartingID;
					realSourceID = source.grParents.get(startLocIndex);
					realSourceX = source.grParentsLocation.get(startLocIndex).x;
					realSourceY = source.grParentsLocation.get(startLocIndex).y;
					
					int addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(endN.getID());
					int endLocIndex = endN.getElementLocations().indexOf(arcEndElLocation);
					SnoopyWriterTransition target = snoopyWriterTransitions.get(addToSPPEDAsTarget);
					nodeTargetID = target.snoopyStartingID;
					realTargetID = target.grParents.get(endLocIndex); 
					realTargetX = target.grParentsLocation.get(endLocIndex).x;
					realTargetY = target.grParentsLocation.get(endLocIndex).y;
					
					halfX = (realTargetX + realSourceX) / 2;
					halfY = (realTargetY + realSourceY) / 2;
				} else {
					int addToSPPEDAsSource = abyssTransitionsID.lastIndexOf(startN.getID()); //który to był
					int startLocIndex = startN.getElementLocations().indexOf(arcStartElLocation);
					SnoopyWriterTransition source = snoopyWriterTransitions.get(addToSPPEDAsSource);
					nodeSourceID = source.snoopyStartingID;
					realSourceID = source.grParents.get(startLocIndex);
					realSourceX = source.grParentsLocation.get(startLocIndex).x;
					realSourceY = source.grParentsLocation.get(startLocIndex).y;
					
					int addToSPPEDAsTarget = abyssPlacesID.lastIndexOf(endN.getID());
					int endLocIndex = endN.getElementLocations().indexOf(arcEndElLocation);
					SnoopyWriterPlace target = snoopyWriterPlaces.get(addToSPPEDAsTarget);
					nodeTargetID = target.snoopyStartingID;
					realTargetID = target.grParents.get(endLocIndex); 
					realTargetX = target.grParentsLocation.get(endLocIndex).x;
					realTargetY = target.grParentsLocation.get(endLocIndex).y;
					
					halfX = (realTargetX + realSourceX) / 2;
					halfY = (realTargetY + realSourceY) / 2;
				}
				
				int grParent = baseIDforNode + 5;
				int sheetMainID = arcStartElLocation.getSheetID() + 1;
				
				
				write(bw, "      <edge source=\""+nodeSourceID+"\" target=\""+nodeTargetID+"\" id=\""+(baseIDforNode)+"\" net=\""+sheetMainID+"\">");
				write(bw, "        <attribute name=\"Multiplicity\" id=\""+(baseIDforNode+1)+"\" net=\""+sheetMainID+"\">");
				write(bw, "          <![CDATA["+weight+"]]>");
				write(bw, "          <graphics count=\"1\">");
				xOff = 20;
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(halfX+xOff)+".00\""
						+ " y=\""+halfY+".00\" id=\""+(baseIDforNode+2)+"\" net=\""+sheetMainID+"\""
						+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				write(bw, "          </graphics>");
				write(bw, "        </attribute>");
				write(bw, "        <attribute name=\"Comment\" id=\""+(baseIDforNode+3)+"\" net=\""+sheetMainID+"\">");
				write(bw, "          <![CDATA["+comment+"]]>");
				write(bw, "          <graphics count=\"1\">");
				xOff = 40;
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\""+(halfX+xOff)+".00\""
						+ " y=\""+halfY+".00\" id=\""+(baseIDforNode+4)+"\" net=\""+sheetMainID+"\""
						+ " show=\"1\" grparent=\""+grParent+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				
				write(bw, "          </graphics>");
				write(bw, "        </attribute>");
				write(bw, "        <graphics count=\"1\">");
				
				//TUTAJ WCHODZĄ REALNE X,Y I ID PORTALI:
				//baseIDforNode +5 == grParent
				write(bw, "          <graphic id=\""+grParent+"\" net=\""+sheetMainID+"\""
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
				
				baseIDforNode += 6; //normal node
			}	
		}

		int arcNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs().size();
		if(howMany != arcNumber) {
			GUIManager.getDefaultGUIManager().log("Arcs saved do not match size of Arcs internal set."
					+ " Meaning: Snoopy SPPED write error. Please ensure after loading that net is correct.",
					 "error", true);
			
			if(howMany > arcNumber) 
				GUIManager.getDefaultGUIManager().log("More arcs saved than should be present in the model. Please advise "
						+ "authors of the program as this may be element-removal algorithmic error.", "error", true);
			else
				GUIManager.getDefaultGUIManager().log("Less arcs saved than should be present in the model. Please advise "
						+ "authors of the program.", "error", true);
		}
	}
	
	
	
	

	private int addArcsInfoExtended(BufferedWriter bw, int currentActiveID, TypesOfArcs arcClass, int howManyToSave) {
		int howManySaved = 0;
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
					try {
						if(a.getArcType() != arcClass)
							continue;
						
						int weight = a.getWeight(); //waga łuku
						String comment = a.getComment();
						int grParent = currentActiveID + 5;
						
						Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss
						Node sourceAbyss = a.getStartNode(); //stąd wychodzi
						//przy czym należy określić, do której lokalizacji
						
						//sourceAbyss == p
						
						int addToSPPEDAsSource = abyssPlacesID.lastIndexOf(sourceAbyss.getID()); //który to był
						if(addToSPPEDAsSource == -1) {
							@SuppressWarnings("unused")
							int WTF= 1; //!!! IMPOSSIBRU!!!!
							return nextID+10;
						}
						SnoopyWriterPlace source = snoopyWriterPlaces.get(addToSPPEDAsSource);
						int nodeSourceID = source.snoopyStartingID;
						int realSourceID = source.grParents.get(location); //k
						int realSourceX = source.grParentsLocation.get(location).x;
						int realSourceY = source.grParentsLocation.get(location).y;
						
						
						//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
						int addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(targetAbyss.getID());
						SnoopyWriterTransition target = snoopyWriterTransitions.get(addToSPPEDAsTarget);
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
						int nodeTargetID = target.snoopyStartingID;
						int realTargetID = target.grParents.get(counter); 
						int realTargetX = target.grParentsLocation.get(counter).x;
						int realTargetY = target.grParentsLocation.get(counter).y;
						
						int halfX = (realTargetX + realSourceX) / 2;
						int halfY = (realTargetY + realSourceY) / 2;
						
						//tutaj wchodzą główne numery główne:
						write(bw, "      <edge source=\""+nodeSourceID+"\""
								+ " target=\""+nodeTargetID+"\" id=\""+nextID+"\" net=\"1\">");
						nextID++; //444
						
						if(arcClass != TypesOfArcs.RESET) {
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
						}
						
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
						
						howManySaved++;
					} catch (Exception e) {
						GUIManager.getDefaultGUIManager().log("Unable to save arc from "+a.getStartNode().getName()+" to "
								+ a.getEndNode().getName(), "error", true);
					}
				}
				
			} //dla wszystkich lokalizacji
			//iteracja++;
		} //dla wszystkich miejsc
		
		//teraz wszystkie wychodzące z tranzycji:
		for(Transition t : transitions) { // wyjściowe z tranzycji
			int location = -1;
			for(ElementLocation el : t.getElementLocations()) { // dla wszystkich jego lokalizacji
				location++; // która faktycznie to jest, jeśli trafiliśmy w portal
				ArrayList<Arc> outArcs = el.getOutArcs(); //pobież listę łuków wyjściowych (portalu)
				
				//kolekcjonowanie danych:
				for(Arc a : outArcs) { //dla każdego łuku
					try {
						if(a.getArcType() != arcClass || a.getArcType() == TypesOfArcs.READARC)
							continue;
						
						int weight = a.getWeight(); //waga łuku
						String comment = a.getComment();
						int grParent = currentActiveID + 5;
						
						Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss (w miejsce)
						Node sourceAbyss = a.getStartNode(); //stąd wychodzi (tranzycja)
						//przy czym należy okreslić, do której lokalizacji
						
						//sourceAbyss == t
						
						int addToSPPEDAsSource = abyssTransitionsID.lastIndexOf(sourceAbyss.getID()); //który to był
						if(addToSPPEDAsSource == -1) {
							@SuppressWarnings("unused")
							int WTF= 1; //!!! IMPOSSIBRU!!!!
							return nextID + 10;
						}
						SnoopyWriterTransition source = snoopyWriterTransitions.get(addToSPPEDAsSource);
						int nodeSourceID = source.snoopyStartingID;
						int realSourceID = source.grParents.get(location); //k
						int realSourceX = source.grParentsLocation.get(location).x;
						int realSourceY = source.grParentsLocation.get(location).y;
						
						
						//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
						int addToSPPEDAsTarget = abyssPlacesID.lastIndexOf(targetAbyss.getID());
						SnoopyWriterPlace target = snoopyWriterPlaces.get(addToSPPEDAsTarget);
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
						int nodeTargetID = target.snoopyStartingID;
						int realTargetID = target.grParents.get(counter); 
						int realTargetX = target.grParentsLocation.get(counter).x;
						int realTargetY = target.grParentsLocation.get(counter).y;
						
						int halfX = (realTargetX + realSourceX) / 2;
						int halfY = (realTargetY + realSourceY) / 2;
						
						//tutaj wchodzą główne numery:
						write(bw, "      <edge source=\""+nodeSourceID+"\""
								+ " target=\""+nodeTargetID+"\" id=\""+nextID+"\" net=\"1\">");
						nextID++; //444
						
						if(arcClass != TypesOfArcs.RESET) {
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
						}
						
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

						howManySaved++;
					} catch (Exception e) {
						GUIManager.getDefaultGUIManager().log("Unable to save arc from "+a.getStartNode().getName()+" to "
								+ a.getEndNode().getName(), "error", true);
					}
				}
				
			} //dla wszystkich lokalizacji
			//iteracja++;
		} //dla wszystkich tranzycji
		
		if(howManySaved != howManyToSave && arcClass != TypesOfArcs.READARC) {
			GUIManager.getDefaultGUIManager().log("Arcs saved do not match size of Arcs internal set."
					+ " Meaning: Snoopy SPPED write error. Please ensure after loading that net is correct.",
					 "error", true);
			if(howManySaved > howManyToSave) 
				GUIManager.getDefaultGUIManager().log("More arcs saved than should be present in the model. Please advise "
						+ "authors of the program as this may be element-removal algorithmic error.", "error", true);
			else
				GUIManager.getDefaultGUIManager().log("Less arcs saved than should be present in the model. Please advise "
						+ "authors of the program.", "error", true);
		}
		
		return nextID;
	}
	
	//TODO:
	private int addArcsAndCoarsesInfoExtended(BufferedWriter bw, int currentActiveID, TypesOfArcs arcClass, int howManyToSave) {
		int howManySaved = 0;
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
					try {
						if(a.getArcType() != arcClass)
							continue;
						
						int weight = a.getWeight(); //waga łuku
						String comment = a.getComment();
						int grParent = currentActiveID + 5;
						
						Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss
						Node sourceAbyss = a.getStartNode(); //stąd wychodzi
						//przy czym należy określić, do której lokalizacji
						
						//sourceAbyss == p
						
						int addToSPPEDAsSource = abyssPlacesID.lastIndexOf(sourceAbyss.getID()); //który to był
						if(addToSPPEDAsSource == -1) {
							@SuppressWarnings("unused")
							int WTF= 1; //!!! IMPOSSIBRU!!!!
							return nextID+10;
						}
						SnoopyWriterPlace source = snoopyWriterPlaces.get(addToSPPEDAsSource);
						int nodeSourceID = source.snoopyStartingID;
						int realSourceID = source.grParents.get(location); //k
						int realSourceX = source.grParentsLocation.get(location).x;
						int realSourceY = source.grParentsLocation.get(location).y;
						
						
						//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
						int addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(targetAbyss.getID());
						SnoopyWriterTransition target = snoopyWriterTransitions.get(addToSPPEDAsTarget);
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
						int nodeTargetID = target.snoopyStartingID;
						int realTargetID = target.grParents.get(counter); 
						int realTargetX = target.grParentsLocation.get(counter).x;
						int realTargetY = target.grParentsLocation.get(counter).y;
						
						int halfX = (realTargetX + realSourceX) / 2;
						int halfY = (realTargetY + realSourceY) / 2;
						
						//tutaj wchodzą główne numery główne:
						write(bw, "      <edge source=\""+nodeSourceID+"\""
								+ " target=\""+nodeTargetID+"\" id=\""+nextID+"\" net=\"1\">");
						nextID++; //444
						
						if(arcClass != TypesOfArcs.RESET) {
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
						}
						
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
						
						howManySaved++;
					} catch (Exception e) {
						GUIManager.getDefaultGUIManager().log("Unable to save arc from "+a.getStartNode().getName()+" to "
								+ a.getEndNode().getName(), "error", true);
					}
				}
				
			} //dla wszystkich lokalizacji
			//iteracja++;
		} //dla wszystkich miejsc
		
		//teraz wszystkie wychodzące z tranzycji:
		for(Transition t : transitions) { // wyjściowe z tranzycji
			int location = -1;
			for(ElementLocation el : t.getElementLocations()) { // dla wszystkich jego lokalizacji
				location++; // która faktycznie to jest, jeśli trafiliśmy w portal
				ArrayList<Arc> outArcs = el.getOutArcs(); //pobież listę łuków wyjściowych (portalu)
				
				//kolekcjonowanie danych:
				for(Arc a : outArcs) { //dla każdego łuku
					try {
						if(a.getArcType() != arcClass || a.getArcType() == TypesOfArcs.READARC)
							continue;
						
						int weight = a.getWeight(); //waga łuku
						String comment = a.getComment();
						int grParent = currentActiveID + 5;
						
						Node targetAbyss = a.getEndNode(); //tutaj trafia łuk w Abyss (w miejsce)
						Node sourceAbyss = a.getStartNode(); //stąd wychodzi (tranzycja)
						//przy czym należy okreslić, do której lokalizacji
						
						//sourceAbyss == t
						
						int addToSPPEDAsSource = abyssTransitionsID.lastIndexOf(sourceAbyss.getID()); //który to był
						if(addToSPPEDAsSource == -1) {
							@SuppressWarnings("unused")
							int WTF= 1; //!!! IMPOSSIBRU!!!!
							return nextID + 10;
						}
						SnoopyWriterTransition source = snoopyWriterTransitions.get(addToSPPEDAsSource);
						int nodeSourceID = source.snoopyStartingID;
						int realSourceID = source.grParents.get(location); //k
						int realSourceX = source.grParentsLocation.get(location).x;
						int realSourceY = source.grParentsLocation.get(location).y;
						
						
						//teraz pobieramy miejsce dodane do snoopiego - docelowe do naszego
						int addToSPPEDAsTarget = abyssPlacesID.lastIndexOf(targetAbyss.getID());
						SnoopyWriterPlace target = snoopyWriterPlaces.get(addToSPPEDAsTarget);
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
						int nodeTargetID = target.snoopyStartingID;
						int realTargetID = target.grParents.get(counter); 
						int realTargetX = target.grParentsLocation.get(counter).x;
						int realTargetY = target.grParentsLocation.get(counter).y;
						
						int halfX = (realTargetX + realSourceX) / 2;
						int halfY = (realTargetY + realSourceY) / 2;
						
						//tutaj wchodzą główne numery:
						write(bw, "      <edge source=\""+nodeSourceID+"\""
								+ " target=\""+nodeTargetID+"\" id=\""+nextID+"\" net=\"1\">");
						nextID++; //444
						
						if(arcClass != TypesOfArcs.RESET) {
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
						}
						
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

						howManySaved++;
					} catch (Exception e) {
						GUIManager.getDefaultGUIManager().log("Unable to save arc from "+a.getStartNode().getName()+" to "
								+ a.getEndNode().getName(), "error", true);
					}
				}
				
			} //dla wszystkich lokalizacji
			//iteracja++;
		} //dla wszystkich tranzycji
		
		if(howManySaved != howManyToSave && arcClass != TypesOfArcs.READARC) {
			GUIManager.getDefaultGUIManager().log("Arcs saved do not match size of Arcs internal set."
					+ " Meaning: Snoopy SPPED write error. Please ensure after loading that net is correct.",
					 "error", true);
			if(howManySaved > howManyToSave) 
				GUIManager.getDefaultGUIManager().log("More arcs saved than should be present in the model. Please advise "
						+ "authors of the program as this may be element-removal algorithmic error.", "error", true);
			else
				GUIManager.getDefaultGUIManager().log("Less arcs saved than should be present in the model. Please advise "
						+ "authors of the program.", "error", true);
		}
		
		return nextID;
	}
	
	
	
	
	
	/**
	 * Metoda realizuje zapis pojedyńczej linii do pliku - zakończonej enterem.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param text String - linia
	 */
	private void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception e) {
			return;
		}
	}
	
	/**
	 * Metoda ta zapisuje końcówkę pliku sieci SPPED.
	 * @param bw BufferedWriter - obiekt zapisujący
	 */
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
