package abyss.files.io;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.graphpanel.IdGenerator;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.PetriNetData;
import abyss.math.PetriNetElement.PetriNetElementType;

/**
 * Klasa odpowiedzialna za odczyt plików projektu.<br><br>
 *
 * Poprawiono:<br>
 * wczytywanie pliku - uaktualnianie zmiennych ID generatora
 * @author students
 * @author MR
 */
public class AbyssReader {

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
			int SID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().returnCleanSheetID();
			
			//IdGenerator.setStartId(0);
			//IdGenerator.setPlaceId(0);
			//IdGenerator.setTransitionId(0);
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
			
			xstream.fromXML(source);
			GUIManager.getDefaultGUIManager().log("Petri net (.abyss) successfully read from file "+path, "text", true);
		} catch (Exception e) {
			e.printStackTrace();
			GUIManager.getDefaultGUIManager().log("Error: " + e.getMessage(), "error", true);
		}

	}
	
	/**
	 * Metoda pomocnicza ustawiająca rozmiar obszaru rysowania wczytanej sieci.
	 * @param aln ArrayList[Node] - elementy z lokalizacjami
	 */
	private void setWorkframeBoundary(ArrayList<Node> aln)
	{
		int x=0;
		int y=0;
		
		for(int i =0; i< aln.size();i++)
		{
			ArrayList<ElementLocation> elementLocationList = aln.get(i).getElementLocations();
			for (int j = 0; j < elementLocationList.size();j++)
			{
				if(x<elementLocationList.get(j).getPosition().x)
					x=elementLocationList.get(j).getPosition().x;
				if(y<elementLocationList.get(j).getPosition().y)
					y=elementLocationList.get(j).getPosition().y;
			}					
		}
		
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(0).getGraphPanel();
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
