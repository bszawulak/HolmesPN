package holmes.files.io;

import java.awt.Point;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import holmes.darkgui.GUIManager;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNet;
import holmes.petrinet.data.SSAplacesVector;
import holmes.petrinet.data.StatePlacesVector;
import holmes.petrinet.data.SPNdataVector;
import holmes.petrinet.data.SPNtransitionData;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.functions.FunctionContainer;
import holmes.utilities.Tools;
import holmes.varia.Check;

/**
 * Klasa odpowiedzialna za zapis projektu do pliku. Zapisuje między innymi: dane sieci (miejsca, tranzycje
 * oraz łuki), inwarianty, zbiory MCT i inne.
 * 
 * @author MR
 *
 */
public class ProjectWriter {
	private PetriNet projectCore;
	private ArrayList<Place> places ;
	private ArrayList<Transition> transitions;
	private ArrayList<MetaNode> metaNodes;
	private ArrayList<Arc> arcs;
	private ArrayList<ArrayList<Integer>> t_invariantsMatrix;
	private ArrayList<String> t_invariantsNames;
	private ArrayList<ArrayList<Integer>> p_invariantsMatrix;
	private ArrayList<String> p_invariantsNames;
	private ArrayList<ArrayList<Transition>> mctData;
	private ArrayList<String> mctNames;
	private ArrayList<StatePlacesVector> statesMatrix;
	private ArrayList<SPNdataVector> firingRatesMatrix;
	private ArrayList<SSAplacesVector> ssaMatrix;
	
	private String newline = "\n";
	
	/**
	 * Konstruktor obiektu klasy ProjectWriter.
	 */
	public ProjectWriter() {
		projectCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		places = projectCore.getPlaces();
		transitions = projectCore.getTransitions();
		metaNodes = projectCore.getMetaNodes();
		arcs = projectCore.getArcs();
		
		t_invariantsMatrix = projectCore.getT_InvMatrix();
		t_invariantsNames = projectCore.accessT_InvDescriptions();
		p_invariantsMatrix = projectCore.getP_InvMatrix();
		p_invariantsNames = projectCore.accessP_InvDescriptions();
		mctData = projectCore.getMCTMatrix();
		mctNames = projectCore.accessMCTnames();
		statesMatrix = projectCore.accessStatesManager().accessStateMatrix();
		firingRatesMatrix = projectCore.accessFiringRatesManager().accessSPNmatrix();
		ssaMatrix = projectCore.accessSSAmanager().accessSSAmatrix();
	}
	
	/**
	 * Główna metoda odpowiedzialna za zapis pliku projektu.
	 * @return int - kod błędu, 0 - wszystko ok
	 */
	public boolean writeProject(String filepath) {
		for(Transition trans : transitions) 
			trans.checkFunctions(arcs, places);
		
		try {
			//BufferedWriter bw = new BufferedWriter(new FileWriter("tmp//test.apf"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
			String projName = projectCore.getName();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			bw.write("Project name: "+projName+newline);
			bw.write("Date: "+dateFormat.format(date)+newline);
			
			bw.write("<Project blocks>"+newline);
			bw.write("  <Subnets>"+newline);
			bw.write("  <InvariantsData>"+newline);
			bw.write("  <PlaceInvData>"+newline);
			bw.write("  <MCT data>"+newline);
			bw.write("  <StatesMatrix>"+newline);
			bw.write("  <FunctionsData>"+newline);
			bw.write("  <FiringRatesData>"+newline);
			bw.write("  <SSAmatrix>"+newline);
			bw.write("</Project blocks>"+newline);
			
			bw.write("<Net data>"+newline);
			bw.write("<ID generator state:"+IdGenerator.getCurrentValues()+">"+newline);
			boolean statusNet = saveNetwork(bw);
			bw.write("<Net data end>"+newline);
			
			bw.write("<Invariants data>"+newline);
			boolean statusInv = saveT_Invariants(bw);
			bw.write("<Invariants data end>"+newline);
			
			bw.write("<PlaceInv data>"+newline);
			boolean statusPInv = saveP_Invariants(bw);
			bw.write("<PlaceInv data end>"+newline);
			
			bw.write("<MCT data>"+newline);
			boolean statusMCT = saveMCT(bw);
			bw.write("<MCT data end>"+newline);
			
			bw.write("<States data>"+newline);
			boolean statusStates = saveStates(bw);
			bw.write("<States data end>"+newline);
			
			bw.write("<Firing rates data>"+newline);
			boolean statusFR = saveFiringRates(bw);
			bw.write("<Firing rates data end>"+newline);
			
			bw.write("<SSA vectors data>"+newline);
			boolean statusSSA = saveSSAvectors(bw);
			bw.write("<SSA vectors data end>"+newline);

			//TODO: log o statusach

			bw.close();
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Project data saved to: "+filepath, "text", true);
			return false;
		}
	}

