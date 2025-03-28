package holmes.varia;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.GraphPanel;
import holmes.petrinet.elements.*;
import holmes.workspace.WorkspaceSheet;

/**
 * Klasa przekształceń elementów graficznych sieci.
 */
public class NetworkTransformations {
	public NetworkTransformations() {
		
	}
	
	/**
	 * Metoda zmienia rozmiar całej sieci na 10% większy lub mniejszy. 
	 * @param magnify (<b>boolean</b>) true, jeśli zwiększamy sieć o 10%, false jeśli zmniejszamy o 10%.
	 */
	public void extendNetwork(boolean magnify) {
		ArrayList<Node> nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();
		ArrayList<ArrayList<Integer>> sheetsToChange = prepareChangeMatrix();

		for(Node n : nodes) {	
			for(ElementLocation el : n.getElementLocations()) {
				Point oldOne = el.getPosition();
				double x = oldOne.getX();
				double y = oldOne.getY();
				
				if(magnify) {
					x *= 1.1;
					y *= 1.1;
				} else {
					x /= 1.1;
					y /= 1.1;
				}
				
				int sheetID = GUIManager.getDefaultGUIManager().IDtoIndex(el.getSheetID());
				if(sheetsToChange.get(sheetID).get(1) < x) {
					sheetsToChange.get(sheetID).set(0, sheetID);
					sheetsToChange.get(sheetID).set(1, (int)x);
				}
				if(sheetsToChange.get(sheetID).get(2) < y) {
					sheetsToChange.get(sheetID).set(0, sheetID);
					sheetsToChange.get(sheetID).set(2, (int)y);
				}
				el.getPosition().setLocation(x, y);

				if(n instanceof Transition) {
					//nieistotne nawet, że to tranzycja, mogłoby być miejsce - istotne jest to, zeby nie
					//wywoływać poniższych pętli dwa razy, dwa miejsca I dla tranzycji, pomiędzy którymi
					//byłby ten sam łuk przecież (tylko przeciwnie skierowany).
					for(Arc arc : el.getInArcs()) {
						arc.updateAllBreakPointsLocationsNetExtension(magnify);
					}
					for(Arc arc : el.getOutArcs()) {
						arc.updateAllBreakPointsLocationsNetExtension(magnify);
					}
				}
			}

		}
		
		//resize all valid panels:
		for(ArrayList<Integer> sheetData : sheetsToChange) {
			if(sheetData.get(0) > -1) {
				GraphPanel gp = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetData.get(0)).getGraphPanel();
				gp.setOriginSize(new Dimension(sheetData.get(1)+100, sheetData.get(2)+100));
				gp.setSize(new Dimension(sheetData.get(1)+100, sheetData.get(2)+100));
				
				GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetData.get(0)).getContainerPanel().setSize(
						new Dimension(sheetData.get(1)+100, sheetData.get(2)+100));
			}
		}

		alignNetToGrid();
		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	public void alignNetToGrid() {
		ArrayList<Node> nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();

		for(Node n : nodes) {	
			for(ElementLocation el : n.getElementLocations()) {
				Point oldXY = el.getPosition();
				el.setPosition(alignToGrid(oldXY));

				for(Arc arc : el.getInArcs()) {
					arc.alignBreakPoints();
				}
				for(Arc arc : el.getOutArcs()) {
					arc.alignBreakPoints();
				}
			}
		}
	}
	
	/**
	 * Metoda pomocnicza zwracająca nowy obiekt punktu o znormalizowanych współrzędnych względem siatki 20/20px
	 * @param coordinates Point - oryginalny punkt;
	 * @return Point - znormalizowane współrzędne
	 */
	public static Point alignToGrid(Point coordinates) {
		int x = coordinates.x;
		int y = coordinates.y;
		
		int deviationX = x % 20;
		if(deviationX <= 10) {
			x -= deviationX;
		} else {
			x += (20-deviationX);
		}
		
		int deviationY = y % 20;
		if(deviationY <= 10) {
			y -= deviationY;
		} else {
			y += (20-deviationY);
		}
		
		if(x==0) x = 20;
		if(y==0) y = 20;

		return new Point(x,y);
	}

	/**
	 * Przygotowuje macierz pomocniczą do przechowywania danych o max(x) i max(y) arkuszy.
	 * @return (<b>ArrayList[ArrayList[Integer]]</b>) każdy wiersz to arkusz od 0 do max(ID arkuszy).
	 */
	private ArrayList<ArrayList<Integer>> prepareChangeMatrix() {
		//przygotowanie tablicy arkuszy do zmiany rozmiary:
		ArrayList<ArrayList<Integer>> sheetsToChange = new ArrayList<ArrayList<Integer>>();
		ArrayList<WorkspaceSheet> sheets = GUIManager.getDefaultGUIManager().getWorkspace().getSheets();
		
		for(int i=0; i<sheets.size(); i++) {
			ArrayList<Integer> points = new ArrayList<Integer>();
			points.add(-1); //sheetID
			points.add(-1); //width
			points.add(-1); //height
			sheetsToChange.add(points);
		}
		return sheetsToChange;
	}
}
