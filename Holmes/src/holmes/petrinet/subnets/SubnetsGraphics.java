package holmes.petrinet.subnets;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.darkgui.LanguageManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.ElementLocation;
import holmes.petrinet.elements.MetaNode;
import holmes.petrinet.elements.Node;
import holmes.petrinet.elements.MetaNode.MetaType;
import holmes.workspace.Workspace;

/**
 * Klasa odpowiedzialna za niektóre operacje graficzne związane z sieciami hierachicznymi.
 */
public class SubnetsGraphics {
	private static LanguageManager lang = GUIManager.getLanguageManager();
	/**
	 * Konstruktor klasy HierarchicalGraphics;
	 */
	public SubnetsGraphics() {
		
	}
	
	/**
	 * Metoda dodaje brakujące panele graficzne sieci.
	 */
	public void addRequiredSheets() {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetsNumber = workspace.getSheets().size();
		ArrayList<Node> nodes = workspace.getProject().getNodes();
		
		int subNets = 0;
		for(Node node : nodes) {
			for(ElementLocation el : node.getElementLocations()) {
				if(el.getSheetID() > subNets)
					subNets = el.getSheetID();
			}
		}
		subNets++;
		
		int metaNodesNumber = 0;
		ArrayList<MetaNode> metanodes = workspace.getProject().getMetaNodes();
		for(MetaNode metanode : metanodes) {
			if(metanode.getRepresentedSheetID() > metaNodesNumber)
				metaNodesNumber = metanode.getRepresentedSheetID();
		}
		metaNodesNumber += 1; //zero się nie liczy
		
		if(metaNodesNumber > subNets)
			subNets = metaNodesNumber;
		
		for(int s = sheetsNumber; s<subNets; s++) {
			GUIManager.getDefaultGUIManager().getWorkspace().newTab(false, new Point(0,0), 1, MetaType.SUBNET);
		}
	}
	
