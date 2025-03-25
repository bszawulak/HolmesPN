package holmes.files.io.snoopy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Place;
import holmes.petrinet.elements.Transition;
import holmes.petrinet.elements.Arc.TypeOfArc;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.varia.Check;

/**
 * Autor włożył ogromny wysiłek, aby ta klasa wraz z pomocniczymi potrafiła zasymulować
 * obłęd twórców programu Snoopy, który objawia się na każdym etapie zapisu danych
 * sieci do pliku. I nawet nie chodzi o ręczne symulowanie parsera, który jest tam
 * użyty. Ten parser działa błędnie. Np. komentarze dla tranzycji są przesuniętę względem
 * osi oX, a powinny oY jak dla miejsc. Poza tym parser pluje danymi jak karabin maszynowy,
 * z czego 60% tych danych jest redundantnych, a kilka zupełnie ignorowanych przez Snoopiego
 * wczytującego tenże plik (np. punkty startu i końca dla łuków).
 */
public class SnoopyWriter {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private ArrayList<Place> places;
	private ArrayList<Transition> transitions;
	private ArrayList<MetaNode> metanodes;
	private ArrayList<Arc> arcs;
	private ArrayList<MetaNode> coarsePlaces = new ArrayList<MetaNode>();
	private ArrayList<MetaNode> coarseTransitions = new ArrayList<MetaNode>();
	
	private ArrayList<SnoopyWriterPlace> snoopyWriterPlaces;
	private ArrayList<Integer> holmesPlacesID = new ArrayList<Integer>();
	private ArrayList<SnoopyWriterTransition> snoopyWriterTransitions;
	private ArrayList<Integer> holmesTransitionsID = new ArrayList<Integer>();
	private ArrayList<SnoopyWriterCoarse> snoopyWriterCoarsePlaces;
	private ArrayList<Integer> holmesCoarsePlacesID = new ArrayList<Integer>();
	private ArrayList<SnoopyWriterCoarse> snoopyWriterCoarseTransitions;
	private ArrayList<Integer> holmesCoarseTransitionsID = new ArrayList<Integer>();

	/**
	 * Konstruktor obiektu klasy SnoopyWriter uzyskujący dostęp do zasobów sieci.
	 */
	public SnoopyWriter() {
		places = overlord.getWorkspace().getProject().getPlaces();
		transitions = overlord.getWorkspace().getProject().getTransitions();
		metanodes = overlord.getWorkspace().getProject().getMetaNodes();
		for(MetaNode meta : metanodes) {
			if(meta.getMetaType() == MetaType.SUBNETPLACE)
				coarsePlaces.add(meta);
			if(meta.getMetaType() == MetaType.SUBNETTRANS)
				coarseTransitions.add(meta);
		}
		
		arcs = overlord.getWorkspace().getProject().getArcs(); 

		snoopyWriterPlaces = new ArrayList<SnoopyWriterPlace>();
		snoopyWriterTransitions = new ArrayList<SnoopyWriterTransition>();
		snoopyWriterCoarsePlaces = new ArrayList<SnoopyWriterCoarse>();
		snoopyWriterCoarseTransitions = new ArrayList<SnoopyWriterCoarse>();
	}
	
