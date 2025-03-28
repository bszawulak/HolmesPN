package holmes.petrinet.elements;

import java.awt.Point;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.workspace.Workspace;

/**
 * Klasa służąca do przechowywania lokalizacji wierzchołka oraz przyłączonych do niej łuków. Jej
 * obiekty latają w programie z lewa na prawo, każdy obiekt T/P/M ma przynajmniej kilka. Każy łuk
 * ma dwa - start i end EL.
 */
public class ElementLocation implements Serializable {
	@Serial
	private static final long serialVersionUID = 2775375770782696276L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();
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
	
	public boolean qSimArcSign = false; //czy łuk między takimi EL ma być wzmocniony
	public boolean qSimDrawed = false; //czy ten EL ma być dodatkowo oznaczony

	/**
	 * Konstruktor obiektów ElementLocation.
	 * @param sheetId (<b>int</b>) identyfikator arkusza w programie.
	 * @param position (<b>Point</b>) punkt lokalizacji.
	 * @param parentNode (<b>Node</b>) wierzchołek, do którego należy lokalizacja.
	 */
	public ElementLocation(int sheetId, Point position, Node parentNode) {
		this.position = position;
		this.notSnappedPosition = position;
		this.sheetId = sheetId;
		this.parentNode = parentNode;
	}
	
	/**
	 * Metoda pozwala pobrać identyfikator arkusza.
	 * @return (<b>int</b>) - identyfikator arkusza.
	 */
	public int getSheetID() {
		return sheetId;
	}

	/**
	 * Metoda pozwala ustawić identyfikator arkusza.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 */
	public void setSheetID(int sheetId) {
		this.sheetId = sheetId;
	}

	/**
	 * Metoda pozwala pobrać punkt lokalizacji.
	 * @return position (<b>Point</b>) - punkt lokalizacji.
	 */
	public Point getPosition() {
		return position;
	}

