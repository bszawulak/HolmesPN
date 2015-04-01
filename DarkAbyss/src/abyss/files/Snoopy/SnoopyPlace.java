package abyss.files.Snoopy;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Place;
import abyss.varia.NetworkTransformations;

/**
 * Klasa symuluje szaleństwo zapisu miejsc w programie Snoopy. To już nawet nie jest Sparta...
 * @author MR
 *
 */
public class SnoopyPlace {
	private Place abyssPlace;
	public int nodeID; // identyfikator podstawowy miejsca (pierwsze miejsce w sieci ma nr 226, kolejne to już zależy)
	public int placeID; // identyfikator główny miejsca (od zera do liczby miejsc)
	public ArrayList<Integer> grParents; // identyfikatory I typu dla jednego miejsca, na ich bazie
	 	// obliczane są identyfikatory II typu dla... wszystkiego
	public ArrayList<Point> grParentsLocation; // lokalizacje powyższych, więcej niż 1 dla portali
	public boolean portal;
	
	/**
	 * Konstruktor domyślny obiektu klasy SnoopyPlace.
	 */
	public SnoopyPlace() {
		grParents = new ArrayList<Integer>();
		grParentsLocation = new ArrayList<Point>();
		portal = false;
	}
	
	/**
	 * Konstruktor główny obiektu klasy SnoopyPlace. Dostaje obiekt miejsca z Abyss.
	 * @param p Place - obiekt miejsca w programie
	 */
	public SnoopyPlace(Place p) {
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
		nodeID = newFreeId;
		placeID = globalID;
		int currID = nodeID;
		int locations = 1;
		int xOff = 25;
		int yOff = 25;
		
		//sprawdź, ile jest lokalizacji (portal check)
		for(ElementLocation el : abyssPlace.getElementLocations()) {
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
			
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("gridAlignWhenSaved").equals("1"))
				pxy = NetworkTransformations.alignToGrid(pxy);
			
			grParentsLocation.add(pxy);
			locations++;
		}
		//powyższa pętla jest ściśle związana z szukaniem danych łuków w SnoopyWriter
		//łapy precz od niej! I od właściwie czegokolwiek w tej metodzie/klasie!
		
		locations--; //odjąć ostatnie dodawanie
		currID = nodeID; //226
		if(locations == 1) 
			write(bw, "      <node id=\"" + currID + "\" net=\"1\">");
		else
			write(bw, "      <node id=\"" + currID + "\" net=\"1\" logic=\"1\">");
		currID++; //teraz: 227
		
		
		// SEKCJA NAZW MIEJSC - ID, lokalizacje, inne
		write(bw, "        <attribute name=\"Name\" id=\""+currID+"\" net=\"1\">"); //226
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
						+ " net=\"1\" show=\"1\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-4)+"\""
						+ " net=\"1\" show=\"1\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 229
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA WYŚWIETLANYCH IDENTYFIKATORÓW, ODDZIELNIE DLA KAŻDEGO PORTALU
		//I NAPRAWDĘ NIEWAŻNE, ŻE TE ID SĄ DLA NICH IDENTYCZNE. JESTEŚMY W ŚWIECIE
		//TWÓRCÓW SNOOPIEGO
		write(bw, "        <attribute name=\"ID\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 230
		write(bw, "          <![CDATA[" + placeID + "]]>"); //ID OD ZERA W GÓRĘ
		write(bw, "          <graphics count=\"" + locations + "\">");
		xOff = 25;
		yOff = 20;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+currID+"\""
						+ " net=\"1\" show=\"0\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-3)+"\""
						+ " net=\"1\" show=\"0\" grparent=\""+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 231
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA TOKENÓW W MIEJSCU/MIEJSACH LOGICZNYCH. KOMENTARZ JAK WYŻEJ, TYLKO BARDZIEJ ABSURDALNIE.
		write(bw, "        <attribute name=\"Marking\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 232
		int tokens = abyssPlace.getTokensNumber();
		write(bw, "          <![CDATA["+tokens+"]]>");
		write(bw, "          <graphics count=\"" + locations + "\">");
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw,"            <graphic x=\""+grParentsLocation.get(i).x+".00\""
					+ " y=\""+grParentsLocation.get(i).y+".00\""
					+ " id=\""+currID+"\" net=\"1\" show=\"1\" grparent=\""+grParents.get(i)+"\""
					+ " state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//currID = grParent(i) - 4
			} else { // dla logicznych
				write(bw,"            <graphic x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+grParentsLocation.get(i).y+".00\""
						+ " id=\""+(grParents.get(i)-2)+"\" net=\"1\" show=\"1\" grparent=\""+grParents.get(i)+"\""
						+ " state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //teraz: 233
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//CZY MIEJSCE JEST PORTALEM:
		write(bw, "        <attribute name=\"Logic\" id=\""+currID+"\" net=\"1\">");
		currID++; //teraz: 234
		if(locations == 1)
			write(bw, "          <![CDATA[0]]>"); //zwykłe, plebejskie miejsce
		else
			write(bw, "          <![CDATA[1]]>"); //habemus portal
		write(bw, "          <graphics count=\"0\"/>");
		write(bw, "        </attribute>");
		
		//SEKCJA KOMENTARZA. KOMENTARZY... TO ZNACZY JEDNEGO, ALE DLA KAŻDEGO PORTALU... OH, FUCK IT...
		write(bw, "        <attribute name=\"Comment\" id=\"" + currID + "\" net=\"1\">");
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
						+ " id=\"" + currID + "\" net=\"1\" show=\"0\"" //!!!!
						+ " grparent=\"" + grParents.get(i) + "\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//currID = grParent(i) - 1
			} else { // dla logicznych
				write(bw, "            <graphic yoff=\""+yOff+".00\""
						+ " x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\""
						+ " id=\"" + (grParents.get(i)-1) + "\" net=\"1\" show=\"0\"" //!!!!
						+ " grparent=\"" + grParents.get(i) + "\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		currID++; //236 == grParent(0)
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
