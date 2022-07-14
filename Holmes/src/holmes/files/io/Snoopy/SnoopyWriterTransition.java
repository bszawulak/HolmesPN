package holmes.files.io.Snoopy;

import java.awt.Color;
import java.awt.Point;
import java.io.BufferedWriter;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Transition;
import holmes.varia.NetworkTransformations;


/**
 * Klasa symuluje szaleństwo zapisu miejsc w programie Snoopy. To już nawet nie Sparta,
 * tylko o wiele gorzej...
 * @author MR
 *
 */
public class SnoopyWriterTransition {
	protected Transition holmesTransition;
	/** Identyfikator podstawowy tranzycji  */
	protected int snoopyStartingID;
	/** Identyfikator główny tranzycji (od zera do liczby tranzycji) */
	protected int globalTransID;
	/** Główny ID każdego ElementLocation (SnoopyID) */
	protected ArrayList<Integer> grParents; // identyfikatory I typu dla jednej tranzycji, na ich bazie
	 	// obliczane są identyfikatory II typu dla... wszystkiego
	/** Małe ID, lokalizacje artybutów, wskazują na odpowiednie duże ID z  grParents */
	protected ArrayList<Point> grParentsLocation; // lokalizacje powyższych, więcej niż 1 dla portali
	protected boolean portal;
	
	/**
	 * Konstruktor domyślny obiektu klasy SnoopyTransition.
	 */
	public SnoopyWriterTransition() {
		grParents = new ArrayList<Integer>();
		grParentsLocation = new ArrayList<Point>();
		portal = false;
	}
	
