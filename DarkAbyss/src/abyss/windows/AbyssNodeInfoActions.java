package abyss.windows;

import javax.swing.JFrame;

import abyss.darkgui.GUIManager;
import abyss.graphpanel.GraphPanel;
import abyss.math.Node;
import abyss.math.Place;

/**
 * Klasa użytkowa dla AbyssNodeInfo, zawiera metody wywoływane interfejsem opisanym w ramach
 * AbyssNodeInfo.
 * @author MR
 *
 */
public class AbyssNodeInfoActions {
	@SuppressWarnings("unused")
	private JFrame parentFrame;
	
	public AbyssNodeInfoActions(JFrame papa) {
		parentFrame = papa;
	}

	/**
	 * Metoda zmieniająca nazwę dla miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param newName String - nowa nazwa
	 */
	public void changeName(Node place, String newName) {
		place.setName(newName);
		repaintGraphPanel(place);
	}
	
	/**
	 * Metoda zmieniająca komentarz dla miejsca sieci.
	 * @param place Place - obiekt miejsca
	 * @param newComment String - nowa nazwa
	 */
	public void changeComment(Node place, String newComment) {
		place.setComment(newComment);
	}
	
	/**
	 * Metoda ustawia nową liczbę tokenów dla miejsca.
	 * @param place Place - obiekt miejsca
	 * @param tokens int - nowa liczba tokenów
	 */
	public void setTokens(Place place, int tokens) {
		place.setTokensNumber(tokens);
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

	/**
	 * Metoda odpowiedzialna za aktualizację komórki tabeli wyświetlającej nazwę miejsca i tranzycji.
	 * @param parentFrame JFrame - obiekt okna wywołującego dla AnyssNodeInfo
	 * @param name String - nowa nazwa
	 */
	public void parentTableUpdate(JFrame parentFrame, String name) {
		if(parentFrame instanceof AbyssNetTables) {
			((AbyssNetTables)parentFrame).updateRow(name, 1);
			//jeśli macierzyste oknow dla AbyssNodeInfo to AbyssNetTables, wtedy jeszcze
			//wywołujemy metodę aktualizującą nazwę miejsca którą właśnie zmieniliśmy, ale
			//bez potrzeby przeładowywania całej tabeli danych
			
			//P.S. NIE, NIE MOŻNA ZASTĄPIĆ PRZEZ this.parentFrame! Nie ten obiekt!
		}
	}
}
