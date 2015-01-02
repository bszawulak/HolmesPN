package abyss.files.Snoopy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.math.Arc;
import abyss.math.Node;
import abyss.math.Place;
import abyss.math.Transition;

/**
 * Autor w�o�y� ogromny wysi�ek, aby ta klasa wraz z pomocniczymi potrafi�a zasymulowa�
 * ob��d tw�rc�w programu Snoopy, kt�ry objawia si� na ka�dym etapie zapisu danych
 * sieci do pliku.
 * @author MR
 *
 */
public class SnoopyWriter {
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<Place> places = new ArrayList<Place>();
	private ArrayList<Transition> transitions = new ArrayList<Transition>();
	private ArrayList<Node> nodes = new ArrayList<Node>();
	
	private ArrayList<SnoopyPlace> snoopyPlaces = new ArrayList<SnoopyPlace>();
	private ArrayList<SnoopyTransition> snoopyTransitions = new ArrayList<SnoopyTransition>();
	
	private String dateAndTime = "2015-01-02 10:44:56";
	
	public SnoopyWriter() {
		arcs = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getArcs();
		places = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getPlaces();
		transitions = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getTransitions();
		nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		
		snoopyPlaces = new ArrayList<SnoopyPlace>();
		snoopyTransitions = new ArrayList<SnoopyTransition>();
	}
	
	public void writeSPPED() {
		int startNodeId = 226; // bo tak
		int currentActiveID = startNodeId;
		try {
			String tPath = GUIManager.getDefaultGUIManager().tmpPath;
			BufferedWriter bw = new BufferedWriter(new FileWriter(tPath+"argh.spped"));
			//NAG��WEK:
			write(bw, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			write(bw, "<?xml-stylesheet type=\"text/xsl\" href=\"/xsl/spped2svg.xsl\"?>");
			write(bw, "<Snoopy version=\"2\" revision=\"1.13\">");
			
			write(bw, "  <netclass name=\"Petri Net\"/>");
			write(bw, "  <nodeclasses count=\"4\">"); //zawsze 4
			
			//MIEJSCA:
			int placesNumber = places.size();
			write(bw, "    <nodeclass count=\""+placesNumber+"\" name=\"Place\">");
			int globalPlaceId = 0;
			for(Place p : places) {
				SnoopyPlace sPlace = new SnoopyPlace(p);
				snoopyPlaces.add(sPlace);
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				
				if(sPlace.portal == true) { //je�li w�a�nie dodane by�o portalem
					currentActiveID += 13; //bo tak, pytajcie w Brandenburgu 'a a a czymuuu?'
				} else {
					currentActiveID ++;
				}
				globalPlaceId++;
				
			}
			write(bw, "    </nodeclass>");
			
			// TRANZYCJE:
			int transNumber = transitions.size();
			write(bw, "    <nodeclass count=\""+transNumber+"\" name=\"Transition\">");
			int globalTransId = 0;
			for(Transition t : transitions) {
				SnoopyTransition sTransition = new SnoopyTransition(t);
				snoopyTransitions.add(sTransition);
				currentActiveID = sTransition.writeTransitionInfoToFile(bw, currentActiveID, globalTransId);
				currentActiveID ++;
				globalTransId++;
			}
			write(bw, "    </nodeclass>");
			
			
			
			write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			write(bw, "  </nodeclasses>");
			write(bw, "  <edgeclasses count=\"1\">");
			write(bw, "    <edgeclass count=\"0\" name=\"Edge\"/>");
			write(bw, "  </edgeclasses>");
			
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();
		} catch (Exception e) {
			
		}
	}
	
	private void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception e) {
			
		}
	}
	
	private void writeEnding(BufferedWriter bw) {
		try {
			write(bw, "  <metadataclasses count=\"3\">");
			write(bw, "    <metadataclass count=\"1\" name=\"General\">");
			write(bw, "      <metadata id=\"212\" net=\"1\">");
			write(bw, "        <attribute name=\"Name\" id=\"213\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"3.00\" x=\"20.00\" y=\"20.00\" id=\"214\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Created\" id=\"215\" net=\"1\">");
			write(bw, "          <![CDATA["+dateAndTime+"]]>"); //ZMIENNA
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"20.00\" x=\"42.00\" y=\"40.00\" id=\"216\" net=\"1\" show=\"0\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Authors\" id=\"217\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"40.00\" x=\"42.00\" y=\"60.00\" id=\"218\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Keywords\" id=\"219\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"40.00\" yoff=\"25.00\" x=\"57.00\" y=\"45.00\" id=\"220\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"Description\" id=\"221\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"40.00\" x=\"42.00\" y=\"60.00\" id=\"222\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <attribute name=\"References\" id=\"223\" net=\"1\">");
			write(bw, "          <![CDATA[]]>");
			write(bw, "          <graphics count=\"1\">");
			write(bw, "            <graphic xoff=\"25.00\" yoff=\"40.00\" x=\"42.00\" y=\"60.00\" id=\"224\" net=\"1\" show=\"1\" grparent=\"225\" state=\"1\" pen=\"0,0,0\" brush=\"255,255,255\"/>");
			write(bw, "          </graphics>");
			write(bw, "        </attribute>");
			write(bw, "        <graphics count=\"1\">");
			write(bw, "          <graphic x=\"17.00\" y=\"20.00\" id=\"225\" net=\"1\" show=\"1\" w=\"15.00\" h=\"24.00\" state=\"1\" pen=\"255,255,255\" brush=\"255,255,255\"/>");
			write(bw, "        </graphics>");
			write(bw, "      </metadata>");
			write(bw, "    </metadataclass>");
			write(bw, "    <metadataclass count=\"0\" name=\"Comment\"/>");
			write(bw, "    <metadataclass count=\"0\" name=\"Constant Class\"/>");
			write(bw, "  </metadataclasses>");
		} catch (Exception e) {
			
		}
	}
}
