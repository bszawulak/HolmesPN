package abyss.windows;

import javax.swing.JFrame;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.Node;
import abyss.math.Place;

public class AbyssNodeInfoAction {
	private JFrame parentFrame;
	
	public AbyssNodeInfoAction(JFrame papa) {
		parentFrame = papa;
	}

	public void changeName(Place place, String newName) {
		place.setName(newName);
		repaintGraphPanel(place);
	}
	
	/**
	 * Metoda odpowiedzialna za przerysowanie grafu obrazu w arkuszu sieci.
	 */
	private void repaintGraphPanel(Node node) {
		int sheetIndex = GUIManager.getDefaultGUIManager().IDtoIndex(node.getElementLocations().get(0).getSheetID());
		GraphPanel graphPanel = GUIManager.getDefaultGUIManager().getWorkspace().getSheets().get(sheetIndex).getGraphPanel();
		graphPanel.repaint();
	}

	public void parentTableUpdate(JFrame parentFrame, String name) {
		if(parentFrame instanceof AbyssNetTables) {
			((AbyssNetTables)parentFrame).updateRow(name, 1);
			//jeśli macierzyste oknow dla AbyssNodeInfo to AbyssNetTables, wtedy jeszcze
			//wywołujemy metodę aktualizującą nazwę miejsca którą właśnie zmieniliśmy, ale
			//bez potrzeby przeładowywania całej tabeli danych
		}
		
	}
}
