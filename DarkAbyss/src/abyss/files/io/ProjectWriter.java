package abyss.files.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import abyss.darkgui.GUIManager;
import abyss.petrinet.data.IdGenerator;
import abyss.petrinet.data.PetriNet;
import abyss.petrinet.elements.Arc;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.Node;
import abyss.petrinet.elements.Place;
import abyss.petrinet.elements.Transition;
import abyss.utilities.Tools;
import abyss.varia.Check;

/**
 * Klasa odpowiedzialna za zapis projektu do pliku. Zapisuje między innymi: dane sieci (miejsca, tranzycje
 * oraz łuki), inwarianty oraz zbiory MCT.
 * 
 * @author MR
 *
 */
public class ProjectWriter {
	private PetriNet projectCore = null;
	private ArrayList<Place> places = null;
	private ArrayList<Transition> transitions = null;
	private ArrayList<MetaNode> metaNodes = null;
	private ArrayList<Arc> arcs = null;
	private ArrayList<ArrayList<Integer>> invariantsMatrix = null;
	private ArrayList<String> invariantsNames = null;
	private ArrayList<ArrayList<Transition>> mctData = null;
	private ArrayList<String> mctNames = null;
	
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
		
		invariantsMatrix = projectCore.getInvariantsMatrix();
		invariantsNames = projectCore.accessInvNames();
		mctData = projectCore.getMCTMatrix();
		mctNames = projectCore.accessMCTNames();
	}
	
	/**
	 * Główna metoda odpowiedzialna za zapis pliku projektu.
	 * @return int - kod błędu, 0 - wszystko ok
	 */
	@SuppressWarnings("unused")
	public boolean writeProject(String filepath) {	
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
			bw.write("  <Invariants data>"+newline);
			bw.write("  <MCT data>"+newline);
			bw.write("</Project blocks>"+newline);
			
			bw.write("<Net data>"+newline);
			bw.write("<ID generator state:"+IdGenerator.getCurrentValues()+">"+newline);
			boolean statusNet = saveNetwork(bw);
			bw.write("<Net data end>"+newline);
			
			bw.write("<Invariants data>"+newline);
			boolean statusInv = saveInvariants(bw);
			bw.write("<Invariants data end>"+newline);
			
			bw.write("<MCT data>"+newline);
			boolean statusMCT = saveMCT(bw);
			bw.write("<MCT data end>"+newline);
			
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
					
					ElementLocation nameLoc = place.getNamesLocations().get(e);
					sheetId = nameLoc.getSheetID();
					pointX = nameLoc.getPosition().x;
					pointY = nameLoc.getPosition().y;
					bw.write(spaces(sp)+"<Place name offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
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
				bw.write(spaces(sp)+"<Transition duration:"+trans.getDPNduration()+">"+newline); //DPN duration
				bw.write(spaces(sp)+"<Transition TPN status:"+trans.getTPNstatus()+">"+newline); //is TPN active?
				bw.write(spaces(sp)+"<Transition DPN status:"+trans.getDPNstatus()+">"+newline); //is DPN active?
				
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
					
					ElementLocation nameLoc = trans.getNamesLocations().get(e);
					sheetId = nameLoc.getSheetID();
					pointX = nameLoc.getPosition().x;
					pointY = nameLoc.getPosition().y;
					bw.write(spaces(sp)+"<Transition name offset data sheet/x/y/elIndex:"+sheetId+";"+pointX+";"+pointY+";"+e+">"+newline); //sheetID/x/y
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
					
					ElementLocation nameLoc = metanode.getNamesLocations().get(e);
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
							
							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+newline);
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
			@SuppressWarnings("unused")
			int x = 1;
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
							
							bw.write(spaces(sp)+"<Arc: "+arcType+"; "+startLoc+" -> "+endLoc+"; "+weight+">"+newline);
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
			@SuppressWarnings("unused")
			int y = 1;
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
			readArcs = 0;
			int totalArcs = arcs.size()-readArcs;
			if(savedArcs != totalArcs) {
				GUIManager.getDefaultGUIManager().log("Error: saved "+savedArcs+" out of total "+totalArcs+" arcs.", "error", true);
			}

			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving net data.", "error", true);
			GUIManager.getDefaultGUIManager().log("Message: "+e.getMessage(), "error", true);
			return false;
		}
		
	}
	
	/**
	 * Metoda służąca do zapisania w pliku projektu inwariantów (CSV) oraz ich nazw.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @return boolean - true, jeśli wszystko dobrze poszło
	 */
	private boolean saveInvariants(BufferedWriter bw) {
		try {
			if(invariantsMatrix == null) {
				bw.write(spaces(2)+"<Invariants: 0>"+newline);
				bw.write(spaces(2)+"<EOI>"+newline);
				return false;	
			}
			
			int sp = 2;
			int invNumber = invariantsMatrix.size();
			
			if(invNumber == 0) {
				bw.write(spaces(2)+"<Invariants: 0>"+newline);
				bw.write(spaces(2)+"<EOI>"+newline);
				return false;	
			}
	
			bw.write(spaces(sp)+"<Invariants: "+invNumber+">"+newline);
			int invSize = invariantsMatrix.get(0).size();
			for(int i=0; i<invNumber; i++) {
				sp = 4;
				ArrayList<Integer> invariant = invariantsMatrix.get(i);
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
				bw.write(spaces(sp)+invariantsNames.get(i)+newline);
			}
			
			bw.write(spaces(2)+"<EOIN>"+newline);
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving invariants data.", "error", true);
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
				bw.write(spaces(sp)+mctNames.get(i)+newline);
			}
			sp = 2;
			bw.write(spaces(sp)+"<EOIMn>"+newline);
			
			return true;
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error while saving MCT data.", "error", true);
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
