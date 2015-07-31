package abyss.files.io.Snoopy;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.Place;
import abyss.varia.NetworkTransformations;

/**
 * Klasa symuluje szaleństwo zapisu miejsc w programie Snoopy. To już nawet nie jest Sparta...
 * @author MR
 *
 */
public class SnoopyWriterPlace {
	private Place abyssPlace;
	/** Identyfikator podstawowy miejsca (pierwsze miejsce w sieci ma nr 226, kolejne to już zależy)  */
	public int snoopyStartingID = -1;
	/** Identyfikator główny miejsca (od zera do liczby miejsc) */
	public int globalPlaceID = -1;
	/** Główny ID każdego ElementLocation (SnoopyID) */
	public ArrayList<Integer> grParents; // identyfikatory I typu dla jednego miejsca, na ich bazie
	 	// obliczane są identyfikatory II typu dla... wszystkiego
	/** Małe ID, lokalizacje artybutów, wskazują na odpowiednie duże ID z  grParents */
	public ArrayList<Point> grParentsLocation; // lokalizacje powyższych, więcej niż 1 dla portali
	public boolean portal;
	
	/**
	 * Konstruktor domyślny obiektu klasy SnoopyPlace.
	 */
	public SnoopyWriterPlace() {
		grParents = new ArrayList<Integer>(); //główny ID miejsca (graphics), najwyższa liczba z ID, inne mówią mu 'grparent'
		grParentsLocation = new ArrayList<Point>();
		portal = false;
	}
	
	/**
	 * Konstruktor główny obiektu klasy SnoopyPlace. Dostaje obiekt miejsca z Abyss.
	 * @param p Place - obiekt miejsca w programie
	 */
	public SnoopyWriterPlace(Place p) {
		this();
		abyssPlace = p;
	}