	/**
	 * Metoda realizująca zapis danych o strukturze sieci do pliku projektu.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli wszystko się udało
	 */
	private boolean saveNetwork(BufferedWriter bw) {
		try {
			//ZAPIS MIEJSCA:
			int sp = 2;
			int placesNumber = places.size();
			bw.write(spaces(sp)+"<Places: "+placesNumber+">"+newline);
			for(int p=0; p<placesNumber; p++) {
				sp = 4;
				Place place = places.get(p);
				bw.write(spaces(sp)+"<Place: "+p+">"+newline);
				sp = 6;
				bw.write(spaces(sp)+"<Place gID:"+place.getID()+">"+newline); //gID
				bw.write(spaces(sp)+"<Place name:"+place.getName()+">"+newline);  //nazwa

				bw.write(spaces(sp)+"<Place comment:"+Tools.convertToCode(place.getComment())+">"+newline); //komentarz
				bw.write(spaces(sp)+"<Place tokens:"+place.getTokensNumber()+">"+newline); //tokeny

				bw.write(spaces(sp)+"<Place XTPN status:"+place.isXTPNplace()+">"+newline); //czy to miejsce XTPN?
				bw.write(spaces(sp)+"<Place XTPN gammaMode:"+place.isGammaModeActiveXTPN()+">"+newline); //czy gamma włączone
				bw.write(spaces(sp)+"<Place XTPN gammaVisible:"+place.isGammaRangeVisible()+">"+newline); //czy gamma widoczne
				bw.write(spaces(sp)+"<Place XTPN gammaMin:"+place.getGammaMin_xTPN()+">"+newline); //gamma minimum
				bw.write(spaces(sp)+"<Place XTPN gammaMax:"+place.getGammaMax_xTPN()+">"+newline); //gamma maximum
				bw.write(spaces(sp)+"<Place XTPN fractionSize:"+place.getFraction_xTPN()+">"+newline); //dokładność po przecinku

				bw.write(spaces(sp)+"<Place XTPN multiset"); //multisetK
				for(int token=0; token<place.accessMultiset().size(); token++) {
					bw.write(":"+place.accessMultiset().get(token)); //tokeny
				}
				bw.write(">"+newline); //tokeny

				bw.write(spaces(sp)+"<Place colored:"+place.isColored+">"+newline);
				bw.write(spaces(sp)+"<Place colors:"
						+place.getColorTokensNumber(0)+";"
						+place.getColorTokensNumber(1)+";"
						+place.getColorTokensNumber(2)+";"
						+place.getColorTokensNumber(3)+";"
						+place.getColorTokensNumber(4)+";"
						+place.getColorTokensNumber(5)+">"+newline);
				
				bw.write(spaces(sp)+"<Location data"+">"+newline);
				sp = 8;
				bw.write(spaces(sp)+"<Place portal status:"+place.isPortal()+">"+newline);

				int elLocations = place.getElementLocations().size();
				bw.write(spaces(sp)+"<Place locations:"+elLocations+">"+newline);
				for(int e=0; e<elLocations; e++) {
					sp = 10;
					ElementLocation eLoc = place.getElementLocations().get(e);
					int sheetId = eLoc.getSheetID();
					int pointX = eLoc.getPosition().x;
					int pointY = eLoc.getPosition().y;
					bw.write(spaces(sp)+"<Place location data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
					
					ElementLocation nameLoc = place.getTextsLocations(GUIManager.locationMoveType.NAME).get(e);
					sheetId = nameLoc.getSheetID();
					pointX = nameLoc.getPosition().x;
					pointY = nameLoc.getPosition().y;
					bw.write(spaces(sp)+"<Place name offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y

					ElementLocation gammaLoc = place.getTextsLocations(GUIManager.locationMoveType.GAMMA).get(e);
					sheetId = gammaLoc.getSheetID();
					pointX = gammaLoc.getPosition().x;
					pointY = gammaLoc.getPosition().y;
					bw.write(spaces(sp)+"<Place gamma offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
				}
				sp = 6;
				bw.write(spaces(sp)+"<Location data block end"+">"+newline);
				sp = 4;
				bw.write(spaces(sp)+"<EOP"+">"+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<Places data block end"+">"+newline);
			
			//ZAPIS TRANZYCJI:
			
			int transNumber = transitions.size();
			bw.write(spaces(sp)+"<Transitions: "+transNumber+">"+newline);
			for(int t=0; t<transNumber; t++) {
				sp = 4;
				Transition trans = transitions.get(t);
				bw.write(spaces(sp)+"<Transition: "+t+">"+newline);
				sp = 6;
				bw.write(spaces(sp)+"<Transition gID:"+trans.getID()+">"+newline); //gID
				bw.write(spaces(sp)+"<Transition type:"+trans.getTransType()+">"+newline); //typ
				bw.write(spaces(sp)+"<Transition name:"+trans.getName()+">"+newline);  //nazwa
				bw.write(spaces(sp)+"<Transition comment:"+Tools.convertToCode(trans.getComment())+">"+newline); //komentarz
				bw.write(spaces(sp)+"<Transition eft:"+trans.getEFT()+">"+newline); //TPN eft
				bw.write(spaces(sp)+"<Transition lft:"+trans.getLFT()+">"+newline); //TPN lft
				bw.write(spaces(sp)+"<Transition duration:"+trans.getDPNduration()+">"+newline); //DPN duration value
				bw.write(spaces(sp)+"<Transition TPN status:"+trans.getTPNstatus()+">"+newline); //is TPN active?
				bw.write(spaces(sp)+"<Transition DPN status:"+trans.getDPNstatus()+">"+newline); //is DPN active?
				bw.write(spaces(sp)+"<Transition function flag:"+trans.isFunctional()+">"+newline); //is functional?

				bw.write(spaces(sp)+"<Transition XTPN status:"+trans.isXTPNtransition()+">"+newline); //czy XTPN?
				bw.write(spaces(sp)+"<Transition XTPN alphaMode:"+trans.isAlphaActiveXTPN()+">"+newline); //czy alpha włączone
				bw.write(spaces(sp)+"<Transition XTPN alphaVisible:"+trans.isAlphaRangeVisible()+">"+newline); //czy alpha widoczne
				bw.write(spaces(sp)+"<Transition XTPN alphaMin:"+trans.getAlphaMin_xTPN()+">"+newline); //alpha minimum
				bw.write(spaces(sp)+"<Transition XTPN alphaMax:"+trans.getAlphaMax_xTPN()+">"+newline); //alpha maximum
				bw.write(spaces(sp)+"<Transition XTPN betaMode:"+trans.isBetaActiveXTPN()+">"+newline); //czy beta włączone
				bw.write(spaces(sp)+"<Transition XTPN betaVisible:"+trans.isBetaRangeVisible()+">"+newline); //czy beta widoczne
				bw.write(spaces(sp)+"<Transition XTPN betaMin:"+trans.getBetaMin_xTPN()+">"+newline); //beta minimum
				bw.write(spaces(sp)+"<Transition XTPN betaMax:"+trans.getBetaMax_xTPN()+">"+newline); //beta maximum
				bw.write(spaces(sp)+"<Transition XTPN tauVisible:"+trans.isTauTimerVisible()+">"+newline); //czy beta widoczne
				bw.write(spaces(sp)+"<Transition XTPN massAction:"+trans.isMassActionKineticsActiveXTPN()+">"+newline); //mass-action
				bw.write(spaces(sp)+"<Transition XTPN fractionSize:"+trans.getFraction_xTPN()+">"+newline); //dokładność po przecinku
				
				bw.write(spaces(sp)+"<Transition colored:"+trans.isColored()+">"+newline); //is colored?
				bw.write(spaces(sp)+"<Transition colors threshold:"
						+trans.getRequiredColoredTokens(0)+";"
						+trans.getRequiredColoredTokens(1)+";"
						+trans.getRequiredColoredTokens(2)+";"
						+trans.getRequiredColoredTokens(3)+";"
						+trans.getRequiredColoredTokens(4)+";"
						+trans.getRequiredColoredTokens(5)+">"+newline);
				
				bw.write(spaces(sp)+"<Location data"+">"+newline);
				sp = 8;
				bw.write(spaces(sp)+"<Transition portal status:"+trans.isPortal()+">"+newline);
				int elLocations = trans.getElementLocations().size();
				bw.write(spaces(sp)+"<Transition locations:"+elLocations+">"+newline);
				for(int e=0; e<elLocations; e++) {
					sp = 10;
					ElementLocation eLoc = trans.getElementLocations().get(e);
					int sheetId = eLoc.getSheetID();
					int pointX = eLoc.getPosition().x;
					int pointY = eLoc.getPosition().y;
					bw.write(spaces(sp)+"<Transition location data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
					
					ElementLocation nameLoc = trans.getTextsLocations(GUIManager.locationMoveType.NAME).get(e);
					sheetId = nameLoc.getSheetID();
					pointX = nameLoc.getPosition().x;
					pointY = nameLoc.getPosition().y;
					bw.write(spaces(sp)+"<Transition name offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y

					ElementLocation alphaLoc = trans.getTextsLocations(GUIManager.locationMoveType.ALPHA).get(e);
					sheetId = alphaLoc.getSheetID();
					pointX = alphaLoc.getPosition().x;
					pointY = alphaLoc.getPosition().y;
					bw.write(spaces(sp)+"<Transition alpha offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y

					ElementLocation betaLoc = trans.getTextsLocations(GUIManager.locationMoveType.BETA).get(e);
					sheetId = betaLoc.getSheetID();
					pointX = betaLoc.getPosition().x;
					pointY = betaLoc.getPosition().y;
					bw.write(spaces(sp)+"<Transition beta offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
				}

				sp = 6;
				bw.write(spaces(sp)+"<Location data block end"+">"+newline);
				sp = 4;
				bw.write(spaces(sp)+"<EOT"+">"+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<Transitions data block end"+">"+newline);
			
			//ZAPIS META-WĘZŁÓW:
			
			int metaNumber = metaNodes.size();
			bw.write(spaces(sp)+"<MetaNodes: "+metaNumber+">"+newline);
			for(int t=0; t<metaNumber; t++) {
				sp = 4;
				MetaNode metanode = metaNodes.get(t);
				bw.write(spaces(sp)+"<MetaNode: "+t+">"+newline);
				sp = 6;
				bw.write(spaces(sp)+"<MetaNode gID:"+metanode.getID()+">"+newline); //gID
				bw.write(spaces(sp)+"<MetaNode type:"+metanode.getMetaType()+">"+newline); //typ
				bw.write(spaces(sp)+"<MetaNode name:"+metanode.getName()+">"+newline);  //nazwa
				bw.write(spaces(sp)+"<MetaNode comment:"+Tools.convertToCode(metanode.getComment())+">"+newline); //komentarz
				bw.write(spaces(sp)+"<MetaNode representedSheet:"+metanode.getRepresentedSheetID()+">"+newline); //sheet którego dotyczy
				bw.write(spaces(sp)+"<Location data>"+newline);
				sp = 8;
				int elLocations = metanode.getElementLocations().size();
				bw.write(spaces(sp)+"<MetaNode locations:"+elLocations+">"+newline);
				for(int e=0; e<elLocations; e++) {
					sp = 10;
					ElementLocation eLoc = metanode.getElementLocations().get(e);
					int sheetId = eLoc.getSheetID();
					int pointX = eLoc.getPosition().x;
					int pointY = eLoc.getPosition().y;
					bw.write(spaces(sp)+"<MetaNode location data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
					
					ElementLocation nameLoc = metanode.getTextsLocations(GUIManager.locationMoveType.NAME).get(e);
					sheetId = nameLoc.getSheetID();
					pointX = nameLoc.getPosition().x;
					pointY = nameLoc.getPosition().y;
					bw.write(spaces(sp)+"<MetaNode name offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
				}

				sp = 6;
				bw.write(spaces(sp)+"<Location data block end"+">"+newline);
				sp = 4;
				bw.write(spaces(sp)+"<EOT"+">"+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<MetaNodes data block end"+">"+newline);
			
			//ŁUKI
			int savedArcs = 0;
			bw.write(spaces(sp)+"<Arcs data block"+">"+newline);
			sp=4;
			for(int p=0; p<placesNumber; p++) {
				Place place = places.get(p);
				int elLocations = place.getElementLocations().size();
				for(int e=0; e<elLocations; e++) { //wszystkie lokalizacje miejsca
					ElementLocation eLoc = place.getElementLocations().get(e);
					ArrayList<Arc> tmp_outgoingArcs = new ArrayList<Arc>(eLoc.getOutArcs());
					ArrayList<Arc> metaOutArcs = eLoc.accessMetaOutArcs();
					tmp_outgoingArcs.addAll(metaOutArcs);
					int arcsNumber = tmp_outgoingArcs.size();
					for(int a=0; a<arcsNumber; a++) { //wszystkie łuki wyjściowe
						Arc arc = tmp_outgoingArcs.get(a);
						String arcType = ""+arc.getArcType();
						Node endNode = arc.getEndNode();
						
						if(endNode instanceof Transition) {
							String startLoc = "P"+p+"("+e+")";
							Transition endTransition = (Transition)endNode;
							int endNodeIndex = transitions.indexOf(endTransition);
							ElementLocation endNodeLocation = arc.getEndLocation();
							int endNodeLocationIndex = -1;
							for(int e2=0; e2<endTransition.getElementLocations().size(); e2++) {
								if(endNodeLocation == endTransition.getElementLocations().get(e2)) {
									endNodeLocationIndex = e2;
									break;
								}
							}
							
							String endLoc = "T"+endNodeIndex+"("+endNodeLocationIndex+")";
							int weight = arc.getWeight();

							String brokenLine = "";
							if(arc.accessBreaks().size() > 0) {
								brokenLine += ";";
								for(Point point : arc.accessBreaks()) {
									brokenLine += point.x;
									brokenLine += "-";
									brokenLine += point.y;
									brokenLine += "x";
								}
								brokenLine = brokenLine.substring(0, brokenLine.length()-1);
							} else {
								brokenLine += ";99999-11111";
							}
							
							boolean isColored = false;
							if(arc.getArcType() == TypeOfArc.COLOR)
								isColored = true;
							String coloredWeights = ""+arc.getColorWeight(0)+":"+arc.getColorWeight(1)+":"+arc.getColorWeight(2)+":"
									+arc.getColorWeight(3)+":"+arc.getColorWeight(4)+":"+arc.getColorWeight(5);
							
							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+brokenLine+";"+isColored
									+";"+coloredWeights+newline);
							savedArcs++;
						} else if(endNode instanceof MetaNode) {
							String startLoc = "P"+p+"("+e+")";
							MetaNode endMetanode = (MetaNode)endNode;
							int endNodeIndex = metaNodes.indexOf(endMetanode);
							ElementLocation endNodeLocation = arc.getEndLocation();
							int endNodeLocationIndex = -1;
							for(int e2=0; e2<endMetanode.getElementLocations().size(); e2++) {
								if(endNodeLocation == endMetanode.getElementLocations().get(e2)) {
									endNodeLocationIndex = e2;
									break;
								}
							}
							
							String endLoc = "M"+endNodeIndex+"("+endNodeLocationIndex+")";
							int weight = arc.getWeight();

							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+newline);
							savedArcs++;
						} 
					}
				}	
			}

			for(int t=0; t<transNumber; t++) {
				Transition trans = transitions.get(t);
				int elLocations = trans.getElementLocations().size();
				for(int e=0; e<elLocations; e++) { //wszystkie lokalizacje tranzycji
					ElementLocation eLoc = trans.getElementLocations().get(e);
					ArrayList<Arc> tmp_outgoingArcs = new ArrayList<Arc>(eLoc.getOutArcs());
					ArrayList<Arc> metaOutArcs = eLoc.accessMetaOutArcs();
					tmp_outgoingArcs.addAll(metaOutArcs);
					int arcsNumber = tmp_outgoingArcs.size();
					for(int a=0; a<arcsNumber; a++) { //wszystkie łuki wyjściowe
						Arc arc = tmp_outgoingArcs.get(a);
						String arcType = ""+arc.getArcType();
						Node endNode = arc.getEndNode();
						if(endNode instanceof Place) {
							String startLoc = "T"+t+"("+e+")";
							Place endPlace = (Place)endNode;
							int endNodeIndex = places.indexOf(endPlace);
							ElementLocation endNodeLocation = arc.getEndLocation();
							int endNodeLocationIndex = -1;
							for(int e2=0; e2<endPlace.getElementLocations().size(); e2++) {
								if(endNodeLocation == endPlace.getElementLocations().get(e2)) {
									endNodeLocationIndex = e2;
									break;
								}
							}
							
							String endLoc = "P"+endNodeIndex+"("+endNodeLocationIndex+")";
							int weight = arc.getWeight();
							
							
							
							String brokenLine = "";
							if(arc.accessBreaks().size() > 0) {
								brokenLine += ";";
								for(Point point : arc.accessBreaks()) {
									brokenLine += point.x;
									brokenLine += "-";
									brokenLine += point.y;
									brokenLine += "x";
								}
								brokenLine = brokenLine.substring(0, brokenLine.length()-1);
							} else {
								brokenLine += ";99999-11111";
							}
							
							boolean isColored = false;
							if(arc.getArcType() == TypeOfArc.COLOR)
								isColored = true;
							String coloredWeights = ""+arc.getColorWeight(0)+":"+arc.getColorWeight(1)+":"+arc.getColorWeight(2)+":"
									+arc.getColorWeight(3)+":"+arc.getColorWeight(4)+":"+arc.getColorWeight(5);
							
							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+brokenLine+";"+isColored
									+";"+coloredWeights+newline);
							savedArcs++;
						} else if(endNode instanceof MetaNode) {
							String startLoc = "T"+t+"("+e+")";
							MetaNode endMetanode = (MetaNode)endNode;
							int endNodeIndex = metaNodes.indexOf(endMetanode);
							ElementLocation endNodeLocation = arc.getEndLocation();
							int endNodeLocationIndex = -1;
							for(int e2=0; e2<endMetanode.getElementLocations().size(); e2++) {
								if(endNodeLocation == endMetanode.getElementLocations().get(e2)) {
									endNodeLocationIndex = e2;
									break;
								}
							}
							
							String endLoc = "M"+endNodeIndex+"("+endNodeLocationIndex+")";
							int weight = arc.getWeight();
							
							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+newline);
							savedArcs++;
						}
					}
					
				}
			}

			for(int m=0; m<metaNumber; m++) {
				MetaNode metaNode = metaNodes.get(m);
				int elLocations = metaNode.getElementLocations().size();
				for(int e=0; e<elLocations; e++) { //wszystkie lokalizacje meta-węzła
					ElementLocation eLoc = metaNode.getElementLocations().get(e);
					ArrayList<Arc> tmp_outgoingArcs = new ArrayList<Arc>(eLoc.getOutArcs());
					ArrayList<Arc> metaOutArcs = eLoc.accessMetaOutArcs();
					tmp_outgoingArcs.addAll(metaOutArcs);
					int arcsNumber = tmp_outgoingArcs.size();
					for(int a=0; a<arcsNumber; a++) { //wszystkie łuki wyjściowe
						Arc arc = tmp_outgoingArcs.get(a);
						String arcType = ""+arc.getArcType();
						Node endNode = arc.getEndNode();
						if(endNode instanceof Place) {
							String startLoc = "M"+m+"("+e+")";
							Place endPlace = (Place)endNode;
							int endNodeIndex = places.indexOf(endPlace);
							ElementLocation endNodeLocation = arc.getEndLocation();
							int endNodeLocationIndex = -1;
							for(int e2=0; e2<endPlace.getElementLocations().size(); e2++) {
								if(endNodeLocation == endPlace.getElementLocations().get(e2)) {
									endNodeLocationIndex = e2;
									break;
								}
							}
							
							String endLoc = "P"+endNodeIndex+"("+endNodeLocationIndex+")";
							int weight = arc.getWeight();
							
							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+newline);
							savedArcs++;
						} else if(endNode instanceof Transition) {
							String startLoc = "M"+m+"("+e+")";
							Transition endTransition = (Transition)endNode;
							int endNodeIndex = transitions.indexOf(endTransition);
							ElementLocation endNodeLocation = arc.getEndLocation();
							int endNodeLocationIndex = -1;
							for(int e2=0; e2<endTransition.getElementLocations().size(); e2++) {
								if(endNodeLocation == endTransition.getElementLocations().get(e2)) {
									endNodeLocationIndex = e2;
									break;
								}
							}
							
							String endLoc = "T"+endNodeIndex+"("+endNodeLocationIndex+")";
							int weight = arc.getWeight();
							
							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+newline);
							savedArcs++;
						} 
					}
				}
			}
			sp = 2;
			
			bw.write(spaces(sp)+"<Arcs data block end"+">"+newline);
			
			ArrayList<Integer> arcClasses = Check.getArcClassCount();
			int readArcs = arcClasses.get(1) / 2;
			//readArcs = 0;
			int totalArcs = arcs.size()-readArcs;
			if(savedArcs != totalArcs) {
				GUIManager.getDefaultGUIManager().log("Error: saved "+savedArcs+" out of total "+totalArcs+" arcs.", "error", true);
			}
			
			bw.write(spaces(sp)+"<Functions data block"+">"+newline);
			sp = 4;
			for(int t=0; t<transitions.size(); t++) {
				Transition transition = transitions.get(t);
				ArrayList<FunctionContainer> fVector = transition.accessFunctionsList();
				for(FunctionContainer fc : fVector) {
					if(fc.simpleExpression.length() == 0)
						continue;
					
					bw.write(spaces(sp)+"<T"+t+";"+fc.fID+";"+fc.simpleExpression+";"+fc.correct+";"+fc.enabled+">"+newline);
				}
			}
			
			sp = 2;
			bw.write(spaces(sp)+"<Functions data block end"+">"+newline);

			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving net data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
		
	}
	
	/**
	 * Metoda służąca do zapisania w pliku projektu t-inwariantów (CSV) oraz ich nazw.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli wszystko dobrze poszło
	 */
	private boolean saveT_Invariants(BufferedWriter bw) {
		try {
			if(t_invariantsMatrix == null) {
				bw.write(spaces(2)+"<Invariants: 0>"+newline);
				bw.write(spaces(2)+"<EOI>"+newline);
				return false;	
			}
			
			int sp = 2;
			int invNumber = t_invariantsMatrix.size();
			
			if(invNumber == 0) {
				bw.write(spaces(2)+"<Invariants: 0>"+newline);
				bw.write(spaces(2)+"<EOI>"+newline);
				return false;	
			}
	
			bw.write(spaces(sp)+"<Invariants: "+invNumber+">"+newline);
			int invSize = t_invariantsMatrix.get(0).size();
			for(int i=0; i<invNumber; i++) {
				sp = 4;
				ArrayList<Integer> invariant = t_invariantsMatrix.get(i);
				String line = ""+i+";";
				
				for(int it=0; it<invSize; it++) {
					line += invariant.get(it)+";";
				}
				line = line.substring(0, line.length()-1); //usun ostatni ';'
				bw.write(spaces(sp)+line+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<EOI>"+newline);
			
			bw.write(spaces(sp)+"<Invariants names>"+newline);
			for(int i=0; i<invNumber; i++) {
				sp = 4;
				bw.write(spaces(sp)+Tools.convertToCode(t_invariantsNames.get(i))+newline);
			}
			bw.write(spaces(2)+"<EOIN>"+newline);
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving t-invariants data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda służąca do zapisania w pliku projektu p-inwariantów (CSV) oraz ich nazw.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli wszystko dobrze poszło
	 */
	private boolean saveP_Invariants(BufferedWriter bw) {
		try {
			if(p_invariantsMatrix == null) {
				bw.write(spaces(2)+"<PInvariants: 0>"+newline);
				bw.write(spaces(2)+"<EOPI>"+newline);
				return false;	
			}
			
			int sp = 2;
			int invNumber = p_invariantsMatrix.size();
			
			if(invNumber == 0) {
				bw.write(spaces(2)+"<PInvariants: 0>"+newline);
				bw.write(spaces(2)+"<EOPI>"+newline);
				return false;	
			}
	
			bw.write(spaces(sp)+"<PInvariants: "+invNumber+">"+newline);
			int invSize = p_invariantsMatrix.get(0).size();
			for(int i=0; i<invNumber; i++) {
				sp = 4;
				ArrayList<Integer> invariant = p_invariantsMatrix.get(i);
				String line = ""+i+";";
				
				for(int it=0; it<invSize; it++) {
					line += invariant.get(it)+";";
				}
				line = line.substring(0, line.length()-1); //usun ostatni ';'
				bw.write(spaces(sp)+line+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<EOPI>"+newline);
			
			bw.write(spaces(sp)+"<PInvariants names>"+newline);
			for(int i=0; i<invNumber; i++) {
				sp = 4;
				bw.write(spaces(sp)+Tools.convertToCode(p_invariantsNames.get(i))+newline);
			}
			bw.write(spaces(2)+"<EOPIN>"+newline);
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving p-invariants data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda służąca do zapisania w pliku projektu zbiorów MCT oraz ich nazw.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli wszystko dobrze poszło
	 */
	private boolean saveMCT(BufferedWriter bw) {
		try {
			if(mctData == null) {
				bw.write(spaces(2)+"<MCT: 0>"+newline);
				bw.write(spaces(2)+"<EOM>"+newline);
				return false;	
			}
			
			int sp = 2;
			int mctNumber = mctData.size();
			
			if(mctNumber == 0) {
				bw.write(spaces(2)+"<MCT: 0>"+newline);
				bw.write(spaces(2)+"<EOM>"+newline);
				return false;	
			}
	
			bw.write(spaces(sp)+"<MCT: "+mctNumber+">"+newline);
			for(int m=0; m<mctNumber; m++) {
				sp = 4;
				ArrayList<Transition> mct =  mctData.get(m);
				if(mct.size() == 0) {
					bw.write(";"+newline);
					continue;
				}
				
				String mctLine = "";
				for(Transition trans : mct) {
					mctLine += transitions.indexOf(trans) + ";";
				}
				mctLine = mctLine.substring(0, mctLine.length()-1); //usun ostatni ';'
				bw.write(spaces(sp)+mctLine+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<EOM>"+newline);
			bw.write(spaces(sp)+"<MCT names>"+newline);
			for(int i=0; i<mctNumber; i++) {
				sp = 4;
				bw.write(spaces(sp)+Tools.convertToCode(mctNames.get(i))+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<EOMn>"+newline);
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving MCT data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Sekcja odpowiedzialna za zapis tablicy stanów sieci.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli operacja się powiodła a pacjent nie zmarł.
	 */
	private boolean saveStates(BufferedWriter bw) {
		try {
			int sp = 2;
			int statesNumber = statesMatrix.size();
	
			if(places.size() > 0) { 
				bw.write(spaces(sp)+"<States: "+statesNumber+">"+newline);
				for(int s=0; s<statesNumber; s++) {
					sp = 4;
					StatePlacesVector sVector = statesMatrix.get(s);
					String stateLine = "";
					for(Double value : sVector.accessVector()) {
						stateLine += value + ";";
					}
					stateLine = stateLine.substring(0, stateLine.length()-1); //usun ostatni ';'
					bw.write(spaces(sp)+stateLine+newline);
					
					String type = sVector.getStateType();
					bw.write(spaces(sp)+type+";"+newline);
					bw.write(spaces(sp)+Tools.convertToCode(sVector.getDescription())+newline);
				}
			} else {
				bw.write(spaces(sp)+"<States: 0>"+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<EOSt>"+newline);
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving states data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda zapisuje tablicę wektorów firing rates tranzycji.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli wszystko się udało
	 */
	private boolean saveFiringRates(BufferedWriter bw) {
		try {
			int sp = 2;
			int frNumber = firingRatesMatrix.size();
	
			if(transitions.size()>0) {
				bw.write(spaces(sp)+"<FRvectors: "+frNumber+">"+newline);
				for(int fr=0; fr<frNumber; fr++) {
					sp = 4;
					SPNdataVector frVector = firingRatesMatrix.get(fr);
					/*
					String frLine = "";
					String stochTypeLine = "";
					for(SPNdataContainer frc : frVector.accessVector()) {
						frLine += frc.ST_function + ";"; //TODO: moduł wektora zapisu
						stochTypeLine += frc.sType + ";";
					}
					frLine = frLine.substring(0, frLine.length()-1); //usun ostatni ';'
					bw.write(spaces(sp)+frLine+newline);
					
					stochTypeLine = stochTypeLine.substring(0, stochTypeLine.length()-1); //usun ostatni ';'
					bw.write(spaces(sp)+stochTypeLine+newline);
					*/
					String dataLine = "version101:";
					for(SPNtransitionData frc : frVector.accessVector()) {
						dataLine += (frc.returnSaveVector()+";");
					}
					dataLine = dataLine.substring(0, dataLine.length()-1); //usun ostatni ';'
					bw.write(spaces(sp)+dataLine+newline);
					
					bw.write(spaces(sp)+frVector.getSPNtype()+";"+newline);
					bw.write(spaces(sp)+Tools.convertToCode(frVector.getDescription())+newline);
				}
			} else {
				bw.write(spaces(sp)+"<FRvectors: 0>"+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<EOFRv>"+newline);		
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving firing rates data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Sekcja odpowiedzialna za zapis tablicy wektorów danych SSA.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli operacja się powiodła a pacjent nie zmarł.
	 */
	private boolean saveSSAvectors(BufferedWriter bw) {
		try {
			int sp = 2;
			int ssaNumber = ssaMatrix.size();
	
			if(places.size() > 0) {
				bw.write(spaces(sp)+"<SSA vectors: "+ssaNumber+">"+newline);
				for(int s=0; s<ssaNumber; s++) {
					sp = 4;
					SSAplacesVector sVector = ssaMatrix.get(s);
					String stateLine = "";
					for(Double value : sVector.accessVector()) {
						stateLine += value + ";";
					}
					stateLine = stateLine.substring(0, stateLine.length()-1); //usun ostatni ';'
					bw.write(spaces(sp)+stateLine+newline);
					
					String ssaType = sVector.getType().toString();
					double ssaVolume = sVector.getVolume();
					bw.write(spaces(sp)+ssaType+";"+ssaVolume+newline);
					bw.write(spaces(sp)+Tools.convertToCode(sVector.getDescription())+newline);
				}
			} else {
				bw.write(spaces(sp)+"<SSA vectors: 0>"+newline);
			}
			
			sp = 2;
			bw.write(spaces(sp)+"<EOSSA>"+newline);
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving SSA data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
	}
	
	/**
	 * Metoda zwraca łańcuch znaków o zadanej liczbie spacji.
	 * @param howMany int - ile spacji
	 * @return String - łańcuch spacji
	 */
	private String spaces(int howMany) {
		String result = "";
		for(int i=0; i<howMany; i++) {
			result += " ";
		}
		return result;
	}
}
