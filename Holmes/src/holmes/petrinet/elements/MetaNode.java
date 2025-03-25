package holmes.petrinet.elements;

import java.awt.Graphics2D;
import java.awt.Point;
import java.io.Serial;
import java.util.ArrayList;

import holmes.darkgui.GUIManager;
import holmes.graphpanel.ElementDraw;
import holmes.graphpanel.ElementDrawSettings;

/**
 * Meta węzeł - typ T, typ P, typ TP
 */
public class MetaNode extends Node {
	@Serial
	private static final long serialVersionUID = -1463839771476569949L;
	private static final GUIManager overlord = GUIManager.getDefaultGUIManager();

	/** SUBNETTRANS, SUBNETPLACE, SUBNET, UNKNOWN */
	public enum MetaType { SUBNETTRANS, SUBNETPLACE, SUBNET, UNKNOWN }
	private MetaType metaType;
	private int metaSheetID;

	/**
	 * Główny konstruktor meta węzła sieci.
	 * @param sheetId (<b>int</b>) identyfikator arkusza.
	 * @param nodeId (<b>int</b>) identyfikator elementu sieci Petriego.
	 * @param nodePosition (<b>Point</b> punkt, w którym znajduje się lokalizacja tego wierzchołka na odpowiednim arkuszu.
	 * @param mt (<b>MetaType</b>) typ meta-węzła .
	 */
	public MetaNode(int sheetId, int nodeId, Point nodePosition, MetaType mt) {
		super(sheetId, nodeId, nodePosition, 15);
		setType(PetriNetElementType.META);
		setMetaType(mt);	
	}
	
	/**
	 * Metoda ustawia podtyp meta-węzła.
	 * @param mt (<b>MetaType</b>) typ meta-węzła .
	 */
	public void setMetaType(MetaType mt) {
		this.metaType = mt;
	}
	
	/**
	 * Metoda zwraca podtyp meta-węzła.
	 * @return (<b>MetaType</b>) - podtyp meta-węzła.
	 */
	public MetaType getMetaType() {
		return this.metaType;
	}
	
	/**
	 * Ustawia numer arkusza który reprezentuje ten meta-węzeł.
	 * @param sheetID int - nr arkusza
	 */
	public void setRepresentedSheetID(int sheetID) {
		this.metaSheetID = sheetID;
	}
	
	/**
	 * Zwraca pierwszy Element Location meta-węzła. Czyli JEDYNY.
	 * @return ElementLocation - jw
	 */
	public ElementLocation getFirstELoc() {
		return getElementLocations().get(0);
	}
	
	/**
	 * Metoda numer arkusza który reprezentuje ten meta-węzeł.
	 * @return int - nr arkusza
	 */
	public int getRepresentedSheetID() {
		return this.metaSheetID;
	}
	
	/**
	 * Metoda zwraca ID podsieci, w którym leży ten meta-węzeł.
	 * @return int - ID podsieci z grafiką metanode'a
	 */
	public int getMySheetID() {
		return getElementLocations().get(0).getSheetID();
	}
	
	/**
	 * Metoda rysująca meta-węzeł na danym arkuszu.
	 * @param g Graphics2D - grafika 2D 
	 * @param sheetId int - identyfikator arkusza
	 * @param eds ElementDrawSettings - opcje rysowania
	 */
	public void draw(Graphics2D g, int sheetId, ElementDrawSettings eds)
	{
		ElementDraw.drawElement(this, g, sheetId, eds);
	}
	
	/**
	 * Usuwa wszystkie meta-łuki skierowane z węzła node (do metanode).
	 * @param node Node - węzeł
	 */
	public void removeAllInConnectionsWith(Node node) {
		ArrayList<Arc> metaIns = getFirstELoc().accessMetaInArcs();
		int size = metaIns.size();
		for(int i=0; i<size; i++) {
			if(metaIns.get(i).getStartNode().equals(node)) {
				Arc arc = metaIns.get(i);
				metaIns.remove(arc);
				arc.getStartLocation().accessMetaOutArcs().remove(arc);
				overlord.getWorkspace().getProject().getArcs().remove(arc);
				
				i--;
				size--;
			}
		}
	}
	
	/**
	 * Usuwa wszystkie meta-łuki skierowane do węzła node (z metanode).
	 * @param node Node - węzeł
	 */
	public void removeAllOutConnectionsWith(Node node) {
		ArrayList<Arc> metaOuts = getFirstELoc().accessMetaOutArcs();
		int size = metaOuts.size();
		for(int i=0; i<size; i++) {
			if(metaOuts.get(i).getEndNode().equals(node)) {
				Arc arc = metaOuts.get(i);
				metaOuts.remove(arc);
				arc.getEndLocation().accessMetaInArcs().remove(arc);
				overlord.getWorkspace().getProject().getArcs().remove(arc);
				
				i--;
				size--;
			}
		}
	}
	
	/**
	 * Metoda zamieniająca dane o krawędzi sieci na łańcuch znaków.
	 * @return String - łańcuch znaków
	 */
	public String toString() {
		String name =  getName();
		String text = "";
		String type = "";
		if(getMetaType() == MetaType.SUBNETPLACE)
			type = "P-type";
		else if(getMetaType() == MetaType.SUBNETTRANS)
			type = "T-type";
		else if(getMetaType() == MetaType.SUBNET)
			type = "P/T-type";
		
		text += "MetaN [sub:"+getRepresentedSheetID()+", ";
		text += type+"]  ";
		
		if(name == null) {
			return "(Meta) -- ";
		} else {
			//text = "(Meta)" + getName();
			text += "(M" + overlord.getWorkspace().getProject().getMetaNodes().indexOf(this)+")";
		}
		
		return text;
	}
}
