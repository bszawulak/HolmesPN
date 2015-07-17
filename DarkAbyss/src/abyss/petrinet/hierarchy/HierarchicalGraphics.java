package abyss.petrinet.hierarchy;

import java.awt.Dimension;
import java.util.ArrayList;

import com.javadocking.dock.CompositeDock;
import com.javadocking.dockable.Dockable;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.Node;
import abyss.workspace.Workspace;

/**
 * Klasa odpowiedzialna za niektóre operacje graficzne związane z sieciami hierachicznymi.
 *  
 * @author MR
 */
public class HierarchicalGraphics {

	public HierarchicalGraphics() {
		
	}
	
	public void resizePanels() {
		int sheetsNumber = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().size();
		ArrayList<Node> nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		
		ArrayList<Dimension> hidden = new ArrayList<Dimension>();
		for(int net=0; net<sheetsNumber; net++) {
			hidden.add(new Dimension(200,200));
		}
		
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int subNet = el.getSheetID();
				if(hidden.get(subNet).getWidth() < el.getPosition().x)
					hidden.get(subNet).setSize(el.getPosition().x, hidden.get(subNet).getHeight());
			
				if(hidden.get(subNet).getHeight() < el.getPosition().y)
					hidden.get(subNet).setSize(hidden.get(subNet).getWidth(), el.getPosition().y);
			}
		}
		
		try {
			for(int net=0; net<sheetsNumber; net++) {
				GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(net).getGraphPanel();
				
				int width = hidden.get(net).width;
				int height = hidden.get(net).height;
				graphPanel.setSize(new Dimension(width+200, height+100));
			}
				
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log("Error: cannot resize sheets.", "error", true);	
		}
	}
	
	public void collapseSubnets() {
		ArrayList<Node> nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		ArrayList<Integer> subnetsVector = new ArrayList<Integer>();
		subnetsVector.add(0);
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int shId = el.getSheetID();
				if(shId > subnetsVector.size()-1) {
					int diff = shId - (subnetsVector.size() - 1);
					updateVector(subnetsVector, diff);
				}
				int oldVal = subnetsVector.get(shId) + 1;
				subnetsVector.set(shId, oldVal);
			}
		}
		//teraz w subnetsVector jest tyle wartości ile podsieci, każda wartość to liczba elementów podsieci
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for(int i=0; i<subnetsVector.size(); i++) {
			indices.add(i);
		}
		int oryginalSize = subnetsVector.size();
		int changes = 0;
		for(int i=0; i<subnetsVector.size(); i++) {
			if(subnetsVector.get(i) > 0)
				continue;
			else {
				for(int j=i+1; j<subnetsVector.size(); j++) {
					if(subnetsVector.get(j) == 0)
						continue;
					else { //podmień - kompresuj
						subnetsVector.set(i, subnetsVector.get(j));
						indices.set(i, indices.get(j));
						changes++;
						break;
					}
				}
			}
		}
		for(int i=0; i<changes; i++) {
			int lastPos = subnetsVector.size() - 1;
			subnetsVector.remove(lastPos);
			indices.remove(lastPos);
		}
		//teraz mamy macierz pomocniczą - indices - jej wartości to numery sieci w ElementLocation's, które
		//należy podmienić na indeksy wartości z indices (na lokalizację tychże wartości).
		
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int sheetID = el.getSheetID();
				int val = indices.get(indices.indexOf(sheetID));
				if(val == indices.indexOf(sheetID)) //nie ma co podmnieniać
					continue;
				
				el.setSheetID(indices.indexOf(sheetID));
			}
		}
		
		
		
		int leaveAlone = oryginalSize - changes;
		//pray...
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		workspace.getProject().repaintAllGraphPanels();
		
		int dockableSize = workspace.getDockables().size();
		CompositeDock parentOfFirst = workspace.getDockables().get(0).getDock().getParentDock();
		for(int d=dockableSize-1; d>=leaveAlone; d--) {
			Dockable dockable = workspace.getDockables().get(d);
			String x = dockable.getID();
			if(x.equals("Sheet 0")) {
				continue;
			}
			workspace.deleteTab(dockable, true);
			d--;
			dockableSize--;
			
			if(dockable.getDock().getParentDock().equals(parentOfFirst))
				GUIManager.getDefaultGUIManager().globalSheetsList.remove(dockable);
		}
	}
	
	private void updateVector(ArrayList<Integer> vector, int howMany) {
		for(int i=0; i<howMany; i++) {
			vector.add(0);
		};
	}
}

