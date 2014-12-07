package abyss.math;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

//import org.simpleframework.xml.Element;

import abyss.darkgui.GUIManager;
import abyss.workspace.Workspace;

/**
 * Klasa s�u��ca do przechowywania lokalizacji wierzcho�ka oraz przy��czonych
 * do niej �uk�w.
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
	 * Konstruktor obiekt�w ElementLocation.
	 * @param sheetId int - identyfikator arkusza w programie
	 * @param position Point - punkt lokalizacji
	 * @param parentNode Node - wierzcho�ek, do kt�rego nale�y lokalizacja
	 */
	public ElementLocation(int sheetId, Point position, Node parentNode) {
		this.position = position;
		this.notSnappedPosition = position;
		this.sheetId = sheetId;
		this.parentNode = parentNode;
	}
	
	/**
	 * Metoda pozwala pobra� identyfikator arkusza.
	 * @return int - identyfikator arkusza
	 */
	public int getSheetID() {
		return sheetId;
	}

	/**
	 * Metoda pozwala ustawi� identyfikator arkusza.
	 * @param sheetId int - identyfikator arkusza
	 */
	public void setSheetID(int sheetId) {
		this.sheetId = sheetId;
	}

	/**
	 * Metoda pozwala pobra� punkt lokalizacji.
	 * @return position Point - punkt lokalizacji
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Metoda pozwala ustawi� punkt lokalizacji.
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
	 * Metoda pozwala pobra� wierzcho�ek, do kt�rego nale�y lokalizacja.
	 * @return Node - wierzcho�ek, do kt�rego nale�y lokalizacja
	 */
	public Node getParentNode() {
		return parentNode;
	}

	/**
	 * Metoda pozwala ustawi� wierzcho�ek, do kt�rego nale�y lokalizacja.
	 * @param parentNode Node - wierzcho�ek, do kt�rego nale�y lokalizacja 
	 */
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * Metoda przesuwa wsp�rz�dne x i y lokalizacji punktu (w�a�ciwie wektora).
	 * @param delta Point - wektor przesuni�cia
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
	 * Metoda przesuwa wsp�rz�dne x i y lokalizacji punktu (w�a�ciwie wektora)
	 * bior�c dodatkowo pod uwag� warto�ci meshSize.
	 * @param delta Point - wektor przesuni�cia
	 * @param meshSize int - domy�lnie 20, zale�enie od ruchu myszy
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
	 * Metoda pozwala pobra� list� �uk�w wyj�ciowych.
	 * @return ArrayList[Arc] - lista �uk�w wyj�ciowych
	 */
	public ArrayList<Arc> getOutArcs() {
		return outArcs;
	}

	/**
	 * Metoda pozwala ustawi� list� �uk�w wyj�ciowych
	 * @param outArcs ArrayList[Arc] - lista �uk�w wyj�ciowych do zast�pienia aktualnej
	 */
	public void setOutArcs(ArrayList<Arc> outArcs) {
		this.outArcs = outArcs;
	}

	/**
	 * Metoda pozwala pobra� list� �uk�w wej�ciowych.
	 * @return ArrayList[Arc] - lista �uk�w wej�ciowych
	 */
	public ArrayList<Arc> getInArcs() {
		return inArcs;
	}

	/**
	 * Metoda pozwala ustawi� list� �uk�w wej�ciowych.
	 * @param inArcs ArrayList[Arc] - lista �uk�w wej�ciowych do zast�pienia aktualnej
	 */
	public void setInArcs(ArrayList<Arc> inArcs) {
		this.inArcs = inArcs;
	}

	/**
	 * Metoda pozwala doda� �uk wej�ciowy.
	 * @param a Arc - �uk wej�ciowy do dodania
	 */
	public void addInArc(Arc a) {
		this.getInArcs().add(a);
	}

	/**
	 * Metoda pozwala usun�� �uk wej�ciowy.
	 * @param a Arc - �uk wej�ciowy do usuni�cia
	 */
	public void removeInArc(Arc a) {
		this.getInArcs().remove(a);
	}

	/**
	 * Metoda pozwala doda� �uk wyj�ciowy.
	 * @param a Arc - �uk wyj�ciowy
	 */
	public void addOutArc(Arc a) {
		this.getOutArcs().add(a);
	}

	/**
	 * Metoda pozwala usun�� �uk wyj�ciowy.
	 * @param a Arc - �uk wyj�ciowy do usuni�cia
	 */
	public void removeOutArc(Arc a) {
		this.getOutArcs().remove(a);
	}

	/**
	 * Metoda pozwala sprawdzi�, czy lokalizacja jest oznaczona jako wybrana.
	 * @return boolean - true, je�li lokalizacja jest oznaczona jako wybrana; 
	 * 		false w przeciwnym wypadku
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Metoda pozwala oznaczy� lokalizacj� jako wybran�, b�d� tak� warto�� odznaczy�.
	 * @param isSelected boolean - warto�� oznaczenia lokalizacji jako wybranej, true je�li zaznaczona
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
	 * Metoda pozwala sprawdzi�, czy istnieje inna lokacja nale��ca do tego
	 * samego wierzcho�ka (portal), kt�ra jest aktualnie oznaczona jako wybrana.
	 * @return boolean - true, je�li istnieje oznaczony jako wybrany odpowiedni portal
	 * 		false w przeciwnym wypadku
	 */
	public boolean isPortalSelected() {
		return isPortalSelected;
	}

	/**
	 * Metoda pozwala oznaczy� portal jako wybrany, b�d� tak� warto�� odznaczy�.
	 * @param isPortalSelected boolean - true / false
	 */
	public void setPortalSelected(boolean isPortalSelected) {
		this.isPortalSelected = isPortalSelected;
	}

	/**
	 * Metoda zamieniaj�ca dane o kraw�dzi sieci na �a�cuch znak�w.
	 * @return String - �a�cuch znak�w
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