	/**
	 * Metoda pozwala ustawić punkt lokalizacji.
	 * @param position (<b>Point</b>) punkt lokalizacji.
	 */
	public void setPosition(Point position) {
		Workspace workspace = overlord.getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(position)) {
			this.position = position;
		}
	}
	
	/**
	 * Ustawia nową lokalizację bez sprawdzania czy leży w zakresie widoczności.
	 * @param position (<b>Point</b>) pozycja.
	 */
	public void forceSetPosition(Point position) {
		this.position = position;
	}

	/**
	 * Metoda pozwala pobrać wierzchołek, do którego należy lokalizacja.
	 * @return (<b>Node</b>) - wierzchołek, do którego należy lokalizacja.
	 */
	public Node getParentNode() {
		return parentNode;
	}

	/**
	 * Metoda pozwala ustawić wierzchołek, do którego należy lokalizacja.
	 * @param parentNode (<b>Node</b>) wierzchołek, do którego należy lokalizacja.
	 */
	public void setParentNode(Node parentNode) {
		this.parentNode = parentNode;
	}

	/**
	 * Metoda przesuwa współrzędne x i y lokalizacji punktu (właściwie wektora).
	 * @param delta (<b>Point</b>) wektor przesunięcia.
	 */
	public void updateLocation(Point delta) {
		Point tempPosition = new Point(position.x + delta.x, position.y + delta.y);
		Workspace workspace = overlord.getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(tempPosition)) {
			position = tempPosition;
		}
	}
	
	/**
	 * Metoda na potrzeby klasy ProjectReader.
	 * @param p (<b>Point</b>) x, y.
	 */
	public void setNotSnappedPosition(Point p) {
		notSnappedPosition = p;
	}

	/**
	 * Metoda przesuwa współrzędne x i y lokalizacji punktu (właściwie wektora)
	 * biorąc dodatkowo pod uwagę wartości meshSize.
	 * @param delta (<b>Point</b>) wektor przesunięcia.
	 * @param meshSize (<b>int</b>) domyślnie 20, zależnie od ruchu myszy.
	 */
	public void updateLocationWithMeshSnap(Point delta, int meshSize) {
		Point notSnPos = notSnappedPosition;
		notSnappedPosition.setLocation(notSnPos.x + delta.x, notSnPos.y + delta.y);
		Point tempPosition = new Point((notSnappedPosition.x + delta.x) / meshSize * meshSize, (notSnappedPosition.y + delta.y) / meshSize * meshSize);
		Workspace workspace = overlord.getWorkspace();
		int sheetIndex = workspace.getIndexOfId(sheetId);
		if (workspace.getSheets().get(sheetIndex).getGraphPanel().isLegalLocation(tempPosition)) {
			position = tempPosition;
		}
	}

	/**
	 * Metoda pozwala pobrać ORYGINALNY obiekt listy łuków wyjściowych.
	 * @return (<b>ArrayList[Arc]</b>) lista łuków wyjściowych.
	 */
	public ArrayList<Arc> getOutArcs() {
		return outArcs;
	}

	/**
	 * Metoda pozwala ustawić listę łuków wyjściowych
	 * @param outArcs (<b>ArrayList[Arc]</b>) lista łuków wyjściowych do zastąpienia aktualnej.
	 */
	@SuppressWarnings("unused")
	public void setOutArcs(ArrayList<Arc> outArcs) {
		this.outArcs = outArcs;
	}

	/**
	 * Metoda pozwala pobrać ORYGINALNY obiekt listy łuków wejściowych.
	 * @return (<b>ArrayList[Arc]</b>) - lista łuków wejściowych.
	 */
	public ArrayList<Arc> getInArcs() {
		return inArcs;
	}

	/**
	 * Metoda pozwala ustawić listę łuków wejściowych.
	 * @param inArcs (<b>ArrayList[Arc]</b>) lista łuków wejściowych do zastąpienia aktualnej
	 */
	@SuppressWarnings("unused")
	public void setInArcs(ArrayList<Arc> inArcs) {
		this.inArcs = inArcs;
	}

	/**
	 * Metoda pozwala dodać łuk wejściowy.
	 * @param a (<b>Arc</b>) łuk wejściowy do dodania.
	 */
	public void addInArc(Arc a) {
		this.inArcs.add(a);
	}

	/**
	 * Metoda pozwala usunąć łuk wejściowy.
	 * @param a (<b>Arc</b>) łuk wejściowy do usunięcia.
	 */
	public void removeInArc(Arc a) {
		this.inArcs.remove(a);
	}

	/**
	 * Metoda pozwala dodać łuk wyjściowy.
	 * @param a (<b>Arc</b>) łuk wyjściowy.
	 */
	public void addOutArc(Arc a) {
		this.outArcs.add(a);
	}

	/**
	 * Metoda pozwala usunąć łuk wyjściowy.
	 * @param a (<b>Arc</b>) łuk wyjściowy do usunięcia.
	 */
	public void removeOutArc(Arc a) {
		this.outArcs.remove(a);
	}
	
	/**
	 * Dostęp do wektora wejściowych (wchodzących) meta-łuków.
	 * @return (<b>ArrayList[Arc]</b>) - wektor meta łuków (IN).
	 */
	public ArrayList<Arc> accessMetaInArcs() {
		return this.metaInArcs;
	}
	
	/**
	 * Dostęp do wektora wyjściowych (wychodzących) meta-łuków.
	 * @return (<b>ArrayList[Arc]</b>) - wektor meta łuków (OUT)
	 */
	public ArrayList<Arc> accessMetaOutArcs() {
		return this.metaOutArcs;
	}

	/**
	 * Metoda pozwala sprawdzić, czy lokalizacja jest oznaczona jako wybrana.
	 * @return (<b>boolean</b>) - true, jeśli lokalizacja jest oznaczona jako wybrana.
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * Metoda pozwala oznaczyć lokalizację jako wybraną, bądź taką wartość odznaczyć.
	 * @param isSelected (<b>boolean</b>) wartość oznaczenia lokalizacji jako wybranej, true jeśli zaznaczona.
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
	 * @return (<b>boolean</b>) - true, jeśli istnieje oznaczony jako wybrany odpowiedni portal.
	 */
	public boolean isPortalSelected() {
		return isPortalSelected;
	}

	/**
	 * Metoda pozwala oznaczyć portal jako wybrany, bądź taką wartość odznaczyć.
	 * @param isPortalSelected (<b>boolean</b>) true, jeśli portal jest zaznaczony.
	 */
	public void setPortalSelected(boolean isPortalSelected) {
		this.isPortalSelected = isPortalSelected;
	}

	/**
	 * Metoda zamieniająca dane o krawędzi sieci na łańcuch znaków.
	 * @return (<b>String</b>) - łańcuch znaków.
	 */
	public String toString() {
		String s;
		if(this.getParentNode() == null) {
			s = "ParentNode: null"
					+ "SheetID:" + this.getSheetID() + "; Pos:"
					+ pointPos(this.getPosition()) 
					+ "; inArcs: "+ this.getInArcs().size()
					+ "; outArcs: " + this.getOutArcs().size();
		} else {
			int index = getParentNode().getElementLocations().indexOf(this);
			
			s = "Node: " + this.getParentNode() +"("+index+") [gID:"+this.getParentNode().getID()+"];\n "
					+ "SheetID: " + this.getSheetID() + "; Pos:"
					+ pointPos(this.getPosition()) 
					+ "; inArcs: "+ this.getInArcs().size()
					+ "; outArcs: " + this.getOutArcs().size()
					+ "; mInArcs: "+ this.accessMetaInArcs().size()
					+ "; mOutArcs: " + this.accessMetaOutArcs().size();
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
