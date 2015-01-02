package abyss.files.Snoopy;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Place;

/**
 * Klasa symuluje szaleñstwo zapisu miejsc w programie Snoopy. To ju¿ nawet nie Sparta,
 * tylko o wiele gorzej...
 * @author MR
 *
 */
public class SnoopyPlace {
	private Place abyssPlace;
	public int nodeID; // identyfikator podstawowy miejsca (pierwsze miejsce w sieci ma nr 226, kolejne to ju¿ zale¿y)
	public int placeID; // identyfikator g³ówny miejsca (od zera do liczby miejsc)
	public ArrayList<Integer> grParents; // identyfikatory I typu dla jednego miejsca, na ich bazie
	 	// obliczane s¹ identyfikatory II typu dla... wszystkiego
	public ArrayList<Point> grParentsLocation; // lokalizacje powy¿szych, wiêcej ni¿ 1 dla portali
	public boolean portal;
	
	public SnoopyPlace() {
		grParents = new ArrayList<Integer>();
		grParentsLocation = new ArrayList<Point>();
		portal = false;
	}
	
	public SnoopyPlace(Place p) {
		this();
		abyssPlace = p;
	}

	/**
	 * Odradzam czytaæ kod tej metody. Zostaliœcie ostrze¿eni.
	 * P.S. Jak ktoœ coœ tu bez mojej wiedzy zmieni - zabijê. MR
	 * 
	 * @param bw BufferedWriter - obiekt zapisuj¹cy
	 * @param newFreeId int - aktualne wolne ID snoopiego dla wêz³a
	 * @param globalID int - globalny nr miejsca, od zera of course
	 * @return int - ostatni u¿yty ID snoopiego w tym kodzie
	 */
	public int writePlaceInfoToFile(BufferedWriter bw, int newFreeId, int globalID) {
		nodeID = newFreeId;
		placeID = globalID;
		int currID = nodeID;
		int locations = 1;
		int xOff = 25;
		int yOff = 25;
		
		//sprawdŸ, ile jest lokalizacji (portal check)
		for(ElementLocation el : abyssPlace.getElementLocations()) {
			if(locations == 1) { //g³ówny wêze³
				currID += 10;
			} else if (locations == 2){ //pierwsze miejsce logiczne
				currID += 36;
				portal = true;
			} else { //wszystkie kolejne miejsca logiczne
				currID += 11;
				portal = true;
			}
			grParents.add(currID);
			Point pxy = el.getPosition(); //Bogom dziêki, to to samo w Abyss i Snoopy...
			pxy = setTo20Grid(pxy);
			grParentsLocation.add(pxy);
			locations++;
		}
		//powy¿sza pêtla jest œciœle zwi¹zana z szukaniem danych ³uków w SnoopyWriter
		//³apy precz od niej! I od w³aœciwie czegokolwiek w tej metodzie/klasie!
		
		locations--; //odj¹c ostatnie dodawanie
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
		xOff = 25;
		yOff = 20;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko g³ówne miejsce
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
		
		//SEKCJA WYŒWIETLANYCH IDENTYFIKATORÓW, ODDZIELNIE DLA KA¯DEGO PORTALU
		//I NAPRAWDÊ NIEWA¯NE, ¯E TE ID S¥ DLA NICH IDENTYCZNE. JESTEŒMY W ŒWIECIE
		//TWÓRCÓW SNOOPIEGO
		write(bw, "        <attribute name=\"ID\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 230
		write(bw, "          <![CDATA[" + placeID + "]]>"); //ID OD ZERA W GÓRÊ
		write(bw, "          <graphics count=\"" + locations + "\">");
		xOff = 25;
		yOff = 20;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko g³ówne miejsce
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
		
		//SEKCJA TOKENÓW W MIEJSCU/MIEJSACH LOGICZNYCH. KOMENTARZ JAK WY¯EJ, TYLKO MOCNIEJ.
		write(bw, "        <attribute name=\"Marking\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 232
		write(bw, "          <![CDATA[0]]>");
		write(bw, "          <graphics count=\"" + locations + "\">");
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko g³ówne miejsce
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
			write(bw, "          <![CDATA[0]]>"); //zwyk³e, plebejskie miejsce
		else
			write(bw, "          <![CDATA[1]]>"); //habemus portal
		write(bw, "          <graphics count=\"0\"/>");
		write(bw, "        </attribute>");
		
		//SEKCJA KOMENTARZA. KOMENTARZY... TO ZNACZY JEDNEGO, ALE DLA KA¯DEGO PORTALU... OH, FUCK IT...
		write(bw, "        <attribute name=\"Comment\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 235
		write(bw, "          <![CDATA[" + abyssPlace.getComment() + "]]>"); //achtung enters!
		write(bw, "          <graphics count=\"" + locations + "\">"); //do liczby portali liczyæ bêdziesz,
		 //a liczb¹, do której bêdziesz liczyæ, bêdzie liczba portali. Mniej jest wykluczone.
		xOff = 0;
		yOff = 40;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko g³ówne miejsce
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
		
		//SEKCJA WYŒWIETLANIA MIEJSCA I JEGO KOPII. TAK JAKBYŒMY JU¯ REDUNDATNIE NIE WYŒWIETLILI
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
	
	private void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception e) {
			
		}
	}
	
	private Point setTo20Grid(Point p) {
		//TODO:
		//dzielenie przez 20 bez reszty
		return p;
	}
}
