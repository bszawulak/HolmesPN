package abyss.files.io.Snoopy;

import java.io.BufferedWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.petrinet.elements.Arc.TypesOfArcs;

public class SnoopyWriterArcExt {
	private ArrayList<Place> places = null;
	private ArrayList<Transition> transitions = null;
	private ArrayList<MetaNode> metanodes = null;
	private ArrayList<Arc> arcs = null;
	private ArrayList<MetaNode> coarsePlaces = null;
	private ArrayList<MetaNode> coarseTransitions = null;
	
	private ArrayList<SnoopyWriterPlace> snoopyWriterPlaces = null;
	private ArrayList<Integer> abyssPlacesID = null;
	private ArrayList<SnoopyWriterTransition> snoopyWriterTransitions = null;
	private ArrayList<Integer> abyssTransitionsID = null;
	private ArrayList<SnoopyWriterCoarse> snoopyWriterCoarsePlaces = null;
	private ArrayList<Integer> abyssCoarsePlacesID = null;
	private ArrayList<SnoopyWriterCoarse> snoopyWriterCoarseTransitions = null;
	private ArrayList<Integer> abyssCoarseTransitionsID = null;
	
	public SnoopyWriterArcExt(ArrayList<Place> places, ArrayList<Transition> transitions, ArrayList<MetaNode> metanodes
			, ArrayList<Arc> arcs, ArrayList<MetaNode> coarsePlaces, ArrayList<MetaNode> coarseTransitions
			, ArrayList<SnoopyWriterPlace> snoopyWriterPlaces, ArrayList<Integer> abyssPlacesID
			, ArrayList<SnoopyWriterTransition> snoopyWriterTransitions, ArrayList<Integer> abyssTransitionsID
			, ArrayList<SnoopyWriterCoarse> snoopyWriterCoarsePlaces, ArrayList<Integer> abyssCoarsePlacesID
			, ArrayList<SnoopyWriterCoarse> snoopyWriterCoarseTransitions, ArrayList<Integer> abyssCoarseTransitionsID) {
		
		this.places = places;
		this.transitions = transitions;
		this.metanodes = metanodes;
		this.arcs = arcs;
		this.coarsePlaces = coarsePlaces;
		this.coarseTransitions = coarseTransitions;
		
		this.snoopyWriterPlaces = snoopyWriterPlaces;
		this.abyssPlacesID = abyssPlacesID;
		
		this.snoopyWriterTransitions = snoopyWriterTransitions;
		this.abyssTransitionsID = abyssTransitionsID;
		
		this.snoopyWriterCoarsePlaces = snoopyWriterCoarsePlaces;
		this.abyssCoarsePlacesID = abyssCoarsePlacesID;
		
		this.snoopyWriterCoarseTransitions = snoopyWriterCoarseTransitions;
		this.abyssCoarseTransitionsID = abyssCoarseTransitionsID;
	}
	
	//*****************************************************************************************************
	
	//TODO:
	public int addArcsAndCoarsesInfoExtended(BufferedWriter bw, int currentActiveID, TypesOfArcs arcClass, int howManyToSave) {
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
						
						//TODO: nextID++; ?? x 2 tutaj?
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
	
	//********************************************************************************************************************
	//********************************************************************************************************************
	//********************************************************************************************************************
	
	public int addArcsInfoExtended(BufferedWriter bw, int currentActiveID, TypesOfArcs arcClass, int howManyToSave) {
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
}