	/**
	 * Metoda realizująca zapis do pliku SPPED. Działa - 03.01.2015. I na tym się zatrzymajmy w opisach.
	 * @return boolean - status operacji: true jeśli nie było problemów  (buahahahahahaha)
	 */
	public boolean writeSPPED(String filePath) {
		boolean status = overlord.subnetsHQ.checkSnoopyCompatibility();
		if(!status) {
			JOptionPane.showMessageDialog(null, lang.getText("SW_entry001"), 
					lang.getText("problem"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//int startNodeId = 226; // bo tak
		int currentActiveID = 226;
		int arcsNumber = 0;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
			
			//NAGŁÓWEK:
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
				SnoopyWriterPlace sPlace = new SnoopyWriterPlace(p);
				snoopyWriterPlaces.add(sPlace);
				holmesPlacesID.add(p.getID());
				
				ArrayList<ElementLocation> clones = p.getElementLocations();
				for(ElementLocation el : clones) {
					arcsNumber += el.getOutArcs().size(); //pobież wszystkie wychodzące
				}
				
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				
				if(sPlace.portal) { //jeśli właśnie dodane było portalem
					currentActiveID += 13; //bo tak, pytajcie w Brandenburgu 'a czymuuu?'
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
				SnoopyWriterTransition sTransition = new SnoopyWriterTransition(t);
				snoopyWriterTransitions.add(sTransition);
				holmesTransitionsID.add(t.getID());
				
				ArrayList<ElementLocation> clones = t.getElementLocations();
				for(ElementLocation el : clones) {
					arcsNumber += el.getOutArcs().size(); //pobież wszystkie wychodzące
				}
				
				currentActiveID = sTransition.writeTransitionInfoToFile(bw, currentActiveID, globalTransId);
				currentActiveID ++;
				globalTransId++;
			}
			write(bw, "    </nodeclass>");
			
			//TEGO NA RAZIE NIE RUSZAMY (DA BÓG: NIGDY)
			//21-07-2015 you wish...
			boolean weAreInDeepShit = false;
			if(coarsePlaces.isEmpty()) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			} else {
				weAreInDeepShit = true;
				int coarsePnumber = coarsePlaces.size();
				write(bw, "    <nodeclass count=\""+coarsePnumber+"\" name=\"Coarse Place\">");
				int globalCoarsePlaceId = 0;
				
				for(MetaNode m : coarsePlaces) {
					SnoopyWriterCoarse sCoarseP = new SnoopyWriterCoarse(m);
					snoopyWriterCoarsePlaces.add(sCoarseP);
					holmesCoarsePlacesID.add(m.getID());
					currentActiveID = sCoarseP.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarsePlaceId);
					currentActiveID ++;
					globalCoarsePlaceId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			if(coarseTransitions.isEmpty()) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			} else {
				weAreInDeepShit = true;
				int coarseTnumber = coarseTransitions.size();
				write(bw, "    <nodeclass count=\""+coarseTnumber+"\" name=\"Coarse Transition\">");
				int globalCoarseTransitionId = 0;
				
				for(MetaNode m : coarseTransitions) {
					SnoopyWriterCoarse sCoarseT = new SnoopyWriterCoarse(m);
					snoopyWriterCoarseTransitions.add(sCoarseT);
					holmesCoarseTransitionsID.add(m.getID());
					currentActiveID = sCoarseT.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarseTransitionId);
					currentActiveID ++;
					globalCoarseTransitionId++;
				}
				write(bw, "    </nodeclass>");
			}
			write(bw, "  </nodeclasses>");
			
			//ŁUKI:
			SnoopyWriterArc arcWriter = new SnoopyWriterArc(places, transitions, metanodes, arcs, coarsePlaces, coarseTransitions
					, snoopyWriterPlaces, holmesPlacesID, snoopyWriterTransitions, holmesTransitionsID
					, snoopyWriterCoarsePlaces, holmesCoarsePlacesID, snoopyWriterCoarseTransitions, holmesCoarseTransitionsID);
			
			write(bw, "  <edgeclasses count=\"1\">");
			write(bw, "    <edgeclass count=\"" + arcsNumber + "\" name=\"Edge\">");
			if(weAreInDeepShit)
				arcWriter.addArcsAndCoarseToFile(bw, currentActiveID, false);
			else
				arcWriter.addArcsToFile(bw, currentActiveID);
			write(bw, "    </edgeclass>");
			write(bw, "  </edgeclasses>");
			
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();

			overlord.log(lang.getText("SW_entry002")+" "+filePath, "text", true);
			overlord.markNetSaved();
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00113_1exception")+" "+filePath, "error", true);
			overlord.log(e.getMessage(), "error", false);
			return false;
		}
	}
	
