package holmes.files.io.Snoopy;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.varia.NetworkTransformations;

/**
 * Klasa symuluje PRAWDZIWE szaleństwo zapisu podsieci (coarse T/P shit) w programie Snoopy. 
 * Niemiec płakał, jak projektował...
 * 
 * @author MR
 *
 */
public class SnoopyWriterCoarse {
	private MetaNode coarseNode;
	/** Identyfikator podstawowy coarseNode  */
	public int snoopyStartingID = -1;
	/** Identyfikator główny tranzycji (od zera do liczby tranzycji) */
	public int globalCoarseID = -1;
	/** Główny ID każdego ElementLocation (SnoopyID) */
	public ArrayList<Integer> grParents; // identyfikatory I typu dla jednej tranzycji, na ich bazie
	 	// obliczane są identyfikatory II typu dla... wszystkiego
	/** Małe ID, lokalizacje artybutów, wskazują na odpowiednie duże ID z  grParents */
	public ArrayList<Point> grParentsLocation; // lokalizacje powyższych, więcej niż 1 dla portali

	/**
	 * Konstruktor domyślny obiektu klasy SnoopyWriterCoarse.
	 */
	public SnoopyWriterCoarse() {
		grParents = new ArrayList<Integer>();
		grParentsLocation = new ArrayList<Point>();
	}
	
	/**
	 * Konstruktor główny, otrzymuje jako parametr obiekt tranzycji programu Holmes.
	 * @param m MetaNode - obiekt MetaNode w programie głównym
	 */
	public SnoopyWriterCoarse(MetaNode m) {
		this();
		coarseNode = m;
		//coarseType = coarseNode.getMetaType();
	}
	
	/**
	 * Odradzam czytać kod tej metody. Zostaliście ostrzeżeni.
	 * P.S. Jak ktoś coś tu bez mojej wiedzy zmieni - zabiję. MR
	 * 
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param newFreeId int - aktualne wolne ID snoopiego dla węzła
	 * @param globalID int - globalny nr obiektu, od zera of course
	 * @return int - ostatni użyty ID snoopiego w tym kodzie
	 */
	public int writeMetaNodeInfoToFile(BufferedWriter bw, int newFreeId, int globalID) {
		snoopyStartingID = newFreeId;
		globalCoarseID = globalID;
		int currID = snoopyStartingID;
		//int locations = 1;
		int xOff = 25;
		int yOff = 25;
		
		//tylko 1 dozwolona lokalizacja
		
		ElementLocation alphaAndOmega = coarseNode.getElementLocations().get(0);
		int netMainID = alphaAndOmega.getSheetID() + 1;
		int subnetID = coarseNode.getRepresentedSheetID() + 1;
		
		grParents.add(currID+5);
		Point pxy = alphaAndOmega.getPosition();
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorGridAlignWhenSaved").equals("1"))
			pxy = NetworkTransformations.alignToGrid(pxy);
		grParentsLocation.add(pxy);
		
		currID = snoopyStartingID; //reset, i zaczynamy dodawać
		write(bw, "      <node id=\"" + currID + "\" net=\""+netMainID+"\" coarse=\""+subnetID+"\">");
		
		
		currID++;
		// SEKCJA NAZW - ID, lokalizacje, inne
		write(bw, "        <attribute name=\"Name\" id=\""+currID+"\" net=\""+netMainID+"\">");
		currID++;
		write(bw, "          <![CDATA[" + coarseNode.getName() + "]]>");
		write(bw, "          <graphics count=\"1\">");
		//xOff = 5; 
		//yOff = 20; 
		xOff = coarseNode.getXNameLoc(0);
		yOff = coarseNode.getYNameLoc(0);
		yOff = SnoopyToolClass.getNormalizedY(yOff);
			
		write(bw, "            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
				+ " x=\""+(grParentsLocation.get(0).x+xOff)+".00\""
				+ " y=\""+(grParentsLocation.get(0).y+yOff)+".00\" id=\""+currID+"\""
				+ " net=\""+netMainID+"\" show=\"1\" grparent=\""+grParents.get(0)+"\" state=\"1\""
				+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		currID++;
		
		//SEKCJA KOMENTARZA
		write(bw, "        <attribute name=\"Comment\" id=\"" + currID + "\" net=\""+netMainID+"\">");
		currID++;
		write(bw, "          <![CDATA[" + coarseNode.getComment() + "]]>"); //achtung enters!
		write(bw, "          <graphics count=\"1\">");

		xOff = 40;
		yOff = 0; //TO JEST CHYBA BŁĄD W SNOOPYM
		write(bw, "            <graphic xoff=\""+xOff+".00\""
				+ " x=\"" + (grParentsLocation.get(0).x+xOff) + ".00\""
				+ " y=\""+grParentsLocation.get(0).y+".00\""
				+ " id=\"" + currID + "\" net=\""+netMainID+"\" show=\"0\""
				+ " grparent=\"" + grParents.get(0) + "\" state=\"1\""
				+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");

		currID++;
		write(bw, "        <graphics count=\"1\">");
		if(currID != grParents.get(0)) {
			GUIManager.getDefaultGUIManager().log("Critical error while writing Snoopy file. ID's don't match.", "error", true);
		}
		
		write(bw, "          <graphic x=\""+grParentsLocation.get(0).x+".00\""
				+ " y=\""+grParentsLocation.get(0).y+".00\""
				+ " id=\""+grParents.get(0)+"\" net=\""+netMainID+"\""
				+ " show=\"1\" w=\"20.00\" h=\"20.00\" state=\"1\""
				+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
		write(bw,"        </graphics>");
		write(bw, "      </node>");

		return grParents.get(0);
	}

	/**
	 * Metoda pomocnicza, zapisująca każdą linię + enter.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param text String - linia tekstu
	 */
	private void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception e) {
			
		}
	}
	
	public String toString() {
		String txt = "";
		String type = "";
		if(coarseNode.getMetaType() == MetaType.SUBNETPLACE)
			type = "[cPlace]";
		else
			type = "[cTrans]";
		
		int mPos = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getMetaNodes().indexOf(coarseNode);
		txt += "M"+mPos + " [gcID:"+globalCoarseID+"]";
		txt += " [SnoopyStartID: "+snoopyStartingID+"]";
		txt += " "+type;
		if(grParents.size()>0)
			txt += " [gParentID:"+grParents.get(0)+"]";
		
		return txt;
	}
}
