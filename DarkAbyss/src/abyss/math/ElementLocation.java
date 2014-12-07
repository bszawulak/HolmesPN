package abyss.math;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

//import org.simpleframework.xml.Element;

import abyss.darkgui.GUIManager;
import abyss.workspace.Workspace;

/**
 * Klasa s³u¿¹ca do przechowywania lokalizacji wierzcho³ka oraz przy³¹czonych
 * do niej ³uków.
 * @author students
 *
 */
public class ElementLocation implements Serializable {
	private static final long serialVersionUID = 2775375770782696276L;
	private int sheetId;
	private Point position;
	private Point notSnappedPosition;
	private Node parentNode;
	private boolean isSelected = false;
	private boolean isPortalSelected = false;
	private ArrayList<Arc> inArcs = new ArrayList<Arc>();
	private ArrayList<Arc> outArcs = new ArrayList<Arc>();

	/**
	 * Konstruktor obiektów ElementLocation.
	 * @param sheetId int - identyfikator arkusza w programie
	 * @param position Point - punkt lokalizacji
	 * @param parentNode Node - wierzcho³ek, do którego nale¿y lokalizacja
	 */
	public ElementLocation(int sheetId, Point position, Node parentNode) {
		this.position = position;
		this.notSnappedPosition = position;
		this.sheetId = sheetId;
		this.parentNode = parentNode;
	}
	
	/**
	 * Metoda pozwala pobraæ identyfikator arkusza.
	 * @return int - identyfikator arkusza
	 */
	public int getSheetID() {
		return sheetId;
	}

	/**
	 * Metoda pozwala ustawiæ identyfikator arkusza.
	 * @param sheetId int - identyfikator arkusza
	 */
	public void setSheetID(int sheetId) {
		this.sheetId = sheetId;
	}

