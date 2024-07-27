package holmes.files.io;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.data.IdGenerator;
import holmes.petrinet.data.PetriNetData;
import holmes.petrinet.elements.Arc;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.PetriNetElement.PetriNetElementType;

/**
 * Klasa odpowiedzialna za odczyt plików projektu.
 */
public class AbyssReader {
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
	private static final LanguageManager lang = GUIManager.getLanguageManager();
	private ArrayList<Node> nodeArray = new ArrayList<Node>();
	private ArrayList<Arc> arcArray = new ArrayList<Arc>();
	private String pnName;

	/**
	 * Metoda odpowiedzialna za czytanie plików projektu formatu .abyss.
	 * @param path String - ścieżka dostępu do pliku
	 */
	public void read(String path) {
		File source = new File(path);
		try {
			XStream xstream = new XStream(new StaxDriver());
			xstream.alias("petriNet", PetriNetData.class);
			PetriNetData PND = (PetriNetData) xstream.fromXML(source);
			int SID = overlord.getWorkspace().getProject().returnCleanSheetID();
			
			int maxPlaceId = 0;
			int maxTransitionId = 0;
			int maxGlobalId = 0;
			
			for(Node n : PND.nodes) {
				if(n.getType() == PetriNetElementType.PLACE) { //przywracanie ID
					if(n.getID() > maxPlaceId)
						maxPlaceId = n.getID();
					if(n.getID() > maxGlobalId)
						maxGlobalId = n.getID();
				} else { //jakakolwiek tranzycja
					if(n.getID() > maxTransitionId)
						maxTransitionId = n.getID();
					if(n.getID() > maxGlobalId)
						maxGlobalId = n.getID();
				}
				for(ElementLocation el : n.getElementLocations()) 
					el.setSheetID(SID);
			}
			for(Arc n : PND.arcs) {
				if(n.getID() > maxGlobalId)
					maxGlobalId = n.getID();
			}
			
			setWorkframeBoundary(PND.nodes); //ustawianie szerokości okna
			getNodeArray().addAll(PND.nodes);
			getArcArray().addAll(PND.arcs);
			pnName = PND.netName;
			
			IdGenerator.setTransitionId(maxTransitionId+1);
			IdGenerator.setPlaceId(maxPlaceId+1);
			IdGenerator.setStartId(maxGlobalId+1);

			overlord.getWorkspace().getProject().accessStatesManager().createCleanStatePN();
			overlord.getWorkspace().getProject().accessSSAmanager().createCleanSSAvector();
			overlord.getWorkspace().getProject().accessFiringRatesManager().createCleanSPNdataVector();
			
			xstream.fromXML(source);
			overlord.log(lang.getText("LOGentry00153")+" "+path, "text", true);
		} catch (Exception e) {
			overlord.log(lang.getText("LOGentry00154exception")+"\n"+e.getMessage(), "error", true);
		}
	}
	
	/**
	 * Metoda pomocnicza ustawiająca rozmiar obszaru rysowania wczytanej sieci.
	 * @param aln ArrayList[Node] - elementy z lokalizacjami
	 */
	private void setWorkframeBoundary(ArrayList<Node> aln) {
		int x=0;
		int y=0;

		for (Node node : aln) {
			ArrayList<ElementLocation> elementLocationList = node.getElementLocations();
			for (ElementLocation elementLocation : elementLocationList) {
				if (x < elementLocation.getPosition().x)
					x = elementLocation.getPosition().x;
				if (y < elementLocation.getPosition().y)
					y = elementLocation.getPosition().y;
			}
		}
		
		GraphPanel graphPanel = overlord.getWorkspace().getSheets().get(0).getGraphPanel();
		graphPanel.setSize(new Dimension(x + 90, y + 90));
		
		graphPanel.setOriginSize(graphPanel.getSize());
	}
	
	public String getPNname() {
		return pnName;
	}

	/**
	 * Metoda zwraca listę węzłów sieci.
	 * @return ArrayList[Node] - wierzchołki sieci
	 */
	public ArrayList<Node> getNodeArray() {
		return nodeArray;
	}

	/**
	 * Metoda ustawia nową listę wierzchołków sieci.
	 * @param nodeArray ArrayList[Node] - nowe wierzchołki sieci
	 */
	public void setNodeArray(ArrayList<Node> nodeArray) {
		this.nodeArray = nodeArray;
	}

	/**
	 * Metdoa zwraca listę łuków sieci.
	 * @return ArrayList[Arc] - lista łuków
	 */
	public ArrayList<Arc> getArcArray() {
		return arcArray;
	}

	/**
	 * Metoda ustawia nową listę łuków sieci.
	 * @param arcArray ArrayList[Arc] - nowa lista łuków
	 */
	public void setArcArray(ArrayList<Arc> arcArray) {
		this.arcArray = arcArray;
	}
}
