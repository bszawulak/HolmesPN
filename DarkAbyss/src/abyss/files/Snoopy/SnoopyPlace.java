package abyss.files.Snoopy;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Place;

/**
 * Klasa symuluje szale�stwo zapisu miejsc w programie Snoopy. To ju� nawet nie Sparta,
 * tylko o wiele gorzej...
 * @author MR
 *
 */
public class SnoopyPlace {
	private Place abyssPlace;
	public int nodeID; // identyfikator podstawowy miejsca (pierwsze miejsce w sieci ma nr 226, kolejne to ju� zale�y)
	public int placeID; // identyfikator g��wny miejsca (od zera do liczby miejsc)
	public ArrayList<Integer> grParents; // identyfikatory I typu dla jednego miejsca, na ich bazie
	 	// obliczane s� identyfikatory II typu dla... wszystkiego
	public ArrayList<Point> grParentsLocation; // lokalizacje powy�szych, wi�cej ni� 1 dla portali
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
	 * Odradzam czyta� kod tej metody. Zostali�cie ostrze�eni.
	 * P.S. Jak kto� co� tu bez mojej wiedzy zmieni - zabij�. MR
	 * 
	 * @param bw BufferedWriter - obiekt zapisuj�cy
	 * @param newFreeId int - aktualne wolne ID snoopiego dla w�z�a
	 * @param globalID int - globalny nr miejsca, od zera of course
	 * @return int - ostatni u�yty ID snoopiego w tym kodzie
	 */
	public int writePlaceInfoToFile(BufferedWriter bw, int newFreeId, int globalID) {
		nodeID = newFreeId;
		placeID = globalID;
		int currID = nodeID;
		int locations = 1;
		int xOff = 25;
		int yOff = 25;
		
		//sprawd�, ile jest lokalizacji (portal check)
		for(ElementLocation el : abyssPlace.getElementLocations()) {
			if(locations == 1) { //g��wny w�ze�
				currID += 10;
			} else if (locations == 2){ //pierwsze miejsce logiczne
				currID += 36;
				portal = true;
			} else { //wszystkie kolejne miejsca logiczne
				currID += 11;
				portal = true;
			}
			grParents.add(currID);
			Point pxy = el.getPosition(); //Bogom dzi�ki, to to samo w Abyss i Snoopy...
			pxy = setTo20Grid(pxy);
			grParentsLocation.add(pxy);
			locations++;
		}
		//powy�sza p�tla jest �ci�le zwi�zana z szukaniem danych �uk�w w SnoopyWriter
		//�apy precz od niej! I od w�a�ciwie czegokolwiek w tej metodzie/klasie!
		
		locations--; //odj�c ostatnie dodawanie
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
			if(i==0) {//tylko g��wne miejsce
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
		
		//SEKCJA WY�WIETLANYCH IDENTYFIKATOR�W, ODDZIELNIE DLA KA�DEGO PORTALU
		//I NAPRAWD� NIEWA�NE, �E TE ID S� DLA NICH IDENTYCZNE. JESTE�MY W �WIECIE
		//TW�RC�W SNOOPIEGO
		write(bw, "        <attribute name=\"ID\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 230
		write(bw, "          <![CDATA[" + placeID + "]]>"); //ID OD ZERA W G�R�
		write(bw, "          <graphics count=\"" + locations + "\">");
		xOff = 25;
		yOff = 20;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko g��wne miejsce
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
		
		//SEKCJA TOKEN�W W MIEJSCU/MIEJSACH LOGICZNYCH. KOMENTARZ JAK WY�EJ, TYLKO MOCNIEJ.
		write(bw, "        <attribute name=\"Marking\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 232
		write(bw, "          <![CDATA[0]]>");
		write(bw, "          <graphics count=\"" + locations + "\">");
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko g��wne miejsce
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
			write(bw, "          <![CDATA[0]]>"); //zwyk�e, plebejskie miejsce
		else
			write(bw, "          <![CDATA[1]]>"); //habemus portal
		write(bw, "          <graphics count=\"0\"/>");
		write(bw, "        </attribute>");
		
		//SEKCJA KOMENTARZA. KOMENTARZY... TO ZNACZY JEDNEGO, ALE DLA KA�DEGO PORTALU... OH, FUCK IT...
		write(bw, "        <attribute name=\"Comment\" id=\"" + currID + "\" net=\"1\">");
		currID++; //teraz: 235
		write(bw, "          <![CDATA[" + abyssPlace.getComment() + "]]>"); //achtung enters!
		write(bw, "          <graphics count=\"" + locations + "\">"); //do liczby portali liczy� b�dziesz,
		 //a liczb�, do kt�rej b�dziesz liczy�, b�dzie liczba portali. Mniej jest wykluczone.
		xOff = 0;
		yOff = 40;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko g��wne miejsce
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
		
		//SEKCJA WY�WIETLANIA MIEJSCA I JEGO KOPII. TAK JAKBY�MY JU� REDUNDATNIE NIE WY�WIETLILI
		//JEGO ELEMENT�W NIE WIADOMO ILE RAZY...
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
