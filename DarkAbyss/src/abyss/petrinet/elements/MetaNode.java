package abyss.petrinet.elements;

import java.awt.Point;

public class MetaNode extends Node {
	/** CoarsePlace, CoarseTrans, SubNet, MCT */
	public enum MetaType { CoarsePlace, CoarseTrans, SubNet, MCT }
	private MetaType metaType;
	
	/**
	 * Główny konstruktor weta węzła sieci.
	 * @param sheetId int - nr okna podsieci
	 * @param nodeId int - wewnętrzny identyfikator w systemie
	 * @param nodePosition Point - współrzędne XY
	 * @param radius int - promień rysowania figury
	 * @param mt MetaType - typ meta-węzła
	 */
	public MetaNode(int sheetId, int nodeId, Point nodePosition, int radius, MetaType mt) {
		super(sheetId, nodeId, nodePosition, radius);
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
}