	/**
	 * Metoda pozwala pobraæ punkt lokalizacji.
	 * @return position Point - punkt lokalizacji
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Metoda pozwala ustawiæ punkt lokalizacji.
	 * @param position Point - punkt lokalizacji
	 */
	public void setPosition(Point position) {
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(position)) {
			this.position = position;
		}
	}

	/**
	 * Metoda pozwala pobraæ wierzcho³ek, do którego nale¿y lokalizacja.
	 * @return Node - wierzcho³ek, do którego nale¿y lokalizacja
	 */
	public Node getParentNode() {
		return parentNode;
	}

	/**
	 * Metoda pozwala ustawiæ wierzcho³ek, do którego nale¿y lokalizacja.
	 * @param parentNode Node - wierzcho³ek, do którego nale¿y lokalizacja 
	 */
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * Metoda przesuwa wspó³rzêdne x i y lokalizacji punktu (w³aœciwie wektora).
	 * @param delta Point - wektor przesuniêcia
	 */
	public void updateLocation(Point delta) {
		Point tempPosition = new Point(position.x + delta.x, position.y + delta.y);
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(tempPosition)) {
			position = tempPosition;
		}
	}

	/**
	 * Metoda przesuwa wspó³rzêdne x i y lokalizacji punktu (w³aœciwie wektora)
	 * bior¹c dodatkowo pod uwagê wartoœci meshSize.
	 * @param delta Point - wektor przesuniêcia
	 * @param meshSize int - domyœlnie 20, zale¿enie od ruchu myszy
	 */
	public void updateLocationWithMeshSnap(Point delta, int meshSize) {
		notSnappedPosition.setLocation(notSnappedPosition.x + delta.x, notSnappedPosition.y + delta.y);
		Point tempPosition = new Point((notSnappedPosition.x + delta.x) / meshSize * meshSize, (notSnappedPosition.y + delta.y) / meshSize * meshSize);
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(tempPosition)) {
			position = tempPosition;
		}
	}

	/**
	 * Metoda pozwala pobraæ listê ³uków wyjœciowych.
	 * @return ArrayList[Arc] - lista ³uków wyjœciowych
	 */
	public ArrayList<Arc> getOutArcs() {
		return outArcs;
	}

	/**
	 * Metoda pozwala ustawiæ listê ³uków wyjœciowych
	 * @param outArcs ArrayList[Arc] - lista ³uków wyjœciowych do zast¹pienia aktualnej
	 */
	public void setOutArcs(ArrayList<Arc> outArcs) {
		this.outArcs = outArcs;
	}

	/**
	 * Metoda pozwala pobraæ listê ³uków wejœciowych.
	 * @return ArrayList[Arc] - lista ³uków wejœciowych
	 */
	public ArrayList<Arc> getInArcs() {
		return inArcs;
	}

	/**
	 * Metoda pozwala ustawiæ listê ³uków wejœciowych.
	 * @param inArcs ArrayList[Arc] - lista ³uków wejœciowych do zast¹pienia aktualnej
	 */
	public void setInArcs(ArrayList<Arc> inArcs) {
		this.inArcs = inArcs;
	}

	/**
	 * Metoda pozwala dodaæ ³uk wejœciowy.
	 * @param a Arc - ³uk wejœciowy do dodania
	 */
	public void addInArc(Arc a) {
		this.getInArcs().add(a);
	}

	/**
	 * Metoda pozwala usun¹æ ³uk wejœciowy.
	 * @param a Arc - ³uk wejœciowy do usuniêcia
	 */
	public void removeInArc(Arc a) {
		this.getInArcs().remove(a);
	}

	/**
	 * Metoda pozwala dodaæ ³uk wyjœciowy.
	 * @param a Arc - ³uk wyjœciowy
	 */
	public void addOutArc(Arc a) {
		this.getOutArcs().add(a);
	}

	/**
	 * Metoda pozwala usun¹æ ³uk wyjœciowy.
	 * @param a Arc - ³uk wyjœciowy do usuniêcia
	 */
	public void removeOutArc(Arc a) {
		this.getOutArcs().remove(a);
	}

	/**
	 * Metoda pozwala sprawdziæ, czy lokalizacja jest oznaczona jako wybrana.
	 * @return boolean - true, jeœli lokalizacja jest oznaczona jako wybrana; 
	 * 		false w przeciwnym wypadku
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Metoda pozwala oznaczyæ lokalizacjê jako wybran¹, b¹dŸ tak¹ wartoœæ odznaczyæ.
	 * @param isSelected boolean - wartoœæ oznaczenia lokalizacji jako wybranej, true jeœli zaznaczona
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		this.setPortalSelected(false);
		if (getParentNode().checkAllPortalsSelection())
			getParentNode().selectAllPortals();
		else
			getParentNode().deselectAllPortals();
	}

	/**
	 * Metoda pozwala sprawdziæ, czy istnieje inna lokacja nale¿¹ca do tego
	 * samego wierzcho³ka (portal), która jest aktualnie oznaczona jako wybrana.
	 * @return boolean - true, jeœli istnieje oznaczony jako wybrany odpowiedni portal
	 * 		false w przeciwnym wypadku
	 */
	public boolean isPortalSelected() {
		return isPortalSelected;
	}

	/**
	 * Metoda pozwala oznaczyæ portal jako wybrany, b¹dŸ tak¹ wartoœæ odznaczyæ.
	 * @param isPortalSelected boolean - true / false
	 */
	public void setPortalSelected(boolean isPortalSelected) {
		this.isPortalSelected = isPortalSelected;
	}

	/**
	 * Metoda zamieniaj¹ca dane o krawêdzi sieci na ³añcuch znaków.
	 * @return String - ³añcuch znaków
	 */
	public String toString() {
		String s = "sheetID: " + this.getSheetID() + "; position"
				+ this.getPosition().toString() + "; parentNodeID"
				+ this.getParentNode().getID() + "; inArcsCount: "
				+ Integer.toString(this.getInArcs().size())
				+ "; outArcsCount: "
				+ Integer.toString(this.getOutArcs().size());
		return s;
	}
}
