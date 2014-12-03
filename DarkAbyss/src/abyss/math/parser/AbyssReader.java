package abyss.math.parser;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.Arc;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.math.PetriNetData;

public class AbyssReader {

	private ArrayList<Node> nodeArray = new ArrayList<Node>();
	private ArrayList<Arc> arcArray = new ArrayList<Arc>();

	public void read(String sciezka) {

		File source = new File(sciezka);

		try {

			XStream xstream = new XStream(new StaxDriver());
			xstream.alias("petriNet", PetriNetData.class);
			PetriNetData PND = (PetriNetData) xstream.fromXML(source);
			int SID = GUIManager.getDefaultGUIManager().getWorkspace().getProject().checkSheetID();
			for(Node n : PND.nodes)
				for(ElementLocation el : n.getElementLocations())
					el.setSheetID(SID);
			setWorkframeBoundary(PND.nodes);
			getNodeArray().addAll(PND.nodes);
			getArcArray().addAll(PND.arcs);
			xstream.fromXML(source);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
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
		
	}

	public ArrayList<Node> getNodeArray() {
		return nodeArray;
	}

	public void setNodeArray(ArrayList<Node> nodeArray) {
		this.nodeArray = nodeArray;
	}

	public ArrayList<Arc> getArcArray() {
		return arcArray;
	}

	public void setArcArray(ArrayList<Arc> arcArray) {
		this.arcArray = arcArray;
	}
}
