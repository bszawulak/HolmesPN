package abyss.files.io.Snoopy;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.MetaNode;
import abyss.petrinet.elements.MetaNode.MetaType;
import abyss.petrinet.elements.Transition;
import abyss.varia.NetworkTransformations;

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
	public int snoopyStartingID;
	/** Identyfikator główny tranzycji (od zera do liczby tranzycji) */
	public int globalCoarseID;
	/** Główny ID każdego ElementLocation (SnoopyID) */
	public ArrayList<Integer> grParents; // identyfikatory I typu dla jednej tranzycji, na ich bazie
	 	// obliczane są identyfikatory II typu dla... wszystkiego
	/** Małe ID, lokalizacje artybutów, wskazują na odpowiednie duże ID z  grParents */
	public ArrayList<Point> grParentsLocation; // lokalizacje powyższych, więcej niż 1 dla portali
	
	private MetaType coarseType;
	
	/**
	 * Konstruktor domyślny obiektu klasy SnoopyWriterCoarse.
	 */
	public SnoopyWriterCoarse() {
		grParents = new ArrayList<Integer>();
		grParentsLocation = new ArrayList<Point>();
	}
	
	/**
	 * Konstruktor główny, otrzymuje jako parametr obiekt tranzycji Abyss.
	 * @param m MetaNode - obiekt MetaNode w programie głównym
	 */
	public SnoopyWriterCoarse(MetaNode m) {
		this();
		coarseNode = m;
		coarseType = coarseNode.getMetaType();
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
		
		grParents.add(currID+5);
		Point pxy = alphaAndOmega.getPosition();
		if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("gridAlignWhenSaved").equals("1"))
			pxy = NetworkTransformations.alignToGrid(pxy);
		grParentsLocation.add(pxy);
		
		locations--; //odjąć ostatnie dodawanie
		currID = snoopyStartingID; //reset, i zaczynamy dodawać (np 357)
		if(locations == 1) 
			write(bw, "      <node id=\"" + currID + "\" net=\"1\">");
		else
			write(bw, "      <node id=\"" + currID + "\" net=\"1\" logic=\"1\">");
		currID++; //teraz: 358
		
		
		// SEKCJA NAZW TRANZYCJI - ID, lokalizacje, inne
		write(bw, "        <attribute name=\"Name\" id=\""+currID+"\" net=\"1\">"); //358
		currID++; //teraz: 359
		write(bw, "          <![CDATA[" + coarseNode.getName() + "]]>");
		write(bw, "          <graphics count=\"" + locations + "\">"); //ile logicznych
		xOff = 5; //TODO: + abyssPlace.getNameOffX();
		yOff = 20; //TODO: + abyssPlace.getNameOffY();
		for(int i=0; i<locations; i++) { 
			//TODO: decyzja, czy środkować czy brać offset z Abyss
			xOff = coarseNode.getXNameLoc(i);
			yOff = coarseNode.getYNameLoc(i);
			yOff = SnoopyToolClass.getNormalizedY(yOff);
			
			if(i==0) {//tylko główne miejsce
				write(bw, "            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+currID+"\""
						+ " net=\"1\" show=\"1\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-3)+"\""
						+ " net=\"1\" show=\"1\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 360
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA WYŚWIETLANYCH IDENTYFIKATORÓW, ODDZIELNIE DLA KAŻDEGO PORTALU
		//I NAPRAWDĘ NIEWAŻNE, ŻE TE ID SĄ DLA NICH IDENTYCZNE. JESTEŚMY W ŚWIECIE
		//TWÓRCÓW SNOOPIEGO
		write(bw, "        <attribute name=\"ID\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 361
		write(bw, "          <![CDATA[" + globalCoarseID + "]]>"); //ID OD ZERA W GÓRĘ
		write(bw, "          <graphics count=\"" + locations + "\">");
		xOff = 25;
		yOff = 20;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw, "            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+currID+"\""
						+ " net=\"1\" show=\"0\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-2)+"\""
						+ " net=\"1\" show=\"0\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 362
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//CZY TRANZYCJA JEST PORTALEM:
		write(bw, "        <attribute name=\"Logic\" id=\""+currID+"\" net=\"1\">");
		currID++; //teraz: 363
		if(locations == 1)
			write(bw, "          <![CDATA[0]]>"); //zwykła, plebejska tranzycja
		else
			write(bw, "          <![CDATA[1]]>"); //habemus portal!
		write(bw, "          <graphics count=\"0\"/>");
		write(bw, "        </attribute>");
		
		//SEKCJA KOMENTARZA. KOMENTARZY... TO ZNACZY JEDNEGO, ALE DLA KAŻDEGO PORTALU... NEVERMIND...
		write(bw, "        <attribute name=\"Comment\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 364
		write(bw, "          <![CDATA[" + coarseNode.getComment() + "]]>"); //achtung enters!
		write(bw, "          <graphics count=\"" + locations + "\">"); //do liczby portali liczyć będziesz,
		 //a liczbą, do której będziesz liczyć, będzie liczba portali. Mniej jest wykluczone.
		xOff = 40;
		yOff = 0; //TO JEST CHYBA BŁAD W SNOOPYM. NIE JEDYNY... POWINNO BYC YOFF, NIE XOFF, tak jak w miejscach
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\"" + (grParentsLocation.get(i).x+xOff) + ".00\""
						+ " y=\""+grParentsLocation.get(i).y+".00\""
						+ " id=\"" + currID + "\" net=\"1\" show=\"0\""
						+ " grparent=\"" + grParents.get(i) + "\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//currID == grParent(i) - 1 !
			} else { // dla logicznych
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\"" + (grParentsLocation.get(i).x+xOff) + ".00\""
						+ " y=\"" + grParentsLocation.get(i).y + ".00\""
						+ " id=\"" + (grParents.get(i)-1) + "\" net=\"1\" show=\"0\""
						+ " grparent=\"" + grParents.get(i) + "\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //365 == grParent(0)
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA WYŚWIETLANIA MIEJSCA I JEGO KOPII. TAK JAKBYŚMY JUŻ REDUNDATNIE NIE WYŚWIETLILI
		//JEGO ELEMENTÓW NIE WIADOMO ILE RAZY...
		write(bw, "        <graphics count=\""+locations+"\">");
		
		if(currID != grParents.get(0)) {
			GUIManager.getDefaultGUIManager().log("CATASTROPHIC ERROR WRITING SPPED FILE. RUN.", "error", true);
		}
		
		for(int i=0; i<locations; i++) { 
			write(bw, "          <graphic x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+grParentsLocation.get(i).y+".00\""
						+ " id=\""+grParents.get(i)+"\" net=\"1\""
						+ " show=\"1\" w=\"20.00\" h=\"20.00\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
		}
		
		write(bw,"        </graphics>");
		write(bw, "      </node>");
		
		int lastParentID = grParents.get(locations-1);
		return lastParentID;
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
}
