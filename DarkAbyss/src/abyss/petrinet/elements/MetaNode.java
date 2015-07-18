package abyss.petrinet.elements;

import java.awt.Graphics2D;
import java.awt.Point;

import abyss.graphpanel.ElementDraw;

/**
 * Meta węzeł - coarse, MCT, etc.
 * 
 * @author MR
 *
 */
public class MetaNode extends Node {
	private static final long serialVersionUID = -1463839771476569949L;

	/** CoarsePlace, CoarseTrans, SubNet, MCT */
	public enum MetaType { CoarsePlace, CoarseTrans, SubNet, MCT }
	private MetaType metaType;
	
	private int metaSheetID;
	
	/**
	 * Główny konstruktor meta węzła sieci.
	 * @param sheetId int - nr okna podsieci
	 * @param nodeId int - wewnętrzny identyfikator w systemie
	 * @param nodePosition Point - współrzędne XY
	 * @param radius int - promień rysowania figury
	 * @param mt MetaType - typ meta-węzła
	 */
	public MetaNode(int sheetId, int nodeId, Point nodePosition, MetaType mt) {
		super(sheetId, nodeId, nodePosition, 15);
		setType(PetriNetElementType.META);
		setMetaType(mt);	
	}
	
	/**
	 * Metoda ustawia podtyp meta-węzła.
	 * @param mt MetaType - podtyp
	 */
	public void setMetaType(MetaType mt) {
		this.metaType = mt;
	}
	
	/**
	 * Metoda zwraca podtyp meta-węzła.
	 * @return MetaType - podtyp
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
	 * Metoda numer arkusza który reprezentuje ten meta-węzeł.
	 * @return int - nr arkusza
	 */
	public int getRepresentedSheetID() {
		return this.metaSheetID;
	}
	
	/**
	 * Metoda rysująca meta-węzeł na danym arkuszu.
	 * @param g Graphics2D - grafika 2D 
	 * @param sheetId int - identyfikator arkusza
	 */
	public void draw(Graphics2D g, int sheetId)
	{
		g = ElementDraw.drawElement(this, g, sheetId);
	}
	
	/**
	 * Metoda zamieniająca dane o krawędzi sieci na łańcuch znaków.
	 * @return String - łańcuch znaków
	 */
	public String toString() {
		String name =  getName();
		if(name == null) {
			return "(Meta)null";
		} else {
			return "(Meta)" + getName();
		}
	}
}
