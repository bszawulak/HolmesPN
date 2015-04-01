package abyss.varia;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.ElementLocation;
import abyss.math.Node;

public class NetworkTransformations {

	public NetworkTransformations() {
		
	}
	
	public void extendNetwork() {
		ArrayList<Node> nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		int maxH = 0;
		int maxW = 0;
		
		for(Node n : nodes) {
			
			for(ElementLocation el : n.getElementLocations()) {
				Point oldOne = el.getPosition();
				double x = oldOne.getX();
				double y = oldOne.getY();
				
				x *= 1.1;
				y *= 1.1;
				
				if(maxH < y)
					maxH = (int) y;
				if(maxW < x)
					maxW = (int) x;
				
				el.getPosition().setLocation(x, y);
			}
		}
		
		GraphPanel gp = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getGraphPanel(0);
		gp.setOriginSize(new Dimension(maxW+100, maxH+100));
		gp.setSize(new Dimension(maxW+100, maxH+100));
		//GUIManager.getDefaultGUIManager().get
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
}