	/**
	 * Odradzam czytać kod tej metody. Zostaliście ostrzeżeni.
	 * P.S. Jak ktoś coś tu bez mojej wiedzy zmieni - zabiję. MR
	 * 
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param newFreeId int - aktualne wolne ID snoopiego dla węzła
	 * @param globalID int - globalny nr miejsca, od zera of course
	 * @return int - ostatni użyty ID snoopiego w tym kodzie
	 */
	public int writePlaceInfoToFile(BufferedWriter bw, int newFreeId, int globalID) {
		snoopyStartingID = newFreeId;
		globalPlaceID = globalID;
		int currID = snoopyStartingID;
		int locations = 1;
		int xOff = 25;
		int yOff = 25;
		
		ArrayList<Integer> locationsSheetID = new ArrayList<Integer>();
		int netMainID = 0;
		//sprawdź, ile jest lokalizacji (portal check)
		boolean isInterface = false;
		for(ElementLocation el : abyssPlace.getElementLocations()) {
			if(el.accessMetaInArcs().size()>0 || el.accessMetaOutArcs().size()>0) {
				isInterface = true;
				break;
			}
		}
		
		ArrayList<Integer> stateForEL = new ArrayList<Integer>();
		for(ElementLocation el : abyssPlace.getElementLocations()) {
			locationsSheetID.add(el.getSheetID() + 1);
			
			if(isInterface) {
				if(el.getSheetID() != 0) //wszystkie podsieci
					stateForEL.add(8);
				else if(el.accessMetaInArcs().size()>0 || el.accessMetaOutArcs().size()>0) { //sieć główna
					stateForEL.add(4);
				} else { // zwykły portal
					stateForEL.add(1);
				}
			} else {
				stateForEL.add(1);
			}
			
			if(locations == 1) { //główny węzeł
				currID += 10;
			} else if (locations == 2){ //pierwsze miejsce logiczne
				currID += 36;
				portal = true;
			} else { //wszystkie kolejne miejsca logiczne
				currID += 11;
				portal = true;
			}
			grParents.add(currID);
			Point pxy = el.getPosition(); //Bogom dzięki, to to samo w Abyss i Snoopy...
			
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorGridAlignWhenSaved").equals("1"))
				pxy = NetworkTransformations.alignToGrid(pxy);
			
			grParentsLocation.add(pxy);
			locations++;
		}
		//!!!!!!!!!!:
		for(int x : locationsSheetID) {
			if(x==1) {//jest gdzieś w głównej sieci
				netMainID = 1;
			}
		}
		if(netMainID == 0) {
			netMainID = locationsSheetID.get(0);
		}
		
		//powyższa pętla jest ściśle związana z szukaniem danych łuków w SnoopyWriter
		//łapy precz od niej! I od właściwie czegokolwiek w tej metodzie/klasie!
		
		locations--; //odjąć ostatnie dodawanie
		currID = snoopyStartingID; //226
		if(locations == 1) 
			write(bw, "      <node id=\"" + currID + "\" net=\""+netMainID+"\">");
		else
			write(bw, "      <node id=\"" + currID + "\" net=\""+netMainID+"\" logic=\"1\">");
		currID++; //teraz: 227
		
		// SEKCJA NAZW MIEJSC - ID, lokalizacje, inne
		write(bw, "        <attribute name=\"Name\" id=\""+currID+"\" net=\""+netMainID+"\">"); //226
		currID++; //teraz: 228
		write(bw, "          <![CDATA[" + abyssPlace.getName() + "]]>");
		write(bw,"          <graphics count=\"" + locations + "\">"); //ile logicznych
		xOff = 5; //TODO: + abyssPlace.getNameOffX();
		yOff = 20; //TODO: + abyssPlace.getNameOffY();
		for(int i=0; i<locations; i++) { 
			//TODO: decyzja, czy środkować czy brać offset z Abyss
			xOff = abyssPlace.getXNameLoc(i);
			yOff = abyssPlace.getYNameLoc(i);
			yOff = SnoopyToolClass.getNormalizedY(yOff);
			
			if(i==0) {//tylko główne miejsce
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+currID+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"1\" grparent=\""
						+ grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-4)+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"1\" grparent=\""
						+ grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 229
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA WYŚWIETLANYCH IDENTYFIKATORÓW, ODDZIELNIE DLA KAŻDEGO PORTALU
		//I NAPRAWDĘ NIEWAŻNE, ŻE TE ID SĄ DLA NICH IDENTYCZNE. JESTEŚMY W ŚWIECIE
		//TWÓRCÓW SNOOPIEGO
		write(bw, "        <attribute name=\"ID\" id=\"" + currID + "\" net=\""+netMainID+"\">");
		currID++; //teraz: 230
		write(bw, "          <![CDATA[" + globalPlaceID + "]]>"); //ID OD ZERA W GÓRĘ
		write(bw, "          <graphics count=\"" + locations + "\">");
		xOff = 25;
		yOff = 20;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+currID+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"0\" grparent=\""
						+ grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-3)+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"0\" grparent=\""
						+ grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 231
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA TOKENÓW W MIEJSCU/MIEJSACH LOGICZNYCH. KOMENTARZ JAK WYŻEJ, TYLKO BARDZIEJ ABSURDALNIE.
		write(bw, "        <attribute name=\"Marking\" id=\"" + currID + "\" net=\""+netMainID+"\">");
		currID++; //teraz: 232
		int tokens = abyssPlace.getTokensNumber();
		write(bw, "          <![CDATA["+tokens+"]]>");
		write(bw, "          <graphics count=\"" + locations + "\">");
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw,"            <graphic x=\""+grParentsLocation.get(i).x+".00\""
					+ " y=\""+grParentsLocation.get(i).y+".00\""
					+ " id=\""+currID+"\" net=\""+locationsSheetID.get(i)+"\" show=\"1\" grparent=\""+grParents.get(i)+"\""
					+ " state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//currID = grParent(i) - 4
			} else { // dla logicznych
				write(bw,"            <graphic x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+grParentsLocation.get(i).y+".00\""
						+ " id=\""+(grParents.get(i)-2)+"\" net=\""+locationsSheetID.get(i)+"\" show=\"1\" grparent=\""
						+ grParents.get(i)+"\""
						+ " state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 233
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//CZY MIEJSCE JEST PORTALEM:
		write(bw, "        <attribute name=\"Logic\" id=\""+currID+"\" net=\""+netMainID+"\">");
		
		if(locations == 1)
			write(bw, "          <![CDATA[0]]>"); //zwykłe, plebejskie miejsce
		else
			write(bw, "          <![CDATA[1]]>"); //habemus portal
		write(bw, "          <graphics count=\"0\"/>");
		write(bw, "        </attribute>");
		
		//SEKCJA KOMENTARZA. KOMENTARZY... TO ZNACZY JEDNEGO, ALE DLA KAŻDEGO PORTALU... OH, FUCK IT...
		currID++; //teraz: 234
		write(bw, "        <attribute name=\"Comment\" id=\"" + currID + "\" net=\""+netMainID+"\">");
		currID++; //teraz: 235
		write(bw, "          <![CDATA[" + abyssPlace.getComment() + "]]>"); //achtung enters!
		write(bw, "          <graphics count=\"" + locations + "\">"); //do liczby portali liczyć będziesz,
		 //a liczbą, do której będziesz liczyć, będzie liczba portali. Mniej jest wykluczone.
		xOff = 0;
		yOff = 40;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw, "            <graphic yoff=\""+yOff+".00\""
						+ " x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\""
						+ " id=\"" + currID + "\" net=\""+locationsSheetID.get(i)+"\" show=\"0\"" //!!!!
						+ " grparent=\"" + grParents.get(i) + "\" state=\""+stateForEL.get(i)+"\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//currID = grParent(i) - 1
			} else { // dla logicznych
				write(bw, "            <graphic yoff=\""+yOff+".00\""
						+ " x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\""
						+ " id=\"" + (grParents.get(i)-1) + "\" net=\""+locationsSheetID.get(i)+"\" show=\"0\"" //!!!!
						+ " grparent=\"" + grParents.get(i) + "\" state=\""+stateForEL.get(i)+"\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		currID++; //236 == grParent(0)
		
		//SEKCJA WYŚWIETLANIA MIEJSCA I JEGO KOPII. TAK JAKBYŚMY JUŻ REDUNDATNIE NIE WYŚWIETLILI
		//JEGO ELEMENTÓW NIE WIADOMO ILE RAZY...
		write(bw, "        <graphics count=\""+locations+"\">");
		
		if(currID != grParents.get(0)) {
			GUIManager.getDefaultGUIManager().log("Critical error while writing Snoopy file. ID's don't match.", "error", true);
		}
		
		for(int i=0; i<locations; i++) { 
			write(bw, "          <graphic x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+grParentsLocation.get(i).y+".00\""
						+ " id=\""+grParents.get(i)+"\" net=\""+locationsSheetID.get(i)+"\""
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
	
	public String toString() {
		String txt = "";
		int pPos = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces().indexOf(abyssPlace);
		txt += "P"+pPos + " [gPlaceID:"+globalPlaceID+"]";
		txt += " [SnoopyStartID: "+snoopyStartingID+"]";
		if(grParents.size()>0) {
			txt += " [gParentID:";
			for(int x : grParents) {
				txt += " "+x;
			}
			txt += "]";
		}
		return txt;
	}
}
