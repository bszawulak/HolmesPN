package abyss.files.io.Snoopy;

import java.awt.Point;
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
import abyss.petrinet.elements.MetaNode.MetaType;

public class SnoopyWriterArc {
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
	
	public SnoopyWriterArc(ArrayList<Place> places, ArrayList<Transition> transitions, ArrayList<MetaNode> metanodes
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
	
	/**
	 * Metoda zapisująca łuki dla sieci wielopoziomowych.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param currentActiveID int - aktualnie wolne ID
	 */
	public void addArcsAndCoarseToFile(BufferedWriter bw, int currentActiveID) {
		boolean debug = false;
		
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
		int metaArcsTotal = metaArcs.size();

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
			try {
				ElementLocation arcStartElLocation = arc.getStartLocation();
				ElementLocation arcEndElLocation = arc.getEndLocation();
				Node startN = arc.getStartNode();
				Node endN = arc.getEndNode();
				
				//normalny pojedynczy łuk
				int weight = arc.getWeight(); //waga łuku
				String comment = removeEnters(arc.getComment());

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
				
				if(debug) {
					@SuppressWarnings("unused")
					int breakX = 1;
				}
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
						
						boolean abyss = weNeedToGoDeeper(metanode, startN, true);
						if(abyss) {
							baseIDforNode = omgThisIsCrazy(arcStartElLocation, arcEndElLocation, arc, metaArcs, baseIDforNode, true, bw);
							howMany++;
							continue;
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
						
						int NET1addToSPPEDAsTarget = abyssCoarseTransitionsID.lastIndexOf(metanode.getID());
						SnoopyWriterCoarse NET1target = snoopyWriterCoarseTransitions.get(NET1addToSPPEDAsTarget);
						//NET1nodeTargetID = NET1target.snoopyStartingID;
						NET1realTargetID = NET1target.grParents.get(0);
						NET1realTargetX = NET1target.grParentsLocation.get(0).x;
						NET1realTargetY = NET1target.grParentsLocation.get(0).y;
						
						NET1halfX = (NET1realTargetX + NET1realSourceX) / 2;
						NET1halfY = (NET1realTargetY + NET1realSourceY) / 2;
						
						//WHAT IF: z ta podsieć ma jeszcze inne wyjścia
						
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
						
						boolean deepSh = weNeedToGoDeeper(metanode, startN, true);
						if(deepSh) {
							baseIDforNode = omgThisIsCrazy(arcStartElLocation, arcEndElLocation, arc, metaArcs, baseIDforNode, true, bw);
							howMany++;
							continue;
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
						
						int NET1addToSPPEDAsTarget = abyssCoarsePlacesID.lastIndexOf(metanode.getID());
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
					
				} else if(!interfacesIN.contains(startN) && interfacesOUT.contains(endN) && !(arcStartElLocation.getSheetID() == 0)) {
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
						
						boolean deepSh = weNeedToGoDeeper(metanode, startN, false);
						if(deepSh) {
							baseIDforNode = omgThisIsCrazy(arcEndElLocation, arcStartElLocation, arc, metaArcs, baseIDforNode, false, bw);
							howMany++;
							continue;
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
						
						int NET1addToSPPEDAsSource = abyssCoarseTransitionsID.lastIndexOf(metanode.getID());
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
						
						boolean deepSh = weNeedToGoDeeper(metanode, startN, false);
						if(deepSh) {
							baseIDforNode = omgThisIsCrazy(arcEndElLocation, arcStartElLocation, arc, metaArcs, baseIDforNode, false, bw);
							howMany++;
							continue;
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
						int NET1addToSPPEDAsSource = abyssCoarsePlacesID.lastIndexOf(metanode.getID());
						SnoopyWriterCoarse NET1source = snoopyWriterCoarsePlaces.get(NET1addToSPPEDAsSource);
						NET1realSourceID = NET1source.grParents.get(0);
						NET1realSourceX = NET1source.grParentsLocation.get(0).x;
						NET1realSourceY = NET1source.grParentsLocation.get(0).y;
						
						
						ElementLocation NET1el = metaOutArc.getEndLocation();
						int NET1EndLocIndex = endN.getElementLocations().indexOf(NET1el);
						int NET1addToSPPEDAsTarget = abyssTransitionsID.lastIndexOf(endN.getID());
						SnoopyWriterTransition NET1target = snoopyWriterTransitions.get(NET1addToSPPEDAsTarget);
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
			} catch (Exception e) {
				
				GUIManager.getDefaultGUIManager().log("Unable to create arc: "+arc.toString(), "error", true);
			}
		}

		//metaArcsTotal;		
		int arcNumber = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs().size() - metaArcsTotal;
		if(howMany != arcNumber) {
			GUIManager.getDefaultGUIManager().log("Arcs saved do not match size of Arcs internal set."
					+ " Meaning: Snoopy SPPED write error. Please ensure after loading that net is correct.",
					 "error", true);
			
			if(howMany > arcNumber) 
				GUIManager.getDefaultGUIManager().log("More arcs saved than should be present in the model. Please notice "
						+ "authors of the program.", "error", true);
			else
				GUIManager.getDefaultGUIManager().log("Less arcs saved than should be present in the model. Please notice "
						+ "authors of the program.", "error", true);
		}
	}
	
	//********************************************************************************************************************
	
	/**
	 * Najbardziej szaleńcza z metod - odpowiada za zapis łuki w sieci wielopoziomowej
	 * @param interfaceNodeElLocation ElementLocation - interfejs
	 * @param arcEndElLocation ElementLocation - element z pary dla intefejsu
	 * @param arc Arc - aktualnie przetwarzany łuk
	 * @param metaArcs ArrayList[Arc] - z tej tablicy usuwamy wykorzystane meta-łuki
	 * @param baseIDforNode int - aktualny wolny ID
	 * @param isInInterface boolean - true, jeśli interfaceNodeElLocation jest intefejsem wejściowym (IN)
	 * @return int - nr następnego wolnego ID
	 */
	@SuppressWarnings("unused")
	private int omgThisIsCrazy(ElementLocation interfaceNodeElLocation, ElementLocation arcEndElLocation, Arc arcus, 
			ArrayList<Arc> metaArcs, int baseIDforNode, boolean isInInterface, BufferedWriter bw) {

		int IDbackup = baseIDforNode;
		int finalID = -1;
		Node startNode = interfaceNodeElLocation.getParentNode();
		Node endNode = arcEndElLocation.getParentNode();
		
		ArrayList<PathPackage> subnetsPath = recreateSubnetsPath(interfaceNodeElLocation, arcEndElLocation, arcus, isInInterface);
		//Collections.reverse(subnetsPath);
		int pathSize = subnetsPath.size();
		//teraz wiemy, od przez ile i jakie sieci należy przejść, poza pierwszą, pozostałe
		//będą związane meta-łukami
		int mainSourceID = -1;
		int mainTargetID = -1;
		ArrayList<Integer> subSourceIDs = new ArrayList<Integer>();
		ArrayList<Point> subSourcePoint = new ArrayList<Point>();
		ArrayList<Integer> subTargetIDs = new ArrayList<Integer>();
		ArrayList<Point> subTargetPoint = new ArrayList<Point>();
		ArrayList<Point> halfPoint = new ArrayList<Point>();
		
		ArrayList<Integer> grParents = new ArrayList<Integer>();
		for(int i=0; i<pathSize; i++) {
			if(i==0)
				grParents.add(IDbackup+=5);
			else if(i == 1)
				grParents.add(IDbackup+=15);
			else
				grParents.add(IDbackup+=5);
		}
		//teraz IDbackup zawiera maksymalną wartość
		
		//znajdź główne ID łuku:
		if(startNode instanceof Place) {
			int index = abyssPlacesID.lastIndexOf(startNode.getID());
			mainSourceID = snoopyWriterPlaces.get(index).snoopyStartingID;	
			index = abyssTransitionsID.lastIndexOf(endNode.getID());
			mainTargetID = snoopyWriterTransitions.get(index).snoopyStartingID;
		} else { //transition, meta na pewno nie 
			int index = abyssTransitionsID.lastIndexOf(startNode.getID());
			mainSourceID = snoopyWriterTransitions.get(index).snoopyStartingID;
			index = abyssPlacesID.lastIndexOf(endNode.getID());
			mainTargetID = snoopyWriterPlaces.get(index).snoopyStartingID;
		}

		//kolejność skąd dokąd łuk prowadzi została już okreslona w czasie określania ścieżki (na bazie isInInterface)
		for(int i=0; i<pathSize; i++) {
			PathPackage happy = subnetsPath.get(i);
			Node sourceNode = happy.source.getParentNode();
			Node targetNode = happy.target.getParentNode();
			
			if(sourceNode instanceof Place) {
				int index = abyssPlacesID.lastIndexOf(sourceNode.getID()); //który to był
				int startLocIndex = sourceNode.getElementLocations().indexOf(happy.source);
				SnoopyWriterPlace source = snoopyWriterPlaces.get(index);
				subSourceIDs.add(source.grParents.get(startLocIndex));
				subSourcePoint.add(source.grParentsLocation.get(startLocIndex));
			} else if(sourceNode instanceof Transition) {
				int index = abyssTransitionsID.lastIndexOf(sourceNode.getID()); //który to był
				int startLocIndex = sourceNode.getElementLocations().indexOf(happy.source);
				SnoopyWriterTransition source = snoopyWriterTransitions.get(index);
				subSourceIDs.add(source.grParents.get(startLocIndex));
				subSourcePoint.add(source.grParentsLocation.get(startLocIndex));
			} else if(sourceNode instanceof MetaNode) {
				MetaNode sMetaNode = (MetaNode) sourceNode;
				if(sMetaNode.getMetaType() == MetaType.SUBNETPLACE) {
					int index = abyssCoarsePlacesID.lastIndexOf(sourceNode.getID()); //który to był
					SnoopyWriterCoarse source = snoopyWriterCoarsePlaces.get(index);
					subSourceIDs.add(source.grParents.get(0));
					subSourcePoint.add(source.grParentsLocation.get(0));
				} else {
					if(sMetaNode.getMetaType() != MetaType.SUBNETTRANS) {
						GUIManager.getDefaultGUIManager().log("Critical error while saving arc: "+arcus.toString()
								+" Wrong metanode:"+sMetaNode.getMetaType(), "error", true);
						return IDbackup+1;
					}
					int index = abyssCoarseTransitionsID.lastIndexOf(sourceNode.getID()); //który to był
					SnoopyWriterCoarse source = snoopyWriterCoarseTransitions.get(index);
					subSourceIDs.add(source.grParents.get(0));
					subSourcePoint.add(source.grParentsLocation.get(0));
				}
			} else {
				GUIManager.getDefaultGUIManager().log("Critical error while saving arc: "+arcus.toString()+" Unrecognized node:"+sourceNode, "error", true);
				return IDbackup+1;
			}
			
			if(targetNode instanceof Place) {
				int index = abyssPlacesID.lastIndexOf(targetNode.getID()); //który to był
				int endLocIndex = targetNode.getElementLocations().indexOf(happy.target);
				SnoopyWriterPlace source = snoopyWriterPlaces.get(index);
				subTargetIDs.add(source.grParents.get(endLocIndex));
				subTargetPoint.add(source.grParentsLocation.get(endLocIndex));
			} else if(targetNode instanceof Transition) {
				int index = abyssTransitionsID.lastIndexOf(targetNode.getID()); //który to był
				int endLocIndex = targetNode.getElementLocations().indexOf(happy.target);
				SnoopyWriterTransition source = snoopyWriterTransitions.get(index);
				subTargetIDs.add(source.grParents.get(endLocIndex));
				subTargetPoint.add(source.grParentsLocation.get(endLocIndex));
			} else if(targetNode instanceof MetaNode) {
				MetaNode sMetaNode = (MetaNode) targetNode;
				if(sMetaNode.getMetaType() == MetaType.SUBNETPLACE) {
					int index = abyssCoarsePlacesID.lastIndexOf(targetNode.getID()); //który to był
					SnoopyWriterCoarse source = snoopyWriterCoarsePlaces.get(index);
					subTargetIDs.add(source.grParents.get(0));
					subTargetPoint.add(source.grParentsLocation.get(0));
				} else {
					if(sMetaNode.getMetaType() != MetaType.SUBNETTRANS) {
						GUIManager.getDefaultGUIManager().log("Critical error while saving arc: "+arcus.toString()
								+" Wrong metanode:"+sMetaNode.getMetaType(), "error", true);
						return IDbackup+1;
					}
					int index = abyssCoarseTransitionsID.lastIndexOf(targetNode.getID()); //który to był
					SnoopyWriterCoarse source = snoopyWriterCoarseTransitions.get(index);
					subTargetIDs.add(source.grParents.get(0));
					subTargetPoint.add(source.grParentsLocation.get(0));
				}
			} else {
				GUIManager.getDefaultGUIManager().log("Critical error while saving arc: "+arcus.toString()+" Unrecognized node:"+sourceNode, "error", true);
				return IDbackup+1;
			}
			Point sPoint = subSourcePoint.get(subSourcePoint.size()-1);
			Point tPoint = subTargetPoint.get(subTargetPoint.size()-1);
			int halfX = (tPoint.x + sPoint.x) / 2;
			int halfY = (tPoint.y + sPoint.y) / 2;
			halfPoint.add(new Point(halfX, halfY));
		}
		
		
		write(bw, "      <edge source=\""+mainSourceID+"\" target=\""+mainTargetID+"\" id=\""+(grParents.get(0)-5)+
				"\" net=\""+(subnetsPath.get(0).subnet)+"\">");
		write(bw, "        <attribute name=\"Multiplicity\" id=\""+(grParents.get(0)-4)+"\" net=\""+(subnetsPath.get(0).subnet+1)+"\">");
		write(bw, "          <![CDATA["+arcus.getWeight()+"]]>");
		write(bw, "          <graphics count=\""+pathSize+"\">");
		int xOff = 20;
		//P/T -> metanode
		for(int i=0; i<pathSize; i++) {
			int id = -1;
			if(i==0)
				id = grParents.get(i)-3;
			else
				id = grParents.get(i)+1;
			
			write(bw, "            <graphic xoff=\""+xOff+".00\""
					+ " x=\""+(halfPoint.get(i).x+xOff)+".00\""
					+ " y=\""+(halfPoint.get(i).y)+".00\" id=\""+id+"\" net=\""+(subnetsPath.get(i).subnet+1)+"\""
					+ " show=\"1\" grparent=\""+grParents.get(i)+"\" state=\"1\""
					+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
		}
		
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		write(bw, "        <attribute name=\"Comment\" id=\""+(grParents.get(0)-2)+"\" net=\""+(subnetsPath.get(0).subnet+1)+"\">");
		write(bw, "          <![CDATA["+removeEnters(arcus.getComment())+"]]>");
		write(bw, "          <graphics count=\""+pathSize+"\">");
		xOff = 40;
		for(int i=0; i<pathSize; i++) {
			int id = -1;
			if(i==0)
				id = grParents.get(i)-1;
			else
				id = grParents.get(i)+2;
			
			write(bw, "            <graphic xoff=\""+xOff+".00\""
					+ " x=\""+(halfPoint.get(i).x+xOff)+".00\""
					+ " y=\""+(halfPoint.get(i).y)+".00\" id=\""+id+"\" net=\""+(subnetsPath.get(i).subnet+1)+"\""
					+ " show=\"1\" grparent=\""+grParents.get(i)+"\" state=\"1\""
					+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
		}

		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		write(bw, "        <graphics count=\"2\">");
		
		//TUTAJ WCHODZĄ REALNE X,Y I ID PORTALI:
		//P/T -> metanode
		for(int i=0; i<pathSize; i++) {
			int state = 8;
			if(i == pathSize-1)
				state = 4;
			
			write(bw, "          <graphic id=\""+grParents.get(i)+"\" net=\""+(subnetsPath.get(i).subnet+1)+"\""
					+ " source=\""+subSourceIDs.get(i)+"\""
					+ " target=\""+subTargetIDs.get(i)+"\" state=\""+state+"\" show=\"1\""
					+ " pen=\"0,0,0\" brush=\"0,0,0\" edge_designtype=\"3\">");			
			write(bw, "            <points count=\"2\">"); //bez łamańców
			write(bw, "              <point x=\""+(subSourcePoint.get(i).x)+".00\" y=\""+(subSourcePoint.get(i).y)+".00\"/>");
			write(bw, "              <point x=\""+(subTargetPoint.get(i).x)+".00\" y=\""+(subTargetPoint.get(i).y)+".00\"/>");
			write(bw, "            </points>");
			write(bw, "          </graphic>");
		}
		write(bw, "        </graphics>");
		write(bw, "      </edge>");

		for(int i=1; i<pathSize; i++) {
			metaArcs.remove(subnetsPath.get(i).arc);
		}
		
		return IDbackup+1;
	}
	
	/**
	 * Metoda odtawarza całą ściężkę zagnieżdżeń podsieci zaczynając od elementu w dajgłębszej sieci.
	 * @param arcStartElLocation ElementLocation - element interfejsu
	 * @param arcEndElLocation ElementLocation - element do pary
	 * @param arcus Arc - łukiem wiekuiście związani...
	 * @param isInInterface boolean - true, jeśli arcStartElLocation jest interfejsem wejściowym ( łuki z niego wychodzą )
	 * @return ArrayList[PathPackage] - odwtworzona ścieżka, I obiekt to arcStartElLocation, arcEndElLocation oraz nr ich podsieci
	 */
	private ArrayList<PathPackage> recreateSubnetsPath(ElementLocation arcStartElLocation, ElementLocation arcEndElLocation, 
			Arc arcus, boolean isInInterface) {
		
		ArrayList<PathPackage> result = new ArrayList<PathPackage>();
		PathPackage first = new PathPackage();
		first.subnet = arcStartElLocation.getSheetID();
		first.arc = arcus;
		if(isInInterface) {
			first.source = arcStartElLocation;
			first.target = arcEndElLocation;
		} else {
			first.source = arcEndElLocation;
			first.target = arcStartElLocation;
		}
		result.add(first);
		
		ArrayList<Integer> path = new ArrayList<Integer>();
		boolean search = true;
		Node startNode = arcStartElLocation.getParentNode();
		
		path.add(arcStartElLocation.getSheetID());
		while(search) {
			int pathSize = path.size();
			int metaSheetToFind = path.get(pathSize-1);
			
			boolean findAnything = false;
			for(MetaNode meta : metanodes) {
				if(findAnything)
					break;
				
				if(meta.getRepresentedSheetID() == metaSheetToFind) {
					if(isInInterface) {
						for(Arc arc : meta.getElementLocations().get(0).accessMetaInArcs() ) {
							if(arc.getStartNode().equals(startNode)) {
								if(!path.contains(arc.getStartLocation().getSheetID())) {
									PathPackage dp = new PathPackage();
									dp.subnet = arc.getStartLocation().getSheetID();
									dp.source = arc.getStartLocation();
									dp.target = meta.getElementLocations().get(0);
									dp.arc = arc;
									result.add(dp);
									
									path.add(arc.getStartLocation().getSheetID());
									findAnything = true;
									continue;
								}
							}
								
						}
					} else {
						for(Arc arc : meta.getElementLocations().get(0).accessMetaOutArcs() ) {
							if(arc.getEndNode().equals(startNode)) {
								if(!path.contains(arc.getEndLocation().getSheetID())) {
									PathPackage dp = new PathPackage();
									dp.subnet = arc.getEndLocation().getSheetID();
									dp.source = meta.getElementLocations().get(0);
									dp.target = arc.getEndLocation();
									dp.arc = arc;
									result.add(dp);
									
									path.add(arc.getEndLocation().getSheetID());
									findAnything = true;
									continue;	
								}
							}
								
						}
					}
				}
			}
			if(!findAnything)
				return result;
		}
		return result;
	}

	/**
	 * Sprawdza, czy nie mamy doczynienia z wielopoziomową siecią.
	 * @param metanode MetaNode - metawęzeł z podsieci zaraz 'pod' normalnym łukiem
	 * @param startN Node - interfejs
	 * @param isInInterface boolean - true, jeśli interfejs wejściowy
	 * @return boolean - true, jeśli wielopoziomowe połączenie
	 */
	private boolean weNeedToGoDeeper(MetaNode metanode, Node startN, boolean isInInterface) {
		//ElementLocation metaEL = metanode.getElementLocations().get(0);
		int metaSheet = metanode.getElementLocations().get(0).getSheetID();
		if(metaSheet == 0)
			return false;
		
		for(MetaNode meta : metanodes) {
			if(meta.getRepresentedSheetID() == metaSheet) {
				if(isInInterface) {
					for(Arc arc : meta.getElementLocations().get(0).accessMetaInArcs() ) {
						if(arc.getStartNode().equals(startN))
							return true;
					}
				} else {
					for(Arc arc : meta.getElementLocations().get(0).accessMetaOutArcs() ) {
						if(arc.getEndNode().equals(startN))
							return true;
					}
				}
			}
		}
		return false;
	}
	
	//********************************************************************************************************************
	//********************************************************************************************************************
	//********************************************************************************************************************


	/**
	 * Stopień odjechania poniższej metody przewyższa normy niczym Czarnobyl w kwestii promieniowania.
	 * A skoro już o tym mowa...<br>
	 * -Вот это от усталости, это от нервного напряжения, а это от депрессии...
	 * -Спасибо, доктор, спасибо... А у вас, кроме водки, ничего нет?
	 * 
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param currentActiveID int - od tego ID zaczynamy dodawać łuki
	 */
	public void addArcsToFile(BufferedWriter bw, int currentActiveID) {
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
						String comment = removeEnters(a.getComment());
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
	
	class PathPackage {
		int subnet = -1;
		public ElementLocation source = null;
		public ElementLocation target = null;
		public Arc arc = null;
	}
	
	private String removeEnters(String txt) {
		txt = txt.replace("\n", " ");
		return txt;
	}
}