	/**
	 * Metoda realizująca zapis do pliku SPEPT - sieci rozszerzone.
	 * @return boolean - status operacji: true jeśli nie było problemów  (buachachachachachacha)
	 */
	public boolean writeSPEPT(String filePath) {
		boolean status = overlord.subnetsHQ.checkSnoopyCompatibility();
		if(!status) {
			JOptionPane.showMessageDialog(null, lang.getText("SW_entry003"), 
					lang.getText("problem"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//int startNodeId = 226; // bo tak
		int currentActiveID = 226;
		//int arcsNumber = 0;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
			
			//NAGŁÓWEK:
			write(bw, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			write(bw, "<?xml-stylesheet type=\"text/xsl\" href=\"/xsl/spped2svg.xsl\"?>");
			write(bw, "<Snoopy version=\"2\" revision=\"1.13\">");
			
			write(bw, "  <netclass name=\"Extended Petri Net\"/>");
			write(bw, "  <nodeclasses count=\"4\">"); //zawsze 4
			
			//MIEJSCA:
			int placesNumber = places.size();
			write(bw, "    <nodeclass count=\""+placesNumber+"\" name=\"Place\">");
			int globalPlaceId = 0;
			for(Place p : places) {
				SnoopyWriterPlace sPlace = new SnoopyWriterPlace(p);
				snoopyWriterPlaces.add(sPlace);
				holmesPlacesID.add(p.getID());
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				if(sPlace.portal) { //jeśli właśnie dodane było portalem
					currentActiveID += 13; //bo tak, 13, pytajcie w Brandenburgu 'a czymuuu?' Nie ja pisałem Snoopiego.
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
				SnoopyWriterTransition sTransition = new SnoopyWriterTransition(t);
				snoopyWriterTransitions.add(sTransition);
				holmesTransitionsID.add(t.getID());
				currentActiveID = sTransition.writeTransitionInfoToFile(bw, currentActiveID, globalTransId);
				currentActiveID ++;
				globalTransId++;
			}
			write(bw, "    </nodeclass>");
			
			boolean weAreInDeepShit = false;
			if(coarsePlaces.isEmpty()) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			} else {
				int coarsePnumber = coarsePlaces.size();
				write(bw, "    <nodeclass count=\""+coarsePnumber+"\" name=\"Coarse Place\">");
				int globalCoarsePlaceId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarsePlaces) {
					SnoopyWriterCoarse sCoarseP = new SnoopyWriterCoarse(m);
					snoopyWriterCoarsePlaces.add(sCoarseP);
					holmesCoarsePlacesID.add(m.getID());		
					currentActiveID = sCoarseP.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarsePlaceId);
					currentActiveID ++;
					globalCoarsePlaceId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			if(coarseTransitions.isEmpty()) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			} else {
				int coarseTnumber = coarseTransitions.size();
				write(bw, "    <nodeclass count=\""+coarseTnumber+"\" name=\"Coarse Transition\">");
				int globalCoarseTransitionId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarseTransitions) {
					SnoopyWriterCoarse sCoarseT = new SnoopyWriterCoarse(m);
					snoopyWriterCoarseTransitions.add(sCoarseT);
					holmesCoarseTransitionsID.add(m.getID());
					currentActiveID = sCoarseT.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarseTransitionId);
					currentActiveID ++;
					globalCoarseTransitionId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			write(bw, "  </nodeclasses>");
			
			//ŁUKI:
			SnoopyWriterArc arcWriter = new SnoopyWriterArc(places, transitions, metanodes, arcs, coarsePlaces, coarseTransitions
					, snoopyWriterPlaces, holmesPlacesID, snoopyWriterTransitions, holmesTransitionsID
					, snoopyWriterCoarsePlaces, holmesCoarsePlacesID, snoopyWriterCoarseTransitions, holmesCoarseTransitionsID);
			
			write(bw, "  <edgeclasses count=\"5\">");
			
			ArrayList<Integer> arcClasses = Check.getArcClassCount();
			if(arcClasses.get(0) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Edge\"/>");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(0) + "\" name=\"Edge\">");
				if(weAreInDeepShit)
					currentActiveID = arcWriter.addArcsAndCoarseToFile(bw, currentActiveID, true, TypeOfArc.NORMAL, arcClasses.get(0));
				else
					currentActiveID = arcWriter.addArcsInfoExtended(bw, currentActiveID, TypeOfArc.NORMAL, arcClasses.get(0));
				write(bw, "    </edgeclass>");
			}
		
			if(arcClasses.get(1) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Read Edge\"/>");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(1) + "\" name=\"Read Edge\">");
				if(weAreInDeepShit)
					currentActiveID = arcWriter.addArcsAndCoarseToFile(bw, currentActiveID, true, TypeOfArc.READARC, arcClasses.get(1));
				else
					currentActiveID = arcWriter.addArcsInfoExtended(bw, currentActiveID, TypeOfArc.READARC, arcClasses.get(1));
				write(bw, "    </edgeclass>");
			}
			
