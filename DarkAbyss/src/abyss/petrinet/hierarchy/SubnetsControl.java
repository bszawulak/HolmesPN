package abyss.petrinet.hierarchy;

import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.petrinet.elements.ElementLocation;
import abyss.petrinet.elements.Node;

/**
 * Klasa odpowiedzialna za metody pomagające w kontrolowaniu sieci hierarchicznych. Albo przynajmniej
 * udawaniu że taka kontrola istnieje.
 * 
 * @author MR
 */
public class SubnetsControl {
	GUIManager overlord = null;

	/**
	 * Konstruktor obiektu klasy SubnetsControl.
	 * @param boss GUIManager - obiekt okna głównego
	 */
	public SubnetsControl(GUIManager boss) {
		this.overlord = boss;
	}
	
	public ArrayList<ElementLocation> getSubnetElementLocations(int sheetID) {
		ArrayList<ElementLocation> result = new ArrayList<ElementLocation>();
		ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
		for(Node n : nodes) {
			for(ElementLocation el : n.getElementLocations()) {
				if(el.getSheetID() == sheetID)
					result.add(el);
			}
		}
		return result;
	}
	
	/**
	 * Metoda zwraca wektor z ilością elementów dla każdej podsieci.
	 * @return ArrayList[Integer] - wektor liczności elementów dla podsieci
	 */
	public ArrayList<Integer> getSubnetsVector() {
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Node> nodes = overlord.getWorkspace().getProject().getNodes();
		for(Node n : nodes) {
			for(ElementLocation el : n.getElementLocations()) {
				int sheetID = el.getSheetID();
				int shNumber = result.size();
				if(sheetID > shNumber-1) {
					HierarchicalGraphics.updateVector(result, sheetID - shNumber + 1, 0);
				}
				
				int value = result.get(sheetID) + 1;
				result.set(sheetID, value);
			}
		}
		return result;
	}
}
