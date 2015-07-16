package abyss.math;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;

//import org.simpleframework.xml.Element;

import abyss.darkgui.GUIManager;
import abyss.workspace.Workspace;

/**
 * Klasa służąca do przechowywania lokalizacji wierzchołka oraz przyłączonych do niej łuków.
 * 
 * @author students
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
	
	private ArrayList<Arc> metaInArcs = new ArrayList<Arc>();
	private ArrayList<Arc> metaOutArcs = new ArrayList<Arc>();

	/**
	 * Konstruktor obiektów ElementLocation.
	 * @param sheetId int - identyfikator arkusza w programie
	 * @param position Point - punkt lokalizacji
	 * @param parentNode Node - wierzchołek, do którego należy lokalizacja
	 */
	public ElementLocation(int sheetId, Point position, Node parentNode) {
		this.position = position;
		this.notSnappedPosition = position;
		this.sheetId = sheetId;
		this.parentNode = parentNode;
	}
	
	/**
	 * Metoda pozwala pobrać identyfikator arkusza.
	 * @return int - identyfikator arkusza
	 */
	public int getSheetID() {
		return sheetId;
	}

	/**
	 * Metoda pozwala ustawić identyfikator arkusza.
	 * @param sheetId int - identyfikator arkusza
	 */
	public void setSheetID(int sheetId) {
		this.sheetId = sheetId;
	}

	/**
	 * Metoda pozwala pobrać punkt lokalizacji.
	 * @return position Point - punkt lokalizacji
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Metoda pozwala ustawić punkt lokalizacji.
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
	 * Ustawia nową lokalizację bez sprawdzania czy leży w zakresie widoczności.
	 * @param position Point - x,y
	 */
	public void forceSetPosition(Point position) {
		this.position = position;
	}

	/**
	 * Metoda pozwala pobrać wierzchołek, do którego należy lokalizacja.
	 * @return Node - wierzchołek, do którego należy lokalizacja
	 */
	public Node getParentNode() {
		return parentNode;
	}

	/**
	 * Metoda pozwala ustawić wierzchołek, do którego należy lokalizacja.
	 * @param parentNode Node - wierzchołek, do którego należy lokalizacja 
	 */
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * Metoda przesuwa współrzędne x i y lokalizacji punktu (właściwie wektora).
	 * @param delta Point - wektor przesunięcia
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
	 * Metoda na potrzeby klasy ProjectReader.
	 * @param p Point - x, y
	 */
	public void setNotSnappedPosition(Point p) {
		notSnappedPosition = p;
	}

	/**
	 * Metoda przesuwa współrzędne x i y lokalizacji punktu (właściwie wektora)
	 * biorąc dodatkowo pod uwagę wartości meshSize.
	 * @param delta Point - wektor przesunięcia
	 * @param meshSize int - domyślnie 20, zależnie od ruchu myszy
	 */
	public void updateLocationWithMeshSnap(Point delta, int meshSize) {
		Point notSnPos = notSnappedPosition;
		notSnappedPosition.setLocation(notSnPos.x + delta.x, notSnPos.y + delta.y);
		Point tempPosition = new Point((notSnappedPosition.x + delta.x) / meshSize * meshSize, (notSnappedPosition.y + delta.y) / meshSize * meshSize);
		Workspace workspace = GUIManager.getDefaultGUIManager().getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(tempPosition)) {
			position = tempPosition;
		}
	}

	/**
	 * Metoda pozwala pobrać listę łuków wyjściowych.
	 * @return ArrayList[Arc] - lista łuków wyjściowych
	 */
	public ArrayList<Arc> getOutArcs() {
		return outArcs;
	}

	/**
	 * Metoda pozwala ustawić listę łuków wyjściowych
	 * @param outArcs ArrayList[Arc] - lista łuków wyjściowych do zastąpienia aktualnej
	 */
	public void setOutArcs(ArrayList<Arc> outArcs) {
		this.outArcs = outArcs;
	}

	/**
	 * Metoda pozwala pobrać listę łuków wejściowych.
	 * @return ArrayList[Arc] - lista łuków wejściowych
	 */
	public ArrayList<Arc> getInArcs() {
		return inArcs;
	}

	/**
	 * Metoda pozwala ustawić listę łuków wejściowych.
	 * @param inArcs ArrayList[Arc] - lista łuków wejściowych do zastąpienia aktualnej
	 */
	public void setInArcs(ArrayList<Arc> inArcs) {
		this.inArcs = inArcs;
	}

	/**
	 * Metoda pozwala dodać łuk wejściowy.
	 * @param a Arc - łuk wejściowy do dodania
	 */
	public void addInArc(Arc a) {
		this.inArcs.add(a);
	}

	/**
	 * Metoda pozwala usunąć łuk wejściowy.
	 * @param a Arc - łuk wejściowy do usunięcia
	 */
	public void removeInArc(Arc a) {
		this.inArcs.remove(a);
	}

	/**
	 * Metoda pozwala dodać łuk wyjściowy.
	 * @param a Arc - łuk wyjściowy
	 */
	public void addOutArc(Arc a) {
		this.outArcs.add(a);
	}

	/**
	 * Metoda pozwala usunąć łuk wyjściowy.
	 * @param a Arc - łuk wyjściowy do usunięcia
	 */
	public void removeOutArc(Arc a) {
		this.outArcs.remove(a);
	}
	
	public ArrayList<Arc> accessMetaInArcs() {
		return this.metaInArcs;
	}
	
	public ArrayList<Arc> accessMetaOutArcs() {
		return this.metaOutArcs;
	}

	/**
	 * Metoda pozwala sprawdzić, czy lokalizacja jest oznaczona jako wybrana.
	 * @return boolean - true, jeśli lokalizacja jest oznaczona jako wybrana; 
	 * 		false w przeciwnym wypadku
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Metoda pozwala oznaczyć lokalizację jako wybraną, bądź taką wartość odznaczyć.
	 * @param isSelected boolean - wartość oznaczenia lokalizacji jako wybranej, true jeśli zaznaczona
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
	 * Metoda pozwala sprawdzić, czy istnieje inna lokacja należąca do tego
	 * samego wierzchołka (portal), która jest aktualnie oznaczona jako wybrana.
	 * @return boolean - true, jeśli istnieje oznaczony jako wybrany odpowiedni portal
	 * 		false w przeciwnym wypadku
	 */
	public boolean isPortalSelected() {
		return isPortalSelected;
	}

	/**
	 * Metoda pozwala oznaczyć portal jako wybrany, bądź taką wartość odznaczyć.
	 * @param isPortalSelected boolean - true / false
	 */
	public void setPortalSelected(boolean isPortalSelected) {
		this.isPortalSelected = isPortalSelected;
	}

	/**
	 * Metoda zamieniająca dane o krawędzi sieci na łańcuch znaków.
	 * @return String - łańcuch znaków
	 */
	public String toString() {
		String s = "";
		if(this.getParentNode() == null) {
			s = "ParentNode: null"
					+ "SheetID: " + this.getSheetID() + "; Position: "
					+ pointPos(this.getPosition()) 
					+ "; inArcs: "+ Integer.toString(this.getInArcs().size())
					+ "; outArcs: " + Integer.toString(this.getOutArcs().size());
		} else {
			s = "Node: " + this.getParentNode() +" [gID:"+this.getParentNode().getID()+" ];\n "
					+ "SheetID: " + this.getSheetID() + "; Position: "
					+ pointPos(this.getPosition()) 
					+ "; inArcs: "+ Integer.toString(this.getInArcs().size())
					+ "; outArcs: " + Integer.toString(this.getOutArcs().size());
		}
		return s;
	}
	
	private String pointPos(Point p) {
		if(p != null)
			return "["+p.x+","+p.y+"]";
		else
			return "[null, null]";
	}
}
