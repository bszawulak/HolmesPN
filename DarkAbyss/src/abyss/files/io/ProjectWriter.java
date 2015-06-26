package abyss.files.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.filechooser.FileFilter;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.IdGenerator;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.PetriNet;
import abyss.math.Place;
import abyss.math.Transition;
import abyss.utilities.Tools;
import abyss.varia.Check;
import abyss.workspace.ExtensionFileFilter;

public class ProjectWriter {
	private PetriNet projectCore = null;
	private ArrayList<Place> places = null;
	private ArrayList<Transition> transitions = null;
	private ArrayList<Arc> arcs = null;
	private ArrayList<ArrayList<Integer>> invariantsMatrix = null;
	private ArrayList<String> invariantsNames = null;
	private ArrayList<ArrayList<Transition>> mctData = null;
	private ArrayList<String> mctNames = null;
	
	
	String newline = "\n";
	
	public ProjectWriter() {
		projectCore = GUIManager.getDefaultGUIManager().getWorkspace().getProject();
		places = projectCore.getPlaces();
		transitions = projectCore.getTransitions();
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
	public boolean writeProject(String filepath) {	
		try {
			//BufferedWriter bw = new BufferedWriter(new FileWriter("tmp//test.apf"));
			BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
			String projName = projectCore.getName();
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			bw.write("Project name: "+projName+newline);
			bw.write("Date: "+dateFormat.format(date)+newline);
			bw.write("<Net data>"+newline);
			bw.write("<ID generator state:"+IdGenerator.getCurrentValues()+">"+newline);
			boolean status = saveNetwork(bw);
			bw.write("<Net data end>"+newline);
			
			bw.close();
			return true;
		} catch (Exception e) {
			
			return false;
		}
	}

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
				bw.write(spaces(sp)+"<Transition eft:"+trans.getMinFireTime()+">"+newline); //TPN eft
				bw.write(spaces(sp)+"<Transition lft:"+trans.getMaxFireTime()+">"+newline); //TPN lft
				
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
			
			//ŁUKI
			int savedArcs = 0;
			bw.write(spaces(sp)+"<Arcs data block"+">"+newline);
			sp=4;
			for(int p=0; p<placesNumber; p++) {
				Place place = places.get(p);
				int elLocations = place.getElementLocations().size();
				for(int e=0; e<elLocations; e++) { //wszystkie lokalizacje miejsca
					ElementLocation eLoc = place.getElementLocations().get(e);
					ArrayList<Arc> outgoingArcs = eLoc.getOutArcs();
					int arcsNumber = outgoingArcs.size();
					for(int a=0; a<arcsNumber; a++) { //wszystkie łuki wyjściowe
						Arc arc = outgoingArcs.get(a);
						String arcType = ""+arc.getArcType();
						String startLoc = "P"+p+"("+e+")";
						
						Node endNode = arc.getEndNode();
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
			for(int t=0; t<transNumber; t++) {
				Transition trans = transitions.get(t);
				int elLocations = trans.getElementLocations().size();
				for(int e=0; e<elLocations; e++) { //wszystkie lokalizacje tranzycji
					ElementLocation eLoc = trans.getElementLocations().get(e);
					ArrayList<Arc> outgoingArcs = eLoc.getOutArcs();
					int arcsNumber = outgoingArcs.size();
					for(int a=0; a<arcsNumber; a++) { //wszystkie łuki wyjściowe
						Arc arc = outgoingArcs.get(a);
						String arcType = ""+arc.getArcType();
						String startLoc = "T"+t+"("+e+")";
						
						Node endNode = arc.getEndNode();
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
			// bw.write(spaces(sp)+""+">"+newline);
		} catch (Exception e) {
			return false;
		}
		
	}
	
	private boolean saveInvariants(BufferedWriter bw) {
		try {
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean saveMCT(BufferedWriter bw) {
		try {
			
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	private String spaces(int howMany) {
		String result = "";
		for(int i=0; i<howMany; i++) {
			result += " ";
		}
		return result;
	}
}
