package abyss.varia;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.ElementLocation;
import abyss.math.Node;
import abyss.workspace.WorkspaceSheet;

/**
 * Klasa przekształceń graficznych i strukturalnych sieci.
 * @author MR
 *
 */
public class NetworkTransformations {

	public NetworkTransformations() {
		
	}
	
	/**
	 * Metoda zmienia rozmiar całej sieci na 10% większy lub mniejszy. 
	 * @param magnify boolean - true, jeśli zwiększamy sieć o 10%, false jeśli zmniejszamy o 10%
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
				
				if(sheetID >= sheetsToChange.size()) {
					@SuppressWarnings("unused")
					int xx=1;
				}
				
				if(sheetsToChange.get(sheetID).get(1) < x) {
					sheetsToChange.get(sheetID).set(0, sheetID);
					sheetsToChange.get(sheetID).set(1, (int)x);
				}
				if(sheetsToChange.get(sheetID).get(2) < y) {
					sheetsToChange.get(sheetID).set(0, sheetID);
					sheetsToChange.get(sheetID).set(2, (int)y);
				}
				
				el.getPosition().setLocation(x, y);
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
		

		GUIManager.getDefaultGUIManager().getWorkspace().getProject().repaintAllGraphPanels();
	}
	
	public void alignNetToGrid() {
		ArrayList<Node> nodes = GUIManager.getDefaultGUIManager().getWorkspace().getProject().getNodes();

		for(Node n : nodes) {	
			for(ElementLocation el : n.getElementLocations()) {
				Point oldXY = el.getPosition();
				el.setPosition(alignToGrid(oldXY));
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
	 * Przygotowuje macierz pomocniczą do przechowywania danych o max(x) i max(y) arkuszy
	 * @return ArrayList[ArrayList[Integer]] - każdy wiersz to arkusz od 0 do max(ID arkuszy)
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