	/**
	 * Konstruktor główny, otrzymuje jako parametr obiekt tranzycji Holmes.
	 * @param t Transition - obiekt tranzycji w programie głównym
	 */
	public SnoopyWriterTransition(Transition t) {
		this();
		holmesTransition = t;
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
		snoopyStartingID = newFreeId;
		globalTransID = globalID;
		int currID = snoopyStartingID;
		int locations = 1;
		int xOff = 25;
		int yOff = 25;
		
		
		ArrayList<Integer> locationsSheetID = new ArrayList<Integer>();
		int netMainID = 0;
		//sprawdź, ile jest lokalizacji (portal check)
		boolean isInterface = false;
		for(ElementLocation el : holmesTransition.getElementLocations()) {
			if(el.accessMetaInArcs().size()>0 || el.accessMetaOutArcs().size()>0) {
				isInterface = true;
				break;
			}
		}
		
		ArrayList<Integer> stateForEL = new ArrayList<Integer>();
		for(ElementLocation el : holmesTransition.getElementLocations()) {
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
			
			if(GUIManager.getDefaultGUIManager().getSettingsManager().getValue("editorGridAlignWhenSaved").equals("1"))
				pxy = NetworkTransformations.alignToGrid(pxy);
			
			grParentsLocation.add(pxy);
			locations++;
		}

		//!!!!!!!!!! Cthulhu fhtagn!
		for(int x : locationsSheetID) {
			if(x==1) {//jest gdzieś w głównej sieci
				netMainID = 1;
			}
		}
		if(netMainID == 0)
			netMainID = locationsSheetID.get(0);
		
		//powyższa pętla jest ściśle związana z szukaniem danych łuków w SnoopyWriter
		//łapy precz od niej! I od właściwie czegokolwiek w tej metodzie/klasie!
		
		locations--; //odjąć ostatnie dodawanie
		currID = snoopyStartingID; //reset, i zaczynamy dodawać (np 357)

		if(locations == 1) 
			write(bw, "      <node id=\"" + currID + "\" net=\""+netMainID+"\">");
		else
			write(bw, "      <node id=\"" + currID + "\" net=\""+netMainID+"\" logic=\"1\">");
		currID++; //teraz: 358
		
		
		// SEKCJA NAZW TRANZYCJI - ID, lokalizacje, inne
		write(bw, "        <attribute name=\"Name\" id=\""+currID+"\" net=\""+netMainID+"\">"); //358
		currID++; //teraz: 359
		write(bw, "          <![CDATA[" + holmesTransition.getName() + "]]>");
		write(bw, "          <graphics count=\"" + locations + "\">"); //ile logicznych
		xOff = 5; //TODO: + holmesPlace.getNameOffX();
		yOff = 20; //TODO: + holmesPlace.getNameOffY();
		for(int i=0; i<locations; i++) { 
			//TODO: decyzja, czy środkować czy brać offset z Holmes
			xOff = holmesTransition.getTextLocation_X(i, GUIManager.locationMoveType.NAME);
			yOff = holmesTransition.getTextLocation_Y(i, GUIManager.locationMoveType.NAME);
			yOff = SnoopyToolClass.getNormalizedY(yOff);
			
			if(i==0) {//tylko główne miejsce
				write(bw, "            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+currID+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"1\" grparent=\""
						+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-3)+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"1\" grparent=\""
						+grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA WYŚWIETLANYCH IDENTYFIKATORÓW, ODDZIELNIE DLA KAŻDEGO PORTALU
		//I NAPRAWDĘ NIEWAŻNE, ŻE TE ID SĄ DLA NICH IDENTYCZNE. JESTEŚMY W ŚWIECIE
		//TWÓRCÓW SNOOPIEGO
		currID++; //teraz: 360
		write(bw, "        <attribute name=\"ID\" id=\"" + currID + "\" net=\""+netMainID+"\">");
		currID++; //teraz: 361
		write(bw, "          <![CDATA[" + globalTransID + "]]>"); //ID OD ZERA W GÓRĘ
		write(bw, "          <graphics count=\"" + locations + "\">");
		xOff = 25;
		yOff = 20;
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw, "            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+currID+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"0\" grparent=\""
						+ grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			} else { // dla logicznych
				write(bw,"            <graphic xoff=\""+xOff+".00\" yoff=\""+yOff+".00\""
						+ " x=\""+(grParentsLocation.get(i).x+xOff)+".00\""
						+ " y=\""+(grParentsLocation.get(i).y+yOff)+".00\" id=\""+(grParents.get(i)-2)+"\""
						+ " net=\""+locationsSheetID.get(i)+"\" show=\"0\" grparent=\""
						+ grParents.get(i)+"\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//CZY TRANZYCJA JEST PORTALEM:
		currID++; //teraz: 362
		write(bw, "        <attribute name=\"Logic\" id=\""+currID+"\" net=\""+netMainID+"\">");
		if(locations == 1)
			write(bw, "          <![CDATA[0]]>"); //zwykła, plebejska tranzycja
		else
			write(bw, "          <![CDATA[1]]>"); //habemus portal!
		write(bw, "          <graphics count=\"0\"/>");
		write(bw, "        </attribute>");
		
		//SEKCJA KOMENTARZA. KOMENTARZY... TO ZNACZY JEDNEGO, ALE DLA KAŻDEGO PORTALU... NEVERMIND...
		currID++; //teraz: 363
		write(bw, "        <attribute name=\"Comment\" id=\"" + currID + "\" net=\""+netMainID+"\">");
		currID++; //teraz: 364
		write(bw, "          <![CDATA[" + holmesTransition.getComment() + "]]>"); //achtung enters!
		write(bw, "          <graphics count=\"" + locations + "\">"); //do liczby portali liczyć będziesz,
		 //a liczbą, do której będziesz liczyć, będzie liczba portali. Mniej jest wykluczone.
		xOff = 40;
		yOff = 0; //TO JEST CHYBA BŁAD W SNOOPYM. NIE JEDYNY... POWINNO BYC YOFF, NIE XOFF, tak jak w miejscach
		for(int i=0; i<locations; i++) { 
			if(i==0) {//tylko główne miejsce
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\"" + (grParentsLocation.get(i).x+xOff) + ".00\""
						+ " y=\""+grParentsLocation.get(i).y+".00\""
						+ " id=\"" + currID + "\" net=\""+locationsSheetID.get(i)+"\" show=\"0\""
						+ " grparent=\"" + grParents.get(i) + "\" state=\""+stateForEL.get(i)+"\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
				//currID == grParent(i) - 1 !
			} else { // dla logicznych
				write(bw, "            <graphic xoff=\""+xOff+".00\""
						+ " x=\"" + (grParentsLocation.get(i).x+xOff) + ".00\""
						+ " y=\"" + grParentsLocation.get(i).y + ".00\""
						+ " id=\"" + (grParents.get(i)-1) + "\" net=\""+locationsSheetID.get(i)+"\" show=\"0\""
						+ " grparent=\"" + grParents.get(i) + "\" state=\""+stateForEL.get(i)+"\""
						+ " pen=\"0,0,0\" brush=\"255,255,255\"/>");
			}
		}
		
		write(bw, "          </graphics>");
		write(bw, "        </attribute>");
		
		//SEKCJA WYŚWIETLANIA MIEJSCA I JEGO KOPII. TAK JAKBYŚMY JUŻ REDUNDATNIE NIE WYŚWIETLILI
		//JEGO ELEMENTÓW NIE WIADOMO ILE RAZY...
		currID++; //365 == grParent(0)
		if(currID != grParents.get(0)) {
			GUIManager.getDefaultGUIManager().log("Critical error: Snoopy ID's do not match while writing", "errer", true);
		}
		
		write(bw, "        <graphics count=\""+locations+"\">");
		
		if(currID != grParents.get(0)) {
			GUIManager.getDefaultGUIManager().log("Critical error while writing Snoopy file. ID's don't match.", "error", true);
		}
		
		Color snoopyColor = holmesTransition.defColor;
		String brushStr = "255,255,255";
		if(!snoopyColor.equals(new Color(224, 224, 224))) {
			brushStr = snoopyColor.getRed()+","+snoopyColor.getGreen()+","+snoopyColor.getBlue();
		}
		
		for(int i=0; i<locations; i++) { 
			write(bw, "          <graphic x=\""+grParentsLocation.get(i).x+".00\""
						+ " y=\""+grParentsLocation.get(i).y+".00\""
						+ " id=\""+grParents.get(i)+"\" net=\""+locationsSheetID.get(i)+"\""
						+ " show=\"1\" w=\"20.00\" h=\"20.00\" state=\"1\""
						+ " pen=\"0,0,0\" brush=\""+brushStr+"\"/>");
		}
		
		write(bw,"        </graphics>");
		write(bw, "      </node>");

		return grParents.get(locations-1);
	}

	/**
	 * Metoda pomocnicza, zapisująca każdą linię + enter.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param text String - linia tekstu
	 */
	protected void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception ignored) {
			
		}
	}
	
	public String toString() {
		StringBuilder txt = new StringBuilder();
		int tPos = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions().indexOf(holmesTransition);
		txt.append("T").append(tPos).append(" [gTransID:").append(globalTransID).append("]");
		txt.append(" [SnoopyStartID: ").append(snoopyStartingID).append("]");
		if(grParents.size()>0) {
			txt.append(" [gParentID:");
			for(int x : grParents) {
				txt.append(" ").append(x);
			}
			txt.append("]");
		}
		return txt.toString();
	}
}