			if(arcClasses.get(2) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Inhibitor Edge\"/>");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(2) + "\" name=\"Inhibitor Edge\">");
				if(weAreInDeepShit)
					currentActiveID = arcWriter.addArcsAndCoarseToFile(bw, currentActiveID, true, TypeOfArc.INHIBITOR, arcClasses.get(2));
				else
					currentActiveID = arcWriter.addArcsInfoExtended(bw, currentActiveID, TypeOfArc.INHIBITOR, arcClasses.get(2));
				write(bw, "    </edgeclass>");
			}
			
			if(arcClasses.get(3) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Reset Edge\"/>");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(3) + "\" name=\"Reset Edge\">");
				if(weAreInDeepShit)
					currentActiveID = arcWriter.addArcsAndCoarseToFile(bw, currentActiveID, true, TypeOfArc.RESET, arcClasses.get(3));
				else
					currentActiveID = arcWriter.addArcsInfoExtended(bw, currentActiveID, TypeOfArc.RESET, arcClasses.get(3));
				write(bw, "    </edgeclass>");
			}
			
			if(arcClasses.get(4) == 0) {
				write(bw, "    <edgeclass count=\"0\" name=\"Equal Edge\"/>");
			} else {
				write(bw, "    <edgeclass count=\"" + arcClasses.get(4) + "\" name=\"Equal Edge\">");
				if(weAreInDeepShit)
					currentActiveID = arcWriter.addArcsAndCoarseToFile(bw, currentActiveID, true, TypeOfArc.EQUAL, arcClasses.get(4));
				else
					currentActiveID = arcWriter.addArcsInfoExtended(bw, currentActiveID, TypeOfArc.EQUAL, arcClasses.get(4));
				write(bw, "    </edgeclass>");
			}

			write(bw, "  </edgeclasses>");
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();

			overlord.log(lang.getText("SW_entry004")+" "+filePath, "text", true);
			overlord.markNetSaved();
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00114exception")+" "+filePath, "error", true);
			overlord.log(e.getMessage(), "error", false);
			return false;
		}
	}

	/**
	 * Metoda realizująca zapis do pliku SPTPT - sieci czasowe. 
	 * @return boolean - status operacji: true jeśli nie było problemów
	 */
	public boolean writeSPTPT(String filePath) {
		boolean status = overlord.subnetsHQ.checkSnoopyCompatibility();
		if(!status) {
			JOptionPane.showMessageDialog(null, lang.getText("SW_entry005"), 
					lang.getText("problem"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		//int startNodeId = 226; // bo tak
		int currentActiveID = 226;
		int arcsNumber = 0;
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
			
			//NAGŁÓWEK:
			write(bw, "<?xml version=\"1.0\" encoding=\"utf-8\"?>");
			write(bw, "<?xml-stylesheet type=\"text/xsl\" href=\"/xsl/spped2svg.xsl\"?>");
			write(bw, "<Snoopy version=\"2\" revision=\"1.13\">");
			
			write(bw, "  <netclass name=\"Time Petri Net\"/>");
			write(bw, "  <nodeclasses count=\"4\">"); //zawsze 4
			
			//MIEJSCA:
			int placesNumber = places.size();
			write(bw, "    <nodeclass count=\""+placesNumber+"\" name=\"Place\">");
			int globalPlaceId = 0;
			for(Place p : places) {
				SnoopyWriterPlace sPlace = new SnoopyWriterPlace(p);
				snoopyWriterPlaces.add(sPlace);
				holmesPlacesID.add(p.getID());
				
				ArrayList<ElementLocation> clones = p.getElementLocations();
				for(ElementLocation el : clones) {
					arcsNumber += el.getOutArcs().size(); //pobież wszystkie wychodzące
				}
				
				currentActiveID = sPlace.writePlaceInfoToFile(bw, currentActiveID, globalPlaceId);
				
				if(sPlace.portal) { //jeśli właśnie dodane było portalem
					currentActiveID += 13; //bo tak, pytajcie 'a czymu?' w Brandenburgu 
				} else {
					currentActiveID ++;
				}
				globalPlaceId++;
				
			}
			write(bw, "    </nodeclass>");
			
			// TRANZYCJE:
			//TODO:
			int transNumber = transitions.size();
			write(bw, "    <nodeclass count=\""+transNumber+"\" name=\"Transition\">");
			int globalTransId = 0;
			for(Transition t : transitions) {
				SnoopyWriterTimeTransition sTransition = new SnoopyWriterTimeTransition(t);
				snoopyWriterTransitions.add(sTransition);
				holmesTransitionsID.add(t.getID());
				
				ArrayList<ElementLocation> clones = t.getElementLocations();
				for(ElementLocation el : clones) {
					arcsNumber += el.getOutArcs().size(); //pobież wszystkie wychodzące
				}
				
				currentActiveID = sTransition.writeTransitionInfoToFile(bw, currentActiveID, globalTransId);
				currentActiveID ++;
				globalTransId++;
			}
			write(bw, "    </nodeclass>");
			
			boolean weAreInDeepShit = false;
			if(coarsePlaces.isEmpty()) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Place\"/>");
			} else {
				int coarsePnumber = coarsePlaces.size();
				write(bw, "    <nodeclass count=\""+coarsePnumber+"\" name=\"Coarse Place\">");
				int globalCoarsePlaceId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarsePlaces) {
					SnoopyWriterCoarse sCoarseP = new SnoopyWriterCoarse(m);
					snoopyWriterCoarsePlaces.add(sCoarseP);
					holmesCoarsePlacesID.add(m.getID());
					currentActiveID = sCoarseP.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarsePlaceId);
					currentActiveID ++;
					globalCoarsePlaceId++;
				}
				write(bw, "    </nodeclass>");
			}
			
			if(coarseTransitions.isEmpty()) {
				write(bw, "    <nodeclass count=\"0\" name=\"Coarse Transition\"/>");
			} else {
				int coarseTnumber = coarseTransitions.size();
				write(bw, "    <nodeclass count=\""+coarseTnumber+"\" name=\"Coarse Transition\">");
				int globalCoarseTransitionId = 0;
				weAreInDeepShit = true;
				for(MetaNode m : coarseTransitions) {
					SnoopyWriterCoarse sCoarseT = new SnoopyWriterCoarse(m);
					snoopyWriterCoarseTransitions.add(sCoarseT);
					holmesCoarseTransitionsID.add(m.getID());
					currentActiveID = sCoarseT.writeMetaNodeInfoToFile(bw, currentActiveID, globalCoarseTransitionId);
					currentActiveID ++;
					globalCoarseTransitionId++;
				}
				write(bw, "    </nodeclass>");
			}
			write(bw, "  </nodeclasses>");
			
			//ŁUKI:
			SnoopyWriterArc arcWriter = new SnoopyWriterArc(places, transitions, metanodes, arcs, coarsePlaces, coarseTransitions
					, snoopyWriterPlaces, holmesPlacesID, snoopyWriterTransitions, holmesTransitionsID
					, snoopyWriterCoarsePlaces, holmesCoarsePlacesID, snoopyWriterCoarseTransitions, holmesCoarseTransitionsID);
			
			write(bw, "  <edgeclasses count=\"1\">");
			write(bw, "    <edgeclass count=\"" + arcsNumber + "\" name=\"Edge\">");
			if(weAreInDeepShit)
				arcWriter.addArcsAndCoarseToFile(bw, currentActiveID, false);
			else
				arcWriter.addArcsToFile(bw, currentActiveID);
			write(bw, "    </edgeclass>");
			write(bw, "  </edgeclasses>");
			
			writeEnding(bw);
			bw.write("</Snoopy>\n");
			bw.close();

			overlord.log(lang.getText("SW_entry006")+" "+filePath, "text", true);
			overlord.markNetSaved();
			return true;
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00115exception")+" "+filePath, "error", true);
			overlord.log(e.getMessage(), "error", false);
			return false;
		}
	}

	/**
	 * Metoda realizuje zapis pojedyńczej linii do pliku - zakończonej enterem.
	 * @param bw BufferedWriter - obiekt zapisujący
	 * @param text String - linia
	 */
	private void write(BufferedWriter bw, String text) {
		try {
			bw.write(text+"\n");
		} catch (Exception ignored) {

		}
	}
	
	/**
	 * Metoda ta zapisuje końcówkę pliku sieci SPPED.
	 * @param bw BufferedWriter - obiekt zapisujący
	 */
	private void writeEnding(BufferedWriter bw) {
		//private String dateAndTime = "2015-01-02 10:44:56";
		DateFormat writeFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		String dateAndTime = writeFormat.format(date);
		
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
			overlord.log(lang.getText("LOGentry00116exception")+"\n"+e.getMessage(), "error", true);
		}
	}
}
