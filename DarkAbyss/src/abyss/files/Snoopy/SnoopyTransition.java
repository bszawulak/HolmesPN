package abyss.files.Snoopy;

import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.ElementLocation;
import abyss.math.Transition;
import abyss.varia.NetworkTransformations;


/**
 * Klasa symuluje szaleństwo zapisu miejsc w programie Snoopy. To już nawet nie Sparta,
 * tylko o wiele gorzej...
 * @author MR
 *
 */
public class SnoopyTransition {
	private Transition abyssTransition;
	public int nodeID; // identyfikator podstawowy tranzycji
	public int transID; // identyfikator główny tranzycji (od zera do liczby tranzycji)
	public ArrayList<Integer> grParents; // identyfikatory I typu dla jednej tranzycji, na ich bazie
	 	// obliczane są identyfikatory II typu dla... wszystkiego
	public ArrayList<Point> grParentsLocation; // lokalizacje powyższych, więcej niż 1 dla portali
	public boolean portal;
	
	/**
	 * Konstruktor domyślny obiektu klasy SnoopyTransition.
	 */
	public SnoopyTransition() {
		grParents = new ArrayList<Integer>();
		grParentsLocation = new ArrayList<Point>();
		portal = false;
	}
	
	/**
	 * Konstruktor główny, otrzymuje jako parametr obiekt tranzycji Abyss.
	 * @param t Transition - obiekt tranzycji w programie głównym
	 */
	public SnoopyTransition(Transition t) {
		this();
		abyssTransition = t;
	}
	
	/**
	 * Odradzam czytać kod tej metody. Zostaliście ostrzeżeni.
	 * P.S. Jak ktoś coś tu bez mojej wiedzy zmieni - zabiję. MR
	 * 
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param newFreeId int - aktualne wolne ID snoopiego dla węzła
	 * @param globalID int - globalny nr tranzycji, od zera of course
	 * @return int - ostatni użyty ID snoopiego w tym kodzie
	 */
	public int writeTransitionInfoToFile(BufferedWriter bw, int newFreeId, int globalID) {
		nodeID = newFreeId;
		transID = globalID;
		int currID = nodeID;
		int locations = 1;
		int xOff = 25;
		int yOff = 25;
		
		//sprawdź, ile jest lokalizacji (portal check)
		for(ElementLocation el : abyssTransition.getElementLocations()) {
			if(locations == 1) { //główny węzeł
				currID += 8;
			} else if (locations == 2){ //pierwsze miejsce logiczne
				currID += 31;
				portal = true;
			} else { //wszystkie kolejne miejsca logiczne
				currID += 9;
				portal = true;
			}
			grParents.add(currID);
			Point pxy = el.getPosition();
			
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("gridAlignWhenSaved").equals("1"))
				pxy = NetworkTransformations.alignToGrid(pxy);
			
			grParentsLocation.add(pxy);
			locations++;
		}
		//powyższa pętla jest ściśle związana z szukaniem danych łuków w SnoopyWriter
		//łapy precz od niej! I od właściwie czegokolwiek w tej metodzie/klasie!
		
		locations--; //odjąć ostatnie dodawanie
		currID = nodeID; //reset, i zaczynamy dodawać (np 357)
		if(locations == 1) 
			write(bw, "      <node id=\"" + currID + "\" net=\"1\">");
		else
			write(bw, "      <node id=\"" + currID + "\" net=\"1\" logic=\"1\">");
		currID++; //teraz: 358
		
		
		// SEKCJA NAZW TRANZYCJI - ID, lokalizacje, inne
		write(bw, "        <attribute name=\"Name\" id=\""+currID+"\" net=\"1\">"); //358
		currID++; //teraz: 359
		write(bw, "          <![CDATA[" + abyssTransition.getName() + "]]>");
		write(bw, "          <graphics count=\"" + locations + "\">"); //ile logicznych
		xOff = 5; //TODO: + abyssPlace.getNameOffX();
		yOff = 20; //TODO: + abyssPlace.getNameOffY();
		for(int i=0; i<locations; i++) { 
			//TODO: decyzja, czy środkować czy brać offset z Abyss
			xOff = abyssTransition.getXNameLoc(i);
			yOff = abyssTransition.getYNameLoc(i);
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
		write(bw, "          <![CDATA[" + transID + "]]>"); //ID OD ZERA W GÓRĘ
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
		write(bw, "          <![CDATA[" + abyssTransition.getComment() + "]]>"); //achtung enters!
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