	/**
	 * Metoda usuwająca zbędne zakładki sieci (puste).
	 */
	public void collapseSubnets() {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		ArrayList<Node> nodes = workspace.getProject().getNodes();
		ArrayList<Integer> subnetsVector = new ArrayList<Integer>();
		subnetsVector.add(0);
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int shId = el.getSheetID();
				if(shId > subnetsVector.size()-1) {
					int diff = shId - (subnetsVector.size() - 1);
					updateVector(subnetsVector, diff, 0);
				}
				int oldVal = subnetsVector.get(shId) + 1;
				subnetsVector.set(shId, oldVal);
			}
		}
		
		int sheetsNumber = workspace.getSheets().size();
		int shNumber = subnetsVector.size();
		if(sheetsNumber > shNumber) {
			SubnetsGraphics.updateVector(subnetsVector, sheetsNumber - shNumber, 0);
		}
		
		//teraz w subnetsVector jest tyle wartości ile podsieci, każda wartość to liczba elementów podsieci
		ArrayList<Integer> indices = new ArrayList<Integer>();
		int emptyNetToRemove = 0;
		for(int i=0; i<subnetsVector.size(); i++) {
			indices.add(i);
			if(subnetsVector.get(i) == 0)
				emptyNetToRemove++;
		}
		int originalSize = subnetsVector.size();
		
		for(int i=0; i<subnetsVector.size(); i++) {
			if(subnetsVector.get(i) > 0)
				continue;
			else {
				for(int j=i+1; j<subnetsVector.size(); j++) {
					if(subnetsVector.get(j) == 0)
						continue;
					else { //podmień - kompresuj
						subnetsVector.set(i, subnetsVector.get(j));
						subnetsVector.set(j, 0);
						indices.set(i, indices.get(j));
						indices.set(j, -1);
						break;
					}
				}
			}
		}
		
		for(int i=0; i<emptyNetToRemove; i++) {
			int lastPos = subnetsVector.size() - 1;
			subnetsVector.remove(lastPos);
			indices.remove(lastPos);
		}
		//teraz mamy macierz pomocniczą - indices - jej wartości to numery sieci w ElementLocation's, które
		//należy podmienić na indeksy wartości z indices (na lokalizację tychże wartości).
		//boolean criticalError = false;
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int sheetID = el.getSheetID();
				int val = indices.get(indices.indexOf(sheetID));
				if(val == indices.indexOf(sheetID)) //nie ma co podmnieniać
					continue;
				if(val == -1) {
					//criticalError = true;
					continue;
				}
				
				el.setSheetID(indices.indexOf(sheetID));
			}
			for(ElementLocation el: n.getTextsLocations(GUIManager.locationMoveType.NAME)) {
				int sheetID = el.getSheetID();
				int val = indices.get(indices.indexOf(sheetID));
				if(val == indices.indexOf(sheetID)) //nie ma co podmieniać
					continue;
				if(val == -1) {
					//criticalError = true;
					continue;
				}
				
				el.setSheetID(indices.indexOf(sheetID));
			}
			
			if(n instanceof MetaNode) {
				int oldRepresentedSheet = ((MetaNode)n).getRepresentedSheetID();
				int val = indices.indexOf(oldRepresentedSheet);
				if(val == -1) {
					//criticalError = true;
					continue;
				}
				//int val = indices.get(indices.indexOf(oldRepresentedSheet));
				((MetaNode)n).setRepresentedSheetID(val);
			}
		}
		
		
		
		int leaveAlone = originalSize - emptyNetToRemove;
		//pray...
		workspace.getProject().repaintAllGraphPanels();
		
		//int dockableSize = workspace.getDockables().size();
		/*
		CompositeDock parentOfFirst = workspace.getDockables().get(0).getDock().getParentDock();
		for(int d=dockableSize-1; d>=leaveAlone; d--) {
			ArrayList<Dockable> dockables = workspace.getDockables();
			Dockable dockable = dockables.get(d);
			String x = dockable.getID();
			if(x.equals("Sheet 0")) {
				continue;
			}
			workspace.deleteTab(dockable, true);
			//d++;
			//dockableSize++;
			
			if(dockable.getDock().getParentDock().equals(parentOfFirst))
				GUIManager.getDefaultGUIManager().globalSheetsList.remove(dockable);
		}
		 */
	}
	
	/**
	 * Metoda przesuwa wszystkie elementy o odpowiednią odległość do lewego górnego rogu panelu.
	 */
	public void realignElements() {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		ArrayList<Node> nodes = workspace.getProject().getNodes();
		ArrayList<Integer> vectorW = new ArrayList<Integer>();
		ArrayList<Integer> vectorH = new ArrayList<Integer>();
		
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int sheetID = el.getSheetID();
				if(sheetID > vectorW.size()-1) {
					updateVector(vectorW, sheetID - vectorW.size() + 1, 99999);
					updateVector(vectorH, sheetID - vectorH.size() + 1, 99999);
				}
				if(el.getPosition().x <  vectorW.get(sheetID)) {
					vectorW.set(sheetID, el.getPosition().x);
				}
				if(el.getPosition().y <  vectorH.get(sheetID)) {
					vectorH.set(sheetID, el.getPosition().y);
				}
			}
		}
		
		for(Node n : nodes) {
			for(ElementLocation el: n.getElementLocations()) {
				int sheetID = el.getSheetID();
				Point oldPoint = el.getPosition();
				int newWidth = oldPoint.x - vectorW.get(sheetID) + 100;
				int newHeight = oldPoint.y - vectorH.get(sheetID) + 100;
				el.getPosition().setLocation(newWidth, newHeight);
			}
		}
		
		workspace.getProject().repaintAllGraphPanels();
	}
	
	/**
	 * Metoda odpowiedzialna za dostosowanie rozmiarów paneli sieci do zawartości.
	 */
	public void resizePanels() {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetsNumber = workspace.getSheets().size();
		ArrayList<Node> nodes = workspace.getProject().getNodes();
		
		ArrayList<Dimension> hidden = new ArrayList<Dimension>();
		hidden.add(new Dimension(1650, 1200));
		for(int net=1; net<sheetsNumber; net++) {
			hidden.add(new Dimension(1650, 1200));
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
				GraphPanel graphPanel = workspace.getSheets().get(net).getGraphPanel();
				
				int width = hidden.get(net).width;
				int height = hidden.get(net).height;
				graphPanel.setSize(new Dimension(width+200, height+100));
				graphPanel.setOriginSize(new Dimension(width+200, height+100));
			}
				
		} catch (Exception e) {
			GUIManager.getDefaultGUIManager().log(lang.getText("LOGentry00419exception"), "error", true);	
		}
	}
	
	/**
	 * Metoda pomocnicza dla collapseSubnets.
	 * @param vector ArrayList[Integer] - wektor podsieci
	 * @param howMany int - o ile powiększyć wektor
	 * @param initValue int - jaka wartość początkowa
	 */
	public static void updateVector(ArrayList<Integer> vector, int howMany, int initValue) {
		for(int i=0; i<howMany; i++) {
			vector.add(initValue);
		}
	}
}

